 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.deliver.util;
 
 //import org.exolab.castor.jdo.CacheManager;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.CacheManager;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentRelationImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
 import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
 import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
 import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.GroupContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
 import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
 import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
 import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
 import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
 import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.RoleContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
 import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
 import org.infoglue.cms.entities.management.impl.simple.ServerNodeImpl;
 import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
 import org.infoglue.cms.entities.management.impl.simple.UserContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
 import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
 import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
 import org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl;
 import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.ActionDefinitionImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.ActionImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.ActorImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.ConsequenceImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.EventImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.WorkflowImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.NotificationMessage;
 import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 
 import com.opensymphony.oscache.base.CacheEntry;
 import com.opensymphony.oscache.base.NeedsRefreshException;
 import com.opensymphony.oscache.base.events.CacheEntryEventListener;
 import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
 import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
 import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;
 import com.opensymphony.oscache.general.GeneralCacheAdministrator;
 
 
 public class CacheController extends Thread
 { 
     public final static Logger logger = Logger.getLogger(CacheController.class.getName());
 
     public static List notifications = new ArrayList();
     
     private static Map eventListeners = new HashMap();
 	private static Map caches = Collections.synchronizedMap(new Hashtable());
 	private boolean expireCacheAutomatically = false;
 	private int cacheExpireInterval = 1800000;
 	private boolean continueRunning = true;
 	
 	private static GeneralCacheAdministrator generalCache = new GeneralCacheAdministrator();
 	
     public static Date expireDateTime = null;
     public static Date publishDateTime = null;
 
 
 	public CacheController()
 	{
 		super();
 	}
 
 	public void setCacheExpireInterval(int cacheExpireInterval)
 	{
 		this.cacheExpireInterval = cacheExpireInterval;
 	}
 
 	public static void renameCache(String cacheName, String newCacheName)
 	{
 	    Object cacheInstance = caches.get(cacheName);
 	    
 	    if(cacheInstance != null)
 	    {
 	        synchronized(caches)
 	        {
 	            caches.put(newCacheName, cacheInstance);
 	            caches.remove(cacheName);
 	        }
 	    }
 	}	
 
 	public static void clearServerNodeProperty()
 	{
 		clearCache("serverNodePropertiesCache");
    	}
 
 	public static void cacheObject(String cacheName, Object key, Object value)
 	{
 		if(!caches.containsKey(cacheName))
 		    caches.put(cacheName, Collections.synchronizedMap(new Hashtable()));
 			
 		Map cacheInstance = (Map)caches.get(cacheName);
 		if(cacheInstance != null && key != null && value != null)
 	    {
 		    synchronized(cacheInstance)
 	        {
 			    cacheInstance.put(key, value);
 	        }
 	    }
 	}	
 	
 	public static Object getCachedObject(String cacheName, Object key)
 	{
 	    Map cacheInstance = (Map)caches.get(cacheName);
 	    
 	    if(cacheInstance != null)
 	    {
 	        synchronized(cacheInstance)
 	        {
 	            return cacheInstance.get(key);
 	        }
 	    }
 	    
         return null;
     }
 
 	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, String[] groups, boolean useGroups)
 	{
 	    //cacheObject(cacheName, key, value);
 	    
 	    if(!caches.containsKey(cacheName))
 	    {
 	        GeneralCacheAdministrator cacheAdministrator = new GeneralCacheAdministrator();
 	        
 	        CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
 	        CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
 	        
 	        cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
 	        cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
 	        caches.put(cacheName, cacheAdministrator);
 	        eventListeners.put(cacheName + "_cacheEntryEventListener", cacheEntryEventListener);
 	        eventListeners.put(cacheName + "_cacheMapAccessEventListener", cacheMapAccessEventListener);
 	    }
 	    
 	    /*
 	    System.out.println("Putting " + cacheName + " with key: " + key + " in relation to:");
 	    for(int i=0; i<groups.length; i++)
 	    {
 	    	System.out.println("group:" + groups[i]);
 	    }
 	    */
 	    /*
 	    logger.info("Putting " + cacheName + " with key: " + key + " in relation to:");
 	    for(int i=0; i<groups.length; i++)
 	    {
 	        logger.info("group:" + groups[i]);
 	    }
 	    */
 	    
 		GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 		synchronized(cacheAdministrator)
 		{
 			if(useGroups)
 			    cacheAdministrator.putInCache(key.toString(), value, groups);
 			else
 			    cacheAdministrator.putInCache(key.toString(), value);
 		}
 		
 		logger.info("Done cacheObjectInAdvancedCache");
 	}	
 	
 	public static Object getCachedObjectFromAdvancedCache(String cacheName, Object key)
 	{
 	    //logger.info("getCachedObjectFromAdvancedCache start:" + cacheName + ":" + key);
 
 	    Object value = null;
 	    
 	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 	    if(cacheAdministrator != null)
 	    {
 	        synchronized(cacheAdministrator)
 	        {
 			    try 
 			    {
 			        value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key.toString(), CacheEntry.INDEFINITE_EXPIRY);
 				} 
 			    catch (NeedsRefreshException nre) 
 			    {
 			    	cacheAdministrator.cancelUpdate(key.toString());
 				}
 	        }
 	    }
 	    
 	    //logger.info("getCachedObjectFromAdvancedCache stop...");
 
 		return value;
 	}
 
 	public static Object getCachedObjectFromAdvancedCache(String cacheName, Object key, int updateInterval)
 	{
 		if(cacheName == null || key == null)
 			return null;
 		
 	    //logger.info("getCachedObjectFromAdvancedCache start:" + cacheName + ":" + key + ":" + updateInterval);
 
 	    //return getCachedObject(cacheName, key);
 	    Object value = null;
 	    
 	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 	    if(cacheAdministrator != null)
 	    {
 	        synchronized(cacheAdministrator)
 	        {
 			    try 
 			    {
 			        value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key.toString(), updateInterval);
 				} 
 			    catch (NeedsRefreshException nre) 
 			    {
 			        cacheAdministrator.cancelUpdate(key.toString());
 				}
 	        }
 	    }
 	    //logger.info("getCachedObjectFromAdvancedCache stop...");
 
 		return value;
 	}
 
 	public static void clearCache(String cacheName)
 	{
 		logger.info("Clearing the cache called " + cacheName);
 		if(caches.containsKey(cacheName))
 		{
 		    Object object = caches.get(cacheName);
 		    if(object instanceof Map)
 			{
 				Map cacheInstance = (Map)object;
 				cacheInstance.clear();
 			}
 			else
 			{
 			    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
 				synchronized(cacheInstance)
 				{
 					cacheInstance.flushAll();
 				}
 			}
 	    	caches.remove(cacheName);
 		    eventListeners.remove(cacheName + "_cacheEntryEventListener");
 		    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 
 		    logger.info("clearCache stop...");
 		}
 	}
 		
 	public static void clearCaches(String entity, String entityId, String[] cachesToSkip)
 	{
 		if(entity == null)
 		{	
 			logger.info("Clearing the caches");
 			logger.info("caches.entrySet().size:" + caches.entrySet().size());
 			for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				logger.info("e:" + e.getKey());
 				boolean skip = false;
 				if(cachesToSkip != null)
 				{
 					for(int index=0; index<cachesToSkip.length; index++)
 					{
 					    if(e.getKey().equals(cachesToSkip[index]))
 					    {
 					        skip = true;
 					        break;
 					    }
 					}
 				}
 				
 				if(!skip)
 				{
 					Object object = e.getValue();
 					if(object instanceof Map)
 					{
 						Map cacheInstance = (Map)e.getValue();
 						cacheInstance.clear();
 					}
 					else
 					{
 					    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
 						synchronized(cacheInstance)
 						{
 					    	cacheInstance.flushAll();
 						}
 				        eventListeners.clear();
 					}
 					logger.info("Cleared cache:" + e.getKey());
 					
 			    	i.remove();
 				}
 			}
 		}
 		else
 		{
 			logger.info("Clearing some caches");
 			logger.info("entity:" + entity);
 
 			for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				logger.info("e:" + e.getKey());
 				boolean clear = false;
 				boolean selectiveCacheUpdate = false;
 				String cacheName = e.getKey().toString();
 				
 				if(cacheName.equalsIgnoreCase("serviceDefinitionCache") && entity.indexOf("ServiceBinding") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("qualifyerListCache") && (entity.indexOf("Qualifyer") > 0 || entity.indexOf("ServiceBinding") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("availableServiceBindingCache") && entity.indexOf("AvailableServiceBinding") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("languageCache") && entity.indexOf("Language") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("localeCache") && entity.indexOf("Language") > 0)
 				{	
 					clear = true;
 				}
 				if((cacheName.equalsIgnoreCase("latestSiteNodeVersionCache") || cacheName.equalsIgnoreCase("pageCacheLatestSiteNodeVersions") || cacheName.equalsIgnoreCase("pageCacheSiteNodeTypeDefinition")) && entity.indexOf("SiteNode") > 0)
 				{	
 					clear = true;
 				}
 				if((cacheName.equalsIgnoreCase("parentSiteNodeCache") || cacheName.equalsIgnoreCase("pageCacheParentSiteNodeCache")) && entity.indexOf("SiteNode") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("NavigationCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("pagePathCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentEditorCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("masterLanguageCache") && (entity.indexOf("Repository") > 0 || entity.indexOf("Language") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("parentRepository") && entity.indexOf("Repository") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("contentAttributeCache") && (entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0))
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("contentVersionCache") && (entity.indexOf("Content") > -1 || entity.indexOf("AccessRight") > 0))
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("boundSiteNodeCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNode") > 0 || entity.indexOf("AccessRight") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("boundContentCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("Content") > 0 || entity.indexOf("AccessRight") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("pageCache") && entity.indexOf("Registry") == -1)
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentCache") && entity.indexOf("Registry") == -1)
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentPropertyCache") && (entity.indexOf("ContentVersion") > -1 || entity.indexOf("AccessRight") > 0))
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("includeCache"))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("authorizationCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentPaletteDivCache") && (entity.indexOf("AccessRight") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("userCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if((cacheName.equalsIgnoreCase("assetUrlCache") || cacheName.equalsIgnoreCase("assetThumbnailUrlCache")) && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("sortedChildContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("workflowCache") && entity.indexOf("WorkflowDefinition") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("rootSiteNodeCache") && entity.indexOf("SiteNode") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("childSiteNodesCache") && entity.indexOf("SiteNode") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("propertySetCache") && entity.indexOf("SiteNode") > 0)
 				{
 				    clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("groupListCache") && entity.indexOf("Group") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("roleListCache") && entity.indexOf("Role") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("groupPropertiesCache") && entity.indexOf("Group") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("rolePropertiesCache") && entity.indexOf("Role") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("principalPropertyValueCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("relatedCategoriesCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0 || entity.indexOf("User") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("redirectCache") && entity.indexOf("Redirect") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("interceptorsCache") && entity.indexOf("Intercept") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("interceptionPointCache") && entity.indexOf("Intercept") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("siteNodeLanguageCache") && (entity.indexOf("Repository") > 0 || entity.indexOf("Language") > 0 || entity.indexOf("SiteNode") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("ServerNodeProperties"))
 				{
 					clear = true;
 				}
 				
 				if(clear)
 				{	
 				    //System.out.println("clearing:" + e.getKey());
 				    logger.info("clearing:" + e.getKey());
 					Object object = e.getValue();
 					if(object instanceof Map)
 					{
 						Map cacheInstance = (Map)e.getValue();
 						cacheInstance.clear();
 					}
 					else
 					{
 					    String useSelectivePageCacheUpdateString = CmsPropertyHandler.getUseSelectivePageCacheUpdate();
 					    boolean useSelectivePageCacheUpdate = false;
 					    if(useSelectivePageCacheUpdateString != null && useSelectivePageCacheUpdateString.equalsIgnoreCase("true"))
 					        useSelectivePageCacheUpdate = true;
 					        
 					    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
 					    synchronized(cacheInstance)
 					    {
 					    	if(selectiveCacheUpdate && entity.indexOf("SiteNode") > 0)
 						    {
 						    	cacheInstance.flushAll();
 						    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
 							    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 						    	logger.info("clearing:" + e.getKey());
 						    }
 						    else if(selectiveCacheUpdate && entity.indexOf("ContentVersion") > 0 && useSelectivePageCacheUpdate)
 						    {
 						        Object cacheEntryEventListener = eventListeners.get(e.getKey() + "_cacheEntryEventListener");
 					    		Object cacheMapAccessEventListener = eventListeners.get(e.getKey() + "_cacheMapAccessEventListener");
 					    		 
 					    		cacheInstance.flushGroup("contentVersion_" + entityId);
 					    		cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
 						    	logger.info("clearing " + e.getKey() + " with group " + "contentVersion_" + entityId);
 						    	//System.out.println("clearing " + e.getKey() + " with group " + "contentVersion_" + entityId);
 	
 						    	try
 						    	{
 							    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(entityId));
 						    		if(contentVersionVO != null)
 						    		{
 						    			cacheInstance.flushGroup("content_" + contentVersionVO.getContentId());
 						    			//System.out.println("also clearing " + e.getKey() + " with group " + "content_" + contentVersionVO.getContentId());
 						    		}
 						    	}
 						    	catch(SystemException se)
 						    	{
 						    		logger.info("Missing content version: " + se.getMessage());
 						    	}
 						    }
 						    else if(selectiveCacheUpdate && entity.indexOf("Content") > 0 && useSelectivePageCacheUpdate)
 						    {
 						    	cacheInstance.flushGroup("content_" + entityId);
 						    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
 						    	logger.info("clearing " + e.getKey() + " with group " + "content_" + entityId);
 						    	//System.out.println("clearing " + e.getKey() + " with group " + "content_" + entityId);
 							}
 						    else
 						    {
 						    	cacheInstance.flushAll();
 						    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
 							    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 								logger.info("clearing:" + e.getKey());
 						    }
 						}
 					}
 					
 					logger.info("Cleared cache:" + e.getKey());
 
 					if(!selectiveCacheUpdate)
 					    i.remove();
 					
 				}
 				else
 				{
 					logger.info("Did not clear " + e.getKey());
 				}
 			}
 		}
 	}
 	
 	public static synchronized void clearCastorCaches() throws Exception
 	{
 	    logger.info("Emptying the Castor Caches");
 	    
 	    while(RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() > 0)
 	    {
 	        logger.info("Number of requests: " + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + " was more than 0 - lets wait a bit.");
 	        Thread.sleep(10);
 	    }
 	    
 		Database db = CastorDatabaseService.getDatabase();
 		//CastorDatabaseService.setBlock(true);
 		
 		try
 		{		
 		    //db.getCacheManager().expireCache();
 
 		    clearCache(db, SmallContentImpl.class);
 			clearCache(db, MediumContentImpl.class);
 			clearCache(db, ContentImpl.class);
 			clearCache(db, ContentRelationImpl.class);
 			clearCache(db, ContentVersionImpl.class);
 			clearCache(db, DigitalAssetImpl.class);
 			clearCache(db, SmallAvailableServiceBindingImpl.class);
 			clearCache(db, AvailableServiceBindingImpl.class);
 			clearCache(db, ContentTypeDefinitionImpl.class);
 			clearCache(db, LanguageImpl.class);
 			clearCache(db, RepositoryImpl.class);
 			clearCache(db, RepositoryLanguageImpl.class);
 			clearCache(db, RoleImpl.class);
 			clearCache(db, GroupImpl.class);
 			clearCache(db, ServiceDefinitionImpl.class);
 			clearCache(db, SiteNodeTypeDefinitionImpl.class);
 			clearCache(db, SystemUserImpl.class);
 			clearCache(db, QualifyerImpl.class);
 			clearCache(db, ServiceBindingImpl.class);
 			clearCache(db, SmallSiteNodeImpl.class);
 			clearCache(db, SiteNodeImpl.class);
 			clearCache(db, SiteNodeVersionImpl.class);
 			clearCache(db, PublicationImpl.class);
 			//clearCache(db, PublicationDetailImpl.class); // This class depends on publication
 			clearCache(db, ActionImpl.class);
 			clearCache(db, ActionDefinitionImpl.class);
 			clearCache(db, ActorImpl.class);
 			clearCache(db, ConsequenceImpl.class);
 			clearCache(db, ConsequenceDefinitionImpl.class);
 			clearCache(db, EventImpl.class);
 			clearCache(db, WorkflowImpl.class);
 			clearCache(db, WorkflowDefinitionImpl.class);
 			clearCache(db, CategoryImpl.class);
 			clearCache(db, ContentCategoryImpl.class);
 			clearCache(db, RegistryImpl.class);
 			clearCache(db, RedirectImpl.class);
 			
 			clearCache(db, InterceptionPointImpl.class);
 			clearCache(db, InterceptorImpl.class);
 			clearCache(db, AccessRightImpl.class);
 	
 			clearCache(db, RolePropertiesImpl.class);
 			clearCache(db, UserPropertiesImpl.class);
 			clearCache(db, GroupPropertiesImpl.class);
 			clearCache(db, UserContentTypeDefinitionImpl.class);
 			clearCache(db, RoleContentTypeDefinitionImpl.class);
 			clearCache(db, GroupContentTypeDefinitionImpl.class);			
 			
 			clearCache(db, ServerNodeImpl.class);			
 			
 		    //commitTransaction(db);
 
 			logger.info("Emptied the Castor Caches");
 		}
 		catch(Exception e)
 		{
 		    logger.error("Exception when tried empty the Castor Caches");
 		    rollbackTransaction(db);
 		}
 		finally
 		{
 			db.close();
 			//CastorDatabaseService.setBlock(false);
 		}
 	}
 	
 	
 	public static synchronized void clearCache(Class type, Object[] ids) throws Exception
 	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 		    CacheManager manager = db.getCacheManager();
 		    manager.expireCache(type, ids);
 		    //Class[] types = {type};
 		    //db.expireCache(types, ids);
 		    
 		    if(type.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
 		       type.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
 		       type.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
 		       type.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
 			   type.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
 		    {
 		        expireDateTime = null;
 		        publishDateTime = null;
 		    }
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 		    db.close();			
 		}
 	}
 	
 	private static synchronized void clearCache(Class c) throws Exception
 	{
 	    Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 		    clearCache(db, c);
 		    /*
 		    Class[] types = {c};
 			Class[] ids = {null};
 			CacheManager manager = db.getCacheManager();
 			manager.expireCache(types);
 			//db.expireCache(types, null);
 			
 		    if(c.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
 		       c.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
 		       c.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
 		       c.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
 			   c.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
 		    {
 		        expireDateTime = null;
 		        publishDateTime = null;
 		    }
 		    */
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			db.close();			
 		}
 	}
 
 	private static synchronized void clearCache(Database db, Class c) throws Exception
 	{
 		Class[] types = {c};
 		Class[] ids = {null};
 		CacheManager manager = db.getCacheManager();
 		manager.expireCache(types);
 		//db.expireCache(types, null);
 		
 	    if(c.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
 	       c.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
 	       c.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
 	       c.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
 		   c.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
 	    {
 	        expireDateTime = null;
 	        publishDateTime = null;
 	    }
 	}
 
 	
 	public void run() 
 	{
 		while(this.continueRunning && expireCacheAutomatically)
 		{
 			logger.info("Clearing caches");
 			try
 			{
 			    clearCastorCaches();
 			}
 			catch(Exception e)
 			{
 			    logger.error("Error clearing cache in expireCacheAutomatically thread:" + e.getMessage(), e);
 			}
 			logger.info("Castor cache cleared");
 			clearCaches(null, null, null);
 			logger.info("All other caches cleared");
 			
 			try
 			{
 				sleep(cacheExpireInterval);
 			} 
 			catch (InterruptedException e){}
 		}
 	}
 
 	public static synchronized void cacheCentralCastorCaches() throws Exception
 	{
 	    Database db = CastorDatabaseService.getDatabase();
 
 	    DatabaseWrapper dbWrapper = new DatabaseWrapper(db);
 
 		try
 		{
 	    	
 	    	beginTransaction(db);
 		    
 	    	String siteNodesToRecacheOnPublishing = CmsPropertyHandler.getSiteNodesToRecacheOnPublishing();
 	    	String recachePublishingMethod = CmsPropertyHandler.getRecachePublishingMethod();
 	    	logger.info("siteNodesToRecacheOnPublishing:" + siteNodesToRecacheOnPublishing);
 	    	if(siteNodesToRecacheOnPublishing != null && !siteNodesToRecacheOnPublishing.equals("") && !siteNodesToRecacheOnPublishing.equals("siteNodesToRecacheOnPublishing"))
 	    	{
 	    	    String[] siteNodeIdArray = siteNodesToRecacheOnPublishing.split(",");
 	    	    for(int i=0; i<siteNodeIdArray.length; i++)
 	    	    {
 	    	        Integer siteNodeId = new Integer(siteNodeIdArray[i]);
 	    	    	logger.info("siteNodeId to recache:" + siteNodeId);
 	    	    	if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("contentCentric"))
 	    	    	    new ContentCentricCachePopulator().recache(dbWrapper, siteNodeId);
 	    	    	else if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("requestCentric"))
 	    	    	    new RequestCentricCachePopulator().recache(dbWrapper, siteNodeId);
 	    	    	else if(recachePublishingMethod != null && recachePublishingMethod.equalsIgnoreCase("requestAndMetaInfoCentric"))
 	    	    	    new RequestAndMetaInfoCentricCachePopulator().recache(dbWrapper, siteNodeId);
 	    	    	else
 	    	    	    logger.warn("No recaching was made during publishing - set the parameter recachePublishingMethod to 'contentCentric' or 'requestCentric' to recache.");
 	    	    }
 	    	}
 		    
 		    commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to rebuild the castor cache:" + e.getMessage(), e);
 		    rollbackTransaction(db);
 		}
 		finally
 		{
 		    closeDatabase(db);
 		}
 	}
 	
 	
 	public void stopThread()
 	{
 		this.continueRunning = false;
 	}
 
 	public boolean getExpireCacheAutomatically() 
 	{
 		return expireCacheAutomatically;
 	}
 
 	public void setExpireCacheAutomatically(boolean expireCacheAutomatically) 
 	{
 		this.expireCacheAutomatically = expireCacheAutomatically;
 	}
 	
     public static Map getCaches()
     {
         return caches;
     }
     
     public static Map getEventListeners()
     {
         return eventListeners;
     }
 
     public static GeneralCacheAdministrator getGeneralCache()
     {
         return generalCache;
     }
     
     public static List getNotifications()
     {
         return notifications;
     }
     
     public static void evictWaitingCache() throws Exception
     {	    
 	    String operatingMode = CmsPropertyHandler.getOperatingMode();
 	    
 	    if(operatingMode != null && operatingMode.equalsIgnoreCase("3") && RequestAnalyser.getBlockRequests())
 	    {
 		    logger.info("evictWaitingCache allready in progress - returning to avoid conflict");
 	        return;
 	    }
 	    
         synchronized(notifications)
         {
 			Iterator i = notifications.iterator();
 			while(i.hasNext())
 			{
 			    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
 			    String className = cacheEvictionBean.getClassName();
 			    String objectId = cacheEvictionBean.getObjectId();
 			    String objectName = cacheEvictionBean.getObjectName();
 				String typeId = cacheEvictionBean.getTypeId();
 				
 			    logger.info("className:" + className);
 				logger.info("objectId:" + objectId);
 				logger.info("objectName:" + objectName);
 				logger.info("typeId:" + typeId);
 
 				try
 			    {
 					//Here we do what we need to if the server properties has changed.
 				    if(className != null && className.equalsIgnoreCase("ServerNodeProperties"))
 				    {
 						try 
 						{
 							clearServerNodeProperty();
 							InfoGlueAuthenticationFilter.initializeProperties();
 							logger.info("Updating InfoGlueAuthenticationFilter");
 						} 
 						catch (SystemException e1) 
 						{
 							logger.warn("Could not refresh authentication filter:" + e1.getMessage(), e1);
 						}
 				    }
 
 				    //if(operatingMode != null && operatingMode.equalsIgnoreCase("0")) //If published-mode we update entire cache to be sure..
 					if(operatingMode != null && operatingMode.equalsIgnoreCase("3")) //If published-mode we update entire cache to be sure..
 					{
 				        if(!RequestAnalyser.getBlockRequests())
 				        {
  			                logger.info("Starting publication thread...");
  			            	PublicationThread pt = new PublicationThread();
  			            	pt.setPriority(Thread.MIN_PRIORITY);
  			            	pt.start();
 			            	logger.info("Done starting publication thread...");
 			            }
 			        }
 				    else
 				    {
 					    boolean isDependsClass = false;
 					    if(className != null && className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
 					        isDependsClass = true;
 				
 					    CacheController.clearCaches(className, objectId, null);
 
 					    logger.info("Updating className with id:" + className + ":" + objectId);
 						if(className != null && !typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM))
 						{
 						    //Class[] types = {Class.forName(className)};
 						    Class type = Class.forName(className);
 						    
 						    if(!isDependsClass && className.equalsIgnoreCase(SystemUserImpl.class.getName()) || className.equalsIgnoreCase(RoleImpl.class.getName()) || className.equalsIgnoreCase(GroupImpl.class.getName()))
 						    {
 						        Object[] ids = {objectId};
 						        CacheController.clearCache(type, ids);
 							}
 						    else if(!isDependsClass)
 						    {
 						        Object[] ids = {new Integer(objectId)};
 							    CacheController.clearCache(type, ids);
 						    }
 						    
 							//If it's an contentVersion we should delete all images it might have generated from attributes.
 							if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
 							{
 							    logger.info("We clear all small contents as well " + objectId);
 								Class typesExtra = SmallContentImpl.class;
 								Object[] idsExtra = {new Integer(objectId)};
 								CacheController.clearCache(typesExtra, idsExtra);
 				
 								logger.info("We clear all medium contents as well " + objectId);
 								Class typesExtraMedium = MediumContentImpl.class;
 								Object[] idsExtraMedium = {new Integer(objectId)};
 								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
 							}
 							else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
 							{
 							    Class typesExtra = SmallAvailableServiceBindingImpl.class;
 								Object[] idsExtra = {new Integer(objectId)};
 								CacheController.clearCache(typesExtra, idsExtra);
 							}
 							else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
 							{
 							    Class typesExtra = SmallSiteNodeImpl.class;
 								Object[] idsExtra = {new Integer(objectId)};
 								CacheController.clearCache(typesExtra, idsExtra);
 							}
 							else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
 							{
 							    logger.info("We should delete all images with digitalAssetId " + objectId);
 								DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
 							}
 						}	
 						//else
 						//{
 						//    logger.info("\n\n\nclearing all non-db caches as this was a system settings call..\n\n\n");											
 						//    CacheController.clearCaches(null, null, null);
 						//}
 				    }
 			    }
 			    catch(Exception e)
 			    {
 			        logger.error("Cache eviction reported an error:" + e.getMessage(), e);
 			    }
 
 		        logger.info("Cache evicted..");
 
 				i.remove();
 			}
         }
 
         logger.info("evictWaitingCache stop");
     }
 
     /**
      * Composer of the pageCacheKey.
      * 
      * @param siteNodeId
      * @param languageId
      * @param contentId
      * @param userAgent
      * @param queryString
      * @return
      */
     
     public static String getPageCacheKey(HttpSession session, HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra)
     {    		
     	String originalRequestURL = request.getParameter("originalRequestURL");
     	if(originalRequestURL == null || originalRequestURL.length() == 0)
     		originalRequestURL = request.getRequestURL().toString();
 
     	String pageKey = null;
     	String pageKeyProperty = CmsPropertyHandler.getPageKey();
     	if(pageKeyProperty != null && pageKeyProperty.length() > 0)
     	{    
     	    pageKey = pageKeyProperty;
     	    pageKey = pageKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
     	    pageKey = pageKey.replaceAll("\\$languageId", "" + languageId);
     	    pageKey = pageKey.replaceAll("\\$contentId", "" + contentId);
     	    pageKey = pageKey.replaceAll("\\$useragent", "" + userAgent);
     	    pageKey = pageKey.replaceAll("\\$queryString", "" + queryString);
     	    
     	    int sessionAttributeStartIndex = pageKey.indexOf("$session.");
     	    while(sessionAttributeStartIndex > -1)
     	    {
         	    int sessionAttributeEndIndex = pageKey.indexOf("_", sessionAttributeStartIndex);
         	    String sessionAttribute = null;
         	    if(sessionAttributeEndIndex > -1)
         	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
         	    else
         	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9);
 
         	    pageKey = pageKey.replaceAll("\\$session." + sessionAttribute, "" + session.getAttribute(sessionAttribute));    	    
     	    
         	    sessionAttributeStartIndex = pageKey.indexOf("$session.");
     	    }
     	    
     	    int cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
     	    while(cookieAttributeStartIndex > -1)
     	    {
         	    int cookieAttributeEndIndex = pageKey.indexOf("_", cookieAttributeStartIndex);
         	    String cookieAttribute = null;
         	    if(cookieAttributeEndIndex > -1)
         	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
         	    else
         	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8);
 
         	    HttpHelper httpHelper = new HttpHelper();
         	    pageKey = pageKey.replaceAll("\\$cookie." + cookieAttribute, "" + httpHelper.getCookie(request, cookieAttribute));    	    
     	    
         	    cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
     	    }
 
     	}
     	else
     	    pageKey  = "" + siteNodeId + "_" + languageId + "_" + contentId + "_" + userAgent + "_" + queryString;
     	
     	return originalRequestURL + "_" + pageKey + extra;
     }
     
     
 	/**
 	 * Rollbacks a transaction on the named database
 	 */
      /*
 	public static void closeTransaction(Database db) throws SystemException
 	{
 	    //if(db != null && !db.isClosed() && db.isActive())
 	        commitTransaction(db);
 	}
 */
     
 	/**
 	 * Begins a transaction on the named database
 	 */
          
 	public static void beginTransaction(Database db) throws SystemException
 	{
 		try
 		{
 			db.begin();
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage());
 			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
 		}
 	}
 
 	/**
 	 * Ends a transaction on the named database
 	 */
 	
     private static void commitTransaction(Database db) throws SystemException
 	{
 		try
 		{
 		    if (db.isActive())
 		    {
 			    db.commit();
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage());
 			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
 		}
 	}
 
  
 	/**
 	 * Rollbacks a transaction on the named database
 	 */
     
 	public static void rollbackTransaction(Database db)
 	{
 		try
 		{
 			if (db.isActive())
 			{
 			    db.rollback();
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
 		}
 	}
 
 	/**
 	 * Close the database
 	 */
      
 	public static void closeDatabase(Database db)
 	{
 		try
 		{
 			db.close();
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred when we tried to close a database. Reason:" + e.getMessage(), e);    
 		}
 	}
 }
 
