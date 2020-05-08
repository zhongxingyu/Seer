 package fr.cg95.cvq.service.request.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.service.request.IConditionService;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.condition.EqualityChecker;
 import fr.cg95.cvq.service.request.condition.IConditionChecker;
 
 public class ConditionService implements IConditionService {
 
     private IRequestServiceRegistry requestServiceRegistry;
     
     private Map<String,IConditionChecker> commonConditions;
 
    public void init() throws CvqConfigurationException {
        commonConditions = new HashMap<String,IConditionChecker>();
        commonConditions.put("_homeFolderResponsible.activeHomeFolder", new EqualityChecker("true"));
     }
 
     @Override
     public boolean isConditionFilled(final String requestTypeLabel, Map<String, String> triggers) {
         boolean test = true;
         IRequestService requestService = requestServiceRegistry.getRequestService(requestTypeLabel);
         Map<String,IConditionChecker> requestConditions = requestService.getConditions();
         requestConditions.putAll(commonConditions);
         for (Entry<String, String> trigger : triggers.entrySet()) {
             if (requestConditions.get(trigger.getKey()) != null
                 && requestConditions.get(trigger.getKey()).test(trigger.getValue()))
                 test = test && true;
             else
                 return false;
         }
         return test;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 }
