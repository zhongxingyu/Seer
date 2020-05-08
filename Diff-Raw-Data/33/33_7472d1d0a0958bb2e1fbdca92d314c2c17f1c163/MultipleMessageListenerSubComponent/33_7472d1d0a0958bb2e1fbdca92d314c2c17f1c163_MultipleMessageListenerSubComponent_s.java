 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.nodeCom;
 
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import net.tinyos.message.Message;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Subcomponent for multiple message listener connected to one particular gateway
  * 
  * @author ph4r05
  */
 public class MultipleMessageListenerSubComponent implements MessageListener  {
     private static final Logger log = LoggerFactory.getLogger(MultipleMessageListenerSubComponent.class);
     
     /**
      * Need to keep parent component - to be able to call message received
      */
     private MultipleMessageListener parent;
     
     /**
      * Directly connected node
      */
     private ConnectedNode node;
     
     /**
      * Queue of message listeners registered for particular AMType
      * AMType->Queue<Listener>
      */
     protected ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>> messageListeners = 
             new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>>(1);
 
     public MultipleMessageListenerSubComponent(MultipleMessageListener parent, ConnectedNode node) {
         this.parent = parent;
         this.node = node;
     }
     
     /**
      * Translate method - prepend gateway id to multiple message listener
      * Called from tiny os via message received
      * @param i
      * @param msg
      * @param mili 
      */
     @Override
     public void messageReceived(int i, Message msg, long mili) {
         if (node==null) return;
         parent.messageReceivedFromHelper(node.getNodeId(), i, msg, mili);
     }
 
     /**
      * Call directly from tiny os
      * @param i
      * @param msg 
      */
     @Override
     public void messageReceived(int i, Message msg) {
         this.messageReceived(i, msg, System.currentTimeMillis());
     }
 
     /**
      * Warning!
      * Message received from multiple listener from queue
      * 
      * @param mReceived 
      */
     public void messageReceived(MessageReceived mReceived){
         if (mReceived==null) return;
         int amtype = mReceived.getMsg().amType();
         // do we have an interest to receive such message?
         if (this.messageListeners.containsKey(amtype)==false) return;
         
         // here we have an interest in given message
         // get list of listeners interested in receiving messages of this AMtype
         ConcurrentLinkedQueue<MessageListener> listenersList = messageListeners.get(amtype);
         if (listenersList==null || listenersList.isEmpty()) return;
         
          // iterate over all registered listeners
          for(MessageListener curListener : listenersList){
              if (curListener == null) {
                  continue;
              }
              
              // notify this listener,, separate try block - exception
              // can be thrown, this exception affects only one listener
              try {
                  // notify here
                  curListener.messageReceived(mReceived.getI(), mReceived.getMsg(), mReceived.getTimeReceivedMili());
              } catch (Exception e) {
                  log.error("Exception during notifying listener", e);
                  continue;
              }
          }
     }
     
     /**
      * unregister message listener
      */
     public synchronized void deregisterListener(Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener){
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             // is message in translate queue?
             Integer amtype = msg.amType();
             AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
             
             // register to mapper
             amtypeMapper.registerMessage(msg);
             
             // is amtype established?
             if (this.messageListeners.containsKey(amtype)==false){
                 // no, cannot be in underlying list
                 return;
             }
             
             // get list
             ConcurrentLinkedQueue<MessageListener> listenersList = this.messageListeners.get(amtype);
             if (listenersList==null || listenersList.isEmpty()){
                 // list empty => cannot be in
                 return;
             }
             
             Iterator<MessageListener> iterator = listenersList.iterator();
             while(iterator.hasNext()){
                 MessageListener curListener = iterator.next();
                 if (curListener==null){
                     iterator.remove();
                     continue;
                 }
                 
                 if (curListener.equals(listener)){
                     iterator.remove();
                     break;
                 }
             }
             
             // push back to map
             this.messageListeners.put(amtype, listenersList);
             
             // if here some listener was probably removed, if list is empty or null,
             // delete it completly and unregister from physical interface
             if (listenersList==null || listenersList.isEmpty()){
                 this.messageListeners.remove(amtype);
                 
                 if (node!=null){
                     this.node.getMoteIf().deregisterListener(msg, this);
                 }
             }
         } catch (Exception e){
             log.error("Exception occurred when de-registering message listener", e);
         }
     }
     
     /**
      * Register message listener
      * If same listener (.equals()) is registered to same message type, request
      * is ignored.
      */
     public synchronized void registerListener(Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener){
         if (this.node==null) return;
         
         // register for particular node here
         // null?
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             //
             // Phase - AMTYpe mapping
             //
             
             // amtype convert, register to static to be able to translate by it
             // is message in translate queue?
             Integer amtype = msg.amType();
             AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
             amtypeMapper.registerMessage(msg);
             
             //
             // Phase - list of listeners
             //
             boolean registeredInMoteIF=false;
             ConcurrentLinkedQueue<MessageListener> queueListeners = null;
             if (this.messageListeners.containsKey(amtype)){
                 queueListeners = this.messageListeners.get(amtype);
                 registeredInMoteIF=true;
             } 
             
             // if null here: 1. not found in messageListeners, 2. found and is null
             if (queueListeners==null){
                 queueListeners = new ConcurrentLinkedQueue<MessageListener>();
             }
             
             boolean isAlreadyInQueue = false;
             Iterator<MessageListener> iterator = queueListeners.iterator();
             while(iterator.hasNext()){
                 MessageListener curMessageListener = iterator.next();
                 
                 // check if is null - remove it
                 if (curMessageListener==null){
                     iterator.remove();
                     log.error("Null message listener found in queue, removing");
                     continue;
                 }
                 
                 // equals? already set, not induce redundancy
                 if (curMessageListener.equals(listener)){
                     isAlreadyInQueue = true;
                     break;
                 }
             }
 
             // if here, listener is not in, set it
             if (isAlreadyInQueue==false){
                 queueListeners.add(listener);
             }
 
             // update map
             this.messageListeners.put(amtype, queueListeners);
             
             //
             // Phase - real registration to mote interface
             //
             if (registeredInMoteIF==false){
                 this.node.getMoteIf().registerListener(msg, this);
             }
         } catch(Exception e){
             log.error("Problem during registering message listener", e);
         }
     }
     
     /**
      * Unregister all listeners from physical interface while keeping them in map
      */
     protected void unregisterListeners(ConnectedNode cn){
          if (cn==null || cn.getMoteIf()==null){
             log.warn("Cannot deregister listeners to invalid node");
             return;
         } 
          
         // iterate over all amtypes and deregister them all
         AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
         for(Integer amtype : this.messageListeners.keySet()){
             if (amtypeMapper.hasAMType(amtype)==false){
                 log.warn("Global amtype mapper has no mapping for amtype: " + amtype);
                 continue;
             }
 
             // unregister secure
             try {
                 cn.getMoteIf().deregisterListener(amtypeMapper.getMessage(amtype), this);
             } catch(Exception e){
                 log.error("Problem during unregistering listener", e);
             }
         }
     }
     
     /**
      * Registers all mapped listeners to physical interface
      */
     protected void registerListeners(ConnectedNode cn){
         if (cn==null || cn.getMoteIf()==null){
             log.warn("Cannot register listeners to invalid node");
             return;
         } 
         
         // perform re-registration for listener
         // iterate over all amtypes and deregister them all
         AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
         for(Integer amtype : this.messageListeners.keySet()){
             if (amtypeMapper.hasAMType(amtype)==false){
                 log.warn("Global amtype mapper has no mapping for amtype: " + amtype);
                 continue;
             }
             
             cn.getMoteIf().registerListener(amtypeMapper.getMessage(amtype), this);
         }
     }
     
     /**
      * Disconnect from currently connected node
      * @param nh
      * @param props 
      */
     public boolean disconnectNode(Properties props){
         if (this.node==null) return false;
         
         // really unregister if possible.
          // If connects to another node in future, we don't want to receive
         // messages from old listeners
         this.unregisterListeners(this.node);        
         return true;
     }
     
     /**
      * Connect given node to this sender, if already in, nothing is done if
      * MoteIF equals already set one
      * 
      * @param nh
      * @param props 
      */
     public boolean connectNode(Properties props){
         if (this.node==null || this.node.getMoteIf()==null){
             throw new NullPointerException("Cannot connect to null or invalid connectedNode");
         }
         
         // register all mapped listeners
         this.registerListeners(this.node);
         return true;
     }
     
     /**
      * Accepts global message listeners - new ones are re-registered
      * @param listeners 
      */
     public void acceptListeners(Map<Integer, Queue<MessageListener>> listeners){
         if (listeners==null || listeners.isEmpty()){
             return;
         }
         
         AMType2MessageMapper amtypeMapper = AMType2MessageMapper.getInstance();
         
         // iterate over given amtypes
         for(Integer amtype : listeners.keySet()){
             // for re-registering we need to know corresponding TinyOS message
             if (amtypeMapper.hasAMType(amtype)==false){
                 log.warn("Global amtype mapper has no mapping for amtype: " + amtype);
                 continue;
             }
             
             Message message = amtypeMapper.getMessage(amtype);
             
             // is current amtype in my map?
             Queue<MessageListener> tmpListenersForAMtype = null;
             
             if (this.messageListeners.containsKey(amtype)){
                 tmpListenersForAMtype = this.messageListeners.get(amtype);
             } else {
                 tmpListenersForAMtype = new LinkedList<MessageListener>();
             }
             
             Queue<MessageListener> providedListenersQueue = listeners.get(amtype);
             for(MessageListener newMessageListener : providedListenersQueue){
                 // is already defined?
                 if (tmpListenersForAMtype.contains(newMessageListener)) continue;
                 // if here new message listener is not registered - register it
                 this.registerListener(message, newMessageListener);
             }
         }
     }
     
     public MultipleMessageListener getParent() {
         return parent;
     }
 
     public void setParent(MultipleMessageListener parent) {
         this.parent = parent;
     }
 
     public ConnectedNode getNode() {
         return node;
     }
 
     public Map<Integer, ConcurrentLinkedQueue<MessageListener>> getMessageListeners() {
         return Collections.unmodifiableMap(messageListeners);
     }
 }
