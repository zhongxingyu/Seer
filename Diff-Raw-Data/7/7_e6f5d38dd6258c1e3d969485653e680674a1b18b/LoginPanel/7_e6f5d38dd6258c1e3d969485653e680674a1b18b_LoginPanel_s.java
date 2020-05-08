 package nl.sense_os.commonsense.client.auth.login;
 
import java.util.Date;
import java.util.logging.Logger;

 import nl.sense_os.commonsense.client.common.components.LoginForm;
 import nl.sense_os.commonsense.client.main.MainEvents;
 
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.user.client.Cookies;
 
 public class LoginPanel extends View {
 
     private static final Logger logger = Logger.getLogger("LoginPanel");
     private ContentPanel panel;
     private LoginForm form;
 
     public LoginPanel(Controller controller) {
         super(controller);
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         EventType type = event.getType();
         if (type.equals(MainEvents.Init)) {
             // do nothing, initialization is done in initialize()
 
         } else if (type.equals(LoginEvents.Show)) {
             // logger.fine("Show");
             LayoutContainer parent = event.<LayoutContainer> getData("parent");
             showPanel(parent);
 
         } else if (type.equals(LoginEvents.AuthenticationFailure)) {
             logger.warning("AuthenticationFailure");
             onAuthenticationFailure(event);
 
         } else if (type.equals(LoginEvents.LoginSuccess)) {
             // logger.fine("LoginSuccess");
             onLoggedIn(event);
 
         } else if (type.equals(LoginEvents.LoggedOut)) {
             // logger.fine("LoggedOut");
             onLoggedOut(event);
 
         } else if (type.equals(LoginEvents.LoginFailure)) {
             logger.warning("LoginFailure");
             onError(event);
 
         } else {
             logger.severe("Unexpected event type: " + type);
         }
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         this.panel = new ContentPanel();
         this.panel.setLayout(new FitLayout());
         this.panel.setHeading("Login");
 
         this.form = new LoginForm();
         this.form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
 
             @Override
             public void handleEvent(FormEvent be) {
                 requestLogin();
             }
 
         });
         this.panel.add(this.form);
 
         this.form.layout();
     }
 
     private void onAuthenticationFailure(AppEvent event) {
         MessageBox.alert(null,
                 "Failed to sign in: incorrect user name or password. Please try again.",
                 new Listener<MessageBoxEvent>() {
 
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                         resetFormValues();
                     }
                 });
         setBusy(false);
     }
 
     private void onError(AppEvent event) {
         MessageBox.alert(null, "Failed to sign in. Please try again.",
                 new Listener<MessageBoxEvent>() {
 
                     @Override
                     public void handleEvent(MessageBoxEvent be) {
                         resetFormValues();
                     }
                 });
     }
 
     private void onLoggedIn(AppEvent event) {
 
         // save new user name if the user wants it
         if (this.form.getRememberMe()) {
             long expiry = 1000l * 60 * 60 * 24 * 14; // 2 weeks
             Date expires = new Date(new Date().getTime() + expiry);
             Cookies.setCookie("username", this.form.getUsername(), expires);
         } else {
             Cookies.removeCookie("username");
         }
     }
 
     private void onLoggedOut(AppEvent event) {
         resetFormValues();
     }
 
     private void requestLogin() {
         setBusy(true);
         AppEvent event = new AppEvent(LoginEvents.LoginRequest);
         event.setData("username", this.form.getUsername());
         event.setData("password", this.form.getPassword());
         Dispatcher.forwardEvent(event);
     }
 
     private void resetFormValues() {
 
         this.form.setUsername(null);
 
         // auto-fill username field from cookie
         final String cookieName = Cookies.getCookie("username");
         if (null != cookieName) {
             this.form.setUsername(cookieName);
         }
 
         // clear password field
         this.form.setPassword(null);
 
         // auto-set remember me checkbox
         this.form.setRememberMe(true);
 
         setBusy(false);
     }
 
     private void setBusy(boolean busy) {
         this.form.setBusy(busy);
     }
 
     private void showPanel(LayoutContainer parent) {
         resetFormValues();
         parent.add(this.panel);
         parent.layout();
     }
 }
