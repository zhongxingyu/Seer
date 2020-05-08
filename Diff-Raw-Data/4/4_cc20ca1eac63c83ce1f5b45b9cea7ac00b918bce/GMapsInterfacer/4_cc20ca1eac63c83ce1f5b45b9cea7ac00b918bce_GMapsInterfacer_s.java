 package com.rt.runtime.maps;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
  
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
  
 import com.google.android.gms.maps.model.LatLng;
 import com.rt.core.Leg;
 import com.rt.core.Waypoint;
 
 public class GMapsInterfacer{
 	
 	//SAXParserFactory parseFactory; 
 	//parseFactory.newSAXParser();
 	
 	//Leg not void
 	public static Leg getPath(Waypoint start, Waypoint end){
 		String url = getDirectionsUrl(start.centerPoint, end.centerPoint);
 		String response;
 		try {
 			response = query(url);
 			JSONObject r = new JSONObject(response);
 			ArrayList<LatLng> list = constructMapData(r);
 
 		} catch(Exception e) {
 			return null;
 		}
 	}
 	
 	//String not void
 	private static String query(String url) throws Exception{
         String data = "";
         InputStream iStream = null;
         HttpURLConnection urlConnection = null;
         try{
             URL url = new URL(strUrl);
  
             // Creating an http connection to communicate with url
             urlConnection = (HttpURLConnection) url.openConnection();
  
             // Connecting to url
             urlConnection.connect();
  
             // Reading data from url
             iStream = urlConnection.getInputStream();
  
             BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
  
             StringBuffer sb = new StringBuffer();
  
             String line = "";
             while( ( line = br.readLine()) != null){
                 sb.append(line);
             }
  
             data = sb.toString();
  
             br.close();
  
         }catch(Exception e){
         	throw new Exception("Exception while fetching url");
         }finally{
             iStream.close();
             urlConnection.disconnect();
         }
         return data;		
 	}
 	
 	//List not void and not sure about the input	
 	private static ArrayList<LatLng> constructMapData(JSONObject jObject) {
 		ArrayList<LatLng> list = new ArrayList<LatLng>();
 
 		try {		
 			jRoutes = jObject.getJSONArray("routes");
 
 			/** Traversing all routes */
 			for(int i = 0; i < jRoutes.length(); i++){
 				JSONArray op = ((JSONObject)jRoutes.get(i)).getJSONArray("overview_polyline");
 
 				if(op.size() > 0) {                
 					String polyline = "";
 					polyline = (String)((JSONObject)((JSONObject)op.get("points");
 
 					list.addAll(decodePoly(polyline));
 				}
 			}
 		} catch(Exception e) {
 
 		}
 
 		return list;
 	}
 
 	 private static ArrayList<LatLng> decodePoly(String encoded) {
  
 		ArrayList<LatLng> poly = new ArrayList<LatLng>();
 		int index = 0, len = encoded.length();
 		int lat = 0, lng = 0;
  
 		while (index < len) {
 			int b, shift = 0, result = 0;
 			do {
 				b = encoded.charAt(index++) - 63;
 				result |= (b & 0x1f) << shift;
 				shift += 5;
 			} while (b >= 0x20);
 			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
 			lat += dlat;
  
 			shift = 0;
 			result = 0;
 			do {
 				b = encoded.charAt(index++) - 63;
 				result |= (b & 0x1f) << shift;
 				shift += 5;
 			} while (b >= 0x20);
 			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
 			lng += dlng;
  
 			LatLng p = new LatLng((((double) lat / 1E5)),
 				(((double) lng / 1E5)));
 			poly.add(p);
 		}
  
 		return poly;
 	}
 
 	private static String getDirectionsUrl(LatLng origin, LatLng dest){
 		// Origin of route
 		String str_origin = "origin="+origin.latitude+","+origin.longitude;
  
 		// Destination of route
 		String str_dest = "destination="+dest.latitude+","+dest.longitude;
  
 		// Sensor enabled
 		String sensor = "sensor=true";
  
 		// Building the parameters to the web service
 		String parameters = str_origin+"&"+str_dest+"&"+sensor;
  
 		// Output format
 		String output = "json";
  
 		// Building the url to the web service
 		String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
  
 		return url;
 	}
 }
