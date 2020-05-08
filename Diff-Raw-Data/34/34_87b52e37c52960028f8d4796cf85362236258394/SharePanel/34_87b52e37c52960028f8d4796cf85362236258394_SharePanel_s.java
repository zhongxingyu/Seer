 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.models.DataSharing;
 import org.iplantc.de.client.models.DataSharing.TYPE;
 import org.iplantc.de.client.models.Sharing;
 
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.dnd.DND.Operation;
 import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;
 import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
 import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid.TreeNode;
 import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
 import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * A panel to display a list of sharee and their permissions
  * 
  * @author sriram
  * 
  */
 public class SharePanel extends ContentPanel {
     private static final String ID_PERM_GROUP = "idPermGroup";
     private EditorTreeGrid<Sharing> grid;
     private FastMap<List<Sharing>> unshareList;
     private List<DiskResource> resources;
     private ToolBar toolbar;
     private static final String ID_BTN_REMOVE = "idBtnRemove";
     private FastMap<List<Sharing>> originalList;
 
     public SharePanel(List<DiskResource> resources) {
         unshareList = new FastMap<List<Sharing>>();
         this.resources = resources;
         init();
     }
 
     private void init() {
         setCollapsible(true);
         setLayout(new FitLayout());
         TreeStore<Sharing> store = new TreeStore<Sharing>();
         store.setKeyProvider(new ModelKeyProvider<Sharing>() {
             @Override
             public String getKey(Sharing model) {
                 return model.getKey();
             }
         });
         ColumnModel cm = buildColumnModel();
         initGrid(store, cm);
 
         add(grid);
         addToolBar();
         TreeGridDropTarget sgdt = new ShareTreeGridDropTargetImpl(grid);
 
         sgdt.setOperation(Operation.COPY);
         sgdt.setAllowDropOnLeaf(false);
         sgdt.setAutoExpand(true);
     }
 
     private void initGrid(TreeStore<Sharing> store, ColumnModel cm) {
         grid = new EditorTreeGrid<Sharing>(store, cm);
 
         grid.getView().setEmptyText(org.iplantc.de.client.I18N.DISPLAY.sharePanelEmptyText());
         grid.setSelectionModel(new TreeGridSelectionModel<Sharing>());
         grid.setClicksToEdit(ClicksToEdit.ONE);
         TreeGridSelectionModel<Sharing> sm = (TreeGridSelectionModel<Sharing>)grid.getSelectionModel();
 
         sm.addListener(Events.SelectionChange, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (grid.getSelectionModel().getSelectedItems().size() > 0) {
                     toolbar.getItemByItemId(ID_BTN_REMOVE).enable();
                     toolbar.getItemByItemId(ID_PERM_GROUP).enable();
                 } else {
                     toolbar.getItemByItemId(ID_BTN_REMOVE).disable();
                     toolbar.getItemByItemId(ID_PERM_GROUP).disable();
                 }
 
             }
         });
         setIcons();
     }
 
     private void setIcons() {
         grid.setIconProvider(new ModelIconProvider<Sharing>() {
 
             @Override
             public AbstractImagePrototype getIcon(Sharing model) {
                 if (model instanceof DataSharing) {
                     DataSharing ds = (DataSharing)model;
                     TYPE type = getSharingResourceType(ds.getPath());
                     if (type == null) {
                         return AbstractImagePrototype.create(Resources.ICONS.share());
                     }
                     if (type.equals(TYPE.FOLDER)) {
                         return AbstractImagePrototype.create(Resources.ICONS.folder());
                     } else {
                         return AbstractImagePrototype.create(Resources.ICONS.file());
                     }
 
                 } else {
                     return AbstractImagePrototype.create(Resources.ICONS.share());
                 }
             }
         });
 
     }
 
     private TYPE getSharingResourceType(String path) {
         for (DiskResource dr : resources) {
             if (dr.getId().equalsIgnoreCase(path)) {
                 if (dr instanceof Folder) {
                     return TYPE.FOLDER;
                 } else {
                     return TYPE.FILE;
                 }
             }
         }
 
         return null;
     }
 
     private void initUpdateListeners() {
         grid.addListener(Events.BeforeEdit, new Listener<GridEvent<Sharing>>() {
             @Override
             public void handleEvent(GridEvent<Sharing> be) {
                 if (!(be.getRecord().getModel() instanceof DataSharing)) {
                     be.setCancelled(true);
                 }
             }
         });
 
         grid.addListener(Events.AfterEdit, new Listener<GridEvent<Sharing>>() {
             @Override
             public void handleEvent(GridEvent<Sharing> be) {
                 // be.setCancelled(true);
                 // edited row can only be of instance DataSharing
                 DataSharing ds = (DataSharing)be.getRecord().getModel();
                 updatePermissions(be.getValue().toString(), ds);
             }
         });
     }
 
     private void addToolBar() {
         toolbar = new ToolBar();
         Button removeBtn = buildUnshareButton();
         toolbar.add(removeBtn);
         toolbar.add(new FillToolItem());
         SimpleComboBox<String> permissionsCombo = buildPermissionsCombo();
         permissionsCombo.setEmptyText(I18N.DISPLAY.permissions());
         permissionsCombo.disable();
         permissionsCombo.addListener(Events.Select, new PermissionsChangeListenerImpl());
         toolbar.add(permissionsCombo);
 
         setTopComponent(toolbar);
     }
 
     private Button buildUnshareButton() {
         Button removeBtn = new Button(org.iplantc.de.client.I18N.DISPLAY.unshare(),
                 AbstractImagePrototype.create(Resources.ICONS.deleteIcon()));
         removeBtn.setId(ID_BTN_REMOVE);
         removeBtn.addSelectionListener(new RemoveButtonSelectionListener());
         removeBtn.disable();
         return removeBtn;
     }
 
     public void loadSharingData(List<Sharing> roots, FastMap<List<Sharing>> sharingMap) {
         originalList = new FastMap<List<Sharing>>();
         TreeStore<Sharing> treeStore = grid.getTreeStore();
         treeStore.removeAll();
         for (Sharing s : roots) {
             treeStore.add(s, false);
             String userName = s.getUserName();
             List<Sharing> list = sharingMap.get(userName);
             List<Sharing> newList = new ArrayList<Sharing>();
             if (list != null) {
                 for (Sharing item : list) {
                     treeStore.add(s, item, false);
                     newList.add(item.copy());
                 }
                 originalList.put(userName, newList);
             }
         }
 
         initUpdateListeners();
         grid.expandAll();
     }
 
     public void addDataSharing(FastMap<DataSharing> sharingMap) {
         TreeStore<Sharing> treeStore = grid.getTreeStore();
         if (sharingMap != null) {
             for (DataSharing s : sharingMap.values()) {
                 Sharing find = new Sharing(s.getCollaborator());
                Sharing exits = treeStore.findModel(find);
                if (exits == null) {
                     treeStore.add(find, false);
                    exits = find;
                 }
                treeStore.add(exits, s, false);
             }
             grid.expandAll();
         }
 
     }
 
     private void addSharing(Sharing obj) {
         TreeStore<Sharing> treeStore = grid.getTreeStore();
         if (!treeStore.contains(obj)) {
             treeStore.add(obj, true);
             grid.setLeaf(obj, false);
         }
         grid.getSelectionModel().select(false, obj);
     }
 
     private ColumnModel buildColumnModel() {
         ColumnConfig sharee = new ColumnConfig(Sharing.NAME, I18N.DISPLAY.name(), 170);
         sharee.setRenderer(new TreeGridCellRenderer<Sharing>());
         ColumnConfig permissions = new ColumnConfig(DataSharing.DISPLAY_PERMISSION,
                 org.iplantc.de.client.I18N.DISPLAY.permissions(), 100);
         permissions.setEditor(buildPermissionsEditor());
         sharee.setMenuDisabled(true);
         sharee.setSortable(true);
         return new ColumnModel(Arrays.asList(sharee, permissions));
     }
 
     private CellEditor buildPermissionsEditor() {
         final SimpleComboBox<String> combo = buildPermissionsCombo();
 
         CellEditor editor = new CellEditor(combo) {
             @Override
             public Object preProcessValue(Object value) {
                 if (value == null) {
                     return value;
                 }
                 return combo.findModel(value.toString());
             }
 
             @Override
             public Object postProcessValue(Object value) {
                 if (value == null) {
                     return value;
                 }
                 return ((ModelData)value).get("value");
             }
         };
 
         return editor;
 
     }
 
     private SimpleComboBox<String> buildPermissionsCombo() {
         final SimpleComboBox<String> combo = new SimpleComboBox<String>();
         combo.setId(ID_PERM_GROUP);
         combo.setForceSelection(true);
         combo.add(org.iplantc.de.client.I18N.DISPLAY.read());
         combo.add(org.iplantc.de.client.I18N.DISPLAY.write());
         combo.add(org.iplantc.de.client.I18N.DISPLAY.own());
         combo.setEditable(false);
 
         combo.setTriggerAction(TriggerAction.ALL);
         return combo;
     }
 
     /**
      * 
      * 
      * @return the sharing list
      */
     public FastMap<List<Sharing>> getSharingMap() {
         FastMap<List<Sharing>> sharingList = new FastMap<List<Sharing>>();
         for (Sharing s : grid.getTreeStore().getModels()) {
             if (!(s instanceof DataSharing)) {
                 List<Sharing> childrens = grid.getTreeStore().getChildren(s);
                 List<Sharing> updatedSharingList = getUpdatedSharingList(s.getUserName(), childrens);
                 if (updatedSharingList != null && updatedSharingList.size() > 0) {
                     sharingList.put(s.getUserName(), updatedSharingList);
                 }
             }
         }
         return sharingList;
     }
 
     /**
      * check the list with original to see if things have changed. ignore unchanged records
      * 
      * @param userName
      * @param list
      * @return
      */
     private List<Sharing> getUpdatedSharingList(String userName, List<Sharing> list) {
         List<Sharing> updateList = new ArrayList<Sharing>();
         if (list != null && userName != null) {
             List<Sharing> fromOriginal = originalList.get(userName);
             if (fromOriginal == null || fromOriginal.isEmpty()) {
                 updateList = list;
             } else {
                 for (Sharing s : list) {
                     if (!fromOriginal.contains(s)) {
                         updateList.add(s);
                     }
                 }
 
             }
 
         }
 
         return updateList;
     }
 
     /**
      * check if a sharing recored originally existed. Needed to remove false submission of unshare list
      * 
      * @return
      */
     private boolean isExistedOriginally(Sharing s) {
         String userName = s.getUserName();
         List<Sharing> fromOriginal = originalList.get(userName);
         if (fromOriginal != null && fromOriginal.contains(s)) {
             return true;
         } else {
             return false;
         }
 
     }
 
     /**
      * @return the unshareList
      */
     public FastMap<List<Sharing>> getUnshareList() {
         return unshareList;
     }
 
     private final class PermissionsChangeListenerImpl implements Listener<FieldEvent> {
         @Override
         public void handleEvent(FieldEvent be) {
             List<Sharing> items = grid.getSelectionModel().getSelectedItems();
             if (items != null) {
                 for (Sharing s : items) {
                     @SuppressWarnings("unchecked")
                     SimpleComboBox<String> perm = (SimpleComboBox<String>)be.getField();
                     SimpleComboValue<String> value = perm.getValue();
                     if (value != null) {
                         if (s instanceof DataSharing) {
                             updatePermissions(value.getValue(), (DataSharing)s);
                         } else {
                             TreeStore<Sharing> treeStore = grid.getTreeStore();
                             Sharing sharing = treeStore.findModel(s);
                             if (sharing != null) {
                                 List<Sharing> models = treeStore.getChildren(sharing);
                                 if (models != null) {
                                     for (Sharing md : models) {
                                         updatePermissions(value.getValue(), (DataSharing)md);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
 
         }
     }
 
     private final class ShareTreeGridDropTargetImpl extends TreeGridDropTarget {
         private ShareTreeGridDropTargetImpl(@SuppressWarnings("rawtypes") TreeGrid tree) {
             super(tree);
         }
 
         @Override
         public void onDragMove(DNDEvent e) {
             super.onDragMove(e);
             @SuppressWarnings("rawtypes")
             TreeNode tn = grid.findNode(e.getTarget());
             setDropFeedback(e, tn);
         }
 
         @Override
         public void onDragEnter(DNDEvent e) {
             super.onDragEnter(e);
             @SuppressWarnings("rawtypes")
             TreeNode tn = grid.findNode(e.getTarget());
             setDropFeedback(e, tn);

         }
 
         @Override
         public void onDragDrop(DNDEvent e) {
             List<?> items = e.getData();
             if (items != null) {
                 if (items.get(0) instanceof Collaborator) {
                     for (Object coll : items) {
                         addSharing(new Sharing((Collaborator)coll));
                     }
                     return;
                 }
             }
 
             @SuppressWarnings("rawtypes")
             TreeNode tn = grid.findNode(e.getTarget());
             Sharing s = (Sharing)tn.getModel();
             for (Object dr : items) {
                 DataSharing ds = new DataSharing(s.getCollaborator(),
                         new Permissions(true, false, false), ((DiskResource)dr).getId());
                 grid.getTreeStore().add(s, ds, false);
             }
         }
     }
 
     private void updatePermissions(String perm, DataSharing model) {
         if (perm.equals(org.iplantc.de.client.I18N.DISPLAY.read())) {
             model.setReadable(true);
         } else if (perm.equals(org.iplantc.de.client.I18N.DISPLAY.write())) {
             model.setWritable(true);
         } else {
             model.setOwner(true);
         }
         grid.getTreeStore().update(model);
     }
 
     private class RemoveButtonSelectionListener extends SelectionListener<ButtonEvent> {
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             List<Sharing> models = grid.getSelectionModel().getSelectedItems();
             removeModels(models);
         }
     }
 
     private void removeModels(List<Sharing> models) {
         // prepared unshared list here
         TreeStore<Sharing> store = grid.getTreeStore();
         for (Sharing model : models) {
             String userName = model.getUserName();
             List<Sharing> list = unshareList.get(userName);
             if (list == null) {
                 list = new ArrayList<Sharing>();
             }
            if (model instanceof DataSharing && isExistedOriginally(model)) {
                list.add(model);
                 store.remove(model);
             } else {
                 Sharing sharing = store.findModel(model);
                 if (sharing != null) {
                     List<Sharing> removeList = store.getChildren(sharing);
                     for (Sharing remItem : removeList) {
                         if (isExistedOriginally(remItem)) {
                             list.add(remItem);
                         }
                     }
                     store.removeAll(sharing);
                     store.remove(sharing);
                 }
             }
             if (!list.isEmpty()) {
                 unshareList.put(userName, list);
             }
         }
     }
 
     private boolean canShare(Collaborator c, String path) {
         TreeStore<Sharing> store = grid.getTreeStore();
         DataSharing s = new DataSharing(c, new Permissions(true, false, false), path);
         if (!store.contains(s)
                 && (!c.getUserName().equalsIgnoreCase(UserInfo.getInstance().getUsername()))) {
             return true;
         }
 
         return false;
     }
 
     private void setDNDFeedback(DNDEvent e, boolean feedback) {
         e.getStatus().setStatus(feedback);
         e.setCancelled(!feedback);
     }
 
     private void setDropFeedback(DNDEvent e, @SuppressWarnings("rawtypes") TreeNode tn) {
         List<?> items = e.getData();
         TreeStore<Sharing> store = grid.getTreeStore();
 
         if (items != null && items.size() > 0) {
             if (items.get(0) instanceof Collaborator) {
                 for (Object obj : items) {
                     if (store.contains(new Sharing((Collaborator)obj))) {
                         setDNDFeedback(e, false);
                         return;
                     }
                 }
                 return;
             } else {
                 if (tn == null) {
                     e.getStatus().setStatus(false);
                     e.setCancelled(true);
                     return;
                 }
                 Sharing s = (Sharing)tn.getModel();
                 for (Object dr : items) {
                     if (!canShare(s.getCollaborator(), ((DiskResource)dr).getId())) {
                         setDNDFeedback(e, false);
                         return;
                     }
                 }
             }
         }
     }
 }
