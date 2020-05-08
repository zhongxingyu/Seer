 package com.ventus.smartphonequadrotor.qphoneapp.util.bluetooth;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.Message;
 import android.util.Log;
 
 import com.ventus.smartphonequadrotor.qphoneapp.activities.BluetoothConnectionActivity;
 import com.ventus.smartphonequadrotor.qphoneapp.services.MainService;
 import com.ventus.smartphonequadrotor.qphoneapp.util.control.ControlLoop;
 import com.ventus.smartphonequadrotor.qphoneapp.util.control.DataAggregator;
 
 /**
  * Manager bluetooth connection and data transfer.
  * @author abhin
  *
  */
 public class BluetoothManager {
 	private static final String TAG = BluetoothManager.class.getName();
 	private static final UUID bluetoothConnectionUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 	private BluetoothAdapter adapter;
 	private BluetoothDevice device;
 	private BluetoothSocket socket;
 	private InputStream inputStream;
 	private OutputStream outputStream;
 	private Object socketLock = new Object();
 	private Object inputStreamLock = new Object();
 	private Object outputStreamLock = new Object();
 	private MainService owner;
 	private QcfpHandlers packetHandlers;
 	private QcfpParser bluetoothDataParser;
 	
 	public BluetoothManager(MainService owner) {
 		this.owner = owner;
 		adapter = BluetoothAdapter.getDefaultAdapter();
 		this.packetHandlers = new QcfpHandlers();
 		bluetoothDataParser =  new QcfpParser(QcfpParser.QCFP_MAX_PACKET_SIZE, packetHandlers);
 		packetHandlers.registerHandler(QcfpCommands.QCFP_ASYNC_DATA, asyncDataCallback);
 		packetHandlers.registerHandler(QcfpCommands.QCFP_FLIGHT_MODE, flightModeCallback);
 		packetHandlers.registerHandler(QcfpCommands.QCFP_CALIBRATE_QUADROTOR, calibrationStatusCallback);
 	}
 	
 	/**
 	 * This getter is for {@link DataAggregator} so that it can read from the bluetooth
 	 * input stream.
 	 * @return
 	 */
 	public InputStream getInputStream() {
 		return this.inputStream;
 	}
 	
 	/**
 	 * This method obviously connects the bluetooth device to the 
 	 * phone.
 	 * @param macAddress
 	 * @throws Exception 
 	 */
 	public void connect(String macAddress) {
 		device = adapter.getRemoteDevice(macAddress);
 		
 		try {
 			synchronized(socketLock) {
 				socket = device.createRfcommSocketToServiceRecord(bluetoothConnectionUuid);
 			}
 			
 			new Thread(new Runnable(){
 				public void run() {
 					InputStream tempIn = null;
 					OutputStream tempOut = null;
 					try {
 						synchronized (socketLock) {
 							socket.connect();						
 						}
 						tempIn = socket.getInputStream();
 						tempOut = socket.getOutputStream();
 						
 						synchronized(inputStreamLock) {
 							inputStream = tempIn;
 						}
 						synchronized(outputStreamLock) {
 							outputStream = tempOut;
 						}
 						sendConnectionSuccess();
 					} catch (IOException ex) {
 						sendConnectionFailure();
 					}
 				}
 			}, "BluetoothConnectionThread").start();
 		} catch (Exception ex) {
 			sendConnectionFailure();
 		}
 	} 
 	
 	private void sendConnectionFailure() {
 		Intent intent = new Intent(BluetoothConnectionActivity.BLUETOOTH_CONNECTION_STATUS_UPDATE);
 		intent.putExtra(
 			BluetoothConnectionActivity.BLUETOOTH_CONNECTION_STATUS, 
 			BluetoothConnectionActivity.BLUETOOTH_STATUS_CONNECTION_FAILURE
 		);
 		owner.sendBroadcast(intent);
 	}
 	
 	private void sendConnectionSuccess() {
 		Intent intent = new Intent(BluetoothConnectionActivity.BLUETOOTH_CONNECTION_STATUS_UPDATE);
 		intent.putExtra(
 			BluetoothConnectionActivity.BLUETOOTH_CONNECTION_STATUS, 
 			BluetoothConnectionActivity.BLUETOOTH_STATUS_CONNECTED
 		);
 		owner.sendBroadcast(intent);
 		bluetoothReader.start();
 	}
 	
 	/**
 	 * This method can be used to send messages to the QCB over bluetooth. This method
 	 * is synchronous and thus can be accessed from the control loop directly.
 	 * @param message
 	 * @throws IOException
 	 */
 	public void write(byte[] message) throws IOException {
 		synchronized(outputStreamLock){
 			if (outputStream != null)
 				outputStream.write(message);
 		}
 	}
 	
 	/**
 	 * This thread is an infinite loop that contains a blocking call to the
 	 * bluetooth input stream. 
 	 */
 	public Thread bluetoothReader = new Thread("BluetoothReaderThread") {
 		public static final int BUFFER_SIZE = 2*QcfpParser.QCFP_MAX_PACKET_SIZE;
 		
 		@Override
 		public void run() {
 			byte[] buffer = new byte[BUFFER_SIZE];
 			
 			while (true) {
 				try {
 					if (owner.getBluetoothManager().getInputStream() == null) {
 						try {
 							Thread.sleep(200);
 						} catch (InterruptedException e) {
 							//whatever
 						}
 						continue;
 					}
 					//the following is a blocking call.
 					int length = owner.getBluetoothManager().getInputStream().read(buffer);
 					bluetoothDataParser.addData(buffer, length);
 				} catch (IOException e) {
 					Log.e(TAG, "Could not read from QCB", e);
 				}
 			}
 		}
 	};
 	
 	/**
 	 * This callback receives data from the bluetooth. The data is in the form of a 
 	 * byte array and needs to be parsed according to the QCFB protocol guide in the
 	 * project documents folder on google docs.
 	 * The data contains information regarding whether the QCB has entered flightmode
 	 * (armed state) or not.
 	 */
 	private QcfpCallback flightModeCallback = new QcfpCallback() {
 		private static final int CMD41_ENABLE_INDEX = 1;
 		
 		@Override
 		public void run(byte[] packet, int length) {
 			//check if the length of the command is appropriate. 
 			if (length == 2) {
 				int flightMode = packet[CMD41_ENABLE_INDEX];
 				owner.flightModeReceivedfromQcb(flightMode);
 			}
 		}
 	};
 	
 	/**
 	 * This callback receives data from the bluetooth. The data is in the form of a 
 	 * byte array and needs to be parsed according to the QCFB protocol guide in the
 	 * project documents folder on google docs.
 	 * The data contains information regarding whether the QCB has entered calibration mode
 	 * or not.
 	 */
 	private QcfpCallback calibrationStatusCallback = new QcfpCallback() {
 		private static final int CMD40_ENABLE_INDEX = 1;
 		
 		@Override
 		public void run(byte[] packet, int length) {
 			//check if the length of the command is appropriate. 
 			if (length == 2) {
 				int calibrationStatus = packet[CMD40_ENABLE_INDEX];
 				owner.calibrationStatusReceivedfromQcb(calibrationStatus);
 			}
 		}
 	};
 	
 	/**
 	 * This callback receives data from the bluetooth. The data is in the form of a 
 	 * byte array and needs to be parsed according to the QCFB protocol guide in the
 	 * project documents folder on google docs.
 	 */
 	private QcfpCallback asyncDataCallback = new QcfpCallback() {
 		private static final int CMD10_DATA_SOURCE_INDEX = 1;
 		private static final int DATA_SOURCE_ACCEL = 0x01;
 		private static final int DATA_SOURCE_GYRO = 0x02;
 		private static final int DATA_SOURCE_MAG = 0x03;
 		private static final int DATA_SOURCE_KIN = 0x06;
 		private static final int DATA_SOURCE_HEIGHT = 0x07;
 		
 		private static final int ACCEL_PAYLOAD_LENGTH = 12;
 		private static final int GYRO_PAYLOAD_LENGTH = 12;
 		private static final int MAG_PAYLOAD_LENGTH = 12;
 		private static final int KIN_PAYLOAD_LENGTH = 18;
 		private static final int HEIGHT_PAYLOAD_LENGTH = 8;
 		
 		private static final int TIMESTAMP_START_INDEX = 2;
 		
 		private static final int X_INDEX_LSB = 6;
 		private static final int X_INDEX_MSB = 7;
 		private static final int Y_INDEX_LSB = 8;
 		private static final int Y_INDEX_MSB = 9;
 		private static final int Z_INDEX_LSB = 10;
 		private static final int Z_INDEX_MSB = 11;
 		private static final int HEIGHT_INDEX_LSB = 6;
 		private static final int HEIGHT_INDEX_MSB = 7;
 		
 		private static final int ROLL_START_INDEX = 6;
 		private static final int PITCH_START_INDEX = 10;
 		private static final int YAW_START_INDEX = 14;
 		
 		@Override
 		public void run(byte[] packet, int length) {
 			// Require command id, data source, 4 timestamp, and at least 1 payload
 			if(length >= 7)
 			{
 				// values from tri axis sensors
 				int x, y, z;
 				
 				// Timestamp is unsigned
 				long timestamp =
 						((packet[TIMESTAMP_START_INDEX]   <<  0) & 0x00000000FF) |
 						((packet[TIMESTAMP_START_INDEX+1] <<  8) & 0x000000FF00) |
 						((packet[TIMESTAMP_START_INDEX+2] << 16) & 0x0000FF0000) |
 						((packet[TIMESTAMP_START_INDEX+3] << 24) & 0x00FF000000);
 				
 				// sensor data is signed
 				switch(packet[CMD10_DATA_SOURCE_INDEX])
 				{
 				case DATA_SOURCE_ACCEL:
 					if(length == ACCEL_PAYLOAD_LENGTH)
 					{
 						x = (packet[X_INDEX_LSB] & 0x00FF) | (packet[X_INDEX_MSB] << 8);
 						y = (packet[Y_INDEX_LSB] & 0x00FF) | (packet[Y_INDEX_MSB] << 8);
 						z = (packet[Z_INDEX_LSB] & 0x00FF) | (packet[Z_INDEX_MSB] << 8);
 						//kinematicsEstimator.registerAccelValues(x, y, z, timestamp);
 						Log.d(TAG, String.format("Accelerometer: X: %f Y: %f Z: %f", x, y, z));
 					}
 					break;
 				case DATA_SOURCE_GYRO:
 					if(length == GYRO_PAYLOAD_LENGTH)
 					{
 						x = (packet[X_INDEX_LSB] & 0x00FF) | (packet[X_INDEX_MSB] << 8);
 						y = (packet[Y_INDEX_LSB] & 0x00FF) | (packet[Y_INDEX_MSB] << 8);
 						z = (packet[Z_INDEX_LSB] & 0x00FF) | (packet[Z_INDEX_MSB] << 8);
 						//kinematicsEstimator.registerGyroValues(x, y, z, timestamp);
 						Log.d(TAG, String.format("Gyroscope: X: %f Y: %f Z: %f", x, y, z));
 					}
 					break;
 				case DATA_SOURCE_MAG:
 					if(length == MAG_PAYLOAD_LENGTH)
 					{
 						x = (packet[X_INDEX_LSB] & 0x00FF) | (packet[X_INDEX_MSB] << 8);
 						y = (packet[Y_INDEX_LSB] & 0x00FF) | (packet[Y_INDEX_MSB] << 8);
 						z = (packet[Z_INDEX_LSB] & 0x00FF) | (packet[Z_INDEX_MSB] << 8);
 						//kinematicsEstimator.registerMagValues(x, y, z, timestamp);
 						Log.d(TAG, String.format("Magnetometer: X: %f Y: %f Z: %f", x, y, z));
 					}
 					break;
 				case DATA_SOURCE_KIN:
 					if(length == KIN_PAYLOAD_LENGTH)
 					{
 						Float yaw, pitch, roll;
 						// Assuming roll, pitch, yaw corresponds to x, y, z and that that is
 						// the order the values are sent in.
 						// Kinematics angles are in radians.
						roll = QcfpCommunication.decodeFloat(packet, ROLL_START_INDEX);
 						pitch = QcfpCommunication.decodeFloat(packet, PITCH_START_INDEX);
						yaw = QcfpCommunication.decodeFloat(packet, YAW_START_INDEX);
 						
 						Message msg = owner.getControlLoop().handler.obtainMessage(ControlLoop.CMAC_UPDATE_MESSAGE);
 						msg.obj = new Object[] {timestamp, 0, roll, pitch, yaw};
 						owner.getControlLoop().handler.sendMessage(msg);
 						owner.getNetworkCommunicationManager().sendKinematicsData(timestamp, roll, pitch, yaw);
 					}
 					break;
 				case DATA_SOURCE_HEIGHT:
 					if(length == HEIGHT_PAYLOAD_LENGTH)
 					{
 						int height = (packet[HEIGHT_INDEX_LSB] & 0x00FF) + ((packet[HEIGHT_INDEX_MSB] & 0x00FF) << 8);
 						// TODO: Do something with the height. Height is in cm.
 						// The value isn't reliable when the height is approximately less than 20cm.
 					}
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 }
