 package com.allen.mapple;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapActivity extends FragmentActivity {
     private GoogleMap mMap;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
         SupportMapFragment mapFragment = (SupportMapFragment)fragment;
         mMap = mapFragment.getMap();

         mMap.setOnMapClickListener(new OnMapClickListener() {
 
             @Override
             public void onMapClick(LatLng arg0) {
 
                 Marker tuti= mMap.addMarker(new MarkerOptions()
                         .position(new LatLng(arg0.latitude, arg0.longitude))
                         .title("SAVE"));
 
             }
         });
 
         mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 
             @Override
             public void onInfoWindowClick(Marker arg0) {
                 Log.d("tuti","neko");
 
                 LatLng ln = arg0.getPosition();
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
 
 }
