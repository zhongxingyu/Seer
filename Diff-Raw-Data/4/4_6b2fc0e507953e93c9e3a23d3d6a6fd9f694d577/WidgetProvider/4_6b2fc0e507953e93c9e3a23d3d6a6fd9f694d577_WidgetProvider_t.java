 /*
  * Copyright (C) 2013 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.SystemClock;
 import android.text.format.DateUtils;
 import android.widget.RemoteViews;
 
 public class WidgetProvider extends AppWidgetProvider {
 
     private static final int RESULT_SUCCESS = 0;
     private static final int RESULT_FAILURE = -1;
     private static final int RESULT_NO_RACE = 1;
 
     private static final long UPDATE_INTERVAL = DateUtils.MINUTE_IN_MILLIS;
     private static final long UPDATE_FUDGE = 50; /* milliseconds */
     private static final long UPDATE_WARNING = (DateUtils.HOUR_IN_MILLIS * 2) + UPDATE_FUDGE;
 
     private static final String URL_PREFIX = "http://www.nascar.com/content/dam/nascar/logos/race/2013/SprintCup/";
 
     private static Race sRace;
     private static Bitmap sBitmap;
 
     @Override
     public void onReceive (final Context context, final Intent intent) {
         /* TODO: Handle schedule update intent */
         if (intent.hasExtra(Intent.EXTRA_ALARM_COUNT)) {
             Util.log("WidgetProvider.onReceive: Widget alarm");
             new UpdateWidgetTask().execute(context);
         } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
             Util.log("WidgetProvider.onReceive: Resetting alarm due to time change");
             setAlarm(context);
         } else
             super.onReceive(context, intent);
     }
 
     @Override
     public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
         Util.log("WidgetProvider.onUpdate: num=" + appWidgetIds.length);
 
         final RemoteViews rViews = new RemoteViews(context.getPackageName(), R.layout.widget_loading);
         appWidgetManager.updateAppWidget(appWidgetIds, rViews);
 
         new UpdateWidgetTask().execute(context);
 
         /* Set alarm to update widget when device is awake. This should really
          * be done in onEnabled, but Android doesn't always call onEnabled. */
         setAlarm(context);
     }
 
     @Override
     public void onDisabled(final Context context) {
         Util.log("WidgetProvider.onDisabled");
 
         final AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         am.cancel(getAlarmIntent(context));
     }
 
     private void setAlarm(final Context context) {
        /* No point setting alarm if no widgets */
        if (getInstalledWidgets(context).length == 0)
            return;

         /* Android relative time rounds down, so update needs to be early if anything */
         final long trigger = UPDATE_INTERVAL - (System.currentTimeMillis() % UPDATE_INTERVAL) - UPDATE_FUDGE;
 
         final AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + trigger,
                 UPDATE_INTERVAL, getAlarmIntent(context));
         Util.log("Initial trigger is " + trigger + " milliseconds from now");
     }
 
     private PendingIntent getAlarmIntent(final Context context) {
         final Intent intent = new Intent(context, WidgetProvider.class);
         return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     }
 
     private int[] getInstalledWidgets(final Context context) {
         final ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
         final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         return appWidgetManager.getAppWidgetIds(thisWidget);
     }
 
     private class UpdateWidgetTask extends AsyncTask<Context, Integer, Integer> {
         private Context mContext;
 
         @Override
         protected Integer doInBackground(final Context... context) {
             mContext = context[0];
             final Race race = Race.getNext(mContext, true, true);
 
             if (race == null)
                 return RESULT_NO_RACE;
 
             if ((sRace != null) && (sBitmap != null) && (sRace.getId() == race.getId()))
                 return RESULT_SUCCESS;
             sRace = race;
 
             try {
                 // FIXME: Use correct URL
                 final URL url = new URL(URL_PREFIX +
                         "Sprint_Unlimited_logo_fold.png/jcr:content/renditions/Sprint_Unlimited_logo_fold.png.main.png");
                 final HttpGet httpRequest = new HttpGet(url.toURI());
 
                 final HttpClient httpclient = new DefaultHttpClient();
                 final HttpResponse resp = httpclient.execute(httpRequest);
                 final int statusCode = resp.getStatusLine().getStatusCode();
 
                 switch(statusCode) {
                 case HttpStatus.SC_OK:
                     final HttpEntity entity = resp.getEntity();
                     final BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                     final InputStream input = b_entity.getContent();
 
                     sBitmap = BitmapFactory.decodeStream(input);
                     break;
                 default:
                     Util.log("Get logo failed (statusCode = " + statusCode + ")");
                     return RESULT_FAILURE;
                 }
             } catch (final Exception e) {
                 Util.log("Get logo failed with exception " + e);
                 return RESULT_FAILURE;
             }
 
             return RESULT_SUCCESS;
         }
 
         @Override
         protected void onPostExecute(final Integer result) {
             Util.log("UpdateWidgetTask.onPostExecute: result=" + result);
             RemoteViews rViews;
 
             switch (result) {
             case RESULT_SUCCESS:
                 final String nextRace = mContext.getString(R.string.widget_next_race, sRace.getStartRelative());
                 rViews = new RemoteViews(mContext.getPackageName(), R.layout.widget);
                 rViews.setTextViewText(R.id.when, nextRace);
                 rViews.setImageViewBitmap(R.id.race_logo, sBitmap);
 
                 if (sRace.isExhibition()) {
                     rViews.setInt(R.id.status, "setText", R.string.widget_exhibition);
                     rViews.setInt(R.id.widget_text_layout, "setBackgroundResource", R.drawable.widget_normal);
                 } else if (!sRace.inProgress()) {
                     rViews.setInt(R.id.status, "setText", R.string.widget_no_questions);
                     rViews.setInt(R.id.widget_text_layout, "setBackgroundResource", R.drawable.widget_normal);
                 } else {
                     final SharedPreferences acache =
                             mContext.getSharedPreferences(Questions.ACACHE, Activity.MODE_PRIVATE);
                     if (acache.contains(Questions.CACHE_PREFIX + sRace.getId())) {
                         rViews.setInt(R.id.status, "setText", R.string.widget_submitted);
                         rViews.setInt(R.id.widget_text_layout, "setBackgroundResource", R.drawable.widget_good);
                     } else if ((sRace.getStartTimestamp() - System.currentTimeMillis()) <= UPDATE_WARNING) {
                         rViews.setInt(R.id.status, "setText", R.string.widget_no_answers);
                         rViews.setInt(R.id.widget_text_layout, "setBackgroundResource", R.drawable.widget_warning);
                     } else {
                         rViews.setInt(R.id.status, "setText", R.string.widget_please_submit);
                         rViews.setInt(R.id.widget_text_layout, "setBackgroundResource", R.drawable.widget_normal);
                     }
                 }
 
                 Intent intent = new Intent(mContext, MainActivity.class);
                 intent.putExtra(MainActivity.INTENT_TAB, 1);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 PendingIntent pi = PendingIntent.getActivity(mContext, 0,
                         intent, PendingIntent.FLAG_UPDATE_CURRENT);
                 rViews.setOnClickPendingIntent(R.id.widget_text_layout, pi);
 
                 intent = new Intent(mContext, RaceActivity.class);
                 intent.putExtra(RaceActivity.INTENT_ID, sRace.getId());
                 intent.putExtra(RaceActivity.INTENT_ALARM, true);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 pi = PendingIntent.getActivity(mContext, 0,
                         intent, PendingIntent.FLAG_UPDATE_CURRENT);
                 rViews.setOnClickPendingIntent(R.id.race_logo, pi);
                 break;
             case RESULT_NO_RACE:
                 rViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_no_race);
 
                 intent = new Intent(mContext, MainActivity.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 pi = PendingIntent.getActivity(mContext, 0,
                         intent, PendingIntent.FLAG_UPDATE_CURRENT);
                 rViews.setOnClickPendingIntent(R.id.widget_full_layout, pi);
                 break;
             case RESULT_FAILURE:
             default:
                 rViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_error);
 
                 intent = new Intent(mContext, WidgetProvider.class);
                 intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                 intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getInstalledWidgets(mContext));
                 pi = PendingIntent.getBroadcast(mContext, 0,
                         intent, PendingIntent.FLAG_UPDATE_CURRENT);
                 rViews.setOnClickPendingIntent(R.id.widget_error_layout, pi);
                 break;
             }
 
             final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
             appWidgetManager.updateAppWidget(getInstalledWidgets(mContext), rViews);
         }
     }
 }
