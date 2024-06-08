package com.example.wideroom;

import android.app.Application;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

/**
 * This class is used to initialize the OneSignal SDK.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class OneSignalNotification extends Application {

    private static final String ONESIGNAL_APP_ID = "27100f8e-6316-478b-8ba0-a8157f66495b";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(){
        super.onCreate();
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
}