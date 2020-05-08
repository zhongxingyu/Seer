 package net.es.oscars.coord.runtimepce;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.net.URL;
 
 import java.lang.ref.WeakReference;
 
 import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;
 import net.es.oscars.coord.req.InterDomainEventRequest;
 import net.es.oscars.utils.sharedConstants.PCERequestTypes;
 import net.es.oscars.utils.soap.OSCARSSoapService;
 import org.apache.log4j.Logger;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.configuration.HierarchicalConfiguration;
 import org.apache.commons.configuration.SubnodeConfiguration;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
 import org.quartz.JobDetail;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 
 import net.es.oscars.coord.common.Coordinator;
 import net.es.oscars.coord.common.PathComputationMutex;
 import net.es.oscars.coord.jobs.PCERuntimeActionCleanerJob;
 
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.coord.req.CancelRequest;
 import net.es.oscars.coord.req.CoordRequest;
 import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
 import net.es.oscars.utils.sharedConstants.StateEngineValues;
 import net.es.oscars.utils.config.ContextConfig;
 import net.es.oscars.utils.config.ConfigException;
 import net.es.oscars.utils.svc.ServiceNames;
 import net.es.oscars.utils.topology.PathTools;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.coord.actions.CoordAction;
 import net.es.oscars.coord.actions.ReservationCompletedForwarder;
 import net.es.oscars.coord.actions.RMUpdateStatusAction;
 import net.es.oscars.coord.actions.RMStoreAction;
 import net.es.oscars.coord.actions.CommittedForwarder;
 import net.es.oscars.coord.actions.CreateResvForwarder;
 import net.es.oscars.coord.actions.ModifyResvForwarder;
 import net.es.oscars.coord.workers.NotifyWorker;
 import net.es.oscars.logging.ErrSev;
 import net.es.oscars.logging.OSCARSNetLogger;
 import net.es.oscars.logging.ModuleName;
 
 
 /**
  * PCERuntimeAction is an Action that is responsible for reading the PCE configuration file.
  * It keeps a static hashMap of all the runTimeActions and associated GRIs.
  * It is responsible for creating proxyActions for all the Aggregators and PCEs that are described
  * in the PCE configuration file.
  *
  * @author lomax
  *
  */
 public class PCERuntimeAction extends CoordAction <PCEData, PCEData> implements Runnable {
 
     private static final long serialVersionUID = 1439115737928915954L;
     private static final String moduleName = ModuleName.PCERUNTIME;
 
     public static final String PCE_CONFIGURATION = "pce-config.xml";
     private static Logger LOG = Logger.getLogger(PCERuntimeAction.class.getName());
 
     private OSCARSNetLogger netLogger = null;
     private static boolean pceServiceStarted = false;
     private HashMap<String, ProxyAction> pces = new HashMap<String, ProxyAction>();
     private static HashMap<String, WeakReference<PCERuntimeAction>> pceRuntimes = new HashMap<String, WeakReference<PCERuntimeAction>>();
     private static Thread serverThread = null;
     private static XMLConfiguration config = null;
     private String requestType = null;
     private String transactionId = null; // TODO put in coordAction
     private static ContextConfig cc = null;
     private static PathComputationMutex pceMutex = new PathComputationMutex();
 
     // Start the background thread that will prune empty entries in the actions map.
     private static long CLEANER_REPEAT = (15 * 60 * 1000); // 15 minutes
     
     static {
         SimpleTrigger jobTrigger = new SimpleTrigger("PCERuntimeAction.Cleaner",
                                                      null,
                                                      SimpleTrigger.REPEAT_INDEFINITELY,
                                                      PCERuntimeAction.CLEANER_REPEAT);
         
         JobDetail     jobDetail  = new JobDetail("PCERuntimeAction.Cleaner", null, PCERuntimeActionCleanerJob.class);
         jobDetail.setVolatility(false);
         
         try {
             Coordinator.getInstance().getScheduler().scheduleJob(jobDetail, jobTrigger);
         } catch (SchedulerException e) {
             LOG.error("Could not schedule PCERuntimeActionCleaner");
             e.printStackTrace();
         } catch (OSCARSServiceException e) {
             LOG.error("Could not schedule PCERuntimeActionCleaner");
             e.printStackTrace();
         }     
     }
     
     /**
      * Constructor - called from the execute methods in CreateReservationRequest, cancelRequest, modifyRequest,
      *                  CommittedEventAction and PCERuntimeAction.setResultsData
      *
      * @param name Set by caller to identify this action : e.g., createReservation-Query-PCERuntimeAction
      * @param request the parent CoordRequest
      * @param data requestData to be passed between PCEs, if null may be set by setRequestData
      * @param transactionId a global id for this IDC request, was passed to Coordinator in the request message
      * @param requestType pceCreate, pceCancel, pceModify, pceCreateCommit, pceModifyCommit
      */
     @SuppressWarnings("unchecked")
     public PCERuntimeAction (String name, 
                              CoordRequest request, 
                              PCEData data, 
                              String transactionId,
                              String requestType) {
         
         super (name, request, data);
         LOG = Logger.getLogger(PCERuntimeAction.class.getName());
         this.netLogger = new OSCARSNetLogger(moduleName, transactionId);
         this.netLogger.setGRI(request.getGRI());
         OSCARSNetLogger.setTlogger(this.netLogger);
         LOG.debug(netLogger.start("constructor", requestType));
         this.requestType = requestType;
         this.transactionId = transactionId;
         cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
         cc.setServiceName(ServiceNames.SVC_COORD);
         // Read the PCE configuration file
         try {
             PCERuntimeAction.readConfiguration(this.netLogger);
         } catch (OSCARSServiceException e) {
             LOG.error(netLogger.error("constructor",ErrSev.MAJOR,
                                        "caught OSCARSServcieException " + e.getMessage()));
             System.exit(-1);
         }
         // Make sure that the PCERuntimeService is running
         if (PCERuntimeAction.serverThread == null) {
             PCERuntimeAction.serverThread = new Thread (this);
             PCERuntimeAction.serverThread.start();
         }
     }
 
     public static ProxyAction getProxyAction (String gri, String pceName, String requestType) {
 
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String event = "getProxyction";
         PCERuntimeAction pceRuntime = PCERuntimeAction.getPCERuntimeAction(gri, requestType);
         if (pceRuntime == null) {
             // This should not happen.
             LOG.error(netLogger.error(event,ErrSev.MAJOR, 
                                       "ProxyAction.getProxyAction unexpected error: no PCERuntime for  gri= " + 
                                       gri + " pceName= " + pceName + " requestType= " + requestType));
             throw new RuntimeException ("PCERuntimeAction is missing for GRI: " + gri + " requestType= " + requestType);
         }
 
         ProxyAction pce = null;
         synchronized (pceRuntime.pces) {
             pce = pceRuntime.pces.get(gri + "-" + pceName + "-" + requestType);
             // pce may be null if it does not exist
         }
         return pce;
     }
 
     public static void setProxyAction (String gri, String pceName, String requestType, ProxyAction pce) {
         PCERuntimeAction pceRuntime = PCERuntimeAction.getPCERuntimeAction(gri, requestType);
         if (pceRuntime == null) {
             // This should not happen.
             throw new RuntimeException ("PCERuntimeAction is missing for GRI: " + gri + " requestType= " + requestType);
         }
         synchronized (pceRuntime.pces) {
             pce = pceRuntime.pces.put (gri + "-" + pceName+ "-" + requestType, pce);
         }
     }
 
     public static PCERuntimeAction getPCERuntimeAction (String gri, String requestType) {
         
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String event = "getPCERunTimeAction";
         PCERuntimeAction pceRuntime =  null;
         String key = gri + "-" + requestType;
         synchronized (PCERuntimeAction.pceRuntimes) {
             WeakReference<PCERuntimeAction> ref =  PCERuntimeAction.pceRuntimes.get(key);
             if (ref != null) {
                 pceRuntime = ref.get();
                 if (pceRuntime == null) {
                     LOG.error(netLogger.error(event, ErrSev.MAJOR, "No PCERuntimeAction (1) for GRI: " + 
                                               gri + " requestType= " + requestType));
                     // No runtime for this GRI This should not happen
                     throw new RuntimeException ("No PCERuntimeAction for GRI: " + 
                                                  gri + " requestType= " + requestType);
                 }
             } else {
                 LOG.error(netLogger.error(event, ErrSev.MAJOR,"No PCERuntimeAction (2) for GRI: " + 
                                           gri + " requestType= " + requestType));
                 throw new RuntimeException ("No PCERuntimeAction for GRI: " + gri + " requestType= " + requestType);
             }
         }
         return pceRuntime;
     }
 
     public void setPCERuntimeAction (String requestType) {
         String key = this.getCoordRequest().getGRI() + "-" + requestType;
         synchronized (PCERuntimeAction.pceRuntimes) {
             PCERuntimeAction.pceRuntimes.put (key, new WeakReference<PCERuntimeAction>(this));
         }
     }
 
     /**
      * Invoke the PCE through the PCE generic client. The request is sent asynchronously.
      * The reply will be returned in a PCEReply message sent from the PCE back to PCERuntimeSoapHandler. 
      */
     public void execute() {
         // Set the netLogger for this thread from the one stored in the constructor
         OSCARSNetLogger.setTlogger(this.netLogger); 
         String event = "execute";
         LOG.debug(this.netLogger.start(event,this.requestType));
  
         // First make sure that there is not another PCERuntimeAction that holds the PCE lock
 
         try {
             if(!PCERuntimeAction.pceMutex.get(this)){
                 //lock not available so give up thread. 
                 //Mutex has added this job to the queue.
                 return;
             }
         } catch (InterruptedException e) {
             LOG.error(netLogger.error(event, ErrSev.MAJOR,"Cannot acquire mutex. GRI= " +
                                       this.getCoordRequest().getGRI() + " requestType= " + requestType));
             this.fail(e);
             return;
         }
 
         // Register this PCERuntime instance
         this.setPCERuntimeAction (this.requestType);
         // Create the graph of PCE modules.
         this.createPCE (PCERuntimeAction.config.configurationAt("PCE"), null);
         // Set PCE Data to the children PCEProxyAction
         PCEData pceData = this.getRequestData();
         //normalize pce data
         PCEReqFormatter.getInstance().normalize(pceData);
 
         for (CoordAction<PCEData,PCEData> pce : this) {
             pce.setRequestData(pceData);
         }
 
         // Create, set and invoke the RMUpdateStatus
         String newState = null;
         if (this.requestType.equals(PCERequestTypes.PCE_CREATE)){
             newState = StateEngineValues.INPATHCALCULATION;
         } else if (this.requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
             newState = StateEngineValues.INCOMMIT;
         } else if (this.requestType.equals(PCERequestTypes.PCE_CANCEL)){
             newState = StateEngineValues.INCANCEL;
         } else if (this.requestType.equals(PCERequestTypes.PCE_MODIFY) ||
                    this.requestType.equals(PCERequestTypes.PCE_MODIFY_COMMIT)){
             newState = StateEngineValues.INMODIFY;
         }
         LOG.debug(netLogger.getMsg(event,"Calling RMUpdateStatus with state "  + newState));
         RMUpdateStatusAction rmUpdateStatusAction = new RMUpdateStatusAction (this.getName() + "-RMUpdateStatusAction",
                                                                               this.getCoordRequest(),
                                                                               this.getCoordRequest().getGRI(),
                                                                               newState);
         rmUpdateStatusAction.execute();
         
         if (rmUpdateStatusAction.getResultData() != null) {
             LOG.debug(netLogger.getMsg(event,"State was set to " + rmUpdateStatusAction.getResultData().getStatus()));
         } else { // shouldn't happen
             LOG.error(netLogger.error(event,ErrSev.MINOR, "rmUpdateStatus resultData is null."));
         }
         this.executed();
         LOG.debug(this.netLogger.end(event));
     }
 
     public void executed() {
         super.executed();
     }
     /**
      * Creates either a PCEProxyAction, or an AggProxyAction.  Links it to the parentProxy
      * if there is one, or to the PCERuntime action if this is the first action
      *
      * @param parent -previous proxyAction, may be null if this is the first action to be executed
      * @param name - name of service, used for tracking messages, taken from configuration file
      * @param pathTag
      * @param neededTags
      * @param role - PCE or AGGREGATOR
      * @param proxyEndpoint - address of proxy callback for PCE replies
      * @param pceEndpoint - address of PCE service to be called
      * @param transactionId - global id for this IDC transaction
      * @return - either a  new PCEProxyAction or AggProxyAction
      */
     private ProxyAction createProxyAction (ProxyAction parent,
                                            String name,
                                            String pathTag,
                                            List<String> neededTags,
                                            ProxyAction.Role role,
                                            String proxyEndpoint,
                                            String pceEndpoint,
                                            String transactionId) {
         ProxyAction proxyAction = null;
 
         if (role == ProxyAction.Role.PCE) {
             proxyAction = new PCEProxyAction (parent,
                                               this.getCoordRequest(),
                                               this,
                                               name,
                                               pathTag,
                                               proxyEndpoint,
                                               pceEndpoint,
                                               transactionId,
                                               this.requestType);
 
         } else if (role == ProxyAction.Role.AGGREGATOR) {
             proxyAction = new AggProxyAction (parent,
                                               this.getCoordRequest(),
                                               this,
                                               name,
                                               pathTag,
                                               neededTags,
                                               proxyEndpoint,
                                               pceEndpoint,
                                               transactionId,
                                               this.requestType);
         }
         if (parent != null) {
             // Add this PCE Proxy to its parent
             parent.add (proxyAction);
         } else {
             // This is the top PCE. Add it to the PCERuntimeAction
             this.add (proxyAction);
         }
         return proxyAction;
     }
 
     /**
      * CreatePCE called from PCRuntimeAction.execute and recursively within createPCE
      * Steps through the PCE configuration (previously loaded by readConfiguration),
      * recursively creating PCEProxyActions for each of the configured PCEs or Aggregators.
      *
      * @param conf - configuration structure previously loaded by readConfiguration from PCE_CONFIGURATION
      * @param parentPce - the parent PCE proxyAction, null if this is the coordRequest
      */
     @SuppressWarnings("unchecked")
     private void createPCE (HierarchicalConfiguration conf, 
                             ProxyAction parentPce) {
         if (conf == null) {
             // Safety
             return;
         }
         String pceName       = conf.getString("Bindings.Name");
         String role          = conf.getString("Bindings.Role");
         String pceEndpoint   = conf.getString("Bindings.Endpoint");
         String proxyEndpoint = conf.getString("Bindings.ProxyEndpoint");
         String pathTag       = conf.getString("Bindings.PathTag");
         List<String> neededTags = conf.getList("Bindings.NeedsPathTag");
 
         ProxyAction.Role pceRole = null;
         if (role.equals("Aggregator")) {
             pceRole = ProxyAction.Role.AGGREGATOR;
         } else if (role.equals("PCE")) {
             pceRole = ProxyAction.Role.PCE;
         }
 
         // Create the Proxy Action object for this PCE
         ProxyAction myProxyAction = createProxyAction (parentPce,
                                                        pceName,
                                                        pathTag,
                                                        neededTags,
                                                        pceRole,
                                                        proxyEndpoint,
                                                        pceEndpoint,
                                                        this.transactionId);
         SubnodeConfiguration aggNode = null;
         try {
             aggNode = conf.configurationAt("Aggregates");
         } catch (java.lang.IllegalArgumentException e) {
             // No Aggregate token
             return;
         }
         if (aggNode == null) {
             // This PCE does not have any child PCE. Return;
             return;
         }
         List<SubnodeConfiguration> pceNodes =  aggNode.configurationsAt("PCE");
         if (pceNodes == null) {
             return;
         }
         for (SubnodeConfiguration node: pceNodes) {
             this.createPCE (node, myProxyAction);
         }
     }
 
     private static void readConfiguration (OSCARSNetLogger netLogger) throws OSCARSServiceException {
         try {
             if (PCERuntimeAction.config == null) {
                 String fileName = cc.getFilePath(ServiceNames.SVC_PCE,
                                                  cc.getContext(),
                                                  PCERuntimeAction.PCE_CONFIGURATION);
                 LOG.debug(netLogger.start("readConfig","from file " + fileName));
                 PCERuntimeAction.config = new XMLConfiguration(fileName);
             }
         } catch (ConfigException e) {
             throw new OSCARSServiceException(e.getMessage());
        } catch (ConfigurationException ex){
             throw new OSCARSServiceException(ex.getMessage());
         }
     }
 
     public void run() {
         try {
             this.startPCERuntimeService();
         } catch (OSCARSServiceException e) {
             LOG.error(this.netLogger.error("run",ErrSev.MAJOR,
                                            "PCERuntimeAction.run caught exception:" + e.getMessage()));
         }
     }
 
     private synchronized void startPCERuntimeService () throws OSCARSServiceException {
         String event = "startPCERuntimeService";
         LOG.debug(netLogger.start(event));
         if (! PCERuntimeAction.pceServiceStarted) {
             try{
                 URL url = new URL("file:" + cc.getFilePath(ServiceNames.SVC_PCE,"server-cxf.xml"));
                 LOG.debug(netLogger.getMsg (event, "URL is" + url.toString()));
                 OSCARSSoapService.setSSLBusConfiguration(url);
             } catch (Exception ex) {
                 LOG.error(netLogger.getMsg(event,
                         "Exception in PCERuntimeAction.startPCERuntimeService" +
                                     ex.getMessage()));
             }
             PCERuntimeSoapServer.getInstance().startServer(false);
             PCERuntimeAction.pceServiceStarted = true;
         }
     }
 
     /**
      * Called by PCEProxyAction or AggProxyActions when the path operation is complete.
      * The path operation should be one of pceCreate, pceCreateCommit, pceModifyCommit, pceCancel or pceModify
      * Needs to store the reservation (or status in the case of cancel)
      * in the resourceManager. 
      * If action is create or modify and this is the last domain needs to start the
      * commit phase.
      * If the reservation is interdomain and this is the last domain needs to send a Notify message 
      * that action is completed to previous domain.
      * In the interdomain case and not last domain needs to forward request to next domain.
      *
      * @param data PCEdata returned by the top pce aggregator
      * @param srcPce the PCERuntimeAction that started the path calculation
      */
     public void setResultData (PCEData data, ProxyAction srcPce) {
         String method ="PCERuntimeAction.setResultData";
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         String requestType = srcPce.getRequestType();
         LOG.debug(netLogger.start(method," for " + requestType));
 
         boolean localRes = true;
         String localDomain = PathTools.getLocalDomainId();
         boolean lastDomain = true;
         boolean firstDomain = true;
         ResDetails resDetails = null;
         CtrlPlanePathContent reservedPath = PCERuntimeAction.getReservedPathFromPceData(data);
         try {
             if (reservedPath != null) {
                 localRes = PathTools.isPathLocalOnly(reservedPath);
                 String domain = PathTools.getLastDomain(reservedPath);
                 lastDomain = localDomain.equals(domain);
                 domain = PathTools.getFirstDomain(reservedPath);
                 firstDomain = localDomain.equals(domain);
             }
         } catch (OSCARSServiceException e) {
             LOG.error (netLogger.error(method, ErrSev.MINOR,"Cannot process PCEData " + e));
             this.fail(e);
             return;
         }
         LOG.debug(netLogger.getMsg(method,"\n\tlocalRes= " + (localRes ? "local-only" : "inter-domain") + "\n\tlocalDomain= " + localDomain +
                   "\n\tfirst domain= " + firstDomain + "\n\t" + "last domain= " + lastDomain));
         
         resDetails = pceData2ResDetails( data, srcPce, localRes, firstDomain, lastDomain);
 
         if (requestType.equals(PCERequestTypes.PCE_CREATE) || requestType.equals(PCERequestTypes.PCE_MODIFY)) {
             String event = null;
             if (requestType.equals(PCERequestTypes.PCE_CREATE)) {
                 event = PCERequestTypes.PCE_CREATE_COMMIT;
             } else if (requestType.equals(PCERequestTypes.PCE_MODIFY)) {
                 event = PCERequestTypes.PCE_MODIFY_COMMIT;
             }
             if (localRes || lastDomain ) {
                 LOG.debug(netLogger.getMsg(method,"coordRequest is "+ this.getCoordRequest()));
                 // This path is intra-domain, we can commit now.
                 this.getCoordRequest().setCommitPhase(true);
                 startCommitPhase(resDetails, event);
                 //LOG.debug(netLogger.getMsg(method,"pceruntime action state is "+ pceRuntimeAction.getState()));
 
             } else {//  not local, not last domain
                 // We need to forward to the next IDC
                 try {
                     // save the resDetails for use in the commit phase
                     this.getCoordRequest().setResDetails(resDetails);
                     String nextDomain = PathTools.getNextDomain(resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                              PathTools.getLocalDomainId());
 
                     if (requestType.equals(PCERequestTypes.PCE_CREATE)) {
 
                         CreateResvForwarder forwarder = new CreateResvForwarder (this.getName() + "-CreateResv",
                                                                                  this.getCoordRequest(),
                                                                                  nextDomain,
                                                                                  resDetails);
                         forwarder.execute();
                     } else if (requestType.equals(PCERequestTypes.PCE_MODIFY)) {
 
                         ModifyResvForwarder forwarder = new ModifyResvForwarder (this.getName() + "-ModifyResv",
                                                                                  this.getCoordRequest(),
                                                                                  nextDomain,
                                                                                  resDetails);
                         forwarder.execute();
                     }
                 } catch (OSCARSServiceException e) {
                     LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Cannot forward message " + e));
                     this.fail(e);
                     return;
                 }
             }
 
         } else if (requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT) || requestType.equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
             // Store reservation
             this. RMStore(resDetails,method);
             if (localRes || firstDomain) {
                 // notify that request is completed
                 NotifyWorker.getInstance().sendInfo (this.getCoordRequest(),
                                                      requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT) ?
                                                                         NotifyRequestTypes.RESV_CREATE_COMPLETED :
                                                                         NotifyRequestTypes.RESV_MODIFY_COMPLETED,
                                                      resDetails);
                 CoordRequest origRequest = CoordRequest.getCoordRequestByAlias( (requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT) ?
                                             "createReservation-" : "modifyReservation") + this.getCoordRequest().getGRI());
                 if(origRequest != null){
                     origRequest.setCompletePhase(true);
                 }
 
                  if (! localRes ) {
                     // firstDomain, not local
                     try {
                         // Send CREATE/MODIFY_RESV_COMPLETED event to the next IDC
                         String nextDomain = PathTools.getNextDomain (resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                                      PathTools.getLocalDomainId());
 
                         ReservationCompletedForwarder forwarder =
                                 new ReservationCompletedForwarder (this.getName() + "-CreateResvCompletedForwarder",
                                                                    this.getCoordRequest(),
                                                                    requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT) ?
                                                                                       NotifyRequestTypes.RESV_CREATE_COMPLETED :
                                                                                       NotifyRequestTypes.RESV_MODIFY_COMPLETED,
                                                                    nextDomain,
                                                                    resDetails);
                         forwarder.execute();
 
                         if (forwarder.getState() == CoordAction.State.FAILED) {
                             LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                                       "forwardRequest failed in PCERuntimeAction.setResultData with exception " +
                                                       forwarder.getException().getMessage()));
                             this.fail(forwarder.getException());
                             return;
                         }
                     } catch (OSCARSServiceException e) {
                         LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Cannot forward message " + e));
                         this.fail(e);
                         return;
                     }
                 }
                 this.releaseMutex (this.getCoordRequest().getGRI());
                     
             } else { // not local, not first Domain
                 try {
                     // will be used when RESV_CREATE|MODIFY_COMPLETED is received
                     this.getCoordRequest().setResDetails(resDetails);
                     CoordRequest origRequest = CoordRequest.getCoordRequestByAlias( (requestType.equals(PCERequestTypes.PCE_CREATE_COMMIT) ?
                             "createReservation-" : "modifyReservation") + this.getCoordRequest().getGRI());
                     if(origRequest != null){
                         origRequest.setResDetails(resDetails);
                     }
                     // send a committed IDE to previous IDC confirming the action is completed
                     String previousDomain = PathTools.getPreviousDomain (resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                                          PathTools.getLocalDomainId());
                     CommittedForwarder forwarder = new CommittedForwarder (this.getName() + "-CommittedForwarder",
                                                                            this.getCoordRequest(),
                                                                            requestType,
                                                                            previousDomain,
                                                                            resDetails);
                     forwarder.execute();
 
                     if (forwarder.getState() == CoordAction.State.FAILED) {
                         LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                                   "forwardRequest failed in PCERuntimeAction.setResultData with exception " +
                                                   forwarder.getException().getMessage()));
                         this.fail(forwarder.getException());
                         return;
                     }
                 } catch (OSCARSServiceException e) {
                     LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Cannot forward message " + e));
                     this.fail(e);
                     return;
                 }
                  // need to wait for the completed event before releasing the mutex
             }
         } else if (requestType.equals(PCERequestTypes.PCE_CANCEL)) {  // ********* PCE CANCEL *********
             CancelRequest cancelRequest = (CancelRequest) srcPce.getCoordRequest();
             cancelRequest.setPCEResultData();
         }
         // Since CoordActions have been added, it is required to re-process.
         this.process();
         LOG.debug(netLogger.end(method));
     }
 
     private void startCommitPhase(ResDetails resDetails, String  event){
 
         PCERuntimeAction pceRuntimeAction = new PCERuntimeAction (this.getName() + "-Commit-PCERuntimeAction",
                                                                           this.getCoordRequest(),
                                                                           null,
                                                                           this.transactionId,
                                                                           event);
                 PCEData pceData = new PCEData(resDetails.getUserRequestConstraint(),
                                               resDetails.getReservedConstraint(),
                                               resDetails.getOptionalConstraint(),
                                               null);
                 pceRuntimeAction.setRequestData(pceData);
                 this.add(pceRuntimeAction);
     }
 
     private Boolean RMStore(ResDetails resDetails, String method) {
         // Create, set and invoke the RMStoreAction
         RMStoreAction rmStoreAction = null;
         rmStoreAction = new RMStoreAction(this.getName() + "-RMStoreAction",
                                           this.getCoordRequest(),
                                           resDetails);
         rmStoreAction.execute();
         if (rmStoreAction.getState() == CoordAction.State.FAILED) {
             LOG.error(netLogger.error(method,ErrSev.MINOR,
                       "rmStore failed in PCERuntimeAction.setResultData with exception " +
                       rmStoreAction.getException().getMessage()));
             this.fail (rmStoreAction.getException());
             return false;
         }
         return true;
     }
 
 
     /**
      * creates a ResDetails structure from the PCEData and information saved in the
      * PCERuntime CoordRequest. The ResDetails is needed to update the reservation in
      * the Resource Manager
      * 
      * @param data - PCEData returned by the final PCE call
      * @param proxyAction
      * @return
      */
     @SuppressWarnings("unchecked")
     public ResDetails pceData2ResDetails (PCEData data,
                                            ProxyAction proxyAction, 
                                            boolean localRes,
                                            boolean firstDomain,
                                            boolean finalDomain) {
         
         if (localRes) {
             // Make sure to have consistent values
             finalDomain = false;
             firstDomain = false;
         }
         ResDetails resDetails = new ResDetails();
         CoordRequest request = this.getCoordRequest();
         resDetails.setCreateTime(request.getReceivedTime());
         resDetails.setGlobalReservationId(request.getGRI());
         // note: fields that are not set in resDetails will not be overwritten for an existing reservation
         // Set description
         String description = (String) request.getAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE);
         if (description != null) {
             resDetails.setDescription(description);
         } else {
             // leave it alone and the value in the reservations table will not be updated
             //resDetails.setDescription("CoordRequest.DESCRIPTION_ATTRIBUTE null in PCERuntimeAction.setResultData");
         }
         // Set login
         String login = (String) request.getAttribute(CoordRequest.LOGIN_ATTRIBUTE);
         if (login != null) {
             resDetails.setLogin(login);
         } else {
             // leave it alone and the value in the reservations table will not be updated
             // resDetails.setLogin("CoordRequest.LOGIN_ATTRIBUTE null in PCERuntimeAction.setResultData");
         }
         // Set user constraints
         resDetails.setUserRequestConstraint(data.getUserRequestConstraint());
         ReservedConstraintType reservedConstraint = new ReservedConstraintType();
         if (data.getTopology() != null && data.getReservedConstraint() != null) {
             PathInfo pathInfo = new PathInfo();
             reservedConstraint.setStartTime(data.getReservedConstraint().getStartTime());
             reservedConstraint.setEndTime(data.getReservedConstraint().getEndTime());
             reservedConstraint.setBandwidth(data.getReservedConstraint().getBandwidth());
             pathInfo.setPath(PCEReqFormatter.getInstance().topoToPath(data.getTopology(),
                              data.getReservedConstraint().getPathInfo().getPath()));
             /* if (data.getTopology().getPath().size() != 0) {
                 pathInfo.setPath(data.getTopology().getPath().get(0));
             }*/
             //set remaining pathInfo parameters
             if(data.getReservedConstraint().getPathInfo() != null){
                 pathInfo.setPathSetupMode(data.getReservedConstraint().getPathInfo().getPathSetupMode());
                 pathInfo.setLayer2Info(data.getReservedConstraint().getPathInfo().getLayer2Info());
                 pathInfo.setLayer3Info(data.getReservedConstraint().getPathInfo().getLayer3Info());
                 pathInfo.setMplsInfo(data.getReservedConstraint().getPathInfo().getMplsInfo());
                 pathInfo.setPathType(data.getReservedConstraint().getPathInfo().getPathType());
             }
             reservedConstraint.setPathInfo(pathInfo);
         } else if(data.getReservedConstraint() != null){
             reservedConstraint = data.getReservedConstraint();
         }
         resDetails.setReservedConstraint(reservedConstraint);
         
         if (proxyAction.getRequestType().equals(PCERequestTypes.PCE_CREATE)) {
             resDetails.setStatus(StateEngineValues.PATHCALCULATED);
         } else if (proxyAction.getRequestType().equals(PCERequestTypes.PCE_MODIFY)) {
             resDetails.setStatus(StateEngineValues.INMODIFY);
         } else if (proxyAction.getRequestType().equals(PCERequestTypes.PCE_CANCEL)) {
             resDetails.setStatus(StateEngineValues.CANCELLED);          
         } else if (proxyAction.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT)){
             if (localRes || firstDomain) {
                 resDetails.setStatus(StateEngineValues.RESERVED);
             } else {
                 resDetails.setStatus(StateEngineValues.COMMITTED);
             }
         } else if (proxyAction.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
             if (localRes || firstDomain) {
                 resDetails.setStatus((String)this.getCoordRequest().getAttribute(CoordRequest.STATE_ATTRIBUTE));
             } else {
                 resDetails.setStatus(StateEngineValues.MODCOMMITTED);
             }
         } 
         return resDetails;
     }
 
     /**
      * Fails the CoordAction. Failing a CoordAction fails the CoordRequest it is part of. 
      * The input exception will be copied to the CoordRequest.
      */
     public void failed (Exception exception) {
         LOG.debug(netLogger.start("PCERunActionAction.failed",this.getCoordRequest().getGRI() ));
         // Update status in RM.
         PCERuntimeAction.releaseMutex(this.getCoordRequest().getGRI());
     }
 
     /**
      * Iterates through the pceRuntimes hashMap and removes all the entries which are no longer
      * referenced from anyplace else.  Called from the PCERuntimeActionCleanerJob
      */
     public static void gc() {
         ArrayList<String> toBeRemoved = new ArrayList<String>();
 
         Set <Map.Entry<String,WeakReference<PCERuntimeAction>>> actionsSet = PCERuntimeAction.pceRuntimes.entrySet();
         for (Map.Entry<String,WeakReference<PCERuntimeAction>> entry : actionsSet) {
             WeakReference<PCERuntimeAction> ref = (WeakReference<PCERuntimeAction>) entry.getValue();
             if ((ref == null) || (ref.get() == null)) {
                 // free entry
                 toBeRemoved.add(entry.getKey());
             }
         } 
         
         for (String id : toBeRemoved) {
             PCERuntimeAction.pceRuntimes.remove(id);
         }
         CoordAction.gc();
     }
         
     private static CtrlPlanePathContent getReservedPathFromPceData (PCEData pceData) {
         
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         //get path constraint
         CtrlPlanePathContent path = null;
         if ( (pceData.getReservedConstraint() != null) && 
              (pceData.getReservedConstraint().getPathInfo() != null)) {
             
             path = pceData.getReservedConstraint().getPathInfo().getPath();
             
         } else {
             String msg = "Connectivity PCE received a request with no " +
                     "reservedConstraint containing a PathInfo element";
             LOG.debug(netLogger.error("getReservedPathFromPceData",ErrSev.MINOR,"calculatePath" +  msg));
         }
         return path;
     }
 
     public static void releaseMutex(String gri)  {
         PCERuntimeAction.pceMutex.release(gri);
     }
  
 }
