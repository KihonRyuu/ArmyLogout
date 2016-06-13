package com.kihon.android.apps.army_logout;

import android.app.Application;

/**
 * Created by kihon on 2016/06/10.
 */
public class AppApplication extends Application {

    private static AppApplication sInstance;

    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
    }

    public synchronized static AppApplication getInstance() {
        return sInstance;
    }

}
