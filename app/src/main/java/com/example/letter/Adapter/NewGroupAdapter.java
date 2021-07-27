package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.AddUserRoomArchitecture.AddUserEntity;
import com.example.letter.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupAdapter extends RecyclerView.Adapter<NewGroupAdapter.NewGroupViewHolder> {

    Context context;
    ArrayList<AddUserEntity> groupMembers;

    public NewGroupAdapter(Context context, ArrayList<AddUserEntity> groupMembers) {
        this.context = context;
        this.groupMembers = groupMembers;
    }

    @NonNull
    @NotNull
    @Override
    public NewGroupViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new NewGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NewGroupViewHolder holder, int position) {
        AddUserEntity user = groupMembers.get(position);
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
        return groupMembers.size();
    }

    public static class NewGroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user_image;
        TextView userName,userNumber,chat_time;
        public NewGroupViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userNumber = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
            chat_time.setVisibility(View.GONE);
        }
    }
}
