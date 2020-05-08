 package com.amca.android.replace.place;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 import com.amca.android.replace.R;
 import com.amca.android.replace.model.Place;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PlaceSelectorMap extends SherlockFragmentActivity implements
 		OnInfoWindowClickListener {
 
 	private Integer userId;
 	private GoogleMap map = null;
 	private Float currentLat, currentLng;
 	private String jsonValue, title;
 	private List<Place> placesList = new ArrayList<Place>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_place_selector_map);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		Intent intent = getIntent();
 		this.currentLat = intent.getFloatExtra("currentLat", 0);
 		this.currentLng = intent.getFloatExtra("currentLng", 0);
 		this.jsonValue = intent.getStringExtra("jsonValue");
 		this.userId = intent.getIntExtra("userId", 0);
 		this.title = intent.getStringExtra("title");
 		setTitle(title);
 
 		setUpMap();
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		String markerId = marker.getId();
 		String[] s = markerId.split("m");
 		int id = Integer.parseInt(s[1]);
 		if (id >= 0 && id < placesList.size()) {
 			Place place = placesList.get(id);
 			Intent intent = new Intent(PlaceSelectorMap.this, PlaceDetail.class);
 			intent.putExtra("userId", this.userId);
 			intent.putExtra("typeId", place.getPlaceType());
			intent.putExtra("placeId", place.getPlaceId());
			intent.putExtra("placeName", place.getPlaceName());
			intent.putExtra("placeAddress", place.getPlaceAddress());
			intent.putExtra("placeDesc", place.getPlaceDesc());
			intent.putExtra("placeReviews", place.getPlaceReviews().toString());
			intent.putExtra("placeDistance", place.getPlaceDistance());
			intent.putExtra("averagePoint", place.getAveragePoint());
 			startActivity(intent);
 		}
 	}
 
 	private void setUpMap() {
 		// Do a null check to confirm that we have not already instantiated the
 		// map.
 		if (map == null) {
 			map = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 			// Check if we were successful in obtaining the map.
 			if (map != null) {
 				// The Map is verified. It is now safe to manipulate the map.
 				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
 						this.currentLat, this.currentLng), 14));
 				map.animateCamera(CameraUpdateFactory.zoomIn());
 				map.setOnInfoWindowClickListener(this);
 
 				try {
 					JSONArray jArray = new JSONArray(this.jsonValue);
 					for (int i = 0; i < jArray.length(); i++) {
 						JSONObject json_data = jArray.getJSONObject(i);
 
 						Place place = new Place();
 						place.setPlaceId(json_data.getInt("placeId"));
 						place.setPlaceName(json_data.getString("placeName"));
 						place.setPlaceDesc(json_data.getString("placeDesc"));
 						place.setPlaceLat(json_data.getString("placeLat"));
 						place.setPlaceLng(json_data.getString("placeLng"));
 						place.setPlaceType(json_data.getInt("placeType"));
 						place.setPlaceAddress(json_data.getString("placeAddress"));
 						place.setPlaceReviews(json_data.getInt("placeReviews"));
 						place.setPlaceDistance(json_data.getDouble("placeDistance"));
 						place.setAveragePoint(json_data.getDouble("averagePoint"));
 						placesList.add(place);
 
 						map.addMarker(new MarkerOptions()
 								.position(
 										new LatLng(Double.parseDouble(place
 												.getPlaceLat()), Double
 												.parseDouble(place
 														.getPlaceLng())))
 								.title(place.getPlaceName())
 								.snippet(place.getPlaceDesc()));
 					}
 				} catch (JSONException e) {
 					Toast.makeText(getApplicationContext(),
 							"Error parsing data " + e.toString(),
 							Toast.LENGTH_SHORT).show();
 				}
 
 				map.addMarker(new MarkerOptions()
 						.position(new LatLng(this.currentLat, this.currentLng))
 						.title("You are here")
 						.icon(BitmapDescriptorFactory
 								.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 			}
 		}
 	}
 }
