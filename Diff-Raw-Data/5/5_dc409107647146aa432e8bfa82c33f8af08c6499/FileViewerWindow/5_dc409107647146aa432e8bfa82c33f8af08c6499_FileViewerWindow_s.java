 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uidiskresource.client.services.errors.DiskResourceErrorAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.services.errors.ErrorGetManifest;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.events.FileEditorWindowClosedEvent;
 import org.iplantc.de.client.viewer.events.FileSavedEvent;
 import org.iplantc.de.client.viewer.events.FileSavedEvent.FileSavedEventHandler;
 import org.iplantc.de.client.viewer.events.SaveFileEvent;
 import org.iplantc.de.client.viewer.presenter.FileViewerPresenter;
 import org.iplantc.de.client.viewer.views.FileViewer;
 import org.iplantc.de.client.views.windows.configs.FileViewerWindowConfig;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.widget.core.client.PlainTabPanel;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 
 /**
  * @author sriram
  * 
  */
 public class FileViewerWindow extends IplantWindowBase {
 
     private PlainTabPanel tabPanel;
     protected JSONObject manifest;
     protected File file;
     private final FileViewerWindowConfig configAB;
     private FileViewer.Presenter p;
 
     public FileViewerWindow(FileViewerWindowConfig config) {
         super(null, null);
         this.configAB = config;
         EventBus.getInstance().addHandler(FileSavedEvent.TYPE, new FileSavedEventHandler() {
 
             @Override
             public void onFileSaved(FileSavedEvent event) {
                 if (file == null) {
                     file = event.getFile();
                 }
                 setTitle(file.getName());
                tabPanel = null;
                getFileManifest();
             }
 
         });
         init();
     }
 
     private void init() {
         setSize("670px", "400px");
         this.file = configAB.getFile();
         getFileManifest();
         if (file != null) {
             setTitle(file.getName());
         } else {
             setTitle("Untitled-" + Math.random());
         }
     }
 
     private void initWidget() {
         if (tabPanel == null) {
             tabPanel = new PlainTabPanel();
             add(tabPanel);
             forceLayout();
         }
     }
 
     /**
      * Returns an array from the manifest for a given key, or null if no array exists under that key.
      * 
      * @param key
      * @return
      */
     protected JSONValue getItems(String key) {
         return (key != null && manifest != null && manifest.containsKey(key)) ? manifest.get(key) : null;
     }
 
     @Override
     public void doHide() {
         if (p != null && p.isDirty()) {
             final ConfirmMessageBox cmb = new ConfirmMessageBox(I18N.DISPLAY.save(),
                     I18N.DISPLAY.unsavedChanges());
             cmb.addHideHandler(new HideHandler() {
 
                 @Override
                 public void onHide(HideEvent event) {
                     if (cmb.getHideButton().getText().equalsIgnoreCase("yes")) {
                         SaveFileEvent sfe = new SaveFileEvent();
                         EventBus.getInstance().fireEvent(sfe);
                     } else {
                         FileViewerWindow.super.doHide();
                         doClose();
                     }
                 }
             });
             cmb.show();
         } else {
             super.doHide();
             doClose();
         }
     }
 
     private void doClose() {
         if (file != null) {
             EventBus eventbus = EventBus.getInstance();
             FileEditorWindowClosedEvent event = new FileEditorWindowClosedEvent(file.getId());
             eventbus.fireEvent(event);
         }
     }
 
     @Override
     public PlainTabPanel getWidget() {
         return tabPanel;
     }
 
     @Override
     public WindowState getWindowState() {
         return createWindowState(configAB);
     }
 
     private void getFileManifest() {
         mask(I18N.DISPLAY.loadingMask());
         if (file != null) {
             Services.FILE_EDITOR_SERVICE.getManifest(file.getId(), new AsyncCallback<String>() {
                 @Override
                 public void onSuccess(String result) {
                     if (result != null) {
                         manifest = JsonUtil.getObject(result);
                         p = new FileViewerPresenter(file, manifest, configAB.isEditing());
                         initWidget();
                         p.go(FileViewerWindow.this);
                         unmask();
                     } else {
                         onFailure(null);
                     }
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     unmask();
                     DiskResourceErrorAutoBeanFactory factory = GWT
                             .create(DiskResourceErrorAutoBeanFactory.class);
                     String message = caught.getMessage();
                     FileViewerWindow.this.hide();
 
                     if (JsonUtils.safeToEval(message)) {
                         AutoBean<ErrorGetManifest> errorBean = AutoBeanCodex.decode(factory,
                                 ErrorGetManifest.class, message);
                         ErrorHandler.post(errorBean.as(), caught);
                     } else {
                         ErrorHandler.post(I18N.ERROR.retrieveStatFailed(), caught);
                     }
                 }
             });
         } else {
             if (configAB.isEditing()) {
                 JSONObject manifest = new JSONObject();
                 manifest.put("content-type", new JSONString("plain"));
                 p = new FileViewerPresenter(file, manifest, configAB.isEditing());
                 initWidget();
                 p.go(FileViewerWindow.this);
                 unmask();
             }
         }
     }
 
 }
