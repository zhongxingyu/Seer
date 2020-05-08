 package com.kangaroo.gui;
 
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import com.android.kangaroo.R;
 import com.kangaroo.gui.UserNotification;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.Context;
 
 public class UserNotification
 {
 	 	private NotificationManager mNM;
 	 	private Context ctx;
 	 	private Set<Integer> currentMessages;
 	 	private Random generator;
 	 	
 	    public  UserNotification(Context myC) 
 	    {     
 	    	ctx = myC;
 	    	mNM = (NotificationManager)ctx.getSystemService("notification");
 	    	currentMessages = new TreeSet<Integer>();
 	    	generator = new Random();
 	    }
 	 	
 	    
 	    public void killNotification(Integer id)
 	    {
 	    	if(currentMessages.contains(id)) 
 	    	{
 	    		currentMessages.remove(id);
 	    		mNM.cancel(id);
 	    	}
 	    	
 	    }
 	    
	    public void updateNotification(Integer id, String title, String textMessage, boolean vibrate_sound, Class<Activity> okKlickActivity)
 	    {
 	    	Notification notification = new Notification(R.drawable.stat_happy, textMessage, System.currentTimeMillis());
 
 	        // The PendingIntent to launch our activity if the user selects this notification
 	        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, okKlickActivity), 0);
 
 	        // Set the info for the views that show in the notification panel.
 	       notification.setLatestEventInfo(ctx, title, textMessage, contentIntent);
 	       notification.tickerText = title;
 	       if(vibrate_sound)
 	       {
 	    	   notification.defaults = notification.DEFAULT_SOUND | notification.DEFAULT_VIBRATE;
 	       }
 
 	        // Send the notification.
 	        mNM.notify(id, notification);
 	    }
 	    
 	    /**
 	     * Show a notification while this service is running.
 	     */
	    public int showNotification(String title, String textMessage, boolean vibrate_sound, Class<Activity> okKlickActivity) 
 	    {
 	        Notification notification = new Notification(R.drawable.stat_happy, textMessage, System.currentTimeMillis());
 
 	        // The PendingIntent to launch our activity if the user selects this notification
 	        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, okKlickActivity), 0);
 
 	        // Set the info for the views that show in the notification panel.
 	       notification.setLatestEventInfo(ctx, title, textMessage, contentIntent);
 	       notification.tickerText = title;
 	       if(vibrate_sound)
 	       {
 	    	   notification.defaults = notification.DEFAULT_SOUND | notification.DEFAULT_VIBRATE;
 	       }
 	       
 	       Integer currentId = generator.nextInt();
 	        // Send the notification.
 	        mNM.notify(currentId, notification);
 	        currentMessages.add(currentId);
 	        return currentId;
 	    }
 }
