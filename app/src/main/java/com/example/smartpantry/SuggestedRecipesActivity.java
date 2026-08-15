package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.ui.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        message = findViewById(R.id.txtMessage);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("recipe_id", recipe.getId());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        loadSuggestions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) loadSuggestions();
    }

    private void loadSuggestions() {
        List<Recipe> matches = db.getStrictSuggestedRecipes();
        adapter.setRecipes(matches);

        if (matches.isEmpty()) {
            message.setText("No recipes match your pantry yet - add more ingredients.");
        } else {
            message.setText(matches.size() + " recipe(s) can be made with your current pantry.");
        }
    }
}
