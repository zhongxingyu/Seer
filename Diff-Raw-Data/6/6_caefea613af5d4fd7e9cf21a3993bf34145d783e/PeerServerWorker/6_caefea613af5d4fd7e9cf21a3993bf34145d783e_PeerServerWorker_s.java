 package com.mycompany.reservationsystem.peer.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import com.mycompany.reservationsystem.peer.communication.COMMUNICATION_MESSAGES;
 import com.mycompany.reservationsystem.peer.data.Peer;
 import com.mycompany.reservationsystem.peer.data.PeerTable;
 
 /*
  * Server thread giving known ip address to clients
 
     Begin
     Wait for client to connect
     Client requests for an ip
     Find all known ip addresses at given point in time
     Send an ip address to client
     Wait for client to ask for another
     Repeat step 5 and 6 until given all ip addresses
     When all ips are given send blank message
     Client then disconnects
     End
  */
 public class PeerServerWorker extends Thread{
 	private Socket connection;
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	private boolean isFinished;
 	private ArrayList<Peer> peerList;
 	private int nextIPIndex = 0;
 	
 	public PeerServerWorker(Socket socket){
 		this.connection = socket;
 		this.isFinished = false;
 		peerList = new ArrayList<Peer>();
 	}
 
 	public void run(){
 		PeerTable peerTable = PeerTable.getInstance();
 		peerList = peerTable.getAllPeers();
 		try{
 			out = new ObjectOutputStream(connection.getOutputStream());
 			out.flush();
 			in = new ObjectInputStream(connection.getInputStream());
 			
 			String message = null;
			message = (String)in.readObject();
 			
 			while(isFinished() == false){
 				if(nextIPIndex+1 >= peerList.size()){
 					setFinished(true);
 				}
 				
 				if(message.equals(COMMUNICATION_MESSAGES.IP_REQUEST.toString()) && isFinished() == false){
 					sendMessage(COMMUNICATION_MESSAGES.IP_RESPONSE);
 				}
 				nextIPIndex++;
 			}
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		finally{
 			//Closing connection
 			try{
 				in.close();
 				out.close();
 				connection.close();
 			}
 			catch(IOException ioException){
 				ioException.printStackTrace();
 			}
 		}
 	}
 	
 	//Send message to client
 	private void sendMessage(COMMUNICATION_MESSAGES communicationMessage){
 		if(communicationMessage.equals(COMMUNICATION_MESSAGES.IP_RESPONSE)){
 			String message = "";
 			message += COMMUNICATION_MESSAGES.IP_RESPONSE.toString() + ":" + peerList.get(nextIPIndex);
 			
 			try{
 				out.writeObject(message);
 				out.flush();
 			}
 			catch(IOException ioException){
 				ioException.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isFinished() {
 		return isFinished;
 	}
 
 	public void setFinished(boolean isFinished) {
 		this.isFinished = isFinished;
 	}
 }
