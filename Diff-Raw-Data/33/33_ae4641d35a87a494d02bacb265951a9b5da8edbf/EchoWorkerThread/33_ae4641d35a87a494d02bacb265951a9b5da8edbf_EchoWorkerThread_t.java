package org.relay;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 /**
  * Listen on port and echo incoming to stdout
  * 
  */
 public class EchoWorkerThread extends Thread {
 	Socket sktIncomingClient;
 
 	public EchoWorkerThread(Socket sktTemp) {
 		sktIncomingClient = sktTemp;
 	}
 
 	public void run() {
 		try {
 
 			InputStream ips = sktIncomingClient.getInputStream();
 			OutputStream ops = sktIncomingClient.getOutputStream();
 
 			System.out.println("Connection From : " + sktIncomingClient.getRemoteSocketAddress().toString());
 
 			int intByteRead = ips.read();
 			while (intByteRead != -1) {
 
 				System.out.write((char) intByteRead);
 				ops.write(intByteRead);
 				intByteRead = ips.read();
 			}
 
 			System.out.println("Connection Closed!");
 			ips.close();
 			ops.close();
 			sktIncomingClient.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
