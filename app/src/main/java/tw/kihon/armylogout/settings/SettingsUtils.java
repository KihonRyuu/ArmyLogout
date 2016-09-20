package tw.kihon.armylogout.settings;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;

import tw.kihon.armylogout.AppApplication;
import tw.kihon.armylogout.InfoItem;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;

public class SettingsUtils {

    private static final String TAG = "SettingsUtils";

//    private static final Context sContext = AppApplication.getInstance();

    private static final SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            AppApplication.getInstance());

    private static final String PREF_DEVICE_ID = "pref_device_id";
    private static final String PREF_FCM_TOKEN = "pref_fcm_token";
    private static final String PREF_SENT_TOKEN_TO_SERVER = "pref_sent_token_to_server";
    private static final String PREF_LOGIN = "pref_login";
    private static final String PREF_DRIVER_DATA = "pref_driver_data";
    private static final String PREF_JOB_NO = "pref_job_no";
    private static final String PREF_PASSWORD = "pref_password";

    private static final String PREF_LOGIN_DATE_MILLIS = "pref_login_date_millis";
    private static final String PREF_MILITARY_INFO = "pref_military_info";
    private static final String PREF_INFO_ITEM_INDEXES = "pref_info_item_indexes";
    private static final String PREF_PROGRESSBAR_COLOR = "pref_progressbar_color";

    private static final String PREF_WIDGET_BACKGROUND_COLOR = "pref_widget_background_color";
    private static final String PREF_WIDGET_TITLE_COLOR = "pref_widget_title_color";
    private static final String PREF_WIDGET_CONTENT_COLOR = "pref_widget_content_color";
    private static final String PREF_FIRST_RUN = "pref_first_run";
    private static final String PREF_FEED_BACK = "pref_feed_back";
    private static final String PREF_WELCOME_TEXT = "pref_welcome_text";
    private static final String PREF_VIEW_MODE = "pref_view_mode";

    public static final int VIEW_MODE_RECYCLER_VIEW = 0;
    public static final int VIEW_MODE_LEGACY = 1;

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

    public static void setFcmToken(String token) {
        sSharedPreferences.edit().putString(PREF_FCM_TOKEN, token).apply();
    }

    public static String getFcmToken() {
        return sSharedPreferences.getString(PREF_FCM_TOKEN, null);
    }

    public static String getMilitaryInfo() {
        return sSharedPreferences.getString(PREF_MILITARY_INFO, null);
    }

    public static void setMilitaryInfo(String jsonString) {
        sSharedPreferences.edit().putString(PREF_MILITARY_INFO, jsonString).apply();
    }

    public static int[] getInfoItemIndexes() {
        String string = sSharedPreferences.getString(PREF_INFO_ITEM_INDEXES, null);
        return string == null ? Ints.toArray(ContiguousSet.create(Range.closedOpen(0, InfoItem.values().length), DiscreteDomain.integers())) : new Gson().fromJson(string, int[].class);
    }

    public static void setInfoItemIndexes(int[] indexes) {
        sSharedPreferences.edit().putString(PREF_INFO_ITEM_INDEXES, Arrays.toString(indexes)).apply();
    }

    public static void setProgressBarColor(int selectedColor) {
        sSharedPreferences.edit().putInt(PREF_PROGRESSBAR_COLOR, selectedColor).apply();

    }

    public static int getProgressBarColor() {
        return sSharedPreferences.getInt(PREF_PROGRESSBAR_COLOR, ContextCompat.getColor(AppApplication.getInstance(), tw.kihon.armylogout.R.color.colorPrimary));
    }

    public static void setWidgetBackgroundColor(int selectedColor) {
        sSharedPreferences.edit().putInt(PREF_WIDGET_BACKGROUND_COLOR, selectedColor).apply();
    }

    public static void setWidgetTitleColor(int selectedColor) {
        sSharedPreferences.edit().putInt(PREF_WIDGET_TITLE_COLOR, selectedColor).apply();
    }

    public static void setWidgetContentColor(int selectedColor) {
        sSharedPreferences.edit().putInt(PREF_WIDGET_CONTENT_COLOR, selectedColor).apply();
    }

    public static int getWidgetBackgroundColor() {
        return sSharedPreferences.getInt(PREF_WIDGET_BACKGROUND_COLOR, 0xAAFFFFFF);
    }

    public static int getWidgetTitleColor() {
        return sSharedPreferences.getInt(PREF_WIDGET_TITLE_COLOR, Color.BLACK);
    }

    public static int getWidgetContentColor() {
        return sSharedPreferences.getInt(PREF_WIDGET_CONTENT_COLOR, ContextCompat.getColor(AppApplication.getInstance(), tw.kihon.armylogout.R.color.md_blue_400));
    }

    public static void firstRun() {
        sSharedPreferences.edit().putBoolean(PREF_FIRST_RUN, false).apply();
    }

    public static boolean isFirstRun() {
        return sSharedPreferences.getBoolean(PREF_FIRST_RUN, true);
    }

    public static void feedback() {
        sSharedPreferences.edit().putBoolean(PREF_FEED_BACK, false).apply();
    }

    public static boolean isHelpFeedback() {
        return sSharedPreferences.getBoolean(PREF_FEED_BACK, true);
    }

    public static String getWelcomeText() {
        return sSharedPreferences.getString(PREF_WELCOME_TEXT, "yo 弟兄!");
    }

    public static void setWelcomeText(String text) {
        sSharedPreferences.edit().putString(PREF_WELCOME_TEXT, text).apply();
    }

    public static void setViewMode(int mode) {
        sSharedPreferences.edit().putInt(PREF_VIEW_MODE, mode).apply();
    }

    public static int getViewMode() {
        return sSharedPreferences.getInt(PREF_VIEW_MODE, VIEW_MODE_RECYCLER_VIEW);
    }
}
