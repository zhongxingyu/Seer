 package com.pengjun.ka.utils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.widget.DatePicker;
 
 import com.pengjun.ka.R;
 
 public class ComponentUtils {
 
 	public static LayoutInflater inflater = null;
 
 	public static Dialog createInfoDialog(Context context, String info) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setIcon(R.drawable.title_warning);
 		builder.setTitle("信息");
 		builder.setMessage(info);
 		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 			}
 		});
 		return builder.create();
 	}
 
 	public static String DatePicker2FormatStr(DatePicker dp) {
 		return String
 				.format(TimeUtils.DATE_STRING_FORMT, dp.getYear(), dp.getMonth() + 1, dp.getDayOfMonth());
 	}
 
 	public static LayoutInflater getLayoutInflater(Context context) {
 		if (inflater == null) {
 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 		return inflater;
 	}
 
 	public static void createNotification(Context context, String notificationTitle,
			String notificationCotent, PendingIntent pendingIntent, int notifyId) {
 
 		Notification notification = new Notification(R.drawable.ic_launcher, notificationTitle,
 				System.currentTimeMillis());
 
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notification.tickerText = notificationCotent;
 		notification.defaults = Notification.DEFAULT_SOUND;
 		notification.defaults |= Notification.DEFAULT_VIBRATE;
 		notification.setLatestEventInfo(context, notificationTitle, notificationCotent, pendingIntent);
 
 		NotificationManager notificationManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(notifyId, notification);
 	}
 
 	public static void putIntentStringData(Intent intent, Bundle bundle, String key, String value) {
 		bundle.putSerializable(key, value);
 		intent.putExtras(bundle);
 	}
 
 	public static Object getIntentData(Intent intent, String key) {
 		return intent.getSerializableExtra(key);
 	}
 
 }
