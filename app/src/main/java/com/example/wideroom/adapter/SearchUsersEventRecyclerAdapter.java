package com.example.wideroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.EventActivity;
import com.example.wideroom.R;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchUsersEventRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUsersEventRecyclerAdapter.UserModelViewHolder> {

    Context context;

    public SearchUsersEventRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(UserModelViewHolder holder, int position, @NonNull UserModel model) {
        Log.i("Información","entra en onBindViewHolder con user: " + model.getUsername());
        holder.usernameText.setText(model.getUsername());
        holder.bioText.setText(model.getBio());

        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        holder.sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.sendRequestBtn.setText("Requested");
                holder.sendRequestBtn.setEnabled(false);

                FirebaseUtil.sendFriendRequest(model.getUserId());
            }
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_users_event_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView bioText;
        ImageView profilePic;
        Button sendRequestBtn;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            bioText = itemView.findViewById(R.id.user_bio_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            sendRequestBtn = itemView.findViewById(R.id.send_request_btn);
        }
    }
}
