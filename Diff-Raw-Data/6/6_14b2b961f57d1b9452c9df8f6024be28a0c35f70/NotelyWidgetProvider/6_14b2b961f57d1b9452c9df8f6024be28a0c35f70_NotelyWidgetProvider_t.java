 package com.kneejah.notely;
 
 import java.util.List;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class NotelyWidgetProvider extends AppWidgetProvider {
 	
 	private static final String TAG = "NotelyWidgetProvider";
 	
 	public static final String WIDGET_NEW      = "widget_new";
 	public static final String WIDGET_PREVIOUS = "widget_previous";
 	public static final String WIDGET_NEXT     = "widget_next";
 	public static final String WIDGET_EDIT     = "widget_edit";
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		
		Log.v(TAG, "in onUpdate");
		
 		// Build the intent to call the service
 		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notes_widget);
 		
 		// next button
 		PendingIntent pendingNextIntent = getButtonIntent(context, appWidgetIds, WIDGET_NEXT);
 		remoteViews.setOnClickPendingIntent(R.id.widget_next, pendingNextIntent);
 		
 		// previous button
 		PendingIntent pendingPrevIntent = getButtonIntent(context, appWidgetIds, WIDGET_PREVIOUS);
 		remoteViews.setOnClickPendingIntent(R.id.widget_prev, pendingPrevIntent);
 		
 		// new button
 		PendingIntent pendingNewIntent = getButtonIntent(context, appWidgetIds, WIDGET_NEW);
 		remoteViews.setOnClickPendingIntent(R.id.widget_new, pendingNewIntent);
 		
 		// actual note itself
 		PendingIntent pendingEditIntent = getButtonIntent(context, appWidgetIds, WIDGET_EDIT);
		remoteViews.setOnClickPendingIntent(R.id.widget_textarea, pendingEditIntent);
 		
 		// Finally update all widgets with the information about the click listener
 		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
 
 		// Update the widgets via the service with default behavior
 		Intent newIntent = new Intent(context, NotelyWidgetUpdateService.class);
 		newIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
 		context.startService(newIntent);
 		
 	}
 	
 	public static void updateWidgets(Context ctx) {
 		
 		Log.v(TAG, "going to update widgets");
 		
 		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
 		
 		int[] appWidgetIds = null;
 		
 		List<AppWidgetProviderInfo> providerInfoList = manager.getInstalledProviders();
 		
 		for (AppWidgetProviderInfo info : providerInfoList) {
 		    if (info.provider.getPackageName().equals(NotelyMain.PACKAGE)) {
 		    	appWidgetIds = manager.getAppWidgetIds(info.provider);
 		    }
 		}
 		
 		if (appWidgetIds != null) {
 			new NotelyWidgetProvider().onUpdate(ctx, manager, appWidgetIds);
 		}
 		
 	}
 	
 	private static PendingIntent getButtonIntent(Context ctx, int[] appWidgetIds, String text) {
 		
 		Intent prevIntent = new Intent(ctx, NotelyWidgetUpdateService.class);
 		prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
 		prevIntent.setAction(text);
 		return PendingIntent.getService(ctx, 0, prevIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 		
 	}
 	
 }
