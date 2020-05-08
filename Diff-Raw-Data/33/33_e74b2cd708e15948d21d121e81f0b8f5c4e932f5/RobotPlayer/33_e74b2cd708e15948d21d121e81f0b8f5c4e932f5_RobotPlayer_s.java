 package fieldbot;
 
 import battlecode.common.Direction;
 import battlecode.common.GameConstants;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 
public class RobotPlayer {

 
     private static void soldier(RobotController rc)
     {
 	while (true) {
 
 	}
     }
 
 
     private static void encampment(RobotController rc)
     {
 	while (true) {
 
 	}
     }
 
 
     private static void hq(RobotController rc)
     {
 	while (true) {
 
 	}
     }
 
 
     public static void run(RobotController rc)
     {
 	while (true) {
 	    try {
		switch(rc.getType()) {

		case RobotType.HQ:
		    hq(rc);
		    break;

		case RobotType.SOLDIER:
		    soldier(rc);
		    break;

		case RobotType.MEDBAY:
		case RobotType.SHIELDS:
		case RobotType.ARTILLERY:
		case RobotType.GENERATOR:
		case RobotType.SUPPLIER:
		    encampment(rc);
		    break;
 
		default:
		    System.out.println("Uh Oh!");
		    break;
 	    }
 	    catch(Exception e) { e.printStackTrace(); }
 	}
     }
 
 }
