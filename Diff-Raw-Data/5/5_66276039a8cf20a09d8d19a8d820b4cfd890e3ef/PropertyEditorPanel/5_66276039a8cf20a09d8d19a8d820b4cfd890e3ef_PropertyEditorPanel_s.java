 package org.iplantc.js.integrate.client.panels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.validator.IPlantValidator;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.metadata.client.PropertyType;
 import org.iplantc.core.metadata.client.property.DataObject;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.property.PropertyTypeCategory;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroupContainer;
 import org.iplantc.core.metadata.client.validation.MetaDataValidator;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.js.integrate.client.I18N;
 import org.iplantc.js.integrate.client.events.CommandLineArgumentChangeEvent;
 import org.iplantc.js.integrate.client.events.JSONMetaDataObjectChangedEvent;
 import org.iplantc.js.integrate.client.services.EnumerationServices;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ListBox;
 
 /**
  * Panel for editing property specific data.
  * 
  * @author amuir
  * 
  */
 public class PropertyEditorPanel extends ContentPanel {
     private final Property property;
     private LayoutContainer containerMain;
     private LayoutContainer containerPropertyTypeEditor;
     private VerticalPanel pnlWidget;
 
     private HashMap<String, List<PropertyType>> propertyTypes; // category, property types
 
     private LayoutContainer pnlDefaultValue;
     private CheckBox ckboxDisplayInGui;
     private CheckBox cbxOptionFlag;
     private LayoutContainer pnlToolTip;
     private ComboBox<CategoryListItem> comboPropertyTypeCategory;
     private ListBox listPropertyType;
     private TextFieldContainer pnlPropertyLabel;
     private ValidationPanel pnlValidation;
     private LayoutContainer pnlCommandLineOption;
     private LayoutContainer pnlBottom;
     private CheckBox cbxRequired;
   
 
     private static final String DEFAULT_BOOLEAN = "false"; //$NON-NLS-1$
     private static final String DEFAULT_STRING = ""; //$NON-NLS-1$
 
     /**
      * Instantiate from the property group container and property to be edited.
      * 
      * @param container parent property group container (needed for validating against external fields).
      * @param property property to be edited.
      */
     public PropertyEditorPanel(final PropertyGroupContainer container, final Property property) {
         this.property = property;
 
         init();
 
         buildInstanceWidgets(container);
     }
 
     private void init() {
         setHeaderVisible(false);
         setBodyStyle("background-color: #EDEDED"); //$NON-NLS-1$
     }
 
     private void buildInstanceWidgets(final PropertyGroupContainer container) {
         buildPropertyTypeCategoryList();
         buildDefaultValuePanel();
         buildGUIEnabledCheckbox();
         buildOptionFlagCheckbox();
         buildRequiredCheckbox();
         buildToolTipPanel();
         buildCommandLineOptionPanel();
         buildBottomPanel();
         buildValidationPanel(container);
         buildPropertyLabel();
 
         // This should be called last, since it will build the propertyTypes map and call
         // initFromPropertyCategory, which will make a selection in the PropertyTypeCategory list, which
         // will call handlePropertyCategoryChange, which configures many other widgets in this panel that
         // should be initialized before this method call.
         buildWidgetPanel();
     }
 
     private void buildCommandLineOptionPanel() {
         String caption = I18N.DISPLAY.flag();
 
         TextField<String> field = buildTextField(property.getName(), 128, new FlagEditKeyUpCommand(),
                 true);
         IPlantValidator.setRegexRestrictedCmdLineChars(field, caption);
 
         pnlCommandLineOption = buildTextField(caption, field);
     }
 
     private void buildDefaultValuePanel() {
         pnlDefaultValue = new VerticalPanel();
 
         pnlDefaultValue.add(new StringDefaultValuePanel(property.getValue()));
     }
 
     private void buildBottomPanel() {
         pnlBottom = new LayoutContainer();
     }
 
     private void buildValidationPanel(final PropertyGroupContainer container) {
         pnlValidation = new ValidationPanel(container, property);
     }
 
     private void firePropertyChangedEvent() {
         EventBus.getInstance().fireEvent(new JSONMetaDataObjectChangedEvent(property));
     }
 
     private void fireCommandLineArgumentChangeEvent() {
         EventBus.getInstance().fireEvent(new CommandLineArgumentChangeEvent(property));
     }
 
     /**
      * Provide an instance of the NumberField GXT widget with the arguments set.
      * 
      * @param value the number value
      * @param width the desired width of the field
      * @param cmdKeyUp the command to execute when the on key up event fires
      * @param setFocus whether the field should have focus
      * @param changeListener a listener to fire when the field's value changes; can be null
      * @return a configured instance of the NumberField GXT widget
      */
     private NumberField buildNumberField(final Number value, int width, final KeyUpCommand cmdKeyUp,
             boolean setFocus, boolean onlyPositiveValues) {
         final NumberField ret = new NumberField();
 
         ret.setWidth(width);
         ret.setSelectOnFocus(setFocus);
 
         if (value != null) {
             ret.setValue(value);
         }
 
         if (cmdKeyUp != null) {
             ret.addKeyListener(new KeyListener() {
                 @Override
                 public void componentKeyUp(ComponentEvent event) {
                     // always send the raw value so validation can tell no input from invalid input
                     cmdKeyUp.execute(ret.getRawValue());
                 }
             });
         }
 
         if (setFocus) {
             ret.focus();
         }
 
         if (onlyPositiveValues) {
             ret.setMinValue(0);
             ret.setAllowNegative(false);
             ret.setAllowDecimals(false);
         }
 
         return ret;
     }
 
     /**
      * Constructs a NumberField for usage by a container.
      * 
      * @param caption text to be used as a caption
      * @param value numeric value to be shown
      * @param width the desired width of the field
      * @param cmdKeyUp the command to execute when the on key up event fires
      * @param setFocus whether the field should have focus
      * @param changeListener a listener to fire when the field's value changes; can be null
      * @return a configured instance of the NumberField GXT widget
      */
     private LayoutContainer buildNumberField(final String caption, final Number value, int width,
             final KeyUpCommand cmdKeyUp, boolean setFocus) {
         LayoutContainer ret = new LayoutContainer();
         
         ret.add(new Label(caption + ":")); //$NON-NLS-1$
         ret.add(buildNumberField(value, width, cmdKeyUp, setFocus, false));
         
         return ret;
     }
 
     private TextFieldContainer buildTextField(final String caption, final String value, int width,
             final KeyUpCommand cmdKeyUp) {
         return buildTextField(caption, buildTextField(value, width, cmdKeyUp, false));
     }
 
     private TextFieldContainer buildTextField(final String caption, TextField<String> field) {
         Label label = new Label(caption + ":"); //$NON-NLS-1$
         return new TextFieldContainer(label, field);
     }
 
     private TextField<String> buildTextField(final String value, int width, final KeyUpCommand cmdKeyUp,
             boolean setFocus) {
         final TextField<String> ret = new TextField<String>();
 
         ret.setWidth(width);
         ret.setSelectOnFocus(true);
         ret.setValue(value);
         ret.setAutoValidate(true);
 
         if (cmdKeyUp != null) {
             ret.addKeyListener(new KeyListener() {
                 @Override
                 public void componentKeyUp(ComponentEvent event) {
                     cmdKeyUp.execute(ret.getValue());
                 }
             });
         }
 
         if (setFocus) {
             ret.focus();
         }
 
         return ret;
     }
 
     private void buildPropertyTypeCategoryList() {
         comboPropertyTypeCategory = new ComboBox<CategoryListItem>();
         comboPropertyTypeCategory.setWidth("140px"); //$NON-NLS-1$
         comboPropertyTypeCategory.setEditable(false);
 
         comboPropertyTypeCategory.setFireChangeEventOnSetValue(true);
         comboPropertyTypeCategory.setForceSelection(true);
         comboPropertyTypeCategory.setTriggerAction(TriggerAction.ALL);
 
         ListStore<CategoryListItem> store = new ListStore<CategoryListItem>();
         comboPropertyTypeCategory.setStore(store);
 
         for (PropertyTypeCategory category : PropertyTypeCategory.values()) {
             store.add(new CategoryListItem(category));
         }
 
         // NOTE: We don't select a default category here, since the category will be selected from the
         // property type in initFromPropertyCategory, which is only called after the propertyTypes map is
         // built from a service call.
         // Also, this listener is only fired if the category changes, which won't happen if we select a
         // default category that is the same for the given property type, but we need this listener to
         // fire from the selection made in initFromPropertyCategory since handlePropertyCategoryChange
         // will correctly initialize the remaining widgets in this panel (which also depend on an
         // initialized propertyTypes map).
         comboPropertyTypeCategory
                 .addSelectionChangedListener(new SelectionChangedListener<CategoryListItem>() {
                     @Override
                     public void selectionChanged(SelectionChangedEvent<CategoryListItem> se) {
                         PropertyTypeCategory category = getSelectedPropertyTypeCategory();
                         if (category != null) {
                             handlePropertyCategoryChange(category);
                         }
                         
                         // changing the category resets the default value, so fire a change event
                         EventBus.getInstance().fireEvent(new CommandLineArgumentChangeEvent(property));
                     }
                 });
     }
 
     private PropertyTypeCategory getSelectedPropertyTypeCategory() {
         List<CategoryListItem> selection = comboPropertyTypeCategory.getSelection();
 
         if (selection != null && selection.size() > 0) {
             CategoryListItem categoryModel = selection.get(0);
 
             if (categoryModel != null) {
                 return categoryModel.getCategory();
             }
         }
 
         return null;
     }
 
     private LayoutContainer buildPropertyTypeDropdown() {
         VerticalPanel ret = new VerticalPanel();
 
         ret.add(new Label(I18N.DISPLAY.parameterType() + ": ")); //$NON-NLS-1$
         ret.add(comboPropertyTypeCategory);
 
         return ret;
     }
 
     private void updateValidationPanel(PropertyTypeCategory category) {
         if (containerPropertyTypeEditor.isEnabled()) {
             pnlValidation.setEnabled(category != PropertyTypeCategory.BOOLEAN);
         }
 
         pnlValidation.reset(category);
     }
 
     private void handlePropertyCategoryChange(final PropertyTypeCategory category) {
         // Check if the current default value is already valid for the new category.
         // If editing a saved property, or if the user only navigated away from this property then back,
         // the value should not be reset.
         boolean resetDefault = true;
 
         String propertyType = property.getType();
         List<PropertyType> types = propertyTypes.get(category.toString());
 
         for (PropertyType type : types) {
             if (type.getName().equals(propertyType)) {
                 // The property's type is valid for this category, so don't reset it's default value.
                 resetDefault = false;
                 break;
             }
         }
 
         if (resetDefault) {
             resetPropertyValue(category);
         }
 
         updateEditorPanel(category);
 
         updatePropertyLabel(category);
 
         updateDefaultValuePanel(category);
 
         updateValidationPanel(category);
 
         updatePropertyTypesToListBox();
     }
 
     private void updateDataObjectFromProperty() {
         DataObject dataObject = property.getDataObject();
 
         if (dataObject == null) {
             dataObject = new DataObject();
             property.setDataObject(dataObject);
         }
 
         dataObject.setName(property.getLabel());
         dataObject.setLabel(property.getLabel());
         dataObject.setCmdSwitch(property.getName());
         dataObject.setDescription(property.getDescription());
         dataObject.setType(property.getType());
         dataObject.setOrder(property.getOrder());
         dataObject.setVisible(property.isVisible());
     }
 
     /**
      * Resets the property value to the default for the given type category.
      * 
      * @param category The category that determines what default property value to use.
      */
     private void resetPropertyValue(PropertyTypeCategory category) {
         switch (category) {
             case BOOLEAN:
                 property.setValue(DEFAULT_BOOLEAN);
                 break;
 
             default:
                 property.setValue(DEFAULT_STRING);
                 break;
         }
     }
 
     /**
      * Replace the contents of the center panel.
      * 
      * @param container a new component to set in the center of the BorderLayout.
      */
     private void updateEditorPanel(final PropertyTypeCategory category) {
         if (containerMain != null && containerPropertyTypeEditor != null) {
             containerMain.remove(containerPropertyTypeEditor);
         }
 
         switch (category) {
             case INPUT:
                 ckboxDisplayInGui.setEnabled(false);
                 cbxOptionFlag.setEnabled(true);
                 cbxRequired.setEnabled(true);
 
                 property.setType(DataObject.INPUT_TYPE);
                 property.setVisible(true);
 
                 updateDataObjectFromProperty();
 
                 containerPropertyTypeEditor = new InputDataObjectFormPanel(property.getDataObject());
                 break;
 
             case OUTPUT:
                 ckboxDisplayInGui.setEnabled(false);
                 cbxOptionFlag.setEnabled(false);
                 cbxOptionFlag.setValue(true);
                 cbxRequired.setEnabled(false);
 
                 property.setType(DataObject.OUTPUT_TYPE);
                 property.setVisible(false);
 
                 updateDataObjectFromProperty();
 
                 OutputDataObjectFormPanel outputPnl = new OutputDataObjectFormPanel(
                         property.getDataObject());
                 outputPnl.setOutputFilenameChangeCommand(buildFilenameChangeCommand());
                 containerPropertyTypeEditor = outputPnl;
                 break;
 
             case BOOLEAN:
                 ckboxDisplayInGui.setEnabled(true);
                 cbxOptionFlag.setEnabled(false);
                 cbxRequired.setEnabled(false);
                 containerPropertyTypeEditor = pnlWidget;
                 break;
 
             default:
                 ckboxDisplayInGui.setEnabled(true);
                 cbxOptionFlag.setEnabled(true);
                 cbxRequired.setEnabled(true);
                 containerPropertyTypeEditor = pnlWidget;
                 break;
         }
 
         if (containerMain != null) {
             containerMain.add(containerPropertyTypeEditor);
         }
 
         // make sure we start with the GUI widgets in the correct state
         ckboxDisplayInGui.setValue(property.isVisible());
         setGuiWidgetsEnabled(property.isVisible());
     }
 
     /**
      * Returns a command that updates the navigation tree when the filename field on the output panel
      * changes its value.
      * 
      * @return a command
      */
     private Command buildFilenameChangeCommand() {
         return new Command() {
             @Override
             public void execute() {
                 fireLabelChangeEvent();
             }
         };
     }
 
     /**
      * Makes the "Label" field visible or invisible depending on the category, and fires an event to
      * update the navigation tree.
      * 
      * @param category
      */
     private void updatePropertyLabel(PropertyTypeCategory category) {
         // show the "Label" field for all categories but output
         pnlPropertyLabel.setVisible((category != PropertyTypeCategory.OUTPUT));
 
         // send an event to update the label in the tree if we're switching from or to OUTPUT
         fireLabelChangeEvent();
     }
 
     /**
      * Updates the navigation tree to show the filename (if category=OUTPUT) or the label (for all other
      * categories).
      */
     private void fireLabelChangeEvent() {
         PropertyTypeCategory category = getSelectedPropertyTypeCategory();
         String newLabel;
         if (category == PropertyTypeCategory.OUTPUT) {
             newLabel = ((OutputDataObjectFormPanel)containerPropertyTypeEditor).getOutputFilename();
         }
         else {
             newLabel = pnlPropertyLabel.field.getValue();
         }
         new LabelEditKeyUpCommand().execute(newLabel);
     }
     
     private void buildOptionFlagCheckbox() {
         cbxOptionFlag = new CheckBox();
 
         cbxOptionFlag.setBoxLabel(I18N.DISPLAY.passFlag());
 
         cbxOptionFlag.setValue(property.isOmit_if_blank());
       
         // add our change listener
         cbxOptionFlag.addListener(Events.Change, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(final BaseEvent be) {
                 handleOptionaFlagCheckboxChanged(cbxOptionFlag.getValue());
             }
         });
         
     }
 
     private void handleOptionaFlagCheckboxChanged(Boolean value) {
         property.setOmit_if_blank(value);
     }
     
     
     private void buildRequiredCheckbox() {
         cbxRequired = new CheckBox();
 
         cbxRequired.setBoxLabel(I18N.DISPLAY.userInputRequired());
 
         // set our initial value
         MetaDataValidator validator = property.getValidator();
 
         if (validator != null) {
             cbxRequired.setValue(validator.isRequired());
             updateOptionFlag(validator.isRequired());
         }
 
         // add our change listener
         cbxRequired.addListener(Events.Change, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(final BaseEvent be) {
                 handleRequiredCheckboxChanged(cbxRequired.getValue());
             }
         });
     }
 
     private void handleRequiredCheckboxChanged(boolean value) {
         // add rule to validator
         MetaDataValidator validator = property.getValidator();
 
         if (validator != null) {
             validator.setRequired(value);
         } else {
             // safety check - this should always be true if our
             if (value) {
                 validator = new MetaDataValidator();
                 validator.setRequired(value);
                 property.setValidator(validator);
             }
         }
         
         if(property.getDataObject() != null) {
             property.getDataObject().setRequired(value);
         }
         
        updateOptionFlag(value);
     }
 
     private void updateOptionFlag(boolean required) {
         cbxOptionFlag.setEnabled(!required && cbxRequired.isEnabled());
         cbxOptionFlag.setValue(!required);
         property.setOmit_if_blank(!required);
     }
 
     /**
      * Clears the default value panel and rebuilds its value field with the current property value.
      * 
      * @param category The category that determines what field to add to the default value panel.
      */
     private void updateDefaultValuePanel(PropertyTypeCategory category) {
         pnlDefaultValue.removeAll();
 
         switch (category) {
             case STRING:
                 pnlDefaultValue.add(new StringDefaultValuePanel(property.getValue()));
                 break;
 
             case NUMBER:
                 pnlDefaultValue.add(new NumberDefaultValuePanel(property.getValue()));
                 break;
 
             case BOOLEAN:
                 pnlDefaultValue.add(new BooleanDefaultValuePanel(property.getValue()));
                 break;
 
             default:
                 break;
         }
 
         pnlDefaultValue.layout();
     }
 
     private void buildGUIEnabledCheckbox() {
         ckboxDisplayInGui = new CheckBox();
 
         ckboxDisplayInGui.setBoxLabel(I18N.DISPLAY.displayInGUI());
         ckboxDisplayInGui.setValue(property.isVisible());
 
         ckboxDisplayInGui.addListener(Events.Change, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(final BaseEvent be) {
                 handleUICheckboxChanged(ckboxDisplayInGui.getValue());
             }
         });
     }
 
     private void buildToolTipPanel() {
         pnlToolTip = buildTextField(I18N.DISPLAY.toolTipText(), property.getDescription(), 480,
                 new DescriptionEditKeyUpCommand());
     }
 
     private void updateValidationPanelOnUIChange(boolean displayUI) {
         // we do not enable the validation panel if the category is Boolean
         if (displayUI) {
             // get our category
             PropertyTypeCategory category = getSelectedPropertyTypeCategory();
             if (category == PropertyTypeCategory.BOOLEAN) {
                 pnlValidation.disable();
             }
         }
     }
 
     private void handleUICheckboxChanged(boolean checked) {
         property.setVisible(checked);
         setGuiWidgetsEnabled(checked);
     }
 
     private void setGuiWidgetsEnabled(boolean enabled) {
         cbxRequired.setEnabled(enabled);
         if (!enabled) {
             cbxRequired.setValue(false);
         }
 
         pnlToolTip.setEnabled(enabled);
         pnlWidget.setEnabled(enabled);
 
         updateValidationPanelOnUIChange(enabled);
     }
 
     private void updatePropertyTypesToListBox() {
         // reset our list box
         listPropertyType.clear();
 
         PropertyTypeCategory category = getSelectedPropertyTypeCategory();
 
         String typeDest = property.getType();
 
         int idx = 0; // keep track of index for setting selection
         int idxSelected = 0;
 
         List<PropertyType> types = propertyTypes.get(category.toString());
 
         for (PropertyType type : types) {
             // add our item to the listbox
             listPropertyType.addItem(type.getDescription());
 
             // is this our desired selection?
             if (type.getName().equals(typeDest)) {
                 idxSelected = idx;
             }
 
             idx++;
         }
 
         // set our selection
         if (listPropertyType.getItemCount() > 0) {
             listPropertyType.setSelectedIndex(idxSelected);
         }
 
         // force type change
         handleWidgetTypeChange();
     }
 
     private String getCategoryFromType(final String propertyType) {
         if (DataObject.INPUT_TYPE.equals(propertyType)) {
             return PropertyTypeCategory.INPUT.toString();
         }
         if (DataObject.OUTPUT_TYPE.equals(propertyType)) {
             return PropertyTypeCategory.OUTPUT.toString();
         }
 
         String ret = ""; // assume failure //$NON-NLS-1$
 
         for (String category : propertyTypes.keySet()) {
             List<PropertyType> types = propertyTypes.get(category);
 
             for (PropertyType type : types) {
                 if (type.getName().equals(propertyType)) {
                     ret = category;
                     break;
                 }
             }
         }
 
         return ret;
     }
 
     private void initFromPropertyCategory() {
         String category = getCategoryFromType(property.getType());
 
         ListStore<CategoryListItem> store = comboPropertyTypeCategory.getStore();
 
         // assume failure... default to first item
         CategoryListItem selectedCategory = store.getAt(0);
 
         for (CategoryListItem categoryModel : store.getModels()) {
             if (categoryModel.getDisplay().equals(category)) {
                 selectedCategory = categoryModel;
                 break;
             }
         }
 
         comboPropertyTypeCategory.setValue(selectedCategory);
     }
 
     private void initPropertyTypes() {
         propertyTypes = new HashMap<String, List<PropertyType>>();
 
         for (PropertyTypeCategory category : PropertyTypeCategory.values()) {
             propertyTypes.put(category.toString(), new ArrayList<PropertyType>());
         }
     }
 
     private void populateWidgetTypeList() {
         mask(I18N.DISPLAY.loadingMask());
 
         EnumerationServices services = new EnumerationServices();
         services.getWidgetTypes(new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 if (result != null) {
                     initPropertyTypes();
 
                     JSONArray property_types = JsonUtil.getArray(JsonUtil.getObject(result),
                             "property_types"); //$NON-NLS-1$
 
                     if (property_types != null) {
                         for (int i = 0,len = property_types.size(); i < len; i++) {
                             PropertyType typeProperty = new PropertyType(JsonUtil.getObjectAt(
                                     property_types, i));
 
                             List<PropertyType> list = propertyTypes.get(typeProperty.getType());
                             list.add(typeProperty);
                         }
 
                         // now that we've populated our property types list, select the correct
                         // category,
                         // which will init the correct widgets, including the property types list box
                         initFromPropertyCategory();
                     } else {
                         ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes());
                     }
                 } else {
                     ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes());
                 }
 
                 unmask();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.DISPLAY.cantLoadWidgetTypes(), caught);
                 unmask();
             }
         });
     }
 
     private LayoutContainer buildWidgetTypeDropdown() {
         VerticalPanel ret = new VerticalPanel();
 
         listPropertyType = new ListBox();
         listPropertyType.addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
                 handleWidgetTypeChange();
             }
         });
 
         populateWidgetTypeList();
 
         ret.add(new Label(I18N.DISPLAY.typeOfFieldNeeded() + ": ")); //$NON-NLS-1$
         ret.add(listPropertyType);
 
         return ret;
     }
 
     private void buildPropertyLabel() {
         pnlPropertyLabel = buildTextField(I18N.DISPLAY.label(), property.getLabel(), 255,
                 new LabelEditKeyUpCommand());
     }
 
     private void updatePanelsAfterWidgetTypeChange(final PropertyTypeCategory category,
             boolean isSelectionWidget) {
         pnlCommandLineOption.setEnabled(!isSelectionWidget);
 
         updateDefaultValuePanel(category);
         pnlDefaultValue.setEnabled(!isSelectionWidget);
 
         pnlBottom.removeAll();
 
         if (isSelectionWidget) {
             pnlBottom.add(new ListboxEditorPanel(category.toString(), property));
         } else {
             pnlBottom.add(pnlValidation);
         }
 
         layout();
     }
 
     private void handleWidgetTypeChange() {
         boolean isSelectionWidget = false;
 
         PropertyTypeCategory category = getSelectedPropertyTypeCategory();
 
         if (listPropertyType.getItemCount() > 0) {
             List<PropertyType> types = propertyTypes.get(category.toString());
 
             PropertyType type = types.get(listPropertyType.getSelectedIndex());
 
             // TODO: find alternatives for hardcoding
             String typeName = type.getName();
             if (typeName.equalsIgnoreCase("selection") || typeName.equalsIgnoreCase("valueselection")) { //$NON-NLS-1$ //$NON-NLS-2$
                 isSelectionWidget = true;
             }
 
             property.setType(typeName);
         }
 
         updatePanelsAfterWidgetTypeChange(category, isSelectionWidget);
     }
 
     private void buildWidgetPanel() {
         pnlWidget = new VerticalPanel();
         pnlWidget.setSpacing(8);
 
         pnlWidget.add(buildWidgetTypeDropdown());
 
         pnlWidget.add(pnlBottom);
     }
 
     private LayoutContainer buildPanel() {
         VerticalPanel ret = new VerticalPanel();
         ret.setStyleAttribute("background-color", "#EDEDED"); //$NON-NLS-1$ //$NON-NLS-2$
         ret.setSpacing(8);
 
         ret.add(pnlCommandLineOption);
 
         ret.add(buildPropertyTypeDropdown());
 
         ret.add(pnlPropertyLabel);
 
         ret.add(pnlDefaultValue);
 
         ret.add(ckboxDisplayInGui);
         ret.add(cbxOptionFlag);
         ret.add(cbxRequired);
 
         ret.add(pnlToolTip);
 
         if (containerPropertyTypeEditor != null) {
             ret.add(containerPropertyTypeEditor);
         }
 
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int pos) {
         super.onRender(parent, pos);
 
         containerMain = buildPanel();
         add(containerMain);
     }
 
     /**
      * Determines if the string contains a double precision numeric value.
      * 
      * @param test string that may or may not contain a number
      * @return true if the string contains a double; otherwise false.
      */
     private boolean isDouble(String test) {
         boolean ret = false; // assume failure
 
         try {
             if (test != null) {
                 Double.parseDouble(test);
 
                 // if we get here, we know parseDouble succeeded
                 ret = true;
             }
         } catch (NumberFormatException nfe) {
             // we are assuming false - setting the return value here would be redundant
         }
 
         return ret;
     }
 
     /**
      * Determines if the string contains a number formatted as an integer value.
      * 
      * @param test string that may or may not contain an integer
      * @return true if the string contains a number formatted as an integer; otherwise false.
      */
     private boolean isInt(String test) {
         if (test == null) {
             return false;
         }
 
         try {
             Integer.parseInt(test);
         } catch (NumberFormatException nfe) {
             return false;
         }
 
         return true;
     }
 
     private interface KeyUpCommand {
         void handleNullInput();
 
         void execute(String value);
     }
 
     private class FlagEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             property.setName(value);
 
             updateDataObjectFromProperty();
             fireCommandLineArgumentChangeEvent();
         }
 
         @Override
         public void handleNullInput() {
             property.setName(DEFAULT_STRING);
 
             updateDataObjectFromProperty();
             fireCommandLineArgumentChangeEvent();
         }
     }
 
     private class ValueEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             property.setValue(value);
             fireCommandLineArgumentChangeEvent();
         }
 
         @Override
         public void handleNullInput() {
             property.setValue(DEFAULT_STRING);
             fireCommandLineArgumentChangeEvent();
         }
     }
 
     /**
      * Command for handling the entry of numeric values.
      * 
      * This command only handles KeyUp events.
      */
     private class NumberValueEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             property.setValue(value);
             fireCommandLineArgumentChangeEvent();
         }
 
         @Override
         public void handleNullInput() {
             property.setValue(DEFAULT_STRING);
             fireCommandLineArgumentChangeEvent();
         }
     }
 
     private class LabelEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             if (value == null) {
                 value = ""; //$NON-NLS-1$
             }
             property.setLabel(value);
 
             updateDataObjectFromProperty();
             fireCommandLineArgumentChangeEvent();
             firePropertyChangedEvent();
         }
 
         @Override
         public void handleNullInput() {
             property.setLabel(DEFAULT_STRING);
 
             updateDataObjectFromProperty();
             fireCommandLineArgumentChangeEvent();
             firePropertyChangedEvent();
         }
     }
 
     private class DescriptionEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             property.setDescription(value);
 
             updateDataObjectFromProperty();
         }
 
         @Override
         public void handleNullInput() {
             property.setDescription(DEFAULT_STRING);
 
             updateDataObjectFromProperty();
         }
     }
 
     private class StringDefaultValuePanel extends VerticalPanel {
         public StringDefaultValuePanel(final String value) {
             String caption = I18N.DISPLAY.defaultValueLabel();
 
             TextField<String> field = buildTextField(value, 255, new ValueEditKeyUpCommand(), false);
 
             IPlantValidator.setRegexRestrictedArgValueChars(field, caption);
 
             add(buildTextField(caption, field));
         }
     }
 
     private class BooleanDefaultValuePanel extends VerticalPanel {
         public BooleanDefaultValuePanel(final String value) {
             add(new Label(I18N.DISPLAY.defaultValue()));
             add(buildBooleanListBox(value));
         }
 
         private ListBox buildBooleanListBox(final String value) {
             final ListBox ret = new ListBox();
             ret.setWidth("140px"); //$NON-NLS-1$
             ret.addItem(I18N.DISPLAY.propertyEditorTrue());
             ret.addItem(I18N.DISPLAY.propertyEditorFalse());
 
             int idxSelected = 0;
 
             if (!value.equals("true")) { //$NON-NLS-1$
                 property.setValue(DEFAULT_BOOLEAN);
                 idxSelected = 1;
             }
 
             ret.setSelectedIndex(idxSelected);
 
             ret.addChangeHandler(new ChangeHandler() {
                 @Override
                 public void onChange(ChangeEvent arg0) {
                     String text = ret.getItemText(ret.getSelectedIndex());
 
                     text = (text.equals(I18N.DISPLAY.propertyEditorTrue())) ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
                     property.setValue(text);
                 }
             });
 
             return ret;
         }
     }
 
     /**
      * User interface for representing a default value that is a number.
      */
     private class NumberDefaultValuePanel extends VerticalPanel {
         public NumberDefaultValuePanel(String value) {
             add(buildNumberField(I18N.DISPLAY.defaultValue(), parseNumberFromString(value), 64,
                     new NumberValueEditKeyUpCommand(), false));
         }
 
         /**
          * Parses value into an int or a double, depending on the format of the given string, or the
          * default double value if the string cannot be parsed as a number.
          * 
          * @param value Number as a string to parse
          * @return Either the int or double value of the given string
          */
         private Number parseNumberFromString(String value) {
             Number numVal;
 
             if (isInt(value)) {
                 numVal = Integer.parseInt(value);
             } else if (isDouble(value)) {
                 numVal = Double.parseDouble(value);
             } else {
                 // it's not an int or a double
                 numVal = null;
                 property.setValue(DEFAULT_STRING);
             }
 
             return numVal;
         }
     }
 
     /**
      * A BaseModelData class for the Property types ComboBox ListStore.
      * 
      * @author psarando
      * 
      */
     protected class CategoryListItem extends BaseModelData {
         private static final long serialVersionUID = 4583897203393642484L;
 
         public CategoryListItem(PropertyTypeCategory category) {
             set("text", category.toString()); //$NON-NLS-1$
             set("value", category); //$NON-NLS-1$
         }
 
         public String getDisplay() {
             return get("text"); //$NON-NLS-1$
         }
 
         public PropertyTypeCategory getCategory() {
             return get("value"); //$NON-NLS-1$
         }
         
         @Override
         public String toString() {
             return getDisplay() + " " + getCategory(); //$NON-NLS-1$
         }
     }
 
     /**
      * A simple subclass of LayoutContainer that holds a label and a text field, and provides access to
      * the field.
      * 
      * @author hariolf
      * 
      */
     private static class TextFieldContainer extends LayoutContainer {
         TextField<String> field;
 
         private TextFieldContainer(Label label, TextField<String> field) {
             this.field = field;
 
             add(label);
             add(field);
         }
     }
 }
