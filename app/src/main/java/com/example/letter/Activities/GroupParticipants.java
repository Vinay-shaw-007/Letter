package com.example.letter.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Adapter.FindUserAdapter;
import com.example.letter.Adapter.FindUserModelAdapter;
import com.example.letter.AddUserRoomArchitecture.AddUserEntity;
import com.example.letter.AddUserRoomArchitecture.AddUserViewModel;
import com.example.letter.Models.User;
import com.example.letter.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class GroupParticipants extends AppCompatActivity implements FindUserModelAdapter.newUserClicked {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private FindUserModelAdapter mAdapter;
    private FloatingActionButton membersAdded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participants);
        hooks();
        setToolbar();
        setRecyclerView();
        setViewModel();
//        buttonClicks();
    }

    private void buttonClicks() {
        membersAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<AddUserEntity> selectedMembers1 = FindUserModelAdapter.getSelectedMembers();
                Object selectedMembers2 = FindUserModelAdapter.getSelectedMembers();

                Intent i = new Intent(GroupParticipants.this, NewGroup.class);

                i.putExtra("groupMembers", selectedMembers1);
//                i.putParcelableArrayListExtra("groupMembers", (ArrayList<? extends Parcelable>) selectedMembers1);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            }
        });
    }

    private void setViewModel() {
        AddUserViewModel viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        viewModel.getAllContactUser().observe(this, addUserEntities -> {
            if (addUserEntities != null) mAdapter.setNewUser(addUserEntities);
        });
    }

    private void setRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FindUserModelAdapter(this, this, true);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void hooks() {
        toolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.addParticipants);
        membersAdded = findViewById(R.id.membersAdded);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    @Override
    public void onUserClicked(AddUserEntity newUser) {

    }
}