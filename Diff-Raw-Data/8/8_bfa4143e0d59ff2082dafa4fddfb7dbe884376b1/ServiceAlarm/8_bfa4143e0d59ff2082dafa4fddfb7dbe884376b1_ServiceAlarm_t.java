 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 
 public final class ServiceAlarm extends Object {
     public static final long PERIOD = 300000;
    public static final long STARTDELAY = 30000;
     private static final long NODELAY = 0;
 
     public static boolean alarmExists(final Context c) {
 	Intent intent = new Intent(IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
 	return (PendingIntent.getBroadcast(c, 0, intent,
 		PendingIntent.FLAG_NO_CREATE) != null);
 
     }
 
     public static void setAlarm(final Context c, final boolean initialdelay) {
 	Long delay;
 	if (initialdelay)
 	    delay = PERIOD;
 	else
 	    delay = NODELAY;
 
 	Intent intent = new Intent(IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
 	intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	AlarmManager mgr = (AlarmManager) c
 		.getSystemService(Context.ALARM_SERVICE);
 	PendingIntent pendingintent = PendingIntent.getBroadcast(c, 0, intent,
 		0);
 
 	mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
 		.elapsedRealtime()
 		+ delay, PERIOD, pendingintent);
     }
 
     public static void unsetAlarm(final Context c) {
 	Intent intent = new Intent(IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
 	intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	AlarmManager mgr = (AlarmManager) c
 		.getSystemService(Context.ALARM_SERVICE);
 	PendingIntent pendingintent = PendingIntent.getBroadcast(c, 0, intent,
 		0);
 
 	mgr.cancel(pendingintent);
     }
 
     public static void setLogTS(final Context c, final boolean state,
 	    final long delay) {
 	Intent intent = new Intent(c, LogService.class);
 	intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	intent.putExtra(LogService.APPNAME, LogService.TIMESTAMP);
 	intent.putExtra(LogService.Message, " ");
 	AlarmManager mgr = (AlarmManager) c
 		.getSystemService(Context.ALARM_SERVICE);
 	PendingIntent pendingintent = PendingIntent.getService(c, 0, intent, 0);
 	if (state)
 	    mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
 		    .elapsedRealtime()
 		    + delay, pendingintent);
 	else
 	    mgr.cancel(pendingintent);
     }
 }
