package com.example.wideroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.ChatActivity;
import com.example.wideroom.R;
import com.example.wideroom.model.ChatMessageModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        ChatMessageModel previousMessage = null;
        if(position > 0) {
            previousMessage = getItem(position - 1);
        }
        String timestamp = model.getTimestamp().toDate().getDate() + "/" +
                model.getTimestamp().toDate().getMonth() + " " +
                FirebaseUtil.timestampToString(model.getTimestamp());

            if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                holder.leftChatLayout.setVisibility(View.GONE);
                holder.rightChatLayout.setVisibility(View.VISIBLE);
                holder.rightChatTextView.setText(model.getMessage());
                if (previousMessage!=null && (!FirebaseUtil.timestampToString(previousMessage.getTimestamp()).equals(FirebaseUtil.timestampToString(model.getTimestamp()))
                        || !previousMessage.getSenderId().equals(model.getSenderId()))) {
                    holder.rightChatTextViewTimestamp.setText(timestamp);
                } else if(previousMessage==null) {
                    holder.rightChatTextViewTimestamp.setText(timestamp);
                } else {
                    holder.rightChatTextViewTimestamp.setVisibility(View.GONE);
                }
                if (previousMessage!=null && (!previousMessage.getSenderId().equals(model.getSenderId()))) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.rightChatLayout.getLayoutParams();
                    params.setMargins(0, 0, 0, 20); // Ajusta el margen superior según sea necesario
                    holder.rightChatLayout.setLayoutParams(params);
                }

            } else {
                holder.rightChatLayout.setVisibility(View.GONE);
                holder.leftChatLayout.setVisibility(View.VISIBLE);
                holder.leftChatTextView.setText(model.getMessage());
                if (previousMessage!=null && (!FirebaseUtil.timestampToString(previousMessage.getTimestamp()).equals(FirebaseUtil.timestampToString(model.getTimestamp()))
                        || !previousMessage.getSenderId().equals(model.getSenderId()))) {
                    holder.leftChatTextViewTimestamp.setText(timestamp);
                } else if(previousMessage==null) {
                    holder.leftChatTextViewTimestamp.setText(timestamp);
                }else{
                    holder.leftChatTextViewTimestamp.setVisibility(View.GONE);
                }
                if (previousMessage!=null && (!previousMessage.getSenderId().equals(model.getSenderId()))) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.leftChatLayout.getLayoutParams();
                    params.setMargins(0, 0, 0, 20); // Ajusta el margen superior según sea necesario
                    holder.leftChatLayout.setLayoutParams(params);
                }
            }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

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
}
