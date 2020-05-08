 /**
  * Android Campus Maps
  *  http://code.google.com/p/vuphone/
  * 
  * @author Kyle Liming
  * @date Oct 8, 2009
  * 
  * Copyright 2009 VUPhone Team
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License. 
  *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *  Unless required by applicable law or agreed to in writing, software 
  *  distributed under the License is distributed on an "AS IS" BASIS, 
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  *  implied. See the License for the specific language governing 
  *  permissions and limitations under the License. 
  */
 
 package edu.vanderbilt.vuphone.android.campusmaps;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 import edu.vanderbilt.vuphone.android.campusmaps.storage.Building;
 import edu.vanderbilt.vuphone.android.campusmaps.tools.Tools;
 
 public class Main extends MapActivity {
 
 	public static Context applicationContext;
 
 	private static final int MENU_ITEM_BUILDING_LIST = 1;
 	private static final int MENU_ITEM_MAP_MODE_GROUP = 0;
 	private static final int MENU_ITEM_ABOUT = 7;
 	// private static final int MENU_SETTINGS = 3;
 
 	private static final int SUBMENU_STREET_VIEW = 6;
 	private static final int SUBMENU_TRAFFIC = 5;
 	private static final int SUBMENU_SATELLITE = 4;
 
 	public static MapView mapView_;
 	private static MapController mc_;
 	private GeoPoint p_;
 	private PathOverlay poLayer_ = null;
 	private static Main instance_ = null;
 	public static Context context_ = null;
 	public static Resources resources_ = null;
 	public static GPS gps_ = null;
 
 	/**
 	 * Called when the activity is first created. Enables user to zoom in/out of
 	 * the center of the screen. Also sets the map to open while viewing
 	 * Vanderbilt Campus.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		instance_ = this;
 		context_ = getBaseContext();
 		resources_ = getResources();
 
 		// clunky mechanic with gross file dependency,
 		// but DBWrapper needs Main.mainContext for its
 		// calls to DBAdapter
 		if (applicationContext == null)
 			applicationContext = getApplicationContext();
 
 		setContentView(R.layout.main);
 		mapView_ = (MapView) findViewById(R.id.mapview);
 		mapView_.setBuiltInZoomControls(false);
 
 		poLayer_ = new PathOverlay(mapView_);
 
 		mc_ = mapView_.getController();
 
 		// Vanderbilt GPS coordinates, used to start the map at a Vanderbilt
 		// Location.
 		double lat = 36.142830;
 		double lng = -86.804437;
 
 		p_ = new GeoPoint((int) (lat * 1000000), (int) (lng * 1000000));
 		centerMapAt(p_, 17);
 
 		
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Set the GPS Listener
 		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		gps_ = GPS.getInstance();
 		gps_.initialize(lm);
 		gps_.showMarker();
 
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		
 		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		GPS.getInstance().uninitialize(lm);
 	}
 
 	/**
 	 * Singleton accessor
 	 * 
 	 * @return running instance
 	 */
 	public static Main getInstance() {
 		return instance_;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	/**
 	 * Creates a "Map Mode" checkable submenu option when menu is clicked.
 	 * Options: Map, Satellite, Traffic, Street View. Creates a "List Buildings"
 	 * option when menu is clicked. Creates a "Show Buildings" option when menu
 	 * is clicked. Creates a "Settings" option when menu is clicked.
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		SubMenu mapModes = menu.addSubMenu("More Map Modes").setIcon(
 				android.R.drawable.ic_menu_mapmode);
 		mapModes.add(MENU_ITEM_MAP_MODE_GROUP, 4, SUBMENU_SATELLITE,
 				"Satellite");
 		mapModes.add(MENU_ITEM_MAP_MODE_GROUP, 5, SUBMENU_TRAFFIC, "Traffic");
 		mapModes.add(MENU_ITEM_MAP_MODE_GROUP, 6, SUBMENU_STREET_VIEW,
 				"Street View");
 		mapModes.setGroupCheckable(MENU_ITEM_MAP_MODE_GROUP, true, false);
 		menu.add(0, 1, MENU_ITEM_BUILDING_LIST, "List Buildings").setIcon(
 				android.R.drawable.ic_menu_agenda);
 
 		menu.add(Menu.NONE, MENU_ITEM_ABOUT, Menu.NONE, "About").setIcon(
 				getResources().getDrawable(
 						android.R.drawable.ic_menu_info_details));
 		/*
 		 * menu.add(0, 2, MENU_SETTINGS, "Settings").setIcon(
 		 * android.R.drawable.ic_menu_preferences);
 		 */
 		return true;
 	}
 
 	/**
 	 * Called when an Menu item is clicked
 	 */
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		super.onMenuItemSelected(featureId, item);
 
 		switch (item.getItemId()) {
 
 		// If the mapView lines are uncommented, why doesn't they work?
 		case (SUBMENU_SATELLITE):
 			if (item.isChecked()) {
 				item.setChecked(false);
 			} else {
 				item.setChecked(true);
 			}
 			if (item.isChecked()) {
 				mapView_.setSatellite(true);
 			} else {
 				mapView_.setSatellite(false);
 			}
 			break;
 
 		case (SUBMENU_TRAFFIC):
 			if (item.isChecked()) {
 				item.setChecked(false);
 			} else {
 				item.setChecked(true);
 			}
 			if (item.isChecked()) {
 				mapView_.setTraffic(true);
 			} else {
 				mapView_.setTraffic(false);
 			}
 			break;
 
 		case (SUBMENU_STREET_VIEW):
 			if (item.isChecked()) {
 				item.setChecked(false);
 			} else {
 				item.setChecked(true);
 			}
 			if (item.isChecked()) {
 				mapView_.setStreetView(true);
 			} else {
 				mapView_.setStreetView(false);
 			}
 			break;
 
 		case (MENU_ITEM_BUILDING_LIST):
 			Intent i = new Intent(this, BuildingList.class);
 			startActivity(i);
 			break;
 		case MENU_ITEM_ABOUT:
 			Intent about = new Intent(this, About.class);
 			startActivity(about);
 			break;
 		/*
 		 * case (MENU_SETTINGS): echo("Settings"); break;
 		 */
 		}
 		return true;
 	}
 
 	/**
 	 * Used to set a marker image on the map
 	 * 
 	 * @param p
 	 *            - location to place marker
 	 */
 	public void drop_pin(GeoPoint p) {
 		MapMarker m = new MapMarker(p);
 		m.drop_pin();
 		centerMapAt(p);
 	}
 
 	/**
 	 * Used to set a marker image on the map
 	 * 
 	 * @param p
 	 * @param building
 	 *            - location to place marker
 	 */
 	public void drop_pin(Building b) {
 		MapMarker m = new MapMarker(b);
 		m.drop_pin();
 		centerMapAt(new GeoPoint(b.getLat_(), b.getLong_()), 18);
 	}
 
 	/**
 	 * Prints a message to the screen for a few seconds
 	 */
 	public static void echo(String s) {
 		Toast.makeText(context_, s, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Moves the map position to show a specified point at center screen
 	 * 
 	 * @param p
 	 *            - coordinates to center on
 	 */
 	public static void centerMapAt(GeoPoint p) {
 		mc_.animateTo(p);
 		mapView_.invalidate();
 	}
 
 	/**
 	 * Moves the map position to show a specified point at center screen
 	 * 
 	 * @param p
 	 *            - coordinates to center on
 	 * @param zoomLevel
 	 *            - level to set zoom
 	 */
 	public void centerMapAt(GeoPoint p, int zoomLevel) {
 		mc_.setZoom(zoomLevel);
 		centerMapAt(p);
 	}
 
 	/**
 	 * Prints a message to LogCat with tag='mad'
 	 * 
 	 * @param s
 	 *            String to print
 	 */
 	public static void trace(String s) {
 		if (s == null)
 			return;
 
 		Log.d("mad", s);
 	}
 
 	public void testPathOverlay() {
 
		
		/**
		 * This looks useful for our pathfinding. IT seems they might have already
		 * implemented it.
		 */
 		// Just some demo paths to test for now
 		poLayer_.StartNewPath(new GeoPoint(36144875, -86806723));
 		poLayer_.AddPoint(new GeoPoint(36146071, -86804298));
 		poLayer_.StartNewPath(new GeoPoint(36143411, -86806401));
 		poLayer_.AddPoint(new GeoPoint(36143238, -86804727));
 		poLayer_.AddPoint(new GeoPoint(36143143, -86803257));
 		poLayer_.AddPoint(new GeoPoint(36143429, -86802624));
 		poLayer_.AddPoint(new GeoPoint(36143935, -86802587));
 
 		// Attempt to draw Wesley Place from GML data in EPSG900913 format from
 		// vu.gml, just testing / demoing.
 		poLayer_.StartNewPath(Tools.EPSG900913ToGeoPoint(-9662429.695230,
 				4320719.417812));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662420.185221,
 				4320683.476196));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662417.200911,
 				4320672.193037));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662417.071184,
 				4320672.178321));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662395.440964,
 				4320669.572643));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662395.711297,
 				4320667.316003));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662386.352760,
 				4320666.189571));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662386.082410,
 				4320668.444238));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662346.924362,
 				4320663.727702));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662359.954998,
 				4320711.017158));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662381.825093,
 				4320713.650537));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662389.499083,
 				4320714.573825));
 		poLayer_.AddPoint(Tools.EPSG900913ToGeoPoint(-9662429.695230,
 				4320719.417812));
 
 	}
 }
