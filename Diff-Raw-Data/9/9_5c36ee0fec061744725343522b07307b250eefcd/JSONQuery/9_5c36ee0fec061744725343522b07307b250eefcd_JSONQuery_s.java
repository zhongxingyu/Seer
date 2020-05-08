 package com.cmozie.classes;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import webConnections.*;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 
 
 public class JSONQuery {
 
 	Context _context;
 	String _zipcode;
 	String _areaCode;
 	String _city;
 	String _county;
 	String _state;
 	String _latitude;
 	String _longitude;
 	String _csa_name;
 	String _cbsa_name;
 	String _region;
 	String _timezone;
 	LocationDisplay _locationDetails;
 	
	public void getLookup(String zipcode){
 		String baseURL = "http://zipfeeder.us/zip?";
 		String key = "key=EN4GbNMq";
 		String qs = "";
 		try{
 			qs = URLEncoder.encode(zipcode, "UTF-8");
 		}catch (Exception e) {
 			
 			Log.e("Bad URL","Encoding Problem");
 			qs = "";
 		}
 		URL finalURL;
 		try{
 			finalURL = new URL (baseURL + key + "&zips=" + qs);
 			Log.i("URL",finalURL.toString());
 			QuoteRequest qr = new QuoteRequest();
 			qr.execute(finalURL);
 		}catch (MalformedURLException e){
 			Log.e("BAD URL", "Malformed URL");
 			finalURL = null;
 		}
 	}
 	
 	private class QuoteRequest extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL...urls){
 			String response = "";
 		for (URL url : urls) {
 			response = WebStuff.getURLStringResponse(url);
 		}
 			return response;
 		}
 		
 		@Override
 		protected void onPostExecute(String result){
 			
 			Log.i("URL RESPONSE", result);
 			try {
 				JSONObject json = new JSONObject(result);
 				JSONArray ja = json.getJSONArray("zips");
 				
 				
 				Log.i("results",result);
 				Boolean error =false;
 				if (error) {
 					
 					Toast toast = Toast.makeText(_context, "Invalid Zipcode", Toast.LENGTH_SHORT);
 					toast.show();
 					
 				}else {
 					//loops through json array 
 					for (int i = 0; i < ja.length(); i++) {
 						//sets a json object to access object values inside array
 						JSONObject one = ja.getJSONObject(i);
 						
 					//setting my text to the values to the strings of the json data
 					_zipcode = one.getString("zip_code");
 					_areaCode = one.getString("area_code");
 					_city = one.getString("city");
 					_state = one.getString("state");
 					_county = one.getString("county");
 					_csa_name = one.getString("csa_name");
 					_cbsa_name = one.getString("cbsa_name");
 					_latitude = one.getString("latitude");
 					_longitude = one.getString("longitude");
 					_region = one.getString("region");
 					_timezone = one.getString("time_zone");
 						 
 					}
 					Log.i("one", _areaCode + _city + _state + _county + _csa_name + _cbsa_name + _latitude + _longitude + _region + _timezone);
 				
 					//sets the values of the text by calling the locationInfo function inside of my Locationdisplay class
 					_locationDetails.locationInfo(_areaCode, _city, _county, _state, _latitude, _longitude, _csa_name, _cbsa_name, _region, _timezone);  
 					
 					Toast toast = Toast.makeText(_context, "Valid Zipcode " + _zipcode , Toast.LENGTH_SHORT);
 					toast.show();
 					
 					
 					
 					
 					
 					}
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				Log.e("JSON","JSON OBJECT EXCEPTION");
 			}
 		}
 		
 	}
 
 
 	
 }
