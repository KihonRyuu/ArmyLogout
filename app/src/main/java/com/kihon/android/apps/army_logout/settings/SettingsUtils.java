package com.kihon.android.apps.army_logout.settings;

import com.google.gson.Gson;

import com.kihon.android.apps.army_logout.AppApplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;

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

    private static final String PREF_LOGIN_DATE_MILLIS = "pref_login_date_millis";
    private static final String PREF_MILITARY_INFO = "pref_military_info";
    private static final String PREF_INFO_ITEM_INDEXES = "pref_info_item_indexes";

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

    public static void setLoginMillis(long loginMillis) {
        sSharedPreferences.edit().putLong(PREF_LOGIN_DATE_MILLIS, loginMillis).apply();
    }

    public static String getMilitaryInfo() {
        return sSharedPreferences.getString(PREF_MILITARY_INFO, null);
    }

    public static void setMilitaryInfo(String jsonString) {
        sSharedPreferences.edit().putString(PREF_MILITARY_INFO, jsonString).apply();
    }

    public static int[] getInfoItemIndexes() {
        String string = sSharedPreferences.getString(PREF_INFO_ITEM_INDEXES, null);
        return string == null ? null : new Gson().fromJson(string, int[].class);
    }

    public static void setInfoItemIndexes(int[] indexes) {
        sSharedPreferences.edit().putString(PREF_INFO_ITEM_INDEXES, Arrays.toString(indexes)).apply();
    }
}
