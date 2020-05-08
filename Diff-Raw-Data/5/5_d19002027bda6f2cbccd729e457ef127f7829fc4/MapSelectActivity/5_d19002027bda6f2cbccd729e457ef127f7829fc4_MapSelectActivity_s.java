 package com.android.lonoti.activies.map;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.android.lonoti.R;
 import com.android.lonoti.R.id;
 import com.android.lonoti.R.layout;
 import com.android.lonoti.R.menu;
 import com.android.lonoti.activities.LonotiEventCreate;
 import com.android.lonoti.adapter.PopupAdapter;
 import com.android.lonoti.adapter.data.MarkerContent;
 import com.android.lonoti.bom.payload.Location;
 import com.android.lonoti.exception.NetworkException;
 import com.android.lonoti.location.LonotiLocationPlaces;
 import com.android.lonoti.network.LonotiAsyncServiceRequest;
import com.android.lonoti.network.LonotiTaskListener;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.model.Circle;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class MapSelectActivity extends FragmentActivity {
 
 	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
 	
 	private Marker marker;
 	private Circle circle;
 	private GoogleMap map;
 	private TextView radiusText;
 	Map<Marker, MarkerContent> data;
 	
 	private String markerDescription;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map_select);
 		
 		String lat = getIntent().getStringExtra("lonoti_location_latitude");
 		String lon = getIntent().getStringExtra("lonoti_location_longitude");
 		markerDescription = getIntent().getStringExtra("lonoti_location_description");
 		
 		LatLng selectedLocation = new LatLng(Double.valueOf(lat),Double.valueOf(lon));
 		
 		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 		
 		SupportMapFragment fr = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map));
 		
 		map = fr.getMap();
 		
 		Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
 		        .title("Hamburg"));
 		// Move the camera instantly to hamburg with a zoom of 15.
 	    
 	    Marker selectedMarker = map.addMarker(new MarkerOptions().position(selectedLocation));
 	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
 
 	    // Zoom in, animating the camera.
 	    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
 	    
 	    data = new HashMap<Marker, MarkerContent>();
 		MarkerContent c1 = new MarkerContent("1");
 		data.put(hamburg, c1);
 		
 		MarkerContent c2 = new MarkerContent("2");
 		data.put(selectedMarker, c2);
 		
 		map.setInfoWindowAdapter(new PopupAdapter(this, getLayoutInflater(), data));
 		
 		map.setOnMapLongClickListener(new OnMapLongClickListener() {
 			
 			@Override
 			public void onMapLongClick(LatLng arg0) {
 				// TODO Auto-generated method stub
 				
 				if(marker != null){
 					marker.remove();
 				}
 				
 				marker = map.addMarker(new MarkerOptions().position(arg0));
 				data.put(marker, new MarkerContent("3"));
 				
 				Location location = new Location(String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude), "");
 				
				AsyncTask<Object, Integer, Long> execute = new LonotiAsyncServiceRequest(new LonotiTaskListener() {
 					
 					@Override
 					public void doTask(String response) {
 						// TODO Auto-generated method stub
 						markerDescription = response;
 					}
 
 					@Override
 					public void doTask(Location loc) {
 						// TODO Auto-generated method stub
 					}
 				});
 				
 				execute.execute("LOCATION_SEARCH",location);
 				
 				/*try {
 					markerDescription = LonotiLocationPlaces.getLocationDescription(location);
 				} catch (NetworkException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}*/
 				
 			}
 		});
 
 		TextView locationText = (TextView) findViewById(R.id.location_text);
 		
 		locationText.setText(lat + ", " + lon);
 
 		radiusText = (TextView) findViewById(R.id.radius_text);
 
 		SeekBar bar = (SeekBar) findViewById(R.id.seekBar1);
 		
 		//bar.setMax(48);
 		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 				// TODO Auto-generated method stub
 				radiusText.setText(String.valueOf(arg1 + 2));
 				if(circle != null){
 					circle.remove();
 				}
 				CircleOptions co = new CircleOptions();
 				co.center(marker.getPosition());
 				co.radius((arg1+2)*1000);
 				co.fillColor(Color.TRANSPARENT);
 				co.strokeColor(Color.BLUE);
 				circle = map.addCircle(co);
 				
 			}
 		});
 
 		
 		//SearchView view = findViewById(R.id.s)
 		
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub
 		
 		if(R.id.done_item == item.getItemId()){
 			
 			if(marker == null){
 				
 				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 				
 				alertDialog.setTitle("Select a location");
 				alertDialog.setIcon(android.R.drawable.stat_notify_error);
 				
 				alertDialog.show();
 				return false;
 				
 			} else{
 				Intent intent = new Intent();
 				intent.putExtra("lonoti_location_latitude", marker.getPosition().latitude);
 				intent.putExtra("lonoti_location_longitude", marker.getPosition().longitude);
 				intent.putExtra("lonoti_location_description", markerDescription);
 				setResult(RESULT_OK,intent);
 				//startActivity(intent);
 				finish();
 				return true;
 			}
 			
 		}
 		
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map_select, menu);
 		return true;
 	}
 
 	public Marker getMarker() {
 		return marker;
 	}
 
 	public void setMarker(Marker marker) {
 		this.marker = marker;
 	}
 
 	public Circle getCircle() {
 		return circle;
 	}
 
 	public void setCircle(Circle circle) {
 		this.circle = circle;
 	}
 
 	public GoogleMap getMap() {
 		return map;
 	}
 
 	public void setMap(GoogleMap map) {
 		this.map = map;
 	}
 
 	public TextView getRadiusText() {
 		return radiusText;
 	}
 
 	public void setRadiusText(TextView radiusText) {
 		this.radiusText = radiusText;
 	}
 
 	public Map<Marker, MarkerContent> getData() {
 		return data;
 	}
 
 	public void setMarkerDescription(String markerDescription) {
 		this.markerDescription = markerDescription;
 	}
 	
 	public String getMarkerDescription() {
 		return markerDescription;
 	}
 }
