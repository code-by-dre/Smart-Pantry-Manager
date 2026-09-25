package com.DeandreNaiker.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // db information
    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    // the pantry table details
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QTY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";

    // the recipes table details
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_ID = "id";
    public static final String COLUMN_RECIPE_NAME = "name";
    public static final String COLUMN_RECIPE_INGREDIENTS = "ingredients_required";
    public static final String COLUMN_RECIPE_STEPS = "steps";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry Table
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT, " +
                COLUMN_PANTRY_QTY + " REAL, " +
                COLUMN_PANTRY_UNIT + " TEXT)";
        db.execSQL(createPantryTable);

        // creating the recipes table
        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT, " +
                COLUMN_RECIPE_INGREDIENTS + " TEXT, " +
                COLUMN_RECIPE_STEPS + " TEXT)";
        db.execSQL(createRecipesTable);

        seedDefaultRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // the CRUD operations for the pantry

    // 1. adding a new ingredient
    public boolean insertIngredient(String name, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        // storing all values in lowercase
        contentValues.put(COLUMN_PANTRY_NAME, name.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_QTY, quantity);
        contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());

        long result = db.insert(TABLE_PANTRY, null, contentValues);
        return result != -1;
    }

    // 2. getting all the pantry items
    public Cursor getAllIngredients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    // 3. editing an existing pantry items quantity or the details
    public boolean updateIngredient(int id, String name, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PANTRY_NAME, name.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_QTY, quantity);
        contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());

        // updating the row where the ID matches
        int result = db.update(TABLE_PANTRY, contentValues, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0; // and then returning true if the update was successful
    }

    // 4. removing an ingredient or item from the pantry
    public Integer deleteIngredient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // deleting the row where the ID matches
        return db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // the CRUD operations for the recipes

    // the method to insert a single recipe
    private void insertRecipe(SQLiteDatabase db, String name, String ingredients, String steps) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RECIPE_NAME, name);
        // storing all values in lowercase
        contentValues.put(COLUMN_RECIPE_INGREDIENTS, ingredients.toLowerCase().trim());
        contentValues.put(COLUMN_RECIPE_STEPS, steps);
        db.insert(TABLE_RECIPES, null, contentValues);
    }

    // deleting an ingredient
    public void deleteIngredient(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COLUMN_PANTRY_NAME + " = ?", new String[]{name});
        db.close();
    }

    public List<String> getPantryIngredientNames() {
        List<String> ingredientNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PANTRY_NAME + " FROM " + TABLE_PANTRY, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0).toLowerCase().trim();
                ingredientNames.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredientNames;
    }

    // to clear all ingredients from the database
    public void clearAllIngredients() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, null, null);
        db.close();
    }

    // the method to preload the 15 recipes
    private void seedDefaultRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Tomato Soup", "tomato, onion, garlic", "1. Chop vegetables. 2. Boil. 3. Blend.");
        insertRecipe(db, "Scrambled Eggs", "egg, butter, milk, salt", "1. Whisk eggs and milk. 2. Melt butter. 3. Cook.");
        insertRecipe(db, "Pasta Sauce", "pasta, tomato, garlic, olive oil", "1. Boil pasta. 2. Sauté garlic. 3. Add tomatoes.");
        insertRecipe(db, "Grilled Cheese", "bread, cheese, butter", "1. Butter bread. 2. Add cheese. 3. Grill in pan.");
        insertRecipe(db, "Pancakes", "flour, egg, milk, butter", "1. Mix ingredients. 2. Pour on hot pan. 3. Flip.");
        insertRecipe(db, "Omelette", "egg, cheese, onion, salt", "1. Whisk eggs. 2. Cook in pan. 3. Add cheese and fold.");
        insertRecipe(db, "Garlic Bread", "bread, butter, garlic", "1. Mix butter and garlic. 2. Spread on bread. 3. Bake.");
        insertRecipe(db, "Mashed Potatoes", "potato, butter, milk, salt", "1. Boil potatoes. 2. Mash with butter and milk.");
        insertRecipe(db, "Fried Rice", "rice, egg, onion, soy sauce", "1. Scramble egg. 2. Sauté onion. 3. Fry with rice.");
        insertRecipe(db, "French Toast", "bread, egg, milk, butter", "1. Whisk egg and milk. 2. Dip bread. 3. Fry in butter.");
        insertRecipe(db, "Roast Chicken", "chicken, olive oil, salt, garlic", "1. Rub chicken with oil. 2. Roast at 200C for 1 hour.");
        insertRecipe(db, "Boiled Corn", "corn, butter, salt", "1. Boil corn for 10 mins. 2. Serve with butter and salt.");
        insertRecipe(db, "Tuna Salad", "tuna, mayonnaise, onion", "1. Drain tuna. 2. Chop onion. 3. Mix together.");
        insertRecipe(db, "Peanut Butter Sandwich", "bread, peanut butter, jam", "1. Spread peanut butter. 2. Spread jam. 3. Combine.");
        insertRecipe(db, "Fruit Smoothie", "banana, apple, milk", "1. Chop fruit. 2. Add milk. 3. Blend until smooth.");
    }
}

