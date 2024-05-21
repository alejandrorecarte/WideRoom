package com.example.wideroom.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

public class EventFragment extends Fragment {


    RecyclerView recyclerView;
    EventRecyclerAdapter adapter;
    String eventId;
    EventModel eventModel;
    double[] coordinates;
    public EventFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_event, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        setupRecyclerView();
        return view;
    }

    void setupRecyclerView() {

        coordinates = getArguments().getDoubleArray("coordinates");
        Log.i("GeoFire", "User coordinates: " + Arrays.toString(coordinates));
        coordinates = new double[]{40.32168011549933, -3.8684653512644993};
        if (coordinates != null) {
            final GeoLocation userLocation = new GeoLocation(coordinates[0], coordinates[1]);
            double radiusInKm = 100;
            double radiusInM = radiusInKm * 1000;
            List<EventModel> events = new ArrayList<>();
            List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(userLocation, radiusInM);
            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            for (GeoQueryBounds b : bounds) {
                Query q = FirebaseUtil.allEventsCollectionReference()
                        .orderBy("geohash")
                        .startAt(b.startHash)
                        .endAt(b.endHash);

                tasks.add(q.get());
            }

            Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Task<?>>> t) {
                            List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                            for (Task<QuerySnapshot> task : tasks) {
                                QuerySnapshot snap = task.getResult();
                                for (DocumentSnapshot doc : snap.getDocuments()) {
                                    double lat = doc.getDouble("lat");
                                    double lng = doc.getDouble("lng");

                                    // We have to filter out a few false positives due to GeoHash
                                    // accuracy, but most will match
                                    GeoLocation docLocation = new GeoLocation(lat, lng);
                                    double distanceInM = -1;
                                    distanceInM = GeoFireUtils.getDistanceBetween(docLocation, userLocation);
                                    if (distanceInM <= radiusInM) {
                                        EventModel event = doc.toObject(EventModel.class);
                                        if (distanceInM != -1) {
                                            event.setDistanceInM(distanceInM);
                                        }
                                        events.add(event);
                                    }
                                }
                            }

                            Collections.sort(events, Comparator.comparingDouble(EventModel::getDistanceInM));
                            adapter = new EventRecyclerAdapter(getContext(), events);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(adapter);
                        }
                    });
        }
    }
}