 package server;
 
 
 import java.rmi.AccessException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Vector;
 
 
 /**
  * This class manages the available ChatServers and available rooms.
  * In a first time, you should not modify its functionalities but only export them for being called by the ChatClient.
  * In a second time, you will modify this to allow creating new rooms and looking them up from the client.
  *
  */
 public class ChatServerManager implements ChatServerManagerInterface{
 
 	private Vector<String> chatRoomsList;
 	
 	private Vector<ChatServer> chatRooms;
 	
 	private Registry registry;
 		
 	/**
 	 * Constructor of the ChatServerManager.
 	 * Must export its functionalities to be called from RMI by the client.
 	 */
 	
 	public ChatServerManager () {
 		chatRoomsList = new Vector<String>();
 		chatRooms = new Vector<ChatServer>();
 		// initial: we create a single chat room and the corresponding ChatServer
 		chatRooms.add(new ChatServer("sports"));
 		chatRoomsList.add("sports");
 		
 		try {
 			ChatServerManagerInterface stub = (ChatServerManagerInterface)UnicastRemoteObject.exportObject(this,0);
 			registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
 		} catch (AccessException e) {
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} 
 		
 		
 	}
 
 	public Vector<String> getRoomsList() {
 		return chatRoomsList;
 	}
 
 	public boolean createRoom(String roomName) {
 		if(chatRooms.contains(roomName)) {
 			return false;
 		}
 		chatRooms.add(new ChatServer(roomName));
 		chatRoomsList.add(roomName);
 		return true;
 		
 	}	
 	
 	public static void main(String[] args) {
 		// TODO main method needs to be done.
 		try {
 			System.setProperty("java.security.policy", "security");
 			System.setProperty("java.rmi.server.hostname", "130.125.12.229");
 			if (System.getSecurityManager() == null) {
 				System.setSecurityManager(new RMISecurityManager());
 			}
 			LocateRegistry.createRegistry(1099);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		ChatServerManager cm = new ChatServerManager();
 		System.out.println("ChatServerManager running successfully...");
 	}
 	
 }
