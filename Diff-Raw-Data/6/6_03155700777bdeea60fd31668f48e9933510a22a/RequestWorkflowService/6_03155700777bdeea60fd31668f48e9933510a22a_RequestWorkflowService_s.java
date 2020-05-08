 package fr.cg95.cvq.service.request.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequest;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.IRequestNotificationService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.users.ICertificateService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 
 /**
  * This services handles workflow tasks for requests. Workflow-related calls to
  * {@link IRequestService} are delegated to this service. It is responsible for :
  * <ul>
  *  <li>Checking requested states changes are authorized</li>
  *  <li>Updating requests state and workflow information</li>
  *  <li>Creating and managing worklow action traces</li>
  * </ul>
  * 
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class RequestWorkflowService implements IRequestWorkflowService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(RequestWorkflowService.class);
     
     private ICertificateService certificateService;
     private IDocumentService documentService;
     private IHomeFolderService homeFolderService;
     private IExternalService externalService;
     private IRequestServiceRegistry requestServiceRegistry;
 
     private IRequestActionService requestActionService;
     private IRequestNotificationService requestNotificationService;
 
     private IRequestDAO requestDAO;
 
     private ListableBeanFactory beanFactory;
 
     public void init() {
         externalService = (IExternalService)
             beanFactory.getBeansOfType(IExternalService.class, false, false).values().iterator().next();
         this.homeFolderService = (IHomeFolderService)
             beanFactory.getBeansOfType(IHomeFolderService.class, false, false).values().iterator().next();
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void updateRequestDataState(final Long id, final DataState rs)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = (Request) requestDAO.findById(Request.class, id);
         if (rs.equals(DataState.VALID))
             validData(request);
         else if (rs.equals(DataState.INVALID))
             invalidData(request);
     }
 
     // TODO : must we trace as request action 
     private void validData(Request request)
         throws CvqException, CvqInvalidTransitionException {
     
         // if no state change asked, just return silently
         if (request.getDataState().equals(DataState.VALID))
             return;
     
         if (request.getDataState().equals(DataState.PENDING))
             request.setDataState(DataState.VALID);
         else
             throw new CvqInvalidTransitionException();
     }
     
     // TODO : must we trace as request action 
     private void invalidData(Request request)
             throws CvqException, CvqInvalidTransitionException {
     
         // if no state change asked, just return silently
         if (request.getDataState().equals(DataState.INVALID))
             return;
     
         if (request.getDataState().equals(DataState.PENDING))
             request.setDataState(DataState.INVALID);
         else
             throw new CvqInvalidTransitionException();
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void updateRequestState(final Long id, final RequestState rs, final String motive)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = (Request) requestDAO.findById(Request.class, id);
 
         if (rs.equals(RequestState.COMPLETE))
             complete(request);
         else if (rs.equals(RequestState.UNCOMPLETE))
             specify(request, motive);
         else if (rs.equals(RequestState.REJECTED))
             reject(request, motive);
         else if (rs.equals(RequestState.CANCELLED))
             cancel(request);
         else if (rs.equals(RequestState.VALIDATED))
             validate(request);
         else if (rs.equals(RequestState.NOTIFIED))
             notify(request, motive);
         else if (rs.equals(RequestState.ACTIVE))
             activate(request);
         else if (rs.equals(RequestState.EXPIRED))
             expire(request);
         else if (rs.equals(RequestState.CLOSED))
             close(request);
         else if (rs.equals(RequestState.ARCHIVED))
             archive(request);
     }
 
     private void complete(Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.COMPLETE))
             return;
 
         if (request.getState().equals(RequestState.PENDING)
                 || request.getState().equals(RequestState.UNCOMPLETE)) {
             
             request.setState(RequestState.COMPLETE);
             Date date = new Date();
             updateLastModificationInformation(request, date);
 
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.COMPLETE, null);
 
         } else {
             throw new CvqInvalidTransitionException();
         }
     }
     
     private void specify(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         if (request.getState().equals(RequestState.UNCOMPLETE))
             return;
         
         if (request.getState().equals(RequestState.PENDING)
             || request.getState().equals(RequestState.VALIDATED)) {
 
             request.setState(RequestState.UNCOMPLETE);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             
             requestActionService.addWorfklowAction(request.getId(), motive, date,
                 RequestState.UNCOMPLETE, null);
         } else {
             throw new CvqInvalidTransitionException();
         }
     }
 
     protected void validateAssociatedDocuments(final Set<RequestDocument> documentSet)
         throws CvqException {
 
         if (documentSet == null)
             return;
 
         for (RequestDocument requestDocument : documentSet) {
             documentService.validate(requestDocument.getDocumentId(),
                     new Date(), "Automatic validation");
         }
     }
 
     /**
 	 * Do the real work of validating a request.
      *
      * <ul>
      *   <li>change request's state and step</li>
      *   <li>generate a PDF certificate is asked for</li>
      *   <li>validate home folder if created along the request</li>
 	 *   <li>validate associated documents</li>
      *   <li>notify associated external services</li>
      *   <li>send notification email to e-citizen if notification enabled for this request</li>
      * </ul>
 	 *
 	 * @param request the request to be validated
 	 * @param generateCertificate
 	 *            whether or not we want the request's certificate to be
 	 *            generated (some requests have to add extra information in the
 	 *            certificate and so they call directly the
 	 *            {@link ICertificateService})
 	 */
     protected void validate(final Request request)
         throws CvqException, CvqInvalidTransitionException, CvqModelException,
             CvqObjectNotFoundException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestValidated(request);
 
         validateXmlData(request.modelToXml());
 
         logger.debug("validate() Gonna generate a pdf of the request");
         byte[] pdfData = certificateService.generateRequestCertificate(request);
 
         validate(request, pdfData);
 
         validateAssociatedDocuments(request.getDocuments());
 
         // those two request types are special ones
         if (request instanceof VoCardRequest || request instanceof HomeFolderModificationRequest)
             homeFolderService.validate(request.getHomeFolderId());
         else
             homeFolderService.onRequestValidated(request.getHomeFolderId(), request.getId());
 
 		// send request data to interested external services
         // TODO DECOUPLING
 		externalService.sendRequest(request);
 
         // TODO DECOUPLING
         requestNotificationService.notifyRequestValidation(request.getId(), pdfData);
     }
 
     public void validate(Request request, byte[] pdfData)
         throws CvqException, CvqInvalidTransitionException {
         
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.VALIDATED))
             return;
 
         if (!request.getState().equals(RequestState.COMPLETE))
             throw new CvqInvalidTransitionException();
 
         request.setState(RequestState.VALIDATED);
         request.setDataState(DataState.VALID);
         request.setStep(RequestStep.DELIVERY);
         Date date = new Date();
         request.setValidationDate(date);
         updateLastModificationInformation(request, date);
        
         requestActionService.addWorfklowAction(request.getId(), null, date,
             RequestState.VALIDATED, pdfData);
     }
     
     protected void validateXmlData(XmlObject xmlObject) {
         ArrayList<Object> validationErrors = new ArrayList<Object>();
         XmlOptions options = new XmlOptions();
         options.setErrorListener(validationErrors);
         boolean isValid = xmlObject.validate(options);
         if (!isValid) {
             for (Object error : validationErrors) {
                 logger.info("validateXmlData() Error : " + error);
             }
         }
     }
 
     private void notify(Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.NOTIFIED))
             return;
 
         if (!request.getState().equals(RequestState.VALIDATED))
             throw new CvqInvalidTransitionException();
 
         request.setState(RequestState.NOTIFIED);
         Date date = new Date();
         updateLastModificationInformation(request, date);
 
         requestActionService.addWorfklowAction(request.getId(), motive, date,
             RequestState.NOTIFIED, null);
     }
 
     private void activate(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.ACTIVE))
             return;
 
         if (!request.getState().equals(RequestState.NOTIFIED))
             throw new CvqInvalidTransitionException();
 
         request.setState(RequestState.ACTIVE);
         Date date = new Date();
         updateLastModificationInformation(request, date);
 
         requestActionService.addWorfklowAction(request.getId(), null, date,
             RequestState.ACTIVE, null);
     }
     
     private void expire(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.EXPIRED))
             return;
 
         if (!request.getState().equals(RequestState.ACTIVE))
             throw new CvqInvalidTransitionException();
 
         request.setState(RequestState.EXPIRED);
         Date date = new Date();
         updateLastModificationInformation(request, date);
 
         requestActionService.addWorfklowAction(request.getId(), null, date,
             RequestState.EXPIRED, null);
     }
     
     private void cancel(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestCancelled(request);
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.CANCELLED))
             return;
 
         if (request.getState().equals(RequestState.COMPLETE)
                 || request.getState().equals(RequestState.UNCOMPLETE)
                 || request.getState().equals(RequestState.PENDING)) {
             request.setState(RequestState.CANCELLED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
 
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.CANCELLED, null);
         } else {
             throw new CvqInvalidTransitionException();
         }
 
         if (request instanceof VoCardRequest)
             // invalidate home folder is creation request is cancelled
             homeFolderService.invalidate(request.getHomeFolderId());
         else if (request instanceof HomeFolderModificationRequest)
             // home folder was supposed to be valid before modification request
             homeFolderService.validate(request.getHomeFolderId());
         else
             homeFolderService.onRequestCancelled(request.getHomeFolderId(), request.getId());
     }
 
     private void reject(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestRejected(request);
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.REJECTED))
             return;
 
         if (request.getState().equals(RequestState.COMPLETE)
                 || request.getState().equals(RequestState.UNCOMPLETE)
                 || request.getState().equals(RequestState.PENDING)) {
             request.setState(RequestState.REJECTED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
 
             requestActionService.addWorfklowAction(request.getId(), motive, date,
                 RequestState.REJECTED, null);
         } else {
             throw new CvqInvalidTransitionException();
         }
 
         if (request instanceof VoCardRequest)
             // invalidate home folder is creation request is cancelled
             homeFolderService.invalidate(request.getHomeFolderId());
         else if (request instanceof HomeFolderModificationRequest)
             // home folder was supposed to be valid before modification request
             homeFolderService.validate(request.getHomeFolderId());
         else
             homeFolderService.onRequestRejected(request.getHomeFolderId(), request.getId());
     }
     
     private void close(Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.CLOSED))
             return;
 
         if (request.getState().equals(RequestState.NOTIFIED)) {
             request.setState(RequestState.CLOSED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
 
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.CLOSED, null);
         } else {
             throw new CvqInvalidTransitionException();
         }
     }
 
     private void archive(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.ARCHIVED))
             return;
 
         if (request.getState().equals(RequestState.CANCELLED)
                 || request.getState().equals(RequestState.REJECTED)
                 || request.getState().equals(RequestState.NOTIFIED)
                 || request.getState().equals(RequestState.CLOSED)) {
 
             request.setState(RequestState.ARCHIVED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
 
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.ARCHIVED, null);
         } else {
             throw new CvqInvalidTransitionException();
         }
 
         homeFolderService.onRequestArchived(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void archiveHomeFolderRequests(Long homeFolderId)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         List<Request> requests = requestDAO.listByHomeFolder(homeFolderId);
         if (requests == null || requests.isEmpty()) {
             logger.debug("archiveHomeFolderRequests() no requests associated to home folder "
                     + homeFolderId);
             return;
         }
 
         // duplicated to avoid state checks
         for (Request request : requests) {
             request.setState(RequestState.ARCHIVED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.ARCHIVED, null);
         }
     }
 
     public DataState[] getPossibleTransitions(DataState ds) {
         List<DataState> dataStateList = new ArrayList<DataState>();
 
         if (ds.equals(DataState.PENDING)) {
             dataStateList.add(DataState.VALID);
             dataStateList.add(DataState.INVALID);
         }
         return (DataState[]) dataStateList.toArray(new DataState[0]);
     }
 
 
     public RequestState[] getPossibleTransitions(RequestState rs) {
 
         List<RequestState> requestStateList = new ArrayList<RequestState>();
 
         if (rs.equals(RequestState.PENDING)) {
             requestStateList.add(RequestState.COMPLETE);
             requestStateList.add(RequestState.UNCOMPLETE);
             requestStateList.add(RequestState.REJECTED);
             requestStateList.add(RequestState.CANCELLED);
         } else if (rs.equals(RequestState.COMPLETE)) {
             requestStateList.add(RequestState.VALIDATED);
             requestStateList.add(RequestState.REJECTED);
             requestStateList.add(RequestState.CANCELLED);
         } else if (rs.equals(RequestState.UNCOMPLETE)) {
             requestStateList.add(RequestState.COMPLETE);
             requestStateList.add(RequestState.UNCOMPLETE);
             requestStateList.add(RequestState.CANCELLED);
             requestStateList.add(RequestState.REJECTED);
         } else if (rs.equals(RequestState.REJECTED)) {
             requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.CANCELLED)) {
             requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.ARCHIVED)) {
             // no more state transitions available from there
         } else if (rs.equals(RequestState.VALIDATED)) {
             requestStateList.add(RequestState.UNCOMPLETE);
             requestStateList.add(RequestState.NOTIFIED);
         } else if (rs.equals(RequestState.NOTIFIED)) {
             requestStateList.add(RequestState.CLOSED);
             requestStateList.add(RequestState.ARCHIVED);
 //            requestStateList.add(RequestState.ACTIVE);
 //        } else if (rs.equals(RequestState.ACTIVE)) {
 //            requestStateList.add(RequestState.EXPIRED);
 //            requestStateList.add(RequestState.CLOSED);
 //        } else if (rs.equals(RequestState.EXPIRED)) {
 //            requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.CLOSED)) {
             requestStateList.add(RequestState.ARCHIVED);
         }
 
         return (RequestState[]) requestStateList.toArray(new RequestState[0]);
     }
 
     public Set<RequestState> getStatesBefore(RequestState rs) {
 
         Set<RequestState> requestStateSet = new HashSet<RequestState>();
 
         if (rs.equals(RequestState.PENDING)) {
             // no state available before pending
         } else if (rs.equals(RequestState.COMPLETE)) {
             requestStateSet.add(RequestState.PENDING);
             requestStateSet.add(RequestState.UNCOMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.UNCOMPLETE));
         } else if (rs.equals(RequestState.UNCOMPLETE)) {
             requestStateSet.add(RequestState.PENDING);
             requestStateSet.add(RequestState.VALIDATED);
         } else if (rs.equals(RequestState.REJECTED)) {
             requestStateSet.add(RequestState.PENDING);
             requestStateSet.add(RequestState.UNCOMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.UNCOMPLETE));
             requestStateSet.add(RequestState.COMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.COMPLETE));
         } else if (rs.equals(RequestState.CANCELLED)) {
             requestStateSet.add(RequestState.PENDING);
             requestStateSet.add(RequestState.UNCOMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.UNCOMPLETE));
             requestStateSet.add(RequestState.COMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.COMPLETE));            
         } else if (rs.equals(RequestState.ARCHIVED)) {
             requestStateSet.add(RequestState.NOTIFIED);
             requestStateSet.addAll(getStatesBefore(RequestState.NOTIFIED));
             requestStateSet.add(RequestState.CLOSED);
             requestStateSet.addAll(getStatesBefore(RequestState.CLOSED));
 //            requestStateSet.add(RequestState.EXPIRED);
 //            requestStateSet.addAll(getStatesBefore(RequestState.EXPIRED));
             requestStateSet.add(RequestState.REJECTED);
             requestStateSet.addAll(getStatesBefore(RequestState.REJECTED));
             requestStateSet.add(RequestState.CANCELLED);
             requestStateSet.addAll(getStatesBefore(RequestState.CANCELLED));
         } else if (rs.equals(RequestState.VALIDATED)) {
             requestStateSet.add(RequestState.COMPLETE);
             requestStateSet.addAll(getStatesBefore(RequestState.COMPLETE));
         } else if (rs.equals(RequestState.NOTIFIED)) {
             requestStateSet.add(RequestState.VALIDATED);
             requestStateSet.addAll(getStatesBefore(RequestState.VALIDATED));
         } else if (rs.equals(RequestState.CLOSED)) {
             requestStateSet.add(RequestState.NOTIFIED);
             requestStateSet.addAll(getStatesBefore(RequestState.NOTIFIED));
 //            requestStateSet.add(RequestState.ACTIVE);
 //            requestStateSet.addAll(getStatesBefore(RequestState.ACTIVE));
 //        } else if (rs.equals(RequestState.ACTIVE)) {
 //            requestStateSet.add(RequestState.NOTIFIED);
 //            requestStateSet.addAll(getStatesBefore(RequestState.NOTIFIED));
 //        } else if (rs.equals(RequestState.EXPIRED)) {
 //            requestStateSet.add(RequestState.ACTIVE);
 //            requestStateSet.addAll(getStatesBefore(RequestState.ACTIVE));
         }
 
         return requestStateSet;
     }
 
     public RequestState[] getStatesExcludedForRunningRequests() {
         RequestState[] excludedStates = 
             new RequestState[] { RequestState.ARCHIVED, RequestState.REJECTED,
                 RequestState.CANCELLED, RequestState.CLOSED };
         return excludedStates;
     }
     
     public RequestState[] getStatesExcludedForRequestsCloning() {
         RequestState[] excludedStates = 
             new RequestState[] { RequestState.REJECTED, RequestState.CANCELLED };
         return excludedStates;
     }
 
     public List<RequestState> getEditableStates() {
         List<RequestState> result = new ArrayList<RequestState>();
 
         result.add(RequestState.PENDING);
         result.add(RequestState.COMPLETE);
         result.add(RequestState.UNCOMPLETE);
 
         return result;
     }
 
     public List<RequestState> getInstructionDoneStates() {
         List<RequestState> result = new ArrayList<RequestState>();
         result.add(RequestState.REJECTED);
         result.add(RequestState.CANCELLED);
         result.add(RequestState.NOTIFIED);
         return result;
     }
 
     private void updateLastModificationInformation(Request request, final Date date)
         throws CvqException {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
 
         Long userId = SecurityContext.getCurrentUserId();
         request.setLastInterveningUserId(userId);
 
         requestDAO.update(request);
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setCertificateService(ICertificateService certificateService) {
         this.certificateService = certificateService;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setRequestActionService(IRequestActionService requestActionService) {
         this.requestActionService = requestActionService;
     }
 
     public void setRequestNotificationService(IRequestNotificationService requestNotificationService) {
         this.requestNotificationService = requestNotificationService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
 }
