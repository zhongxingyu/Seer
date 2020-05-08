 package client;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import server.Server;
 import view.ChatPanel;
 import client.handling.TCPHandlerClient;
 import client.handling.UDPClient;
 import common.CommonConstants;
 import common.MasterClass;
 import common.Message;
 import common.MessageInfoStrings;
 import common.MessageType;
 import common.handling.HandlingException;
 import common.logging.EventType;
 import common.logging.Log;
 import controller.ContactListController;
 import controller.LoginController;
 
 /**
  * Client master class, contains handler threads, IP and port maps.
  * @author etudiant
  *
  */
 public class Client implements MasterClass { 
 	
 	private static final long DEFAULT_TIMEOUT_TIME = 0;
 	public static final long LIVE_DELAY = 4000;
 	
 	private String login;
 	private String pass;
 	
 	private String serverIp;
 	private int serverPort;
 	private Socket clientSocket;
 	
 	private boolean running = false;
 	private boolean connected = false;
 	private boolean tryToConnect = true;
 	
 	private Map<String, InetAddress> clientIps;
 	private Map<String, String> clientPorts;
 	private List<String> clientLogins;
 	
 	private ClientTimeoutHandler timeoutHandler;
 	
 	private TCPHandlerClient tcpHandlerClient;
 	private UDPClient udpClient;	
 	
 	private long timeout;
 	private int mainUDPListeningPort;
 	private Log log = new Log();
 	private InetAddress ipOtherClient;
 	private int portOtherClient;
 	private boolean serverUp = false;
 	private Date serverTimeoutDate;
 	
 	private LoginController loginController;
 	private ContactListController contactListController;
 	
 	private Map<String, ChatPanel> wereDisconnectedMulti = new HashMap<String, ChatPanel>();
 
 	private Map<String, ChatPanel> wereDisconnectedSingle = new HashMap<String, ChatPanel>();
 
 	/**
 	 * Constructs a Client object.
 	 * @param login - Client login.
 	 * @param pass - Client password.
 	 * @param serverIp - IP address of the Server. 
 	 * @param serverPort - port used by the Server (Note: by default, should be 8001).
 	 * @throws IOException
 	 */
 	public Client(String login, String pass, String serverIp, int serverPort) throws IOException {
 		super();
 		this.login = login;
 		this.pass = pass;
 		
 		//clientSocket = new Socket(serverIp, serverPort);
 		clientSocket = new Socket();
 		this.serverIp = serverIp;
 		this.serverPort = serverPort;
 		
 		clientIps = new HashMap<String, InetAddress>();
 		clientPorts = new HashMap<String, String>();
 		setClientLogins(new ArrayList<String>());
 		timeoutHandler = new ClientTimeoutHandler(this);
 		running = false;
 		timeout = DEFAULT_TIMEOUT_TIME;
 		log = new Log();
 	}
 
 	public Client(String login, String serverIp, int port) {
 		super();
 		this.login=login;
 	}
 	
 	/**
 	 * Prints a summary of the Client's settings.
 	 */
 	public void printRecap() {
 		System.out.println("Starting the client:");
 		System.out.println("\tIp:\t" + clientSocket.getInetAddress());
 		System.out.println("\tPort:\t" + clientSocket.getLocalPort());
 	}
 	
 	/**
 	 * Starts the client: launch the TCP thread and the UDP master class.
 	 */
 	public void start() {
 		running = true;
 		
 		setupHandlers();
 		
 		tcpHandlerClient.run();
 		if (connected == true) {
 			log.log(EventType.START, "Starting UDP handler");
 			udpClient.start();
 		}
 	}
 
 	/**
 	 * Creates TCP and UDP handlers, without starting them.
 	 */
 	public void setupHandlers() {
 		tcpHandlerClient = new TCPHandlerClient(clientSocket, this);
 		udpClient = new UDPClient(this);
 	}
 
 	public static void main(String args[]) {
 		String c_login=args[0];
 		String c_pass=args[1];
 		String ip = args[2];
 		try {
 			Client client = new Client(c_login, c_pass, ip, CommonConstants.DEFAULT_SERVER_PORT);
 			client.printRecap();
 			client.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	public String getServerIp() {
 		return serverIp;
 	}
 
 	public void setServerIp(String ip) {
 		this.serverIp = ip;
 	}
 
 	public int getServerPort() {
 		return serverPort;
 	}
 
 	public void setServerPort(int port) {
 		this.serverPort = port;
 	}
 	
 	public void setRunning(boolean run) {
 		running = run;
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 
 	public ClientTimeoutHandler getTimeoutHandler() {
 		return timeoutHandler;
 	}
 	
 	public long getTimeoutTime() {
 		return timeout;
 	}
 	
 	public Map<String, InetAddress> getClientIps() {
 		return clientIps;
 	}
 	
 	public void setClientIps(Map<String, InetAddress> o)
 	{
 		this.clientIps=o;
 	}
 	
 	public Map<String, String> getClientPorts() {
 		return clientPorts;
 	}
 
 	public void setClientPorts(Map<String, String> clientPorts) {
 		this.clientPorts = clientPorts;
 	}
 	
 	public int getUDPMainListeningPort() {
 		return mainUDPListeningPort;
 	}
 
 	public void setUDPMainListeningPort(int listeningPort) {
 		this.mainUDPListeningPort = listeningPort;
 	}
 
 	public Log getLog() {
 		return log;
 	}
 
 	public void setLog(Log log) {
 		this.log = log;
 	}
 
 	public InetAddress getIpotherclient() {
 		return ipOtherClient;
 	}
 
 	public void setIpotherclient(InetAddress ipotherclient) {
 		this.ipOtherClient = ipotherclient;
 	}
 
 	public int getPortotherclient() {
 		return portOtherClient;
 	}
 
 	public void setPortotherclient(int portotherclient) {
 		this.portOtherClient = portotherclient;
 	}
 	
 	private String loginotherclient;
 	
 	public String getLoginotherclient() {
 		return loginotherclient;
 	}
 
 	public void setLoginotherclient(String loginotherclient) {
 		this.loginotherclient = loginotherclient;
 	}
 
 	public void setLoginController(LoginController loginController) {
 		this.loginController = loginController;
 	}
 
 	public LoginController getLoginController() {
 		return loginController;
 	}
 
 	public List<String> getClientLogins() {
 		return clientLogins;
 	}
 
 	public void setClientLogins(List<String> clientLogins) {
 		this.clientLogins = clientLogins;
 	}
 
 	public ContactListController getContactListController() {
 		return contactListController;
 	}
 	
 	public void setContactListController(ContactListController clc) {
 		this.contactListController = clc;
 	}
 
 	public TCPHandlerClient getTcpHandlerClient() {
 		return tcpHandlerClient;
 	}
 
 	public UDPClient getUdpClient() {
 		return udpClient;
 	}
 
 	public Socket getClientSocket() {
 		return clientSocket;
 	}
 
 	@Override
 	public void run() {
 		log.log(EventType.START, "Starting Client as Thread");
 		start();
 	}
 
 	/**
 	 * Server connection behavior.
 	 * @return True if the Client attempts to connect to the Server when disconnected.
 	 */
 	public boolean tryToConnect() {
 		return tryToConnect;
 	}
 	
 	/**
 	 * Sets the Server connection behavior. 
 	 * @param tryToConnect - Should the Client attempt to connect with the Server?
 	 */
 	public void tryToConnect(boolean tryToConnect) {
 		this.tryToConnect = tryToConnect;
 	}
 
 	/**
 	 * Connection status.
 	 * @return True if the Client is connected to a Server.
 	 */
 	public boolean isConnected() {
 		return connected;
 	}
 
 	/**
 	 * Sets the connection status. Must be called whenever the client
 	 * acquires or loses connection to the server.
 	 * @param isConnected - New connection status.
 	 */
 	public void setConnected(boolean isConnected) {
 		this.connected = isConnected;
 	}
 
 	/**
 	 * Disconnects the {@link Client}: sends a DISCONNECT {@link Message} to the {@link Server}
 	 * and sets the Client's connection status to false.
 	 */
 	public void disconnect() {
 		
 		if (serverUp) {
 			Message disconnectMsg = new Message(MessageType.DISCONNECT);
 			disconnectMsg.addInfo(MessageInfoStrings.LOGIN, login);
 			
 			try {
 				udpClient.getSend().getMessageManager().handleMessage(disconnectMsg, udpClient.getSend().getSocket());
 				connected = false;
 				running = false;
 			} catch (HandlingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		} else {
 			Message disconnectMsgClient =  new Message(MessageType.DISCONNECT_CLIENT);
 			disconnectMsgClient.addInfo(MessageInfoStrings.LOGIN, login);
 			
 			for (String key : clientIps.keySet()) {
 				InetAddress targetAddress = clientIps.get(key);
 				int targetPort = Integer.parseInt(clientPorts.get(key));
 				try {
 					DatagramSocket targetSocket = new DatagramSocket();
 					
 					ByteArrayOutputStream b = new ByteArrayOutputStream();
 				    ObjectOutputStream o = new ObjectOutputStream(b);
 				    o.writeObject(disconnectMsgClient);
 					byte[] buf = b.toByteArray();
 					DatagramPacket p = new DatagramPacket(buf, buf.length, targetAddress, targetPort);
 					targetSocket.connect(targetAddress, targetPort);
 					targetSocket.send(p);
 					System.out.println("Sending disco message to " + key + "(" + targetAddress + 
 							" : " + targetPort + ")");
 					
 					
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called after updating the login list, this removes old clients from the IP and port Maps.
 	 */
 	public void removeDisconnectedClients() {
 		Map<String, InetAddress> clientIpsClone = new HashMap<String, InetAddress>(clientIps);
 		for (String login : clientIpsClone.keySet()) {
 			if (!clientLogins.contains(login)) {
 				clientIps.remove(login);
 				clientPorts.remove(login);
 			}
 		}
 	}
 
 	/**
 	 * Reconnects disconnected clients and informs that these clients are reconnected.
 	 */
 	public void updateChatPanels() {
 		///////////////////////////
 		// multi user ChatPanels //
 		///////////////////////////
 		Map<Vector<String>, ChatPanel> chatPanelMap = contactListController.getClw().getMapListChat();
 		for (Vector<String> currentPanelList : chatPanelMap.keySet()) {
 			for (String login : currentPanelList) {
 				if (!clientIps.containsKey(login)) {
 					Message msg = new Message(MessageType.REQUEST_IP); //TODO: extract method
 					msg.addInfo(MessageInfoStrings.LOGIN, login);
 					msg.addInfo(MessageInfoStrings.PORT, getUDPMainListeningPort() + "");
 					try {
 						udpClient.getSend().getMessageManager().handleMessage(msg, udpClient.getSend().getSocket());
 					} catch (HandlingException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 					
 					wereDisconnectedMulti.put(login, chatPanelMap.get(currentPanelList));
 				}
 			}
 		}
 		////////////////////////////
 		// single user ChatPanels //
 		////////////////////////////
 		Map<String, ChatPanel> chatPanelMapSingle = contactListController.getClw().getDiscMap();
 		for (String login : chatPanelMapSingle.keySet()) {
 			if (!clientIps.containsKey(login)) {
 				Message msg = new Message(MessageType.REQUEST_IP); //TODO: extract method
 				msg.addInfo(MessageInfoStrings.LOGIN, login);
 				msg.addInfo(MessageInfoStrings.PORT, getUDPMainListeningPort() + "");
 				try {
 					udpClient.getSend().getMessageManager().handleMessage(msg, udpClient.getSend().getSocket());
 				} catch (HandlingException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 				
 				wereDisconnectedSingle.put(login, chatPanelMapSingle.get(login));
 			}
 		}
 		
 		
 	}
 	
 	public void reconnectedClients() {
 		/////////////////
 		// final stuff //
 		/////////////////
 		for (String key : wereDisconnectedSingle.keySet()) {
 			if (clientIps.containsKey(key)) {
 				System.out.println("######### reconnected - " + key);
 				wereDisconnectedSingle.get(key).displayReconnectedMessage(key);
				wereDisconnectedSingle.clear();
 			} else {
 				System.out.println("######### still out - " + key);
 			}
 		}
 		for (String key : wereDisconnectedMulti.keySet()) {
 			if (clientIps.containsKey(key)) {
 				System.out.println("######### reconnected - " + key);
 				wereDisconnectedMulti.get(key).displayReconnectedMessage(key);
				wereDisconnectedMulti.clear();
 			} else {
 				System.out.println("######### still out - " + key);
 			}
		}		
 	}
 	
 	public Map<String, ChatPanel> getWereDisconnectedMulti() {
 		return wereDisconnectedMulti;
 	}
 
 	public void setWereDisconnectedMulti(
 			Map<String, ChatPanel> wereDisconnectedMulti) {
 		this.wereDisconnectedMulti = wereDisconnectedMulti;
 	}
 
 	public Map<String, ChatPanel> getWereDisconnectedSingle() {
 		return wereDisconnectedSingle;
 	}
 
 	public void setWereDisconnectedSingle(
 			Map<String, ChatPanel> wereDisconnectedSingle) {
 		this.wereDisconnectedSingle = wereDisconnectedSingle;
 	}
 
 	public boolean isServerUp() {
 		return serverUp;
 	}
 
 	public void setServerUp(boolean serverUp) {
 		this.serverUp = serverUp;
 	}
 
 	public void resetServerTimeout() {
 		setServerTimeoutDate(new Date());
 		serverUp = true;
 	}
 
 	public Date getServerTimeoutDate() {
 		return serverTimeoutDate;
 	}
 
 	public void setServerTimeoutDate(Date serverTimeoutDate) {
 		this.serverTimeoutDate = serverTimeoutDate;
 	}
 
 	
 	
 }
