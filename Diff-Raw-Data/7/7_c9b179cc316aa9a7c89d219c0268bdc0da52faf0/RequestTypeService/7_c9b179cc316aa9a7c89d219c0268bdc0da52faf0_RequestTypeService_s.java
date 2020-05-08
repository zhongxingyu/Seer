 package fr.cg95.cvq.service.request.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
import org.apache.log4j.Logger;

 import fr.cg95.cvq.business.authority.CategoryProfile;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.business.document.DocumentType;
import fr.cg95.cvq.business.request.DisplayGroup;
 import fr.cg95.cvq.business.request.RequestForm;
 import fr.cg95.cvq.business.request.RequestFormType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.request.Requirement;
 import fr.cg95.cvq.dao.request.IRequestFormDAO;
 import fr.cg95.cvq.dao.request.IRequestTypeDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.document.IDocumentTypeService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.IRequestTypeService;
 import fr.cg95.cvq.service.request.annotation.RequestFilter;
 import fr.cg95.cvq.util.Critere;
 
 /**
  *
  * @author bor@zenexity.fr
  */
 public class RequestTypeService implements IRequestTypeService {
 
    private static Logger logger = Logger.getLogger(RequestTypeService.class);
    
     private IDocumentTypeService documentTypeService;
     private IRequestServiceRegistry requestServiceRegistry;
 
     private IRequestTypeDAO requestTypeDAO;
     private IRequestFormDAO requestFormDAO;
 
     @Override
     public List<RequestType> getAllRequestTypes()
         throws CvqException {
 
         // ecitizens can see all activated requests types
         if (SecurityContext.isFrontOfficeContext()) {
             Set<Critere> criteriaSet = new HashSet<Critere>();
             Critere activeCriteria = new Critere();
             activeCriteria.setAttribut(RequestType.SEARCH_BY_STATE);
             activeCriteria.setValue(true);
             criteriaSet.add(activeCriteria);
             return requestTypeDAO.listByCategoryAndState(criteriaSet);
         }
 
         if (SecurityContext.isAdminContext())
             return requestTypeDAO.listAll();
 
         // if agent is admin, return all categories ...
         if (SecurityContext.getCurrentCredentialBean().hasSiteAdminRole())
             return requestTypeDAO.listAll();
 
         // else filters categories it is authorized to see
         CategoryRoles[] authorizedCategories =
             SecurityContext.getCurrentCredentialBean().getCategoryRoles();
         List<RequestType> results = new ArrayList<RequestType>();
         if (authorizedCategories == null) {
             return results;
         } else {
             for (CategoryRoles categoryRole : authorizedCategories) {
                 results.addAll(categoryRole.getCategory().getRequestTypes());
             }
         }
         
         return results;
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.READ)
     @RequestFilter(privilege=ContextPrivilege.READ)
     public List<RequestType> getRequestTypes(Set<Critere> criteriaSet)
         throws CvqException {
 
         return requestTypeDAO.listByCategoryAndState(criteriaSet);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.NONE)
     public List<RequestType> getManagedRequestTypes()
         throws CvqException {
 
         // else filters categories it is authorized to see
         CategoryRoles[] authorizedCategories =
             SecurityContext.getCurrentCredentialBean().getCategoryRoles();
         List<RequestType> results = new ArrayList<RequestType>();
         for (CategoryRoles categoryRole : authorizedCategories) {
             if (categoryRole.getProfile().equals(CategoryProfile.MANAGER))
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
         Set<Requirement> requirements = new HashSet<Requirement>(requestType.getRequirements());
         Iterator<Requirement> it = requirements.iterator();
         while(it.hasNext()){
             Requirement r = it.next();
             if (r.getDocumentType().getId().equals(documentTypeId)) {
                 it.remove();
                 foundRequirement = true;
                 break;
             }
         }
         if (foundRequirement) {
             requestType.setRequirements(requirements);
             requestTypeDAO.update(requestType);
         }
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
 
         if (requestSeason.getUuid() == null || requestSeason.getUuid().isEmpty())
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
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestSeason getRequestTypeSeason(Long requestTypeId, String uuid)
         throws CvqException {
 
         for (RequestSeason season : getRequestTypeById(requestTypeId).getSeasons()) {
             if (season.getUuid().equalsIgnoreCase(uuid)) {
                 return season;
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
 
     /**
      * Get the list of seasons whose registrations are currently open.
      *
      * @return null if no seasons defined for this request type, an empty list if no
      *  seasons with opened registrations, the list of seasons with opened registrations
      *  otherwise.
      */
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestSeason> getOpenSeasons(RequestType requestType) {
 
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
 
     @Override
     public boolean isRegistrationOpen(final Long requestTypeId) throws CvqException {
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         IRequestService service = requestServiceRegistry.getRequestService(requestType.getLabel());
         if (!service.isOfRegistrationKind())
             return true;
 
         Set<RequestSeason> openSeasons = getOpenSeasons(requestType);
         if (openSeasons == null)
             return true;
         if (openSeasons.isEmpty())
             return false;
         else
             return true;
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
                     throw new CvqModelException("label already used",
                         "requestForm.message.labelAlreadyUsed");
                 if (requestForm.getShortLabel().equals(shortLabel))
                     throw new CvqModelException("short label already used",
                         "requestForm.message.shortLabelAlreadyUsed");
             }
         }
     }
 
     public Long modifyRequestTypeForm(Long requestTypeId, RequestForm requestForm)
         throws CvqException {
         Long result = -1L;
 
         if (requestForm.getType() == null)
             requestForm.setType(RequestFormType.REQUEST_MAIL_TEMPLATE);
 
         RequestType requestType = getRequestTypeById(requestTypeId);
         if (requestType == null)
             throw new CvqModelException("request type is invalid",
                 "requestForm.message.requestTypeIsInvalid");
 
         checkRequestFormLabelUniqueness(requestForm.getLabel(), requestForm.getShortLabel(),
                 requestForm.getType(), requestTypeId,
                 requestForm.getId() == null ? new Long(-1) : requestForm.getId());
 
         if (requestForm.getLabel() == null && requestForm.getLabel().trim().isEmpty())
             throw new CvqModelException("label is null","requestForm.message.labelIsNull");
         if (requestForm.getShortLabel() == null && requestForm.getShortLabel().trim().isEmpty())
             throw new CvqModelException("short label is null","requestForm.message.shortLabelIsNull");
 
         if (this.requestTypeContainsForm(requestType, requestForm)) {
             result = requestForm.getId();
             requestFormDAO.update(requestForm);
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
         for (RequestForm f : type.getForms()) {
             if(f.getId().equals(form.getId())) return true;
         }
 
         return false;
     }
 
     public void removeRequestTypeForm(final Long requestTypeId, final Long requestFormId)
         throws CvqException {
         RequestType requestType = getRequestTypeById(requestTypeId);
         RequestForm requestForm =
             (RequestForm) requestFormDAO.findById(RequestForm.class, requestFormId);
         requestType.getForms().remove(requestForm);
 
         requestFormDAO.delete(requestForm);
     }
 
     public void removeRequestTypeForm(final Long requestFormId)
         throws CvqException {
         RequestForm requestForm =
             (RequestForm) requestFormDAO.findById(RequestForm.class, requestFormId);
 
         for(RequestType t : requestForm.getRequestTypes())
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
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setRequestFormDAO(IRequestFormDAO requestFormDAO) {
         this.requestFormDAO = requestFormDAO;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setDocumentTypeService(IDocumentTypeService documentTypeService) {
         this.documentTypeService = documentTypeService;
     }
 }
