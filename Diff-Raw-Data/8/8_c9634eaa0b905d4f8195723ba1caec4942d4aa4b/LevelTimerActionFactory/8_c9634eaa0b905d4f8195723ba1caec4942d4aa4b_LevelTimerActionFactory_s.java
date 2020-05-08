 package vooga.towerdefense.factories.actionfactories;
 
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.LevelTimerAction;
 import vooga.towerdefense.factories.ActionAnnotation;
 import vooga.towerdefense.gameelements.GameElement;
 
 public class LevelTimerActionFactory extends ActionFactory {
 	
 	private int myDuration;
<<<<<<< HEAD:src/vooga/towerdefense/factories/actionfactories/TimerActionFactory.java
	public TimerActionFactory(@ActionAnnotation(name = "duration", value = "int") String duration) {
 		super();
=======
	public LevelTimerActionFactory(@ActionAnnotation(name = "duration", value = "int") String duration) {
>>>>>>> 37447bd9e04995f6c87631ba073442a509d1fbb7:src/vooga/towerdefense/factories/actionfactories/LevelTimerActionFactory.java
 		myDuration = Integer.parseInt(duration);
 	}
 
 	@Override
 	protected Action buildAction(GameElement e) {
 		return new LevelTimerAction(myDuration);
 	}
 
 }
