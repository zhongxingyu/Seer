 package org.wyona.security.core.api;
 
 import org.wyona.commons.io.Path;
 import org.wyona.security.core.AuthorizationException;
 import org.wyona.yarep.core.Repository;
 
 /**
  *
  */
 public interface PolicyManager {
 
     /**
      * @deprecated
      */
     public boolean authorize(Path path, Identity identity, Role role) throws AuthorizationException;
     
     /**
      * @deprecated
      */
     public boolean authorize(String path, Identity identity, Role role) throws AuthorizationException;
     
     /**
      *
      */
     public boolean authorize(String path, Identity identity, Usecase usecase) throws AuthorizationException;
     
     /**
      *
      */
     public boolean authorize(Policy policy, Identity identity, Usecase usecase) throws AuthorizationException;
    
     /**
      * Get policies repository
      * @deprecated It's not good to reveal the actual data repository
      */
      public Repository getPoliciesRepository();
 
     /**
      * Get policy of a specific node
      * @param path Path of content, e.g. /hello/world.html
      * @param aggregate Boolean which specifies if implementation shall return an aggregated policy, e.g. an aggregation of the policies for /, /hello/ and /hello/world.html
     * @return Policy which is associated with content path
      */
     public Policy getPolicy(String path, boolean aggregate) throws AuthorizationException;
 
     /**
      * Set new or modified policy
      * @param path Path of content, e.g. /hello/world.html
      * @param policy New or modified policy
      */
     public void setPolicy(String path, Policy policy) throws AuthorizationException;
     
     /**
      * Removes the policy from the given path.
      * @param path
      * @throws AuthorizationException
      */
     public void removePolicy(String path) throws AuthorizationException;
     
     /**
      * Creates an empty policy.
      * @return empty policy
      */
     public Policy createEmptyPolicy() throws AuthorizationException;
 
     /**
      * @return All the usecases which the policy manager supports. For example this can be useful for a policy editor in order to select from a list of usecases/actions/rights.
      */
     public String[] getUsecases() throws AuthorizationException;
 
     /**
      * @return Get usecase label, for example return "Read" as the label for the usecaseId "r"
      */
     public String getUsecaseLabel(String usecaseId, String language) throws AuthorizationException;
 }
