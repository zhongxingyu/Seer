 package fr.cg95.cvq.service.request.school.impl;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.school.StudyGrantRequest;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.service.request.condition.EqualityChecker;
 import fr.cg95.cvq.service.request.impl.RequestService;
 import fr.cg95.cvq.service.request.school.IStudyGrantRequestService;
 
 /**
  * @author Jean-Sébastien Bour (jsb@zenexity.fr)
  *
  */
 public class StudyGrantRequestService extends RequestService implements IStudyGrantRequestService {
     
     public boolean accept(Request request) {
         return request instanceof StudyGrantRequest;
     }
 
     public Request getSkeletonRequest() throws CvqException {
         return new StudyGrantRequest();
     }
 
     @Override
     public void onRequestValidated(Request request) throws CvqException {
         StudyGrantRequest sgr = (StudyGrantRequest) request;
         Individual subject = individualService.getById(sgr.getSubjectId());
         subject.setBirthDate(sgr.getSubjectBirthDate());
         if (subject instanceof Adult) {
             Adult adult = (Adult) subject;
             if (sgr.getSubjectMobilePhone() != null 
                     && (adult.getMobilePhone() == null || adult.getMobilePhone().isEmpty()))
                 adult.setMobilePhone(sgr.getSubjectMobilePhone());
             if (sgr.getSubjectPhone() != null 
                     && (adult.getHomePhone() == null || adult.getHomePhone().isEmpty()))
                 adult.setHomePhone(sgr.getSubjectPhone());
             adult.setEmail(sgr.getSubjectEmail());
         }
         individualService.modify(subject);
     }
     
     @Override
     protected void initFilledConditions() {
         super.initFilledConditions();
         filledConditions.put("abroadInternship", new EqualityChecker("true"));
        filledConditions.put("currentStudiesDiploma", new EqualityChecker("otherStudies"));
         filledConditions.put("taxHouseholdCity", new EqualityChecker("573"));
         filledConditions.put("currentSchoolName", new EqualityChecker("autre"));
         filledConditions.put("isSubjectAccountHolder", new EqualityChecker("true"));
     }
 
     public void setEdemandeId(Long requestId, String edemandeId)
         throws CvqException {
         StudyGrantRequest request = (StudyGrantRequest)getById(requestId);
         request.setEdemandeId(edemandeId);
         modify(request);
     }
 
     public void setAccountHolderEdemandeId(@IsRequest final Long requestId, final String accountHolderEdemandeId)
         throws CvqException {
         StudyGrantRequest request = (StudyGrantRequest)getById(requestId);
         request.setAccountHolderEdemandeId(accountHolderEdemandeId);
         modify(request);
     }
 }
