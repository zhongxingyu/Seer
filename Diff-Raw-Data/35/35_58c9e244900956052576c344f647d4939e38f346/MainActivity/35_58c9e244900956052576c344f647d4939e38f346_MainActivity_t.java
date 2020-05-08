 package com.hillbomber;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
 import android.view.Menu;
import android.view.View;
import android.widget.Button;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class MainActivity extends MapActivity {
 
 	private SharedPreferences userPreferences;
 
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 
 	private MapView mapView;
	private View routeView; 
	private Button refreshButton, newButton, startButton, endButton;  
 
 	private MapController mapController;
 	private GoogleParser googleParser;
 
 	private Facebook facebook = new Facebook("468347859863144");
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
		LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		routeView = inflater.inflate(R.layout.route, null);
		routeView.setVisibility(View.GONE);
		addContentView(routeView, new LayoutParams());
		
 		mapView = (MapView) findViewById(R.id.mapview);
 		userPreferences = getPreferences(MODE_PRIVATE);
 		String access_token = userPreferences.getString("access_token", null);
 		long expires = userPreferences.getLong("access_expires", 0);
 		if (access_token != null && expires != 0) {
 			facebook.setAccessToken(access_token);
 			facebook.setAccessExpires(expires);
 		}
 
 		if (!facebook.isSessionValid()) {
 			facebook.authorize(this, new DialogListener() {
 				public void onComplete(Bundle values) {
 					SharedPreferences.Editor editor = userPreferences.edit();
                     editor.putString("access_token", facebook.getAccessToken());
                     editor.putLong("access_expires", facebook.getAccessExpires());
                     editor.commit();
 				}
 
 				public void onFacebookError(FacebookError e) {
 //					Log.e("Facebook Error", e.toString());
 				}
 
 				public void onError(DialogError e) {
 //					Log.e("Facebook Error", e.toString());
 				}
 
 				public void onCancel() {
 				}
 			});
 		}
 
 		googleParser = new GoogleParser();
 
 		BufferedReader in = null;
 		String line = null;
 		try {
 			in = new BufferedReader(
 					new InputStreamReader(
 							getConnection("http://hillbomber.herokuapp.com/trails.json")));
 			while ((line = in.readLine()) != null) {
 				JSONArray routes = (JSONArray) new JSONTokener(line)
 						.nextValue();
 				for (int i = 0; i < routes.length(); i++) {
 					JSONObject route = routes.getJSONObject(i);
 					String url = googleParser.directions(new GeoPoint(
 							(int) (route.getDouble("s_lat") * 1E6),
 							(int) (route.getDouble("s_long") * 1E6)),
 							new GeoPoint(
 									(int) (route.getDouble("e_lat") * 1E6),
 									(int) (route.getDouble("e_long") * 1E6)));
 					RouteOverlay routeOverlay = new RouteOverlay(
 							googleParser.parse(getConnection(url)), Color.BLUE);
 					mapView.getOverlays().add(routeOverlay);
 				}
 				mapView.invalidate();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapController = mapView.getController();
 		mapController.setZoom(18); // Fixed Zoom Level
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new LongboardLocationListener();
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, locationListener);
 
 		// Get the current location in start-up
 		centerLocation(new GeoPoint((int) (locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER)
 				.getLatitude() * 1000000), (int) (locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER)
 				.getLongitude() * 1000000)));
 	}
 	
 	@Override
 	public void onResume() {    
         super.onResume();
         facebook.extendAccessTokenIfNeeded(this, null);
     }
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		facebook.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
	public void onNewRouteClicked(View v) {
		routeView.setVisibility(View.VISIBLE);
	}
	
	public void onEndRouteClicked(View v) {
		routeView.setVisibility(View.VISIBLE);
	}
	
	public void onRefreshClicked(View v) {
	}
	
 	private InputStream getConnection(String url) {
 		InputStream is = null;
 		try {
 			URLConnection conn = new URL(url).openConnection();
 			is = conn.getInputStream();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return is;
 	}
 
 	private class LongboardLocationListener implements LocationListener {
 
 		public void onLocationChanged(Location argLocation) {
 			// TODO Auto-generated method stub
 			GeoPoint geoPoint = new GeoPoint(
 					(int) (argLocation.getLatitude() * 1000000),
 					(int) (argLocation.getLongitude() * 1000000));
 
 			centerLocation(geoPoint);
 		}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 		}
 	}
 
 	private void centerLocation(GeoPoint centerGeoPoint) {
 		mapController.animateTo(centerGeoPoint);
 	}
 
 }
