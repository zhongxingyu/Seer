 package controller;
 
 import javafx.application.Platform;
 import javafx.fxml.FXML;
 import javafx.scene.control.MenuButton;
 import runner.Main;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Erick
  * Date: 8/30/13
  * Time: 2:13 AM
  */
 public class MenuController {
 
     private boolean wantsStudy = false;
     private boolean wantsReading = false;
     private boolean wantsQuiz = false;
     private boolean wantsLookAt = false;
 
 
     @FXML
     private MenuButton todo;
 
     private String coming = "Coming Soon...";
 
 
     public MenuController() {
 
     }
 
     @FXML
     public void getReading() {
         todo.setText(todo.getItems().get(0).getText());
 
         wantsStudy = false;
         wantsReading = true;
 
         wantsQuiz = false;
         wantsLookAt = false;
 
 
     }
 
 
     @FXML
     public void setRequestedStage() {
         System.out.println("Read " + wantsReading + ", Quiz " + wantsQuiz +
                 ", Study " + wantsStudy + ", Look At " + wantsLookAt);
 
         if (wantsReading) {
             System.out.println(wantsReading + " Wants Reading");
             Main.getStage().getScene().setRoot(Main.getReading());
 
         }
         if (wantsLookAt) {
             //Set 3 Card Stage
 //            Stage s = new Stage();
 //            s.initModality(Modality.APPLICATION_MODAL);
 //            s.initStyle(StageStyle.UNDECORATED);
 //            Scene scene = new Scene(Main.getLookAt(), Color.BURLYWOOD);
 //            s.setScene(scene);
 //            s.show();
         }
         if (wantsQuiz) {
             //Set 3 Card Stage
 //            Stage s = new Stage();
 //            s.initModality(Modality.APPLICATION_MODAL);
 //            s.initStyle(StageStyle.UNDECORATED);
 //            Scene scene = new Scene(Main.getQuiz(), Color.BURLYWOOD);
 //            s.setScene(scene);
 //            s.show();
         }
         if (wantsStudy) {
             //Set 3 Card Stage
 //            Stage s = new Stage();
 //            s.initStyle(StageStyle.UNDECORATED);
 //            Scene scene = new Scene(Main.getStudy(), Color.BURLYWOOD);
 //            s.setScene(scene);
 //            s.show();
         }
     }
 
     @FXML
     public void studyCards() {
         todo.setText(coming);
         wantsStudy = true;
         wantsReading = false;
         wantsQuiz = false;
         wantsLookAt = false;
     }
 
     @FXML
     public void takeQuiz() {
         todo.setText(coming);
         wantsStudy = false;
         wantsReading = false;
         wantsQuiz = true;
         wantsLookAt = false;
     }
 
     @FXML
     public void lookAtTarotDeck() {
         todo.setText(coming);
         wantsStudy = false;
         wantsReading = false;
         wantsQuiz = false;
         wantsLookAt = true;
     }
 
     @FXML
     public void close() {
         Platform.exit();
     }
 
 
     @FXML
     public void showProgress() {
         System.out.println("Progress");
     }
 
 }
