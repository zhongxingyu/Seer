 package com.dedaulus.cinematty.framework.tools;
 
 import android.location.Location;
 
 /**
  * User: Dedaulus
  * Date: 19.06.11
  * Time: 18:17
  */
 public class LocationHelper {
    public static final int COARSE_TIME_LISTEN_TIMEOUT = 30000;
    public static final int COARSE_LISTEN_DISTANCE = 10;
     public static final int FINE_TIME_LISTEN_TIMEOUT = 0;
     public static final int FINE_LISTEN_DISTANCE = 0;
 
     private static final int FINE_LOCATION_TIME_ADVANTAGE = 300000;
 
     public static Location selectBetterLocation(Location location1, Location location2) {
         if (location1 == null) return location2;
        else if (location2 == null) return location2;
         else {
             long time1 = location1.getTime();
             long time2 = location2.getTime();
 
             boolean firstFine = isFineLocation(location1);
             boolean secondFine = isFineLocation(location2);
 
             if (firstFine && !secondFine) {
                 time1 += FINE_LOCATION_TIME_ADVANTAGE;
             } else if (!firstFine && secondFine) {
                 time2 += FINE_LOCATION_TIME_ADVANTAGE;
             }
 
             if (time1 > time2) return location1;
             else return location2;
         }
     }
 
     public static boolean isFineLocation(Location location) {
         return location.getProvider().equalsIgnoreCase("gps");
     }
 }
