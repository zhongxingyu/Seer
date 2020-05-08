 package org.iplantc.core.tito.client.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.BoundedTextField;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.metadata.client.property.DataObject;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.models.FileFormat;
 import org.iplantc.core.tito.client.models.InfoType;
 import org.iplantc.core.tito.client.models.JsInfoType;
 import org.iplantc.core.tito.client.services.EnumerationServices;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.Radio;
 import com.extjs.gxt.ui.client.widget.form.RadioGroup;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * 
  * A form to collect data objects from tool integrators
  * 
  * @author sriram
  * 
  */
 public abstract class DataObjectFormPanel extends VerticalPanel {
     private static final String ID_FLD_OP_NAME = "idFldOpName";
 
 	private static final String ID_INFO_TYPE_CBO = "idInfoTypeCbo";
 
 	private static final String ID_RADIO_MANY = "idRadioMany";
 
 	private static final String ID_RADIO_FOLDER = "idRadioFolder";
 
 	private static final String ID_RADIO_ONE = "idRadioOne";
 
 	protected final DataObject data;
 
     protected RadioGroup multiplicityGroup;
     protected ComboBox<InfoType> infoTypeField;
     protected Grid<FileFormat> formatField;
     protected TextField<String> outputFileNameField;
     private Command outputFilenameChangeCommand;
 
     /**
      * Create a new instance of DataObjectFormPanel
      * 
      * @param obj instance of DataObject
      * @param paramType DataObject type - input or output
      */
     public DataObjectFormPanel(DataObject obj) {
         data = obj;
 
         init();
         initForm();
 
         initFieldValues(obj);
 
         initInfoTypes();
     }
 
     /**
      * Sets a command that will be called when the "output file" field changes.
      * 
      * @param cmd
      */
     public void setOutputFilenameChangeCommand(Command cmd) {
         outputFilenameChangeCommand = cmd;
     }
 
     /**
      * set the form field values from the DataObject
      * 
      * @param obj an instance of DataObject to be loaded into this form
      */
     protected void initFieldValues(DataObject obj) {
         if (obj != null) {
             initTextField(outputFileNameField, obj.getOutputFilename());
             initMultiplicity(obj.getMultiplicity());
         }
     }
 
     private void initTextField(TextField<String> field, String value) {
         if (value != null && !value.isEmpty()) {
             field.setValue(value);
         }
     }
     
    private void initMultiplicity(String multiplicity) {
         String label = multiplicity;
 
         if (I18N.DISPLAY.multiplicityFolder().equals(multiplicity)) {
             label = I18N.DISPLAY.folder();
         }
 
         for (Field<?> r : multiplicityGroup.getAll()) {
             if (((Radio)r).getBoxLabel().equals(label)) {
                 ((Radio)r).setValue(true);
                 return;
             }
         }
 
     }
 
     private void init() {
         setSize(450, 450);
     }
 
     private void initForm() {
         buildFields();
         addFields();
     }
    
     private void buildMultiplicityRadio(String label) {
         multiplicityGroup = new RadioGroup();
         multiplicityGroup.setFieldLabel(label);
 
         Radio one = new Radio() {
             @Override
             protected void onClick(ComponentEvent be) {
                 super.onClick(be);
 
                 data.setMultiplicity(DataObject.MULTIPLICITY_ONE);
             }
         };
         one.setBoxLabel(I18N.DISPLAY.multiplicityOne());
         one.setValue(true);
         one.setId(ID_RADIO_ONE);
 
         Radio many = new Radio() {
             @Override
             protected void onClick(ComponentEvent be) {
                 super.onClick(be);
 
                 data.setMultiplicity(DataObject.MULTIPLICITY_MANY);
             }
         };
         many.setBoxLabel(I18N.DISPLAY.multiplicityMany());
         many.setId(ID_RADIO_MANY);
 
         Radio folder = new Radio() {
             @Override
             protected void onClick(ComponentEvent be) {
                 super.onClick(be);
 
                 data.setMultiplicity(DataObject.MULTIPLICITY_FOLDER);
             }
         };
         folder.setBoxLabel(I18N.DISPLAY.folder());
         folder.setId(ID_RADIO_FOLDER);
 
         multiplicityGroup.add(one);
         multiplicityGroup.add(many);
         multiplicityGroup.add(folder);
         multiplicityGroup.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     private void buildInfoTypeComboBox(String label, List<InfoType> infoTypes) {
         infoTypeField = new ComboBox<InfoType>();
         infoTypeField.setId(ID_INFO_TYPE_CBO);
         infoTypeField.setWidth(250);
         infoTypeField.setForceSelection(true);
         infoTypeField.setFieldLabel(label);
         infoTypeField.setDisplayField("description"); //$NON-NLS-1$
         infoTypeField.setTriggerAction(TriggerAction.ALL);
         infoTypeField.setFireChangeEventOnSetValue(true);
         infoTypeField.setAllowBlank(false);
         infoTypeField.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
 
         // enable auto-complete
         infoTypeField.setEditable(true);
         infoTypeField.setTypeAhead(true);
        infoTypeField.setQueryDelay(0);
 
         ListStore<InfoType> store = new ListStore<InfoType>();
         store.add(infoTypes);
 
         infoTypeField.setStore(store);
         infoTypeField.addSelectionChangedListener(new SelectionChangedListener<InfoType>() {
             @Override
             public void selectionChanged(SelectionChangedEvent<InfoType> sce) {
                 InfoType type = sce.getSelectedItem();
                 if (type != null) {
                     if (type.getId() != null) {
                         data.setFileTypeId(type.getId());
                     }
                     if (type.getName() != null) {
                         data.setFileType(type.getName());
                     }
                 }
             }
         });
     }
 
     /**
      * Add fields to this form
      */
     protected abstract void addFields();
 
     protected void buildFields() {
         outputFileNameField = buildTextField(ID_FLD_OP_NAME,I18N.DISPLAY.outputFileName(), false, null, 100, null,
                 new OutputFilenameKeyUpCommand());
 
         if (data.getType().equals(DataObject.INPUT_TYPE)) {
             buildMultiplicityRadio(I18N.DISPLAY.inputMultiplicityOption());
         } else {
             buildMultiplicityRadio(I18N.DISPLAY.outPutMultiplicityOption());
         }
         
         buildInfoTypeComboBox(I18N.DISPLAY.infoTypePrompt(), new ArrayList<InfoType>());
     }
 
     private TextField<String> buildTextField(String id,String label, boolean allowBlank, String defaultVal,
             int maxLength, Validator validator, final KeyUpCommand cmdKeyUp) {
         final TextField<String> field = new BoundedTextField<String>();
 
         field.setFieldLabel(label);
         field.setId(id);
         field.setAllowBlank(allowBlank);
         field.setMaxLength(maxLength);
         field.setValidateOnBlur(true);
         field.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
 
         if (defaultVal != null) {
             field.setValue(defaultVal);
         }
 
         if (validator != null) {
             field.setValidator(validator);
         }
 
         if (cmdKeyUp != null) {
             field.addKeyListener(new KeyListener() {
                 @Override
                 public void componentKeyUp(ComponentEvent event) {
                     cmdKeyUp.execute(field.getValue());
                 }
             });
         }
 
         return field;
     }
     
     private void setInfoType() {
         ListStore<InfoType> store = infoTypeField.getStore();
 
         InfoType value = null;
         if (data != null) {
             // select the info type based on the file info type ID.
             value = store.findModel(InfoType.ID, data.getFileInfoTypeId());
 
             if (value == null) {
                 // The file info type ID is not available, so search for the file info type by name next.
                 value = store.findModel(InfoType.NAME, data.getFileInfoType());
             }
         }
 
         if (value == null) {
             // A file info type could not be found that matches the given data object.
             value = store.findModel(InfoType.DESCRIPTION, "Unspecified"); //$NON-NLS-1$
         }
 
         infoTypeField.setValue(value);
     }
 
     /**
      * Populates the "Type of information..." combo box.
      * @param selected
      */
     private void initInfoTypes() {
         EnumerationServices services = new EnumerationServices();
 
         services.getInfoTypes(new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 List<InfoType> types = new ArrayList<InfoType>();
 
                 JSONObject obj = JsonUtil.getObject(result);
                 JSONArray arr = JsonUtil.getArray(obj, "info_types"); //$NON-NLS-1$
 
                 if (arr != null) {
                     JsArray<JsInfoType> jsInfoType = JsonUtil.asArrayOf(arr.toString());
                     for (int i = 0; i < jsInfoType.length(); i++) {
                         types.add(new InfoType(jsInfoType.get(i).getId(), jsInfoType.get(i).getName(),
                                 jsInfoType.get(i).getDescription()));
                     }
 
                     infoTypeField.getStore().add(types);
                     infoTypeField.getStore().sort("description", SortDir.ASC); //$NON-NLS-1$
 
                     setInfoType();
                 } else {
                     // there may be an error message returned in the JSON response
                     onFailure(new Exception(JsonUtil.getString(obj, "message"))); //$NON-NLS-1$
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.DISPLAY.cantLoadInfoTypes(), caught);
             }
         });
     }
 
     public String getOutputFilename() {
         String filename = outputFileNameField.getValue();
         return filename == null ? "" : filename; //$NON-NLS-1$
     }
 
     private interface KeyUpCommand {
         void handleNullInput();
 
         void execute(String value);
     }
 
     private class OutputFilenameKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             data.setOutputFilename(value);
             outputFilenameChangeCommand.execute();
         }
 
         @Override
         public void handleNullInput() {
             data.setOutputFilename(""); //$NON-NLS-1$
             outputFilenameChangeCommand.execute();
         }
     }
 
 }
