 package com.valfom.skydiving.altimeter;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.valfom.skydiving.altimeter.AltimeterCalibrationFragment.OnZeroAltitudeChangedListener;
 
 public class AltimeterActivity extends Activity implements SensorEventListener, OnZeroAltitudeChangedListener {
 
 	private static final String TAG = "AltimeterActivity";
 	public static final String PREF_FILE_NAME = "SAPrefs";
 	public static final byte STEP = 10;
 
 	private SensorManager sensorManager;
 	private Sensor sensorPressure;
 	// private Vibrator vibrator;
 	// private PowerManager.WakeLock wakeLock;
 
 	private TextView tvAltitude;
 	private TextView tvAltitudeUnit;
 
 	private boolean convert = false;
 
 	private Timer timerSavePoints;
 	
 	private Integer trackId = null;
 	
 	// Altitude
 	private int altitude;
 	private int zeroAltitude;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
 
 		if (sensorPressure != null) {
 
 			Log.d(TAG, "Barometer: min delay " + sensorPressure.getMinDelay()
 					+ ", power " + sensorPressure.getPower() + ", resolution"
 					+ sensorPressure.getResolution() + ", max range "
 					+ sensorPressure.getMaximumRange());
 			
 			tvAltitude = (TextView) findViewById(R.id.tvAltitude);
 			tvAltitudeUnit = (TextView) findViewById(R.id.tvAltitudeUnit);
 
 			ToggleButton tbShowData = (ToggleButton) findViewById(R.id.tbLog);
 
 			tbShowData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
 					if (isChecked) {
 
 //						DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
 						DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
 				        String startDate = dateFormat.format(new Date());
 				        dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
 				        String startTime = dateFormat.format(new Date());
 				        
 				        ContentValues values = new ContentValues();
 				        
 				        values.put(AltimeterDB.KEY_TRACKS_DATE, startDate);
 				        values.put(AltimeterDB.KEY_TRACKS_TIME, startTime);
 				        values.put(AltimeterDB.KEY_TRACKS_TYPE, 1);
 				        
 				        Uri uri = Uri.parse(AltimeterContentProvider.CONTENT_URI_TRACKS + "/insert");
 				        Uri url = getContentResolver().insert(uri, values);
 				        
 				        String[] arr = url.toString().split("/");
 				        
 				        trackId = Integer.valueOf(arr[1]);
 				        
 				        timerSavePoints = new Timer();
 				        timerSavePoints.schedule(new savePointsTask(), 0, 1000);
 					} else {
 						
 						trackId = null;
 						
 						if (timerSavePoints != null) {
 
 							timerSavePoints.cancel();
 							timerSavePoints = null;
 			        	}
 					}
 				}
 			});
 
 			// vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 
 			// PowerManager powerManager = (PowerManager)
 			// getSystemService(Context.POWER_SERVICE);
 			// wakeLock =
 			// powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
 
 			// wakeLock.acquire();
 			// vibrator.vibrate(1000);
 			// wakeLock.release();
 
 			// SENSOR_DELAY_NORMAL (200,000 microsecond delay) 200
 			// SENSOR_DELAY_GAME (20,000 microsecond delay) 200
 			// SENSOR_DELAY_UI (60,000 microsecond delay) <100 ms
 			// SENSOR_DELAY_FASTEST (0 microsecond delay) 200 ms
 			// As of Android 3.0 (API Level 11) you can also specify the delay
 			// as an absolute value (in microseconds).
 
 			sensorManager.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_FASTEST);
 		}
 	}
 	
 	private class savePointsTask extends TimerTask {
 
         @Override
         public void run() {
         	
             runOnUiThread(new Runnable() {
 
                 @Override
                 public void run() {
                 	
                 	int customAltitude;
                 	
             		customAltitude = altitude - zeroAltitude;
             		
             		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
 			        String date = dateFormat.format(new Date());
             		
             		ContentValues values = new ContentValues();
                     
                     values.put(AltimeterDB.KEY_POINTS_DATE, date);
                     values.put(AltimeterDB.KEY_POINTS_ALTITUDE, customAltitude);
                     values.put(AltimeterDB.KEY_POINTS_TRACK_ID, trackId);
                     
                     Uri uri = Uri.parse(AltimeterContentProvider.CONTENT_URI_POINTS.toString());
                     getContentResolver().insert(uri, values);
                 }
             });
         }
 	};
 
 	@Override
 	protected void onDestroy() {
 
 		super.onDestroy();
 
 		sensorManager.unregisterListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 
 		String altitudeUnit;
 		SharedPreferences sharedPreferences;
 
 		sharedPreferences = getSharedPreferences(PREF_FILE_NAME,
 				Activity.MODE_PRIVATE);
 		zeroAltitude = sharedPreferences.getInt("zeroAltitude", 0);
 
 		AltimeterSettings settings = new AltimeterSettings(this);
 
 		altitudeUnit = settings.getAltitudeUnit();
 
 		 if (altitudeUnit.equals(getString(R.string.ft))) convert = true;
 		 else convert = false;
 
 		tvAltitudeUnit.setText(altitudeUnit);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		getMenuInflater().inflate(R.menu.menu_main, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		switch (item.getItemId()) {
 
 		case R.id.action_settings:
 
 			Intent settings = new Intent(this,
 					AltimeterPreferenceActivity.class);
 			startActivity(settings);
 
 			break;
 		case R.id.action_list:
 
 			Intent list = new Intent(this, AltimeterListActivity.class);
 			startActivity(list);
 
 			break;
 		case R.id.action_calibrate:
 			
 			FragmentManager fragmentManager = getFragmentManager();
 			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 			fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
 			
 			AltimeterCalibrationFragment  calibrationFragment = (AltimeterCalibrationFragment) 
 					fragmentManager.findFragmentById(R.id.calibration_container);
 			
 			if (calibrationFragment == null) {
 				
 				fragmentTransaction.add(R.id.calibration_container, new AltimeterCalibrationFragment());
 //				fragmentTransaction.addToBackStack(null);
 			
 			} else {
 				
 				fragmentTransaction.remove(calibrationFragment);
 			}
 			
 			fragmentTransaction.commit();
 			
 			break;
 		default:
 			break;
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		Log.d(TAG, "Accuracy " + accuracy);
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 
 		float atmosphericPressure;
 		int customAltitude;
 
 		atmosphericPressure = event.values[0];
 
 		altitude = (int) SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
 				atmosphericPressure);
 
 		customAltitude = altitude - zeroAltitude;
 		
 		if (convert) customAltitude = AltimeterSettings.convertAltitudeToFt(customAltitude);
 
 		tvAltitude.setText(String.valueOf(customAltitude));
 	}
 
 	@Override
 	public void onZeroAltitudeChanged(int code) {
 		
 		switch (code) {
 		
 			case AltimeterCalibrationFragment.CODE_DECREASE:
 				zeroAltitude += STEP;
 				break;
 			case AltimeterCalibrationFragment.CODE_SET_TO_ZERO:
 				zeroAltitude = altitude;
 				break;
 			case AltimeterCalibrationFragment.CODE_INCREASE:
 				zeroAltitude -= STEP;
 				break;
 		}
 		
 		SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putInt("zeroAltitude", zeroAltitude);
 		editor.apply();
 	}
 }
