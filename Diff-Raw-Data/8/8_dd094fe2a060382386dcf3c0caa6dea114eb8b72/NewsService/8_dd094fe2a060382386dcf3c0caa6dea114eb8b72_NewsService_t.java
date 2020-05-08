 package com.osastudio.newshub;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.data.AppDeadline;
 import com.osastudio.newshub.data.AppProperties;
 import com.osastudio.newshub.data.NewsMessage;
 import com.osastudio.newshub.data.NewsMessageList;
 import com.osastudio.newshub.data.NewsMessageSchedule;
 import com.osastudio.newshub.library.PreferenceManager;
 import com.osastudio.newshub.library.UpgradeManager;
 import com.osastudio.newshub.net.AppDeadlineApi;
 import com.osastudio.newshub.net.AppPropertiesApi;
 import com.osastudio.newshub.net.NewsMessageApi;
 import com.osastudio.newshub.utils.Utils;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.text.TextUtils;
 import android.widget.Toast;
 
 public class NewsService extends Service {
 
    private static final String TAG = "NewsService";
 
    private static final String ACTION_CHECK_NEWS_MESSAGE_SCHEDULE = "com.osastudio.newshub.action.CHECK_NEWS_MESSAGE_SCHEDULE";
    private static final String ACTION_PULL_NEWS_MESSAGE = "com.osastudio.newshub.action.PULL_NEWS_MESSAGE";
    private static final long RETRY_DELAYED_MILLIS = 10 * 60 * 1000;
    private static final int NEWS_MESSAGE_NOTIFICATION = 1300;
 
    private Handler mHandler = new Handler();
    private UpgradeManager mUpgradeManager;
    private boolean mCheckingNewVersion = false;
    private AppDeadline mAppDeadline;
    private INewsServiceBinder mBinder = new INewsServiceBinder();
    private NewsReceiver mNewsReceiver = new NewsReceiver();
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       Utils.logi(TAG, "____________onStartCommand");
 
       scheduleCheckingNewsMessageSchedule();
 
       return START_STICKY;
    }
 
    @Override
    public void onCreate() {
       super.onCreate();
       Utils.logi(TAG, "____________onCreate");
 
       registerNewsReceiver();
 
       mHandler.postDelayed(new Runnable() {
          public void run() {
             mHandler.postDelayed(this, 10000);
             Utils.logi(TAG,
                   "______________I am alive..." + System.currentTimeMillis());
          }
       }, 10000);
    }
 
    @Override
    public IBinder onBind(Intent intent) {
       Utils.logi(TAG, "____________onBind");
       return mBinder;
    }
 
    @Override
    public boolean onUnbind(Intent intent) {
       Utils.logi(TAG, "____________onUnbind");
       return super.onUnbind(intent);
    }
 
    @Override
    public void onDestroy() {
       super.onDestroy();
       Utils.logi(TAG, "____________onDestroy");
 
       unregisterNewsReceiver();
 
       Intent selfIntent = new Intent();
       selfIntent.setClass(this, NewsService.class);
       startService(selfIntent);
    }
 
    private class INewsServiceBinder extends INewsService.Stub {
 
       @Override
       public void downloadApk(String apkUrl) {
          if (mUpgradeManager == null) {
             mUpgradeManager = new UpgradeManager(NewsService.this);
             mUpgradeManager.setHanlder(mHandler);
          }
          mUpgradeManager.download(apkUrl);
       }
 
       @Override
       public boolean isDownloadingApk() {
          return (mUpgradeManager != null) ? mUpgradeManager.isDownloading()
                : false;
       }
 
       @Override
       public void checkNewVersion() {
          if (!mCheckingNewVersion) {
             mCheckingNewVersion = true;
             new AppPropertiesTask().execute(NewsService.this);
          }
       }
 
       public boolean isCheckingNewVersion() {
          return mCheckingNewVersion;
       }
 
       @Override
       public boolean hasNewVersion(AppProperties properties,
             boolean notifyIfNoNewVersion) {
          PackageInfo pkgInfo = Utils.getPackageInfo(NewsService.this);
          if (properties.isUpgradeAvailable(pkgInfo)
                || properties.isUpgradeNecessary(pkgInfo)) {
             Intent intent = new Intent();
             intent.setClass(NewsService.this, UpgradeActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                   | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                   | Intent.FLAG_ACTIVITY_NO_HISTORY);
             intent.putExtra(AppProperties.EXTRA_APP_PROPERTIES, properties);
             startActivity(intent);
             return true;
          } else {
             if (notifyIfNoNewVersion) {
                Toast.makeText(NewsService.this,
                      getString(R.string.no_new_version), Toast.LENGTH_LONG)
                      .show();
             }
             return false;
          }
       }
 
       @Override
       public boolean hasExpired() {
          return (mAppDeadline != null) ? mAppDeadline.hasExpired()
                : new AppDeadline().hasExpired();
       }
 
       @Override
       public void checkAppDeadline() {
          new AppDeadlineTask().execute(NewsService.this);
       }
 
       @Override
       public void checkNewsMessage(String userId) {
          String str = ((NewsApp) NewsService.this.getApplication())
                .getPrefsManager().getMessageScheduleString();
          NewsMessageSchedule schedule = NewsMessageSchedule.parseString(str);
          if (schedule != null) {
             analyzeNewsMessage(userId, schedule);
          } else {
             requestNewsMessageSchedule(userId);
          }
       }
 
    };
 
    private void analyzeNewsMessage(String userId, NewsMessageSchedule schedule) {
       if (schedule.isPullingAllowed()) {
          int count = schedule.getCount();
          if (count == 0) {
             if (schedule.pullNow()) {
                requestNewsMessageList(userId, count + 1, true,
                      RETRY_DELAYED_MILLIS);
             } else {
                schedulePullingNewsMessage(userId, schedule.getScheduleMillis(),
                      count + 1);
             }
          } else if (count == 1) {
             requestNewsMessageList(userId, count + 1, true, 0);
          } else if (count == 2) {
             requestNewsMessageList(userId, count + 1);
          }
       }
    }
 
    private void requestNewsMessageSchedule(String userId) {
       new NewsMessagescheduleTask(userId).execute(NewsService.this);
    }
 
    private void checkNewsMessageSchedule() {
       PreferenceManager prefsManager = ((NewsApp) getApplication())
             .getPrefsManager();
       String userId = prefsManager.getUserId();
       if (!TextUtils.isEmpty(userId)) {
          String str = prefsManager.getMessageScheduleString();
          NewsMessageSchedule schedule = NewsMessageSchedule.parseString(str);
          if (!schedule.isToday()) {
             requestNewsMessageSchedule(userId);
          }
       }
    }
 
    private void scheduleCheckingNewsMessageSchedule() {
       AlarmManager alarmManager = (AlarmManager) NewsService.this
             .getSystemService(Context.ALARM_SERVICE);
       Intent intent = new Intent();
       intent.setAction(ACTION_CHECK_NEWS_MESSAGE_SCHEDULE);
       intent.setClass(NewsService.this, NewsService.class);
       PendingIntent pi = PendingIntent.getBroadcast(NewsService.this, 0,
            intent, PendingIntent.FLAG_CANCEL_CURRENT);
       alarmManager.cancel(pi);
       alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
             System.currentTimeMillis(), 10 * 60 * 1000, pi);
    }
 
    private void requestNewsMessageList(String userId, int count,
          boolean retryIfFailed, long retryDelayedMillis) {
       NewsMessageListTask task = new NewsMessageListTask(userId);
       task.setCount(count);
       task.setRetryIfFailed(retryIfFailed);
       task.setRetryDelayedMillis(retryDelayedMillis);
       task.execute(NewsService.this);
    }
 
    private void requestNewsMessageList(String userId, int count) {
       requestNewsMessageList(userId, count, false, 0);
    }
 
    private void schedulePullingNewsMessage(String userId, long scheduleMillis,
          int count) {
       AlarmManager alarmManager = (AlarmManager) NewsService.this
             .getSystemService(Context.ALARM_SERVICE);
       Intent intent = new Intent();
       intent.setAction(ACTION_PULL_NEWS_MESSAGE);
       intent.setClass(NewsService.this, NewsService.class);
       intent.setData(Uri.parse("userId:" + userId));
       intent.putExtra("userId", userId);
       intent.putExtra("count", count);
       PendingIntent pi = PendingIntent.getBroadcast(NewsService.this, 0,
            intent, PendingIntent.FLAG_CANCEL_CURRENT);
       alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleMillis, pi);
    }
 
    private void notifyNewsMessage(NewsMessageList messages) {
       for (int i = 0; i < messages.getList().size(); i++) {
          NewsMessage msg = messages.getList().get(i);
          Notification noti = new Notification(R.drawable.noti,
                getString(R.string.news_message_prompt_title),
                System.currentTimeMillis());
          PendingIntent pi = PendingIntent.getActivity(this, 0,
                getNewsMessageLaunchIntent(msg),
                PendingIntent.FLAG_UPDATE_CURRENT);
          noti.setLatestEventInfo(this, msg.getContent(),
                getMsgTitleByType(msg), pi);
          noti.flags |= Notification.FLAG_AUTO_CANCEL;
          NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
          manager.notify(NEWS_MESSAGE_NOTIFICATION + i, noti);
       }
    }
 
    private Intent getNewsMessageLaunchIntent(NewsMessage msg) {
       Intent intent = new Intent();
       intent.setClass(this, CategoryActivity.class);
       Bundle extras = new Bundle();
       extras.putInt(CategoryActivity.MESSAGE_SEND_TYPE, msg.getType());
       extras.putString(CategoryActivity.MESSAGE_SERVICE_ID, msg.getId());
       intent.putExtras(extras);
      intent.setData(Uri.parse("msg:" + msg));
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       return intent;
    }
 
    private String getMsgTitleByType(NewsMessage msg) {
       switch (msg.getType()) {
       case NewsMessage.MSG_TYPE_NOTICE:
          return getString(R.string.msg_prompt_title_notice);
 
       case NewsMessage.MSG_TYPE_NOTICE_FEEDBACK:
          return getString(R.string.msg_prompt_title_notice_feedback);
 
       case NewsMessage.MSG_TYPE_DAILY_REMINDER:
          return getString(R.string.msg_prompt_title_daily_reminder);
 
       case NewsMessage.MSG_TYPE_COLUMNIST:
          return getString(R.string.msg_prompt_title_colomnist);
 
       case NewsMessage.MSG_TYPE_RECOMMEND:
          return getString(R.string.msg_prompt_title_recommend);
 
       default:
          return "";
       }
    }
 
    private void clearNewsMessageNotification() {
       NotificationManager manager = (NotificationManager) getApplicationContext()
             .getSystemService(Context.NOTIFICATION_SERVICE);
       manager.cancel(NEWS_MESSAGE_NOTIFICATION);
    }
 
    private class NewsReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          Utils.logi(TAG, "_______________onReceive: " + action);
          if (ACTION_CHECK_NEWS_MESSAGE_SCHEDULE.equals(action)) {
             checkNewsMessageSchedule();
          } else if (ACTION_PULL_NEWS_MESSAGE.equals(action)) {
             Uri data = intent.getData();
             String userId = intent.getStringExtra("userId");
             int count = intent.getIntExtra("count", 0);
             if (count == 0) {
                requestNewsMessageList(userId, count, true, RETRY_DELAYED_MILLIS);
             } else {
                requestNewsMessageList(userId, count);
             }
          }
       }
    };
 
    private void registerNewsReceiver() {
       IntentFilter filter = new IntentFilter();
       filter.addAction(ACTION_PULL_NEWS_MESSAGE);
       registerReceiver(mNewsReceiver, filter);
    }
 
    private void unregisterNewsReceiver() {
       unregisterReceiver(mNewsReceiver);
    }
 
    private class AppPropertiesTask extends
          AsyncTask<Context, Integer, AppProperties> {
       @Override
       protected AppProperties doInBackground(Context... params) {
          return AppPropertiesApi.getAppProperties(params[0]);
       }
 
       @Override
       protected void onPostExecute(AppProperties result) {
          mCheckingNewVersion = false;
          if (result != null) {
             try {
                mBinder.hasNewVersion(result, true);
             } catch (Exception e) {
                // e.printStackTrace();
             }
          }
       }
    }
 
    private class AppDeadlineTask extends
          AsyncTask<Context, Integer, AppDeadline> {
       private Context context;
 
       @Override
       protected AppDeadline doInBackground(Context... params) {
          this.context = params[0];
          return AppDeadlineApi.getAppDeadline(params[0]);
       }
 
       @Override
       protected void onPostExecute(AppDeadline result) {
          mAppDeadline = result;
          ((NewsApp) this.context.getApplicationContext())
                .setAppDeadline(result);
       }
    }
 
    private class NewsMessagescheduleTask extends
          AsyncTask<Context, Integer, NewsMessageSchedule> {
       private Context context;
       private String userId;
 
       public NewsMessagescheduleTask(String userId) {
          this.userId = userId;
       }
 
       @Override
       protected NewsMessageSchedule doInBackground(Context... params) {
          this.context = params[0];
          return NewsMessageApi.getNewsMessageSchedule(params[0], this.userId);
       }
 
       @Override
       protected void onPostExecute(NewsMessageSchedule result) {
          if (result != null) {
             PreferenceManager prefsManager = ((NewsApp) NewsService.this
                   .getApplication()).getPrefsManager();
             result.setBaseMillis(System.currentTimeMillis());
             result.setCount(0);
             prefsManager.setMessageScheduleString(result.toString());
             analyzeNewsMessage(this.userId, result);
          }
       }
    }
 
    private class NewsMessageListTask extends
          AsyncTask<Context, Integer, NewsMessageList> {
       private Context context;
       private String userId;
       private int count = 0;
       private boolean retryIfFailed = false;
       private long retryDelayedMillis = 0;
 
       public NewsMessageListTask(String userId) {
          this.userId = userId;
       }
 
       public void setCount(int count) {
          this.count = count;
       }
 
       public void setRetryIfFailed(boolean retry) {
          this.retryIfFailed = retry;
       }
 
       public void setRetryDelayedMillis(long millis) {
          this.retryDelayedMillis = millis;
       }
 
       @Override
       protected NewsMessageList doInBackground(Context... params) {
          this.context = params[0];
          return NewsMessageApi.getNewsMessageList(params[0], this.userId);
       }
 
       @Override
       protected void onPostExecute(NewsMessageList result) {
          PreferenceManager prefsManager = ((NewsApp) NewsService.this
                .getApplication()).getPrefsManager();
          NewsMessageSchedule schedule = NewsMessageSchedule
                .parseString(prefsManager.getMessageScheduleString());
          if (result != null && result.isSuccess()) {
             if (schedule != null) {
                // schedule.setCount(3);
                schedule.setCount(this.count);
                prefsManager.setMessageScheduleString(schedule.toString());
             }
             notifyNewsMessage(result);
          } else {
             if (schedule != null) {
                schedule.setCount(this.count);
                prefsManager.setMessageScheduleString(schedule.toString());
             }
             if (this.retryIfFailed) {
                if (this.retryDelayedMillis > 0) {
                   schedulePullingNewsMessage(this.userId,
                         this.retryDelayedMillis, this.count + 1);
                } else {
                   requestNewsMessageList(this.userId, this.count + 1);
                }
             }
          }
       }
    }
 
 }
