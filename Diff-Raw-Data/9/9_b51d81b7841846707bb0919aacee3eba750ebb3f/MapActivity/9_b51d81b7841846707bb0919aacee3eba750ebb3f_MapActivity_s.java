 package dk.markedsbooking.loppemarkeder;
 
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import dk.markedsbooking.loppemarkeder.dummy.MarkedContent;
 import dk.markedsbooking.loppemarkeder.model.MarkedItem;
 
 public class MapActivity extends SherlockFragmentActivity implements
 		LocationListener, OnInfoWindowClickListener {
 
 	GoogleMap googleMap;
 	LocationManager locationManager;
 	String provider;
 	List<MarkedItem> markedList;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		// Getting Google Play availability status
 		int status = GooglePlayServicesUtil
 				.isGooglePlayServicesAvailable(getBaseContext());
 
 		// Showing status
 		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
 													// not available
 
 			int requestCode = 10;
 			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
 					requestCode);
 			dialog.show();
 
 		} else { // Google Play Services are available
 
 			// Getting reference to the SupportMapFragment of activity_main.xml
 			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map);
 
 			// Getting GoogleMap object from the fragment
 			googleMap = fm.getMap();
 
 			// Enabling MyLocation Layer of Google Map
 			googleMap.setMyLocationEnabled(true);
 
 			googleMap.setOnInfoWindowClickListener(this);
 
 			// Getting LocationManager object from System Service
 			// LOCATION_SERVICE
 			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
 			// Creating a criteria object to retrieve provider
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			criteria.setAltitudeRequired(false);
 			criteria.setBearingRequired(false);
 			criteria.setCostAllowed(true);
 			criteria.setPowerRequirement(Criteria.POWER_LOW);
 
 			// Getting the name of the best provider
 			provider = locationManager.getBestProvider(criteria, true);
 
 			// Getting Current Location
 			Location location = locationManager.getLastKnownLocation(provider);
 
 			// add marked markers
 			// add markets
 			markedList = MarkedContent.ITEMS;
 			for (MarkedItem item : markedList) {
 				MarkerOptions markerOption = new MarkerOptions()
 				.position(new LatLng(item.latitude, item.longitude))
				.title(item.name +" - "+item.getDate())
				.snippet(item.address).anchor(0.5f, 0.5f);
 
 				Marker marker = googleMap.addMarker(markerOption);
 
 				item.markerId = marker.getId();
 			}
 			
 			if (location != null) {
 				onLocationChanged(location);
 			} else {
 				showLocationNotFoundAlertToUser();
 			}
 		}
 	}
 
 	private void showLocationNotFoundAlertToUser() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setMessage(R.string.current_location_not_found)
 				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 
 		AlertDialog ad = alertDialogBuilder.create();
 		ad.show();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	private void setupActionBar() {
 		// Show the Up button in the action bar.
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.map, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
		TextView tvLocation = (TextView) findViewById(R.id.tv_location);

 		// Getting latitude of the current location
 		double latitude = location.getLatitude();
 
 		// Getting longitude of the current location
 		double longitude = location.getLongitude();
 
 		// Creating a LatLng object for the current location
 		LatLng latLng = new LatLng(latitude, longitude);
 
 		// Showing the current location in Google Map
 		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 
 		// Zoom in the Google Map
 		// Zoom in, animating the camera.
 		googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
 
 		// Setting latitude and longitude in the TextView tv_location
		tvLocation.setText("Latitude:" + latitude + ", Longitude:" + longitude);
 		Toast.makeText(this, "onLocationChanged: Provider " + provider + " has been selected. Location available "+location.getLatitude() +" "+location.getLongitude() +" \nTime "+new Date(location.getTime()), Toast.LENGTH_LONG).show();
 
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
 
 	/* Request updates at startup */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// every 2 seconds and 10 meter in change
 		locationManager.requestLocationUpdates(provider, 2000, 10, this);
 	}
 
 	/* Remove the locationlistener updates when Activity is paused */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		String id = marker.getId();
 
 		MarkedItem item = getByMarkerId(id);
 
 		Toast.makeText(
 				this,
 				"Marker info " + marker.getTitle() + " " + id + " Item "
 						+ item.name, Toast.LENGTH_SHORT).show();
 
 		
 		 Intent detailIntent = new Intent(this, MarketDetailActivity.class);
 		 detailIntent.putExtra(MarketDetailFragment.ARG_ITEM_ID, item.id);
 		 startActivity(detailIntent);
 		
 	}
 
 	public MarkedItem getByMarkerId(String markerId) {
 		MarkedItem res = null;
 		for (MarkedItem item : markedList) {
 			if (markerId.equals(item.markerId)) {
 				res = item;
 				break;
 			}
 		}
 		return res;
 	}
 
 }
