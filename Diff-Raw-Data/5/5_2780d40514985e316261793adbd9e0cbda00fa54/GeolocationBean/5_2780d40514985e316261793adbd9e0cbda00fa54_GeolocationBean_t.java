 package org.icemobile.samples.springbasic;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * This is a sample backing bean for the MVC supported state
  * The properties should be the same
  */
 public class GeolocationBean {
 
     // Location
     private String location;
     private String latitude;
     private String longitude;
     private String altitude;
     private String direction;
 
     private int timeout = 30;
     private int maximumAge = 5;
     private String enableHighPrecision = "asNeeded";
     private boolean continuousUpdates = true;
     
     private boolean isAndroidContainer;
 
 
    public String getGeolocation() {
         return location;
     }
 
    public void setGeolocation(String location) {
         this.location = location;
         try {
             String[] parts = location.split(",");
             latitude = parts[0];
             longitude = parts[1];
             altitude = parts[2];
             direction = parts[3];
         } catch (Exception e)  {
             //always expect four comma-separtated parts
         }
     }
 
     public String getLatitude() {
         return latitude;
     }
 
     public String getLongitude() {
         return longitude;
     }
 
     public String getAltitude() {
         return altitude;
     }
 
     public String getDirection() {
         return direction;
     }
     
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     public int getMaximumAge() {
         return maximumAge;
     }
 
     public void setMaximumAge(int maximumAge) {
         this.maximumAge = maximumAge;
     }
 
     public String getEnableHighPrecision() {
         return enableHighPrecision;
     }
 
     public void setEnableHighPrecision(String enableHighPrecision) {
         this.enableHighPrecision = enableHighPrecision;
     }
 
     public boolean isContinuousUpdates() {
         return continuousUpdates;
     }
 
     public void setContinuousUpdates(boolean continuousUpdates) {
         this.continuousUpdates = continuousUpdates;
     }
     
     public void resetValues(){
         this.latitude = null;
         this.longitude = null;
         this.altitude = null;
         this.direction = null;
     }
     
     public boolean isAndroidContainer(){
         return isAndroidContainer;
     }
     
     public void setAndroidContainer(boolean val){
         isAndroidContainer = val;
     }
 
 
 }
