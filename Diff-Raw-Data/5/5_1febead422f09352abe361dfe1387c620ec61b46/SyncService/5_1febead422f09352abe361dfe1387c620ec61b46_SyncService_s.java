 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
 package org.pixmob.fm2.services;
 
 import static org.pixmob.fm2.Constants.TAG;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.List;
 
 import org.pixmob.actionservice.ActionExecutionFailedException;
 import org.pixmob.actionservice.ActionService;
 import org.pixmob.fm2.R;
 import org.pixmob.fm2.model.Account;
 import org.pixmob.fm2.model.AccountRepository;
 import org.pixmob.fm2.net.AccountNetworkClient;
 import org.pixmob.fm2.ui.FM2;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * Service for synchronizing accounts.
  * @author Pixmob
  */
 public class SyncService extends ActionService {
     public static final String EXTRA_TRACK_UPDATES = "trackUpdates";
     private static final String EXTRA_RETRY_COUNT = "retryCount";
     private static final int RETRY_MAX = 3;
     private static final long RETRY_PERIOD = 1000 * 60 * 3;
     private static final int SYNC_DONE = 1;
     private static final int SYNC_ERROR = 2;
     
     /**
      * Method callbacks for the synchronization service.
      * @author Pixmob
      */
     public static interface Listener {
         /**
          * This method is called when a synchronization is done.
          */
         void onSyncDone();
         
         /**
          * This method is called when a synchronization failed.
          */
         void onSyncError(Exception cause);
     }
     
     /**
      * Local binder for the synchronization service.
      * @author Pixmob
      */
     public static class LocalBinder extends android.os.Binder {
         private final SyncService syncService;
         
         private LocalBinder(final SyncService syncService) {
             this.syncService = syncService;
         }
         
         /**
          * Get the synchronization service instance.
          */
         public SyncService getService() {
             return syncService;
         }
     }
     
     private final IBinder binder = new LocalBinder(this);
     private AccountNetworkClient accountNetworkClient;
     private WeakReference<Listener> listenerRef;
     private Handler listenerHandler;
     private PendingIntent syncPendingIntent;
     
     public SyncService() {
         super("FM2/Sync", 10000, 2);
     }
     
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
     
     public void setListener(Listener listener) {
         if (listener == null) {
             listenerRef = null;
         } else {
             listenerRef = new WeakReference<SyncService.Listener>(listener);
         }
     }
     
     @Override
     public void onCreate() {
         super.onCreate();
         
         // THe listener handler will dispatch events to the registered listener
         // using the main thread.
         listenerHandler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 Listener listener = null;
                 if (listenerRef != null) {
                     listener = listenerRef.get();
                 }
                 if (listener != null) {
                     try {
                         switch (msg.what) {
                             case SYNC_DONE:
                                 listener.onSyncDone();
                                 break;
                             case SYNC_ERROR:
                                 listener.onSyncError((Exception) msg.obj);
                                 break;
                         }
                     } catch (Exception e) {
                         Log.w(TAG, "Sync listener error", e);
                     }
                 }
             }
         };
         
         accountNetworkClient = new AccountNetworkClient(this);
         syncPendingIntent = PendingIntent.getActivity(
             this,
             0,
             new Intent(this, FM2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                     | Intent.FLAG_ACTIVITY_SINGLE_TOP),
             PendingIntent.FLAG_CANCEL_CURRENT);
     }
     
     @Override
     public void onDestroy() {
         super.onDestroy();
         
         // Release resources.
         accountNetworkClient = null;
         listenerHandler = null;
         listenerRef = null;
     }
     
     /**
      * Check if the device is network connected.
      */
     private boolean isNetworkConnected() {
         final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         final NetworkInfo info = cm.getActiveNetworkInfo();
         if (info != null) {
             return info.isAvailable() && info.isConnected();
         }
         
         return false;
     }
     
     @Override
     protected void onHandleAction(Intent intent)
             throws ActionExecutionFailedException, InterruptedException {
         if (!isNetworkConnected()) {
             Log.w(TAG,
                 "Skip user account synchronization since network is not available");
             return;
         }
         
         Log.i(TAG, "Starting user account synchronization");
         
         final Notification n = new Notification(
                 android.R.drawable.stat_notify_sync,
                 getString(R.string.status_synchronizing),
                 System.currentTimeMillis());
         n.setLatestEventInfo(this, getString(R.string.app_name),
             getString(R.string.status_synchronizing), syncPendingIntent);
         startForeground(R.string.status_synchronizing, n);
         
         final boolean trackUpdates = intent.getBooleanExtra(
             EXTRA_TRACK_UPDATES, false);
         final int retryCount = intent.getIntExtra(EXTRA_RETRY_COUNT, 0);
         
         try {
             doSync(trackUpdates, retryCount);
         } catch (IOException e) {
             fireOnSyncError(e);
             throw new ActionExecutionFailedException("Synchronization failed",
                     e);
         } finally {
             stopForeground(true);
             
             Log.i(TAG, "User account synchronization done");
             fireOnSyncDone();
         }
     }
     
     private void doSync(boolean trackUpdates, int retryCount)
             throws IOException {
         boolean accountsUpdated = false;
         boolean tryLater = false;
         
         final AccountRepository accountRepository = new AccountRepository(this);
         try {
             final List<Account> accounts = accountRepository.list();
             for (final Account account : accounts) {
                 Log.i(TAG, "Synchronizing account for user " + account.login);
                 
                 final int accountStatusBeforeUpdate = account.status;
                 
                 try {
                     // Read account data from the website.
                     accountNetworkClient.update(account);
                     
                     // Update the local database.
                     accountRepository.update(account);
                     
                     if (trackUpdates
                             && account.status != accountStatusBeforeUpdate) {
                         accountsUpdated = true;
                     }
                 } catch (IOException e) {
                     Log.w(TAG, "Account update failed for user "
                             + account.login, e);
                     tryLater = true;
                 }
             }
         } finally {
             accountRepository.dispose();
         }
         
         if (accountsUpdated) {
             Log.i(TAG, "An account has been updated");
             
             final PendingIntent openUI = PendingIntent.getActivity(this, 0,
                 new Intent(this, FM2.class)
                         .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                 | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                 PendingIntent.FLAG_CANCEL_CURRENT);
             
             final Notification n = new Notification(
                     android.R.drawable.stat_sys_warning,
                     getString(R.string.notif_account_updated),
                     System.currentTimeMillis());
             n.setLatestEventInfo(this, getString(R.string.app_name),
                 getString(R.string.notif_account_updated), openUI);
             n.defaults = Notification.DEFAULT_ALL;
             n.flags = Notification.FLAG_AUTO_CANCEL;
             
             final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
             nm.notify(R.string.notif_account_updated, n);
         }
         
         if (tryLater) {
             // There was trouble while trying to update accounts:
             // try again later.
             retryCount += 1;
             if (retryCount == RETRY_MAX) {
                 // Do not try again as we reached the limit.
                 Log.w(TAG, "Retry count reached: "
                         + "skip background account synchronization");
             } else {
                 Log.i(TAG, "Scheduling background account synchronization "
                         + "in order to resolve account update trouble");
                 
                 final PendingIntent syncLaterIntent = PendingIntent.getService(
                     this, 0, new Intent(this, SyncService.class).putExtra(
                         EXTRA_RETRY_COUNT, retryCount),
                     PendingIntent.FLAG_CANCEL_CURRENT);
                 final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                 am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                     System.currentTimeMillis() + RETRY_PERIOD, RETRY_PERIOD,
                     syncLaterIntent);
             }
         }
     }
     
     private void fireOnSyncError(Exception cause) {
         final Message msg = new Message();
         msg.obj = cause;
         msg.what = SYNC_ERROR;
         listenerHandler.sendMessage(msg);
     }
     
     private void fireOnSyncDone() {
         listenerHandler.sendEmptyMessage(SYNC_DONE);
     }
 }
