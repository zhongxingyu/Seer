 package networking;
 
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import networking.NetworkMessage.Type;
 
 /**********************************************************
  *
  * @author 
  *
  *This class handles the clients communication with the server. It receives and
  *sends information over input and output streams continuously.
  *\*********************************************************/
 class Client extends Thread{
 	public int clientId;
 
 	private Socket socket;
 	private ClientWriteThread writeThread;
 	private ClientReadThread readThread;
 	private LinkedBlockingQueue<NetworkMessage> toSend; 
 	private LinkedBlockingQueue<ChatMessage> chatReceived; 
 	private LinkedBlockingQueue<ActionMessage> actionReceived;
 	private Lock isRegistered;
 	public Networking _net;
 	private volatile boolean shutdown;
 
 	/*
 	 * These constants are the port number and host name for your server. For
 	 * now, the server name is set to localhost. If you wish to connect to
 	 * another computer, change the name to the name of that computer or IP.
 	 *
 	 * NOTE: you may need to change the port # periodically, especially if your
 	 * exited the application on error and did not close the socket cleanly. The
 	 * socket may still be in use when you try to run again and this will cause
 	 * otherwise working code to fail due to a busy port.
 	 */
 	private int port;// = 1337;
 	private String addrName = "localhost";
 	public String username;
 
 	/**********************************************************
 	 * TODO: Initializes the private variables
 	 * creates a socket (you write that method)
 	 * TODO: And create the two new, read & write threads.
 	 * @throws IOException 
 	 * @throws UnknownHostException 
 	 **********************************************************/
 	public Client(String _hostIp, int _port, String _username, Networking net) throws IOException {
 		//init private i-vars here
 		//userName = _userName;
 
 		//System.out.println("Connecting as " + userName + "...");
 
 		toSend = new LinkedBlockingQueue<NetworkMessage>();
 		chatReceived = new LinkedBlockingQueue<ChatMessage>();
 		actionReceived = new LinkedBlockingQueue<ActionMessage>();
 		isRegistered = new Lock(0);
 		shutdown = false;
 
 		port = _port;
 		addrName = _hostIp;
 		clientId = -1;
 		username = _username;
 		socket = null;
 		_net = net;
 
 		socket = createSocket();
 	}
 
 	/******************************************************************
 	 * This function should use the server port and name information to create a
 	 * Socket and return it.
 	 *
 	 * NOTE:Make sure to catch and report exceptions!!!
 	 * @throws IOException 
 	 *******************************************************************/
 	public Socket createSocket() throws IOException {
 		//InetAddress addr = null;
 		Socket sock = null;
 		sock = new Socket(addrName, port);
		sock.setSoTimeout(100);
 		return sock;
 	}
 
 	public boolean signOff() {
 		System.out.println("client: shutting down <" + clientId + ", " + username + ">");
 		shutdown = true;
 		try {
 			socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public boolean send (NetworkMessage nm) {
 		//l.lock();
 		try {
 			isRegistered.check();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//System.out.println("Current id before sending: " + clientId);
 		nm.setSenderID(clientId);
 		boolean ret = toSend.offer(nm);
 		isRegistered.release();
 		return ret;
 	}
 
 	public NetworkMessage receive (Type t) {
 		try {
 			if (t == Type.CHAT) {
 				return chatReceived.take();
 			} else if (t == Type.ACTION) {
 				return actionReceived.take();
 				//if notStack addAction
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public void run() {
 		/*
 		int i = 0;
 		while (socket == null && i < 10) {
 			try {
 				socket = createSocket();
 			} catch (UnknownHostException e1) {
 				System.out.println("client: cannot find host, attempt: " + i);
 				i++;
 			} catch (IOException e1) {
 				System.out.println("client: io failure, attempt: " + i);
 				i++;
 			}
 			try {
 				Thread.sleep(4000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		 */
 
 		// Check that socket creation was successful
 		if (socket == null) {
 			System.out.println("client: error creating socket");
 			return;
 		}
 
 		System.out.println("client: socket successfully connected");
 
 		//make threads here
 		writeThread = new ClientWriteThread();
 		readThread = new ClientReadThread(this);
 
 		//System.out.println("Running threads");
 		writeThread.start();
 		readThread.start();
 		try {
 			readThread.join();
 			writeThread.join();
 			System.out.println("client<" + clientId + ", " + username + "> central thread has quit");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/***********************************************************
 	 * This thread is used to write to the socket
 	 * It has access to the Client's <socket> variable
 	 ***********************************************************/ 
 	private class ClientWriteThread extends Thread {
 		//private PrintWriter writer;
 		//private BufferedReader stdinReader;
 		private ObjectOutputStream writer;
 
 		/*********************************************************************
 		 *  Initialize both the <PrintWriter> and the <BufferedReader> classes.
 		 * - The <writer> should be writing to the socket (socket.getOutputStream())
 		 * - The <stdinReader> should be reading (AND BLOCKING) from standard input (System.in)
 		 * Make sure to catch exceptions.      
 		 ***********************************************************************/
 		public ClientWriteThread() {
 			try {
 				//writer = new PrintWriter(socket.getOutputStream());
 				writer = new ObjectOutputStream(socket.getOutputStream());
 				//stdinReader = new BufferedReader(new InputStreamReader(System.in));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		/**********************************************************************
 		 * This is what runs when the Thread is started
 		 * This should keep trying to read from the STDIN reader,
 		 * and once it gets a message it should send it to the server
 		 * by calling writer.println()
 		 ***********************************************************************/ 
 		public void run() {
 			NetworkMessage message = null;
 
 			try {
 				//Get Id
 				System.out.println("client: requesting id, current id: " + clientId);
 				writer.writeObject(new Handshake(clientId, -1, username));
 				writer.flush();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			while(true) {
 				//System.out.println("Print your message");
 				//message = stdinReader.readLine();
 				//System.out.println("Reader is taking now.");
 				try {
 					message = toSend.take();
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				//System.out.println("Reader is done taking.");
 				if (message != null || !shutdown) {
 					/* This is necessary for the server to know who you are */          
 					//System.out.println(userName + " sending: " + message);
 					//writer.println(userName);
 					//writer.flush();
 					try {
 						//System.out.println("Writing out message");
 						writer.writeObject(message);
 						writer.flush();
 					} catch (IOException e) {
 						if (!shutdown) {
 							System.out.println("client: host has disconnected");
 							_net._suggestPanel.connectionError();
 						}
 						break;
 					}
 				} else {
 					System.err.println("client<" + clientId + ", " + username + ">: writing thread has quit!");
 					break;
 				}
 			}
 		}
 
 	}
 
 	/************************************************************************ 
 	 * This thread is used to read from a socket
 	 * It has access to Client's <socket> variable
 	 * This blocks!
 	 ************************************************************************/
 	private class ClientReadThread extends Thread {
 		//private BufferedReader reader;
 		private ObjectInputStream reader;
 		private Client parent;
 
 		/*****************************************************************
 		 * This should initialize the buffered Reader
 		 *****************************************************************/
 		public ClientReadThread(Client _parent) {
 			/* TODO */
 			parent = _parent;
 			try {
 				//reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 				reader = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		/***********************************************************************
 		 * This should keep trying to read from the BufferedReader
 		 * The message read should be printed out with a simple System.out.println()
 		 * This method blocks
 		 * NOTE: If the server dies, reader.readLine() will be returning null!
 		 ************************************************************************/
 		public void run() {
 			NetworkMessage message = null;
 			while (true) {
 				try {
 					//System.out.println("Waiting for message...");
 					//message = reader.readLine();
 					message = (NetworkMessage) reader.readObject();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 					if (shutdown)
 						System.err.println("client<" + clientId + ", " + username + ">: reading thread has quit on connection loss");
 					else {
						//e.printStackTrace();
 						System.out.println("client<" + clientId + ", " + username + ">: connection with host has closed, shutting down");
 						_net._suggestPanel.connectionError();
 					}
 					break;
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (message != null || !shutdown) {
 					//System.out.println("client: received message of type " + message.type);
 					if (message.type == Type.ACTION) {
 						_net.backend.receiveNetworkedObject(((ActionMessage) message).action);
 						actionReceived.offer((ActionMessage) message);
 					} else if (message.type == Type.CHAT) {
 						//System.out.println("client: received chat message: " + ((ChatMessage) message).text);
 						ChatMessage chatMsg = (ChatMessage) message;
 						_net._suggestPanel.newMessage(chatMsg.uname, chatMsg.text);
 						chatReceived.offer((ChatMessage) message);
 					} else if (message.type == Type.HANDSHAKE) {
 						clientId = ((Handshake) message).client_id;
 						if (clientId == -1) {
 							//Error condition, username is already in use
 							username = _net._suggestPanel.retryUsername();
 							if (username != null) {
 								System.out.println("client: requesting id again, current id: " + clientId);
 								/* Bypassing safety registration lock */
 								toSend.offer(new Handshake(clientId, -1, username));
 								continue;
 							} else {
 								/* Shut down networking hard */
 								_net._suggestPanel.connectionError();
 								_net.signOff();
 								return;
 							}
 						}
 						System.out.println("client: udpate start uid to: "+((Handshake) message).getStartUID());
 						_net.getBackend().setStartUID(((Handshake) message).getStartUID());
 						System.out.println("client: update id to: " + clientId);
 						_net.getBackend().loadFromNetwork(((Handshake) message).project);
 						parent.isRegistered.release();
 					} else if (message.type == Type.USER_UPDATE) {
 						System.out.println("client: received updated client list");
 						for(ClientInfo ci : ((UpdateUsersMessage) message).activeUsers) {
 							System.out.println(ci.username);
 						}
 						_net._suggestPanel.updateUsers(((UpdateUsersMessage) message).activeUsers);
 					}
 				} else {
 					System.err.println("client<" + clientId + ", " + username + ">: reading thread has quit");
 					break;
 				}
 			}
 		}
 	}
 }
