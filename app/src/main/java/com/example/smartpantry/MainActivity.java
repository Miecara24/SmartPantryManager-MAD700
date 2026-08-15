package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.ui.PantryAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        count = findViewById(R.id.txtPantryCount);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.Listener() {
            @Override public void onEdit(PantryItem item) {
                Intent i = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                i.putExtra("id", item.getId());
                startActivity(i);
            }

            @Override public void onDelete(PantryItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete ingredient")
                        .setMessage("Delete " + item.getName() + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deletePantryItem(item.getId());
                            loadPantry();
                            Toast.makeText(MainActivity.this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);

        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        findViewById(R.id.btnRecipes).setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        loadPantry();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) loadPantry();
    }

    private void loadPantry() {
        adapter.setItems(db.getAllPantryItems());
        count.setText(db.getAllPantryItems().size() + " ingredients stored");
    }
}
