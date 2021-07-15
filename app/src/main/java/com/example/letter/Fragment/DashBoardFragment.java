package com.example.letter.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Activities.ChatActivity;
import com.example.letter.Adapter.UserModelAdapter;
import com.example.letter.R;
import com.example.letter.UserRoomArchitecture.UserEntity;
import com.example.letter.UserRoomArchitecture.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DashBoardFragment extends Fragment implements UserModelAdapter.UserItemClicked {


    private RecyclerView mRecyclerView;
    private View view;
    private UserModelAdapter mAdapter;
    private UserViewModel viewModel;
    public DashBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_dash_board, container, false);
        hookups();
        setRecyclerView();
        ViewModel();
        return view;
    }

    private void ViewModel() {
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        viewModel.getAllUsers().observe(getViewLifecycleOwner(), userEntities -> {
            Log.d("RoomDatabaseTesting", "ViewModel: "+userEntities);
            new Handler(Looper.getMainLooper()).post(() -> mAdapter.setData(userEntities));

        });
        viewModel.FirebaseUsers();
    }

    private void hookups() {
        mRecyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new UserModelAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClicked(UserEntity user) {
        Intent i = new Intent(getContext(), ChatActivity.class);
        i.putExtra("name", user.getName());
        i.putExtra("image", user.getImage_url());
        i.putExtra("uid", user.getUid());
        startActivity(i);
    }

    @Override
    public void onLongItemClicked(UserEntity user) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("REMOVE");
        dialog.setMessage("Are you sure you want to remove "+user.getName()+" ?");
        dialog.setPositiveButton("YES", (dialog1, which) -> {
            viewModel.delete(user);
        });
        dialog.setNegativeButton("NO", null);
        dialog.show();
    }

}