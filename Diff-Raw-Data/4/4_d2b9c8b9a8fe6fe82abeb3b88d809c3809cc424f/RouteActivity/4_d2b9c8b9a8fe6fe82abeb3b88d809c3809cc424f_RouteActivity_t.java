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
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 package se.team05.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.team05.R;
 import se.team05.content.ParcelableGeoPoint;
 import se.team05.content.Result;
 import se.team05.content.Route;
 import se.team05.content.Track;
 import se.team05.data.DatabaseHandler;
 import se.team05.dialog.AlertDialogFactory;
 import se.team05.dialog.EditCheckPointDialog;
 import se.team05.dialog.SaveRouteDialog;
 import se.team05.listener.MapLocationListener;
 import se.team05.listener.MapOnGestureListener;
 import se.team05.listener.RouteActivityButtonListener;
 import se.team05.overlay.CheckPoint;
 import se.team05.overlay.CheckPointOverlay;
 import se.team05.overlay.RouteOverlay;
 import se.team05.util.Utils;
 import se.team05.view.EditRouteMapView;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.PowerManager.WakeLock;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 /**
  * The main use of this activity and of this application in general is that the
  * user is supposed to run a route of his or her choice and then be able to save
  * it. As the main idea is that a user will run a route, we will from here on
  * refer to this action as "running" or "exercise".
  * 
  * This activity Presents a map to the user. In the main menu the user gets the
  * choice of running a new route or an existing one he/she has saved from
  * earlier and thusly this activity will serve both functions. If the user
  * chooses to run a new run he/she will be presented with a map and the
  * possibility to record a new route. Recording will paint the path that the
  * user undertakes, as well as time distance and speed. The user can also place
  * checkpoints, which the user can use to activate music or sound at a given
  * location. Both checkpoints and paths is represented by geopoints. After
  * completion, the possibility to save this route will appear and the user gets
  * transferred to the start screen. If the user chooses an old route instead,
  * the old one will be painted in grey at the start and a new path in blue will
  * gradually get painted as the user moves along. When the user is done he or
  * she will instead be presented with the possibility to save the result
  * 
  * @author Markus Schutzer, Patrik Thitusson, Daniel Kvist
  */
 public class RouteActivity extends MapActivity implements EditCheckPointDialog.Callbacks, SaveRouteDialog.Callbacks,
 		CheckPointOverlay.Callbacks, MapOnGestureListener.Callbacks, MapLocationListener.Callbacks, Utils.Callbacks,
 		RouteActivityButtonListener.Callbacks
 {
 	private static final int USER_ROUTE_COLOR = 78;
 	private static final int RECORDED_ROUTE_COLOR = 10;
 	private static final String TAG = "Personal trainer";
 	private static final String BUNDLE_RID = "rid";
 	private static final String BUNDLE_CID = "cid";
 	private static final String BUNDLE_STARTED = "started";
 	private static final String BUNDLE_TIME_PASSED = "timePassed";
 	private static final String BUNDLE_TOTAL_DISTANCE = "totalDistance";
 	private static final String BUNDLE_GEOPOINT_LIST = "geoPointList";
 	private static final String BUNDLE_ACTIVE_DIALOG = "activeDialog";
 	private static final String BUNDLE_SAVE_RESULT_CHECKED = "isSaveResultChecked";
 	private static final int DIALOG_NONE = -1;
 	private static final int DIALOG_SAVE_ROUTE = 0;
 	private static final int DIALOG_SAVE_RESULT = 1;
 	private static final int DIALOG_CHECKPOINT = 2;
 
 	private List<Overlay> overlays;
 	private Route route;
 	private WakeLock wakeLock;
 	private MapLocationListener mapLocationListener;
 	private LocationManager locationManager;
 	private EditRouteMapView mapView;
 	private MyLocationOverlay myLocationOverlay;
 	private CheckPointOverlay checkPointOverlay;
 	private EditCheckPointDialog checkPointDialog;
 	private SaveRouteDialog saveRouteDialog;
 	private AlertDialog saveResultDialog;
 	private DatabaseHandler databaseHandler;
 	private CheckPoint currentCheckPoint;
 	private Button stopAndSaveButton;
 	private Button startButton;
 	private TextView speedView;
 	private TextView distanceView;
 	private TextView timeView;
 
 	/**
 	 * Will present a map to the user and will also display a dot representing
 	 * the user's location. Also contains three buttons of which one
 	 * (startRunButton) will start the recording of the user's movement and will
 	 * paint the track accordingly on the map. The button stopAndSaveButton will
 	 * finish the run and save it. As of now it is recorded in the memory but
 	 * later it will have database functionality. The button addCheckPointButton
 	 * will place a checkpoint at the user's current location.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		databaseHandler = new DatabaseHandler(this);
 		route = new Route(getString(R.string.new_route), getString(R.string.this_is_a_new_route));
 		wakeLock = Utils.acquireWakeLock(this);
 
 		setupMapView();
 		if (savedInstanceState != null)
 		{
 			restoreInstance(savedInstanceState);
 		}
 		else
 		{
 			route.setId(getIntent().getLongExtra(Route.EXTRA_ID, -1));
 		}
 
 		setupRouteAndCheckPoints();
 		setupTextViewsAndButtons();
 		setupMyLocationAndListener();
 		if (route.isStarted())
 		{
 			onStartRouteClick();
 		}
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * Sets up the mapview and fetches the overlays that are used to draw on the
 	 * map.
 	 */
 	private void setupMapView()
 	{
 		mapView = (EditRouteMapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setOnGestureListener(new MapOnGestureListener(this));
 		overlays = mapView.getOverlays();
 	}
 
 	/**
 	 * Sets up the checkpoint overlay and draws the route that the user has
 	 * recorded.
 	 */
 	private void setupRouteAndCheckPoints()
 	{
 		checkPointOverlay = new CheckPointOverlay(getResources().getDrawable(R.drawable.ic_launcher), this);
 		overlays.add(checkPointOverlay);
 		if (!route.isNewRoute())
 		{
 			route = databaseHandler.getRoute(route.getId());
 			RouteOverlay recordedRouteOverlay = new RouteOverlay(route.getGeoPoints(), RECORDED_ROUTE_COLOR);
 			overlays.add(recordedRouteOverlay);
 			checkPointOverlay.setCheckPoints(route.getCheckPoints());
 			setTitle(getString(R.string.saved_route_) + route.getName());
 		}
 	}
 
 	/**
 	 * Initiates the textviews and buttons in the view and adds listeners to
 	 * them.
 	 */
 	private void setupTextViewsAndButtons()
 	{
 		RouteActivityButtonListener clickListener = new RouteActivityButtonListener(this);
 		distanceView = (TextView) findViewById(R.id.show_distance_textview);
 		speedView = (TextView) findViewById(R.id.show_speed_textview);
 		timeView = (TextView) findViewById(R.id.show_time_textview);
 		Button addCheckPointButton = (Button) findViewById(R.id.add_checkpoint);
 		Button showResultButton = (Button) findViewById(R.id.show_result_button);
 		stopAndSaveButton = (Button) findViewById(R.id.stop_button);
 		startButton = (Button) findViewById(R.id.start_button);
 		stopAndSaveButton.setOnClickListener(clickListener);
 		startButton.setOnClickListener(clickListener);
 		if (route.isNewRoute())
 		{
 			addCheckPointButton.setOnClickListener(clickListener);
 		}
 		else
 		{
 			showResultButton.setOnClickListener(clickListener);
 			showResultButton.setVisibility(View.VISIBLE);
 			addCheckPointButton.setVisibility(View.GONE);
 		}
 	}
 
 	/**
 	 * Sets up the location manager, location lsitener and some criteria that
 	 * ask for the gps provider (fine). Then add overlay for my location and the
 	 * trace of the user's route.
 	 */
 	private void setupMyLocationAndListener()
 	{
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		mapLocationListener = new MapLocationListener(this, route.isNewRoute(), route.getCheckPoints());
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mapLocationListener);
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setCostAllowed(false);
 		String providerName = locationManager.getBestProvider(criteria, true);
 		if (providerName != null)
 		{
 			Log.d(TAG, getString(R.string.provider_) + providerName);
 		}
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		overlays.add(myLocationOverlay);
 		RouteOverlay userRouteOverlay = new RouteOverlay(route.getGeoPoints(), USER_ROUTE_COLOR);
 		overlays.add(userRouteOverlay);
 	}
 
 	/**
 	 * This method restores the instance after a configuration change has
 	 * happen. Important data is saved in the OnSavedInstanceState and contains
 	 * more data if a dialog is shown.
 	 * 
 	 * @param savedInstanceState
 	 */
 	private void restoreInstance(Bundle savedInstanceState)
 	{
 		route = databaseHandler.getRoute(savedInstanceState.getLong(BUNDLE_RID));
 		route.setTotalDistance(savedInstanceState.getFloat(BUNDLE_TOTAL_DISTANCE));
 		route.setTimePassed(savedInstanceState.getInt(BUNDLE_TIME_PASSED));
 		route.setStarted(savedInstanceState.getBoolean(BUNDLE_STARTED));
 
 		ArrayList<ParcelableGeoPoint> geoPoints = savedInstanceState.getParcelableArrayList(BUNDLE_GEOPOINT_LIST);
 		route.setGeoPoints(geoPoints);
 		RouteOverlay routeOverlay3 = new RouteOverlay(route.getGeoPoints(), USER_ROUTE_COLOR);
 		overlays.add(routeOverlay3);
 		if (route.isNewRoute())
 		{
 			checkPointOverlay.setCheckPoints(route.getCheckPoints());
 		}
 
 		int activeDialog = savedInstanceState.getInt(BUNDLE_ACTIVE_DIALOG);
 		switch (activeDialog)
 		{
 			case DIALOG_CHECKPOINT:
 				long checkPointId = savedInstanceState.getLong(BUNDLE_CID);
 				currentCheckPoint = databaseHandler.getCheckPoint(checkPointId);
 				showCheckPointDialog(currentCheckPoint, EditCheckPointDialog.MODE_EDIT);
 				break;
 			case DIALOG_SAVE_ROUTE:
 				showSaveRouteDialog();
 				saveRouteDialog.setSaveResultChecked(savedInstanceState.getBoolean(BUNDLE_SAVE_RESULT_CHECKED));
 				break;
 			case DIALOG_SAVE_RESULT:
 				showSaveResultDialog(route.getId());
 				break;
 		}
 	}
 
 	/**
 	 * Starts a new alert dialog which shows the distance and time and asks the
 	 * user if the results should be saved or not.
 	 * 
 	 * @param rid
 	 */
 	private void showSaveResultDialog(long rid)
 	{
 		Result result = new Result(rid, (int) System.currentTimeMillis() / 1000, route.getTimePassed(), (int) route.getTotalDistance(), 0);
 		saveResultDialog = AlertDialogFactory.newSaveResultDialog(this, route, result);
 		saveResultDialog.show();
 		route.setStarted(false);
 	}
 
 	/**
 	 * This is called when the user has selected a media from the media
 	 * selection activity. A list of tracks is then passed back as a result
 	 * which this method then saves into the database.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == MediaSelectorActivity.REQUEST_MEDIA && resultCode == RESULT_OK)
 		{
 			ArrayList<Track> selectedTracks = data.getParcelableArrayListExtra(MediaSelectorActivity.EXTRA_SELECTED_ITEMS);
 			currentCheckPoint.addTracks(selectedTracks);
 		}
 	}
 
 	/**
 	 * Unused method, must implement this because of MapActivity inheritance.
 	 * Might be implemented in a later stage.
 	 */
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		return false;
 	}
 
 	/**
 	 * This will be called when user changes location. It will create a new
 	 * GeoPoint consisting of longitude and latitude represented by integers and
 	 * add it to the route. It will also get the user's speed and total distance
 	 * traveled and convert this data into strings to be presented on the
 	 * screen. As of now this method will be called once every three seconds,
 	 * this number is a tradeoff between fast updates which would be needed for
 	 * doing fast paced activities like cycling and slower like walking. Slow
 	 * activities could do with a lesser update interval and as such preserve
 	 * battery life but as of this version the user does not have the
 	 * possibility to choose what kind of activity to undertake and thus the
 	 * value is hard coded.
 	 * 
 	 * @param location
 	 *            the new location of the user
 	 */
 	@Override
 	public void onLocationChanged(ParcelableGeoPoint geoPoint, String userSpeed, String userDistance, float totalDistance)
 	{
 		if (route.isStarted())
 		{
 			route.getGeoPoints().add(geoPoint);
 			route.setTotalDistance(totalDistance);
 			speedView.setText(userSpeed);
 			distanceView.setText(userDistance + getString(R.string.km));
 			mapView.postInvalidate();
 		}
 	}
 
 	/**
 	 * When our activity resumes, we want to register for location updates.
 	 */
 	protected void onResume()
 	{
 		super.onResume();
 		myLocationOverlay.enableMyLocation();
 	}
 
 	/**
 	 * When our activity pauses, we want to remove listening for location
 	 * updates
 	 */
 	protected void onPause()
 	{
 		super.onPause();
 		myLocationOverlay.disableMyLocation();
 	}
 
 	/**
 	 * Starts the timer for a new route and change the button Start to
 	 * StopAndSave
 	 */
 	@Override
 	public void onStartRouteClick()
 	{
 		wakeLock = Utils.acquireWakeLock(this);
 		route.setStarted(true);
 		startButton.setVisibility(View.GONE);
 		stopAndSaveButton.setVisibility(View.VISIBLE);
 		Utils.startTimer(this);
 	}
 
 	/**
 	 * Stops the route and timer and toggles the buttons. Also releases the wake
 	 * lock.
 	 */
 	@Override
 	public void onStopRouteClick()
 	{
 		Utils.stopTimer();
 		route.setStarted(false);
 		startButton.setVisibility(View.VISIBLE);
 		stopAndSaveButton.setVisibility(View.GONE);
 		wakeLock = Utils.releaseWakeLock();
 
 		if (!route.isNewRoute())
 		{
 			mapLocationListener.stopService();
 			showSaveResultDialog(route.getId());
 		}
 		else
 		{
 			showSaveRouteDialog();
 		}
 	}
 
 	/**
 	 * Shows the create checkpoint dialog and passes in the current geo point if
 	 * my location is enabled.
 	 */
 	@Override
 	public void onAddCheckPointClick()
 	{
 		if (myLocationOverlay.isMyLocationEnabled())
 		{
 			GeoPoint geoPoint = myLocationOverlay.getMyLocation();
 			if (geoPoint != null)
 			{
 				createCheckPoint(geoPoint);
 			}
 		}
 	}
 
 	/**
 	 * Launches the activity which shows a list of results for this route.
 	 */
 	@Override
 	public void onShowResultClick()
 	{
 		Intent intent = new Intent(this, ListExistingResultsActivity.class);
 		intent.putExtra(Route.EXTRA_ID, route.getId());
 		startActivity(intent);
 	}
 
 	/**
 	 * Creates a new result and initates the saveRouteDialog
 	 */
 	private void showSaveRouteDialog()
 	{
 		saveRouteDialog = new SaveRouteDialog(this, this, route);
 		saveRouteDialog.show();
 	}
 
 	/**
 	 * Method that gets called to update the UI with how much time that has
 	 * passed and presents this to the user. Will use field timePassed to
 	 * determine time while not altering the timePassed variable if we want to
 	 * pass that value to the database.
 	 */
 	@Override
 	public void onTimerTick()
 	{
 		timeView.setText(route.getTimePassedAsString());
 		route.setTimePassed(route.getTimePassed() + 1);
 	}
 
 	/**
 	 * This method deletes the checkpoint with all its tracks from the database.
 	 * It also calls on the checkpoint overlay to delete its current selected
 	 * checkpoint from the view. We then need to invalidate the map view for it
 	 * to update.
 	 */
 	@Override
 	public void onDeleteCheckPoint(long checkPointId)
 	{
 		databaseHandler.deleteCheckPoint(checkPointId);
 		databaseHandler.deleteTracksByCid(checkPointId);
 		checkPointOverlay.deleteCheckPoint();
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * Callback from the checkpoint dialog that tells the activity that the user
 	 * has pressed the save button and thus the checkpoint with all its data
 	 * should now be saved. This method also saves the "tracks" that are related
 	 * to the checkpoint.
 	 */
 	@Override
 	public void onSaveCheckPoint(CheckPoint checkPoint)
 	{
 		long cid = checkPoint.getId();
 		if (cid > 0)
 		{
 			databaseHandler.updateCheckPoint(checkPoint);
 			databaseHandler.deleteTracksByCid(cid);
 		}
 		else
 		{
 			cid = databaseHandler.saveCheckPoint(checkPoint);
 			checkPoint.setId(cid);
 		}
 		for (Track track : checkPoint.getTracks())
 		{
 			databaseHandler.saveTrack(cid, track);
 		}
 		checkPoint.getTracks().clear();
 	}
 
 	/**
 	 * When a checkpoint is tapped this method calls the showCheckPointDialog
 	 * method with MODE_EDIT which marks it as a edit dialog. This method also
 	 * sets the current checkpoint to the last tapped.
 	 */
 	@Override
 	public void onCheckPointTap(CheckPoint checkPoint)
 	{
 		currentCheckPoint = checkPoint;
 		showCheckPointDialog(checkPoint, EditCheckPointDialog.MODE_EDIT);
 	}
 
 	/**
 	 * Initiates a new checkpoint dialog
 	 * 
 	 * @param checkPoint
 	 * @param mode
 	 */
 	private void showCheckPointDialog(CheckPoint checkPoint, int mode)
 	{
 		checkPointDialog = new EditCheckPointDialog(this, checkPoint, mode);
 		checkPointDialog.show();
 	}
 
 	/**
 	 * Creates a checkpoint with the geopoint and adds it to the checkpoint
 	 * overlay, it also calls showCheckPointDialog with the MODE_ADD which marks
 	 * it as a add dialog. This method also saves the newly created checkpoint
 	 * as the current checkpoint for future reference.
 	 * 
 	 * @param geoPoint
 	 */
 	private void createCheckPoint(GeoPoint geoPoint)
 	{
 		CheckPoint checkPoint = new CheckPoint(geoPoint);
 		currentCheckPoint = checkPoint;
 		checkPointOverlay.addCheckPoint(checkPoint);
 		showCheckPointDialog(checkPoint, EditCheckPointDialog.MODE_ADD);
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * The onTap method zooms in on double tap and creates a geopoint on single
 	 * tap which it sends to createCheckPoint
 	 */
 	@Override
 	public void onTap(int x, int y, int eventType)
 	{
 		switch (eventType)
 		{
 			case MapOnGestureListener.EVENT_DOUBLE_TAP:
 				mapView.getController().zoomInFixing(x, y);
 				break;
 			case MapOnGestureListener.EVENT_SINGLE_TAP:
 				if (checkPointDialog == null || !checkPointDialog.isShowing())
 				{
 					GeoPoint geoPoint = mapView.getProjection().fromPixels(x, y);
 					createCheckPoint(geoPoint);
 				}
 				break;
 		}
 	}
 
 	/**
 	 * This method is called when an item in the action bar (options menu) has
 	 * been pressed. Currently this only takes the user to the parent activity
 	 * (main activity).
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				NavUtils.navigateUpFromSameTask(this);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * This method is called from the save geoPointList dialog when the user has
 	 * pressed the "save" button. It creates a new geoPointList with the
 	 * information given in the dialog and then saves it to the database. After
 	 * that, the user is taken back to the main activity.
 	 */
 	@Override
 	public void onSaveRoute(Route route, boolean saveResult)
 	{
 		route.setId(databaseHandler.saveRoute(route));
 		if (saveResult)
 		{
 			Result result = new Result(-1, -1, route.getTimePassed(), (int) route.getTotalDistance(), 0);
 			result.setRid(route.getId());
 			result.setTimestamp((int) System.currentTimeMillis() / 1000);
 			databaseHandler.saveResult(result);
 		}
 		databaseHandler.saveGeoPoints(route.getId(), route.getGeoPoints());
 		databaseHandler.updateCheckPointRid(route.getId());
 		launchMainActivity();
 	}
 
 	/**
 	 * Called when a route is being dismissed. This method just launches the
 	 * main activity.
 	 */
 	@Override
 	public void onDismissRoute()
 	{
 		databaseHandler.deleteRoute(route);
 		databaseHandler.deleteCheckPoints(route.getId());
 		launchMainActivity();
 	}
 
 	/**
 	 * Called by the system when the activity is shut down completely. Releases
	 * the wake lock and stops listening for location updates.
 	 */
 	@Override
 	public void onDestroy()
 	{
 		wakeLock = Utils.releaseWakeLock();
		locationManager.removeUpdates(mapLocationListener);
 		super.onDestroy();
 	}
 
 	/**
 	 * Private helper method to launch the main activity.
 	 */
 	private void launchMainActivity()
 	{
 		Intent intent = new Intent(this, MainActivity.class);
 		this.startActivity(intent);
 	}
 
 	/**
 	 * Saves all important data to be able to handle configuration changes. if a
 	 * dialog is shown it saves some extra fields to be able to restore the
 	 * dialog with its properties and fields
 	 */
 	@Override
 	protected void onSaveInstanceState(final Bundle outState)
 	{
 		int activeDialog = getActiveDialog();
 		outState.putBoolean(BUNDLE_STARTED, route.isStarted());
 		outState.putFloat(BUNDLE_TOTAL_DISTANCE, route.getTotalDistance());
 		outState.putInt(BUNDLE_TIME_PASSED, route.getTimePassed());
 		outState.putInt(BUNDLE_ACTIVE_DIALOG, activeDialog);
 		outState.putParcelableArrayList(BUNDLE_GEOPOINT_LIST, route.getGeoPoints());
 		if (activeDialog == DIALOG_CHECKPOINT)
 		{
 			currentCheckPoint = checkPointDialog.getCheckPoint();
 			onSaveCheckPoint(currentCheckPoint);
 			outState.putLong(BUNDLE_CID, currentCheckPoint.getId());
 		}
 		else if (activeDialog == DIALOG_SAVE_ROUTE)
 		{
 			route = saveRouteDialog.getRoute();
 			if (route.getId() == -1)
 			{
 				route.setId(databaseHandler.saveRoute(route));
 			}
 			else
 			{
 				databaseHandler.updateRoute(route);
 			}
 			outState.putBoolean(BUNDLE_SAVE_RESULT_CHECKED, saveRouteDialog.isSaveResultChecked());
 		}
 		outState.putLong(BUNDLE_RID, route.getId());
 	}
 
 	/**
 	 * Private helper method to assist in calculating which, if any, dialog is
 	 * shown
 	 * 
 	 * @return the dialog shown constant
 	 */
 	private int getActiveDialog()
 	{
 		int activeDialog = DIALOG_NONE;
 		if (checkPointDialog != null && checkPointDialog.isShowing())
 		{
 			activeDialog = DIALOG_CHECKPOINT;
 		}
 		if (saveRouteDialog != null && saveRouteDialog.isShowing())
 		{
 			activeDialog = DIALOG_SAVE_ROUTE;
 		}
 		if (saveResultDialog != null && saveResultDialog.isShowing())
 		{
 			activeDialog = DIALOG_SAVE_RESULT;
 		}
 		return activeDialog;
 	}
 
 	/**
 	 * Callback method which starts the timer again after back button is pressed
 	 * when a dialog is shown
 	 */
 	@Override
 	public void onResumeTimer()
 	{
 		Utils.startTimer(this);
 	}
 
 	/**
 	 * Shows a alert dialog when back button is pressed to confirm that the user
 	 * wants to discard the route. This is implemented to prevent the user to
 	 * hit the back button by mistake and quit the route.
 	 */
 	@Override
 	public void onBackPressed()
 	{
 		AlertDialog alertDialog = AlertDialogFactory.newConfirmBackDialog(this);
 		alertDialog.show();
 	}
 }
