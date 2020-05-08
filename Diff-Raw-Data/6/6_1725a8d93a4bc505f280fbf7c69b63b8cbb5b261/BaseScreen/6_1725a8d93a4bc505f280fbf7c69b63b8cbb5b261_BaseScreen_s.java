 package com.globalresolvewws;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Config;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.bluetooth.*;
 
 public class BaseScreen extends Activity {
 	// Debugging
 	private static final String TAG = "Weather Warning";
 
 	// Message types sent from the BluetoothChatService Handler
 	public static final int MESSAGE_STATE_CHANGE = 1;
 	public static final int MESSAGE_READ = 2;
 	public static final int MESSAGE_WRITE = 3;
 	public static final int MESSAGE_DEVICE_NAME = 4;
 	public static final int MESSAGE_TOAST = 5;
 
 	// Key names received from the BluetoothChatService Handler
 	public static final String DEVICE_NAME = "device_name";
 	public static final String TOAST = "toast";
 
 	// Intent request codes
 	private static final int REQUEST_CONNECT_DEVICE = 1;
 	private static final int REQUEST_ENABLE_BT = 2;
 
 	// Name of the connected device
 	private String mConnectedDeviceName = null;
 	// Local Bluetooth adapter
 	private BluetoothAdapter mbtA = null;
 	// Member object for the chat services
 	private BluetoothConnectionHandler mConnectionHandler = null;
 
 	private TextView mTitle;
 	private Button mUpdateButton;
 	private Button sUpdateButton;
 	private TextView tempCurr;
 	private TextView serviceValue;
 	private ArrayList<Weather> WeatherList;
 	private WeatherService WS;
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Set up the window layout
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.main);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 				R.layout.custom_title);
 		// Set up the custom title
 		mTitle = (TextView) findViewById(R.id.title_left_text);
 		mTitle.setText(R.string.app_name);
 		mTitle = (TextView) findViewById(R.id.title_right_text);
 		setContentView(R.layout.activity_main);
 		serviceValue = (TextView) findViewById(R.id.temp_service);
 		tempCurr = (TextView) findViewById(R.id.temp_curr);
		
 
 		// Bluetooth adapter
 		mbtA = BluetoothAdapter.getDefaultAdapter();
 		if (mbtA == null) {
 			Toast.makeText(this, "Bluetooth not supported with this device",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		final Button btnMapView = (Button) findViewById(R.id.buttonMapView);
 		setupServiceConnection();
 		btnMapView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(final View view) {
 				final Intent mapScreen = new Intent(getApplicationContext(),
 						MapScreen.class);
 				startActivity(mapScreen);
 				finish();
 			}
 		});
 
 		// If BT is not on, request that it be enabled.
 		mbtA = BluetoothAdapter.getDefaultAdapter();
 		if (mbtA == null) {
 			Toast.makeText(this, "Bluetooth not supported with this device",
 					Toast.LENGTH_LONG).show();
 		} if (!mbtA.isEnabled()) {
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         // Otherwise, setup the chat session
         } else {
             if (mConnectionHandler == null) setupConnection();
         }
 	}
 
 	private void setupServiceConnection() {
 		//Start the service
 		sUpdateButton = (Button) findViewById(R.id.updateServiceValues);
 		sUpdateButton.setOnClickListener(new OnClickListener() {
 			public void onClick(final View view)
 			{
 				WeatherList = WS.weather();
 				serviceValue.setText(WeatherList.get(0).getMaxTemp());
 			}
 		});
 	}
 	
 	
 	private void setupConnection() {
 		// Initialize the BluetoothChatService to perform bluetooth connections
 			mUpdateButton = (Button) findViewById(R.id.updateValues);
 			mUpdateButton.setOnClickListener(new OnClickListener() {
 				private String message;
 				public void onClick(final View view) {
 					message = "t";
 					sendMessage(message);
 				}
 			});
 		mConnectionHandler = new BluetoothConnectionHandler(this, mHandler);
 	}
 
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(final Message msg) {
 			switch (msg.what) {
 			case MESSAGE_STATE_CHANGE:
 				switch (msg.arg1) {
 				case BluetoothConnectionHandler.STATE_CONNECTED:
 					mTitle.setText(R.string.title_connected_to);
 					mTitle.append(mConnectedDeviceName);
 					break;
 				case BluetoothConnectionHandler.STATE_CONNECTING:
 					mTitle.setText(R.string.title_connecting);
 					break;
 				case BluetoothConnectionHandler.STATE_LISTEN:
 				case BluetoothConnectionHandler.STATE_NONE:
 					mTitle.setText(R.string.title_not_connected);
 					break;
 				default:
 					break;
 				}
 				break;
             case MESSAGE_READ:
                 byte[] readBuf = (byte[]) msg.obj;
                 // construct a string from the valid bytes in the buffer
                 String readMessage = new String(readBuf, 0, msg.arg1);
                 tempCurr.setText(readMessage);
                 break;
 			case MESSAGE_DEVICE_NAME:
 				// save the connected device's name
 				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
 				Toast.makeText(getApplicationContext(),
 						"Connected to " + mConnectedDeviceName,
 						Toast.LENGTH_SHORT).show();
 				break;
 			case MESSAGE_TOAST:
 				Toast.makeText(getApplicationContext(),
 						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
 						.show();
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	/**
 	 * Sends a message.
 	 * 
 	 * @param message
 	 *            A string of text to send.
 	 */
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
 	private void sendMessage(final String message) {
 		// Check that we're actually connected before trying anything
 		if (mConnectionHandler.getState() != BluetoothConnectionHandler.STATE_CONNECTED) {
 			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 
 		// Check that there's actually something to send
 		if (message.length() > 0) {
 			// Get the message bytes and tell the BluetoothChatService to write
 			final byte[] send = message.getBytes(Charset.forName("UTF-8"));
 			mConnectionHandler.write(send);
 		}
 	}
 
 	@Override
 	public synchronized void onResume() {
 		super.onResume();
 		// Performing this check in onResume() covers the case in which BT was
 		// not enabled during onStart(), so we were paused to enable it...
 		// onResume() will be called when ACTION_REQUEST_ENABLE activity
 		// returns.
 		if (mConnectionHandler != null
 				&& mConnectionHandler.getState() == BluetoothConnectionHandler.STATE_NONE) {
 			// Only if the state is STATE_NONE, do we know that we haven't
 			// started already
 			// Start the Bluetooth chat services
 			mConnectionHandler.start();
 		}
 
 	}
 
 	@Override
 	public void onDestroy() {
 		if (mConnectionHandler != null) {
 			mConnectionHandler.stop();
 		}
 		super.onDestroy();
 	}
 
 	private void ensureDiscoverable() {
 		if (Config.LOGD) {
 			Log.d(TAG, "ensure discoverable");
 		}
 		if (mbtA.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			final Intent discoverableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.putExtra(
 					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 			startActivity(discoverableIntent);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode,
 			  Intent data) {
 		if (requestCode == REQUEST_CONNECT_DEVICE) {
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				// Get the device MAC address
 				final String address = data.getExtras().getString(
 						DeviceListActivity.extraDeviceAddress);
 				// Get the BLuetoothDevice object
 				final BluetoothDevice device = mbtA.getRemoteDevice(address);
 				// Attempt to connect to the device
 				mConnectionHandler.connect(device);
 			}
 		} else if (requestCode == REQUEST_ENABLE_BT) {
 			// When the request to enable Bluetooth returns
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so set up a chat session
 				setupConnection();
 			} else {
 				// User did not enable Bluetooth or an error occured
 				Toast.makeText(this, "Bluetooth Not Enabled",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 		boolean isValid = false;
 		if (item.getItemId() == R.id.scan) {
 			// Launch the DeviceListActivity to see devices and do scan
 			final Intent serverIntent = new Intent(this,
 					DeviceListActivity.class);
 			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
 			isValid = true;
 		} else if (item.getItemId() == R.id.discoverable) {
 			// Ensure this device is discoverable by others
 			ensureDiscoverable();
 			isValid = true;
 		}
 		return isValid;
 	}
 
 }
