 package no.oxycoon.android.rebus;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.ProgressDialog;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import android.content.*;
 import android.widget.*;
 import android.util.Log;
 import android.view.*;
 
 public class RebusActivity extends Activity {
 	private Button startMapViewButton, startRaceButton, settingsButton,
 			cancelButton, finishedRacebutton;
 
 	private MyLocationListener mll;
 	private LocationManager locationManager;
 	private ServerContactTask task;
 
 	private Boolean activeRebus;
 	private Boolean locationFound;
 
 	public static final int SELECT_AVAILABLE_RACES = 1; // responsecode for
 														// RebusListViewer
 	public static final int SELECT_FINISHED_RACES = 2; // responsecode for
 														// RebusListViewer
 	
 	private Track currentTrack;
 
 	/**
 	 * Values for proximity alert
 	 * */
 	public static long minimum_distance_for_update = 5; // meters
 	public static long minimum_time_for_update = 5000; // milliseconds
 
 	private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
 	private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
 
 	private static final long RADIUS_FOR_POINT = 10; // meters
 	private static final long PROXY_ALERT_EXPIRATION = -1;
 
 	private static final String PROX_ALERT_INTENT = "no.oxycoon.android.rebus.ProximityAlert";
 	
 	private double postLongitude, postLatitude, postRadius;
 	private String postClue;
 
 	/**
 	 * End
 	 * */
 
 	//User information
 	private String uname, upwd; 
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mll = new MyLocationListener();
 
 		startMapViewButton = (Button) findViewById(R.id.main_button_startmap);
 		startRaceButton = (Button) findViewById(R.id.main_button_startrace);
 		settingsButton = (Button) findViewById(R.id.main_button_settings);
 		cancelButton = (Button) findViewById(R.id.main_button_cancel);
 		finishedRacebutton = (Button)findViewById(R.id.main_button_finishedrace);
 		
 		settingsButton.setOnClickListener(new ButtonHandler());
 		startMapViewButton.setOnClickListener(new ButtonHandler());
 		startRaceButton.setOnClickListener(new ButtonHandler());
 		cancelButton.setOnClickListener(new ButtonHandler());
 		finishedRacebutton.setOnClickListener(new ButtonHandler());
 
 		if (activeRebus == null){
 			activeRebus = false;
 		}
 		if (locationFound == null){
 			locationFound = false;
 		}
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			uname = extras.getString("username");
 			upwd = extras.getString("password");
 		}
 	}
 	
 	public void activateRebus(){
 		activeRebus = true;
 		locationFound = true;
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mll);
 	}
 	
 	public void endRebus(){
 		activeRebus = false;
 		locationManager.removeUpdates(mll);
 	}
 	
 	public void showClue(){
 		Toast.makeText(this, postClue, 10).show();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case SELECT_AVAILABLE_RACES: {
 			if (resultCode == Activity.RESULT_OK) {
 				if (data != null) {
 					// TODO: Start a timer notification for time until race
 					// starts.
 					activateRebus();
 					
 					
 					String[] stringTemp = data.getStringArrayExtra("returnResult");
 					
 					currentTrack = new Track(Integer.parseInt(stringTemp[0]), stringTemp[1], stringTemp[2], Long.parseLong(stringTemp[3]), Long.parseLong(stringTemp[4]));
					
					//AlarmManager am = AlarmManager
					
					
					
 
					// TODO: Start a Proximity Alert with given data.
 				} // End if data
 			} // End if resultCode
 			break;
 		} // End case SELECT_AVAILABLE_RACES
 		case SELECT_FINISHED_RACES: {
 			if (resultCode == Activity.RESULT_OK) {
 				if (data != null) {
 					Intent intent = new Intent(this, RebusMap.class);
 
 					intent.putExtra("longitude", data.getDoubleArrayExtra("longitude"));
 					intent.putExtra("latitude", data.getDoubleArrayExtra("latitude"));
 
 					startActivity(intent);
 				} // End if data
 			} // End if resultCode
 			break;
 		} // End case SELECT_FINISHED_RACES
 		} // End switch
 	} // End onActivityResult()
 
 	/**
 	 * @author Daniel
 	 *
 	 */
 	private class ButtonHandler implements View.OnClickListener {
 		public void onClick(View arg0) {
 			switch (arg0.getId()) {
 			case R.id.main_button_startmap: {
 				Intent intent = new Intent(RebusActivity.this, RebusMap.class);
 				intent.putExtra("active", activeRebus);
 				startActivity(intent);
 				break;
 			} // End case R.id.main_button_startmap
 			case R.id.main_button_startrace: {
 				Intent intent = new Intent(RebusActivity.this, RebusListViewer.class);
 				intent.putExtra("newRace", true);
 				intent.putExtra("username", uname);
 				intent.putExtra("password", upwd);
 				startActivityForResult(intent, SELECT_AVAILABLE_RACES);
 				break;
 			} // End case R.id.main_button_startrace
 			case R.id.main_button_settings: {
 				break;
 			} // End case R.id.main_button_settings
 			case R.id.main_button_cancel: {
 				showClue();
 				break;
 			} // End case R.id.main_button_cancel
 			case R.id.main_button_finishedrace: {
 				/**Real method to use.*/
 				Intent intent = new Intent(RebusActivity.this, RebusListViewer.class);
 				intent.putExtra("newRace", false);
 				startActivityForResult(intent, SELECT_FINISHED_RACES);
 				break;
 			} // End case R.id.main_button_finishedrace
 			} // End switch
 		} // End onClick()
 	} // End class ButtonHandler
 
	// TODO: Check and return user's location to server.
 	/**
 	* http://www.firstdroid.com/2010/04/29/android-development-using-gps-to-get-current-location-2/
 	*/
 	private class MyLocationListener implements LocationListener {
 		public void onLocationChanged(Location location) {
 			if (activeRebus){
 				double tempLat = location.getLatitude();
 				double tempLng = location.getLongitude();
 				String tempUrl = "http://rdb.goldclone.no/?username="+uname+"&password="+upwd+"&longitude="+tempLng+"&latitude="+tempLat;
 				
 				task = new ServerContactTask();
 				task.execute(tempUrl);
 			}
 		}
 
 		public void onStatusChanged(String s, int i, Bundle b) {
 		}
 
 		public void onProviderDisabled(String s) {
 		}
 
 		public void onProviderEnabled(String s) {
 		}
 	} // End class MyLocationListener
 	
 	/**
 	 * ServerContactTask
 	 * 
 	 * Sends the user's location to the server.
 	 * 
 	 * @author Daniel
 	 */
 	private class ServerContactTask extends	AsyncTask<String, String, Boolean> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 		}//End onPreExecute()
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 		}//End onProgressUpdate()
 		
 		protected void onPostExecute(Boolean result){
 			if(locationFound){
 				locationFound = false;
 				
 				//TODO: update a new proximity alert
 			}
 		}
 
 		/**
 		 * doInBackground()
 		 * 
 		 **/
 		@Override
 		protected Boolean doInBackground(String... params) {
 			//Sends user location to server
 			try{				
 				HttpParams httpParams = new BasicHttpParams();
 				HttpConnectionParams.setSoTimeout(httpParams, 5000);
 				HttpClient theClient = new DefaultHttpClient(httpParams);
 				HttpGet method = new HttpGet(new URI(params[0]));
 				
 				theClient.execute(method);
 			}//end try
 			catch(Exception e){
 			}//end catch
 			
 			//Gets next post if user's found current post.
 			if(locationFound){
 				try {
 					String tempPostURL = "http://rdb.goldclone.no/?format=xml&target=post&username="+uname+"&password="+upwd;
 					
 					HttpParams httpParams = new BasicHttpParams();
 					HttpConnectionParams.setSoTimeout(httpParams, 30000);
 					HttpClient theClient = new DefaultHttpClient(httpParams);
 					HttpGet method = new HttpGet(new URI(tempPostURL));
 
 					HttpResponse response = theClient.execute(method);
 
 					InputStream in = response.getEntity().getContent();
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
 
 					NodeList nodeLst = doc.getElementsByTagName("post");
 
 					// XML-parse loop
 					for (int i = 0; i < nodeLst.getLength(); i++) {
 						Node fstNode = nodeLst.item(i);
 						if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
 							Element fstElement = (Element) fstNode;
 							// ------Gets the post radius from the xml-------
 							NodeList radiusList = fstElement.getElementsByTagName("radius");
 							Element radiusElement = (Element) radiusList.item(0);
 							NodeList textRList = radiusElement.getChildNodes();
 							
 							postRadius = Double.parseDouble(textRList.item(0).getNodeValue().toString());
 							// ------Gets the post latitude from the xml-------
 							NodeList latList = fstElement.getElementsByTagName("latitude");
 							Element latElement = (Element) latList.item(0);
 							NodeList textLatList = latElement.getChildNodes();
 
 							postLatitude = Double.parseDouble(textLatList.item(0).getNodeValue().toString());	
 							// ------Gets the post longitude from the xml-------
 							NodeList longList = fstElement.getElementsByTagName("longitude");
 							Element longElement = (Element) longList.item(0);
 							NodeList textLngList = longElement.getChildNodes();
 
 							postLongitude = Double.parseDouble(textLngList.item(0).getNodeValue().toString());
 							// ------Gets the post clue from the xml-------
 							NodeList clueList = fstElement.getElementsByTagName("clue");
 							Element clueElement = (Element) clueList.item(0);
 							NodeList textClueList = clueElement.getChildNodes();
 
 							postClue = textClueList.item(0).getNodeValue().toString();
 						}// End if
 					}// End for i < nodeLst.getLength()
 				}// End try
 				catch (Exception e) {
 
 				}// end catch
 			}//end if
 			return true;
 			
 		}//end doInBackground()
 	}//end class ServerContactTask
 }
