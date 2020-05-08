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
 
 package org.infoglue.cms.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
 import org.infoglue.cms.entities.management.ServerNodeVO;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.Timer;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
 
 
 /**
  * CMSPropertyHandler.java
  * Created on 2002-sep-12 
  * 
  * This class is used to get properties for the system in a transparent way.
  * The second evolution of this class made it possible for properties to be fetched from the propertyset if there instead. Fallback to file.
  * 
  * @author Stefan Sik, ss@frovi.com
  * @author Mattias Bogeblad 
  */
 
 public class CmsPropertyHandler
 {
     private final static Logger logger = Logger.getLogger(CmsPropertyHandler.class.getName());
 
 	private static Properties cachedProperties 		= null;
 	private static PropertySet propertySet			= null; 
 
 	private static String serverNodeName			= null;
 	
 	private static String globalSettingsServerNodeId= "-1";
 	private static String localSettingsServerNodeId	= null;
 	
 	private static String applicationName 			= null;
 	private static String contextRootPath 			= null;
 	private static String operatingMode				= null;
 	private static File propertyFile 				= null;
 	
 	public static void setApplicationName(String theApplicationName)
 	{
 		CmsPropertyHandler.applicationName = theApplicationName;
 	}
 
 	public static void setContextRootPath(String contextRootPath)
 	{
 		CmsPropertyHandler.contextRootPath = contextRootPath;
 	}
 
 	public static void setOperatingMode(String operatingMode)
 	{
 		CmsPropertyHandler.operatingMode = operatingMode;
 	}
 
 	public static String getApplicationName()
 	{
 		return applicationName;
 	}
 	
 	public static void setPropertyFile(File aPropertyFile)
 	{
 		propertyFile = aPropertyFile;
 	}
 	
 	/**
 	 * This method initializes the parameter hash with values.
 	 */
 
 	private static void initializeProperties()
 	{
 	    try
 		{
 			System.out.println("**************************************");
 			System.out.println("Initializing properties from file.....");
 			System.out.println("**************************************");
 			
 			cachedProperties = new Properties();
 			if(propertyFile != null)
 			    cachedProperties.load(new FileInputStream(propertyFile));
 			else
 			    cachedProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + applicationName + ".properties"));
 			
 			Enumeration enumeration = cachedProperties.keys();
 			while(enumeration.hasMoreElements())
 			{
 				String key = (String)enumeration.nextElement();
 				if(key.indexOf("webwork.") > 0)
 				{
 					webwork.config.Configuration.set(key, cachedProperties.getProperty(key)); 
 				}
 			}
 			
 	        Map args = new HashMap();
 		    args.put("globalKey", "infoglue");
 		    propertySet = PropertySetManager.getInstance("jdbc", args);
 		    
 		    serverNodeName = cachedProperties.getProperty("serverNodeName");
 		    
 		    if(serverNodeName == null || serverNodeName.length() == 0)
 		    {
 		    	try
 		    	{
 				    InetAddress localhost = InetAddress.getLocalHost();
 				    serverNodeName = localhost.getHostName();
 		    	}
 		    	catch(Exception e)
 		    	{
 		    		System.out.println("Error initializing serverNodeName:" + e.getMessage());
 		    	}
 		    }
 		    
 		    System.out.println("serverNodeName:" + serverNodeName);
 		    
 		    initializeLocalServerNodeId();
 		}	
 		catch(Exception e)
 		{
 			cachedProperties = null;
 			logger.error("Error loading properties from file " + "/" + applicationName + ".properties" + ". Reason:" + e.getMessage());
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * This method gets the local server node id if available.
 	 */
 
 	public static void initializeLocalServerNodeId()
 	{
         try
 	    {
 	        List serverNodeVOList = ServerNodeController.getController().getServerNodeVOList();
 	        Iterator serverNodeVOListIterator = serverNodeVOList.iterator();
 	        while(serverNodeVOListIterator.hasNext())
 	        {
 	            ServerNodeVO serverNodeVO = (ServerNodeVO)serverNodeVOListIterator.next();
 	            if(serverNodeVO.getName().equalsIgnoreCase(serverNodeName))
 	            {
 	                localSettingsServerNodeId = serverNodeVO.getId().toString();
 	                break;
 	            }
 	        }
 	    }
 	    catch(Exception e)
 	    {
 	        logger.warn("An error occurred trying to get localSettingsServerNodeId: " + e.getMessage(), e);
 	    }
 	    
 	    System.out.println("localSettingsServerNodeId:" + localSettingsServerNodeId);
 	}
 	
 	/**
 	 * This method returns all properties .
 	 */
 
 	public static Properties getProperties()
 	{
 		if(cachedProperties == null)
 			initializeProperties();
 				
 		return cachedProperties;
 	}	
 
  
 	/**
 	 * This method returns a propertyValue corresponding to the key supplied.
 	 */
 
 	public static String getProperty(String key)
 	{
 		String value;
 		if(cachedProperties == null)
 			initializeProperties();
 		
 		value = cachedProperties.getProperty(key);
 		if (value != null)
 			value = value.trim();
 				
 		return value;
 	}	
 
 
 	/**
 	 * This method sets a property during runtime.
 	 */
 
 	public static void setProperty(String key, String value)
 	{
 		if(cachedProperties == null)
 			initializeProperties();
 		
 		cachedProperties.setProperty(key, value);
 		
 		CacheController.clearCache("serverNodePropertiesCache");
 	}	
 
 	public static String getServerNodeProperty(String key, boolean inherit)
 	{
 		return getServerNodeProperty(null, key, inherit, null);
 	}
 
 	public static String getServerNodeProperty(String key, boolean inherit, String defaultValue)
 	{
 		return getServerNodeProperty(null, key, inherit, defaultValue);
 	}
 
 	/**
 	 * This method gets the serverNodeProperty but also fallbacks to the old propertyfile if not found in the propertyset.
 	 * 
 	 * @param key
 	 * @param inherit
 	 * @return
 	 */
 	
 	public static String getServerNodeProperty(String prefix, String key, boolean inherit, String defaultValue)
 	{
 	    String value = null;
 	    
         String cacheKey = "" + prefix + "_" + key + "_" + inherit;
         String cacheName = "serverNodePropertiesCache";
 		logger.info("cacheKey:" + cacheKey);
 		value = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(value != null)
 		{
 			return value;
 		}
 	    
 		Timer timer = new Timer();
 		logger.info("Getting jdbc-property:" + cacheKey);
 	    if(localSettingsServerNodeId != null)
 	    {
 	    	if(prefix != null)
 	    	{
 		        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + prefix + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 			        //System.out.println("Global value: " + value);
 			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 			        {
 			            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				        //System.out.println("Global value: " + value);
 			        }
 
 		        }
 	    	}
 	    	else
 	    	{
 		        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 			        //System.out.println("Global value: " + value);
 		        }	    		
 	    	}
 	    }
 		else
 		{
 			if(prefix != null)
 	    	{
 				value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 				//System.out.println("Global value immediately: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 			        //System.out.println("Global value: " + value);
 		        }	    		
 	    	}
 			else
 			{
 				value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				//System.out.println("Global value immediately: " + value);				
 			}
 	    }
 	    
 	    if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 	    {
 	        value = getProperty(key);
 	        //System.out.println("Property value: " + value);
 	    }
 
 	    //try{throw new Exception("APA");}catch(Exception e){e.printStackTrace();}
 
 	    if((value == null || value.indexOf(key) > -1) && defaultValue != null)
 	    	value = defaultValue;
 	    
 	    CacheController.cacheObject(cacheName, cacheKey, value);
 	    
 	    logger.info("Getting property " + cacheKey + " took:" + timer.getElapsedTime());
 	    
 	    return value;
 	}
 
 	
 	/**
 	 * This method gets the serverNodeDataProperty.
 	 * 
 	 * @param key
 	 * @param inherit
 	 * @return
 	 */
 	
 	public static String getServerNodeDataProperty(String prefix, String key, boolean inherit, String defaultValue)
 	{
 	    String value = null;
 	    
         String cacheKey = "" + prefix + "_" + key + "_" + inherit;
         String cacheName = "serverNodePropertiesCache";
 		logger.info("cacheKey:" + cacheKey);
 		value = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(value != null)
 		{
 			return value;
 		}
 	    
 		Timer timer = new Timer();
 
 		logger.info("Getting jdbc-property:" + cacheKey);
 	    if(localSettingsServerNodeId != null)
 	    {
 	    	if(prefix != null)
 	    	{
 		        value = getDataPropertyValue("serverNode_" + localSettingsServerNodeId + "_" + prefix + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 			        //System.out.println("Global value: " + value);
 			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 			        {
 			            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				        //System.out.println("Global value: " + value);
 			        }
 
 		        }
 	    	}
 	    	else
 	    	{
 		        value = getDataPropertyValue("serverNode_" + localSettingsServerNodeId + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
 			        //System.out.println("Global value: " + value);
 		        }	    		
 	    	}
 	    }
 		else
 		{
 			if(prefix != null)
 	    	{
 				value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 				//System.out.println("Global value immediately: " + value);
 	    	}
 			else
 			{
 				value = getDataPropertyValue("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				//System.out.println("Global value immediately: " + value);				
 			}
 	    }
 	    
 	    if(value == null && defaultValue != null)
 	    	value = defaultValue;
 	    
 	    CacheController.cacheObject(cacheName, cacheKey, value);
 	    
 	    logger.info("Getting property " + cacheKey + " took:" + timer.getElapsedTime());
 	    
 	    return value;
 	}
 	
 	public static String getDataPropertyValue(String fullKey)
 	{
 		String result = null;
 		
 		try
 		{
 			byte[] valueBytes = propertySet.getData(fullKey);
 	    
 			result = (valueBytes != null ? new String(valueBytes, "utf-8") : "");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	public static String getContextRootPath()
 	{
 	    return contextRootPath; //getProperty("contextRootPath"); Concurrency issues...
 	}
 
 	public static String getOperatingMode()
 	{
		if(operatingMode == null)
			return getProperty("operatingMode");
		else
			return operatingMode; //getProperty("operatingMode"); Concurrency issues...
 	}
 	
 	//TODO - refresh if changed....
 	//private static String inputCharacterEncoding = null;
 	public static String getInputCharacterEncoding(String defaultEncoding)
 	{
 		//if(inputCharacterEncoding == null)
 		//{
 			String applicationName = CmsPropertyHandler.getApplicationName();
 			String newInputCharacterEncoding = CmsPropertyHandler.getServerNodeProperty("inputCharacterEncoding", true, defaultEncoding);
 			if(!applicationName.equalsIgnoreCase("cms"))
 				newInputCharacterEncoding = CmsPropertyHandler.getServerNodeProperty("deliver", "inputCharacterEncoding", true, defaultEncoding);
 
 			//inputCharacterEncoding = newInputCharacterEncoding;
 			//}
 			return newInputCharacterEncoding;
 		//return inputCharacterEncoding;
 	}
 	
 	public static String getUp2dateUrl()
 	{
 	    return getProperty("up2dateUrl");
 	}
 
 	public static String getURLComposerClass()
 	{
 	    return getProperty("URLComposerClass");
 	}
 
 	public static String getMaxClients()
 	{
 	    return getProperty("maxClients");
 	}
 
 	public static String getAdministratorUserName()
 	{
 	    return getProperty("administratorUserName");
 	}
 
 	public static String getAdministratorPassword()
 	{
 	    return getProperty("administratorPassword");
 	}
 
 	public static String getAdministratorEmail()
 	{
 	    return getProperty("administratorEmail");
 	}
 
 	public static String getDbRelease()
 	{
 	    return getProperty("dbRelease");
 	}
 
 	public static String getDbUser()
 	{
 	    return getProperty("dbUser");
 	}
 
 	public static String getDbPassword()
 	{
 	    return getProperty("dbPassword");
 	}
 
 	public static String getMasterServer()
 	{
 	    return getProperty("masterServer");
 	}
 	public static String getSlaveServer()
 	{
 	    return getProperty("slaveServer");
 	}
 
 	public static String getBuildName()
 	{
 	    return getProperty("buildName");
 	}
 	public static String getAdminToolsPath()
 	{
 	    return getProperty("adminToolsPath");
 	}
 	public static String getDbScriptPath()
 	{
 	    return getProperty("dbScriptPath");
 	}
 
 	public static String getDigitalAssetUploadPath()
 	{
 	    return getServerNodeProperty("digitalAssetUploadPath", true);
 	}
 	
 	public static String getExtranetCookieTimeout()
 	{
 	    return getServerNodeProperty("extranetCookieTimeout", true, "1800");
 	}
 
 	public static String getWebServicesBaseUrl()
 	{
 	    return getServerNodeProperty("webServicesBaseUrl", true);
 	}
 
 	public static String getPublicationThreadDelay()
 	{
 	    return getServerNodeProperty("publicationThreadDelay", true, "5000");
 	}
 
 	public static String getPathsToRecacheOnPublishing()
 	{
 	    return getServerNodeProperty("pathsToRecacheOnPublishing", true);
 	}
 
 	public static String getDisableTemplateDebug()
 	{
 	    return getServerNodeProperty("disableTemplateDebug", true, "false");
 	}
 
 	public static String getTree()
 	{
 	    return getServerNodeProperty("tree", true, "html");
 	}
 
 	public static String getTreeMode()
 	{
 	    return getServerNodeProperty("treemode", true, "dynamic");
 	}
 
 	public static String getPreviewDeliveryUrl()
 	{
 	    return getServerNodeProperty("previewDeliveryUrl", true);
 	}
 
 	public static String getStagingDeliveryUrl()
 	{
 	    return getServerNodeProperty("stagingDeliveryUrl", true);
 	}
 
 	public static String getEditionPageSize()
 	{
 		return getServerNodeProperty("edition.pageSize", true, "10");
 	}
 	
 	public static String getContentTreeSort()
 	{
 		return getServerNodeProperty("content.tree.sort", true, "name");
 	}
 
 	public static String getStructureTreeSort()
 	{
 		return getServerNodeProperty("structure.tree.sort", true, "name");
 	}
 
 	public static String getStructureTreeIsHiddenProperty()
 	{
 		return getServerNodeProperty("structure.tree.isHidden", true);
 	}
 
 	public static String getDisableEmptyUrls()
 	{
 		return getServerNodeProperty("disableEmptyUrls", true, "yes");
 	}
 
 	public static String getCacheUpdateAction()
 	{
 		return getServerNodeProperty("cacheUpdateAction", true, "UpdateCache.action");
 	}
 
 	public static String getLogPath()
 	{
 		return getServerNodeProperty("logPath", true);
 	}
 
 	public static String getLogTransactions()
 	{
 		return getServerNodeProperty("logTransactions", true, "false");
 	}
 	
 	public static String getEnableExtranetCookies()
 	{
 		return getServerNodeProperty("enableExtranetCookies", true, "false");
 	}
 	
 	public static String getUseAlternativeBrowserLanguageCheck()
 	{
 		return getServerNodeProperty("useAlternativeBrowserLanguageCheck", true, "false");
 	}
 	
 	public static String getCaseSensitiveRedirects()
 	{
 		return getServerNodeProperty("caseSensitiveRedirects", true, "false");
 	}
 	
 	public static String getUseDNSNameInURI()
 	{
 		return getServerNodeProperty("useDNSNameInURI", true, "false");
 	}
     
 	public static String getWysiwygEditor()
 	{
 		return getServerNodeProperty("wysiwygEditor", true, "FCKEditor");
 	}
 
 	public static String getProtectContentTypes()
 	{
 		return getServerNodeProperty("protectContentTypes", true, "false");
 	}
 
 	public static String getProtectWorkflows()
 	{
 		return getServerNodeProperty("protectWorkflows", true, "false");
 	}
 
 	public static String getProtectCategories()
 	{
 		return getServerNodeProperty("protectCategories", true, "false");
 	}
 
 	public static String getMaxRows()
 	{
 		return getServerNodeProperty("maxRows", true, "100");
 	}
 	
 	public static String getShowContentVersionFirst()
 	{
 		return getServerNodeProperty("showContentVersionFirst", true, "true");
 	}
 	
 	public static String getShowComponentsFirst()
 	{
 		return getServerNodeProperty("showComponentsFirst", true, "true");
 	}
 	
 	public static String getShowAllWorkflows()
 	{
 		return getServerNodeProperty("showAllWorkflows", true, "true");
 	}
 	
 	public static String getIsPageCacheOn()
 	{
 	    return getServerNodeProperty("isPageCacheOn", true, "true");
 	}
 
 	public static String getEditOnSite()
 	{
 	    return getServerNodeProperty("editOnSite", true, "true");
 	}
 
 	public static String getUseSelectivePageCacheUpdate()
 	{
 	    return getServerNodeProperty("useSelectivePageCacheUpdate", true, "true");
 	}
 
 	public static String getExpireCacheAutomatically()
 	{
 	    return getServerNodeProperty("expireCacheAutomatically", true, "false");
 	}
 
 	public static String getCacheExpireInterval()
 	{
 	    return getServerNodeProperty("cacheExpireInterval", true, "1800");
 	}
 
 	public static String getSessionTimeout()
 	{
 	    return getServerNodeProperty("session.timeout", true, "1800");
 	}
 
 	public static String getCompressPageCache()
 	{
 	    return getServerNodeProperty("compressPageCache", true, "true");
 	}
 
 	public static String getCompressPageResponse()
 	{
 	    return getServerNodeProperty("compressPageResponse", true, "false");
 	}
 
 	public static String getSiteNodesToRecacheOnPublishing()
 	{
 	    return getServerNodeProperty("siteNodesToRecacheOnPublishing", true);
 	}
 
 	public static String getRecachePublishingMethod()
 	{
 	    return getServerNodeProperty("recachePublishingMethod", true);
 	}
 
 	public static String getRecacheUrl()
 	{
 	    return getServerNodeProperty("recacheUrl", true);
 	}
 
 	public static String getUseUpdateSecurity()
 	{
 	    return getServerNodeProperty("useUpdateSecurity", true, "true");
 	}
 	
 	public static String getAllowedAdminIP()
 	{
 	    return getServerNodeProperty("allowedAdminIP", true);
 	}
 
 	public static String getPageKey()
 	{
 	    return getServerNodeProperty("pageKey", true);
 	}
 	
 	public static String getCmsBaseUrl()
 	{
 	    return getServerNodeProperty("cmsBaseUrl", true);
 	}
 
 	public static String getComponentEditorUrl()
 	{
 	    return getServerNodeProperty("componentEditorUrl", true);
 	}
 
 	public static String getComponentRendererUrl()
 	{
 	    return getServerNodeProperty("componentRendererUrl", true);
 	}
 
 	public static String getComponentRendererAction()
 	{
 	    return getServerNodeProperty("componentRendererAction", true);
 	}
 
 	public static String getEditOnSiteUrl()
 	{
 	    return getServerNodeProperty("editOnSiteUrl", true);
 	}
 
 	public static String getUseFreeMarker()
 	{
 	    return getServerNodeProperty("useFreeMarker", true, "false");
 	}
 
 	public static String getWebServerAddress()
 	{
 	    return getServerNodeProperty("webServerAddress", true);
 	}
 
 	public static String getApplicationBaseAction()
 	{
 	    return getServerNodeProperty("applicationBaseAction", true);
 	}
 
 	public static String getDigitalAssetBaseUrl()
 	{
 	    return getServerNodeProperty("digitalAssetBaseUrl", true);
 	}
 
 	public static String getImagesBaseUrl()
 	{
 	    return getServerNodeProperty("imagesBaseUrl", true);
 	}
 
 	public static String getDigitalAssetPath()
 	{
 	    return getServerNodeProperty("digitalAssetPath", true);
 	}
 
 	public static String getEnableNiceURI()
 	{
 	    return getServerNodeProperty("enableNiceURI", true, "true");
 	}
 
 	public static String getNiceURIEncoding()
 	{
 	    return getServerNodeProperty("niceURIEncoding", true, "UTF-8");
 	}
 
 	public static String getNiceURIAttributeName()
 	{
 	    return getServerNodeProperty("niceURIAttributeName", true, "NiceURIName");
 	}
 
 	public static String getRequestArgumentDelimiter()
 	{
 	    return getServerNodeProperty("requestArgumentDelimiter", true, "&amp;");
 	}
 
 	public static String getErrorUrl()
 	{
 	    return getServerNodeProperty("errorUrl", true);
 	}
 
 	public static String getErrorBusyUrl()
 	{
 	    return getServerNodeProperty("errorBusyUrl", true);
 	}
 
 	public static String getExternalThumbnailGeneration()
 	{
 	    return getServerNodeProperty("externalThumbnailGeneration", true);
 	}
 
 	public static String getURIEncoding()
 	{
 	    return getServerNodeProperty("URIEncoding", true, "UTF-8");
 	}
 
 	public static String getWorkflowEncoding()
 	{
 	    return getServerNodeProperty("workflowEncoding", true, "UTF-8");
 	}
 
 	public static String getFormsEncoding()
 	{
 	    return getServerNodeProperty("formsEncoding", true, "UTF-8");
 	}
 
 	public static String getUseShortTableNames()
 	{
 	    return getServerNodeProperty("useShortTableNames", true, "false");
 	}
 
 	public static String getLogDatabaseMessages()
 	{
 	    return getServerNodeProperty("logDatabaseMessages", true, "false");
 	}
 
 	public static String getStatisticsEnabled()
 	{
 	    return getServerNodeProperty("statistics.enabled", true, "false");
 	}
 
 	public static String getStatisticsLogPath()
 	{
 	    return getServerNodeProperty("statisticsLogPath", true);
 	}
 
 	public static String getStatisticsLogOneFilePerDay()
 	{
 	    return getServerNodeProperty("statisticsLogOneFilePerDay", true, "false");
 	}
 
 	public static String getStatisticsLogger()
 	{
 	    return getServerNodeProperty("statisticsLogger", true, "W3CExtendedLogger");
 	}
 
 	public static String getEnablePortal()
 	{
 	    return getServerNodeProperty("enablePortal", true, "true");
 	}
 
 	public static String getPortletBase()
 	{
 	    return getServerNodeProperty("portletBase", true);
 	}
 
 	public static String getMailSmtpHost()
 	{
 	    return getServerNodeProperty("mail.smtp.host", true);
 	}
 
 	public static String getMailSmtpAuth()
 	{
 	    return getServerNodeProperty("mail.smtp.auth", true);
 	}
 
 	public static String getMailSmtpUser()
 	{
 	    return getServerNodeProperty("mail.smtp.user", true);
 	}
 
 	public static String getMailSmtpPassword()
 	{
 	    return getServerNodeProperty("mail.smtp.password", true);
 	}
 
 	public static String getMailContentType()
 	{
 	    return getServerNodeProperty("mail.contentType", true, "text/html");
 	}
 	
 	public static String getSystemEmailSender()
 	{
 	    return getServerNodeProperty("systemEmailSender", true);
 	}
 
 	public static String getWarningEmailReceiver()
 	{
 	    return getServerNodeProperty("warningEmailReceiver", true);
 	}
 
 	public static String getExportFormat()
 	{
 	    return getServerNodeProperty("exportFormat", true, "1");
 	}
 
 	public static String getHelpUrl()
 	{
 	    return getServerNodeProperty("helpUrl", true);
 	}
 
 	public static String getPreferredLanguageCode(String userName)
 	{
         return propertySet.getString("principal_" + userName + "_languageCode");
 	}
 
 	public static String getPreferredToolId(String userName)
 	{
 	    return propertySet.getString("principal_" + userName + "_defaultToolId");
 	}
 
 	public static List getInternalDeliveryUrls()
 	{
 		List urls = new ArrayList();
 		
 	    String internalDeliverUrlsString = CmsPropertyHandler.getServerNodeDataProperty(null, "internalDeliveryUrls", true, null);
 	    //System.out.println("internalDeliverUrlsString:" + internalDeliverUrlsString);
 	    if(internalDeliverUrlsString != null && !internalDeliverUrlsString.equals(""))
 		{
 	    	try
 			{
 	    		Properties properties = new Properties();
 				properties.load(new ByteArrayInputStream(internalDeliverUrlsString.getBytes("UTF-8")));
 
 				int i = 0;
 				String deliverUrl = null;
 				while((deliverUrl = properties.getProperty("" + i)) != null)
 				{ 
 					urls.add(deliverUrl);
 					i++;
 				}	
 
 			}	
 			catch(Exception e)
 			{
 			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	    else
 	    {
 			int i = 0;
 			String deliverUrl = null;
 			while((deliverUrl = CmsPropertyHandler.getProperty("internalDeliverUrl." + i)) != null)
 			{ 
 				urls.add(deliverUrl);
 				i++;
 			}	
 	    }
 
 	    return urls;
 	}
 
 	public static List getPublicDeliveryUrls()
 	{
 		List urls = new ArrayList();
 		
 	    String publicDeliverUrlString = CmsPropertyHandler.getServerNodeDataProperty(null, "publicDeliveryUrls", true, null);
 	    //System.out.println("publicDeliverUrlString:" + publicDeliverUrlString);
 	    if(publicDeliverUrlString != null && !publicDeliverUrlString.equals(""))
 		{
 	    	try
 			{
 	    		Properties properties = new Properties();
 				properties.load(new ByteArrayInputStream(publicDeliverUrlString.getBytes("UTF-8")));
 
 				int i = 0;
 				String deliverUrl = null;
 				while((deliverUrl = properties.getProperty("" + i)) != null)
 				{ 
 					urls.add(deliverUrl);
 					i++;
 				}	
 
 			}	
 			catch(Exception e)
 			{
 			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	    else
 	    {
 			int i = 0;
 			String deliverUrl = null;
 			while((deliverUrl = CmsPropertyHandler.getProperty("publicDeliverUrl." + i)) != null)
 			{ 
 				urls.add(deliverUrl);
 				i++;
 			}	
 	    }
 	    
 	    return urls;
 	}
 	
 	public static String getPropertySetValue(String key)
 	{
 	    String value = null;
 	    
         String cacheKey = "" + key;
         String cacheName = "propertySetCache";
 		//logger.info("cacheKey:" + cacheKey);
 		value = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(value != null)
 		{
 		    logger.info("Returning property " + cacheKey + " value " + value);
 			return value;
 		}
 	    
 		value = propertySet.getString(key);
 		logger.info("propertySetCache did not have value... refetched:" + value);
 	    
 	    CacheController.cacheObject(cacheName, cacheKey, value);
 	    
 	    return value;
 	}
 
 	public static String getAnonymousPassword()
 	{
 		String password = "anonymous";
 		//String specifiedPassword = getProperty("security.anonymous.password");
 		String specifiedPassword = getServerNodeProperty("deliver", "security.anonymous.password", true, "anonymous");
 		if(specifiedPassword != null && !specifiedPassword.equalsIgnoreCase("") && specifiedPassword.indexOf("security.anonymous.password") == -1)
 			password = specifiedPassword;
 		
 		//System.out.println("password:" + password);
 		
 		return password;
 	}
 
 	public static String getAnonymousUser()
 	{
 		String userName = "anonymous";
 		//String specifiedUserName = getProperty("security.anonymous.username");
 		String specifiedUserName = getServerNodeProperty("deliver", "security.anonymous.username", true, "anonymous");
 		if(specifiedUserName != null && !specifiedUserName.equalsIgnoreCase("") && specifiedUserName.indexOf("security.anonymous.username") == -1)
 			userName = specifiedUserName;
 		
 		//System.out.println("userName:" + userName);
 		
 		return userName;
 	}
 
 }
