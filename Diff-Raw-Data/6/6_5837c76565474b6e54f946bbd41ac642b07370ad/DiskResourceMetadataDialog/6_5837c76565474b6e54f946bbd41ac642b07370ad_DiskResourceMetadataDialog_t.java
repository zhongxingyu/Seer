 package org.iplantc.core.uidiskresource.client.views.metadata;
 
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadataList;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceMetadataProperties;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMetadataUpdateCallback;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Sets;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.Store;
 import com.sencha.gxt.data.shared.Store.Change;
 import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent.InvalidHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.event.ValidEvent;
 import com.sencha.gxt.widget.core.client.event.ValidEvent.ValidHandler;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.form.Validator;
 import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
 import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.editing.ClicksToEdit;
 import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 
 public class DiskResourceMetadataDialog extends IPlantDialog {
 
     private static final String USER_UNIT_TAG = "ipc_user_unit_tag";
 
     @UiTemplate("DiskResourceMetadataEditorPanel.ui.xml")
     interface DiskResourceMetadataEditorPanelUiBinder extends
             UiBinder<Widget, DiskResourceMetadataDialog> {
     }
 
     private static DiskResourceMetadataEditorPanelUiBinder uiBinder = GWT
             .create(DiskResourceMetadataEditorPanelUiBinder.class);
 
     @UiField
     TextButton addMetadataButton;
 
     @UiField
     TextButton deleteMetadataButton;
 
     @UiField
     ColumnModel<DiskResourceMetadata> cm;
 
     @UiField
     Grid<DiskResourceMetadata> grid;
 
     @UiField
     ListStore<DiskResourceMetadata> listStore;
 
     private final TextButton okButton;
 
     private final DiskResource resource;
 
     private final Set<DiskResourceMetadata> toBeDeleted = Sets.newHashSet();
 
     private final DiskResourceAutoBeanFactory autoBeanFactory = GWT
             .create(DiskResourceAutoBeanFactory.class);
 
     private GridInlineEditing<DiskResourceMetadata> gridInlineEditing;
 
     private AttributeCell attributeCell;
 
     public DiskResourceMetadataDialog(final DiskResource resource,
             final DiskResourceView.Presenter presenter) {
         super(true);
         this.resource = resource;
         setSize("500", "300");
         setWidget(uiBinder.createAndBindUi(this));
         setHeadingText(I18N.DISPLAY.metadata() + ":" + resource.getId());
 
         okButton = getButtonById(PredefinedButton.OK.name());
         addHelp(new HTML(I18N.HELP.metadataHelp()));
 
         addOkButtonSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 presenter.setDiskResourceMetaData(resource, getMetadataToAdd(), getMetadataToDelete(),
                         new DiskResourceMetadataUpdateCallback());
             }
         });
         initGridEditing(grid, listStore, okButton);
         grid.getSelectionModel().addSelectionChangedHandler(
                 new MetadataSelectionChangedListener(deleteMetadataButton, grid.getSelectionModel(),
                         gridInlineEditing));
 
         presenter.getDiskResourceMetadata(resource, new RetrieveMetadataCallback(listStore,
                 autoBeanFactory));
     }
     
     private void initGridEditing(final Grid<DiskResourceMetadata> grid,
             final ListStore<DiskResourceMetadata> listStore, final TextButton okButton) {
         gridInlineEditing = new GridInlineEditing<DiskResourceMetadata>(grid);
        gridInlineEditing.setClicksToEdit(ClicksToEdit.TWO);
         ColumnConfig<DiskResourceMetadata, String> column1 = grid.getColumnModel().getColumn(0);
         ColumnConfig<DiskResourceMetadata, String> column2 = grid.getColumnModel().getColumn(1);
 
         TextField field1 = new TextField();
         TextField field2 = new TextField();
 
         field1.setAutoValidate(true);
         field2.setAutoValidate(true);
 
         field1.setAllowBlank(false);
         field2.setAllowBlank(false);
 
         field1.addValidator(new DuplicateAttributeValidator(listStore));
         AttributeValidationHandler validationHandler = new AttributeValidationHandler(okButton);
         field1.addInvalidHandler(validationHandler);
         field1.addValidHandler(validationHandler);
 
         gridInlineEditing.addEditor(column1, field1);
         gridInlineEditing.addEditor(column2, field2);
         gridInlineEditing.addCompleteEditHandler(new CompleteEditHandler<DiskResourceMetadata>() {
 
             @Override
             public void onCompleteEdit(CompleteEditEvent<DiskResourceMetadata> event) {
                 listStore.commitChanges();
             }
         });
 
     }
 
     @UiFactory
     ListStore<DiskResourceMetadata> createListStore() {
         return new ListStore<DiskResourceMetadata>(new ModelKeyProvider<DiskResourceMetadata>() {
 
             @Override
             public String getKey(DiskResourceMetadata item) {
                return item.getAttribute() + "-" + item.getValue() + "-" + item.getUnit();
             }
         });
     }
 
     @UiHandler("deleteMetadataButton")
     void onDeleteMetadataSelected(SelectEvent event) {
         if (!resource.getPermissions().isWritable()) {
             AlertMessageBox mb = new AlertMessageBox(I18N.ERROR.permissionErrorTitle(),
                     I18N.ERROR.permissionErrorMessage());
             mb.show();
             return;
         }
 
         for (DiskResourceMetadata md : grid.getSelectionModel().getSelectedItems()) {
             toBeDeleted.add(md);
             listStore.remove(md);
         }
     }
 
     @UiHandler("addMetadataButton")
     void onAddMetadataSelected(SelectEvent event) {
         if (!resource.getPermissions().isWritable()) {
             return;
         }
         DiskResourceMetadata md = autoBeanFactory.metadata().as();
         md.setAttribute(getUniqeAttrName("New Attribute", 0));
         md.setValue("New Value");
         md.setUnit(USER_UNIT_TAG);
         listStore.add(0, md);
         gridInlineEditing.startEditing(new GridCell(0, 0));
         gridInlineEditing.getEditor(grid.getColumnModel().getColumn(0)).validate();
 
     }
 
     private String getUniqeAttrName(String attrName, int i) {
         String retName = i > 0 ? attrName + "_(" + i + ")" : attrName;
         for (DiskResourceMetadata md : listStore.getAll()) {
             if (md.getAttribute().equals(retName)) {
                 return getUniqeAttrName(attrName + "", ++i);
             }
         }
         return retName;
     }
 
     @UiFactory
     ColumnModel<DiskResourceMetadata> createColumnModel() {
         List<ColumnConfig<DiskResourceMetadata, ?>> columns = Lists.newArrayList();
         DiskResourceMetadataProperties props = GWT.create(DiskResourceMetadataProperties.class);
         ColumnConfig<DiskResourceMetadata, String> attributeColumn = new ColumnConfig<DiskResourceMetadata, String>(
                 props.attribute(), 150, "Attribute");
         ColumnConfig<DiskResourceMetadata, String> valueColumn = new ColumnConfig<DiskResourceMetadata, String>(
                 props.value(), 150, "Value");
 
         attributeCell = new AttributeCell(listStore);
         attributeColumn.setCell(attributeCell);
         columns.add(attributeColumn);
         columns.add(valueColumn);
 
         ColumnModel<DiskResourceMetadata> cm = new ColumnModel<DiskResourceMetadata>(columns);
         return cm;
     }
 
     protected Set<DiskResourceMetadata> getMetadataToDelete() {
         return toBeDeleted;
     }
 
     protected Set<DiskResourceMetadata> getMetadataToAdd() {
         return Sets.newHashSet(listStore.getAll());
     }
 
     private final class AttributeCell extends AbstractCell<String> {
         private final ListStore<DiskResourceMetadata> listStore;
         private final DiskResourceMetadataProperties props = GWT
                 .create(DiskResourceMetadataProperties.class);
 
         public AttributeCell(final ListStore<DiskResourceMetadata> listStore) {
             this.listStore = listStore;
         }
 
         @Override
         public void render(Context context, String value, SafeHtmlBuilder sb) {
             if (value == null) {
                 return;
             }
 
             Multiset<String> dupeSet = HashMultiset.create();
 
             for (DiskResourceMetadata drmd : listStore.getAll()) {
                 Store<DiskResourceMetadata>.Record record = listStore.getRecord(drmd);
 
                 if (record.isDirty()) {
                     Change<DiskResourceMetadata, String> change = record.getChange(props.attribute());
                     if (change != null) {
                         dupeSet.add(change.getValue());
                     }
                 } else {
                     String attribute = drmd.getAttribute();
                     dupeSet.add(attribute);
                 }
             }
 
             // Ok Button only enabled if there are no duplicate attributes.
             int count = dupeSet.count(value);
             if (count == 1) {
                 sb.append(SafeHtmlUtils.fromString(value));
             } else {
                 sb.append(SafeHtmlUtils
                         .fromSafeConstant("<span qtip='duplicate attribute' style='color:red;'>"));
                 sb.append(SafeHtmlUtils.fromString(value));
                 sb.append(SafeHtmlUtils.fromSafeConstant("</span>"));
             }
         }
     }
 
     private final class MetadataSelectionChangedListener implements
             SelectionChangedHandler<DiskResourceMetadata> {
 
         private final TextButton deleteButton;
         private final GridSelectionModel<DiskResourceMetadata> sm;
         private final GridInlineEditing<DiskResourceMetadata> editing;
 
         public MetadataSelectionChangedListener(final TextButton deleteButton,
                 final GridSelectionModel<DiskResourceMetadata> sm,
                 final GridInlineEditing<DiskResourceMetadata> editing) {
             this.deleteButton = deleteButton;
             this.sm = sm;
             this.editing = editing;
         }
 
         @Override
         public void onSelectionChanged(SelectionChangedEvent<DiskResourceMetadata> event) {
             deleteButton.setEnabled(!sm.getSelectedItems().isEmpty());
             editing.completeEditing();
         }
     }
 
     private final class RetrieveMetadataCallback implements AsyncCallback<String> {
 
         private final ListStore<DiskResourceMetadata> store;
         private final DiskResourceAutoBeanFactory autoBeanFactory;
 
         public RetrieveMetadataCallback(final ListStore<DiskResourceMetadata> store,
                 final DiskResourceAutoBeanFactory autoBeanFactory) {
             this.store = store;
             this.autoBeanFactory = autoBeanFactory;
         }
 
         @Override
         public void onSuccess(String result) {
             AutoBean<DiskResourceMetadataList> bean = AutoBeanCodex.decode(autoBeanFactory,
                     DiskResourceMetadataList.class, result);
             // for (DiskResourceMetadata md : bean.as().getMetadata()) {
             // md.setId("mdId:" + bean.as().getMetadata().indexOf(md));
             // }
             store.addAll(bean.as().getMetadata());
 
         }
 
         @Override
         public void onFailure(Throwable caught) {
             ErrorHandler.post(caught);
         }
     }
 
     private final class AttributeValidationHandler implements ValidHandler, InvalidHandler {
         private final TextButton okButton;
 
         public AttributeValidationHandler(TextButton okButton) {
             this.okButton = okButton;
         }
 
         @Override
         public void onValid(ValidEvent event) {
             okButton.setEnabled(true);
         }
 
         @Override
         public void onInvalid(InvalidEvent event) {
             okButton.setEnabled(false);
         }
     }
 
     private final class DuplicateAttributeValidator implements Validator<String> {
 
         private final ListStore<DiskResourceMetadata> listStore;
         private final DiskResourceMetadataProperties props = GWT
                 .create(DiskResourceMetadataProperties.class);
 
         public DuplicateAttributeValidator(ListStore<DiskResourceMetadata> listStore) {
             this.listStore = listStore;
         }
 
         @Override
         public List<EditorError> validate(Editor<String> editor, String value) {
             List<EditorError> errors = Lists.newArrayList();
             Multiset<String> dupeSet = HashMultiset.create();
             for (DiskResourceMetadata md : listStore.getAll()) {
                 Store<DiskResourceMetadata>.Record record = listStore.getRecord(md);
 
                 if (record.isDirty()) {
                     Change<DiskResourceMetadata, String> change = record.getChange(props.attribute());
                     if (change != null) {
                         dupeSet.add(change.getValue());
                     }
                 } else {
                     String attribute = md.getAttribute();
                     dupeSet.add(attribute);
                 }
             }
             // If there are duplicates AND the value is one of the dupes
             if (dupeSet.count(value) > 1) {
                 // if ((dupeSet.size() != listStore.getAll().size()) && dupeSet.contains(value)) {
                 errors.add(new DefaultEditorError(editor, "Duplicate Attribute", value));
             }
             return errors;
         }
     }
 }
