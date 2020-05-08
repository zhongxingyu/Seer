 package fieldbot;
 
 import battlecode.common.Direction;
 import battlecode.common.GameConstants;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 
/**
 *
 */
public class RobotPlayer
{
 
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
		RobotType type = rc.getType();
 
		if (type == RobotType.SOLDIER) soldier(rc);
		else if (type == RobotType.HQ) hq(rc);
		else encampment(rc);
 	    }
 	    catch(Exception e) { e.printStackTrace(); }
 	}
     }
 
 }
