 package com.maliugin.connectionmanager.widget;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 import com.maliugin.connectionmanager.R;
 import com.maliugin.connectionmanager.mobilemanager.Android23ConnectionManager;
 import com.maliugin.connectionmanager.mobilemanager.MobileConnectionManager;
 import com.maliugin.connectionmanager.mobilemanager.ReflectionAPIConnectionManager;
 
 public class InternetWidgetProvider extends AppWidgetProvider {
     public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
     public static final int ENABLED_ICON = R.drawable.box_green;
     public static final int DISABLED_ICON = R.drawable.box_red;
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         RemoteViews views = createWidgetView(context);
         setWidgetResources(context, views);
         appWidgetManager.updateAppWidget(appWidgetIds, views);
         super.onUpdate(context, appWidgetManager, appWidgetIds);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         final String action = intent.getAction();
         MobileConnectionManager apnManager = createAPNManager(context);
         RemoteViews views = createWidgetView(context);
         setWidgetResources(context, views);
         if (ACTION_WIDGET_RECEIVER.equals(action)) {
             boolean isEnabled = apnManager.isConnectionEnabled();
             showMessage(context, isEnabled);
             setStatusImg(!isEnabled, views);
             apnManager.switchConnection();
         }
         AppWidgetManager manager = AppWidgetManager.getInstance(context);
         int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, InternetWidgetProvider.class));
         manager.updateAppWidget(widgetIds, views);
         super.onReceive(context, intent);
     }
 
     protected void setWidgetResources(Context context, RemoteViews views) {
         Intent active = new Intent(context, InternetWidgetProvider.class);
         active.setAction(ACTION_WIDGET_RECEIVER);
         PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
         views.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
         setStatusImg(createAPNManager(context), views);
     }
 
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

     protected void showMessage(Context context, boolean APNEnabled) {
         int message = APNEnabled ? R.string.internet_disabled : R.string.internet_enabled;
         Toast toastMessage = Toast.makeText(context, message, Toast.LENGTH_SHORT);
         toastMessage.show();
     }
 
     protected RemoteViews createWidgetView(Context context) {
         return new RemoteViews(context.getPackageName(), R.layout.main);
     }
 
     protected MobileConnectionManager createAPNManager(Context context) {
         if (Integer.valueOf(Build.VERSION.SDK) >= 9) {
             return new Android23ConnectionManager(context);
         } else {
             return new ReflectionAPIConnectionManager(context); 
         }
     }
 
     protected void setStatusImg(MobileConnectionManager apnManager, RemoteViews views) {
         setStatusImg(apnManager.isConnectionEnabled(), views);
     }
 
     protected void setStatusImg(boolean isConnected, RemoteViews views) {
         int buttonImg = isConnected ? ENABLED_ICON : DISABLED_ICON;
         views.setImageViewResource(R.id.widget_button, buttonImg);
     }
 }
