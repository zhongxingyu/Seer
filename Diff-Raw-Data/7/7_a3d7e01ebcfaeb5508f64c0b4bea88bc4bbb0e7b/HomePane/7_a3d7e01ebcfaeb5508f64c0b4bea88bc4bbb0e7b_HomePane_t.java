 
 package view.home;
 
 import java.util.List;
 
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.text.Text;
 import model.conferences.Conference;
 import model.conferences.ConferenceManager;
 import model.papers.Paper;
 import model.papers.PaperManager;
 import model.reviews.Review;
 import model.reviews.ReviewManager;
 import view.conferences.ConferencePane;
 import view.conferences.ConferenceRow;
 import view.papers.PaperPane;
 import view.papers.PaperRow;
 import view.util.CenterPaneCallbacks;
 import view.util.CustomReviewRow;
 import view.util.CustomTable;
 import view.util.GenericPane;
 import view.util.MessageDialog;
 import view.util.ProgressSpinnerCallbacks;
 import view.util.ProgressSpinnerService;
 import view.util.SceneCallbacks;
 import controller.user.LoggedUser;
 
 /**
  * JavaFX pane responsible for displaying the users home interface.
  *
  * @author Mohammad Juma
  * @version 11-11-2013
  */
 public class HomePane extends GenericPane<GridPane> implements EventHandler {
     
     /**
      * Number of clicks for a double click.
      */
     private static final int DOUBLE_CLICK = 2;
     
     /**
      * Column names of conferences in TableView.
      */
     private static final String[] conferencesColumnNames = { "Conference Name", "Program Chair", "Authors",
             "Reviewers", "Date" };
     
     /**
      * The variable names for the conferences table used by Java FX's table classes
      */
     private static final String[] conferencesVariableNames = { "name", "programChair", "authors", "reviewers", "date" };
     
     /**
      * Column names of papers in TableView.
      */
     private static final String[] papersColumnNames = { "Paper Name", "Conference Name", "Reviewed", "Revised",
             "Submission Date" };
     
     /**
      * The variable names for the papers table used by Java FX's table classes
      */
     private static final String[] papersVariableNames = { "paperName", "conferenceName", "reviewed", "revised", "date" };
     
     /**
      * Column names of reviews in TableView.
      */
     private static final String[] reviewsColumnsNames = { "Paper Name", "Author", "Conference Name", "Reviewed",
             "Submission Date" };
     
     /**
      * The variable names for the reviews table used by Java FX's table classes
      */
     private static final String[] reviewsVariableNames = { "paperName", "author", "conferenceName", "reviewed", "date" };
     
     /**
      * A table for the conferences the user is a Program Chair of.
      */
     private CustomTable<ConferenceRow> conferencesTable;
     
     /**
      * A table for the papers the user is an author of.
      */
     private CustomTable<PaperRow> papersTable;
     
     /**
      * A table for the reviews the user has written.
      */
     private CustomTable<CustomReviewRow> reviewsTable;
     
     /**
      * The list of conferences the user is a Program Chair of.
      */
     private List<Conference> listOfConferences;
     
     /**
      * The list of papers the user has written.
      */
     private List<Paper> listOfPapers;
     
     /**
      * The list of reviews the user has written.
      */
     private List<Review> listOfReviews;
     
     /**
      * Constructs a new HomePane pane that extends GridPane and displays the initial user
      * interface the user is greeted with upon login in.
      * 
      * @param sceneCallback A callback to the scene this pane is in
      * @param centerPaneCallback A callback to the center pane
      * @param progressSpinnerCallback A callback to the progress spinner
      */
     public HomePane(final SceneCallbacks sceneCallback, final CenterPaneCallbacks centerPaneCallback,
             final ProgressSpinnerCallbacks progressSpinnerCallback) {
         super(new GridPane(), sceneCallback);
         addCenterPaneCallBacks(centerPaneCallback);
         addProgressSpinnerCallBack(progressSpinnerCallback);
         
         conferencesTable = new CustomTable<ConferenceRow>(conferencesColumnNames, conferencesVariableNames);
         papersTable = new CustomTable<PaperRow>(papersColumnNames, papersVariableNames);
         reviewsTable = new CustomTable<CustomReviewRow>(reviewsColumnsNames, reviewsVariableNames);
         
         pane.setAlignment(Pos.TOP_LEFT);
         pane.setHgap(10);
         pane.setVgap(10);
         pane.setPadding(new Insets(0, 5, 5, 5));
         
         LoggedUser.getInstance().clearPermissions();
         
        create();
     }
     
     /**
      * Refreshes the current pane after data has been changed.
      */
     @Override
     public GenericPane<GridPane> refresh() {
         return new HomePane(sceneCallback, centerPaneCallback, progressSpinnerCallback);
     }
     
     /**
      * Creates the main components of the HomePane pane.
      */
     private void create() {
         final Text myConferencesText = new Text("My Conferences");
         myConferencesText.setId("header2");
         conferencesTable.setOnMouseClicked(this);
         pane.add(myConferencesText, 0, 0);
         pane.add(conferencesTable, 0, 1);
         
         final Text myPapersText = new Text("My Papers");
         myPapersText.setId("header2");
         papersTable.setOnMouseClicked(this);
         pane.add(myPapersText, 0, 3);
         pane.add(papersTable, 0, 4);
         
         final Text myReviewsText = new Text("My Reviews");
         myReviewsText.setId("header2");
         reviewsTable.setOnMouseClicked(this);
         pane.add(myReviewsText, 0, 6);
         pane.add(reviewsTable, 0, 7);
        
        new LoadDataService(progressSpinnerCallback).start();
     }
     
     /**
      * Populates the tables from the database.
      */
     private void populate() {
         if (listOfConferences != null) {
             for (Conference conference : listOfConferences) {
                 conferencesTable.add(new ConferenceRow(conference.getID(), conference.getName(), conference
                         .getLocation(), conference.getDate(), conference.getProgramChair(), conference.getAuthors(),
                         conference.getReviewers()));
             }
             conferencesTable.updateItems();
         }
         if (listOfPapers != null) {
             for (Paper paper : listOfPapers) {
                 papersTable.add(new PaperRow(paper.getPaperID(), paper.getTitle(), paper.getConferenceName(), paper
                         .getStatus().getStringValue(), paper.getRevised(), paper.getSubmissionDate()));
             }
             papersTable.updateItems();
         }
         if (listOfReviews != null) {
             Paper p = null;
             for (Review review : listOfReviews) {
                 p = Paper.paperFromID(review.getPaperID());
                 reviewsTable.add(new CustomReviewRow(review.getID(), p.getTitle(), p.getConferenceName()));
             }
             reviewsTable.updateItems();
         }
     }
     
     /**
      * Handles user input
      */
     @Override
     public void handle(final Event event) {
         if (event.getSource() == conferencesTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 int conferenceID = conferencesTable.getSelectionModel().getSelectedItem().getID();
                 centerPaneCallback.pushPane(new ConferencePane(conferenceID, sceneCallback, centerPaneCallback,
                         progressSpinnerCallback));
             }
         }
         else if (event.getSource() == papersTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 int paperID = papersTable.getSelectionModel().getSelectedItem().getId();
                 centerPaneCallback.pushPane(new PaperPane(paperID, sceneCallback, centerPaneCallback,
                         progressSpinnerCallback));
             }
         }
         else if (event.getSource() == reviewsTable) {
             MouseEvent mouseEvent = (MouseEvent) event;
             if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
                 int reviewID = reviewsTable.getSelectionModel().getSelectedItem().getId();
                 //centerPaneCallback.pushPane(new ReviewPane(reviewID, sceneCallback, centerPaneCallback,
                 //       progressSpinnerCallback));
                 // TODO uncomment me!
             }
         }
         
     }
     
     /**
      * Loads conference, paper, and review data from database.
      */
     private class LoadDataService extends ProgressSpinnerService {
         
         /**
          * Creates a new LoadDataService.
          *
          * @param progressSpinnerCallback Spinner that spins during database query.
          */
         public LoadDataService(final ProgressSpinnerCallbacks progressSpinnerCallback) {
             super(progressSpinnerCallback);
         }
         
         /**
          * Creates a new task for loading conferences
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
                         int id = LoggedUser.getInstance().getUser().getID();
                         listOfConferences = ConferenceManager.getConferencesForUser(id);
                         listOfPapers = PaperManager.getAuthorsSubmittedPapers(id);
                         
                         for (Paper p : listOfPapers) {
                             listOfReviews.add(ReviewManager.getSubmittedReview(p.getPaperID(), id));
                         }
                         setSuccess(true);
                     }
                     catch (Exception e) {
                         new MessageDialog(sceneCallback.getPrimaryStage()).showDialog(e.getMessage(), false);
                     }
                     return null;
                 }
             };
         }
         
         /**
          * Called when a conference loading is done to populate table
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
