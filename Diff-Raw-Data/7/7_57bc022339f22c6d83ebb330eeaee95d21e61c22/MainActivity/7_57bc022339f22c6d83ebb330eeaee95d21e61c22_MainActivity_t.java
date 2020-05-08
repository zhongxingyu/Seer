 package info.eigenein.openwifi.activities;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import com.google.android.maps.*;
 import info.eigenein.openwifi.R;
 import info.eigenein.openwifi.helpers.LocationProcessor;
 import info.eigenein.openwifi.helpers.ScanResultTracker;
 import info.eigenein.openwifi.helpers.ScanServiceManager;
 import info.eigenein.openwifi.helpers.entities.Area;
 import info.eigenein.openwifi.helpers.entities.Cluster;
 import info.eigenein.openwifi.helpers.entities.ClusterList;
 import info.eigenein.openwifi.helpers.entities.Network;
 import info.eigenein.openwifi.helpers.location.L;
 import info.eigenein.openwifi.helpers.map.ClusterListOverlay;
 import info.eigenein.openwifi.helpers.map.ClusterOverlay;
 import info.eigenein.openwifi.helpers.map.MapViewListener;
 import info.eigenein.openwifi.helpers.map.TrackableMapView;
 import info.eigenein.openwifi.persistency.entities.StoredLocation;
 import info.eigenein.openwifi.persistency.entities.StoredScanResult;
 import org.apache.commons.collections.map.MultiKeyMap;
 
 import java.util.*;
 
 /**
  * Main application activity with the map.
  */
 public class MainActivity extends MapActivity {
     private static final String LOG_TAG = MainActivity.class.getCanonicalName();
 
     private final static int DEFAULT_ZOOM = 17;
 
     private TrackableMapView mapView = null;
     private MyLocationOverlay myLocationOverlay = null;
     private ClusterListOverlay clusterListOverlay = null;
 
     private RefreshScanResultsAsyncTask refreshScanResultsAsyncTask = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         // Setup action bar.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             getActionBar().setDisplayShowTitleEnabled(false);
         }
 
         // Setup map.
         mapView = (TrackableMapView)findViewById(R.id.map_view);
         mapView.setBuiltInZoomControls(true);
         mapView.addMovedOrZoomedObserver(new MapViewListener() {
             @Override
             public void onMovedOrZoomed() {
                 Log.d(LOG_TAG, "mapView onMovedOrZoomed");
                 startRefreshingScanResultsOnMap();
             }
         });
         // Setup map controller.
         final MapController mapController = mapView.getController();
         // Setup current location.
         myLocationOverlay = new MyLocationOverlay(this, mapView);
         myLocationOverlay.runOnFirstFix(new Runnable() {
             public void run() {
                 // Zoom in to current location
                 mapController.setZoom(DEFAULT_ZOOM);
                 mapController.animateTo(myLocationOverlay.getMyLocation());
             }
         });
         // Setup overlays.
         final List<Overlay> overlays = mapView.getOverlays();
         overlays.add(myLocationOverlay);
         clusterListOverlay = new ClusterListOverlay();
         overlays.add(clusterListOverlay);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         boolean isServiceStarted = ScanServiceManager.isStarted(this);
         menu.findItem(R.id.start_scan_menuitem).setVisible(!isServiceStarted);
         menu.findItem(R.id.pause_scan_menuitem).setVisible(isServiceStarted);
 
         return true;
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         // Initialize my location.
         if (myLocationOverlay != null) {
             // Enable my location.
             myLocationOverlay.enableMyLocation();
             myLocationOverlay.enableCompass();
         }
         // Update overlays.
         startRefreshingScanResultsOnMap();
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         if (myLocationOverlay != null) {
             // Disable my location to avoid using of location services.
             myLocationOverlay.disableCompass();
             myLocationOverlay.disableMyLocation();
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.settings_menuitem:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.start_scan_menuitem:
                 ScanServiceManager.restart(this);
                 Toast.makeText(this, R.string.scan_started, Toast.LENGTH_LONG).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.pause_scan_menuitem:
                 ScanServiceManager.stop(this);
                 Toast.makeText(this, R.string.scan_paused, Toast.LENGTH_SHORT).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.statistics_menuitem:
                 startActivity(new Intent(this, StatisticsActivity.class));
                 return true;
             case R.id.show_my_location_menuitem:
                GeoPoint myLocation = myLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    mapView.getController().animateTo(myLocation);
                } else {
                    Toast.makeText(this, R.string.my_location_is_unavailable, Toast.LENGTH_SHORT).show();
                }
                 return true;
             case R.id.map_view_menuitem:
                 final CharSequence[] items = getResources().getTextArray(R.array.map_views);
                 new AlertDialog.Builder(this)
                         .setTitle(getString(R.string.map_view))
                         .setItems(items, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int item) {
                                 switch (item) {
                                     case 0:
                                         mapView.setSatellite(false);
                                         break;
                                     case 1:
                                         mapView.setSatellite(true);
                                         break;
                                 }
                             }
                         })
                         .show();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void invalidateOptionsMenu() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Added in API level 11
             super.invalidateOptionsMenu();
         }
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     /**
      * Refreshes the scan results on the map.
      */
     private void startRefreshingScanResultsOnMap() {
         Log.d(LOG_TAG, "startRefreshingScanResultsOnMap");
 
         // Check if the task is already running.
        cancelRefreshScanResultsAsyncTask();
 
         // Check map bounds.
         if (mapView.getLatitudeSpan() == 0 || mapView.getLongitudeSpan() == 0) {
             Log.w(LOG_TAG, "Zero mapView span.");
             return;
         }
         // Get map bounds.
         final Projection mapViewProjection = mapView.getProjection();
         GeoPoint nwGeoPoint = mapViewProjection.fromPixels(0, 0);
         GeoPoint seGeoPoint = mapViewProjection.fromPixels(mapView.getWidth(), mapView.getHeight());
         // Run task to retrieve the scan results and process them into a cluster list.
         refreshScanResultsAsyncTask = new RefreshScanResultsAsyncTask(
                 L.fromE6(seGeoPoint.getLatitudeE6()),
                 L.fromE6(nwGeoPoint.getLongitudeE6()),
                 L.fromE6(nwGeoPoint.getLatitudeE6()),
                 L.fromE6(seGeoPoint.getLongitudeE6()),
                 0.0005 * Math.pow(2.0, 20.0 - mapView.getZoomLevel())
         );
         refreshScanResultsAsyncTask.execute();
     }
 
     private synchronized void cancelRefreshScanResultsAsyncTask() {
         if (refreshScanResultsAsyncTask != null) {
             // Cancel old task.
             refreshScanResultsAsyncTask.cancel(true);
             refreshScanResultsAsyncTask = null;
         }
     }
 
     /**
      * Used to aggregate the scan results from the application database.
      */
     public class RefreshScanResultsAsyncTask extends AsyncTask<Void, Void, ClusterList> {
         private final String LOG_TAG = RefreshScanResultsAsyncTask.class.getCanonicalName();
 
         /**
          * Defines a "border" for selecting scan results within the specified area.
          * Without this border a cluster "jumps" when one of its scan results
          * goes off the visible area.
          */
         private static final double BORDER_WIDTH = 0.002;
 
         private final double minLatitude;
 
         private final double minLongitude;
 
         private final double maxLatitude;
 
         private final double maxLongitude;
 
         private final double gridSize;
 
         /**
          * Groups scan results into the grid by their location.
          * (int, int) -> StoredScanResult
          */
         private final MultiKeyMap cellToScanResultCache = new MultiKeyMap();
 
         public RefreshScanResultsAsyncTask(
                 double minLatitude,
                 double minLongitude,
                 double maxLatitude,
                 double maxLongitude,
                 double gridSize) {
             Log.d(LOG_TAG, String.format(
                     "RefreshScanResultsAsyncTask[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s, gridSize=%s]",
                     minLatitude,
                     minLongitude,
                     maxLatitude,
                     maxLongitude,
                     gridSize));
             this.minLatitude = minLatitude;
             this.minLongitude = minLongitude;
             this.maxLatitude = maxLatitude;
             this.maxLongitude = maxLongitude;
             this.gridSize = gridSize;
         }
 
         @Override
         protected ClusterList doInBackground(Void... params) {
             Log.d(LOG_TAG, "doInBackground");
             // Retrieve scan results.
             List<StoredScanResult> scanResults = ScanResultTracker.getScanResults(
                     MainActivity.this,
                     minLatitude - BORDER_WIDTH,
                     minLongitude - BORDER_WIDTH,
                     maxLatitude + BORDER_WIDTH,
                     maxLongitude + BORDER_WIDTH
             );
             Log.d(LOG_TAG, "scanResults.size() " + scanResults.size());
             // Process them.
             for (StoredScanResult scanResult : scanResults) {
                 // Check if we're cancelled.
                 if (isCancelled()) {
                     return null;
                 }
                 addScanResult(scanResult);
             }
             return buildClusterList();
         }
 
         @Override
         protected synchronized void onPostExecute(ClusterList clusterList) {
             Log.d(LOG_TAG, "onPostExecute " + clusterList);
 
             clusterListOverlay.clearClusterOverlays();
             for (Cluster cluster : clusterList) {
                 ClusterOverlay clusterOverlay = new ClusterOverlay(
                         MainActivity.this,
                         cluster
                 );
                 clusterListOverlay.addClusterOverlay(clusterOverlay);
             }
             mapView.invalidate();
         }
 
         @Override
         protected void onCancelled(ClusterList result) {
             Log.d(LOG_TAG, "onCancelled");
         }
 
         private void addScanResult(StoredScanResult scanResult) {
             final StoredLocation location = scanResult.getLocation();
 
             int key1 = (int)Math.floor(location.getLatitude() / gridSize);
             int key2 = (int)Math.floor(location.getLongitude() / gridSize);
 
             List<StoredScanResult> subCache = (List<StoredScanResult>)cellToScanResultCache.get(key1, key2);
             if (subCache == null) {
                 subCache = new ArrayList<StoredScanResult>();
                 cellToScanResultCache.put(key1, key2, subCache);
             }
 
             subCache.add(scanResult);
         }
 
         private ClusterList buildClusterList() {
             ClusterList clusterList = new ClusterList();
 
             // Iterate through grid cells.
             for (Object o : cellToScanResultCache.values()) {
                 // Check if we're cancelled.
                 if (isCancelled()) {
                     return null;
                 }
 
                 List<StoredScanResult> subCache = (List<StoredScanResult>)o;
                 HashMap<String, HashSet<String>> ssidToBssidCache = new HashMap<String, HashSet<String>>();
 
                 LocationProcessor locationProcessor = new LocationProcessor();
                 for (StoredScanResult scanResult : subCache) {
                     // Check if we're cancelled.
                     if (isCancelled()) {
                         return null;
                     }
                     // Combine BSSIDs from the same SSIDs.
                     HashSet<String> bssids = ssidToBssidCache.get(scanResult.getSsid());
                     if (bssids == null) {
                         bssids = new HashSet<String>();
                         ssidToBssidCache.put(scanResult.getSsid(), bssids);
                     }
                     bssids.add(scanResult.getBssid());
                     // Track the location.
                     locationProcessor.add(scanResult.getLocation());
                 }
 
                 // Initialize a cluster.
                 Area area = locationProcessor.getArea();
                 Cluster cluster = new Cluster(area);
                 // And fill it with networks.
                 for (Map.Entry<String, HashSet<String>> entry : ssidToBssidCache.entrySet()) {
                     cluster.add(new Network(entry.getKey(), entry.getValue()));
                 }
                 // Finally, ass the cluster to the cluster list.
                 clusterList.add(cluster);
                 Log.d(LOG_TAG, "clusterList.add " + cluster);
             }
 
             return clusterList;
         }
     }
 }
