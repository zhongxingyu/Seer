 package fr.cg95.cvq.service.request.impl;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestForm;
 import fr.cg95.cvq.business.request.RequestFormType;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestStep;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.request.Requirement;
 import fr.cg95.cvq.business.request.DisplayGroup;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequest;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.business.users.payment.PaymentState;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.request.IRequestActionDAO;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.IRequestFormDAO;
 import fr.cg95.cvq.dao.request.IRequestNoteDAO;
 import fr.cg95.cvq.dao.request.IRequestTypeDAO;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ICategoryService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.document.IDocumentTypeService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.service.users.ICertificateService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.localization.ILocalizationService;
 import fr.cg95.cvq.util.mail.IMailService;
 import fr.cg95.cvq.xml.common.SubjectType;
 
 
 /**
  * Partial implementation of the {@link IRequestService}, only provides functionalities
  * common to all request types.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public abstract class RequestService implements IRequestService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(RequestService.class);
 
     protected String fopConfig;
     protected String localReferentialFilename;
     protected String placeReservationFilename;
     protected String externalReferentialFilename;
     protected Boolean supportUnregisteredCreation;
     protected String subjectPolicy = SUBJECT_POLICY_NONE;
     protected String label;
     protected String xslFoFilename;
     protected Boolean isOfRegistrationKind;
 
     protected ICategoryService categoryService;
     protected IDocumentService documentService;
     protected IDocumentTypeService documentTypeService;
     protected IHomeFolderService homeFolderService;
     protected ICertificateService certificateService;
     protected IRequestServiceRegistry requestServiceRegistry;
     protected ILocalAuthorityRegistry localAuthorityRegistry;
     protected IMailService mailService;
     protected ILocalizationService localizationService;
     protected IExternalService externalService;
     protected IIndividualService individualService;
 
     protected IGenericDAO genericDAO;
     protected IRequestDAO requestDAO;
     protected IRequestTypeDAO requestTypeDAO;
     protected IRequestNoteDAO requestNoteDAO;
     protected IRequestActionDAO requestActionDAO;
     protected IRequestFormDAO requestFormDAO;
 
     protected RequestWorkflowService requestWorkflowService;
 
     private ListableBeanFactory beanFactory;
 
     public RequestService() {
         super();
     }
 
     public void init() throws CvqConfigurationException {
         // register with the request registry
         requestServiceRegistry.registerService(this, getLabel());
 
         if (supportUnregisteredCreation == null)
             supportUnregisteredCreation = Boolean.FALSE;
         
         this.homeFolderService = (IHomeFolderService)
             beanFactory.getBeansOfType(IHomeFolderService.class, false, false).values().iterator().next();
         this.externalService = (IExternalService)
             beanFactory.getBeansOfType(IExternalService.class, false, false).values().iterator().next();
     }
 
     private Critere getCurrentUserFilter() throws CvqException {
 
         Critere crit = new Critere();
         if (SecurityContext.isBackOfficeContext()) {
             Agent agent = SecurityContext.getCurrentAgent();
             Set<CategoryRoles> agentCategoryRoles = agent.getCategoriesRoles();
             if (agentCategoryRoles == null || agentCategoryRoles.isEmpty())
                 return null;
             StringBuffer sb = new StringBuffer();
             for (CategoryRoles categoryRoles : agentCategoryRoles) {
                 if (sb.length() > 0)
                     sb.append(",");
                 sb.append("'")
                     .append(categoryRoles.getCategory().getId())
                     .append("'");
             }
             crit.setAttribut("belongsToCategory");
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(sb.toString());
         } else if (SecurityContext.isFrontOfficeContext()) {
             Adult adult = SecurityContext.getCurrentEcitizen();
             crit.setAttribut(Request.SEARCH_BY_HOME_FOLDER_ID);
             crit.setComparatif(Critere.EQUALS);
             crit.setValue(adult.getHomeFolder().getId());
         } else {
             return null;
         }
 
         return crit;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT, privilege=ContextPrivilege.NONE)
     public List<Request> get(Set<Critere> criteriaSet, final String sort, final String dir,
             final int recordsReturned, final int startIndex)
         throws CvqException {
 
         if (criteriaSet == null)
             criteriaSet = new HashSet<Critere>();
         Critere userFilterCritere = getCurrentUserFilter();
         if (userFilterCritere != null)
             criteriaSet.add(userFilterCritere);
 
         return requestDAO.search(criteriaSet, sort, dir, recordsReturned, startIndex);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT, privilege=ContextPrivilege.NONE)
     public Long getCount(Set<Critere> criteriaSet) throws CvqException {
 
         if (criteriaSet == null)
             criteriaSet = new HashSet<Critere>();
         Critere userFilterCritere = getCurrentUserFilter();
         if (userFilterCritere != null)
             criteriaSet.add(userFilterCritere);
 
         return requestDAO.count(criteriaSet);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Request getById(final Long id)
         throws CvqException, CvqObjectNotFoundException {
             return (Request) requestDAO.findById(Request.class, id);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByRequesterId(final Long requesterId)
         throws CvqException {
 
         return requestDAO.listByRequester(requesterId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getBySubjectId(final Long subjectId)
         throws CvqException, CvqObjectNotFoundException {
 
         return requestDAO.listBySubject(subjectId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getBySubjectIdAndRequestLabel(final Long subjectId, 
             final String requestLabel, boolean retrieveArchived)
         throws CvqException, CvqObjectNotFoundException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.label_required");
 
         RequestState[] excludedStates = null;
         if (!retrieveArchived)
             excludedStates = new RequestState[] { RequestState.ARCHIVED };
 
         return requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates);
     }
 
     public List<DisplayGroup> getAllDisplayGroups() {
         return this.requestTypeDAO.listAllDisplayGroup();  
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByHomeFolderId(final Long homeFolderId) throws CvqException {
 
 		return requestDAO.listByHomeFolder(homeFolderId);
 	}
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<Request> getByHomeFolderIdAndRequestLabel(final Long homeFolderId, 
             final String requestLabel)
         throws CvqException, CvqObjectNotFoundException {
 
         return requestDAO.listByHomeFolderAndLabel(homeFolderId, requestLabel, null);
     }
 
     private void updateLastModificationInformation(Request request, final Date date)
         throws CvqException {
 
         // update request's last modification date
         if (date != null)
             request.setLastModificationDate(date);
         else
             request.setLastModificationDate(new Date());
         request.setLastInterveningAgentId(SecurityContext.getCurrentUserId());
 
         requestDAO.update(request);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<RequestNote> getNotes(final Long id)
         throws CvqException {
 
         // TODO ACMF : if ecitizen, filter notes he is not authorized to see 
         // (eg instruction internal)
         return requestNoteDAO.listByRequest(id);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void addNote(final Long requestId, final RequestNoteType rtn, final String note)
         throws CvqException, CvqObjectNotFoundException {
 
 
         Long agentId = SecurityContext.getCurrentUserId();
 
 	    RequestNote requestNote = new RequestNote();
         requestNote.setType(rtn);
         requestNote.setNote(note);
         requestNote.setAgentId(agentId);
 
         Request request = getById(requestId);
 	    if (request.getNotes() == null) {
 	        Set<RequestNote> notes = new HashSet<RequestNote>();
 	        notes.add(requestNote);
 	        request.setNotes(notes);
     	} else {
 	        request.getNotes().add(requestNote);
 	    }
 
         updateLastModificationInformation(request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocuments(final Long requestId, final Set<Long> documentsId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         for (Long documentId : documentsId) {
             RequestDocument requestDocument = new RequestDocument();
             requestDocument.setDocumentId(documentId);
             if (request.getDocuments() == null) {
                 Set<RequestDocument> documentSet = new HashSet<RequestDocument>();
                 documentSet.add(requestDocument);
                 request.setDocuments(documentSet);
             } else {
                 request.getDocuments().add(requestDocument);
             }
         }
 
         updateLastModificationInformation(request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
 
         updateLastModificationInformation(request, null);
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getId() != null)
             request = getById(request.getId());
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void removeDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         if (request.getId() != null)
             request = getById(request.getId());
         Iterator<RequestDocument> it = request.getDocuments().iterator();
         while (it.hasNext()) {
             RequestDocument rd = it.next();
             if (rd.getDocumentId().equals(documentId)){
                 it.remove();
             }
         }
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<RequestAction> getActions(final Long id)
         throws CvqException {
 
         return requestActionDAO.listByRequest(id);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public boolean hasAction(final Long requestId, final String label)
         throws CvqException {
         return requestActionDAO.hasAction(requestId, label);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void addAction(final Long requestId, final String label, final String note)
         throws CvqException {
 
         Request request = getById(requestId);
         addActionTrace(label, note, new Date(), null, request, null);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void addSystemAction(final Long requestId, final String label)
         throws CvqException {
 
         Request request = getById(requestId);
         addActionTrace(label, null, new Date(), null, request, null);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(final Long requestId)
         throws CvqException {
 
         Request request = getById(requestId);
         return request.getDocuments();
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(Request request)
         throws CvqException {
         if (request.getId() != null)
             request = getById(request.getId());
         return request.getDocuments();
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public byte[] getCertificate(final Long id, final RequestState requestState)
         throws CvqException {
 
         RequestAction requestAction =
             requestActionDAO.findByRequestIdAndResultingState(id,requestState);
 
         return requestAction != null ? requestAction.getFile() : null;
     }
 
     public List<RequestType> getAllRequestTypes()
         throws CvqException {
 
         // ecitizens can see all activated requests types
         if (SecurityContext.isFrontOfficeContext())
             return requestTypeDAO.listByCategoryAndState(null, true);
         
         if (SecurityContext.isAdminContext())
             return requestTypeDAO.listAll();
 
         // if agent is admin, return all categories ...
         if (SecurityContext.getCurrentCredentialBean().hasSiteAdminRole())
             return requestTypeDAO.listAll();
             
         // else filters categories it is authorized to see
         CategoryRoles[] authorizedCategories = 
             SecurityContext.getCurrentCredentialBean().getCategoryRoles();
         List<RequestType> results = new ArrayList<RequestType>();
         for (CategoryRoles categoryRole : authorizedCategories) {
             results.addAll(categoryRole.getCategory().getRequestTypes());
         }
 
         return results;
     }
 
     @Override
     public RequestType getRequestTypeById(final Long requestTypeId)
         throws CvqException {
         
         return (RequestType) requestTypeDAO.findById(RequestType.class, requestTypeId);
     }
 
     @Override
     public RequestType getRequestTypeByLabel(final String requestLabel)
         throws CvqException {
         
         return requestTypeDAO.findByLabel(requestLabel);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public List<RequestType> getRequestsTypes(final Long categoryId, final Boolean active)
         throws CvqException {
         
         return requestTypeDAO.listByCategoryAndState(categoryId,active);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void modifyRequestType(RequestType requestType)
         throws CvqException {
         requestTypeDAO.update(requestType);
     }
 
     @Override
     public Set<DocumentType> getAllowedDocuments(final Long requestTypeId)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         Set<Requirement> requirements = requestType.getRequirements();
         if (requirements != null) {
             Set<DocumentType> resultSet = new LinkedHashSet<DocumentType>();
             for (Requirement requirement : requirements) {
                 resultSet.add(requirement.getDocumentType());
             }
             return resultSet;
         } 
 
         return null;
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void modifyRequestTypeRequirement(Long requestTypeId, Requirement requirement)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         if (requestType.getRequirements() == null)
             return;
         requestType.getRequirements().add(requirement);
         requestTypeDAO.update(requestType);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void addRequestTypeRequirement(Long requestTypeId, Long documentTypeId)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         if (requestType.getRequirements() == null)
             requestType.setRequirements(new HashSet<Requirement>());
         DocumentType documentType = 
             documentTypeService.getDocumentTypeById(documentTypeId);
         Requirement requirement = new Requirement();
         requirement.setMultiplicity(Integer.valueOf("1"));
         requirement.setRequestType(requestType);
         requirement.setSpecial(false);
         requirement.setDocumentType(documentType);
         if (!requestType.getRequirements().contains(requirement)) {
             requestType.getRequirements().add(requirement);
             requestTypeDAO.update(requestType);
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void removeRequestTypeRequirement(Long requestTypeId, Long documentTypeId)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         if (requestType.getRequirements() == null)
             return;
 
         boolean foundRequirement = false;
         for (Requirement requirement : requestType.getRequirements()) {
             if (requirement.getDocumentType().getId().equals(documentTypeId)) {
                 requestType.getRequirements().remove(requirement);
                 foundRequirement = true;
                 break;
             }
         }
 
         if (foundRequirement) {
             logger.debug("removeRequestTypeRequirement() found requirement to remove");
             requestTypeDAO.update(requestType);
         }
     }
 
     /**
      * Get the list of seasons whose registrations are currently open.
      *
      * @return null if no seasons defined for this request type, an empty list if no
      *  seasons with opened registrations, the list of seasons with opened registrations
      *  otherwise.
      */
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     protected Set<RequestSeason> getOpenSeasons(RequestType requestType) {
 
         if (requestType.getSeasons() != null && !requestType.getSeasons().isEmpty()) {
             Date now = new Date();
             Set<RequestSeason> openSeasons = new HashSet<RequestSeason>();
             Set<RequestSeason> seasons = requestType.getSeasons();
             for (RequestSeason requestSeason : seasons) {
                 if (requestSeason.getRegistrationStart().before(now)
                         && requestSeason.getRegistrationEnd().after(now))
                     openSeasons.add(requestSeason);
             }
 
             return openSeasons;
         }
 
         return null;
     }
 
     //////////////////////////////////////////////////////////
     // Season related methods
     //////////////////////////////////////////////////////////
 
     private void checkSeasonSupport(RequestType requestType) throws CvqModelException {
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             throw new CvqModelException("request.season.not_supported");
     }
 
     private void checkSeasondDatesOverlapping(RequestSeason rs1, RequestSeason rs2)
         throws CvqModelException {
 
         if (rs1.getRegistrationStart().before(rs2.getRegistrationEnd())
                 && rs1.getRegistrationEnd().after(rs2.getRegistrationEnd())
                 || rs1.getRegistrationEnd().after(rs2.getRegistrationStart())
                     && rs1.getRegistrationStart().before(rs2.getRegistrationStart()))
             throw new CvqModelException("request.season.seasons_registration_overlapped");
 
         if (rs1.getEffectStart().before(rs2.getEffectEnd())
                 && rs1.getEffectEnd().after(rs2.getEffectEnd())
                 || rs1.getEffectEnd().after(rs2.getEffectStart())
                     && rs1.getEffectStart().before(rs2.getEffectStart()))
             throw new CvqModelException("request.season.seasons_effect_overlapped");
     }
 
     private void checkSeasonValidity (Set<RequestSeason> seasons, RequestSeason requestSeason)
         throws CvqModelException {
         // Set now Date at 00h00:00 0000
         Calendar calendar = new GregorianCalendar();
         calendar.setTime(new Date());
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         Date dayNow = calendar.getTime();
 
         // check validity of seasons data
         if (requestSeason.getRegistrationStart() == null)
             throw new CvqModelException("request.season.registration_start_required");
         if (requestSeason.getRegistrationEnd() == null)
             throw new CvqModelException("request.season.registration_end_required");
         if (requestSeason.getEffectStart() == null)
             throw new CvqModelException("request.season.effect_start_required");
         if (requestSeason.getEffectEnd() == null)
             throw new CvqModelException("request.season.effect_end_required");
 
         if (requestSeason.getUuid() == null)
             requestSeason.setUuid(UUID.randomUUID().toString());
 
         // check registrationt start
         if (requestSeason.getRegistrationStart().before(dayNow))
             throw new CvqModelException("request.season.registration_start_before_now");
 
         // check scheduling chronological respect
         if (!requestSeason.getRegistrationStart().before(requestSeason.getRegistrationEnd()))
             throw new CvqModelException("request.season.registration_start_after_registration_end");
         if (!requestSeason.getEffectStart().before(requestSeason.getEffectEnd()))
             throw new CvqModelException("request.season.effect_start_after_effect_end");
 
         // Registration and effect date overlapping policy
         if (!requestSeason.getRegistrationStart().before(requestSeason.getEffectStart()))
             throw new CvqModelException("request.season.registration_start_after_effect_start");
         if (!requestSeason.getRegistrationEnd().before(requestSeason.getEffectEnd()))
             throw new CvqModelException("request.season.registration_end_after_effect_end");
 
         // check season's registration dates do not overlap and season's effect dates to
         for(RequestSeason rs : seasons) {
             if (!requestSeason.getUuid().equals(rs.getUuid())) {
                 checkSeasondDatesOverlapping(requestSeason, rs);
 
                 // test the label uniqueness
                 if (rs.getLabel().equals(requestSeason.getLabel()))
                     throw new CvqModelException("request.season.already_used_label");
             }
             // This rules apply just for modification
             else {
                 if (rs.getEffectEnd().before(dayNow))
                     throw new CvqModelException("request.season.effect_ended");
                 if (rs.getRegistrationStart().before(dayNow)
                         && ! rs.getRegistrationStart().equals(requestSeason.getRegistrationStart()))
                     throw new CvqModelException("request.season.registration_started");
 
             }
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void addRequestTypeSeason(final Long requestTypeId, RequestSeason requestSeason)
             throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         checkSeasonSupport(requestType);
 
         Set<RequestSeason> seasons = requestType.getSeasons();
         if (seasons == null)
             seasons = new HashSet<RequestSeason>();
         
         checkSeasonValidity(seasons, requestSeason);
 
         requestSeason.setRequestType(requestType);
         seasons.add(requestSeason);
 
         requestTypeDAO.update(requestType);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void modifyRequestTypeSeason(final Long requestTypeId, RequestSeason requestSeason)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         checkSeasonSupport(requestType);
         
         Set<RequestSeason> seasons = requestType.getSeasons();
         if (seasons == null)
             throw new CvqModelException("requestType.error.noSeasonFound");
         
         checkSeasonValidity(seasons, requestSeason);
 
         Iterator<RequestSeason> it = seasons.iterator();
         while (it.hasNext()) {
             RequestSeason rs = it.next();
             if (rs.getUuid().equals(requestSeason.getUuid())){
                 it.remove();
             }
         }
         seasons.add(requestSeason);
 
         requestTypeDAO.update(requestType);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.MANAGE)
     public void removeRequestTypeSeason(final Long requestTypeId, final String requestSeasonUuid)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         checkSeasonSupport(requestType);
 
         Set<RequestSeason> seasons = requestType.getSeasons();
         Iterator<RequestSeason> it = seasons.iterator();
         while(it.hasNext()) {
             RequestSeason rs = it.next();
             if (rs.getUuid().equals(requestSeasonUuid))
                 it.remove();
         }
         requestType.setSeasons(seasons);
 
         requestTypeDAO.update(requestType);
     }
 
     public boolean isRegistrationOpen(final Long requestTypeId) throws CvqException {
 
         if (!isOfRegistrationKind())
             return true;
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons == null)
             return true;
         if (openSeasons.isEmpty())
             return false;
         else
             return true;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestSeason getRequestAssociatedSeason(Long requestId) throws CvqException {
 
         Request request = getById(requestId);
         RequestType requestType = request.getRequestType();
 
         if (requestType.getSeasons() != null) {
             for (RequestSeason requestTypeSeason : requestType.getSeasons()) {
                 if (requestTypeSeason.getUuid().equals(request.getSeasonUuid()))
                     return requestTypeSeason;
             }
         }
         return null;
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestSeason> getRequestTypeSeasons(Long requestTypeId)
         throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         return requestType.getSeasons();
     }
 
     //////////////////////////////////////////////////////////
     // Payment related methods
     //////////////////////////////////////////////////////////
 
     public final void notifyPaymentResult(final Payment payment)
         throws CvqException {
 
         // for each different request found in purchased items list, notify the associated
         // service of payment result status
         Set<Long> requests = new HashSet<Long>();
         Set<PurchaseItem> purchaseItems = payment.getPurchaseItems();
         for (PurchaseItem purchaseItem : purchaseItems) {
             // if purchase item is bound to a request, notify the corresponding service
             if (purchaseItem.getRequestId() != null)
                 requests.add(purchaseItem.getRequestId());
         }
 
         if (!requests.isEmpty()) {
         	for (Long requestId : requests) {
         	    Request request = getById(requestId);
                 IRequestService requestService = 
                     requestServiceRegistry.getRequestService(getById(requestId));
                 if (payment.getState().equals(PaymentState.VALIDATED))
                     requestService.onPaymentValidated(request, payment.getBankReference());
                 else if (payment.getState().equals(PaymentState.CANCELLED))
                     requestService.onPaymentCancelled(request);
                 else if (payment.getState().equals(PaymentState.REFUSED))
                     requestService.onPaymentRefused(request);
             }
         }
 
         externalService.creditHomeFolderAccounts(payment);
     }
 
     public boolean hasMatchingExternalService(final String requestLabel)
         throws CvqException {
 
         return externalService.hasMatchingExternalService(requestLabel);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Date, String> getConsumptionsByRequest(final Long requestId, final Date dateFrom,
             final Date dateTo)
         throws CvqException {
 
         Request request = getById(requestId);
         if (request.getState().equals(RequestState.ARCHIVED)) {
             logger.debug("getConsumptionsByRequest() Filtering archived request");
             return null;
         }
 
         return externalService.getConsumptionsByRequest(request, dateFrom, dateTo);
     }
 
     public String getConsumptionsField()
         throws CvqException {
         return null;
     }
 
     //////////////////////////////////////////////////////////
     // Workflow related methods
     //////////////////////////////////////////////////////////
 
     @Override
     public void prepareDraft(Request request) throws CvqException {
         request.setDraft(true);
         request.setHomeFolderId(SecurityContext.getCurrentEcitizen().getHomeFolder().getId());
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public Long processDraft(Request request) throws CvqException {
         if(request.getId() == null) {
             return this.createDraft(request);
         } else {
             this.modifyDraft(request);
             return request.getId();
         }
     }
     
     @Override
     public Long createDraft(Request request) throws CvqException {
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen(), null);
         return finalizeAndPersist(request);
     }
     
     @Override
     public void modifyDraft(Request request) throws CvqException {
         if(this.isSubjectChanged(request)) {
             this.createDraft(request);
         } else {
             createOrSynchronizeHomeFolder(request, null);
             finalizeAndPersist(request);
         }
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void finalizeDraft(Request request) throws CvqException {
         request.setDraft(false);
         this.modifyDraft(request);
     }
     
     protected boolean isSubjectChanged(Request request) {
         Long subjectId = this.requestDAO.getSubjectId(request.getId());
         if(subjectId == null) {
             if(request.getSubjectId() != null) return true;
             else return false;
         }
         return !subjectId.equals(request.getSubjectId());
     }
     
     protected void notifyRequestCreation(Request request, byte[] pdfData)
         throws CvqException {
 
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Adult requester = (Adult) individualService.getById(request.getRequesterId());
         if (requester.getEmail() != null && !requester.getEmail().equals("")) {
             Map<String, String> ecitizenCreationNotifications =
                 lacb.getEcitizenCreationNotifications();
             if (ecitizenCreationNotifications == null) {
                 logger.warn("notifyRequestCreation() ecitizen creation notifications not configured !");
                 return;
             }
             String mailData = ecitizenCreationNotifications.get("mailData");
             Boolean attachPdf =
                 Boolean.valueOf(ecitizenCreationNotifications.get("attachPdf"));
             String mailDataBody =
                 localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE, mailData, false);
 
             if (mailDataBody == null) {
                 logger.warn("notifyRequestCreation() no mail data for ecitizen request creation notification");
             } else {
                 StringBuffer mailSubject = new StringBuffer();
                 mailSubject.append("[").append(lacb.getDisplayTitle()).append("] ")
                     .append(ecitizenCreationNotifications.get("mailSubject"));
 
                 if (attachPdf) {
                     mailService.send(null, requester.getEmail(), null,
                             mailSubject.toString(), mailDataBody, pdfData, "Recu_Demande.pdf");
                 } else {
                     mailService.send(null, requester.getEmail(), null,
                             mailSubject.toString(), mailDataBody);
                 }
             }
         }
     }
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public Long create(Request request)
         throws CvqException, CvqObjectNotFoundException {
 
         performBusinessChecks(request, SecurityContext.getCurrentEcitizen(), null);
         
         return finalizeAndPersist(request);
     }
 
     @Override
     @Context(type=ContextType.UNAUTH_ECITIZEN,privilege=ContextPrivilege.WRITE)
     public Long create(Request request, Adult requester, Individual subject)
         throws CvqException {
         
         HomeFolder homeFolder = performBusinessChecks(request, requester, subject);
         
         return finalizeAndPersist(request, homeFolder);
     }
     
     protected HomeFolder performBusinessChecks(Request request,Adult requester,Individual subject)
         throws CvqException, CvqObjectNotFoundException {
         
         HomeFolder homeFolder = createOrSynchronizeHomeFolder(request, requester);
         
         if(!request.getDraft() || request.getDraft() == null)
             checkSubjectPolicy(request.getSubjectId(),request.getHomeFolderId(),getSubjectPolicy());
         
         if (request.getSubjectId() != null) {
             Individual individual = individualService.getById(request.getSubjectId());
             request.setSubjectId(individual.getId());
             request.setSubjectLastName(individual.getLastName());
             request.setSubjectFirstName(individual.getFirstName());
         }
         
         return homeFolder;
     }
     
     protected void performBusinessChecks(final Request request) throws CvqException {
         performBusinessChecks(request, null, null);
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
     protected HomeFolder createOrSynchronizeHomeFolder(Request request, Adult requester)
         throws CvqException, CvqModelException {
 
         // in case requester id is not filled, feed it with currently logged in ecitizen
         if (request.getRequesterId() == null && SecurityContext.getCurrentEcitizen() != null) 
             request.setRequesterId(SecurityContext.getCurrentEcitizen().getId());
         
         if (request.getRequesterId() == null) {
             if (supportUnregisteredCreation.booleanValue()) {
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
 
     /**
      * Perform checks wrt subject policies :
      * <ul>
      *   <li>Check that subject is coherent wrt the request's policy.</li>
      *   <li>Check that subject is allowed to issue a request of the given type</li>
      * </ul>
      * 
      * @throws CvqModelException if there's a policy violation
      */
     private void checkSubjectPolicy(final Long subjectId, Long homeFolderId, final String policy) 
         throws CvqException, CvqModelException {
 
         // first, check general subject policy
         if (!policy.equals(SUBJECT_POLICY_NONE)) {
             if (subjectId == null)
                 throw new CvqModelException("model.request.subject_is_required");
             Individual subject = individualService.getById(subjectId);
             if (policy.equals(SUBJECT_POLICY_INDIVIDUAL)) {
                 if (!(subject instanceof Individual)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(SUBJECT_POLICY_ADULT)) {
                 if (!(subject instanceof Adult)) {
                     throw new CvqModelException("model.request.wrong_subject_type");
                 }
             } else if (policy.equals(SUBJECT_POLICY_CHILD)) {
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
         if (!getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Individual individual = individualService.getById(subjectId);
             boolean isAuthorized = false;
             Map<Long, Set<RequestSeason>> authorizedSubjectsMap = 
                 getAuthorizedSubjects(homeFolderId);
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
 
     /**
      * Get the list of eligible subjects for the current request service. Does
      * not make any control on already existing requests.
      */
     private Set<Long> getEligibleSubjects(final Long homeFolderId) throws CvqException {
 
         if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
             Set<Long> result = new HashSet<Long>();
             result.add(homeFolderId);
             return result;
         } else {
             List<Individual> individualsReference = homeFolderService.getIndividuals(homeFolderId);
             Set<Long> result = new HashSet<Long>();
             for (Individual individual : individualsReference) {
                 if (getSubjectPolicy().equals(SUBJECT_POLICY_INDIVIDUAL)) {
                     result.add(individual.getId());
                 } else if (getSubjectPolicy().equals(SUBJECT_POLICY_ADULT)) {
                     if (individual instanceof Adult)
                         result.add(individual.getId());
                 } else if (getSubjectPolicy().equals(SUBJECT_POLICY_CHILD)) {
                     if (individual instanceof Child)
                         result.add(individual.getId());
                 }
             }
 
             return result;
         }
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Map<Long, Set<RequestSeason>> getAuthorizedSubjects(final Long homeFolderId)
             throws CvqException, CvqObjectNotFoundException {
 
         RequestType requestType = requestTypeDAO.findByLabel(getLabel());
         logger.debug("getAuthorizedSubjects() searching authorized subjects for : "
                 + requestType.getLabel());
 
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons != null) {
             // no open seasons, no registration is possible
             if (openSeasons.isEmpty())
                 return null;
 
             Set<Long> eligibleSubjects = getEligibleSubjects(homeFolderId);
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
 
             // by default, add every subject to all open seasons, restrictions
             // will be made next
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, openSeasons);
 
             // no restriction on the number of registrations per season
             // just return the whole map
             if (requestType.getAuthorizeMultipleRegistrationsPerSeason())
                 return result;
 
             for (RequestSeason season : openSeasons) {
                 // get all requests made for this season by the current home
                 // folder
                 List<Request> seasonRequests = requestDAO.listByHomeFolderAndSeason(homeFolderId,
                         season.getUuid());
                 for (Request request : seasonRequests) {
                     Set<RequestSeason> subjectSeasons = null;
                     if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
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
                         if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE))
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
             Set<Long> eligibleSubjects = getEligibleSubjects(homeFolderId);
             Map<Long, Set<RequestSeason>> result = new HashMap<Long, Set<RequestSeason>>();
             for (Long subjectId : eligibleSubjects)
                 result.put(subjectId, null);
             RequestState[] excludedStates = requestWorkflowService
                     .getStatesExcludedForRunningRequests();
             List<Long> homeFolderSubjectIds = requestDAO.listHomeFolderSubjectIds(homeFolderId,
                     getLabel(), excludedStates);
             if (getSubjectPolicy().equals(SUBJECT_POLICY_NONE)) {
                 if (!homeFolderSubjectIds.isEmpty()) {
                     return null;
                 } else {
                     return result;
                 }
             } else {
                 for (Long subjectId : homeFolderSubjectIds)
                     result.remove(subjectId);
                 return result;
             }
         }
     }
 
     protected void setAdministrativeInformation(Request request) throws CvqException {
         
         RequestType requestType = getRequestTypeByLabel(getLabel());
         request.setRequestType(requestType);
         request.setState(RequestState.PENDING);
         request.setDataState(DataState.PENDING);
         request.setStep(RequestStep.INSTRUCTION);
         request.setCreationDate(new Date());
         request.setOrangeAlert(Boolean.FALSE);
         request.setRedAlert(Boolean.FALSE);
 
     }
     
     protected Long finalizeAndPersist(Request request, HomeFolder homeFolder) 
         throws CvqException {
 
         setAdministrativeInformation(request);
 
         if (isOfRegistrationKind()) {
             RequestType requestType = getRequestTypeByLabel(getLabel());
             Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
             if (openSeasons != null && !openSeasons.isEmpty())  
                 request.setSeasonUuid(openSeasons.iterator().next().getUuid());
         }
         
         Long requestId = (requestDAO.saveOrUpdate(request)).getId();
 
         if (homeFolder != null) {
             homeFolder.setBoundToRequest(Boolean.valueOf(true));
             homeFolder.setOriginRequestId(requestId);
             homeFolderService.modify(homeFolder);
         }
         
         if(!request.getDraft() || request.getDraft() == null) {
             // TODO DECOUPLING
             logger.debug("create() Gonna generate a pdf of the request");
             byte[] pdfData =
                 certificateService.generateRequestCertificate(request, this.fopConfig);
             addActionTrace(CREATION_ACTION, null, new Date(), RequestState.PENDING, request, pdfData);
     
             // TODO DECOUPLING
             notifyRequestCreation(request, pdfData);
         }
 
         return requestId;
     }
     
     /**
      * Finalize the setting of request properties (creation date, state, ...) and persist it in BD.
      */
     protected Long finalizeAndPersist(final Request request)
         throws CvqException {
         return finalizeAndPersist(request, null);
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
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Node getRequestClone(final Long subjectId, Long homeFolderId, final String requestLabel)
         throws CvqException {
 
     	if (requestLabel == null)
     		throw new CvqModelException("request.error.labelRequired");
     	if (subjectId == null && homeFolderId == null)
     		throw new CvqModelException("request.error.subjectOrHomeFolderRequired");
 
     	RequestState[] excludedStates =
             requestWorkflowService.getStatesExcludedForRequestsCloning();
         List<Request> requests = null;
         if (subjectId != null)
         	requests = requestDAO.listBySubjectAndLabel(subjectId, requestLabel, excludedStates);
         else if (homeFolderId != null)
         	requests = requestDAO.listByHomeFolderAndLabel(homeFolderId, requestLabel,
         			excludedStates);
         Request request = null;
         if (requests == null || requests.isEmpty()) {
             IRequestService tempRequestService =
                 requestServiceRegistry.getRequestService(requestLabel);
             request = tempRequestService.getSkeletonRequest();
             if (subjectId != null
             		&& tempRequestService.getSubjectPolicy() != SUBJECT_POLICY_NONE) {
             	checkSubjectPolicy(subjectId, homeFolderId, tempRequestService.getSubjectPolicy());
             	request.setSubjectId(subjectId);
             }
         } else {
             // choose the most recent version of this request 
             request = requests.get(0);
             if (requests.size() > 1)
         	    for (Request requestCloned : requests)
                     if (request.getCreationDate().compareTo(requestCloned.getCreationDate()) < 0)
                         request = requestCloned;
         }
 
         Class[] parameterTypes = null;
         Object[] arguments = null;
         try {
             Method modelToXmlMethod = request.getClass().getMethod("modelToXml", parameterTypes);
             XmlObject xmlRequest = (XmlObject) modelToXmlMethod.invoke(request, arguments);
 
             Method copyMethod = xmlRequest.getClass().getMethod("copy", parameterTypes);
             XmlObject xmlRequestCopy = (XmlObject) copyMethod.invoke(xmlRequest, arguments);
 
             String xmlRequestCopyClass = xmlRequestCopy.getClass().getSimpleName();
             String getBodyMethod = "get" + xmlRequestCopyClass.replace("DocumentImpl", "");
 
             Method xmlRequestGetBody =
                 xmlRequestCopy.getClass().getMethod(getBodyMethod, parameterTypes);
             fr.cg95.cvq.xml.common.RequestType xmlRequestType =
                 (fr.cg95.cvq.xml.common.RequestType) xmlRequestGetBody.invoke(xmlRequestCopy, arguments);
             
             if (request.getSubjectId() != null) {
                 SubjectType subject = xmlRequestType.addNewSubject();
                 Individual requestSubject = individualService.getById(request.getSubjectId());
                 subject.setIndividual(Individual.modelToXml(requestSubject));
             }
 
             purgeClonedRequest(xmlRequestType);
 
             return xmlRequestCopy.getDomNode();
         } catch (SecurityException e) {
         	logger.error("getRequestClone() Security exception while cloning request");
         	throw new CvqException("Security exception while cloning request");
         } catch (IllegalAccessException e) {
         	logger.error("getRequestClone() Illegal access exception while cloning request");
         	throw new CvqException("Illegal access exception while cloning request");
         } catch (InvocationTargetException e) {
         	logger.error("getRequestClone() Invocation target exception while cloning request");
         	throw new CvqException("Invocation target exception while cloning request");
         } catch (NoSuchMethodException e) {
             // hey, you know what ? I know how my methods are named :-)
         }
 
         return null;
     }
 
     protected void purgeClonedRequest(fr.cg95.cvq.xml.common.RequestType requestType) {
 
     	// administrative data
         requestType.setId(0);
         requestType.setCreationDate(null);
         requestType.setDataState(null);
         requestType.setLastInterveningAgentId(0);
         requestType.setLastModificationDate(null);
         requestType.setObservations(null);
         requestType.setState(null);
         requestType.setStep(null);
         requestType.setValidationDate(null);
 
         // business data
         requestType.setRequester(null);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void modify(Request request)
         throws CvqException {
 
         if (request != null) {
             updateLastModificationInformation(request, null);
         }
     }
 
     protected void delete(final Request request)
         throws CvqException, CvqObjectNotFoundException {
 
         requestDAO.delete(request);
         homeFolderService.onRequestDeleted(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void delete(final Long id)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         delete(request);
     }
 
     //////////////////////////////////////////////////////////
     // Request Workflow related methods
     //////////////////////////////////////////////////////////
 
 
     // Request data state treatment
     /////////////////////////////////////////////////////////
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void updateRequestDataState(final Long id, final DataState rs)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         if (rs.equals(DataState.VALID))
             validData(id);
         else if (rs.equals(DataState.INVALID))
             invalidData(id);
     }
 
     private void validData(final Long id)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         Request request = getById(id);
         requestWorkflowService.validData(request);
     }
 
     private void invalidData(final Long id)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         Request request = getById(id);
         requestWorkflowService.invalidData(request);
     }
 
 
     // Request state treatment
     // TODO : make workflow method private - migrate unit tests
     /////////////////////////////////////////////////////////
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void updateRequestState(final Long id, final RequestState rs, final String motive)
             throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
         if (rs.equals(RequestState.COMPLETE))
             complete(id);
         else if (rs.equals(RequestState.UNCOMPLETE))
             specify(id, motive);
         else if (rs.equals(RequestState.REJECTED))
             reject(id, motive);
         else if (rs.equals(RequestState.CANCELLED))
             cancel(id);
         else if (rs.equals(RequestState.VALIDATED))
             validate(id);
         else if (rs.equals(RequestState.NOTIFIED))
             notify(id, motive);
         else if (rs.equals(RequestState.ACTIVE))
             activate(id);
         else if (rs.equals(RequestState.EXPIRED))
             expire(id);
         else if (rs.equals(RequestState.CLOSED))
             close(id);
         else if (rs.equals(RequestState.ARCHIVED))
             archive(id);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void complete(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         complete(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void complete(Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.complete(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void specify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         specify(request, motive);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void specify(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.specify(request, motive);
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
     protected void validate(final Request request, final boolean generateCertificate)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.validate(request, generateCertificate, this.fopConfig);
 
         validateAssociatedDocuments(getAssociatedDocuments(request.getId()));
 
         // those two request types are special ones
         if (request instanceof VoCardRequest || request instanceof HomeFolderModificationRequest)
             homeFolderService.validate(request.getHomeFolderId());
         else
             homeFolderService.onRequestValidated(request.getHomeFolderId(), request.getId());
 
 		// send request data to interested external services
         // TODO DECOUPLING
 		externalService.sendRequest(request);
 
 		LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         String requestTypeLabel = request.getRequestType().getLabel();
 
         // send notification to ecitizen if enabled
         // TODO DECOUPLING
         Adult requester = (Adult) individualService.getById(request.getRequesterId());
         if (lacb.hasEcitizenValidationNotification(requestTypeLabel)
                 && (requester.getEmail() != null && !requester.getEmail().equals(""))) {
             String mailData = lacb.getEcitizenValidationNotificationData(requestTypeLabel,
                     "mailData");
             Boolean attachPdf =
                 Boolean.valueOf(lacb.getEcitizenValidationNotificationData(requestTypeLabel,
                         "attachPdf"));
             String mailDataBody =
                 localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
                         ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE, mailData, false);
 
             if (mailDataBody == null) {
                 logger.warn("validate() local authority has activated ecitizen notification for request type "
                         + requestTypeLabel + " but has no mail data for it !");
                 return;
             }
 
             byte[] pdfData = null;
             if (attachPdf.booleanValue()) {
                 pdfData = getCertificate(request.getId(), RequestState.VALIDATED);
                 if (pdfData == null)
                     pdfData = certificateService.generateRequestCertificate(request, this.fopConfig);
             }
 
             StringBuffer mailSubject = new StringBuffer();
             mailSubject.append("[").append(lacb.getDisplayTitle()).append("] ")
                 .append(localizationService.getRequestLabelTranslation(request.getClass().getName(), "fr", false))
                 .append(" validée");
 
             if (attachPdf.booleanValue()) {
                 mailService.send(null, requester.getEmail(), null,
                         mailSubject.toString(), mailDataBody, pdfData, "Attestation_Demande.pdf");
             } else {
                 mailService.send(null, requester.getEmail(), null,
                         mailSubject.toString(), mailDataBody);
             }
         }
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void validate(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         validate(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void validate(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         validate(request, true);
         validateXmlData(request.modelToXml());
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void notify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         notify(request, motive);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void notify(Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.notify(request, motive);
 
         // automatically switch to ACTIVE state if request type is of registration kind
         // and has activated the mechanism.
         // this is to avoid agents one more manual state's change.
         RequestType requestType = request.getRequestType();
         if (isOfRegistrationKind()
                 && requestType.getHasAutomaticActivation()
                 && requestType.getSeasons() == null)
             requestWorkflowService.activate(request);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void activate(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         activate(request);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void activate(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.activate(request);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void expire(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         expire(request);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void expire(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.expire(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void cancel(final Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.cancel(request);
 
         // those two request types are special ones
         if (request instanceof VoCardRequest || request instanceof HomeFolderModificationRequest)
             homeFolderService.invalidate(request.getHomeFolderId());
         else
             homeFolderService.onRequestCancelled(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void cancel(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         cancel(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void reject(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.reject(request, motive);
 
         // those two request types are special ones
         if (request instanceof VoCardRequest || request instanceof HomeFolderModificationRequest)
             homeFolderService.invalidate(request.getHomeFolderId());
         else
             homeFolderService.onRequestRejected(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void reject(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         reject(request,motive);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void close(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         close(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void close(Request request)
         throws CvqException, CvqInvalidTransitionException {
 
         requestWorkflowService.close(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void archive(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         Request request = getById(id);
         archive(request);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void archive(final Request request)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         requestWorkflowService.archive(request);
 
         homeFolderService.onRequestArchived(request.getHomeFolderId(), request.getId());
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void archiveHomeFolderRequests(Long homeFolderId)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException {
 
         List<Request> requests = getByHomeFolderId(homeFolderId);
         if (requests == null || requests.isEmpty()) {
             logger.debug("archiveHomeFolderRequests() no requests associated to home folder "
                     + homeFolderId);
             return;
         }
 
         for (Request request : requests) {
             request.setState(RequestState.ARCHIVED);
             Date date = new Date();
             updateLastModificationInformation(request, date);
             addActionTrace(STATE_CHANGE_ACTION, null, date, RequestState.ARCHIVED, request, null);
         }
     }
 
     public RequestState[] getPossibleTransitions(RequestState rs) {
 
         return requestWorkflowService.getPossibleTransitions(rs);
     }
 
     public DataState[] getPossibleTransitions(DataState ds) {
         List<DataState> dataStateList = new ArrayList<DataState>();
 
         if (ds.equals(DataState.PENDING)) {
             dataStateList.add(DataState.VALID);
             dataStateList.add(DataState.INVALID);
         }
         return (DataState[]) dataStateList.toArray(new DataState[0]);
     }
 
     public Set<RequestState> getStatesBefore(RequestState rs) {
 
         return requestWorkflowService.getStatesBefore(rs);
     }
 
     public List<RequestState> getEditableStates() {
         List<RequestState> result = new ArrayList<RequestState>();
         result.add(RequestState.PENDING);
         return result;
     }
     
     protected void addActionTrace(final String label, final String note, final Date date,
             final RequestState resultingState, final Request request, final byte[] pdfData)
         throws CvqException {
 
         // retrieve user or agent id according to context
         Long userId = SecurityContext.getCurrentUserId();
         if (userId == null && request instanceof VoCardRequest) {
             VoCardRequest vocr = (VoCardRequest) request;
             // there can't be a logged in user at VO card request creation time
             userId = vocr.getRequesterId();
         } 
 
         RequestAction requestAction = new RequestAction();
         requestAction.setAgentId(userId);
         requestAction.setLabel(label);
         requestAction.setNote(note);
         requestAction.setDate(date);
         requestAction.setResultingState(resultingState);
         requestAction.setFile(pdfData);
 
         if (request.getActions() == null) {
             Set<RequestAction> actionsSet = new HashSet<RequestAction>();
             actionsSet.add(requestAction);
             request.setActions(actionsSet);
         } else {
             request.getActions().add(requestAction);
         }
         
         requestDAO.update(request);
     }
 
     protected void addCertificateToActionTrace(final Request request,
             final RequestState requestState, byte[] pdfData)
         throws CvqException {
 
         RequestAction requestAction =
             requestActionDAO.findByRequestIdAndResultingState(request.getId(), requestState);
         requestAction.setFile(pdfData);
         requestActionDAO.update(requestAction);
     }
 
 
     //////////////////////////////////////////////////////////
     // RequestForm related Methods
     //////////////////////////////////////////////////////////
 
     /**
      * Generate requestForm asset resource name
      */
     private String generateAssetRessourceName (String requestTypeLabel,
             String requestFormLabel, String filename) {
         String generatedName =  requestTypeLabel
             + "_" + RequestFormType.REQUEST_MAIL_TEMPLATE.toString()
             + "_" + requestFormLabel;
             generatedName = generatedName.trim().replace(' ', '_');
 
         // get file extension
         if (filename != null) {
             String[] splitFilename = filename.split("\\.");
             if (splitFilename.length > 1)
                 generatedName = generatedName + "."  + splitFilename[splitFilename.length - 1];
         } else
             generatedName = generatedName + ".xsl-fo";
 
         return generatedName;
     }
 
     /**
      * Check requestForm's labels uniqueness for given RequesType and RequestFormType
      */
     private void checkRequestFormLabelUniqueness(String label, String shortLabel,
             RequestFormType requestFormType, Long requestTypeId,
             Long requestFormId) throws CvqModelException {
         List<RequestForm> requestFormList =
             requestFormDAO.findByTypeAndRequestTypeId(requestFormType, requestTypeId);
         for (RequestForm requestForm : requestFormList) {
             if (!requestForm.getId().equals(requestFormId)) {
                 if (requestForm.getLabel().equals(label))
                     throw new CvqModelException("label already used","requestForm.message.labelAlreadyUsed");
                 if (requestForm.getShortLabel().equals(shortLabel))
                     throw new CvqModelException("short label already used","requestForm.message.shortLabelAlreadyUsed");
             }
         }
     }
 
     public List<File> getMailTemplates(String pattern) throws CvqException {
         if(pattern == null) pattern="*";
         return this.localAuthorityRegistry.getLocalResourceContent(
             ILocalAuthorityRegistry.MAIL_TEMPLATES_TYPE,
             pattern);
     }
 
     public File getTemplateByName(String name) {
         return this.localAuthorityRegistry.getCurrentLocalAuthorityResource(
             ILocalAuthorityRegistry.MAIL_TEMPLATES_TYPE, name, false);
     }
 
     public Long modifyRequestTypeForm(Long requestTypeId, RequestForm requestForm)
         throws CvqException {
         Long result = -1L;
 
         if (requestForm.getType() == null) 
             requestForm.setType(RequestFormType.REQUEST_MAIL_TEMPLATE);
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         if (requestType == null)
             throw new CvqModelException("request type is invalid","requestForm.message.requestTypeIsInvalid");
 
         checkRequestFormLabelUniqueness(requestForm.getLabel(), requestForm.getShortLabel(),
                 requestForm.getType(), requestTypeId,
                 requestForm.getId() == null ? new Long(-1) : requestForm.getId());
 
         if (requestForm.getLabel() == null && requestForm.getLabel().trim() == "")
             throw new CvqModelException("label is null","requestForm.message.labelIsNull");
         if (requestForm.getShortLabel() == null && requestForm.getShortLabel().trim() == "")
             throw new CvqModelException("short label is null","requestForm.message.shortLabelIsNull");
 
         if (this.requestTypeContainsForm(requestType, requestForm)) {
             result = requestForm.getId();
             requestDAO.update(requestForm);
         }else {
             Set<RequestType> requestTypesSet = new HashSet<RequestType>();
             requestTypesSet.add(requestType);
             requestForm.setRequestTypes(requestTypesSet);
             requestType.getForms().add(requestForm);
             result = requestFormDAO.create(requestForm);
         }
 
         return result;
     }
 
     protected boolean requestTypeContainsForm(RequestType type, RequestForm form) {
         for(RequestForm f : (Set<RequestForm>)type.getForms()) {
             if(f.getId().equals(form.getId())) return true;
         }
 
         return false;
     }
 
     public void removeRequestTypeForm(final Long requestTypeId, final Long requestFormId)
         throws CvqException {
         RequestType requestType = getRequestTypeById(requestTypeId);
         RequestForm requestForm = 
             (RequestForm) genericDAO.findById(RequestForm.class, requestFormId);
         requestType.getForms().remove(requestForm);
 
         requestFormDAO.delete(requestForm);
     }
 
     public void removeRequestTypeForm(final Long requestFormId)
         throws CvqException {
         RequestForm requestForm =
             (RequestForm) genericDAO.findById(RequestForm.class, requestFormId);
 
         for(RequestType t : (Set<RequestType>)requestForm.getRequestTypes())
             t.getForms().remove(requestForm);
 
         requestFormDAO.delete(requestForm);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.READ)
     public List<RequestForm> getRequestTypeForms(Long requestTypeId,
             RequestFormType requestFormType) throws CvqException {
 
         List<RequestForm> result =
             requestFormDAO.findByTypeAndRequestTypeId(requestFormType, requestTypeId);
         return result;
 
     }
 
     public RequestForm getRequestFormById(Long id) throws CvqException {
         return (RequestForm)requestFormDAO.findById(RequestForm.class, id);
     }
 
     public void onPaymentValidated(Request request, String paymentReference)
         throws CvqException {
     }
 
     public void onPaymentRefused(Request request)
         throws CvqException {
     }
 
     public void onPaymentCancelled(Request request)
         throws CvqException {
     }
 
     public void onExternalServiceSendRequest(Request request, String sendRequestResult) throws CvqException {
     }
 
     public String getLocalReferentialFilename() {
         return this.localReferentialFilename;
     }
 
     public void setLocalReferentialFilename(final String filename) {
         this.localReferentialFilename = filename;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setRequestActionDAO(IRequestActionDAO requestActionDAO) {
         this.requestActionDAO = requestActionDAO;
     }
 
     public void setRequestNoteDAO(IRequestNoteDAO requestNoteDAO) {
         this.requestNoteDAO = requestNoteDAO;
     }
 
     public void setRequestFormDAO(IRequestFormDAO requestFormDAO) {
         this.requestFormDAO = requestFormDAO;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setDocumentTypeService(IDocumentTypeService documentTypeService) {
         this.documentTypeService = documentTypeService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setCertificateService(ICertificateService certificateService) {
         this.certificateService = certificateService;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setFopConfig(String fopConfig) {
         this.fopConfig = fopConfig;
     }
 
     public String getLabel() {
         if (label != null)
             return this.label;
         else
             return "";
     }
 
     public void setLabel(final String label) {
         this.label = label;
     }
 
     public String getXslFoFilename() {
         return this.xslFoFilename;
     }
 
     public void setXslFoFilename(String xslFoFilename) {
         this.xslFoFilename = xslFoFilename;
     }
 
     public void setSupportUnregisteredCreation(String supportUnregisteredCreation) {
         this.supportUnregisteredCreation = Boolean.valueOf(supportUnregisteredCreation);
     }
 
     public boolean supportUnregisteredCreation() {
         return supportUnregisteredCreation == null ? false : supportUnregisteredCreation;
     }
 
     public void setPlaceReservationFilename(String placeReservationFilename) {
         this.placeReservationFilename = placeReservationFilename;
     }
 
     public String getPlaceReservationFilename() {
         return placeReservationFilename;
     }
 
     public void setExternalReferentialFilename(String externalReferentialFilename) {
         this.externalReferentialFilename = externalReferentialFilename;
     }
 
     public String getExternalReferentialFilename() {
         return externalReferentialFilename;
     }
 
     public String getSubjectPolicy() {
         return subjectPolicy;
     }
 
     public void setSubjectPolicy(final String subjectPolicy) {
         this.subjectPolicy = subjectPolicy;
     }
 
     public boolean isOfRegistrationKind() {
         return isOfRegistrationKind == null ? false : isOfRegistrationKind;
     }
 
     public void setIsOfRegistrationKind(String isOfRegistrationKind) {
         this.isOfRegistrationKind = Boolean.valueOf(isOfRegistrationKind);
     }
 
 	public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
 		this.localAuthorityRegistry = localAuthorityRegistry;
 	}
 
 	public void setMailService(IMailService mailService) {
 		this.mailService = mailService;
 	}
 
 	public void setLocalizationService(ILocalizationService localizationService) {
 		this.localizationService = localizationService;
 	}
 
     public void setRequestWorkflowService(RequestWorkflowService requestWorkflowService) {
         this.requestWorkflowService = requestWorkflowService;
     }
 
     public void setCategoryService(ICategoryService categoryService) {
         this.categoryService = categoryService;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }
     
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
     
     public boolean isConditionFilled (Map<String, String> triggers) {
         return true;
     }
     
 }
