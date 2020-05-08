 package org.iplantc.js.integrate.client.panels;
 
 import org.iplantc.core.client.widgets.BoundedTextArea;
 import org.iplantc.core.client.widgets.BoundedTextField;
 import org.iplantc.core.client.widgets.validator.BasicEmailValidator;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.js.integrate.client.Constants;
 import org.iplantc.js.integrate.client.I18N;
 import org.iplantc.js.integrate.client.events.NewToolRequestSubmitEvent;
 import org.iplantc.js.integrate.client.validator.BasicNameValidator;
 import org.iplantc.js.integrate.client.validator.BasicUrlValidator;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FileUploadField;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
 import com.extjs.gxt.ui.client.widget.form.HiddenField;
 import com.extjs.gxt.ui.client.widget.form.Radio;
 import com.extjs.gxt.ui.client.widget.form.RadioGroup;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 
 /**
  * 
  * A tabbed form panel to collect user request for new tool deployment into DE
  * 
  * @author sriram
  * 
  */
 public class ToolRequestFormPanel extends LayoutContainer {
     private static final String ASTERISK_HTML = "<span style=\"color: red; vertical-align: super\">*</span> "; //$NON-NLS-1$
 
     private FormPanel form;
     private FormData formData;
 
     private HiddenField<String> userField;
     private TextField<String> nameField;
     private TextField<String> emailField;
     private TextField<String> phoneField;
     private TextField<String> toolNameField;
     private RadioGroup srcGrp;
     private Radio srcLinkOptionField;
     private Radio srcUploadOptionField;
     private TextArea toolDescription;
     private TextField<String> srcLinkField;
     private FileUploadField srcUpldField;
     private TextField<String> versionField;
     private TextField<String> docLinkField;
     private FileUploadField testDataUpldField;
     private TextArea cmdLineField;
     private FileUploadField addnlUpldField;
     private TextArea addnlInfoField;
     private Button btnSubmit;
     private Button btnCancel;
     private SimpleComboBox<String> threadingCombo;
 
     public static final String USER_ID = "user"; //$NON-NLS-1$
     public static  final String NAME = "name"; //$NON-NLS-1$
     public static final String EMAIL = "email"; //$NON-NLS-1$
     public static final String PHONE = "phone"; //$NON-NLS-1$
     public static final String SRC_UPLOAD_OPTION = "src_upload_option"; //$NON-NLS-1$
     public static final String SRC_LINK_OPTION = "src_link_option"; //$NON-NLS-1$
     public static  final String TOOL_DESCRIPTION = "tool_description"; //$NON-NLS-1$
     public static final String SRC_UPLOAD = "src_upload_file"; //$NON-NLS-1$
     public static final String SRC_LINK = "src_url"; //$NON-NLS-1$
     public static final String VERSION = "version"; //$NON-NLS-1$
     public static final String DOCUMENTATION = "documentation_url"; //$NON-NLS-1$
     public static final String TEST_DATA = "test_data_file"; //$NON-NLS-1$
     public static final String CMD_LINE = "cmd_line"; //$NON-NLS-1$
     public static final String ADDNL_DATA = "additional_data_file"; //$NON-NLS-1$
     public static final String ADDNL_INFO = "additional_info"; //$NON-NLS-1$
     public static final String MULTI_THREADED = "multi-threaded"; //$NON-NLS-1$
     public final String YES = "Yes";
     public final String NO = "No";
     public final String DONT_KNOW = "Don't know";
     
 
     private static final String ID = "id_"; //$NON-NLS-1$
 
     private TabItem personal;
     private TabItem toolInfo;
     private TabItem otherInfo;
 
     private MessageBox wait;
 
     private TabPanel tabs;
 
     /**
      * creates a new instance of ToolRequestFormPanel
      * 
      */
     public ToolRequestFormPanel() {
         setLayout(new FormLayout());
         initForm();
     }
 
     private void initForm() {
         form = new FormPanel();
         addSubmitListener();
         form.setEncoding(Encoding.MULTIPART);
         form.setAction(Constants.CLIENT.newToolRequest());
         form.setMethod(Method.POST);
         form.setSize(490, 470);
         form.setHeaderVisible(false);
         form.setBodyBorder(false);
         formData = new FormData("95%"); //$NON-NLS-1$
         form.setLayout(new FitLayout());
         form.setButtonAlign(HorizontalAlignment.CENTER);
         buildFields();
         addFields();
         setDefaultValues();
         add(form);
     }
 
     private void addSubmitListener() {
         form.addListener(Events.Submit, new Listener<FormEvent>() {
             @Override
             public void handleEvent(FormEvent be) {
                 NewToolRequestSubmitEvent event = new NewToolRequestSubmitEvent();
                 EventBus.getInstance().fireEvent(event);
 
                 if (wait != null) {
                     wait.close();
                 }
 
                 processResults(be.getResultHtml());
             }
 
             private void processResults(String resultHtml) {
                 JSONObject obj = JSONParser.parseStrict(resultHtml).isObject();
 
                 if (obj.containsKey("error")) { //$NON-NLS-1$
                     ErrorHandler.post(I18N.DISPLAY.newToolRequestError());
                 } else {
                     MessageBox.info(I18N.DISPLAY.success(), I18N.DISPLAY.requestConfirmMsg(), null);
                 }
 
             }
         });
     }
 
     private void setDefaultValues() {
         srcUploadOptionField.setValue(true);
         srcLinkField.setVisible(false);
     }
 
     private void addFields() {
         personal = buildPersonalTabItem();
 
         toolInfo = buildTooInfoTabItem();
 
         otherInfo = buildOtherInfoTabItem();
 
         tabs = new TabPanel();
         tabs.setDeferredRender(false);
         tabs.add(personal);
         tabs.add(toolInfo);
         tabs.add(otherInfo);
 
         form.add(tabs);
 
         buildSubmitButton();
         buildCancelButton();
 
         form.addButton(btnSubmit);
         form.addButton(btnCancel);
 
         FormButtonBinding binding = new FormButtonBinding(form);
         binding.addButton(btnSubmit);
     }
 
     /**
 	 * 
 	 */
     private void buildFields() {
         buildUserIdField(I18N.DISPLAY.userId());
         buildSrcOptionsFields(I18N.DISPLAY.link(), I18N.DISPLAY.upload(),
                 ASTERISK_HTML + I18N.DISPLAY.srcLinkPrompt());
 
        nameField = buildTextField(I18N.DISPLAY.name(), false, null, NAME, null,
 100);
         emailField = buildTextField(I18N.DISPLAY.email(), false, getEmail(), EMAIL,
                 new BasicEmailValidator(), 256);
         phoneField = buildTextField(I18N.DISPLAY.phone(), true, null, PHONE, null, 30);
 
         toolNameField = buildTextField(I18N.DISPLAY.toolName(), false, null, NAME,
                 new BasicNameValidator(), 64);
         srcLinkField = buildTextField(null, false, null, SRC_LINK, new BasicUrlValidator(), 1024);
         toolDescription = buildTextArea(I18N.DISPLAY.toolDesc(), false, null, TOOL_DESCRIPTION, 1024);
         versionField = buildTextField(I18N.DISPLAY.version(), false, null, VERSION, null, 64);
         docLinkField = buildTextField(I18N.DISPLAY.docLink(), false, null, DOCUMENTATION,
                 new BasicUrlValidator(), 1024);
         srcUpldField = buildFileUpldField(null, false, SRC_UPLOAD);
         testDataUpldField = buildFileUpldField(I18N.DISPLAY.upldTestData(), false, TEST_DATA);
         cmdLineField = buildTextArea(I18N.DISPLAY.cmdLineRun(), false, null, CMD_LINE, 1024);
         addnlUpldField = buildFileUpldField(I18N.DISPLAY.addnlData(), true, ADDNL_DATA);
         addnlInfoField = buildTextArea(I18N.DISPLAY.comments(), true, null, ADDNL_INFO, 1024);
         threadingCombo = buildThreadingOptionsCombo(I18N.DISPLAY.isMultiThreaded(), false,
                 MULTI_THREADED);
 
     }
 
     private SimpleComboBox<String> buildThreadingOptionsCombo(String label, boolean b, String name) {
         SimpleComboBox<String> combo = new SimpleComboBox<String>();
         combo.setName(name);
         combo.setFieldLabel(label);
         combo.setTriggerAction(TriggerAction.ALL);
         combo.setEditable(false);
         combo.setId(ID + name);
         combo.add(YES);
         combo.add(NO);
         combo.add(DONT_KNOW);
         combo.setSimpleValue(YES);
         return combo;
     }
 
     private TextArea buildTextArea(String label, boolean allowBlank, String defaultVal, String name,
             int maxLength) {
         TextArea field = new BoundedTextArea();
         field.setMaxLength(maxLength);
         field.setName(name);
         field.setId(ID + name);
         if (!allowBlank) {
             label = ASTERISK_HTML + label;
         }
         field.setFieldLabel(label);
         field.setAllowBlank(allowBlank);
         field.setValidateOnBlur(true);
         field.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
         if (defaultVal != null) {
             field.setValue(defaultVal);
         }
         return field;
     }
 
     private FileUploadField buildFileUpldField(String label, boolean allowBlank, String name) {
         FileUploadField field = new FileUploadField();
         field.setName(name);
         field.setId(ID + name);
         if (label != null) {
             if (!allowBlank) {
                 label = ASTERISK_HTML + label;
             }
             field.setFieldLabel(label);
         } else {
             field.setHideLabel(true);
         }
         field.setAllowBlank(allowBlank);
         field.setValidateOnBlur(true);
         field.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
         return field;
     }
 
     /**
      * 
      * @param label
      * @param allowBlank
      * @param defaultVal
      * @param name
      * @param validator
      * @param maxLength
      * @return
      */
     private TextField<String> buildTextField(String label, boolean allowBlank, String defaultVal,
             String name, Validator validator, int maxLength) {
         BoundedTextField<String> field = new BoundedTextField<String>();
         field.setMaxLength(maxLength);
         field.setName(name);
         field.setId(ID + name);
         if (label != null) {
             if (!allowBlank) {
                 label = ASTERISK_HTML + label;
             }
             field.setFieldLabel(label);
         } else {
             field.setHideLabel(true);
         }
         field.setAllowBlank(allowBlank);
         field.setValidateOnBlur(true);
         field.setStyleAttribute("padding-bottom", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
 
         if (defaultVal != null) {
             field.setValue(defaultVal);
         }
         if (validator != null) {
             field.setValidator(validator);
         }
 
         return field;
     }
 
     private TabItem buildOtherInfoTabItem() {
         TabItem otherInfo = new TabItem(I18N.DISPLAY.otherTab());
 
         otherInfo.setLayout(new FormLayout(LabelAlign.TOP));
         otherInfo.setStyleAttribute("padding", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
         otherInfo.add(testDataUpldField, formData);
         otherInfo.add(cmdLineField, formData);
         otherInfo.add(addnlUpldField, formData);
         otherInfo.add(addnlInfoField, formData);
 
         return otherInfo;
     }
 
     private TabItem buildTooInfoTabItem() {
         TabItem toolInfo = new TabItem(I18N.DISPLAY.toolTab());
         toolInfo.setScrollMode(Scroll.AUTOY);
 
         toolInfo.setLayout(new FormLayout(LabelAlign.TOP));
         toolInfo.setStyleAttribute("padding", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
         toolInfo.add(toolNameField, formData);
         toolInfo.add(toolDescription, formData);
         toolInfo.add(srcGrp, formData);
         toolInfo.add(srcLinkField, formData);
         toolInfo.add(srcUpldField, formData);
         toolInfo.add(docLinkField, formData);
         toolInfo.add(versionField, formData);
         toolInfo.add(threadingCombo,formData);
 
         return toolInfo;
     }
 
     private TabItem buildPersonalTabItem() {
         TabItem personal = new TabItem(I18N.DISPLAY.contactTab());
 
         personal.setLayout(new FormLayout(LabelAlign.TOP));
         personal.setStyleAttribute("padding", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
         personal.add(userField, formData);
         personal.add(nameField, formData);
         personal.add(emailField, formData);
         personal.add(phoneField, formData);
 
         return personal;
     }
 
     private void buildUserIdField(String label) {
         userField = new HiddenField<String>();
         userField.setName(USER_ID);
         userField.setId(ID + USER_ID);
 
         UserInfo info = UserInfo.getInstance();
         userField.setValue(info.getUsername());
     }
 
     private String getEmail() {
         UserInfo info = UserInfo.getInstance();
 
         return info.getEmail();
     }
 
     private void buildSrcOptionsFields(String urlLabel, String upldLabel, String grpLabel) {
         srcLinkOptionField = new Radio();
         srcLinkOptionField.setBoxLabel(urlLabel);
         srcLinkOptionField.setName(SRC_LINK_OPTION);
         srcLinkOptionField.setId(ID + SRC_LINK_OPTION);
 
         srcUploadOptionField = new Radio() {
             @Override
             protected void afterRender() {
                 super.afterRender();
                 addListener(Events.Change, new UploadOptionChangeListener());
             }
         };
         srcUploadOptionField.setBoxLabel(upldLabel);
         srcUploadOptionField.setName(SRC_UPLOAD_OPTION);
         srcUploadOptionField.setId(ID + SRC_UPLOAD_OPTION);
 
         srcGrp = new RadioGroup();
         srcGrp.setFieldLabel(grpLabel);
         srcGrp.add(srcLinkOptionField);
         srcGrp.add(srcUploadOptionField);
     }
 
     private class UploadOptionChangeListener implements Listener<BaseEvent> {
         @Override
         public void handleEvent(BaseEvent be) {
             boolean enableSrcUpldField = srcUploadOptionField.getValue();
             boolean enableSrcLinkField = !enableSrcUpldField;
 
             srcUpldField.setVisible(enableSrcUpldField);
             srcUpldField.setEnabled(enableSrcUpldField);
 
             srcLinkField.setVisible(enableSrcLinkField);
             srcLinkField.setEnabled(enableSrcLinkField);
 
             form.layout();
         }
     }
 
     /**
      * validate this form
      * 
      * @return true if the form is valid and false if form is invalid
      */
     public boolean validate() {
         for (Field<?> f : form.getFields()) {
             f.clearInvalid();
         }
 
         return form.isValid();
     }
 
     /**
      * Submit this form
      */
     public void submit() {
         // remove unused file upload fields
         if (srcUpldField.getValue() == null) {
             toolInfo.remove(srcUpldField);
         }
 
         if (addnlUpldField.getValue() == null) {
             otherInfo.remove(addnlUpldField);
         }
 
         wait = MessageBox.wait(I18N.DISPLAY.progress(), I18N.DISPLAY.submitRequest(),
                 I18N.DISPLAY.submitting());
         form.submit();
     }
 
     private void buildSubmitButton() {
         btnSubmit = new Button();
         btnSubmit.setText(I18N.DISPLAY.submit());
         btnSubmit.setId("idBtnSubmit"); //$NON-NLS-1$
     }
 
     private void buildCancelButton() {
         btnCancel = new Button();
         btnCancel.setText(I18N.DISPLAY.cancel());
         btnCancel.setId("idBtnCancel"); //$NON-NLS-1$
     }
 
     public Button getSubmitButton() {
         return btnSubmit;
     }
 
     public Button getCancelButton() {
         return btnCancel;
     }
 }
