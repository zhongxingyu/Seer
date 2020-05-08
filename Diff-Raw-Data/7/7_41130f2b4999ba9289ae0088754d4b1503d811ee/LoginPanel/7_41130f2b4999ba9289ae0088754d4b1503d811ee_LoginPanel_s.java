 package org.timadorus.webapp.client.login;
 
 import java.util.Date;
 
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.SessionId;
 import org.timadorus.webapp.client.eventhandling.events.ShowLoginEvent;
 import org.timadorus.webapp.client.eventhandling.events.ShowLogoutEvent;
 import org.timadorus.webapp.client.eventhandling.handler.ShowLoginHandler;
 import org.timadorus.webapp.client.eventhandling.handler.ShowLogoutHandler;
 import org.timadorus.webapp.client.service.Service;
 import org.timadorus.webapp.client.service.ServiceAsync;
 import org.timadorus.webapp.client.service.ServiceType;
 import org.timadorus.webapp.shared.Action;
 import org.timadorus.webapp.shared.Response;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 //FormPanel for Login
 @SuppressWarnings("deprecation")
 public class LoginPanel extends FormPanel implements HistoryListener, ShowLoginHandler, ShowLogoutHandler {
 
   private final int rows = 4;
   
   private final int columns = 2;
   
   private Grid grid = new Grid(rows, columns);
 
   private TextBox userBox = new TextBox();
 
   private PasswordTextBox passBox = new PasswordTextBox();
 
   private Label userLabel = new Label("Benutzername");;
 
   private Label passLabel = new Label("Passwort");
 
   private HTML errorHTML = new HTML();
 
   private Button submit = new Button("Einloggen");
 
   public User user;
 
   private SessionId sessionId;
 
   private DefaultTimadorusWebApp entry;
 
   private static final long TWO_MIN = 1000 * 60 * 2;
 
   private int logincounter;
 
   private static LoginPanel loginPanel;
   
   private final ServiceAsync<User, String> loginService = GWT.create(Service.class);
 
   public LoginPanel(SessionId session, DefaultTimadorusWebApp entryIn) {
     super();
 
     this.entry = entryIn;
     
     entry.addHandler(ShowLoginEvent.SHOWDIALOG, this);
     entry.addHandler(ShowLogoutEvent.SHOWLOGOUT, this);
     
     setUser(new User());
     logincounter = 0;
     setupHistory();
 
     userBox.addFocusHandler(new FocusHandler() {
 
       @Override
       public void onFocus(FocusEvent event) {
         userBox.setText("");
       }
 
     });
 
     passBox.addFocusHandler(new FocusHandler() {
 
       @Override
       public void onFocus(FocusEvent event) {
         passBox.setText("");
       }
 
     });
 
     this.sessionId = session;
     // RootPanel.get("context").add(new Label("Benutzer login"));
     grid.setWidget(0, 0, userLabel);
     grid.setWidget(0, 1, userBox);
     grid.setWidget(1, 0, passLabel);
     grid.setWidget(1, 1, passBox);
     grid.setWidget(2, 1, errorHTML);
     grid.setWidget(3, 1, submit);
 
     /**
      * Create a handler for the sendButton and nameField.
      */
     class MyHandler implements ClickHandler, KeyUpHandler {
       /**
        * Will be triggered if the button was clicked.
        * 
        * @param event The click event object
        */
       public void onClick(ClickEvent event) {
         handleEvent();
       }
 
       /**
        * Will be triggered if the "Enter" button was hit while located in an input field.
        * 
        * @param event The KeyUpEvent object
        */
       public void onKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           handleEvent();
         }
       }
 
       private void handleEvent() {
         clearError();
         getUser().setUsername(userBox.getText());
         getUser().setPassword(passBox.getText());
         
         if (getUser().getUsername().equals("") || user.getPassword().equals("")) {
          loginInvalid("Bitte Felder ausfllen!");
           History.newItem("login");
         } else {
           sendToServer();
           userBox.setText("");
           passBox.setText("");
 
         }
       }
 
       /**
        * Username und Passwort an Server senden
        */
       private void sendToServer() {
         
         //TODO Command-Pattern
         
         Action<User> action = new Action<User>(ServiceType.LOGIN , user);
         AsyncCallback<Response<String>> response = new AsyncCallback<Response<String>>() {
 
           @Override
           public void onFailure(Throwable caught) {
             gettimadorus().showDialogBox("Fehlermeldung", "Fehler bei der Anmeldung");
             loginInvalid("Fehler bei der Anmeldung!");
             History.newItem("login");
             System.out.println(caught);
           }
 
           @Override
           public void onSuccess(Response<String> result) {
             final int maxAttempts = 4;
             if (result.getResult() != null) {
               if (result.getResult().equals(User.USER_INACTIVE)) {
                 loginInvalid("User ist deaktiviert!");
                 History.newItem("welcome");
                 RootPanel.get("content").add(
                          new HTML("<div id=\"info\" class=\"info\">Der angegebene User ist deaktiviert. " 
                                   + "Das kann mehrere Gruende haben<br />(Anmerkung vom Programmierer: Welche denn? "
                                   + "Gesperrt durch Admin oder sowas? Oder ist damit gemeint 'Registriert, aber Mail "
                                   + "noch nicht verifiziert'? Oder beides?) </div>"));
 
                 submit.setEnabled(true);
                 gettimadorus().setLoggedin(false);
                 
               } else if (result.getResult().equals(User.USER_INVALID)) {
                 loginInvalid("Username und/oder Passwort falsch!");
                 submit.setEnabled(true);
                 logincounter++;
 
                 gettimadorus().setLoggedin(false);
                 System.out.println("logincounter " + logincounter);
                 if (logincounter < maxAttempts) {
                   History.newItem("login");
                 } else {
                   History.newItem("welcome");
                 }
               } else {
                 getUser().setActive(true);
                 gettimadorus().setLoggedin(true);                
 
                 Cookies.setCookie("session", result.getResult(), new Date(System.currentTimeMillis() + TWO_MIN));
                 sessionId.setSessionId(result.getResult());
                 System.out.println("Login session => " + result.getResult());
                 History.newItem("welcome");
               }
             }
           }
         };
         
         loginService.execute(action, response);
       }
     }
     
 
     // Add a handler to send the name to the server
     MyHandler handler = new MyHandler();
     submit.addClickHandler(handler);
     userBox.addKeyUpHandler(handler);
     passBox.addKeyUpHandler(handler);
 
     setWidget(grid);
     setStyleName("formPanel");
   }
 
   public static final LoginPanel getLoginPanel(SessionId sessionId, DefaultTimadorusWebApp entry) {
     if (LoginPanel.loginPanel == null) {
       LoginPanel.loginPanel = new LoginPanel(sessionId, entry);
     }
     return LoginPanel.loginPanel;
   }
 
   private void setupHistory() {
     History.addHistoryListener(this);
     // History.onHistoryChanged("login");
   }
 
   public void setTimadorusWebApp(DefaultTimadorusWebApp webapp) {
     this.entry = webapp;
   }
 
   /**
    * If the login was invalid, the error message will be formatted and inserted here.
    * 
    * @param message The error message which shall be displayed
    */
   private void loginInvalid(String message) {
     errorHTML.setHTML("<span class=\"error\">" + message + "</span>");
   }
 
   private void clearError() {
     errorHTML.setHTML("");
     Element info = DOM.getElementById("info");
     if (info != null) {
       info.getParentElement().removeChild(info);
     }
   }
 
   @Override
   public void onHistoryChanged(String historyToken) {
 //    if (LOGIN_STATE.equals(historyToken)) {
 //      gettimadorus().loadLoginPanel();
 //    }else if (LOGOUT_STATE.equals(historyToken)) {
 //      gettimadorus().loadLogoutPanel();
 //    }  else if (WELCOME_STATE.equals(historyToken)) {
 //      gettimadorus().loadWelcomePanel();
 //    } else if (CREATE_CHARACTER_STATE.equals(historyToken)) {
 //      gettimadorus().loadCreateCharacter();
 //    } else if (REGISTER_STATE.equals(historyToken)) {
 //      gettimadorus().loadRegisterPanel();
 //    }
   }
 
   private DefaultTimadorusWebApp gettimadorus() {
     return entry;
   }
 
   public User getUser() {
     return user;
   }
 
   public void setUser(User userIn) {
     this.user = userIn;
   }
 
   @Override
   public void showLogout() {
     RootPanel.get("content").clear();
    RootPanel.get("content").add(new Label("Sie haben sich ausgeloggt. Unten haben sie die Mglichkeit, sich wieder "
                                     + "einzuloggen:"));
 
     RootPanel.get("content").add(this);
     RootPanel.get("information").clear();
   }
 
   @Override
   public void showLogin() {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(new Label("In bestehenden Account einloggen:"));
     RootPanel.get("content").add(this);
   }
 
 }
