 package in.mum.robocode;
 
 import robocode.Robot;
 import robocode.ScannedRobotEvent;
 
 /**
  * 
  * 
  * @author Santosh Iyer
  */
 public class MyFirstBot extends Robot {
 	
 	final static String ROBOT_NAME="GORT";
 	
 	@Override
 	public String getName() {
 		return ROBOT_NAME;
 	}
 	
 	@Override
 	public void run() {
 		ahead(getHeading() %90);
 		turnRight(90);
 		while(true){
 			ahead(getHeading() %90);
 			turnRight(90);
 		}
 	}
 
 	@Override
 	public void onScannedRobot(ScannedRobotEvent event) {
 		fire(1);
 	}
	
	@Override
	public void doNothing() {
		super.doNothing();
	}
	
 }
