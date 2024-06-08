package com.example.wideroom.activities;

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
import com.example.wideroom.R;
import com.example.wideroom.adapters.SearchUsersEventRecyclerAdapter;
import com.example.wideroom.models.EventModel;
import com.example.wideroom.models.EventSubscriptionModel;
import com.example.wideroom.models.UserModel;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to show an event information.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class EventActivity extends AppCompatActivity implements OnMapReadyCallback {

    EventModel eventModel;
    TextView eventName;
    ImageButton backBtn;
    ImageView eventBackgroundPic;
    TextView address;
    TextView date;
    TextView noResults;
    Button subscribeBtn;
    Button searchForUsersBtn;
    MapView mapView;
    EventSubscriptionModel sub;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    SearchUsersEventRecyclerAdapter adapter;
    private GoogleMap googleMap;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        eventModel = AndroidUtil.getEventModelFromIntent(getIntent());
        noResults = findViewById(R.id.no_results_text);
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
        progressBar = findViewById(R.id.register_progress_bar);
        recyclerView = findViewById(R.id.search_users_event_recycler_view);
        searchForUsersBtn.setVisibility(View.GONE);
        setInProgress(false);
        FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sub = task.getResult().toObject(EventSubscriptionModel.class);
                if(sub!=null && sub.isSubscribed()){
                    subscribeBtn.setText(getResources().getString(R.string.unsuscribe));
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
        // listener de el botón de suscribir. Verifica si estás o no suscrito 
        // cambia el mensaje según. Establece a true o false el campo de suscrito
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInProgress(true);
                if(sub == null){
                    Log.i("EventActivity", "Subscribing");
                    sub = new EventSubscriptionModel(FirebaseUtil.currentUserId(),Timestamp.now());
                    FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).document(FirebaseUtil.currentUserId()).set(sub);
                }
                    //subscribed
                if(sub.isSubscribed()){
                    sub.setSubscribed(false);
                    FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).set(sub).addOnCompleteListener(task -> {
                        subscribeBtn.setText(getResources().getString(R.string.suscribe));
                        searchForUsersBtn.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        setInProgress(false);
                    });
                    //not subscribed
                }else{
                    sub.setSubscribed(true);
                    FirebaseUtil.getEventsSubscriberReference(eventModel.getEventId(), FirebaseUtil.currentUserId()).set(sub).addOnCompleteListener(task -> {
                        subscribeBtn.setText(getResources().getString(R.string.unsuscribe));
                        searchForUsersBtn.setVisibility(View.VISIBLE);
                        setInProgress(false);
                    });
                }
            }
        });
        // listener del botón de buscar usuarios y muestra el recycler view
        searchForUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForUsersBtn.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                setupRecyclerView();
            }
        });
        // recupera los datos del evento para mostrarlos
        eventName.setText(eventModel.getEventName());
        address.setText(eventModel.getAddress() + ", " + eventModel.getCity() + "\n" + eventModel.getDistanceAsString());
        date.setText(eventModel.getDate());
    }

    /**
     * Establishes the data of the recycler view of the people subscribed to the event.
     * Does not show the friends subscribed or the ones that has requested friendship
     */
    void setupRecyclerView(){
        Log.i("Información","entra en el recycler");
        FirebaseUtil.allOwnFriendsReference()
                .get().addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        List<String> subscribedFriends = new ArrayList<>();
                        for(QueryDocumentSnapshot document : t.getResult()){
                            subscribedFriends.add(document.getString("userId"));
                            Log.i("Información", document.getId() + " => " + document.getString("userId"));
                        }
                        FirebaseUtil.allEventSubscribersReference(eventModel.getEventId()).whereEqualTo("subscribed", true)
                                .get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        List<String> subscribers = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(!document.getString("userId").equals(FirebaseUtil.currentUserId()) && !subscribedFriends.contains(document.getString("userId"))){
                                                subscribers.add(document.getString("userId"));
                                            }
                                        }
                                        Log.i("Información", "Número de suscriptores encontrados: " + subscribers.size()); // Para verificar cuántos suscriptores se han obtenido.
                                        if (subscribers.size() != 0) {
                                            Query query = FirebaseUtil.allUserCollectionReference().whereIn("userId", subscribers);
                                            Log.i("Información", "Query Firestore creada correctamente: " + query.toString()); // Para verificar la consulta Firestore creada correctamente.
                                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                                    .setQuery(query, UserModel.class).build();
                                            adapter = new SearchUsersEventRecyclerAdapter(options, EventActivity.this);
                                            recyclerView.setAdapter(adapter);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                            adapter.startListening();
                                        }else{
                                            noResults.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    }
                });
    }

    /**
     * Shows or hides the progressBar
     * @param inProgress
     */
    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            subscribeBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            subscribeBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the map with the location
     * @param map
     */
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng COORDINATES = new LatLng(eventModel.getLat(), eventModel.getLng());
        // adds a mark on the given coordinates
        googleMap.addMarker(new MarkerOptions()
                .position(COORDINATES)
                .title(eventModel.getAddress())
                .snippet(eventModel.getCity()));
        // moves the camera to the given coordinates with a zoom of 16
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COORDINATES, 16));
    }

    /**
     * on Resume,, adapter starts listening
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    /**
     * Pauses the map and the activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Destroys the map and activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * Reduces the memory on use
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}