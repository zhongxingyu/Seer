 package com.dedaulus.cinematty.framework.tools;
 
 import android.location.Location;
 import com.dedaulus.cinematty.framework.Cinema;
 
 import java.util.Comparator;
 
 /**
  * User: Dedaulus
  * Date: 24.04.11
  * Time: 0:58
  */
 public class CinemaComparator implements Comparator<Cinema> {
     private CinemaSortOrder sortOrder;
     private Location location;
     private float[] distance1;
     private float[] distance2;
 
     public CinemaComparator(CinemaSortOrder sortOrder, Object data) {
         this.sortOrder = sortOrder;
         switch (this.sortOrder) {
         case BY_DISTANCE:
             location = (Location)data;
             distance1 = new float[1];
             distance2 = new float[1];
             break;
         }
     }
 
     public int compare(Cinema o1, Cinema o2) {
         switch (sortOrder) {
         case BY_CAPTION:
             return o1.getName().compareTo(o2.getName());
 
         case BY_FAVOURITE:
             if (o1.getFavourite() == o2.getFavourite()) return 0;
             return (o1.getFavourite() < o2.getFavourite()) ? 1 : -1;
 
         case BY_DISTANCE:
             if (location != null && o1.getCoordinate() != null && o2.getCoordinate() != null) {
                 Location.distanceBetween(
                         location.getLatitude(), location.getLongitude(),
                         o1.getCoordinate().latitude, o1.getCoordinate().longitude,
                         distance1);
                 Location.distanceBetween(
                         location.getLatitude(), location.getLongitude(),
                         o2.getCoordinate().latitude, o2.getCoordinate().longitude,
                         distance2);
 
                 if (distance1[0] == distance2[0]) return 0;
                 return (distance1[0] < distance2[0]) ? -1 : 1;
 
            } else if (location != null) {
                if (o1.getCoordinate() != null) return 1;
                else if (o2.getCoordinate() != null) return -1;
             }
 
             return o1.getName().compareTo(o2.getName());
 
         default:
             throw new RuntimeException("Sort order not implemented!");
         }
     }
 }
