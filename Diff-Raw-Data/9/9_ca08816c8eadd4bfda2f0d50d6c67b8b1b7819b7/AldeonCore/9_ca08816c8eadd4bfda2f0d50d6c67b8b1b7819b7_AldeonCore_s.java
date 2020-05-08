 package org.aldeon.core;
 
 
 import com.google.inject.Inject;
 import org.aldeon.core.events.AppClosingEvent;
 import org.aldeon.core.events.InboundRequestEvent;
 import org.aldeon.db.Db;
 import org.aldeon.dht.Dht;
 import org.aldeon.dht.DhtModule;
 import org.aldeon.dht.crawler.Crawler;
 import org.aldeon.dht.interest.DemandChangedEvent;
 import org.aldeon.events.ACB;
 import org.aldeon.events.AsyncCallback;
 import org.aldeon.events.EventLoop;
 import org.aldeon.networking.NetworkState;
 import org.aldeon.networking.common.AddressType;
 import org.aldeon.networking.common.InboundRequestTask;
 import org.aldeon.networking.common.PeerAddress;
 import org.aldeon.networking.common.Receiver;
 import org.aldeon.networking.common.Sender;
 import org.aldeon.sync.TopicManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Application core. Aggregates all the services necessary for
  * proper functioning of server-side and client-side elements.
  *
  */
 public class AldeonCore extends BaseCore {
 
     private static final Logger log = LoggerFactory.getLogger(AldeonCore.class);
 
     private final Dht dht;
     private final NetworkState networkState;
 
     @Inject
     public AldeonCore(Db storage, EventLoop eventLoop, TopicManager topicManager, NetworkState networkState) {
         super(storage, eventLoop, topicManager);
 
         // Initialize all senders and receivers
 
         getEventLoop().assign(AppClosingEvent.class, new ACB<AppClosingEvent>(clientSideExecutor()) {
             @Override
             public void react(AppClosingEvent val) {
                 closeServices();
                 closeExecutors();
                 closeDb();
                 log.debug("Core closed successfully.");
             }
         });
 
         AsyncCallback<InboundRequestTask> callback = new ACB<InboundRequestTask>(serverSideExecutor()) {
             @Override
             protected void react(InboundRequestTask val) {
                 getEventLoop().notify(new InboundRequestEvent(val));
             }
         };
 
        // Initialize DHT

        dht = DhtModule.create(getSender().acceptedTypes(), eventLoop);

         // Iterate through machine addresses
 
         this.networkState = networkState;
 
         for(AddressType type: getSender().acceptedTypes()) {
             for(PeerAddress machineAddress: networkState.getMachineAddresses(type)) {
                 if(machineAddress != null) {
                     log.info("Detected address (" + machineAddress.getType() + "): " + machineAddress);
                 }
             }
         }
 
         // Start communication
 
         getReceiver().setCallback(callback);
 
         getSender().start();
         getReceiver().start();
         initializeSupervisor();
 
         // Initialize crawler
 
         final Crawler crawler = new Crawler(getDht(), getSender());
         eventLoop.assign(DemandChangedEvent.class, new ACB<DemandChangedEvent>(clientSideExecutor()) {
             @Override
             protected void react(DemandChangedEvent val) {
                 crawler.notifyDemandChanged(val.addressType(), val.topic());
             }
         });
 
         log.debug("Initialized the core.");
     }
 
     @Override
     public Dht getDht() {
         return dht;
     }
 
     @Override
     public Sender getSender() {
         return networkState.getUnifiedSender();
     }
 
     @Override
     public Receiver getReceiver() {
         return networkState.getUnifiedReceiver();
     }
 
     private void closeServices() {
         log.debug("Closing the core...");
         getSender().close();
         getReceiver().close();
     }
 }
