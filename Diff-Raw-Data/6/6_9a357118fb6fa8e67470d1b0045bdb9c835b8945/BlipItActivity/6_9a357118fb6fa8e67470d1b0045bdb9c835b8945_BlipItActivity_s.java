 package com.thoughtworks.blipit.activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.thoughtworks.blipit.R;
 import com.thoughtworks.blipit.overlays.BalloonMyLocationOverlay;
 import com.thoughtworks.blipit.services.BlipNotificationService;
 import com.thoughtworks.blipit.utils.BlipItServiceHelper;
 import com.thoughtworks.blipit.utils.BlipItUtils;
 import com.thoughtworks.contract.BlipItSubscribeResource;
 
 // TODO: Handle other lifecycle events to correctly interact with BlipItNotificationService
 public class BlipItActivity extends MapActivity {
     private static final String TAG = "BlipItActivity";
 
     private MapView mapView;
     private BlipNotificationClientHandler blipNotificationClientHandler;
     private Messenger blipItNotificationService;
     private Messenger blipNotificationHandler;
     private BlipItSubscribeResource blipItResource;
     private BalloonMyLocationOverlay userLocationOverlay;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initMapView();
         initBlipNotifications();
     }
 
     private void initBlipNotifications() {
         blipNotificationClientHandler = new BlipNotificationClientHandler(this);
         bindService(new Intent(this, BlipNotificationService.class), blipNotificationClientHandler, BIND_AUTO_CREATE);
         blipNotificationHandler = new Messenger(blipNotificationClientHandler);
         blipItResource = new BlipItServiceHelper().getBlipItResource();
     }
 
     public void setBlipItNotificationService(Messenger blipItNotificationService) {
         this.blipItNotificationService = blipItNotificationService;
     }
 
     private void initMapView() {
         mapView = (MapView) this.findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         userLocationOverlay = new BalloonMyLocationOverlay(this, mapView);
         mapView.getOverlays().add(userLocationOverlay);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         userLocationOverlay.enableMyLocation();
         userLocationOverlay.enableCompass();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         userLocationOverlay.disableMyLocation();
         userLocationOverlay.disableCompass();
     }
 
     public Messenger getBlipNotificationHandler() {
         return blipNotificationHandler;
     }
 
     public void updateBlips(Bundle data) {
         Toast.makeText(this, R.string.blip_notification_received, Toast.LENGTH_SHORT).show();
     }
 
     public void sendUserLocationUpdate(GeoPoint geoPoint) {
         try {
             blipItNotificationService.send(BlipItUtils.getMessageWithGeoPoint(geoPoint, BlipItUtils.MSG_USER_LOCATION_UPDATED));
         } catch (RemoteException e) {
             Log.e(TAG, "An error occured while updating user location", e);
         }
     }
 }
