package com.example.sanitariuszapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.sanitariuszapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ToTake extends AppCompatActivity {

    // ***
    // Bottom navigation buttons
    // ***
    private FloatingActionButton btnAfternoon, btnNotes, btnHome, btnToTake, btnContact;
    private FloatingActionButton currentSelectedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totake);

        // ***
        // Enable immersive full-screen mode
        // ***
        hideNavigationBar();

        // ***
        // Bind UI components
        // ***
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnNotes = findViewById(R.id.btnNotes);
        btnHome = findViewById(R.id.btnHome);
        btnToTake = findViewById(R.id.btnToTake);
        btnContact = findViewById(R.id.btnContact);

        // ***
        // Highlight current "ToTake" navigation item
        // ***
        scaleSelected(btnToTake);

        // ***
        // Set up bottom navigation click listeners
        // ***
        btnAfternoon.setOnClickListener(v -> {
            startActivity(new Intent(this, Afternoon.class));
            overridePendingTransition(0, 0);
        });
        btnNotes.setOnClickListener(v -> {
            startActivity(new Intent(this, Archive.class));
            overridePendingTransition(0, 0);
        });
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
        });
        btnToTake.setOnClickListener(v -> {
            // Already in ToTake screen
        });
        btnContact.setOnClickListener(v -> {
            startActivity(new Intent(this, Contact.class));
            overridePendingTransition(0, 0);
        });
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
    // Reload immersive mode when window focus changes
    // ***
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideNavigationBar();
    }

    // ***
    // Hide system UI for full-screen display
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
}
