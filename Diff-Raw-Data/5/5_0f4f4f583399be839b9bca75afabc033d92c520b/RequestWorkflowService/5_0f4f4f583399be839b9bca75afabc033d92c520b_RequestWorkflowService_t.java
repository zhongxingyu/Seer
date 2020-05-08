 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.oval.ConstraintViolation;
 import net.sf.oval.Validator;
 import net.sf.oval.context.ClassContext;
 import net.sf.oval.context.FieldContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationListener;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestEvent;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.request.RequestEvent.COMP_DATA;
 import fr.cg95.cvq.business.request.RequestEvent.EVENT_TYPE;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequest;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.business.users.ActorState;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.UsersEvent;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.exception.CvqValidationException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestPdfService;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.IRequestDocumentService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.IRequestTypeService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.request.external.IRequestExternalService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * This services handles workflow tasks for requests. It is responsible for :
  * <ul>
  *  <li>Checking requested states changes are authorized</li>
  *  <li>Updating requests state and workflow information</li>
  *  <li>Creating and managing workflow action traces</li>
  * </ul>
  * 
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class RequestWorkflowService implements IRequestWorkflowService, ApplicationListener<UsersEvent>,
     ApplicationContextAware {
 
     private static Logger logger = Logger.getLogger(RequestWorkflowService.class);
     
     private IRequestPdfService requestPdfService;
     private IDocumentService documentService;
     private IHomeFolderService homeFolderService;
     private IIndividualService individualService;
     
     private IRequestServiceRegistry requestServiceRegistry;
     private IRequestActionService requestActionService;
     private IRequestExternalService requestExternalService;
     private IRequestTypeService requestTypeService;
     private IRequestDocumentService requestDocumentService;
 
     private IRequestDAO requestDAO;
     
     private ApplicationContext applicationContext;
 
     @Override
     public void createAccountCreationRequest(VoCardRequest dcvo, List<Adult> adults, List<Child> children,
             List<Adult> foreignRoleOwners, final Address address, List<Document> documents, String note)
             throws CvqException {
 
         HomeFolder homeFolder = homeFolderService.create(adults, children, address);
         
         dcvo.setHomeFolderId(homeFolder.getId());
 
         // by default, set the home folder responsible as requester
         // we need to search for it manually because there is no citizen yet in security context
         Adult homeFolderResponsible = null;
         for (Adult adult : adults) {
             if (adult.getIndividualRoles() != null) {
                 for (IndividualRole individualRole : adult.getIndividualRoles()) {
                     if (individualRole.getRole().equals(RoleType.HOME_FOLDER_RESPONSIBLE)) {
                         homeFolderResponsible = adult;
                         break;
                     }
                 }
             }
         }
         SecurityContext.setCurrentEcitizen(homeFolderResponsible);
         
         dcvo.setRequesterId(homeFolderResponsible.getId());
         dcvo.setRequesterLastName(homeFolderResponsible.getLastName());
         dcvo.setRequesterFirstName(homeFolderResponsible.getFirstName());
 
         Long requestId = finalizeAndPersist(dcvo, homeFolder, note);
 
         requestDocumentService.addDocuments(dcvo, documents);
 
         homeFolderService.saveForeignRoleOwners(homeFolder.getId(), adults, children,
                 foreignRoleOwners);
 
         HibernateUtil.getSession().flush();
 
         logger.debug("create() Created request object with id : " + requestId);
     }
     
     @Override
     public void isAccountModificationRequestAuthorized(final HomeFolder homeFolder) 
         throws CvqModelException {
         
         List<Request> otherRequests = 
             requestDAO.listByHomeFolderAndLabel(homeFolder.getId(), 
                     IRequestTypeService.HOME_FOLDER_MODIFICATION_REQUEST, 
                     getStatesExcludedForInProgressRequest(), false);
         if (otherRequests != null && !otherRequests.isEmpty())
             throw new CvqModelException("homeFolder.error.alreadyAccountModifcationInProgess");
 
         if (!homeFolder.getState().equals(ActorState.VALID))
             throw new CvqModelException("homeFolder.error.accountModifcationPossibleForValidatedAccount");
     }
 
     @Override
     public void createAccountModificationRequest(HomeFolderModificationRequest hfmr,
             List<Adult> adults, List<Child> children, List<Adult> foreignRoleOwners,
             Address adress, List<Document> documents, String note) throws CvqException {
 
         // load home folder first to check for the existence of another
         // similar request in progress
         HomeFolder homeFolder = 
             homeFolderService.getById(SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
         isAccountModificationRequestAuthorized(homeFolder);
         
         hfmr.setHomeFolderId(homeFolder.getId());
         performBusinessChecks(hfmr, null);
 
         setAdministrativeInformation(hfmr);
         requestDAO.create(hfmr);
 
         homeFolderService.modify(homeFolder.getId(), hfmr.getId(), adults, children, adress);
         
         // in case of an home folder responsible change, the new one has normally been set
         // in the security context. Yes, this seems like a hack. And so it is.
         hfmr.setRequesterId(SecurityContext.getCurrentEcitizen().getId());
         hfmr.setRequesterFirstName(SecurityContext.getCurrentEcitizen().getFirstName());
         hfmr.setRequesterLastName(SecurityContext.getCurrentEcitizen().getLastName());
         
         requestDAO.update(hfmr);
 
         // TODO REFACTORING : branch into common treatments
         // To flush new individuals and retrieve them with Query.list
         HibernateUtil.getSession().flush();
         byte[] pdfData = requestPdfService.generateCertificate(hfmr);
 
         requestActionService.addCreationAction(hfmr.getId(), new Date(), pdfData, note);
 
         requestDocumentService.addDocuments(hfmr, documents);
         
         if (foreignRoleOwners != null) {
             for (int i = 0; i < foreignRoleOwners.size(); i++) {
                 if (foreignRoleOwners.get(i).getId() != null) {
                     Adult mergeRoleOwner = (Adult)HibernateUtil.getSession().merge(foreignRoleOwners.get(i));
                     foreignRoleOwners.set(i, mergeRoleOwner);
                 }
             }
         }
         homeFolderService.saveForeignRoleOwners(hfmr.getHomeFolderId(), adults, children, 
                 foreignRoleOwners);        
 
         RequestEvent requestEvent = 
             new RequestEvent(this, EVENT_TYPE.REQUEST_CREATED, hfmr);
         if (pdfData != null)
             requestEvent.addComplementaryData(COMP_DATA.PDF_FILE, pdfData);
         applicationContext.publishEvent(requestEvent);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, String note) throws CvqException {
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen());
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestCreated(request);
         return finalizeAndPersist(request, note);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, List<Document> documents, String note) throws CvqException {
         Long requestId = create(request, note);
         requestDocumentService.addDocuments(request, documents);
         return requestId;
     }
     
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, String note)
         throws CvqException {
         HomeFolder homeFolder = performBusinessChecks(request, requester);
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestCreated(request);
         return finalizeAndPersist(request, homeFolder, note);
     }
     
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN}, privilege = ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, List<Document> documents, String note) 
         throws CvqException {
         
         HomeFolder homeFolder = performBusinessChecks(request, requester);
         
         HibernateUtil.getSession().flush();
         SecurityContext.setCurrentEcitizen(
                 homeFolderService.getHomeFolderResponsible(homeFolder.getId()));
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestCreated(request);
         requestDocumentService.addDocuments(request, documents);
         return finalizeAndPersist(request, homeFolder, note);
     }
     
     private HomeFolder performBusinessChecks(Request request, Adult requester)
         throws CvqException, CvqObjectNotFoundException {
         
         HomeFolder homeFolder = createOrSynchronizeHomeFolder(request, requester);
         
         if (!RequestState.DRAFT.equals(request.getState())) {
             IRequestService requestService = 
                 requestServiceRegistry.getRequestService(request);
             RequestType requestType = 
                 requestTypeService.getRequestTypeByLabel(requestService.getLabel());
             request.setRequestType(requestType);
             checkSubjectPolicy(request.getSubjectId(), requestService.getSubjectPolicy(), request);
         }
         
         if (request.getSubjectId() != null) {
             Individual individual = individualService.getById(request.getSubjectId());
             request.setSubjectId(individual.getId());
             request.setSubjectLastName(individual.getLastName());
             request.setSubjectFirstName(individual.getFirstName());
         }
         
         return homeFolder;
     }
     
     /**
      * Create or synchronize informations from home folder :
      * <ul>
      *   <li>Create an home folder containing the given requester if requester id is not provided 
      *         within request object (in this case, requester parameter <strong>must</strong> must
      *         be provided)</li>
      *   <li>Load and set home folder and requester informations if requester id is provided
      *         within request object (in this case, requester parameter is not used)</li>
      * </ul>
      * 
      * @return the newly created home folder or null if home folder already existed 
      */
     private HomeFolder createOrSynchronizeHomeFolder(Request request, Adult requester)
         throws CvqException, CvqModelException {
 
         // in case requester id is not filled, feed it with currently logged in ecitizen
         if (request.getRequesterId() == null && SecurityContext.getCurrentEcitizen() != null) 
             request.setRequesterId(SecurityContext.getCurrentEcitizen().getId());
         
         if (request.getRequesterId() == null) {
             IRequestService requestService = requestServiceRegistry.getRequestService(request);
             if (requestService.supportUnregisteredCreation()) {
                 logger.debug("create() Gonna create implicit home folder");
                 HomeFolder homeFolder = homeFolderService.create(requester);
                 request.setHomeFolderId(homeFolder.getId());
                 request.setRequesterId(requester.getId());
                 request.setRequesterLastName(requester.getLastName());
                 request.setRequesterFirstName(requester.getFirstName());
                 
                 SecurityContext.setCurrentEcitizen(requester);
 
                 return homeFolder;
             } else {
                 logger.error("create() refusing creation by unregistered user");
                 throw new CvqModelException("request.error.unregisteredCreationUnauthorized");
             }
         } else {
             logger.debug("create() Adult already exists, re-synchronizing it with DB");
             // load requester in order to have names information
             Individual somebody = individualService.getById(request.getRequesterId());
             if (somebody instanceof Child)
                 throw new CvqModelException("request.error.requesterMustBeAdult");
             Adult adult = (Adult) somebody;
             request.setRequesterLastName(adult.getLastName());
             request.setRequesterFirstName(adult.getFirstName());
             request.setHomeFolderId(adult.getHomeFolder().getId());
         }
 
         return null;
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkSubjectPolicy(final Long subjectId, Long homeFolderId, final String policy,
             final RequestType requestType) 
         throws CvqException, CvqModelException {
 
         // first, check general subject policy
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             if (subjectId == null)
                 throw new CvqModelException("model.request.subject_is_required");
             Individual subject = individualService.getById(subjectId);
             if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_INDIVIDUAL)) {
                 if (!(subject instanceof Individual)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_ADULT)) {
                 if (!(subject instanceof Adult)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_CHILD)) {
                 if (!(subject instanceof Child)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             }
         } else {
             if (subjectId != null)
                 throw new CvqModelException("model.request.subject_not_supported");
         }
         
         // then check that request's subject is allowed to issue this request
         // ie that it is authorized to issue it (no current one, an open season, ...)
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Individual individual = individualService.getById(subjectId);
             boolean isAuthorized = false;
             Map<Long, Set<RequestSeason>> authorizedSubjectsMap = 
                 getAuthorizedSubjects(requestType, homeFolderId);
             if (authorizedSubjectsMap != null) {
                 Set<Long> authorizedSubjects = authorizedSubjectsMap.keySet();
                 for (Long authorizedSubjectId : authorizedSubjects) {
                     if (authorizedSubjectId.equals(individual.getId())) {
                         isAuthorized = true;
                         break;
                     }
                 }
             }
             if (!isAuthorized)
                 throw new CvqModelException("request.error.subjectNotAuthorized");
         }
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkSubjectPolicy(final Long subjectId, final String policy, final Request request)
         throws CvqException, CvqModelException {
 
         // first, check general subject policy
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             if (subjectId == null)
                 throw new CvqModelException("model.request.subject_is_required");
             Individual subject = individualService.getById(subjectId);
             if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_INDIVIDUAL)) {
                 if (!(subject instanceof Individual)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_ADULT)) {
                 if (!(subject instanceof Adult)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_CHILD)) {
                 if (!(subject instanceof Child)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             }
         } else {
             if (subjectId != null)
                 throw new CvqModelException("model.request.subject_not_supported");
         }
         
         // then check that request's subject is allowed to issue this request
         // ie that it is authorized to issue it (no current one, an open season, ...)
         if (!policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Individual individual = individualService.getById(subjectId);
             boolean isAuthorized = false;
             for (Long authorizedSubjectId : getAuthorizedSubjects(request)) {
                 if (authorizedSubjectId.equals(individual.getId())) {
                     isAuthorized = true;
                     break;
                 }
             }
             if (!isAuthorized)
                 throw new CvqModelException("request.error.subjectNotAuthorized");
         }
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkRequestTypePolicy(RequestType requestType, HomeFolder homeFolder)
         throws CvqException {
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!requestType.getActive()) {
             throw new CvqModelException("requestType.message.inactive");
         }
         if (!service.supportUnregisteredCreation() && homeFolder == null) {
             throw new CvqModelException("requestType.message.onlyRegisteredUsers");
         }
         if (!requestTypeService.isRegistrationOpen(requestType.getId())) {
             throw new CvqModelException("requestType.message.registrationClosed");
         }
         if (homeFolder != null
             && !IRequestWorkflowService.SUBJECT_POLICY_NONE.equals(service.getSubjectPolicy())
             && getAuthorizedSubjects(requestType, homeFolder.getId()).isEmpty()) {
             throw new CvqModelException("requestType.message.noAuthorizedSubjects");
         }
         if (requestType.getLabel().equals(IRequestTypeService.HOME_FOLDER_MODIFICATION_REQUEST)) {
             isAccountModificationRequestAuthorized(
                 SecurityContext.getCurrentEcitizen().getHomeFolder());
         }
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void checkRequestTypePolicy(RequestType requestType, RequestSeason requestSeason,
         HomeFolder homeFolder)
         throws CvqException {
         checkRequestTypePolicy(requestType, homeFolder);
         if (requestTypeService.isOfRegistrationKind(requestType.getId())) {
             Set<RequestSeason> seasons = requestTypeService.getOpenSeasons(requestType);
             if (seasons.isEmpty() && requestSeason == null) {
                 return;
             } else if (!seasons.contains(requestSeason)) {
                 throw new CvqException("TODO");
             }
         } else {
             if (requestSeason != null) {
                 throw new CvqException("TODO");
             }
         }
     }
 
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void validate(Request request, List<String> steps, boolean useAcceptance)
         throws CvqValidationException, ClassNotFoundException, IllegalAccessException,
             InvocationTargetException, NoSuchMethodException {
         Validator validator = new Validator();
         validator.disableAllProfiles();
         if (steps == null) {
             validator.enableAllProfiles();
             validator.disableProfile("administration");
         } else {
             validator.enableProfile("default");
             for (String step : steps) {
                 validator.enableProfile(step);
             }
         }
         Map<String, List<String>> invalidFields = new LinkedHashMap<String, List<String>>();
         for (ConstraintViolation violation : validator.validate(request)) {
             collectInvalidFields(violation, invalidFields, "", "");
         }
         if (invalidFields.get("") != null) {
             Iterator<String> iterator = invalidFields.get("").iterator();
             while (iterator.hasNext()) {
                 String invalidField = iterator.next();
                 if ("subjectId".equals(invalidField)) {
                     String firstStep = request.getStepStates().keySet().iterator().next();
                     if (invalidFields.get(firstStep) == null)
                         invalidFields.put(firstStep, new ArrayList<String>(1));
                     invalidFields.get(firstStep).add(invalidField);
                     iterator.remove();
                 }
             }
             if (invalidFields.get("").isEmpty()) {
                 invalidFields.remove("");
             }
         }
         if (validator.isProfileEnabled("validation") && !useAcceptance) {
             if (invalidFields.get("validation") == null)
                 invalidFields.put("validation", new ArrayList<String>(1));
             invalidFields.get("validation").add("useAcceptance");
         }
         if (!invalidFields.isEmpty()) throw new CvqValidationException(invalidFields);
     }
 
     private void collectInvalidFields(ConstraintViolation violation,
         Map<String, List<String>> fields, String prefix, String stepName)
         throws ClassNotFoundException,  IllegalAccessException, InvocationTargetException,
             NoSuchMethodException {
         Class<? extends Annotation> annotationClass =
             Class.forName(violation.getErrorCode()).asSubclass(Annotation.class);
         if (violation.getCauses() == null) {
             String field;
             if (violation.getContext() instanceof ClassContext)
                 field = prefix;
             else {
                 String[] profiles = (String[])annotationClass.getMethod("profiles").invoke(
                     ((FieldContext)violation.getContext()).getField().getAnnotation(annotationClass));
                 if (profiles != null && profiles.length > 0)
                     stepName = profiles[0];
                field = (StringUtils.isNotBlank(prefix) ? prefix + '.' : "") + violation.getMessage();
             }
             if (fields.get(stepName) == null)
                 fields.put(stepName, new ArrayList<String>());
             fields.get(stepName).add(field);
         } else {
             String[] profiles = (String[])annotationClass.getMethod("profiles").invoke(
                     ((FieldContext)violation.getContext()).getField().getAnnotation(annotationClass));
             if (profiles != null && profiles.length > 0)
                 stepName = profiles[0];
             for (ConstraintViolation cause : violation.getCauses()) {
                collectInvalidFields(cause, fields, (StringUtils.isNotBlank(prefix) ? prefix + '.' : "") + violation.getMessage(), stepName);
             }
         }
     }
 
     /**
      * Get the list of eligible subjects for the current request service. Does
      * not make any control on already existing requests.
      */
     private Set<Long> getEligibleSubjects(final Long homeFolderId, final String policy) 
         throws CvqException {
 
         if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Set<Long> result = new HashSet<Long>();
             result.add(homeFolderId);
             return result;
         } else {
             List<Individual> individualsReference = homeFolderService.getIndividuals(homeFolderId);
             Set<Long> result = new HashSet<Long>();
             for (Individual individual : individualsReference) {
                 if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_INDIVIDUAL)) {
                     result.add(individual.getId());
                 } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_ADULT)) {
                     if (individual instanceof Adult)
                         result.add(individual.getId());
                 } else if (policy.equals(IRequestWorkflowService.SUBJECT_POLICY_CHILD)) {
                     if (individual instanceof Child)
                         result.add(individual.getId());
                 }
             }
 
             return result;
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Map<Long, Set<RequestSeason>> getAuthorizedSubjects(final RequestType requestType,
             final Long homeFolderId)
             throws CvqException, CvqObjectNotFoundException {
 
         logger.debug("getAuthorizedSubjects() searching authorized subjects for : "
                 + requestType.getLabel());
 
         IRequestService requestService = 
             requestServiceRegistry.getRequestService(requestType.getLabel());
         if (requestService.isOfRegistrationKind() && !requestType.getSeasons().isEmpty()) {
             Set<RequestSeason> openSeasons = requestTypeService.getOpenSeasons(requestType);
             // no open seasons, no registration is possible
             if (openSeasons.isEmpty())
                 return Collections.emptyMap();
 
             Set<Long> eligibleSubjects = 
                 getEligibleSubjects(homeFolderId, requestService.getSubjectPolicy());
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
 
             // by default, add every subject to all open seasons, restrictions
             // will be made next
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, new HashSet<RequestSeason>(openSeasons));
 
             // no restriction on the number of registrations per season
             // just return the whole map
             if (requestType.getAuthorizeMultipleRegistrationsPerSeason())
                 return result;
 
             for (RequestSeason season : openSeasons) {
                 // get all requests made for this season by the current home folder
                 Set<Critere> criterias = new HashSet<Critere>(3);
                 criterias.add(new Critere(Request.SEARCH_BY_HOME_FOLDER_ID,
                     homeFolderId, Critere.EQUALS));
                 criterias.add(new Critere(Request.SEARCH_BY_SEASON_ID,
                     season.getId(), Critere.EQUALS));
                 List<Request> seasonRequests = 
                     requestDAO.search(criterias, null, null, 0, 0, false);
                 for (Request request : seasonRequests) {
                     Set<RequestSeason> subjectSeasons = null;
                     if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE))
                         subjectSeasons = result.get(request.getHomeFolderId());
                     else
                         subjectSeasons = result.get(request.getSubjectId());
                     // no current request on this season, let's continue
                     if (subjectSeasons == null)
                         continue;
                     // a request on this season and it is the last one for this
                     // subject,
                     // simply remove the subject
                     else if (subjectSeasons.size() == 1)
                         if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE))
                             result.remove(request.getHomeFolderId());
                         else
                             result.remove(request.getSubjectId());
                     // a request on this season and it is not the last one for
                     // this subject,
                     // drop the season from the set of possible ones
                     else
                         subjectSeasons.remove(season);
                 }
             }
             return result;
 
         } else {
             Set<Long> eligibleSubjects = 
                 getEligibleSubjects(homeFolderId, requestService.getSubjectPolicy());
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, null);
             if (!requestService.getSupportMultiple()) {
                 RequestState[] excludedStates = getStatesExcludedForRunningRequests();
                 List<Long> homeFolderSubjectIds = requestDAO.listHomeFolderSubjectIds(homeFolderId,
                         requestService.getLabel(), excludedStates);
                 if (requestService.getSubjectPolicy().equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
                     if (!homeFolderSubjectIds.isEmpty()) {
                         return Collections.emptyMap();
                     } else {
                         return result;
                     }
                 } else {
                     for (Long subjectId : homeFolderSubjectIds)
                         result.remove(subjectId);
                     return result;
                 }
             }
             return result;
         }
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Long> getAuthorizedSubjects(final Request request)
         throws CvqException, CvqObjectNotFoundException {
         List<Long> subjects = new ArrayList<Long>();
         if (SecurityContext.getCurrentEcitizen() != null
             && !requestTypeService.getSubjectPolicy(request.getRequestType().getId())
                 .equals(IRequestWorkflowService.SUBJECT_POLICY_NONE)) {
             Map<Long, Set<RequestSeason>> authorizedSubjects =
                 getAuthorizedSubjects(request.getRequestType(),
                     SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
             if (request.getRequestSeason() == null) {
                 subjects = new ArrayList<Long>(authorizedSubjects.keySet());
             } else {
                 for (Map.Entry<Long, Set<RequestSeason>> subject : authorizedSubjects.entrySet()) {
                     if (subject.getValue().contains(request.getRequestSeason())) {
                         subjects.add(subject.getKey());
                     }
                 }
             }
             if (request.getSubjectId() != null && !subjects.contains(request.getSubjectId())) {
                 subjects.add(request.getSubjectId());
             }
         }
         return subjects;
     }
 
     // FIXME : when first entering the request creation process, stepStates is empty
     // so return null and add a generic message in view
     @Override
     @Context(types = {ContextType.UNAUTH_ECITIZEN, ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<String> getMissingSteps(Request request) {
         if (request.getStepStates() == null || request.getStepStates().size() == 0) {
             return null;
         }
         List<String> result = new ArrayList<String>();
         for (Map.Entry<String, Map<String, Object>> stepState : request.getStepStates().entrySet()) {
             if ((Boolean)stepState.getValue().get("required")
                 && !"complete".equals(stepState.getValue().get("state"))
                 && !"validation".equals(stepState.getKey())) {
                 result.add(stepState.getKey());
             }
         }
         return result;
     }
 
     private void setAdministrativeInformation(Request request) throws CvqException {
         
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         RequestType requestType = 
             requestTypeService.getRequestTypeByLabel(requestService.getLabel());
         request.setRequestType(requestType);
         if (!RequestState.DRAFT.equals(request.getState()))
             request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         request.setStep(RequestStep.INSTRUCTION);
         request.setCreationDate(new Date());
         request.setOrangeAlert(Boolean.FALSE);
         request.setRedAlert(Boolean.FALSE);
 
     }
     
     private Long finalizeAndPersist(Request request, HomeFolder homeFolder, String note)
         throws CvqException {
 
         setAdministrativeInformation(request);
 
         // requests other than acount creation that triggered an home folder creation are tied to it
         if (homeFolder != null 
                 && !request.getRequestType().getLabel().equals(IRequestTypeService.VO_CARD_REGISTRATION_REQUEST)) {
             request.setHasTiedHomeFolder(true);
         }
         
         Long requestId = requestDAO.saveOrUpdate(request).getId();
 
         if (!RequestState.DRAFT.equals(request.getState())) {
             // TODO DECOUPLING
             // To flush new individuals and retrieve them with Query.list
             HibernateUtil.getSession().flush();
             byte[] pdfData = requestPdfService.generateCertificate(request);
             
             requestActionService.addCreationAction(requestId, new Date(), pdfData, note);
     
             RequestEvent requestEvent = 
                 new RequestEvent(this, EVENT_TYPE.REQUEST_CREATED, request);
             if (pdfData != null)
                 requestEvent.addComplementaryData(COMP_DATA.PDF_FILE, pdfData);
             applicationContext.publishEvent(requestEvent);
 
         } else if (!requestActionService.hasAction(requestId, RequestActionType.CREATION)) {
             requestActionService.addDraftCreationAction(requestId, new Date());
         }
 
         return requestId;
     }
     
     /**
      * Finalize the setting of request properties (creation date, state, ...) and persist it in BD.
      * @param note TODO
      */
     private Long finalizeAndPersist(final Request request, String note)
         throws CvqException {
         return finalizeAndPersist(request, null, note);
     }
 
     @Override
     public Long getLastRequestForSubjectAndLabel(final Long subjectId, final String requestLabel) 
         throws CvqException {
         
         RequestState[] excludedStates = getStatesExcludedForRequestsCloning();
         List<Request> requests = null;
         
         if (subjectId != null)
             requests = 
                 requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates, false);
         
         if (requests == null || requests.isEmpty())
             return null;
         
         // choose the most recent version of this request 
         Request request = requests.get(0);
         if (requests.size() > 1) {
             for (Request requestCloned : requests) {
                 if (request.getCreationDate().compareTo(requestCloned.getCreationDate()) < 0)
                     request = requestCloned;
             }
         }
         return request.getId();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Request getRequestClone(final Long requestId) 
         throws CvqException {
         
         if (requestId == null) {
             throw new CvqException("request.error.requestIdRequired");
         }
         
         Request request = requestDAO.findById(requestId, true);
         if (request == null)
             throw new CvqException("request.error.notFound");
         
         try {
             XmlObject xmlRequestCopy = request.modelToXml().copy();
 
             // we want a Document parameter type and not a DocumentImpl one
             Class<?>[] parameterTypes = new Class[] {
                     xmlRequestCopy.getClass().getInterfaces()[0]
             };
             Object[] arguments = new Object[] {
                     xmlRequestCopy
             };
             Method xmlToModel = 
                 request.getClass().getMethod("xmlToModel", parameterTypes);
             Request requestClone = (Request) xmlToModel.invoke(xmlRequestCopy, arguments);
 
             purgeClonedRequest(requestClone);
             RequestType requestType = 
                 requestTypeService.getRequestTypeById(request.getRequestType().getId());
             requestClone.setRequestType(requestType);
             requestClone.setHomeFolderId(request.getHomeFolderId());
             requestClone.setSubjectId(request.getSubjectId());
             return requestClone;
 
         } catch (SecurityException e) {
             logger.error("getRequestClone() Security exception while cloning request");
             throw new CvqException("Security exception while cloning request");
         } catch (IllegalAccessException e) {
             logger.error("getRequestClone() Illegal access exception while cloning request");
             throw new CvqException("Illegal access exception while cloning request");
         } catch (InvocationTargetException e) {
             e.printStackTrace();
             logger.error("getRequestClone() Invocation target exception while cloning request");
             throw new CvqException("Invocation target exception while cloning request");
         } catch (NoSuchMethodException e) {
             logger.error("getRequestClone() No such method");
             throw new CvqException("No such method : " + e.getMessage());
         }
     }
     
     @Override
     public Request getSkeletonRequest(final String requestTypeLabel) throws CvqException {
         IRequestService service = requestServiceRegistry.getRequestService(requestTypeLabel);
         Request request = service.getSkeletonRequest();
         request.setRequestType(requestTypeService.getRequestTypeByLabel(requestTypeLabel));
         if (SecurityContext.getCurrentEcitizen() != null) {
             Adult currentEcitizen = SecurityContext.getCurrentEcitizen();
             request.setRequesterId(currentEcitizen.getId());
             request.setHomeFolderId(currentEcitizen.getHomeFolder().getId());
         }
         return request;
     }
 
     @Override
     public Request getSkeletonRequest(final String requestTypeLabel, final Long requestSeasonId)
         throws CvqException {
         Request request = getSkeletonRequest(requestTypeLabel);
         RequestType requestType = request.getRequestType();
         RequestSeason requestSeason = null;
         if (requestSeasonId != null) {
             requestSeason =
                 requestTypeService.getRequestSeason(requestType.getId(), requestSeasonId);
         }
         checkRequestTypePolicy(requestType, requestSeason,
             homeFolderService.getById(request.getHomeFolderId()));
         request.setState(RequestState.DRAFT);
         request.setRequestSeason(requestSeason);
         create(request, null);
         return request;
     }
 
     private void purgeClonedRequest(Request request) {
 
         // administrative data
         request.setId(null);
         request.setCreationDate(null);
         request.setDataState(null);
         request.setLastInterveningUserId(Long.valueOf(0));
         request.setLastModificationDate(null);
         request.setState(null);
         request.setStep(null);
         request.setValidationDate(null);
 
         // business data
         request.setRequesterId(null);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void rewindWorkflow(Request request, List<Document> documents, String note)
         throws CvqException {
         rewindWorkflow(request, note);
         requestDocumentService.addDocuments(request, documents);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void modify(Request request)
         throws CvqException {
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request);
         requestService.onRequestModified(request);
         
         updateLastModificationInformation(request, null);
     }
 
     private void delete(final Request request)
         throws CvqException, CvqObjectNotFoundException {
 
         requestDAO.delete(request);
         
         if (request.getHasTiedHomeFolder())
             homeFolderService.delete(request.getHomeFolderId());
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void delete(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = (Request) requestDAO.findById(Request.class, id);
         delete(request);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
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
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
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
 	 *            {@link IRequestPdfService})
 	 */
     private void validate(final Request request)
         throws CvqException, CvqInvalidTransitionException, CvqModelException,
             CvqObjectNotFoundException {
 
         // if no state change asked, just return silently
         if (request.getState().equals(RequestState.VALIDATED))
             return;
 
         if (!request.getState().equals(RequestState.COMPLETE))
             throw new CvqInvalidTransitionException();
 
         IRequestService requestService = requestServiceRegistry.getRequestService(request.getId());
         requestService.onRequestValidated(request);
 
         List<String> externalCheckErrors = requestExternalService.checkExternalReferential(request);
         if (!externalCheckErrors.isEmpty()) {
             throw new CvqException(StringUtils.join(externalCheckErrors.iterator(), '\n'));
         }
 
         // TODO Decoupling
         byte[] pdfData = requestPdfService.generateCertificate(request);
         if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
             request.setDocumentsArchive(
                 requestPdfService.generateDocumentsArchive(request.getDocuments()));
         }
 
         request.setState(RequestState.VALIDATED);
         request.setDataState(DataState.VALID);
         request.setStep(RequestStep.DELIVERY);
         Date date = new Date();
         request.setValidationDate(date);
         updateLastModificationInformation(request, date);
        
         requestActionService.addWorfklowAction(request.getId(), null, date,
             RequestState.VALIDATED, pdfData);
 
         validateAssociatedDocuments(request.getDocuments());
 
         // those two request types are special ones
         if (request instanceof VoCardRequest || request instanceof HomeFolderModificationRequest
                 || request.getHasTiedHomeFolder())
             homeFolderService.validate(request.getHomeFolderId());
 
 		// send request data to interested external services
         // TODO DECOUPLING
 		requestExternalService.sendRequest(request);
 
         RequestEvent requestEvent = 
             new RequestEvent(this, EVENT_TYPE.REQUEST_VALIDATED, request);
         if (pdfData != null)
             requestEvent.addComplementaryData(COMP_DATA.PDF_FILE, pdfData);
         applicationContext.publishEvent(requestEvent);
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
             
             byte[] pdfData = requestPdfService.generateCertificate(request);
             if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
                 request.setDocumentsArchive(
                     requestPdfService.generateDocumentsArchive(request.getDocuments()));
             }
 
             requestActionService.addWorfklowAction(request.getId(), null, date,
                 RequestState.CANCELLED, pdfData);
         } else {
             throw new CvqInvalidTransitionException();
         }
 
         if (request instanceof VoCardRequest)
             // invalidate home folder is creation request is cancelled
             homeFolderService.invalidate(request.getHomeFolderId());
         else if (request instanceof HomeFolderModificationRequest)
             // home folder was supposed to be valid before modification request
             homeFolderService.validate(request.getHomeFolderId());
         else if (request.getHasTiedHomeFolder())
             homeFolderService.invalidate(request.getHomeFolderId());
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
 
             byte[] pdfData = requestPdfService.generateCertificate(request);
             if (requestServiceRegistry.getRequestService(request).isArchiveDocuments()) {
                 request.setDocumentsArchive(
                     requestPdfService.generateDocumentsArchive(request.getDocuments()));
             }
 
             requestActionService.addWorfklowAction(request.getId(), motive, date,
                 RequestState.REJECTED, pdfData);
         } else {
             throw new CvqInvalidTransitionException();
         }
 
         if (request instanceof VoCardRequest)
             // invalidate home folder is creation request is cancelled
             homeFolderService.invalidate(request.getHomeFolderId());
         else if (request instanceof HomeFolderModificationRequest)
             // home folder was supposed to be valid before modification request
             homeFolderService.validate(request.getHomeFolderId());
         else if (request.getHasTiedHomeFolder())
             homeFolderService.invalidate(request.getHomeFolderId());
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
 
         if (!SecurityContext.isAdminContext()) {
             throw new CvqModelException("request.error.archiveForbidden");
         }
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
 
         if (request.getHasTiedHomeFolder())
             homeFolderService.archive(request.getHomeFolderId());
     }
 
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     private void archiveHomeFolderRequests(Long homeFolderId)
         throws CvqException {
 
         List<Request> requests = requestDAO.listByHomeFolder(homeFolderId, false);
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
 
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void rewindWorkflow(Request request, String note)
         throws CvqException, CvqInvalidTransitionException {
         if (request.getState().equals(RequestState.PENDING)
             || request.getState().equals(RequestState.UNCOMPLETE)) {
             request.setState(RequestState.PENDING);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             requestActionService.addWorfklowAction(request.getId(), note, date,
                 RequestState.PENDING, requestPdfService.generateCertificate(request));
         } else {
             throw new CvqInvalidTransitionException();
         }
     }
 
     public DataState[] getPossibleTransitions(DataState ds) {
         List<DataState> dataStateList = new ArrayList<DataState>();
 
         if (ds.equals(DataState.PENDING)) {
             dataStateList.add(DataState.VALID);
             dataStateList.add(DataState.INVALID);
         }
         return dataStateList.toArray(new DataState[dataStateList.size()]);
     }
 
 
     public RequestState[] getPossibleTransitions(RequestState rs) {
 
         List<RequestState> requestStateList = new ArrayList<RequestState>();
 
         if (rs.equals(RequestState.DRAFT)) {
             requestStateList.add(RequestState.PENDING);
         } else if (rs.equals(RequestState.PENDING)) {
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
             if (SecurityContext.isAdminContext())
                 requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.CANCELLED)) {
             if (SecurityContext.isAdminContext())
                 requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.ARCHIVED)) {
             // no more state transitions available from there
         } else if (rs.equals(RequestState.VALIDATED)) {
             requestStateList.add(RequestState.UNCOMPLETE);
             requestStateList.add(RequestState.NOTIFIED);
         } else if (rs.equals(RequestState.NOTIFIED)) {
             requestStateList.add(RequestState.CLOSED);
             if (SecurityContext.isAdminContext())
                 requestStateList.add(RequestState.ARCHIVED);
         } else if (rs.equals(RequestState.CLOSED)) {
             if (SecurityContext.isAdminContext())
                 requestStateList.add(RequestState.ARCHIVED);
         }
 
         return requestStateList.toArray(new RequestState[requestStateList.size()]);
     }
 
     public Set<RequestState> getStatesBefore(RequestState rs) {
 
         Set<RequestState> requestStateSet = new HashSet<RequestState>();
 
         if (rs.equals(RequestState.DRAFT)) {
             // no state available before draft
         } else if (rs.equals(RequestState.PENDING)) {
             requestStateSet.add(RequestState.DRAFT);
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
         }
 
         return requestStateSet;
     }
 
     private RequestState[] getStatesExcludedForInProgressRequest() {
         RequestState[] excludedStates = 
             new RequestState[] { RequestState.ARCHIVED, RequestState.REJECTED,
                 RequestState.CANCELLED, RequestState.CLOSED, RequestState.NOTIFIED,
                 RequestState.VALIDATED };
         return excludedStates;        
     }
     
     public RequestState[] getStatesExcludedForRunningRequests() {
         RequestState[] excludedStates = 
             new RequestState[] { RequestState.ARCHIVED, RequestState.REJECTED,
                 RequestState.CANCELLED, RequestState.CLOSED };
         return excludedStates;
     }
     
     /**
      * Actually the opposite of the {@link #getStatesExcludedForRunningRequests()}, except for the
      * {@link RequestState#ARCHIVED} state which is excluded from everything
      */
     public RequestState[] getStatesExcludedForRequestsCloning() {
         RequestState[] excludedStates = 
             new RequestState[] { RequestState.PENDING, RequestState.COMPLETE, 
                 RequestState.UNCOMPLETE, RequestState.VALIDATED, RequestState.ARCHIVED };
         return excludedStates;
     }
 
     public List<RequestState> getEditableStates() {
         List<RequestState> result = new ArrayList<RequestState>();
 
         result.add(RequestState.PENDING);
         result.add(RequestState.COMPLETE);
         result.add(RequestState.UNCOMPLETE);
 
         return result;
     }
 
     public boolean isEditable(final Long requestId) throws CvqObjectNotFoundException {
         Request request = (Request) requestDAO.findById(Request.class, requestId);
         String requestTypeLabel = request.getRequestType().getLabel();
         if ((RequestState.DRAFT.equals(request.getState())
                 || RequestState.PENDING.equals(request.getState())
                 || RequestState.UNCOMPLETE.equals(request.getState())) 
                 && !IRequestTypeService.VO_CARD_REGISTRATION_REQUEST.equals(requestTypeLabel)
                 && !IRequestTypeService.HOME_FOLDER_MODIFICATION_REQUEST.equals(requestTypeLabel))
             return true;
         
         return false;
     }
     
     @Override
     public boolean isSupportMultiple(final String requestLabel) 
         throws CvqException {
         IRequestService requestService = requestServiceRegistry.getRequestService(requestLabel);
         return requestService.getSupportMultiple();
     }
     
     public List<RequestState> getInstructionDoneStates() {
         List<RequestState> result = new ArrayList<RequestState>();
         result.add(RequestState.REJECTED);
         result.add(RequestState.CANCELLED);
         result.add(RequestState.NOTIFIED);
         return result;
     }
 
     private void updateLastModificationInformation(Request request,
         final Date date) {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
 
         Long userId = SecurityContext.getCurrentUserId();
         request.setLastInterveningUserId(userId);
 
         requestDAO.update(request);
     }
 
     @Override
     public void onApplicationEvent(UsersEvent usersEvent) {
         logger.debug("onApplicationEvent() got an home folder event of type " + usersEvent.getEvent());
         if (usersEvent.getEvent().equals(UsersEvent.EVENT_TYPE.HOME_FOLDER_ARCHIVE)) {
             logger.debug("onApplicationEvent() gonna archive home folder " + usersEvent.getHomeFolderId());
             try {
                 archiveHomeFolderRequests(usersEvent.getHomeFolderId());
             } catch (CvqException e) {
                 // FIXME : something better to do ?
                 e.printStackTrace();
                 throw new RuntimeException();
             }
         }
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 
     public void setRequestActionService(IRequestActionService requestActionService) {
         this.requestActionService = requestActionService;
     }
 
     public void setRequestExternalService(IRequestExternalService requestExternalService) {
         this.requestExternalService = requestExternalService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setRequestTypeService(IRequestTypeService requestTypeService) {
         this.requestTypeService = requestTypeService;
     }
 
     public void setRequestDocumentService(IRequestDocumentService requestDocumentService) {
         this.requestDocumentService = requestDocumentService;
     }
 
     public void setRequestPdfService(IRequestPdfService requestPdfService) {
         this.requestPdfService = requestPdfService;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext arg0) throws BeansException {
         this.applicationContext = arg0;
     }
 }
