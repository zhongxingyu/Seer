 package com.rahulagarwal.android.androidtoalerts;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 
 import com.google.android.c2dm.C2DMessaging;
 
 public class MainActivity extends Activity {
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         SharedPreferences settings = getSharedPreferences(Constants.PREF_NAME, 0);
         String pushRegistrationID = settings.getString("pushRegistrationID", null);
         if (pushRegistrationID != null) {
         	// Registered with C2DM
         	setContentView(R.layout.connected);
         } else {
         	//Not Registered with C2DM
         	setContentView(R.layout.notconnected);
         } 
         registerReceiver(mUpdateUIErrorReceiver, new IntentFilter(Constants.UPDATE_UI_ACTION_ERROR));
         registerReceiver(mUpdateUIReceiver, new IntentFilter(Constants.UPDATE_UI_ACTION));
     }
     
     public void connectToPush(View view) {
     	Log.d(Constants.LOG_TAG, "Connecting To Push");
     	C2DMessaging.register(getApplicationContext(), Constants.SENDER_ACCOUNT_EMAIL);
     	setBusyIndicator(true);
     	
     }
     
     public void disconnectToPush(View view) {
     	Log.d(Constants.LOG_TAG, "Disconnecting To Push");
     	C2DMessaging.unregister(getApplicationContext());
     	setBusyIndicator(true);
     }
     
     public void setBusyIndicator(boolean mode) {
 		ProgressBar bar = (ProgressBar)findViewById(R.id.progress_main);
 		if (bar != null) {
 			bar.setVisibility(mode?View.VISIBLE:View.INVISIBLE);
 		}
 	}
     
     private final BroadcastReceiver mUpdateUIErrorReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             setBusyIndicator(false);
             findViewById(R.id.connection_error).setVisibility(View.VISIBLE);
         }
     };
     
     private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         	setBusyIndicator(false);
             int status = intent.getIntExtra(Constants.STATUS_EXTRA, Constants.ERROR_STATUS);
         	if (status == Constants.REGISTERED_STATUS) {
         		setContentView(R.layout.connected);
         	} else if (status == Constants.UNREGISTERED_STATUS) {
         		setContentView(R.layout.notconnected);
        	} else if (status == Constants.ERROR_STATUS || status == Constants.AUTH_ERROR_STATUS) {
        		findViewById(R.id.connection_error).setVisibility(View.VISIBLE);
         	}
         }
     };
     
     
 }
