 package org.omships.omships;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 
 /**
  * Shows the current ship on the map of the world.
  * @author jimtahu
  * Based on the basic map demo
  */
 public class MapActivity extends FragmentActivity {
 	private GoogleMap daMap;
 	
 	private void setUpMap() {
         daMap.addMarker(new MarkerOptions()
        .position(new LatLng(0, 0))
         .title(Settings.getShip().getName()));
     }
 	
 	private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (daMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             daMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (daMap != null) {
                 setUpMap();
             }
         }
     }
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maplayout);
 		setUpMapIfNeeded();
 	}
 
 	protected void onResume(){
 		super.onResume();
 		setUpMapIfNeeded();
 	}
 }//end MapActivity
