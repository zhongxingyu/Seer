 package cvv.pelmen;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddStore extends Activity {
 	EditText lat, lon;
 	EditText address;
 	String httpResult;
 	private static final String DEBUG_TAG = "PelmenHttp";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_store);
 }
 
 	public void getCoordinates(View view){
 		
 		GPSTracker gps = new GPSTracker(this);
     	if(gps.canGetLocation()){ 
     		//Getting Latitude and Longitude
     		lat = (EditText) findViewById(R.id.lat_field);
     		lon = (EditText) findViewById(R.id.lon_field);
     		lat.setText( ((Double)gps.getLatitude()).toString() );
     		lon.setText( ((Double)gps.getLongitude()).toString() );
     	} else {
     		//Showing GPS Settings Alert Dialog
     		gps.showSettingsAlert();
     	}
 	}
 	
 
 	public void getAddress(View view){
 		if (lat == null || lon == null) 
 			Toast.makeText(this, "Please, enter Store coordinates first", Toast.LENGTH_LONG).show();
 		else {
 			// Build URL
 			//		URL example:
 			// 		http://geocode-maps.yandex.ru/1.x/?results=1&kind=house&format=json&geocode=30.52086642935999,50.46592397135805
 			String url = "http://geocode-maps.yandex.ru/1.x/?"
 					+ "results=1"
 					+ "&lang=uk-UA"
 					+ "&kind=street"
 					+ "&format=json"
 					+ "&geocode="
 						+ lon.getText().toString() + ","
 						+ lat.getText().toString();
 
 			address = (EditText) findViewById(R.id.address_field);
 			
 			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		    if (networkInfo != null && networkInfo.isConnected()) {
 		    	// Make async HTTP request. Result put in httpResult var
 		    	new DownloadWebpageTask().execute(url);
 		    	if (httpResult != null) {
 					try {
 						//Parsing JSON data of Yandex Map Geocoder
 						//	JSON scheme see here - http://api.yandex.ru/maps/doc/geocoder/desc/concepts/response_structure.xml#json_response
 						JSONObject jsonResult =  new JSONObject(httpResult).
 								getJSONObject("response").
 								getJSONObject("GeoObjectCollection").
 								getJSONArray("featureMember").
 								getJSONObject(0).
 								getJSONObject("GeoObject").
 								getJSONObject("metaDataProperty").
 								getJSONObject("GeocoderMetaData");
 						address.setText(jsonResult.getString("text"));
 						
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		    		
 		    	}
     	
 		    } 
 		    else Toast.makeText(this, "No network connection ;(", Toast.LENGTH_LONG).show();
 		        
 		}
 	}
 
 	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
 	    @Override
 	    protected String doInBackground(String... urls) {
 	          
 	        // params comes from the execute() call: params[0] is the url.
 	        try {
 	            return downloadUrl(urls[0]);
 	        } catch (IOException e) {
 	            return "Unable to retrieve web page. URL may be invalid.";
 	        }
 	    }
 	    // onPostExecute displays the results of the AsyncTask.
 	    @Override
 	    protected void onPostExecute(String result) {
 	       httpResult = result;
 	   }
 	    
 	 // Given a URL, establishes an HttpUrlConnection and retrieves
 	 // the web page content as a InputStream, which it returns as
 	 // a string.
 	 private String downloadUrl(String myurl) throws IOException {
 	     InputStream is = null;
 	         
 	     try {
 	         URL url = new URL(myurl);
 	         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	         conn.setReadTimeout(10000 /* milliseconds */);
 	         conn.setConnectTimeout(15000 /* milliseconds */);
 	         conn.setRequestMethod("GET");
 	         conn.setDoInput(true);
 	         // Starts the query
 	         conn.connect();
 	         int response = conn.getResponseCode();
 	         Log.d(DEBUG_TAG, "The response is: " + response);
 	         is = conn.getInputStream();
 
 	         // Convert the InputStream into a string
 	         String contentAsString = readIt(is);
 	         return contentAsString;
 	         
 	     // Makes sure that the InputStream is closed after the app is
 	     // finished using it.
 	     } finally {
 	         if (is != null) {
 	             is.close();
 	         } 
 	     }
 	 }
 	 
 	// Reads an InputStream and converts it to a String.
 	 public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
 	     Reader reader = new InputStreamReader(stream, "UTF-8");        
 	     String buff = "";
 	     int i;
 	     i = reader.read();
 	     while (i != -1) {
 	    	 buff += (char)i;
 	    	 i = reader.read();
 	     }
 	     return buff;
 	 }
 	}
 }
