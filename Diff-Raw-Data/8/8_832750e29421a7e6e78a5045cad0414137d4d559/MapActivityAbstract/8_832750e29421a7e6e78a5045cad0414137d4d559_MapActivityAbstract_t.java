 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.SocialHitchhikingApplication;
 import no.ntnu.idi.socialhitchhiking.map.overlays.CustomSpeechBubbleOverlay;
 import no.ntnu.idi.socialhitchhiking.map.overlays.MapGestureDetectorOverlay;
 import no.ntnu.idi.socialhitchhiking.map.overlays.RoutePathOverlay;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 /**
  * This is the activity where driver selects where to drive from an to.
  * It displays the route on the map, and lets the user change the start-point, end-point or points in between.
  * <br><br>
  * In order to display Google Maps data, you must register with the Google Maps service 
  * and obtain a Google Maps API Key. For information about how to get a Google Maps API Key, see 
  * <a href=http://code.google.com/intl/no-NO/android/add-ons/google-apis/mapkey.html>Obtaining a Maps API Key</a>.
  *<br><br>
  * For the auto complete to work, you also will have to obtain a Google Places API Key,  
  * <a href=http://code.google.com/intl/no-NO/apis/maps/documentation/places/#Authentication> here.</a>
  *
  *@version 1.0
  *@since 1.0
  */
 public abstract class MapActivityAbstract extends MapActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
 
 	/**
 	 * The View that displays the map.
 	 */
 	protected MapView mapView;
 
 	/** 
 	 * Handles geocoding (transforming a street address or other description of a 
 	 * location into a (latitude, longitude) coordinate) and reverse geocoding 
 	 * (transforming a (latitude, longitude) coordinate into a (partial) address).
 	 */
 	protected Geocoder fancyGeocoder;
 
 	/**
 	 * An overlay used for detecting different types of gestures, such as 
 	 * long presses, single clicks and double clicks.
 	 */
 	protected MapGestureDetectorOverlay gestureDetectorOverlay; 
 
 	/**
 	 * An overlay for drawing a route on the map.
 	 */
 	protected RoutePathOverlay routePathOverlay;
 
 	/**
 	 * The zoom level on the startup of the activity
 	 */
 	private final int INITIAL_ZOOM_LEVEL = 12;
 
 	/**
 	 * The place to be in the center of the map when the activity starts
 	 */
 	private final String INITIAL_CENTER_OF_MAP = "Trondheim, Norge";
 
 	/**
 	 * A tag for sending log messages inside this class.
 	 */
 	protected final String TAG = "MapActivity";
 
 	/**
 	 * The progress bar that is visible when drawing a route.
 	 */
 	protected ProgressBar progressBar;
 
 	/**
 	 * The adapter that contains the auto complete results.
 	 */
 	protected ArrayAdapter<String> adapter;
 
 	/**
 	 * The {@link MapRoute} that is being shown in the {@link MapView}.
 	 */
 	protected MapRoute selectedRoute;
 	
 	/**
 	 * The application that owns this activity.
 	 */
 	private SocialHitchhikingApplication app;
 	
 	/**
 	 * A boolean value that should be true only when an already created route is being edited.
 	 */
 	protected boolean inEditMode = false;
 	
 	/**
 	 * A boolean value that should be true only when in the mode where the user 
 	 * chooses where to be picked up and where to be dropped off (on a {@link no.ntnu.idi.freerider.model.Journey}).
 	 */
 	protected boolean inPickupMode = false;
 	
 	/**
 	 * An integer used when in edit mode, to remember what position the route had in the list of created routes.
 	 */
 	protected int positionOfRoute;
 	
 	protected Route drawableRoute;
 	
 	/**
 	 * A {@link Handler} that receives {@link Message}s from the 
 	 * {@link #drawPathOnMap(MapRoute)}-method.
 	 */
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			if(msg.what == 1337){
 				onDrawingPathEnded();
 			}else if(msg.what == 1338){
 				if(msg.obj instanceof MapRoute){
 					MapRoute route = (MapRoute)msg.obj;
 					String routeDescription = route.getRouteDescription();
 					routePathOverlay = new RoutePathOverlay(route);
 					List<Overlay> listOfOverlays = mapView.getOverlays();
 					try{
 						listOfOverlays.add(1, routePathOverlay);
 					}catch (IndexOutOfBoundsException e) {
 						listOfOverlays.add(routePathOverlay);
 					}
 					mapView.invalidate();
 
 					if(routeDescription != null && routeDescription.length() > 1){
 						if(!inPickupMode) makeToast(routeDescription); 
 					}
 					onDrawingPathEnded();
 				}
 			}else if(msg.what > 10000 && msg.what<11000){
 				if(msg.obj instanceof String){
 					onDrawingPathEnded();
 					String s = (String)msg.obj;
 					makeToast(s);
 				}
 			}
 		}
 	};
 
 	/**
 	 * Here we initialize the content view, the map view, the progress bar, the geocoder (in GeoHelper) and the route cache (in PersistHelper).
 	 * Some variables are initialized: {@link #app} and {@link #gestureDetectorOverlay}.
 	 * <br><br>
 	 * If getIntent().getExtras() is not {@link null}, the route in getApp().getSelectedMapRoute() should be drawn. 
 	 */
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		initContentView();
 		initMapView();
 		initProgressBar();
 		app = (SocialHitchhikingApplication) getApplication();
 		GeoHelper.initGeocoder(this);
 		PersistHelper.initRouteCache(this);		
 		
 		mapView.setBuiltInZoomControls(true);
 		selectedRoute = new MapRoute(getUser(), "no name", -1, new ArrayList<MapLocation>());
 		
 		//Moving the center of the map to this point:
 		mapView.getController().setCenter(GeoHelper.getGeoPoint(INITIAL_CENTER_OF_MAP));
 
 		//Setting the zoom-level:
 		mapView.getController().setZoom(INITIAL_ZOOM_LEVEL); 
 
 		//Adding a map overlay that detects map gestures (e.g. long press):
 		gestureDetectorOverlay = new MapGestureDetectorOverlay(this, this);
 		List<Overlay> listOfOverlays = mapView.getOverlays();
 		listOfOverlays.clear();
 		listOfOverlays.add(gestureDetectorOverlay); 
 
 		mapView.invalidate();
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			int latE6 = extras.getInt("latitudeE6");
 			int lonE6 = extras.getInt("longitudeE6");
 			int zoomLevel = extras.getInt("zoomLevel");
 			if(latE6 > 0 && lonE6 > 0){
 				mapView.getController().setCenter(new GeoPoint(latE6, lonE6));
 			}
 			if(zoomLevel > 0){
 				mapView.getController().setZoom(zoomLevel);
 			}
 			boolean clearMap = extras.getBoolean("clear");
 			if(clearMap){
 				//nada
 			}else{
 				MapRoute mr = getApp().getSelectedMapRoute();
 				if(mr != null){
 					setSelectedRoute(mr);
 					drawPathOnMap(mr.getMapPoints());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Should set the activity content from a layout resource, using {@link #setContentView(int)}.
 	 */
 	protected abstract void initContentView();
 	
 	
 	/**
 	 * Should set the {@link MapView}.<br>
 	 * Example: mapView = (MapView)findViewById(R.id.map_view);
 	 */
 	protected abstract void initMapView();
 	
 	/**
 	 * Should set the {@link ProgressBar}, using {@link #setProgressBar(ProgressBar)}.
 	 * Example: setProgressBar((ProgressBar)findViewById(R.id.progressBar));
 	 */
 	protected abstract void initProgressBar();
 	
 	/**
 	 * Set the progress bar.
 	 * @param pb The progress bar to be shown when drawing the route.
 	 */
 	protected void setProgressBar(ProgressBar pb){
 		progressBar = pb;
 	}
 	
 	/**
 	 * Return the application that owns this activity. 
 	 */
 	protected SocialHitchhikingApplication getApp(){
 		return app;
 	}
 	
 	/**
 	 * Return the {@link User} that is logged in.
 	 */
 	protected User getUser(){
 		return app.getUser();
 	}
 
 	/**
 	 * Return the {@link MapRoute} that is shown on the map.
 	 */
 	public MapRoute getSelectedRoute(){
 		return selectedRoute;
 	}
 	
 	/**
 	 * Set the {@link MapRoute} that is (to be) shown on the map.
 	 */
 	public void setSelectedRoute(MapRoute selectedRoute){
 		this.selectedRoute = selectedRoute;
 	}
 
 	/**
 	 * This method is called by when a button in the {@link MapView} is pressed. 
 	 * It retrieves the text from two text fields and makes a road between them 
 	 * by calling the {@link #drawPathOnMap(String, String)}-method.
 	 * 
 	 * @param v
 	 */
 	public void findAndDrawPath(View v){
 		Overlay gestureOverlay = mapView.getOverlays().get(0);
 		mapView.getOverlays().clear();
 		mapView.getOverlays().add(gestureOverlay);
 		EditText etF = (EditText)findViewById(R.id.etGoingFrom);
 		EditText etT = (EditText)findViewById(R.id.etGoingTo);
 		
 		//adda av magnus
 		//EditText etA = (EditText)findViewById(R.id.etAddDest);
 
 		//Remove the keyboard:
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(etF.getWindowToken(), 0);
 		imm.hideSoftInputFromWindow(etT.getWindowToken(), 0);
 
 		//Draw the path:
 		drawPathOnMap(etF.getText().toString(), etT.getText().toString());
 	}
 	
 	/**
 	 * Drawing a path on the map between the two given {@link Strings})
 	 */
 	protected void drawPathOnMap(String from, String to){
 		drawPathOnMap(GeoHelper.getLocationList(new String[]{from, to}));
		
 	}
 
 	/**
 	 * Drawing a path on the map between all the {@link MapLocation}s in the given {@link List}.
 	 */
 	protected void drawPathOnMap(final List<MapLocation> locationList){
 		onDrawingPathStarted();
 		new Thread(new Runnable(){
 			@Override
 			public void run() { 
 				MapRoute route = PersistHelper.routeCacheGetRoute(locationList);
 				if(route != null && route.getMapPoints() != null && route.getMapPoints().size() != 0 && route.getRouteData() != null && route.getRouteData().size() != 0){
 					drawPathOnMap(route);
 				}
 				else{
 					MapRoute mapRoute;
 					try {
 						if(locationList.size() < 2){
 							onDrawingPathEnded();
 							return;
 						}
 						mapRoute = new MapRoute(selectedRoute, locationList,true);
 					} catch (Exception e){
 						String message = "Could not load the route from Google Maps...";
 						Toast toast = Toast.makeText(MapActivityAbstract.this, message, Toast.LENGTH_LONG);
 						toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 						toast.show();
 						return;
 					}
 					boolean success = drawPathOnMap(mapRoute);
 					if(!success) return; 
 					PersistHelper.saveRouteToCache(mapRoute);
 					//mapRoute = new MapRoute(selectedRoute, locationList,false);
 				}
 			}
 		}).start();
 		
 		for (int i = 0; i < locationList.size(); i++) {
 			if(i == 0){ 
 				drawMarkerAt(locationList.get(i), "start");
 			}
 			else if(i == locationList.size()-1) {
 				drawMarkerAt(locationList.get(i), "stop");
 			}
 			else {
 				drawMarkerAt(locationList.get(i), "through");
 			}
 		}
 		if(locationList.size() > 1) {
 			zoomAndCenterMap(GeoHelper.getGeoPoint(locationList.get(0)), GeoHelper.getGeoPoint(locationList.get(locationList.size()-1)));
 		}
 		else if(locationList.size() == 1){
 			mapView.getController().animateTo(GeoHelper.getGeoPoint(locationList.get(0)));
 		}
 	}
 	
 	/**
 	 * Draws the given route on the map.
 	 */
 	public boolean drawPathOnMap(MapRoute routeToDraw){
 		if(routeToDraw.getRouteData() == null || routeToDraw.getRouteData().size() == 0){
 			makeToast("Error. Try again.");
 			return false;
 		}else{
 			setSelectedRoute(routeToDraw);
 			Message drawRoutePathMessage = new Message();
 			drawRoutePathMessage.obj 	= routeToDraw;
 			drawRoutePathMessage.what 	= 1338;
 			mHandler.sendMessage(drawRoutePathMessage);
 		}
 		return true;
 	}
 	
 	/**
 	 * Zooms and centers the map so that both given {@link GeoPoint}s 
 	 * are visible on the map.
 	 */
 	protected void zoomAndCenterMap(GeoPoint gpFrom, GeoPoint gpTo){
 		//Fix zoom and centre-point:
 		int maxLat,minLat,maxLon,minLon;
 		if(gpTo.getLatitudeE6() > gpFrom.getLatitudeE6()){
 			maxLat = gpTo.getLatitudeE6();
 			minLat = gpFrom.getLatitudeE6();
 		}else{
 			maxLat = gpFrom.getLatitudeE6();
 			minLat = gpTo.getLatitudeE6();
 		}
 
 		if(gpTo.getLongitudeE6() > gpFrom.getLongitudeE6()){
 			maxLon = gpTo.getLongitudeE6();
 			minLon = gpFrom.getLongitudeE6();
 		}else{
 			maxLon = gpFrom.getLongitudeE6();
 			minLon = gpTo.getLongitudeE6();
 		}
 		int fancyInt = 25;
 		mapView.getController().zoomToSpan(maxLat-minLat+fancyInt, maxLon-minLon+fancyInt);
 		mapView.getController().animateTo(new GeoPoint(
 				(maxLat+minLat)/2, 
 				(maxLon+minLon)/2));
 	}
 
 
 	/**
 	 * Should be called when drawing a path on the map starts.
 	 */
 	private void onDrawingPathStarted(){
 		if(progressBar != null){
 			progressBar.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	/**
 	 * Should be called when drawing a path stops.
 	 */
 	private void onDrawingPathEnded(){
 		if(progressBar != null){
 			progressBar.setVisibility(View.GONE);
 		}
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return false;
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
 	 */
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		return false;
 	} 
 
 
 	/**
 	 * Adds the given point ({@link MapLocation}) to the route. If there is more than two points, the user is
 	 * sent to the choose order screen.
 	 * @param mapLocation The point to add.
 	 */
 	protected void addPoint(final MapLocation mapLocation) {
 		 AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
 	        alertbox.setMessage(mapLocation.getAddress());
 	        alertbox.setPositiveButton("Add this address", new DialogInterface.OnClickListener() {
 	            @Override
 				public void onClick(DialogInterface arg0, int arg1) {
 	            	if(selectedRoute == null || selectedRoute.getMapPoints().size() == 0){
 	            		List<MapLocation> drivingThrough = new ArrayList<MapLocation>();
 	            		drivingThrough.add(mapLocation);
 	            		MapRoute mapRoute;
 
 	            		try {
 							mapRoute = new MapRoute(selectedRoute, drivingThrough,true);
 						} catch (Exception e){ 
 							makeToast("Could not load the route from Google Maps...");
 							return;
 						}
 	            		
 	            		MapActivityAbstract.this.setSelectedRoute(mapRoute);
 	            		MapActivityAbstract.this.drawMarkerAt(mapRoute.getStartLocation(), "start");
 	            	}else if(selectedRoute.getMapPoints().size() == 1){
 	            		List<MapLocation> drivingThrough = selectedRoute.getMapPoints();
 	            		drivingThrough.add(mapLocation);
 	            		drawPathOnMap(drivingThrough);
 	            	}else{ 
 	            		selectedRoute.getMapPoints().add(mapLocation);
 
 	            		Intent dragAndDropIntent = new Intent(MapActivityAbstract.this, no.ntnu.idi.socialhitchhiking.map.draganddrop.DragAndDropListActivity.class);
 	            		getApp().setSelectedMapRoute(selectedRoute);
 	            		dragAndDropIntent.putExtra("type", "addPoint");
 	            		dragAndDropIntent.putExtra("editMode", inEditMode);
 	            		dragAndDropIntent.putExtra("routePosition", positionOfRoute);
 	            		startActivity(dragAndDropIntent);
 	            		finish();
 	            	}
 	            }
 	        }); 
 	        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	            @Override
 				public void onClick(DialogInterface arg0, int arg1) {
 	                
 	            }
 	        });
 	        alertbox.show();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
 	 */
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
 	 */
 	@Override
 	public void onShowPress(MotionEvent e) {
 	} 
 
 	/**
 	 * On a double tap on the map, is zooms in one level and animates to the points pressed.
 	 */
 	@Override
 	public boolean onDoubleTap(MotionEvent e) {
 		mapView.getController().zoomIn();
 		GeoPoint gp = mapView.getProjection().fromPixels(
 				(int) e.getX(),
 				(int) e.getY());
 		mapView.getController().animateTo(gp);
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnDoubleTapListener#onDoubleTapEvent(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onDoubleTapEvent(MotionEvent e) {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onSingleTapConfirmed(MotionEvent e) {
 		return false;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	/**
 	 * Creates a {@link CustomSpeechBubbleOverlay} with on the {@link #mapView}, with a {@link Drawable} 
 	 * at the given {@link GeoPoint}. It also and makes speech bubble, with the given title and snippet,
 	 * that pops up when the {@linkplain Drawable} is clicked.
 	 *  
 	 * @param drawable The {@link Drawable} to display on the {@link #mapView}.
 	 * @param gp The {@link GeoPoint} where the {@link Drawable} should be drawn.
 	 * @param title The title of the speech bubble.
 	 * @param snippet The snippet (main text) of the text bubble.
 	 */
 	protected Overlay drawSomethingWithTextBubble(Drawable drawable, GeoPoint gp, String title, String snippet){
 		CustomSpeechBubbleOverlay  itemizedOverlay = new CustomSpeechBubbleOverlay(drawable, mapView);
 		OverlayItem overlayItem = new OverlayItem(gp, title, snippet);
 		itemizedOverlay.addOverlay(overlayItem);
 		mapView.getOverlays().add(itemizedOverlay);
 		return itemizedOverlay;
 	}
 
 	/**
 	 * Draws a map marker on the {@link #mapView} at the given {@link GeoPoint}.
 	 * 
 	 * @param gp The {@link GeoPoint} that tells the marker should be drawn.
 	 */
 	protected void drawMarkerAt(final MapLocation mapLocation, final String type){
 		new Thread(new Runnable(){
 			@Override
 			public void run() {
 				String title = "";
 				Drawable drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.google_marker_thumb_mini);
 				if(type.equals("start")){
 					title = "Driving from";
 					drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.google_marker_thumb_mini_start);
 				}else if(type.equals("stop")){
 					title = "Driving to";
 					drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.google_marker_thumb_mini_stop);
 				}else if(type.equals("through")){
 					title = "Driving through";
 					drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.google_marker_thumb_mini_through);
 				}
 
 				String address = mapLocation.getAddressWithLines();
 				String lines[] = address.split("\n");
 				String snippet = "";
 				for (int i = 0; i < lines.length; i++) {
 					if(lines[i].length()>1){
 						snippet += lines[i].trim()+"\n";
 					}
 				}
 				drawSomethingWithTextBubble(drawable, GeoHelper.getGeoPoint(mapLocation), title.trim(), snippet.trim());
 			}
 		}).start();
 	}
 
 	/**
 	 * Draws a red or green cross at the given {@link Location}.
 	 */
 	protected synchronized Overlay drawCross(final Location location, final boolean green){
 		Drawable drawable;
 		if(green) 	drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.cross_pickup); 
 		else 		drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.cross_dropoff);
 
 		String address = GeoHelper.getAddressAtPointString(GeoHelper.getGeoPoint(location));
 		address = address.replace(",","\n");
 		String lines[] = address.split("\n");
 		String title = "Dropoff point";
 		if(green) title = "Pickup point";
 		String snippet = "";
 		for (int i = 0; i < lines.length; i++) {
 			snippet += lines[i].trim() +"\n";
 		}
 		return drawSomethingWithTextBubble(drawable, GeoHelper.getGeoPoint(location), title.trim(), snippet.trim());
 	}
 	
 	/**
 	 * Draws a red or green thumb at the given {@link Location}.
 	 */
 	protected synchronized Overlay drawThumb(final Location location, final boolean green){
 		Drawable drawable;
 		if(green) 	drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.thumb_green); 
 		else 		drawable = MapActivityAbstract.this.getResources().getDrawable(R.drawable.thumb_red);
 
 		String address = GeoHelper.getAddressAtPointString(GeoHelper.getGeoPoint(location));
 		address = address.replace(",","\n");
 		String lines[] = address.split("\n");
 		String title = "Going to";
 		if(green) title = "Going from";
 		String snippet = "";
 		for (int i = 0; i < lines.length; i++) {
 			snippet += lines[i].trim() +"\n";
 		}
 		return drawSomethingWithTextBubble(drawable, GeoHelper.getGeoPoint(location), title.trim(), snippet.trim());
 	}
 
 	/**
 	 * Draws a marker on the map.
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 */
 	protected void drawMarkerAt(double latitude, double longitude){
 		drawMarkerAt(new MapLocation(latitude, longitude), "");
 	}
 
 	/**
 	 * Makes a toast on the screen.
 	 * @param msg The message to be shown in the toast.
 	 */
 	public void makeToast(final String msg){
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast toast = Toast.makeText(MapActivityAbstract.this, msg, Toast.LENGTH_LONG);
 				toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 				toast.show();
 			}
 		});
 	}
 	
 
 }
