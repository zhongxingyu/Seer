 package org.iplantc.core.uidiskresource.client.metadata.view;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadataList;
 import org.iplantc.core.uicommons.client.models.diskresources.MetadataTemplateAttribute;
 import org.iplantc.core.uicommons.client.models.diskresources.MetadataTemplateInfo;
 import org.iplantc.core.uicommons.client.validators.UrlValidator;
 import org.iplantc.core.uicommons.client.widgets.IPlantAnchor;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceMetadataProperties;
 import org.iplantc.core.uidiskresource.client.services.callbacks.DiskResourceMetadataUpdateCallback;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.safehtml.shared.SafeUri;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
 import com.sencha.gxt.core.client.resources.ThemeStyles;
 import com.sencha.gxt.core.shared.FastMap;
 import com.sencha.gxt.data.shared.LabelProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.Dialog;
 import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.AccordionLayoutAppearance;
 import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.ExpandMode;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
 import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent;
 import com.sencha.gxt.widget.core.client.event.InvalidEvent.InvalidHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.ValidEvent;
 import com.sencha.gxt.widget.core.client.event.ValidEvent.ValidHandler;
 import com.sencha.gxt.widget.core.client.form.CheckBox;
 import com.sencha.gxt.widget.core.client.form.ComboBox;
 import com.sencha.gxt.widget.core.client.form.DateField;
 import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
 import com.sencha.gxt.widget.core.client.form.Field;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.FormPanel.LabelAlign;
 import com.sencha.gxt.widget.core.client.form.FormPanelHelper;
 import com.sencha.gxt.widget.core.client.form.IsField;
 import com.sencha.gxt.widget.core.client.form.NumberField;
 import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
 import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
 import com.sencha.gxt.widget.core.client.form.TextArea;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
 import com.sencha.gxt.widget.core.client.grid.editing.ClicksToEdit;
 import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 import com.sencha.gxt.widget.core.client.tips.QuickTip;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 
 public class DiskResourceMetadataView implements IsWidget {
 
     private final class RemoveTemplateHandlerImpl implements ClickHandler {
 
         @Override
         public void onClick(ClickEvent event) {
             ConfirmMessageBox cmb = new ConfirmMessageBox(I18N.DISPLAY.confirmAction(),
                     I18N.DISPLAY.metadataTemplateConfirmRemove());
             cmb.addHideHandler(new HideHandler() {
 
                 @Override
                 public void onHide(HideEvent event) {
                     Dialog d = (Dialog)event.getSource();
                     if (d.getHideButton().getText().equalsIgnoreCase("yes")) { //$NON-NLS-1$
                         alc.remove(templateForm);
                         deleteTemplateAttrs();
                         templateCombo.setEnabled(true);
                         expandUserMetadataPanel();
                        templateCombo.setValue(null);
                         selectedTemplate = null;
                     }
                 }
             });
 
             cmb.show();
         }
     }
 
     private final class TemplateInfoSelectionHandler implements SelectionHandler<MetadataTemplateInfo> {
         @Override
         public void onSelection(SelectionEvent<MetadataTemplateInfo> event) {
             selectedTemplate = event.getSelectedItem();
            templateCombo.setValue(selectedTemplate, true);
             onTemplateSelected(selectedTemplate);
         }
     }
 
     private final class TemplateInfoLabelProvider implements LabelProvider<MetadataTemplateInfo> {
         @Override
         public String getLabel(MetadataTemplateInfo item) {
             return item.getName();
         }
     }
 
     private final class TemplateInfoModelKeyProvider implements ModelKeyProvider<MetadataTemplateInfo> {
         @Override
         public String getKey(MetadataTemplateInfo item) {
             return item.getId();
         }
     }
 
     public interface Presenter extends org.iplantc.core.uicommons.client.presenter.Presenter {
         /**
          * Retrieves a collection of metadata for the given resource.
          * 
          * @param resource
          * @param callback
          * @return a collection of the given resource's metadata.
          */
         void getDiskResourceMetadata(AsyncCallback<String> callback);
 
         void setDiskResourceMetaData(Set<DiskResourceMetadata> metadataToAdd,
                 Set<DiskResourceMetadata> metadataToDelete,
                 DiskResourceMetadataUpdateCallback diskResourceMetadataUpdateCallback);
 
         DiskResource getSelectedResource();
 
         void getTemplates();
 
         void onTemplateSelected(String templateId);
 
     }
 
     private static final String IPC_METADATA_TEMPLATE_ATTR = "ipc-metadata-template"; //$NON-NLS-1$
     private static final String USER_UNIT_TAG = "ipc_user_unit_tag"; //$NON-NLS-1$
 
     @UiTemplate("DiskResourceMetadataEditorPanel.ui.xml")
     interface DiskResourceMetadataEditorPanelUiBinder extends UiBinder<Widget, DiskResourceMetadataView> {
     }
 
     private static DiskResourceMetadataEditorPanelUiBinder uiBinder = GWT
             .create(DiskResourceMetadataEditorPanelUiBinder.class);
 
     @UiField
     BorderLayoutContainer con;
 
     @UiField
     ToolBar toolbar;
 
     @UiField
     TextButton addMetadataButton;
 
     @UiField
     TextButton deleteMetadataButton;
 
     private Grid<DiskResourceMetadata> grid;
 
     private ListStore<DiskResourceMetadata> listStore;
 
     @UiField
     ComboBox<MetadataTemplateInfo> templateCombo;
 
     private VerticalLayoutContainer templateContainer;
 
     private ContentPanel templateForm;
 
     private final Widget widget;
 
     private final Set<DiskResourceMetadata> toBeDeleted = Sets.newHashSet();
 
     private GridInlineEditing<DiskResourceMetadata> gridInlineEditing;
 
     private MetadataCell metadataCell;
 
     private final DiskResourceAutoBeanFactory autoBeanFactory = GWT
             .create(DiskResourceAutoBeanFactory.class);
 
     private Presenter presenter;
 
     private boolean valid;
 
     private ListStore<MetadataTemplateInfo> templateStore;
 
     private final AccordionLayoutContainer alc;
 
     private final AccordionLayoutAppearance appearance;
 
     private ContentPanel userMetadataPanel;
 
     private final VerticalLayoutContainer centerPanel;
 
     private final FastMap<Field<?>> templateAttrFieldMap = new FastMap<Field<?>>();
 
     private final FastMap<DiskResourceMetadata> attrAvuMap = new FastMap<DiskResourceMetadata>();
 
     private MetadataTemplateInfo selectedTemplate;
 
     private final DiskResource selectedResource;
 
     private final boolean writable;
 
     private int unique_avu_id;
 
     private final DateTimeFormat timestampFormat = DateTimeFormat
             .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
 
     interface MetadataHtmlTemplates extends SafeHtmlTemplates {
 
         @SafeHtmlTemplates.Template("<b>{0}</b>")
         SafeHtml boldHeader(String headerText);
 
         @SafeHtmlTemplates.Template("<span qtip=\"{0}\">{0}</span>")
         SafeHtml cell(String value);
 
         @SafeHtmlTemplates.Template("<img style='cursor:pointer;' qtip=\"{1}\" src=\"{0}\"/>")
         SafeHtml labelInfo(SafeUri img, String toolTip);
 
         @SafeHtmlTemplates.Template("<span style='color:red; top:-5px;'>*</span>")
         SafeHtml required();
 
         @SafeHtmlTemplates.Template("<span> {0}&nbsp;{2}&nbsp;{1}</span>")
         SafeHtml labelHtml(SafeHtml info, String label, SafeHtml required);
     }
 
     MetadataHtmlTemplates htmlTemplates = GWT.create(MetadataHtmlTemplates.class);
 
     public DiskResourceMetadataView(DiskResource dr) {
         widget = uiBinder.createAndBindUi(this);
         selectedResource = dr;
         writable = selectedResource.getPermissions().isWritable();
         alc = new AccordionLayoutContainer();
         centerPanel = new VerticalLayoutContainer();
         con.setCenterWidget(centerPanel);
         appearance = GWT.<AccordionLayoutAppearance> create(AccordionLayoutAppearance.class);
         initGrid();
         addMetadataButton.setEnabled(writable);
         deleteMetadataButton.disable();
         templateCombo.setEnabled(writable);
         valid = true;
     }
 
     public void setPresenter(Presenter p) {
         this.presenter = p;
     }
     
     interface MetadataInfoTemplate extends XTemplates {
         @XTemplate("<div qtip=\"{name}\" >{name}</div>")
         SafeHtml templateInfo(String name);
     }
 
     @UiFactory
     ComboBox<MetadataTemplateInfo> buildTemplateCombo() {
         templateStore = new ListStore<MetadataTemplateInfo>(new TemplateInfoModelKeyProvider());
 
         templateCombo = new ComboBox<MetadataTemplateInfo>(templateStore,
                 new TemplateInfoLabelProvider(), new AbstractSafeHtmlRenderer<MetadataTemplateInfo>() {
 
                     @Override
                     public SafeHtml render(MetadataTemplateInfo object) {
                         final MetadataInfoTemplate xtemp = GWT.create(MetadataInfoTemplate.class);
                         return xtemp.templateInfo(object.getName());
                     }
                 });
         templateCombo.setEditable(false);
         templateCombo.setWidth(250);
         templateCombo.setEmptyText(I18N.DISPLAY.metadataTemplateSelect());
         templateCombo.setTypeAhead(true);
         templateCombo.addSelectionHandler(new TemplateInfoSelectionHandler());
         return templateCombo;
     }
 
     private void onTemplateSelected(MetadataTemplateInfo templateInfo) {
         templateCombo.setEnabled(false);
         presenter.onTemplateSelected(templateInfo.getId());
         buildTemplateContainer();
         alc.mask(I18N.DISPLAY.loadingMask());
     }
 
     public void loadTemplateAttributes(List<MetadataTemplateAttribute> attributes) {
         templateAttrFieldMap.clear();
         IPlantAnchor removeLink = buildRemoveTemplateLink();
         templateContainer.add(removeLink, new VerticalLayoutData(.25, -1));
         for (MetadataTemplateAttribute attribute : attributes) {
             Field<?> field = getAttributeValueWidget(attribute);
             if (field != null) {
                 templateAttrFieldMap.put(attribute.getName(), field);
                 templateContainer.add(
                         buildFieldLabel(field, attribute.getName(), attribute.getDescription(),
                                 !attribute.isRequired()), new VerticalLayoutData(.90, -1));
                 // remove these from store so that they don't display in grid
                 listStore.remove(attrAvuMap.get(attribute.getName()));
             }
         }
         alc.forceLayout();
         alc.unmask();
     }
 
     private IPlantAnchor buildRemoveTemplateLink() {
         return new IPlantAnchor(I18N.DISPLAY.metadataTemplateRemove(), 575,
                 new RemoveTemplateHandlerImpl());
     }
 
     private void deleteTemplateAttrs() {
         DiskResourceMetadata drmd = attrAvuMap.get(IPC_METADATA_TEMPLATE_ATTR);
         if (drmd != null) {
             toBeDeleted.add(drmd);
             for (String key : templateAttrFieldMap.keySet()) {
                 DiskResourceMetadata avu = attrAvuMap.get(key);
                 if (avu != null) {
                     toBeDeleted.add(avu);
                     listStore.remove(avu);
                 }
             }
         }
     }
 
     private TextField buildTextField(MetadataTemplateAttribute attribute) {
         TextField fld = new TextField();
         fld.setAllowBlank(!attribute.isRequired());
 
         DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
         if (avu != null) {
             fld.setValue(avu.getValue());
         }
 
         return fld;
     }
 
     private NumberField<Integer> buildIntegerField(MetadataTemplateAttribute attribute) {
         NumberField<Integer> nf = new NumberField<Integer>(new IntegerPropertyEditor());
         nf.setAllowBlank(!attribute.isRequired());
         nf.setAllowDecimals(false);
         nf.setAllowNegative(true);
 
         DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
         if (avu != null) {
             nf.setValue(new Integer(avu.getValue()));
         }
 
         return nf;
     }
 
     private NumberField<Double> buildNumberField(MetadataTemplateAttribute attribute) {
         NumberField<Double> nf = new NumberField<Double>(new DoublePropertyEditor());
         nf.setAllowBlank(!attribute.isRequired());
         nf.setAllowDecimals(true);
         nf.setAllowNegative(true);
 
         DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
         if (avu != null) {
             nf.setValue(new Double(avu.getValue()));
         }
 
         return nf;
     }
 
     private FieldLabel buildFieldLabel(IsWidget widget, String lbl, String description,
             boolean allowBlank) {
         FieldLabel fl = new FieldLabel(widget);
         fl.setHTML(buildLabelWithDescription(lbl, description, allowBlank));
         new QuickTip(fl);
         fl.setLabelAlign(LabelAlign.TOP);
         return fl;
     }
 
     private TextArea buildTextArea(MetadataTemplateAttribute attribute) {
         TextArea area = new TextArea();
         area.setAllowBlank(!attribute.isRequired());
         area.setHeight(200);
 
         DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
         if (avu != null) {
             area.setValue(avu.getValue());
         }
 
         return area;
     }
 
     private CheckBox buildBooleanField(MetadataTemplateAttribute attribute) {
         CheckBox cb = new CheckBox();
 
         DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
         if (avu != null) {
             cb.setValue(new Boolean(avu.getValue()));
         }
 
         return cb;
     }
 
     private TextField buildURLField(MetadataTemplateAttribute attribute) {
         TextField tf = buildTextField(attribute);
         tf.addValidator(new UrlValidator());
         return tf;
     }
 
     private DateField buildDateField(MetadataTemplateAttribute attribute) {
         final DateField tf = new DateField(new DateTimePropertyEditor(timestampFormat));
         tf.setAllowBlank(!attribute.isRequired());
 		tf.setEmptyText(timestampFormat.format(new Date(0)));
 
 		DiskResourceMetadata avu = attrAvuMap.get(attribute.getName());
 		if (avu != null) {
             try {
                 tf.setValue(timestampFormat.parse(avu.getValue()));
             } catch (Exception e) {
                 GWT.log(avu.getValue(), e);
             }
 		}
 
         return tf;
     }
 
     /**
      * @param attribute
      * @return Field based on MetadataTemplateAttribute type.
      */
     private Field<?> getAttributeValueWidget(MetadataTemplateAttribute attribute) {
         String type = attribute.getType();
         if (type.equalsIgnoreCase("timestamp")) { //$NON-NLS-1$
             return buildDateField(attribute);
         } else if (type.equalsIgnoreCase("boolean")) { //$NON-NLS-1$
             return buildBooleanField(attribute);
         } else if (type.equalsIgnoreCase("number")) { //$NON-NLS-1$
             return buildNumberField(attribute);
         } else if (type.equalsIgnoreCase("integer")) { //$NON-NLS-1$
             return buildIntegerField(attribute);
         } else if (type.equalsIgnoreCase("string")) { //$NON-NLS-1$
             return buildTextField(attribute);
         } else if (type.equalsIgnoreCase("multiline text")) { //$NON-NLS-1$
             return buildTextArea(attribute);
         } else if (type.equalsIgnoreCase("URL/URI")) { //$NON-NLS-1$
             return buildURLField(attribute);
         } else {
             return null;
         }
     }
 
     private String buildLabelWithDescription(final String label, final String description,
             boolean allowBlank) {
         if (label == null) {
             return null;
         }
         SafeUri infoUri = IplantResources.RESOURCES.info().getSafeUri();
         SafeHtml infoImg = Strings.isNullOrEmpty(description) ? SafeHtmlUtils.fromString("") //$NON-NLS-1$
                 : htmlTemplates.labelInfo(infoUri, description);
         SafeHtml required = allowBlank ? SafeHtmlUtils.fromString("") : htmlTemplates.required(); //$NON-NLS-1$
 
         return htmlTemplates.labelHtml(infoImg, label, required).asString();
     }
 
     public void populateTemplates(List<MetadataTemplateInfo> templates) {
         templateStore.clear();
         templateStore.addAll(templates);
     }
 
     private void initGrid() {
         buildUserMetadataPanel();
         grid = new Grid<DiskResourceMetadata>(createListStore(), createColumnModel());
         userMetadataPanel.add(grid);
         centerPanel.add(userMetadataPanel, new VerticalLayoutData(1, -1));
 
         if (writable) {
             initEditor();
         }
         grid.getSelectionModel().addSelectionChangedHandler(new MetadataSelectionChangedListener());
         new QuickTip(grid);
     }
 
     private void initEditor() {
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
 
         AttributeValidationHandler validationHandler = new AttributeValidationHandler();
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
 
     private void buildUserMetadataPanel() {
         userMetadataPanel = new ContentPanel(appearance);
         userMetadataPanel.setSize("575", "275"); //$NON-NLS-1$ //$NON-NLS-2$
         userMetadataPanel.setCollapsible(true);
         userMetadataPanel.getHeader().addStyleName(ThemeStyles.getStyle().borderTop());
 
         userMetadataPanel.setHeadingHtml(htmlTemplates.boldHeader(I18N.DISPLAY.userMetadata()));
     }
 
     private ListStore<DiskResourceMetadata> createListStore() {
         listStore = new ListStore<DiskResourceMetadata>(new ModelKeyProvider<DiskResourceMetadata>() {
             @Override
             public String getKey(DiskResourceMetadata item) {
                 if (item != null) {
                     return item.getId();
                 } else {
                     return ""; //$NON-NLS-1$
                 }
             }
         });
 
         return listStore;
     }
 
     @UiHandler("deleteMetadataButton")
     void onDeleteMetadataSelected(SelectEvent event) {
         expandUserMetadataPanel();
         for (DiskResourceMetadata md : grid.getSelectionModel().getSelectedItems()) {
             toBeDeleted.add(md);
             listStore.remove(md);
         }
     }
 
     private void expandUserMetadataPanel() {
         if (userMetadataPanel.isCollapsed()) {
             userMetadataPanel.expand();
         }
     }
 
     @UiHandler("addMetadataButton")
     void onAddMetadataSelected(SelectEvent event) {
         expandUserMetadataPanel();
         DiskResourceMetadata md = newMetadata(getUniqeAttrName(I18N.DISPLAY.newAttribute(), 0),
                 I18N.DISPLAY.newValue(), USER_UNIT_TAG);
         md.setId(unique_avu_id++ + ""); //$NON-NLS-1$
         listStore.add(0, md);
         gridInlineEditing.startEditing(new GridCell(0, 0));
         gridInlineEditing.getEditor(grid.getColumnModel().getColumn(0)).validate();
     }
 
     private String getUniqeAttrName(String attrName, int i) {
         String retName = i > 0 ? attrName + "_(" + i + ")" : attrName; //$NON-NLS-1$ //$NON-NLS-2$
         for (DiskResourceMetadata md : listStore.getAll()) {
             if (md.getAttribute().equals(retName)) {
                 return getUniqeAttrName(attrName, ++i);
             }
         }
         return retName;
     }
 
     ColumnModel<MetadataTemplateAttribute> createTemplateColumnModel() {
         List<ColumnConfig<MetadataTemplateAttribute, ?>> columns = Lists.newArrayList();
 
         ColumnModel<MetadataTemplateAttribute> cm = new ColumnModel<MetadataTemplateAttribute>(columns);
         return cm;
     }
 
     private ColumnModel<DiskResourceMetadata> createColumnModel() {
         List<ColumnConfig<DiskResourceMetadata, ?>> columns = Lists.newArrayList();
         DiskResourceMetadataProperties props = GWT.create(DiskResourceMetadataProperties.class);
         ColumnConfig<DiskResourceMetadata, String> attributeColumn = new ColumnConfig<DiskResourceMetadata, String>(
                 props.attribute(), 150, I18N.DISPLAY.attribute());
         ColumnConfig<DiskResourceMetadata, String> valueColumn = new ColumnConfig<DiskResourceMetadata, String>(
                 props.value(), 150, I18N.DISPLAY.paramValue());
 
         metadataCell = new MetadataCell();
         attributeColumn.setCell(metadataCell);
         valueColumn.setCell(metadataCell);
         columns.add(attributeColumn);
         columns.add(valueColumn);
 
         ColumnModel<DiskResourceMetadata> cm = new ColumnModel<DiskResourceMetadata>(columns);
         return cm;
     }
 
     public Set<DiskResourceMetadata> getMetadataToDelete() {
         return toBeDeleted;
     }
 
     public Set<DiskResourceMetadata> getMetadataToAdd() {
         HashSet<DiskResourceMetadata> metaDataToAdd = Sets.newHashSet();
         metaDataToAdd.addAll(listStore.getAll());
 
         if (selectedTemplate != null) {
             metaDataToAdd.add(newMetadata(IPC_METADATA_TEMPLATE_ATTR, selectedTemplate.getId(), "")); //$NON-NLS-1$
 
             for (String attr : templateAttrFieldMap.keySet()) {
                 Field<?> field = templateAttrFieldMap.get(attr);
                 if (field.isValid() && field.getValue() != null
                         && !field.getValue().toString().isEmpty()) {
                     String value = field.getValue().toString();
                     if (field instanceof DateField) {
                         value = timestampFormat.format(((DateField)field).getValue());
                     }
                     metaDataToAdd.add(newMetadata(attr, value, "")); //$NON-NLS-1$
                 }
             }
         }
 
         return metaDataToAdd;
     }
 
     private DiskResourceMetadata newMetadata(String attr, String value, String unit) {
         DiskResourceMetadata avu = autoBeanFactory.metadata().as();
 
         avu.setAttribute(attr);
         avu.setValue(value);
         avu.setUnit(unit);
 
         return avu;
     }
 
     private final class MetadataCell extends AbstractCell<String> {
 
         @Override
         public void render(Context context, String value, SafeHtmlBuilder sb) {
             if (!Strings.isNullOrEmpty(value)) {
                 sb.append(htmlTemplates.cell(value));
             }
         }
     }
 
     private final class MetadataSelectionChangedListener implements
             SelectionChangedHandler<DiskResourceMetadata> {
 
         @Override
         public void onSelectionChanged(SelectionChangedEvent<DiskResourceMetadata> event) {
             deleteMetadataButton.setEnabled(event.getSelection().size() > 0 && writable);
             if (gridInlineEditing != null) {
                 gridInlineEditing.completeEditing();
             }
 
         }
     }
 
     private final class AttributeValidationHandler implements ValidHandler, InvalidHandler {
 
         public AttributeValidationHandler() {
         }
 
         @Override
         public void onValid(ValidEvent event) {
             valid = true;
         }
 
         @Override
         public void onInvalid(InvalidEvent event) {
             valid = false;
         }
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     public void loadMetadata(DiskResourceMetadataList metadataList) {
         List<DiskResourceMetadata> metadata = metadataList.getMetadata();
         attrAvuMap.clear();
 
         DiskResourceMetadata templateAvu = null;
         for (DiskResourceMetadata avu : metadata) {
             avu.setId(unique_avu_id++ + ""); //$NON-NLS-1$
             String attribute = avu.getAttribute();
             attrAvuMap.put(attribute, avu);
 
             if (attribute.equalsIgnoreCase(IPC_METADATA_TEMPLATE_ATTR)) {
                 templateAvu = avu;
             }
         }
 
         listStore.clear();
         listStore.commitChanges();
         listStore.addAll(metadata);
 
         if (templateAvu != null) {
             listStore.remove(templateAvu);
             selectedTemplate = templateStore.findModelWithKey(templateAvu.getValue());
             templateCombo.setValue(selectedTemplate);
             onTemplateSelected(selectedTemplate);
         }
     }
 
     public boolean isValid() {
         if (selectedTemplate != null && templateForm != null) {
             List<IsField<?>> fields = FormPanelHelper.getFields(templateForm);
             for (IsField<?> f : fields) {
                 if (!f.isValid(false)) {
                     valid = false;
                     return valid;
                 }
                 valid = true;
             }
            
         }
         return valid;
     }
 
     private void buildTemplateContainer() {
         centerPanel.clear();
         alc.clear();
         alc.setExpandMode(ExpandMode.SINGLE);
         buildTemplatePanel();
         buildUserMetadataPanel();
         // must re add the grid
         userMetadataPanel.add(grid);
         alc.add(templateForm);
         alc.add(userMetadataPanel);
         alc.setActiveWidget(templateForm);
         centerPanel.add(alc, new VerticalLayoutData(1, -1));
 
     }
 
     private void buildTemplatePanel() {
         templateForm = new ContentPanel(appearance);
         templateForm.setBodyStyle("background-color: #fff; padding: 5px"); //$NON-NLS-1$
         templateForm.setSize("575", "275"); //$NON-NLS-1$ //$NON-NLS-2$
         templateForm.setHeadingHtml(htmlTemplates.boldHeader(templateCombo.getCurrentValue().getName()));
         templateForm.getHeader().addStyleName(ThemeStyles.getStyle().borderTop());
         templateContainer = new VerticalLayoutContainer();
         templateContainer.setScrollMode(ScrollMode.AUTOY);
         templateForm.add(templateContainer);
         // need this to be set manually to avoid renderer assertion error
         templateForm.setCollapsible(true);
         // end temp fix
     }
 
 }
