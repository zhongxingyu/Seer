 package com.tealeaf.plugin.plugins;
 import java.util.Map;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import com.tealeaf.EventQueue;
 import com.tealeaf.GLSurfaceView;
 import com.tealeaf.TeaLeaf;
 import com.tealeaf.logger;
 import android.content.pm.PackageManager;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import java.util.HashMap;
 import java.util.List;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.location.GpsStatus;
 import android.location.Criteria;
 
 import com.tealeaf.plugin.IPlugin;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.util.Log;
 
 import com.tealeaf.EventQueue;
 import com.tealeaf.event.*;
 
 public class GeolocPlugin implements IPlugin, LocationListener, GpsStatus.Listener {
 	public class GeolocEvent extends com.tealeaf.event.Event {
 		boolean failed;
 		double longitude, latitude, accuracy;
 
 		public GeolocEvent() {
 			super("geoloc");
 			this.failed = true;
 		}
 
 		public GeolocEvent(double longitude, double latitude, double accuracy) {
 			super("geoloc");
 			this.failed = false;
 			this.longitude = longitude;
 			this.latitude = latitude;
 			this.accuracy = accuracy;
 		}
 	}
 
 	private boolean _high_accuracy;	// High accuracy mode enabled?
 	private Activity _activity;		// Activity
 	private Context _ctx;			// App context
 	private LocationManager _mgr;	// Location manager instance
 	private boolean _gps_ask;		// Has asked user to enable GPS in settings?
 	private Location _location;		// Last location
 	private boolean _gps_requested;	// Waiting for location updates from GPS?
 	private boolean _net_requested;	// Waiting for location updates from network?
 	private boolean _gps_wanted;	// Is GPS data requested?
 	private long _last_request;		// Last GPS request
 
 	@Override
 		public void onLocationChanged(Location loc) {
 			logger.log("{geoloc} Received location changed event:", loc != null ? loc.toString() : "(null)");
 
 			// Update last location
 			_location = loc;
 
 			if (loc != null) {
 				EventQueue.pushEvent(new GeolocEvent(loc.getLongitude(), loc.getLatitude(), loc.getAccuracy()));
 			} else {
 				EventQueue.pushEvent(new GeolocEvent());
 			}
 
 			// If it is time to stop requests,
			if (System.currentTimeMillis() - _last_request < 30 * 1000) { // 30 seconds
 				logger.log("{geoloc} Ending requests since the user has not requested any position data for a while");
 				stopRequests();
 				_gps_wanted = false;
 			}
 		}
 
 	@Override
 		public void onProviderDisabled(String provider) {
 			logger.log("{geoloc} Location provider disabled: ", provider);
 			// TODO: How should we react?
 		}
 
 	@Override
 		public void onProviderEnabled(String provider) {
 			logger.log("{geoloc} Location provider enabled: ", provider);
 			// TODO: How should we react?
 		}
 
 	@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			logger.log("{geoloc} Location provider status changed: ", provider, status);
 			// TODO: How should we react?
 		}
 
 	@Override
 		public void onGpsStatusChanged(int event) {
 			switch (event) {
 				case GpsStatus.GPS_EVENT_STARTED:
 					logger.log("{geoloc} GPS status: Started");
 					break;
 				case GpsStatus.GPS_EVENT_STOPPED:
 					logger.log("{geoloc} GPS status: Stopped");
 					break;
 				case GpsStatus.GPS_EVENT_FIRST_FIX:
 					logger.log("{geoloc} GPS status: First fix");
 					break;
 				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 					logger.log("{geoloc} GPS status: Satellite update");
 					break;
 				default:
 					logger.log("{geoloc} GPS status:", event);
 			}
 			// TODO: How should we react?
 		}
 
 	public GeolocPlugin() {
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 		_ctx = applicationContext;
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
 		_mgr = (LocationManager)_ctx.getSystemService(Context.LOCATION_SERVICE);
 		_activity = activity;
 
 		// Listen for GPS events
 		_mgr.addGpsStatusListener(this);
 
 		// Report whether or not our favorite providers are enabled
 		if (_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			logger.log("{geoloc} GPS provider is initially enabled.");
 		} else {
 			logger.log("{geoloc} GPS provider is initially DISABLED.");
 		}
 		if (_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			logger.log("{geoloc} Network provider is initially enabled.");
 		} else {
 			logger.log("{geoloc} Network provider is initially DISABLED.");
 		}
 
 		// Print out provider list
 		List<String> providers = _mgr.getAllProviders();
 		for (String provider : providers) {
 			LocationProvider info = _mgr.getProvider(provider);
 			logger.log("{geoloc} Location provider", provider, ":", info.toString());
 		}
 
 		// Get last locations
 		Location gps_last = _mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location net_last = _mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		// Take the best of the two options
 		if (gps_last != null) {
 			_location = gps_last;
 		} else {
 			_location = net_last;
 		}
 
 		// Report intiial position
 		if (_location == null) {
 			logger.log("{geoloc} Initial position not found");
 		} else {
 			logger.log("{geoloc} Initial position found:", _location.toString());
 		}
 
 		// Start requests immediately so that the location subsystem will warm up faster
 		//startRequests();
 	}
 
 	// Returns true if requests are started
 	public boolean startRequests() {
 		final LocationManager mgr = _mgr;
 		final GeolocPlugin thiz = this;
 
 		// If network provider is available,
 		if (!_net_requested && _mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			// Request network provider position udpate
 			logger.log("{geoloc} Requesting location from network provider");
 			_activity.runOnUiThread(new Runnable() {
 				public void run() {
 					mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, thiz);
 				}
 			});
 			_net_requested = true;
 		}
 
 		// If high accuracy is required, or network provider is disabled,
 		if (_high_accuracy || !_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			// Request GPS position update
 			if (!_gps_requested && _mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				logger.log("{geoloc} Requesting location from GPS provider");
 				_activity.runOnUiThread(new Runnable() {
 					public void run() {
 						mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, thiz);
 					}
 				});
 				_gps_requested = true;
 			}
 		}
 
 		return _gps_requested || _net_requested;
 	}
 
 	public void stopRequests() {
 		final LocationManager mgr = _mgr;
 		final GeolocPlugin thiz = this;
 
 		if (_gps_requested || _net_requested) {
 			logger.log("{geoloc} Removing location requests on pause");
 			_activity.runOnUiThread(new Runnable() {
 				public void run() {
 					mgr.removeUpdates(thiz);
 				}
 			});
 			_gps_requested = false;
 			_net_requested = false;
 		}
 	}
 
 	public void onResume() {
 		if (_gps_wanted) {
 			startRequests();
 		}
 	}
 
 	public void onStart() {
 		if (_gps_wanted) {
 			startRequests();
 		}
 	}
 
 	public void onPause() {
 		stopRequests();
 	}
 
 	public void onStop() {
 		stopRequests();
 	}
 
 	public void onDestroy() {
 		stopRequests();
 	}
 
 	public void onNewIntent(Intent intent) {
 	}
 
 	public void setInstallReferrer(String referrer) {
 	}
 
 	public void onActivityResult(Integer request, Integer result, Intent data) {
 	}
 
 	private void showGPSDisabledAlertToUser() {
 		TeaLeaf.get().runOnUiThread(new Runnable() {
 			public void run() {
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TeaLeaf.get());
 				alertDialogBuilder.setMessage("This application is requesting Location Services. Would you like to allow this?")
 								  .setCancelable(false)
 								  .setPositiveButton("Goto Settings Page To Enable GPS",
 									new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id){
 						Intent callGPSSettingIntent = new Intent(
 							android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						TeaLeaf.get().startActivity(callGPSSettingIntent);
 					}
 				});
 
 				alertDialogBuilder.setNegativeButton("Cancel",
 									new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 
 				AlertDialog alert = alertDialogBuilder.create();
 				alert.show();
 			}
 		});
 	}
 
 	public void onRequest(String jsonData) {
 		try {
 			if (!_gps_wanted) {
 				_gps_wanted = true;
 				startRequests();
 			}
 
 			_last_request = System.currentTimeMillis();
 
             JSONObject obj = new JSONObject(jsonData);
 			if (obj.has("enableHighAccuracy")) {
             	_high_accuracy = obj.getBoolean("enableHighAccuracy");
 			}
 
 			// If no GPS provider,
 			if (!_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				// If high accuracy requested or no providers enabled,
 				if (_high_accuracy || !_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 					// If not bugged user yet,
 					if (!_gps_ask) {
 						logger.log("{geoloc} Presenting GPS disabled alert to user");
 
 						showGPSDisabledAlertToUser();
 
 						_gps_ask = true;
 					}
 				}
 			}
 
 			boolean hasProvider = startRequests();
 
 			if (_location != null) {
 				logger.log("{geoloc} Reporting previous location");
 				EventQueue.pushEvent(new GeolocEvent(_location.getLongitude(), _location.getLatitude(), _location.getAccuracy()));
 			} else if (!hasProvider) {
 				EventQueue.pushEvent(new GeolocEvent());
 			} // Otherwise we wait
 		} catch (Exception e) {
 			logger.log("{geoloc} Exception:", e);
 			e.printStackTrace();
 			EventQueue.pushEvent(new GeolocEvent());
 		}
 	}
 
 	public void logError(String error) {
 	}
 
 	public boolean consumeOnBackPressed() {
 		return true;
 	}
 
 	public void onBackPressed() {
 	}
 }
 
