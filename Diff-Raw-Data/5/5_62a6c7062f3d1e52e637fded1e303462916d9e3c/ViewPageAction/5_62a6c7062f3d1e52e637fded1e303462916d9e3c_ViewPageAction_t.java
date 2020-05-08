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
 
 import java.net.URLEncoder;
 import java.security.Principal;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.AuthenticationModule;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.DesEncryptionHelper;
 import org.infoglue.cms.util.mail.MailServiceFactory;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.filters.ViewPageFilter;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.EditOnSiteBasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.invokers.PageInvoker;
 import org.infoglue.deliver.portal.PortalService;
 //import org.infoglue.deliver.services.StatisticsService;
 import org.infoglue.deliver.util.BrowserBean;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.RequestAnalyser;
 import org.infoglue.deliver.util.ThreadMonitor;
 
 
 /**
  * This is the main delivery action. Gets called when the user clicks on a link that goes inside the site.
  *
  * @author Mattias Bogeblad
  */
 
 public class ViewPageAction extends InfoGlueAbstractAction 
 {
 	private static final long serialVersionUID = 1L;
 
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
 
 	private boolean isRecacheCall = false;
 	
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
 	public static long lastRequestProcessingTime = 0;
 	private static Thread lastThread = null;
 	private static boolean memoryWarningSent = false;
 	
 	private ThreadMonitor tk = null;
 	
 	private static Random random = new Random();
 
 	
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
         if(isRecacheCall)
         {
 	        //logger.warn("ThreadId:" + Thread.currentThread().getName());
             Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
         }
                     	    	
         //TODO - Can this be removed perhaps
         while(!CmsPropertyHandler.getOperatingMode().equals("3") && RequestAnalyser.getRequestAnalyser().getBlockRequests())
         {
         	//System.out.println("Queing up requests as cache eviction are taking place..");
         	Thread.sleep(10);
         }
         
         try
         {
 	        Integer maxActiveRequests = new Integer(CmsPropertyHandler.getMaxActiveRequests());
 	        Integer maxRequestTime = new Integer(CmsPropertyHandler.getMaxRequestTime());
         	//System.out.println("maxActiveRequests:" + maxActiveRequests + "-" + maxRequestTime);
 
 	    	while(CmsPropertyHandler.getUseHighLoadLimiter().equalsIgnoreCase("true") && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > maxActiveRequests.intValue() && (lastRequestProcessingTime > maxRequestTime.intValue() || maxRequestTime.intValue() < 1))
 	    	{
 	        	if(logger.isInfoEnabled())
 	        		logger.info("Queing up...:" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() + "(" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + ") - " + lastRequestProcessingTime);
 	        	
 	            int sleepTime = random.nextInt(300);
 	            //System.out.println("Queing up...:" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() + "(" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + ") - " + lastRequestProcessingTime + " for " + sleepTime + " ms");
 	            
 	        	Thread.sleep(sleepTime);
 	    	}
         }
         catch(Exception e)
         {
         	logger.error("You have faulty settings in either maxActiveRequests or maxRequestTime - fix this as it affects performance:" + e.getMessage(), e);
         }
         
         if(logger.isInfoEnabled())
         {
 	        logger.info("************************************************");
 	    	logger.info("* ViewPageAction was called....                *");
 	    	logger.info("************************************************");
         }
         
         HttpServletRequest request = getRequest();
         
     	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
     		tk = new ThreadMonitor(new Long(CmsPropertyHandler.getDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
     	else
     	{
     		if(!CmsPropertyHandler.getKillLiveRequestWhichTimedout())
     			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view seems to take to long!", false);
     		else
     			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
     	}
     	
     	RequestAnalyser.getRequestAnalyser().incNumberOfCurrentRequests(tk);
 
     	long start 			= System.currentTimeMillis();
     	long elapsedTime 	= 0;
     	    	
     	DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
     	
 		beginTransaction(dbWrapper.getDatabase());
 
    		try
 		{
 			validateAndModifyInputParameters(dbWrapper.getDatabase());
 	    	
 	    	this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			
 			String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), getRequest(), this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");
 	    	//String pageKey = CacheController.getPageCacheKey(this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");
 
 	    	if(logger.isInfoEnabled())
 	    		logger.info("pageKey:" + pageKey);
 	    	
 	    	String pagePath	= null;
 	    	
 	    	boolean isUserRedirected = false;
 			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionIdForPageCache(dbWrapper.getDatabase(), siteNodeId);
 			
 			if(logger.isInfoEnabled())
 				logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 			
 			String protectWorking = CmsPropertyHandler.getProtectDeliverWorking();
 			String protectPreview = CmsPropertyHandler.getProtectDeliverPreview();
 			boolean protectDeliver = false;
 
 			if(protectWorking.equals("true") && !CmsPropertyHandler.getOperatingMode().equals("0"))
 				protectDeliver = true;
 			else if(protectPreview.equals("true") && !CmsPropertyHandler.getOperatingMode().equals("2"))
 				protectDeliver = true;
 				
 			if(protectedSiteNodeVersionId != null || protectDeliver)
 				isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), protectedSiteNodeVersionId, protectDeliver);
 		
 			this.templateController = getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, false);
 			
 			if(logger.isInfoEnabled())
 				logger.info("handled extranet users: " + isUserRedirected);
 	
 			// ----
 			// -- portlet
 			// ----
 			
 			// -- check if the portal is active
 	        String portalEnabled = CmsPropertyHandler.getEnablePortal();
 	        boolean portalActive = ((portalEnabled != null) && portalEnabled.equals("true"));
 			
 	        if (portalActive && !isRecacheCall) 
 	        {
 	        	if(logger.isInfoEnabled())
 	        		logger.info("---> Checking for portlet action");
 	            
 	        	PortalService service = new PortalService();
 	            //TODO: catch PortalException?
 	            boolean actionExecuted = service.service(getRequest(), getResponse());
 	            
 	            // -- if an action was executed return NONE as a redirect is issued
 	            if (actionExecuted) 
 	            {
 	            	if(logger.isInfoEnabled())
 	            		logger.info("---> PortletAction was executed, returning NONE as a redirect has been issued");
 	                isUserRedirected = true;
 	                return NONE;
 	            }
 	        }
 	
 	        if(logger.isInfoEnabled())
 	        	logger.info("handled portal action: " + isUserRedirected);
 	        
 			if(!isUserRedirected)
 			{	
 				if(logger.isInfoEnabled())
 					logger.info("this.templateController.getPrincipal():" + this.templateController.getPrincipal());
 				
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
 				deliveryContext.setUseFullUrl(Boolean.parseBoolean(CmsPropertyHandler.getUseDNSNameInURI()));
 				
 				SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = getSiteNodeTypeDefinition(this.siteNodeId, dbWrapper.getDatabase());
 								
 			    try
 			    {
 			        String invokerClassName = siteNodeTypeDefinitionVO.getInvokerClassName();
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
 			
 	        //StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
 			//logger.info("Registered request in statistics service");
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(dbWrapper.getDatabase());
 
 			getResponse().setContentType("text/html; charset=UTF-8");
 			getRequest().setAttribute("responseCode", "500");
 			getRequest().setAttribute("error", e);
 			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
 		}
 		finally
 		{
 			if(logger.isInfoEnabled())
 				logger.info("Before closing transaction");
 
 			closeTransaction(dbWrapper.getDatabase());
 		  
 			if(logger.isInfoEnabled())
 				logger.info("After closing transaction");
 
 			elapsedTime = System.currentTimeMillis() - start;
 			RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(elapsedTime);
 			lastRequestProcessingTime = elapsedTime;
 
 		    //System.out.println("The page delivery took " + elapsedTime + "ms for request " + this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString());
 			if(!memoryWarningSent)
 			{
 				float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
 				float percentLeft = (memoryLeft / ((float)Runtime.getRuntime().maxMemory() / 1024f / 1024f)) * 100f;
 				float percentLeft2 = ((float)Runtime.getRuntime().freeMemory() / (float)Runtime.getRuntime().totalMemory()) * 100f;
 				//System.out.println("memoryLeft:" + memoryLeft);
 				//System.out.println("maxMemory:" + (Runtime.getRuntime().maxMemory() / 1024 / 1024));
 				//System.out.println("percentLeft:" + percentLeft);
 				if(percentLeft < 15 && percentLeft2 < 15)
 				{
 					memoryWarningSent = true;
 					String subject = "Memory is getting low on " + CmsPropertyHandler.getServerName();
 					String mailBody = "The java maximum heap size is almost used up - only " + (int)memoryLeft + "MB (" + (int)percentLeft + "%) left. Increase the max heap size if possible or trim the cache sizes if they are very large.";
 			        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
 			        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
 			        {
 						try
 						{
 							logger.warn("Sending warning mail:" + (int)percentLeft + ":" + (int)memoryLeft + ":" + Runtime.getRuntime().maxMemory() / 1024 / 1024);
 							MailServiceFactory.getService().sendEmail(warningEmailReceiver, warningEmailReceiver, null, subject, mailBody, "utf-8");
 						} 
 						catch (Exception e)
 						{
 							logger.error("Could not send mail:" + e.getMessage(), e);
 						}
 			        }
 				}
 			}
 			
 			if(elapsedTime > 10000)
 			{
 			    logger.warn("The page delivery took " + elapsedTime + "ms for request " + this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString());
 			    logger.warn("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 			}
 			else
 			{
 				if(logger.isInfoEnabled())
 				{
 					logger.info("The page delivery took " + elapsedTime + "ms");			
 					logger.info("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 				}
 			}
 
 	    	if(tk != null)
 	    		tk.done();
 		}
 		
         return NONE;
     }
     
 
 
 	/**
 	 * This method the renderer for the component editor. 
 	 */
          
 	public String doRenderDecoratedPage() throws Exception
 	{
 		if(CmsPropertyHandler.getOperatingMode().equals("3"))
 			return doExecute();
 				
         while(!CmsPropertyHandler.getOperatingMode().equals("3") && RequestAnalyser.getRequestAnalyser().getBlockRequests())
         {
         	//System.out.println("Queing up requests as cache eviction are taking place..");
         	Thread.sleep(10);
         }
 		
 		logger.info("************************************************");
 		logger.info("* ViewPageAction was called....                *");
 		logger.info("************************************************");
 
         HttpServletRequest request = getRequest();
 
     	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
     		tk = new ThreadMonitor(new Long(CmsPropertyHandler.getDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
     	else
     	{
     		if(!CmsPropertyHandler.getKillLiveRequestWhichTimedout())
     			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view seems to take to long!", false);
     		else
     			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
     	}
     		
     	RequestAnalyser.getRequestAnalyser().incNumberOfCurrentRequests(tk);
 
    		long start			= new Date().getTime();
 		long elapsedTime 	= 0;
     			
 		DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
     	//Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(dbWrapper.getDatabase());
 
 		try
 		{
 			validateAndModifyInputParameters(dbWrapper.getDatabase());
 	    	
 			this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 
 			//String pageKey  = "" + this.siteNodeId + "_" + this.languageId + "_" + this.contentId + "_" + browserBean.getUseragent() + "_" + getRequest().getQueryString() + "_" + this.showSimple + "_pagecomponentDecorated";
 			//String pageKey = CacheController.getPageCacheKey(this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "_" + this.showSimple + "_pagecomponentDecorated");
 	    	String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), this.getRequest(), this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "_" + this.showSimple + "_pagecomponentDecorated");
 
 			logger.info("A pageKey:" + pageKey);
 			String pagePath	= null;
 	    	
 			boolean isUserRedirected = false;
 			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(dbWrapper.getDatabase(), siteNodeId);
 			logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 
 			String protectWorking = CmsPropertyHandler.getProtectDeliverWorking();
 			String protectPreview = CmsPropertyHandler.getProtectDeliverPreview();
 			boolean protectDeliver = false;
 
 			if(protectWorking.equals("true") && !CmsPropertyHandler.getOperatingMode().equals("0"))
 				protectDeliver = true;
 			else if(protectPreview.equals("true") && !CmsPropertyHandler.getOperatingMode().equals("2"))
 				protectDeliver = true;
 				
 			if(protectedSiteNodeVersionId != null || protectDeliver)
 				isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), protectedSiteNodeVersionId, protectDeliver);
 			
 			this.templateController = getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, true);
 
 			logger.info("handled extranet users");
 
 			// ----
 			// -- portlet
 			// ----
 			
 			// -- check if the portal is active
 	        String portalEnabled = CmsPropertyHandler.getEnablePortal();
 	        boolean portalActive = ((portalEnabled != null) && portalEnabled.equals("true"));
 			
 	        if (portalActive && !isRecacheCall) 
 	        {
 	            logger.info("---> Checking for portlet action");
 	            PortalService service = new PortalService();
 	            //TODO: catch PortalException?
 	            boolean actionExecuted = service.service(getRequest(), getResponse());
 	            
 	            // -- if an action was executed return NONE as a redirect is issued
 	            if (actionExecuted) 
 	            {
 	                logger.info("---> PortletAction was executed, returning NONE as a redirect has been issued");
 	                isUserRedirected = true;
 	                return NONE;
 	            }
 	        }
 	
 	        logger.info("handled portal action");
 
 			if(!isUserRedirected)
 			{	
 				logger.info("this.templateController.getPrincipal():" + this.templateController.getPrincipal());
 		
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
 				deliveryContext.setUseFullUrl(Boolean.parseBoolean(CmsPropertyHandler.getUseDNSNameInURI()));
 
 				SiteNode siteNode = nodeDeliveryController.getSiteNode(dbWrapper.getDatabase(), this.siteNodeId);
 				if(siteNode == null)
 				    throw new SystemException("There was no page with this id.");

				if(siteNode.getSiteNodeTypeDefinition() == null)
				    throw new SystemException("There was no SiteNodeTypeDefinition defined for the site node " + siteNode.getName() + "[" + siteNode.getId() + "].");

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
 			
 			//StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(dbWrapper.getDatabase());
 			throw new SystemException(e.getMessage());
 		}
 		finally
 		{
 		    closeTransaction(dbWrapper.getDatabase());
 
 		    elapsedTime = System.currentTimeMillis() - start;
 
 			RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(elapsedTime);
 
 			if(!memoryWarningSent)
 			{
 				float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
 				float percentLeft = (memoryLeft / ((float)Runtime.getRuntime().maxMemory() / 1024f / 1024f)) * 100f;
 				float percentLeft2 = ((float)Runtime.getRuntime().freeMemory() / (float)Runtime.getRuntime().totalMemory()) * 100f;
 				
 				//System.out.println("memoryLeft:" + memoryLeft);
 				//System.out.println("maxMemory:" + (Runtime.getRuntime().maxMemory() / 1024f / 1024f));
 				//System.out.println("totalMemory:" + (Runtime.getRuntime().totalMemory() / 1024f / 1024f));
 				//System.out.println("freeMemory:" + (Runtime.getRuntime().freeMemory() / 1024f / 1024f));
 				//System.out.println("percentLeft:" + percentLeft);
 				//System.out.println("percentLeft2:" + percentLeft2);
 				if(percentLeft < 15 && percentLeft2 < 15)
 				{
 					memoryWarningSent = true;
 					String subject = "Memory is getting low on " + CmsPropertyHandler.getServerName();
 					String mailBody = "The java maximum heap size is almost used up - only " + (int)memoryLeft + "MB (" + (int)percentLeft + "%) left. Increase the max heap size if possible or trim the cache sizes if they are very large.";
 			        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
 			        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
 			        {
 						try
 						{
 							logger.warn("Sending warning mail:" + (int)percentLeft + ":" + (int)memoryLeft + ":" + Runtime.getRuntime().maxMemory() / 1024f / 1024f);
 							MailServiceFactory.getService().sendEmail(warningEmailReceiver, warningEmailReceiver, null, subject, mailBody, "utf-8");
 						} 
 						catch (Exception e)
 						{
 							logger.error("Could not send mail:" + e.getMessage(), e);
 						}
 			        }
 				}
 			}
 
 			if(elapsedTime > 20000)
 			{
 			    logger.warn("The page delivery took " + elapsedTime + "ms for request " + this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString());
 			    logger.warn("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 			}
 			else
 			{
 			    logger.info("The page delivery took " + elapsedTime + "ms");			
 			    logger.info("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
 			}
 
 			if(tk != null)
 	    		tk.done();
 		}
 		
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
 		
 		String operatingMode = CmsPropertyHandler.getOperatingMode();
 		
 		if(operatingMode != null && (operatingMode.equals("0") || operatingMode.equals("1") || operatingMode.equals("2")))
 		{
 		    String editOnSite = CmsPropertyHandler.getEditOnSite();
 			boolean isEditOnSightDisabled = templateController.getIsEditOnSightDisabled();
 			
 			if(allowEditOnSightAtAll && !isEditOnSightDisabled && editOnSite != null && editOnSite.equalsIgnoreCase("true"))
 			{
 				templateController = new EditOnSiteBasicTemplateController(dbWrapper, infoGluePrincipal);
 				templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
 				templateController.setHttpRequest(request);	
 				templateController.setBrowserBean(browserBean);
 				templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
 			}
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
 		
 		this.principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
 		/*
 		boolean enforceJ2EEPrincipal = AuthenticationModule.getAuthenticationModule(null, null).enforceJ2EEContainerPrincipal();
 		if(!enforceJ2EEPrincipal || this.getRequest().getUserPrincipal() != null)
 			this.principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
 		*/
 		if(this.principal == null)
 		{
 			try
 			{
 				this.principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
 				if(this.principal == null)
 				{
 				    Map arguments = new HashMap();
 				    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
 				    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
 				    
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
 
 		try
 		{
 			if(getSiteNodeId() != null)
 				SiteNodeController.getSiteNodeVOWithId(getSiteNodeId(), db);
 	    }
 	    catch(Exception e)
 	    {
 			throw new SystemException("There was no page with the current specification. SiteNodeId:" + getSiteNodeId());
 	    }
 		
 		if(getLanguageId() == null)
 		{
 		    LanguageVO browserLanguageVO = null;
 
 		    String useAlternativeBrowserLanguageCheck = CmsPropertyHandler.getUseAlternativeBrowserLanguageCheck();
 		    if(useAlternativeBrowserLanguageCheck == null || !useAlternativeBrowserLanguageCheck.equalsIgnoreCase("true"))
 		        browserLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, browserBean.getLanguageCode(), getSiteNodeId(), (InfoGluePrincipal)this.principal);
 		    else
 		        browserLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfRepositorySupportsIt(db, browserBean.getLanguageCode(), getSiteNodeId());
 
 			logger.debug("Checking browser language...");
 
 		    if(browserLanguageVO != null)
 			{
 			    logger.info("The system had browserLanguageVO available:" + browserLanguageVO.getName());
 			    setLanguageId(browserLanguageVO.getLanguageId());
 			}
 			else
 			{
 				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, this.getSiteNodeId());
 				if(masterLanguageVO == null)
 					throw new SystemException("There was no master language for the siteNode " + getSiteNodeId());
 
 				
 				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 				boolean isMasterLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, ndc.getSiteNode(db, siteNodeId), masterLanguageVO.getId());
 				if(!isMasterLanguageValid)
 				{
 				    logger.info("Master language was not allowed on this sitenode... let's take the next on in order");
 				    List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, this.getSiteNodeId());
 				    Iterator languagesIterator = languages.iterator();
 				    while(languagesIterator.hasNext())
 				    {
 				        LanguageVO currentLanguage = (LanguageVO)languagesIterator.next();
 				        boolean isCurrentLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, ndc.getSiteNode(db, siteNodeId), currentLanguage.getId());
 					    logger.info("currentLanguage validity:" + isCurrentLanguageValid);
 				        if(isCurrentLanguageValid)
 				        {
 				            setLanguageId(currentLanguage.getLanguageId());
 				            break;
 				        }
 				    }
 				}
 				else
 				{
 				    logger.info("The system had no browserLanguageVO available - using master language instead:" + masterLanguageVO.getName());
 				    setLanguageId(masterLanguageVO.getLanguageId());				
 				}
 
 			}
 		}
 		else
 		{
 		    LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, getLanguageId(), getSiteNodeId());
 		   
 		    if(languageVO != null)
 			{
 			    logger.info("The system had browserLanguageVO available:" + languageVO.getName());
 			    setLanguageId(languageVO.getLanguageId());
 			}
 			else
 			{
 				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, this.getSiteNodeId());
 				if(masterLanguageVO == null)
 					throw new SystemException("There was no master language for the siteNode " + getSiteNodeId());
 				
 				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 				boolean isMasterLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, ndc.getSiteNode(db, siteNodeId), masterLanguageVO.getId());
 				if(!isMasterLanguageValid)
 				{
 				    logger.info("Master language was not allowed on this sitenode... let's take the next on in order");
 				    List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, this.getSiteNodeId());
 				    Iterator languagesIterator = languages.iterator();
 				    while(languagesIterator.hasNext())
 				    {
 				        LanguageVO currentLanguage = (LanguageVO)languagesIterator.next();
 				        boolean isCurrentLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, ndc.getSiteNode(db, siteNodeId), currentLanguage.getId());
 					    logger.info("currentLanguage validity:" + isCurrentLanguageValid);
 				        if(isCurrentLanguageValid)
 				        {
 				            setLanguageId(currentLanguage.getLanguageId());
 				            break;
 				        }
 				    }
 				}
 				else
 				{
 				    logger.info("The system had no browserLanguageVO available - using master language instead:" + masterLanguageVO.getName());
 				    setLanguageId(masterLanguageVO.getLanguageId());				
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method validates that the current page is accessible to the requesting user.
 	 * It fetches information from the page metainfo about if the page is protected and if it is 
 	 * validates the users credentials against the extranet database,
 	 */
 	
 	public boolean handleExtranetLogic(Database db, Integer protectedSiteNodeVersionId, boolean protectDeliver) throws SystemException, Exception
 	{
 		boolean isRedirected = false;
 		
 		try
 		{
 		    String referer = this.getRequest().getHeader("Referer");
 			logger.info("referer:" + referer);
 			
 			if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
 				referer = "/"; 
 			
 			Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
 			logger.info("principal:" + principal);
 
 			//First we check if the user is logged in to the container context
 			if(principal == null)
 			{
 			    if(this.getRequest().getUserPrincipal() != null && !(this.getRequest().getUserPrincipal() instanceof InfoGluePrincipal))
 			    {
 					Map status = new HashMap();
 					status.put("redirected", new Boolean(false));
 					principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL()).loginUser(getRequest(), getResponse(), status);
 					Boolean redirected = (Boolean)status.get("redirected");
 					if(redirected != null && redirected.booleanValue())
 					{
 					    this.getHttpSession().removeAttribute("infogluePrincipal");
 					    this.principal = null;
 					    return true;
 					}
 					else if(principal != null)
 					{
 					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 	
 					    this.principal = principal;
 					}
 			    }
 			}
 		
 			if(principal == null && !protectDeliver)
 			{
 				Principal anonymousPrincipal = getAnonymousPrincipal();
 				boolean isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
 				if(isAuthorized)
 				{	
 					principal = anonymousPrincipal;
 					if(principal != null)
 					{
 					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 					    this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 					}
 				}
 			}
 			
 			if(principal == null)
 			{				
 				Map status = new HashMap();
 				status.put("redirected", new Boolean(false));
 				principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL()).loginUser(getRequest(), getResponse(), status);
 				Boolean redirected = (Boolean)status.get("redirected");
 				if(redirected != null && redirected.booleanValue())
 				{
 				    this.getHttpSession().removeAttribute("infogluePrincipal");
 				    this.principal = null;
 				    return true;
 				}
 				else if(principal != null)
 				{
 				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 
 				    this.principal = principal;
 				}
 				
 				if(principal == null)
 					principal = loginWithCookies();
 				
 			    if(principal == null)
 			        principal = loginWithRequestArguments();
 			    
 			    if(principal == null)
 			    {	
 			    	try
 					{
 						principal = getAnonymousPrincipal();
 						
 						if(principal != null)
 						{
 							this.getHttpSession().setAttribute("infogluePrincipal", principal);
 							this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 							
 							boolean isAuthorized = false;
 							if(!protectDeliver)
 								isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
 							
 							if(!isAuthorized)
 							{	
 								this.getHttpSession().removeAttribute("infogluePrincipal");
 								logger.info("SiteNode is protected and anonymous user was not allowed - sending him to login page.");
 								String redirectUrl = getRedirectUrl(getRequest(), getResponse());
 								//String url = this.getURLBase() + "/ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
 								getResponse().sendRedirect(redirectUrl);
 								isRedirected = true;
 							}
 						}
 					}
 					catch(Exception e) 
 					{
 					    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
 					}
 			    }
 				else
 				{
 					boolean isAuthorized = false;
 					if(protectDeliver && protectedSiteNodeVersionId == null && !principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
 						isAuthorized = true;
 					else if(!protectDeliver)
 						isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
 					
 					if(!isAuthorized)
 					{	
 						if(this.referer == null)
 							this.referer = this.getRequest().getHeader("Referer");
 						
 						if(this.referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
 							this.referer = "/"; 
 
 						if(principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
 						{
 							logger.info("SiteNode is protected and user was anonymous - sending him to login page.");
 							//String url = "ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
 							String url = getRedirectUrl(getRequest(), getResponse());
 							
 							getResponse().sendRedirect(url);
 							isRedirected = true;
 						}
 						else
 						{
 							logger.info("SiteNode is protected and user has no access - sending him to no access page.");
 						    String url = "ExtranetLogin!noAccess.action?referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
 							getResponse().sendRedirect(url);
 							isRedirected = true;
 						}
 					}
 					else
 					{
 						this.getHttpSession().setAttribute("infogluePrincipal", principal);
 						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 		
 						this.principal = principal;
 					}
 				}
 
 			}
 			else
 			{
 				logger.info("principal:" + principal);
 				logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
 
 				Principal alternativePrincipal = loginWithCookies();
 			    if(alternativePrincipal == null)
 			        alternativePrincipal = loginWithRequestArguments();
 
 			    if(protectedSiteNodeVersionId != null && alternativePrincipal != null && AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)alternativePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
 			    {
 			        logger.info("The user " + alternativePrincipal.getName() + " was approved.");
 			    }
 				else if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()) &&  !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
 				{	
 					if(this.referer == null)
 						this.referer = this.getRequest().getHeader("Referer");
 					
 					if(this.referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
 						this.referer = "/"; 
 
 					if(principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
 					{
 						logger.info("SiteNode is protected and user was anonymous - sending him to login page.");
 						//String url = "ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
 						String url = getRedirectUrl(getRequest(), getResponse());
 						
 						getResponse().sendRedirect(url);
 						isRedirected = true;
 					}
 					else
 					{
 						logger.info("SiteNode is protected and user has no access - sending him to no access page.");
 						String url = "ExtranetLogin!noAccess.action?referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
 
 						getResponse().sendRedirect(url);
 						isRedirected = true;
 					}
 				}
 				else if(protectedSiteNodeVersionId == null && protectDeliver)
 				{
 					logger.info("Setting user to anonymous... as this is a protected deliver but not a extranet...");
 					Principal anonymousPrincipal = getAnonymousPrincipal();
 					
 					//this.getHttpSession().setAttribute("infogluePrincipal", anonymousPrincipal);
 					//this.getHttpSession().setAttribute("infoglueRemoteUser", anonymousPrincipal.getName());
 	
 					this.principal = anonymousPrincipal;
 				}
 			}
 		}
 		catch(SystemException se)
 		{
 			logger.warn("An error occurred:" + se.getMessage(), se);
 			throw se;
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred:" + e.getMessage(), e);
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
 	    String enableExtranetCookiesString = CmsPropertyHandler.getEnableExtranetCookies();
 	    String extranetCookieTimeoutString = CmsPropertyHandler.getExtranetCookieTimeout();
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
 		    if(cookies != null)
 		    {
 			    for(int i=0; i<cookies.length; i++)
 			    {
 			        Cookie cookie = cookies[i];
 			        if(cookie.getName().equals("igextranetuserid"))
 			            userName = cookie.getValue();
 			        else if(cookie.getName().equals("igextranetpassword"))
 			            password = cookie.getValue();
 			    }
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
 				{
 				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 				}
 			    
 		    }
 	    }
 	    
 	    return principal;
 	}
 	
 	/**
 	 * This method (if enabled in deliver.properties) checks for arguments in the request
 	 * and logs the user in if available.
 	 * 
 	 * @return Principal
 	 * @throws Exception
 	 */
 	private Principal loginWithRequestArguments() throws Exception
 	{
 	    Principal principal = null;
 	    
         String userName = this.getRequest().getParameter("j_username");
 	    String password = this.getRequest().getParameter("j_password");
 	    String ticket 	= null; //this.getRequest().getParameter("ticket");
 	    //System.out.println("userName:" + userName);
 	    //System.out.println("password:" + password);
 	    //System.out.println("ticket:" + ticket);
 		
 		if(ticket != null)
 	    {
 			//System.out.println("ticket used in loginWithRequestArguments:" + ticket);
 		    Map arguments = new HashMap();
 		    arguments.put("ticket", ticket);
 		    
 			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 			if(principal != null)
 			{
 			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 			}
 	    }		    
 	    else if(userName != null && password != null)
 	    {
 		    Map arguments = new HashMap();
 		    arguments.put("j_username", userName);
 		    arguments.put("j_password", password);
 		    
 			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 			if(principal != null)
 			{
 			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
 				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
 			}
 	    }
 	    
 	    return principal;
 	}
 
 
 	/**
 	 * Gets the SiteNodeType definition of this given node
 	 * @return
 	 */
 	private SiteNodeTypeDefinitionVO getSiteNodeTypeDefinition(Integer siteNodeId, Database db) throws SystemException
 	{
 	    String key = "" + siteNodeId;
 		logger.info("key:" + key);
 		SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = (SiteNodeTypeDefinitionVO)CacheController.getCachedObject("pageCacheSiteNodeTypeDefinition", key);
 		if(siteNodeTypeDefinitionVO != null)
 		{
 			logger.info("There was an cached siteNodeTypeDefinitionVO:" + siteNodeTypeDefinitionVO);
 		}
 		else
 		{
 		    
 		    SiteNode siteNode = nodeDeliveryController.getSiteNode(db, this.siteNodeId);
 			if(siteNode == null)
 			    throw new SystemException("There was no page with this id.");
 			
 			if(siteNode.getSiteNodeTypeDefinition() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName().equals(""))
 			{
 			    throw new SystemException("There was no page invoker class assigned to the site node " + siteNode.getName());
 			}
 			
 			siteNodeTypeDefinitionVO = siteNode.getSiteNodeTypeDefinition().getValueObject();
 			
 			CacheController.cacheObject("pageCacheSiteNodeTypeDefinition", key, siteNodeTypeDefinitionVO);
 		}
 		
 		return siteNodeTypeDefinitionVO;
 	}
 
   	private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception 
   	{
 		String url = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL()).getLoginDialogUrl(request, response);
 		
 		return url;
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
     
     public void setRecacheCall(boolean isRecacheCall)
     {
         this.isRecacheCall = isRecacheCall;
     }
     
     public void setCmsUserName(String userName)
     {
     	if(logger.isInfoEnabled())
     		logger.info("userName:" + userName);
     	
         this.getHttpSession().setAttribute("cmsUserName", userName);
     }
     
 }
