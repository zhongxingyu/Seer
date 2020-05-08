 package com.webshrub.moonwalker.androidapp;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.CallLog;
 import android.provider.Contacts;
 import android.provider.ContactsContract;
 import android.util.Log;
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class CallLogPlugin extends Plugin {
     /**
      * List Action
      */
     private static final String ACTION = "list";
     private static final String DELETE = "delete";
     private static final String CONTACT_ACTION = "contact";
     private static final String SHOW_ACTION = "show";
     private static final String TAG = "CallLogPlugin";
     private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
     private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yy;kk:mm");
     public final String ON = "on";
     public final String OFF = "off";
 
     @Override
     public PluginResult execute(String action, JSONArray data, String callbackId) {
         Log.d(TAG, "Plugin Called");
         PluginResult result = null;
 
         if (ACTION.equals(action)) {
             try {
                 int limit = -1;
                 String contactLogFlag = "";
                 //obtain date to limit by
                 if (!data.isNull(0)) {
                     String d = data.getString(0);
                     contactLogFlag = data.getString(1);
 
                     Log.d(TAG, "Time period is: " + d);
                     if (d.equals("3day"))
                         limit = -3;
                     else if (d.equals("week"))
                         limit = -7;
                     else if (d.equals("month"))
                         limit = -30;
                     else if (d.equals("all"))
                         limit = -1000000; // LOL
                 }
 
                 //turn this into a date
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime(new Date());
                 calendar.add(Calendar.DAY_OF_YEAR, limit);
                 Date limitDate = calendar.getTime();
                 String limiter = String.valueOf(limitDate.getTime());
 
                 //now do required search
                 JSONObject callInfo = getCallListing(limiter, contactLogFlag);
                 Log.d(TAG, "Returning " + callInfo.toString());
                 result = new PluginResult(Status.OK, callInfo);
             } catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(Status.JSON_EXCEPTION);
             }
         } else if (DELETE.equals(action)) {
             try {
                 if (!data.isNull(0)) {
                     deleteCallLogByNumber(data.getString(0));
                     result = new PluginResult(Status.OK);
 
                 }
             } catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(Status.JSON_EXCEPTION);
             } catch (Exception e) {
             }
         } else if (SHOW_ACTION.equals(action)) {
             try {
                 if (!data.isNull(0)) {
                     viewContact(data.getString(0));
                 }
             } catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(Status.JSON_EXCEPTION);
             } catch (Exception e) {
             }
         } else if (CONTACT_ACTION.equals(action)) {
             try {
                 String contactInfo = getContactNameFromNumber(data.getString(0));
                 Log.d(TAG, "Returning " + contactInfo.toString());
                 result = new PluginResult(Status.OK, contactInfo);
             } catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(Status.JSON_EXCEPTION);
             }
         } else {
             result = new PluginResult(Status.INVALID_ACTION);
             Log.d(TAG, "Invalid action : " + action + " passed");
         }
         return result;
     }
 
     /**
      * Gets the Directory listing for file, in JSON format
      *
      * @return JSONObject representation of directory list. e.g
      *         {"filename":"/sdcard"
      *         ,"isdir":true,"children":[{"filename":"a.txt"
      *         ,"isdir":false},{...}]}
      * @throws org.json.JSONException
      */
     private JSONObject getCallListing(String period, String contactLogFlag) throws JSONException {
 
         JSONObject callLog = new JSONObject();
 
         String[] strFields = {
                 CallLog.Calls.DATE,
                 CallLog.Calls.NUMBER,
                 CallLog.Calls.TYPE,
                 CallLog.Calls.DURATION,
                 CallLog.Calls.NEW,
                 CallLog.Calls.CACHED_NAME,
                 CallLog.Calls.CACHED_NUMBER_TYPE,
                 CallLog.Calls.CACHED_NUMBER_LABEL};
 
         try {
             Cursor callLogCursor = ctx.getContext().getContentResolver().query(
                     CallLog.Calls.CONTENT_URI,
                     strFields,
                     CallLog.Calls.DATE + ">?" + "and " + CallLog.Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE,
                     new String[]{period},
                     CallLog.Calls.DEFAULT_SORT_ORDER);
 
             int callCount = callLogCursor.getCount();
 
             if (callCount > 0) {
                 JSONObject callLogItem = new JSONObject();
                 JSONArray callLogItems = new JSONArray();
 
                 callLogCursor.moveToFirst();
                 do {
                     if (callLogCursor.getString(5) == null) {
                         callLogItem.put("date", SIMPLE_DATE_FORMAT.format(new Date(callLogCursor.getLong(0))));
                         callLogItem.put("datetime", SIMPLE_DATE_TIME_FORMAT.format(new Date(callLogCursor.getLong(0))));
                         callLogItem.put("number", callLogCursor.getString(1));
                         callLogItem.put("type", callLogCursor.getInt(2));
                         callLogItem.put("duration", callLogCursor.getLong(3));
                         callLogItem.put("new", callLogCursor.getInt(4));
                         callLogItem.put("cachedName", "Unknown");
                         callLogItem.put("text", "<Please write calling company name here before sending>");
                         callLogItem.put("cachedNumberType", callLogCursor.getInt(6));
                         //callLogItem.put("name", getContactNameFromNumber(callLogCursor.getString(1))); //grab name too
                         callLogItems.put(callLogItem);
                         callLogItem = new JSONObject();
                     } else if (ON.equalsIgnoreCase(contactLogFlag)) {
                         callLogItem.put("date", SIMPLE_DATE_FORMAT.format(new Date(callLogCursor.getLong(0))));
                         callLogItem.put("datetime", SIMPLE_DATE_TIME_FORMAT.format(new Date(callLogCursor.getLong(0))));
                         callLogItem.put("number", callLogCursor.getString(1));
                         callLogItem.put("type", callLogCursor.getInt(2));
                         callLogItem.put("duration", callLogCursor.getLong(3));
                         callLogItem.put("new", callLogCursor.getInt(4));
                         callLogItem.put("cachedName", callLogCursor.getString(5));
                         callLogItem.put("text", "<Please write calling company name here before sending>");
                         callLogItem.put("cachedNumberType", callLogCursor.getInt(6));
                         //callLogItem.put("name", getContactNameFromNumber(callLogCursor.getString(1))); //grab name too
                         callLogItems.put(callLogItem);
                         callLogItem = new JSONObject();
                     }
                 } while (callLogCursor.moveToNext());
                 callLog.put("rows", callLogItems);
             }
 
             callLogCursor.close();
         } catch (Exception e) {
             Log.d("CallLog_Plugin",
                     " ERROR : SQL to get cursor: ERROR " + e.getMessage());
         }
 
         return callLog;
     }
 
     /**
      * Show contact data based on id
      *
      * @param number
      */
     private void viewContact(String number) {
         Intent i = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                 Uri.parse(String.format("tel: %s", number)));
         this.ctx.startActivity(i);
     }
 
 
     /**
      * Util method to grab name based on number
      */
     private String getContactNameFromNumber(String number) {
         // define the columns I want the query to return
         String[] projection = new String[]{Contacts.Phones.DISPLAY_NAME, Contacts.Phones.NUMBER};
 
         // encode the phone number and build the filter URI
         Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
 
         // query time
         Cursor c = ctx.getContext().getContentResolver().query(contactUri, projection, null, null, null);
 
         // if the query returns 1 or more results
         // return the first result
         if (c.moveToFirst()) {
             String name = c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
             c.deactivate();
             return name;
         }
 
         // return the original number if no match was found
         return number;
     }
 
     public void deleteCallLogByNumber(String number) {
         try {
             String queryString = CallLog.Calls.NUMBER + " = '" + number + "'";
             ctx.getContext().getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
         } catch (Exception e) {
 
         }
     }
 }
 
