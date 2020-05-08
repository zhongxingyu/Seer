 /**
  * File: PunctualThread.java
  * @author: Tucker Trainor <tmt33@pitt.edu>
  *
  * A server thread to handle punctual proofs of authorization. Extends the
  * DeferredThread base class.
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.ConnectException;
 import java.io.*;
 import java.util.*;
 
 public class PunctualThread extends DeferredThread {
 	public int randomServer = 0;
 	
 	/**
 	 * Constructor that sets up the socket we'll chat over
 	 *
 	 * @param _socket - The socket passed in from the server
 	 * @param _my_tm - The Transaction Manager that called the thread
 	 */
 	public PunctualThread(Socket _socket, CloudServer _my_tm) {
 		super(_socket, _my_tm);
 	}
 	
 	/**
 	 * run() is basically the main method of a thread. This thread
 	 * simply reads Message objects off of the socket.
 	 */
 	public void run() {
 		generator = new Random(new Date().getTime());
 		
 		PrintStream printStreamOriginal = System.out;
 		if (!my_tm.verbose) {
 			System.setOut(new PrintStream(new OutputStream() {
 				public void close() {}
 				public void flush() {}
 				public void write(byte[] b) {}
 				public void write(byte[] b, int off, int len) {}
 				public void write(int b) {}
 			}));
 		}
 		
 		try {
 			// Print incoming message
 			System.out.println("** New connection from " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 			
 			// Set up I/O streams with the calling thread
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			
 			Message msg = null;
 			Message resp = null;
 			
 			while (true) {
 				// Loop to read messages
 				String msgText = "ACK";
 				// Read and print message
 				msg = (Message)input.readObject();
 				System.out.println("[" + socket.getInetAddress() +
 								   ":" + socket.getPort() + "] " + msg.theMessage);
 				
 				if (msg.theMessage.equals("DONE")) {
 					break;
 				}
 				else if (msg.theMessage.equals("KILL")) {
 					my_tm.shutdownServer();
 					break;
 				}
 				else if (msg.theMessage.indexOf("POLICYUPDATE") != -1) { // Policy update from Policy Server
 					String msgSplit[] = msg.theMessage.split(" ");
 					int update = Integer.parseInt(msgSplit[1]);
 					// Check that we aren't going backwards in a race condition
 					if (my_tm.getPolicy() < update) {
 						my_tm.setPolicy(update);
 					}
 					latencySleep(); // Simulate latency
 					output.writeObject(new Message(msgText)); // send ACK
 					break;
 				}
 				else if (msg.theMessage.indexOf("PARAMETERS") != -1) { // Configuration change
 					// PARAMETERS <PROOF> <VM> <PUSH>
 					String msgSplit[] = msg.theMessage.split(" ");
 					my_tm.proof = msgSplit[1];
 					my_tm.validationMode = Integer.parseInt(msgSplit[2]);
 					my_tm.policyPush = Integer.parseInt(msgSplit[3]);
 					System.out.println("Server parameters updated: " + msg.theMessage);
 					System.out.println("Proof: " + my_tm.proof);
 					System.out.println("Validation mode: " + my_tm.validationMode);
 					System.out.println("Policy push mode: " + my_tm.policyPush);
 					// No artificial latency needed, send ACK
 					output.writeObject(new Message(msgText));
 					break;
 				}
 				
 				// Separate queries
 				String queryGroup[] = msg.theMessage.split(",");
 				for (int i = 0; i < queryGroup.length; i++) {
 					// Handle instructions
 					String query[] = queryGroup[i].split(" ");
 					if (query[0].equals("R")) { // READ
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed
 							// (e.g. if this query has been passed in) it is set
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.getPolicy();
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "READ for txn " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to read
 								System.out.println("READ for txn " + query[1] +
 												   " sequence " + query[3]);
 								databaseRead();
 								// Add policy version for passed query logging
 								msgText += " " + transactionPolicyVersion;
 								// Add to query log
 								if (addToQueryLog(query, transactionPolicyVersion)) {
 									System.out.println("Transaction " + query[1] +
 													   " sequence " + query[3] +
 													   " query logged.");
 								}
 								else {
 									System.out.println("Error logging query.");
 								}
 							}
 						}
 						else { // Pass to server
 							System.out.println("Pass READ of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2]);
 							msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 							System.out.println("Response to READ of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2] +
 											   ": " + msgText);
 						}
 					}
 					else if (query[0].equals("W")) { // WRITE
 						// Check server number, perform query or pass on
 						if (Integer.parseInt(query[2]) == my_tm.serverNumber) { // Perform query on this server
 							// Check that if a fresh Policy version is needed, it is gotten
 							if (transactionPolicyVersion == 0) {
 								transactionPolicyVersion = my_tm.getPolicy();
 								System.out.println("Transaction " + query[1] +
 												   " Policy version set: " +
 												   transactionPolicyVersion);
 							}
 							
 							// Check transaction policy against server policy
 							if (checkLocalAuth() == false) {
 								msgText = "ABORT LOCAL_POLICY_FAIL";
 								System.out.println("ABORT LOCAL_POLICY_FAIL: " +
 												   "WRITE for txn " + query[1] +
 												   " sequence " + query[3]);
 							}
 							else { // OK to write
 								System.out.println("WRITE for txn " + query[1] +
 												   " sequence " + query[3]);
 								databaseWrite();
 								// Add policy version for passed query logging
 								msgText += " " + transactionPolicyVersion;
 								// Add to query log
 								if (addToQueryLog(query, transactionPolicyVersion)) {
 									System.out.println("Transaction " + query[1] +
 													   " sequence " + query[3] +
 													   " query logged.");
 								}
 								else {
 									System.out.println("Error logging query.");
 								}
 							}
 						}
 						else { // Pass to server
 							System.out.println("Pass WRITE of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2]);
 							msgText = passQuery(Integer.parseInt(query[2]), queryGroup[i]);
 							System.out.println("Response to WRITE of transaction " + query[1] +
 											   " sequence " + query[3] +
 											   " to server " + query[2] +
 											   ": " + msgText);
 						}
 					}
 					else if (query[0].equals("RUNAUTHS")) {
 						// Run any necessary re-authorizations on queries
 						int version = Integer.parseInt(query[1]);
 						System.out.println("Running auth. on transaction " +
 										   queryLog.get(0).getTransaction() + 
 										   " queries using policy version " +
 										   version);
 						msgText = "TRUE";
 						for (int j = 0; j < queryLog.size(); j++) {
 							// If policy used for proof during transaction differs
 							if (queryLog.get(j).getPolicy() != version) {
 								if (!checkLocalAuth()) {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + version +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): FAIL");
 									msgText = "FALSE";
 									break;
 								}
 								else {
 									System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 													   " for txn " + queryLog.get(j).getTransaction() +
 													   ", seq " + queryLog.get(j).getSequence() +
 													   " with policy v. " + version +
 													   " (was v. " + queryLog.get(j).getPolicy() +
 													   "): PASS");
 									queryLog.get(j).setPolicy(version); // Update policy in log
 								}
 							}
 							else { // Output message of same policy
 								System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 												   " for txn " + queryLog.get(j).getTransaction() +
 												   ", seq " + queryLog.get(j).getSequence() +
 												   " with policy v. " + version +
 												   ": ALREADY DONE");
 							}
 						}
 					}
 					else if (query[0].equals("PTC")) { // Prepare-to-Commit
 						if (my_tm.validationMode >= 0 && my_tm.validationMode <= 2) {
 							msgText = prepareToCommit(0); // No global version
 						}
 						else { // Uses a global version, pass to method
 							msgText = prepareToCommit(Integer.parseInt(query[1]));
 						}
 					}
 					else if (query[0].equals("C")) { // COMMIT
 						System.out.println("COMMIT phase - transaction " + query[1]);
 						// Begin 2PC/2PV methods
 						msgText = coordinatorCommit();
 						System.out.println("Status of 2PC/2PV of transaction " + query[1] +
 										   ": " + msgText);
 					}
 					else if (query[0].equals("RSERV")) { // Random server for policy pushing
 						randomServer = Integer.parseInt(query[1]);
 					}
 					else if (query[0].equals("S")) { // Sleep for debugging
 						Thread.sleep(Integer.parseInt(query[1]));
 					}
 					else if (query[0].toUpperCase().equals("EXIT")) { // end of transaction
 						// send exit flag to RobotThread
 						msgText = "FIN";
 						if (!my_tm.threadSleep) { // append total sleep time to message
 							msgText += " " + totalSleepTime;
 						}
 					}
 				}
 				latencySleep(); // Simulate latency to RobotThread
 				// ACK completion of this query group to RobotThread
 				output.writeObject(new Message(msgText));
 			}
 			// Close any SocketGroup connection
 			if (sockList.size() > 0) {
 				int serverNum;
 				for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 					msg = new Message("DONE");
 					serverNum = socketList.nextElement();
 					latencySleep(); // Simulate latency
 					sockList.get(serverNum).output.writeObject(msg);
 					sockList.get(serverNum).socket.close();
 				}
 			}
 			
 			// Close and cleanup
 			System.out.println("** Closing connection with " + socket.getInetAddress() +
 							   ":" + socket.getPort() + " **");
 			socket.close();
 		}
 		catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		System.out.flush();
 		System.setOut(printStreamOriginal);
 	}
 
 	/**
 	 * Passes a query to other specified server
 	 *
 	 * @param otherServer - The number of the server to pass to
 	 * @param query - The query that must be performed on another server
 	 *
 	 * @return String - the ACK/ABORT from the other server
 	 */
 	public String passQuery(int otherServer, String query) {
 		String server = my_tm.serverList.get(otherServer).getAddress();
 		int port = my_tm.serverList.get(otherServer).getPort();
 		Message msg = null;
 		try {
 			// Check SocketList for an existing socket, else create and add new
 			if (!sockList.hasSocket(otherServer)) {
 				// Create new socket, add it to SocketGroup
 				System.out.println("Connecting to " + server +
 								   " on port " + port);
 				Socket sock = new Socket(server, port);
 				sockList.addSocketObj(otherServer, new SocketObject(sock,
 																	new ObjectOutputStream(sock.getOutputStream()),	
 																	new ObjectInputStream(sock.getInputStream())));
 				// Push policy updates as necessary
 				if (!hasUpdated) {
 					if (my_tm.validationMode == 1 || my_tm.validationMode == 3) {
 						// We can update at the addition of any participant
 						forcePolicyUpdate(my_tm.policyPush);
 						hasUpdated = true; // This only needs to be done once
 					}
 					else if (my_tm.validationMode == 2 || my_tm.validationMode == 4) {
 						// We want to randomize when the policy update is pushed
 						if (otherServer == randomServer) { // Do if random server is picked
 							forcePolicyUpdate(my_tm.policyPush);
 							hasUpdated = true; // This only needs to be done once	
 						}
 					}
 				}
 			}
 			
 			// Send query
 			msg = new Message(query);
 			latencySleep(); // Simulate latency to other server
 			sockList.get(otherServer).output.writeObject(msg);
 			msg = (Message)sockList.get(otherServer).input.readObject();
 			System.out.println("Server " + otherServer +
 							   " says: " + msg.theMessage +
 							   " for passed query " + query);
 			// else it is an ABORT, no need to log, will be handled by RobotThread
 			return msg.theMessage;
 		}
 		catch (ConnectException ce) {
 			System.err.println(ce.getMessage() +
 							   ": Check server address and port number.");
 			ce.printStackTrace(System.err);
 		}
 		catch (Exception e) {
 			System.err.println("Error during passQuery(): " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		return "FAIL";
 	}
 
 	/**
 	 * The prepare-to-commit method that is invoked when participating servers
 	 * received the PTC call from the coordinator
 	 *
 	 * @param globalVersion - used for global consistency check
 	 * @return boolean
 	 */
 	public String prepareToCommit(int globalVersion) {
 		// Receive PTC message, handle options
 		if (my_tm.validationMode == 0) { // 2PC only
 			// Return integrity status
 			if (integrityCheck()) {
 				return "YES";
 			}
 			else {
 				return "NO";
 			}
 		}
 		else if (my_tm.validationMode == 1 || my_tm.validationMode == 2) {
 			// 1. Rec'v PTC, request for policy version
 			//    Return integrity status (YES/NO), Policy version
 			if (integrityCheck()) {
 				return "YES " + transactionPolicyVersion;
 			}
 			else {
 				return "NO";
 			}
 		}
 		else if (my_tm.validationMode == 3) {
 			// Check global master policy version against transaction version
 			if (globalVersion == transactionPolicyVersion) {
 				// Perform integrity check
 				if (integrityCheck()) {
 					// Run local authorizations
 					System.out.println("Running auth. on transaction " +
 									   queryLog.get(0).getTransaction() + 
 									   " queries using policy version " +
 									   transactionPolicyVersion);
 					for (int j = 0; j < queryLog.size(); j++) {
 						if (!checkLocalAuth()) {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for txn " + queryLog.get(j).getTransaction() +
 											   ", seq " + queryLog.get(j).getSequence() +
 											   " with policy v. " + transactionPolicyVersion +
 											   " (was v. " + queryLog.get(j).getPolicy() +
 											   "): FAIL");
 							return "YES FALSE"; // (authorization failed)
 						}
 						else {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for txn " + queryLog.get(j).getTransaction() +
 											   ", seq " + queryLog.get(j).getSequence() +
 											   " with policy v. " + transactionPolicyVersion +
 											   " (was v. " + queryLog.get(j).getPolicy() +
 											   "): PASS");
 						}
 					}
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}
 			else {
 				return "YES FALSE"; // (policy inequality)
 			}
 		}
 		else { // (my_tm.validationMode == 4)
 			// Check global master policy version against transaction version
 			if (globalVersion != transactionPolicyVersion) {
 				// Have server get global version from the policy server
 				int calledGlobal = my_tm.callPolicyServer();
 				// Check version for possible race condition
 				if (calledGlobal > globalVersion) {
 					calledGlobal = globalVersion;
 				}
 				// Perform integrity check
 				if (integrityCheck()) {
 					// Run local authorizations
 					System.out.println("Running auth. on transaction " +
 									   queryLog.get(0).getTransaction() + 
 									   " queries using policy version " +
 									   calledGlobal);
 					for (int j = 0; j < queryLog.size(); j++) {
 						if (!checkLocalAuth()) {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for txn " + queryLog.get(j).getTransaction() +
 											   ", seq " + queryLog.get(j).getSequence() +
 											   " with policy v. " + calledGlobal +
 											   " (was v. " + queryLog.get(j).getPolicy() +
 											   "): FAIL");
 							return "YES FALSE"; // (authorization failed)
 						}
 						else {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for txn " + queryLog.get(j).getTransaction() +
 											   ", seq " + queryLog.get(j).getSequence() +
 											   " with policy v. " + calledGlobal +
 											   " (was v. " + queryLog.get(j).getPolicy() +
 											   "): PASS");
 						}
 					}
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}			
 			else { // (globalVersion == transactionPolicyVersion) 
 				for (int j = 0; j < queryLog.size(); j++) {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + globalVersion +
 									   ": ALREADY DONE");
 				}
 				// Perform integrity check
 				if (integrityCheck()) {
 					return "YES TRUE"; // (integrity and authorizations pass)
 				}
 				else {
 					return "NO FALSE"; // (integrity fail)
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Handles the 2PV view consistency check. Calls each participant with the
 	 * PTC command, receives back their policy versions, and determines whether
 	 * or not to run proofs of authorization.
 	 *
 	 * @return String - COMMIT or ABORT under view consistency
 	 */
 	public String viewConsistencyCheck() {
 		String status = "COMMIT";
 		Message msg = null;
 		ArrayList<Integer> versions = new ArrayList<Integer>();
 		
 		// Check coordinator's integrity
 		if (!integrityCheck()) {
 			return "ABORT PTC_RESPONSE_NO";
 		}
 		
 		// Add coordinator's policy version to ArrayList
 		versions.add(transactionPolicyVersion);
 		// Call all participants, send PTC and gather policy versions
 		if (sockList.size() > 0) {
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			boolean integrityOkay = true;
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Send messages to all participants
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC");
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum[i]).output.writeObject(msg);
 					}
 					catch (Exception e) {
						System.err.println("PTC Call Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			
 			// Check coordinator's integrity
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			
 			// Receive responses
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't listen for the Policy server
 					try {
 						msg = (Message)sockList.get(serverNum[i]).input.readObject();
 						// Check response, add policy version to ArrayList
 						if (msg.theMessage.indexOf("YES") != -1) {
 							if (my_tm.validationMode != 0) { // Not 2PC only
 								String msgSplit[] = msg.theMessage.split(" ");
 								versions.add(Integer.parseInt(msgSplit[1]));
 							}
 						}
 						else { // ABORT - someone responded with a NO
 							integrityOkay = false;
 						}
 					}
 					catch (Exception e) {
						System.err.println("PTC Call Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			// Check for any reported integrity failures
 			if (!integrityOkay) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 		}
 		else { // No other servers - check only coordinator for integrity
 			if (!integrityCheck()) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 		}
 		
 		// If 2PC only, no need to check policies or run auths
 		if (my_tm.validationMode == 0) {
 			return status;
 		}
 		
 		// Turn ArrayList into an array of ints, sort and compare versions
 		Integer versionArray[] = new Integer[versions.size()];
 		versionArray = versions.toArray(versionArray);
 		// Sort array, compare first value with last
 		Arrays.sort(versionArray);
 		if (versionArray[0] == versionArray[versionArray.length - 1]) {
 			// Policy versions match across servers - no further action needed			
 			return status;
 		}
 		else { // Handle inequality
 			if (my_tm.validationMode == 1) { // ABORT
 				status = "ABORT VIEW_CONSISTENCY_FAIL";
 			}
 			else { // Find common policy and run authorizations with it
 				// For simplicity, use minimum of versions as common policy
 				status = runAuths((int)versionArray[0]);
 			}
 		}
 		return status;
 	}
 	
 	/**
 	 * Handles the 2PV global consistency check. Sends each participant the PTC
 	 * command and the global master policy version. If all participants are
 	 * using the global version, then authorizations can be performed. Otherwise
 	 * a decision is made whether to allow calls to the policy server to refresh
 	 * or to ABORT.
 	 *
 	 * @return String - COMMIT or ABORT
 	 */
 	public String globalConsistencyCheck() {
 		Message msg = null;
 		boolean integrityOkay = true;
 		boolean authorizationsOkay = true;
 		boolean consistencyOkay = true;
 		
 		// Have coordinator's server call the policy server and retrieve the
 		// current global master policy version
 		int globalVersion = my_tm.callPolicyServer();
 		
 		// Check coordinator for its version
 		if (my_tm.validationMode == 3 && transactionPolicyVersion != globalVersion) {
 			return "ABORT GLOBAL_CONSISTENCY_FAIL";
 		}
 		// Else policies match, and/or VM == 4
 		if (sockList.size() > 0) {
 			int serverNum[] = new int[sockList.size()];
 			int counter = 0;
 			// Gather server sockets
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum[counter] = socketList.nextElement();
 				counter++;
 			}
 			// Send messages to all participants
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("PTC " + globalVersion);
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum[i]).output.writeObject(msg);
 					}
 					catch (Exception e) {
 						System.err.println("PTC Send Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 			// Check coordinator's integrity
 			if (!integrityCheck()) {
 				integrityOkay = false;
 			}
 			// If integrity is okay, re-run auths if necessary with global version 
 			if (integrityOkay) {
 				for (int j = 0; j < queryLog.size(); j++) {
 					if (queryLog.get(j).getPolicy() != globalVersion) {
 						if (!checkLocalAuth()) {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for transaction " + queryLog.get(j).getTransaction() +
 											   ", sequence " + queryLog.get(j).getSequence() +
 											   " with policy v. " + globalVersion +
 											   ": FAIL");
 							authorizationsOkay = false;
 						}
 						else {
 							System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 											   " for transaction " + queryLog.get(j).getTransaction() +
 											   ", sequence " + queryLog.get(j).getSequence() +
 											   " with policy v. " + globalVersion +
 											   ": PASS");
 							queryLog.get(j).setPolicy(globalVersion); // Update policy in log
 						}
 					}
 					else {
 						System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 										   " for txn " + queryLog.get(j).getTransaction() +
 										   ", seq " + queryLog.get(j).getSequence() +
 										   " with policy v. " + globalVersion +
 										   ": ALREADY DONE");
 					}
 				}
 			}
 			// Receive responses
 			for (int i = 0; i < sockList.size(); i++) {
 				if (serverNum[i] != 0) { // Don't listen for the Policy server
 					try {
 						msg = (Message)sockList.get(serverNum[i]).input.readObject();
 						// mode 3: if all participants are using global, they
 						// run auths and return YES/NO, TRUE/FALSE
 						// if any are not using global, ABORT
 						
 						// mode 4: if any not using global, they call policy
 						// server and get global, run auths, return Y/N, T/F
 						
 						// Check response
 						if (msg.theMessage.indexOf("ABORT") != -1) { // Policy inequality
 							consistencyOkay = false;
 						}
 						if (msg.theMessage.indexOf("NO") != -1) { // Someone responded NO
 							integrityOkay = false;
 						}
 						else if (integrityOkay && msg.theMessage.indexOf("FALSE") != -1) { // Someone responded FALSE
 							authorizationsOkay = false;
 						}
 					}
 					catch (Exception e) {
 						System.err.println("PTC Recv Error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}			
 			if (!consistencyOkay) {
 				return "ABORT GLOBAL_CONSISTENCY_FAIL";
 			}
 			else if (!integrityOkay) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 			else if (!authorizationsOkay) {
 				return "ABORT PTC_RESPONSE_FALSE";
 			}
 		}
 		else { // No other servers - check only coordinator for integrity
 			if (!integrityCheck()) {
 				return "ABORT PTC_RESPONSE_NO";
 			}
 			// Run auths if necessary using global version
 			for (int j = 0; j < queryLog.size(); j++) {
 				if (queryLog.get(j).getPolicy() != globalVersion) {
 					if (!checkLocalAuth()) {
 						System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 										   " for transaction " + queryLog.get(j).getTransaction() +
 										   ", sequence " + queryLog.get(j).getSequence() +
 										   " with policy v. " + globalVersion +
 										   ": FAIL");
 						return "ABORT PTC_RESPONSE_FALSE";
 					}
 					else {
 						System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 										   " for transaction " + queryLog.get(j).getTransaction() +
 										   ", sequence " + queryLog.get(j).getSequence() +
 										   " with policy v. " + globalVersion +
 										   ": PASS");
 						queryLog.get(j).setPolicy(globalVersion); // Update policy in log
 					}
 				}
 				else {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + globalVersion +
 									   ": ALREADY DONE");
 				}
 			}
 		}
 		
 		return "COMMIT";
 	}
 	
 	/**
 	 * Method to run authorizations on each query performed on all participating
 	 * servers, including the coordinator.
 	 *
 	 * @return String - COMMIT or ABORT
 	 */
 	public String runAuths(int version) {
 		// Check local auths on coordinator
 		System.out.println("Running auth. on transaction " +
 						   queryLog.get(0).getTransaction() + 
 						   " queries using policy version " +
 						   version);
 		for (int j = 0; j < queryLog.size(); j++) {
 			// If policy used for proof during transaction differs
 			if (queryLog.get(j).getPolicy() != version) {
 				if (!checkLocalAuth()) {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + version +
 									   " (was v. " + queryLog.get(j).getPolicy() +
 									   "): FAIL");
 					return "ABORT LOCAL_AUTHORIZATION_FAIL";
 				}
 				else {
 					System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 									   " for txn " + queryLog.get(j).getTransaction() +
 									   ", seq " + queryLog.get(j).getSequence() +
 									   " with policy v. " + version +
 									   " (was v. " + queryLog.get(j).getPolicy() +
 									   "): PASS");
 					queryLog.get(j).setPolicy(version); // Update policy in log
 				}
 			}
 			else { // Output message of same policy
 				System.out.println("Authorization of " + queryLog.get(j).getQueryType() +
 								   " for txn " + queryLog.get(j).getTransaction() +
 								   ", seq " + queryLog.get(j).getSequence() +
 								   " with policy v. " + version +
 								   ": ALREADY DONE");
 			}
 		}
 		
 		// Contact all other participants, have them run authorizations and return results
 		if (sockList.size() > 0) {
 			Message msg = null;
 			int serverNum;
 			
 			for (Enumeration<Integer> socketList = sockList.keys(); socketList.hasMoreElements();) {
 				serverNum = socketList.nextElement();
 				if (serverNum != 0) { // Don't call the Policy server
 					try {
 						msg = new Message("RUNAUTHS " + version);
 						latencySleep(); // Simulate latency
 						// Send
 						sockList.get(serverNum).output.writeObject(msg);
 						// Rec'v
 						msg = (Message)sockList.get(serverNum).input.readObject();
 						// Check response, add policy version to ArrayList
 						if (msg.theMessage.equals("FALSE")) {
 							return "ABORT LOCAL_AUTHORIZATION_FAIL";
 						}
 					}
 					catch (Exception e) {
 						System.err.println("runAuths() error: " + e.getMessage());
 						e.printStackTrace(System.err);
 					}
 				}
 			}
 		}
 		
 		return "COMMIT";
 	}
 }
