package com.example.sanitariuszapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sanitariuszapp.activities.Afternoon;

import java.util.Calendar;

public class AddToAfternoonActivity extends AppCompatActivity {

    // ***
    // User input fields for patient details and procedure
    // ***
    private EditText etFirstName, etLastName, etRoomNumber, etNote, etProcedure;

    // ***
    // Action buttons for saving and time selection
    // ***
    private Button btnSavePatient, btnSelectHour;

    // ***
    // Database helper for storing afternoon procedure entries
    // ***
    private PatientDatabaseHelper dbHelper;

    // ***
    // Holds selected procedure time as a string
    // ***
    public String timeStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_afternoon);

        // ***
        // Enable full-screen immersive mode
        // ***
        hideNavigationBar();

        // ***
        // Setup toolbar with back navigation
        // ***
        Toolbar toolbar = findViewById(R.id.toolbarAddPatient);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add to Afternoon");
        }

        // ***
        // Bind UI components for name, room, procedure, and notes
        // ***
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etProcedure = findViewById(R.id.etProcedure);
        etNote = findViewById(R.id.etNote);
        btnSavePatient = findViewById(R.id.btnSavePatient);
        btnSelectHour = findViewById(R.id.btnSetHour);

        // ***
        // Initialize database helper instance
        // ***
        dbHelper = new PatientDatabaseHelper(this);

        // ***
        // Launch TimePickerDialog to set procedure hour
        // ***
        btnSelectHour.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(AddToAfternoonActivity.this, (view, hourOfDay, minute1) -> {
                timeStr = String.format("%02d:%02d", hourOfDay, minute1);
            }, hour, minute, true).show();
        });

        // ***
        // Handle save action: validate inputs, insert data, and navigate back
        // ***
        btnSavePatient.setOnClickListener(v -> {
            if (timeStr.isEmpty()) {
                timeStr = "00:00";
            }

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String roomNumber = etRoomNumber.getText().toString().trim();
            String procedure = etProcedure.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            String finalName = firstName + " " + lastName;
            String finalProcedure = procedure + "\n" + timeStr;

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                    || TextUtils.isEmpty(roomNumber) || TextUtils.isEmpty(procedure)) {
                Toast.makeText(AddToAfternoonActivity.this,
                        "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.insertAfternoonData(roomNumber, finalName, finalProcedure, note);

            // ***
            // Finish current and return to Afternoon activity with animation
            // ***
            finish();
            startActivity(new Intent(this, Afternoon.class));
            overridePendingTransition(R.anim.activity_slide_left1, R.anim.activity_slide_left2);
        });
    }

    // ***
    // Hide system UI for immersive full-screen experience
    // ***
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    // ***
    // Handle toolbar back button press
    // ***
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}