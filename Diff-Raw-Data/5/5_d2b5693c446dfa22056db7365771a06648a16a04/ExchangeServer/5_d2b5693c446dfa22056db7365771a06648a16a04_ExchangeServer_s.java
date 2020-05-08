 package org.mcexchange;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ExchangeServer implements Runnable {
 	//------------------------------//
 	//    STATIC/SINGLETON STUFF    //
 	//------------------------------//
 	
 	//the one instance of ExchangeServer
 	private static final ExchangeServer es = new ExchangeServer();
 	
 	public static final int DEFAULT_PORT = 62924;
 
 	/**
 	 * Gets the singleton instance of ExchangeServer.
 	 */
 	public static ExchangeServer getInstance() {
 		return es;
 	}
 
 	//------------------------------//
 	//       NON-STATIC STUFF       //
 	//------------------------------//
 	
 	private ServerProperties sp;
 	private boolean listening = false;
 	private int port = DEFAULT_PORT;
 	private ServerSocket socket = null;
 	private ArrayList<Thread> connections = new ArrayList<Thread>();
 	
 	//private constructor ensuring that es is the only instance.
 	private ExchangeServer() {
 	}
 	
 	/**
 	 * Loads the properties file/creates it. The given command-line arguments
 	 * will be parsed for options. All properties can be specified in the c-l,
 	 * they won't persist beyond that execution.
 	 * @param override The properties to override.
 	 */
 	public void loadProperties(String[] override) {
 		sp = new ServerProperties(override);
 	}
 	
 	/**
 	 * Get the server properties object.
 	 */
 	public ServerProperties getProperties() {
 		return sp;
 	}
 	
 	/**
 	 * Obtains a reference to the client connections. It is important to note
 	 * that this list is NOT copied so any changes here will affect the whole
 	 * Server.
 	 */
 	public List<Thread> getConnections() {
 		return connections;
 	}
 	
 	/**
 	 * Binds the server to the specified port.
 	 * @param bindPort
 	 */
 	public void bind(int bindPort) {
 		try {
 			socket = new ServerSocket(bindPort);
			System.out.println("Successfully bound to port: "+port+".");
 		}
 		catch (IOException e) {
			System.err.println("Could not bind to port: "+port+".");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Makes server listen for incoming connections from clients.
 	 */
 	public void listen() {
 		listening = true;
 		System.out.println("Listening for client connections.");
 		while(listening) {
 			try {
 				Thread t = new Thread(new ClientConnection(socket.accept()));
 				t.start();
 				connections.add(t);
 				System.out.println("Succesfully connected to client.");
 			} catch (IOException e) {
 				System.err.println("Could not connect to client.");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Where all of the not-so-exciting action occurs!
 	 */
 	public void run() {
 		try {
 			port = Integer.parseInt(sp.getProperty("port"));
 		} catch(NumberFormatException e) {
 			//ignore...
 		}
 		bind(port);
 		listen();
 	}
 }
