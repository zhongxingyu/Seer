 package fr.cg95.cvq.service.request;
 
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
 import net.sf.oval.context.OValContext;
 import net.sf.oval.exception.OValException;
import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestData;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.exception.CvqException;
 
 public class SubjectIdCheck extends AbstractAnnotationCheck<LocalReferential> {
 
     private static final long serialVersionUID = 1L;
 
    private static IRequestSearchService requestSearchService;

     private static IRequestServiceRegistry requestServiceRegistry;
 
     private static IRequestWorkflowService requestWorkflowService;
 
     @Override
     public boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context,
         Validator validator) throws OValException {
         RequestData requestData = (RequestData)validatedObject;
         if (requestData.getId() != null) {
            Request requestFromDb;
            try {
                requestFromDb = requestSearchService.getById(requestData.getId(), false);
                if (requestFromDb.getSubjectId() != null) {
                    if (requestFromDb.getSubjectId().equals(valueToValidate)) {
                        return true;
                    } else if (!RequestState.DRAFT.equals(requestData.getState())) {
                        return false;
                    }
                 }
            } catch (CvqException e) {
                return false;
             }
         }
         try {
             requestWorkflowService.checkSubjectPolicy(
                 (Long)valueToValidate,
                 requestData.getHomeFolderId(),
                 requestServiceRegistry.getRequestService(requestData.getRequestType().getLabel())
                     .getSubjectPolicy(),
                 requestData.getRequestType()
             );
             return true;
         } catch (CvqException e) {
             return false;
         }
     }
 
    public static void setRequestSearchService(IRequestSearchService requestSearchService) {
        SubjectIdCheck.requestSearchService = requestSearchService;
    }

     public static void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         SubjectIdCheck.requestServiceRegistry = requestServiceRegistry;
     }
 
     public static void setRequestWorkflowService(IRequestWorkflowService requestWorkflowService) {
         SubjectIdCheck.requestWorkflowService = requestWorkflowService;
     }
 }
