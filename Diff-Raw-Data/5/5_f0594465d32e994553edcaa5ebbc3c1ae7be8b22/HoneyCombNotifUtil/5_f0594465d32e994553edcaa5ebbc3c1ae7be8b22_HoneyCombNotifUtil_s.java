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
 
 package org.wahtod.wififixer.LegacySupport;
 
 import org.wahtod.wififixer.R;
 import org.wahtod.wififixer.ui.WifiFixerActivity;
 import org.wahtod.wififixer.utility.NotifUtil;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.widget.RemoteViews;
 
 public class HoneyCombNotifUtil extends NotifUtil {
     @Override
     public void vaddNetNotif(final Context context, final String ssid,
 	    final String signal) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	Notification.Builder builder = new Notification.Builder(context);
 
 	if (ssid.length() > 0) {
 	    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
 	    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		    intent, 0);
 	    builder.setOnlyAlertOnce(true);
 	    builder.setOngoing(true);
 	    builder.setContentIntent(contentIntent);
 	    builder.setSmallIcon(R.drawable.wifi_ap);
 	    builder.setContentText(context
 		    .getString(R.string.open_network_found));
 	    builder.setWhen(System.currentTimeMillis());
 	    RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		    R.layout.net_notif_layout);
 	    contentView.setTextViewText(R.id.ssid, ssid);
 	    contentView.setTextViewText(R.id.signal, signal);
 	    builder.setContent(contentView);
 	    builder.setTicker(context.getText(R.string.open_network_found));
 	    /*
 	     * Fire notification, cancel if message empty: means no open APs
 	     */
 	    nm.notify(NotifUtil.NETNOTIFID, builder.getNotification());
 	} else
 	    nm.cancel(NotifUtil.NETNOTIFID);
 
     }
 
     @Override
     public void vaddStatNotif(Context ctxt, final String ssid, String status,
 	    final int signal, final boolean flag) {
 	ctxt = ctxt.getApplicationContext();
 	NotificationManager nm = (NotificationManager) ctxt
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (!flag) {
 	    nm.cancel(NotifUtil.STATNOTIFID);
 	    NotifUtil.statnotif = null;
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
 
 	if (NotifUtil.statnotif == null) {
 	    Notification.Builder builder = new Notification.Builder(ctxt);
 	    Intent intent = new Intent(ctxt, WifiFixerActivity.class)
 		    .setAction(Intent.ACTION_MAIN).setFlags(
 			    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 	    NotifUtil.contentIntent = PendingIntent.getActivity(ctxt, 0,
 		    intent, 0);
 	    builder.setContentIntent(NotifUtil.contentIntent);
 	    builder.setContent(new RemoteViews(ctxt.getPackageName(),
		    R.layout.status_notif_layout_black));
 	    builder.setOngoing(true);
 	    builder.setOnlyAlertOnce(true);
 	    builder.setSmallIcon(R.drawable.signal_level, signal);
 	    builder.setContentTitle(ctxt.getString(R.string.network_status));
 	    NotifUtil.statnotif = builder.getNotification();
 	}
 
 	if (NotifUtil.ssidStatus == NotifUtil.SSID_STATUS_UNMANAGED) {
 	    status = ctxt.getString(R.string.unmanaged) + status;
 	}
 	NotifUtil.statnotif.iconLevel = signal;
 	NotifUtil.statnotif.contentView.setImageViewResource(R.id.signal, icon);
 	NotifUtil.statnotif.contentView.setTextViewText(R.id.ssid,
 		truncateSSID(ssid));
 	NotifUtil.statnotif.contentView.setTextViewText(R.id.status, status);
 
 	/*
 	 * Fire the notification
 	 */
 	nm.notify(NotifUtil.STATNOTIFID, NotifUtil.statnotif);
 
     }
 
     @Override
     public void vaddLogNotif(final Context ctxt, final boolean flag) {
 
 	NotificationManager nm = (NotificationManager) ctxt
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (!flag) {
 	    nm.cancel(NotifUtil.LOGNOTIFID);
 	    return;
 	}
 	if (NotifUtil.lognotif == null) {
 	    Notification.Builder builder = new Notification.Builder(ctxt);
 	    Intent intent = new Intent(ctxt, WifiFixerActivity.class)
 		    .setAction(Intent.ACTION_MAIN).setFlags(
 			    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 	    NotifUtil.contentIntent = PendingIntent.getActivity(ctxt, 0,
 		    intent, 0);
 	    builder.setContentIntent(NotifUtil.contentIntent);
 	    builder.setContent(new RemoteViews(ctxt.getPackageName(),
		    R.layout.status_notif_layout_black));
 	    builder.setOngoing(true);
 	    builder.setOnlyAlertOnce(true);
 	    builder.setSmallIcon(R.drawable.logging_enabled);
 	    builder.setContentTitle(ctxt.getString(R.string.network_status));
 	    NotifUtil.lognotif = builder.getNotification();
 	    NotifUtil.lognotif.contentView.setImageViewResource(R.id.signal,
 		    R.drawable.logging_enabled);
 
 	}
 
 	NotifUtil.lognotif.contentView.setTextViewText(R.id.status,
 		getLogString(ctxt).toString());
 
 	/*
 	 * Fire the notification
 	 */
 	nm.notify(NotifUtil.LOGNOTIFID, NotifUtil.lognotif);
 
     }
 
     @Override
     public void vshow(final Context context, final String message,
 	    final String tickerText, final int id, PendingIntent contentIntent) {
 
 	/*
 	 * If contentIntent is NULL, create valid contentIntent
 	 */
 	if (contentIntent == null)
 	    contentIntent = PendingIntent.getActivity(context, 0, new Intent(),
 		    0);
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	Notification.Builder builder = new Notification.Builder(context);
 	builder.setTicker(tickerText);
 	builder.setWhen(System.currentTimeMillis());
 	builder.setSmallIcon(R.drawable.statusicon);
 	builder.setContentTitle(context.getText(R.string.app_name));
 	builder.setContentIntent(contentIntent);
 	builder.setContentText(message);
 	builder.setAutoCancel(true);
 
 	// unique ID
 	nm.notify(id, builder.getNotification());
 
     }
 }
