 package be.uclouvain.sinf1225.gourmet;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import be.uclouvain.sinf1225.gourmet.models.City;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationListener;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationReceiver;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 public class RestaurantMapView extends Activity implements GourmetLocationReceiver
 {
 	private GourmetLocationListener locationListener;
 	private HashMap<Marker,Restaurant> markerToRestaurant = null;
 	
 	protected GoogleMap getMap()
 	{
 		return ((MapFragment) getFragmentManager().findFragmentById(R.id.RestaurantListMap)).getMap();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_restaurant_list_map);
 		
 		// Initialisation des services de localisation
 		locationListener = new GourmetLocationListener(this,this).init();
 		
 		GoogleMap map = getMap();
 		
 		if(markerToRestaurant == null)
 		{
 			markerToRestaurant = new HashMap<Marker,Restaurant>();
 			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		
			List<Restaurant> restaurants = Restaurant.getAllRestaurants();
 			for(Restaurant restaurant : restaurants)
 			{
 				Marker marker = getMap().addMarker(new MarkerOptions()
 		        .position(new LatLng(restaurant.getLocation().getLatitude(), restaurant.getLocation().getLongitude()))
 		        .title(restaurant.getName()));
 				markerToRestaurant.put(marker, restaurant);
 			}
 			
 			map.setOnMarkerClickListener(new OnMarkerClickListener()
 			{
 				@Override
 				public boolean onMarkerClick(Marker arg0)
 				{
 					Restaurant restaurant = markerToRestaurant.get(arg0);
 					
 					Intent intent = new Intent(RestaurantMapView.this, RestaurantListView.class);
 				    intent.putExtra("name", restaurant.getName());
 				    startActivity(intent);
 				    
 					return true;
 				}
 			});
 		}
 	}
 
 	@Override
 	public void onPause()
 	{
 		if(locationListener != null)
 			locationListener.close();
 		locationListener = null;
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop()
 	{
 		if(locationListener != null)
 			locationListener.close();
 		locationListener = null;
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume()
 	{
 		if(locationListener == null)
 			locationListener = new GourmetLocationListener(this,this);
 		super.onResume();
 	}
 	
 	@Override
 	public void onLocationUpdate(Location loc)
 	{
 	}
 }
