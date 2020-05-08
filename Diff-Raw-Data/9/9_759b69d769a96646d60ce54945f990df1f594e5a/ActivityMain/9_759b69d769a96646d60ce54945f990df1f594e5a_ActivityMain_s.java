 package com.RoboMobo;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 public class ActivityMain extends Activity
 {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         RMR.init(this);
         setContentView(R.layout.main);
 
         LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         LocationListener mlocListener = new GPSModule(getApplicationContext());
         mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
     }
 }
