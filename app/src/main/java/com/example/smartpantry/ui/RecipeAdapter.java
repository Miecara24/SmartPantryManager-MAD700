package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface Listener { void onRecipeSelected(Recipe recipe); }

    private List<Recipe> recipes;
    private final Listener listener;

    public RecipeAdapter(List<Recipe> recipes, Listener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Recipe recipe = recipes.get(position);
        h.name.setText(recipe.getName());
        h.summary.setText(recipe.getIngredients().size() + " required ingredients");
        h.itemView.setOnClickListener(v -> listener.onRecipeSelected(recipe));
    }

    @Override
    public int getItemCount() { return recipes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, summary;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtRecipeName);
            summary = itemView.findViewById(R.id.txtRecipeSummary);
        }
    }
}
