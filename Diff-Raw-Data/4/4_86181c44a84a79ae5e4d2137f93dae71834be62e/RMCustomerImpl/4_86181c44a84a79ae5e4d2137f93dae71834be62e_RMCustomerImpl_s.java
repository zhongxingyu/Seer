 package serversrc.resImpl;
 
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import serversrc.resInterface.*;
 
 import java.util.Calendar;
 import java.util.Enumeration;
 
 
 public class RMCustomerImpl extends RMBaseImpl implements RMCustomer{
 	
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         port = 1099;
 
         if (args.length == 1) {
             server = server + ":" + args[0];
             port = Integer.parseInt(args[0]);
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.RMCustomerImpl [port]");
             System.exit(1);
         }
 
         try {
             // create a new Server object
             RMCustomerImpl obj = new RMCustomerImpl();
             // dynamically generate the stub (client proxy)
             RMCustomer rm = (RMCustomer) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group2RMCustomer", rm);
 
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
         	Customer c = (Customer) m_itemHT.get(key);
 			if (c != null)
 				return new Customer(c);
 			return null;
         }
     }
 	
 	public RMCustomerImpl() throws RemoteException {
 		
 	}
 	
 	@Override
 	public int newCustomer(int id) throws RemoteException {
 		
 		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
         // Generate a globally unique ID for the new customer
         int cid = Integer.parseInt( String.valueOf(id) +
                                 String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                 String.valueOf( Math.round( Math.random() * 100 + 1 )));
         Customer cust = new Customer( cid );
         writeData( id, cust.getKey(), cust );
         Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
         return cid;
 	}
 	
 	@Override
 	public boolean newCustomer(int id, int cid) throws RemoteException, TransactionAbortedException {
 	    {
 	    	Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") called" );
 	        Customer cust = (Customer) readData( id, Customer.getKey(cid) );
 	        if ( cust == null ) {
 	            cust = new Customer(cid);
 	            writeData( id, cust.getKey(), cust );
 	            Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") created a new customer" );
 	            return true;
 	        } else {
 	            Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") failed--customer already exists");
 	            throw new TransactionAbortedException(id);
 	        } // else
 		}
 	}
 
 	@Override
 	/**
 	 * Must remove all reserved items of that customer as well. 
 	 */
 	public RMHashtable deleteCustomer(int id, int customerID) throws RemoteException, TransactionAbortedException {
 		
 		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
 		if ( cust == null ) {
 			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
 			throw new TransactionAbortedException(id);
 		}
 		RMHashtable reservationHT = null;          
 		// Increase the reserved numbers of all reservable items which the customer reserved. 
 		reservationHT = cust.getReservations();
 
 		// remove the customer from the storage
 		removeData(id, cust.getKey());
 		cust.setDeleted(true);
 
 		return reservationHT;
 
 	}
 
     // Returns data structure containing customer reservation info. Returns null if the
     //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
     //  reservations.
     public RMHashtable getCustomerReservations(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if ( cust == null ) {
             Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             throw new TransactionAbortedException(id);
         } else {
             return cust.getReservations();
         } // if
     }
 
 	public Customer getCustomer(int id, int customerID)
 		throws RemoteException {
 		return (Customer) readData( id, Customer.getKey(customerID) );
 	}
 
     // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if ( cust == null ) {
             Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             throw new TransactionAbortedException(id);
         } else {
                 String s = cust.printBill();
                 Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                 System.out.println( s );
                 return s;
         } // if
     }
 
     public ReservedItem reserve(int id, int cid, String key, String location, int price, ReservedItem.rType rtype)
     		throws RemoteException, TransactionAbortedException {
     	Customer cust = (Customer) readData(id, Customer.getKey(cid));
     	if (cust == null)
     		throw new TransactionAbortedException(id);
     	if (cust.isDeleted())
     		throw new TransactionAbortedException(id);
    	return cust.reserve(key, location, price, rtype);
     }
 
     public boolean unreserve(int id, int cid, ReservedItem item)
     		throws RemoteException, TransactionAbortedException {
     	Customer cust = (Customer) readData(id, Customer.getKey(cid));
     	if (cust == null)
     		throw new TransactionAbortedException(id);
     	if (cust.isDeleted()) 
     		throw new TransactionAbortedException(id);
     	cust.unreserve(item.getKey());
     	return true;
     }
 
     public boolean shutdown() throws RemoteException {
     	System.out.println("quit");
     	Registry registry = LocateRegistry.getRegistry(port);
     	try {
     		registry.unbind("Group2RMCustomer");
     		UnicastRemoteObject.unexportObject(this, false);
     	} catch (NotBoundException e) {
     		throw new RemoteException("Could not unregister service, quiting anyway", e);
     	}
 
     	new Thread() {
     		@Override
     		public void run() {
     			Trace.info("Shutting down...");
     			try {
     				sleep(2000);
     			} catch (InterruptedException e) {
     				// I don't care
     			}
     			Trace.info("done");
     			System.exit(0);
     		}
 
     	}.start();
     	return true;
     }
 
     
 }
