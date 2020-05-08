 package com.lauchenauer.nextbusperth.app.prefs;
 
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.OverlayItem;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.lauchenauer.nextbusperth.R;
 import com.lauchenauer.nextbusperth.app.prefs.map.MapStop;
 import com.lauchenauer.nextbusperth.app.prefs.map.NBMapView;
 import com.lauchenauer.nextbusperth.app.prefs.map.OnMapViewChangedListener;
 import com.lauchenauer.nextbusperth.app.prefs.map.StopItemOverlay;
 import com.lauchenauer.nextbusperth.helper.StopsHelper;
 
 public class StopSelectorActivity extends MapActivity implements OnMapViewChangedListener {
     private static final int ZOOM_LEVEL = 16;
 
     private StopItemOverlay overlay;
    private GeoPoint lastTopLeft = new GeoPoint(0, 0);
     private NBMapView mapView;
     private TextView zoomText;
     private ProgressBar progressBar;
     private MapStopsDownloadTask currentTask;
     private MapStopsDownloadTask nextTask;
 
     @Override
     protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.stop_selector);
 
         zoomText = (TextView) findViewById(R.id.zoom_text);
         progressBar = (ProgressBar) findViewById(R.id.progressBar);
 
         Bundle extras = getIntent().getExtras();
 
         mapView = (NBMapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapView.getController().setCenter(new GeoPoint(extras.getInt("lat"), extras.getInt("lon")));
         mapView.getController().setZoom(extras.getInt("zoom"));
         mapView.setOnMapViewChangedListener(this);
 
         Drawable bus_stop = this.getResources().getDrawable(R.drawable.green_pin_32);
         overlay = new StopItemOverlay(bus_stop, this);
         mapView.getOverlays().add(overlay);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     public void onMapViewChanged(int zoomlevel, GeoPoint topLeft, GeoPoint bottomRight) {
         Log.d("[StopSelectorActivity.onMapViewChanged]", "Zoom: " + zoomlevel + "   TL: " + topLeft.toString() + "   BR: " + bottomRight.toString());
 
        boolean samePosition = lastTopLeft.getLatitudeE6() == topLeft.getLatitudeE6() && lastTopLeft.getLongitudeE6() == topLeft.getLongitudeE6();
        if (samePosition) return;
        lastTopLeft = topLeft;
 
         if (zoomlevel >= ZOOM_LEVEL) {
             zoomText.setVisibility(View.INVISIBLE);
 
             if (currentTask == null) {
                 progressBar.setVisibility(View.VISIBLE);
                 currentTask = new MapStopsDownloadTask(topLeft, bottomRight);
                 currentTask.execute();
             } else {
                 nextTask = new MapStopsDownloadTask(topLeft, bottomRight);
             }
 
         } else {
             zoomText.setVisibility(View.VISIBLE);
             updateOverlays(new ArrayList<MapStop>());
         }
     }
 
     private void updateOverlays(List<MapStop> stops) {
         List<OverlayItem> items = new ArrayList<OverlayItem>(stops.size());
         for (MapStop stop : stops) {
             OverlayItem item = new OverlayItem(new GeoPoint((int) (stop.getLatitude() * 1000000), (int) (stop.getLongitude() * 1000000)), stop.getStopNumber(), stop.getStopName());
             items.add(item);
         }
 
         overlay.clearOverlays();
 
         // only add items if zoom level is correct (in case it is called out of order by a task completeing after a zoom out event
         if (mapView.getZoomLevel() >= ZOOM_LEVEL) {
             overlay.addOverlays(items);
         }
 
         progressBar.setVisibility(View.INVISIBLE);
     }
 
     private synchronized void taskDone() {
         currentTask = null;
 
         if (nextTask != null) {
             currentTask = nextTask;
             nextTask = null;
 
             progressBar.setVisibility(View.VISIBLE);
             currentTask.execute();
         }
     }
 
     private class MapStopsDownloadTask extends AsyncTask<Void, Void, List<MapStop>> {
         private GeoPoint topLeft;
         private GeoPoint bottomRight;
 
         private MapStopsDownloadTask(GeoPoint topLeft, GeoPoint bottomRight) {
             this.topLeft = topLeft;
             this.bottomRight = bottomRight;
         }
 
         @Override
         protected List<MapStop> doInBackground(Void... voids) {
             return StopsHelper.retrieveStops(topLeft, bottomRight);
         }
 
         @Override
         protected void onPostExecute(List<MapStop> mapStops) {
             StopSelectorActivity.this.updateOverlays(mapStops);
             StopSelectorActivity.this.taskDone();
         }
     }
 }
