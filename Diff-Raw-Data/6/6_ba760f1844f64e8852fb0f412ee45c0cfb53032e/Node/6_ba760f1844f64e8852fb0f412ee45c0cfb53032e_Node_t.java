 package node;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import communicate.NodeServer;
 
 public class Node {
 	
 	private static final int DEFAULT_PORT = 2010;
 	private static final String RMI_NAME = "ChatDSV";
 
 	private static NodeServer nodeServer;
 	private static Log log;
 	private static LamportsClock clock;
 	private static InetSocketAddress socket;
 	
 	public static void main(String[] args)
 	{
 		// vytvorime security managera
 		// konfigurace je specifikovana bud pomoci -Djava.security.policy=file.policy
 		if (System.getSecurityManager() == null)
 			System.setSecurityManager(new RMISecurityManager());
 		
 		clock = new LamportsClock();
 		log = new Log(clock);
 		socket = null;
 
 		processConfigurationFile(args);
 
 		interactiveMode();
 
 		log.make("Exit");
 		System.exit(0);
 	}
 	
 	private static void interactiveMode() {
 		String CurLine = "";
 
 		System.out.println("Enter a line of text (type 'quit' to exit): ");
 		InputStreamReader converter = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(converter);
 
 		while (true){
 			try {
 				CurLine = in.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			if ((CurLine.contentEquals("quit")))
 				break;
 			else
 				parsingCommand(CurLine);			
 		}
 	}
 
 	private static void processConfigurationFile(String[] args) {
 		if (args.length > 0)
 		{
 			String confFilePath = args[0];
 			System.out.println("Loading configuration file: " + confFilePath);
 
 			try {
 				FileInputStream fstream = new FileInputStream(confFilePath);
 				DataInputStream in = new DataInputStream(fstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(in));
 				String strLine;
 				// Read File Line By Line
 				while ((strLine = br.readLine()) != null) {
 					System.out.println(strLine);
 					parsingCommand(strLine);
 				}
 				in.close();
 			} catch (IOException e) {
 				System.err.println("processConfigurationFile: " + e.getMessage());
 				System.exit(1);
 			}
 		}
 	}
 
 	private static void parsingCommand(String command) {
 		String[] parts, subparts;
 		parts = command.split(" ", 2);
 		
 		if(parts.length >= 1)
 		{
 			if (parts[0].contentEquals("setsocket"))
 			{
 				subparts = parts[1].split(" ");
 				if (subparts.length >= 2)
 					socket = new InetSocketAddress(subparts[0], Integer.parseInt(subparts[1]));
 				else
 					socket = new InetSocketAddress(subparts[0], DEFAULT_PORT);
 				
 				nodeServer = createRMIRegistry(socket.getPort());
 				if (nodeServer == null)
 					System.exit(1);	
 			}
 			else if (parts[0].contentEquals("help"))
 			{
 				System.out.println("Commands:");
 				System.out.println("	setsocket <ip address> <port>");
 				System.out.println("	join <ip address> <port>");
 				System.out.println("	send <message>");
 				System.out.println("	lsnodes");
 				System.out.println("	help");
 				System.out.println("	logout");
 				System.out.println("	quit");
 			}
 			else if( socket == null )
 			{
 				System.out.println("You must at first run: setsocket <ip address> <port>");
 			}
 			else if (parts[0].contentEquals("join"))
 			{
 				subparts = parts[1].split(" ");
 				if (subparts.length >= 2)
 					joinToNetwork(subparts[0], Integer.parseInt(subparts[1]));
 				else
 					joinToNetwork(subparts[0]);
 			}
 			else if (parts[0].contentEquals("logout"))
 			{
 				logout();
 			}
 			else if (parts[0].contentEquals("send"))
 			{
 				sendMessage(parts[1]);
 			}
 			else if (parts[0].contentEquals("lsnodes"))
 			{
 				System.out.print("Nodes: ");
 				try {
 					for ( InetSocketAddress address : nodeServer.getNodes() )
 						System.out.print(address.getAddress().getCanonicalHostName() + " ");
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 				System.out.print("\n");
 			}
 			else
 			{
 				System.out.println("Unrecognized command: " + command);
 			}
 		}
 	}
 
 	private static void sendMessage(String message) {
 
 		try {
 			int logicTimeOfRequest = nodeServer.getClock().event();
 
 			nodeServer.request(logicTimeOfRequest, socket);
 
 			// vsem uzlum posle REQUEST a rovnou prijima REPLY a aktualizuje si logicky cas
 			for ( InetSocketAddress address : nodeServer.getNodes() )
 			{
 				if ( address.equals(socket) )
 					continue;
 
 				Registry registry = LocateRegistry.getRegistry(address.getAddress().getCanonicalHostName(), address.getPort());
 				NodeServer remoteNode = (NodeServer) registry.lookup(RMI_NAME);
 				int logicTimeOfReply = remoteNode.request(logicTimeOfRequest,socket);
 				clock.event(logicTimeOfReply);
 				log.make("is sending us a reply", logicTimeOfReply, address);
 			}
 
 			log.make("We are waiting for request on head of queue");
 			while(!nodeServer.isOurRequestOnHeadOfQueue());
 
 			// jsme v kriticke sekci a muzeme rozeslat nasi zpravu
 			int logicTimeOfMessage = clock.event();
 			for ( InetSocketAddress address : nodeServer.getNodes() )
 			{
 				if ( address.equals(socket) )
 					continue;
 
 				Registry registry = LocateRegistry.getRegistry(address.getAddress().getCanonicalHostName(), address.getPort());
 				NodeServer remoteNode = (NodeServer) registry.lookup(RMI_NAME);
 				remoteNode.message(message, logicTimeOfMessage, socket);
 				log.make("We are sending a message to node " + address.getAddress().getCanonicalHostName(), logicTimeOfMessage);
 			}
 
 			log.make("We sended a message '" + message + "' to all nodes", logicTimeOfMessage);
 			
 			// opustime kritickou sekci rozeslanim release
 			int logicTimeOfRelease = clock.event();
 			for ( InetSocketAddress address : nodeServer.getNodes() )
 			{
 				if ( address.equals(socket) )
 					continue;
 
 				Registry registry = LocateRegistry.getRegistry(address.getAddress().getCanonicalHostName(), address.getPort());
 				NodeServer remoteNode = (NodeServer) registry.lookup(RMI_NAME);
 				remoteNode.release(logicTimeOfRelease, socket, logicTimeOfRequest);
 				log.make("We are sending a release to node " + address.getAddress().getCanonicalHostName(), logicTimeOfRelease);
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			System.out.println("sendMessage: " + e.getMessage());
 		} catch (NotBoundException e) {
 			e.printStackTrace();
 			System.out.println("sendMessage: " + e.getMessage());
 		}
 	}
 
 	private static void logout() {
 		try {
 			int logicTimeOfLogout = nodeServer.getClock().event();
 
 			Iterator<InetSocketAddress> i = nodeServer.getNodes().iterator();
 			while(i.hasNext())
 			{
 				InetSocketAddress s = i.next();
 				if ( s.equals(socket) )
 					continue;
 
 				Registry registry = LocateRegistry.getRegistry(s.getAddress().getCanonicalHostName(), s.getPort());
 				NodeServer remoteNode = (NodeServer) registry.lookup(RMI_NAME);
 				log.make("Logging out from node "+s.getAddress().getCanonicalHostName()+":"+s.getPort(),logicTimeOfLogout);
				remoteNode.logout(logicTimeOfLogout, socket);
				nodeServer.removeNode(s);
//				i.remove();
 			}
 		}
 		catch (Exception e) {
 			System.err.println("logout: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private static void joinToNode(int logicTimeOfJoin, InetSocketAddress address) throws RemoteException, NotBoundException {
 		// vyhledani vzdaleneho objektu
 		Registry registry = LocateRegistry.getRegistry(address.getAddress().getCanonicalHostName(), address.getPort());
 		NodeServer remoteNode = (NodeServer) registry.lookup(RMI_NAME);
 		nodeServer.addNodes(remoteNode.addNode(logicTimeOfJoin, socket));
 	}
 
 	private static void joinToNetwork(String host, Integer port) {
 		int logicTimeOfJoin = clock.event();
 
 		try {
 			ArrayList<InetSocketAddress> visitedNodes = new ArrayList<InetSocketAddress>(nodeServer.getNodes());
 			InetSocketAddress firstAddress = new InetSocketAddress(host, port);
 			joinToNode(logicTimeOfJoin, firstAddress);
 			nodeServer.addNode(logicTimeOfJoin, firstAddress);
 			visitedNodes.add(firstAddress);
 
 			List<InetSocketAddress> nodeServerNodesBackup;
 			while ( !visitedNodes.containsAll(nodeServer.getNodes()) )
 			{
 				nodeServerNodesBackup = new ArrayList<InetSocketAddress>(nodeServer.getNodes());
 				for( InetSocketAddress address : nodeServerNodesBackup )
 				{
 					if (!visitedNodes.contains(address))
 					{
 						visitedNodes.add(address);
 						try {
 							nodeServer.addNode(logicTimeOfJoin, address);
 							joinToNode(logicTimeOfJoin, address);
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	private static void joinToNetwork(String host) {
 		joinToNetwork(host, DEFAULT_PORT);
 	}
 
 	public static NodeServer createRMIRegistry(int port)
 	{
 		// jmeno nasi sluzby
 		String name = "ChatDSV";
 		
 		nodeServer = new NodeServerImplementation(port, clock, log, socket);
 
 		try {
 			// vytvoreni samotneho objektu a jeho stubu
 			NodeServer stub = (NodeServer) UnicastRemoteObject.exportObject(nodeServer, 50000);
 
 			// zaregistrovani jmena u objektu 
 			Registry registry = LocateRegistry.createRegistry(2010);
 			registry.rebind(name, stub);
 			log.make("Created RMI Registry at " + socket.getAddress().getCanonicalHostName() + ":" + socket.getPort());
 		}
 		catch (Exception e) {
 			//neco je spatne :(
 			System.err.println("createRMIRegistry: " + e.getMessage());
 			e.getStackTrace();
 			return null;
 		}
 		
 		Thread testAliveNodesThread = new TestAliveNodesThread("TestAliveNodesThread", log, clock, nodeServer, socket, RMI_NAME);
 		testAliveNodesThread.start();
 		
 		return nodeServer;
 	}
 }
