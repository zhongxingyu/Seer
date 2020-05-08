 package tw.ipis.routetaiwan;
 
 import java.net.URLEncoder;
 import java.text.Format;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Bound;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.Poly;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.ValueText;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Time;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.location.Criteria;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.maps.GeoPoint;
 import com.google.gson.Gson;
 
 public class planroute extends Activity {
 
 	//	ProgressBar planning;
 	String TAG = "~~planroute~~";
 	private ProgressBar planning;
 	private EditText from;
 	private EditText to;
 	private List<GeoPoint> _points = new ArrayList<GeoPoint>();
 	private LocationManager locationMgr;
 	private String provider;
 	private DownloadWebPageTask task = null;
 	public DirectionResponseObject dires = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.planroute);
 
 		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);
 		planning = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
 		planning.setIndeterminate(true);
 		sv.addView(planning);
 
 		planning.setVisibility(ProgressBar.GONE);
 	}
 
 	@Override
 	protected void onStop() {
 		locationMgr.removeUpdates(locationListener);
 		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
 			task.cancel(true);
 		super.onStop();
 	}
 
 	public void start_planing(View v) {
 		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
 			task.cancel(true);
 		foreground_cosmetic();
 		Getroute();
 	}
 
 	private void foreground_cosmetic() {
 		from = (EditText)findViewById(R.id.from);
 		to = (EditText)findViewById(R.id.to);
 
 		InputMethodManager imm = (InputMethodManager)getSystemService(
 				Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(from.getWindowToken(), 0);
 
 		planning.setVisibility(ProgressBar.VISIBLE);
 	}
 
 	public void Getroute() {
 		String request = "";
 		from = (EditText)findViewById(R.id.from);
 		to = (EditText)findViewById(R.id.to);
 		String start = from.getText().toString();	// Get user input "From"
 		String destination = to.getText().toString();	// Get user input "to"
 		String Mapapi = "https://maps.googleapis.com/maps/api/directions/json?origin={0}&destination={1}&sensor={3}&departure_time={2}&mode={4}&alternatives=true&region=tw";
 
 		if(Locale.getDefault().getDisplayLanguage().contentEquals("中文"))
 			Mapapi = new StringBuilder().append(Mapapi).append("&language=zh-tw").toString();
 
 		long now = System.currentTimeMillis() / 1000;
 		if(destination.isEmpty())
 			destination = "Taipei 101";
 
 		try {
 			if (start.isEmpty()) {
 				Location current = GetCurrentPosition();
 				String curr = current.getLatitude() + "," + current.getLongitude();
 				request = MessageFormat.format(Mapapi, URLEncoder.encode(curr, "UTF-8"), 
 						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
 						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
 			}
 			else {
 				request = MessageFormat.format(Mapapi, URLEncoder.encode(start, "UTF-8"),
 						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
 						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
 			}
 			Log.d(TAG, request);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		}
 
 		ConnectivityManager connMgr = (ConnectivityManager) 
 		getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			/* Use the url for http request */
 			task = new DownloadWebPageTask();
 			task.execute(new String[] {request});
 		} else {
 			Toast.makeText(this,"No network availalble" , Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 
 	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... urls) {
 			String response = "";
 			for (String url : urls) {
 				HttpGet httpGet = new HttpGet(url);
 				httpGet.addHeader("accept", "application/json");
 				//				StringBuilder builder = new StringBuilder();
 				HttpClient client = new DefaultHttpClient();
 				try {
 					HttpResponse result = client.execute(httpGet);
 					StatusLine statusLine = result.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = result.getEntity();
 						//						InputStream content = entity.getContent();
 						//						BufferedReader reader = new BufferedReader(new InputStreamReader(content), 65728);
 						//						String line = null;
 						//						while ((line = reader.readLine()) != null) {
 						//							builder.append(line);
 						//						}
 
 						response = EntityUtils.toString(entity);
 					} else {
 						Log.e(TAG, "Failed to download file");
 					}
 					return response;
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					return "";
 				}
 			}
 			return "";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			decode(result);
 		}
 	}
 
 	public String convertTime(long time){
 		time = time * 1000;	// Change to milli-seconds
 		Date date = new Date(time);
 		Format format = new SimpleDateFormat("HH:mm");
 		return format.format(date).toString();
 	}
 	
 	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity) {
 		TextView tv = new TextView(this);
 		tv.setText(content);
 		tv.setTextColor(textcolor);
 		tv.setTextSize(16);
 		tv.setHorizontallyScrolling(false);
 		tv.setWidth(0);
 		tv.setGravity(gravity);
 		if(weight != 0)
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, weight));
 		parent.addView(tv);
 		return tv;
 	}
 	
 	private TableRow CreateTableRow(TableLayout parent, float weight, int num){
 		TableRow tr = new TableRow(this);	// 1st row
 		if(num % 2 == 0)
 			tr.setBackgroundColor(Color.WHITE);
 		else
 			tr.setBackgroundColor(Color.LTGRAY);
 		if(weight != 0)
 			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
 		else
 			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		parent.addView(tr);
 		return tr;
 	}
 
 	public boolean dumpdetails(DirectionResponseObject dires) {
 		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);
 
 		// Create a LinearLayout element
 		TableLayout tl = new TableLayout(this);
 		tl.setOrientation(TableLayout.VERTICAL);
 		sv.removeAllViews();
 
 		if(!dires.status.contentEquals("OK")) {
 			Toast.makeText(this,"No result" , Toast.LENGTH_LONG).show();
 			return false;
 		}
 
 
 		// Add text
 		for (int i = 0; i < dires.routes.length; i++) {
 			int transit = 0;
 			for (int j = 0; j < dires.routes[i].legs.length; j++)	{
 				TableRow tr = CreateTableRow(tl, 0, i);	// 1st row
 
 				String title = "";
 				Time arrival_time, departure_time;
 				arrival_time = dires.routes[i].legs[j].arrival_time;
 				departure_time = dires.routes[i].legs[j].departure_time;
 				int duration = arrival_time.value - departure_time.value;
 				
				String dur = String.format(" (%d小時%d分)", TimeUnit.SECONDS.toHours(duration), TimeUnit.SECONDS.toMinutes(duration % 3600));
 				
 				title = new StringBuilder().append(convertTime(departure_time.value)).append(" - ").append(convertTime(arrival_time.value)).append(dur).toString();
 
 				createTextView(title, tr, Color.rgb(0,0,0), 1.0f, Gravity.LEFT);
 				
 				TableRow transit_times = CreateTableRow(tl, 0, i);	// 2nd row, leave it for later use
 
 				for (int k = 0; k < dires.routes[i].legs[j].steps.length; k++) {
 					Step step = dires.routes[i].legs[j].steps[k];
 					if(step.travel_mode.contentEquals("WALKING")) {
 						String walk = new StringBuilder().append(step.html_instructions).append(" (" + step.distance.text + ", " +step.duration.text + ")").toString();
 						tr = CreateTableRow(tl, 1.0f, i);
 						createTextView("走", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER);
 						createTextView(walk, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT);
 					}
 					else if(step.travel_mode.contentEquals("TRANSIT")) {
 						String trans = new StringBuilder().append(getResources().getString(R.string.taketransit)).append(step.transit_details.line.short_name).append(" (" + step.transit_details.num_stops + 
 								getResources().getString(R.string.stops) + ", " +step.duration.text + ")").toString();
 						transit++;
 						
 						tr = CreateTableRow(tl, 1.0f, i);
 						createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER);
 						createTextView(trans, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT);
 					}
 					if(k == dires.routes[i].legs[j].steps.length - 1) {
 						// Arrived
 						tr = CreateTableRow(tl, 1.0f, i);
 						createTextView("到", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER);
 						createTextView("目的地", tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT);
 					}
 				}
 				String str = getResources().getString(R.string.transit) + ": " + transit + "X";
 				createTextView(str, transit_times, Color.rgb(0,0,0), 1.0f, Gravity.LEFT);
 			}
 		}
 		// Add the LinearLayout element to the ScrollView
 		sv.addView(tl);
 
 		return true;
 	}
 
 	private void decode(String result) {
 		try {
 			Gson gson = new Gson();
 			dires = gson.fromJson(result,	DirectionResponseObject.class);
 			Log.i(TAG, "Total routes = " + dires.routes.length);
 			planning.setVisibility(ProgressBar.GONE);
 			if(dumpdetails(dires)) {
 				List<LatLng> list = decodePoly(dires.routes[0].overview_polyline.points);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Toast.makeText(this,"Cannot decode resource" , Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public Location GetCurrentPosition() {
 		initLocationProvider();
 		if(locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {	// Using GPS as provider, wait for GPS_EVENT_FIRST_FIX
 			Log.e(TAG, "GPS");
 			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 			Toast.makeText(this,"Getting current position..." , Toast.LENGTH_LONG).show();
 			locationMgr.addGpsStatusListener(gpsListener);
			Toast.makeText(this,"return..." , Toast.LENGTH_LONG).show();
			return locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		}
 		else {
 			locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 			Log.e(TAG, "network");
 			return locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 	}
 
 	private boolean initLocationProvider() {
 		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		//1.選擇最佳提供器
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 
 		provider = locationMgr.getBestProvider(criteria, true);
 
 		Toast.makeText(this,"使用" + provider + "定位..." , Toast.LENGTH_LONG).show();
 
 		if (provider != null) {
 			return true;
 		}
 
 		//2.選擇使用GPS提供器
 		//		if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 		//			provider = LocationManager.GPS_PROVIDER;
 		//			Toast.makeText(this,"使用" + provider + "定位..." , Toast.LENGTH_LONG).show();
 		//			return true;
 		//		}
 
 		//3.選擇使用網路提供器
 		// if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 		//  provider = LocationManager.NETWORK_PROVIDER;
 		//  return true;
 		// }
 
 		Toast.makeText(this,"無法取得定位..." , Toast.LENGTH_LONG).show();
 		return false;
 	}
 
 	/**
 	 * Method to decode polyline points
 	 * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
 	 * */
 	private List<LatLng> decodePoly (String encoded) {
 
 		List<LatLng> poly = new ArrayList<LatLng>();
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
 
 	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
 		@Override
 		public void onGpsStatusChanged(int event) {
 			switch (event) {
 			case GpsStatus.GPS_EVENT_STARTED:
 				Log.d(TAG, "GPS_EVENT_STARTED");
 				Toast.makeText(planroute.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
 				break;
 			case GpsStatus.GPS_EVENT_STOPPED:
 				Log.d(TAG, "GPS_EVENT_STOPPED");
 				Toast.makeText(planroute.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
 				break;
 			case GpsStatus.GPS_EVENT_FIRST_FIX:
 				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
 				Toast.makeText(planroute.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
 				break;
 			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 				Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
 				break;
 			}
 		}
 	};
 	LocationListener locationListener = new LocationListener(){
 		@Override
 		public void onLocationChanged(Location location) {
 			Log.e(TAG, "location: " + location.getLatitude() + "," + location.getLongitude());
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 
 		}
 	};
 
 	public class DirectionResponseObject {
 		public String status;
 		public Route[] routes;
 		String copyrights;
 		Poly overview_polyline;
 		String[] warnings;
 		int[] waypoint_order;
 		Bound bounds;
 
 		public class Route {
 			String summary;
 			String[] warnings;
 			Leg[] legs;
 			Poly overview_polyline;
 			String copyrights;
 
 			public class Leg {
 				public Step[] steps;
 				ValueText duration;
 				Time arrival_time, departure_time;
 
 				public class Step {
 					String travel_mode;
 					LatLng start_location, end_location;
 					Poly polyline;
 					ValueText duration;
 					String html_instructions;
 					ValueText distance;
 					Transit transit_details;
 
 					public class Transit {
 						Stop arrival_stop, departure_stop;
 						Time arrival_time, departure_time;
 						String headsign;
 						int num_stops;
 						TransitLine line;
 
 						public class Stop {
 							LatLng location;
 							String name;
 						}
 						public class TransitLine {
 							Agency agencies[];
 							String name;
 							String short_name;
 							Vehicle vehicle;
 
 							public class Vehicle {
 								String icon;
 								String name;
 								String type;
 							}
 
 							public class Agency {
 								String name;
 								String url;
 							}
 						}
 					}
 
 					public class Poly {
 						public String points;
 					}
 
 					public class ValueText {
 						int value;
 						String text;
 					}
 				}
 			}
 
 			public class Bound {
 				LatLng southwest, northeast;
 			}
 
 			public class Time {
 				int value;
 			}
 		}
 	}
 }
