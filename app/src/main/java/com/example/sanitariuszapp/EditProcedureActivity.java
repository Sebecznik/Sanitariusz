package com.example.sanitariuszapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

/***
 Activity for editing or deleting a patient's procedure,
 including time selection and validation.
 ***/
public class EditProcedureActivity extends AppCompatActivity {

    /*** UI elements ***/
    private EditText etProcedureText;
    private Button btnPickTime;
    private Button btnDeleteProcedure;
    private Button btnSaveProcedure;
    private TextView tvTimeDisplay;

    /*** Data and helpers ***/
    private int patientId;
    private int procedureIndex;
    private PatientDatabaseHelper dbHelper;
    private Procedure procedure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_procedure);

        /*** Maintain immersive full-screen mode ***/
        hideNavigationBar();

        /*** Configure toolbar ***/
        Toolbar toolbar = findViewById(R.id.toolbarEditProcedure);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Procedure");
        }

        /*** Initialize UI references ***/
        etProcedureText = findViewById(R.id.etProcedureText);
        btnPickTime = findViewById(R.id.btnPickTime);
        tvTimeDisplay = findViewById(R.id.tvTimeDisplay);
        btnDeleteProcedure = findViewById(R.id.btnDeleteProcedure);
        btnSaveProcedure = findViewById(R.id.btnSaveProcedure);

        /*** Initialize data and validate Intent extras ***/
        dbHelper = new PatientDatabaseHelper(this);
        patientId = getIntent().getIntExtra("patient_id", -1);
        procedureIndex = getIntent().getIntExtra("procedure_index", -1);
        if (patientId == -1 || procedureIndex == -1) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*** Load procedure from database ***/
        Patient patient = dbHelper.getPatientById(patientId);
        if (patient == null || procedureIndex >= patient.getProcedures().size()) {
            Toast.makeText(this, "Procedure not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        procedure = patient.getProcedures().get(procedureIndex);

        /*** Populate UI with existing data ***/
        etProcedureText.setText(procedure.getText());
        tvTimeDisplay.setText(
                procedure.getTime().isEmpty() ? "No time set" : procedure.getTime()
        );

        /*** Time picker setup ***/
        btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(
                    EditProcedureActivity.this,
                    (view, hourOfDay, minute1) -> tvTimeDisplay.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true
            ).show();
        });

        /*** Delete procedure action ***/
        btnDeleteProcedure.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("delete", true);
            resultIntent.putExtra("procedure_index", procedureIndex);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        /*** Save changes action ***/
        btnSaveProcedure.setOnClickListener(v -> {
            String newText = etProcedureText.getText().toString().trim();
            String newTime = tvTimeDisplay.getText().toString();
            if (newTime.equals("No time set")) newTime = "";
            if (newText.isEmpty()) {
                etProcedureText.setError("Procedure text cannot be empty");
                return;
            }
            procedure.setText(newText);
            procedure.setTime(newTime);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("delete", false);
            resultIntent.putExtra("procedure_index", procedureIndex);
            resultIntent.putExtra("new_text", newText);
            resultIntent.putExtra("new_time", newTime);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /*** Handle toolbar back navigation ***/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*** Maintain immersive mode on focus change ***/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideNavigationBar();
    }

    /*** Hide system UI for immersive experience ***/
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }
}