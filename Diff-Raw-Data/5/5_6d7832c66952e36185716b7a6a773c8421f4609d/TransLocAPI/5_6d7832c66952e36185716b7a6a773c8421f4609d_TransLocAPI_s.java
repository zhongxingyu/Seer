 // Class for pulling JSON from the TransLoc API
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 
 import org.json.*;
 
 
 public class TransLocAPI {
 
     private static String TRANSLOC = "http://api.transloc.com/1.1/";
 
     /*
     Method to get a list of agencies from TransLoc
     */
     public static String getAgency(String name) throws IOException, JSONException {
         URL url = new URL(TRANSLOC + "agencies.json");
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         String agency = ParseJSON.parseAgency(name, builder.toString());
         return agency;
     }
 
     /*
     Method to get a list of routes from TransLoc
     */
     public static ArrayList<HashMap> getRoutes(String id) throws IOException, JSONException {
         URL url = new URL(TRANSLOC + "routes.json?agencies=" + id);
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         ArrayList<HashMap> routes = ParseJSON.parseRoutes(id, builder.toString());
         return routes;
     }
 
     public static ArrayList<HashMap> getStops(String id) throws IOException, JSONException {
         URL url = new URL(TRANSLOC + "stops.json?agencies=" + id);
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         ArrayList<HashMap> stops = ParseJSON.parseStops(builder.toString());
         return stops;
     }
 
     public static ArrayList<HashMap> getEstimate(String id, String route, String stop) throws IOException, JSONException {
         URL url = new URL(TRANSLOC + "arrival-estimate.json?"
                                    + "agencies=" + id
                                    + "&routes="  + route
                                    + "&stops="   + stop);
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         ArrayList<HashMap> estimates = ParseJSON.parseEstimate(builder.toString());
         return estimates;
     }
 
     public static void main(String[] args) throws IOException, JSONException {
         String id = getAgency("uchicago");
         System.out.println(id);
         System.out.println(getRoutes(id));
         System.out.println(getStops(id));
     }
 }
 
 class ParseJSON {
 
     public static String parseAgency(String name, String json) throws JSONException {
         JSONObject jObj = new JSONObject(json);
         JSONArray temp_data = (JSONArray)jObj.get("data");
  
         String ret = "";
 
         for (int i=0; i < temp_data.length(); i++) {
             if ((((JSONObject)temp_data.get(i)).getString("name")).equals(name))
                 ret = (String)((JSONObject)temp_data.get(i)).getString("agency_id");
         }
         return ret;
     }
 
     public static ArrayList<HashMap> parseRoutes(String id, String json) throws JSONException {
         JSONObject jObj = new JSONObject(json);
         JSONArray temp_data = jObj.getJSONObject("data").getJSONArray(id);
         ArrayList<HashMap> routes = new ArrayList<HashMap>();
 
         for (int i=0; i < temp_data.length(); i++) {
            HashMap<String,String> pairs = new HashMap<String,String>();
             JSONObject j = temp_data.optJSONObject(i);
             Iterator it = j.keys();
             while (it.hasNext()) {
                 String n = (String)it.next();
                pairs.put(n,j.getString(n));
             }
             routes.add(pairs);
         }
         return routes;
     }
 
     public static ArrayList<HashMap> parseStops(String json) throws JSONException {
         JSONObject jObj = new JSONObject(json);
         JSONArray temp_data = jObj.getJSONArray("data");
         ArrayList<HashMap> stops = new ArrayList<HashMap>();
 
         for (int i=0; i < temp_data.length(); i++) {
             HashMap<String,String> pairs = new HashMap<String,String>();
             JSONObject j = temp_data.optJSONObject(i);
             Iterator it = j.keys();
             while (it.hasNext()) {
                 String n = (String)it.next();
                 pairs.put(n,j.getString(n));
             }
             stops.add(pairs);
         }
         return stops;
     }
 
     public static ArrayList<HashMap> parseEstimate(String json) throws JSONException {
         JSONObject jObj = new JSONObject(json);
         JSONArray temp_data = jObj.getJSONArray("data").getJSONObject(0).getJSONArray("arrivals");
         ArrayList<HashMap> estimates = new ArrayList<HashMap>();
 
         for (int i=0; i < temp_data.length(); i++) {
             HashMap<String,String> pairs = new HashMap<String,String>();
             JSONObject j = temp_data.optJSONObject(i);
             Iterator it = j.keys();
             while (it.hasNext()) {
                 String n = (String)it.next();
                 pairs.put(n,j.getString(n));
             }
             estimates.add(pairs);
         }
         return estimates;
     }
 
     public static void main(String[] args) throws IOException {
         return;
     }
 }
 
 
