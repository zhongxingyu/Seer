 package edu.cmu.cs.cs214.hw9.backend;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Server extends Thread {
 
 	Backend localStorage = new Database();
 	boolean serverOn = true;
 	Map<String, Integer> userTable;
 	ServerSocket sSock;
 	final int id;
 	Cache c;
 	
 	public Server(int id) {
 		this.id = id;
 		userTable = new HashMap<String, Integer>();
 		c = new Cache();
 		
 		System.out.println("Starting server on port " + ServerConstants.servePort + id);
 		try {
 			sSock = new ServerSocket(ServerConstants.servePort + id);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void run() {
 		while (serverOn) {
 			try {
 				Socket sock = sSock.accept();
 				ServeThread t = new ServeThread(sock.getInputStream(), sock.getOutputStream(), localStorage, userTable, id, c, sock);
 				t.start();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	//Main method
 	public static void main(String[] args) {
 		
 		for (int i = 0; i < ServerConstants.serverList.length; i ++) {
 			Server server = new Server(i);
 			server.start();
 		}
 	
 	}
 	

 }
