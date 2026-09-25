package com.DeandreNaiker.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private DatabaseHelper databaseHelper;
    private TextView tvEmptyRecipes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        tvEmptyRecipes = view.findViewById(R.id.tvEmptyRecipes);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        databaseHelper = new DatabaseHelper(requireContext());

        // 1. Fetch available ingredients from SQLite
        List<String> pantryIngredients = databaseHelper.getPantryIngredientNames();

        // 2. Define Master Recipe Catalogue
        List<Recipe> masterRecipes = new ArrayList<>();
        masterRecipes.add(new Recipe("Garlic Butter Rice", Arrays.asList("Butter", "Rice", "Garlic"), "Melt butter, sauté garlic, add cooked rice and mix."));
        masterRecipes.add(new Recipe("Scrambled Eggs", Arrays.asList("Butter", "Eggs", "Milk"), "Whisk eggs with milk. Melt butter in pan and scramble eggs gently."));

        // 3. Filter recipes based on what's in the pantry
        List<Recipe> matchingRecipes = new ArrayList<>();
        for (Recipe recipe : masterRecipes) {
            boolean canMake = false;
            for (String requiredItem : recipe.getIngredients()) {
                if (pantryIngredients.contains(requiredItem.toLowerCase().trim())) {
                    canMake = true;
                    break;
                }
            }
            if (canMake) {
                matchingRecipes.add(recipe);
            }
        }

        // 4. Handle Empty State
        if (matchingRecipes.isEmpty()) {
            tvEmptyRecipes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyRecipes.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recipeAdapter = new RecipeAdapter(matchingRecipes);
            recyclerView.setAdapter(recipeAdapter);
        }

        return view;
    }
}