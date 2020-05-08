 package com.malcom.library.android.utils;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 import com.malcom.library.android.MCMDefines;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import static android.content.Context.LOCATION_SERVICE;
 
 /**
  * Created by PedroDuran on 02/07/13.
  */
 public class LocationUtils {
 
     private static final Boolean ENABLED = true;
 
     public static String getDeviceCityLocation(Context context) {
         String res = "";
         Location lastKnownLocation = getLocation(context);
 
         //lastKnowLocation could be null, so instead of throw an NPE return an empty String
         if ( lastKnownLocation == null )
             return res;
 
         try {
             Geocoder gcd = new Geocoder(context, Locale.getDefault());
             List<Address> addresses = gcd.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
             if (addresses.size() > 0) {
                 res = addresses.get(0).getLocality();
             }
         } catch (IOException e) {
             Log.e(MCMDefines.LOG_TAG,"Error getting city location: "+e.getMessage());
         } catch (NullPointerException npe) {
             Log.e(MCMDefines.LOG_TAG,"Error getting city location: "+npe.getMessage());
         }
 
         return res;
     }
 
     /**
      * Gets cached location from most accurate provider
      * @param context
      * @return
      */
     public static Location getLocation(Context context) {
         LocationManager locationManager = (LocationManager) context.getSystemService( LOCATION_SERVICE );
         String provider = mostAccurateLocationProvider( locationManager );
         return locationManager.getLastKnownLocation( provider );
     }
 
     public static JSONObject getLocationJson(Context context) {
 
         JSONObject locationJson = new JSONObject();
 
         Location lastKnownLocation = getLocation(context);
 
         try {
             if (lastKnownLocation != null)
             {
                 Float accuracy = new Float(lastKnownLocation.getAccuracy());
                 locationJson.put("accuracy", accuracy.longValue());
                 locationJson.put("latitude", lastKnownLocation.getLatitude());
                 locationJson.put("longitude", lastKnownLocation.getLongitude());
             } else
             {
                 locationJson.put("accuracy", 0);
                 locationJson.put("latitude", 0);
                 locationJson.put("longitude", 0);
             }
         } catch (JSONException e) {
             Log.i(MCMDefines.LOG_TAG, "JSONException = " + e.getMessage());
         }
 
         return locationJson;
     }
 
    private static String mostAccurateLocationProvider(LocationManager locationManager)
    {
         Criteria criteria = new Criteria();
         criteria.setAccuracy( Criteria.ACCURACY_FINE );
        String bestProvider = locationManager.getBestProvider( criteria, ENABLED ); // Could be null if there isn't provider matching with criteria
        return bestProvider != null ? bestProvider : LocationManager.NETWORK_PROVIDER;
     }
 }
