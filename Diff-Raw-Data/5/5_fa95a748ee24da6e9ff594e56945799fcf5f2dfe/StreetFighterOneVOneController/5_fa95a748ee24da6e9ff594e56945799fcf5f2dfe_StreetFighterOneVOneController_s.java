 package games.fighter.davidalan.controller.levels;
 
 import games.fighter.davidalan.util.CollisionDetector;
 
 
 import util.input.*;
 import vooga.fighter.controller.Controller;
 import vooga.fighter.controller.gameinformation.GameInfo;
 import vooga.fighter.controller.interfaces.ControllerDelegate;
 
 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.view.Canvas;
 import vooga.fighter.view.FourPlayerMatchGameLayout;
 import vooga.fighter.controller.levels.OneVOneController;
 
 public class StreetFighterOneVOneController extends OneVOneController{
     
 	private CollisionDetector myDetector; 
 	public StreetFighterOneVOneController(String name, Canvas frame, ControllerDelegate manager, 
     		GameInfo gameinfo, String filepath) {
     	super(name, frame, manager, gameinfo, filepath);
     	myDetector= new CollisionDetector(); 
     }
 
 
     /**
      * Return concrete controller
      */
     @Override
 	public Controller getController(String name, Canvas frame, ControllerDelegate manager, GameInfo gameinfo,
                                     String filepath) {
         Controller controller = new OneVOneController(name, frame, manager, gameinfo, filepath);
         return controller;
     }
 
 
     /**
      * Details movement inputs
      * @param alObj
      */
     @InputMethodTarget(name = "player1_jump")
     public void playerOneJumpInput (AlertObject alObj)  {
     	CharacterObject myChar=getInputObjects().get(0); 
     	for (GameObject object: getMode().getMyObjects()){
     		if (myDetector.hitTop(myChar.getCurrentState().getCurrentRectangle(), object.getCurrentState().getCurrentRectangle())){
     		        getInputObjects().get(0).jump();
     		        }
     	}
     }
 
 
     @InputMethodTarget(name = "player2_jump")
     public void playerTwoJumpInput (AlertObject alObj)  {
     	CharacterObject myChar=getInputObjects().get(1); 
     	for (GameObject object: getMode().getMyObjects()){
     		if (myDetector.hitTop(myChar.getCurrentState().getCurrentRectangle(), object.getCurrentState().getCurrentRectangle())){
     		        getInputObjects().get(1).jump();
     		        }
     	}
     }
 
 
 
     @InputMethodTarget(name = "player1_attack")
     public void playerOneAttackInput(AlertObject alObj) {
         AttackObject newAttack = getInputObjects().get(0).attack("weakPunch");
         getMode().addObject(newAttack);
     }
 
     @InputMethodTarget(name = "player2_attack")
     public void playerTwoAttacknput(AlertObject alObj) {
     	   AttackObject newAttack = getInputObjects().get(1).attack("weakPunch");
            getMode().addObject(newAttack);
     }
 
 
 }
