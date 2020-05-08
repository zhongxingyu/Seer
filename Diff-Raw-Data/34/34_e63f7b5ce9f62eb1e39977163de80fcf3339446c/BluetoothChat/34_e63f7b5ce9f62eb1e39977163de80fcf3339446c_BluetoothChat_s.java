 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.authorwjf.bounce;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This is the main Activity that displays the current chat session.
  */
 public class BluetoothChat extends Service {
 	// Debugging
 	private static final String TAG = "BluetoothChat";
 	private static final boolean D = true;
 
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
 	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
 	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
 	private static final int REQUEST_ENABLE_BT = 3;
 
 	// Layout Views
 	private ListView mConversationView;
 	private EditText mOutEditText;
 	private Button mSendButton;
 
 	// Name of the connected device
 	private String mConnectedDeviceName = null;
 	// Array adapter for the conversation thread
 	private ArrayAdapter<String> mConversationArrayAdapter;
 	// String buffer for outgoing messages
 	private StringBuffer mOutStringBuffer;
 	// Local Bluetooth adapter
 	private BluetoothAdapter mBluetoothAdapter = null;
 	// Member object for the chat services
 	private BluetoothChatService mChatService = null;
 
 	private final IBinder mBinder = new LocalBinder();
 
 	public class LocalBinder extends Binder {
 		BluetoothChat getService() {
 			return BluetoothChat.this;
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		if(D) Log.e(TAG, "+++ ON CREATE +++");
 		System.out.println("bluetooth created");
 
 		// Get local Bluetooth adapter
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
 			return;
		}
 	}
 
 	private Runnable f = new Runnable()  {
 		// Fetch the scores ...
 		//AsyncTask.execute(new Runnable() {
 		public void run() {
 			fetchScoreboardFunc();
 		}
 	};
 
 	private void fetchScoreboardFunc() {
 		// Fetch the scores ...
 		AsyncTask.execute(new Runnable() {
 			public void run() {
 				try {
 					// Instantiate the scoreboardEntriesList
 					//ArrayList<ScoreboardEntry> scoreboardEntriesList = new ArrayList<ScoreboardEntry>();
 
 					// Get the attributes used for the HTTP GET
 					//String currentUserFBID = application.getCurrentFBUser().getId();
 					//String currentUserAccessToken = Session.getActiveSession().getAccessToken();
 					Random rgen = new Random();
 					double currentScore = rgen.nextDouble();
 					String currentUserFBID = Main.fbid;
 					// Execute the HTTP Get to our server for the scores of the user's friends
 					HttpClient client = new DefaultHttpClient();
 					/* Update this */
 					//String getURL = "http://www.friendsmash.com/scores?fbid=" + currentUserFBID + "&access_token=" + currentUserAccessToken;
 					String getURL = "http://omnidrive.herokuapp.com/data?fbid=" + currentUserFBID + "&currentScore=" + currentScore;
 					System.out.println(getURL);
 					HttpGet get = new HttpGet(getURL);
 					HttpResponse responseGet = client.execute(get);
 
 				} catch (Exception e) {
 					Log.e(BluetoothChat.TAG, e.toString());
 				}
 			}
 
 		});
 	}
 
 	public void onStart() {
 		if(D) Log.e(TAG, "++ ON START ++");
		System.out.println("on start");

 		// If BT is not on, request that it be enabled.
 		// setupChat() will then be called during onActivityResult
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			// Otherwise, setup the chat session
 		} else {
 			if (mChatService == null) setupChat();
 		}
 	}
 
 	private void setupChat() {
 		Log.d(TAG, "setupChat()");
 
 		// Initialize the array adapter for the conversation thread
 		mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
 
 		// Initialize the BluetoothChatService to perform bluetooth connections
 		mChatService = new BluetoothChatService(this, mHandler);
 
 		// Initialize the buffer for outgoing messages
 		mOutStringBuffer = new StringBuffer("");
 
 		BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
 	    BluetoothDevice remoteDevice = myAdapter.getRemoteDevice("00:00:00:00:00:00");
 	    try	{
 	    BluetoothSocket btSocket = remoteDevice.createRfcommSocketToServiceRecord(BluetoothChatService.MY_UUID_INSECURE);
 	    btSocket.connect();
 	    }
 	    catch(IOException exc)	{
 	    	System.out.println("Error connecting");
 	    	stopSelf();
 	    }
 		new AutomaticQueryThread(mSendButton, this).start();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Stop the Bluetooth chat services
 		if (mChatService != null) mChatService.stop();
 		if(D) Log.e(TAG, "--- ON DESTROY ---");
 	}
 
 	private void ensureDiscoverable() {
 		if(D) Log.d(TAG, "ensure discoverable");
 		if (mBluetoothAdapter.getScanMode() !=
 				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 			startActivity(discoverableIntent);
 		}
 	}
 
 	public void hackMessage(String str)	{
 		sendMessage(str);
 	}
 
 	/**
 	 * Sends a message.
 	 * @param message  A string of text to send.
 	 */
 	private void sendMessage(String message) {
 		System.out.println("trying to sendMessage");
 		// Check that we're actually connected before trying anything
 		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
 			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		/*
 		 * MPG = (14.7 * 6.17 * 4.54 * VSS * 0.621371) / (3600 * MAF / 100)
 		 	= 710.7 * VSS / MAF
 
 			MPG - miles per gallon
 			14.7 grams of air to 1 gram of gasoline - ideal air/fuel ratio
 			6.17 pounds per gallon - density of gasoline
 			4.54 grams per pound - conversion
 			VSS - vehicle speed in kilometers per hour
 			0.621371 miles per hour/kilometers per hour - conversion
 			3600 seconds per hour - conversion
 			MAF - mass air flow rate in 100 grams per second
 			100 - to correct MAF to give grams per second
 
 		 */
 
 		// Check that there's actually something to send
 		if (message != null && message.length() > 0) {
 			// PARSER
 			String lmsg = message.toLowerCase();
 			if(lmsg.equals("rpm")) {
 				message = "010C";
 			} else if(lmsg.equals("speed")) {
 				message = "010D";
 			} else if(lmsg.equals("baro")) {
 				message = "0133";
 			} else if(lmsg.equals("ambient")) {
 				message = "0146";
 			} else if(lmsg.equals("throttle")) {
 				message = "0149";
 			} else if(lmsg.equals("maf")) {
 				message = "0110";
 			}
 		}
 		// Get the message bytes and tell the BluetoothChatService to write
 		byte[] send = message.getBytes();
 		mChatService.write(send);
 
 		// Reset out string buffer to zero and clear the edit text field
 		mOutStringBuffer.setLength(0);
 		//mOutEditText.setText(mOutStringBuffer);
 	}
 
 	// The action listener for the EditText widget, to listen for the return key
 	private TextView.OnEditorActionListener mWriteListener =
 			new TextView.OnEditorActionListener() {
 		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 			// If the action is a key-up event on the return key, send the message
 			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
 				String message = view.getText().toString();
 				sendMessage(message);
 			}
 			if(D) Log.i(TAG, "END onEditorAction");
 			return true;
 		}
 	};
 
 	// The Handler that gets information back from the BluetoothChatService
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MESSAGE_STATE_CHANGE:
 				if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
 				switch (msg.arg1) {
 				case BluetoothChatService.STATE_CONNECTED:
 					mConversationArrayAdapter.clear();
 					break;
 				case BluetoothChatService.STATE_CONNECTING:
 					break;
 				case BluetoothChatService.STATE_LISTEN:
 				case BluetoothChatService.STATE_NONE:
 					break;
 				}
 				break;
 			case MESSAGE_WRITE:
 				byte[] writeBuf = (byte[]) msg.obj;
 				// construct a string from the buffer
 				String writeMessage = new String(writeBuf);
 				mConversationArrayAdapter.add("Me:  " + writeMessage);
 				break;
 			case MESSAGE_READ:
 				byte[] readBuf = (byte[]) msg.obj;
 				// construct a string from the valid bytes in the buffer
 				String readMessage = new String(readBuf, 0, msg.arg1);
 				mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
 				break;
 			case MESSAGE_DEVICE_NAME:
 				// save the connected device's name
 				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
 				Toast.makeText(getApplicationContext(), "Connected to "
 						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
 				break;
 			case MESSAGE_TOAST:
 				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
 						Toast.LENGTH_SHORT).show();
 				break;
 			}
 		}
 	};
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(D) Log.d(TAG, "onActivityResult " + resultCode);
 		switch (requestCode) {
 		case REQUEST_CONNECT_DEVICE_SECURE:
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				connectDevice(data, true);
 			}
 			break;
 		case REQUEST_CONNECT_DEVICE_INSECURE:
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				connectDevice(data, false);
 			}
 			break;
 		case REQUEST_ENABLE_BT:
 			// When the request to enable Bluetooth returns
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so set up a chat session
 				setupChat();
 			} else {
 				// User did not enable Bluetooth or an error occurred
 				Log.d(TAG, "BT not enabled");
 				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	private void connectDevice(Intent data, boolean secure) {
 		// Get the device MAC address
 		String address = data.getExtras()
 				.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
 		// Get the BluetoothDevice object
 		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
 		// Attempt to connect to the device
 		mChatService.connect(device, secure);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent serverIntent = null;
 		switch (item.getItemId()) {
 		case R.id.secure_connect_scan:
 			// Launch the DeviceListActivity to see devices and do scan
 			serverIntent = new Intent(this, DeviceListActivity.class);
 			return true;
 		case R.id.insecure_connect_scan:
 			// Launch the DeviceListActivity to see devices and do scan
 			serverIntent = new Intent(this, DeviceListActivity.class);
 			return true;
 		case R.id.discoverable:
 			// Ensure this device is discoverable by others
 			ensureDiscoverable();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mBinder;
 	}
 
 }
 
