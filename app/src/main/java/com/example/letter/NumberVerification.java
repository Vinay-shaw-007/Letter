package com.example.letter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberVerification extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phone_number;
    private Button continue_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(NumberVerification.this,R.color.black));
        setContentView(R.layout.activity_number_verification);

        phone_number = findViewById(R.id.user_phone_number);
        continue_number = findViewById(R.id.btn_continue);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phone_number);

        continue_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phone_number.getText().toString().trim().isEmpty()){
                    Toast.makeText(NumberVerification.this, "Please Enter the Number", Toast.LENGTH_SHORT).show();
                }
                else if(!(Validate_phone(phone_number.getText().toString().trim()))) {
                    Toast.makeText(NumberVerification.this, "Enter the Valid Phone Number", Toast.LENGTH_SHORT).show();

                }else{
                    Intent i = new Intent(NumberVerification.this, OtpVerification.class);
                    i.putExtra("mobile_number", ccp.getFullNumberWithPlus().trim());
                    startActivity(i);

                }
            }
        });
    }

    private boolean Validate_phone(String number) {

        Pattern p = Pattern.compile("[6-9][0-9]{9}");
        Matcher m =p.matcher(number);
        return (m.find()&&m.group().equals(number));
    }
}