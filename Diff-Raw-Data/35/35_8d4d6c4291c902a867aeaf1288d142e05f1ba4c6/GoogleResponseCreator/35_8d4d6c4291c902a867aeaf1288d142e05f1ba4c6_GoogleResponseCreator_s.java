 package com.guide.city.crawler;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class GoogleResponseCreator {
 
     public static JSONObject getDetailsAboutPlace(Map<String, String> params) throws IOException, JSONException {
         return getResponse(params, GoogleHelper.GOOGLE_PLACE_DETAILS_URL);
     }
 
     public static JSONObject getTextSearchPlace(Map<String, String> params) throws IOException, JSONException {
         return getResponse(params, GoogleHelper.GOOGLE_TEXT_SEARCH_URL);
     }
 
    public static JSONObject getInformationAboutGeolacation(Map<String, String> params) throws IOException, JSONException {
         return getResponse(params, GoogleHelper.GOOGLE_GEOCODE_URL);
     }
 
    public static String getStreet(Map<String, String> params) throws IOException, JSONException {
         JSONObject response = getResponse(params, GoogleHelper.GOOGLE_GEOCODE_URL);
         JSONObject location = response.getJSONArray("results").getJSONObject(0);
         return location.getString("formatted_address");
     }
 
    private static JSONObject getResponse(Map<String, String> params, String googleUrl) throws IOException, JSONException {
         String url = googleUrl + "?" + GoogleHelper.encodeParams(params);
         JSONObject response = GoogleHelper.read(url);
 
         return response;
     }
 }
