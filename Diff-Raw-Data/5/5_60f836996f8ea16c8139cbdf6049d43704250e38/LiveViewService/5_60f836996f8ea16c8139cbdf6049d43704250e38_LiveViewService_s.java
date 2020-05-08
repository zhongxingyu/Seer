 package nl.rnplus.olv.service;
 
 import java.io.IOException;
 
 import nl.rnplus.olv.data.LiveViewDbConstants;
 import nl.rnplus.olv.data.LiveViewDbHelper;
 import nl.rnplus.olv.messages.MessageConstants;
 import nl.rnplus.olv.messages.calls.SetLed;
 import nl.rnplus.olv.messages.calls.SetScreenMode;
 import nl.rnplus.olv.messages.calls.SetVibrate;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.IBinder;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.PhoneLookup;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 /**
  * This service hosts and controls the thread communicating with the LiveView
  * device.
  **/
 public class LiveViewService extends Service
 {
 	private static final String TAG = "LiveViewService";
     public static final	String ACTION_START = "start";
     public static final String ACTION_STOP = "stop";
     final public static String PLUGIN_COMMAND   = "nl.rnplus.olv.plugin.command";
     
     private LiveViewService myself;
 
     private LiveViewThread workerThread = null;
     
     Boolean NotificationNeedsUpdate = true;
     Cursor notification_cursor = null;    
     int contentcolumn = -1;
     int titlecolumn = -1;
     int typecolumn = -1;
     int timecolumn = -1;
     int readcolumn = -1;
     
     SQLiteDatabase globdb = null;
     
     BroadcastReceiver plugin_receiver = new PluginCommandReceiver();
     
     MyPhoneStateListener phoneListener=new MyPhoneStateListener();
     
     /*
      * (non-Javadoc)
      * @see android.app.Service#onBind(android.content.Intent)
      */
     @Override
     public IBinder onBind(Intent intent)
     {
         return null;
     }
     
     @Override
     public void onCreate()
     {    
     	myself = this;    	
     	registerReceiver(plugin_receiver, new IntentFilter(PLUGIN_COMMAND));
 
     	IntentFilter mediaintentfilter = new IntentFilter();
     	mediaintentfilter.addAction("com.android.music.metachanged");
     	mediaintentfilter.addAction("com.android.music.playstatechanged");
     	mediaintentfilter.addAction("com.android.music.playbackcomplete");
     	mediaintentfilter.addAction("com.android.music.queuechanged");
     	registerReceiver(media_receiver, mediaintentfilter);
     	
     	TelephonyManager telephonyManager
     	=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
     	telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
     }
     
     @Override
     public void onDestroy()
     {    
     	unregisterReceiver(media_receiver);
     	unregisterReceiver(plugin_receiver);
     	try {
     		notification_cursor.close();
     	} catch(Exception e) {
     		String message = "Database error in service (onDestroy): " + e.getMessage();
 	        Log.e(TAG, message);
     	}
     	
     }    
 
     /*
      * (non-Javadoc)
      * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
      */
     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         String action = intent.getAction();
         if (ACTION_START.equals(action))
         {
             startThread();
         } else if (ACTION_STOP.equals(action))
         {
             stopThread();
         }
         return START_NOT_STICKY;
     }
 
     /**
      * Starts the worker thread if the thread is not already running. If there
      * is a thread running that has already been stopped then a new thread is
      * started.
      */
     private void startThread()
     {
         if (workerThread == null || !workerThread.isLooping())
         {
             workerThread = new LiveViewThread(this);
             workerThread.start();
         }
     }
 
     /**
      * Signals the current worker thread to stop itself. If no worker thread is
      * available then nothing is done.
      */
     private void stopThread()
     {
         if (workerThread != null && workerThread.isAlive())
         {
             workerThread.stopLoop();
         }
         stopSelf();
     }
         
     public String getNotificationContent(int notification, int type)
     {
     	String content = "ERROR";
     	try {
 	    	int id = 0;
 	    	LiveViewDbHelper dbHelper;
 	    	Cursor cursor;
 			dbHelper = new LiveViewDbHelper(this);	
 			dbHelper.openToRead();
 			cursor = dbHelper.getAlertsOfType(type);
 			int index_CONTENT = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT);
 			int index_ID = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID);
 			cursor.moveToPosition(notification);
 			content = cursor.getString(index_CONTENT);
 			id = cursor.getInt(index_ID);
 			cursor.close();
 			dbHelper.close();
 			dbHelper.openToWrite();
 			dbHelper.setAlertRead(id);
 			dbHelper.close();
     	} catch (Exception e) {
     		Log.e("getNotificationContent", "Error: "+e.getMessage());
     	}
     	return content;
     }
     
     public String getNotificationTitle(int notification, int type)
     {
     	String title = "";
     	try {
 	    	LiveViewDbHelper dbHelper;
 	    	Cursor cursor;
 			dbHelper = new LiveViewDbHelper(this);	
 			dbHelper.openToRead();
 			cursor = dbHelper.getAlertsOfType(type);
 			int index_TITLE = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE);
 			cursor.moveToPosition(notification);
 			title = cursor.getString(index_TITLE);
 			cursor.close();
 			dbHelper.close();
     	} catch (Exception e) {
     		Log.e("getNotificationTitle", "Error: "+e.getMessage());
     	}
     	return title;
     }
     
     public long getNotificationTime(int notification, int type) {
    	int timestamp = 0;
     	try {
 	    	LiveViewDbHelper dbHelper;
 	    	Cursor cursor;
 			dbHelper = new LiveViewDbHelper(this);	
 			dbHelper.openToRead();
 			cursor = dbHelper.getAlertsOfType(type);
 			int index_TIMESTAMP = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP);
 			cursor.moveToPosition(notification);
			timestamp = cursor.getInt(index_TIMESTAMP);
 			cursor.close();
 			dbHelper.close();
     	} catch (Exception e) {
     		Log.e("getNotificationTime", "Error: "+e.getMessage());
     	}
     	return timestamp;
     }
  
     public int getNotificationUnreadCount(int type)
     {
     	LiveViewDbHelper dbHelper;
     	Cursor cursor;
 		dbHelper = new LiveViewDbHelper(this);	
 		dbHelper.openToRead();
 		cursor = dbHelper.getAlertsOfType(type);
 		int index_UNREAD = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD);
 		int unreadCount = 0;
 		for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
 			if (cursor.getInt(index_UNREAD)==1) {
 				unreadCount++;
 			}
 		}
 		cursor.close();
 		dbHelper.close();
         return unreadCount;
     }
     
     public int getNotificationTotalCount(int type)
     {
     	LiveViewDbHelper dbHelper;
     	Cursor cursor;
 		dbHelper = new LiveViewDbHelper(this);	
 		dbHelper.openToRead();
 		cursor = dbHelper.getAlertsOfType(type);
 		int totalCount = cursor.getCount();
 		cursor.close();
 		dbHelper.close();
         return totalCount;
     }
     
     /* Media receiver */
     
     boolean MediaInfoNeedsUpdate = false;
     String MediaInfoArtist = "";
     String MediaInfoTrack  = "";
     String MediaInfoAlbum  = "";
     
     public Boolean getMediaInfoNeedsUpdate()
     {
         return MediaInfoNeedsUpdate;
     }
 
     public void setMediaInfoNeedsUpdate(Boolean NotificationNeedsUpdate)
     {
         this.MediaInfoNeedsUpdate = NotificationNeedsUpdate;
     }
     
     public String getMediaInfoArtist()
     {
         return MediaInfoArtist;
     }
     
     public String getMediaInfoTrack()
     {
         return MediaInfoTrack;
     }
     
     public String getMediaInfoAlbum()
     {
         return MediaInfoAlbum;
     }
     
     private BroadcastReceiver media_receiver = new BroadcastReceiver()
     {
     	@Override
     	public void onReceive(Context context, Intent intent)
     	{
     	//String action = intent.getAction();
     	//String cmd = intent.getStringExtra("command");
     	//Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
     	MediaInfoArtist = intent.getStringExtra("artist");
     	MediaInfoAlbum = intent.getStringExtra("album");
     	MediaInfoTrack = intent.getStringExtra("track");
     	MediaInfoNeedsUpdate = true;
     	Log.d("OLV Music","Artist: "+MediaInfoArtist+", Album: "+MediaInfoAlbum+" and Track: "+MediaInfoTrack);
     	}
     	};
     	
         public class PluginCommandReceiver extends BroadcastReceiver  
         {  
             @Override  
             public void onReceive(Context context, Intent intent)  
             {  
             	Log.w("PLUGIN DEBUG", "Received intent, current LiveView status is: "+workerThread.getLiveViewStatus());
             	Log.w("PLUGIN DEBUG", "Command: "+intent.getExtras().getString("command"));
 	    		try
 	    		{
 	    			switch (workerThread.getLiveViewStatus()){
 	            		case MessageConstants.DEVICESTATUS_OFF:
 	    		    			if (intent.getExtras().getString("command").contains("vibrate"))
 	    		    			{
 	    		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
 	    		    				int vdelay = intent.getExtras().getInt("delay");
 	    		    				int vtime = intent.getExtras().getInt("time");
 	    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
 	    		    			}
 	    		    			else
 	    		    			{
 		    		    			if (intent.getExtras().getString("command").contains("notify"))
 		    		    			{
 		    		    				Log.w("PLUGIN DEBUG", "Sent vibration & blink.");
 		    		    				int vdelay = intent.getExtras().getInt("delay");
 		    		    				int vtime = intent.getExtras().getInt("time");
 		    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
 		    		    				workerThread.sendCall(new SetLed(Color.GREEN, vdelay, vtime));
 		    		    				if (intent.getExtras().getInt("displaynotification")==1)
 		    		    				{
 		    		    					String line1 = intent.getExtras().getString("line1");
 		    		    					String line2 = intent.getExtras().getString("line2");
 		    		    					if (line1==null) line1 = "";
 		    		    					if (line2==null) line2 = "";
 		    		    					int icon_type = intent.getExtras().getInt("icon_type");
 		    		    					if (icon_type>2)
 		    		    					{
 		    		    						icon_type=2;
 		    		    					}
 		    		    					byte[] img = intent.getExtras().getByteArray("icon");
 		    		    					workerThread.showNewAlert(line1, line2, icon_type, img);
 		    		    				}
 		    		    			}
 		    		    			else
 		    		    			{
 		    		    				if (intent.getExtras().getString("command").contains("awaken"))
 		    		    				{
 		    		    					Log.w("PLUGIN DEBUG", "Sent awaken.");
 		    		    					workerThread.sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
 		    		    					workerThread.openMenuFromStandby();
 		    		    				}
 		    		    				else
 		    		    				{
 		    		                        String message = "Error: Plugin command receiver: Unknown command.";
 		    		                        Log.e(TAG, message);
 		    		                        
 		    		    	                return;
 		    		    				}
 		    		    			}
 	    		    			}
 	            			break;
 	            		case MessageConstants.DEVICESTATUS_ON:
     		    			if (intent.getExtras().getString("command").contains("vibrate"))
     		    			{
     		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
     		    				int vdelay = intent.getExtras().getInt("delay");
     		    				int vtime = intent.getExtras().getInt("time");
     		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
     		    			}
 	            			break;
 	            		case MessageConstants.DEVICESTATUS_MENU:
     		    			if (intent.getExtras().getString("command").contains("vibrate"))
     		    			{
     		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
     		    				int vdelay = intent.getExtras().getInt("delay");
     		    				int vtime = intent.getExtras().getInt("time");
     		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
     		    			}
     		    			if (intent.getExtras().getString("command").contains("panel"))
     		    			{
     		    				Log.w("PLUGIN DEBUG", "Show panel.");
     		    				String top_string = intent.getExtras().getString("top_string");
     		    				String bottom_string = intent.getExtras().getString("bottom_string");
     		    				boolean isAlert = intent.getExtras().getBoolean("isAlert");
     		    				boolean useImage = intent.getExtras().getBoolean("useImage");
     		    				byte[] img = intent.getExtras().getByteArray("image");
     		    				workerThread.showPanel(top_string, bottom_string, isAlert, useImage, img);
     		    				Log.e("DEBUG", top_string);
     		    				Log.e("DEBUG", bottom_string);
     		    			}    		    			
 	            			break;
 	            		default:
 	                        String message = "Error: Unknown device state!";
 	                        Log.e(TAG, message);
 	                        
 	            			break;
 	            	}
 	    		}
 	    		catch (IOException e)
 	            {
                     String message = "Error: IOException in plugin command receiver: " + e.getMessage();
                     Log.e(TAG, message);
                     
                     e.printStackTrace();
 	                return;
 	            }
             }  
         }  
         
         public class MyPhoneStateListener extends PhoneStateListener {
         	Context context;
         	@Override
         	public void onCallStateChanged(int state,String incomingNumber){
         		try {
         		if (incomingNumber.length()>0) Log.d("PhoneCallStateNotified", "Incoming number "+incomingNumber);
 		        	if (state == TelephonyManager.CALL_STATE_RINGING)
 		        	{
 		        		if (workerThread.getLiveViewStatus()==MessageConstants.DEVICESTATUS_OFF)
 		        		{
 								workerThread.showIncomingCallScreen(state, "Incoming call", getContactByAddr(myself, incomingNumber));
 		        		}
 		        		Log.d("PhoneCallStateNotified", "Status: RINGING");
 		        	}
 		        	if (state == TelephonyManager.CALL_STATE_IDLE)
 		        	{
 		        		Log.d("PhoneCallStateNotified", "Status: IDLE");
 		        	}
 		        	if (state == TelephonyManager.CALL_STATE_OFFHOOK)
 		        	{
 		        		Log.d("PhoneCallStateNotified", "Status: OFFHOOK");
 		        	} 
 				} catch (Exception e) {
 	                String message = "Exception in incoming call receiver: " + e.getMessage();
 	                Log.e(TAG, message);
 					//e.printStackTrace();
 				}	
         	}
         }        
         
         private static String getContactByAddr(Context context, String phoneNumber) {
     		Uri personUri = null;
     		Cursor cur = null;
 
     		try {
     			personUri = Uri.withAppendedPath(
     					ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
     					phoneNumber);
     			cur = context.getContentResolver()
     					.query(personUri,
     							new String[] { PhoneLookup.DISPLAY_NAME }, null,
     							null, null);
     			if (cur.moveToFirst()) {
     				int nameIdx = cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
     				return cur.getString(nameIdx);
     			}
     			return phoneNumber;
     		} finally {
     			if (cur != null) {
     				cur.close();
     			}
     		}
     	}
 }
