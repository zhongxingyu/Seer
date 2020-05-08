 package com.kii.demo.sync.activities;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ExpandableListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kii.cloud.sync.BackupService;
 import com.kii.cloud.sync.DownloadManager;
 import com.kii.cloud.sync.KiiClientTask;
 import com.kii.cloud.sync.KiiSyncClient;
 import com.kii.demo.sync.R;
 import com.kii.demo.sync.utils.MimeInfo;
 import com.kii.demo.sync.utils.MimeUtil;
 import com.kii.demo.sync.utils.Utils;
 import com.kii.sync.KiiFile;
 import com.kii.sync.KiiNewEventListener;
 import com.kii.sync.SyncMsg;
 
 public class KiiFilePickerActivity extends ExpandableListActivity implements
         View.OnClickListener {
 
     public static final String TAG = "KiiFilePickerActivity";
     // message for the handler
     public final static int PROGRESS_START = 1;
     public final static int PROGRESS_CHECK = 2;
     public final static int PROGRESS_END = 3;
     public final static int PROGRESS_AUTO = 4;
     public final static int SETUP_ADPTOR = 5;
     public final static int PROGRESS_UPDATE = 6;
     public final static int PROGRESS_SCAN_FILES = 7;
     public final static int PROGRESS_SCAN_FILES_FINISH = 8;
 
     final static int MENU_RESTORE_TRASH = 1;
     final static int MENU_MOVE_TRASH = 2;
     final static int MENU_DELETE = 3;
     final static int MENU_DELETE_LOCAL = 4;
     final static int MENU_DOWNLOAD = 5;
     final static int MENU_CANCEL = 6;
 
     NewEventListener mNewEventListener = null;
     private boolean needDownload = false;
 
     KiiFileExpandableListAdapter mAdapter;
     View mHeaderView = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         requestWindowFeature(Window.FEATURE_PROGRESS);
         mNewEventListener = new NewEventListener(this);
         connect();
         registerForContextMenu(getExpandableListView());
     }
 
     void showToast(String title, int errorCode) {
         showToast(title, Utils.getErrorMsg(errorCode, this));
     }
 
     void showToast(String title, CharSequence msg) {
         showToast(title + ":" + msg);
     }
 
     void showToast(String msg) {
         Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
     }
 
     private void updateScanDialog() {
         if (scanDialog == null) {
             scanDialog = ProgressDialog.show(this, "",
                     "Scanning for update. Please wait...", true);
         } else {
             if (scanTotalCount > 0) {
                 scanDialog.setMessage(String.format("Scan %d out of %d",
                         scanCurCount, scanTotalCount));
             }
         }
     }
 
     private boolean updateProgress() {
         KiiSyncClient kiiClient = KiiSyncClient.getInstance();
         if (kiiClient != null) {
             int progress = kiiClient.getProgress();
             if (progress > 0) {
                 setProgress(progress * 100);
                 mAdapter.notifyDataSetChanged();
                 return true;
             }
 
             DownloadManager downManager = kiiClient.downManager;
             if (downManager != null && downManager.getDownloadProgress() >= 0) {
                 setProgress((int) (downManager.getDownloadProgress() * 100));
                 return true;
             }
         }
         return false;
     }
 
     final static int DIALOG_UPDATE = 2;
 
     ProgressDialog scanDialog = null;
 
     protected Dialog onCreateDialog(int id) {
         Dialog dialog;
         switch (id) {
             case DIALOG_UPDATE:
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(scanChange.size() + " file(s) has changed.")
                         .setCancelable(false)
                         .setPositiveButton("Update Now",
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,
                                             int id) {
                                         updateFileChange();
                                     }
                                 })
                         .setNegativeButton("Cancel",
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,
                                             int id) {
                                         dialog.cancel();
                                     }
                                 });
                 dialog = builder.create();
                 break;
             default:
                 dialog = null;
         }
         return dialog;
     }
 
     public Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case SETUP_ADPTOR:
                     adpaterSetup();
                     break;
                 case PROGRESS_AUTO:
                     if (updateProgress()) {
                         setProgressBarIndeterminateVisibility(true);
                         setProgressBarVisibility(true);
                         handler.sendEmptyMessageDelayed(PROGRESS_AUTO, 500);
                     } else {
                         setProgressBarIndeterminateVisibility(false);
                         setProgressBarVisibility(false);
                     }
                     break;
                 case PROGRESS_START:
                     handler.removeMessages(PROGRESS_AUTO);
                     handler.removeMessages(PROGRESS_CHECK);
                     handler.removeMessages(PROGRESS_END);
                     setProgressBarIndeterminateVisibility(true);
                     setProgressBarVisibility(true);
                     if (msg.obj != null && msg.obj instanceof String) {
                         setTitle((String) msg.obj);
                     }
                 case PROGRESS_CHECK:
                     updateProgress();
                     msg.what = PROGRESS_CHECK;
                     Message newMsg = new Message();
                     newMsg.copyFrom(msg);
                     handler.sendMessageDelayed(newMsg, 500);
                     break;
 
                 case PROGRESS_UPDATE:
                     mAdapter.notifyDataSetChanged();
                     break;
                 case PROGRESS_SCAN_FILES:
                     updateScanDialog();
                     handler.sendEmptyMessageDelayed(PROGRESS_SCAN_FILES, 500);
                     break;
                 case PROGRESS_SCAN_FILES_FINISH:
                     handler.removeMessages(PROGRESS_SCAN_FILES);
                     if (scanDialog != null) {
                         scanDialog.dismiss();
                         scanDialog = null;
                         if (!scanChange.isEmpty()) {
                             showDialog(DIALOG_UPDATE);
                         } else {
                             showToast("No update is found.");
                         }
                     }
                     break;
                 case PROGRESS_END:
                 default:
                     handler.removeMessages(PROGRESS_AUTO);
                     handler.removeMessages(PROGRESS_CHECK);
                     handler.removeMessages(PROGRESS_END);
 
                     setProgressBarIndeterminateVisibility(false);
                     setProgressBarVisibility(false);
                     setTitle(R.string.app_name);
                     if (mAdapter != null) {
                         mAdapter.notifyDataSetChanged();
                     }
                     if (needDownload) {
                         needDownload = false;
                         doDownloadAll();
                     }
                     break;
             }
 
         }
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.resume_upload:
                 fullFefresh();
                 break;
             case R.id.refresh:
                 syncRefresh();
                 break;
             case R.id.suspend:
                 syncStop();
                 break;
             case R.id.setting:
                 setting();
                 break;
             case R.id.scan_change:
                 scanFileChange();
                 break;
             case R.id.download_all:
                 prepareDownloadAll();
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
 
         return true;
     }
 
     @Override
     protected void onDestroy() {
         // unregister the listener
         if (mNewEventListener != null) {
             mNewEventListener.unregister();
         }
         super.onDestroy();
     }
 
     /**
      * suspend the existing sync if there is any
      */
     private void syncStop() {
         KiiSyncClient kiiClient = KiiSyncClient.getInstance();
         if (kiiClient != null) {
             kiiClient.suspend();
         }
     }
 
     ArrayList<KiiFile> scanChange = null;
     int scanTotalCount = -1;
     int scanCurCount = 0;
 
     /*
      * Check the existing backup files for changes
      */
     private void scanFileChange() {
 
         scanChange = new ArrayList<KiiFile>();
         scanTotalCount = -1;
         scanCurCount = 0;
 
         new Thread(new Runnable() {
             public void run() {
                 handler.sendEmptyMessage(PROGRESS_SCAN_FILES);
                 KiiSyncClient kiiClient = KiiSyncClient.getInstance();
                 KiiFile[] files = kiiClient.getBackupFiles();
                 scanTotalCount = files.length;
                 scanCurCount = 0;
                 for (; scanCurCount < files.length; scanCurCount++) {
                     if (files[scanCurCount].isFile()) {
                         if (kiiClient.bodySameAsLocal(files[scanCurCount])) {
                             scanChange.add(files[scanCurCount]);
                         }
                     }
                 }
                 handler.sendEmptyMessage(PROGRESS_SCAN_FILES_FINISH);
             }
         }).start();
     }
 
     /*
      * download all the files from cloud
      */
     private void prepareDownloadAll() {
         needDownload = true;
         syncRefresh();
     }
 
     private void doDownloadAll() {
         Runnable r = new Runnable() {
             public void run() {
                 KiiSyncClient client = KiiSyncClient.getInstance();
                 if (client != null) {
                     KiiFile[] files = client.getBackupFiles();
                     if (files != null) {
                         for (KiiFile file : files) {
                             int status = client.getStatus(file);
                             if (!KiiSyncClient.isFileInTrash(file)
                                     && (status == KiiFile.STATUS_BODY_OUTDATED || status == KiiFile.STATUS_NO_BODY)) {
                                 client.download(file,
                                         Utils.getKiiFileDest(file));
                             }
                         }
                         handler.sendEmptyMessage(KiiFilePickerActivity.PROGRESS_END);
                     }
                 }
             }
         };
         new Thread(r).start();
     }
 
     /*
      * update the backup files which have changed must call scanFileChange
      * before this
      */
     private void updateFileChange() {
         KiiSyncClient client = KiiSyncClient.getInstance();
         client.updateBody(scanChange);
         Utils.startSync(getApplicationContext(), BackupService.ACTION_REFRESH);
     }
 
     /**
      * resume upload
      */
     private void fullFefresh() {
         Utils.startSync(this, BackupService.ACTION_REFRESH);
     }
 
     /**
      * get new records from server if there are any
      */
     private void syncRefresh() {
         Utils.startSync(this, BackupService.ACTION_REFRESH_QUICK);
     }
 
     /**
      * Login
      */
     private void connect() {
 
         Receiver receiver = new Receiver();
         // register ACTION_INIT_COMPLETE which will be send by KiiClient
         // registerReceiver(receiver, new
         // IntentFilter(KiiRefClient.ACTION_INIT_COMPLETE));
         registerReceiver(receiver, new IntentFilter(
                 DownloadManager.ACTION_DOWNLOAD_END));
         registerReceiver(receiver, new IntentFilter(
                 DownloadManager.ACTION_DOWNLOAD_START));
         KiiClientTask task = new KiiClientTask(getApplicationContext(),
                 "Connect", KiiClientTask.SYNC_CONNECT, mNewEventListener);
         task.execute();
     }
 
     private void adpaterSetup() {
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mHeaderView == null)
             mHeaderView = inflater.inflate(R.layout.cloud_header_view, null);
         getExpandableListView().addHeaderView(mHeaderView);
         setLastSyncTime();
         mAdapter = new KiiFileExpandableListAdapter(this,
                 KiiSyncClient.getInstance(),
                 KiiFileExpandableListAdapter.TYPE_DATA, this);
         setListAdapter(mAdapter);
         mNewEventListener.register();
     }
 
     private void setting() {
         Intent intent = new Intent(this, StartActivity.class);
         intent.setAction(Intent.ACTION_CONFIGURATION_CHANGED);
         startActivity(intent);
     }
 
     public Intent getLaunchFileIntent(String path, MimeInfo mime) {
         if (TextUtils.isEmpty(path))
             return null;
         if (mime == null)
             return null;
         Intent commIntent = null;
         Uri fileUri = Uri.fromFile(new File(path));
         commIntent = new Intent(Intent.ACTION_VIEW);
         commIntent.setDataAndType(fileUri, mime.getMimeType());
         return commIntent;
     }
 
     public Intent getLaunchURLIntent(URL url, String mimeType) {
         Intent commIntent = null;
         commIntent = new Intent(Intent.ACTION_VIEW);
         if (mimeType.startsWith("video")) {
             commIntent.setDataAndType(Uri.parse(url.toString()), "video/*");
         } else if (mimeType.startsWith("audio")) {
             commIntent.setDataAndType(Uri.parse(url.toString()), mimeType);
         } else {
             commIntent.setData(Uri.parse(url.toString()));
         }
         return commIntent;
     }
 
     @Override
     public boolean onChildClick(ExpandableListView parent, View v,
             int groupPosition, int childPosition, long id) {
         Object o = v.getTag();
         if (o instanceof KiiFile) {
             KiiFile kFile = (KiiFile) o;
             if (kFile.isFile()) {
 
                 String category = kFile.getCategory();
 
                 // check if the file is trash category, if yes prompt for
                 // restore
                 if (!TextUtils.isEmpty(category)
                         && KiiSyncClient.CATEGORY_TRASH
                                 .equalsIgnoreCase(category)) {
                     showToast("Please restore before view!");
                     return true;
                 }
 
                 Intent intent = null;
                 MimeInfo mime = MimeUtil.getInfoByKiiFile(kFile);
 
                 if (KiiSyncClient.getInstance().getStatus(kFile) != KiiFile.STATUS_NO_BODY) {
                     intent = getLaunchFileIntent(kFile.getLocalPath(), mime);
                 }
                 if (intent == null && kFile.getAvailableURL() != null) {
                     if (mime != null) {
                         intent = getLaunchURLIntent(kFile.getAvailableURL(),
                                 mime.getMimeType());
                     }
                 }
 
                 if (intent == null) {
                     showToast("Failed to launch the file - " + kFile.getTitle());
                 } else {
                     try {
                         startActivity(intent);
                     } catch (Exception ex) {
                         showToast("Encounter error when launch file ("
                                 + kFile.getTitle() + "). Error("
                                 + ex.getMessage() + ")");
                     }
                 }
             } else {
                 showToast("Failed to launch the viewer for " + kFile.getTitle());
             }
         }
         return super.onChildClick(parent, v, groupPosition, childPosition, id);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
         int type = ExpandableListView
                 .getPackedPositionType(info.packedPosition);
         int group = ExpandableListView
                 .getPackedPositionGroup(info.packedPosition);
         int child = ExpandableListView
                 .getPackedPositionChild(info.packedPosition);
         // Only create a context menu for child items
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
             KiiFile kFile = (KiiFile) mAdapter.getChild((int) group,
                     (int) child);
             if (kFile != null && kFile.isFile()) {
                 menu.setHeaderTitle(kFile.getTitle());
 
                 KiiSyncClient kiiClient = KiiSyncClient.getInstance();
 
                 if (kiiClient == null) {
                     showToast("Not ready.");
                     return;
                 }
 
                 int status = kiiClient.getStatus(kFile);
 
                 switch (status) {
                     case KiiFile.STATUS_DELETE_REQUEST:
                     case KiiFile.STATUS_DELETE_REQUEST_INCLUDEBODY:
                     case KiiFile.STATUS_SERVER_DELETE_REQUEST:
                         showToast("No option for deleted file.");
                         break;
                     case KiiFile.STATUS_PREPARE_TO_SYNC:
                     case KiiFile.STATUS_UPLOADING_BODY:
                         menu.add(MENU_CANCEL, 0, 0, "Cancel Upload");
                         break;
                     // TODO: can implement the option to update the backup copy
                     // or restore the previous version
                     case KiiFile.STATUS_BODY_OUTDATED:
                     case KiiFile.STATUS_NO_BODY:
                     case KiiFile.STATUS_SYNCED:
                     case KiiFile.STATUS_REQUEST_BODY:
                     case KiiFile.STATUS_DOWNLOADING_BODY:
                     case KiiFile.STATUS_UNKNOWN:
                     default:
                         String category = kFile.getCategory();
                         if (TextUtils.isEmpty(category)
                                 || !KiiSyncClient.CATEGORY_TRASH
                                         .equalsIgnoreCase(category)) {
                             menu.add(MENU_MOVE_TRASH, 0, 0, "Move To Trash");
                             if (status == KiiFile.STATUS_NO_BODY) {
                                 menu.add(MENU_DOWNLOAD, 0, 0, "Download");
                             } else {
                                 menu.add(MENU_DELETE_LOCAL, 0, 0,
                                         "Delete Local & Backup");
                             }
                         } else {
                             menu.add(MENU_RESTORE_TRASH, 0, 0,
                                     "Restore From Trash");
                         }
                         menu.add(MENU_DELETE, 0, 0, "Delete Backup Copy");
                         break;
                 }
             } else {
                 showToast("No menu for directory.");
             }
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
                 .getMenuInfo();
 
         String title = "Menu";
 
         int type = ExpandableListView
                 .getPackedPositionType(info.packedPosition);
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 
             int groupPos = ExpandableListView
                     .getPackedPositionGroup(info.packedPosition);
             int childPos = ExpandableListView
                     .getPackedPositionChild(info.packedPosition);
 
             final KiiFile kFile = (KiiFile) mAdapter.getChild((int) groupPos,
                     (int) childPos);
             if (kFile != null && kFile.isFile()) {
                 final KiiSyncClient client = KiiSyncClient.getInstance();
                 if (client == null) {
                     Log.d(TAG, "get KiiRefClient failed, return!");
                     return true;
                 }
                 switch (item.getGroupId()) {
                     case MENU_RESTORE_TRASH:
                         Runnable r = new Runnable() {
                             public void run() {
                                 client.restoreFromTrash(kFile);
                             }
                         };
                         new Thread(r).start();
                         break;
                     case MENU_MOVE_TRASH:
                         client.moveKiiFileToTrash(kFile);
                         break;
                     case MENU_DELETE:
                         client.delete(kFile, false);
                         break;
                     case MENU_DELETE_LOCAL:
                         client.delete(kFile, true);
                         break;
                     case MENU_CANCEL:
                         client.cancel(kFile);
                         break;
                     case MENU_DOWNLOAD:
                         Toast.makeText(
                                 this,
                                 "Download at folder:"
                                         + KiiSyncClient.getInstance()
                                                 .getDownloadFolder(),
                                 Toast.LENGTH_SHORT).show();
                         Runnable r1 = new Runnable() {
                             public void run() {
                                 client.download(kFile, null);
                             }
                         };
                         new Thread(r1).start();
                         break;
                 }
 
                 Utils.startSync(getApplicationContext(),
                         BackupService.ACTION_REFRESH);
             }
 
             return true;
         } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
             int groupPos = ExpandableListView
                     .getPackedPositionGroup(info.packedPosition);
             Toast.makeText(this, title + ": Group " + groupPos + " clicked",
                     Toast.LENGTH_SHORT).show();
 
             return true;
         }
 
         return false;
     }
 
     class Receiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction()
                     .equals(DownloadManager.ACTION_DOWNLOAD_START)) {
                 handler.sendEmptyMessageDelayed(
                         KiiFilePickerActivity.PROGRESS_START, 500);
             } else if (intent.getAction().equals(
                     DownloadManager.ACTION_DOWNLOAD_END)) {
                 handler.sendEmptyMessage(KiiFilePickerActivity.PROGRESS_END);
             }
         }
     }
 
     public class NewEventListener implements KiiNewEventListener {
 
         final static String TAG = "NewEventListener";
 
         KiiSyncClient client = null;
         long id = 0;
         Context context = null;
 
         public NewEventListener(Context context) {
             this.context = context;
             id = System.currentTimeMillis();
         }
 
         public boolean register() {
             client = KiiSyncClient.getInstance();
             if (client == null) {
                 throw new NullPointerException();
             }
             return client.registerNewEventListener(id, this);
         }
 
         public void unregister() {
             if (id != 0) {
                 client.unregisterNewEventListener(id);
             }
         }
 
         @Override
         public void onNewSyncDeleteEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onNewSyncInsertEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onNewSyncUpdateEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onSyncComplete(SyncMsg msg) {
             handler.sendEmptyMessageDelayed(KiiFilePickerActivity.PROGRESS_END,
                     500);
             if (msg != null) {
                 switch (msg.sync_result) {
                     case SyncMsg.ERROR_AUTHENTICAION_ERROR:
                         Intent apiIntent = new Intent(
                                 context.getApplicationContext(),
                                 StartActivity.class);
                         apiIntent
                                 .setAction(StartActivity.ACTION_ENTER_PASSWORD);
                         context.startActivity(apiIntent);
                         break;
                     case SyncMsg.OK:
                         setLastSyncTime();
                         break;
                 }
             }
         }
 
         @Override
         public void onSyncStart(String syncMode) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_START, 500);
         }
 
         @Override
         public void onQuotaExceeded(Uri arg0) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_UPDATE, 500);
 
         }
 
         @Override
         public void onLocalChangeSyncedEvent(Uri[] uris) {
             handler.sendEmptyMessageDelayed(
                     KiiFilePickerActivity.PROGRESS_UPDATE, 500);
         }
 
         public void onConnectComplete() {
             adpaterSetup();
         }
 
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.menu_button:
                 View row = (View) v.getTag();
                 getExpandableListView().showContextMenuForChild(row);
                 break;
         }
     }
 
     public void handleRefresh(View v) {
         syncRefresh();
     }
 
     private void setLastSyncTime() {
        if (mHeaderView != null) {
            TextView tv = (TextView) mHeaderView.findViewById(R.id.header_text);
            tv.setText(Utils.getLastSyncTime(this));
        }
     }
 
 }
