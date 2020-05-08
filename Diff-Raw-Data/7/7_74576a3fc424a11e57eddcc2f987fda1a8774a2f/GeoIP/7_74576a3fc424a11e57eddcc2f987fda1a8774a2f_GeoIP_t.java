 package org.vaadin.artur.geoip;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GeoIP {
     private static Map<String, Location> cache = Collections
             .synchronizedMap(new HashMap<String, GeoIP.Location>());
 
     public static class Location implements Serializable {
         private String ip;
         private String areacode;
         private String city;
         private String countryCode;
         private String countryName;
         private double latitude;
         private double longitude;
         private String metroCode;
         private String regionCode;
         private String regionName;
         private String zipcode;
 
         public String getIp() {
             return ip;
         }
 
         public String getAreacode() {
             return areacode;
         }
 
         public String getCity() {
             return city;
         }
 
         public String getCountryCode() {
             return countryCode;
         }
 
         public String getCountryName() {
             return countryName;
         }
 
         public double getLatitude() {
             return latitude;
         }
 
         public double getLongitude() {
             return longitude;
         }
 
         public String getMetroCode() {
             return metroCode;
         }
 
         public String getRegionCode() {
             return regionCode;
         }
 
         public String getRegionName() {
             return regionName;
         }
 
         public String getZipcode() {
             return zipcode;
         }
 
     }
 
     public static Location getLocation(String ip) {
         if (cache.containsKey(ip)) {
             return cache.get(ip);
         }
 
         try {
             URL url = new URL("http://freegeoip.net/json/" + ip);
             URLConnection c = url.openConnection();
             String s = IOUtils.toString(c.getInputStream());
             JSONObject obj = new org.json.JSONObject(s);
 
             // Store in cache for later
             Location location = parseLocation(obj);
             cache.put(ip, location);
             return location;
         } catch (IOException e) {
            // getLogger().log(Level.INFO, "Error doing lookup for " + ip, e);
         } catch (JSONException e) {
             getLogger().log(Level.INFO,
                     "Invalid response when doing lookup for " + ip, e);
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Error doing lookup for " + ip, t);
         }

         cache.put(ip, null);
         return null;
     }
 
     private static Location parseLocation(JSONObject obj) throws JSONException {
         Location info = new Location();
         info.ip = obj.getString("ip");
         info.areacode = obj.getString("areacode");
         info.city = obj.getString("city");
         info.countryCode = obj.getString("country_code");
         info.countryName = obj.getString("country_name");
         info.latitude = obj.getDouble("latitude");
         info.longitude = obj.getDouble("longitude");
         info.metroCode = obj.getString("metro_code");
         info.regionCode = obj.getString("region_code");
         info.regionName = obj.getString("region_name");
         info.zipcode = obj.getString("zipcode");
         return info;
     }
 
     private static Logger getLogger() {
         return Logger.getLogger(GeoIP.class.getName());
     }
 
 }
