 /**
  *
  */
 package org.iplantc.core.uidiskresource.client.sharing.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.collaborators.events.UserSearchResultSelected;
 import org.iplantc.core.uicommons.client.collaborators.events.UserSearchResultSelected.USER_SEARCH_EVENT_TAG;
 import org.iplantc.core.uicommons.client.collaborators.models.Collaborator;
 import org.iplantc.core.uicommons.client.collaborators.presenter.ManageCollaboratorsPresenter.MODE;
 import org.iplantc.core.uicommons.client.collaborators.util.UserSearchField;
 import org.iplantc.core.uicommons.client.collaborators.views.ManageCollaboratorsDailog;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.core.uidiskresource.client.sharing.models.DataSharing;
 import org.iplantc.core.uidiskresource.client.sharing.models.DataSharingKeyProvider;
 import org.iplantc.core.uidiskresource.client.sharing.models.DataSharingProperties;
 import org.iplantc.core.uidiskresource.client.sharing.views.DataSharingView.Presenter;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.safecss.shared.SafeStyles;
 import com.google.gwt.safecss.shared.SafeStylesUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.cell.core.client.TextButtonCell;
 import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.core.shared.FastMap;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.StringLabelProvider;
 import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
 import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
 import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
 import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
 import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 
 /**
  * @author sriram
  *
  */
 public class DataSharingPermissionsPanel implements IsWidget {
 
     @UiField
     Grid<DataSharing> grid;
 
     @UiField
     ToolBar toolbar;
 
     @UiField(provided = true)
     ListStore<DataSharing> listStore;
 
     @UiField(provided = true)
     ColumnModel<DataSharing> cm;
 
     @UiField
     VerticalLayoutContainer container;
 
     private FastMap<List<DataSharing>> originalList;
     private final FastMap<DiskResource> resources;
     private final Presenter presenter;
     private GridEditing<DataSharing> gridEditing;
 
     private static final String ID_PERM_GROUP = "idPermGroup";
     private SimpleComboBox<Object> permCombo;
     private FastMap<List<DataSharing>> sharingMap;
     private HorizontalPanel explainPanel;
 
     final Widget widget;
     private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
     @UiTemplate("DataSharingPermissionsView.ui.xml")
     interface MyUiBinder extends UiBinder<Widget, DataSharingPermissionsPanel> {
     }
 
     public DataSharingPermissionsPanel(Presenter dataSharingPresenter, FastMap<DiskResource> resources) {
         this.presenter = dataSharingPresenter;
         this.resources = resources;
         init();
         widget = uiBinder.createAndBindUi(this);
         initToolbar();
         initGrid();
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     private void init() {
         listStore = new ListStore<DataSharing>(new DataSharingKeyProvider());
         cm = buildColumnModel();
         buildPermissionsCombo();
         EventBus.getInstance().addHandler(UserSearchResultSelected.TYPE,
                 new UserSearchResultSelected.UserSearchResultSelectedEventHandler() {
 
                     @Override
                     public void onUserSearchResultSelected(
                             UserSearchResultSelected userSearchResultSelected) {
                         if (userSearchResultSelected.getTag().equals(
                                 UserSearchResultSelected.USER_SEARCH_EVENT_TAG.SHARING.toString())) {
                             addCollaborator(userSearchResultSelected.getCollaborator());
                         }
 
                     }
                 });
     }
 
     private void initGrid() {
         grid.setSelectionModel(new CellSelectionModel<DataSharing>());
         gridEditing = new GridInlineEditing<DataSharing>(grid);
         gridEditing.addEditor(cm.getColumn(1), permCombo);
         gridEditing.addCompleteEditHandler(new CompleteEditHandler<DataSharing>() {
 
             @Override
             public void onCompleteEdit(CompleteEditEvent<DataSharing> event) {
                 Object value = permCombo.getCurrentValue();
                 updatePermissions(value.toString(), grid.getSelectionModel().getSelectedItem()
                         .getUserName());
             }
         });
         grid.getView().setAutoExpandColumn(cm.getColumn(0));
         grid.getView().setEmptyText(I18N.DISPLAY.sharePrompt());
 
     }
 
     private void initToolbar() {
         addExplainPanel();
         toolbar.add(new UserSearchField(USER_SEARCH_EVENT_TAG.SHARING).asWidget());
         toolbar.add(new FillToolItem());
         toolbar.add(buildChooseCollabButton());
     }
 
     private TextButton buildChooseCollabButton() {
         TextButton button = new TextButton();
         button.setText(I18N.DISPLAY.chooseFromCollab());
         button.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 final ManageCollaboratorsDailog dialog = new ManageCollaboratorsDailog(MODE.SELECT);
                 dialog.setModal(true);
                 dialog.show();
                 dialog.addOkButtonSelectHandler(new SelectHandler() {
 
                     @Override
                     public void onSelect(SelectEvent event) {
                         List<Collaborator> selected = dialog.getSelectedCollaborators();
                         if (selected != null && selected.size() > 0) {
                             for (Collaborator c : selected) {
                                 addCollaborator(c);
                             }
                         }
                     }
                 });
 
             }
 
         });
         button.setToolTip(I18N.DISPLAY.chooseFromCollab());
         button.setIcon(IplantResources.RESOURCES.share());
         return button;
     }
 
     private SimpleComboBox<Object> buildPermissionsCombo() {
         permCombo = new SimpleComboBox<Object>(new StringLabelProvider<Object>());
         permCombo.setId(ID_PERM_GROUP);
         permCombo.setForceSelection(true);
         permCombo.add(DataSharing.READ);
         permCombo.add(DataSharing.WRITE);
         permCombo.add(DataSharing.OWN);
         permCombo.setEditable(false);
 
         permCombo.setTriggerAction(TriggerAction.ALL);
         return permCombo;
     }
 
     private void addExplainPanel() {
         explainPanel = new HorizontalPanel();
         explainPanel.add(new LabelToolItem(I18N.DISPLAY.variablePermissionsNotice() + ":"));
         TextButton explainBtn = new TextButton(I18N.DISPLAY.explain(), new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 ArrayList<DataSharing> shares = new ArrayList<DataSharing>();
                 for (String user : sharingMap.keySet()) {
                     shares.addAll(sharingMap.get(user));
                 }
 
                 ShareBreakDownDialog explainDlg = new ShareBreakDownDialog(shares);
                 explainDlg.setHeadingText(I18N.DISPLAY.whoHasAccess());
                 explainDlg.show();
             }
         });
         explainPanel.add(explainBtn);
         toolbar.add(explainPanel);
     }
 
     private void addCollaborator(Collaborator user) {
         String userName = user.getUserName();
         if (userName != null && userName.equalsIgnoreCase(UserInfo.getInstance().getUsername())) {
             AlertMessageBox amb = new AlertMessageBox(I18N.DISPLAY.warning(),
             		I18N.DISPLAY.selfShareWarning());
             amb.show();
             return;
         }
 
         // Only add users not already displayed in the grid.
         if (sharingMap.get(userName) == null) {
             List<DataSharing> shareList = new ArrayList<DataSharing>();
             DataSharing displayShare = null;
 
             for (String path : resources.keySet()) {
                 DataSharing share = new DataSharing(user, presenter.getDefaultPermissions(), path);
                 shareList.add(share);
 
                 if (displayShare == null) {
                     displayShare = share.copy();
                     grid.getStore().add(displayShare);
                 }
             }
 
             sharingMap.put(userName, shareList);
         }
     }
 
     private void removeModels(DataSharing model) {
         ListStore<DataSharing> store = grid.getStore();
 
         DataSharing sharing = store.findModel(model);
         if (sharing != null) {
             // Remove the shares from the sharingMap as well as the grid.
             sharingMap.put(sharing.getUserName(), null);
             store.remove(sharing);
         }
     }
 
     public void loadSharingData(FastMap<List<DataSharing>> sharingMap) {
         this.sharingMap = sharingMap;
         originalList = new FastMap<List<DataSharing>>();
 
         ListStore<DataSharing> store = grid.getStore();
         store.clear();
         explainPanel.setVisible(false);
 
         for (String userName : sharingMap.keySet()) {
             List<DataSharing> dataShares = sharingMap.get(userName);
 
             if (dataShares != null && !dataShares.isEmpty()) {
                 List<DataSharing> newList = new ArrayList<DataSharing>();
                 for (DataSharing share : dataShares) {
                     DataSharing copyShare = share.copy();
                     newList.add(copyShare);
                 }
                 originalList.put(userName, newList);
 
                 // Add a dummy display share to the grid.
                 DataSharing displayShare = dataShares.get(0).copy();
                 if (hasVaryingPermissions(dataShares)) {
                     // Set the display permission to "varies" if this user's share list has varying
                     // permissions.
                     displayShare.setDisplayPermission(I18N.DISPLAY.varies());
                     explainPanel.setVisible(true);
                 }
 
                 store.add(displayShare);
             }
         }
     }
 
     private ColumnModel<DataSharing> buildColumnModel() {
         List<ColumnConfig<DataSharing, ?>> configs = new ArrayList<ColumnConfig<DataSharing, ?>>();
         DataSharingProperties props = GWT.create(DataSharingProperties.class);
         ColumnConfig<DataSharing, String> name = new ColumnConfig<DataSharing, String>(
                props.name());
 
         name.setHeader(I18N.DISPLAY.name());
         name.setWidth(220);
         ColumnConfig<DataSharing, String> permission = new ColumnConfig<DataSharing, String>(
                 props.displayPermission());
 
         permission.setHeader(I18N.DISPLAY.permissions());
        permission.setWidth(80);
         SafeStyles permTextStyles = SafeStylesUtils.fromTrustedString("color:#0098AA;cursor:pointer;");
         permission.setColumnTextStyle(permTextStyles);
         ColumnConfig<DataSharing, String> remove = new ColumnConfig<DataSharing, String>(
                 new ValueProvider<DataSharing, String>() {
 
                     @Override
                     public String getValue(DataSharing object) {
                         return "";
                     }
 
                     @Override
                     public void setValue(DataSharing object, String value) {
                         // do nothing
 
                     }
 
                     @Override
                     public String getPath() {
                         return "";
                     }
                 });
 
        SafeStyles textStyles = SafeStylesUtils.fromTrustedString("padding: 1px 3px;cursor:pointer;");
        remove.setColumnTextStyle(textStyles);
         remove.setToolTip(I18N.DISPLAY.unshare());
         TextButtonCell button = buildRemoveButtonCell();
         remove.setCell(button);
         configs.add(name);
         configs.add(permission);
         configs.add(remove);
         return new ColumnModel<DataSharing>(configs);
     }
 
     private TextButtonCell buildRemoveButtonCell() {
         TextButtonCell button = new TextButtonCell();
         button.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 removeModels(grid.getSelectionModel().getSelectedItem());
             }
 
         });
         button.setIcon(IplantResources.RESOURCES.delete());
         return button;
     }
 
     /**
      *
      *
      * @return the sharing list
      */
     public FastMap<List<DataSharing>> getSharingMap() {
         FastMap<List<DataSharing>> sharingList = new FastMap<List<DataSharing>>();
         for (DataSharing share : grid.getStore().getAll()) {
             String userName = share.getUserName();
             List<DataSharing> dataShares = sharingMap.get(userName);
             List<DataSharing> updatedSharingList = getUpdatedSharingList(userName, dataShares);
             if (updatedSharingList != null && updatedSharingList.size() > 0) {
                 sharingList.put(userName, updatedSharingList);
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
     private List<DataSharing> getUpdatedSharingList(String userName, List<DataSharing> list) {
         List<DataSharing> updateList = new ArrayList<DataSharing>();
         if (list != null && userName != null) {
             List<DataSharing> fromOriginal = originalList.get(userName);
 
             if (fromOriginal == null || fromOriginal.isEmpty()) {
                 updateList = list;
             } else {
                 for (DataSharing s : list) {
                     if (!fromOriginal.contains(s)) {
                         updateList.add(s);
                     }
                 }
             }
         }
 
         return updateList;
     }
 
     private void updatePermissions(String perm, String username) {
         List<DataSharing> models = sharingMap.get(username);
         if (models != null) {
             boolean own = perm.equals(DataSharing.OWN);
             boolean write = own || perm.equals(DataSharing.WRITE);
             boolean read = true;
 
             for (DataSharing share : models) {
                 if (own) {
                     share.setOwner(true);
                 } else if (write) {
                     share.setWritable(true);
                 } else {
                     share.setReadable(true);
                 }
             }
 
             if (resources.size() != models.size()) {
                 Collaborator user = models.get(0).getCollaborator();
                 DiskResourceAutoBeanFactory factory = GWT.create(DiskResourceAutoBeanFactory.class);
                 AutoBean<Permissions> autoBean = AutoBeanCodex.decode(factory, Permissions.class,
                         buildSharingPermissions(read, write, own));
                 for (String path : resources.keySet()) {
                     boolean shared = false;
                     for (DataSharing existingShare : models) {
                         if (path.equals(existingShare.getPath())) {
                             shared = true;
                             break;
                         }
                     }
 
                     if (!shared) {
                         models.add(new DataSharing(user, autoBean.as(), path));
                     }
                 }
             }
 
             checkExplainPanelVisibility();
         }
     }
 
     /**
      * Checks if the explainPanel should be hidden after permissions have been updated or removed.
      */
     private void checkExplainPanelVisibility() {
         if (explainPanel.isVisible()) {
             boolean permsVary = false;
 
             for (DataSharing dataShare : grid.getStore().getAll()) {
                 permsVary = hasVaryingPermissions(sharingMap.get(dataShare.getUserName()));
 
                 if (permsVary) {
                     // Stop checking after the first user is found with variable permissions.
                     break;
                 }
             }
 
             if (!permsVary) {
                 explainPanel.setVisible(false);
             }
         }
     }
 
     /**
      * @param dataShares
      * @return true if the given dataShares list has a different size than the resources list, or if not
      *         every permission in the given dataShares list is the same; false otherwise.
      */
     private boolean hasVaryingPermissions(List<DataSharing> dataShares) {
         if (dataShares == null || dataShares.size() != resources.size()) {
             return true;
         } else {
             String displayPermission = dataShares.get(0).getDisplayPermission();
 
             for (DataSharing share : dataShares) {
                 if (!displayPermission.equals(share.getDisplayPermission())) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private String buildSharingPermissions(boolean read, boolean write, boolean own) {
         JSONObject permission = new JSONObject();
         permission.put("read", JSONBoolean.getInstance(read));
         permission.put("write", JSONBoolean.getInstance(write));
         permission.put("own", JSONBoolean.getInstance(own));
         return permission.toString();
     }
 
     /**
      * @return the unshareList
      */
     public FastMap<List<DataSharing>> getUnshareList() {
         // Prepare unshared list here
         FastMap<List<DataSharing>> unshareList = new FastMap<List<DataSharing>>();
 
         for (String userName : originalList.keySet()) {
             if (sharingMap.get(userName) == null) {
                 // The username entry from the original list was removed from the sharingMap, which means
                 // it was unshared.
                 List<DataSharing> removeList = originalList.get(userName);
 
                 if (removeList != null && !removeList.isEmpty()) {
                     unshareList.put(userName, removeList);
                 }
             }
         }
 
         return unshareList;
     }
 
     public void mask(String loadingMask) {
         container.mask(I18N.DISPLAY.loadingMask());
     }
 
     public void unmask() {
         container.unmask();
     }
 
 }
