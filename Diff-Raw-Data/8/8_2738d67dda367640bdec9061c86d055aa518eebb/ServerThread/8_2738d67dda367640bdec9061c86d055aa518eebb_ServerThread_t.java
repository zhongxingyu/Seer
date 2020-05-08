 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 
 public abstract class ServerThread extends Thread{
 
 	private Socket socket;
 	private ObjectInputStream inputStream;
 	private ObjectOutputStream outputStream;
 	private Server parentServer;
 	
 	public ServerThread(Server psrv, Socket skt){
 		this.socket = skt;
 		parentServer = psrv;
         try
         {
             // create output first
         	outputStream = new ObjectOutputStream(socket.getOutputStream()); //needs this or it wont work
             inputStream  = new ObjectInputStream(socket.getInputStream());
         }
         catch (IOException e) {
             System.out.println("Error Creating Streams: " + e);
             return;
 
         }
 		
 	}
 	
 	public void run()
 	{
 		//check for message client read/append
 		// if read, return values
 		// if append start 2pc protocol then paxos protocol
 		
 		ServerMessage msg;
 		
 		try {
             msg = (ServerMessage) inputStream.readObject();
             System.out.println("RECIEVED:" + msg);
             
             switch (msg.getType()) {
 		        case ServerMessage.CLIENT_READ:
 		        	//read the file
 		        case ServerMessage.CLIENT_APPEND:
 		        	//create a new ballot by incrementing current ballot by 1
 		        	if (!parentServer.isPaxosLeader()){
 		        		parentServer.addPaxosLeaders(socket.getLocalAddress().getHostAddress());
 		        		parentServer.setPaxosLeader(true);
 		        		for (int i = 0; i < Server.StatServers.size(); i++){
 		        			ServerMessage leaderMsg = new ServerMessage(ServerMessage.PAXOS_ADD_LEADER, socket.getLocalAddress().getHostAddress(), socket.getLocalAddress().getHostAddress() );
 			        		sendMessage(Server.StatServers.get(i), 3000, leaderMsg);
 			        	}
 		        		//send
 		        	}
 		        			        	
 		        	parentServer.setCurrentBallotNumber(parentServer.getCurrentBallotNumber()+1);
 		        	ServerMessage ballot = new ServerMessage(ServerMessage.PAXOS_PREPARE, parentServer.getCurrentBallotNumber() + "," + parentServer.getProcessId(), socket.getLocalAddress().getHostAddress() );
 		        	
 System.out.println("My address:" + socket.getLocalAddress().getHostAddress() );
 	        	
 		        	//send to all other stat or grade servers
 
 		        	for (int i = 0; i < Server.StatServers.size(); i++){
 		        		sendMessage(Server.StatServers.get(i), 3000, ballot);
 		        	}
 		        	break;
 		   
 		        case ServerMessage.PAXOS_PREPARE:
 		 
 		        	//contents of the incoming prepare message are ballotnum,processesid.
 		        	int proposedBallot = Integer.parseInt(msg.getMessage().split(",")[0]);
 		        	int proposedprocessID = Integer.parseInt(msg.getMessage().split(",")[1]); //for tie breakers
 		        	//if the incoming ballot is newer than my ballot, update my ballot and send an ack, otherwise the incoming
 		        	//ballot is old and we can ignore it
 		        	//or if the ballots are the same, process id will be the tie breaker. 
 		        	//if the prepare is sent to myself
 		        	if (proposedBallot > parentServer.getCurrentBallotNumber() || (proposedBallot == parentServer.getCurrentBallotNumber() && proposedprocessID >= parentServer.getProcessId()) ){
 		        		parentServer.setCurrentBallotNumber(proposedBallot);
 		        		//send the ack message with the current ballot, the last accepted ballot, the current value.
		        		ServerMessage ackMessage = new ServerMessage(ServerMessage.PAXOS_ACK, parentServer.getCurrentBallotNumber() + ","+ parentServer.getCurrentAcceptNum() + "," + parentServer.getAcceptValue(), socket.getLocalAddress().getHostName() );
 		        		sendMessage(socket.getInetAddress().getHostName(), 3000, ackMessage);
 		        	
 		        	}
 		        	break;
 		        	
 		        case ServerMessage.PAXOS_ACK:
 		        	//contents of the incoming ack message are current ballot, the last accepted ballot, the current value
 		        	proposedBallot = Integer.parseInt(msg.getMessage().split(",")[0]);
 		        	Hashtable<Integer,ArrayList<ServerMessage> > hash = parentServer.getMessageHash();
 		        	ArrayList<ServerMessage> ballot_msgs = hash.get(proposedBallot);
 		            //add the incoming message to a collection of responses for this ballot
 		        	if (ballot_msgs == null){
 		        		ballot_msgs = new ArrayList<ServerMessage>();
 		        	}
 		            ballot_msgs.add(msg);
 		        	hash.put(proposedBallot, ballot_msgs);
 		        	parentServer.setMessageHash(hash);
 
 		        	//check to see if we have gotten a majority of responses... if not, do nothing
 		        	if(ballot_msgs.size() > Server.StatServers.size()/2)
 		        	{
 
         				ServerMessage acceptMsg = new ServerMessage(ServerMessage.PAXOS_ACCEPT, parentServer.getCurrentBallotNumber() +","+ parentServer.getAcceptValue() ,socket.getLocalAddress().getHostAddress() );
         				sendMessage(Server.stat2PCLeader, 3000, acceptMsg);
 		        	
 		        	}
 		        	
 		        	break;
 		        	
 		        case ServerMessage.PAXOS_ACCEPT:
 		        	
 		        	parentServer.setPaxosLeaderResponseCount(parentServer.getPaxosLeaderResponseCount() + 1);
 		        	
 		        	if (parentServer.getPaxosLeaderResponseCount() == parentServer.getPaxosLeaders().size()){
 		        		//reset response count for the next query
 		        		parentServer.setPaxosLeaderResponseCount(0);
 		        		String acceptVal = msg.getMessage().split(",")[1]; //for tie breakers
 		        		for (int i = 0; i < Server.StatServers.size(); i++){
 		        			ServerMessage vote2pc = new ServerMessage(ServerMessage.TWOPHASE_VOTE_REQUEST, acceptVal);
 			        		sendMessage(Server.StatServers.get(i), 3000, vote2pc);
 		        		}
 		        	}
 		        	
 		        
 		        	
 		        	
 		        	 break;
 		        
 		        case ServerMessage.PAXOS_ADD_LEADER:
 		        	parentServer.addPaxosLeaders(msg.getMessage());
 		         	
 		        	
 		        case ServerMessage.TWOPHASE_VOTE_REQUEST:
 		        	//reply yes or no
 		        case ServerMessage.TWOPHASE_VOTE_YES:
 		        	//tally yes vote
 		        case ServerMessage.TWOPHASE_VOTE_NO:
 		        	//send abort
 		        case ServerMessage.TWOPHASE_ABORT:
 		        	//cancel the write changes
 		        case ServerMessage.TWOPHASE_COMMIT:
 		        	//write any changes
 	        
 	        }
         }
         catch (IOException e) {
             System.out.println(" Exception reading Streams: " + e);
             System.exit(1);             			
         }		
         catch(ClassNotFoundException ex) {
         	//this shouldnt be a problem, only ServerMessages should be sent.
 			System.exit(1);
         }
 		
        
 		
 	}
 	
 	private void sendMessage(String host, int port, ServerMessage msg){
 		
 		System.out.println("SENDING " + msg + " to Server:" + host + "...");
 		
 		 try {
 		      InetAddress address = InetAddress.getByName(host);
		      System.out.print("      Connecting to Server:"+host+"...");
 		      
 		      // open socket, then input and output streams to it
 		      Socket socket = new Socket(address,port);
 		      ObjectOutputStream to_server = new ObjectOutputStream(socket.getOutputStream());
 		      System.out.print("Connected");
 		      
 		      // send command to server, then read and print lines until
 		      // the server closes the connection
 		      
 		      to_server.writeObject(msg); to_server.flush();
 		      System.out.println("....SENT");
 		 } catch (IOException e){
			 System.out.println("      ERROR: Server failed sending message:" + e.getMessage());
 		 }
 	}
 	
 	
 }
