 package net.es.oscars.coord.req;
 
 import net.es.oscars.api.soap.gen.v06.*;
 import net.es.oscars.coord.actions.*;
 import net.es.oscars.logging.ModuleName;
 import net.es.oscars.resourceManager.soap.gen.GetStatusReqContent;
 import net.es.oscars.resourceManager.soap.gen.GetStatusRespContent;
 import net.es.oscars.utils.sharedConstants.ErrorCodes;
 import net.es.oscars.utils.soap.ErrorReport;
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
 
 import net.es.oscars.common.soap.gen.MessagePropertiesType;
 import net.es.oscars.coord.soap.gen.PSSReplyContent;
 import net.es.oscars.coord.workers.InternalAPIWorker;
 import net.es.oscars.coord.workers.NotifyWorker;
 import net.es.oscars.coord.common.Coordinator;
 import net.es.oscars.logging.ErrSev;
 import net.es.oscars.logging.OSCARSNetLogger;
 import net.es.oscars.pss.soap.gen.SetupReqContent;
 import net.es.oscars.pss.soap.gen.TeardownReqContent;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.sharedConstants.StateEngineValues;
 import net.es.oscars.utils.topology.PathTools;
 import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
 import net.es.oscars.utils.sharedConstants.PSSConstants;
 
 import java.util.HashSet;
 
 /**
  * Calls the PSS server to either instantiate or teardown a path depending on
  * the type of the requestData.
  * parameters are set by the constructor. 
  * 
  * @author lomax,mrt
  *
  */
 public class PathRequest extends CoordRequest <PathRequestParams,PSSReplyContent >{
     
     private static final long       serialVersionUID  = 1L;
     private static final Logger     LOG = Logger.getLogger(PathRequest.class.getName());
 
 
     private OSCARSNetLogger netLogger = null;
     private ResDetails   resDetails = null;
     private String localDomain = null;
     private String nextIDC = null;
     private String previousIDC = null;
     private boolean isExecuted = false;
     private boolean isLocalOnly = false;
     private boolean isLastDomain = true;
     private boolean isFirstDomain = true;
     private boolean receivedUpStream = false;   // received an UPSTREAM event
     private boolean receivedDownStream = false; // received a DOWNSTREAM event
     private String status = null;   // null until set by PSSReply or by FAILURE EVENT
     private boolean isTeardownPending = false; // set when a teardown has been started as the result of an IDE FAILED event
     private String failedEvent = null;
     private String errorCode = null;
     private String completedEvent = null;
 
     public static final String PSS_CREATE_PATH = "CreatePath";
     public static final String PSS_TEARDOWN_PATH = "TeardownPath";
 
     /**
      *
      * @param name should be of the form CreatePath-gri, or TeardownPath-gri
      * @param msgProps MessageProperties of the message that triggered this  method
      * @param resDetails - Details for the reservation
      * @return if a request by this name has been registered, returns it
      *         otherwise creates and registers a new pathRequest
      */
     public static PathRequest getPathRequest(String name,
                                              MessagePropertiesType msgProps,
                                              ResDetails resDetails)
             throws OSCARSServiceException {
 
         PathRequest pathReq = (PathRequest) CoordRequest.getCoordRequestByAlias(name);
         if (pathReq != null ) {
             return pathReq;
         }
 
         /* Otherwise create and attempt to register a new PathRequest */
 
         if (name.startsWith(PSS_CREATE_PATH)) {
             CreatePathContent content = new CreatePathContent();
             content.setMessageProperties(msgProps);
             content.setGlobalReservationId(resDetails.getGlobalReservationId());
             pathReq = new PathRequest(name,content,resDetails);
         } else if (name.startsWith(PSS_TEARDOWN_PATH)){
             TeardownPathContent content = new TeardownPathContent();
             content.setMessageProperties(msgProps);
             content.setGlobalReservationId(resDetails.getGlobalReservationId());
             pathReq = new PathRequest(name,content,resDetails);
         }
         CoordRequest coordRequest = pathReq.registerExclusiveAlias(name);
         if  (coordRequest != null) { // another pathRequest for this name has just been registered, use it
             CoordRequest.forget(pathReq.getLocalId());
             return (PathRequest) coordRequest;
         }
         else {
             return pathReq;
         }
     }
     
     private PathRequest(String name,
                         CreatePathContent createPathContent,
                         ResDetails resDetails) throws OSCARSServiceException {
         super (name,
                createPathContent.getMessageProperties().getGlobalTransactionId(),
                resDetails.getGlobalReservationId());
 
         this.setRequestData(new PathRequestParams(createPathContent));
         this.setMessageProperties(createPathContent.getMessageProperties());
         this.resDetails = resDetails;
         this.failedEvent = NotifyRequestTypes.PATH_SETUP_FAILED;
         this.errorCode = ErrorCodes.PATH_SETUP_FAILED;
         this.completedEvent = NotifyRequestTypes.PATH_SETUP_COMPLETED;
         this.setContext();
     }
     
     private PathRequest(String name,
                         TeardownPathContent teardownPathContent,
                         ResDetails resDetails) throws OSCARSServiceException {
         super (name,
                teardownPathContent.getMessageProperties().getGlobalTransactionId(),
                resDetails.getGlobalReservationId());
  
         this.setRequestData(new PathRequestParams(teardownPathContent));
         this.resDetails = resDetails;
         this.failedEvent = NotifyRequestTypes.PATH_TEARDOWN_FAILED;
         this.errorCode = ErrorCodes.PATH_TEARDOWN_FAILED;
         this.completedEvent = NotifyRequestTypes.PATH_TEARDOWN_COMPLETED;
         this.setContext();
     }
 
     private void setContext() throws OSCARSServiceException {
         
         this.netLogger = OSCARSNetLogger.getTlogger();
         this.netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
         this.netLogger.setGRI(this.getGRI());
         
         CtrlPlanePathContent path = resDetails.getReservedConstraint().getPathInfo().getPath();
         this.isLocalOnly = PathTools.isPathLocalOnly(path);
         this.localDomain = PathTools.getLocalDomainId();
         this.nextIDC = PathTools.getNextDomain(path, this.localDomain);
         this.previousIDC = PathTools.getPreviousDomain(path, this.localDomain);
         this.isFirstDomain = this.localDomain.equals(PathTools.getFirstDomain(path));
         this.isLastDomain = this.localDomain.equals(PathTools.getLastDomain(path));
         
         this.setCoordRequest(this);
         LOG.debug(netLogger.end("setContext",
                                  (isLocalOnly? "LocalRes" : "interDomainRes") +
                                   " prevDomain: " + this.previousIDC +
                                   " nexdDomain: "+ this.nextIDC)) ;
     }
 
     /*
      * Make sure this is only executed once
      */
     public synchronized boolean checkIfExecuted() {
         if (this.isExecuted) {
             return true;
         }
         this.isExecuted = true;
         return false;
     }
 
     /**
      *  The reservation state is set to INSETUP or INTEARDOWN, and
      *  a pathCreateAction  or pathTeardownAction is executed to call the PSS
      */
     public void execute()  {
 
         if (checkIfExecuted()) {
             return;
         }
 
         String reqType = this.getRequestData().getType();
         String method = "PathRequest(" + reqType + ")" + ".execute";
         LOG.debug(netLogger.start(method));
 
         try {
 
             RMUpdateStatusAction rmAction = new RMUpdateStatusAction(this.getName() + "-RMUpdateAction",
                                                                      this,
                                                                      this.getGRI(),
                                                                      reqType.equals(PathRequestParams.CREATEPATHCONTENT)
                                                                              ? StateEngineValues.INSETUP
                                                                              : StateEngineValues.INTEARDOWN);
 
             rmAction.execute();
             if (rmAction.getState() ==  CoordAction.State.FAILED) {
                     throw rmAction.getException();
             }
 
             // Create and execute a PSS Action to send a PSS_SETUP message
             if (reqType.equals(PathRequestParams.CREATEPATHCONTENT)) {
                 SetupReqContent pssReq = new SetupReqContent();
                 Coordinator coordinator = Coordinator.getInstance();
                 pssReq.setTransactionId(this.getTransactionId());
                 pssReq.setReservation(resDetails);
                 pssReq.setCallbackEndpoint(coordinator.getCallbackEndpoint());
                 PSSCreatePathAction pathCreateAction = new PSSCreatePathAction (this.getName() + "-PSSCreatePathAction",
                                                                                 null,
                                                                                 pssReq);
                 pathCreateAction.execute();
                 if (pathCreateAction.getState() == CoordAction.State.FAILED) {
                     throw pathCreateAction.getException();
                 }
 
                 // If path is remote, send createPath to next IDC.
                 if ( (! this.isLocalOnly) &&
                      (resDetails.getUserRequestConstraint().getPathInfo().getPathSetupMode().equals("signal-xml"))) {
 
                     CreatePathForwarder forwarder = new CreatePathForwarder (this.getName() + "-Forwarder",
                                                                              this,
                                                                              this.nextIDC,
                                                                              this.getRequestData().getCreatePathContent());
                     forwarder.execute();
                 }
             } else if (reqType.equals(PathRequestParams.TEARDOWNPATHCONTENT)) {
                 TeardownReqContent pssReq = new TeardownReqContent();
                 Coordinator coordinator = Coordinator.getInstance();
                 pssReq.setTransactionId(this.getTransactionId());
                 pssReq.setCallbackEndpoint(coordinator.getCallbackEndpoint());
                 pssReq.setReservation(resDetails);
                 PSSTeardownPathAction pathTeardownAction = new PSSTeardownPathAction (this.getName() + "-PSSTeardownPathAction",
                                                                                       null,
                                                                                       pssReq);
                 pathTeardownAction.execute();
                 if (pathTeardownAction.getState() == CoordAction.State.FAILED) {
                     throw pathTeardownAction.getException();
                 }
                 isTeardownPending = true;
                 // If path is remote, send teardownPath to next IDC.
                 if ((! this.isLocalOnly) &&
                     (resDetails.getUserRequestConstraint().getPathInfo().getPathSetupMode().equals("signal-xml")) &&
                     (resDetails.getUserRequestConstraint().getEndTime()*1000 > System.currentTimeMillis())) {
 
                     TeardownPathForwarder forwarder = new TeardownPathForwarder (this.getName() + "-Forwarder",
                                                                                  this,
                                                                                  this.nextIDC,
                                                                                  this.getRequestData().getTeardownPathContent());
                     forwarder.execute();
                 }
             }
 
         } catch (Exception ex ) {
             ErrorReport errorRep = this.getCoordRequest().getErrorReport(method, this.failedEvent, ex);
             this.fail(new OSCARSServiceException(errorRep));
         }
         LOG.debug(netLogger.end(method));
     }
 
     /**
      * Called by PSSReplyRequest to set the results of the SETUP or TEARDOWN REQUEST
      * @param pssReply
      */
 
     public void setPSSReplyResult (PSSReplyContent pssReply) {
 
         String reqType = this.getRequestData().getType();
         String method = "PathRequest(" + reqType + ").setResultData";
         LOG.debug(netLogger.start(method));
         if ( ! pssReply.getStatus().equals(PSSConstants.SUCCESS) &&
              ! pssReply.getStatus().equals(PSSConstants.FAIL)) {
             // Transient state. The coordinator should not be called by the PSS with a status other than SUCCESS or FAIL
             // This is a safety net test
             this.notifyError("PSS called Coordinator with " + pssReply.getStatus(),this.resDetails);
             this.fail (new OSCARSServiceException(this.errorCode,
                                                   "PSS called Coordinator with " + pssReply.getStatus(),
                                                    ErrorReport.SYSTEM));
             return;
         }
 
         // Handle error
         if (pssReply.getStatus().equals(PSSConstants.FAIL)) {
             String errorMsg = null;
             ErrorReport errorReport = null;
             if ( pssReply.getErrorReport() != null &&
                     pssReply.getErrorReport().getErrorMsg() != null) {
                 errorReport =  ErrorReport.fault2report(pssReply.getErrorReport());
             } else {
                 if (this.errorCode.equals(ErrorCodes.PATH_TEARDOWN_FAILED)) {
                     errorMsg = "Path Teardown failed with error from PSSTeardown";
                 } else {
                     errorMsg = "Path Setup failed with error from PSSSetup";
                 }
                 errorReport = new ErrorReport(this.errorCode,
                                               errorMsg,
                                               ErrorReport.SYSTEM,
                                               this.getGRI(),
                                               this.getTransactionId(),
                                               System.currentTimeMillis()/1000L,
                                               ModuleName.COORD,
                                               PathTools.getLocalDomainId());
             }
             this.fail (new OSCARSServiceException(errorReport));
 
         } else {    // SUCCESS from PSSReply
             if (this.status != null && this.status.equals(PSSConstants.FAIL)) {
                 // request has already been failed from another domain,
                 // we just did a local teardown
                 // change status from UNKNOWN to FAILED
                 ErrorReport errorRep = new ErrorReport(errorCode, "local teardown succeeded",
                                                   ErrorReport.UNKNOWN, this.getGRI(),this.getTransactionId(),
                                                   System.currentTimeMillis()/1000L, CoordRequest.moduleName,
                                                   PathTools.getLocalDomainId());
                 RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                   this,
                                                                   this.getGRI(),
                                                                   StateEngineValues.FAILED,
                                                                   errorRep);
                 action.execute();
                 this.unRegisterAlias(this.getName());
                 return;
             }
 
             synchronized (this) {
                 if (this.status == null){
                     this.status = pssReply.getStatus();
                 }
             }
             if ( ! this.isLocalOnly) {
                 if (this.isFirstDomain) {
                     this.sendUpStream(null);
                 } else if (this.isLastDomain) {
                     this.sendDownStream(null);
                 } else {
                     if (this.receivedDownStream) {
                         this.sendDownStream(null);
                     }
                     if (this.receivedUpStream) {
                         this.sendUpStream(null);
                     }
                 }
             }
             this.setReservationState();
         }
     }
 
     /**
      *  Called from InterDomainEventRequest when it has received a PATH_SETUP or TEARDOWN event
      * @param event  name of the event
      */
     public void processEvent(String event) {
         if (this.isLocalOnly) {
             // shouldn't be here
             return;
         }
         if ((event.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED)) ||
             (event.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED)) ) {
             this.receivedDownStream = true;
             if (! this.isFirstDomain) {
                 boolean sendEvent = false;
                 synchronized (this) {
                     if ((this.status != null) && (this.status.equals(PSSConstants.SUCCESS))) {
                         sendEvent = true;
                     }
                 }
                 if (sendEvent) {
                     this.sendDownStream(null);
                 }
             }
             setReservationState();
 
         } else if ((event.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED)) ||
                    (event.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED)) ) {
             this.receivedUpStream = true;
             if (! this.isLastDomain) {
                 boolean sendEvent = false;
                 synchronized (this) {
                     if ((this.status != null) && (this.status.equals(PSSConstants.SUCCESS))) {
                         sendEvent = true;
                     }
                 }
                 if (sendEvent) {
                     this.sendUpStream(null);
                 }
             }
             setReservationState();
         }
     }
 
       /**
        *  Called from InterDomainEventRequest when it has received a PATH_SETUP or TEARDOWN FAILED event
        * @param event  name of the event
        * @param eventContent - the eventContent that was received
       */
     public void processErrorEvent (String event, InterDomainEventContent eventContent) {
 
         String errorCode = event; // overrides default error event
         boolean isTeardownError = false;
         boolean isSetupError = false;
 
         if (event.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_FAILED)) {
             errorCode = ErrorCodes.PATH_SETUP_DOWNSTREAM_FAILED;
             isSetupError = true;
         } else if  (event.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_FAILED)) {
             errorCode = ErrorCodes.PATH_SETUP_UPSTREAM_FAILED;
             isSetupError = true;
         } else if (event.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_FAILED)) {
             errorCode = ErrorCodes.PATH_TEARDOWN_DOWNSTREAM_FAILED;
             isTeardownError = true;
         } else if (event.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_FAILED)) {
             errorCode = ErrorCodes.PATH_TEARDOWN_UPSTREAM_FAILED;
             isTeardownError = true;
         }
 
 
        if ((isTeardownError || isSetupError) && getResvStatus().equals(StateEngineValues.ACTIVE)) {
             // try to do a local path teardown
             try {
                 TeardownReqContent pssReq = new TeardownReqContent();
                 Coordinator coordinator = Coordinator.getInstance();
                 pssReq.setTransactionId(this.getTransactionId());
                 pssReq.setCallbackEndpoint(coordinator.getCallbackEndpoint());
                 pssReq.setReservation(resDetails);
                 PSSTeardownPathAction pathTeardownAction = new PSSTeardownPathAction (this.getName() + "-PSSTeardownPathAction",
                                                                                       null,
                                                                                        pssReq);
                 pathTeardownAction.execute();
                 if (pathTeardownAction.getState() == CoordAction.State.FAILED) {
                    throw pathTeardownAction.getException();
                 }
                 isTeardownPending = true;
             } catch (Exception ex) {
                 LOG.warn(netLogger.error("ProcessErrorEvent", ErrSev.MINOR, "failed to teardown an ACTIVE circuit"));
             }
         }
 
          ErrorReport errRep = new ErrorReport(errorCode,
                                               eventContent.getErrorMessage(),
                                               ErrorReport.SYSTEM,
                                               this.getGRI(),
                                               this.getTransactionId(),
                                               System.currentTimeMillis()/1000L,
                                               ModuleName.COORD,
                                               eventContent.getErrorSource());
          this.fail (new OSCARSServiceException(errRep));
     }
 
     // sends UPSTREAM event to nextIDC (downstream) IDC
     private void sendUpStream(String errorMsg) {
         String reqType = this.getRequestData().getType();
         try {
             this.receivedUpStream = true; // needed for the first domain which never actually receives an upstream event
             if (this.nextIDC != null) {
                 if  (errorMsg == null) {
                     InternalAPIWorker.getInstance().sendEventContent(this.getCoordRequest(),
                                                                  this.resDetails, 
                                                                  reqType.equals(PathRequestParams.CREATEPATHCONTENT) ?
                                                                          NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED :
                                                                          NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED,
                                                                  this.nextIDC);
                 } else {
                     InternalAPIWorker.getInstance().sendErrorEvent(this.getCoordRequest(),
                                                                    this.resDetails,
                                                                    reqType.equals(PathRequestParams.CREATEPATHCONTENT) ?
                                                                            NotifyRequestTypes.PATH_SETUP_UPSTREAM_FAILED :
                                                                            NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_FAILED,
                                                                    errorMsg,
                                                                    PathTools.getLocalDomainId(),
                                                                    this.nextIDC);
                 }
             }
         } catch (OSCARSServiceException e) {
             this.fail(e);
         }       
     }
     // sends a DOWNSTREAM event to the previousIDC (upstream) IDC
     private void sendDownStream(String errorMsg) {
         String reqType = this.getRequestData().getType();
         try {
             this.receivedDownStream = true; // needed for the last domain which never actually receives an downstream event
             if (this.previousIDC != null) {
                 if (errorMsg == null ) {
                     InternalAPIWorker.getInstance().sendEventContent(this.getCoordRequest(),
                                                                      this.resDetails,
                                                                      reqType.equals(PathRequestParams.CREATEPATHCONTENT) ?
                                                                              NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED :
                                                                              NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED,
                                                                      this.previousIDC);
                 } else {
                      InternalAPIWorker.getInstance().sendErrorEvent(this.getCoordRequest(),
                                                                     this.resDetails,
                                                                     reqType.equals(PathRequestParams.CREATEPATHCONTENT) ?
                                                                             NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_FAILED :
                                                                             NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_FAILED,
                                                                     errorMsg,
                                                                     PathTools.getLocalDomainId(),
                                                                     this.previousIDC);
                 }
             }
         } catch (OSCARSServiceException e) {
             this.fail(e);
         }        
     }
 
     /**
      * Called when some internal state of the pathRequest is changed.
      *  or when processing an InterDomainEvent: PATH_{TEARDOWN,SETUP}_{UPSTREAM,DOWNSTREAM_}_{CONFIRMED,FAILED}
      * Called by setPSSReplyResult on the return of the pathSetup or teardown PSS request
      * Checks to see if the status in the RM should be updated
      */
     private void setReservationState () {
         String reqType = this.getRequestData().getType();
         String newState = null;
         String event = null;
         synchronized (this) {
             if (! this.isLocalOnly) {
                 if ((this.status != null) && this.receivedUpStream && this.receivedDownStream) {
                     // All IDC's in the path have completed
                     if (this.status.equals(PSSConstants.SUCCESS)) {
                         if (reqType.equals(PathRequestParams.CREATEPATHCONTENT)) {
                             newState = StateEngineValues.ACTIVE;
                         } else if (reqType.equals(PathRequestParams.TEARDOWNPATHCONTENT)) {
                             newState = StateEngineValues.RESERVED;
                         }
                         event = this.completedEvent;
                     } else if (this.status.equals(PSSConstants.FAIL)) {
                         newState = StateEngineValues.FAILED;
                         event = this.failedEvent;
                     }
                 }
             } else {  // isLocalOnly
                 if (this.status != null) {
                     if (this.status.equals(PSSConstants.SUCCESS)) {
                         if (reqType.equals(PathRequestParams.CREATEPATHCONTENT)) {
                             newState = StateEngineValues.ACTIVE;
                         } else if (reqType.equals(PathRequestParams.TEARDOWNPATHCONTENT)) {
                             newState = StateEngineValues.RESERVED;
                         }
                         event = this.completedEvent;
                     } else if (this.status.equals(PSSConstants.FAIL)) {
                         newState = StateEngineValues.FAILED;
                         event = this.failedEvent;
                     }
                 } else { // if it's LocalOnly null  means the PSS has not returned
                     newState =  StateEngineValues.FAILED;
                 }
             }
         }
         if (newState != null) {
             this.setReservationState (newState);
         }
         if (event != null) {
             // if event has been set, the request is either completed or failed
             // remove from alias list
             LOG.debug(netLogger.end("PathRequest.setReservationState", " calling unRegisterAlias with " +
                                         this.getName()));
             this.unRegisterAlias(this.getName());
             NotifyWorker.getInstance().sendInfo(this, event, this.resDetails);
         }
         this.executed();
     }
 
     /**
      *  Calls the RM to update the reservation status
      * @param resvState value of the new state: ACTIVE or RESERVED. FAILED  state is handled by  failed method
      */
     private void setReservationState (String resvState) {
 
         if (resvState.equals(StateEngineValues.FAILED)) {
             return;
         }
 
         RMUpdateStatusAction rmUpdate = new RMUpdateStatusAction(this.getName() + "-RMUpdateStatusAction", 
                                                                  this,
                                                                  this.getGRI(),
                                                                  resvState);
         rmUpdate.execute();
         if (rmUpdate.getState() == CoordAction.State.FAILED){
             this.fail (rmUpdate.getException());
         }        
     }
     /**
      * Overrides method in coordRequest to be sure that a multi-domain path request
      * is not waiting for notification from another domain.
      * @return
      */
     public boolean isRequestComplete() {
         String resStatus = getResvStatus();
         if (isFullyCompleted() &&
             ! resStatus.equals(StateEngineValues.INSETUP) &&
             ! resStatus.equals(StateEngineValues.INTEARDOWN)) {
             return true;
         }
         return false;
     }
 
     /**
       * Send notification that this request has failed
       * CoordRequest implementations are expected to implement it.
       * @param errorMsg
       * @param resDetails
       */
     public void notifyError (String errorMsg, ResDetails resDetails) {
         String source = PathTools.getLocalDomainId();
         NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                              this.failedEvent,
                                              errorMsg,
                                              source,
                                              resDetails);
     }
 
 
     /**
      *  Called by the base CoordAction fail method
      * @param ex - should contain an ErrorReport for the failure.
      */
     public void failed (Exception ex) {
         String method = "PathRequest.failed";
         LOG.error(netLogger.error(method, ErrSev.FATAL, this.getName() + " failed with " + ex.getMessage()));
 
         synchronized (this) {
             if (this.status != null && this.status.equals(PSSConstants.FAIL)) {
                  return; // failure has already been handled
             } 
             // ignore extraneous interDomain error events
             if (!isLocalOnly) {
                 String status =  getResvStatus();
                 if (status.equals(StateEngineValues.FAILED) ||
                         status.equals(StateEngineValues.UNKNOWN)) {
                     return;
                 }
             }
 
             this.status = PSSConstants.FAIL;
         }
 
 
         // Fill in any missing parts of the errorReport.
         ErrorReport errorRep = this.getCoordRequest().getErrorReport(method, this.failedEvent, ex);
 
         // update status to UNKNOWN
         RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                   this,
                                                                   this.getGRI(),
                                                                   StateEngineValues.UNKNOWN,
                                                                   errorRep);
         action.execute();
 
         if (action.getState() == CoordAction.State.FAILED) {
             LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                       action.getException().getMessage()));
         }
         // send IDE up and/or downstream
         if ( ! this.isLocalOnly) {
             if (! this.isFirstDomain) {
                 LOG.debug(netLogger.getMsg(method,"sending PATH_TEARDOWN_DOWNSTREAM_FAILED to " + this.previousIDC));
                 this.sendDownStream(errorRep.getErrorMsg());
             }
             if (! this.isLastDomain) {
                  LOG.debug(netLogger.getMsg(method,"sending PATH_TEARDOWN_UPSTREAM_FAILED to " + this.nextIDC));
                 this.sendUpStream(errorRep.getErrorMsg());
             }
         }
         this.notifyError (exception.getMessage(), this.resDetails);
         if (!isTeardownPending){
             this.unRegisterAlias(this.getName());
         }
     }
 }
