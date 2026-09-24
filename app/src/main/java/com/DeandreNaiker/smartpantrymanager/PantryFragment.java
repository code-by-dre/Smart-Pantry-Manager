package com.DeandreNaiker.smartpantrymanager;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PantryFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private PantryAdapter pantryAdapter;
    private Cursor cursor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        databaseHelper = new DatabaseHelper(requireContext());
        cursor = databaseHelper.getAllIngredients();

        recyclerView = view.findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        pantryAdapter = new PantryAdapter(requireContext(), cursor);
        recyclerView.setAdapter(pantryAdapter);

        // Attach Swipe-to-Delete logic to the RecyclerView
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We are not moving items up/down
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 1. Find out which item was swiped
                int position = viewHolder.getAdapterPosition();
                cursor.moveToPosition(position);
                String ingredientName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_NAME));

                // 2. Delete it from the database
                databaseHelper.deleteIngredient(ingredientName);

                // 3. Refresh the screen
                cursor = databaseHelper.getAllIngredients();

                pantryAdapter.swapCursor(cursor);

                Toast.makeText(requireContext(), ingredientName + " removed", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        // Setup Floating Action Button to trigger the dialog
        FloatingActionButton fab = view.findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> showAddIngredientDialog());

        return view;
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Ingredient");

        // Inflate the custom XML layout we just created
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etQuantity = dialogView.findViewById(R.id.etDialogQuantity);
        EditText etUnit = dialogView.findViewById(R.id.etDialogUnit);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();

            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name and quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            double quantity = Double.parseDouble(qtyStr);

            // 1. Save to SQLite database (Make sure "addIngredient" matches your DatabaseHelper method!)
            databaseHelper.insertIngredient(name, quantity, unit);

            // 2. Refresh the RecyclerView immediately
            cursor = databaseHelper.getAllIngredients();
            pantryAdapter.swapCursor(cursor);

            Toast.makeText(requireContext(), "Ingredient added!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}