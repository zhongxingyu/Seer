 package net.sourcewalker.vfrmap;
 
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.tileprovider.MapTileProviderBasic;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 
 import android.app.Activity;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 public class VfrMapActivity extends Activity {
 
     private static final float RAD_TO_DEGREE = (float) (180 / Math.PI);
     private static final long WARN_LOCATION_AGE = 30000;
     private static final double METER_TO_FEET = 3.2808399;
    private static final double MS_TO_KMH = 3.6;
 
     private MapView mapView;
     private VfrTileSource tileSource;
     private PlaneOverlay locationOverlay;
     private LocationManager locationManager;
     private OwnLocationListener locationListener;
     private CompassManager compassManager;
     private TextView viewHeight;
     private TextView viewSpeed;
     private TextView viewHeading;
     private TextView viewAccuracy;
 
     /*
      * (non-Javadoc)
      * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
         viewHeight = (TextView) findViewById(R.id.data_height);
         viewSpeed = (TextView) findViewById(R.id.data_speed);
         viewHeading = (TextView) findViewById(R.id.data_heading);
         viewAccuracy = (TextView) findViewById(R.id.data_accuracy);
 
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         tileSource = new VfrTileSource();
         MapTileProviderBasic provider = new MapTileProviderBasic(this,
                 tileSource);
         mapView.setTileSource(provider.getTileSource());
         mapView.getController().setZoom(10);
         mapView.getController().setCenter(new GeoPoint(47, 10));
 
         locationOverlay = new PlaneOverlay(this, mapView);
         mapView.getOverlays().add(locationOverlay);
 
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         locationListener = new OwnLocationListener();
 
         compassManager = new CompassManager(this);
         compassManager.addUpdateListener(new CompassListener());
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onResume()
      */
     @Override
     protected void onResume() {
         super.onResume();
 
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                 1000, 10, locationListener);
         Location lastLocation = locationManager
                 .getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (lastLocation != null) {
             locationListener.onLocationChanged(lastLocation);
         }
 
         compassManager.resume();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onPause()
      */
     @Override
     protected void onPause() {
         compassManager.pause();
         locationManager.removeUpdates(locationListener);
 
         super.onPause();
     }
 
     private class OwnLocationListener implements LocationListener {
 
         private final String formatHeight;
         private final String formatSpeed;
         private final String formatAccuracy;
 
         public OwnLocationListener() {
             formatHeight = getString(R.string.format_height_ft);
             formatSpeed = getString(R.string.format_speed_kph);
             formatAccuracy = getString(R.string.format_accuracy);
         }
 
         /*
          * (non-Javadoc)
          * @see
          * android.location.LocationListener#onLocationChanged(android.location
          * .Location)
          */
         @Override
         public void onLocationChanged(Location location) {
             IGeoPoint point = new GeoPoint(location.getLatitude(),
                     location.getLongitude());
             locationOverlay.setPlaneLocation(point);
 
             viewHeight.setText(String.format(formatHeight,
                     location.getAltitude() * METER_TO_FEET));
            viewSpeed.setText(String.format(formatSpeed, location.getSpeed()
                    * MS_TO_KMH));
             if (System.currentTimeMillis() - location.getTime() > WARN_LOCATION_AGE) {
                 viewAccuracy.setText(getString(R.string.data_accuracy_old));
             } else {
                 viewAccuracy.setText(String.format(formatAccuracy,
                         location.getAccuracy()));
             }
         }
 
         /*
          * (non-Javadoc)
          * @see
          * android.location.LocationListener#onProviderDisabled(java.lang.String
          * )
          */
         @Override
         public void onProviderDisabled(String provider) {
         }
 
         /*
          * (non-Javadoc)
          * @see
          * android.location.LocationListener#onProviderEnabled(java.lang.String)
          */
         @Override
         public void onProviderEnabled(String provider) {
         }
 
         /*
          * (non-Javadoc)
          * @see
          * android.location.LocationListener#onStatusChanged(java.lang.String,
          * int, android.os.Bundle)
          */
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
         }
 
     }
 
     private class CompassListener implements CompassManager.Listener {
 
         private final String formatHeading;
 
         public CompassListener() {
             this.formatHeading = getString(R.string.format_heading);
         }
 
         /*
          * (non-Javadoc)
          * @see
          * net.sourcewalker.vfrmap.CompassManager.Listener#onUpdateCompass(net
          * .sourcewalker.vfrmap.CompassManager, float, float, float)
          */
         @Override
         public void onUpdateCompass(CompassManager sender, float azimuth,
                 float pitch, float roll) {
             float heading = azimuth * RAD_TO_DEGREE;
             locationOverlay.setAzimuth(heading);
             viewHeading.setText(String.format(formatHeading, heading));
         }
 
     }
 
 }
