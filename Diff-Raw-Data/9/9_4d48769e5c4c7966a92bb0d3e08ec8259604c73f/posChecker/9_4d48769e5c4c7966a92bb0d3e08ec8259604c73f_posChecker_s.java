 package com.example.htttppost;
 
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Handler;
 import android.util.FloatMath;
 import android.util.Log;
 
 public class posChecker extends Service{
 	private Handler handler = new Handler();
 	private double _lon;
 	private double _lat;
 	private LocationManager locationMgr;
 	private final double EARTH_RADIUS = 6378137.0;
 	
 	private final LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 		    _lat = location.getLatitude();
 		    _lon = location.getLongitude();
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub	
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub	
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub	
 		}
 	};
 
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     @Override
     public void onStart(Intent intent, int startId) {
     	handler.postDelayed(showTime, 1000);
     	
     	//Get LocationManager
     	LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     	Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
     	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
         super.onStart(intent, startId);
     }
  
     @Override
     public void onDestroy() {
         handler.removeCallbacks(showTime);
         super.onDestroy();
     }
     
     public double checkDis(double lat_a, double lng_a, double lat_b, double lng_b){
     	double radLat1 = (lat_a * Math.PI / 180.0);
     	double radLat2 = (lat_b * Math.PI / 180.0);
     	double a = radLat1 - radLat2;
     	double b = (lng_a - lng_b) * Math.PI / 180.0;
     	double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
     	+ Math.cos(radLat1) * Math.cos(radLat2)
     	* Math.pow(Math.sin(b / 2), 2)));
     	s = s * EARTH_RADIUS;
     	s = Math.round(s * 10000) / 10000;
     	return s;
     }
      
     private Runnable showTime = new Runnable() {
        public void run() {
    	   try{
        	   String provider = locationMgr.getBestProvider(new Criteria(), true);
        	   Location location = locationMgr.getLastKnownLocation(provider);
               _lon = location.getLongitude();
               _lat = location.getLatitude();
    	   }
    	   catch(Exception e) {
               e.printStackTrace();
    	   }
     	   //check distance
            Log.i("Distance:", ""+checkDis(30.0, -121.0, _lat, _lon)); 
            handler.postDelayed(this, 1000);
         }
     };
 }
