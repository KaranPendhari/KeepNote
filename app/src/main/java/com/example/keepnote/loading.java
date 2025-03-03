package com.example.keepnote;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Check if user is already logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is logged in, redirect to MainActivity
            new Handler().postDelayed(() -> {
                startActivity(new Intent(loading.this, MainActivity.class));
                finish();
            }, 2000);
        } else {
            // User is not logged in, show the welcome screen
            new Handler().postDelayed(() -> {
                startActivity(new Intent(loading.this, welcome.class));
                finish();
            }, 2000);
        }
    }
}
