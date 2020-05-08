 package de.uulm.mi.ubicom.proximity.proximity_periphery_bluetooth_test.bluetooth;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Locale;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.os.ParcelUuid;
 import android.util.Log;
 
 public class BTClient implements Runnable{
 	private Thread recvThread;
 	private volatile boolean listening=true;
 	private BluetoothSocket clientSocket;
 	private final BluetoothDevice device;
 	private final ParcelUuid service;
     private InputStream clientSocketInStream;
     private OutputStream clientSocketOutStream;
 	
 	public BTClient(BluetoothDevice device, ParcelUuid service) throws IOException {
 		
 		this.device = device;
 		this.service = service;
 		
 	}
 	
 	//executes itself in another thread and listens
 	public void startListeningForIncomingBytes(){
 		recvThread = new Thread(this);
 		recvThread.start();
 	}
 	
 
 	
     public void run() {
     	 byte[] buffer = new byte[1024];  // buffer store for the stream
         // Cancel discovery because it will slow down the connection.
         
         try {//cannot throw here of course (the method who called start will already be somewhere else because of the thread
             // Connect the device through the socket. This will block
             // until it succeeds or throws an exception.
    
        	clientSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(service.getUuid().toString().toUpperCase(Locale.GERMANY)));
 
             Log.d("bt",clientSocket.toString());
             clientSocketInStream = clientSocket.getInputStream();
     		clientSocketOutStream = clientSocket.getOutputStream();
             clientSocket.connect();
             Log.d("bt","connected");
         } catch (IOException connectException) {
             // Unable to connect; close the socket and get out.
         	connectException.printStackTrace();
             try {
             	clientSocket.close();
             } catch (IOException closeException) {
             	closeException.printStackTrace();
             }
             return;
         }
 
         // manage the connection 
         int bytes = 0;
 		while (listening){
 			Log.d("bt","listen");
 			try {
                 // Read from the InputStream
 				Log.d("bt","before read");
                 bytes = clientSocketInStream.read(buffer);
                 Log.d("bt","after read");
                 if (bytes == -1){
                 	Log.d("bt","close");
                 	//connection remotely closed
                 	listening = false;
                 	break;
                 }
                 // Send the obtained bytes to the UI Activity.
                 
                 //send to UI
                 /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                         .sendToTarget();
                  */
                Log.d("input",new String(buffer,0,bytes));
             } catch (IOException e) {
             	e.printStackTrace();
                 break;
             }
 		}
     }
 
     /** Will cancel an in-progress connection, and close the socket. 
      * @throws IOException */
     public void cancel() throws IOException {
     	listening = false;
         clientSocket.close();
 
     }	
 	
     
     public void write(String s) throws IOException{
     	clientSocketOutStream.write(s.getBytes());
     }
 }
