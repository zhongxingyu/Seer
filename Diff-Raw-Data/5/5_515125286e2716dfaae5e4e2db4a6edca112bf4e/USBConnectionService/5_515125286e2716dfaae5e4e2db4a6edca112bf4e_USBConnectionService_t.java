 package de.uniulm.bagception.rfidapi.miniusbconnectionservice.service;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbManager;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import de.philipphock.android.lib.services.observation.ObservableService;
 import de.uniulm.bagception.broadcastconstants.BagceptionBroadcastContants;
 import de.uniulm.bagception.rfidapi.RFIDMiniMe;
 import de.uniulm.bagception.rfidapi.UsbCommunication;

 
 public class USBConnectionService extends ObservableService {
 
 	private PendingIntent mPermissionIntent;
 	private static final String ACTION_USB_PERMISSION = "com.mti.rfid.minime.USB_PERMISSION";
 
 	private UsbCommunication mUsbCommunication = UsbCommunication.newInstance();
 
 	public static final String SERVICE_NAME = "de.uniulm.bagception.rfidapi.miniusbconnectionservice.service.USBConnectionService";
 
 	private UsbManager mManager;
 
 	private boolean isConnected = false;
 
 	private static final int PID = 49193;
 	private static final int VID = 4901;
 
	private final de.uniulm.bagception.service.USBConnectionService.Stub mBinder = new de.uniulm.bagception.service.USBConnectionService.Stub() {
 
 		@Override
 		public boolean isConnected() throws RemoteException {
 			return USBConnectionService.this.isConnected;
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	public void foceRescanUSBState() {
 		scanDevice();
 	}
 
 	private void scanDevice() {
 		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
 		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
 		while (deviceIterator.hasNext()) {
 			UsbDevice device = deviceIterator.next();
 			if (device.getProductId() == PID && device.getVendorId() == VID) {
 				if (!mManager.hasPermission(device)) {
 					Log.d("USB", "No Permission.. requesting");
 					mManager.requestPermission(device, mPermissionIntent);
 					// TODO usbStateChanged(true) necessary?
 				}
 				usbStateChanged(true);
 				Log.d("USB", "PERMISSION");
 				return;
 
 			}
 		}
 		usbStateChanged(false);
 
 	}
 
 	BroadcastReceiver usbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			scanDevice();
 			String action = intent.getAction();
 
 			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) { // will
 				// by
 				// system
 				UsbDevice device = intent
 						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 
 				mUsbCommunication.setUsbInterface(mManager, device);
 
 				usbStateChanged(true);
 
 			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
 				// mUsbCommunication.setUsbInterface(null, null);//TODO causes
 				// nullpointerexception
 				usbStateChanged(false);
 				// getReaderSn(false);
 
 			} else if (ACTION_USB_PERMISSION.equals(action)) {
 				Log.d(UsbCommunication.TAG, "permission");
 				synchronized (this) {
 					UsbDevice device = intent
 							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						mUsbCommunication.setUsbInterface(mManager, device);
 						usbStateChanged(true);
 
 						RFIDMiniMe.setPowerLevelTo18();
 						RFIDMiniMe.sleepMode();
 					} else {
 						// finish(); TODO?
 					}
 				}
 			}
 		}
 	};
 
 	public static void sendRescanBroadcast(Context c) {
 		Intent i = new Intent();
 		i.setAction(BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_RESCAN);
 		c.sendBroadcast(i);
 	}
 
 	BroadcastReceiver rescanrecv = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			scanDevice();
 		}
 	};
 
 	BroadcastReceiver doRFIDScan = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			RFIDMiniMe.triggerInventory(context);
 		}
 	};
 
 	private void usbStateChanged(boolean connected) {
 		isConnected = connected;
 		String action = null;
 		if (connected) {
 			action = BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_CONNECTED;
 		} else {
 			action = BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_DISCONNECTED;
 		}
 		Intent i = new Intent();
 		i.setAction(action);
 		sendBroadcast(i);
 
 	}
 
 	@Override
 	protected void onFirstInit() {
 
 		mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 				ACTION_USB_PERMISSION), 0);
 
 		{
 			IntentFilter filter = new IntentFilter();
 			//filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
 			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
 			filter.addAction(ACTION_USB_PERMISSION);
 			registerReceiver(usbReceiver, filter);
 		}
 		{
 			IntentFilter filter = new IntentFilter();
 			filter.addAction(BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_RESCAN);
 			registerReceiver(rescanrecv, filter);
 		}
 		{
 			IntentFilter filter = new IntentFilter();
 			filter.addAction(BagceptionBroadcastContants.USB_CONNECTION_BROADCAST_RFIDSCAN);
 			registerReceiver(doRFIDScan, filter);
 
 		}
 
 	}
 
 	@Override
 	public void onDestroy() {
 		//unregisterReceiver(usbReceiver);
 		unregisterReceiver(rescanrecv);
 		unregisterReceiver(doRFIDScan);
 		super.onDestroy();
 	}
 
 	@Override
 	public String getServiceName() {
 
 		return SERVICE_NAME;
 	}
 
 
 
 }
