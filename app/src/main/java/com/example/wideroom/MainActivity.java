package com.example.wideroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.onesignal.OneSignal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    EventsFragment eventsFragment;
    double[] coordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();
        eventsFragment = new EventsFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);

        OneSignal.getNotifications().clearAllNotifications();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        if (!OneSignal.getNotifications().getPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permisos de Notificación");
            builder.setMessage("¿Desea recibir notificaciones de esta aplicación?");
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    Intent intentNot = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intentNot.setData(uri);
                    startActivity(intentNot);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Si el usuario rechaza, cierra la actividad
                    finish();
                }
            });
            builder.setCancelable(false); // Evita que el usuario pueda cerrar el diálogo sin responder
            builder.show();
        }

        searchButton.setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_chat) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                }
                if (item.getItemId() == R.id.menu_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                }
                if (item.getItemId() == R.id.menu_event) {
                    Bundle args = new Bundle();
                    args.putSerializable("coordinates", coordinates);
                    eventsFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, eventsFragment).commit();
                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.menu_chat);

        String oneSignalId = OneSignal.getUser().getOnesignalId();

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
                            .url("https://api.onesignal.com/apps/8fb48336-9fc4-45ae-b884-ccda62fd2c3a/users/by/onesignal_id/" + oneSignalId)
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
            } else {
                // Si la ubicación es nula, puedes solicitar actualizaciones de ubicación
                requestLocationUpdates();
            }
        });
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
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }
}