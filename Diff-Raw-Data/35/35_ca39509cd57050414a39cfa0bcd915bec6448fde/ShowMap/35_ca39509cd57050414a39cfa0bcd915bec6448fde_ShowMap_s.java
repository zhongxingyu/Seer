 package com.brousalis;
 
 import java.util.HashSet;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class ShowMap extends MapActivity {
 
 	// SharedPreference Strings
 	public static final String SAVED_MAP_STATE = "SavedMapState";
 	public static final String SAVED_MAP_LAT = "SavedMapLat";
 	public static final String SAVED_MAP_LONG = "SavedMapLong";
 	public static final String SAVED_MAP_ZOOM = "SavedMapZoom";
 	public static final String SAVED_DEFAULT_ZOOM = "DefaultZoom";
 	public static final String SAVED_ZOOM_ON_CENTER = "ZoomOnCenter";
 	public static final String REGISTERED_DEVICE = "RegisteredDevice";
 	public static Boolean BETA_MODE = false;
 	private static String UNIQUE_ID = "";
 
 	// Default Values
 	// DEFAULT_MAP_ZOOM moved to prefs - 9/29/10 estokes
 	// It is still needed here, see CSN01 (CSN = Code Side Note)
 	public static final int DEFAULT_MAP_ZOOM = 17;
 	// TODO: Figure out some good value to init these to
 	public static final int DEFAULT_MAP_LAT = 0;
 	public static final int DEFAULT_MAP_LONG = 0;
 
 	/**
 	 * The Standard Location manager for an Android Device
 	 */
 	private LocationManager _locMgr;
 
 	/**
 	 * The Map controller that allows us to retrieve or send information to our
 	 * _mapView
 	 */
 	private MapController _mapController;
 
 	/**
 	 * MapView which will display trails to users
 	 */
 	private MapView _mapView;
 
 	/**
 	 * The settings object where both custom settings are stored as well as
 	 * settings set in the options dialog
 	 */
 	private SharedPreferences _settings;
 
 	private DataHandler _dataHandler;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.main);
 		this._mapView = (MapView) findViewById(R.id.mapView);
 		this._mapView.setBuiltInZoomControls(true);
 
 		this._mapController = this._mapView.getController();
 
 		this._locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		BETA_MODE = Boolean.parseBoolean(this.getString(R.string.beta));
 		if(BetaChecker.isUpToDate(
 				BETA_MODE,
 				this.getString(R.string.beta_check_url)
 						+ this.getString(R.string.beta_version))) {
 			//showOutOfDateDialog();
 		}
 		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		UNIQUE_ID = mTelephonyMgr.getDeviceId();
 
 		this._settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		Log.w("MTM",
 				"MTM Settings: "
 						+ this._settings.getBoolean(REGISTERED_DEVICE, false));
 		String idCheckResult = BetaChecker.checkUser(
 				this.getString(R.string.register_device_url), UNIQUE_ID);
 		Boolean validUser = idCheckResult.equals("registered");
 		Boolean bannedUser = idCheckResult.equals("banned");
 		if (bannedUser && BETA_MODE) {
 			showBannedUserDialog();
 		}
 		// if(!this._settings.getBoolean(REGISTERED_DEVICE, false)) {
 		else if (!validUser && BETA_MODE) {
 			showNewBetaUserDialog(this.getString(R.string.register_device_url));
 		}
 		//showBannedUserDialog();
 		showOutOfDateDialog();
 		// }
 		Log.w("MTM", "MTM: onCreate()");
 	}
 
 	private void showOutOfDateDialog() {
 		final BetaDialog updateNeeded = new BetaDialog(ShowMap.this, R.layout.out_of_date);
 		updateNeeded.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				finish();
 			}
 		});
		updateNeeded.setCancelAction(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				takeUserToNewDownload();
				updateNeeded.dismiss();
 			}
 		});
 		
 		updateNeeded.show();
 	}
	private void takeUserToNewDownload() {
 		showBannedUserDialog();
 	}
 	private void showBannedUserDialog() {
		final Dialog bannedUser = new Dialog(ShowMap.this);
		bannedUser.requestWindowFeature(Window.FEATURE_NO_TITLE);
		bannedUser.setContentView(R.layout.banned_user);
		bannedUser.setCancelable(true);
 		bannedUser.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				finish();
 			}
 		});
 		bannedUser.setOnDismissListener(new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				finish();
 			}
 		});
 		Button cancelButton = (Button) bannedUser
 				.findViewById(R.id.beta_user_banned_cancel);
 		cancelButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		bannedUser.show();
 	}
 
 	private void showNewBetaUserDialog(String registerUrl) {
 		final Dialog newUser = new Dialog(ShowMap.this);
 		final String registrationUrl = registerUrl;
 		// Make sure to remove the window title before doing ANYTHING else, or
 		// you'll get a pretty Force Close message.
 		newUser.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		newUser.setContentView(R.layout.new_beta_user);
 		newUser.setCancelable(false);
 		
 		newUser.setOnDismissListener(new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				finish();
 			}
 		});
 		
 		final EditText name = (EditText) newUser
 				.findViewById(R.id.beta_user_name);
 		name.setSingleLine();
 		Button cancelButton = (Button) newUser
 				.findViewById(R.id.beta_user_cancel);
 		cancelButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		final Button submitButton = (Button) newUser
 				.findViewById(R.id.beta_user_submit);
 		submitButton.setEnabled(false);
 		submitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				BetaChecker.registerUser(registrationUrl, UNIQUE_ID, name
 						.getEditableText().toString());
 				registerDeviceLocally();
 				newUser.setOnDismissListener(new OnDismissListener() {
 					@Override
 					public void onDismiss(DialogInterface dialog) {
 						
 					}
 				});
 				newUser.dismiss();
 			}
 		});
 		name.setHintTextColor(Color.LTGRAY);
 		name.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (name.getEditableText().length() > 0) {
 					submitButton.setEnabled(true);
 				} else {
 					submitButton.setEnabled(false);
 				}
 
 				return false;
 			}
 
 		});
 
 		newUser.show();
 
 	}
 
 	public void registerDeviceLocally() {
 		SharedPreferences.Editor editor = this._settings.edit();
 		editor.putBoolean(REGISTERED_DEVICE, true);
 		editor.commit();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		Log.w("MTM", "MTM: conFigChanged()");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.w("MTM", "MTM: onPause()");
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// Always save the location, regardless if the user wants to go here
 		SharedPreferences.Editor editor = this._settings.edit();
 		editor.putInt(SAVED_MAP_LAT, this._mapView.getMapCenter()
 				.getLatitudeE6());
 		editor.putInt(SAVED_MAP_LONG, this._mapView.getMapCenter()
 				.getLongitudeE6());
 		editor.putInt(SAVED_MAP_ZOOM, this._mapView.getZoomLevel());
 		editor.commit();
 		Log.w("MTM", "MTM: onStop()");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.w("MTM", "MTM: onDestroy()");
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// drawTrail();
 		Log.w("MTM", "MTM: onResume()");
 	}
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		Log.w("MTM", "MTM: onRestart()");
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		// Get the GPS Location if it's available, otherwise use the network
 		// Location.
 		// By doing this, we don't force users to open up the GPS on program
 		// start.
 		// We'll also load here rather than oncreate to ensure settings are
 		// loaded.
 		this.initLocation();
 		this.initializeParser();
 		this.drawTrail();
 		Log.w("MTM", "MTM: onStart()");
 	}
 
 	/**
 	 * Initialize the XML Parser and Parse the data from the url.
 	 */
 	private void initializeParser() {
 		this._dataHandler = new DataHandler();
 		this._dataHandler.parseDocument();
 	}
 
 	/**
 	 * Resolves all connections for all trails pulled down
 	 */
 	private void drawTrail() {
 		HashSet<Trail> trails = this._dataHandler.getParsedTrails();
 		this._mapView.getOverlays().clear();
 		for (Trail t : trails) {
 			t.resolveConnections();
 			this._mapView.getOverlays().addAll(t.getTrailPoints());
 		}
 	}
 
 	/**
 	 * This method MUST be here. Google's TOS requires it. Still not sure if we
 	 * have to turn this to true if we're displaying custom trail routes...
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_quit:
 			this.finish();
 			break;
 		case R.id.menu_settings:
 			Intent settings = new Intent(this, TrailPrefs.class);
 			this.startActivity(settings);
 			break;
 		case R.id.menu_center:
 			this.centerMapOnCurrentLocation(_settings.getBoolean(
 					SAVED_ZOOM_ON_CENTER, true));
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * Initialize the Location. This happens when the app starts from cold or if
 	 * the user does not have save location
 	 */
 	private void initLocation() {
 		GeoPoint p;
 		this._settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 
 		if (this._settings.getBoolean(SAVED_MAP_STATE, false)) {
 			p = new GeoPoint(this._settings.getInt(SAVED_MAP_LAT,
 					DEFAULT_MAP_LAT), this._settings.getInt(SAVED_MAP_LONG,
 					DEFAULT_MAP_LONG));
 
 			this._mapController.animateTo(p);
 			// Zoom to the Saved Map zoom. If none is present, get the Saved
 			// Default zoom from the prefs.
 			// If that's not there, Use the DefaultMapZoom. That last one should
 			// NEVER happen
 			this._mapController.setZoom(this._settings.getInt(SAVED_MAP_ZOOM,
 					Integer.parseInt(this._settings.getString(
 							SAVED_DEFAULT_ZOOM,
 							Integer.toString(DEFAULT_MAP_ZOOM)))));
 			this._mapView.setSatellite(true);
 			this._mapView.invalidate();
 		} else {
 			this.centerMapOnCurrentLocation(false);
 		}
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
 		super.dispatchTouchEvent(ev);
 
 		return true;
 	};
 
 	/**
 	 * Centers the map on the current location (GPS, Cell or WiFi, whichever is
 	 * more accurate and present)
 	 * 
 	 * @param zoomOnCenter
 	 *            Should the zoom level be changed to what is defined in prefs
 	 */
 	private void centerMapOnCurrentLocation(boolean zoomOnCenter) {
 		GeoPoint p;
 		Location networkLoc = _locMgr
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		Location gpsLoc = _locMgr
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location recentLoc = (gpsLoc != null) ? gpsLoc : networkLoc;
 		// CSN01
 		// We can't just pull this from prefs, as this function is shared
 		// It's sometimes called by the initializer, not the "center me" button.
 		if (zoomOnCenter) {
 			this._mapController.setZoom(Integer.parseInt(_settings.getString(
 					SAVED_DEFAULT_ZOOM, DEFAULT_MAP_ZOOM + "")));
 		}
 
 		if (recentLoc != null) {
 			p = new GeoPoint((int) (recentLoc.getLatitude() * 1E6),
 					(int) (recentLoc.getLongitude() * 1E6));
 			this._mapController.animateTo(p);
 			this._mapView.setSatellite(true);
 			this._mapView.invalidate();
 		}
 	}
 }
