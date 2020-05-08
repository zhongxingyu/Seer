 package net.visualcoding.ts3serverquery;
 
 import net.visualcoding.ts3serverquery.event.TS3ClientMovedEvent;
 import net.visualcoding.ts3serverquery.event.TS3ClientConnectedEvent;
 import net.visualcoding.ts3serverquery.event.TS3ClientDisconnectedEvent;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.io.IOException;
 
 /**
  * This thread polls the TS3 server to find newly connected clients,
  * disconnected clients, and clients that have moved channels.
  * <p>
  * This thread sends a {@code clientlist} command to the TS3 server in order to
  * determine if clients have connected, disconnected, or moved.
  * <p>
  * The TS3 {@code channel} notification only notifies the the query client of
  * events from a single channel. In order to receive client moved events from
  * all channels, a polling mechanism is required.
  *
  * @author Aldehir Rojas
  * @version 1.0
  */
 public class TS3PollingThread extends Thread {
 
     /** Default latency of the polling thread. */
     private static final int DEFAULT_LATENCY = 500;
 
     /**
      * Reference to the server query client that is to receive event
      * notifications.
      */
     private TS3ServerQueryClient serverQuery;
 
     /** Polling latency. Time in milliseconds before polling again. */
     private int latency;
 
     /**
      * Map of the last known clients and their associated information.
      */
     private Map<Integer, ClientInfo> map = null;
 
     /**
      * Constructs the thread with the specified server query client and the
      * default latency of {@value #DEFAULT_LATENCY}.
      *
      * @param serverQuery server query client that is to be notified of events
      */
     public TS3PollingThread(TS3ServerQueryClient serverQuery) {
         this(serverQuery, DEFAULT_LATENCY);
     }
 
     /**
      * Constructs the thread with the specified server query client and polling
      * latency.
     *
      * @param serverQuery server query client that is to be notified of events
      * @param latency     time in milliseconds between polls
      */
     public TS3PollingThread(TS3ServerQueryClient serverQuery, int latency) {
         this.serverQuery = serverQuery;
         this.latency = latency;
     }
 
     /**
      * Polling thread execution.
      */
     public void run() {
         try {
 
             // Loop indefinitely inside the try-catch so the thread terminates
             // immediately if a) the socket is closed or b) the thread is
             // interrupted.
 
             while(true) {
                 // Get the current clients as a map
                 Map<Integer, ClientInfo> currentClients = getCurrentClients();
 
                 // Process clients
                 processClients(currentClients);
 
                 // Re-assign map
                 map = currentClients;
 
                 // Wait a bit before polling again
                 Thread.sleep(latency);
             }
 
         } catch(IOException e) {
             /** @todo Implement */
             System.err.println("IO Exception");
         } catch(InterruptedException e) {
             /** @todo Implement */
             System.err.println("Interruped Exception");
         }
     }
 
     /**
      * Returns a map of clients where the client id is the key and the
      * associated value is a ClientInfo object.
      *
      * @return a map of clients where the client id is the key and the
      *         associated value is a ClientInfo object.
      */
     final protected Map<Integer, ClientInfo> getCurrentClients()
             throws InterruptedException, IOException {
 
         // Send `clientlist -uid` to get a list of all clients with their uids
         TS3Result result = serverQuery.execute("clientlist -uid");
 
         // Create a map to store the clients
         Map<Integer, ClientInfo> clients;
         clients = new HashMap<Integer, ClientInfo>(151);
 
         // Loop through all of the clients
         for(TS3Map item : result.getItems()) {
             // Instantiate a new ClientInfo object to store client information
             ClientInfo info = new ClientInfo();
 
             info.clientId = item.getInteger("clid").intValue();
             info.clientName = item.get("client_nickname");
             info.clientUid = item.get("client_unique_identifier");
             info.channelId = item.getInteger("cid").intValue();
 
             // Add to the map
             clients.put(info.clientId, info);
         }
 
         return clients;
     }
 
     /**
      * Sends client connected events to the server query client associated with
      * this polling thread.
      *
      * @param connectedClients map of the current clients
      */
     protected void processClients(Map<Integer, ClientInfo> connectedClients) {
         // Don't process if previous map is null, i.e. not yet initialized
         if(map == null) return;
 
         // Loop through all of the clients in the previous mapping
         for(ClientInfo client : map.values()) {
 
             // Send an event notification if the client does not exist in the
             // map of currently connected clients
             if(!connectedClients.containsKey(client.clientId)) {
                 serverQuery.notify(new TS3ClientDisconnectedEvent(
                     client.clientName, client.clientId, client.clientUid));
             }
         }
 
         // Loop through all of the clients in the current map
         for(ClientInfo client : connectedClients.values()) {
 
             // Get the ClientInfo object in the previous map
             ClientInfo previous = map.get(client.clientId);
 
             if(previous == null) {
                 // Send an event notification if the client does not exist in
                 // the previous map of connected clients
                 serverQuery.notify(new TS3ClientConnectedEvent(
                         client.clientName, client.clientId, client.clientUid));
             } else {
                 // Check if the client moved channels
                 if(client.channelId != previous.channelId) {
 
                     // Create the event object
                     TS3ClientMovedEvent event = new TS3ClientMovedEvent(
                             client.clientName, client.clientId,
                             client.clientUid);
 
                     event.setSource(previous.channelId);
                     event.setDestination(client.channelId);
 
                     // Send notification
                     serverQuery.notify(event);
                 }
             }
         }
     }
 
     /**
      * Basic structure for client information.
      */
     protected class ClientInfo {
         /** Name of the client. */
         protected String clientName;

         /** Id of the client. */
         protected int clientId;
 
         /** Unique id of the client. */
         protected String clientUid;
 
         /** Channel id of the channel the client is currently in. */
         protected int channelId;
 
         /**
          * Constructs a ClientInfo object initializing all string fields to null
          * and int fields to 0.
          */
         public ClientInfo() {
             this(null, 0, null, 0);
         }
 
         /**
          * Constructs a ClientInfo object initializing all fields.
          *
          * @param clientName name of the client
          * @param clientId   id of the client
          * @param clientUid  unique id of the client
          * @param channelId  channel id of the channel the client is in
          */
        public ClientInfo(String clientName, int clientId, String clientUid,
                int channelId) {
             this.clientName = clientName;
             this.clientId = clientId;
             this.clientUid = clientUid;
             this.channelId = channelId;
         }
     }
 }
