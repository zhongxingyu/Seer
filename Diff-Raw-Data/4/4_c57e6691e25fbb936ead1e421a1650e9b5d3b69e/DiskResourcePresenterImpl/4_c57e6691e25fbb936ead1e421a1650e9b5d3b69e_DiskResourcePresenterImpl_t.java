 package org.iplantc.core.uidiskresource.client.presenters;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.resources.client.messages.IplantDisplayStrings;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.events.diskresources.DiskResourceRefreshEvent;
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uidiskresource.client.dataLink.presenter.DataLinkPresenter;
 import org.iplantc.core.uidiskresource.client.dataLink.view.DataLinkPanel;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceRenamedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesDeletedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesMovedEvent;
 import org.iplantc.core.uidiskresource.client.events.FolderCreatedEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestImportFromUrlEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.ShowFilePreviewEvent;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.DiskResourcesEventHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityGridHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityNavigationHandler;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.FolderContentsLoadConfig;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.FolderContentsRpcProxy;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.SelectDiskResourceByIdStoreAddHandler;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.SelectFolderByIdLoadHandler;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearch;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearchAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearchResult;
 import org.iplantc.core.uidiskresource.client.services.callbacks.CreateFolderCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceDeleteCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMetadataUpdateCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMoveCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceRestoreCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.GetDiskResourceDetailsCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.RenameDiskResourceCallback;
 import org.iplantc.core.uidiskresource.client.sharing.views.DataSharingDialog;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceSearchView;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 import org.iplantc.core.uidiskresource.client.views.HasHandlerRegistrationMgmt;
 import org.iplantc.core.uidiskresource.client.views.dialogs.FolderSelectDialog;
 import org.iplantc.core.uidiskresource.client.views.dialogs.InfoTypeEditorDialog;
 import org.iplantc.core.uidiskresource.client.views.metadata.DiskResourceMetadataDialog;
 import org.iplantc.core.uidiskresource.client.views.widgets.DiskResourceViewToolbar;
 import org.iplantc.core.uidiskresource.client.views.widgets.DiskResourceViewToolbarImpl;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.inject.Inject;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 import com.sencha.gxt.data.client.loader.RpcProxy;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.SortInfo;
 import com.sencha.gxt.data.shared.Store;
 import com.sencha.gxt.data.shared.Store.StoreFilter;
 import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
 import com.sencha.gxt.data.shared.loader.LoadHandler;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoader;
 import com.sencha.gxt.data.shared.loader.TreeLoader;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.info.Info;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
 
 /**
  * 
  * @author jstroot
  * 
  */
 public class DiskResourcePresenterImpl implements DiskResourceView.Presenter,
         DiskResourceViewToolbarImpl.Presenter, HasHandlerRegistrationMgmt {
 
     final DiskResourceView view;
     private final DiskResourceView.Proxy proxy;
     private final TreeLoader<Folder> treeLoader;
     private final HashMap<EventHandler, HandlerRegistration> registeredHandlers = new HashMap<EventHandler, HandlerRegistration>();
     private final List<HandlerRegistration> dreventHandlers = new ArrayList<HandlerRegistration>();
     private final DiskResourceServiceFacade diskResourceService;
     private final IplantDisplayStrings DISPLAY;
     private final DiskResourceAutoBeanFactory drFactory;
     private final Builder builder;
     private final DataSearchAutoBeanFactory dataSearchFactory;
     private final List<String> searchHistory = Lists.newArrayList();
     private String currentSearchTerm;
     @SuppressWarnings("rawtypes")
     private RpcProxy rpc_proxy;
     private PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>> gridLoader;
 
     @Inject
     public DiskResourcePresenterImpl(final DiskResourceView view, final DiskResourceView.Proxy proxy,
             final DiskResourceServiceFacade diskResourceService, final IplantDisplayStrings display,
             final DiskResourceAutoBeanFactory factory, final DataSearchAutoBeanFactory dataSearchFactory) {
         this.view = view;
         this.proxy = proxy;
         this.diskResourceService = diskResourceService;
         this.DISPLAY = display;
         this.drFactory = factory;
         this.dataSearchFactory = dataSearchFactory;
         
         builder = new MyBuilder(this);
 
         treeLoader = new TreeLoader<Folder>(this.proxy) {
             @Override
             public boolean hasChildren(Folder parent) {
                 return parent.hasSubDirs();
             }
         };
 
         this.view.setTreeLoader(treeLoader);
         this.view.setPresenter(this);
         this.proxy.setPresenter(this);
 
         initHandlers();
         initDragAndDrop();
         // loadSearchHistory();
         loadUserTrashPath();
         initFolderContentRpc();
     }
 
     @SuppressWarnings("unchecked")
     private void initFolderContentRpc() {
         rpc_proxy = new FolderContentsRpcProxy(diskResourceService);
         gridLoader = new PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>>(
                 rpc_proxy);
         FolderContentsLoadConfig config = new FolderContentsLoadConfig();
         gridLoader.useLoadConfig(config);
         gridLoader.setReuseLoadConfig(true);
         view.setViewLoader(gridLoader);
     }
     
     private void initDragAndDrop() {
 
     }
 
     @Override
     public void loadSearchHistory() {
         diskResourceService.getDataSearchHistory(new AsyncCallback<String>() {
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
 
             @Override
             public void onSuccess(String result) {
                 JSONObject obj = JsonUtil.getObject(result);
                 if (obj != null) {
                     JSONArray arr = JsonUtil.getArray(obj, "data-search"); //$NON-NLS-1$
                     if (arr != null) {
                         for (int i = 0; i < arr.size(); i++) {
                             searchHistory.add(arr.get(i).isString().stringValue());
                         }
                     }
                 }
                 view.renderSearchHistory(searchHistory);
             }
 
         });
 
     }
 
     @Override
     public void loadUserTrashPath() {
         final String userName = UserInfo.getInstance().getUsername();
         diskResourceService.getUserTrashPath(userName, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 // best guess of user trash. this is horrible
                 UserInfo.getInstance().setTrashPath("/iplant/trash/home/rods/" + userName);
             }
 
             @Override
             public void onSuccess(String result) {
                 JSONObject obj = JsonUtil.getObject(result);
                 UserInfo.getInstance().setTrashPath(JsonUtil.getString(obj, "trash"));
             }
         });
     }
 
     private void initHandlers() {
         // Add selection handlers which will control the visibility of the toolbar buttons
         DiskResourceViewToolbar toolbar = view.getToolbar();
         initToolbar(toolbar);
         addFileSelectChangedHandler(new ToolbarButtonVisibilityGridHandler(toolbar));
         addFolderSelectionHandler(new ToolbarButtonVisibilityNavigationHandler(toolbar));
 
         treeLoader.addLoadHandler(new ChildTreeStoreBinding<Folder>(view.getTreeStore()));
 
         EventBus eventBus = EventBus.getInstance();
         DiskResourcesEventHandler diskResourcesEventHandler = new DiskResourcesEventHandler(this);
         dreventHandlers.add(eventBus.addHandler(DiskResourceRefreshEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourcesDeletedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(FolderCreatedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourceRenamedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourceSelectedEvent.TYPE, diskResourcesEventHandler));
         dreventHandlers.add(eventBus.addHandler(DiskResourcesMovedEvent.TYPE, diskResourcesEventHandler));
 
 //        DataSearchHandler dataSearchHandler = new DataSearchHandler(this);
 //        eventBus.addHandler(DataSearchNameSelectedEvent.TYPE, dataSearchHandler);
 //        eventBus.addHandler(DataSearchPathSelectedEvent.TYPE, dataSearchHandler);
 //        eventBus.addHandler(DataSearchHistorySelectedEvent.TYPE, dataSearchHandler);
     }
 
     private void initToolbar(DiskResourceViewToolbar toolbar) {
         // Disable all buttons, except for Uploads.
         toolbar.setNewFolderButtonEnabled(false);
         toolbar.setRefreshButtonEnabled(false);
         toolbar.setDownloadsEnabled(false);
         toolbar.setBulkDownloadButtonEnabled(false);
         toolbar.setSimpleDowloadButtonEnabled(false);
         toolbar.setRenameButtonEnabled(false);
         toolbar.setShareButtonEnabled(false);
         toolbar.setDeleteButtonEnabled(false);
         toolbar.setRestoreMenuItemEnabled(false);
         toolbar.setEditEnabled(false);
         toolbar.setMoveButtonEnabled(false);
     }
     
     @Override
     public void cleanUp() {
         EventBus eventBus = EventBus.getInstance();
         for (HandlerRegistration hr : dreventHandlers) {
            eventBus.removeHandler(hr);
         }
     }
 
     @Override
     public void handleSearchEvent(DiskResource resource) {
         if (resource instanceof Folder) {
             Folder f = (Folder)resource;
             view.setSelectedFolder(f);
             onFolderSelected(f);
         } else {
             EventBus.getInstance().fireEvent(new ShowFilePreviewEvent((File)resource, this));
         }
         addToSearchHistory(getCurrentSearchTerm());
     }
 
     @Override
     public DiskResourceView getView() {
         return view;
     }
 
     @Override
     public void go(HasOneWidget container) {
         container.setWidget(view);
         // JDS Re-select currently selected folder in order to load center panel.
         setSelectedFolderById(getSelectedFolder());
     }
 
     @Override
     public void go(HasOneWidget container, HasId folderToSelect, final List<HasId> diskResourcesToSelect) {
 
         if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getId())) {
             go(container);
         } else {
             container.setWidget(view);
             setSelectedFolderById(folderToSelect);
             setSelectedDiskResourcesById(diskResourcesToSelect);
         }
     }
 
     @Override
     public void setSelectedDiskResourcesById(final List<HasId> diskResourcesToSelect) {
         SelectDiskResourceByIdStoreAddHandler diskResourceStoreAddHandler = new SelectDiskResourceByIdStoreAddHandler(
                 diskResourcesToSelect, this);
         HandlerRegistration diskResHandlerReg = view.getListStore().addStoreAddHandler(
                 diskResourceStoreAddHandler);
         addEventHandlerRegistration(diskResourceStoreAddHandler, diskResHandlerReg);
     }
 
     @Override
     public void setSelectedFolderById(final HasId folderToSelect) {
         if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getId())) {
             return;
         }
 
         Folder folder = view.getFolderById(folderToSelect.getId());
         if (folder != null) {
             // De-select currently selected folder, in case it is folderToSelect, to load center panel.
             view.deSelectNavigationFolder();
             view.setSelectedFolder(folder);
         } else {
             // Create and add the SelectFolderByIdLoadHandler to the treeLoader.
             final SelectFolderByIdLoadHandler handler = new SelectFolderByIdLoadHandler(folderToSelect,
                     this);
             HandlerRegistration reg = treeLoader.addLoadHandler(handler);
             addEventHandlerRegistration(handler, reg);
 
             // If a parent folder of folderToSelect already exists, then we need to load its children.
             String parentPath = DiskResourceUtil.parseParent(folderToSelect.getId());
             Folder parentFolder = view.getFolderByPath(parentPath);
             while (validParentPath(parentPath) && parentFolder == null) {
                 parentPath = DiskResourceUtil.parseParent(parentPath);
                 parentFolder = view.getFolderByPath(parentPath);
             }
 
             if (validParentPath(parentPath) && parentFolder != null) {
                 treeLoader.loadChildren(parentFolder);
             }
         }
     }
 
     private boolean validParentPath(String path) {
         return !Strings.isNullOrEmpty(path) && !path.equals("/"); //$NON-NLS-1$
     }
 
     @Override
     public Folder getSelectedFolder() {
         return view.getSelectedFolder();
     }
 
     @Override
     public Set<DiskResource> getSelectedDiskResources() {
         return view.getSelectedDiskResources();
     }
 
     @Override
     public void onFolderSelected(final Folder folder) {
         view.showDataListingWidget();
         view.deSelectDiskResources();
         FolderContentsLoadConfig config = gridLoader.getLastLoadConfig();
         config.setFolder(folder);
         gridLoader.load(0,200);
     }
 
     @Override
     public void onDiskResourceSelected(Set<DiskResource> selection) {
         if (selection != null && selection.size() == 1) {
             Iterator<DiskResource> it = selection.iterator();
             getDetails(it.next().getPath());
         } else {
             view.resetDetailsPanel();
         }
 
     }
 
     private void getDetails(String path) {
         JSONObject obj = new JSONObject();
         JSONArray arr = new JSONArray();
         arr.set(0, new JSONString(path));
         obj.put("paths", arr);
         diskResourceService.getStat(obj.toString(), new GetDiskResourceDetailsCallback(this, path,
                 drFactory));
 
     }
 
 
     @Override
     public void doBulkUpload() {
         EventBus.getInstance().fireEvent(new RequestBulkUploadEvent(this, getSelectedUploadFolder()));
     }
 
     @Override
     public void doSimpleUpload() {
         EventBus.getInstance().fireEvent(new RequestSimpleUploadEvent(this, getSelectedUploadFolder()));
     }
 
     @Override
     public void doImport() {
         EventBus.getInstance().fireEvent(new RequestImportFromUrlEvent(this, getSelectedUploadFolder()));
     }
 
     private Folder getSelectedUploadFolder() {
         Folder selectedFolder = getSelectedFolder();
 
         if (selectedFolder == null) {
             for (Folder root : view.getTreeStore().getRootItems()) {
                 if (root.getName().equals(UserInfo.getInstance().getUsername())) {
                     return root;
                 }
             }
         }
 
         return selectedFolder;
     }
 
     @Override
     public void doCreateNewFolder(final Folder parentFolder, final String newFolderName) {
         view.mask(DISPLAY.loadingMask());
         diskResourceService.createFolder(parentFolder, newFolderName, new CreateFolderCallback(
                 parentFolder, view));
     }
 
     @Override
     public void doRefresh(Folder folder) {
         String folderId = folder.getId();
         ArrayList<DiskResource> selectedResources = Lists.newArrayList(getSelectedDiskResources());
         EventBus.getInstance().fireEvent(new DiskResourceRefreshEvent(folderId, selectedResources));
     }
 
     @Override
     public void refreshFolder(String folderId, List<DiskResource> selectedResources) {
         Folder folder = view.getFolderById(folderId);
         if (folder == null) {
             return;
         }
 
         Folder selectedFolder = getSelectedFolder();
         view.refreshFolder(folder);
 
         if (selectedFolder == folder) {
             setSelectedFolderById(selectedFolder);
         }
     }
 
     @Override
     public void doSimpleDownload() {
         EventBus.getInstance().fireEvent(
                 new RequestSimpleDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doBulkDownload() {
         EventBus.getInstance().fireEvent(
                 new RequestBulkDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doRename(final DiskResource dr, final String newName) {
         view.mask(DISPLAY.loadingMask());
         diskResourceService.renameDiskResource(dr, newName, new RenameDiskResourceCallback(dr, view));
     }
 
     @Override
     public void doShare() {
         DataSharingDialog dlg = new DataSharingDialog(getSelectedDiskResources());
         dlg.show();
         dlg.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 onDiskResourceSelected(getSelectedDiskResources());
             }
         });
     }
 
     @Override
     public void requestDelete() {
         doDelete();
     }
 
     @Override
     public void doDelete() {
         Set<DiskResource> selectedResources = getSelectedDiskResources();
         if (!selectedResources.isEmpty() && DiskResourceUtil.isOwner(selectedResources)) {
             HashSet<DiskResource> drSet = Sets.newHashSet(selectedResources);
 
             if (DiskResourceUtil.containsTrashedResource(drSet)) {
                 confirmDelete(drSet);
             } else {
                 delete(drSet, DISPLAY.deleteMsg());
             }
         }
     }
 
     private void confirmDelete(final Set<DiskResource> drSet) {
         final MessageBox confirm = new ConfirmMessageBox(DISPLAY.warning(), DISPLAY.emptyTrashWarning());
 
         confirm.addHideHandler(new HideHandler() {
             @Override
             public void onHide(HideEvent event) {
                 if (confirm.getHideButton() == confirm.getButtonById(PredefinedButton.YES.name())) {
                     delete(drSet, DISPLAY.deleteTrash());
                 }
             }
         });
 
         confirm.show();
     }
 
     private void delete(Set<DiskResource> drSet, String announce) {
         view.mask(DISPLAY.loadingMask());
         final AsyncCallback<HasPaths> callback = new DiskResourceDeleteCallback(drSet, getSelectedFolder(), view, announce);
         diskResourceService.deleteDiskResources(drSet, callback);
     }
 
     @Override
     public void doMetadata() {
         if (getSelectedDiskResources().size() == 1) {
             DiskResourceMetadataDialog dlg = new DiskResourceMetadataDialog(getSelectedDiskResources()
                     .iterator().next(), this);
             dlg.show();
         }
 
     }
 
     @Override
     public void doDataQuota() {
         // TODO Auto-generated method stub
         Info.display("You clicked something!", "doDataQuota");
 
     }
 
     @Override
     public void addFileSelectChangedHandler(SelectionChangedHandler<DiskResource> selectionChangedHandler) {
         view.addDiskResourceSelectChangedHandler(selectionChangedHandler);
     }
 
     @Override
     public void addFolderSelectionHandler(SelectionHandler<Folder> selectionHandler) {
         view.addFolderSelectionHandler(selectionHandler);
     }
 
     // @Override
     // public void setSelectedDiskResourcesById(Set<String> diskResourceIdList) {
     //
     // }
 
     @Override
     public void unregisterHandler(EventHandler handler) {
         if (registeredHandlers.containsKey(handler)) {
             registeredHandlers.remove(handler).removeHandler();
         }
     }
 
     @Override
     public void addEventHandlerRegistration(EventHandler handler, HandlerRegistration reg) {
         registeredHandlers.put(handler, reg);
     }
 
     @Override
     public void getDiskResourceMetadata(DiskResource resource, AsyncCallback<String> callback) {
         diskResourceService.getDiskResourceMetaData(resource, callback);
     }
 
     @Override
     public void setDiskResourceMetaData(DiskResource resource, Set<DiskResourceMetadata> metadataToAdd,
             Set<DiskResourceMetadata> metadataToDelete,
             DiskResourceMetadataUpdateCallback diskResourceMetadataUpdateCallback) {
         diskResourceService.setDiskResourceMetaData(resource, metadataToAdd, metadataToDelete,
                 diskResourceMetadataUpdateCallback);
     }
 
     @Override
     public boolean canDragDataToTargetFolder(final Folder targetFolder,
             final Collection<DiskResource> dropData) {
         if (targetFolder.isFilter()) {
             return false;
         }
 
         // Assuming that ownership is of no concern.
         for (DiskResource dr : dropData) {
             // if the resource is a direct child of target folder
             if (DiskResourceUtil.isChildOfFolder(targetFolder, dr)) {
                 return false;
             }
 
             if (dr instanceof Folder) {
                if (targetFolder.getPath().equals(dr.getPath())) {
                    return false;
                }

                 // cannot drag an ancestor (parent, grandparent, etc) onto a child and/or descendant
                 if (DiskResourceUtil.isDescendantOfFolder((Folder)dr, targetFolder)) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public void doMoveDiskResources(Folder targetFolder, Set<DiskResource> resources) {
         diskResourceService.moveDiskResources(resources, targetFolder, new DiskResourceMoveCallback(
                 view, targetFolder, resources));
     }
 
     @Override
     public Set<? extends DiskResource> getDragSources(IsWidget source, Element dragStartEl) {
         // Verify the drag started from a valid item in the tree or grid, then return the selected items.
         if (isViewGrid(source)) {
             Set<DiskResource> selectedResources = getSelectedDiskResources();
 
             if (!selectedResources.isEmpty()) {
                 // Verify the dragStartEl is a row within the grid.
                 Element targetRow = view.findGridRow(dragStartEl);
 
                 if (targetRow != null) {
                     int dropIndex = view.findRowIndex(targetRow);
 
                     DiskResource selDiskResource = view.getListStore().get(dropIndex);
                     if (selDiskResource != null) {
                         return Sets.newHashSet(selectedResources);
                     }
                 }
             }
         } else if (isViewTree(source) && (getSelectedFolder() != null)) {
             // Verify the dragStartEl is a folder within the tree.
             Folder srcFolder = getDropTargetFolder(source, dragStartEl);
 
             if (srcFolder != null) {
                 return Sets.newHashSet(srcFolder);
             }
         }
 
         return null;
     }
 
     @Override
     public Folder getDropTargetFolder(IsWidget target, Element eventTargetElement) {
         Folder ret = null;
         if (view.isViewTree(target) && (view.findTreeNode(eventTargetElement) != null)) {
             TreeNode<Folder> targetTreeNode = view.findTreeNode(eventTargetElement);
             ret = targetTreeNode.getModel();
         } else if (view.isViewGrid(target)) {
             Element targetRow = view.findGridRow(eventTargetElement).cast();
 
             if (targetRow != null) {
                 int dropIndex = view.findRowIndex(targetRow);
 
                 DiskResource selDiskResource = view.getListStore().get(dropIndex);
                 ret = (selDiskResource instanceof Folder) ? (Folder)selDiskResource : null;
             }
         }
         return ret;
     }
 
     @Override
     public boolean isViewGrid(IsWidget widget) {
         return view.isViewGrid(widget);
     }
 
     @Override
     public boolean isViewTree(IsWidget widget) {
         return view.isViewTree(widget);
     }
 
     @Override
     public Builder builder() {
         return builder;
     }
 
     private class MyBuilder implements Builder {
 
         private final DiskResourceView.Presenter presenter;
 
         public MyBuilder(DiskResourceView.Presenter presenter) {
             this.presenter = presenter;
         }
 
         @Override
         public void go(HasOneWidget container) {
             presenter.go(container);
         }
 
         @Override
         public Builder hideNorth() {
             presenter.getView().setNorthWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideWest() {
             presenter.getView().setWestWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideCenter() {
             presenter.getView().setCenterWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder hideEast() {
             presenter.getView().setEastWidgetHidden(true);
             return this;
         }
 
         @Override
         public Builder singleSelect() {
             presenter.getView().setSingleSelect();
             return this;
         }
 
         @Override
         public Builder disableFilePreview() {
             presenter.getView().disableFilePreview();
             return this;
         }
 
     }
 
     @Override
     public void doSearch(final String val) {
         // TODO temp. remove search
         ListStore<DiskResource> store = view.getListStore();
         if (store.isFiltered()) {
             store.getFilters().clear();
         }
 
         if (Strings.isNullOrEmpty(val) || val.length() < 3) {
             store.setEnableFilters(false);
         } else {
             store.addFilter(new StoreFilter<DiskResource>() {
 
                 @Override
                 public boolean select(Store<DiskResource> store, DiskResource parent, DiskResource item) {
                     return item.getName().toLowerCase().contains(val.toLowerCase());
                 }
             });
             store.setEnableFilters(true);
         }
     }
 
     private void doSearchTempDisabled(final String val) {
         if (Strings.isNullOrEmpty(val) || val.length() < 3) {
             return;
         }
 
         diskResourceService.search(val, 50, null, new AsyncCallback<String>() {
 
             private DiskResourceSearchView searchView;
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
 
             }
 
             @Override
             public void onSuccess(String result) {
                 AutoBean<DataSearchResult> bean = AutoBeanCodex.decode(dataSearchFactory,
                         DataSearchResult.class, result);
                 List<DiskResource> resources = new ArrayList<DiskResource>();
                 for (DataSearch ds : bean.as().getSearchResults()) {
                     if (ds.getType().equalsIgnoreCase("file")) {
                         File f = buildDummyFile(ds);
                         resources.add(f);
                     } else {
                         Folder fo = buildDummyFolder(ds);
                         resources.add(fo);
                     }
 
                 }
 
                 if (searchView == null) {
                     searchView = new DiskResourceSearchView();
                 }
                 view.deSelectNavigationFolder();
                 searchView.loadResults(resources);
                 view.showSearchResultWidget(searchView.asWidget());
                 setCurrentSearchTerm(val);
             }
 
             private Folder buildDummyFolder(DataSearch ds) {
                 AutoBean<Folder> folder = AutoBeanCodex.decode(drFactory, Folder.class, "{}"); //$NON-NLS-1$
                 Folder fo = folder.as();
                 fo.setId(ds.getId());
                 fo.setName(ds.getName());
                 fo.setPath(ds.getId());
                 return fo;
             }
 
             private File buildDummyFile(DataSearch ds) {
                 AutoBean<File> file = AutoBeanCodex.decode(drFactory, File.class, "{}"); //$NON-NLS-1$
                 File f = file.as();
                 f.setId(ds.getId());
                 f.setName(ds.getName());
                 f.setPath(DiskResourceUtil.parseParent(ds.getId()));
                 return f;
             }
         });
     }
 
     @Override
     public void deSelectDiskResources() {
         view.deSelectDiskResources();
     }
 
     @Override
     public void addToSearchHistory(String searchTerm) {
         if (!searchHistory.contains(searchTerm)) {
             searchHistory.add(searchTerm);
         }
 
         saveSearchHistory();
         view.renderSearchHistory(searchHistory);
     }
 
     @Override
     public void removeFromSearchHistory(String searchTerm) {
         if (searchHistory.contains(searchTerm)) {
             searchHistory.remove(searchTerm);
         }
         saveSearchHistory();
         view.renderSearchHistory(searchHistory);
     }
 
     private Splittable getSearchHistoryAsSplittable() {
         Splittable split = StringQuoter.createSplittable();
         if (searchHistory.size() > 0) {
             DiskResourceUtil.createSplittableFromStringList(searchHistory).assign(split, "data-search");
         } else {
             StringQuoter.createIndexed().assign(split, "data-search");
         }
         return split;
     }
 
     @Override
     public void saveSearchHistory() {
         Splittable split = getSearchHistoryAsSplittable();
         diskResourceService.saveDataSearchHistory(split.getPayload(), new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.searchHistoryError(), caught);
             }
 
             @Override
             public void onSuccess(String result) {/* DO NOTHING */
             }
         });
     }
 
     @Override
     public String getCurrentSearchTerm() {
         return currentSearchTerm;
     }
 
     @Override
     public void setCurrentSearchTerm(String searchTerm) {
         this.currentSearchTerm = searchTerm;
 
     }
 
     @Override
     public void emptyTrash() {
         view.mask(I18N.DISPLAY.loadingMask());
         diskResourceService.emptyTrash(UserInfo.getInstance().getUsername(),
                 new AsyncCallback<String>() {
 
                     @Override
                     public void onSuccess(String result) {
                         doRefresh(view.getFolderByPath(UserInfo.getInstance().getTrashPath()));
                         view.unmask();
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(caught);
                         view.unmask();
                     }
                 });
 
     }
 
     @Override
     public void restore() {
         final Set<DiskResource> selectedResources = getSelectedDiskResources();
 
         if (selectedResources == null || selectedResources.isEmpty()) {
             return;
         }
 
         HasPaths request = drFactory.pathsList().as();
         request.setPaths(DiskResourceUtil.asStringIdList(selectedResources));
 
         maskView();
         diskResourceService.restoreDiskResource(request, new DiskResourceRestoreCallback(view,
                 drFactory, selectedResources));
     }
 
     @Override
     public void maskView() {
         view.mask(DISPLAY.loadingMask());
     }
 
     @Override
     public void unMaskView() {
         boolean hasLoadHandlers = false;
         for (Entry<EventHandler, HandlerRegistration> entry : registeredHandlers.entrySet()) {
             if (entry.getKey() instanceof LoadHandler<?, ?>) {
                 hasLoadHandlers = true;
             }
 
         }
         if (!hasLoadHandlers) {
             view.unmask();
         }
     }
 
     @Override
     public void unMaskView(boolean clearRegisteredHandlers) {
         if (clearRegisteredHandlers) {
             registeredHandlers.clear();
         }
         unMaskView();
     }
 
     @Override
     public void doDataLinks() {
         IPlantDialog dlg = new IPlantDialog(true);
         dlg.setHeadingText(I18N.DISPLAY.manageDataLinks());
         dlg.setHideOnButtonClick(true);
         dlg.setWidth(550);
         dlg.setOkButtonText(I18N.DISPLAY.done());
         DataLinkPanel.Presenter<DiskResource> dlPresenter = new DataLinkPresenter<DiskResource>(
                 new ArrayList<DiskResource>(getSelectedDiskResources()));
         dlPresenter.go(dlg);
         dlg.addHelp(new HTML(I18N.HELP.manageDataLinksHelp()));
         dlg.show();
     }
 
     @Override
     public void OnInfoTypeClick(final String id, final String type) {
         final InfoTypeEditorDialog dialog = new InfoTypeEditorDialog(type);
         dialog.show();
         dialog.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 String newType = dialog.getSelectedValue();
                 diskResourceService.setFileType(id, newType, new AsyncCallback<String>() {
 
                     @Override
                     public void onFailure(Throwable arg0) {
                         ErrorHandler.post(arg0);
                     }
 
                     @Override
                     public void onSuccess(String arg0) {
                         getDetails(id);
                     }
                 });
             }
         });
 
     }
 
     @Override
     public void onMove() {
         final FolderSelectDialog fsd = new FolderSelectDialog();
         fsd.show();
         fsd.addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 Folder targetFolder = fsd.getValue();
                 final Set<DiskResource> selectedResources = getSelectedDiskResources();
                 if (DiskResourceUtil.isMovable(targetFolder, selectedResources)) {
                     if (canDragDataToTargetFolder(targetFolder, selectedResources)) {
                         doMoveDiskResources(targetFolder, selectedResources);
                     } else {
                         IplantAnnouncer.getInstance().schedule(
                                 new ErrorAnnouncementConfig(I18N.ERROR.diskResourceIncompleteMove()));
                     }
                 } else {
                     IplantAnnouncer.getInstance().schedule(
                             new ErrorAnnouncementConfig(I18N.ERROR.permissionErrorMessage()));
                 }
             }
         });
 
     }
 
     @Override
     public void updateSortInfo(SortInfo sortInfo) {
         FolderContentsLoadConfig config = gridLoader.getLastLoadConfig();
         config.setSortInfo(Arrays.asList(sortInfo));
         gridLoader.load();
     }
 
 }
