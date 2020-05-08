 // -------------------------------
 // adapated from Kevin T. Manley
 // CSE 593
 //
 package ResImpl;
 
 import ResInterface.*;
 import LockManager.*;
 
 import java.util.*;
 import java.rmi.*;
 import java.io.*;
 import java.net.*;
 
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 //public class Middleware extends java.rmi.server.UnicastRemoteObject
 public class Middleware
 implements MiddleWare {
 
     protected RMHashtable m_itemHT = new RMHashtable();
 
     static RMCluster rmc = new RMCluster();
     static RMCluster rmp = new RMCluster();
     static RMCluster rmh = new RMCluster();
     static RMCluster rmall = new RMCluster();
     // static TransactionManager tm = null;
     static TransactionManagerManager tmm = null;
     private Shutdown s = new Shutdown();
 
     private static ResourceManagerAcceptor friends;
 
     public static void main(String args[]) {
         // Figure out where server is running
         String server_c = "skinner.cs.mcgill.ca";
         String server_p = server_c;
         String server_h = server_c;
         int port = 9988;
         int portc = 9898;
         int portp = portc;
         int porth = portc;
 
        
         // tm = new TransactionManager();
         tmm = new TransactionManagerManager();
         // TODO CHANGE PORT and SREVER PARSING
         if (args.length == 7)  // mw_port, (server, port) * 3
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[3];
             server_h = args[5];
             portc = Integer.parseInt(args[2]);
             portp = Integer.parseInt(args[4]);
             porth = Integer.parseInt(args[6]);
         }
         else if (args.length == 5) // mw_port, allserver, (port, ) * 3
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[1];
             server_h = args[1];
             portc = Integer.parseInt(args[2]);
             portp = Integer.parseInt(args[3]);
             porth = Integer.parseInt(args[4]);
         }
         else if (args.length == 2) // mw_port, allserver
         {
             port = Integer.parseInt(args[0]);
             server_c = args[1];
             server_p = args[1];
             server_h = args[1];
         }
         else if (args.length == 1)
             port = Integer.parseInt(args[0]);
         else if (args.length != 0) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.Middleware");
             System.out.println("  OR : java ResImpl.Middleware middleware_port");
             System.out.println("  OR : java ResImpl.Middleware mw_port rm_server");
             System.out.println("  OR : java ResImpl.Middleware mw_port rm_server carport hotelport flightport");
             System.out.println("  OR : java ResImpl.Middleware mw_port carserver carport hotelserver hotelport flightserver flightport");
             System.exit(1);
         }
 
         friends = new ResourceManagerAcceptor();
         friends.start();
 
         rmall.add(rmc);
         rmall.add(rmp);
         rmall.add(rmh);
 
         try {
             Middleware obj = new Middleware();
             MiddleWare rm = (MiddleWare) UnicastRemoteObject.exportObject(obj, 0);
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group13Middleware", rm);
             System.err.println("Server ready");
         }
         catch (Exception e)
         {
             System.err.println("Middleware exception: ");
             e.printStackTrace();
         }
 
 
         /*
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
         */
     }
 
     public Middleware() throws RemoteException {
     }
 
 
     // TODO remove these methods as they will not be called here
     // where these are all the private and many of the protected methods
 
     // Reads a data item
     private RMItem readData( int id, String key )
     {
         synchronized(m_itemHT){
             return (RMItem) m_itemHT.get(key);
         }
     }
 
     // Writes a data item
     private void writeData( int id, String key, RMItem value )
     {
         synchronized(m_itemHT){
             m_itemHT.put(key, value);
         }
     }
 
     // Remove the item out of storage
     protected RMItem removeData(int id, String key){
         synchronized(m_itemHT){
             return (RMItem)m_itemHT.remove(key);
         }
     }
 
 
     // deletes the entire item
     public boolean deleteItem(int id, String key)
     {
         Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key );
         // Check if there is such an item in the storage
         if( curObj == null ) {
             Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
             return false;
         } else {
             if(curObj.getReserved()==0){
                 removeData(id, curObj.getKey());
                 Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                 return true;
             }
             else{
                 Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                 return false;
             }
         } // if
     }
 
 
     // query the number of available seats/rooms/cars
     public int queryNum(int id, String key) {
         Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key);
         int value = 0;
         if( curObj != null ) {
             value = curObj.getCount();
         } // else
         Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
         return value;
     }
 
     // query the price of an item
     public int queryPrice(int id, String key){
         return 0;
     }
 
     public void decrementItem(int id, String key) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         curObj.setCount(curObj.getCount() - 1);
         curObj.setReserved(curObj.getReserved() + 1);
     }
 
     public void incrementItem(int id, String key, int change) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         curObj.setCount(curObj.getCount() + change);
         curObj.setReserved(curObj.getReserved() - change);
     }
 
 
 
     // reserve an item
     protected boolean reserveItem(int id, int customerID, String key, String location)
     {
         Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         }
 
         // check if the item is available
         ReservableItem item = (ReservableItem)readData(id, key);
         if(item==null){
             Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         }else if(item.getCount()==0){
             Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
             return false;
         }else{
             cust.reserve( key, location, item.getPrice());
             writeData( id, cust.getKey(), cust );
 
             // decrease the number of available items in the storage
             item.setCount(item.getCount() - 1);
             item.setReserved(item.getReserved()+1);
 
             Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }
     }
 
     // Create a new flight, or add seats to existing flight
     //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
     public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Flight.getKey(flightNum), LockManager.WRITE, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[addFlight] Deadlock");
         }
         return rmp.addFlight(id, flightNum, flightSeats, flightPrice);
     }
 
 
 
     public boolean deleteFlight(int id, int flightNum)
         throws RemoteException, TransactionAbortedException
     {
         String key = Flight.getKey(flightNum);
         try{
             tmm.lock(id, key, LockManager.WRITE, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[deleteFlight] Deadlock");
         }
         return rmp.deleteFlight(id, flightNum);
     }
 
 
 
     // Create a new room location or add rooms to an existing location
     //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
     public boolean addRooms(int id, String location, int count, int price)
         throws RemoteException, TransactionAbortedException
     {
         String key = Hotel.getKey(location);
         try{
             tmm.lock(id, key, LockManager.WRITE, rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[addRooms] Deadlock");
         }
         return rmh.addRooms(id, location, count, price);
     }
 
     // Delete rooms from a location
     public boolean deleteRooms(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Hotel.getKey(location);
         try{
             tmm.lock(id, key, LockManager.WRITE, rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[deleteRooms] Deadlock");
         }
         return rmh.deleteRooms(id, location);
     }
 
     // Create a new car location or add cars to an existing location
     //  NOTE: if price <= 0 and the location already exists, it maintains its current price
     public boolean addCars(int id, String location, int count, int price)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Car.getKey(location), LockManager.WRITE, rmc);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[addCars] Deadlock");
         }
         return rmc.addCars(id, location, count, price);
     }
 
 
     // Delete cars from a location
     public boolean deleteCars(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Car.getKey(location);
         try{
             tmm.lock(id, key, LockManager.WRITE, rmc);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[deleteCars] Deadlock");
         }
         return rmc.deleteCars(id, location);
     }
 
 
 
     // Returns the number of empty seats on this flight
     public int queryFlight(int id, int flightNum)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Flight.getKey(flightNum), LockManager.READ, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryFlight] Deadlock");
         }
         return rmp.queryFlight(id, flightNum);
     }
 
     // Returns price of this flight
     public int queryFlightPrice(int id, int flightNum )
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Flight.getKey(flightNum), LockManager.READ, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryFlightPrice] Deadlock");
         }
         return rmp.queryFlightPrice(id, flightNum);
     }
 
 
     // Returns the number of rooms available at a location
     public int queryRooms(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Hotel.getKey(location), LockManager.READ, rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryRooms] Deadlock");
         }
         return rmh.queryRooms(id, location);
     }
 
 
     // Returns room price at this location
     public int queryRoomsPrice(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Hotel.getKey(location), LockManager.READ,rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryRoomsPrice] Deadlock");
         }
         return rmh.queryRoomsPrice(id, location);
     }
 
 
     // Returns the number of cars available at a location
     public int queryCars(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Car.getKey(location), LockManager.READ, rmc);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryCars] Deadlock");
         }
         return rmc.queryCars(id, location);
     }
 
 
     // Returns price of cars at this location
     public int queryCarsPrice(int id, String location)
         throws RemoteException, TransactionAbortedException
     {
         try{
             tmm.lock(id, Car.getKey(location), LockManager.READ, rmc);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryCarsPrice] Deadlock");
         }
         return rmc.queryCarsPrice(id, location);
     }
 
     // Returns data structure containing customer reservation info. Returns null if the
     //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
     //  reservations.
     public RMHashtable getCustomerReservations(int id, int customerID)
         throws RemoteException
     {
         return null;
     }
 
     // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
         try{
             tmm.lock(id, Customer.getKey(customerID), LockManager.READ, rmall);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[queryCustomerInfo] Deadlock (read customer)");
         }
         RMHashtable reservationHT = rmall.getCustomerReservations(id, customerID);
         if (reservationHT == null)
         {   // TODO: can unlock too
             return "";
         }
         for(Enumeration e = reservationHT.keys(); e.hasMoreElements();)
         {
             String reservedkey = (String) (e.nextElement());
             try{
                 tmm.lock(id, reservedkey, LockManager.READ, rmall);
             } catch (DeadlockException d)
             {
                 tmm.abort(id);
                 throw new TransactionAbortedException(id, "[queryCustomerInfo] Deadlock (read customer reservation)");
             }
         }
         Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
         System.out.println( s );
         return rmall.queryCustomerInfo(id, customerID);
     }
 
     // customer functions
     // new customer just returns a unique customer identifier
 
     public int newCustomer(int id)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("INFO: RM::newCustomer(" + id + ") called" );
         int cid = Integer.parseInt( String.valueOf(id) +
             String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
             String.valueOf( Math.round( Math.random() * 100 + 1 )));
         try{
             tmm.lock(id, Customer.getKey(cid), LockManager.WRITE, rmall);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[newCustomer] Deadlock");
         }
         rmall.newCustomer(id, cid);
         return cid;
     }
 
     // I opted to pass in customerID instead. This makes testing easier
     public boolean newCustomer(int id, int customerID )
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
         try{
             tmm.lock(id, Customer.getKey(customerID), LockManager.WRITE, rmall);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[newCustomer] Write Deadlock");
         }
         return rmall.newCustomer(id, customerID);
     }
 
 
     // Deletes customer from the database.
     public boolean deleteCustomer(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
         try{
             tmm.lock(id, Customer.getKey(customerID), LockManager.WRITE, rmall);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[deleteCustomer] Write Deadlock");
         }
         RMHashtable reservationHT = rmall.getCustomerReservations(id, customerID);
         if (reservationHT == null)
         {   // TODO: can unlock too
             return false;
         }
         for(Enumeration e = reservationHT.keys(); e.hasMoreElements();)
         {
             String reservedkey = (String) (e.nextElement());
             try{
                 // TODO: make him sendto again.
                 // fix it!
                 tmm.lock(id, reservedkey, LockManager.WRITE, rmall);
             } catch (DeadlockException d)
             {
                 tmm.abort(id);
                 throw new TransactionAbortedException(id, "[deleteCustomer] Write Item Deadlock");
             }
         }
         return rmall.deleteCustomer(id, customerID);
     }
 
     // Adds car reservation to this customer.
     public boolean reserveCar(int id, int customerID, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Car.getKey(location);
         Trace.info("RM::reserveCar( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
         try{
             tmm.lock(id, Customer.getKey(customerID), LockManager.WRITE, rmall);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveCar] Write Customer Deadlock");
         }
         try{
             tmm.lock(id, key, LockManager.WRITE, rmc);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveCar] Write Car Deadlock");
         }
         // TODO: if this is false you may unlock
         return rmc.reserveCar(id, customerID, location);
     }
 
 
     // Adds room reservation to this customer.
     public boolean reserveRoom(int id, int customerID, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Hotel.getKey(location);
         Trace.info("RM::reserveHotel( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
         try{
            tmm.lock(id, Customer.getKey(customerID), LockManager.WRITE, rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveRoom] Write Customer Deadlock");
         }
         try{
             tmm.lock(id, key, LockManager.WRITE, rmh);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveRoom] Write Room Deadlock");
         }
         // TODO: if this is false you may unlock
         return rmh.reserveRoom(id, customerID, location);
     }
 
     // Adds flight reservation to this customer.
     public boolean reserveFlight(int id, int customerID, int flightNum)
         throws RemoteException, TransactionAbortedException
     {
         String key = Flight.getKey(flightNum);
         Trace.info("RM::reservePlane( " + id + ", customer=" + customerID + ", " +key+ ", "+flightNum+" ) called" );
         try{
            tmm.lock(id, Customer.getKey(customerID), LockManager.WRITE, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveFlight] Write Customer Deadlock");
         }
         try{
             tmm.lock(id, key, LockManager.WRITE, rmp);
         } catch (DeadlockException d)
         {
             tmm.abort(id);
             throw new TransactionAbortedException(id, "[reserveFlight] Write Flight Deadlock");
         }
         return rmp.reserveFlight(id, customerID, flightNum);
     }
 
     /* reserve an itinerary */
     public boolean itinerary(int id, int customer,Vector flightNumbers,String location,boolean car,boolean room)
         throws RemoteException, TransactionAbortedException
     {
         ListIterator itr = flightNumbers.listIterator();
         while (itr.hasNext()) {
             reserveFlight(id, customer, Integer.parseInt(String.valueOf(itr.next())));
         }
         if (car)
             reserveCar(id, customer, location);
 
         if (room)
             reserveRoom(id, customer, location);
 
         return true;
     }
 
     public int start() throws RemoteException
         {return tmm.start();}
 
     public boolean commit(int trxnId) throws RemoteException
     {
         // tm.commit(trxnId);
         return true;
     }
     public void abort(int trxnId) throws RemoteException
     {   // TM's abort
         ;
     }
 
     public void enlist(int trxnId) throws RemoteException
     {
         // tm.start(trxnId);
     }
 
     public void clientAbort(int trxnId) throws RemoteException
     {   // TMM's abort
         tmm.abort(trxnId);
     }
 
     public void clientCommit(int trxnId) throws RemoteException, TransactionAbortedException
     {
         tmm.commit(trxnId);
     }
 
     public boolean shutdown() throws RemoteException
     {
         tmm.shutdown();
         rmall.shutdown();
         s.start();
         return true;
     }
 }
 
 class ResourceManagerAcceptor extends Thread
 {
     Registry registry;
     public ResourceManagerAcceptor()
     {}
 
     public void run()
     {
         ServerSocket welcomeSocket = null;
         try {
             welcomeSocket = new ServerSocket(8085);
         } catch (IOException e) {
             System.out.println("disaster struck while creating accepting server:");
             e.printStackTrace();
         }
 
         while(true)
         {
             String message[] = null;
             String server = null;
             int port = 0;
             String clientName = null;
             ResourceManager guy = null;
             try {
                 Socket connectionSocket = welcomeSocket.accept();
                 BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 message = inFromClient.readLine().split(",");
                 System.out.println("Received: " + message.toString() + "end");
 
                 clientName = message[0];
                 port = Integer.parseInt(message[1]);
                 server = connectionSocket.getInetAddress().getHostName();
 
                 Registry registry = LocateRegistry.getRegistry(server, port);
 
                 guy = (ResourceManager) registry.lookup(clientName);
             } catch (IOException e) {
                 System.out.println("disaster struck while accepting a message from an rm:");
                 e.printStackTrace();
                 continue;
             } catch (NotBoundException e) {
                 System.out.println("disaster struck while accepting a message from an rm:");
                 e.printStackTrace();
                 continue;
             }
             if (clientName.contains("Car"))
                 Middleware.rmc.add(guy);
             else if (clientName.contains("Plane"))
                 Middleware.rmp.add(guy);
             else if (clientName.contains("Hotel"))
                 Middleware.rmh.add(guy);
             else
                 System.out.println("confusing message: " + clientName);
             System.out.println("added RM: " + clientName);
         
         }
     }
 }
