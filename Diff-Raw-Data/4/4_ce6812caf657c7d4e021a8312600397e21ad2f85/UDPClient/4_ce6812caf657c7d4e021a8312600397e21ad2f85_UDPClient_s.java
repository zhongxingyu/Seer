 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 
 
 public class UDPClient {
 	
 	private DatagramSocket clientSocket;
 	private byte[] sendingData = new byte[1000];
 	private byte[] receivedData = new byte[1000];
 	private ArrayList<T_Packet> received = new ArrayList<T_Packet>();
 	private int experimentTime = 30; /*experiment duration in seconds*/ 
 	
 	
 	private void SendMsg(InetAddress serverAddress, int port, String msg) throws IOException{
 		Date start;
 		Date end = new Date();
 		sendingData = msg.getBytes();
 		DatagramPacket sendPacket = new DatagramPacket(sendingData, sendingData.length, serverAddress, port);
 		clientSocket.send(sendPacket);
 		System.out.println("Client sent a request to " + serverAddress.toString() + " port " + port + " msg " + msg);
 
 		/*client starts listening*/
 		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
 		start = new Date();	/*get current time*/
 		while ( (end.getTime()-start.getTime())/1000 < experimentTime ){
 			clientSocket.receive(receivedPacket);
 			//Something received from the server
 			System.out.println("Client: " + receivedData.toString());
 			received.add(new T_Packet(Integer.parseInt(receivedData.toString())));
 			end = new Date();
 		}
 		System.out.println("Experiment ended");
 	}
 	
 	private void CalculateResult(){
 		
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws UnknownHostException 
 	 */
 	public static void main(String[] args) throws UnknownHostException, IOException {
 		// TODO Auto-generated method stub
 		UDPClient client = new UDPClient();
		client.SendMsg(InetAddress.getByName("125.22.22.6"), 6789, "test_request");
 		client.CalculateResult();
 	}
 
 }
