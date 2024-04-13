package com.example.wideroom;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class OneSignalNotification extends Application {

    private static final String ONESIGNAL_APP_ID = "6b027511-d7eb-4c8b-aa32-f8211e2c317b";

    @Override
    public void onCreate(){
        super.onCreate();
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

    }
}
