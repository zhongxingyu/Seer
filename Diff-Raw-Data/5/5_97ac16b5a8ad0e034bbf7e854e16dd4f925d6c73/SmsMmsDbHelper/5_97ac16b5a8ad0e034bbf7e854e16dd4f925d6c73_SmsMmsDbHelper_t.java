 /*
  * Copyright 2012 Matthew Precious
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.mattprecious.smsfix.library.util;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class SmsMmsDbHelper {
     private final static String TAG = "SmsMmsDbHelper";
 
     private final static Uri SMS_URI = Uri.parse("content://sms");
     private final static Uri MMS_URI = Uri.parse("content://mms");
     private final static Uri MMS_SMS_URI = Uri.parse("content://mms-sms/conversations");
 
     private final static String TYPE_COLUMN_SMS = "type";
     private final static String TYPE_COLUMN_MMS = "msg_box";
 
     public static Uri getSmsUri() {
         return SMS_URI;
     }
 
     public static Uri getMmsUri() {
         return MMS_URI;
     }
 
     public static Uri getMmsSmsUri() {
         return MMS_SMS_URI;
     }
 
     public static String getTypeColumnName(Uri uri) {
         return (uri == SMS_URI) ? TYPE_COLUMN_SMS : TYPE_COLUMN_MMS;
     }
 
     public static Cursor getInboxCursor(Context context, Uri uri, String[] columns, String order) {
         return context.getContentResolver().query(uri, columns, getTypeColumnName(uri) + "=?",
                 new String[] { "1" }, order);
     }
 
     /**
      * Returns the ID of the most recent message
      * 
      * @return long
      */
     public static long getLastMessageId(Context context, Uri uri) {
         long id = -1;
         Cursor c = getInboxCursor(context, uri, new String[] { "_id", "date" }, "_id DESC");
 
         // if there are any messages at our cursor
         if (c.getCount() > 0) {
             // get the first one
             c.moveToFirst();
 
             // grab its ID
             id = c.getLong(c.getColumnIndexOrThrow("_id"));
         }
 
         c.close();
 
         return id;
     }
 
     public static long fixMessages(Context context, Uri uri, long lastUpdatedId) {
         String[] columns = { "_id", "date" };
         Cursor c = getInboxCursor(context, uri, columns, "_id DESC");
 
         long newUpdatedId;
         // if there are any messages
         if (c.getCount() > 0) {
             // move to the first one
             c.moveToFirst();
 
             // get the message's ID
             long id = c.getLong(c.getColumnIndexOrThrow("_id"));
 
             Log.d(TAG, "Latest ID: " + id + "; Last ID: " + lastUpdatedId);
 
             // update our counter
             newUpdatedId = id;
 
             // while the new ID is still greater than the last altered message
             // loop just in case messages come in quick succession
             while (id > lastUpdatedId) {
                 long date = c.getLong(c.getColumnIndexOrThrow("date"));
 
                 // alter the time stamp
                 alterMessage(context, uri, id, date);
 
                 // base case, handle there being no more messages and break out
                 if (c.isLast()) {
                     Log.d(TAG, "This is the last message, aborting");
                     break;
                 }
 
                 // move to the next message
                 c.moveToNext();
 
                 // grab its ID
                 id = c.getLong(c.getColumnIndexOrThrow("_id"));
             }
         } else {
             // there aren't any messages, reset the id counter
             newUpdatedId = -1;
         }
 
         c.close();
 
         return newUpdatedId;
     }
 
     /**
      * Alter the time stamp of the message with the given ID
      * 
      * @param id
      *            - the ID of the message to be altered
      */
     public static void alterMessage(Context context, Uri uri, long id, long date) {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
 
         Log.d(TAG, "Adjusting timestamp for message: " + id);
         Log.d(TAG, "Timestamp from the message is: " + date);
 
         // if the user has asked for the Future Only option, make sure the
         // message
         // time is greater than the phone time, giving a 5 second grace
         // period
 
         // keeping the preference name as cdma so when users upgrade it uses
         // their current value
         boolean futureOnly = settings.getBoolean("cdma", false);
         boolean inTheFuture = (date - (new Date()).getTime()) > 5000;
 
         Log.d(TAG, "Future only: " + Boolean.toString(futureOnly));
         Log.d(TAG, "In the future? " + Boolean.toString(inTheFuture));
 
         if (!futureOnly || inTheFuture) {
             // if the user wants to use the phone's time, use the current date
             if (settings.getString("offset_method", "manual").equals("phone")) {
                 date = (new Date()).getTime();
                
                // MMS dates are stores as seconds... not ms...
                if (uri == SmsMmsDbHelper.getMmsUri()) {
                    date /= 1000;
                }
             } else {
                 date = date + TimeHelper.getOffset(context);
             }
         }
 
         Log.d(TAG, "Setting timestamp to " + date);
 
         // update the message with the new time stamp
         ContentValues values = new ContentValues();
         values.put("date", date);
         int result = context.getContentResolver().update(uri, values, "_id = " + id, null);
 
         Log.d(TAG, "Rows updated: " + result);
     }
 }
