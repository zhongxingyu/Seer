 package serversrc.resImpl;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import serversrc.resInterface.*;
 
 
 public class RMHotelImpl extends RMBaseImpl implements RMHotel {
 
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         int port = 1099;
 
         if (args.length == 1) {
             server = server + ":" + args[0];
             port = Integer.parseInt(args[0]);
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.RMHotelImpl [port]");
             System.exit(1);
         }
 
         try {
             // create a new Server object
             RMHotelImpl obj = new RMHotelImpl();
             // dynamically generate the stub (client proxy)
             RMHotel rm = (RMHotel) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group2RMHotel", rm);
 
             System.err.println("Server ready");
         } catch (Exception e) {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
 
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
     }
 	
     protected RMItem readData( int id, String key )
     {
     	synchronized(m_transactionHT){
     		RMHashtable trHT = (RMHashtable) m_transactionHT.get(id);
     		if(trHT != null){
 				RMItem item = (RMItem) trHT.get(key);
 				if(item != null)
 					return item;
 			}
     	}
         synchronized(m_itemHT) {
        	Hotel c = (Hotel) m_itemHT.get(key);
			if (c != null)
				return new Hotel(c);
			return null;
         }
     }
 	
 	public RMHotelImpl() throws RemoteException {
 	
 	}
 	
     // Create a new room location or add rooms to an existing location
     //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
     public boolean addRooms(int id, String location, int count, int price)
         throws RemoteException
     {
         Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
         Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
         if ( curObj == null ) {
             // doesn't exist...add it
             Hotel newObj = new Hotel( location, count, price );
             writeData( id, newObj.getKey(), newObj );
             Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
         } else {
             // add count to existing object and update price...
             curObj.setCount( curObj.getCount() + count );
             if ( price > 0 ) {
                 curObj.setPrice( price );
             } // if
             writeData( id, curObj.getKey(), curObj );
             Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
         } // else
         return(true);
     }
     
     // Delete rooms from a location
     public boolean deleteRooms(int id, String location)
         throws RemoteException, TransactionAbortedException {
         return deleteItem(id, Hotel.getKey(location));
         
     }
 
     // Returns the number of rooms available at a location
     public int queryRooms(int id, String location)
         throws RemoteException
     {
         return queryNum(id, Hotel.getKey(location));
     }
     
     
     // Returns room price at this location
     public int queryRoomsPrice(int id, String location)
         throws RemoteException
     {
         return queryPrice(id, Hotel.getKey(location));
     }
 
 
 }
