package com.example.letter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {

    TextView resend_code;
    PinView pinView;
    Button verify;
    String Phone_number,mVerificationId;
    FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(OtpVerification.this,R.color.black));
        setContentView(R.layout.activity_otp_verification);
        pinView = findViewById(R.id.pinview);
        verify = findViewById(R.id.btn_verify);
        resend_code = findViewById(R.id.resend_code);
        Phone_number = getIntent().getStringExtra("mobile_number");
        mAuth = FirebaseAuth.getInstance();
//        FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);
//        FirebaseAuthSettings firebaseAuthSettings = mAuth.getFirebaseAuthSettings();
//        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(Phone_number, pinView.getText().toString().trim());
        generateOtp();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
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
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("TAG", "onCodeSent:" + verificationId);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            // ...
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            //Log.d("TAG", "onVerificationCompleted:" + credential);

            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            //Log.w("TAG", "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // The SMS quota for the project has been exceeded
                // ...
            }
            else
                Toast.makeText(OtpVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            // Show a message and update the UI
            // ...
        }


    };
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(OtpVerification.this , HomeScreen.class));
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w("TAG", "signInWithCredential:failure", task.getException());

                                // The verification code entered was invalid
                                Toast.makeText(OtpVerification.this, ""+task.getException(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}