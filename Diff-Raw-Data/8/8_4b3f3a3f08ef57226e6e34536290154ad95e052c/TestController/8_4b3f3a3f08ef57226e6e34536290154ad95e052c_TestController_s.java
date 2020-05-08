 package skrivfx;
 
 import classes.Word;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.animation.FadeTransition;
 import javafx.animation.ParallelTransition;
 import javafx.animation.RotateTransition;
 import javafx.animation.TranslateTransition;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ColorPicker;
 import javafx.scene.control.Slider;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.TextField;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Rectangle;
 import javafx.stage.FileChooser;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 
 public class TestController implements Initializable{
     
     // Other stuff
     private models.ModelSingleton model;
     private views.ViewSingleton view;
     private boolean hasReachedEnd = false;
     private WritingBoxEvent wbe;
     
     //These are objects injected from the FXML file:
     @FXML
     private ToggleButton menuButton;
     @FXML
     private ToggleButton writeButton;
     @FXML
     private Button openButton;
     @FXML
     private Button newButton;
     @FXML
     private Button saveButton;
     @FXML
     private Button closeButton;
     @FXML
     private Rectangle thumb;
     @FXML
     private Pane track;
     @FXML
     private Pane drawingPane;
     @FXML
     private TabPane tabPane;
     @FXML
     private ToggleButton highlightButton;
     @FXML
     private HBox writeMenu;
     @FXML
     private Button undoButton;
     @FXML
     private Button redoButton;
     @FXML
     private Button returnButton;
     @FXML
     private ToggleButton chatButton;
     @FXML
     private Button settingsButton;
     @FXML
     private Rectangle viewport;
     @FXML
     private AnchorPane chatPane;
     @FXML
     private ColorPicker colorPicker;
     @FXML
     private Slider strokeSlider;
     @FXML
     private AnchorPane settingsPane;
     @FXML
     private CheckBox hostCheckBox;
     @FXML
     private TextField portField;
     @FXML
     private TextField iPField;
     @FXML
     private ToggleButton collabToggle;
     
     //attributes
     private ParallelTransition parallelTransition;
    
     
 /*------------------------    Write Button    ------------------------*/
     @FXML
     private void handleWriteButtonAction(ActionEvent e){
         if(highlightButton.isSelected()){
             highlightButton.setSelected(false);
         }
         
         if (writeButton.isSelected()){
             writeMenu.setVisible(true);
             this.writeButtonOpen();
             
             
             if(tabPane.getTabs().size() != 0){    
                 viewport.setVisible(true);
                 
             }
         }
         else{
             this.writeButtonClose();
             viewport.setVisible(false);
             Thread t = new Thread(new Runnable(){
                 @Override
                 public void run(){
                     try{
                         Thread.sleep(50);
                     } catch(Exception e){ System.out.println("pause exception in write button"); }
                     writeMenu.setVisible(false);
                 }
             });
             t.start(); 
         } 
     }
     
     private void writeButtonOpen(){
         FadeTransition writeFade = 
         new FadeTransition(Duration.millis(250), writeMenu);
         writeFade.setFromValue(0.0);
         writeFade.setToValue(1.0);
         writeFade.setCycleCount(1);
         writeFade.setAutoReverse(true);
         
         TranslateTransition writeTranslate =
         new TranslateTransition(Duration.millis(100), writeMenu);
         writeTranslate.setFromX(-100);
         writeTranslate.setToX(-55);
         writeTranslate.setCycleCount(1);
         writeTranslate.setAutoReverse(true);
         
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 writeFade,
                 writeTranslate
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
     }
     
     private void writeButtonClose(){
         FadeTransition writeFade = 
         new FadeTransition(Duration.millis(50), writeMenu);
         writeFade.setFromValue(1.0);
         writeFade.setToValue(0.0);
         writeFade.setCycleCount(1);
         writeFade.setAutoReverse(true);
         
         TranslateTransition writeTranslate =
         new TranslateTransition(Duration.millis(50), writeMenu);
         writeTranslate.setToX(-300);
         writeTranslate.setCycleCount(1);
         writeTranslate.setAutoReverse(true);
         
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 writeFade,
                 writeTranslate
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
     }
     
 /*------------------------Animated Menu Button------------------------*/
     
     @FXML
     private void handleMenuButtonAction(){
         if (menuButton.isSelected()){
             newButton.setVisible(true);
             openButton.setVisible(true);
             saveButton.setVisible(true);
             closeButton.setVisible(true);
             settingsButton.setVisible(true);
             this.menuButtonOpen();          
         }
         else{
             this.menuButtonClose();
             Thread t = new Thread(new Runnable(){
                 @Override
                 public void run(){
                     try{
                         Thread.sleep(250);
                     } catch(Exception e){ System.out.println("pause exception"); }
                     TestController.this.hideAllMenuButtons();
                 }
             });
             t.start(); 
         }
     }
     
     //code reuse goes out the window:
     
     private void menuButtonOpen(){
         FadeTransition newFade = 
         new FadeTransition(Duration.millis(50), newButton);
         newFade.setFromValue(0.0);
         newFade.setToValue(1.0);
         newFade.setCycleCount(1);
         newFade.setAutoReverse(true);
         
         TranslateTransition newTranslate =
         new TranslateTransition(Duration.millis(50), newButton);
         newTranslate.setFromY(-100);
         newTranslate.setToY(5);
         newTranslate.setCycleCount(1);
         newTranslate.setAutoReverse(true);
         
         FadeTransition openFade = 
         new FadeTransition(Duration.millis(100), openButton);
         openFade.setFromValue(0.0);
         openFade.setToValue(1.0);
         openFade.setCycleCount(1);
         openFade.setAutoReverse(true);
         
         TranslateTransition openTranslate =
         new TranslateTransition(Duration.millis(100), openButton);
         openTranslate.setFromY(-100);
         openTranslate.setToY(5);
         openTranslate.setCycleCount(1);
         openTranslate.setAutoReverse(true);
         
         FadeTransition saveFade = 
         new FadeTransition(Duration.millis(150), saveButton);
         saveFade.setFromValue(0.0);
         saveFade.setToValue(1.0);
         saveFade.setCycleCount(1);
         saveFade.setAutoReverse(true);
         
         TranslateTransition saveTranslate =
         new TranslateTransition(Duration.millis(150), saveButton);
         saveTranslate.setFromY(-100);
         saveTranslate.setToY(5);
         saveTranslate.setCycleCount(1);
         saveTranslate.setAutoReverse(true);
         
         FadeTransition closeFade = 
         new FadeTransition(Duration.millis(200), closeButton);
         closeFade.setFromValue(0.0);
         closeFade.setToValue(1.0);
         closeFade.setCycleCount(1);
         closeFade.setAutoReverse(true);
         
         TranslateTransition closeTranslate =
         new TranslateTransition(Duration.millis(200), closeButton);
         closeTranslate.setFromY(-100);
         closeTranslate.setToY(5);
         closeTranslate.setCycleCount(1);
         closeTranslate.setAutoReverse(true);
         
         FadeTransition settingsFade = 
         new FadeTransition(Duration.millis(250), settingsButton);
         settingsFade.setFromValue(0.0);
         settingsFade.setToValue(1.0);
         settingsFade.setCycleCount(1);
         settingsFade.setAutoReverse(true);
         
         TranslateTransition settingsTranslate =
         new TranslateTransition(Duration.millis(250), settingsButton);
         settingsTranslate.setFromY(-100);
         settingsTranslate.setToY(5);
         settingsTranslate.setCycleCount(1);
         settingsTranslate.setAutoReverse(true);
         
         RotateTransition rotateTransition = 
         new RotateTransition(Duration.millis(150), menuButton);
         rotateTransition.setByAngle(90);
         rotateTransition.setCycleCount(1);
         rotateTransition.setAutoReverse(true);
 
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 newFade,
                 newTranslate,
                 
                 openFade,
                 openTranslate,
                 
                 saveFade,
                 saveTranslate,
                 
                 closeFade,
                 closeTranslate,
                 
                 settingsFade,
                 settingsTranslate,
                 
                 rotateTransition
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
 }
     private void menuButtonClose(){
         FadeTransition newFade = 
         new FadeTransition(Duration.millis(50), newButton);
         newFade.setFromValue(1.0);
         newFade.setToValue(0.0);
         newFade.setCycleCount(1);
         newFade.setAutoReverse(true);
         
         TranslateTransition newTranslate =
         new TranslateTransition(Duration.millis(50), newButton);
         newTranslate.setFromY(5);
         newTranslate.setToY(-100);
         newTranslate.setCycleCount(1);
         newTranslate.setAutoReverse(true);
         
         FadeTransition openFade = 
         new FadeTransition(Duration.millis(100), openButton);
         openFade.setFromValue(1.0);
         openFade.setToValue(0.0);
         openFade.setCycleCount(1);
         openFade.setAutoReverse(true);
         
         TranslateTransition openTranslate =
         new TranslateTransition(Duration.millis(100), openButton);
         openTranslate.setFromY(5);
         openTranslate.setToY(-100);
         openTranslate.setCycleCount(1);
         openTranslate.setAutoReverse(true);
         
         FadeTransition saveFade = 
         new FadeTransition(Duration.millis(150), saveButton);
         saveFade.setFromValue(1.0);
         saveFade.setToValue(0.0);
         saveFade.setCycleCount(1);
         saveFade.setAutoReverse(true);
         
         TranslateTransition saveTranslate =
         new TranslateTransition(Duration.millis(150), saveButton);
         saveTranslate.setFromY(5);
         saveTranslate.setToY(-100);
         saveTranslate.setCycleCount(1);
         saveTranslate.setAutoReverse(true);
         
         FadeTransition closeFade = 
         new FadeTransition(Duration.millis(150), closeButton);
         closeFade.setFromValue(1.0);
         closeFade.setToValue(0.0);
         closeFade.setCycleCount(1);
         closeFade.setAutoReverse(true);
         
         TranslateTransition closeTranslate =
         new TranslateTransition(Duration.millis(150), closeButton);
         closeTranslate.setFromY(5);
         closeTranslate.setToY(-100);
         closeTranslate.setCycleCount(1);
         closeTranslate.setAutoReverse(true);
         
         FadeTransition settingsFade = 
         new FadeTransition(Duration.millis(175), settingsButton);
         settingsFade.setFromValue(1.0);
         settingsFade.setToValue(0.0);
         settingsFade.setCycleCount(1);
         settingsFade.setAutoReverse(true);
         
         TranslateTransition settingsTranslate =
         new TranslateTransition(Duration.millis(175), settingsButton);
         settingsTranslate.setFromY(5);
         settingsTranslate.setToY(-100);
         settingsTranslate.setCycleCount(1);
         settingsTranslate.setAutoReverse(true);
         
         RotateTransition rotateTransition = 
         new RotateTransition(Duration.millis(150), menuButton);
         rotateTransition.setByAngle(-90);
         rotateTransition.setCycleCount(1);
         rotateTransition.setAutoReverse(true);
 
         parallelTransition = new ParallelTransition();
         parallelTransition.getChildren().addAll(
                 newFade,
                 newTranslate,
                 
                 openFade,
                 openTranslate,
                 
                 saveFade,
                 saveTranslate,
                 
                 closeFade,
                 closeTranslate,
                 
                 settingsFade,
                 settingsTranslate,
                 
                 rotateTransition
         );
         parallelTransition.setCycleCount(1);
         parallelTransition.play();
         if(settingsPane.isVisible()){
             this.closeSettingsPane();
         }
         
 }
     
 /*------------------------    Menu Buttons    ------------------------*/
     
     @FXML
     private void handleNewButtonAction(){
         if(tabPane.getTabs().size() != 0){
 //            Word word = new Word(view.getSnapshot(model.left(), model.top(), model.getWidth(), model.getHeight()));
 //            model.addWord(word);
 //            view.drawWord(word);
 //            view.clearWritingCanvas();
             //wbe.commitWord();
         }
         
         tabPane.getTabs().add(view.addTab(tabPane.widthProperty(), tabPane.heightProperty(), this));
         model.addPage();
         tabPane.getSelectionModel().select(model.getCurrentIndex());
         
         
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             this.hideAllMenuButtons();
         }
     }
     
     @FXML
     private void handleOpenButtonAction(){
         System.out.println("open button pressed");
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             this.hideAllMenuButtons();
             FileChooser fc = new FileChooser();
             fc.setTitle("Open skriv file...");
             File defaultDirectory = new File(".");
             fc.setInitialDirectory(defaultDirectory);
             fc.showOpenDialog(new Stage());
         }
     }
     
     @FXML
     private void handleSaveButtonAction(){
         System.out.println("save button pressed");
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             this.hideAllMenuButtons();
             
             FileChooser fc = new FileChooser();
             fc.setTitle("Save skriv file...");
             //File defaultDirectory = new File(".");
             //fc.setInitialDirectory(defaultDirectory);
             //File selectedDirectory = dc.showDialog(new Stage());
             //fc.showSaveDialog(new Stage());
             File f = fc.showSaveDialog(new Stage());
             System.out.println(f.getName());
             
             try{
                 FileOutputStream fos = new FileOutputStream(f);
                 ObjectOutputStream oos = new ObjectOutputStream(fos);
                 for(Word w : model.getWords()){
                     oos.writeObject(w);
                 }   
                 fos.close();
                 oos.close();
             } catch(Exception e){ System.out.println("oh dang..."); }
         }
     }
     @FXML
     private void handleCloseButtonAction(){
         tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());
         if(menuButton.isSelected()){
             this.menuButtonClose();
             menuButton.setSelected(false);
             this.hideAllMenuButtons();
             if(tabPane.getTabs().size() == 0){
                 this.writeButton.setSelected(false);
                 this.handleWriteButtonAction(null);
             }
         }
     }
     
     @FXML
     private void handleSettingsButtonAction(){
         if(menuButton.isSelected()){
 //            this.menuButtonClose();
 //            menuButton.setSelected(false);
 //            this.hideAllMenuButtons();
             if(settingsPane.isVisible()){
                 this.closeSettingsPane();
             }
             else{
                 this.openSettingsPane();
             }
         }
         
     }
     
     private void openSettingsPane(){
         settingsPane.setVisible(true);
         
         FadeTransition spFade = 
         new FadeTransition(Duration.millis(100), settingsPane);
         spFade.setFromValue(0.0);
         spFade.setToValue(1.0);
         spFade.setCycleCount(1);
         spFade.setAutoReverse(true);
         spFade.play();
     }
     private void closeSettingsPane(){
         FadeTransition spFade = 
         new FadeTransition(Duration.millis(100), settingsPane);
         spFade.setFromValue(1.0);
         spFade.setToValue(0.0);
         spFade.setCycleCount(1);
         spFade.setAutoReverse(true);
         spFade.play();
         
         Thread t = new Thread(new Runnable(){
             @Override
             public void run(){
                 try{
                     Thread.sleep(100);
                 } catch(Exception e){ System.out.println("pause exception"); }
                 settingsPane.setVisible(false);
             }
         });
         t.start();
         
     }
     
     @FXML
     private void handleCollabToggle(){
         if (collabToggle.isSelected()){
             collabToggle.setText("On");
             System.out.println("collaboration mode on");
         }
         else{
             collabToggle.setText("Off");
             System.out.println("collaboration mode off");
         }
     }
     
     private void hideAllMenuButtons(){
         newButton.setVisible(false);
         openButton.setVisible(false);
         saveButton.setVisible(false);
         closeButton.setVisible(false);
         settingsButton.setVisible(false);
         
     }
 
 /*------------------------  Toolbar Buttons  -------------------------*/
     @FXML
     private void handleReturnButtonPressed(){
         if(viewport.isVisible()){
             this.moveViewportDown();
             System.out.println("return button pressed");
             System.out.println(this.viewport.getY());
             System.out.println(this.viewport.isVisible());
         }
         else{
             System.out.println("viewport not initialized");
             System.out.println(this.viewport.getY());
             System.out.println(this.viewport.isVisible());
         }
     }
     
     @FXML
     private void handleChatButtonPressed(){
     if (chatButton.isSelected()){
         this.chatPane.setVisible(true);
         FadeTransition chatFade = 
         new FadeTransition(Duration.millis(100), chatPane);
         chatFade.setFromValue(0.0);
         chatFade.setToValue(1.0);
         chatFade.setCycleCount(1);
         chatFade.setAutoReverse(true);
         chatFade.play();
     }
     else{
         FadeTransition chatFade = 
         new FadeTransition(Duration.millis(100), chatPane);
         chatFade.setFromValue(1.0);
         chatFade.setToValue(0.0);
         chatFade.setCycleCount(1);
         chatFade.setAutoReverse(true);
         chatFade.play();
         Thread t = new Thread(new Runnable(){
             @Override
             public void run(){
                 try{
                     Thread.sleep(100);
                 } catch(Exception e){ System.out.println("pause exception"); }
                 chatPane.setVisible(false);
             }
         });
         t.start(); 
         
     }
 }
 /*------------------------      Viewport     -------------------------*/
     public void moveViewportDown(){
         if(viewport.getY() < 325){
             TranslateTransition vpTranslate =
             new TranslateTransition(Duration.millis(100), viewport);
             vpTranslate.setFromY(viewport.getY());
             vpTranslate.setToY(viewport.getY() + (viewport.getHeight()/2));
             vpTranslate.setCycleCount(1);
             vpTranslate.play();
             viewport.setY(viewport.getY() + (viewport.getHeight()/2));
         }
         else{
             viewport.setY(340);
             System.out.println("viewport is at end of page");
         }
         
     }
     
 /*------------------------      MiniMap      -------------------------*/
     
     @FXML
     private void scrollDragAction(MouseEvent evt){
         //System.out.println("Druggeded");
        // if (thumb.getHeight() track.getFitHeight())
         thumb.setOpacity(0.5);
         if(evt.getY()-thumb.getHeight()/2 > 0 && evt.getY()+thumb.getHeight()/2 < track.getHeight()) {
             thumb.setY(evt.getY()-thumb.getHeight()/2);
         }
         if (evt.getY()-thumb.getHeight()/2<=0) {
             thumb.setY(0);
         }
         if (evt.getY()+thumb.getHeight()>=track.getHeight()) {
             thumb.setY(track.getHeight()-thumb.getHeight());
         }
     }
     
     @FXML
     private void onMouseEntered(MouseEvent evt){
         thumb.setOpacity(0.5);
     }
     
     @FXML
     private void onMouseExited(MouseEvent evt){
         thumb.setOpacity(0.3);
     }
     
     @FXML
     private void minimouserelease(MouseEvent evt){
         thumb.setOpacity(0.5);
         if(evt.getY()-thumb.getHeight()/2 > 0 && evt.getY()+thumb.getHeight()/2 < track.getHeight()) {
             thumb.setY(evt.getY()-thumb.getHeight()/2);
         }
         if (evt.getY()-thumb.getHeight()/2<=0) {
             thumb.setY(0);
         }
         if (evt.getY()+thumb.getHeight()>=track.getHeight()) {
             thumb.setY(track.getHeight()-thumb.getHeight());
         }
     }
     
     @FXML
     private void minimousedragged(MouseEvent evt){
         thumb.setOpacity(0.5);
         if(evt.getY()-thumb.getHeight()/2 > 0 && evt.getY()+thumb.getHeight()/2 < track.getHeight()) {
             thumb.setY(evt.getY()-thumb.getHeight()/2);
         }
         if (evt.getY()-thumb.getHeight()/2<=0) {
             thumb.setY(0);
         }
         if (evt.getY()+thumb.getHeight()>=track.getHeight()) {
             thumb.setY(track.getHeight()-thumb.getHeight());
         }
     }
     
     @FXML
     private void minimousepress(MouseEvent evt){
         thumb.setOpacity(0.5);
         if(evt.getY()-thumb.getHeight()/2 > 0 && evt.getY()+thumb.getHeight()/2 < track.getHeight()) {
             thumb.setY(evt.getY()-thumb.getHeight()/2);
         }
         if (evt.getY()-thumb.getHeight()/2<=0) {
             thumb.setY(0);
         }
         if (evt.getY()+thumb.getHeight()>=track.getHeight()) {
             thumb.setY(track.getHeight()-thumb.getHeight());
         }
     }
 /*------------------------  Mouse & Keyboard  ------------------------*/
     
     @FXML
     private void keyboardShortcut(KeyEvent kp){ 
         System.out.println(kp.getText());
         
     }
         
     
     @Override
     public void initialize(URL url, ResourceBundle rb){
         model = models.ModelSingleton.getInstance();
         view = views.ViewSingleton.getInstance();
         view.makeWritingCanvas(drawingPane.widthProperty(), drawingPane.heightProperty());
         view.makeMinimapCanvas(drawingPane.widthProperty(), drawingPane.heightProperty());
      
         wbe = new WritingBoxEvent();
         view.addHandlers(wbe);
         view.setLineWidth(3);
 
         drawingPane.getChildren().add(view.getWritingCanvas());
         colorPicker.setValue(javafx.scene.paint.Color.BLACK);
         
         // For when a different Tab is selected
         tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>(){
             @Override
             public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                 wbe.commitWord();
                 view.setCurrentIndex(tabPane.getSelectionModel().getSelectedIndex());
                 model.setCurrentIndex(tabPane.getSelectionModel().getSelectedIndex());
                 
             }
         });
         
         strokeSlider.valueProperty().addListener(new ChangeListener<Number>(){
             @Override
             public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                 view.setLineWidth(strokeSlider.getValue());
             }         
         });
         
         colorPicker.valueProperty().addListener(new ChangeListener<Color>(){
             @Override
             public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                 view.setStrokeColor(colorPicker.getValue());
             }  
         });
     }
 }
