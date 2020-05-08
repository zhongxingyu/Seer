 package replicated_calculator;
 import point_to_point_queue.*;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.InetAddress;
 import java.io.*;
 import java.util.HashMap;
 import week6.ClientEventConnectDenied;
 
 /**
  * A simple, non-robust client for connecting to DistributedCalculator. 
  * It sends the events to the calculator and receives back the events as 
  * acknowledgements. Only the acknowledgements of read events are used
  * for anything, as they carry the value which was read. This value is 
  * reported back to the caller of the SimpleClient using a callback.
  * 
  * A fuller implementation could, e.g., handle detection of when a server
  * is down and report back to the user which commands were carried out.
  * If the server crashes the client could try to shift to another 
  * server in the server group.
  * 
  * A fuller implementation could also make the client robust such that 
  * it can handle to crash and then restart.
  * 
  * A fuller implementation could also be extended to handle a client-centric
  * semantics such as read-your-writes in case the client disconnects from one
  * server and then connects to another server.
  * 
  * @author Jesper Buus Nielsen, Aarhus University, 2012.
  *
  */
 public class ClientNonRobust extends Thread implements Client  {
     
     /*
      * The name of this client. 
      */
     protected String clientName;
     
     /*
      * Point-to-point message for sending messages to the server.
      * Used for sending ClientEvents to the server.
      */
     protected PointToPointQueueSenderEndNonRobust<ClientEvent> toServer;
     
     /*
      * Point-to-point message queue for receiving messages from the server.
      * Used for getting back client events sent on toServer. When an
      * event comes back it means that the server has handled the event.
      */
     protected PointToPointQueueReceiverEndNonRobust<ClientEvent> fromServer;
     
     /*
      * The event identifier of the next event to be sent to the server.
      */
     protected long eventID = 0;
     
     /*
      * Used for storing the callbacks which are used to report back the values 
      * of reads.
      */
     private final HashMap<Long,Callback<BigInteger>> callbacks = new HashMap<Long,Callback<BigInteger>>();
     
     /*
      * Used to signal that the SimpleClient has been disconnected. When set
      * to true, the SimpleClient will close down immediately.
      */
     private boolean shutdown = false;
     
     /**
      * Send an addition command to the server.
      */
     synchronized public void add(String left, String right, String res) {
 		toServer.put(new ClientEventAdd(clientName,eventID++,left,right,res));
     }
     
     /**
      * Send an assignment event to the server.
      */	
     synchronized public void assign(String var, BigInteger val) {
 	toServer.put(new ClientEventAssign(clientName,eventID++,var,val));
     }
     
     /**
      * Send a beginAtomic event to the server.
      */	
     synchronized public void beginAtomic() {
 	toServer.put(new ClientEventBeginAtomic(clientName,eventID++));
     }
     
     /**
      * Sends a compare event to the server.
      */
     synchronized public void compare(String left, String right, String res) {
 	toServer.put(new ClientEventCompare(clientName,eventID++,left,right,res));
     }
     
     /**
      * Connects to the server and sends a connect event to the server.
      * Opens a point-to-point queue for receiving acknowledgements from the server.
      * Then starts a thread (this) which polls the acknowledgements and treats them.
      */
     synchronized public boolean connect(String addressOfServer, String clientName) {
 	this.clientName = clientName;
 	this.toServer = new PointToPointQueueSenderEndNonRobust<ClientEvent>();
 	this.toServer.setReceiver(new InetSocketAddress(addressOfServer,Parameters.serverPortForClients));		
 	try {
 	    final String myAddress = InetAddress.getLocalHost().getCanonicalHostName();
 	    this.fromServer = new PointToPointQueueReceiverEndNonRobust<ClientEvent>();
 	    this.fromServer.listenOnPort(Parameters.clientPortForServer);
 	    toServer.put(new ClientEventConnect(clientName,eventID++,new InetSocketAddress(myAddress,Parameters.clientPortForServer)));
 	} catch (IOException e) {
 	    return false;
 	}
 	this.start();
 	return true;
     }
     
     /**
      * Sends a disconnect event to the server. Will shut down immediately. 
      * Should only be called when all events have been sent and acknowledged.
      */
     synchronized public void disconnect() {
 	toServer.put(new ClientEventDisconnect(clientName,eventID++));
 	toServer.shutdown();
 	fromServer.shutdown();
 	shutdown = true;
     }
     
     /**
      * Sends an endAtomic event to the server.
      */
     synchronized public void endAtomic() {
 	toServer.put(new ClientEventEndAtomic(clientName,eventID++));
     }
     
     /**
      * Sends a multiplication event to the server.
      */
     synchronized public void mult(String left, String right, String res) {
 	toServer.put(new ClientEventMult(clientName,eventID++,left,right,res));
     }
     
     /**
      * Sends a read event to the server. It stores the callback under
      * the event identifier so it can be called when an acknowledgement returns.
      */
     synchronized public void read(String var, Callback<BigInteger> callback) {
 	final long eid = eventID++;
 	toServer.put(new ClientEventRead(clientName,eid,var));
 	synchronized (callbacks) {
 	    callbacks.put(new Long(eid), callback);
 	}
     }
     
     /** 
      * Keeps getting the next acknowledgement from the server. If the
      * acknowledgement is a read event, then the value of the event is 
      * reported back to the creater of the read event using a callback.
      */
     public void run() {
 		while (!shutdown) {
 			final ClientEvent nextACK = fromServer.get();
 			if (nextACK instanceof ClientEventRead) {
 				ClientEventRead eventRead = (ClientEventRead)nextACK;
 				Callback<BigInteger> callback;
 				synchronized (callbacks) {
 					callback = callbacks.get(new Long(eventRead.eventID));
 				}
 				callback.result(eventRead.getVal());
 			}else if(nextACK instanceof ClientEventConnectDenied){
 				System.out.println("Username already connected...");
				disconnect();
 			}
 		}
 		toServer.shutdown();
     }
 }
