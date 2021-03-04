package com.example.letter.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.letter.Adapter.MessageAdapter;
import com.example.letter.Models.Message;
import com.example.letter.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class UserChatActivity extends AppCompatActivity {

    MessageAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        EditText messagebox = findViewById(R.id.messagebox);
        ImageView sendBtn = findViewById(R.id.sendBtn);
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        setSupportActionBar(toolbar);
        messages = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid+receiverUid;
        receiverRoom = receiverUid+senderUid;
        adapter = new MessageAdapter(this, messages, senderRoom, receiverRoom);
        recyclerView.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Message message = snapshot1.getValue(Message.class);
                    message.setMessageId(snapshot1.getKey());
                    messages.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = messagebox.getText().toString().trim();
                if (messageTxt.isEmpty()){
                    return;
                }
                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, date.getTime());
                messagebox.setText("");
                String randomKey = database.getReference().push().getKey();
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
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
            }
        });
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}