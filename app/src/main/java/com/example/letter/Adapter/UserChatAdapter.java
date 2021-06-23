package com.example.letter.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Models.Message;
import com.example.letter.R;
import com.example.letter.Utils.MyDiffUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserChatViewHolder>{

    private final int MSG_SEND = 1;
    Context context;
    ArrayList<Message> messages;
    public UserChatAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_SEND){
            view = LayoutInflater.from(context).inflate(R.layout.message_send_layout, parent, false);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.message_receive_layout, parent, false);
        }
        return new UserChatViewHolder(view);

    }

    @Override
    public int getItemViewType(int position) {
        int MSG_RECEIVE = 2;
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(messages.get(position).getSenderId())){
            return MSG_SEND;
        }else return MSG_RECEIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull UserChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageET.setEmojiconSize(80);
        holder.messageET.setText(message.getMessage());
        if (message.getMessage() !=null && message.getMessage().trim().equals("Photo")){
            holder.messageET.setVisibility(View.GONE);
            holder.chatImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.chatImage);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(Long.parseLong(message.getTimeStamp()));
        holder.timeTV.setText(sdf.format(date));
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(message.getSenderId())){
            if (message.isSeen()){
                int seenMsgColor = Color.parseColor("#1FB9FF");
                holder.seenMsg.setColorFilter(seenMsgColor);
            }
        }

    }

    @Override
    public int getItemCount() {
        return messages!=null ? messages.size():0;
    }

    public static class UserChatViewHolder extends RecyclerView.ViewHolder {
        TextView timeTV, same_msg_date;
        EmojiconTextView messageET;
        View view;
        ImageView chatImage, seenMsg;
        public UserChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageET = itemView.findViewById(R.id.messageET);
            timeTV = itemView.findViewById(R.id.timeTV);
            same_msg_date = itemView.findViewById(R.id.same_msg_date);
            seenMsg = itemView.findViewById(R.id.seenMsg);
            chatImage = itemView.findViewById(R.id.chatImage);
            view = itemView.findViewById(R.id.rootView);
        }
    }

    public void sendChats(ArrayList<Message> newMessage){
        MyDiffUtil diffUtil = new MyDiffUtil(messages,newMessage);
        DiffUtil.DiffResult diffResults = DiffUtil.calculateDiff(diffUtil);
        messages = newMessage;
        diffResults.dispatchUpdatesTo(this);
    }
}
/*    public UserChatAdapter(Context context) {
        this.context = context;
    }
    public void update(ArrayList<Message> messages1){
        messages.clear();
        messages.addAll(messages1);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }else {
            return 200;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;
            viewHolder.messageSent.setText(message.getMessage());
            if (message.getMessage() != null && message.getMessage().trim().equals("Image")){

                viewHolder.sendImage.setVisibility(View.VISIBLE);
                viewHolder.messageSent.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.sendImage);
            }
        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            viewHolder.messageReceive.setText(message.getMessage());
            if (message.getMessage() !=null &&  message.getMessage().trim().equals("Image")){
                viewHolder.receiveImage.setVisibility(View.VISIBLE);
                viewHolder.messageReceive.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.receiveImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size():0;
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageSent, message_send_time,seen;
        ImageView sendImage;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSent = itemView.findViewById(R.id.message_send);
            message_send_time = itemView.findViewById(R.id.message_send_time);
            seen = itemView.findViewById(R.id.seen);
            sendImage = itemView.findViewById(R.id.sendImage);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView messageReceive, message_receive_time;
        ImageView receiveImage;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveImage = itemView.findViewById(R.id.receiveImage);
            messageReceive = itemView.findViewById(R.id.message_receive);
        }
    }*/
