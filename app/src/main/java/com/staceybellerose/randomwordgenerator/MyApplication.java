package com.staceybellerose.randomwordgenerator;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.staceybellerose.randomwordgenerator.utils.PRNGFixes;

import jonathanfinerty.once.Once;

/**
 * Main Application class
 */
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
        PRNGFixes.apply();
    }
}
