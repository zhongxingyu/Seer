 package com.slalomdigital.opscheck;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.AsyncTask;
 import android.util.Log;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aaronc
  * Date: 8/12/13
  * Time: 6:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CheckTask extends AsyncTask<Void, Void, Boolean> {
     private CheckListener checkListener;
     private Context context;
     private String body = null;
     private String type = null;
     private String encoding = null;
     private boolean showPopup;
     private String url;
     private HttpResponse response;
 
     public CheckTask(CheckListener checkListener, boolean showPopup, Context context) {
         this.checkListener = checkListener;
         this.context = context;
         this.showPopup = showPopup;
     }
 
     @Override
     protected Boolean doInBackground(Void... params) {
         boolean returnValue = true;
         // Get the base URL from the preferences
 
         // Get the key from the preferences
 
         // properly create the url
         String versionName = null;
         int versionCode;
         String appName = null;
         String key = null;
         String server = null;
         try {
             PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
             versionName = pInfo.versionName;
             versionCode = pInfo.versionCode;
             appName = context.getPackageName();
         } catch (PackageManager.NameNotFoundException e) {
             Log.e("OpsCheck", "Exception getting package info: " + e.getLocalizedMessage());
             // Let the app operate normally when there's a problem with OpsCheck
             returnValue = true;
         }
 
         try {
             // Pull from props
             InputStream inputStream = context.getResources().getAssets().open("opscheckconfig.properties");
             Properties properties = new Properties();
             properties.load(inputStream);
 
             key = properties.getProperty("key", null);
             server = properties.getProperty("server", null);
         } catch (Exception e) {
             Log.e("OpsCheck", "Exception while getting the URL or key from the properties:" + e.getLocalizedMessage());
             // Let the app operate normally when there's a problem with OpsCheck
             returnValue = true;
         }
 
         if (server != null && key != null && versionName != null && appName != null) {
             url = server + "?app_key=" + key + "&version=" + versionName + "&build=" + appName;
 
             HttpClient httpclient = new DefaultHttpClient();
 
             // Prepare a request object
             HttpGet httpget = new HttpGet(url);
 
             // Execute the request
             try {
                 response = httpclient.execute(httpget);
                 // Examine the response status
                 if (response.getStatusLine().getStatusCode() == 200) {
                     // Check the server status in the headers...
                    Header serverStatusHeader = response.getFirstHeader("Version-Check");
                     if (serverStatusHeader != null && !serverStatusHeader.getValue().trim().equalsIgnoreCase("connect")) {
                         // Don't Connect...
                         returnValue = false;
                     } else {
                         returnValue = true;
                     }
                 } else {
                     Log.e("OpsCheck", "Server returned: " + response.getStatusLine().toString());
 
                     // When we fail to do the check we let the application operate normally...
                     returnValue = true;
                 }
 
                 // Get hold of the response entity
                 // If the response does not enclose an entity, there is no need
                 // to worry about connection release
                 HttpEntity entity = response.getEntity();
 
                 // Get the type
                 type = entity.getContentType().getValue();
 
                 // Get the encoding
                 try {
                     encoding = entity.getContentEncoding().getValue();
                 } catch (Exception e) {
                     Log.i("OpsCheck", "Exception getting encoding header: " + e.getLocalizedMessage());
                 }
 
                 try {
                     // A Simple HTML Response Read
                     InputStream instream = entity.getContent();
                     body = convertStreamToString(instream);
                     // now you have the string representation of the HTML request
                     instream.close();
                 } catch (Exception e) {
                     Log.e("OpsCheck", "Exception reading response body: " + e.getLocalizedMessage());
                 }
             } catch (Exception e) {
                 Log.e("OpsCheck", "Exception during check: " + e.getLocalizedMessage());
 
                 // When we fail to do the check we let the application operate normally...
                 returnValue = true;
             }
         }
 
         // update the preference
         SharedPreferences opsCheckPrefs = context.getSharedPreferences(OpsCheck.PREFERENCES, Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = opsCheckPrefs.edit();
         editor.putBoolean(OpsCheck.SHOULD_CONNECT, returnValue);
         editor.commit();
 
         return returnValue;
     }
 
     @Override
     protected void onPostExecute(Boolean result) {
         // Show an alert if needed...
        if (showPopup && !result && url != null && body != null) {
             Intent intent = new Intent(context, OpsCheckDialogActivity.class);
             intent.putExtra(OpsCheckDialogActivity.EXTRA_BODY, body);
             if (type != null) intent.putExtra(OpsCheckDialogActivity.EXTRA_TYPE, type);
             if (encoding != null) intent.putExtra(OpsCheckDialogActivity.EXTRA_ENCODING, encoding);
             intent.putExtra(OpsCheckDialogActivity.EXTRA_URL, url);
             context.startActivity(intent);
         }
 
         // Call the callback if one was set...
         if (checkListener != null) {
             //Call the listener
             checkListener.onCheck(result, response);
         }
     }
 
     private static String convertStreamToString(InputStream is) {
     /*
      * To convert the InputStream to String we use the BufferedReader.readLine()
      * method. We iterate until the BufferedReader return null which means
      * there's no more data to read. Each line will appended to a StringBuilder
      * and returned as String.
      */
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
 
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
 }
