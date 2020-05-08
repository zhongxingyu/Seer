 package com.cse5236groupthirteen.utilities;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.parse.ParseGeoPoint;
 
 public class YumHelper {
 
 	public static void handleError(Context context, String errmsg) {
 		Toast.makeText(context, errmsg, Toast.LENGTH_LONG).show();
 		Log.e("Yum Exception!", errmsg);
 	}
 	
 	public static void handleException(Context context, Exception e, String errmsg) {
 		Toast.makeText(context, "(e) "+errmsg, Toast.LENGTH_LONG).show();
 		Log.e("Yum Exception!", errmsg);
 		Log.e("Yum Exception!", e.toString());
 	}
 	
 	public static Location getLastBestLocation(Context context) {
 		LocationManager lm = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 		Location locationGPS = lm
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location locationNet = lm
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		boolean haveGPS = (locationGPS != null);
 		boolean haveNet = (locationNet != null);
 		
 		if (!haveGPS && !haveNet) {
			
 			// uh oh... no location
 			
 		} else if (haveGPS && !haveNet) {
 			
 			return locationGPS;
 			
 		} else if (!haveGPS && haveNet) {
 			
 			return locationNet;
 			
 		} else if (haveGPS && haveNet) {
 			
 			long GPSLocationTime = locationGPS.getTime();
 			long NetLocationTime = locationNet.getTime();
 			
 			if (GPSLocationTime - NetLocationTime > 0) {
 				
 				return locationGPS;
 				
 			} else {
 				
 				return locationNet;
 				
 			}
 			
 		} else {
 			// no other possible combination of haveGPS and haveNet
 		}
 		
 		
 		long GPSLocationTime = 0;
 		if (null != locationGPS) {
 			GPSLocationTime = locationGPS.getTime();
 		}
 
 		long NetLocationTime = 0;
 
 		if (null != locationNet) {
 			NetLocationTime = locationNet.getTime();
 		}
 
 		Location myLocation = new Location(locationNet);
 		if (0 < GPSLocationTime - NetLocationTime) {
 			myLocation = locationGPS;
 		} else {
 			myLocation = locationNet;
 		}
 		return myLocation;
 	}
 	
 	public static ParseGeoPoint getLastBestLocationForParse(Context context) {
 
 		Location myLocation = getLastBestLocation(context);
 		double lat = myLocation.getLatitude();
 		double lon = myLocation.getLongitude();
 
 		return new ParseGeoPoint(lat, lon);
 	}
 
 	public static ParseGeoPoint getParseGeoPointFromRestaurantFullAddress(
 			Context context, String address) {
 
 		String strAddress = address;
 		Geocoder coder = new Geocoder(context);
 		List<android.location.Address> addresses;
 		try {
 
 			strAddress = strAddress.replace(' ', '+');
 			addresses = coder.getFromLocationName(strAddress, 1);
 			if (addresses == null) {
 				return null;
 			}
 			if (addresses.isEmpty()) {
 				return null;
 			}
 			android.location.Address location = addresses.get(0);
 			double latitude = location.getLatitude();
 			double longitude = location.getLongitude();
 
 			return new ParseGeoPoint(latitude, longitude);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 
 	public static ParseGeoPoint getParseGeoPointFromAddress(String address) {
 		JSONObject o = getLocationInfo(address);
 		GeoPoint g = getGeoPoint(o);
 		double lat = g.getLatitudeE6() / 1.0E6;
 		double lon = g.getLongitudeE6() / 1.0E6;
 		return new ParseGeoPoint(lat, lon);
 	}
 
 	private static JSONObject getLocationInfo(String address) {
 		String strAddress = address.replace(' ', '+');
 		HttpGet httpGet = new HttpGet(
 				"http://maps.google.com/maps/api/geocode/json?address="
 						+ strAddress + "&ka&sensor=false");
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		StringBuilder stringBuilder = new StringBuilder();
 
 		try {
 			response = client.execute(httpGet);
 			HttpEntity entity = response.getEntity();
 			InputStream stream = entity.getContent();
 			int b;
 			while ((b = stream.read()) != -1) {
 				stringBuilder.append((char) b);
 			}
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		}
 
 		JSONObject JsonObject = new JSONObject();
 		try {
 			JsonObject = new JSONObject(stringBuilder.toString());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		return JsonObject;
 	}
 
 	private static GeoPoint getGeoPoint(JSONObject JsonObject) {
 
 		Double lon = Double.valueOf(0);
 		Double lat = Double.valueOf(0);
 
 		try {
 
 			lon = ((JSONArray) JsonObject.get("results")).getJSONObject(0)
 					.getJSONObject("geometry").getJSONObject("location")
 					.getDouble("lng");
 
 			lat = ((JSONArray) JsonObject.get("results")).getJSONObject(0)
 					.getJSONObject("geometry").getJSONObject("location")
 					.getDouble("lat");
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
 
 	}
 
 }
