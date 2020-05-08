 package info.eigenein.openwifi.services;
 
 import android.app.IntentService;
 import android.content.*;
 import android.net.wifi.*;
 import android.support.v4.content.*;
 import android.util.Log;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Tracker;
 import info.eigenein.openwifi.helpers.Settings;
 import info.eigenein.openwifi.helpers.io.SyncHttpClient;
 import info.eigenein.openwifi.sync.ScanResultDownSyncer;
 import info.eigenein.openwifi.sync.ScanResultUpSyncer;
 import info.eigenein.openwifi.sync.Syncer;
 import info.eigenein.openwifi.sync.TaggedRequest;
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 
 import java.io.IOException;
 
 /**
  * Synchronizes the local database with the server database.
  */
 public class SyncIntentService extends IntentService {
     public static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();
 
     /**
      * Used to notify receivers with the service state.
      */
     public static final String STATUS_CODE_EXTRA_KEY = "statusCode";
     /**
      * Used to check whether the device is still connected to the specified wireless network.
      */
     public static final String SSID_EXTRA_KEY = "ssid";
 
     public static final int RESULT_CODE_NOT_SYNCING = 0;
     public static final int RESULT_CODE_SYNCING = 1;
 
     /**
      * Minimal sync period.
      */
     public static final long SYNC_PERIOD_MILLIS = 60L * 60L * 1000L;
 
     public SyncIntentService() {
         super(SERVICE_NAME);
     }
 
     protected void onHandleIntent(final Intent intent) {
         Log.i(SERVICE_NAME + ".onHandleIntent", "Service is running.");
         EasyTracker.getInstance().setContext(this);
 
         // Check current network SSID if specified.
         final String ssid = intent.getStringExtra(SSID_EXTRA_KEY);
         if (ssid != null && !checkSsid(ssid)) {
             // We're not connected or connected to the network other than requested.
             return;
         }
 
         final Settings settings = Settings.with(this);
 
         // Notify the receiver that we're starting.
         sendStatusMessage(RESULT_CODE_SYNCING);
         // The client ID will be used in the HTTP(S) requests.
         final String clientId = settings.clientId();
         // Prepare the HTTP client.
         final HttpClient client = new SyncHttpClient(this);
         // Set the "syncing now" flag.
         settings.edit().syncingNow(true).commit();
         // Start syncing.
         try {
             // Download the scan results.
             sync(client, new ScanResultDownSyncer(settings), clientId);
             // Upload our scan results.
             sync(client, new ScanResultUpSyncer(), clientId);
             // Update last sync time.
             settings.edit().lastSyncTime(System.currentTimeMillis()).commit();
         } finally {
             // Reset the "syncing now" flag.
             settings.edit().syncingNow(false).commit();
             // Ensure immediate deallocation of all system resources.
             client.getConnectionManager().shutdown();
             // Notify the receiver that we've finished.
             sendStatusMessage(RESULT_CODE_NOT_SYNCING);
         }
 
         Log.i(SERVICE_NAME + ".onHandleIntent", "Everything is finished.");
     }
 
     /**
      * Checks that the device is connected to the wireless network with the specified SSID.
      */
     private boolean checkSsid(String expectedSsid) {
         final WifiInfo wifiInfo = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo == null || !wifiInfo.getSSID().equals(expectedSsid)) {
            Log.w(SERVICE_NAME + ".checkSsid", String.format(
                    "SSID has been changed or Wi-Fi is not available. Expected: %s, actual: %s.",
                    expectedSsid,
                    wifiInfo != null ? wifiInfo.getSSID() : "(null)"));
             EasyTracker.getTracker().sendEvent(
                     SERVICE_NAME,
                     "checkSsid",
                     wifiInfo != null ? "SSID mismatch" : "wifiInfo is null",
                     0L);
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Sends the service status message to the local broadcast receivers.
      */
     private void sendStatusMessage(int statusCode) {
         final Intent intent = new Intent(SERVICE_NAME);
         intent.putExtra(STATUS_CODE_EXTRA_KEY, statusCode);
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
     }
 
     /**
      * Performs syncing with the specified syncer.
      */
     private void sync(final HttpClient client, final Syncer syncer, final String clientId) {
         Log.i(SERVICE_NAME + ".sync", "Starting syncing with " + syncer);
         // Prepare the event tracker.
         EasyTracker.getInstance().setContext(this);
         final Tracker tracker = EasyTracker.getTracker();
         // Performance counters.
         final long syncStartTime = System.currentTimeMillis();
         // Synchronization loop.
         while (true) {
             // Get next request.
             final TaggedRequest taggedRequest = syncer.getNextRequest(this);
             if (taggedRequest == null) {
                 Log.d(SERVICE_NAME, "getNextRequest returned null.");
                 break;
             }
             // Initialize the request with the common parameters.
             initializeRequest(taggedRequest.getRequest(), clientId);
             // Execute the request.
             Log.d(SERVICE_NAME + ".sync", "Executing the request: " + taggedRequest.getRequest().getURI());
             final long requestStartTime = System.currentTimeMillis();
             HttpResponse response;
             try {
                 response = client.execute(taggedRequest.getRequest());
             } catch (IOException e) {
                 Log.w(SERVICE_NAME + ".sync", "Syncing is broken: " + e.getMessage());
                 tracker.sendEvent(
                         SERVICE_NAME,
                         "client.execute",
                         String.format("%s/%s", syncer.getClass().getSimpleName(), e.getClass().getSimpleName()),
                         syncer.getSyncedEntitiesCount());
                 break;
             }
             final long requestEndTime = System.currentTimeMillis();
             // Log headers.
             for (Header header : response.getAllHeaders()) {
                 Log.d(SERVICE_NAME + ".sync", String.format("%s: %s", header.getName(), header.getValue()));
             }
             // Check the status code.
             final StatusLine statusLine = response.getStatusLine();
             Log.d(SERVICE_NAME + ".sync", String.format("Request is finished in %sms: %s",
                     requestEndTime - requestStartTime,
                     statusLine));
             // Process the response.
             if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                 Log.w(SERVICE_NAME + ".sync", "Syncing is broken:" + statusLine);
                 tracker.sendEvent(
                         SERVICE_NAME,
                         "client.execute",
                         String.format("%s/%d", syncer.getClass().getSimpleName(), statusLine.getStatusCode()),
                         syncer.getSyncedEntitiesCount());
                 break;
             }
             Log.d(SERVICE_NAME + ".sync", "Processing the response ...");
             final long processResponseStartTime = System.currentTimeMillis();
             final boolean hasNext = syncer.processResponse(this, taggedRequest, response);
             final long processResponseTime = System.currentTimeMillis() - processResponseStartTime;
             Log.d(SERVICE_NAME + ".sync", String.format("Response is processed in %sms.", processResponseTime));
             tracker.sendTiming(SERVICE_NAME, processResponseTime, "syncer.processResponse", syncer.toString());
             if (!hasNext) {
                 Log.i(SERVICE_NAME + ".sync", "Sync is finished.");
                 tracker.sendEvent(SERVICE_NAME, "sync", syncer.getClass().getSimpleName(), syncer.getSyncedEntitiesCount());
                 break;
             }
         }
         // The sync loop is finished. Collect sync statistics.
         final long syncTime = System.currentTimeMillis() - syncStartTime;
         final long syncedEntitiesCount = syncer.getSyncedEntitiesCount();
         final long entitySyncTime = syncedEntitiesCount != 0 ? syncTime / syncedEntitiesCount : 0;
         Log.i(SERVICE_NAME + ".sync", String.format("Synced %s entities in %sms (%sms per entity)",
                 syncedEntitiesCount,
                 syncTime,
                 entitySyncTime));
         // Send sync time.
         tracker.sendTiming(SERVICE_NAME, syncTime, "sync", syncer.toString());
     }
 
     /**
      * Initializes the request.
      */
     private static void initializeRequest(final HttpRequest request, final String clientId) {
         request.setHeader("X-Client-ID", clientId);
         request.setHeader("Accept", "application/json");
         request.setHeader("Content-Type", "application/json");
         request.setHeader("Connection", "Keep-Alive");
     }
 }
