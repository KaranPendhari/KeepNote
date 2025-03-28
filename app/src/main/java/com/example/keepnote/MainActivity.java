package com.example.keepnote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build());

        // Initialize Drawer and Toggle Button
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton btnToggleSidebar = findViewById(R.id.btn_toggle_sidebar);

        // Toggle Sidebar
        btnToggleSidebar.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });

        // Handle Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_notes) {
                selectedFragment = new NotesFragment();
            } else if (item.getItemId() == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Sidebar Menu Clicks
        NavigationView navigationView = findViewById(R.id.sidebar);
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handling menu items with if-else instead of switch
            if (item.getItemId() == R.id.nav_logout) {
                showLogoutConfirmation();
                drawerLayout.closeDrawers(); // Close the drawer after selection
                return true;
            } else if (item.getItemId() == R.id.nav_canvas) {
                // Open Canvas Activity when "Canvas" is clicked
                Intent canvasIntent = new Intent(this, Canvas.class);
                startActivity(canvasIntent);
                drawerLayout.closeDrawers(); // Close the drawer after selection
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                // Open SettingsActivity when "Settings" is clicked
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                drawerLayout.closeDrawers(); // Close the drawer after selection
                return true;
            } else if (item.getItemId() == R.id.nav_aboutus) {
                // Open About Activity when "About" is clicked
                Intent aboutUsIntent = new Intent(this, AboutUsActivity.class);
                startActivity(aboutUsIntent);
                drawerLayout.closeDrawers(); // Close the drawer after selection
                return true;
            }
            return false;
        });

        // Default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, welcome.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
