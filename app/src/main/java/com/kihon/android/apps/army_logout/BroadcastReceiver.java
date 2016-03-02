package com.kihon.android.apps.army_logout;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.SimpleTimeZone;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
	
	private long diffInMillis = 0;
	private long mInDateTimeInMillis = 0;
	private long mOutDateTimeInMillis = 0;
	private String mCountTimeText = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("BroadcastReceiver", "onReceive");

		Calendar calendar = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
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

		calendar.setTimeInMillis(diffInMillis);

//		mCountTimeText = (calendar.get(Calendar.DAY_OF_YEAR)-1) + "天 "
//		+ pad(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
//		+ pad(calendar.get(Calendar.MINUTE)) + ":"
//		+ pad(calendar.get(Calendar.SECOND));
		
		StringBuffer buffer = new StringBuffer();
		
		if (mInDateTimeInMillis <= System.currentTimeMillis()){
			if ((calendar.get(Calendar.YEAR) - 1970) != 0) {
				buffer.append((calendar.get(Calendar.YEAR) - 1970) + "年");
				buffer.append((calendar.get(Calendar.MONTH) + 1) + "個月");
				buffer.append((calendar.get(Calendar.DAY_OF_MONTH) - 1) + "天 ");
			} else {
				buffer.append((calendar.get(Calendar.DAY_OF_YEAR) - 1) + "天 ");
			}
		} else {
			buffer.append((calendar.get(Calendar.DAY_OF_YEAR) - 1) + "天 ");
		}
//		buffer.append((calendar.get(Calendar.DAY_OF_YEAR) - 1) + "天 ");
		buffer.append((pad(calendar.get(Calendar.HOUR_OF_DAY)) + ":"));
		buffer.append((pad(calendar.get(Calendar.MINUTE)) + ":"));
		buffer.append((pad(calendar.get(Calendar.SECOND))));
		mCountTimeText = buffer.toString();

		double login_passday = Calendar.getInstance().getTimeInMillis() - mInDateTimeInMillis;
		
		login_percent = (float) (( login_passday / (mOutDateTimeInMillis - mInDateTimeInMillis)) * 100f);
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
		
		
		ComponentName thiswidget = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager.getInstance(context).updateAppWidget(thiswidget, views);
		
	}
	
	private String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
	

}
