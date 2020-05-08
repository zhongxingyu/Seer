 package com.gitwork;
 
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 //since we are going to use gps too we will implement a location listener
 public class ServiceMap extends MapActivity implements OnClickListener,
 		LocationListener {
 	// as usual global ui stuff
 	Button bSearch;
 	EditText eSearch;
 	AutoCompleteTextView eCat;
 	MapView searchMap;
 	// lets get a map controller for well ...
 	MapController mController;
 	// n lets get an overlay thats premade for us for the phones location
 	MyLocationOverlay phoneLocation;
 	// make a list of overlays that we will bandika on the map
 	List<Overlay> mOverLayList;
 	// lets get a gps location manager
 	LocationManager locationSnitch;
 	Drawable d;
 	GeoPoint gp;
 	String[] cats;
 
 	String[] laty, longi, titl, desc;
 
 	// variables
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 		// TODO Auto-generated method stub
 		super.onCreate(icicle);
 		setContentView(R.layout.servicemap);
 		bSearch = (Button) findViewById(R.id.btMapSearch);
 		eSearch = (EditText) findViewById(R.id.etMapSearch);
 		searchMap = (MapView) findViewById(R.id.searchMapView);
 		d = getResources().getDrawable(R.drawable.launcher);
 		eCat = (AutoCompleteTextView) findViewById(R.id.acCat);
 
 		bSearch.setOnClickListener(this);
 		// so that the map is in street view
 		searchMap.setStreetView(true);
 		searchMap.setBuiltInZoomControls(true);
 
 		// register our custom overlay look way below
 		basicOverlay basics = new basicOverlay();
 		// load and init the overlays list
 		mOverLayList = searchMap.getOverlays();
 		// add our own overlay to the above ^^^^ list
 		mOverLayList.add(basics);
 		// lets activate the devices location
 		phoneLocation = new MyLocationOverlay(ServiceMap.this, searchMap);
 		mOverLayList.add(phoneLocation);
 		// lets mod our map
 		mController = searchMap.getController();
 		mController.setZoom(8); // ranges from 0 to 18
 		// make a mock location n set it as the center of our map
 		GeoPoint point = new GeoPoint(2750547, (int) 36.8166667);
 
 		addJob(2750547, (int) 36.8166667, d, "loc", "this is a test loc");
 		mController.animateTo(point);
 		pickCats p = new pickCats();
 		p.execute();
 		
 		collectJobs cj = new collectJobs();
 		cj.execute();
 
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.btMapSearch:
 			// do some search thing we will do this together
 			break;
 
 		}
 
 	}
 
 	// lets make an overlay over the map so it can be easier to deal with the
 	// map
 	class basicOverlay extends Overlay {
 		// later when moving from prototype to app we can add stuff here
 	}
 
 	// never mind the location listener stuff for now we will get to this in a
 	// while
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addJob(int La, int Lo, Drawable PP, String title, String desc) {
 
 		GeoPoint p = new GeoPoint(La, Lo);
 		OverlayItem oi = new OverlayItem(p, title, desc);
 		customPinPoint cp = new customPinPoint(PP, ServiceMap.this);
 		cp.insertPinPoint(oi);
 		mOverLayList.add(cp);
 	}
 
 	public class pickCats extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected void onPostExecute(Void result) {
 			// TODO Auto-generated method stub
 			// super.onPostExecute(result);'
 			// Toast.makeText(projects_list.this,
 			// "The List Has been collected now choose your constituency",
 			// Toast.LENGTH_LONG).show();
 			eCat.requestFocus();
 			eCat.setEnabled(true);
 			ArrayAdapter<String> constituencyListAdapter = new ArrayAdapter<String>(
 					ServiceMap.this, R.layout.list_item, cats);
 			eCat.setAdapter(constituencyListAdapter);
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			// TODO Auto-generated method stub
 			// super.onProgressUpdate(values);
 
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 
 			// deactivate
 			eCat.setEnabled(false);
 			// Toast.makeText(projects_list.this,
 			// "Please wait while the list of constiuencies is collected",
 			// Toast.LENGTH_LONG).show();
 
 			// create a client
 			HttpClient cli = new DefaultHttpClient();
 			HttpPost post = new HttpPost("http://semasoftltd.com/webs/cons.php");
 			HttpResponse response = null;
 			try {
 				response = cli.execute(post);
 				if (response.getStatusLine().getStatusCode() == 200) {
 					String s = EntityUtils.toString(response.getEntity());
 					JSONArray ja = new JSONArray(s);
 					cats = new String[ja.length()];
 					for (int i = 0; i < ja.length(); i++) {
 						JSONObject c = ja.getJSONObject(i);
 						cats[i] = c.getString("Constituency");
 						Log.v("CONST", c.getString("Constituency") + "this");
 						if (i / 100.0 == 1.0 || i / 100.0 == 2.0) {
 							publishProgress();
 						}
 
 					}
 
 				}
 
 			} catch (Exception e) {
 
 			}
 
 			return null;
 		}
 
 	}
 
 	public class collectJobs extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 
 			// deactivate
 			eCat.setEnabled(false);
 			// Toast.makeText(projects_list.this,
 			// "Please wait while the list of constiuencies is collected",
 			// Toast.LENGTH_LONG).show();
 
 			// create a client
 			HttpClient cli = new DefaultHttpClient();
 			HttpPost post = new HttpPost("http://zacckos.heliohost.org/git_work/map_populate.php");
 			HttpResponse response = null;
 			try {
 				response = cli.execute(post);
 				if (response.getStatusLine().getStatusCode() == 200) {
 					String s = EntityUtils.toString(response.getEntity());
					JSONArray ja = new JSONArray(s);
 					laty = new String[ja.length()];
 					longi = new String[ja.length()];
 					titl = new String[ja.length()];
 					desc = new String[ja.length()];
 					for (int i = 0; i < ja.length(); i++) {
						JSONObject c = ja.getJSONObject(i);
 						laty[i] = c.getString("latitude");
 						longi[i] = c.getString("longitude");
 						titl[i] = c.getString("task_name");
 						desc[i] = c.getString("task_requirements");
 
 					}
 
 				}
 
 			} catch (Exception e) {
 
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 
 			for (int s = 0; s < laty.length; s++) {
 				addJob(Integer.parseInt(laty[s]), Integer.parseInt(longi[s]),
 						d, titl[s], desc[s]);
 			}
 
 		}
 	}
 
 }
