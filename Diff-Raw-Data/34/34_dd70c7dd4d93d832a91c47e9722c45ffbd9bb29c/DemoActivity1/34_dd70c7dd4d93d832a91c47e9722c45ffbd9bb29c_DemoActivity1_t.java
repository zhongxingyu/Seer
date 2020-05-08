 //Following example from https://github.com/moagrius/MapView/wiki/Example-Activity
 
 package com.austingore.tiledimageview;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 public class DemoActivity1 extends Activity
 {
     private MapView mapView;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         FrameLayout frame = new FrameLayout(this);
         setContentView(frame);
 
 //        int mapWidth = 341;
 //        int mapHeight = 512;
 
         mapView = new MapView(this);
         mapView.setBackgroundColor(0xFFF5F5F5);
         mapView.setTileTranstionEnabled(true);
         mapView.setTileTransitionDuration(400);
 //        mapView.setBaseMapSize(mapWidth, mapHeight);
 
         //add a tile set of images - first set to be added are the ones that will be used when all the way zoomed in on.
         //last tile set added will be the one used for most zoomed out view
 //        mapView.addZoomLevel("tiles/alexis_25_%col%_%row%.jpg", "downsamples/alexis.jpg");
         mapView.addZoomLevel(1109, 2048,"tiles/alexis/alexis_100_%col%_%row%.jpg", "downsamples/alexis.jpg");
         mapView.addZoomLevel(683, 1024,"tiles/alexis/alexis_50_%col%_%row%.jpg", "downsamples/alexis.jpg");
         mapView.addZoomLevel(341, 512,"tiles/alexis/alexis_25_%col%_%row%.jpg", "downsamples/alexis.jpg");
 
 
 
         mapView.setMarkerAnchorPoints(0.5f, 1.0f);
         mapView.registerGeolocator(42.379676, -71.040280, 42.346550, -71.094919);
         mapView.initialize();
 
 
         //adding the marker seems to work...
         ImageView marker = new ImageView(this);
         marker.setImageResource(R.drawable.ic_launcher);
         mapView.addMarker(marker, 42.35848, -71.063736);
 
 
         mapView.addOnReadyListener(new Runnable(){
             @Override
             public void run() {
                 mapView.moveToAndCenter(42.35848, -71.063736);
             }
 
 
         });
 
         FrameLayout.LayoutParams mapViewLayout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         frame.addView(mapView, mapViewLayout);
 
 }
 }
