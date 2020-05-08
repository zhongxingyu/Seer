 package current;
 
 import battlecode.common.*;
 
 public class RobotPlayer {
 	public static void run(RobotController myRC)
 	{
 		BaseRobot br = null;
 		try {
             switch (myRC.getType()) {
             case HQ:
                 br = new HQRobot(myRC);
                 break;
             case SOLDIER:
                 br = new SoldierRobot(myRC);
                 break;
             case ARTILLERY:
             	br = new Artillery(myRC);
             	break;
             default:
                 br = new PassiveEncampment(myRC);
             }
 			
 			while(true)
			{			
 				br.run();
 
 				br.rc.yield();
 			}
 
 		} catch (Exception e) {
 			//DEBUG
             System.out.println("Shit happened!");
             e.printStackTrace();
             br.rc.addMatchObservation(e.toString());
             br.rc.breakpoint();
 		}
 	}
 }
