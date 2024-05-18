package com.example.wideroom;

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
import com.example.wideroom.adapter.EventRecyclerAdapter;
import com.example.wideroom.model.EventModel;
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

    // Nueva variable para almacenar la categoría seleccionada
    String selectedCategory = "Music"; // Categoría predeterminada

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

        // Crear un adaptador para el Spinner de categoría
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Crear un adaptador para el Spinner de orden
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orders);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);

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

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                order[0] = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if(!searchTerm.isEmpty() && searchTerm.length() < 3){
                searchInput.setError("Invalid event name");
                return;
            }
            setupEventSearchRecyclerView(coordinates, selectedCategory, distance[0], order[0] ,searchTerm);
        });
    }

    void setupEventSearchRecyclerView(double[] coordinates, String category, int radiusInKm, String order, String searchTerm) {
        Log.i("GeoFire", "User coordinates: " + Arrays.toString(coordinates));
        final GeoLocation userLocation = new GeoLocation(coordinates[0], coordinates[1]);
        double radiusInM = radiusInKm * 1000;

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(userLocation, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = FirebaseUtil.allEventsCollectionReference()
                    .whereEqualTo("category", category) ;// Filtrar por categoría
                    /*.orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

                     */

            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();
                        List<EventModel> events = new ArrayList<>(); // Lista de eventos

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");

                                // Filtrar eventos dentro del radio
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, userLocation);
                                if (distanceInM <= radiusInM) {
                                    EventModel event = doc.toObject(EventModel.class);
                                    event.setDistanceInM(distanceInM);
                                    events.add(event); // Agregar evento a la lista
                                }
                            }
                        }

                        if (!events.isEmpty()) { // Si hay eventos disponibles
                            // Ordenar la lista de eventos según el criterio seleccionado
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

                            // Mostrar los eventos en el RecyclerView
                            adapter = new EventRecyclerAdapter(FilterEventActivity.this, events);
                            recyclerView.setLayoutManager(new LinearLayoutManager(FilterEventActivity.this));
                            recyclerView.setAdapter(adapter);
                        } else { // Si no hay eventos disponibles
                            // Mostrar un mensaje indicando que no hay eventos en esta categoría
                            Toast.makeText(FilterEventActivity.this, getResources().getString(R.string.no_events_category), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}