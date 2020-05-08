 package com.pwr.zpi;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.speech.tts.TextToSpeech;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.pwr.zpi.database.entity.Workout;
 import com.pwr.zpi.dialogs.ErrorDialogFragment;
 import com.pwr.zpi.dialogs.MyDialog;
 import com.pwr.zpi.listeners.GestureListener;
 import com.pwr.zpi.listeners.MyGestureDetector;
 import com.pwr.zpi.services.LocationService;
 
 public class MainScreenActivity extends FragmentActivity implements GestureListener {
 	
 	public static final boolean REDUCED_VERSION = false;
 	
 	private RunListenerApi api;
 	private static final String TAG = MainScreenActivity.class.getSimpleName();
 	private TextView GPSStatusTextView;
 	private TextView GPSSignalTextView;
 	private TextView workoutNameTextView;
 	private ImageButton settingsButton;
 	private ImageButton historyButton;
 	private ImageButton planningButton;
 	private Button startButton;
 	private Button musicButton;
 	
 	private boolean isServiceConnected;
 	private int gpsStatus = -1;
 	private View mCurrent;
 	private Handler handler;
 	private Workout workout;
 	private GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	
 	public static final short GPS_NOT_ENABLED = 0;
 	public static final short NO_GPS_SIGNAL = 1;
 	public static final short GPS_WORKING = 2;
 	
 	private static final short LEFT = 0;
 	private static final short RIGHT = 1;
 	private static final short UP = 2;
 	private static final short DOWN = 3;
 	
 	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 	public static final int TTS_DATA_CHECK_CODE_REQUEST = 0x1;
 	public final static int REQUEST_GOOGLE_PLAY_SERVICES = 7;
 	public static final int WORKOUT_REQUEST = 0x2;
 	
 	public boolean isConnected;
 	private Location mLastLocation;
 	// service data
 	boolean mIsBound;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handler = new Handler();
 		setContentView(R.layout.main_screen_activity);
 		isServiceConnected = false;
 		GPSStatusTextView = (TextView) findViewById(R.id.textViewGPSIndicator);
 		GPSSignalTextView = (TextView) findViewById(R.id.GPSSignalTextView);
 		settingsButton = (ImageButton) findViewById(R.id.buttonSettings);
 		historyButton = (ImageButton) findViewById(R.id.buttonHistory);
 		planningButton = (ImageButton) findViewById(R.id.buttonPlans);
 		startButton = (Button) findViewById(R.id.buttonStart);
 		musicButton = (Button) findViewById(R.id.buttonMusic);
 		workoutNameTextView = (TextView) findViewById(R.id.textViewMainScreenWorkout);
 		
 		// locationListener = new MyLocationListener(this);
 		
 		prepareGestureListener();
 		addListeners();
 		doStartService();
 		doBindService();
 		// locationListener.getmLocationClient().connect();
 		
 		isConnected = false;
 	}
 	
 	private void checkSpeechSynthezator() {
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_DATA_CHECK_CODE_REQUEST);
 	}
 	
 	private void addListeners() {
 		GPSStatusTextView.setOnTouchListener(gestureListener);
 		settingsButton.setOnTouchListener(gestureListener);
 		historyButton.setOnTouchListener(gestureListener);
 		startButton.setOnTouchListener(gestureListener);
 		planningButton.setOnTouchListener(gestureListener);
 		musicButton.setOnTouchListener(gestureListener);
 	}
 	
 	private void prepareGestureListener() {
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector(this,
 			true, true, true, true));
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mCurrent = v;
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (!mIsBound) {
 			doBindService();
 		}
 		else
 		{
 			try {
 				if (isServiceConnected) {
 					gpsStatus = api.getGPSStatus();
 					handleGPSStatusChange();
 				}
 			}
 			catch (RemoteException e) {
 				Log.w(TAG, "Failed to get gpsStatus ", e);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		try {
 			if (isServiceConnected) {
 				gpsStatus = api.getGPSStatus();
 			}
 			
 		}
 		catch (RemoteException e) {
 			Log.w(TAG, "Failed to get gpsStatus ", e);
 		}
 		handleGPSStatusChange();
 		//	askForGpsStatus();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		//		askForGpsStatus();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureListener.onTouch(mCurrent, event);
 	}
 	
 	private void startActivity(Class<? extends Activity> activity,
 		short swipeDirection) {
 		Intent i = new Intent(MainScreenActivity.this, activity);
 		if (swipeDirection == RIGHT) { //get workout
 			startActivityForResult(i, WORKOUT_REQUEST);
 		}
 		else {
 			if (swipeDirection == DOWN) {
				if (workout != null) {
					i.putExtra(Workout.TAG, workout);
				}
 			}
 			startActivity(i);
 		}
 		switch (swipeDirection)
 		{
 			case RIGHT:
 				overridePendingTransition(R.anim.in_right_anim,
 					R.anim.out_right_anim);
 				break;
 			case LEFT:
 				overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 				break;
 			case DOWN:
 				overridePendingTransition(R.anim.in_down_anim, R.anim.out_down_anim);
 				break;
 			case UP:
 				overridePendingTransition(R.anim.in_up_anim, R.anim.out_up_anim);
 				break;
 		}
 	}
 	
 	@Override
 	public void onLeftToRightSwipe() {
 		startActivity(PlaningActivity.class, RIGHT);
 		
 	}
 	
 	@Override
 	public void onRightToLeftSwipe() {
 		startActivity(HistoryActivity.class, LEFT);
 		
 	}
 	
 	@Override
 	public void onUpToDownSwipe() {
 		startActivityIfPossible();
 		
 	}
 	
 	@Override
 	public void onDownToUpSwipe() {
 		startActivity(SettingsActivity.class, UP);
 		
 	}
 	
 	private void startSystemMusicPlayer() {
 		Intent i;
 		i = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
 		startActivity(i);
 	}
 	
 	/*
 	 * Handle results returned to the FragmentActivity by Google Play services
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Decide what to do based on the original request code
 		switch (requestCode) {
 			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
 				/*
 				 * If the result code is Activity.RESULT_OK, try to connect again
 				 */
 				switch (resultCode) {
 					case Activity.RESULT_OK:
 						/*
 						 * Try the request again
 						 */
 						break;
 					default:
 						break;
 				}
 				break;
 			case TTS_DATA_CHECK_CODE_REQUEST:
 				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 					// success, create the TTS instance
 					try {
 						api.prepareTextToSpeech();
 					}
 					catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
 					// missing data, install it
 					Intent installIntent = new Intent();
 					installIntent.setAction(
 						TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 					startActivity(installIntent);
 				}
 				break;
 			case WORKOUT_REQUEST:
 				if (resultCode == RESULT_OK)
 				{
 					workout = data.getParcelableExtra(Workout.TAG);
 					if (workout != null)
 					{
 						workoutNameTextView.setText(workout.getName());
 					}
 				}
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void showGPSAccuracy() {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					GPSSignalTextView.setText(String.format("%s: %.2fm", getResources()
 						.getString(R.string.gps_accuracy), mLastLocation.getAccuracy()));
 				}
 				catch (Throwable t) {
 					Log.e(TAG, "Error while updating the UI with tweets", t);
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected void onDestroy() {
 		
 		try {
 			api.removeListener(runListener);
 			unbindService(serviceConnection);
 			
 		}
 		catch (Throwable t) {
 			// catch any issues, typical for destroy routines
 			// even if we failed to destroy something, we need to continue destroying
 			Log.w(TAG, "Failed to unbind from the service", t);
 		}
 		doStopService();
 		Log.i(TAG, "Activity destroyed");
 		super.onDestroy();
 	}
 	
 	private void startActivityIfPossible() {
 		switch (gpsStatus)
 		{
 			case -1:
 				// Shouldn't be here;
 				Log.e("Service_info", "no gps status info");
 				break;
 			case GPS_NOT_ENABLED:
 				MyDialog dialog = new MyDialog();
 				DialogInterface.OnClickListener positiveButtonHandler = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						Intent intent = new Intent(
 							android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						startActivity(intent);
 					}
 				};
 				
 				dialog.showAlertDialog(this, R.string.dialog_message_no_gps,
 					R.string.empty_string, android.R.string.yes,
 					android.R.string.no, positiveButtonHandler, null);
 				
 				break;
 			case NO_GPS_SIGNAL:
 				dialog = new MyDialog();
 				positiveButtonHandler = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						
 					}
 				};
 				dialog.showAlertDialog(this,
 					R.string.dialog_message_low_gps_accuracy,
 					R.string.empty_string, android.R.string.ok,
 					R.string.empty_string, positiveButtonHandler, null);
 				break;
 			default:
 				startActivity(ActivityActivity.class, DOWN);
 				break;
 		
 		}
 		
 	}
 	
 	private void showGoogleServicesDialog(final ConnectionResult connectionResult) {
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (connectionResult.hasResolution()) {
 					try {
 						// Start an Activity that tries to resolve the error
 						connectionResult.startResolutionForResult(MainScreenActivity.this,
 							CONNECTION_FAILURE_RESOLUTION_REQUEST);
 						/*
 						 * Thrown if Google Play services canceled the original
 						 * PendingIntent
 						 */
 					}
 					catch (IntentSender.SendIntentException e) {
 						// Log the error
 						e.printStackTrace();
 					}
 				}
 				else {
 					/*
 					 * If no resolution is available, display a dialog to the user with
 					 * the error.
 					 */
 					// Get the error dialog from Google Play services
 					Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 						connectionResult.getErrorCode(), MainScreenActivity.this,
 						REQUEST_GOOGLE_PLAY_SERVICES); // tu by�a z�a liczba w
 					// dokumentacji :/
 					
 					// If Google Play services can provide an error dialog
 					if (errorDialog != null) {
 						// Create a new DialogFragment for the error dialog
 						ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 						// Set the dialog in the DialogFragment
 						errorFragment.setDialog(errorDialog);
 						// Show the error dialog in the DialogFragment
 						errorFragment.show(getSupportFragmentManager(),
 							"Location Updates");
 					}
 				}
 				
 			}
 		});
 		
 	}
 	
 	void doStartService() {
 		Log.i("Service_info", "Main Screen --> start service");
 		
 		Intent intent = new Intent(LocationService.class.getName());
 		//intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		//intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		startService(intent);
 		
 	}
 	
 	void doStopService() {
 		Log.i("Service_info", "Main Screen --> stop service");
 		Intent intent = new Intent(LocationService.class.getName());
 		stopService(intent);
 	}
 	
 	void doBindService() {
 		Log.i("Service_info", "Main Screen Binding");
 		Intent intent = new Intent(LocationService.class.getName());
 		//intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		//intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		bindService(intent, serviceConnection, 0);
 		mIsBound = true;
 	}
 	
 	void doUnbindService() {
 		Log.i("Service_info", "Main Screen Unbinding");
 		if (mIsBound) {
 			try {
 				api.removeListener(runListener);
 				unbindService(serviceConnection);
 			}
 			catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			
 			mIsBound = false;
 			
 		}
 	}
 	
 	@Override
 	public void onSingleTapConfirmed(MotionEvent e) {
 		if (mCurrent != null)
 		{
 			switch (mCurrent.getId())
 			{
 				case R.id.textViewGPSIndicator:
 					// if gps is not running
 					if (gpsStatus == GPS_NOT_ENABLED) {
 						Intent intent = new Intent(
 							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						startActivity(intent);
 					}
 					break;
 				case R.id.buttonStart:
 					startActivityIfPossible();
 					break;
 				case R.id.buttonHistory:
 					startActivity(HistoryActivity.class, LEFT);
 					break;
 				case R.id.buttonSettings:
 					startActivity(SettingsActivity.class, UP);
 					break;
 				case R.id.buttonPlans:
 					startActivity(PlaningActivity.class, RIGHT);
 					break;
 				case R.id.buttonMusic:
 					startSystemMusicPlayer();
 					break;
 				default:
 					break;
 			}
 		}
 	}
 	
 	private final ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			Log.i(TAG, "Service connection established");
 			isServiceConnected = true;
 			// that's how we get the client side of the IPC connection
 			api = RunListenerApi.Stub.asInterface(service);
 			try {
 				api.addListener(runListener);
 				gpsStatus = api.getGPSStatus();
 				handleGPSStatusChange();
 				getConnectionResult();
 			}
 			catch (RemoteException e) {
 				Log.e(TAG, "Failed to add listener", e);
 			}
 			
 			checkSpeechSynthezator();
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			Log.i(TAG, "Service connection closed");
 		}
 	};
 	
 	private final RunListener.Stub runListener = new RunListener.Stub() {
 		
 		@Override
 		public void handleLocationUpdate() throws RemoteException {
 			Location location = api.getLatestLocation();
 			
 			gpsStatus = api.getGPSStatus();
 			mLastLocation = location;
 			showGPSAccuracy();
 		}
 		
 		@Override
 		public void handleConnectionResult() throws RemoteException {
 			getConnectionResult();
 			
 		}
 		
 		@Override
 		public void handleTimeChange() throws RemoteException {}
 		
 		@Override
 		public void handleWorkoutChange(Workout workout) throws RemoteException {}
 	};
 	
 	private void getConnectionResult()
 	{
 		Intent intent;
 		try {
 			intent = api.getConnectionResult();
 			boolean connectionFailed = intent.getBooleanExtra(LocationService.CONNECTION_FIAILED_TAG, true);
 			if (connectionFailed)
 			{
 				int errorCode = intent.getIntExtra("status_code", -1);
 				PendingIntent pendingIntent = intent
 					.getParcelableExtra("pending_intent");
 				showGoogleServicesDialog(new ConnectionResult(errorCode,
 					pendingIntent));
 			}
 		}
 		catch (RemoteException e) {
 			Log.e(TAG, "Failed to get connectionResult", e);
 		}
 		
 	}
 	
 	private void handleGPSStatusChange()
 	{
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				switch (gpsStatus) {
 					case GPS_NOT_ENABLED:
 						GPSStatusTextView.setText(getResources().getString(
 							R.string.gps_disabled));
 						break;
 					case NO_GPS_SIGNAL:
 						GPSStatusTextView.setText(getResources().getString(
 							R.string.gps_enabled));
 						break;
 					case GPS_WORKING:
 						GPSStatusTextView.setText(getResources().getString(
 							R.string.gps_enabled));
 						break;
 				}
 			}
 			
 		});
 	}
 }
