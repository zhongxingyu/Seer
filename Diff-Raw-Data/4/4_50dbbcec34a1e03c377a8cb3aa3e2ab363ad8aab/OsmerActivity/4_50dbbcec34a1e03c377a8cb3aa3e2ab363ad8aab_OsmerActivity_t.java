 package it.giacomos.android.osmer;
 import com.google.android.maps.MapActivity;
 
 import it.giacomos.android.osmer.R.id;
 import it.giacomos.android.osmer.downloadManager.DownloadManager;
 import it.giacomos.android.osmer.downloadManager.DownloadReason;
 import it.giacomos.android.osmer.downloadManager.DownloadStatus;
 import it.giacomos.android.osmer.downloadManager.state.StateName;
 import it.giacomos.android.osmer.downloadManager.state.Urls;
 import it.giacomos.android.osmer.guiHelpers.ButtonListenerInstaller;
 import it.giacomos.android.osmer.guiHelpers.ImageViewUpdater;
 import it.giacomos.android.osmer.guiHelpers.MenuActionsManager;
 import it.giacomos.android.osmer.guiHelpers.NetworkGuiErrorManager;
 import it.giacomos.android.osmer.guiHelpers.ObservationTypeGetter;
 import it.giacomos.android.osmer.guiHelpers.OnTouchListenerInstaller;
 import it.giacomos.android.osmer.guiHelpers.TextViewUpdater;
 import it.giacomos.android.osmer.guiHelpers.TitlebarUpdater;
 import it.giacomos.android.osmer.guiHelpers.ToggleButtonGroupHelper;
 import it.giacomos.android.osmer.instanceSnapshotManager.SnapshotManager;
 import it.giacomos.android.osmer.locationUtils.GeocodeAddressTask;
 import it.giacomos.android.osmer.locationUtils.GeocodeAddressUpdateListener;
 import it.giacomos.android.osmer.locationUtils.LocationComparer;
 import it.giacomos.android.osmer.locationUtils.LocationInfo;
 import it.giacomos.android.osmer.observations.ObservationTime;
 import it.giacomos.android.osmer.observations.ObservationType;
 import it.giacomos.android.osmer.observations.ObservationsCache;
 import it.giacomos.android.osmer.widgets.InfoHtmlBuilder;
 import it.giacomos.android.osmer.widgets.LocationToImgPixelMapper;
 import it.giacomos.android.osmer.widgets.ODoubleLayerImageView;
 import it.giacomos.android.osmer.widgets.OViewFlipper;
 import it.giacomos.android.osmer.widgets.mapview.OMapView;
 import it.giacomos.android.osmer.widgets.mapview.MapViewMode;
 import it.giacomos.android.osmer.widgets.SituationImage;
 import it.giacomos.android.osmer.preferences.*;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.graphics.Bitmap;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
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
 public class OsmerActivity extends MapActivity 
 implements OnClickListener, 
 DownloadUpdateListener,
 FlipperChildChangeListener,
 SnapshotManagerListener,
 OMapViewEventListener,
 LocationListener,
 GeocodeAddressUpdateListener
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		/* left icon for logo, right for connection status */
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.main);
 		init();
 	}
 
 	public void onResume()
 	{	
 		super.onResume();
 		
 		/* registers network status monitor broadcast receiver (for this it needs `this')
 		 * Must be called _after_ instance restorer's onResume!
 		 */
 		m_downloadManager.onResume(this);		
 		
 		/* Location Manager */
 		OMapView omv = (OMapView)findViewById(R.id.mapview);
 		omv.onResume();
 		m_locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
 		/*  (String provider, long minTime, float minDistance, LocationListener listener)
 		 * 5000 the LocationManager could potentially rest for minTime milliseconds between location updates to conserve power.
 		 * If minDistance is greater than 0, a location will only be broadcasted if the device moves by minDistance meters.
 		 */
 		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 50, this);
 		m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, this);
 	}
 	
 	public void onPause()
 	{
 		super.onPause();
 		/* unregisters network status monitor broadcast receiver (for this it needs `this')
 		 */
 		m_downloadManager.onPause(this);
 		((OMapView) findViewById(R.id.mapview)).onPause();
 		m_locationManager.removeUpdates(this);
 	}
 		
 	public void onStop()
 	{
 		super.onStop();
 		m_observationsCache.saveLatestToStorage(this);
 		new InternalStorageSaver(this);
 	}
 	
 	public void onRestart()
 	{
 		super.onRestart();
 	}
 	
 	public void onStart()
 	{
 		super.onStart();
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		this.closeOptionsMenu();
 		if(mMenuActionsManager == null)
 			mMenuActionsManager = new MenuActionsManager(this);
 		
 		boolean ret = mMenuActionsManager.itemSelected(item);
 		return ret;
 	}
 	
 	@Override
 	public void onBackPressed()
 	{
 		ViewFlipper buttonFlipper = (ViewFlipper) findViewById(R.id.buttonsFlipper);
 		int displayedChild = buttonFlipper.getDisplayedChild();
 		switch(displayedChild)
 		{
 		case 1: /* sat, daily, last obs: just as clicking home */
 			this.onClick(findViewById(R.id.buttonHome));
 			break;
 		case 2: /* daily observations, like pressing fvg button to go back to the satellite */
 			this.onClick(findViewById(R.id.buttonMapInsideDaily));
 			break;
 		case 3:
 			this.onClick(findViewById(R.id.buttonMapInsideLatest));
 			break;
 		default:
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
 
 	
 	public void init()
 	{
 		OMapView map = (OMapView) findViewById(R.id.mapview);
 		map.setMapViewEventListener(this);
 		/* install listeners on buttons */
 		installButtonListener();
 		installOnTouchListener();
 		((OViewFlipper) findViewById(R.id.viewFlipper1)).setOnChildPageChangedListener(this);
 		m_downloadManager = new DownloadManager(this);
 		m_observationsCache = new ObservationsCache();
 		/* map updates the observation data in ItemizedOverlay when new observations are available
 		 *
 		 */
 		m_observationsCache.installObservationsCacheUpdateListener(map);
 		
 		SituationImage situationImage = (SituationImage) findViewById(R.id.homeImageView);
 		/* Situation Image will listen for cache changes, which happen on store() call
 		 * in this class or when cache is restored from the internal storage.
 		 */
 		m_observationsCache.setLatestObservationCacheChangeListener(situationImage);
 		/* Before calling onResume on download manager */
 		m_observationsCache.restoreFromStorage(this);
 		new FromInternalStorageRestorer(this);
 		mSettings = new Settings(this);
 		mMenuActionsManager = null;
 		mSwipeHintCount = mTapOnMarkerHintCount = 0;
 		mCurrentLocation = null;
 	}	
 
 
 	@Override
 	public void networkStatusChanged(boolean online) {
 		// TODO Auto-generated method stub
 		new TitlebarUpdater(this);
 		if(!online)
 			Toast.makeText(this, R.string.netUnavailableToast, Toast.LENGTH_LONG).show();
 		else
 		{
 			new CurrentViewUpdater(this);
 			/* update locality if Location is available */
 			if(mCurrentLocation != null)
 				new GeocodeAddressTask(this.getApplicationContext(), this).execute(mCurrentLocation);
 		}
 	}
 	
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		new SnapshotManager().save(outState, this);
 		mToggleButtonGroupHelper.saveButtonsState(outState);
 	}
 	
 	protected void onRestoreInstanceState(Bundle inState)
 	{
 		super.onRestoreInstanceState(inState);
 		/* restores from the bundle if the bundle creation timestamp is reasonably
 		 * close to current timestamp
 		 */
 		new SnapshotManager().restore(inState, this);
 		mToggleButtonGroupHelper.restoreButtonsState(inState);
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
 	
 	@Override
 	public void onDownloadProgressUpdate(int step, int total)
 	{
 		new TitlebarUpdater(this);
 		setProgress((int) (ProgressBarParams.MAX_PB_VALUE * step /  total));
 	}
 	
 	@Override
 	public void onTextUpdate(String txt, StringType t)
 	{
 		switch(t)
 		{
 		case HOME: case TODAY: case TOMORROW: case TWODAYS:
 			new TextViewUpdater(this, txt, t);
 		break;
 		default:
 			/* situation image will be updated directly by the cache, since SituationImage is 
 			 * listening to the ObservationCache through the LatestObservationCacheChangeListener
 			 * interface.
 			 */
 			m_observationsCache.store(txt, t);
 		break;
 		}
 	}
 	
 	@Override
 	public void onDownloadStart(DownloadReason reason)
 	{
 		if(reason == DownloadReason.Init)
 			Toast.makeText(this, R.string.downloadingToast, Toast.LENGTH_SHORT).show();
 		else if(reason == DownloadReason.Incomplete)
 			Toast.makeText(this, R.string.completingDownloadToast, Toast.LENGTH_SHORT).show();
 		else if(reason == DownloadReason.DataExpired)
 			Toast.makeText(this, R.string.dataExpiredToast, Toast.LENGTH_SHORT).show();
 		
 		setProgressBarVisibility(true);
 		ProgressBarParams.currentValue = 0;
 		setProgress(0);
 	}
 	
 	@Override
 	public void onTextUpdateError(StringType t, String errorMessage)
 	{
 		String text = new InfoHtmlBuilder().wrapErrorIntoHtml(errorMessage, getResources());
 		new TextViewUpdater(this, text, t);
 		new NetworkGuiErrorManager(this, errorMessage);
 	}
 	
 	@Override
 	public void onBitmapUpdate(Bitmap bmp, BitmapType bType)
 	{
 		new ImageViewUpdater(this, bmp, bType);
 	}
 	
 	@Override
 	public void onBitmapUpdateError(BitmapType bType, String errorMessage)
 	{
 		new NetworkGuiErrorManager(this, errorMessage);
 	}
 	
 	/* executed when a new locality / address becomes available.
 	 */
 	public void onGeocodeAddressUpdate(LocationInfo locInfo)
 	{
 		if(locInfo.error.isEmpty())
 		{
 			((ODoubleLayerImageView) findViewById(R.id.homeImageView)).onLocalityChanged(locInfo.locality, locInfo.subLocality, locInfo.address);
 			((ODoubleLayerImageView) findViewById(R.id.todayImageView)).onLocalityChanged(locInfo.locality, locInfo.subLocality, locInfo.address);
 			((ODoubleLayerImageView) findViewById(R.id.tomorrowImageView)).onLocalityChanged(locInfo.locality, locInfo.subLocality, locInfo.address);
 			((ODoubleLayerImageView) findViewById(R.id.twoDaysImageView)).onLocalityChanged(locInfo.locality, locInfo.subLocality, locInfo.address);
 		}
 	}
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		LocationComparer locationComparer = new LocationComparer();
 		if(locationComparer.isBetterLocation(location, mCurrentLocation))
 		{	
 			((ODoubleLayerImageView) findViewById(R.id.homeImageView)).onLocationChanged(location);
 			((ODoubleLayerImageView) findViewById(R.id.todayImageView)).onLocationChanged(location);
 			((ODoubleLayerImageView) findViewById(R.id.tomorrowImageView)).onLocationChanged(location);
 			((ODoubleLayerImageView) findViewById(R.id.twoDaysImageView)).onLocationChanged(location);
 			mCurrentLocation = location;
 			if(this.m_downloadManager.state().name() == StateName.Online)
 				new GeocodeAddressTask(this.getApplicationContext(), this).execute(mCurrentLocation);
 		}
 	}
 	
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub		
 	}
 	
 	/** Installs listeners for the button click events 
 	 * 
 	 */
 	protected void installButtonListener()
 	{
 		mToggleButtonGroupHelper = new ToggleButtonGroupHelper(this);
 		new ButtonListenerInstaller(this, mToggleButtonGroupHelper);
 	}
 	
 	protected void installOnTouchListener()
 	{
 		new OnTouchListenerInstaller(this);
 	}
 
 	void executeObservationTypeSelectionDialog(ObservationTime oTime)
 	{
 		new ObservationTypeGetter(this, oTime, -1);
 	}
 	
 
 	@Override
 	public void onSatelliteEnabled(boolean en) {
 				
 	}
 
 	@Override
 	public void onMeasureEnabled(boolean en) {
 		ToggleButton b = (ToggleButton) findViewById(R.id.measureToggleButton);
 		b.setChecked(en);
 	}
 
 	public void onSelectionDone(ObservationType type, ObservationTime oTime) 
 	{
 		/* switch the working mode of the map view */
 		OMapView map = (OMapView) findViewById(R.id.mapview);
 		map.setMode(new MapViewMode(type, oTime));
 		map.updateObservations(m_observationsCache.getObservationData(oTime));
 	}
 
 	/* (non-Javadoc)
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		Button b = (Button) v;
 		OViewFlipper viewFlipper = (OViewFlipper) this.findViewById(R.id.viewFlipper1);
		viewFlipper.setOutAnimation(null);
		viewFlipper.setInAnimation(null);
 
 		ViewFlipper buttonsFlipper = (ViewFlipper) findViewById(R.id.buttonsFlipper);
 		
 		//if(!mToggleButtonGroupHelper.isOn(v.getId()))
 		{
 			/* if not already checked, changed flipper page */
 			/* change flipper child. Title is updated by the FlipperChildChangeListener */
 			switch(b.getId())
 			{
 			case R.id.buttonHome:
 				viewFlipper.setDisplayedChild(FlipperChildren.HOME);
 				buttonsFlipper.setDisplayedChild(0);
 				/* set checked  the clicked button */
 				mToggleButtonGroupHelper.setClicked(b);
 				break;
 			case R.id.buttonToday:
 				viewFlipper.setDisplayedChild(FlipperChildren.TODAY);
 				mToggleButtonGroupHelper.setClicked(b);
 				break;
 			case R.id.buttonTomorrow:
 				viewFlipper.setDisplayedChild(FlipperChildren.TOMORROW);
 				mToggleButtonGroupHelper.setClicked(b);
 				break;
 			case R.id.buttonTwoDays:
 				viewFlipper.setDisplayedChild(FlipperChildren.TWODAYS);
 				mToggleButtonGroupHelper.setClicked(b);
 				break;
 			case R.id.buttonMap:
 			case R.id.buttonMapInsideDaily:
 				((ToggleButton)findViewById(R.id.measureToggleButton)).setChecked(false);
 				/* remove itemized overlays (observations), if present, and restore radar view */
 				((OMapView) findViewById(R.id.mapview)).setMode(new MapViewMode(ObservationType.RADAR, ObservationTime.DAILY));
 				viewFlipper.setDisplayedChild(FlipperChildren.MAP);
 				buttonsFlipper.setDisplayedChild(1);
 //				/* do not set the map button clicked, but the radar one 
 //				 * because the buttonsFlipper child has changed.
 //				 */
 //				mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonRadar));
 				break;
 			case R.id.buttonMapInsideLatest:
 				((ToggleButton)findViewById(R.id.measureToggleButton)).setChecked(false);
 				/* remove itemized overlays (observations), if present, and restore radar view */
 				((OMapView) findViewById(R.id.mapview)).setMode(new MapViewMode(ObservationType.RADAR, ObservationTime.LATEST));
 				viewFlipper.setDisplayedChild(FlipperChildren.MAP);
 				buttonsFlipper.setDisplayedChild(1);
 //				/* do not set the map button clicked, but the radar one 
 //				 * because the buttonsFlipper child has changed.
 //				 */
 //				mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonRadar));
 				break;
 			case R.id.measureToggleButton:
 			case R.id.satelliteViewButton:
 				/* no call to mToggleButtonGroupHelper.setClicked(b); */
 				break;
 			default:
 				mToggleButtonGroupHelper.setClicked(b);
 				break;
 			}
 			
 			/* hints on buttons */
 			switch(b.getId())
 			{
 			case R.id.buttonHome:
 			case R.id.buttonToday:
 			case R.id.buttonTomorrow:	
 			case R.id.buttonTwoDays:
 				if(mSettings.isSwipeHintEnabled() && mSwipeHintCount == 0)
 				{
 					Toast.makeText(this, R.string.hint_swipe, Toast.LENGTH_LONG).show();
 					mSwipeHintCount++; /* don't be silly, just once per start */
 				}
 				break;
 				
 			case R.id.buttonDailySky:
 			case R.id.buttonHumMean:
 			case R.id.buttonWMax:
 			case R.id.buttonWMean:
 			case R.id.buttonDailyRain:	
 			case R.id.buttonTMin:	
 			case R.id.buttonTMax:
 			case R.id.buttonLatestSky:
 			case R.id.buttonHumidity:
 			case R.id.buttonWind:
 			case R.id.buttonPressure:
 			case R.id.buttonLatestRain:	
 			case R.id.buttonSnow:	
 			case R.id.buttonTemp:
 				if(mSettings.isObsScrollIconsHintEnabled())
 				{
 					/* this is shown just once */
 					Toast.makeText(this, R.string.hint_scroll_buttons, Toast.LENGTH_LONG).show();
 					mSettings.setObsScrollIconsHintEnabled(false);
 				}
 					
 				break;
 			default:
 				break;	
 			}
 		}
 		
 		
 		/* buttons that can have effect even offline */
 		switch(b.getId())
 		{
 		case R.id.buttonDailySky:
 			onSelectionDone(ObservationType.SKY, ObservationTime.DAILY);
 			break;
 		case R.id.buttonHumMean:
 			Log.e("onClick", "buttonHumMean");
 			onSelectionDone(ObservationType.MEAN_HUMIDITY, ObservationTime.DAILY);
 			break;
 		case R.id.buttonWMax:
 			onSelectionDone(ObservationType.MAX_WIND, ObservationTime.DAILY);
 			break;
 		case R.id.buttonWMean:
 			onSelectionDone(ObservationType.MEAN_WIND, ObservationTime.DAILY);
 			break;
 		case R.id.buttonDailyRain:
 			onSelectionDone(ObservationType.RAIN, ObservationTime.DAILY);
 			break;
 		case R.id.buttonTMin:
 			onSelectionDone(ObservationType.MIN_TEMP, ObservationTime.DAILY);
 			break;
 		case R.id.buttonTMean:
 			onSelectionDone(ObservationType.MEAN_TEMP, ObservationTime.DAILY);
 			break;
 		case R.id.buttonTMax:
 			onSelectionDone(ObservationType.MAX_TEMP, ObservationTime.DAILY);
 			break;
 			
 		/* latest */
 		case R.id.buttonLatestSky:
 			onSelectionDone(ObservationType.SKY, ObservationTime.LATEST);
 			break;
 		case R.id.buttonHumidity:
 			onSelectionDone(ObservationType.HUMIDITY, ObservationTime.LATEST);
 			break;
 		case R.id.buttonWind:
 			onSelectionDone(ObservationType.WIND, ObservationTime.LATEST);
 			break;
 		case R.id.buttonPressure:
 			onSelectionDone(ObservationType.PRESSURE, ObservationTime.LATEST);
 			break;
 		case R.id.buttonLatestRain:
 			onSelectionDone(ObservationType.RAIN, ObservationTime.LATEST);
 			break;
 		case R.id.buttonSea:
 			onSelectionDone(ObservationType.SEA, ObservationTime.LATEST);
 			break;
 		case R.id.buttonSnow:
 			onSelectionDone(ObservationType.SNOW, ObservationTime.LATEST);
 			break;
 		case R.id.buttonTemp:
 			onSelectionDone(ObservationType.TEMP, ObservationTime.LATEST);
 			break;
 			
 			/* satellite or map on MapView */
 		case R.id.satelliteViewButton:
 			OMapView omv = (OMapView) findViewById(R.id.mapview);
 			omv.setSatellite(((ToggleButton)b).isChecked());
 			break;
 			
 		case R.id.measureToggleButton:
 			OMapView omv1 = (OMapView) findViewById(R.id.mapview);
 			boolean buttonChecked = ((ToggleButton)b).isChecked();
 			
 			if(buttonChecked && mSettings.isMapMoveToMeasureHintEnabled())
 			{
 				Toast.makeText(this, R.string.hint_move_to_measure_on_map, Toast.LENGTH_LONG).show();
 			}
 			else if(buttonChecked && !mSettings.isMapMoveToMeasureHintEnabled() 
 					&& mSettings.isMapMoveLocationToMeasureHintEnabled())
 			{
 				Toast.makeText(this, R.string.hint_move_location_to_measure_on_map, Toast.LENGTH_LONG).show();
 			}
 			omv1.setMeasureEnabled(buttonChecked);
 			break;	
 		}
 		
 		/* 
 		 * FIXME 
 		 * daily and latest observations are cached in the ObservationCache, so it is 
 		 * not compulsory to be online to show them.
 		 */
 		if(b.getId() == R.id.buttonDailyObs)
 		{
 			buttonsFlipper.setDisplayedChild(2);
 			onSelectionDone(ObservationType.SKY, ObservationTime.DAILY);
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonDailySky));
 			if(mSettings.isMapMarkerHintEnabled() && mTapOnMarkerHintCount == 0)
 			{
 				Toast.makeText(this, R.string.hint_tap_on_map_markers, Toast.LENGTH_LONG).show();
 				mTapOnMarkerHintCount++; /* do not be too annoying */
 			}
 		}
 		else if(b.getId() == R.id.buttonLastObs)
 		{
 			buttonsFlipper.setDisplayedChild(3);
 			onSelectionDone(ObservationType.SKY, ObservationTime.LATEST);
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonLatestSky));
 		}
 		/* try to download only if online */
 		else if(m_downloadManager.state().name() == StateName.Online)
 		{
 			if(b.getId() ==  R.id.buttonMap || b.getId() == R.id.buttonMapInsideDaily
 					|| b.getId() == R.id.buttonMapInsideLatest)
 			{
 		//		int id = mToggleButtonGroupHelper.buttonOn();
 			//	if(id == R.id.buttonRadar)
 					radar();
 			}
 			else if(b.getId() == R.id.buttonRadar)
 				radar();
 			else if(b.getId() == R.id.buttonToday)
 			{
 				getTodayForecast();
 			}
 			else if(b.getId() == R.id.buttonTomorrow)
 			{
 				getTomorrowForecast();
 			}
 			else if(b.getId() == R.id.buttonTwoDays)
 			{
 				getTwoDaysForecast();
 			}
 			else if(b.getId() == R.id.buttonHome)
 			{
 				
 			}
 		}
 		
 		ToggleButton measureButton = (ToggleButton) findViewById(R.id.measureToggleButton);
 		if(mToggleButtonGroupHelper.isOn(R.id.buttonRadar))
 			measureButton.setVisibility(View.VISIBLE);
 		else
 			measureButton.setVisibility(View.GONE);
 		
 		new TitlebarUpdater(this);
 	}
 	
 	public DownloadManager stateMachine() { return m_downloadManager; }
 	
 	public ToggleButtonGroupHelper getToggleButtonGroupHelper() 
 	{
 		return mToggleButtonGroupHelper;
 	}
 	
 	@Override
 	public void onFlipperChildChangeEvent(int child) {
 		// TODO Auto-generated method stub
 		new TitlebarUpdater(this);
 		switch(child)
 		{
 		case FlipperChildren.HOME:
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonHome));
 			break;
 		case FlipperChildren.TODAY:
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonToday));
 			break;
 		case FlipperChildren.TOMORROW:
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonTomorrow));
 			break;
 		case FlipperChildren.TWODAYS:
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonTwoDays));
 			break;
 		case FlipperChildren.MAP:
 			ViewFlipper buttonsFlipper = (ViewFlipper) findViewById(R.id.buttonsFlipper);
 			buttonsFlipper.setDisplayedChild(1);
 			mToggleButtonGroupHelper.setClicked(findViewById(R.id.buttonRadar));
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* private members */
 	private DownloadManager m_downloadManager;
 	private LocationManager m_locationManager; 
 	private Location mCurrentLocation;
 	private ObservationsCache m_observationsCache;
 	private MenuActionsManager mMenuActionsManager;
 	ToggleButtonGroupHelper mToggleButtonGroupHelper;
 	private Settings mSettings;
 	private int mSwipeHintCount, mTapOnMarkerHintCount;
 	
 	
 	Urls m_urls;
 	
 	/// temp
 	int availCnt = 0;
 
 	
 }
