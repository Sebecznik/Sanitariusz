package com.example.sanitariuszapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sanitariuszapp.AddPatientDialog;
import com.example.sanitariuszapp.Patient;
import com.example.sanitariuszapp.PatientAdapter;
import com.example.sanitariuszapp.PatientDatabaseHelper;
import com.example.sanitariuszapp.PatientDetailsActivity;
import com.example.sanitariuszapp.Procedure;
import com.example.sanitariuszapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ***
    // UI components and database helper
    // ***
    private RecyclerView rvPatients;
    private PatientAdapter adapter;
    private List<Patient> patientList;
    private PatientDatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;

    // ***
    // Bottom navigation buttons and layout elements
    // ***
    private ImageButton btnAfternoon, btnArchive, btnToTake, btnContact, fabAddPatient, btnHome, btnClearProcedures, btnInfo;
    private ConstraintLayout topContainer, itemInfo;
    private ImageButton currentSelectedButton;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ***
        // Bring top container to front and hide navigation bar
        // ***
        topContainer = findViewById(R.id.topContainer);
        topContainer.bringToFront();
        hideNavigationBar();

        // ***
        // Initialize database helper
        // ***
        dbHelper = new PatientDatabaseHelper(this);

        // ***
        // Initialize bottom navigation buttons
        // ***
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnArchive = findViewById(R.id.btnArchive);
        btnToTake = findViewById(R.id.btnToTake);
        btnContact = findViewById(R.id.btnContact);
        btnHome = findViewById(R.id.btnHome);
        fabAddPatient = findViewById(R.id.fabAddPatient);
        btnClearProcedures = findViewById(R.id.btnClearProcedures);
        btnInfo = findViewById(R.id.btnInfo);

        // ***
        // Highlight home button by default
        // ***
        scaleSelected(btnHome);

        // ***
        // Handle bottom navigation clicks
        // ***
        btnAfternoon.setOnClickListener(v -> {
            startActivity(new Intent(this, Afternoon.class));
            overridePendingTransition(R.anim.activity_slide_left1, R.anim.activity_slide_left2);
        });
        btnArchive.setOnClickListener(v -> {
            startActivity(new Intent(this, Archive.class));
            overridePendingTransition(R.anim.activity_slide_left1, R.anim.activity_slide_left2);
        });
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0,0);
        });
        btnContact.setOnClickListener(v -> {
            startActivity(new Intent(this, Contact.class));
            overridePendingTransition(R.anim.activity_slide_right1, R.anim.activity_slide_right2);
        });
        fabAddPatient.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            AddPatientDialog bottomSheetDialog = new AddPatientDialog();
            bottomSheetDialog.show(getSupportFragmentManager(), "AddPatientBottomSheetDialog");
        });
        btnClearProcedures.setOnClickListener(v -> {
            showEndShiftDialog();
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnInfo.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            displayInfo();
        });

        // ***
        // Initialize drawer layout and set up menu button
        // ***
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        }

        // ***
        // Set up RecyclerView for patient list
        // ***
        patientList = dbHelper.getAllPatients();
        rvPatients = findViewById(R.id.rvPatients);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvPatients.setLayoutManager(layoutManager);
        rvPatients.setClipToPadding(true);
        rvPatients.setClipToOutline(false);
        adapter = new PatientAdapter(this, patientList);
        rvPatients.setAdapter(adapter);

        // ***
        // Enable swipe-to-delete functionality
        // ***
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Patient patient = patientList.get(pos);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Potwierdzenie usunięcia")
                        .setMessage("Czy na pewno chcesz usunąć pacjenta?")
                        .setPositiveButton("Tak", (dialog, which) -> {
                            dbHelper.deletePatient(patient.getId());
                            patientList.remove(pos);
                            adapter.notifyItemRemoved(pos);
                            Snackbar.make(rvPatients, "Pacjent usunięty", Snackbar.LENGTH_LONG)
                                    .setAction("Cofnij", v -> Toast.makeText(MainActivity.this, "Funkcja cofania niezaimplementowana", Toast.LENGTH_SHORT).show())
                                    .show();
                        })
                        .setNegativeButton("Nie", (dialog, which) -> adapter.notifyItemChanged(pos))
                        .setCancelable(false)
                        .show();
            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                float limit = viewHolder.itemView.getWidth() * 0.4f;
                float translationX = Math.max(dX, -limit);
                PatientAdapter.PatientViewHolder holder = (PatientAdapter.PatientViewHolder) viewHolder;
                holder.foregroundLayout.setTranslationX(translationX);
                float backgroundTranslationX = translationX / 2;
                holder.foregroundLayout.setTranslationX(backgroundTranslationX);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvPatients);
    }

    // ***
    // Highlight the selected navigation button
    // ***
    private void scaleSelected(ImageButton selected) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setScaleX(1f);
            currentSelectedButton.setScaleY(1f);
        }
        selected.setScaleX(1.2f);
        selected.setScaleY(1.2f);
        currentSelectedButton = selected;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ***
        // Refresh patient list and drawer content
        // ***
        patientList.clear();
        patientList.addAll(dbHelper.getAllPatients());
        adapter.notifyDataSetChanged();
        populateDrawerPatientCircles();
    }

    // ***
    // Generate patient initials inside drawer menu
    // ***
    private void populateDrawerPatientCircles() {
        LinearLayout llInitials = findViewById(R.id.llDrawerPatientInitials);
        if (llInitials == null) return;
        llInitials.removeAllViews();
        for (Patient p : patientList) {
            TextView circle = new TextView(this);
            circle.setText(getPatientInitials(p.getName()));
            circle.setTextSize(16);
            circle.setGravity(Gravity.CENTER);
            circle.setBackgroundResource(R.drawable.circle_background);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            circle.setLayoutParams(params);

            circle.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PatientDetailsActivity.class);
                intent.putExtra("patient_id", p.getId());
                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
            });

            llInitials.addView(circle);
        }
    }

    private String getPatientInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return fullName.substring(0, 1).toUpperCase();
        }
    }

    // ***
    // Disable default action bar menu
    // ***
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return false;
    }

    // ***
    // Collapse any expanded RecyclerView items on outside touch
    // ***
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (adapter != null && adapter.isItemExpanded() && isClickOutsideRecyclerView(rvPatients, ev)) {
            adapter.collapseExpandedItem();
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isClickOutsideRecyclerView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float x = ev.getRawX();
        float y = ev.getRawY();

        return x < location[0]
                || x > location[0] + view.getWidth()
                || y < location[1]
                || y > location[1] + view.getHeight();
    }

    // ***
    // Re-hide navigation bar on window focus regain
    // ***
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

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
    // Display patient and procedure summary in custom dialog
    // ***
    private void displayInfo() {
        int totalPatients = patientList.size();
        int totalProcedures = 0;
        int toDoProcedures = 0;
        int inProgressProcedures = 0;
        int finishedProcedures = 0;
        int onTimeProcedures = 0;

        for (Patient p : patientList) {
            if (p.getProcedures() != null) {
                totalProcedures += p.getProcedures().size();
                for (Procedure proc : p.getProcedures()) {
                    if (proc.getStatus() == Procedure.STATUS_DEFAULT) toDoProcedures++;
                    else if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) inProgressProcedures++;
                    else if (proc.getStatus() == Procedure.STATUS_FINISHED) finishedProcedures++;
                    if (proc.getTime() != null && !proc.getTime().isEmpty() && !proc.getTime().equals("00:00")) onTimeProcedures++;
                }
            }
        }

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_info, null);

        TextView totalPatientsView = view.findViewById(R.id.totalPatients);
        TextView totalProceduresView = view.findViewById(R.id.totalProcedures);
        TextView toDoProceduresView = view.findViewById(R.id.toDoProcedures);
        TextView inProgressProceduresView = view.findViewById(R.id.inProgressProcedures);
        TextView finishedProceduresView = view.findViewById(R.id.finishedProcedures);
        TextView onTimeProceduresView = view.findViewById(R.id.onTimeProcedures);

        totalPatientsView.setText("Liczba pacjentów: " + totalPatients);
        totalProceduresView.setText("ilość zabiegów: " + totalProcedures);
        toDoProceduresView.setText("pozostało: " + toDoProcedures);
        inProgressProceduresView.setText("w trakcie: " + inProgressProcedures);
        finishedProceduresView.setText("zakończone: " + finishedProcedures);
        onTimeProceduresView.setText("na godzine: " + onTimeProcedures);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        alertDialog.show();
    }

    // ***
    // Display end shift dialog and archive patients
    // ***
    private void showEndShiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_end_shift, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnConfirm.setOnClickListener(v -> {
            dbHelper.deleteOldArchive();
            List<Patient> patients = dbHelper.getAllPatients();
            for (Patient patient : patients) {
                dbHelper.archivePatient(patient);
            }
            dbHelper.deleteAllProcedures();
            recreate();
            Toast.makeText(MainActivity.this, "Zarchiwizowano!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
}
