 package com.finlay.geomonsters;
 
 import java.net.MalformedURLException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.finlay.geomonsters.R;
 import com.finlay.geomonsters.battle.BattleActivity;
 
 import io.socket.IOAcknowledge;
 import io.socket.IOCallback;
 import io.socket.SocketIO;
 import io.socket.SocketIOException;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private static final String TAG = "MainActivity";
 
 	private Button theButton = null;
 	private TextView theTextView = null;
 
 	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
 	private static final String URL = "http://204.191.142.13:8000/";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// set app to fullscreen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_main);
 
 		// layout item
 		theTextView = (TextView) findViewById(R.id.txtMessage);
 		theButton = (Button) findViewById(R.id.btnGetLocation);
 		theButton.setOnClickListener(new MyButtonClickListener());
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		Log.v(TAG, "onCreated");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private class MyButtonClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View arg0) {
 
 			theButton.setText("Retrieving position...");
 			locationListener = new MyLocationListener();
 
 			// Best provider
 			Criteria criteria = new Criteria();
 			String bestProvider = locationManager.getBestProvider(criteria, false);
 
 			locationManager.requestLocationUpdates(bestProvider, 60, 5, locationListener);
 		}
 
 	}
 
 	/*-----Listener class to get coordinates-----*/
 	private class MyLocationListener implements LocationListener {
 		private SocketIO socket;
 
 		public MyLocationListener() {
			socket = new SocketIO();
 		}
 
 		@Override
 		public void onLocationChanged(Location loc) {
 
			if (!socket.isConnected())
 				try {
 					theButton.setText("Connecting to server...");
 					socket.connect(URL, new MyIOCallback());
 				} catch (MalformedURLException e) {
 					Log.e(TAG, e.getMessage());
 				}

 
 			Log.v(TAG, "Location Changed");
 			Toast.makeText(getBaseContext(), "Location changed: Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();
 
 			String longitude ="" + loc.getLongitude();
 			String latitude = "" + loc.getLatitude();
 
 			theButton.setText("Querying database...");
 			showMessage(longitude + ", " + latitude);
 
 			// Send call to node server
 			sendLocation(longitude, latitude);
 
 
 		}
 
 		@Override
 		public void onProviderDisabled(String arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onProviderEnabled(String arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void sendMessage(String message) {
 			try {
 
 				JSONObject json = new JSONObject();
 				json.putOpt("message", message);
 				socket.emit("user message", json);
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		public void sendLocation(String longitude, String latitude) {
 			try {
 
 				JSONObject json = new JSONObject();
 				json.putOpt("longitude", longitude);
 				json.putOpt("latitude", latitude);
 				socket.emit("user position", json);
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	private class MyIOCallback implements IOCallback {
 		@Override
 		public void on(String event, IOAcknowledge arg1, Object... arg2) {
 			try {
 
 				Log.v(TAG, "Server triggered event '" + event + "'");
 				JSONObject obj = (JSONObject) arg2[0];
 
 				if (event.equals("message"))
 					Log.v(TAG, "Message from server: " + obj.getString("message"));
 				if (event.equals("result")) {
 					Log.v(TAG, "Return from server: " + obj.getString("value"));
 					launchBattle(obj.getString("value"));
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onConnect() {
 			Log.v(TAG, "Connection established");
 			showMessage("Connection established.");
 		}
 
 		@Override
 		public void onDisconnect() {
 			Log.v(TAG, "Connected terminated");
 
 		}
 
 		@Override
 		public void onError(SocketIOException arg0) {
 			Log.v(TAG, "onError " + arg0.getMessage());
 
 		}
 
 		@Override
 		public void onMessage(String arg0, IOAcknowledge arg1) {
 			Log.v(TAG, "onMessage from server: " + arg0);
 		}
 
 		@Override
 		public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
 			Log.v(TAG, "Server JSON Message: " + arg0.toString());
 
 		}
 
 	}
 
 	public void showMessage(final String s) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				theTextView.setText(s);
 			}
 		});
 	}
 	public void launchBattle(String s) {
 		locationManager.removeUpdates(locationListener);
 		try {
 			Intent intent = new Intent(this, BattleActivity.class);	
 			
 			// TODO: Look up creature in area & percent change of hitting
 			if (s.equals("forest"))
 				intent.putExtra("ENEMYNAME", "Bulbasaur");
 			else if (s.equals("water"))
 				intent.putExtra("ENEMYNAME", "Squirtle");
 			else if (s.equals("cemetery"))
 				intent.putExtra("ENEMYNAME", "Haunter");
 			else
 				intent.putExtra("ENEMYNAME", "FireType");
 			startActivity(intent);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
 
 
