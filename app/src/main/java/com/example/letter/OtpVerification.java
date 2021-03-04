package com.example.letter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {

    private TextView resend_code;
    private PinView pinView;
    private Button verify;
    private String Phone_number,mVerificationId;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(OtpVerification.this,R.color.black));
        setContentView(R.layout.activity_otp_verification);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();
        pinView = findViewById(R.id.pinview);
        verify = findViewById(R.id.btn_verify);
        resend_code = findViewById(R.id.resend_code);
        resend_code.setVisibility(View.INVISIBLE);
        resend_code.setEnabled(false);
        Phone_number = getIntent().getStringExtra("mobile_number");
        mAuth = FirebaseAuth.getInstance();

        generateOtp();
        verify.setOnClickListener(v -> {
            if (pinView.getText().toString().isEmpty()){
                Toast.makeText(OtpVerification.this, "Enter the code", Toast.LENGTH_SHORT).show();
            }
            else if(pinView.getText().toString().length()!=6){
                Toast.makeText(OtpVerification.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
            else{
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,pinView.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }
        });
        resend_code.setOnClickListener(v -> {
            generateOtp();
        });


    }
    private void generateOtp(){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(Phone_number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            dialog.dismiss();
            mVerificationId = verificationId;
            mResendToken = token;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            resend_code.setVisibility(View.VISIBLE);
            resend_code.setEnabled(true);
        }
    };
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(OtpVerification.this , SetupProfile.class));
                            finishAffinity();

                        } else {
                                Toast.makeText(OtpVerification.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}