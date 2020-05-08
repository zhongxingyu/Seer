 package de.uniluebeck.itm.mdc.util;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
import de.uniluebeck.itm.mdc.MobileDataCollectorActivity;
 import de.uniluebeck.itm.mdc.R;
 
 public class Notifications {
 	
 	public static Notification createNotification(Context context, int contentId) {
 		final String content = context.getString(contentId);
 		return Notifications.createNotification(context, content);
 	}
 	
 	public static Notification createNotification(Context context, String content) {
 		final long when = System.currentTimeMillis();
		final Intent intent = new Intent(context, MobileDataCollectorActivity.class);
 		final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
 		final Notification notification = new Notification();
 		notification.setLatestEventInfo(context, context.getText(R.string.app_name), content, contentIntent);
 		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
 		notification.when = when;
 		notification.icon = R.drawable.ic_plugins;
 		return notification;
 	}
 	
 	
 }
