 package com.isawabird;
 
 import android.app.Application;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 import com.isawabird.MyLocation.LocationResult;
 import com.isawabird.parse.ParseConsts;
 import com.isawabird.parse.ParseUtils;
 import com.parse.Parse;
 import com.parse.ParseGeoPoint;
 
 public class ISawABirdApplication extends Application {
 
 	@Override
 	public void onCreate() {
 		Parse.initialize(this, ParseConsts.APP_ID, ParseConsts.CLIENT_KEY);
 		
 		/* Initialize with last known location */ 
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		Location lastKnown = null;
 		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 			lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		}else if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 			lastKnown = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (lastKnown != null){
 			ParseUtils.location = new ParseGeoPoint(lastKnown.getLatitude(), lastKnown.getLongitude());
 		}
 		
 		MyLocation myLocation = new MyLocation();
 		LocationResult locationResult = new LocationResult(){
 		    @Override
 		    public void gotLocation(Location location){
 		        Log.i(Consts.TAG, "Lat , long are " + location.getLatitude() + " " + location.getLongitude());
 		        ParseUtils.location = new ParseGeoPoint(location.getLatitude(), location.getLongitude()); 
 		    }
 		};
 		myLocation.getLocation(this, locationResult);
 
 		super.onCreate();
 	}
 }
