 package org.processbase.ui.servlet;
 
 import com.vaadin.event.Action;
 import com.vaadin.event.Action.Handler;
 import com.vaadin.event.ShortcutAction;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window.Notification;
 import com.vaadin.ui.themes.Runo;
 import java.util.Locale;
 
 import org.hibernate.annotations.common.Version;
 
 /**
  *
  * @author mgubaidullin
  */
 public class LoginPanel extends GridLayout implements Handler {
 
     private Panel panel = new Panel();
     public FormLayout form = new FormLayout();
     private VerticalLayout vlayout = new VerticalLayout();
     private Button btnLogin = null;
     private Action action_ok = null;
     private TextField username = new TextField("Username", "");
     private PasswordField password = new PasswordField("Password", "");
     private Label labelLeft = new Label("");
     private Label labelRight = new Label("");
     private Locale locale = null;
     private Embedded logo = null;
 
     public LoginPanel() {
         super(3, 2);
     }
 
     public void initUI() {
         setWidth("100%");
         setHeight("100%");
         addComponent(labelLeft, 0, 0);
         addComponent(labelRight, 2, 0);
         addComponent(panel, 1, 1);
         setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
 
         panel.setWidth("300px"); 
 
         username.setCaption(((PbApplication)getApplication()).getPbMessages().getString("userName"));
         form.addComponent(username);
         username.setWidth("100%");
         username.focus();
 
         password.setCaption(((PbApplication)getApplication()).getPbMessages().getString("password"));
         password.setWidth("100%");
         form.addComponent(password);
 
         btnLogin = new Button(((PbApplication)getApplication()).getPbMessages().getString("login"), this, "okHandler");
         btnLogin.setStyleName(Runo.BUTTON_DEFAULT);
         action_ok = new ShortcutAction("Default key", ShortcutAction.KeyCode.ENTER, null);
         form.addComponent(btnLogin);
         form.setComponentAlignment(btnLogin, Alignment.BOTTOM_RIGHT);
         
         Label version=new Label(org.processbase.ui.core.Version.VERSION);
         version.setHeight("8px");
         form.addComponent(version);
         form.setComponentAlignment(version, Alignment.BOTTOM_RIGHT);
 //        btnLogin.addListener(this);
 
         createLogo();
         vlayout.addComponent(logo);
         vlayout.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
         vlayout.addComponent(form);
         vlayout.setComponentAlignment(form, Alignment.MIDDLE_CENTER);
         vlayout.setMargin(false, true, false, true);
         vlayout.setSpacing(true);
         panel.setContent(vlayout);
         panel.addActionHandler(this);
     }
 
     private void createLogo() {
         ThemeResource themeResource = new ThemeResource("icons/processbase.png");
         logo = new Embedded("", themeResource);
         logo.setType(Embedded.TYPE_IMAGE);
     }
 
     public Action[] getActions(Object target, Object sender) {
         return new Action[]{action_ok};
     }
 
     public void handleAction(Action action, Object sender, Object target) {
         if (action == action_ok) {
             okHandler();
         }
     }
 
     public void okHandler() {
         try {
             username.commit();
             password.commit();
             ((PbApplication)getApplication()).authenticate(username.getValue().toString(), password.getValue().toString());
         } catch (Exception ex) {
              ex.printStackTrace();
             getApplication().getMainWindow().showNotification("Error", ex.getMessage(), Notification.TYPE_ERROR_MESSAGE);
            //throw new RuntimeException(ex);
         }
     }
 }
