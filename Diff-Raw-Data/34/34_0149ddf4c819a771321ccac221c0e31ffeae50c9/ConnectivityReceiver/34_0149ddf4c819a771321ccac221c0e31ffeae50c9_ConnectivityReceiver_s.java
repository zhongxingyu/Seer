 package at.tomtasche.inspectroid;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class ConnectivityReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getExtras() == null) {
 			return;
 		}
 
 		SharedPreferences preferences = context.getSharedPreferences(
 				"proxyboxy", Context.MODE_PRIVATE);
		boolean enabled = preferences.getBoolean("proxy", true);
 		if (!enabled) {
 			return;
 		}
 
 		ConnectivityManager connectivityManager = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
 		if (info == null || info.getType() != ConnectivityManager.TYPE_WIFI) {
 			return;
 		}
 
 		Intent serviceIntent = new Intent(context, ProxyService.class);
 		if (info.isConnected()) {
 			context.startService(serviceIntent);
 		} else {
 			context.stopService(serviceIntent);
 		}
 	}
 }
