package com.example.letter.Activities;

import android.os.Bundle;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                bottomView();
            }
        }).start();

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
}