 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AMIdentityRepository.java,v 1.4 2006-04-14 09:06:38 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 
 import com.iplanet.am.sdk.AMDirectoryManager;
 import com.iplanet.am.sdk.AMDirectoryWrapper;
 import com.iplanet.am.util.Debug;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.sm.DNMapper;
 
 /**
  * The class <code> AMIdentityRepository </code> represents an object to access
  * the repositories in which user/role/group and other identity data is
  * configured. This class provides access to methods which will search, create
  * and delete identities. An instance of this class can be obtained in the
  * following manner:
  * <p>
  * 
  * <PRE>
  * 
  * AMIdentityRepository = new AMIdentityRepository(ssoToken, orgName);
  * 
  * </PRE>
  * 
  * @supported.all.api
  */
 public final class AMIdentityRepository {
 
     private SSOToken token;
 
     private String org;
 
     public static Debug debug = Debug.getInstance("amIdm");
 
     protected static boolean logStatus = false;
 
     public static Map listeners = new CaseInsensitiveHashMap();
 
     /*
      * static { String status = SystemProperties.get(Constants.AM_LOGSTATUS); if
      * (status == null) { status = "INACTIVE"; } if
      * (status.equalsIgnoreCase("ACTIVE")) { logStatus = true; } }
      */
 
     /**
      * Constructor for the <code>AMIdentityRepository</code> object. If a null
      * is passed for the organization identifier <code>orgName</code>, then
      * the "root" organization is assumed.
      * 
      * @param ssotoken
      *            Single sign on token of the user
      * @param orgName
      *            Name of the organization (can be a Fully qualified DN)
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public AMIdentityRepository(SSOToken ssotoken, String orgName)
             throws IdRepoException, SSOException {
         token = ssotoken;
         org = DNMapper.orgNameToDN(orgName);
     }
 
     /**
      * Returns the set of supported object types <code>IdType</code> for this
      * deployment. This is not organization specific.
      * 
      * @return Set of supported <code> IdType </code> objects.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public Set getSupportedIdTypes() throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set res = amdm.getSupportedTypes(token, org);
         res.remove(IdType.REALM);
         return res;
     }
 
     /**
      * Returns the set of Operations for a given <code>IdType</code>,
      * <code>IdOperations</code> that can be performed on an Identity. This
      * varies for each organization (and each plugin?).
      * 
      * @param type
      *            Type of identity
      * @return Set of <code>IdOperation</code> objects.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public Set getAllowedIdOperations(IdType type) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.getSupportedOperations(token, type, org);
 
     }
 
     /**
      * Return the special identities for this realm for a given type. These
      * identities cannot be deleted and hence have to be shown in the admin
      * console as non-deletable.
      * 
      * @param type
      *            Type of the identity
      * @return IdSearchResult
      * @throws IdRepoException
      *             if there is a datastore exception
      * @throws SSOException
      *             if the user's single sign on token is not valid.
      */
     public IdSearchResults getSpecialIdentities(IdType type)
             throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         IdSearchResults results = amdm.getSpecialIdentities(token, type, org);
 
         if (type.equals(IdType.USER)) {
             // Iterating through to get out the names and remove only amadmin
             // anonymous as per AM console requirement.
 
             IdSearchResults newResults = new IdSearchResults(type, org);
             Set identities = results.getSearchResults();
             if ((identities != null) && !identities.isEmpty()) {
                 for (Iterator i = identities.iterator(); i.hasNext(); ) {
                     AMIdentity amid = ((AMIdentity)i.next());
                     String remUser = amid.getName().toLowerCase();
                     if (!remUser.equalsIgnoreCase(IdConstants.AMADMIN_USER) &&
                         !remUser.equalsIgnoreCase(IdConstants.ANONYMOUS_USER)) {
                         newResults.addResult(amid, amid.getAttributes());
                     }
                 }
                 results = newResults;
             }
         }
         return results;
     }
 
     /**
      * Searches for identities of a certain type. The iterator returns
      * AMIdentity objects for use by the application.
      * 
      * @deprecated This method is deprecated. Use
      *             {@link #searchIdentities(
      *             IdType type,String pattern,IdSearchControl ctrl)}
      * @param type
      *            Type of identity being searched for.
      * @param pattern
      *            Search pattern, like "a*" or "*".
      * @param avPairs
      *            Map of attribute-values which can further help qualify the
      *            search pattern.
      * @param recursive
      *            If true, then the search is performed on the entire subtree
      *            (if applicable)
      * @param maxResults
      *            Maximum number of results to be returned. A -1 means no limit
      *            on the result set.
      * @param maxTime
      *            Maximum amount of time after which the search should return
      *            with partial results.
      * @param returnAttributes
      *            Set of attributes to be read when performing the search.
      * @param returnAllAttributes
      *            If true, then read all the attributes of the entries.
      * @return results containing <code>AMIdentity</code> objects.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public IdSearchResults searchIdentities(IdType type, String pattern,
             Map avPairs, boolean recursive, int maxResults, int maxTime,
             Set returnAttributes, boolean returnAllAttributes)
             throws IdRepoException, SSOException {
         // DelegationEvaluator de = new DelegationEvaluator();
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.search(token, type, pattern, avPairs, recursive,
                 maxResults, maxTime, returnAttributes, org);
     }
 
     /**
      * Searches for identities of certain types from each plugin and returns a
      * combined result
      * 
      * @param type
      *            Type of identity being searched for.
      * @param pattern
     *            Patter to be used when searching.
      * @param ctrl
      *            IdSearchControl which can be used to set up various search
      *            controls on the search to be performed.
      * @return Returns the combines results in an object IdSearchResults.
      * @see com.sun.identity.idm.IdSearchControl
      * @see com.sun.identity.idm.IdSearchResults
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public IdSearchResults searchIdentities(IdType type, String pattern,
             IdSearchControl ctrl) throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.search(token, type, pattern, ctrl, org);
     }
 
     /**
      * Returns a handle of the Identity object representing this realm for
      * services related operations only. This <code> AMIdentity
      * </code> object
      * can be used to assign and unassign services containing dynamic attributes
      * to this realm
      * 
      * @return a handle of the Identity object.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public AMIdentity getRealmIdentity() throws IdRepoException, SSOException {
         String univId = "id=ContainerDefaultTemplateRole,ou=realm," + org;
         return IdUtils.getIdentity(token, univId);
     }
 
     /**
      * Creates a single object of a type. The object is created in all the
      * plugins that support creation of this type of object.
      * 
      * @param type
      *            Type of object to be created.
      * @param idName
      *            Name of object
      * @param attrMap
      *            Map of attribute-values to be set when creating the entry.
      * @return Identity object representing the newly created entry.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public AMIdentity createIdentity(IdType type, String idName, Map attrMap)
             throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.create(token, type, idName, attrMap, org);
     }
 
     /**
      * Creates multiple objects of the same type. The objects are created in all
      * the <code>IdRepo</code> plugins that support creation of these objects.
      * 
      * @param type
      *            Type of object to be created
      * @param identityNamesAndAttrs
      *            Names of the identities and their
      * @return Set of created Identities.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public Set createIdentities(IdType type, Map identityNamesAndAttrs)
             throws IdRepoException, SSOException {
         Set results = new HashSet();
 
         if (identityNamesAndAttrs == null || identityNamesAndAttrs.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
         }
 
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Iterator it = identityNamesAndAttrs.keySet().iterator();
 
         while (it.hasNext()) {
             String name = (String) it.next();
             Map attrMap = (Map) identityNamesAndAttrs.get(name);
             AMIdentity id = amdm.create(token, type, name, attrMap, org);
             results.add(id);
         }
 
         return results;
     }
 
     /**
      * Deletes identities. The Set passed is a Set of identity names.
      * 
      * @param type
      *            Type of Identity to be deleted.
      * @param identities
      *            Set of AMIDentity objects to be deleted
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      */
     public void deleteIdentities(IdType type, Set identities)
             throws IdRepoException, SSOException {
         if (identities == null || identities.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
         }
 
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Iterator it = identities.iterator();
         while (it.hasNext()) {
             AMIdentity id = (AMIdentity) it.next();
            amdm.delete(token, id.getType(), id.getName(), org, id.DN);
         }
     }
 
     /**
      * Returns <code>true</code> if the data store has successfully
      * authenticated the identity with the provided credentials. In case the
      * data store requires additional credentials, the list would be returned
      * via the <code>IdRepoException</code> exception.
      * 
      * @param credentials
      *            Array of callback objects containing information such as
      *            username and password.
      * 
      * @return <code>true</code> if data store authenticates the identity;
      *         else <code>false</code>
      */
     public boolean authenticate(Callback[] credentials) throws IdRepoException,
             com.sun.identity.authentication.spi.AuthLoginException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return (amdm.authenticate(org, credentials));
     }
 
     /**
      * Adds a listener, which should receive notifications for all changes that
      * occured in this organization.
      * 
      * @param listener
      *            The callback which implements <code>AMEventListener</code>.
      * @return Integer identifier for this listener.
      */
     public int addEventListener(IdEventListener listener) {
         ArrayList listOfListeners = (ArrayList) listeners.get(org);
         if (listOfListeners == null) {
             listOfListeners = new ArrayList();
         }
         synchronized (listeners) {
             listOfListeners.add(listener);
             listeners.put(org, listOfListeners);
         }
         return (listOfListeners.size() - 1);
     }
 
     /**
      * Removes listener as the application is no longer interested in receiving
      * notifications.
      * 
      * @param identifier
      *            Integer identifying the listener.
      */
     public void removeEventListener(int identifier) {
         ArrayList listOfListeners = (ArrayList) listeners.get(org);
         if (listOfListeners != null) {
             synchronized (listeners) {
                 listOfListeners.remove(identifier);
             }
         }
     }
 }
