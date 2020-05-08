 package com.eumetrica.em2;
 
 import java.util.Set;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.neurosky.thinkgear.TGDevice;
 
 public class PatientEKGMain extends Activity {
 
 	Button connectButton;
 	Button startRecordingButton;
 	Button stopRecordingButton;
 	TextView signalValue;
 	TextView connectionStatus;
 	TGDevice tgDevice;
 	BluetoothAdapter btAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_patient_ekgmain);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		//Create the buttons on the screen and assign handlers
 		connectButton = (Button) findViewById(R.id.connect_button);
 		connectButton.setOnClickListener(connectButtonHandler);
 		startRecordingButton = (Button) findViewById(R.id.start_recording_button);
 		startRecordingButton.setOnClickListener(startRecordingButtonHandler);
 		stopRecordingButton = (Button) findViewById(R.id.stop_recording_button);
 		stopRecordingButton.setOnClickListener(stopRecordButtonHandler);
 		//Create the text-views on the screen
 		signalValue = (TextView) findViewById(R.id.signal_value);
 		connectionStatus = (TextView) findViewById(R.id.connectionStatusTextView);
 		
 	}
 
 	//Handle incoming messages from the device
 	//Update the connection state and log the status
 	private final Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case TGDevice.MSG_STATE_CHANGE:
 				switch (msg.arg1) {
 				case TGDevice.STATE_IDLE:
 					Log.i ("info", "idle");
 					connectionStatus.setText("Idle");
 					break;
 				case TGDevice.STATE_CONNECTING:
 					Log.i ("info", "connecting");
 					connectionStatus.setText("Connecting...");
 					break;
 				case TGDevice.STATE_CONNECTED:
 					Log.i ("info", "connected");
 					connectionStatus.setText("Connected");
 					//tgDevice.start();
 					break;
 				case TGDevice.STATE_DISCONNECTED:
 					Log.i ("info", "disconnected");
 					connectionStatus.setText("Disconnected");
 					break;
 				case TGDevice.STATE_NOT_FOUND:
 					Log.i ("info", "not found");
 					connectionStatus.setText("Not found");
 				case TGDevice.STATE_NOT_PAIRED:
 					Log.i ("info", "not paired");
 					connectionStatus.setText("Not paired");
 				default:
 					break;
 				}
 				break;
 			case TGDevice.MSG_POOR_SIGNAL:
				Log.v("HelloEEG", "PoorSignal: " + msg.arg1);
 				connectionStatus.setText("Poor signal");
 			case TGDevice.MSG_ATTENTION:
				Log.v("HelloEEG", "Attention: " + msg.arg1);
 				connectionStatus.setText("Attention");
 				break;
 			case TGDevice.MSG_RAW_DATA:
 				int rawADCValue = msg.arg1;
 				Log.i ("info", "Got RAW DATA ****** ADC Value is " + rawADCValue);
 				//ekgValue.setText(rawValue);
 				break;
 			case TGDevice.MSG_EEG_POWER:
 				//TGEegsPower ep = (TGEegPower) msg.arg1;
 				//Log.v("HelloEEG", "Delta: " + ep.delta);
 			default:
 				break;
 			}
 		}
 	};
 
 	//Handler for the ON button
 	View.OnClickListener connectButtonHandler = new View.OnClickListener() {
 		public void onClick(View v) {
 			
 			// Initializing the Bluetooth adapter
 			btAdapter = BluetoothAdapter.getDefaultAdapter();
 
 			//Check if Bluetooth is enabled
 			if (!btAdapter.isEnabled()) {
 				signalValue.setText("Bluetooth disabled");
 			}
 			//Check that the Bluetooth adapter is not null
 			if (btAdapter != null) {
 				signalValue.setText("Bluetooth not null");
 				//Get the paired devices
 				Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
 				//Go through paired Devices and get the names
 				String[] deviceNames = new String[pairedDevices.size()];
 				for (int j=0; j < pairedDevices.size(); j++){
 					if (pairedDevices.iterator().hasNext()){
 						deviceNames[j] = pairedDevices.iterator().next().getName();
 						signalValue.setText(deviceNames[j]);
 					}
 				}
 				//Get a new NeuroSky device and connect to it
 				Log.i ("info", "Getting new TGDevice");
 				tgDevice = new TGDevice(btAdapter, handler);
 				Log.i ("info", "Got new TGDevice - connecting");
 				tgDevice.connect(true);
 				Log.i ("info", "Connected to new TGDevice");
 				
 			}
 		}
 	};
 	
 	//Handler for the START button
 	View.OnClickListener startRecordingButtonHandler = new View.OnClickListener() {
 		public void onClick(View v) {
 			Log.i ("info", "Starting recording");
 			tgDevice.start();
 		}
 	};
 	
 	//Handler for the STOP button
 	View.OnClickListener stopRecordButtonHandler = new View.OnClickListener() {
 		public void onClick(View v) {
 			Log.i ("info", "Stopping recording");
 			tgDevice.stop();
 		}
 	};
 
 	//UNNECESSARY -- OPTIONS MENU STUFF -- DISABLED
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.activity_patient_ekgmain, menu);
 //		return true;
 //	}
 //
 //	@Override
 //	public boolean onOptionsItemSelected(MenuItem item) {
 //		switch (item.getItemId()) {
 //		case android.R.id.home:
 //			// This ID represents the Home or Up button. In the case of this
 //			// activity, the Up button is shown. Use NavUtils to allow users
 //			// to navigate up one level in the application structure. For
 //			// more details, see the Navigation pattern on Android Design:
 //			//
 //			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 //			//
 //			NavUtils.navigateUpFromSameTask(this);
 //			return true;
 //		}
 //		return super.onOptionsItemSelected(item);
 //	}
 }
