 package bcmNetworking;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.swing.JOptionPane;
 
 import bcmBackend.Friend;
 import bcmGUI.ChatWindow;
 
 public class RequestSocket extends Thread {
 	//Information passed to the Socket to facilitate creation
 	private Friend selectedFriend;
 	private String username;
 	
 	public RequestSocket(Friend selectedFriend, String username){
 		this.selectedFriend = selectedFriend;
 		this.username = username;
 		this.start();
 		
 	}
 	
 	public void run(){
 		String commandTyped;	
 		Socket connection = null;
 		BufferedReader incoming;
 		PrintWriter outgoing;
 		String messageIn;
 		int proposedPort;
 		ReceiveSocketSW rs = null;
 		SendSocketSW ss = null;
 		Bridge bridge = new Bridge();
 		ChatWindow cw = null;
 		
 		if(selectedFriend == null)
 			System.out.println("SelectedFriend = null");
 		cw = new ChatWindow(selectedFriend, username, bridge);
 		try{
 			//JOptionPane.showMessageDialog(null, "Connection Attempting. Please wait.");
 			try{
 			connection = new Socket(selectedFriend.getIP(), BCMProtocol.MANAGER_PORT);
 			} catch (Exception ce) {
 				System.out.println(ce.toString());
 				cw.dispose();
 				JOptionPane.showMessageDialog(null, "Could not Connect.");
 				return;
 			}
 			System.out.println("Socket Built");
 			incoming = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
 			outgoing = new PrintWriter(connection.getOutputStream(), true);
 			
 			//Send out handshake
 			outgoing.println(BCMProtocol.HANDSHAKE);
 			
 			//Verify handshake
 			messageIn = incoming.readLine();
 			if(! BCMProtocol.HANDSHAKE.equals(messageIn)){
 				throw new Exception("Connected program is not a BCMsgr");
 			}
 			System.out.println("Handshake Successful. Now Connected.");
 			
 			//Propose & Build the first connection -> Manager:Receiver::Requester:Sender
 			proposedPort = proposePort();
 			
 			while(true){
 				
 				outgoing.println(proposedPort);
 				System.out.println("Sending " + proposedPort + " to Manager.");
 				
 				messageIn = incoming.readLine();
 				System.out.println("Received from Manager: \"" + messageIn + "\"");
 				
 				if(messageIn.equalsIgnoreCase("available") == true){ //If proposed port is available
 					System.out.println("Manager says port is available");
 					ss = new SendSocketSW(selectedFriend.getIP(),proposedPort, bridge); //Build a Sender
 					System.out.println("SendSocket built on Requester Side");
 					outgoing.println("completed");//Inform the Manager that connection building is complete
 					break;
 				} else {
 					System.out.println("Manager says:" + messageIn);
 					//get the next available port
 					proposedPort++;
 					while(!portAvailable(proposedPort))
 						proposedPort++;
 					if(proposedPort > BCMProtocol.MAX_PORT){
 						connection.setReuseAddress(true);
 						connection.close();
 						throw new IllegalArgumentException("No Ports Available.");
 					}
 				}
 			}
 			
 			System.out.println("Attempting to build 2nd connection");
 			//Propose & Build the 2nd connection -> Manager:Sender::Requester:Receiver
 			proposedPort = proposePort();
 			
 			while(true){
 				System.out.println("Sending " + proposedPort + " to Manager.");
 				outgoing.println(proposedPort);
 				
 				messageIn = incoming.readLine();
 				System.out.println("Received from Manager: \"" + messageIn + "\"");
 				
 				if(messageIn.equalsIgnoreCase("available") == true){ //If proposed port is available
 					System.out.println("Manager says port is available");
 					rs = new ReceiveSocketSW(proposedPort);
 					System.out.println("ReceiveSocket built on Requester Side");
 					//sleep(500);
 					outgoing.println(connection.getLocalAddress());//Inform the Manager that connection building is complete
 					messageIn = incoming.readLine(); //receive confirmation from Manager
 					
 					connection.setReuseAddress(true);
 					connection.close();
 					break;
 				} else {
 					System.out.println("Manager says:" + messageIn);
 					//get the next available port
 					proposedPort++;
 					while(!portAvailable(proposedPort))
 						proposedPort++;
 						if(proposedPort > BCMProtocol.MAX_PORT){
 							connection.setReuseAddress(true);
 							connection.close();
 							throw new IllegalArgumentException("No Ports Available.");
 						}
 					}
 				
 			}
		//Set interactions	
 		cw.setReceiveSocket(rs);
 		cw.setSendSocket(ss);
 		cw.enableChat();
 		} catch (Exception e) {
 			System.out.println("Error: " + e);
 		}
 	}
 	
 	/*
 	 *Checks if the port provided is available
 	 *@param port to be checked
 	 *@return true if available, false if not
 	 */
 public boolean portAvailable(int port){
 		System.out.println("portAvailable called...");
 		if(port > BCMProtocol.MAX_PORT || port < BCMProtocol.MIN_PORT)
 			throw new IllegalArgumentException("Invalid start port: " + port);
 		
 		/*System.out.println("Taken ports: " + takenPorts + " || Length = " + takenPorts.length());
 		if(takenPorts.length() > 0 ){
 			System.out.println("Now running vs takenports");
 			String checkPorts[] = takenPorts.split(";");
 			for(String str : checkPorts){
 				int toCheck = Integer.parseInt(str);
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
 	
 	public int proposePort(){
 		System.out.println("proposePort called...");
 		int port = BCMProtocol.MIN_PORT;
 		while(true){
 			if(portAvailable(port) == true){
 				System.out.print("portAvailable says " + port + " is available");
 				break;
 			}
 			if(port == BCMProtocol.MAX_PORT+1){
 				System.out.println("Ended, no ports available");
 				return 0;}
 			port++;
 		}
 		System.out.println("Ended, proposing " + port);
 		return port;
 	}
 	
 }
