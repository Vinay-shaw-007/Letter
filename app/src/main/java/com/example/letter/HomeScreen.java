package com.example.letter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {

    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        logout = findViewById(R.id.logout);
        FirebaseAuth.getInstance().signOut();
        logout.setOnClickListener(v -> {
            startActivity(new Intent(HomeScreen.this , NumberVerification.class));
        });

    }
}