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
 
 package org.infoglue.deliver.applications.actions;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.filters.ViewPageFilter;
 import org.infoglue.deliver.controllers.kernel.impl.simple.*;
 import org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker;
 import org.infoglue.deliver.invokers.DecoratedComponentBasedHTMLPageInvoker;
 import org.infoglue.deliver.invokers.HTMLPageInvoker;
 import org.infoglue.deliver.invokers.PageInvoker;
 import org.infoglue.deliver.portal.PortalService;
 import org.infoglue.deliver.services.StatisticsService;
 import org.infoglue.deliver.util.BrowserBean;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.RequestAnalyser;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.*;
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 
 import java.net.URLEncoder;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * This is the main delivery action. Gets called when the user clicks on a link that goes inside the site.
  *
  * @author Mattias Bogeblad
  */
 
 public class ViewPageAction extends InfoGlueAbstractAction 
 {
     public final static Logger logger = Logger.getLogger(ViewPageAction.class.getName());
 
 	//These are the standard parameters which uniquely defines which page to show.
 	private Integer siteNodeId = null;
 	private Integer contentId  = null; 
 	private Integer languageId = null;
 	
 	private boolean showSimple = false;
 	
 	//This parameter are set if you want to access a certain repository startpage
 	private String repositoryName = null;
 	
 	//A cached nodeDeliveryController
 	protected NodeDeliveryController nodeDeliveryController					= null;
 	protected IntegrationDeliveryController integrationDeliveryController 	= null;
 	protected TemplateController templateController 						= null;
 		
 	private static final boolean USE_LANGUAGE_FALLBACK        			= true;
 	private static final boolean DO_NOT_USE_LANGUAGE_FALLBACK 			= false;
 	
 	//The browserbean
 	private BrowserBean browserBean = null;
 	private Principal principal = null;
 		
 	//A possibility to set the referer address
 	private String referer = null;
 
 	//For statistics only and debug
 	public static long contentVersionTime = 0;
 	public static long serviceBindingTime = 0;
 	public static long contentAttributeTime = 0;
 	public static long boundContentTime = 0;
 	public static long inheritedServiceBindingTime = 0;
 	public static long selectMatchingEntitiesTime = 0;
 	public static long isValidTime = 0;
 	public static long qualifyersTime = 0;
 	public static long sortQualifyersTime = 0;
 	public static long commitTime = 0;
 	public static long rollbackTime = 0;
 	public static long closeTime = 0;
 	
 
 	
 	/**
 	 * The constructor for this action - contains nothing right now.
 	 */
     
     public ViewPageAction() 
     {
     }
     
     /**
      * This method is the application entry-point. The parameters has been set through the setters
      * and now we just have to render the appropriate output. 
      */
          
     public String doExecute() throws Exception
     {
         //CacheController.evictWaitingCache();
         if(RequestAnalyser.getBlockRequests() ||
            (RequestAnalyser.getMaxClients() != 0 && 
            RequestAnalyser.getNumberOfCurrentRequests() > RequestAnalyser.getMaxClients()))
         {
             logger.info("Maximum number of clients reached in ViewPage. Responding with an error.");
 			getResponse().setContentType("text/html; charset=UTF-8");
 			getRequest().setAttribute("responseCode", "503");
 			getRequest().getRequestDispatcher("/ErrorPage!busy.action").forward(getRequest(), getResponse());
 
             return NONE;
         }
         getLogger().warn("viewpage let through");
 
         
         HttpServletRequest request = getRequest();
         
     	long start = System.currentTimeMillis();
     	synchronized(RequestAnalyser.getCurrentRequests())
     	{
     	    request.setAttribute("startTime", new Long(start));
     	    RequestAnalyser.getCurrentRequests().add(request);
     	}
     	long elapsedTime 	= 0;
     	
     	getLogger().warn("************************************************");
     	getLogger().warn("* ViewPageAction was called....                *");
     	getLogger().warn("************************************************");
     	
         request.setAttribute("progress", "ViewPageAction has been called");
     	
     	DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
     	//Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(dbWrapper.getDatabase());
 
 		try
 		{
 	    	validateAndModifyInputParameters(dbWrapper.getDatabase());
 	    	
 	        request.setAttribute("progress", "validateAndModifyInputParameters done");
 
 	    	this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.templateController 			= getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, false);
 			
 	    	String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), this.templateController, this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");
 	    	//String pageKey = CacheController.getPageCacheKey(this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");
 
 	        request.setAttribute("progress", "pageKey found:" + pageKey);
 
 	    	getLogger().info("pageKey:" + pageKey);
 	    	String pagePath	= null;
 	    	
 	    	boolean isUserRedirected = false;
 			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(dbWrapper.getDatabase(), siteNodeId);
 			getLogger().info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 			if(protectedSiteNodeVersionId != null)
 				isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), protectedSiteNodeVersionId);
 		
 			getLogger().info("handled extranet users");
 	
 			// ----
 			// -- portlet
 			// ----
 			
 			// -- check if the portal is active
 	        String portalEnabled = CmsPropertyHandler.getProperty("enablePortal") ;
 	        boolean portalActive = ((portalEnabled != null) && portalEnabled.equals("true"));
 			
 	        if (portalActive) 
 	        {
 	            getLogger().info("---> Checking for portlet action");
 	            PortalService service = new PortalService();
 	            //TODO: catch PortalException?
 	            boolean actionExecuted = service.service(getRequest(), getResponse());
 	            
 	            // -- if an action was executed return NONE as a redirect is issued
 	            if (actionExecuted) 
 	            {
 	                getLogger().info("---> PortletAction was executed, returning NONE as a redirect has been issued");
 	                //TODO: maybe statistics service should run here
 	                getLogger().warn("No statistics have been run for this request");
 	                return NONE;
 	            }
 	        }
 	
 	        request.setAttribute("progress", "handled portal action");
 
 	        getLogger().info("handled portal action");
 			
 			if(!isUserRedirected)
 			{	
 				getLogger().info("this.templateController.getPrincipal():" + this.templateController.getPrincipal());
 				DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(/*(InfoGluePrincipal)this.principal*/);
 				deliveryContext.setRepositoryName(this.repositoryName);
 				deliveryContext.setSiteNodeId(this.siteNodeId);
 				deliveryContext.setContentId(this.contentId);
 				deliveryContext.setLanguageId(this.languageId);
 				deliveryContext.setPageKey(pageKey);
 				deliveryContext.setSession(this.getSession());
 				deliveryContext.setInfoGlueAbstractAction(this);
 				deliveryContext.setHttpServletRequest(this.getRequest());
 				deliveryContext.setHttpServletResponse(this.getResponse());
 				
 				SiteNode siteNode = nodeDeliveryController.getSiteNode(dbWrapper.getDatabase(), this.siteNodeId);
 				if(siteNode == null)
 				    throw new SystemException("There was no page with this id.");
 				
 				if(siteNode.getSiteNodeTypeDefinition() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName().equals(""))
 				{
 				    throw new SystemException("There was no page invoker class assigned to the site node " + siteNode.getName());
 				}
 				else
 				{
 				    try
 				    {
 				        request.setAttribute("progress", "before pageInvoker is called on siteNode:" + siteNode.getName());
 
 						String invokerClassName = siteNode.getSiteNodeTypeDefinition().getInvokerClassName();
 				        PageInvoker pageInvoker = (PageInvoker)Class.forName(invokerClassName).newInstance();
 				        pageInvoker.setParameters(dbWrapper, this.getRequest(), this.getResponse(), this.templateController, deliveryContext);
 				        pageInvoker.deliverPage();
 
 				        request.setAttribute("progress", "after pageInvoker was called");
 				    }
 				    catch(ClassNotFoundException e)
 				    {
 				        throw new SystemException("An error was thrown when trying to use the page invoker class assigned to this page type:" + e.getMessage(), e);
 				    }
 				}	
 			}
 				        
 	        StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(dbWrapper.getDatabase());
 
 			getResponse().setContentType("text/html; charset=UTF-8");
 			getRequest().setAttribute("responseCode", "500");
 			getRequest().setAttribute("error", e);
 			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
 
 			//throw new SystemException(e.getMessage());
 		}
 		finally
 		{
			closeTransaction(dbWrapper.getDatabase());

 			elapsedTime = System.currentTimeMillis() - start;
 			synchronized(RequestAnalyser.getCurrentRequests())
 	    	{
 			    RequestAnalyser.getCurrentRequests().remove(request);
 	    	}
 		}
 
 		
 		if(elapsedTime > 10000)
 		{
 		    getLogger().warn("The page delivery took " + elapsedTime + "ms for request " + this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString());
 		    getLogger().warn("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 		}
 		else
 		{
 		    getLogger().info("The page delivery took " + elapsedTime + "ms");			
 			getLogger().info("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 		}
 		
         return NONE;
     }
     
 
 
 	/**
 	 * This method the renderer for the component editor. 
 	 */
          
 	public String doRenderDecoratedPage() throws Exception
 	{
 		long start			= new Date().getTime();
 		long elapsedTime 	= 0;
     	
 		getLogger().info("************************************************");
 		getLogger().info("* ViewPageAction was called....                *");
 		getLogger().info("************************************************");
 		
 		DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
     	//Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(dbWrapper.getDatabase());
 
 		try
 		{
 			validateAndModifyInputParameters(dbWrapper.getDatabase());
 	    	
 			this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.templateController 			= getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, true);
 
 			//String pageKey  = "" + this.siteNodeId + "_" + this.languageId + "_" + this.contentId + "_" + browserBean.getUseragent() + "_" + getRequest().getQueryString() + "_" + this.showSimple + "_pagecomponentDecorated";
 			//String pageKey = CacheController.getPageCacheKey(this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "_" + this.showSimple + "_pagecomponentDecorated");
 	    	String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), this.templateController, this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "_" + this.showSimple + "_pagecomponentDecorated");
 
 			getLogger().info("A pageKey:" + pageKey);
 			String pagePath	= null;
 	    		
 			boolean isUserRedirected = false;
 			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(dbWrapper.getDatabase(), siteNodeId);
 			getLogger().info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 			if(protectedSiteNodeVersionId != null)
 				isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), protectedSiteNodeVersionId);
 		
 			getLogger().info("handled extranet users");
 	
 			if(!isUserRedirected)
 			{	
 				getLogger().info("this.templateController.getPrincipal():" + this.templateController.getPrincipal());
 		
 				DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(/*this.templateController.getPrincipal()*/);
 				deliveryContext.setRepositoryName(this.repositoryName);
 				deliveryContext.setSiteNodeId(this.siteNodeId);
 				deliveryContext.setLanguageId(this.languageId);
 				deliveryContext.setContentId(this.contentId);
 				deliveryContext.setShowSimple(this.showSimple);
 				deliveryContext.setPageKey(pageKey);
 				deliveryContext.setSession(this.getSession());
 				deliveryContext.setInfoGlueAbstractAction(this);
 				deliveryContext.setHttpServletRequest(this.getRequest());
 				deliveryContext.setHttpServletResponse(this.getResponse());
 	
 				SiteNode siteNode = nodeDeliveryController.getSiteNode(dbWrapper.getDatabase(), this.siteNodeId);
 				if(siteNode == null)
 				    throw new SystemException("There was no page with this id.");
 				
 				String invokerClassName = siteNode.getSiteNodeTypeDefinition().getInvokerClassName();
 				
 				if(invokerClassName == null && invokerClassName.equals(""))
 				{
 				    throw new SystemException("There was no page invoker class assigned to this page type.");
 				}
 				else
 				{
 				    try
 				    {
 				        PageInvoker pageInvoker = (PageInvoker)Class.forName(invokerClassName).newInstance();
 				        pageInvoker = pageInvoker.getDecoratedPageInvoker();
 				        pageInvoker.setParameters(dbWrapper, this.getRequest(), this.getResponse(), this.templateController, deliveryContext);
 				        pageInvoker.deliverPage();
 				    }
 				    catch(ClassNotFoundException e)
 				    {
 				        throw new SystemException("An error was thrown when trying to use the page invoker class assigned to this page type:" + e.getMessage(), e);
 				    }
 				}
 			}
 			
 			StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(dbWrapper.getDatabase());
 			throw new SystemException(e.getMessage());
 		}
 		finally
 		{
 		    closeTransaction(dbWrapper.getDatabase());
 		}
 		
 		elapsedTime = new Date().getTime() - start;
 		getLogger().info("The page delivery took " + elapsedTime + "ms");
 		
 		return NONE;
 	}
     
   
     
    	/**
 	 * This method should be much more sophisticated later and include a check to see if there is a 
 	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
 	 * The method also consideres wheter or not to invoke the preview-version with administrative functioality or the 
 	 * normal site-delivery version.
 	 */
 	
 	public TemplateController getTemplateController(DatabaseWrapper dbWrapper, Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal, boolean allowEditOnSightAtAll) throws SystemException, Exception
 	{
 		TemplateController templateController = new BasicTemplateController(dbWrapper, infoGluePrincipal);
 		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
 		templateController.setHttpRequest(request);	
 		templateController.setBrowserBean(browserBean);
 		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
 		
 		String operatingMode = CmsPropertyHandler.getProperty("operatingMode");
 		String editOnSite = CmsPropertyHandler.getProperty("editOnSite");
 		boolean isEditOnSightDisabled = templateController.getIsEditOnSightDisabled();
 		if(allowEditOnSightAtAll && !isEditOnSightDisabled && operatingMode != null && (operatingMode.equals("0") || operatingMode.equals("1") || operatingMode.equals("2")) && editOnSite != null && editOnSite.equalsIgnoreCase("true"))
 		{
 			templateController = new EditOnSiteBasicTemplateController(dbWrapper, infoGluePrincipal);
 			templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
 			templateController.setHttpRequest(request);	
 			templateController.setBrowserBean(browserBean);
 			templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
 		}
 		
 		return templateController;		
 	}
 
 
 	/**
 	 * Here we do all modifications needed on the request. For example we read the startpage if no
 	 * siteNodeId is given and stuff like that. Also a good place to put url-rewriting.
 	 * Rules so far includes: defaulting to the first repository if not specified and also defaulting to
 	 * masterlanguage for that site if not specifying.
 	 */
 	 
 	private void validateAndModifyInputParameters(Database db) throws SystemException, Exception
 	{
 		this.browserBean = new BrowserBean();
 		this.browserBean.setRequest(getRequest());
 		
 		if(getSiteNodeId() == null)
 		{
 			if(getRepositoryName() == null)
 			{
 				setRepositoryName(RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getName());
 			}
 			
 			SiteNodeVO rootSiteNodeVO = NodeDeliveryController.getRootSiteNode(db, getRepositoryName());	
 			if(rootSiteNodeVO == null)
 				throw new SystemException("There was no repository called " + getRepositoryName() + " or no pages were available in that repository");
 			
 			setSiteNodeId(rootSiteNodeVO.getSiteNodeId());
 		} 
 		
 		if(getLanguageId() == null)
 		{
 			LanguageVO browserLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfRepositorySupportsIt(db, browserBean.getLanguageCode(), getSiteNodeId());
 			if(browserLanguageVO != null)
 			{
 				setLanguageId(browserLanguageVO.getLanguageId());
 			}
 			else
 			{
 				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, this.getSiteNodeId());
 				if(masterLanguageVO == null)
 					throw new SystemException("There was no master language for the siteNode " + getSiteNodeId());
 	
 				setLanguageId(masterLanguageVO.getLanguageId());				
 			}
 		}
 		
 		this.principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
 		if(this.principal == null)
 		{
 			try
 			{
 				this.principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
 				if(this.principal == null)
 				{
 				    Map arguments = new HashMap();
 				    arguments.put("j_username", "anonymous");
 				    arguments.put("j_password", "anonymous");
 				    
 					this.principal = ExtranetController.getController().getAuthenticatedPrincipal(db, arguments);
 					
 					if(principal != null)
 						CacheController.cacheObject("userCache", "anonymous", this.principal);
 				}
 				//this.principal = ExtranetController.getController().getAuthenticatedPrincipal("anonymous", "anonymous");
 				
 			}
 			catch(Exception e) 
 			{
 			    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
 			}
 		}
 	}
 
 	/**
 	 * This method validates that the current page is accessible to the requesting user.
 	 * It fetches information from the page metainfo about if the page is protected and if it is 
 	 * validates the users credentials against the extranet database,
 	 */
 	
 	public boolean handleExtranetLogic(Database db, Integer protectedSiteNodeVersionId) throws SystemException, Exception
 	{
 	   boolean isRedirected = false;
 		
 		try
 		{
 		    String referer = this.getRequest().getHeader("Referer");
 			getLogger().info("referer:" + referer);
 			
 			if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
 				referer = "/"; 
 			
 			Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
 			getLogger().info("principal:" + principal);
 			if(principal == null)
 			{
 			    principal = loginWithCookies();
 			    			    
 			    if(principal == null)
 			    {
 				    try
 					{
 						principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
 						if(principal == null)
 						{
 						    Map arguments = new HashMap();
 						    arguments.put("j_username", "anonymous");
 						    arguments.put("j_password", "anonymous");
 						    
 							principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 							if(principal != null)
 								CacheController.cacheObject("userCache", "anonymous", principal);
 						}
 						
 						if(principal != null)
 						{
 						    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 						    
 							SiteNodeVersionVO siteNodeVersionVO = this.nodeDeliveryController.getActiveSiteNodeVersionVO(db, siteNodeId);
 							boolean isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", siteNodeVersionVO.getId().toString());
 							if(!isAuthorized)
 							{	
 								getLogger().info("SiteNode is protected and user was not found - sending him to login page.");
 								String url = this.getURLBase() + "/ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
 								getResponse().sendRedirect(url);
 								isRedirected = true;
 							}
 						}
 					}
 					catch(Exception e) 
 					{
 					    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
 					}
 			    }
 			}
 			else
 			{
 				getLogger().info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 				//Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(siteNodeId);
 				
 				Map arguments = new HashMap();
 				arguments.put("j_username", "anonymous");
 			    arguments.put("j_password", "anonymous");
 			    
 				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()) &&  !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)ExtranetController.getController().getAuthenticatedPrincipal(arguments), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
 				{	
 					if(this.referer == null)
 						this.referer = this.getRequest().getHeader("Referer");
 					
 					if(this.referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
 						referer = "/"; 
 
 					if(principal.getName().equals("anonymous"))
 					{
 						getLogger().info("SiteNode is protected and user was anonymous - sending him to login page.");
 						String url = "ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
 						getResponse().sendRedirect(url);
 						isRedirected = true;
 					}
 					else
 					{
 						getLogger().info("SiteNode is protected and user has no access - sending him to no access page.");
 						//final String url = "ExtranetLogin!noAccess.action?returnAddress=" + java.net.URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString(), "UTF-8");
 						String url = "ExtranetLogin!noAccess.action?referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
 						getResponse().sendRedirect(url);
 						isRedirected = true;
 					}
 				}
 			}
 		}
 		catch(SystemException se)
 		{
 			se.printStackTrace();
 			throw se;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return isRedirected;
 	}
 	
 	
 	/**
 	 * This method (if enabled in deliver.properties) checks for authentication cookies and 
 	 * logs the user in if available.
 	 * 
 	 * @return Principal
 	 * @throws Exception
 	 */
 	private Principal loginWithCookies() throws Exception
 	{
 	    Principal principal = null;
 	    
 	    boolean enableExtranetCookies = false;
 	    int extranetCookieTimeout = 43200; //30 days default
 	    String enableExtranetCookiesString = CmsPropertyHandler.getProperty("enableExtranetCookies");
 	    String extranetCookieTimeoutString = CmsPropertyHandler.getProperty("extranetCookieTimeout");
 	    if(enableExtranetCookiesString != null && enableExtranetCookiesString.equalsIgnoreCase("true"))
 	    {
 	        enableExtranetCookies = true;
 	    }
 	    if(extranetCookieTimeoutString != null)
 	    {
 	        try
 		    {
 	            extranetCookieTimeout = Integer.parseInt(extranetCookieTimeoutString.trim());
 		    }
 	        catch(Exception e) {}
 		}
 	
 	    if(enableExtranetCookies)
 	    {
 	        String userName = null;
 		    String password = null;
 		    Cookie[] cookies = this.getRequest().getCookies();
 		    for(int i=0; i<cookies.length; i++)
 		    {
 		        Cookie cookie = cookies[i];
 		        if(cookie.getName().equals("igextranetuserid"))
 		            userName = cookie.getValue();
 		        else if(cookie.getName().equals("igextranetpassword"))
 		            password = cookie.getValue();
 		    }
 		    
 		    if(userName != null && password != null)
 		    {
 		        DesEncryptionHelper encHelper = new DesEncryptionHelper();
 		        userName = encHelper.decrypt(userName);
 		        password = encHelper.decrypt(password);
 		        
 			    Map arguments = new HashMap();
 			    arguments.put("j_username", userName);
 			    arguments.put("j_password", password);
 			    
 				principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 				if(principal != null)
 				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 			    
 		    }
 	    }
 	    
 	    return principal;
 	}
 	
 	
 	/**
 	 * Setters and getters for all things sent to the page in the request
 	 */
 	
     public java.lang.Integer getSiteNodeId()
     {
         return this.siteNodeId;
     }
         
     public void setSiteNodeId(Integer siteNodeId)
     {
         this.siteNodeId = siteNodeId;
 	}
 
     public Integer getContentId()
     {
         return this.contentId;
     }
         
     public void setContentId(Integer contentId)
     {
     	this.contentId = contentId;
     }
 
     public Integer getLanguageId()
     {
         return this.languageId;
     }
         
     public void setLanguageId(Integer languageId)
     {
 		this.languageId = languageId;   
 	}
 
     public String getRepositoryName()
     {
         return this.repositoryName;
     }
         
     public void setRepositoryName(String repositoryName)
     {
 	    this.repositoryName = repositoryName;
     }
     
 	public String getReferer()
 	{
 		return referer;
 	}
 
 	public void setReferer(String referer)
 	{
 		this.referer = referer;
 	}
 
     public void setShowSimple(boolean showSimple)
     {
         this.showSimple = showSimple;
     }
     
 }
