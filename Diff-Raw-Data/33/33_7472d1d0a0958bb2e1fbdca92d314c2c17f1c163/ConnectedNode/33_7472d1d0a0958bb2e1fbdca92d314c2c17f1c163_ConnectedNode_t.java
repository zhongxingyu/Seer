 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.nodes;
 
 import fi.wsnusbcollect.App;
 import fi.wsnusbcollect.nodeCom.MessageListener;
 import fi.wsnusbcollect.nodeCom.MessageListenerInterface;
 import fi.wsnusbcollect.nodeCom.MessageSenderInterface;
 import fi.wsnusbcollect.nodeCom.MessageSentListener;
 import fi.wsnusbcollect.nodeCom.MessageToSend;
 import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
 import fi.wsnusbcollect.usb.NodeConfigRecord;
 import fi.wsnusbcollect.usb.USBarbitrator;
 import java.util.Properties;
 import java.util.concurrent.TimeoutException;
 import net.tinyos.message.Message;
 import net.tinyos.message.MoteIF;
 import net.tinyos.packet.BuildSource;
 import net.tinyos.packet.PhoenixSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Represents directly connected node - can be listened on local wired interface
  * directly. Base station is example of connected node.
  * In testbed with more nodes connected, each this object will represent each directly
  * connected node.
  * 
  * 
  * @author ph4r05
  */
 public class ConnectedNode extends AbstractNodeHandler implements NodeHandler{
     private static final Logger log = LoggerFactory.getLogger(ConnectedNode.class);
     
     // node object (contains node id, platform, position, readings, timers)
     private GenericNode nodeObj;
     
     // hold information about connected node - data from database/auto detection
     // if node is not connected, this object has no special meaning (holds only
     // node id, product and description - can be determined from nodeObj. Only usefull
     // field can be serial)
     private NodeConfigRecord nodeConfig;
     
     // message sender bound to specified node
     private MessageSenderInterface msgSender;
     
     // message listener bound to specified node
     private MessageListenerInterface msgListener;
 
     // mote interface for specific node
     private MoteIF moteIf;
     
     private boolean resetQueues=true;
     
     /**
      * Proxy method to nodeObj. Throws nullpointer exception if nodeObj is null
      * @return 
      */
     @Override
     public int getNodeId(){
         if (this.nodeObj==null){
             throw new NullPointerException("Cannot determine node ID, nodeObj is null");
         }
         
         return this.nodeObj.getNodeId();
     }
 
     /**
      * Checks whether object contains non null required fields
      * @return 
      */
     @Override
     public boolean isCorrect() {
         return this.nodeObj!=null;
     }
 
     /**
      * Destructing 
      * @throws Throwable 
      */
     @Override
     protected void finalize() throws Throwable {
         this.shutdown();
         super.finalize();
     }
     
     /**
      * Disconnects moteif
      */
     public void disconnect(){
         // shutdown sender
         if (this.msgSender!=null){
             this.msgSender.disconnectNode(this, this.resetQueues);
         }
         
         // shutdown listener
         if (this.msgListener!=null){
             this.msgListener.disconnectNode(this, this.resetQueues);
         }
         
         // shutdown moteif
         if (this.moteIf!=null){
             this.moteIf.getSource().shutdown();
             this.moteIf = null;
         }
     }
     
     /**
      * disconnect + connect
      */
     public void reconnect(){
         this.disconnect();
         this.connectToNode();
     }
     
     /**
      * Returns whether is possible to reset this node by means of hw.
      * 
      * @extension: in senslab we can reset node with external command executed
      * and there is no need to be connected directly - really depends on system
      * and on mote type.
      * 
      * @return 
      */
     public boolean hwresetPossible(){
         // necessarry objects 
         if (this.nodeObj==null 
                 || this.nodeConfig==null 
                 || this.nodeObj.getPlatform()==null) return false;
         
         // is node connected physically?
         // need to reach node directly
         // 
         
         return this.nodeObj.getPlatform().canHwReset();
     }
     
     /**
      * performs hw reset if applicable. 
      * If yes, then disconnect all threads
      * perform hw reset, wait 1000 miliseconds, register all threads, wait 500 miliseconds
      * 
      */
     public void hwreset(){
         if (this.hwresetPossible()==false) return;
         USBarbitrator usbArbitrator = App.getRunningInstance().getUsbArbitrator();
         
         // node reset is not able here (emulator?)
         if (usbArbitrator.isAbleNodeReset()==false) return;
         
         // disconnect all nodes
         this.disconnect();
         // reset nodes
         Properties properties = new Properties();
         if (this.nodeConfig.getDeviceAlias()!=null && this.nodeConfig.getDeviceAlias().isEmpty()==false){
             properties.setProperty("preferDevice", this.nodeConfig.getDeviceAlias());
         }
         
         // reset node with USB arbitrator - handles environment differences
         boolean ok = usbArbitrator.resetNode(this, properties);
         
         if (ok){
             this.connectToNode();
         } else {
             log.error("Cannot restart node from node handler");
         }
     }
     
     /**
      * shutdown all threads spawned for this node
      * sender, listener, mote interface listener
      */
     @Override
     public void shutdown(){   
         // shutdown sender
         log.debug("Shutting down node: " + this.getNodeId());
         
         if (this.msgSender!=null && this.msgSender.isAlive()){
             this.msgSender.shutdown();
             log.info("MsgSender interrupted");
         }
         
         // shutdown listener
         if (this.msgListener!=null && this.msgListener.isAlive()){
             this.msgListener.shutdown();
             log.info("MsgListener interrupted");
         }
         
         // shutdown moteif
         if (this.moteIf!=null){
             this.moteIf.getSource().shutdown();
             this.moteIf = null;
         }
         
         log.info("NodeID: " + this.getNodeId() + " shutted down");
     }
     
     /**
      * Starts messagelistener, msgsender threads
      */
     public synchronized void start(){
         // shutdown sender
         if (this.msgListener!=null && this.msgListener.isAlive()==false){
             this.msgListener.start();
             log.info("MsgListener thread started for NodeID: " + this.getNodeId());
         }
         
         // shutdown listener
         if (this.msgSender!=null && this.msgSender.isAlive()==false){
             this.msgSender.start();
             log.info("MsgSender thread started for NodeID: " + this.getNodeId());
         }
     }
 
     /**
      * Method delegated to message sender if exists
      * @return 
      */
     @Override
     public synchronized boolean canAddMessage2Send() {
         if (this.msgSender==null){
             log.info("Message sender is null => cannot add message to send");
             return false;
         }
         return msgSender.canAdd();
     }
 
     @Override
     public synchronized void addMessage2Send(MessageToSend msg) {
         if (this.isMessageSenderFit()==false){
             log.warn("Cannot send message to null message sender");
             throw new NullPointerException("Cannot use null message sender");
         }
         try {
             msgSender.add(msg);
         } catch (TimeoutException ex) {
             log.error("Cannot add message", ex);
         }
     }
 
     /**
      * Connected node can send message to everyone if supports base station feature
      * 
      * @param target
      * @param msg
      * @param text
      * @param listener
      * @param listenerKey 
      */
     public synchronized void addMessage2Send(int target, Message msg, String text, MessageSentListener listener, String listenerKey) {
         if (this.isMessageSenderFit()==false){
             log.warn("Cannot send message to null message sender");
             throw new NullPointerException("Cannot use null message sender");
         }
         msgSender.add(this.getNodeId(), target, msg, text, listener, listenerKey);
     }
     
     @Override
     public synchronized void addMessage2Send(Message msg, String text, MessageSentListener listener, String listenerKey) {
         if (this.isMessageSenderFit()==false){
             log.warn("Cannot send message to null message sender");
             throw new NullPointerException("Cannot use null message sender");
         }
         msgSender.add(this.getNodeId(), this.getNodeId(), msg, text, listener, listenerKey);
     }
 
     /**
      * Connected node can send message to everyone if supports base station feature
      * 
      * @param target
      * @param msg
      * @param text 
      */
     public synchronized void addMessage2Send(int target, Message msg, String text) {
         if (this.isMessageSenderFit()==false){
             log.warn("Cannot send message to null message sender");
             throw new NullPointerException("Cannot use null message sender");
         }
         msgSender.add(this.getNodeId(), target, msg, text);
     }
     
     @Override
     public synchronized void addMessage2Send(Message msg, String text){
         if (this.isMessageSenderFit()==false){
             log.warn("Cannot send message to null message sender");
             throw new NullPointerException("Cannot use null message sender");
         }
         msgSender.add(this.getNodeId(), this.getNodeId(), msg, text);
     }
 
     /**
      * Call to msgListener.reset()
      * @see MyMessageListener.reset();
      */
     public synchronized void resetMsgListener() {
         msgListener.reset();
     }
 
     /**
      * Delegates 
      * @param msg
      * @param listener 
      */
     public synchronized void registerMessageListener(Message msg, MessageListener listener) {
         msgListener.registerListener(msg, listener);
     }
 
     public synchronized int getReceivedQueueLength() {
         return msgListener.getQueueLength();
     }
 
     public synchronized void deregisterMessageListener(Message msg, MessageListener listener) {
         msgListener.deregisterListener(msg, listener);
     }
 
     public void setDropingReceivedPackets(boolean dropingPackets) {
         msgListener.setDropingPackets(dropingPackets);
     }
     
     /**
      * Connects to given source (by connection string) and if OK returns mote interface
      * @param source
      * @return 
      */
     public static MoteIF getConnectionToNode(String source){
         // build custom error mesenger - store error messages from tinyos to logs directly
         TOSLogMessenger messenger = new TOSLogMessenger();
         PhoenixSource phoenix = BuildSource.makePhoenix(source, messenger);
         MoteIF moteInterface = null;
         
         // phoenix is not null, can create packet source and mote interface
         if (phoenix != null) {
             // loading phoenix
             moteInterface = new MoteIF(phoenix);
         }
         
         return moteInterface;
     }
     
     /**
      * Connects to node specified by connection string
      * @param source
      * @return 
      */
     public boolean connectToNode(String source){
         MoteIF connectionToNode = ConnectedNode.getConnectionToNode(source);
         if (connectionToNode==null){
             log.warn("Cannot connect to device: " + source);
             return false;
         }
         
         this.setMoteIf(connectionToNode);
         return true;
     }
     
     /**
      * Connect to node by config
      * @return 
      */
     public boolean connectToNode(){
         if (this.nodeConfig == null){
             log.warn("Cannot connect to node without node config object");
             return false;
         }
         
         return this.connectToNode(this.nodeConfig.getConnectionString());
     }
     
     
     /**
      * Returns flag determining whether is message listener ready for use
      * @return 
      */
     public boolean isMessageListenerFit(){
         return this.msgListener!=null;
     }
     
     /**
      * Returns flag determining whether is message sender ready for use
      * @return 
      */
     public boolean isMessageSenderFit(){
         return this.msgSender!=null;
     }
     
     /**
      * Returns original message listener for this node
      * @return 
      */
     public MessageListenerInterface getMsgListener() {
         return msgListener;
     }
 
     public void setMsgListener(MessageListenerInterface msgListener) {
         this.msgListener = msgListener;
     }
 
     /**
      * Returns original message sender for this node
      * @return 
      */
     public MessageSenderInterface getMsgSender() {
         return msgSender;
     }
 
     public void setMsgSender(MessageSenderInterface msgSender) {
         this.msgSender = msgSender;
     }
 
     public NodeConfigRecord getNodeConfig() {
         return nodeConfig;
     }
 
     public void setNodeConfig(NodeConfigRecord nodeConfig) {
         this.nodeConfig = nodeConfig;
     }
 
     public GenericNode getNodeObj() {
         return nodeObj;
     }
 
     public void setNodeObj(GenericNode nodeObj) {
         if (this.nodeObj!=null 
                 && (this.msgListener!=null || this.msgSender!=null || this.moteIf!=null)){
             // print warning message - inconsistency
             // @decide: should be thrown exception?
             log.warn("You should not set different node object here, if previous was"
                     + "not null. It can cause inconsistency with msglistener/msgsender/moteif");
         }
         this.nodeObj = nodeObj;
     }
 
     public MoteIF getMoteIf() {
         return moteIf;
     }
 
     /**
      * Sets new mote interface, re-registers for msgsender, msglistener.
      * This operation will flush all packet queues in specified objects.
      * @param moteIf 
      */
     public void setMoteIf(MoteIF moteIf) {
         this.moteIf = moteIf;
         
         Properties props = new Properties();
         props.setProperty("resetQueues", Boolean.valueOf(this.resetQueues).toString());
         
         // set new node for sender/listener if not null
         if (this.msgListener!=null){
             this.msgListener.connectNode(this, props);
         }
         
         if (this.msgSender!=null){
             this.msgSender.connectNode(this, props);
         }
     }
 
     @Override
     public String toString() {
         return "ConnectedNode{" + "nodeObj=" + nodeObj + ", nodeConfig=" + nodeConfig + ", msgSender=" + msgSender + ", msgListener=" + msgListener + ", moteIf=" + moteIf + '}';
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final ConnectedNode other = (ConnectedNode) obj;
         if (this.nodeObj != other.nodeObj && (this.nodeObj == null || !this.nodeObj.equals(other.nodeObj))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 83 * hash + (this.nodeObj != null ? this.nodeObj.hashCode() : 0);
         return hash;
     }
 
     @Override
     public void close() {
         this.shutdown();
         this.moteIf=null;
     }
 
     /**
      * Returns type of node handler - connected node
      * @return 
      */
     @Override
     public int getType() {
         return AbstractNodeHandler.NODE_HANDLER_CONNECTED;
     }
 
     @Override
     public boolean canListen() {
         return this.moteIf!=null && this.msgListener!=null;
     }
 
     @Override
     public boolean canSend() {
         return this.moteIf!=null && this.msgSender!=null;
     }
 
     @Override
     public void updateLastSeen(long mili) {
         if (this.nodeObj==null){
             log.error("Cannot update - null object");
             return;
         }
         
         // if updating with older value than current, leave it
         if (this.nodeObj.getLastSeen() > mili) {
             return;
         }
         
         this.nodeObj.setLastSeen(mili);
     }
 
     public boolean isResetQueues() {
         return resetQueues;
     }
 
     public void setResetQueues(boolean resetQueues) {
         this.resetQueues = resetQueues;
     }
 }
