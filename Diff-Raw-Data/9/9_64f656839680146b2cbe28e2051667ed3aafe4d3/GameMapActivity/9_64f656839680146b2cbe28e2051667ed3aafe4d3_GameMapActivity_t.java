 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of GenericAndroidClient.
  *
  *  GenericAndroidClient is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  GenericAndroidClient is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package uk.ac.horizon.ug.exploding.client;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import uk.ac.horizon.ug.exploding.client.model.Member;
 import uk.ac.horizon.ug.exploding.client.model.Message;
 import uk.ac.horizon.ug.exploding.client.model.Player;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.animation.BounceInterpolator;
 import android.widget.Toast;
 
 /**
  * @author cmg
  *
  */
 public class GameMapActivity extends MapActivity implements ClientStateListener {
 
 	private static final String TAG = "Map";
 	private static final int MILLION = 1000000;
 	private static final int MIN_ZOOM_LEVEL = 14;
 	private MyLocationOverlay myLocationOverlay;
 	private MyMapOverlay itemOverlay;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		try {
 			Log.d(TAG, "Try to load map view");
 			setContentView(R.layout.map);
 			MapView mapView = (MapView)findViewById(R.id.map_view);
 			mapView.setBuiltInZoomControls(true);
 			myLocationOverlay = new MyLocationOverlay(this, mapView);
 			mapView.getOverlays().add(myLocationOverlay);
 			myLocationOverlay.runOnFirstFix(new Runnable() {
 				public void run() {
 					centreOnMyLocation();
 				}
 			});
 			Resources res = getResources();
 			Drawable drawable = res.getDrawable(R.drawable.icon/*android.R.drawable.btn_star*/);
 			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
 			Log.d(TAG,"defaultDrawable="+drawable);
 			ClientState clientState = BackgroundThread.getClientState(this);
 			itemOverlay = new MyMapOverlay(drawable, clientState);
 			BackgroundThread.addClientStateListener(itemOverlay, this, Member.class.getName());
 			mapView.getOverlays().add(itemOverlay);
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Error loading map view: "+e);
 		}
 		Set<String> types = new HashSet<String>();
 		types.add(Message.class.getName());
 		BackgroundThread.addClientStateListener(this, this, ClientState.Part.ZONE.flag(), types);
 		ClientState clientState = BackgroundThread.getClientState(this);
 		clientStateChanged(clientState);
 	}
 	private static final long ZONE_VIBRATE_MS = 500;
 
 	@Override
 	public void clientStateChanged(final ClientState clientState) {
 		if (clientState==null)
 			return;
 		if (clientState.isZoneChanged())
 			zoneChanged(clientState.getZoneID());
 		handleMessages(clientState);
 	}
 
 	/**
 	 * @param zoneID
 	 */
 	protected void zoneChanged(String zoneID) {
 		Log.d(TAG, "Zone change to "+zoneID);
 		if (zoneID!=null) {
 			Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
 			if (vibrator!=null)
 				vibrator.vibrate(ZONE_VIBRATE_MS);
 			Toast.makeText(GameMapActivity.this, "Entered zone "+zoneID, Toast.LENGTH_SHORT).show();
 		}
 	}		
 	/**
 	 * @param clientState
 	 */
 	private void handleMessages(ClientState clientState) {
 		if (clientState==null || clientState.getCache()==null) 
 			return;
 		List<Object> messages = clientState.getCache().getFacts(Message.class.getName());
 		if (messages.size()==0)
 			return;
 		Log.d(TAG,"Messages: "+messages.size());
 		
 		for (Object m : messages) {
 			Message message = (Message)m;
 			NotificationUtils.postMessage(this, message);
 			clientState.getCache().removeFactSilent(message);
 		}
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();    
     	inflater.inflate(R.menu.map_menu, menu);    
     	return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.map_my_location:
 			centreOnMyLocation();
 			return true;
 		case R.id.map_menu_gps:
 		{
 			Intent intent = new Intent();
 			intent.setClass(this, GpsStatusActivity.class);
 			startActivity(intent);
 			return true;
 		}			
 		case R.id.map_menu_create_member:
 		{
 			// check if we can...
 			Player player = getPlayer();
			// Oops - this is for events
			//if (player==null || !player.isSetCanAuthor() || !player.getCanAuthor()) {
			//	Toast.makeText(this, "You cannot author yet - keep playing", Toast.LENGTH_LONG).show();
			//	return true;
			//}
			if (player==null || !player.isSetNewMemberQuota() || player.getNewMemberQuota()<1) {
				Toast.makeText(this, "You cannot create a member yet - keep playing", Toast.LENGTH_LONG).show();
 				return true;
 			}
 			Intent intent = new Intent();
 			intent.setClass(this, CreateMemberActivity.class);
 			startActivity(intent);
 			return true;
 		}						
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * @return
 	 */
 	private Player getPlayer() {
 		Client cache = BackgroundThread.getClientState(this).getCache();
 		if (cache==null)
 			return null;
 		List<Object> players = cache.getFacts(Player.class.getName());
 		if (players.size()==0)
 			return null;
 		return (Player)players.get(0);
 	}
 
 	private void centreOnMyLocation() {
 		try {
 			Location loc = LocationUtils.getCurrentLocation(this);
 			if (loc!=null) {
 				MapView mapView = (MapView)findViewById(R.id.map_view);
 				MapController controller = mapView.getController();
 				int zoomLevel = mapView.getZoomLevel();
 				// zoom Level 15 is about 1000m on a side
 				if (zoomLevel < MIN_ZOOM_LEVEL)
 					controller.setZoom(MIN_ZOOM_LEVEL);
 				GeoPoint point = new GeoPoint((int)(loc.getLatitude()*MILLION), (int)(loc.getLongitude()*MILLION));
 				controller.animateTo(point);
 			}
 			else
 			{
 				Toast.makeText(this, "Current location unknown", Toast.LENGTH_SHORT).show();
 			}
 		}catch (Exception e) {
 			Log.e(TAG, "doing centreOnMyLocation", e);
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		//LocationUtils.unregisterOnThread(this, this, null);
 		myLocationOverlay.disableCompass();
 		myLocationOverlay.disableMyLocation();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		myLocationOverlay.enableCompass();
 		myLocationOverlay.enableMyLocation();
 //		LocationUtils.registerOnThread(this, this, null);
 //		centreOnMyLocation();
 	}		
 //	@Override
 //	public void onLocationChanged(Location location) {
 //		// TODO Auto-generated method stub
 //		
 //	}
 //
 //	@Override
 //	public void onProviderDisabled(String provider) {
 //		// TODO Auto-generated method stub
 //		
 //	}
 //
 //	@Override
 //	public void onProviderEnabled(String provider) {
 //		// TODO Auto-generated method stub
 //		
 //	}
 //
 //	@Override
 //	public void onStatusChanged(String provider, int status, Bundle extras) {
 //		// TODO Auto-generated method stub
 //		
 //	}
 
 }
