 package tu.kom.uhg;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 
 public class MapActivity extends GenericActivity implements 
 OnMarkerClickListener, android.location.LocationListener{
 	
 	private LocationManager locationManager;
 	private String provider;
 	private GoogleMap map;
 	private static final LatLng MARKER1 = new LatLng(49.8513, 8.63194);
 	private static final LatLng MARKER2 = new LatLng(49.8771, 8.65362);
 	private static final LatLng MARKER3 = new LatLng(49.8777, 8.6519);
 	private static final LatLng MARKER4 = new LatLng(49.8762, 8.65213);
 	private static final LatLng MARKER5 = new LatLng(49.8773, 8.65615);
 	
 	//Trimm-dich-Pfad A (11 Stages)
 	private static final LatLng PARKOURA = new LatLng(49.8762, 8.65472);
 	private static final LatLng STAGEA1 = new LatLng(49.8761, 8.65464);
 	private static final LatLng STAGEA2 = new LatLng(49.877, 8.65383);
 	private static final LatLng STAGEA3 = new LatLng(49.8795, 8.65326);
 	private static final LatLng STAGEA4 = new LatLng(49.8794, 8.653);
 	private static final LatLng STAGEA5 = new LatLng(49.8797, 8.65104);
 	private static final LatLng STAGEA6 = new LatLng(49.8797, 8.65104);
 	private static final LatLng STAGEA7 = new LatLng(49.8796, 8.65092);
 	private static final LatLng STAGEA8 = new LatLng(49.8793, 8.65083);
 	private static final LatLng STAGEA9 = new LatLng(49.8786, 8.65078);
 	private static final LatLng STAGEA10 = new LatLng(49.8782, 8.65073);
 	private static final LatLng STAGEA11 = new LatLng(49.8778, 8.65066);
 	
 	//Trimm-dich-Pfad B (1 Stage, multiply tasks)
 	private static final LatLng PARKOURB = new LatLng(49.8768, 8.65162);
 	
 	//Ruhepunkt A
 	private static final LatLng CHILLPOINTA = new LatLng(49.8782, 8.65371);
 	//Ruhepunkt B
 	private static final LatLng CHILLPOINTB = new LatLng(49.878, 8.65105);
 
 	Context context = this;
 	
 	//removable markers- quiz, info etc (not finished)
 	List<Marker> removableMarkersList = new ArrayList<Marker>();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 		map.setMyLocationEnabled(true);
 		map.setOnMarkerClickListener(this);
 		
 		// Get the location manager
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // Define the criteria how to select the locatioin provider -> use
 	    // default
 	    Criteria criteria = new Criteria();
 	    provider = locationManager.getBestProvider(criteria, false);
 	    Location location = locationManager.getLastKnownLocation(provider);
 	    locationManager.requestLocationUpdates(provider, 400, 1, this);
 	    if (location != null) {
 	        System.out.println("Provider " + provider + " has been selected.");
 	        onLocationChanged(location);
 	    }
 	    
 	    addMarkersToMapOnCreate();
 	}
 	
 	private void addMarkersToMapOnCreate() {
 		//Parkour A
 		Location parkourA_loc = new Location("ParkourA");
 		parkourA_loc.setLatitude(PARKOURA.latitude);
 		parkourA_loc.setLongitude(PARKOURA.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(PARKOURA)
 		.title("Parkour A")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_big)));
 		//Parkour B
 		Location parkourB_loc = new Location("ParkourB");
 		parkourB_loc.setLatitude(PARKOURB.latitude);
 		parkourB_loc.setLongitude(PARKOURB.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(PARKOURB)
 		.title("Parkour B")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_big)));
 		
 		//Chillpoint A
 		Location chillpointA_loc = new Location("ChillpointA");
 		chillpointA_loc.setLatitude(CHILLPOINTA.latitude);
 		chillpointA_loc.setLongitude(CHILLPOINTA.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(CHILLPOINTA)
 		.title("Chillpoint 1")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.ruheplace_big)));
 		//Chillpoint B
 		Location chillpointB_loc = new Location("ChillpointB");
 		chillpointB_loc.setLatitude(CHILLPOINTB.latitude);
 		chillpointB_loc.setLongitude(CHILLPOINTB.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(CHILLPOINTB)
 		.title("Chillpoint 2")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.ruheplace_big)));
 	}
 	
 	private void addMarkersToMap(Location myLoc) {		
 		Location loc1 = new Location("Marker1");
 		loc1.setLatitude(MARKER1.latitude);
 		loc1.setLongitude(MARKER1.longitude);
 		
 		Location loc2 = new Location("Marker2");
 		loc2.setLatitude(MARKER2.latitude);
 		loc2.setLongitude(MARKER2.longitude);
 		
 		Location loc3 = new Location("Marker3");
 		loc3.setLatitude(MARKER3.latitude);
 		loc3.setLongitude(MARKER3.longitude);
 		
 		Location loc4 = new Location("Marker4");
 		loc4.setLatitude(MARKER4.latitude);
 		loc4.setLongitude(MARKER4.longitude);
 		
 		Location loc5 = new Location("Marker5");
 		loc5.setLatitude(MARKER5.latitude);
 		loc5.setLongitude(MARKER5.longitude);
 		
 		map.addMarker(new MarkerOptions()
 			.position(MARKER1)
 			.title("quizquiz")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 		
 		if (loc2.distanceTo(myLoc) <= ACTIVATION_DISTANCE)
 			map.addMarker(new MarkerOptions()
 	        .position(MARKER2)
 	        .title("quizquiz")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 		if (loc3.distanceTo(myLoc) <= ACTIVATION_DISTANCE)
 			map.addMarker(new MarkerOptions()
 	        .position(MARKER3)
 	        .title("quizquiz")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 		
 		if (loc4.distanceTo(myLoc) <= ACTIVATION_DISTANCE)
 			map.addMarker(new MarkerOptions()
 	        .position(MARKER4)
 	        .title("quizquiz")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 		
 		if (loc5.distanceTo(myLoc) <= ACTIVATION_DISTANCE)
 			map.addMarker(new MarkerOptions()
 	        .position(MARKER5)
 	        .title("quizquiz")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 	}
 	
 	@Override
     public boolean onMarkerClick(final Marker marker) {
 		//Toast.makeText(MapActivity.this, "Marker " + marker.getTitle() + " clicked", Toast.LENGTH_LONG).show();
 		
 		//check markers ID
 		if(marker.getTitle().equals("Parkour A")){
 			final AlertDialog alertDialog;
 			alertDialog = new AlertDialog.Builder(MapActivity.this).create();
 			alertDialog.setTitle("Trimm-dich-Pfad");
 			alertDialog.setMessage("Der Halt-Dich-Fit Parkour bietet mit seinen 11 Stationen " +
 					"verteilt über den Herrngarten den idealen Ausgleich zum Büroalltag. " +
 					"Mit kleinen Übungen kannst du deine Fitness täglich verbessern. \nParkour starten?");
 			//ja button
 			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ja", new DialogInterface.OnClickListener(){
 					
 				public void onClick(DialogInterface dialog, int which) {
 					//start Trimm-dich-Pfad
 					startParkourA();
 				}
 			});
 			//nein button
 			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which){
 					alertDialog.cancel();
 				}
 			});
 			alertDialog.show();
 		} else if(marker.getTitle().equals("Parkour B")){
 			final AlertDialog alertDialog;
 			alertDialog = new AlertDialog.Builder(MapActivity.this).create();
 			alertDialog.setTitle("Trimm-dich-Pfad");
 			alertDialog.setMessage("Der Rückenfit Parkour bietet dir ein gezieltes Training " +
 					"für den vom Bürostuhl gestressten Rücken. \nParkour starten?");
 			//ja button
 			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ja", new DialogInterface.OnClickListener(){
 					
 				public void onClick(DialogInterface dialog, int which) {
 					//Parkour B will be played only on one place, so no additional markers needed
 					Intent intent = new Intent(MapActivity.this, ParkourActivity.class);
 					intent.putExtra("markerTitle", marker.getTitle());
 			        startActivity(intent);
 				}
 			});
 			//nein button
 			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which){
 					alertDialog.cancel();
 				}
 			});
 			alertDialog.show();
 		} else if (marker.getTitle().subSequence(0, 7).equals("Station")) {
 			Intent intent = new Intent(MapActivity.this, ParkourActivity.class);
 			intent.putExtra("markerTitle", marker.getTitle());
 	        startActivity(intent);
 		} else if (marker.getTitle().contains("Chillpoint"))  {
 			Intent intent = new Intent(MapActivity.this, ParkourActivity.class);
 			intent.putExtra("markerTitle", marker.getTitle());
 	        startActivity(intent);
 		} else {//TOOD FIXME
 			Intent intent = new Intent(MapActivity.this, QuizActivity.class);
 			intent.putExtra("markerId", marker.getId());
 	        startActivity(intent);
 		}
         
 		// We return false to indicate that we have not consumed the event and that we wish
         // for the default behavior to occur (which is for the camera to move such that the
         // marker is centered and for the marker's info window to open, if it has one).
 		return false;
 	}
 
 	/* Request updates at startup */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		locationManager.requestLocationUpdates(provider, 400, 1, this);
 	}
 	
 	@Override
 	protected void onPause() {
 	  super.onPause();
 	  locationManager.removeUpdates(this);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		double lat = location.getLatitude();
 		double lon = location.getLongitude();
 		LatLng target = new LatLng(lat, lon);
 		//move camera center to the current position
 		map.animateCamera(CameraUpdateFactory.newLatLng(target));
 		//map.clear();
 		clearMapFromHidableMarkers();
 	    addMarkersToMap(location);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	  // TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 	
 	private void clearMapFromHidableMarkers() {
 		map.clear();
 		addMarkersToMapOnCreate();
 	}
 	
 	private void startParkourA() {
 		//clear map
 		map.clear();		
 		//show Stages 1-11
 		Location stageA1_loc = new Location("ParkourA1");
 		stageA1_loc.setLatitude(STAGEA1.latitude);
 		stageA1_loc.setLongitude(STAGEA1.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA1)
 		.title("Station 1")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA2_loc = new Location("ParkourA2");
 		stageA2_loc.setLatitude(STAGEA2.latitude);
 		stageA2_loc.setLongitude(STAGEA2.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA2)
 		.title("Station 2")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA3_loc = new Location("ParkourA3");
 		stageA3_loc.setLatitude(STAGEA3.latitude);
 		stageA3_loc.setLongitude(STAGEA3.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA3)
 		.title("Station 3")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA4_loc = new Location("ParkourA4");
 		stageA4_loc.setLatitude(STAGEA4.latitude);
 		stageA4_loc.setLongitude(STAGEA4.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA4)
 		.title("Station 4")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA5_loc = new Location("ParkourA5");
 		stageA5_loc.setLatitude(STAGEA5.latitude);
 		stageA5_loc.setLongitude(STAGEA5.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA5)
 		.title("Station 5")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA6_loc = new Location("ParkourA6");
 		stageA6_loc.setLatitude(STAGEA6.latitude);
 		stageA6_loc.setLongitude(STAGEA6.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA6)
 		.title("Station 6")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA7_loc = new Location("ParkourA7");
 		stageA7_loc.setLatitude(STAGEA7.latitude);
 		stageA7_loc.setLongitude(STAGEA7.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA7)
 		.title("Station 7")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA8_loc = new Location("ParkourA8");
 		stageA8_loc.setLatitude(STAGEA8.latitude);
 		stageA8_loc.setLongitude(STAGEA8.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA8)
 		.title("Station 8")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA9_loc = new Location("ParkourA9");
 		stageA9_loc.setLatitude(STAGEA9.latitude);
 		stageA9_loc.setLongitude(STAGEA9.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA9)
 		.title("Station 9")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA10_loc = new Location("ParkourA10");
 		stageA10_loc.setLatitude(STAGEA10.latitude);
 		stageA10_loc.setLongitude(STAGEA10.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA10)
 		.title("Station 10")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 		Location stageA11_loc = new Location("ParkourA11");
 		stageA11_loc.setLatitude(STAGEA11.latitude);
 		stageA11_loc.setLongitude(STAGEA11.longitude);
 		map.addMarker(new MarkerOptions()
 		.position(STAGEA11)
 		.title("Station 11")
         .icon(BitmapDescriptorFactory.fromResource(R.drawable.activities_small)));
 		
 	}
 }
