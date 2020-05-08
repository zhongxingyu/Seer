 import java.lang.reflect.Method;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSClient extends DFSComponent {
 
   private HashMap<Integer, RequestWrapper> issuedCommands;
   private LinkedList<Integer> requestQueue;
   private PersistentStorageCache cache;
   private int serverAddr;
 
   public DFSClient(DFSNode client, int serverAddr) {
     super(client);
     this.serverAddr = serverAddr;
   }
 
   /**
    * Callback for server response timeout.
    *
    * @param seqno
    *            Sequence number of the timed-out packet.
    *
    */
   public void requestTimeoutCb(Integer seqno) {
     RequestWrapper request = issuedCommands.remove(seqno);
     if (request == null) {
       // Needed in case events get reordered.
       return;
     }
 
     request.invokeCallback(seqno, new DFSExceptions.ServerTimeout());
   }
 
 
   /**
    * Callback for network layer.
    *
    * @param seqno
    *            Sequence number of finished packet.
    *
    * @param e
    *           Exception thrown by possible failure of packet.
    */
   public void networkDeliveryCb(Integer seqno, NetworkExceptions.NetworkException e) {
     RequestWrapper request = issuedCommands.get(seqno);
     if (request == null) {
       // Deal with event ordering issues.
       return;
     }
 
     if (e != null) {
       issuedCommands.remove(seqno);
       request.invokeCallback(seqno, new DFSExceptions.Network(e));
     } else {
       Callback cb;
       Method method;
       try {
         method = Callback.getMethod("requestTimeoutCb", this, new String[]{ "java.lang.Integer" });
         cb = new Callback(method, this, new Object[]{seqno});
       } catch (NoSuchMethodException nsme) {
         assert(false): "Should never get here.";
         nsme.printStackTrace();
         return;
       } catch (ClassNotFoundException cnfe) {
         assert(false): "Should never get here.";
         cnfe.printStackTrace();
         return;
       }
       addTimeout(cb, ReliableInOrderMsgLayer.MAX_PACKET_XFER_TIME);
 
     }
   }
 
 
   /**
    * Test user callback.
    *
    */
   public void commandComplete(Integer seqNo, Exception e) {
     RequestWrapper request = issuedCommands.get(seqNo);
     if (e != null) {
 
       DFSMessage message = request.msg;
       System.err.print("Node " + this.addr + ": Error: ");
       e.printStackTrace();
 
       System.err.println(" on server " + request.recipient + " and file " +
                          ((FileNameMessage) message).getFileName());
     } else {
       // TODO
     }
   }
 
 
   /**
    * Issue a request to execute a command on the server.
    *
    * @param msg
    *            Message to send to the server.
    *
    * @param userCompleteCb
    *            Callback to fire on success.
    */
   private void issueCommand(DFSMessage msg, Callback userCompleteCb) {
     // Create a network delivery callback.
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer",
                               "NetworkExceptions$NetworkException" };
       method = Callback.getMethod("networkDeliveryCb", this, paramTypes);
       cb = new Callback(method, this, new Object[]{ msg, serverAddr });
     } catch (IllegalArgumentException iae) {
       System.err.println(iae.getMessage());
       return;
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     // Pack and send the message.
     byte[] packedMsg = msg.pack();
     System.out.println("Client (addr " + this.addr + ") sending: " +
                        msg.toString());
     int seqno = RIOSend(serverAddr, Protocol.DATA, packedMsg, cb);
     requestQueue.add(seqno);
     issuedCommands.put(seqno, new RequestWrapper(msg, serverAddr, userCompleteCb));
   }
 
 
   /**
    * Send a create request to the server.
    *
    * @param filename
    *            Name of the file to create.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void create(String filename, Callback cb) {
     Flags flags = new Flags();
     flags.set(SyncFlags.Create);
     SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
     issueCommand(msg, cb);
   }
 
 
   /**
    * Send a get request to the server.
    *
    * @param filename
    *            Name of the file to get.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void get(String filename, Callback cb) {
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
     PersistentStorageCache.CacheState state = cachedFile.getState();
 
     if (state == PersistentStorageCache.CacheState.INVALID ||
         (state == PersistentStorageCache.CacheState.READ_ONLY &&
          (cachedFile.getOwner() == cache.kNotOwned))) {
       // If file isn't cached or is owned by someone else, send sync
       // request to server.
       Flags flags = new Flags();
       flags.set(SyncFlags.ReadOnly);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(msg, cb);
     } else {
       // File is cached, so we fire callback with its contents.
       // TODO: callback exception sig: cb(Exception e, String data);
       cb.setParams(new Object[]{null, cachedFile.getData()});
       try {
         cb.invoke();
       } catch (Exception e) {
         System.err.println("An exception occurred while trying to call the" +
                            "callback for a get request to " + filename + ":");
         e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a put request to the server.
    *
    * @param filename
    *            Name of the file to put.
    *
    * @param contents
    *            Contents to put in file.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void put(String filename, String contents, Callback cb) {
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(msg, cb);
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       // TODO: callback exception sig: cb(Exception e, String data);
       cache.update(new DFSFilename(filename), contents);
       cb.setParams(new Object[]{null, cachedFile.getData()});
       try {
         cb.invoke();
       } catch (Exception e) {
         System.err.println("An exception occurred while trying to call the" +
                            "callback for a put request to " + filename + ":");
         e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a append request to the server.
    *
    * @param filename
    *            Name of the file to append.
    *
    * @param contents
    *            Contents to append to file.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void append(String filename, String contents, Callback cb) {
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(msg, cb);
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       // TODO: callback exception sig: cb(Exception e, String data);
       cache.update(new DFSFilename(filename),
                    cachedFile.getData() + contents);
       cb.setParams(new Object[]{null, null});
       try {
         cb.invoke();
       } catch (Exception e) {
         System.err.println("An exception occurred while trying to call the" +
                            "callback for an append request to " + filename +
                            ":");
         e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a delete request to the server.
    *
    * @param filename
    *            Name of the file to delete.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void delete(String filename, Callback cb) {
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(msg, cb);
     case READ_WRITE:
       // We have ownership, so delete file.
       // TODO: callback exception sig: cb(Exception e, String data);
       cache.delete(new DFSFilename(filename));
       cb.setParams(new Object[]{null, null});
       try {
         cb.invoke();
       } catch (Exception e) {
         System.err.println("An exception occurred while trying to call the" +
                            "callback for an append request to " + filename +
                            ":");
         e.printStackTrace();
       }
     }
   }
 
 
   /**
    * TODO
    * NOTE: This has 3 cases, essentially:
    *         * SyncRequest
    *         * SyncData (invalidation)
    *         * SyncData (completion)
    */
  public void onReceive(Integer from, FileNameMessage msg) {
     AckDataMessage response;
 
     if (msg instanceof SyncRequestMessage) {
       SyncRequestMessage srMsg = (SyncRequestMessage) msg;
       Flags flags = srMsg.getFlags();
 
       if (flags.isSet(SyncFlags.TransferOwnership)) {
         response = handleTransferOwnershipRequest(from, srMsg.getDFSFileName());
       } else if (flags.isSet(SyncFlags.Create)) {
         //        response = handleCreateSyncRequest(from, filename, srMsg);
       } else if (flags.isSet(SyncFlags.ReadOnly)) {
         //        response = handleReadOnlySyncRequest(from, filename, srMsg);
       } else {
         System.err.println("Node " + this.addr + ": Error: Invalid flags in " +
                            "message received from server " + from);
         return;
       }
     } else if (msg instanceof SyncDataMessage) {
        // Data or invalidation received
     } else {
       System.err.println("Node " + this.addr + ": Error: Invalid message " +
                          "type received from server " + from);
       return;
     }
 
     // TODO: Send the response.
 
     /*
     // We're a client receiving a response.
     ResponseMessage response = (ResponseMessage) msg;
     int seqno = requestQueue.remove();
     RequestWrapper issuedReq = issuedCommands.remove(seqno);
     DFSMessage issuedMsg = issuedReq.msg;
     if (response.getCode() != 0) {
       System.err.print("Node " + this.addr + ": Error: ");
 
       // Print command.
       switch (issuedMsg.getMessageType()) {
       case Create:
         System.err.print("create");
         break;
       case Get:
         System.err.print("get");
         break;
       case Put:
         System.err.print("put");
         break;
       case Append:
         System.err.print("append");
         break;
       case Delete:
         System.err.print("delete");
         break;
       default:
         System.err.print("invalid command");
       }
 
       System.err.println(" on server " + from + " and file " +
                          ((FileNameMessage) issuedMsg).getFileName() +
                          " returned error code " + response.getCode());
     }
     */
   }
 
 
   /**
    * TODO
    */
   private AckDataMessage handleTransferOwnershipRequest(Integer from, DFSFilename fname) {
     return null;
   }
 
 }
