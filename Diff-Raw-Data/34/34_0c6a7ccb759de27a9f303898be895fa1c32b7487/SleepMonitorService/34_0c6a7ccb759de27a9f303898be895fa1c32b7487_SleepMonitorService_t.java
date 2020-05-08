 package com.pinokia.considerateapp;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.provider.Settings.SettingNotFoundException;
import android.os.Handler;
 import android.util.Log;
 
 public class SleepMonitorService extends Service {
 	public boolean initialized = false;
 	public boolean active = false;
 	private boolean awake = false;
	Handler serviceHandler;
	Runnable homescreenTask = new Runnable(){
		public void run() {
			//Launch our Lockscreen from here.
			Log.v("homescreenTask","launching ze missiles!");
		}
	};
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		Log.d(getClass().getSimpleName(), "onBind()");
 		return null;//we don't bind
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(getClass().getSimpleName(),"onCreate()");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		super.onStartCommand(intent, flags, startId);	
 
 		if (initialized)
 		{
 			return 1;
 		}
 		Log.v("onStartCommand", "SleepMonitorService started!");
 
		serviceHandler = new Handler();
 		startReceivers();
 		initialized=true;
 		return 1;
 	}	
 
 	void startReceivers() {
 		if (active) return;
 		IntentFilter onFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
 		IntentFilter offFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
 		// IntentFilter battfilter = new IntentFilter(Intent.ACTION_SCREEN_CHANGED);
 
 		registerReceiver(screenOn, onFilter);
 		registerReceiver(screenOff, offFilter);
 		// registerReceiver(battchange, battfilter);
 
 		active=true;
 	}
 
 	BroadcastReceiver screenOn = new BroadcastReceiver() {
 		public static final String TAG="screenOn";
 		public static final String OnIntent="android.intent.action.SCREEN_ON";
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(!intent.getAction().equals(OnIntent)) return;
 
 			Log.v(TAG, "Screen turned on!");
 			awake=true;
 			return;
 		}
 	};
 
 	BroadcastReceiver screenOff = new BroadcastReceiver() {
 		public static final String TAG="screenOff";
 		public static final String OffIntent="android.intent.action.SCREEN_OFF";
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(!intent.getAction().equals(OffIntent)) return;
 
 			Log.v(TAG, "Screen turned off!");
 			awake=false;
 
 			serviceHandler.postDelayed(homescreenTask, 500L);
 
 			return;
 		}
 
 	};
 }
