package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.AddUserRoomArchitecture.AddUserEntity;
import com.example.letter.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserModelAdapter extends RecyclerView.Adapter<FindUserModelAdapter.viewHolder>{

    private static final String TAG = "CHECKING_GROUP";
    ArrayList<AddUserEntity> userList = new ArrayList<>();
    static ArrayList<AddUserEntity> selectedUser = new ArrayList<>();
    Context context;
    newUserClicked newUserClicked;
    boolean formGroup;

    public FindUserModelAdapter(Context context, newUserClicked newUserClicked, boolean formGroup) {
        this.context = context;
        this.newUserClicked = newUserClicked;
        this.formGroup = formGroup;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new viewHolder(view, newUserClicked, userList);
    }


    public void setNewUser(List<AddUserEntity> newUser){
        userList.clear();
        userList.addAll(newUser);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        AddUserEntity user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userNumber.setText(user.getPhoneNumber());

        if (user.getImage_url() != null && user.getImage_url().trim().equals("No Image Uploaded")){
            String url = "https://firebasestorage.googleapis.com/v0/b/letter-7b8dc.appspot.com/o/Profiles%2Favatar.png?alt=media&token=6f152fe7-edef-47fb-85c5-a081a73cb760";
            Glide.with(context).load(url).placeholder(R.drawable.avatar).into(holder.user_image);
        }else {
            Glide.with(context).load(user.getImage_url()).placeholder(R.drawable.avatar).into(holder.user_image);
        }

        if (formGroup){
            holder.itemView.setOnClickListener(v -> {
                if (holder.checkBox.getVisibility() == View.GONE){
//                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                    holder.checkBox.setVisibility(View.VISIBLE);
                }else {
//                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    holder.checkBox.setVisibility(View.GONE);
                }
                newUserClicked.onUserClicked(user);
            });
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static ArrayList<AddUserEntity> getSelectedMembers(){
        return selectedUser;
    }

    public static class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView user_image;
        TextView userName,userNumber,chat_time;
        newUserClicked newUserClicked;
        ImageView checkBox;
        ArrayList<AddUserEntity> userEntities;
        public viewHolder(@NonNull View itemView, FindUserModelAdapter.newUserClicked newUserClicked, ArrayList<AddUserEntity> userList) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userNumber = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
            checkBox = itemView.findViewById(R.id.checkbox);
            chat_time.setVisibility(View.GONE);
            this.userEntities = userList;
            this.newUserClicked = newUserClicked;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()){
                newUserClicked.onUserClicked(userEntities.get(getAdapterPosition()));
            }
        }
    }
    public interface newUserClicked{
        void onUserClicked(AddUserEntity newUser);
    }
}
