 package com.smartpark.background;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 import com.smartpark.bluetooth.BlueController;
 
 import android.bluetooth.BluetoothDevice;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BackgoundOperationThread extends Thread {
 	
 	/*
 	 * keeps a list of booleans to determine if all activities have been
 	 * destroyed so that the thread wont continue running for ever.
 	 */
 	public boolean activityMAIN = false;
 	public boolean activitySettings = false;
 	public boolean activityLOGIN = false;
 	
 	private static long shutdownTime = 0; // 0 = never
 	
 	// TRANSMITBUFFERS
 	private LinkedList<String> btTransmitBuffer = new LinkedList<String>();
 	private LinkedList<String> tcpTransmitBuffer = new LinkedList<String>();
 	
 	// Debugging and stuff
 	private static final String TAG = "bgThread";
 	private static final boolean D = Ref.D;
 	
 	// Device to connect to
 	private final String SMARTPARK_DEVICE = "HC-06-SLAVE";
 	
 	private BufferedReader bufferedReader;
 	
 	private boolean run = true;
 	
 	public BackgoundOperationThread() {
 		Log.e(TAG, "++ bgThread Constructor ++");
 		
 		// TODO
 	}
 	
 	@Override
 	public void run() {
 		Log.e(TAG, "++ bgThread started ++");
 		String btInData = null;
 		String tcpInData = null;
 		run = true;
 		// ===========================================================
 		while (run) {
 			
 			if (Ref.btState == Ref.STATE_CONNECTED) {
 				// Code to process
 				try {
 					Log.d(TAG, "--> reading started");
 
 					btInData = btRead();
 					Log.d(TAG, "--> DATA read                  " + btInData);
 					Integer t = Integer.parseInt(btInData);
 					if (t != 10) {
 						t++;
 						Log.d(TAG, "Will now send: " + Integer.toString(t));
 						sendByBT(Integer.toString(t));
 					}
 					Log.d(TAG, "--> reading started");
 				} catch (NumberFormatException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				while (btTransmitBuffer.size() > 0
 						&& Ref.getbtState() == Ref.STATE_CONNECTED) {
 					Log.d(TAG, "BT sending data");
 					btWrite();
 					
 				}
 				// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
 			} else {
 				// Handle reconnection
 				Log.e(TAG, "BT not connected: handling error");
 				if (Ref.btDevice == null || Ref.btState != Ref.STATE_CONNECTED) {
 					Ref.btState = Ref.STATE_CONNECTING;
 					
 					if (Ref.btController == null) {
 						Log.e(TAG, "BlueController intance recreate");
 						Ref.btController = new BlueController();
 					}
 					
 					BluetoothDevice device = Ref.btController
 							.getPairedDeviceByName(SMARTPARK_DEVICE);
 					if (device == null) {
 						Ref.btController.findNearbyDevices(Ref.mainActivity);
 						for (int i = 0; i < 10; i++) {
 							device = Ref.btController
 									.getFoundDeviceByName(SMARTPARK_DEVICE);
 							
 							if (device != null
 									&& device.getName()
 											.equals(SMARTPARK_DEVICE)) {
 								
 							}
 							Toast.makeText(Ref.mainActivity,
 									"Bluetooth avaialbe", Toast.LENGTH_SHORT)
 									.show();
 							try {
 								BackgoundOperationThread.sleep(1200);
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								Log.e(TAG, "Interrupted Exception occured" + e);
 							}
 						}
 					}
 					if (device != null) {
 						Ref.btDevice = device;
 						Ref.btController.connect();
 						Log.d(TAG, "--> connected to " + device.getAddress());
 						bufferedReader = new BufferedReader(
 								new InputStreamReader(Ref.btInStream));
 					} else {
 						Log.w(TAG, "--> device is null, bluetooth not found");
 					}
 				}
 			}// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
 
 			if (Ref.tcpState == Ref.STATE_CONNECTED) {
 				// Code to process
 				
 				// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
 			} else {
 				// Handle reconnection
 				
 			}// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
 			
 			// -----------------------------------------------------
 			// -----------------------------------------------------
 			// -----------------------------------------------------
 			// -----------------------------------------------------
 			
 			Log.d(TAG, "BT buffer size: " + btTransmitBuffer.size());
 			
 			// Check to see if the thread needs to start shutting down
 			// Log.d(TAG, "--> thread running");
 			try {
 				Thread.sleep(3000);
 			} catch (InterruptedException e) {
 			}
 			
 			if (activityMAIN || activitySettings || activityLOGIN) {
 				shutdownTime = 0;
 				// Log.d(TAG, "thread not idled");
 			} else {
 				Log.d("TAG", "--> bgThread timer started");
 				if (shutdownTime == 0) {
 					shutdownTime = System.currentTimeMillis();
 				} else if (System.currentTimeMillis() - shutdownTime > 5000) {
 					shutdownThread();
 					run = false;
 					Log.i(TAG, "--> Shutting down thread");
 				}
 				if (D)
 					Log.d("BackThread",
 							"thread is shutting down"
 									+ (System.currentTimeMillis() - shutdownTime));
 				try {
 					Thread.sleep(3000);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		Log.i(TAG, "--> Thread is shutdown");
 	}
 	
 	private String btRead() {
 		Log.e(TAG, "++ btRead ++");
 		String inData = null;
 		if (Ref.btInStream != null) {
 			Log.e(TAG, "iStream good");
 			try {
 				if (bufferedReader == null) {
 					if (Ref.btInStream != null) {
 						bufferedReader = new BufferedReader(
 								new InputStreamReader(Ref.btInStream));
 						Log.e(TAG, "bufferedReader was = null");
 					}
 				}
 				Log.e(TAG, "bufferedreader good");
 				if (bufferedReader.ready()) {
 					Log.e(TAG, "reader ready");
 					inData = bufferedReader.readLine();
 					Log.d(TAG, "DATA= " + bufferedReader.readLine());
 				}
 			} catch (IOException e1) {
 				if (Ref.getbtState() != Ref.STATE_CONNECTED) {
 					Ref.setbtState(Ref.STATE_NOT_CONNECTED);
 					Log.e(TAG, "btSocket not connected");
 				}
 			}
 		}else{
 			Ref.btState = Ref.STATE_NOT_CONNECTED;
 		}
 		return inData;
 	}
 
 	private void btWrite() {
 		Log.e(TAG, "++ btWrite ++");
 		byte[] data = btTransmitBuffer.removeFirst().getBytes();
 		try {
 			Ref.btOutStream.write(data);
 		} catch (IOException e1) {
 			Log.e(TAG, "Sending of data with bt failed" + e1);
 			if (Ref.btState != Ref.STATE_CONNECTED) {
 				Ref.btState = Ref.STATE_NOT_CONNECTED;
 				Log.e(TAG, "btSocket set to NOT CONNECTED");
 			}
 		}
 	}
 	
 	private void shutdownThread() {
 		Log.e(TAG, "++ shutdownThread ++");
 		// Do not invoke method that forcefully shut a thread down.
 		// Let the run method run out.
 		//		this.shutdownThread();
 		try {
 			Ref.btSocket.close();
 		} catch (IOException e) {
 		}
 		Ref.bgThread = null;
 	}
 	
 	// The next two methods put strings in transmitbuffer
 	public void sendByBT(String data) {
 		Log.e(TAG, "++ sendByBT ++");
 		btTransmitBuffer.addLast(data + "\r\n");
 	}
 
 	public void sendByTCP(String data) {
 		Log.e(TAG, "++ sendByTCP ++");
 		tcpTransmitBuffer.addLast(data);
 	}
 
 }
