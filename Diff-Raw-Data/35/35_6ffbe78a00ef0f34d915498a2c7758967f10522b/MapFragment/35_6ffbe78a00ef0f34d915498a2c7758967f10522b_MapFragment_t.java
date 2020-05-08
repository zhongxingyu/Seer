 package com.tldr;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.auth.AccountHelper;
 import com.datastore.BaseDatastore;
 import com.datastore.DatastoreResultHandler;
 import com.datastore.TaskDatastore;
 import com.datastore.UserInfoDatastore;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPositionCreator;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.tldr.com.tldr.userinfoendpoint.model.UserInfo;
 import com.tldr.exlap.TriggerRegister.TriggerDomains;
 import com.tldr.gamelogic.Factions;
 import com.tldr.taskendpoint.model.Task;
 import com.tldr.tools.FakeLocationProvider;
 import com.tldr.tools.ToolBox;
 
 public class MapFragment extends Fragment implements LocationListener,
 		FragmentCommunicator, DatastoreResultHandler, OnMarkerClickListener {
 
 	private MapView mMapView;
 	private View mWindow;
 	private GoogleMap mMap;
 	private Bundle mBundle;
 	private RelativeLayout buttonOverlay;
 	private ImageView playerButton;
 	private ImageView fractionbuton;
 	private OnLocationChangedListener mListener;
 	private LocationManager locationManager;
 
 	private TaskDatastore taskDatastore;
 	private UserInfoDatastore userDatastore;
 	private AccountHelper auth;
 
 	private List<Marker> taskMarkers;
 	private List<Marker> userMarkers;
 	private Marker selfMarker;
 	private boolean menueActive = false;
 	private int standardEMSForSearchField = 33;
 
 	private MyOnInfoWindowClickListener myOnInfoWindowClickListerer;
 	HashMap<Marker, Task> tasksHashMap;
 	List<HashMap<String, String>> tasksList;
 	HashMap<Marker, UserInfo> usersHashMap;
 	List<HashMap<String, String>> usersList;
 	private float zoomLevel = 15.0f;
 
 	// UI Stuff
 	private AutoCompleteTextView searchField;
 
 	public final static int ORIGINAL_SCREEN_WIDTH = 1200;
 
 	public void initialize() {
 		Bundle args = getArguments();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		auth = new AccountHelper(getActivity());
 		taskDatastore = new TaskDatastore(this, auth.getCredential());
 		userDatastore = new UserInfoDatastore(this, auth.getCredential());
 		View v = new View(getActivity());
 		v = inflater.inflate(R.layout.map_layout, container, false);
 
 		try {
 			MapsInitializer.initialize(getActivity());
 		} catch (GooglePlayServicesNotAvailableException e) {
 			Log.w("TLDR", e);
 		}
 
 		locationManager = (LocationManager) getActivity().getSystemService(
 				Context.LOCATION_SERVICE);
 
 		GlobalData.setLocationManager(locationManager);
 		GlobalData.setFakeLocationProvider(FakeLocationProvider
 				.getFakeLocationProvider(5000, locationManager, this,
 						getActivity()));
 		if (locationManager != null) {
 			boolean gpsIsEnabled = locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			boolean networkIsEnabled = locationManager
 					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 			boolean fakeLocProvEnabled = locationManager
 					.isProviderEnabled(FakeLocationProvider.PROVIDER_NAME);
 			if (fakeLocProvEnabled) {
 				locationManager.requestLocationUpdates(
 						FakeLocationProvider.PROVIDER_NAME, 1L, 1F, this);
 			}
 			if (!GlobalData.fake_location_data_enabled) {
 				if (gpsIsEnabled) {
 					locationManager.requestLocationUpdates(
 							LocationManager.GPS_PROVIDER, 5000L, 10F, this);
 					GlobalData
 							.setLastknownPosition(locationManager
 									.getLastKnownLocation(LocationManager.GPS_PROVIDER));
 				} else if (networkIsEnabled) {
 					locationManager.requestLocationUpdates(
 							LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
 					GlobalData
 							.setLastknownPosition(locationManager
 									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
 				} else {
 					// Show an error dialog that GPS is disabled...
 				}
 			}
 		} else {
 			// Show some generic error dialog because something must have gone
 			// wrong with location manager.
 		}
 
 		mMapView = (MapView) v.findViewById(R.id.map);
 		mMapView.onCreate(mBundle);
 		setUpMapIfNeeded(v);
 		Location lastKnown = GlobalData.getLastknownPosition();
 		if (lastKnown != null) {
 			UserInfo current = GlobalData.getCurrentUser();
 			if (current != null) {
 				userDatastore.updateUser(current.setGeoLat(
 						lastKnown.getLatitude()).setGeoLon(
 						lastKnown.getLongitude()));
 			}
 		}
 
 		// //Layout Searchfield by Dimensions
 		// Display d = getActivity().getWindowManager().getDefaultDisplay();
 		// Point p = new Point();
 		// d.getSize(p);
 		// double factor = (((double)p.x/(double)ORIGINAL_SCREEN_WIDTH));
 		// int ems = (int)(((double)standardEMSForSearchField)*factor);
 		searchField = (AutoCompleteTextView) v
 				.findViewById(R.id.mapSearchEditText);
 		// searchField.setEms(ems);
 		// Log.d("TLDR", p.x+"x"+p.y);
 		return v;
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onViewCreated(view, savedInstanceState);
 		mWindow = view;
 		searchField.clearFocus();
 		searchField.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 					// Clear focus here from edittext
 					InputMethodManager imm = (InputMethodManager) getActivity()
 							.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(mWindow.getWindowToken(), 0);
 					searchField.clearFocus();
 					mMapView.requestFocus();
 				}
 				return false;
 			}
 		});
 		searchField.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				InputMethodManager imm = (InputMethodManager) getActivity()
 						.getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mWindow.getWindowToken(), 0);
 				searchField.clearFocus();
 				mMapView.requestFocus();
 				Marker m = ((AutoCompletionMarker) arg0.getItemAtPosition(arg2))
 						.getMarker();
 				flyTo(m.getPosition());
 				m.showInfoWindow();
 				searchField.setText("");
 
 			}
 		});
 		mMap.setOnMapClickListener(new OnMapClickListener() {
 
 			@Override
 			public void onMapClick(LatLng arg0) {
 				// TODO Auto-generated method stub
 				InputMethodManager imm = (InputMethodManager) getActivity()
 						.getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mWindow.getWindowToken(), 0);
 				searchField.clearFocus();
 				mMapView.requestFocus();
 				if (menueActive) {
 					switchMenue();
 				}
 			}
 		});
 
 		tasksHashMap = new HashMap<Marker, Task>();
 		tasksList = new ArrayList<HashMap<String, String>>();
 		usersHashMap = new HashMap<Marker, UserInfo>();
 		usersList = new ArrayList<HashMap<String, String>>();
 		myOnInfoWindowClickListerer = new MyOnInfoWindowClickListener(this,
 				tasksHashMap, usersHashMap, tasksList, usersList);
 		mMap.setOnInfoWindowClickListener(myOnInfoWindowClickListerer);
 
 		InputMethodManager imm = (InputMethodManager) view.getContext()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
 		mMapView.requestFocus();
 		userDatastore.getNearbyUsers();
 		if (GlobalData.getLastknownPosition() != null) {
 			// generateMarkers(lastknown);
 			flyTo(GlobalData.getLastknownPosition());
 			this.selfMarker = mMap.addMarker(new MarkerOptions().position(
 					new LatLng(GlobalData.getLastknownPosition().getLatitude(),
 							GlobalData.getLastknownPosition().getLongitude()))
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.tldr_button_car)));
 		}
 		handleRequestResult(BaseDatastore.REQUEST_TASK_FETCHNEARBY,
 				GlobalData.getAllTasks());
 		resetMarkers();
 	}
 
 	private void acceptAllNearbyTasks() {
 		for (Marker m : taskMarkers) {
 			m.setIcon(BitmapDescriptorFactory
 					.fromResource(R.drawable.tldr_target_sm));
 		}
 	}
 
 	private void rejectAllNearbyTasks() {
 		for (Marker m : taskMarkers) {
 			m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.target));
 		}
 	}
 
 	private void deleteAllTasks() {
 		for (Marker m : taskMarkers) {
 			m.remove();
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mBundle = savedInstanceState;
 
 	}
 
 	private void setUpMapIfNeeded(View inflatedView) {
 		if (mMap == null) {
 			mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
 			if (mMap != null) {
 				setUpMap();
 			}
 		}
 	}
 
 	private void setUpMap() {
 		mMap.setMyLocationEnabled(true);
 		mMap.setOnMarkerClickListener(this);
 		taskMarkers = new ArrayList<Marker>();
 		userMarkers = new ArrayList<Marker>();
 		// taskDatastore.createFakeTasks();// AUSKOMMENTIEREN
 		// taskDatastore.getNearbyTasks(); // AUSKOMMENTIEREN
 
 	}
 
 	private void fillMapWithOverlays() {
 		mMap.clear();
 		mMap.setMyLocationEnabled(true);
 		mMap.setOnMarkerClickListener(this);
 		userDatastore.getNearbyUsers();
 		handleRequestResult(BaseDatastore.REQUEST_TASK_FETCHNEARBY,
 				GlobalData.getAllTasks());
 		resetMarkers();
 
 		selfMarker = mMap.addMarker(new MarkerOptions().position(
 				new LatLng(GlobalData.getLastknownPosition().getLatitude(),
 						GlobalData.getLastknownPosition().getLongitude()))
 				.icon(BitmapDescriptorFactory
 						.fromResource(R.drawable.tldr_button_car)));
 		flyTo(GlobalData.getLastknownPosition());
 	}
 
 	private void flyTo(Location location) {
 		if (location != null) {
 			flyTo(new LatLng(location.getLatitude(), location.getLongitude()));
 		}
 	}
 
 	private void flyTo(LatLng location) {
 		if (location != null) {
 			// Log.d("TLDR", "flying to new Location !");
//			if(GlobalData.getCameraPosition()!=null){
//				this.zoomLevel=GlobalData.getCameraPosition().zoom;
//			}else{
//				this.zoomLevel=17f;
//			}
//			mMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
//			if (GlobalData.isFirstStart()) {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
				Log.d("TLDR", "camera animated!");
//				GlobalData.setCameraPosition(mMap.getCameraPosition());
//				GlobalData.setFirstStart(false);
//				Log.d("TLDR", "setFirstStart");
//			} else {
//				mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
//				mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

//			}
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mMapView.onResume();
 		((HomeActivity) getActivity()).animateMenuIcons(0);
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mMapView.onPause();
 		GlobalData.setCameraPosition(mMap.getCameraPosition());
 	}
 
 	@Override
 	public void onDestroy() {
 		mMapView.onDestroy();
 		super.onDestroy();
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// flyTo(location);
 		Log.d("TLDR", "Location Changed to " + location);
 		if (!GlobalData.fake_location_data_enabled
 				|| location.getProvider().equals(
 						FakeLocationProvider.PROVIDER_NAME)) {
 			GlobalData.setLastknownPosition(location);
 			if (GlobalData.getCurrentUser() != null) {
 				LatLng latLon = null;
 				// try {
 				// latLon = (new LatLng(mMap.getMyLocation().getLatitude(), mMap
 				// .getMyLocation().getLongitude()));
 				// } catch (Exception e) {
 				latLon = (new LatLng(location.getLatitude(),
 						location.getLongitude()));
 				// }
 				if (latLon != null) {
 					if (selfMarker == null)
 						fillMapWithOverlays();
 					selfMarker.setPosition(latLon);
 					if (GlobalData.getTriggerRegister() != null) {
 						GlobalData.getTriggerRegister().onNewData(
 								TriggerDomains.GPS, latLon);
 					}
 				}
 				GlobalData.getCurrentUser().setGeoLon(latLon.longitude)
 						.setGeoLat(latLon.latitude);
 				// ist genuaer so!
 				fillMapWithOverlays();
 			}
 		}
 	}
 
 	private void resetMarkers() {
 		List<Task> acceptedTasks;
 		acceptedTasks = GlobalData.getAcceptedTasks();
 		for (Task t : acceptedTasks) {
 			List<PolylineOptions> poL = ToolBox
 					.generatePolylineFromIDs(t.getGoals(), GlobalData
 							.getCurrentUser().getFinishedGoals());
 			for (PolylineOptions po : poL) {
 
 				mMap.addPolyline(po);
 			}
 		}
 		List<AutoCompletionMarker> autoCompletionObjects = new ArrayList<MapFragment.AutoCompletionMarker>();
 		int i = 0;
 		Location current = GlobalData.getLastknownPosition();
 		List<Task> tasks = GlobalData.getAllTasks();
 		if (tasks != null) {
 
 			for (Task t : tasks) {
 				float[] distance = new float[] { 0.0f };
 				if (current != null) {
 					Location.distanceBetween(current.getLatitude(),
 							current.getLongitude(), t.getGeoLat(),
 							t.getGeoLon(), distance);
 				}
 				Marker newMarker;
 				if (GlobalData.getAcceptedTasks().contains(t))
 					newMarker = mMap.addMarker(new MarkerOptions()
 							.position(new LatLng(t.getGeoLat(), t.getGeoLon()))
 							.title(t.getTitle())
 							.snippet(
 									(t.getDescription().length() < 30 ? t
 											.getDescription() : t
 											.getDescription().substring(0, 29)
 											+ ".."))
 							.icon(BitmapDescriptorFactory
 									.fromResource(R.drawable.tldr_target_sm)));
 				else
 					newMarker = mMap.addMarker(new MarkerOptions()
 							.position(new LatLng(t.getGeoLat(), t.getGeoLon()))
 							.title(t.getTitle())
 							.snippet(
 									(t.getDescription().length() < 30 ? t
 											.getDescription() : t
 											.getDescription().substring(0, 29)
 											+ ".."))
 							.icon(BitmapDescriptorFactory
 									.fromResource(R.drawable.target)));
 				taskMarkers.add(newMarker);
 				Log.d("TLDR", newMarker.toString());
 				ToolBox.addInRealDistanceOrder(autoCompletionObjects,
 						new AutoCompletionMarker(newMarker, distance[0]));
 
 				tasksHashMap.put(newMarker, t);
 				HashMap<String, String> newMap = new HashMap<String, String>();
 				newMap.put("title", t.getTitle());
 				newMap.put("desc", t.getDescription());
 				int dist = Math.round(distance[0]);
 				DecimalFormat df = new DecimalFormat("#.#");
 				newMap.put(
 						"distance",
 						(dist < 1000 ? dist + "m" : "~"
 								+ df.format(((dist) / 1000)) + "km"));
 				tasksList.add(newMap);
 			}
 		}
 		myOnInfoWindowClickListerer.setTasks(tasksHashMap, tasksList);
 		if (autoCompletionObjects.size() > 0) {
 			searchField.setAdapter(new ArrayAdapter<AutoCompletionMarker>(this
 					.getActivity(),
 					android.R.layout.simple_dropdown_item_1line,
 					autoCompletionObjects));
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void receiveMessage(int messageID, Object data) {
 		// TODO Auto-generated method stub
 		if (messageID == SPEECH_REQUEST_MESSAGE) {
 			String message = (String) data;
 			Log.d("TLDR", "Speech Input: " + message);
 			message = message.toLowerCase();
 			if (message.contains("all")
 					&& (message.contains("tasks") || message.contains("task")
 							|| message.contains("alle") || message
 								.contains("missions"))) {
 				if ((message.contains("accept")
 						|| message.contains("akzeptieren") || message
 							.contains("except"))) {
 					acceptAllNearbyTasks();
 				} else if (message.contains("reject")
 						|| message.contains("decline")) {
 					rejectAllNearbyTasks();
 				} else if (message.contains("delete")) {
 					deleteAllTasks();
 				}
 
 			} else
 				ToolBox.showAlert(this.getActivity(), "Speech Result", message,
 						"Dismiss", null);
 
 		}
 	}
 
 	@Override
 	public void handleRequestResult(int requestId, Object result) {
 		// TODO Auto-generated method stub
 		if (requestId == BaseDatastore.REQUEST_USERINFO_UPDATEUSER) {
 			UserInfo userResult = (UserInfo) result;
 			if (userResult != null) {
 				GlobalData.setCurrentUser(userResult);
 			}
 		}
 		if (requestId == BaseDatastore.REQUEST_USERINFO_NEARBYUSERS) {
 			List<UserInfo> users = (List<UserInfo>) result;
 			List<UserInfo> usersDef = new ArrayList<UserInfo>();
 			List<UserInfo> usersMof = new ArrayList<UserInfo>();
 			Location current = GlobalData.getLastknownPosition();
 			GlobalData.setAllUsers(users);
 			if (users != null) {
 				for (UserInfo u : users) {
 					if (!u.getId().equals(GlobalData.getCurrentUser().getId())
 							&& u.getGeoLat() != 0.0 && u.getGeoLon() != 0.0) {
 						Marker newMarker = mMap
 								.addMarker(new MarkerOptions()
 										.position(
 												new LatLng(u.getGeoLat(), u
 														.getGeoLon()))
 										.title(u.getUsername())
 										.icon(BitmapDescriptorFactory
 												.fromResource(R.drawable.agent)));
 
 						userMarkers.add(newMarker);
 
 						usersHashMap.put(newMarker, u);
 						HashMap<String, String> newMap = new HashMap<String, String>();
 						newMap.put("name", u.getUsername());
 						float[] distance = new float[] { 0.0f };
 						if (current != null) {
 							Location.distanceBetween(current.getLatitude(),
 									current.getLongitude(), u.getGeoLat(),
 									u.getGeoLon(), distance);
 						}
 						int dist = Math.round(distance[0]);
 						DecimalFormat df = new DecimalFormat("#.#");
 						newMap.put("distance", (dist < 1000 ? dist + "m" : "~"
 								+ df.format(((dist) / 1000)) + "km"));
 						usersList.add(newMap);
 					}
 
 					switch (u.getFaction()) {
 					case (1):
 						usersDef.add(u);
 						break;
 					case (2):
 						usersMof.add(u);
 						break;
 					}
 				}
 				myOnInfoWindowClickListerer.setUsers(usersHashMap, usersList);
 
 				GlobalData.setFractionUsers(1, usersDef);
 				GlobalData.setFractionUsers(2, usersMof);
 			}
 		}
 	}
 
 	public class AutoCompletionMarker {
 		private Marker marker;
 		private String title;
 		private float distance;
 
 		private AutoCompletionMarker(Marker m, float distance) {
 			marker = m;
 			title = m.getTitle();
 			this.distance = distance;
 		}
 
 		@Override
 		public String toString() {
 			// TODO Auto-generated method stub
 			DecimalFormat df = new DecimalFormat("#.#");
 			return title
 					+ " ("
 					+ (distance < 1000 ? (int) distance + "m" : "~"
 							+ df.format((distance / 1000)) + "km") + ")";
 		}
 
 		private Marker getMarker() {
 			return marker;
 		}
 
 		public float getDistance() {
 			return distance;
 		}
 
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 		if (marker.equals(selfMarker)) {
 			switchMenue();
 		}
 		return false;
 	}
 
 	private void switchMenue() {
 		if (buttonOverlay == null) {
 			buttonOverlay = (RelativeLayout) getActivity().findViewById(
 					R.id.overlaytest);
 			fractionbuton = (ImageView) getActivity().findViewById(
 					R.id.overlayFractionButton);
 			Factions.setMenueButton(fractionbuton);
 			fractionbuton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					onFactionButtonClick();
 				}
 			});
 			playerButton = (ImageView) getActivity().findViewById(
 					R.id.overlayPlayerButton);
 			playerButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					onPlayerButtonClick();
 				}
 			});
 		}
 		// boolean state = buttonOverlay.getVisibility() == View.VISIBLE;
 		selfMarker.setVisible(menueActive);
 		buttonOverlay.setVisibility(menueActive ? View.GONE : View.VISIBLE);
 		mMap.getUiSettings().setAllGesturesEnabled(menueActive);
 		menueActive = !menueActive;
 
 	}
 
 	private void onPlayerButtonClick() {
 		Fragment f1 = new ProfileDetailsFragment();
 		Bundle bundle = new Bundle();
 
 		Location current = GlobalData.getLastknownPosition();
 
 		HashMap<String, String> myUser = new HashMap<String, String>();
 		myUser.put("distance", "-");
 		myUser.put("name", GlobalData.getCurrentUser().getUsername());
 
 		bundle.putSerializable("HashMap", myUser);
 		bundle.putDouble("geo_lat", current.getLatitude());
 		bundle.putDouble("geo_lon", current.getLongitude());
 
 		f1.setArguments(bundle);
 		FragmentTransaction ft = this.getFragmentManager().beginTransaction();
 		ft.replace(this.getId(), f1);
 		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 		ft.addToBackStack(null);
 		ft.commit();
 	}
 
 	private void onFactionButtonClick() {
 		Toast.makeText(getActivity(), "Fraction", Toast.LENGTH_SHORT).show();
 		Bundle bundle = new Bundle();
 		Fragment fractionDetails = new FactionDetailsFragment();
 		fractionDetails.setArguments(bundle);
 		FragmentTransaction ft = this.getFragmentManager().beginTransaction();
 		ft.replace(R.id.homeActivity, fractionDetails);
 		ft.addToBackStack(null);
 		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 		ft.commit();
 	}
 
 }
