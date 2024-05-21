package com.example.wideroom.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.activities.EventActivity;
import com.example.wideroom.R;
import com.example.wideroom.models.EventModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;

import java.util.List;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventModelViewHolder> {

    private Context context;
    private List<EventModel> eventsList;

    public EventRecyclerAdapter(Context context, List<EventModel> eventsList) {
        this.context = context;
        this.eventsList = eventsList;
    }

    @NonNull
    @Override
    public EventModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_recycler_row, parent, false);
        return new EventModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventModelViewHolder holder, int position) {
        EventModel eventModel = eventsList.get(position);
        holder.eventNameText.setText(eventModel.getEventName());
        holder.dateText.setText(eventModel.getDate());
        holder.cityText.setText(eventModel.getCity());
        holder.distanceText.setText(eventModel.getDistanceAsString());
        holder.addressText.setText(eventModel.getAddress());

        FirebaseUtil.getEventPicIconStorageRef(eventModel.getEventId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setEventPic(context, uri, holder.eventPic);
                    }else{
                        Log.i("FireUtil Info","Event pic not found");
                        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.event_icon);
                        AndroidUtil.setEventPic(context, uri, holder.eventPic);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventActivity.class);
            AndroidUtil.passEventModelAsIntent(intent, eventModel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class EventModelViewHolder extends RecyclerView.ViewHolder{
        TextView eventNameText;
        TextView dateText;
        TextView cityText;
        TextView addressText;
        TextView distanceText;
        ImageView eventPic;

        public EventModelViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameText = itemView.findViewById(R.id.event_name_text);
            dateText = itemView.findViewById(R.id.date_text);
            cityText = itemView.findViewById(R.id.city_text);
            addressText = itemView.findViewById(R.id.address_text);
            distanceText = itemView.findViewById(R.id.distance_text);
            eventPic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}