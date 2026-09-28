package com.DeandreNaiker.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private DatabaseHelper databaseHelper;
    private TextView tvEmptyRecipes;
    private SearchView searchViewRecipes;

    private List<Recipe> masterRecipes = new ArrayList<>();
    private List<Recipe> matchingRecipes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        tvEmptyRecipes = view.findViewById(R.id.tvEmptyRecipes);
        searchViewRecipes = view.findViewById(R.id.searchViewRecipes);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        databaseHelper = new DatabaseHelper(requireContext());

        // 1. Fetch available ingredients from the Pantry
        List<String> pantryIngredients = databaseHelper.getPantryIngredientNames();

        // 2. Fetch all 100 recipes from SQLite
        masterRecipes = databaseHelper.getAllRecipes();

        // 3. Filter recipes based on what's in the pantry (STRICT MATCHING)
        matchingRecipes.clear();
        for (Recipe recipe : masterRecipes) {
            boolean canMake = true; // Assume we can make it until proven otherwise

            for (String requiredItem : recipe.getIngredients()) {
                // If the pantry DOES NOT contain the required ingredient, we fail the strict match
                if (!pantryIngredients.contains(requiredItem.toLowerCase().trim())) {
                    canMake = false;
                    break; // Stop checking this recipe, we are missing an ingredient
                }
            }

            // Only add to suggestions if EVERY ingredient was found
            if (canMake) {
                matchingRecipes.add(recipe);
            }
        }

        // 4. Initial Display (Pantry matches only)
        updateRecyclerView(matchingRecipes, "No recipes match your current pantry. Add more ingredients to see suggestions!");

        // 5. Search Bar Logic
        searchViewRecipes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    // Search is empty: revert to showing only pantry-matched recipes
                    updateRecyclerView(matchingRecipes, "No recipes match your current pantry. Add more ingredients to see suggestions!");
                } else {
                    // User is typing: search through ALL 100 recipes by title
                    List<Recipe> searchResults = new ArrayList<>();
                    for (Recipe recipe : masterRecipes) {
                        if (recipe.getTitle().toLowerCase().contains(newText.toLowerCase().trim())) {
                            searchResults.add(recipe);
                        }
                    }
                    updateRecyclerView(searchResults, "No recipes found for '" + newText + "'.");
                }
                return true; // Indicates we handled the search text change
            }
        });

        return view;
    }

    // Helper method to refresh the RecyclerView data and handle empty states cleanly
    private void updateRecyclerView(List<Recipe> recipesToDisplay, String emptyMessage) {
        if (recipesToDisplay.isEmpty()) {
            tvEmptyRecipes.setText(emptyMessage);
            tvEmptyRecipes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyRecipes.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recipeAdapter = new RecipeAdapter(recipesToDisplay);
            recyclerView.setAdapter(recipeAdapter);
        }
    }
}