 package com.agile.rocbarfinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import org.json.*;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class BarInfoFetcher extends AsyncTask<String, Void, Boolean>{
 	private final String apiKey = "AIzaSyD3ZznuBfcgAwbz5eqWtngbby871Epkq3U";
 	private final String searchLatitude = "43.1547";		// For Rochester NY
 	private final String searchLongitude = "-77.6158"; 	// For Rochester NY
	private final String searchRadius = "15000";			// In meters
	private final String searchType = "bar";
 	
 	private final String placesApiUrl = "https://maps.googleapis.com/maps/api/place/search/json?";
 	private final String placesLocationParam = "&location=" + searchLatitude + "," + searchLongitude;
 	private final String placesRadiusParam = "&radius=" + searchRadius;
	private final String placesNamesParam = "&types=" + searchType;
 	private final String placesSensorParam = "&sensor=true";
 	private final String placesKeyParam = "&key=" + apiKey;
 	
 	private final BarInfoStorage infoStorage;
 	
 	public BarInfoFetcher(Context c){
 		super();
 		infoStorage = BarInfoStorage.getInstance(c);
 	}
 	
 	private List<BarInformation> getData(){
 		List<BarInformation> lbi = new ArrayList<BarInformation>();
 		try {
 		    HttpPost httppost = new HttpPost(placesApiUrl + placesLocationParam + placesRadiusParam + placesNamesParam + placesSensorParam + placesKeyParam);
 		    HttpClient httpclient = new DefaultHttpClient();
 		    HttpResponse response = httpclient.execute(httppost);
 		    String data = EntityUtils.toString(response.getEntity());
 
 		    JSONArray jsonA = new JSONObject(data).getJSONArray("results");
 		    
 		    for (int i = 0; i < jsonA.length(); i++){
 		    	lbi.add(new BarInformation(jsonA.getJSONObject(i)));
 		    	System.out.println(lbi.get(i).name);
 		    }
 		    
 		    
 	
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}
 		return lbi;
 	}
 
 
 	@Override
 	protected Boolean doInBackground(String... params) {
 		List<BarInformation> data = getData();
 		infoStorage.addAllBars(data);
 
 		return true;
 	}
 }
