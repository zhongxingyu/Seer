package relay;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 public class RelayWorkerThread extends Thread {
 	Socket sktConnectFromClient;
 	Socket sktConnectToServer;
 
 	public RelayWorkerThread(Socket sktTemp) {
 		sktConnectFromClient = sktTemp;
 	}
 
 	public void run() {
 		InputStream ipsIncoming = null;
 		OutputStream opsOutGoing = null;
 
 		try {
 			// Get forwarding connection details from config file
 			sktConnectToServer = new Socket(RelayServer.getForwardHost(), RelayServer.getForwardPort());
 
 			ipsIncoming = sktConnectFromClient.getInputStream();
 			opsOutGoing = sktConnectToServer.getOutputStream();
 
 			RelayReceiverThread receiverThread = new RelayReceiverThread(sktConnectToServer, sktConnectFromClient);
 			receiverThread.start();
 
 			// Read Request from Client - Relay to Server
 			int intByteRead = ipsIncoming.read();
 
 			while (intByteRead != -1) {
 				opsOutGoing.write(intByteRead);
 				System.out.print((char) intByteRead);
 				intByteRead = ipsIncoming.read();
 			}
 
 			// Wait till all responses have been received
 			receiverThread.join();
 
 		} catch (Exception e) {
 
 		} finally {
 			try {
 				if (opsOutGoing != null) {
 					opsOutGoing.close();
 				}
 				if (ipsIncoming != null) {
 					ipsIncoming.close();
 				}
 
 				sktConnectFromClient.close();
 				sktConnectToServer.close();
 
 			} catch (Exception e) {
 
 			}
 		}
 	}
 
 }
