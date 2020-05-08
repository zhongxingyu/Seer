 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.infoglue.cms.applications.common.ToolbarButton;
 import org.infoglue.cms.applications.common.ToolbarButton;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.InterceptionPointVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGlueGroup;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.security.InfoGlueRole;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.StringManager;
 import org.infoglue.cms.util.StringManagerFactory;
 import org.infoglue.deliver.util.HttpHelper;
 import org.infoglue.deliver.util.HttpUtilities;
 import org.infoglue.deliver.util.Timer;
 
 public class ToolbarController
 {
 	private final static Logger logger = Logger.getLogger(ToolbarController.class.getName());
 
 	private static final long serialVersionUID = 1L;
 	
 	private String URIEncoding = CmsPropertyHandler.getURIEncoding();
 
 	private InfoGluePrincipal principal = null;
 	private Locale locale = null;
 	private String toolbarKey = null;
 	private String primaryKey = null;
 	private Integer primaryKeyAsInteger = null;
 	private String extraParameters = null;
 	
 	public List<ToolbarButton> getRightToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, String primaryKey, String extraParameters)
 	{
 		this.toolbarKey = toolbarKey;
 		this.principal = principal;
 		this.locale = locale;
 		this.primaryKey = primaryKey;
 		this.extraParameters = extraParameters;
 		try
 		{
 			primaryKeyAsInteger = new Integer(primaryKey);
 		}
 		catch (Exception e) 
 		{
 			//Do nothing
 		}
 
 		try
 		{
 			List<ToolbarButton> toolbarButtons = new ArrayList<ToolbarButton>();
 	
 			toolbarButtons.addAll(getHelpButton());
 			toolbarButtons.addAll(getWindowCloseButton());
 
 			return toolbarButtons;
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;	
 	}
 	
 	public List<ToolbarButton> getToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, String primaryKey, String extraParameters)
 	{
 		Timer t = new Timer();
 		
 		this.toolbarKey = toolbarKey;
 		this.principal = principal;
 		this.locale = locale;
 		this.primaryKey = primaryKey;
 		this.extraParameters = extraParameters;
 		try
 		{
 			primaryKeyAsInteger = new Integer(primaryKey);
 		}
 		catch (Exception e) 
 		{
 			//Do nothing
 		}
 		logger.info("toolbarKey:" + toolbarKey);
 		logger.info("primaryKey:" + primaryKey);
 		logger.info("extraParameters:" + extraParameters);
 		
 		t.printElapsedTime("Button 1 took");
 		
 		try
 		{
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
 				return getContentVersionButtons();
 			
 			if(toolbarKey.equalsIgnoreCase("tool.common.globalSubscriptions.header"))
 				return getGlobalSubscriptionsButtons();
 			
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
 				return getCreateSiteNodeButtons();
 			*/
 			
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
 				return getRepositoriesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
 				return getRepositoryDetailsButtons();
 			*/
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
 				return getGroupPropertiesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
 				return getRolePropertiesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
 				return getUserPropertiesButtons();
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUserList.header"))
 				return getSystemUsersButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
 				return getSystemUserDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
 				return getRolesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 				return getRoleDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
 				return getGroupsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
 				return getGroupDetailsButtons();
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
 				return getLanguagesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
 				return getLanguageDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
 				return getInterceptionPointsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
 				return getInterceptionPointButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
 				return getInterceptorsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
 				return getInterceptorButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
 				return getServiceDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
 				return getServiceDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
 				return getAvailableServiceBindingsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
 				return getAvailableServiceBindingDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
 				return getSiteNodeTypeDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
 				return getSiteNodeTypeDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
 				return getContentTypeDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
 				return getContentTypeDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
 				return getCategoryButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
 				return getAvailablePackagesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinitionList.header"))
 				return getWorkflowDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinition.header"))
 				return getWorkflowDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.portletList.header"))
 				return getPortletsButtons();
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.portlet.header"))
 			//	return getPortletDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.redirectList.header"))
 				return getRedirectsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRedirect.header"))
 				return getRedirectDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeList.header"))
 				return getServerNodesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServerNode.header"))
 				return getServerNodeDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
 				return getMessageCenterButtons();
 			*/
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;				
 	}
 	
 	public List<ToolbarButton> getFooterToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, String primaryKey, String extraParameters)
 	{
 		Timer t = new Timer();
 		
 		this.toolbarKey = toolbarKey;
 		this.principal = principal;
 		this.locale = locale;
 		this.primaryKey = primaryKey;
 		this.extraParameters = extraParameters;
 		try
 		{
 			primaryKeyAsInteger = new Integer(primaryKey);
 		}
 		catch (Exception e) 
 		{
 			//Do nothing
 		}
 		System.out.println("toolbarKey:" + toolbarKey);
 		logger.info("toolbarKey:" + toolbarKey);
 		logger.info("primaryKey:" + primaryKey);
 		logger.info("extraParameters:" + extraParameters);
 		
 		t.printElapsedTime("Button 1 took");
 		
 		try
 		{
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
 				return getContentVersionFooterButtons();
 			
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
 				return getCreateSiteNodeFooterButtons();
 			
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
 				return getMessageCenterFooterButtons();
 
 			if(toolbarKey.equalsIgnoreCase("tool.common.subscriptions.header"))
 				return getSaveCancelFooterButtons();
 
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
 				return getRepositoriesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
 				return getRepositoryDetailsButtons();
 			*/
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
 				return getGroupPropertiesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
 				return getRolePropertiesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
 				return getUserPropertiesButtons();
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUserList.header"))
 				return getSystemUsersButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
 				return getSystemUserDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
 				return getRolesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 				return getRoleDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
 				return getGroupsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
 				return getGroupDetailsButtons();
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
 				return getLanguagesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
 				return getLanguageDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
 				return getInterceptionPointsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
 				return getInterceptionPointButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
 				return getInterceptorsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
 				return getInterceptorButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
 				return getServiceDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
 				return getServiceDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
 				return getAvailableServiceBindingsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
 				return getAvailableServiceBindingDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
 				return getSiteNodeTypeDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
 				return getSiteNodeTypeDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
 				return getContentTypeDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
 				return getContentTypeDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
 				return getCategoryButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
 				return getAvailablePackagesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinitionList.header"))
 				return getWorkflowDefinitionsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinition.header"))
 				return getWorkflowDefinitionDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.portletList.header"))
 				return getPortletsButtons();
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.portlet.header"))
 			//	return getPortletDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.redirectList.header"))
 				return getRedirectsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRedirect.header"))
 				return getRedirectDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeList.header"))
 				return getServerNodesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServerNode.header"))
 				return getServerNodeDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
 				return getMessageCenterButtons();
 			*/
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;				
 	}
 
 	
 	private List<ToolbarButton> getContentVersionButtons() throws Exception
 	{
 		Timer t = new Timer();
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		/*
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.save.label"), 
 									  getLocalizedString(locale, "tool.contenttool.save.label"),
 									  "javascript:validateAndSubmitContentForm();",
 									  "images/v3/saveInlineIcon.gif"));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
 									  "javascript:validateAndSubmitContentFormThenClose();",
 									  "images/v3/saveAndExitInlineIcon.gif"));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.publish.label"), 
 									  getLocalizedString(locale, "tool.contenttool.publish.label"),
 									  "javascript:validateAndSubmitContentFormThenSubmitToPublish();",
 				  					  "images/v3/publishIcon.gif"));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
 				  					  "javascript:refreshCaller();",
 				  					  "images/v3/cancelIcon.gif"));
 		*/
 		
 		System.out.println("primaryKeyAsInteger:" + primaryKeyAsInteger);
 		LanguageVO currentLanguageVO = null;
 		ContentVO contentVO = null;
 		if(primaryKeyAsInteger != null)
 		{
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(primaryKeyAsInteger);
 			contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
 			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(contentVersionVO.getLanguageId());
 		}
 		else
 		{
 			System.out.println("extraInformation:" + extraParameters);
 			Map extraParametersMap = new HttpHelper().toMap(extraParameters, "UTF-8");
 			contentVO = ContentController.getContentController().getContentVOWithId(new Integer((String)extraParametersMap.get("contentId")));
 			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(new Integer((String)extraParametersMap.get("languageId")));
 		}
 			
 		
 		ToolbarButton languageDropButton = new ToolbarButton("",
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()), 
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()),
 				  											 "",
 					  										 "images/v3/menu-button-arrow.png",
 					  										 "right",
 					  										 false);
 		
 		Iterator repositoryLanguagesIterator = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId()).iterator();
 		while(repositoryLanguagesIterator.hasNext())
 		{
 			LanguageVO languageVO = (LanguageVO)repositoryLanguagesIterator.next();
 			if(!currentLanguageVO.getId().equals(languageVO.getId()))
 			{
 				languageDropButton.getSubButtons().add(new ToolbarButton("" + languageVO.getId(),
 						 StringUtils.capitalize(languageVO.getDisplayLanguage()), 
 						 StringUtils.capitalize(languageVO.getDisplayLanguage()),
 						 "changeLanguage(" + contentVO.getId() + ", " + languageVO.getId() + ");",
 						 ""));
 			}
 		}
 		
 		buttons.add(languageDropButton);
 
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getContentVersionFooterButtons() throws Exception
 	{
 		Timer t = new Timer();
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.save.label"), 
 									  getLocalizedString(locale, "tool.contenttool.save.label"),
 									  "javascript:validateAndSubmitContentForm();",
 									  "images/v3/saveInlineIcon.gif"));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
 									  "javascript:validateAndSubmitContentFormThenClose();",
 									  "images/v3/saveAndExitInlineIcon.gif"));
 		
 		/*
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.publish.label"), 
 									  getLocalizedString(locale, "tool.contenttool.publish.label"),
 									  "javascript:validateAndSubmitContentFormThenSubmitToPublish();",
 				  					  "images/v3/publishIcon.gif"));
 		*/
 		
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
 				  					  "parent.closeDialog();",
				  					  "images/v3/cancelIcon.gif"));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getCreateSiteNodeButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.save.label"), 
 				  getLocalizedString(locale, "tool.contenttool.save.label"),
 				  "javascript:validateAndSubmitContentForm();",
 				  "images/v3/saveInlineIcon.gif"));
 	
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getCreateSiteNodeFooterButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.saveButton.label"), 
 									  getLocalizedString(locale, "tool.common.saveButton.label"),
 									  "document.editForm.submit()",
 									  "images/v3/createBackgroundPenPaper.gif",
 				  					  "left",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "parent.closeDialog();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  true));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getMessageCenterFooterButtons()
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.nextButton.label"), 
 									  getLocalizedString(locale, "tool.common.nextButton.label"),
 									  "submitForm();",
 									  "images/v3/nextBackground.gif",
 				  					  "left",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "parent.closeDialog();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  true));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSaveCancelFooterButtons()
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.saveButton.label"), 
 									  getLocalizedString(locale, "tool.common.saveButton.label"),
 									  "submitForm();",
 									  "images/v3/createBackgroundPenPaper.gif",
 				  					  "left",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "parent.closeDialog();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  true));
 
 		return buttons;
 	}
 
 	/*
 	private List<ToolbarButton> getRepositoriesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateRepository!input.action", getLocalizedString(locale, "images.managementtool.buttons.newRepository"), "tool.managementtool.createRepository.header"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('repository');", getLocalizedString(locale, "images.managementtool.buttons.deleteRepository"), "tool.managementtool.deleteRepositories.header"));
 		buttons.add(new ToolbarButton(true, "javascript:openPopup('ImportRepository!input.action', 'Import', 'width=400,height=250,resizable=no');", getLocalizedString(locale, "images.managementtool.buttons.importRepository"), getLocalizedString(locale, "tool.managementtool.importRepository.header")));	
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getRepositoryDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteRepository.header&yesDestination=" + URLEncoder.encode("DeleteRepository.action?repositoryId=" + primaryKey, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListRepository.action?title=Repositories", "UTF-8") + "&message=tool.managementtool.deleteRepository.text&extraParameters=" + extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteRepository"), "tool.managementtool.deleteRepository.header"));
 		buttons.add(new ToolbarButton(true, "javascript:openPopup('ExportRepository!input.action?repositoryId=" + primaryKey + "', 'Export', 'width=600,height=500,resizable=no,scrollbars=yes');", getLocalizedString(locale, "images.managementtool.buttons.exportRepository"), getLocalizedString(locale, "tool.managementtool.exportRepository.header")));	
 		buttons.add(new ToolbarButton("ViewRepositoryProperties.action?repositoryId=" + primaryKey, getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewRepository.action?repositoryId=" + primaryKey, "UTF-8"), "UTF-8");
 		buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Repository&extraParameters=" + primaryKey +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header"));
 		buttons.add(new ToolbarButton("ViewListRepositoryLanguage.action?repositoryId=" + primaryKey +"&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.repositoryLanguages"), "tool.managementtool.repositoryLanguages.header"));
 		
 		buttons.add(new ToolbarButton(true, "javascript:openPopup('RebuildRegistry!input.action?repositoryId=" + primaryKey + "', 'Registry', 'width=400,height=200,resizable=no');", getLocalizedString(locale, "images.managementtool.buttons.rebuildRegistry"), getLocalizedString(locale, "tool.managementtool.rebuildRegistry.header")));	
 		
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getAvailablePackagesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("RefreshUpdates.action", getLocalizedString(locale, "images.managementtool.buttons.refreshUpdates"), "Refresh Updates"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('updatePackage');", getLocalizedString(locale, "images.managementtool.buttons.installUpdate"), "Install update"));
 		return buttons;
 	}
 	*/
 	
 	private List<ToolbarButton> getGlobalSubscriptionsButtons()
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.createSubscription.header"), 
 				  getLocalizedString(locale, "tool.common.createSubscription.header"),
 				  "showDiv('newSubscriptionForm')",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "left",
 				  true));
 		
 		return buttons;
 	}
 
 	
 	private List<ToolbarButton> getSystemUsersButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		if(UserControllerProxy.getController().getSupportCreate())
 		{
 			boolean hasAccessToCreateRole = hasAccessTo(principal, "SystemUser.Create", true);
 			if(hasAccessToCreateRole)
 			{
 				buttons.add(new ToolbarButton("",
 											  getLocalizedString(locale, "tool.managementtool.createSystemUser.header"), 
 											  getLocalizedString(locale, "tool.managementtool.createSystemUser.header"),
 											  "CreateSystemUser!inputV3.action",
 											  "images/v3/createBackgroundPenPaper.gif"));
 			}
 		}
 		
 		/*		
 		buttons.add(new ToolbarButton(true, "javascript:toggleSearchForm();", getLocalizedString(locale, "images.managementtool.buttons.searchButton"), "Search Form"));
 		*/
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSystemUserDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String yesDestination 	= URLEncoder.encode("DeleteSystemUser!v3.action?userName=" + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListSystemUser!v3.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the user " + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 
 		if(!this.primaryKey.equals(CmsPropertyHandler.getAnonymousUser()))
 		{
 			InfoGluePrincipal user = UserControllerProxy.getController().getUser(primaryKey);
 			if(user.getAutorizationModule().getSupportDelete())
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"),
 						  "Confirm.action?header=tool.managementtool.deleteSystemUser.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteSystemUser.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/createBackgroundPenPaper.gif"));
 
 				//buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteSystemUser.header&yesDestination=" + URLEncoder.encode("DeleteSystemUser.action?userName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding) + "&noDestination=" + URLEncoder.encode("ViewListSystemUser.action?title=SystemUsers", URIEncoding) + "&message=tool.managementtool.deleteSystemUser.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.deleteSystemUser"), "tool.managementtool.deleteSystemUser.header"));
 			}
 		
 			if(user.getAutorizationModule().getSupportUpdate())
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"), 
 						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"),
 						  "UpdateSystemUserPassword!inputV3.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding),
 						  "images/v3/passwordIcon.gif"));
 
 				//buttons.add(new ToolbarButton("UpdateSystemUserPassword!input.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.updateSystemUserPassword"), "Update user password"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = UserPropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"),
 					  "ViewUserProperties.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding),
 					  "images/v3/advancedSettingsIcon.gif"));
 
 			//buttons.add(new ToolbarButton("ViewUserProperties.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.viewSystemUserProperties"), "View User Properties"));
 		}
 		
 		if(principal.getIsAdministrator())
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"), 
 					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"),
 					  "AuthorizationSwitchManagement!inputUser.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)),
 					  "images/v3/createBackgroundPenPaper.gif"));
 
 			//buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputUser.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferUserAccessRights"), "Transfer Users Access Rights"));
 		}
 
 		return buttons;				
 	}
 	
 	private List<ToolbarButton> getRolesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		if(RoleControllerProxy.getController().getSupportCreate())
 		{
 			boolean hasAccessToCreateRole = hasAccessTo(principal, "Role.Create", true);
 			if(hasAccessToCreateRole)
 			{
 				buttons.add(new ToolbarButton("",
 											  getLocalizedString(locale, "tool.managementtool.createRole.header"), 
 											  getLocalizedString(locale, "tool.managementtool.createRole.header"),
 											  "CreateRole!inputV3.action",
 											  "images/v3/createBackgroundPenPaper.gif"));
 			}
 		}
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getRoleDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		String yesDestination 	= URLEncoder.encode("DeleteRole!v3.action?roleName=" + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListRole!listManagableRoles.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the role " + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueRole role = RoleControllerProxy.getController().getRole(this.primaryKey);
 		if(role.getAutorizationModule().getSupportDelete())
 		{
 			boolean hasAccessToDeleteRole = hasAccessTo(principal, "Role.Delete", "" + this.primaryKey);
 			if(hasAccessToDeleteRole)
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteRole.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteRole.header"),
 						  "Confirm.action?header=tool.managementtool.deleteRole.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteRole.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/deleteBackgroundWasteBasket.gif"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = RolePropertiesController.getController().getContentTypeDefinitionVOList(this.primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			boolean hasAccessToEditProperties = hasAccessTo(principal, "Role.EditProperties", true);
 			if(hasAccessToEditProperties)
 			{
 				buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"),
 					  "ViewRoleProperties!v3.action?roleName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)),
 					  "images/v3/advancedSettingsIcon.gif"));
 			}
 		}
 
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Role.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Role.ManageAccessRights", "" + this.primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 		{
 			buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights.action?interceptionPointCategory=Role&extraParameters=" + URLEncoder.encode(this.primaryKey, URIEncoding) + "&returnAddress=ViewRole!v3.action?roleName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
 				  "images/v3/accessRightsIcon.gif"));
 		}
 		/*
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputRole.action?roleName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferRoleAccessRights"), "Transfer Roles Access Rights"));
 		*/
 
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getGroupsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		if(GroupControllerProxy.getController().getSupportCreate())
 		{
 			boolean hasAccessToCreateGroup = hasAccessTo(principal, "Group.Create", true);
 			if(hasAccessToCreateGroup)
 			{
 				buttons.add(new ToolbarButton("",
 											  getLocalizedString(locale, "tool.managementtool.createGroup.header"), 
 											  getLocalizedString(locale, "tool.managementtool.createGroup.header"),
 											  "CreateGroup!inputV3.action",
 											  "images/v3/createBackgroundPenPaper.gif"));
 			}
 		}
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getGroupDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		String yesDestination 	= URLEncoder.encode("DeleteGroup!v3.action?groupName=" + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListGroup!listManagableGroups.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the group " + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(this.primaryKey);
 		if(group.getAutorizationModule().getSupportDelete())
 		{
 			boolean hasAccessToDeleteGroup = hasAccessTo(principal, "Group.Delete", "" + this.primaryKey);
 			if(hasAccessToDeleteGroup)
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteGroup.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteGroup.header"),
 						  "Confirm.action?header=tool.managementtool.deleteGroup.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteGroup.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/deleteBackgroundWasteBasket.gif"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = GroupPropertiesController.getController().getContentTypeDefinitionVOList(this.primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			boolean hasAccessToEditProperties = hasAccessTo(principal, "Group.EditProperties", true);
 			if(hasAccessToEditProperties)
 			{
 				buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"),
 					  "ViewGroupProperties!v3.action?groupName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)),
 					  "images/v3/advancedSettingsIcon.gif"));
 			}
 		}
 
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + this.primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 		{
 			buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(this.primaryKey, URIEncoding) + "&returnAddress=ViewGroup!v3.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
 				  "images/v3/accessRightsIcon.gif"));
 		}
 		/*
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
 		*/
 
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getGroupPropertiesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "javascript:openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.GroupProperties&entityId=" + this.primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif"));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getRolePropertiesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "javascript:openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.RoleProperties&entityId=" + this.primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif"));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getUserPropertiesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "javascript:openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.UserProperties&entityId=" + this.primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif"));
 		
 		return buttons;
 	}
 
 	/*
 	private List<ToolbarButton> getGroupsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		if(UserControllerProxy.getController().getSupportCreate())
 			buttons.add(new ToolbarButton("CreateGroup!input.action", getLocalizedString(locale, "images.managementtool.buttons.newGroup"), "New Group"));	
 		//if(UserControllerProxy.getController().getSupportDelete())
 		//	buttons.add(new ToolbarButton(true, "javascript:submitListFormWithPrimaryKey('group', 'groupName');", getLocalizedString(locale, "images.managementtool.buttons.deleteGroup"), "tool.managementtool.deleteGroups.header"));
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getGroupDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		String yesDestination 	= URLEncoder.encode("DeleteGroup.action?groupName=" + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListGroup.action?title=Groups", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the group " + URLEncoder.encode(this.primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(this.primaryKey);
 		if(group.getAutorizationModule().getSupportDelete())
 			buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteGroup.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteGroup.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.deleteGroup"), "tool.managementtool.deleteGroup.header"));
 		
 		List<ToolbarButton> contentTypeDefinitionVOList<ToolbarButton> = GroupPropertiesController.getController().getContentTypeDefinitionVOList(this.primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 			buttons.add(new ToolbarButton("ViewGroupProperties.action?groupName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.viewGroupProperties"), "View Group Properties"));
 		
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(this.primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
 				
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + this.primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewGroup.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool", getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "Group Access Rights"));
 
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getLanguagesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateLanguage!input.action", getLocalizedString(locale, "images.managementtool.buttons.newLanguage"), "New Language"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('language');", getLocalizedString(locale, "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguages.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getLanguageDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = LanguageController.getController().getLanguageVOWithId(this.primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteLanguage.header&yesDestination=" + URLEncoder.encode("DeleteLanguage.action?languageId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListLanguage.action?title=Languages", "UTF-8") + "&message=tool.managementtool.deleteLanguage.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguage.header"));
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getInterceptionPointsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateInterceptionPoint!input.action", getLocalizedString(locale, "images.managementtool.buttons.newInterceptionPoint"), "New InterceptionPoint"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('interceptionPoint');", getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoints.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getInterceptionPointButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(this.primaryKeyAsInteger);
 		String name = interceptionPointVO.getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteInterceptionPoint.header&yesDestination=" + URLEncoder.encode("DeleteInterceptionPoint.action?interceptionPointId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListInterceptionPoint.action?title=InterceptionPoints", "UTF-8") + "&message=tool.managementtool.deleteInterceptionPoint.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoint.header"));
 		if(interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue() == false)
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=" + interceptionPointVO.getCategory() + "&interceptionPointId=" + primaryKeyAsInteger + "&returnAddress=ViewInterceptionPoint.action?interceptionPointId=" + primaryKeyAsInteger + "&colorScheme=ManagementTool", getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "InterceptionPoint Access Rights"));
 		
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getInterceptorsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateInterceptor!input.action", getLocalizedString(locale, "images.managementtool.buttons.newInterceptor"), "New Interceptor"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('interceptor');", getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptor"), "tool.managementtool.deleteInterceptors.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getInterceptorButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = InterceptorController.getController().getInterceptorVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteInterceptor.header&yesDestination=" + URLEncoder.encode("DeleteInterceptor.action?interceptorId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListInterceptor.action?title=Interceptors", "UTF-8") + "&message=tool.managementtool.deleteInterceptor.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptor"), "tool.managementtool.deleteInterceptor.header"));
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getServiceDefinitionsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateServiceDefinition!input.action", getLocalizedString(locale, "images.managementtool.buttons.newServiceDefinition"), "New ServiceDefinition"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('serviceDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteServiceDefinition"), "tool.managementtool.deleteServiceDefinitions.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getServiceDefinitionDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = ServiceDefinitionController.getController().getServiceDefinitionVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteServiceDefinition.header&yesDestination=" + URLEncoder.encode("DeleteServiceDefinition.action?serviceDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListServiceDefinition.action?title=ServiceDefinitions", "UTF-8") + "&message=tool.managementtool.deleteServiceDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteServiceDefinition"), "tool.managementtool.deleteServiceDefinition.header"));
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getAvailableServiceBindingsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateAvailableServiceBinding!input.action", getLocalizedString(locale, "images.managementtool.buttons.newAvailableServiceBinding"), "New AvailableServiceBinding"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('availableServiceBinding');", getLocalizedString(locale, "images.managementtool.buttons.deleteAvailableServiceBinding"), "tool.managementtool.deleteAvailableServiceBindings.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getAvailableServiceBindingDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithId(this.primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteAvailableServiceBinding.header&yesDestination=" + URLEncoder.encode("DeleteAvailableServiceBinding.action?availableServiceBindingId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListAvailableServiceBinding.action?title=AvailableServiceBindings", "UTF-8") + "&message=tool.managementtool.deleteAvailableServiceBinding.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteAvailableServiceBinding"), "tool.managementtool.deleteAvailableServiceBinding.header"));
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getSiteNodeTypeDefinitionsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateSiteNodeTypeDefinition!input.action", getLocalizedString(locale, "images.managementtool.buttons.newSiteNodeTypeDefinition"), "New SiteNodeTypeDefinition"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('siteNodeTypeDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteSiteNodeTypeDefinition"), "tool.managementtool.deleteSiteNodeTypeDefinitions.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getSiteNodeTypeDefinitionDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(this.primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteSiteNodeTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteSiteNodeTypeDefinition.action?siteNodeTypeDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListSiteNodeTypeDefinition.action?title=SiteNodeTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteSiteNodeTypeDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteSiteNodeTypeDefinition"), "tool.managementtool.deleteSiteNodeTypeDefinition.header"));
 		return buttons;				
 	}
 
 
 	private List<ToolbarButton> getContentTypeDefinitionsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateContentTypeDefinition!input.action", getLocalizedString(locale, "images.managementtool.buttons.newContentTypeDefinition"), "New ContentTypeDefinition"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('contentTypeDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteContentTypeDefinition"), "tool.managementtool.deleteContentTypeDefinitions.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getContentTypeDefinitionDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteContentTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteContentTypeDefinition.action?contentTypeDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListContentTypeDefinition.action?title=ContentTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteContentTypeDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteContentTypeDefinition"), "tool.managementtool.deleteContentTypeDefinition.header"));
 		
 		String protectContentTypes = CmsPropertyHandler.getProtectContentTypes();
 		if(protectContentTypes != null && protectContentTypes.equalsIgnoreCase("true"))
 		{
 			String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContentTypeDefinition.action?contentTypeDefinitionId=" + this.primaryKey, "UTF-8"), "UTF-8");
 			buttons.add(getAccessRightsButton("ContentTypeDefinition", this.primaryKey, returnAddress));
 		}
 		
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getCategoryButtons() throws Exception
 	{
 	    String url = "CategoryManagement!new.action";
 		if(primaryKeyAsInteger != null)
 			url += "?model/parentId=" + primaryKeyAsInteger;
 
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton(url, getLocalizedString(locale, "images.managementtool.buttons.newCategory"), "New Category"));
 
 		if(primaryKeyAsInteger != null)
 			buttons.add(new ToolbarButton(true, "javascript:openPopup('CategoryManagement!displayTreeForMove.action?categoryId=" + this.primaryKey + "', 'Category', 'width=400,height=600,resizable=no,status=yes');", getLocalizedString(locale, "images.managementtool.buttons.moveCategory"), "Move Category"));
 
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('category');", getLocalizedString(locale, "images.managementtool.buttons.deleteCategory"), "Delete Category"));
 		
 		if(primaryKeyAsInteger != null)
 		{	
 		    String returnAddress = URLEncoder.encode(URLEncoder.encode("CategoryManagement!edit.action?categoryId=" + this.primaryKey + "&title=Category%20Details", "UTF-8"), "UTF-8");
 		    buttons.add(getAccessRightsButton("Category", this.primaryKey, returnAddress));
 		}
 		
 		return buttons;
 	}
 	
 	private ToolbarButton getAccessRightsButton(String interceptionPointCategory, String extraParameter, String returnAddress) throws Exception
 	{
 		return new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=" + interceptionPointCategory + "&extraParameters=" + extraParameter +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header");
 	}
 
 	private List<ToolbarButton> getRedirectsButtons() throws Exception
 	{
 		
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateRedirect!input.action", getLocalizedString(locale, "images.managementtool.buttons.newRedirect"), "New Redirect"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('redirect');", getLocalizedString(locale, "images.managementtool.buttons.deleteRedirect"), "tool.managementtool.deleteRedirects.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getRedirectDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = RedirectController.getController().getRedirectVOWithId(this.primaryKeyAsInteger).getUrl();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteRedirect.header&yesDestination=" + URLEncoder.encode("DeleteRedirect.action?redirectId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListWorkflowDefinition.action", "UTF-8") + "&message=tool.managementtool.deleteWorkflowDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinition.header"));
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getPortletsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("UploadPortlet.action", getLocalizedString(locale, "images.managementtool.buttons.newPortlet"), "New Portlet"));	
 		//buttons.add(new ToolbarButton(true, "javascript:submitListForm('workflowDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinitions.header"));
 		return buttons;
 	}
 
 	
 	private List<ToolbarButton> getWorkflowDefinitionsButtons() throws Exception
 	{
 		
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateWorkflowDefinition!input.action", getLocalizedString(locale, "images.managementtool.buttons.newWorkflowDefinition"), "New WorkflowDefinition"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('workflowDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinitions.header"));
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getWorkflowDefinitionDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(this.primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteWorkflowDefinition.header&yesDestination=" + URLEncoder.encode("DeleteWorkflowDefinition.action?workflowDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListWorkflowDefinition.action", "UTF-8") + "&message=tool.managementtool.deleteWorkflowDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinition.header"));
 	    final String protectWorkflows = CmsPropertyHandler.getProtectWorkflows();
 	    if(protectWorkflows != null && protectWorkflows.equalsIgnoreCase("true"))
 	    {
 			String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewWorkflowDefinition.action?workflowDefinitionId=" + this.primaryKey, "UTF-8"), "UTF-8");
 			final WorkflowDefinitionVO workflowDefinition = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(this.primaryKeyAsInteger);
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Workflow&extraParameters=" + workflowDefinition.getName() +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header"));
 	    }
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getServerNodesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateServerNode!input.action", getLocalizedString(locale, "images.managementtool.buttons.newServerNode"), "tool.managementtool.createServerNode.header"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('serverNode');", getLocalizedString(locale, "images.managementtool.buttons.deleteServerNode"), "tool.managementtool.deleteServerNodes.header"));
 		buttons.add(new ToolbarButton("ViewServerNodeProperties.action?serverNodeId=-1", getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getServerNodeDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteServerNode.header&yesDestination=" + URLEncoder.encode("DeleteServerNode.action?serverNodeId=" + this.primaryKey, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListServerNode.action?title=ServerNodes", "UTF-8") + "&message=tool.managementtool.deleteServerNode.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteServerNode"), "tool.managementtool.deleteServerNode.header"));
 		buttons.add(new ToolbarButton("ViewServerNodeProperties.action?serverNodeId=" + this.primaryKey, getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getMessageCenterButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateEmail!inputChooseRecipients.action", getLocalizedString(locale, "images.managementtool.buttons.newEmail"), "tool.managementtool.createEmail.header"));
 		
 		return buttons;
 	}
 	*/
 	
 	private List<ToolbarButton> getHelpButton() throws Exception
 	{
 		String helpPageBaseUrl = "http://www.infoglue.org";
 		
 		String helpPageUrl = "";
 
 		if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
 			helpPageUrl = "/help/tools/contenttool/contentVersion";
 
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
 			helpPageUrl = "/help/tools/managementtool/roles";
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 			helpPageUrl = "/help/tools/managementtool/role";
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.createRole.header"))
 			helpPageUrl = "/help/tools/managementtool/create_role";
 
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
 			helpPageUrl = "/help/tools/managementtool/groups";
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
 			helpPageUrl = "/help/tools/managementtool/group";
 		if(toolbarKey.equalsIgnoreCase("tool.managementtool.createGroup.header"))
 			helpPageUrl = "/help/tools/managementtool/create_group";
 
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("helpButton",
 									  getLocalizedString(locale, "tool.common.helpButton.label"), 
 									  getLocalizedString(locale, "tool.common.helpButton.title"),
 									  helpPageUrl,
 									  "images/v3/helpIcon.gif"));
 		return buttons;
 	}
 
 	private List<ToolbarButton> getWindowCloseButton() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("exitButton",
 									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
 									  getLocalizedString(locale, "tool.common.closeWindowButton.title"),
 									  "parent.closeDialog();",
 				  					  "images/v3/closeWindowIcon.gif",
 				  					  "right",
 				  					  true));
 		return buttons;
 	}
 
 	/**
 	 * Used by the view pages to determine if the current user has sufficient access rights
 	 * to perform the action specific by the interception point name.
 	 *
 	 * @param interceptionPointName THe Name of the interception point to check access rights
 	 * @return True is access is allowed, false otherwise
 	 */
 	public boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, boolean returnSuccessIfInterceptionPointNotDefined)
 	{
 		logger.info("Checking if " + principal.getName() + " has access to " + interceptionPointName);
 
 		try
 		{
 			return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, returnSuccessIfInterceptionPointNotDefined);
 		}
 		catch (SystemException e)
 		{
 		    logger.warn("Error checking access rights", e);
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
 	public boolean hasAccessTo(InfoGluePrincipal principal, String interceptionPointName, String extraParameter)
 	{
 		logger.info("Checking if " + principal.getName() + " has access to " + interceptionPointName + " with extraParameter " + extraParameter);
 
 		try
 		{
 		    return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, extraParameter);
 		}
 		catch (SystemException e)
 		{
 		    logger.warn("Error checking access rights", e);
 			return false;
 		}
 	}
 	
 	public String getLocalizedString(Locale locale, String key) 
   	{
     	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
 
     	return stringManager.getString(key);
   	}
 }
