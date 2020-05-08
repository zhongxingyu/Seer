 package com.ouchadam.fang.debug;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 import com.novoda.notils.java.Collections;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.parsing.ChannelFinder;
 import com.ouchadam.fang.parsing.PodcastParser;
 import com.ouchadam.fang.persistance.ChannelPersister;
 import com.ouchadam.fang.persistance.FangProvider;
 import com.ouchadam.fang.persistance.Query;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 import com.ouchadam.fang.presentation.item.DatabaseCounter;
 import com.ouchadam.fang.presentation.item.LastUpdatedManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 public class ChannelFeedDownloadService extends Service {
 
     private static final int NOTIFICATION_ID = 0xAC;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         List<Feed> feeds = Collections.newArrayList();
         FeedServiceInfo from = FeedServiceInfo.from(intent.getExtras());
         switch (from.getType()) {
             case ADD:
                 feeds = from.getUrlsToAdd();
                 break;
             case REFRESH:
                 feeds = getSubscribedChannelUrls();
                 break;
         }
 
         if (!feeds.isEmpty()) {
             showNotification();
             downloadAndPersistPodcastFeeds(feeds);
         }
         return START_STICKY;
     }
 
     private void showNotification() {
         Notification notification = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.play_button).setContentTitle("Refreshing podcast feeds").build();
         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.notify(NOTIFICATION_ID, notification);
     }
 
     private List<Feed> getSubscribedChannelUrls() {
         Query query = getQueryValues();
         Cursor cursor = getContentResolver().query(query.uri, query.projection, query.selection, query.selectionArgs, query.sortOrder);
 
         List<Feed> feeds = Collections.newArrayList();
         if (cursor != null && cursor.moveToFirst()) {
             do {
                 Feed feed = new Feed();
                 feed.url = cursor.getString(cursor.getColumnIndex(Tables.Channel.URL.name()));
                 feed.channelTitle = cursor.getString(cursor.getColumnIndex(Tables.Channel.CHANNEL_TITLE.name()));
                 feed.oldItemCount = cursor.getInt(cursor.getColumnIndex(Tables.Channel.NEW_ITEM_COUNT.name()));
                 feeds.add(feed);
             } while (cursor.moveToNext());
         }
         if (cursor != null) {
             cursor.close();
         }
         return feeds;
     }
 
     private Query getQueryValues() {
         return new Query.Builder().withUri(FangProvider.getUri(Uris.CHANNEL)).build();
     }
 
     public void downloadAndPersistPodcastFeeds(List<Feed> feeds) {
         // TODO Move to thread pool
         final ThreadTracker threadTracker = new ThreadTracker(feeds.size(), threadsCompleteListener);
         for (final Feed feed : feeds) {
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     getPodcastFrom(feed, threadTracker);
                 }
             }).start();
         }
     }
 
     private void getPodcastFrom(Feed feed, ThreadTracker threadTracker) {
         int currentItemCount = getCurrentItemCount(feed) - feed.oldItemCount;
         PodcastParser podcastParser = PodcastParser.newInstance(ChannelFinder.newInstance());
         try {
            Log.e("!!!", "Fetching : " + feed.url);
             InputStream urlInputStream = getInputStreamFrom(feed.url);
             podcastParser.parse(urlInputStream);
             new ChannelPersister(getContentResolver()).persist(podcastParser.getResult(), feed.url, currentItemCount);
            Log.e("!!!", "Fetched : " + feed.url);
         } catch (IOException e) {
             broadcastFailure(e.getMessage());
         }
         threadTracker.threadFinished();
     }
 
     private int getCurrentItemCount(Feed feed) {
         if (!feed.hasChannelTitle()) {
             return 0;
         }
         return new DatabaseCounter(
                 getContentResolver(),
                 Uris.FULL_ITEM,
                 new String[]{Tables.Item.CHANNEL.name()},
                 Tables.Channel.CHANNEL_TITLE.name() + "=?",
                 new String[]{feed.channelTitle}
         ).getCurrentCount();
     }
 
     private InputStream getInputStreamFrom(String url) throws IOException {
         URL urlForStream = new URL(url);
         return urlForStream.openStream();
     }
 
     private void broadcastFailure(String failureMessage) {
         // TODO
     }
 
     private static class ThreadTracker {
 
         private final OnAllThreadsComplete onAllThreadsComplete;
         private int threadCount;
 
         interface OnAllThreadsComplete {
             void onFinish();
         }
 
         public ThreadTracker(int threadCount, OnAllThreadsComplete onAllThreadsComplete) {
             this.threadCount = threadCount;
             this.onAllThreadsComplete = onAllThreadsComplete;
         }
 
         public synchronized void threadFinished() {
             if (--threadCount == 0) {
                 onAllThreadsComplete.onFinish();
             }
         }
 
     }
 
     private final ThreadTracker.OnAllThreadsComplete threadsCompleteListener = new ThreadTracker.OnAllThreadsComplete() {
         @Override
         public void onFinish() {
             // TODO show notification with how many new items
             LastUpdatedManager.from(ChannelFeedDownloadService.this).setLastUpdated();
             dismissNotification();
             stopSelf();
         }
     };
 
     private void dismissNotification() {
         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.cancel(NOTIFICATION_ID);
 
     }
 }
