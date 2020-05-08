 package fr.cg95.cvq.service.request.school.impl;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.school.LearningActivitiesDiscoveryRegistrationRequest;
 import fr.cg95.cvq.service.request.impl.RequestService;
 
 /**
  *
  * @author vsi@zenexity.fr
  */
 public final class LearningActivitiesDiscoveryRegistrationRequestService extends RequestService {
 
     @Override
     public boolean accept(final Request request) {
         return request instanceof LearningActivitiesDiscoveryRegistrationRequest;
     }
 
     @Override
     public Request getSkeletonRequest() {
         return new LearningActivitiesDiscoveryRegistrationRequest();
     }
 }
