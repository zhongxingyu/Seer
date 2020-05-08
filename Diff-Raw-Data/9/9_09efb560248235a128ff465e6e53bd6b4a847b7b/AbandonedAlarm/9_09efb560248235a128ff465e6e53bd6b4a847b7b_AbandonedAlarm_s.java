 package com.corridor9design.abandonedandroid;
 
 import java.util.Calendar;
 import java.util.Random;
 
 import android.annotation.SuppressLint;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class AbandonedAlarm extends BroadcastReceiver {
 	
 	Calendar nextAlarm = Calendar.getInstance();
 	
 	public AbandonedAlarm() {
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		launchNotification(context, 1);
 		
 		// get the number of seconds until the next alarm
 		nextAlarm(context, randomizeAlarm(30, 1, 1)); // TODO create interface that allows user to set longestSpan, shortestSpan, and severity
 	}
 	
 	@SuppressLint("Wakelock")
 	private void launchNotification(Context context, int severity){
 		
 		if(MainActivity.getPreferences("isFirstRun", context).equals("yes")){
 			Toast.makeText(context, "Don't forget about me...", Toast.LENGTH_SHORT).show();
 			MainActivity.setPreferences("isFirstRun", "no", context);
 			return;
 		}
 		
 		@SuppressWarnings("deprecation")
 		WakeLock screenOn = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ShowLoneliness");
 		screenOn.acquire();
 		
 		NotificationManager nManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
 			.setAutoCancel(true)
 			.setSmallIcon(R.drawable.ic_launcher)
 			.setTicker("I'm getting lonely here...")
 			.setContentTitle("Abandoned")
 			.setContentText("Letting you know when I'm neglected...")
 			.setDefaults(Notification.DEFAULT_ALL);
 		
 		// OPEN APPLICATION ON CLICK
 		// Creates an explicit intent for an Activity in your app
 		Intent resultIntent = new Intent(context, MainActivity.class);
 
 		// The stack builder object will contain an artificial back stack for the
 		// started Activity.
 		// This ensures that navigating backward from the Activity leads out of
 		// your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(MainActivity.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		notification.setContentIntent(resultPendingIntent);
 		
 		
 		nManager.notify(6810293, notification.build());
 		Log.d("Notification", "Completed");
 		screenOn.release();
 
 	}
 	
 	private void nextAlarm(Context context, int seconds){
 		Intent intent = new Intent(context, AbandonedAlarm.class);
 		PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		AlarmManager amanager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		nextAlarm.add(Calendar.SECOND, seconds);
 		
 		amanager.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pending);
 		
 	}
 	
 	private int randomizeAlarm(int longestSpan, int shortestSpan, double severity){
 		Random rand = new Random();
 		// longestSpan = the longest length of time the device will go without alarming
 		// shortestSpan = the shortest length of time the device will go before alarming again
 		// severity = Divider that will shorten the longestSpan depending on sliding scale
 		
 		int initialLength = rand.nextInt((longestSpan - shortestSpan + 1) + shortestSpan);
 		int finalCountdown = (int) (initialLength / severity);
 		
 		
 		System.out.println(initialLength + "     " + severity);
 		Log.d("Next Alarm", finalCountdown + " seconds remaining");
 		return finalCountdown;
 	}
 	
 	public static void setPreferences(String key, String value, Context context) {
 		SharedPreferences preferences = PreferenceManager
 			.getDefaultSharedPreferences(context);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString(key, value);
 		editor.commit();
 	}
 
 	public static String getPreferences(String key, Context context) {
 		SharedPreferences preferences = PreferenceManager
 			.getDefaultSharedPreferences(context);
 		return preferences.getString(key, "0");
 	}
 
 }
