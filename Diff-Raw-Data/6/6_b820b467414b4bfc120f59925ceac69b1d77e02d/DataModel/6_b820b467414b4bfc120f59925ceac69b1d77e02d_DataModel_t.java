 package com.kmky.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.kmky.util.Constants;
 
 /**
  * Created by FrederikKastrup on 23/09/13.
  */
 public class DataModel{
 
     private static DataModel sInstance;
     private ArrayList<LogEntry> mLogs;
     private Context mContext;
     private KMKYSQLiteHelper mDbHelper;
 
     /**
      * Singleton. Initialise the model. Load the data from the database into the
      * model
      * @param context
      */
     private DataModel(Context context) {
         mDbHelper = new KMKYSQLiteHelper(context);
         // load data into logs
         mLogs = mDbHelper.getLogs();
 
         for (LogEntry logEntry : mLogs)
         {
             Log.i(Constants.TAG, "Datamodel: Constructor: LogEntries in mLogs: Phone number = " + logEntry.getPhonenumber() + " Type = " + logEntry.getType() + " timeStamp = " + logEntry.getTimestamp() + " Incoming " + logEntry.getIncoming() + " Outgoing " + logEntry.getOutgoing());
         }
     }
 
     /**
      * Singleton of data model. Creates new Instance of Data model if not exists.
      * Else returns Instance.
      * @return
      */
     public static DataModel getInstance(Context context){
         if (sInstance == null)
             sInstance = new DataModel(context);
         return sInstance;
     }
 
     /**
      * Get list of unique the phone numbers in mLogs
      * Used for the getLogs methods
      * @return
      */
     public List<String> getUniquePhoneNumbers()
     {
         List<String> phonenumberList = new ArrayList<String>();
 
         for (LogEntry log : mLogs)
         {
             if (!phonenumberList.contains(log.getPhonenumber()))
             {
                 phonenumberList.add(log.getPhonenumber());
             }
         }
         return phonenumberList;
     }
 
     /**
      * mLogs is empty, creates new LogEntry. Else check if new log already exists then updates that log.\
      * Else creates new log and adds it to mLogs.
      * @param phonenumber
      * @param type
      * @param timestamp
      * @param incoming
      * @param outgoing
      */
     public void addLog(String phonenumber, String type, long timestamp, int incoming, int outgoing){
 
         // Removes spacing in phonenumber
         phonenumber = phonenumber.replace(" ", "");
 
         // Check if log exists or else create it.
         LogEntry newLog = new LogEntry(0, phonenumber, type, timestamp, incoming, outgoing);
         LogEntry existingLog = null;
 
         // Checks if logs is empty
         if (mLogs.size() == 0){
 
             Log.d(Constants.TAG, "Datamodel: addLog: List is empty");
             mDbHelper.addLog(phonenumber, type, timestamp, incoming, outgoing);
             reloadLog();
 
         }
         else{
             // Checks if the logs exists.
             boolean update = false;
             // Id for the log.
             long logEntryId = 0;
 
             for (LogEntry logEntry : mLogs){
 
                 if (logEntry.getPhonenumber().equals(phonenumber) && logEntry.getType().equals(type) && logEntry.getTimestamp() == timestamp){
 
                     update = true;
                     logEntryId = logEntry.getId();
                     existingLog = new LogEntry(logEntry.getId(), logEntry.getPhonenumber(), logEntry.getType(), logEntry.getTimestamp(), logEntry.getIncoming(), logEntry.getOutgoing());
                 }
             }
             if (update){
 
                 updateLog(newLog, existingLog, logEntryId);
 
             } else if (!update){
 
 //				Log.d(Constants.TAG, "Datamodel: addLog: adding logEntry");
                 mDbHelper.addLog(phonenumber, type, timestamp, incoming, outgoing);
                 reloadLog();
             }
         }
     }
 
     /**
      * Remove old log from the list and replace it with a new log
      * Then updates in database
      * @param newLog
      * @param existingLog
      * @param id
      */
     public void updateLog(LogEntry newLog, LogEntry existingLog, long id){
 
         String phonenumber = existingLog.getPhonenumber();
         String type = existingLog.getType();
         long date = existingLog.getTimestamp();
         int incoming = existingLog.getIncoming();
         int outgoing = existingLog.getOutgoing();
 
         Log.d(Constants.TAG, "Datamodel: updateLog: newLog: incoming: " + newLog.getIncoming() + " outgoing: " + newLog.getOutgoing());
 
         if (newLog.getIncoming() == 1){
 
             incoming++;
             Log.d(Constants.TAG, "Datamodel: updateLog: updating incoming logEntry");
             mDbHelper.updateLog(existingLog.getId(), 1, 0);
             reloadLog();
         }
         else if (newLog.getOutgoing() == 1){
 
             outgoing++;
             Log.d(Constants.TAG, "Datamodel: updateLog: updating outgoing logEntry");
             mDbHelper.updateLog(existingLog.getId(), 0, 1);
             reloadLog();
         }
     }
 
     public LogEntry getSmsLogsForPeronOnSpecificDate(String phonenumber, long date){
 
         LogEntry emptyLog = new LogEntry(0, phonenumber, "sms", date, 0, 0);
 
         for (LogEntry log : mLogs)
         {
             if (log.getPhonenumber().equals(phonenumber) && log.getTimestamp() == date && log.getType().equals("sms"))
             {
                 return log;
             }
         }
         return emptyLog;
     }
 
     public LogEntry getCallLogsForPeronOnSpecificDate(String phonenumber, long date)
     {
         LogEntry emptyLog = new LogEntry(0, phonenumber, "call", date, 0, 0);
 
         for (LogEntry log : mLogs)
         {
             if (log.getPhonenumber().equals(phonenumber) && log.getTimestamp() == date && log.getType().equals("sms"))
             {
                 return log;
             }
         }
         return emptyLog;
     }
 
     public LogEntry getSmsLogsForPersonToDate(String phonenumber, long date){
         int incoming = 0;
         int outgoing = 0;
         String type = "sms";
 
         for (LogEntry log : mLogs){
 
             if (log.getPhonenumber().equals(phonenumber) && log.getType().equals("sms")){
                 incoming = incoming + log.getIncoming();
 //                Log.i(Constants.TAG, "Datamodel: getSmsLogsForPersonToDate incoming " + incoming);
                 outgoing = outgoing + log.getOutgoing();
 //                Log.i(Constants.TAG, "Datamodel: getSmsLogsForPersonToDate outgoing " + outgoing);
             }
         }
         LogEntry logToDate = new LogEntry(0, phonenumber, type, date, incoming, outgoing);
         return logToDate;
     }
 
     /**
      * Gets all calls logs from a specific person to date
      * @param phonenumber
      * @param date
      * @return
      */
     public LogEntry getCallLogsForPersonToDate(String phonenumber, long date){
         int incoming = 0;
         int outgoing = 0;
         String type = "call";
 
         for (LogEntry log : mLogs){
 
             if (log.getPhonenumber().equals(phonenumber) && log.getType().equals("call")){
 
                 incoming = incoming + log.getIncoming();
 //                Log.i(Constants.TAG, "Datamodel: getCallLogsForPersonToDate incoming " + incoming);
                 outgoing = outgoing + log.getOutgoing();
 //                Log.i(Constants.TAG, "Datamodel: getCallLogsForPersonToDate outgoing " + outgoing);
             }
         }
         LogEntry logToDate = new LogEntry(0, phonenumber, type, date, incoming, outgoing);
         return logToDate;
     }
 
     /**
      * Gets the top ten least contacted numbers
      * @return
      */
     public List<String> getNumbersForLeastContacted(List<String> phonenumberlist, int state){
 
         List<String> phonenumberList = phonenumberlist;
         List<TopTen> SortList = new ArrayList<TopTen>();
         Date now = new Date();
         Long milliSeconds = now.getTime();
 
         // Find phone numbers for least contacted
         for (String phonenumber : phonenumberList){
 
             int outgoingSMS = getSmsLogsForPersonToDate(phonenumber, milliSeconds).getOutgoing();
             int outgoingCall = getCallLogsForPersonToDate(phonenumber, milliSeconds).getOutgoing();
 
             // Calculating the total communication for each phonenumber
             int totalCommunication = outgoingCall + outgoingSMS;
 
             SortList.add(new TopTen(phonenumber, totalCommunication, 0));
             Log.d(Constants.TAG, "Datamodel: getPhonenumbersForLeastContacted: totalCommunication for phone number " + phonenumber + " amount of communication " +Integer.toString(totalCommunication));
         }
 
         // Sort the list
         Collections.sort(SortList);
 
         // Clears phone number list
         phonenumberList.clear();
 
         switch (state)
         {
             case 1:
 
                 for (TopTen topTen : SortList){
                     String phonenumner = null;
                     phonenumberList.add(topTen.getPhonenumber());
 
                     phonenumberList.add(phonenumner);
                 }
             break;
 
             case 2:
 
                 // Add the phone numbers to list
                 if (SortList.size() > 10){
 
                     for (int i = 0; i < 10; i++){
                         String phonenumber = null;
                         TopTen topTen = SortList.get(i);
                         phonenumber = topTen.getPhonenumber();
                         phonenumberList.add(phonenumber);
                     }
                 }
                 else{
 
                     for (TopTen topTen : SortList){
                         String phonenumner = null;
                         phonenumberList.add(topTen.getPhonenumber());
                     }
                 }
 
 
             break;
         }
 
         return phonenumberList;
     }
 
     /**
      * Get the top ten numbers for most contacted
      * @return
      */
     public List<String> getNumbersForMostContacted(List<String> phonenumberlist, int state){
 
         List<String> phonenumberList = phonenumberlist;
         List<TopTen> SortList = new ArrayList<TopTen>();
         Date now = new Date();
         Long milliSeconds = now.getTime();
 
         for (String phonenumber : phonenumberList){
 
             int outgoingSMS = getSmsLogsForPersonToDate(phonenumber, milliSeconds).getOutgoing();
             int outgoingCall = getCallLogsForPersonToDate(phonenumber, milliSeconds).getOutgoing();
 
             // Calculating the total communication for each phonenumber
             int totalCommunication = outgoingCall + outgoingSMS;
 
             SortList.add(new TopTen(phonenumber, totalCommunication, 0));
             Log.d(Constants.TAG, "Datamodel: getNumbersForMostContacted: totalCommunication for phone number " + phonenumber + " amount of communication "  +Integer.toString(totalCommunication));
 //            Log.d(Constants.TAG, "Datamodel: getNumbersForMostContacted: totalCommunication for phone number " + phonenumber + " amount of communication "  +Integer.toString(totalCommunication));
         }
 
         // Sort the list
         Collections.sort(SortList, Collections.reverseOrder());
 
         // Clears phone number list
         phonenumberList.clear();
 
         switch (state)
         {
             case 1:
 
                 for (TopTen topTen : SortList){
                     String phonenumner = null;
                     phonenumberList.add(topTen.getPhonenumber());
 
                     phonenumberList.add(phonenumner);
                 }
                 break;
 
             case 2:
 
                 // Add the phone numbers to list
                 if (SortList.size() > 10){
 
                     for (int i = 0; i < 10; i++){
                         String phonenumber = null;
                         TopTen topTen = SortList.get(i);
                         phonenumber = topTen.getPhonenumber();
                         phonenumberList.add(phonenumber);
                     }
                 }
                 else{
 
                     for (TopTen topTen : SortList){
                         String phonenumner = null;
                         phonenumberList.add(topTen.getPhonenumber());
                     }
                 }
 
 
                 break;
         }
 
         return phonenumberList;
     }
 
     /**
      * Gets the top ten numbers for people who have contacted you the least
      * @return
      */
     public List<String> getNumbersForLeastContactedYou(List<String> phonenumberlist, int state){
 
         List<String> phonenumberList = phonenumberlist;
         List<TopTen> SortList = new ArrayList<TopTen>();
         Date now = new Date();
         Long milliSeconds = now.getTime();
 
         // Find phone numbers for least contacted
         for (String phonenumber : phonenumberList){
 
             int incomingSMS = getSmsLogsForPersonToDate(phonenumber, milliSeconds).getIncoming();
             int incomingCall = getCallLogsForPersonToDate(phonenumber, milliSeconds).getIncoming();
 
             // Calculating the total communication for each phonenumber
             int totalCommunication = incomingCall + incomingSMS;
 
             SortList.add(new TopTen(phonenumber, totalCommunication, 0));
             Log.d(Constants.TAG, "Datamodel: getNumbersForLeastContactedYou: totalCommunication for phone number " + phonenumber + " amount of communication " +Integer.toString(totalCommunication));
         }
 
         // Sort the list
         Collections.sort(SortList);
 
         // Clears phone number list
         phonenumberList.clear();
 
         switch (state)
         {
             case 1:
 
                 for (TopTen topTen : SortList){
                     String phonenumner = null;
                     phonenumberList.add(topTen.getPhonenumber());
 
                     phonenumberList.add(phonenumner);
                 }
                 break;
 
             case 2:
 
                 // Add the phone numbers to list
                 if (SortList.size() > 10){
 
                     for (int i = 0; i < 10; i++){
                         String phonenumber = null;
                         TopTen topTen = SortList.get(i);
                         phonenumber = topTen.getPhonenumber();
                         phonenumberList.add(phonenumber);
                     }
                 }
                 else{
 
                     for (TopTen topTen : SortList){
                         String phonenumner = null;
                         phonenumberList.add(topTen.getPhonenumber());
                     }
                 }
 
 
                 break;
         }
 
         return phonenumberList;
     }
 
     /**
      *  Gets the top ten numbers for people who have contacted you the most
      * @return
      */
     public List<String> getNumbersForMostContactedYou(List<String> phonenumberlist, int state){
 
         List<String> phonenumberList = phonenumberlist;
         List<TopTen> SortList = new ArrayList<TopTen>();
         Date now = new Date();
         Long milliSeconds = now.getTime();
 
         // Find phone numbers for least contacted
         for (String phonenumber : phonenumberList){
 
             int incomingSMS = getSmsLogsForPersonToDate(phonenumber, milliSeconds).getIncoming();
             int incomingCall = getCallLogsForPersonToDate(phonenumber, milliSeconds).getIncoming();
 
             // Calculating the total communication for each phonenumber
             int totalCommunication = incomingCall + incomingSMS;
 
             SortList.add(new TopTen(phonenumber, totalCommunication, 0));
             Log.d(Constants.TAG, "Datamodel: getNumbersForMostContactedYou: totalCommunication for phone number " + phonenumber + " amount of communication " +Integer.toString(totalCommunication));
         }
 
         // Sort the list
         Collections.sort(SortList, Collections.reverseOrder());
 
         // Clears phone number list
         phonenumberList.clear();
 
         switch (state)
         {
             case 1:
 
                 for (TopTen topTen : SortList){
                    String phonenumner = topTen.getPhonenumber();
                     phonenumberList.add(phonenumner);

                 }
                 break;
 
             case 2:
 
                 // Add the phone numbers to list
                 if (SortList.size() > 10){
 
                     for (int i = 0; i < 10; i++){
                         String phonenumber = null;
                         TopTen topTen = SortList.get(i);
                         phonenumber = topTen.getPhonenumber();
                         phonenumberList.add(phonenumber);
                     }
                 }
                 else{
 
                     for (TopTen topTen : SortList){
                         String phonenumner = null;
                         phonenumberList.add(topTen.getPhonenumber());
                     }
                 }
 
 
                 break;
         }
 
         return phonenumberList;
     }
 
     /**
      * Reloads the logs into mList
      */
     private void reloadLog(){
 
         mLogs.clear();
         mLogs = mDbHelper.getLogs();
     }
 }
