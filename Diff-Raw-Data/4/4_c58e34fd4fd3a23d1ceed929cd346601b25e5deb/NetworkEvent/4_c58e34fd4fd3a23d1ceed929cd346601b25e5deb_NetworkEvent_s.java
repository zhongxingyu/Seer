 /**
  * Network Event
  * 
  * @author jldupont
  */
 package com.systemical.android.eventor;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.NetworkInterface;
 
 import com.systemical.android.net.NetUtil;
 
 import android.content.Context;
 import android.util.Log;
 
 public class NetworkEvent {
 
 	private static final String TAG="Eventor.NetworkEvent";
 	protected Context c=null;
 	NetUtil net=null;
 
     private static final byte[] EVENTOR_ADDR =
         new byte[] {(byte) 239,(byte) 0,(byte) 0,(byte) 1};
     private static final int EVENTOR_PORT = 6666;
 	NetworkInterface ni=null;
	private MulticastSocket multicastSocket;
	private InetAddress groupAddress;
 	
 	
 	public NetworkEvent(Context context) {
 		c=context;
 		net=new NetUtil(c);
 		
 		// highly unlikely to fail!
 		try {
 			groupAddress = InetAddress.getByAddress(EVENTOR_ADDR);
 		}catch(Exception e) {
 			Log.e(TAG, "getByAddress: "+e.toString());
 		}		
 		maybeRefreshNetworkInterface();
 		maybeRefreshSocket();
 	}
 	
 	public void refresh() {
 		reset();
 		maybeRefreshNetworkInterface();
 		maybeRefreshSocket();
 	}
 	
 	protected void maybeRefreshSocket() {
 		if (multicastSocket==null)
 			try{ 
 				openSocket();
 			}catch(Exception e) {
 				multicastSocket=null;
 				Log.e(TAG, "socket open: "+e.toString());
 			}		
 	}
 	
 	protected void maybeRefreshNetworkInterface() {
 		if (ni==null) {
 			ni=net.getFirstWifiInterface();
 		}
 	}//
 
     private void openSocket() throws IOException {
     	if (ni!=null) {
 	        multicastSocket = new MulticastSocket(EVENTOR_PORT);
 	        multicastSocket.setTimeToLive(2);
 	        multicastSocket.setReuseAddress(true);
 	        multicastSocket.setNetworkInterface(ni);
 	        multicastSocket.joinGroup(groupAddress);
     	}
     }
 
     protected void reset() {
     	ni=null;
     	multicastSocket=null;
     }
     
     // ====================================================================== API
     
     /**
      * Send a data packet
      *  
      * @param data
      * @throws IOException 
      */
     public void sendData(String data) throws IOException {
     	DatagramPacket notif;
     	byte[] requestData=data.getBytes();
 		notif=new DatagramPacket(requestData, requestData.length, groupAddress, EVENTOR_PORT);
 		try {
 			multicastSocket.send(notif);
 		}catch(IOException e1) {
 			Log.v(TAG, "Error sending: "+e1.toString());
 			// let's try one more time
 			reset();
 			maybeRefreshNetworkInterface();
 			maybeRefreshSocket();
 			multicastSocket.send(notif);
 		}
     }//
 	
 }///
