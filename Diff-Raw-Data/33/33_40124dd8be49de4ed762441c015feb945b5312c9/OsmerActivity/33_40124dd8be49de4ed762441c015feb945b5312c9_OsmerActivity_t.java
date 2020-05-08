 package it.giacomos.android.osmer;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import it.giacomos.android.osmer.fragments.MapFragmentListener;
 import it.giacomos.android.osmer.instanceSnapshotManager.SnapshotManager;
 import it.giacomos.android.osmer.instanceSnapshotManager.SnapshotManagerListener;
 import it.giacomos.android.osmer.interfaceHelpers.MenuActionsManager;
 import it.giacomos.android.osmer.interfaceHelpers.NetworkGuiErrorManager;
 import it.giacomos.android.osmer.interfaceHelpers.ObservationTypeGetter;
 import it.giacomos.android.osmer.interfaceHelpers.RadarImageTimestampTextBuilder;
 import it.giacomos.android.osmer.interfaceHelpers.TitlebarUpdater;
 import it.giacomos.android.osmer.locationUtils.LocationService;
 import it.giacomos.android.osmer.network.DownloadManager;
 import it.giacomos.android.osmer.network.DownloadReason;
 import it.giacomos.android.osmer.network.DownloadStateListener;
 import it.giacomos.android.osmer.network.DownloadStatus;
 import it.giacomos.android.osmer.network.Data.DataPool;
 import it.giacomos.android.osmer.network.Data.DataPoolCacheUtils;
 import it.giacomos.android.osmer.network.Data.DataPoolErrorListener;
 import it.giacomos.android.osmer.network.state.BitmapType;
 import it.giacomos.android.osmer.network.state.StateName;
 import it.giacomos.android.osmer.network.state.Urls;
 import it.giacomos.android.osmer.network.state.ViewType;
 import it.giacomos.android.osmer.observations.MapMode;
 import it.giacomos.android.osmer.observations.ObservationType;
 import it.giacomos.android.osmer.observations.ObservationsCache;
 import it.giacomos.android.osmer.webcams.WebcamDataHelper;
 import it.giacomos.android.osmer.widgets.AnimatedImageView;
 import it.giacomos.android.osmer.widgets.OAnimatedTextView;
 import it.giacomos.android.osmer.widgets.map.MapViewMode;
 import it.giacomos.android.osmer.widgets.map.OMapFragment;
 import it.giacomos.android.osmer.widgets.map.RadarOverlayUpdateListener;
 import it.giacomos.android.osmer.pager.ActionBarManager;
 import it.giacomos.android.osmer.pager.DrawerItemClickListener;
 import it.giacomos.android.osmer.pager.MyActionBarDrawerToggle;
 import it.giacomos.android.osmer.pager.TabsAdapter;
 import it.giacomos.android.osmer.pager.ViewPagerPages;
 import it.giacomos.android.osmer.preferences.*;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnDismissListener;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.ViewFlipper;
 /** 
  * 
  * @author giacomo
  *
  * - download first image (today forecast) and situation text immediately and independently (i.e. in
  *   two AsyncTasks each other), so that the user obtains the first information as soon as possible;
  * - in the background, continue downloading all other relevant information (tomorrow, two days image and
  *   forecast), so that it is ready when the user flips.
  */
 public class OsmerActivity extends FragmentActivity 
 implements OnClickListener, 
 DownloadStateListener,
 SnapshotManagerListener,
 OnMenuItemClickListener, 
 OnDismissListener,
 MapFragmentListener,
 RadarOverlayUpdateListener,
 DataPoolErrorListener
 {
 	private final DownloadManager m_downloadManager;
 	private final DownloadStatus mDownloadStatus;
 	
 	public OsmerActivity()
 	{
 		super();
 		/* these are final */
 		/* Create a download status in INIT state */
 		mDownloadStatus = new DownloadStatus();
 		m_downloadManager = new DownloadManager(this, mDownloadStatus);
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		this.setProgressBarVisibility(true);
 
 
 		/* create the location update client and connect it to the location service */
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 		if(resultCode == ConnectionResult.SUCCESS)
 		{
 			mGoogleServicesAvailable = true;
 			setContentView(R.layout.main);
 			init();
 		}
 		else
 		{
 			mGoogleServicesAvailable = false;
 			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
 		}
 		
 	}
 
 	public void onResume()
 	{	
 		super.onResume();
 		if(!mGoogleServicesAvailable)
 			return;
 
 		m_downloadManager.onResume(this);
 	}
 
 	public void onPause()
 	{
 		super.onPause();
 		if(!mGoogleServicesAvailable)
 			return;
 		/* unregisters network status monitor broadcast receiver (for this it needs `this')
 		 */
 		m_downloadManager.onPause(this);
 		mLocationService.disconnect();
 	}
 
 	/**
 	 * When using the ActionBarDrawerToggle, you must call it during
 	 * onPostCreate() and onConfigurationChanged()...
 	 */
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		if(!mGoogleServicesAvailable)
 			return;
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 
 		mActionBarManager.init(savedInstanceState);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if(!mGoogleServicesAvailable)
 			return;
 		// Pass any configuration change to the drawer toggls
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	public void onStop()
 	{
 		/* From Android documentation:
 		 * Note that this method may never be called, in low memory situations where 
 		 * the system does not have enough memory to keep your activity's process running 
 		 * after its onPause() method is called. 
 		 */
 		super.onStop();
 		if(!mGoogleServicesAvailable)
 			return;
 	}
 
 	protected void onDestroy()
 	{
 		if(mGoogleServicesAvailable)
 		{
 			/* drawables are unbinded inside ForecastFragment's onDestroy() */
 			if(mRefreshAnimatedImageView != null)
 				mRefreshAnimatedImageView.hide();
 			/* cancel async tasks that may be running when the application is destroyed */
 			m_downloadManager.stopPendingTasks();
 			mDataPool.clear();
 		}
 		super.onDestroy();
 	}
 
 	public void onRestart()
 	{
 		super.onRestart();
 		if(!mGoogleServicesAvailable)
 			return;
 	}
 
 	public void onStart()
 	{
 		super.onStart();
 		if(!mGoogleServicesAvailable)
 			return;
 	}
 
 	public void init()
 	{
 		mCurrentViewType = ViewType.HOME;
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.pager);
 		
 		mLocationService = new LocationService(getApplicationContext(), mDownloadStatus);
 		/* Set the number of pages that should be retained to either side of 
 		 * the current page in the view hierarchy in an idle state
 		 */
 		//		mViewPager.setOffscreenPageLimit(3);
 		mMainLayout = (LinearLayout) findViewById(R.id.mainLayout);
 		mMainLayout.addView(mViewPager);
 
 		mSettings = new Settings(this);
 		mTapOnMarkerHintCount = 0;
 		mRefreshAnimatedImageView = null;
 
 		mDrawerItems = getResources().getStringArray(R.array.drawer_text_items);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		/* Action bar stuff.  */
 		mActionBarManager = new ActionBarManager(this);
 		/* Set the adapter for the list view */
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.drawer_list_item, mDrawerItems));
 
 		/* Set the list's click listener */
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this));
 		mTitle = getTitle();
 		mDrawerTitleOpen = getResources().getString(R.string.drawer_open);
 
 		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 
 		mDrawerToggle = new MyActionBarDrawerToggle(this, drawerLayout,
 				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
 		/*Set the drawer toggle as the DrawerListener */
 		drawerLayout.setDrawerListener(mDrawerToggle);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		mActionBarManager.drawerItemChanged(0);
 
 		mMenuActionsManager = null;
 		mCurrentLocation = null;
 
 		/* set html text on Radar info text view */
 		TextView radarInfoTextView = (TextView)findViewById(R.id.radarInfoTextView);
 		radarInfoTextView.setText(Html.fromHtml(getResources().getString(R.string.radar_info)));
 		radarInfoTextView.setVisibility(View.GONE);
 
 		m_observationsCache = new ObservationsCache();
 		/* map updates the observation data in ItemizedOverlay when new observations are available
 		 *
 		 */
 		OMapFragment map = (OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
 		m_observationsCache.installObservationsCacheUpdateListener(map);

		/* Create DataPool.  */
		mDataPool = new DataPool(this);
		mDataPool.registerErrorListener(this); /* listen for network errors */
		/* register observations cache on DataPool. Data initialization with DataPoolCacheUtils
		 * is done afterwards, inside onStart.
		 */
		mDataPool.registerTextListener(ViewType.DAILY_TABLE, m_observationsCache);
		mDataPool.registerTextListener(ViewType.LATEST_TABLE, m_observationsCache);
		
		/* download manager. Instantiated in the constructor because it's final */
		m_downloadManager.setDownloadListener(mDataPool);
 		
 		DataPoolCacheUtils dataPoolCacheUtils = new DataPoolCacheUtils();
 		
 		/* load cached tables at startup. map is updated because installed as a listener
 		 * above. Situation image will register as LatestObservationCacheChangeListener
 		 * in SituationFragment.onActivityCreated. It will be immediately notified
 		 * inside ObservationsCache.setLatestObservationCacheChangeListener.
 		 */
 		m_observationsCache.onTextChanged(dataPoolCacheUtils.loadFromStorage(ViewType.DAILY_TABLE, getApplicationContext()),
 				ViewType.DAILY_TABLE, true);
 		m_observationsCache.onTextChanged(dataPoolCacheUtils.loadFromStorage(ViewType.LATEST_TABLE, getApplicationContext()),
 				ViewType.LATEST_TABLE, true);
 		/* create the location update client and connect it to the location service */
 		mLocationService.connect();
 	}
 	
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) 
 	{
 		/* save refresh state of the refresh animated circular scrollbar in order to 
 		 * restore its state and visibility right after the menu is recreated.
 		 */
 		boolean refreshWasVisible = (mRefreshAnimatedImageView != null && 
 				mRefreshAnimatedImageView.getVisibility() == View.VISIBLE);
 		mRefreshAnimatedImageView = null;
 		mButtonsActionView = menu.findItem(R.id.actionbarmenu).getActionView();
 		mRefreshAnimatedImageView = (AnimatedImageView) mButtonsActionView.findViewById(R.id.refresh_animation);
 		if(refreshWasVisible)
 			mRefreshAnimatedImageView.start();
 		mInitButtonMapsOverflowMenu();
 
 		/* set visibility and state on map buttons */
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		// The action bar home/up action should open or close the drawer.
 		// ActionBarDrawerToggle will take care of this.
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		this.closeOptionsMenu();
 		if(mMenuActionsManager == null)
 			mMenuActionsManager = new MenuActionsManager(this);
 
 		boolean ret = mMenuActionsManager.itemSelected(item);
 		return ret;
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		if(!mGoogleServicesAvailable)
 			super.onBackPressed();
 		else
 		{
 			/* first of all, if there's a info window on the map, close it */
 			int displayedChild = ((ViewFlipper) findViewById(R.id.viewFlipper)).getDisplayedChild();
 			OMapFragment map = (OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
 			if(displayedChild == 1  &&  map.isInfoWindowVisible()) /* map view visible */
 			{
 				map.hideInfoWindow();
 			}
 			else if(displayedChild == 1)
 			{
 				mDrawerList.setItemChecked(0, true);
 				mActionBarManager.drawerItemChanged(0);
 			}
 			else
 				super.onBackPressed();
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(int id, Bundle args)
 	{
 		if(id == MenuActionsManager.TEXT_DIALOG)
 		{
 			// Use the Builder class for convenient dialog construction
 			Builder builder = new AlertDialog.Builder(this);
 			builder.setPositiveButton(getResources().getString(R.string.ok_button), null);
 			AlertDialog dialog = builder.create();
 			dialog.setTitle(args.getString("title"));
 			dialog.setCancelable(true);
 			dialog.setMessage(Html.fromHtml(args.getString("text")));
 			dialog.show();
 			((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
 
 		}
 		return super.onCreateDialog(id, args);
 	}
 
 	/*
 	 * Handle results returned to the FragmentActivity
 	 * by Google Play services
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		// Decide what to do based on the original request code
 		switch (requestCode) 
 		{
 		case LocationService.CONNECTION_FAILURE_RESOLUTION_REQUEST :
 			/*
 			 * If the result code is Activity.RESULT_OK, try
 			 * to connect again
 			 */
 			switch (resultCode) 
 			{
 			case Activity.RESULT_OK :
 				/*
 				 * Try the request again
 				 */
 				break;
 			}
 		}
 	}
 
 	public void onGoogleMapReady()
 	{
 
 	}
 	
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	@Override
 	public void networkStatusChanged(boolean online) {
 		// TODO Auto-generated method stub
 		TitlebarUpdater titlebarUpdater = new TitlebarUpdater();
 		titlebarUpdater.update(this);
 		titlebarUpdater = null;
 
 		if(!online)
 		{
 			Toast.makeText(getApplicationContext(), R.string.netUnavailableToast, Toast.LENGTH_LONG).show();	
 		}
 		else
 		{
 			MapViewUpdater currenvViewUpdater = new MapViewUpdater();
 			currenvViewUpdater.update(this);
 			currenvViewUpdater = null;
 
 			/* trigger an update of the locality if Location is available */
 			if(mLocationService.getCurrentLocation() != null)
 				mLocationService.updateGeocodeAddress();
 		}
 	}
 
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		SnapshotManager snapManager = new SnapshotManager();
 		snapManager.save(outState, this);
 		snapManager = null;
 	}
 
 	protected void onRestoreInstanceState(Bundle inState)
 	{
 		super.onRestoreInstanceState(inState);
 		/* restores from the bundle if the bundle creation timestamp is reasonably
 		 * close to current timestamp
 		 */
 		SnapshotManager snapManager = new SnapshotManager();
 		snapManager.restore(inState, this);
 		snapManager = null;
 	}
 
 	public void getSituation()
 	{
 		m_downloadManager.getSituation();
 	}
 
 	public void getTodayForecast()
 	{
 		m_downloadManager.getTodayForecast();
 	}
 
 	public void getTomorrowForecast()
 	{
 		m_downloadManager.getTomorrowForecast();
 	}
 
 	public void getTwoDaysForecast()
 	{
 		m_downloadManager.getTwoDaysForecast();
 	}
 
 	void radar()
 	{
 		m_downloadManager.getRadarImage();
 	}
 
 	void satellite()
 	{
 		//TextView textView = (TextView) findViewById(R.id.mainTextView);
 	}
 
 	void updateWbcamList()
 	{
 		WebcamDataHelper webcamDataHelper = new WebcamDataHelper();
 		if(webcamDataHelper.dataIsOld(getApplicationContext()))
 		{
 			Log.e("OsmerActivity.webcams()", "updating webcam list");
 			m_downloadManager.getWebcamList();
 		}
 		else
 			Log.e("OsmerActivity.webcams()", "not updating webcam list");
 	}
 
 	@Override
 	public void onDownloadProgressUpdate(int step, int total)
 	{
 		TitlebarUpdater tbu = new TitlebarUpdater();
 		tbu.update(this);
 		tbu = null;
 		double progressValue = ProgressBarParams.MAX_PB_VALUE * step /  total;
 		setProgress((int) progressValue);
 		ProgressBarParams.currentValue = progressValue;
 		if(mRefreshAnimatedImageView != null && ProgressBarParams.currentValue == ProgressBarParams.MAX_PB_VALUE)
 			mRefreshAnimatedImageView.hide(); /* stops and hides */
 		else if(mRefreshAnimatedImageView != null)
 			mRefreshAnimatedImageView.start();
 	}
 
 	@Override
 	public void onTextUpdateError(ViewType t, String errorMessage)
 	{
 		NetworkGuiErrorManager ngem = new NetworkGuiErrorManager();
 		ngem.onError(this, errorMessage);
 		ngem = null;
 	}
 
 	@Override
 	public void onBitmapUpdateError(BitmapType bType, String errorMessage)
 	{
 		NetworkGuiErrorManager ngem = new NetworkGuiErrorManager();
 		ngem.onError(this, errorMessage);
 		ngem = null;
 	}
 
 	@Override
 	public void onDownloadStart(DownloadReason reason)
 	{
 		if(reason == DownloadReason.Init)
 			Toast.makeText(getApplicationContext(), R.string.downloadingToast, Toast.LENGTH_SHORT).show();
 		else if(reason == DownloadReason.Incomplete)
 			Toast.makeText(getApplicationContext(), R.string.completingDownloadToast, Toast.LENGTH_SHORT).show();
 		else if(reason == DownloadReason.DataExpired)
 			Toast.makeText(getApplicationContext(), R.string.dataExpiredToast, Toast.LENGTH_SHORT).show();
 
 		setProgressBarVisibility(true);
 		ProgressBarParams.currentValue = 0;
 		setProgress(0);
 		if(mRefreshAnimatedImageView != null)
 			mRefreshAnimatedImageView.resetErrorFlag();
 	}
 
 	@Override
 	public void onStateChanged(long previousState, long state) 
 	{
 		if(((state & DownloadStatus.WEBCAM_OSMER_DOWNLOADED) != 0) &&  ((state & DownloadStatus.WEBCAM_OTHER_DOWNLOADED) != 0 ))
 			Toast.makeText(getApplicationContext(), R.string.webcam_lists_downloaded, Toast.LENGTH_SHORT).show();
 	}
 
 	void executeObservationTypeSelectionDialog(MapMode mapMode)
 	{
 		ObservationTypeGetter oTypeGetter = new ObservationTypeGetter();
 		oTypeGetter.get(this, mapMode, -1);
 		oTypeGetter = null;
 	}
 
 	public void onSelectionDone(ObservationType observationType, MapMode mapMode) 
 	{
 		/* switch the working mode of the map view. Already in PAGE_MAP view flipper page */
 		OMapFragment map = (OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
 		map.setMode(new MapViewMode(observationType, mapMode));
 		if(mapMode == MapMode.DAILY_OBSERVATIONS || mapMode == MapMode.LATEST_OBSERVATIONS)
 			map.updateObservations(m_observationsCache.getObservationData(mapMode));
 	}
 
 	@Override
 	public boolean onMenuItemClick(MenuItem menuItem) 
 	{
 		/* must manually check an unchecked item, and vice versa.
 		 * a checkbox or radio button does not change its state automatically.
 		 */
 		if(menuItem.isCheckable())
 			menuItem.setChecked(!menuItem.isChecked());
 		OMapFragment omv = (OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
 		switch(menuItem.getItemId())
 		{
 		case R.id.centerMapButton:
 			omv.centerMap();
 			break;	
 		case R.id.mapNormalViewButton:
 			omv.setNormalViewEnabled(menuItem.isChecked());
 			break;		
 		case R.id.satelliteViewButton:
 			omv.setSatEnabled(menuItem.isChecked());
 			break;			
 		case R.id.terrainViewButton:
 			omv.setTerrainEnabled(menuItem.isChecked()); 
 			break;
 		case R.id.radarInfoButton:
 			View radarInfoTextView = findViewById(R.id.radarInfoTextView);
 			if(menuItem.isChecked())
 				radarInfoTextView.setVisibility(View.VISIBLE);
 			else
 				radarInfoTextView.setVisibility(View.GONE);
 			break;
 		case R.id.measureToggleButton:
 			if(menuItem.isChecked() && mSettings.isMapMoveToMeasureHintEnabled())
 				Toast.makeText(getApplicationContext(), R.string.hint_move_to_measure_on_map, Toast.LENGTH_LONG).show();
 			omv.setMeasureEnabled(menuItem.isChecked());
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 
 		switch(v.getId())
 		{
 		case R.id.actionOverflow:
 			ToggleButton buttonMapsOveflowMenu = (ToggleButton) mButtonsActionView.findViewById(R.id.actionOverflow);
 			buttonMapsOveflowMenu.setChecked(true);
 			mCreateMapOptionsPopupMenu();
 			break;
 		default:
 			break;		
 		}
 	}
 
 	@Override
 	public void onDismiss(PopupMenu popupMenu) 
 	{
 		/* uncheck button when popup is dismissed */
 		ToggleButton buttonMapsOveflowMenu = (ToggleButton) mButtonsActionView.findViewById(R.id.actionOverflow);
 		buttonMapsOveflowMenu.setChecked(false);
 	} 
 
 	/* called by onPrepareOptionsMenu every time the action bar is recreated
 	 * @param buttonsActionView the button (view) where the menu is anchored.
 	 * 
 	 */
 	private void mInitButtonMapsOverflowMenu() 
 	{
 		if(mButtonsActionView == null || !mGoogleServicesAvailable)
 			return;
 
 		/* button for maps menu */
 		ToggleButton buttonMapsOveflowMenu = (ToggleButton) mButtonsActionView.findViewById(R.id.actionOverflow);
 		switch(mCurrentViewType)
 		{
 		case HOME:
 		case TODAY:
 		case TOMORROW:
 		case TWODAYS:
 			if(buttonMapsOveflowMenu != null)
 				buttonMapsOveflowMenu.setVisibility(View.GONE);
 			break;
 		default:
 			buttonMapsOveflowMenu.setOnClickListener(this);
 			buttonMapsOveflowMenu.setVisibility(View.VISIBLE);
 			break;
 		}
 	}
 
 	private void mCreateMapOptionsPopupMenu() 
 	{
 		OMapFragment map = (OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
 		ToggleButton buttonMapsOveflowMenu = (ToggleButton) mButtonsActionView.findViewById(R.id.actionOverflow);
 		PopupMenu mapOptionsMenu = new PopupMenu(this, buttonMapsOveflowMenu);
 		Menu menu = mapOptionsMenu.getMenu();
 		mapOptionsMenu.getMenuInflater().inflate(R.menu.map_options_popup_menu, menu);
 		menu.findItem(R.id.measureToggleButton).setVisible(mCurrentViewType == ViewType.RADAR);
 		menu.findItem(R.id.radarInfoButton).setVisible(mCurrentViewType == ViewType.RADAR);
 		menu.findItem(R.id.measureToggleButton).setChecked(map.isMeasureEnabled());
 		menu.findItem(R.id.radarInfoButton).setChecked(findViewById(R.id.radarInfoTextView).getVisibility() == View.VISIBLE);
 
 		switch(mCurrentViewType)
 		{
 		case HOME: case TODAY: case TOMORROW: case TWODAYS:
 			menu.findItem(R.id.satelliteViewButton).setVisible(false);
 			menu.findItem(R.id.terrainViewButton).setVisible(false);
 			menu.findItem(R.id.mapNormalViewButton).setVisible(false);
 			menu.findItem(R.id.centerMapButton).setVisible(false);
 			break;
 		default:
 			menu.findItem(R.id.satelliteViewButton).setVisible(true);
 			menu.findItem(R.id.terrainViewButton).setVisible(true);
 			menu.findItem(R.id.mapNormalViewButton).setVisible(true);
 			menu.findItem(R.id.centerMapButton).setVisible(true);
 			menu.findItem(R.id.satelliteViewButton).setChecked(map.isSatEnabled());
 			menu.findItem(R.id.terrainViewButton).setChecked(map.isTerrainEnabled());
 			menu.findItem(R.id.mapNormalViewButton).setChecked(map.isNormalViewEnabled());
 			break;
 		}
 
 		mapOptionsMenu.setOnMenuItemClickListener(this);
 		mapOptionsMenu.setOnDismissListener(this);
 		mapOptionsMenu.show();
 	}
 
 	public void switchView(ViewType id) 
 	{
 		mCurrentViewType = id;
 		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
 
 		if (id == ViewType.HOME) 
 		{
 			viewFlipper.setDisplayedChild(0);
 			mViewPager.setCurrentItem(ViewPagerPages.HOME);
 		} 
 		else if (id == ViewType.TODAY) 
 		{
 			viewFlipper.setDisplayedChild(0);
 			mViewPager.setCurrentItem(ViewPagerPages.TODAY);
 		} 
 		else if (id == ViewType.TOMORROW) 
 		{
 			viewFlipper.setDisplayedChild(0);
 			mViewPager.setCurrentItem(ViewPagerPages.TOMORROW);
 		} 
 		else if (id == ViewType.TWODAYS) 
 		{
 			viewFlipper.setDisplayedChild(0);
 			mViewPager.setCurrentItem(ViewPagerPages.TWODAYS);
 		} 
 		else if (id == ViewType.RADAR) 
 		{
 			viewFlipper.setDisplayedChild(1);
 			/* remove itemized overlays (observations), if present, and restore radar view */
 			((OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview)).setMode(new MapViewMode(ObservationType.NONE, MapMode.RADAR));
 		} 
 		else if (id == ViewType.ACTION_CENTER_MAP) 
 		{
 			((OMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview)).centerMap();
 		} 
 		else 
 		{
 			viewFlipper.setDisplayedChild(1);
 		}
 
 		if (id == ViewType.DAILY_SKY) {
 			onSelectionDone(ObservationType.SKY, MapMode.DAILY_OBSERVATIONS);
 			if(mSettings.isMapMarkerHintEnabled() && mTapOnMarkerHintCount == 0)
 			{
 				Toast.makeText(getApplicationContext(), R.string.hint_tap_on_map_markers, Toast.LENGTH_LONG).show();
 				mTapOnMarkerHintCount++; /* do not be too annoying */
 			}
 		} 
 		else if (id == ViewType.DAILY_HUMIDITY) 
 			onSelectionDone(ObservationType.AVERAGE_HUMIDITY, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_WIND_MAX) 
 			onSelectionDone(ObservationType.MAX_WIND, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_WIND) 
 			onSelectionDone(ObservationType.AVERAGE_WIND, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_RAIN) 
 			onSelectionDone(ObservationType.RAIN, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_MIN_TEMP) 
 			onSelectionDone(ObservationType.MIN_TEMP, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_MEAN_TEMP) 
 			onSelectionDone(ObservationType.AVERAGE_TEMP, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.DAILY_MAX_TEMP) 
 			onSelectionDone(ObservationType.MAX_TEMP, MapMode.DAILY_OBSERVATIONS);
 		else if (id == ViewType.LATEST_SKY) 
 			onSelectionDone(ObservationType.SKY, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_HUMIDITY) 
 			onSelectionDone(ObservationType.HUMIDITY, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_WIND) 
 			onSelectionDone(ObservationType.WIND, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_PRESSURE) 
 			onSelectionDone(ObservationType.PRESSURE, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_RAIN) 
 			onSelectionDone(ObservationType.RAIN, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_SEA) 
 			onSelectionDone(ObservationType.SEA, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_SNOW) 
 			onSelectionDone(ObservationType.SNOW, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.LATEST_TEMP) 
 			onSelectionDone(ObservationType.TEMP, MapMode.LATEST_OBSERVATIONS);
 		else if (id == ViewType.WEBCAM) 
 			onSelectionDone(ObservationType.NONE, MapMode.WEBCAM);
 		/* try to download only if online */
 		if(m_downloadManager.state().name() == StateName.Online)
 		{
 			if(id == ViewType.RADAR)
 				radar(); /* map mode has already been set if type is MAP or RADAR */
 			else if(id == ViewType.TODAY)
 				getTodayForecast();
 			else if(id == ViewType.TOMORROW)
 				getTomorrowForecast();
 			else if(id == ViewType.TWODAYS)
 				getTwoDaysForecast();
 			else if(id == ViewType.HOME)
 			{ }
 			else if(id == ViewType.WEBCAM)
 				updateWbcamList();
 		}
 
 		TitlebarUpdater titleUpdater = new TitlebarUpdater();
 		titleUpdater.update(this);
 		titleUpdater = null;
 
 		/* show or hide maps menu button according to the current view type */
 		mInitButtonMapsOverflowMenu();
 	}
 
 	public DownloadManager stateMachine() { return m_downloadManager; }
 
 	public ObservationsCache getObservationsCache()
 	{
 		return m_observationsCache;
 	}
 
 	public ViewPager getViewPager()
 	{
 		return mViewPager;
 	}
 
 	public Location getCurrentLocation()
 	{
 		return mCurrentLocation;
 	}
 
 	public  String[] getDrawerItems()
 	{
 		return mDrawerItems;
 	}
 
 	public ListView getDrawerListView()
 	{
 		return mDrawerList;
 	}
 
 	public ActionBarManager getActionBarPersonalizer()
 	{
 		return mActionBarManager;
 	}
 
 	public AnimatedImageView getRefreshAnimatedImageView()
 	{
 		return mRefreshAnimatedImageView;
 	}
 
 	public DataPool getDataPool()
 	{
 		return mDataPool;
 	}
 	
 	public DownloadStatus getDownloadStatus()
 	{
 		return mDownloadStatus;
 	}
 	
 	public LocationService getLocationService()
 	{
 		return mLocationService;
 	}
 	
 	@Override
 	public void onRadarImageUpdated() 
 	{
 		Settings settings = new Settings(this);
 		long radarTimestampMillis = settings.getRadarImageTimestamp();
 		long currentTimestampMillis = System.currentTimeMillis();
 		CharSequence text = new RadarImageTimestampTextBuilder().buildText(currentTimestampMillis, 
 				radarTimestampMillis, getResources(), mSettings.isFirstExecution());
 		OAnimatedTextView radarTimestampText = (OAnimatedTextView) findViewById(R.id.radarTimestampTextView);
 		if(radarTimestampText != null)
 			radarTimestampText.setText(text);
 	}
 
 
 	/* private members */
 	int mTapOnMarkerHintCount;
 	private Location mCurrentLocation;
 	private ObservationsCache m_observationsCache;
 	private MenuActionsManager mMenuActionsManager;
 	private Settings mSettings;
 
 	private ListView mDrawerList;
 	private String[] mDrawerItems;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private CharSequence mTitle, mDrawerTitleOpen;
 	private ActionBarManager mActionBarManager;
 	private AnimatedImageView mRefreshAnimatedImageView;
 	private ViewType mCurrentViewType;
 	/* ActionBar menu button and menu */
 	private View mButtonsActionView;
 	private boolean mGoogleServicesAvailable;
 	Urls m_urls;
 
 	private DataPool mDataPool;
 	private LocationService mLocationService;
 
 	ViewPager mViewPager;
 	TabsAdapter mTabsAdapter;
 	LinearLayout mMainLayout;
 
 
 	int availCnt = 0;
 }
