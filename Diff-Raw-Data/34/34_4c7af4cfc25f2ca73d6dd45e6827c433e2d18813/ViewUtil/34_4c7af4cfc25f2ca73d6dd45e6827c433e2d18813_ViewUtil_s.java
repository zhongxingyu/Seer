 package com.quanleimu.util;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.activity.ThirdpartyTransitActivity;
 import com.quanleimu.broadcast.CommonIntentAction;
 
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class ViewUtil {
 	
 	public static void postShortToastMessage(final View parent, final String message, long delay)
 	{
 		if (parent == null || message == null)
 		{
 			return;
 		}
 		
 		final long delayTime = delay > 0 ? delay : 0;
 		
 		parent.postDelayed(new Runnable() {
 
 			public void run() {
 				Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT).show();
 			}
 			
 		}, delayTime);
 	}
 	
 	public static void postShortToastMessage(final View parent,final int resId, long delay)
 	{
 		if (parent == null)
 		{
 			return;
 		}
 		
 		final long delayTime = delay > 0 ? delay : 0;
 		
 		parent.postDelayed(new Runnable() {
 
 			public void run() {
 				Toast.makeText(parent.getContext(), resId, Toast.LENGTH_SHORT).show();
 			}
 			
 		}, delayTime);
 	}
 	
 	public static void removeNotification(Context context, final int notificationid)
 	{
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
 		wk.acquire(1000);
 		
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.cancel(notificationid);
 	}
 	
 	public static void putOrUpdateNotification(final Context context, final int notificationId, String notificationType, String title, String msg, final Bundle extras, final boolean isPersistant)
 	{
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
 		wk.acquire(1000);
 		
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		String tickerText = msg;//message.getMessage();
 		int icon = R.drawable.push_icon;
 
 		String contentTitle = title == null ? "" : title;
 		String contentText = msg;
 
 		Intent notificationIntent = notificationType == null ? new Intent() : new Intent(notificationType);
 		if (extras != null)
 		{
 			notificationIntent.putExtras(extras);
 		}
 		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
 				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		Notification notification = new Notification(icon, tickerText,
 				System.currentTimeMillis());
 		notification.defaults |= Notification.DEFAULT_SOUND;
 		notification.defaults |= Notification.DEFAULT_VIBRATE;
 		if (isPersistant)
 		{
 			notification.flags |= Notification.FLAG_NO_CLEAR;
 		}
 		else
 		{
 			notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		}
 		
 		notification.setLatestEventInfo(context, contentTitle, contentText,
 				contentIntent);
 		
 		mNotificationManager.notify(notificationId, notification);
 	}
 	
 	public static void pickupPhoto(final Context context, final int tmpFileIndex)
 	{
 		View view = LinearLayout.inflate(context, R.layout.upload_head, null);
 		Builder builder = new AlertDialog.Builder(context);
 		builder.setView(view);
 
 		final AlertDialog ad = builder.create();
 		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
 		lp.y = 300;
 		ad.onWindowAttributesChanged(lp);
 		ad.show();
 		
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				ad.dismiss();
 				
 				switch(v.getId())
 				{
 					case R.id.photo_album:
 						Intent startAlbum = new Intent(context,
 								ThirdpartyTransitActivity.class);
 						Bundle ex = new Bundle();
 						ex.putString(ThirdpartyTransitActivity.ThirdpartyKey,
 								ThirdpartyTransitActivity.ThirdpartyType_Albam);
 						startAlbum.putExtras(ex);
 						context.startActivity(startAlbum);
 						break;
 					case R.id.photo_make:
 						Intent startCap = new Intent(context,
 								ThirdpartyTransitActivity.class);
 						Bundle ext = new Bundle();
 						ext.putString(ThirdpartyTransitActivity.ThirdpartyKey,
 								ThirdpartyTransitActivity.ThirdpartyType_Photo);
 						ext.putInt(ThirdpartyTransitActivity.Name_PhotoNumber, tmpFileIndex);
 						startCap.putExtras(ext);
 						context.startActivity(startCap);
 						break;
 					case R.id.photo_cancle:
 						break;
 				}
 			}
 		};
 
 		view.findViewById(R.id.photo_album).setOnClickListener(listener);
 		view.findViewById(R.id.photo_make).setOnClickListener(listener);
 		view.findViewById(R.id.photo_cancle).setOnClickListener(listener);
 	}
 	
 	static public Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight)
 	{
		Float width  = new Float(srcBmp.getWidth());
		Float height = new Float(srcBmp.getHeight());
 		Float ratio = width/height;
 		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);
 
 		return thumbnail;
 	}
 	
 }
