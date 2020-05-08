 package com.tortel.notifier;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.database.ContentObserver;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.telephony.SmsMessage;
 
 /**
  * What's left:
  * Fix MMS !!!!!!!!!!!!!!
  * Catch failed messages - Probably not possible without actually checking for failed messages
  * Fix assorted (now unknown) bugs
  * Actually log shit.
  * 
  * 
  * Make sure to check for threadId <= 0
  * 
  * Done
  * Multiple messages from the same sender - 2/9
  * Some SharedPrefs - 2/12
  * Start at boot - 2/12
  * Revise contact lookup, make it return both the name and contact ID within a wrapper - 2/23
  * SharedPrefs 4/4
  * Database with custom contacts 4/4
  * UI for customization 4/5
  * Main UI Should be Customization, with options menu for add new person and preferences and restart service 4/5
  */
 
 
 public class SMSListenerService extends Service {
 
 	//The Action fired by the Android-System when a SMS was received.
 	public static final String RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
 	public static final String MMS_RECEIVE = "android.provider.Telephony.WAP_PUSH_RECEIVED";
 	private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";
 	private static final String SENT_ACTION="com.android.mms.transaction.MESSAGE_SENT";
 	
 	/**
 	 * Last message received from
 	 */
 	private String lastSender;
 	
 	/**
 	 * SMS Receiver
 	 * TODO: Fix MMS receiver
 	 */
     private BroadcastReceiver receiver;
     private BroadcastReceiver failedReceiver;
     
     /**
      * Observer
      */
     private SmsContentObserver smsContentObserver;
     
     /**
      * Service required methods
      */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if(prefs.getBoolean("enabled", true)){
 			lastSender = "";
 			//Initialize
 			receiver = new Receiver();
 			failedReceiver = new FailedReceiver();
 			smsContentObserver = new SmsContentObserver(new Handler());
 			//Message Receiver
 	        registerReceiver(receiver, new IntentFilter(RECEIVED_ACTION));
 	        //MMS Receiver
 	        IntentFilter tmp = new IntentFilter(MMS_RECEIVE);
 	        try {
 				tmp.addDataType(MMS_DATA_TYPE);
 			} catch (MalformedMimeTypeException e) {
 				e.printStackTrace();
 			}
 	        registerReceiver(receiver, tmp);
 	        
 	        //Failed message receiver
 	        registerReceiver(failedReceiver, new IntentFilter(SENT_ACTION));
 	        //Register observer
 	        registerObserver();
 		} else {
 			this.stopSelf();
 		}
 	}
 
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if(!prefs.getBoolean("enabled", true)){
 			unregisterReceiver(receiver);
 			unregisterReceiver(failedReceiver);
 			getContentResolver().unregisterContentObserver(smsContentObserver);
 		}
 	}
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	
 	private void registerObserver(){
         //Observer
         getContentResolver().
         	registerContentObserver(Uri.parse("content://mms-sms/conversations/"),
         			true, smsContentObserver);
 	}
 	
 	/**
 	 * End Service required methods
 	 */
     
     public void failedMessage(){
 		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification note = new Notification(R.drawable.failed,"Message Failed.",System.currentTimeMillis());
 		PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, Utils.getSmsInboxIntent(), 0);
 		note.setLatestEventInfo(this, "Message Failed.","Message failed to send.", i);
 		
 		setDefaults(note);
 		
 		mgr.notify(1210, note);
     }
     
     /**
      * Method to handle incoming MMS
      * @param in
      */
     public void handleMmsReceive(Intent in){
     	//TODO: Finish this
 		//MMS received
 		Log.v("MMS received");
 		MmsMessage msg = null;
 		
 		//Now, we try and get the MMS details
 		for(int i=0; i< 8;i++){
 			msg = Utils.getMmsDetails(getBaseContext(), 0);
 			if(msg != null){
 				Log.v("Got details, exiting loop");
 				break;
 			}
 			//Wait it out, try again
 			Log.v("Looping.");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// Whoops! Ohwell, let it loop
 			}
 		}
 		
 		
 		
 		//MmsMessage message = Utils.getMmsDetails(this);
 		int count = Utils.getUnreadCount(this);
 		if(count ==0)
 			count =1;
 		Log.v(count+" unread messages");
 		
 		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification note=null;
 		if(msg != null){
 			if(count <= 1){
 				//Just this new message
 				note = new Notification(msg.sender.icon, msg.sender.name+": "+msg.subject,System.currentTimeMillis());
 				Intent tmp=null;
 				if(msg.sender.threadId > 0){
 					tmp = new Intent(Intent.ACTION_VIEW); 
 					tmp.setData(Uri.parse("content://mms-sms/conversations/"+msg.sender.threadId));
 				} else {
 					tmp = Utils.getSmsInboxIntent();
 				}
 				PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, tmp, 0);
 				note.setLatestEventInfo(this, msg.sender.name, msg.subject, i);
 				
 			}else{
 				//Multiple unread messages
 				note = new Notification(msg.sender.icon, msg.sender.name+": "+msg.subject,System.currentTimeMillis());
 				Intent tmp=null;
 				if(msg.sender.name == lastSender && msg.sender.threadId > 0){
 					//Use the name in the notification, and the thread intent
 					tmp = new Intent(Intent.ACTION_VIEW); 
 					tmp.setData(Uri.parse("content://mms-sms/conversations/"+msg.sender.threadId));
 					
 					PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, tmp, 0);
 					note.setLatestEventInfo(this, msg.sender.name, count +" new messages.", i);
 					
 				} else {
 					tmp = Utils.getSmsInboxIntent();
 					PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, tmp, 0);
 					note.setLatestEventInfo(this, msg.sender.name, count +" new messages.", i);
 				}
 				
 			}
 		} else {
 			//Couldnt get the details. :(
 			//No need to have checks for number of messages, its the same either way
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			note = new Notification(Values.icons[ Integer.parseInt( prefs.getString("defColor", "0"))],"New Message",System.currentTimeMillis());
 		
 			PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, Utils.getSmsInboxIntent(), 0);
 			note.setLatestEventInfo(this, "New Messages", count+" new messages", i);
 		}
 		
 		//Finalize it, and send it
 		setDefaults(note);
 		
 		mgr.notify(1210, note);
     }
     
     /**
      * Method to handle an incoming SMS message
      * @param in incoming intent
      */
     public void handleSmsReceive(Intent in){
 		Log.v("On SMS RECEIVE");
 
 		Bundle bundle = in.getExtras();
 		if(bundle!=null)
 		{
 			String body="";
 			String sender="";
 			long time = System.currentTimeMillis();
 			Object[] pdus = (Object[])bundle.get("pdus");
 			SmsMessage[] messages = new SmsMessage[pdus.length];
 			for(int i = 0; i<pdus.length; i++)
 			{
 	    		Log.v("FOUND MESSAGE");
 				messages[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
 			}
 			for(SmsMessage message: messages){
 				sender = message.getOriginatingAddress();
 				body = message.getMessageBody();
 				time = message.getTimestampMillis();
 			}
 			Log.v("Message from "+sender);
 			smsNotification(body,sender, time);
 		}
     }
     
     /**
      * Sets the default values for LED, ring, and vibrate
      * @param note the notificatoin
      */
     private void setDefaults(Notification note){
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		note.ledARGB = Values.ledColors[Integer.parseInt(prefs.getString("ledColor", "0"))];
 		int tmp = Integer.parseInt(prefs.getString("ledPat","0"));
 		note.ledOnMS = Values.ledPatterns[tmp];
 		note.ledOffMS = Values.ledPatterns[tmp];
 		if(prefs.getBoolean("vibrate", true))
 			note.defaults |= Notification.DEFAULT_VIBRATE;
 		note.sound = Uri.parse(prefs.getString("ringtone", "DEFAULT_NOTIFICATION_URI"));
 		note.flags |= Notification.FLAG_AUTO_CANCEL;
 		note.flags |= Notification.FLAG_SHOW_LIGHTS; 
     }
     
 	/**
 	 * This method creates the notification
 	 * @param body the message body
 	 * @param sender the sender's raw address
 	 */
 	private void smsNotification(String body, String sender, long time){
 		//Get extra details
 		Contact contact = Utils.lookupContact(sender, this);
 		int count = Utils.getUnreadCount(this, body);
 
 		//Cut the body over 80char off for the notification
 		if(body.length() > 80)
 			body = body.substring(0, 80);
 		
 		//Make the notification
 		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification note = new Notification(contact.icon,contact.name+": "+body,time);
 		Log.v("Notification from "+contact.name+" with thread id "+contact.threadId);
 
 		if(count <= 1){
 			Log.v("One unread message");
 			Intent tmp;
 			if(contact.threadId > 0){
 				tmp = new Intent(Intent.ACTION_VIEW); 
 				tmp.setData(Uri.parse("content://mms-sms/conversations/"+contact.threadId));
 			} else {
 				tmp = Utils.getSmsInboxIntent();
 			}
 			PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, tmp, 0);
 			note.setLatestEventInfo(this, contact.name, body, i);
 		} else {
 			if(!contact.name.equals(lastSender)){
 				PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, Utils.getSmsInboxIntent(), 0);
 				note.setLatestEventInfo(this, "New Message", count+" new messages", i);
 			} else {
 				//Messages are from the same person, display name and open thread
 				Intent tmp;
 				if(contact.threadId > 0){
 					tmp = new Intent(Intent.ACTION_VIEW); 
 					tmp.setData(Uri.parse("content://mms-sms/conversations/"+contact.threadId));
 				} else {
 					tmp = Utils.getSmsInboxIntent();
 				}
 				PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0, tmp, 0);
 				note.setLatestEventInfo(this,contact.name,count+" new messages",i);
 			}//displayname != lastSender
 			note.number = count;
 		}
 		
 		setDefaults(note);
 		mgr.notify(1210, note);
 		
 		lastSender = contact.name;
 	}
 	
 	/**
 	 * SMS and MMS receiver class
 	 * @author Scott Warner
 	 *
 	 */
 	private class Receiver extends BroadcastReceiver{
     	public void onReceive(Context c, Intent in){
     		Log.v("On Receive");
     		if(in.getAction().equals(RECEIVED_ACTION)){
     			handleSmsReceive(in);
     		} else if(in.getAction().equals(MMS_RECEIVE)){
     			try{
     				//Sleep for 2 sec to let MMS load
     				//Thread.sleep(2000);
     			} catch(Exception e){
     				//Ohh snap.
     			}
     			handleMmsReceive(in);
     		}
     	}
 	}
 	
 	/**
 	 * Failed Message receiver
 	 * @author Scott Warner
 	 *
 	 */
 	private class FailedReceiver extends BroadcastReceiver{
         public void onReceive(Context c, Intent in) {
         	Log.v("Message sent intent received");
         	if(getResultCode() != Activity.RESULT_OK){
         		failedMessage();
         	}
          }
 	}
 	
 	/**
 	 * Observer. Removes the notification when the message is read
 	 * @author Scott Warner
 	 *
 	 */
 	private class SmsContentObserver extends ContentObserver {
 	    public SmsContentObserver(Handler handler) {
 	      super(handler);
 	    }
 	
 	    public void onChange(boolean selfChange) {
 	    	super.onChange(selfChange);
 	    	try {
 				Thread.sleep(2000);
 	    	} catch (InterruptedException e) {
 				//Oops. Ohwell
 			}
 		    int count = Utils.getUnreadCount(SMSListenerService.this);
 		    if (count == 0) {
 		    	NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		    	mgr.cancel(1210);
 		    } else {
 		    	//Something with unread messages can go here
 		    }
 	    }
 	}
 	
 }
