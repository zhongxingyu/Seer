 package edu.cmu.cs.cs214.hw9.backend;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 public class ClientHandler {
 	
 	private int serverPort;
 	private String server;
 	Socket cSocket;
 	ObjectOutputStream out;
  	ObjectInputStream in;
  	String message;
 	
 	public ClientHandler(String request) {
 		
 		chooseServer();
 		message = null;
 	}
 	
 	public void register(String email, String name, String password) {
 		openConnection();
 		String requestType = "REGISTER";
 		String request = requestType + "____" + email + "____" + name + "____" + password; 
 		handleMessages(request);	
 		closeConnection();
 		
 	}
 	
 	public void updateStatus(String email, String status) {
 		openConnection();
 		String requestType = "UPDATE_STATUS";
 		String request = requestType + "____" + email + "____" + status;
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void addFriend(String requesterEmail, String requesteeEmail) {
 		openConnection();
 		String requestType = "ADD_FRIEND";
 		String request = requestType + "____" + requesterEmail + "____" + requesteeEmail + "____" + "CLIENT";
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void removeFriend(String requesterEmail, String requesteeEmail) {
 		openConnection();
 		String requestType = "REMOVE_FRIEND";
 		String request = requestType + "____" + requesterEmail + "____" + requesteeEmail + "____" + "CLIENT";
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void login(String email, String password) {
 		openConnection();
 		String requestType = "LOGIN";
 		String request = requestType + "____" + email + "____" + password;
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void getUserInfo(String email) {
 		openConnection();
 		String requestType = "GET_USER_INFO";
 		String request = requestType + "____" + email;
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void getStatuses(String email) {
 		openConnection();
 		String requestType = "GET_STATUSES";
 		String request = requestType + "____" + email;
 		handleMessages(request);
 		closeConnection();
 	}
 	
 	public void getFriendUpdates(String email) {
 		openConnection();
 		String requestType = "GET_FRIEND_UPDATES";
 		String request = requestType + "____" + email;
 		handleMessages(request);
 		closeConnection();
 	}
 
 	
 	public String[] parseMessage() {
 		String myDelimiter = "____";
 		String[] result = message.split(myDelimiter);  
 		return result;
 	}
 	
 	public void handleMessages(String request) {
 		while(message == null) {
 			
 			try {
 				message = (String)in.readObject();
 				sendMessage(request);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	//Open socket and read/write connection
 	public void openConnection() {
 		try {
 			cSocket = new Socket(server, serverPort);
 			out = new ObjectOutputStream(cSocket.getOutputStream());
 			out.flush();
 			in = new ObjectInputStream(cSocket.getInputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//Close socket
 	public void closeConnection() {
 		try{
 			in.close();
 			out.close();
 			cSocket.close();
 		}
 		catch(IOException ioException){
 			ioException.printStackTrace();
 		}	
 	}
 	
 	//Send a message to the server
 	public void sendMessage(String msg)
 	{
 		try{
 			out.writeObject(msg);
 			out.flush();
 		}
 		catch(IOException ioException){
 			ioException.printStackTrace();
 		}
 	}
 	
 	//Get a server and port from the serverList 
 	public void chooseServer() {
		int serverIndex = (int) (Math.random() * (ServerConstants.serverList.length));
		serverPort = ServerConstants.servePort + serverIndex;
		server = ServerConstants.serverList[serverIndex]+":"+serverPort;
 
 	}
 
 }
