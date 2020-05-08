 /*Copyright 2013 Ahmad Al-saleem  
  * Copyright 2010, 2011, 2012 mapsforge.org  
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.oscim.app;
 
import org.oscim.app.App;
 import org.oscim.core.GeoPoint;
 import org.oscim.core.MapPosition;
 import org.oscim.core.MercatorProjection;
 import org.oscim.overlay.GenericOverlay;
 import org.oscim.overlay.ItemizedOverlay;
 import org.oscim.overlay.OverlayItem;
 import org.oscim.renderer.overlays.CircleOverlay;
 import org.oscim.renderer.overlays.TileOverlay;
 import org.oscim.view.MapView;
 import org.osmdroid.overlays.ItemizedOverlayWithBubble;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class LocationHandler {
 	private static final int DIALOG_LOCATION_PROVIDER_DISABLED = 2;
 
 	private MyLocationListener mLocationListener;
 	private LocationManager mLocationManager;
 	private boolean mShowMyLocation;
 
 	private ToggleButton mSnapToLocationView;
 	private boolean mSnapToLocation;
 
 	/* package */final TileMap mTileMap;
 
 	LocationHandler(TileMap tileMap) {
 		mTileMap = tileMap;
 
 		mLocationManager = (LocationManager) tileMap
 				.getSystemService(Context.LOCATION_SERVICE);
 		mLocationListener = new MyLocationListener();
 
 		mSnapToLocationView = (ToggleButton) tileMap
 				.findViewById(R.id.snapToLocationView);
 
 		mSnapToLocationView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (isSnapToLocationEnabled()) {
 					disableSnapToLocation(true);
 				} else {
 					enableSnapToLocation(true);
 				}
 			}
 		});
 	}
 
 	@SuppressWarnings("deprecation")
 	boolean enableShowMyLocation(boolean centerAtFirstFix) {
 		Log.d("TileMap", "enableShowMyLocation " + mShowMyLocation);
 
 		gotoLastKnownPosition();
 
 		if (!mShowMyLocation) {
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			String bestProvider = mLocationManager.getBestProvider(criteria, true);
 
 			if (bestProvider == null) {
 				mTileMap.showDialog(DIALOG_LOCATION_PROVIDER_DISABLED);
 				return false;
 			}
 
 			mShowMyLocation = true;
 
 			Log.d("TileMap", "enableShowMyLocation " + mShowMyLocation);
 
 			mLocationListener.setFirstCenter(centerAtFirstFix);
 
 			mLocationManager.requestLocationUpdates(bestProvider, 1000, 0,
 					mLocationListener);
 
 			mSnapToLocationView.setVisibility(View.VISIBLE);
 
 			return true;
 		}
 		return false;
 	}
 
 	public void gotoLastKnownPosition() {
 		Location currentLocation = null;
 		Location bestLocation = null;
 
 		for (String provider : mLocationManager.getProviders(true)) {
 			currentLocation = mLocationManager.getLastKnownLocation(provider);
 			if (currentLocation == null)
 				continue;
 			if (bestLocation == null
 					|| currentLocation.getAccuracy() < bestLocation.getAccuracy()) {
 				bestLocation = currentLocation;
 			}
 		}
 
 		if (bestLocation != null) {
 			//byte zoom = mTileMap.map.getMapPosition().getZoomLevel();
 			//if (zoom < 12)
 			byte zoom = (byte) 12;
 
 			MapPosition mapPosition = new MapPosition(bestLocation.getLatitude(),
 					bestLocation.getLongitude(), zoom, 1, 0);
 
 			
 			
 			if(App.map.getOverlays().size() >2){
 			App.map.getOverlays().remove(2);
 			App.map.getOverlays().remove(1);
 			}
 			
 			
 			if(bestLocation == null && currentLocation == null ){ // no location has been accuqired 
 			Toast.makeText(App.map.getContext(), "No location info avaible", Toast.LENGTH_SHORT).show();
 				
 				
 				return ;
 				
 				
 			}
 			DrawableOverlay l =  new  DrawableOverlay(App.map);
 	
			if( currentLocation == null ){ // no location has been accuqired 
				Toast.makeText(App.map.getContext(), "No location info avaible", Toast.LENGTH_SHORT).show();
 			
 	l.setLat(((float)mapPosition.lat));
 	l.setLog((float)mapPosition.lon);
 	
 	
 	
 	// calculation related to offset the lat/log  ... make it general function 
 	
 	double ground=	MercatorProjection.calculateGroundResolution(App.map.getMapPosition().getMapCenter().getLatitude(), App.map.getMapPosition().getMapPosition().zoomLevel);
 	
 	double  raduisInPixle = ((double) currentLocation.getAccuracy()) /ground;
 
 	double latt = MercatorProjection.latitudeToPixelY(App.map.getMapPosition().getMapCenter().getLatitude()	, App.map.getMapPosition().getMapPosition().zoomLevel);
 	
 	//Toast.makeText(App.activity,"raduis in pixle"+ String.valueOf(latt), Toast.LENGTH_LONG).show();
 	
 	CircleOverlay ts = new CircleOverlay (App.map,(float)currentLocation.getAccuracy()  );
 	
 	// ts.setRaduis((float) currentLocation.getAccuracy());
 	App.map.getOverlays().add(1,ts );
 	
 	//App.map.getOverlays().add(new GenericOverlay ( App.map , new TileOverlay(App.map)));
 	//l.setAccuracy(currentLocation.getAccuracy());
 //	Toast.makeText(App.activity,"raduis of : " + String.valueOf(currentLocation.getAccuracy()), 400).show();
 			
 	//l.onUpdate(mapPosition, true);
   App.map.getOverlays().add(2,l);
   
 			App.map.setMapCenter(mapPosition);
 
 		} else {
 			mTileMap.showToastOnUiThread(mTileMap
 					.getString(R.string.error_last_location_unknown));
 		}
 		
 	}
 
 	/**
 	 * Disables the "show my location" mode.
 	 * @return ...
 	 */
 	boolean disableShowMyLocation() {
 		if (mShowMyLocation) {
 			mShowMyLocation = false;
 			disableSnapToLocation(false);
 			
 			if(App.map.getOverlays().size() >2){
 App.map.getOverlays().remove(2);
 App.map.getOverlays().remove(1);
 			}
 App.map.redrawMap(true);
 			mLocationManager.removeUpdates(mLocationListener);
 			// if (circleOverlay != null) {
 			// mapView.getOverlays().remove(circleOverlay);
 			// mapView.getOverlays().remove(itemizedOverlay);
 			// circleOverlay = null;
 			// itemizedOverlay = null;
 			// }
 
 			mSnapToLocationView.setVisibility(View.GONE);
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the status of the "show my location" mode.
 	 * @return true if the "show my location" mode is enabled, false otherwise.
 	 */
 	boolean isShowMyLocationEnabled() {
 		return mShowMyLocation;
 	}
 
 	/**
 	 * Disables the "snap to location" mode.
 	 * @param showToast
 	 *            defines whether a toast message is displayed or not.
 	 */
 	void disableSnapToLocation(boolean showToast) {
 		if (mSnapToLocation) {
 			mSnapToLocation = false;
 			mSnapToLocationView.setChecked(false);
 
 			App.map.setClickable(true);
 
 			if (showToast) {
 				mTileMap.showToastOnUiThread(mTileMap
 						.getString(R.string.snap_to_location_disabled));
 			}
 		}
 	}
 
 	/**
 	 * Enables the "snap to location" mode.
 	 * @param showToast
 	 *            defines whether a toast message is displayed or not.
 	 */
 	void enableSnapToLocation(boolean showToast) {
 		if (!mSnapToLocation) {
 			mSnapToLocation = true;
 
 			App.map.setClickable(false);
 
 			if (showToast) {
 				mTileMap.showToastOnUiThread(mTileMap
 						.getString(R.string.snap_to_location_enabled));
 			}
 		}
 	}
 
 	/**
 	 * Returns the status of the "snap to location" mode.
 	 * @return true if the "snap to location" mode is enabled, false otherwise.
 	 */
 	boolean isSnapToLocationEnabled() {
 		return mSnapToLocation;
 	}
 
 	class MyLocationListener implements LocationListener {
 
 		private boolean mSetCenter;
 
 		@Override
 		public void onLocationChanged(Location location) {
 
 			Log.d("LocationListener", "onLocationChanged, "
 					+ " lon:" + location.getLongitude()
 					+ " lat:" + location.getLatitude());
 
 			if (!isShowMyLocationEnabled()) {
 				return;
 			}
 
 			GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
 
 			// this.advancedMapViewer.overlayCircle.setCircleData(point, location.getAccuracy());
 			// this.advancedMapViewer.overlayItem.setPoint(point);
 			// this.advancedMapViewer.circleOverlay.requestRedraw();
 			// this.advancedMapViewer.itemizedOverlay.requestRedraw();
 
 			if (mSetCenter || isSnapToLocationEnabled()) {
 				mSetCenter = false;
 				App.map.setCenter(point);
 			}
 			if(mSnapToLocation)
 				gotoLastKnownPosition();
 			//gotoLastKnownPosition();
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			// do nothing
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// do nothing
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// do nothing
 		}
 
 		boolean isFirstCenter() {
 			return mSetCenter;
 		}
 
 		void setFirstCenter(boolean center) {
 			mSetCenter = center;
 		}
 	}
 }
