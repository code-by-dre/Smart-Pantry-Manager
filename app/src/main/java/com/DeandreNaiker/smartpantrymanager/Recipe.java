package com.DeandreNaiker.smartpantrymanager;

import java.util.List;

public class Recipe {
    private String title;
    private List<String> ingredients;
    private String instructions;

    // "public" MUST be in front of Recipe here!
    public Recipe(String title, List<String> ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }
}