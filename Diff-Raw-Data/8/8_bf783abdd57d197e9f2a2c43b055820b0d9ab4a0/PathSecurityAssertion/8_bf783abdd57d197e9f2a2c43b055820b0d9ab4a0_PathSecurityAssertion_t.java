 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.sdata.tool.SDataAccessException;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.api.SecurityAssertion;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * An implementation of the Security Assertion that uses the http method, the
  * path and the sakai security service for perform the assertion. On check it
  * will throw SDataExceptions indicating forbidden if the path is outside its
  * configured range, or it is denied by the the Sakai security service.
  * 
  * @author ieb
  */
 public class PathSecurityAssertion implements SecurityAssertion
 {
 
 	/**
 	 * the init parameter name for baseLocation
 	 */
 	private static final String BASE_LOCATION_INIT = "locationbase";
 
 	/**
 	 * The default setting for the baseLocation
 	 */
 	private static final String DEFAULT_BASE_LOCATION = "";
 
 	/**
 	 * the init parameter for baseResource
 	 */
 	private static final String BASE_REFERENCE_INIT = "referencebase";
 
 	/**
 	 * the default value for base resource
 	 */
 	private static final String DEFAULT_BASE_REFERENCE = "";
 
 	/**
 	 * the init parameter name for the lock map
 	 */
 	private static final String LOCK_MAP_INIT = "locks";
 
 	/**
 	 * the default lock map
 	 */
 	private static final String DEFAULT_LOCK_MAP = "GET:content.read,PUT:content.revise.any,HEAD:content.read,POST:content.revise.any,DELETE:content.delete.any";
 
 	private static final Log log = LogFactory.getLog(PathSecurityAssertion.class);
 
 	/**
 	 * The base location that is Security Advisor applies to. Only paths that
 	 * start with this are allowed all others are denied regardless of the
 	 * method. If the path starts with baseLocation, baseLocation is removed
 	 * from the path and baseResource is prepended to the patch to generate a
 	 * full resource location suitable for using with the security service.
 	 */
 	private String baseLocation;
 
 
 
 	/**
 	 * A map mapping http methods to locks
 	 */
 	private Map<String, String> locks;
 
 	/**
 	 * this is prepended to the resource path, after normalizing (ie removing
 	 * baseLocation) and before sending to the Sakai security service.
 	 */
 	private String baseReference;
 
 	private boolean inTest = false;
 
 	private EntityManager entityManager;
 
 	private AuthzGroupService authzGroupService;
 
 	private SessionManager sessionManager;
 
 	/**
 	 * Construct a PathSecurityAssertion class based on the standard
 	 * configuration map. The Map may have init parameters as defined by
 	 * BASE_LOCATION_INIT, BASE_RESOURCE_LOCATION_INIT, LOCK_MAP_INIT
 	 * 
 	 * @param config
 	 */
 	public PathSecurityAssertion(Map<String, String> config)
 	{
 		baseLocation = config.get(BASE_LOCATION_INIT);
 		if (baseLocation == null)
 		{
 			baseLocation = DEFAULT_BASE_LOCATION;
 		}
 		else
 		{
 			log.info("Set Base Location to " + baseLocation);
 		}
 		baseReference = config.get(BASE_REFERENCE_INIT);
 		if (baseReference == null)
 		{
 			baseReference = DEFAULT_BASE_REFERENCE;
 		}
 		else
 		{
 			log.info("Set Base Reference to " + baseReference);
 		}
 		String lockMapSpec = config.get(LOCK_MAP_INIT);
 		if (lockMapSpec == null)
 		{
 			lockMapSpec = DEFAULT_LOCK_MAP;
 		}
 		else
 		{
 			log.info("Set lockMapSpec to " + lockMapSpec);
 		}
 		locks = new HashMap<String, String>();
 		String[] specList = lockMapSpec.split(",");
 		for (String spec : specList)
 		{
 			String[] kv = spec.split(":", 2);
 			locks.put(kv[0], kv[1]);
 		}
 		String testMode = config.get("testmode");
 		if (testMode == null)
 		{
 			inTest = false;
 			entityManager = Kernel.entityManager();
 			authzGroupService = Kernel.authzGroupService();
 			sessionManager = Kernel.sessionManager();
 		}
 		else
 		{
 			inTest = true;
 		}
 
 	}
 
 	/**
 	 * Performs the security assertion based on the resourceLocation, from the
 	 * original request and the method begin attempted. Will throw a
 	 * SDataException with Forbidden if the resource location is outside the
 	 * configured range, or if permission is denied.
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.SecurityAssertion#check(java.lang.String,java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void check(String method, String resourceLocation) throws SDataException
 	{
		
 		if (inTest || entityManager == null )
 		{
 			return;
 		}
 		if (!(baseLocation.length() == 0)
 				&& (resourceLocation == null || !resourceLocation
 						.startsWith(baseLocation)))
 		{
 			log.info("Denied " + method + " on [" + resourceLocation
 					+ "] base mismatch [" + baseLocation + "]");
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN, "Access Forbidden");
 		}
 		String resourceReference = baseReference
 				+ resourceLocation.substring(baseLocation.length());
 		Reference ref = entityManager.newReference(resourceReference);
 		
		try {
			if (resourceLocation.substring(baseLocation.length() + 1).split("/")[0].equals("~" + sessionManager.getCurrentSessionUserId())){
				return;
			}
		} catch (Exception e){}
		
 		// the main problem here is how do we know if this is a collection or a resource, as the trailing / matters.
 		
 		Collection<?> groups = ref.getAuthzGroups();
 		String resourceLock = getResourceLock(method);
 		
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		if (authzGroupService.isAllowed(userId, resourceLock, groups))
 		{
 			log.info("Granted [" + method + "]:[" + resourceLock + "] on ["
 					+ resourceReference + "]");
 			return;
 		}
 		// make certain this is not a collection
 		if ( !resourceReference.endsWith("/") ) {
 			resourceReference = resourceReference + "/";
 			ref = entityManager.newReference(resourceReference);
 			groups = ref.getAuthzGroups();
 			if (authzGroupService.isAllowed(userId, resourceLock, groups))
 			{
 				log.info("Granted [" + method + "]:[" + resourceLock + "] on ["
 						+ resourceReference + "]");
 				return;
 			}
 		}
 		log.info("All Denied " + method + ":" + resourceLock + " on "
 				+ resourceLocation + " baseReference:[" + baseReference
 				+ "] baseLocation:[" + baseLocation + "]");
 		throw new SDataAccessException(HttpServletResponse.SC_FORBIDDEN,
 				"Access denied for operation " + method);
 	}
 
 	/**
 	 * Convert the HTTP Method into a lock.
 	 * 
 	 * @param method
 	 * @return
 	 */
 	private String getResourceLock(String method)
 	{
 		return locks.get(method);
 	}
 
 
 	public EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	public void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	public AuthzGroupService getAuthzGroupService() {
 		return authzGroupService;
 	}
 
 	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
 		this.authzGroupService = authzGroupService;
 	}
 
 	public SessionManager getSessionManager() {
 		return sessionManager;
 	}
 
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 
 }
