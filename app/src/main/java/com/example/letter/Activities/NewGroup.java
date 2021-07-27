package com.example.letter.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Adapter.FindUserModelAdapter;
import com.example.letter.AddUserRoomArchitecture.AddUserEntity;
import com.example.letter.AddUserRoomArchitecture.AddUserViewModel;
import com.example.letter.Models.GroupMembers;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroup extends AppCompatActivity implements FindUserModelAdapter.newUserClicked {

    private EditText groupName, groupInfo;
    private TextView participants;
    private CircleImageView groupImage;
    private RecyclerView mRecyclerView;
    private FindUserModelAdapter mAdapter;
    private ArrayList<AddUserEntity> groupUsers;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        hookups();
        setToolbar();
        setRecyclerView();
        buttonClicks();
        setViewModel();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }else
            selectImage();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        requestPermissionLauncher.launch(intent);

    }

    private final ActivityResultLauncher<Intent> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Intent intent = result.getData();
                        imageUri = intent.getData();
                        if (imageUri == null){
                            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
                            Glide.with(getApplicationContext()).load(url).placeholder(R.drawable.avatar).into(groupImage);
                        }else {
                            Glide.with(getApplicationContext())
                                    .load(imageUri)
                                    .placeholder(R.drawable.avatar)
                                    .into(groupImage);
                        }
                        
                    }
                }
            });

    private void setViewModel() {
        AddUserViewModel viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        viewModel.getAllContactUser().observe(this, addUserEntities -> {
            if (addUserEntities != null){
                mAdapter.setNewUser(addUserEntities);
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void setRecyclerView() {

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FindUserModelAdapter(this, this, true);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void buttonClicks() {
        groupImage.setOnClickListener(v -> checkPermission());
    }

    private void setToolbar() {
        //set toolbar
        Toolbar toolbar = findViewById(R.id.newGroupToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void hookups() {
        groupName = findViewById(R.id.group_name);
        groupInfo = findViewById(R.id.group_info);
        participants = findViewById(R.id.participants);
        mRecyclerView = findViewById(R.id.group_members);
        groupImage = findViewById(R.id.groupImage);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupUsers = new ArrayList<>();
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onUserClicked(AddUserEntity groupMember) {
        if (groupUsers.contains(groupMember)){
            groupUsers.remove(groupMember);
        }else
            groupUsers.add(groupMember);
        int  size = groupUsers.size();
        participants.setText("Participants ("+size+")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_group, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
////                searchUsers(query);
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
////                if (searchView.getQuery().length() != 0) {
////                    searchUsers(newText);
////                }
////                else{
////                    searchUsers("");
////                }
//                return false;
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done){
            if (!TextUtils.isEmpty(groupName.getText())){
                sendDataToFirebase();
            }else groupName.setError("Group name required.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendDataToFirebase() {


        String imageUrl, creatorImageUrl, creatorName;
        if(imageUri == null) imageUrl = "No Image Uploaded";
        else imageUrl = imageUri.toString();
        String admin = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String groupInfo1 = groupInfo.getText().toString().trim();
        String groupName1 = groupName.getText().toString().trim();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups").push();

        if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() == null){
            creatorImageUrl = "No Image Uploaded";
        }else creatorImageUrl =  FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).isEmpty()){
            creatorName = "Creator";
        }else creatorName =  FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        GroupMembers gg = new GroupMembers();
        // group creator details....
        gg.setUid(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        gg.setAdmin(true);
        gg.setImage_url(creatorImageUrl);
        gg.setName(creatorName);
        gg.setPhoneNumber(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        reference.child("Members").child(gg.getUid()).setValue(gg);

        for (AddUserEntity entity: groupUsers){
            String name, image_url, phoneNumber, uid;
            name = entity.getName();
            image_url = entity.getImage_url();
            phoneNumber = entity.getPhoneNumber();
            uid = entity.getUid();
            // storing data in new GroupMembers class
            GroupMembers gu = new GroupMembers();
            gu.setUid(uid);
            gu.setAdmin(false);
            gu.setImage_url(image_url);
            gu.setName(name);
            gu.setPhoneNumber(phoneNumber);
            reference.child("Members").child(uid).setValue(gu);
        }


        HashMap<String, Object> groupDetails = new HashMap<>();
        groupDetails.put("Creator", admin);
        groupDetails.put("Group_Name", groupName1);
        groupDetails.put("Group_Description", groupInfo1);
        groupDetails.put("time", timeStamp);
        groupDetails.put("Group_Image", imageUrl);
        reference.child("Details").setValue(groupDetails);












        //        HashMap<String, Object> l = new HashMap<>();
//        l.put("admin", groupMembers.get(1).getUid());
//        FirebaseDatabase.getInstance().getReference().child("Groups").child("-MfO7LLyGKb6J-4-0Pxr").child("Members").child(groupMembers.get(2).getUid()).child("admin").setValue(true);
    }

//    private void searchUsers(String query) {
//        ArrayList<AddUserEntity> searchList = new ArrayList<>();
//        for (int i = 0; i<allData.size(); i++){
//            if (allData.get(i).getName().toLowerCase().startsWith(query.toLowerCase().trim())){
//                searchList.add(allData.get(i));
//            }
//            mAdapter.setNewUser(searchList);
//            if (query.equals("")){
//                viewModel.getAllContactUser().observe(this, addUserEntities -> {
//                    if (addUserEntities != null){
//                        mAdapter.setNewUser(addUserEntities);
//                    }
//                });
//            }
//        }
//    }
}