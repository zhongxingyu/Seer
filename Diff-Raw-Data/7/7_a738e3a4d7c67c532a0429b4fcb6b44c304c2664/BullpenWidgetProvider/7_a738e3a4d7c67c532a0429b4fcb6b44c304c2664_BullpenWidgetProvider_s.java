 
 package com.smilo.bullpen;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class BullpenWidgetProvider extends AppWidgetProvider {
 
     private static final String TAG = "BullpenWidgetProvider";
 
     // the pending intent to broadcast alarm.
     private static PendingIntent mSender;
     
     // the alarm manager to refresh bullpen widget periodically.
     private static AlarmManager mManager;
 
     // the intent to save [android.appwidget.action.APPWIDGET_UPDATE] intent.
     private static Intent mAppWidgetUpdateIntent;
     
     // the url string to show selected item.
     private static String mSelectedItemUrl = null;
     
     // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
     private static boolean mFirstCallListViewService = true;
     private static boolean mFirstCallContentService = true;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         super.onReceive(context, intent);
 
         ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         AppWidgetManager awm = AppWidgetManager.getInstance(context);
 
         String action = intent.getAction();
         int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
         Log.i(TAG, "onReceive - action[" + action + "], appWidgetsNum[" + appWidgetIds.length + "]");
 
         for (int i=0 ; i<appWidgetIds.length ; i++) {
             
             // APPWIDGET_UPDATE intent ////////////////////
             // This intent will be called periodically.
             if (action.equals(Constants.ACTION_APPWIDGET_UPDATE)) {
                 // Refresh alarm setting
                 mAppWidgetUpdateIntent = intent;
                 removePreviousAlarm();
                 setNewAlarm(context);
 
                 setRemoteViewToShowList(context, awm, appWidgetIds[i] );
     
             // APPWIDGET_DISABLED intent ////////////////////
             // This intent will be called when Bullpen widget removed. 
             } else if (action.equals(Constants.ACTION_APPWIDGET_DISABLED)) {
                 removePreviousAlarm();
 
             // ACTION_SHOW_ITEM intent ////////////////////
             // This intent will be called when some item selected.
             // EXTRA_ITEM_URL was already filled in the BullpenListViewFactory - getViewAt().
             } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                 if (Utils.checkInternetConnectivity(cm)) {
                     removePreviousAlarm();
                     mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                    
                    setRemoteViewToShowItem(context, awm, appWidgetIds[i]);
        
                     // Send broadcast intent to update mSelectedItemUrl variable on the BullpenContentFactory.
                     // On the first time to show some item, this intent does not operate.
                     Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_URL);
                     broadcastIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
                     context.sendBroadcast(broadcastIntent);
                 } else {
                     Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                 }
 
             // ACTION_SHOW_LIST intent ////////////////////
             // This intent will be called when current item pressed.
             } else if (action.equals(Constants.ACTION_SHOW_LIST)) {
                 if (Utils.checkInternetConnectivity(cm)) {
                     // Refresh alarm setting
                     removePreviousAlarm();
                     setNewAlarm(context);
         
                     setRemoteViewToShowList(context, awm, appWidgetIds[i]);
                 } else {
                     Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                 }
             }
         }
     }
 
     private void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId) {
         
         Intent serviceIntent, clickIntent;
         
         serviceIntent = new Intent(context, BullpenListViewService.class);
         serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
     
         clickIntent = new Intent(context, BullpenWidgetProvider.class);
         clickIntent.setAction(Constants.ACTION_SHOW_ITEM);
         clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
     
         RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
         // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
         rv.setRemoteAdapter(appWidgetId, R.id.listView, serviceIntent);
     
         PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                 context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setPendingIntentTemplate(R.id.listView, linkPendingIntent);
     
         Log.i(TAG, "updateAppWidget[BaseballListViewService]");
         awm.updateAppWidget(appWidgetId, rv);
         if (mFirstCallListViewService) {
             mFirstCallListViewService = false;
         } else {
             Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballListViewService]");
             awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
         }
     }
     
     private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId) {
     
         Intent serviceIntent, clickIntent;
         
         serviceIntent = new Intent(context, BullpenContentService.class);
         serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl); // Store mSelectedItemUrl to the serviceIntent
     
         clickIntent = new Intent(context, BullpenWidgetProvider.class);
         clickIntent.setAction(Constants.ACTION_SHOW_LIST);
         clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
     
         RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
         // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
         rv.setRemoteAdapter(appWidgetId, R.id.contentView, serviceIntent);
     
         PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                 context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setPendingIntentTemplate(R.id.contentView, linkPendingIntent);
     
         Log.i(TAG, "updateAppWidget[BaseballContentService]");
         awm.updateAppWidget(appWidgetId, rv);
         if (mFirstCallContentService) {
             mFirstCallContentService = false;
         } else {
             Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballContentService]");
             awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.contentView);
         }
     }
     
     private void setNewAlarm(Context context) {
         Log.i(TAG, "setNewAlarm");
 
         long firstTime = System.currentTimeMillis() + Constants.WIDGET_UPDATE_INTERVAL_AT_MILLIS;
         mSender = PendingIntent.getBroadcast(context, 0, mAppWidgetUpdateIntent, 0);
         mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         mManager.set(AlarmManager.RTC, firstTime, mSender);
     }
 
     private void removePreviousAlarm() {
         Log.i(TAG, "removePreviousAlarm");
 
         if (mManager != null && mSender != null) {
             mSender.cancel();
             mManager.cancel(mSender);
         }
     }
     
     @Override
     public void onUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
         Log.i(TAG, "onUpdate");
 
         super.onUpdate(context, awm, appWidgetIds);
     }
 
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         Log.i(TAG, "onDeleted");
 
         super.onDeleted(context, appWidgetIds);
     }
 
     @Override
     public void onDisabled(Context context) {
         Log.i(TAG, "onDisabled");
         super.onDisabled(context);
     }
 
     @Override
     public void onEnabled(Context context) {
         Log.i(TAG, "onEnabled");
         super.onEnabled(context);
     }
 }
