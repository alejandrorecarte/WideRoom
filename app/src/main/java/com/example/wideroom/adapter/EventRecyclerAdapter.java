package com.example.wideroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.ChatActivity;
import com.example.wideroom.EventActivity;
import com.example.wideroom.R;
import com.example.wideroom.model.EventModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class EventRecyclerAdapter extends FirestoreRecyclerAdapter<EventModel, EventRecyclerAdapter.EventModelViewHolder> {

    Context context;

    public EventRecyclerAdapter(@NonNull FirestoreRecyclerOptions<EventModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(EventModelViewHolder holder, int position, @NonNull EventModel model) {
        FirebaseUtil.getEventReference(model.getEventId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    EventModel eventModel=task.getResult().toObject(EventModel.class);

                    holder.eventNameText.setText(eventModel.getEventName());
                    holder.dateText.setText(eventModel.getDate());
                    holder.cityText.setText(eventModel.getCity());


                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, EventActivity.class);
                        AndroidUtil.passEventModelAsIntent(intent, model);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    });

                }
            }
        });


    }

    @NonNull
    @Override
    public EventModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_recycler_row, parent, false);
        return new EventModelViewHolder(view);
    }

    class EventModelViewHolder extends RecyclerView.ViewHolder{
        TextView eventNameText;
        TextView dateText;
        TextView cityText;

        public EventModelViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameText = itemView.findViewById(R.id.event_name_text);
            dateText = itemView.findViewById(R.id.date_text);
            cityText = itemView.findViewById(R.id.city_text);

        }
    }
}
