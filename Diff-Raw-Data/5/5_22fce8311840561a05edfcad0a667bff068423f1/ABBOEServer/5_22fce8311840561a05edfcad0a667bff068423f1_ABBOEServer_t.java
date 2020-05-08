 package biomine3000.objects;
 
 import static biomine3000.objects.BusinessObjectEventType.*;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 
 
 import util.CmdLineArgs2;
 import util.DateUtils;
 import util.StringUtils;
 import util.dbg.ILogger;
 import util.dbg.Logger;
 import util.net.NonBlockingSender;
 
 /**
  * Advanced Business Objects Exchange Server.
  * 
  * Reads business objects from each client, broadcasting back everything it reads
  * to all clients.
  * 
  * Two dedicated threads will created for each client, one for sending and one for reading {@link
  * BusinessObject}s.  
  * 
  * Once a client closes its sockets outputstream (the inputstream of the server's socket),
  * the server stops sending to that client and closes the socket. 
  *
  */
 public class ABBOEServer {   
    
     private static ILogger log = new Logger.ILoggerAdapter(null, new DateUtils.BMZGenerator());
     
     public static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
         
     private ServerSocket serverSocket;    
     private int serverPort;
     
     /** all access to this client list should be synchronized on the ABBOEServer instance */
     private List<Client> clients;
     
     private State state;
     
     private synchronized List<String> clientReport(Client you) {
         List<String> result = new ArrayList<String>();
         for (Client client: clients) {
             if (client == you) {
                 result.add(client.name+" (you)");
             }
             else {
                 result.add(client.name);
             }
         }
         return result;
     }
     
     /** Create server data structures and start listening */
     public ABBOEServer(int port) throws IOException {
         state = State.NOT_RUNNING;
         this.serverPort = port;
         serverSocket = new ServerSocket(serverPort);        
         clients = new ArrayList<Client>();
         log("Listening.");
     }                            
     
     /**
      * Send an object to all applicable clients. Does not block, as sending is done
      * using a dedicated thread for each client.
      */
     private synchronized void sendToAllClients(Client src, BusinessObject bo) {
         // defer constructing send bytes to time when sending to first applicable client (there might be none) 
         byte[] bytes = null;
         for (Client client: clients) {
             if (client.shouldSend(src, bo)) {
                 if (bytes == null) {
                     bytes = bo.bytes();
                 }
                 client.send(bytes);               
             }
         }
     }          
     
     /** 
      * Should never return. Only way to exit is through client request "stop",
      * {@link UnrecoverableServerException}, or stop signal.
      */
     private void mainLoop() {         
                          
         state = State.ACCEPTING_CLIENTS;
         
         while (state == State.ACCEPTING_CLIENTS) {
             log("Waiting for client...");
             
             try {
                 Socket clientSocket = serverSocket.accept();
                 if (state == State.ACCEPTING_CLIENTS) {
                     log("Client connected from "+clientSocket.getRemoteSocketAddress());
                     acceptSingleClient(clientSocket);                    
                 }
                 else {
                     // while waiting, someone seems to have changed our policy to "not accepting clients any more"
                     // TODO: more graceful way of rejecting this connection
                     log("Not accepting client from: "+clientSocket.getRemoteSocketAddress());
                     clientSocket.close();
                 }
             } 
             catch (IOException e) {
                 if (state == State.ACCEPTING_CLIENTS) {
                     error("Accepting a client failed", e);
                 }
                 // else we are shutting down, and failure is to be expected to result from server socket having been closed 
             }                                                            
         }                                        
         
         log.info("Finished ABBOE main loop");
     }
             
     /** Actually, a connection to a client */
     private class Client implements NonBlockingSender.Listener {        
         Socket socket;
         BufferedInputStream is;
         OutputStream os;        
         NonBlockingSender sender;
         BusinessObjectReader reader;
         ReaderListener readerListener;               
         ClientReceiveMode receiveMode = ClientReceiveMode.ALL;
         Subscriptions subscriptions = Subscriptions.ALL;
         boolean closed;
         /** actual name of client, not including user or addr */
         String clientName;
         String user;
         String addr;
         /** Derived from clientName, user and addr */
         String name;
         boolean senderFinished;
         boolean receiverFinished;
         /** services implemented by client */
         LinkedHashSet<String> services = new LinkedHashSet<String>();         
                 
         Client(Socket socket) throws IOException {
             senderFinished = false;
             receiverFinished = false;
             this.socket = socket;
             addr = socket.getRemoteSocketAddress().toString();
             initName();
             is = new BufferedInputStream(socket.getInputStream());
             os = socket.getOutputStream();            
             sender = new NonBlockingSender(socket, this, log);
             sender.setName(name);
             readerListener = new ReaderListener(this);
             closed = false;
             
             log("Client connected");
             synchronized(ABBOEServer.this) {
                 clients.add(this);
             }            
         }
         
         private void initName() {
             StringBuffer buf = new StringBuffer();
             if (clientName != null) {
                 buf.append(clientName+"-");
             }
             if (user != null) {
                 buf.append(user+"-");
             }
             buf.append(addr);
             name = buf.toString();
         }
         
         private synchronized void setName(String clientName) {
             this.clientName = clientName;
             initName();
             sender.setName("sender-"+this.name);  
             reader.setName("reader-"+this.name);
         }
         
         private synchronized void setUser(String user) {
             this.user = user;
             initName();
             sender.setName("sender-"+name);  
             reader.setName("reader-"+name);
         }
         
         private void send(BusinessObject obj) {
             obj.getMetaData().setSender("ABBOE");
             log.info("Sending: "+obj);
             send(obj.bytes());
         }
         
         private void send(String text) {
             log("Sending plain text to client "+this+": "+text);
             PlainTextObject reply = new PlainTextObject(text);
             send(reply);        
         }
 
         /** Should the object <bo> from client <source> be sent to this client? */ 
         public boolean shouldSend(Client source, BusinessObject bo) {
             
             boolean result;
             
             if (receiveMode == ClientReceiveMode.ALL) {
                 result = true;
             }
             else if (receiveMode == ClientReceiveMode.NONE) {
                 result = false;
             }
             else if (receiveMode == ClientReceiveMode.EVENTS_ONLY) {
                 result = bo.isEvent();               
             }
             else if (receiveMode == ClientReceiveMode.NO_ECHO) {
                 result = (source != this);
             }
             else {
                 log.error("Unknown receive mode: "+receiveMode+"; not sending!");
                 result = false;
             }
             
             if (result == true) {
                 result = subscriptions.shouldSend(bo);
             }
             
             return result;
         }
         
         
        /**
         * Put object to queue of messages to be sent (to this one client) and return immediately.        
         * Assume send queue has unlimited capacity.
         */
         private void send(byte[] packet) {
             if (senderFinished) {
                 warn("No more sending business");
                 return;
             }
             
             try {                
                 sender.send(packet);
             }
             catch (IOException e) {
                 error("Failed sending to client "+this, e);
                 doSenderFinished();
             }
         }       
         
         private synchronized void registerServices(List<String> names) {
             services.addAll(names);
         }
         
         private void startReaderThread() {
             reader = new BusinessObjectReader(is, readerListener, name, false, log);            
             Thread readerThread = new Thread(reader);
             readerThread.start();
         }               
         
         /**
          * Gracefully dispose of a single client after ensuring both receiver and sender 
          * have finished
          */ 
         private void doClose() {
             if (closed) {
                 error("Attempting to close a client multiple times", null);
             }
             
             log("Closing connection with client: "+this);
 
             try {                                        
                 os.flush();
             }
             catch (IOException e) {
                 // let's not bother to even log the exception at this stage
             }
             
             try {
                 // closing socket also closes streams if needed
                 socket.close();
             }
             catch (IOException e) {
                 // let's not bother to even log the exception at this stage
             }
             
             synchronized(ABBOEServer.this) {
                 clients.remove(this);
                 closed = true;
                 if (state == State.SHUTTING_DOWN && clients.size() == 0) {
                     // last client closed, no more clients, finalize shutdown sequence...
                     log.info("No more clients, finalizing shutdown sequence...");
                     finalizeShutdownSequence();
                 }
             }
             
             PlainTextObject msg = new PlainTextObject("Client "+this+" disconnected", CLIENTS_PART_NOTIFY);
             msg.getMetaData().setName(this.name);
             msg.getMetaData().setSender("ABBOE");
             sendToAllClients(this, msg);
         }                
         
         @Override
         public void senderFinished() {            
             doSenderFinished();                     
         }
             
         /** 
          * Actually, just initiate closing of output channel. On noticing this,
          * client should close its socket, which will then be noticed on this server
          * as a {@link BusinessObjectReader.Listener#noMoreObjects()} notification from the {@link #reader}.  
          */
         public void initiateClosingSequence() {
             log.info("Initiating closing sequence for client: "+this);
             BusinessObject shutdownNotification = new PlainTextObject("SERVER IS GOING TO SHUT DOWN IMMEDIATELY");            
             BusinessObjectMetadata meta = shutdownNotification.getMetaData();
             meta.setSender("ABBOE");
             meta.setEvent("server/shutdown");
             try {
                 log.info("Sending shutdown event to client: "+this);
                 sender.send(shutdownNotification.bytes());
             }
             catch (IOException e) {
                 log.warning("Failed sending shutdown event to client "+this);
             }
             
             sender.requestStop();
         }
         
         private synchronized void doSenderFinished() {
             log("doSenderFinished");
             if (senderFinished) {
                 log("Already done");
                 return;
             }
             
             senderFinished = true;
                         
             try {
                 socket.shutdownOutput();
             }
             catch (IOException e) {
                 log.error("Failed closing socket output after finishing sender", e);
             }
             
             
             if (receiverFinished) {
                 doClose();
             }                                 
         }
 
         private synchronized void doReceiverFinished() {
             log("doReceiverFinished");
             if (receiverFinished) {
                 log("Already done");
                 return;
             }
             
             // request stop of sender
             sender.requestStop();
             
             receiverFinished = true;
             
             if (senderFinished) {
                 doClose();
             }                         
         }
         
         private void error(String msg, Exception e) {
             log.error(name+": "+msg, e);
         }
         
         private void log(String msg) {
             log.info(name+": "+msg);
         }
         
         public String toString() {
             return name;
         }
 
     }
                     
     private void acceptSingleClient(Socket clientSocket) {
         
         Client client;
          
         try {
             client = new Client(clientSocket);
             String abboeUser = Biomine3000Utils.getUser();            
             client.send("Welcome to this fully operational java-A.B.B.O.E., run by "+abboeUser);
             if (Biomine3000Utils.isBMZTime()) {
                 client.send("For relaxing times, make it Zombie time");
             }
             client.send("Please register by sending a \""+CLIENTS_REGISTER+"\" event");           
         }
         catch (IOException e) {
             error("Failed creating streams on socket", e);
             try {
                 clientSocket.close();
             }
             catch (IOException e2) {
                 // failed even this, no further action possible
             }
             return;
         }        
 
         client.startReaderThread();
     }          
     
     
     private class SystemInReader extends Thread {
         public void run() {
             try {
                 stdInReadLoop();
             }
             catch (IOException e)  {
                 log.error("IOException in SystemInReader", e);
                 log.error("Terminating...");
                 shutdown();
             }
         }
     }
     
     /**
      * FOO: it does not seem to be possible to interrupt a thread waiting on system.in, even
      * by closing System.in... Thread.interrupt does not work either...
      * it seems that it is not possible to terminate completely cleanly, then.
      */
     private void stdInReadLoop() throws IOException {        
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));            
         String line = br.readLine();
         boolean gotStopRequest = false;
         while (line != null && !gotStopRequest) {   
             if (line.equals("stop") || line.equals("s")) {
                 // this is the end
                 gotStopRequest = true;                
             }
             else {
                 // just a message to be broadcast
                 BusinessObject message = new PlainTextObject(line);
                 String user = Biomine3000Utils.getUser();
                 String sender = user != null ? "ABBOE-"+user : "ABBOE";
                 message.getMetaData().setSender(sender);
                 // log.dbg("Sending object: "+sendObj );  
                 sendToAllClients(null, message);
                 line = br.readLine();
             }
         }
         
         if (gotStopRequest) {
             log.info("Got stop request");
         }
         else {
             log.info("Tranquilly finished reading stdin");
         }
         
         log.info("Harmoniously closing down server by closing output of all client sockets");        		 
         shutdown();
         
     }
             
     
     @SuppressWarnings("unused")
     private synchronized void shutdown() {
         state = State.SHUTTING_DOWN;
         
         // TODO: more delicate termination needed?
         log("Initiating shutdown sequence");
         
         if (clients.size() > 0) {                   
             for (Client client: clients) {
                 client.initiateClosingSequence();
             }                           
             
             // start a thread to ensure shutdown in case some clients fail to close their connections
             ShutdownThread shutdownThread = new ShutdownThread();
             shutdownThread.start();
         }
         else {
             // no clients to close!
             finalizeShutdownSequence();
         }
         
         // System.exit(pExitCode);
     }
     
     /** Finalize shutdown sequence after closing all clients (if any) */
     private void finalizeShutdownSequence() {
         try {
             log.info("Finalizing shutdown sequence by closing server socket");
             serverSocket.close();
         }
         catch (IOException e) {
             // foo
         }
         
         log.info("Exiting");
         System.exit(0);
     }
             
     
    /**
     * Ensure shutdown in case some client(s) fail to close their connection properly 
     * Note that tempting as it might be, it is not possible to send any "you fool" message
     * to these clients at this stage, as any outgoing connections have already been shut down.
     */
     private class ShutdownThread extends Thread {
         public void run() {
             try {
                 Thread.sleep(5000);
                 log.error("Following clients have failed to close their connection properly:" +
                            StringUtils.collectionToString(clients,", ")+
                 		  "; forcing shutdown...");
                 finalizeShutdownSequence();
             }
             catch (InterruptedException e) {
                 log.error("Shutdownthread interrupted");
             }
             
         }
     }
     
     private void handleServicesRegisterEvent(Client client, BusinessObject bo) {
         BusinessObjectMetadata meta = bo.getMetaData();        
         List<String> names = meta.getList("names");
         String name = meta.getString("name");
         if (name != null && names != null) {
             // client has decided to generously provide both name and names; 
             // let's as generously handle this admittably deranged request
             names = new ArrayList<String>(names);
             names.add(name);            
         }
         else if (name != null && names == null) { 
             names = Collections.singletonList(name);
         }
         else if (name == null && names != null) { 
             // no action
         }
         else {
             // both null
             sendErrorReply(client, "No name nor names in services/register event");
             return;
         }
         
         // names now contains the services to register
         client.registerServices(names);        
     }
     
     private void handleClientsListEvent(Client requestingClient) {
         
         BusinessObject clientReport;
         
         synchronized(ABBOEServer.this) {
             clientReport = new PlainTextObject(StringUtils.colToStr(clientReport(requestingClient), "\n"));
             List<String> clientNames = new ArrayList<String>();            
             for (Client client: clients) {
                 if (client != requestingClient) {
                     clientNames.add(client.name);
                 }
             }
             clientReport.getMetaData().put("you", requestingClient.name);
             clientReport.getMetaData().setEvent(CLIENTS_LIST_REPLY);
             clientReport.getMetaData().putStringList("others", clientNames);
         }
         
         requestingClient.send(clientReport);
     }
     
     
     
     private void handleClientRegisterEvent(Client client, BusinessObject bo) {
         BusinessObjectMetadata meta = bo.getMetaData(); 
         String name = meta.getName();
         String user = meta.getUser();
         String receiveModeName = meta.getString(ClientReceiveMode.KEY); 
         if (name != null) {
             client.setName(name);
         }
         else {
             warn("No name in register packet from "+client);
         }
         if (user != null) {
             client.setUser(user);
         }     
         else {
             warn("No user in register packet from "+client);
         }
         
         String msg = "Registered you as \""+name+"-"+user+"\".";
         
         if (receiveModeName != null) {
             ClientReceiveMode recvMode = ClientReceiveMode.getMode(receiveModeName);
             if (recvMode == null) {
                 sendErrorReply(client, "Unrecognized rcv mode in packet: "+recvMode+", using the default: "+client.receiveMode);
             }
             else {
                 client.receiveMode = recvMode;
                 msg+=" Your receive mode is set to: \""+recvMode+"\".";
             }
         }
         else {
             msg+=" No receive mode specified, using the default: "+client.receiveMode;
         }                                                                       
         
         Subscriptions subscriptions = null;
         try {
             subscriptions = meta.getSubscriptions();
         }
         catch (InvalidJSONException e) {
             sendErrorReply(client, "Unrecognized subscriptions in packet: "+e.getMessage());
         }                                                                                    
         
         if (subscriptions != null) {
             msg+=" Your subscriptions are set to: "+subscriptions+".";                            
         }
         else {
             msg+=" You did not specify subscriptions; using the default: "+client.subscriptions;                            
         }                        
                      
         BusinessObject replyObj = new PlainTextObject(msg);
         replyObj.setEvent(CLIENTS_REGISTER_REPLY);
         client.send(replyObj);
 
         // only set after sending the plain text reply                        
         if (subscriptions != null) {
             client.subscriptions = subscriptions;                            
         }               
         
         PlainTextObject registeredMsg = new PlainTextObject("Client "+client+" registered", CLIENTS_REGISTER_NOTIFY);
         registeredMsg.getMetaData().setName(client.name);
         registeredMsg.getMetaData().setSender("ABBOE");
         sendToAllClients(client, registeredMsg);
     }
     
     /** Listens to a single dedicated reader thread reading objects from the input stream of a single client */
     private class ReaderListener implements BusinessObjectReader.Listener {
         Client client;
         
         ReaderListener(Client client) {
             this.client = client;
         }
 
         @Override
         public void objectReceived(BusinessObject bo) {                        
             if (bo.isEvent()) {
                 BusinessObjectEventType et = bo.getMetaData().getKnownEvent();
                 // does this event need to be sent to other clients?
                 boolean forwardEvent = true;
                 if (et != null) {                    
                     log("Received "+et+" event: "+bo);
                     if (et == CLIENT_REGISTER) {
                         sendErrorReply(client, "Using deprecated name for client registration; the " +
                                        "present-day jargon defines that event type be \""+
                                 CLIENTS_REGISTER.toString()+"\"");
                         handleClientRegisterEvent(client, bo);                        
                         forwardEvent = false;
                     }
                    else if (et == CLIENTS_REGISTER) {
                         // deprecated version
                         handleClientRegisterEvent(client, bo);
                         forwardEvent = false;
                     }
                     else if (et == CLIENTS_LIST) {
                         handleClientsListEvent(client);
                         forwardEvent = false;
                     }
                     else if (et == SERVICES_REGISTER) {
                         handleServicesRegisterEvent(client, bo);
                         forwardEvent = false;
                     }
                     else {
                        log("Received known event which this ABBOE implementation does not handle: "+bo);
                     }
                 }
                 else {
                     log("Received unknown event: "+bo.getMetaData().getEvent());
                 }
                 
                 // send the event if needed 
                 if (forwardEvent) {
                     log("Sending the very same event to all clients...");
                     ABBOEServer.this.sendToAllClients(client, bo);
                 }
             }
             else {
                 // not an event, assume mythical "content"
                 
                 if (bo.hasPayload() && bo.getMetaData().getType().equals(Biomine3000Mimetype.PLAINTEXT.toString())) {
                     PlainTextObject pto = new PlainTextObject(bo.getMetaData().clone(), bo.getPayload());
                     log("Received content: "+Biomine3000Utils.formatBusinessObject(pto));
                 }
                 else {
                     log("Received content: "+bo);
                 }
                 // log("Sending the very same content to all clients...");
                 ABBOEServer.this.sendToAllClients(client, bo);
             }
             
         }
                
         
 
         @Override
         public void noMoreObjects() {
             log("connectionClosed (client closed connection).");                                  
             client.doReceiverFinished();            
         }
 
         private void handleException(Exception e) {
             if (e.getMessage() != null && e.getMessage().equals("Connection reset")) {
                 log.info("Connection reset by client: "+this.client);
             }
             else {          
                 error("Exception while reading objects from client "+client, e);                                            
                 log.error(e);                
             }
             client.doReceiverFinished();
         }
         
         @Override
         public void handle(IOException e) {
             handleException(e);
         }
 
         @Override
         public void handle(InvalidBusinessObjectException e) {
             handleException(e);
             
         }
 
         @Override
         public void handle(RuntimeException e) {
             handleException(e);            
         }
         
         public void connectionReset() {
             error("Connection reset by client: "+this.client);
             client.doReceiverFinished();
         }
     }
         
     private void sendErrorReply(Client client, String error) {
         PlainTextObject reply = new PlainTextObject();
         reply.getMetaData().setEvent(ERROR);
         reply.setText(error);
         log("Sending error reply to client "+client+": "+error);
         client.send(reply);        
     }
            
     private void startSystemInReadLoop() {           
         SystemInReader systemInReader = new SystemInReader();
         systemInReader.start();
     }
     
     public static void main(String[] pArgs) throws Exception {
         
         CmdLineArgs2 args = new CmdLineArgs2(pArgs);
                         
         Integer port = args.getInt("port");
         
         if (port == null) {             
             port = Biomine3000Utils.conjurePortByHostName();
         }
         
         if (port == null) {
             error("No -port");
             System.exit(1);
         }
         
         log("Starting ABBOE at port "+port);
                        
         try {
             ABBOEServer server = new ABBOEServer(port);
             // start separate thread for reading system.in
             server.startSystemInReadLoop();
             // the current thread will start executing the main loop
             server.mainLoop();
         }
         catch (IOException e) {
             error("Failed initializing ABBOE", e);
         }
     }
     
     private static void log(String msg) {
         log.info(msg);
     }    
     
     private static void warn(String msg) {
         log.warning(msg);
     }        
     
     private static void error(String msg) {
         log.error(msg);
     }
     
     private static void error(String msg, Exception e) {
         log.error(msg, e);
     }
     
     private enum State {
         NOT_RUNNING,
         ACCEPTING_CLIENTS,
         SHUTTING_DOWN;
     }
           
 }
