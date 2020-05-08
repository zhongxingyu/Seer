 package bazooka.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.*;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Configuration extends Composite {
 
   interface Binder extends UiBinder<Widget, Configuration> {}
 
   private static final Binder binder = GWT.create(Binder.class);
 
   @UiField ListBox configList;
   @UiField Button saveButton;
   @UiField Button deleteButton;
   @UiField Button cloneButton;
   @UiField Button addParamButton;
   @UiField Button removeParamButton;
   @UiField VerticalPanel parametersPanel;
 
   private final Map<String, Map<String, String>> configurations = new HashMap<String, Map<String, String>>();
 
   Configuration() {
     initWidget(binder.createAndBindUi(this));
   }
 
   @Override protected void onLoad() {
     populateConfigList();
     populateDefaultParameters();
   }
 
   @UiHandler("configList")
   void onConfigListChanged(ChangeEvent event) {
     reloadParameters();
     disableSaveButton();
 
     if (isFirstConfigSelected())
       disableDeleteButton();
     else
       enableDeleteButton();
   }
 
   @UiHandler("configList")
   void onConfigListKeyDown(KeyDownEvent event) {
     onConfigListChanged(null);
   }
 
   @UiHandler("configList")
   void onConfigListKeyUp(KeyUpEvent event) {
     onConfigListChanged(null);
   }
 
   @UiHandler("saveButton")
   void onSaveButtonClicked(ClickEvent event) {
     refreshParameters();
     disableSaveButton();
   }
 
   @UiHandler("deleteButton")
   void onDeleteButtonClicked(ClickEvent event) {
     if (isFirstConfigSelected()) {
       Window.alert("Cannot delete the default configuration.");
     }
     else if (Window.confirm("Are you sure you want to delete '" + getSelectedConfig() + "'?")) {
       removeSelectedConfig();
       selectFirstConfig();
       reloadParameters();
       disableDeleteButton();
     }
   }
 
   @UiHandler("cloneButton")
   void onCloneButtonClicked(ClickEvent event) {
     String newConfigName = askNewConfigName();
     if (newConfigName != null) {
       addConfig(newConfigName);
       selectLastConfig();
       cloneParameters();
       enableDeleteButton();
     }
   }
 
   @UiHandler("addParamButton")
   void onAddParamClicked(ClickEvent event) {
     addParameter("", "");
     addParamButton.setFocus(false);
     enableSaveButton();
   }
 
   private void populateConfigList() {
     addConfig("Default");
     addConfig("Foo");
     addConfig("Bar");
     addConfig("Baz");
   }
 
   private void populateDefaultParameters() {
     for (int i = 1; i <= 10; i++) {
       String key = "FOO" + i, value = "BAR" + i;
       getSelectedParameters().put(key, value);
       addParameter(key, value);
     }
   }
 
   private void reloadParameters() {
     clearParameters();
     for (Map.Entry<String, String> param : getSelectedParameters().entrySet())
       addParameter(param.getKey(), param.getValue());
   }
 
   private void cloneParameters() {
     for (int i = 0; i < parametersPanel.getWidgetCount(); i++) {
       HorizontalPanel entry = (HorizontalPanel) parametersPanel.getWidget(i);
       String key = getParameterKey(entry);
       String value = getParameterValue(entry);
       getSelectedParameters().put(key, value);
     }
   }
 
   private void refreshParameters() {
     getSelectedParameters().clear();
 
     for (int i = parametersPanel.getWidgetCount() - 1; i >= 0 ; i--) {
       HorizontalPanel entry = (HorizontalPanel) parametersPanel.getWidget(i);
       String key = getParameterKey(entry);
 
       if (mustDiscardParameter(key))
         removeParameter(entry);
       else
         getSelectedParameters().put(key, getParameterValue(entry));
     }
   }
 
   private void clearParameters() {
     parametersPanel.clear();
   }
 
   private void addParameter(String key, String value) {
     parametersPanel.add(buildParameterEntry(key, value));
   }
 
   private void removeParameter(HorizontalPanel entry) {
     parametersPanel.remove(entry);
   }
 
   private Map<String, String> getSelectedParameters() {
     return configurations.get(getSelectedConfig());
   }
 
   private String getParameterKey(HorizontalPanel entry) {
     TextBox key = (TextBox) entry.getWidget(0);
     return key.getText();
   }
 
   private String getParameterValue(HorizontalPanel entry) {
     TextBox key = (TextBox) entry.getWidget(2);
     return key.getText();
   }
 
   private boolean mustDiscardParameter(String key) {
     return "".equals(key.trim());
   }
 
   private void enableSaveButton() {
     saveButton.setEnabled(true);
   }
 
   private void disableSaveButton() {
     saveButton.setEnabled(false);
   }
 
   private void enableDeleteButton() {
     deleteButton.setEnabled(true);
   }
 
   private void disableDeleteButton() {
     deleteButton.setEnabled(false);
   }
 
   private void addConfig(String name) {
     configList.addItem(name);
     configurations.put(name, new HashMap<String, String>());
   }
 
   private void removeSelectedConfig() {
     configurations.remove(getSelectedConfig());
     configList.removeItem(getSelectedConfigIndex());
   }
 
   private boolean containsConfig(String name) {
     return configurations.containsKey(name);
   }
 
   private String getSelectedConfig() {
     return configList.getItemText(getSelectedConfigIndex());
   }
 
   private int getSelectedConfigIndex() {
     return configList.getSelectedIndex();
   }
 
   private void selectFirstConfig() {
     configList.setSelectedIndex(0);
   }
 
   private boolean isFirstConfigSelected() {
     return configList.getSelectedIndex() == 0;
   }
 
   private void selectLastConfig() {
     int lastIndex = configList.getItemCount() - 1;
     configList.setSelectedIndex(lastIndex);
   }
 
   private HorizontalPanel buildParameterEntry(String key, String value) {
     HorizontalPanel entry = new HorizontalPanel();
     entry.setSpacing(5);
     entry.add(buildParameterTextBox(key));
     entry.add(buildEqualsLabel());
     entry.add(buildParameterTextBox(value));
     entry.add(buildRemoveParameterButton(entry));
     return entry;
   }
 
   private TextBox buildParameterTextBox(String text) {
     TextBox paramBox = new TextBox();
     paramBox.setText(text);
    paramBox.setWidth("140");
     paramBox.addChangeHandler(new ChangeHandler() {
       public void onChange(ChangeEvent event) {
         enableSaveButton();
       }
     });
     return paramBox;
   }
 
   private Label buildEqualsLabel() {
     Label label = new Label("=");
    label.setWidth("10");
     label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     return label;
   }
 
   private Widget buildRemoveParameterButton(final HorizontalPanel entry) {
     Button remButton = new Button();
     remButton.setStyleName(removeParamButton.getStyleName());
     remButton.setHTML(removeParamButton.getHTML());
     remButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
         removeParameter(entry);
         enableSaveButton();
       }
     });
     return remButton;
   }
 
   private String askNewConfigName() {
     String newName = Window.prompt("Please specify the configuration name.", "My Config");
     while (containsConfig(newName)) {
       Window.alert("'" + newName + "' already exists, please choose a different name.");
       newName = askNewConfigName();
     }
     return newName;
   }
 }
