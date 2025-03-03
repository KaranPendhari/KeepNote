package com.example.keepnote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch themeSwitch;
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

        // Set the switch based on current theme preference
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_THEME, false);
        themeSwitch.setChecked(isDarkMode);

        // Set theme when switch changes
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the selected theme in SharedPreferences
            sharedPreferences.edit().putBoolean(KEY_THEME, isChecked).apply();

            // Apply the theme change
            if (isChecked) {
                setTheme(android.R.style.Theme_Black);
            } else {
                setTheme(android.R.style.Theme_Light);
            }
            recreate(); // Recreate activity to apply the theme change
        });
    }
}
