 package controller;
 
 import javafx.animation.KeyFrame;
 import javafx.animation.KeyValue;
 import javafx.animation.Timeline;
 import javafx.application.Platform;
 import javafx.fxml.FXML;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.MenuButton;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 import runner.Main;
 
 /**
  * Created with Intellij IDEA.
  * User: Erick
  * Date: 8/30/13
  * Time: 1:13 PM
  */
 
 public class ReadingController {
 
     @FXML
     private String enabled = "Reading Helper Enabled";
     @FXML
     private String disabled = "Reading Helper Disabled";
 
 
     private static Stage stage;
 
     @FXML
     private ToggleButton helpToggle;
 
     @FXML
     private Text selection;
 
     @FXML
     private MenuButton typeOfReading;
 
     @FXML
     private CheckBox invert;
 
     ///////////
     // boole's to keep track of what user wants
     private boolean wantsHelp = true;
     private boolean wants3Card = false;
     private boolean invertCards = false;
 
 
     public static Stage getStage() {
         return stage;
     }
 
     public ReadingController() {
        System.out.println(wantsHelp);
     }
 
     @FXML
     public void wants3Card() {
         wants3Card = true;
         typeOfReading.setText(typeOfReading.getItems().get(0).getText());
     }
 
     public static void setStage(Stage stage) {
         ReadingController.stage = stage;
     }
 
     public void setWantsHelp(boolean wantsHelp) {
         this.wantsHelp = wantsHelp;
     }
 
     @FXML
     private void setHelp() {
 
         helpToggle.setText(helpToggle.isSelected() ? disabled : enabled);
 
         setWantsHelp(helpToggle.isSelected() ? false : true);
 
         flashSelection(helpToggle.getText());
 
         System.out.println("Help = " + wantsHelp);
     }
 
     @FXML
     public void close() {
         Main.getStage().getScene().setRoot(Main.getMenu());
         Main.getStage().setWidth(540.0);
         Main.getStage().setHeight(160.0);
     }
 
     public void setInvert(boolean b) {
         invertCards = b;
     }
 
     @FXML
     public void invertSelected() {
         setInvert(invert.isSelected() ? true : false);
         flashSelection(invertCards ? "Inverted Cards On" : "Inverted Cards Off");
         System.out.println(invertCards);
     }
 
     @FXML
     public void exit() {
         Platform.exit();
     }
 
     private void flashSelection(String text) {
         selection.setText(text);
         Timeline ani = new Timeline();
         KeyFrame frame = new KeyFrame(Duration.millis(0), new KeyValue(selection.opacityProperty(), 1.0f));
         KeyFrame fadeFrame = new KeyFrame(Duration.millis(2000), new KeyValue(selection.opacityProperty(), 0.0f));
         ani.getKeyFrames().addAll(frame, fadeFrame);
         ani.play();
     }
 }
