 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright Ⓒ 2012  Mattias Andrée, Peyman Eshtiagh,
  *                   Calle Lejdbrandt, Magnus Lundberg
  *
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.network;
 
 // Blackboardclass to send messages with
 import cnt.Blackboard;
 import cnt.messages.*;
 
 // Classes needed for UPnP
 import org.teleal.cling.*;
 import org.teleal.cling.model.message.header.*;
 import org.teleal.cling.model.message.*;
 import org.teleal.cling.model.meta.*;
 import org.teleal.cling.model.types.*;
 import org.teleal.cling.model.action.*;
 import org.teleal.cling.registry.*;
 import org.teleal.cling.support.igd.*;
 import org.teleal.cling.support.igd.callback.*;
 import org.teleal.cling.support.model.*;
 import org.teleal.cling.controlpoint.*;
 import cnt.util.IGDListener;
 
 // Classes needed for TCP sockets
 import java.util.*;
 import java.io.*;
 
 // Classes needed for UDP socket
 import java.net.*;
 
 /**
 * Connection Networking layer
 *<p>
 * This class sets up the sockets to be used by the client. 
 * Provides socket sockets for higher levels of networking.
 *
 * @author Calle Lejdbrandt, <a href="callel@kth.se">callel@kth.se</a>
 */
 
 public class ConnectionNetworking
 {
 
 	/**
 	* Port tha game clients will be using
 	*/
 	public static final int PORT = 44923;
 
 	/**
 	* Default Constructor
 	* <p>
 	* The default constructor. Will try to make any kind of connection.
 	* Order of connection importance (high -&gt; low):
 	* Public TCP -&gt; Local TCP 
 	*</p>
 	* 
 	*
 	* @throws IOException Thrown in case of network errors.
 	*/
 	public ConnectionNetworking(ObjectNetworking objectNetworking)
 	{
 	
 		this.objectNetworking = objectNetworking;
 	
 		Blackboard.broadcastMessage(new SystemMessage(null, "Starting upp sockets..."));
 
 		// Check if public ip is same as internal ip
 		// The IP service only want us to check every 300 sec. So getExternalIP should make sure to execute the check only every 300 sec. Use local cache instead of constant lookup.
 		if (!getExternalIP().equals(getInternalIP()))
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Public and Local ip differ. Trying UPnP"));
 			if(createPortForward(ConnectionNetworking.PORT))
 			{
 				startTCP(ConnectionNetworking.PORT);
 			} else 
 			{
 				startLocalTCP();
 			}
 		} else
 			startTCP(ConnectionNetworking.PORT);
 	}
 	
 	/**
 	* Constructor with non-standard port
 	* <p>
 	* Constructor taking a non-standard port as port to listen on
 	*
 	* @param port port to use as listeningport
 	*
 	* @throws IOException Thrown in case of network errors.
 	*/
 	public ConnectionNetworking(int port, ObjectNetworking objectNetworking) 
 	{
 
 		this.objectNetworking = objectNetworking;
 
 		// Check if public ip is same as internal ip
 		// The IP service only want us to check every 300 sec. So getExternalIP should make sure to execute the check only every 300 sec. Use local cache instead of constant lookup.
 		if (!getExternalIP().equals(getInternalIP()))
 		{
 			if(createPortForward(port))
 			{
 				startTCP(port);
 			} else 
 			{
 				startLocalTCP();
 			}
 		} else
 			startTCP(port);
 		
 	}
 
 	/**
 	* ObjectNetworker to communicate with
 	*/
 	public final ObjectNetworking objectNetworking;
 
 	/**
 	* Public ip. Should not be final since an public ip might change at any time if dynamic
 	*/
 	public Inet4Address externalIP;
 
 	/**
 	* Local ip. Not as probable as Public IP, but same reason.
 	*/
 	public Inet4Address internalIP;
 
 	/**
 	* Flag to set wheter we can act as a server. I.e. do we have access on public ip?
 	*/
 	public boolean isServer;
 
 	/**
 	* Map of current threaded connections to use to store sockets
 	*/
 	final HashMap<Integer, Socket> sockets = new HashMap<Integer, Socket>();
 
 	/**
 	* Map of current threaded connections to use to store ObjectOutputStreams
 	*/
 	final HashMap<Integer, ObjectOutputStream> objectOutputs = new HashMap<Integer, ObjectOutputStream>();
 
 	/**
 	* Map of current threaded connections to use to store ObjectInputStremas
 	*/
 	final HashMap<Integer, ObjectInputStream> objectInputs = new HashMap<Integer, ObjectInputStream>();
 
 	/**
 	* The time when we last updated our external ip. Provider ask us not to do so more then every 5min/host.
 	*/
 	private Date lastUpdate;
 
 	/**
 	* The UPnP RemoteService being used for UPnP devices. 
 	*/
 	private UpnpService upnpService;
 
 	/**
 	* Make a TCP serversocket to listen on incoming sockets
 	*
 	* @param port Port to listen on
 	*/
 	private void startTCP(int port) 
 	{
 		ServerSocket _server = null;
 		try {
 			_server = new ServerSocket(port);
 		} catch (IOException err) 
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Error: Cannot listen to port. Port is busy. Is another game instance running?"));
 			return;
 		}
 		
 		this.isServer = true;
 		
 		final Thread serverThread = new Thread(new TCPServer(_server, this.objectNetworking, this));
 		serverThread.setDaemon(true);
 		serverThread.start();
 	}
 
 	/**
 	* Setup the client to be running locally. Without PublicIP or NAT.
 	*/
 	private void startLocalTCP()
 	{
 		this.isServer = false;
 		
 		Blackboard.broadcastMessage(new SystemMessage(null, "Running in local mode. Needs access to at least one server in cloud."));
 		
 		// Do nothing else, we can only use outgoing sockets
 	}
 	
 	/**
 	* Makes a connection to specefied IP and port
 	*
 	* @param host an Inet4Address to connect to
 	* @param port an int to use as portnumber
 	* @param peer the peer to map the connection to
 	*
 	* @return <code>Socket</code> on successfull connection. <code>null</code> otherwise.
 	*/
 	public final Socket connect(Inet4Address host, int port, int peer)
 	{
 		Blackboard.broadcastMessage(new SystemMessage(null, "Initiating connection to ["+ host + ":" + port + "]"));
 		Socket connection = null;
 		try {
 			connection = new Socket(host, port);
 		} catch (IOException err) 
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Connection Failed"));
 			return null;
 		}
 		
 		// Always save live sockets. TODO: lookup possible security issues with this.
 		this.sockets.put(peer, connection);
 		ObjectOutputStream out; 
 		try
 		{
 			out = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
 			out.flush();
 			this.objectOutputs.put(peer, out);
 			this.objectInputs.put(peer, new ObjectInputStream(new BufferedInputStream(connection.getInputStream())));
 		} catch (IOException ioe)
 		{
 			// TODO: fix this
 		}
 		
		TCPReceiver receiver = new TCPReceiver(connection, this.objectNetworking, this);
		Thread t = new Thread(receiver);
		t.start();
		
 		Blackboard.broadcastMessage(new SystemMessage(null, "Connected to ID: " + peer));
 
 		Blackboard.broadcastMessage(new SystemMessage(null, "Number of open sockets: " + this.sockets.size()));
 
 		return connection;
 	}
 	
 	/**
 	* Takes a serialized object and sends it to all known remote clients
 	*
 	* @param message Serialized object to send as message
 	*/
 	public void send(Serializable message)
 	{ 
 		
 		Blackboard.broadcastMessage(new SystemMessage(null, "Initiating send"));
 		if (this.sockets != null && this.sockets.isEmpty() == false)
 		{
 	
 			for (int peer : this.sockets.keySet())
 			{
 						
 				try
 				{
 					TCPSender _sender = new TCPSender(this.objectOutputs.get(peer), message);
 					Thread _tmpThread = new Thread(_sender);
 					_tmpThread.start();
 				
 				} catch (IOException ioe)
 				{
 					Socket dead_socket = this.sockets.get(peer);
 					this.sockets.remove(peer);	
 					Socket _socket = this.connect((Inet4Address)dead_socket.getInetAddress(), dead_socket.getPort(), peer);
 					this.sockets.put(peer, _socket);
 					ObjectOutputStream out = null;
 					try {
 						this.objectInputs.put(peer, new ObjectInputStream(new BufferedInputStream(_socket.getInputStream())));
 						out = new ObjectOutputStream(new BufferedOutputStream(_socket.getOutputStream()));
 						out.flush();
 						this.objectOutputs.put(peer, out);
 					} catch(IOException iioe)
 					{
 						// TODO: fix this
 					}
 					if (_socket != null)
 					{
 						try
 						{
 							TCPSender _sender = new TCPSender(out, message);
 							Thread _tmpThread = new Thread(_sender);
                                         	        _tmpThread.start();
 						} catch (IOException sec_ioe) 
 						{
 							//TODO: Change to correct BlackboardMessage to send a PlayerDroped message
 							Blackboard.broadcastMessage(new SystemMessage(null, "Error sending [ " + message + " ] to " + peer));
 						}
 					} else
 					{
 						//TODO: Change to correct BlackboardMessage to send a PlayerDroped message
 						Blackboard.broadcastMessage(new SystemMessage(null, "Error sending [ " + message + " ] to " + peer));
 					}
 				}
 			}
 		}
 	}
 		
 	/**
 	* Retrive the internatl IP from the local host.
 	*
 	* Sets the local ip, and also returns it as a Inet4Address object.
 	*
 	* @return internal ip
 	*/
 	private final Inet4Address getInternalIP()
 	{
 		try
 		{
 			this.internalIP = (Inet4Address)Inet4Address.getLocalHost();
 		} catch (UnknownHostException uhe)
 		{
 			this.internalIP = null;
 		}
 
 		return this.internalIP;
 	}
 	
 	/**
 	* Retrives the external ip adress by adress lookup.
 	*
 	* Lookups the external ip for the host. Uses a cache and only does lookups every 5min.
 	*
 	* @return external ip
 	*/
 	private Inet4Address getExternalIP()
 	{
 		Date current = new Date();
 		// 300'000 seconds = 5 minutes. note parantecis is more for clarity then function
 		if ((this.externalIP != null) && (this.lastUpdate != null) && (current.getTime() < (this.lastUpdate.getTime() + 300000)))
 			return this.externalIP;	
 		else
 		{
 			try {
 			        this.externalIP = (Inet4Address)Inet4Address.getByName(Toolkit.getPublicIP());
 				return this.externalIP;
 
 			} catch (Exception err)
 			{
 				this.externalIP = null;
 				Blackboard.broadcastMessage(new SystemMessage(null, "Error: Couldn't retrive external IP address:" + err));
 				return null;
 			}
 			
 		}
 	}
 
 	/**
 	* Tries to find any UPNP enabled device that is a Internet Gateway Device.
 	* If found a portforward, try to make a portforward.
 	*
 	* @param port the port to be forwarded
 	*
 	* @return returns <code>true</code> on succes, <code>false</code> other whise.
 	*/
 	private boolean createPortForward(int port)
 	{
 		// NOTE: This UPNP implementation is multithreaded. 
 		// Any Device is found asynchronously.
 
 		// monitor object to be used for the search for a device
 		Object monitor = new Object();
 
 		this.upnpService = new UpnpServiceImpl();
 
 		//The IGDListener class is custom made and resides in util
 		this.upnpService.getRegistry().addListener(new IGDListener(upnpService, monitor, port));
 		// Set the service we want to search for (makes MUCH less network congestion if network has many UPnP devices)
 		ServiceType _type = new UDAServiceType("WANIPConnection");
 
 		//Initiate a standard search
 		//this.upnpService.getControlPoint().search(new ServiceTypeHeader(_type));
 		// Testing default serach
 		this.upnpService.getControlPoint().search(new STAllHeader());
 		try
 		{
 			// Devices are discovered asynchronously, but should be faster then 5 sec.
 		        synchronized (monitor)
 			{
 			    monitor.wait(5000);
 			}
 		} catch (InterruptedException ie)
 		{
 			// {ignore and continue}
 		}
 		
 		// If we found the correct device, we have also made a PortForardd. 
 		// If no PortForward could be done, device is removed before this check. 
 		// Probably... depending on timeout and asymchronous behaviour.
 		if (this.upnpService.getRegistry().getDevices(_type).size() > 0)
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "We have " + this.upnpService.getRegistry().getDevices(_type).size() + " IGD(s) "));
 			return true;
 		} else
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "We have 0 IGDs"));
 			return false;
 		}
 	}
 	
 	/**
 	* Removes the portforwaring on any UPnP device that was discovered during startup connecting
 	*
 	*/
 	private void removePortForward()
 	{
 		// Monitor object - see createPortForward
 		Object monitor = new Object();
 
 		// RemoteService we are interested in - see createPortForward
 		ServiceType _type = new UDAServiceType("WANIPConnection");
 		
 		// Se if we actually have a UpnpService and devices
 		if (this.upnpService != null && this.upnpService.getRegistry().getDevices(_type).size() > 0)
 		{
 			// For all IGDs, remove portmappings
 			for (RemoteDevice device : (RemoteDevice[])this.upnpService.getRegistry().getDevices(_type).toArray())
 			{
 				RemoteService portMap = device.findService(_type);
 				// This retrives the IGDListener instances and executes demapPort()
 				// the iterator is needed as it's unknown what kind of collection is retrived from Cling
 				
 				IGDListener _listener = (IGDListener)this.upnpService.getRegistry().getListeners().iterator().next();
 				_listener.demapPort(portMap, this.upnpService.getRegistry(), device);
 			}
 		}
 	}	
 }
