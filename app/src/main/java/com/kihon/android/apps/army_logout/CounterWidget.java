package com.kihon.android.apps.army_logout;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class CounterWidget extends AppWidgetProvider {

    private static final String TAG = "CounterWidget";

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
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

        private long diffInMillis = 0;
        private long mInDateTimeInMillis = 0;
        private long mOutDateTimeInMillis = 0;
        private String mCountTimeText = null;

        private Calendar mCalendar;
        private Handler mRefreshInformationHandler = new Handler();
        private Runnable mRefreshInformationRunnable;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            mCalendar = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));

            mRefreshInformationRunnable = new Runnable() {
                @Override
                public void run() {

                    // Build the widget update for today
                    RemoteViews updateViews = buildUpdate(UpdateService.this);
                    Intent intent = new Intent(UpdateService.this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, 0, intent, 0);
                    updateViews.setOnClickPendingIntent(R.id.widgetMainLayout, pendingIntent);
                    Log.v(TAG, "update built");

                    // Push update for this widget to the home screen
                    ComponentName thisWidget = new ComponentName(UpdateService.this, CounterWidget.class);
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(UpdateService.this);
                    appWidgetManager.updateAppWidget(thisWidget, updateViews);
                    Log.v(TAG, "widget updated");

                    mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);
                }
            };

            mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);

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

            float login_percent;

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setTextViewText(R.id.textView2, "");

            SharedPreferences settings = context.getSharedPreferences(MainActivity.PREF, 0);
            views.setInt(R.id.widgetMainLayout, "setBackgroundColor", settings.getInt(MainActivity.PREF_WIDGETBGCOLOR, Color.WHITE));
            views.setTextColor(R.id.until_logout_title, settings.getInt(MainActivity.PREF_WIDGETTITLECOLOR, 0xFF000000));
            views.setTextColor(R.id.until_logout_text, settings.getInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, 0xFF3B5998));
            views.setTextColor(R.id.logout_percent_title, settings.getInt(MainActivity.PREF_WIDGETTITLECOLOR, 0xFF000000));
            views.setTextColor(R.id.login_percent_text, settings.getInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, 0xFF3B5998));

            mInDateTimeInMillis = settings.getLong(MainActivity.PREF_LOGINMILLIS, 0);
            mOutDateTimeInMillis = settings.getLong(MainActivity.PREF_LOGOUTMILLIS, 0);

            /**
             * 20130408 - 入伍倒數
             */

            if (mInDateTimeInMillis > System.currentTimeMillis()) {
                diffInMillis = mInDateTimeInMillis - System.currentTimeMillis();
                views.setTextViewText(R.id.until_logout_title, "距離入伍還剩下");
            } else {
                diffInMillis = mOutDateTimeInMillis - System.currentTimeMillis();
                views.setTextViewText(R.id.until_logout_title, "距離返陽還剩下");
            }

            mCalendar.setTimeInMillis(diffInMillis);
            StringBuilder buffer = new StringBuilder();

            if (mInDateTimeInMillis <= System.currentTimeMillis()) {
                if ((mCalendar.get(Calendar.YEAR) - 1970) != 0) {
                    buffer.append(mCalendar.get(Calendar.YEAR) - 1970).append("年");
                    buffer.append(mCalendar.get(Calendar.MONTH) + 1).append("個月");
                    buffer.append(mCalendar.get(Calendar.DAY_OF_MONTH) - 1).append("天 ");
                } else {
                    buffer.append(mCalendar.get(Calendar.DAY_OF_YEAR) - 1).append("天 ");
                }
            } else {
                buffer.append(mCalendar.get(Calendar.DAY_OF_YEAR) - 1).append("天 ");
            }
            buffer.append(pad(mCalendar.get(Calendar.HOUR_OF_DAY))).append(":");
            buffer.append(pad(mCalendar.get(Calendar.MINUTE))).append(":");
            buffer.append((pad(mCalendar.get(Calendar.SECOND))));
            mCountTimeText = buffer.toString();

            double login_passday = Calendar.getInstance().getTimeInMillis() - mInDateTimeInMillis;

            login_percent = (float) ((login_passday / (mOutDateTimeInMillis - mInDateTimeInMillis)) * 100f);
            String percentText = new DecimalFormat("#.##").format(login_percent);
            if (login_percent >= 100) {
                views.setTextViewText(R.id.login_percent_text, "100%");
            } else if (login_percent <= 0) {
                views.setTextViewText(R.id.login_percent_text, "0%");
            } else {
                views.setTextViewText(R.id.login_percent_text, percentText + "%");
            }

            views.setTextViewText(R.id.until_logout_text, mCountTimeText);
            views.setProgressBar(R.id.progressBar, 100, (int) login_percent, false);

            if (diffInMillis <= 0) {
                views.setTextViewText(R.id.until_logout_text, "學長(`・ω・́)ゝ 你已經成功返陽了!");
                views.setTextViewText(R.id.login_percent_text, "100%");
            }
            return views;
        }

        private String pad(int c) {
            return String.format(Locale.getDefault(), "%02d", c);
        }
    }
}
