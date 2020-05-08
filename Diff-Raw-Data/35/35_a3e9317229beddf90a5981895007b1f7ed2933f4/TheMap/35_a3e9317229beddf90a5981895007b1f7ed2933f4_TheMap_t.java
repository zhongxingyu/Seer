 /* ============================================================
  * Copyright  2012 Adam Book, Rickard Edfast, Erik Sjgren,
  * Alexander Kurkimki
  *
  * This file is part of Pubrundan.
  *
  * Pubrundan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Pubrundan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Pubrundan. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package com.tarea.pubrundan;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.gesture.GestureOverlayView;
 import android.gesture.GestureOverlayView.OnGestureListener;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.Projection;
 import com.tarea.pubrundan.Pubs.JAPripps;
 
 // TODO: Auto-generated Javadoc
 //### This class follows some code examples from http://eagle.phys.utk.edu/guidry/android/index.html  ###//
 
 /**
  * The class TheMap
  * 
  * @author Erik Sjgren
  * 
  */
 public class TheMap extends MapActivity implements OnGestureListener,
 		OnDoubleTapListener {
 
 	/** The start campus. */
 	private boolean startCampus = true; // false = Lindholmen, true =
 										// Johanneberg (default campus)
 	/** The zoom level. */
 	private int zoomLevel = 17;
 
 	/** The mc. */
 	private MapController mc;
 
 	/** The gp. */
 	private GeoPoint gp;
 
 	/** The map view. */
 	private MapView mapView;
 
 	/** The go to pub list button. */
 	private Button getThePositionButton, changeCampusButton, goToPubListButton; // Buttons
 																				// shown
 																				// on
 																				// top
 																				// of
 																				// theMap
 	/** The my location overlay. */
 	private MyLocationOverlay myLocationOverlay; // An Overlay showing a "point"
 	// on the map aka your
 	// current position
 
 	/** The map overlays. */
 	private List<Overlay> mapOverlays;
 
 	/** The drawable. */
 	private Drawable drawable;
 
 	/** The itemized overlay. */
 	private OverlayClass itemizedOverlay;
 
 	/** The different views. */
 	private final CharSequence[] differentViews = { "Street", "Satellite",
 			"Traffic" }; // selectable map views
 
 	// Define an array containing the access overlay items for all of the pubs
 	// of Johanneberg and Lindholmen
 	// Coordinates need to be converted into integers, by default they are
 	// displayed in microdegrees
 
 	// The pubs in the array are listed and hardcoded from
 	// coordinates_of_the_pubs.txt
 	/** The all pubs array. */
 	public OverlayItem[] allPubsArray = {
 			// J.A. Pripps
 			new OverlayItem(new GeoPoint((int) (57.688984 * 1E6),
 					(int) (11.974389 * 1E6)), "J.A. Pripps", "Johanneberg"),
 			// Gasquen
 			new OverlayItem(new GeoPoint((int) (57.688725 * 1E6),
 					(int) (11.975156 * 1E6)), "Gasquen", "Johanneberg"),
 			// Bulten
 			new OverlayItem(new GeoPoint((int) (57.689148 * 1E6),
 					(int) (11.978496 * 1E6)), "Bulten", "Johanneberg"),
 			// Winden
 			new OverlayItem(new GeoPoint((int) (57.688961 * 1E6),
 					(int) (11.978665 * 1E6)), "Winden", "Johanneberg"),
 			// Zaloonen
 			new OverlayItem(new GeoPoint((int) (57.689097 * 1E6),
 					(int) (11.979126 * 1E6)), "Zaloonen", "Johanneberg"),
 			// Club Avancez
 			new OverlayItem(new GeoPoint((int) (57.692219 * 1E6),
 					(int) (11.976862 * 1E6)), "Club Avancez", "Johanneberg"),
 			// GoldenI
 			new OverlayItem(new GeoPoint((int) (57.692939 * 1E6),
 					(int) (11.975242 * 1E6)), "GoldenI", "Johanneberg"),
 			// Hubben 2.1
 			new OverlayItem(new GeoPoint((int) (57.688331 * 1E6),
 					(int) (11.979217 * 1E6)), "Hubben 2.1", "Johanneberg"),
 			// Basen
 			new OverlayItem(new GeoPoint((int) (57.688776 * 1E6),
 					(int) (11.978852 * 1E6)), "Basen", "Johanneberg"),
 			// Kajsabaren
 			new OverlayItem(new GeoPoint((int) (57.688196 * 1E6),
 					(int) (11.978573 * 1E6)), "Kajsabaren", "Johanneberg"),
 			// Jrnvgspub
 			new OverlayItem(new GeoPoint((int) (57.688317 * 1E6),
 					(int) (11.975757 * 1E6)), "Jrnvgspub", "Johanneberg"),
 			// GasTown
 			new OverlayItem(new GeoPoint((int) (57.687935 * 1E6),
 					(int) (11.975918 * 1E6)), "GasTown", "Johanneberg"),
 			// FortNOx
 			new OverlayItem(new GeoPoint((int) (57.687302 * 1E6),
 					(int) (11.977237 * 1E6)), "FortNOx", "Johanneberg"),
 			// Spritkket
 			new OverlayItem(new GeoPoint((int) (57.689587 * 1E6),
 					(int) (11.977978 * 1E6)), "Spritkket", "Johanneberg"),
 			// Focus
 			new OverlayItem(new GeoPoint((int) (57.691001 * 1E6),
 					(int) (11.977591 * 1E6)), "Focus", "Johanneberg"),
 			// Rda rummet
 			new OverlayItem(new GeoPoint((int) (57.689977 * 1E6),
 					(int) (11.976862 * 1E6)), "Rda rummet", "Johanneberg"),
 			// Sigurd/A-fiket
 			new OverlayItem(new GeoPoint((int) (57.687563 * 1E6),
 					(int) (11.976717 * 1E6)), "Sigurd/A-fiket", "Johanneberg"),
 			// Pub P
 			new OverlayItem(new GeoPoint((int) (57.706463 * 1E6),
 					(int) (11.939596 * 1E6)), "Pub P", "Lindholmen"),
 			// 11:an
 			new OverlayItem(new GeoPoint((int) (57.706085 * 1E6),
 					(int) (11.936675 * 1E6)), "11:an", "Johanneberg"),
 			// Gangnam Style
 			new OverlayItem(new GeoPoint((int) (37.5175725 * 1E6),
 					(int) (127.047462 * 1E6)), "Gangnam Style", "Sydkorea"),
 
 	};
 
 	// standard onCreate method
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 	
 		requestWindowFeature(Window.FEATURE_NO_TITLE); // Suppress title bar for
 														// more space
 		setContentView(R.layout.showthemap); // Use xml-layout showtomap.xml
 		
 		// Add map controller with zoom controls
 		mapView = (MapView) findViewById(R.id.mv);
 		mapView.setSatellite(true); // Satellite is set by default
 		mapView.setBuiltInZoomControls(true); // Set android:clickable=true in
 												// main.xml
 
 		// create an overlay that shows our current location
 		myLocationOverlay = new MyLocationOverlay(this, mapView); // Creates a
 																	// new
 																	// MyLocationOverlay
 																	// for later
 																	// use in
 																	// the code
 		// checkIfGpsIsEnabled(); // check if gps is enabled
 
 		loading(); // loading animation, invokes: changeToCampusJohanneberg(),
 					// showThePubs()
 
 		// Button for get my location
 		getThePositionButton = (Button) findViewById(R.id.getPosition);
 		getThePositionButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showTheCurrentPosition();
 			}
 		});
 
 		// Button to control access overlay
 		changeCampusButton = (Button) findViewById(R.id.changeCampus);
 		changeCampusButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (startCampus) {
 					changeToCampusLindholmen();
 					startCampus = false;
 				} else if (!startCampus) {
 
 					changeToCampusJohanneberg();
 					startCampus = true;
 				}
 			}
 		});
 
 		// Button to goto PubList.java
 		goToPubListButton = (Button) findViewById(R.id.pubList);
 		goToPubListButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startPubListActivity();
 			}
 		});
 
 		String pubName = getIntent().getStringExtra("Pub");	
 		int pubNrInArray = getIntent().getIntExtra("Pub to animate to in array list", 1);  // 1 = defaultvalue
 		
 		if( pubName != null && pubNrInArray >= 0 ){
 			Toast.makeText(getBaseContext(), pubName, Toast.LENGTH_LONG).show();
 			GeoPoint gp = allPubsArray[pubNrInArray].getPoint();
 			animateToPubandSetZoom(gp);
         }
 		
 	}
 	
 	public void animateToPubandSetZoom(GeoPoint gp){
 		
 		mc = mapView.getController();
 		mc.animateTo(gp);
		mapView.invalidate();
		mapView.getController();
		mapView.postInvalidate();
 		mc.setZoom(19);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.view.GestureDetector.OnDoubleTapListener#onDoubleTap(android.
 	 * view.MotionEvent)
 	 */
 	public boolean onDoubleTap(MotionEvent e) {
 		int x = (int) e.getX(), y = (int) e.getY();
 		Projection p = mapView.getProjection();
 		mapView.getController().animateTo(p.fromPixels(x, y));
 		mapView.getController().zoomIn();
 		return true;
 	}
 
 	// display all pubs in the allPubsArray as an overlay onto the map
 	/**
 	 * Show the pubs.
 	 */
 	public void showThePubs() {
 
 		// Create itemizedOverlay2 if it doesn't exist
 		if (itemizedOverlay == null) {
 			mapOverlays = mapView.getOverlays();
 			drawable = this.getResources().getDrawable(
 					R.drawable.icon_pub_location);
 			itemizedOverlay = new OverlayClass(drawable, this);
 		}
 		for (int i = 0; i < allPubsArray.length; i++) {
 
 			itemizedOverlay.addOverlay(allPubsArray[i]);
 		}
 		// Added symbols will be displayed when map is redrawn so force redraw
 		// now
 		itemizedOverlay.populateFix();
 		mapOverlays.add(itemizedOverlay); // need to be outside the for-loop
 											// (source:
 											// http://stackoverflow.com/questions/2659770/android-map-performance-poor-because-of-many-overlays)
		mapView.postInvalidate();
 	}
 
 	/**
 	 * Loading.
 	 */
 	private void loading() {
 
 		final Object loadingDialog = ProgressDialog.show(this,
 				"Laddar pubbar...", "Vnta...", true);
 		new Thread() {
 			public void run() {
 				changeToCampusJohanneberg(); // change the position to
 												// Johanneberg
 				showThePubs(); // Displaying all pubs as overlay in maps,
 								// see method for
 				((Dialog) loadingDialog).dismiss();
 			}
 		}.start();
 	}
 
 	// The map will navigate to your current position
 	/**
 	 * Show the current position.
 	 */
 	public void showTheCurrentPosition() {
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		myLocationOverlay.enableMyLocation();
 		mapView.getOverlays().add(myLocationOverlay);
 
 		mc = mapView.getController();
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				GeoPoint gp = myLocationOverlay.getMyLocation();
 				mc.setZoom(zoomLevel);
 				mc.animateTo(gp);
 				mc.setCenter(gp);
 			}
 		});
 	}
 
 	// navigate to campus Lindholmen if users clicks changeCampusButton
 	/**
 	 * Change to campus lindholmen.
 	 */
 	public void changeToCampusLindholmen() {
 
 		mc = mapView.getController();
 		gp = new GeoPoint((int) (57.705947 * 1e6), (int) (11.936642 * 1e6));
 		mc.animateTo(gp);
 		mc.setCenter(gp);
 		mc.setZoom(zoomLevel);
 	}
 
 	// navigate to campus Lindholmen if users clicks changeCampusButton
 	/**
 	 * Change to campus johanneberg.
 	 */
 	public void changeToCampusJohanneberg() {
 
 		mc = mapView.getController();
 		gp = new GeoPoint((int) (57.691144 * 1e6), (int) (11.976078 * 1e6));
 		mc.animateTo(gp);
 		mc.setCenter(gp);
 		mc.setZoom(zoomLevel);
 	}
 	
 	public void animateToGeopoint(GeoPoint gp, int zoom){
 		
 		mc = mapView.getController();
 		//gp = new GeoPoint((int) (57.689034 * 1e6), (int) (11.976468 * 1e6));
 		mc.animateTo(gp);
 		mc.setCenter(gp);
 		mc.setZoom(zoom);
 	}
 
 	// starting new activity( PubList.java ) if user clicks goToPubListButton
 	/**
 	 * Start pub list activity.
 	 */
 	public void startPubListActivity() {
 
 		// default PubList.class in development test using PubInfo.class
 		Intent i = new Intent(this, PubList.class); // context = this ,
 													// PubList.class =
 													// ClassToNavigateTo.class
 		startActivity(i);
 	}
 
 	// starting new activity( PubInfo.java ) if user clicks goToPubListButton
 	/**
 	 * Start pub info activity.
 	 */
 	public void startPubInfoActivity() {
 
 		// default PubList.class in development test using PubInfo.class
 		Intent i = new Intent(this, JAPripps.class); // context = this ,
 														// PubInfo.class =
 														// ClassToNavigateTo.class
 		startActivity(i);
 	}
 
 	// Required method since class extends MapActivity
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false; // Don't display a route
 	}
 
 	// Initiating Menu XML file (menu.xml)
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater oMenu = getMenuInflater();
 		oMenu.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	// onOptionsMenu with three items, "Street", "Satellite" & Traffic... code
 	// refactor is needed!
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.change_map:
 			new AlertDialog.Builder(this).setTitle("ndra vy")
 					.setSingleChoiceItems(differentViews, -1, // -1 === fulhack
 																// :|
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int item) {
 									if (item == 0) {
 										mapView.setTraffic(false);
 										mapView.setSatellite(false);
 										mapView.setStreetView(true);
 									}
 
 									else if (item == 1) {
 										mapView.setStreetView(false);
 										mapView.setTraffic(false);
 										mapView.setSatellite(true);
 									}
 
 									else if (item == 2) {
 										mapView.setStreetView(false);
 										mapView.setSatellite(false);
 										mapView.setTraffic(true);
 									}
 									dialog.dismiss();
 									Toast.makeText(TheMap.this,
 											differentViews[item] + " r valt",
 											Toast.LENGTH_SHORT).show();
 								}
 							}).create().show();
 		case R.id.settings:
 			return true;
 
 		case R.id.share:
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * Attempts to enable MyLocation, registering for updates from
 	 * LocationManager.GPS_PROVIDER and LocationManager.NETWORK_PROVIDER.
 	 * 
 	 * @see com.google.android.maps.MapActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// when our activity resumes, we want to register for location updates
 		myLocationOverlay.enableMyLocation();
 	}
 
 	/*
 	 * Stops location updates.
 	 * 
 	 * @see com.google.android.maps.MapActivity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// when our activity pauses, we want to remove listening for location
 		myLocationOverlay.disableMyLocation();
 	}
 
 	/**
 	 * On tab changed.
 	 * 
 	 * @param tabId
 	 *            the tab id
 	 */
 	public void onTabChanged(String tabId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Called when the user press back button in the TheMap.
 	 */
 	@Override
 	public void onBackPressed() {
 		new AlertDialog.Builder(this).setTitle("Avsluta")
 				// Title: "Avsluta"
 				.setIcon(R.drawable.icon_warning)
 				.setMessage("Sker p att du vill avsluta?") // Message:
 																// "Sker p att du vill avsluta?"
 				.setNegativeButton("Nej", null) // Negative answer button: "Nej"
 												// -> return null aka close
 												// dialog
 				.setNeutralButton("Ja, avsluta!", // Neutral answer button:
 													// "Ja, avsluta" -> exit
 													// current activity
 						new android.content.DialogInterface.OnClickListener() {
 
 							public void onClick(DialogInterface arg0, int arg1) {
 								TheMap.super.onBackPressed();
 								System.exit(0); // if "Ja, avsluta" exit and
 												// remove location/network
 												// services for this app
 							}
 						}).create().show();
 	}
 
 	/**
 	 * Check if gps is enabled.
 	 */
 	public void checkIfGpsIsEnabled() {
 		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			NoGpsDialog();
 		}
 	}
 
 	/**
 	 * No gps dialog.
 	 */
 	public void NoGpsDialog() {
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("GPS avstngd")
 				.setMessage("Din GPS r avstngd, vill du starta den?")
 				.setIcon(R.drawable.icon_warning)
 				.setCancelable(false)
 				.setNeutralButton("Ja",
 						new android.content.DialogInterface.OnClickListener() {
 							public void onClick(final DialogInterface dialog,
 									final int id) {
 								startActivity(new Intent(
 										Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 							}
 						})
 				.setNegativeButton("Nej",
 						new DialogInterface.OnClickListener() {
 							public void onClick(final DialogInterface dialog,
 									final int id) {
 								dialog.cancel();
 							}
 						});
 		final AlertDialog ad = builder.create();
 		ad.show();
 	}
 
 	/**
 	 * On location changed.
 	 * 
 	 * @param location
 	 *            the location
 	 */
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * On provider disabled.
 	 * 
 	 * @param provider
 	 *            the provider
 	 */
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * On provider enabled.
 	 * 
 	 * @param provider
 	 *            the provider
 	 */
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * On status changed.
 	 * 
 	 * @param provider
 	 *            the provider
 	 * @param status
 	 *            the status
 	 * @param extras
 	 *            the extras
 	 */
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.view.GestureDetector.OnDoubleTapListener#onDoubleTapEvent(android
 	 * .view.MotionEvent)
 	 */
 	public boolean onDoubleTapEvent(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.view.GestureDetector.OnDoubleTapListener#onSingleTapConfirmed
 	 * (android.view.MotionEvent)
 	 */
 	public boolean onSingleTapConfirmed(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * On down.
 	 * 
 	 * @param e
 	 *            the e
 	 * @return true, if successful
 	 */
 	public boolean onDown(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * On fling.
 	 * 
 	 * @param e1
 	 *            the e1
 	 * @param e2
 	 *            the e2
 	 * @param velocityX
 	 *            the velocity x
 	 * @param velocityY
 	 *            the velocity y
 	 * @return true, if successful
 	 */
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * On long press.
 	 * 
 	 * @param e
 	 *            the e
 	 */
 	public void onLongPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * On scroll.
 	 * 
 	 * @param e1
 	 *            the e1
 	 * @param e2
 	 *            the e2
 	 * @param distanceX
 	 *            the distance x
 	 * @param distanceY
 	 *            the distance y
 	 * @return true, if successful
 	 */
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * On show press.
 	 * 
 	 * @param e
 	 *            the e
 	 */
 	public void onShowPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * On single tap up.
 	 * 
 	 * @param e
 	 *            the e
 	 * @return true, if successful
 	 */
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.gesture.GestureOverlayView.OnGestureListener#onGesture(android
 	 * .gesture.GestureOverlayView, android.view.MotionEvent)
 	 */
 	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.gesture.GestureOverlayView.OnGestureListener#onGestureCancelled
 	 * (android.gesture.GestureOverlayView, android.view.MotionEvent)
 	 */
 	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.gesture.GestureOverlayView.OnGestureListener#onGestureEnded(android
 	 * .gesture.GestureOverlayView, android.view.MotionEvent)
 	 */
 	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.gesture.GestureOverlayView.OnGestureListener#onGestureStarted
 	 * (android.gesture.GestureOverlayView, android.view.MotionEvent)
 	 */
 	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
