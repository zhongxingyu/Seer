 package intranetp2p;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class P2PHandler {
 
 	private static final String IS_FILE_AVAILABLE = "isFileAvailable";
 
 	CacheMgr mgr;
 	Socket clientSocket;
 	Server server = new Server(7777);
 
 	public P2PHandler() {
 		mgr = new CacheMgr();
 		// Started the LocalServer
 
 		server.startServer();
 
 		listenPeersRequest();
 	}
 
 	/**
 	 * Reads the Datagram listening to the port searches the file returns the
 	 * outputstream Mediator listens to Peers Request TODO : Thread
 	 * Implementation may be Required
 	 */
 	public void listenPeersRequest() {
 
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					// System.out.println(clientSocket);
 
 					ArrayList<String> request = new ArrayList();
 					InputStream in = null;
 					OutputStream out = null;
 					BufferedReader pbr = null;
 					boolean flag = false;
 					String response = new String();
 
 					while (true) {
 
 						/**
 						 * ClientSocket Responsibilities are 1. Listen to the
 						 * ClientRequests and Sends the Responses
 						 */
 						clientSocket = server.getClientSocket();
 
 					//	System.out.println(" Listening your Request ....");
 
 						if (clientSocket != null) {
 
 							System.out
 									.println(" ClientSocket requested is under process ");
 
 							in = clientSocket.getInputStream();
 							out = clientSocket.getOutputStream();
 							pbr = new BufferedReader(new InputStreamReader(in));
 
 						} else {
 							continue;
 						}
 
 						/**
 						 * FirstLine : isFileAvailable SecondLine : FileName
 						 */
 						/*
 						 * System.out .println(" Printing the Details of
 						 * Requested Message : " + br.readLine());
 						 * 
 						 * System.out.println(br.readLine().equals(
 						 * "isFileURLAvailable".trim()));
 						 */
 
 						String str = new String();
 						while ((str = pbr.readLine()) != null) {
 							request.add(str);
 						}
 						System.out.println("kool");
 
 						if (request.get(1).equals("isFileURLAvailable".trim())) {
 
 							System.out
 									.println(" Yes Got Message : Please wait ");
 
 							response = mgr.isFileURLAvailable(request.get(1)
 									.trim());
 
 							// TODO can make it into a single condition
 							if (response != null) {
 								StringTokenizer st = new StringTokenizer(
 										response);
 								out.write(("Available \n" + st.nextToken()
 										+ "\n" + st.nextToken() + "\n"
 										+ st.nextToken() + "\n" + st
 										.nextToken()).getBytes());
 
 							} else {
 								out.write("NA".getBytes());
 
 							}
 
 						} else if (request.get(1).contains("getFile")) {
 							// FileName is after getFile String
 							/**
 							 * FirstList : getFile+"FileName"
 							 */
 							out.write(mgr.getFileByName(request.get(1)
 									.substring("getFile".length())));
 
 						} else if (request.get(1).contains("partFile")) {
 							// processing the response
 							/**
 							 * First Line : getFile+"FileName" Second Line : int
 							 * offset Third Line : int length
 							 */
 							out.write(mgr.getFile(request.get(1).substring(
 									"getFile".length()), Integer
 									.parseInt(request.get(2)), Integer
 									.parseInt(request.get(3))));
 
 						}
 					}
 
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					new RuntimeException(e);
 				}
 
 			}
 		};
 		t.start();
 	}
 
 	/**
 	 * Based on the request from proxy , notifies all peers to search for a
 	 * given fileName ApplicationServer sends the Notification for all peers,to
 	 * search for a file with the given FileName
 	 * 
 	 * MY LOCAL IS ACTING AS A CLIENT
 	 * 
 	 * TODO : Can we rename this method as getFileFromPeers
 	 */
 	public void notifyAllPeers(final String fileurl) throws UnknownHostException {
 //		 Thread nt = new Thread(){
 //			public void run(){
 				ArrayList<Socket> clntSocket = new ArrayList();
 				try {
 
 					// write a method getAllPeersAddress
 					// Iterate each Peer and Send the Info
 
 					byte[] infoBytes = searchAndGetFile("PeerList.txt");
 
 					ByteArrayInputStream bis = new ByteArrayInputStream(infoBytes);
 					BufferedReader br = new BufferedReader(new InputStreamReader(bis));
 
 					assert (infoBytes != null);
 
 					BufferedWriter bw = null;
 					String ipAddress = null;
 

 					int i = 0;
 
 					InputStream is = null;
 					OutputStream os = null;
 
 					/**
 					 * Sending Request PART
 					 */
 					while ((ipAddress = br.readLine()) != null) {
 
 						System.out.println(" Printing the IP Address "
 								+ ipAddress.trim());
 
 						clntSocket.add(new Socket(ipAddress.trim(), 7777));
 
 						// System.out.println(("isFileURLAvailable\n" + fileurl));
 
 						/**
 						 * Format :
 						 * 
 						 * FirstLine : isFileAvailable SecondLine : FileName
 						 */
 						String sb = new String();
 						sb = "isFileURLAvailable" + "\n" + fileurl;
 
 						clntSocket.get(i).getOutputStream().write(sb.getBytes());
 
 						i++;
 
 					}
 					/**
 					 * Receving RESPONSE PART
 					 */
 
 					System.out.println(" Going to Sleep :)");
 
 					Thread.sleep(50000);
 
 					// Get the list of PEERS who has the Required FileName
 					Socket[] availablePeers = null;
 					int countOfAvailable = 0;
 					for (Socket peer : clntSocket) {
 
 						System.out
 								.println(" Waked UP!! Getting the List of Available Peers");
 
 						is = peer.getInputStream();
 						br = new BufferedReader(new InputStreamReader(is));
 
 						System.out.println(br.ready());
 
 						if (br.ready() && br.readLine().trim().equals("Available")) {
 
 							availablePeers[countOfAvailable] = peer;
 							countOfAvailable++;
 						}
 
 					}
 
 					// getting the count of AVAILABLE SERVER PEERS
 					if (countOfAvailable > 1) {
 
 						for (Socket peer : availablePeers) {
 
 							// TODO logic of getting the bytes from each
 							/**
 							 * 1. Get the size of the file 2. Compute the bytes for each
 							 * Peer 3. Request the Bytes Assuming each peer has COMPLETE
 							 * FILE FOR SAMPLE , GET THE FILE SIZE FROM THE FIRST PEER
 							 */
 							is = peer.getInputStream();
 							os = peer.getOutputStream();
 
 							br = new BufferedReader(new InputStreamReader(is));
 							bw = new BufferedWriter(new OutputStreamWriter(os));
 
 							int size = 0, offset = 0, length = 0;
 
 							bw.write("get" + fileurl + "\n" + offset + "\n" + "length");
 
 							/**
 							 * construct a packet 1. GET 2. FILENAME
 							 * 
 							 */
 
 						}
 					} else if (countOfAvailable == 1) {
 
 						try {
 
 							is = availablePeers[0].getInputStream();
 							os = availablePeers[0].getOutputStream();
 
 							br = new BufferedReader(new InputStreamReader(is));
 							bw = new BufferedWriter(new OutputStreamWriter(os));
 
 							bw.write("get" + fileurl);
 
 							// wait for the response
 							Thread.sleep(5000);
 							
 							
 							boolean flag = false;
 							while (true) {
 								while (br.readLine() != null) {
 									mgr.saveFile(br.readLine().getBytes(), fileurl);
 									flag = true;
 								}
 								if (flag) {
 									break;
 								}
 							}
 						} catch (Exception e) {
 							throw new RuntimeException(e);
 						} finally {
 							is.close();
 							os.close();
 						}
 					} else {
 						// NO SERVER PEER HAS THE FILE
 					}
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					throw new RuntimeException(e);
 				}
 
 //			}
 //		}
 //		nt.start();
 		System.out.println(" Bye ");
 	}
 
 	/**
 	 * 
 	 * API Sends the response to the Client PRoxy
 	 */
 	public Byte sendResponse() {
 		return null;
 	}
 
 	/**
 	 * CommunicationMediator calls this API to get Responses from other Peers
 	 */
 	public void getResponseFromPeers() {
 
 	}
 
 	/**
 	 * CommunicationMediator sends a BroadCast Message to all the Clients/Peers
 	 * conected
 	 * 
 	 * @return
 	 */
 	public Byte broadcastMessage() {
 		return null;
 	}
 
 	// API to return the peer list file
 	public byte[] searchAndGetFile(String fileName) {
 
 		String path = System.getProperty("user.home") + File.separator
 				+ "LANP2P";
 		File dir = new File(path);
 
 		DataInputStream dis;
 
 		System.out.println(System.getProperty("user.home"));
 
 		if (!dir.exists()) {
 			System.out
 					.println("Directory Doesnot exist...!Creating the Directory");
 			dir.mkdir();
 		}
 
 		for (File search : dir.listFiles()) {
 
 			System.out.println("Printing the Files in the Directory "
 					+ search.getName());
 			if (search.getName().equals(fileName)) {
 				try {
 					dis = new DataInputStream(new FileInputStream(search));
 					byte[] b1 = new byte[dis.available()];
 
 					dis.readFully(b1);
 
 					System.out.println(" Printing the bytes : " + b1.length);
 					return b1;
 
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (EOFException eof) {
 					eof.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}
 		return null;
 	}
 
 }
