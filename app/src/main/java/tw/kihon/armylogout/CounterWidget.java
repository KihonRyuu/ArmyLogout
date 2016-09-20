package tw.kihon.armylogout;

import com.google.android.gms.analytics.HitBuilders;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Locale;

import tw.kihon.armylogout.settings.SettingsUtils;

public class CounterWidget extends AppWidgetProvider {

    private static final String TAG = "CounterWidget";

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        AppApplication.getInstance().getDefaultTracker().setScreenName("widget");
        AppApplication.getInstance().getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: ");
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {

        private static final String TAG = "UpdateService";

        private Handler mHandler = new Handler(Looper.getMainLooper());
        private Runnable mRunnable;
        private ServiceUtil mServiceUtil;
        private MilitaryInfo mMilitaryInfo;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            mRunnable = new Runnable() {
                @Override
                public void run() {

                    // Build the widget update for today
                    RemoteViews updateViews = buildUpdate(UpdateService.this);
                    Intent intent = new Intent(UpdateService.this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, (int) System.currentTimeMillis(), intent, 0);
                    updateViews.setOnClickPendingIntent(R.id.widgetMainLayout, pendingIntent);
//                    Log.v(TAG, "update built");

                    // Push update for this widget to the home screen
                    ComponentName thisWidget = new ComponentName(UpdateService.this, CounterWidget.class);
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(UpdateService.this);
                    appWidgetManager.updateAppWidget(thisWidget, updateViews);
//                    Log.v(TAG, "widget updated");

                    mHandler.postDelayed(this, 650);
                }
            };

            mHandler.post(mRunnable);

            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            mMilitaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());
            mServiceUtil = new ServiceUtil(mMilitaryInfo);

            views.setTextViewText(R.id.until_logout_title, mServiceUtil.isLoggedIn() ? "距離返陽還剩下" : "距離入伍還剩下");
            views.setTextViewText(R.id.until_logout_text, mServiceUtil.getRemainingDayWithString());
            views.setTextViewText(R.id.login_percent_text, String.format(Locale.TAIWAN, "%.2f%%", mServiceUtil.getPercentage()));
            views.setProgressBar(R.id.progressBar, 100, (int) mServiceUtil.getPercentage(), false);

            views.setInt(R.id.widgetMainLayout, "setBackgroundColor", SettingsUtils.getWidgetBackgroundColor());
            views.setTextColor(R.id.until_logout_title, SettingsUtils.getWidgetTitleColor());
            views.setTextColor(R.id.logout_percent_title, SettingsUtils.getWidgetTitleColor());
            views.setTextColor(R.id.until_logout_text, SettingsUtils.getWidgetContentColor());
            views.setTextColor(R.id.login_percent_text, SettingsUtils.getWidgetContentColor());

          /*
            SharedPreferences settings = context.getSharedPreferences(MainActivity.LEGACY_PREF, 0);
            views.setInt(R.id.widgetMainLayout, "setBackgroundColor", settings.getInt(MainActivity.PREF_WIDGETBGCOLOR, Color.WHITE));
            views.setTextColor(R.id.until_logout_title, settings.getInt(MainActivity.PREF_WIDGETTITLECOLOR, 0xFF000000));
            views.setTextColor(R.id.until_logout_text, settings.getInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, 0xFF3B5998));
            views.setTextColor(R.id.logout_percent_title, settings.getInt(MainActivity.PREF_WIDGETTITLECOLOR, 0xFF000000));
            views.setTextColor(R.id.login_percent_text, settings.getInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, 0xFF3B5998));

            mInDateTimeInMillis = settings.getLong(MainActivity.PREF_LOGINMILLIS, 0);
            mOutDateTimeInMillis = settings.getLong(MainActivity.PREF_LOGOUTMILLIS, 0);

            */
            return views;
        }
    }
}
