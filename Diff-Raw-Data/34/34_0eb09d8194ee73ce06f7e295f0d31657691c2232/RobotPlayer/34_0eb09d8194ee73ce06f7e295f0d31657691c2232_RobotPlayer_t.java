 package testplayer;
 
 import battlecode.common.*;
 
 /**
  *
  */
 public class RobotPlayer
 {
    public static Storage know;
 
     public static void run(RobotController rc)
     {
         preprocessing(rc);
         while (true) {
             try {
 
                 if (know.MY_TYPE == RobotType.SOLDIER) {
                 }
                 else if (know.MY_TYPE == RobotType.HQ) {
                    hq(rc);
                 }
                 else{}
                 rc.yield();
             }
             catch(Exception e) { e.printStackTrace(); }
         }
     }
     public static void soldier(RobotController rc){
 
     }
     public static void hq(RobotController rc){
         while (true) {
             try {
                 if (!rc.isActive())
                { rc.yield(); continue; }
                 Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                 if (rc.canMove(dir))
                     rc.spawn(dir);
                 rc.setIndicatorString(0, ""+Clock.getBytecodeNum());
                 rc.setIndicatorString(1, know.DISTANCE_BETWEEN + ", " + know.EST_RUSH_TIME);
 
                 rc.yield();
             } catch(Exception e) { e.printStackTrace(); }
         }
     }
     public static void preprocessing(RobotController rc){
         know = new Storage(rc);
     }
 
 
}
