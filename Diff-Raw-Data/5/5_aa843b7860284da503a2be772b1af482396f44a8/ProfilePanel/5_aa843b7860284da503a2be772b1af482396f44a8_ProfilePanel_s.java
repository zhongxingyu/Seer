 package org.timadorus.webapp.client.profile;
 
 import org.timadorus.webapp.client.TimadorusWebApp;
 import org.timadorus.webapp.client.User;
 import org.timadorus.webapp.client.rpc.service.DeleteUserService;
 import org.timadorus.webapp.client.rpc.service.DeleteUserServiceAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 @SuppressWarnings("deprecation")
 public class ProfilePanel extends FormPanel implements HistoryListener {
   
   private static ProfilePanel profilePanel;
   
   private TimadorusWebApp entry;
   
   private User user;
   
   private final int rows = 9;
   
   private final int columns = 3;
 
   Grid grid = new Grid(rows, columns);
   
   Button delete = new Button("Account löschen");
   
   Button confirm = new Button("Löschung bestätigen");
   
   PasswordTextBox passBox = new PasswordTextBox();
   
   private void setupHistory() {
     History.addHistoryListener(this);
   }
 
   public ProfilePanel(TimadorusWebApp timadorusWebApp, final User user) {
     super();
     this.entry = timadorusWebApp;
     this.user = user;
     setupHistory();
     grid.setWidget(0, 0, delete);    
     
     class DeleteHandler implements ClickHandler, KeyUpHandler {
       /**
        * Wird ausgelöst, wenn Button gedrückt wurde
        */
       public void onClick(ClickEvent event) {
         System.out.println("Account löschen Button geklickt");
         handleEvent();
       }
 
       /**
        * Prüft ob "Enter" gedrückt wurde
        */
       public void onKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           handleEvent();
         }
       }
 
       private void handleEvent() {
         System.out.println("handle Event");
         grid.remove(delete);
         grid.setWidget(0, 0, new Label("Sind Sie sich sicher? Geben Sie Ihr Passwort zur Bestätigung ein."));
         grid.setWidget(1, 0, passBox);
         grid.setWidget(2, 0, confirm);
       }      
     }
     
     class ConfirmHandler implements ClickHandler, KeyUpHandler {
       /**
        * Wird ausgelöst, wenn Button gedrückt wurde
        */
       public void onClick(ClickEvent event) {
         System.out.println("Löschung bestätigen Button geklickt");
         handleEvent();
       }
 
       /**
        * Prüft ob "Enter" gedrückt wurde
        */
       public void onKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           handleEvent();
         }
       }
 
       private void handleEvent() {
         System.out.println("handle Event");
         if (passBox.getText().equals(user.getPassword())) {
           System.out.println("Deleting " + user.getDisplayname());
           deleteAccount();
           showDialogBox("Information", "Ihr Account wurde erfolgreich gelöscht!");
         } else {
           passBox.setText("");
           showDialogBox("Fehlermeldung", "Passwort falsch! Versuchen Sie es erneut!");
         }
       }
 
       private void deleteAccount() {
         DeleteUserServiceAsync deleteAccountServiceAsync = GWT.create(DeleteUserService.class);
         AsyncCallback<String> asyncCallback = new AsyncCallback<String>() {
           
           public void onSuccess(String result) {
             if (result != null) {
              if (result == User.USER_INVALID) {
                 System.out.println("Unsuccessfully deleted");                
               }
              if (result == String.valueOf(User.OK)) {
                 System.out.println("Successfully deleted");
               }
               History.newItem("welcome");
               History.newItem("logout");
             }
           }
           
           public void onFailure(Throwable caught) {
             System.out.println(caught);
           }
         };
         deleteAccountServiceAsync.delete(user, asyncCallback);
       }
     }
     
     DeleteHandler delHandler = new DeleteHandler();
     delete.addClickHandler(delHandler);
     ConfirmHandler conHandler = new ConfirmHandler();
     confirm.addClickHandler(conHandler);
     
     setWidget(grid);
     setStyleName("formPanel");
   }
   
   public void showDialogBox(String title, String message) {
     // Create the popup dialog box
     final DialogBox dialogBox = new DialogBox();
 
     dialogBox.setText(title);
     dialogBox.setAnimationEnabled(true);
     final Button closeButton = new Button("Close");
 
     // We can set the id of a widget by accessing its Element
     closeButton.getElement().setId("closeButton");
 
     VerticalPanel dialogVPanel = new VerticalPanel();
     dialogVPanel.addStyleName("dialogVPanel");
 
     dialogVPanel.add(new HTML((new StringBuffer().append("<b>").append(message).append("</b>")).toString()));
     dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
     dialogVPanel.add(closeButton);
     dialogBox.setWidget(dialogVPanel);
 
     dialogBox.center();
 
     // Add a handler to close the DialogBox
     closeButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
         dialogBox.hide();
       }
     });
   }
 
   public static Widget getProfilePanel(TimadorusWebApp entry, User user) {
     if (profilePanel == null) {
       profilePanel = new ProfilePanel(entry, user);
     }
     return profilePanel;
   }
   
   public TimadorusWebApp getEntry() {
     return entry;
   }
 
   @Override
   public void onHistoryChanged(String arg0) {
     // TODO Auto-generated method stub
     
   }
   
   
 
 }
