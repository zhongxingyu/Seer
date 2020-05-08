 package com.zhuri.talk;
 
 import android.app.Service;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.os.Bundle;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.os.PowerManager;
 import android.os.BatteryManager;
 import android.util.Log;
 import android.widget.Toast;
 import android.net.wifi.WifiManager;
 
 import com.zhuri.slot.*;
 import com.zhuri.talk.RoidTalkRobot;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 
 public class TalkService extends Service implements Runnable {
 	private Thread worker = null;
 	private boolean running = false;
 	private RoidTalkRobot mClient = null;
 	private WifiManager.WifiLock mWifiLock = null;
 	private PowerManager.WakeLock mWakeLock = null;
 	private LocationManager mLocationManager = null;
 	
 	static final String TAG = "TALK";
 	static final long INTERVAL_LONG = 45 * 60 * 1000;
 	static final long INTERVAL_SHORT = 3 * 60 * 1000;
 	public static final String INTENT_CHANGE_LOCATION_SETTING = "com.zhuri.change_location_setting";
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	public static Intent serviceIntent(Context context) {
 		Intent intent = new Intent(context, TalkService.class);
 		return intent;
 	}
 
 	private String mProvider = null;
 	final LocationListener mLocationListener = new LocationListener() {
 		@Override
 		public void onLocationChanged(Location location) {
 			if (mClient != null && location != null)
 				mClient.updateLocation(formatLocation(location));
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 
 		}
 	};
 
 	private String formatLocation(Location location) {
 		double altitude  = location.getAltitude();
 		double latitude  = location.getLatitude();
 		double longitude = location.getLongitude();
 
 		StringBuilder builder = new StringBuilder();
 		if (mProvider != null) {
 			builder.append("provider: ");
 			builder.append(mProvider);
 			builder.append("\n");
 		}
 		builder.append("altitude: ");
 		builder.append(altitude);
 		builder.append("\n");
 		builder.append("latitude: ");
 		builder.append(latitude);
 		builder.append("\n");
 		builder.append("longitude: ");
 		builder.append(longitude);
 		builder.append("\n");
 		builder.append("https://maps.google.com/maps?ll=" + latitude + "," + longitude + "&spn=0.004710,0.007832&t=k&hl=en");
 
 		return builder.toString();
 	}
 
 	private void startLocationListen(String provider) {
 		if (provider != null) {
 			mProvider = provider;
 			mLocationManager.removeUpdates(mLocationListener);
 			mLocationManager.requestLocationUpdates(provider, 5000, 5, mLocationListener);
 			Location location = mLocationManager.getLastKnownLocation(provider);
 			if (mClient != null && location != null)
 				mClient.updateLocation(formatLocation(location));
 		}
 		return;
 	}
 
 	public BroadcastReceiver locationSettingRcvr = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(INTENT_CHANGE_LOCATION_SETTING)) {
 				String action = intent.getStringExtra("action");
 				String provider = intent.getStringExtra("provider");
 
 				if (action != null) {
 					if (action.equals("start")) {
 						startLocationListen(provider);
 					} else if (action.equals("stop")) {
 						mLocationManager.removeUpdates(mLocationListener);
 					}
 				}
 			}
 		}
 	};
 
 	public BroadcastReceiver batteryLevelRcvr = new BroadcastReceiver() {
 		private String lastOuted = "";
 
 		public void onReceive(Context context, Intent intent) {
 			Log.v(TAG, "battery status change");
 
 			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
 
 				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
 				int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
 				int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
 				int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
 				int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
 				boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
 				String  technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
 
 				StringBuilder builder = new StringBuilder();
 				builder.append("battery " + level + "/" + scale);
 				builder.append(" ");
 
 				switch (plugged) {
 					case BatteryManager.BATTERY_PLUGGED_AC:
 						builder.append("ac");
 						break;
 
 					case BatteryManager.BATTERY_PLUGGED_USB:
 						builder.append("usb");
 						break;
 
 					default:
 						if (plugged != 0)
 							builder.append("plugged=" + plugged);
 						break;
 				}
 
 				builder.append(" ");
 
 				switch (status) {
 					case BatteryManager.BATTERY_STATUS_UNKNOWN:
 						builder.append("status");
 						break;
 
 					case BatteryManager.BATTERY_STATUS_CHARGING:
 						builder.append("charging");
 						break;
 
 					case BatteryManager.BATTERY_STATUS_DISCHARGING:
 					case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
 						builder.append("discharging");
 						break;
 
 					case BatteryManager.BATTERY_STATUS_FULL:
 						builder.append("full");
 						break;
 
 					default:
 						builder.append("status=" + status);
 						break;
 				}
 
 				builder.append(" ");
 				switch (health) {
 					case BatteryManager.BATTERY_HEALTH_GOOD:
 						builder.append("good");
 						break;
 
 					case BatteryManager.BATTERY_HEALTH_DEAD:
 						builder.append("dead");
 						break;
 
 					case BatteryManager.BATTERY_HEALTH_OVERHEAT:
 						builder.append("overheat");
 						break;
 
 					case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
 						builder.append("overvoltage");
 						break;
 
 					case BatteryManager.BATTERY_HEALTH_UNKNOWN:
 						builder.append("health");
 						break;
 
 					case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
 						builder.append("unspecified-failure");
 						break;
 
 					default:
 						builder.append("health=" + health);
 						break;
 				}
 
 				if (mClient != null && !lastOuted.equals(builder.toString())) {
 					lastOuted = builder.toString();
 					builder.append(" " + temperature + ".C");
 					builder.append(" " + technology);
 					builder.append(" " + voltage + "mV");
 					if (present == false)
 						builder.append(" lost battery");
 					mClient.updatePresence(builder.toString());
 				}
 
 				return;
 			}
 		}
 	};
 
 	private class AlarmReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (!mWakeLock.isHeld())
 				mWakeLock.acquire(INTERVAL_SHORT);
 			Log.i(TAG, "AlarmReceiver onReceive");
 			return;
 		}
 	};
 
 	private Intent mIntent = null;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.i(TAG, "onCreate");
 
 		mIntent = new Intent(this, AlarmReceiver.class);
 		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 
 		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 		mWifiLock = wifiManager.createWifiLock("com.zhuri.talk");
 		mWifiLock.acquire();
 
 		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
 		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.zhuri.talk");
		mWakeLock.setReferenceCounted(false);
 
 		SlotThread.Init();
 		worker = new Thread(this);
 		running = true;
 		worker.start();
 
 		long realtime = SystemClock.elapsedRealtime() + INTERVAL_LONG;
 		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mIntent, 0);
 		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, realtime, INTERVAL_LONG, pendingIntent);
 
 		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 		registerReceiver(batteryLevelRcvr, batteryLevelFilter);
 
 		IntentFilter locationSettingFilter = new IntentFilter(INTENT_CHANGE_LOCATION_SETTING);
 		registerReceiver(locationSettingRcvr, locationSettingFilter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.i(TAG, "onDestroy");
 		
 		SlotThread.quit();
 
 		try {
 			worker.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		if (running) {
 			throw new RuntimeException("worker thread should not running.");
 		}
 		
 		unregisterReceiver(locationSettingRcvr);
 		unregisterReceiver(batteryLevelRcvr);
 
 		if (mWifiLock.isHeld())
 			mWifiLock.release();
 		if (mWakeLock.isHeld())
 			mWakeLock.release();
 		worker = null;
 
 		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mIntent, 0);
 		am.cancel(pendingIntent);
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		Log.i(TAG, "onStart");
 		super.onStart(intent, startId);
 	}
 
 	private void initialize() {
 		mClient = new RoidTalkRobot(this);
 		mClient.start();
 		return;
 	}
 
 	@Override
 	public void run() {
 		Log.i(TAG, "run prepare");
 
 		initialize();
 		try {
 			while (SlotThread.step());
 		} catch (Exception e) {
 			e.printStackTrace();
 			stopSelf();
 		}
 
 		mClient.close();
 		running = false;
 		Log.i(TAG, "run finish");
 	}
 }
