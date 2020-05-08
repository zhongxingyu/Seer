 package com.chess.saldo;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 import com.bcseime.android.chess.saldo2.R;
 import com.chess.saldo.service.entities.Saldo;
 import com.chess.saldo.service.entities.SaldoType;
 
 import java.util.Arrays;
 
 /**
  * Created by bjorncs on 08.06.13.
  */
 public abstract class WidgetProviders extends AppWidgetProvider {
 
     public static class Large extends WidgetProviders {
 
         private static RemoteViews updateWidgetView(Context context) {
             Saldo saldo = new SettingsManager(context).getSaldo();
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.large_widget_layout);
             updateSaldoItem(views, R.id.dataProgress, R.id.dataProgressContainer, R.id.dataValue, Math.round(saldo.dataLeft), Math.round(saldo.dataTotal));
             updateSaldoItem(views, R.id.minutesProgress, R.id.minutesProgressContainer, R.id.minutesValue, saldo.minutesLeft, saldo.minutesTotal);
             updateSaldoItem(views, R.id.mmsProgress, R.id.mmsProgressContainer, R.id.mmsValue, saldo.mmsLeft, saldo.mmsTotal);
             updateSaldoItem(views, R.id.smsProgress, R.id.smsProgressContainer, R.id.smsValue, saldo.smsLeft, saldo.smsTotal);
 
             Log.d("CHESS_SALDO", "Widget views updated with saldo information");
             if (saldo.moneyUsed == -1) {
                 views.setTextViewText(R.id.cashValue, "-");
             } else {
                 views.setTextViewText(R.id.cashValue, Integer.toString(Math.round(saldo.moneyUsed)));
             }
             Intent i = new Intent(context.getApplicationContext(), MainActivity.class);
             PendingIntent updateServiceIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
             views.setOnClickPendingIntent(R.id.widget_layout, updateServiceIntent);
             return views;
         }
 
 
         private static void updateSaldoItem(RemoteViews remoteViews, int progressId, int progressContainerId, int textId, int progressValue, int progressMax) {
             if (progressValue == -1 || progressMax == -1) {
                 remoteViews.setTextViewText(textId, "-");
                 // Workaround for issue 11040 (https://code.google.com/p/android/issues/detail?id=11040)
                 remoteViews.setViewVisibility(progressContainerId, View.INVISIBLE);
             } else {
                 remoteViews.setTextViewText(textId, Integer.toString(progressValue));
                 // Workaround for issue 11040 (https://code.google.com/p/android/issues/detail?id=11040)
                 remoteViews.setViewVisibility(progressContainerId, View.VISIBLE);
                 remoteViews.setProgressBar(progressId, progressMax, progressValue, false);
             }
         }
 
         @Override
         protected RemoteViews getUpdatedRemoteView(Context context, int widgetId) {
             return updateWidgetView(context);
         }
     }
 
     public static class Small extends WidgetProviders {
 
         private static RemoteViews updateWidgetView(Context context, int widgetId) {
             RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.small_widget_layout);
 
             SettingsManager settings = new SettingsManager(context);
             Saldo saldo = settings.getSaldo();
             SaldoType type = settings.getWidgetType(widgetId);
             int progress = saldo.getProgressFor(type);
             int max = saldo.getMaxFor(type);
 
             remoteViews.setTextViewText(R.id.lblSaldoType, type.widgetName);
             if (progress == -1) {
                 remoteViews.setTextViewText(R.id.lblSaldoValue, "-");
                 // Workaround for issue 11040 (https://code.google.com/p/android/issues/detail?id=11040)
                 remoteViews.setViewVisibility(R.id.pgrSaldoContainer, View.INVISIBLE);
             } else {
                 remoteViews.setTextViewText(R.id.lblSaldoValue, Integer.toString(progress));
                 if (type == SaldoType.MONEY) {
                     // Workaround for issue 11040 (https://code.google.com/p/android/issues/detail?id=11040)
                     remoteViews.setViewVisibility(R.id.pgrSaldoContainer, View.INVISIBLE);
                 } else {
                     remoteViews.setViewVisibility(R.id.pgrSaldo, View.VISIBLE);
                    remoteViews.setProgressBar(R.id.pgrSaldo, max, progress, false);
                 }
             }
 
             Intent i = new Intent(context.getApplicationContext(), MainActivity.class);
             PendingIntent updateServiceIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
             remoteViews.setOnClickPendingIntent(R.id.widget_layout, updateServiceIntent);
             return remoteViews;
         }
 
         @Override
         protected RemoteViews getUpdatedRemoteView(Context context, int widgetId) {
             return updateWidgetView(context, widgetId);
         }
     }
 
     public static void updateAllWidgets(Context context) {
         AppWidgetManager manager = AppWidgetManager.getInstance(context);
 
         RemoteViews largeView = Large.updateWidgetView(context);
         int[] largeIds = manager.getAppWidgetIds(new ComponentName(context, Large.class));
         for (int appWidgetId : largeIds) {
             manager.updateAppWidget(appWidgetId, largeView);
         }
 
 
         int[] smallIds = manager.getAppWidgetIds(new ComponentName(context, Small.class));
         for (int appWidgetId : smallIds) {
             RemoteViews smallView = Small.updateWidgetView(context, appWidgetId);
             manager.updateAppWidget(appWidgetId, smallView);
         }
     }
 
     public static boolean hasWidgets(Context context) {
         AppWidgetManager manager = AppWidgetManager.getInstance(context);
         return manager.getAppWidgetIds(new ComponentName(context, Large.class)).length > 0 ||
                 manager.getAppWidgetIds(new ComponentName(context, Small.class)).length > 0;
     }
 
     protected abstract RemoteViews getUpdatedRemoteView(Context context, int widgetId);
 
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         Log.d("CHESS_SALDO", "OnDeleted() called on widget provider, widgetIds = " + Arrays.toString(appWidgetIds));
         super.onDeleted(context, appWidgetIds);
     }
 
     @Override
     public void onDisabled(Context context) {
         super.onDisabled(context);
         Log.d("CHESS_SALDO", "OnDisabled() called on widget provider");
     }
 
     @Override
     public void onEnabled(Context context) {
         super.onEnabled(context);
         Log.d("CHESS_SALDO", "OnEnabled() called on widget provider");
         ConnectivityBroadcastReceiver.setUpdateAlarm(context);
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         super.onUpdate(context, appWidgetManager, appWidgetIds);
         Log.d("CHESS_SALDO", "OnUpdate() was called for widgets");
         for (int widgetId : appWidgetIds) {
             RemoteViews remoteViews = getUpdatedRemoteView(context, widgetId);
             appWidgetManager.updateAppWidget(widgetId, remoteViews);
         }
     }
 }
