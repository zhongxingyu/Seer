 package vooga.towerdefense.action;
 
 
 /**
  * A simple Action that disables itself after a certain amount of time.
  * @author JLongley
  *
  */
 public class LevelTimerAction extends Action {
 
 	private int myTimer;
 	
 	public LevelTimerAction(int duration) {
 		super();
		myTimer = duration;
 	}
 	
 	public int getRemainingTime() {
 		return myTimer;
 	}
 	
 	@Override
 	public void executeAction(double elapsedTime) {
 		myTimer-=elapsedTime;
 		if(myTimer<=0)
 			setEnabled(false);
 	}
 }
