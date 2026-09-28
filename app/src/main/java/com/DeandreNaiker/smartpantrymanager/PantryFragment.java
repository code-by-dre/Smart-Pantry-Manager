package com.DeandreNaiker.smartpantrymanager;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class PantryFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private PantryAdapter pantryAdapter;
    private Cursor cursor;
    private Spinner spinnerFilterCategory;
    private String currentFilter = "All Categories";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        databaseHelper = new DatabaseHelper(requireContext());

        spinnerFilterCategory = view.findViewById(R.id.spinnerFilterCategory);
        recyclerView = view.findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup Filter Spinner
        String[] filterCategories = {"All Categories", "Produce", "Dairy", "Spices", "Meat", "Grains", "Other"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterCategories);
        spinnerFilterCategory.setAdapter(filterAdapter);

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filterCategories[position];
                refreshCursor();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cursor = databaseHelper.getAllIngredients();
        pantryAdapter = new PantryAdapter(requireContext(), cursor);

        // Handle Clicks
        pantryAdapter.setOnItemClickListener((id, name, quantity, unit, category) -> showEditIngredientDialog(id, name, quantity, unit, category));
        recyclerView.setAdapter(pantryAdapter);

        // Swipe-to-delete with UNDO Snackbar
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                cursor.moveToPosition(position);

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_QTY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_UNIT));

                String tempCategory = "Other 🥫";
                try {
                    tempCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_CATEGORY));
                } catch (Exception e) {}

                // Make the category final for the lambda expression
                final String category = tempCategory;

                // Delete the item
                databaseHelper.deleteIngredient(id);
                refreshCursor();

                // Show Snackbar with Undo functionality
                Snackbar.make(view, name + " removed from pantry", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            databaseHelper.insertOrUpdateIngredient(name, qty, unit, category);
                            refreshCursor();
                        })
                        .setActionTextColor(Color.parseColor("#FFCA28")) // Yellow Action Text
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                ColorDrawable background = new ColorDrawable(Color.parseColor("#EF5350")); // Red
                Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);

                if (dX > 0) {
                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                } else if (dX < 0) {
                    background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);

                if (dX != 0 && icon != null) {
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    if (dX > 0) {
                        icon.setBounds(itemView.getLeft() + iconMargin, iconTop, itemView.getLeft() + iconMargin + icon.getIntrinsicWidth(), iconBottom);
                    } else {
                        icon.setBounds(itemView.getRight() - iconMargin - icon.getIntrinsicWidth(), iconTop, itemView.getRight() - iconMargin, iconBottom);
                    }
                    icon.draw(c);
                }
            }
        }).attachToRecyclerView(recyclerView);

        FloatingActionButton fab = view.findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> showAddIngredientDialog());

        return view;
    }

    private void refreshCursor() {
        cursor = databaseHelper.getIngredientsByCategory(currentFilter);
        pantryAdapter.swapCursor(cursor);
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Ingredient");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etQuantity = dialogView.findViewById(R.id.etDialogQuantity);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        btnMinus.setOnClickListener(v -> {
            String current = etQuantity.getText().toString().trim();
            double val = current.isEmpty() ? 0 : Double.parseDouble(current);
            if (val > 0) etQuantity.setText(String.valueOf(val - 1));
        });

        btnPlus.setOnClickListener(v -> {
            String current = etQuantity.getText().toString().trim();
            double val = current.isEmpty() ? 0 : Double.parseDouble(current);
            etQuantity.setText(String.valueOf(val + 1));
        });

        String[] units = {"pcs", "g", "kg", "ml", "litres", "tbsp", "tsp"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(unitAdapter);

        String[] categories = {"Produce", "Dairy", "Spices", "Meat", "Grains", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();
            if (name.isEmpty() || qtyStr.isEmpty()) return;

            databaseHelper.insertOrUpdateIngredient(name, Double.parseDouble(qtyStr), spinnerUnit.getSelectedItem().toString(), spinnerCategory.getSelectedItem().toString());
            refreshCursor();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4CAF50"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#F44336"));
    }

    private void showEditIngredientDialog(int id, String currentName, double currentQty, String currentUnit, String currentCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Ingredient");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etQuantity = dialogView.findViewById(R.id.etDialogQuantity);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        etName.setText(currentName);
        etQuantity.setText(String.valueOf(currentQty));

        btnMinus.setOnClickListener(v -> {
            String current = etQuantity.getText().toString().trim();
            double val = current.isEmpty() ? 0 : Double.parseDouble(current);
            if (val > 0) etQuantity.setText(String.valueOf(val - 1));
        });

        btnPlus.setOnClickListener(v -> {
            String current = etQuantity.getText().toString().trim();
            double val = current.isEmpty() ? 0 : Double.parseDouble(current);
            etQuantity.setText(String.valueOf(val + 1));
        });

        String[] units = {"pcs", "g", "kg", "ml", "litres", "tbsp", "tsp"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(unitAdapter);
        for (int i = 0; i < units.length; i++) if (units[i].equalsIgnoreCase(currentUnit)) spinnerUnit.setSelection(i);

        String[] categories = {"Produce", "Dairy", "Spices", "Meat", "Grains", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);
        for (int i = 0; i < categories.length; i++) if (categories[i].equalsIgnoreCase(currentCategory)) spinnerCategory.setSelection(i);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();
            if (name.isEmpty() || qtyStr.isEmpty()) return;

            databaseHelper.updateIngredient(id, name, Double.parseDouble(qtyStr), spinnerUnit.getSelectedItem().toString());
            refreshCursor();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4CAF50"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#F44336"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cursor != null && !cursor.isClosed()) cursor.close();
    }
}