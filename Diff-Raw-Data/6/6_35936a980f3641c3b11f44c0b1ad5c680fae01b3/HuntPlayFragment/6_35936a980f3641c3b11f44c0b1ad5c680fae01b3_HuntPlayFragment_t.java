 package com.chaseit.fragments;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.chaseit.ParseHelper;
 import com.chaseit.R;
 import com.chaseit.activities.HuntShowImageActivity;
 import com.chaseit.models.Location;
 import com.chaseit.models.UserHunt;
 import com.chaseit.models.UserHunt.HuntStatus;
 import com.chaseit.models.wrappers.HuntWrapper;
 import com.chaseit.models.wrappers.LocationWrapper;
 import com.chaseit.models.wrappers.UserHuntWrapper;
 import com.chaseit.util.Constants;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.LatLngBounds.Builder;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.parse.FindCallback;
 import com.parse.GetDataCallback;
 import com.parse.ParseException;
 import com.parse.ParseImageView;
 import com.parse.SaveCallback;
 
 public class HuntPlayFragment extends Fragment implements LocationListener {
 	private static final String tag = "Debug - com.chaseit.fragments.HuntPlayFragment";
 
 	//display elements
 	private GoogleMap googleMap;
 	private Button focusOnMarkers;
 	private UserHunt userHunt;
 	private ParseImageView ivHuntImageClue;
 	private TextView tvHuntMessageClue;
 	private ProgressBar pbHuntProgress;
 	private Button btnHuntProgressCheck;
 	private Button btnDemoHuntProgressCheck;
 
 	//wrappers that get passed in from details/summary page
 	private UserHuntWrapper uHuntWrapper;
 	private HuntWrapper wHunt;
 	private ArrayList<LocationWrapper> wLocations;
 
 	private LocationWrapper wLastLocation;
 	private LocationWrapper wNextLocation;
 	private Location lastLocation;
 	private Location nextLocation;
 
 	private LocationManager mLocationManager;
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_hunt_play, container,
 				false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Log.d("DEBUG", "........ON ACTIVITY CREATED......");
 
 		//setup all static view data
 		//no drawing done here
 		loadViewElements(getView());
 
 		//get the user-hunt wrapper hunt wrapper and all the wrapped locations for this hunt
 		Bundle extras = getActivity().getIntent().getExtras();
 		uHuntWrapper = (UserHuntWrapper) extras.getSerializable(Constants.USER_HUNT_WRAPPER_DATA_NAME);
 		wHunt = (HuntWrapper) extras.getSerializable(Constants.HUNT_WRAPPER_DATA_NAME);
 		Bundle locationBundle = extras.getBundle(Constants.LOCATIONS_BUNDLE_DATA_NAME);
 		wLocations = locationBundle.getParcelableArrayList(Constants.LOCATIONS_WRAPPER_DATA_NAME);
 
 		//SXG get this in background
 		userHunt = ParseHelper.getHuntInProgressByIdBlocking(uHuntWrapper.getObjectId());
 
 		//setup the hunt
 		setupHuntState();
 
 		//go to current location
 		setupLocationInformation();
 
 		//make sure that this is NOT a new hunt before fetching the last location and drawing it
 		if(wLastLocation != null && wLastLocation.getIndexInHunt() != -1){
 			//fetch the last location from Parse and then draw
 			ParseHelper.getLocationByObjectId(wLastLocation.getObjectId(), new FindCallback<Location>() {
 				@Override
 				public void done(List<Location> objects, ParseException e) {
 					if(objects == null){
 						Log.d("DEBUG", "Found no locations for objectId " + wLastLocation.getObjectId());
 						return;
 					}
 
 					if(objects.size() > 1){
 						Log.d("DEBUG", "Found multiple locations for objectId " + wLastLocation.getObjectId());
 						return;
 					}
 					lastLocation = objects.get(0);
 					ArrayList<LocationWrapper> conquered = getConqueredLocationsInChase(lastLocation.getIndexInHunt());
 					drawMarkersAndPolygon(conquered, true);
 					fetchAndShowCluesForLocation(wNextLocation, true);
 				}
 
 			});			
 		} else {
 			//no need to draw anything. simply fetch and show the clues
 			lastLocation = null;
 			fetchAndShowCluesForLocation(wNextLocation, true);
 		}
 
 	}
 
 	//	@Override
 	public void onResume() {
 		Log.d("DEBUG", "........RESUMING PLAY......");
 		////		//make the "I am Here" button non clickable till we fetch the clues
 		////		btnHuntProgressCheck.setClickable(false);
 		////		setupHuntState();
 		////		ArrayList<LocationWrapper> conquered = getConqueredLocationsInChase(wLastLocation.getIndexInHunt());
 		////		drawMarkersAndPolygon(conquered, true);
 		////		fetchAndShowCluesForLocation(wNextLocation, true);
 		super.onResume();
 	}
 
 	/**
 	 * Uses the user-hunt wrapper and the list of all wrapped locations for this hunt
 	 * to determine the previous and next location
 	 * Toasts user based on state
 	 */
 	private void setupHuntState() {
 		//		Hunt hunt = ParseHelper.getHuntByObjectIdBlocking(uHuntWrapper.getHuntObjectId());
 		//		locationList = ParseHelper.getLocationsforHuntBlocking(hunt);
 		if (wLocations != null) {
 			//find out where we have been
 			wLastLocation = getWrappedLocationByIndex(uHuntWrapper.getLocationIndex());
 			wNextLocation = getWrappedLocationByIndex(uHuntWrapper.getLocationIndex()+1);
 
 			pbHuntProgress.setMax(wLocations.size());
 			pbHuntProgress.setProgress(wLastLocation != null ? wLastLocation.getIndexInHunt() + 1 : 0);
 
 			//find out if we are starting the hunt
 			if (uHuntWrapper.getLocationIndex() == -1) {
 				// just started
 				Toast.makeText(getActivity().getBaseContext(), "Hunt has started",Toast.LENGTH_SHORT).show();
 			} else if(wNextLocation == null || isHuntComplete(wLastLocation)){					
 				//we are done with the hunt
 				//SXG go to success page
 				Toast.makeText(getActivity().getBaseContext(), "Hunt is over",Toast.LENGTH_SHORT).show();
 				btnHuntProgressCheck.setClickable(false);
 				btnDemoHuntProgressCheck.setClickable(false);
 			} else {
 				Toast.makeText(getActivity().getBaseContext(), "Hunt is NOT over, next location loaded", Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			Log.d(tag, "no locations found for hunt");
 		}
 	}
 
 	private void loadViewElements(View view){
 		ivHuntImageClue = (ParseImageView) view.findViewById(R.id.ivHuntImageClue);
 		ivHuntImageClue.setOnClickListener(getHuntImageClueClickListener());
 		tvHuntMessageClue = (TextView) view.findViewById(R.id.tvHuntMessageClue);
 		btnHuntProgressCheck = (Button) view.findViewById(R.id.btnHuntProgressCheck);
 		focusOnMarkers = (Button) view.findViewById(R.id.btnFocusOnMarkers);
 		btnDemoHuntProgressCheck = (Button) view.findViewById(R.id.btnDemoHuntProgressCheck);
 		pbHuntProgress = (ProgressBar)view.findViewById(R.id.pbHuntProgress);
 		pbHuntProgress.setClickable(false);
 		initilizeMap();
 
 		//setup click listeners
 		btnHuntProgressCheck.setOnClickListener(getBtnHuntProgressCheckClickListener(true));
 		btnDemoHuntProgressCheck.setOnClickListener(getBtnHuntProgressCheckClickListener(false));
 
 		//make it non clickable till we fetch the clues
 		btnHuntProgressCheck.setClickable(false);
 		btnDemoHuntProgressCheck.setClickable(false);
 
 		focusOnMarkers.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				focusOnMarkers();
 			}
 		});
 
 	}
 
 	private void setupLocationInformation() {
 		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 
 		android.location.Location location = mLocationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
 			googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
 			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 		} else {
 			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		}
 	}
 
 
 	private void focusOnMarkers() {
		/*
		 * if this is a new hunt, nothing to draw
		 */
		if(wLastLocation == null)
			return;
					
 		ArrayList<LocationWrapper> conquered = getConqueredLocationsInChase(wLastLocation.getIndexInHunt());
 		drawMarkersAndPolygon(conquered, true);
 	}
 
 	private OnClickListener getBtnHuntProgressCheckClickListener(final boolean isLive) {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				LatLng target = wNextLocation.getLocation();
 				Float distanceFromTarget = getDistanceFromTarget(target);
 				//if with 200m of target, or in demo mode 
 				if (distanceFromTarget < 200 || !isLive) {
 					//they have successfully found the target location.
 					Toast.makeText(getActivity().getBaseContext(), "Perfect!. Congratulations!!", Toast.LENGTH_SHORT).show();
 
 					//update locations
 					wLastLocation = wNextLocation;
 					lastLocation = nextLocation;
 
 					//update the progress
 					pbHuntProgress.setMax(wLocations.size());
 					pbHuntProgress.setProgress(wLastLocation != null ? wLastLocation.getIndexInHunt() + 1 : 0);
 
 					//get next location
 					wNextLocation = getWrappedLocationByIndex(wLastLocation.getIndexInHunt() + 1);
 
 					//setup hunt progress status
 					HuntStatus huntStatus;
 					if(wNextLocation == null || isHuntComplete(wLastLocation)){
 						Toast.makeText(getActivity().getBaseContext(), "Hunt is complete !. Congratulations !!", Toast.LENGTH_SHORT).show();
 						huntStatus = HuntStatus.COMPLETED;
 						btnHuntProgressCheck.setClickable(false);
 						btnDemoHuntProgressCheck.setClickable(false);
 					} else {
 						huntStatus = HuntStatus.IN_PROGRESS;
 					}
 
 					//update user hunt
 					userHunt.setHuntStatus(huntStatus);
 					userHunt.setLastLocationLat(wLastLocation.getLocation().latitude);
 					userHunt.setLastLocationLong(wLastLocation.getLocation().longitude);
 					userHunt.setLastLocationIndex(wLastLocation.getIndexInHunt());					
 					userHunt.setLastLocationObjectId(wLastLocation.getObjectId());
 					saveUserHunt(userHunt);
 
 					//update the map markers and polygon
 					ArrayList<LocationWrapper> conquered = getConqueredLocationsInChase(wLastLocation.getIndexInHunt());
 					drawMarkersAndPolygon(conquered, false);
 
 					if(huntStatus == HuntStatus.IN_PROGRESS){
 						//fetch new clues
 						fetchAndShowCluesForLocation(wNextLocation, true);
 						Toast.makeText(getActivity().getBaseContext(), "Hunt is NOT over, loading next location", Toast.LENGTH_SHORT).show();													
 					}
 				} else {
 					Toast.makeText(getActivity().getBaseContext(), "Sorry, not quite there. Try again", Toast.LENGTH_SHORT).show();
 				}
 			}
 		};
 	}
 
 	private void saveUserHunt(final UserHunt userHunt) {
 		userHunt.saveInBackground(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				if (e == null){
 					uHuntWrapper = new UserHuntWrapper(userHunt);
 				} else {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private void fetchAndShowCluesForLocation(final LocationWrapper wLocation, final boolean storeAsNextLocation) {
 		ParseHelper.getLocationByObjectId(wLocation.getObjectId(), new FindCallback<Location>() {
 			@Override
 			public void done(List<Location> objects, ParseException e) {
 				if(objects == null){
 					Log.d("DEBUG", "Found no locations for objectId " + wLocation.getObjectId());
 					return;
 				}
 
 				if(objects.size() > 1){
 					Log.d("DEBUG", "Found multiple locations for objectId " + wLocation.getObjectId());
 					return;
 				}
 
 				if(storeAsNextLocation){
 					nextLocation = objects.get(0);
 				}
 				tvHuntMessageClue.setText(objects.get(0).getHint());
 				ivHuntImageClue.setParseFile(objects.get(0).getImage());
 				ivHuntImageClue.loadInBackground(new GetDataCallback() {
 					@Override
 					public void done(byte[] data, ParseException e) {
 						if(e == null){
 							btnHuntProgressCheck.setClickable(true);
 							btnDemoHuntProgressCheck.setClickable(true);
 						} else {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		});
 	}
 
 	private OnClickListener getHuntImageClueClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Bitmap bitmap = ((BitmapDrawable) ivHuntImageClue.getDrawable()).getBitmap();
 				Intent in = new Intent(HuntPlayFragment.this.getActivity().getBaseContext(), HuntShowImageActivity.class);
 				in.putExtra(Constants.IMAGE_EXTRA, bitmap);
 				startActivity(in);
 			}
 		};
 	}
 
 	public Float getDistanceFromTarget(LatLng target) {
 		android.location.Location currentLocation = googleMap.getMyLocation();
 		Float result = null;
 		float[] results = new float[10];
 		android.location.Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), 
 				target.latitude, target.longitude, 
 				results);
 		result = Float.valueOf(results[0]);
 		Toast.makeText(getActivity().getBaseContext(), "Distance: " + result + " meters", Toast.LENGTH_SHORT).show();
 		return result;
 	}
 
 	/**
 	 * function to load map. If map is not created it will create it for you
 	 * */
 	private void initilizeMap() {
 		if (googleMap == null) {
 			googleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.huntMap)).getMap();
 
 			// check if map is created successfully or not
 			if (googleMap == null) {
 				Log.d(tag, "unable to get a handle on google map");
 			} else {
 				googleMap.setMyLocationEnabled(true);				
 			}
 		}
 	}
 
 	public void drawMarkersAndPolygon(List<LocationWrapper> mapPoints, boolean animate) {
 		if(mapPoints == null || mapPoints.size() == 0){
 			Log.d("DEBUG", "Nothing to draw");
 			return;
 		}
 
 		PolylineOptions rectOptions = new PolylineOptions();
 		// Collections.sort(mapPoints);
 		Builder builder = new LatLngBounds.Builder();
 		for (LocationWrapper point : mapPoints) {
 			LatLng ll = point.getLocation();
 
 			// drop marker first
 			googleMap.addMarker(new MarkerOptions().position(ll)
 					.title((point.getIndexInHunt() + 1) + ". "+ point.getLocationName())
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 
 			builder.include(ll);
 			rectOptions.add(ll).width(5).color(Color.BLUE).geodesic(true);
 			googleMap.addPolyline(rectOptions);
 		}
 
 		if(animate){
 			googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
 		} else {
 			googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));			
 		}
 	}
 
 
 	private ArrayList<LocationWrapper> getConqueredLocationsInChase(int lastConqueredLocationIndex){
 		//construct list that needs to be drawn
 		//not sure if we can depend on the list being in order of location index in hunt
 		ArrayList<LocationWrapper> conquered = new ArrayList<LocationWrapper>();
 		for(int i = 0; i <= lastConqueredLocationIndex; i++){
 			conquered.add(getWrappedLocationByIndex(i));
 		}
 
 		return conquered;
 	}
 
 	/**
 	 * Returns null for a newly started hunt since locationIndex for it would be -1
 	 * @param index
 	 * @return
 	 */
 
 	private LocationWrapper getWrappedLocationByIndex(int index){
 		for(LocationWrapper lW : wLocations){
 			if(lW.getIndexInHunt() == index){
 				return lW;
 			}
 		}
 		Log.d("DEBUG", "Location not found by index " + index);
 		return null;
 	}
 
 	private boolean isHuntComplete(LocationWrapper location){
 		if(location.getIndexInHunt() == (wLocations.size() - 1)){
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onLocationChanged(android.location.Location location) {
 		if (location != null) {
 			googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
 			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 			mLocationManager.removeUpdates(this);
 		}		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) { }
 	@Override
 	public void onProviderEnabled(String provider) { }
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) { }
 
 }
