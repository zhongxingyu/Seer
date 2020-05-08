 /*Copyright [2010-2011] [David Van de Ven]
 
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
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.widget.RemoteViews;
 
 public class NotifUtil {
     public static final int NETNOTIFID = 8236;
     private static final int STATNOTIFID = 2392;
     private static final int MAX_SSID_LENGTH = 16;
     private static int ssidStatus = 0;
 
     /*
      * for SSID status in status notification
      */
     public static final int SSID_STATUS_UNMANAGED = 3;
     public static final int SSID_STATUS_MANAGED = 7;
    private static final String NULL_SSID = "empty";
 
     private NotifUtil() {
 
     }
 
     public static void addNetNotif(final Context context, final String ssid,
 	    final String signal) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (ssid.length() > 0) {
 	    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
 	    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		    intent, 0);
 
 	    Notification netnotif = new Notification(R.drawable.wifi_ap,
 		    context.getString(R.string.open_network_found), System
 			    .currentTimeMillis());
 	    RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		    R.layout.net_notif_layout);
 	    contentView.setTextViewText(R.id.ssid, ssid);
 	    contentView.setTextViewText(R.id.signal, signal);
 	    netnotif.contentView = contentView;
 	    netnotif.contentIntent = contentIntent;
 	    netnotif.flags = Notification.FLAG_ONGOING_EVENT;
 	    netnotif.tickerText = context.getText(R.string.open_network_found);
 	    /*
 	     * Fire notification, cancel if message empty: means no open APs
 	     */
 	    nm.notify(NETNOTIFID, netnotif);
 	} else
 	    nm.cancel(NETNOTIFID);
 
     }
 
     public static void addStatNotif(final Context context, final String ssid,
 	    String status, final int signal, final boolean flag) {
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (!flag) {
 	    nm.cancel(STATNOTIFID);
 	    return;
 	}
 
 	int icon = 0;
 	switch (signal) {
 	case 0:
 	    icon = R.drawable.signal0;
 	    break;
 	case 1:
 	    icon = R.drawable.signal1;
 	    break;
 	case 2:
 	    icon = R.drawable.signal2;
 	    break;
 	case 3:
 	    icon = R.drawable.signal3;
 	    break;
 	case 4:
 	    icon = R.drawable.signal4;
 	    break;
 	}
 
 	Notification statnotif = new Notification(icon, context
 		.getString(R.string.network_status), System.currentTimeMillis());
 
 	Intent intent = new Intent(context, WifiFixerActivity.class).setAction(
 		Intent.ACTION_MAIN).setFlags(
 		Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 
 	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		intent, 0);
 	statnotif.contentIntent = contentIntent;
 	statnotif.flags = Notification.FLAG_ONGOING_EVENT;
 
 	if (ssidStatus == SSID_STATUS_UNMANAGED) {
	    status = status + context.getString(R.string.unmanaged);
 	}
 	statnotif.setLatestEventInfo(context, truncateSSID(ssid), status,
 		contentIntent);
 	statnotif.iconLevel = signal;
 
 	/*
 	 * Fire the notification
 	 */
 	nm.notify(STATNOTIFID, statnotif);
 
     }
 
     public static void setSsidStatus(final int color) {
 
 	ssidStatus = color;
     }
 
     public static void show(final Context context, final String message,
 	    final String tickerText, final int id, PendingIntent contentIntent) {
 
 	/*
 	 * If contentIntent is NULL, create valid contentIntent
 	 */
 	if (contentIntent == null)
 	    contentIntent = PendingIntent.getActivity(context, 0, new Intent(),
 		    0);
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	CharSequence from = context.getText(R.string.app_name);
 
 	Notification notif = new Notification(R.drawable.statusicon,
 		tickerText, System.currentTimeMillis());
 
 	notif.setLatestEventInfo(context, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(id, notif);
 
     }
 
     public static String truncateSSID(String ssid) {
	if (ssid == null || ssid.length() < 1)
	    return NULL_SSID;
	else
 	if (ssid.length() < MAX_SSID_LENGTH)
 	    return ssid;
 	else
 	    return ssid.substring(0, MAX_SSID_LENGTH);
 
     }
 
     public static void cancel(final int notif, final Context context) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 	nm.cancel(notif);
     }
 
 }
