 package jp.upset.horoscope;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	private final static String MSG_ACTION = "msg_count";
 	private final static String PUSH_ACTION = "registerPush";
 
 	@Override
 	protected void onError(Context arg0, String str) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void onMessage(Context context, Intent intent) {
 		// TODO Auto-generated method stub
 
 		try {
 			String t = intent.getStringExtra("type");
 			String s = intent.getExtras().getString("msg_count");
 			int type = Integer.parseInt(t);
 			int count = Integer.parseInt(s);
 			Preference.setMessageCount(context, count);
 			if(type ==1){
 				String msg = intent.getStringExtra("msg");
 				Intent i = new Intent(context, MainActivity.class);
 				i.putExtra("tab", 2);
 				PendingIntent pi = PendingIntent.getActivity(context, 0, i,
						Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				Notification noti = new NotificationCompat.Builder(context)
 						.setContentTitle(context.getString(R.string.app_name)).setContentText(msg).setTicker(msg)
 						.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pi)
 						.build();
 				noti.flags = Notification.FLAG_ONLY_ALERT_ONCE
 						| Notification.FLAG_AUTO_CANCEL;
 				NotificationManager nm = (NotificationManager) context
 						.getSystemService(Context.NOTIFICATION_SERVICE);
 				nm.notify(19283456, noti);
 			}
 
 			Intent broadcast = new Intent();
 			broadcast.setAction(MSG_ACTION);
 			broadcast.putExtra("msg_count", count);
 			context.sendBroadcast(broadcast);
 
 			
 
 		} catch (Exception e) {
 
 		}
 	}
 
 	@Override
 	protected void onRegistered(Context context, String arg1) {
 		// TODO Auto-generated method stub
 		Log.i("aa", "onRegistered");
 		Intent broadcast = new Intent();
 		broadcast.setAction(PUSH_ACTION);
 		broadcast.putExtra("reg_id", arg1);
 		context.sendBroadcast(broadcast);
 	}
 
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 		Log.i("aa", "onUnregistered");
 
 	}
 
 }
