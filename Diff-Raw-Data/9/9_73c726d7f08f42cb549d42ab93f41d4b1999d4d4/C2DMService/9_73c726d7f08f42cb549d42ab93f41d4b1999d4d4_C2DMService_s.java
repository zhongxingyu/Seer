 package com.purplehatstands.clementine.remote;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.PowerManager;
 import android.util.Log;
 
 public class C2DMService extends IntentService implements AuthTokenReceiver {
   private static final String TAG = "C2DMService";
   
   private static PowerManager.WakeLock wake_lock_;
   private static final String WAKELOCK_KEY = "com.purplehatstands.clementine.remote";
   
   // Dev server that supports HTTPS with the correct cert.
   private static final String DEV_SERVER = "dev-dot-latest-dot-clementine-player.appspot.com";
   private static final String REGISTER_PATH = "/c2dm/register";
   private static final String COOKIE_PATH = "/_ah/login?continue=http://localhost/&auth=";
   
   private String registration_id_;
   
   private AndroidHttpClient client_;
   private HttpContext http_context_;
 
   public C2DMService() {
     super("C2DMService");
     client_ = AndroidHttpClient.newInstance("ClementineRemote");
     // Create a local instance of cookie store
     BasicCookieStore cookie_store = new BasicCookieStore();
 
     // Create local HTTP context
     http_context_ = new BasicHttpContext();
     // Bind custom cookie store to the local context
     http_context_.setAttribute(ClientContext.COOKIE_STORE, cookie_store);
   }
 
   @Override
   protected void onHandleIntent(Intent intent) {
     Log.d(TAG, "onHandleIntent");
     try {
       Context context = getApplicationContext();
       if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
         HandleRegistration(context, intent);
       } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
         HandleMessage(context, intent);
       }
     } finally {
       wake_lock_.release();
     }
   }
   
   // C2DM
   private void HandleRegistration(Context context, Intent intent) {
     Log.d(TAG, "HandleRegistration");
     String registration = intent.getStringExtra("registration_id"); 
     if (intent.getStringExtra("error") != null) {
         // Registration failed, should try again later.
     } else if (intent.getStringExtra("unregistered") != null) {
         // unregistration done, new messages from the authorized sender will be rejected
     } else if (registration != null) {
        // Send the registration ID to the 3rd party site that is sending the messages.
        // This should be done in a separate thread.
        // When done, remember that all registration is done.
       Log.d(TAG, "C2DM Registration: " + registration);
 
       SharedPreferences prefs = context.getSharedPreferences("c2dm", Context.MODE_PRIVATE);
       Editor editor = prefs.edit();
       editor.putString("c2dm_reg", registration);
       registration_id_ = registration;
       RegisterDeviceWithServer();
     }
   }
   
   private void RegisterDeviceWithServer() {
     Log.d(TAG, "RegisterDeviceWithServer");
     Context context = getApplicationContext();
     AccountManager manager = AccountManager.get(this);
     Account google_account = manager.getAccountsByType("com.google")[0];
     GetAuthTokenCallback callback = new GetAuthTokenCallback("ah", this, context);
     manager.invalidateAuthToken("com.google", "ah");
     manager.getAuthToken(google_account, "ah", true, callback, null);
   }
   
   public void OnAuthTokenReceived(String service, String token) {
     Log.d(TAG, "OnAuthTokenReceived");
     new GetCookieTask().execute(token);
   }
   
   private class GetCookieTask extends AsyncTask<String, Integer, Boolean> {
 
     @Override
     protected Boolean doInBackground(String... params) {
       String token = params[0];
      AndroidHttpClient client = AndroidHttpClient.newInstance("ClementineRemote");
      client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
 
       HttpGet get = new HttpGet(COOKIE_PATH + token);
       Log.d(TAG, get.getURI().toString());
       try {
        HttpResponse response = client.execute(new HttpHost(DEV_SERVER, 443, "https"), get, http_context_);
         // This should get redirected and fill in the ACSID cookie.
         return response.getStatusLine().getStatusCode() == 302;
       } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       } finally {
        client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
       }
       return false;
     }
     
     @Override
     protected void onPostExecute(Boolean result) {
       Log.d(TAG, "Got Cookie: " + result.toString());
       if (result) {
         new DeviceRegistration().execute(registration_id_);
       } else {
         client_.close();
       }
     }
   }
   
   
   private class DeviceRegistration extends AsyncTask<String, Integer, Boolean> {
 
     @Override
     protected Boolean doInBackground(String... params) {
       String registration_id = params[0];
       HttpPost post = new HttpPost(REGISTER_PATH);
       List<NameValuePair> post_data = new ArrayList<NameValuePair>();
       post_data.add(new BasicNameValuePair("registration_id", registration_id));
       post_data.add(new BasicNameValuePair("brand", Build.BRAND));
       post_data.add(new BasicNameValuePair("manufacturer", Build.MANUFACTURER));
       post_data.add(new BasicNameValuePair("device", Build.DEVICE));
       post_data.add(new BasicNameValuePair("model", Build.MODEL));
       try {
         post.setEntity(new UrlEncodedFormEntity(post_data));
         HttpResponse response = client_.execute(
             new HttpHost(DEV_SERVER, 443, "https"), post, http_context_);
         return response.getStatusLine().getStatusCode() == 200;
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       } catch (IOException e) {
         e.printStackTrace();
       } finally {
         client_.close();
       }
       return false;
     } 
   }
   
   // C2DM
   private void HandleMessage(Context context, Intent intent) {
     String accountName = intent.getExtras().getString("account");
     String message = intent.getExtras().getString("message"); 
     Log.d(TAG, "Message received from: " + accountName);
     Log.d(TAG, "Message: " + message);
   }
   
   public static void runIntentInService(Context context, Intent intent) {
     if (wake_lock_ == null) {
       PowerManager power_manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
       wake_lock_ = power_manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
     }
     wake_lock_.acquire();
     
     intent.setClass(context, C2DMService.class);
     context.startService(intent);
   }
 
 
 }
