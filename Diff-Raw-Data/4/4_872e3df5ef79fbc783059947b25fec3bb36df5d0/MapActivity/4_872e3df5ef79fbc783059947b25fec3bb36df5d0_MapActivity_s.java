 package com.baggers.bagboy;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class MapActivity extends Fragment {
 	
 	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
     static final LatLng KIEL = new LatLng(53.551, 9.993);
     private GoogleMap map;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {        
 		// Inflate the layout for this fragment
 		View v = inflater.inflate(R.layout.activity_map, container, false);
 		
 		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
 //		Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG).title("Hamburg"));
 //	    Marker kiel = map.addMarker(new MarkerOptions()
 //	    	.position(KIEL)
 //	    	.title("Kiel")
 //	    	.snippet("Kiel is cool")
 //	    	.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
 
 	    // Move the camera instantly to hamburg with a zoom of 15.
 //	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
//
//	    // Zoom in, animating the camera.
 //	    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
 	    
 	    return v;
 	}
 }
