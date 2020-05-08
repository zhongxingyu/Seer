 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.nodeCom;
 
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import net.tinyos.message.Message;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for listening to multiple nodes at once.
  * Can save RAM to decrease thread count needed for full operation.
  * @author ph4r05
  */
 public class MultipleMessageListener extends Thread implements MessageListenerInterface {
 
     private static final Logger log = LoggerFactory.getLogger(MultipleMessageListener.class);
     
     /**
      * Maximum number of messages in queue to reset queue
      */
     public static final int MAX_QUEUE_SIZE_TO_RESET=10000;
 
     /**
      * Master message queue for every node
      */
     private final ConcurrentLinkedQueue<MessageReceived> queue = 
             new ConcurrentLinkedQueue<MessageReceived>();
     
     /**
      * Per node listeners
      * NodeID -> MultipleMessageListenerSubComponent
      */
     protected ConcurrentHashMap<Integer, MultipleMessageListenerSubComponent> perNodeListeners = 
             new ConcurrentHashMap<Integer, MultipleMessageListenerSubComponent>();
     
     /**
      *Global queue of message listeners registered for particular AMType
      * AMType->Queue<Listener>
      * 
      * Listener is added here if global register is called. This listeners will
      * be connected to newly connected nodes.
      */
     protected Map<Integer, Queue<MessageListener>> messageListeners = 
             new ConcurrentHashMap<Integer, Queue<MessageListener>>(1);
     
     /**
      * time of last sent message
      */
     protected long timeLastMessageSent;
 
     /**
      * Should I shutdown?
      */
     protected boolean shutdown=false;
     
     /**
      * Am I dropping packets currently?
      */
     protected boolean dropingPackets=true;
 
     /**
      *
      * @param gateway
      * @param logger
      */
     public MultipleMessageListener(String threadName) {
         // set thread title
         super("MyMessageListener: " + threadName);
     }
 
     /**
      * Pausing thread
      * @param microsecs
      */
     private void pause(int microsecs) {
         try {
             Thread.sleep(microsecs);
         } catch (InterruptedException ie) {
             log.warn("Cannot sleep", ie);
         }
     }
     
     /**
      * Performs shutdown
      */
     @Override
     public synchronized void shutdown(){
         this.reset();
         this.dropingPackets=true;
         this.shutdown=true;
     }
     
     /**
      * Register message listener
      * If same listener (.equals()) is registered to same message type, request
      * is ignored.
      */
     @Override
     public synchronized void registerListener(net.tinyos.message.Message msg, MessageListener listener){
         // null?
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
         amtypeMapper.registerMessage(msg);
         
         int amtype = msg.amType();
         
         // register to global if exists
         Queue<MessageListener> queueListeners = null;
         if (this.messageListeners.containsKey(amtype)){
             queueListeners = this.messageListeners.get(amtype);
         }
         
         // if is queue null, init it here
         if (queueListeners==null){
             queueListeners=new ConcurrentLinkedQueue<MessageListener>();
         }
         
         // add to queue if is not already in
         if (queueListeners.contains(listener)==false){
             queueListeners.add(listener);
         }
         
         // store back to map
         this.messageListeners.put(amtype, queueListeners);
         
         // register to all defined nodes
         for(Integer node : this.perNodeListeners.keySet()){
             this.registerListener(node, msg, listener);
         }
     }
     
     /**
      * register only to particular node
      * @param node
      * @param msg
      * @param listener 
      */
     @Override
     public void registerListener(int node, Message msg, MessageListener listener) {
         // register for particular node here
         // null?
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             // node id needs to be in database
             if (this.perNodeListeners.containsKey(node)==false){
                 log.error("Cannot register listener for node: " + node + "; not found in database");
                 return;
             }
             
             MultipleMessageListenerSubComponent subComponent = this.perNodeListeners.get(node);
             subComponent.registerListener(msg, listener);
         } catch(Exception e){
             log.error("Problem during registering message listener", e);
         }
     }
     
     /**
      * Unregister global message listener - remove from my global list + deregister from each per node
      */
     @Override
     public synchronized void deregisterListener(net.tinyos.message.Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener){
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
         amtypeMapper.registerMessage(msg);
         
         // deregister from my global map
         if (this.messageListeners.containsKey(msg.amType())){
             Queue<MessageListener> queueListeners = this.messageListeners.get(msg.amType());
             if (queueListeners!=null && queueListeners.isEmpty()==false){
                 Iterator<MessageListener> iterator = queueListeners.iterator();
                 while(iterator.hasNext()){
                     if (listener.equals(iterator.next())){
                         iterator.remove();
                     }
                 }
             }
         }
         
         // register to all defined nodes
         for(Integer node : this.perNodeListeners.keySet()){
             this.deregisterListener(node, msg, listener);
         }
     }
 
     /**
      * Deregister from particular connected node
      * @param node
      * @param msg
      * @param listener 
      */
     @Override
     public void deregisterListener(int node, Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener) {
         // register for particular node here
         // null?
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             // node id needs to be in database
             if (this.perNodeListeners.containsKey(node)==false){
                 log.error("Cannot register listener for node: " + node + "; not found in database");
                 return;
             }
             
             MultipleMessageListenerSubComponent subComponent = this.perNodeListeners.get(node);
             subComponent.deregisterListener(msg, listener);
         } catch (Exception e){
             log.error("Exception occurred when de-registering message listener", e);
         }
     }
 
    
     
     /**
      * perform hard reset to this object = clears entire memory
      */
     @Override
     public synchronized void reset(){
         this.queue.clear();
         
         log.info("MesageListener queues was flushed");
     }
     
     /**
      * After gateway change is needed to register as listener for all registered
      * AMtypes. MyMessageListener is registered as listener to tinyos listener.
      */
     @Override
     public synchronized void reregisterListeners(){
         if (this.perNodeListeners == null || this.perNodeListeners.isEmpty()) {
             return;
         }
 
         // tell to accept new 
         for (Integer nodeid : this.perNodeListeners.keySet()) {
             try {
                 MultipleMessageListenerSubComponent subComponents = this.perNodeListeners.get(nodeid);
                 subComponents.acceptListeners(messageListeners);
             } catch (Exception e) {
                 log.error("Exception when re-registering message listeners", e);
             }
         }
     }
     
     /**
      * The thread either executes tasks or sleep.
      */
     @Override
     public void run() {
          // do in infitite loop
         MessageReceived tmpMessage = null;            
         Iterator<MessageListener> iterator = null;
 
          while(true){
             // yield for some time, processor rest
             try {
                 Thread.sleep(0, 200);
             } catch (InterruptedException ex) {
                 log.warn("Cannot sleep in message listener loop", ex);
             }
             
             // shutdown
             if (this.shutdown == true){
                 log.info("MyMessageReceiver shutting down.");
                 
                 return;
             }
 
             //  nulltest on queue itself - should not happen
             if (queue==null){
                 log.error("Queue is null - should not happen, queue is final, "
                         + "initialized in constructor. Has to exit...");
                 
                 break;
             }
 
              // yield for some time iff is queue empty
              if (queue == null || queue.isEmpty()) {
                  this.pause(5);
                  continue;
              }
 
              //  nulltest
              if (queue == null) {
                  continue;
              }
 
              // check queue size
              if (queue.size() > MAX_QUEUE_SIZE_TO_RESET) {
                  queue.clear();
 
                  log.warn("Warning! Input queue had to be flushed out!"
                          + "Overflow, size was greater than " + MAX_QUEUE_SIZE_TO_RESET);
                  continue;
              }
 
              // if is nonempty, select first element
              if (!queue.isEmpty()) {
                  tmpMessage = queue.remove();
              } else {
                  tmpMessage = null;
              }
 
              // end of synchronization block, check if we have some message
              if (tmpMessage == null) {
                  continue;
              }
 
              // check listener for existence
              if (!(tmpMessage instanceof MessageReceived)) {
                  log.error("Message is not instance of messageReceived - please inspect it");
                  continue;
              }
 
              // determine message received
              Message msg = tmpMessage.getMsg();
              if (msg == null) {
                  // empty message, continue
                  log.error("Message is empty in envelope", tmpMessage);
                  continue;
              }
 
              // iterate over listeners list and notify each listener 
              // in serial manner
              boolean large = queue.size() > 1500;
              if (large) {
                  log.info(this.getName() + "; XX qsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
              }
              
              // signalize to particular node id from which it arrived
              Integer amtype = Integer.valueOf(msg.amType());
              int nodeId = tmpMessage.getGateway();
              if (this.perNodeListeners.containsKey(nodeId)==false){
                  // nobody... thus need to report to global registered listeners
                  
                  // get list of listeners interested in receiving messages of
                  // this AMtype
                  Queue<MessageListener> listenersList = messageListeners.get(amtype);
                  if (listenersList == null || listenersList.isEmpty()) {
                      // list is null || empty, cannot forward to anyone
                      continue;
                  }
                  
                  // iterate over all registered listeners
                  for(MessageListener curListener : listenersList){
                      if (curListener == null) {
                          continue;
                      }
                      
                      // notify this listener,, separate try block - exception
                      // can be thrown, this exception affects only one listener
                      try {
                          // notify here
                          curListener.messageReceived(tmpMessage.getI(), msg, tmpMessage.getTimeReceivedMili());
                      } catch (Exception e) {
                          log.error("Exception during notifying listener", e);
                          continue;
                      }
                  }
              } else {
                 // somebody is gateway -> redirect to it
                 MultipleMessageListenerSubComponent subComponent = this.perNodeListeners.get(nodeId);
                 subComponent.messageReceived(tmpMessage);
              }
 
              // set message to null to release it from memory for garbage collector
              tmpMessage.setMsg(null);
              tmpMessage = null;
         }
     }
 
     /**
      * Return size of message queue to send
      *
      * @return
      */
     @Override
     public synchronized int getQueueLength(){
         return this.queue != null ? this.queue.size() : 0;
     }
 
     /**
      * Entry method for helper message receivers - contains gateway information
      * For each gateway is registered different helper class
      * 
      * @param gateway
      * @param i
      * @param msg
      * @param mili 
      */
     public void messageReceivedFromHelper(int gateway, int i, Message msg, long mili){
         // blocking?
         if (this.dropingPackets) return;
 
         MessageReceived msgReceiveed = new MessageReceived(i, msg);
         msgReceiveed.setGateway(gateway);
         //msgReceiveed.setTimeReceivedMili(mili);
         msgReceiveed.setTimeReceivedMili(msg.getMilliTime());
         this.queue.add(msgReceiveed);
     }
     
     @Override
     public void messageReceived(int i, Message msg) {
         // blocking?
         if (this.dropingPackets) return;
         
         MessageReceived msgReceiveed = new MessageReceived(i, msg);
         msgReceiveed.setTimeReceivedMili(System.currentTimeMillis());
         this.queue.add(msgReceiveed);
     }
 
 
     @Override
     public void disconnectNode(ConnectedNode nh, boolean resetQueues) {
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         // exists?
         if (this.perNodeListeners.containsKey(nh.getNodeId())==false){
             log.error("Cannot disconnect from nodeId: " + nh.getNodeId() + "; not connected to");
             return;
         }
         
         // disconnect means disconnect from moteIf, if we want to destruct, call another
         MultipleMessageListenerSubComponent subComponent = this.perNodeListeners.get(nh.getNodeId());
         subComponent.disconnectNode(null);
     }
     
     @Override
     public void disconnectNode(ConnectedNode nh, Properties props) {
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         boolean reset=false;
         if (props!=null){
             String resetQueues = props.getProperty("reset", "false");
             try {
                 reset = Boolean.parseBoolean(resetQueues);
             } catch(Exception e){
                 log.warn("Cannot convert resetQueues to boolean", e);
             }
         }
         
         this.disconnectNode(nh, reset);
     }
     
     /**
      * Add node to registered nodes.
      * If node exists - connect is called on that node (reconnect is probably initiated)
      * If node does not exist then new node is created and registered default listeners
      * 
      * @param nh
      * @param props 
      */
     @Override
     public void connectNode(ConnectedNode nh, Properties props) {
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         // if no such node in register, init new one
         if (this.perNodeListeners.containsKey(nh.getNodeId())==false){
             // no such per node listener, connect to it
             MultipleMessageListenerSubComponent subComponent = 
                     new MultipleMessageListenerSubComponent(this, nh);
             
             // accept global listeners
             subComponent.acceptListeners(this.messageListeners);
         } else {
             // such node is already in register -> reconnect
             MultipleMessageListenerSubComponent subComponent = this.perNodeListeners.get(nh.getNodeId());
             subComponent.connectNode(null);
         }
         
         boolean reset=false;
         if (props!=null){
             String resetQueues = props.getProperty("reset", "false");
             try {
                 reset = Boolean.parseBoolean(resetQueues);
             } catch(Exception e){
                 log.warn("Cannot convert resetQueues to boolean", e);
             }
         }
         
         // reset internal queues
         if (reset) {
             this.reset();
         }
         
         // re-register listeners here
         this.reregisterListeners();
         
         // reset queues again
         if (reset) {
             this.reset();
         }
         
         log.info("Added nodeid: " + nh.getNodeId() + " to MessageListener");
     }
     
     /** 
      * Perform reconnect on given subcomponent - keeps registered listeners
      */
     @Override
     public void reconnectNode(ConnectedNode nh) {
         if (nh==null){
             throw new NullPointerException("Cannot reconnect to null node");
         }
         
         // has such node in register?
         if (this.perNodeListeners.containsKey(nh.getNodeId())==false){
             // no such node - not for me?
             return;
         }
         
         MultipleMessageListenerSubComponent subComp = this.perNodeListeners.get(nh.getNodeId());
         subComp.disconnectNode(null);
         subComp.connectNode(null);
     }
     
     /**
      * =========================================================================
      *
      * GETTERS + SETTERS
      *
      * =========================================================================
      */
 
     public long getTimeLastMessageSent() {
         return timeLastMessageSent;
     }
 
     public void setTimeLastMessageSent(Long timeLastMessageSent) {
         this.timeLastMessageSent = timeLastMessageSent;
     }
 
     @Override
     public boolean isShutdown() {
         return shutdown;
     }
 
     @Override
     public void setShutdown(boolean shutdown) {
         this.shutdown = shutdown;
     }
 
     @Override
     public boolean isDropingPackets() {
         return dropingPackets;
     }
 
     @Override
     public void setDropingPackets(boolean dropingPackets) {
         this.dropingPackets = dropingPackets;
     }
 
     /**
      * Indexing class for decision whether for received note with given AMTYPE
      * current listener accepts this new message based on amtype, nodeid
      */
     protected class AMTypeNodeIDComposite{
         private int amtype;
         private int nodeid;
 
         public AMTypeNodeIDComposite() {
         }
         
         public AMTypeNodeIDComposite(int amtype, int nodeid) {
             this.amtype = amtype;
             this.nodeid = nodeid;
         }
 
         public int getAmtype() {
             return amtype;
         }
 
         public void setAmtype(int amtype) {
             this.amtype = amtype;
         }
 
         public int getNodeid() {
             return nodeid;
         }
 
         public void setNodeid(int nodeid) {
             this.nodeid = nodeid;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final AMTypeNodeIDComposite other = (AMTypeNodeIDComposite) obj;
             if (this.amtype != other.amtype) {
                 return false;
             }
             if (this.nodeid != other.nodeid) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 79 * hash + this.amtype;
             hash = 79 * hash + this.nodeid;
             return hash;
         }
     }
 }
