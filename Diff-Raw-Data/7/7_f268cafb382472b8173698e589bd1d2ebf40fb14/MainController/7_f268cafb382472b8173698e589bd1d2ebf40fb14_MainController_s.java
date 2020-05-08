 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Main;
 
 import navigation.NavigationListener;
 import navigation.NavigationEvent;
 import Search.SearchQuery;
 import popups.accountSettings.AccountSettingsPopup;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JDialog;
 import navigation.NavigationSearchEvent;
 
 /**
  * A singelton class for handling the major tasks in the UI on a high level,
  * such as performing a search or showing the main view.
  * 
  * @author Peter
  */
 public enum MainController {
 
     /**
      * The MainController instance.
      */
     INSTANCE;
     /**
      * The NavigationListeners that is listening to the controllers events.
      */
     private List<NavigationListener> navigationListeners = new ArrayList<NavigationListener>();
     /**
      * The account settings dialog
      */
     private JDialog accountSettingsDialog;
 
    private MainController() {
        // Create the Dialog, so it can be shown instantly later
        createAccountSettingsDialog();
    }

     /**
      * Performs a search by displaying the search results panels.
      * 
      * @param sq 
      */
     public void search(SearchQuery sq) {
         notifyNavigationListeners(new NavigationSearchEvent(sq));
     }
 
     /**
      * Creates the account settings dialog.
      */
     private void createAccountSettingsDialog() {
         JDialog dialog = new JDialog(MainApp.getApplication().getMainFrame(),
                 AccountSettingsPopup.DIALOG_TITLE,
                 Dialog.ModalityType.APPLICATION_MODAL);
         dialog.setResizable(false);
         dialog.add(new AccountSettingsPopup(dialog));
         accountSettingsDialog = dialog;
     }
 
     /**
      * Shows the account settings popup.
      */
     public void showAccountSettingsPopup() {
         if (accountSettingsDialog == null) {
             createAccountSettingsDialog();
         }
         Dimension popupSize = AccountSettingsPopup.PREFERRED_SIZE;
        
         // Set size and center on screen
         accountSettingsDialog.setBounds(0, 0, popupSize.width, popupSize.height);
         accountSettingsDialog.setLocationRelativeTo(null);
         accountSettingsDialog.setVisible(true);
     }
 
     /**
      * Notifies all navigation listeners about a navigation in the UI
      * @param sq 
      */
     public void notifyNavigationListeners(NavigationEvent navigationEvent) {
         if (navigationEvent != null) {
             for (NavigationListener sl : navigationListeners) {
                 sl.onNavigate(navigationEvent);
             }
         }
     }
 
     /**
      * Adds a NavigationListner to be notified when a navigation in
      * the UI is performed.
      * @param navigationListener 
      */
     public void addNavigationListener(NavigationListener navigationListener) {
         navigationListeners.add(navigationListener);
     }
 
     /**
      * Removes a NavigationListener.
      * @param navigationListener 
      */
     public void removeNavigationListener(NavigationListener navigationListener) {
         navigationListeners.remove(navigationListener);
     }
 }
