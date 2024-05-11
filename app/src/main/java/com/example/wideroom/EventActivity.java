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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.adapter.SearchUsersEventRecyclerAdapter;
import com.example.wideroom.model.EventModel;
import com.example.wideroom.model.EventSubscriptionModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
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
    Button searchForUsersBtn;
    MapView mapView;
    EventSubscriptionModel sub;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    SearchUsersEventRecyclerAdapter adapter;
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
        searchForUsersBtn = findViewById(R.id.search_users_btn);
        backBtn = findViewById(R.id.back_btn);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        progressBar = findViewById(R.id.event_progress_bar);
        recyclerView = findViewById(R.id.search_users_event_recycler_view);


        searchForUsersBtn.setVisibility(View.GONE);
        setInProgress(false);

        FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sub = task.getResult().toObject(EventSubscriptionModel.class);
                if(sub!=null && sub.isSubscribed()){
                    subscribeBtn.setText("Unsubscribe");
                    searchForUsersBtn.setVisibility(View.VISIBLE);
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
                        searchForUsersBtn.setVisibility(View.GONE);
                        searchForUsersBtn.setText("Send a new chat petition (x" + sub.getMessageInvitationQuantity() + ")");
                        setInProgress(false);
                    });
                }else{
                    sub.setSubscribed(true);
                    FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).set(sub).addOnCompleteListener(task -> {
                        subscribeBtn.setText("Unsubscribe");
                        searchForUsersBtn.setVisibility(View.VISIBLE);
                        setInProgress(false);
                    });
                }
            }
        });

        searchForUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForUsersBtn.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                setupRecyclerView();

            }
        });

        eventName.setText(eventModel.getEventName());
        address.setText(eventModel.getAddress() + ", " + eventModel.getCity() + "\n" + eventModel.getDistanceAsString());
        date.setText(eventModel.getDate());

    }

    void setupRecyclerView(){
        Log.i("Información","entra en el recycler");
        FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).whereEqualTo("subscribed",true)
                .get().addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        List<String> subscribers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            subscribers.add(document.getString("userId"));
                        }
                        Log.i("Información", "Número de suscriptores encontrados: " + subscribers.size()); // Para verificar cuántos suscriptores se han obtenido.
                            Query query=FirebaseUtil.allUserCollectionReference().whereIn("userId",subscribers);

                            Log.i("Información", "Query Firestore creada correctamente: " + query.toString()); // Para verificar la consulta Firestore creada correctamente.
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(query, UserModel.class).build();

                            adapter = new SearchUsersEventRecyclerAdapter(options, EventActivity.this);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            adapter.startListening();
                    }
                    Log.e("Error","Error al obtener datos de suscriptores");
                });

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
        if(adapter!=null){
            adapter.startListening();
        }
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