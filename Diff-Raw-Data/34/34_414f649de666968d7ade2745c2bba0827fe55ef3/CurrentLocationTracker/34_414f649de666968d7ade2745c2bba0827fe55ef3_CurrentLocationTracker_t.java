 package info.eigenein.openwifi.helpers;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 /**
  * Tracks the best known location.
  */
 public class CurrentLocationTracker {
     private static final String LOG_TAG = CurrentLocationTracker.class.getCanonicalName();
 
     /**
      * Time delta to treat a location outdated.
      */
     private static final long OUTDATED_TIME_DELTA = 1000 * 10 * 2;
 
    /**
     * Used providers.
     */
    private static final String[] PROVIDERS = new String[] {
            LocationManager.PASSIVE_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
    };

     private static final CurrentLocationTracker instance = new CurrentLocationTracker();
 
     /**
      * Tracked location.
      */
     private static Location location = null;
 
     /**
      * Gets the location tracker.
      */
     public static CurrentLocationTracker getInstance() {
         return instance;
     }
 
     /**
      * Gets the best tracked location.
      */
     public Location getLocation(final Context context) {
         final LocationManager locationManager = getLocationManager(context);
 
         // Find the best location from the current location and last known locations.
        Location bestLocation = location;
        for (final String provider : PROVIDERS) {
            // Skip provider if not available.
            if (!locationManager.isProviderEnabled(provider)) {
                Log.w(LOG_TAG + ".getLocation", provider + " is not available");
                continue;
            }
            // Find best location.
            Log.d(LOG_TAG + ".getLocation", "trying " + provider);
            bestLocation = getBestLocation(
                    bestLocation,
                    locationManager.getLastKnownLocation(provider)
            );
        }
 
         // Check if the best location is not outdated.
         if (bestLocation != null && System.currentTimeMillis() - bestLocation.getTime() < OUTDATED_TIME_DELTA) {
             Log.d(LOG_TAG + ".getLocation", formatLocation(bestLocation));
             return bestLocation;
         }
 
         Log.d(LOG_TAG + ".getLocation", "null");
         return null;
     }
 
     /**
      * Notifies the tracker that there is location update.
      */
     public void notifyLocationChanged(final Location location) {
         Log.d(LOG_TAG, "notifyLocationChanged " + formatLocation(location));
 
         // Choose the best location.
         CurrentLocationTracker.location = getBestLocation(
                 CurrentLocationTracker.location, location);
 
         Log.d(LOG_TAG, "CurrentLocationTracker.location " + formatLocation(CurrentLocationTracker.location));
     }
 
     private static LocationManager getLocationManager(final Context context) {
         return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
     }
 
     /**
      * See https://developer.android.com/guide/topics/location/strategies.html
      */
     private static Location getBestLocation(final Location location2, final Location location1) {
         // A new location is always better than no location.
         if (location1 == null) {
             return location2;
         } else if (location2 == null) {
             return location1;
         }
 
         // Check whether the new location fix is newer or older.
         final long timeDelta = location2.getTime() - location1.getTime();
         final boolean isSignificantlyNewer = timeDelta > OUTDATED_TIME_DELTA;
         final boolean isSignificantlyOlder = timeDelta < -OUTDATED_TIME_DELTA;
         final boolean isNewer = timeDelta > 0;
 
         // If it's been more than two minutes since the current location, use the new location
         // because the user has likely moved.
         if (isSignificantlyNewer) {
             return location2;
             // If the new location is more than two minutes older, it must be worse.
         } else if (isSignificantlyOlder) {
             return location1;
         }
 
         // Check whether the new location fix is more or less accurate.
         float accuracyDelta = location2.getAccuracy() - location1.getAccuracy();
         final boolean isLessAccurate = accuracyDelta > 0.0f;
         final boolean isMoreAccurate = accuracyDelta < 0.0f;
         final boolean isSignificantlyLessAccurate = accuracyDelta > 200.0f;
 
         // Check if the old and new location are from the same provider.
         final boolean isFromSameProvider = isSameProvider(
                 location2.getProvider(),
                 location1.getProvider());
 
         // Determine location quality using a combination of timeliness and accuracy.
         if (isMoreAccurate) {
             return location2;
         } else if (isNewer && !isLessAccurate) {
             return location2;
         } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
             return location2;
         }
         return location1;
     }
 
     /**
      * Checks whether two providers are the same.
      */
     private static boolean isSameProvider(final String provider1, final String provider2) {
         if (provider1 == null) {
             return provider2 == null;
         }
         return provider1.equals(provider2);
     }
 
     private static String formatLocation(final Location location) {
         if (location == null) {
             return "null";
         }
         return String.format(
                 "Location[time=%s, provider=%s, accuracy=%s, lat=%s, lot=%s]",
                 location.getTime(),
                 location.getProvider(),
                 location.getAccuracy(),
                 location.getLatitude(),
                 location.getLongitude());
     }
 }
