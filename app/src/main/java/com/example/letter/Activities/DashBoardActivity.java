package com.example.letter.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.letter.Fragment.AddUsersFragment;
import com.example.letter.Fragment.CallsFragment;
import com.example.letter.Fragment.DashBoardFragment;
import com.example.letter.Fragment.StatusFragment;
import com.example.letter.R;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class DashBoardActivity extends AppCompatActivity {

    private SmoothBottomBar smoothBottomBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        hookups();
        setToolbar();
        new Thread(this::bottomView).start();

    }

    private void bottomView() {
        loadFragment(new DashBoardFragment());
        smoothBottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {
            Fragment fragment = null;
            switch (i){
                case 0: fragment = new DashBoardFragment();
                        break;
                case 1: fragment = new StatusFragment();
                        break;
                case 2: fragment = new CallsFragment();
                        break;
                case 3: fragment = new AddUsersFragment();
                        break;
            }
            loadFragment(fragment);
            return false;
        });
    }
    private void loadFragment(Fragment fragment){
        if (fragment != null){
            runOnUiThread(() -> getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit());

        }
    }
    private void setToolbar() {
        //set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void hookups() {
        smoothBottomBar = findViewById(R.id.bottomBar);
    }

    @Override
    public void onBackPressed() {
        if (smoothBottomBar.getItemActiveIndex() == 0){
            finishAffinity();
        }else {
            loadFragment(new DashBoardFragment());
            smoothBottomBar.setItemActiveIndex(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.new_group:
                Toast.makeText(this, "New Group Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DashBoardActivity.this, NewGroup.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
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
}