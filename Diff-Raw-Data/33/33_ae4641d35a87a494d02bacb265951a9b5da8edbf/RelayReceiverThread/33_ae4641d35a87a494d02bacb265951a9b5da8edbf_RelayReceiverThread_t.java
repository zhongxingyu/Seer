package org.relay;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 /**
  * Incoming thread class
  * 
  * 
  */
 public class RelayReceiverThread extends Thread {
 	private Socket sktConnectFromServer;
 	private Socket sktConnectToClient;
 
 	public RelayReceiverThread(Socket sktFromServer, Socket sktToClient) {
 		sktConnectFromServer = sktFromServer;
 		sktConnectToClient = sktToClient;
 	}
 
 	public void run() {
 		InputStream ipsIncoming = null;
 		OutputStream opsOutgoing = null;
 
 		try {
 			ipsIncoming = sktConnectFromServer.getInputStream();
 			opsOutgoing = sktConnectToClient.getOutputStream();
 
 			// Read Response from Server - Relay to Client
 			int intByteRead = ipsIncoming.read();
 
 			while (intByteRead != -1) {
 				opsOutgoing.write(intByteRead);
 				System.out.print((char) intByteRead);
 				intByteRead = ipsIncoming.read();
 			}
 
 		} catch (Exception e) {
 
 		} finally {
 			try {
 
 				opsOutgoing.flush();
 
 				if (ipsIncoming != null) {
 					ipsIncoming.close();
 				}
 				if (opsOutgoing != null) {
 					opsOutgoing.close();
 				}
 
 			} catch (Exception e) {
 
 			}
 		}
 	}
 
 }
