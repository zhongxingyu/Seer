 package com.davelabs.wakemehome;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.View;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 
 public class AlarmActivity extends Activity{
 	
 	private Vibrator _vibrator;
 	private Ringtone _r;
 	private LocationClient _lc;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.alarm);
         vibrateAlarm();
         ringAlarm();
 	}
 
 	private void ringAlarm() {
 		Uri tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 		_r = RingtoneManager.getRingtone(getApplicationContext(),tone);
 		_r.setStreamType(AudioManager.STREAM_ALARM);
 		_r.play();
 	}
 
 	private void vibrateAlarm() {
 		_vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
         _vibrator.vibrate(10000000);
 	}
 	
 	public void imAwakeButtonClicked(View v) {
 		_vibrator.cancel();
 		_r.stop();
 		removeProximityAlert();
 	}
 
 	private void removeProximityAlert() {
 		_lc = new LocationClient(this, 
 				new GooglePlayServicesClient.ConnectionCallbacks() {
 
 					@Override
 					public void onConnected(Bundle arg0) {
 						removeGeofence();
 						
 					}
 									
 					@Override
 					public void onDisconnected() {}
 			
 		},
 				new GooglePlayServicesClient.OnConnectionFailedListener() {
 
 					@Override
 					public void onConnectionFailed(ConnectionResult arg0) {}
 			
 		}
 		);
		
 		
 	}
 	
 	private void removeGeofence() {
 		List<String> geoList = new ArrayList<String>();
 		geoList.add("Dave");
 		_lc.removeGeofences(geoList, new LocationClient.OnRemoveGeofencesResultListener() {
 
 			@Override
 			public void onRemoveGeofencesByPendingIntentResult(int arg0,
 					PendingIntent arg1) {}
 
 			@Override
 			public void onRemoveGeofencesByRequestIdsResult(int arg0,
 					String[] arg1) {}} );
 	}	
 	
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     	this.startActivity(intent);		
 	}
 }
