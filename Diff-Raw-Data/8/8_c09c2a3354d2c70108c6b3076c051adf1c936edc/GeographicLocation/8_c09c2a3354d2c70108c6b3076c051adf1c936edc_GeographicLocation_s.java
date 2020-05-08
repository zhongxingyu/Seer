 package de.fhb.polyencoder.geo;
 
 /**
  * GeographicLocation with GPS coordinates.
  * 
  * Porting of Mark McClures JavaScript PolylineEncoder (named as Trackpoint)
  * 
  * @author Mark Rambow (markrambow[at]gmail[dot]com)
  * @author Peter Pensold
  * @version 1
  */
 public class GeographicLocation {
   private double altitude;
   private double latitude;
   private double longitude;
 
 
 
   /**
    * Creates a GeographicLocation with a given latitude and longitude. The
    * altitude will be set to 0.0
    * 
    * @param latitude
    * @param longitude
    */
   public GeographicLocation(double latitude, double longitude) {
     this(latitude, longitude, 0.0);
   }
 
 
 
   public GeographicLocation(double latitude, double longitude, double altitude) {
     setLatitude(latitude);
     setLongitude(longitude);
     setAltitude(altitude);
   }
 
 
 
   public boolean hasValidLatitude() {
     return GeographicCoordinate.isValidLatitude(latitude);
   }
 
 
 
   public boolean hasInvalidLatitude() {
     return GeographicCoordinate.isInvalidLatitude(latitude);
   }
 
 
 
   public boolean hasValidLongitude() {
     return GeographicCoordinate.isValidLongitude(longitude);
   }
 
 
 
   public boolean hasInvalidLongitude() {
     return GeographicCoordinate.isInvalidLongitude(longitude);
   }
 
 
 
   /**
    * Return format is: Latitude;Longitude;Altitude
    * 
    * @return String with the coordinates of this GeographicLocation.
    */
   public String toString() {
     return String.valueOf(latitude) + ", " + String.valueOf(longitude) + ", " + String.valueOf(altitude);
   }
 
 
 
   /**
    * Produces a hashcode of this GeographicLocation. Uses a prime number to
    * generate this number.
    * 
    * This method was generated with Eclipse.
    * 
    * @return the hashcode of this GeographicLocation
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    * @see java.util.HashMap
    */
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     long temp;
 
     temp = Double.doubleToLongBits(altitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
 
     temp = Double.doubleToLongBits(latitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
 
     temp = Double.doubleToLongBits(longitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
 
     return result;
   }
 
 
 
   /**
    * Indicates whether some GeographicLocation equals this GeographicLocation.
    * It will compare the altitude, longitude and latitude of the
    * GeographicLocation.
    * 
    * This method was generated with Eclipse.
    * 
    * @param obj
    *          the reference object with which to compare.
    * 
    * @return {@code true} if this object is the same as the {@code obj}
    *         argument; {@code false} otherwise.
    */
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
 
     if (obj == null) {
       return false;
     }
 
     if (!(obj instanceof GeographicLocation)) {
       return false;
     }
 
     GeographicLocation other = (GeographicLocation) obj;
 
     if (Double.doubleToLongBits(altitude) != Double.doubleToLongBits(other.altitude)) {
       return false;
     }
 
     if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude)) {
       return false;
     }
 
     if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) {
       return false;
     }
 
     return true;
   }
 
 
 
   public double getAltitude() {
     return altitude;
   }
 
 
 
   public void setAltitude(double altitude) {
     this.altitude = altitude;
   }
 
 
 
   public double getLatitude() {
     return latitude;
   }
 
 
 
   public void setLatitude(double latitude) {
     this.latitude = latitude;
   }
 
 
 
   public double getLongitude() {
     return longitude;
   }
 
 
 
   public void setLongitude(double longitude) {
     this.longitude = longitude;
   }
 
 
 
   /**
    * Short for getAltitude()
    * 
    * @return altitude
    */
   public double alt() {
     return getAltitude();
   }
 
 
 
   /**
    * Short for getLatitude()
    * 
    * @return latitude
    */
   public double lat() {
     return getLatitude();
   }
 
 
 
   /**
    * Short for getLongitude()
    * 
    * @return longitude
    */
   public double lng() {
     return getLongitude();
   }
 }
