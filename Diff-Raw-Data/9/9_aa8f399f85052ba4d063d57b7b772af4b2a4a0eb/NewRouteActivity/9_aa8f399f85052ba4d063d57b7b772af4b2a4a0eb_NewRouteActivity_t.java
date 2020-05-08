 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 */
 package se.team05.activity;
 
 import java.util.ArrayList;
 
 import se.team05.R;
 import se.team05.content.Routes;
 import se.team05.overlay.RouteOverlay;
 import se.team05.view.EditRouteMapView;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 public class NewRouteActivity extends MapActivity implements LocationListener, View.OnClickListener
 {
 
 	private ArrayList<GeoPoint> route;
 	private LocationManager locationManager;
 	private String providerName;
 	private int routeIdTag;
 	private EditRouteMapView mapView;
 	private boolean started = false;
 	private MyLocationOverlay myLocationOverlay;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_while_running);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		routeIdTag = Routes.getInstance().getCount();
 		route = new ArrayList<GeoPoint>();
 
 		System.out.println("IDTAG:::::::::::::::" + routeIdTag);
 
 		Button runButton = (Button) findViewById(R.id.stop_and_save_button);
 		runButton.setOnClickListener(this);
 
 		Button startRunButton = (Button) findViewById(R.id.start_run_button);
 		startRunButton.setOnClickListener(this);
 		
 		Button addCheckPointButton = (Button) findViewById(R.id.add_checkpoint);
 		addCheckPointButton.setOnClickListener(this);
 
 		mapView = (EditRouteMapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setMapActivity(this);
 		
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
 		//TODO Maybe move this ?!
 		
 		
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setCostAllowed(false);
 
 		providerName = locationManager.getBestProvider(criteria, true);
 
 		if (providerName != null)
 		{
 			System.out.println("NO PROVIDER:" + providerName);
 		}
 
 		RouteOverlay routeOverlay = new RouteOverlay(route, 78, true);
 
 		mapView.getOverlays().add(routeOverlay);
 		
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 		
 		mapView.postInvalidate();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void onLocationChanged(Location location)
 	{
 
 		if (started)
 		{
 			GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
 			route.add(p);
 			mapView.postInvalidate();
 		}
 	}
 	
 	protected void onResume() {
 		super.onResume();
 		// when our activity resumes, we want to register for location updates
 		myLocationOverlay.enableMyLocation();
 	}
 
 	protected void onPause() {
 		super.onPause();
 		// when our activity pauses, we want to remove listening for location
 		// updates
 		myLocationOverlay.disableMyLocation();
 	}
 	
 	public void onProviderDisabled(String provider)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onProviderEnabled(String provider)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	public ArrayList<GeoPoint> getRoute()
 	{
 		return route;
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.start_run_button:
 				started = true;
 				break;
 
 			case R.id.stop_and_save_button:
 				Routes.getInstance().addNewRoute(route, 1);
 				Intent intent = new Intent(this, MainActivity.class);
 				this.startActivity(intent);
 				break;
 			case R.id.add_checkpoint:
 				if(myLocationOverlay.isMyLocationEnabled())
				{
					GeoPoint geoPoint = myLocationOverlay.getMyLocation();
					if(geoPoint!=null)
						{
							mapView.setCheckPoint(geoPoint);
						}
				}
				
 				break;
 			default:
 				break;
 
 		}
 		
 		
 	}
 	//TODO delete or edit ? temporarily to grant CheckpointOverlay access to update the mapview
 	public EditRouteMapView getMapView()
 	{
 		return mapView;
 	}
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu) {
 	// getMenuInflater().inflate(R.menu.activity_while_running, menu);
 	// return true;
 	// }
 	//
 	//
 	// @Override
 	// public boolean onOptionsItemSelected(MenuItem item) {
 	// switch (item.getItemId()) {
 	// case android.R.id.home:
 	// NavUtils.navigateUpFromSameTask(this);
 	// return true;
 	// }
 	// return super.onOptionsItemSelected(item);
 	// }
 
 }
