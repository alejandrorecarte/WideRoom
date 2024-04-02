package com.example.wideroom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.model.EventModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;

import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    EventModel eventModel;
    TextView eventName;
    ImageButton backBtn;
    ImageView eventBackgroundPic;
    TextView address;
    TextView date;
    Button subscribeBtn;
    Button searchBtn;
    ImageButton locationBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        eventModel = AndroidUtil.getEventModelFromIntent(getIntent());
        eventName = findViewById(R.id.event_name);
        eventBackgroundPic = findViewById(R.id.event_background_pic);
        address = findViewById(R.id.address);
        date = findViewById(R.id.date);
        subscribeBtn = findViewById(R.id.subscribe_btn);
        searchBtn = findViewById(R.id.search_users_btn);
        backBtn = findViewById(R.id.back_btn);
        locationBtn = findViewById(R.id.location_btn);

        try {
            FirebaseUtil.getEventPicIconStorageRef(eventModel.getEventId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setEventPic(this, uri, eventBackgroundPic);
                        }
                    });
        }catch(Exception e){}

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", eventModel.getLat(), eventModel.getLng());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                v.getContext().startActivity(intent);
            }
        });

        eventName.setText(eventModel.getEventName());
        address.setText(eventModel.getAddress() + ", " + eventModel.getCity() + "\n" + eventModel.getDistanceAsString());
        date.setText(eventModel.getDate());

    }
}