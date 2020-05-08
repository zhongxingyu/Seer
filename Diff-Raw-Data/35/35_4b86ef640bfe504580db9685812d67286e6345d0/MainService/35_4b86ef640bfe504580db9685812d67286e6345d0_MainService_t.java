 /* README: Start the app.  Unplug/plug in device.  Pray it works... */
 package com.example.morebluetoothtesting;
 
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 
 import android.app.Service;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.util.Log;
 
 public class MainService extends Service {
 
 	private String CLASS_NAME = this.getClass().getSimpleName();
 
 	private int KEY_SIZE = 32;
 
 	private BluetoothConnection btConnection = null;
 	private Magnetometer magListener = null;
 	
 	HashMap<String, String> keyAddrMap = null;
 	FileManager fm = null;
 	byte key[] = new byte[KEY_SIZE];
 
 	/* Receives Bluetooth related messages (e.g. when BT is connected) */
 	public final BroadcastReceiver btReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 
 			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
 				String connectedAddr = ((BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getAddress();
 				if (keyAddrMap.get(connectedAddr) == null)
 					Log.v("asd", "I don't know you...");	// disconnect
 				else
 					key = keyAddrMap.get(connectedAddr).getBytes();
 				Log.v(CLASS_NAME, "Connected to BT " + keyAddrMap.get(connectedAddr) + " " + connectedAddr);
 				doAuthSeq();
 			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
 				Log.v("Disconnected", "DISCONNECTED!");
 				btConnection.stop();
 				btConnection.start();
 			}
 		}
 	};
 
 	private byte[] listenForMIChallenge() {
 		synchronized (magListener) {
 			try {
 				magListener.wait();
 			} catch (Exception e) { Log.e("Exception!", e.toString()); }
 		}
 		byte micData[] = magListener.getData();
 		try {
 			Log.v(CLASS_NAME, new String(micData, 0, 8, "ASCII"));
 		} catch (Exception e) { Log.e("Error!", e.toString()); }
 		return micData;
 	}
 
 	private void doAuthSeq() {
 		int response = BluetoothConnection.RESPONSE_NONE;
 
 		Log.v(CLASS_NAME, "Sending unlock command");
 		while (!(btConnection.write("dsadsa\r".getBytes())));
 
 		boolean miDataOK = false;
 		byte miData[] = new byte[8];
 
 		/* Keep looping until we get an ACK back from door, meaning our response
 		 * to the MI challenge was good.
 		 */
 		while (response != BluetoothConnection.RESPONSE_ACK) {
 
 			/* Loop until we have a possibly valid MI challenge.
 			 * Challenge comes from door, so it will be encrypted
 			 */
 			do {
 				miDataOK = true;
 				miData = listenForMIChallenge();
				byte miDataCopy[] = miData;	// Keep an unmodified copy so we can mess with the original
 				for (int i=0; i<8; i++) {
 					if (miData[i] > 'z' || miData[i] < 'A')	{	// Not A-Za-z, not valid
 						miDataOK = false;
 						break;
 					}
 				}
 				/* If that wasn't valid, try this... */
 				if (!miDataOK) {
 					ByteBuffer buf = ByteBuffer.wrap(miData);
 					long shifted = ((buf.getLong()) >>> 2) | 0x4000000000000000L;
 					ByteBuffer bufShifted = ByteBuffer.allocate(8);
 					bufShifted.putLong(shifted);
 					miData = bufShifted.array();
					miDataOK = true;
					for (int i=0; i<8; i++) {
						if (miData[i] > 'z' || miData[i] < 'A')	{	// Not A-Za-z, not valid
							miDataOK = false;
						}
					}
				}
				/* Still not valid, try something else... */
				if (!miDataOK) {
			        for (int j=0; j<8; j++) {
			            byte oldByte = miDataCopy[j];
			            byte newByte;
			            byte oldBit = (byte)((oldByte >> 7) & 0x1);
			            byte newBit;
			            newByte = (byte) (oldBit << 7);
			            for(int i=6; i>=0; i--) {
			                if (((oldByte >> i) & 0x1) == oldBit) {
			                    if (oldBit == 1)
			                        newBit = 0;
			                    else
			                        newBit = 1;
			                    newByte = (byte) (newByte | (newBit << i));
			                }
			                else {
			                    newByte = (byte) (newByte | (byte)(((oldByte >> i) & 0x1) << i));
			                }
			                oldBit = (byte)((oldByte >> i) & 0x1);
			            }
			            miDataCopy[j] = newByte;
			        }
			        miDataOK = true;
 					for (int i=0; i<8; i++) {
 						if (miData[i] > 'z' || miData[i] < 'A')	{	// Not A-Za-z, not valid
 							miDataOK = false;
 						}
 					}
 				}
 			} while (!miDataOK);
 
 			/* Reply to the door with the challenge we received, and wait for an ACK or NAK. */
 			try {
 				//btConnection.write(encryptData(miData, new byte[16]));
 				btConnection.write(miData);
 				btConnection.write("\r".getBytes());
 			} catch (Exception e) { Log.e("Exception!", e.toString()); }
 			synchronized (btConnection.mConnectedThread) {
 				try {
 					Log.v("before", "before wait");
 					btConnection.mConnectedThread.wait(1000);
 					Log.v("after", "after wait");
 				} catch (Exception e) { Log.e("Exception!", e.toString()); }
 			}
 			response = btConnection.getResponse();
 			Log.v("response", response+"");
 		}
 	}
 
 	private byte[] encryptData(byte[] data, byte[] encryptedData) {
 		byte[] encrypted = {};
 		byte[] paddedData = new byte [data.length + 16 - data.length%16];	// pad to 16 byte block size
 
 		Cipher cipher = null;
 		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
         System.arraycopy(data, 0, paddedData, 0, data.length);
 
         try {
             cipher = Cipher.getInstance("AES/ECB/NoPadding");
 		} catch (Exception e) { Log.e("Error!", e.toString()); }
 		try {
 			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
 		} catch (Exception e) { Log.e("Error!", e.toString()); }
 		try {
 			encrypted = cipher.doFinal(paddedData);
 		} catch (Exception e) { Log.e("Error!", e.toString()); }
 
 		encryptedData = encrypted;
 
 		return encrypted;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.v(CLASS_NAME, "Created");
 
 		IntentFilter conFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
 		IntentFilter disFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
 		registerReceiver(btReceiver, conFilter);
 		registerReceiver(btReceiver, disFilter);
 		btConnection = BluetoothConnection.getInstance();
 
 		magListener = new Magnetometer();
 		SensorManager sensorManager;
 		Sensor mag;
 
 		sensorManager = (SensorManager)getBaseContext().getSystemService(Context.SENSOR_SERVICE);
 		if ((mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)) != null) {
 			Log.v(CLASS_NAME, "There is a magnetomter!");
 			HandlerThread hThread = new HandlerThread("hThread");
 			hThread.start();
 			Handler h = new Handler(hThread.getLooper());
 			sensorManager.registerListener(magListener, mag, SensorManager.SENSOR_DELAY_FASTEST, h);
 		} else {
 			Log.v(CLASS_NAME, "There is no magnetomter!");
 		}
 
 		/* Load list of known keys/passes/ids */
 		fm = new FileManager(getBaseContext());
 		keyAddrMap = fm.getKeyAddrMap();
 	}
 
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.v(CLASS_NAME, "Started");
 
 		btConnection.start();
 
 		return START_NOT_STICKY;
 	}
 
 	@Override
 	public void onDestroy () {
 		super.onDestroy();
 		unregisterReceiver(btReceiver);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	public class LocalBinder extends Binder {
 		MainService getService() {
 			return MainService.this;
 		}
 	}
 }
