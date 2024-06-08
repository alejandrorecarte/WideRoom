package com.example.wideroom.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.wideroom.R;
import com.example.wideroom.activities.login.LoginEmailActivity;
import com.example.wideroom.fragments.FriendRequestFragment;
import com.example.wideroom.models.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.onesignal.OneSignal;

/**
 * This class is used to show the user a loading screen while the app is starting up.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class SplashActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_splash);
        // controls the notification sending of chat, requests and what opens depending on the notification that is pressed
        OneSignal.getNotifications().addClickListener(event ->
        {
            try {
                String activity = event.getNotification().getAdditionalData().get("activity").toString();
                if(activity.equals("ChatActivity")){
                    String userId = event.getNotification().getAdditionalData().get("userId").toString();
                    FirebaseUtil.allUserCollectionReference().document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    UserModel model = task.getResult().toObject(UserModel.class);
                                    Intent mainIntent = new Intent(this, MainActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(mainIntent);
                                    Intent intent = new Intent(this, ChatActivity.class);
                                    AndroidUtil.passUserModelAsIntent(intent, model);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }else if(activity.equals("FriendRequestFragment")){
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);
                    Fragment friendRequestFragment = new FriendRequestFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,friendRequestFragment).commit();
                }else if(activity.equals("SearchUserActivity")){
                    String userId = event.getNotification().getAdditionalData().get("userId").toString();
                    FirebaseUtil.allUserCollectionReference().document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    UserModel model = task.getResult().toObject(UserModel.class);
                                    Intent mainIntent = new Intent(this, MainActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(mainIntent);
                                    Intent intent = new Intent(this, ChatActivity.class);
                                    AndroidUtil.passUserModelAsIntent(intent, model);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }catch(Exception e){
                Log.e("OneSignal Response", Log.getStackTraceString(e));
            }
        });
        new Handler().postDelayed(new Runnable() {
            /**
             * Identifies if the user has already logged in or not and redirects to the correct activity
             */
            @Override
            public void run() {
                if(FirebaseUtil.isLoggedIn()){
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginEmailActivity.class));
                }
                finish();
            }
        },1000);
    }
}