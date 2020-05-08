 package jp.muo.smsproxy;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.BatteryManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BatteryLevelObserver extends BroadcastReceiver {
 	private static final String BATTERY_PREFS_KEY = "bat_level";
 	private static final String PREFS_IS_OKAY = "is_okay";
	private static final int BAT_LOW = 74;
	private static final int BAT_OKAY = 80;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		final String action = intent.getAction();
 		if (action.equals(Intent.ACTION_BATTERY_LOW)) {
 			BatteryLevelObserver.updateStatus(context);
 		}
 		else if (action.equals(Intent.ACTION_BATTERY_OKAY)) {
 			BatteryLevelObserver.updateStatus(context);
 		}
 	}
 
 	public static void updateStatus(Context context) {
 		BroadcastReceiver batReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				context.unregisterReceiver(this);
 				SharedPreferences prefs = context.getSharedPreferences(BATTERY_PREFS_KEY, Context.MODE_PRIVATE);
 				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
 				boolean isLowerTrigger = level <= BAT_LOW;
 				boolean isOkayTrigger = level >= BAT_OKAY;
 				intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
 				boolean isPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
 				boolean isOkayInPrefs = prefs.getBoolean(PREFS_IS_OKAY, true);
 				boolean isBatteryLevelOkay = isOkayInPrefs;
 				SharedPreferences.Editor editor = prefs.edit();
 				if (isOkayInPrefs) {
 					if (isLowerTrigger && !isPlugged) {
 						SmsProxyManager mgr = new SmsProxyManager(context);
 						Toast.makeText(context, "sending bat. notif.", Toast.LENGTH_LONG).show();
 						if (mgr.isEnabled()) {
 							mgr.send(SmsProxyManager.Mode.CALL, context.getString(R.string.sms_bat));
 						}
 						isBatteryLevelOkay = false;
 					}
 				}
 				else {
 					if (isOkayTrigger) {
 						isBatteryLevelOkay = true;
 					}
 				}
 				Toast.makeText(context, "isOkay: " + (isBatteryLevelOkay ? "true" : "false"), Toast.LENGTH_LONG).show();
 				if (isOkayInPrefs != isBatteryLevelOkay) {
 					editor.putBoolean(PREFS_IS_OKAY, isBatteryLevelOkay);
 					editor.commit();
 				}
 			}
 		};
 		context.registerReceiver(batReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 	}
 }
