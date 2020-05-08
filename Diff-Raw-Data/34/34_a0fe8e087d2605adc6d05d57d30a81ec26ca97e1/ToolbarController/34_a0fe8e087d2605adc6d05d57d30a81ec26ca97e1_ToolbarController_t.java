 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.infoglue.cms.applications.common.ImageButton;
 import org.infoglue.cms.applications.common.ToolbarButton;
 import org.infoglue.cms.applications.common.ToolbarButton;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.InterceptionPointVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
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
 	
 	public List<ToolbarButton> getRightToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		try
 		{
 			List<ToolbarButton> toolbarButtons = new ArrayList<ToolbarButton>();
 	
 			//if(toolbarKey.equalsIgnoreCase("tool.common.adminTool.header"))
 			//	toolbarButtons.addAll(getMySettingsButton(toolbarKey, principal, locale, request, disableCloseButton));
 			toolbarButtons.addAll(getHelpButton(toolbarKey, principal, locale, request, disableCloseButton));
 			
 			if(!disableCloseButton)
 			{
 				toolbarButtons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
 			}
 			
 			return toolbarButtons;
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;	
 	}
 	
 	public List<ToolbarButton> getToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		Timer t = new Timer();
 		
 		System.out.println("toolbarKey:" + toolbarKey);
 		logger.info("toolbarKey:" + toolbarKey);
 		
 		try
 		{
 			if(toolbarKey.equalsIgnoreCase("tool.common.adminTool.header"))
 				return getAdminToolStandardButtons();
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentHeader"))
 				return getContentButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
 				return getContentVersionButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionStandaloneHeader"))
 				return getContentVersionStandaloneButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionWizardHeader"))
 				return getContentVersionButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.common.globalSubscriptions.header"))
 				return getGlobalSubscriptionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeComponentsHeader"))
 				return getSiteNodeButtons(toolbarKey, principal, locale, request);
 							
 			
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
 				return getCreateSiteNodeButtons();
 			*/
 			
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
 				return getRepositoriesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
 				return getRepositoryDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
 				return getGroupPropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
 				return getRolePropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
 				return getUserPropertiesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUserList.header"))
 				return getSystemUsersButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
 				return getSystemUserDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
 				return getRolesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 				return getRoleDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupList.header"))
 				return getGroupsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
 				return getGroupDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
 				return getLanguagesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
 				return getLanguageDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
 				return getInterceptionPointsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
 				return getInterceptionPointButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
 				return getInterceptorsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
 			//	return getInterceptorButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
 				return getServiceDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
 			//	return getServiceDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
 				return getAvailableServiceBindingsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
 			//	return getAvailableServiceBindingDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
 				return getSiteNodeTypeDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
 			//	return getSiteNodeTypeDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
 				return getContentTypeDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
 			//	return getContentTypeDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
 				return getCategoryButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
 			//	return getAvailablePackagesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinitionList.header"))
 				return getWorkflowDefinitionsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewWorkflowDefinition.header"))
 			//	return getWorkflowDefinitionDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.portletList.header"))
 				return getPortletsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.portlet.header"))
 			//	return getPortletDetailsButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.redirectList.header"))
 				return getRedirectsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRedirect.header"))
 			//	return getRedirectDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.serverNodeList.header"))
 				return getServerNodesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			//if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewServerNode.header"))
 			//	return getServerNodeDetailsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
 				return getMessageCenterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.themes.header"))
 				return getThemesButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.labels.header"))
 				return getLabelsButtons(toolbarKey, principal, locale, request, disableCloseButton);
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;				
 	}
 	
 
 	public List<ToolbarButton> getFooterToolbarButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		Timer t = new Timer();
 		
 		logger.info("toolbarKey:" + toolbarKey);
 				
 		try
 		{
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.accessRights.header"))
 				return getCommonFooterSaveOrSaveAndExitOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateAccessRights!saveAndExitV3.action");
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentHeader"))
 				return asButtons(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.createContentHeader") || toolbarKey.equalsIgnoreCase("tool.contenttool.createFolderHeader"))
 				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionHeader"))
 				return getContentVersionFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionStandaloneHeader"))
 				return getContentVersionFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentVersionWizardHeader"))
 				return getContentVersionWizardFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.contentPropertiesHeader"))
 				return getCommonFooterSaveOrSaveAndExitOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewContentProperties!saveAndExitV3.action");
 
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.createSiteNodeHeader"))
 				return getCreateSiteNodeFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewMessageCenter.header"))
 				return getMessageCenterFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.common.subscriptions.header"))
 				return getSaveCancelFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.publishSiteNode.header"))
 				return getPublishPageFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.common.unpublishing.unpublishContentsHeader"))
 				return getUnPublishContentsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.mysettings.header"))
 				return getMySettingsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			/*
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
 				return getRepositoriesButtons();
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
 				return getRepositoryDetailsButtons();
 			*/
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroupProperties.header"))
 				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleProperties.header"))
 				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewUserProperties.header"))
 				return getEntityPropertiesFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createSystemUser.header"))
 				return getCreateSystemUserFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
 				return getSystemUserDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 				return getRoleDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createRole.header"))
 				return getCreateRoleFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.viewGroup.header"))
 				return getGroupDetailFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.createGroup.header"))
 				return getCreateGroupFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.assetDialog.assetDialogForMultipleBindingsHeader"))
 				return getAssetDialogForMultipleBindingsFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			
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
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentQuick.header"))
 				return getQuickDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentVC.header"))
 				return getVCDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.deploymentVC.chooseTagHeader"))
 				return getVCDeployFooterButtons(toolbarKey, principal, locale, request, disableCloseButton);
 		
 			if(toolbarKey.equalsIgnoreCase("tool.structuretool.moveSiteNode.header"))
 				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
 			
 			if(toolbarKey.equalsIgnoreCase("tool.contenttool.moveContent.header"))
 				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
 
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.uploadTheme.header"))
 				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
 			if(toolbarKey.equalsIgnoreCase("tool.managementtool.uploadTranslation.header"))
 				return getCommonFooterSaveOrCloseButton(toolbarKey, principal, locale, request, disableCloseButton);
 
 		}
 		catch(Exception e) {e.printStackTrace();}			
 					
 		return null;				
 	}
 
 	private List<ToolbarButton> getAssetDialogForMultipleBindingsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		//buttons.add(getCommonFooterCancelButton("ViewListGroup!listManagableGroups.action"));
 				
 		return buttons;
 	}
 
 	
 	private List<ToolbarButton> getGroupDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateGroup!saveAndExitV3.action"));
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListGroup!listManagableGroups.action"));
 				
 		return buttons;
 	}
 
 	private List<ToolbarButton> getCreateGroupFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateGroup!saveAndExitV3.action"));
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListGroup!listManagableGroups.action"));
 				
 		return buttons;
 	}
 
 	private List<ToolbarButton> getRoleDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateRole!saveAndExitV3.action"));
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListRole!listManagableRoles.action"));
 				
 		return buttons;
 	}
 
 	private List<ToolbarButton> getCreateRoleFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateRole!saveAndExitV3.action"));
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListRole!listManagableRoles.action"));
 				
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSystemUserDetailFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		String primaryKey = request.getParameter("userName");
 		if(primaryKey == null || primaryKey.equals(""))
 			throw new Exception("Missing argument userName for primaryKey.");
 		
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(primaryKey);
 		if(infoGluePrincipal == null)
 			throw new SystemException("No user found called '" + primaryKey + "'. This could be an encoding issue if you gave your user a login name with non ascii chars in it. Look in the administrative manual on how to solve it.");
 		boolean supportsUpdate = infoGluePrincipal.getAutorizationModule().getSupportUpdate();
 		
 		if(supportsUpdate)
 		{
 			buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 			buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "UpdateSystemUser!saveAndExitV3.action"));
 		}
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListSystemUser!v3.action"));
 				
 		return buttons;
 	}
 
 	private List<ToolbarButton> getCreateSystemUserFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, "CreateSystemUser!saveAndExitV3.action"));
 		buttons.add(getCommonFooterCancelButton(toolbarKey, principal, locale, request, disableCloseButton, "ViewListSystemUser!listManagableSystemUsers.action"));
 				
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getEntityPropertiesFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.save.label"), 
 									  getLocalizedString(locale, "tool.contenttool.save.label"),
 									  "validateAndSubmitContentForm();",
 									  "images/v3/saveInlineIcon.gif",
 									  "left",
 									  "save",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
 									  "validateAndSubmitContentFormThenExit();",
 									  "images/v3/saveAndExitInlineIcon.gif",
 									  "left",
 									  "saveAndExit",
 									  true));
 		
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
 				  					  "cancel();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  "cancel",
 				  					  true));
 		
 		return buttons;
 	}
 	
 	
 	private List<ToolbarButton> getAdminToolStandardButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		/*
 		ToolbarButton languageDropButton = new ToolbarButton("",
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()), 
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()),
 				  											 "",
 					  										 "images/v3/menu-button-arrow.png",
 					  										 "right",
 					  										 "dropArrow",
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
 						 "",
 						 ""));
 			}
 		}
 		
 		buttons.add(languageDropButton);
 		*/
 		
 		return buttons;
 	}
 	
 	
 	private List<ToolbarButton> getContentButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		Integer contentId = new Integer(request.getParameter("contentId"));
 		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 		
		
 		ToolbarButton createButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentTitle"),
 				  "CreateContent!inputV3.action?isBranch=false&repositoryId=" + contentVO.getRepositoryId() + "&parentContentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "create",
 				  "inlineDiv");
 
 		ToolbarButton createFolderButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentFolderLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.createContentFolderTitle"),
 				  "CreateContent!inputV3.action?isBranch=true&repositoryId=" + contentVO.getRepositoryId() + "&parentContentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "create",
 				  "inlineDiv");
 
 		createButton.getSubButtons().add(createFolderButton);
 		buttons.add(createButton);
 
 		ToolbarButton moveButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentTitle"),
 				  "MoveContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "moveContent");
 
 		ToolbarButton moveMultipleButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveMultipleContentTitle"),
 				  "MoveMultipleContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "moveContent");
 		
 		moveButton.getSubButtons().add(moveMultipleButton);
 		buttons.add(moveButton);
 
 		ToolbarButton deleteButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentTitle"),
 				  "DeleteContent!V3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "",
 				  "delete",
 				  true,
 				  true,
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentConfirmationLabel", new String[]{contentVO.getName()}),
 				  "inlineDiv");
 		
 		ToolbarButton deleteChildrenButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenTitle"),
 				  "DeleteContentChildren.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "",
 				  "delete",
 				  true,
 				  true,
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentChildrenConfirmationLabel", new String[]{contentVO.getName()}),
 				  "inlineDiv");
 		
 		deleteButton.getSubButtons().add(deleteChildrenButton);
 		buttons.add(deleteButton);
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.editContentMetaInfoTitle"),
 				  "ViewContentProperties!V3.action?contentId=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "properties"));
 
 		ToolbarButton publishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentTitle"),
 				  "ViewListContentVersion!V3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "publish");
 
 		ToolbarButton submitToPublishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsTitle"),
 				  "ViewListContentVersion!V3.action?contentId=" + contentId + "&recurseContents=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "submitToPublish");
 
 		publishButton.getSubButtons().add(submitToPublishButton);
 		buttons.add(publishButton);
 
 		ToolbarButton unpublishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsTitle"),
 				  "UnpublishContentVersion!inputV3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "unpublish");
 
 		ToolbarButton unpublishAllButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllTitle"),
 				  "UnpublishContentVersion!inputChooseContents.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "unpublish");
 
 		unpublishButton.getSubButtons().add(unpublishAllButton);
 		buttons.add(unpublishButton);
 
		if(contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=Content&extraParameters=" + contentVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights"));
		}
		
 		ToolbarButton syncTreeButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeTitle"),
 				  "syncWithTree();",
 				  "",
 				  "syncTree");
 
 		ToolbarButton runTaskButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.runTaskTitle"),
 				  "ViewExecuteTask.action?contentId=" + contentId + "",
 				  "",
 				  "runTask");
 
 		ToolbarButton changeContentTypeButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeContentTypeDefinitionTitle"),
 				  "UpdateContent!inputContentType.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
 				  "",
 				  "changeContentType");
 
 		ToolbarButton exportContentButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.exportContentTitle"),
 				  "ExportContent!input.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
 				  "",
 				  "exportContent");
 
 		ToolbarButton importContentButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.importContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.importContentTitle"),
 				  "ImportContent!input.action?parentContentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId(),
 				  "",
 				  "importContent");
 
 		syncTreeButton.getSubButtons().add(runTaskButton);
 		syncTreeButton.getSubButtons().add(changeContentTypeButton);
 		syncTreeButton.getSubButtons().add(exportContentButton);
 		syncTreeButton.getSubButtons().add(importContentButton);
 		buttons.add(syncTreeButton);
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getContentVersionStandaloneButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		Timer t = new Timer();
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		LanguageVO currentLanguageVO = null;
 		ContentVO contentVO = null;
 		
 		Integer primaryKeyAsInteger = null;
 		try
 		{
 			primaryKeyAsInteger = new Integer(request.getParameter("contentVersionId"));
 		}
 		catch (Exception e) 
 		{
 		}
 		
 		if(primaryKeyAsInteger != null)
 		{
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(primaryKeyAsInteger);
 			contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
 			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(contentVersionVO.getLanguageId());
 		}
 		else
 		{
 			contentVO = ContentController.getContentController().getContentVOWithId(new Integer((String)request.getParameter("contentId")));
 			currentLanguageVO = LanguageController.getController().getLanguageVOWithId(new Integer((String)request.getParameter("languageId")));
 		}
 		
 		ToolbarButton languageDropButton = new ToolbarButton("",
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()), 
 															 StringUtils.capitalize(currentLanguageVO.getDisplayLanguage()),
 				  											 "",
 					  										 "images/v3/menu-button-arrow.png",
 					  										 "right",
 					  										 "dropArrow",
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
 						 "",
 						 ""));
 			}
 		}
 		
 		buttons.add(languageDropButton);
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getContentVersionButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		Integer contentId = new Integer(request.getParameter("contentId"));
 		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 		
 		String contentVersionIdString = request.getParameter("contentVersionId");
 		Integer contentVersionId = null;
 		if(contentVersionIdString == null)
 		{
 			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
 			if(contentVersionVO != null)
 				contentVersionId = contentVersionVO.getId();
 		}
 		else
 		{
 			contentVersionId = new Integer(contentVersionIdString);
 		}
 		
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.moveContentTitle"),
 				  "MoveContent!inputV3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "moveContent"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentTitle"),
 				  "DeleteContent!V3.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "",
 				  "delete",
 				  true,
 				  true,
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.deleteContentConfirmationLabel", new String[]{contentVO.getName()}),
 				  "inlineDiv"));
 
 		ToolbarButton publishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentTitle"),
 				  "ViewListContentVersion!v3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "publish");
 
 		ToolbarButton submitToPublishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.publishContentsTitle"),
 				  "ViewListContentVersion!v3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "submitToPublish");
 
 		publishButton.getSubButtons().add(submitToPublishButton);
 		buttons.add(publishButton);
 		
 		ToolbarButton unpublishButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsTitle"),
 				  "UnpublishContentVersion!inputV3.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "unpublish");
 
 		ToolbarButton unpublishAllButton = new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.unpublishContentsAllTitle"),
 				  "UnpublishContentVersion!inputChooseContents.action?contentId=" + contentId + "&recurseContents=false&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "unpublish");
 
 		unpublishButton.getSubButtons().add(unpublishAllButton);
 		buttons.add(unpublishButton);
 
		if(contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=Content&extraParameters=" + contentId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights"));
		}

 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeLabel"), 
 				  getLocalizedString(locale, "tool.contenttool.toolbarV3.showContentInTreeTitle"),
 				  "syncWithTree();",
 				  "",
 				  "syncTree"));
 
 		if(contentVersionId != null)
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageLabel"), 
 					  getLocalizedString(locale, "tool.contenttool.toolbarV3.changeLanguageTitle"),
 					  "ChangeVersionLanguage!input.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&contentVersionId=" + contentVersionId,
 					  "",
 					  "changeLanguage"));
 	
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.contenttool.toolbarV3.showDataAsXMLLabel"), 
 					  getLocalizedString(locale, "tool.contenttool.toolbarV3.showDataAsXMLTitle"),
 					  "ViewContentVersion!asXML.action?contentId=" + contentId + "&repositoryId=" + contentVO.getRepositoryId() + "&contentVersionId=" + contentVersionId,
 					  "",
 					  "showDataAsXML"));
 		}
 		
 		return buttons;
 	}
 	
 
 	private List<ToolbarButton> getContentVersionFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.save.label"), 
 									  getLocalizedString(locale, "tool.contenttool.save.label"),
 									  "javascript:validateAndSubmitContentForm();",
 									  "images/v3/saveInlineIcon.gif",
 									  "save"));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
 									  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
 									  "javascript:validateAndSubmitContentFormThenClose();",
 									  "images/v3/saveAndExitInlineIcon.gif",
 									  "saveAndExit"));
 		
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
 				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 									  "cancel",
 				  					  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getContentVersionWizardFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.nextButton.label"), 
 									  getLocalizedString(locale, "tool.common.nextButton.label"),
 									  "javascript:validateAndSubmitContentForm();",
 									  "images/v3/saveInlineIcon.gif",
 									  "save"));
 		
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
 				  					  getLocalizedString(locale, "tool.contenttool.cancel.label"),
 				  					  "if(parent && parent.closeInlineDiv) parent.parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 									  "cancel",
 				  					  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSiteNodeButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String siteNodeId = request.getParameter("siteNodeId");
 		String repositoryId = request.getParameter("repositoryId");
 		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId));
 		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(new Integer(siteNodeId));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.structuretool.toolbarV3.createPageLabel"), 
 				  getLocalizedString(locale, "tool.structuretool.toolbarV3.createPageTitle"),
 				  "CreateSiteNode!inputV3.action?isBranch=true&repositoryId=" + repositoryId + "&parentSiteNodeId=" + siteNodeId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "create"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageLabel"), 
 				  getLocalizedString(locale, "tool.structuretool.toolbarV3.movePageTitle"),
 				  "MoveSiteNode!inputV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "",
 				  "movePage"));
 
 		//if(!hasPublishedVersion())
 		//{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageLabel"), 
 					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageTitle"),
 					  "DeleteSiteNode!V3.action?siteNodeId=" + siteNodeId + "&repositoryId=" + repositoryId + "&changeTypeId=4&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 					  "",
 					  "",
 					  "delete",
 					  true,
 					  true,
 					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageLabel"), 
 					  getLocalizedString(locale, "tool.structuretool.toolbarV3.deletePageConfirmationLabel", new String[]{siteNodeVO.getName()}),
 					  "inlineDiv"));
 			
 		    //buttons.add(new ImageButton(this.getCMSBaseUrl() + "/Confirm.action?header=tool.structuretool.deleteSiteNode.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=SiteNode&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.structuretool.deleteSiteNode.message", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.deleteSiteNode"), "Delete SiteNode"));
 		//}
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.structuretool.toolbarV3.editPageMetaInfoLabel"), 
 				getLocalizedString(locale, "tool.structuretool.toolbarV3.editPageMetaInfoTitle"),
 				"ViewAndCreateContentForServiceBinding.action?siteNodeId=" + siteNodeId + "&repositoryId=" + repositoryId + "&siteNodeVersionId=" + siteNodeVersionVO.getId() + "&hideLeafs=true&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				"",
 				"properties"));
 
 		buttons.add(StructureToolbarController.getPreviewButtons(new Integer(repositoryId), new Integer(siteNodeId), locale));
 
 		buttons.add(StructureToolbarController.getCoverButtons(new Integer(repositoryId), new Integer(siteNodeId), locale, principal));
 
 		ToolbarButton publishButton = StructureToolbarController.getPublishCurrentNodeButton(new Integer(repositoryId), new Integer(siteNodeId), locale);
 		
 		ToolbarButton publishStructureButton = StructureToolbarController.getPublishButtons(new Integer(repositoryId), new Integer(siteNodeId), locale);
 		
 		publishButton.getSubButtons().add(publishStructureButton);
 		
 		buttons.add(publishButton);
 
		if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
		{
			buttons.add(new ToolbarButton("",
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonLabel"), 
				  getLocalizedString(locale, "tool.common.accessRights.accessRightsButtonTitle"),
				  "ViewAccessRights!V3.action?interceptionPointCategory=SiteNodeVersion&extraParameters=" + siteNodeVersionVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
				  "images/v3/accessRightIcon.gif",
				  "accessRights"));
		}

 		//buttons.add(StructureToolbarController.getTasksButtons(new Integer(repositoryId), new Integer(siteNodeId), locale));
 
 		return buttons;
 
 		/*
 		buttons.add(new ImageButton(this.getCMSBaseUrl() + "/CreateSiteNode!input.action?isBranch=true&parentSiteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.newSiteNode"), "New SiteNode"));	
 		
 		ImageButton moveButton = getMoveButton();
 		moveButton.getSubButtons().add(getMoveMultipleButton());
 		buttons.add(moveButton);	
 
 		
 		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
 			buttons.add(new ImageButton(true, "javascript:openPopup('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
 		else if(this.siteNodeVersionVO != null)
 			buttons.add(new ImageButton(true, "javascript:openPopupWithOptionalParameter('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "', 'PageProperties', 'width=750,height=700,resizable=no,status=yes,scrollbars=yes', '" + getLocalizedString(getSession().getLocale(), "tool.structuretool.changeSiteNodeStateToWorkingQuestion") + "', 'changeStateToWorking=true');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
 
 		buttons.add(getPreviewButtons());
 		
 		if(hasPublishedVersion())
 		{
 		    ImageButton unpublishButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!input.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
 		    ImageButton unpublishAllButton = new ImageButton(this.getCMSBaseUrl() + "/UnpublishSiteNodeVersion!inputChooseSiteNodes.action?siteNodeId=" + this.siteNodeId + "&siteNodeVersionId=" + this.siteNodeVersionId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishAllVersion"), "tool.contenttool.unpublishAllVersion.header");
 		    unpublishButton.getSubButtons().add(unpublishAllButton);
 		
 		    buttons.add(unpublishButton);
 		}
 		
 		ImageButton coverButton = new ImageButton(this.getCMSBaseUrl() + "/ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeCover"), "SiteNode Cover");	
 		coverButton.getSubButtons().add(getSimplePageComponentsButton());
 		buttons.add(coverButton);	
 
 		if(!isReadOnly())
 		{
 		    ImageButton pageComponentsButton = getViewPageComponentsButton();
 		    pageComponentsButton.getSubButtons().add(getSimplePageComponentsButton());
 		    buttons.add(pageComponentsButton);	
 		}
 		
 		ImageButton publishButton = getPublishCurrentNodeButton();
 	    publishButton.getSubButtons().add(getPublishButton());
 	    buttons.add(publishButton);	
 	    		
 		buttons.add(getExecuteTaskButton());
 
 		if(this.siteNodeVersionVO != null && this.siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
 			buttons.add(getAccessRightsButton());	
 		*/
 	}
 	
 	private List<ToolbarButton> getCreateSiteNodeButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.save.label"), 
 				  getLocalizedString(locale, "tool.contenttool.save.label"),
 				  "javascript:validateAndSubmitContentForm();",
 				  "images/v3/saveInlineIcon.gif",
 				  "save"));
 	
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getCreateSiteNodeFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.saveButton.label"), 
 									  getLocalizedString(locale, "tool.common.saveButton.label"),
 									  "save();",
 									  "images/v3/createBackgroundPenPaper.gif",
 				  					  "left",
 									  "save",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  "cancel",
 				  					  true));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getMySettingsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.save.label"), 
 									  getLocalizedString(locale, "tool.contenttool.save.label"),
 									  "javascript:save();",
 									  "images/v3/saveInlineIcon.gif",
 									  "save"));
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
 									  getLocalizedString(locale, "tool.common.closeWindowButton.label"),
 									  "javascript:closeAndReload();",
 									  "images/v3/closeWindowIcon.gif",
 									  "close"));
 						
 		return buttons;
 	}
 
 	private List<ToolbarButton> getMessageCenterFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.nextButton.label"), 
 									  getLocalizedString(locale, "tool.common.nextButton.label"),
 									  "submitForm();",
 									  "images/v3/nextBackground.gif",
 				  					  "left",
 									  "next",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  "cancel",
 				  					  true));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSaveCancelFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.common.saveButton.label"), 
 									  getLocalizedString(locale, "tool.common.saveButton.label"),
 									  "submitForm();",
 									  "images/v3/createBackgroundPenPaper.gif",
 				  					  "left",
 				  					  "save",
 									  true));
 
 		buttons.add(new ToolbarButton("",
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  					  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  					  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/cancelIcon.gif",
 				  					  "left",
 				  					  "cancel",
 				  					  true));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getPublishPageFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		SiteNodeVO siteNodeVO = null;
 		
 		Integer primaryKeyAsInteger = null;
 		try
 		{
 			primaryKeyAsInteger = new Integer(request.getParameter("siteNodeId"));
 		}
 		catch (Exception e) 
 		{
 		}
 
 		if(primaryKeyAsInteger != null)
 			siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(primaryKeyAsInteger);
 			
 		if(siteNodeVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + siteNodeVO.getRepositoryId()))
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"), 
 					  getLocalizedString(locale, "tool.common.publishing.publishButtonLabel"),
 					  "submitToPublish('true');",
 					  "images/v3/publishPageIcon.gif",
 					  "left",
 					  "publish",
 					  true));
 		}
 		
 		if(siteNodeVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"), 
 					  getLocalizedString(locale, "tool.common.publishing.submitToPublishButtonLabel"),
 					  "submitToPublish('false');",
 					  "images/v3/publishPageIcon.gif",
 					  "left",
 					  "submitToPublish",
 					  true));
 		}
 		
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  "images/v3/cancelIcon.gif",
 				  "left",
 				  "cancel",
 				  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getUnPublishContentsFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		ContentVO contentVO = null;
 		
 		Integer primaryKeyAsInteger = null;
 		try
 		{
 			primaryKeyAsInteger = new Integer(request.getParameter("contentId"));
 		}
 		catch (Exception e) 
 		{
 		}
 
 		if(primaryKeyAsInteger != null)
 			contentVO = ContentController.getContentController().getContentVOWithId(primaryKeyAsInteger);
 			
 		if(contentVO != null && hasAccessTo(principal, "PublishingTool.Read", true) && hasAccessTo(principal, "Repository.Read", "" + contentVO.getRepositoryId()))
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"), 
 					  getLocalizedString(locale, "tool.common.unpublishing.unpublishButtonLabel"),
 					  "submitToPublish('true');",
 					  "images/v3/publishPageIcon.gif",
 					  "left",
 					  "publish",
 					  true));
 		}
 		
 		if(contentVO != null && hasAccessTo(principal, "Common.SubmitToPublishButton", true))
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"), 
 					  getLocalizedString(locale, "tool.common.unpublishing.submitToUnpublishButtonLabel"),
 					  "submitToPublish('false');",
 					  "images/v3/publishPageIcon.gif",
 					  "left",
 					  "submitToPublish",
 					  true));
 		}
 		
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  "images/v3/cancelIcon.gif",
 				  "left",
 				  "cancel",
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
 	
 	private List<ToolbarButton> getGlobalSubscriptionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.createSubscription.header"), 
 				  getLocalizedString(locale, "tool.common.createSubscription.header"),
 				  "showDiv('newSubscriptionForm')",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "left",
 				  "create",
 				  true));
 		
 		return buttons;
 	}
 
 	
 	private List<ToolbarButton> getRepositoriesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createRepository.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createRepository.header"),
 				  "CreateRepository!inputV3.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteRepositories.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteRepositories.header"),
 				"submitListForm('repository');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"Delete repository?",
 				"Really want to delete rep...",
 				"managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.importRepository.header"), 
 				  getLocalizedString(locale, "tool.managementtool.importRepository.header"),
 				  "javascript:openPopup('ImportRepository!input.action', 'Import', 'width=600,height=500,resizable=no');",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "left",
 				  "import",
 				  true));
 
 
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getRepositoryDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"), 
 				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"),
 				  "DeleteRepository!markForDelete.action?repositoryId=" + request.getParameter("repositoryId"),
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "left",
 				  "create",
 				  false,
 				  true,
 				  getLocalizedString(locale, "tool.managementtool.deleteRepository.header"),
 				  getLocalizedString(locale, "tool.managementtool.deleteRepository.text"),
 				  "inlineDiv"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights.action?interceptionPointCategory=Repository&extraParameters=" + request.getParameter("repositoryId") + "&returnAddress=ViewRepository.action?repositoryId=" + request.getParameter("repositoryId"),
 				  "images/v3/accessRightIcon.gif",
 				  "accessRights",
 				  "managementWorkIframe"));
 		/*
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteRepository.header&yesDestination=" + URLEncoder.encode("DeleteRepository.action?repositoryId=" + this.repositoryId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListRepository.action?title=Repositories", "UTF-8") + "&message=tool.managementtool.deleteRepository.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteRepository"), "tool.managementtool.deleteRepository.header"));
 		buttons.add(new ImageButton(true, "javascript:openPopup('ExportRepository!input.action?repositoryId=" + this.repositoryId + "', 'Export', 'width=600,height=500,resizable=no,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.exportRepository"), getLocalizedString(getSession().getLocale(), "tool.managementtool.exportRepository.header")));	
 		buttons.add(new ImageButton(true, "javascript:openPopup('ImportRepository!inputCopy.action?repositoryId=" + this.repositoryId + "', 'Export', 'width=600,height=500,resizable=no,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.copyRepository"), getLocalizedString(getSession().getLocale(), "tool.managementtool.importRepositoryCopy.header")));	
 		buttons.add(new ImageButton("ViewRepositoryProperties.action?repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewRepository.action?repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8");
 		buttons.add(new ImageButton("ViewAccessRights.action?interceptionPointCategory=Repository&extraParameters=" + this.repositoryId +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header"));
 		buttons.add(new ImageButton("ViewListRepositoryLanguage.action?repositoryId=" + this.repositoryId +"&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.repositoryLanguages"), "tool.managementtool.repositoryLanguages.header"));
 		
 		buttons.add(new ImageButton(true, "javascript:openPopup('RebuildRegistry!input.action?repositoryId=" + this.repositoryId + "', 'Registry', 'width=400,height=200,resizable=no');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.rebuildRegistry"), getLocalizedString(getSession().getLocale(), "tool.managementtool.rebuildRegistry.header")));	
 		*/
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getSystemUsersButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
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
 											  "images/v3/createBackgroundPenPaper.gif",
 											  "create"));
 			}
 		}
 		
 		/*		
 		buttons.add(new ToolbarButton(true, "javascript:toggleSearchForm();", getLocalizedString(locale, "images.managementtool.buttons.searchButton"), "Search Form"));
 		*/
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getSystemUserDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("userName");
 		}
 		catch (Exception e) 
 		{
 		}
 
 		String yesDestination 	= URLEncoder.encode("DeleteSystemUser!v3.action?userName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListSystemUser!v3.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the user " + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 
 		if(!primaryKey.equals(CmsPropertyHandler.getAnonymousUser()))
 		{
 			InfoGluePrincipal user = UserControllerProxy.getController().getUser(primaryKey);
 			if(user.getAutorizationModule().getSupportDelete())
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteSystemUser.header"),
 						  "Confirm.action?header=tool.managementtool.deleteSystemUser.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteSystemUser.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/createBackgroundPenPaper.gif",
 						  "delete"));
 
 				//buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteSystemUser.header&yesDestination=" + URLEncoder.encode("DeleteSystemUser.action?userName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding) + "&noDestination=" + URLEncoder.encode("ViewListSystemUser.action?title=SystemUsers", URIEncoding) + "&message=tool.managementtool.deleteSystemUser.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.deleteSystemUser"), "tool.managementtool.deleteSystemUser.header"));
 			}
 		
 			if(user.getAutorizationModule().getSupportUpdate())
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"), 
 						  getLocalizedString(locale, "tool.managementtool.viewSystemUserPasswordDialog.header"),
 						  "UpdateSystemUserPassword!inputV3.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding),
 						  "images/v3/passwordIcon.gif",
 						  "password"));
 
 				//buttons.add(new ToolbarButton("UpdateSystemUserPassword!input.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.updateSystemUserPassword"), "Update user password"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = UserPropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewUserProperties.header"),
 					  "ViewUserProperties!v3.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding),
 					  "images/v3/advancedSettingsIcon.gif",
 					  "advancedSettings"));
 
 			//buttons.add(new ToolbarButton("ViewUserProperties.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding), URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.viewSystemUserProperties"), "View User Properties"));
 		}
 		
 		if(principal.getIsAdministrator())
 		{
 			buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"), 
 					  getLocalizedString(locale, "tool.managementtool.transferAccessRights.header"),
 					  "AuthorizationSwitchManagement!inputUser.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)),
 					  "images/v3/createBackgroundPenPaper.gif",
 					  "create"));
 
 			//buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputUser.action?userName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferUserAccessRights"), "Transfer Users Access Rights"));
 		}
 
 		return buttons;				
 	}
 	
 	private List<ToolbarButton> getRolesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
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
 											  "images/v3/createBackgroundPenPaper.gif",
 											  "create"));
 			}
 		}
 		
 		return buttons;
 	}
 	
 	private List<ToolbarButton> getRoleDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("roleName");
 		}
 		catch (Exception e) 
 		{
 		}
 
 		String yesDestination 	= URLEncoder.encode("DeleteRole!v3.action?roleName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListRole!listManagableRoles.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the role " + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueRole role = RoleControllerProxy.getController().getRole(primaryKey);
 		if(role.getAutorizationModule().getSupportDelete())
 		{
 			boolean hasAccessToDeleteRole = hasAccessTo(principal, "Role.Delete", "" + primaryKey);
 			if(hasAccessToDeleteRole)
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteRole.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteRole.header"),
 						  "Confirm.action?header=tool.managementtool.deleteRole.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteRole.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/deleteBackgroundWasteBasket.gif",
 						  "delete"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = RolePropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			boolean hasAccessToEditProperties = hasAccessTo(principal, "Role.EditProperties", true);
 			if(hasAccessToEditProperties)
 			{
 				buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewRoleProperties.header"),
 					  "ViewRoleProperties!v3.action?roleName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)),
 					  "images/v3/advancedSettingsIcon.gif",
 					  "advancedSettings"));
 			}
 		}
 
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Role.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Role.ManageAccessRights", "" + primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 		{
 			buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights.action?interceptionPointCategory=Role&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewRole!v3.action?roleName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
 				  "images/v3/accessRightsIcon.gif",
 				  "accessRights"));
 		}
 		/*
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputRole.action?roleName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferRoleAccessRights"), "Transfer Roles Access Rights"));
 		*/
 
 		return buttons;				
 	}
 
 	
 	private List<ToolbarButton> getGroupsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
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
 											  "images/v3/createBackgroundPenPaper.gif",
 											  "create"));
 			}
 		}
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getGroupDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("groupName");
 		}
 		catch (Exception e) 
 		{
 		}
 
 		String yesDestination 	= URLEncoder.encode("DeleteGroup!v3.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListGroup!listManagableGroups.action", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the group " + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(primaryKey);
 		if(group.getAutorizationModule().getSupportDelete())
 		{
 			boolean hasAccessToDeleteGroup = hasAccessTo(principal, "Group.Delete", "" + primaryKey);
 			if(hasAccessToDeleteGroup)
 			{
 				buttons.add(new ToolbarButton("",
 						  getLocalizedString(locale, "tool.managementtool.deleteGroup.header"), 
 						  getLocalizedString(locale, "tool.managementtool.deleteGroup.header"),
 						  "Confirm.action?header=tool.managementtool.deleteGroup.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteGroup.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding),
 						  "images/v3/deleteBackgroundWasteBasket.gif",
 						  "delete"));
 			}
 		}
 		
 		List contentTypeDefinitionVOList = GroupPropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 		{
 			boolean hasAccessToEditProperties = hasAccessTo(principal, "Group.EditProperties", true);
 			if(hasAccessToEditProperties)
 			{
 				buttons.add(new ToolbarButton("",
 					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"), 
 					  getLocalizedString(locale, "tool.managementtool.viewGroupProperties.header"),
 					  "ViewGroupProperties!v3.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)),
 					  "images/v3/advancedSettingsIcon.gif",
 					  "advancedSettings"));
 			}
 		}
 
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 		{
 			buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewGroup!v3.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool",
 				  "images/v3/accessRightsIcon.gif",
 				  "accessRights"));
 		}
 		/*
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
 		*/
 
 		return buttons;				
 	}
 
 	private List<ToolbarButton> getGroupPropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("groupName");
 		}
 		catch (Exception e) 
 		{
 		}
 
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.GroupProperties&entityId=" + primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif",
 									  "left",
 									  "attachAsset",
 									  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getRolePropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("roleName");
 		}
 		catch (Exception e) 
 		{
 		}
 		
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.RoleProperties&entityId=" + primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif",
 									  "left",
 									  "attachAsset",
 									  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getUserPropertiesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		String primaryKey = null;
 		try
 		{
 			primaryKey = request.getParameter("userName");
 		}
 		catch (Exception e) 
 		{
 		}
 		
 		buttons.add(new ToolbarButton("",
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"), 
 									  getLocalizedString(locale, "tool.contenttool.uploadDigitalAsset.label"),
 									  "openWindow('ViewDigitalAsset.action?entity=org.infoglue.cms.entities.management.UserProperties&entityId=" + primaryKey + "', 'DigitalAsset', 'width=400,height=200,resizable=no');",
 									  "images/v3/attachAssetBackgroundIcon.gif",
 									  "left",
 									  "attachAsset",
 									  true));
 		
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
 		
 		String yesDestination 	= URLEncoder.encode("DeleteGroup.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListGroup.action?title=Groups", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the group " + URLEncoder.encode(primaryKey, URIEncoding), URIEncoding);
 		
 		InfoGlueGroup group = GroupControllerProxy.getController().getGroup(primaryKey);
 		if(group.getAutorizationModule().getSupportDelete())
 			buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteGroup.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteGroup.text&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding), getLocalizedString(locale, "images.managementtool.buttons.deleteGroup"), "tool.managementtool.deleteGroup.header"));
 		
 		List<ToolbarButton> contentTypeDefinitionVOList<ToolbarButton> = GroupPropertiesController.getController().getContentTypeDefinitionVOList(primaryKey);
 		if(contentTypeDefinitionVOList.size() > 0)
 			buttons.add(new ToolbarButton("ViewGroupProperties.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.viewGroupProperties"), "View Group Properties"));
 		
 		if(principal.getIsAdministrator())
 			buttons.add(new ToolbarButton("AuthorizationSwitchManagement!inputGroup.action?groupName=" + URLEncoder.encode(URLEncoder.encode(primaryKey, URIEncoding)), getLocalizedString(locale, "images.managementtool.buttons.transferGroupAccessRights"), "Transfer Groups Access Rights"));
 				
 		boolean hasAccessToManageAllAccessRights = hasAccessTo(principal, "Group.ManageAllAccessRights", true);
 		boolean hasAccessToManageAccessRights = hasAccessTo(principal, "Group.ManageAccessRights", "" + primaryKey);
 		if(hasAccessToManageAllAccessRights || hasAccessToManageAccessRights)
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Group&extraParameters=" + URLEncoder.encode(primaryKey, URIEncoding) + "&returnAddress=ViewGroup.action?groupName=" + URLEncoder.encode(primaryKey, URIEncoding) + "&colorScheme=ManagementTool", getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "Group Access Rights"));
 
 		return buttons;				
 	}
 	*/
 	
 	private List<ToolbarButton> getLanguagesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createLanguage.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createLanguage.header"),
 				  "CreateLanguage!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteLanguages.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteLanguages.header"),
 				"submitListForm('language');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"Delete repository?",
 				"Really want to delete rep...",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 /*	
 	private List<ToolbarButton> getLanguagesButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateLanguage!input.action", getLocalizedString(locale, "images.managementtool.buttons.newLanguage"), "New Language"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('language');", getLocalizedString(locale, "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguages.header"));
 		return buttons;
 	}
 */
 	private List<ToolbarButton> getLanguageDetailsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"), 
 				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"),
 				  "DeleteLanguage.action?languageId=" + request.getParameter("languageId"),
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "left",
 				  "create",
 				  false,
 				  true,
 				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.header"),
 				  getLocalizedString(locale, "tool.managementtool.deleteLanguage.text"),
 				  "inlineDiv"));
 
 		return buttons;
 	}
 /*
 	private List<ToolbarButton> getLanguageDetailsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		String name = LanguageController.getController().getLanguageVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteLanguage.header&yesDestination=" + URLEncoder.encode("DeleteLanguage.action?languageId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListLanguage.action?title=Languages", "UTF-8") + "&message=tool.managementtool.deleteLanguage.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguage.header"));
 		return buttons;				
 	}
 	*/
 
 	private List<ToolbarButton> getInterceptionPointsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createInterceptionPoint.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createInterceptionPoint.header"),
 				  "CreateInterceptionPoint!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
 				"submitListForm('interceptionPoint');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 	private List<ToolbarButton> getInterceptionPointsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateInterceptionPoint!input.action", getLocalizedString(locale, "images.managementtool.buttons.newInterceptionPoint"), "New InterceptionPoint"));	
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('interceptionPoint');", getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoints.header"));
 		return buttons;
 	}
 	*/
 	private List<ToolbarButton> getInterceptionPointButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		
 		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(new Integer(request.getParameter("interceptionPointId")));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoints.header"),
 				"DeleteInterceptionPoint.action?interceptionPointId=" + interceptionPointVO.getId(),
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				false, 
 				false, 
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoint.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptionPoint.text"),
 				"managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"), 
 				  getLocalizedString(locale, "tool.contenttool.accessRights.header"),
 				  "ViewAccessRights!V3.action?interceptionPointCategory=" + interceptionPointVO.getCategory() + "&interceptionPointId=" + interceptionPointVO.getId() + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent",
 				  "images/v3/accessRightsIcon.gif",
 				  "accessRights"));
 
 		return buttons;
 	}
 	
 	/*
 	private List<ToolbarButton> getInterceptionPointButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(primaryKeyAsInteger);
 		String name = interceptionPointVO.getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteInterceptionPoint.header&yesDestination=" + URLEncoder.encode("DeleteInterceptionPoint.action?interceptionPointId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListInterceptionPoint.action?title=InterceptionPoints", "UTF-8") + "&message=tool.managementtool.deleteInterceptionPoint.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoint.header"));
 		if(interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue() == false)
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=" + interceptionPointVO.getCategory() + "&interceptionPointId=" + primaryKeyAsInteger + "&returnAddress=ViewInterceptionPoint.action?interceptionPointId=" + primaryKeyAsInteger + "&colorScheme=ManagementTool", getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "InterceptionPoint Access Rights"));
 		
 		return buttons;				
 	}
 	*/
 
 	private List<ToolbarButton> getInterceptorsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createInterceptor.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createInterceptor.header"),
 				  "CreateInterceptor!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptors.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteInterceptors.header"),
 				"submitListForm('interceptor');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
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
 	*/
 
 	private List<ToolbarButton> getServiceDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createServiceDefinition.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createServiceDefinition.header"),
 				  "CreateServiceDefinition!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinitions.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteServiceDefinitions.header"),
 				"submitListForm('serviceDefinition');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
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
 	*/
 
 	private List<ToolbarButton> getAvailableServiceBindingsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createAvailableServiceBinding.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createAvailableServiceBinding.header"),
 				  "CreateAvailableServiceBinding!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBindings.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteAvailableServiceBindings.header"),
 				"submitListForm('availableServiceBinding');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 
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
 		String name = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteAvailableServiceBinding.header&yesDestination=" + URLEncoder.encode("DeleteAvailableServiceBinding.action?availableServiceBindingId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListAvailableServiceBinding.action?title=AvailableServiceBindings", "UTF-8") + "&message=tool.managementtool.deleteAvailableServiceBinding.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteAvailableServiceBinding"), "tool.managementtool.deleteAvailableServiceBinding.header"));
 		return buttons;				
 	}
 	*/
 
 	private List<ToolbarButton> getSiteNodeTypeDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createSiteNodeTypeDefinition.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createSiteNodeTypeDefinition.header"),
 				  "CreateSiteNodeTypeDefinition!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinitions.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteSiteNodeTypeDefinitions.header"),
 				"submitListForm('siteNodeTypeDefinition');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 
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
 		String name = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteSiteNodeTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteSiteNodeTypeDefinition.action?siteNodeTypeDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListSiteNodeTypeDefinition.action?title=SiteNodeTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteSiteNodeTypeDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteSiteNodeTypeDefinition"), "tool.managementtool.deleteSiteNodeTypeDefinition.header"));
 		return buttons;				
 	}
 
 	*/
 
 	private List<ToolbarButton> getContentTypeDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createContentTypeDefinition.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createContentTypeDefinition.header"),
 				  "CreateContentTypeDefinition!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinitions.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteContentTypeDefinitions.header"),
 				"submitListForm('contentTypeDefinition');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 
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
 		String name = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteContentTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteContentTypeDefinition.action?contentTypeDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListContentTypeDefinition.action?title=ContentTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteContentTypeDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteContentTypeDefinition"), "tool.managementtool.deleteContentTypeDefinition.header"));
 		
 		String protectContentTypes = CmsPropertyHandler.getProtectContentTypes();
 		if(protectContentTypes != null && protectContentTypes.equalsIgnoreCase("true"))
 		{
 			String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContentTypeDefinition.action?contentTypeDefinitionId=" + primaryKey, "UTF-8"), "UTF-8");
 			buttons.add(getAccessRightsButton("ContentTypeDefinition", primaryKey, returnAddress));
 		}
 		
 		return buttons;				
 	}
 
 	*/
 
 	private List<ToolbarButton> getCategoryButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 	    String url = "CategoryManagement!new.action";
 		//if(primaryKeyAsInteger != null)
 		//	url += "?model/parentId=" + primaryKeyAsInteger;
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createCategory.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createCategory.header"),
 				  url,
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteCategories.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteCategories.header"),
 				"submitListForm('category');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 
 	private List<ToolbarButton> getCategoryButtons() throws Exception
 	{
 	    String url = "CategoryManagement!new.action";
 		if(primaryKeyAsInteger != null)
 			url += "?model/parentId=" + primaryKeyAsInteger;
 
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton(url, getLocalizedString(locale, "images.managementtool.buttons.newCategory"), "New Category"));
 
 		if(primaryKeyAsInteger != null)
 			buttons.add(new ToolbarButton(true, "javascript:openPopup('CategoryManagement!displayTreeForMove.action?categoryId=" + primaryKey + "', 'Category', 'width=400,height=600,resizable=no,status=yes');", getLocalizedString(locale, "images.managementtool.buttons.moveCategory"), "Move Category"));
 
 		buttons.add(new ToolbarButton(true, "javascript:submitListForm('category');", getLocalizedString(locale, "images.managementtool.buttons.deleteCategory"), "Delete Category"));
 		
 		if(primaryKeyAsInteger != null)
 		{	
 		    String returnAddress = URLEncoder.encode(URLEncoder.encode("CategoryManagement!edit.action?categoryId=" + primaryKey + "&title=Category%20Details", "UTF-8"), "UTF-8");
 		    buttons.add(getAccessRightsButton("Category", primaryKey, returnAddress));
 		}
 		
 		return buttons;
 	}
 	
 	private ToolbarButton getAccessRightsButton(String interceptionPointCategory, String extraParameter, String returnAddress) throws Exception
 	{
 		return new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=" + interceptionPointCategory + "&extraParameters=" + extraParameter +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header");
 	}
 	*/
 
 	private List<ToolbarButton> getRedirectsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createRedirect.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createRedirect.header"),
 				  "CreateRedirect!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteRedirects.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteRedirects.header"),
 				"submitListForm('redirect');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
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
 		String name = RedirectController.getController().getRedirectVOWithId(primaryKeyAsInteger).getUrl();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteRedirect.header&yesDestination=" + URLEncoder.encode("DeleteRedirect.action?redirectId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListWorkflowDefinition.action", "UTF-8") + "&message=tool.managementtool.deleteWorkflowDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinition.header"));
 		return buttons;				
 	}
 
 	*/
 
 	private List<ToolbarButton> getPortletsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createPortlet.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createPortlet.header"),
 				  "UploadPortlet!inputV3.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create"));
 
 		return buttons;
 	}
 
 	/*
 	private List<ToolbarButton> getPortletsButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("UploadPortlet.action", getLocalizedString(locale, "images.managementtool.buttons.newPortlet"), "New Portlet"));	
 		//buttons.add(new ToolbarButton(true, "javascript:submitListForm('workflowDefinition');", getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinitions.header"));
 		return buttons;
 	}
 
 	*/
 
 	private List<ToolbarButton> getWorkflowDefinitionsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createWorkflowDefinition.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createWorkflowDefinition.header"),
 				  "CreateWorkflowDefinition!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinitions.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteWorkflowDefinitions.header"),
 				"submitListForm('workflowDefinition');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 	
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
 		String name = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(primaryKeyAsInteger).getName();
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteWorkflowDefinition.header&yesDestination=" + URLEncoder.encode("DeleteWorkflowDefinition.action?workflowDefinitionId=" + primaryKeyAsInteger, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListWorkflowDefinition.action", "UTF-8") + "&message=tool.managementtool.deleteWorkflowDefinition.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteWorkflowDefinition"), "tool.managementtool.deleteWorkflowDefinition.header"));
 	    final String protectWorkflows = CmsPropertyHandler.getProtectWorkflows();
 	    if(protectWorkflows != null && protectWorkflows.equalsIgnoreCase("true"))
 	    {
 			String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewWorkflowDefinition.action?workflowDefinitionId=" + primaryKey, "UTF-8"), "UTF-8");
 			final WorkflowDefinitionVO workflowDefinition = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithId(primaryKeyAsInteger);
 			buttons.add(new ToolbarButton("ViewAccessRights.action?interceptionPointCategory=Workflow&extraParameters=" + workflowDefinition.getName() +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(locale, "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header"));
 	    }
 		return buttons;				
 	}
 
 	*/
 
 	private List<ToolbarButton> getServerNodesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createServerNode.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createServerNode.header"),
 				  "CreateServerNode!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				getLocalizedString(locale, "tool.managementtool.deleteServerNodes.header"),
 				getLocalizedString(locale, "tool.managementtool.deleteServerNodes.header"),
 				"submitListForm('serverNode');",
 				"images/v3/deleteBackgroundWasteBasket.gif",
 				"left",
 				"delete",
 				true, 
 				false, 
 				"",
 				"",
 				"managementWorkIframe"));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.editServerNodeProperties.header"), 
 				  getLocalizedString(locale, "tool.managementtool.editServerNodeProperties.header"),
 				  "ViewServerNodeProperties.action?serverNodeId=-1",
 				  "images/v3/deleteBackgroundWasteBasket.gif",
 				  "delete",
 				  "managementWorkIframe"));
 
 		return buttons;
 	}
 
 	/*
 
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
 		buttons.add(new ToolbarButton("Confirm.action?header=tool.managementtool.deleteServerNode.header&yesDestination=" + URLEncoder.encode("DeleteServerNode.action?serverNodeId=" + primaryKey, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListServerNode.action?title=ServerNodes", "UTF-8") + "&message=tool.managementtool.deleteServerNode.text&extraParameters=" + this.extraParameters, getLocalizedString(locale, "images.managementtool.buttons.deleteServerNode"), "tool.managementtool.deleteServerNode.header"));
 		buttons.add(new ToolbarButton("ViewServerNodeProperties.action?serverNodeId=" + primaryKey, getLocalizedString(locale, "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		return buttons;				
 	}
 	*/
 
 	private List<ToolbarButton> getMessageCenterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.createEmail.header"), 
 				  getLocalizedString(locale, "tool.managementtool.createEmail.header"),
 				  "CreateEmail!inputChooseRecipients.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create"));
 
 		return buttons;
 	}
 
 	/*
 
 	private List<ToolbarButton> getMessageCenterButtons() throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("CreateEmail!inputChooseRecipients.action", getLocalizedString(locale, "images.managementtool.buttons.newEmail"), "tool.managementtool.createEmail.header"));
 		
 		return buttons;
 	}
 	*/
 
 	private List<ToolbarButton> getThemesButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.uploadTheme.header"), 
 				  getLocalizedString(locale, "tool.managementtool.uploadTheme.header"),
 				  "ViewThemes!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getLabelsButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.managementtool.uploadTranslation.header"), 
 				  getLocalizedString(locale, "tool.managementtool.uploadTranslation.header"),
 				  "ViewLabels!input.action",
 				  "images/v3/createBackgroundPenPaper.gif",
 				  "create",
 				  "managementWorkIframe"));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getQuickDeployFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.nextButton.label"), 
 				  getLocalizedString(locale, "tool.common.nextButton.label"),
 				  "submitForm();",
 				  "images/v3/nextBackground.gif",
 				  "left",
 				  "next",
 				  true));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  "images/v3/cancelIcon.gif",
 				  "left",
 				  "cancel",
 				  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getVCDeployFooterButtons(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.nextButton.label"), 
 				  getLocalizedString(locale, "tool.common.nextButton.label"),
 				  "submitForm();",
 				  "images/v3/nextBackground.gif",
 				  "left",
 				  "next",
 				  true));
 
 		buttons.add(new ToolbarButton("",
 				  getLocalizedString(locale, "tool.common.cancelButton.label"), 
 				  getLocalizedString(locale, "tool.common.cancelButton.label"),
 				  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  "images/v3/cancelIcon.gif",
 				  "left",
 				  "cancel",
 				  true));
 		
 		return buttons;
 	}
 
 	private List<ToolbarButton> getMySettingsButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 		buttons.add(new ToolbarButton("mySettingsButton",
 									  getLocalizedString(locale, "tool.managementtool.mysettings.header"), 
 									  getLocalizedString(locale, "tool.managementtool.mysettings.header"),
 									  "javascript:openMySettings();",
 									  "images/v3/mySettingsIcon.gif",
 									  "left",
 									  "mySettings",
 									  true));
 		
 		return buttons;
 	}
 
 	
 	private List<ToolbarButton> getHelpButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
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
 									  "images/v3/helpIcon.gif",
 									  "help"));
 
 		return buttons;
 	}
 
 	private List<ToolbarButton> getWindowCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton) throws Exception
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(new ToolbarButton("exitButton",
 									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
 									  getLocalizedString(locale, "tool.common.closeWindowButton.title"),
 									  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/closeWindowIcon.gif",
 				  					  "right",
 									  "close",
 				  					  true));
 		return buttons;
 	}
 
 	private ToolbarButton getDialogCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		return new ToolbarButton("exitButton",
 									  getLocalizedString(locale, "tool.common.closeWindowButton.label"), 
 									  getLocalizedString(locale, "tool.common.closeWindowButton.title"),
 									  "if(parent && parent.closeInlineDiv) parent.closeInlineDiv(); else if(parent && parent.closeDialog) parent.closeDialog(); else window.close();",
 				  					  "images/v3/closeWindowIcon.gif",
 				  					  "right",
 									  "close",
 				  					  true);
 	}
 
 	private List<ToolbarButton> getCommonFooterSaveOrCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
 				
 		return buttons;		
 	}
 
 	private List<ToolbarButton> getCommonFooterSaveOrSaveAndExitOrCloseButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String exitUrl)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(getCommonFooterSaveButton(toolbarKey, principal, locale, request, disableCloseButton));
 		buttons.add(getCommonFooterSaveAndExitButton(toolbarKey, principal, locale, request, disableCloseButton, exitUrl));
 		buttons.add(getDialogCloseButton(toolbarKey, principal, locale, request, disableCloseButton));
 				
 		return buttons;		
 	}
 
 	private List<ToolbarButton> asButtons(ToolbarButton button)
 	{
 		List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();
 
 		buttons.add(button);
 				
 		return buttons;		
 	}
 
 	private ToolbarButton getCommonFooterSaveButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton)
 	{
 		return new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.save.label"), 
 				  getLocalizedString(locale, "tool.contenttool.save.label"),
 				  "save(document.inputForm);",
 				  "images/v3/saveInlineIcon.gif",
 				  "left",
 				  "save",
 				  true);
 	}
 
 	private ToolbarButton getCommonFooterSaveAndExitButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String exitUrl)
 	{
 		return new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"), 
 				  getLocalizedString(locale, "tool.contenttool.saveAndExit.label"),
 				  "saveAndExit(\"" + exitUrl + "\");",
 				  "images/v3/saveAndExitInlineIcon.gif",
 				  "left",
 				  "saveAndExit",
 				  true);
 	}
 	
 	private ToolbarButton getCommonFooterCancelButton(String toolbarKey, InfoGluePrincipal principal, Locale locale, HttpServletRequest request, boolean disableCloseButton, String cancelUrl)
 	{
 		return new ToolbarButton("",
 				  getLocalizedString(locale, "tool.contenttool.cancel.label"), 
 				  getLocalizedString(locale, "tool.contenttool.cancel.label"),
 				  "" + cancelUrl + "",
 				  "images/v3/cancelIcon.gif",
 				  "left",
 				  "cancel",
 				  false);
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
 		return LabelController.getController(locale).getLocalizedString(locale, key);
     	/*
 		StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
 
     	return stringManager.getString(key);
     	*/
   	}
 
 	public String getLocalizedString(Locale locale, String key, Object[] args) 
   	{
 		return LabelController.getController(locale).getLocalizedString(locale, key, args);
    	}
 }
