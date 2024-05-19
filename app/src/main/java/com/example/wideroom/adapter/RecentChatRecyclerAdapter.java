package com.example.wideroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.ChatActivity;
import com.example.wideroom.R;
import com.example.wideroom.model.ChatroomModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            boolean lastMessageSendByMe=model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                            UserModel otherUserModel=task.getResult().toObject(UserModel.class);

                            FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                    .addOnCompleteListener(t -> {
                                        if(t.isSuccessful()){
                                            Uri uri = t.getResult();
                                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                        }else{
                                            Log.i("FireUtil Info","Other user profile pic not found");
                                            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.person_icon);
                                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                        }
                                    });

                            holder.usernameText.setText(otherUserModel.getUsername());

                            FirebaseUtil.currentUserDetails().get().addOnCompleteListener(t -> {
                                if(t.isSuccessful()){
                                    UserModel currentUser = t.getResult().toObject(UserModel.class);
                                    if(otherUserModel.getUsername().equals(currentUser.getUsername())){
                                        holder.usernameText.setText(holder.usernameText.getText() + " (Me)");
                                    }
                                }
                            });

                            if(lastMessageSendByMe){
                                holder.lastMessageText.setText("You : "+model.getLastMessage());
                            }
                            else{
                                AggregateQuery aq = FirebaseUtil.getChatroomMessageReference(model.getChatroomId())
                                        .whereEqualTo("senderId", otherUserModel.getUserId())
                                        .whereEqualTo("read", false)
                                        .count();

                                aq.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            TypedValue typedValue = new TypedValue();
                                            context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                                            int color = ContextCompat.getColor(context, typedValue.resourceId);
                                            AggregateQuerySnapshot snapshot = task.getResult();
                                            if(snapshot.getCount() > 0){
                                                holder.lastMessageText.setTypeface(null, Typeface.BOLD);
                                                holder.lastMessageTime.setTypeface(null, Typeface.BOLD);
                                                holder.lastMessageTime.setTextColor(color);
                                                holder.nonReadMessagesCountLayout.setVisibility(View.VISIBLE);
                                                holder.nonReadMessagesCount.setText(String.valueOf(snapshot.getCount()));
                                            }
                                            Log.d("Firebase Info", "Count: " + snapshot.getCount());
                                        }
                                    }
                                });

                                String lastMessage = model.getLastMessage();
                                if(lastMessage.length() > 80){
                                    lastMessage = lastMessage.substring(0,80)+"...";
                                }

                                holder.lastMessageText.setText(lastMessage);

                            }

                            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            });
                        }
                    }
                });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;
        TextView nonReadMessagesCount;
        RelativeLayout nonReadMessagesCountLayout;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            nonReadMessagesCount = itemView.findViewById(R.id.non_read_messages_count);
            nonReadMessagesCountLayout = itemView.findViewById(R.id.non_read_messages_layout);
            nonReadMessagesCountLayout.setVisibility(View.GONE);
        }
    }
}