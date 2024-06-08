package com.example.wideroom.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.R;
import com.example.wideroom.fragments.ChatFragment;
import com.example.wideroom.fragments.EventFragment;
import com.example.wideroom.fragments.FriendRequestFragment;
import com.example.wideroom.fragments.ProfileFragment;
import com.example.wideroom.utils.FirebaseUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.onesignal.OneSignal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;
    ImageButton filterButton;
    FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1002;
    private static final String ONESIGNAL_APP_ID = "27100f8e-6316-478b-8ba0-a8157f66495b";
    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    EventFragment eventsFragment;
    FriendRequestFragment friendRequestFragment;
    double[] coordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();
        eventsFragment = new EventFragment();
        friendRequestFragment= new FriendRequestFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);
        filterButton = findViewById(R.id.main_filter_btn);

        OneSignal.getNotifications().clearAllNotifications();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();


        searchButton.setOnClickListener((v) -> {

            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        filterButton.setOnClickListener((v) -> {
            Intent intent = new Intent(MainActivity.this, FilterEventActivity.class);
            intent.putExtra("coordinates", coordinates);
            startActivity(intent);

        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_chat) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                    searchButton.setVisibility(View.VISIBLE);
                    filterButton.setVisibility(View.GONE);
                }
                if (item.getItemId() == R.id.menu_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                    searchButton.setVisibility(View.GONE);
                    filterButton.setVisibility(View.GONE);
                }
                if (item.getItemId() == R.id.menu_event) {
                    Bundle args = new Bundle();
                    args.putSerializable("coordinates", coordinates);
                    eventsFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, eventsFragment).commit();
                    searchButton.setVisibility(View.GONE);
                    filterButton.setVisibility(View.VISIBLE);

                }
                if (item.getItemId() == R.id.menu_friend_request) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, friendRequestFragment).commit();
                    searchButton.setVisibility(View.GONE);
                    filterButton.setVisibility(View.GONE);
                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.menu_chat);

        String oneSignalId = OneSignal.getUser().getOnesignalId();
        Log.i("OneSignal Info", "OneSignal ID: " + oneSignalId);
        FirebaseUtil.currentUserDetails().update("oneSignalId", oneSignalId);
        updateSubscriptionId(oneSignalId);
    }

    private static void updateSubscriptionId(String oneSignalId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url("https://api.onesignal.com/apps/"+ONESIGNAL_APP_ID+"/users/by/onesignal_id/" + oneSignalId)
                            .get()
                            .addHeader("accept", "application/json")
                            .build();

                    Response response = client.newCall(request).execute();

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    JSONObject responseObject = new JSONObject(response.body().string());
                    JSONArray subscriptionsArray = responseObject.getJSONArray("subscriptions");
                    if (subscriptionsArray.length() > 0) {
                        String subscriptionId = subscriptionsArray.getJSONObject(0).getString("id");
                        FirebaseUtil.currentUserDetails().update("subscriptionId", subscriptionId);
                        Log.i("OneSignal Response", "Subscription ID: " + subscriptionId);
                    } else {
                        FirebaseUtil.currentUserDetails().update("subscriptionId", null);
                        Log.e("OneSignal Response", "No se encontraron suscripciones");
                    }
                } catch (Exception e) {
                    Log.e("OneSignal Response", Log.getStackTraceString(e));
                    FirebaseUtil.currentUserDetails().update("subscriptionId", null);
                }
                return null;
            }
        }.execute();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // Usa la latitud, longitud y altitud como sea necesario
                coordinates = new double[]{latitude, longitude};
                
                requestNotificationPermission();
            } else {
                // Si la ubicación es nula, puedes solicitar actualizaciones de ubicación
                requestLocationUpdates();
            }
        });
    }

    private void requestNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos de notificación no están concedidos, solicítalos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_NOTIFICATION_PERMISSION);
        }
    }


    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000) // 10 segundos
                .setFastestInterval(5 * 1000); // 5 segundos

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        // Usa la latitud, longitud y altitud como sea necesario
                        coordinates = new double[]{latitude, longitude};
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_NOTIFICATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Los permisos de notificación han sido concedidos
                    // Puedes proceder con tu lógica de notificación aquí
                } else {
                    Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}