package com.example.sanitariuszapp.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sanitariuszapp.PatientDatabaseHelper;
import com.example.sanitariuszapp.Procedure;
import com.example.sanitariuszapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Archive extends AppCompatActivity {

    // ***
    // Bottom navigation and date selection buttons
    // ***
    private FloatingActionButton btnAfternoon, btnArchive, btnHome, btnToTake, btnContact, btnSelectDate, currentSelectedButton;

    // ***
    // ListView to display archived patient entries
    // ***
    private ListView listViewArchive;

    // ***
    // Database helper for retrieving archived data
    // ***
    private PatientDatabaseHelper dbHelper;

    // ***
    // DrawerLayout for side menu navigation
    // ***
    private DrawerLayout drawerLayout;

    // ***
    // Adapter and data list for ListView content
    // ***
    private ArrayAdapter<String> adapter;
    private List<String> patientList;

    // ***
    // UI elements for showing and navigating selected date
    // ***
    private TextView tvDate;
    private ImageButton selectNext, selectPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        // ***
        // Hide system navigation for full-screen experience
        // ***
        hideNavigationBar();

        // ***
        // Bind UI components
        // ***
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnHome = findViewById(R.id.btnHome);
        btnToTake = findViewById(R.id.btnToTake);
        btnContact = findViewById(R.id.btnContact);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        listViewArchive = findViewById(R.id.listViewArchive);
        tvDate = findViewById(R.id.tvDate);
        btnArchive = findViewById(R.id.btnArchive);
        selectNext = findViewById(R.id.selectNext);
        selectPrevious = findViewById(R.id.selectPrevious);

        // ***
        // Initialize database and list adapter
        // ***
        dbHelper = new PatientDatabaseHelper(this);
        patientList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientList);
        listViewArchive.setAdapter(adapter);

        // ***
        // Highlight current archive button in navigation
        // ***
        scaleSelected(btnArchive);

        // ***
        // Initialize drawer layout and menu icon
        // ***
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        }

        // ***
        // Set default date to yesterday and load archives
        // ***
        getYesterdayDate();

        // ***
        // Bottom navigation item click handlers
        // ***
        btnAfternoon.setOnClickListener(v -> {
            startActivity(new Intent(this, Afternoon.class));
            overridePendingTransition(R.anim.activity_slide_left1, R.anim.activity_slide_left2);
        });
        btnArchive.setOnClickListener(v -> {
            startActivity(new Intent(this, Archive.class));
            overridePendingTransition(0, 0);
        });
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.activity_slide_right1, R.anim.activity_slide_right2);
        });
        btnToTake.setOnClickListener(v -> {
            // TODO: implement ToTake navigation
        });
        btnContact.setOnClickListener(v -> {
            startActivity(new Intent(this, Contact.class));
            overridePendingTransition(R.anim.activity_slide_right1, R.anim.activity_slide_right2);
        });

        // ***
        // Date picker dialog for selecting archive date
        // ***
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // ***
        // Navigation between dates
        // ***
        selectNext.setOnClickListener(v -> goToNext());
        selectPrevious.setOnClickListener(v -> goToPrevious());
    }

    // ***
    // Highlight selected FloatingActionButton
    // ***
    private void scaleSelected(FloatingActionButton selected) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setScaleX(1f);
            currentSelectedButton.setScaleY(1f);
        }
        selected.setScaleX(1.2f);
        selected.setScaleY(1.2f);
        currentSelectedButton = selected;
    }

    // ***
    // Show DatePickerDialog and update list on date selection
    // ***
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            String formatted = String.format("%04d-%02d-%02d", y, m+1, d);
            tvDate.setText(formatted);
            loadArchivedPatients(formatted);
        }, year, month, day);
        datePickerDialog.show();
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    // ***
    // Load archived patients for given date and display in ListView
    // ***
    private void loadArchivedPatients(String date) {
        patientList.clear();
        Cursor cursor = dbHelper.getArchivedPatientsByDate(date);

        if (cursor != null && cursor.moveToFirst()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Procedure>>() {}.getType();

            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_NAME));
                String room = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_ROOM));
                String proceduresJson = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_PROCEDURES));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_NOTE));
                if (note.isEmpty()) note = "None";

                List<Procedure> procedureList = new ArrayList<>();
                if (proceduresJson != null && proceduresJson.trim().startsWith("[")) {
                    try {
                        procedureList = gson.fromJson(proceduresJson, listType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                StringBuilder sb = new StringBuilder();
                if (!procedureList.isEmpty()) {
                    for (Procedure p : procedureList) {
                        sb.append("- ").append(p.getText())
                                .append(" (").append(p.getTime() == null || p.getTime().isEmpty() ? "00:00" : p.getTime())
                                .append(")\n");
                    }
                } else {
                    sb.append("No procedures\n");
                }

                String display = "Room: " + room
                        + "\nName: " + name
                        + "\nProcedures:\n" + sb.toString()
                        + "Note: " + note;
                patientList.add(display);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No patients for this date", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    // ***
    // Keep immersive mode on window focus
    // ***
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideNavigationBar();
    }

    // ***
    // Hide system navigation for full-screen
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
    // Set date label to prompt user
    // ***
    public String getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        tvDate.setText("Select date");
        return "";
    }

    // ***
    // Navigate to next archive date
    // ***
    public void goToNext() {
        changeDateBy(1);
    }

    // ***
    // Navigate to previous archive date
    // ***
    public void goToPrevious() {
        changeDateBy(-1);
    }

    // ***
    // Change displayed date by offset days and reload
    // ***
    private void changeDateBy(int days) {
        String current = tvDate.getText().toString();
        if (current.equals("Select date")) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(current));
            cal.add(Calendar.DAY_OF_YEAR, days);
            String newDate = sdf.format(cal.getTime());
            tvDate.setText(newDate);
            loadArchivedPatients(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}