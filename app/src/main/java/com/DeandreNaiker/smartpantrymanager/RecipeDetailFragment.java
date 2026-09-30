package com.DeandreNaiker.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RecipeDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvIngredients = view.findViewById(R.id.tvDetailIngredients);
        TextView tvSteps = view.findViewById(R.id.tvDetailSteps);

        if (getArguments() != null) {
            String title = getArguments().getString("recipe_title", "");
            String rawIngredients = getArguments().getString("recipe_ingredients", "");
            String rawSteps = getArguments().getString("recipe_steps", "");

            tvTitle.setText(title);

            // formatting the ingredients as a bulleted list
            String[] ingredientsArray = rawIngredients.split(",");
            StringBuilder formattedIngredients = new StringBuilder();
            for (String ingredient : ingredientsArray) {
                if (!ingredient.trim().isEmpty()) {
                    String trimmed = ingredient.trim();
                    formattedIngredients.append("• ")
                            .append(trimmed.substring(0, 1).toUpperCase())
                            .append(trimmed.substring(1)).append("\n");
                }
            }
            tvIngredients.setText(formattedIngredients.toString().trim());

            tvSteps.setText(rawSteps);
        }

        return view;
    }
}
