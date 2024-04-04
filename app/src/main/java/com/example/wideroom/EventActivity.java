package com.example.wideroom;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.model.EventModel;
import com.example.wideroom.model.EventSubscriptionModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class EventActivity extends AppCompatActivity implements OnMapReadyCallback {

    EventModel eventModel;
    TextView eventName;
    ImageButton backBtn;
    ImageView eventBackgroundPic;
    TextView address;
    TextView date;
    Button subscribeBtn;
    Button sendChatPetitionBtn;
    MapView mapView;
    EventSubscriptionModel sub;
    ProgressBar progressBar;
    private GoogleMap googleMap;


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
        sendChatPetitionBtn = findViewById(R.id.search_users_btn);
        backBtn = findViewById(R.id.back_btn);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        progressBar = findViewById(R.id.event_progress_bar);


        sendChatPetitionBtn.setVisibility(View.GONE);
        setInProgress(false);

        FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sub = task.getResult().toObject(EventSubscriptionModel.class);
                if(sub!=null && sub.isSubscribed()){
                    subscribeBtn.setText("Unsubscribe");
                    sendChatPetitionBtn.setVisibility(View.VISIBLE);
                }
            }
        });

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

        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInProgress(true);
                if(sub == null){
                    Log.i("EventActivity", "Subscribing");
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;
                    sub = new EventSubscriptionModel(eventModel.getEventId(),new Timestamp(currentTimeSeconds, 0));
                    FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).document(FirebaseUtil.currentUserId()).set(sub);
                }
                if(sub.isSubscribed()){
                    sub.setSubscribed(false);
                    FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).set(sub).addOnCompleteListener(task -> {
                        subscribeBtn.setText("Subscribe");
                        sendChatPetitionBtn.setVisibility(View.GONE);
                        sendChatPetitionBtn.setText("Send a new chat petition (x" + sub.getMessageInvitationQuantity() + ")");
                        setInProgress(false);
                    });
                }else{
                    sub.setSubscribed(true);
                    FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).set(sub).addOnCompleteListener(task -> {
                        subscribeBtn.setText("Unsubscribe");
                        sendChatPetitionBtn.setVisibility(View.VISIBLE);
                        setInProgress(false);
                    });
                }
            }
        });

        sendChatPetitionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sub.getMessageInvitationQuantity() > 0) {
                    FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        int totalDocuments = queryDocumentSnapshots.size();

                        Random random = new Random();
                        int randomIndex = random.nextInt(totalDocuments);

                        AtomicInteger petitionsSentTries = new AtomicInteger(5);
                        while (petitionsSentTries.get() > 0) {
                            Log.i("SendChatPetitionBtn", "Entered the while");
                            FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).orderBy("subTimestamp").limit(randomIndex + 1).get().addOnSuccessListener(queryDocumentSnapshots1 -> {

                            if (!queryDocumentSnapshots1.isEmpty()) {
                                DocumentSnapshot randomDocument = queryDocumentSnapshots1.getDocuments().get(randomIndex);
                                //String id = randomDocument.getId();
                                EventSubscriptionModel sub = randomDocument.toObject(EventSubscriptionModel.class);
                                if (!sub.getUserId().equals(FirebaseUtil.currentUserId()) && sub.isSubscribed()) {
                                    AndroidUtil.showToast(v.getContext(), "Message petition sent to ." + sub.getUserId());
                                    sub.setMessageInvitationQuantity(sub.getMessageInvitationQuantity() - 1);
                                    sendChatPetitionBtn.setText("Send a new chat petition (x" + sub.getMessageInvitationQuantity() + ")");
                                    FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).document(FirebaseUtil.currentUserId()).set(sub);
                                    petitionsSentTries.set(0);
                                }else{
                                }
                            } else {
                                AndroidUtil.showToast(v.getContext(), "Not subscribed users found.");
                            }
                            });
                            petitionsSentTries.getAndDecrement();
                        }
                    });
                }else{
                    AndroidUtil.showToast(v.getContext(), "You have no more chat petition to send");
                }
            }
        });

        eventName.setText(eventModel.getEventName());
        address.setText(eventModel.getAddress() + ", " + eventModel.getCity() + "\n" + eventModel.getDistanceAsString());
        date.setText(eventModel.getDate());

    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            subscribeBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            subscribeBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng COORDINATES = new LatLng(eventModel.getLat(), eventModel.getLng());
        // Añadir marcador en las coordenadas especificadas
        googleMap.addMarker(new MarkerOptions()
                .position(COORDINATES)
                .title(eventModel.getAddress())
                .snippet(eventModel.getCity()));

        // Mover la cámara a las coordenadas especificadas y ajustar el zoom
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COORDINATES, 12));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}