 /**
  * TerrainGIS 
  * Android program for mapping
  * 
  * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.kalcik.vojta.terraingis.location;
 
 import java.io.Serializable;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 import cz.kalcik.vojta.terraingis.MainActivity;
 import cz.kalcik.vojta.terraingis.R;
 import cz.kalcik.vojta.terraingis.components.Settings;
 import cz.kalcik.vojta.terraingis.fragments.MapFragment;
 import cz.kalcik.vojta.terraingis.fragments.SettingsFragment;
 
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.GpsStatus;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 
 /**
  * class for manage GPS and other location services
  * @author jules
  *
  */
 public class LocationWorker implements LocationListener
 {
     // constants =====================================================================
     
     private enum ProviderType {GPS, BOTH};
     // data =========================================================================
     public static class LocationWorkerData implements Serializable
     {
         private static final long serialVersionUID = 1L;
         public boolean runLocation;
 
         public LocationWorkerData(boolean runLocation, int currentMindist)
         {
             this.runLocation = runLocation;
         }
     }
     
     // attributes ====================================================================
     private MainActivity mMainActivity;
     private MapFragment mMapFragment;
     private SharedPreferences mSharedPref;
     private Coordinate locationPoint = new Coordinate();
     private CommonLocationListener mCommon;
     private GPSStatusListener mGPSStatusListener = new GPSStatusListener();
     private ProviderType currentProvider;
     private LocationWorkerData data = new LocationWorkerData(false, Settings.LOCATION_MINDIST_DEFAULT);
     private boolean hasGPS;
     private long mLastGPSMillis = 0;
     
     /**
      * constructor
      * @param context
      * @param map
      */
     public LocationWorker(MainActivity mainActivity)
     {
         mMainActivity = mainActivity;
         mCommon = new CommonLocationListener(mMainActivity);
         mMapFragment = mMainActivity.getMapFragment();
         hasGPS = mCommon.hasGPSDevice();
         PreferenceManager.setDefaultValues(mMainActivity, R.xml.settings, false);
         mSharedPref = PreferenceManager.getDefaultSharedPreferences(mMainActivity);
     }
     
     // public methods ================================================================
     /**
      * start location service
      * @return
      */
     public void start()
     {
         startLocation();
         data.runLocation = true;
     }
     
     /**
      * stop location service
      */
     public void stop()
     {
         data.runLocation = false;
         stopLocation();
     }
     
     /**
      * resume location service
      */
     public void resume()
     {
         if(data.runLocation)
         {
             startLocation();
         }
     }   
  
     /**
      * pause location service
      */
     public void pause()
     {
         if(data.runLocation)
         {
             stopLocation();
         }
     }
 
     /**
      * @return if device has GPS
      */
     public boolean hasGPSDevice()
     {
         return hasGPS;
     }
     // getter setter =================================================================
     
     public LocationWorkerData getData()
     {
         return data;
     }
     
     public void setData(Serializable data)
     {
         this.data = (LocationWorkerData)data;
     }
     
     public boolean isRunLocation()
     {
         return data.runLocation;
     }
     
     // on methods ===================================================================
     
     /**
      * receive new location update
      */
     public void onLocationChanged(Location location)
     {
         synchronized(this)
         {
             // switch to GPS
             if(location.getProvider().equals(LocationManager.GPS_PROVIDER))
             {
                 mLastGPSMillis = SystemClock.elapsedRealtime();
                 
                 if(currentProvider == ProviderType.BOTH)
                 {
                     currentProvider = ProviderType.GPS;
                     switchProvider();
                 }
             }
         }
 
         locationPoint.x = location.getLongitude();
         locationPoint.y = location.getLatitude();
         locationPoint.z = location.getAltitude();
         
         mMapFragment.setLonLatLocation(locationPoint);
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras)
     {
 
     }
 
     public void onProviderEnabled(String provider)
     {
 
     }
 
     public void onProviderDisabled(String provider)
     {
         
     }
     
     // private methods ===============================================================
     /**
      * start GPS listen
      */
     private void runGPS()
     {
         if(hasGPS)
         {
             // default
             mCommon.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                     getMinTime(), Settings.LOCATION_MINDIST_DEFAULT, this);
             mCommon.locationManager.addGpsStatusListener(mGPSStatusListener);
         }        
     }
     
     /**
      * start Network listen
      */
     private void runNetwork()
     {
         // default
        if (mCommon.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            mCommon.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                 getMinTime(), Settings.LOCATION_MINDIST_DEFAULT, this);
        }
     }
 
     /**
      * stop location service
      */
     private void startLocation()
     {
         currentProvider = ProviderType.BOTH;
         runGPS();
         runNetwork();
     }
     
     /**
      * stop location service
      */
     private void stopLocation()
     {
         if(hasGPS)
         {
             mCommon.locationManager.removeGpsStatusListener(mGPSStatusListener);
         }
         
         mCommon.locationManager.removeUpdates(this);
     }
     
     /**
      * switch to only GPS or both
      * @param providerType
      */
     private void switchProvider()
     {
         stopLocation();
 
         runGPS();
         
         if(currentProvider == ProviderType.BOTH)
         {
             runNetwork();
         }
     }
     
     /**
      * @return mintime of GPS
      */
     private int getMinTime()
     {
         return 1000 * SettingsFragment.getIntShareSettings(mSharedPref, SettingsFragment.KEY_GPS_MINTIME);
     }
     // classes =======================================================================
     
     /**
      * listener for GPS status
      * @author jules
      *
      */
     private class GPSStatusListener implements GpsStatus.Listener
     {
         private boolean isGPSFix = false;
         
         public synchronized void onGpsStatusChanged(int event)
         {
             if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
             {
                 if (mLastGPSMillis != 0)
                 {
                     isGPSFix = (SystemClock.elapsedRealtime() - mLastGPSMillis) < 
                              getMinTime() * 2;
                 }
             }
             else if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
             {
                 isGPSFix = true;
             }
             
             ProviderType previousType = currentProvider;
             currentProvider = isGPSFix ? ProviderType.GPS : ProviderType.BOTH;
             
             if(previousType != currentProvider)
             {
                 switchProvider();
             }
         }
     }
 }
