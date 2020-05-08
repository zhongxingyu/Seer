 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
<<<<<<< HEAD
 import android.os.*;
 import android.support.v4.content.LocalBroadcastManager;
import java.io.*;
import java.net.URI;
=======
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
>>>>>>> Moved all course related data classes to domain, moved course related utility classes to utility, made downloadUtility class which deals with all static methods related to downloading from the REST service
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.utility.DownloadUtilities;
 
 /**
  *
  * @author kristoffer
  */
 public class MuldvarpService extends Service {
     public static final String ACTION_PEOPLE_UPDATE = "no.hials.muldvarp.ACTION_PEOPLE_UPDATE";
     
     int mStartMode;       // indicates how to behave if the service is killed
     // Binder given to clients
     private final IBinder mBinder = new LocalBinder();
     boolean mAllowRebind; // indicates whether onRebind should be used
         
     String server = "http://master.uials.no:8080/muldvarp/";
     String course = server + "resources/course";
     String courseCache = "CourseCache";
         
     SharedPreferences preferences;
     
     LocalBroadcastManager mLocalBroadcastManager;
     static final int MSG_UPDATE = 1;
     Handler mHandler = new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_UPDATE: {
                     Intent intent = new Intent(ACTION_PEOPLE_UPDATE);
                     intent.putExtra("value", "people");
                     mLocalBroadcastManager.sendBroadcast(intent);
                     Message nmsg = mHandler.obtainMessage(MSG_UPDATE);
                     mHandler.sendMessageDelayed(nmsg, 1000);
                 }
                 break;
                 default:
                     super.handleMessage(msg);
             }
         }
     };
     
     @Override
     public void onCreate() {
         // The service is being created
         super.onCreate();
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
                        
         new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... arg0) {
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(MuldvarpService.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 return null;
             }
             
             @Override
             protected void onPostExecute(Void retVal) {
                 System.out.println("Sending message");
                 // Tell any local interested parties about the start.
                 mLocalBroadcastManager.sendBroadcast(new Intent(ACTION_PEOPLE_UPDATE));
 
                 // Prepare to do update reports.
                 /*mHandler.removeMessages(MSG_UPDATE);
                 Message msg = mHandler.obtainMessage(MSG_UPDATE);
                 mHandler.sendMessageDelayed(msg, 1000);*/
             }
             
         }.execute();
         
     }
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         // The service is starting, due to a call to startService()
         
         Bundle extra = intent.getExtras();
         if(extra != null){
             switch (extra.getInt("whatToDo")) {
                 case 1: // get all courses
                     new DownloadCourses().execute(course);
                     break;
                 case 2: // get single course
                     break;
             }
         }
 
         return mStartMode;
     }
     
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder {
         public MuldvarpService getService() {
             return MuldvarpService.this;
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
     
     @Override
     public boolean onUnbind(Intent intent) {
         // All clients have unbound with unbindService()
         return mAllowRebind;
     }
     @Override
     public void onRebind(Intent intent) {
         // A client is binding to the service with bindService(),
         // after onUnbind() has already been called
     }
     @Override
     public void onDestroy() {
         // The service is no longer used and is being destroyed
          
         // Stop doing updates.
         mHandler.removeMessages(MSG_UPDATE);
     }
     
     private class DownloadCourses extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... urls) {
             try{
                 cacheThis(
                         new InputStreamReader(DownloadUtilities.getJSONData(urls[0])), 
                         new File(getCacheDir(), courseCache));
                 return true;
             }catch(Exception ex){
                 Logger.getLogger(MuldvarpService.class.getName()).log(Level.SEVERE, null, ex);
             }
             return false;
         } 
         
         @Override
         protected void onPostExecute(Boolean retVal) {
             // callback here
         }
     }
     
     private void cacheThis(Reader json, File f) {
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(f));
             char[] buffer = new char[1024];
             int length;
             while((length = json.read(buffer)) != -1) {
                 writer.write(buffer, 0, length);
             }
             writer.flush();
             writer.close();
             json.close();
         } catch (IOException ex) {
             Logger.getLogger(MuldvarpService.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
