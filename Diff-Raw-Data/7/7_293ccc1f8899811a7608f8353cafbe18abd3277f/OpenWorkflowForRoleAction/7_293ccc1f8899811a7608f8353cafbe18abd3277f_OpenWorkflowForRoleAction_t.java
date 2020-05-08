 package org.generationcp.ibpworkbench.actions;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.ibpworkbench.IBPWorkbenchApplication;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.SessionData;
 import org.generationcp.ibpworkbench.comp.MarsProjectDashboard;
import org.generationcp.ibpworkbench.comp.MasWorkflowDiagram;
 import org.generationcp.ibpworkbench.comp.window.IContentWindow;
 import org.generationcp.ibpworkbench.comp.window.ProgressWindow;
 import org.generationcp.ibpworkbench.navigation.NavManager;
 import org.generationcp.ibpworkbench.navigation.UriUtils;
 import org.generationcp.ibpworkbench.util.ToolUtil;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.Role;
 import org.generationcp.middleware.pojos.workbench.Tool;
 import org.generationcp.middleware.pojos.workbench.ToolType;
 import org.generationcp.middleware.pojos.workbench.WorkflowTemplate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Component.Event;
 import com.vaadin.ui.Window;
 
 @Configurable
 public class OpenWorkflowForRoleAction implements ItemClickListener, ClickListener, ActionListener {
     private static final long serialVersionUID = 1L;
     
     private static final Logger LOG = LoggerFactory.getLogger(OpenWorkflowForRoleAction.class);
     
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     @Autowired
     private ToolUtil toolUtil;
     
     private Project project;
     
     public OpenWorkflowForRoleAction() {
     }
     
     public OpenWorkflowForRoleAction(Project project) {
         this.project = project;
     }
     
     @Override
     public void itemClick(ItemClickEvent event) {
         @SuppressWarnings("unchecked")
         BeanItem<Role> item = (BeanItem<Role>) event.getItem();
         
         Role role = item.getBean();
         if (role == null) {
             return;
         }
         
         LOG.trace("Opening workflow for role {} on project {}", new Object[] { role, project });
         
         WorkflowTemplate workflowTemplate = role.getWorkflowTemplate();
         if (workflowTemplate == null) {
             LOG.warn("No workflow template assigned to role: {}", role);
             return;
         }
         
         Component component = event.getComponent();
         Window window = component.getWindow();
         IContentWindow contentWindow = (IContentWindow) window;
         
         showWorkflowDashboard(project, role, contentWindow);
         
         String url = String.format("/OpenProjectWorkflowForRole?projectId=%d&roleId=%d", project.getProjectId(), role.getRoleId());
         NavManager.navigateApp(window, url, true, project.getProjectName());
     }
     
     @Override
     public void buttonClick(ClickEvent event) {
         Window window = event.getComponent().getWindow();
         doAction(window, "/OpenWorkflowForRole", true);
     }
     
     @Override
     public void doAction(Event event) {
         NavManager.breadCrumbClick(this, event);
     }
     
     @Override
     public void doAction(Window window, String uriFragment, boolean isLinkAccessed) {
         IContentWindow contentWindow = (IContentWindow) window;
         Map<String, List<String>> params = UriUtils.getUriParameters(uriFragment);
         
         Project project = null;
         Role role = null;
         try {
             Long projectId = Long.parseLong(params.get("projectId").get(0));
             Integer roleId = Integer.parseInt(params.get("roleId").get(0));
             
             project = workbenchDataManager.getProjectById(projectId);
             role = workbenchDataManager.getRoleById(roleId);
             
             showWorkflowDashboard(project, role, contentWindow);
         }
         catch (MiddlewareQueryException e) {
             LOG.error("QueryException", e);
             MessageNotifier.showError(window, 
                     messageSource.getMessage(Message.DATABASE_ERROR), 
                     "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
         }
         
         NavManager.navigateApp(window, uriFragment, isLinkAccessed, project.getProjectName());
     }
     
     private void showWorkflowDashboard(Project project, Role role, IContentWindow contentWindow) {
         updateTools((Window) contentWindow, project);
         updateProjectLastOpenedDate((Window) contentWindow, project);
         
         SessionData sessionData = IBPWorkbenchApplication.get().getSessionData();
         sessionData.setLastOpenedProject(project);
         
         String projectTemplate = role.getWorkflowTemplate().getName();
         if (projectTemplate != null){
             if (projectTemplate.equals("MARS")) {
                 MarsProjectDashboard projectDashboard = new MarsProjectDashboard(project);
                 contentWindow.showContent(projectDashboard);
             }
             else if (projectTemplate.equals("MAS")) {
                MasWorkflowDiagram masWorkflowDiagram = new MasWorkflowDiagram(project);
                contentWindow.showContent(masWorkflowDiagram);
             }
             else if (projectTemplate.equals("Manager")) {
                 MarsProjectDashboard projectDashboard = new MarsProjectDashboard(project);
                 contentWindow.showContent(projectDashboard);
             }
         }
     }
     
     protected void updateTools(Window window, Project project) {
         IBPWorkbenchApplication app = IBPWorkbenchApplication.get();
         
         // don't do anything if the project is the last project opened
         if (app.getSessionData().isLastOpenedProject(project)) {
             return;
         }
         
         // show a progress window
         ProgressWindow progressWindow = new ProgressWindow(messageSource.getMessage(Message.UPDATING_TOOLS_CONFIGURATION), 10 * 1000);
         progressWindow.setCaption(messageSource.getMessage(Message.UPDATING));
         progressWindow.setModal(true);
         progressWindow.setClosable(false);
         progressWindow.setResizable(false);
         progressWindow.center();
         
         window.addWindow(progressWindow);
         progressWindow.startProgress();
         
         // get all native tools
         List<Tool> nativeTools = null;
         try {
             nativeTools = workbenchDataManager.getToolsWithType(ToolType.NATIVE);
         }
         catch (MiddlewareQueryException e1) {
             LOG.error("QueryException", e1);
             MessageNotifier.showError(window, messageSource.getMessage(Message.DATABASE_ERROR),
                     "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
             return;
         }
         
         for (Tool tool : nativeTools) {
             // close the native tools
             try {
                 toolUtil.closeNativeTool(tool);
             }
             catch (IOException e) {
                 LOG.error("Exception", e);
             }
             
             // rewrite the configuration file
             try {
                 toolUtil.updateToolConfigurationForProject(tool, project);
             }
             catch (IOException e) {
                 LOG.error("Exception", e);
             }
         }
         
         // get web tools
         List<Tool> webTools = null;
         try {
             webTools = workbenchDataManager.getToolsWithType(ToolType.WEB);
         }
         catch (MiddlewareQueryException e2) {
             LOG.error("QueryException", e2);
             MessageNotifier.showError(window, messageSource.getMessage(Message.DATABASE_ERROR),
                     "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
             return;
         }
         
         for (Tool tool : webTools) {
             // rewrite the configuration file
             try {
                 toolUtil.updateToolConfigurationForProject(tool, project);
             }
             catch (IOException e) {
                 LOG.error("Exception", e);
             }
         }
     }
     
     private void updateProjectLastOpenedDate(Window window, Project project) {
         try {
             project.setLastOpenDate(new Date());
             workbenchDataManager.saveOrUpdateProject(project);
             
             // set the last opened project in the session
             IBPWorkbenchApplication app = IBPWorkbenchApplication.get();
             app.getSessionData().setLastOpenedProject(project);
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString(), e);
             showDatabaseError(window);
         }
     }
     
     private void showDatabaseError(Window window) {
         MessageNotifier.showError(window, 
                 messageSource.getMessage(Message.DATABASE_ERROR), 
                 "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));
     }
 }
