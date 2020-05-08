 package edu.westmont.course;
 
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import android.location.Location;
 import android.os.Bundle;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DrawMap extends FragmentActivity implements 
 GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
 	private static final int GPS_ERRORDIALOG_REQUEST = 0;
 	GoogleMap myMap;
 	protected LocationClient myLocationClient;
 	protected int defaultZoom = 15;
 	protected boolean useDefaultZoom = true;
 	protected LocationChanger lc = new LocationChanger(40.715842,-74.006237);
 	protected DistanceFinder ranger = new DistanceFinder();
 	protected String userDefinedName = "";
 	protected LinkedList<Location> listLocation = new LinkedList<Location>();
 	protected LinkedList<Marker> listMarker = new LinkedList<Marker>();
 	protected LinkedList<Polyline> listLine = new LinkedList<Polyline>();
 	protected LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
 	protected boolean showCurrentLocation = false;
 	protected boolean moveCamera = true;
 	protected boolean runAgain = true;
 	protected PositionsDataSource datasource;
 
 	/**
 	 * Initiates an instance of the class and if the mapping service is available
 	 * it changes the view to the map view.  Otherwise it displays the main activity view.
 	 * Attribution for this code belongs to Lynda.com, "Building Android Apps with Google Maps API v2" 
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (servicesOk()) {
 			setContentView(R.layout.activity_map);
 			if (initMap()){
 				myLocationClient = new LocationClient(this, this, this);
 				myLocationClient.connect();
 			}
 			else Toast.makeText(this, "The map in not available right now.", Toast.LENGTH_SHORT).show();
 		}
 		else setContentView(R.layout.activity_main);
 
 		Intent intent = getIntent();
 		String runName = intent.getStringExtra(MainActivity.RUN_NAME);
 		Toast.makeText(this, runName, Toast.LENGTH_SHORT).show();
 
 		userDefinedName = runName;
 		datasource = new PositionsDataSource(this);
 		datasource.open();
 		datasource.setRunName(runName);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.map_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Credit for this method belongs to lynda.com, "Building Android Apps with Google Maps API v2"
 	 */
 	@Override
 	public boolean onOptionsItemSelected(android.view.MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.stopButton:
 			runAgain = !runAgain;
 			if (runAgain) item.setTitle(R.string.stop);
 			else item.setTitle(R.string.resume);
 			break;
 		case R.id.resetButton:
 			resetMap(true,true,true);
 			break;
 		case R.id.updateMapCamera:
 			moveCamera = !moveCamera;
 			if (moveCamera) item.setTitle(R.string.stay_put);
 			else {
 				item.setTitle(R.string.fly_to);
 				gotoCurrentLocation(false);
 			}
 			break;
 		case R.id.showCurrentLocation:
 			showCurrentLocation = !showCurrentLocation;
 			if (showCurrentLocation) {
 				useDefaultZoom = true;
 				item.setTitle(R.string.show_all);
 			}
 			else item.setTitle(R.string.show_current);
 			gotoCurrentLocation(false);
 			break;
 		case R.id.mapTypeNormal:
 			changeMapType(GoogleMap.MAP_TYPE_NORMAL);
 			break;
 		case R.id.mapTypeSatellite:
 			changeMapType(GoogleMap.MAP_TYPE_SATELLITE);
 			break;
 		case R.id.mapTypeHybrid:
 			changeMapType(GoogleMap.MAP_TYPE_HYBRID);
 			break;
 		case R.id.mapTypeTerrain:
 			changeMapType(GoogleMap.MAP_TYPE_TERRAIN);
 			break;
 		default:
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	};
 
 	private void changeMapType(int mapType){
 		useDefaultZoom = true;
 		myMap.setMapType(mapType);
 	}
 
 	/**
 	 * Determines if the Google Play Services are available in the current operating environment and returns
 	 * a boolean.
 	 * Attribution for this method belongs to Lynda.com, "Building Android Apps with the Google Maps API v2"
 	 * @return Returns True if the Google Play Services are available in the current environment.  Otherwise returns false.
 	 * 
 	 */
 	public boolean servicesOk(){
 		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		if (isAvailable == ConnectionResult.SUCCESS)
 			return true;
 		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
 			Dialog d = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
 			d.show();
 		}
 		else Toast.makeText(this, R.string.google_play_error_message, Toast.LENGTH_SHORT).show();
 		return false;
 	}
 	/**
 	 * Initiates the map with the purpose of getting a reference to it.
 	 * @return a boolean indicating if the map has been initiated.
 	 */
 	public boolean initMap(){
 		if (myMap == null){
 			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 			myMap = mapFrag.getMap();
 		}
 		if (myMap != null){
 			myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
 
 				@Override
 				public View getInfoWindow(Marker arg0) {
 					return null;
 				}
 
 				@Override
 				public View getInfoContents(Marker marker) {
 					View v = getLayoutInflater().inflate(R.layout.info_window, null);
 					TextView tvtitle = (TextView) v.findViewById(R.id.tv_title);
 					TextView tv1 = (TextView) v.findViewById(R.id.tv_text1);
 					TextView tv2 = (TextView) v.findViewById(R.id.tv_text2);
 					TextView tv3 = (TextView) v.findViewById(R.id.tv_text3);
 					TextView tv4 = (TextView) v.findViewById(R.id.tv_text4);
 					TextView tv5 = (TextView) v.findViewById(R.id.tv_text5);
 					String[] markerText = ranger.getLastString();
 					tvtitle.setText(marker.getTitle());
 					tv1.setText(markerText[0]);
 					tv2.setText(markerText[1]);
 					tv3.setText(markerText[2]);
 					tv4.setText(markerText[3]);
 					tv5.setText(markerText[4]);
 					return v;
 				}
 			});
 		}
 		return (myMap != null);
 	}
 
 
 	protected void gotoCurrentLocation(boolean add){
 		Location location = myLocationClient.getLastLocation();
 		if (location == null) Toast.makeText(this, "Sorry, your current location is not available",Toast.LENGTH_LONG).show();
 		else gotoLocation(location,add,add,add);
 	}
 
 	protected void gotoLocation(Location loc, boolean includeDatabase, boolean addMarker, boolean addLine){
 		LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
 		if (loc.getAccuracy() < 100) {
 			if (includeDatabase || addMarker || addLine) {
 				boundsBuilder.include(ll);
 				listLocation.add(loc);
 				ranger.addDistanceToLocation(loc);
 			}
 			if (includeDatabase) datasource.createPosition(loc);
 			if (addMarker) addLatLngToMap(ll);
 			if (addLine && listLocation.size() > 1) drawLine(listLocation.get(listLocation.size()-2), listLocation.getLast());
 		}
 		if (moveCamera){
 			CameraUpdate update;
 			if (showCurrentLocation && useDefaultZoom) {
 				update = CameraUpdateFactory.newLatLngZoom(ll, defaultZoom);
 				useDefaultZoom = false;
 			}
 			else if (showCurrentLocation) update = CameraUpdateFactory.newLatLng(ll);
 			else {
 				LatLngBounds bounds = boundsBuilder.build();
 				update = CameraUpdateFactory.newLatLngBounds(bounds, 70);
 			}
 			myMap.animateCamera(update);
 		}
 	}
 
 	protected void addLatLngToMap(LatLng ll){
 		MarkerOptions options = new MarkerOptions()
 		.title(userDefinedName)
 		.position(ll);
 		listMarker.add(myMap.addMarker(options));
 	}
 
 	/*
 	 * Draw a line on the map between the last two objects on the listLatLng list.
 	 */
 	private void drawLine(Location a, Location b){
 		PolylineOptions plo = new PolylineOptions()
 		.add(new LatLng(a.getLatitude(), a.getLongitude()))
 		.add(new LatLng(b.getLatitude(), b.getLongitude()))
 		.color(Color.BLUE)
 		.width(5);
 		listLine.add(myMap.addPolyline(plo));
 	}
 
 	public void resetMap(boolean resetLocations, boolean resetMarkers, boolean resetLines){
 		if (resetMarkers){
 			Iterator<Marker> markerI = listMarker.iterator();
 			while (markerI.hasNext()){
 				markerI.next().remove();
 			}
 			listMarker = new LinkedList<Marker>();
 		}
 		if (resetLines){
 			Iterator<Polyline> lineI = listLine.iterator();
 			while (lineI.hasNext()){
 				lineI.next().remove();
 			}
 			listLine = new LinkedList<Polyline>();
 		}
 		if (resetLocations){
 			listLocation = new LinkedList<Location>();
 			ranger = new DistanceFinder();
 			boundsBuilder = LatLngBounds.builder();
 		}
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 		// todo: implement this code by checking if it can be resolved.
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		//gotoCurrentLocation();
 		LocationRequest request = LocationRequest.create();
 		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 		request.setInterval(10000);
 		request.setFastestInterval(5000);
 		myLocationClient.requestLocationUpdates(request, this);
 	}
 
 	@Override
 	public void onDisconnected() {
 		Toast.makeText(this, "Error connecting to GPS.", Toast.LENGTH_LONG);
 	}
 
 	@Override
 	public void onLocationChanged(Location loc) {
 		if (runAgain){
			//gotoLocation(loc,true,true,true);
			gotoLocation(lc.next(),false,true,true);
			//addBatch(lc.getBatch(100),false,true,true);
 		}
 	}
 
 	public void addBatch(java.util.Collection<Location> list, boolean addDatabase, boolean addMarker, boolean addLine){
 		Iterator<Location> iterator = list.iterator();
 		moveCamera = false; 
 		while (iterator.hasNext()){
			gotoLocation(iterator.next(),addDatabase,addMarker,addLine);
 		}
 		moveCamera = true;
 		gotoLocation(listLocation.getLast(),false,false,false);
 	}
 }
