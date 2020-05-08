 
 package com.smilo.bullpen;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetHost;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class BullpenWidgetProvider extends AppWidgetProvider {
 
     private static final String TAG = "BullpenWidgetProvider";
 
     // the pending intent to broadcast alarm.
     private static PendingIntent mSender;
     
     // the alarm manager to refresh bullpen widget periodically.
     private static AlarmManager mManager;
     
     // the url string to show selected item.
     private static String mSelectedItemUrl = null;
     
     // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
     private static boolean mIsSkipFirstCallListViewService = true;
     private static boolean mIsSkipFirstCallContentService = true;
     
     private static boolean mSelectedPermitMobileConnection = false;
     private static int mSelectedRefreshTime = -1;
     private static String mSelectedBullpenBoardUrl = null;
     
     // For SharedPreferences.
     private static final String mSharedPreferenceName = "Bullpen";
     private static final String mKeyCompleteToSetup = "key_complete_to_setup";
     private static final String mKeyPermitMobileConnection = "key_permit_mobile_connection";
     private static final String mKeyRefreshTime = "key_refresh_time";
     private static final String mKeyBullpenBoardUrl = "key_bullpen_board_url";
 
     
     private static enum PENDING_INTENT_REQUEST_CODE {
         REQUEST_TOP,
         REQUEST_PREV,
         REQUEST_NEXT,
         REQUEST_REFRESH,
         REQUEST_SETTING,
         REQUEST_UNKNOWN,
     };
     
     @Override
     public void onReceive(Context context, Intent intent) {
         super.onReceive(context, intent);
 
         String action = intent.getAction();
         int pageNum = intent.getIntExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
         int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
         AppWidgetManager awm = AppWidgetManager.getInstance(context);
         int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
         Log.i(TAG, "onReceive - action[" + action + "], appWidgetId[" + appWidgetId + "], pageNum[" + pageNum +
                 "], appWidgetsNum[" + appWidgetIds.length + "]");
         
         for (int i = 0 ; i < appWidgetIds.length ; i++) {
         	Log.i(TAG, "onReceive - current appWidgetId[" + appWidgetIds[i] + "]");
         	
         	if (appWidgetId == appWidgetIds[i]) {
 
                 // After setting configuration activity, this intent will be called.
                 if (action.equals(Constants.ACTION_INIT_LIST)) {
                     boolean selectedPermitMobileConnectionType = intent.getBooleanExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, false);
                     int selectedRefreshTimeType = intent.getIntExtra(Constants.EXTRA_REFRESH_TIME_TYPE, -1);
                     int selectedBullpenBoardType = intent.getIntExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, -1);
                     
                     // Save configuration info.
                     SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
                     SharedPreferences.Editor editor = pref.edit();
                     editor.putBoolean(mKeyCompleteToSetup, true);
                     editor.putBoolean(mKeyPermitMobileConnection, selectedPermitMobileConnectionType);
                     editor.putInt(mKeyRefreshTime, selectedRefreshTimeType);
                     editor.putInt(mKeyBullpenBoardUrl, selectedBullpenBoardType);
                     editor.commit();
                     
                     // Update global variables.
                     mSelectedPermitMobileConnection = selectedPermitMobileConnectionType;
                     mSelectedRefreshTime = Utils.getRefreshTime(selectedRefreshTimeType);
                     mSelectedBullpenBoardUrl = Utils.getBullpenBoardUrl(selectedBullpenBoardType);
 
                     // Send broadcast intent to update mSelectedBullpenBoardUrl and pageNum variable on the BullpenListViewFactory.
                     context.sendBroadcast(buildUpdateListUrlIntent(appWidgetId, Constants.DEFAULT_PAGE_NUM));
                     
                     if (Utils.isInternetConnected(context, mSelectedPermitMobileConnection) == false) {
                         setRemoteViewToShowLostInternetConnection(context, awm, appWidgetId, pageNum);
                         return;
                     } else {
                         // Broadcast ACTION_SHOW_LIST intent.
                         context.sendBroadcast(buildShowListIntent(context, appWidgetId, Constants.DEFAULT_PAGE_NUM));
                     }
 
                 // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
                 // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
                 } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                     (action.equals(Constants.ACTION_SHOW_LIST))) {
                     if (Utils.isInternetConnected(context, mSelectedPermitMobileConnection) == false) {
                         Log.e(TAG, "onReceive - Internet is not connected!");
                         return;
                     } else {
                         refreshAlarmSetting(context, appWidgetId, pageNum);
                         setRemoteViewToShowList(context, awm, appWidgetId, pageNum);
                     }
 
                 } else if (action.equals(Constants.ACTION_REFRESH_LIST)){
                     // Send broadcast intent to update mSelectedBullpenBoardUrl and pageNum variable on the BullpenListViewFactory.
                     context.sendBroadcast(buildUpdateListUrlIntent(appWidgetId, pageNum));
                     
                     // Broadcast ACTION_SHOW_LIST intent.
                     context.sendBroadcast(buildShowListIntent(context, appWidgetId, pageNum));
                     
                 // This intent will be called when some item selected.
                 // EXTRA_ITEM_URL was already filled in the BullpenListViewFactory - getViewAt().
                 } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                     removePreviousAlarm();
                     mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
 
                     // Send broadcast intent to update mSelectedItemUrl variable on the BullpenContentFactory.
                     context.sendBroadcast(buildUpdateItemUrlIntent(appWidgetId, pageNum));
                     
                     if (Utils.isInternetConnected(context, mSelectedPermitMobileConnection) == false) {
                         Log.e(TAG, "onReceive - Internet is not connected!");
                         return;
                     } else {
                         setRemoteViewToShowItem(context, awm, appWidgetId, pageNum);
                     }
                 }
         	}
         }
     }
 
     private Intent buildShowListIntent(Context context, int appWidgetId, int pageNum) {
         Intent intent = new Intent(context, BullpenWidgetProvider.class);
         intent.setAction(Constants.ACTION_SHOW_LIST);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         
         return intent;
     }
     
     private Intent buildUpdateListUrlIntent(int appWidgetId, int pageNum) {
         if (mSelectedBullpenBoardUrl == null) {
             Log.e(TAG, "buildUpdateListUrlIntent - mSelectedBullpenBoardUrl is null!");
             return null;            
         }
         
         Intent intent = new Intent(Constants.ACTION_UPDATE_LIST_URL);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
         intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         
         return intent;
     }
     
     private Intent buildUpdateItemUrlIntent(int appWidgetId, int pageNum) {
         if (mSelectedItemUrl == null) {
             Log.e(TAG, "buildUpdateItemUrlIntent - mSelectedItemUrl is null!");
             return null;            
         }
         
         Intent intent = new Intent(Constants.ACTION_UPDATE_ITEM_URL);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
         intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         
         return intent;
     }
         
     private Intent buildRefreshListIntent(Context context, int appWidgetId, int pageNum) {
         Intent intent = new Intent(context, BullpenWidgetProvider.class);
         intent.setAction(Constants.ACTION_REFRESH_LIST);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         
         return intent;
     }
     
     private Intent buildShowItemIntent(Context context, int appWidgetId, int pageNum, boolean isAddSelectedItemUri) {
         if (isAddSelectedItemUri == true && mSelectedItemUrl == null) {
             Log.e(TAG, "buildShowItemIntent - mSelectedItemUrl is null!");
             return null;            
         }
         
         Intent intent = new Intent(context, BullpenWidgetProvider.class);
         intent.setAction(Constants.ACTION_SHOW_ITEM);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         if (isAddSelectedItemUri)
             intent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
         intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         
         return intent;
     }
     
     private Intent buildConfigurationActivityIntent(Context context, int appWidgetId) {
         Intent intent = new Intent(context, BullpenConfigurationActivity.class);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, mSelectedPermitMobileConnection);
         intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, Utils.getRefreshTimeType(mSelectedRefreshTime));
         intent.putExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, Utils.getBullpenBoardType(mSelectedBullpenBoardUrl));
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
         return intent;
     }
 
     private void setRemoteViewToShowLostInternetConnection(Context context, AppWidgetManager awm, int appWidgetId, int pageNum) {
         
         Intent intent = null;
         PendingIntent pendingIntent = null;
         
         // Create new remoteViews
         RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lost_internet_connection);
         
         // Set refresh button of the remoteViews.
         intent = buildRefreshListIntent(context, appWidgetId, pageNum);
         pendingIntent = PendingIntent.getBroadcast(
                 context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnLostInternetRefresh, pendingIntent);
         
         // Set setting button of the remoteViews.
         intent = buildConfigurationActivityIntent(context, appWidgetId);
         pendingIntent = PendingIntent.getActivity(
         		context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnLostInternetSetting, pendingIntent);
         
         // Update widget.
         Log.i(TAG, "updateAppWidget [LostInternetConnection]");
         awm.updateAppWidget(appWidgetId, rv);
     }
     
     private void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId, int pageNum) {
 
         // Check abnormal case
         if (mSelectedBullpenBoardUrl == null) {
             Log.e(TAG, "setRemoteViewToShowList - mSelectedBullpenBoardUrl is null!");
             return;
         }
         
         Intent intent = null;
         PendingIntent pendingIntent = null;
         
         // Create new remoteViews.
         RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
         
         // Set a remoteAdapter to the remoteViews.
         Intent serviceIntent = new Intent(context, BullpenListViewService.class);
         serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         serviceIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
         serviceIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         // views.setRemoteAdapter(R.id.listView, serviceIntent); // For API14+
         rv.setRemoteAdapter(appWidgetId, R.id.listView, serviceIntent);
         rv.setScrollPosition(R.id.listView, 0); // Scroll to top
 
         
         if (Utils.isTodayBestUrl(mSelectedBullpenBoardUrl)) {
         	// Set title of the remoteViews.
         	rv.setTextViewText(R.id.textListTitle, Utils.getRemoteViewTitle(context, mSelectedBullpenBoardUrl));
             
         	rv.setViewVisibility(R.id.btnListNavPrev, View.GONE);
         	rv.setViewVisibility(R.id.btnListNavNext, View.GONE);
         } else {
         	// Set title of the remoteViews.
             rv.setTextViewText(R.id.textListTitle, Utils.getRemoteViewTitleWithPageNum(context, mSelectedBullpenBoardUrl, pageNum));
             intent = buildRefreshListIntent(context, appWidgetId, Constants.DEFAULT_PAGE_NUM);
             pendingIntent = PendingIntent.getBroadcast(
                     context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
             rv.setOnClickPendingIntent(R.id.textListTitle, pendingIntent);
             
             // Set prev button of the removeViews.
         	rv.setViewVisibility(R.id.btnListNavPrev, View.VISIBLE);
             intent = buildRefreshListIntent(context, appWidgetId, (pageNum > Constants.DEFAULT_PAGE_NUM ? pageNum - 1 : pageNum));
             pendingIntent = PendingIntent.getBroadcast(
                     context, PENDING_INTENT_REQUEST_CODE.REQUEST_PREV.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
             rv.setOnClickPendingIntent(R.id.btnListNavPrev, pendingIntent);
             
             // Set next button of the remoteViews.
         	rv.setViewVisibility(R.id.btnListNavNext, View.VISIBLE);
             intent = buildRefreshListIntent(context, appWidgetId, pageNum + 1);
             pendingIntent = PendingIntent.getBroadcast(
                     context, PENDING_INTENT_REQUEST_CODE.REQUEST_NEXT.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
             rv.setOnClickPendingIntent(R.id.btnListNavNext, pendingIntent);
         }
         
         /*
         // Set top button of the remoteViews.
         intent = buildRefreshListIntent(context, appWidgetId, Constants.DEFAULT_PAGE_NUM);
         pendingIntent = PendingIntent.getBroadcast(
                 context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnListNavTop, pendingIntent);
         */
 
         // Set refresh button of the remoteViews.
         intent = buildRefreshListIntent(context, appWidgetId, pageNum);
         pendingIntent = PendingIntent.getBroadcast(
                 context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnListRefresh, pendingIntent);
         
         // Set setting button of the remoteViews.
         intent = buildConfigurationActivityIntent(context, appWidgetId);
         pendingIntent = PendingIntent.getActivity(
         		context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnListSetting, pendingIntent);
         
         // Set a pending intent for click event to the remoteViews.
         Intent clickIntent = buildShowItemIntent(context, appWidgetId, pageNum, false);
         PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
         		context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setPendingIntentTemplate(R.id.listView, linkPendingIntent);
     
         // Update widget.
         Log.i(TAG, "updateAppWidget [BaseballListViewService]");
         awm.updateAppWidget(appWidgetId, rv);
         
         // On first call, we need not execute notifyAppWidgetViewDataChanged()
         // because onDataSetChanged() is called automatically after BullpenListViewFactory is created.
         if (mIsSkipFirstCallListViewService) {
             mIsSkipFirstCallListViewService = false;
         } else {
             Log.i(TAG, "notifyAppWidgetViewDataChanged [BaseballListViewService]");
             awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
         }
     }
     
     private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId, int pageNum) {
     
         // Check abnormal case
         if (mSelectedBullpenBoardUrl == null) {
             Log.e(TAG, "setRemoteViewToShowItem - mSelectedBullpenBoardUrl is null!");
             return;
         }
         
         Intent intent = null;
         PendingIntent pendingIntent = null;
         
         // Create new remoteViews.
         RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
         
         // Set a remoteAdapter to the remoteViews.
         Intent serviceIntent = new Intent(context, BullpenContentService.class);
         serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
         serviceIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         // views.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
         rv.setRemoteAdapter(appWidgetId, R.id.contentView, serviceIntent);
         //rv.setScrollPosition(R.id.contentView, 0); // Scroll to top
 
         // Set title of the remoteViews.
         rv.setTextViewText(R.id.textContentTitle, Utils.getRemoteViewTitle(context, mSelectedBullpenBoardUrl));
 
         // Set top button of the remoteViews.
         intent = buildRefreshListIntent(context, appWidgetId, pageNum);
         pendingIntent = PendingIntent.getBroadcast(
                 context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnContentNavTop, pendingIntent);
         
         // Set refresh button of the remoteViews.
         intent = buildShowItemIntent(context, appWidgetId, pageNum, true);
         pendingIntent = PendingIntent.getBroadcast(
         		context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnContentRefresh, pendingIntent);
         
         // Set setting button of the remoteViews.
         intent = buildConfigurationActivityIntent(context, appWidgetId);
         pendingIntent = PendingIntent.getActivity(
         		context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.btnContentSetting, pendingIntent);
         
         // Set a pending intent for click event to the remoteViews.
         Intent clickIntent = buildShowListIntent(context, appWidgetId, pageNum);
         PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                 context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setPendingIntentTemplate(R.id.contentView, linkPendingIntent);
     
         // Update widget.
         Log.i(TAG, "updateAppWidget [BaseballContentService]");
         awm.updateAppWidget(appWidgetId, rv);
 
         // On first call, we need not execute notifyAppWidgetViewDataChanged()
         // because onDataSetChanged() is called automatically after BullpenContentFactory is created.
         if (mIsSkipFirstCallContentService) {
             mIsSkipFirstCallContentService = false;
         } else {
             Log.i(TAG, "notifyAppWidgetViewDataChanged [BaseballContentService]");
             awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.contentView);
         }
     }
     
     private void refreshAlarmSetting(Context context, int appWidgetId, int pageNum) {
         // If user does not want to refresh, just remove alarm setting.
         if (mSelectedRefreshTime == -1) {
             removePreviousAlarm();
             
         // If user wants to refresh, set new alarm.
         } else {
             removePreviousAlarm();
             setNewAlarm(context, appWidgetId, pageNum, false);
         }
     }
     
     private void setNewAlarm(Context context, int appWidgetId, int pageNum, boolean isUrgentMode) {
         Log.i(TAG, "setNewAlarm - appWidgetId[" + appWidgetId + "], pageNum[" + pageNum + "]");
 
         Intent updateIntent = new Intent();
         updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
         updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         updateIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
         updateIntent.setClass(context, BullpenWidgetProvider.class);
         
         long alarmTime = System.currentTimeMillis() + (mSelectedRefreshTime <= 0 ? Constants.DEFAULT_INTERVAL_AT_MILLIS : mSelectedRefreshTime);
         if (isUrgentMode) alarmTime = 0;
         mSender = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
         mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         mManager.set(AlarmManager.RTC, alarmTime, mSender);
     }
 
     private void removePreviousAlarm() {
         Log.i(TAG, "removePreviousAlarm");
 
         if (mManager != null && mSender != null) {
             mSender.cancel();
             mManager.cancel(mSender);
         }
     }
 
     public static void removeWidget(Context context, int appWidgetId) {
         AppWidgetHost host = new AppWidgetHost(context, 1);
         host.deleteAppWidgetId(appWidgetId);
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
         removePreviousAlarm();
         
         // Delete all saved data.
         SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = pref.edit();
         editor.clear();
         editor.commit();
         
         super.onDisabled(context);
     }
 
     @Override
     public void onEnabled(Context context) {
         Log.i(TAG, "onEnabled");
         removePreviousAlarm();
 
    	// Initialize global variables.
        mIsSkipFirstCallListViewService = true;
        mIsSkipFirstCallContentService = true;
        
         // Load configuration info.
         SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
         
         boolean isCompleteToSetup = pref.getBoolean(mKeyCompleteToSetup, false);
         mSelectedPermitMobileConnection = pref.getBoolean(mKeyPermitMobileConnection, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION);
         mSelectedRefreshTime = Utils.getRefreshTime(pref.getInt(mKeyRefreshTime, Constants.DEFAULT_REFRESH_TIME_TYPE));
         mSelectedBullpenBoardUrl = Utils.getBullpenBoardUrl(pref.getInt(mKeyBullpenBoardUrl, Constants.DEFAULT_BULLPEN_BOARD_TYPE));
 
         if (isCompleteToSetup) {
             // Set urgent alarm to update list as soon as possible.
             AppWidgetManager awm = AppWidgetManager.getInstance(context);
             int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
 
             for (int i = 0 ; i < appWidgetIds.length ; i++) {
                 setNewAlarm(context, appWidgetIds[i], Constants.DEFAULT_PAGE_NUM, true);
             }
         }
         
         super.onEnabled(context);
     }
 }
