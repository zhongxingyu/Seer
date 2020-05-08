 package mobi.monaca.framework;
 
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.gcm.GCMPushDataset;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	private static final String TAG = GCMIntentService.class.getSimpleName();
 
 	@Override
 	protected void onError(Context arg0, String arg1) {
 		MyLog.d(TAG, "onError :" + arg1);
 	}
 
 	@Override
 	protected void onMessage(Context arg0, Intent arg1) {
 		MyLog.d(TAG, "onMessage");
 		Bundle b = arg1.getExtras();
 		if (b == null) {
 			return;
 		}
 		String message = b.getString("message");
 		String pushProjectId = b.getString("push_project_id");
 		String extraJsonString = b.getString("extra_json");
 		GCMPushDataset data = new GCMPushDataset(pushProjectId, message, extraJsonString);
 
 		String title = b.getString("title") != null ? b.getString("title") : getString(R.string.app_name) + " Received Push";
 		Intent intent = new Intent(this, MonacaNotificationActivity.class);
 		intent.putExtra(GCMPushDataset.KEY, data);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int id = (int)System.currentTimeMillis();
 		PendingIntent pending = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		Notification notification = new Notification();
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notification.icon = R.drawable.icon;
 		notification.tickerText = title;
 		notification.setLatestEventInfo(this, title, message, pending);
 
 		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.notify(id, notification);
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, String arg1) {
 		MyLog.d(TAG, "onRegisterd :" + arg1);
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		MyLog.d(TAG, "onUnregistered :" + arg1);
 	}
 }
