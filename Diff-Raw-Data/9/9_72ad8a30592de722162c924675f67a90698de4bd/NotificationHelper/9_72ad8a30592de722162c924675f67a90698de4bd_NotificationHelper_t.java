 package com.aboveware.abovetracker;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 public class NotificationHelper {
 	private Context mContext;
 	public static int NOTIFICATION_HELPER_ID = R.string.exit;
 	private Notification mNotification;
 	private NotificationManager mNotificationManager;
 	private PendingIntent mContentIntent;
 	private CharSequence mContentTitle;
 
 	public NotificationHelper(Context context) {
 		mContext = context;
 	}
 
 	/**
 	 * Put the notification into the status bar
 	 * @param ongoingNotification 
 	 */
 	public void createNotification(int ticker, int title, int message, boolean ongoingNotification) {
 		// get the notification manager
 		mNotificationManager = (NotificationManager) mContext
 		    .getSystemService(Context.NOTIFICATION_SERVICE);
 
 		// create the notification
 		// int icon = android.R.drawable.stat_sys_download;
		int icon = R.drawable.ic_launcher;
 		CharSequence tickerText = mContext.getString(ticker); // Initial text that
 		                                                      // appears in the
 		                                                      // status bar
 		long when = System.currentTimeMillis();
 		mNotification = new Notification(icon, tickerText, when);
 
 		// create the content which is shown in the notification pull down
 		mContentTitle = mContext.getString(title); // Full title of the notification
 		                                           // in the pull down
 		CharSequence contentText = mContext.getString(ticker); // Text of the notification in the
 		                                          // pull down
 
 		// you have to set a PendingIntent on a notification to tell the system what
 		// you want it to do when the notification is selected
 		// I don't want to use this here so I'm just creating a blank one
 		mContentIntent = PendingIntent
     .getActivity(mContext, 0, new Intent(mContext, SmsErrorActivity.class),
         PendingIntent.FLAG_CANCEL_CURRENT);
 		
 		// add the additional content and intent to the notification
 		mNotification.setLatestEventInfo(mContext, mContentTitle, contentText,
 		    mContentIntent);
 
 		// make this notification appear in the 'Ongoing events' section
 		if (ongoingNotification){
 			mNotification.flags = Notification.FLAG_ONGOING_EVENT;
 		}
 
 		// show the notification
 		mNotificationManager.notify(NOTIFICATION_HELPER_ID, mNotification);
 	}
 
 	/**
 	 * Receives progress updates from the background task and updates the status
 	 * bar notification appropriately
 	 * 
 	 * @param percentageComplete
 	 */
 	public void progressUpdate(int percentageComplete) {
 		// build up the new status message
 		CharSequence contentText = percentageComplete + "% complete";
 		// publish it to the status bar
 		mNotification.setLatestEventInfo(mContext, mContentTitle, contentText,
 		    mContentIntent);
 		mNotificationManager.notify(NOTIFICATION_HELPER_ID, mNotification);
 	}
 
 	/**
 	 * called when the background task is complete, this removes the notification
 	 * from the status bar. We could also use this to add a new ‘task complete’
 	 * notification
 	 */
 	public void completed() {
 		// remove the notification from the status bar
 		mNotificationManager.cancel(NOTIFICATION_HELPER_ID);
 	}
 }
