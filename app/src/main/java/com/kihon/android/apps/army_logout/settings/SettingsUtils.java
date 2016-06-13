package com.kihon.android.apps.army_logout.settings;

import com.kihon.android.apps.army_logout.AppApplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class SettingsUtils {

    private static final String TAG = "SettingsUtils";

//    private static final Context sContext = AppApplication.getInstance();

    private static final SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            AppApplication.getInstance());

    private static final String PREF_DEVICE_ID = "pref_device_id";
    private static final String PREF_GCM_TOKEN = "pref_gcm_token";
    private static final String PREF_SENT_TOKEN_TO_SERVER = "pref_sent_token_to_server";
    private static final String PREF_LOGIN = "pref_login";
    private static final String PREF_DRIVER_DATA = "pref_driver_data";
    private static final String PREF_JOB_NO = "pref_job_no";
    private static final String PREF_PASSWORD = "pref_password";

    public static void setDeviceId(String deviceId) {
        sSharedPreferences.edit().putString(PREF_DEVICE_ID, deviceId).apply();
    }

    public static String getDeviceId() {
        return sSharedPreferences.getString(PREF_DEVICE_ID, createDeviceId());
    }

    public static void setSentTokenToServer(boolean sent) {
        sSharedPreferences.edit().putBoolean(PREF_SENT_TOKEN_TO_SERVER, sent).apply();
    }

    public static boolean getSentTokenToServer() {
        return sSharedPreferences.getBoolean(PREF_SENT_TOKEN_TO_SERVER, false);
    }

    private static String createDeviceId() {
        String deviceId = Settings.Secure.getString(AppApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
        SettingsUtils.setDeviceId(deviceId);
        Log.d(TAG, "createDeviceId: " + deviceId);
        return deviceId;
    }

    public static void setGcmToken(String token) {
        sSharedPreferences.edit().putString(PREF_GCM_TOKEN, token).apply();
    }

    public static String getGcmToken() {
        return sSharedPreferences.getString(PREF_GCM_TOKEN, "FAKE_" + getDeviceId());
    }

    public static void setLoginMillis() {

    }
}
