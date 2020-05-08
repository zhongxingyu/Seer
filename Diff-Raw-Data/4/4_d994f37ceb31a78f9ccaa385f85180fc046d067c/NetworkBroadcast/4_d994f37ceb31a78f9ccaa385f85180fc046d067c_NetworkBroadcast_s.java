 package org.xierch.mpiwifi;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiInfo;  
 import android.net.wifi.WifiManager;  
 import android.os.StrictMode;
 
 public class NetworkBroadcast extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (!intent.getAction().equals
 				(ConnectivityManager.CONNECTIVITY_ACTION))
 			return;
     	SharedPreferences settings = context
     			.getSharedPreferences("settings", Context.MODE_PRIVATE);
     	if (!settings.getBoolean("autoLogin", false)) return;
     	
     	SharedPreferences loginInfo = context
     			.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
     	String netId = loginInfo.getString("netId", "");
     	String pwd   = loginInfo.getString("pwd", "");
     	
     	if (netId.isEmpty() || pwd.isEmpty()) {
     		SharedPreferences.Editor editor = settings.edit();
     		editor.putBoolean("autoLogin", false);
     		editor.commit();
     		return;
     	}
     	
     	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         	.permitNetwork().build());
     	
     	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
     	if (!wifiManager.isWifiEnabled()) return;
     	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	if (wifiInfo != null)
 	    	if (wifiInfo.getSSID().equals("NamOn_Hostel"))
 	    		WifiLoginer.loginNamon(context, netId, pwd, true);
     	
 	}
 
 }
