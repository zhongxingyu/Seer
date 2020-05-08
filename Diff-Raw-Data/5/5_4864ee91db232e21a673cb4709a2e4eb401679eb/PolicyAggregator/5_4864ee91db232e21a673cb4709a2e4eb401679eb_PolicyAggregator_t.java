 package org.wyona.security.impl.util;
 
 import org.wyona.security.core.AuthorizationException;
 import org.wyona.security.core.UsecasePolicy;
 import org.wyona.security.core.api.Policy;
 import org.wyona.security.core.api.PolicyManager;
 /*
 import org.wyona.security.core.GroupPolicy;
 import org.wyona.security.core.api.Identity;
 */
 
 import org.wyona.commons.io.PathUtil;
 
 import org.apache.log4j.Logger;
 
 /**
  * Utility class to aggregate policies based on their parent policies
  */
 public class PolicyAggregator {
 
     private static Logger log = Logger.getLogger(PolicyAggregator.class);
 
     /**
      *
      */
     public static Policy aggregatePolicy(Policy policy) throws AuthorizationException {
         return policy;
     }
 
     /**
      *
      */
     public static Policy aggregatePolicy(String path, PolicyManager pm) throws AuthorizationException {
         Policy policy = pm.getPolicy(path, false);
         if (policy == null) {
             if (!path.equals("/")) {
                 return aggregatePolicy(PathUtil.getParent(path), pm);
             } else {
                 log.warn("No policies found at all, not even a root policy!");
                 return null;
             }
         } else {
             if (!policy.useInheritedPolicies()) {
                 return policy;
             } else {
                 if (!path.equals("/")) {
                     Policy parentPolicy = aggregatePolicy(PathUtil.getParent(path), pm);
 
                     UsecasePolicy[] usecasePolicies = policy.getUsecasePolicies();
                     UsecasePolicy[] parentUsecasePolicies = parentPolicy.getUsecasePolicies();
                     for (int i = 0; i < parentUsecasePolicies.length; i++) {
                         boolean usecaseAlreadyExists = false;
                         for (int k = 0; k < usecasePolicies.length; k++) {
                             if (parentUsecasePolicies[i].getName().equals(usecasePolicies[k].getName())) {
                                 usecaseAlreadyExists = true;
                                usecasePolicies[k].merge(parentUsecasePolicies[i]);
                                 break;
                             }
                         }
                         if(!usecaseAlreadyExists) {
                             try {
                                 policy.addUsecasePolicy(parentUsecasePolicies[i]);
                             } catch(Exception e) {
                                 log.error(e, e);
                                 throw new AuthorizationException(e.getMessage());
 			    }
                         }
                     }
 
                     return policy;
                 } else {
                     return policy;
                 }
             }
         }
     }
 }
