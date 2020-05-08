 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.image.ImageView;
 import javafx.scene.text.Text;
 
 /**
  * FXML Controller class
  *
  * @author Karl
  */
 public class FightController implements Initializable {
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
     private void handleMenuFightWin(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuFightLoose(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuFightRandom(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuFightRegular(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuCharacterNew(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuCharacterUpdate(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
     private void handleMenuCharacterDelete(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
     }
     @FXML
    private void handleMenuFightRegular(ActionEvent e){
        Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
    }
    @FXML
     private void handleMenuDisconnect(ActionEvent e){
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Handle");
         ScenesContext.getInstance().setSession(null);
         ScenesContext.getInstance().showLogin();
     }
     
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         Logger.getLogger(FightController.class.getName()).log(Level.INFO, "Session token : " + ScenesContext.getInstance().getSession().getToken());
     }    
 }
