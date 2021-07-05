package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Models.User;
import com.example.letter.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserAdapter extends RecyclerView.Adapter<FindUserAdapter.viewHolder>{

    ArrayList<User> userList;
    static ArrayList<User> selectedMembers;
    Context context;
    newUserClicked newUserClicked;
    boolean from_group;
    public FindUserAdapter(ArrayList<User> userList, Context context, newUserClicked newUserClicked, boolean from_group) {
        this.userList = userList;
        this.context = context;
        this.newUserClicked = newUserClicked;
        this.from_group = from_group;
        selectedMembers = new ArrayList<>();
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
        if (from_group){
            holder.checkBox.setVisibility(View.VISIBLE);
            for (User i: selectedMembers) {
                holder.checkBox.setChecked(true);
            }
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedMembers.contains(user)) {
                        selectedMembers.remove(user);
                    } else {
                        selectedMembers.add(user);
                    }
                }
            });
        }
    }

    public static void setSelectedMembers(ArrayList<User> gm){
        selectedMembers = gm;
    }

    public static ArrayList<User> getSelectedMembers(){
        return selectedMembers;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView user_image;
        TextView userName,userNumber,chat_time;
        newUserClicked newUserClicked;
        CheckBox checkBox;
        public viewHolder(@NonNull View itemView, FindUserAdapter.newUserClicked newUserClicked) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userNumber = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
            checkBox = itemView.findViewById(R.id.checkbox);
            chat_time.setVisibility(View.GONE);
            if (!from_group){
                this.newUserClicked = newUserClicked;
                itemView.setOnClickListener(this);
            }

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
