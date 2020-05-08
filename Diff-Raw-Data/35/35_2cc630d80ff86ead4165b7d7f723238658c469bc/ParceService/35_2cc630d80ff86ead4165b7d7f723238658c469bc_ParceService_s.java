 package com.argando.parcersample.backgroundupdate;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
import android.content.Context;
 import android.content.Intent;
import android.net.ConnectivityManager;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.util.Log;
 
 /**
  * User: argando
  * Date: 21.07.12
  * Time: 16:56
  */
 public class ParceService extends Service
 {
 	private static final String LOG_TAG = ParceService.class.getSimpleName();
	private static final int INTERVAL = 1000 * 60 * 5; // 30 sec
 	private static final int FIRST_RUN = 5000; // 5 seconds
 	private int REQUEST_CODE = 111223232;
 
 	private AlarmManager alarmManager;
 
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 
 		startService();
 		Log.v(LOG_TAG, "onCreate(..)");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		Log.v(LOG_TAG, "onBind(..)");
 		return null;
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		if (alarmManager != null)
 		{
 			Intent intent = new Intent(this, ParceService.class);
 			alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
 		}
 		Log.v(LOG_TAG, "Service onDestroy(). Stop AlarmManager at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
 	}
 
 	private void startService()
 	{
 		Intent intent = new Intent(this, UpdateReceiver.class);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);
 		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + FIRST_RUN, INTERVAL, pendingIntent);
 
 		Log.v(LOG_TAG, "AlarmManger started at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
 	}
 }
