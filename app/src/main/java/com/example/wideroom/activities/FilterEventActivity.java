package com.example.wideroom.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wideroom.R;
import com.example.wideroom.adapters.EventRecyclerAdapter;
import com.example.wideroom.models.EventModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is used to show the user a screen that allows the user to filter all events near him or her.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class FilterEventActivity extends AppCompatActivity {
    final String[] categories = {"Music", "Sport", "Films"};
    final String[] orders = {"Distance ⬇", "Distance ⬆",  "Event name ⬇" , "Event name ⬆"};
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    SeekBar distanceBar;
    TextView distanceTextView;
    Spinner categorySpinner;
    Spinner orderSpinner;
    EventRecyclerAdapter adapter;
    // variable para almacenar la categoría seleccionada
    String selectedCategory = "Music"; // Categoría predeterminada

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
        setContentView(R.layout.activity_filter_event);
        searchInput=findViewById(R.id.search_event_input);
        searchButton=findViewById(R.id.search_event_btn);
        backButton=findViewById(R.id.back_btn);
        recyclerView=findViewById(R.id.search_event_recycler_view);
        distanceBar=findViewById(R.id.distance_bar);
        categorySpinner=findViewById(R.id.category_spinner);
        distanceTextView=findViewById(R.id.distance_quantity_text);
        orderSpinner=findViewById(R.id.order_spinner);
        searchInput.requestFocus();
        double[] coordinates = getIntent().getExtras().getDoubleArray("coordinates");
        final int[] distance = {100};
        final String[] order = {"Distance ⬇"};
        // creates an adapter for the category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        // creates an adapter for the order spinner
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orders);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);
        // controls the distance bar
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceTextView.setText(String.valueOf(progress) + " Km");
                distance[0] = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        // controls the selection of an item of category
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // controls the selection of an item of order
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                order[0] = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // finishes this activity
        backButton.setOnClickListener(v -> {
            finish();
        });
        //listener of the search button the refreshes according to selected characteristics
        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if(!searchTerm.isEmpty() && searchTerm.length() < 3){
                searchInput.setError(getResources().getString(R.string.invalid_eventname));
                return;
            }
            setupEventSearchRecyclerView(coordinates, selectedCategory, distance[0], order[0] ,searchTerm);
        });
    }

    /**
     * Establishes the recycler view for the events that check the chracteristics
     * @param coordinates
     * @param category
     * @param radiusInKm
     * @param order
     * @param searchTerm
     */
    void setupEventSearchRecyclerView(double[] coordinates, String category, int radiusInKm, String order, String searchTerm) {
        Log.i("GeoFire", "User coordinates: " + Arrays.toString(coordinates));
        final GeoLocation userLocation = new GeoLocation(coordinates[0], coordinates[1]);
        double radiusInM = radiusInKm * 1000;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(userLocation, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = FirebaseUtil.allEventsCollectionReference()
                    .whereEqualTo("category", category) // Filtrar por categoría
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        //list of events
                        List<EventModel> events = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");
                                // filters events using the radio
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, userLocation);
                                if (distanceInM <= radiusInM) {
                                    EventModel event = doc.toObject(EventModel.class);
                                    event.setDistanceInM(distanceInM);
                                    // adds the event to the list
                                    events.add(event); 
                                }
                            }
                        }
                        for(int i = 0; i < events.size(); i++){
                            if(!events.get(i).getEventName().toLowerCase().contains(searchTerm.toLowerCase())){
                                events.remove(events.get(i));
                            }
                        }
                        if (!events.isEmpty()) { 
                            // order the list depending on the order selected
                            switch (order){
                                case "Distance ⬇":
                                    Collections.sort(events, Comparator.comparingDouble(EventModel::getDistanceInM));
                                    break;
                                case "Distance ⬆":
                                    Collections.sort(events, Comparator.comparingDouble(EventModel::getDistanceInM).reversed());
                                    break;
                                case "Event name ⬇":
                                    Collections.sort(events, Comparator.comparing(EventModel::getEventName));
                                    break;
                                case "Event name ⬆":
                                    Collections.sort(events, Comparator.comparing(EventModel::getEventName).reversed());
                                    break;
                            }

                            // show the evnets using recycler rows
                            adapter = new EventRecyclerAdapter(FilterEventActivity.this, events);
                            recyclerView.setLayoutManager(new LinearLayoutManager(FilterEventActivity.this));
                            recyclerView.setAdapter(adapter);
                        } else { 
                            //shows a message if no events are found
                            Toast.makeText(FilterEventActivity.this, getResources().getString(R.string.no_events_category), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}