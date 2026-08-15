package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.PantryItem;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {
    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private final Listener listener;

    public PantryAdapter(List<PantryItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<PantryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.getName());
        h.quantity.setText(item.getQuantity() + " " + item.getUnit() +
                (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()
                        ? "" : " • Expiry: " + item.getExpiryDate()));
        h.edit.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity;
        Button edit, delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtName);
            quantity = itemView.findViewById(R.id.txtQuantity);
            edit = itemView.findViewById(R.id.btnEdit);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
