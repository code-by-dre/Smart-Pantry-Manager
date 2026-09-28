package com.DeandreNaiker.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 2; // Version 2 for Categories

    // Pantry table details
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QTY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";
    public static final String COLUMN_PANTRY_CATEGORY = "category";

    // Recipes table details
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
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT, " +
                COLUMN_PANTRY_QTY + " REAL, " +
                COLUMN_PANTRY_UNIT + " TEXT, " +
                COLUMN_PANTRY_CATEGORY + " TEXT)";
        db.execSQL(createPantryTable);

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

    // --- PANTRY CRUD OPERATIONS ---

    public boolean insertIngredient(String name, double quantity, String unit, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PANTRY_NAME, name.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_QTY, quantity);
        contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_CATEGORY, category);

        long result = db.insert(TABLE_PANTRY, null, contentValues);
        return result != -1;
    }

    // Smart insert/update method to prevent duplicates and combine quantities
    public boolean insertOrUpdateIngredient(String name, double quantity, String unit, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        String normalizedName = name.toLowerCase().trim();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " WHERE " + COLUMN_PANTRY_NAME + " = ?", new String[]{normalizedName});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_ID));
            double existingQty = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_QTY));
            cursor.close();

            return updateIngredient(id, normalizedName, existingQty + quantity, unit);
        } else {
            cursor.close();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_PANTRY_NAME, normalizedName);
            contentValues.put(COLUMN_PANTRY_QTY, quantity);
            contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());
            contentValues.put(COLUMN_PANTRY_CATEGORY, category);

            long result = db.insert(TABLE_PANTRY, null, contentValues);
            return result != -1;
        }
    }

    public Cursor getAllIngredients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    // Fetch ingredients filtered by category (Emoji-safe check)
    public Cursor getIngredientsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Use .contains() instead of exact equality to avoid emoji encoding issues
        if (category == null || category.contains("All Categories")) {
            return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
        }

        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " WHERE " + COLUMN_PANTRY_CATEGORY + " = ?", new String[]{category});
    }

    public boolean updateIngredient(int id, String name, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PANTRY_NAME, name.toLowerCase().trim());
        contentValues.put(COLUMN_PANTRY_QTY, quantity);
        contentValues.put(COLUMN_PANTRY_UNIT, unit.toLowerCase().trim());

        int result = db.update(TABLE_PANTRY, contentValues, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public Integer deleteIngredient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

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
                ingredientNames.add(cursor.getString(0).toLowerCase().trim());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredientNames;
    }

    public void clearAllIngredients() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, null, null);
        db.close();
    }

    // --- RECIPE OPERATIONS ---

    private void insertRecipe(SQLiteDatabase db, String name, String ingredients, String steps) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RECIPE_NAME, name);
        contentValues.put(COLUMN_RECIPE_INGREDIENTS, ingredients.toLowerCase().trim());
        contentValues.put(COLUMN_RECIPE_STEPS, steps);
        db.insert(TABLE_RECIPES, null, contentValues);
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
                String ingredientsRaw = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS));
                String steps = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS));

                List<String> ingredientList = new ArrayList<>();
                for (String item : ingredientsRaw.split(",")) {
                    ingredientList.add(item.trim());
                }

                recipeList.add(new Recipe(name, ingredientList, steps));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recipeList;
    }

    private void seedDefaultRecipes(SQLiteDatabase db) {
        // Core 15
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

        // Breakfast & Bakery
        insertRecipe(db, "Avocado Toast", "bread, avocado, salt, pepper", "1. Toast bread. 2. Mash avocado. 3. Season with salt and pepper.");
        insertRecipe(db, "Oatmeal", "oats, milk, honey, cinnamon", "1. Boil milk. 2. Stir in oats. 3. Top with honey and cinnamon.");
        insertRecipe(db, "Breakfast Burrito", "tortilla, egg, cheese, bacon", "1. Cook bacon. 2. Scramble eggs. 3. Wrap in tortilla with cheese.");
        insertRecipe(db, "Waffles", "flour, egg, milk, butter, sugar", "1. Mix batter. 2. Pour into waffle iron. 3. Cook until golden.");
        insertRecipe(db, "Banana Bread", "banana, flour, sugar, egg, butter", "1. Mash bananas. 2. Mix with dry ingredients. 3. Bake at 180C for 60 mins.");
        insertRecipe(db, "Blueberry Muffins", "flour, sugar, egg, milk, blueberries", "1. Mix batter. 2. Fold in blueberries. 3. Bake at 200C for 20 mins.");
        insertRecipe(db, "Egg Muffins", "egg, spinach, cheese, onion", "1. Whisk eggs. 2. Add chopped veg and cheese. 3. Bake in muffin tin.");
        insertRecipe(db, "Hash Browns", "potato, oil, salt, pepper", "1. Grate potato. 2. Squeeze out moisture. 3. Fry in oil until crispy.");
        insertRecipe(db, "Eggs Benedict", "egg, bread, ham, butter, lemon juice", "1. Poach eggs. 2. Make hollandaise. 3. Assemble on toasted bread.");
        insertRecipe(db, "Green Smoothie", "spinach, banana, milk, honey", "1. Add ingredients to blender. 2. Blend until smooth.");

        // Pasta & Italian
        insertRecipe(db, "Spaghetti Bolognese", "pasta, beef, tomato, onion, garlic", "1. Brown beef and onion. 2. Add tomato and simmer. 3. Serve over pasta.");
        insertRecipe(db, "Fettuccine Alfredo", "pasta, cream, butter, parmesan", "1. Boil pasta. 2. Melt butter and cream. 3. Toss with cheese and pasta.");
        insertRecipe(db, "Pesto Pasta", "pasta, basil, garlic, olive oil, parmesan", "1. Blend basil, garlic, oil, cheese. 2. Toss with cooked pasta.");
        insertRecipe(db, "Mac and Cheese", "pasta, milk, butter, cheese, flour", "1. Make cheese sauce with butter, flour, milk. 2. Mix with boiled pasta.");
        insertRecipe(db, "Carbonara", "pasta, egg, bacon, parmesan, black pepper", "1. Fry bacon. 2. Toss hot pasta with egg and cheese off heat.");
        insertRecipe(db, "Penne Arrabbiata", "pasta, tomato, garlic, chili flakes, olive oil", "1. Sauté garlic and chili. 2. Add tomato. 3. Toss with penne.");
        insertRecipe(db, "Lasagna", "pasta, beef, tomato, ricotta, mozzarella", "1. Layer meat sauce, ricotta, and noodles. 2. Top with mozzarella. 3. Bake.");
        insertRecipe(db, "Mushroom Risotto", "rice, mushroom, onion, broth, parmesan", "1. Sauté onion and mushroom. 2. Slowly add broth to rice. 3. Stir in cheese.");
        insertRecipe(db, "Garlic Parmesan Pasta", "pasta, garlic, butter, parmesan", "1. Boil pasta. 2. Melt butter, sauté garlic. 3. Toss with cheese.");
        insertRecipe(db, "Pasta Primavera", "pasta, broccoli, bell pepper, olive oil, garlic", "1. Sauté veggies in oil and garlic. 2. Toss with cooked pasta.");

        // Chicken & Poultry
        insertRecipe(db, "Chicken Curry", "chicken, onion, garlic, curry powder, coconut milk", "1. Sauté chicken and onion. 2. Add spices and coconut milk. 3. Simmer.");
        insertRecipe(db, "Chicken Parmesan", "chicken, breadcrumbs, tomato, mozzarella", "1. Bread and fry chicken. 2. Top with tomato and cheese. 3. Bake.");
        insertRecipe(db, "Lemon Herb Chicken", "chicken, lemon, rosemary, olive oil, garlic", "1. Marinate chicken. 2. Grill or bake until cooked through.");
        insertRecipe(db, "Chicken Stir-Fry", "chicken, broccoli, soy sauce, garlic, ginger", "1. Sauté chicken. 2. Add veggies and soy sauce. 3. Stir-fry.");
        insertRecipe(db, "Chicken Tacos", "chicken, tortilla, cheese, lettuce, salsa", "1. Cook and shred chicken. 2. Assemble in tortillas.");
        insertRecipe(db, "Chicken Fajitas", "chicken, bell pepper, onion, tortilla", "1. Slice and sauté chicken and veggies. 2. Serve with tortillas.");
        insertRecipe(db, "BBQ Chicken", "chicken, bbq sauce, olive oil", "1. Coat chicken in oil and bake. 2. Baste with BBQ sauce and broil.");
        insertRecipe(db, "Chicken Quesadilla", "chicken, tortilla, cheese, onion", "1. Place chicken and cheese between tortillas. 2. Toast in pan.");
        insertRecipe(db, "Chicken Noodle Soup", "chicken, pasta, carrot, celery, broth", "1. Boil chicken and veg in broth. 2. Add pasta until tender.");
        insertRecipe(db, "Teriyaki Chicken", "chicken, soy sauce, sugar, ginger, garlic", "1. Sear chicken. 2. Add teriyaki sauce. 3. Simmer until thick.");

        // Beef & Pork
        insertRecipe(db, "Beef Tacos", "beef, tortilla, cheese, lettuce, tomato", "1. Brown beef with spices. 2. Assemble in tortillas.");
        insertRecipe(db, "Steak and Eggs", "steak, egg, butter, salt, pepper", "1. Sear steak in butter. 2. Fry eggs. 3. Serve together.");
        insertRecipe(db, "Beef Stroganoff", "beef, mushroom, onion, sour cream, pasta", "1. Sauté beef and mushrooms. 2. Stir in sour cream. 3. Serve over pasta.");
        insertRecipe(db, "Pork Chops", "pork, apple, onion, butter", "1. Sear pork chops. 2. Sauté apples and onions in butter. 3. Serve together.");
        insertRecipe(db, "BBQ Ribs", "pork, bbq sauce, sugar, paprika", "1. Rub ribs with spices. 2. Slow bake. 3. Baste with BBQ sauce.");
        insertRecipe(db, "Beef Chili", "beef, kidney beans, tomato, onion, chili powder", "1. Brown beef and onion. 2. Add beans, tomato, and spices. 3. Simmer.");
        insertRecipe(db, "Meatballs", "beef, pork, breadcrumbs, egg, garlic", "1. Mix ingredients. 2. Form balls. 3. Bake or fry.");
        insertRecipe(db, "Sloppy Joes", "beef, bun, tomato, onion, brown sugar", "1. Brown beef. 2. Add sweet tomato sauce. 3. Serve on buns.");
        insertRecipe(db, "Beef Stir-Fry", "beef, broccoli, soy sauce, garlic", "1. Slice beef thinly. 2. Sauté with broccoli and garlic. 3. Add soy sauce.");
        insertRecipe(db, "Pork Fried Rice", "pork, rice, egg, soy sauce, peas", "1. Scramble egg. 2. Cook pork. 3. Toss with cold rice and soy sauce.");

        // Vegetarian & Vegan
        insertRecipe(db, "Veggie Curry", "potato, carrot, coconut milk, curry powder", "1. Chop veg. 2. Simmer in coconut milk and spices until soft.");
        insertRecipe(db, "Lentil Soup", "lentils, carrot, celery, onion, broth", "1. Sauté veggies. 2. Add lentils and broth. 3. Simmer 40 mins.");
        insertRecipe(db, "Black Bean Tacos", "black beans, tortilla, corn, salsa", "1. Warm beans and corn. 2. Assemble in tortillas with salsa.");
        insertRecipe(db, "Chickpea Salad", "chickpeas, cucumber, tomato, lemon, olive oil", "1. Chop veggies. 2. Toss with chickpeas, oil, and lemon.");
        insertRecipe(db, "Stuffed Bell Peppers", "bell pepper, rice, black beans, cheese", "1. Hollow peppers. 2. Stuff with rice and beans. 3. Top with cheese and bake.");
        insertRecipe(db, "Veggie Burger", "black beans, breadcrumbs, onion, egg, bun", "1. Mash beans and mix with egg/breadcrumbs. 2. Form patties. 3. Grill.");
        insertRecipe(db, "Roasted Chickpeas", "chickpeas, olive oil, paprika, salt", "1. Toss chickpeas in oil and spices. 2. Roast at 200C for 30 mins.");
        insertRecipe(db, "Tomato Basil Soup", "tomato, basil, onion, garlic, cream", "1. Roast tomatoes and garlic. 2. Blend with basil. 3. Stir in cream.");
        insertRecipe(db, "Caprese Salad", "tomato, mozzarella, basil, olive oil", "1. Slice tomato and mozzarella. 2. Layer with basil. 3. Drizzle with oil.");
        insertRecipe(db, "Spinach Quiche", "pie crust, egg, spinach, cheese, milk", "1. Whisk eggs and milk. 2. Add spinach/cheese. 3. Pour into crust and bake.");

        // Seafood
        insertRecipe(db, "Garlic Butter Shrimp", "shrimp, butter, garlic, lemon, parsley", "1. Melt butter. 2. Sauté garlic and shrimp. 3. Finish with lemon.");
        insertRecipe(db, "Baked Salmon", "salmon, lemon, olive oil, garlic", "1. Marinate salmon. 2. Bake at 200C for 15 mins.");
        insertRecipe(db, "Tuna Melt", "bread, tuna, mayonnaise, cheese", "1. Mix tuna and mayo. 2. Top bread with tuna and cheese. 3. Grill.");
        insertRecipe(db, "Shrimp Tacos", "shrimp, tortilla, cabbage, lime", "1. Grill shrimp. 2. Assemble in tortillas with cabbage slaw.");
        insertRecipe(db, "Fish and Chips", "fish, potato, flour, oil", "1. Cut potatoes and fry. 2. Batter fish and fry until golden.");
        insertRecipe(db, "Lemon Caper Tilapia", "tilapia, butter, lemon, capers", "1. Pan-fry tilapia. 2. Make sauce with butter, lemon, capers. 3. Pour over fish.");
        insertRecipe(db, "Shrimp Scampi", "pasta, shrimp, butter, garlic, lemon", "1. Boil pasta. 2. Sauté shrimp in garlic butter. 3. Toss together.");
        insertRecipe(db, "Fish Tacos", "fish, tortilla, salsa, lime", "1. Grill or fry fish. 2. Assemble in tortillas with salsa.");
        insertRecipe(db, "Crab Cakes", "crab, mayonnaise, breadcrumbs, egg", "1. Mix ingredients. 2. Form patties. 3. Pan-fry until crispy.");
        insertRecipe(db, "Seafood Paella", "rice, shrimp, mussels, saffron, broth", "1. Toast rice in saffron broth. 2. Add seafood and simmer.");

        // Salads & Light Meals
        insertRecipe(db, "Caesar Salad", "lettuce, croutons, parmesan, chicken", "1. Chop lettuce. 2. Toss with dressing, croutons, and cheese.");
        insertRecipe(db, "Greek Salad", "cucumber, tomato, feta, olive, olive oil", "1. Chop veggies. 2. Toss with feta, olives, and oil.");
        insertRecipe(db, "Cobb Salad", "lettuce, chicken, bacon, egg, avocado", "1. Boil egg, cook bacon. 2. Assemble all ingredients over lettuce.");
        insertRecipe(db, "Quinoa Salad", "quinoa, cucumber, tomato, lemon, olive oil", "1. Cook quinoa. 2. Toss with chopped veggies and dressing.");
        insertRecipe(db, "Potato Salad", "potato, mayonnaise, mustard, celery, egg", "1. Boil potatoes and eggs. 2. Chop and mix with mayo and mustard.");
        insertRecipe(db, "Macaroni Salad", "pasta, mayonnaise, carrot, onion", "1. Boil pasta. 2. Mix with mayo and chopped veggies.");
        insertRecipe(db, "Waldorf Salad", "apple, celery, walnuts, mayonnaise", "1. Chop apples and celery. 2. Toss with nuts and mayo.");
        insertRecipe(db, "Tuna Nicoise", "tuna, potato, egg, green beans, olive", "1. Boil potatoes, beans, eggs. 2. Arrange with tuna and olives.");
        insertRecipe(db, "Fruit Salad", "apple, banana, grape, orange, honey", "1. Chop all fruit. 2. Toss in a bowl with a drizzle of honey.");
        insertRecipe(db, "Coleslaw", "cabbage, carrot, mayonnaise, vinegar", "1. Shred cabbage and carrot. 2. Toss with mayo and vinegar.");

        // Sandwiches & Wraps
        insertRecipe(db, "BLT", "bread, bacon, lettuce, tomato, mayonnaise", "1. Cook bacon. 2. Toast bread. 3. Assemble sandwich.");
        insertRecipe(db, "Club Sandwich", "bread, turkey, bacon, lettuce, tomato", "1. Toast 3 slices of bread. 2. Layer meats and veggies.");
        insertRecipe(db, "Turkey Wrap", "tortilla, turkey, cheese, lettuce, mayonnaise", "1. Spread mayo on tortilla. 2. Add turkey, cheese, lettuce. 3. Roll up.");
        insertRecipe(db, "Chicken Salad Sandwich", "bread, chicken, mayonnaise, celery", "1. Mix shredded chicken with mayo and celery. 2. Serve on bread.");
        insertRecipe(db, "Egg Salad Sandwich", "bread, egg, mayonnaise, mustard", "1. Boil and mash eggs. 2. Mix with mayo and mustard. 3. Serve on bread.");
        insertRecipe(db, "Meatball Sub", "bun, beef, tomato, cheese", "1. Cook meatballs in tomato sauce. 2. Place in bun, top with cheese, broil.");
        insertRecipe(db, "Veggie Wrap", "tortilla, hummus, cucumber, bell pepper, spinach", "1. Spread hummus. 2. Add sliced veggies. 3. Roll tightly.");
        insertRecipe(db, "Pulled Pork Sandwich", "bun, pork, bbq sauce, cabbage", "1. Slow cook pork and shred. 2. Mix with BBQ sauce. 3. Serve on bun with slaw.");
        insertRecipe(db, "Ham and Cheese", "bread, ham, cheese, butter", "1. Butter bread. 2. Layer ham and cheese. 3. Grill or eat cold.");
        insertRecipe(db, "Philly Cheesesteak", "bun, beef, onion, bell pepper, cheese", "1. Sauté beef and veg. 2. Melt cheese on top. 3. Serve in bun.");

        // Soups & Stews
        insertRecipe(db, "Minestrone", "pasta, tomato, beans, carrot, celery", "1. Sauté veggies. 2. Add broth and tomatoes. 3. Add pasta and simmer.");
        insertRecipe(db, "Chicken Tortilla Soup", "chicken, tomato, black beans, corn, tortilla", "1. Simmer chicken, beans, and corn in tomato broth. 2. Top with tortilla strips.");
        insertRecipe(db, "Broccoli Cheddar Soup", "broccoli, cheese, milk, butter, flour", "1. Make cheese sauce. 2. Boil broccoli. 3. Blend together.");
        insertRecipe(db, "French Onion Soup", "onion, beef broth, bread, cheese, butter", "1. Caramelize onions in butter. 2. Add broth. 3. Top with bread/cheese and broil.");
        insertRecipe(db, "Butternut Squash Soup", "butternut squash, onion, broth, cream", "1. Roast squash. 2. Sauté onion. 3. Blend with broth and cream.");
        insertRecipe(db, "Corn Chowder", "corn, potato, bacon, onion, cream", "1. Fry bacon. 2. Boil potato and corn in broth. 3. Stir in cream.");
        insertRecipe(db, "Split Pea Soup", "split peas, ham, carrot, onion, broth", "1. Sauté veg. 2. Add peas, ham, and broth. 3. Simmer 1.5 hours.");
        insertRecipe(db, "Mushroom Soup", "mushroom, onion, garlic, broth, cream", "1. Sauté mushrooms. 2. Add broth and simmer. 3. Blend and add cream.");
        insertRecipe(db, "Beef and Barley Soup", "beef, barley, carrot, celery, broth", "1. Brown beef. 2. Add veg, barley, and broth. 3. Simmer until tender.");
        insertRecipe(db, "Pumpkin Soup", "pumpkin, onion, garlic, coconut milk", "1. Roast pumpkin. 2. Blend with sautéed onions and coconut milk.");

        // Desserts & Snacks
        insertRecipe(db, "Chocolate Chip Cookies", "flour, butter, sugar, chocolate, egg", "1. Cream butter and sugar. 2. Add dry ingredients and chocolate. 3. Bake at 180C.");
        insertRecipe(db, "Brownies", "flour, butter, sugar, cocoa, egg", "1. Melt butter. 2. Mix all ingredients. 3. Bake at 175C for 25 mins.");
        insertRecipe(db, "Apple Pie", "pie crust, apple, sugar, cinnamon, butter", "1. Slice apples. 2. Toss with sugar/cinnamon. 3. Fill crust and bake.");
        insertRecipe(db, "Cheesecake", "cream cheese, sugar, egg, graham crackers, butter", "1. Make crust. 2. Beat cream cheese, sugar, egg. 3. Pour over crust and bake.");
        insertRecipe(db, "Hummus", "chickpeas, tahini, lemon, garlic, olive oil", "1. Blend all ingredients until smooth. 2. Drizzle with olive oil.");
        insertRecipe(db, "Guacamole", "avocado, onion, tomato, lime, cilantro", "1. Mash avocado. 2. Stir in chopped veg and lime juice.");
        insertRecipe(db, "Salsa", "tomato, onion, jalapeno, lime, cilantro", "1. Dice all vegetables finely. 2. Toss with lime juice and salt.");
        insertRecipe(db, "Bruschetta", "bread, tomato, basil, garlic, olive oil", "1. Toast bread. 2. Mix chopped tomato, basil, oil. 3. Spoon over bread.");
        insertRecipe(db, "Trail Mix", "almonds, walnuts, raisins, chocolate, pumpkin seeds", "1. Combine all ingredients in a large bowl. 2. Store in airtight container.");
        insertRecipe(db, "Garlic Knots", "pizza dough, garlic, butter, parmesan", "1. Tie dough into knots. 2. Bake until golden. 3. Toss in garlic butter.");
    }
}

