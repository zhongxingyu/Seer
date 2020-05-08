 package com.example.counteverything;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Created by stylesuxx on 10/14/13.
  *
  * The API.
  */
 public class Api extends AsyncTask<JSONObject, Void, String> {
     private Exception exception;
     private final Context mContext;
     private final static String TAG = "API";
 
     public Api(Context context) {
         this.mContext = context;
     }
 
     /**
      * This method is executed in background when the execute method is invoked on this Class.
      *
      * @param params A JSON object with the action and all needed parameters
      * @return A string about Success or Error of the API call,
      */
     @Override
     protected String doInBackground(JSONObject... params) {
         int BUFFER_SIZE = 2000;
         InputStream in = null;
         String str = "";
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
         String token = prefs.getString("api_token", "");
         String url = prefs.getString("api_url", "");
         JSONObject p = params[0];
         try {
             p.put("token", token);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         String URL = url +"?json=[" + p.toString() + "]";
         Log.v(TAG, URL);
 
         // Try to connect to the server
         try {
             in = OpenHttpConnection(URL);
             if(in != null){
                 InputStreamReader isr = new InputStreamReader(in);
                 int charRead;
                 str = "";
                 char[] inputBuffer = new char[BUFFER_SIZE];
                 try {
                     while ((charRead = isr.read(inputBuffer))>0){
                         //---convert the chars to a String---
                         String readString = String.copyValueOf(inputBuffer, 0, charRead);
                         str += readString;
                         inputBuffer = new char[BUFFER_SIZE];
                     }
                     in.close();
                 } catch (IOException e) {
                     this.exception = e;
                     return "Error";
                 }
             }
             else{
                 this.exception = new IOException("Check url.");
                 return "Error";
             }
         } catch (IOException e) {
             this.exception = e;
             return "Error";
         }
         try {
             JSONObject json = new JSONObject(str);
             return json.get("message").toString();
         } catch (JSONException e) {
            e.printStackTrace();
         }

        return "JSON ERROR";
     }
 
     /**
      * Executed after doInBackground returns. Prints a toast to notify the user about success
      * or error of the API Request.
      *
      * @param str The return value of the background process
      */
     protected void onPostExecute(String str) {
         if(str.equals("Error")) {
             Toast.makeText(this.mContext, "Error: " + this.exception.getMessage(), Toast.LENGTH_SHORT).show();
         }
         else if (!str.isEmpty()) {
             Toast.makeText(this.mContext, str, Toast.LENGTH_SHORT).show();
         }
     }
 
     /**
      * Open an url and return an inputstream
      *
      * @param urlString The url to connect to
      * @return Inputstream or null
      * @throws IOException
      */
     private InputStream OpenHttpConnection(String urlString) throws IOException {
         InputStream in = null;
         int response = -1;
 
         URL url = new URL(urlString);
         URLConnection conn = url.openConnection();
 
         if (!(conn instanceof HttpURLConnection))
             throw new IOException("Not an HTTP connection");
 
         try{
             HttpURLConnection httpConn = (HttpURLConnection) conn;
             httpConn.setAllowUserInteraction(false);
             httpConn.setInstanceFollowRedirects(true);
             httpConn.setRequestMethod("GET");
             httpConn.connect();
 
             response = httpConn.getResponseCode();
             if (response == HttpURLConnection.HTTP_OK) {
                 in = httpConn.getInputStream();
             }
         } catch(java.net.UnknownHostException ex){
             throw new IOException("Check URL.");
         } catch(Exception ex){
             throw new IOException("Check API token.");
         }
         return in;
     }
 }
