 package com.eyecall.volunteer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.widget.RadioGroup;
 
 import com.eyecall.event.ClickEvent;
 import com.eyecall.eventbus.Event;
 import com.eyecall.eventbus.EventBus;
 import com.eyecall.eventbus.EventListener;
 import com.eyecall.eventbus.InputEventListener;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 public class LocationActivity extends FragmentActivity implements EventListener, OnMapLongClickListener{
 	
 	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
    private com.eyecall.connection.Connection connection;
     
     private Location location;
     
     private GoogleMap map;
     
     private Marker marker;
 	//private VolunteerProtocolHandler protocolHandler;
 	
     //private PreferencesManager preferencesManager;
 	
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	this.setContentView(R.layout.activity_location);
     	
     	//preferencesManager = new PreferencesManager(this);
     	
     	// Set location
     	Intent intent = getIntent();
     	if(intent.hasExtra("location")){
     		location = intent.getParcelableExtra("location");
     	}
     	
     	// Register listeners and events
     	registerEvents();
     	
     	// Init map
     	initMap();
     }
     
     @Override
     public void onMapLongClick(LatLng point) {
     	marker.setPosition(point);
     }
     
     private void initMap(){
     	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
     	if(status==ConnectionResult.SUCCESS){
     		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map)).getMap();
             if(map!=null){
             	map.setMyLocationEnabled(true);
                 LatLng position;
                 if(location==null){
                 	position = new LatLng(0,0);
                 	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                 }else{
                 	position = new LatLng(location.getLatitude(), location.getLongitude());
                 	map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
                 }
                 map.setOnMapLongClickListener(this);
                 marker = map.addMarker(new MarkerOptions().title("Location").position(position));
             }else{
             	logger.warn("Map not loaded, onCreateView not finished");
             }
     	}else{
     		logger.warn("Map not loaded, GooglePlayServices unavailable:");
     		if(status==ConnectionResult.SERVICE_MISSING){
     			logger.warn("SERVICE_MISSING");
     		}else if(status==ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
     			logger.warn("SERVICE_VERSION_UPDATE_REQUIRED");
     		}else if(status==ConnectionResult.SERVICE_DISABLED){
     			logger.warn("SERVICE_DISABLED");
     		}else if(status==ConnectionResult.SERVICE_INVALID){
     			logger.warn("SERVICE_INVALID");
     		}
     	}
     	
     	
     	getSupportFragmentManager().executePendingTransactions();
     	// Get a handle to the Map Fragment
     	
     }
     
     private void registerEvents(){
     	findViewById(R.id.location_button_cancel).setOnClickListener(new InputEventListener(EventTag.CANCEL_LOCATION_ADD, null));
     	findViewById(R.id.location_button_save).setOnClickListener(new InputEventListener(EventTag.SAVE_LOCATION, null));
     	EventBus.getInstance().subscribe(this);
     }
 
 	@Override
 	public void onEvent(Event e) {
 		if(e instanceof ClickEvent){
 			ClickEvent event = (ClickEvent) e;
 			if(event.getTag().equals(EventTag.CANCEL_LOCATION_ADD.getName())){
 				this.finish();
 			}else if(event.getTag().equals(EventTag.SAVE_LOCATION.getName())){
 				if(location==null){
 					location = new Location();
 				}else{
 					// First remove old location
					VolunteerProtocolHandler.removeLocation(location);
 					logger.debug("Removed location: {}", location.toString());
 				}
 				//location.setLatitude(map.)
 				RadioGroup radios = (RadioGroup) findViewById(R.id.location_radiogroup_preferred);
 				
 				int selected = radios.getCheckedRadioButtonId();
 				
 				location.setPreferred(selected==R.id.location_radio_preferred);
 				location.setLatitude(marker.getPosition().latitude);
 				location.setLongitude(marker.getPosition().longitude);
 				location.setRadius(0);
 				
 				// Add new location
				VolunteerProtocolHandler.addLocation(location);
 				logger.debug("Added location: {}", location.toString());
 				
 				
 			}
 		}
 		
 	}
 }
