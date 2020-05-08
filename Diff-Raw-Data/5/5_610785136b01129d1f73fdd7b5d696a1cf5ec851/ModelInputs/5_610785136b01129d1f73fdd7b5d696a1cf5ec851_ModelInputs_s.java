 
 package vooga.scroller.model;
 
<<<<<<< HEAD
 import util.input.AlertObject;
 import util.input.Input;
 import util.input.InputClassTarget;
 import util.input.InputMethodTarget;
 import util.input.PositionObject;
=======
import util.input.*;
>>>>>>> input
 import javax.swing.JComponent;
 import vooga.scroller.sprites.superclasses.Player;
 /**
  * Class that holds all user defined control methods. These methods can work
  * on the player used in the construciton of this.
  * 
  * @author Scott Valentine
  * 
  */
 @InputClassTarget
 public class ModelInputs {
 
     private static final String TEST_CONTROLS = "vooga/scroller/resources/controls/TestMapping";
 
     private Input myInput;
     private Player myPlayer;
 
     /**
      * Creates a new set of ModelInputs based on
      * 
      * @param player on which the controls will act
      * @param view from where the controls come from.
      */
     public ModelInputs (Player player, JComponent view) {
         myInput = new Input(TEST_CONTROLS, view);
         myPlayer = player;
         myInput.addListenerTo(this);
     }
 
     // Note, these methods can be redefined to customize games.
     // TODO: add more @InputMethodTarget methods
     
     
     /**
      * Player moves up
      * 
      * @param alObj
      */
     @InputMethodTarget(name = "jump")
     public void jumpInput (AlertObject alObj) {
         myPlayer.translate(Player.UP_VELOCITY);
     }
 
     /**
      * Player moves down
      * 
      * @param alObj
      */
     @InputMethodTarget(name = "left")
     public void leftInput (AlertObject alObj) {
         myPlayer.translate(Player.LEFT_VELOCITY);
     }
 
     /**
      * Player moves right
      * 
      * @param alObj
      */
     @InputMethodTarget(name = "right")
     public void rightInput (AlertObject alObj) {
         myPlayer.translate(Player.RIGHT_VELOCITY);
     }
 
     /**
      * Player moves left
      * @param alObj
      */
     @InputMethodTarget(name = "down")
     public void downInput (AlertObject alObj) {
         myPlayer.translate(Player.DOWN_VELOCITY);
     }
     
     @InputMethodTarget(name="test")
     public void movementCoordTest(PositionObject posObj) {
         myPlayer.setCenter(posObj.getX(), posObj.getY());
     }
 
 }
