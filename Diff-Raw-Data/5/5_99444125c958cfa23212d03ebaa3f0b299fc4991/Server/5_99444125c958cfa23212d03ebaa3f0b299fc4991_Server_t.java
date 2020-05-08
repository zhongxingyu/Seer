 //import java.net.InetAddress;
 //import java.net.UnknownHostException;
 import java.net.InetAddress;
 import java.rmi.ConnectException;
 import java.rmi.ConnectIOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UID;
 //import java.util.ArrayList;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TimerTask;
 import java.util.Timer;
 //import java.util.Scanner;
 import java.util.Map;
 
 
 public class Server extends java.rmi.server.UnicastRemoteObject implements Runnable,RmiServerInterface {
 	//RmiServerImpl rmi_svr;
 	private static final long serialVersionUID = 1L;
 	
 	
 	//for getting (string) IP address of a node based on it's UID
 	private Map<UID, RmiClientInterface> connectedNodes;
 	private Map<UID, RmiClientInterface> connectedViewers;	
 	
 	private List<UID> subscribedViewers;
 	
 	//Stores IP addresses of nodes that need to be tested
 	private List<InetAddress> importantNodes;
 	
 	
 	private class periodicGather extends TimerTask {
 		Server svr = null;
 		public periodicGather(Server s){
 			svr = s;
 		}
 		public void run(){
 			svr.gatherSnapshots();
 		}
 	}
 	
 	
 	public Server() throws RemoteException
 	{
 		super();
 		connectedNodes = new HashMap<UID, RmiClientInterface>();
 		connectedViewers = new HashMap<UID, RmiClientInterface>();
 		
 		importantNodes = new ArrayList<InetAddress>();
 		subscribedViewers = new ArrayList<UID>();
 		
 		//setup server on RMI
 		Registry registry = LocateRegistry.getRegistry();
 		registry.rebind("NetworkDiagSvr", this);
 		
 		Timer timer = new Timer();
 		timer.schedule(new periodicGather(this),new Date(), 60000);
 	}
 
 
 
 	public void run() {
 
 			
 	}
 
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws InterruptedException{
 		System.out.println("RMI Diagnostics Service\nServer Starting...");
 		Server s = null;
 		
 		try{
 			s = new Server();
 			Thread t = new Thread(s);
 			t.run();
 			
 			System.out.println("Server Started.");
 			
 			t.join();
 		}
 		catch(ConnectException ce){
 			System.err.println("Failed to connect to RMI registry.");
 			System.exit(-1);
 		}
 		catch(RemoteException re){
 			System.err.println("Failed to setup RMI.");
 			re.printStackTrace();
 			System.exit(-1);
 		}
 		
 	}
 	

	//TODO - locking required to stop new nodes/viewers being added. Causes problems when changing a list while iterating (throws error)
 	private void gatherSnapshots(){
 		List<Snapshot> allShots = new ArrayList<Snapshot>();
 		//Iterator<Snapshot> allSnaps;
 		Iterator<UID> subscribed;
 		List<UID> toRemove = new ArrayList<UID>();
 		Iterator<UID> removing = null;
 		UID current = null;
 		
 		System.out.println("************************************");
 		System.out.println("Requesting and Sending Snapshots ***");
 		System.out.println("************************************");
 		System.out.println("---- Gathering Snapshots -----");
 		
 		try {
 			Iterator<UID> allNodes = connectedNodes.keySet().iterator();
 
 			while(allNodes.hasNext()){
 				current = allNodes.next();
 				System.out.println("Requesting snapshot from node " + current + "...");
 				try{
 					allShots.add(connectedNodes.get(current).compileSnapshot());
 					System.out.println("Snapshot received.");
 				}
 				catch(ConnectException ce){
 					System.out.println("Failed to contact node with id " +current+", removed.");
					toRemove.add(current);
 				}
 				finally{
 					removing = toRemove.iterator();
 					while(removing.hasNext()){
 						current = removing.next();
 						connectedNodes.remove(current);
 					}
 				}
 			}
 			
 			System.out.println("------------------------------");
 			System.out.println("----- Sending Snapshots ------");
 			
 			subscribed = subscribedViewers.iterator();
 			
 			while(subscribed.hasNext()){
 				try{
 					current = subscribed.next();
 					connectedViewers.get(current).sendSnapshots(allShots);
 					System.out.println("Snapshots sent to subscribed Viewer with id " + current);
 				}
 				catch(ConnectException ce){
 					System.err.println("Error sending snapshots to subscribed Viewer with id "+current+", removed.");
 					toRemove.add(current);
 				}
 				catch(ConnectIOException ioe){
 					System.err.println("Error finding Viewer "+current+" while attempting to send snapshots, removed.");
 					toRemove.add(current);
 				}
 			}
 			
 		} catch (RemoteException e) {
 			System.out.println("An exception occurred: " + e.getMessage());
 		}
 		finally{
 			removing = toRemove.iterator();
 			while(removing.hasNext()){
 				current = removing.next();
 				subscribedViewers.remove(current);
 				connectedViewers.remove(current);
 			}
 		}
 		System.out.println("------------------------------");
 		System.out.println("************************************");
 		System.out.println("************************************");
 		System.out.println("************************************\n");
 		
 	}
 	
 	
 // REMOTE METHODS //	
 	
 	
 	public UID register(InetAddress ip, clientType type, RmiClientInterface client) throws RemoteException {
 		UID id = new UID(); //issue a new unique ID for the node
 		
 		switch(type){
 			case NODE: 
 				synchronized(connectedNodes){
 					connectedNodes.put(id,client);
 				}	
 				break;
 				
 			case VIEWER:
 				synchronized(connectedViewers){
 					connectedViewers.put(id,client);
 				}
 				break;
 				
 			default:
 				System.err.println("Unrecognised client type " + type + " attempted to register.");
 				return null;
 		}
 
 		System.out.println("New " + type.toString().toLowerCase() + " with IP address \"" + ip + "\" registered with UID " + id.toString());
 		return id;
 	}
 	
 
 
 	public List<InetAddress> setup(UID id, clientType type) throws RemoteException {
 		return importantNodes;
 	}
 
 
 	public void goodbye(UID id, clientType type) throws RemoteException {
 		switch(type){
 			case NODE: 
 				synchronized(connectedNodes){
 					connectedNodes.remove(id);
 				}	
 				break;
 				
 			case VIEWER:
 				synchronized(connectedViewers){
 					connectedViewers.remove(id);
 				}
 				break;
 				
 			default:
 				System.err.println("Unrecognised client type " + type + " attempted to leave.");
 				return;
 		}
 
 	
 		System.out.println(type.toString().toLowerCase() + " with UID " + id.toString() + " has disconnected.");
 		
 	}
 
 
 	public void addImportantNodes(List<InetAddress> moreNodes) throws RemoteException {
 		importantNodes.addAll(moreNodes);
 		RmiClientInterface currentAdd = null;
 		
 		//Update all connected nodes with new list
 		Iterator<RmiClientInterface> allNodes = connectedNodes.values().iterator();
 		
 		while(allNodes.hasNext()){
 			currentAdd = allNodes.next();
 			currentAdd.updateImportantNodes(importantNodes);
 		}
 		
 		System.out.println("Added important nodes: " + moreNodes.toString());
 	}
 
 
 	public void removeImportantNodes(List<InetAddress> nodesToDel) throws RemoteException {
 		importantNodes.removeAll(nodesToDel);
 		
 		//Update all connected nodes with new list
 		Iterator<RmiClientInterface> allNodes = connectedNodes.values().iterator();
 		
 		while(allNodes.hasNext()){
 			allNodes.next().updateImportantNodes(importantNodes);
 		}
 	}
 	
 
 	public boolean snapshotSubscribe(UID id, clientType type) throws RemoteException {
 		if(type == clientType.NODE)
 			return false;
 		
 		synchronized(subscribedViewers){
 			//check viewer is registered
 			if (!connectedViewers.containsKey(id)){
 				return false;
 			}
 			
 			//is registered, so add to list of subscribed Viewers
 			subscribedViewers.add(id);
 			
 			return true;
 		}
 	}
 	
 	
 }
