package com.example.letter.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letter.Activities.HomeActivity;
import com.example.letter.Models.Status;
import com.example.letter.Models.UserStatus;
import com.example.letter.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder> {
    private Context context;
    private ArrayList<UserStatus> userStatuses;
    private String randomKey;
    public TopStatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);

        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() -1);
        Glide.with(context).load(lastStatus.getImage()).into(holder.circleImageView);
        holder.circularStatusView.setPortionsCount(userStatus.getStatuses().size());

        holder.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();
                for (Status status:userStatus.getStatuses()){
                    Log.d("Vinay", "topStatusAdapter"+ status.getImage());
                    myStories.add(new MyStory(status.getImage()+""));
                }
                new StoryView.Builder(((HomeActivity) context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(userStatus.getName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder{
        com.devlomi.circularstatusview.CircularStatusView circularStatusView;
        CircleImageView circleImageView;
        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            circularStatusView = itemView.findViewById(R.id.circular_status_view);
            circleImageView = itemView.findViewById(R.id.lastImage);
        }
    }
}
