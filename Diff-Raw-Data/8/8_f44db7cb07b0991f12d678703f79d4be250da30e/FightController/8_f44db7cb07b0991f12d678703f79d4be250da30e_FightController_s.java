 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.gui;
 
 import brutes.ScenesContext;
 import brutes.game.ObservableCharacter;
 import brutes.net.Protocol;
 import brutes.net.client.ErrorResponseException;
 import brutes.net.client.InvalidResponseException;
 import brutes.net.client.NetworkClient;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.concurrent.Worker;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.MenuItem;
 import javafx.scene.image.ImageView;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 
 /**
  * FXML Controller class
  *
  * @author Karl
  */
 public class FightController implements Initializable {
     private ArrayList<Stage> openedStages;
     
     @FXML
     private Text myName;
     @FXML
     private Text myLevel;
     @FXML
     private Text myLifes;
     @FXML
     private Text myStrength;
     @FXML
     private Text mySpeed;
     @FXML
     private Text myBonus1;
     @FXML
     private Text myBonus2;
     @FXML
     private Text myBonus3;
     @FXML
     private ImageView myImage;
     @FXML
     private ImageView myBonus1Image;
     @FXML
     private ImageView myBonus2Image;
     @FXML
     private ImageView myBonus3Image;
     @FXML
     private Text chName;
     @FXML
     private Text chLevel;
     @FXML
     private Text chLifes;
     @FXML
     private Text chStrength;
     @FXML
     private Text chSpeed;
     @FXML
     private Text chBonus1;
     @FXML
     private Text chBonus2;
     @FXML
     private Text chBonus3;
     @FXML
     private ImageView chImage;
     @FXML
     private ImageView chBonus1Image;
     @FXML
     private ImageView chBonus2Image;
     @FXML
     private ImageView chBonus3Image;
     @FXML
     private MenuItem menuFightWin;
     @FXML
     private MenuItem menuFightLoose;
     @FXML
     private MenuItem menuFightRandom;
     @FXML
     private MenuItem menuFightRegular;
     
     private void doFight(FightTask.FightType type){
         menuFightWin.setDisable(true);
         menuFightLoose.setDisable(true);
         menuFightRandom.setDisable(true);
         menuFightRegular.setDisable(true);
         FightTask fightTask = new FightTask(type);
         fightTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
             @Override
             public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newState) {
                 if(newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED){
                     menuFightWin.setDisable(false);
                     menuFightLoose.setDisable(false);
                     menuFightRandom.setDisable(false);
                     menuFightRegular.setDisable(false);
                     ScenesContext.getInstance().getSession().netLoadMyCharacter();
                     ScenesContext.getInstance().getSession().netLoadChallengerCharacter();
                 }
             }
         });
         new Thread(fightTask).start();
     }
     
     @FXML
     private void handleMenuFightWin(ActionEvent e){
         this.doFight(FightTask.FightType.CHEAT_WIN);
     }
     @FXML
     private void handleMenuFightLoose(ActionEvent e){
         this.doFight(FightTask.FightType.CHEAT_LOOSE);
     }
     @FXML
     private void handleMenuFightRandom(ActionEvent e){
         this.doFight(FightTask.FightType.CHEAT_RANDOM);
     }
     @FXML
     private void handleMenuFightRegular(ActionEvent e){
         this.doFight(FightTask.FightType.REGULAR);
     }
     @FXML
     private void handleMenuCharacterNew(ActionEvent e){
         try {
             Parent root = FXMLLoader.load(this.getClass().getResource("CreateCharacter.fxml"));
             Scene scene = new Scene(root);
             Stage window = new Stage();
             window.setScene(scene);
            window.setTitle("Nouveau personnage");
             window.setResizable(false);
             this.openedStages.add(window);
             window.show();
         } catch (IOException ex) {
             Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     @FXML
     private void handleMenuCharacterUpdate(ActionEvent e){
         try {
             Parent root = FXMLLoader.load(this.getClass().getResource("UpdateCharacter.fxml"));
             Scene scene = new Scene(root);
             Stage window = new Stage();
             window.setScene(scene);
            window.setTitle("Modifier son personnage");
             window.setResizable(false);
             this.openedStages.add(window);
             window.show();
         } catch (IOException ex) {
             Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     @FXML
     private void handleMenuCharacterDelete(ActionEvent e){
         try {
             Parent root = FXMLLoader.load(this.getClass().getResource("DeleteCharacter.fxml"));
             Scene scene = new Scene(root);
             Stage window = new Stage();
             window.setScene(scene);
            window.setTitle("Supprimer son personnage");
             window.setResizable(false);
             this.openedStages.add(window);
             window.show();
         } catch (IOException ex) {
             Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     @FXML
     private void handleMenuDisconnect(ActionEvent e){
         new Thread(){
             @Override
             public void run() {
                 try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                     try {
                         connection.sendLogout(ScenesContext.getInstance().getSession().getToken());
                     } catch (InvalidResponseException | ErrorResponseException ex) {
                         Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (IOException ex) {
                     Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }.start();
         for(Iterator<Stage> it = this.openedStages.iterator(); it.hasNext();){
             it.next().close();
         }
         this.openedStages.clear();
         ScenesContext.getInstance().showLogin();
     }
     
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         this.openedStages = new ArrayList<>();
         
         ObservableCharacter me = ScenesContext.getInstance().getSession().getMyCharacter();
         ObservableCharacter ch = ScenesContext.getInstance().getSession().getChallengerCharacter();
         
         this.myName.textProperty().bind(me.getName());
         this.myLevel.textProperty().bind(me.getLevel().asString());
         this.myLifes.textProperty().bind(me.getLife().asString());
         this.myStrength.textProperty().bind(me.getStrength().asString());
         this.mySpeed.textProperty().bind(me.getSpeed().asString());
         this.myBonus1.textProperty().bind(me.getBonus(0).getName());
         this.myBonus2.textProperty().bind(me.getBonus(1).getName());
         this.myBonus3.textProperty().bind(me.getBonus(2).getName());
         
         this.chName.textProperty().bind(ch.getName());
         this.chLevel.textProperty().bind(ch.getLevel().asString());
         this.chLifes.textProperty().bind(ch.getLife().asString());
         this.chStrength.textProperty().bind(ch.getStrength().asString());
         this.chSpeed.textProperty().bind(ch.getSpeed().asString());
         this.chBonus1.textProperty().bind(ch.getBonus(0).getName());
         this.chBonus2.textProperty().bind(ch.getBonus(1).getName());
         this.chBonus3.textProperty().bind(ch.getBonus(2).getName());
         
         ScenesContext.getInstance().getSession().netLoadMyCharacter();
         ScenesContext.getInstance().getSession().netLoadChallengerCharacter();
     }    
 }
