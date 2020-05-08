 /**
  * Name1 StudentNumber1
  * Name2 StudentNumber2
  * Name3 StudentNumber3
  */
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 /**
  * Client 
  * Skeleton code for Multicast client
  */
 public class MulticastSender {
 	
 	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
 	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
 	
 	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet      
 	
 	MulticastSocket socket;
 	InetAddress address;
 	int port;
 	
 	/**
 	 * Default Constructor
 	 * 
 	 * Fills an instance with the hardcoded values
 	 */
 	public MulticastSender() {
 		this(MCAST_ADDR, MCAST_PORT);
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * Creates an instance with specific values for the 
 	 * address and port of the multicast group 
 	 * 
 	 * @param addr Address of the multicast group as string
 	 * @param port Port number of the server 
 	 */
 	public MulticastSender(String addr, int port) {
 		try {
 			this.port= port;
 			address = InetAddress.getByName(addr);
 			socket = new MulticastSocket(port);
 			socket.joinGroup(address);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	
 	/**
 	 * Run method
 	 *
 	 * This method sends a datagram with the strnig "Data?" to a server and
 	 * then enters an endless loop in which it attempts to receive datagrams
 	 * and prints the content of received datagrams.
 	 */
 	public void run(){
 		String msg = "Date?";
 		byte[] buffer;
 		DatagramPacket packet = null;
 		InputStreamReader converter = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(converter);
 		String input = "";
 		int ans = 0;
 		
 		try {
 			
 			// send datagram to server - asking for date
 			packet = new DatagramPacket(msg.getBytes(),	msg.length(), 
 					address, port);
 			socket.send(packet);
 			System.out.println("Send Msg");
 			
 			do{
 				
 				System.out.print("0) End the program.\n1) Connect to the system.\n 2) Send a Command\n");
 				input = in.readLine();//Input command.
 				ans = Integer.parseInt(input);
 
 				switch(ans){
 
 				case 0://End the program
 					break;
 				case 1://Connect to the multicast server
					
 					break;
 				case 2://Send a command
					//sendMessage(0, input, socket, input);
 					break;
 				default://Not a valid command
 					System.out.print("This is not a valid option.\n");
 					break;
 					
 				}
 				
 			}while(ans != 0);
 			
 		} catch(Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	private void sendMessage(byte type, String dest, MulticastSocket socket, String data)
 	{
 		byte[] buffer;
 		
 		
 	}
 	
 	public void queueMessage(byte type, String dest, MulticastSocket socket, String data)
 	{
 		
 	}
 	
 }
