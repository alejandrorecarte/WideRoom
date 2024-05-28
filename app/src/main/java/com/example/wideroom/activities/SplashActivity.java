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

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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