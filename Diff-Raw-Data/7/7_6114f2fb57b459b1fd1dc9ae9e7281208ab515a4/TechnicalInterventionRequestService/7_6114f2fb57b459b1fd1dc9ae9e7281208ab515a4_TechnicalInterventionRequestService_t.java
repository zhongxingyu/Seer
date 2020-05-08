 package fr.cg95.cvq.service.request.technical.impl;
 
 import java.util.Arrays;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.technical.TechnicalInterventionRequest;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.condition.EqualityListChecker;
 import fr.cg95.cvq.service.request.impl.RequestService;
 import fr.cg95.cvq.service.request.technical.ITechnicalInterventionRequestService;
 
 public class TechnicalInterventionRequestService extends RequestService 
     implements ITechnicalInterventionRequestService {
 
     @Override
     public boolean accept(Request request) {
         return request instanceof TechnicalInterventionRequest;
     }
 
    @Override
     public Request getSkeletonRequest() throws CvqException {
         TechnicalInterventionRequest request = new TechnicalInterventionRequest();
         if (SecurityContext.getCurrentEcitizen() != null) {
             request.setInterventionPlace(SecurityContext.getCurrentEcitizen().getHomeFolder().getAdress().clone());
         }
         return request;
     }
 
    @Override
     protected void initFilledConditions() {
         super.initFilledConditions();
        filledConditions.put("interventionType", 
                new EqualityListChecker(Arrays.asList("other", "Other", "Autre", "autre")));
     }
 }
