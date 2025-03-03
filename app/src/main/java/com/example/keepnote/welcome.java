package com.example.keepnote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class welcome extends AppCompatActivity {

    Button loginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Lottie Animation
        LottieAnimationView animationView = findViewById(R.id.animationView);
        animationView.setAnimation("Animation - 1739716063212.json");
        animationView.setMinFrame(60);
        animationView.setMaxFrame(90);
        animationView.setSpeed(0.3f);
        animationView.playAnimation();

        // Find buttons by ID
        loginButton = findViewById(R.id.welcomelogin);
        signUpButton = findViewById(R.id.welcomeSignup);

        // Set onClickListener for Login Button
        loginButton.setOnClickListener(v -> {
            Intent loginIntent = new Intent(welcome.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // Set onClickListener for Sign Up Button
        signUpButton.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(welcome.this, SignupActivity.class);
            startActivity(signUpIntent);
        });
    }
}
