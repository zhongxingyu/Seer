 /**
  * File: PolicyRequestThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A thread to handle Policy version requests from CloudServers.
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 public class PolicyRequestThread extends Thread {
 	private PolicyServer my_ps;
     private final Socket socket; // The socket that we'll be talking over
 	private final int latency;
 	
 	/**
 	 * Constructor that sets up the thread
 	 *
 	 * @param _my_ps
 	 * @param _socket
 	 * @param _latency
 	 */
 	public PolicyRequestThread(PolicyServer _my_ps, Socket _socket, int _latency) {
 		my_ps = _my_ps;
 		socket = _socket;
 		latency = _latency;
 	}
 	
 	public void run() {
 		try {
 			// Print incoming message
 			System.out.println("** New connection from " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 			
 			// Set up I/O streams with the calling thread
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			
 			Message msg = null;
 
 			// Read and print message
 			msg = (Message)input.readObject();
 			System.out.println("[" + socket.getInetAddress() +
 							   ":" + socket.getPort() + "] " + msg.theMessage);
 			
 			if (msg.theMessage.equals("POLICYREQUEST")) {
 				// Sleep to simulate latency of response
 				try {
 					Thread.sleep(latency);
 				}
 				catch(Exception e) {
 					System.err.println("latencySleep() Error: " + e.getMessage());
 					e.printStackTrace(System.err);
 				}
 				// Return the current policy version to the requester
 				System.out.println("** Policy version request from " + socket.getInetAddress() +
 								   ":" + socket.getPort() + " **");
 				output.writeObject(new Message("" + PolicyVersion.getCurrent()));
 			}
 			else if (msg.theMessage.equals("POLICYPUSH")) {
 				// Increment the policy version and distribute to all servers
 				PolicyThread thread = null;
 				int policyVersion = PolicyVersion.getCurrent();
				if (policyVersion < Integer.MAX_VALUE) {
 					// Update policy version
 					PolicyVersion.updatePolicy();
 					System.out.println("Policy version updated to v. " + policyVersion);
 					// Spread the word, no latency
 					for (int i = 1; i <= my_ps.maxServers; i++) {
 						thread = new PolicyThread(policyVersion,
 												  my_ps.serverList.get(i).getAddress(),
 												  my_ps.serverList.get(i).getPort(),
 												  0);
 						thread.start();
 					}
 				}
 				// ACK sender of request
 				output.writeObject(new Message("ACK"));
 			}
 			else if (msg.theMessage.equals("POLICYPUSH UPDATEONLY")) {
 				// Increment policy version, but do not distribute to servers
 				PolicyThread thread = null;
 				int policyVersion = PolicyVersion.getCurrent();
 				if (policyVersion < Integer.MAX_VALUE) {
 					// Update policy version
 					PolicyVersion.updatePolicy();
 					System.out.println("Policy version updated to v. " + policyVersion);
 				}
 				// ACK sender of request
 				output.writeObject(new Message("ACK"));
 			}
 			else if (msg.theMessage.indexOf("POLICYPUSH") != -1) {
 				// We'll be sending an update to an individual server
 				PolicyThread thread = null;
 				String msgSplit[] = msg.theMessage.split(" ");
 				// Get destination server
 				int dest = Integer.parseInt(msgSplit[1]);
 				int policyVersion = PolicyVersion.getCurrent();
 				if (policyVersion < Integer.MAX_VALUE) {
 					// Update policy version
 					PolicyVersion.updatePolicy();
 					// Send to destination, no latency
 					thread = new PolicyThread(policyVersion,
 											  my_ps.serverList.get(dest).getAddress(),
 											  my_ps.serverList.get(dest).getPort(),
 											  0);
 					thread.start();
 				}
 				// ACK sender of request
 				output.writeObject(new Message("ACK"));
 			}
 			else {
 				output.writeObject(new Message("FAIL"));
 			}
 			
 			// Close the connection
 			socket.close();
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 }
