 package at.roadrunner.android.activity;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import at.roadrunner.android.R;
 import at.roadrunner.android.model.Delivery;
 import at.roadrunner.android.util.AddressOverlay;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class DeliveryMap extends MapActivity {
 	private Geocoder _geocoder;
 	private MapView _mapView;
 	private static final double FACTOR = 1E6;
 	private AddressOverlay _addressOverlay;
 	private List<Overlay> _addressOverlays;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_map);
 	    
 	    // get the Delivery Object
 	    Bundle extras = getIntent().getExtras();
         Delivery delivery = (Delivery) extras.getSerializable("Delivery");
         
         
         if (delivery != null) {
         	_geocoder = new Geocoder(this);
         	_mapView = (MapView) findViewById(R.id.mapview);
         	GeoPoint geoFrom = null;
         	GeoPoint geoTo = null;
         	
         	// create overlays
         	_addressOverlays = _mapView.getOverlays();
        	    Drawable drawable = this.getResources().getDrawable(R.drawable.ic_deliverymap_marker);
        	    _addressOverlay = new AddressOverlay(drawable, getApplicationContext());
         	
        	    // get lat/lng values of addresses
 	       	List<Address> from = null;
 	       	List<Address> to = null;
        	    try {
 				from = _geocoder.getFromLocationName(delivery.getFrom().getFormatedAddressForGoogleMap(), 1);
 				to = _geocoder.getFromLocationName(delivery.getDestination().getFormatedAddressForGoogleMap(), 1);
				
				if (from != null) {
					geoFrom = new GeoPoint((int) (from.get(0).getLatitude() * FACTOR), (int) (from.get(0).getLongitude() * FACTOR));
					_addressOverlay.addOverlay(new OverlayItem(geoFrom, "From", delivery.getFrom().toString()));
				}
				
				if (to != null) {
					geoTo = new GeoPoint((int) (to.get(0).getLatitude() * FACTOR), (int) (to.get(0).getLongitude() * FACTOR));
					_addressOverlay.addOverlay(new OverlayItem(geoTo, "From", delivery.getDestination().toString()));
				}
				_addressOverlays.add(_addressOverlay);
				
				if (geoFrom != null) { 
					_mapView.getController().animateTo(geoTo);
					_mapView.getController().setZoom(10);
				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	
 
 }
