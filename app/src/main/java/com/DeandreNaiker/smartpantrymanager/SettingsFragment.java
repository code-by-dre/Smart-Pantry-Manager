package com.DeandreNaiker.smartpantrymanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

        Button btnClearPantry = view.findViewById(R.id.btnClearPantry);
        btnClearPantry.setOnClickListener(v -> showClearConfirmationDialog());

        return view;
    }

    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Pantry Items?")
                .setMessage("Are you sure you want to delete all ingredients from your pantry? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    databaseHelper.clearAllIngredients();
                    Toast.makeText(requireContext(), "Pantry cleared successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}