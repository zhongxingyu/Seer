 package com.example.tracker;
 
 import java.util.List;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.graphics.PixelFormat;
 
 import android.view.View.OnTouchListener;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 import android.provider.Settings.Secure;
 
 public class TrackerService extends Service implements OnTouchListener{
 
 	private String TAG = this.getClass().getSimpleName();
 	public static String deviceID = null;
 	private BroadcastReceiver receiver = null;
 	private boolean isScreenOn = false;
 	
 	private LinearLayout fakeLayout;
 	private WindowManager mWindowManager;
 	
 	/** UserPresent is more important to flag to start or stop to track the user behavior */
 	private boolean isUserPresent = false;
 	/** Keep the previous "RecentTaskList" to compare with latest one, 
 	 * if not match, one application has been opened */
 	private List<ActivityManager.RecentTaskInfo> recentTaskListPrevious = null;
 	
 	private SystemStatus previousStatus = SystemStatus.INAPP;
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		deviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
 		Log.i(TAG, "DeviceID:" + deviceID);
 		Log.i(TAG, "Service onCreate: the number of processes is " + getTotalRunningApp());
 		
 		/** Create and configure the fake layout for service */
 		fakeLayout = new LinearLayout(this);
 		LayoutParams layoutPrams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
 		fakeLayout.setLayoutParams(layoutPrams);
 		fakeLayout.setOnTouchListener(this);
 		
 		/** Fetch WindowManager and add fake layout to it */
 		mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
 		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
 				0,
 				WindowManager.LayoutParams.MATCH_PARENT,
 				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
 				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                 PixelFormat.TRANSLUCENT);
 		params.gravity = Gravity.LEFT | Gravity.TOP;
 		mWindowManager.addView(fakeLayout, params);
 		
 		/** Initialize the recentTaskListPrevious */
 		updateRecentTaskListPrevious();
 		previousStatus = SystemStatus.INAPP;
 		
 		/** Create the filter to contain three Actions: ScreenOn, ScreenOff, UserPresent */
 		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
 		filter.addAction(Intent.ACTION_SCREEN_OFF);
 		filter.addAction(Intent.ACTION_USER_PRESENT);
 		
 		/** Register the Broadcast Receiver to make it work */
 		receiver = new ScreenReceiver();
 		registerReceiver(receiver, filter);	
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// TODO Auto-generated method stub
 		
 		/** Phone has three state: 
 		 * 		Screen Off: turn off the screen
 		 * 	    Screen On:
 		 * 			1.User not Present: before unlock the phone;
 		 * 			2.User Present: phone unlocked.
 		 */
 		
 		isScreenOn = intent.getBooleanExtra("isScreenOn", true);
 		isUserPresent = intent.getBooleanExtra("isUserPresent", true);
 		
 		
 		if(isScreenOn) {
 			Log.i(TAG, "Screen is on!");
 //			AggregateMessages.addMessages("Screen is on!");
 		} else {
 			Log.i(TAG, "Screen is off!");
 		}
 		
 		if(isUserPresent) {
 			Log.i(TAG, "User is present!");
 //			AggregateMessages.addMessages(deviceID);
 			AggregateMessages.addMessages("START", false);
 			/** Start the tracking */
 		} else {
 			Log.i(TAG, "User not present!");
 //			AggregateMessages.addMessages("User not present!");
 			/** Stop the tracking */
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		/** Before destroy to unregister the Broadcast Receiver first(Avoid memory leak)*/
 		unregisterReceiver(receiver);
 		
 		if(mWindowManager != null) {
 			if(fakeLayout != null) {
 				mWindowManager.removeView(fakeLayout);
 			}
 		}
 		
 		Log.i(TAG, "Boardcast Receiver Unregistered.");
 		super.onDestroy();
 		Log.i(TAG, "Service onDestroy.");
 	}
 	
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		// TODO Auto-generated method stub
 		/** Delay 1 second to check the application status
 		 * give application sometime to bring up or move to the front
 		 */
 		if(arg1.getAction() ==  MotionEvent.ACTION_OUTSIDE) {
 			/** Log the raw touch location data */
 			/** Test Result: always return (0,0), so gesture detection is not possible */
 			//Log.i(TAG, Float.toString(arg1.getRawX()));
 			//Log.i(TAG, Float.toString(arg1.getRawY()));
 		    Handler handler = new Handler(); 
 		    handler.postDelayed(new Runnable() { 
 		         public void run() { 
 		        	 SystemStatus status = trackStatus();
 		        	 Log.i(TAG, "Recorded Touch Outside the view.");
 		        	 Log.i(TAG, "TimeStamp: " + System.nanoTime() + "  Sys_Status:" + status); 
 		        	 AggregateMessages.addMessages("TimeStamp: " + System.nanoTime() + "  Sys_Status:" + status, false);
 		         } 
 		    }, 1000); 
 		}
 		return true;
 	}
 
 	/** Return the number of running processes right now */
 	public int getTotalRunningApp() {
 	    ActivityManager actvityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
 	    List<RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
 	    return procInfos.size();
 	}
 	
 	public SystemStatus trackStatus() {
 		/** Get latest recentTaskList */
 		ActivityManager actvityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
 		List<ActivityManager.RecentTaskInfo> recentTaskList = actvityManager.getRecentTasks(5, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
 		/** Compare the recentTaskList with previous */
 		/** Need to be optimized in the future */
 		for(int i = 0; i < recentTaskList.size(); i++) {
 			ActivityManager.RecentTaskInfo recent = recentTaskList.get(i);
 			/** Check the very first process */
 			if(i == 0) {
 				ActivityManager.RecentTaskInfo previous = recentTaskListPrevious.get(i);
 				
 				Log.i(TAG, "Recent ID:" + recent.persistentId);
 				Log.i(TAG, "Previous Id:" + previous.persistentId);
 				
 				if(recent.persistentId == 3) {
 					if(previousStatus == SystemStatus.INAPP) {
 						previousStatus = SystemStatus.MAINM;
 						recentTaskListPrevious = recentTaskList;
 						return SystemStatus.SWMAN;
 					} else {
 						previousStatus = SystemStatus.MAINM;
 						recentTaskListPrevious = recentTaskList;
 						return SystemStatus.MAINM;
 					}
 				} else {
 					if(previousStatus == SystemStatus.MAINM) {
 						previousStatus = SystemStatus.INAPP;
 						recentTaskListPrevious = recentTaskList;
 						return SystemStatus.SWAPP;						
 					} else if(previousStatus == SystemStatus.INAPP) {
 						if(recent.persistentId == previous.persistentId) {
 							previousStatus = SystemStatus.INAPP;
 							recentTaskListPrevious = recentTaskList;
 							return SystemStatus.INAPP;	
 						} else {
 							previousStatus = SystemStatus.INAPP;
 							recentTaskListPrevious = recentTaskList;
 							return SystemStatus.SWAPP;
 						}
 					}
 				}
 			}
 		}	
 		return SystemStatus.ERROR;	
 	}
 	
 	public void updateRecentTaskListPrevious() {
 		ActivityManager actvityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
 		recentTaskListPrevious = actvityManager.getRecentTasks(5, ActivityManager.RECENT_IGNORE_UNAVAILABLE);		
 	}
 }
