package com.example.sanitariuszapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.sanitariuszapp.AddAfternoonDialog;
import com.example.sanitariuszapp.AfternoonProc;
import com.example.sanitariuszapp.AfternoonProcAdapter;
import com.example.sanitariuszapp.Patient;
import com.example.sanitariuszapp.PatientDatabaseHelper;
import com.example.sanitariuszapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Afternoon extends AppCompatActivity {

    // ***
    // Bottom navigation and add-to-afternoon button
    // ***
    private FloatingActionButton btnAfternoon, btnArchive, btnHome, btnToTake, btnContact, btnAddToAfternoon;
    private FloatingActionButton currentSelectedButton;

    // ***
    // Drawer layout for side menu
    // ***
    private DrawerLayout drawerLayout;

    // ***
    // Database helper and ListView for afternoon procedures
    // ***
    private PatientDatabaseHelper dbHelper;
    private ListView listViewAfternoon;

    // ***
    // Data list and adapter for afternoon procedures
    // ***
    private List<AfternoonProc> afternoonProcList;
    private ArrayAdapter<AfternoonProc> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afternoon);

        // ***
        // Enable full-screen immersive mode
        // ***
        hideNavigationBar();

        // ***
        // Bind UI components
        // ***
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnArchive = findViewById(R.id.btnArchive);
        btnHome = findViewById(R.id.btnHome);
        btnToTake = findViewById(R.id.btnToTake);
        btnContact = findViewById(R.id.btnContact);
        btnAddToAfternoon = findViewById(R.id.btnAddToAfternoon);
        listViewAfternoon = findViewById(R.id.listViewAfternoon);
        drawerLayout = findViewById(R.id.drawer_layout);

        // ***
        // Highlight current navigation item
        // ***
        scaleSelected(btnAfternoon);

        // ***
        // Bottom navigation click listeners
        // ***
        btnAfternoon.setOnClickListener(v -> {
            startActivity(new Intent(this, Afternoon.class));
            overridePendingTransition(0, 0);
        });
        btnArchive.setOnClickListener(v -> {
            startActivity(new Intent(this, Archive.class));
            overridePendingTransition(R.anim.activity_slide_right1, R.anim.activity_slide_right2);
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
        // Show dialog to add a new afternoon procedure
        // ***
        btnAddToAfternoon.setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            AddAfternoonDialog dialog = new AddAfternoonDialog();
            dialog.show(getSupportFragmentManager(), "AddAfternoonBottomSheetDialog");
        });

        // ***
        // Initialize database helper
        // ***
        dbHelper = new PatientDatabaseHelper(this);

        // ***
        // Load and display afternoon procedures
        // ***
        loadAfternoonPatients();

        // ***
        // Drawer menu icon click listener
        // ***
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        }
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
    // Reload immersive mode on focus change
    // ***
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideNavigationBar();
    }

    // ***
    // Hide system UI for full-screen experience
    // ***
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    // ***
    // Query database for afternoon procedures and populate ListView
    // ***
    private void loadAfternoonPatients() {
        afternoonProcList = new ArrayList<>();

        Cursor cursor = dbHelper.getAllAfternoonProcedures();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    String room = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_ROOM));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_NAME));
                    String procedures = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_PROCEDURES));
                    String note = cursor.getString(cursor.getColumnIndexOrThrow(PatientDatabaseHelper.COLUMN_NOTE));

                    afternoonProcList.add(new AfternoonProc(room, name, procedures, note));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (!afternoonProcList.isEmpty()) {
            adapter = new AfternoonProcAdapter(this, afternoonProcList);
            listViewAfternoon.setAdapter(adapter);
        } else {
            listViewAfternoon.setAdapter(null);
        }
    }
}