 package com.pq2.coffeenearby;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.PictureDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import com.google.android.maps.*;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 import java.util.ArrayList;
 
 public class CoffeeNearbyActivity extends MapActivity implements View.OnClickListener, LocationListener {
     private final String TAG = "COFFEENEARBYACTIVITY";
 
     private Handler mHandler;
     private final static int MSG_GPS_TIMEOUT = 0;
 
     private ProgressDialog mSearching;
     private ProgressDialog mLocating;
 
     private MapView mMapView;
     private LocationOverlay locationOverlay;
     private CoffeeOverlay coffeeOverlay;
 
     private Button mBtnRefreshCoffee;
     private Button mBtnRefreshLocation;
     private Button mBtnGps;
     private LocationManager mLocationManager;
     private final Place mCurrentLocation = new Place(45.52, -122.681944, "You are here", "");
     private Place[] coffeeLocations;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mHandler = new Handler(){
             @Override
             public void handleMessage(Message msg) {
                 if(msg.what == MSG_GPS_TIMEOUT){
                     CoffeeNearbyActivity.this.handleGpsTimeout();
                 }
             }
         };
 
         //Register for location
         mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         //Refresh Coffee button
         mBtnRefreshCoffee = ((Button) findViewById(R.id.refresh_coffee));
         mBtnRefreshCoffee.setOnClickListener(this);
 
 
         //Refresh location button
         mBtnRefreshLocation = ((Button) findViewById(R.id.refresh_location));
         mBtnRefreshLocation.setOnClickListener(this);
 
 
         //GPS button
         mBtnGps = ((Button) findViewById(R.id.gps_button));
         mBtnGps.setOnClickListener(this);
 
 //        mBtnRefresh.setBackgroundDrawable(SVGParser.getSVGFromResource(getResources(), R.raw.noun_project_16).createPictureDrawable());
         mMapView = (MapView) findViewById(R.id.mapview);
 //        mMapView.setOnClickListener(this);
         mMapView.getController().setZoom(17);
         mMapView.getController().setCenter(mCurrentLocation.toGeoPoint());
 
 //        Drawable markerDrawable = SVGParser.getSVGFromResource(getResources(), R.raw.noun_project_462).createPictureDrawable();
 //        Drawable coffeeDrawable = SVGParser.getSVGFromResource(getResources(), R.raw.noun_project_16).createPictureDrawable();
 
         Drawable markerDrawable = getResources().getDrawable(R.drawable.marker);
         Drawable coffeeDrawable = getResources().getDrawable(R.drawable.coffee);
 
 
         locationOverlay = new LocationOverlay(markerDrawable, mCurrentLocation.toGeoPoint(), "You are here");
         coffeeOverlay = new CoffeeOverlay(coffeeDrawable, this);
 
         mMapView.getOverlays().add(locationOverlay);
 
         this.requestLocationUpdate();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             mBtnGps.setVisibility(View.GONE);
         } else {
             mBtnGps.setVisibility(View.VISIBLE);
         }
 
     }
 
     protected void requestLocationUpdate() {
         if(mLocating != null && mLocating.isShowing()) return;
         mLocating = ProgressDialog.show(this, "Finding location...", "Please wait", true);
         if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
             mHandler.sendEmptyMessageDelayed(MSG_GPS_TIMEOUT, 10000);
         } else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
             mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
         }else{
              this.alertNoLocation();
         }
     }
 
     protected void handleGpsTimeout(){
         Log.v(TAG, "GPS timed out, falling over to network");
         mLocationManager.removeUpdates(this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
     }
 
     protected void alertNoLocation(){
         mLocating.dismiss();
        Toast.makeText(this, "Unable to find location", 3);
     }
 
 
     protected void updateSearch() {
         if (mSearching != null && mSearching.isShowing()) return;
         mSearching = ProgressDialog.show(this, "Searching...", "Please wait", true);
         new UpdateSearchTask().execute();
 
     }
 
     protected void updateSearchCallback(Place[] places) {
         coffeeLocations = places;
         Log.v(TAG, coffeeLocations.length + " locations found");
         updateCoffeeMarkers();
         mSearching.dismiss();
     }
 
     protected void updateCoffeeMarkers() {
         coffeeOverlay.clearAll();
         for (int i = 0; i < coffeeLocations.length; i++) {
             coffeeOverlay.addOverlay(new OverlayItem(coffeeLocations[i].toGeoPoint(), coffeeLocations[i].getName(), coffeeLocations[i].getAddress()));
             mMapView.invalidate();
         }
         //We have to add this only once we have items, otherwise a tap crashes the app
         if (!mMapView.getOverlays().contains(coffeeOverlay)) mMapView.getOverlays().add(coffeeOverlay);
     }
 
     protected void updateLocationMarker() {
 
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.gps_button:
                 Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                 startActivity(intent);
                 break;
             case R.id.refresh_coffee:
                 this.updateSearch();
                 break;
             case R.id.refresh_location:
                 this.requestLocationUpdate();
                 break;
             case R.id.mapview:
                 break;
         }
     }
 
     @Override
     public void onLocationChanged(Location location) {
         //TODO:Update running average of location and replace marker
         Log.v(TAG, "Location updated " + location.getLatitude() + ", " + location.getLongitude());
         mCurrentLocation.setLatitude(location.getLatitude());
         mCurrentLocation.setLongitude(location.getLongitude());
 
         mLocating.dismiss();
 
         if (coffeeLocations == null) this.updateSearch();
 
         //Update overlays
         locationOverlay.updateLocation(mCurrentLocation.toGeoPoint());
 
         //Zoom to location
         mMapView.getController().animateTo(mCurrentLocation.toGeoPoint());
 
         mMapView.postInvalidate();
 
         //TODO:Make it an option to update constantly
         mLocationManager.removeUpdates(this);
 
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
 
     }
 
     @Override
     public void onProviderEnabled(String s) {
         if (s.equals(LocationManager.GPS_PROVIDER)) {
             mBtnGps.setVisibility(View.GONE);
         }
     }
 
     @Override
     public void onProviderDisabled(String s) {
         if (s.equals(LocationManager.GPS_PROVIDER)) {
             mBtnGps.setVisibility(View.VISIBLE);
         }
     }
 
     protected class LocationOverlay extends ItemizedOverlay<OverlayItem> {
 
         private OverlayItem location;
 
         public LocationOverlay(Drawable defaultMarker, GeoPoint point, String title) {
             super(boundCenterBottom(defaultMarker));
             // TODO Auto-generated constructor stub
             location = new OverlayItem(point, title, "");
             populate();
         }
 
         public void updateLocation(GeoPoint point) {
             location = new OverlayItem(point, "You are here", "");
             populate();
         }
 
         @Override
         protected OverlayItem createItem(int i) {
             // TODO Auto-generated method stub
             return location;
         }
 
         @Override
         public int size() {
             // TODO Auto-generated method stub
             return 1;
         }
 
     }
 
     protected class CoffeeOverlay extends ItemizedOverlay<OverlayItem> {
 
         private ArrayList<OverlayItem> coffeeOverlayItems = new ArrayList<OverlayItem>(20);
         private Context mContext;
         private Toast lastToast;
 
         @Override
         protected boolean onTap(int i) {
             if (lastToast != null) lastToast.cancel();
             lastToast = Toast.makeText(mContext, coffeeOverlayItems.get(i).getTitle(), 3);
             lastToast.show();
             return true;
         }
 
         public CoffeeOverlay(Drawable defaultMarker, Context context) {
             super(boundCenterBottom(defaultMarker));
             this.mContext = context;
         }
 
         public void addOverlay(OverlayItem item) {
             coffeeOverlayItems.add(item);
             populate();
         }
 
         @Override
         public OverlayItem createItem(int i) {
             return coffeeOverlayItems.get(i);
         }
 
         @Override
         public int size() {
             return coffeeOverlayItems.size();
         }
 
         public void clearAll() {
             coffeeOverlayItems.clear();
             populate();
         }
     }
 
     private class UpdateSearchTask extends AsyncTask<Integer, Integer, Place[]> {
 
         @Override
         protected Place[] doInBackground(Integer... Integers) {
             return PlacesApi.searchForPlaces(mCurrentLocation, "coffee");
         }
 
         @Override
         protected void onPostExecute(Place[] places) {
             CoffeeNearbyActivity.this.updateSearchCallback(places);
         }
     }
 }
