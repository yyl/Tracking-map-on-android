package com.yyl.myrmex.tracking;

import android.app.Application;
import android.content.Context;

public class Tracking extends Application{

    private static Context context;

    public void onCreate(){
        super.onCreate();
        Tracking.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Tracking.context;
    }
}