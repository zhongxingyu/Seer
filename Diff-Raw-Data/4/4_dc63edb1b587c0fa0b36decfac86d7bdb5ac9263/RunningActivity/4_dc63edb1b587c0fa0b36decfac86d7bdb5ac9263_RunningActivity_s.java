 package com.rt.ui;
 
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import com.rt.core.Leg;
 import com.rt.core.MapDataManager;
 import com.rt.core.Waypoint;
 import com.rt.runtime.Event;
 import com.rt.runtime.LocationMonitor;
 
 import android.os.Bundle;
 import com.rt.R;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import android.location.Location;
 import android.graphics.Color;
 
 
 //TODO:	Should see if there is an onPause method or something like that
 //		So that we can pause the LM.
 //		Should add distanceLeft, distanceRan, timeRan UI elements.
 //		All of that data should be easily accessed from the LM
 //		(Public access, don't worry about concurency)
 public class RunningActivity extends AbstractRuntimeActivity implements
 		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
 
 	private GoogleMap map;
 	private LocationMonitor lm;
 	private Location currentLoc;
 	private LocationClient lc;
 	private Semaphore lock;
 	private Leg selectedLeg;
 	private Leg lastLeg;
 	private ArrayList<Polyline> lines;
 	private boolean needRefresh;
 	
 	private static final LocationRequest REQUEST = LocationRequest.create()
 			.setInterval(5000) // 5 seconds
 			.setFastestInterval(16) // 16ms = 60fps
 			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if(mdm == null){
 			mdm = new MapDataManager();
 		}
 		lock = new Semaphore(1);
 		lm = new LocationMonitor(this, mdm);
 		Thread t = new Thread(lm);
 		t.start();
 		needRefresh = false;
 		
 		lines = new ArrayList<Polyline>();
 		setContentView(R.layout.activity_running);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setUpMapIfNeeded();
 		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		setUpLocationClientIfNeeded();
 		lc.connect();
 	}
 
 	void setUpMapIfNeeded() {
 		if (map == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			map = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 			// Check if we were successful in obtaining the map.
 			if (map != null) {
 				map.setMyLocationEnabled(true);
 				recreateMap(mdm.getWaypoints(), mdm.getLegs());
 			}
 		}
 	}
 
 	private void setUpLocationClientIfNeeded() {
 		if (lc == null) {
 			lc = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
 					this); // OnConnectionFailedListener
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// Tracing of user
 		map.addPolyline(new PolylineOptions()
 				.add(new LatLng(location.getLatitude(), location.getLongitude()),
 						new LatLng(currentLoc.getLatitude(), currentLoc
 								.getLongitude())).width(5).color(Color.YELLOW));
 
 		currentLoc = location;
 		lm.addEvent(new Event(LocationMonitor.UPDATE_POS, new LatLng(location.getLatitude(), location.getLongitude())));
 	
 		try {
 			lock.acquire();
 			//If we need a Refresh
 			if(needRefresh) {
 				needRefresh = false;
 				//And the leg has actually changed
				if(lastLeg != selectedLeg && lastLeg != null && selectedLeg != null) {
 					//Create our new selected leg
 					Polyline tempLine = map.addPolyline(new PolylineOptions()
 				     .addAll((selectedLeg.points))
 				     .width(5)
 				     .color(Color.RED));
 					
 					//Find its' corresponding leg and remove it
 					for(int i=0; i<lines.size(); i++){
 						if(lines.get(i).equals(tempLine)){
 							lines.get(i).remove();
 							break;
 						}
 					}
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			lock.release();
 		}
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		// do nothing
 
 	}
 
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		lc.requestLocationUpdates(REQUEST, this);
 		currentLoc = lc.getLastLocation();
 
 		if (currentLoc != null) {
 			CameraUpdate startCam = CameraUpdateFactory.newLatLngZoom(
 					new LatLng(currentLoc.getLatitude(), currentLoc
 							.getLongitude()), 17f);
 			map.moveCamera(startCam);
 		}
 
 	}
 
 	@Override
 	public void onDisconnected() {
 		// do nothing
 
 	}
 
 	@Override
 	public void recreateMap(ArrayList<Waypoint> waypoints, ArrayList<Leg> legs) {
 		
 		System.out.println(waypoints.size());
 		for (int i = 0; i < waypoints.size(); i++) {
 			map.addMarker(new MarkerOptions()
 					.position(waypoints.get(i).centerPoint)
 					.title("Waypoint")
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
 		}
 		//This should only be called once for this class, so we create a new array for our polylines
 		lines = new ArrayList<Polyline>();
 		
 		System.out.println(legs.size());
 		for (int i = 0; i < legs.size(); i++) {
 			Polyline addLine = map.addPolyline(new PolylineOptions().addAll(legs.get(i).points)
 												.width(5).color(Color.BLUE));
 			
 			lines.add(addLine);
 		}
 	}
 	
 	public void updateCurrentLeg(Leg l) {
 		try {
 			lock.acquire();
 			lastLeg = selectedLeg;
 			selectedLeg = l;
 			needRefresh = true;
			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			lock.release();
 		}
 	}
 }
