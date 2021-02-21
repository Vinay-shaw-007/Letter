package com.example.letter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupProfile extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private CircleImageView circleImageView;
    private Uri selectedImage;
    private EditText userName;
    private Button btn_create_account;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Creating Profile...");
        dialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        circleImageView = findViewById(R.id.profile_logo);
        userName = findViewById(R.id.user_name);
        btn_create_account = findViewById(R.id.btn_user_profile);
        circleImageView.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, 16);
        });
        btn_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creating_user_profile();
            }
        });
    }

    private void creating_user_profile() {
        if (userName.getText().toString().isEmpty()){
            userName.setError("Please enter your Name");
            return;
        }
        dialog.show();
        if(selectedImage != null){
            StorageReference storageReference = storage.getReference().child("Profiles").child(auth.getUid());
            storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                String uid = auth.getUid();
                                String number = auth.getCurrentUser().getPhoneNumber();
                                String name = userName.getText().toString().trim();
                                User user = new User(uid, name, number, imageUrl);
                                database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                startActivity(new Intent(SetupProfile.this, HomeActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        });
                    }
                }
            });
        }
        else{
            String uid = auth.getUid();
            String number = auth.getCurrentUser().getPhoneNumber();
            String name = userName.getText().toString().trim();
            User user = new User(uid, name, number, "No Image Uploaded ");
            database.getReference()
                    .child("users")
                    .child(uid)
                    .setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            startActivity(new Intent(SetupProfile.this, HomeActivity.class));
                            finish();
                        }
                    });

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data !=null && data.getData()!=null){
            circleImageView.setImageURI(data.getData());
            selectedImage = data.getData();
        }
    }
}