 package de.escidoc.core.aa.filter;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import de.escidoc.core.aa.business.interfaces.UserAccountHandlerInterface;
 import de.escidoc.core.aa.business.interfaces.UserGroupHandlerInterface;
 import de.escidoc.core.aa.business.persistence.RoleGrant;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.resources.DbResourceCache;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.UserContext;
 
 /**
  * Encapsulate the additional work which has to be done to get the permission
  * filter queries mainly for Lucene filtering.
  * 
  * @spring.bean id="filter.PermissionsQuery"
  * @author Andr&eacute; Schenk
  */
 public class PermissionsQuery extends DbResourceCache {
     /**
      * Resource id which will never exist in the repository.
      */
     private static final String INVALID_ID = "escidoc:-1";
 
     /**
      * Logging goes there.
      */
     private static final AppLogger LOG =
         new AppLogger(PermissionsQuery.class.getName());
 
     private TripleStoreUtility tripleStoreUtility = null;
 
     private UserAccountHandlerInterface userAccountHandler = null;
 
     private UserGroupHandlerInterface userGroupHandler = null;
 
     /**
      * Create a new LucenePermissions object.
      * 
      * @throws IOException
      *             not thrown here
      */
     public PermissionsQuery() throws IOException {
     }
 
     /**
      * This method is not used here.
      * 
      * @param id
      *            resource id
      */
     @Override
     protected void deleteResource(final String id) {
     }
 
     /**
      * Get the list of all child containers for all containers the user is
      * granted to.
      * 
      * @param userGrants
      *            user grants of the user
      * @param userGroupGrants
      *            user group grants of the user
      * 
      * @return list of all child containers
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     protected Set<String> getHierarchicalContainers(
         final Set<String> userGrants, final Set<String> userGroupGrants)
         throws WebserverSystemException {
         Set<String> result = new HashSet<String>();
 
         try {
             for (String grant : userGrants) {
                 List<String> childContainers =
                     tripleStoreUtility.getAllChildContainers(grant);
 
                if ((childContainers != null) && (childContainers.size() > 0)) {
                    result.add(grant);
                     result.addAll(childContainers);
                 }
             }
             for (String grant : userGroupGrants) {
                 List<String> childContainers =
                     tripleStoreUtility.getAllChildContainers(grant);
 
                if ((childContainers != null) && (childContainers.size() > 0)) {
                    result.add(grant);
                     result.addAll(childContainers);
                 }
             }
         }
         catch (TripleStoreSystemException e) {
             LOG.error("getting child containers from database failed", e);
         }
 
         // ensure the list is not empty
         if (result.size() == 0) {
             result.add(INVALID_ID);
         }
 
         return result;
     }
 
     /**
      * Get the resource with the given id and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource id list will be written
      * @param id
      *            resource id
      * @param userId
      *            user id
      * 
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     public void getResource(
         final Writer output, final String id, final String userId)
         throws WebserverSystemException {
         Set<String> userGrants = getUserGrants(userId);
         Set<String> userGroupGrants = getUserGroupGrants(userId);
 
         getResource(output, (UserContext.isRestAccess() ? "rest" : "soap")
             + "_content", id, userId, retrieveGroupsForUser(userId),
             userGrants, userGroupGrants, getHierarchicalContainers(userGrants,
                 userGroupGrants));
     }
 
     /**
      * Get all grants directly assigned to the given user.
      * 
      * @param userId
      *            user id
      * 
      * @return all direct grants for the user
      */
     protected Set<String> getUserGrants(final String userId) {
         Set<String> result = new HashSet<String>();
 
         if ((userId != null) && (userId.length() > 0)) {
             try {
                 Map<String, Map<String, List<RoleGrant>>> currentGrants =
                     userAccountHandler.retrieveCurrentGrantsAsMap(userId);
 
                 if (currentGrants != null) {
                     for (String role : currentGrants.keySet()) {
                         for (String objectId : currentGrants.get(role).keySet()) {
                             result.add(objectId);
                         }
                     }
                 }
             }
             catch (Exception e) {
                 LOG.error("getting the user grants from AA failed", e);
             }
         }
 
         // ensure the list is not empty
         if (result.size() == 0) {
             result.add(INVALID_ID);
         }
 
         return result;
     }
 
     /**
      * Get all group grants assigned to the given user.
      * 
      * @param userId
      *            user id
      * 
      * @return all group grants for the user
      */
     protected Set<String> getUserGroupGrants(final String userId) {
         Set<String> result = new HashSet<String>();
 
         if ((userId != null) && (userId.length() > 0)) {
             try {
                 Set<String> groupIds =
                     userGroupHandler.retrieveGroupsForUser(userId);
 
                 if (groupIds != null) {
                     for (String groupId : groupIds) {
                         Map<String, Map<String, List<RoleGrant>>> currentGrants =
                             userGroupHandler
                                 .retrieveCurrentGrantsAsMap(groupId);
 
                         if (currentGrants != null) {
                             for (String role : currentGrants.keySet()) {
                                 for (String objectId : currentGrants
                                     .get(role).keySet()) {
                                     result.add(objectId);
                                 }
                             }
                         }
                     }
                 }
             }
             catch (Exception e) {
                 LOG.error("getting the user group grants from AA failed", e);
             }
         }
 
         // ensure the list is not empty
         if (result.size() == 0) {
             result.add(INVALID_ID);
         }
 
         return result;
     }
 
     /**
      * Injects the data source.
      * 
      * @spring.property ref="escidoc-core.DataSource"
      * @param myDataSource
      *            data source from Spring
      */
     public void setMyDataSource(final DataSource myDataSource) {
         super.setDataSource(myDataSource);
     }
 
     /**
      * Injects the TripleStore utility.
      * 
      * @spring.property ref="business.TripleStoreUtility"
      * @param tripleStoreUtility
      *            TripleStoreUtility from Spring
      */
     public void setTripleStoreUtility(
         final TripleStoreUtility tripleStoreUtility) {
         this.tripleStoreUtility = tripleStoreUtility;
     }
 
     /**
      * Injects the user account handler.
      * 
      * @spring.property ref="business.UserAccountHandler"
      * @param userAccountHandler
      *            user account handler from Spring
      */
     public void setUserAccountHandler(
         final UserAccountHandlerInterface userAccountHandler) {
         this.userAccountHandler = userAccountHandler;
     }
 
     /**
      * Injects the user group handler.
      * 
      * @spring.property ref="business.UserGroupHandler"
      * @param userGroupHandler
      *            user group handler from Spring
      */
     public void setUserGroupHandler(
         final UserGroupHandlerInterface userGroupHandler) {
         this.userGroupHandler = userGroupHandler;
     }
 
     /**
      * This method is not used for Lucene filtering.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      */
     @Override
     protected void storeResource(
         final String id, final String restXml, final String soapXml) {
     }
 }
