 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aether.io;
 
 import aether.cluster.ClusterMgr;
 import aether.conf.ConfigMgr;
 import aether.net.ControlMessage;
 import aether.net.NetMgr;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 /**
  *
  * Client Connector threads handle the communication with the client component.
  * For a read request, this thread will return a list of nodes having the 
  * chunks to the client through a ControlMessage, and receive the file for a 
  * write request
  * @author aniket
  */
 public class ClientConnector implements Runnable {
 
   
     
     private Socket sock;
     private ConcurrentHashMap<String, Integer> map;
     private static final int SOCKET_SO_TIMEOUT = 5000;
     private static final Logger log = 
             Logger.getLogger(DataMgr.class.getName());
     private InputStream in;
     private OutputStream out;
     private ObjectInputStream oIn;
     private ObjectOutputStream oOut;
     
     
     
     
     static {
         log.setLevel(Level.INFO);
         ConsoleHandler handler = new ConsoleHandler();
         handler.setFormatter(new SimpleFormatter());
         handler.setLevel(Level.ALL);
         log.addHandler(handler);
     }
     
     
     
     public ClientConnector (Socket s, ConcurrentHashMap<String, Integer> m) 
             throws IOException {
         
         sock = s;
         map = m;
         if (ConfigMgr.getIfDebug()) {
             log.setLevel(Level.FINE);
         }
         init();
     }
     
     
     
     
     
     
     
     /**
      * Initialize socket and the streams to read and write from
      * @throws IOException 
      */
     private void init() throws IOException {
 
         /* Initialize the socket and the stream handles
          * 
          */
 
         sock.setSoTimeout(SOCKET_SO_TIMEOUT);
         out = sock.getOutputStream();
         oOut = new ObjectOutputStream(out);
         oOut.flush();
 
         in = sock.getInputStream();
         oIn = new ObjectInputStream(in);
 
 
     }
     
     
     
     
     
     
     
     /**
      * Split the given data into chunks
      * @param rawdata       data to be split
      * @param file          name of the file to which this data belongs
      * @param chunkSize     size of a chunk
      * @param numChunks     number of chunks
      * @return              Array of chunks
      */
     public Chunk[] chunkify (byte[] rawdata, String file, int chunkSize, 
             int numChunks) {
         
         Chunk[] chunkArray = new Chunk[numChunks];
         byte[] chunkData;
         
         for (int i=0; i < numChunks; i++) {
             
             /* Make sure that the last chunk data has to be copied properly.
              * Otherwise, copyOfRange will just append zeroes and client will
              * cry when the read file is different than the written.
              */
             if (i == numChunks-1) {
                 chunkData = Arrays.copyOfRange(rawdata, i*chunkSize, 
                         rawdata.length);
             } else {
                 chunkData = Arrays.copyOfRange(rawdata, i*chunkSize, 
                     ((i+1) * chunkSize)-1);
             }
                         
             chunkArray[i] = new Chunk(file, i, chunkData);
         }
         
         return chunkArray;
     }
     
     
     
     
     
     /**
      * Prepare the payload for 'l' message to be sent to the client
      * @param map   Hashmap containing chunkIds and list of nodes having those
      * @return  String delimited chunks and addresses.
      */ 
     private String prepareChunkNodeList (HashMap<Integer, 
             LinkedList<InetAddress>> map) {
         
         String load = "";
         for (Integer i: map.keySet()) {
             
             LinkedList<InetAddress> nodes = map.get(i);
             String addresses = "";
             for (InetAddress add: nodes) {
                 addresses = addresses + ":" + 
                         add.toString().replaceFirst(".*/", "");
             }
             
             
             load = load + "%" + i.toString() + "|" + addresses;
         }
         
         return load;
     }
     
     
     
     
     /**
      * Handle the read request from client
      * @param first The first ControlMessage sent by client
      * @throws SocketException
      * @throws IOException
      * @throws UnsupportedOperationException 
      */
     private void read (ControlMessage first) throws SocketException, IOException,
             UnsupportedOperationException {
         
         String filename = first.ParseEControl();
         
 
         if (filename == null) {
             /* This means that the client is requesting a node 
              * list. That is the task for ClusterMgr
              */
             log.fine("Node list request received");
             ClusterMgr c = ClusterMgr.getInstance();
             String clustrInfo = c.prepareJoinMessagePayload();
             ControlMessage nodeListMsg = new ControlMessage('j',
                     sock.getInetAddress(), 0, clustrInfo);
             log.fine("Sending the node list to client");
             oOut.writeObject(nodeListMsg);
             oOut.flush();
 
         } else {
             
             log.log(Level.FINE, "Received a read request for file {0}",
                 filename);
             /*
              * First get the list of nodes that have chunks of this file
              */
 
             NetMgr netmgr = new NetMgr (sock.getLocalPort());
             ControlMessage probe = new ControlMessage ('e', 
                     NetMgr.getBroadcastAddr(), filename);
             log.log(Level.FINE, "Querying for the chunk list of file {0}", 
                     filename);
             netmgr.send(probe);
             
             HashMap<Integer, LinkedList<InetAddress>> chunkMap = 
                     new HashMap<>();
             
             while (true) {
             
                 log.fine("Waiting for chunk reply");
                 ControlMessage reply = null;
                 try {
                     reply = (ControlMessage) netmgr.receive();
                 } catch (SocketTimeoutException e) {
                     /* This means no more replies are coming.
                      * We must have gotten all of them.
                      */
                     log.fine("Tired of waiting for chunk replies");
                     break;
                 }
                 
                 
                 if (reply.getMessageSubtype() != 'k') {
                     continue;
                 }
                 
                 String data = reply.parseKControl();
                 String[] chunkIds = data.split(":");
                 InetAddress addr = reply.getSourceIp();
                 
                 for (String id:chunkIds) {
                     /* Prepare a map in which chunkId is key and InetAddress
                      * list is value.
                      */
                     Integer i = Integer.parseInt(id);
                     if (chunkMap.containsKey(i)) {
                         LinkedList<InetAddress> foo = chunkMap.get(i);
                         foo.add(addr);
                         chunkMap.put(i, foo);
                     } else {
                         LinkedList<InetAddress> foo = new LinkedList<>();
                         foo.add(addr);
                         chunkMap.put(i, foo);
                     }
                 }
                 
             }
             
             /* We are here means chunk map is prepared. Now prepare the list 
              * message to be sent.
              */
             String payload = prepareChunkNodeList (chunkMap);
             ControlMessage list = new ControlMessage ('l', 
                     sock.getInetAddress(), payload);
             oOut.writeObject(list);
             oOut.flush();
 
         }
     }
     
     
     
     
     
     
     
     
     
     
     /**
      * Handle the write request by the client
      * @param first The first ControlMessage sent by client
      * @throws IOException 
      */
     private void write (ControlMessage first) throws IOException {
         /*
          * TODO: Accept the file over the channel, split it
          * into chunks and hand it to the replication manager.
          */
         String filedata = first.parseWControl();
         // First token is filename and second is size in bytes
         String[] tokens = filedata.split(":");
         String file = tokens[0];
         Integer size = Integer.parseInt(tokens[1]);
 
         log.fine("Received a write request from client");
         log.log(Level.FINE, "File: {0}, size: {1} B",
                 new Object[]{file, size});
 
         ControlMessage ack = new ControlMessage('k',
                 sock.getInetAddress());
 
         byte[] rawdata = new byte[size];
         /* This should be improved to read the stream into 
          * chunks directly. We are risking read of a huge file
          * and running out of memory here. Important thing to
          * improve in future
          */
         int chunkSize = ConfigMgr.getChunkSize();
         int numChunks = size / chunkSize;
         if ((size % chunkSize) != 0) {
             numChunks++;
         }
        
        log.fine("Sending acknowledgment to client");
        oOut.writeObject(ack);
        oOut.flush();
 
         log.log(Level.FINE,
                 "Spliting the file {0} in {1} chunks",
                 new Object[]{file, numChunks});
 
         oIn.readFully(rawdata);
 
         /* We are here means file was received. Now we need to
          * split that file into chunks
          */
         Chunk[] chunks = chunkify(rawdata, file, chunkSize,
                 numChunks);
         log.log(Level.FINE, "Created {0} chunks", chunks.length);
         
         map.put(file, chunks.length);
         
         /* Now I just need to pass these chunks to the replication manager
          * and we are set. This final step cannot be completed till replication
          * manager defines the API
          */
     }
     
     
     
     
     /**
      * Return the chunk the client has asked for
      * @param first ControlMessage sent by client having filename and chunkId
      * @throws IOException 
      */
     private void readChunk (ControlMessage first) throws IOException {
         
         String chunkDetails = first.parseBControl();
         String[] tokens = chunkDetails.split(":");
         
         String chunkName = tokens[0] + tokens[1];
         log.log(Level.FINE, "Received request for chunk name {0}", chunkName);
         Chunk chunk = null;
         
         // Get the chunk from replication manager
         /*
          * Have to write the code to get the chunk from replication manager 
          * here. The API is not defined yet.
          */
         
         oOut.writeObject(chunk);
         oOut.flush();
     }
     
     
     
     /**
      * Cleanup the table
      * @throws IOException 
      */
     private void cleanUp () throws IOException {
         
         oIn.close();
         oOut.close();
         in.close();
         out.close();
         sock.close();
         
     }
     
     
     
     
     @Override
     public void run() {
 
 
         log.log(Level.FINE, "Starting handshake with client on socket {0}",
                 sock);
 
         try {
             ControlMessage first = (ControlMessage) oIn.readObject();
             char subtype = first.getMessageSubtype();
 
             switch (subtype) {
 
                 case 'e':
                     /* Read request. This means client either wants healthy 
                      * node list, or list of nodes having chunks of his file
                      */
                     read(first);
                     break;
 
                 case 'w':
                     /* Write request. This means the file write is to be 
                      * done. 
                      */
 
                     write(first);
                     break;
                     
                 case 'b':
                     /* Read chunk request. This means that the client has
                      * the chunk Ids and filename and knows what he is 
                      * asking for. Just return him the chunk.
                      */
                     readChunk(first);
                     break;
 
                 default:
 
                     log.log(Level.SEVERE,
                             "Unknown request '{0}' by the client", subtype);
 
             }
             
             cleanUp();
 
         } catch (IOException ie) {
             log.severe("I/O Exception while handling the client request");
             ie.printStackTrace();
         } catch (ClassNotFoundException c) {
             log.severe("Received unknown object from the client");
             c.printStackTrace();
         }
     }
 }
