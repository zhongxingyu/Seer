 package edu.dartmouth.cs.audiorecorder;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.preference.PreferenceManager;
 
 public class BootReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context arg0, Intent arg1) {
 		if (PreferenceManager.getDefaultSharedPreferences(arg0).getBoolean(
				SensorPreferenceActivity.IS_ON, false))
			SensorPreferenceActivity.start(arg0);
 	}
 
 }
