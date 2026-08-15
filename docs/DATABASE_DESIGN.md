# Database design

## pantry_items
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- name: TEXT NOT NULL
- quantity: REAL NOT NULL
- unit: TEXT NOT NULL
- expiry_date: TEXT

## recipes
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- name: TEXT NOT NULL
- method: TEXT NOT NULL

## recipe_ingredients
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- recipe_id: INTEGER NOT NULL
- name: TEXT NOT NULL
- quantity: REAL NOT NULL
- unit: TEXT NOT NULL

Relationship:
recipes (1) ---- (many) recipe_ingredients

The pantry_items table stores the user's current stock. The recipe tables store the seeded recipe collection. Strict matching checks every recipe_ingredient against pantry_items before a recipe is returned.
