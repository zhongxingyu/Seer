 package ru.allgage.geofriend;
 
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class MapActivity extends FragmentActivity {
 	private static final String SENDER_ID = "119606192268";
 	private GoogleMap mMap;
 	LocationManager locationManager;
 	LocationListener listener;
 	long lastTime = 0;
 	boolean isFirstUpdate = true;
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		 
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 		mMap = mapFragment.getMap();
 		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		
 		if(mMap != null) {
 			mMap.setOnInfoWindowClickListener(new clickWindowHandler(this));
 			
 			UpdateMap.setMap(mMap);
 			new UpdateMap().execute("allStatuses");
 		}
 		Monitor.getInstance().flag = true;
 		isFirstUpdate = false;
 		
 		listener = new LocationListener() {
 
 		    @Override
 		    public void onLocationChanged(Location location) {
 		    	if(isFirstUpdate) {
 		    		Toast.makeText(getApplicationContext(), "Location detected", Toast.LENGTH_LONG).show();
 		    		isFirstUpdate = false;
 		    	}
		    	new SendTask().execute("updateStatus", location.getLatitude(), loc.getLongitude(), status);
 		        new SendTask().execute("updatePosition", location.getLatitude(), location.getLongitude());
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
 		};
 		
 		/**locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 		        600000,          // 600-second interval.
 		        0,             // 10 meters.
 		        listener);**/
 		SharedPreferences preferences = getSharedPreferences("settings", 0);
 		
 		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 240000, 20, listener);
 		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 	    {
 			showDialog(0);
 			
 			finish();
 	    }
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 5f, listener);
 		
 		//toCallAsynchronous();
 		
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		final String regId = GCMRegistrar.getRegistrationId(this);
 		
 		
 		//if (regId.equals("")) {
 		  GCMRegistrar.register(this, SENDER_ID);
 		//}
 	}
 	
 	protected Dialog onCreateDialog(int id) {
 	      if (id == 0) {
 	        AlertDialog.Builder adb = new AlertDialog.Builder(this);
 	        // 
 	        adb.setTitle("GPS");
 	        // 
 	        adb.setMessage("Enable GPS");
 	        // 
 	        adb.setIcon(android.R.drawable.ic_dialog_info);
 	        //   
 	        adb.setNeutralButton("OK", null);
 	        //  
 	        return adb.create();
 	      }
 	      return super.onCreateDialog(id);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 	    return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.item1:
 	        Intent intent = new Intent(this, StatusActivity.class);
 	        startActivityForResult(intent, 1);
 	        return true;
 	    case R.id.item2:
 	    	finish();
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (data == null) return;
 	    String status = data.getStringExtra("status");
 	    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    if(loc != null)
 	    	new SendTask().execute("updateStatus", loc.getLatitude(), loc.getLongitude(), status);
 	    
 	    Editor ed = getSharedPreferences("settings", 0).edit();
 	    ed.putString("status", status);
 	    ed.commit();
 	}
 	
 	
 	@Override
     public void onBackPressed()
     {
 		moveTaskToBack(true);
     }
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	    super.onConfigurationChanged(newConfig);
 	}
 	
 	protected void onDestroy() {
 		super.onDestroy();
 		locationManager.removeUpdates(listener);
     	GCMRegistrar.unregister(this);
     	new CloseTask(this).execute();
 	}
 
 }
 
 class clickWindowHandler implements OnInfoWindowClickListener {
 
 	Activity parent;
 	
 	clickWindowHandler(Activity p) {
 		parent = p;
 	}
 	
 	@Override
 	public void onInfoWindowClick(Marker arg0) {
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(parent, UserActivity.class);
 		intent.putExtra("login", arg0.getTitle());
 		parent.startActivity(intent);
 	}
 	
 }
 
