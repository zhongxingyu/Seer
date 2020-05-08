 package ru.valfom.whatisthedistance;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class MainActivity extends FragmentActivity {
 
 	private GoogleMap map;
 	
 	private List<CustomMarker> markers = new ArrayList<CustomMarker>();
 	
 	private long lastBackPressTime = 0;
 	private Toast toastOnExit;
 	
 	private double distance;
 	private TextView tvDistance;
 	private TextView tvDistanceUnit;
 	private double ratio = 1000;
 	private boolean showMarkers = true;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		
 		tvDistance = (TextView) findViewById(R.id.tvDistance);
 		tvDistanceUnit = (TextView) findViewById(R.id.tvDistanceUnit);
 		
 		setUpMapIfNeeded();
 	}
 	
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 		
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		String mapType = sharedPreferences.getString("lMapType", getString(R.string.settings_normal));
 		
 		if (mapType.equals(getString(R.string.settings_normal))) map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		else if (mapType.equals(getString(R.string.settings_satellite))) map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 		else if (mapType.equals(getString(R.string.settings_hybrid))) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		
 		String units = sharedPreferences.getString("lUnits", getString(R.string.km));
 		
 		if (units.equals(getString(R.string.km))) {
 			
 			tvDistanceUnit.setText(R.string.km);
 			ratio = 1000;
 			
 		} else if (units.equals(getString(R.string.mi))) {
 			
 			tvDistanceUnit.setText(R.string.mi);
 			ratio = 1609.344;
 		}
 		
 		tvDistance.setText(String.format("%.3f", distance / ratio));
 	}
 
 	private void setUpMapIfNeeded() {
 		
         if (map == null)
         	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 
         if (map == null) return;
 
         map.setMapType(GoogleMap.MAP_TYPE_NONE);
         map.setMyLocationEnabled(true);
         
         map.setOnMapClickListener(new OnMapClickListener() {
 			
 			@Override
 			public void onMapClick(LatLng point) {
 				
 				if (markers.size() > 1) {
 				
 					showMarkers = !showMarkers;
 					
 					for (CustomMarker customMarker : markers) {
 						
 						customMarker.getMarker().setVisible(showMarkers);
 					}
 				}
 			}
 		});
         
         map.setOnMapLongClickListener(new OnMapLongClickListener() {
 			
 			@Override
 			public void onMapLongClick(LatLng point) {
 				
 				Marker marker = map.addMarker(new MarkerOptions()
 						.position(point)
 						.draggable(true)
 						.visible(showMarkers));
 				
 				CustomMarker customMarker = new CustomMarker();
 				
 				customMarker.setMarker(marker);
 				customMarker.setId(markers.size());
 				
 				if (markers.size() > 0) {
 					
 					double curDistance;
 					CustomMarker prevMarker = markers.get(markers.size() - 1);
 					LatLng prevPoint = prevMarker.getMarker().getPosition();
 					
 					Polyline polyline = map.addPolyline(new PolylineOptions()
 				    		.add(prevPoint, point)
 				    		.width(5)
 				    		.color(Color.RED));
 					
 					curDistance = calculateDistance(prevPoint.latitude, prevPoint.longitude, point.latitude, point.longitude);
 					
 					distance += curDistance;
 					
 					tvDistance.setText(String.format("%.3f", distance / ratio));
 					
 					prevMarker.setPolyline(polyline);
 					prevMarker.setDistance(curDistance);
 				}
 				
 				markers.add(customMarker);
 			}
 		});
         
         map.setOnMarkerDragListener(new OnMarkerDragListener() {
 			
 			@Override
			public void onMarkerDragStart(Marker marker) {}
 			
 			@Override
 			public void onMarkerDragEnd(Marker marker) {}
 			
 			@Override
 			public void onMarkerDrag(Marker marker) {
 				
 				for (CustomMarker customMarker : markers) {
 					
 					if (customMarker.getMarker().equals(marker)) {
 						
 						Double prevDistance = null, nextDistance = null;
 						int id = customMarker.getId();
 						
 						if (id > 0) {
 							
 							CustomMarker prevCustomMarker = markers.get(id - 1);
 						
 							if (prevCustomMarker.isPolyline()) {
 								
 								Polyline polyline = prevCustomMarker.getPolyline();
 								
 								List<LatLng> points = polyline.getPoints();
 								
 								LatLng prevPoint = points.get(0);
 								LatLng curPoint = marker.getPosition();
 								
 								polyline.remove();
 								
 								Polyline polylineNew = map.addPolyline(new PolylineOptions()
 					    				.add(prevPoint, curPoint)
 					    				.width(5)
 					    				.color(Color.RED));
 								
 								prevDistance = calculateDistance(prevPoint.latitude, prevPoint.longitude, curPoint.latitude, curPoint.longitude);
 								
 								prevCustomMarker.setPolyline(polylineNew);
 								prevCustomMarker.setDistance(prevDistance);
 							}
 						}
 						
 						if (id < markers.size() - 1) {
 							
 								if (customMarker.isPolyline()) {
 								
 								Polyline polyline = customMarker.getPolyline();
 								
 								List<LatLng> points = polyline.getPoints();
 								
 								LatLng nextPoint = points.get(1);
 								LatLng curPoint = marker.getPosition();
 								
 								polyline.remove();
 								
 								Polyline polylineNew = map.addPolyline(new PolylineOptions()
 					    				.add(curPoint, nextPoint)
 					    				.width(5)
 					    				.color(Color.RED));
 								
 								nextDistance = calculateDistance(curPoint.latitude, curPoint.longitude, nextPoint.latitude, nextPoint.longitude);
 								
 								customMarker.setPolyline(polylineNew);
 								customMarker.setDistance(nextDistance);
 							}
 						}
 						
 						distance = 0;
 						
 						for (int i = 0; i < markers.size(); i++) {
 							
 							if ((i == (id - 1)) || (i == id)) continue;
 							
 							distance += markers.get(i).getDistance();
 						}
 						
 						if (prevDistance != null) distance += prevDistance;
 						if (nextDistance != null) distance += nextDistance;
 						
 						tvDistance.setText(String.format("%.3f", distance / ratio));
 						
 						break;
 					}
 				}
 			}
 		});
     }
 	
 	private double calculateDistance(double llat1, double llong1, double llat2, double llong2) {
 
 		// http://gis-lab.info/qa/great-circles.html
 
 		int rad = 6372795;
 
 		double lat1 = llat1 * Math.PI / 180;
 		double lat2 = llat2 * Math.PI / 180;
 		double long1 = llong1 * Math.PI / 180;
 		double long2 = llong2 * Math.PI / 180;
 
 		double cl1 = Math.cos(lat1);
 		double cl2 = Math.cos(lat2);
 		double sl1 = Math.sin(lat1);
 		double sl2 = Math.sin(lat2);
 		double delta = long2 - long1;
 		double cdelta = Math.cos(delta);
 		double sdelta = Math.sin(delta);
 
 		double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
 		double x = sl1 * sl2 + cl1 * cl2 * cdelta;
 		double ad = Math.atan2(y, x);
 		double dist = ad * rad;
 
 		return dist;
 	}
 
 	public double round(double d, int p) {
 
     	return new BigDecimal(d).setScale(p, RoundingMode.HALF_UP).doubleValue();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		switch (item.getItemId()) {
 
 		case R.id.action_settings:
 			Intent settings = new Intent(this, SettingsActivity.class);
 			startActivity(settings);
 			break;
 		case R.id.action_clear_map:
 			map.clear();
 			tvDistance.setText(getString(R.string.default_value_distance));
 			distance = 0;
 			markers.clear();
 			break;
 		case R.id.action_delete_last_marker:
 			
 			int markersCount = markers.size();
 			
 			if (markersCount == 1) {
 				
 				map.clear();
 				markers.clear();
 			} else if (markersCount > 1) {
 				
 				CustomMarker customMarker = markers.get(markersCount - 1);
 				customMarker.getMarker().remove();
 				markers.remove(markersCount - 1);
 				CustomMarker prevCustomMarker = markers.get(markers.size() - 1);
 				prevCustomMarker.getPolyline().remove();
 				distance -= prevCustomMarker.getDistance();
 				prevCustomMarker.setDistance(0);
 				
 				tvDistance.setText(String.format("%.3f", distance / ratio));
 			}
 			
 			if ((markers.size() <= 1) && !showMarkers) {
 				
 				showMarkers = true;
 				
 				for (CustomMarker customMarker : markers) {
 					
 					customMarker.getMarker().setVisible(showMarkers);
 				}
 			}
 			
 			break;
 			
 		default:
 			break;
 		}
 
 		return true;
 	}
 	
 	@Override
 	public void onBackPressed() {
 
 		if (lastBackPressTime < (System.currentTimeMillis() - 2000)) {
 
 		    toastOnExit = Toast.makeText(this, getString(R.string.message_on_exit), Toast.LENGTH_SHORT);
 		    toastOnExit.show();
 		    lastBackPressTime = System.currentTimeMillis();
 
 		  } else {
 
 			  if (toastOnExit != null) toastOnExit.cancel();
 
 			  super.onBackPressed();
 		 }
 	}
 }
