package com.example.sanitariuszapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.example.sanitariuszapp.ContactAdapter;
import com.example.sanitariuszapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contact extends AppCompatActivity {

    // ***
    // Navigation buttons and drawer layout
    // ***
    private FloatingActionButton btnAfternoon, btnArchive, btnHome, btnToTake, btnContact;
    private FloatingActionButton currentSelectedButton;
    private DrawerLayout drawerLayout;

    // ***
    // Expandable list data structures
    // ***
    private List<String> groupList;
    private List<String> childList;
    private Map<String, List<String>> mobileCollection;

    // ***
    // UI component: expandable list view
    // ***
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // ***
        // Prepare group titles and corresponding child data
        // ***
        createGroupList();
        createCollection();

        // ***
        // Hide system navigation for full-screen experience
        // ***
        hideNavigationBar();

        // ***
        // Initialize expandable list view and adapter
        // ***
        expandableListView = findViewById(R.id.elvMobiles);
        expandableListAdapter = new ContactAdapter(this, groupList, mobileCollection);
        expandableListView.setAdapter(expandableListAdapter);

        // ***
        // Group expand listener placeholder (for future behavior)
        // ***
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                // TODO: handle group expansion logic if needed
            }
        });

        // ***
        // Handle child (contact) clicks to start phone dialer
        // ***
        expandableListView.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            String selected = expandableListAdapter.getChild(groupPosition, childPosition).toString();
            String phoneNumber = selected.substring(selected.indexOf(": ") + 2);

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
            return true;
        });

        // ***
        // Initialize bottom navigation buttons and highlight current
        // ***
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnArchive = findViewById(R.id.btnArchive);
        btnHome = findViewById(R.id.btnHome);
        btnToTake = findViewById(R.id.btnToTake);
        btnContact = findViewById(R.id.btnContact);
        scaleSelected(btnContact);

        // ***
        // Set up bottom navigation click actions
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
            overridePendingTransition(R.anim.activity_slide_left1, R.anim.activity_slide_left2);
        });
        btnToTake.setOnClickListener(v -> {
            // TODO: navigate to "ToTake" screen when implemented
        });
        btnContact.setOnClickListener(v -> {
            // Reload current screen
            startActivity(new Intent(this, Contact.class));
            overridePendingTransition(0, 0);
        });

        // ***
        // Initialize drawer menu trigger
        // ***
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        }
    }

    // ***
    // Scale bottom nav button to indicate selection
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
    // Populate expandable list data from JSON asset
    // ***
    private void createCollection() {
        mobileCollection = new HashMap<>();
        try (InputStream is = getAssets().open("contact.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String jsonString = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(jsonString);
            for (String group : groupList) {
                JSONArray array = jsonObject.optJSONArray(group);
                childList = new ArrayList<>();
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        childList.add(array.getString(i));
                    }
                }
                mobileCollection.put(group, childList);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // ***
    // Define group titles for contacts
    // ***
    private void createGroupList() {
        groupList = new ArrayList<>();
        groupList.add("ratownicy");
        groupList.add("Oddziały Równica");
        groupList.add("Oddziały UIZ");
        groupList.add("Sanitariusze");
        groupList.add("Fizjoterapeuci WR");
        groupList.add("Inne");
    }

    // ***
    // Maintain immersive full-screen mode
    // ***
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    // ***
    // Hide system UI elements for full-screen display
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
