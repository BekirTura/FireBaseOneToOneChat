package com.donetr.chat.donefirebaseexample;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;


public class FirebaseChat extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
    }
}
