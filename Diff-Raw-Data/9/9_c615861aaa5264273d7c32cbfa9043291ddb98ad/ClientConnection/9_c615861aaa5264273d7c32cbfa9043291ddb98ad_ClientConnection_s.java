 package network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class ClientConnection {
 
 	private Socket connection;
 	private BufferedReader reader;
 	private PrintWriter writer;
 	
 	public void connect(String host, int port) {
 		try {
 			connection = new Socket(host, port);
 			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			writer = new PrintWriter(connection.getOutputStream(), true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			try {
 				connection.close();
 			} catch (Exception ex) {}
 		}
 	}
 	
 	public void close() {
 		try {
 			connection.close();
 		} catch (Exception e) {}
 		try {
 			reader.close();
 		} catch (Exception e) {}
 		try {
 			writer.close();
 		} catch (Exception e) {}
 	}
 	
 	public void send(String msg) {
 		writer.println(msg);
 	}
 	
 	public String receive() {
 		// TODO: Should perhaps have a separate thread that accepts incoming Strings and handles them instead?
 		try {
			return reader.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
