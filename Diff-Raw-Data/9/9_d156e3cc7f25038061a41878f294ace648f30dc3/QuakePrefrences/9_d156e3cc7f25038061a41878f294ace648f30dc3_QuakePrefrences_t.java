 package com.barefoot.pocketshake;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.location.Location;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.Toast;
 
 import com.barefoot.pocketshake.service.AlarmScheduler;
 import com.barefoot.pocketshake.service.ReferencePointCalculator;
 import com.barefoot.pocketshake.storage.EarthQuakeDataWrapper;
 
 public class QuakePrefrences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
     
     public static String BROADCAST_ACTION = "com.barefoot.pocketshake.QuakePreferences.refreshView";
     private Intent broadcast = new Intent(BROADCAST_ACTION);
     private ReferencePointCalculator refCalculator;
    private EarthQuakeDataWrapper dbWrapper;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.pref);
         refCalculator = new ReferencePointCalculator(this);
        dbWrapper = new EarthQuakeDataWrapper(this);
     }
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 	}
	
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		Log.i("Preference Change Listener", "Change Listener has been called for key:: " + key);
 		if("time_freq_unit".equalsIgnoreCase(key)) {
 			Log.i("Preference Change Listener", "Updating service alarm to be run at new interval");
 			new AlarmScheduler(this).updateAlarmSchedule();
 		}
 		
 		if("intensity_setting".equalsIgnoreCase(key)) {
 			Log.i("Preference Change Listener", "Updating min earthquake intensity and refreshing list");
 			refreshFeedWithNewSettings();
 		}
 		
 		if("radius_value".equalsIgnoreCase(key)) {
 			Log.i("Preference Change Listener", "Updating last known location for the user.");
 			Location lastLocation = refCalculator.getReferencePoint();
 			if(lastLocation == null) {
 				Log.i("Preference Change Listener", "No known last location for user! Radius setting won't work.");
 				showInvalidLastLocationToast();
 			} else {
 				refreshFeedWithNewSettings();
 			}
 		}
 	}
 	
 	private void refreshFeedWithNewSettings() {
		dbWrapper.refreshFeedCache(true);
 		sendBroadcast(broadcast);
 	}
 	
 	private void showInvalidLastLocationToast() {
 		Toast updateNotification = Toast.makeText(this, "You don't have last known location set in GPS. This setting won't work for you.", Toast.LENGTH_LONG);
 		updateNotification.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
 		updateNotification.show();
 	}	
 }
