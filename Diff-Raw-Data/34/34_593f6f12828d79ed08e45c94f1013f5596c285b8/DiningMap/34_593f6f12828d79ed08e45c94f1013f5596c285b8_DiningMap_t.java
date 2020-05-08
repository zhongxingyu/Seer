 package org.vandymobile.dining;
 
import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 
 public class DiningMap extends MapActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dining_map_large);
         //getActionBar().setDisplayHomeAsUpEnabled(true);
        MapView this_map = (MapView) findViewById(R.id.mapview);
        this_map.setBuiltInZoomControls(true);
        MapController _mapViewController = this_map.getController();
        GeoPoint _geoPoint = new GeoPoint(36143091, -86804699); //This is roughly the center of Vanderbilt
        _mapViewController.animateTo(_geoPoint);
        _mapViewController.setZoom(17);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_dining_map, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
 
 }
