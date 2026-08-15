package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "quantity REAL NOT NULL," +
                "unit TEXT NOT NULL," +
                "expiry_date TEXT)");

        db.execSQL("CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "method TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recipe_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "quantity REAL NOT NULL," +
                "unit TEXT NOT NULL," +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry_items");
        onCreate(db);
    }

    // ---------- Pantry CRUD ----------
    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry_items", null, v);
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("pantry_items", null, null, null,
                null, null, "name COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            list.add(new PantryItem(
                    c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit")),
                    c.getString(c.getColumnIndexOrThrow("expiry_date"))
            ));
        }
        c.close();
        return list;
    }

    public PantryItem getPantryItem(long id) {
        Cursor c = getReadableDatabase().query("pantry_items", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = new PantryItem(
                    c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit")),
                    c.getString(c.getColumnIndexOrThrow("expiry_date"))
            );
        }
        c.close();
        return item;
    }

    public int updatePantryItem(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return getWritableDatabase().update("pantry_items", v, "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        return getWritableDatabase().delete("pantry_items", "id=?",
                new String[]{String.valueOf(id)});
    }

    // ---------- Recipe reads ----------
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor c = getReadableDatabase().query("recipes", null, null, null,
                null, null, "name COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow("id"));
            recipes.add(new Recipe(
                    id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("method")),
                    getRecipeIngredients(id)
            ));
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        Cursor c = getReadableDatabase().query("recipes", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("method")),
                    getRecipeIngredients(id)
            );
        }
        c.close();
        return recipe;
    }

    private List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("recipe_ingredients", null, "recipe_id=?",
                new String[]{String.valueOf(recipeId)}, null, null, "id ASC");
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))
            ));
        }
        c.close();
        return list;
    }

    // ---------- Strict matching ----------
    public List<Recipe> getStrictSuggestedRecipes() {
        List<PantryItem> pantry = getAllPantryItems();
        List<Recipe> matches = new ArrayList<>();

        for (Recipe recipe : getAllRecipes()) {
            boolean qualifies = true;

            for (RecipeIngredient required : recipe.getIngredients()) {
                PantryItem available = findPantryMatch(pantry, required.getName());

                if (available == null || available.getQuantity() + 0.0001 < required.getQuantity()) {
                    qualifies = false;
                    break;
                }

                // For a simple robust implementation, units are normalised.
                // Compatible units are converted before comparison where possible.
                double availableBase = toBaseQuantity(available.getQuantity(), available.getUnit());
                double requiredBase = toBaseQuantity(required.getQuantity(), required.getUnit());

                if (!unitsCompatible(available.getUnit(), required.getUnit()) ||
                        availableBase + 0.0001 < requiredBase) {
                    qualifies = false;
                    break;
                }
            }

            if (qualifies) matches.add(recipe);
        }
        return matches;
    }

    private PantryItem findPantryMatch(List<PantryItem> pantry, String requiredName) {
        String target = normaliseIngredient(requiredName);
        for (PantryItem item : pantry) {
            if (normaliseIngredient(item.getName()).equals(target)) return item;
        }
        return null;
    }

    private String normaliseIngredient(String value) {
        String s = value.toLowerCase(Locale.ROOT).trim();
        if (s.endsWith("ies")) s = s.substring(0, s.length() - 3) + "y";
        else if (s.endsWith("oes")) s = s.substring(0, s.length() - 2);
        else if (s.endsWith("s") && !s.endsWith("ss")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private boolean unitsCompatible(String a, String b) {
        String x = normaliseUnit(a);
        String y = normaliseUnit(b);
        if (x.equals(y)) return true;
        if (isMass(x) && isMass(y)) return true;
        if (isVolume(x) && isVolume(y)) return true;
        if (x.equals("item") && y.equals("item")) return true;
        return false;
    }

    private boolean isMass(String u) {
        return u.equals("g") || u.equals("kg");
    }

    private boolean isVolume(String u) {
        return u.equals("ml") || u.equals("l");
    }

    private String normaliseUnit(String unit) {
        String u = unit.toLowerCase(Locale.ROOT).trim();
        if (u.equals("grams") || u.equals("gram")) return "g";
        if (u.equals("kilograms") || u.equals("kilogram")) return "kg";
        if (u.equals("millilitres") || u.equals("millilitre") || u.equals("milliliters") || u.equals("milliliter")) return "ml";
        if (u.equals("litres") || u.equals("litre") || u.equals("liters") || u.equals("liter")) return "l";
        if (u.equals("pieces") || u.equals("piece") || u.equals("items")) return "item";
        return u;
    }

    private double toBaseQuantity(double quantity, String unit) {
        String u = normaliseUnit(unit);
        if (u.equals("kg")) return quantity * 1000.0;
        if (u.equals("l")) return quantity * 1000.0;
        return quantity;
    }

    // ---------- Recipe seed data ----------
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Pasta",
                "Boil the pasta. Cook the tomato and onion in a pan, add pasta and seasoning, then serve.",
                new String[]{"pasta","tomato","onion","salt"},
                new double[]{200,2,1,2}, new String[]{"g","item","item","g"});

        addRecipe(db, "Egg Fried Rice",
                "Cook the rice, scramble the eggs, add vegetables and soy sauce, then stir-fry together.",
                new String[]{"rice","egg","carrot","soy sauce"},
                new double[]{200,2,1,20}, new String[]{"g","item","item","ml"});

        addRecipe(db, "Chicken Stir Fry",
                "Slice chicken and vegetables. Fry chicken, add vegetables and soy sauce, and cook until tender.",
                new String[]{"chicken","pepper","onion","soy sauce"},
                new double[]{250,1,1,20}, new String[]{"g","item","item","ml"});

        addRecipe(db, "Omelette",
                "Beat eggs with salt. Cook in a pan and fold with onion and cheese.",
                new String[]{"egg","onion","cheese","salt"},
                new double[]{3,1,50,2}, new String[]{"item","item","g","g"});

        addRecipe(db, "Vegetable Soup",
                "Chop the vegetables, add to water with seasoning and simmer until soft.",
                new String[]{"carrot","potato","onion","salt"},
                new double[]{2,2,1,2}, new String[]{"item","item","item","g"});

        addRecipe(db, "Chicken Sandwich",
                "Cook the chicken, place it on bread with tomato and lettuce, then serve.",
                new String[]{"chicken","bread","tomato","lettuce"},
                new double[]{150,2,1,30}, new String[]{"g","item","item","g"});

        addRecipe(db, "Pancakes",
                "Mix flour, milk and egg into a batter. Cook portions on a lightly greased pan.",
                new String[]{"flour","milk","egg"},
                new double[]{200,250,2}, new String[]{"g","ml","item"});

        addRecipe(db, "French Toast",
                "Dip bread in beaten egg and milk, then fry until golden.",
                new String[]{"bread","egg","milk"},
                new double[]{2,2,100}, new String[]{"item","item","ml"});

        addRecipe(db, "Mashed Potatoes",
                "Boil potatoes until soft, mash with butter and milk, and season with salt.",
                new String[]{"potato","butter","milk","salt"},
                new double[]{4,30,100,2}, new String[]{"item","g","ml","g"});

        addRecipe(db, "Beef Pasta",
                "Brown the beef, add tomato and cooked pasta, season and simmer briefly.",
                new String[]{"beef","pasta","tomato","onion"},
                new double[]{250,200,2,1}, new String[]{"g","g","item","item"});

        addRecipe(db, "Chicken Curry",
                "Brown chicken and onion, add curry seasoning and tomato, then simmer until cooked.",
                new String[]{"chicken","onion","tomato","curry powder"},
                new double[]{250,1,2,10}, new String[]{"g","item","item","g"});

        addRecipe(db, "Tuna Salad",
                "Combine tuna with lettuce, tomato and onion. Mix and serve chilled.",
                new String[]{"tuna","lettuce","tomato","onion"},
                new double[]{150,50,1,1}, new String[]{"g","g","item","item"});

        addRecipe(db, "Garlic Bread",
                "Mix butter with garlic, spread on bread and bake until crisp.",
                new String[]{"bread","butter","garlic"},
                new double[]{4,40,2}, new String[]{"item","g","item"});

        addRecipe(db, "Cheese Toast",
                "Place cheese on bread and toast until the cheese melts.",
                new String[]{"bread","cheese"},
                new double[]{2,60}, new String[]{"item","g"});

        addRecipe(db, "Rice and Beans",
                "Cook rice and beans, season with salt and combine before serving.",
                new String[]{"rice","beans","salt"},
                new double[]{200,200,2}, new String[]{"g","g","g"});

        addRecipe(db, "Beef Burger",
                "Shape seasoned beef into a patty, cook thoroughly and serve in bread with lettuce.",
                new String[]{"beef","bread","lettuce"},
                new double[]{200,1,30}, new String[]{"g","item","g"});

        addRecipe(db, "Chicken Rice Bowl",
                "Cook rice and chicken, add carrot and soy sauce, then serve in a bowl.",
                new String[]{"chicken","rice","carrot","soy sauce"},
                new double[]{200,200,1,20}, new String[]{"g","g","item","ml"});

        addRecipe(db, "Tomato Omelette",
                "Beat eggs, add chopped tomato and onion, then cook until set.",
                new String[]{"egg","tomato","onion"},
                new double[]{3,1,1}, new String[]{"item","item","item"});

        addRecipe(db, "Vegetable Pasta",
                "Cook pasta and sauté the vegetables. Combine and season before serving.",
                new String[]{"pasta","carrot","pepper","onion"},
                new double[]{200,1,1,1}, new String[]{"g","item","item","item"});

        addRecipe(db, "Creamy Chicken Pasta",
                "Cook chicken, add milk and cheese, then combine with cooked pasta.",
                new String[]{"chicken","pasta","milk","cheese"},
                new double[]{200,200,150,50}, new String[]{"g","g","ml","g"});
    }

    private void addRecipe(SQLiteDatabase db, String name, String method,
                           String[] names, double[] quantities, String[] units) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("method", method);
        long recipeId = db.insert("recipes", null, recipe);

        for (int i = 0; i < names.length; i++) {
            ContentValues ingredient = new ContentValues();
            ingredient.put("recipe_id", recipeId);
            ingredient.put("name", names[i]);
            ingredient.put("quantity", quantities[i]);
            ingredient.put("unit", units[i]);
            db.insert("recipe_ingredients", null, ingredient);
        }
    }
}
