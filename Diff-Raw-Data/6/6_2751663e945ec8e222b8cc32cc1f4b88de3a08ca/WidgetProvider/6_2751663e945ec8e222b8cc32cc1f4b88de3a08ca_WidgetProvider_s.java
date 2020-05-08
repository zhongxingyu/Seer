 package com.tywholland.poeevents;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
import android.util.Log;
 
 public class WidgetProvider extends AppWidgetProvider {
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 
		Log.d("POE", "in provider onUpdate");
		PoEUtil.updateWidget(context);
		Log.d("POE", "after update widget in provider");
 
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 }
