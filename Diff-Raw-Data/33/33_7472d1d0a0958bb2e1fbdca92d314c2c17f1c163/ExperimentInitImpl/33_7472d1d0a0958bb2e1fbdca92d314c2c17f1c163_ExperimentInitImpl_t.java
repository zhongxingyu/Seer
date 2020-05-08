 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.experiment;
 
 import fi.wsnusbcollect.App;
 import fi.wsnusbcollect.db.ExperimentMetadata;
 import fi.wsnusbcollect.db.USBconfiguration;
 import fi.wsnusbcollect.messages.CollectionDebugMsg;
 import fi.wsnusbcollect.messages.CommandMsg;
 import fi.wsnusbcollect.messages.CtpInfoMsg;
 import fi.wsnusbcollect.messages.CtpReportDataMsg;
 import fi.wsnusbcollect.messages.CtpResponseMsg;
 import fi.wsnusbcollect.messages.CtpSendRequestMsg;
 import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
 import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
 import fi.wsnusbcollect.nodeCom.MultipleMessageListener;
 import fi.wsnusbcollect.nodeCom.MultipleMessageSender;
 import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
 import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import fi.wsnusbcollect.nodes.GenericNode;
 import fi.wsnusbcollect.nodes.NodeHandler;
 import fi.wsnusbcollect.nodes.NodePlatform;
 import fi.wsnusbcollect.nodes.NodePlatformFactory;
 import fi.wsnusbcollect.nodes.SimpleGenericNode;
 import fi.wsnusbcollect.usb.NodeConfigRecord;
 import fi.wsnusbcollect.usb.USBarbitrator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.annotation.Resource;
 import net.tinyos.message.MoteIF;
 import net.tinyos.packet.BuildSource;
 import net.tinyos.packet.PhoenixSource;
 import org.ini4j.Ini;
 import org.ini4j.Wini;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Experiment initialization/helper class
  * @author ph4r05
  */
 @Repository
 @Transactional
 public class ExperimentInitImpl implements ExperimentInit {
     private static final Logger log = LoggerFactory.getLogger(ExperimentInit.class);
     
     @Resource(name="experimentRecords")
     protected ExperimentRecords2DB expRecords;
     
     @Resource(name="nodeHandlerRegister")
     protected NodeHandlerRegister nodeReg;
     
     //@Autowired
     protected ExperimentCoordinator expCoordinator;
     
     @Resource(name="USBarbitrator")
     protected USBarbitrator usbArbitrator;
     
     // main experiment metadata
     protected ExperimentMetadata expMeta;
     
     // config file prepared by application
     protected Wini config;
     
     private int status=0;
     
     @Resource(name="experimentParameters")
     private ExperimentParameters params;
     
     public static final String INISECTION_MOTELIST="motelist";
     public static final String INISECTION_METADATA="experimentMetadata";
     public static final String INISECTION_PARAMETERS="experimentParameters";
     
     /**
      * Loads configuration to experiment
      * @deprecated 
      */
     public void loadConfig(){
         
     }
     
     /**
      * Process settings from parameters/config file
      */
     public List<NodeConfigRecord> getNodes2connect(){
         // default include/exclude motelist from parameters
         // init file has precedense
         String moteInclude = App.getRunningInstance().getUseMotesString();
         String moteExclude = App.getRunningInstance().getIgnoreMotesString();
         
         // read config file
         this.config = App.getRunningInstance().getIni();
         if (this.config!=null){            
             // include/exclude reading
             if (this.config.containsKey(INISECTION_MOTELIST)){
                 Ini.Section motelist = this.config.get(INISECTION_MOTELIST);
                 
                 if (motelist.containsKey("include")){
                     moteInclude = motelist.get("include");
                 }
                 
                 if (motelist.containsKey("exclude")){
                     moteExclude = motelist.get("exclude");
                 }
             }
         }
         
         // config file/arguments parsing, node selectors, get final set of nodes to connect to
         return this.usbArbitrator.getNodes2connect(moteInclude, moteExclude);
     }
     
     /**
      * Helper method - re-programs connected nodes specified by parameters/config file.
      * Only path to directory with makefile is required. Then is executed
      * make telosb install,X bsl,/dev/mote_telosX
      * 
      * @extension: add multithreading to save time required for reprogramming
      * 
      * @param makeDir  absolute path to makefile directory with mote program
      */
     @Override
     public void reprogramConnectedNodes(String makefileDir){
         List<NodeConfigRecord> nodes2connect = this.getNodes2connect();
         this.usbArbitrator.reprogramNodes(nodes2connect, makefileDir);
     }
     
     @Override
     public void initClass() {
         log.info("Class initialized");
         this.expCoordinator = App.getRunningInstance().getExpCoord();
         
         // stores metadata about experiment
         expMeta = new ExperimentMetadata();
         expMeta.setDatestart(new Date());
         // get usb configuration
         USBconfiguration currentConfiguration = this.usbArbitrator.getCurrentConfiguration();
         expMeta.setNodeConfiguration(currentConfiguration);
         
         // owner of experiment, determine from system
         String username = System.getProperty("user.name");
         if (username!=null){
             log.info("Found that user running this experiment is: " + username);
             expMeta.setOwner(username);
         }
         
         // read config file
         this.config = App.getRunningInstance().getIni();
         if (this.config!=null){
             // read experiment metadata - to be stored in database
             if (this.config.containsKey(INISECTION_METADATA)==false){
                 log.error("Config file has to contain section " + INISECTION_METADATA);
                 throw new IllegalArgumentException("Config file has to contain section " + INISECTION_METADATA);
             }
             
             // store config file as raw
             expMeta.setConfigFile(App.getRunningInstance().getConfigFileContents());
             
             // get metadata section from ini file
             Ini.Section metadata = this.config.get("experimentMetadata");
             
             // experiment group
             if (metadata.containsKey("group")){
                 expMeta.setExperimentGroup(metadata.get("group"));
             } else {
                 expMeta.setExperimentGroup("default");
                 log.warn("Cannot found experiment group, using default");
             }
             
             // experiment name
             if (metadata.containsKey("name")){
                 expMeta.setName(metadata.get("name"));
             } else {
                 log.error("INI file must contain experiment name field");
                 throw new IllegalArgumentException("INI file must contain experiment name field");
             }
             
             // annotation
             if (metadata.containsKey("annotation")){
                 expMeta.setDescription(metadata.get("annotation"));
             }
             
             // keywords
             if (metadata.containsKey("keywords")){
                 expMeta.setKeywords(metadata.get("keywords"));
             }
             
             // read parameters from config file
             this.params.load(this.config);
         }
         
         // config file/arguments parsing, node selectors, get final set of nodes to connect to
         List<NodeConfigRecord> nodes2connect = this.getNodes2connect();
         // init connected nodes - builds handler and connecto to them
         this.initConnectedNodes(null, nodes2connect);
         //write information about directly connected nodes and its configuration (nodes2connect)
         ArrayList<String> nodeList = new ArrayList<String>(nodes2connect.size());
         Iterator<NodeConfigRecord> ncrIt = nodes2connect.iterator();
         while(ncrIt.hasNext()){
             NodeConfigRecord ncr = ncrIt.next();
             nodeList.add(ncr.getSerial());
         }
         
         // set connected nodes as list
         expMeta.setConnectedNodesUsed(nodeList);
         
         // persist meta
         this.expRecords.storeExperimentMeta(expMeta);
     }
 
     @Override
     public void deinitExperiment() {
         this.closeExperiment();
     }
     
     /**
      * Experiment is closing now... update timers in database
      */
     public void closeExperiment(){
         if (this.expMeta==null){
             throw new NullPointerException("Current experiment metadata is null");
         }
         
         this.expRecords.closeExperiment(expMeta);
     }
     
     /**
      * Updates real experiment start in miliseconds - in configuration
      * @param mili 
      */
     @Override
     public void updateExperimentStart(long mili){
         if (this.expMeta==null){
             throw new NullPointerException("Current experiment metadata is null");
         }
         
         this.expRecords.updateExperimentStart(expMeta, mili);
     }
 
     /**
      * Stores experiment configuration to database
      * @deprecated 
      */
     public void storeConfig(){
         // store experiment metadata to database
         
         
         // store experiment parameters to database
         this.params.storeToDatabase(expMeta);
     }
     
     @Override
     public void initEnvironment() {
         log.info("Environment initialized");
         
         // here can init shell console
     }
 
     /**
      * Initialize connected nodes here
      * @param props
      * @param ncr 
      */
     @Override
         public void initConnectedNodes(Properties props, List<NodeConfigRecord> ncr) {
         log.info("initializing connected nodes here");
         if (ncr==null){
             throw new NullPointerException("NCR is null");
         }
         
         MultipleMessageSender mMsgSender = null;
         Map<Integer, MoteIF> connectedNodes = new HashMap<Integer, MoteIF>();
         Integer defaultGateway=null;
         
         int nodesPerOneListener=16;
         List<List<ConnectedNode>> listForListener = new LinkedList<List<ConnectedNode>>();
         List<ConnectedNode> curListForListener = new LinkedList<ConnectedNode>();
         listForListener.add(curListForListener);
         
         Iterator<NodeConfigRecord> iterator = ncr.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord nextncr = iterator.next();
             System.out.println("Node to connect to: " + nextncr.toString());
             
             if (nextncr.getNodeId()==null){
                 log.warn("Cannot work with node without defined node ID, please "
                         + "define its node id in database: " + nextncr.toString());
                 continue;
             }
             
             // determine platform
             NodePlatform platform = NodePlatformFactory.getPlatform(nextncr);
             // try to connect to node
             MoteIF connectToNode = this.connectToNode(nextncr.getConnectionString());
             if (connectToNode==null){
                 log.warn("Cannot connect to node: " + nextncr.toString());
                 continue;
             }
             
             // build generic node info
             GenericNode gn = new SimpleGenericNode(true, nextncr.getNodeId());
             gn.setPlatform(platform);
             
             ConnectedNode cn = new ConnectedNode();
             cn.setNodeObj(gn);
             cn.setNodeConfig(nextncr);
             cn.setMoteIf(connectToNode);
             
 //            // message listener
 //            MyMessageListener listener = new MyMessageListener(connectToNode);
 //            listener.setDropingPackets(true);
 //            
 //            // message sender
 //            MessageSender sender = new MessageSender(connectToNode);
 //            
 //            cn.setMsgListener(listener);
 //            cn.setMsgSender(sender);
             
             // store for multiple packet sender
             connectedNodes.put(cn.getNodeId(), connectToNode);
             defaultGateway = cn.getNodeId();
             
             // multiple nodes for listener
             curListForListener.add(cn);
             if (curListForListener.size() >= nodesPerOneListener){
                 curListForListener = new LinkedList<ConnectedNode>();
                 listForListener.add(curListForListener);
             }
 
             // add to map
             this.nodeReg.put(cn);
             
             System.out.println("Initialized connected node: " + cn.toString());
         }
         
         // init multiple sender
         mMsgSender = new MultipleMessageSender(defaultGateway, connectedNodes.get(defaultGateway));
         mMsgSender.setAllGateways(connectedNodes, defaultGateway, true);
         mMsgSender.start();
         log.info("MultipleMessage sender started");
         
         Collection<NodeHandler> nodeHandlers = this.nodeReg.values();
         for(NodeHandler curNH : nodeHandlers){
             if (!(curNH instanceof ConnectedNode)) continue;
             final ConnectedNode cn = (ConnectedNode) curNH;
             cn.setMsgSender(mMsgSender);
             
             log.info("MessageSender updated for node: " + cn.getNodeId());
         }
         
         // init multiple receiver
         log.info("Initializing multiple message listener");
         int listenerCount = 1;
         for(List<ConnectedNode> curXListForListener: listForListener){
             log.info("MultipleMessageListener id: " + listenerCount);
             MultipleMessageListener mMsgListener = new MultipleMessageListener(" block: " + listenerCount);
             mMsgListener.setDropingPackets(true);
             
             for(ConnectedNode cn : curXListForListener){
                 mMsgListener.connectNode(cn, null);
                 cn.setMsgListener(mMsgListener);
                 
                 // add listening to packets here to separate DB listener
                 ExperimentData2DB dbForNode = App.getRunningInstance().getAppContext().getBean("experimentData2DB", ExperimentData2DB.class);
                 dbForNode.setExpMeta(expMeta);
                 log.info("DB for node is running: " + dbForNode.isRunning() + "; for node: " + cn.getNodeId());
             
                 cn.registerMessageListener(new CommandMsg(), dbForNode);
                 cn.registerMessageListener(new NoiseFloorReadingMsg(), dbForNode);
                 cn.registerMessageListener(new MultiPingResponseReportMsg(), dbForNode);
                 cn.registerMessageListener(new CtpReportDataMsg(), dbForNode);
                 cn.registerMessageListener(new CtpResponseMsg(), dbForNode);
                 cn.registerMessageListener(new CtpSendRequestMsg(), dbForNode);
                 cn.registerMessageListener(new CtpInfoMsg(), dbForNode);
                 cn.registerMessageListener(new CollectionDebugMsg(), dbForNode);
                 log.info("Listener for node: " + cn.getNodeId());
             }
             
             listenerCount+=1;
         }
         
         // starting all threads
         System.out.println("Starting all threads");
         this.nodeReg.startAll();
         
         System.out.println("Initialized");
         status=1;
     }
     
     /**
      * Connects to given source and if OK returns mote interface
      * @param source
      * @return 
      */
     public MoteIF connectToNode(String source){
         // build custom error mesenger - store error messages from tinyos to logs directly
         TOSLogMessenger messenger = new TOSLogMessenger();
         // instantiate phoenix source
         PhoenixSource phoenix = BuildSource.makePhoenix(source, messenger);
         MoteIF moteInterface = null;
         
         // phoenix is not null, can create packet source and mote interface
         if (phoenix != null) {
             // loading phoenix
             moteInterface = new MoteIF(phoenix);
         }
         
         return moteInterface;
     }
 
     @Override
     public String toString() {
         return "ExperimentInitImpl{" + "status=" + status + '}';
     }
 
     public NodeHandlerRegister getNodeReg() {
         return nodeReg;
     }
 
     public void setNodeReg(NodeHandlerRegister nodeReg) {
         this.nodeReg = nodeReg;
     }
 
     public ExperimentCoordinator getExpCoordinator() {
         return expCoordinator;
     }
 
     public void setExpCoordinator(ExperimentCoordinatorImpl expCoordinator) {
         this.expCoordinator = expCoordinator;
     }
 
 //    public void setConsole(Console console) {
 //        this.console = console;
 //    }
 
     @Override
     public ExperimentMetadata getExpMeta() {
         return expMeta;
     }
 
     public void setExpMeta(ExperimentMetadata expMeta) {
         this.expMeta = expMeta;
     }
 
     public ExperimentParameters getParams() {
         return params;
     }
 
     public void setParams(ExperimentParameters params) {
         this.params = params;
     }   
     
     
 }
