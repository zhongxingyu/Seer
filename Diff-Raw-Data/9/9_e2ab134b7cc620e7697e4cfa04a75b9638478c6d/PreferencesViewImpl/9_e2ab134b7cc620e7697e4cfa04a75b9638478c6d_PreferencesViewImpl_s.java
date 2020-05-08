 package org.iplantc.de.client.preferences.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.iplantc.core.uiapps.widgets.client.view.editors.validation.AnalysisOutputValidator;
 import org.iplantc.core.uicommons.client.Constants;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uidiskresource.client.views.widgets.FolderSelectorField;
 import org.iplantc.de.client.I18N;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
 import com.sencha.gxt.core.client.util.Margins;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 import com.sencha.gxt.widget.core.client.form.CheckBox;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.form.validator.MaxLengthValidator;
 
 /**
  * A view imple for preferences screen
  * 
  * @author sriram
  * 
  */
 public class PreferencesViewImpl implements PreferencesView {
 
     private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
     @UiTemplate("PreferencesView.ui.xml")
     interface MyUiBinder extends UiBinder<Widget, PreferencesViewImpl> {
     }
 
     private final Widget widget;
 
     @UiField
     VerticalLayoutContainer container;
 
     @UiField
     VerticalLayoutContainer prefContainer;
 
     @UiField
     VerticalLayoutContainer kbContainer;
 
     @UiField
     CheckBox cboNotifyEmail;
 
     @UiField
     CheckBox cboLastPath;
 
     @UiField
     CheckBox cboSaveSession;
 
     @UiField
     TextField appKbSc;
 
     @UiField
     TextField dataKbSc;
 
     @UiField
     TextField anaKbSc;
 
     @UiField
     TextField notKbSc;
 
     @UiField
     TextField closeKbSc;
 
     FolderSelectorField defaultOpFolder;
 
     static UserSettings us = UserSettings.getInstance();
 
     private final Map<TextField, String> kbMap;
 
     public PreferencesViewImpl() {
         widget = uiBinder.createAndBindUi(this);
         kbMap = new HashMap<TextField, String>();
         container.setScrollMode(ScrollMode.AUTOY);
         initDefaultOutputFolder();
         appKbSc.addValidator(new MaxLengthValidator(1));
         dataKbSc.addValidator(new MaxLengthValidator(1));
         anaKbSc.addValidator(new MaxLengthValidator(1));
         notKbSc.addValidator(new MaxLengthValidator(1));
         closeKbSc.addValidator(new MaxLengthValidator(1));
         setSCToolTip();
 
         appKbSc.addKeyPressHandler(new KeyPressHandler() {
 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                 kbShortcutToUpperCase(appKbSc, event);
             }
         });
         dataKbSc.addKeyPressHandler(new KeyPressHandler() {
 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                 kbShortcutToUpperCase(dataKbSc, event);
             }
         });
         anaKbSc.addKeyPressHandler(new KeyPressHandler() {
 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                 kbShortcutToUpperCase(anaKbSc, event);
             }
         });
         notKbSc.addKeyPressHandler(new KeyPressHandler() {
 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                 kbShortcutToUpperCase(notKbSc, event);
             }
         });
         closeKbSc.addKeyPressHandler(new KeyPressHandler() {
 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                 kbShortcutToUpperCase(closeKbSc, event);
             }
         });
         populateKbMap();
     }
 
     private void setSCToolTip() {
         appKbSc.setToolTip("Maximum 1 character");
         dataKbSc.setToolTip("Maximum 1 character");
         anaKbSc.setToolTip("Maximum 1 character");
         notKbSc.setToolTip("Maximum 1 character");
         closeKbSc.setToolTip("Maximum 1 character");
     }
 
     private void initDefaultOutputFolder() {
         defaultOpFolder = new FolderSelectorField();
         defaultOpFolder.setValidatePermissions(true);
         defaultOpFolder.setId("idDefaultFolderSelector"); //$NON-NLS-1$
 
         defaultOpFolder.addValidator(new AnalysisOutputValidator());
         defaultOpFolder.addValueChangeHandler(new ValueChangeHandler<HasId>() {
 
             @Override
             public void onValueChange(ValueChangeEvent<HasId> event) {
                 defaultOpFolder.validate(false);
             }
         });
 
         prefContainer.add(new HTML(I18N.DISPLAY.defaultOutputFolder()), new VerticalLayoutData(.9, -1,
                 new Margins(5)));
         prefContainer.add(defaultOpFolder.asWidget(), new VerticalLayoutData(.9, -1, new Margins(5)));
     }
 
     private void populateKbMap() {
         kbMap.put(appKbSc, appKbSc.getValue());
         kbMap.put(dataKbSc, dataKbSc.getValue());
         kbMap.put(anaKbSc, anaKbSc.getValue());
         kbMap.put(notKbSc, notKbSc.getValue());
         kbMap.put(closeKbSc, closeKbSc.getValue());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.google.gwt.user.client.ui.IsWidget#asWidget()
      */
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     @Override
     public void setPresenter(Presenter p) {/* Not Used */
     }
 
     @Override
     public void setDefaultValues() {
         cboNotifyEmail.setValue(true);
         cboLastPath.setValue(true);
         cboSaveSession.setValue(true);
         appKbSc.setValue(Constants.CLIENT.appsKeyShortCut());
         dataKbSc.setValue(Constants.CLIENT.dataKeyShortCut());
         anaKbSc.setValue(Constants.CLIENT.analysisKeyShortCut());
         notKbSc.setValue(Constants.CLIENT.notifyKeyShortCut());
         closeKbSc.setValue(Constants.CLIENT.closeKeyShortCut());
         defaultOpFolder.setValueFromStringId(us.getSystemDefaultOutputFolder());
     }
 
     @Override
     public void setValues() {
         cboNotifyEmail.setValue(us.isEnableEmailNotification());
         cboLastPath.setValue(us.isRememberLastPath());
         defaultOpFolder.setValueFromStringId(us.getDefaultOutputFolder());
         cboSaveSession.setValue(us.isSaveSession());
 
         appKbSc.setValue(us.getAppsShortCut());
         dataKbSc.setValue(us.getDataShortCut());
         anaKbSc.setValue(us.getAnalysesShortCut());
         notKbSc.setValue(us.getNotifiShortCut());
         closeKbSc.setValue(us.getCloseShortCut());
     }
 
     @Override
     public UserSettings getValues() {
         us.setEnableEmailNotification(cboNotifyEmail.getValue());
         us.setRememberLastPath(cboLastPath.getValue());
         us.setSaveSession(cboSaveSession.getValue());
         us.setDefaultOutputFolder(defaultOpFolder.getValue().getId());
         us.setAppsShortCut(appKbSc.getValue());
         us.setDataShortCut(dataKbSc.getValue());
         us.setAnalysesShortCut(anaKbSc.getValue());
         us.setNotifiShortCut(notKbSc.getValue());
         us.setCloseShortCut(closeKbSc.getValue());
 
         return us;
     }
 
     private void resetKbFieldErrors() {
         for (TextField ks : kbMap.keySet()) {
             ks.clearInvalid();
         }
 
     }
 
     @Override
     public boolean isValid() {
         boolean valid = defaultOpFolder.isValid(false) && appKbSc.isValid() && dataKbSc.isValid()
                 && anaKbSc.isValid() && notKbSc.isValid() && closeKbSc.isValid();
         populateKbMap();
         resetKbFieldErrors();
         for (TextField ks : kbMap.keySet()) {
             for (TextField sc : kbMap.keySet()) {
                 if (ks != sc) {
                     if (kbMap.get(ks).equals(kbMap.get(sc))) {
                         ks.markInvalid(I18N.DISPLAY.duplicateShortCutKey(kbMap.get(ks)));
                         sc.markInvalid(I18N.DISPLAY.duplicateShortCutKey(kbMap.get(ks)));
                         valid = false;
                     }
                 }
             }
         }
         return valid;
     }
 
     private void kbShortcutToUpperCase(TextField fld, KeyPressEvent event) {
         int code = event.getNativeEvent().getCharCode();
         if ((code > 96 && code <= 122)) {
             fld.clear();
             fld.setValue((event.getCharCode() + "").toUpperCase());
             fld.setText((event.getCharCode() + "").toUpperCase());
             fld.setCursorPos(1);
             fld.focus();
         } else if ((code > 47 && code <= 57) || (code > 64 && code <= 90)) {
             fld.clear();
             fld.setValue(event.getCharCode() + "");
             fld.setText(event.getCharCode() + "");
             fld.setCursorPos(1);
             fld.focus();
        } else {
            if (code != 0) {
                event.preventDefault();
            }
         }
     }
 }
