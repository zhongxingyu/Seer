 
 package view.users;
 
 import java.util.List;
 
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 import model.users.User;
 import model.users.UserManager;
 import view.conferences.AddUserCallback;
 import view.util.CustomTable;
 import view.util.ProgressSpinnerCallbacks;
 import view.util.ProgressSpinnerService;
 
 /**
  * JavaFX pane responsible for displaying the users registered in the database.
  * 
  * @version 11-23-2013
  */
 public class UsersPane extends Stage implements EventHandler {
     
     private static final int DOUBLE_CLICK = 2;
     
     /**
      * The default width.
      */
     private static final int DEFAULT_WIDTH = 400;
     
     /**
      * The default height.
      */
     private static final int DEFAULT_HEIGHT = 400;
     
     /**
      * The root pane.
      */
     private BorderPane root;
     
     /**
      * The scene.
      */
     private Scene scene;
     
     /**
      * The list of users.
      */
     private List<User> listOfUser;
     
     /**
      * Column names of conference users TableView.
      */
     private final String[] usersColumnNames = { "First Name", "Last Name" };
     
     /**
      * The Database variables used to populate the conference users TableView.
      */
     private final String[] usersVariableNames = { "firstName", "lastName" };
     
     private CustomTable<UserRow> usersTable;
     
     private ProgressSpinnerCallbacks progressSpinnerCallbacks;
     
     private AddUserCallback addUserCallback;
     
     public UsersPane(final Stage owner, final ProgressSpinnerCallbacks progressSpinnerCallbacks, final AddUserCallback addUserCallback) {
         this.progressSpinnerCallbacks = progressSpinnerCallbacks;
         this.addUserCallback = addUserCallback;
         
         root = new BorderPane();
         scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
         
        usersTable.setStyle("-fx-color: darkred; -fx-border-width: 1; -fx-border-color: aqua;");
         usersTable = new CustomTable<UserRow>(usersColumnNames, usersVariableNames);
         initModality(Modality.WINDOW_MODAL);
         initOwner(owner);
         
     }
     
     public void showDialog() {
         Text conferenceUsersText = new Text("Conference Users");
         conferenceUsersText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
         
         usersTable.setOnMouseClicked(this);
         
         root.setCenter(usersTable);
         
         setScene(scene);
         show();
         
         new LoadUsers(progressSpinnerCallbacks).start();
     }
     
     /**
      * Populates the users table.
      */
     private void populateTable() {
         if (listOfUser != null) {
             for (User u : listOfUser) {
                 //TODO add permission
                 usersTable.add(new UserRow(u.getID(), u.getFirstName(), u.getLastName(), null));
             }
             usersTable.updateItems();
         }
     }
     
     @Override
     public void handle(final Event event) {
         Object source = event.getSource();
         if (source == usersTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 close();
                 addUserCallback.addReviewer(usersTable.getSelectionModel()
                                                       .getSelectedItem()
                                                       .getID());
             }
         }
     }
     
     private class LoadUsers extends ProgressSpinnerService {
         
         /**
          * Creates a new LoadDataService.
          * 
          * @param progressSpinnerCallbacks Spinner that spins during database query.
          */
         public LoadUsers(final ProgressSpinnerCallbacks progressSpinnerCallbacks) {
             super(progressSpinnerCallbacks);
         }
         
         /**
          * Creates a new task for loading table lists.
          */
         @Override
         protected Task<String> createTask() {
             return new Task<String>() {
                 
                 /**
                  * Calls the new task.
                  */
                 @Override
                 protected String call() {
                     try {
                         listOfUser = UserManager.getUsers();
                         setSuccess(true);
                     }
                     catch (Exception e) {
                         // TODO show error
                     }
                     return null;
                 }
             };
         }
         
         /**
          * Called when data loading is done to populate tables
          */
         @Override
         protected void succeeded() {
             if (getSuccess()) {
                 populateTable();
             }
             super.succeeded();
         }
     }
     
 }
