 package se.chalmers.krogkollen.map;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.animation.LinearInterpolator;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.model.*;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.detailed.DetailedActivity;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 import se.chalmers.krogkollen.utils.ActivityID;
 import se.chalmers.krogkollen.utils.IObserver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  */
 
  /**
  * The standard implementation of IMapView.
  *
  * This is a normal map with the user marked on the map, and with a list of pubs marked on the map.
  */
 public class MapActivity extends Activity implements IMapView, IObserver{
 
     /**
      * Identifier for the intent used to start the activity for detailed view.
      */
     public static final String MARKER_PUB_ID = "se.chalmers.krogkollen.MARKER_PUB_ID";
 
     private GoogleMap mMap;
 
     private UserLocation userLocation;
     private Marker userMarker;
     private List<Marker> pubMarkers = new ArrayList<Marker>();
 
     private Menu mainMenu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_map);
 		
 		// Get the map and add some markers for pubs.
         this.mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         this.addPubMarkers();
 
         mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
             @Override
             public boolean onMarkerClick(Marker marker) {
 
 
                 // Move camera to the clicked marker.
                 mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                         new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), 18, 0, 0)));
 
                if (!marker.getTitle().equalsIgnoreCase("user")) {
                     // Open favorites.
                 } else {
                     // Open detailed view.
                    openDetailedView(marker.getId());
                 }
                 return true; // Suppress default behavior; move camera and open info window.
             }
         });
 
         // Add services for auto update of the user's location.
         this.userLocation = UserLocation.getInstance();
         this.userLocation.addObserver(this);
         this.userLocation.startTrackingUser();
         addUserMarker(this.userLocation.getCurrentLatLng());
 
         // Move to the current location of the user.
         moveCameraToUser(16);
 
         ActionBar actionBar = getActionBar();
         //actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setIcon(R.drawable.list_icon);
         actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 
     private void openDetailedView(String id) {
         Intent detailedIntent = new Intent(this, DetailedActivity.class);
         detailedIntent.putExtra(MARKER_PUB_ID, id); // Sends the name of the pub with the intent
         startActivity(detailedIntent);
     }
 
     // Start the activity in a local method to keep the right context.
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
 		// Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.map, menu);
         mainMenu = menu;
 
 		return true;
 	}
 
     /**
      * Center the Google maps camera on the user.
      *
      * @param zoom how close to zoom in on the user.
      */
     private void moveCameraToUser(int zoom) {
         mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(this.userLocation.getCurrentLatLng(), zoom, 0, 0)));
     }
 
 	/**
 	 * Adds a user marker to the map.
 	 * 
 	 * @param latLng the user location
 	 */
 	public void addUserMarker(LatLng latLng){
 		userMarker = mMap.addMarker(new MarkerOptions()
 						.position(latLng)
 						.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker))
                         .title("user"));
  	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menuItem) {
         switch (menuItem.getItemId()) {
             case R.id.refresh_info:
 
                 AsyncTask<Void, Void, Void>  refreshLoader = new AsyncTask<Void, Void, Void>() {
                     @Override
                     protected Void doInBackground(Void... Voids) {
                         refreshPubMarkers();
                         return null;
                     }
 
                     @Override
                     protected void onPostExecute(Void result) {
                         mainMenu.getItem(R.id.refresh_info).setIcon(R.drawable.refresh_icon);
                     }
                 };
                 refreshLoader.execute();
                 mainMenu.getItem(R.id.refresh_info).setIcon(R.drawable.refresh_icon);
                 return true;
             case R.id.search:
                 // Open search
                 return true;
             case R.id.go_to_my_location:
                 moveCameraToUser(18);
                 return true;
             case android.R.id.home:
                 // Open list view
                 return true;
             default:
                 return super.onOptionsItemSelected(menuItem);
         }
     }
 	
 	/**
 	 * Adds markers to the map for all pubs in PubUtilities.
 	 */
     public void addPubMarkers() {
         for (int i = 0; i < PubUtilities.getInstance().getPubList().size(); i++) {
             addPubToMap(PubUtilities.getInstance().getPubList().get(i));
         }
     }
     
     /**
      * Removes all pub markers and adds them again with (new) information.
      */
     public void refreshPubMarkers() {
     	for(Marker pubMarker: this.pubMarkers){
     		pubMarker.remove();
     	}
     	this.pubMarkers.clear();
     	this.addPubMarkers();
     }
     
     @Override
 	public void update() {
 		this.animateMarker(this.userMarker, this.userLocation.getCurrentLatLng());
 	}
     
     /**
 	 * ** Method written by Google, found on stackoverflow.com **
 	 * ** http://stackoverflow.com/questions/13728041/move-markers-in-google-map-v2-android **
 	 * Moves the user marker smoothly to a new position.
 	 * 
 	 * @param marker	the marker that will be moved
 	 * @param toPosition	the position to where the marker will be moved
 	 */
 	private void animateMarker(final Marker marker, final LatLng toPosition) {
         final Handler handler = new Handler();
         final long start = SystemClock.uptimeMillis();
         Projection proj = this.mMap.getProjection();
         Point startPoint = proj.toScreenLocation(marker.getPosition());
         final LatLng startLatLng = proj.fromScreenLocation(startPoint);
         final long duration = 500;
 
         final LinearInterpolator interpolator = new LinearInterpolator();
 
         handler.post(new Runnable() {
             @Override
             public void run() {
                 long elapsed = SystemClock.uptimeMillis() - start;
                 float t = interpolator.getInterpolation((float) elapsed / duration);
                 double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                 double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                 marker.setPosition(new LatLng(lat, lng));
 
                 if (t < 1.0) {
                     // Post again 16ms later.
                     handler.postDelayed(this, 16);
                 }
             }
         });
     }
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		this.userLocation.stopTrackingUser();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.userLocation.startTrackingUser();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		int activity = this.getIntent().getIntExtra(ActivityID.ACTIVITY_ID, 0);
 		Intent intent;
 		switch(activity) {
 		case ActivityID.MAIN:
 			intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			startActivity(intent);
 			break;
 		case ActivityID.LIST:
 			//Intent intent = new Intent(this, ListActivity.class);
 			//intent.putExtra(CallingActivity.MAP);
 			//this.startActivity(intent);
 			break;
 		default:
 			intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			startActivity(intent);
 			break;
 		}
 	}
     
 	@Override
 	public void navigate(Class<?> destination) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addPubToMap(IPub pub) {
 
         int drawable;
         // Determine which marker color to add.
         switch (pub.getQueueTime()) {
             case 1:
                 drawable = R.drawable.green_marker_bg;
                 break;
             case 2:
                 drawable = R.drawable.yellow_marker_bg;
                 break;
             case 3:
                 drawable = R.drawable.red_marker_bg;
                 break;
             default:
                 drawable = R.drawable.gray_marker_bg;
                 break;
         }
         mMap.addMarker(MarkerOptionsFactory.createMarkerOptions(getResources(), drawable, pub.getName(), pub.getTodaysOpeningHour(),
                 new LatLng(pub.getLatitude(), pub.getLongitude()), pub.getID()));
 	}
 }
