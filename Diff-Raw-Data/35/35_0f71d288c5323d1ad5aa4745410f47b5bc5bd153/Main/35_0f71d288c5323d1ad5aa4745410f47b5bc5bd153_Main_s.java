 package de.blau.android;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.drawable.ColorDrawable;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.nfc.Tag;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ZoomControls;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import de.blau.android.Logic.CursorPaddirection;
 import de.blau.android.Logic.Mode;
 import de.blau.android.TagEditor.TagEditorData;
 import de.blau.android.actionbar.ModeDropdownAdapter;
 import de.blau.android.actionbar.UndoDialogFactory;
 import de.blau.android.easyedit.EasyEditManager;
 import de.blau.android.exception.OsmException;
 import de.blau.android.osb.Bug;
 import de.blau.android.osb.CommitTask;
 import de.blau.android.osm.BoundingBox;
 import de.blau.android.osm.Node;
 import de.blau.android.osm.OsmElement;
 import de.blau.android.osm.Server;
 import de.blau.android.osm.StorageDelegator;
 import de.blau.android.osm.UndoStorage;
 import de.blau.android.osm.Way;
 import de.blau.android.prefs.PrefEditor;
 import de.blau.android.prefs.Preferences;
 import de.blau.android.presets.Preset;
 import de.blau.android.resources.Paints;
 import de.blau.android.services.TrackerService;
 import de.blau.android.services.TrackerService.TrackerBinder;
 import de.blau.android.services.TrackerService.TrackerLocationListener;
 import de.blau.android.util.SavingHelper;
 import de.blau.android.views.overlay.OpenStreetBugsOverlay;
 import de.blau.android.views.overlay.OpenStreetMapViewOverlay;
 
 /**
  * This is the main Activity from where other Activities will be started.
  * 
  * @author mb
  */
 public class Main extends SherlockActivity implements OnNavigationListener, ServiceConnection, TrackerLocationListener {
 
 	/**
 	 * Tag used for Android-logging.
 	 */
 	private static final String DEBUG_TAG = Main.class.getName();
 
 	/**
 	 * Requests a {@link BoundingBox} as an activity-result.
 	 */
 	public static final int REQUEST_BOUNDINGBOX = 0;
 
 	/**
 	 * Requests a list of {@link Tag Tags} as an activity-result.
 	 */
 	public static final int REQUEST_EDIT_TAG = 1;
 
 	private DialogFactory dialogFactory;
 	
 	/** Objects to handle showing device orientation. */
 	private SensorManager sensorManager;
 	private Sensor sensor;
 	private SensorEventListener sensorListener = new SensorEventListener() {
 		float lastOrientation = -9999;
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		}
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			float orientation = event.values[0];
 			map.setOrientation(orientation);
 			// Repaint map only if orientation changed by at least 1 degree since last repaint
 			if (Math.abs(orientation - lastOrientation) > 1) {
 				lastOrientation = orientation;
 				map.invalidate();
 			}
 		}
 	};
 
 	/** The map View. */
 	private Map map;
 	/** Detector for taps, drags, and scaling. */
 	private VersionedGestureDetector mDetector;
 	/** Onscreen map zoom controls. */
 	private ZoomControls zoomControls;
 	/**
 	 * Our user-preferences.
 	 */
 	private Preferences prefs;
 
 	/**
 	 * Adapter providing items for the mode selection dropdown in the ActionBar
 	 */
 	private ModeDropdownAdapter modeDropdown;
 
 	/**
 	 * The manager for the EasyEdit mode
 	 */
 	private EasyEditManager easyEditManager;
 
 	/**
 	 * The logic that manipulates the model. (non-UI)<br/>
 	 * This is created in {@link #onCreate(Bundle)} and never changed afterwards.<br/>
 	 * If may be null or not reflect the current state if accessed from outside this activity.
 	 */
 	protected static Logic logic;
 	
 	/**
 	 * The currently selected preset
 	 */
 	private static Preset currentPreset;
 
 	/**
 	 * Flag indicating wheter the map will be re-downloaded once the activity resumes
 	 */
 	private static boolean redownloadOnResume;
 
 	/**
 	 * Flag indicating whether data should be loaded from a file when the activity resumes.
 	 * Set by {@link #onCreate(Bundle)}.
 	 * Overridden by {@link #redownloadOnResume}.
 	 */
 	private boolean loadOnResume;
 
 	/** Initialized in onCreate - this empty file indicates by its existence that showGPS should be enabled on start */
 	private File showGPSFlagFile = null;
 	private boolean showGPS;
 	private boolean followGPS;
 	/**
 	 * a local copy of the desired value for {@link TrackerService#setListenerNeedsGPS(boolean)}.
 	 * Will be automatically given to the tracker service on connect.
 	 */
 	private boolean wantLocationUpdates = false;
 
 	/**
 	 * The current instance of the tracker service
 	 */
 	private TrackerService tracker = null;
 
 	private UndoListener undoListener;
 
 	/**
 	 * While the activity is fully active (between onResume and onPause), this stores the currently active instance
 	 */
 	private static Main runningInstance;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		Log.i("Main", "onCreate");
 		setTheme(R.style.Theme_customMain);
 
 		super.onCreate(savedInstanceState);
 		Application.mainActivity = this;
 		
 		showGPSFlagFile = new File(this.getFilesDir(), "showgps.flag");
 		showGPS = showGPSFlagFile.exists();
 		
 		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 		if (sensorManager != null) {
 			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 			if (sensor == null) {
 				sensorManager = null;
 			}
 		}
 
 		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 
 		RelativeLayout rl = new RelativeLayout(getApplicationContext());
 		if (map != null) {
 			map.onDestroy();
 		}
 		map = new Map(getApplicationContext());
 		dialogFactory = new DialogFactory(this);
 
 		//Register some Listener
 		MapTouchListener mapTouchListener = new MapTouchListener();
 		map.setOnTouchListener(mapTouchListener);
 		map.setOnCreateContextMenuListener(mapTouchListener);
 		map.setOnKeyListener(new MapKeyListener());
 		mDetector = VersionedGestureDetector.newInstance(getApplicationContext(), mapTouchListener);
 		rl.addView(map);
 		
 		// Set up the zoom in/out controls
 		zoomControls = new ZoomControls(getApplicationContext());
 		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				logic.zoom(Logic.ZOOM_IN);
 				updateZoomControls();
 			}
 		});
 		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				logic.zoom(Logic.ZOOM_OUT);
 				updateZoomControls();
 			}
 		});
 		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		rl.addView(zoomControls, rlp);
 		
 		setContentView(rl);
 		
 		//Load previous logic (inkl. StorageDelegator)
 		logic = (Logic) getLastNonConfigurationInstance();
 		loadOnResume = false;
 		if (logic == null) {
 			Log.i("Main", "onCreate - creating new logic");
 			logic = new Logic(map, new Paints(getApplicationContext().getResources()));
 			if (isLastActivityAvailable()) {
 				// Start loading after resume to ensure loading dialog can be removed afterwards
 				loadOnResume = true;
 			} else {
 				gotoBoxPicker();
 			}
 		} else {
 			Log.i("Main", "onCreate - using logic from getLastNonConfigurationInstance");
 			logic.setMap(map);
 		}
 		
 		easyEditManager = new EasyEditManager(this, logic);
 		
 	}
 	
 	/**
 	 * Loads the preferences into {@link #map} and {@link #logic}, triggers new {@inheritDoc}
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		prefs = new Preferences(this);
 		map.setPrefs(prefs);
 		logic.setPrefs(prefs);
 		map.requestFocus();
 		
 		undoListener = new UndoListener();
 		
 		showActionBar();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.i("Main", "onResume");
 
 		bindService(new Intent(this, TrackerService.class), this, BIND_AUTO_CREATE);
 		
 		if (redownloadOnResume) {
 			redownloadOnResume = false;
 			logic.downloadLast();
 		} else if (loadOnResume) {
 			loadOnResume = false;
 			logic.loadFromFile();
 		}
 		
 		if (currentPreset == null) {
 			currentPreset = prefs.getPreset();
 		}
 		runningInstance = this;
 		
 		updateActionbarEditMode();
 		if (!prefs.isOpenStreetBugsEnabled() && logic.getMode() == Mode.MODE_OPENSTREETBUG) {
 			logic.setMode(Mode.MODE_MOVE);
 		}
 		modeDropdown.setShowOpenStreetBug(prefs.isOpenStreetBugsEnabled());
 		
 		logic.setSelectedBug(null);
 		logic.setSelectedNode(null);
 		logic.setSelectedWay(null);
 
 		if (tracker != null) tracker.setListener(this);
 
 		setShowGPS(showGPS); // reactive GPS listener if needed
 		setFollowGPS(followGPS);
 	}
 
 	@Override
 	protected void onPause() {
 		Log.i("Main", "onPause");
 		runningInstance = null;
 		disableLocationUpdates();
 		if (tracker != null) tracker.setListener(null);
 
 		// onPause is the last lifecycle callback guaranteed to be called on pre-honeycomb devices
 		// on honeycomb and later, onStop is also guaranteed to be called, so we can defer saving.
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) saveData();
 		
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStop() {
 		Log.i("Main", "onStop");
 		
 		// On devices with Android versions before Honeycomb, we already save data in onPause
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) saveData();
 		
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		map.onDestroy();
 		if (tracker != null) tracker.setListener(null);
 		try {
 			unbindService(this);
 		} catch (Exception e) {} // ignore errors, this is just cleanup
 		super.onDestroy();
 	}
 
 	/**
 	 * Save current data (state, downloaded data, changes, ...) to file(s)
 	 */
 	private void saveData() {
 		Log.i("Main", "saving data");
 		logic.save();
 		if (showGPS) {
 			try {
 				showGPSFlagFile.createNewFile();
 			} catch (IOException e) {
 				Log.e("Main", "failed to create showGPS flag file");
 			}
 		} else {
 			showGPSFlagFile.delete();
 		}
 	}
 
 	/**
 	 * Update the state of the onscreen zoom controls to reflect their ability
 	 * to zoom in/out.
 	 */
 	private void updateZoomControls() {
 		zoomControls.setIsZoomInEnabled(logic.canZoom(Logic.ZOOM_IN));
 		zoomControls.setIsZoomOutEnabled(logic.canZoom(Logic.ZOOM_OUT));
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		Log.i("Main", "onRetainNonConfigurationInstance");
 		return logic;
 	}
 
 	/**
 	 * Sets up the Action Bar.
 	 */
 	private void showActionBar() {
 		ActionBar actionbar = getSupportActionBar();
 		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		actionbar.setBackgroundDrawable(new ColorDrawable(0xaa000000));
 		actionbar.setDisplayShowHomeEnabled(false);
 		actionbar.setDisplayShowTitleEnabled(false);
 		
 		modeDropdown = new ModeDropdownAdapter(this, prefs.isOpenStreetBugsEnabled());
 		actionbar.setListNavigationCallbacks(modeDropdown, this);
 		
 		actionbar.show();
 		setSupportProgressBarIndeterminateVisibility(false);
 	}
 	
 	
 	public void updateActionbarEditMode() {
 		getSupportActionBar().setSelectedNavigationItem(modeDropdown.getIndexForMode(logic.getMode()));
 	}
 	
 	public static void onEditModeChanged() {
 		if (runningInstance != null) runningInstance.updateActionbarEditMode();
 	}
 	
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		logic.setMode(modeDropdown.getModeForItem(itemPosition));
 		return true;
 	}
 	
 	
 	/**
 	 * Creates the menu from the XML file "main_menu.xml".<br> {@inheritDoc}
 	 */
  	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		final MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		
 		menu.findItem(R.id.menu_gps_show).setChecked(showGPS);
 		menu.findItem(R.id.menu_gps_follow).setChecked(followGPS);
 		menu.findItem(R.id.menu_gps_start).setEnabled(tracker != null && !tracker.isTracking());
 		menu.findItem(R.id.menu_gps_pause).setEnabled(tracker != null && tracker.isTracking());
 
 		MenuItem undo = menu.findItem(R.id.menu_undo);
 		undo.setVisible(logic.getUndo().canUndo() || logic.getUndo().canRedo());
 		View undoView = undo.getActionView();
 		undoView.setOnClickListener(undoListener);
 		undoView.setOnLongClickListener(undoListener);
 		
 		return true;
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_confing:
 			startActivity(new Intent(getApplicationContext(), PrefEditor.class));
 			return true;
 			
 		case R.id.menu_help:
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://wiki.openstreetmap.org/wiki/Vespucci/Help"));
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 			return true;
 
 		case R.id.menu_gps_show:
 			toggleShowGPS();
 			return true;
 			
 
 		case R.id.menu_gps_follow:
 			toggleFollowGPS();
 			return true;
 
 		case R.id.menu_gps_start:
 			if (tracker != null && ensureGPSProviderEnabled()) {
 				tracker.startTracking();
 				setFollowGPS(true);
 				triggerMenuInvalidation();
 			}
 			return true;
 
 		case R.id.menu_gps_pause:
 			if (tracker != null && ensureGPSProviderEnabled()) {
 				tracker.stopTracking(false);
 				triggerMenuInvalidation();
 			}
 			return true;
 
 		case R.id.menu_gps_clear:
 			if (tracker != null) tracker.stopTracking(true);
 			triggerMenuInvalidation();
 			map.invalidate();
 			return true;
 			
 		case R.id.menu_gps_export:
 			if (tracker != null) {
 				SavingHelper.asyncExport(this, tracker);
 			}
 			return true;
 
 		case R.id.menu_transfer_download_current:
 			onMenuDownloadCurrent();
 			return true;
 
 		case R.id.menu_transfer_download_other:
 			gotoBoxPicker();
 			return true;
 
 		case R.id.menu_transfer_upload:
 			confirmUpload();
 			return true;
 		
 		case R.id.menu_transfer_export:
 			if (logic == null || logic.delegator == null) return true;
 			SavingHelper.asyncExport(this, logic.delegator);
 			return true;
 
 		case R.id.menu_undo:
 			// should not happen
 			undoListener.onClick(null);
 			return true;
 			
 		}
 		
 		return false;
 	}
 
 	private void setShowGPS(boolean show) {
 		if (show && !ensureGPSProviderEnabled()) {
 			show = false;
 		}
 		showGPS = show;
 		Log.d("Main", "showGPS: "+ show);
 		if (show) {
 			enableLocationUpdates();
 		} else {
 			setFollowGPS(false);
 			map.setLocation(null);
 			disableLocationUpdates();
 		}
 		map.invalidate();
 		triggerMenuInvalidation();
 	}
 	
 	/**
 	 * Checks if GPS is enabled in the settings.
 	 * If not, returns false and shows location settings.
 	 * @return true if GPS is enabled, false if not
 	 */
 	private boolean ensureGPSProviderEnabled() {
 		LocationManager locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
 		try {
 			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				return true;
 			} else {
 				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 				return false;
 			}
 		} catch (Exception e) {
 			Log.e("Main", "Error when checking for GPS, assuming GPS not available", e);
 			Toast.makeText(this, R.string.gps_failure, Toast.LENGTH_SHORT).show();
 			return false;
 		}
 	}
 
 	private void setFollowGPS(boolean follow) {
 		followGPS = follow;
 		if (follow) {
 			setShowGPS(true);
 			if (map.getLocation() != null) onLocationChanged(map.getLocation());
 		}
 		triggerMenuInvalidation();
 	}
 	
 	private void toggleShowGPS() {
 		boolean newState = !showGPS;
 		setShowGPS(newState);
 	}
 	
 	
 	private void toggleFollowGPS() {
 		boolean newState = !followGPS;
 		setFollowGPS(newState);
 	}
 	
 	private void enableLocationUpdates() {
 		if (wantLocationUpdates == true) return;
 		if (sensorManager != null) {
 			sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
 		}
 		wantLocationUpdates = true;
 		if (tracker != null) tracker.setListenerNeedsGPS(true);
 	}
 	
 	private void disableLocationUpdates() {
 		if (wantLocationUpdates == false) return;
 		if (sensorManager != null) sensorManager.unregisterListener(sensorListener);
 		wantLocationUpdates  = false;
 		if (tracker != null) tracker.setListenerNeedsGPS(false);
 	}
 
 	/**
 	 * Handles the menu click on "download current view".<br>
 	 * When no {@link #delegator} is set, the user will be redirected to AreaPicker.<br>
 	 * When the user made some changes, {@link #DIALOG_TRANSFER_DOWNLOAD_CURRENT_WITH_CHANGES} will be shown.<br>
 	 * Otherwise the current viewBox will be re-downloaded from the server.
 	 */
 	private void onMenuDownloadCurrent() {
 		if (logic.hasChanges()) {
 			showDialog(DialogFactory.DOWNLOAD_CURRENT_WITH_CHANGES);
 		} else {
 			performCurrentViewHttpLoad();
 		}
 	}
 
 	/**
 	 * Uses {@link DialogFactory} to create Dialogs<br> {@inheritDoc}
 	 */
 	@Override
 	protected Dialog onCreateDialog(final int id) {
 		Dialog dialog = dialogFactory.create(id);
 		if (dialog != null) {
 			return dialog;
 		}
 		return super.onCreateDialog(id);
 	}
 	
 	/**
 	 * Prepare the fields of dialogs before they are shown. Only some need this special
 	 * handling.
 	 * @param id Dialog ID number.
 	 * @param dialog Dialog object.
 	 */
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		super.onPrepareDialog(id, dialog);
 		if (dialog instanceof AlertDialog) {
 			AlertDialog ad = (AlertDialog)dialog;
 			switch (id) {
 			case DialogFactory.CONFIRM_UPLOAD:
 				TextView changes = (TextView)ad.findViewById(R.id.upload_changes);
 				changes.setText(getString(R.string.confirm_upload_text, getPendingChanges()));
 				break;
 			case DialogFactory.OPENSTREETBUG_EDIT:
 				Bug bug = logic.getSelectedBug();
 				// NullPointerException on following line
 				// dialog (and hence ad) cannot be null due to instanceof
 				// therefore bug must have been null
 				// Investigation of this in progress.... (AG 10-Apr-2011)
 				ad.setTitle(getString((bug.getId() == 0) ? R.string.openstreetbug_new_title : R.string.openstreetbug_edit_title));
 				TextView comments = (TextView)ad.findViewById(R.id.openstreetbug_comments);
 				comments.setText(bug.getComment().replaceAll("<hr />", "\n"));
 				EditText comment = (EditText)ad.findViewById(R.id.openstreetbug_comment);
 				comment.setText("");
 				comment.setFocusable(!bug.isClosed());
 				comment.setFocusableInTouchMode(!bug.isClosed());
 				comment.setEnabled(!bug.isClosed());
 				CheckBox close = (CheckBox)ad.findViewById(R.id.openstreetbug_close);
 				close.setChecked(bug.isClosed());
 				close.setEnabled(!bug.isClosed() && bug.getId() != 0);
 				Button commit = ad.getButton(AlertDialog.BUTTON_POSITIVE);
 				commit.setEnabled(!bug.isClosed());
 				break;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REQUEST_BOUNDINGBOX && data != null) {
 			handleBoxPickerResult(resultCode, data);
 		} else if (requestCode == REQUEST_EDIT_TAG && resultCode == RESULT_OK && data != null) {
 			handleTagEditorResult(data);
 		}
 	}
 
 	/**
 	 * @param resultCode
 	 * @param data
 	 */
 	private void handleBoxPickerResult(final int resultCode, final Intent data) {
 		Bundle b = data.getExtras();
 		int left = b.getInt(BoxPicker.RESULT_LEFT);
 		int bottom = b.getInt(BoxPicker.RESULT_BOTTOM);
 		int right = b.getInt(BoxPicker.RESULT_RIGHT);
 		int top = b.getInt(BoxPicker.RESULT_TOP);
 
 		try {
 			BoundingBox box = new BoundingBox(left, bottom, right, top);
 			if (resultCode == RESULT_OK) {
 				performHttpLoad(box);
 			} else if (resultCode == RESULT_CANCELED) {
 				openEmptyMap(box);
 			}
 		} catch (OsmException e) {
 			//Values should be done checked in LocationPciker.
 			Log.e(DEBUG_TAG, "OsmException", e);
 		}
 	}
 
 	/**
 	 * @param data
 	 */
 	private void handleTagEditorResult(final Intent data) {
 		Bundle b = data.getExtras();
 		if (b != null && b.containsKey(TagEditor.TAGEDIT_DATA)) {
 			// Read data from extras
 			TagEditorData editorData = (TagEditorData) b.getSerializable(TagEditor.TAGEDIT_DATA);
 			logic.setTags(editorData.type, editorData.osmId, editorData.tags);
 			map.invalidate();
 		}
 	}
 	
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		map.onLowMemory();
 	}
 
 	/**
 	 * TODO: put this in Logic!!! Checks if a serialized {@link StorageDelegator} file is available.
 	 * 
 	 * @return true, when the file is available, otherwise false.
 	 */
 	private boolean isLastActivityAvailable() {
 		FileInputStream in = null;
 		try {
 			in = openFileInput(StorageDelegator.FILENAME);
 			return true;
 		} catch (final FileNotFoundException e) {
 			return false;
 		} finally {
 			SavingHelper.close(in);
 		}
 	}
 
 	public void performCurrentViewHttpLoad() {
 		logic.downloadCurrent();
 	}
 
 	private void performHttpLoad(final BoundingBox box) {
 		logic.downloadBox(box);
 	}
 
 	private void openEmptyMap(final BoundingBox box) {
 		logic.newEmptyMap(box);
 	}
 
 	/**
 	 * 
 	 */
 	public void performUpload(final String comment) {
 		dismissDialog(DialogFactory.CONFIRM_UPLOAD);
 		final Server server = prefs.getServer();
 
 		if (server != null && server.isLoginSet()) {
 			if (logic.hasChanges()) {
 				logic.upload(comment);
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.toast_no_changes, Toast.LENGTH_LONG).show();
 			}
 		} else {
 			showDialog(DialogFactory.NO_LOGIN_DATA);
 		}
 	}
 	
 	/**
 	 * Commit changes to the currently selected OpenStreetBug.
 	 * @param comment Comment to add to the bug.
 	 * @param close Flag to indicate if the bug is to be closed.
 	 */
 	public void performOpenStreetBugCommit(final String comment, final boolean close) {
 		Log.d("Vespucci", "Main.performOpenStreetBugCommit");
 		dismissDialog(DialogFactory.OPENSTREETBUG_EDIT);
 		new CommitTask(logic.getSelectedBug(), comment, close) {
 			
 			/** Flag to track if the bug is new. */
 			private boolean newBug;
 			
 			@Override
 			protected void onPreExecute() {
 				newBug = (bug.getId() == 0);
 				setSupportProgressBarIndeterminateVisibility(true);
 			}
 			
 			@Override
 			protected Boolean doInBackground(String... args) {
 				// execute() is called below with no arguments (args will be empty)
 				// getDisplayName() is deferred to here in case a lengthy OSM query
 				// is required to determine the nickname
 				String nickname = null;
 				Server server = prefs.getServer();
 				if (server.isLoginSet()) {
 					nickname = server.getDisplayName();
 				}
 				return super.doInBackground(nickname);
 			}
 			
 			@Override
 			protected void onPostExecute(Boolean result) {
 				if (result && newBug) {
 					for (OpenStreetMapViewOverlay o : map.getOverlays()) {
 						if (o instanceof OpenStreetBugsOverlay) {
 							((OpenStreetBugsOverlay)o).addBug(bug);
 						}
 					}
 				}
 				setSupportProgressBarIndeterminateVisibility(false);
 				Toast.makeText(getApplicationContext(), result ? R.string.openstreetbug_commit_ok : R.string.openstreetbug_commit_fail, Toast.LENGTH_SHORT).show();
 				map.invalidate();
 			}
 			
 		}.execute();
 	}
 
 	/**
 	 * 
 	 */
 	public void confirmUpload() {
 		final Server server = prefs.getServer();
 
 		if (server != null && server.isLoginSet()) {
 			if (logic.hasChanges()) {
 				showDialog(DialogFactory.CONFIRM_UPLOAD);
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.toast_no_changes, Toast.LENGTH_LONG).show();
 			}
 		} else {
 			showDialog(DialogFactory.NO_LOGIN_DATA);
 		}
 	}
 
 	/**
 	 * Starts the LocationPicker activity for requesting a location.
 	 */
 	public void gotoBoxPicker() {
 		Intent intent = new Intent(getApplicationContext(), BoxPicker.class);
 		if (logic.hasChanges()) {
 			DialogFactory.createDataLossActivityDialog(this, intent, REQUEST_BOUNDINGBOX).show();
 		} else {
 			startActivityForResult(intent, REQUEST_BOUNDINGBOX);
 		}
 	}
 
 	private enum AppendMode {
 		APPEND_START, APPEND_APPEND
 	}
 
 	/**
 	 * @param selectedElement
 	 */
 	public void performTagEdit(final OsmElement selectedElement) {
 		if (selectedElement instanceof Node) {
 			logic.setSelectedNode((Node) selectedElement);
 		} else if (selectedElement instanceof Way) {
 			logic.setSelectedWay((Way) selectedElement);
 		}
 	
 		if (selectedElement != null) {
 			Intent startTagEditor = new Intent(getApplicationContext(), TagEditor.class);
 			startTagEditor.putExtra(TagEditor.TAGEDIT_DATA, new TagEditorData(selectedElement));
 			Main.this.startActivityForResult(startTagEditor, Main.REQUEST_EDIT_TAG);
 		}
 	}
 
 	
 	public class UndoListener implements OnClickListener, OnLongClickListener {
 
 		@Override
 		public void onClick(View arg0) {
 			String name = logic.getUndo().undo();
 			if (name != null) {
 				Toast.makeText(Main.this, getResources().getString(R.string.undo) + ": " + name, Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(Main.this, getResources().getString(R.string.undo_nothing), Toast.LENGTH_SHORT).show();
 			}
 			map.invalidate();
 		}
 
 		@Override
 		public boolean onLongClick(View v) {
 			UndoStorage undo = logic.getUndo();
 			if (undo.canUndo() || undo.canRedo()) {
 				UndoDialogFactory.showUndoDialog(Main.this, undo);
 			} else {
 				Toast.makeText(Main.this, getResources().getString(R.string.undo_nothing), Toast.LENGTH_SHORT).show();				
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * A TouchListener for all gestures made on the touchscreen.
 	 * 
 	 * @author mb
 	 */
 	private class MapTouchListener implements OnTouchListener, VersionedGestureDetector.OnGestureListener, OnCreateContextMenuListener, OnMenuItemClickListener {
 
 		private AppendMode appendMode;
 
 		private List<OsmElement> clickedNodesAndWays;
 		private List<Bug> clickedBugs;
 		
 		@Override
 		public boolean onTouch(final View v, final MotionEvent m) {
 			if (m.getAction() == MotionEvent.ACTION_DOWN) {
 				clickedBugs = null;
 				clickedNodesAndWays = null;
 				logic.handleTouchEventDown(m.getX(), m.getY());
 			}
 			mDetector.onTouchEvent(v, m);
 			return v.onTouchEvent(m);
 		}
 		
 		@Override
 		public void onClick(View v, float x, float y) {
 			OpenStreetBugsOverlay osbo = map.getOpenStreetBugsOverlay();
 			clickedBugs = (osbo != null) ? osbo.getClickedBugs(x, y, map.getViewBox()) : null;
 			
 			Mode mode = logic.getMode();
 			boolean isInEditZoomRange = logic.isInEditZoomRange();
 			
 			if (isInEditZoomRange) {
 				switch (mode) {
 				case MODE_MOVE:
 					break;
 				case MODE_OPENSTREETBUG:
 					switch ((clickedBugs == null) ? 0 : clickedBugs.size()) {
 					case 0:
 						performBugEdit(logic.makeNewBug(x, y));
 						break;
 					case 1:
 						performBugEdit(clickedBugs.get(0));
 						break;
 					default:
 						v.showContextMenu();
 						break;
 					}
 					break;
 				case MODE_ADD:
 					logic.performAdd(x, y);
 					break;
 				case MODE_TAG_EDIT:
 					selectElementForTagEdit(v, x, y);
 					break;
 				case MODE_ERASE:
 					selectElementForErase(v, x, y);
 					break;
 				case MODE_SPLIT:
 					selectElementForSplit(v, x, y);
 					break;
 				case MODE_APPEND:
 					performAppend(v, x, y);
 					break;
 				case MODE_EASYEDIT:
 					performEasyEdit(v, x, y);
 					break;
 				}
 				map.invalidate();
 			} else {
 				switch ((clickedBugs == null) ? 0 : clickedBugs.size()) {
 				case 0:
 					if (!isInEditZoomRange) {
 						int res;
 						switch (mode) {
 						case MODE_ADD:
 						case MODE_EDIT:
 						case MODE_APPEND:
 						case MODE_ERASE:
 						case MODE_SPLIT:
 						case MODE_TAG_EDIT:
 						case MODE_EASYEDIT:
 							res = R.string.toast_not_in_edit_range;
 							break;
 						case MODE_OPENSTREETBUG:
 							res = R.string.toast_not_in_bug_range;
 							break;
 						case MODE_MOVE:
 						default:
 							res = 0;
 							break;
 						}
 						if (res != 0) {
 							Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG).show();
 						}
 					}
 					break;
 				case 1:
 					performBugEdit(clickedBugs.get(0));
 					break;
 				default:
 					v.showContextMenu();
 					break;
 				}
 			}
 		}
 		
 		@Override
 		public boolean onLongClick(View v, float x, float y) {
 			if (logic.getMode() != Mode.MODE_EASYEDIT) {
 				return false; // ignore long clicks
 			}
 			
 			if (logic.isInEditZoomRange()) {
 				return easyEditManager.handleLongClick(v, x, y);
 			}
 			
 			return true; // long click handled
 		}
 
 		@Override
 		public void onDrag(View v, float x, float y, float dx, float dy) {
 			logic.handleTouchEventMove(x, y, -dx, dy);
 			setFollowGPS(false);
 		}
 		
 		@Override
 		public void onScale(View v, float scaleFactor, float prevSpan, float curSpan) {
 			logic.zoom((curSpan - prevSpan) / prevSpan);
 			updateZoomControls();
 		}
 		
 		private void selectElementForTagEdit(final View v, final float x, final float y) {
 			clickedNodesAndWays = logic.getClickedNodesAndWays(x, y);
 			switch (((clickedBugs == null) ? 0 : clickedBugs.size()) + clickedNodesAndWays.size()) {
 			case 0:
 				// no elements were touched, ignore
 				break;
 			case 1:
 				// exactly one element touched
 				if (clickedBugs != null && clickedBugs.size() == 1) {
 					performBugEdit(clickedBugs.get(0));
 				} else {
 					performTagEdit(clickedNodesAndWays.get(0));
 				}
 				break;
 			default:
 				// multiple possible elements touched - show menu
 				v.showContextMenu();
 				break;
 			}
 		}
 
 		private void selectElementForErase(final View v, final float x, final float y) {
 			clickedNodesAndWays = logic.getClickedNodes(x, y);
 			switch (clickedNodesAndWays.size()) {
 			case 0:
 				// no elements were touched, ignore
 				break;
 			case 1:
 				logic.performEraseNode((Node)clickedNodesAndWays.get(0));
 				break;
 			default:
 				v.showContextMenu();
 				break;
 			}
 		}
 
 		private void selectElementForSplit(final View v, final float x, final float y) {
 			clickedNodesAndWays = logic.getClickedNodes(x, y);
 
 			//TODO remove nodes with no ways from list
 //			for (Iterator iterator = clickedNodesAndWays.iterator(); iterator.hasNext();) {
 //				Node node = (Node) iterator.next();
 //				if (node.getWaysCount() < 1) {
 //					iterator.remove();
 //				}
 //			}
 
 			switch (clickedNodesAndWays.size()) {
 			case 0:
 				// no elements were touched, ignore
 				break;
 			case 1:
 				logic.performSplit((Node)clickedNodesAndWays.get(0));
 				break;
 			default:
 				v.showContextMenu();
 				break;
 			}
 		}
 
 		/**
 		 * Appends a new Node to a selected Way. If any Way was yet selected, the user have to select one end-node
 		 * first. When the user clicks on an empty area, a new node will generated. When he clicks on a existing way,
 		 * the new node will be generated on that way. when he selects a different node, this one will be used. when he
 		 * selects the previous selected node, it will be de-selected.
 		 * 
 		 * @param x the click-position on the display.
 		 * @param y the click-position on the display.
 		 */
 		public void performAppend(final View v, final float x, final float y) {
 			Node lSelectedNode = logic.getSelectedNode();
 			Way lSelectedWay = logic.getSelectedWay();
 
 			if (lSelectedWay == null) {
 				clickedNodesAndWays = logic.getClickedEndNodes(x, y);
 				switch (clickedNodesAndWays.size()) {
 				case 0:
 					// no elements touched, ignore
 					break;
 				case 1:
 					logic.performAppendStart(clickedNodesAndWays.get(0));
 					break;
 				default:
 					appendMode = AppendMode.APPEND_START;
 					v.showContextMenu();
 					break;
 				}
 			} else if (lSelectedWay.isEndNode(lSelectedNode)) {
 				// TODO Resolve multiple possible selections
 				logic.performAppendAppend(x, y);
 			}
 		}
 		
 		/**
 		 * Perform easy edit touch processing.
 		 * @param v
 		 * @param x the click-position on the display.
 		 * @param y the click-position on the display.
 		 */
 		public void performEasyEdit(final View v, final float x, final float y) {
 			if (!easyEditManager.actionModeHandledClick(x, y)) {
 				clickedNodesAndWays = logic.getClickedNodesAndWays(x, y);
 				switch (((clickedBugs == null) ? 0 : clickedBugs.size()) + clickedNodesAndWays.size()) {
 				case 0:
 					// no elements were touched
 					easyEditManager.nothingTouched();
 					break;
 				case 1:
 					// exactly one element touched
 					if (clickedBugs != null && clickedBugs.size() == 1) {
 						performBugEdit(clickedBugs.get(0));
 					} else {
 						easyEditManager.editElement(clickedNodesAndWays.get(0));
 					}
 					break;
 				default:
 					// multiple possible elements touched - show menu
 					v.showContextMenu();
 					break;
 				}
 			}
 		}
 
 		/**
 		 * Edit an OpenStreetBug.
 		 * @param bug The bug to edit.
 		 */
 		private void performBugEdit(final Bug bug) {
 			Log.d("Vespucci", "editing bug:"+bug);
 			logic.setSelectedBug(bug);
 			showDialog(DialogFactory.OPENSTREETBUG_EDIT);
 		}
 		
 		@Override
 		public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
 			onCreateDefaultContextMenu(menu, v, menuInfo);
 		}
 			
 		public void onCreateDefaultContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
 			int id = 0;
 			if (clickedBugs != null) {
 				for (Bug b : clickedBugs) {
 					menu.add(Menu.NONE, id++, Menu.NONE, b.getDescription()).setOnMenuItemClickListener(this);
 				}
 			}
 			if (clickedNodesAndWays != null) {
 				for (OsmElement e : clickedNodesAndWays) {
 					menu.add(Menu.NONE, id++, Menu.NONE, e.getDescription()).setOnMenuItemClickListener(this);
 				}
 			}
 		}
 
 		@Override
 		public boolean onMenuItemClick(final android.view.MenuItem item) {
 			int itemId = item.getItemId();
 			if (clickedBugs != null && itemId >= 0 && itemId < clickedBugs.size()) {
 				performBugEdit(clickedBugs.get(itemId));
 			} else {
 				if (clickedBugs != null) {
 					itemId -= clickedBugs.size();
 				}
 				if (itemId >= 0 && itemId < clickedNodesAndWays.size()) {
 					OsmElement element = clickedNodesAndWays.get(itemId);
 					switch (logic.getMode()) {
 					case MODE_TAG_EDIT:
 						performTagEdit(element);
 						break;
 					case MODE_ERASE:
 						logic.performEraseNode((Node) element);
 						break;
 					case MODE_SPLIT:
 						logic.performSplit((Node) element);
 						break;
 					case MODE_APPEND:
 						switch (appendMode) {
 						case APPEND_START:
 							logic.performAppendStart(element);
 							break;
 						case APPEND_APPEND:
 							// TODO
 							break;
 						}
 						break;
 					case MODE_EASYEDIT:
 						easyEditManager.editElement(element);
 						break;
 					}
 				}
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * A KeyListener for all key events.
 	 * 
 	 * @author mb
 	 */
 	public class MapKeyListener implements OnKeyListener {
 
 		@Override
 		public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
 			switch (event.getAction()) {
 			case KeyEvent.ACTION_UP:
 				if (!v.onKeyUp(keyCode, event)) {
 					switch (keyCode) {
 					case KeyEvent.KEYCODE_VOLUME_UP:
 					case KeyEvent.KEYCODE_VOLUME_DOWN:
 						// this stops the piercing beep related to volume adjustments
 						return true;
 					}
 				}
 				break;
 			case KeyEvent.ACTION_DOWN:
 				if (!v.onKeyDown(keyCode, event)) {
 					switch (keyCode) {
 					case KeyEvent.KEYCODE_DPAD_CENTER:
 						setFollowGPS(true);
 						return true;
 						
 					case KeyEvent.KEYCODE_DPAD_UP:
 						translate(Logic.CursorPaddirection.DIRECTION_UP);
 						return true;
 						
 					case KeyEvent.KEYCODE_DPAD_DOWN:
 						translate(Logic.CursorPaddirection.DIRECTION_DOWN);
 						return true;
 						
 					case KeyEvent.KEYCODE_DPAD_LEFT:
 						translate(Logic.CursorPaddirection.DIRECTION_LEFT);
 						return true;
 						
 					case KeyEvent.KEYCODE_DPAD_RIGHT:
 						translate(Logic.CursorPaddirection.DIRECTION_RIGHT);
 						return true;
 						
 					case KeyEvent.KEYCODE_VOLUME_UP:
 					case KeyEvent.KEYCODE_SEARCH:
 						logic.zoom(Logic.ZOOM_IN);
 						updateZoomControls();
 						return true;
 						
 					case KeyEvent.KEYCODE_VOLUME_DOWN:
 					case KeyEvent.KEYCODE_SHIFT_LEFT:
 					case KeyEvent.KEYCODE_SHIFT_RIGHT:
 						logic.zoom(Logic.ZOOM_OUT);
 						updateZoomControls();
 						return true;
 					}
 				}
 				break;
 			}
 			return false;
 		}
 		
 		private void translate(final CursorPaddirection direction) {
 			setFollowGPS(false);
 			logic.translate(direction);
 		}
 	}
 
 	/**
 	 * @return a list of all pending changes to upload (contains newlines)
 	 */
 	public String getPendingChanges() {
 		List<String> changes = logic.getPendingChanges(this);
 		StringBuilder retval = new StringBuilder();
 		for (String change : changes) {
 			retval.append(change).append('\n');
 		}
 		return retval.toString();
 	}
 	
 	/**
 	 * Invalidates (redraws) the map
 	 */
 	public void invalidateMap() {
 		map.invalidate();
 	}
 	
 	public void triggerMapContextMenu() {
 		map.showContextMenu();
 	}
 	
 	public static boolean hasChanges() {
 		if (logic == null) return false;
 		return logic.hasChanges();
 	}
 	
 	/**
 	 * Sets the activity to re-download the last downloaded area on startup
 	 * (use e.g. when the API URL is changed)
 	 */
 	public static void prepareRedownload() {
 		redownloadOnResume = true;
 	}
 
 	/**
 	 * @return the currentPreset
 	 */
 	public static Preset getCurrentPreset() {
 		return currentPreset;
 	}
 
 	/**
 	 * Resets the current preset, causing it to be re-parsed
 	 */
 	public static void resetPreset() {
 		currentPreset = null;
 	}
 	
 	public String getBaseURL() {
 		return prefs.getServer().getBaseURL();
 	}
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		Log.i("Main", "Tracker service connected");
 		tracker = (((TrackerBinder)service).getService());
 		map.setTracker(tracker);
 		tracker.setListener(this);
 		tracker.setListenerNeedsGPS(wantLocationUpdates);
 		triggerMenuInvalidation();
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		// should never happen, but just to be sure
 		Log.i("Main", "Tracker service disconnected");
 		tracker = null;
 		map.setTracker(null);
 		triggerMenuInvalidation();
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if (followGPS) {
 			BoundingBox viewBox = map.getViewBox();
 			// ensure the view is zoomed in to at least the most zoomed-out
 			while (!viewBox.canZoomOut() && viewBox.canZoomIn()) {
 				viewBox.zoomIn();
 			}
 			viewBox.moveTo((int) (location.getLongitude() * 1E7d), (int) (location.getLatitude() * 1E7d));
 		}
 		if (showGPS) {
 			map.setLocation(location);
 		}
 		map.invalidate();
 	}
 	
 	
 	@Override
 	/**
 	 * DO NOT CALL DIRECTLY in custom code.
 	 * Use {@link #triggerMenuInvalidation()} to make it easier to debug and implement workarounds for android bugs.
 	 * Must be called from the main thread.
 	 */
 	public void invalidateOptionsMenu() {
 		//Log.d(DEBUG_TAG, "invalidateOptionsMenu called");
 		super.invalidateOptionsMenu();
 	}
 	
 	/**
 	 * Simply calls {@link #invalidateOptionsMenu()}.
 	 * Used to make it easier to implement workarounds.
 	 * MUST BE CALLED FROM THE MAIN/UI THREAD!
 	 */
 	public void triggerMenuInvalidation() {
 		//Log.d(DEBUG_TAG, "triggerMenuInvalidation called");
 		invalidateOptionsMenu(); // TODO delay or make conditional to work around android bug?
 	}
 	
 	/**
 	 * Invalidates the options menu of the main activity if such an activity exists.
 	 * MUST BE CALLED FROM THE MAIN/UI THREAD!
 	 */
 	public static void triggerMenuInvalidationStatic() {
 		if (Application.mainActivity == null) return;
 		// DO NOT IGONORE "wrong thread" EXCEPTIONS FROM THIS.
 		// It *will* mess up your menu in many creative ways.
 		Application.mainActivity.triggerMenuInvalidation();
 	}
 
 }
