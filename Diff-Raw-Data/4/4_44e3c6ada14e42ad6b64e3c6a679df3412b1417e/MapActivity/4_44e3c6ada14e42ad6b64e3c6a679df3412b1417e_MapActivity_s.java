 /*******************************************************************************
  * Copyright (C) 2013-2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package ru.neverdark.phototools;
 
 import ru.neverdark.phototools.db.LocationsDbAdapter;
 import ru.neverdark.phototools.fragments.ConfirmCreateFragment;
 import ru.neverdark.phototools.utils.Constants;
 import ru.neverdark.phototools.utils.Log;
 import android.content.Intent;
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapActivity extends SherlockFragmentActivity implements
         OnMapLongClickListener {
 
     private GoogleMap mMap;
     private MenuItem mMenuItemDone;
     private Marker mMarker;
     private LatLng mMarkerPosition;
     private int mAction;
     private long mRecordId;
     private String mLocationName;
     private boolean isButtonVisible = false;
 
     /**
      * Handles getting information from Confirmation dialog
      * 
      * @param locationName
      *            location name
      */
     public void handleConfirmDialog(String locationName) {
         Log.message("Enter");
 
         Intent intent = new Intent();
         intent.putExtra(Constants.LOCATION_LATITUDE, mMarkerPosition.latitude);
         intent.putExtra(Constants.LOCATION_LONGITUDE, mMarkerPosition.longitude);
 
         if (locationName != null) {
             saveDataToDatabase(locationName);
             intent.putExtra(Constants.LOCATION_NAME, locationName);
         } else {
             mRecordId = Constants.LOCATION_POINT_ON_MAP_CHOICE;
         }
 
         intent.putExtra(Constants.LOCATION_RECORD_ID, mRecordId);
 
         setResult(RESULT_OK, intent);
         finish();
     }
 
     /**
      * Inits Google Map
      */
     private void initMap() {
         Log.message("Enter");
         if (mMap == null) {
             mMap = ((SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map)).getMap();
 
             mMap.setMyLocationEnabled(true);
             mMap.setOnMapLongClickListener(this);
         }
 
         /* gets current coord if have */
         Intent intent = getIntent();
         Double latitude = intent.getDoubleExtra(Constants.LOCATION_LATITUDE, 0);
         Double longitude = intent.getDoubleExtra(Constants.LOCATION_LONGITUDE,
                 0);
 
         /* gets action */
         mAction = intent.getByteExtra(Constants.LOCATION_ACTION,
                 Constants.LOCATION_ACTION_ADD);
 
         if (mAction == Constants.LOCATION_ACTION_EDIT) {
             mRecordId = intent.getLongExtra(Constants.LOCATION_RECORD_ID,
                     Constants.LOCATION_POINT_ON_MAP_CHOICE);
             mLocationName = intent.getStringExtra(Constants.LOCATION_NAME);
             mMarkerPosition = new LatLng(latitude, longitude);
             setMarker();
         }
 
         /* checks for coordinates was received */
         if ((latitude != 0) || (longitude != 0)) {
             CameraPosition currentPosition = new CameraPosition.Builder()
                     .target(new LatLng(latitude, longitude))
                     .zoom(Constants.MAP_CAMERA_ZOOM).build();
             mMap.moveCamera(CameraUpdateFactory
                     .newCameraPosition(currentPosition));
         }
 
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.message("Enter");
         super.onCreate(savedInstanceState);
         setTheme(R.style.Theme_Sherlock);
         setContentView(R.layout.activity_map);
 
         initMap();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         Log.enter();
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.map_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public void onMapLongClick(LatLng point) {
         Log.message("Enter");
         mMarkerPosition = point;
         setMarker();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.map_button_done:
             showConfirmDialog();
             return true;
         case R.id.map_type_map:
             mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
             return true;
 
         case R.id.map_type_terrain:
             mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
             return true;
         case R.id.map_type_satellite:
             mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
             return true;
         case R.id.map_type_hybrid:
             mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         Log.enter();
         mMenuItemDone = menu.findItem(R.id.map_button_done);
        setButtonVisible(isButtonVisible);
         return true;
     }
 
     /**
      * Saves data to the database
      * 
      * @param locationName
      *            location name for save into database
      */
     private void saveDataToDatabase(String locationName) {
         Log.message("Enter");
 
         LocationsDbAdapter dbAdapter = new LocationsDbAdapter(
                 getApplicationContext());
         dbAdapter.open();
 
         if (mAction == Constants.LOCATION_ACTION_ADD) {
             mRecordId = dbAdapter.createLocation(locationName,
                     mMarkerPosition.latitude, mMarkerPosition.longitude);
         } else if (mAction == Constants.LOCATION_ACTION_EDIT) {
             dbAdapter.updateLocation(mRecordId, locationName,
                     mMarkerPosition.latitude, mMarkerPosition.longitude);
         }
 
         dbAdapter.close();
     }
 
     /**
      * Sets visible property for Done button
      * 
      * @param isVisible
      *            true for Done button visible, false for invisible
      */
     private void setButtonVisible(final boolean isVisible) {
         Log.message("Enter");
         if (isVisible == true) {
             mMenuItemDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                     | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         } else {
             mMenuItemDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         }
         
         mMenuItemDone.setVisible(isVisible);
     }
 
     /**
      * Sets marker to the long tap position If marker already exists - remove
      * old marker and set new marker in new position
      */
     private void setMarker() {
         Log.message("Enter");
 
         /* If we have marker - destroy */
         if (mMarker != null) {
             mMap.clear();
         }
         mMarker = mMap.addMarker(new MarkerOptions().position(mMarkerPosition));
         isButtonVisible = true;
         invalidateOptionsMenu();
     }
 
     /**
      * Shows confirmation dialog
      */
     private void showConfirmDialog() {
         Log.message("Enter");
         ConfirmCreateFragment confirmDialog = ConfirmCreateFragment
                 .NewInstance(mAction, mLocationName);
         confirmDialog.show(getSupportFragmentManager(),
                 Constants.CONFIRM_DIALOG);
     }
 
 }
