 /*
  *  Copyright (C) 2011 GSyC/LibreSoft, Universidad Rey Juan Carlos.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Raúl Román López <rroman@gsyc.es>
  *
  */
 
 package com.libresoft.apps.ARviewer.Location;
 
 import java.util.ArrayList;
 
 import com.libresoft.apps.ARviewer.Utils.GeoNames.AltitudeManager;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 public class ARLocationManager{
 	public static final int NO_LATLONG = -360;
 	
 	public static final int MODE_MANUAL = 2;
 	public static final int MODE_NETWORK = 1;
 	public static final int MODE_GPS = 0;
 	
 	private static ARLocationManager arLocationManager = null;
 	
 	private ArrayList<OnLocationUpdateListener> arrayLocationListener = null;
 	
 	private boolean ls_altitude = false;
 	private Location mLocation = null;
 	private LocationManager mLocationManager;
 	private MLocationListener mLocationListener = null;
 	private boolean isUpdateOn = false;
 	
 	// Location provider (GPS or network), by default: network
 	private String loc_provider;
 	
 	// Location Unit (seconds:1 or minutes:60), by default: seconds.
 	private Integer location_unit = null;
 	
 	// Location Periodic in seconds, by default 2 minutes.
 	private Integer location_period = null; 
 	
 	// Minimum distance of gps refresh
 	private Integer minimum_distance = null;
 	
 	private ARLocationManager(Context mContext){
 		mLocation = new Location("Manual");
 		mLocation.setLatitude(0);
 		mLocation.setLongitude(0);
 		mLocation.setAltitude(AltitudeManager.NO_ALTITUDE_VALUE);
 		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
 		mLocationListener = new MLocationListener();
 	}
 	
 	public static ARLocationManager getInstance(Context mContext){
 		if(arLocationManager == null)
 			arLocationManager = new ARLocationManager(mContext);
 		return arLocationManager;
 	}
 	
 	public Location getLastKnownLocation(Context mContext){
 		loadConfig(mContext);
 		if(mLocationManager != null){
			Location loc = mLocationManager.getLastKnownLocation(loc_provider);
			if(loc != null)
				mLocation = loc;
 		}
 		return mLocation;
 	}
 	
 	public Location getLocation(){
 		return mLocation;
 	}
 	
 	public void setLocation(Location location){
 		mLocation = location;
 	}
 	
 	public void setLocation(float latitude, float longitude, float altitude){
 		mLocation.setLatitude(latitude);
 		mLocation.setLongitude(longitude);
 		mLocation.setAltitude(altitude);
 	}
 	
 	public void resetLocation(){
 		arLocationManager = null;
 	}
 	
 	public void setLocationServiceAltitude(boolean ls_altitude){
 		this.ls_altitude = ls_altitude;
 	}
 	
 	public boolean isLocationServiceAltitude(){
 		return ls_altitude;
 	}
 	
 	private void loadConfig (Context mContext){
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
 		
 		if (sharedPreferences.getString(LocationPreferences.KEY_LOCATION_PROVIDERS, "network").equals("gps"))
 			loc_provider = LocationManager.GPS_PROVIDER;
 		else
 			loc_provider = LocationManager.NETWORK_PROVIDER;
 		
 		location_unit = Integer.parseInt(sharedPreferences.getString(LocationPreferences.KEY_LOCATION_UNITS, "60"));
 		location_period = sharedPreferences.getInt(LocationPreferences.KEY_LOCATION_PERIOD, 2);
 		minimum_distance = sharedPreferences.getInt(LocationPreferences.KEY_LOCATION_DISTANCE, 10);
 	}
 	
 	public void startUpdates(Context mContext){
 		loadConfig(mContext);
 		mLocationManager.requestLocationUpdates( loc_provider, 
 				 ((location_period)* 1000) * location_unit, 
 				 minimum_distance, 
 				 mLocationListener);
 	}
 	
 	public void pauseUpdates(){
 		if(mLocationManager != null)
 			mLocationManager.removeUpdates(mLocationListener);
 	}
 	
 	public void stopUpdates(){
 		pauseUpdates();
 		if(arrayLocationListener != null)
 			arrayLocationListener.clear();
 	}
 	
 	public boolean isUpdateOn(){
 		return isUpdateOn;
 	}
 	
 	private class MLocationListener implements LocationListener {
 
         public void onLocationChanged(Location loc) {
 
         	if (loc != null) {
     			
     			// Save the current location
 				mLocation = loc;
  
         		if ((arrayLocationListener != null) && (arrayLocationListener.size() > 0)){
         			// Execute all the listener
         			for (int i=0; i<arrayLocationListener.size(); i++)
         				arrayLocationListener.get(i).onUpdate(loc); 
         		}
             }
             
         }
         
         public void onProviderDisabled(String provider) {}
         
         public void onProviderEnabled(String provider) {}
         
         public void onStatusChanged(String provider, int status, 
             Bundle extras) {}
     }
 	
 	public int addLocationListener(OnLocationUpdateListener listener){
 		if(arrayLocationListener == null)
 			arrayLocationListener = new ArrayList<OnLocationUpdateListener>();
 		if(arrayLocationListener.indexOf(listener) == -1)
 			arrayLocationListener.add(listener);
 		return arrayLocationListener.indexOf(listener);
 	}
 	
 	public void removeLocationListener(int id){
 		if ((arrayLocationListener != null) && (arrayLocationListener.size() > id))
 			arrayLocationListener.remove(id);
 	}
 	
 	public interface OnLocationUpdateListener {
 		public abstract void onUpdate(Location loc);
 	}
 	
 }
