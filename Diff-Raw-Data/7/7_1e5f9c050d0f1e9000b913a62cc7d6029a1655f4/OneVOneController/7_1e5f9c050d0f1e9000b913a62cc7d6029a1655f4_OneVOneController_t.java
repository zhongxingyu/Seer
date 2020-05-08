 package vooga.fighter.controller;
 
 
 
 import java.util.List;
 import util.input.*;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.MouseClickObject;
 import vooga.fighter.view.Canvas;
 import vooga.fighter.view.FourPlayerMatchGameLayout;
 
 
 /**
  * 
  * @author Jerry Li
  * 
  * @author by Jack Matteucci
  * 
  */
 @InputClassTarget
 public class OneVOneController extends LevelController {
     private static final String INPUT_PATHWAY = "vooga.fighter.config.leveldefault";
 
     public OneVOneController () {
         super();
     }   
 	
     public OneVOneController(String name, Canvas frame, ControllerDelegate manager, 
     		GameInfo gameinfo) {
     	super(name, frame, manager, gameinfo);
     	frame.setLayout(new FourPlayerMatchGameLayout());
     }
     
     public Controller getController(String name, Canvas frame, ControllerDelegate manager, GameInfo gameinfo) {
         Controller controller = new OneVOneController(name, frame, manager, gameinfo);
         return controller;
     }
 
     public void notifyEndCondition (String endCondition) {
     	removeListener();
     	getManager().notifyEndCondition("ScoreScreen");
     }
     
     @InputMethodTarget(name = "player1_jump")
     public void playerOneJumpInput (AlertObject alObj)  {
         getInputObjects().get(0).move(270);
     }
     
     @InputMethodTarget(name = "player1_left")
     public void playerOneLeftInput (AlertObject alObj) {
         getInputObjects().get(0).move(180);
         
     }
     
     @InputMethodTarget(name = "player1_right")
     public void playerOneRightInput(AlertObject alObj) {
         getInputObjects().get(0).move(0);
         
     }
     
     @InputMethodTarget(name = "player1_down")
     public void playerOneDownInput(AlertObject alObj) {
         getInputObjects().get(0).move(90);
         
     }
     
     @InputMethodTarget(name = "player2_jump")
     public void playerTwoJumpInput (AlertObject alObj)  {
         getInputObjects().get(1).move(270);
     }
     
     @InputMethodTarget(name = "player2_left")
     public void playerTwoLeftInput (AlertObject alObj) {
         getInputObjects().get(1).move(180);
        
     }
     
     @InputMethodTarget(name = "player2_right")
     public void playerTwoRightInput(AlertObject alObj) {
         getInputObjects().get(1).move(0);
         
     }
     
     @InputMethodTarget(name = "player2_down")
     public void playerTwoDownInput(AlertObject alObj) {
         getInputObjects().get(1).move(90);
         
     }
     
     @InputMethodTarget(name = "player1_attack")
     public void playerOneAttackInput(AlertObject alObj) {
         getInputObjects().get(0).attack("weakPunch");
     }
     
     @InputMethodTarget(name = "player2_attack")
     public void playerTwoAttacknput(AlertObject alObj) {
     	getInputObjects().get(1).attack("weakPunch");
     }
     
     @InputMethodTarget(name = "continue")
     public void mouseclick(PositionObject pos)  {
     	//This is a test
     	getInputObjects().get(1).changeHealth(-10);
     }
     
     public void removeListener(){
     	super.removeListener();
     	getInput().removeListener(this);
     }
     
     
 }
