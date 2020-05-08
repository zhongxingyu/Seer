 /**
  * @contributor(s): Kristian Greve Hagen (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:
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
  * This class handles all the action going on in the tours tab.
  * 
  */
 
 package org.ubicompforall.CityExplorer.gui;
 
 import java.util.ArrayList;
 
 import org.ubicompforall.CityExplorer.data.DBFactory;
 import org.ubicompforall.CityExplorer.data.DatabaseUpdater;
 import org.ubicompforall.CityExplorer.data.IntentPassable;
 import org.ubicompforall.CityExplorer.data.Poi;
 import org.ubicompforall.CityExplorer.data.SeparatedListAdapter;
 import org.ubicompforall.CityExplorer.data.Trip;
 import org.ubicompforall.CityExplorer.data.TripAdapter;
 import org.ubicompforall.CityExplorer.gui.TripListActivity;
 import org.ubicompforall.CityExplorer.map.MapsActivity;
 
import org.ubicompforall.CityExplorer.CityExplorer;
 import org.ubicompforall.CityExplorer.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class PlanTripTab extends PlanActivityTab{
 
 	/**
 	 * Field containing this activity's resources.
 	 */
 	private Resources res;
 	
 	/**
 	 * Field containing the adapter.
 	 */
 	private SeparatedListAdapter adapter;
 	
 	/**
 	 * Field containing this activity's ListView.
 	 */
 	private ListView lv;
 	
 	/**
 	 * Field containing the trip currently being modified.
 	 */
 	private Trip trip;
 	
 	/**
 	 * Field containing the request code for new trip.
 	 */
 	protected static final int NEW_TRIP = 3;
 	
 	/**
 	 * Field containing the request code for add to trip.
 	 */
 	protected static final int ADD_TO_TRIP = 4;
 	
 	/**
 	 * Field containing the request code from other activities.
 	 */
 	private int requestCode;
 	
 	/**
 	 * Field containing all pois from an old trip, when creating a new trip from an existing.
 	 */
 	private ArrayList<Poi> existingPois;
 	
 	/**
 	 * Field containing the name of the newly created trip.
 	 */
 	private String tripName;
 
 	private ArrayList<Trip> downloadedTrips;
 
 	/**
 	 * Field giving access to databaseupdater methods.
 	 */
 	private DatabaseUpdater du;
 	
 	/**
 	 * Field containing the request code for downloading trips.
 	 */
 	protected static final int DOWNLOAD_TRIP = 7;
 	
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);	
 		init();
 	}
 
 	/**
 	 * Initializes the activity.
 	 */
 	private void init() {
 		requestCode = getIntent().getIntExtra("requestCode",0);
 		lv = getListView();
 
 		if (requestCode == PlanPoiTab.ADD_TO_TRIP || requestCode == NewTripActivity.ADD_TO_TRIP || requestCode == DOWNLOAD_TRIP){
 			lv.setOnItemLongClickListener(null);
 		}else {			
 			lv.setOnItemLongClickListener(new DrawPopup());
 		}
 		
 		if(requestCode == DOWNLOAD_TRIP){			
 			du = new DatabaseUpdater(this);
 			TripAdapter tripAdapter = new TripAdapter(this, R.layout.plan_listitem, du.getInternetTrips());
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.INTERNET_TRIPS);
 			adapter.addSection("Server Tours", tripAdapter);
 		}else {			
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.TRIP_LIST);
 		}
 		
 		res = getResources();
 
 		adapter.notifyDataSetChanged();
 		lv.setAdapter(adapter); 
 
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int pos, long id) {
 		if(l.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER)
 		{
 			//Pressing a section header.
 			return;
 		}
 		trip = (Trip) l.getAdapter().getItem(pos);
 
 //		if(requestCode == NewTripActivity.ADD_TO_TRIP){
 ////			if(!isEmptyTrip(trip)){
 //				Intent resultIntent = new Intent();
 //				resultIntent.putExtra(IntentPassable.TRIP, trip);
 //				setResult( Activity.RESULT_OK, resultIntent );
 //				finish();
 ////			}else {
 ////				Toast.makeText(this, "This tour has no locations", Toast.LENGTH_LONG).show();
 ////			}
 //			return;
 //		} // NewTrip->ADD_TO_TRIP
 
 		if (requestCode == PlanPoiTab.ADD_TO_TRIP){
 			Intent resultIntent = new Intent();
 			resultIntent.putExtra(IntentPassable.TRIP, trip);
 			setResult( Activity.RESULT_OK, resultIntent );
 			finish();
 			return;
 		} // if ADD_TO_TRIP
 		
 		if (requestCode == DOWNLOAD_TRIP){
 
 			if(downloadedTrips == null){				
 				downloadedTrips = new ArrayList<Trip>();
 			}
 			if(!downloadedTrips.contains(trip)){
 				v.setBackgroundColor(0xff9ba7d5);
 				downloadedTrips.add(trip);
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				downloadedTrips.remove(trip);
 			}
 			return;
 		} // if download trip
 
 //		if (!isEmptyTrip(trip)) {
 			Intent details = new Intent( this, TripListActivity.class );
 			details.putExtra("trip", trip);
 			startActivity(details);
 //		} else {
 //			Toast.makeText(this, "This tour has no locations", Toast.LENGTH_LONG).show();
 //		}
 	} // onListItemClick
 
 	/**
 	 * Checks if the given trip is empty.
 	 * 
 	 * @param t The trip you want to check.
 	 * @return True if the trip is empty, false otherwise.
 	 */
 //	private boolean isEmptyTrip(Trip t) {
 //		if(t.getPois().size() > 0){
 //			return false;
 //		}else {
 //			return true;
 //		}
 //	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		adapter.notifyDataSetChanged();		
 
 		if(existingPois != null){
 			int nrOfPoIs = existingPois.size();
 			for (Poi p : existingPois) {
 				trip.addPoi(p);
 				DBFactory.getInstance(this).addPoiToTrip(trip, p);				
 			}
 			existingPois = null;
 			Toast.makeText(this, 
 					nrOfPoIs + " location(s) added to " + tripName + ".", Toast.LENGTH_LONG).show();
 		}
 		adapter.notifyDataSetChanged();
 
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		if (requestCode == PlanPoiTab.ADD_TO_TRIP || requestCode == NewTripActivity.ADD_TO_TRIP){
 			menu.setGroupVisible(R.id.planMenuGroupTrip, false);
 		}
 		if(requestCode == PlanTripTab.DOWNLOAD_TRIP){
 			menu.removeItem(R.id.planMenuNewTrip);
 		}
 		return true;
 	}
 
	private void debug( int level, String message ) {
		CityExplorer.debug( level, message );		
	} //debug

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
		debug(0, "here" );
 		super.onCreateOptionsMenu(menu);
 		menu.setGroupVisible(R.id.planMenuGroupPoi, false);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 
 		if(item.getItemId() == R.id.planMenuNewTrip){
 			Intent newTrip = new Intent(PlanTripTab.this, NewTripActivity.class);
 			newTrip.putExtra("requestCode", NEW_TRIP);
 			startActivityForResult(newTrip, NEW_TRIP);
 
 		}
 //		if(item.getItemId() == R.id.planMenuUpdateTrips){
 //			if(requestCode == DOWNLOAD_TRIP){
 //				if (downloadedTrips==null){
 //					Toast.makeText(this, "No trips selected", Toast.LENGTH_LONG).show();
 //					return false;
 //				}else {
 //					int[] res = du.storeTrips(downloadedTrips);
 //					
 //					Toast.makeText(this, res[0]+" trips added, "+res[1]+" trips updated", Toast.LENGTH_LONG).show();
 //				}
 //				finish();
 //			}else {				
 //				Intent downloadPoi= new Intent(PlanTabTrip.this, PlanTabTrip.class);
 //				downloadPoi.putExtra("requestCode", DOWNLOAD_TRIP);
 //				startActivityForResult(downloadPoi, DOWNLOAD_TRIP);
 //			}
 //		} // if planMenu->UpdateTrip // RS-120123
 		
 		return true;
 	}
 
 	/**
 	 * Shows quick actions when the user long-presses an item.
 	 */
 	final private class DrawPopup implements AdapterView.OnItemLongClickListener {
 
 		public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
 			if(parent.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER)
 			{
 				//Pressing a section header
 				return true;
 			}
 
 			final Trip	t 			= (Trip) parent.getAdapter().getItem(pos);
 			final int	idx			= pos;
 			final int[] xy 			= new int[2]; v.getLocationInWindow(xy);
 			final Rect rect 		= new Rect(	xy[0], 
 					xy[1], 
 					xy[0]+v.getWidth(), 
 					xy[1]+v.getHeight());
 
 			final QuickActionPopup qa = new QuickActionPopup(PlanTripTab.this, v, rect);
 
 			Drawable	addPoiIcon	= res.getDrawable(android.R.drawable.ic_menu_add);
 			Drawable	mapviewIcon	= res.getDrawable(android.R.drawable.ic_menu_mapmode);
 			Drawable	deleteIcon	= res.getDrawable(android.R.drawable.ic_menu_delete);
 
 
 			// declare quick actions 
 			qa.addItem(addPoiIcon, "Add to Tour", new OnClickListener(){
 				public void onClick(View view)
 				{
 					
 					Intent selectPoi = new Intent(PlanTripTab.this, PlanPoiTab.class);
 					selectPoi.putExtra(IntentPassable.TRIP, t);
 					selectPoi.putExtra("requestCode", ADD_TO_TRIP);
 					startActivityForResult(selectPoi, ADD_TO_TRIP);
 
 					qa.dismiss();
 				}
 			});
 			qa.addItem(mapviewIcon,	"Show on map", new OnClickListener(){
 				public void onClick(View view){
 					Intent showInMap = new Intent(PlanTripTab.this, MapsActivity.class);
 					showInMap.putExtra(IntentPassable.TRIP, t);
 					startActivity(showInMap);
 					qa.dismiss();
 				}
 			});
 
 
 			qa.addItem(deleteIcon, "Delete", new OnClickListener(){
 				public void onClick(View view){	
 					((TripAdapter)adapter.getAdapter(idx)).remove(t);
 					adapter.notifyDataSetChanged();
 					qa.dismiss();
 				}
 			});
 
 			qa.show();
 			return true;
 		} // onItemLongClick
 	} // DrawPopup
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode==Activity.RESULT_CANCELED){
 			return;
 		}
 		switch (requestCode){
 		case ADD_TO_TRIP:
 			Poi poi = (Poi) data.getParcelableExtra(IntentPassable.POI);
 			if (poi == null){ 
 				break;
 			}
 			trip.addPoi(poi);
 			Toast.makeText(this, poi.getLabel() + " added to " + trip.getLabel() + ".", Toast.LENGTH_LONG).show();
 			DBFactory.getInstance(this).addPoiToTrip(trip, poi);
 			break;
 		case NEW_TRIP:
 			existingPois = data.getParcelableArrayListExtra(IntentPassable.POILIST);
 			trip = data.getParcelableExtra(IntentPassable.TRIP);
 			tripName = data.getStringExtra(NewTripActivity.TRIPNAME);
 			if (existingPois==null){ 
 				break;
 			}
 			if(trip==null){
 				break;
 			}
 			break;
 		default:
 			break;
 		}
 	}
 }
