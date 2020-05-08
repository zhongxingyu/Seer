 /**
  * File: WorkerThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * Based on original code "EchoThread.java" by Adam J. Lee (adamlee@cs.pitt.edu) 
  *
  * A simple server thread. This class just echoes the messages sent
  * over the socket until the socket is closed.
  */
 
 import java.lang.Thread;            // We will extend Java's base Thread class
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.ObjectInputStream;   // For reading Java objects off of the wire
 import java.io.ObjectOutputStream;  // For writing Java objects to the wire
 import java.util.ArrayList;
 
 public class WorkerThread extends Thread {
     private final Socket socket; // The socket that we'll be talking over
     private final int serverNumber; // The number of this server
 	private final ArrayList<ServerID> serverList;
 	private final int readSleep = 5; // number of milliseconds for a READ
 	private final int writeSleep = 5; // number of milliseconds for a WRITE
 
 	/**
 	 * Constructor that sets up the socket we'll chat over
 	 *
 	 * @param _socket - The socket passed in from the server
 	 * @param _socket - The number assigned to the server that created thread
 	 */
 	public WorkerThread(Socket _socket, int _serverNumber, ArrayList<ServerID> _serverList) {
 		socket = _socket;
 		serverNumber = _serverNumber;
 		serverList = _serverList;
 	}
 
 	/**
 	 * run() is basically the main method of a thread. This thread
 	 * simply reads Message objects off of the socket.
 	 */
 	public void run() {
 		try {
 			// Print incoming message
 			System.out.println("** New connection from " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 
 			// set up I/O streams with the RobotThread
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 
 			// Loop to read messages
 			Message msg = null;
 			// read and print message
 			msg = (Message)input.readObject();
 			System.out.println("[" + socket.getInetAddress() +
 								   ":" + socket.getPort() + "] " + msg.theMessage);
 
 				// Sample messages
 				// B 1
 				// R 1 1,R 1 3,R 1 1,R 1 2
 				// W 1 2,W 1 1
 				// R 1 3
 				// C 1
 				// exit
 				
 				// Break up query grouping
 			String queryGroup[] = msg.theMessage.split(",");
 				// Get first query
 				
 				// Write an ACK back to the sender
 //				if (query.length != 1) { // if not "exit"
 //					output.writeObject(new Message("Received message #" + count +
 //												   " of transaction " + query[1]));
 //				}
 //				else {
 //					output.writeObject(new Message("Received message #" + count));
 //				}
 				
 				// Break up query grouping and loop to process each query
 //			int groupIndex = 0;
 			
 			for (int i = 0; i < queryGroup.length; i++) {
 				// Handle instructions
 				String query[] = queryGroup[i].split(" ");
 				if (query[0].equals("B")) { // BEGIN
 					System.out.println("BEGIN transaction " + query[1]);
 				}
 				else if (query[0].equals("R")) { // READ
 					// Check server number, perform query or pass on
 					if (Integer.parseInt(query[2]) == serverNumber) { // Perform query on this server
 						if (accessData() == false) {
 							// message an error, abort transaction
 						}
 						else {
 							// add time to counter or sleep
 							System.out.println("READ for transaction " + query[1]);
 							Thread.sleep(readSleep);
 						}
 					}
 					else { // pass to server
 						System.out.println("Pass READ of transaction " + query[1] +
 										   " to server " + query[2]);
						passQuery(Integer.parseInt(query[2]), msg.theMessage);
 					}
 				}
 				else if (query[0].equals("W")) { // WRITE
 					// Check server number, perform query or pass on
 					if (Integer.parseInt(query[2]) == serverNumber) { // Perform query on this server
 						if (accessData() == false) {
 							// message an error, abort transaction
 						}
 						else {
 							// add time to counter or sleep
 							System.out.println("WRITE for transaction " + query[1]);
 							Thread.sleep(writeSleep);
 						}
 					}
 					else { // pass to server
 						System.out.println("Pass WRITE of transaction " + query[1] +
 										   " to server " + query[2]);
						passQuery(Integer.parseInt(query[2]), msg.theMessage);
 					}
 				}
 				else if (query[0].equals("C")) { // COMMIT
 					// will need to keep list of all servers accessed, use it
 					// to finalize commit across servers
 					System.out.println("COMMIT STUB - transaction " + query[1]);
 				}
 			}
 			// ACK completion
 			output.writeObject(new Message("ACK"));
 			
 			// Close and cleanup
 			System.out.println("** Closing connection with " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 			socket.close();
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * Passes a query to other specified server
 	 *
 	 * @param otherServer - The number of the server to pass to
 	 * @param query - The query that must be performed on another server
 	 *
 	 * @return boolean - true if query was successful, else false
 	 */
 	public boolean passQuery(int otherServer, String query) {
 		// need to figure out how to store server addresses/ports
 		// for now, use localhost and a given port
 		String server = serverList.get(otherServer).getAddress();
 		int port = serverList.get(otherServer).getPort();
 		
 		// here we do the same thing we'd do from RobotThread: set up a
 		// socket with server X, pass it just a single query, and get back
 		// the result
 		try {
 			// Connect to the specified server
 			final Socket sock = new Socket(server, port);
 			System.out.println("Connected to " + server +
 							   " on port " + port);
 			
 			// Set up I/O streams with the server
 			final ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
 			final ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
 			Message msg = null, resp = null;
 			
 			// send query
 			msg = new Message(query);
 			output.writeObject(msg);
 			resp = (Message)input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + resp.theMessage);
 			// handle response
 			
 			// send EXIT
 			msg = new Message("EXIT");
 			output.writeObject(msg);
 			resp = (Message)input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + resp.theMessage);
 			
 			// shut things down
 			sock.close();
 			
 			return true;
 		}
 		catch (ConnectException ce) {
 			System.err.println(ce.getMessage() +
 							   ": Check server address and port number.");
 			ce.printStackTrace(System.err);
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}		
 		return false;
 	}
 	
 	/**
 	 * Checks the integrity of the data for the commit
 	 *
 	 * @return boolean - true if integrity check comes back OK, else false
 	 */
 	public boolean verifyIntegrity() {
 		System.out.println("verifyIntegrity() stub");
 		// perform random success operation
 		return true;
 	}
 
 	/**
 	 * Checks if accessing requested data was successful
 	 *
 	 * @return boolean - true if access was successful, else false
 	 */
 	public boolean accessData() {
 		System.out.println("accessData() stub");
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks the local policy for authorization to data
 	 *
 	 * @return boolean - true if authorization check comes back OK, else false
 	 */
 	public boolean getLocalAuth() {
 		System.out.println("getLocalAuth() stub");
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Checks the global policy for authorization to data
 	 *
 	 * @return boolean - true if authorization check comes back OK, else false
 	 */
 	public boolean getPolicyAuth() {
 		System.out.println("getPolicyAuth() stub");
 		// perform random success operation
 		return true;
 	}
 	
 	/**
 	 * Calls the Certificate Authority for the freshest policy
 	 *
 	 * @return boolean - true if freshest policy is received/good, else false
 	 */
 	public boolean checkPolicyWithCA() {
 		System.out.println("checkPolicyWithCA() stub");
 		// call the CA server, get results of call back
 		return true;
 	}
 }
