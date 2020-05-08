 package org.gnuton.newshub.tasks;
 
 import android.os.AsyncTask;
 import android.util.Log;

import org.gnuton.newshub.utils.Notifications;

 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Scanner;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Created by gnuton on 5/19/13.
  * listener must implement OnRequestCompleted.
  */
 public class DownloadWebTask extends AsyncTask<String, Void, String>{
     private static final String TAG = "DOWNLOAD_WEB_TASK";
 
     private static OnRequestCompletedListener listener;
     public DownloadWebTask(Object o) {
         if (o instanceof OnRequestCompletedListener) {
             this.listener = (OnRequestCompletedListener) o;
         } else {
             throw new ClassCastException(o.toString() + " must implement DownloadWebTask.OnRequestCompletedListener");
         }
     }
 
     @Override
     protected String doInBackground(String... urls) {
         try {
             return downloadUrl(urls[0]);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     protected void onPostExecute(String s) {
         listener.onRequestCompleted(s);
     }
 
     static public String downloadUrl(String url) throws IOException {
         InputStream is = null;
         Log.d(TAG, "Downloading: " + url);
         try {
             URL u = new URL(url);
             HttpURLConnection conn = (HttpURLConnection) u.openConnection();
             conn.setInstanceFollowRedirects(true);
 
             conn.setReadTimeout(10 * 1000);
             conn.setConnectTimeout(10 * 1000);
             conn.setRequestMethod("GET");
             conn.setDoInput(true);
             //conn.connect();
 
             int status = conn.getResponseCode();
             if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == HttpURLConnection.HTTP_SEE_OTHER)
             {
                 String redUrl = conn.getHeaderField("Location");
                 String cookies = conn.getHeaderField("Set-Cookie");
 
                 Log.d(TAG, "Redirect URL:>" + redUrl + "<");
                 conn = (HttpURLConnection) new URL(redUrl).openConnection();
                 conn.setRequestProperty("Cookie", cookies);
             }
 
             Log.d(TAG, "Status: " + status);
             is = conn.getInputStream();
 
             if ("gzip".equals(conn.getContentEncoding())) {
                 Log.d(TAG,"GZIP input stream");
                 is = new GZIPInputStream(is);
             }
 
             return readText(is);
        } catch(Exception e) {
            Notifications.showMsg(e.getMessage());
             return null;
         }
         finally {
             if (is != null)
                 is.close();
         }
     }
 
     static private String readText(InputStream is) {
         Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
         if (scanner.hasNext())
             return scanner.next();
         return "";
     }
 
     public interface OnRequestCompletedListener {
         public void onRequestCompleted(final String buffer);
     }
 }
