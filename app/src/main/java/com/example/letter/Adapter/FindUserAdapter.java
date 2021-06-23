package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Models.User;
import com.example.letter.R;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserAdapter extends RecyclerView.Adapter<FindUserAdapter.viewHolder>{

    ArrayList<User> userList;
    Context context;
    newUserClicked newUserClicked;
    public FindUserAdapter(ArrayList<User> userList, Context context, newUserClicked newUserClicked) {
        this.userList = userList;
        this.context = context;
        this.newUserClicked = newUserClicked;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new viewHolder(view, newUserClicked);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userNumber.setText(user.getPhoneNumber());

        if (user.getImage_url().trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(context).load(url).placeholder(R.drawable.avatar).into(holder.user_image);
        }else {
            Glide.with(context).load(user.getImage_url()).placeholder(R.drawable.avatar).into(holder.user_image);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView user_image;
        TextView userName,userNumber,chat_time;
        newUserClicked newUserClicked;
        public viewHolder(@NonNull View itemView, FindUserAdapter.newUserClicked newUserClicked) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userNumber = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
            chat_time.setVisibility(View.GONE);
            this.newUserClicked = newUserClicked;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            newUserClicked.onUserClicked(userList.get(getAdapterPosition()));
        }
    }
    public interface newUserClicked{
        void onUserClicked(User newUser);
    }
}
