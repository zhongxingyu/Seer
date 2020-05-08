 package com.google.code.geobeagle.actions;
 
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.map.GeoMapActivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 
 /** Show the map, centered around the current location */
 public class MenuActionMap implements MenuAction {
     private final LocationControlBuffered mLocationControl;
     private final Activity mActivity;
     
     public MenuActionMap(Activity activity, 
             LocationControlBuffered locationControl)  {
         mActivity = activity;
         mLocationControl = locationControl;
     }
     
     @Override
     public void act() {
         Location location = mLocationControl.getLocation();
         final Intent intent = 
             new Intent(mActivity, GeoMapActivity.class);
        if (location != null) {
            intent.putExtra("latitude", (float)location.getLatitude());
            intent.putExtra("longitude", (float)location.getLongitude());
        }
         mActivity.startActivity(intent);
     }
 
     @Override
     public int getId() {
         return R.string.menu_map;
     }
 
 }
