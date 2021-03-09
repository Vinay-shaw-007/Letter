package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Models.Message;
import com.example.letter.R;
import com.example.letter.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{


    UserItemClicked userItemClicked;
    Context context;
    ArrayList<User> users;
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
        User user = users.get(position);
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId+user.getUid();
        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            String lastMsgTime = snapshot.child("lastMsgTime").getValue(String.class);
                            holder.last_msg.setText(lastMsg);
//                            Toast.makeText(context, (int) time, Toast.LENGTH_SHORT).show();
                            holder.chat_time.setText(lastMsgTime);
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
        Glide.with(context).load(user.getImage_url()).placeholder(R.drawable.avatar).into(holder.user_image);
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        CircleImageView user_image;
        TextView user_name,last_msg,chat_time;
        UserItemClicked userItemClicked;
        ArrayList<User> ClickedUser;
        public UserViewHolder(@NonNull View itemView, UserItemClicked userItemClicked, ArrayList<User> users) {
            super(itemView);
            user_image = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            last_msg = itemView.findViewById(R.id.last_msg);
            chat_time = itemView.findViewById(R.id.chat_time);
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