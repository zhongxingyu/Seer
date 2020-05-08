 package Main;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.SocketAddress;
 
 /**
  * Provides threads for the server so multiple clients can be handled by a
  * single server
  * 
  * @author Cole Christie
  * 
  */
 public class ServerThread extends Thread {
 	private Logging mylog;
 	private Socket socket;
 	private Networking network;
 	private Auth subject;
 	private int UID;
 	private Crypto crypt;
 	private Object JobLock;
 	private JobManagement JobQueue;
 
 	/**
 	 * CONSTRUCTOR for Server Worker Thread
 	 */
 	public ServerThread(Auth passedSubject, Logging passedLog, Socket passedSocket, int passedUID, Object passedLock,
 			JobManagement passedJobQueue) {
 		subject = passedSubject;
 		mylog = passedLog;
 		socket = passedSocket;
 		UID = passedUID;
 		JobLock = passedLock;
 		JobQueue = passedJobQueue;
 		network = new Networking(mylog);
 	}
 
 	/**
 	 * CONSTRUCTOR for Server Management Thread
 	 */
 	public ServerThread(Logging passedLog, Object passedLock, JobManagement passedJobQueue) {
 		mylog = passedLog;
 		JobLock = passedLock;
 		JobQueue = passedJobQueue;
 	}
 
 	/**
 	 * Runs the Job loading framework based upon the execution request passed to
 	 * it (string argument).
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public void JobLoader(String type) {
 		synchronized (JobLock) {
 			if (type.compareToIgnoreCase("sw") == 0) {
 				JobQueue.SampleWindows();
 				mylog.out("INFO", "Loaded 10 sample jobs (Windows).");
 			} else if (type.compareToIgnoreCase("sl") == 0) {
 				JobQueue.SampleLinux();
 				mylog.out("INFO", "Loaded 10 sample jobs (Linux/UNIX).");
 			} else if (type.compareToIgnoreCase("sa") == 0) {
 				JobQueue.Sample();
 				mylog.out("INFO", "Loaded 10 sample jobs (Any OS).");
 			} else if (type.compareToIgnoreCase("cuq") == 0) {
 				JobQueue.ClearUnsentQueue();
 				mylog.out("INFO", "Unassigned job queue reset.");
 			} else if (type.compareToIgnoreCase("caq") == 0) {
 				JobQueue.ClearSentQueue();
 				mylog.out("INFO", "Assigned job queue reset.");
 			} else if (type.compareToIgnoreCase("list") == 0) {
 				mylog.out("INFO", "[" + JobQueue.UnassignedCount() + "] unassigned jobs are left in the queue");
 				mylog.out("INFO", "[" + JobQueue.AssignedCount() + "] jobs are in progress");
 			}
 			// TODO add "load" option
 		}
 	}
 
 	/**
 	 * Runs the Job loading framework based upon the execution request passed to
 	 * it (string argument). Returns the count (int) of the number of jobs that
 	 * were loaded.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public void JobLoader(String type, String filename) {
 		int QtyJobsLoaded = 0;
 		synchronized (JobLock) {
 			if (type.compareToIgnoreCase("load") == 0) {
 				try {
 					QtyJobsLoaded = JobQueue.Load(filename);
 				} catch (IOException e) {
 					mylog.out("ERROR", "Failed to load jobs from file [" + filename + "]");
 				}
 				mylog.out("INFO", "Loaded [" + QtyJobsLoaded + "] jobs.");
 			}
 		}
 	}
 
 	/**
 	 * Assigns a job to the client in the system and returns a string that has
 	 * the requested jobs instructions
 	 * 
 	 * @param clientID
 	 * @return
 	 */
 	public String AssignJob(String clientID, String OS, int ClientSecurityLevel) {
 		String job = "";
 		synchronized (JobLock) {
 			job = JobQueue.Assign(clientID, OS, ClientSecurityLevel);
 		}
 		return job;
 	}
 
 	/**
 	 * Server thread Enables multi-client support
 	 */
 	public void run() {
 		mylog.out("INFO", "Establishing session with client number [" + UID + "]");
 
 		// Load and save the cleints IP and port for future UUID creation
 		SocketAddress theirAddress = socket.getRemoteSocketAddress();
 		String ClientIP = theirAddress.toString();
		ClientIP = ClientIP.replace("/", "");
 		
 		// Bind I/O to the socket
 		network.BringUp(socket);
 
 		// Prep
 		String fromClient = null;
 
 		// Activate crypto
 		crypt = new Crypto(mylog, subject.GetPSK());
 		byte[] fetched = network.ReceiveByte();
 		String dec = crypt.decrypt(fetched);
 		String craftReturn = dec + "<S>";
 		mylog.out("INFO", "Validating encryption with handshake.");
 		byte[] returnData = crypt.encrypt(craftReturn);
 		network.Send(returnData);
 
 		// Main Loop
 		while (!socket.isClosed()) {
 			// Collect data sent over the network
 			fetched = network.ReceiveByte();
 			if (fetched == null) {
 				mylog.out("WARN", "Client disconnected abruptly");
 				break;
 			}
 
 			// Decrypt sent data
 			fromClient = crypt.decrypt(fetched);
 
 			// Precalculate meta data from passed arguments (for job
 			// distribution)
 			boolean jobRequest = false;
 			String ClientName = ClientIP;
 			String ClientOS = "";
 			int ClientSecurityLevel = 0;
 			if (fromClient == null) {
 				mylog.out("WARN", "Client disconnected abruptly");
 				break;
 			} else {
 				// Only if the client did not abruptly disconnect, and only if
 				// the following abstract conditions are met
 				if (fromClient.toLowerCase().contains("job")) {
 					String[] CHOP = fromClient.split(":");
 					jobRequest = true;
 					// Add the random number passed to us to the servers UID of
 					// this client session to create a reasonable UUID
 					if (CHOP.length == 4) {
 						ClientName = ClientName + CHOP[1];
 						ClientOS = CHOP[2];
 						ClientSecurityLevel = Integer.parseInt(CHOP[3]);
 					} else {
 						// The client failed to send all of the meta data we
 						// need, so abort the job request
 						fromClient = "Job request failed. Missing meta data in request.";
 						jobRequest = false;
 					}
 				}
 			}
 
 			// Case sensitive actions based upon data received
 			if (fromClient.compareToIgnoreCase("quit") == 0) {
 				mylog.out("INFO", "Client disconnected gracefully");
 				break;
 			} else if (jobRequest) {
 				mylog.out("INFO", "Client [" + ClientName + "] with security level [" + ClientSecurityLevel
 						+ "] reuested a job for [" + ClientOS + "]");
 				synchronized (JobLock) {
 					String work = JobQueue.Assign(ClientName, ClientOS, ClientSecurityLevel);
 					returnData = crypt.encrypt(work);
 					network.Send(returnData);
 					if (work.length() > 0) {
 						mylog.out("INFO", "JobOut:[" + work + "]");
 						mylog.out("INFO", "[" + JobQueue.UnassignedCount() + "] unassigned jobs are left in the queue");
 						mylog.out("INFO", "[" + JobQueue.AssignedCount() + "] jobs are in progress");
 					} else {
 						mylog.out("WARN", "There are no jobs for [" + ClientOS + "] with Security Level ["
 								+ ClientSecurityLevel + "]");
 					}
 				}
 			} else if (fromClient.compareToIgnoreCase("<REKEY>") == 0) {
 				SendACK(); // Send ACK
 				String prime = null;
 				String base = null;
 
 				// Grab 1st value (should be handshake for PRIME)
 				fromClient = fromNetwork();
 				SendACK(); // Send ACK
 				if (fromClient.compareToIgnoreCase("<PRIME>") == 0) {
 					prime = fromNetwork();
 					SendACK(); // Send ACK
 				} else {
 					mylog.out("ERROR", "Failed proper DH handshake over the network (failed to receive PRIME).");
 				}
 
 				// Grab 2nd value (should be handshake for BASE)
 				fromClient = fromNetwork();
 				SendACK(); // Send ACK
 				if (fromClient.compareToIgnoreCase("<BASE>") == 0) {
 					base = fromNetwork();
 					SendACK(); // Send ACK
 				} else {
 					mylog.out("ERROR", "Failed proper DH handshake over the network (failed to receive BASE).");
 				}
 
 				// Use received values to start DH
 				DH myDH = new DH(mylog, prime, 16, base, 16);
 
 				// Send rekeying ack
 				returnData = crypt.encrypt("<REKEY-STARTING>");
 				network.Send(returnData);
 				RecieveACK(); // Wait for ACK
 
 				// Perform phase1
 				myDH.DHPhase1();
 
 				// Receive client public key
 				byte[] clientPubKey = null;
 				fromClient = fromNetwork();
 				SendACK(); // Send ACK
 				if (fromClient.compareToIgnoreCase("<PUBLICKEY>") == 0) {
 					clientPubKey = fromNetworkByte();
 					SendACK(); // Send ACK
 					returnData = crypt.encrypt("<PubKey-GOOD>");
 					network.Send(returnData);
 					RecieveACK(); // Wait for ACK
 				} else {
 					mylog.out("ERROR", "Failed to receieve client public key.");
 				}
 
 				// Send server public key to client
 				network.Send(crypt.encrypt("<PUBLICKEY>"));
 				RecieveACK(); // Wait for ACK
 				network.Send(crypt.encrypt(myDH.GetPublicKeyBF()));
 				RecieveACK(); // Wait for ACK
 				fromClient = fromNetwork();
 				SendACK(); // Send ACK
 				if (fromClient.compareToIgnoreCase("<PubKey-GOOD>") != 0) {
 					mylog.out("ERROR", "Client has failed to acknowledge server public key!");
 				}
 
 				// Use server DH public key to generate shared secret
 				myDH.DHPhase2(myDH.CraftPublicKey(clientPubKey));
 
 				// Final verification
 				// System.out.println("Shared Secret (Hex): " +
 				// myDH.GetSharedSecret(10));
 				crypt.ReKey(myDH.GetSharedSecret(10));
 
 			} else {
 				mylog.out("INFO", "Received from client [" + fromClient + "]");
 				craftReturn = "<S>" + fromClient;
 				returnData = crypt.encrypt(craftReturn);
 				network.Send(returnData);
 			}
 			try {
 				Thread.sleep(1000); // Have the thread sleep for 1 second to
 									// lower CPU load on the server
 			} catch (InterruptedException e) {
 				mylog.out("ERROR", "Failed to have the thread sleep.");
 				e.printStackTrace();
 			}
 		}
 
 		// Tear down bound I/O
 		network.BringDown();
 
 		// Close this socket
 		try {
 			socket.close();
 		} catch (IOException e) {
 			mylog.out("ERROR", "Failed to close SOCKET within SERVER THREAD");
 		}
 	}
 
 	/**
 	 * Reads a string from the network
 	 * 
 	 * @return
 	 */
 	private String fromNetwork() {
 		String decryptedValue = null;
 
 		byte[] initialValue = network.ReceiveByte();
 		if (initialValue == null) {
 			mylog.out("WARN", "Client disconnected abruptly");
 		}
 		decryptedValue = crypt.decrypt(initialValue);
 		if (decryptedValue == null) {
 			mylog.out("WARN", "Client disconnected abruptly");
 		} else if (decryptedValue.compareToIgnoreCase("quit") == 0) {
 			mylog.out("WARN", "Client disconnected abruptly");
 		}
 
 		return decryptedValue;
 	}
 
 	/**
 	 * Read bytes from the network
 	 * 
 	 * @return
 	 */
 	private byte[] fromNetworkByte() {
 		byte[] decryptedValue = null;
 
 		byte[] initialValue = network.ReceiveByte();
 		if (initialValue == null) {
 			mylog.out("WARN", "Client disconnected abruptly");
 		}
 		decryptedValue = crypt.decryptByte(initialValue);
 		if (decryptedValue == null) {
 			mylog.out("WARN", "Client disconnected abruptly");
 		}
 
 		return decryptedValue;
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void SendACK() {
 		network.Send(crypt.encrypt("<ACK>"));
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void RecieveACK() {
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 		network.Send(crypt.encrypt("<ACK>"));
 	}
 }
