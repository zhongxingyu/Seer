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
 
 package org.infoglue.deliver.controllers.kernel.impl.simple;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.filters.FilterConstants;
 import org.infoglue.deliver.controllers.kernel.URLComposer;
 import org.infoglue.deliver.invokers.PageInvoker;
 
 import webwork.action.ActionContext;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lbj
  * Date: 22-01-2004
  * Time: 16:41:17
  * To change this template use Options | File Templates.
  */
 public class BasicURLComposer extends URLComposer
 {
     private final static Logger logger = Logger.getLogger(BasicURLComposer.class.getName());
 
     public BasicURLComposer()
     {
     }
 
     public String composeDigitalAssetUrl(String dnsName, String filename, DeliveryContext deliveryContext)
     {
         String disableEmptyUrls = CmsPropertyHandler.getDisableEmptyUrls();
         if((filename == null || filename.equals("")) && (disableEmptyUrls == null || disableEmptyUrls.equalsIgnoreCase("yes")))
             return "";
             
         String assetUrl = "";
         
         String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
         if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
         	enableNiceURI = "false";
 
         String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
         if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
             useDNSNameInUrls = "false";
 
         if(enableNiceURI.equalsIgnoreCase("true") || useDNSNameInUrls.equalsIgnoreCase("false"))
         {
 	        StringBuffer sb = new StringBuffer(256);
 	        
 	        if(deliveryContext.getUseFullUrl())
 	        {
 		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
 	            int indexOfProtocol = originalUrl.indexOf("://");
 	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
 	            String base = originalUrl.substring(0, indexFirstSlash);
 	            sb.append(base);
 	        }
 	        
 	        String servletContext = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
 	        String digitalAssetPath = CmsPropertyHandler.getDigitalAssetBaseUrl();
 	        if (!digitalAssetPath.startsWith("/"))
 	        	digitalAssetPath = "/" + digitalAssetPath;
 	        
 	        //logger.info("servletContext:" + servletContext);
 	        //logger.info("digitalAssetPath:" + digitalAssetPath);
 	
 	        if(digitalAssetPath.indexOf(servletContext) == -1)
 	        	sb.append(servletContext);
 	        
 	        sb.append(digitalAssetPath);
 	     
 	        sb.append("/").append(filename);
 	        
 	        //logger.info("sb:" + sb);
 	        
 	        assetUrl = sb.toString();
         }
         else
         {
         	String operatingMode = CmsPropertyHandler.getOperatingMode();
 		    String keyword = "";
 		    if(operatingMode.equalsIgnoreCase("0"))
 		        keyword = "working=";
 		    else if(operatingMode.equalsIgnoreCase("2"))
 		        keyword = "preview=";
 		    if(operatingMode.equalsIgnoreCase("3"))
 		        keyword = "live=";
 		    
 		    if(dnsName != null)
 		    {
     		    int startIndex = dnsName.indexOf(keyword);
     		    if(startIndex != -1)
     		    {
     		        int endIndex = dnsName.indexOf(",", startIndex);
         		    if(endIndex > -1)
     		            dnsName = dnsName.substring(startIndex, endIndex);
     		        else
     		            dnsName = dnsName.substring(startIndex);
     		        
     		        dnsName = dnsName.split("=")[1];
     		    }
     		    else
     		    {
     		        int endIndex = dnsName.indexOf(",");
     		        if(endIndex > -1)
     		            dnsName = dnsName.substring(0, endIndex);
     		        else
     		            dnsName = dnsName.substring(0);
     		        
     		    }
 		    }
 
             String context = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
 
             assetUrl = dnsName + context + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + filename;
         }
         
         return assetUrl;
     }
     
     public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException
     {
         /*
         String disableEmptyUrls = CmsPropertyHandler.getDisableEmptyUrls();
         if(filename == null || filename.equals("") && disableEmptyUrls == null || disableEmptyUrls.equalsIgnoreCase("no"))
             return "";
         */
         
     	String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
         if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
         	enableNiceURI = "false";
         
         String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
         if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
             useDNSNameInUrls = "false";
 
         if(enableNiceURI.equalsIgnoreCase("true") && deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") == -1 && !deliveryContext.getDisableNiceUri())
         {
             String context = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
             
             SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db, true);
             SiteNode currentSiteNode = SiteNodeController.getSiteNodeWithId(deliveryContext.getSiteNodeId(), db, true);
     		if(!siteNode.getRepository().getId().equals(currentSiteNode.getRepository().getId()))
     		{
     		    String dnsName = siteNode.getRepository().getDnsName();
     		    logger.info("dnsName:" + dnsName + " for siteNode " + siteNode.getName());
     		    
     		    String operatingMode = CmsPropertyHandler.getOperatingMode();
     		    String keyword = "";
     		    if(operatingMode.equalsIgnoreCase("0"))
     		        keyword = "working=";
     		    else if(operatingMode.equalsIgnoreCase("2"))
     		        keyword = "preview=";
     		    if(operatingMode.equalsIgnoreCase("3"))
     		        keyword = "live=";
 
     		    String repositoryPath = null;
     	    	int pathStartIndex = dnsName.indexOf("path=");
     	    	if(pathStartIndex != -1)
     	    	{
     	    		repositoryPath = dnsName.substring(pathStartIndex + 5);
     	    	}
     	    	logger.info("repositoryPath in constructing new url:" + repositoryPath);    	
 
     		    if(dnsName != null)
     		    {
 	    		    int startIndex = dnsName.indexOf(keyword);
 	    		    if(startIndex != -1)
 	    		    {
 	    		        int endIndex = dnsName.indexOf(",", startIndex);
 	        		    if(endIndex > -1)
 	    		            dnsName = dnsName.substring(startIndex, endIndex);
 	    		        else
 	    		            dnsName = dnsName.substring(startIndex);
 	    		        
 	    		        dnsName = dnsName.split("=")[1];
 	    		    }
 	    		    else
 	    		    {
 	    		        int endIndex = dnsName.indexOf(",");
 	    		        if(endIndex > -1)
 	    		            dnsName = dnsName.substring(0, endIndex);
 	    		        else
 	    		            dnsName = dnsName.substring(0);
 	    		        
 	    		    }
     		    }
     		    
     		    if(repositoryPath != null)
     		    {
         		    if(context.startsWith("/"))
         		    	context = dnsName + context + "/" + repositoryPath;
         		    else
         		     	context = dnsName + "/" + context + "/" + repositoryPath;
     		    }
     		    else
     		    {
         		    if(context.startsWith("/"))
         		    	context = dnsName + context;
         		    else
         		    	context = dnsName + "/" + context;
                 }
     		}
     		else
     		{
     		    String dnsName = siteNode.getRepository().getDnsName();
 
     		    String repositoryPath = null;
     	    	int pathStartIndex = dnsName.indexOf("path=");
     	    	if(pathStartIndex != -1)
     	    	{
     	    		repositoryPath = dnsName.substring(pathStartIndex + 5);
     	    	}
     	    	logger.info("repositoryPath in constructing new url:" + repositoryPath);    	
     	    	
     		    if(repositoryPath != null)
     		    	context = context + "/" + repositoryPath;
     		}
 
             StringBuffer sb = new StringBuffer(256);
 
             if(deliveryContext.getUseFullUrl())
 	        {
 		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
 	            int indexOfProtocol = originalUrl.indexOf("://");
 	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
 	            String base = originalUrl.substring(0, indexFirstSlash);
 	            sb.append(base);
 	        }
 
 	        sb.append(context);
 	        
 	        try 
 			{
 	            sb.append(NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getPageNavigationPath(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext));
 	            
 	            if(sb.toString().endsWith(context))
 	                sb.append("/");
 	            
 	            boolean addedContent = false;
 	            if (contentId != null && contentId.intValue() != -1)
 	            {
 	                sb.append("?contentId=").append(String.valueOf(contentId));
 	                addedContent = true;
 	            }
 
 	            if (languageId != null && languageId.intValue() != -1 && deliveryContext.getLanguageId().intValue() != languageId.intValue())
 	            {
 	                if(addedContent)
 	                    sb.append(getRequestArgumentDelimiter());
 	                else
 	                    sb.append("?");
 	                    
 	                sb.append("languageId=").append(String.valueOf(languageId));
 	            }
 
 	            return (!sb.toString().equals("") ? sb.toString() : "/");
 	        } 
 	        catch (Exception e) 
 			{
 	            e.printStackTrace();
 	        }
 	        return null;
         }
         else
         {           
             if(!useDNSNameInUrls.equalsIgnoreCase("false"))
             {
 	    		if(siteNodeId == null)
 	    			siteNodeId = new Integer(-1);
 	
 	    		if(languageId == null)
 	    			languageId = new Integer(-1);
 	
 	    		if(contentId == null)
 	    			contentId = new Integer(-1);
 	
 	            String arguments = "siteNodeId=" + siteNodeId + getRequestArgumentDelimiter() + "languageId=" + languageId + getRequestArgumentDelimiter() + "contentId=" + contentId;
 
 	            SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db, true);
 	    		String dnsName = CmsPropertyHandler.getWebServerAddress();
 	    		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
 	    			dnsName = siteNode.getRepository().getDnsName();
 
 	        	String operatingMode = CmsPropertyHandler.getOperatingMode();
 			    String keyword = "";
 			    if(operatingMode.equalsIgnoreCase("0"))
 			        keyword = "working=";
 			    else if(operatingMode.equalsIgnoreCase("2"))
 			        keyword = "preview=";
 			    if(operatingMode.equalsIgnoreCase("3"))
 			        keyword = "live=";
 			    
 			    if(dnsName != null)
 			    {
 	    		    int startIndex = dnsName.indexOf(keyword);
 	    		    if(startIndex != -1)
 	    		    {
 	    		        int endIndex = dnsName.indexOf(",", startIndex);
 	        		    if(endIndex > -1)
 	    		            dnsName = dnsName.substring(startIndex, endIndex);
 	    		        else
 	    		            dnsName = dnsName.substring(startIndex);
 	    		        
 	    		        dnsName = dnsName.split("=")[1];
 	    		    }
 	    		    else
 	    		    {
 	    		        int endIndex = dnsName.indexOf(",");
 	    		        if(endIndex > -1)
 	    		            dnsName = dnsName.substring(0, endIndex);
 	    		        else
 	    		            dnsName = dnsName.substring(0);
 	    		        
 	    		    }
 			    }
 
 	            String context = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
 
 	            String url = dnsName + context + "/" + CmsPropertyHandler.getApplicationBaseAction() + "?" + arguments;
 
 				if(deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
 				{
 		            String componentRendererUrl = CmsPropertyHandler.getComponentRendererUrl();
 					if(componentRendererUrl.endsWith("/"))
 					    componentRendererUrl += "/";
 					
 					url = componentRendererUrl + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments;
 				}
 				
 	            //getLogger().info("url:" + url);
 	            
 	            return url;
             }
             else
             {
                 StringBuffer sb = new StringBuffer(256);
                 if(deliveryContext.getUseFullUrl())
     	        {
     		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
     	            int indexOfProtocol = originalUrl.indexOf("://");
     	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
     	            String base = originalUrl.substring(0, indexFirstSlash);
     	            sb.append(base);
     	        }
                 
                 String servletContext = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
     	        
                 if(siteNodeId == null)
 	    			siteNodeId = new Integer(-1);
 	
 	    		if(languageId == null)
 	    			languageId = new Integer(-1);
 	
 	    		if(contentId == null)
 	    			contentId = new Integer(-1);
 	
 	            String arguments = "siteNodeId=" + siteNodeId + getRequestArgumentDelimiter() + "languageId=" + languageId + getRequestArgumentDelimiter() + "contentId=" + contentId;
 	            
 				if(deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
 				{
 				    sb.append(servletContext + "/" + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments);
 				}
 				else
 				{
 				    sb.append(servletContext + "/" + CmsPropertyHandler.getApplicationBaseAction() + "?" + arguments);
 		        }
 	            //getLogger().info("url:" + url);
 
 	            return sb.toString();            
             }
         }
     }
 
     public String composePageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException
     {
         String pageUrl = composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
 
         String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
         if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
         	enableNiceURI = "false";
         
         if(enableNiceURI.equalsIgnoreCase("true") && !deliveryContext.getDisableNiceUri())
         {
             if (pageUrl.indexOf("?") == -1) 
 	        {
 	            pageUrl += "?languageId=" + String.valueOf(languageId);
 	        } 
 	        else 
 	        {
 	        	if(pageUrl.indexOf("languageId=") == -1)
 	        		pageUrl += getRequestArgumentDelimiter() + "languageId=" + String.valueOf(languageId);
 	        }
         }
         
         return pageUrl;
     }
 
     private String getRequestArgumentDelimiter()
     {
         String requestArgumentDelimiter = CmsPropertyHandler.getRequestArgumentDelimiter();
         if(requestArgumentDelimiter == null || requestArgumentDelimiter.equalsIgnoreCase("") || (!requestArgumentDelimiter.equalsIgnoreCase("&") && !requestArgumentDelimiter.equalsIgnoreCase("&amp;")))
             requestArgumentDelimiter = "&";
 
         return requestArgumentDelimiter;
     }
 
     public String composePageBaseUrl(String dnsName)
     {
         String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
         if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
             useDNSNameInUrls = "false";
 
         if(!useDNSNameInUrls.equalsIgnoreCase("false"))
         {
         	String operatingMode = CmsPropertyHandler.getOperatingMode();
 		    String keyword = "";
 		    if(operatingMode.equalsIgnoreCase("0"))
 		        keyword = "working=";
 		    else if(operatingMode.equalsIgnoreCase("2"))
 		        keyword = "preview=";
 		    if(operatingMode.equalsIgnoreCase("3"))
 		        keyword = "live=";
 		    
 		    if(dnsName != null)
 		    {
     		    int startIndex = dnsName.indexOf(keyword);
     		    if(startIndex != -1)
     		    {
     		        int endIndex = dnsName.indexOf(",", startIndex);
         		    if(endIndex > -1)
     		            dnsName = dnsName.substring(startIndex, endIndex);
     		        else
     		            dnsName = dnsName.substring(startIndex);
     		        
     		        dnsName = dnsName.split("=")[1];
     		    }
     		    else
     		    {
     		        int endIndex = dnsName.indexOf(",");
     		        if(endIndex > -1)
     		            dnsName = dnsName.substring(0, endIndex);
     		        else
     		            dnsName = dnsName.substring(0);
     		        
     		    }
 		    }
 
             String context = CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT);
         	
             return dnsName + context + "/" + CmsPropertyHandler.getApplicationBaseAction();
         }
         
         if(ActionContext.getRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
 		{
             //String componentRendererUrl = CmsPropertyHandler.getComponentRendererUrl();
 		    //if(componentRendererUrl.endsWith("/"))
 		    //   componentRendererUrl += "/";
 			
 			return "/" + CmsPropertyHandler.getComponentRendererUrl();
 		}
         
         return "/" + CmsPropertyHandler.getApplicationBaseAction();
     }
 
 
 } 
