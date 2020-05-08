 package org.nebulostore.communication.bdbdht;
 
 import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.Durability;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 import com.sleepycat.je.LockMode;
 import com.sleepycat.je.OperationStatus;
 import com.sleepycat.je.Transaction;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
 import org.nebulostore.appcore.Message;
 import org.nebulostore.appcore.Module;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.communication.CommunicationPeer;
 import org.nebulostore.communication.address.CommAddress;
 import org.nebulostore.communication.dht.KeyDHT;
 import org.nebulostore.communication.dht.ValueDHT;
 import org.nebulostore.communication.messages.ReconfigureDHTAckMessage;
 import org.nebulostore.communication.messages.ReconfigureDHTMessage;
 import org.nebulostore.communication.messages.bdbdht.BdbMessageWrapper;
 import org.nebulostore.communication.messages.bdbdht.HolderAdvertisementMessage;
 import org.nebulostore.communication.messages.dht.DHTMessage;
 import org.nebulostore.communication.messages.dht.ErrorDHTMessage;
 import org.nebulostore.communication.messages.dht.GetDHTMessage;
 import org.nebulostore.communication.messages.dht.OkDHTMessage;
 import org.nebulostore.communication.messages.dht.OutDHTMessage;
 import org.nebulostore.communication.messages.dht.PutDHTMessage;
 import org.nebulostore.communication.messages.dht.ValueDHTMessage;
 import org.nebulostore.networkmonitor.NetworkContext;
 
 /**
  * Implementation of berkely db based engine for...
  * @author marcin
  */
 public class BdbPeer extends Module {
 
   private static String configurationPath_ = "resources/conf/communication/BdbPeer.xml";
   private static Logger logger_ = Logger.getLogger(BdbPeer.class);
 
   private String storagePath_;
   private String storeName_;
   private Database database_;
   private Environment env_;
 
   private boolean isProxy_;
   private CommAddress holderCommAddress_;
   //host1.planetlab.informatik.tu-darmstadt.de
   private final CommAddress bdbHolderCommAddress_ = null;
 
   private final BlockingQueue<Message> senderInQueue_;
   private final ReconfigureDHTMessage reconfigureRequest_;
   private Timer advertisementsTimer_;
 
   /**
    * @param inQueue
    * @param outQueue
    * @param senderInQueue Queue to module for direct sending of messages to
    * other peers
    */
   public BdbPeer(BlockingQueue<Message> inQueue,
       BlockingQueue<Message> outQueue,
       CommAddress peerAddress,
       BlockingQueue<Message> senderInQueue,
       ReconfigureDHTMessage reconfigureRequest) {
     super(inQueue, outQueue);
 
     senderInQueue_ = senderInQueue;
     reconfigureRequest_ = reconfigureRequest;
 
     // TODO: move to factory in appcore
     XMLConfiguration config = null;
     try {
       config = new XMLConfiguration(configurationPath_);
     } catch (ConfigurationException cex) {
       logger_.error("Configuration read error in: " + configurationPath_);
     }
     if (config.getString("type", "proxy").equals("storage-holder")) {
       logger_.info("Configuring as bdb database holder");
 
      storagePath_ = config.getString("sleepycat.storage-path",
          "/tmp/nebulostore-bdb");

      storeName_ = config.getString("sleepycat.store-name", "nebulostore-bdb");
 
       logger_.debug("storagePath: " + storagePath_);
 
       env_ = createEnvironment(new File(storagePath_));
 
       DatabaseConfig dbConfig = new DatabaseConfig();
       dbConfig.setTransactional(true);
       dbConfig.setAllowCreate(true);
 
       database_ = env_.openDatabase(null, storeName_, dbConfig);
 
       advertisementsTimer_ = new Timer();
       advertisementsTimer_.schedule(new SendAdvertisement(), 4000, 2000);
 
       if (reconfigureRequest_ != null) {
         outQueue_.add(new ReconfigureDHTAckMessage(reconfigureRequest_));
       }
 
       holderCommAddress_ = CommunicationPeer.getPeerAddress();
     } else {
       logger_.info("Configuring as proxy");
       isProxy_ = true;
       holderCommAddress_ = bdbHolderCommAddress_;
     }
     logger_.info("fully initialized");
   }
 
   /**
    * Module that sends holder advertisements.
    */
   public class SendAdvertisement extends TimerTask {
     @Override
     public void run() {
       logger_.info("Sending holder advertisements to remote hosts...");
       for (CommAddress address : NetworkContext.getInstance().getKnownPeers()) {
         senderInQueue_.add(new HolderAdvertisementMessage(address));
       }
     }
   }
 
   @Override
   public void endModule() {
     logger_.info("Ending bdb peer");
     if (!isProxy_) {
       advertisementsTimer_.cancel();
       logger_.info("Closing database...");
       database_.close();
       env_.close();
     }
 
     super.endModule();
   }
 
   private Environment createEnvironment(File dir) {
     EnvironmentConfig config = new EnvironmentConfig();
     config.setAllowCreate(true);
     config.setDurability(Durability.COMMIT_SYNC);
 
     config.setTransactional(true);
     config.setTxnSerializableIsolation(true);
 
     Environment env = new Environment(dir, config);
     return env;
   }
 
   private void put(PutDHTMessage putMsg, boolean fromNetwork,
       CommAddress sourceAddress) {
     String srcAddr = sourceAddress == null ? "null" : sourceAddress.toString();
     logger_.info("PutDHTMessage (" + putMsg.getId() + ") in holder with " +
         putMsg.getKey().toString() + " : " + putMsg.getValue().toString() +
         " and sourceAddress = " + srcAddr);
 
     KeyDHT key = putMsg.getKey();
     ValueDHT valueDHT = putMsg.getValue();
 
     // TODO: Serialization error handling as well
     Transaction t = env_.beginTransaction(null, null);
 
     DatabaseEntry data = new DatabaseEntry();
     OperationStatus operationStatus = database_.get(t,
             new DatabaseEntry(key.toString().getBytes()),
             data,
             LockMode.DEFAULT);
 
     if (operationStatus == OperationStatus.SUCCESS) {
       logger_.info("Performing merge on object from DHT");
       ValueDHT oldValue = ValueDHT.build(new String(data.getData()));
       valueDHT = new ValueDHT(valueDHT.getValue().merge(oldValue.getValue()));
     }
 
     String value = valueDHT.serializeValue();
     database_.put(t, new DatabaseEntry(key.toString().getBytes()),
         new DatabaseEntry(value.getBytes()));
 
     t.commit();
     if (fromNetwork) {
       senderInQueue_.add(new BdbMessageWrapper(null, sourceAddress,
           new OkDHTMessage(putMsg)));
     } else {
       outQueue_.add(new OkDHTMessage(putMsg));
     }
     logger_.info("PutDHTMessage processing finished");
   }
 
   private void get(GetDHTMessage getMsg, boolean fromNetwork, CommAddress sourceAddress) {
     logger_.debug("GetDHTMessage in holder");
     KeyDHT key = getMsg.getKey();
     DatabaseEntry data = new DatabaseEntry();
     OperationStatus operationStatus = database_.get(null,
         new DatabaseEntry(key.toString().getBytes()), data, LockMode.DEFAULT);
 
     OutDHTMessage outMessage;
     if (operationStatus == OperationStatus.SUCCESS) {
       ValueDHT value = ValueDHT.build(new String(data.getData()));
       outMessage = new ValueDHTMessage(getMsg, key, value);
     } else {
       logger_.debug("Unable to read from database. Sending ErrorDHTMessage.");
       outMessage = new ErrorDHTMessage(getMsg, new NebuloException(
           "Unable to read from bdb database. Operation status: " + operationStatus));
     }
 
     if (fromNetwork) {
       senderInQueue_.add(new BdbMessageWrapper(null, sourceAddress, outMessage));
     } else {
       outQueue_.add(outMessage);
     }
     logger_.debug("GetDHTMessage processing finished");
   }
 
   @Override
   protected void processMessage(Message msg) throws NebuloException {
     logger_.debug("Processing message: " + msg);
     if (msg instanceof HolderAdvertisementMessage) {
       logger_.info("Message accepted: " + msg);
 
       if (holderCommAddress_ == null && reconfigureRequest_ != null) {
         outQueue_.add(new ReconfigureDHTAckMessage(reconfigureRequest_));
       }
       holderCommAddress_ = ((HolderAdvertisementMessage) msg)
           .getSourceAddress();
       logger_.info("Holder detected at " + holderCommAddress_);
       return;
     }
 
     if (isProxy_ && holderCommAddress_ == null) {
       logger_.debug("Holder not set up, waiting...");
       logger_.debug("Sleeping and waiting for holderCommAddress");
       //TODO-GM Why are we sleeping? Couldn't we add those messages to some kind
       //of queue?
       try {
         Thread.sleep(500);
       } catch (InterruptedException e) {
         logger_.error(e);
       }
       inQueue_.add(msg);
       return;
     }
 
     if (isProxy_) {
       if (msg instanceof DHTMessage) {
         logger_.debug("Message accepted. " + msg);
         logger_.debug("Putting message to be sent to holder (taskId = " +
             msg.getId() + ")");
         senderInQueue_.add(new BdbMessageWrapper(null, holderCommAddress_,
             (DHTMessage) msg));
       } else {
         logger_.error("Unknown message of type: " + msg);
       }
     } else {
       logger_.info("Message accepted: " + msg);
       boolean fromNetwork = false;
       CommAddress sourceAddress = null;
       Message message;
       if (msg instanceof BdbMessageWrapper) {
         message = ((BdbMessageWrapper) msg).getWrapped();
         fromNetwork = true;
         sourceAddress = ((BdbMessageWrapper) msg).getSourceAddress();
       } else {
         message = msg;
       }
 
       if (message instanceof PutDHTMessage) {
         put((PutDHTMessage) message, fromNetwork, sourceAddress);
       } else if (message instanceof GetDHTMessage) {
         get((GetDHTMessage) message, fromNetwork, sourceAddress);
       } else {
         logger_.error("BdbPeer got message that is not supported");
       }
     }
   }
 
   public CommAddress getHolderAddress() {
     return holderCommAddress_;
   }
 }
