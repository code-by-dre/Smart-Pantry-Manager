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
    private static final int DATABASE_VERSION = 3; // Upgraded to 3 to load the new detailed recipes

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

    public Cursor getIngredientsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
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
        insertRecipe(db, "Tomato Soup", "tomato, onion, garlic",
                "1. Heat olive oil in a large pot over medium heat.\n\n" +
                        "2. Add finely diced onions and sauté until translucent (about 5 mins).\n\n" +
                        "3. Stir in minced garlic and cook for 1 minute until fragrant.\n\n" +
                        "4. Add chopped tomatoes, bring to a gentle boil, and simmer for 20 minutes.\n\n" +
                        "5. Carefully blend the soup until smooth and serve hot.");

        insertRecipe(db, "Scrambled Eggs", "egg, butter, milk, salt",
                "1. Crack eggs into a bowl, add a splash of milk, and whisk thoroughly until combined.\n\n" +
                        "2. Melt butter in a non-stick pan over medium-low heat.\n\n" +
                        "3. Pour in the egg mixture and gently stir continuously as curds form.\n\n" +
                        "4. Remove from heat just before they are fully set, as they will continue to cook in the pan.\n\n" +
                        "5. Season with a pinch of salt and serve immediately.");

        insertRecipe(db, "Pasta Sauce", "pasta, tomato, garlic, olive oil",
                "1. Bring a large pot of salted water to a boil and cook pasta until al dente.\n\n" +
                        "2. In a separate pan, heat olive oil over medium heat and sauté minced garlic until golden.\n\n" +
                        "3. Add chopped tomatoes, reduce heat, and simmer for 15 minutes to thicken the sauce.\n\n" +
                        "4. Drain the pasta and toss it directly into the tomato sauce to coat thoroughly.\n\n" +
                        "5. Serve hot with a drizzle of fresh olive oil.");

        insertRecipe(db, "Grilled Cheese", "bread, cheese, butter",
                "1. Generously butter one side of each slice of bread.\n\n" +
                        "2. Place one slice, butter-side down, in a skillet over medium-low heat.\n\n" +
                        "3. Layer your preferred cheese evenly over the bread in the skillet.\n\n" +
                        "4. Top with the second slice of bread, butter-side up.\n\n" +
                        "5. Grill until the bottom is golden brown, flip carefully, and cook until the cheese is melted.");

        insertRecipe(db, "Pancakes", "flour, egg, milk, butter",
                "1. In a large bowl, whisk together the flour, egg, and milk until a smooth batter forms.\n\n" +
                        "2. Heat a lightly oiled griddle or pan over medium-high heat.\n\n" +
                        "3. Pour or scoop the batter onto the griddle, using approximately 1/4 cup for each pancake.\n\n" +
                        "4. Cook until bubbles form on the surface and the edges look dry, then flip.\n\n" +
                        "5. Cook until lightly browned on the other side and serve warm with butter.");

        insertRecipe(db, "Omelette", "egg, cheese, onion, salt",
                "1. Whisk eggs in a bowl with a pinch of salt until perfectly blended.\n\n" +
                        "2. Heat oil or butter in a pan over medium heat and sauté finely chopped onions until soft.\n\n" +
                        "3. Pour the eggs over the onions and let sit until the edges start to set.\n\n" +
                        "4. Sprinkle grated cheese over one half of the omelette.\n\n" +
                        "5. Gently fold the omelette in half and slide it onto a plate.");

        insertRecipe(db, "Garlic Bread", "bread, butter, garlic",
                "1. Preheat your oven to 180°C (350°F).\n\n" +
                        "2. In a small bowl, mix softened butter with finely minced garlic.\n\n" +
                        "3. Slice the bread horizontally and spread the garlic butter generously over the cut sides.\n\n" +
                        "4. Place the bread on a baking sheet, butter side up.\n\n" +
                        "5. Bake for 10-15 minutes until the edges are crispy and golden brown.");

        insertRecipe(db, "Mashed Potatoes", "potato, butter, milk, salt",
                "1. Peel and chop potatoes into even cubes.\n\n" +
                        "2. Place in a large pot of salted water, bring to a boil, and cook until fork-tender (about 15 mins).\n\n" +
                        "3. Drain the potatoes well and return them to the warm pot.\n\n" +
                        "4. Add butter and a splash of milk, then mash thoroughly until smooth and creamy.\n\n" +
                        "5. Season with salt to taste and serve hot.");

        insertRecipe(db, "Fried Rice", "rice, egg, onion, soy sauce",
                "1. Heat a tablespoon of oil in a wok or large skillet over medium-high heat.\n\n" +
                        "2. Scramble the egg quickly, then push it to the side of the pan.\n\n" +
                        "3. Add chopped onions and sauté until translucent.\n\n" +
                        "4. Add cold, pre-cooked rice and stir-fry, breaking up any clumps.\n\n" +
                        "5. Drizzle with soy sauce, toss everything together evenly, and serve.");

        insertRecipe(db, "French Toast", "bread, egg, milk, butter",
                "1. In a shallow dish, whisk together the egg and milk.\n\n" +
                        "2. Melt a small amount of butter in a skillet over medium heat.\n\n" +
                        "3. Dip each slice of bread into the egg mixture, ensuring both sides are coated.\n\n" +
                        "4. Place the bread in the skillet and cook until golden brown on the bottom.\n\n" +
                        "5. Flip and cook the other side until golden, then serve warm.");

        insertRecipe(db, "Roast Chicken", "chicken, olive oil, salt, garlic",
                "1. Preheat your oven to 200°C (400°F).\n\n" +
                        "2. Pat the chicken dry with paper towels, then rub generously with olive oil.\n\n" +
                        "3. Season all over with salt and rub with minced garlic, ensuring it gets under the skin.\n\n" +
                        "4. Place the chicken in a roasting pan and roast for about 1 hour.\n\n" +
                        "5. Ensure the juices run clear, let it rest for 10 minutes, then carve and serve.");

        insertRecipe(db, "Boiled Corn", "corn, butter, salt",
                "1. Bring a large pot of water to a rolling boil.\n\n" +
                        "2. Husk the corn and carefully drop the cobs into the boiling water.\n\n" +
                        "3. Boil uncovered for 5 to 7 minutes until the kernels are tender.\n\n" +
                        "4. Remove the corn from the water with tongs and drain briefly.\n\n" +
                        "5. Smear with butter, sprinkle with salt, and serve hot.");

        insertRecipe(db, "Tuna Salad", "tuna, mayonnaise, onion",
                "1. Drain the canned tuna thoroughly and place it in a medium mixing bowl.\n\n" +
                        "2. Finely dice the onion and add it to the bowl.\n\n" +
                        "3. Add the mayonnaise to the tuna and onions.\n\n" +
                        "4. Use a fork to flake the tuna and mix everything together until well combined.\n\n" +
                        "5. Serve chilled on bread, crackers, or over a bed of greens.");

        insertRecipe(db, "Peanut Butter Sandwich", "bread, peanut butter, jam",
                "1. Lay two slices of bread flat on a clean surface.\n\n" +
                        "2. Spread an even layer of peanut butter over one slice of bread.\n\n" +
                        "3. Spread an even layer of jam over the other slice of bread.\n\n" +
                        "4. Press the two slices together, spread sides facing inward.\n\n" +
                        "5. Cut in half diagonally and serve.");

        insertRecipe(db, "Fruit Smoothie", "banana, apple, milk",
                "1. Peel the banana and core the apple, then chop both into chunks.\n\n" +
                        "2. Place the chopped fruit into a blender.\n\n" +
                        "3. Pour in the milk, ensuring it covers about half the fruit.\n\n" +
                        "4. Blend on high speed until completely smooth and frothy.\n\n" +
                        "5. Pour into a tall glass and serve immediately.");

        // Breakfast & Bakery
        insertRecipe(db, "Avocado Toast", "bread, avocado, salt, pepper",
                "1. Toast the bread until it reaches your desired crispness.\n\n" +
                        "2. Slice the avocado in half, remove the pit, and scoop the flesh into a bowl.\n\n" +
                        "3. Mash the avocado roughly with a fork, keeping some texture.\n\n" +
                        "4. Spread the mashed avocado evenly over the warm toast.\n\n" +
                        "5. Sprinkle generously with salt and pepper before serving.");

        insertRecipe(db, "Oatmeal", "oats, milk, honey, cinnamon",
                "1. Combine oats and milk in a saucepan over medium heat.\n\n" +
                        "2. Bring to a gentle simmer, stirring frequently to prevent sticking.\n\n" +
                        "3. Cook for 5-7 minutes until the oats have absorbed the liquid and become creamy.\n\n" +
                        "4. Remove from heat and transfer to a bowl.\n\n" +
                        "5. Drizzle with honey and sprinkle with cinnamon before eating.");

        insertRecipe(db, "Breakfast Burrito", "tortilla, egg, cheese, bacon",
                "1. Cook the bacon in a skillet until crispy, then chop into bite-sized pieces.\n\n" +
                        "2. In the same pan, scramble the eggs until fully cooked.\n\n" +
                        "3. Lay the tortilla flat and place the scrambled eggs and bacon in the center.\n\n" +
                        "4. Sprinkle a generous amount of cheese over the hot filling to melt it.\n\n" +
                        "5. Fold the sides of the tortilla inward and roll tightly to form a burrito.");

        insertRecipe(db, "Waffles", "flour, egg, milk, butter, sugar",
                "1. Preheat your waffle iron according to its instructions.\n\n" +
                        "2. Whisk the flour and sugar together in a large mixing bowl.\n\n" +
                        "3. In a separate bowl, combine the egg, milk, and melted butter, then pour into the dry ingredients.\n\n" +
                        "4. Mix until just combined (some lumps are fine).\n\n" +
                        "5. Pour the batter onto the hot iron and cook until golden and crisp.");

        insertRecipe(db, "Banana Bread", "banana, flour, sugar, egg, butter",
                "1. Preheat oven to 180°C (350°F) and grease a loaf pan.\n\n" +
                        "2. In a bowl, mash the ripe bananas completely with a fork.\n\n" +
                        "3. Stir in melted butter, sugar, and the egg until smooth.\n\n" +
                        "4. Gently fold in the flour until the batter is just mixed.\n\n" +
                        "5. Pour into the pan and bake for 60 minutes until a toothpick comes out clean.");

        insertRecipe(db, "Blueberry Muffins", "flour, sugar, egg, milk, blueberries",
                "1. Preheat your oven to 200°C (400°F) and line a muffin tin with paper cups.\n\n" +
                        "2. Whisk together the flour, sugar, egg, and milk until a thick batter forms.\n\n" +
                        "3. Very gently fold in the blueberries so they don't break.\n\n" +
                        "4. Spoon the batter into the muffin cups, filling them 3/4 of the way to the top.\n\n" +
                        "5. Bake for 20 minutes until the tops are golden and spring back when touched.");

        insertRecipe(db, "Egg Muffins", "egg, spinach, cheese, onion",
                "1. Preheat oven to 190°C (375°F) and lightly grease a muffin tin.\n\n" +
                        "2. Finely chop the spinach and onion, and divide them evenly among the muffin cups.\n\n" +
                        "3. Whisk the eggs thoroughly in a bowl and pour the mixture over the veggies in the tin.\n\n" +
                        "4. Top each cup with a sprinkle of grated cheese.\n\n" +
                        "5. Bake for 15-20 minutes until the eggs are puffed and set.");

        insertRecipe(db, "Hash Browns", "potato, oil, salt, pepper",
                "1. Peel the potatoes and grate them using a box grater.\n\n" +
                        "2. Wrap the grated potatoes in a clean towel and squeeze out as much moisture as possible.\n\n" +
                        "3. Heat oil in a large skillet over medium-high heat.\n\n" +
                        "4. Spread the potatoes in an even layer in the pan and season with salt and pepper.\n\n" +
                        "5. Cook until the bottom is dark brown and crispy, then flip and crisp the other side.");

        insertRecipe(db, "Eggs Benedict", "egg, bread, ham, butter, lemon juice",
                "1. Toast the bread and lightly pan-fry the ham slices until warm.\n\n" +
                        "2. Whisk melted butter and lemon juice rapidly over a double boiler to create hollandaise sauce.\n\n" +
                        "3. Bring a pot of water to a gentle simmer, create a whirlpool, and poach the eggs for 3 minutes.\n\n" +
                        "4. Layer the ham on the toasted bread and carefully place a poached egg on top.\n\n" +
                        "5. Drizzle generously with the warm hollandaise sauce and serve.");

        insertRecipe(db, "Green Smoothie", "spinach, banana, milk, honey",
                "1. Add a handful of fresh spinach and a peeled banana to a blender.\n\n" +
                        "2. Pour in the milk to help the ingredients blend smoothly.\n\n" +
                        "3. Add a squeeze of honey for natural sweetness.\n\n" +
                        "4. Blend on high speed for at least 60 seconds to ensure there are no leafy chunks.\n\n" +
                        "5. Pour into a glass and enjoy immediately.");

        // Pasta & Italian
        insertRecipe(db, "Spaghetti Bolognese", "pasta, beef, tomato, onion, garlic",
                "1. Boil the pasta in a large pot of salted water until al dente.\n\n" +
                        "2. In a deep pan, brown the minced beef and chopped onions over medium-high heat.\n\n" +
                        "3. Add minced garlic and cook for 1 more minute until fragrant.\n\n" +
                        "4. Pour in the chopped tomatoes, reduce the heat, and let the sauce simmer for 20 minutes.\n\n" +
                        "5. Serve the rich meat sauce over the hot cooked pasta.");

        insertRecipe(db, "Fettuccine Alfredo", "pasta, cream, butter, parmesan",
                "1. Cook the pasta in heavily salted boiling water until tender, then drain.\n\n" +
                        "2. In a skillet over medium-low heat, melt the butter and gently stir in the cream.\n\n" +
                        "3. Let the mixture simmer for 2 minutes to slightly thicken.\n\n" +
                        "4. Remove from heat and aggressively whisk in the grated parmesan until a smooth sauce forms.\n\n" +
                        "5. Toss the hot pasta directly in the pan until fully coated in the Alfredo sauce.");

        insertRecipe(db, "Pesto Pasta", "pasta, basil, garlic, olive oil, parmesan",
                "1. Boil the pasta until cooked to your liking, reserving a little pasta water before draining.\n\n" +
                        "2. In a food processor, blend the fresh basil, garlic, parmesan, and olive oil into a smooth paste.\n\n" +
                        "3. Transfer the drained pasta to a large serving bowl.\n\n" +
                        "4. Spoon the pesto over the pasta, adding a splash of pasta water to loosen the sauce.\n\n" +
                        "5. Toss well until every noodle is green and coated.");

        insertRecipe(db, "Mac and Cheese", "pasta, milk, butter, cheese, flour",
                "1. Boil the pasta until tender, then drain and set aside.\n\n" +
                        "2. In a pot, melt the butter, whisk in the flour, and cook for 1 minute to make a roux.\n\n" +
                        "3. Gradually whisk in the milk, stirring constantly until the sauce thickens and bubbles.\n\n" +
                        "4. Remove from heat, stir in the grated cheese until completely melted into a smooth sauce.\n\n" +
                        "5. Fold the cooked pasta into the cheese sauce and serve hot.");

        insertRecipe(db, "Carbonara", "pasta, egg, bacon, parmesan, black pepper",
                "1. Boil the pasta in salted water. In a separate bowl, whisk the egg and parmesan together.\n\n" +
                        "2. Chop and fry the bacon in a pan until crispy and golden.\n\n" +
                        "3. Drain the pasta, reserving a small cup of pasta water, and add the pasta to the bacon pan off the heat.\n\n" +
                        "4. Quickly pour in the egg and cheese mixture, tossing rapidly so the eggs create a creamy sauce without scrambling.\n\n" +
                        "5. Season heavily with freshly cracked black pepper and serve immediately.");

        insertRecipe(db, "Penne Arrabbiata", "pasta, tomato, garlic, chili flakes, olive oil",
                "1. Cook the penne in boiling salted water until al dente.\n\n" +
                        "2. Heat olive oil in a pan and gently fry the minced garlic and chili flakes for 30 seconds.\n\n" +
                        "3. Add the chopped tomatoes to the pan and simmer for 15 minutes until the sauce thickens.\n\n" +
                        "4. Drain the pasta and immediately toss it into the spicy tomato sauce.\n\n" +
                        "5. Mix well over low heat for 1 minute so the pasta absorbs the flavors.");

        insertRecipe(db, "Lasagna", "pasta, beef, tomato, ricotta, mozzarella",
                "1. Brown the beef in a pan, add the tomatoes, and simmer to create a thick meat sauce.\n\n" +
                        "2. In a baking dish, spread a thin layer of meat sauce on the bottom.\n\n" +
                        "3. Layer pasta sheets, ricotta cheese, and meat sauce repeatedly until the dish is full.\n\n" +
                        "4. Top the final layer generously with shredded mozzarella cheese.\n\n" +
                        "5. Bake at 190°C (375°F) for 45 minutes until bubbly and golden on top.");

        insertRecipe(db, "Mushroom Risotto", "rice, mushroom, onion, broth, parmesan",
                "1. In a large pan, sauté chopped onions and sliced mushrooms in a little oil until soft and brown.\n\n" +
                        "2. Add the dry rice and toast it in the pan for 1 minute, stirring constantly.\n\n" +
                        "3. Keep the broth warm on the stove, and begin adding it to the rice one ladle at a time.\n\n" +
                        "4. Stir the rice continuously until the liquid is absorbed before adding the next ladle.\n\n" +
                        "5. Once the rice is creamy and tender, remove from heat and stir in the parmesan cheese.");

        insertRecipe(db, "Garlic Parmesan Pasta", "pasta, garlic, butter, parmesan",
                "1. Boil the pasta in salted water until tender, then drain thoroughly.\n\n" +
                        "2. In a skillet, melt a generous block of butter over medium heat.\n\n" +
                        "3. Add finely minced garlic and sauté gently for 1-2 minutes until fragrant but not brown.\n\n" +
                        "4. Toss the hot pasta into the garlic butter to coat completely.\n\n" +
                        "5. Remove from heat, toss in the grated parmesan cheese, and serve immediately.");

        insertRecipe(db, "Pasta Primavera", "pasta, broccoli, bell pepper, olive oil, garlic",
                "1. Boil the pasta, adding the chopped broccoli during the last 3 minutes of cooking.\n\n" +
                        "2. In a large pan, heat olive oil and sauté minced garlic and sliced bell peppers until tender.\n\n" +
                        "3. Drain the pasta and broccoli, then add them directly to the pan with the peppers.\n\n" +
                        "4. Toss everything together, adding an extra drizzle of olive oil for moisture.\n\n" +
                        "5. Season with salt and pepper and serve warm.");

        // Chicken & Poultry
        insertRecipe(db, "Chicken Curry", "chicken, onion, garlic, curry powder, coconut milk",
                "1. Chop the chicken into bite-sized cubes.\n\n" +
                        "2. Sauté chopped onions and garlic in a deep pan until soft and golden.\n\n" +
                        "3. Add the chicken and cook until the outside is browned.\n\n" +
                        "4. Stir in the curry powder to coat the chicken, then pour in the coconut milk.\n\n" +
                        "5. Simmer on low heat for 20 minutes until the chicken is cooked through and the sauce thickens.");

        insertRecipe(db, "Chicken Parmesan", "chicken, breadcrumbs, tomato, mozzarella",
                "1. Pound chicken breasts flat, coat them in breadcrumbs, and fry in oil until golden and crispy.\n\n" +
                        "2. Transfer the crispy chicken to a baking sheet.\n\n" +
                        "3. Spoon a generous layer of thick tomato sauce over each piece of chicken.\n\n" +
                        "4. Top the sauce with slices of mozzarella cheese.\n\n" +
                        "5. Bake at 200°C (400°F) until the cheese is melted, bubbly, and slightly browned.");

        insertRecipe(db, "Lemon Herb Chicken", "chicken, lemon, rosemary, olive oil, garlic",
                "1. In a bowl, whisk together olive oil, lemon juice, minced garlic, and chopped rosemary.\n\n" +
                        "2. Place the chicken in the marinade, coat well, and let it rest for at least 30 minutes.\n\n" +
                        "3. Heat a grill or a heavy skillet over medium-high heat.\n\n" +
                        "4. Cook the chicken for 6-8 minutes on each side until fully cooked through.\n\n" +
                        "5. Let the chicken rest for 5 minutes before slicing and serving.");

        insertRecipe(db, "Chicken Stir-Fry", "chicken, broccoli, soy sauce, garlic, ginger",
                "1. Slice the chicken into very thin strips for quick cooking.\n\n" +
                        "2. Heat oil in a wok or large pan and quickly sear the chicken until just browned, then remove it.\n\n" +
                        "3. In the same pan, stir-fry the broccoli, minced garlic, and ginger until vibrant and tender-crisp.\n\n" +
                        "4. Return the chicken to the pan and pour over the soy sauce.\n\n" +
                        "5. Toss everything together over high heat for 2 minutes and serve immediately.");

        insertRecipe(db, "Chicken Tacos", "chicken, tortilla, cheese, lettuce, salsa",
                "1. Cook the chicken in a skillet with your favorite spices until completely done.\n\n" +
                        "2. Use two forks to shred the cooked chicken into small pieces.\n\n" +
                        "3. Warm the tortillas in a dry pan or microwave for 15 seconds.\n\n" +
                        "4. Fill each tortilla with the shredded chicken, fresh lettuce, and grated cheese.\n\n" +
                        "5. Top with salsa and serve immediately.");

        insertRecipe(db, "Chicken Fajitas", "chicken, bell pepper, onion, tortilla",
                "1. Slice the chicken, bell peppers, and onions into long, thin strips.\n\n" +
                        "2. Heat a large skillet over high heat with a splash of oil.\n\n" +
                        "3. Toss the chicken into the pan, searing until browned, then add the peppers and onions.\n\n" +
                        "4. Sauté rapidly until the vegetables are charred but still slightly crunchy.\n\n" +
                        "5. Serve sizzling hot directly into warm tortillas.");

        insertRecipe(db, "BBQ Chicken", "chicken, bbq sauce, olive oil",
                "1. Preheat oven to 200°C (400°F) and lightly coat the chicken pieces in olive oil.\n\n" +
                        "2. Place on a baking sheet and bake for 25 minutes until mostly cooked through.\n\n" +
                        "3. Remove from the oven and brush a thick layer of BBQ sauce over all sides of the chicken.\n\n" +
                        "4. Return to the oven and bake for another 10-15 minutes until the sauce is sticky and caramelized.\n\n" +
                        "5. Let rest briefly before serving.");

        insertRecipe(db, "Chicken Quesadilla", "chicken, tortilla, cheese, onion",
                "1. Dice and cook the chicken and onions in a skillet until fully done.\n\n" +
                        "2. Place a large tortilla flat in a clean skillet over medium-low heat.\n\n" +
                        "3. Sprinkle cheese over half the tortilla, add the chicken mixture, and top with more cheese.\n\n" +
                        "4. Fold the empty half of the tortilla over the filling and press down gently with a spatula.\n\n" +
                        "5. Cook until the bottom is crispy, flip carefully, and crisp the other side until cheese melts.");

        insertRecipe(db, "Chicken Noodle Soup", "chicken, pasta, carrot, celery, broth",
                "1. In a large pot, bring the broth to a gentle simmer.\n\n" +
                        "2. Add chopped carrots and celery, cooking for 10 minutes until they start to soften.\n\n" +
                        "3. Add chopped raw chicken breast and simmer until the chicken is cooked through.\n\n" +
                        "4. Stir in the pasta and cook according to the package directions until tender.\n\n" +
                        "5. Taste for seasoning, ladling into bowls to serve hot.");

        insertRecipe(db, "Teriyaki Chicken", "chicken, soy sauce, sugar, ginger, garlic",
                "1. Cut chicken into bite-sized chunks and sear in a hot pan until golden on all sides.\n\n" +
                        "2. In a small bowl, whisk together soy sauce, sugar, minced garlic, and grated ginger.\n\n" +
                        "3. Pour the sauce mixture over the cooked chicken in the pan.\n\n" +
                        "4. Reduce the heat and let it simmer rapidly until the sauce reduces into a thick, sticky glaze.\n\n" +
                        "5. Toss to ensure all pieces are coated and serve over rice.");

        // Beef & Pork
        insertRecipe(db, "Beef Tacos", "beef, tortilla, cheese, lettuce, tomato",
                "1. Add minced beef to a skillet over medium-high heat and break it apart as it cooks.\n\n" +
                        "2. Drain any excess fat, then stir in your preferred taco spices and a splash of water.\n\n" +
                        "3. Simmer until the water evaporates and the meat is richly flavored.\n\n" +
                        "4. Warm the tortillas and chop the lettuce and tomatoes.\n\n" +
                        "5. Assemble the tacos by layering the beef, lettuce, tomatoes, and cheese.");

        insertRecipe(db, "Steak and Eggs", "steak, egg, butter, salt, pepper",
                "1. Season the steak heavily with salt and coarse black pepper.\n\n" +
                        "2. Heat a cast-iron skillet until smoking hot, add butter, and sear the steak (3-4 mins per side for medium-rare).\n\n" +
                        "3. Remove the steak to a cutting board and let it rest for 5 minutes.\n\n" +
                        "4. Crack the eggs directly into the beef fat left in the skillet and fry to your liking.\n\n" +
                        "5. Slice the rested steak against the grain and serve alongside the eggs.");

        insertRecipe(db, "Beef Stroganoff", "beef, mushroom, onion, sour cream, pasta",
                "1. Slice beef into thin strips and sear quickly in a hot pan, then set aside.\n\n" +
                        "2. In the same pan, sauté sliced mushrooms and chopped onions until browned and soft.\n\n" +
                        "3. Return the beef to the pan, lower the heat, and stir in the sour cream to create a rich sauce.\n\n" +
                        "4. Do not let the sour cream boil to prevent splitting.\n\n" +
                        "5. Serve immediately over a bed of freshly boiled pasta.");

        insertRecipe(db, "Pork Chops", "pork, apple, onion, butter",
                "1. Season the pork chops with salt and pepper and sear in a hot skillet until cooked through, then remove.\n\n" +
                        "2. Add butter to the pan along with sliced apples and thinly sliced onions.\n\n" +
                        "3. Sauté over medium heat until the apples are tender and the onions are deeply caramelized.\n\n" +
                        "4. Return the pork chops to the pan for 1 minute to warm through and soak up the juices.\n\n" +
                        "5. Serve the chops topped with the sweet and savory apple-onion mixture.");

        insertRecipe(db, "BBQ Ribs", "pork, bbq sauce, sugar, paprika",
                "1. Massage the ribs thoroughly with a dry rub made of sugar, paprika, salt, and pepper.\n\n" +
                        "2. Wrap the ribs tightly in foil and bake slowly at 135°C (275°F) for 2.5 to 3 hours until tender.\n\n" +
                        "3. Remove from the foil and place the ribs on a baking sheet.\n\n" +
                        "4. Brush generously with BBQ sauce and broil on high for 5 minutes until the sauce caramelizes and bubbles.\n\n" +
                        "5. Cut into individual ribs and serve sticky and hot.");

        insertRecipe(db, "Beef Chili", "beef, kidney beans, tomato, onion, chili powder",
                "1. Brown the minced beef and chopped onion in a large pot until fully cooked.\n\n" +
                        "2. Stir in a generous amount of chili powder and cook for 1 minute to toast the spices.\n\n" +
                        "3. Add the chopped tomatoes and the drained kidney beans to the pot.\n\n" +
                        "4. Bring to a boil, then reduce heat and simmer uncovered for at least 30 minutes to thicken.\n\n" +
                        "5. Serve hot, optionally topped with cheese or sour cream.");

        insertRecipe(db, "Meatballs", "beef, pork, breadcrumbs, egg, garlic",
                "1. In a large bowl, use your hands to mix the beef, pork, breadcrumbs, egg, and minced garlic.\n\n" +
                        "2. Roll the mixture gently into golf-ball-sized spheres, being careful not to pack them too tightly.\n\n" +
                        "3. Heat a thin layer of oil in a skillet and brown the meatballs on all sides.\n\n" +
                        "4. Transfer the browned meatballs to a baking dish and bake at 190°C (375°F) for 15 minutes.\n\n" +
                        "5. Serve plain or simmered in your favorite sauce.");

        insertRecipe(db, "Sloppy Joes", "beef, bun, tomato, onion, brown sugar",
                "1. Brown the ground beef and diced onion in a skillet, draining off the excess fat.\n\n" +
                        "2. Stir in tomato sauce (or ketchup) and a spoonful of brown sugar for sweetness.\n\n" +
                        "3. Simmer the mixture on low heat for 10 minutes until the sauce reduces and clings tightly to the meat.\n\n" +
                        "4. Toast the hamburger buns lightly in a pan or oven.\n\n" +
                        "5. Spoon the messy, rich meat mixture generously onto the buns and serve hot.");

        insertRecipe(db, "Beef Stir-Fry", "beef, broccoli, soy sauce, garlic",
                "1. Slice the beef against the grain into extremely thin strips.\n\n" +
                        "2. Sear the beef quickly in a smoking hot wok or skillet, then remove it to a plate.\n\n" +
                        "3. Add chopped broccoli and minced garlic to the pan with a splash of water to steam briefly.\n\n" +
                        "4. When the broccoli is tender-crisp, return the beef to the pan and pour over the soy sauce.\n\n" +
                        "5. Toss continuously for 1 minute until everything is coated and hot.");

        insertRecipe(db, "Pork Fried Rice", "pork, rice, egg, soy sauce, peas",
                "1. Dice the pork into small cubes and cook in a hot skillet until browned and cooked through, then set aside.\n\n" +
                        "2. Scramble the egg in the pan, chopping it into small pieces as it cooks.\n\n" +
                        "3. Add cold, day-old rice and the peas to the pan, stirring vigorously to break up clumps.\n\n" +
                        "4. Add the cooked pork back in and drizzle everything evenly with soy sauce.\n\n" +
                        "5. Stir-fry for 3-4 minutes until the rice is hot and slightly toasted.");

        // Vegetarian & Vegan
        insertRecipe(db, "Veggie Curry", "potato, carrot, coconut milk, curry powder",
                "1. Peel and chop the potatoes and carrots into even, bite-sized cubes.\n\n" +
                        "2. Heat a tablespoon of oil in a pot, add curry powder, and toast for 30 seconds until fragrant.\n\n" +
                        "3. Toss the chopped vegetables into the spices, then pour in the coconut milk.\n\n" +
                        "4. Bring to a gentle boil, then turn down the heat and simmer for 25 minutes.\n\n" +
                        "5. Check that the potatoes are fork-tender before serving warm over rice.");

        insertRecipe(db, "Lentil Soup", "lentils, carrot, celery, onion, broth",
                "1. Dice the carrots, celery, and onions and sauté them in a large soup pot until soft.\n\n" +
                        "2. Rinse the lentils under cold water, picking out any debris, and add them to the pot.\n\n" +
                        "3. Pour in the broth and bring the soup to a rolling boil.\n\n" +
                        "4. Reduce the heat, cover, and let simmer gently for 35-40 minutes until the lentils are completely tender.\n\n" +
                        "5. Season with salt and pepper to taste before serving.");

        insertRecipe(db, "Black Bean Tacos", "black beans, tortilla, corn, salsa",
                "1. Rinse and drain the black beans, then warm them in a small saucepan over medium heat.\n\n" +
                        "2. Stir the corn kernels into the beans and heat through.\n\n" +
                        "3. Lightly toast the tortillas in a dry skillet until pliable and warm.\n\n" +
                        "4. Spoon the bean and corn mixture into the center of each tortilla.\n\n" +
                        "5. Top generously with salsa and fold to serve.");

        insertRecipe(db, "Chickpea Salad", "chickpeas, cucumber, tomato, lemon, olive oil",
                "1. Rinse and thoroughly drain the chickpeas, placing them in a large salad bowl.\n\n" +
                        "2. Dice the cucumber and tomatoes into small, even pieces and add them to the bowl.\n\n" +
                        "3. In a separate small dish, whisk together fresh lemon juice, olive oil, salt, and pepper.\n\n" +
                        "4. Pour the dressing over the vegetables and chickpeas.\n\n" +
                        "5. Toss well and let sit for 10 minutes so the flavors meld before serving.");

        insertRecipe(db, "Stuffed Bell Peppers", "bell pepper, rice, black beans, cheese",
                "1. Cut the tops off the bell peppers and carefully remove all seeds and membranes from the inside.\n\n" +
                        "2. In a bowl, mix cooked rice, black beans, and half of your grated cheese.\n\n" +
                        "3. Stuff the hollowed-out peppers tightly with the rice and bean mixture.\n\n" +
                        "4. Place upright in a baking dish, top with the remaining cheese, and bake at 190°C (375°F) for 35 mins.\n\n" +
                        "5. The peppers should be tender and the cheese melted and browned.");

        insertRecipe(db, "Veggie Burger", "black beans, breadcrumbs, onion, egg, bun",
                "1. Drain and roughly mash the black beans in a bowl with a fork (leave some chunks for texture).\n\n" +
                        "2. Finely dice the onion and mix it into the beans along with the breadcrumbs and the egg to bind.\n\n" +
                        "3. Shape the mixture firmly into burger-sized patties using your hands.\n\n" +
                        "4. Heat oil in a skillet and pan-fry the patties for 4-5 minutes per side until deeply browned and crisp.\n\n" +
                        "5. Serve hot on toasted buns with your favorite burger toppings.");

        insertRecipe(db, "Roasted Chickpeas", "chickpeas, olive oil, paprika, salt",
                "1. Preheat oven to 200°C (400°F). Drain and thoroughly dry the chickpeas with paper towels (crucial for crunch).\n\n" +
                        "2. Toss the dry chickpeas in a bowl with olive oil, paprika, and salt until evenly coated.\n\n" +
                        "3. Spread them out in a single layer on a large baking sheet.\n\n" +
                        "4. Roast for 25-30 minutes, shaking the pan halfway through, until crispy and deeply golden.\n\n" +
                        "5. Let cool slightly before eating for maximum crunch.");

        insertRecipe(db, "Tomato Basil Soup", "tomato, basil, onion, garlic, cream",
                "1. Roughly chop the tomatoes and sauté them in a pot with chopped onions and garlic until broken down.\n\n" +
                        "2. Add a cup of water or broth, bring to a boil, and simmer for 15 minutes.\n\n" +
                        "3. Stir in a large handful of fresh basil leaves and turn off the heat.\n\n" +
                        "4. Use an immersion blender to purée the soup until completely smooth.\n\n" +
                        "5. Stir in a splash of cream, season, and serve warm.");

        insertRecipe(db, "Caprese Salad", "tomato, mozzarella, basil, olive oil",
                "1. Slice the tomatoes and the fresh mozzarella cheese into thick, even rounds.\n\n" +
                        "2. Arrange the slices on a serving platter, alternating tomato, mozzarella, and a whole basil leaf.\n\n" +
                        "3. Drizzle high-quality olive oil generously over the entire arrangement.\n\n" +
                        "4. Sprinkle with coarse sea salt and freshly cracked black pepper.\n\n" +
                        "5. Serve immediately as a fresh, light appetizer.");

        insertRecipe(db, "Spinach Quiche", "pie crust, egg, spinach, cheese, milk",
                "1. Preheat oven to 190°C (375°F) and press the pie crust into a tart or pie pan.\n\n" +
                        "2. Sauté the spinach until wilted, squeeze out all excess liquid, and spread it over the bottom of the crust.\n\n" +
                        "3. Whisk the eggs and milk together in a bowl until frothy, and stir in the grated cheese.\n\n" +
                        "4. Pour the egg mixture carefully into the pie crust over the spinach.\n\n" +
                        "5. Bake for 35-40 minutes until the center is just set and the top is golden.");

        // Seafood
        insertRecipe(db, "Garlic Butter Shrimp", "shrimp, butter, garlic, lemon, parsley",
                "1. Melt the butter in a large skillet over medium-high heat.\n\n" +
                        "2. Add minced garlic and cook for 1 minute until fragrant but not browned.\n\n" +
                        "3. Add the peeled shrimp in a single layer and cook for 2 minutes per side until pink and opaque.\n\n" +
                        "4. Remove from heat immediately, squeeze fresh lemon juice over the top, and toss with chopped parsley.\n\n" +
                        "5. Serve hot directly from the pan.");

        insertRecipe(db, "Baked Salmon", "salmon, lemon, olive oil, garlic",
                "1. Preheat your oven to 200°C (400°F) and line a baking tray with foil.\n\n" +
                        "2. Place the salmon skin-side down and rub the flesh with olive oil and minced garlic.\n\n" +
                        "3. Layer thin slices of lemon directly on top of the salmon fillets.\n\n" +
                        "4. Bake for 12-15 minutes, depending on thickness, until the fish flakes easily with a fork.\n\n" +
                        "5. Serve immediately with the roasted lemon slices.");

        insertRecipe(db, "Tuna Melt", "bread, tuna, mayonnaise, cheese",
                "1. Mix the drained tuna thoroughly with mayonnaise in a small bowl.\n\n" +
                        "2. Spread the tuna mixture in a thick layer onto a slice of bread.\n\n" +
                        "3. Top with a generous slice of melting cheese (like cheddar or Swiss).\n\n" +
                        "4. Place on a baking sheet and broil in the oven for 3-5 minutes until the cheese is bubbling and golden.\n\n" +
                        "5. Watch closely so the bread does not burn, then serve hot.");

        insertRecipe(db, "Shrimp Tacos", "shrimp, tortilla, cabbage, lime",
                "1. Toss the shrimp in a little oil and your favorite spices.\n\n" +
                        "2. Sear the shrimp quickly in a hot pan for 2-3 minutes until pink and fully cooked.\n\n" +
                        "3. Very finely shred the raw cabbage and toss it with fresh lime juice.\n\n" +
                        "4. Warm the tortillas, then fill them with the hot shrimp and top with the crunchy cabbage slaw.\n\n" +
                        "5. Serve immediately with extra lime wedges.");

        insertRecipe(db, "Fish and Chips", "fish, potato, flour, oil",
                "1. Cut the potatoes into thick fries and deep-fry or bake them until golden and crispy.\n\n" +
                        "2. Whisk flour with a little water (or beer) to create a smooth, thick batter.\n\n" +
                        "3. Dip thick pieces of white fish into the batter, letting excess drip off.\n\n" +
                        "4. Carefully drop the fish into hot oil and fry for 5-7 minutes until the batter is deep golden and crisp.\n\n" +
                        "5. Drain on paper towels, season heavily with salt, and serve with the fries.");

        insertRecipe(db, "Lemon Caper Tilapia", "tilapia, butter, lemon, capers",
                "1. Season the tilapia fillets with salt and pepper and pan-fry in a little oil for 3-4 minutes per side until cooked.\n\n" +
                        "2. Remove the fish to a warm plate and lower the heat in the pan.\n\n" +
                        "3. Melt butter in the pan, then stir in lemon juice and capers, scraping up any browned bits.\n\n" +
                        "4. Simmer the sauce for 1 minute until slightly thickened.\n\n" +
                        "5. Pour the hot, tangy sauce over the fish and serve.");

        insertRecipe(db, "Shrimp Scampi", "pasta, shrimp, butter, garlic, lemon",
                "1. Boil the pasta in salted water until al dente, then drain.\n\n" +
                        "2. In a large skillet, melt butter and gently sauté minced garlic until fragrant.\n\n" +
                        "3. Add the shrimp and cook rapidly until they turn pink and opaque.\n\n" +
                        "4. Remove from heat, toss in the hot pasta, and squeeze fresh lemon juice over everything.\n\n" +
                        "5. Toss vigorously to create a light, buttery sauce that coats the noodles.");

        insertRecipe(db, "Fish Tacos", "fish, tortilla, salsa, lime",
                "1. Pan-fry or bake flaky white fish until it easily falls apart with a fork.\n\n" +
                        "2. Flake the cooked fish into chunks.\n\n" +
                        "3. Warm the tortillas on a dry skillet until soft.\n\n" +
                        "4. Load the tortillas with the fish chunks and top with a generous spoonful of salsa.\n\n" +
                        "5. Squeeze fresh lime juice over the top just before eating.");

        insertRecipe(db, "Crab Cakes", "crab, mayonnaise, breadcrumbs, egg",
                "1. Pick through the crab meat to remove any shell fragments, then place in a bowl.\n\n" +
                        "2. Gently fold in mayonnaise, breadcrumbs, and the egg without breaking up the crab chunks too much.\n\n" +
                        "3. Form the mixture lightly into thick patties.\n\n" +
                        "4. Heat oil in a skillet and gently pan-fry the cakes for 4-5 minutes per side until golden brown and crispy.\n\n" +
                        "5. Serve hot, optionally with a squeeze of lemon.");

        insertRecipe(db, "Seafood Paella", "rice, shrimp, mussels, saffron, broth",
                "1. Heat oil in a large, wide pan and briefly toast the rice along with a pinch of saffron.\n\n" +
                        "2. Pour in the broth and bring to a simmer; do not stir the rice from this point on.\n\n" +
                        "3. After 10 minutes of simmering, arrange the shrimp and scrubbed mussels neatly on top of the rice.\n\n" +
                        "4. Cover and cook for another 10 minutes until the rice is tender, the shrimp are pink, and the mussels open.\n\n" +
                        "5. Discard any unopened mussels, let the pan rest for 5 minutes, and serve family-style.");

        // Salads & Light Meals
        insertRecipe(db, "Caesar Salad", "lettuce, croutons, parmesan, chicken",
                "1. Grill or pan-fry the chicken breast until cooked, let it rest, and slice it thickly.\n\n" +
                        "2. Chop crisp lettuce and place it into a large serving bowl.\n\n" +
                        "3. Add the croutons and a generous handful of shaved parmesan cheese.\n\n" +
                        "4. Drizzle with Caesar dressing and toss thoroughly so every leaf is coated.\n\n" +
                        "5. Top with the warm sliced chicken and serve immediately.");

        insertRecipe(db, "Greek Salad", "cucumber, tomato, feta, olive, olive oil",
                "1. Roughly chop the cucumber and tomatoes into large, bite-sized chunks.\n\n" +
                        "2. Place the vegetables in a bowl and add a handful of whole Kalamata olives.\n\n" +
                        "3. Cut the feta cheese into large blocks and gently place them on top of the salad.\n\n" +
                        "4. Drizzle generously with high-quality olive oil and a pinch of salt and oregano.\n\n" +
                        "5. Do not toss heavily; serve fresh and rustic.");

        insertRecipe(db, "Cobb Salad", "lettuce, chicken, bacon, egg, avocado",
                "1. Hard-boil the egg, cook the bacon until crisp, and dice the cooked chicken and avocado.\n\n" +
                        "2. Create a bed of chopped lettuce on a large platter or wide bowl.\n\n" +
                        "3. Arrange the chicken, crumbled bacon, chopped egg, and diced avocado in neat, individual rows across the lettuce.\n\n" +
                        "4. Serve with dressing on the side so diners can toss it themselves on their plates.\n\n" +
                        "5. Enjoy fresh while the avocado is perfectly green.");

        insertRecipe(db, "Quinoa Salad", "quinoa, cucumber, tomato, lemon, olive oil",
                "1. Cook the quinoa in boiling water until the grains pop and are tender (about 15 mins), then let cool completely.\n\n" +
                        "2. Finely dice the cucumber and tomatoes and add them to a large bowl.\n\n" +
                        "3. Fluff the cooled quinoa with a fork and gently mix it into the vegetables.\n\n" +
                        "4. Whisk lemon juice and olive oil together to create a light dressing.\n\n" +
                        "5. Pour over the salad, toss well, and let sit for 15 minutes before serving.");

        insertRecipe(db, "Potato Salad", "potato, mayonnaise, mustard, celery, egg",
                "1. Boil the potatoes and eggs until fully cooked; drain and let them cool.\n\n" +
                        "2. Peel and chop the cooled potatoes and eggs into bite-sized chunks.\n\n" +
                        "3. Finely dice the celery for crunch and add it to a large mixing bowl with the potatoes and eggs.\n\n" +
                        "4. In a separate bowl, mix mayonnaise and a spoonful of mustard, then fold this dressing into the potato mix.\n\n" +
                        "5. Chill in the refrigerator for at least 1 hour before serving to let the flavors meld.");

        insertRecipe(db, "Macaroni Salad", "pasta, mayonnaise, carrot, onion",
                "1. Boil the macaroni pasta until tender, drain, and rinse under cold water to stop the cooking process.\n\n" +
                        "2. Grate the carrot and finely mince the onion.\n\n" +
                        "3. In a large bowl, whisk mayonnaise with a pinch of salt and pepper to create a dressing.\n\n" +
                        "4. Add the cold pasta, carrot, and onion to the bowl and stir well to ensure the pasta is heavily coated.\n\n" +
                        "5. Cover and refrigerate until serving.");

        insertRecipe(db, "Waldorf Salad", "apple, celery, walnuts, mayonnaise",
                "1. Core the apples and chop them into crisp, bite-sized cubes.\n\n" +
                        "2. Finely slice the celery to add a distinct crunch.\n\n" +
                        "3. Roughly chop the walnuts.\n\n" +
                        "4. Place all ingredients into a bowl and dollop with just enough mayonnaise to bind everything together lightly.\n\n" +
                        "5. Toss gently and serve chilled as a refreshing side dish.");

        insertRecipe(db, "Tuna Nicoise", "tuna, potato, egg, green beans, olive",
                "1. Boil the potatoes, green beans, and eggs until tender but not mushy, then cool them.\n\n" +
                        "2. Halve the potatoes and eggs, and arrange them neatly on a large serving plate alongside the green beans.\n\n" +
                        "3. Place chunks of high-quality canned or seared fresh tuna in the center.\n\n" +
                        "4. Scatter olives over the dish.\n\n" +
                        "5. Drizzle the entire composed salad with a light olive oil dressing just before serving.");

        insertRecipe(db, "Fruit Salad", "apple, banana, grape, orange, honey",
                "1. Peel the banana and orange, and core the apple.\n\n" +
                        "2. Chop all the fruit into uniformly sized pieces so it is easy to eat.\n\n" +
                        "3. Place the chopped fruit and whole grapes into a large serving bowl.\n\n" +
                        "4. Drizzle lightly with honey to enhance the natural sweetness.\n\n" +
                        "5. Toss very gently to avoid turning the banana into mush, and serve fresh.");

        insertRecipe(db, "Coleslaw", "cabbage, carrot, mayonnaise, vinegar",
                "1. Finely shred the cabbage using a knife or food processor.\n\n" +
                        "2. Grate the carrots on the large holes of a box grater and mix them with the cabbage in a large bowl.\n\n" +
                        "3. In a small cup, whisk together mayonnaise and a splash of vinegar to create a tangy dressing.\n\n" +
                        "4. Pour the dressing over the vegetables and toss vigorously until every strand is coated.\n\n" +
                        "5. Let rest in the fridge for 30 minutes to soften slightly before eating.");

        // Sandwiches & Wraps
        insertRecipe(db, "BLT", "bread, bacon, lettuce, tomato, mayonnaise",
                "1. Fry or bake the bacon strips until they are extremely crispy, then drain on paper towels.\n\n" +
                        "2. Toast two slices of bread to a deep golden brown.\n\n" +
                        "3. Slather mayonnaise generously on one side of each slice of toasted bread.\n\n" +
                        "4. Layer fresh lettuce leaves, thick slices of juicy tomato, and the crispy bacon between the bread.\n\n" +
                        "5. Press gently, cut in half, and eat immediately while the toast is warm and the bacon is crunchy.");

        insertRecipe(db, "Club Sandwich", "bread, turkey, bacon, lettuce, tomato",
                "1. Toast three slices of bread per sandwich and cook the bacon until crispy.\n\n" +
                        "2. On the bottom slice, spread mayo and layer turkey, lettuce, and tomato.\n\n" +
                        "3. Place the middle slice of toast on top, spread with mayo, and add the crispy bacon and more lettuce.\n\n" +
                        "4. Cap with the final slice of toast.\n\n" +
                        "5. Secure the tall sandwich with toothpicks in four corners and cut diagonally into quarters.");

        insertRecipe(db, "Turkey Wrap", "tortilla, turkey, cheese, lettuce, mayonnaise",
                "1. Lay a large tortilla flat on a clean surface and spread a thin layer of mayonnaise down the center.\n\n" +
                        "2. Arrange slices of turkey and cheese evenly over the mayo.\n\n" +
                        "3. Top with a handful of crisp, shredded lettuce.\n\n" +
                        "4. Fold the left and right sides of the tortilla inward over the filling.\n\n" +
                        "5. Roll tightly from the bottom up, slice in half diagonally, and serve.");

        insertRecipe(db, "Chicken Salad Sandwich", "bread, chicken, mayonnaise, celery",
                "1. Shred or finely chop the cooked, cooled chicken and place it in a bowl.\n\n" +
                        "2. Finely dice the celery and add it to the chicken for crunch.\n\n" +
                        "3. Stir in mayonnaise until the mixture reaches your preferred creamy consistency.\n\n" +
                        "4. Season with salt and pepper to taste.\n\n" +
                        "5. Scoop generously between two slices of fresh or toasted bread and serve.");

        insertRecipe(db, "Egg Salad Sandwich", "bread, egg, mayonnaise, mustard",
                "1. Hard-boil the eggs, plunge them into an ice bath, peel, and roughly chop them into a bowl.\n\n" +
                        "2. Add a dollop of mayonnaise and a small squeeze of mustard for tanginess.\n\n" +
                        "3. Mash gently with a fork until the ingredients are combined but still slightly chunky.\n\n" +
                        "4. Season with salt and pepper.\n\n" +
                        "5. Spoon the rich egg salad between two soft slices of bread and enjoy.");

        insertRecipe(db, "Meatball Sub", "bun, beef, tomato, cheese",
                "1. Warm pre-cooked meatballs by simmering them gently in a rich tomato sauce.\n\n" +
                        "2. Slice a long sub or hoagie roll open, keeping the hinge intact.\n\n" +
                        "3. Carefully spoon 3 to 4 saucy meatballs into the bun.\n\n" +
                        "4. Top generously with sliced or grated melting cheese.\n\n" +
                        "5. Place under a hot broiler for 2 minutes until the cheese is bubbling and golden, then serve carefully.");

        insertRecipe(db, "Veggie Wrap", "tortilla, hummus, cucumber, bell pepper, spinach",
                "1. Lay a large tortilla flat and smear a thick, even layer of hummus across the middle.\n\n" +
                        "2. Slice the cucumber and bell pepper into long, thin strips (julienne).\n\n" +
                        "3. Pile a handful of fresh spinach leaves over the hummus.\n\n" +
                        "4. Lay the vegetable strips neatly across the spinach.\n\n" +
                        "5. Tuck the sides in and roll the tortilla tightly to hold all the crisp vegetables together.");

        insertRecipe(db, "Pulled Pork Sandwich", "bun, pork, bbq sauce, cabbage",
                "1. Use two forks to thoroughly shred slow-cooked, tender pork while it is still warm.\n\n" +
                        "2. Toss the shredded meat with your favorite BBQ sauce until it is sticky and well-coated.\n\n" +
                        "3. Slice a soft burger bun in half and lightly toast the insides.\n\n" +
                        "4. Pile a large mound of the hot BBQ pork onto the bottom bun.\n\n" +
                        "5. Top the meat with a handful of raw, shredded cabbage for crunch, add the top bun, and serve.");

        insertRecipe(db, "Ham and Cheese", "bread, ham, cheese, butter",
                "1. Butter one side of two slices of bread.\n\n" +
                        "2. On the unbuttered side of one slice, lay down your cheese and layer the ham on top.\n\n" +
                        "3. Close the sandwich so the buttered sides are facing outward.\n\n" +
                        "4. Place in a skillet over medium-low heat and cook until the bottom is golden brown.\n\n" +
                        "5. Flip and cook the other side until the bread is crispy and the cheese has completely melted.");

        insertRecipe(db, "Philly Cheesesteak", "bun, beef, onion, bell pepper, cheese",
                "1. Thinly slice the beef, onions, and bell peppers.\n\n" +
                        "2. Heat oil in a large skillet or griddle and sauté the vegetables until soft and caramelized, then push to one side.\n\n" +
                        "3. Add the beef to the hot pan and cook rapidly until browned.\n\n" +
                        "4. Mix the meat and vegetables together into a tight pile shaped like your bun, and lay the cheese over the top to melt.\n\n" +
                        "5. Place the opened bun directly over the pile to steam, then use a spatula to scoop the whole thing into the bun.");

        // Soups & Stews
        insertRecipe(db, "Minestrone", "pasta, tomato, beans, carrot, celery",
                "1. Finely chop the carrots and celery and sauté them in a large pot until they soften.\n\n" +
                        "2. Pour in chopped tomatoes and broth, bringing the soup to a lively simmer.\n\n" +
                        "3. Add the rinsed beans and let the soup cook for 15 minutes to develop flavor.\n\n" +
                        "4. Stir in the dry pasta and cook for another 10 minutes until the pasta is tender.\n\n" +
                        "5. Serve piping hot in deep bowls.");

        insertRecipe(db, "Chicken Tortilla Soup", "chicken, tomato, black beans, corn, tortilla",
                "1. In a large pot, bring chopped tomatoes, broth, black beans, and corn to a simmer.\n\n" +
                        "2. Add cooked, shredded chicken to the pot and heat through for 10 minutes.\n\n" +
                        "3. While the soup simmers, cut tortillas into thin strips and fry or bake them until crispy.\n\n" +
                        "4. Ladle the hot, robust soup into bowls.\n\n" +
                        "5. Top with a handful of crispy tortilla strips just before serving so they don't get soggy.");

        insertRecipe(db, "Broccoli Cheddar Soup", "broccoli, cheese, milk, butter, flour",
                "1. Chop the broccoli into small florets and boil them in a small amount of water until very tender.\n\n" +
                        "2. In a separate large pot, melt the butter, whisk in the flour to make a roux, and cook for 1 minute.\n\n" +
                        "3. Gradually whisk in the milk until a smooth, thick white sauce forms.\n\n" +
                        "4. Remove from heat and stir in the grated cheddar cheese until melted.\n\n" +
                        "5. Fold the cooked broccoli into the cheese sauce, season, and serve hot.");

        insertRecipe(db, "French Onion Soup", "onion, beef broth, bread, cheese, butter",
                "1. Slice the onions thinly and cook them very slowly in butter over low heat for 40 mins until deeply caramelized.\n\n" +
                        "2. Pour the beef broth over the dark, sweet onions and simmer for 20 minutes.\n\n" +
                        "3. Ladle the hot soup into oven-safe bowls.\n\n" +
                        "4. Float a thick slice of toasted bread on top of each bowl and cover entirely with grated cheese.\n\n" +
                        "5. Broil in the oven for 3-5 minutes until the cheese is a bubbling, melted crust.");

        insertRecipe(db, "Butternut Squash Soup", "butternut squash, onion, broth, cream",
                "1. Peel and cube the squash, tossing it in oil and roasting at 200°C (400°F) until very soft and slightly caramelized.\n\n" +
                        "2. Sauté chopped onions in a large pot until translucent.\n\n" +
                        "3. Add the roasted squash and the broth to the pot and bring to a brief simmer.\n\n" +
                        "4. Use an immersion blender to purée the mixture until incredibly smooth and velvety.\n\n" +
                        "5. Stir in a splash of heavy cream, season with salt, and serve warm.");

        insertRecipe(db, "Corn Chowder", "corn, potato, bacon, onion, cream",
                "1. Fry chopped bacon in a large pot until crispy, remove the bacon, and leave the fat in the pot.\n\n" +
                        "2. Sauté diced onions in the bacon fat until soft.\n\n" +
                        "3. Add diced potatoes, corn kernels, and broth, and simmer until the potatoes are tender (about 15 mins).\n\n" +
                        "4. Stir in a splash of cream and gently mash some of the potatoes against the pot to thicken the soup.\n\n" +
                        "5. Stir the crispy bacon back in and serve hot.");

        insertRecipe(db, "Split Pea Soup", "split peas, ham, carrot, onion, broth",
                "1. Sauté diced carrots and onions in a large soup pot until soft.\n\n" +
                        "2. Rinse the split peas and add them to the pot along with the diced ham and broth.\n\n" +
                        "3. Bring the mixture to a boil, then reduce the heat to a low simmer.\n\n" +
                        "4. Cover and cook slowly for 1.5 to 2 hours, stirring occasionally until the peas break down entirely into a thick purée.\n\n" +
                        "5. Serve hot with crusty bread.");

        insertRecipe(db, "Mushroom Soup", "mushroom, onion, garlic, broth, cream",
                "1. Slice the mushrooms thickly and sauté them with onions in butter until deeply browned and their liquid has evaporated.\n\n" +
                        "2. Add minced garlic and cook for 1 more minute.\n\n" +
                        "3. Pour in the broth and let the soup simmer gently for 20 minutes to develop a deep, earthy flavor.\n\n" +
                        "4. Carefully blend the soup until smooth, or leave it chunky if you prefer.\n\n" +
                        "5. Stir in heavy cream, heat gently without boiling, and serve.");

        insertRecipe(db, "Beef and Barley Soup", "beef, barley, carrot, celery, broth",
                "1. Cut the beef into small cubes and brown them aggressively in a hot soup pot, then set aside.\n\n" +
                        "2. In the same pot, sauté diced carrots and celery until they begin to soften.\n\n" +
                        "3. Return the beef to the pot, add the uncooked barley, and pour in the broth.\n\n" +
                        "4. Bring to a boil, then lower the heat, cover, and simmer for 45-60 minutes until the beef is tender and the barley is plump.\n\n" +
                        "5. Season heavily with black pepper and serve hearty and hot.");

        insertRecipe(db, "Pumpkin Soup", "pumpkin, onion, garlic, coconut milk",
                "1. Peel, seed, and cube the pumpkin, then roast it in the oven until extremely soft and lightly browned.\n\n" +
                        "2. In a large pot, sauté onions and garlic until translucent.\n\n" +
                        "3. Add the roasted pumpkin chunks into the pot along with the coconut milk.\n\n" +
                        "4. Use a blender or immersion blender to purée the mixture into a thick, smooth, vibrant orange soup.\n\n" +
                        "5. Heat gently until warmed through, season with salt and pepper, and serve.");

        // Desserts & Snacks
        insertRecipe(db, "Chocolate Chip Cookies", "flour, butter, sugar, chocolate, egg",
                "1. Preheat oven to 180°C (350°F). In a large bowl, vigorously cream the softened butter and sugar together until pale and fluffy.\n\n" +
                        "2. Beat in the egg until fully incorporated.\n\n" +
                        "3. Gently stir in the flour until a dough forms, being careful not to overmix.\n\n" +
                        "4. Fold in a generous amount of chocolate chips or chunks.\n\n" +
                        "5. Drop spoonfuls of dough onto a baking sheet and bake for 10-12 minutes until the edges are golden but the centers remain soft.");

        insertRecipe(db, "Brownies", "flour, butter, sugar, cocoa, egg",
                "1. Preheat oven to 175°C (350°F) and line a square baking pan with parchment paper.\n\n" +
                        "2. Melt the butter in a microwave or saucepan and whisk in the sugar and cocoa powder while it is hot.\n\n" +
                        "3. Allow to cool slightly, then beat in the egg vigorously to create a glossy batter.\n\n" +
                        "4. Fold the flour into the wet mixture gently until just combined.\n\n" +
                        "5. Pour into the prepared pan and bake for 20-25 minutes until a toothpick inserted in the center comes out with moist crumbs.");

        insertRecipe(db, "Apple Pie", "pie crust, apple, sugar, cinnamon, butter",
                "1. Peel, core, and slice the apples thinly. Toss them in a large bowl with the sugar and cinnamon.\n\n" +
                        "2. Unroll one pie crust and press it into the bottom of a 9-inch pie dish.\n\n" +
                        "3. Pour the spiced apples into the crust, piling them slightly higher in the center, and dot with small pieces of butter.\n\n" +
                        "4. Cover with the second pie crust, crimp the edges tightly to seal, and cut small slits in the top to vent steam.\n\n" +
                        "5. Bake at 190°C (375°F) for 45-50 minutes until the crust is golden and the filling is bubbling.");

        insertRecipe(db, "Cheesecake", "cream cheese, sugar, egg, graham crackers, butter",
                "1. Crush the graham crackers into fine crumbs, mix with melted butter, and press firmly into the bottom of a springform pan to make the crust.\n\n" +
                        "2. In a large bowl, beat the softened cream cheese and sugar together until completely smooth and free of lumps.\n\n" +
                        "3. Mix in the eggs one at a time on low speed, just until blended.\n\n" +
                        "4. Pour the batter gently over the crust.\n\n" +
                        "5. Bake at 160°C (325°F) for 45-50 minutes until the edges are set but the center still slightly jiggles, then chill overnight.");

        insertRecipe(db, "Hummus", "chickpeas, tahini, lemon, garlic, olive oil",
                "1. Drain the chickpeas, reserving a small amount of the liquid from the can.\n\n" +
                        "2. Place the chickpeas, tahini paste, a clove of garlic, and fresh lemon juice into a food processor.\n\n" +
                        "3. Blend continuously for 2-3 minutes, streaming in a tablespoon of olive oil.\n\n" +
                        "4. If the hummus is too thick, add a spoonful of the reserved chickpea liquid and blend again until incredibly smooth and airy.\n\n" +
                        "5. Serve in a shallow bowl, making a swirl on top to hold an extra drizzle of olive oil.");

        insertRecipe(db, "Guacamole", "avocado, onion, tomato, lime, cilantro",
                "1. Slice the avocados in half, remove the pit, and scoop the green flesh into a large bowl.\n\n" +
                        "2. Mash the avocado with a fork, leaving it slightly chunky for texture.\n\n" +
                        "3. Finely dice the onion and tomato, and roughly chop the fresh cilantro.\n\n" +
                        "4. Fold the vegetables and cilantro gently into the mashed avocado.\n\n" +
                        "5. Squeeze the juice of half a lime over the mixture, season with salt, stir gently, and serve immediately with chips.");

        insertRecipe(db, "Salsa", "tomato, onion, jalapeno, lime, cilantro",
                "1. Wash the vegetables. Core the tomatoes and remove the seeds from the jalapeno (unless you want high heat).\n\n" +
                        "2. Finely and uniformly dice the tomatoes, onion, and jalapeno into very small pieces.\n\n" +
                        "3. Place the diced vegetables into a bowl and add a handful of chopped cilantro.\n\n" +
                        "4. Squeeze fresh lime juice over the top and season with salt.\n\n" +
                        "5. Stir well and let sit for at least 15 minutes at room temperature so the juices release and flavors combine.");

        insertRecipe(db, "Bruschetta", "bread, tomato, basil, garlic, olive oil",
                "1. Slice a baguette or crusty loaf on a diagonal and toast the slices under a broiler until crispy and golden.\n\n" +
                        "2. Immediately rub the hot, toasted bread lightly with a raw, halved clove of garlic.\n\n" +
                        "3. In a bowl, toss diced fresh tomatoes with chopped basil, a pinch of salt, and a drizzle of olive oil.\n\n" +
                        "4. Spoon the tomato mixture generously over the toasted bread.\n\n" +
                        "5. Serve immediately before the bread becomes soggy.");

        insertRecipe(db, "Trail Mix", "almonds, walnuts, raisins, chocolate, pumpkin seeds",
                "1. Measure out equal portions of almonds, walnuts, and pumpkin seeds for a crunchy base.\n\n" +
                        "2. Place the nuts and seeds into a large mixing bowl.\n\n" +
                        "3. Add a generous handful of sweet raisins for chewiness.\n\n" +
                        "4. Wait until the nuts are completely cool (if you toasted them), then add the chocolate pieces so they don't melt.\n\n" +
                        "5. Toss everything together evenly and store in an airtight container for snacking.");

        insertRecipe(db, "Garlic Knots", "pizza dough, garlic, butter, parmesan",
                "1. Preheat oven to 200°C (400°F). Roll out the pizza dough on a floured surface and cut it into short strips.\n\n" +
                        "2. Tie each strip of dough gently into a simple knot and place them on a baking sheet.\n\n" +
                        "3. Bake for 10-12 minutes until the knots puff up and turn golden brown.\n\n" +
                        "4. While they bake, melt butter in a saucepan and stir in minced garlic and grated parmesan.\n\n" +
                        "5. As soon as the knots come out of the oven, brush or toss them heavily with the garlic butter and serve hot.");
    }
}

