 package org.apache.cordova.tickk;
 
import android.location.Location;

 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 
import android.util.Log;
 
 public class GeoListener implements LocationListener {

    public void onLocationChanged(Location location) {
        Log.d("[Cordova GeoService]", "GeoListener - onLocationChanged");
    }
     
 }
