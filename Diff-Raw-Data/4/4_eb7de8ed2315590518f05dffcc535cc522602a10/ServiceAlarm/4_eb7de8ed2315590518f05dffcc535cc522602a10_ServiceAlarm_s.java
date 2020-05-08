 package org.wahtod.wififixer;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 
 public final class ServiceAlarm extends Object {
     public static final long PERIOD = 300000;
     public static final long STARTDELAY = 120000;
     private static final long NODELAY = 0;
 
     public static void setAlarm(Context c, boolean initialdelay) {
 	Long delay;
 	if (initialdelay)
 	    delay = PERIOD;
 	else
 	    delay = NODELAY;
 
	Intent myStarterIntent = new Intent(c, WifiFixerService.class);
 	myStarterIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	AlarmManager mgr = (AlarmManager) c
 		.getSystemService(Context.ALARM_SERVICE);
 	PendingIntent pendingintent = PendingIntent.getService(c, 0,
 		myStarterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 	mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
 		.elapsedRealtime()
 		+ delay, PERIOD, pendingintent);
     }
 
     public static void unsetAlarm(Context c) {
 	Intent myStarterIntent = new Intent(c, WifiFixerService.class);
 	myStarterIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	AlarmManager mgr = (AlarmManager) c
 		.getSystemService(Context.ALARM_SERVICE);
 	PendingIntent pendingintent = PendingIntent.getService(c, 0,
 		myStarterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 	mgr.cancel(pendingintent);
     }
 }
