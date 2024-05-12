package com.example.wideroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.R;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FriendRequestRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, FriendRequestRecyclerAdapter.UserModelViewHolder> {

    Context context;

    public FriendRequestRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.usernameText.setText(model.getUsername());
        holder.bioText.setText(model.getBio());

        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }
                });

        holder.acceptButton.setOnClickListener(v -> {
           FirebaseUtil.acceptFriendRequest(model.getUserId());
           holder.itemView.setVisibility(View.GONE);
        });

        holder.declineButton.setOnClickListener(v -> {
            FirebaseUtil.removeFriend(model.getUserId());
            holder.itemView.setVisibility(View.GONE);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView bioText;
        ImageView profilePic;
        Button acceptButton;
        Button declineButton;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            bioText = itemView.findViewById(R.id.user_bio_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            acceptButton = itemView.findViewById(R.id.accept_btn);
            declineButton = itemView.findViewById(R.id.reject_btn);
        }
    }
}
