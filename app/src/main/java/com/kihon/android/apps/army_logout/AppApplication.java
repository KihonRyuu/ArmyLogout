package com.kihon.android.apps.army_logout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.karumi.dexter.Dexter;
import com.kihon.android.apps.army_logout.settings.SettingsUtils;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import io.fabric.sdk.android.Fabric;

/**
 * Created by kihon on 2016/06/10.
 */
public class AppApplication extends Application {

    private static AppApplication sInstance;
    private FirebaseAnalytics mFirebaseAnalytics;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableExceptionReporting(true);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableAdvertisingIdCollection(true);
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        sInstance = this;
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
            Fabric.with(this, new Answers());
            Crashlytics.setUserIdentifier(SettingsUtils.getDeviceId());
        }
        super.onCreate();

        Dexter.initialize(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserId(SettingsUtils.getDeviceId());
        getDefaultTracker().set("&uid", SettingsUtils.getDeviceId());
    }

    public synchronized FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public synchronized static AppApplication getInstance() {
        return sInstance;
    }

}
