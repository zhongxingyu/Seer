 /*
  *  Copyright (c) 2011-12, The Linux Foundation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *          notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *          notice, this list of conditions and the following disclaimer in the
  *          documentation and/or other materials provided with the distribution.
  *        * Neither the name of The Linux Foundation nor
  *          the names of its contributors may be used to endorse or promote
  *          products derived from this software without specific prior written
  *          permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.android.thermometer;
 
 import java.util.UUID;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothDevicePicker;
 import android.bluetooth.IBluetoothThermometerServices;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.ParcelUuid;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class BluetoothThermometerClient extends Activity {
 	private static final String TAG = "BluetoothThermometerClient";
 
 	public static UUID GATTServiceUUID = null;
 
 	public static ParcelUuid GATTServiceParcelUUID = null;
 
 	public static BluetoothDevice RemoteDevice = null;
 
 	public static ListView mListView = null;
 
 	static final String REMOTE_DEVICE = "RemoteDevice";
 
 	public static String UserDefinedUUID = null;
 
 	public static final String USER_DEFINED = "UserDefined";
 
 	public static final String[] StringServicesUUID = {
 			"0000180900001000800000805f9b34fb", // Thermometer service 0000180900001000800000805f9b34fb
 			"0000180A00001000800000805f9b34fb", // Device Service
 			USER_DEFINED };
 	protected static final int DEVICE_SELECTED = 0;
 
 	public static final String GATT_SERVICE_NAME = "GATT_SERVICE_NAME";
 
 	public static final String GATT_SERVICE_HEALTH_SERVICE = "GATT_SERVICE_HEALTH_SERVICE";
 
 	public static final String GATT_SERVICE_DEVICE_INFO_SERVICE = "GATT_SERVICE_DEVICE_INFO_SERVICE";
 
 	public static Context mainContext = null;
 
 	public static IBluetoothThermometerServices bluetoothThermometerServices = null;
 
 	public static Button buttonHealth = null;
 
 	public static Button buttonDevice = null;
 
 	public static Button buttonDiscoverSrv = null;
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 	    // Called when the connection with the service is established
 	    public void onServiceConnected(ComponentName className, IBinder service) {
 	        // Following the example above for an AIDL interface,
 	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
 			Log.e(TAG, "**********onServiceConnected***************");
 			bluetoothThermometerServices = IBluetoothThermometerServices.Stub
 					.asInterface(service);
 
 	    }
 
 		public void onServiceDisconnected(ComponentName name) {
 			Log.e(TAG, "*************onServiceDisconnected***********");
 			onServiceDisconn();
 		}
 	};
 
 
 	public final Handler msgHandler = new Handler() {
 		 @Override
 	     public void handleMessage(Message msg) {
 			 switch (msg.what) {
               case DEVICE_SELECTED:
 				Log.d(TAG, "device selected");
 				RemoteDevice = (BluetoothDevice) msg.getData().getParcelable(
 						REMOTE_DEVICE);
 				BluetoothThermometerClient.buttonHealth.setEnabled(true);
 				BluetoothThermometerClient.buttonHealth.setClickable(true);
 				BluetoothThermometerClient.buttonDevice.setEnabled(true);
 				BluetoothThermometerClient.buttonDevice.setClickable(true);
 				break;
	     default:
 		break;
 			 }
 		 }
 	};
 
 	/** Called when the activity is first created. */
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.thermomain);
 		Log.d(TAG, "****Set main content view*****");
 
 		mainContext = this.getApplicationContext();
 		BluetoothThermometerClientReceiver.registerHandler(msgHandler);
         final Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
         buttonConnect.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 				Log.d(TAG, "Button connect to bt devices clicked");
 				bindToThermometerService();
 
             }
         });
 		buttonHealth = (Button) findViewById(R.id.buttonHealth);
 		buttonHealth.setEnabled(false);
 		buttonHealth.setClickable(false);
 		buttonHealth.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startGattService(StringServicesUUID[0]);
 			}
 		});
 
 		buttonDevice = (Button) findViewById(R.id.buttonDevice);
 		buttonDevice.setEnabled(false);
 		buttonDevice.setClickable(false);
 		buttonDevice.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startGattService(StringServicesUUID[1]);
 			}
 		});
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.e(TAG, "****the activity is paused*****");
     }
 
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.e(TAG, "****the activity is destroyed*****");
 		close();
 	}
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		Log.e(TAG, "****the activity is restart*****");
 	}
 
 	public void onServiceConn()
 	{
 		Intent in1 = new Intent(BluetoothDevicePicker.ACTION_LAUNCH);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,
 				BluetoothDevicePicker.FILTER_TYPE_ALL);
 		in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE,
				"com.android.bluetooth.thermometer");
 		in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS,
 				BluetoothThermometerClientReceiver.class.getName());
 		in1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		this.startActivity(in1);
 	}
 
 	public void startGattService(String uuidString)
 	{
 		Log.d(TAG, "Inside startGattService for : " + uuidString);
 		try {
 			GATTServiceUUID = convertUUIDStringToUUID(uuidString);
 			Log.d(TAG, " GATTServiceUUID = " + GATTServiceUUID);
 			GATTServiceParcelUUID = new ParcelUuid(GATTServiceUUID);
 			Log.d(TAG, " GATTServiceParcelUUID = " + GATTServiceParcelUUID);
 			if (bluetoothThermometerServices != null) {
 				boolean isGattService = bluetoothThermometerServices
 						.startThermometerService(RemoteDevice,
 								GATTServiceParcelUUID,
 								BluetoothThermometerServicesScreen.mCallback);
 				if (!isGattService) {
 					Log.e(TAG, "Thermometer service could not get GATT service");
 					Toast.makeText(getApplicationContext(),
 							"could not start thermo service",
 							Toast.LENGTH_SHORT).show();
 				} else {
 					Log.d(TAG, "Thermometer service got GATT service : "
 							+ GATTServiceParcelUUID);
 					Intent in = new Intent();
 					in.setClass(
 							mainContext,
 							com.android.thermometer.BluetoothThermometerServicesScreen.class);
 					in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					if (uuidString.equals(StringServicesUUID[0])) {
 						Log.d(TAG,
 								"Service name .. GATT_SERVICE_HEALTH_SERVICE");
 						in.putExtra(GATT_SERVICE_NAME,
 								GATT_SERVICE_HEALTH_SERVICE);
 					} else if (uuidString.equals(StringServicesUUID[1])) {
 						Log.d(TAG,
 								"Service name .. GATT_SERVICE_DEVICE_INFO_SERVICE");
 						in.putExtra(GATT_SERVICE_NAME,
 								GATT_SERVICE_DEVICE_INFO_SERVICE);
 					} else {
 						Log.e(TAG,
 								"Error uuidString not found in the services list");
 					}
 
 					mainContext.startActivity(in);
 				}
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"Not connected to service", Toast.LENGTH_SHORT).show();
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void onServiceDisconn()
 	{
 		close();
 	}
 
     public synchronized void close() {
 		if (mConnection != null) {
 			Log.e(TAG, "unbinding from thermometer service");
 			mainContext.unbindService(mConnection);
 		}
 		mConnection = null;
 		RemoteDevice = null;
 		GATTServiceParcelUUID = null;
 		GATTServiceUUID = null;
 		mainContext = null;
 		BluetoothThermometerServicesScreen.serviceReady = false;
 		BluetoothThermometerServicesScreen.deviceServiceReady = false;
     }
 
 	public void bindToThermometerService() {
 		String className = IBluetoothThermometerServices.class.getName();
 		Log.d(TAG, "class name : " + className);
 		Intent in = new Intent(className);
 		if (!mainContext.bindService(in, mConnection, Context.BIND_AUTO_CREATE)) {
 			Log.e(TAG, "Could not bind to Remote Service");
 		} else {
 			Log.e(TAG, "Succ bound to Remote Service");
 			onServiceConn();
 		}
 	}
 
 	private UUID convertUUIDStringToUUID(String UUIDStr) {
 		if (UUIDStr.length() != 32) {
 			return null;
 		}
 		String uuidMsB = UUIDStr.substring(0, 16);
 		String uuidLsB = UUIDStr.substring(16, 32);
 
 		if (uuidLsB.equals("800000805f9b34fb")) {
 			// TODO Long is represented as two complement. Fix this later.
 			UUID uuid = new UUID(Long.valueOf(uuidMsB, 16), 0x800000805f9b34fbL);
 			return uuid;
 		} else {
 			UUID uuid = new UUID(Long.valueOf(uuidMsB, 16),
 					Long.valueOf(uuidLsB));
 			return uuid;
 		}
 	}
 
 
 }
