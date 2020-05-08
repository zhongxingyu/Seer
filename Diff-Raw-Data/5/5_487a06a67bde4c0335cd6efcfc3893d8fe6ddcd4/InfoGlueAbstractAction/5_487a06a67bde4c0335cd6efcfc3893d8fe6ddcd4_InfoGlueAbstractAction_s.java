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
 
 package org.infoglue.cms.applications.common.actions;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.AuthenticationModule;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
 import org.infoglue.deliver.util.CacheController;
 
 import webwork.action.ActionContext;
 
 /**
  * @author Mattias Bogeblad
  *
  * This is an abstract action used for all InfoGlue actions. Just to not have to put to much in the WebworkAbstractAction.
  */
 
 public abstract class InfoGlueAbstractAction extends WebworkAbstractAction
 {
     protected String colorScheme = null; 
     
 	/**
 	 * This method lets the velocity template get hold of all actions inheriting.
 	 * 
 	 * @return The action object currently invoked 
 	 */
 	
 	public InfoGlueAbstractAction getThis()
 	{
 		return this;
 	}
 	
     /**
      * Gets a logger for the action class.
      */
 
 	protected Logger getLogger() 
 	{
 	    return Logger.getLogger(this.getClass().getName());
 	}
 
 	/**
 	 * This method returns the logout url.
 	 * @author Mattias Bogeblad
 	 */
 	
 	public String getLogoutURL() throws Exception
 	{
 		AuthenticationModule authenticationModule = AuthenticationModule.getAuthenticationModule(null, null);
 	    return authenticationModule.getLogoutUrl();
 	}
 
 
 	/**
 	 * This method returns the actions url base.
 	 * @author Mattias Bogeblad
 	 */
 	
 	public String getURLBase()
 	{
 	    return this.getRequest().getContextPath();
 	}
 
 	/**
 	 * This method returns the current url.
 	 * @author Mattias Bogeblad
 	 */
 	
 	public String getCurrentURL()
 	{
		return this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString();
 	}
 
 	public String getOriginalFullURL()
 	{
     	String originalRequestURL = this.getRequest().getParameter("originalRequestURL");
     	if(originalRequestURL == null || originalRequestURL.length() == 0)
     		originalRequestURL = this.getRequest().getRequestURL().toString();
 
     	String originalQueryString = this.getRequest().getParameter("originalQueryString");
     	if(originalQueryString == null || originalQueryString.length() == 0)
     		originalQueryString = this.getRequest().getQueryString();
 
    	return originalRequestURL + "?" + originalQueryString;
 	}
 
 	/**
 	 * This method returns the session timeout value.
 	 */
 	
 	public int getSessionTimeout()
 	{
 	    return this.getHttpSession().getMaxInactiveInterval();
 	}
 
 	/**
 	 * Gets a list of tool languages
 	 */
 
 	public List getToolLocales()
 	{
 	    List toolLocales = new ArrayList();
 	    
 	    int index = 0;
 	    String languageCode = CmsPropertyHandler.getProperty(index + ".toolLanguageCode");
 	    while(languageCode != null)
 	    {
 	        Locale locale = new java.util.Locale(languageCode);
 	        if(locale != null)
 	            toolLocales.add(locale);
 	        
 	        index++;
 	        languageCode = CmsPropertyHandler.getProperty(index + ".toolLanguageCode");
 	    }
 	    
 	    return toolLocales;
 	}
 
 	
 	/**
 	 * This method returns a propertyValue for the logged in user.
 	 * 
 	 * @author Mattias Bogeblad
 	 */
 	
 	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters)
 	{		
 		return getPrincipalPropertyValue(propertyName, escapeSpecialCharacters, false);
 	}
 
 	/**
 	 * This method returns a propertyValue for the logged in user.
 	 * 
 	 * @author Mattias Bogeblad
 	 */
 	
 	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue)
 	{
 		getLogger().info("propertyName: " + propertyName);
 		getLogger().info("escapeSpecialCharacters: " + escapeSpecialCharacters);
 		getLogger().info("findLargestValue: " + findLargestValue);
 	    
 		String value = "";
 		
 		try
 		{
 		    InfoGluePrincipal infoGluePrincipal = this.getInfoGluePrincipal();
 		    LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
 		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(infoGluePrincipal, propertyName, languageVO.getId(), null, false, escapeSpecialCharacters, findLargestValue);
 		}
 		catch(Exception e)
 		{
 		    getLogger().warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
 		}
 		
 		return value;
 	}
 
 	/**
 	 * Getting a property for a Principal - used for personalisation. 
 	 * This method starts with getting the property on the user and if it does not exist we check out the
 	 * group-properties as well.
 	 */
 	
 	public Map getPrincipalPropertyHashValues(String propertyName, boolean escapeSpecialCharacters)
 	{
 		Map value = new HashMap();
 		
 		try
 		{
 		    InfoGluePrincipal infoGluePrincipal = this.getInfoGluePrincipal();
 		    LanguageVO languageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
 			value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyHashValues(infoGluePrincipal, propertyName, languageVO.getId(), null, false, escapeSpecialCharacters);
 		}
 		catch(Exception e)
 		{
 		    getLogger().warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
 		}
 		
 		return value;
 	}	
 
 	public Principal getAnonymousPrincipal() throws SystemException
 	{
 	    Principal principal = null;
 		try
 		{
 			principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
 			if(principal == null)
 			{
 			    Map arguments = new HashMap();
 			    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
 			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
 			    arguments.put("ticket", this.getHttpSession().getAttribute("ticket"));
 
 			    principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 				
 				if(principal != null)
 					CacheController.cacheObject("userCache", "anonymous", principal);
 			}			
 		}
 		catch(Exception e) 
 		{
 		    getLogger().warn("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
 		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
 		}
 
 		return principal;
 	}
 	
 	/**
 	 * Used by the view pages to determine if the current user has sufficient access rights
 	 * to perform the action specific by the interception point name.
 	 *
 	 * @param interceptionPointName THe Name of the interception point to check access rights
 	 * @return True is access is allowed, false otherwise
 	 */
 	public boolean hasAccessTo(String interceptionPointName)
 	{
 		getLogger().info("Checking if " + getUserName() + " has access to " + interceptionPointName);
 
 		try
 		{
 			return AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), interceptionPointName);
 		}
 		catch (SystemException e)
 		{
 		    getLogger().warn("Error checking access rights", e);
 			return false;
 		}
 	}
 
 	/**
 	 * Used by the view pages to determine if the current user has sufficient access rights
 	 * to perform the action specific by the interception point name.
 	 *
 	 * @param interceptionPointName THe Name of the interception point to check access rights
 	 * @return True is access is allowed, false otherwise
 	 */
 	public boolean hasAccessTo(String interceptionPointName, String extraParameter)
 	{
 		getLogger().info("Checking if " + getUserName() + " has access to " + interceptionPointName + " with extraParameter " + extraParameter);
 
 		try
 		{
 		    return AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), interceptionPointName, extraParameter);
 		}
 		catch (SystemException e)
 		{
 		    getLogger().warn("Error checking access rights", e);
 			return false;
 		}
 	}
 
 	/**
 	 * Get the username for the currently logged in user
 	 */
 	public String getUserName()
 	{
 		return getInfoGluePrincipal().getName();
 	}
 
 	/**
 	 * Get a single parameter from the ActionContext (hides Servlet implementation)
 	 */
 	protected final String getSingleParameter(String parameterName)
 	{
 		return (String) ActionContext.getSingleValueParameters().get(parameterName);
 	}
 
 	/**
 	 * Get a parameter (could possibly be an array) from the ActionContext (hides Servlet implementation)
 	 */
 	protected final String getParameter(String parameterName)
 	{
 		return (String) ActionContext.getParameters().get(parameterName);
 	}
 	
     public String getColorScheme()
     {
         return colorScheme;
     }
     
     public void setColorScheme(String colorScheme)
     {
         this.colorScheme = colorScheme;
     }
 
     public String encode(String value)
     {
         return this.getResponse().encodeUrl(value);
     }
 
     public String getComponentRendererUrl()
     {
         return CmsPropertyHandler.getComponentRendererUrl();
     }
     
     public String getComponentRendererAction()
     {
         return CmsPropertyHandler.getComponentRendererAction();
     }
     
     public String getCMSBaseUrl()
     {
         return CmsPropertyHandler.getCmsBaseUrl();
     }
     
     public Locale getLocale()
     {
         return this.getSession().getLocale();
     }
     
     public Integer getToolId()
     {
         return this.getSession().getToolId();
     }
 
     public String getLanguageCode()
     {
         return this.getSession().getLocale().getLanguage();
     }
     
 	public void setLanguageCode(String languageCode)
 	{
 		this.getSession().setLocale(new java.util.Locale(languageCode));
 	}
 
 	public void setToolId(Integer toolId)
 	{
 		this.getSession().setToolId(toolId);
 	}
 
 	//--------------------------------------------------------------------------
 	// Database/Transaction specific operations
 	//--------------------------------------------------------------------------
 
 	/**
 	 * Begins a transaction on the supplied database
 	 */
 	
 	public void beginTransaction(Database db) throws SystemException
 	{
 		try
 		{
 			db.begin();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
 		}
 	}
        
 	/**
 	 * Rollbacks a transaction on the named database
 	 */
      
 	public void closeTransaction(Database db) throws SystemException
 	{
 	    //if(db != null && !db.isClosed() && db.isActive())
 	        //commitTransaction(db);
 	        rollbackTransaction(db);
 	}
 
 	
 	/**
 	 * Ends a transaction on the named database
 	 */
 	
     public void commitTransaction(Database db) throws SystemException
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
 		    throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
 		}
 		finally
 		{
 		    closeDatabase(db);
 		}
 	}
 	
  
 	/**
 	 * Rollbacks a transaction on the named database
 	 */
      
 	public void rollbackTransaction(Database db) throws SystemException
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
 			getLogger().warn("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
 		}
 		finally
 		{
 		    closeDatabase(db);
 		}
 	}
 
 	/**
 	 * Close the database
 	 */
      
 	public void closeDatabase(Database db) throws SystemException
 	{
 		try
 		{
 			db.close();
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("An error occurred when we close database. Reason:" + e.getMessage());
 			throw new SystemException("An error occurred when we tried to close a database. Reason:" + e.getMessage(), e);    
 		}
 	}
 }
 
