package com.staceybellerose.randomwordgenerator;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import jonathanfinerty.once.Once;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        Once.initialise(this);
    }
}
