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
 
 package org.infoglue.deliver.invokers;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.PageTemplateController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.io.FileHelper;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.StringManager;
 import org.infoglue.cms.util.StringManagerFactory;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentBinding;
 import org.infoglue.deliver.applications.databeans.ComponentProperty;
 import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;
 import org.infoglue.deliver.applications.databeans.ComponentTask;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.applications.databeans.WebPage;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DecoratedComponentLogic;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.Timer;
 import org.infoglue.deliver.util.VelocityTemplateProcessor;
 
 /**
 * @author Mattias Bogeblad
 *
 * This class delivers a normal html page by using the component-based method but also decorates it
 * so it can be used by the structure tool to manage the page components.
 */
 
 public class DecoratedComponentBasedHTMLPageInvoker extends ComponentBasedHTMLPageInvoker
 {
 	private final static DOMBuilder domBuilder = new DOMBuilder();
 	private final static VisualFormatter formatter = new VisualFormatter();
 	
     private final static Logger logger = Logger.getLogger(DecoratedComponentBasedHTMLPageInvoker.class.getName());
 
 	private String propertiesDivs 	= "";
 	private String tasksDivs 		= "";
 	
 	/**
 	 * This is the method that will render the page. It uses the new component based structure. 
 	 */ 
 	
 	public void invokePage() throws SystemException, Exception
 	{
 		Timer timer = new Timer();
 		timer.setActive(false);
 		
 		String decoratePageTemplate = "";
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
 		
 		timer.printElapsedTime("Initialized controllers");
 		
 		Integer repositoryId = nodeDeliveryController.getSiteNode(getDatabase(), this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		//logger.info("componentXML:" + componentXML);
 		
 		componentXML = appendPagePartTemplates(componentXML);
 		
 		timer.printElapsedTime("After getPageComponentsString");
 		
 		Timer decoratorTimer = new Timer();
 		decoratorTimer.setActive(false);
 
 		InfoGlueComponent baseComponent = null;
 		
 		if(componentXML == null || componentXML.length() == 0)
 		{
 			decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		}
 		else
 		{
 		    Document document = null;
 		    try
 		    {
 		        document = domBuilder.getDocument(componentXML);
 		    }
 		    catch(Exception e)
 		    {
 		        throw new SystemException("There was a problem parsing the component structure on the page. Could be invalid XML in the ComponentStructure attribute. Message:" + e.getMessage(), e);
 		    }
 		    
 			decoratorTimer.printElapsedTime("After reading document");
 			
    			List unsortedPageComponents = new ArrayList();
 			List pageComponents = getPageComponents(getDatabase(), componentXML, document.getRootElement(), "base", this.getTemplateController(), null, unsortedPageComponents);
 
 			preProcessComponents(nodeDeliveryController, repositoryId, unsortedPageComponents, pageComponents);
 			
 			if(pageComponents.size() > 0)
 			{
 				baseComponent = (InfoGlueComponent)pageComponents.get(0);
 			}
 
 			decoratorTimer.printElapsedTime("After getting basecomponent");
 			
 			if(baseComponent == null)
 			{
 				decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 			}
 			else
 			{
 				//if(this.getDeliveryContext().getShowSimple() == true)
 			    //{
 			    //    decoratePageTemplate = showSimplePageStructure(this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), baseComponent);
 			    //}
 			    //else
 			    //{
 				    ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
 					decoratePageTemplate = decorateComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId()/*, metaInfoContentVO.getId()*/);
 					decoratePageTemplate = decorateTemplate(this.getTemplateController(), decoratePageTemplate, this.getDeliveryContext(), baseComponent);
 				//}
 			}
 		}
 		
 		timer.printElapsedTime("After main decoration");
 		
 		//TODO - TEST
 		decoratePageTemplate += propertiesDivs + tasksDivs;
 		
 		/*
 		Map context = getDefaultContext();
 
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		context.put("componentEditorUrl", componentEditorUrl);
 		boolean oldUseFullUrl = this.getTemplateController().getDeliveryContext().getUseFullUrl();
 		this.getTemplateController().getDeliveryContext().setUseFullUrl(true);
 		context.put("currentUrl", URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8"));
 		context.put("contextName", this.getRequest().getContextPath());
 		
 		this.getTemplateController().getDeliveryContext().setUseFullUrl(oldUseFullUrl);
 		StringWriter cacheString = new StringWriter();
 		PrintWriter cachedStream = new PrintWriter(cacheString);
 		
 		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, decoratePageTemplate, false, baseComponent);
 
 		String pageString = cacheString.toString();
 		*/
 		String pageString = decoratePageTemplate;
 		//pageString = decorateHeadAndPageWithVarsFromComponents(pageString);
 
 		this.setPageString(pageString);
 		
 		timer.printElapsedTime("End invokePage");
 	}
 	
 	 /**
 	  * This method prints out the first template dialog.
 	  */
 
 	 private String showInitialBindingDialog(Integer siteNodeId, Integer languageId, Integer contentId)
 	 {
 		 String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		 String url = "javascript:window.open('" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?eee=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&specifyBaseTemplate=true&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', 'BaseTemplate', 'width=600,height=700,left=50,top=50,toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes');";
 		 
 		 String pageTemplateHTML = " or choose a page template below.<br><br>";
 		 
 	     boolean foundPageTemplate = false;
 
 	     try
 		 {
 	    	 SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
 	    	 LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId());
 	    	 
 	    	 InfoGluePrincipal principal = this.getTemplateController().getPrincipal();
 	    	 String cmsUserName = (String)this.getTemplateController().getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    	 if(cmsUserName != null)
 	    		 principal = this.getTemplateController().getPrincipal(cmsUserName);
 		    
 		     List sortedPageTemplates = PageTemplateController.getController().getPageTemplates(principal, masterLanguageVO.getId());
 			 Iterator sortedPageTemplatesIterator = sortedPageTemplates.iterator();
 			 int index = 0;
 			 pageTemplateHTML += "<table border=\"0\" width=\"80%\" cellspacing=\"0\"><tr>";
 			 
 		     while(sortedPageTemplatesIterator.hasNext())
 			 {
 			     ContentVO contentVO = (ContentVO)sortedPageTemplatesIterator.next();
 			     ContentVersionVO contentVersionVO = this.getTemplateController().getContentVersion(contentVO.getId(), LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId(), false);
 			     if(contentVersionVO != null)
 			     {
 				     String imageUrl = this.getTemplateController().getAssetUrl(contentVO.getId(), "thumbnail");
 				     if(imageUrl == null || imageUrl.equals(""))
 				         imageUrl = this.getRequest().getContextPath() + "/images/undefinedPageTemplate.jpg";
 				 
 				     pageTemplateHTML += "<td style=\"font-family:verdana, sans-serif; font-size:10px; border: 1px solid #C2D0E2; padding: 5px 5px 5px 5px;\" valign=\"bottom\" align=\"center\"><a href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!addPageTemplate.action?repositoryId=" + contentVO.getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&pageTemplateContentId=" + contentVO.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + imageUrl + "\" border=\"0\" style=\"width: 100px;\"><br>";
 				     pageTemplateHTML += contentVO.getName() + "</a>";
 				     pageTemplateHTML += "</td>";	
 
 				     index++;
 				     if(index >= 5)
 				     {
 				    	 index = 0;
 				    	 pageTemplateHTML += "</tr><tr>";
 				     }
 				     
 				     foundPageTemplate = true;
 			     }
 			 }
 			 pageTemplateHTML += "</tr></table>";
 
 		 }
 		 catch(Exception e)
 		 {
 		     logger.warn("A problem arouse when getting the page templates:" + e.getMessage(), e);
 		 }
 		 
 		 this.getTemplateController().getDeliveryContext().setContentType("text/html");
 		 this.getTemplateController().getDeliveryContext().setDisablePageCache(true);
 		 return "<html><body style=\"font-family:verdana, sans-serif; font-size:10px;\">The page has no base component assigned yet. Click <a href=\"" + url + "\">here</a> to assign one" + (foundPageTemplate ? pageTemplateHTML : "") + "</body></html>";
 	 }
 
 
 	/**
 	 * This method adds the neccessairy html to a template to make it right-clickable.
 	 */	
 
 	private String decorateTemplate(TemplateController templateController, String template, DeliveryContext deliveryContext, InfoGlueComponent component)
 	{
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String decoratedTemplate = template;
 		
 		try
 		{
 			String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 			InfoGluePrincipal principal = templateController.getPrincipal();
 		    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 		    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
 			    principal = templateController.getPrincipal(cmsUserName);
 
 		    boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 			boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + component.getSlotName());
 			boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + component.getContentId() + "_" + component.getSlotName());
 			boolean hasAccessToChangeComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeComponent", "" + component.getContentId() + "_" + component.getSlotName());
 			boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
 
 		    boolean hasSubmitToPublishAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.SubmitToPublish", "");
 		    boolean hasPageStructureAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.PageStructure", "");
 		    boolean hasOpenInNewWindowAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.OpenInNewWindow", "");
 		    boolean hasViewSourceAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ViewSource", "");
 
 		    String extraHeader 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/pageComponentEditorHeader.vm"));
 		    String extraBody 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/pageComponentEditorBody.vm"));
 
 			boolean oldUseFullUrl = this.getTemplateController().getDeliveryContext().getUseFullUrl();
 			this.getTemplateController().getDeliveryContext().setUseFullUrl(true);
 			
 			String parameters = "repositoryId=" + templateController.getSiteNode().getRepositoryId() + "&siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + this.getRequest().getParameter("activatedComponentId") + "&componentContentId=" + this.getRequest().getParameter("componentContentId") + "&showSimple=false&showLegend=false&originalUrl=" + URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8");
 
 			StringBuffer path = getPagePathAsCommaseparatedIds(templateController);
 			
 			extraHeader = extraHeader.replaceAll("\\$\\{focusElementId\\}", "" + this.getRequest().getParameter("focusElementId"));
 			extraHeader = extraHeader.replaceAll("\\$\\{contextName\\}", this.getRequest().getContextPath());
 			extraHeader = extraHeader.replaceAll("\\$\\{componentEditorUrl\\}", componentEditorUrl);
 			if(principal.getName().equalsIgnoreCase(CmsPropertyHandler.getAnonymousUser()))
 				extraHeader = extraHeader.replaceAll("\\$\\{limitedUserWarning\\}", "alert('User was " + principal.getName() + "');");
 			else
 				extraHeader = extraHeader.replaceAll("\\$\\{limitedUserWarning\\}", "");
 			extraHeader = extraHeader.replaceAll("\\$\\{currentUrl\\}", URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8"));
 			extraHeader = extraHeader.replaceAll("\\$\\{activatedComponentId\\}", "" + this.getRequest().getParameter("activatedComponentId"));
 			extraHeader = extraHeader.replaceAll("\\$\\{parameters\\}", parameters);
 			extraHeader = extraHeader.replaceAll("\\$\\{siteNodeId\\}", "" + templateController.getSiteNodeId());
 			extraHeader = extraHeader.replaceAll("\\$\\{languageId\\}", "" + templateController.getLanguageId());
 			extraHeader = extraHeader.replaceAll("\\$\\{parentSiteNodeId\\}", "" + templateController.getSiteNode().getParentSiteNodeId());
 			extraHeader = extraHeader.replaceAll("\\$\\{repositoryId\\}", "" + templateController.getSiteNode().getRepositoryId());
 			extraHeader = extraHeader.replaceAll("\\$\\{path\\}", "" + path.substring(1));
			extraHeader = extraHeader.replaceAll("\\$\\{userPrefferredLanguageCode\\}", "" + CmsPropertyHandler.getPreferredLanguageCode(principal.getName()));
			
 			this.getTemplateController().getDeliveryContext().setUseFullUrl(oldUseFullUrl);
 
 		    String changeUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponentsForChange.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=base&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 		    extraBody = extraBody + "<script type=\"text/javascript\">initializeComponentEventHandler('base0_" + component.getId() + "Comp', '" + component.getId() + "', '', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=base&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "','" + changeUrl + "');</script>";
 
 		    Locale locale = templateController.getLocaleAvailableInTool(principal);
 		    
 			String submitToPublishHTML = getLocalizedString(locale, "deliver.editOnSight.submitToPublish");
 		    String addComponentHTML = getLocalizedString(locale, "deliver.editOnSight.addComponentHTML");
 			String deleteComponentHTML = getLocalizedString(locale, "deliver.editOnSight.deleteComponentHTML");
 			String changeComponentHTML = getLocalizedString(locale, "deliver.editOnSight.changeComponentHTML");
 			String accessRightsHTML = getLocalizedString(locale, "deliver.editOnSight.accessRightsHTML");
 			String pageComponentsHTML = getLocalizedString(locale, "deliver.editOnSight.pageComponentsHTML");
 			String viewSourceHTML = getLocalizedString(locale, "deliver.editOnSight.viewSourceHTML");
 			String componentEditorInNewWindowHTML = getLocalizedString(locale, "deliver.editOnSight.componentEditorInNewWindowHTML");
 			String savePageTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePageTemplateHTML");
 			String savePagePartTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePagePartTemplateHTML");
 			String editHTML = getLocalizedString(locale, "deliver.editOnSight.editHTML");
 			String editInlineHTML = getLocalizedString(locale, "deliver.editOnSight.editContentInlineLabel");
 			String propertiesHTML = getLocalizedString(locale, "deliver.editOnSight.propertiesHTML");
 
 			String saveTemplateUrl = "saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(deliveryContext.getSiteNodeId()).getMetaInfoContentId() + "');";
 			String savePartTemplateUrl = "savePartComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(deliveryContext.getSiteNodeId()).getMetaInfoContentId() + "');";
 			if(!hasSaveTemplateAccess)
 			{
 				saveTemplateUrl = "alert('Not authorized to save template');";
 				savePartTemplateUrl = "alert('Not authorized to save part template');";
 			}
 			
 			extraBody = extraBody.replaceAll("\\$siteNodeId", "" + templateController.getSiteNodeId());
 			extraBody = extraBody.replaceAll("\\$repositoryId", "" + templateController.getSiteNode().getRepositoryId());
 			extraBody = extraBody.replaceAll("\\$originalFullURL", URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8"));
 			
 			extraBody = extraBody.replaceAll("\\$editHTML", editHTML);
 			extraBody = extraBody.replaceAll("\\$submitToPublishHTML", submitToPublishHTML);
 			extraBody = extraBody.replaceAll("\\$addComponentHTML", addComponentHTML);
 			extraBody = extraBody.replaceAll("\\$deleteComponentHTML", deleteComponentHTML);
 			extraBody = extraBody.replaceAll("\\$changeComponentHTML", changeComponentHTML);
 		    extraBody = extraBody.replaceAll("\\$accessRightsHTML", accessRightsHTML);
 		    
 		    extraBody = extraBody.replaceAll("\\$pageComponents", pageComponentsHTML);
 		    extraBody = extraBody.replaceAll("\\$componentEditorInNewWindowHTML", componentEditorInNewWindowHTML);
 		    extraBody = extraBody.replaceAll("\\$savePageTemplateHTML", savePageTemplateHTML);
 		    extraBody = extraBody.replaceAll("\\$savePagePartTemplateHTML", savePagePartTemplateHTML);
 		    extraBody = extraBody.replaceAll("\\$saveTemplateUrl", saveTemplateUrl);
 		    extraBody = extraBody.replaceAll("\\$savePartTemplateUrl", savePartTemplateUrl);
 		    extraBody = extraBody.replaceAll("\\$viewSource", viewSourceHTML);
 			extraBody = extraBody.replaceAll("\\$propertiesHTML", propertiesHTML);
 		    
 		    extraBody = extraBody.replaceAll("\\$addComponentJavascript", "var hasAccessToAddComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToAddComponent + ";");
 		    extraBody = extraBody.replaceAll("\\$deleteComponentJavascript", "var hasAccessToDeleteComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToDeleteComponent + ";");
 		    extraBody = extraBody.replaceAll("\\$changeComponentJavascript", "var hasAccessToChangeComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToChangeComponent + ";");
 		    extraBody = extraBody.replaceAll("\\$changeAccessJavascript", "var hasAccessToAccessRights" + " = " + hasAccessToAccessRights + ";");
 		    
 		    extraBody = extraBody.replaceAll("\\$submitToPublishJavascript", "var hasAccessToSubmitToPublish = " + hasSubmitToPublishAccess + ";");
 		    extraBody = extraBody.replaceAll("\\$pageStructureJavascript", "var hasPageStructureAccess = " + hasPageStructureAccess + ";");
 		    extraBody = extraBody.replaceAll("\\$openInNewWindowJavascript", "var hasOpenInNewWindowAccess = " + hasOpenInNewWindowAccess + ";");
 		    extraBody = extraBody.replaceAll("\\$allowViewSourceJavascript", "var hasAccessToViewSource = " + hasViewSourceAccess + ";");
 
 		    //List tasks = getTasks();
 			//component.setTasks(tasks);
 			
 			//String tasks = templateController.getContentAttribute(component.getContentId(), "ComponentTasks", true);
 			
 			/*
 			Map context = new HashMap();
 			context.put("templateLogic", templateController);
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, extraBody);
 			extraBody = cacheString.toString();
 			*/
 			
 			//extraHeader.replaceAll()
 			
 			timer.printElapsedTime("Read files");
 			
 			StringBuffer modifiedTemplate = new StringBuffer(template);
 			
 			//Adding stuff in the header
 			int indexOfHeadEndTag = modifiedTemplate.indexOf("</head");
 			if(indexOfHeadEndTag == -1)
 				indexOfHeadEndTag = modifiedTemplate.indexOf("</HEAD");
 			
 			if(indexOfHeadEndTag > -1)
 			{
 				modifiedTemplate = modifiedTemplate.replace(indexOfHeadEndTag, modifiedTemplate.indexOf(">", indexOfHeadEndTag) + 1, extraHeader);
 			}
 			else
 			{
 				int indexOfHTMLStartTag = modifiedTemplate.indexOf("<html");
 				if(indexOfHTMLStartTag == -1)
 					indexOfHTMLStartTag = modifiedTemplate.indexOf("<HTML");
 		
 				if(indexOfHTMLStartTag > -1)
 				{
 					modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfHTMLStartTag) + 1, "<head>" + extraHeader);
 				}
 				else
 				{
 					logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct header.");
 				}
 			}
 
 			timer.printElapsedTime("Header handled");
 
 			//Adding stuff in the body	
 			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
 			if(indexOfBodyStartTag == -1)
 				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
 			
 			if(indexOfBodyStartTag > -1)
 			{
 			    //String pageComponentStructureDiv = "";
 				String pageComponentStructureDiv = getPageComponentStructureDiv(templateController, deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), component);
 				timer.printElapsedTime("pageComponentStructureDiv");
 				String componentPaletteDiv = getComponentPaletteDiv(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), templateController);
 				//String componentPaletteDiv = "";
 				timer.printElapsedTime("componentPaletteDiv");
 				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody + pageComponentStructureDiv + componentPaletteDiv);
 			}
 			else
 			{
 				logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
 			}
 			
 			timer.printElapsedTime("Body handled");
 
 			decoratedTemplate = modifiedTemplate.toString();
 		}
 		catch(Exception e)
 		{
 			logger.warn("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
 		}
 		
 		return decoratedTemplate;
 	}
 
 	private StringBuffer getPagePathAsCommaseparatedIds(TemplateController templateController)
 	{
 		StringBuffer path = new StringBuffer("");
 		
 		SiteNodeVO currentSiteNode = templateController.getSiteNode();
 		while(currentSiteNode != null)
 		{
 			path.insert(0, "," + currentSiteNode.getId().toString());
 			if(currentSiteNode.getParentSiteNodeId() != null)
 				currentSiteNode = templateController.getSiteNode(currentSiteNode.getParentSiteNodeId());
 			else
 				currentSiteNode = null;
 		}
 		return path;
 	}
 
    
 	private String decorateComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId/*, Integer metainfoContentId*/) throws Exception
 	{
 		String decoratedComponent = "";
 		
 		//logger.info("decorateComponent.contentId:" + contentId);
 
 		//logger.info("decorateComponent:" + component.getName());
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			String componentString = getComponentString(templateController, component.getContentId(), component); 
 
 			if(component.getParentComponent() == null && templateController.getDeliveryContext().getShowSimple())
 			{
 			    templateController.getDeliveryContext().setContentType("text/html");
 			    templateController.getDeliveryContext().setDisablePageCache(true);
 			    componentString = "<html><head></head><body onload=\"toggleDiv('pageComponents');\">" + componentString + "</body></html>";
 			}
 			
 			templateController.setComponentLogic(new DecoratedComponentLogic(templateController, component));
 			Map context = super.getDefaultContext();
 			context.put("templateLogic", templateController);
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString, false, component);
 			componentString = cacheString.toString();
 	
 			int bodyIndex = componentString.indexOf("<body");
 			if(bodyIndex == -1)
 				bodyIndex = componentString.indexOf("<BODY");
 		
 			if(component.getParentComponent() == null && bodyIndex > -1)
 			{
 				String onContextMenu = " id=\"base0_0Comp\" onload=\"javascript:setToolbarInitialPosition();\"";
 				if(templateController.getDeliveryContext().getShowSimple())
 					onContextMenu = " id=\"base0_0Comp\" onload=\"javascript:setToolbarInitialPosition();\"";
 				
 				
 				StringBuffer sb = new StringBuffer(componentString);
 				sb.insert(bodyIndex + 5, onContextMenu);
 				componentString = sb.toString();
 
 				Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, component.getId(), component.getContentId(), componentPropertiesDocument, component);
 
 				Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, component, 0, 1, componentTasksDocument, templateController);
 			}
 	
 			int offset = 0;
 			int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			//logger.info("slotStartIndex:" + slotStartIndex);
 			while(slotStartIndex > -1)
 			{
 				decoratedComponent += componentString.substring(offset, slotStartIndex);
 				int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 				
 				String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
 				String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 				
 				Slot slotBean = new Slot();
 			    slotBean.setId(id);
 
 			    String[] allowedComponentNamesArray = null;
 			    int allowedComponentNamesIndex = slot.indexOf(" allowedComponentNames");
 				if(allowedComponentNamesIndex > -1)
 				{    
 				    String allowedComponentNames = slot.substring(allowedComponentNamesIndex + 24, slot.indexOf("\"", allowedComponentNamesIndex + 24));
 				    allowedComponentNamesArray = allowedComponentNames.split(",");
 				    //logger.info("allowedComponentNamesArray:" + allowedComponentNamesArray.length);
 				    slotBean.setAllowedComponentsArray(allowedComponentNamesArray);
 				}
 
 				String[] disallowedComponentNamesArray = null;
 				int disallowedComponentNamesIndex = slot.indexOf(" disallowedComponentNames");
 				if(disallowedComponentNamesIndex > -1)
 				{    
 				    String disallowedComponentNames = slot.substring(disallowedComponentNamesIndex + 27, slot.indexOf("\"", disallowedComponentNamesIndex + 27));
 				    disallowedComponentNamesArray = disallowedComponentNames.split(",");
 				    //logger.info("disallowedComponentNamesArray:" + disallowedComponentNamesArray.length);
 				    slotBean.setDisallowedComponentsArray(disallowedComponentNamesArray);
 				}
 
 				boolean inherit = true;
 				int inheritIndex = slot.indexOf("inherit");
 				if(inheritIndex > -1)
 				{    
 				    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
 				    inherit = Boolean.parseBoolean(inheritString);
 				}
 				slotBean.setInherit(inherit);
 				
 				boolean disableAccessControl = false;
 				int disableAccessControlIndex = slot.indexOf("disableAccessControl");
 				if(disableAccessControlIndex > -1)
 				{    
 				    String disableAccessControlString = slot.substring(disableAccessControlIndex + "disableAccessControl".length() + 2, slot.indexOf("\"", disableAccessControlIndex + "disableAccessControl".length() + 2));
 				    disableAccessControl = Boolean.parseBoolean(disableAccessControlString);
 				}
 
 				String addComponentText = null;
 				int addComponentTextIndex = slot.indexOf("addComponentText");
 				if(addComponentTextIndex > -1)
 				{    
 				    addComponentText = slot.substring(addComponentTextIndex + "addComponentText".length() + 2, slot.indexOf("\"", addComponentTextIndex + "addComponentText".length() + 2));
 				}
 
 				String addComponentLinkHTML = null;
 				int addComponentLinkHTMLIndex = slot.indexOf("addComponentLinkHTML");
 				if(addComponentLinkHTMLIndex > -1)
 				{    
 				    addComponentLinkHTML = slot.substring(addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2, slot.indexOf("\"", addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2));
 				}
 
 				int allowedNumberOfComponentsInt = -1;
 				int allowedNumberOfComponentsIndex = slot.indexOf("allowedNumberOfComponents");
 				if(allowedNumberOfComponentsIndex > -1)
 				{    
 					String allowedNumberOfComponents = slot.substring(allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2, slot.indexOf("\"", allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2));
 					try
 					{
 						allowedNumberOfComponentsInt = new Integer(allowedNumberOfComponents);
 					}
 					catch (Exception e) 
 					{
 						allowedNumberOfComponentsInt = -1;
 					}
 				}
 
 				slotBean.setDisableAccessControl(disableAccessControl);
 				slotBean.setAddComponentLinkHTML(addComponentLinkHTML);
 			    slotBean.setAddComponentText(addComponentText);
 			    slotBean.setAllowedNumberOfComponents(new Integer(allowedNumberOfComponentsInt));
 				component.setContainerSlot(slotBean);
 				
 				String subComponentString = "";
 				
 				//TODO - test
 				if(component.getIsInherited())
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"inheritedComponentDiv\");\">";
 				else
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"componentDiv\" onmouseup=\"javascript:assignComponent('" + siteNodeId + "', '" + languageId + "', '" + contentId + "', '" + component.getId() + "', '" + id + "', '" + false + "', '" + slotBean.getAllowedComponentsArrayAsUrlEncodedString() + "', '" + slotBean.getDisallowedComponentsArrayAsUrlEncodedString() + "', '');\">";
 				    
 				List subComponents = getInheritedComponents(getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);
 
 			    InfoGluePrincipal principal = templateController.getPrincipal();
 			    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 			    if(cmsUserName != null)
 				    principal = templateController.getPrincipal(cmsUserName);
 
 				String clickToAddHTML = "";
 				boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + id);
 				if(slotBean.getDisableAccessControl())
 					hasAccessToAddComponent = true;
 				
 			    boolean hasMaxComponents = false;
 				if(component.getSlotList() != null)
 				{
 					Iterator slotListIterator = component.getSlotList().iterator();
 					while(slotListIterator.hasNext())
 					{
 						Slot parentSlot = (Slot)slotListIterator.next();
 						if(parentSlot.getId().equalsIgnoreCase(id))
 						{
 							if(parentSlot.getAllowedNumberOfComponents() != -1 && parentSlot.getComponents().size() >= parentSlot.getAllowedNumberOfComponents())
 								hasMaxComponents = true;
 						}
 					}
 				}
 				
 				if(hasAccessToAddComponent && !hasMaxComponents)
 				{
 					if(slotBean.getAddComponentText() != null)
 					{
 						clickToAddHTML = slotBean.getAddComponentText();
 					}
 					else
 					{
 						Locale locale = templateController.getLocaleAvailableInTool(principal);
 						clickToAddHTML = getLocalizedString(locale, "deliver.editOnSight.slotInstructionHTML");
 					}
 				}
 				
 				//logger.info("subComponents for " + id + ":" + subComponents);
 				if(subComponents != null && subComponents.size() > 0)
 				{
 					//logger.info("SUBCOMPONENTS:" + subComponents.size());
 					int index = 0;
 					Iterator subComponentsIterator = subComponents.iterator();
 					while(subComponentsIterator.hasNext())
 					{
 						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 						if(subComponent != null)
 						{
 							component.getComponents().put(subComponent.getSlotName(), subComponent);
 							if(subComponent.getIsInherited())
 							{
 								//logger.info("Inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								if(!this.getTemplateController().getDeliveryContext().getShowSimple())
 								    subComponentString += "<span id=\""+ id + index + "Comp\" class=\"inheritedslot\">" + childComponentsString + "</span>";
 								else
 								    subComponentString += childComponentsString;
 								    
 								Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), subComponent.getContentId(), componentPropertiesDocument, subComponent);
 								
 								Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, index, subComponents.size() - 1, componentTasksDocument, templateController);
 								
 							}
 							else
 							{
 								//logger.info("Not inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								//logger.info("childComponentsString:" + childComponentsString);
 								
 								if(!this.getTemplateController().getDeliveryContext().getShowSimple())
 								{    
 								    String allowedComponentNamesAsEncodedString = null;
 								    String disallowedComponentNamesAsEncodedString = null;
 
 								    for(int i=0; i < subComponent.getParentComponent().getSlotList().size(); i++)
 								    {
 								        Slot subSlotBean = (Slot)subComponent.getParentComponent().getSlotList().get(i);
 								        
 								        if(subSlotBean.getId() != null && subSlotBean.getId().equals(subComponent.getSlotName()))
 								        {
 								            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 								            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
 								            subComponent.setContainerSlot(subSlotBean);
 								        }
 								    }
 
 								    String changeUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponentsForChange.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&componentId=" + subComponent.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&AAAA=1")  + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "&AAAA=1");
 								    subComponentString += "<span id=\""+ id + index + "_" + subComponent.getId() + "Comp\">" + childComponentsString + "<script type=\"text/javascript\">initializeComponentEventHandler('" + id + index + "_" + subComponent.getId() + "Comp', '" + subComponent.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?aa=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&AAAA=1")  + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "&AAAA=1") + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + subComponent.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "','" + changeUrl + "');</script></span>";
 								}
 								else
 								{
 								    subComponentString += childComponentsString;
 								}
 								
 								Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), subComponent.getContentId(), componentPropertiesDocument, subComponent);
 								
 								Document componentTasksDocument = getComponentTasksDOM4JDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, index, subComponents.size() - 1, componentTasksDocument, templateController);
 							}
 						}
 						index++;
 					}
 					
 					if(component.getContainerSlot().getAddComponentLinkHTML() != null && !component.getIsInherited())
 					{
 					    String allowedComponentNamesAsEncodedString = null;
 					    String disallowedComponentNamesAsEncodedString = null;
 					    
 					    for(int i=0; i < component.getSlotList().size(); i++)
 					    {
 					        Slot subSlotBean = (Slot)component.getSlotList().get(i);
 					        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
 					        {
 					            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 					            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
 					        }
 					    }
 
 						String linkUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?BBB=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&BBBB=1") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "&BBBB=1");
 						subComponentString += "" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl);
 					}
 					else
 					{
 						subComponentString += "" + clickToAddHTML;
 					}
 				}
 				else
 				{
 					if(component.getContainerSlot().getAddComponentLinkHTML() != null && !component.getIsInherited())
 					{
 					    String allowedComponentNamesAsEncodedString = null;
 					    String disallowedComponentNamesAsEncodedString = null;
 					    
 					    for(int i=0; i < component.getSlotList().size(); i++)
 					    {
 					        Slot subSlotBean = (Slot)component.getSlotList().get(i);
 					        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
 					        {
 					            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 					            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
 					        }
 					    }
 
 						String linkUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?BBB=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&BBBB=1") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "&BBBB=1");
 						subComponentString += "" + component.getContainerSlot().getAddComponentLinkHTML().replaceAll("\\$linkUrl", linkUrl);
 					}
 					else
 					{
 						subComponentString += "" + clickToAddHTML;
 					}
 				}
 				
 				if(!component.getIsInherited())
 				{
 				    String allowedComponentNamesAsEncodedString = null;
 				    String disallowedComponentNamesAsEncodedString = null;
 				    
 				    for(int i=0; i < component.getSlotList().size(); i++)
 				    {
 				        Slot subSlotBean = (Slot)component.getSlotList().get(i);
 				        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
 				        {
 				            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 				            disallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
 				        }
 				    }
 
 				    subComponentString += "<script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + "_" + id + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?BBB=1&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + (contentId == null ? "-1" : contentId) + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&BBBB=1") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "&BBBB=1") + "', '', '', '" + id + "', '" + component.getContentId() + "');</script></div>";
 				}
 				else
 				    subComponentString += "</div>";
 				  
 				/**
 				 * 
 				 */
 
 				boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 				boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + component.getContentId() + "_" + id);
 				boolean hasAccessToChangeComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeComponent", "" + component.getContentId() + "_" + id);
 				if(slotBean.getDisableAccessControl())
 				{
 					hasAccessToDeleteComponent = true;
 				}
 				
 			    StringBuffer sb = new StringBuffer();
 			    sb.append("<script type=\"text/javascript\">");
 				sb.append("hasAccessToAddComponent" + component.getId() + "_" + id.replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToAddComponent + ";");
 				sb.append("hasAccessToChangeComponent" + component.getId() + "_" + id.replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToChangeComponent + ";");
 				sb.append("hasAccessToAccessRights = " + hasAccessToAccessRights + ";");
 				sb.append("</script>");
 
 				subComponentString += sb.toString();
 			    /**
 			     * 
 			     */
 			    
 				decoratedComponent += subComponentString;
 							
 				offset = slotStopIndex + 10;
 				slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			}
 			
 			//logger.info("offset:" + offset);
 			decoratedComponent += componentString.substring(offset);
 		}
 		catch(Exception e)
 		{		
 			logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 		}
 		
 		return decoratedComponent;
 	}
 
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentPropertiesDiv(TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, Integer componentContentId, Document document, InfoGlueComponent component) throws Exception
 	{	
 	    if(templateController.getRequestParameter("skipPropertiesDiv") != null && templateController.getRequestParameter("skipPropertiesDiv").equalsIgnoreCase("true"))
 	        return "";
 
 	    StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		InfoGluePrincipal principal = templateController.getPrincipal();
 	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
 		    principal = templateController.getPrincipal(cmsUserName);
 
 		//Locale locale = templateController.getLocale();
 	    Locale locale = templateController.getLocaleAvailableInTool(principal);
 
 		timer.printElapsedTime("After locale");
 	    
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		/*
 		String formsEncoding = CmsPropertyHandler.getFormsEncoding();
 		String acceptCharset = "";
 		if(formsEncoding != null && formsEncoding.length() > 0)
 		    acceptCharset = "accept-charset=\"" + formsEncoding + "\"";
 		*/
 		String componentName = component.getName();
 		if(componentName.length() > 20) 
 			componentName = componentName.substring(0, 20) + "...";
 		
 		String slotName = component.getSlotName();
 		if(slotName.length() > 10) 
 			slotName = slotName.substring(0, 10) + "...";
 		
 		List languages = LanguageDeliveryController.getLanguageDeliveryController().getLanguagesForSiteNode(getDatabase(), siteNodeId, templateController.getPrincipal());
 		
 		sb.append("<div id=\"component" + componentId + "Properties\" class=\"componentProperties\" style=\"right:5px; top:5px; visibility:hidden;\">");
 		sb.append("	<div id=\"component" + componentId + "PropertiesHandle\" class=\"componentPropertiesHandle\"><div id=\"leftPaletteHandleCompProps\">Properties - " + componentName + " in slot " + slotName + "</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('component" + componentId + "Properties');\" class=\"white\"><img src=\"" + componentEditorUrl + "/images/closeIcon.gif\" border=\"0\"/></a></div></div>");
 		sb.append("	<div id=\"component" + componentId + "PropertiesBody\" class=\"componentPropertiesBody\">");
 		
 		sb.append("	<form id=\"component" + componentId + "PropertiesForm\" name=\"component" + componentId + "PropertiesForm\" action=\"" + componentEditorUrl + "ViewSiteNodePageComponents!updateComponentProperties.action\" method=\"POST\">");
 		if(languages.size() == 1)
 			sb.append("<input type=\"hidden\" name=\"languageId\" value=\"" + ((LanguageVO)languages.get(0)).getId() + "\">");
 		
 		sb.append("		<table class=\"igPropertiesTable\" border=\"0\" cellpadding=\"4\" cellspacing=\"0\" style='padding: 2px; margin-bottom: 6px;'>");
 
 		if(languages.size() > 1)
 		{
 			sb.append("		<tr class=\"igtr\">");
 			sb.append("			<td class=\"igpropertylabel\" align=\"left\">" + getLocalizedString(locale, "deliver.editOnSight.changeLanguage") + "</td>");  //$ui.getString("tool.contenttool.languageVersionsLabel")
 			sb.append("			<td class=\"igtd\" width='60%'; align=\"left\">");
 		
 			sb.append("			");
 			sb.append("			<select class=\"mediumdrop\" name=\"languageId\" onChange=\"javascript:changeLanguage(" + siteNodeId + ", this, " + contentId + ");\">");
 			
 			Iterator languageIterator = languages.iterator();
 			int index = 0;
 			int languageIndex = index;
 			while(languageIterator.hasNext())
 			{
 				LanguageVO languageVO = (LanguageVO)languageIterator.next();
 				if(languageVO.getLanguageId().intValue() == languageId.intValue())
 				{
 					sb.append("					<option class=\"iglabel\" value=\"" + languageVO.getLanguageId() + "\" selected><span class=\"iglabel\">" + languageVO.getName() + "</span></option>");
 					sb.append("					<script type=\"text/javascript\">");
 					sb.append("					</script>");
 					languageIndex = index;
 				}
 				else
 				{
 					sb.append("					<option value=\"" + languageVO.getLanguageId() + "\">" + languageVO.getName() + "</option>");
 				}
 				index++;
 			}
 			sb.append("			</select>");
 			sb.append("			<!--");
 			sb.append("				var originalIndex = " + languageIndex + ";");
 			sb.append("			-->");
 	
 			sb.append("			</td>");
 			sb.append("			<td class=\"igtd\">&nbsp;</td>");
 			sb.append("			<td class=\"igtd\">&nbsp;</td>");
 			sb.append("		</tr>");
 
 			sb.append("		<tr>");
 			sb.append("			<td class=\"igtd igpropertyDivider\" colspan='4'><img src='images/trans.gif' width='100' height='5'/></td>");
 			sb.append("		</tr>");
 		}
 		
 		Collection componentProperties = getComponentProperties(componentId, document);
 		
 		String hideProtectedProperties = CmsPropertyHandler.getHideProtectedProperties();
 		int numberOfHiddenProperties = 0;
 		
 		int propertyIndex = 0;
 		boolean isAdvancedProperties = false;
 		Iterator componentPropertiesIterator = componentProperties.iterator();
 		while(componentPropertiesIterator.hasNext())
 		{
 			ComponentProperty componentProperty = (ComponentProperty)componentPropertiesIterator.next();
 		
 			boolean hasAccessToProperty = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentPropertyEditor.EditProperty", "" + componentContentId + "_" + componentProperty.getName());
 			boolean isFirstAdvancedProperty = false;
 			if(componentProperty.getName().equalsIgnoreCase("CacheResult"))
 			{
 				isFirstAdvancedProperty = true;
 				isAdvancedProperties = true;
 			}
 			
 			//logger.info("componentProperty:" + componentProperty.getName() + ":" + isAdvancedProperties);
 			if(componentProperty.getName().equalsIgnoreCase("CacheResult") ||
 			   componentProperty.getName().equalsIgnoreCase("UpdateInterval") ||
 			   componentProperty.getName().equalsIgnoreCase("CacheKey") ||
 			   componentProperty.getName().equalsIgnoreCase("PreRenderOrder"))
 			{
 				hasAccessToProperty = true;
 			}
 			
 			String title = "";
 			
 			//Advanced properties
 			if(isFirstAdvancedProperty)
 			{
 				if(componentProperties.size() - numberOfHiddenProperties < 1)
 				{
 					sb.append("		<tr class=\"igtr\">");
 					sb.append("			<td class=\"igpropertyvalue\" valign=\"top\" align=\"left\" colspan=\"4\" style=\"padding: 6px 0px 6px 2px;\">" + getLocalizedString(locale, "deliver.editOnSight.noPropertiesVisible") + " </td>");
 					sb.append("		</tr>");
 				}
 
 				sb.append("		<tr class=\"igtr\">");
 				sb.append("			<td class=\"igpropertylabel\" valign=\"top\" align=\"left\" colspan=\"4\" style=\"padding: 6px 0px 6px 2px; font-weight: bold;\">" + getLocalizedString(locale, "deliver.editOnSight.advancedProperties") + " <img src='images/downArrow.gif' onclick=\"$('.advancedProperty" + componentId + "').toggle();\"/></td>");
 				sb.append("		</tr>");
 			}
 			
 			if(!hasAccessToProperty && hideProtectedProperties.equalsIgnoreCase("true"))
 			{
 				numberOfHiddenProperties++;
 			}
 			else
 			{
 				StringBuffer helpSB = new StringBuffer();
 				helpSB.append("<div class=\"tooltipDiv\" id=\"helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "\">");
 				helpSB.append("" + (componentProperty.getDescription() == null || componentProperty.getDescription().equalsIgnoreCase("") ? "No description" : componentProperty.getDescription()) + "");
 				helpSB.append("</div>");
 	
 				if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.BINDING))
 				{
 					String assignUrl = "";
 					String createUrl = "";
 					 
 					if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 					{
 						assignUrl = componentEditorUrl + componentProperty.getVisualizingAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 					}
 					else
 					{	
 						if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 						{
 						    String allowedContentTypeIdParameters = "";
 	
 						    if(componentProperty.getAllowedContentTypeNamesArray() != null && componentProperty.getAllowedContentTypeNamesArray().length > 0)
 						    {
 						        allowedContentTypeIdParameters = "&" + componentProperty.getAllowedContentTypeIdAsUrlEncodedString(templateController.getDatabase());
 						        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
 						    }
 						    
 							if(componentProperty.getIsMultipleBinding())
 							{
 								if(componentProperty.getIsAssetBinding())
 									assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleAssetBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 								else
 									assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 							}
 							else
 							{
 								if(componentProperty.getIsAssetBinding())
 								{
 									String assignedParameters = "";
 									Iterator<ComponentBinding> bindingsIterator = componentProperty.getBindings().iterator();
 									while(bindingsIterator.hasNext())
 									{
 										ComponentBinding componentBinding = bindingsIterator.next();
 										assignedParameters = "&assignedContentId=" + componentBinding.getEntityId() + "&assignedAssetKey=" + componentBinding.getAssetKey() + "&assignedPath=" + formatter.encodeURI(componentProperty.getValue());
 									}
 									
 									assignUrl = componentEditorUrl + "ViewContentVersion!viewAssetsForComponentBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple() + assignedParameters;
 								}
 								else
 									assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 
 								if(componentProperty.getBindings().size() > 0)
 								{
 									ComponentBinding firstBinding = componentProperty.getBindings().get(0);
 									title = templateController.getContentPath(firstBinding.getEntityId(), true, true);
 								}
 							}
 						}
 						else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 						{
 							if(componentProperty.getIsMultipleBinding())
 								assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 							else
 								assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 
 							if(componentProperty.getBindings().size() > 0)
 							{
 								ComponentBinding firstBinding = componentProperty.getBindings().get(0);
 								title = templateController.getContentPath(firstBinding.getEntityId(), true, true);
 							}
 						}
 						else if(componentProperty.getEntityClass().equalsIgnoreCase("Category"))
 						{
 							if(componentProperty.getIsMultipleBinding())
 								assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 							else
 								assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 						}
 					}
 						
 					if(componentProperty.getCreateAction() != null && !componentProperty.getCreateAction().equals(""))
 					{
 						createUrl = componentEditorUrl + componentProperty.getCreateAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 					}
 					else
 					{	
 						if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 						{
 							createUrl = assignUrl;
 						}
 						else if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 						{
 						    String allowedContentTypeIdParameters = "";
 	
 						    if(componentProperty.getAllowedContentTypeNamesArray() != null && componentProperty.getAllowedContentTypeNamesArray().length > 0)
 						    {
 						        allowedContentTypeIdParameters = "&" + componentProperty.getAllowedContentTypeIdAsUrlEncodedString(templateController.getDatabase());
 						        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
 						    }
 	
 						    String returnAddress = URLEncoder.encode("ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=Content&entityId=#entityId&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&path=#path&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "", "UTF-8");
 							
 					        String cancelKey = templateController.getOriginalFullURL();
 					        String cancelAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", cancelKey);
 					        if(cancelAddress == null)
 					        {
 					        	cancelAddress = URLEncoder.encode(cancelKey, "UTF-8");
 					        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", cancelKey, cancelAddress);
 					        }
 	
 							if(componentProperty.getIsMultipleBinding())
 								createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 							else
 								createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 						}
 						else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 						{
 							//createUrl = null;
 	
 						    String returnAddress = URLEncoder.encode("ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=Content&entityId=#entityId&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&path=#path&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "", "UTF-8");
 							
 					        String cancelKey = templateController.getOriginalFullURL();
 					        String cancelAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", cancelKey);
 					        if(cancelAddress == null)
 					        {
 					        	cancelAddress = URLEncoder.encode(cancelKey, "UTF-8");
 					        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", cancelKey, cancelAddress);
 					        }
 	
 							if(componentProperty.getIsMultipleBinding())
 								createUrl = componentEditorUrl + "CreateSiteNodeWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 							else
 								createUrl = componentEditorUrl + "CreateSiteNodeWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 						}
 					}
 									
 					boolean isPuffContentForPage = false;
 					if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.BINDING) && componentProperty.getEntityClass().equalsIgnoreCase("Content") && componentProperty.getIsPuffContentForPage())
 						isPuffContentForPage = true;
 					
 					String dividerClass = "igpropertyDivider";
 					if(isPuffContentForPage)
 						dividerClass = "";
 					
 					if(isAdvancedProperties)
 						sb.append("		<tr class=\"igtr advancedProperty" + componentId + "\" style='display:none;'>");
 					else
 						sb.append("		<tr class=\"igtr\">");
 					
 					sb.append("				<td class=\"igpropertylabel " + dividerClass + "\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					sb.append("				<td class=\"igpropertyvalue " + dividerClass + "\" align=\"left\">");
 	
 					if(hasAccessToProperty)
 					{
 						String warningText = getLocalizedString(locale, "deliver.editOnSight.dirtyWarning");
 						sb.append("<a title=\"" + title + "\" class=\"componentEditorLink\" href=\"javascript:if(checkDirty('" + warningText + "')){window.open('" + assignUrl + "','Assign','toolbar=no,status=yes,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no,width=300,height=600,left=5,top=5')};\">");
 					}
 	
 					sb.append("" + (componentProperty.getValue() == null || componentProperty.getValue().equalsIgnoreCase("") ? "Undefined" : componentProperty.getValue()) + (componentProperty.getIsAssetBinding() ? " (" + componentProperty.getAssetKey() + ")" : ""));
 					
 					if(hasAccessToProperty)
 						sb.append("</a>");
 					
 					sb.append("</td>");
 					
 					if(componentProperty.getValue() != null && componentProperty.getValue().equalsIgnoreCase("Undefined"))
 					{	
 						if(hasAccessToProperty && createUrl != null)
 							sb.append("			<td class=\"igtd " + dividerClass + "\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + createUrl + "\"><img src=\"" + componentEditorUrl + "/images/createContent.gif\" border=\"0\" alt=\"Create new content to show\"></a></td>");
 						else
 							sb.append("			<td class=\"igtd " + dividerClass + "\" width=\"16\">&nbsp;</td>");
 					}
 					else
 					{
 						if(hasAccessToProperty)
 							sb.append("			<td class=\"igtd " + dividerClass + "\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					}
 					sb.append("			<td class=\"igtd " + dividerClass + "\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"javascript:showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					
 					sb.append("		</tr>");
 					
 					if(isPuffContentForPage && componentProperty.getBindings() != null && componentProperty.getBindings().size() > 0)
 					{
 						sb.append("	<tr>");
 
 						ComponentBinding binding = componentProperty.getBindings().get(0);
 						List referencingPages = templateController.getReferencingPages(binding.getEntityId(), 50, new Boolean(true));
 						
 						if(referencingPages.size() == 0)
 						{
 							sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">&nbsp;</td><td class=\"igpropertyvalue igpropertyDivider\" valign=\"top\" align=\"left\">");
 							sb.append("			" + getLocalizedString(locale, "deliver.editOnSight.noDetailPageWithContentBinding.label"));
 						}
 						else if(referencingPages.size() == 1)
 						{
 							SiteNodeVO siteNodeVO = (SiteNodeVO)referencingPages.get(0);
 							String path = templateController.getPagePath(siteNodeVO.getId(), templateController.getLanguageId());
 							sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">&nbsp;</td><td class=\"igpropertyvalue igpropertyDivider\" valign=\"top\" align=\"left\">");
 							sb.append("			" + getLocalizedString(locale, "deliver.editOnSight.detailPageWithContentBinding.label") + "<span title='" + path + "'>" + siteNodeVO.getName() + "(" + siteNodeVO.getSiteNodeId() + ")</span>");
 						}
 						else
 						{
 							sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + getLocalizedString(locale, "deliver.editOnSight.detailPagesWithContentBinding.label") + "</td><td class=\"igpropertyvalue igpropertyDivider\" valign=\"top\" align=\"left\">");
 							sb.append("			<input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "_detailSiteNodeId\"/>");
 							sb.append("			<select class=\"propertyselect\" name=\"" + componentProperty.getName() + "_detailSiteNodeId\">");	
 							Iterator referencingPagesIterator = referencingPages.iterator();
 							while(referencingPagesIterator.hasNext())
 							{
 								SiteNodeVO siteNodeVO = (SiteNodeVO)referencingPagesIterator.next();
 								String path = templateController.getPagePath(siteNodeVO.getId(), templateController.getLanguageId());
 								Integer detailSiteNodeId = componentProperty.getDetailSiteNodeId();
 								
 								if(detailSiteNodeId != null && detailSiteNodeId.equals(siteNodeVO.getSiteNodeId()))
 									sb.append("			<option value='" + siteNodeVO.getSiteNodeId() + "' title='" + path + "' selected=\"1\">" + siteNodeVO.getName() + "(" + siteNodeVO.getSiteNodeId() + ")" + "</option>");								
 								else
 									sb.append("			<option value='" + siteNodeVO.getSiteNodeId() + "' title='" + path + "'>" + siteNodeVO.getName() + "(" + siteNodeVO.getSiteNodeId() + ")" + "</option>");								
 							}
 							sb.append("			</select>");	
 
 							if(hasAccessToProperty)
 							    propertyIndex++;
 						}
 						sb.append("			</td>");
 						sb.append("			<td class=\"igpropertylabel igpropertyDivider\"></td>");
 						sb.append("			<td class=\"igpropertylabel igpropertyDivider\"></td>");
 						sb.append("		</tr>");
 					}
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.TEXTFIELD))
 				{
 					if(isAdvancedProperties)
 						sb.append("	<tr class=\"igtr advancedProperty" + componentId + "\" style='display:none;'>");
 					else
 						sb.append("	<tr class=\"igtr\">");
 					
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><input type=\"text\" class=\"propertytextfield\" name=\"" + componentProperty.getName() + "\" value=\"" + componentProperty.getValue() + "\" onkeydown=\"setDirty();\"></td>");
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getValue() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.DATEFIELD))
 				{
 					sb.append("	<tr class=\"igtr\">");
 					
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 					{
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">");
 					
 						sb.append("			<input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\">");
 						sb.append("			<input type=\"text\" class=\"propertydatefield\" style=\"width: 100px;\" id=\"" + componentProperty.getName() + "\" name=\"" + componentProperty.getName() + "\" value=\"" + componentProperty.getValue() + "\" onkeydown=\"setDirty();\"/>&nbsp;<a name=\"calendar_" + componentProperty.getName() + "\" id=\"calendar_" + componentProperty.getName() + "\"><img src=\"" + componentEditorUrl + "/images/calendar.gif\" border=\"0\"/></a>");
 						sb.append("			<script type=\"text/javascript\">");
 						sb.append("				Calendar.setup({");
 						sb.append("	        		inputField     :    \"" + componentProperty.getName() + "\",");
 						sb.append("	        		ifFormat       :    \"%Y-%m-%d %H:%M\",");
 						sb.append("	        		button         :    \"calendar_" + componentProperty.getName() + "\",");
 						sb.append("	        		align          :    \"BR\",");
 						sb.append("	        		singleClick    :    true,");
 						sb.append("	        		firstDay  	   : 	1,");
 						sb.append("	        		showsTime	   :    true,");
 						sb.append("	        		timeFormat     :    \"24\"");
 						sb.append("				});");
 						sb.append("			</script>");
 						sb.append("			</td>");
 					}
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getValue() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.CUSTOMFIELD))
 				{
 					String processedMarkup =  componentProperty.getCustomMarkup().replaceAll("propertyName", componentProperty.getName());
 					processedMarkup = processedMarkup.replaceAll("propertyValue", componentProperty.getValue());
 
 					sb.append("	<tr class=\"igtr\">");
 					
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 					{
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">");
 					
 						sb.append("			<input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\">");
 						sb.append("			" + processedMarkup + "");
 						sb.append("			</td>");
 					}
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getValue() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.TEXTAREA))
 				{
 					if(isAdvancedProperties)
 						sb.append("	<tr class=\"igtr advancedProperty" + componentId + "\" style='display:none;'>");
 					else
 						sb.append("	<tr class=\"igtr\">");
 
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><textarea class=\"propertytextarea\" name=\"" + componentProperty.getName() + "\" onkeydown=\"setDirty();\">" + (componentProperty.getValue() == null ? "" : componentProperty.getValue()) + "</textarea></td>");
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getValue() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.SELECTFIELD))
 				{
 					if(isAdvancedProperties)
 						sb.append("	<tr class=\"igtr advancedProperty" + componentId + "\" style='display:none;'>");
 					else
 						sb.append("	<tr class=\"igtr\">");
 
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 					{
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><select class=\"propertyselect\" name=\"" + componentProperty.getName() + "\" onchange=\"setDirty();\">");
 						
 						Iterator optionsIterator = componentProperty.getOptions().iterator();
 						while(optionsIterator.hasNext())
 						{
 						    ComponentPropertyOption option = (ComponentPropertyOption)optionsIterator.next();
 						    boolean isSame = false;
 						    if(componentProperty != null && componentProperty.getValue() != null && option != null && option.getValue() != null)
 						    	isSame = componentProperty.getValue().equals(option.getValue());
 						    sb.append("<option value=\"" + option.getValue() + "\"" + (isSame ? " selected=\"1\"" : "") + ">" + option.getName() + "</option>");
 						}
 						
 					    sb.append("</td>");
 					}
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 				else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.CHECKBOXFIELD))
 				{
 					if(isAdvancedProperties)
 						sb.append("	<tr class=\"igtr advancedProperty" + componentId + "\" style='display:none;'>");
 					else
 						sb.append("	<tr class=\"igtr\">");
 
 					sb.append("			<td class=\"igpropertylabel igpropertyDivider\" valign=\"top\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 					
 					if(hasAccessToProperty)
 					{
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\">");
 						
 						Iterator optionsIterator = componentProperty.getOptions().iterator();
 						while(optionsIterator.hasNext())
 						{
 						    ComponentPropertyOption option = (ComponentPropertyOption)optionsIterator.next();
 						    boolean isSame = false;
 						    if(componentProperty != null && componentProperty.getValue() != null && option != null && option.getValue() != null)
 						    {
 						    	String[] values = componentProperty.getValue().split(",");
 						    	for(int i=0; i<values.length; i++)
 						    	{
 						    		isSame = values[i].equals(option.getValue());
 						    		if(isSame)
 						    			break;
 						    	}
 						    }
 	
 						    sb.append("<input type=\"checkbox\" name=\"" + componentProperty.getName() + "\" value=\"" + option.getValue() + "\"" + (isSame ? " checked=\"1\"" : "") + " onclicked=\"setDirty();\"/>" + option.getName() + " ");
 						}
 						
 					    sb.append("</td>");
 					}
 					else
 						sb.append("			<td class=\"igpropertyvalue igpropertyDivider\" align=\"left\">" + componentProperty.getDisplayName() + "</td>");
 		
 					if(hasAccessToProperty)
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\" style='padding-top: 2px;'></a></td>");
 					else
 						sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"></td>");
 					
 					sb.append("			<td class=\"igtd igpropertyDivider\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMarkGrad.gif\" onMouseOver=\"showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 					sb.append("		</tr>");
 					
 					if(hasAccessToProperty)
 					    propertyIndex++;
 				}
 			}
 		}
 		
 		timer.printElapsedTime("getComponentPropertiesDiv: 5");
 		
 		if(numberOfHiddenProperties > 0)
 		{
 			sb.append("		<tr class=\"igtr\">");
 			sb.append("			<td colspan=\"4\" class=\"igtd igpropertyDivider\" style=\"padding: 6px 0px 6px 2px; color: darkred;\">" + getLocalizedString(locale, "deliver.editOnSight.protectedPropertiesExists") + "</td>");
 			sb.append("		</tr>");
 		}
 		
 		/*
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td colspan=\"4\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" height=\"5\" width=\"1\"></td>");
 		sb.append("		</tr>");
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td colspan=\"4\" style=\"padding: 6px 0px 6px 2px;\">");
 		sb.append("				<a href=\"javascript:submitForm('component" + componentId + "PropertiesForm');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.save") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("				<a href=\"javascript:hideDiv('component" + componentId + "Properties');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.close") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("			</td>");
 		sb.append("		</tr>");
 		*/
 		sb.append("		</table>");
 		sb.append("	</div>");
 		sb.append("	<div id=\"component" + componentId + "PropertiesFooter\" class=\"componentPropertiesFooter\">");
 		sb.append("		<a href=\"javascript:submitForm('component" + componentId + "PropertiesForm');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.save") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("		<a href=\"javascript:hideDiv('component" + componentId + "Properties');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.close") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("	</div>");
 		sb.append("		<input type=\"hidden\" name=\"repositoryId\" value=\"" + repositoryId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"siteNodeId\" value=\"" + siteNodeId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"languageId\" value=\"" + languageId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"contentId\" value=\"" + contentId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"componentId\" value=\"" + componentId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"showSimple\" value=\"" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\">");
 		sb.append("		</form>");
 		sb.append("	</div>");
 
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("		var theHandle = document.getElementById(\"component" + componentId + "PropertiesHandle\");\n");
 		sb.append("		var theRoot   = document.getElementById(\"component" + componentId + "Properties\");\n");
 		
 		sb.append("		componentId = \"" + componentId + "\";\n");
 		sb.append("		activatedComponentId = QueryString(\"activatedComponentId\");\n");
 		sb.append("		if(activatedComponentId && activatedComponentId == componentId)\n"); 
 		sb.append("			//showDiv(\"component\" + componentId + \"Properties\");\n"); 
 
 		sb.append("		$(theHandle).css('cursor', 'move');\n");
 		sb.append("		$(theRoot).draggable({handle: theHandle, cursor: 'move', distance: 10});\n");
 		
 		sb.append("	</script>");
 
 		return sb.toString();
 	}
 
 	
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentTasksDiv(Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, InfoGlueComponent component, int position, int maxPosition, Document document, TemplateController templateController) throws Exception
 	{		
 	    InfoGluePrincipal principal = templateController.getPrincipal();
 	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    if(cmsUserName != null)
 		    principal = templateController.getPrincipal(cmsUserName);
 	    
 		boolean hasAccessToAccessRights 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 		boolean hasAccessToAddComponent 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 		boolean hasAccessToDeleteComponent 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 		
 		
 		boolean hasMoveComponentUpAccess 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.hasMoveComponentUpAccess", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 		boolean hasMoveComponentDownAccess 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.hasMoveComponentDownAccess", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 		
 		boolean hasAccessToChangeComponent 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeComponent", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 	    boolean hasSaveTemplateAccess 		= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
 	   
 	    boolean hasSubmitToPublishAccess 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.SubmitToPublish", "");
 	    boolean hasPageStructureAccess 		= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.PageStructure", "");
 	    boolean hasOpenInNewWindowAccess 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.OpenInNewWindow", "");
 	    boolean hasViewSourceAccess 		= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ViewSource", "");
 	    boolean hasMySettingsAccess 		= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.MySettings", "");
 	    boolean hasCreateSubpageAccess 		= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.CreateSubpage", "");
 	    boolean hasEditPageMetadataAccess 	= AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.EditPageMetadata", "");
 
 	    boolean hasMaxComponents = false;
 		if(component.getParentComponent() != null && component.getParentComponent().getSlotList() != null)
 		{
 			Iterator slotListIterator = component.getParentComponent().getSlotList().iterator();
 			while(slotListIterator.hasNext())
 			{
 				Slot slot = (Slot)slotListIterator.next();
 				if(slot.getId().equalsIgnoreCase(component.getSlotName()))
 				{
 					if(slot.getAllowedNumberOfComponents() != -1 && slot.getComponents().size() >= slot.getAllowedNumberOfComponents())
 					{
 						hasMaxComponents = true;
 					}
 				}
 			}
 		}
 		
 	    if(component.getContainerSlot() != null && component.getContainerSlot().getDisableAccessControl())
 	    {
 	    	hasAccessToAddComponent = true;
 	    	hasAccessToDeleteComponent = true;
 	    }
 	    
 	    if(hasMaxComponents)
 	    	hasAccessToAddComponent = false;
 
 	    if(component.getIsInherited())
 		{
 		    StringBuffer sb = new StringBuffer();
 		    sb.append("<script type=\"text/javascript\">");
 		    sb.append("hasAccessToAddComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToAddComponent + ";");
 			sb.append("hasAccessToDeleteComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToDeleteComponent + ";");
 			sb.append("hasAccessToChangeComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToChangeComponent + ";");
 			sb.append("hasAccessToAccessRights = " + hasAccessToAccessRights + ";");
 			sb.append("</script>");
 			return sb.toString();
 		}
 	    
 	    StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String componentEditorUrl = "" + CmsPropertyHandler.getComponentEditorUrl();
 		String originalFullURL = "" + templateController.getOriginalFullURL();
 		String componentRendererUrl = "" + CmsPropertyHandler.getComponentRendererUrl();
 
 		sb.append("<div id=\"component" + component.getId() + "Menu\" class=\"skin0 editOnSightMenuDiv\">");
 		    		
 		Collection componentTasks = getComponentTasks(component.getId(), document);
 
 		int taskIndex = 0;
 		Iterator componentTasksIterator = componentTasks.iterator();
 		while(componentTasksIterator.hasNext())
 		{
 		    ComponentTask componentTask = (ComponentTask)componentTasksIterator.next();
 		    
 		    String view = componentTask.getView();
 		    boolean openInPopup = componentTask.getOpenInPopup();
 		    String icon = componentTask.getIcon();
 		    
 		    view = view.replaceAll("\\$componentEditorUrl", componentEditorUrl);
 			view = view.replaceAll("\\$originalFullURL", originalFullURL);
 			view = view.replaceAll("\\$componentRendererUrl", componentRendererUrl);
 		    view = view.replaceAll("\\$repositoryId", repositoryId.toString());
 		    view = view.replaceAll("\\$siteNodeId", siteNodeId.toString());
 		    view = view.replaceAll("\\$languageId", languageId.toString());
 		    view = view.replaceAll("\\$componentId", component.getId().toString());
 		    sb.append("<div class=\"igmenuitems linkComponentTask\" " + ((icon != null && !icon.equals("")) ? "style=\"background-image:url(" + icon + ")\"" : "") + " onClick=\"executeTask('" + view + "', " + openInPopup + ");\"><a href='#'>" + componentTask.getName() + "</a></div>");
 		}
 
 		//Locale locale = templateController.getLocale();
 		Locale locale = templateController.getLocaleAvailableInTool(principal);
 		
 		String editHTML 						= getLocalizedString(locale, "deliver.editOnSight.editHTML");
 		String editInlineHTML 					= getLocalizedString(locale, "deliver.editOnSight.editContentInlineLabel");
 		String submitToPublishHTML 				= getLocalizedString(locale, "deliver.editOnSight.submitToPublish");
 		String addComponentHTML 				= getLocalizedString(locale, "deliver.editOnSight.addComponentHTML");
 		String deleteComponentHTML 				= getLocalizedString(locale, "deliver.editOnSight.deleteComponentHTML");
 		String changeComponentHTML 				= getLocalizedString(locale, "deliver.editOnSight.changeComponentHTML");
 		String moveComponentUpHTML 				= getLocalizedString(locale, "deliver.editOnSight.moveComponentUpHTML");
 		String moveComponentDownHTML 			= getLocalizedString(locale, "deliver.editOnSight.moveComponentDownHTML");
 		String propertiesHTML 					= getLocalizedString(locale, "deliver.editOnSight.propertiesHTML");
 		String pageComponentsHTML 				= getLocalizedString(locale, "deliver.editOnSight.pageComponentsHTML");
 		String viewSourceHTML		 			= getLocalizedString(locale, "deliver.editOnSight.viewSourceHTML");
 		String componentEditorInNewWindowHTML 	= getLocalizedString(locale, "deliver.editOnSight.componentEditorInNewWindowHTML");
 		String savePageTemplateHTML 			= getLocalizedString(locale, "deliver.editOnSight.savePageTemplateHTML");
 		String savePagePartTemplateHTML 		= getLocalizedString(locale, "deliver.editOnSight.savePagePartTemplateHTML");
     	String changePageMetaDataLabel 			= getLocalizedString(locale, "deliver.editOnSight.changePageMetaDataLabel");
     	String createSubPageToCurrentLabel 		= getLocalizedString(locale, "deliver.editOnSight.createSubPageToCurrentLabel");
     	String mySettingsLabel 					= getLocalizedString(locale, "deliver.editOnSight.mySettingsLabel");
 
 		String returnAddress = "" + componentEditorUrl + "ViewInlineOperationMessages.action";
 		
 		String metaDataUrl 			= componentEditorUrl + "ViewAndCreateContentForServiceBinding.action?siteNodeId=" + siteNodeId + "&repositoryId=" + repositoryId + "&changeStateToWorking=true";
     	String createSiteNodeUrl 	= componentEditorUrl + "CreateSiteNode!inputV3.action?isBranch=true&repositoryId=" + repositoryId + "&parentSiteNodeId=" + siteNodeId + "&languageId=" + languageId + "&returnAddress=" + URLEncoder.encode(returnAddress, "utf-8") + "&originalAddress=" + URLEncoder.encode(templateController.getCurrentPageUrl(), "utf-8");
     	String mySettingsUrl 		= componentEditorUrl + "ViewMySettings.action"; 
 
 	    sb.append("<div id=\"editInlineDiv" + component.getId() + "\" class=\"igmenuitems linkEditArticle\"><a href='#'>" + editInlineHTML + "</a></div>");
 		sb.append("<div id=\"editDiv" + component.getId() + "\" class=\"igmenuitems linkEditArticle\"><a href='#'>" + editHTML + "</a></div>");
 
 		if(hasEditPageMetadataAccess)
 			sb.append("<div class=\"igmenuitems linkMetadata\" onClick=\"openInlineDiv('" + metaDataUrl + "', 700, 750, true);\"><a href='#'>" + changePageMetaDataLabel + "</a></div>");
 		if(hasCreateSubpageAccess)
 			sb.append("<div class=\"igmenuitems linkCreatePage\" onClick=\"openInlineDiv('" + createSiteNodeUrl + "', 700, 750, true);\"><a href='#'>" + createSubPageToCurrentLabel + "</a></div>");
 		
 	    if(hasSubmitToPublishAccess)
 	    	sb.append("<div class=\"igmenuitems linkPublish\" onClick=\"submitToPublish(" + siteNodeId + ", " + languageId + ", " + repositoryId + ", '" + URLEncoder.encode("" + componentEditorUrl + "ViewInlineOperationMessages.action", "UTF-8") + "');\"><a href='#'>" + submitToPublishHTML + "</a></div>");
 		if(hasAccessToAddComponent)
 			sb.append("<div class=\"igmenuitems linkAddComponent\" onClick=\"insertComponent();\"><a href='#'>" + addComponentHTML + "</a></div>");
 		if(hasAccessToDeleteComponent)
 		    sb.append("<div class=\"igmenuitems linkDeleteComponent\" onClick=\"deleteComponent();\"><a href='#'>" + deleteComponentHTML + "</a></div>");
 		if(hasAccessToChangeComponent)
 		    sb.append("<div class=\"igmenuitems linkChangeComponent\" onClick=\"changeComponent();\"><a href='#'>" + changeComponentHTML + "</a></div>");
 		if(hasSaveTemplateAccess)
 		    sb.append("<div class=\"igmenuitems linkCreatePageTemplate\" onClick=\"saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(siteNodeId).getMetaInfoContentId() + "');\"><a href='#'>" + savePageTemplateHTML + "</a></div>");
 
 		String upUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=0&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 		String downUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=1&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 			
 		if(hasMoveComponentUpAccess)
 			if(position > 0)
 				sb.append("<div class=\"igmenuitems linkMoveComponentUp\" onClick=\"invokeAddress('" + upUrl + "');\"><a href='#'>" + moveComponentUpHTML + "</a></div>");
 		
 		if(hasMoveComponentDownAccess)
 			if(maxPosition > position)
 				sb.append("<div class=\"igmenuitems linkMoveComponentDown\" onClick=\"invokeAddress('" + downUrl + "');\"><a href='#'>" + moveComponentDownHTML + "</a></div>");
 		
 		sb.append("<div style='border-top: 1px solid #bbb; height: 1px; margin: 0px; padding: 0px; line-height: 1px;'></div>");
 		sb.append("<div class=\"igmenuitems linkComponentProperties\" onClick=\"showComponent(event);\"><a href='#'>" + propertiesHTML + "</a></div>");
 		if(hasPageStructureAccess || hasOpenInNewWindowAccess || hasViewSourceAccess)
 			sb.append("<div style='border-top: 1px solid #bbb; height: 1px; margin:0px; padding: 0px; line-height: 1px;'></div>");
 		if(hasPageStructureAccess)
 			sb.append("<div class=\"igmenuitems linkPageComponents\" onClick=\"javascript:toggleDiv('pageComponents');\"><a href='#'>" + pageComponentsHTML + "</a></div>");
 		if(hasOpenInNewWindowAccess)
 			sb.append("<div id=\"componentEditorInNewWindowDiv" + component.getId() + "\" class=\"igmenuitems linkOpenInNewWindow\"  onClick=\"window.open(document.location.href,'PageComponents','');\"><a href='#'>" + componentEditorInNewWindowHTML + "</a></div>");
 		if(hasViewSourceAccess)
 			sb.append("<div class=\"igmenuitems linkViewSource\" onClick=\"javascript:viewSource();\"><a href='javascript:viewSource();'>" + viewSourceHTML + "</a></div>");
 		if(hasMySettingsAccess)
 			sb.append("<div class=\"igmenuitems linkMySettings\" onClick=\"javascript:openInlineDiv('" + mySettingsUrl + "', 700, 750, true);\"><a href='#'>" + mySettingsLabel + "</a></div>");
 
 		sb.append("</div>");
 		
     	
 		sb.append("<script type=\"text/javascript\">");
 		sb.append("hasAccessToAddComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToAddComponent + ";\n");
 		sb.append("hasAccessToDeleteComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToDeleteComponent + ";\n");
 		sb.append("hasAccessToChangeComponent" + component.getId() + "_" + component.getSlotName().replaceAll("[^0-9,a-z,A-Z]", "_") + " = " + hasAccessToChangeComponent + ";\n");
 		sb.append("hasAccessToAccessRights = " + hasAccessToAccessRights + ";\n");
 		sb.append("</script>");
 		
 		return sb.toString();
 	}
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getPageComponentStructureDiv(TemplateController templateController, Integer siteNodeId, Integer languageId, InfoGlueComponent component) throws Exception
 	{		
 	    if(templateController.getRequestParameter("skipComponentStructure") != null && templateController.getRequestParameter("skipComponentStructure").equalsIgnoreCase("true"))
 	        return "";
 	    
 		StringBuffer sb = new StringBuffer();
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		sb.append("<div id=\"pageComponents\" style=\"right:5px; top:5px; visibility:hidden; display: none;\">");
 
 		sb.append("	<div id=\"dragCorner\" style=\"position: absolute; width: 16px; height: 16px; background-color: white; bottom: 0px; right: 0px;\"><a href=\"javascript:expandWindow('pageComponents');\"><img src=\"" + this.getRequest().getContextPath() + "/images/enlarge.gif\" border=\"0\" width=\"16\" height=\"16\"></a></div>");
 			
 		sb.append("		<div id=\"pageComponentsHandle\"><div id=\"leftPaletteHandle\">Page components</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('pageComponents');\" class=\"white\">close</a></div></div>");
 		sb.append("		<div id=\"pageComponentsBody\"><table class=\"igtable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
 
 		sb.append("		<tr class=\"igtr\">");
 	    sb.append("			<td class=\"igtd\" colspan=\"20\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"><span id=\"" + component.getId() + component.getSlotName() + "ClickableDiv\" class=\"iglabel\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"5\" height=\"1\">" + component.getName() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + component.getSlotName() + "ClickableDiv', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?CCC=1&siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=base&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', '', '', 'base', '" + component.getContentId() + "');</script></td>");
 		sb.append("		</tr>");
 		
 		renderComponentTree(templateController, sb, component, 0, 0, 1);
 
 		sb.append("		<tr class=\"igtr\">");
 		for(int i=0; i<20; i++)
 		{
 			sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td>");
 		}
 		sb.append("		</tr>");
 		sb.append("		</table>");
 		sb.append("		</div>");
 		sb.append("	</div>");
 		
 		return sb.toString();
 	}
 
 	/**
 	 * This method renders the component tree visually
 	 */
 	
 	private void renderComponentTree(TemplateController templateController, StringBuffer sb, InfoGlueComponent component, int level, int position, int maxPosition) throws Exception
 	{
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		ContentVO componentContentVO = templateController.getContent(component.getContentId());
 		
 		int colspan = 20 - level;
 		
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"16\"></td>");
 		
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		
 	    String changeAllowedComponentNamesAsEncodedString = null;
 	    String changeDisallowedComponentNamesAsEncodedString = null;
 	    
 	    if(component.getParentComponent() != null)
 	    {
 	    	Slot subSlotBean = component.getParentComponent().getSlot(component.getSlotName());
 	    	changeAllowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 	    	changeDisallowedComponentNamesAsEncodedString = subSlotBean.getDisallowedComponentsArrayAsUrlEncodedString();
 	    }
 
 		String changeUrl = componentEditorUrl + "ViewSiteNodePageComponents!listComponentsForChange.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=" + component.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((changeAllowedComponentNamesAsEncodedString != null) ? "&" + changeAllowedComponentNamesAsEncodedString : "&AAAA=1")  + ((changeDisallowedComponentNamesAsEncodedString != null) ? "&" + changeDisallowedComponentNamesAsEncodedString : "&AAAA=1");
 		sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/componentIcon.gif\" width=\"16\" height=\"16\"></td><td class=\"igtd\" colspan=\"" + (colspan - 2) + "\"><span id=\"" + component.getId() + "\" class=\"igLabel\">" + componentContentVO.getName() + "</span><script type=\"text/javascript\">initializeComponentInTreeEventHandler('" + component.getId() + "', '" + component.getId() + "', '', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=" + component.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', '" + changeUrl + "', '" + component.getSlotName() + "', 'APA');</script>");
 		String upUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=0&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 		String downUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=1&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 		
 		if(position > 0)
 		    sb.append("<a href=\"" + upUrl + "\"><img src=\"" + this.getRequest().getContextPath() + "/images/upArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		if(maxPosition > position)
 		    sb.append("<a href=\"" + downUrl + "\"><img src=\"" + this.getRequest().getContextPath() + "/images/downArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		
 		sb.append("</td>");
 		
 		sb.append("		</tr>");
 		
 		//Properties
 		/*
 		
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td><td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/propertiesIcon.gif\" width=\"16\" height=\"16\" border=\"0\"></td><td class=\"igtd\" colspan=\"" + (colspan - 3) + "\"><span onclick=\"javascript:showComponentProperties('component" + component.getId() + "Properties');\" class=\"iglabel\">Properties</span></td>");
 		sb.append("		</tr>");
 		
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/endline.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/containerIcon.gif\" width=\"16\" height=\"16\"></td><td class=\"igtd\" colspan=\"" + (colspan - 4) + "\"><span class=\"iglabel\">Slots</span></td>");
 		sb.append("</tr>");
 		*/
 		
 		Iterator slotIterator = component.getSlotList().iterator();
 		while(slotIterator.hasNext())
 		{
 			Slot slot = (Slot)slotIterator.next();
 	
 			sb.append("		<tr class=\"igtr\">");
 			sb.append("			<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 			for(int i=0; i<level; i++)
 			{
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 			}
 			if(slot.getComponents().size() > 0)
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 			else
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/endline.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 
 		    String allowedComponentNamesAsEncodedString = slot.getAllowedComponentsArrayAsUrlEncodedString();
 		    String disallowedComponentNamesAsEncodedString = slot.getDisallowedComponentsArrayAsUrlEncodedString();
 		    //logger.info("allowedComponentNamesAsEncodedString:" + allowedComponentNamesAsEncodedString);
 		    //logger.info("disallowedComponentNamesAsEncodedString:" + disallowedComponentNamesAsEncodedString);
 		    
 		    sb.append("<td class=\"igtd\" colspan=\"" + (colspan - 4) + "\"><span id=\"" + component.getId() + slot.getId() + "ClickableDiv\" class=\"iglabel\">" + slot.getId() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + slot.getId() + "ClickableDiv', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?ddd=1&siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=" + slot.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "") + ((disallowedComponentNamesAsEncodedString != null) ? "&" + disallowedComponentNamesAsEncodedString : "") + "', '', '', '" + slot.getId() + "', '" + component.getContentId() + "');</script></td>");
 			
 			sb.append("		</tr>");
 
 			List slotComponents = slot.getComponents();
 			//logger.info("Number of components in slot " + slot.getId() + ":" + slotComponents.size());
 
 			if(slotComponents != null)
 			{
 				Iterator slotComponentIterator = slotComponents.iterator();
 				int newPosition = 0;
 				while(slotComponentIterator.hasNext())
 				{
 					InfoGlueComponent slotComponent = (InfoGlueComponent)slotComponentIterator.next();
 					//ContentVO componentContent = templateController.getContent(slotComponent.getContentId()); 
 					//String imageUrl = "" + this.getRequest().getContextPath() + "/images/componentIcon.gif";
 					//String imageUrlTemp = getDigitalAssetUrl(componentContent.getId(), "thumbnail");
 					//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 					//	imageUrl = imageUrlTemp;
 					
 					if(slotComponent.getPagePartTemplateComponent() == null)
 						renderComponentTree(templateController, sb, slotComponent, level + 2, newPosition, slotComponents.size() - 1);
 					else
 						renderComponentTree(templateController, sb, slotComponent.getPagePartTemplateComponent(), level + 2, newPosition, slotComponents.size() - 1);
 					
 					newPosition++;
 				}	
 			}
 		}
 	}
 
 
 	/**
 	 * This method creates the tabpanel for the component-palette.
 	 */
 	//private static String componentPaletteDiv = null;
 	
 	private String getComponentPaletteDiv(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
 	{		
 		InfoGluePrincipal principal = templateController.getPrincipal();
 	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    if(cmsUserName != null)
 		    principal = templateController.getPrincipal(cmsUserName);
 
 		if(!templateController.getDeliveryContext().getShowSimple())
 	    {
 		    boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.Palette", false, true);
 		    if(!hasAccess || templateController.getRequestParameter("skipToolbar") != null && templateController.getRequestParameter("skipToolbar").equalsIgnoreCase("true"))
 		        return "";
 	    }
 	    
 		ContentVO contentVO = templateController.getBoundContent(BasicTemplateController.META_INFO_BINDING_NAME);
 
 		//Cache
 		String key = "" + templateController.getPrincipal().getName();
 		String componentPaletteDiv = (String)CacheController.getCachedObject("componentPaletteDivCache", key);
 		if(componentPaletteDiv != null)
 		{
 			if(componentPaletteDiv != null && (templateController.getRequestParameter("refresh") == null || !templateController.getRequestParameter("refresh").equalsIgnoreCase("true")))
 			{
 				return componentPaletteDiv.replaceAll("CreatePageTemplate\\!input.action\\?contentId=.*?'", "CreatePageTemplate!input.action?contentId=" + contentVO.getContentId() + "'");
 			}
 		}
 		//End Cache
 		
 		StringBuffer sb = new StringBuffer();
 			
 		String componentEditorUrl 		= CmsPropertyHandler.getComponentEditorUrl();
 		String componentRendererUrl 	= CmsPropertyHandler.getComponentRendererUrl();
 		String componentRendererAction 	= CmsPropertyHandler.getComponentRendererAction();
 		
 		
 		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:200;\"><img src=\"" + this.getRequest().getContextPath() + "/images/componentDraggedIcon.gif\"></div>");
 		
 		Map componentGroups = getComponentGroups(getComponentContents(), templateController);
 		
 		sb.append("<div id=\"paletteDiv\">");
 		 
 		sb.append("<div id=\"paletteHandle\">");
 		sb.append("	<div id=\"leftPaletteHandle\">Component palette</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('paletteDiv');\" class=\"white\">close</a></div>");
 		sb.append("</div>");
 
 		sb.append("<div id=\"paletteBody\">");
 		sb.append("<table class=\"tabPanel\" cellpadding=\"0\" cellspacing=\"0\">");
 		sb.append(" <tr class=\"igtr\">");
 		
 		Iterator groupIterator = componentGroups.keySet().iterator();
 		int index = 0;
 		String groupName = "";
 		String initialGroupName = "";
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 			
 			if(index == 0)
 			{	
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"thistab\" onclick=\"javascript:changeTab('" + groupName + "');\" height=\"20\"><nobr>" + groupName + "</nobr></td>");
 				initialGroupName = groupName;
 			}
 			else if(!groupIterator.hasNext())
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"igtab\" style=\"border-right: solid thin black\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 			else
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"igtab\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 
 			index++;
 		}
 		
 	    boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
 	    
 		sb.append("  <td class=\"igpalettetd\" width=\"90%\" style=\"text-align: right; border-right: solid thin gray; border-bottom: solid thin white\" align=\"right\">&nbsp;<a href=\"javascript:refreshComponents(document.location.href);\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/refresh.gif\" alt=\"Refresh palette\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivDown('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowDown.gif\" alt=\"Move down\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivUp('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowUp.gif\" alt=\"Move up\" border=\"0\"></a>&nbsp;<a href=\"javascript:toggleDiv('pageComponents');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/pageStructure.gif\" alt=\"Toggle page structure\" border=\"0\"></a>&nbsp;");
 		if(hasSaveTemplateAccess)
 		    sb.append("<a href=\"javascript:saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + contentVO.getId() + "');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/saveComponentStructure.gif\" alt=\"Save the page as a template page\" border=\"0\"></a>&nbsp;");
 		
 		sb.append("<a href=\"javascript:window.open(document.location.href, 'PageComponents', '');\"><img src=\"" + this.getRequest().getContextPath() + "/images/fullscreen.gif\" alt=\"Pop up in a large window\" border=\"0\"></a>&nbsp;</td>");
 		
 		sb.append(" </tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 		
 		sb.append("<script type=\"text/javascript\">");
 		sb.append("var currentGroup = \"" + initialGroupName + "\";");
 		sb.append("</script>");
 				
 		String openGroupName = "";
 
 		groupIterator = componentGroups.keySet().iterator();
 		index = 0;
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 
 			if(index == 0)
 			{
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:3; visibility: inherited;\">");
 				openGroupName = groupName;
 			}
 			else
 			    sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:2; visibility: inherited;\">");	
 			
 			sb.append("<div id=\"" + groupName + "Components\" style=\"visibility:inherit; position:absolute; top:1px; left:5px; height:50px; \">");
 			sb.append("	<table class=\"igtable\" style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 			sb.append("	<tr class=\"igtr\">");
 			//sb.append("	<td width=\"100%\"><nobr>");
 			
 			String imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
 			List components = (List)componentGroups.get(groupName); //getComponentContents();
 			Iterator componentIterator = components.iterator();
 			int componentIndex = 0;
 			while(componentIterator.hasNext())
 			{
 				ContentVO componentContentVO = (ContentVO)componentIterator.next();
 	
 				//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
 				//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 				//	imageUrl = imageUrlTemp;
 				sb.append("	<td class=\"igpalettetd\">");
 				sb.append("		<div id=\"" + componentIndex + "\" style=\"display: block; visibility: inherited;\"><nobr><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\">");
 				sb.append("		<span onMouseDown=\"grabIt(event);\" onmouseover=\"showDetails('" + componentContentVO.getName() + "');\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\" nowrap=\"1\">" + ((componentContentVO.getName().length() > 22) ? componentContentVO.getName().substring(0, 17) : componentContentVO.getName()) + "...</span>");
 				sb.append("     </nobr></div>"); 
 				sb.append("	</td>");
 				
 				imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
 			}
 			sb.append("  <td class=\"igpalettetd\" width=\"90%\">&nbsp;</td>");
 			
 			//sb.append("	</nobr></td>");
 			sb.append("	</tr>");
 			sb.append("	</table>");
 			sb.append("</div>");
 			
 			sb.append("</div>");
 			
 			sb.append("<script type=\"text/javascript\"> if (bw.bw) tabInit('" + groupName + "Components'); </script>");
 
 			
 			index++;
 		}
 		
 		sb.append("<div id=\"statusListBg\">");
 		sb.append("<table class=\"igtable\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 		sb.append("<tr class=\"igtr\">");
 		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"15px\">&nbsp;<a href=\"#\" onclick=\"moveLeft(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowleft.gif\" alt=\"previous\" border=\"0\"></a></td>");
 		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"95%\"><span class=\"componentsStatusText\">Details: </span><span id=\"statusText\" class=\"componentsStatusText\">&nbsp;</span></td>");
 		sb.append("	<td class=\"igpalettetd\" align=\"right\"><a href=\"#\" onclick=\"moveRight(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowright.gif\" alt=\"next\" border=\"0\"></a>&nbsp;</td>");
 		sb.append("</tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("	  	changeTab('" + openGroupName + "');");
 		
 		sb.append("		var theHandle = document.getElementById(\"paletteHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"paletteDiv\");");
 		
 		sb.append("		$(theHandle).css('cursor', 'move');\n");
 		sb.append("		$(theRoot).draggable({handle: theHandle});\n");
 		sb.append("	</script>");
 
 		sb.append("</div>");
 		
 		//Caching the result
 		componentPaletteDiv = sb.toString();
 		CacheController.cacheObject("componentPaletteDivCache", key, componentPaletteDiv);				
 		
 		return componentPaletteDiv;
 	}
 
 	/**
 	 * This method gets all component groups from the available components.
 	 * This is dynamically so if one states a different group in the component the group is created.
 	 */
 
 	private Map getComponentGroups(List components, TemplateController templateController)
 	{
 		Map componentGroups = new HashMap();
 		
 		Iterator componentIterator = components.iterator();
 		while(componentIterator.hasNext())
 		{
 			ContentVO componentContentVO = (ContentVO)componentIterator.next();
 			String groupName = templateController.getContentAttribute(componentContentVO.getId(), "GroupName", true);
 			if(groupName == null || groupName.equals(""))
 				groupName = "Other";
 			
 			List groupComponents = (List)componentGroups.get(groupName);
 			if(groupComponents == null)
 			{
 				groupComponents = new ArrayList();
 				componentGroups.put(groupName, groupComponents);
 			}
 			
 			groupComponents.add(componentContentVO);
 		}
 		
 		return componentGroups;
 	}
 
 	/**
 	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
 	 */
 	
 	public List getComponentContents() throws Exception
 	{
 		HashMap arguments = new HashMap();
 		arguments.put("method", "selectListOnContentTypeName");
 		
 		List argumentList = new ArrayList();
 		HashMap argument = new HashMap();
 		argument.put("contentTypeDefinitionName", "HTMLTemplate");
 		argumentList.add(argument);
 		arguments.put("arguments", argumentList);
 		
 		return ContentController.getContentController().getContentVOList(arguments, getDatabase());
 	}
 	
 	/**
 	 * This method fetches the pageComponent structure as a document.
 	 */
 	 /*   
 	protected org.w3c.dom.Document getComponentPropertiesDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentPropertiesDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		org.w3c.dom.Document cachedComponentPropertiesDocument = (org.w3c.dom.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentPropertiesDocument != null)
 			return cachedComponentPropertiesDocument;
 		
 		org.w3c.dom.Document componentPropertiesDocument = null;
    	
 		try
 		{
 			String xml = this.getComponentPropertiesString(templateController, siteNodeId, languageId, contentId);
 			//logger.info("xml: " + xml);
 			if(xml != null && xml.length() > 0)
 			{
 				componentPropertiesDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentPropertiesDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentPropertiesDocument;
 	}
 	*/
 	
 	protected Document getComponentPropertiesDOM4JDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentPropertiesDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		Document cachedComponentPropertiesDocument = (Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentPropertiesDocument != null)
 			return cachedComponentPropertiesDocument;
 		
 		Document componentPropertiesDocument = null;
    	
 		try
 		{
 			String xml = this.getComponentPropertiesString(templateController, siteNodeId, languageId, contentId);
 			//logger.info("xml: " + xml);
 			if(xml != null && xml.length() > 0)
 			{
 				componentPropertiesDocument = domBuilder.getDocument(xml);
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentPropertiesDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentPropertiesDocument;
 	}
 
 	/**
 	 * This method fetches the template-string.
 	 */
    
 	private String getComponentPropertiesString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentPropertiesString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		String cachedComponentPropertiesString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentPropertiesString != null)
 			return cachedComponentPropertiesString;
 			
 		String componentPropertiesString = null;
    	
 		try
 		{
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
 		    //logger.info("masterLanguageId:" + masterLanguageId);
 		    componentPropertiesString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentProperties", true);
 
 			if(componentPropertiesString == null)
 				throw new SystemException("There was no properties assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentPropertiesString);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return componentPropertiesString;
 	}
 	
 	
 	/**
 	 * This method fetches the tasks as a document.
 	 */
 /*	    
 	protected org.w3c.dom.Document getComponentTasksDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 	    
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentTasksDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		org.w3c.dom.Document cachedComponentTasksDocument = (org.w3c.dom.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentTasksDocument != null)
 			return cachedComponentTasksDocument;
 		
 		org.w3c.dom.Document componentTasksDocument = null;
    	
 		try
 		{
 			String xml = this.getComponentTasksString(templateController, siteNodeId, languageId, contentId);
 			if(xml != null && xml.length() > 0)
 			{
 			    componentTasksDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentTasksDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentTasksDocument;
 	}
 */
 	
 	protected Document getComponentTasksDOM4JDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 	    
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentTasksDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		Document cachedComponentTasksDocument = (Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentTasksDocument != null)
 			return cachedComponentTasksDocument;
 		
 		Document componentTasksDocument = null;
    	
 		try
 		{
 			String xml = this.getComponentTasksString(templateController, siteNodeId, languageId, contentId);
 			if(xml != null && xml.length() > 0)
 			{
 			    componentTasksDocument = domBuilder.getDocument(xml);
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentTasksDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentTasksDocument;
 	}
 
 	/**
 	 * This method fetches the tasks for a certain component.
 	 */
    
 	private String getComponentTasksString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentTasksString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		String cachedComponentTasksString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentTasksString != null)
 			return cachedComponentTasksString;
 			
 		String componentTasksString = null;
    	
 		try
 		{
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId();
 		    componentTasksString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentTasks", true);
 
 			if(componentTasksString == null)
 				throw new SystemException("There was no tasks assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentTasksString);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return componentTasksString;
 	}
 	
 	public Collection getComponentProperties(Integer componentId, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer componentContentId) throws Exception
 	{
 		Document componentPropertiesDocument = getComponentPropertiesDOM4JDocument(templateController, siteNodeId, languageId, componentContentId);
 		return getComponentProperties(componentId, componentPropertiesDocument, templateController);
 	}
 	
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentProperties(Integer componentId, Document document) throws Exception
 	{
 		//TODO - hr kan vi skert cache:a.
 		
 		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
 		List componentProperties = new ArrayList();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			if(document != null)
 			{
 				timer.printElapsedTime("Read document");
 
 				String propertyXPath = "//property";
 				//logger.info("propertyXPath:" + propertyXPath);
 				List anl = document.selectNodes(propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				Iterator anlIterator = anl.iterator();
 				while(anlIterator.hasNext())
 				{
 					Element binding = (Element)anlIterator.next();
 					
 					String name							 = binding.attributeValue("name");
 					String displayName					 = binding.attributeValue("displayName");
 					String description					 = binding.attributeValue("description");
 					String defaultValue					 = binding.attributeValue("defaultValue");
 					String dataProvider					 = binding.attributeValue("dataProvider");
 					String type							 = binding.attributeValue("type");
 					String allowedContentTypeNamesString = binding.attributeValue("allowedContentTypeDefinitionNames");
 					String visualizingAction 			 = binding.attributeValue("visualizingAction");
 					String createAction 				 = binding.attributeValue("createAction");
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 
 					ComponentProperty property = new ComponentProperty();
 					property.setComponentId(componentId);
 					property.setName(name);
 					property.setDisplayName(displayName);
 					property.setDescription(description);
 					property.setDefaultValue(defaultValue);
 					property.setDataProvider(dataProvider);
 					property.setType(type);
 					property.setVisualizingAction(visualizingAction);
 					property.setCreateAction(createAction);
 					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
 					{
 					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
 					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
 					}
 					
 					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
 					{
 						String entity 	= binding.attributeValue("entity");
 						boolean isMultipleBinding 		= new Boolean(binding.attributeValue("multiple")).booleanValue();
 						boolean isAssetBinding 	  		= new Boolean(binding.attributeValue("assetBinding")).booleanValue();
 						boolean isPuffContentForPage 	= new Boolean(binding.attributeValue("isPuffContentForPage")).booleanValue();
 						
 						property.setEntityClass(entity);
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property1");
 
 						property.setValue(value);
 						property.setIsMultipleBinding(isMultipleBinding);
 						property.setIsAssetBinding(isAssetBinding);
 						property.setIsPuffContentForPage(isPuffContentForPage);
 						List<ComponentBinding> bindings = getComponentPropertyBindings(componentId, name, this.getTemplateController());
 						property.setBindings(bindings);
 						if(entity.equals("Content") && isPuffContentForPage)
 						{
 							String detailSiteNodeId = getComponentPropertyValue(componentId, name + "_detailSiteNodeId");
 							if(detailSiteNodeId != null && !detailSiteNodeId.equals("") && !detailSiteNodeId.equals("Undefined"))
 								property.setDetailSiteNodeId(new Integer(detailSiteNodeId));
 						}
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.DATEFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.CUSTOMFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						String customMarkup = binding.attributeValue("customMarkup");
 						String processedMarkup =  customMarkup.replaceAll("propertyName", name);
 						processedMarkup = processedMarkup.replaceAll("propertyValue", value);
 
 						property.setCustomMarkup(processedMarkup);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
 					{		
 						boolean WYSIWYGEnabled = new Boolean(binding.attributeValue("WYSIWYGEnabled")).booleanValue();
 						property.setWYSIWYGEnabled(WYSIWYGEnabled);
 						String WYSIWYGToolbar = binding.attributeValue("WYSIWYGToolbar");
 						property.setWYSIWYGToolbar(WYSIWYGToolbar);
 
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						
 						List optionList = binding.elements("option");
 						Iterator optionListIterator = optionList.iterator();
 						while(optionListIterator.hasNext())
 						{
 							Element option = (Element)optionListIterator.next();
 							String optionName	= option.attributeValue("name");
 							String optionValue	= option.attributeValue("value");
 							ComponentPropertyOption cpo = new ComponentPropertyOption(optionName, optionValue);
 							property.getOptions().add(cpo);
 						}
 						
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.CHECKBOXFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property3");
 						
 						List optionList = binding.elements("option");
 						Iterator optionListIterator = optionList.iterator();
 						while(optionListIterator.hasNext())
 						{
 							Element option = (Element)optionListIterator.next();
 							String optionName	= option.attributeValue("name");
 							String optionValue	= option.attributeValue("value");
 							ComponentPropertyOption cpo = new ComponentPropertyOption(optionName, optionValue);
 							property.getOptions().add(cpo);
 						}
 						
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					
 					componentProperties.add(property);
 				}
 			}
 			
 			addSystemProperties(componentProperties, componentId);
 		}
 		catch(Exception e)
 		{
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 		}
 							
 		return componentProperties;
 	}
 
 
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	public void addSystemProperties(List componentProperties, Integer componentId) throws Exception
 	{
 		ComponentProperty cacheResultProperty = new ComponentProperty();
 		cacheResultProperty.setComponentId(componentId);
 		cacheResultProperty.setName("CacheResult");
 		cacheResultProperty.setDisplayName("Cache Result");
 		cacheResultProperty.setDescription("Do you want to cache the components rendered result.");
 		cacheResultProperty.setDefaultValue("false");
 		cacheResultProperty.setDataProvider("");
 		cacheResultProperty.setType("select");
 		cacheResultProperty.setVisualizingAction("");
 		cacheResultProperty.setCreateAction("");
 		
 		ComponentPropertyOption cpoNo = new ComponentPropertyOption("No", "false");
 		ComponentPropertyOption cpoYes = new ComponentPropertyOption("Yes", "true");
 		cacheResultProperty.getOptions().add(cpoNo);
 		cacheResultProperty.getOptions().add(cpoYes);
 			
 		String value = getComponentPropertyValue(componentId, "CacheResult");
 		cacheResultProperty.setValue(value);
 		
 		componentProperties.add(cacheResultProperty);
 
 		ComponentProperty cacheIntervalProperty = new ComponentProperty();
 		cacheIntervalProperty.setComponentId(componentId);
 		cacheIntervalProperty.setName("UpdateInterval");
 		cacheIntervalProperty.setDisplayName("Cache Update Interval");
 		cacheIntervalProperty.setDescription("Interval before the cache gets updated");
 		cacheIntervalProperty.setDefaultValue("-1");
 		cacheIntervalProperty.setDataProvider("");
 		cacheIntervalProperty.setType("select");
 		cacheIntervalProperty.setVisualizingAction("");
 		cacheIntervalProperty.setCreateAction("");
 		
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("1 second", "1"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("2 seconds", "2"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("3 seconds", "3"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("4 seconds", "4"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("5 seconds", "5"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("10 seconds", "10"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("15 seconds", "15"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("20 seconds", "20"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("30 seconds", "30"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("1 minute", "60"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("2 minutes", "120"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("5 minutes", "300"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("10 minutes", "600"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("30 minutes", "1800"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("1 hour", "3600"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("2 hours", "7200"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("6 hours", "21600"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("12 hours", "43200"));
 		cacheIntervalProperty.getOptions().add(new ComponentPropertyOption("24 hours", "86400"));
 
 		String updateIntervalValue = getComponentPropertyValue(componentId, "UpdateInterval");
 		cacheIntervalProperty.setValue(updateIntervalValue);
 		
 		componentProperties.add(cacheIntervalProperty);
 
 		ComponentProperty cacheKeyProperty = new ComponentProperty();
 		cacheKeyProperty.setComponentId(componentId);
 		cacheKeyProperty.setName("CacheKey");
 		cacheKeyProperty.setDisplayName("Cache Key");
 		cacheKeyProperty.setDescription("Key for the component cache");
 		cacheKeyProperty.setDefaultValue("");
 		cacheKeyProperty.setDataProvider("");
 		cacheKeyProperty.setType("textfield");
 		cacheKeyProperty.setVisualizingAction("");
 		cacheKeyProperty.setCreateAction("");
 		
 		String cacheKeyValue = getComponentPropertyValue(componentId, "CacheKey");
 		cacheKeyProperty.setValue(cacheKeyValue);
 		
 		componentProperties.add(cacheKeyProperty);
 
 		ComponentProperty priorityProperty = new ComponentProperty();
 		priorityProperty.setComponentId(componentId);
 		priorityProperty.setName("PreRenderOrder");
 		priorityProperty.setDisplayName("Pre processing order");
 		priorityProperty.setDescription("State the order in which the component get's prerendered");
 		priorityProperty.setDefaultValue("99");
 		priorityProperty.setDataProvider("");
 		priorityProperty.setType("select");
 		priorityProperty.setVisualizingAction("");
 		priorityProperty.setCreateAction("");
 		
 		for(int i=0; i<15; i++)
 			priorityProperty.getOptions().add(new ComponentPropertyOption("" + i, "" + i));
 
 		String preRenderOrderPropertyValue = getComponentPropertyValue(componentId, "PreRenderOrder");
 		priorityProperty.setValue(preRenderOrderPropertyValue);
 		
 		componentProperties.add(priorityProperty);
 
 	}
 
 	private List getComponentProperties(Integer componentId, Document document, TemplateController templateController) throws Exception
 	{
 		//TODO - hr kan vi skert cache:a.
 		
 		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
 		List componentProperties = new ArrayList();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			if(document != null)
 			{
 			//if(componentPropertiesXML != null && componentPropertiesXML.length() > 0)
 			//{
 				//org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentPropertiesXML.getBytes("UTF-8"));
 
 				timer.printElapsedTime("Read document");
 
 				String propertyXPath = "//property";
 				//logger.info("propertyXPath:" + propertyXPath);
 				List anl = document.selectNodes(propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				Iterator anlIterator = anl.iterator();
 				while(anlIterator.hasNext())
 				{
 					Element binding = (Element)anlIterator.next();
 					
 					String name							 = binding.attributeValue("name");
 					String displayName					 = binding.attributeValue("displayName");
 					String description					 = binding.attributeValue("description");
 					String defaultValue					 = binding.attributeValue("defaultValue");
 					String dataProvider					 = binding.attributeValue("dataProvider");
 					String type							 = binding.attributeValue("type");
 					String allowedContentTypeNamesString = binding.attributeValue("allowedContentTypeDefinitionNames");
 					String visualizingAction 			 = binding.attributeValue("visualizingAction");
 					String createAction 				 = binding.attributeValue("createAction");
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 
 					ComponentProperty property = new ComponentProperty();
 					property.setComponentId(componentId);
 					property.setName(name);
 					property.setDisplayName(displayName);
 					property.setDescription(description);
 					property.setDefaultValue(defaultValue);
 					property.setDataProvider(dataProvider);
 					property.setType(type);
 					property.setVisualizingAction(visualizingAction);
 					property.setCreateAction(createAction);
 					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
 					{
 					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
 					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
 					}
 					
 					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
 					{
 						String entity 	= binding.attributeValue("entity");
 						boolean isMultipleBinding 		= new Boolean(binding.attributeValue("multiple")).booleanValue();
 						boolean isAssetBinding 			= new Boolean(binding.attributeValue("assetBinding")).booleanValue();
 						boolean isPuffContentForPage 	= new Boolean(binding.attributeValue("isPuffContentForPage")).booleanValue();
 
 						property.setEntityClass(entity);
 						String value = getComponentPropertyValue(componentId, name, templateController);
 
 						property.setValue(value);
 						property.setIsMultipleBinding(isMultipleBinding);
 						property.setIsAssetBinding(isAssetBinding);
 						property.setIsPuffContentForPage(isPuffContentForPage);
 						List<ComponentBinding> bindings = getComponentPropertyBindings(componentId, name, templateController);
 						property.setBindings(bindings);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.DATEFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.CUSTOMFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						String customMarkup = binding.attributeValue("customMarkup");
 						String processedMarkup =  customMarkup.replaceAll("propertyName", name);
 						processedMarkup = processedMarkup.replaceAll("propertyValue", value);
 
 						property.setCustomMarkup(processedMarkup);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
 					{		
 						boolean WYSIWYGEnabled = new Boolean(binding.attributeValue("WYSIWYGEnabled")).booleanValue();
 						property.setWYSIWYGEnabled(WYSIWYGEnabled);
 						String WYSIWYGToolbar = binding.attributeValue("WYSIWYGToolbar");
 						property.setWYSIWYGToolbar(WYSIWYGToolbar);
 
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						
 						List optionList = binding.elements("option");
 						Iterator optionListIterator = optionList.iterator();
 						while(optionListIterator.hasNext())
 						{
 							Element option = (Element)optionListIterator.next();
 							String optionName	= option.attributeValue("name");
 							String optionValue	= option.attributeValue("value");
 							ComponentPropertyOption cpo = new ComponentPropertyOption(optionName, optionValue);
 							property.getOptions().add(cpo);
 						}
 						
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					
 					componentProperties.add(property);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 			e.printStackTrace();
 		}
 							
 		return componentProperties;
 	}
 
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentTasks(Integer componentId, Document document) throws Exception
 	{
 		List componentTasks = new ArrayList();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			if(document != null)
 			{
 				timer.printElapsedTime("Read document");
 
 				String propertyXPath = "//task";
 				//logger.info("propertyXPath:" + propertyXPath);
 				List anl = document.selectNodes(propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				Iterator anlIterator = anl.iterator();
 				while(anlIterator.hasNext())
 				{
 					Element binding = (Element)anlIterator.next();
 					
 					String name			= binding.attributeValue("name");
 					String view			= binding.attributeValue("view");
 					String icon			= binding.attributeValue("icon");
 					String openInPopup 	= binding.attributeValue("openInPopup");
 					if(openInPopup == null || (!openInPopup.equals("true") && !openInPopup.equals("false")))
 						openInPopup = "true";
 					
 					if(logger.isInfoEnabled())
 					{
 						logger.info("name:" + name);
 						logger.info("view:" + view);
 						logger.info("openInPopup:" + openInPopup);
 					}
 					
 					ComponentTask task = new ComponentTask();
 					task.setName(name);
 					task.setView(view);
 					task.setIcon(icon);
 					task.setOpenInPopup(new Boolean(openInPopup));
 					task.setComponentId(componentId);
 					
 					componentTasks.add(task);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 		}
 							
 		return componentTasks;
 	}
 
 
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private String getComponentPropertyValue(Integer componentId, String name) throws Exception
 	{
 		String value = "Undefined";
 		
 		Timer timer = new Timer();
 		timer.setActive(false);
 				
 		Integer siteNodeId = null;
 		Integer languageId = null;
 		
 		if(this.getRequest().getParameter("siteNodeId") != null && this.getRequest().getParameter("siteNodeId").length() > 0)
 			siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		else
 		{
 			siteNodeId = this.getTemplateController().getDeliveryContext().getSiteNodeId();
 		}
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, null);
 
 		if(this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
 		{
 			languageId = new Integer(this.getRequest().getParameter("languageId"));
 			//logger.info("" + languageId + "=" + this.getTemplateController().getDeliveryContext().getLanguageId());
 			if(!languageId.equals(this.getTemplateController().getDeliveryContext().getLanguageId()))
 			{
 				//logger.info("Getting 2");
 				languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNodeWithValityCheck(getDatabase(), nodeDeliveryController, siteNodeId).getId();				
 			}
 		}
 		else
 		{
 			languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNodeWithValityCheck(getDatabase(), nodeDeliveryController, siteNodeId).getId();
 		}
 		
 		//logger.info("languageId:" + languageId);
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), languageId);
 		
 		Integer contentId  = new Integer(-1);
 		if(this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 		
 		Document document = getPageComponentsDOM4JDocument(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		List anl = document.selectNodes(componentXPath);
 		Iterator anlIterator = anl.iterator();
 		while(anlIterator.hasNext())
 		{
 			Element property = (Element)anlIterator.next();
 			
 			String id 			= property.attributeValue("type");
 			String path 		= property.attributeValue("path");
 			
 			//logger.info("Locale:" + locale.getLanguage());
 			if(property.attribute("path_" + locale.getLanguage()) != null)
 				path = property.attributeValue("path_" + locale.getLanguage());
 
 			value 				= path;
 		}
 
 		/*
 		org.w3c.dom.Document document = getPageComponentsDocument(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element property = (org.w3c.dom.Element)anl.item(i);
 			
 			String id 			= property.getAttribute("type");
 			String path 		= property.getAttribute("path");
 			
 			if(property.hasAttribute("path_" + locale.getLanguage()))
 				path = property.getAttribute("path_" + locale.getLanguage());
 
 			value 				= path;
 		}
 		*/
 		
 		return value;
 	}
 
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private List<ComponentBinding> getComponentPropertyBindings(Integer componentId, String name, TemplateController templateController) throws Exception
 	{
 		List<ComponentBinding> componentBindings = new ArrayList<ComponentBinding>();
 		
 		Timer timer = new Timer();
 		timer.setActive(false);
 				
 		Integer siteNodeId = null;
 		Integer languageId = null;
 		
 		if(this.getRequest() != null && this.getRequest().getParameter("siteNodeId") != null && this.getRequest().getParameter("siteNodeId").length() > 0)
 			siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		else
 		{
 			siteNodeId = templateController.getDeliveryContext().getSiteNodeId();
 		}
 		
 		if(this.getRequest() != null && this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
 		{
 			languageId = new Integer(this.getRequest().getParameter("languageId"));
 			if(!languageId.equals(templateController.getDeliveryContext().getLanguageId()))
 			{
 				languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();				
 			}
 		}
 		else
 		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
 		        
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 		
 		Integer contentId  = new Integer(-1);
 		if(this.getRequest() != null && this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		Document document = getPageComponentsDOM4JDocument(templateController.getDatabase(), templateController, siteNodeId, languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']/binding";
 		//logger.info("componentXPath:" + componentXPath);
 		List anl = document.selectNodes(componentXPath);
 		Iterator anlIterator = anl.iterator();
 		while(anlIterator.hasNext())
 		{
 			Element property = (Element)anlIterator.next();
 			
 			String entity   = property.attributeValue("entity");
 			String entityId = property.attributeValue("entityId");
 			String assetKey = property.attributeValue("assetKey");
 			
 			ComponentBinding componentBinding = new ComponentBinding();
 			componentBinding.setEntityClass(entity);
 			componentBinding.setEntityId(new Integer(entityId));
 			componentBinding.setAssetKey(assetKey);
 
 			componentBindings.add(componentBinding);
 		}
 		
 		return componentBindings;
 	}
 
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private String getComponentPropertyValue(Integer componentId, String name, TemplateController templateController) throws Exception
 	{
 		String value = "Undefined";
 		
 		Timer timer = new Timer();
 		timer.setActive(false);
 				
 		Integer languageId = null;
 		if(this.getRequest() != null && this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
 		    languageId = new Integer(this.getRequest().getParameter("languageId"));
 		else
 		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), templateController.getSiteNodeId()).getId();
 		        
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 		
 		Integer contentId  = new Integer(-1);
 		if(this.getRequest() != null && this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), languageId, contentId);
 
 		Document document = getPageComponentsDOM4JDocument(templateController.getDatabase(), templateController, templateController.getSiteNodeId(), languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		List anl = document.selectNodes(componentXPath);
 		Iterator anlIterator = anl.iterator();
 		while(anlIterator.hasNext())
 		{
 			Element property = (Element)anlIterator.next();
 			
 			String id 			= property.attributeValue("type");
 			String path 		= property.attributeValue("path");
 			
 			if(property.attribute("path_" + locale.getLanguage()) != null)
 				path = property.attributeValue("path_" + locale.getLanguage());
 
 			value 				= path;
 		}
 
 		/*
 		org.w3c.dom.Document document = getPageComponentsDocument(templateController.getDatabase(), templateController, templateController.getSiteNodeId(), languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element property = (org.w3c.dom.Element)anl.item(i);
 			
 			String id 			= property.getAttribute("type");
 			String path 		= property.getAttribute("path");
 
 			if(property.hasAttribute("path_" + locale.getLanguage()))
 				path = property.getAttribute("path_" + locale.getLanguage());
 				
 			value 				= path;
 		}
 		*/
 
 		return value;
 	}
 
 
 	/*
 	 * This method returns a bean representing a list of bindings that the component has.
 	 */
 	 
 	private List getContentBindnings(Integer componentId) throws Exception
 	{
 		List contentBindings = new ArrayList();
 		
 		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);			
 		////logger.info("componentXML:" + componentXML);
 
 		Document document = domBuilder.getDocument(componentXML);
 		String componentXPath = "//component[@id=" + componentId + "]/bindings/binding";
 		//logger.info("componentXPath:" + componentXPath);
 		List anl = document.selectNodes(componentXPath);
 		Iterator anlIterator = anl.iterator();
 		while(anlIterator.hasNext())
 		{
 			Element binding = (Element)anlIterator.next();
 			//logger.info(XMLHelper.serializeDom(binding, new StringBuffer()));
 			//logger.info("YES - we read the binding properties...");		
 			
 			String id 			= binding.attributeValue("id");
 			String entityClass 	= binding.attributeValue("entity");
 			String entityId 	= binding.attributeValue("entityId");
 			String assetKey 	= binding.attributeValue("assetKey");
 			//logger.info("id:" + id);
 			//logger.info("entityClass:" + entityClass);
 			//logger.info("entityId:" + entityId);
 			
 			if(entityClass.equalsIgnoreCase("Content"))
 			{
 				ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(new Integer(entityId), getDatabase());
 				ComponentBinding componentBinding = new ComponentBinding();
 				componentBinding.setId(new Integer(id));
 				componentBinding.setComponentId(componentId);
 				componentBinding.setEntityClass(entityClass);
 				componentBinding.setEntityId(new Integer(entityId));
 				componentBinding.setAssetKey(assetKey);
 				componentBinding.setBindingPath(contentVO.getName());
 				
 				contentBindings.add(componentBinding);
 			}
 		}
 			
 		return contentBindings;
 	}
 	 
 	 
 	private void printComponentHierarchy(List pageComponents, int level)
 	{
 		Iterator pageComponentIterator = pageComponents.iterator();
 		while(pageComponentIterator.hasNext())
 		{
 			InfoGlueComponent tempComponent = (InfoGlueComponent)pageComponentIterator.next();
 			
 			for(int i=0; i<level; i++)
 			    logger.info(" ");
 			
 			logger.info("  component:" + tempComponent.getName());
 			
 			Iterator slotIterator = tempComponent.getSlotList().iterator();
 			while(slotIterator.hasNext())
 			{
 				Slot slot = (Slot)slotIterator.next();
 				
 				for(int i=0; i<level; i++)
 					logger.info(" ");
 					
 				logger.info(" slot for " + tempComponent.getName() + ":" + slot.getId());
 				printComponentHierarchy(slot.getComponents(), level + 1);
 			}
 		}			
 	}
 	
   	public String getLocalizedString(Locale locale, String key) 
   	{
     	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
 
     	return stringManager.getString(key);
   	}
 
 }
