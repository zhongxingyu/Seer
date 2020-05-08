 /*
  * Copyright 2011 Matthew Precious
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.mattprecious.smsfix;
 
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 /**
  * Service to fix all incoming text messages
  * 
  * How it works:
  * 		The service listens to the SMS database for any changes. Once notified,
  * 		the service loops through the database in descending order and checks
  * 		each message ID against the last known ID. If the ID is greater, then
  * 		we can assume it is a new message and alter its time stamp. Once we
  * 		reach a message that is not new, stop the loop.
  * 
  * @author Matthew Precious
  *
  */
 public class FixService extends Service {
 	private SharedPreferences settings;
 	private Editor editor;
 	
	// The content://sms URI does not notify when a thread is deleted, so instead
 	//  we use the content://mms-sms/conversations URI for observing.
 	//  This provider, however, does not play nice when looking for and editing
	//  the existing messages. So, we use the original content://sms URI
 	//  for our editing
 	private Uri observingURI = Uri.parse("content://mms-sms/conversations");
	private Uri editingURI = Uri.parse("content://sms");
 	private Cursor observingCursor;
 	private Cursor editingCursor;
 	private FixServiceObserver observer = new FixServiceObserver();
 	
 	public static long lastSMSId = 0;			// the ID of the last message we've altered
 	public static boolean running = false;		// is the service running?
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		// we're running
 		running = true;
 		
 		settings = PreferenceManager.getDefaultSharedPreferences(this);
 		editor = settings.edit();
 		
 		// update the preference
 		editor.putBoolean("active", true);
 		editor.commit();
 		
 		// set up the query we'll be observing
 		// we only need the ID and the date
 		String[] columns = {"_id", "date"};
 		observingCursor = getContentResolver().query(observingURI, null, null, null, null);
 		editingCursor = getContentResolver().query(editingURI, columns, "type=?", new String[]{"1"}, "_id DESC");
 		
 		// register the observer
 		observingCursor.registerContentObserver(observer);
 		
 		// get the current last message ID
 		lastSMSId = getLastMessageId();
 		
 		Log.i(getClass().getSimpleName(), "SMS messages now being monitored");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		// no longer running
 		running = false;
 		
 		// update the preference
 		editor.putBoolean("active", false);
 		editor.commit();
 		
 		Log.i(getClass().getSimpleName(), "SMS messages are no longer being monitored. Good-bye.");
 	}
 	
 	/**
 	 * Returns the ID of the most recent message
 	 * 
 	 * @return long
 	 */
 	private long getLastMessageId() {
         long ret = -1;
         
         // if there are any messages at our cursor
         if (editingCursor.getCount() > 0) {
         	// get the first one
 	        editingCursor.moveToFirst();
 	        
 	        // grab its ID
 	        ret = editingCursor.getLong(editingCursor.getColumnIndex("_id"));
         }
         
         return ret;
 	}
 
 	/**
 	 * Updates the time stamp on any messages that have come in
 	 */
 	private void fixLastMessage() {
 		// if there are any messages
         if (editingCursor.getCount() > 0) {
         	// move to the first one
 	        editingCursor.moveToFirst();
 	        
 	        // get the message's ID
 	        long id = editingCursor.getLong(editingCursor.getColumnIndex("_id"));
 	        
 	        // keep the current last changed ID
 	        long oldLastChanged = lastSMSId;
 	        
 	        // update our counter
 	        lastSMSId = id;
 	        
 	        // while the new ID is still greater than the last altered message
 	        // loop just in case messages come in quick succession
 	        while (id > oldLastChanged) {
 	        	// alter the time stamp
 	        	alterMessage(id);
 		        
 	        	// base case, handle there being no more messages and break out
 		        if (editingCursor.isLast()) {
 		        	break;
 		        }
 		        
 		        // move to the next message
 	        	editingCursor.moveToNext();
 	        	
 	        	// grab its ID
 	        	id = editingCursor.getLong(editingCursor.getColumnIndex("_id"));
 	        }
         } else {
         	// there aren't any messages, reset the id counter
         	lastSMSId = -1;
         }
 	}
 	
 	/**
 	 * Get the desired offset change based on the user's preferences
 	 * 
 	 * @return long
 	 */
 	private long getOffset() {
 		long offset = 0;
 		
 		// if the user wants us to auto-determine the offset
 		//  use the negative of their GMT offset
 		if (settings.getString("offset_method", "automatic").equals("automatic")) {
 			offset = TimeZone.getDefault().getRawOffset() * -1;
 		// otherwise, use the offset the user has specified
 		} else {
 			offset = Integer.parseInt(settings.getString("offset", "0")) * 3600000;
 		}
 		
 		return offset;
 	}
 	
 	/**
 	 * Alter the time stamp of the message with the given ID
 	 * 
 	 * @param id - the ID of the message to be altered
 	 */
 	private void alterMessage(long id) {
 		Log.i(getClass().getSimpleName(), "Adjusting timestamp for message: " + id);
     	
         Date date;
         
         // if the user wants to use the phone's time, use the current date
         if (settings.getString("offset_method", "automatic").equals("phone")) {
         	date = new Date();
         } else {
         	// grab the date assigned to the message
         	date = new Date(editingCursor.getLong(editingCursor.getColumnIndex("date")));
         	
         	// if the user has asked for the CDMA fix, make sure the message time is greater
         	//  than the phone time, giving a 5 second grace period
         	if (!settings.getBoolean("cdma", false) || (date.getTime() - (new Date()).getTime() > 5000)) {
         		date.setTime(date.getTime() + getOffset());
         	}
         }
         
         // update the message with the new time stamp
         ContentValues values = new ContentValues();
         values.put("date", date.getTime());
         getContentResolver().update(editingURI, values, "_id = " + id, null);
 	}
 	
 	/**
 	 * ContentObserver to handle updates to the SMS database
 	 * 
 	 * @author Matthew Precious
 	 *
 	 */
 	private class FixServiceObserver extends ContentObserver {
 		
 		public FixServiceObserver() {
 			super(null);
 		}
 		
 		@Override
 		public void onChange(boolean selfChange) {
 			super.onChange(selfChange);
 		
 			// if the change wasn't self inflicted 
 			// TODO: make this boolean actually work...
 			if (!selfChange) {
 				Log.i(getClass().getSimpleName(), "SMS database altered, checking...!");
 				// requery the database to get the latest messages
 				editingCursor.requery();
 				
 				// fix them
 				fixLastMessage();
 			}
 		}
 	}
 	
 }
