 package mobisocial.noteshere.location;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 
 /**
  * Wrapper for Android's LocationManager
  */
 public class LocationHelper {
     public static final String TAG = "LocationHelper";
     
     private static final long WAIT_TIME = 1000 * 60;
     
     private final LocationManager mManager;
     private Location mLastLocation;
     
     public LocationHelper(final Activity activity) {
         mManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
         mLastLocation = null;
     }
     
     /**
      * Start a location update (or get a cached version if no callback)
      * @param {@link LocationResult} callback (can be null)
      * @return {@link android.location.Location} if one is cached
      */
     public Location requestLocation(final LocationResult cb) {
         Location cached = mManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (cached == null) {
             cached = mManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
         if (cached == null) {
             cached = mManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
         }
         mLastLocation = cached;
         if (cb != null) {
             LocationListener locationListener = new LocationListener() {
                 @Override
                 public void onLocationChanged(Location location) {
                     boolean better = isBetterLocation(location, mLastLocation);
                     if (better) {
                         // No need to keep trying if this is what we want
                         mManager.removeUpdates(this);
                         mLastLocation = location;
                         cb.onLocation(location);
                     }
                 }
 
                 @Override
                 public void onProviderDisabled(String provider) {}
 
                 @Override
                 public void onProviderEnabled(String provider) {}
 
                 @Override
                 public void onStatusChanged(String provider, int status,
                         Bundle extras) {}
             };
             mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
             mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
         }
         return cached;
     }
     
     /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
 
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > WAIT_TIME;
        boolean isNewer = timeDelta > 0;
 
        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        }
 
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());
 
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
     
     /**
      * Callback used when a location is retrieved by the location manager.
      */
     public static interface LocationResult {
         public void onLocation(Location location);
     }
 
 }
