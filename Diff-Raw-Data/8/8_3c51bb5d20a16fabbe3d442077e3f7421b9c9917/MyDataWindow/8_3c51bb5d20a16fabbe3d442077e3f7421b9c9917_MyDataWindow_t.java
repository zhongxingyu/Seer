 package org.iplantc.de.client.views.windows;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.core.metadata.client.JSONMetaDataObject;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.controllers.DataController;
 import org.iplantc.de.client.controllers.DataMonitor;
 import org.iplantc.de.client.events.DataPayloadEvent;
 import org.iplantc.de.client.events.DataPayloadEventHandler;
 import org.iplantc.de.client.events.ManageDataRefreshEvent;
 import org.iplantc.de.client.events.ManageDataRefreshEventHandler;
 import org.iplantc.de.client.events.disk.mgmt.DiskResourceSelectedEvent;
 import org.iplantc.de.client.models.BasicWindowConfig;
 import org.iplantc.de.client.models.ClientDataModel;
 import org.iplantc.de.client.models.WindowConfig;
 import org.iplantc.de.client.services.FolderServiceFacade;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.views.panels.DataDetailsPanel;
 import org.iplantc.de.client.views.panels.DataMainPanel;
 import org.iplantc.de.client.views.panels.DataNavigationPanel;
 
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * A window that shows a directory tree on the left panel, a directory list in the middle, and a
  * details/preview panel on the right.
  */
 public class MyDataWindow extends IPlantThreePanelWindow implements DataMonitor {
     private ClientDataModel model;
 
     private DataNavigationPanel pnlNavigation;
     private DataMainPanel pnlMain;
     private DataDetailsPanel pnlDetails;
 
     protected List<HandlerRegistration> handlers;
 
     /**
      * {@inheritDoc}
      */
     public MyDataWindow(final String tag, final BasicWindowConfig config) {
         super(tag, config);
 
         initHandlers();
     }
 
     private void initHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         handlers = new ArrayList<HandlerRegistration>();
 
         handlers.add(eventbus.addHandler(DataPayloadEvent.TYPE, new DataPayloadEventHandlerImpl()));
         handlers.add(eventbus.addHandler(ManageDataRefreshEvent.TYPE,
                 new ManageDataRefreshEventHandlerImpl()));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initPanels() {
         pnlNavigation = new DataNavigationPanel(tag, DataNavigationPanel.Mode.EDIT);
         pnlNavigation.setMaskingParent(pnlContents);
 
         pnlMain = new DataMainPanel(tag, model);
         pnlMain.setMaskingParent(pnlContents);
 
         pnlDetails = new DataDetailsPanel(tag);
         pnlDetails.setMaskingParent(pnlContents);
     }
 
     private void seed(final String username, final String json) {
         model.seed(json);
         pnlNavigation.seed(username, model);
         pnlMain.seed(model);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void compose() {
         pnlContents.add(pnlNavigation, dataWest);
         pnlContents.add(pnlMain, dataCenter);
         pnlContents.add(pnlDetails, dataEast);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void afterRender() {
         super.afterRender();
 
         retrieveData(new RetrieveDataCallback());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void show() {
         super.show();
 
         if (model != null && model.getRootFolder() != null) {
             selectConfigNode();
         }
     }
 
     /**
      * Applies a window configuration to the window.
      * 
      * @param config
      */
     @Override
     public void configure(WindowConfig config) {
         if (config instanceof BasicWindowConfig) {
             this.config = (BasicWindowConfig)config;
         }
     }
 
     /**
      * 
      * Get user workspace's files and folder
      * 
      */
     protected void retrieveData(AsyncCallback<String> callback) {
         FolderServiceFacade facade = new FolderServiceFacade();
 
         mask(I18N.DISPLAY.loadingMask());
 
         facade.getHomeFolder(callback);
     }
 
     private class RetrieveDataCallback implements AsyncCallback<String> {
         @Override
         public void onFailure(Throwable caught) {
             handleFailure(caught);
         }
 
         @Override
         public void onSuccess(String result) {
             unmask();
             seed(UserInfo.getInstance().getUsername(), result);
             // Select the folder set by the config
             if (!selectConfigNode()) {
                 // The configured folder is not available
                 // select the root instead.
                 selectRootNode();
             }
         }
 
     }
 
     /**
      * Selects the folder set in the basic window config.
      * 
      * @return true if a folder was found to select, false otherwise.
      */
     private boolean selectConfigNode() {
         if (config == null) {
             return false;
         }
 
         String path = config.getId();
 
         // reset the config here so that this folder is not selected every time the window in minimized
         // and re-shown.
         config = null;
 
        return pnlNavigation.selectFolder(path);
 
     }
 
     private void selectRootNode() {
         if (pnlNavigation != null) {
             Folder root = model.getRootFolder();
 
             if (root != null) {
                 pnlNavigation.selectFolder(root.getId());
             }
         }
     }
 
     public void removeEventHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         // unregister
         for (HandlerRegistration reg : handlers) {
             eventbus.removeHandler(reg);
         }
 
         // clear our list
         handlers.clear();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void cleanup() {
         super.cleanup();
 
         removeEventHandlers();
 
         pnlNavigation.cleanup();
         pnlMain.cleanup();
         pnlDetails.cleanup();
     }
 
     private void deleteFolders(final List<String> folders) {
         if (folders != null) {
             pnlMain.deleteDiskResources(folders);
 
             for (String path : folders) {
                 model.deleteDiskResource(path);
 
                 if (model.isCurrentPage(path)) {
                     Folder folder = model.getFolder(DataUtils.parseParent(path));
 
                     if (folder != null) {
                         EventBus.getInstance().fireEvent(new DiskResourceSelectedEvent(folder, tag));
                     }
                 }
             }
         }
     }
 
     private void deleteFiles(final List<String> files) {
         model.deleteDiskResources(files);
         pnlMain.deleteDiskResources(files);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteResources(List<String> folders, List<String> files) {
         deleteFolders(folders);
         deleteFiles(files);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileRename(String id, String name) {
         if (pnlMain != null) {
             pnlMain.renameFile(id, name);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderCreated(String idParentFolder, JSONObject jsonFolder) {
         Folder folder = model.createFolder(idParentFolder, jsonFolder);
 
         pnlMain.addDiskResource(idParentFolder, folder);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderRename(String id, String name) {
         Folder folder = model.renameFolder(id, name);
 
         if (folder != null) {
             pnlMain.renameFolder(id, name);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderMove(Map<String, String> folders) {
         if (folders != null) {
             Iterator<String> itr = folders.keySet().iterator();
 
             while (itr.hasNext()) {
                 String srcId = itr.next().toString();
                 String destId = folders.get(srcId);
 
                 Folder src = model.getFolder(srcId);
                 Folder dest = model.getFolder(destId);
 
                 if (src != null && dest != null) {
                     // remove from main panel
                     pnlMain.deleteDiskResource(srcId);
 
                     // construct new Id
                     String[] tokens = srcId.split("/"); //$NON-NLS-1$
                     String newSrcId = destId + "/" + tokens[tokens.length - 1]; //$NON-NLS-1$
                     model.moveResource(src, newSrcId, destId);
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileMove(Map<String, String> files) {
         if (files != null) {
             Iterator<String> itr = files.keySet().iterator();
             while (itr.hasNext()) {
                 // remove from main panel
                 pnlMain.deleteDiskResource(itr.next().toString());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void addFile(String path, File file) {
         model.createFile(path, file);
 
         pnlMain.addDiskResource(path, file);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileSavedAs(String idOrig, String idParentFolder, File info) {
         addFile(idParentFolder, info);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getCaption() {
         return I18N.DISPLAY.data();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected int getEastWidth() {
        return 180;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected int getWestWidth() {
         return 160;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initModel() {
         model = new ClientDataModel();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void setInitialSize() {
         setSize(800, 410);
     }
 
     private void handleFailure(Throwable caught) {
         unmask();
         ErrorHandler.post(I18N.ERROR.retrieveFolderInfoFailed(), caught);
     }
 
     private class DataPayloadEventHandlerImpl implements DataPayloadEventHandler {
         @Override
         public void onFire(DataPayloadEvent event) {
             DataController controller = DataController.getInstance();
             controller.handleEvent(MyDataWindow.this, event.getPayload());
         }
     }
 
     private class ManageDataRefreshEventHandlerImpl implements ManageDataRefreshEventHandler {
 
         @Override
         public void onRefresh(ManageDataRefreshEvent mdre) {
             if (tag.equals(mdre.getTag())) {
                 pnlNavigation.removeTreePanel();
                 initModel();
                 JSONObject obj = new JSONObject();
                 obj.put(JSONMetaDataObject.ID, new JSONString(mdre.getCurrentFolderId()));
                 configure(new BasicWindowConfig(obj));
                 retrieveData(new RetrieveDataCallback());
             }
         }
 
     }
 }
