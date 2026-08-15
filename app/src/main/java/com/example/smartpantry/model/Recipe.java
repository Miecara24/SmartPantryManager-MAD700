package com.example.smartpantry.model;

import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String method;
    private List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String method, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.ingredients = ingredients;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getMethod() { return method; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
