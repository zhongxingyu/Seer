 package org.vaadin.addons.locationtextfield;/*
  *
  *  * Licensed to the Apache Software Foundation (ASF) under one or more
  *  * contributor license agreements.  See the NOTICE file distributed with
  *  * this work for additional information regarding copyright ownership.
  *  * The ASF licenses this file to You under the Apache License, Version 2.0
  *  * (the "License"); you may not use this file except in compliance with
  *  * the License.  You may obtain a copy of the License at
  *  *
  *  *      http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  *
  */
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * {@link LocationProvider} which uses Google.
  *
  * This class can only be used when used in conjunction with Google Maps API
  * See http://code.google.com/apis/maps/documentation/geocoding/ for details
  */
 public final class GoogleGeocoder extends URLConnectionGeocoder {
 
     private static final String URL = "maps.googleapis.com/maps/api/geocode/json";
     private static final String INSECURE_URL = "http://" + URL;
     private static final String SECURE_URL = "https://" + URL;
 
     private static final GoogleGeocoder INSTANCE = new GoogleGeocoder();
 
     private boolean useSecureConnection;
 
     private GoogleGeocoder() {
         // nuthin'
     }
 
     public static GoogleGeocoder getInstance() {
         return INSTANCE;
     }
 
     protected String getURL(String address) throws UnsupportedEncodingException {
        return (this.useSecureConnection ? SECURE_URL : INSECURE_URL) + "?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false";
     }
 
     protected Collection<? extends GeocodedLocation> createLocations(String address, String input) throws GeocodingException {
         final Set<GeocodedLocation> locations = new LinkedHashSet<GeocodedLocation>();
         try {
             JSONObject obj = new JSONObject(input);
             if ("OK".equals(obj.getString("status"))) {
                 JSONArray results = obj.getJSONArray("results");
                 boolean ambiguous = results.length() > 1;
                 for (int i = 0; i < results.length(); i++) {
                     JSONObject result = results.getJSONObject(i);
                     GeocodedLocation loc = new GeocodedLocation();
                     loc.setAmbiguous(ambiguous);
                     loc.setOriginalAddress(address);
                     loc.setGeocodedAddress(result.getString("formatted_address"));
                     JSONArray components = result.getJSONArray("address_components");
                     for (int j = 0; j < components.length(); j++) {
                         JSONObject component = components.getJSONObject(j);
                         String value = component.getString("short_name");
                         JSONArray types = component.getJSONArray("types");
                         for (int k = 0; k < types.length(); k++) {
                             String type = types.getString(k);
                             if ("street_number".equals(type))
                                 loc.setStreetNumber(value);
                             else if ("route".equals(type))
                                 loc.setRoute(value);
                             else if ("locality".equals(type))
                                 loc.setLocality(value);
                             else if ("administrative_area_level_1".equals(type))
                                 loc.setAdministrativeAreaLevel1(value);
                             else if ("administrative_area_level_2".equals(type))
                                 loc.setAdministrativeAreaLevel2(value);
                             else if ("country".equals(type))
                                 loc.setCountry(value);
                             else if ("postal_code".equals(type))
                                 loc.setPostalCode(value);
                         }
                     }
                     JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                     loc.setLat(location.getDouble("lat"));
                    loc.setLon(location.getDouble("lng"));
                     loc.setType(getLocationType(result));
                     locations.add(loc);
                 }
             }
         } catch (JSONException e) {
             throw new GeocodingException(e.getMessage(), e);
         }
         return locations;
     }
 
     private LocationType getLocationType(JSONObject result) throws JSONException {
         if (!result.has("types"))
             return LocationType.UNKNOWN;
         JSONArray types = result.getJSONArray("types");
         for (int i = 0; i < types.length(); i++) {
             final String type = types.getString(i);
             if ("street_address".equals(type))
                 return LocationType.STREET_ADDRESS;
             else if ("route".equals(type))
                 return LocationType.ROUTE;
             else if ("intersection".equals(type))
                 return LocationType.INTERSECTION;
             else if ("country".equals(type))
                 return LocationType.COUNTRY;
             else if ("administrative_area_level_1".equals(type))
                 return LocationType.ADMIN_LEVEL_1;
             else if ("administrative_area_level_2".equals(type))
                 return LocationType.ADMIN_LEVEL_2;
             else if ("locality".equals(type))
                 return LocationType.LOCALITY;
             else if ("neighborhood".equals(type))
                 return LocationType.NEIGHBORHOOD;
             else if ("postal_code".equals(type))
                 return LocationType.POSTAL_CODE;
             else if ("point_of_interest".equals(type))
                 return LocationType.POI;
         }
         return LocationType.UNKNOWN;
     }
 
     public boolean isUseSecureConnection() {
         return useSecureConnection;
     }
 
     public void setUseSecureConnection(boolean useSecureConnection) {
         this.useSecureConnection = useSecureConnection;
     }
 }
