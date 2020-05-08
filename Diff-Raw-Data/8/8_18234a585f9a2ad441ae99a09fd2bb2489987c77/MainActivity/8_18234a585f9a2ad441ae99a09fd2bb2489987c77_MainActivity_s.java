 /*
  * $ Id $
  * (c) Copyright 2009 Marcus Thiesen (marcus@thiesen.org)
  *
  *  This file is part of HHPT.
  *
  *  HHPT is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  HHPT is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with HHPT.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.thiesen.hhpt.ui.activity.main;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.thiesen.hhpt.geolookup.StationFinder;
 import org.thiesen.hhpt.geolookup.appengine.AppEngineStationFinder;
 import org.thiesen.hhpt.ui.activity.ConfigActivity;
 import org.thiesen.hhpt.ui.activity.R;
 import org.thiesen.hhpt.ui.map.TapListenerOverlay;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 public class MainActivity extends MapActivity {
 
     public final static String TAG = "HHPT";
 
     MapView _mapView;  
     private MapController _mc;  
     private final static int DEFAULT_ZOOM_VALUE = 17;
 
     public static final double DEFAULT_SEARCH_RADIUS_MILES = 1.0D;
 
     private static final int MENU_SHOW_POSITION = 1;
     private static final int MENU_CONFIG = 2;  
     private static final int MENU_UPDATE = 3;
     private static final int MENU_REFRESH = 4;
 
     MyLocationOverlay _myLocationOverlay;
 
     Bitmap _busBmp;
 
     Bitmap _trainBmp;
 
     Handler _uiThreadCallback;
 
     boolean _paused;
 
     private final LocationListener _listener =  new LocationListenerImpl(this);
 
     private LocationManager _locationManager; 
 
     StationFinder _finder;
 
     private final BlockingQueue<LookupPosition> _searchRequestQueue = new LinkedBlockingQueue<LookupPosition>();
 
     private boolean _locationListenerIsRegistered;
 
     @Override  
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         _finder = new AppEngineStationFinder();
 
         new SearcherThread( MainActivity.this, _searchRequestQueue ).start();
 
         _uiThreadCallback = new Handler();
 
         initViewMembers();  
         preloadIconBitmaps();
 
         _mapView.getOverlays().add( new TapListenerOverlay( this ) );
 
         _locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
 
         initMyLocationOverview();
 
         registerLocationListener();
 
         if ( !_locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
             buildAlertMessageNoGps();
         }         
 
         useLastKnownLocation( _locationManager );
         Log.v( TAG, "On create finished");
         
         //updateLocation( 53.250721,10.433192 );
         
     }
 
     private void preloadIconBitmaps() {
         _busBmp = BitmapFactory.decodeResource(getResources(),R.drawable.bus);
         _trainBmp = BitmapFactory.decodeResource(getResources(),R.drawable.train);
     }
 
     private void initViewMembers() {
         setContentView(R.layout.main);  
         _mapView = (MapView) findViewById(R.id.map);
         _mapView.setBuiltInZoomControls( true );
         _mc = _mapView.getController();
     }
 
     private void toggleLocationListener() {
         if ( _locationListenerIsRegistered ) {
             unregisterLocationListener();
         } else {
            _locationListenerIsRegistered = true;
             registerLocationListener();
         }
     }
 
     private void unregisterLocationListener() {
         _locationManager.removeUpdates( _listener );
     }
 
     private void registerLocationListener() {
         _locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 20, _listener );
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterLocationListener();
         _myLocationOverlay.disableMyLocation();
         _paused = true;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         _paused = false;
         _myLocationOverlay.enableMyLocation();
         updateLocation( _mapView.getMapCenter() );
     }
 
     @Override
     public boolean onCreateOptionsMenu( final Menu menu ) {
         menu.add(0, MENU_SHOW_POSITION, 0, "Show Position" ).setIcon( android.R.drawable.ic_menu_mylocation );
         menu.add(0, MENU_CONFIG, 0, "Configuration" ).setIcon( android.R.drawable.ic_menu_preferences );
         menu.add(0, MENU_UPDATE, 0, "Toggle GPS Updates" ).setIcon( android.R.drawable.ic_menu_compass );
         menu.add(0, MENU_REFRESH, 0, "Refresh" ).setIcon( android.R.drawable.ic_menu_search );
 
         return true;
     }
 
     /* Handles item selections */
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case MENU_SHOW_POSITION:
                 showPosition();
                 return true;
             case MENU_CONFIG: 
                 showConfig();
                 return true;
             case MENU_UPDATE:
                 toggleAutomaticLocationUpdateState();
                 return true;
             case MENU_REFRESH:
                  updateLocation( _mapView.getMapCenter() );
                  return true;
 
         }
         return false;
     }
 
     private void toggleAutomaticLocationUpdateState() {
         toggleLocationListener();
         
         final Context context = getApplicationContext();
         final CharSequence text =  _locationListenerIsRegistered ? "Location updates activated" : "Location updates deactivated";
         final int duration = Toast.LENGTH_SHORT;
 
         final Toast toast = Toast.makeText(context, text, duration);
         toast.show();
     }
 
 
     private void showConfig() {
         final Intent intend = new Intent( getApplicationContext(), ConfigActivity.class );
         startActivity( intend );
     }
 
     private void showPosition() {
         final GeoPoint myLocation = _myLocationOverlay.getMyLocation();
 
         if ( myLocation != null ) {
             updateLocation( myLocation );
             _mc.animateTo( myLocation );
         } else {
             showNoLocationDialog();
         }
     }
 
     private void useLastKnownLocation( final LocationManager manager ) {
         Location lastKnownLocation = manager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
         
         if ( lastKnownLocation == null ) {
             lastKnownLocation = manager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
         }
         
         if ( lastKnownLocation != null ) {
             updateLocation( lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude() );
             _mc.animateTo( new GeoPoint( (int)Math.round( lastKnownLocation.getLatitude() * 1E6 ), (int)Math.round( lastKnownLocation.getLongitude() * 1E6 ) ) );
         } else {
             updateLocation( _mapView.getMapCenter() );
             
             //showNoLocationDialog();
         }
     }
 
     private void showNoLocationDialog() {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         builder.setMessage( "No location" ).setPositiveButton( "OK", new DialogInterface.OnClickListener() {
 
             public void onClick( final DialogInterface dialog, @SuppressWarnings("unused") final int which ) {
                 dialog.cancel();
             }
         } );
 
         builder.show();
     }
 
     private void updateLocation( final GeoPoint myLocation ) {
         updateLocation( myLocation.getLatitudeE6() / 1E6D, myLocation.getLongitudeE6()  / 1E6D );   
     }
 
     public void getAndDisplayResultsFor( final GeoPoint currentCenter ) {
         getAndDisplayResultsFor(  currentCenter.getLatitudeE6() / 1E6D, currentCenter.getLongitudeE6()  / 1E6D );
     }
 
     void updateLocation( final double lat, final double lon ) {
         Log.d( TAG, "Starting updateLocation " + lat + ", " + lon );
 
         Log.d( TAG, "My Location: " + _myLocationOverlay.getMyLocation() );
         _mc.animateTo( new GeoPoint( (int) Math.round(lat * 1E6) , (int) Math.round(lon * 1E6) ));
         _mc.setZoom(DEFAULT_ZOOM_VALUE);  
 
 
         getAndDisplayResultsFor( lat, lon );
     }
 
 
 
     private void getAndDisplayResultsFor( final double lat, final double lon ) {
         if ( _paused ) return;
 
         _searchRequestQueue.add( new LookupPosition( lat, lon) );
 
     }
 
     private void launchGPSOptions() {
         final ComponentName toLaunch = new ComponentName("com.android.settings","com.android.settings.SecuritySettings");
         final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         intent.addCategory(Intent.CATEGORY_LAUNCHER);
         intent.setComponent(toLaunch);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivityForResult(intent, 0);
     }  
 
 
     private void buildAlertMessageNoGps() {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
         .setCancelable(false)
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                 launchGPSOptions(); 
             }
         })
         .setNegativeButton("No", new DialogInterface.OnClickListener() {
             public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                 dialog.cancel();
             }
         });
         final AlertDialog alert = builder.create();
         alert.show();
     }
 
 
     private void initMyLocationOverview() {
         _myLocationOverlay = new MyLocationOverlay(this, _mapView );
         _myLocationOverlay.enableMyLocation();
         _myLocationOverlay.runOnFirstFix(new Runnable() {
             public void run() {
                 updateLocation( _myLocationOverlay.getMyLocation() );
             }
 
 
         });
 
     } 
 
     void showException( final Exception e ) {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         builder.setMessage( "Fatal Error: " + e.getMessage() ).setPositiveButton( "OK", new DialogInterface.OnClickListener() {
 
             public void onClick( final DialogInterface dialog, @SuppressWarnings("unused")  final int which ) {
                 dialog.cancel();
             }
         } );
 
         builder.show();
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     boolean isPaused() {
         return _paused;
 
     }
 
 
 
 }
