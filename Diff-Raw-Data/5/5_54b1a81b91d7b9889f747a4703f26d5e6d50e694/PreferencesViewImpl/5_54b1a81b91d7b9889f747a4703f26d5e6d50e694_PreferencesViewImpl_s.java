 package org.iplantc.de.client.preferences.views;
 
 import org.iplantc.core.uiapps.widgets.client.view.fields.AppWizardFolderSelector;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.de.client.I18N;
 
 import com.google.gwt.core.client.GWT;
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
 import com.sencha.gxt.widget.core.client.form.Radio;
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
     CheckBox cboNotifyEmail;
 
     @UiField
     CheckBox cboLastPath;
 
     @UiField
     Radio radioSaveSession;
 
     @UiField
     Radio radioNoSaveSession;
 
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
 
     AppWizardFolderSelector defaultOpFolder;
 
     static UserSettings us = UserSettings.getInstance();
 
     public PreferencesViewImpl() {
         widget = uiBinder.createAndBindUi(this);
         container.setScrollMode(ScrollMode.AUTOY);
         defaultOpFolder = new AppWizardFolderSelector();
         prefContainer.add(new HTML(I18N.DISPLAY.defaultOutputFolder()), new VerticalLayoutData(.9, -1,
                 new Margins(5)));
         prefContainer.add(defaultOpFolder.asWidget(), new VerticalLayoutData(.9, -1, new Margins(5)));
         appKbSc.addValidator(new MaxLengthValidator(1));
         dataKbSc.addValidator(new MaxLengthValidator(1));
         anaKbSc.addValidator(new MaxLengthValidator(1));
         notKbSc.addValidator(new MaxLengthValidator(1));
         closeKbSc.addValidator(new MaxLengthValidator(1));
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
         radioSaveSession.setValue(true);
         appKbSc.setValue(us.getAppsShortCut());
         dataKbSc.setValue(us.getDataShortCut());
         anaKbSc.setValue(us.getAnalysesShortCut());
         notKbSc.setValue(us.getNotifiShortCut());
         closeKbSc.setValue(us.getCloseShortCut());
     }
 
     @Override
     public void setValues() {
         cboNotifyEmail.setValue(us.isEnableEmailNotification());
         cboLastPath.setValue(us.isRememberLastPath());
         defaultOpFolder.setValueFromStringId(us.getDefaultOutputFolder());
         if (us.isSaveSession()) {
             radioSaveSession.setValue(true);
             radioNoSaveSession.setValue(false);
         } else {
             radioSaveSession.setValue(false);
             radioNoSaveSession.setValue(true);
         }
 
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
         if (radioNoSaveSession.getValue()) {
            us.setSaveSession(false);
        } else {
             us.setSaveSession(true);
         }
         us.setDefaultOutputFolder(defaultOpFolder.getValue().getId());
         us.setAppsShortCut(appKbSc.getValue());
         us.setDataShortCut(dataKbSc.getValue());
         us.setAnalysesShortCut(anaKbSc.getValue());
         us.setNotifiShortCut(notKbSc.getValue());
         us.setCloseShortCut(closeKbSc.getValue());
 
         return us;
     }
 
     @Override
     public boolean isValid() {
         return appKbSc.isValid() && dataKbSc.isValid() && anaKbSc.isValid() && notKbSc.isValid()
                 && closeKbSc.isValid();
     }
 
 }
