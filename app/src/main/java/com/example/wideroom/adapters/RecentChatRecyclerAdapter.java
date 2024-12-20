package com.example.wideroom.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wideroom.activities.ChatActivity;
import com.example.wideroom.R;
import com.example.wideroom.models.ChatroomModel;
import com.example.wideroom.models.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * This class is used to show every recent chatroom that the current user has.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    /**
     * Parametrized constructor.
     * @param options
     * @param context
     */
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param holder
     * @param position
     * @param model
     */
    @Override
    protected void onBindViewHolder(ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());
                            UserModel otherUserModel = task.getResult().toObject(UserModel.class);
                            FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            Uri uri = t.getResult();
                                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                        } else {
                                            Log.i("FireUtil Info", "Other user profile pic not found");
                                            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.person_icon);
                                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                        }
                                    });
                            holder.usernameText.setText(otherUserModel.getUsername());
                            if (lastMessageSendByMe) {
                                holder.lastMessageText.setText("You : " + model.getLastMessage());
                            } else {
                                // Obtener el recuento de mensajes no leídos
                                FirebaseUtil.getChatroomMessageReference(model.getChatroomId())
                                        .whereEqualTo("senderId", otherUserModel.getUserId())
                                        .whereEqualTo("read", false)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot querySnapshot = task.getResult();
                                                    int unreadMessagesCount = querySnapshot.size();
                                                    if (unreadMessagesCount > 0) {
                                                        // Mostrar el recuento de mensajes no leídos y formatear en negrita
                                                        holder.lastMessageText.setText(model.getLastMessage());
                                                        holder.lastMessageText.setTypeface(null, Typeface.BOLD);
                                                        holder.nonReadMessagesCountLayout.setVisibility(View.VISIBLE);
                                                        holder.nonReadMessagesCount.setText(String.valueOf(unreadMessagesCount));
                                                    } else {
                                                        // Si no hay mensajes no leídos, mostrar el último mensaje normalmente
                                                        holder.lastMessageText.setText(model.getLastMessage());
                                                        holder.lastMessageText.setTypeface(null, Typeface.NORMAL);
                                                        holder.nonReadMessagesCountLayout.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    Log.e("Firestore Error", "Error getting unread messages count: ", task.getException());
                                                }
                                            }
                                        });
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

    /**
     * 
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    /**
     * Establishes the relationship between the recycler view java items and layout
     */
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