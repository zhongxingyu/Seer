 package com.relurori.breakout;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.OrientationEventListener;
 import android.view.OrientationListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class MainPanelActivity extends Activity {
 
 	private static final String TAG = MainPanelActivity.class.getSimpleName();
 
 	OrientationEventListener listener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Intent i = getIntent();
 		String key = i.getStringExtra("genericKey");
 
 		Log.d(TAG, "Requesting no title");
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		Log.d(TAG, "Setting full screen");
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(new MainPanel(getBaseContext(), MainPanelActivity.this));
 	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "Destroy");
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stop");
		super.onStop();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "Pause");
		super.onPause();
	}
 }
