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
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 
 public class wififixer extends BroadcastReceiver {
 
 	private static final long PERIOD = 300000;
 	private static final long STARTDELAY = 60000;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// Create intent which will start the Service. 
		Intent myStarterIntent = new Intent(context, WifiFixerService.class);
 		// Set the Launch-Flag to the Intent. 
 		myStarterIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent=PendingIntent.getService(context, 0, myStarterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
		mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, (SystemClock.elapsedRealtime()+STARTDELAY), PERIOD, pendingintent);
 	}
 	
 }
