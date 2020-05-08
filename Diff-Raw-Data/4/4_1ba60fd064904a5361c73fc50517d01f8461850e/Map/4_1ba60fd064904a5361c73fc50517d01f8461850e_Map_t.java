 /**
  *   Copyright (C) 2010-2011, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel.views;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 import no.rkkc.bysykkel.Constants;
 import no.rkkc.bysykkel.Constants.FindRackCriteria;
 import no.rkkc.bysykkel.MenuHelper;
 import no.rkkc.bysykkel.R;
 import no.rkkc.bysykkel.Toaster;
 import no.rkkc.bysykkel.db.RackAdapter;
 import no.rkkc.bysykkel.model.Rack;
 import no.rkkc.bysykkel.tasks.RackSyncTask;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class Map extends MapActivity {
     private BysyklistMapView mMapView;
     private MyLocationOverlay mMyLocation;
     private MapController mMapController;
     private RackAdapter mRackAdapter;
     private ViewHolder mViewHolder = new ViewHolder();
     private RacksOverlay mRackOverlay; 
     private RackStateThread mRackStateThread = new RackStateThread();
     private HashMap<Integer, RackState> mRackStateCache = new HashMap<Integer, RackState>();
     private GeoPoint mContextMenuGeoPoint = null;
     
     private static final String TAG = "Bysyklist-Map";
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mRackAdapter = new RackAdapter(Map.this);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.map);
         setupMapView();
         setupInfoPanel();
         setupMyLocation();
         
         if (isFirstRun()) {
             new RackSyncTask(this).execute((Void[])null);
             return;
         }
 
         initializeMap();
 
         if (getLastNonConfigurationInstance() != null) {
             restoreStateAfterOrientationChange();
         } else if ("no.rkkc.bysykkel.FIND_NEAREST_READY_BIKE".equals(getIntent().getAction())
             || "no.rkkc.bysykkel.FIND_NEAREST_FREE_SLOT".equals(getIntent().getAction())
             || "no.rkkc.bysykkel.SHOW_RACK".equals(getIntent().getAction())) {
             // TODO: Should really find a better way to single out these intents
             processIntent(getIntent());
         } else {
             // Default and most common action.
             animateToLastKnownLocationIfAvailable();
             animateToMyLocationOnFirstFix();
         }
         
         setupListeners();
     }
 
     private void restoreStateAfterOrientationChange() {
         MapContext mapContext = (MapContext)getLastNonConfigurationInstance();
         
         mMapController.setZoom(mapContext.zoomLevel);
         mMapController.setCenter(mapContext.mapCenter);
         if (mapContext.highlightedRack != null) {
             highlightRack(mapContext.highlightedRack);
             showRackInfo(mapContext.highlightedRack);
         }
         mRackStateCache = mapContext.rackStateCache;
         
         for (RackState rackState: mRackStateCache.values()) {
             Rack rack = rackState.getRack();
             
             if ((Integer)rack.getId() != mRackOverlay.highlightedRackId) {
                 mRackOverlay.setMarker(rackState.getRack());
             }
         }
     }
 
     private void setupListeners() {
         mMapView.setOnLongpressListener(new BysyklistMapView.OnLongpressListener() {
             public void onLongpress(final MapView view, GeoPoint longpressLocation) {
                 mContextMenuGeoPoint = longpressLocation;
                 runOnUiThread(new Runnable() {
                     public void run() {
                         view.showContextMenu();
                     }
                 });
             }
         });
         
         mMapView.setOnZoomChangeListener(new BysyklistMapView.OnZoomChangeListener() {
             public void onZoomChange(MapView view, int newZoom, int oldZoom) {
                 mRackStateThread.mHandler.sendEmptyMessage(RackStateThread.UPDATE_VISIBLE_RACKS);
             }
         });
 
         mMapView.setOnPanChangeListener(new BysyklistMapView.OnPanChangeListener() {
             public void onPanChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter) {
                 mRackStateThread.mHandler.sendEmptyMessage(RackStateThread.UPDATE_VISIBLE_RACKS);     
             }
         });
     }
 
     public void animateToMyLocationOnFirstFix() {
         mMyLocation.runOnFirstFix(new Runnable() {
             public void run() {
                 mMapController.setZoom(16);
                 animateToMyLocation();
             }
         });
     }
 
     private void animateToLastKnownLocationIfAvailable() {
         GeoPoint recentLocation = mMyLocation.getMyLocation();
         if (recentLocation != null) {
             mMapController.animateTo(recentLocation);
         } else {
             showOsloOverview();
         }
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
     }
     
     @Override
     protected void onStart() {
         super.onStart();
         
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         mMapView.setSatellite(prefs.getString("map-mode", "MAP").equals("SATELLITE"));
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mRackStateThread.enable();
         mMyLocation.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         mMyLocation.disableMyLocation();
         mRackStateThread.disable();
     }
     
     @Override
     protected void onStop() {
         super.onStop();
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mRackAdapter.close();
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
         return new MapContext();
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case Constants.DIALOG_SEARCHING_BIKE:
                 ProgressDialog bikeSearchDialog = new ProgressDialog(this);
                 String message = String.format(getString(R.string.searchdialog_message_first), 
                         getString(R.string.word_bike));
                 bikeSearchDialog.setMessage(message);
                 bikeSearchDialog.setIndeterminate(true);
                 bikeSearchDialog.setCancelable(true);
                 
                 return bikeSearchDialog;
             case Constants.DIALOG_SEARCHING_SLOT:
                 ProgressDialog slotSearchDialog = new ProgressDialog(this);
                 String slotMessage = String.format(getString(R.string.searchdialog_message_first), 
                         getString(R.string.word_slot));
                 slotSearchDialog.setMessage(slotMessage);
                 slotSearchDialog.setIndeterminate(true);
                 slotSearchDialog.setCancelable(true);
                 
                 return slotSearchDialog;
             case Constants.DIALOG_ABOUT:
                 return new AboutDialog(this);
         }
         
         return super.onCreateDialog(id);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu  menu, View  v, ContextMenu.ContextMenuInfo menuInfo) {
         menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getString(R.string.nearest_bike));
         menu.add(Menu.NONE, Menu.FIRST+1, Menu.NONE, getString(R.string.nearest_slot));
     }
 
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case Menu.FIRST:
                 new ShowNearestRackTask(FindRackCriteria.ReadyBike, mContextMenuGeoPoint).execute();
                 return true;
             case Menu.FIRST+1:
                 new ShowNearestRackTask(FindRackCriteria.FreeSlot, mContextMenuGeoPoint).execute();
                 return true;
         }
             
         return super.onContextItemSelected(item);
     }
 
     /* Menu */
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         menu.setGroupVisible(R.id.map_menu, true);
         return true;
     }
 
     /* Handles menu item selections */
     public boolean onOptionsItemSelected(MenuItem item) {
         MenuHelper menuHelper = new MenuHelper(this);
         return menuHelper.mapOptionsItemSelected(item);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
     
     @Override
     public boolean dispatchTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             hideRackInfo();
         }
         
         return super.dispatchTouchEvent(event);
     }
     
     private boolean isFirstRun() {
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         if (settings.getLong("racksUpdatedTime", -1) == -1) {
             return true;
         } 
 
         return false;
     }
 
     private void setupMapView() {
         // Set up map view
         mMapView = (BysyklistMapView)findViewById(R.id.mapview);
         mMapView.setBuiltInZoomControls(true);
         registerForContextMenu(mMapView);
         mMapController = mMapView.getController();
         
     }
 
     private void setupInfoPanel() {
         mViewHolder.infoPanel = (RackInfoPanel) findViewById(R.id.infoPanel);
         mViewHolder.infoPanel.setVisibility(View.GONE);
         mViewHolder.name = (TextView) mViewHolder.infoPanel.findViewById(R.id.name);
         mViewHolder.information = (TextView) findViewById(R.id.information);
     }
 
     private void setupMyLocation() {
         mMyLocation = new MyLocationOverlay(this, mMapView);
         mMyLocation.enableMyLocation();
         mMapView.getOverlays().add(mMyLocation);
     }
     
     /**
      * Check given intent for action that corresponds to one of our shortcuts and perform task.
      */
     private void processIntent(Intent intent) {
         String action = intent.getAction();
         
         if (action == null) {
             return;
         } else if (action.equals("no.rkkc.bysykkel.FIND_NEAREST_READY_BIKE")) {
             new ShowNearestRackTask(FindRackCriteria.ReadyBike).execute();
         } else if (action.equals("no.rkkc.bysykkel.FIND_NEAREST_FREE_SLOT")) {
             new ShowNearestRackTask(FindRackCriteria.FreeSlot).execute();
         } else if (action.equals("no.rkkc.bysykkel.SHOW_RACK")) {
             Rack rack = mRackAdapter.getRack(intent.getIntExtra("rackId", 0));
             mMapController.setCenter(rack.getLocation());
             mRackOverlay.highlightRack(intent.getIntExtra("rackId", 0));
             showRackInfo(rack);
         }
     }
     
 
     @Override
     public void onNewIntent(Intent newIntent) {
         super.onNewIntent(newIntent);
 
         // If we end up here we have probably used one of our shortcuts, find out which.
         processIntent(newIntent);
     }
 
     /**
      * Display overview of Oslo. Used when no fix before GPS/GSM has been acquired.
      */
     private void showOsloOverview() {
         // Show standard location (Overview of Oslo) 
         mMapController.setZoom(13);
         mMapController.setCenter(new GeoPoint((int)(59.924653*1E6), (int) (10.731071*1E6)));
     }
 
     /**
      * Set up the map with the overlay containing the bike rack representions
      */
     public void initializeMap() {
         mRackOverlay = initializeRackOverlay(mRackAdapter.getRacks());
         mMapView.getOverlays().add(mRackOverlay);  
         mMapView.invalidate();
     }
 
     /**
      * Initial setup of the overlay, defining the pin. 
      * 
      * @return {@link RacksOverlay}
      */
     private RacksOverlay initializeRackOverlay(ArrayList<Rack> racks) {
         Drawable default_marker = getResources().getDrawable(R.drawable.bubble_red_questionmark);
         default_marker.setBounds(0, 0, default_marker.getIntrinsicWidth(), default_marker
                 .getIntrinsicHeight());
         
         RacksOverlay rackOverlay = new RacksOverlay(default_marker, racks);
         return rackOverlay;
     }
 
     /**
      * @param closestRackWithSlotOrBike
      */
     private void animateToRack(Rack closestRackWithSlotOrBike) {
         mMapController.animateTo(closestRackWithSlotOrBike.getLocation());
     }
 
     /**
      * 
      */
     public void animateToMyLocation() {
         new Thread(new Runnable(){
             public void run() {
                  Looper.prepare();
                 
                 GeoPoint location = getMyLocation();
                 
                 if (location != null) {
                     mMapController.animateTo(location);
                 }
                 
             }
             }).start();
     }
     
     /**
      * Retrieve fresh location fix, ignore most-recently-set location
      * 
      * @return
      */
     private GeoPoint getMyCurrentLocation() {
         return getMyLocation(false);
     }
     
     /**
      * Retrieve location fix, use most-recently-set if available.
      * 
      * @return
      */
     private GeoPoint getMyLocation() {
         return getMyLocation(true);
     }
     
     /**
      * 
      * @param useLastSet - Whether to allow use of last set location if available, or get fresh location fix.
      * @return
      */
     private GeoPoint getMyLocation(boolean useLastSet) {
         GeoPoint location = null;
         
         if (useLastSet) {
             location = mMyLocation.getMyLocation();
         }
         
         // Times in seconds
         int retryTime = 10;
         int retryTimeElapsed = 0;
 
         // If we don't have a location, try for retryTime seconds before giving up
         while (location == null && retryTimeElapsed/20 < retryTime) {
             if (retryTimeElapsed == 0) {
                 Toaster.toast(Map.this, R.string.location_waiting, Toast.LENGTH_SHORT);
             }
     
             SystemClock.sleep(200);
             
             location = mMyLocation.getMyLocation();
             retryTimeElapsed++;
         }
         
         if (location == null) {
             Toaster.toast(Map.this, R.string.location_not_found, Toast.LENGTH_SHORT);
         }
         
         return location;
     }
 
     public Rack getClosestRack(GeoPoint searchPoint, FindRackCriteria criteria) {
         if (searchPoint == null) {
             // No location to search from was specified.
             return null;
         }
         
         List<LocationAndDistance> sortedStationLocations = new ArrayList<LocationAndDistance>();
         for (Rack rack : mRackAdapter.getRacks()) {
             Log.v(Map.TAG, rack.toString());
             
             Location rackLocation = new Location("Bysyklist");
             rackLocation.setLatitude(rack.getLocation().getLatitudeE6() / 1E6);
             rackLocation.setLongitude(rack.getLocation().getLongitudeE6() / 1E6);
             
             Location searchLocation = new Location("Bysyklist");
             searchLocation.setLatitude(searchPoint.getLatitudeE6() / 1E6);
             searchLocation.setLongitude(searchPoint.getLongitudeE6() / 1E6);
             
             sortedStationLocations.add(new LocationAndDistance(rack, searchLocation.distanceTo(rackLocation)));
         }
         Collections.sort(sortedStationLocations);
 
         // Find first matching station
         Rack foundRack = null;
         Rack rack = null;
         for (LocationAndDistance lad : sortedStationLocations) {
             rack = mRackAdapter.getRack(lad.getStationIndex(), true);
             
             if (!rack.hasBikeAndSlotInfo()) continue; // Sometimes we get no information from the rack, so just skip it.
             
             if ((criteria == FindRackCriteria.ReadyBike && rack.getNumberOfReadyBikes() > 0)
                     || (criteria == FindRackCriteria.FreeSlot && 
                             rack.getNumberOfEmptySlots() > 0)) {
                 foundRack = rack;
                 Log.v(Map.TAG, "Found station:" + foundRack);
                 break;
             }
         }
         
         return foundRack;
     }
     
     public void showRackInfo(Rack rack) {
         mViewHolder.name.setText(rack.getDescription());
         mViewHolder.information.setText(R.string.rackdialog_fetching);
         mViewHolder.infoPanel.setRackId(rack.getId());
         mViewHolder.infoPanel.setVisibility(View.VISIBLE);
         mViewHolder.infoPanel.getStatusInfo();
         
         rack.incrementViewCount();
         mRackAdapter.save(rack);
     }
     
     public void showRackInfo(int rackId) {
         Rack rack = mRackAdapter.getRack(rackId);
         showRackInfo(rack);
     }
     
     public void hideRackInfo() {
         if (mViewHolder.infoPanel != null) {
             mRackOverlay.resetHighlighting();
             mViewHolder.infoPanel.setVisibility(View.GONE);
         }
     }
     
     /**
      * @param nearestRackWithSlotOrBike
      * @return
      */
     public void highlightRack(Integer rackId, final Integer duration) {
         mRackOverlay.highlightRack(rackId);
         
         if (duration == null) {
             return;
         }
 
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(duration);
                 } catch (InterruptedException e) {
                     // Don't do anything. The finally-clause will revert to previous state anyway.
                 } finally {
                     mRackOverlay.resetHighlighting();
                     mMapView.postInvalidate();
                 }
             }
         });
         
         Rack rack = mRackAdapter.getRack(rackId);
         rack.incrementViewCount();
         mRackAdapter.save(rack);
     }
     
     public void highlightRack(Integer rackId) {
         highlightRack(rackId, null);
     }
 
     private class LocationAndDistance implements Comparable<LocationAndDistance> {
         private Rack rack;
         // private Location location;
         private float distanceInMeters;
         
         public LocationAndDistance(Rack rack, float distanceInMeters) {
             this.rack = rack;
             // this.location = location;
             this.distanceInMeters = distanceInMeters;
         }
         
         public int compareTo(LocationAndDistance another) {
             return (int) (this.distanceInMeters - another.distanceInMeters);
         }
         
         public int getStationIndex() {
             return rack.getId();
         }
     }
     
     private class RacksOverlay extends ItemizedOverlay<OverlayItem> {
         private List<OverlayItem> items = new ArrayList<OverlayItem>();
         private ArrayList<Rack> racks;
         private Integer highlightedRackId;
 
         /**
          * Marker indicating that there are both free bikes and locks
          */
         private Drawable ok_marker;
         
         /**
          * Marker indicating that the rack has free bikes, but no locks
          */
         private Drawable partial_marker_bikes;
         
         /**
          * Marker indicating that the rack has ready locks, but no bikes
          */
         private Drawable partial_marker_locks;
         
         /**
          * Marker indicating that we do not know status of rack.
          */
         private Drawable data_missing_marker;
         
         /**
          * Marker indicating that user is viewing the info popup of a rack.
          */
         private Drawable info_marker;
         
         public RacksOverlay(Drawable default_marker, ArrayList<Rack> racks) {
             super(default_marker);
             this.racks = racks;
             boundCenterBottom(default_marker);
             
             setupMarkers();
             populate();
         }
         
         private void setupMarkers() {
             partial_marker_bikes = getResources().getDrawable(R.drawable.bubble_yellow_bicycle);
             partial_marker_bikes.setBounds(0, 0, partial_marker_bikes.getIntrinsicWidth(), 
                     partial_marker_bikes.getIntrinsicHeight());
             
             partial_marker_locks = getResources().getDrawable(R.drawable.bubble_yellow_parking);
             partial_marker_locks.setBounds(0, 0, partial_marker_locks.getIntrinsicWidth(), 
                     partial_marker_locks.getIntrinsicHeight());
             
             ok_marker = getResources().getDrawable(R.drawable.bubble_green);
             ok_marker.setBounds(0, 0, ok_marker.getIntrinsicWidth(), ok_marker
                     .getIntrinsicHeight());
             
             info_marker = getResources().getDrawable(R.drawable.bubble_info);
             info_marker.setBounds(0, 0, info_marker.getIntrinsicWidth(), info_marker
                     .getIntrinsicHeight());
     
             data_missing_marker = getResources().getDrawable(R.drawable.bubble_red);
             data_missing_marker.setBounds(0, 0, data_missing_marker.getIntrinsicWidth(), data_missing_marker
                     .getIntrinsicHeight());
             
             boundCenterBottom(partial_marker_bikes);
             boundCenterBottom(partial_marker_locks);
             boundCenterBottom(ok_marker);
             boundCenterBottom(info_marker);
             boundCenterBottom(data_missing_marker);
         }
         
         public void highlightRack(int rackId) {
             highlightedRackId = rackId;
             setMarker(rackId, info_marker);
         }
         
         /**
          * Display the marker indicating that we do not know the status of this rack.
          * 
          * @param rackId
          */
         public void setDataMissingMarker(int rackId) {
             setMarker(rackId, data_missing_marker);
         }
         
         /**
          * Display the marker indicating that there ready bikes
          * 
          * @param rackId
          */
         public void setReadyBikesMarker(int rackId) {
             setMarker(rackId, partial_marker_bikes);
         }
         
         /**
          * Display the marker indicating that there are free locks.
          * 
          * @param rackId
          */
         public void setEmptyLocksMarker(int rackId) {
             setMarker(rackId, partial_marker_locks);
         }
         
         /**
          * Display the marker indicating that there are both free bikes and locks.
          * 
          * @param rackId
          */
         public void setOkMarker(int rackId) {
             setMarker(rackId, ok_marker);
         }
         
         public void setMarker(int rackId, Drawable marker) {
             getItem(findOverlayIndex(rackId)).setMarker(marker);
             mMapView.postInvalidate();
         }
         
         /**
          * Sets the marker drawable according to the state stored in the given Rack.
          * 
          * @param rack 
          */
         public void setMarker(Rack rack) {
             if (rack.isOnline() && rack.hasBikeAndSlotInfo() && rack.hasReadyBikes() && rack.hasEmptySlots()) {
                 setOkMarker(rack.getId());
             } else if (rack.isOnline() && rack.hasBikeAndSlotInfo() && rack.hasReadyBikes()) {
                 setReadyBikesMarker(rack.getId());
             } else if (rack.isOnline() && rack.hasBikeAndSlotInfo() && rack.hasEmptySlots()) {
                 setEmptyLocksMarker(rack.getId());
             } else {
                 setDataMissingMarker(rack.getId());
             }
         }
 
         public void resetHighlighting() {
             if (highlightedRackId != null) {
                 setMarker(getRackState(highlightedRackId).getRack());
                 highlightedRackId = null;
             }
         }
         
         @Override
         protected OverlayItem createItem(int i) {
             Rack rack = racks.get(i);
 //            Log.v(Map.TAG, "Adding rack "+rack.getId() + " to overlay");
             OverlayItem item = new OverlayItem(rack.getLocation(), rack.getDescription(), Integer.toString(rack.getId()));
             items.add(item);
 
             return item;
         }
 
         @Override
         public void draw(Canvas canvas, MapView mapView, boolean shadow) {
             super.draw(canvas, mapView, shadow);
         }
 
         @Override
         public int size() {
             return racks.size();
         }
         
         @Override
         protected boolean onTap(int i) {
             Rack rack = findRack(i);
             
             highlightRack(rack.getId());
             showRackInfo(rack);
             
             return true;
         }
 
         private Rack findRack(int overlayIndex) {
             Rack rack = null;
             for (int i = 0; i < racks.size(); i++) {
                 rack = racks.get(i);
                 int rackId = rack.getId();
                 int overlayIndexRackId = Integer.parseInt(items.get(overlayIndex).getSnippet());
                 
                 if (rackId == overlayIndexRackId) {
                     return rack;
                 }
             }
             
             // This should never occur
             throw new NoSuchElementException("Overlay with index " + overlayIndex
                     + " doesn't exists as rack");
         }
         
         private int findOverlayIndex(int rackId) {
             for (int i = 0; i < this.size(); i++) {
                 if (rackId == Integer.parseInt(items.get(i).getSnippet())) {
                     return i;
                 }
             }
             
             // This should never occur
             throw new NoSuchElementException("Overlay with rack " + rackId
                     + " doesn't exists");
         }
     }
     
     public class ShowNearestRackTask extends AsyncTask<Object, Void, Void> {
         int dialogId;
         FindRackCriteria criteria;
         GeoPoint geoPoint;
         Rack nearestRack;
         
         public ShowNearestRackTask(FindRackCriteria criteria, GeoPoint geoPoint) {
             super();
             
             if (criteria == FindRackCriteria.ReadyBike) {
                 this.dialogId = Constants.DIALOG_SEARCHING_BIKE;
             } else {
                 this.dialogId = Constants.DIALOG_SEARCHING_SLOT;
             }
             this.criteria = criteria;
             this.geoPoint = geoPoint;
         }
         
         public ShowNearestRackTask(FindRackCriteria criteria) {
             this(criteria, null);
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             
             hideRackInfo();
             mRackOverlay.resetHighlighting();
             showDialog(dialogId);
         }
         
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             
             dismissDialog(dialogId);
             
             if (nearestRack == null) {
                 // No rack found. Inform user and exit.
                 Toaster.toast(Map.this, R.string.error_search_failed, Toast.LENGTH_SHORT);
             } else {
                 highlightRack(nearestRack.getId(), 3000);
                 showRackInfo(nearestRack);
                 animateToRack(nearestRack);
                 
                 // Show stats for the closest rack
                 Toaster.toast(Map.this, getRackInfoText(nearestRack, criteria), Toast.LENGTH_SHORT);
             }
         }
 
         @Override
         protected Void doInBackground(Object... params) {
             // Establish location to search from
             GeoPoint searchPoint;
             if (geoPoint == null) {
                 searchPoint = getMyCurrentLocation();
             } else {
                 searchPoint = geoPoint;
             }
 
             if (searchPoint == null) {
                 return null;
             }
             
             nearestRack = getClosestRack(searchPoint, criteria);
             return null;
         }
         
         /**
          * Constructs string that is to be displayed to user when rack with bikes or free locks has been found
          * 
          * @param rack
          * @param criteria
          * @return
          */
         private String getRackInfoText(Rack rack, FindRackCriteria criteria) {
             final int noOfFreeItems;;
             final String itemType;
             if (criteria == FindRackCriteria.FreeSlot) {
                 noOfFreeItems = rack.getNumberOfEmptySlots();
                 if (noOfFreeItems > 1) {
                     itemType = getText(R.string.word_slots).toString();
                 } else {
                     itemType = getText(R.string.word_slot).toString();
                 }
             } else {
                 noOfFreeItems = rack.getNumberOfReadyBikes();
                 if (noOfFreeItems > 1) {
                     itemType = getText(R.string.word_bikes).toString();
                 } else {
                     itemType = getText(R.string.word_bike).toString();
                 }
             }
             
             return Integer.toString(noOfFreeItems) + " " + itemType;
         }
         
     }
     
     /**
      * Updates the state information of all racks currently visible on screen.
      * 
      * By default it refreshes the information every X seconds, but the handler accepts requests
      * to update the information at any time. When an update request is received, the scheduled
      * update is postponed.
      */
     class RackStateThread extends Thread {
           private static final int TIME_BETWEEN_UPDATES = 60000;
           static final int UPDATE_VISIBLE_RACKS = 1;
           public Handler mHandler;
           private boolean isDisabled;
 
           public void run() {
               Looper.prepare();
 
               mHandler = new Handler() {
                   public void handleMessage(Message msg) {
                       mHandler.removeMessages(UPDATE_VISIBLE_RACKS);
                       
                       runOnUiThread(new Runnable() {
                         public void run() {
                             setProgressBarIndeterminateVisibility(true);
                         }
                       });
 
                       /*
                        * Get top left and bottom right corners, to know which racks to update. We
                        * include a little extra space around the visible area, since the marker
                        * icon may be visible if the point is just outside the screen.
                        */
                      GeoPoint topLeft = mMapView.getProjection().fromPixels(-15, 0);
                      GeoPoint bottomRight = mMapView.getProjection().fromPixels(mMapView.getWidth()+15, mMapView.getHeight()+35);
                       
                       Integer[] rackIds = mRackAdapter.getRackIds(topLeft, bottomRight);
 
                       for (final int rackId: rackIds) {
                           if (mHandler.hasMessages(UPDATE_VISIBLE_RACKS) || isDisabled) {
                               // A new message has come in, these racks are no longer interesting.
                               break;
                           }
                           
                           final Rack rack;
                           if (mRackStateCache.get(rackId) != null && !mRackStateCache.get(rackId).isStale()) {
                               // Get cached rack
                               rack = getRackState(rackId).getRack();
                           } else {
                               rack = mRackAdapter.getRack(rackId, true);
                               setRackState(rack);
                           }
                           
                           runOnUiThread(new Runnable() {
                                 public void run() {
                                     if (mRackOverlay.highlightedRackId == null ||
                                             rack.getId() != mRackOverlay.highlightedRackId) {
                                         mRackOverlay.setMarker(rack);
                                     }
                                 }
                           });
                       }
                       
                       runOnUiThread(new Runnable() {
                           public void run() {
                               setProgressBarIndeterminateVisibility(false);
                           }
                       });
                       
                       if (!isDisabled) {
                           mHandler.sendEmptyMessageDelayed(UPDATE_VISIBLE_RACKS, TIME_BETWEEN_UPDATES);
                       }
                   }
               };
 
               /*
                * Fire off first message to start scheduled updates.
                * 
                * The delay is a hack to avoid a bug where we get "wrong" coordinates,
                * which only yields an update of a few racks in the center. Maybe map
                * has not been properly set up yet?
                */
               mHandler.sendEmptyMessageDelayed(UPDATE_VISIBLE_RACKS, 1000);
 
               Looper.loop();
           }
 
         public void enable() {
             if (!isAlive()) {
                 this.start();
             } else {
                 mHandler.sendEmptyMessage(RackStateThread.UPDATE_VISIBLE_RACKS);
             }
             isDisabled = false;
         }
 
         public void disable() {
             if (mHandler != null) {
                 mHandler.removeMessages(RackStateThread.UPDATE_VISIBLE_RACKS);
             }
             isDisabled = true;
         }
     }
     
 
     /**
      * Stores various Views to avoid instantiating them every time they are used.
      */
     static class ViewHolder {
         RackInfoPanel infoPanel;
         TextView name;
         TextView information;
     }
     
 
     /**
      * Keeps Rack object with information about when it was last updated. Used for determining
      * whether to get stack state from ClearChannel or use a Cached version.
      */
     class RackState {
         /**
          * The amount of time in ms before rack data should be considered stale.
          */
         private static final int TIME_BEFORE_CONSIDERED_STALE = 30000;
         
         /**
          * Time in unix time (ms since epoch) when rack this rack was stored.
          */
         long lastUpdated;
         
         Rack rack;
         
         public RackState(Rack rack) {
             setRack(rack);
         }
         
         public long getLastUpdated() {
             return lastUpdated;
         }
         
         public void setLastUpdate() {
             this.lastUpdated = System.currentTimeMillis();
         }
         
         public Rack getRack() {
             return rack;
         }
         
         public void setRack(Rack rack) {
             this.rack = rack;
             setLastUpdate();
         }
         
         /**
          * Checks whether the threshold for considering rack data expired.
          * 
          * @return boolean
          */
         public boolean isStale() {
             return (System.currentTimeMillis() - getLastUpdated()) > TIME_BEFORE_CONSIDERED_STALE;
         }
     }
     
     public RackState getRackState(int rackId) {
         return mRackStateCache.get(rackId);
     }
 
     public void setRackState(Rack rack) {
         mRackStateCache.put(rack.getId(), new RackState(rack));
     }
     
     /**
      * Class for keeping all the context we need to rebuild our map after orientation change
      */
     private class MapContext {
         int zoomLevel;
         GeoPoint mapCenter;
         Integer highlightedRack;
         HashMap<Integer, RackState> rackStateCache;
         
         public MapContext() {
             this.zoomLevel = mMapView.getZoomLevel();
             this.mapCenter = mMapView.getMapCenter();
             this.highlightedRack = mRackOverlay.highlightedRackId;
             this.rackStateCache = Map.this.mRackStateCache;
         }
     }
 }
