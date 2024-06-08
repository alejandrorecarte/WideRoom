package com.example.wideroom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wideroom.R;
import com.example.wideroom.models.ChatMessageModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/**
 * This class is used to show every chat message that the user has sent or received with other friend.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;

    /**
     * Parametrized constructor.
     * @param options
     * @param context
     */
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
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
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        ChatMessageModel previousMessage = null;
        if (position > 0) {
            previousMessage = getItem(position - 1);
        }
        configureChatLayout(model, previousMessage, holder);
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return chatModelViewHolder
     */
    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    /**
     * Establishes the relationship between the recycler view java items and layout
     */
    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextView,rightChatTextView, leftChatTextViewTimestamp, rightChatTextViewTimestamp;
        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout=itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout=itemView.findViewById(R.id.right_chat_layout);
            leftChatTextView=itemView.findViewById(R.id.left_chat_textview);
            leftChatTextViewTimestamp=itemView.findViewById(R.id.left_chat_textview_timestamp);
            rightChatTextView=itemView.findViewById(R.id.right_chat_textview);
            rightChatTextViewTimestamp=itemView.findViewById(R.id.right_chat_textview_timestamp);
        }
    }

    /**
     * Configures the chat Layout
     * @param model
     * @param previousMessage
     * @param holder
     */
    private void configureChatLayout(ChatMessageModel model, ChatMessageModel previousMessage, ChatModelViewHolder holder) {
        String timestamp = model.getTimestamp().toDate().getDate() + "/" +
                model.getTimestamp().toDate().getMonth() + " " +
                FirebaseUtil.timestampToString(model.getTimestamp());
        boolean isCurrentUser = model.getSenderId().equals(FirebaseUtil.currentUserId());
        View chatLayout = isCurrentUser ? holder.rightChatLayout : holder.leftChatLayout;
        TextView chatTextView = isCurrentUser ? holder.rightChatTextView : holder.leftChatTextView;
        TextView chatTimestampTextView = isCurrentUser ? holder.rightChatTextViewTimestamp : holder.leftChatTextViewTimestamp;
        chatLayout.setVisibility(View.VISIBLE);
        chatTextView.setText(model.getMessage());
        boolean showTimestamp = previousMessage == null ||
                !FirebaseUtil.timestampToString(previousMessage.getTimestamp()).equals(FirebaseUtil.timestampToString(model.getTimestamp())) ||
                !previousMessage.getSenderId().equals(model.getSenderId());
        if (showTimestamp) {
            chatTimestampTextView.setText(timestamp);
            chatTimestampTextView.setVisibility(View.VISIBLE);
        } else {
            chatTimestampTextView.setVisibility(View.GONE);
        }
        if (previousMessage != null && !previousMessage.getSenderId().equals(model.getSenderId())) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatLayout.getLayoutParams();
            params.setMargins(0, 0, 0, 20); // Adjusts margin top
            chatLayout.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatLayout.getLayoutParams();
            params.setMargins(0, 0, 0, 0); // Restablish margins
            chatLayout.setLayoutParams(params);
        }
        // hides the opposite design of the chat
        if (isCurrentUser) {
            holder.leftChatLayout.setVisibility(View.GONE);
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
        }
    }
}