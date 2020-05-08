 package com.chaseit.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.chaseit.ParseHelper;
 import com.chaseit.R;
 import com.chaseit.models.Hunt;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.LatLngBounds.Builder;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.SaveCallback;
 
 public class CreateChaseLocationsActivity extends FragmentActivity implements LocationListener {
 	public static int CREATE_CHASE_LOCATIONS_REQ_CODE = 500;
 
 	private LocationManager mLocationManager;
 	private EditText etSearchName;
 	private	ImageButton btnSearch;
 	private Button btnDiscard;
 	private Button btnDone;
 	private GoogleMap fCreateChaseMap;
 	private AlertDialog alert;
 
 
 	private Hunt chase;
 	private List<LatLng> mapPoints;
 	private LatLng currentPoint;
 	private String friendlyName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_create_chase_locations);
 		mapPoints = new ArrayList<LatLng>();
 		initlializeElements();
 		initilizeMap();
 
 
 		Intent i = getIntent();
 		String chaseId = i.getStringExtra("chaseId");
 		ParseHelper.getHuntByObjectId(chaseId, new GetCallback<Hunt>() {
 			@Override
 			public void done(Hunt object, ParseException e) {
 				if (e == null) {
 					chase = object;
 				} else {
 					e.printStackTrace();
 				}
 			}
 		});
 		setupOnMapLongPressAction();
 		setupOnSearchAction();
 		setupOnDiscardAction();
 		setupOnDoneAction();
 		setupLocationInformation();
 	}
 
 	private void setupLocationInformation() {
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		android.location.Location location = mLocationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
 			fCreateChaseMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
 			fCreateChaseMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 		} else {
 			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		}
 	}
 
 	private void setupOnDoneAction() {
 		btnDone.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(mapPoints.size() < 2){
 					showAlertDialog();
 				} else {
 					double totalDistance = calculateChaseDistance();
 					chase.setTotalDistance(totalDistance);
 					chase.saveInBackground(new SaveCallback() {
 						@Override
 						public void done(ParseException e) {
 							if(e == null){
 								Intent resultIntent = new Intent();
 								resultIntent.putExtra("showSummary", true);
 								setResult(RESULT_OK, resultIntent);
 								finish();						
 							}
 						}
 					});
 				}	
 			}
 		});
 	}
 	private void setupOnDiscardAction() {
 		btnDiscard.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				ParseHelper.deleteHunt(chase, null);
 				setResult(RESULT_OK);
 				finish();				
 			}
 		});
 	}
 	
 	@Override
 	public void onBackPressed() {
 		ParseHelper.deleteHunt(chase, null);
 		setResult(RESULT_OK);
 		finish();
 	}
 
 	private void showAlertDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(
 				CreateChaseLocationsActivity.this);
 		builder.setMessage(
 				"Please add at least two locations for your Chase!")
 				.setCancelable(false)
 				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						alert.dismiss();
 					}
 				});
 		alert = builder.create();
 		alert.show();
 	}
 
 	private double calculateChaseDistance() {
 		if(mapPoints == null || mapPoints.size() < 2){
 			return 0;
 		}
 		double totalDist = 0;
 		LatLng p1, p2;
 		p1 = mapPoints.get(0);
 		int i = 1;
 		float [] results;
 		while(i < mapPoints.size()){
 			p2 = mapPoints.get(i);
 			results = new float[10];
 			Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
 			totalDist += results[0];
 			p1 = p2;
 			i++;
 		}
 		return totalDist;
 	}
 
 	private void setupOnSearchAction() {
 		btnSearch.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				String searchText = etSearchName.getText().toString();
 				if(StringUtils.isNotBlank(searchText)){
 					new GeocoderTask(searchText, new LatLng(0, 0)).execute(this);
 				}
 			}
 		});
 	}
 
 
 	private void setupOnMapLongPressAction(){
 		fCreateChaseMap.setOnMapLongClickListener(new OnMapLongClickListener() {
 			@Override
 			public void onMapLongClick(LatLng point) {
 				new GeocoderTask("", point).execute(this);
 			}
 
 		});
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 			friendlyName = data.getStringExtra("friendlyName");
			double latitude = data.getDoubleExtra("latitude", 0.0);
			double longitude = data.getDoubleExtra("longitude", 0.0);
 			// add current point to the list of locations for this chase
 			if(mapPoints.size() == 0){
 				chase.setLocality(friendlyName);
				chase.setStartLocation(new ParseGeoPoint(latitude, longitude));
 
 			}
			LatLng point = new LatLng(latitude, longitude);
			
			mapPoints.add(point);
 			fCreateChaseMap.addMarker(new MarkerOptions()
			.position(point)
 			.title((mapPoints.size()) + ". " + friendlyName)
 			.icon(BitmapDescriptorFactory
 					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 			drawPolygon();
 		} else {
 			// center on the last position
 			if (mapPoints.size() > 0) {
 				fCreateChaseMap.moveCamera(CameraUpdateFactory.newLatLng(mapPoints.get(mapPoints.size() - 1)));
 				fCreateChaseMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 			} else {
 				fCreateChaseMap.moveCamera(CameraUpdateFactory.newCameraPosition(fCreateChaseMap.getCameraPosition()));
 				fCreateChaseMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 			}
 		}
 	}
 
 	private void drawPolygon() {
 		PolylineOptions rectOptions = new PolylineOptions();
 		Builder builder = new LatLngBounds.Builder();
 		for (LatLng point : mapPoints) {
 			builder.include(point);
 			rectOptions.add(point).width(5).color(Color.BLUE).geodesic(true);
 			fCreateChaseMap.addPolyline(rectOptions);
 		}
 		// move the camera to show the whole chase
 		fCreateChaseMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
 				builder.build(), 50));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_chase_locations, menu);
 		return true;
 	}
 
 	/**
 	 * function to load map. If map is not created it will create it for you
 	 * */
 	private void initilizeMap() {
 		if (fCreateChaseMap == null) {
 			fCreateChaseMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fCreateChaseMap)).getMap();
 			fCreateChaseMap.setMyLocationEnabled(true);
 			if (fCreateChaseMap == null) {
 				Log.d("DEBUG", "Sorry! unable to create maps");
 			}
 		}
 	}
 
 	private void initlializeElements() {
 		etSearchName = (EditText) findViewById(R.id.etSearchName);
 		btnSearch = (ImageButton) findViewById(R.id.btnSearch);
 		btnDiscard = (Button)findViewById(R.id.btnDiscard);
 		btnDone = (Button)findViewById(R.id.btnDone);
 		etSearchName.bringToFront();
 		btnSearch.bringToFront();
 		btnDiscard.bringToFront();
 		btnDone.bringToFront();		
 	}
 
 	@Override
 	public void onLocationChanged(android.location.Location location) {
 		if (location != null) {
 			fCreateChaseMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
 			fCreateChaseMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 			mLocationManager.removeUpdates(this);
 		}		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) { }
 	@Override
 	public void onProviderEnabled(String provider) { }
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) { }
 
 	// An AsyncTask class for accessing the GeoCoding Web Service
 	private class GeocoderTask extends AsyncTask<Object, Void, List<Address>>{ 
 		private String locationName;
 		private LatLng location;
 
 		public GeocoderTask(String string, LatLng point) {
 			locationName = string;
 			location = point;
 		}
 
 
 		@Override
 		protected List<Address> doInBackground(Object... objects) {
 			// Creating an instance of Geocoder class
 			Geocoder geocoder = new Geocoder(getBaseContext());
 			List<Address> addresses = null;
 
 			try {
 				if(StringUtils.isBlank(locationName)){
 					addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
 				} else {
 					// Getting an Addres that matches the input text
 					addresses = geocoder.getFromLocationName(locationName, 1);            		
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return addresses;
 		}
 
 		@Override
 		protected void onPostExecute(List<Address> addresses) {
 			if(addresses != null && addresses.size() > 0){
 				friendlyName = addresses.get(0).getSubLocality() + ", " + addresses.get(0).getSubLocality();
				currentPoint = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
 			} else {
 				if(location != null && !(location.latitude == 0 && location.longitude == 0)){
 					friendlyName = "Unknown";
 					currentPoint = new LatLng(location.latitude, location.longitude);
 				} else {
 					Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
 					return;
 				}
 			}
 			Intent addLocation = new Intent(getBaseContext(),
 					AddChaseLocation.class);
 			addLocation.putExtra("chaseId", chase.getObjectId());
 			addLocation.putExtra("latitude", currentPoint.latitude);
 			addLocation.putExtra("longitude", currentPoint.longitude);
 			addLocation.putExtra("friendlyName", friendlyName);
 			addLocation.putExtra("locationIndex", mapPoints.size());
 			startActivityForResult(addLocation,
 					AddChaseLocation.ADD_CHASE_LOCATION_ACTIVITY_CODE);
 		}
 	}
 
 }
