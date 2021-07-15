package com.example.letter.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.letter.UserRoomArchitecture.UserEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserModelAdapter extends RecyclerView.Adapter<UserModelAdapter.UserViewHolder>{

    UserItemClicked userItemClicked;
    Context context;
    List<UserEntity> users = new ArrayList<>();
    public UserModelAdapter(Context context , UserItemClicked userItemClicked){
        this.userItemClicked=userItemClicked;
        this.context=context;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation , parent , false);
        return new UserViewHolder(view, userItemClicked, users);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserEntity userEntity = users.get(position);
        String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String hisUid = userEntity.getUid();
        holder.user_name.setText(userEntity.getName());
        if (userEntity.getImage_url().trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(context).load(url).placeholder(R.drawable.avatar).into(holder.user_image);
        }else {
            Glide.with(context).load(userEntity.getImage_url()).placeholder(R.drawable.avatar).into(holder.user_image);
        }

    }
    public void setData(List<UserEntity> newUsers){
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        CircleImageView user_image;
        TextView user_name,last_msg,chat_time, unSeenMsg;
        UserItemClicked userItemClicked;
        List<UserEntity> ClickedUser;
        ConstraintLayout dashBoardLayout;
        public UserViewHolder(@NonNull View itemView, UserItemClicked userItemClicked, List<UserEntity> users) {
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
            dashBoardLayout.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            userItemClicked.onItemClicked(ClickedUser.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            userItemClicked.onLongItemClicked(ClickedUser.get(getAdapterPosition()));
            return true;
        }
    }
    public interface UserItemClicked{
        void onItemClicked(UserEntity user);
        void onLongItemClicked(UserEntity user);
    }
}