package com.example.wideroom;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;

    ChatFragment chatFragment;
    ProfileFragment profileFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatFragment= new ChatFragment();
        profileFragment=new ProfileFragment();

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        searchButton= findViewById(R.id.main_search_btn);

        searchButton.setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this,SearchUserActivity.class));
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.menu_chat){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                }

                if(item.getItemId()==R.id.menu_profile){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                }
                return false;
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
}