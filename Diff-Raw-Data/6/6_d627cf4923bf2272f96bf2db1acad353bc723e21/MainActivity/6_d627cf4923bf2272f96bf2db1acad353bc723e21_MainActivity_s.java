 package com.ncit.nxt;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.R.integer;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class MainActivity extends Activity {
 
 	private static final int REQUEST_ENABLE_BT = 1;
 	private static final int REQUEST_CONNECT_DEVICE = 2;
 	private static final int REQUEST_SETTINGS = 3;
 
 	public static final int MESSAGE_TOAST = 1;
 	public static final int MESSAGE_STATE_CHANGE = 2;
 
 	public static final String TOAST = "toast";
 
 	private BluetoothAdapter mBluetoothAdapter;
 	private PowerManager mPowerManager;
 	private PowerManager.WakeLock mWakeLock;
 	private NXTTalker mNXTTalker;
 
 	private int mState = NXTTalker.STATE_NONE;
 	private int mSavedState = NXTTalker.STATE_NONE;
 	private boolean mNewLaunch = true;
 	private String mDeviceAddress = null;
 
 	private ArrayList<Button> motorButtons = new ArrayList<Button>();
 	private Button testConnectButton;
 	private TextView testStatusTextView;
 
 	private boolean mRegulateSpeed = true;
 	private boolean mSynchronizeMotors = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_main);
 
 		if (savedInstanceState != null) {
 			mNewLaunch = false;
 			mDeviceAddress = savedInstanceState.getString("device_address");
 			if (mDeviceAddress != null) {
 				mSavedState = NXTTalker.STATE_CONNECTED;
 			}
 
 			if (savedInstanceState.containsKey("power")) {
 				//	mPower = savedInstanceState.getInt("power");
 			}
 			if (savedInstanceState.containsKey("controls_mode")) {
 				//./mControlsMode = savedInstanceState.getInt("controls_mode");
 			}
 		}
 
 		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NXT Remote Control");
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 
 		setupUI();
 
 		mNXTTalker = new NXTTalker(mHandler);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		//Log.i("NXT", "NXTRemoteControl.onStart()");
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else {
 			if (mSavedState == NXTTalker.STATE_CONNECTED) {
 				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
 				mNXTTalker.connect(device);
 			} else {
 				if (mNewLaunch) {
 					mNewLaunch = false;
 					findBrick();
 				}
 			}
 		}
 
 	}
 
 	private void setupUI() {
 		motorButtons.add((Button) findViewById(R.id.m1b1));
 		motorButtons.add((Button) findViewById(R.id.m1b2));
 		motorButtons.add((Button) findViewById(R.id.m2b1));
 		motorButtons.add((Button) findViewById(R.id.m2b2));
 		motorButtons.add((Button) findViewById(R.id.m3b1));
 		motorButtons.add((Button) findViewById(R.id.m3b2));
 
 		testStatusTextView = (TextView) findViewById(R.id.testtext);
 
 		for (int i = 0; i<motorButtons.size(); i++) {
 			boolean dir = false;
 			if (i%2==1) {
 				dir = true;
 			}
 			switch (i) {
 			case 0: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(0,true,1.0d,50)); break;
 			case 1: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(0,false,1.0d,50)); break;
 			case 2: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(1,true,1.0d,50)); break;
 			case 3: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(1,false,1.0d,50)); break;
 			case 4: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(2,true,1.0d,50)); break;
 			case 5: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(2,false,1.0d,50)); break;
 			default: motorButtons.get(i).setOnTouchListener(new MotorButtonListener(i/2,false,1.0d,0)); break;
 			}
 			//	motorButtons.get(i).setOnTouchListener(new MotorButtonListener((short)(i%3),dir,1.0d,50));
 		}
 
 		testConnectButton = (Button) findViewById(R.id.testconnectbutton);
 		testConnectButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				findBrick();
 			}
 		});
 
 		displayState();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case REQUEST_ENABLE_BT:
 			if (resultCode == Activity.RESULT_OK) {
 				findBrick();
 			} else {
 				Toast.makeText(this, "Bluetooth not enabled, exiting.", Toast.LENGTH_LONG).show();
 				finish();
 			}
 			break;
 		case REQUEST_CONNECT_DEVICE:
 			if (resultCode == Activity.RESULT_OK) {
 				String address = data.getExtras().getString(DeviceChooser.EXTRA_DEVICE_ADDRESS);
 				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
 				//Toast.makeText(this, address, Toast.LENGTH_LONG).show();
 				mDeviceAddress = address;
 				mNXTTalker.connect(device);
 			}
 			break;
 		case REQUEST_SETTINGS:
 			//XXX?
 			break;
 		}
 	}
 
 	private void displayState() {
 		String stateText = null;
 		int color = 0;
 		switch (mState){ 
 		case NXTTalker.STATE_NONE:
 			stateText = "Not connected";
 			color = 0xffff0000;
 			//	            mConnectButton.setVisibility(View.VISIBLE);
 			//	            mDisconnectButton.setVisibility(View.GONE);
 			setProgressBarIndeterminateVisibility(false);
 			if (mWakeLock.isHeld()) {
 				mWakeLock.release();
 			}
 			break;
 		case NXTTalker.STATE_CONNECTING:
 			stateText = "Connecting...";
 			color = 0xffffff00;
 			//	            mConnectButton.setVisibility(View.GONE);
 			//	            mDisconnectButton.setVisibility(View.GONE);
 			setProgressBarIndeterminateVisibility(true);
 			if (!mWakeLock.isHeld()) {
 				mWakeLock.acquire();
 			}
 			break;
 		case NXTTalker.STATE_CONNECTED:
 			stateText = "Connected";
 			color = 0xff00ff00;
 			//	            mConnectButton.setVisibility(View.GONE);
 			//	            mDisconnectButton.setVisibility(View.VISIBLE);
 			setProgressBarIndeterminateVisibility(false);
 			if (!mWakeLock.isHeld()) {
 				mWakeLock.acquire();
 			}
 			break;
 		}
 		testStatusTextView.setText(stateText);
 		testStatusTextView.setTextColor(color);
 	}
 
 	private void findBrick() {
 		Intent intent = new Intent(this, DeviceChooser.class);
 		startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
 	}
 
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MESSAGE_TOAST:
 				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
 				break;
 			case MESSAGE_STATE_CHANGE:
 				mState = msg.arg1;
 				displayState();
 				break;
 			}
 		}
 	};
 
 	private class MotorButtonListener implements OnTouchListener {
 
 		private double motormod;
 		private int power;
 		private byte motorB;
 
 		public MotorButtonListener(int motor, boolean dir, double powerMod, int power) {
 			motormod = powerMod;
 			this.power = power;
 			if (!dir){
				power *= -1;
 				Log.d("PWR", "Power: "+power);
 			}
 			switch (motor){ 
 			case 0: motorB = NXTTalker.MOTOR1; break;
 			case 1: motorB = NXTTalker.MOTOR2; break;
 			case 2: motorB = NXTTalker.MOTOR3; break;
 			default: motorB = NXTTalker.MOTOR1; break;
 			}
 		}
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			//Log.i("NXT", "onTouch event: " + Integer.toString(event.getAction()));
 			int action = event.getAction();
 			//if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
 			if (action == MotionEvent.ACTION_DOWN) {
 	//			Log.d("Rev", "Direction is: "+mReverse);
 				byte fPower = (byte) (power*motormod);
 				mNXTTalker.motor(motorB, fPower, mRegulateSpeed, mSynchronizeMotors);
 			} else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
 				mNXTTalker.motor(motorB, (byte) 0, mRegulateSpeed, mSynchronizeMotors);
 			}
 			return true;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
