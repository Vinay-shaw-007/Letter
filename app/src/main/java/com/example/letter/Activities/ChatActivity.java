package com.example.letter.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Adapter.UserChatAdapter;
import com.example.letter.Models.Message;
import com.example.letter.Models.User;
import com.example.letter.Notification.APIService;
import com.example.letter.Notification.Client;
import com.example.letter.Notification.Data;
import com.example.letter.Notification.Response;
import com.example.letter.Notification.Sender;
import com.example.letter.Notification.Token;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramotion.circlemenu.CircleMenuView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    private String hisName, hisProfileImage, hisUid;
    private ImageView btn_back, profileImage, btn_add, btn_send;
    private TextView name, hisCurrentStatus;
    private AppCompatImageButton btn_down;
    private FirebaseDatabase database;
    private FirebaseUser myUid;
    private ArrayList<Message> mMessages;
    private RecyclerView mRecyclerView;
    private UserChatAdapter mAdapter;
    private EmojiconEditText messageBox;
    private NestedScrollView scrollView;
    private boolean menuVisibility = true;
    private CircleMenuView circleMenuView;
    public static final int REQUEST_STORAGE = 1;
    public static final int REQUEST_FILE = 2;
    public static final int REQUEST_CAMERA_PIC = 3;
    private ProgressDialog imageUploading;
    private String currentPhotoPath;
    private Uri imageUri;
    private ValueEventListener seenListener;
    private DatabaseReference myReference, hisReference;
    private APIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        hookups();
        setToolbar();
        getOtherUserDetails();
        setOtherUserDetails();
        buttonClicks();
        fetchChats();
        seenMessage(hisUid);
        typingState();
        showOnlineIndicator();
        showEmoticonKeyboard();
    }

    private void showEmoticonKeyboard() {
        View view = findViewById(R.id.chatRoot);
        ImageView emoji = findViewById(R.id.insertEmoticons);
        EmojIconActions emojIconActions;
        emojIconActions = new EmojIconActions(this,view,messageBox,emoji);
        emojIconActions.setUseSystemEmoji(true);
        emojIconActions.ShowEmojIcon();
    }

    private void showOnlineIndicator() {
        //it will show online when user is online and show nothing when user goes offline
        database.getReference().child("presentStatus").child(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if (!Objects.requireNonNull(status).isEmpty()){
                        if (status.equals("Offline")){
                            hisCurrentStatus.setVisibility(View.GONE);
                        }else{
                            hisCurrentStatus.setVisibility(View.VISIBLE);
                            hisCurrentStatus.setText(status);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void typingState() {
        //changing the online status to typing when other user start typing and show online when it stop typing
        final Handler handler = new Handler();
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presentStatus").child(myUid.getUid()).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);

            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presentStatus").child(myUid.getUid()).setValue("Online");
                }
            };
        });
    }

    private void seenMessage(String hisUid) {

        myReference = FirebaseDatabase.getInstance().getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages");
        hisReference = FirebaseDatabase.getInstance().getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages");
        seenListener = myReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot snapshot1: snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            if (Objects.requireNonNull(message).getSenderId().equals(hisUid) && message.getReceiverId().equals(myUid.getUid())) {
                                HashMap<String, Object> msgSeen = new HashMap<>();
                                msgSeen.put("seen", true);
                                myReference.child(Objects.requireNonNull(snapshot1.getKey())).updateChildren(msgSeen);
                                hisReference.child(Objects.requireNonNull(snapshot1.getKey())).updateChildren(msgSeen);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void fetchChats() {
        database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mMessages.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                mMessages.add(message);
                            }
                            mAdapter = new UserChatAdapter(ChatActivity.this);
                            mAdapter.sendChats(mMessages);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void buttonClicks() {
        btn_back.setOnClickListener(v -> onBackPressed());
        btn_send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(messageBox.getText())){
                checkUser();
                sendMessage();
                messageBox.setText("");
            }else {
                messageBox.setError("Type Something");
            }
        });
        btn_add.setOnClickListener(v -> {

            if (menuVisibility){
                circleMenuView.setVisibility(View.VISIBLE);
                circleMenu();
                menuVisibility = false;
            }else {
                circleMenuView.setVisibility(View.GONE);
                menuVisibility = true;
            }

        });
        btn_down.setOnClickListener(v ->
                scrollView.post(() ->
                        scrollView.fullScroll(View.FOCUS_DOWN)));
    }

    private void circleMenu() {
        circleMenuView.setEventListener(new CircleMenuView.EventListener(){
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                super.onMenuOpenAnimationStart(view);
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                super.onMenuOpenAnimationEnd(view);
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                super.onMenuCloseAnimationStart(view);
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                super.onMenuCloseAnimationEnd(view);
                menuVisibility=true;
                circleMenuView.setVisibility(View.GONE);
            }

            @Override
            public boolean onButtonLongClick(@NonNull CircleMenuView view, int buttonIndex) {
                return super.onButtonLongClick(view, buttonIndex);
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                super.onButtonClickAnimationStart(view, buttonIndex);
                switch (buttonIndex){
                    case 0:  selectImageFromGallery();
                        break;
                    case 1: selectImageFromCamera();
                        break;
                }
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                super.onButtonClickAnimationEnd(view, buttonIndex);
                menuVisibility=true;
                circleMenuView.setVisibility(View.GONE);
            }

            @Override
            public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                super.onButtonLongClickAnimationStart(view, buttonIndex);
            }

            @Override
            public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                super.onButtonLongClickAnimationEnd(view, buttonIndex);
            }
        });
    }

    private void selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }else {
            selectImage();
        }
    }

    private void selectImageFromCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }else {
            openCamera();
        }
    }

    private void openCamera() {
        String fileName = "LetterImage";

        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();
            imageUri = FileProvider.getUriForFile(ChatActivity.this, "com.example.letter.fileprovider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            requestCamera.launch(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        requestFile.launch(intent);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK && data != null && data.getData() != null){
////            Uri uri1 = data.getData();
////            imageUploading.show();
////            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chats").child(myUid.getUid());
////            storageReference.putFile(uri1).addOnCompleteListener(task ->
////                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
////                        String randomKey, imageUrl, date;
////                        randomKey = database.getReference().push().getKey();
////                        date = System.currentTimeMillis()+"";
////                        imageUrl = uri.toString();
////                        Message message = new Message("Photo",myUid.getUid(),hisUid,date,randomKey);
////                        message.setImageUrl(imageUrl);
////                        database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages").child(Objects.requireNonNull(randomKey)).setValue(message)
////                                .addOnSuccessListener(aVoid ->
////                                        database.getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages").child(randomKey).setValue(message)
////                                        .addOnSuccessListener(aVoid1 -> imageUploading.dismiss()));
////                    }));
////        }
////        else if (requestCode == REQUEST_CAMERA_PIC  && resultCode == RESULT_OK){
////            imageUploading.show();
////            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chats").child(myUid.getUid());
//            storageReference.putFile(imageUri).addOnCompleteListener(task ->
//                    storageReference.getDownloadUrl().addOnSuccessListener(uriImage1 -> {
////                        String randomKey, imageUrl, date;
////                        randomKey = database.getReference().push().getKey();
////                        date = System.currentTimeMillis()+"";
////                        imageUrl = uriImage1.toString();
////                        Message message = new Message("Photo",myUid.getUid(),hisUid,date,randomKey);
////                        message.setImageUrl(imageUrl);
////                        database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages").child(Objects.requireNonNull(randomKey)).setValue(message)
////                                .addOnSuccessListener(aVoid ->
////                                        database.getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages").child(randomKey).setValue(message)
////                                                .addOnSuccessListener(aVoid1 -> imageUploading.dismiss()));
////                    }))
////            .addOnFailureListener(e -> {
////                Toast.makeText(ChatActivity.this, ""+e, Toast.LENGTH_SHORT).show();
////                Log.d("cameraImage", "onFailure: "+e);
////                Log.d("cameraImage", "onFailure: "+currentPhotoPath);
////                Log.d("cameraImage", "onFailure: "+imageUri);
////            });
////        }
//    }

    private void sendMessage() {
        scrollView.post(() ->
                scrollView.fullScroll(View.FOCUS_DOWN));
        String messageText, randomKey, date;
        messageText = messageBox.getText().toString().trim();
        date = System.currentTimeMillis()+"";
        randomKey = database.getReference().push().getKey();
        Message message = new Message(messageText,myUid.getUid(),hisUid,date,randomKey);
        message.setSeen(false);
        database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages").child(Objects.requireNonNull(randomKey)).setValue(message)
                .addOnSuccessListener(aVoid -> database.getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages").child(randomKey).setValue(message)
                        .addOnSuccessListener(aVoid1 -> {

                        }));
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                        assert user != null;
                        sendNotification(hisUid,user.getName(),messageText);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void hookups()  {
        //binding with imageView
        btn_back = findViewById(R.id.backBtn);
        profileImage = findViewById(R.id.profileImage);
        btn_add = findViewById(R.id.add);
        btn_send = findViewById(R.id.sendBtn);
        btn_down = findViewById(R.id.downView);

        //binding with textView
        name = findViewById(R.id.profileName);
        hisCurrentStatus = findViewById(R.id.currentStatus);

        //initializing firebase
        database = FirebaseDatabase.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser();
        //initializing arrayList of messages
        mMessages = new ArrayList<>();

        //binding recyclerView
        mRecyclerView = findViewById(R.id.chat_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setNestedScrollingEnabled(false);
//        mRecyclerView.smoothScrollToPosition(mMessages.size()-1);

        //binding with editText
        messageBox = findViewById(R.id.messageBox);

        //binding with ScrollView
        scrollView = findViewById(R.id.scrollView);

        //binding with circleMenuView
        circleMenuView = findViewById(R.id.circleMenuView);

        //Initializing progressDialog
        imageUploading = new ProgressDialog(this);
        imageUploading.setCancelable(false);
        imageUploading.setMessage("Sending Image");

        //calling apiServices
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

    }

    private void setOtherUserDetails() {
        //set his details in the toolbar
        name.setText(hisName);
        if (hisProfileImage.trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(this).load(url).placeholder(R.drawable.avatar).into(profileImage);
        }else {
            Glide.with(this)
                    .load(hisProfileImage)
                    .placeholder(R.drawable.avatar)
                    .into(profileImage);
        }
    }

    private void getOtherUserDetails() {
        //fetching his details like name profileImage and his uid
        hisName = getIntent().getStringExtra("name");
        hisProfileImage = getIntent().getStringExtra("image");
        hisUid = getIntent().getStringExtra("uid");
    }

    private void setToolbar() {
        //set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void checkUser() {
        DatabaseReference Reference = FirebaseDatabase.getInstance().getReference().child("DashBoard").child(hisUid).child(myUid.getUid());
        Reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child("users").child(myUid.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Reference.setValue(user);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String hisUid, String myName, String messageText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = reference.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(myUid.getUid(),"New Message : "+messageText,""+myName, hisUid, R.mipmap.ic_launcher_round1);
                    Sender sender = new Sender(data, Objects.requireNonNull(token).getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(@NotNull Call<Response> call, @NotNull retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NotNull Call<Response> call, @NotNull Throwable t) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUserNotification(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        scrollView.post(() ->
                scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUserId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presentStatus").child(Objects.requireNonNull(currentUserId)).setValue("Online");
        currentUserNotification(hisUid);
    }

    @Override
    protected void onPause() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presentStatus").child(Objects.requireNonNull(currentUserId)).setValue("Offline");
        super.onPause();
        myReference.removeEventListener(seenListener);
        currentUserNotification("none");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    ActivityResultLauncher<Intent> requestCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                Intent data = result.getData();
                Uri imageUri1 = data.getData();
                imageUploading.show();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chats").child(myUid.getUid());
                storageReference.putFile(imageUri1).addOnCompleteListener(task ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uriImage1 -> {
                            String randomKey, imageUrl, date;
                            randomKey = database.getReference().push().getKey();
                            date = System.currentTimeMillis()+"";
                            imageUrl = uriImage1.toString();
                            Message message = new Message("Photo",myUid.getUid(),hisUid,date,randomKey);
                            message.setImageUrl(imageUrl);
                            database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages").child(Objects.requireNonNull(randomKey)).setValue(message)
                                    .addOnSuccessListener(aVoid ->
                                            database.getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages").child(randomKey).setValue(message)
                                                    .addOnSuccessListener(aVoid1 -> imageUploading.dismiss()));
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                            Log.d("cameraImage", "onFailure: "+e);
                            Log.d("cameraImage", "onFailure: "+currentPhotoPath);
                            Log.d("cameraImage", "onFailure: "+imageUri);
                        });
            }

        }
    }) ;
    ActivityResultLauncher<Intent> requestFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                Intent data = result.getData();
                Uri uri1 = data.getData();
                imageUploading.show();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chats").child(myUid.getUid());
                storageReference.putFile(uri1).addOnCompleteListener(task ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String randomKey, imageUrl, date;
                            randomKey = database.getReference().push().getKey();
                            date = System.currentTimeMillis()+"";
                            imageUrl = uri.toString();
                            Message message = new Message("Photo",myUid.getUid(),hisUid,date,randomKey);
                            message.setImageUrl(imageUrl);
                            database.getReference().child("DashBoard").child(myUid.getUid()).child(hisUid).child("chats").child("messages").child(Objects.requireNonNull(randomKey)).setValue(message)
                                    .addOnSuccessListener(aVoid ->
                                            database.getReference().child("DashBoard").child(hisUid).child(myUid.getUid()).child("chats").child("messages").child(randomKey).setValue(message)
                                                    .addOnSuccessListener(aVoid1 -> imageUploading.dismiss()));
                        }));
            }
        }
    });

}