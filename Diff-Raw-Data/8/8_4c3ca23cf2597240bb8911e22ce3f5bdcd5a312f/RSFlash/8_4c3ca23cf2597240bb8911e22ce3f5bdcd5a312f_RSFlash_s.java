 package com.redstar.rsflash;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.FeatureInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class RSFlash extends Activity implements SensorEventListener {
 	public final static String TAG = "RSFlash";
 	
 	public final static int SHAKETIMER = 1;
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
 	
 	/** Called when the activity is first created. */
 	private Camera mCamera;
 	private String mFlashMode;
 	private boolean mHasFlash;
 	private boolean mDefaultOn = false;
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	private float mAccel; // acceleration apart from gravity
 	private float mAccelCurrent; // current acceleration including gravity
 	private float mAccelLast; // last acceleration including gravity
 	private Handler mHandler;
 	private boolean mInShake;
 
 	private Button mFlashButton;
 	private ToggleButton mShakeTB;
 	private Button mGodButton;
 	private boolean mIsFlashOn = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i(TAG, "onCreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		init();
 
 		mHasFlash = checkFlash();
 
 //		open();
 
 		if (!mHasFlash) {
 			mFlashButton.setEnabled(false);
 		}
 
 	}
 
 	private void init() {
 		mInShake = false;
 		mHandler = new Handler() {
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case SHAKETIMER:
 					mInShake = false;
 					break;
 				}
 			}
 		};
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 
 		mAccel = 0.00f;
 		mAccelCurrent = SensorManager.GRAVITY_EARTH;
 		mAccelLast = SensorManager.GRAVITY_EARTH;
 
 		mFlashButton = (Button) findViewById(R.id.button1);
 		mFlashButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 //				if (!mHasFlash) {
 //					return;
 //				}
 //
 //				Button btn = (Button) v;
 //				Parameters params = mCamera.getParameters();
 //				String oldMode = params.getFlashMode();
 //				if (oldMode.equals(Parameters.FLASH_MODE_OFF)) {
 //					params.setFlashMode(Parameters.FLASH_MODE_TORCH);
 //					// btn.setText("On");
 //					btn.setBackgroundResource(R.drawable.bt_on);
 //				} else {
 //					params.setFlashMode(Parameters.FLASH_MODE_OFF);
 //					// btn.setText("Off");
 //					btn.setBackgroundResource(R.drawable.bt_off);
 //				}
 //
 //				mCamera.setParameters(params);
 				int id = v.getId();
 				switch(id) {
 				case R.id.button1:
 					Button btn = (Button) v;
 					if(mIsFlashOn) {
 						stopFlashService();
 						btn.setBackgroundResource(R.drawable.bt_off);
 					} else {
 						startFlashService();
 						btn.setBackgroundResource(R.drawable.bt_on);
 					}
 					break;
 				}
 			}
 
 		});
 		
 		updateFlashState();
 		
 		mGodButton = (Button)findViewById(R.id.god_btn);
 		mGodButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startVoiceRecognitionActivity();
 			}
 		});
 		CheckVoiceRecognition();
 
 		mShakeTB = (ToggleButton) findViewById(R.id.toggleButton1);
 		mShakeTB.setVisibility(View.GONE);
 		mShakeTB.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				ToggleButton btn = (ToggleButton) v;
 				if (btn.isChecked()) {
 					mSensorManager.registerListener(RSFlash.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 				} else {
 					mSensorManager.unregisterListener(RSFlash.this);
 				}
 			}
 
 		});
 
 	}
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	private boolean shakeOn = false;
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.shake:
 			if (!shakeOn) {
 				mSensorManager.registerListener(RSFlash.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 				item.setTitle(R.string.shakeoff);
 				shakeOn = true;
 			} else {
 				mSensorManager.unregisterListener(RSFlash.this);
 				item.setTitle(R.string.shakeon);
 				shakeOn = false;
 			}
 			return true;
 		case R.id.godmode:
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 //		release();
 //		if (mShakeTB.isChecked()) {
 //			Log.i(TAG, "Unregist Accelerometer");
 			mSensorManager.unregisterListener(this);
 //		}
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 //		open();
 //		if (mShakeTB.isChecked()) {
 //			Log.i(TAG, "Regist Accelerometer");
 		updateFlashState();
 			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 //		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 //		release();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			
 			String godSay = getResources().getString(R.string.god_say).toLowerCase();
 			for(String match : matches) {
 				if(match.toLowerCase().contains(godSay)) {
					mFlashButton.performClick();
					mHandler.sendEmptyMessageDelayed(SHAKETIMER, 1000);
 					return;
 				}
 			}
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private boolean TEST = true;
 
 	private boolean checkFlash() {
 		boolean ret = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
 //		 FeatureInfo[] fs = this.getPackageManager().getSystemAvailableFeatures();
 //		 for(FeatureInfo f : fs) {
 //			 Log.i(TAG, f.toString());
 //		 }
 		// TODO:
 		// if(!ret) {
 		// Toast.makeText(this, R.string.noflash, Toast.LENGTH_LONG).show();
 		// }
 		if (TEST) {
 			ret = true;
 		}
 		return ret;
 	}
 
 	private void open() {
 //		if (!mHasFlash) {
 //			return;
 //		}
 //
 //		if (mCamera == null) {
 //			mCamera = Camera.open();
 //			Parameters params = mCamera.getParameters();
 //			mFlashMode = params.getFlashMode();
 //			if (mDefaultOn) {
 //				params.setFlashMode(Parameters.FLASH_MODE_TORCH);
 //				mCamera.setParameters(params);
 //			} else {
 //				// params.setFlashMode(Parameters.FLASH_MODE_OFF);
 //				// mCamera.setParameters(params);
 //			}
 //
 //			// List<String> modes = params.getSupportedFlashModes();
 //			// Log.i("aaaa", modes.toString());
 //		}
 	}
 
 	private void release() {
 //		Log.i(TAG, "release!!!");
 //		if (mCamera != null) {
 //			Parameters params = mCamera.getParameters();
 //			if(mFlashMode != null) {
 //				params.setFlashMode(mFlashMode);
 //			}
 //			mCamera.setParameters(params);
 //			mCamera.release();
 //			mCamera = null;
 //		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent se) {
 		float x = se.values[0];
 		float y = se.values[1];
 		float z = se.values[2];
 		mAccelLast = mAccelCurrent;
 		mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
 		float delta = mAccelCurrent - mAccelLast;
 		mAccel = mAccel * 0.9f + delta; // perform low-cut filter
 //		Log.i(TAG, "x=" + x + ", y=" + y + ", z=" + z);
 //		Log.i(TAG, "mAccel=" + mAccel);
 
 		if (mAccel > 2f) {
 			shakeFlash();
 		}
 	}
 
 	private void shakeFlash() {
 		// if(mShakeTB.isChecked() && !mInShake) {
 		if (shakeOn && !mInShake) {
 			mInShake = true;
 			mFlashButton.performClick();
 			mHandler.sendEmptyMessageDelayed(SHAKETIMER, 1000);
 		}
 	}
 	
 	private void CheckVoiceRecognition() {
 		// Check to see if a recognition activity is present
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() != 0) {
 //			mGodButton.setOnClickListener(this);
 		} else {
 			mGodButton.setEnabled(false);
 //			mGodButton.setText("Recognizer not present");
 		}
 	}
 	
 	private void startVoiceRecognitionActivity() {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 	
 	private void startFlashService() {
 		mIsFlashOn = true;
 		Intent intent = new Intent(RSFlashService.ACTION_FLASHLIGHT_ON);
 		intent.setClass(this, RSFlashService.class);
 		startService(intent);
 	}
 	
 	private void stopFlashService() {
 		mIsFlashOn = false;
 		Intent intent = new Intent(RSFlashService.ACTION_FLASHLIGHT_OFF);
 		intent.setClass(this, RSFlashService.class);
 		startService(intent);
 	}
 	
 	private void updateFlashState() {
 		if(FlashServiceRunning(this)) {
 			mIsFlashOn = true;
 			mFlashButton.setBackgroundResource(R.drawable.bt_on);
 		} else {
 			mIsFlashOn = false;
 			mFlashButton.setBackgroundResource(R.drawable.bt_off);
 		}
 	}
 
 	
 	private boolean FlashServiceRunning(Context context) {
 		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
 
 		List<ActivityManager.RunningServiceInfo> svcList = am.getRunningServices(100);
 
 		if (!(svcList.size() > 0))
 			return false;
 		for (int i = 0; i < svcList.size(); i++) {
 			RunningServiceInfo serviceInfo = svcList.get(i);
 			ComponentName serviceName = serviceInfo.service;
 			if (serviceName.getClassName().endsWith(".RSFlashService"))
 				return true;
 		}
 		return false;
 	}
 }
