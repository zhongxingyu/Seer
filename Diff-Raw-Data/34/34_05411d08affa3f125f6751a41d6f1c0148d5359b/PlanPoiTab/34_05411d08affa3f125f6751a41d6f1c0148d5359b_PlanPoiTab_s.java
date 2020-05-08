 /**
  * @contributor(s): Kristian Greve Hagen (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:		15 Dec 2011, Rune
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
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
 
 /**
  * @description:
  * This class handles all the action going on in the locations tab.
  * 
  */
 
 package org.ubicompforall.CityExplorer.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.ubicompforall.CityExplorer.data.DBFactory;
 import org.ubicompforall.CityExplorer.data.DatabaseUpdater;
 import org.ubicompforall.CityExplorer.data.IntentPassable;
 import org.ubicompforall.CityExplorer.data.Poi;
 import org.ubicompforall.CityExplorer.data.PoiAdapter;
 import org.ubicompforall.CityExplorer.data.SeparatedListAdapter;
 import org.ubicompforall.CityExplorer.data.Sharing;
 import org.ubicompforall.CityExplorer.data.Trip;
 import org.ubicompforall.CityExplorer.map.MapsActivity;
 
 import org.ubicompforall.CityExplorer.CityExplorer;
 import org.ubicompforall.CityExplorer.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class PlanPoiTab extends PlanActivityTab implements LocationListener, OnMultiChoiceClickListener, DialogInterface.OnClickListener{
 
 	/** Field containing the String of the category settings, used in shared preferences. */
 	private static String CATEGORY_SETTINGS = "catset";
 
 	/*** Field containing the list of all favorite pois. */
 	private ArrayList<Poi> favouriteList = new ArrayList<Poi>();
 
 	/*** Field containing all pois.*/
 	private ArrayList<Poi> allPois = new ArrayList<Poi>();
 
 	/*** Field containing the adapter for favorite pois.*/
 	private PoiAdapter favouriteAdapter;
 
 	/*** Field containing this activity's resources.*/
 	private Resources res;
 
 	/*** Field containing the {@link SeparatedListAdapter} that holds all the other adapters.*/
 	private SeparatedListAdapter adapter;
 
 	/*** Field containing this activity's {@link ListView}.*/
 	private ListView lv;
 
 	/*** Field containing the users current location.*/
 	private Location userLocation;
 
 	/*** Field containing an {@link ArrayList} of the categories.*/
 	private ArrayList<String> categories;
 
 	/*** Field containing this activity's context.*/
 	private Context context;
 
 	/*** Field containing a {@link HashMap} for the checked categories in the filter.*/
 	private HashMap<String, Boolean> CheckedCategories = new HashMap<String, Boolean>();
 
 	/*** Field containing the request code from other activities.*/
 	private int requestCode;
 
 	/*** Field containing the request code for add to trip.*/
 	protected static final int ADD_TO_TRIP = 1;
 
 	/*** Field containing the request code for sharing a poi.*/
 	private static final int SHARE_POI = 5;
 
 	/*** Field containing the request code for downloading pois.*/
 	private static final int DOWNLOAD_POI = 6;
 
 	/*** Field containing a single poi.*/
 	private Poi poi;
 
 	/*** Field containing pois you want to share.*/
 	private ArrayList<Poi> sharePois;
 
 	/*** Field containing pois you want to download.*/
 	private ArrayList<Poi> downloadedPois;
 
 	/*** Field giving access to databaseUpdater methods.*/
 	private DatabaseUpdater du;
 
 	/*** Field containing pois you have selected for adding to trip.*/
 	private ArrayList<Poi> selectedPois;
 
 	/*** Field containing a single trip.*/
 	private Trip trip;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);		
 		debug(0, "PlanTabPoi~118 create");
 		init();
 	}
 
 	
 	private void debug( int level, String message ) {
 		CityExplorer.debug( level, message );		
 	} //debug
 
 
 
 	/**
 	 * Initializes the activity.
 	 */
 	private void init() {
 		context = this;
 		requestCode = getIntent().getIntExtra("requestCode",0);
 		lv = getListView();
 		if (requestCode == NewPoiActivity.CHOOSE_POI || 
 				requestCode == PlanTripTab.ADD_TO_TRIP || 
 				requestCode == TripListActivity.ADD_TO_TRIP ||
 				requestCode == SHARE_POI ||
 				requestCode == DOWNLOAD_POI)
 		{
 			lv.setOnItemLongClickListener(null);
 		}else {			
 			lv.setOnItemLongClickListener(new DrawPopup());
 		}
 		if(requestCode == DOWNLOAD_POI){			
 			du = new DatabaseUpdater(this);
 			allPois = du.getInternetPois();
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.INTERNET_POIS);
 		}else {			
 			allPois = DBFactory.getInstance(this).getAllPois();
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.POI_LIST);
 		}
 		if(requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){		
 			trip = (Trip) getIntent().getParcelableExtra(IntentPassable.TRIP);		
 		}
 		res = getResources();
 		lv.setAdapter(adapter);
 		categories = DBFactory.getInstance(this).getUniqueCategoryNames();
 		Collections.sort(categories);
 		CheckedCategories.put("Favourites", true);
 		buildFilter();
 
 		makeSections();
 
 		lv.setAdapter(adapter);
 
 		initGPS();
 	}//init()
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(requestCode != DOWNLOAD_POI){			
 			updateSections();
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * Makes the category sections that is shown in the list. 
 	 */
 	private void makeSections()
 	{
 		favouriteAdapter = new PoiAdapter(this, R.layout.plan_listitem, favouriteList);
 		if(requestCode != DOWNLOAD_POI){			
 			adapter.addSection("Favourites", favouriteAdapter);
 		}
 		for (Poi poi : allPois)
 		{
 			if(poi.isFavourite())//add to favourite section
 			{
 				favouriteList.add(poi);
 				favouriteAdapter.notifyDataSetChanged();
 			}
 			if( !adapter.getSectionNames().contains(poi.getCategory()))//category does not exist, create it.
 			{
 				ArrayList<Poi> list = new ArrayList<Poi>();
 
 				PoiAdapter testAdapter;
 				testAdapter = new PoiAdapter(this, R.layout.plan_listitem, list);
 				adapter.addSection(poi.getCategory(), testAdapter);
 			}
 			((PoiAdapter)adapter.getAdapter(poi.getCategory())).add(poi);//add to the correct section
 			((PoiAdapter)adapter.getAdapter(poi.getCategory())).notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * Updates the category sections in the list, e.g. after choosing filtering.
 	 */
 	@SuppressWarnings("unchecked")
 	private void updateSections()
 	{
 		allPois = DBFactory.getInstance(this).getAllPois();
 		ArrayList<String> sectionsInUse = new ArrayList<String>(); 
 		for (Poi poi : allPois)
 		{
 			//ignore sections that are turned off:
 			if(CheckedCategories.keySet().contains(poi.getCategory()))
 			{
 				if( !CheckedCategories.get(poi.getCategory()))//this section is turned off:
 				{
 					continue;
 				}
 			}
 			sectionsInUse.add(poi.getCategory());
 			if(!adapter.getSectionNames().contains(poi.getCategory()))
 			{
 				ArrayList<Poi> list = new ArrayList<Poi>();
 				list.add(poi);
 				PoiAdapter testAdapter = new PoiAdapter(this, R.layout.plan_listitem, list);
 				adapter.addSection(poi.getCategory(), testAdapter);
 			}//if contains category
 		}//for POIs
 		ArrayList<String> ListSections = (ArrayList<String>) adapter.getSectionNames().clone();
 		for(String sec : ListSections){
 			if( !sectionsInUse.contains(sec) && !sec.equalsIgnoreCase("Favourites"))
 			{
 				adapter.removeSection(sec);	
 
 			}
 		}//for sections
 		lv.setAdapter(adapter);
 	}//updateSections
 
 	/**
 	 * Builds the filter list.
 	 */
 	private void buildFilter()
 	{
 		SharedPreferences settings = getSharedPreferences(CATEGORY_SETTINGS, 0);
 		for (String cat : categories)
 		{
 			boolean checked = settings.getBoolean(cat, true);
 			CheckedCategories.put(cat, checked);
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		if (requestCode == NewPoiActivity.CHOOSE_POI)
 		{	
 			menu.removeItem(R.id.planMenuNewPoi);
 			menu.removeItem(R.id.planMenuSharePois);
 			menu.removeItem(R.id.planMenuUpdatePois);
 			menu.removeItem(R.id.planMenuAddPois);
 		}
 		else if (requestCode == SHARE_POI)
 		{	
 			menu.removeItem(R.id.planMenuAddPois);
 			menu.removeItem(R.id.planMenuNewPoi);
 			menu.removeItem(R.id.planMenuUpdatePois);
 		}
 		else if(requestCode == DOWNLOAD_POI){
 			menu.removeItem(R.id.planMenuAddPois);
 			menu.removeItem(R.id.planMenuNewPoi);
 			menu.removeItem(R.id.planMenuSharePois);
 			menu.removeItem(R.id.planMenuFilter);
 		}
 		else if(requestCode == PlanTripTab.ADD_TO_TRIP  || requestCode == TripListActivity.ADD_TO_TRIP){
 			menu.removeItem(R.id.planMenuNewPoi);
 			menu.removeItem(R.id.planMenuSharePois);
 			menu.removeItem(R.id.planMenuUpdatePois);
 		}
 		else
 		{
 			menu.removeItem(R.id.planMenuAddPois);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		menu.setGroupVisible(R.id.planMenuGroupTrip, false);
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 
 		if(item.getItemId() == R.id.planMenuNewPoi){
 			Intent newPoi = new Intent(PlanPoiTab.this, NewPoiActivity.class);
 			startActivity(newPoi);
 		}
 
 		if(item.getItemId() == R.id.planMenuFilter)
 		{
 			categories = DBFactory.getInstance(this).getUniqueCategoryNames();
 			Collections.sort(categories);
 
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setTitle("Filter");
 			ArrayList<String> cat = (ArrayList<String>) categories.clone();
 			cat.add(0, "Favourites");
 
 
 			boolean[] CheckedCat = new boolean[cat.size()];
 			for (String c : cat)
 			{
 				if(CheckedCategories.get(c) == null)
 				{
 					CheckedCategories.put(c, true);
 				}
 				CheckedCat[cat.indexOf(c)] = CheckedCategories.get(c);
 			}
 
 			String[] array = new String[cat.size()];
 			cat.toArray(array);
 			alert.setMultiChoiceItems(array, CheckedCat, this);
 			alert.setPositiveButton("OK", this);
 			alert.create();
 			alert.show();
 		}
 
 		if(item.getItemId() == R.id.planMenuUpdatePois)
 		{
 			if(requestCode == DOWNLOAD_POI){
 				if (downloadedPois==null){
 					Toast.makeText(this, "No locations selected", Toast.LENGTH_LONG).show();
 					return false;
 				}else {
 					int[] res = du.storePois(downloadedPois);
 					Toast.makeText(context, res[0]+" locations added, "+res[1]+" locations updated", Toast.LENGTH_LONG).show();
 				}
 				finish();
 			}else {				
 				Intent downloadPoi= new Intent(PlanPoiTab.this, PlanPoiTab.class);
 				downloadPoi.putExtra("requestCode", DOWNLOAD_POI);
 				startActivityForResult(downloadPoi, DOWNLOAD_POI);
 			}
 		}
 
 		if(item.getItemId() == R.id.planMenuSharePois)
 		{
 			if(requestCode == SHARE_POI){
 				if (sharePois==null){
 					Toast.makeText(this, "No locations selected", Toast.LENGTH_LONG).show();
 					return false;
 				}else {
 					Sharing.send(this, sharePois);
 					sharePois = null;
 				}
 				finish();
 			}else {
 				Intent sharePoi= new Intent(PlanPoiTab.this, PlanPoiTab.class);
 				sharePoi.putExtra("requestCode", SHARE_POI);
 				startActivityForResult(sharePoi, SHARE_POI);
 			}
 		}
 
 		if(item.getItemId() == R.id.planMenuAddPois)
 		{
 			if(requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){
 				if (selectedPois==null){
 					Toast.makeText(this, "No locations selected", Toast.LENGTH_LONG).show();
 					return false;
 				}else {
 					for (Poi p : selectedPois) {
 						trip.addPoi(p);
 						DBFactory.getInstance(this).addPoiToTrip(trip, p);						
 					}
 					Toast.makeText(this, selectedPois.size() + " locations added to " + trip.getLabel() + ".", Toast.LENGTH_LONG).show();
 					selectedPois = null;
 				}
 				Intent resultIntent = new Intent();
 				resultIntent.putExtra(IntentPassable.TRIP, trip);
 				setResult( Activity.RESULT_OK, resultIntent );
 				finish();
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int pos, long id) {
 		if(l.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER)
 		{
 			//Pressing a header			
 			return;
 		}
 		Poi p = (Poi) l.getAdapter().getItem(pos);
 
 		if (requestCode == NewPoiActivity.CHOOSE_POI){
 			Intent resultIntent = new Intent();
 			resultIntent.putExtra(IntentPassable.POI, p);
 			setResult( Activity.RESULT_OK, resultIntent );
 			finish();
 			return;
 		}
 
 		if (requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){
 
 			if(selectedPois == null){				
 				selectedPois = new ArrayList<Poi>();
 			}
 			if(!selectedPois.contains(p)){
 				v.setBackgroundColor(0xff9ba7d5);
 				selectedPois.add(p);
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				selectedPois.remove(p);
 			}
 			return;
 		}
 
 
 		if (requestCode == SHARE_POI){
 			if(sharePois == null){				
 				sharePois = new ArrayList<Poi>();
 			}
 			if(!sharePois.contains(p)){
 				v.setBackgroundColor(0xff9ba7d5);
 				sharePois.add(p);
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				sharePois.remove(p);
 			}
 			return;
 		}
 
 		if (requestCode == DOWNLOAD_POI){
 
 			if(downloadedPois == null){				
 				downloadedPois = new ArrayList<Poi>();
 			}
 			if(!downloadedPois.contains(p)){
 				v.setBackgroundColor(0xff9ba7d5);
 				downloadedPois.add(p);
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				downloadedPois.remove(p);
 			}
 			return;
 		}
 
 		Intent details = new Intent(PlanPoiTab.this, PoiDetailsActivity.class);
 		details.putExtra(IntentPassable.POI, p);
 
 		startActivity(details);
 	}
 
 	/**
 	 * Shows quick actions when the user long-presses an item.
 	 */
 	final private class DrawPopup implements AdapterView.OnItemLongClickListener {
 		public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
 
 			if(parent.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER)
 			{
 				Intent showInMap = new Intent(PlanPoiTab.this, MapsActivity.class);
 				Adapter sectionAd = adapter.getAdapter(parent.getAdapter().getItem(pos).toString());
 				ArrayList<Poi> selectedPois = new ArrayList<Poi>();
 				for (int i = 0; i < sectionAd.getCount(); i++)
 				{
 					selectedPois.add((Poi) sectionAd.getItem(i));
 				}
 				showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, selectedPois);
 
 				startActivity(showInMap);
 
 				return true;
 			}
 
 			final Poi	p 			= (Poi) parent.getAdapter().getItem(pos);
 			final AdapterView<?> par = parent;
 
 			final int[] xy 			= new int[2]; v.getLocationInWindow(xy);
 
 			final Rect rect 		= new Rect(	xy[0], 
 					xy[1], 
 					xy[0]+v.getWidth(), 
 					xy[1]+v.getHeight());
 
 			final QuickActionPopup qa = new QuickActionPopup(PlanPoiTab.this, v, rect);
 
 			Drawable addToTripIcon	= res.getDrawable(android.R.drawable.ic_menu_add);
 			Drawable mapviewIcon		= res.getDrawable(android.R.drawable.ic_menu_mapmode);
 			Drawable directIcon		= res.getDrawable(android.R.drawable.ic_menu_directions);
 			Drawable shareIcon		= res.getDrawable(android.R.drawable.ic_menu_share);
 			Drawable deleteIcon		= res.getDrawable(android.R.drawable.ic_menu_delete);
 
 
 			// Declare quick actions 
 
 			// 1: Show on Map
 			qa.addItem(mapviewIcon,	"Show on map", new OnClickListener(){
 
 				public void onClick(View view){
 					Intent showInMap = new Intent(PlanPoiTab.this, MapsActivity.class);
 					ArrayList<Poi> selectedPois = new ArrayList<Poi>();
 					selectedPois.add(p);
 					showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, selectedPois);
 					startActivity(showInMap);
 					qa.dismiss();
 				}
 			});
 
 			// 2:
 			qa.addItem(directIcon, "Get directions", new OnClickListener(){
 
 				public void onClick(View view){
 
 					//Latitude and longitude for current position
 					double slon = userLocation.getLongitude();
 					double slat = userLocation.getLatitude();
 					//Latitude and longitude for selected poi
 					double dlon = p.getGeoPoint().getLongitudeE6()/1E6;
 					double dlat = p.getGeoPoint().getLatitudeE6()/1E6;
 
 					Intent navigate = new Intent(PlanPoiTab.this, NavigateFrom.class);
 					navigate.putExtra("slon", slon);
 					navigate.putExtra("slat", slat);
 					navigate.putExtra("dlon", dlon);
 					navigate.putExtra("dlat", dlat);
 					startActivity(navigate);
 
 					qa.dismiss();
 				}
 			});
 
 			// 3: Favourite
 			if(p.isFavourite()){ // this POI is a favourite, add an option to not make it a favourite
 				Drawable	favIcon	= res.getDrawable(R.drawable.favstar_on);
 				qa.addItem(favIcon,	"",	new OnClickListener(){
 
 					public void onClick(View view)
 					{
 						//set favourite off
 						Poi poi = p;
 
 						poi = poi.modify().favourite(false).build();
 						DBFactory.getInstance(PlanPoiTab.this).editPoi(poi);//update poi;
 
 						adapter.notifyDataSetChanged();//update list
 						Toast.makeText(PlanPoiTab.this, poi.getLabel() + " removed from favourites.", Toast.LENGTH_LONG).show();
 						qa.dismiss();
 					}
 				});
 			}else{ // this POI is not a favourite, add an option to make it a favourite
 				Drawable	favIcon	= res.getDrawable(R.drawable.favstar_off);
 				qa.addItem(favIcon,	"",	new OnClickListener(){
 
 					public void onClick(View view)
 					{
 						//set as favourite
 						Poi poi = p;
 
 						poi = poi.modify().favourite(true).build();
 						DBFactory.getInstance(PlanPoiTab.this).editPoi(poi);//update poi;
 
 						allPois.remove(p);
 						allPois.add(poi);
 						Toast.makeText(PlanPoiTab.this, poi.getLabel() + " added to favourites.", Toast.LENGTH_LONG).show();
 						adapter.notifyDataSetChanged();//update list
 						qa.dismiss();
 					}
 				});
 			}
 
 			// 4: Edit 
 			// 5: 
 			qa.addItem(shareIcon, "Share", new OnClickListener(){
 
 				public void onClick(View view){
 					ArrayList<Poi> sharePoi = new ArrayList<Poi>();
 					sharePoi.add(p);
 					Sharing.send(PlanPoiTab.this, sharePoi);
 					qa.dismiss();
 				}
 			});
 
 			// 6: 
			qa.addItem(addToTripIcon, "Add Locations", new OnClickListener(){
 
 				public void onClick(View view){
 					poi = p;
 					Intent selectTrip = new Intent(PlanPoiTab.this, PlanTripTab.class);
 					selectTrip.putExtra("requestCode", ADD_TO_TRIP);
 					startActivityForResult(selectTrip, ADD_TO_TRIP);
 					qa.dismiss();
 				}
 			});
 
 			// 7:
 			qa.addItem(deleteIcon, "Delete", new OnClickListener(){
 
 				public void onClick(View view){
 
 					DBFactory.getInstance(context).deletePoi(p);											
 					updateSections();
 					((SeparatedListAdapter)par.getAdapter()).notifyDataSetChanged();
 
 					qa.dismiss();
 				}
 			});
 
 			qa.show();
 			return true;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode==Activity.RESULT_CANCELED){
 			return;
 		}
 		switch (requestCode){
 		case ADD_TO_TRIP:
 			Trip trip = (Trip) data.getParcelableExtra(IntentPassable.TRIP);
 			if (trip==null){ 
 				break;
 			}
 
 			trip.addPoi(poi);
 			DBFactory.getInstance(this).addPoiToTrip(trip, poi);
 			Toast.makeText(this, poi.getLabel() + " added to " + trip.getLabel() + ".", Toast.LENGTH_LONG).show();
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	/**
 	 * Initializes the GPS of the phone.
 	 */
 	void initGPS()
 	{
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		onLocationChanged(lastKnownLocation);
 
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		this.userLocation = location;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(this, R.string.map_gps_disabled_toast, Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(this, "Waiting for GPS lock", Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 
 	}
 
 	/**
 	 * Handles click events in the filter dialog.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onClick(DialogInterface dialog, int which, boolean isChecked)
 	{
 		ArrayList<String> cat = (ArrayList<String>) categories.clone();
 		cat.add(0, "Favourites");
 
 		CheckedCategories.remove(cat.get(which));
 		CheckedCategories.put(cat.get(which), isChecked);
 	}
 
 	/**
 	 * Handles the buttons in the filter dialog. 
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onClick(DialogInterface dialog, int which)
 	{
 		//add selection to settings:
 		SharedPreferences settings = getSharedPreferences(CATEGORY_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		//editor.putBoolean("key", value);
 
 		ArrayList<String> cat = (ArrayList<String>) categories.clone();
 		cat.add(0, "Favourites");
 
 		for (String title : cat)
 		{
 			boolean isChecked = CheckedCategories.get(title);
 
 			//preferences:
 			editor.putBoolean(title, isChecked);
 
 			if( !isChecked){
 				if(adapter.getSectionNames().contains(title))
 					adapter.removeSection(title);
 			}
 			else {
 				ArrayList<Poi> list = new ArrayList<Poi>();
 
 				for (Poi poi : allPois)
 				{
 					if(title.equals("Favourites") && poi.isFavourite())//add to favourite section
 					{
 						list.add(poi);
 					}
 					else if(poi.getCategory().equals(title))
 					{
 						list.add(poi);
 					}
 				}
 				PoiAdapter testAdapter = new PoiAdapter(this, R.layout.plan_listitem, list);
 				adapter.addSection(title, testAdapter);
 
 				if(title.equals("Favourites"))
 					favouriteList = list;
 			}
 		}
 
 		// Commit the edits!
 		editor.commit();
 
 		lv.setAdapter(adapter);
 	}
 }
