 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 package org.generationcp.ibpworkbench.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.ibpworkbench.IBPWorkbenchApplication;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.comp.WorkflowConstants;
 import org.generationcp.ibpworkbench.comp.ibtools.breedingview.select.SelectDatasetForBreedingViewWindow;
 import org.generationcp.ibpworkbench.comp.window.IContentWindow;
 import org.generationcp.ibpworkbench.navigation.NavManager;
 import org.generationcp.ibpworkbench.util.ToolUtil;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.ManagerFactoryProvider;
 import org.generationcp.middleware.manager.api.UserDataManager;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.User;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.Tool;
 import org.generationcp.middleware.pojos.workbench.ToolType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component.Event;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Window;
 
 @Configurable
 public class LaunchWorkbenchToolAction implements WorkflowConstants, ClickListener, ActionListener {
     private static final long serialVersionUID = 1L;
     
     private final static Logger LOG = LoggerFactory.getLogger(LaunchWorkbenchToolAction.class);
     
     Project project;
     
     public static enum ToolEnum {
          GERMPLASM_BROWSER("germplasm_browser")
         ,STUDY_BROWSER("study_browser")
         ,GERMPLASM_LIST_BROWSER("germplasm_list_browser")
         ,GDMS("gdms")
         ,FIELDBOOK("fieldbook")
         ,OPTIMAS("optimas")
         ,BREEDING_MANAGER("breeding_manager")
         ,BREEDING_VIEW("breeding_view")
         ;
         
         String toolName;
         
         ToolEnum(String toolName) {
             this.toolName = toolName;
         }
         
         public String getToolName() {
             return toolName;
         }
         
         public static boolean isCorrectTool(String toolName) {
             if(ToolEnum.GERMPLASM_BROWSER.getToolName().equals(toolName) ||
                ToolEnum.STUDY_BROWSER.getToolName().equals(toolName) ||
                ToolEnum.GERMPLASM_LIST_BROWSER.getToolName().equals(toolName) ||
                ToolEnum.GDMS.getToolName().equals(toolName) ||
                ToolEnum.FIELDBOOK.getToolName().equals(toolName) ||
                ToolEnum.OPTIMAS.getToolName().equals(toolName) ||
                ToolEnum.BREEDING_MANAGER.getToolName().equals(toolName) ||
                ToolEnum.BREEDING_VIEW.getToolName().equals(toolName)) {
                 return true;
             }   return false;
             
         }
     }
 
     private ToolEnum toolEnum;
     
     private String toolConfiguration;
 
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
     
     @Autowired
     private ManagerFactoryProvider managerFactoryProvider;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
     
     @Autowired
     private ToolUtil toolUtil;
     
     public LaunchWorkbenchToolAction() {
     }
     
     public LaunchWorkbenchToolAction(ToolEnum toolEnum) {
         this.toolEnum = toolEnum;
         this.toolConfiguration = WorkflowConstants.DEFAULT;
     }
     
     public LaunchWorkbenchToolAction(ToolEnum toolEnum, Project project, String toolConfiguration) {
         this.toolEnum = toolEnum;
         this.project = project;
         this.toolConfiguration = toolConfiguration;
     }
     
     @Override
     public void buttonClick(ClickEvent event) {
         
         Window window = event.getComponent().getWindow();
         
         launchTool(toolEnum.getToolName(), window, true);
         
     }
 
     @Override
     public void doAction(Event event) {
         NavManager.breadCrumbClick(this, event);
     }
 
     @Override
     public void doAction(Window window, String uriFragment, boolean isLinkAccessed) {
 
         String toolName = uriFragment.split("/")[1];
         
         if(ToolEnum.isCorrectTool(toolName)) {
                 
             launchTool(toolName, window, isLinkAccessed);
             
         } else {
             LOG.debug("Cannot launch tool due to invalid tool: {}", toolName);
             MessageNotifier.showError(window, messageSource.getMessage(Message.LAUNCH_TOOL_ERROR), 
                     messageSource.getMessage(Message.INVALID_TOOL_ERROR_DESC, Arrays.asList(toolName).toArray()));
         }
     }
     
     private void launchTool(String toolName, Window window, boolean isLinkAccessed) {
         Tool tool = null;
         
         try {
             tool = workbenchDataManager.getToolWithName(toolName);
         } catch (MiddlewareQueryException qe) {
             LOG.error("QueryException", qe);
             MessageNotifier.showError(window, messageSource.getMessage(Message.DATABASE_ERROR),
                     "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
             return;
         }
         
         if (tool == null) {
             LOG.warn("Cannot find tool " + toolEnum);
             showLaunchError(window, toolEnum.toString());
             return;
         } else {
             if (tool.getToolType() == ToolType.NATIVE) {
                 
                 if (toolName.equals(ToolEnum.BREEDING_VIEW.getToolName()) 
                         && toolConfiguration.equals(WorkflowConstants.BREEDING_VIEW_SINGLE_SITE_ANALYSIS_CENTRAL)
                         ) {
                         
                     window.addWindow(new SelectDatasetForBreedingViewWindow(project, Database.CENTRAL));
                         
                 } else if (toolName.equals(ToolEnum.BREEDING_VIEW.getToolName()) 
                         && toolConfiguration.equals(WorkflowConstants.BREEDING_VIEW_SINGLE_SITE_ANALYSIS_LOCAL))
                  {
                 
                     window.addWindow(new SelectDatasetForBreedingViewWindow(project, Database.LOCAL));
                 
                 }else {
                 
                     try {
                         toolUtil.launchNativeTool(tool);
                     }
                     catch (IOException e) {
                         File absoluteToolFile = new File(tool.getPath()).getAbsoluteFile();
                         
                         LOG.error("Cannot launch " + absoluteToolFile.getAbsolutePath(), e);
                         showLaunchError(window, absoluteToolFile.getAbsolutePath());
                     }
                 
                 }
                 
             }
             else if (tool.getToolType() == ToolType.WEB_WITH_LOGIN) {
                 String loginUrl = tool.getPath();
                 
                 // get the currently logged in user's local database username and password
                 IBPWorkbenchApplication app = IBPWorkbenchApplication.get();
                 User user = app.getSessionData().getUserData();
                 Project currentProject = app.getSessionData().getLastOpenedProject();
                 
                 User localIbdbUser = null;
                 try {
                     if (user != null && currentProject != null) {
                         Integer localIbdbUserId = workbenchDataManager.getLocalIbdbUserId(user.getUserid(), currentProject.getProjectId());
                         ManagerFactory managerFactory = managerFactoryProvider.getManagerFactoryForProject(currentProject);
                         UserDataManager userDataManager = managerFactory.getUserDataManager();
 
                         localIbdbUser = userDataManager.getUserById(localIbdbUserId);
                     }
                 }
                 catch (MiddlewareQueryException e) {
                     LOG.error("QueryException", e);
                     MessageNotifier.showError(window, messageSource.getMessage(Message.DATABASE_ERROR),
                             "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
                     return;
                 }
                 finally {
                     String url = tool.getPath();
                     if (localIbdbUser != null) {
                        url = getWebLoginForwardUrl(loginUrl, localIbdbUser.getName(), localIbdbUser.getPassword());
                    }
                    else if (user != null) {
                         url = getWebLoginForwardUrl(loginUrl, user.getName(), user.getPassword());
                     }
                     
                     Embedded browser = new Embedded("", new ExternalResource(url));
                     browser.setType(Embedded.TYPE_BROWSER);
                     browser.setSizeFull();
                     browser.setHeight("800px");
                     browser.setWidth("100%");
                     
                     NavManager.navigateApp(window, "/" + toolName, isLinkAccessed);
                     
                     IContentWindow contentWindow = (IContentWindow) window;
                     contentWindow.showContent(browser);
                 }
             }
             else {
                 
                 Embedded browser = new Embedded("", new ExternalResource(tool.getPath()));
                 browser.setType(Embedded.TYPE_BROWSER);
                 browser.setSizeFull();
                 browser.setHeight("800px");
                 browser.setWidth("100%");
                 
                 NavManager.navigateApp(window, "/" + toolName, isLinkAccessed);
                 
                 IContentWindow contentWindow = (IContentWindow) window;
                 contentWindow.showContent(browser);
             }
         }
     }
     
     private void showLaunchError(Window window, String tool) {
         MessageNotifier.showError(window, messageSource.getMessage(Message.LAUNCH_TOOL_ERROR),
                 "<br />" + messageSource.getMessage(Message.LAUNCH_TOOL_ERROR_DESC, tool));
     }
     
     private String getWebLoginForwardUrl(String url, String username, String password) {
         ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
         HttpServletRequest request = requestAttributes.getRequest();
         
         String urlFormat = "%s://%s:%d/%s/web_login_forward?login_url=%s&username=%s&password=%s";
         
         String scheme = request.getScheme();
         String serverName = request.getServerName();
         String contextPath = request.getContextPath();
         int port = request.getServerPort();
         
         return String.format(urlFormat, scheme, serverName, port, contextPath, url, username, password);
     }
 }
