package com.example.letter.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Adapter.TopStatusAdapter;
import com.example.letter.Adapter.UserAdapter;
import com.example.letter.Models.User;
import com.example.letter.Models.UserStatus;
import com.example.letter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.ArrayList;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

public class HomeActivity extends AppCompatActivity implements UserAdapter.UserItemClicked {

    private RecyclerView mRecyclerView, statusRecyclerView;
    private Adapter mAdapter, statusAdapter;
    FirebaseDatabase database;
    ArrayList<User> users;
    ArrayList<UserStatus> statuses;
    BottomNavigationView bottomNavigationView;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        navigationView.setItemIconTintList(null);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Status");
        progressDialog.setCancelable(false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        setSupportActionBar(toolbar);
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        statuses = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recycler_view);
        statusRecyclerView = findViewById(R.id.status_List);

        mRecyclerView.setHasFixedSize(true);
        statusRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(HORIZONTAL);
        statusRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new UserAdapter(this, this, users);
        statusAdapter = new TopStatusAdapter(this, statuses);

        mRecyclerView.setAdapter(mAdapter);
        statusRecyclerView.setAdapter(statusAdapter);
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if (!(user.getUid().equals(FirebaseAuth.getInstance().getUid())))
                        users.add(user);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomItemSelected(item);
                return false;
            }
        });

    }

    private void bottomItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.status:
                Intent i = new Intent();
                i.setType("*/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 50);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50){
            if (data != null && data.getData() !=null){
                progressDialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date  date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                });

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.new_group:
                Toast.makeText(this, "New Group Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:
                Toast.makeText(this, "Invite Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "Setting Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onItemClicked(User user) {
        Intent i = new Intent(this, UserChatActivity.class);
        i.putExtra("name", user.getName());
        i.putExtra("uid", user.getUid());
        startActivity(i);
    }
}