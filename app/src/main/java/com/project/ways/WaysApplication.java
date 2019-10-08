package com.project.ways;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class WaysApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
