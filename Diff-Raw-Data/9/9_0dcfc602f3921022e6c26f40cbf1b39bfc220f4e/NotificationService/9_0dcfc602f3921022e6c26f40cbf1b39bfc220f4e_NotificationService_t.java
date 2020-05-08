 package com.ilsecondodasinistra.workitout;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.Notification.Builder;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.IBinder;
 
 public class NotificationService extends Service {
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public int onStartCommand(Intent intent, int flags, int startId) {   
 		
 		//Delete old notifications
 		CancelAllNotifications(getApplicationContext());
 
         Context context = getApplicationContext();
         NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 	        // show the notification now
 //	        Toast.makeText(context, "Siamo nel caso Android 4.0", 1000).show();
 	        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, WorkItOutMain.class), 0); // open MainActivity if the user selects this notification
 	        
 	        String longText = (String)getText(R.string.longTextForNotification);
 	        
 	        Builder builder = new Notification.Builder(this);
 	        builder.setContentTitle(context.getString(R.string.notificationTitle))
 	        		.setContentText(context.getString(R.string.notificationMessage))
 	        		.setSmallIcon(R.drawable.workitout_icon)
 	        		.setContentIntent(pi)
 	        		.build();
 	        
 	        Notification mNotification = new Notification.BigTextStyle(builder).bigText(context.getString(R.string.longTextForNotification)).build();
 	        
 	//        Notification mNotification = new Notification(R.drawable.workitout_icon, context.getString(R.string.app_name), System.currentTimeMillis());
 	
 			//Sound
 			mNotification.defaults |= Notification.DEFAULT_SOUND;
 	
 			//Vibration
 	        long[] vibrate = {0,100,200,300};
 	        mNotification.vibrate = vibrate;
 	//		mNotification.defaults |= Notification.DEFAULT_VIBRATE;
 	
 			mNotification.ledARGB = Color.RED;
 			mNotification.ledOnMS = 300;
 			mNotification.ledOffMS = 600;
 	//		mNotification.defaults |= Notification.DEFAULT_LIGHTS;
 			mNotification.flags = Notification.FLAG_SHOW_LIGHTS;
 	//		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
 			mNotificationManager.notify(0, mNotification);
 	
 	        /*
 	         * Riga commentata in modo da avere le notifiche cancellabili
 	         */
 	//        mNotification.flags |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
 		}
 		else
 		{
 //			Toast.makeText(context, "Siamo nel caso Android 2.3", 1000).show();
 		     //Create a new notification. The construction Notification(int icon, CharSequence tickerText, long when) is deprecated.
 	        //If you target API level 11 or above, use Notification.Builder instead
 	        //The second parameter, if it it set to null, the notification will not show the marquee
 	        Notification noti = new Notification(R.drawable.workitout_icon, null, System.currentTimeMillis());
 	    
 	        //Set the activity to be launch when selected
 //	        Intent notificationIntent = new Intent(getApplicationContext(), WorkItOutMain.class);
 	        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, WorkItOutMain.class), 0); // open MainActivity if the user selects this notification
 	    
 	        //Set the notification details
 	        //Second parameter: the title of the content
 	        //Third parameter: the content message of the content
 	        noti.setLatestEventInfo(getApplicationContext(), 
 	        						context.getString(R.string.notificationTitle),
 	        						context.getString(R.string.notificationMessage), pi);
 	    
 	        //Set the notification to remove itself after selected
 	        noti.flags = Notification.FLAG_AUTO_CANCEL;
 	    
 	        //Show the notification
 	        mNotificationManager.notify(1, noti);
 		}
 		return START_NOT_STICKY;
 		}
 	
 	public static void CancelAllNotifications(Context ctx) {
 	    String ns = Context.NOTIFICATION_SERVICE;
 	    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
 	    nMgr.cancelAll();	    
 	}
 
}
