 import java.net.Socket;
 
 public class ModuleFactory {
 	public static Module createConfigMod()
 	{
 		Configuration config = new Configuration();
 		config.intialConfiguration();
 		
 		return config;
 		
 	}
 
 	public static Module createLogMod(String peerID) {
 		
 		Logger log = new Logger(peerID);
 		log.intialConfiguration();
 		
 		return log;
 	}
 	
 	public static Module createCtrlMod(String peerID)
 	{
 		Controller controller = new Controller(peerID);
 		controller.intialConfiguration();
		
 		return controller;
 	}
 	
 	public static Module createServerMod(String peerID, Controller controller)
 	{
 		Server server = new Server(peerID, controller);
 		server.intialConfiguration();
 		return server;
 	}
 	
 	public static Module createPeer(Socket socket, Controller controller)
 	{
 		Peer peer = new Peer(socket, controller);
 		peer.intialConfiguration();
 		return peer;
 	}
 
 	public static Module createTopPeerMod()
 	{
 			return null;
 	}
 
	public static Module crateOptimisticMod()
 	{
 			return null;
 	}
 }
 
