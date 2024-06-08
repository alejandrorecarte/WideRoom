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

/**
 * This class is used to show every event.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventModelViewHolder> {

    private Context context;
    private List<EventModel> eventsList;

    /**
     * Parametrized constructor.
     * @param context
     * @param eventsList
     */
    public EventRecyclerAdapter(Context context, List<EventModel> eventsList) {
        this.context = context;
        this.eventsList = eventsList;
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public EventModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_recycler_row, parent, false);
        return new EventModelViewHolder(view);
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Gets the count of the events
     * @return
     */
    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    /**
     *
    * Establishes the relationship between the recycler view java items and layout
     */
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