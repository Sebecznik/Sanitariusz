package com.example.sanitariuszapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sanitariuszapp.activities.Afternoon;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddAfternoonDialog extends BottomSheetDialogFragment {

    // ***
    // Input fields and action buttons in bottom sheet
    // ***
    private EditText etFirstName, etLastName, etRoomNumber, etProcedure, etNote;
    private Button btnSavePatient, btnSelectHour;

    // ***
    // Database helper for inserting afternoon data
    // ***
    private PatientDatabaseHelper dbHelper;

    // ***
    // Selected time string for procedure
    // ***
    private String timeStr = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ***
        // Inflate layout for adding afternoon procedure
        // ***
        View view = inflater.inflate(R.layout.dialog_add_afternoon, container, false);

        // ***
        // Bind input fields and buttons
        // ***
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etRoomNumber = view.findViewById(R.id.etRoomNumber);
        etProcedure = view.findViewById(R.id.etProcedure);
        etNote = view.findViewById(R.id.etNote);
        btnSavePatient = view.findViewById(R.id.btnSavePatient);
        btnSelectHour = view.findViewById(R.id.btnSetHour);

        // ***
        // Initialize database helper
        // ***
        dbHelper = new PatientDatabaseHelper(requireContext());

        // ***
        // Show time picker dialog to select procedure hour
        // ***
        btnSelectHour.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(), (TimePicker tp, int hourOfDay, int minute1) -> {
                timeStr = String.format("%02d:%02d", hourOfDay, minute1);
            }, hour, minute, true).show();
        });

        // ***
        // Validate inputs, insert data, and return to Afternoon screen
        // ***
        btnSavePatient.setOnClickListener(v -> {
            // Default time if not selected
            if (timeStr.isEmpty()) timeStr = "00:00";

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String roomNumber = etRoomNumber.getText().toString().trim();
            String procedure = etProcedure.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            // Combine names and procedure with time
            String finalName = firstName + " " + lastName;
            String finalProcedure = procedure + "\n" + timeStr;

            // Check required fields
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(roomNumber) || TextUtils.isEmpty(procedure)) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert into database
            dbHelper.insertAfternoonData(roomNumber, finalName, finalProcedure, note);
            Toast.makeText(getContext(), "Procedure added", Toast.LENGTH_SHORT).show();
            dismiss();

            // Navigate back to Afternoon activity
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), Afternoon.class);
                startActivity(intent);
            }
        });

        return view;
    }
}