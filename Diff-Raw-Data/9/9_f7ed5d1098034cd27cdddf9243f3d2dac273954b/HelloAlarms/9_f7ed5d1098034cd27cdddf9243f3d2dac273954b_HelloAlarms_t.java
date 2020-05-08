 package com.fernferret.helloalarms;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.KeyguardManager;
 import android.app.KeyguardManager.KeyguardLock;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HelloAlarms extends Activity {
 	
 	private static final int SINGLE_ALARM_RC = 0;
 	private static final int MULTI_ALARM_RC = 1;
 	private static final int NO_FLAGS = 0;
 	private static final int TIME_IN_SECONDS_FOR_SINGLE_ALARM = 10;
 	private static final int TIME_IN_SECONDS_FOR_MULTI_ALARM = 7;
 	
 	private static final String MULTI_STATE = "MULTI";
 	private static final String SINGLE_STATE = "SINGLE";
 	private static final int WAKE_USER_REQUEST_CODE = 42;
 	
 	// Shared Toast
 	private Toast mToast;
 	
 	private SharedPreferences mSettings;
 	
 	// Single Alarm Variables
 	private long mSingleStartTime;
 	private Handler mSingleHandler = new Handler();
 	private TextView mTimeTillSingle;
 	private Button mSingleButton;
 	
 	// Multi Alarm Variables
 	private long mMultiStartTime;
 	private Handler mMultiHandler = new Handler();
 	private Button mMultiStartButton;
 	private Button mMultiStopButton;
 	private TextView mTimeTillMulti;
 	
 	PowerManager.WakeLock mWakeLock;
 	KeyguardManager mKeyguardManager;
 	KeyguardLock mKeyguardLock;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mSingleButton = (Button) findViewById(R.id.single_alarm);
 		mSingleButton.setOnClickListener(mSingleAlarmListener);
 		
 		mMultiStartButton = (Button) findViewById(R.id.start_button);
 		mMultiStartButton.setOnClickListener(mMultiAlarmStartListener);
 		
 		mMultiStopButton = (Button) findViewById(R.id.stop_button);
 		mMultiStopButton.setOnClickListener(mMultiAlarmStopListener);
 		
 		mTimeTillSingle = (TextView) findViewById(R.id.time_till_single);
 		mTimeTillMulti = (TextView) findViewById(R.id.time_till_next_multi);
 		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
 		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
 		mKeyguardLock = mKeyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
 	}
 	
 	@Override
 	protected void onResume() {
 		mSingleStartTime = mSettings.getLong(SINGLE_STATE, 0L);
 		mMultiStartTime = mSettings.getLong(MULTI_STATE, 0L);
 		
 		if (mSingleStartTime != 0L) {
 			mSingleHandler.removeCallbacks(mUpdateSingleTimerTask);
 			mSingleHandler.postDelayed(mUpdateSingleTimerTask, 100);
 			mSingleButton.setEnabled(false);
 		}
 		if (mMultiStartTime != 0L) {
 			mMultiHandler.removeCallbacks(mUpdateMultiTimerTask);
 			mMultiHandler.postDelayed(mUpdateMultiTimerTask, 100);
 			mMultiStartButton.setEnabled(false);
 			mMultiStopButton.setEnabled(true);
 		}
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause() {
 		Editor editor = mSettings.edit();
 		editor.putLong(SINGLE_STATE, mSingleStartTime);
 		editor.putLong(MULTI_STATE, mMultiStartTime);
 		editor.commit();
 		super.onPause();
 	}
 	
 	private OnClickListener mSingleAlarmListener = new OnClickListener() {
 		public void onClick(View view) {
 			
 			mSingleButton.setEnabled(false);
 			
 			Intent singleAlarmIntent = new Intent(HelloAlarms.this, SingleAlarm.class);
 			
 			PendingIntent singleAlarmPendingIntent = PendingIntent.getBroadcast(HelloAlarms.this, 
 					SINGLE_ALARM_RC, singleAlarmIntent, NO_FLAGS);
 			long currentTime = System.currentTimeMillis();
 			
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTimeInMillis(currentTime);
 			
 			calendar.add(Calendar.SECOND, TIME_IN_SECONDS_FOR_SINGLE_ALARM);
 			
 			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 			alarmManager.set(AlarmManager.RTC_WAKEUP, 
 					calendar.getTimeInMillis(), singleAlarmPendingIntent);
 			
 			if (mSingleStartTime == 0L) {
 				mSingleStartTime = currentTime;
 				mSingleHandler.removeCallbacks(mUpdateSingleTimerTask);
 				mSingleHandler.postDelayed(mUpdateSingleTimerTask, 100);
 			}
 			
 			/**
 			 * By using a shared toast, we can cancel a toast!
 			 */
 			if (mToast != null) {
 				mToast.cancel();
 			}
 			mToast = Toast.makeText(HelloAlarms.this, R.string.one_shot_scheduled, Toast.LENGTH_SHORT);
 			mToast.show();
 		}
 	};
 	
 	private OnClickListener mMultiAlarmStartListener = new OnClickListener() {
 		public void onClick(View view) {
 			// Allow the user to stop the multi alarm, but not start it again
 			mMultiStartButton.setEnabled(false);
 			mMultiStopButton.setEnabled(true);
 			
 			Intent multiAlarmIntent = new Intent(HelloAlarms.this, MultiAlarm.class);
 			
 			PendingIntent multiAlarmPendingIntent = PendingIntent.getBroadcast(HelloAlarms.this, MULTI_ALARM_RC, multiAlarmIntent, NO_FLAGS);
 			
 			// Set the first alarm time to fire at now plus whatever the time we have set for the alarm in seconds
 			long firstTime = SystemClock.elapsedRealtime();
 			firstTime += (TIME_IN_SECONDS_FOR_MULTI_ALARM * 1000);
 			
 			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, TIME_IN_SECONDS_FOR_MULTI_ALARM * 1000, multiAlarmPendingIntent);
 			
 			if (mMultiStartTime == 0L) {
 				mMultiStartTime -= (TIME_IN_SECONDS_FOR_MULTI_ALARM * 1000);
 				mMultiHandler.removeCallbacks(mUpdateMultiTimerTask);
 				mMultiHandler.postDelayed(mUpdateMultiTimerTask, 100);
 			}
 			
 			if (mToast != null) {
 				mToast.cancel();
 			}
 			mToast = Toast.makeText(HelloAlarms.this, R.string.starting_repeating_task, Toast.LENGTH_SHORT);
 			mToast.show();
 			
 		}
 	};
 	
 	private OnClickListener mMultiAlarmStopListener = new OnClickListener() {
 		public void onClick(View view) {
 			Intent multiAlarmIntent = new Intent(HelloAlarms.this, MultiAlarm.class);
 			
 			PendingIntent multiAlarmPendingIntent = PendingIntent.getBroadcast(HelloAlarms.this, MULTI_ALARM_RC, multiAlarmIntent, NO_FLAGS);
 			mMultiHandler.removeCallbacks(mUpdateMultiTimerTask);
 			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 			alarmManager.cancel(multiAlarmPendingIntent);
 			
 			if (mToast != null) {
 				mToast.cancel();
 			}
 			mToast = Toast.makeText(HelloAlarms.this, R.string.repeating_task_stopped, Toast.LENGTH_SHORT);
 			mToast.show();
 			mMultiStartButton.setEnabled(true);
 			mMultiStopButton.setEnabled(false);
 			mMultiStartButton.setText(R.string.start);
 			mTimeTillMulti.setText("");
 			mMultiStartTime = 0L;
 		}
 	};
 	
 	private Runnable mUpdateSingleTimerTask = new Runnable() {
 		public void run() {
 			long millis = SystemClock.uptimeMillis() + 100;
 			double displayTime = TIME_IN_SECONDS_FOR_SINGLE_ALARM - ((System.currentTimeMillis() - (mSingleStartTime + TIME_IN_SECONDS_FOR_SINGLE_ALARM)) / 1000.0);
 			int roundedTime = (int) displayTime;
 			if (displayTime < -2.0) {
 				mTimeTillSingle.setText("");
 				mSingleHandler.removeCallbacks(mUpdateSingleTimerTask);
 				mSingleStartTime = 0L;
 				mSingleButton.setEnabled(true);
 				mSingleButton.setText(getString(R.string.single_alarm_button_text));
 			} else if (displayTime < 0.0) {
 				wakeDevice();
 				mTimeTillSingle.setText("Event just fired!");
 				mSingleButton.setText(getString(R.string.single_alarm_button_text_with_param, roundedTime + 2));
 			} else {
 				mTimeTillSingle.setText(getString(R.string.time_till_single, displayTime));
 				mSingleButton.setText(getString(R.string.single_alarm_button_text_with_param, roundedTime + 2));
 			}
 			
 			mSingleHandler.postAtTime(this, millis);
 		}
 	};
 	
 	private Runnable mUpdateMultiTimerTask = new Runnable() {
 		public void run() {
 			long millis = SystemClock.uptimeMillis() + 100;
 			double displayTime = TIME_IN_SECONDS_FOR_MULTI_ALARM - ((SystemClock.elapsedRealtime() - (mMultiStartTime + TIME_IN_SECONDS_FOR_MULTI_ALARM)) / 1000.0);
 			int roundedTime = (int) displayTime;
 			if (displayTime <= 0.0) {
 				mMultiStartButton.setText(getString(R.string.multi_restarting, 0));
 				mTimeTillMulti.setText(getString(R.string.time_till_multi, 0.0));
 				mMultiStartTime = SystemClock.elapsedRealtime();
 			} else {
 				
 				mMultiStartButton.setText(getString(R.string.multi_restarting, roundedTime + 1));
 				mTimeTillMulti.setText(getString(R.string.time_till_multi, displayTime));
 			}
 			mMultiHandler.postAtTime(this, millis);
 		}
 	};
 	
 	private void wakeDevice() {
 		mWakeLock.acquire();
 		mKeyguardLock.disableKeyguard();
 		Intent wakeUser = new Intent(this, WakeUpAlert.class);
 		startActivityForResult(wakeUser, WAKE_USER_REQUEST_CODE);
 	}
 	
 	private void sleepDeviceAfterWoken() {
 		mKeyguardLock.reenableKeyguard();
 		mWakeLock.release();
 	}
 	
 }
