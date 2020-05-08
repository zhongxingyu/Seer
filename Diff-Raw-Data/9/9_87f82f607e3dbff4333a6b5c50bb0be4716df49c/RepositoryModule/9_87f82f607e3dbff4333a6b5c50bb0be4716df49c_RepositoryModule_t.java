 package org.mule.galaxy.repository.client;
 
 import org.mule.galaxy.repository.client.activity.ActivityPanel;
 import org.mule.galaxy.repository.client.admin.LifecycleForm;
 import org.mule.galaxy.repository.client.admin.LifecycleListPanel;
 import org.mule.galaxy.repository.client.admin.PolicyPanel;
 import org.mule.galaxy.repository.client.admin.PropertyDescriptorForm;
 import org.mule.galaxy.repository.client.admin.PropertyDescriptorListPanel;
 import org.mule.galaxy.repository.client.admin.TypeForm;
 import org.mule.galaxy.repository.client.admin.TypeListPanel;
 import org.mule.galaxy.repository.client.item.RepositoryMenuPanel;
 import org.mule.galaxy.repository.client.property.PropertyInterfaceManager;
 import org.mule.galaxy.repository.rpc.RegistryService;
 import org.mule.galaxy.repository.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.GalaxyModule;
 import org.mule.galaxy.web.client.NavMenuItem;
 import org.mule.galaxy.web.client.admin.AdministrationPanel;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 public class RepositoryModule implements GalaxyModule {
     protected Galaxy galaxy;
     private PropertyInterfaceManager propertyInterfaceManager = new PropertyInterfaceManager();
     protected RepositoryConstants repositoryConstants;
     private RegistryServiceAsync registryService;
     private int repositoryTabIndex = 0;
     private boolean showTypeSystem = true;
     
     public void initialize(Galaxy galaxy) {
         this.galaxy = galaxy;
         
         if (repositoryConstants == null) {
             this.repositoryConstants = (RepositoryConstants) GWT.create(RepositoryConstants.class);
         }
         
         createService();
         loadRepositoryTab();
         addAdministrationMenuItems();
     }
 
     protected void addAdministrationMenuItems() {
 
        AdministrationPanel adminPanel = galaxy.getAdministrationPanel();
         if (galaxy.hasPermission("VIEW_ACTIVITY")) {
             adminPanel.addUtilityMenuItem(new NavMenuItem("Activity",
                                                           "ActivityPanel",
                                                           new ActivityPanel(adminPanel, galaxy, registryService)));
         }
         
         if (isShowTypeSystem()) {
             if (galaxy.hasPermission("MANAGE_LIFECYCLES")) {
                 NavMenuItem item = new NavMenuItem("Lifecycles",
                                                    "lifecycles",
                                                    new LifecycleListPanel(adminPanel, registryService),
                                                    new LifecycleForm(adminPanel, registryService));
                 adminPanel.addManageMenuItem(item);
             }
             
             if (galaxy.hasPermission("MANAGE_POLICIES")) {
                 NavMenuItem item = new NavMenuItem("Policies", "policies", new PolicyPanel(adminPanel, galaxy, registryService), null);
                 adminPanel.addManageMenuItem(item);
             }
 
             if (galaxy.hasPermission("MANAGE_PROPERTIES")) {
                 NavMenuItem item = new NavMenuItem("Properties",
                         "properties",
                         new PropertyDescriptorListPanel(adminPanel, registryService),
                         new PropertyDescriptorForm(adminPanel, this));
                 adminPanel.addManageMenuItem(item);
             }
 
             if (galaxy.hasPermission("MANAGE_PROPERTIES")) {
                 NavMenuItem item = new NavMenuItem("Types",
                         "types",
                         new TypeListPanel(adminPanel, registryService),
                         new TypeForm(adminPanel, this));
                 adminPanel.addManageMenuItem(item);
             }
         }
     }
     
     protected RepositoryMenuPanel createRepositoryPanels() {
         return new RepositoryMenuPanel(this);
     }
 
     public int getRepositoryTab() {
         return repositoryTabIndex;
     }
 
     protected void loadRepositoryTab() {
         galaxy.getPageManager().createTab(repositoryTabIndex, "Repository", "browse", repositoryConstants.repo_TabTip());
         createRepositoryPanels();
     }
 
     private void createService() {
         this.registryService = (RegistryServiceAsync) GWT.create(RegistryService.class);
         ServiceDefTarget target = (ServiceDefTarget) registryService;
         String baseUrl = GWT.getModuleBaseURL();
         target.setServiceEntryPoint(baseUrl + "../handler/registry.rpc");
     }
 
     public Galaxy getGalaxy() {
         return galaxy;
     }
 
     public PropertyInterfaceManager getPropertyInterfaceManager() {
         return propertyInterfaceManager;
     }
 
     public RepositoryConstants getRepositoryConstants() {
         return repositoryConstants;
     }
 
     public RegistryServiceAsync getRegistryService() {
         return registryService;
     }
 
     public boolean isShowTypeSystem() {
         return showTypeSystem;
     }
 
     public void setShowTypeSystem(boolean showTypeSystem) {
         this.showTypeSystem = showTypeSystem;
     }
 
     public void setRepositoryTab(int repositoryTabIndex) {
         this.repositoryTabIndex = repositoryTabIndex;
     }
     
 
 }
