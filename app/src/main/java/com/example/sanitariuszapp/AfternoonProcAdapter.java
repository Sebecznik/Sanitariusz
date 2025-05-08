package com.example.sanitariuszapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AfternoonProcAdapter extends ArrayAdapter<AfternoonProc> {

    // ***
    // Database helper for managing afternoon procedure records
    // ***
    private PatientDatabaseHelper dbHelper;

    // ***
    // Constructor: initialize adapter with context and data list
    // ***
    public AfternoonProcAdapter(Context context, List<AfternoonProc> patients) {
        super(context, 0, patients);
        dbHelper = new PatientDatabaseHelper(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ***
        // Inflate list item view if not recycled
        // ***
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_afternoon_patient, parent, false);
        }

        // ***
        // Retrieve current data object
        // ***
        AfternoonProc afternoonProc = getItem(position);

        // ***
        // Bind UI components for room, name, procedures, and note
        // ***
        TextView txtRoom = convertView.findViewById(R.id.textViewRoom);
        TextView txtName = convertView.findViewById(R.id.textViewName);
        TextView txtProcedures = convertView.findViewById(R.id.textViewProcedure);
        TextView txtNote = convertView.findViewById(R.id.textViewNote);
        View foregroundLayout = convertView.findViewById(R.id.foreground_layout);
        if (foregroundLayout == null) {
            foregroundLayout = convertView;
        }

        // ***
        // Populate UI with data
        // ***
        if (afternoonProc != null) {
            txtRoom.setText(afternoonProc.getRoom());
            txtName.setText(afternoonProc.getName());
            txtProcedures.setText(afternoonProc.getProcedures());
            txtNote.setText(afternoonProc.getNote());
        }

        // ***
        // Handle item click: show action dialog for status change or deletion
        // ***
        View finalForegroundLayout = foregroundLayout;
        convertView.setOnClickListener(v -> {
            String[] options = {"In Progress", "Finish", "Delete"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Select Action")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // In Progress
                                finalForegroundLayout.setBackgroundColor(
                                        getContext().getResources().getColor(R.color.PrimaryChosen));
                                break;
                            case 1: // Finish
                                finalForegroundLayout.setBackgroundColor(
                                        getContext().getResources().getColor(R.color.gray));
                                break;
                            case 2: // Delete
                                // Remove record from database and adapter
                                if (afternoonProc != null) {
                                    dbHelper.deleteAfternoonProcedure(
                                            afternoonProc.getRoom(), afternoonProc.getName());
                                    remove(afternoonProc);
                                    notifyDataSetChanged();
                                    Toast.makeText(getContext(),
                                            "Procedure removed", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    })
                    .show();
        });

        return convertView;
    }
}