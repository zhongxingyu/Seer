 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.utility;
 
 
 
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.inbox.Inbox;
 import no.ntnu.idi.socialhitchhiking.journey.ListJourneys;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 
 
 
 public class SendNotification{
 
 	public static final int LIST_JOURNEY = 1364312654;
 	public static final int INBOX = 354632246;
 	private static NotificationManager nm;
 
 	public static void create(Context con,int id,String title, String msg,String ticker){
 		nm = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
 		int icon = R.drawable.car_icon;
 		CharSequence tickerText = ticker;
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification( icon, tickerText, when);
 		Context context = con.getApplicationContext();
 		CharSequence contentTitle = title;
 		
 		Intent notificationIntent;
 		PendingIntent contentIntent = null;
 		
 		CharSequence contentText = msg;
 		if(id == LIST_JOURNEY){
 			notificationIntent = new Intent(con, ListJourneys.class);
 			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			
 			contentIntent =
 					PendingIntent.getActivity(con, LIST_JOURNEY, notificationIntent, 0);
 		}
 		else if(id == INBOX){
 			notificationIntent = new Intent(con, Inbox.class);
 			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			contentIntent =
 					PendingIntent.getActivity(con, INBOX, notificationIntent, 0);
 		}
 
 		long[] vib = {0,100,200,300};
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
		//notification.sound = Uri.parse("android.resource://no.ntnu.idi.socialhitchhiking/" + R.raw.notif_sound_1);
		notification.sound = Uri.parse("android.resource://no.ntnu.idi.socialhitchhiking/" + R.raw.notif);
 		notification.vibrate = vib;
 		notification.setLatestEventInfo(context,contentTitle,contentText,contentIntent);
 
 		nm.notify( id, notification );
 	}
 
 }
