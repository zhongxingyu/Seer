 package client;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import poker.GUI.Login;
 
 
 /**
  * Client Class used as a main entry point for the program and for
  * initializing parameters.
  * 
  * @author Aleksey
  */
 
 public class Client {
 	
 	/**
 	 * Main entry point in the client side program
 	 * 
 	 * @param args (is ignored)
 	 */
     private static Login login;
 
 	public static void main(String args[]) {
        login = new Login();
 	}
 	
 	public static void start(String name) {
 		
 		try {
 			Socket socket = new Socket(InetAddress.getLocalHost(), 9999);
			(new PrintWriter(socket.getOutputStream())).println(name);
 						
 			TaskQueue que = new TaskQueue();
 			Conn conn = new Conn(socket, socket.getOutputStream());
 			
 			ServerListener listener = new ServerListener(
 					socket.getInputStream() , que);
 			ClientGame game = new ClientGame(conn, que);	
 			
 			Thread listenerThread = new Thread(listener);
 			Thread gameThread = new Thread(game);
 			
 			listenerThread.start();
 			gameThread.start();
 						
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		
 	}
 }
 			
 
 			
 			
 
 
