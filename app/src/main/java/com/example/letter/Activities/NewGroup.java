package com.example.letter.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Adapter.NewGroupAdapter;
import com.example.letter.Models.User;
import com.example.letter.R;

import java.util.ArrayList;
import java.util.Objects;

public class NewGroup extends AppCompatActivity {

    private EditText groupName, groupInfo;
    private TextView addMembers, participants;
    private RecyclerView mRecyclerView;
    private NewGroupAdapter mAdapter;
    private ArrayList<User> groupMembers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        hookups();
        setToolbar();
        buttonClicks();
        getIntentData();
        setDataIntoAdapter();
    }

    @SuppressLint("SetTextI18n")
    private void setDataIntoAdapter() {
        int  size = groupMembers.size();
        participants.setText("Participants ("+size+")");
        mAdapter = new NewGroupAdapter(this, groupMembers);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void getIntentData() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("groupMembers")){
            Bundle bundle = getIntent().getExtras();
            groupMembers = (ArrayList<User>)bundle.getSerializable("groupMembers");
        }

    }

    private void buttonClicks() {
        addMembers.setOnClickListener(v -> {
            Intent i = new Intent(this, FindUserActivity.class);
            i.putExtra("addMembers", true);
            if (groupMembers.size() != 0) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("groupMembers", groupMembers);
                i.putExtras(bundle);
            }
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });
    }

    private void setToolbar() {
        //set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Create Group");
    }

    private void hookups() {
        groupName = findViewById(R.id.group_name);
        groupInfo = findViewById(R.id.group_info);
        addMembers = findViewById(R.id.add_members);
        participants = findViewById(R.id.participants);
        mRecyclerView = findViewById(R.id.group_members);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupMembers = new ArrayList<>();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}