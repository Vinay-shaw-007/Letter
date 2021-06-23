package com.example.letter.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Activities.UserChatActivity;
import com.example.letter.Models.Message;
import com.example.letter.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter{

    private final Context context;
    private final ArrayList<Message> messages;
    private final int ITEM_SEND=1 , ITEM_RECEIVE=2;
    private final String senderRoom;
    private final String receiverRoom;
    private final boolean tap_on_send = false;
    private final boolean tap_on_receive = false;
    private final itemClicked itemClicked;
    public MessageAdapter(Context context, itemClicked itemClicked, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.itemClicked = itemClicked;
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

    @SuppressLint({"ClickableViewAccessibility", "ResourceType", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
//        int[] reactions =new int[]{
//                R.drawable.ic_angy_emoji,
//                R.drawable.ic_cry_emoji,
//                R.drawable.ic_dislike_emoji,
//                R.drawable.ic_happy_emoji,
//                R.drawable.ic_heart_emoji,
//                R.drawable.ic_like_emoji,
//                R.drawable.ic_love_emoji,
//                R.drawable.ic_shock_emoij,
//                R.drawable.ic_swag_emoji
//        };
//        ReactionsConfig config = new ReactionsConfigBuilder(context)
//                .withReactions(reactions)
//                .build();
//
//        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
//            if (holder.getClass() == SendViewHolder.class){
//                SendViewHolder viewHolder = (SendViewHolder)holder;
//               try{
//                   viewHolder.feeling.setImageResource(reactions[pos]);
//                   viewHolder.feeling.setVisibility(View.VISIBLE);
//               }catch (Exception ignored) {
//
//               }
//            }
//            else {
//                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
//                try {
//                    viewHolder.feeling.setImageResource(reactions[pos]);
//                    viewHolder.feeling.setVisibility(View.VISIBLE);
//                }catch (Exception ignored){
//
//                }
//
//            }
//            message.setFeeling(pos);
//            FirebaseDatabase.getInstance().getReference()
//                    .child("chats")
//                    .child(senderRoom)
//                    .child(receiverRoom)
//                    .child("messages")
//                    .child(message.getMessageId()).setValue(message);
//            FirebaseDatabase.getInstance().getReference()
//                    .child("chats")
//                    .child(receiverRoom)
//                    .child(senderRoom)
//                    .child("messages")
//                    .child(message.getMessageId()).setValue(message);
//            return true; // true is closing popup, false is requesting a new selection
//        });
        if (holder.getClass() == SendViewHolder.class){
            SendViewHolder viewHolder = (SendViewHolder)holder;
            viewHolder.message_send.setText(message.getMessage());
            if (message.getMessage()!=null &&  message.getMessage().trim().equals("Image")){
                viewHolder.sendImage.setVisibility(View.VISIBLE);
                viewHolder.message_send.setVisibility(View.GONE);

                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.sendImage);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = new Date(Long.parseLong(message.getTimeStamp()));
            viewHolder.message_send_time.setText(sdf.format(date));
            if (position == messages.size()-1){
                if (message.isSeen()){
                    viewHolder.seen.setText("Seen");
                }else {
                    viewHolder.seen.setText("Delivered");
                }
            }else {
                viewHolder.seen.setVisibility(View.GONE);
            }
            //Set emoji's on message and images
//            if (message.getFeeling() >= 0){
//                viewHolder.feeling.setImageResource(reactions[message.getFeeling()]);
//                viewHolder.feeling.setVisibility(View.VISIBLE);
//            }else {
//                viewHolder.feeling.setVisibility(View.GONE);
//            }
//            viewHolder.message_send.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (tap_on_send){
//                        viewHolder.feeling.setVisibility(View.INVISIBLE);
//                        tap_on_send=false;
//                    }else {
//                        popup.onTouch(v, event);
//                        tap_on_send=true;
//                    }
//                    return false;
//                }
//            });
//            viewHolder.sendImage.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (tap_on_send){
//                        viewHolder.feeling.setVisibility(View.INVISIBLE);
//                        tap_on_send=false;
//                    }else {
//                        popup.onTouch(v, event);
//                        tap_on_send=true;
//                    }
//                    return false;
//                }
//            });

            viewHolder.message_send.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Message");
                    if (message.getMessage().equals("You deleted this message")){
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("chats")
                                        .child(senderRoom).child(receiverRoom).child("messages").child(message.getMessageKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid){
                                        Toast.makeText(context, "Cleaned", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }else {
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                message.setMessage("You deleted this message");
                                FirebaseDatabase.getInstance().getReference().child("chats")
                                        .child(senderRoom).child(receiverRoom).child("messages").child(message.getMessageKey()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        message.setMessage("This message was deleted");
                                        FirebaseDatabase.getInstance().getReference().child("chats")
                                                .child(receiverRoom).child(senderRoom).child("messages").child(message.getMessageKey()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                    builder.setNegativeButton("CANCEL",null);
                    builder.show();
                    return true;
                }
            });

        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.message_receive.setText(message.getMessage());
            if (message.getMessage()!=null && message.getMessage().trim().equals("Image")){
                viewHolder.receiveImage.setVisibility(View.VISIBLE);
                viewHolder.message_receive.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.receiveImage);
            }

            //Set emoji's on message and images
//            if (message.getFeeling() >= 0){
//                viewHolder.feeling.setImageResource(reactions[message.getFeeling()]);
//                viewHolder.feeling.setVisibility(View.VISIBLE);
//            }else {
//                viewHolder.feeling.setVisibility(View.GONE);
//            }
//            viewHolder.message_receive.setOnTouchListener((v, event) -> {
//                if (tap_on_receive) {
//                    viewHolder.feeling.setVisibility(View.INVISIBLE);
//                    tap_on_receive=false;
//                }else {
//                    popup.onTouch(v, event);
//                    tap_on_receive=true;
//                }
//                return false;
//            });

//            viewHolder.receiveImage.setOnTouchListener((v, event) -> {
//                if (tap_on_receive) {
//                    viewHolder.feeling.setVisibility(View.INVISIBLE);
//                    tap_on_receive=false;
//                }else {
//                    popup.onTouch(v, event);
//                    tap_on_receive=true;
//                }
//                return false;
//            });
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {
        ImageView feeling,sendImage;
        TextView message_send, message_send_time, seen;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            message_send = itemView.findViewById(R.id.message_send);
            message_send_time = itemView.findViewById(R.id.message_send_time);
            feeling = itemView.findViewById(R.id.feeling);
            seen = itemView.findViewById(R.id.seen);
            sendImage = itemView.findViewById(R.id.sendImage);
            sendImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, "Image Clicked adapter", Toast.LENGTH_SHORT).show();
                    if (itemClicked != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            itemClicked.onImageClicked(position, messages.get(position));
                        }
                    }

                }
            });
        }
    }
    public  class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ImageView feeling,receiveImage;
        TextView message_receive;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            message_receive = itemView.findViewById(R.id.message_receive);
            feeling = itemView.findViewById(R.id.feeling);
            receiveImage = itemView.findViewById(R.id.receiveImage);
            receiveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClicked != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            itemClicked.onImageClicked(position, messages.get(position));
                        }
                    }

                }
            });
        }
    }
    public interface itemClicked{
        void onImageClicked(int pos, Message message);
    }
}
