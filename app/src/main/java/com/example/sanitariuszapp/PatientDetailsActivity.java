package com.example.sanitariuszapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.List;

/***
 * Main activity displaying patient details and managing medical procedures.
 * Provides functionality for adding, editing, starting, and completing procedures.
 */
public class PatientDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvRoom;
    private LinearLayout llProcedures;
    private EditText etNewProcedure, etNote;
    private Button btnAddProcedure, btnDone, btnAddToAfternoon;
    private PatientDatabaseHelper dbHelper;
    private Patient patient;

    /***
     * Initializes the activity, sets up UI components and loads patient data.
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        hideNavigationBar();

        Toolbar toolbar = findViewById(R.id.toolbarPatientDetails);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Patient Details");
        }

        tvName = findViewById(R.id.tvName);
        tvRoom = findViewById(R.id.tvRoom);
        llProcedures = findViewById(R.id.llProcedures);
        etNewProcedure = findViewById(R.id.etNewProcedure);
        etNote = findViewById(R.id.etNote);
        btnAddProcedure = findViewById(R.id.btnAddProcedure);
        btnDone = findViewById(R.id.btnDone);

        dbHelper = new PatientDatabaseHelper(this);

        int patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Error loading patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        patient = dbHelper.getPatientById(patientId);
        if (patient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvName.setText(patient.getName());
        tvRoom.setText("Room: " + patient.getRoomNumber());
        if (patient.getNote() != null) {
            etNote.setText(patient.getNote());
        }
        displayProcedures();

        btnAddProcedure.setOnClickListener(v -> {
            String newProcText = etNewProcedure.getText().toString().trim();
            if (!newProcText.isEmpty()) {
                List<Procedure> updatedProcedures = new ArrayList<>(patient.getProcedures());
                updatedProcedures.add(new Procedure(newProcText, "00:00"));
                int rows = dbHelper.updatePatientProcedures(patient.getId(), updatedProcedures);
                if (rows > 0) {
                    patient = dbHelper.getPatientById(patient.getId());
                    displayProcedures();
                    etNewProcedure.setText("");
                    Toast.makeText(PatientDetailsActivity.this, "Procedure added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PatientDetailsActivity.this, "Error adding procedure", Toast.LENGTH_SHORT).show();
                }
            } else {
                etNewProcedure.setError("Enter procedure");
            }
        });

        btnDone.setOnClickListener(v -> {
            patient.setNote(etNote.getText().toString().trim());
            int rows = dbHelper.updatePatientNote(patient.getId(), patient.getNote());
            finish();
        });
    }

    /***
     * Displays procedures grouped by status: IN_PROGRESS, DEFAULT/MARKED, FINISHED
     */
    private void displayProcedures() {
        llProcedures.removeAllViews();
        List<Procedure> procedures = patient.getProcedures();

        // Group procedures by status
        List<Procedure> inProgress = new ArrayList<>();
        List<Procedure> normal = new ArrayList<>();
        List<Procedure> finished = new ArrayList<>();
        for (Procedure proc : procedures) {
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) {
                inProgress.add(proc);
            } else if (proc.getStatus() == Procedure.STATUS_FINISHED) {
                finished.add(proc);
            } else {
                normal.add(proc);
            }
        }

        // Merge into single sorted list
        List<Procedure> sortedProcedures = new ArrayList<>();
        sortedProcedures.addAll(inProgress);
        sortedProcedures.addAll(normal);
        sortedProcedures.addAll(finished);

        for (Procedure proc : sortedProcedures) {
            final int originalIndex = patient.getProcedures().indexOf(proc);

            // Create special view for IN_PROGRESS procedures with timer
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) {
                long elapsed = System.currentTimeMillis() - proc.getStartTimestamp();
                long remaining = proc.getDurationMillis() - elapsed;
                Context context = PatientDetailsActivity.this;

                LinearLayout llActive = new LinearLayout(context);
                llActive.setOrientation(LinearLayout.HORIZONTAL);
                int padding = getResources().getDimensionPixelSize(R.dimen.procedure_padding);
                llActive.setPadding(padding, padding, padding, padding);
                LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                llParams.setMargins(5, 5, 5, 5);
                llActive.setLayoutParams(llParams);

                TextView tvDetails = new TextView(context);
                String timeStr = (proc.getTime() == null || proc.getTime().isEmpty()) ? "No time set" : proc.getTime();
                tvDetails.setText(proc.getText() + " (" + timeStr + ")");
                tvDetails.setTextSize(16);
                tvDetails.setGravity(Gravity.CENTER_VERTICAL);
                tvDetails.setTextColor(ContextCompat.getColor(context, R.color.Text));
                LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                );
                tvDetails.setLayoutParams(detailsParams);

                TextView tvTimer = new TextView(context);
                tvTimer.setTextSize(16);
                tvTimer.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                tvTimer.setLayoutParams(timerParams);
                tvTimer.setText(remaining >= 0 ? formatTime(remaining) : "-" + formatTime(-remaining));

                final Drawable bg = DrawableCompat.wrap(
                        ContextCompat.getDrawable(context, R.drawable.procedure_default_drawable).mutate()
                );
                int initialColor = getDesiredColor(proc.getDurationMillis(), remaining);
                DrawableCompat.setTint(bg, initialColor);
                llActive.setBackground(bg);
                final int[] previousColor = { initialColor };

                llActive.addView(tvDetails);
                llActive.addView(tvTimer);

                if (remaining > 0) {
                    new CountDownTimer(remaining, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tvTimer.setText(formatTime(millisUntilFinished));
                            int newColor = getDesiredColor(proc.getDurationMillis(), millisUntilFinished);
                            if (newColor != previousColor[0]) {
                                ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), previousColor[0], newColor);
                                colorAnim.setDuration(800);
                                colorAnim.addUpdateListener(animation -> {
                                    int animatedValue = (int) animation.getAnimatedValue();
                                    DrawableCompat.setTint(bg, animatedValue);
                                });
                                colorAnim.start();
                                previousColor[0] = newColor;
                            }
                        }
                        @Override
                        public void onFinish() {
                            tvTimer.setText("00:00");
                            ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), previousColor[0], Color.RED);
                            colorAnim.setDuration(800);
                            colorAnim.addUpdateListener(animation -> {
                                int animatedValue = (int) animation.getAnimatedValue();
                                DrawableCompat.setTint(bg, animatedValue);
                            });
                            colorAnim.start();
                            startCountUpTimer(tvTimer);
                        }
                    }.start();
                } else {
                    startCountUpTimer(tvTimer);
                }

                llActive.setOnClickListener(v -> {
                    String[] options = {"Edit", "In Progress", "Complete", "Delete"};
                    new AlertDialog.Builder(PatientDetailsActivity.this)
                            .setTitle("Select action")
                            .setItems(options, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        Intent intent = new Intent(PatientDetailsActivity.this, EditProcedureActivity.class);
                                        intent.putExtra("patient_id", patient.getId());
                                        intent.putExtra("procedure_index", originalIndex);
                                        startActivityForResult(intent, 100);
                                        break;
                                    case 1:
                                        showDurationDialog(proc, originalIndex);
                                        break;
                                    case 2:
                                        proc.setStatus(Procedure.STATUS_FINISHED);
                                        List<Procedure> updatedProcedures = new ArrayList<>(patient.getProcedures());
                                        updatedProcedures.set(originalIndex, proc);
                                        int rowsFinished = dbHelper.updatePatientProcedures(patient.getId(), updatedProcedures);
                                        if (rowsFinished > 0) {
                                            patient = dbHelper.getPatientById(patient.getId());
                                            displayProcedures();
                                            Toast.makeText(PatientDetailsActivity.this, "Procedure completed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PatientDetailsActivity.this, "Error updating procedure", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 3:
                                        List<Procedure> proceduresAfterDeletion = new ArrayList<>(patient.getProcedures());
                                        if (originalIndex >= 0 && originalIndex < proceduresAfterDeletion.size()) {
                                            proceduresAfterDeletion.remove(originalIndex);
                                            int rowsDeleted = dbHelper.updatePatientProcedures(patient.getId(), proceduresAfterDeletion);
                                            if (rowsDeleted > 0) {
                                                patient = dbHelper.getPatientById(patient.getId());
                                                displayProcedures();
                                                Toast.makeText(PatientDetailsActivity.this, "Procedure deleted", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PatientDetailsActivity.this, "Error deleting procedure", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        break;
                                }
                            })
                            .show();
                });

                llProcedures.addView(llActive);
                continue;
            }

            // Default view for other procedure statuses
            TextView tv = new TextView(this);
            tv.setText(proc.toString());
            tv.setTextSize(18);
            tv.setPadding(20, 12, 20, 12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            tv.setLayoutParams(params);

            switch (proc.getStatus()) {
                case Procedure.STATUS_MARKED:
                    tv.setBackgroundResource(R.drawable.procedure_marked_drawable);
                    break;
                case Procedure.STATUS_FINISHED:
                    tv.setBackgroundResource(R.drawable.procedure_finished_drawable);
                    break;
                default:
                    tv.setBackgroundResource(R.drawable.procedure_default_drawable);
                    break;
            }
            tv.setTextColor(ContextCompat.getColor(this, R.color.Text));

            tv.setOnClickListener(v -> {
                String[] options = {"Edit", "In Progress", "Complete", "Add to afternoon", "Delete"};
                new AlertDialog.Builder(PatientDetailsActivity.this)
                        .setTitle("Select action")
                        .setItems(options, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent intent = new Intent(PatientDetailsActivity.this, EditProcedureActivity.class);
                                    intent.putExtra("patient_id", patient.getId());
                                    intent.putExtra("procedure_index", originalIndex);
                                    startActivityForResult(intent, 100);
                                    break;
                                case 1:
                                    showDurationDialog(proc, originalIndex);
                                    break;
                                case 2:
                                    proc.setStatus(Procedure.STATUS_FINISHED);
                                    List<Procedure> updatedProcedures = new ArrayList<>(patient.getProcedures());
                                    updatedProcedures.set(originalIndex, proc);
                                    int rowsFinished = dbHelper.updatePatientProcedures(patient.getId(), updatedProcedures);
                                    if (rowsFinished > 0) {
                                        patient = dbHelper.getPatientById(patient.getId());
                                        displayProcedures();
                                        Toast.makeText(PatientDetailsActivity.this, "Procedure completed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PatientDetailsActivity.this, "Error updating procedure", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 3:
                                    AlertDialog.Builder noteDialog = new AlertDialog.Builder(PatientDetailsActivity.this);
                                    noteDialog.setTitle("Add note");

                                    final EditText input = new EditText(PatientDetailsActivity.this);
                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                    noteDialog.setView(input);

                                    noteDialog.setPositiveButton("OK", (dialogInterface, i) -> {
                                        String note = input.getText().toString();
                                        dbHelper.addToAfternoonTable(patient, proc, note);
                                        Toast.makeText(PatientDetailsActivity.this, "Procedure added to afternoon", Toast.LENGTH_SHORT).show();
                                    });

                                    noteDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

                                    noteDialog.show();
                                    break;

                                case 4:
                                    List<Procedure> proceduresAfterDeletion = new ArrayList<>(patient.getProcedures());
                                    if (originalIndex >= 0 && originalIndex < proceduresAfterDeletion.size()) {
                                        proceduresAfterDeletion.remove(originalIndex);
                                        int rowsDeleted = dbHelper.updatePatientProcedures(patient.getId(), proceduresAfterDeletion);
                                        if (rowsDeleted > 0) {
                                            patient = dbHelper.getPatientById(patient.getId());
                                            displayProcedures();
                                            Toast.makeText(PatientDetailsActivity.this, "Procedure deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PatientDetailsActivity.this, "Error deleting procedure", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                            }
                        })
                        .show();
            });

            llProcedures.addView(tv);
        }
    }

    /***
     * Starts a count-up timer to show elapsed time after procedure completion
     * @param tvTimer TextView to display the timer
     */
    private void startCountUpTimer(TextView tvTimer) {
        final long timerStart = System.currentTimeMillis();
        new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = System.currentTimeMillis() - timerStart;
                tvTimer.setText("-" + formatTime(elapsed));
            }
            @Override
            public void onFinish() { }
        }.start();
    }

    /***
     * Shows duration selection dialog for starting a procedure
     * @param proc Procedure to start
     * @param index Index of the procedure in the list
     */
    public void showDurationDialog(final Procedure proc, final int index) {
        final NumberPicker numberPicker = new NumberPicker(PatientDetailsActivity.this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(120);
        numberPicker.setValue(10);
        new AlertDialog.Builder(PatientDetailsActivity.this)
                .setTitle("Select procedure duration (minutes)")
                .setView(numberPicker)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    int minutes = numberPicker.getValue();
                    long durationMillis = minutes * 60 * 1000;
                    proc.setDurationMillis(durationMillis);
                    proc.setStartTimestamp(System.currentTimeMillis());
                    proc.setStatus(Procedure.STATUS_IN_PROGRESS);
                    List<Procedure> procedures = patient.getProcedures();
                    for (int j = 0; j < procedures.size(); j++) {
                        if (j != index && procedures.get(j).getStatus() == Procedure.STATUS_IN_PROGRESS) {
                            procedures.get(j).setStatus(Procedure.STATUS_DEFAULT);
                        }
                    }
                    updateProcedure(proc, index);
                    Toast.makeText(PatientDetailsActivity.this, "Procedure started", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /***
     * Updates procedure in database and refreshes the display
     * @param proc Updated procedure
     * @param index Index of the procedure to update
     */
    private void updateProcedure(Procedure proc, int index) {
        List<Procedure> updatedProcedures = new ArrayList<>(patient.getProcedures());
        updatedProcedures.set(index, proc);
        int rows = dbHelper.updatePatientProcedures(patient.getId(), updatedProcedures);
        if (rows > 0) {
            patient = dbHelper.getPatientById(patient.getId());
            displayProcedures();
        } else {
            Toast.makeText(PatientDetailsActivity.this, "Error updating procedure", Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * Handles results from started activities
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int index = data.getIntExtra("procedure_index", -1);
            if (index != -1) {
                List<Procedure> procedures = new ArrayList<>(patient.getProcedures());
                boolean delete = data.getBooleanExtra("delete", false);
                if (delete) {
                    procedures.remove(index);
                } else {
                    String newText = data.getStringExtra("new_text");
                    String newTime = data.getStringExtra("new_time");
                    Procedure proc = procedures.get(index);
                    proc.setText(newText);
                    proc.setTime(newTime);
                    procedures.set(index, proc);
                }
                int rows = dbHelper.updatePatientProcedures(patient.getId(), procedures);
                if (rows > 0) {
                    patient = dbHelper.getPatientById(patient.getId());
                    displayProcedures();
                    Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /***
     * Hides system navigation bar for immersive experience
     */
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    /***
     * Handles options menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***
     * Formats milliseconds into mm:ss time string
     * @param millis Time in milliseconds
     * @return Formatted time string
     */
    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60));
        return String.format("%02d:%02d", minutes, seconds);
    }

    /***
     * Determines color based on remaining time percentage
     * @param duration Total procedure duration
     * @param remaining Remaining time
     * @return Color value representing time status
     */
    private int getDesiredColor(long duration, long remaining) {
        float ratio = duration > 0 ? (float) remaining / duration : 0;
        if (ratio >= 0.67f) {
            return Color.parseColor("#00c6bc"); // Green - plenty of time
        } else if (ratio >= 0.33f) {
            return Color.parseColor("#F7D060"); // Yellow - moderate time
        } else {
            return Color.parseColor("#D1545B"); // Red - urgent
        }
    }
}