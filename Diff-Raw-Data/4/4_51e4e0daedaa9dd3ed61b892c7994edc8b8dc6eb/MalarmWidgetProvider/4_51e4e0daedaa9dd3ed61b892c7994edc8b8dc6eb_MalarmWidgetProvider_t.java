 package com.mamewo.malarm24;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  */
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class MalarmWidgetProvider
 	extends AppWidgetProvider
 {
 	final static
 	private String PACKAGE_NAME = MalarmWidgetProvider.class.getPackage().getName();
 	final static
 	public String LIST_VIEWER_ACTION = PACKAGE_NAME + ".LIST_VIEWER_ACTION";
 	final static
 	public String PLAYER_ACTION = PACKAGE_NAME + ".PLAYER_ACTION";
 	
 	final static
 	private String TAG = "malarm";
 	
 	//TODO: play interface
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 		Log.i(TAG, "action: " + action);
 		if (PLAYER_ACTION.equals(action)) {
 			Intent i = new Intent(context, MalarmPlayerService.class);
 //			int command = intent.getIntExtra("command", 1);
 			Rect r = intent.getSourceBounds();
 //			Log.d(TAG, "command: " + command + " " + r.left);
 			//umm...
 			if (r.left > 130) {
 				i.setAction(MalarmPlayerService.PLAYNEXT_ACTION);
 				context.startService(i);
 			}
 			else {
 				i.setAction(MalarmPlayerService.PLAYSTOP_ACTION);
 				context.startService(i);
 			}
 		}
 		super.onReceive(context, intent);
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget);
 		{
 			Intent intent = new Intent(context, MalarmWidgetProvider.class);
 			intent.setAction(PLAYER_ACTION);
 			PendingIntent pintent =
 					PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 			rv.setOnClickPendingIntent(R.id.widget_play, pintent);
 		}
 		{
 			Intent intent = new Intent(context, MalarmWidgetProvider.class);
 			intent.setAction(PLAYER_ACTION);
 			PendingIntent pintent =
 					PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 			rv.setOnClickPendingIntent(R.id.widget_next, pintent);
 		}
 		appWidgetManager.updateAppWidget(appWidgetIds[0], rv);
 	}
 }
