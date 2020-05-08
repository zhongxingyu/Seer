 package net.djmacgyver.bgt;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import net.djmacgyver.bgt.activity.MainActivity;
 import net.djmacgyver.bgt.socket.SocketCommand;
 import net.djmacgyver.bgt.socket.SocketService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	public static String gcmId;
 	
 	public GCMIntentService() {
 		super(gcmId);
 	}
 
 	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	protected void onMessage(Context context, Intent intent) {
 		if (!intent.hasExtra("title") || !intent.hasExtra("weather")) return;
 		
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification n;
 		PendingIntent i = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), 0);
 		
 		// somehow, GCM seems to convert the server int to a string
 		int weather = Integer.parseInt(intent.getExtras().getString("weather"));
 		String message = getResources().getString(R.string.bladenight) + ": ";
 		if (weather != 0) {
 			message += getResources().getString(R.string.yes_rolling);
 			n = new Notification(R.drawable.ampel_gruen, message, System.currentTimeMillis());
 			n.setLatestEventInfo(getApplicationContext(), intent.getExtras().getString("title"), message, i);
 		} else {
 			message += getResources().getString(R.string.no_cancelled);
 			n = new Notification(R.drawable.ampel_rot, message, System.currentTimeMillis());
 			n.setLatestEventInfo(getApplicationContext(), intent.getExtras().getString("title"), message, i);
 		}
 		
 		n.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
 		n.flags |= Notification.FLAG_AUTO_CANCEL;
 		nm.notify(1, n);
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, final String regId) {
 		ServiceConnection conn = new ServiceConnection() {
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 			}
 			
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder service) {
 				SocketService s = ((SocketService.LocalBinder) service).getService();
 				JSONObject data = new JSONObject();
 				try {
 					data.put("regId", regId);
 				} catch (JSONException e) {}
 				SocketCommand c = new SocketCommand("updateRegistration", data);
 				s.getSharedConnection().sendCommand(c);
 				unbindService(this);
 			}
 		};
 		bindService(new Intent(this, SocketService.class), conn, BIND_AUTO_CREATE);
 		Log.v("GCM registration", "registered (id=\"" + regId + "\"");
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
 	}
 
 }
