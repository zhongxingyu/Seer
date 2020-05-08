 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.lang.reflect.Method;
 
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSServer extends DFSComponent {
 
   public static class SyncRequestWrapper {
     int originator;
     SyncRequestMessage request;
 
     public SyncRequestWrapper(int originator, SyncRequestMessage request) {
       this.originator = originator;
       this.request = request;
     }
   }
 
   public static class OwnershipRequestWrapper extends SyncRequestWrapper {
     int desiredOwner;
     Set<Integer> outstandingReaders;
     
     public OwnershipRequestWrapper(int originator, SyncRequestMessage request, int desiredOwner) {
       super(originator, request);
       this.desiredOwner = desiredOwner;
     }
   }
   
   public static final String tempFileName = ".temp";
 
   private HashMap<DFSFilename, LinkedList<SyncRequestWrapper>> readRequests;
   private HashMap<DFSFilename, OwnershipRequestWrapper> transferOwnerRequests;
   private HashMap<DFSFilename, Set<Integer>> readCheckOuts;
   //private HashMap<DFSFilename, Integer> whoOwns;
   private PersistentStorageCache cache;
 
   public DFSServer(DFSNode parent) {
     super(parent);
 
     System.out.println("Starting the DFSServer...");
     
     readRequests = new HashMap<DFSFilename, LinkedList<SyncRequestWrapper>>();
     transferOwnerRequests = new HashMap<DFSFilename, OwnershipRequestWrapper>();
     readCheckOuts = new HashMap<DFSFilename, Set<Integer>>();
 
     cache = new PersistentStorageCache(parent, this, true);
   }
 
   @Override
   public void onReceive(Integer from, FileNameMessage msg) {
     FileNameMessage f = msg;
     String filename = f.getFileName();
 
     // Demux on the message type and dispatch accordingly.
     // TODO: Shouldn't we get rid of most of these cases?
     switch (msg.getMessageType()) {
     case SyncRequest:
       handleSyncRequest(from, (SyncRequestMessage) msg);
       break;
     case SyncData:
       handleSyncData(from, (SyncDataMessage) msg);
       break;
     case AckData:
       handleAckData(from, (AckDataMessage) msg);
       break;
     default:
       // TODO: Exception fixit. Giving error code 1 so the bitch'll compile.
     }
   }
 
   private void handleAckData(Integer from, AckDataMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     
     OwnershipRequestWrapper req = transferOwnerRequests.get(filename);
     if (req != null && 
         req.outstandingReaders != null &&
         req.outstandingReaders.contains(from)) {
       req.outstandingReaders.remove(from);
      if (req.outstandingReaders.size() == 0)
        finishOwnershipTransfer(req);
     }
 
     if (req.desiredOwner == from) {
       cache.set(filename,
                 entry.getVersion(), 
                 entry.getData(),
                 from,
                 entry.exists());
       transferOwnerRequests.remove(filename);
     }
   }
 
   private void handleSyncData(Integer from, SyncDataMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getOwner() == from) {
       if (entry.getVersion() < msg.getVersion()) {
         cache.set(entry.getKey(),
                   msg.getVersion(),
                   msg.getData(),
                   entry.getOwner(),
                   entry.exists());
       }
 
       if (msg.getFlags().isSet(SyncFlags.TransferOwnership)) {
         transferOwnerToServer(filename);
         OwnershipRequestWrapper rq = transferOwnerRequests.get(filename);
         if (rq != null) {
           invalidateOwnership(rq);
         }
       }
 
       RIOSend(from, Protocol.DATA, new AckDataMessage(filename.toString(), entry.getVersion()).pack());
     }
   }
 
   /** Handles a SyncRequestMessage
    *
    * from: address of requester
    * filename: file in question
    * msg: the message itself
    */
   private void handleSyncRequest(Integer from, SyncRequestMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     Flags f = msg.getFlags();
 
     // Create is a special case; handle separately.
     if (f.isSet(SyncFlags.Create)) {
       Errors result = createFile(from, filename, msg);
       DFSMessage reply = null;
 
       switch (result) {
       case Success:
         reply = buildSyncDataMessage(filename);
         copyAppropriateRequestFlags((SyncDataMessage) reply, msg);
         break;
       case Delayed:
         break;
       default:
         reply = new ResponseMessage(filename.toString(), result.getId());
         break;
       }
       
       if (reply != null)
         RIOSend(from, Protocol.DATA, reply.pack());
 
       return;
     }
 
     //Todo: handle multiple flags set, though certain combo's are impossible
     if (f.isSet(SyncFlags.TransferOwnership)) {
       if (!doTransferOwnership(filename, msg, from)){
         RIOSend(from, 
                 Protocol.DATA,
                 new ResponseMessage(filename.toString(), Errors.OwnershipConflict.getId()).pack());
       }
 
       return;
     } 
 
     if (cache.get(filename).getOwner() != PersistentStorageCache.kNotOwned) {
       SyncRequestWrapper req = new SyncRequestWrapper(cache.get(filename).getVersion(),
                                                       msg);
       enqueueSyncRequest(filename, req);
       return;
     }
 
     checkOutForRead(from, filename);
     SyncDataMessage sdMsg = buildSyncDataMessage(filename);
     copyAppropriateRequestFlags(sdMsg, msg);
 
     RIOSend(from, Protocol.DATA, sdMsg.pack());
   }
   
   private SyncDataMessage buildSyncDataMessage(DFSFilename f) {
     PersistentStorageCache.CacheEntry entry = cache.get(f);
     return new SyncDataMessage(f.toString(), 
                                entry.getVersion(), 
                                new Flags(), 
                                entry.getOwner(), 
                                entry.exists(), 
                                entry.getData());
   }
 
   private void copyAppropriateRequestFlags(SyncDataMessage msg, SyncRequestMessage req) {
     Flags f = req.getFlags();
 
     if (f.isSet(SyncFlags.ReadOnly))
       msg.getFlags().set(SyncFlags.ReadOnly);
   }
 
   private void checkOutForRead(Integer client, DFSFilename filename) {
     //add to readCheckOuts
     Set<Integer> s = readCheckOuts.get(filename);
     if (s == null) {
       s = new HashSet<Integer>();
       readCheckOuts.put(filename, s);
     }
 
     s.add(client);
   }
 
   private void unCheckOutForRead(Integer client, String filename) {
     if (readCheckOuts.get(filename) == null)
       return;
 
     readCheckOuts.get(filename).remove(client);
   }
 
   private Errors createFile(int requestor, DFSFilename file, SyncRequestMessage msg) {
     if (cache.getState(file) != PersistentStorageCache.CacheState.INVALID &&
         cache.get(file).exists())
       return Errors.FileAlreadyExists;
 
     if (cache.get(file).getOwner() != PersistentStorageCache.kNotOwned) {
       OwnershipRequestWrapper req = new OwnershipRequestWrapper(
         requestor, msg, (msg.getFlags().isSet(SyncFlags.TransferOwnership) ? 
                          requestor : 
                          PersistentStorageCache.kNotOwned));
 
       if (!queueOwnershipRequestWrapper(file, req))
         return Errors.OwnershipConflict;
       else
         return Errors.Delayed;
     }
 
     cache.create(file);
 
     if (msg.getFlags().isSet(SyncFlags.TransferOwnership)) {
       doTransferOwnership(file, msg, requestor);
     }
 
     return Errors.Success;
   }
 
   private void enqueueSyncRequest(DFSFilename filename, SyncRequestWrapper req) {
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getOwner() == PersistentStorageCache.kNotOwned)
       throw new IllegalStateException("Shouldn't be doing this with a not-owned entry");
 
     LinkedList<SyncRequestWrapper> reqs = readRequests.get(filename);
     if (reqs == null) {
       reqs = new LinkedList<SyncRequestWrapper>();
       readRequests.put(filename, reqs);
     }
 
     if (reqs.size() == 0) {
       RIOSend(entry.getOwner(), Protocol.DATA, new SyncRequestMessage(filename.toString(),
                                                                       entry.getVersion(),
                                                                       new Flags()).pack());
     }
     
     reqs.add(req);
   }
 
   private boolean stealOwnershipForRequest(OwnershipRequestWrapper request) {
     Flags f = new Flags();
     f.set(SyncFlags.TransferOwnership);
     SyncRequestMessage req = new SyncRequestMessage(request.request.getFileName(),
                                                     request.request.getVersion(),
                                                     f);
     RIOSend(request.desiredOwner, Protocol.DATA, req.pack(), newDeliveryCBInstance());
     return true;
   }
 
   private boolean queueOwnershipRequestWrapper(DFSFilename file, OwnershipRequestWrapper wrap) {
     OwnershipRequestWrapper req = transferOwnerRequests.get(file);
     if (req != null)
       return false;
 
     transferOwnerRequests.put(file, req);
     
     return true;
   }
 
   private boolean doTransferOwnership(DFSFilename file, SyncRequestMessage msg, int newOwner) {
     OwnershipRequestWrapper req = new OwnershipRequestWrapper(newOwner, msg, newOwner);
     if (!queueOwnershipRequestWrapper(file, req))
       return false;
 
     PersistentStorageCache.CacheEntry entry = cache.get(file);
 
     if (cache.get(file).getOwner() == PersistentStorageCache.kNotOwned) {
      if (invalidateOwnership(req))
        finishOwnershipTransfer(req);
     } else if (!stealOwnershipForRequest(req)) {
       return false;
     }
 
     return true;
   }
 
  public void finishOwnershipTransfer(OwnershipRequestWrapper req) {
    DFSFilename file = new DFSFilename(req.request.getFileName());
    if (req.desiredOwner == PersistentStorageCache.kNotOwned) {
      transferOwnerToServer(file);
      return;
    }
    
    SyncDataMessage msg = buildSyncDataMessage(file);
    msg.getFlags().set(SyncFlags.TransferOwnership);
    RIOSend(req.desiredOwner, Protocol.DATA, msg.pack());
    transferOwnerRequests.remove(file);
  }
                                

   public void transferOwnerToServer(DFSFilename filename) {
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getState().equals(PersistentStorageCache.CacheState.INVALID))
       return; // ISSUES
 
     cache.set(filename,
               entry.getVersion(),
               entry.getData(),
               PersistentStorageCache.kNotOwned,
               entry.exists());
   }
 
  public boolean invalidateOwnership(OwnershipRequestWrapper req) {
     DFSFilename file = new DFSFilename(req.request.getFileName());
     PersistentStorageCache.CacheEntry entry = cache.get(file);
     Set<Integer> checkouts = readCheckOuts.get(file);
     if (checkouts != null) {
       int numResponsesNeeded = 0;
       Iterator<Integer> it = checkouts.iterator();
       
       while (it.hasNext()) {
         int reader = it.next();
         RIOSend(reader, 
                 Protocol.DATA,
                 new SyncDataMessage(file.toString(),
                                     entry.getVersion(),
                                     new Flags(),
                                     entry.getOwner(),
                                     entry.exists(),
                                     null).pack());
         numResponsesNeeded++;
       }
      
      return numResponsesNeeded == 0;
     }

    return true;
   }
   
   public Callback newDeliveryCBInstance() {
     try {
       String[] args = new String[]{ "java.lang.Integer", "java.lang.Exception" };
       Method m = Callback.getMethod("sessionSetupPacketTimeout", parent, args);
       return new Callback(m, this, new Object[]{ null, null });
     } catch (NoSuchMethodException nsme) {
       assert(false) : "Should never get here.";
       nsme.printStackTrace();
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false) : "Should never get here.";
       cnfe.printStackTrace();
       return null;
     }    
   }    
   
   public void networkDeliveryCallback(Integer seqNum, Exception e) {
     // TODO bitches
   }
 
 }
