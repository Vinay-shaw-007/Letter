package com.example.letter.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Adapter.MessageAdapter;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.ramotion.circlemenu.CircleMenuView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class UserChatActivity extends AppCompatActivity implements MessageAdapter.itemClicked {

    private MessageAdapter adapter;
    private ArrayList<Message> messages;
    private String senderRoom, receiverRoom, senderUid, receiverUid;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference mReference, mReference1;
    private RecyclerView recyclerView;
    private UserChatAdapter mAdapter;
    private ProgressDialog dialog;
    private EditText messageBox;
    private TextView currentStatus;
    private APIService apiService;
    private boolean notify =false, menuVisibility = true;
    private final int REQUEST_STORAGE = 100;
    private final int REQUEST_FILE = 200;
    private String  stringPath;
    private Intent iData;
    private Uri uri;
    private ProgressDialog dialog1;
    private String name;
    private ValueEventListener seenListener;
    CircleMenuView circleMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        messageBox = findViewById(R.id.messagebox);
        recyclerView = findViewById(R.id.chat_recycler_view);
        ImageView sendBtn = findViewById(R.id.sendBtn);
        ImageView attachment = findViewById(R.id.attachment);
        ImageView add = findViewById(R.id.add);
        TextView profileName = findViewById(R.id.profileName);
        currentStatus = findViewById(R.id.currentStatus);
        ImageView profileImage1 = findViewById(R.id.profileImage);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView downView = findViewById(R.id.downView);
        recyclerView = findViewById(R.id.chat_recycler_view);
        circleMenuView = findViewById(R.id.circleMenuView);


        dialog1 = new ProgressDialog(this);
        dialog1.setMessage("Fetching Chats");
        dialog1.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage =  FirebaseStorage.getInstance();
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        name = getIntent().getStringExtra("name");
        String profileImage = getIntent().getStringExtra("image");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        senderRoom = senderUid+receiverUid;
        receiverRoom = receiverUid+senderUid;

        messages = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this,this, messages, senderUid, receiverUid);
//        mAdapter.notifyDataSetChanged();
//        mAdapter = new UserChatAdapter(this);
        recyclerView.setAdapter(mAdapter);
        fetchExistingChats();


        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);


        profileName.setText(name);
        if (profileImage.trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(this).load(url).placeholder(R.drawable.avatar).into(profileImage1);
        }else {
            Glide.with(this)
                    .load(profileImage)
                    .placeholder(R.drawable.avatar)
                    .into(profileImage1);
        }


        database.getReference().child("presentStatus").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()){
                        if (status.equals("Offline")){
                            currentStatus.setVisibility(View.GONE);
                        }else{
                            currentStatus.setVisibility(View.VISIBLE);
                            currentStatus.setText(status);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(receiverUid);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuVisibility){
                    circleMenuView.setVisibility(View.VISIBLE);
                    circleMenuView();
                    menuVisibility =false;
                }else {
                    menuVisibility=true;
                    circleMenuView.setVisibility(View.INVISIBLE);
                }

            }
        });


        sendBtn.setOnClickListener(v -> {
            notify = true;
            String messageTxt = messageBox.getText().toString().trim();
            if (messageTxt.isEmpty()){
                messageBox.setError("Type Something...");
            }else {

//                bottomClick();
                checkUser();
                sendData(messageTxt, messageBox);

            }
            messageBox.setText("");

        });

        downView.setOnClickListener(v -> bottomClick());

        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

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
                database.getReference().child("presentStatus").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);

            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presentStatus").child(senderUid).setValue("Online");
                }
            };
        });


    }


    private void seenMessage(String userId1){
        mReference = FirebaseDatabase.getInstance().getReference().child("chats").child(userId1).child(senderUid).child("messages");
        mReference1 = FirebaseDatabase.getInstance().getReference().child("chats").child(senderUid).child(userId1).child("messages");
        seenListener = mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Message message = snapshot1.getValue(Message.class);
                    if (message.getSenderId().equals(userId1) &&message.getReceiverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                        HashMap<String , Object> message1 = new HashMap<>();
                        message1.put("seen", true);
                        mReference.child(snapshot1.getKey()).updateChildren(message1);
                        mReference1.child(snapshot1.getKey()).updateChildren(message1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_FILE);
    }

    private void checkUser() {
        DatabaseReference Reference = FirebaseDatabase.getInstance().getReference().child("DashBoard").child(receiverUid).child(senderUid);
        Reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child("users").child(senderUid).addValueEventListener(new ValueEventListener() {
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

    private void fetchExistingChats() {
        dialog1.show();
        database.getReference().child("DashBoard").child(senderUid).child(receiverUid)
                .child("chats")
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
//                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);

                        }
                        adapter = new MessageAdapter(UserChatActivity.this,UserChatActivity.this, messages, senderUid, receiverUid);
//                        mAdapter.sendChats(messages);
                        recyclerView.setAdapter(adapter);

//                        mAdapter.notifyDataSetChanged();
                        dialog1.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendData(String messageTxt, EditText messagebox) {
        recyclerView.scrollToPosition(messages.size()-1);
        String randomKey  = database.getReference().push().getKey();
        String date = String.valueOf(System.currentTimeMillis());
        Message message = new Message(messageTxt, senderUid, receiverUid, date, randomKey);
        message.setSeen(false);
        HashMap<String , Object >  lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg",message.getMessage());
        lastMsgObj.put("lastMsgTime", date);

        database.getReference().child("DashBoard").child(senderUid).child(receiverUid).child("chats").updateChildren(lastMsgObj);
        database.getReference().child("DashBoard").child(receiverUid).child(senderUid).child("chats").updateChildren(lastMsgObj);
        database.getReference().child("DashBoard").child(senderUid).child(receiverUid)
                .child("chats")
                .child("messages")
                .child(Objects.requireNonNull(randomKey))
                .setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("DashBoard").child(receiverUid).child(senderUid)
                                .child("chats")
                                .child("messages")
                                .child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                    }
                });
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if (notify){
                        assert user != null;
                        sendNotification(receiverUid,user.getName(),messageTxt);
                    }
                    notify  = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiverUid, String name, String messageTxt) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = reference.orderByKey().equalTo(receiverUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(senderUid,"New Message: "+messageTxt,""+name, receiverUid, R.mipmap.ic_launcher_round1);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(UserChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void circleMenuView() {

        circleMenuView.setEventListener(new CircleMenuView.EventListener(){
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                Log.d("circleMenuView", "onMenuOpenAnimationStart: ");
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("circleMenuView", "onMenuOpenAnimationEnd: ");
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                Log.d("circleMenuView", "onMenuCloseAnimationStart: ");
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("circleMenuView", "onMenuCloseAnimationEnd: ");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                Log.d("circleMenuView", "onButtonClickAnimationStart: "+buttonIndex);
                switch (buttonIndex){
                    case 0:  selectImageFromGallery();
                        break;
                    case 1: selectImageFromCamera();
                        break;
                }
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                Log.d("circleMenuView", "onButtonClickAnimationEnd: ");
                menuVisibility=true;
                circleMenuView.setVisibility(View.INVISIBLE);
            }

            @Override
            public boolean onButtonLongClick(@NonNull CircleMenuView view, int buttonIndex) {
                return true;
            }

            @Override
            public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                Log.d("circleMenuView", "onButtonLongClickAnimationStart: ");
            }

            @Override
            public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
                Log.d("circleMenuView", "onButtonLongClickAnimationEnd: ");
            }
        });
    }

    private void selectImageFromCamera() {
//        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE);
//        }else {
//            selectImage();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK){
            uri = data.getData();
            iData= data;
            dialog.show();
            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chats").child(senderUid);
            storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String randomKey = FirebaseDatabase.getInstance().getReference().push().getKey();
                                String imageUrl = uri.toString();
                                String date = System.currentTimeMillis()+"";
                                Message message = new Message("Image",senderUid,receiverUid,date,randomKey);
                                message.setImageUrl(imageUrl);
                                message.setSeen(false);
                                HashMap<String , Object >  map = new HashMap<>();
                                map.put("lastMsg",message.getMessage());
                                map.put("lastMsgTime", date);
                                database1.getReference().child("DashBoard").child(senderUid).child(receiverUid).child("chats").updateChildren(map);
                                database1.getReference().child("DashBoard").child(receiverUid).child(senderUid).child("chats").updateChildren(map);
                                database1.getReference().child("DashBoard").child(senderUid).child(receiverUid).child("chats").child("messages")
                                        .child(Objects.requireNonNull(randomKey))
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database1.getReference().child("DashBoard").child(receiverUid).child(senderUid).child("chats").child("messages")
                                                        .child(randomKey)
                                                        .setValue(message)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
                    }
                }
            });
        }
    }

    private void selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE);
        }else {
            selectImage();
        }
    }

    private void currentUserNotification(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUserId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presentStatus").child(currentUserId).setValue("Online");
        currentUserNotification(receiverUid);
    }

    @Override
    protected void onStart() {
        super.onStart();

        recyclerView.scrollToPosition(messages.size()-1);
    }

    @Override
    protected void onPause() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presentStatus").child(currentUserId).setValue("Offline");
        super.onPause();
        currentUserNotification("none");
        mReference.removeEventListener(seenListener);
    }

    public void bottomClick() {
        recyclerView.scrollToPosition(messages.size()-1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onImageClicked(int pos, Message message) {

        Uri parse = Uri.parse(message.getImageUrl());
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("imageUrl", message.getImageUrl());
        intent.putExtra("userName", name);
        intent.putExtra("time", message.getTimeStamp());
        intent.putExtra("senderId", message.getSenderId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
//        overridePendingTransition(R.anim.zoom_in,R.anim.static_animation);
//        String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//        Log.d("showImage", "onImageClicked: Inside image clicked link "+parse);
//        Cursor cursor = getContentResolver().query(parse,filePathColumn,null,null,null);
//        if (cursor == null){
//            Toast.makeText(this, "Inside cursor null1", Toast.LENGTH_SHORT).show();
//            Log.d("showImage", "onImageClicked: Inside cursor null1");
//            stringPath = parse.getPath();
//        }else {
//            Toast.makeText(this, "Inside cursor not null1", Toast.LENGTH_SHORT).show();
//            Log.d("showImage", "onImageClicked: Inside cursor not null1");
//
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            stringPath = cursor.getString(columnIndex);
//        }
//        if (checkReadStoragePermission()){
//            Toast.makeText(this, "Inside open gallery code1", Toast.LENGTH_SHORT).show();
//            Log.d("showImage", "onImageClicked: Inside open gallery code1 "+stringPath+" -----");
//            File file = new File(stringPath);
//            if (file.exists()){
//                Uri uri1;
//                uri1 = FileProvider.getUriForFile(UserChatActivity.this, UserChatActivity.this.getPackageName() +"."+ BuildConfig.APPLICATION_ID+".provider",file);
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setDataAndType(uri1, "image/*");
//                UserChatActivity.this.startActivity(intent);
//            }
//
//        }else {
//            Toast.makeText(this, "Inside ask permission", Toast.LENGTH_SHORT).show();
//            Log.d("showImage", "onImageClicked: Inside ask permission");
//            ActivityCompat.requestPermissions(UserChatActivity.this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},REQUEST_STORAGE);
//        }
    }

    private boolean checkReadStoragePermission() {
        return ContextCompat.checkSelfPermission(UserChatActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }
}