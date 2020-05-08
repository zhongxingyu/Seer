 package org.iplantc.core.uidiskresource.client.views;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.DataCollapseStyle;
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.diskresources.Permissions;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uicommons.client.widgets.IPlantAnchor;
 import org.iplantc.core.uidiskresource.client.events.DataSearchHistorySelectedEvent;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceModelKeyProvider;
 import org.iplantc.core.uidiskresource.client.views.cells.DiskResourceNameCell;
 import org.iplantc.core.uidiskresource.client.views.widgets.DiskResourceViewToolbar;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.Style.SelectionMode;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.SortDir;
 import com.sencha.gxt.data.shared.Store.StoreSortInfo;
 import com.sencha.gxt.data.shared.TreeStore;
 import com.sencha.gxt.data.shared.loader.TreeLoader;
 import com.sencha.gxt.dnd.core.client.DND.Operation;
 import com.sencha.gxt.dnd.core.client.DragSource;
 import com.sencha.gxt.dnd.core.client.DropTarget;
 import com.sencha.gxt.theme.blue.client.status.BlueStatusAppearance.BlueStatusResources;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
 import com.sencha.gxt.widget.core.client.button.ToolButton;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.GridView;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 import com.sencha.gxt.widget.core.client.tree.Tree;
 import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;
 import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
 import com.sencha.gxt.widget.core.client.tree.TreeStyle;
 import com.sencha.gxt.widget.core.client.tree.TreeView;
 
 public class DiskResourceViewImpl implements DiskResourceView {
 
     private final class GridSelectionHandler implements SelectionChangedHandler<DiskResource> {
         @Override
         public void onSelectionChanged(SelectionChangedEvent<DiskResource> event) {
             if ((event.getSelection() != null) && !event.getSelection().isEmpty()) {
                 presenter.onDiskResourceSelected(Sets.newHashSet(event.getSelection()));
             } else {
                 resetDetailsPanel();
             }
         }
     }
 
     private final class TreeSelectionHandler implements SelectionHandler<Folder> {
         @Override
         public void onSelection(SelectionEvent<Folder> event) {
             if (DiskResourceViewImpl.this.widget.isAttached() && (event.getSelectedItem() != null)) {
                 onFolderSelected(event.getSelectedItem());
             }
         }
     }
 
     @UiTemplate("DiskResourceView.ui.xml")
     interface DiskResourceViewUiBinder extends UiBinder<Widget, DiskResourceViewImpl> {
     }
 
     private static DiskResourceViewUiBinder BINDER = GWT.create(DiskResourceViewUiBinder.class);
 
     private Presenter presenter;
 
     @UiField
     DiskResourceViewToolbar toolbar;
 
     @UiField
     BorderLayoutContainer con;
 
     @UiField
     ContentPanel westPanel;
 
     @UiField(provided = true)
     Tree<Folder, String> tree;
 
     @UiField(provided = true)
     final TreeStore<Folder> treeStore;
 
     @UiField
     ContentPanel centerPanel;
 
     @UiField
     Grid<DiskResource> grid;
 
     @UiField
     ColumnModel<DiskResource> cm;
 
     @UiField
     ListStore<DiskResource> listStore;
 
     @UiField
     GridView<DiskResource> gridView;
 
     @UiField
     VerticalLayoutContainer detailsPanel;
 
     // TODO temp. remove search
     // @UiField
     // ContentPanel historyPanel;
 
     @UiField
     BorderLayoutData westData;
     @UiField
     BorderLayoutData centerData;
     @UiField
     BorderLayoutData eastData;
     @UiField
     BorderLayoutData northData;
     @UiField
     BorderLayoutData southData;
 
     private final Widget widget;
 
     private TreeLoader<Folder> treeLoader;
 
     private final CheckBoxSelectionModel<DiskResource> sm;
 
     @Inject
     public DiskResourceViewImpl(final Tree<Folder, String> tree) {
         this.tree = tree;
         this.treeStore = tree.getStore();
         // KLUDGE GXT 3.0.1 hasn't implemented the tree loading icon, so we'll
         // use the one from Status.
         tree.setView(new TreeView<Folder>() {
             private final BlueStatusResources resources = GWT.create(BlueStatusResources.class);
 
             @Override
             public void onLoading(TreeNode<Folder> node) {
                 onIconStyleChange(node, resources.loading());
 
                 // Does nothing in GXT 3.0.1, but call it in case of any future version updates.
                 super.onLoading(node);
             }
         });
 
         sm = new CheckBoxSelectionModel<DiskResource>(new IdentityValueProvider<DiskResource>());
 
         widget = BINDER.createAndBindUi(this);
 
         detailsPanel.setScrollMode(ScrollMode.AUTO);
 
         grid.setSelectionModel(sm);
         ColumnConfig<DiskResource, DiskResource> name = getDiskResourceColumnModel().getNameColumn();
         grid.getStore().addSortInfo(
                 new StoreSortInfo<DiskResource>(name.getValueProvider(), name.getComparator(),
                         SortDir.ASC));
 
         // Set Leaf icon to a folder
         TreeStyle treeStyle = tree.getStyle();
         TreeAppearance appearance = tree.getAppearance();
         treeStyle.setLeafIcon(appearance.closeNodeIcon());
         tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         tree.getSelectionModel().addSelectionHandler(new TreeSelectionHandler());
 
         grid.getSelectionModel().addSelectionChangedHandler(new GridSelectionHandler());
 
         // by default no details to show...
         resetDetailsPanel();
         setGridEmptyText();
         addTreeCollapseButton();
     }
 
     private void addTreeCollapseButton() {
         westPanel.setCollapsible(false);
         DataCollapseStyle style = IplantResources.RESOURCES.getDataCollapseStyle();
         style.ensureInjected();
         ToolButton tool = new ToolButton(new IconConfig(style.collapse(), style.collapseHover()));
         tool.setId("idTreeCollapse");
         tool.setToolTip(I18N.DISPLAY.collapseAll());
         tool.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 tree.collapseAll();
             }
         });
         westPanel.getHeader().removeTool(westPanel.getHeader().getTool(0));
         westPanel.getHeader().addTool(tool);
     }
 
     @Override
     public void onDiskResourceSelected(Set<DiskResource> selection) {
         onDiskResourceSelected(selection);
     }
 
     @Override
     public void onFolderSelected(Folder folder) {
         presenter.onFolderSelected(folder);
     }
 
     @UiFactory
     ListStore<DiskResource> createListStore() {
         DiskResourceModelKeyProvider keyProvider = new DiskResourceModelKeyProvider();
         ListStore<DiskResource> listStore2 = new ListStore<DiskResource>(keyProvider);
 
         return listStore2;
     }
 
     @UiFactory
     public ValueProvider<Folder, String> createValueProvider() {
         return new ValueProvider<Folder, String>() {
 
             @Override
             public String getValue(Folder object) {
                 return object.getName();
             }
 
             @Override
             public void setValue(Folder object, String value) {
             }
 
             @Override
             public String getPath() {
                 return "name"; //$NON-NLS-1$
             }
         };
     }
 
     @UiFactory
     ColumnModel<DiskResource> createColumnModel() {
         return new DiskResourceColumnModel(this, sm);
     }
 
     private DiskResourceColumnModel getDiskResourceColumnModel() {
         return (DiskResourceColumnModel)cm;
     }
 
     @Override
     public void setPresenter(Presenter presenter) {
         this.presenter = presenter;
         toolbar.setPresenter(presenter);
         initDragAndDrop();
     }
 
     private void initDragAndDrop() {
         DiskResourceViewDnDHandler dndHandler = new DiskResourceViewDnDHandler(presenter);
 
         DropTarget gridDropTarget = new DropTarget(grid);
         gridDropTarget.setAllowSelfAsSource(true);
         gridDropTarget.setOperation(Operation.COPY);
         gridDropTarget.addDragEnterHandler(dndHandler);
         gridDropTarget.addDragMoveHandler(dndHandler);
         gridDropTarget.addDropHandler(dndHandler);
 
         DragSource gridDragSource = new DragSource(grid);
         gridDragSource.addDragStartHandler(dndHandler);
 
         DropTarget treeDropTarget = new DropTarget(tree);
         treeDropTarget.setAllowSelfAsSource(true);
         treeDropTarget.setOperation(Operation.COPY);
         treeDropTarget.addDragEnterHandler(dndHandler);
         treeDropTarget.addDragMoveHandler(dndHandler);
         treeDropTarget.addDropHandler(dndHandler);
 
         DragSource treeDragSource = new DragSource(tree);
         treeDragSource.addDragStartHandler(dndHandler);
 
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     @Override
     public void setTreeLoader(TreeLoader<Folder> treeLoader) {
         tree.setLoader(treeLoader);
         this.treeLoader = treeLoader;
     }
 
     @Override
     public Folder getSelectedFolder() {
         return tree.getSelectionModel().getSelectedItem();
     }
 
     @Override
     public Set<DiskResource> getSelectedDiskResources() {
         return Sets.newHashSet(grid.getSelectionModel().getSelectedItems());
     }
 
     @Override
     public TreeStore<Folder> getTreeStore() {
         return treeStore;
     }
 
     @Override
     public ListStore<DiskResource> getListStore() {
         return listStore;
     }
 
     @Override
     public boolean isLoaded(Folder folder) {
         TreeNode<Folder> findNode = tree.findNode(folder);
         return findNode.isLoaded();
     }
 
     @Override
     public void setDiskResources(Set<DiskResource> folderChildren) {
         grid.getStore().clear();
         grid.getStore().addAll(folderChildren);
     }
 
     @Override
     public void setWestWidgetHidden(boolean hideWestWidget) {
         westData.setHidden(hideWestWidget);
     }
 
     @Override
     public void setCenterWidgetHidden(boolean hideCenterWidget) {
         // If we are hiding the center widget, update west data to fill
         // available space.
         if (hideCenterWidget) {
             westData.setSize(1);
         }
         centerData.setHidden(hideCenterWidget);
     }
 
     @Override
     public void setEastWidgetHidden(boolean hideEastWidget) {
         eastData.setHidden(hideEastWidget);
     }
 
     @Override
     public void setNorthWidgetHidden(boolean hideNorthWidget) {
         northData.setHidden(hideNorthWidget);
     }
 
     @Override
     public void setSouthWidget(IsWidget widget) {
         southData.setHidden(false);
         con.setSouthWidget(widget, southData);
     }
 
     @Override
     public void setSouthWidget(IsWidget widget, double size) {
         southData.setHidden(false);
         southData.setSize(size);
         con.setSouthWidget(widget, southData);
     }
 
     @Override
     public void addDiskResourceSelectChangedHandler(
             SelectionChangedHandler<DiskResource> selectionChangedHandler) {
         grid.getSelectionModel().addSelectionChangedHandler(selectionChangedHandler);
     }
 
     @Override
     public void addFolderSelectionHandler(SelectionHandler<Folder> selectionHandler) {
         tree.getSelectionModel().addSelectionHandler(selectionHandler);
     }
 
     @Override
     public void setSelectedFolder(Folder folder) {
 
         final Folder findModelWithKey = treeStore.findModelWithKey(folder.getId());
         if (findModelWithKey != null) {
             showDataListingWidget();
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                 @Override
                 public void execute() {
                     tree.getSelectionModel().setSelection(Lists.newArrayList(findModelWithKey));
                     tree.scrollIntoView(findModelWithKey);
                 }
             });
         }
     }
 
     @Override
     public void setSelectedDiskResources(List<HasId> diskResourcesToSelect) {
         List<DiskResource> resourcesToSelect = Lists.newArrayList();
         for (HasId hi : diskResourcesToSelect) {
             DiskResource findModelWithKey = listStore.findModelWithKey(hi.getId());
             if (findModelWithKey != null) {
                 resourcesToSelect.add(findModelWithKey);
             }
         }
         grid.getSelectionModel().select(resourcesToSelect, false);
     }
 
     @Override
     public void addFolder(Folder parent, Folder newChild) {
         treeStore.add(parent, newChild);
         listStore.add(newChild);
     }
 
     @Override
     public Folder getFolderById(String folderId) {
         return treeStore.findModelWithKey(folderId);
     }
 
     @Override
     public void expandFolder(Folder folder) {
         tree.setExpanded(folder, true);
     }
 
     @Override
     public void deSelectDiskResources() {
         grid.getSelectionModel().deselectAll();
     }
 
     @Override
     public void deSelectNavigationFolder() {
         tree.getSelectionModel().deselectAll();
     }
 
     @Override
     public void refreshFolder(Folder folder) {
         if (folder == null || treeStore.findModel(folder) == null) {
             return;
         }
 
         removeChildren(folder);
         treeLoader.load(folder);
     }
 
     @Override
     public void removeChildren(Folder folder) {
         if (folder == null || treeStore.findModel(folder) == null) {
             return;
         }
 
         treeStore.removeChildren(folder);
     }
 
     @Override
     public DiskResourceViewToolbar getToolbar() {
         return toolbar;
     }
 
     @Override
     public void mask(String loadingMask) {
         con.mask(loadingMask);
     }
 
     @Override
     public void unmask() {
         con.unmask();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <D extends DiskResource> void removeDiskResources(Collection<D> resources) {
         // De-select everything first, so the remove calls don't trigger extra
         // SelectionChanged events.
         grid.getSelectionModel().deselect((List<DiskResource>)Lists.newArrayList(resources));
 
         for (DiskResource dr : resources) {
             listStore.remove(dr);
             if (dr instanceof Folder) {
                 treeStore.remove((Folder)dr);
             }
         }
     }
 
     private void setGridEmptyText() {
         gridView.setEmptyText(I18N.DISPLAY.noItemsToDisplay());
     }
 
     @Override
     public void updateDiskResource(DiskResource originalDr, DiskResource newDr) {
         // Check each store for for existence of original disk resource
         Folder treeStoreModel = treeStore.findModelWithKey(originalDr.getId());
         if (treeStoreModel != null) {
 
             // Grab original disk resource's parent, then remove original from
             // tree store
             Folder parentFolder = treeStore.getParent(treeStoreModel);
             treeStore.remove(treeStoreModel);
 
             treeStoreModel.setId(newDr.getId());
             treeStoreModel.setName(newDr.getName());
             treeStore.add(parentFolder, treeStoreModel);
         }
 
         DiskResource listStoreModel = listStore.findModelWithKey(originalDr.getId());
 
         if (listStoreModel != null) {
             listStore.remove(listStoreModel);
             if (listStoreModel instanceof File) {
                 listStore.add(newDr);
             } else {
                 listStore.add(newDr);
             }
         }
 
     }
 
     @Override
     public boolean isViewTree(IsWidget widget) {
         return widget.asWidget() == tree;
     }
 
     @Override
     public boolean isViewGrid(IsWidget widget) {
         return widget.asWidget() == grid;
     }
 
     @Override
     public TreeNode<Folder> findTreeNode(Element el) {
         return tree.findNode(el);
     }
 
     @Override
     public Element findGridRow(Element el) {
         return grid.getView().findRow(el);
     }
 
     @Override
     public int findRowIndex(Element targetRow) {
         return grid.getView().findRowIndex(targetRow);
     }
 
     @Override
     public void setSingleSelect() {
         grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         // Hide the checkbox column
         getDiskResourceColumnModel().setCheckboxColumnHidden(true);
     }
 
     @Override
     public void disableFilePreview() {
         Cell<DiskResource> cell = getDiskResourceColumnModel().getNameColumn().getCell();
         if (cell instanceof DiskResourceNameCell) {
             ((DiskResourceNameCell)cell).setPreviewEnabled(false);
         }
 
     }
 
     @Override
     public void showDataListingWidget() {
         if (!grid.isAttached()) {
             centerPanel.clear();
             centerPanel.add(grid, centerData);
             // reset search
             presenter.setCurrentSearchTerm(null);
             toolbar.clearSearchTerm();
         }
     }
 
     @Override
     public void showSearchResultWidget(IsWidget w) {
         if (!w.asWidget().isAttached()) {
             w.asWidget().setHeight(centerPanel.getOffsetHeight(true) + "px"); //$NON-NLS-1$
             centerPanel.clear();
             centerPanel.add(w.asWidget(), centerData);
         }
     }
 
     @Override
     public void resetDetailsPanel() {
         detailsPanel.clear();
         FieldLabel fl = new FieldLabel();
         fl.setLabelWidth(detailsPanel.getOffsetWidth(true) - 10);
         fl.setLabelSeparator(""); //$NON-NLS-1$
        fl.setHTML(getDetailAsHtml(I18N.DISPLAY.noDetails(), false)); //$NON-NLS-1$
         HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(2);
         hp.add(fl);
         detailsPanel.add(hp);
     }
 
     private String getDetailAsHtml(String detail, boolean bolded) {
         if (bolded) {
             return "<span style='font-size:10px'><b>" + detail + "</b> </span>"; //$NON-NLS-1$ //$NON-NLS-2$
         } else {
            return "<span style='font-size:10px;'>" + detail + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
 
     /**
      * Parses a timestamp string into a formatted date string and adds it to this panel.
      * 
      * @param label
      * @param value
      */
     private HorizontalPanel getDateLabel(String label, Date date) {
         String value = ""; //$NON-NLS-1$
 
         if (date != null) {
             DateTimeFormat formatter = DateTimeFormat
                     .getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
 
             value = formatter.format(date);
         }
 
         return getStringLabel(label, value);
 
     }
 
     private HorizontalPanel getStringLabel(String label, String value) {
         HorizontalPanel panel = buildRow();
         FieldLabel fl = new FieldLabel();
         fl.setHTML(getDetailAsHtml(label, true));
         panel.add(fl);
 
         FieldLabel fv = new FieldLabel();
         fv.setLabelSeparator(""); //$NON-NLS-1$
         fv.setHTML(getDetailAsHtml(value + "", false)); //$NON-NLS-1$
         panel.add(fv);
 
         return panel;
     }
 
     private HorizontalPanel getDirFileCount(String label, int file_count, int dir_count) {
         return getStringLabel(label, file_count + " / " + dir_count); //$NON-NLS-1$
     }
 
     /**
      * Add permissions detail
      * 
      */
     private HorizontalPanel getPermissionsLabel(String label, Permissions p) {
         String value;
         if (p.isOwner()) {
             value = I18N.DISPLAY.owner();
         } else if (!p.isWritable()) {
             value = I18N.DISPLAY.readOnly();
         } else {
             value = I18N.DISPLAY.readWrite();
         }
 
         return getStringLabel(label, value);
     }
 
     private HorizontalPanel buildRow() {
         HorizontalPanel panel = new HorizontalPanel();
         panel.setHeight("25px"); //$NON-NLS-1$
         panel.setSpacing(1);
         return panel;
     }
 
     private IPlantAnchor buildSearchHistoryLink(final String searchTerm) {
         IPlantAnchor anchor = new IPlantAnchor(searchTerm, 120, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 DataSearchHistorySelectedEvent dsh = new DataSearchHistorySelectedEvent(searchTerm);
                 EventBus.getInstance().fireEvent(dsh);
 
             }
         });
 
         return anchor;
     }
 
     @Override
     public void renderSearchHistory(List<String> history) {
         final VerticalLayoutContainer vlc = new VerticalLayoutContainer();
         vlc.setScrollMode(ScrollMode.AUTOY);
         // TODO temp. remove search
         // historyPanel.clear();
         // historyPanel.setWidget(vlc);
         if (history != null && history.size() > 0) {
             for (final String term : history) {
                 IPlantAnchor link = buildSearchHistoryLink(term);
                 final HorizontalPanel hp = buildRow();
                 hp.sinkEvents(Event.ONMOUSEOVER);
                 hp.sinkEvents(Event.ONMOUSEOUT);
                 final Image closeImg = new Image(IplantResources.RESOURCES.close());
                 addDomHandlers(hp, closeImg);
                 closeImg.addClickHandler(new ClickHandler() {
 
                     @Override
                     public void onClick(ClickEvent event) {
                         vlc.remove(hp);
                         presenter.removeFromSearchHistory(term);
                     }
                 });
                 hp.add(link);
                 closeImg.setVisible(false);
                 hp.add(closeImg);
                 vlc.add(hp);
             }
         }
         // KLUDGE:
         // TODO temp. remove search
         // historyPanel.forceLayout();
     }
 
     private void addDomHandlers(HorizontalPanel hp, final Image closeImg) {
         hp.addDomHandler(new MouseOverHandler() {
 
             @Override
             public void onMouseOver(MouseOverEvent event) {
                 closeImg.setVisible(true);
 
             }
         }, MouseOverEvent.getType());
 
         hp.addDomHandler(new MouseOutHandler() {
 
             @Override
             public void onMouseOut(MouseOutEvent event) {
                 closeImg.setVisible(false);
 
             }
         }, MouseOutEvent.getType());
     }
 
     @Override
     public boolean isRoot(DiskResource dr) {
         if (!(dr instanceof Folder))
             return false;
 
         for (Folder f : treeStore.getRootItems()) {
             if (f.getId().equalsIgnoreCase(dr.getId())) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean isCenterHidden() {
         return centerData.isHidden();
     }
 
     @Override
     public void updateDetails(String path, DiskResourceInfo info) {
         detailsPanel.clear();
         Set<DiskResource> selection = getSelectedDiskResources();
         // gaurd race condition
         if (selection != null && selection.size() == 1) {
             Iterator<DiskResource> it = selection.iterator();
             DiskResource next = it.next();
             if (next.getId().equals(path)) {
                 detailsPanel
                         .add(getDateLabel(I18N.DISPLAY.lastModified(), new Date(info.getModified())));
                 detailsPanel.add(getDateLabel(I18N.DISPLAY.createdDate(), new Date(info.getCreated())));
                 detailsPanel.add(getPermissionsLabel(I18N.DISPLAY.permissions(), info.getPermissions()));
                 if (!DiskResourceUtil.inTrash(next)) {
                     detailsPanel.add(getSharingLabel(I18N.DISPLAY.share(), info.getShareCount(),
                             info.getPermissions()));
                 }
                 if (info.getType().equalsIgnoreCase("file")) {
                     addFileDetails(info);
 
                 } else {
                     addFolderDetails(info);
                 }
             }
 
         }
     }
 
     private void addFolderDetails(DiskResourceInfo info) {
         detailsPanel.add(getDirFileCount(I18N.DISPLAY.files() + " / " + I18N.DISPLAY.folders(), //$NON-NLS-1$
                 info.getFileCount(), info.getDirCount()));
     }
 
     private void addFileDetails(DiskResourceInfo info) {
         detailsPanel.add(getStringLabel(I18N.DISPLAY.size(),
                 DiskResourceUtil.formatFileSize(info.getSize() + ""))); //$NON-NLS-1$
         detailsPanel.add(getStringLabel("Type", info.getFileType()));
         detailsPanel.add(getInfoTypeLabel("Info-Type", info));
     }
 
     private HorizontalPanel getSharingLabel(String label, int shareCount, Permissions permissions) {
         IPlantAnchor link = null;
         HorizontalPanel panel = buildRow();
         FieldLabel fl = new FieldLabel();
         fl.setHTML(getDetailAsHtml(label, true));
         panel.add(fl);
         if (permissions.isOwner()) {
             if (shareCount == 0) {
                 link = new IPlantAnchor(I18N.DISPLAY.nosharing(), 100, new SharingLabelClickHandler());
             } else {
                 link = new IPlantAnchor("" + shareCount, 100, new SharingLabelClickHandler()); //$NON-NLS-1$
             }
             panel.add(link);
         } else {
             panel.add(new HTML("-"));
         }
 
         return panel;
     }
 
     private HorizontalPanel getInfoTypeLabel(String label, DiskResourceInfo info) {
         IPlantAnchor link = null;
         HorizontalPanel panel = buildRow();
         FieldLabel fl = new FieldLabel();
         fl.setHTML(getDetailAsHtml(label, true));
         panel.add(fl);
         String infoType = info.getInfoType();
         if (infoType != null && !infoType.isEmpty()) {
             link = new IPlantAnchor(infoType, 100, new InfoTypeClickHandler(infoType));
         } else {
             link = new IPlantAnchor("Select", 100, new InfoTypeClickHandler(""));
         }
         panel.add(link);
         return panel;
     }
 
     @Override
     public void maskDetailsPanel() {
         detailsPanel.mask(I18N.DISPLAY.loadingMask());
     }
 
     @Override
     public void unmaskDetailsPanel() {
         detailsPanel.unmask();
     }
 
     private class InfoTypeClickHandler implements ClickHandler {
 
         private final String infoType;
 
         public InfoTypeClickHandler(String type) {
             this.infoType = type;
         }
 
         @Override
         public void onClick(ClickEvent arg0) {
             Set<DiskResource> selection = getSelectedDiskResources();
             Iterator<DiskResource> it = selection.iterator();
             presenter.OnInfoTypeClick(it.next().getId(), infoType);
         }
 
     }
 
     private class SharingLabelClickHandler implements ClickHandler {
         @Override
         public void onClick(ClickEvent event) {
             presenter.doShare();
 
         }
     }
 
 }
