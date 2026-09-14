package com.DeandreNaiker.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry Table Details
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QTY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";

    // Recipes Table Details
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

        // Create Recipes Table
        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT, " +
                COLUMN_RECIPE_INGREDIENTS + " TEXT, " +
                COLUMN_RECIPE_STEPS + " TEXT)";
        db.execSQL(createRecipesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // --- CRUD OPERATIONS FOR PANTRY ---

    // 1. CREATE: Add a new ingredient
    public boolean insertIngredient(String name, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        // Storing in lowercase helps with the strict-matching rule later
        contentValues.put(COLUMN_PANTRY_NAME, name.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_QTY, quantity);
        contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());

        long result = db.insert(TABLE_PANTRY, null, contentValues);
        return result != -1;
    }

    // 2. READ: Get all pantry items
    public Cursor getAllIngredients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }
}

