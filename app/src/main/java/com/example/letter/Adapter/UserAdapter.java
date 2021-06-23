package com.example.letter.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Models.Message;
import com.example.letter.Models.User;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{


    UserItemClicked userItemClicked;
    Context context;
    ArrayList<User> users;
    ArrayList<Message> messages ;
    int count;
    public UserAdapter(Context context ,UserItemClicked userItemClicked,  ArrayList<User> users){
        this.userItemClicked=userItemClicked;
        this.context=context;
        this.users=users;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation , parent , false);
        return new UserViewHolder(view, userItemClicked, users);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        messages = new ArrayList<>();
        User user = users.get(position);
        String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String hisUid = user.getUid();
        FirebaseDatabase.getInstance().getReference().child("DashBoard").child(myUid).child(hisUid).child("chats").child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            count = 0;
                            for (DataSnapshot snapshot1: snapshot.getChildren()){
                                Message message = snapshot1.getValue(Message.class);
                                if (!message.isSeen() && message.getSenderId().trim().equals(hisUid)){
                                    count++;
                                }
                                messages.add(message);
                            }
                            Collections.reverse(messages);

                            String lastMsg = messages.get(0).getMessage();

                            String lastMsgTime = messages.get(0).getTimeStamp();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM, HH:mm", Locale.getDefault());
                            if (lastMsg != null && lastMsgTime != null){
                                Date data = new Date(Long.parseLong(lastMsgTime));
                                holder.last_msg.setText(lastMsg);
                                holder.chat_time.setText(sdf.format(data));
                            }
                            if (count!=0){
                                holder.unSeenMsg.setVisibility(View.VISIBLE);
                                holder.unSeenMsg.setText(String.valueOf(count));
                                int seenMsgColor = Color.parseColor("#1FB9FF");
                                holder.chat_time.setTextColor(seenMsgColor);
                            }else {
                                count = 0;
                                holder.unSeenMsg.setVisibility(View.GONE);
                            }
                        }else{
                            holder.last_msg.setText("Tap to chat");
                            holder.chat_time.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.user_name.setText(user.getName());

        holder.dashBoardLayout.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("REMOVE");
            builder.setMessage("Are you sure you want to remove "+user.getName()+" ?");
            builder.setPositiveButton("REMOVE", (dialog, which) ->
                    FirebaseDatabase.getInstance().getReference().child("DashBoard")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getUid()).removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, user.getName()+" removed", Toast.LENGTH_SHORT).show()));
            builder.setNegativeButton("CANCEL",null);
            builder.show();
            return true;
        });
        if (user.getImage_url().trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(context).load(url).placeholder(R.drawable.avatar).into(holder.user_image);
        }else {
            Glide.with(context).load(user.getImage_url()).placeholder(R.drawable.avatar).into(holder.user_image);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        CircleImageView user_image;
        TextView user_name,last_msg,chat_time, unSeenMsg;
        UserItemClicked userItemClicked;
        ArrayList<User> ClickedUser;
        ConstraintLayout dashBoardLayout;
        public UserViewHolder(@NonNull View itemView, UserItemClicked userItemClicked, ArrayList<User> users) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            unSeenMsg = itemView.findViewById(R.id.unSeenMsg);
            last_msg = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
            dashBoardLayout = itemView.findViewById(R.id.dashBoardLayout);
            this.ClickedUser=users;
            this.userItemClicked=userItemClicked;
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            userItemClicked.onItemClicked(ClickedUser.get(getAdapterPosition()));
        }
    }
    public interface UserItemClicked{
        void onItemClicked(User user);
    }
}
//        FirebaseDatabase.getInstance().getReference().child("DashBoard").child(myUid).child(hisUid).child("chats").child("messages")
//                .addValueEventListener(new ValueEventListener() {
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()){
//                            for (DataSnapshot snapshot1: snapshot.getChildren()){
//                                Message message = snapshot1.getValue(Message.class);
//                                messages.add(message);
//                            }
//                            Collections.reverse(messages);
//
//                            String lastMsg = messages.get(0).getMessage();
//
//                            String lastMsgTime = messages.get(0).getTimeStamp();
//
//                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM, HH:mm", Locale.getDefault());
//                            if (lastMsg != null && lastMsgTime != null){
//                                Date data = new Date(Long.parseLong(lastMsgTime));
//                                holder.last_msg.setText(lastMsg);
//                                holder.chat_time.setText(sdf.format(data));
//                            }
//
//
//                        }else{
//                            holder.last_msg.setText("Tap to chat");
//                            holder.chat_time.setText("");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });