 package com.coacheller.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.coacheller.shared.FieldVerifier;
 import com.coacheller.shared.RatingGwt;
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class CoachellerRateComposite extends Composite {
 
   /**
    * The message displayed to the user when the server cannot be reached or
    * returns an error.
    */
   private static final String SERVER_ERROR = "An error occurred while "
       + "attempting to contact the server. Please check your network "
       + "connection and try again.";
 
   private static final String ADMIN_EMAIL = "afan@coacheller.com";
   private static final String ADMIN_ERROR = "You do not have permission to do this. Sorry.";
 
   private String ownerEmail = "";
 
   interface Binder extends UiBinder<Widget, CoachellerRateComposite> {
   }
 
   private static Binder uiBinder = GWT.create(Binder.class);
 
   private final CoachellerServiceAsync coachellerService = GWT.create(CoachellerService.class);
   private List<RatingGwt> ratingsList;
 
   @UiField
   Label title;
 
   @UiField
   Label infoBox;
 
   @UiField
   Label emailLabel;
 
   @UiField
   Label weekendLabel;
 
   @UiField
   Label scoreLabel;
 
   @UiField
   ListBox artistInput;
 
   @UiField
   ListBox weekendInput;
 
   @UiField
   ListBox scoreInput;
 
   @UiField
   com.google.gwt.user.client.ui.Button addRatingButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button reloadButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button recalculateButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button clearRatingButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button clearUserButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button backButton;;
 
   @UiField
   RatingsTable ratingsTable;
 
   public CoachellerRateComposite() {
     initWidget(uiBinder.createAndBindUi(this));
 
     initUiElements();
   }
 
   public CoachellerRateComposite(String ownerEmail) {
     this();
     this.ownerEmail = ownerEmail;
    retrieveSets();
    retrieveRatings();
     emailLabel.setText(ownerEmail);
   }
 
   private void initUiElements() {
     title.setText("Coachella Set Rater");
 
     ListDataProvider<RatingGwt> listDataProvider = new ListDataProvider<RatingGwt>();
     listDataProvider.addDataDisplay(ratingsTable);
     ratingsList = listDataProvider.getList();
 
     weekendLabel.setText("Weekend");
     scoreLabel.setText("Score");
 
     Element androidElement = getElement().getFirstChildElement().getFirstChildElement();
     final Animation androidAnimation = new AndroidAnimation(androidElement);
 
     weekendInput.addItem("1");
     weekendInput.addItem("2");
 
     scoreInput.addItem("1");
     scoreInput.addItem("2");
     scoreInput.addItem("3");
     scoreInput.addItem("4");
     scoreInput.addItem("5");
     scoreInput.setSelectedIndex(4);
 
     addRatingButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         addRating();
 
         androidAnimation.run(400);
       }
     });
 
     reloadButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         if (ownerEmail.equals(ADMIN_EMAIL)) {
           infoBox.setText("");
           coachellerService.loadSetData(new AsyncCallback<String>() {
 
             public void onFailure(Throwable caught) {
               // Show the RPC error message to the user
               infoBox.setText(SERVER_ERROR);
             }
 
             public void onSuccess(String result) {
               infoBox.setText(result);
             }
           });
 
           androidAnimation.run(400);
         } else {
           infoBox.setText(ADMIN_ERROR);
         }
       }
     });
 
     recalculateButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         if (ownerEmail.equals(ADMIN_EMAIL)) {
           infoBox.setText("");
           coachellerService.recalculateSetRatingAverages(new AsyncCallback<String>() {
 
             public void onFailure(Throwable caught) {
               // Show the RPC error message to the user
               infoBox.setText(SERVER_ERROR);
             }
 
             public void onSuccess(String result) {
               infoBox.setText(result);
             }
           });
 
           androidAnimation.run(400);
         } else {
           infoBox.setText(ADMIN_ERROR);
         }
       }
     });
 
     clearRatingButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         if (ownerEmail.equals(ADMIN_EMAIL)) {
           infoBox.setText("");
           coachellerService.deleteAllRatings(new AsyncCallback<String>() {
             public void onFailure(Throwable caught) {
               // Show the RPC error message to the user
               infoBox.setText(SERVER_ERROR);
             }
 
             public void onSuccess(String result) {
               infoBox.setText(result);
             }
           });
           androidAnimation.run(400);
         } else {
           infoBox.setText(ADMIN_ERROR);
         }
       }
     });
 
     clearUserButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         if (ownerEmail.equals(ADMIN_EMAIL)) {
           infoBox.setText("");
           coachellerService.deleteAllUsers(new AsyncCallback<String>() {
             public void onFailure(Throwable caught) {
               // Show the RPC error message to the user
               infoBox.setText(SERVER_ERROR);
             }
 
             public void onSuccess(String result) {
               infoBox.setText(result);
             }
           });
 
           androidAnimation.run(400);
         } else {
           infoBox.setText(ADMIN_ERROR);
         }
       }
     });
 
     backButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         FlowControl.go(new CoachellerViewComposite());
       }
     });
   }
 
   @Override
   public String getTitle() {
     return PageToken.RATE.getValue() + "=" + ownerEmail;
   }
 
   private void retrieveSets() {
     infoBox.setText("");
     coachellerService.getSetArtists("2012", null, new AsyncCallback<List<String>>() {
 
       public void onFailure(Throwable caught) {
         // Show the RPC error message to the user
         infoBox.setText(SERVER_ERROR);
       }
 
       public void onSuccess(List<String> result) {
         ArrayList<String> sortedTasks = new ArrayList<String>(result);
         Collections.sort(sortedTasks, SET_NAME_COMPARATOR);
 
         artistInput.clear();
         for (String artist : sortedTasks) {
           artistInput.addItem(artist);
         }
       }
     });
   }
 
   private void addRating() {
     infoBox.setText("");
     String artist = artistInput.getItemText(artistInput.getSelectedIndex());
     String weekend = weekendInput.getItemText(weekendInput.getSelectedIndex());
     String score = scoreInput.getItemText(scoreInput.getSelectedIndex());
     if (!FieldVerifier.isValidEmail(ownerEmail)) {
       infoBox.setText(FieldVerifier.EMAIL_ERROR);
       return;
     }
     if (!FieldVerifier.isValidWeekend(weekend)) {
       infoBox.setText(FieldVerifier.WEEKEND_ERROR);
       return;
     }
     if (!FieldVerifier.isValidScore(score)) {
       infoBox.setText(FieldVerifier.SCORE_ERROR);
       return;
     }
 
     // Then, we send the input to the server.
     coachellerService.addRatingBySetArtist(ownerEmail, artist, "2012", weekend, score,
         new AsyncCallback<String>() {
           public void onFailure(Throwable caught) {
             // Show the RPC error message to the user
             infoBox.setText(SERVER_ERROR);
           }
 
           public void onSuccess(String result) {
             infoBox.setText(result);
             retrieveRatings();
           }
         });
   }
 
   private void retrieveRatings() {
     coachellerService.getRatingsByUserEmail(ownerEmail, new AsyncCallback<List<RatingGwt>>() {
 
       public void onFailure(Throwable caught) {
         // Show the RPC error message to the user
         infoBox.setText(SERVER_ERROR);
       }
 
       public void onSuccess(List<RatingGwt> result) {
         ratingsList.clear();
         ratingsList.addAll(result);
         Collections.sort(ratingsList, RATING_NAME_COMPARATOR);
       }
     });
   }
 
   public static final Comparator<? super String> SET_NAME_COMPARATOR = new Comparator<String>() {
     public int compare(String t0, String t1) {
       return t0.compareTo(t1);
     }
   };
 
   public static final Comparator<? super RatingGwt> RATING_NAME_COMPARATOR = new Comparator<RatingGwt>() {
     public int compare(RatingGwt t0, RatingGwt t1) {
       // Sort by set time first
       if (t0.getScore() < t1.getScore()) {
         return 1;
       } else if (t0.getScore() > t1.getScore()) {
         return -1;
       } else {
         // Sort items alphabetically within each group
         return t0.getArtistName().compareTo(t1.getArtistName());
       }
     }
   };
 
   public static class RatingsTable extends CellTable<RatingGwt> {
 
     public Column<RatingGwt, String> artistNameColumn;
     public Column<RatingGwt, String> weekendColumn;
     public Column<RatingGwt, String> scoreColumn;
 
     interface TasksTableResources extends CellTable.Resources {
       @Source("RatingsTable.css")
       TableStyle cellTableStyle();
     }
 
     interface TableStyle extends CellTable.Style {
 
       String columnName();
 
       String columnScore();
 
     }
 
     private static TasksTableResources resources = GWT.create(TasksTableResources.class);
 
     public RatingsTable() {
       super(100, resources);
 
       artistNameColumn = new Column<RatingGwt, String>(new TextCell()) {
         @Override
         public String getValue(RatingGwt object) {
           return object.getArtistName();
         }
       };
       addColumn(artistNameColumn, "Artist Name");
       addColumnStyleName(0, "columnFill");
       addColumnStyleName(0, resources.cellTableStyle().columnName());
 
       weekendColumn = new Column<RatingGwt, String>(new TextCell()) {
         @Override
         public String getValue(RatingGwt object) {
           if (object.getWeekend() != null) {
             return object.getWeekend().toString();
           }
           return "";
         }
       };
       addColumn(weekendColumn, "Weekend");
       addColumnStyleName(1, "columnFill");
       addColumnStyleName(1, resources.cellTableStyle().columnScore());
 
       scoreColumn = new Column<RatingGwt, String>(new TextCell()) {
         @Override
         public String getValue(RatingGwt object) {
           if (object.getScore() != null) {
             return object.getScore().toString();
           }
           return "";
         }
       };
       addColumn(scoreColumn, "Score");
       addColumnStyleName(2, "columnFill");
       addColumnStyleName(2, resources.cellTableStyle().columnScore());
 
     }
   }
 
   class AndroidAnimation extends Animation {
     private static final int TOP = -50;
     private static final int BOTTOM = 150;
     Element element;
 
     public AndroidAnimation(Element element) {
       this.element = element;
     }
 
     @Override
     protected void onStart() {
       element.getStyle().setTop(TOP, Unit.PX);
     }
 
     @Override
     protected void onUpdate(double progress) {
       element.getStyle().setTop(TOP + (BOTTOM - TOP) * interpolate(progress), Unit.PX);
     }
 
     @Override
     protected void onComplete() {
       element.getStyle().setTop(TOP, Unit.PX);
     }
   }
 
 }
