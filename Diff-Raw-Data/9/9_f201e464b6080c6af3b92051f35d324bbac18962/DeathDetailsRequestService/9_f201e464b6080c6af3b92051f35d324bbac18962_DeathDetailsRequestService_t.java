 package fr.cg95.cvq.service.request.civil.impl;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.civil.DeathDetailsRequest;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.civil.IDeathDetailsRequestService;
 import fr.cg95.cvq.service.request.impl.RequestService;
 
 /**
  * Implementation of the {@link IDeathDetailsRequestService death details request service}.
  * 
  * @author bor@zenexity.fr
  */
 public final class DeathDetailsRequestService extends RequestService 
     implements IDeathDetailsRequestService {
     
     @Override
 	public boolean accept(Request request) {
 		return request instanceof DeathDetailsRequest;
 	}
 
     @Override
 	public Request getSkeletonRequest() throws CvqException {
         DeathDetailsRequest request = new DeathDetailsRequest();
         //FIXME see Birth
         if (SecurityContext.getCurrentSite() != null) {
             request.setDeathCity(SecurityContext.getCurrentSite().getDisplayTitle());
            request.setDeathPostalCode(SecurityContext.getCurrentSite().getPostalCode().substring(0,2));
         }
         return request;
     }
 }
