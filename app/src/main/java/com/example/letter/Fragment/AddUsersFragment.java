package com.example.letter.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Adapter.FindUserModelAdapter;
import com.example.letter.AddUserRoomArchitecture.AddUserEntity;
import com.example.letter.AddUserRoomArchitecture.AddUserViewModel;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddUsersFragment extends Fragment implements  FindUserModelAdapter.newUserClicked{

    private RecyclerView mRecyclerView;
    private FindUserModelAdapter mAdapter;
    private View view;
    private AddUserViewModel viewModel;
    public AddUsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Toast.makeText(getContext(), "fresh 1", Toast.LENGTH_SHORT).show();
        view = inflater.inflate(R.layout.fragment_add_users, container, false);
        hookups();
        setRecyclerView();
        ViewModel();
//        checkPermission();
        return view;
    }

    private void ViewModel() {
        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        viewModel.getAllContactUser().observe(getViewLifecycleOwner(), addUserEntities -> {
            Log.d("FetchContactUsers", "onChanged: "+addUserEntities);
            if (addUserEntities != null) {
                mAdapter.setNewUser(addUserEntities);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermission();
    }

    private void hookups() {
        mRecyclerView = view.findViewById(R.id.add_users);
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("Fetching Users");
//        dialog.show();
    }
    private void setRecyclerView(){
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FindUserModelAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }else {
            //fetch contact
            viewModel.fetch();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    viewModel.fetch();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    //When permission is denied
                    Toast.makeText(getContext(), "Permission Required", Toast.LENGTH_SHORT).show();
                    //Call checkPermission method
                    checkPermission();
                }
            });

    @Override
    public void onUserClicked(AddUserEntity newUser) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DashBoard")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(newUser.getUid());
        databaseReference
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    databaseReference.setValue(newUser);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}