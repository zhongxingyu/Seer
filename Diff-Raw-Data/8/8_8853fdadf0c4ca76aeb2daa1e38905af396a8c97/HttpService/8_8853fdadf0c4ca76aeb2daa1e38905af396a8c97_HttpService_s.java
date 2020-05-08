 package net.anei.cadpage;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.Queue;
import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.Process;
 
 public class HttpService extends Service {
   
   public static class HttpRequest {
     
     private Uri uri = null;
     private URL url = null;
     private int connectTimeout = 60000;
     private int readTimeout = 30000;
     
     private int status = -1;
     private String result = null;
     private String content = null;
     
     public HttpRequest(Uri uri) {
       this.uri = uri;
     }
     
     public HttpRequest(URL url) {
       this.url = url;
     }
     
     public HttpRequest(Uri uri, int connectTimeout, int readTimeout) {
       this.uri = uri;
       this.connectTimeout = connectTimeout;
       this.readTimeout = readTimeout;
     }
     
     public HttpRequest(URL url, int connectTimeout, int readTimeout) {
       this.url = url;
       this.connectTimeout = connectTimeout;
       this.readTimeout = readTimeout;
     }
     
     /**
      * @return return requested URL code
      */
     public URL getUrl() {
       return url;
     }
     
     public int getConnectionTimeout() {
       return connectTimeout;
     }
     
     public int getReadTimeout() {
       return readTimeout;
     }
     
     /**
      * Connect to remote host and return result
      * This should not be called on the main dispatch thread
      */
     void connect() {
       
       // If we weren't passed a URL initially, it is time to convert the
       // Uri to a URL.  This this throws an malformed URL exception, ignore it
       // we will report a bad URL later on
       if (url == null) {
         try {
           url = new URL(uri.toString());
         } catch (MalformedURLException ex) {
           Log.e(ex.getMessage());
         }
         
         // If we have a URL, connnect to it
         if (url == null) {
           Log.i("Sending:" + uri.toString());
           status = 400;
           result = "Bad request:" + uri.toString();
         } else {
           Log.i("Sending:" + url.toString());
           HttpURLConnection connect = null;
           InputStream is = null;
           try {
             connect = (HttpURLConnection)url.openConnection();
             connect.setConnectTimeout(connectTimeout);
             connect.setReadTimeout(readTimeout);
             connect.connect();
             status = connect.getResponseCode();
             result = connect.getResponseMessage();
             
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             is = connect.getInputStream();
             int b;
             while ((b = is.read()) >= 0) os.write(b);
             content = os.toString();
           } 
           
           catch (IOException ex) {
             status = 408;
             result = "IO Error";
           }
           finally {
             if (is != null)
               try { is.close(); } catch (IOException ex) {}
             if (connect != null) connect.disconnect();
           }
           Log.i("Result:" + status + ": " + result + '\n' + content);
         }
       }
     }
     
     void process() {
       if (status != 200) {
         processError(status, result);
       } else {
         processContent(content);
       }
     }
 
     /**
      * This will be called when the HTTP request returns an error or failure status
      * @param status status
      * @param result result
      */
     protected void processError(int status, String result) {}
 
     /**
      * This will be called when the HTTP request returns successful result
      * @param content response contents
      */
     protected void processContent(String content) {
      Matcher match = BODY_PTN.matcher(content);
      if (match.find()) content = match.group(1);
       processBody(content);
     }
    private static final Pattern BODY_PTN = Pattern.compile("<body>(.*?)</body>", Pattern.CASE_INSENSITIVE);
 
     /**
      * This will be called when the HTTP request returns a successful result
      * and contents contains a parsable body component
      * @param body body component from response content
      */
     protected void processBody(String body) {}
   }
   
   private static Handler mHandler;
 
   // Wake lock and synchronize lock
   private static PowerManager.WakeLock mWakeLock = null;
   
   // Master request queue
   private static Queue<HttpRequest> reqQueue =new LinkedList<HttpRequest>();
 
   @Override
   public void onCreate() {
     
     // Create a handler which (we hope) will link to the main dispatch thread msg queue
     mHandler = new Handler();
     
     // Launch the HttpServiceThread that is going to do all of the work
     new HttpServiceThread();
   }
 
   @Override
   public IBinder onBind(Intent intent) {
     return null;
   }
 
   private final class HttpServiceThread extends Thread {
     
     public HttpServiceThread() {
       super("HttpServiceThread");
       setPriority(Process.THREAD_PRIORITY_BACKGROUND);
       start();
     }
 
     @Override
     public void run() {
       
       try {
          
         while (true) {
           
           // Get next request from request queue.  If there aren't any remaining
           // entries, it is time to kill this service
           HttpRequest req;
           synchronized(reqQueue) {
             req = reqQueue.poll();
             if (req == null) {
               if (mWakeLock != null) mWakeLock.release();
               stopSelf();
               return;
             }
           }
           
           // Build and fire off the Http request
           req.connect();
           
           // And call the request process response method on the main dispatch thread
           final HttpRequest req2 = req;
           mHandler.post(new Runnable(){
             @Override
             public void run() {
               req2.process();
             }});
         }
       } 
       
       // Any exceptions that get thrown should be rethrown on the dispatch thread
       catch (final Exception ex) {
         mHandler.post(new Runnable(){
           @Override
           public void run() {
             throw new RuntimeException(ex.getMessage(), ex);
           }});
       }
     }
   }
 
   /**
    * Initiate an HTTP request
    * @param context current context
    * @param request HTTP request to be initiated
    */
   public static void addHttpRequest(Context context, HttpRequest request) {
     
     // Everything is synchronized by the request queue
     synchronized (reqQueue) {
       
       // If we haven't established a power wake lock, do that now.
       if (mWakeLock == null) {
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Log.LOGTAG+".HttpService");
         mWakeLock.setReferenceCounted(false);
       }
       if(!mWakeLock.isHeld()) mWakeLock.acquire();
       
       // Add new request to request queue and launch the HttpService
       // We don't need to pass anything, just make sure it got started
       reqQueue.add(request);
       context.startService(new Intent(context, HttpService.class));
     }
   }
 }
