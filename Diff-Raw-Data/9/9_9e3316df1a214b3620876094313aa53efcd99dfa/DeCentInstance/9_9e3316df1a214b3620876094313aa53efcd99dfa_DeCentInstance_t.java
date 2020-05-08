 package decentchat.api;
 
 import java.rmi.AlreadyBoundException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import org.apache.log4j.Logger;
 
 import decentchat.internal.NodeKey;
 import decentchat.internal.nodes.Node;
 import decentchat.internal.nodes.NodeImpl;
 import decentchat.internal.nodes.RingMaintainer;
 
 public class DeCentInstance {
 	
 	static Logger logger = Logger.getLogger(DeCentInstance.class);
 	
 	private String ip;
 	private int port;
 	private Registry reg = null;
 	private NodeImpl localNode = null;
 	private RingMaintainer maintainer = null;
 
 	/**
 	 * The {@link DeCentInstance} is the main instance the client will be 
 	 * using when using the DeCentChat protocol. It defines the main
 	 * public interface.
 	 *   
 	 */
 	public DeCentInstance() {		
 	}
 	
 	public int getPort() {
 		return this.port;
 	}
 	
 	public boolean init(String bootstrap_ip, int bootstrap_port, int registry_port) {
 		this.port = registry_port;
 		Node bootstrapNode = null;
 		try {
 			bootstrapNode = (Node)Naming.lookup("rmi://" + bootstrap_ip +":" +this.port+ "/node");
 			logger.debug("Polling ip from bootstrap node...");
 			ip = bootstrapNode.getIP();
 			logger.debug("Recieved IP:" + ip);
 			// Now init registry
 			createLocalRegistry(ip, registry_port);
 		} catch (Exception e) {
 			logger.error("Problem connecting", e);
 			return false;
 		}
 		if(ip == null || reg == null) {
 			logger.error("Problem fetching ip:" + ip);
 			return false;
 		}
 		createLocalNode(NodeKey.MAX_KEY, bootstrapNode);
 		return true;
 	}
 	
 	public boolean init(String hostname, int registry_port) {
 		this.ip = hostname;
 		this.port = registry_port;
 		createLocalRegistry(ip, registry_port);
 		createLocalNode(NodeKey.MIN_KEY, null);
 		return true;
 	}
 	
 	/**
 	 * Creates the local RMI {@link Registry}. Sets the reg field.
 	 * 
 	 * @param hostname Hostname the {@link Registry} should respond with.
 	 * @param port Port the {@link Registry} is to be created on.
 	 */
 	private void createLocalRegistry(String hostname, int port) {
 		// Set the corret rmi hostname
 		System.setProperty("java.rmi.server.hostname", hostname);
 		try {
 			// Start the registry
 			reg = LocateRegistry.createRegistry(port);
 		} catch (RemoteException e) {
 			logger.error("Problem creating the Registry!", e);
 		}
 	}
 	
 	/**
 	 * Creates the LocalNode instance and sets the {@link DeCentInstance}'s 
 	 * localNode field.
 	 * @param key Key used by this node.
 	 * @param bootstrapNode Node that is used to join the network. Set this
 	 * 		  to <code>null</code> if you want to create a new network.
 	 */
 	private void createLocalNode(NodeKey key, Node bootstrapNode) {
 		try {
 			logger.debug("Creating localNode");
 			if(bootstrapNode == null) {
 				localNode = new NodeImpl(key);
 			} else {
 				localNode = new NodeImpl(NodeKey.MIN_KEY, bootstrapNode);
 			}
 			reg.bind("node", localNode);
 			logger.debug("Starting Maintainer...");
 			maintainer = new RingMaintainer(localNode);
 			maintainer.start();
			logger.debug("Started Maintainer successfully!");
 		} catch (RemoteException e) {
 			logger.error("Error creating localnode", e);
 		} catch (AlreadyBoundException e) {
 			logger.error("Error creating localnode", e);
 		}
 	}
 	
 }
