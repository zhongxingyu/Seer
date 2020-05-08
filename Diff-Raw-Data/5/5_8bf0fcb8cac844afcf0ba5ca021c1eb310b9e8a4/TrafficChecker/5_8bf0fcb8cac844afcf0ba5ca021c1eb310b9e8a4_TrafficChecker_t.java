 /*
  *  This File is licensed under GPL v3.
  *  Copyright (C) 2012 Rene Peinthor.
  *
  *  This file is part of TrafficChecker.
  *
  *  BlueMouse is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TrafficChecker is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TrafficChecker.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oldsch00l.TrafficChecker;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ViewSwitcher;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class TrafficChecker extends MapActivity {
 	private ListView reportListView;
 	private ViewSwitcher viewSwitcherMain;
 	private TrafficParser trafficParser;
 
 	// This handler is called after the traffic parsing is finished
 	private RefreshHandler mRefreshHandler = new RefreshHandler();
 	private Menu mOptionsMenu;
 	private ProgressDialog refreshDialog;
 	private static boolean mProgressDialogShowing = false;
 
 	static final int DIALOG_FIRST_START_ID = 0;
 
 	final static String EN_NORTHEAST = "NorthEast.xml";
 	final static String EN_NORTHWEST = "NorthWest.xml";
 	final static String EN_SOUTHEAST = "SouthEast.xml";
 	final static String EN_SOUTHWEST = "SouthWest.xml";
 	final static String EN_EAST = "East.xml";
 	final static String EN_WESTMIDLANDS = "WMidlands.xml";
 	final static String EN_EASTMIDLANDS = "EMidlands.xml";
 	final static String EN_M25 = "M25.xml";
 	final static String EN_M1 = "M1.xml";
 	final static String EN_M2 = "M2.xml";
 	final static String EN_M3 = "M3.xml";
 	final static String EN_M4 = "M4.xml";
 	final static String EN_M5 = "M5.xml";
 	final static String EN_M6 = "M6.xml";
 	final static String EN_M62 = "M62.xml";
 	final static String EN_A1M = "A1(M).xml";
 	final static String EN_M40 = "M40.xml";
 	final static String EN_M42 = "M42.xml";
 	final static String EN_M621 = "M621.xml";
 	final static String EN_M60 = "M60.xml";
 
 	public static HashMap<String,TrafficRegion> RegionMap;
 	static {
 		RegionMap = new HashMap<String, TrafficRegion>();
 		int i = 0;
 		//Austria
 		RegionMap.put("austria", new TrafficRegion( Country.Austria, "austria", new GeoPoint(47500000, 13880000), i++, 7));
 		RegionMap.put("burgenland", new TrafficRegion( Country.Austria, "burgenland", new GeoPoint(47567000, 16468000), i++, 9));
 		RegionMap.put("kaernten", new TrafficRegion( Country.Austria, "kaernten", new GeoPoint(46730000, 13897000), i++, 8));
 		RegionMap.put("niederoesterreich", new TrafficRegion( Country.Austria, "niederoesterreich", new GeoPoint(48222000, 15863000), i++, 8));
 		RegionMap.put("oberoesterreich", new TrafficRegion( Country.Austria, "oberoesterreich", new GeoPoint(48100000, 14000000), i++, 8));
 		RegionMap.put("salzburg", new TrafficRegion( Country.Austria, "salzburg", new GeoPoint(47400000, 13090000), i++, 9));
 		RegionMap.put("steiermark", new TrafficRegion( Country.Austria, "steiermark", new GeoPoint(47320000, 15080000), i++, 8));
 		RegionMap.put("tirol", new TrafficRegion( Country.Austria, "tirol", new GeoPoint(47236000, 11360000), i++, 8));
 		RegionMap.put("vorarlberg", new TrafficRegion( Country.Austria, "vorarlberg", new GeoPoint(47220000, 9900000), i++, 10));
 		RegionMap.put("wien", new TrafficRegion( Country.Austria, "wien", new GeoPoint(48200000, 16367500), i++, 12));
 		RegionMap.put("a1", new TrafficRegion( Country.Austria, "a1", new GeoPoint(48070000, 14650000), i++, 8));
 		RegionMap.put("a2", new TrafficRegion( Country.Austria, "a2", new GeoPoint(47400000, 15232000), i++, 8));
 		RegionMap.put("a4", new TrafficRegion( Country.Austria, "a4", new GeoPoint(48005000, 16770000), i++, 10));
 		RegionMap.put("a6", new TrafficRegion( Country.Austria, "a6", new GeoPoint(48042000, 16970000), i++, 11));
 		RegionMap.put("a7", new TrafficRegion( Country.Austria, "a7", new GeoPoint(48280000, 14374000), i++, 12));
 		RegionMap.put("a8", new TrafficRegion( Country.Austria, "a8", new GeoPoint(48180000, 13740000), i++, 10));
 		RegionMap.put("a9", new TrafficRegion( Country.Austria, "a9", new GeoPoint(47380000, 14890000), i++, 9));
 		RegionMap.put("a10", new TrafficRegion( Country.Austria, "a10", new GeoPoint(47120000, 13400000), i++, 9));
 		RegionMap.put("a12", new TrafficRegion( Country.Austria, "a12", new GeoPoint(47240000, 11330000), i++, 9));
 		RegionMap.put("a13", new TrafficRegion( Country.Austria, "a13", new GeoPoint(47130000, 11440000), i++, 11));
 		RegionMap.put("a14", new TrafficRegion( Country.Austria, "a14", new GeoPoint(47320000,  9700000), i++, 10));
 		RegionMap.put("a21", new TrafficRegion( Country.Austria, "a21", new GeoPoint(48100000, 16120000), i++, 11));
 		RegionMap.put("a22", new TrafficRegion( Country.Austria, "a22", new GeoPoint(48258000, 16387000), i++, 14));
 		RegionMap.put("a23", new TrafficRegion( Country.Austria, "a23", new GeoPoint(48180000, 16404000), i++, 12));
 
 		//Germany
 		RegionMap.put("baden_wuerttemberg", new TrafficRegion( Country.Germany, "baden_wuerttemberg", new GeoPoint(48680000, 9157000), i++, 8, GermanyRegions.createBadenWuerttembergCoordinates()));
 		RegionMap.put("bayern", new TrafficRegion( Country.Germany, "bayern", new GeoPoint(49139000, 11480000), i++, 7, GermanyRegions.createBayernCoordinates()));
 		RegionMap.put("berlin", new TrafficRegion( Country.Germany, "berlin", new GeoPoint(52530000, 13425000), i++, 11, GermanyRegions.createBerlinCoordinates()));
 		RegionMap.put("brandenburg", new TrafficRegion( Country.Germany, "brandenburg", new GeoPoint(52530000, 13000000), i++, 8, GermanyRegions.createBrandenburgCoordinates()));
 		RegionMap.put("bremen", new TrafficRegion( Country.Germany, "bremen", new GeoPoint(53277000, 8698000), i++, 10, GermanyRegions.createBremenCoordinates()));
 		RegionMap.put("hamburg", new TrafficRegion( Country.Germany, "hamburg", new GeoPoint(53558000, 10000000), i++, 11, GermanyRegions.createHamburgCoordinates()));
 		RegionMap.put("hessen", new TrafficRegion( Country.Germany, "hessen", new GeoPoint(50506000, 8910000), i++, 8, GermanyRegions.createHessenCoordinates()));
 		RegionMap.put("mecklenburg_vorpommern", new TrafficRegion( Country.Germany, "mecklenburg_vorpommern", new GeoPoint(54008000, 12508000), i++, 8, GermanyRegions.createMecklenburgVorpommernCoordinates()));
 		RegionMap.put("niedersachsen", new TrafficRegion( Country.Germany, "niedersachsen", new GeoPoint(52843000, 9320000), i++, 8, GermanyRegions.createNiedersachsenCoordinates()));
 		RegionMap.put("nordrhein_westfalen", new TrafficRegion( Country.Germany, "nordrhein_westfalen", new GeoPoint(51488000, 7440000), i++, 8, GermanyRegions.createNordrheinWestfalenCoordinates()));
 		RegionMap.put("rheinland_pfalz", new TrafficRegion( Country.Germany, "rheinland_pfalz", new GeoPoint(49902000, 7420000), i++, 8, GermanyRegions.createRheinlandPfalzCoordinates()));
 		RegionMap.put("saarland", new TrafficRegion( Country.Germany, "saarland", new GeoPoint(49404000, 6968000), i++, 9, GermanyRegions.createSaarlandCoordinates()));
 		RegionMap.put("sachsen", new TrafficRegion( Country.Germany, "sachsen", new GeoPoint(51076000, 13440000), i++, 10, GermanyRegions.createSachsenCoordinates()));
 		RegionMap.put("sachsen_anhalt", new TrafficRegion( Country.Germany, "sachsen_anhalt", new GeoPoint(52032000, 11610000), i++, 8, GermanyRegions.createSachsenAnhaltCoordinates()));
 		RegionMap.put("schleswig_holstein", new TrafficRegion( Country.Germany, "schleswig_holstein", new GeoPoint(54207000, 9810000), i++, 8, GermanyRegions.createSchleswigHolsteinCoordinates()));
 		RegionMap.put("thueringen", new TrafficRegion( Country.Germany, "thueringen", new GeoPoint(50868000, 10980000), i++, 8, GermanyRegions.createThueringenCoordinates()));
 
 		//England
 		RegionMap.put(EN_NORTHEAST, new TrafficRegion( Country.England, EN_NORTHEAST, new GeoPoint(54530000, -1500000), i++, 8));
 		RegionMap.put(EN_NORTHWEST, new TrafficRegion( Country.England, EN_NORTHWEST, new GeoPoint(54000000, -2700000), i++, 8));
 		RegionMap.put(EN_SOUTHEAST, new TrafficRegion( Country.England, EN_SOUTHEAST, new GeoPoint(51000000, 0), i++, 8));
 		RegionMap.put(EN_SOUTHWEST, new TrafficRegion( Country.England, EN_SOUTHWEST, new GeoPoint(51200000, -2850000), i++, 7));
 		RegionMap.put(EN_EAST, new TrafficRegion( Country.England, EN_EAST, new GeoPoint(52520000, -2000000), i++, 7));
 		RegionMap.put(EN_WESTMIDLANDS, new TrafficRegion( Country.England, EN_WESTMIDLANDS, new GeoPoint(52550000, -2220000), i++, 8));
 		RegionMap.put(EN_EASTMIDLANDS, new TrafficRegion( Country.England, EN_EASTMIDLANDS, new GeoPoint(52650000, -1090000), i++, 8));
 		RegionMap.put(EN_M25, new TrafficRegion( Country.England, EN_M25, new GeoPoint(51500000, -150000), i++, 9));
 		RegionMap.put(EN_M1, new TrafficRegion( Country.England, EN_M1, new GeoPoint(52650000, -1170000), i++, 8));
 		RegionMap.put(EN_M2, new TrafficRegion( Country.England, EN_M2, new GeoPoint(51320000, 690000), i++, 10));
 		RegionMap.put(EN_M3, new TrafficRegion( Country.England, EN_M3, new GeoPoint(51200000, -950000), i++, 10));
 		RegionMap.put(EN_M4, new TrafficRegion( Country.England, EN_M4, new GeoPoint(51480000, -1544000), i++, 8));
 		RegionMap.put(EN_M5, new TrafficRegion( Country.England, EN_M5, new GeoPoint(51600000, -2460000), i++, 8));
 		RegionMap.put(EN_M6, new TrafficRegion( Country.England, EN_M6, new GeoPoint(53450000, -2460000), i++, 7));
 		RegionMap.put(EN_M62, new TrafficRegion( Country.England, EN_M62, new GeoPoint(53700000, -1730000), i++, 9));
 		RegionMap.put(EN_A1M, new TrafficRegion( Country.England, EN_A1M, new GeoPoint(54300000, -1500000), i++, 8));
 		RegionMap.put(EN_M40, new TrafficRegion( Country.England, EN_M40, new GeoPoint(52000000, -1300000), i++, 9));
 		RegionMap.put(EN_M42, new TrafficRegion( Country.England, EN_M42, new GeoPoint(52500000, -1800000), i++, 10));
 		RegionMap.put(EN_M621, new TrafficRegion( Country.England, EN_M621, new GeoPoint(53760000, -1576000), i++, 12));
 		RegionMap.put(EN_M60, new TrafficRegion( Country.England, EN_M60, new GeoPoint(53480000, -2240000), i++, 11));
 	}
 
 	public static java.util.List<TrafficRegion> getCountryList(Country count) {
 		ArrayList<TrafficRegion> retList = new ArrayList<TrafficRegion>();
 
 		for( TrafficRegion region : RegionMap.values() ) {
 			if( region.getCountry() == count )
 				retList.add(region);
 		}
 		Collections.sort(retList);
 		return retList;
 	}
 
 	public static int getCountrySelectionCount(Country country) {
 		int count = 0;
 		for( TrafficRegion region : getCountryList(country)) {
 			if( region.isSelected() )
 				count++;
 		}
 		return count;
 	}
 
 	public enum Country {
 		Austria ("http://www.oeamtc.at/feeds/verkehr", R.drawable.flag_at, R.drawable.flag_at_gray),
 		Germany ("http://www.freiefahrt.info/upload/lmst.de_DE.xml", R.drawable.flag_de, R.drawable.flag_de_gray),
 		//Germany ("http://rp.oldsch00l.com/tmp/lmst.de_DE.xml", R.drawable.flag_de, R.drawable.flag_de_gray),
 		England ("http://www.highways.gov.uk/rssfeed", R.drawable.flag_en, R.drawable.flag_en_gray);
 
 		private final String mUrl;
 		private final int mFlag;
 		private final int mFlagInactive;
 		Country (String url, int flag, int flagInactive) {
 			mUrl = url;
 			mFlag = flag;
 			mFlagInactive = flagInactive;
 		}
 
 		String getUrl() { return mUrl; }
 		public int getFlag() { return mFlag; }
 		public int getFlagInactive() { return mFlagInactive; }
 	}
 
 	//MapActivity stuff
 	private MapController mapController;
 	private MapView mMapView;
 	private TrafficMapItemizedOverlay mTrafficOverlay = null;
 	private TrafficMapItemizedOverlay mRoadWorksOverlay = null;
 	private TrafficMapItemizedOverlay mRoadConditionOverlay = null;
 //	private ArrayList<TrafficOverlay> mLineOverlays;
 	private MyLocationOverlay mMyLocationOverlay;
 	private Drawable iconTraffic;
 	private Drawable iconRoadWorks;
 	private Drawable iconRoadCondition;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.main);
 
 		//refresh dialog
 		refreshDialog = new ProgressDialog(this);
 		refreshDialog.setCancelable(false);
 		refreshDialog.setTitle(R.string.refresh);
 		refreshDialog.setMessage(getString(R.string.refreshDialogText));
 
 		// set members
 		reportListView = (ListView) View.inflate(this, R.layout.listview, null);
 		viewSwitcherMain = (ViewSwitcher) findViewById(R.id.ViewSwitcherMain);
 		mMapView = (MapView)View.inflate(this, R.layout.mapview, null);
 
 		viewSwitcherMain.addView(reportListView);
 		viewSwitcherMain.addView(mMapView);
 
 		reportListView.setOnItemClickListener(new ListItemClickListener());
 
 		//map activity
 		mMapView.setBuiltInZoomControls(true);
 
 		mapController = mMapView.getController();
 
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 		mMapView.getOverlays().add(mMyLocationOverlay);
 		mMyLocationOverlay.enableCompass();
 //		mMyLocationOverlay.enableMyLocation();
 //		mLineOverlays = new ArrayList<TrafficOverlay>();
 
 		//initialize map icons
 		iconTraffic = getResources().getDrawable(R.drawable.icon_traffic);
 		iconRoadWorks = getResources().getDrawable(R.drawable.icon_roadworks);
 		iconRoadCondition = getResources().getDrawable(R.drawable.icon_roadcondition);
 
 
 		String strRegions = TrafficProvider.getSetting( getContentResolver(), TrafficProvider.SET_REGION);
 		if (strRegions.length() == 0) {
 			showDialog(DIALOG_FIRST_START_ID);
 		} else {
 			setSelectedRegions(strRegions);
 
 			updateTrafficNews(getSelectedRegions());
 		}
 
 		if( TrafficProvider.getSetting(getContentResolver(), TrafficProvider.SET_VIEW).equals("map") )
 			switchView(true);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		case DIALOG_FIRST_START_ID:
 			builder.setMessage(
 					String.format(getString(R.string.first_start),
 							getString(R.string.app_name)))
 					.setCancelable(false)
 					.setPositiveButton("Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
									Intent selectRegion = new Intent(
 											getBaseContext(),
 											SelectRegionActivity.class);
									startActivityForResult(selectRegion, 0);
 								}
 							});
 			dialog = builder.create();
 			break;
 		}
 		return dialog;
 	}
 
 	@Override
 	protected void onPause() {
 		mMyLocationOverlay.disableCompass();
 		mMyLocationOverlay.disableMyLocation();
 		super.onPause();
 		dismissProgressDialog();
 	}
 
 	@Override
 	protected void onResume() {
 		mMyLocationOverlay.enableCompass();
 		mMyLocationOverlay.enableMyLocation();
 		super.onResume();
 		if(mProgressDialogShowing)
 			showProgressDialog();
 	}
 
 	/**
 	 * This method should be called to update the traffic news.
 	 *
 	 * It creates a new parser thread and starts it, after the parser
 	 * is finished it will call the mRefreshHandler and update the gui.
 	 *
 	 * @param sRegions Regions to get traffic news in a comma seperated list.
 	 */
 	public void updateTrafficNews(String sRegions) {
 		trafficParser = new TrafficParser( getApplicationContext(), mRefreshHandler);
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean bTraffic = sp.getBoolean("traffic", true);
 		boolean bRoadWorks = sp.getBoolean("roadworks", true);
 		String sOrder = sp.getString("orderby", "location");
 		trafficParser.setFilter(bTraffic, bRoadWorks);
 		trafficParser.setOrderBy(sOrder);
 		trafficParser.setRegionList(sRegions);
 		trafficParser.start();
 		showProgressDialog();
 	}
 
 	static public String getRegionString(Context context, String urlString) {
 		if( urlString.equals("austria") ) return context.getString(R.string.austria);
 		else if( urlString.equals("burgenland") ) return context.getString(R.string.burgenland);
 		else if( urlString.equals("kaernten") ) return context.getString(R.string.carinthia);
 		else if( urlString.equals("niederoesterreich") ) return context.getString(R.string.loweraustria);
 		else if( urlString.equals("oberoesterreich") ) return context.getString(R.string.upperaustria);
 		else if( urlString.equals("salzburg") ) return context.getString(R.string.salzburg);
 		else if( urlString.equals("steiermark") ) return context.getString(R.string.styria);
 		else if( urlString.equals("tirol") ) return context.getString(R.string.tyrol);
 		else if( urlString.equals("vorarlberg") ) return context.getString(R.string.vorarlberg);
 		else if( urlString.equals("wien") ) return context.getString(R.string.vienna);
 		else if( urlString.equals("a1") ) return context.getString(R.string.a1);
 		else if( urlString.equals("a2") ) return context.getString(R.string.a2);
 		else if( urlString.equals("a4") ) return context.getString(R.string.a4);
 		else if( urlString.equals("a6") ) return context.getString(R.string.a6);
 		else if( urlString.equals("a7") ) return context.getString(R.string.a7);
 		else if( urlString.equals("a8") ) return context.getString(R.string.a8);
 		else if( urlString.equals("a9") ) return context.getString(R.string.a9);
 		else if( urlString.equals("a10") ) return context.getString(R.string.a10);
 		else if( urlString.equals("a12") ) return context.getString(R.string.a12);
 		else if( urlString.equals("a13") ) return context.getString(R.string.a13);
 		else if( urlString.equals("a14") ) return context.getString(R.string.a14);
 		else if( urlString.equals("a21") ) return context.getString(R.string.a21);
 		else if( urlString.equals("a22") ) return context.getString(R.string.a22);
 		else if( urlString.equals("a23") ) return context.getString(R.string.a23);
 		//Germany
 		else if( urlString.equals("baden_wuerttemberg") ) return context.getString(R.string.baden_wuerttemberg);
 		else if( urlString.equals("bayern") ) return context.getString(R.string.bayern);
 		else if( urlString.equals("berlin") ) return context.getString(R.string.berlin);
 		else if( urlString.equals("brandenburg") ) return context.getString(R.string.brandenburg);
 		else if( urlString.equals("bremen") ) return context.getString(R.string.bremen);
 		else if( urlString.equals("hamburg") ) return context.getString(R.string.hamburg);
 		else if( urlString.equals("hessen") ) return context.getString(R.string.hessen);
 		else if( urlString.equals("mecklenburg_vorpommern") ) return context.getString(R.string.mecklenburg_vorpommern);
 		else if( urlString.equals("niedersachsen") ) return context.getString(R.string.niedersachsen);
 		else if( urlString.equals("nordrhein_westfalen") ) return context.getString(R.string.nordrhein_westfalen);
 		else if( urlString.equals("rheinland_pfalz") ) return context.getString(R.string.rheinland_pfalz);
 		else if( urlString.equals("saarland") ) return context.getString(R.string.saarland);
 		else if( urlString.equals("sachsen") ) return context.getString(R.string.sachsen);
 		else if( urlString.equals("sachsen_anhalt") ) return context.getString(R.string.sachsen_anhalt);
 		else if( urlString.equals("schleswig_holstein") ) return context.getString(R.string.schleswig_holstein);
 		else if( urlString.equals("thueringen") ) return context.getString(R.string.thueringen);
 
 		//England
 		else if( urlString.equals(EN_NORTHEAST) ) return context.getString(R.string.northeastengland);
 		else if( urlString.equals(EN_NORTHWEST) ) return context.getString(R.string.northwestengland);
 		else if( urlString.equals(EN_SOUTHEAST) ) return context.getString(R.string.southeastengland);
 		else if( urlString.equals(EN_SOUTHWEST) ) return context.getString(R.string.southwestengland);
 		else if( urlString.equals(EN_EAST) ) return context.getString(R.string.eastofengland);
 		else if( urlString.equals(EN_WESTMIDLANDS) ) return context.getString(R.string.westmidlandsengland);
 		else if( urlString.equals(EN_EASTMIDLANDS) ) return context.getString(R.string.eastmidlandsengland);
 		else if( urlString.equals(EN_M25) ) return context.getString(R.string.en_m25);
 		else if( urlString.equals(EN_M1) ) return context.getString(R.string.en_m1);
 		else if( urlString.equals(EN_M2) ) return context.getString(R.string.en_m2);
 		else if( urlString.equals(EN_M3) ) return context.getString(R.string.en_m3);
 		else if( urlString.equals(EN_M4) ) return context.getString(R.string.en_m4);
 		else if( urlString.equals(EN_M5) ) return context.getString(R.string.en_m5);
 		else if( urlString.equals(EN_M6) ) return context.getString(R.string.en_m6);
 		else if( urlString.equals(EN_M62) ) return context.getString(R.string.en_m62);
 		else if( urlString.equals(EN_A1M) ) return context.getString(R.string.en_a1m);
 		else if( urlString.equals(EN_M40) ) return context.getString(R.string.en_m40);
 		else if( urlString.equals(EN_M42) ) return context.getString(R.string.en_m42);
 		else if( urlString.equals(EN_M621) ) return context.getString(R.string.en_m621);
 		else if( urlString.equals(EN_M60) ) return context.getString(R.string.en_m60);
 		return urlString;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		mOptionsMenu = menu;
 
 		//restore settings
 		MenuItem viewSwitchItem = menu.findItem( R.id.ViewSwitchMenu);
 		if( TrafficProvider.getSetting(getContentResolver(), TrafficProvider.SET_VIEW).equals("map") )
 			flipMenuItem(viewSwitchItem, true, R.string.text, R.drawable.menu_text);
 		else
 			flipMenuItem(viewSwitchItem, false, R.string.map, R.drawable.menu_map);
 
 		return true;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	                                ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if( RegionMap.keySet().contains(item.getItemId()) ) {
 			RegionMap.get(item.getItemId()).setSelected(!RegionMap.get(item.getItemId()).isSelected());
 			TrafficProvider.setSetting(getContentResolver(), TrafficProvider.SET_REGION, getSelectedRegions());
 			updateTrafficNews(getSelectedRegions());
 			return true;
 		}
 		else
 			return super.onContextItemSelected(item);
 	}
 
 	public void openSelectRegionActivity(View view) {
 		Intent myIntent = new Intent(this, SelectRegionActivity.class);
 		this.startActivityForResult(myIntent, 0);
 	}
 
 	public void openContextMenuFromButton(View view) {
 		registerForContextMenu(view);
 		openContextMenu(view);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch( item.getItemId() )
 		{
 		case R.id.reload:
 			trafficParser.clearCache();
 			updateTrafficNews(getSelectedRegions());
 			return true;
 		case R.id.ViewSwitchMenu:
 			switchView();
 			return true;
 		case R.id.menu_settings:
 			Intent settingsActivity = new Intent(getBaseContext(),
                     Preferences.class);
 			startActivityForResult(settingsActivity, 1);
 			return true;
 		case R.id.About:
 			AboutDialog.create(this).show();
 			return true;
 		case R.id.Exit:
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			if( TrafficProvider.getSetting(getContentResolver(), TrafficProvider.SET_VIEW).equals("map"))
 			{
 				switchView();
 				return true;
 			}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void refreshMapAndReportList() {
 		List<Message> filteredList = trafficParser.getResultList();
 
 		if( filteredList == null)
 			filteredList = new ArrayList<Message>();
 
 		updateMapIconOverlays(filteredList);
 //		updateMapLineOverlays(filteredList);
 		mMapView.invalidate();
 
 		if( filteredList.size() == 0)
 		{
 			Message emptyMsg = new Message();
 			emptyMsg.setTitle( getString(R.string.NoRegionSelected) );
 			filteredList.add(emptyMsg);
 		}
 
 		// set adapter
 		reportListView.setAdapter(new TrafficAdapter(getApplicationContext(), filteredList));
 
 		//zoom to region
 		TrafficRegion tr = getBiggestSelected();
 		if( tr != null && tr.getGeoPoint() != null)
 		{
 			mapController.setZoom(tr.getZoom());
 			mapController.animateTo(tr.getGeoPoint());
 		}
 	}
 
 	private TrafficRegion getBiggestSelected() {
 		TrafficRegion retRegion = null;
 		for( TrafficRegion tr : RegionMap.values() ) {
 			if( retRegion == null )
 			{
 				if( tr.isSelected() )
 					retRegion = tr;
 			}
 			else
 			{
 				if( tr.isSelected() && retRegion.getZoom() > tr.getZoom() )
 					retRegion = tr;
 			}
 		}
 
 		return retRegion == null ? RegionMap.get("austria") : retRegion;
 	}
 
 	private String getSelectedRegions() {
 		StringBuffer sbRet = new StringBuffer();
 		ArrayList<TrafficRegion> orderedRegion = new ArrayList<TrafficRegion>(RegionMap.values());
 		Collections.sort(orderedRegion);
 		for (TrafficRegion region : orderedRegion) {
 			if( region.isSelected() )
 			{
 				sbRet.append(region.getRegionUrlAppend() );
 				sbRet.append( ',' );
 			}
 		}
 		return sbRet.toString();
 	}
 
 	private void setSelectedRegions(String str) {
 		for (String strregion : str.split(",")) {
 			for( TrafficRegion region : RegionMap.values()) {
 				if( region.getRegionUrlAppend().equals(strregion))
 					region.setSelected(true);
 			}
 		}
 	}
 
 	//map activity
 	private void updateMapIconOverlays(List<Message> messageList) {
 		mMapView.getOverlays().remove( mTrafficOverlay);
 		mMapView.getOverlays().remove( mRoadWorksOverlay);
 		mMapView.getOverlays().remove( mRoadConditionOverlay);
 
 		mTrafficOverlay = new TrafficMapItemizedOverlay(iconTraffic, getApplicationContext());
 		mRoadWorksOverlay = new TrafficMapItemizedOverlay(iconRoadWorks, getApplicationContext());
 		mRoadConditionOverlay = new TrafficMapItemizedOverlay(iconRoadCondition, getApplicationContext());
 
 		for (Message message : messageList) {
 			if( message.getGeoDataList() != null
 					&& message.getGeoDataList().size() > 0
 					&& message.getType() == Message.Type.TRAFFIC)
 			{
 				OverlayItem trOverlayItem = new OverlayItem(
 					message.getGeoDataList().get(0), //as we don't support lines, just show the first point
 					message.getTitle(),
 					message.getDescription()
 				);
 
 				switch ( message.getSubtype() )
 				{
 					case ROADWORKS:
 						mRoadWorksOverlay.addOverlay(trOverlayItem);
 						break;
 					case ROADCONDITION:
 						mRoadConditionOverlay.addOverlay(trOverlayItem);
 						break;
 					default:
 						mTrafficOverlay.addOverlay(trOverlayItem);
 				}
 			}
 		}
 
 		if( mTrafficOverlay.size() > 0)
 		{
 			mTrafficOverlay.populateData();
 			mMapView.getOverlays().add(mTrafficOverlay);
 		}
 		if( mRoadConditionOverlay.size() > 0)
 		{
 			mRoadConditionOverlay.populateData();
 			mMapView.getOverlays().add(mRoadConditionOverlay);
 		}
 		if( mRoadWorksOverlay.size() > 0)
 		{
 			mRoadWorksOverlay.populateData();
 			mMapView.getOverlays().add(mRoadWorksOverlay);
 		}
 	}
 
 //	private void updateMapLineOverlays(List<Message> messageList) {
 //
 //		for (TrafficOverlay troverlay : mLineOverlays) {
 //			mMapView.getOverlays().remove(troverlay);
 //		}
 //
 //		mLineOverlays.clear();
 //		for (Message message : messageList) {
 //			if( message.getGeoDataList() != null
 //					&& message.getGeoDataList().size() > 1
 //					&& message.getType() == Message.Type.TRAFFIC)
 //			{
 //				TrafficOverlay trol = new TrafficOverlay(message.getGeoDataList() );
 //				mLineOverlays.add( trol );
 //				mMapView.getOverlays().add(trol);
 //			}
 //		}
 //	}
 
 	@Override
 	public boolean isRouteDisplayed() {
 		return false;
 	}
 
 	private class ListItemClickListener implements AdapterView.OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			if( trafficParser != null && trafficParser.getResultList() != null)
 			{
 				Message msg = trafficParser.getResultList().get(position);
 
 				if( msg.getGeoDataList() != null && msg.getGeoDataList().size() > 0)
 				{
 					switchView();
 					mapController.animateTo( msg.getGeoDataList().get(0));
 					if(mMapView.getZoomLevel() < 12)
 						mapController.setZoom( 14);
 				}
 			}
 		}
 	}
 
 	private void switchView() {
 		switchView(false);
 	}
 
 	private void switchView(boolean restore) {
 		MenuItem viewSwitchItem = null;
 
 		if( !restore )
 		{
 			if( mOptionsMenu != null )
 				viewSwitchItem = mOptionsMenu.findItem( R.id.ViewSwitchMenu);
 			if( TrafficProvider.getSetting(getContentResolver(), TrafficProvider.SET_VIEW).equals("text"))
 			{
 				flipMenuItem(viewSwitchItem, true, R.string.text, R.drawable.menu_text);
 				TrafficProvider.setSetting(getContentResolver(), TrafficProvider.SET_VIEW, "map");
 			}
 			else
 			{
 				flipMenuItem(viewSwitchItem, false, R.string.map, R.drawable.menu_map);
 				TrafficProvider.setSetting(getContentResolver(), TrafficProvider.SET_VIEW, "text");
 			}
 		}
 
 		viewSwitcherMain.showNext();
 	}
 
 	private void flipMenuItem( MenuItem item, boolean checked, int text, int icon) {
 		if( item != null)
 		{
 			item.setChecked(checked);
 			item.setTitle(text);
 			if( icon > 0 )
 				item.setIcon(icon);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		TrafficProvider.setSetting(getContentResolver(), TrafficProvider.SET_REGION, getSelectedRegions());
 		updateTrafficNews(getSelectedRegions());
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private void showProgressDialog() {
 		mProgressDialogShowing = true;
 		refreshDialog.show();
 	}
 
 	private void dismissProgressDialog() {
 		mProgressDialogShowing = false;
 		refreshDialog.dismiss();
 	}
 
 	class RefreshHandler extends Handler {
 		@Override
 		public void  handleMessage  (android.os.Message msg) {
 			refreshMapAndReportList();
 			dismissProgressDialog();
 		}
 	}
 }
