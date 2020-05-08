 package com.openmap.grupp1.maphandler;
 
 
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.openmap.grupp1.R;
 import com.openmap.grupp1.PopupandDialogHandler;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 /**
  *Creates the GoogleMap.
  *Listens to different clicks and movement of the map
  */
 
 
 public class MyMap
 implements  OnMapLongClickListener,OnMarkerClickListener,OnCameraChangeListener{
 
 	private GoogleMap myMap;
 	private Context mCtx;
 	private final String PREFS_NAME ="MySharedPrefs";
 	private MarkerHandler markerhandler;
 
 	public MyMap(Context context) {
 		//Map creator
 
 		this.mCtx = context;
 
 		//Connects the mapfragment to myMap
 		myMap = ((MapFragment) ((Activity) mCtx).getFragmentManager().findFragmentById(R.id.map)).getMap();
 
 		//enables all click
 		myMap.setMyLocationEnabled(true);
 		myMap.setOnMapLongClickListener(this);
		myMap.setOnMarkerClickListener(this); 
 		myMap.setOnCameraChangeListener(this);
 
		// Creates locationhandler that keep track and handles your position		
 		LocationHandler lh = new LocationHandler(myMap, mCtx);
 		//Makes the camera move to your location
 		lh.updateToMyLocation();
 		//Creates The CameraChangeHandler
 		//occ = new CameraChangeHandler(myMap,context,lh.getMylocation());
 
 		markerhandler = new MarkerHandler();
 	}
 
 
 
 
 	/**
 	 * Changes maptype if the user has changed type in settings
 	 * @param The map to be changed
 	 */
 	public void setMap(String map){
 		if (map.equals("Hybrid"))
 			myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		if (map.equals("Satellite"))
 			myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 		if (map.equals("Normal"))
 			myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		if(map.equals("Terrain"))	 
 			myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 	}	
 
 	/**
 	 * Gets the current map
 	 * @return The map
 	 */
 	public GoogleMap getMap() {
 		return myMap;
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.google.android.gms.maps.GoogleMap.OnMapLongClickListener#onMapLongClick(com.google.android.gms.maps.model.LatLng)
 	 * 
 	 * Saves the coordinates in SharedPreferences.
 	 * Zoom in the camera at the location the user have pressed and creates
 	 * a marker to make it easier for them to see if they have pressed the right point.
 	 * Creates a popup so the user can decide if it was the right position
 	 */
 	@Override
 	public void onMapLongClick(LatLng point) {	
 		//Saves the latitude and longitude in the shared preferences to use in AddTagActivity
 		SharedPreferences settings = mCtx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("markerLat", String.valueOf(point.latitude));
 		editor.putString("markerLng", String.valueOf(point.longitude));
 		editor.commit();
 
 		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(point,myMap.getMaxZoomLevel()-4); 
 		myMap.animateCamera(update);
 		Marker marker = myMap.addMarker(new MarkerOptions().position(point).title("This location?"));
 		marker.showInfoWindow();
 		// create interactive dialog window
 		PopupandDialogHandler insertinfo = new PopupandDialogHandler(mCtx);
 		insertinfo.confirmLocationPopup(marker, myMap); 
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.google.android.gms.maps.GoogleMap.OnMarkerClickListener#onMarkerClick(com.google.android.gms.maps.model.Marker)
 	 * Creates a infowindow so the user can see all the info of the marker they have pressed
 	 */
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 		MarkerHandler infowindow = new MarkerHandler();
 		infowindow.showInfo(mCtx, marker.getPosition(), mCtx.getResources(), myMap);
 		return true; 
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see com.google.android.gms.maps.GoogleMap.OnCameraChangeListener#onCameraChange(com.google.android.gms.maps.model.CameraPosition)
 	 * When the user moves the camera it gets the projection from the new view and creates 
 	 * a LatLng Boundary with these coordinates. If the user have zoomed in more than level 6 it 
 	 * calls on markerhandler.addmarkerstoscreen to add the markers in the
 	 * area the user is currently watching.
 	 */
 	@Override
 	public void onCameraChange(CameraPosition arg0) {
 		Log.d("Hej", "onCameraChange1");
 		Projection p = myMap.getProjection();
 		LatLng nearLeft = p.getVisibleRegion().nearLeft;
 		LatLng farRight = p.getVisibleRegion().farRight;
 		if(arg0.zoom > 6 /*&& (!database.contains(farRight) &&
 				!database.contains(nearLeft)) || 
 				markerhandler.IfFull()*/){
 
 			LatLngBounds database = new LatLngBounds(
 					new LatLng(nearLeft.latitude,nearLeft.longitude),
 					new LatLng(farRight.latitude,farRight.longitude));		
 			markerhandler.addMarkersToScreen(myMap,mCtx.getResources(),database,mCtx);}
 
 
 	}
 
 
 
 }
