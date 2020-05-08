 package edu.aau.utzon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.widget.SearchView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.readystatesoftware.maps.TapControlledMapView;
 import com.readystatesoftware.maps.OnSingleTapListener;
 
 
 import edu.aau.utzon.WebserviceActivity.RestContentObserver;
 import edu.aau.utzon.indoor.IndoorActivity;
 import edu.aau.utzon.location.LocationHelper;
 import edu.aau.utzon.location.NearPoiPublisher;
 import edu.aau.utzon.webservice.PointModel;
 import edu.aau.utzon.webservice.ProviderContract;
 import edu.aau.utzon.webservice.RestServiceHelper;
 
 public class OutdoorActivity extends SherlockMapActivity implements NearPoiPublisher {
 
 	private LocationHelper mLocationHelper;
 	private ArrayList<PointModel> mOutdoorPois;
 	private TapControlledMapView mMapView;
 	private MyLocationOverlay mMyLocationOverlay;
 
 	public final static  String[] mProjectionAll = {ProviderContract.Points.ATTRIBUTE_ID, 
 		ProviderContract.Points.ATTRIBUTE_X, 
 		ProviderContract.Points.ATTRIBUTE_Y, 
 		ProviderContract.Points.ATTRIBUTE_DESCRIPTION};
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		mMyLocationOverlay.enableMyLocation();
 		mLocationHelper.onResume();
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		mMyLocationOverlay.disableMyLocation();
 		mLocationHelper.onPause();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
		//Intent intent = new Intent(this, IndoorActivity.class);
		//startActivity(intent);
 		
 		super.onCreate(savedInstanceState);
 		// Init locationHelper
 		mOutdoorPois = new ArrayList<PointModel>();
 		this.mLocationHelper = new LocationHelper(getApplicationContext());
 		this.mLocationHelper.setNearPoiPublisher(this);
 		mLocationHelper.onCreate(savedInstanceState);
 
 		// Remove title bar
 		if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ) {
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		}
 
 		// Display Google maps to the user
 		setContentView(R.layout.mapview);
 
 		// Enable built-in map controls
 		mMapView = (TapControlledMapView) findViewById(R.id.mapview);
 		mMapView.setBuiltInZoomControls(true);
 
 
 		// Draw the user position on map
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 		mMyLocationOverlay.enableMyLocation();
 		mMapView.getOverlays().add(mMyLocationOverlay);
 
 		registerContentObserver();
 		getAllOutdoorPois();
 
 		// Do fancy fancy animation to our current position :P
 		//animateToLocation(mLocTool.getCurrentLocation());
 	}
 	
 	public void userIsNearPoi(PointModel poi) {
 		Log.e("TACO", "IS NEAR POI");
 		StartPoiContentActivity();
 	}
 	
 	private void StartPoiContentActivity() {
 		startActivity(new Intent(getApplicationContext(), PoiContentActivity.class));
 	}
 	
 	public ArrayList<PointModel> getPois() {
 		return mOutdoorPois;
 	}
 
 	private void getAllOutdoorPois() {
 		RestServiceHelper.getServiceHelper()
 		.getLocationPoints(this);
 	}
 
 	private void registerContentObserver() {
 		RestContentObserver mContentObserver = new RestContentObserver(new Handler());
 		this.getApplicationContext()
 		.getContentResolver()
 		.registerContentObserver(ProviderContract.Points.CONTENT_URI, true, mContentObserver);
 	}
 
 	protected void animateToLocation(Location loc)
 	{
 		// Only animate if its a valid location
 		if(loc != null){
 			// Display current position on map
 			//MapView mapView = (MapView) findViewById(R.id.mapview);
 			MapController mc = mMapView.getController();
 
 			GeoPoint point =  LocationHelper.locToGeo(loc);
 			mc.animateTo(point);
 		}
 	}
 
 	public void updateOutdoorPois(ArrayList<PointModel> pois) {
 		mOutdoorPois = pois;
 		drawOutdoorPois();
 	}
 
 	protected void drawOutdoorPois()
 	{
 		//MapView mapView = (MapView) findViewById(R.id.mapview);
 
 		// Setup overlays
 		List<Overlay> mapOverlays = mMapView.getOverlays();
 		mapOverlays.clear();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
 		final BalloonOverlay itemizedoverlay = new BalloonOverlay(drawable, mMapView);
 		//		GMapsOverlay itemizedover)lay = new GMapsOverlay(drawable, this);
 
 		// Add POI to the overlay
 		for(PointModel p : mOutdoorPois)
 		{
 			itemizedoverlay.addOverlay(new OverlayItem(p.mGeoPoint, "Title", p.mDesc));
 		}
 
 		// Ballon stuff
 		mMapView.setOnSingleTapListener(new OnSingleTapListener() {		
 			@Override
 			public boolean onSingleTap(MotionEvent e) {
 				itemizedoverlay.hideAllBalloons();
 				return true;
 			}
 		});
 
 		// set iOS behavior attributes for overlay (?)
 		itemizedoverlay.setShowClose(false);
 		itemizedoverlay.setShowDisclosure(true);
 		itemizedoverlay.setSnapToCenter(false);
 
 		mapOverlays.add(itemizedoverlay);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.layout.menu_outdoor, menu);
 
 		MenuItem searchItem = menu.findItem(R.id.actionbar_search);
 
 		return true;
 	}
 
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.actionbar_center_location:
 			animateToLocation(mLocationHelper.getCurrentLocation());
 			return true;
 		case R.id.actionbar_poi_list:
 			Intent poiListIntent = new Intent(this, PoiListActivity.class);
 			poiListIntent.putExtra("pois", mOutdoorPois);
 			startActivity(poiListIntent);
 			return true;
 		case R.id.actionbar_search:
 			//
 			onSearchRequested();
 			return true;
 		case R.id.actionbar_augmented:
 			Intent augmentedIntent = new Intent(this, AugmentedActivity.class);
 			augmentedIntent.putExtra("pois", mOutdoorPois);
 			startActivity(augmentedIntent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	class RestContentObserver extends ContentObserver{
 		public RestContentObserver(Handler handler) {
 			super(handler);
 		}
 
 		@Override
 		public boolean deliverSelfNotifications() {
 			return true;
 		}
 
 		@Override
 		public void onChange(boolean selfChange) {
 			super.onChange(selfChange);
 			// New content is available
 			Cursor c = managedQuery(ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
 					mProjectionAll,                        	// The columns to return for each row
 					null,                    				// Selection criteria
 					null,                     				// Selection criteria
 					null);                        			// The sort order for the returned rows
 
 			ArrayList<PointModel> points = new ArrayList<PointModel>();
 
 			c.moveToFirst();
 			do
 			{
 				int colIndexId = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_ID);
 				int colIndexDesc = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_DESCRIPTION);
 				int colIndexX = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_X);
 				int colIndexY = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_Y);
 
 				int id = c.getInt(colIndexId);
 				String desc = c.getString(colIndexDesc);
 				float x = c.getFloat(colIndexX);
 				float y = c.getFloat(colIndexY);
 
 				PointModel p = new PointModel();
 				p.mDesc = desc;
 				p.mId = id;
 				p.mGeoPoint = new GeoPoint((int)x,(int)y);
 
 				points.add(p);
 
 			} while (c.moveToNext() == true);
 
 			updateOutdoorPois(points);
 
 			Log.e("TACO", "Cos them hoes is bitches!");
 		}
 	}
 }
