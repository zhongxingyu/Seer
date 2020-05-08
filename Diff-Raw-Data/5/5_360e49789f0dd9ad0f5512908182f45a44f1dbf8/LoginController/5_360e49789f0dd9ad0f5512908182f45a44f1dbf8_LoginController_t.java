 package ch9k.core;
 
 import ch9k.configuration.Configuration;
import ch9k.core.event.AccountLogoffEvent;
 import ch9k.core.gui.ApplicationWindow;
 import ch9k.core.gui.LoginPanel;
 import ch9k.eventpool.EventPool;
 import java.awt.EventQueue;
 
 /**
  * The login model handles the loading of the account (and verifying it)
  * @author Bruno Corijn
  * @author Pieter De Baets
  */
 public class LoginController {
     private Configuration configuration;
     private boolean cancelled = false;
     private LoginPanel view;
 
     /**
      * Show the login-window, will block until a valid configuration is acquired
      * @param window The root application window
      * @return configuration
      */
     public synchronized Configuration run(final ApplicationWindow window) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 view = new LoginPanel(LoginController.this, window);
                 window.setVisible(true);
                 window.repaint();
             }
         });
 
         while(configuration == null && !cancelled) {
             try {
                 wait();
             } catch (InterruptedException ex) {}
         }
         return configuration;
     }
 
     /**
      * Mark the login-process as cancelled
      * @param cancelled
      */
     public synchronized void setCancelled(boolean cancelled) {
         this.cancelled = cancelled;
         notifyAll();
     }
     
     /**
      * Try loading a configuration by authenticating an existing user
      * @param username
      * @param password
      * @return succes
      */
     public synchronized boolean login(String username, String password) {
         Configuration configuration = new Configuration(username);
         Account account = configuration.getAccount();
 
         if(account != null && account.authenticate(password)) {
             this.configuration = configuration;
             notifyAll();
             return true;
         } else {
            EventPool.getAppPool().raiseEvent(new AccountLogoffEvent());
             return false;
         }
     }
     
     /**
      * Create a configuration for a new user
      * @param username
      * @param password
      */
     public synchronized void register(String username, String password) {
         configuration = new Configuration(username);
         configuration.setAccount(new Account(username, password));
 
         notifyAll();
     }
 
     /**
      * Validate a given set of inputs
      * @param username 
      * @param password
      */
     public void validateLogin(String username, String password) {
         if(username.isEmpty() || password.isEmpty()) {
             view.setError(I18n.get("ch9k.core", "error_fill_all_fields"));
         } else {
             boolean success = login(username, password);
             if(!success) {
                 view.setError(I18n.get("ch9k.core", "error_invalid_credentials"));
             } else {
                 view.setError(null);
             }
         }
     }
 } 
