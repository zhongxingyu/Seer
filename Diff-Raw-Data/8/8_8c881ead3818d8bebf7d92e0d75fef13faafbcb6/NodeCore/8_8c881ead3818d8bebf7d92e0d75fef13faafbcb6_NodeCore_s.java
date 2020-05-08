 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package node;
 
 import compute.ADUV_constants;
 import compute.DirectoryInterface;
 import compute.NodeInterface;
 import java.io.Serializable;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 import structs.Message;
 import structs.MessageTypeInterface;
 import structs.NodeInfo;
 import structs.NodeStateInterface;
 import structs.TokenColorInterface;
 import utils.Log;
 
 /**
  * Class with main algorithm (ADUV)
  * @author pavel
  */
 public class NodeCore extends UnicastRemoteObject implements NodeInterface,Serializable,ADUV_constants,
         MessageTypeInterface,TokenColorInterface,NodeStateInterface{
     
     private static final long serialVersionUID = -1937161867341487222L;
 
     /**
      * My color type
      */
     private Color myColor;
     
     /**
      * Actual node state
      */
     private NodeState myState;
     
     /**
      * Queue for messages
      */
     private Queue<Message> messageQueue;
     
     /**
      * Node information (ID in general)
      */
     private NodeInfo nodeInfo;
     
     /**
      * Log object
      */
     private Log log;
     
     /**
      * Directory with nodes
      */
     private DirectoryInterface directory;
     
     /**
      * Registry with RMI
      */
     private Registry registry;
     
     /**
      * Generated work amount
      */
     private int workAmount;
     
     /**
      * Work sent indicator
      */
     private boolean workSent;
     private boolean workStopAccepted;
 
     /**
      * Start token sent
      */
     private boolean startTokenSent;
     
     /**
      * Logical clock of the process
      * -> incremented with send process
      */
     private int clock;
     
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setDirectory(DirectoryInterface directory) {
         this.directory = directory;
     }
     
     /**
      * Logging object
      * @param log 
      */
     public void setLog(Log log) {
         this.log = log;
     }
     
     /**
      * Startup function
      */
     public NodeCore(NodeInfo nodeInfo) throws RemoteException{
         super();
         //1] Init message queue
         this.messageQueue = new LinkedList<Message>();
         //2]set node info
         this.nodeInfo = nodeInfo;
         //3]work send --> false
         this.workSent = false;
         //4]token start
         this.startTokenSent = false;
         this.workStopAccepted = false;
         //5]init clocks
         this.clock = 0;
     }
     
     /**
      * Run node with command line
      */
     public void run() throws RemoteException, NotBoundException{
         //set my state & color
         this.myColor = Color.WHITE;
         
         while(true){
             try {
                
                 //state --> based on the work amount
                 if(this.workAmount == 0){
                     this.myState = NodeState.PASSIVE;
                     //this.log.logNode("Work state is PASSIVE.", -1, this.nodeInfo.getID());
                 }else{
                     this.myState = NodeState.ACTIVE;
                     //this.log.logNode("New work state is ACTIVE", -1, this.nodeInfo.getID());
                 }
                 
                 //start queue processing
                 processQueue();        
                 
                 //start random work
                 startRandomWork();
                            
                 //initiate work stop
                 if(this.nodeInfo.getID() == 0 && !this.startTokenSent && 
                         workStopAccepted && workAmount ==0){
                     
                     this.startTokenSent = true;
                     sendStartToken();
                 }
                 //////////////////////////////////////////ú
                 if(this.workAmount == 0){
                     Thread.sleep(SLEEP_TIME);
                 }
                 
             } catch (InterruptedException ex) {
                ex.printStackTrace();
             }
         }
     }
     
     /**
      * Inserts message into queue
      * @param message - message to insert
      * @throws RemoteException 
      */
     @Override
     public synchronized void putMessage(Message message) throws RemoteException {
         Message messageTmp = new Message();
         messageTmp.setClock(message.getClock());
         messageTmp.setColor(message.getColor());
         messageTmp.setPayload(message.getPayload());
         messageTmp.setType(message.getType());
         
         this.messageQueue.add(message);
     }
 
     /**
      * Queue process --> until it is not empty
      */
     private void processQueue() throws RemoteException, NotBoundException, InterruptedException {
         if(this.messageQueue.isEmpty()){
             Thread.sleep(1000);
             return;
         }
         
         while(!this.messageQueue.isEmpty()){
             Message mess = this.messageQueue.poll();
             MessageType type = mess.getType();
             //synchronize my logical time
             this.setNewClock(mess.getClock());
             
             switch(type){
                 case START:
                     this.log.logNode("Starting work ...", -1, this.nodeInfo.getID());
                     this.workAmount = new Random().nextInt(SLEEP_TIME_MODULE);
                     this.log.logNode("Generated work amount is "+this.workAmount+".", clock, this.nodeInfo.getID());
                     this.workStopAccepted = true;
                     break;
                     
                 case GET_WORK: //some work with random time
                     this.log.logNode("Work has been received: "+mess.toString()+".", clock, this.nodeInfo.getID());
                     this.workAmount  += Integer.parseInt(mess.getPayload());
                     this.log.logNode("New work amount is "+this.workAmount+".", clock, this.nodeInfo.getID());
                     break;
                     
                 case TOKEN:
                     this.log.logNode("Token has been received "+mess.toString(), clock, this.nodeInfo.getID());
                     processToken(mess);
                     break;
                     
                 case STOP:
                     this.directory.logout(this.nodeInfo);
                     this.log.logNode("Exiting", clock, this.nodeInfo.getID());
                     System.exit(0);
                     break;
                 case WORK_TERMINATED:
                     this.log.logNode("Work has been terminated :).", clock, this.nodeInfo.getID());
                     this.init();
                     break;
                default:
                    //do nothing by default
                    break;
             }
         }
     }
 
     /**
      * Start random computing
      */
     private void startRandomWork() throws RemoteException, NotBoundException {
         try {
             //check if you have some work
             if(workAmount == 0) return;
             
             //simulate working
             Thread.sleep(WORK_AMOUNT_UNIT*1000);
             
             //change variable with consumed work
             int oldWork = this.workAmount;
             this.workAmount -= WORK_AMOUNT_UNIT;
             if(this.workAmount < 0){
                 this.workAmount = 0;
             }
             
             //log it
             this.log.logNode("Working :  "+oldWork+"->"+this.workAmount+".", clock, this.nodeInfo.getID());
                 
                 ///////////////////////////////////////////////////////
                 //if some work has been sent -> end it
                 boolean sendWork = new Random().nextBoolean();
                 if(workSent || sendWork){
                     //this.log.logNode("Work has been allready sent.", -1, this.nodeInfo.getID());
                     return;
                 }
    
                             //send work to node with lower id
                 if(this.directory.getNodeCount() > 1){
                     int randInd;
                     
                     //generate random number
                     if(this.directory.getNodeCount() == 2){
                         randInd = this.getNeighbourID();
                     }else{
                         do{
                             randInd = new Random().nextInt(this.directory.getNodeCount() - 1 );                 
                         }while(this.nodeInfo.getID() == randInd); 
                     }
                 this.workSent = true;
                 //send work
                 int RandomWork = new Random().nextInt(SLEEP_TIME_MODULE);
                 Message workMess = new Message(MessageType.GET_WORK, -1, Color.WHITE,Integer.toString(RandomWork));
                 sendMessageToProcess(workMess,randInd);
                 this.log.logNode("Work has been sent to "+randInd+"->"+workMess.toString()+".", clock, this.nodeInfo.getID());
                 
                 //change my color -> if you are sending to the host with lower id
                 if(this.nodeInfo.getID() > randInd){
                     this.myColor = Color.BLACK;
                     this.log.logNode("Receiver ID was smaller than mine -> I become to be BLACK.", clock, this.nodeInfo.getID());
                 }
                 
             }
             
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * This method sends a message to the process; this process also sets logical clock
      * @param workMess
      * @param address 
      */
     private void sendMessageToProcess(Message workMess,int address) throws RemoteException, NotBoundException {
         String nodeName = NODE_NAME_PREFIX+address;
         //set clock
         this.clock ++; //increment clock
         workMess.setClock(clock);
         
         //message prepared
         this.log.logNode("Sending message to processs "+address+"-->"+workMess.toString(), clock, this.nodeInfo.getID());
         NodeInterface nodeDirectory = 
                 (NodeInterface)this.registry.lookup(nodeName);
         nodeDirectory.putMessage(workMess);
     }
 
     /**
      * Token processing & sending
      * @param mess  - message
      */
     private void processToken(Message mess) throws RemoteException, NotBoundException {
         Message outMess = new Message();
         outMess.setType(MessageType.TOKEN);
        this.log.logNode("Received message -> "+mess.toString(),clock, 0);
         
         if(mess.getColor() == Color.BLACK){
             //black token
             if(this.nodeInfo.getID() == 0){
                 //primary process
                 this.log.logNode("BLACK node has been received.",clock, 0);
                 this.startTokenSent = false;
             }else{
                 outMess.setColor(Color.BLACK);
             }
         }else{
             //white token
             
             if(this.nodeInfo.getID() == 0){
                 //primary process --> received white node;all stops working
                 this.log.logNode("Work has been terminated :).", clock, 0);
                 sendTerminatedMessage();
                 this.init();
             }else{
                 
                 //i am not the primary process
                 if(this.myState == NodeState.PASSIVE){
                     //I don't have any work
                     outMess.setColor(myColor);
                 }else{
                     //I have work
                     outMess.setColor(Color.BLACK);
                 }              
             }
         }
         
         //get node count and send token if node count is bigger than one
         if(this.directory.getNodeCount() == 1) return;
         
         //send data
         if(this.nodeInfo.getID() != 0){
             int neighbourID = getNeighbourID();
             this.sendMessageToProcess(outMess,neighbourID);
         }
         
         //reset my color
         this.myColor = Color.WHITE;
     }
 
     /**
      * Returns next neighbour id
      * @return neighbour id
      */
     private int getNeighbourID() throws RemoteException {
         int retID = 0;
         NodeInfo[] nodes = this.directory.listNodes();
         
         //find my position in array
         int i;
         for(i=0;i<nodes.length;i++){
             if(nodes[i].getID() == this.nodeInfo.getID()){
                 break;
             }
         }
         
         //succesor is 
         retID = i+1;
         
         //array indexing starts with zero
         if(nodes.length == retID){
             retID = 0;
         }
         
         return retID;
     }
 
     /**
      * This method sends start token
      */
     private void sendStartToken() throws RemoteException, NotBoundException {
         if(this.directory.getNodeCount() > 1){
             int neighbourID = this.getNeighbourID();
             Message mess = new Message();
             mess.setType(MessageType.TOKEN);
             mess.setColor(Color.WHITE);
             this.sendMessageToProcess(mess, neighbourID);
             this.log.logNode("Sending TOKEN.", clock, this.nodeInfo.getID());
         }
     }
 
     /**
      * Method used for the init.
      */
     private void init() {
         //init boolean control variables            
         this.startTokenSent = false;
         this.workAmount = 0;
         //remove all elements
         this.messageQueue.removeAll(this.messageQueue);
         this.workSent = false;
         this.myColor = Color.WHITE;
         this.workStopAccepted = false;
         this.clock = 0;
         this.log.logNode("Node initialized.", clock, this.nodeInfo.getID());
     }
 
     /**
      * Method for terminated message spam.
      */
     private void sendTerminatedMessage() throws RemoteException, NotBoundException {
         Message messTerm=new Message();
         messTerm.setType(MessageType.WORK_TERMINATED);
         this.log.logNode("Sending termination message to all nodes.", clock, this.nodeInfo.getID());
         
         for(NodeInfo node : this.directory.listNodes()){    
                 this.sendMessageToProcess(messTerm, node.getID());
         }
     }
     
     /**
      * This method sets new clock time from the message
      */
     public void setNewClock(int messageTime){
         if(messageTime >= this.clock){
             this.clock = messageTime + 1;
         }
     }
     
 }
