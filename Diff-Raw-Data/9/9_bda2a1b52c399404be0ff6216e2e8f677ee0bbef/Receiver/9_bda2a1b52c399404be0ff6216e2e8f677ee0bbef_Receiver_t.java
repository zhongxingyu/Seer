 package sta.andswtch.network;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Enumeration;
 
 import android.util.Log;
 
 public class Receiver implements Runnable {
 	
 	private DatagramSocket socket;
     private Thread thread;
     private boolean shutdown = true;
     DatagramPacket packet;
     IConnectionManager conManager;
     
     private static final String TAG = Receiver.class.getName();
 	
     
     public Receiver(ConnectionManager conManager){
     	this.conManager = conManager;
     }
     
     
 	public void start(int port) throws IOException {
 		
 		InetAddress localIP = getLocalIpAddress();
 		
 		Log.d(TAG, "try to create a DatagramSocket" + localIP.getHostAddress()+":"+port);
 
 		thread = new Thread(this, "Andswtch UDP_Server");
 		socket = new DatagramSocket(port, getLocalIpAddress());
 		
         if (shutdown == true){
         	Log.d(TAG, "starting server at  " + localIP.getHostAddress()+":"+port);
             thread.start();
         }
         else{
         	Log.e(TAG,"there is already a server running");
         	//TODO error handling?
         }
         
         
 	}
 	
 	
 	
 	public void run() {
 		try {
 			socket.setSoTimeout(3000);
 		
 			 byte[] buf = new byte[400];
 			
 			packet = new DatagramPacket(buf, buf.length);
 			Log.d(TAG, "Server: Receiving...");
 			
 			/* Receive the UDP-Packet */
 			socket.receive(packet);
 			Log.d(TAG, "Server: Received: '" + new String(packet.getData()) + "'");
 			Log.d(TAG, "Server: Done.");
 			
 			conManager.updateDatastructure(new String(packet.getData()));
 			
 			
 		}  catch (SocketTimeoutException e){
 			conManager.errorAlert();
			socket.close();
 		}  catch (Exception e) {
 			Log.e(TAG, "S: Error", e);
 		}
         socket.close();
         shutdown =true;
 	}
 	
 	
 	public InetAddress getLocalIpAddress() {
 	    try {
 	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 	            NetworkInterface intf = en.nextElement();
 	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 	                InetAddress inetAddress = enumIpAddr.nextElement();
 	                if (!inetAddress.isLoopbackAddress()) {
 	                	Log.d(TAG, "inetAddress is: " + inetAddress.getHostAddress().toString());
 	                    return inetAddress;
 	                }
 	            }
 	        }
 	    } catch (SocketException ex) {
 	        Log.e(TAG, "error while attemting to find out the local IP: "+ex.toString());
 	    }
 	    return null;
 	}
 }
