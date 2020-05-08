 package fi.action.wpoint;
 
 import org.apache.http.HttpException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 
 public class WPointActivity extends MapActivity implements LocationListener {
 
    public static final int SCAN_INTERVAL_SECONDS = 10;
     public static final int SCAN_WHEN_MOVED_METERS = 10;
     
     public WifiManager        wifiManager;
     public Location           currentLocation;
     
     private MapView           mapView;
     private MyLocationOverlay myLocationOverlay;
     private LocationManager   locationManager;
     private BroadcastReceiver scanReceiver;
     private boolean           originalWifiState;
     private ProgressDialog    gettingLocationDialog;
 
     
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // UI components
         setContentView(R.layout.main);
         gettingLocationDialog = new ProgressDialog(this);
         gettingLocationDialog.setMessage(getResources().getString(R.string.waiting));
         gettingLocationDialog.show();
         
         // HACK: HTTP requests should really be run on separate threads
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
         
         // Wireless lan
         wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         originalWifiState = wifiManager.isWifiEnabled();
         if (scanReceiver == null) {
             scanReceiver = new ScanReceiver(this);
             registerReceiver(
                scanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
             );
         }
 
         // Map
         mapView = (MapView) findViewById(R.id.MapView);
         mapView.setBuiltInZoomControls(true);
         mapView.getController().setZoom(mapView.getMaxZoomLevel()-1);
         myLocationOverlay = new MyLocationOverlay(this, mapView);
         myLocationOverlay.enableMyLocation();
         mapView.getOverlays().add(myLocationOverlay);
         mapView.invalidate();
         
         // Location
         currentLocation = null;
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.layout.menu, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item)  {
         switch (item.getItemId()) {
             case R.id.scan:
                 Toast.makeText(this, R.string.scanning , Toast.LENGTH_LONG).show();
                 wifiManager.startScan();
                 return true;
             case R.id.exit:
                 finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void onResume() {
         super.onResume();
         if (!wifiManager.isWifiEnabled()) {
             wifiManager.setWifiEnabled(true);
         }
         locationManager.requestLocationUpdates(
             LocationManager.NETWORK_PROVIDER,
            SCAN_INTERVAL_SECONDS * 10000,
             SCAN_WHEN_MOVED_METERS,
             this
         );
 
         if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             showGPSDisabledAlertToUser();
         }
         else {
             locationManager.requestLocationUpdates(
                             LocationManager.GPS_PROVIDER,
                            SCAN_INTERVAL_SECONDS * 10000,
                             SCAN_WHEN_MOVED_METERS, 
                             this
              );
         }
     }
 
     public void onPause() {
         super.onPause();
         wifiManager.setWifiEnabled(originalWifiState);
         locationManager.removeUpdates(this);
     }
 
     public void onStop() {
         super.onStop();
         wifiManager.setWifiEnabled(originalWifiState);
         locationManager.removeUpdates(this);
         if (scanReceiver != null) {
             unregisterReceiver(scanReceiver);
             scanReceiver = null;
         }
     }
 
     private void showGPSDisabledAlertToUser() {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder
             .setTitle(R.string.dialog_title)
             .setMessage(R.string.dialog_gps_disabled)
             .setCancelable(false)
             .setPositiveButton(R.string.dialog_settings_button,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                         startActivity(callGPSSettingIntent);
                      }
                  }
             );
         alertDialogBuilder.setNegativeButton(R.string.dialog_ignore_button,
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                 }
             }
         );
         AlertDialog alert = alertDialogBuilder.create();
         alert.show();
     }
     
     public void onLocationChanged(Location location) {
         Log.d("WPoint", "Location changed to " + location.getLatitude() + ", " +
               location.getLongitude() + ".");
         
         gettingLocationDialog.hide();
         currentLocation = location;
 
         // Update user's current location on map
         showLocation(location);
         
         // Send request to API
         String response = null;
         try {
             response = HttpConnector.executeHttpGet(
                 "http://wpoint.herokuapp.com/api/v1/spots.json?" + 
                 "latitude=" + location.getLatitude() +
                 "&longitude=" + location.getLongitude()
             );
         }
         catch (HttpException e) {
             Toast.makeText(this, R.string.error_invalid_response + " " +  e.getMessage(),
                            Toast.LENGTH_LONG).show();
             e.printStackTrace();
             return;
         }
         catch (Exception e) {
             Toast.makeText(this, R.string.error_connecting_server, Toast.LENGTH_LONG).show();
             e.printStackTrace();
             return;
         }
 
         // Set icon for hotspots
         Drawable icon = getResources().getDrawable(R.drawable.blue_dot);
         HotspotsOverlay spotOverlays = new HotspotsOverlay(icon, this);
         
         // Parse the responsed JSON
         try {
             JSONArray array = new JSONArray(response);
             
             for (int i = 0; i < array.length(); i++) {
                 JSONObject hotspot = array.getJSONObject(i);
                 
                 if (!hotspot.getBoolean("open")) {
                     continue;
                 }
                 JSONObject coordinates = hotspot.getJSONObject("location");
                 String ssid            = hotspot.getString("ssid");
                 Double latitude        = coordinates.getDouble("lat");
                 Double longitude       = coordinates.getDouble("lng");
 
                 GeoPoint hotspotLocation = new GeoPoint(
                     (int)(latitude*1E6),
                     (int)(longitude*1E6)
                 );
                 OverlayItem overlayitem = new OverlayItem(
                     hotspotLocation,
                     ssid,
                     "signal: n dbm" // TODO
                 );
                 spotOverlays.add(overlayitem);
             }
             
             // Refresh map from the old hotspots
             if (!mapView.getOverlays().isEmpty()) {
                 mapView.getOverlays().clear();
                 mapView.getOverlays().add(myLocationOverlay);
             }
             
             mapView.getOverlays().add(spotOverlays);
             mapView.invalidate();
         }
         catch (JSONException e) {
             Toast.makeText(this, R.string.error_parsing_hotspots , Toast.LENGTH_LONG).show();
             e.printStackTrace();
             return;
         }
     }
 
     public void onProviderDisabled(String provider) {
     }
 
     public void onProviderEnabled(String provider) {
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 
     protected boolean isRouteDisplayed() {
         return false;
     }
     
     private void showLocation(Location location) {
         if (location == null) {
             return;
         }
         double currentLatitude = location.getLatitude();
         double currentLongitude = location.getLongitude();
         GeoPoint currentLocationGeoPoint = new GeoPoint(
             (int)(currentLatitude * 1E6),
             (int)(currentLongitude * 1E6)
         );
         mapView.getController().animateTo(currentLocationGeoPoint);
     }
 }
