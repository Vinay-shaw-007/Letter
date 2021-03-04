package com.example.letter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Models.Message;
import com.example.letter.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SEND=1 , ITEM_RECEIVE=2;
    String senderRoom, receiverRoom;
    public MessageAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SEND;
        }
        else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        int[] reactions =new int[]{
                R.drawable.ic_angy_emoji,
                R.drawable.ic_cry_emoji,
                R.drawable.ic_dislike_emoji,
                R.drawable.ic_happy_emoji,
                R.drawable.ic_heart_emoji,
                R.drawable.ic_like_emoji,
                R.drawable.ic_love_emoji,
                R.drawable.ic_shock_emoij,
                R.drawable.ic_swag_emoji
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == SendViewHolder.class){
                SendViewHolder viewHolder = (SendViewHolder)holder;
                viewHolder.feeling.setImageResource(reactions[pos]);
                viewHolder.feeling.setVisibility(View.VISIBLE);
            }
            else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
                viewHolder.feeling.setImageResource(reactions[pos]);
                viewHolder.feeling.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });
        if (holder.getClass() == SendViewHolder.class){
            SendViewHolder viewHolder = (SendViewHolder)holder;
            viewHolder.message_send.setText(message.getMessage());
            if (message.getFeeling() >= 0){
//                message.setFeeling(reactions[(int) message.getFeeling()]);
                viewHolder.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.feeling.setVisibility(View.VISIBLE);
            }else {
                viewHolder.feeling.setVisibility(View.GONE);
            }
            viewHolder.message_send.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.message_receive.setText(message.getMessage());
            if (message.getFeeling() >= 0){
                viewHolder.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.feeling.setVisibility(View.VISIBLE);
            }else {
                viewHolder.feeling.setVisibility(View.GONE);
            }
            viewHolder.message_receive.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {
        ImageView feeling;
        TextView message_send;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            message_send = itemView.findViewById(R.id.message_send);
            feeling = itemView.findViewById(R.id.feeling);
        }
    }
    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ImageView feeling;
        TextView message_receive;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            message_receive = itemView.findViewById(R.id.message_receive);
            feeling = itemView.findViewById(R.id.feeling);
        }
    }
}
