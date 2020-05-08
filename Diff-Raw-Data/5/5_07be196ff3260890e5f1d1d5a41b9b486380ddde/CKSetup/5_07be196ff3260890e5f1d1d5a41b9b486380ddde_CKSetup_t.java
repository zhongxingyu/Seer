 /**********************************************************************************
  * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/CKSetup.java $
  * $Id: CKSetup.java 3635 2012-12-02 21:26:23Z ggolden $
  ***********************************************************************************
  *
 * Copyright (c) 2012, 2013 Etudes, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.mneme.tool;
 
 import java.util.Collection;
 import java.util.Vector;
 
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.tool.cover.SessionManager;
 
 /**
  * The class that contains methods that perform a security check for Mneme and set the collection id for CK editor
  */
 public class CKSetup
 {
 
 	public CKSetup()
 	{
 
 	}
 
 	/**
 	 * Sets the session manager with the collection id and security advisor
 	 * 
 	 * @param docsPath
 	 *        Path to collection
 	 * @param toolContext
 	 *        Current site id
 	 */
 	public void setCKCollectionAttrib(String docsPath, String toolContext)
 	{
 		if (docsPath != null && toolContext != null)
 		{
 			docsPath = docsPath.replace("{CONTEXT}", toolContext);
 			SessionManager.getCurrentSession().setAttribute("ck.collectionId", docsPath);
			String attrb = "fck.security.advisor." + docsPath;
 
 			SessionManager.getCurrentSession().setAttribute(attrb, new SecurityAdvisor()
 			{
 				public SecurityAdvice isAllowed(String userId, String function, String reference)
 				{
 					try
 					{
 						String anotherRef = new String(reference);
 						anotherRef = anotherRef.replace("/content/private/mneme", "/site");
 						org.sakaiproject.entity.api.Reference ref1 = org.sakaiproject.entity.cover.EntityManager.newReference(anotherRef);
 						Collection<?> mnemeGrpAllow = org.sakaiproject.authz.cover.AuthzGroupService.getAuthzGroupsIsAllowed(userId,
 								org.etudes.mneme.api.MnemeService.MANAGE_PERMISSION, null);
 						if (mnemeGrpAllow.contains("/site/" + ref1.getContainer()))
 						{
 							return SecurityAdvice.ALLOWED;
 						}
 						/*
 						 * if (checkSecurity(userId, "mneme.manage", ref1.getContainer())) return SecurityAdvice.ALLOWED; if (checkSecurity(userId, "mneme.grade", ref1.getContainer())) return SecurityAdvice.ALLOWED;
 						 * 
 						 * // otherwise, user must be submission user and have submit permission if (checkSecurity(userId, "mneme.submit", ref1.getContainer())) return SecurityAdvice.ALLOWED;
 						 */
 					}
 					catch (Exception e)
 					{
 						// logger.warn("exception in setting security advice for CK collection" + e.toString());
 						e.printStackTrace();
 						return SecurityAdvice.NOT_ALLOWED;
 					}
 					return SecurityAdvice.NOT_ALLOWED;
 				}
 
 				/*
 				 * public boolean checkSecurity(String userId, String function, String context) { // check for super user if (org.sakaiproject.authz.cover.SecurityService.isSuperUser(userId)) return true;
 				 * 
 				 * // check for the user / function / context-as-site-authz // use the site ref for the security service (used to cache the security calls in the security service) String siteRef =
 				 * org.sakaiproject.site.cover.SiteService.siteReference(context); // form the azGroups for a context-as-implemented-by-site
 				 * 
 				 * Collection azGroups = new Vector(2); azGroups.add(siteRef); azGroups.add("!site.helper"); boolean rv = false; try { rv = org.sakaiproject.authz.cover.SecurityService.unlock(userId, function, siteRef, azGroups); } catch (Exception e) {
 				 * e.printStackTrace(); } return rv; // return true; }
 				 */
 			});
 		}
 	}
 
 }
