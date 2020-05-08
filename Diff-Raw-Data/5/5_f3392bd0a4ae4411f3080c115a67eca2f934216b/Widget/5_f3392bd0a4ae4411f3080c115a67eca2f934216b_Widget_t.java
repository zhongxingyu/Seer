 package com.komamitsu.android.naver.topic;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.WeakHashMap;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.SystemClock;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class Widget extends AppWidgetProvider {
   private static final String TAG = Widget.class.getSimpleName();
   private static final int TOPIC_INTERVAL_SEC = 15;
   private static final int TOPIC_ERROR_WAIT_SEC = 60;
   private static final int TOPIC_REFRESH_SEC = 1200;
   private static final int REQUEST_CODE = 0;
   private static final String NAVER_JAPAN_URL = "http://www.naver.jp/";
   private static int newsIndex = 0;
   private static long lastUpdateTime = -1;
   private static List<Topic> newsList;
   private static PendingIntent pendingIntent;
   private static BroadcastReceiver wakeupReceiver;
   private static BroadcastReceiver sleepReceiver;
 
   @Override
   public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
     MyLog.d(TAG, "NaverJapanNewsWidget.onUpdate(): context=" + context);
     initReceivers(context);
     super.onUpdate(context, appWidgetManager, appWidgetIds);
   }
 
   private void initReceivers(Context context) {
     // set screen on receiver
     if (wakeupReceiver != null)
       context.unregisterReceiver(wakeupReceiver);
 
     wakeupReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
         MyLog.d(TAG, "Waking up!");
         setAlarm(context);
       }
     };
     context.getApplicationContext().registerReceiver(wakeupReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
 
     // set screen off receiver
     if (sleepReceiver != null)
       context.unregisterReceiver(sleepReceiver);
 
     sleepReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
         MyLog.d(TAG, "Going to sleep...");
         cancelAlarm(context);
       }
     };
     context.getApplicationContext().registerReceiver(sleepReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
 
     setAlarm(context);
   }
 
   @Override
   public void onReceive(Context context, Intent intent) {
     String action = intent.getAction();
     MyLog.d(TAG, "NaverJapanNewsWidget.onReceive(): context=" + context + ", intent.action=" + action);
     if (action == null || action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
       updateView(context);
     }
     super.onReceive(context, intent);
   }
 
   public static Topic getNextNews() {
     boolean shouldDownLoadNews = newsList == null;
     long now = System.currentTimeMillis();
     if (now > lastUpdateTime + TOPIC_REFRESH_SEC * 1000) {
       Log.i(TAG, "getNextNews(): refresh newsList");
       lastUpdateTime = now;
       shouldDownLoadNews = true;
     }
 
     if (shouldDownLoadNews) {
       final DefaultHttpClient client = new DefaultHttpClient();
       try {
         InputStream topPageContent = Utils.getInputStreamViaHttp(client, NAVER_JAPAN_URL);
         if (topPageContent != null) {
           Extractor extractor = Extractor.getInstance();
           newsList = extractor.extract(topPageContent);
         }
       } catch (ParseException e) {
         Log.e(TAG, "Failed to parse NAVER Japan top page (NaverJapanNewsParseException)", e);
       } finally {
         if (client != null && client.getConnectionManager() != null) {
           client.getConnectionManager().shutdown();
         }
       }
     }
 
     if (newsList == null) {
       try {
         Thread.sleep(TOPIC_ERROR_WAIT_SEC * 1000);
       } catch (InterruptedException e) {
       }
       return null;
     }
 
     Topic news = newsList.get(newsIndex);
     newsIndex++;
     newsIndex %= newsList.size();
 
     return news;
   }
 
   @Override
   public void onDisabled(Context context) {
     MyLog.d(TAG, "NaverJapanNewsWidget.onDisabled(): context=" + context);
     cancelAlarm(context);
 
     if (wakeupReceiver != null) {
       context.unregisterReceiver(wakeupReceiver);
       wakeupReceiver = null;
     }
 
     if (sleepReceiver != null) {
       context.unregisterReceiver(sleepReceiver);
       sleepReceiver = null;
     }
 
     super.onDisabled(context);
   }
 
   private void setAlarm(Context context) {
     MyLog.d(TAG, "NaverJapanNewsWidget.setAlarm() context=" + context);
     final Intent intent = new Intent(context, Widget.class);
     if (pendingIntent == null) {
       // service = PendingIntent.getService(context, REQUEST_CODE, intent,
       // PendingIntent.FLAG_CANCEL_CURRENT);
       pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
     }
     long firstTime = SystemClock.elapsedRealtime();
     AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     long interval = TOPIC_INTERVAL_SEC * 1000;
     am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, pendingIntent);
   }
 
   private void cancelAlarm(Context context) {
     AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     am.cancel(pendingIntent);
   }
 
   private static WeakHashMap<String, Bitmap> imageCache = new WeakHashMap<String, Bitmap>();
 
   public void updateView(Context context) {
     RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_word);
     Topic news = getNextNews();
     if (news == null)
       return;
     // Log.i(TAG, "Next news: " + news);
 
     final DefaultHttpClient client = new DefaultHttpClient();
     try {
       if (news.getImage() == null) {
         Bitmap b = null;
         String urlOfImage = news.getUrlOfImage();
         // debug
         if (Config.isDebug) {
           for (String key : imageCache.keySet())
             Log.d(TAG, "#### " + key);
         }
         if (imageCache.containsKey(urlOfImage)) {
           b = imageCache.get(urlOfImage);
           MyLog.d(TAG, "Found a image in cache: " + urlOfImage);
         } else {
           try {
             InputStream imageStream = Utils.getInputStreamViaHttp(client, urlOfImage);
             if (imageStream != null) {
               b = BitmapFactory.decodeStream(imageStream);
               MyLog.d(TAG, "Downloaded the news image: " + urlOfImage);
               imageCache.put(urlOfImage, b);
             }
             news.setImage(b);
           } catch (Exception e) {
             Log.e(TAG, "Getting an image error, url=" + urlOfImage, e);
           }
         }
       }
 
       if (news.getImage() != null) {
         updateViews.setImageViewBitmap(R.id.news_image, news.getImage());
       } else {
         updateViews.setImageViewResource(R.id.news_image, R.drawable.noimage);
       }
       String title = news.getRank() + "位 :  " + news.getTitle();
       updateViews.setTextViewText(R.id.news_title, title);
       updateViews.setTextViewText(R.id.news_detail, news.getDetail());
       updateViews.setTextViewText(R.id.news_time, news.getTime());
 
       ComponentName thisWidget = new ComponentName(context, Widget.class);
 
       Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrlOfLink()));
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, webIntent, 0);
       updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
 
       AppWidgetManager manager = AppWidgetManager.getInstance(context);
       manager.updateAppWidget(thisWidget, updateViews);
     } finally {
       if (client != null && client.getConnectionManager() != null) {
         client.getConnectionManager().shutdown();
       }
     }
   }
 }
