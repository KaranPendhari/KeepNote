package com.example.keepnote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private Switch themeSwitch;
    private Button logoutButton;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize Switch for theme selection
        themeSwitch = findViewById(R.id.themeSwitch);
        logoutButton = findViewById(R.id.logoutButton);

        // Set the switch based on current theme preference
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_THEME, false);
        themeSwitch.setChecked(isDarkMode);

        // Apply the theme based on the stored preference
        applyTheme(isDarkMode);

        // Set theme when switch changes
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the selected theme in SharedPreferences
            sharedPreferences.edit().putBoolean(KEY_THEME, isChecked).apply();

            // Apply the theme change
            applyTheme(isChecked);
            recreate(); // Recreate activity to apply the theme change
        });

        // Set up Logout functionality with AlertDialog
        logoutButton.setOnClickListener(v -> {
            // Show confirmation dialog before logging out
            new androidx.appcompat.app.AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Log out the user if they confirm
                        FirebaseAuth.getInstance().signOut();

                        // Redirect to LoginActivity
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Close the current activity
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Close the dialog without action
                    .show();
        });
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
