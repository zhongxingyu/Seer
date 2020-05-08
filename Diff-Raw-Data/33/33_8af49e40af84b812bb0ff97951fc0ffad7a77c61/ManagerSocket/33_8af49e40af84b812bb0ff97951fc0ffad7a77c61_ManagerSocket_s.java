 package bcmNetworking;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import bcmBackend.Friend;
 import bcmBackend.FriendList;
 import bcmGUI.ChatWindow;
 
 public class ManagerSocket extends Thread{
 	//Default port vales
 	private String takenPorts = "";
 	private FriendList friendList;
 	private String username;
 	
 	public ManagerSocket(FriendList friendList, String username){
 		this.friendList = friendList;
 		this.username = username;
 		this.start();
 	}
 	
 	public void run(){
 		
 		int port = BCMProtocol.MANAGER_PORT;
 		ServerSocket listener;
 		Socket connection;
 		BufferedReader incoming;
 		PrintWriter outgoing;
 		String messageIn;
 		Friend requestingFriend;
 		String reqIP;
 		int proposedPort;
 		Bridge bridge;
 		
 		while(true) { //Perpetually listen for incoming connections, but allowing only one build at a time
 			try{
 				//Initialize Server
 				listener = new ServerSocket(port);
 				System.out.println("Listening on port" + listener.getLocalPort());
 				
 				//Upon successful connection, close listener
 				connection = listener.accept();
 				listener.close();
 				
 				//Initialize Stream Writers and Readers
 				incoming  = new BufferedReader(new InputStreamReader(connection.getInputStream()) );
 				outgoing = new PrintWriter(connection.getOutputStream(),true);
 				ReceiveSocketSW rs;
 				SendSocketSW ss;
 				
 				//Send handshake
 				outgoing.println(BCMProtocol.HANDSHAKE);
 				//receive handshake and verify 
 				/*TODO: Create a loop that will wait for the right connection for an 
 				* appropriate amount of time, then timeout. 
 				*/
 				messageIn = incoming.readLine();
 				if(! BCMProtocol.HANDSHAKE.equals(messageIn)){
 					throw new Exception("Connected program is not a BCMsgr");
 				}
 				System.out.println("Handshake succesful. Now connected.");
 				
 				/*
 				 *Receive the proposed port from the RequestSocket 
 				 *Check if it is available, and if so, create the
 				 *ReceiveSocket on this end, and wait for the SendSocket from the other end
 				 */
 				while(true){ //The loop that handles the RequestSocket proposing & the ManagerSocket checking availability
 					
 					messageIn = incoming.readLine(); //Read the proposed port from the Request Socket
 					System.out.println("Connection requested at port " + messageIn);
 					proposedPort = Integer.parseInt(messageIn);//Translate the proposed port into an integer
 					
 					if(portAvailable(proposedPort)){ //If the port is available
 						
 						System.out.print("Proposed port good. Building ReceiveSocket and waiting of Requester...");
 						rs = new ReceiveSocketSW(proposedPort); //Build the ReceiveSocket on the Manager side
 						
 						outgoing.println("available"); //Inform Requester that socket is Available
 						
 						messageIn = incoming.readLine(); //Wait for the message from the Requester to connect to our ReceieveSocket
 						
 						System.out.println("Requester says " + messageIn);
 						
 						break;
 					} else { //If the port is not available
 						outgoing.println("not available");
 					}	
 				}
 				
 				//System.out.println("Attempting to build 2nd Half");
 				/*
 				 *Receive the 2nd proposed port from the RequestSocket 
 				 *Check if it is available, and if so, wait for the 
 				 *RequestSocket ot create its SendingSocket and connect to that with a SendSocket on this end
 				 */
 				while(true){
 					messageIn = incoming.readLine(); //Read the proposed port
 					System.out.println("Connection requested at port " + messageIn);				
 					proposedPort = Integer.parseInt(messageIn); //Translate the proposed port into an integer
 					
 					if(portAvailable(proposedPort)){
 						outgoing.println("available"); //Inform Requester that socket is available
 						System.out.print("Proposed port good. Waiting for requester to build ReceiveSocket...");
 						messageIn = incoming.readLine(); //Receive ready message from Requester
 						System.out.println("Requester says: " + messageIn);
 						
 						reqIP = messageIn;//retrieve the IP address of the Requester
 						reqIP = reqIP.substring(1);// Remove the "\" in the address
 						bridge = new Bridge();
 						ss = new SendSocketSW(reqIP, proposedPort, bridge);//Connect to the Requester's ReceiveSocket
 						outgoing.println("SenderSocket on Manager side built"); //Inform the Requester that we are connected
 
 						break;
 					} else { //If the current port is not available
 						outgoing.println("not available");
 					}
 				}
 			
 			
			ChatWindow cw = new ChatWindow(getRequestingFriend(reqIP),username,rs, ss, bridge);	
 			connection.setReuseAddress(true); //allow the default port to be reused after we close it
 			connection.close(); //Close the port so that we can listen again
 				
 			} catch (Exception e) {
 				System.out.println("An error occured while opening the connection.");
 				System.out.println(e.toString());
 				return;
 			}	
 		}
 	}
 	
 	/*
 	 *Checks if the port provided is available
 	 *@param port to be checked
 	 *@return true if available, false if not
 	 */
 	public boolean portAvailable(int port){
 		
 		if(port > BCMProtocol.MAX_PORT || port < BCMProtocol.MIN_PORT)
 			throw new IllegalArgumentException("Invalid start port: " + port);
 		
 		/*if(takenPorts.length() > 0 ){
 			String checkPorts[] = takenPorts.split(";");
 			for(String str : checkPorts){
 				if(port == Integer.parseInt(str))
 					return false;
 			}
 		}*/
 			
 		
 		ServerSocket ss = null;
 		try{
 			ss = new ServerSocket(port);
 			ss.setReuseAddress(true);
 			
 			
 			System.out.println("portAvailable says true");
 			
 			return true;
 		}catch (Exception e) {
 			System.out.println(e.toString());
 		} finally {
 			if(ss != null){
 				try{
 					ss.close();
 				} catch (IOException e) {
 					//Should not happen.
 				}
 				
 			}
 		} 
 		System.out.println("portAvailable says false");
 		return false;
 	}
 	
 	public String getTakenPorts(){
 		return this.takenPorts;
 	}
 	
 	public Friend getRequestingFriend(String reqIP){
 		Friend toReturn = friendList.searchFriends(reqIP);
 		if(toReturn == null)
 			toReturn = new Friend("unknown", "unknown", reqIP);
 		return toReturn;
 	}
 }
