 package edu.pitt.designs1635.ParkIt;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import edu.pitt.designs1635.ParkIt.ParkingLocationItemizedOverlay.BalloonTouchListener;
 import edu.pitt.designs1635.ParkIt.Directions.DrivingDirections;
 import edu.pitt.designs1635.ParkIt.Directions.DrivingDirections.IDirectionsListener;
 import edu.pitt.designs1635.ParkIt.Directions.DrivingDirections.Mode;
 import edu.pitt.designs1635.ParkIt.Directions.DrivingDirectionsFactory;
 import edu.pitt.designs1635.ParkIt.Directions.Route;
 import edu.pitt.designs1635.ParkIt.Directions.RouteOverlay;
 
 public class ParkItActivity extends SherlockMapActivity implements LocationListener
 {
 	private dbAdapter mDbHelper;
 	private Cursor mCursor;
 	private MapView mapView;
 	private ParkingLocationItemizedOverlay gItemizedOverlay, lItemizedOverlay, mItemizedOverlay;
 	private CurrentLocationOverlay cLocationOverlay;
 	private Drawable drawable;
 	private SharedPreferences prefs;
 	private MapController mapCtrl;
 	LocationManager mlocManager;
 	Location lastKnownLocation;
 	Double latitude, longitude;
 	ActionMode mMode;
 	private Criteria criteria;
 	private GeoPoint p;
 	private RouteOverlay m_route;
 
 	public static final int INFORMATION_ACTIVITY = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.main);
 
 		setSupportProgressBarIndeterminateVisibility(false);
 
 		prefs = this.getSharedPreferences("parkItPrefs", Activity.MODE_PRIVATE);
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapCtrl = mapView.getController();
 
 		criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
 
 		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, this);
 
 		updateLocation(getCurrentLocation());
 
 		if(!prefs.getBoolean("tutorial", false))
 		{
 			promptTutorial();
 		}
 
 		mDbHelper = new dbAdapter(this);
 		mDbHelper.open();
 		mCursor = mDbHelper.fetchAllRows();
 		startManagingCursor(mCursor);
 		mCursor.moveToFirst();
 
 		getRemotePoints();
 
 		mapCtrl.setZoom(17);
 	}
 
 	public void promptTutorial()
 	{
 		AlertDialog.Builder ed = new AlertDialog.Builder(this);
 		ed.setIcon(R.drawable.ic_launcher);
 		ed.setTitle("First Time User");
 		ed.setView(LayoutInflater.from(this).inflate(R.layout.tut_dialog,null));
 
 		SharedPreferences.Editor editor = prefs.edit();
 			editor.putBoolean("tutorial", true);
 			editor.commit();
 
 		ed.setPositiveButton("Yes",
 		new android.content.DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int arg1) {
 			startActivity(new Intent(getApplicationContext(), Tutorial.class));
 			}
 		});
 
 		ed.setNegativeButton("No",
 		new android.content.DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int arg1) {
 			}
 		});
 		ed.show();
 	}
 
 	@Override
 	protected void onStop()
 	{
 		super.onStop();
 		mlocManager.removeUpdates(this);
 		if(mDbHelper != null)
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		mDbHelper.open();
 		refreshAllPoints();
         
         // Check is GPS is on.  Show alert if off.
         if (! isGPSAvailable()) {
         	showAlertMessageNoGPS();
         }
         
         // Check for Internet connection on startup.
         if (! isNetworkAvailable()) {
         	showAlertMessageNoInternets();
         } 
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 		mlocManager.removeUpdates(this);
 		if(mDbHelper != null)
 			mDbHelper.close();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.menu_add:
 				SharedPreferences.Editor editor = prefs.edit();
 				editor.putInt("last_location_lat", mapView.getMapCenter().getLatitudeE6());
 				editor.putInt("last_location_lon", mapView.getMapCenter().getLongitudeE6());
 				editor.commit();
 				mDbHelper.close();
 				startActivity(new Intent(this, Add.class));
 				return true;
 			case R.id.menu_refresh:
 				getRemotePoints();
 				updateLocation(getCurrentLocation());
 				return true;
 			case R.id.menu_alarm:
 				startActivity(new Intent(this, Timer.class));
 				return true;
 			case R.id.menu_settings:
 				//mDbHelper.abandonShip();
 				//refreshAllPoints();
 				return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			ActionBar ab = getSupportActionBar();
 			ab.setTitle("ParkIt");
 			List<Overlay> points = mapView.getOverlays();
 			
 			if(m_route != null)
 				points.remove(m_route);
 			mapView.invalidate();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);	
 		}
 	}
 
 	public void getRemotePoints()
 	{
 		setSupportProgressBarIndeterminateVisibility(true);
 		ActionBar ab = getSupportActionBar();
 		ab.setTitle("Refreshing Points...");
 
 		Parse.initialize(this, "pAtl7R7WUbPl3RIVMD9Ov8UDVODGYSJ9tImxKTPQ", "cgjq64nO8l5RVbmrqYH3Nv2VC1zPyX4904htpXPy");
 		ParseQuery query = new ParseQuery("Points");
 		query.findInBackground(new FindCallback() {
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null) {
 					pointsProcessing(objects);
 				}
 			}
 		});
 
 		mapView.invalidate();
 		ab.setTitle("ParkIt");
 		setSupportProgressBarIndeterminateVisibility(false);
 	}
 
 	public void pointsProcessing(List<ParseObject> objs)
 	{
 		for(int i=0; i < objs.size(); i++)
 		{
 			try{
 				mDbHelper.addPoint(objs.get(i));
 			}catch(Exception e){}
 		}
 		refreshAllPoints();
 	}
 
 	public void refreshAllPoints()
 	{
 		mCursor = mDbHelper.fetchAllRows();
 		startManagingCursor(mCursor);
 		mCursor.moveToFirst();
 		drawable = getResources().getDrawable(R.drawable.g_icon);
 		gItemizedOverlay = new ParkingLocationItemizedOverlay(drawable, mapView);
 		drawable = getResources().getDrawable(R.drawable.l_icon);
 		lItemizedOverlay = new ParkingLocationItemizedOverlay(drawable, mapView);
 		drawable = getResources().getDrawable(R.drawable.m_icon);
 		mItemizedOverlay = new ParkingLocationItemizedOverlay(drawable, mapView);
 
 		cLocationOverlay = new CurrentLocationOverlay(getCurrentLocation());
 
 		gItemizedOverlay.hideAllBalloons();
 		lItemizedOverlay.hideAllBalloons();
 		mItemizedOverlay.hideAllBalloons();
 
 		gItemizedOverlay.setBalloonTouchListener(new MyBalloonTouchListener());
 		lItemizedOverlay.setBalloonTouchListener(new MyBalloonTouchListener());
 		mItemizedOverlay.setBalloonTouchListener(new MyBalloonTouchListener());
 
 
 		OverlayItem overlayItem;
 		GeoPoint point;
 
 		//Will take the cursor (contains every record in the db) and iterate through adding each point to the appropriate overlay
 		if(mCursor.getCount() > 0)
 		{
 			do{
 				ParkingLocation pl = new ParkingLocation(mCursor.getInt(1), mCursor.getInt(2));
 
 				pl.setName(mCursor.getString(4));
 				pl.setRate(mCursor.getFloat(8));
 				pl.setType(mCursor.getInt(3));
 				pl.setPayment(mCursor.getInt(5));
 				pl.setLimit(mCursor.getInt(6));
 
 				if(mCursor.getInt(3) == 0)
 					mItemizedOverlay.addOverlay(pl);
 				else if(mCursor.getInt(3) == 1)
 					lItemizedOverlay.addOverlay(pl);
 				else
 					gItemizedOverlay.addOverlay(pl);
 				mCursor.moveToNext();
 			}while(!mCursor.isAfterLast());
 		}
 
 
 		List<Overlay> points = mapView.getOverlays();
 		points.clear();
 		points.add(gItemizedOverlay);
 		points.add(lItemizedOverlay);
 		points.add(mItemizedOverlay);
 		points.add(cLocationOverlay);
 	}
 
 	public void onLocationChanged(Location location) {
 		//updateLocation(location);
 	}
 
 	private void updateLocation(GeoPoint p)
 	{
 		mapCtrl.animateTo(p);
 	}
 
 	private GeoPoint getCurrentLocation()
 	{
 		List<String> providers = mlocManager.getProviders(true);
 		Location location = null;
 		int i = providers.size();
 		for( i = providers.size()-1; i>=0; i--)
 		{
 			//if( i == 0 ) break;
 			location = mlocManager.getLastKnownLocation( providers.get(i) );
 			if(location != null )
 			   break;
 		}
 		GeoPoint p = null;
 
 		Double lat = location.getLatitude()*1E6;
 		Double lng = location.getLongitude()*1E6;
 		p = new GeoPoint(lat.intValue(), lng.intValue());
 
 		return p;
 	}
 
 	public void onProviderDisabled(String provider) {
 		// required for interface, not used
 	}
 
 	public void onProviderEnabled(String provider) {
 		// required for interface, not used
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// required for interface, not used
 	}
 
 	private class MyIDirectionsListener implements IDirectionsListener
 	{
 
 		@Override
 		public void onDirectionsAvailable(Route route, Mode mode) {
 			
 			List<Overlay> points = mapView.getOverlays();
 			
 			if(m_route != null)
 				points.remove(m_route);
 			
 			m_route = new RouteOverlay(route.getGeoPoints());
 			//RouteSegmentOverlay rso = new RouteSegmentOverlay(route.getGeoPoints().get(0), route.getGeoPoints().get(route.getGeoPoints().size() - 2));
 			points.add(m_route);
 
 			mMode = startActionMode(new RouteActionMode());
 			
 			//Log.i("MyIDirectionsListener", "Route achieved! Size = " + ro.size());
 			mapView.invalidate();
 			
 		}
 
 		@Override
 		public void onDirectionsNotAvailable() {
 			Log.i("MyIDirectionsListener", "No Route!!!!!!");
 			//TODO provide a warning to the user
 		}
 		
 	}
 
 	private class MyBalloonTouchListener implements BalloonTouchListener
 	{
 
 		@Override
 		public void onBalloonTap(ParkingLocation pl) {
 			Intent info = new Intent(ParkItActivity.this, Information.class);
 			
 			info.putExtra("edu.pitt.designs1635.ParkIt.location.info", pl);
 			mDbHelper.close();
 			startActivityForResult(info, INFORMATION_ACTIVITY);
 			
 		}
 
 		@Override
 		public void onNextClick(ParkingLocation pl) {
 			ActionBar ab = getSupportActionBar();
 			ab.setTitle("Loading Directions");
 			setSupportProgressBarIndeterminateVisibility(true);
 			getDirections(pl.getGeoPoint());
 		}
 		
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 
 		Log.i("ParkItActivity.onActivityResult", "Start");
 		if(data == null)
 			return;
 		
 		switch (requestCode)
 		{
 			case INFORMATION_ACTIVITY:
 				Bundle extras = data.getExtras();
 				Log.i("ParkItActivity.onActivityResult", "in INFORMATION_ACTIVITY");
 				
 				switch(resultCode)
 				{
 				case Information.GOTO_TRUE:
 
 					//Log.i("ParkItActivity.onActivityResult", "in GOTO_TRUE");
 					
 					ParkingLocation pl = extras.getParcelable("edu.pitt.designs1635.ParkIt.Information.info");
 					getDirections(pl.getGeoPoint());
 					
 					//DrivingDirections dir = DrivingDirectionsFactory.createDrivingDirections();
 					//dir.driveTo(p, pl.getGeoPoint(), Mode.DRIVING, new MyIDirectionsListener());
 					
 					break;
 					
 				case Information.GOTO_FALSE:
 					Log.i("ParkItActivity.onActivityResult", "in GOTO_FALSE");
 					break;
 				default:
 
 					Log.i("ParkItActivity.onActivityResult", "in default");
 					break;
 				}
 		
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void getDirections(GeoPoint destination)
 	{
		
 		DrivingDirections dir = DrivingDirectionsFactory.createDrivingDirections();
		dir.driveTo(getCurrentLocation(), destination, Mode.DRIVING, new MyIDirectionsListener());
 	}
 
 	private final class RouteActionMode implements ActionMode.Callback {
         @Override
         public boolean onCreateActionMode(ActionMode mode, Menu menu) {
             //Used to put dark icons on light action bar           
             menu.add("Clear Route")
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
             return true;
         }
 
         @Override
         public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
             return false;
         }
 
         @Override
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         	setSupportProgressBarIndeterminateVisibility(false);
             ActionBar ab = getSupportActionBar();
 			ab.setTitle("ParkIt");
 			List<Overlay> points = mapView.getOverlays();
 			
 			if(m_route != null)
 				points.remove(m_route);
 			mapView.invalidate();
 
             mode.finish();
             return true;
         }
 
         @Override
         public void onDestroyActionMode(ActionMode mode) {
         	setSupportProgressBarIndeterminateVisibility(false);
         	ActionBar ab = getSupportActionBar();
 			ab.setTitle("ParkIt");
 			List<Overlay> points = mapView.getOverlays();
 			
 			if(m_route != null)
 				points.remove(m_route);
 			mapView.invalidate();
         }
     }
 	
 	private boolean isNetworkAvailable() {
     	ConnectivityManager connectivityManager = 
     			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     	NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
     	return activeNetworkInfo != null;
     }
     
     private boolean isGPSAvailable() {
     	LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     	if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
     		//System.out.println("GPS is enabled");
     		//Toast.makeText(this, "GPS enabled", Toast.LENGTH_LONG).show();	
     		return true;	
     	} else {
     		//System.out.println("GPS is not enabled");
     		//Toast.makeText(this, "GPS not enabled", Toast.LENGTH_LONG).show();
     		return false;
     	}
 
     }
     
     private void showAlertMessageNoGPS() {
     	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setMessage("GPS is not enabled on this phone.  Do you want to enable it?")
     		.setCancelable(false)
     		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
     			public void onClick(final DialogInterface dialog, final int id) {
     				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
     			}
     		})
     		.setNegativeButton("No", new DialogInterface.OnClickListener() {
     			public void onClick(final DialogInterface dialog, final int id) {
     				dialog.cancel();
     			}
     		});
     	final AlertDialog alert = builder.create();
     	alert.show();	
     }
     
     private void showAlertMessageNoInternets() {
     	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setMessage("No connection to the Internet detected.  Most features of this app require an active Internet connection.")
     		.setCancelable(false)
     		.setPositiveButton("Continue Anyway", new DialogInterface.OnClickListener() {
     			public void onClick(final DialogInterface dialog, final int id) {
     				dialog.cancel();
     			}
     		})
     		.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
     			public void onClick(final DialogInterface dialog, final int id) {
     				finish();
     			}
     		});
     	final AlertDialog alert = builder.create();
     	alert.show();	
     }
 }
 
