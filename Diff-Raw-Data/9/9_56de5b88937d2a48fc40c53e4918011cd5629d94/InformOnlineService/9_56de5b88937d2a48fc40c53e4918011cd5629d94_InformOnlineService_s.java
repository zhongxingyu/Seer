 /*
  * Copyright (C) 2009 University of Washington
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
 
 package com.radicaldynamic.groupinform.services;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.ConditionVariable;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.activities.AccountDeviceList;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.logic.AccountDevice;
 import com.radicaldynamic.groupinform.logic.InformOnlineSession;
 import com.radicaldynamic.groupinform.logic.InformOnlineState;
 import com.radicaldynamic.groupinform.utilities.FileUtils;
 import com.radicaldynamic.groupinform.utilities.HttpUtils;
 
 /**
  * 
  */
 public class InformOnlineService extends Service {    
     private static final String t = "InformOnlineService: ";
     
     // Whether or not we are currently attempting to connect to the service
     private boolean mConnecting = false;
     
     // Assume that we are not initialized when this service starts
     private boolean mInitialized = false;
     
     // Assume that the "online service" is not answering when we start
     private boolean mServicePingSuccessful = false;
     
     // Assume that we are not signed in when this service starts
     private boolean mSignedIn = false;
     
     // This is the object that receives interactions from clients.  
     // See RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
   
     private ConditionVariable mCondition;
     
     private Runnable mTask = new Runnable() {
         public void run() {            
             for (int i = 1; i > 0; ++i) {
                 if (mConnecting == false)
                     connect();
                 // Retry connection to Inform Online service every 10 minutes
                 if (mCondition.block(600 * 1000))
                     break;
             }
         }
     };
       
     @Override
     public void onCreate() {        
         Thread persistentConnectionThread = new Thread(null, mTask, "InformOnlineService");        
         mCondition = new ConditionVariable(false);
         persistentConnectionThread.start();
     }
     
     @Override
     public void onDestroy() {
         if (isInitialized())
             serializeSession();
         
         mCondition.open();
     }
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder {
         public InformOnlineService getService() {
             return InformOnlineService.this;
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
     
     public boolean isInitialized()
     {
         return mInitialized;
     }
     
     public boolean isRegistered()
     {
         return Collect.getInstance().getInformOnlineState().hasRegistration();
     }
     
     // Is Inform Online available?
     public boolean isRespondingToPings()
     {
         return mServicePingSuccessful;
     }
     
     // Registered, connected & signed in
     public boolean isSignedIn()
     {
         return mSignedIn;
     }
     
     /*
      * Sign in to the Inform Online service
      */
     private boolean checkin()
     {
         // Assume we are registered unless told otherwise
         boolean registered = true;
         
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("deviceId", Collect.getInstance().getInformOnlineState().getDeviceId()));
         params.add(new BasicNameValuePair("deviceKey", Collect.getInstance().getInformOnlineState().getDeviceKey()));
         params.add(new BasicNameValuePair("fingerprint", Collect.getInstance().getInformOnlineState().getDeviceFingerprint()));
         
         String checkinUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/checkin";
         String postResult = HttpUtils.postUrlData(checkinUrl, params);            
         JSONObject checkin;
         
         try {
             Log.d(Collect.LOGTAG, t + "parsing postResult " + postResult);                
             checkin = (JSONObject) new JSONTokener(postResult).nextValue();
             
             String result = checkin.optString(InformOnlineState.RESULT, InformOnlineState.FAILURE);
             
             if (result.equals(InformOnlineState.OK)) {
                 Log.i(Collect.LOGTAG, t + "successful checkin");
                 
                 if (checkin.has("defaultdb")) {
                     Log.i(Collect.LOGTAG, t + "assigning default database " + checkin.getString("defaultDb"));
                     Collect.getInstance().getInformOnlineState().setDefaultDatabase(checkin.getString("defaultDb"));
                 }
             } else if (result.equals(InformOnlineState.FAILURE)) {
                 Log.w(Collect.LOGTAG, t + "checkin unsuccessful");
                 registered = false;
             } else {
                 // Something bad happened
                 Log.e(Collect.LOGTAG, t + "system error while processing postResult");
             }                
         } catch (NullPointerException e) {
             // Communication error
             Log.e(Collect.LOGTAG, t + "no postResult to parse.  Communication error with node.js server?");
             e.printStackTrace();            
         } catch (JSONException e) {
             // Parse error (malformed result)
             Log.e(Collect.LOGTAG, t + "failed to parse postResult " + postResult);
             e.printStackTrace();
         }
         
         Log.i(Collect.LOGTAG, t + "device registration state is " + registered);
 
         // Clear the session for subsequent requests and reset stored state
         if (registered == false)          
             Collect.getInstance().getInformOnlineState().resetDevice();
         
         return registered;
     }
     
     /*
      * Try and say "goodbye" to Inform Online so that we know that 
      * this client's session is no longer needed.
      * 
      * This is a "best effort" method and it is possible that the device may
      * have already been checked out by another process (such as resetting the device).
      */
     @SuppressWarnings("unused")
     private boolean checkout()
     {
         boolean saidGoodbye = false;
         
         String checkoutUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/checkout";
         String getResult = HttpUtils.getUrlData(checkoutUrl);
         JSONObject checkout;
         
         try {
             Log.d(Collect.LOGTAG, t + "parsing getResult " + getResult);                
             checkout = (JSONObject) new JSONTokener(getResult).nextValue();
             
             String result = checkout.optString(InformOnlineState.RESULT, InformOnlineState.ERROR);
             
             if (result.equals(InformOnlineState.OK)) {
                 Log.i(Collect.LOGTAG, t + "said goodbye to Inform Online");
                 saidGoodbye = true;
             } else 
                 Log.i(Collect.LOGTAG, t + "device checkout unnecessary");
         } catch (NullPointerException e) {
             // Communication error
             Log.e(Collect.LOGTAG, t + "no getResult to parse.  Communication error with node.js server?");
             e.printStackTrace();
         } catch (JSONException e) {
             // Parse error (malformed result)
             Log.e(Collect.LOGTAG, t + "failed to parse getResult " + getResult);
             e.printStackTrace();
         }
         
         Collect.getInstance().getInformOnlineState().setSession(null);
         
         return saidGoodbye;
     }
     
     /*
      * Connect to the Inform Online service and if registered, attempt to sign in
      */
     private void connect()
     {
         mConnecting = true;
         
         if (!mInitialized) {
             Collect.getInstance().setInformOnlineState(new InformOnlineState(getApplicationContext()));
             restoreSession();
         }
         
         Log.d(Collect.LOGTAG, t + "pinging " + getString(R.string.tf_default_nodejs_server) + ":" + getText(R.string.tf_default_nodejs_port));
         
         // Try to ping the service to see if it is "up" (and determine whether we are registered)
         String pingUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/ping";
         String getResult = HttpUtils.getUrlData(pingUrl);
         JSONObject ping;
         
         try {
             Log.d(Collect.LOGTAG, t + "parsing getResult " + getResult);                
             ping = (JSONObject) new JSONTokener(getResult).nextValue();
             
             String result = ping.optString(InformOnlineState.RESULT, InformOnlineState.ERROR);
 
             // Online and registered (checked in)
             if (result.equals(InformOnlineState.OK)) {
                 Log.i(Collect.LOGTAG, t + "ping successful (we are connected and checked in)");
                mSignedIn = true;
             } else if (result.equals(InformOnlineState.FAILURE)) {
                 Log.w(Collect.LOGTAG, t + "ping successful but not signed in (will attempt checkin)");
                 mServicePingSuccessful = true;
                 
                 if (Collect.getInstance().getInformOnlineState().hasRegistration() && checkin()) {
                     Log.i(Collect.LOGTAG, t + "checkin successful (we are connected)");
                     mSignedIn = true;
                 } else {
                     Log.w(Collect.LOGTAG, t + "checkin failed (registration invalid)");
                    /*
                     * We're no longer registered even though we thought we were at one point.
                     * 
                     * Now what?
                     */
                 }
             } else {
                 // Assume offline
                 Log.w(Collect.LOGTAG, t + "ping failed (we are offline)");
                 mSignedIn = false;
             }
         } catch (NullPointerException e) {
             // This usually indicates a communication error and will send us into an offline state
             Log.e(Collect.LOGTAG, t + "ping error while communicating with service (we are offline)");
             e.printStackTrace();
             mServicePingSuccessful = mSignedIn = false;            
         } catch (JSONException e) {
             // Parse errors (malformed result) send us into an offline state
             Log.e(Collect.LOGTAG, t + "ping error while parsing getResult " + getResult + " (we are offline)");
             e.printStackTrace();
             mServicePingSuccessful = mSignedIn = false;
         } finally {
             if (mSignedIn) {
                 // Update our list of account devices
                 AccountDeviceList.fetchDeviceList();
                 loadDeviceHash();
                 
                 // Update our list of account databases (aka form folders)
                 // TODO
             }
             
             // Unblock
             mInitialized = true;
             mConnecting = false;            
         }
     }
 
     /*
      * Parse the cached device hash and load it into memory for lookup by other pieces of this application.
      * This allows us to have some fall back if we cannot connect to Inform Online immediately.
      */
     private void loadDeviceHash()
     {
         Log.d(Collect.LOGTAG , t + "loading device cache");
               
         try {
             FileInputStream fis = new FileInputStream(new File(FileUtils.DEVICE_CACHE_FILE_PATH));        
             InputStreamReader reader = new InputStreamReader(fis);
             BufferedReader buffer = new BufferedReader(reader, 8192);
             StringBuilder sb = new StringBuilder();
             
             String cur;
     
             while ((cur = buffer.readLine()) != null) {
                 sb.append(cur + "\n");
             }
             
             buffer.close();
             reader.close();
             fis.close();
             
             try {
                 JSONArray jsonDevices = (JSONArray) new JSONTokener(sb.toString()).nextValue();
                 
                 for (int i = 0; i < jsonDevices.length(); i++) {
                     JSONObject jsonDevice = jsonDevices.getJSONObject(i);
     
                     AccountDevice device = new AccountDevice(
                             jsonDevice.getString("id"),
                             jsonDevice.getString("alias"),
                             jsonDevice.getString("email"),
                             jsonDevice.getString("status"));
     
                     // Optional information that will only be present if the user is also an account owner
                     device.setLastCheckin(jsonDevice.optString("lastCheckin"));
                     device.setPin(jsonDevice.optString("pin"));
                     device.setTransferStatus(jsonDevice.optString("transfer"));
     
                     Collect.getInstance().getAccountDevices().put(device.getId(), device);
                 }
             } catch (JSONException e) {
                 // Parse error (malformed result)
                 Log.e(Collect.LOGTAG, t + "failed to parse JSON " + sb.toString());
                 e.printStackTrace();
             }
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, t + "unable to read device cache: " + e.toString());
             e.printStackTrace();
         }
     }    
     
     /*
      * Determine if the Inform Online service is "up"
      * Do not check for authentication
      */
     @SuppressWarnings("unused")
     private boolean ping()
     {
         boolean alive = false;
         
         // Try to ping the service to see if it is "up"
         String pingUrl = Collect.getInstance().getInformOnlineState().getServerUrl() + "/ping";
         String getResult = HttpUtils.getUrlData(pingUrl);
         JSONObject ping;
         
         try {
             Log.d(Collect.LOGTAG, t + "parsing getResult " + getResult);                
             ping = (JSONObject) new JSONTokener(getResult).nextValue();
             
             String result = ping.optString(InformOnlineState.RESULT, InformOnlineState.ERROR);
             
             if (result.equals(InformOnlineState.OK) || result.equals(InformOnlineState.FAILURE))
                 alive = true;
         } catch (NullPointerException e) {
             // Communication error
             Log.e(Collect.LOGTAG, t + "no getResult to parse.  Communication error with node.js server?");
             e.printStackTrace();
         } catch (JSONException e) {
             // Parse error (malformed result)
             Log.e(Collect.LOGTAG, t + "failed to parse getResult " + getResult);
             e.printStackTrace();
         }
         
         return alive;
     }
     
     /*
      * Restore a serialized session from disk
      */
     private void restoreSession()
     {        
         // Restore any serialized session information
         File sessionCache = new File(FileUtils.SESSION_CACHE_FILE_PATH);
         
         if (sessionCache.exists()) {
             Log.i(Collect.LOGTAG, t + "restoring cached session");
             
             try {
                 InformOnlineSession session = new InformOnlineSession();
                 
                 FileInputStream fis = new FileInputStream(sessionCache);
                 ObjectInputStream ois = new ObjectInputStream(fis);
                 session = (InformOnlineSession) ois.readObject();
                 ois.close();
                 fis.close();
                 
                 Collect.getInstance().getInformOnlineState().setSession(new BasicCookieStore());
                 Iterator<InformOnlineSession> cookies = session.getCookies().iterator();
                 
                 if (cookies.hasNext()) {
                     InformOnlineSession ios = cookies.next();
                     
                     BasicClientCookie bcc = new BasicClientCookie(ios.getName(), ios.getValue());
                     bcc.setDomain(ios.getDomain());
                     bcc.setExpiryDate(ios.getExpiryDate());
                     bcc.setPath(ios.getPath());
                     bcc.setVersion(ios.getVersion());
                  
                     Collect.getInstance().getInformOnlineState().getSession().addCookie(bcc);
                 }
             } catch (Exception e) {
                 Log.w(Collect.LOGTAG, t + "problem restoring cached session " + e.toString());
                 e.printStackTrace();
                 
                 // Don't leave a broken file hanging
                 FileUtils.deleteFile(FileUtils.SESSION_CACHE_FILE_PATH);
                 
                 // Clear the session
                 Collect.getInstance().getInformOnlineState().setSession(null);
             }
         }
     }
     
     /*
      * Serialize the current session to disk
      */
     private void serializeSession()
     {
         // Attempt to serialize the session for later use
         if (Collect.getInstance().getInformOnlineState().getSession() instanceof CookieStore) {
             Log.i(Collect.LOGTAG, t + "serializing session");
             
             try {
                 InformOnlineSession session = new InformOnlineSession();
                 
                 Iterator<Cookie> cookies = Collect.getInstance().getInformOnlineState().getSession().getCookies().iterator();
                 
                 while (cookies.hasNext()) {
                     Cookie c = cookies.next();                    
                     session.getCookies().add(new InformOnlineSession(
                             c.getDomain(),
                             c.getExpiryDate(),
                             c.getName(),
                             c.getPath(),
                             c.getValue(),
                             c.getVersion()
                     ));
                 }
                 
                 FileOutputStream fos = new FileOutputStream(new File(FileUtils.SESSION_CACHE_FILE_PATH));
                 ObjectOutputStream out = new ObjectOutputStream(fos);
                 out.writeObject(session);
                 out.close();
                 fos.close();
             } catch (Exception e) {
                 Log.w(Collect.LOGTAG, t + "problem serializing session " + e.toString());
                 e.printStackTrace();
                 
                 // Make sure that we don't leave a broken file hanging
                 FileUtils.deleteFile(FileUtils.SESSION_CACHE_FILE_PATH);
             }
         }
     }
 }
