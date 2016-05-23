package com.zzg.demo_analytics;

import android.app.Application;

import com.zzg.logservice.utils.LogReceiverUtils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogReceiverUtils.regeistLogReceiver("4f83c5d852701564c0000011", getApplicationContext());
//        LogReceiverUtils.setDebugMode(true);
    }
}
