package com.example.sanitariuszapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class AddPatientDialog extends BottomSheetDialogFragment {

    // ***
    // User input fields for patient details
    // ***
    private EditText etFirstName, etLastName, etRoomNumber;

    // ***
    // Save button for confirming and storing patient
    // ***
    private Button btnSavePatient;

    // ***
    // Database helper to insert new patient record
    // ***
    private PatientDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ***
        // Inflate layout for adding a new patient
        // ***
        View view = inflater.inflate(R.layout.dialog_add_patient, container, false);

        // ***
        // Bind input fields and action button
        // ***
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etRoomNumber = view.findViewById(R.id.etRoomNumber);
        btnSavePatient = view.findViewById(R.id.btnSavePatient);

        // ***
        // Initialize database helper instance
        // ***
        dbHelper = new PatientDatabaseHelper(requireContext());

        // ***
        // Handle save action: validate, insert, and refresh
        // ***
        btnSavePatient.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String roomNumber = etRoomNumber.getText().toString().trim();

            // Validate required fields
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(roomNumber)) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create patient object and insert into database
            Patient patient = new Patient(roomNumber, firstName + " " + lastName, new ArrayList<>(), "");
            long id = dbHelper.insertPatient(patient);

            if (id != -1) {
                // ***
                // Notify success and refresh main screen
                // ***
                Toast.makeText(getContext(), "Patient added", Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
                dismiss();
            } else {
                // ***
                // Notify failure to add patient
                // ***
                Toast.makeText(getContext(), "Error adding patient", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
