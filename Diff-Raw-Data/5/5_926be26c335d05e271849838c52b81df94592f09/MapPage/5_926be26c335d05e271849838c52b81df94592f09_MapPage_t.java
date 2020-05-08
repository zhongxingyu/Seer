 /**
  * 
  */
 package edu.mills.cs180.safetravels;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 /**
  * @author KateFeeney
  *
  */
 public class MapPage extends MapActivity implements OnClickListener {
 	private MapView map;
 	private MapController controller;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mapview);
 		initMapView();
 		initMyLocation();
		 //set up click listeners
        View SendToFriendButton = findViewById(R.id.send_to_friend_button);
        SendToFriendButton.setOnClickListener(this);
        View MadeItButton = findViewById(R.id.made_it_button);
        MadeItButton.setOnClickListener(this);
 	}
 
 	/** Find and initialize the map view. */
 	private void initMapView() {
 		map = (MapView) findViewById(R.id.map);
 		controller = map.getController();
 		map.setSatellite(true);
 		map.setBuiltInZoomControls(true);
 	}
 
 	/** Start tracking the position on the map. */
 	private void initMyLocation() {
 		final MyLocationOverlay overlay = new MyLocationOverlay(this, map);
 		overlay.enableMyLocation();
 		overlay.enableCompass(); // does not work in emulator
 		overlay.runOnFirstFix(new Runnable() {
 			@Override
 			public void run() {
 				// Zoom in to current location
 				controller.setZoom(16);
 				controller.animateTo(overlay.getMyLocation());
 			}
 		});
 		map.getOverlays().add(overlay);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// Required by MapActivity
 		return false;
 	}
 	
 	@Override
 	public void onClick(View v){
 		switch(v.getId()){
 		case R.id.send_to_friend_button:
 			startActivity(new Intent(this, TestPage.class));
 			break;
 		case R.id.made_it_button:
 			startActivity(new Intent(this, TestPage.class));
 			break;
 		}
 	}
 }
