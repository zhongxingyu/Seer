 /*******************************************************************************
  * Copyright (c) 2011 Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  *  to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  ******************************************************************************/
 
 package com.kopysoft.chronos.content;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.RemoteViews;
 import com.kopysoft.chronos.R;
 import com.kopysoft.chronos.adapter.clock.PayPeriodAdapterList;
 import com.kopysoft.chronos.enums.Defines;
 import com.kopysoft.chronos.types.Job;
 import com.kopysoft.chronos.types.Punch;
 import com.kopysoft.chronos.types.Task;
 import com.kopysoft.chronos.types.holders.PunchTable;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ethan
  * Date: 3/15/12
  * Time: 7:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EnableWidget extends AppWidgetProvider {
 
    private static String SEND_CLOCK = "com.kopysoft.chronos.service.EnableWidget.CLOCK";
    public static String UPDATE_FROM_APP = "com.kopysoft.chronos.service.EnableWidget.UPDATE";
     private static String TAG = Defines.TAG + " - Widget";
     private static final boolean printDebugMessages = Defines.DEBUG_PRINT;
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 
         Log.d(TAG, "onUpdate");
         final int N = appWidgetIds.length;
 
         // Perform this loop procedure for each App Widget that belongs to this provider
         for (int i = 0; i < N; i++) {
             int appWidgetId = appWidgetIds[i];
             //Intent intent = new Intent(context, com.kopysoft.MorseMessenger.EnableWidget.class);
             Intent intent = new Intent(context, com.kopysoft.chronos.content.EnableWidget.class);
             intent.setAction(SEND_CLOCK);
             PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
 
             Chronos chron = new Chronos(context);
             Job currentJob = chron.getAllJobs().get(0);
             PunchTable punchTable = chron.getAllPunchesForThisPayPeriodByJob(currentJob);
             chron.close();
 
             Duration dur = PayPeriodAdapterList.getTime(punchTable.getPunchPair(DateTime.now()), true);
 
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
             if (dur.getMillis() < 0) {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_disabled);
             } else {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_enabled);
             }
             views.setOnClickPendingIntent(R.id.imageButton, pendingIntent);
 
             // Tell the AppWidgetManager to perform an update on the current app widget
             appWidgetManager.updateAppWidget(appWidgetId, views);
         }
     }
 
     @Override
     public void onEnabled(Context context) {
         super.onEnabled(context);
         Log.d(TAG, "onEnable");
 
         Chronos chron = new Chronos(context);
         Job currentJob = chron.getAllJobs().get(0);
         PunchTable punchTable = chron.getAllPunchesForThisPayPeriodByJob(currentJob);
         chron.close();
 
         Duration dur = PayPeriodAdapterList.getTime(punchTable.getPunchPair(DateTime.now()), true);
 
         RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
         if (dur.getMillis() < 0) {
             views.setImageViewResource(R.id.imageButton, R.drawable.widget_disabled);
         } else {
             views.setImageViewResource(R.id.imageButton, R.drawable.widget_enabled);
         }
 
         Intent runIntent = new Intent().setClass(context,
                 com.kopysoft.chronos.content.NotificationBroadcast.class);
         runIntent.putExtra("timeToday", dur.getMillis());
         context.sendBroadcast(runIntent);
 
         AppWidgetManager manager = AppWidgetManager.getInstance(context);
         manager.updateAppWidget(new ComponentName(context, EnableWidget.class), views);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.d(TAG, "onRecieve");
         AppWidgetManager manager = AppWidgetManager.getInstance(context);
         RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
 
         Intent intentSend = new Intent(context, com.kopysoft.chronos.content.EnableWidget.class);
         intentSend.setAction(SEND_CLOCK);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentSend, 0);
 
         Chronos chron = new Chronos(context);
         Job currentJob = chron.getAllJobs().get(0);
         PunchTable punchTable = chron.getAllPunchesForThisPayPeriodByJob(currentJob);
         Task defaultTask = chron.getAllTasks().get(0);
 
         Duration dur = PayPeriodAdapterList.getTime(punchTable.getPunchPair(DateTime.now()), true);
 
         if (intent.getAction().compareTo(SEND_CLOCK) == 0) {
 
            if ( dur.getMillis() < 0) {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_enabled);
             } else {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_disabled);
             }
 
             Punch newPunch = new Punch(currentJob, defaultTask, DateTime.now());
             chron.insertPunch(newPunch);
 
             punchTable = chron.getAllPunchesForThisPayPeriodByJob(currentJob);
             defaultTask = chron.getAllTasks().get(0);
 
             dur = PayPeriodAdapterList.getTime(punchTable.getPunchPair(DateTime.now()));
 
             //Send intent for the notification bar
             Intent runIntent = new Intent().setClass(context,
                     com.kopysoft.chronos.content.NotificationBroadcast.class);
             runIntent.putExtra("timeToday", dur.getMillis());
             context.sendBroadcast(runIntent);
             //End intent
 
         } else if (intent.getAction().compareTo(UPDATE_FROM_APP) == 0) {
            if (dur.getMillis() > 0) {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_disabled);
             } else {
                 views.setImageViewResource(R.id.imageButton, R.drawable.widget_enabled);
             }
         } else {
             super.onReceive(context, intent);
             return;
         }
 
         Intent runIntent = new Intent().setClass(context,
                 com.kopysoft.chronos.content.NotificationBroadcast.class);
         runIntent.putExtra("timeToday", dur.getMillis());
         context.sendBroadcast(runIntent);
 
         chron.close();
 
         if (printDebugMessages) Log.d(TAG, intent.getAction());
         views.setOnClickPendingIntent(R.id.imageButton, pendingIntent);
         manager.updateAppWidget(new ComponentName(context, EnableWidget.class), views);
 
     }
 }
