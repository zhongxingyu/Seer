 package org.thingml.chestbelt.android.chestbeltdroid.viewer;
 
 import org.thingml.chestbelt.android.chestbeltdroid.communication.BluetoothManagementService;
 import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection;
 import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection.ChestBeltServiceConnectionCallback;
 import org.thingml.chestbelt.android.chestbeltdroid.devices.Device;
 import org.thingml.chestbelt.driver.ChestBeltListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 abstract public class VisualizationActivity extends Activity implements ChestBeltServiceConnectionCallback {
 
 	private static final String TAG = VisualizationActivity.class.getSimpleName();
 	
 	protected ChestBeltServiceConnection chestBeltConnection;
 	private String deviceAddress;
 	
 	abstract protected void onBindingReady();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		deviceAddress = getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS);
 		chestBeltConnection = new ChestBeltServiceConnection(this, deviceAddress);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Intent intent = new Intent(this, BluetoothManagementService.class);
 		bindService(intent, chestBeltConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		chestBeltConnection.stopListenning();
 		if (chestBeltConnection.isBound()) {
 			unbindService(chestBeltConnection);
 			chestBeltConnection.setBound(false);
 		}
 	}
 	
 	protected void removeChestBeltListenner(ChestBeltListener listenner) {
		chestBeltConnection.getDriver().removeChestBeltListener(listenner);
		Log.e(TAG, "ChestBelt listenner removed");
 	}
 	
 	@Override
 	public void serviceBound() {
 		Log.e(TAG, "Service bound");
 		if (chestBeltConnection.isConnected()) {
 			onBindingReady();
 		}
 	}
 
 	@Override
 	public void deviceConnected() {
 		Log.e(TAG, "Device connected");
 		if (chestBeltConnection.isBound()) {
 			onBindingReady();
 		}
 	}
 
 	@Override
 	public void deviceDisconnected() {
 		Log.e(TAG, "Device disconnected");
 	}
 }
