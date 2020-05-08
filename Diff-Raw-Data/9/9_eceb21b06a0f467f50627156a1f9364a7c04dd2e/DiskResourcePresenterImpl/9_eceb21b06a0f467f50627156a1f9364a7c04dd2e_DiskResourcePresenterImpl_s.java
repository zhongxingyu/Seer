 package org.iplantc.core.uidiskresource.client.presenters;
 
 import java.util.ArrayList;
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
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uicommons.client.widgets.ContextualHelpPopup;
 import org.iplantc.core.uidiskresource.client.dataLink.presenter.DataLinkPresenter;
 import org.iplantc.core.uidiskresource.client.dataLink.view.DataLinkPanel;
 import org.iplantc.core.uidiskresource.client.events.DataSearchHistorySelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DataSearchNameSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DataSearchPathSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceRenamedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesDeletedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesMovedEvent;
 import org.iplantc.core.uidiskresource.client.events.FileUploadedEvent;
 import org.iplantc.core.uidiskresource.client.events.FolderCreatedEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestImportFromUrlEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.ShowFilePreviewEvent;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceMetadata;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.DataSearchHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.DiskResourcesEventHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityGridHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityNavigationHandler;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.DiskResourceViewLoadHandler;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.SelectDiskResourceByIdStoreAddHandler;
 import org.iplantc.core.uidiskresource.client.presenters.proxy.SelectFolderByIdLoadHandler;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearch;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearchAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearchResult;
 import org.iplantc.core.uidiskresource.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uidiskresource.client.services.callbacks.CreateFolderCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceDeleteCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMetadataUpdateCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMoveCallback;
 import org.iplantc.core.uidiskresource.client.services.callbacks.RenameDiskResourceCallback;
 import org.iplantc.core.uidiskresource.client.sharing.views.DataSharingDialog;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceSearchView;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 import org.iplantc.core.uidiskresource.client.views.HasHandlerRegistrationMgmt;
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
 import com.sencha.gxt.data.shared.loader.LoadHandler;
 import com.sencha.gxt.data.shared.loader.TreeLoader;
 import com.sencha.gxt.widget.core.client.button.ToolButton;
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
     private DiskResourceServiceFacade diskResourceService;
     private final IplantDisplayStrings DISPLAY;
     private final DiskResourceAutoBeanFactory drFactory;
     private final Builder builder;
     private final DataSearchAutoBeanFactory dataSearchFactory;
     private final List<String> searchHistory = Lists.newArrayList();
     private String currentSearchTerm;
 
     @Inject
     public DiskResourcePresenterImpl(final DiskResourceView view, final DiskResourceView.Proxy proxy,
             final DiskResourceServiceFacade diskResourceService,
             final IplantDisplayStrings display, final DiskResourceAutoBeanFactory factory,
  final DataSearchAutoBeanFactory dataSearchFactory) {
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
         loadSearchHistory();
         loadUserTrashPath();
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
                     JSONArray arr = JsonUtil.getArray(obj, "data-search");
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
                 // best guess of user name. this is horrible
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
 
         treeLoader.addLoadHandler(new DiskResourceViewLoadHandler(this.view.getTreeStore(), this));
 
         EventBus eventBus = EventBus.getInstance();
         DiskResourcesEventHandler diskResourcesEventHandler = new DiskResourcesEventHandler(this);
         eventBus.addHandler(FileUploadedEvent.TYPE, diskResourcesEventHandler);
         eventBus.addHandler(DiskResourcesDeletedEvent.TYPE, diskResourcesEventHandler);
         eventBus.addHandler(FolderCreatedEvent.TYPE, diskResourcesEventHandler);
         eventBus.addHandler(DiskResourceRenamedEvent.TYPE, diskResourcesEventHandler);
         eventBus.addHandler(DiskResourceSelectedEvent.TYPE, diskResourcesEventHandler);
         eventBus.addHandler(DiskResourcesMovedEvent.TYPE, diskResourcesEventHandler);
 
         DataSearchHandler dataSearchHandler = new DataSearchHandler(this);
         eventBus.addHandler(DataSearchNameSelectedEvent.TYPE, dataSearchHandler);
         eventBus.addHandler(DataSearchPathSelectedEvent.TYPE, dataSearchHandler);
         eventBus.addHandler(DataSearchHistorySelectedEvent.TYPE, dataSearchHandler);
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
        Folder tmp = getSelectedFolder();
        if (tmp != null) {
            view.deSelectNavigationFolder();
            view.setSelectedFolder(tmp);
        }
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
         SelectDiskResourceByIdStoreAddHandler diskResourceStoreAddHandler = new SelectDiskResourceByIdStoreAddHandler(diskResourcesToSelect, this);
         HandlerRegistration diskResHandlerReg = view.getListStore().addStoreAddHandler(diskResourceStoreAddHandler);
         addEventHandlerRegistration(diskResourceStoreAddHandler, diskResHandlerReg);
     }
 
     @Override
     public void setSelectedFolderById(final HasId folderToSelect) {
         if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getId())) {
             return;
         }
 
         Folder folder = view.getFolderById(folderToSelect.getId());
         if (folder != null) {
             view.setSelectedFolder(folder);
         } else {
             // Create and add the SelectFolderByIdLoadHandler to the treeLoader.
             SelectFolderByIdLoadHandler handler = new SelectFolderByIdLoadHandler(folderToSelect, this);
             HandlerRegistration reg = treeLoader.addLoadHandler(handler);
             addEventHandlerRegistration(handler, reg);
         }
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
     public void onFolderSelected(Folder folder) {
         view.showDataListingWidget();
         view.deSelectDiskResources();
         if (view.isLoaded(folder)) {
             Set<DiskResource> children = Sets.newHashSet();
             if (folder.getFolders() != null) {
                 children.addAll(folder.getFolders());
             }
             if (folder.getFiles() != null) {
                 children.addAll(folder.getFiles());
             }
             view.setDiskResources(children);
         } else {
             treeLoader.load(folder);
         }
     }
 
     @Override
     public void onDiskResourceSelected(Set<DiskResource> selection) {
         if (selection != null && selection.size() == 1) {
             Iterator<DiskResource> it = selection.iterator();
             getDetails(it.next());
         } else {
             view.resetDetailsPanel();
         }
 
     }
 
     private void getDetails(DiskResource resource) {
         JSONObject obj = new JSONObject();
         JSONArray arr = new JSONArray();
         final String path = resource.getId();
         arr.set(0, new JSONString(path));
         obj.put("paths", arr);
         diskResourceService.getStat(obj.toString(), new GetDiskResourceDetailsCallback(this, path, drFactory));
 
     }
 
 //    @Override
 //    public void onFolderLoad(Folder loadedFolder, Set<DiskResource> folderChildren) {
 //        // FIXME JDS This method needs to go away. Instead, this action should be performed via a loadHandler.
 //        if ((getSelectedFolder() != null) && getSelectedFolder().equals(loadedFolder)) {
 //            view.setDiskResources(folderChildren);
 //        }
 //    }
 
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
                 parentFolder, view, newFolderName));
     }
 
     @Override
     public void doRefresh() {
         view.refreshFolder(getSelectedFolder());
     }
 
     @Override
     public void doSimpleDownload() {
         EventBus.getInstance().fireEvent(new RequestSimpleDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doBulkDownload() {
         EventBus.getInstance().fireEvent(new RequestBulkDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
     }
 
     @Override
     public void doRename(final DiskResource dr, final String newName) {
         view.mask(DISPLAY.loadingMask());
         diskResourceService.renameDiskResource(dr, newName, new RenameDiskResourceCallback(dr, view,
                 drFactory));
     }
 
     @Override
     public void doShare() {
         DataSharingDialog dlg = new DataSharingDialog(getSelectedDiskResources());
         dlg.show();
     }
 
     @Override
     public void requestDelete() {
         doDelete();
     }
 
     @Override
     public void doDelete() {
         if (!getSelectedDiskResources().isEmpty()
                 && DiskResourceUtil.isOwner(getSelectedDiskResources())) {
             view.mask(DISPLAY.loadingMask());
 
             HashSet<DiskResource> drSet = Sets.newHashSet(getSelectedDiskResources());
             diskResourceService.deleteDiskResources(drSet, new DiskResourceDeleteCallback(drSet, getSelectedFolder(), view));
 
         } else if ((getSelectedFolder() != null) && DiskResourceUtil.isOwner(getSelectedFolder())) {
             view.mask(DISPLAY.loadingMask());
             HashSet<DiskResource> drSet = Sets.newHashSet((DiskResource)getSelectedFolder());
             diskResourceService.deleteDiskResources(drSet, new DiskResourceDeleteCallback(drSet, getSelectedFolder(), view));
         }
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
 
 //    @Override
 //    public void setSelectedDiskResourcesById(Set<String> diskResourceIdList) {
 //
 //    }
 
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
         // Assuming that ownership is of no concern.
         for (DiskResource dr : dropData) {
             // if the resource is a direct child of target folder
             if (DiskResourceUtil.isChildOfFolder(targetFolder, dr)) {
                 return false;
             }
             if (dr instanceof Folder) {
 
                 // cannot drag an ancestor (parent, grandparent, etc) onto a child and/or descendant
                 if (DiskResourceUtil.isDescendantOfFolder(targetFolder, (Folder)dr)) {
                     return false;
                 }
 
             } else if (dr instanceof File) {
 
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
     public Folder getDropTargetFolder(IsWidget target, Element eventTargetElement) {
         Folder ret = null;
         if (view.isViewTree(target) && (view.findTreeNode(eventTargetElement) != null)) {
             TreeNode<Folder> targetTreeNode = view.findTreeNode(eventTargetElement);
             ret = targetTreeNode.getModel();
         } else if (view.isViewGrid(target)) {
             Element targetRow = view.findGridRow(target.asWidget().getElement()).cast();
 
             if (targetRow != null) {
                 int dropIndex = view.findRowIndex(targetRow);
                 // TODO JDS Do some type checking here. got errors last time for casting file to folder
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
         public Builder disableDiskResourceHyperlink() {
             presenter.getView().disableDiskResourceHyperlink();
             return this;
         }
 
     }
 
     @Override
     public void doSearch(final String val) {
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
                 AutoBean<Folder> folder = AutoBeanCodex.decode(drFactory, Folder.class, "{}");
                 Folder fo = folder.as();
                 fo.setId(ds.getId());
                 fo.setName(ds.getName());
                 fo.setPath(ds.getId());
                 return fo;
             }
 
             private File buildDummyFile(DataSearch ds) {
                 AutoBean<File> file = AutoBeanCodex.decode(drFactory, File.class, "{}");
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
             public void onSuccess(String result) {/* DO NOTHING */}
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
         diskResourceService.emptyTrash(UserInfo.getInstance().getUsername(),
                 new AsyncCallback<String>() {
 
                     @Override
                     public void onSuccess(String result) {
                         Folder f = view.getFolderById(UserInfo.getInstance().getTrashPath());
                         if (f != null) {
                             view.refreshFolder(f);
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(caught);
                     }
                 });
 
     }
 
     @Override
     public void restore() {
         Iterator<DiskResource> it = getSelectedDiskResources().iterator();
         JSONObject obj = new JSONObject();
         JSONArray pathArr = new JSONArray();
         int i = 0;
         while (it.hasNext()) {
             DiskResource r = it.next();
             pathArr.set(i++, new JSONString(r.getId()));
         }
         obj.put("paths", pathArr);
 
         diskResourceService.restoreDiskResource(obj, new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
 
             @Override
             public void onSuccess(String result) {
                 view.removeDiskResources(getSelectedDiskResources());
             }
         });
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
         final ToolButton btn = dlg.gelHelpToolButton();
         btn.addSelectHandler(new SelectHandler() {
             
             @Override
             public void onSelect(SelectEvent event) {
                 ContextualHelpPopup popup = new ContextualHelpPopup();
                 popup.add(new HTML(I18N.HELP.manageDataLinksHelp()));
                 popup.showAt(btn.getAbsoluteLeft(), btn.getAbsoluteTop() + 15);
                 
             }
         });
         dlg.show();
     }
 
 }
