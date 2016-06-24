package com.kihon.android.apps.army_logout;

import java.util.Locale;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
//		Toast.makeText(context, "TimeWidgetRemoved id(s):"+appWidgetIds, Toast.LENGTH_SHORT).show();
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		Intent intent = new Intent(context, BroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(intent);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		super.onDisabled(context);
	}
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, BroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pendingIntent);		
	}
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//		final int N = appWidgetIds.length;
//
//		// Perform this loop procedure for each App Widget that belongs to this provider
//		for (int i=0; i<N; i++) {
//			int appWidgetId = appWidgetIds[i];
//
//			// Create an Intent to launch ExampleActivity
//			Intent intent = new Intent(context, MainActivity.class);
//			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//			// Get the layout for the App Widget and attach an on-click listener
//			// to the button
//			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
//			views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
//
//			// Tell the AppWidgetManager to perform an update on the current app widget
//			appWidgetManager.updateAppWidget(appWidgetId, views);
//
////			onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId));
//		}
		
		ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
		
		for (int widgetId : appWidgetManager.getAppWidgetIds(thisWidget)) {
			
			// Create an Intent to launch ExampleActivity
			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			//Get the remote views
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			// Set the text with the current time.
//			remoteViews.setTextViewText(R.id.textView2, Utility.getCurrentTime("hh:mm:ss a"));
			remoteViews.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

//		for (int appWidgetId : appWidgetIds) {
//			Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
//			onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, options);
//		}
		
//		Intent intent = new Intent(context, MainActivity.class);
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
//		views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
//		appWidgetManager.updateAppWidget(appWidgetIds, views);
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		//Do some operation here, once you see that the widget has change its size or position.
//		Toast.makeText(context, "onAppWidgetOptionsChanged() called", Toast.LENGTH_SHORT).show();
	    RemoteViews updateViews= new RemoteViews(context.getPackageName(), R.layout.widget);
	    String msg=
	        String.format(Locale.getDefault(),
	                      "[%d-%d] x [%d-%d]",
	                      newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),
	                      newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
	                      newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT),
	                      newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));

//	    updateViews.setTextViewText(R.id.size, msg);

	    appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	}
    
}
