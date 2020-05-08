 package org.jdleesmiller.trackjd;
 
 import com.loopj.android.http.*;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.util.Log;
 
 public class DataUploader implements Runnable {
 
   /**
    * This is used for both the connection timeout (when trying to make the
    * connection, wait this long before giving up) and the socket timeout (once a
    * connection is made, wait this long for the upload to finish -- that is, for
    * us to receive the response). The timeouts are important, because Internet
    * connectivity will often be intermittent, and we have to retry reasonably
    * quickly.
    */
  private static final int TIMEOUT_MILLIS = 5000;
 
   /**
    * Wait this long between attempting uploads.
    */
  private static final long UPLOAD_INTERVAL_MILLIS = 5000;
 
   /**
    * Max number of records to upload at once. This is over all sensors.
    */
  private static final int MAX_RECORDS_TO_UPLOAD = 5000;
 
   private final TrackJDService service;
 
   private final SharedPreferences prefs;
 
   private final Handler uploadHandler;
 
   public DataUploader(TrackJDService service) {
     this.service = service;
     this.prefs = service.getSharedPreferences(Constants.PREFS_FILE_NAME,
         Context.MODE_PRIVATE);
     this.uploadHandler = new Handler();
   }
 
   /**
    * Check devices's reported WiFi state. Note that this does not imply that we
    * can actually connect to the server.
    * 
    * @return
    */
   public boolean isWiFiOn() {
     return ((ConnectivityManager) service
         .getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(
         ConnectivityManager.TYPE_WIFI).isConnected();
   }
 
   /**
    * Start checking whether to upload. Note that this doesn't actually start an
    * upload unless we're on WiFi.
    */
   public void start() {
     uploadHandler.post(this);
   }
 
   /**
    * Stop checking whether to upload.
    */
   public void stop() {
     uploadHandler.removeCallbacks(this);
   }
 
   /**
    * @return fully qualified URI to post data to
    */
   public String getLogPath() {
     String serverName = prefs.getString(Constants.PREF_SERVER_NAME, "");
     return "http://" + serverName + "/log";
   }
 
   /**
    * Runs periodically; checks WiFi state and, if we are connected, attempts an
    * upload.
    */
   public void run() {
     if (isWiFiOn()) {
       // we're on WiFi -- try to upload
       AsyncHttpClient client = new AsyncHttpClient();
       client.setTimeout(TIMEOUT_MILLIS);
       RequestParams params = new RequestParams();
      addDeviceIdentifiers(params);
       final long lastIdUploaded = service.getDataLogger().addToUpload(params,
           MAX_RECORDS_TO_UPLOAD);
       Log.d("DataUploader", "POST: " + getLogPath());
       client.post(service, getLogPath(), params,
           new AsyncHttpResponseHandler() {
             @Override
             public void onSuccess(String response) {
               Log.d("DataUploader", "log success");
               service.getDataLogger().clearLastUpload(lastIdUploaded);
             }
 
             @Override
             public void onFinish() {
               Log.d("DataUploader", "log finished");
               // always keep polling
               uploadHandler.postDelayed(DataUploader.this,
                   UPLOAD_INTERVAL_MILLIS);
             }
           });
     } else {
       // no WiFi connection; try again later
       uploadHandler.postDelayed(this, UPLOAD_INTERVAL_MILLIS);
     }
   }
 
   /**
    * Try to get as many unique device identifiers as we can. I assume that these
    * are cheap to get, so it isn't too bad to compute them on every post. They
    * should not change, but they can become known and unknown -- e.g. if
    * Bluetooth is not turned on, the bdaddr comes back as "<UNKNOWN>". If we
    * can't get one, try to continue running -- we have several device
    * identifiers that we can use.
    * 
    * @param postData
    */
  private void addDeviceIdentifiers(RequestParams params) {
     params.put("installation", Installation.id(service));
 
     try {
       params.put("bdaddr", BluetoothAdapter.getDefaultAdapter().getAddress());
     } catch (RuntimeException e) {
       Log.w("DataUploader", "failed to get Bluetooth MAC address");
     }
 
     try {
       WifiManager wifiManager = (WifiManager) service
           .getSystemService(Context.WIFI_SERVICE);
       params.put("macaddr", wifiManager.getConnectionInfo().getMacAddress());
     } catch (RuntimeException e) {
       Log.w("DataUploader", "failed to get WiFi MAC address");
     }
   }
 }
