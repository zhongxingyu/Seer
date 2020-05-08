 package projet.locusta.mapacti;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import locusta.project.entities.Event;
 import locusta.project.entities.User;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class LocustaMapActivityMain extends MapActivity {
 	
 	private MapView mapView;
 	private MapController mapController;
 	private List<Overlay> mapOverlays;
 	private Map<Integer, MapItemizedOverlay> itemzedOverlays;
 	private GeolocalisationService geolocalisationService;
 	
 	private float distance = 10000.0f; // The event distance in meter
 	private int zoomLevel = 17; // Map zoom
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_locusta_map);
 		
 		// Get map service
 		mapView = (MapView) findViewById(R.id.mapview);
 	    mapView.setBuiltInZoomControls(true);
 	    
 	    // Get map controller
 	    mapController = mapView.getController();
 	    mapController.setZoom(zoomLevel);
 	    
 	    // Get the geolocalisation service
 	    geolocalisationService = new GeolocalisationService(this);
 	    
 	    // Add item's icons in a Map
 	    itemzedOverlays = new ItemizedOverlaysInitialization().init(this);
 	    
 	    
 	    mapOverlays = mapView.getOverlays();
 	    // Add items to map
 	    for (MapItemizedOverlay item : itemzedOverlays.values()) {
 			mapOverlays.add(item);
 		}
 	    
 	    
 	    // test
 	    Date d = new Date();
 	    User u = new User("userName", "pass");
 	    
 	    Event rennes = new Event("La rue de la soif", "De la boisson à foison :)", d, -1.678905f, 48.112474f, u);
 	    rennes.getEventType().setId(37);
 	    
 	    Event rennesBouffe = new Event("La rue de la bouffe", "De la bouffe à foison :)", d, -1.681255f, 48.105397f, u);
 	    rennesBouffe.getEventType().setId(39);
 	    
 	    Collection<Event> events = new ArrayList<Event>();
 	    events.add(rennes);
 	    events.add(rennesBouffe);
 	    addEvents(events);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_locusta_map, menu);
 		return true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	/**
 	 * Menu selection
 	 */
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
 		case R.id.menu_clear_events :
 			clearEvents();
 			Toast.makeText(getApplicationContext(), "Events cleared", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.menu_friends :
 			// TODO La partie de Dany :)
 			Toast.makeText(getApplicationContext(), "La partie de Dany :)", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.menu_current_location :
 			Location currentLocation = geolocalisationService.getLocation();
 
 			String msg = String.format(
 					getResources().getString(R.string.display_current_location), currentLocation.getLatitude(),
 					currentLocation.getLongitude());
 			
 			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
 		default:
 			System.out.println("Menu id unrecognized");
 			break;
 		}
     	return true;
     }
     
     //---------------- Event ----------------//
 	
 	/**
 	 * Add events on the map
 	 * @param events
 	 */
 	public void addEvents(Collection<Event> events) {
 		for (Event event : events) {
 			itemzedOverlays.get(event.getEventType().getId()).addOverlay(createOverlayItem(event));
 		}
 	}
 	
 	private OverlayItem createOverlayItem(Event event) {
 		GeoPoint point = new GeoPoint((int)(event.getLatitude() * 1E6), (int)(event.getLongitude() * 1E6));
 	    return new OverlayItem(point, event.getName(), event.getDescription());
 	}
 	
 	/**
 	 * clear the event on the map
 	 */
 	public void clearEvents() {
 		for (MapItemizedOverlay item : itemzedOverlays.values()) {
 			item.clearOverlays();
 		}
 	}
 	
 	//---------------- Location ----------------//
 	
 	protected void onResume() {
 		super.onResume();
 		geolocalisationService.onResume();
 	}
 	
 	protected void onPause() {
 		super.onPause();
 		geolocalisationService.onPause();
 	}
 	
 	/**
 	 * Map position refresh
 	 * @param p
 	 */
 	public void onLocationChanged(GeoPoint p) {
 		mapController.animateTo(p);
 		mapController.setCenter(p);
 	}
 	
 	public void showToast(String message) {
 		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
 	}
 
 }
