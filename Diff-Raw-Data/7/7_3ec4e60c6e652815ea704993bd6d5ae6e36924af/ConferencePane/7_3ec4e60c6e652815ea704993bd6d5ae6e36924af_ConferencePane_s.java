 
 package view.conferences;
 
 import java.util.List;
 
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.text.Text;
 import model.conferences.Conference;
 import model.conferences.ConferenceManager;
 import model.conferences.ConferenceUser;
 import model.papers.Paper;
 import model.papers.PaperManager;
 import model.permissions.PermissionLevel;
 import view.papers.PaperRow;
 import view.papers.UploadPaperPane;
 import view.users.UsersPane;
 import view.util.Callbacks;
 import view.util.CustomTable;
 import view.util.GenericPane;
 import view.util.MainPaneCallbacks;
 import view.util.MessageDialog;
 import view.util.ProgressSpinnerCallbacks;
 import view.util.ProgressSpinnerService;
 
 /**
  * JavaFX pane responsible for displaying information about a selected conference.
  * 
  * @author Mohammad Juma
  * @version 11-23-2013
  */
 public class ConferencePane extends GenericPane<GridPane> implements EventHandler, AddUserCallback {
     
     /**
      * Number of clicks for a double click.
      */
     private static final int DOUBLE_CLICK = 2;
     
     /**
      * Column names of conference papers TableView.
      */
     private final String[] conferencePapersColumnolumnNames = { "Paper Name", "Date" };
     
     /**
      * The Database variables used to populate the conference papers TableView.
      */
     private final String[] conferencePapersVariableNames = { "paperName", "date" };
     
     /**
      * Column names of conference users TableView.
      */
     private final String[] conferenceUsersColumnNames = { "Name", "Role" };
     
     /**
      * The Database variables used to populate the conference users TableView.
      */
     private final String[] conferenceUsersVariableNames = { "name", "role" };
     
     /**
      * The name of the conference.
      */
     private final Text conferenceNameText;
     
     /**
      * The location of the conference.
      */
     private final Text conferenceLocationText;
     
     /**
      * The date of the conference.
      */
     private final Text conferenceDateText;
     
     /**
      * The program chair of the conference.
      */
     private final Text conferenceProgramChairText;
     
     /**
      * The number of authors in the conference.
      */
     private final Text authorsText;
     
     /**
      * The number of reviewers in the conference.
      */
     private final Text reviewersText;
     
     /**
      * The id of the conference.
      */
     private final int conferenceID;
     
     /**
      * List of papers in the conference.
      */
     private CustomTable<PaperRow> conferencePapersTable;
     
     /**
      * List of users in the conference.
      */
     private CustomTable<ConferenceUserRow> conferenceUsersTable;
     
     /**
      * The list of papers in the conference.
      */
     private List<Paper> listOfPapers;
     
     /**
      * The list of users in the conference.
      */
     private List<ConferenceUser> listOfUsers;
     
     private Button removeConferenceButton;
     
     private Button addSubprogramChairButton;
     
     private Button addReviewerButton;
     
     private Button uploadPaperButton;
     
     private Button uploadReviewButton;
     
     private Button viewPaperButton;
     
     private Button viewUserButton;
     
     private Button assignPaperButton;
     
     /**
      * Constructs a new Conference Pane that extends GridPane and displays the information about
      * the given conference.
      */
     public ConferencePane(final Conference conference, final Callbacks callbacks, final MainPaneCallbacks mainPaneCallbacks,
             final ProgressSpinnerCallbacks progressSpinnerCallbacks) {
         super(new GridPane(), callbacks);
         addMainPaneCallBacks(mainPaneCallbacks);
         addProgressSpinnerCallBacks(progressSpinnerCallbacks);
         
         conferenceID = conference.getID();
         conferenceNameText = new Text("Conference: " + conference.getName());
         conferenceNameText.setId("conf-text");
         conferenceLocationText = new Text("Location: " + conference.getLocation());
         conferenceLocationText.setId("conf-text");
         conferenceDateText = new Text("Date: " + conference.getDate()
                                                            .toString());
         conferenceDateText.setId("conf-text");
         conferenceProgramChairText = new Text("Program Chair: " + conference.getProgramChair());
         conferenceProgramChairText.setId("conf-text");
         authorsText = new Text("Authors: " + Integer.toString(conference.getAuthors()));
         authorsText.setId("conf-text");
         reviewersText = new Text("Reviewers: " + Integer.toString(conference.getReviewers()));
         reviewersText.setId("conf-text");
        
         conferencePapersTable = new CustomTable<PaperRow>(conferencePapersColumnolumnNames, conferencePapersVariableNames);
         
         conferenceUsersTable = new CustomTable<ConferenceUserRow>(conferenceUsersColumnNames, conferenceUsersVariableNames);
         
         pane.setAlignment(Pos.TOP_LEFT);
         pane.setHgap(10);
         pane.setVgap(10);
         pane.setPadding(new Insets(0, 5, 5, 5));
         
         create();
     }
     
     //TODO need to add permission checks for buttons
     /**
      * Creates the main components of the ConferencePane pane.
      */
     private void create() {
         new LoadDataService(progressSpinnerCallbacks).start();
         
         pane.add(conferenceNameText, 0, 0);
         pane.add(conferenceLocationText, 1, 0);
         pane.add(conferenceDateText, 0, 1);
         pane.add(conferenceProgramChairText, 1, 1);
         pane.add(authorsText, 0, 2);
         pane.add(reviewersText, 1, 2);
         
         Text conferencePapersText = new Text("Conference Papers");
        
         conferencePapersText.setId("conf-title");
        //   conferencePapersText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
         pane.add(conferencePapersText, 0, 3);
         conferencePapersTable.setOnMouseClicked(this);
         pane.add(conferencePapersTable, 0, 4);
         
         Text conferenceUsersText = new Text("Conference Users");
        
         conferenceUsersText.setId("conf-title");
         
         pane.add(conferenceUsersText, 0, 5);
         conferenceUsersTable.setOnMouseClicked(this);
         pane.add(conferenceUsersTable, 0, 6);
         
         removeConferenceButton = new Button("Remove Conference");
         removeConferenceButton.setOnAction(this);
         
         addSubprogramChairButton = new Button("Add Subprogram Chair");
         addSubprogramChairButton.setOnAction(this);
         
         addReviewerButton = new Button("Add Reviewer");
         addReviewerButton.setOnAction(this);
         
         assignPaperButton = new Button("Assign Paper");
         assignPaperButton.setOnAction(this);
         
         uploadPaperButton = new Button("Upload Paper");
         uploadPaperButton.setOnAction(this);
         
         uploadReviewButton = new Button("Upload Review");
         uploadReviewButton.setOnAction(this);
         
         HBox bottomBox = new HBox(12);
         bottomBox.getChildren()
                  .add(removeConferenceButton);
         bottomBox.getChildren()
                  .add(addSubprogramChairButton);
         bottomBox.getChildren()
                  .add(addReviewerButton);
         bottomBox.getChildren()
                  .add(uploadPaperButton);
         bottomBox.getChildren()
                  .add(uploadReviewButton);
         
         pane.add(bottomBox, 0, 7);
     }
     
     /**
      * Populates the tables with data from the database.
      */
     private void populate() {
         if (listOfPapers != null) {
             for (Paper p : listOfPapers) {
                 conferencePapersTable.add(new PaperRow(p.getPaperID(), p.getTitle(), p.getSubmissionDate()));
             }
             conferencePapersTable.updateItems();
         }
         if (listOfUsers != null) {
             for (ConferenceUser u : listOfUsers) {
                 conferenceUsersTable.add(new ConferenceUserRow(u.getUserID(), u.getUsername(), u.getRole()));
             }
             conferenceUsersTable.updateItems();
         }
     }
     
     /**
      * Event handler for handling table and button click events.
      */
     @Override
     public void handle(final Event event) {
         Object source = event.getSource();
         if (source == conferencePapersTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 int paperID = conferencePapersTable.getSelectionModel()
                                                    .getSelectedItem()
                                                    .getId();
             }
         }
         if (source == conferenceUsersTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 int userID = conferenceUsersTable.getSelectionModel()
                                                  .getSelectedItem()
                                                  .getID();
             }
         }
         
         if (source == addReviewerButton) {
             new UsersPane(callbacks.getPrimaryStage(), progressSpinnerCallbacks, this).showDialog();
         }
         
         if (source == addSubprogramChairButton) {
             //TODO finish
         }
         if (source == uploadPaperButton) {
             mainPaneCallbacks.pushPane(new UploadPaperPane(conferenceID, callbacks, mainPaneCallbacks, progressSpinnerCallbacks));
         }
     }
     
     @Override
     public void addReviewer(final int userID) {
         new AddUserService(progressSpinnerCallbacks, conferenceID, userID, PermissionLevel.REVIEWER).start();
     }
     
     /**
      * Adds a user to the conference
      * 
      * 
      */
     private class AddUserService extends ProgressSpinnerService {
         
         private int conferenceID;
         private int userID;
         private PermissionLevel permission;
         
         public AddUserService(final ProgressSpinnerCallbacks progressSpinnerCallbacks, final int ConferenceID, final int UserID,
                 final PermissionLevel permission) {
             super(progressSpinnerCallbacks);
             
             this.conferenceID = ConferenceID;
             this.userID = UserID;
             this.permission = permission;
         }
         
         @Override
         protected Task<String> createTask() {
             return new Task<String>() {
                 
                 /**
                  * Calls the new task.
                  */
                 @Override
                 protected String call() {
                     try {
                         ConferenceManager.addUserToConference(conferenceID, userID, permission);
                         setSuccess(true);
                         System.out.println("adding " + userID);
                     }
                     catch (Exception e) {
                         //TODO make sure message dialog works
                         new MessageDialog(callbacks.getPrimaryStage()).showDialog(e.getMessage(), false);
                         
                     }
                     return null;
                 }
             };
         }
         
         @Override
         protected void succeeded() {
             if (getSuccess()) {
                 //  populate();
             }
             super.succeeded();
         }
     }
     
     /**
      * Loads conference, paper, and review data from database.
      */
     private class LoadDataService extends ProgressSpinnerService {
         
         /**
          * Creates a new LoadDataService.
          * 
          * @param progressSpinnerCallbacks Spinner that spins during database query.
          */
         public LoadDataService(final ProgressSpinnerCallbacks progressSpinnerCallbacks) {
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
                         listOfPapers = PaperManager.getPapers(conferenceID);
                         listOfUsers = ConferenceManager.getUsersInConference(conferenceID);
                         
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
                 populate();
             }
             super.succeeded();
         }
     }
 }
