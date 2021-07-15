package com.example.letter.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_splash_screen);
        lottieAnimationView = findViewById(R.id.logo_animation);

        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getUid() != null){
               startActivity(new Intent(SplashScreen.this, DashBoardActivity.class));
            }else {
                startActivity(new Intent(SplashScreen.this, NumberVerification.class));
            }
            finish();
        }, 3*1000);




    }

}