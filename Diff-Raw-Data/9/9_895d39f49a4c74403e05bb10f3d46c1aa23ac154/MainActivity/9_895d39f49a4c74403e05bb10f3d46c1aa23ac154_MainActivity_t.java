 package pro.schmid.android.whereareyou;
 
 import java.util.List;
 
 import pro.schmid.android.androidonfire.Firebase;
 import pro.schmid.android.androidonfire.FirebaseEngine;
 import pro.schmid.android.androidonfire.callbacks.FirebaseLoaded;
 import pro.schmid.android.whereareyou.NameFragment.NameDialogListener;
 import pro.schmid.android.whereareyou.utils.Constants;
 import pro.schmid.android.whereareyou.utils.Utils;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 public class MainActivity extends FragmentActivity implements NameDialogListener {
 
 	private FirebaseMapManager mFirebaseMapManager;
 	private FirebaseEngine mEngine;
 	private Firebase mCurrentFirebase;
 	private String mUsername;
 	private String mRoomFromIntent;
 
 	private LocationManager mLocationManager;
 	private GoogleMap mMap;
 
 	private SharedPreferences mPreferences;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_main);
 		setProgressBarIndeterminateVisibility(true);
 
 		int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		if (googlePlayServicesAvailable != ConnectionResult.SUCCESS) {
 			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesAvailable, this, -1);
 			errorDialog.show();
 			return;
 		}
 
 		Uri data = getIntent().getData();
 		if (data != null) {
 			List<String> params = data.getPathSegments();
 			mRoomFromIntent = params.get(0);
 		}
 
 		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
 		getUserName();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		EasyTracker.getInstance().activityStart(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
 
 		if (mMap == null) {
 			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 			mMap.setMyLocationEnabled(true);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		mLocationManager.removeUpdates(locationListener);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		EasyTracker.getInstance().activityStop(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (mEngine != null) {
 			mEngine.onDestroy();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		MenuItem findItem = menu.findItem(R.id.menu_share_group);
 		findItem.setVisible(mCurrentFirebase != null);
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.menu_share_group:
 				shareGroup();
 				return true;
 
 			case R.id.menu_prefs:
 				Intent intent = new Intent(this, SettingsActivity.class);
 				startActivity(intent);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void shareGroup() {
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, "Come to my group: " + Constants.BASE_URL + mCurrentFirebase.name());
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 	}
 
 	private void startApplication() {
 		loadFirebase();
 	}
 
 	private void loadFirebase() {
 		mEngine = FirebaseEngine.getInstance();
 		mEngine.setLoadedListener(new FirebaseLoaded() {
 
 			@Override
 			public void firebaseLoaded() {
				Firebase firebase = mEngine.newFirebase(Constants.FIREBASE_URL).child(Constants.PROTOCOL_VERSION);
 
 				if (mRoomFromIntent != null) {
 					mCurrentFirebase = firebase.child(mRoomFromIntent);
 				} else {
 					mCurrentFirebase = firebase.push();
 				}
 
 				mFirebaseMapManager = new FirebaseMapManager(MainActivity.this, mMap, mCurrentFirebase, mUsername);
 
 				Location myLocation = mMap.getMyLocation();
 				if (myLocation != null) {
 					mFirebaseMapManager.setMyLocation(myLocation);
 				}
 
 				invalidateOptionsMenu();
 				setProgressBarIndeterminateVisibility(false);
 			}
 		});
 		mEngine.loadEngine(this);
 	}
 
 	// Define a listener that responds to location updates
 	private final LocationListener locationListener = new LocationListener() {
 
 		private boolean mFirstCenter = true;
 
 		@Override
 		public void onLocationChanged(Location location) {
 			if (mFirebaseMapManager != null) {
 				mFirebaseMapManager.setMyLocation(location);
 
 				if (mFirstCenter) {
 					mFirstCenter = false;
 
 					CameraUpdate newLatLngZoom = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), Constants.MAP_ZOOM);
 					mMap.animateCamera(newLatLngZoom);
 				}
 			}
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 	};
 
 	private void getUserName() {
 		mUsername = mPreferences.getString(Constants.PREF_NAME, null);
 
 		if (mUsername == null) {
 			// Try to get the username from the accounts
 			mUsername = Utils.getAccountUsername(this);
 
 			if (mUsername == null) {
 				DialogFragment newFragment = NameFragment.newInstance();
 				newFragment.show(getSupportFragmentManager(), "name");
 			} else {
 				setUsername(mUsername);
 			}
 		} else {
 			startApplication();
 		}
 	}
 
 	@Override
 	public void onDialogPositiveClick(NameFragment dialog) {
 		mUsername = dialog.getUsername();
 		setUsername(mUsername);
 	}
 
 	private void setUsername(String username) {
 		mPreferences.edit().putString(Constants.PREF_NAME, mUsername).commit();
 		startApplication();
 	}
 
 }
