 package org.iplantc.de.client.views.windows;
 
 import java.util.List;
 
 import org.iplantc.core.uiapps.integration.client.presenter.AppsIntegrationPresenterImpl;
 import org.iplantc.core.uiapps.integration.client.view.AppsIntegrationView;
 import org.iplantc.core.uiapps.integration.client.view.AppsIntegrationViewImpl;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplate;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplateAutoBeanFactory;
 import org.iplantc.core.uiapps.widgets.client.models.Argument;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentGroup;
 import org.iplantc.core.uiapps.widgets.client.services.AppMetadataServiceFacade;
 import org.iplantc.core.uiapps.widgets.client.services.AppTemplateServices;
 import org.iplantc.core.uiapps.widgets.client.services.DeployedComponentServices;
 import org.iplantc.core.uiapps.widgets.client.services.impl.AppTemplateCallbackConverter;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.CommonModelUtils;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.UUIDServiceAsync;
 import org.iplantc.de.client.views.windows.configs.AppsIntegrationWindowConfig;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.sencha.gxt.widget.core.client.event.MaximizeEvent;
 import com.sencha.gxt.widget.core.client.event.MaximizeEvent.MaximizeHandler;
 import com.sencha.gxt.widget.core.client.event.RestoreEvent;
 import com.sencha.gxt.widget.core.client.event.RestoreEvent.RestoreHandler;
 import com.sencha.gxt.widget.core.client.event.ShowEvent;
 import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
 
 /**
  * A window for the App Integration editor XXX JDS Much of the Apps Integration module config, presenter,
  * and window closely mimic that of the App Wizard.
  * 
  * @author jstroot, sriram, psarando
  * 
  */
 public class AppIntegrationWindow extends IplantWindowBase {
 
     private final AppsIntegrationView.Presenter presenter;
     protected List<HandlerRegistration> handlers;
     private final AppTemplateServices templateService;
     private final AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
     private final DeployedComponentServices dcServices = GWT.create(DeployedComponentServices.class);
 
     public AppIntegrationWindow(AppsIntegrationWindowConfig config, final EventBus eventBus,
             final UUIDServiceAsync uuidService, final AppMetadataServiceFacade appMetadataService) {
         super(null, config);
 
         AppsIntegrationView view = new AppsIntegrationViewImpl(uuidService, appMetadataService);
         templateService = GWT.create(AppTemplateServices.class);
         presenter = new AppsIntegrationPresenterImpl(view, eventBus, templateService, I18N.ERROR,
                 I18N.DISPLAY, uuidService);
         presenter.setOnlyLabelEditMode(config.isOnlyLabelEditMode());
         setTitle(I18N.DISPLAY.createApps());
         setSize("600", "375");
 
         addRestoreHandler(new RestoreHandler() {
 
             @Override
             public void onRestore(RestoreEvent event) {
                 maximized = false;
             }
         });
 
         addMaximizeHandler(new MaximizeHandler() {
 
             @Override
             public void onMaximize(MaximizeEvent event) {
                 maximized = true;
             }
         });
 
         addShowHandler(new ShowHandler() {
 
             @Override
             public void onShow(ShowEvent event) {
                 AppIntegrationWindow.this.maximize();
             }
         });
 
         init(presenter, config);
 
         // JDS Add presenter as a before hide handler to determine if user has changes before closing.
         HandlerRegistration hr = this.addBeforeHideHandler(presenter);
         presenter.setBeforeHideHandlerRegistration(hr);
     }
 
     private void init(final AppsIntegrationView.Presenter presenter, AppsIntegrationWindowConfig config) {
         Splittable legacyAppTemplateJson = config.getLegacyAppTemplateJson();
         if (config.getAppTemplate() != null) {
             AppTemplateCallbackConverter at = new AppTemplateCallbackConverter(factory, dcServices,
                     new AsyncCallback<AppTemplate>() {
 
                         @Override
                         public void onSuccess(AppTemplate result) {
                             presenter.go(AppIntegrationWindow.this, result);
                            AppIntegrationWindow.this.forceLayout();
                         }
 
                         @Override
                         public void onFailure(Throwable caught) {
                             /*
                              * JDS Do nothing since this this callback converter is called manually below
                              * (i.e. no over-the-wire integration)
                              */
                         }
                     });
             at.onSuccess(config.getAppTemplate().getPayload());
         } else if ((legacyAppTemplateJson != null) && (!legacyAppTemplateJson.getPayload().isEmpty())) {
             presenter.goLegacy(this, config.getLegacyAppTemplateJson());
         } else if (config.getAppId().equalsIgnoreCase(Constants.CLIENT.newAppTemplate())) {
             // Create empty AppTemplate
             AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
 
             AppTemplate newAppTemplate = factory.appTemplate().as();
             newAppTemplate.setName("New App");
             ArgumentGroup argGrp = factory.argumentGroup().as();
             argGrp.setName("");
             argGrp.setLabel("Group 1");
             argGrp.setArguments(Lists.<Argument> newArrayList());
             newAppTemplate.setArgumentGroups(Lists.<ArgumentGroup> newArrayList(argGrp));
             presenter.go(this, newAppTemplate);
            AppIntegrationWindow.this.forceLayout();
         } else {
             templateService.getAppTemplateForEdit(
                     CommonModelUtils.createHasIdFromString(config.getAppId()),
                     new AsyncCallback<AppTemplate>() {
                         @Override
                         public void onFailure(Throwable caught) {
                             ErrorHandler.post(I18N.ERROR.unableToRetrieveWorkflowGuide(), caught);
                             AppIntegrationWindow.this.hide();
                         }
 
                         @Override
                         public void onSuccess(AppTemplate result) {
                             presenter.go(AppIntegrationWindow.this, result);
                            AppIntegrationWindow.this.forceLayout();
                         }
                     });
         }
     }
 
     @Override
     public WindowState getWindowState() {
         return createWindowState(getUpdatedConfig());
     }
 
     /**
      * @return
      */
     private AppsIntegrationWindowConfig getUpdatedConfig() {
         AppsIntegrationWindowConfig config = ConfigFactory.appsIntegrationWindowConfig("");
         AppTemplate appTemplate = presenter.getAppTemplate();
         config.setAppTemplate(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(appTemplate)));
         return config;
     }
 }
