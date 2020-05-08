 package com.shreyaschand.MEDIC.Patient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Trial extends Activity implements OnClickListener {
 
 	private BluetoothAdapter btAdapter;
 	public BluetoothSocket socket;
 
 	private TextView output = null;
 	private ScrollView scroller = null;
 
 	private static final int DEVICE_SELECT = 1;
 	private static final int REQUEST_ENABLE_BT = 2;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trial);
 
 		View backButtonView = findViewById(R.id.trial_back_button);
 		backButtonView.setOnClickListener(this);
 
 		output = ((TextView) findViewById(R.id.test_output));
 		scroller = (ScrollView) findViewById(R.id.trial_scroller);
 
 		btAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		Button connect_button = (Button) findViewById(R.id.trial_connect_button);
 		connect_button.setEnabled(false);
 		connect_button.setOnClickListener(this);
 
 		if (btAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 	}
 
 	public void onStart() {
 		super.onStart();
 		if (!btAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else {
 			findViewById(R.id.trial_connect_button).setEnabled(true);
 		}
 
 	}
 
 	public void onPause() {
 		super.onPause();
 		try {
 			socket.close();
 			output.append("Connection terminated.");
 		} catch (IOException e) {
 			output.append("Error closing socket.\nAssuming already closed.");
 		} catch (NullPointerException e) {} //Never got to create a socket.
 		socket = null;
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		try {
 			socket.close();
 			output.append("Connection terminated.");
 		} catch (IOException e) {
 			output.append("Error closing socket.\nAssuming already closed.");
 		} catch (NullPointerException e) {} //Never got to create a socket.
 		socket = null;
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case REQUEST_ENABLE_BT:
 			if (resultCode == Activity.RESULT_OK) {
 				findViewById(R.id.trial_connect_button).setEnabled(true);
 			} else {
 				Toast.makeText(this, "Bluetooth not enabled.",
 						Toast.LENGTH_LONG).show();
 				finish();
 			}
 			break;
 		case DEVICE_SELECT:
 			if (resultCode == Activity.RESULT_OK) {
				new ConnectBT().execute(new String[] { data.getExtras()
 						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS) });
 			}
 			break;
 		}
 	}
 
	private class ConnectBT extends AsyncTask<String, Void, Boolean> {
 
 		protected Boolean doInBackground(String... mac) {
 			try {
 				publishProgress();
 				BluetoothDevice device = btAdapter.getRemoteDevice(mac[0]);
 				Method m = device.getClass().getMethod("createRfcommSocket",
 						new Class[] { int.class });
 				socket = (BluetoothSocket) m.invoke(device, 1);
 				socket.connect();
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 
 		protected void onProgressUpdate(Void... updates) {
 			output.append("Connecting...");
 			scroller.fullScroll(ScrollView.FOCUS_DOWN);
 		}
 
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				output.setText("connected.");
 				findViewById(R.id.trial_connect_button).setEnabled(false);
 				new BTCommunicator().execute(socket);
 			} else {
 				output.setText("error connecting.");
 			}
 		}
 
 	}
 
 	private class BTCommunicator extends
 			AsyncTask<BluetoothSocket, String, Void> {
 		protected Void doInBackground(BluetoothSocket... socket) {
 			try {
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						socket[0].getInputStream()));
 				String message = in.readLine();
 				while (message != null) {
 					publishProgress(new String[] { message });
 					message = in.readLine();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onProgressUpdate(String... update) {
 			output.append("\n" + update[0]);
 			scroller.fullScroll(ScrollView.FOCUS_DOWN);
 		}
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.trial_back_button:
 			finish();
 			break;
 		case R.id.trial_connect_button:
 			startActivityForResult(new Intent(this, DeviceListActivity.class),
 					DEVICE_SELECT);
 		}
 	}
 }
