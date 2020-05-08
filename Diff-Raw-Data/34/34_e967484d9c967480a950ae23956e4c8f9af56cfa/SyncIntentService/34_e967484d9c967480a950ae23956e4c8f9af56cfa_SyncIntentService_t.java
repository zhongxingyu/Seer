 package info.eigenein.openwifi.services;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 import info.eigenein.openwifi.helpers.Settings;
 import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
 import info.eigenein.openwifi.persistency.entities.StoredScanResult;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 /**
  * Synchronizes the local database with the server database.
  */
 public class SyncIntentService extends IntentService {
     private static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();
 
     private static final String getUrl = "http://openwifi.info/api/scan-results/%s/%s/";
 
     private static final String postUrl = "http://openwifi.info/api/scan-results/";
 
     /**
      * Maximum allowed number of scan results to be processed at once.
      */
    private static final int PAGE_SIZE = 128;
 
     public SyncIntentService() {
         super(SERVICE_NAME);
     }
 
     protected void onHandleIntent(Intent intent) {
         Log.i(SERVICE_NAME, "Service is running.");
 
         final Settings settings = Settings.with(this);
 
         // Download the results and update last ID.
         final String lastSyncId = downloadScanResults(settings.lastSyncId());
         settings.edit().lastSyncId(lastSyncId).commit();
         // Upload our results.
         final String clientId = settings.clientId();
         uploadScanResults(clientId);
 
         Log.i(SERVICE_NAME, "Everything is finished.");
     }
 
     private String downloadScanResults(String lastSyncId) {
         Log.i(SERVICE_NAME, "Starting to download scan results ...");
         Log.d(SERVICE_NAME, "lastSyncId: " + lastSyncId);
         // TODO: implement downloading.
         Log.i(SERVICE_NAME, "Finished. lastSyncId: " + lastSyncId);
         return lastSyncId;
     }
 
     private void uploadScanResults(String clientId) {
         Log.i(SERVICE_NAME, "Starting to upload scan results ...");
         // Prepare the HTTP client.
         final HttpParams params = new BasicHttpParams();
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         final DefaultHttpClient client = new DefaultHttpClient(params);
         client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
         // Synchronize.
         int syncedResultCount = 0;
         int skip = 0;
        final long syncStartTime = System.currentTimeMillis();
         while (true) {
            // Prepare the page.
            Log.d(SERVICE_NAME, "Querying for the page: " + skip + ", " + PAGE_SIZE);
             List<StoredScanResult> scanResults =
                     ScanResultTracker.getUnsyncedScanResults(this, skip, PAGE_SIZE);
             Log.d(SERVICE_NAME, "scanResults: " + scanResults.size());
             if (scanResults.isEmpty()) {
                 // Finished.
                 break;
             }
             // Prepare the request.
             HttpPost request = new HttpPost(postUrl);
             request.setHeader("X-Client-ID", clientId);
             request.setHeader("Accept", "application/json");
             request.setHeader("Content-Type", "application/json");
             request.setHeader("Connection", "Keep-Alive");
             try {
                 final JSONArray array = new JSONArray();
                 for (StoredScanResult scanResult : scanResults) {
                     array.put(scanResult.toJsonObject());
                 }
                 final String jsonString = array.toString();
                 request.setEntity(new StringEntity(jsonString, HTTP.UTF_8));
             } catch (UnsupportedEncodingException e) {
                 Log.e(SERVICE_NAME, "Could not create the entity.", e);
                 continue;
             }
             // Execute the request.
             HttpResponse response = null;
             final long requestStartTime = System.currentTimeMillis();
             try {
                 response = client.execute(request);
             } catch (IOException e) {
                 Log.w(SERVICE_NAME, e.getMessage());
                 break;
             }
             final long requestEndTime = System.currentTimeMillis();
             // Check the status code.
             final StatusLine statusLine = response.getStatusLine();
             Log.d(SERVICE_NAME, String.format("statusLine: %s (%sms)",
                     statusLine,
                     requestEndTime - requestStartTime));
             if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Log.d(SERVICE_NAME, "markAsSynced");
                 ScanResultTracker.markAsSynced(this, scanResults);
                 syncedResultCount += scanResults.size();
             }
             // Move forward.
             skip += PAGE_SIZE;
         }
        final long syncTime = System.currentTimeMillis() - syncStartTime;
        Log.i(SERVICE_NAME, String.format("Uploaded %s results in %sms (%ms per result)",
                syncedResultCount,
                syncTime,
                syncTime / syncedResultCount));
     }
 }
