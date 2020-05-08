 package communication;
 
 import java.net.InetAddress;
 import java.util.Random;
 
 import communication.rudp.socket.RUDPDatagram;
 import communication.rudp.socket.RUDPSocket;
 
 public class Main {
 
 	public static void main(String[] args) {
 		//Create 2 communication end points
 		/*DisseminationCore core1 = new DisseminationCore();
 		DisseminationCore core2 = new DisseminationCore();
 		CommunicationInterface comm1 = new RUDP(core1);
 		CommunicationInterface comm2 = new RUDP(core2);*/
 		
 		//Message msg;
 		try {
 			RUDPDatagram dgram;
 			InetAddress dst = InetAddress.getByName("10.13.1.122");
 
 
 			byte[] data = new byte[200000];
 			Random R = new Random();
 			R.nextBytes(data);
 
 			while(true) {
 				RUDPSocket sock = new RUDPSocket();
 				dgram = new RUDPDatagram(dst, 23456, data);
 				sock.send(dgram);
 				
 				Thread.sleep(1000);
			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		//Send data
 /*		msg = new TestMessage(comm1.getLocalIp(),comm2.getLocalIp(),null);
 		
 		try {
 			comm1.sendMessage(msg);
 		}
 		catch (DestinationNotReachableException e) {
 			
 		}*/
 		
 		//Shutdown
 		//comm1.shutdown();
 		//comm2.shutdown();
 	}
 }
