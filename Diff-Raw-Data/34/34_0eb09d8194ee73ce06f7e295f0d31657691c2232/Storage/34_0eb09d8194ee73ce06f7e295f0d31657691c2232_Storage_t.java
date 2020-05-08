 package testplayer;
 
 import battlecode.common.*;
 
 public class Storage {
 
    // Constants
 
     public static double DISTANCE_BETWEEN;
     public static double EST_RUSH_TIME;
     public static MapLocation MY_HQ;
     public static MapLocation ENEMY_HQ;
     public static MapLocation CENTER; //center between HQs, not true center
     public static double SLOPE;
     public static int MAP_HEIGHT;
     public static int MAP_WIDTH;
     public static int MAP_SIZE;
     public static Team MY_TEAM;
     public static RobotType MY_TYPE;
     public static Team ENEMY_TEAM;
     public static RobotInfo MY_INFO;
     public static Robot ME;
 
     // Variables
 
 
    public Storage(RobotController rc){
         try {
            MY_HQ = rc.senseHQLocation();
             ENEMY_HQ = rc.senseEnemyHQLocation();
             CENTER = new MapLocation((MY_HQ.x + ENEMY_HQ.x)/2,(MY_HQ.y + ENEMY_HQ.y)/2);
             DISTANCE_BETWEEN = Math.sqrt(MY_HQ.distanceSquaredTo(ENEMY_HQ));
             SLOPE = (double)(MY_HQ.y - ENEMY_HQ.y) / (MY_HQ.x - ENEMY_HQ.x);
             ME = rc.getRobot();
             MY_INFO = rc.senseRobotInfo(ME);
             MY_TYPE = rc.getType();
             MAP_HEIGHT = rc.getMapHeight();
             MAP_WIDTH = rc.getMapWidth();
             MAP_SIZE = MAP_HEIGHT * MAP_WIDTH;
             MY_TEAM = rc.getTeam();
             ENEMY_TEAM = MY_TEAM.opponent();
             EST_RUSH_TIME = getRushTime(rc);
         }
         catch(Exception e) { e.printStackTrace(); }
    }

     public static double getRushTime(RobotController rc){
         //calculate estimated turns for rush
         double x_dif = MY_HQ.x - ENEMY_HQ.x;
         double y_dif = MY_HQ.y - ENEMY_HQ.y;
         double x;
         double y;
         double offset;
         double time = 0.0;
         String s = "";
         //rc.setIndicatorString(0, "c=" + CENTER + ", m=" + MY_HQ + ", e=" + ENEMY_HQ + ", xdif=" + x_dif + ", ydif" + y_dif + ", slope=" + SLOPE);
         for (int i=0; i<20; i++) {
             offset = 6 * Math.random() - 3;
             x = Math.random() * x_dif;
             y = SLOPE * x + ENEMY_HQ.y;
             s += " (" + (int)x + ", " + (int)y + ")";
             if (Team.NEUTRAL.equals(rc.senseMine(new MapLocation((int)x, (int)y)))){
                 time += 12;
                 s += "!";
             }
         }
         rc.setIndicatorString(2, s);
         time *= (DISTANCE_BETWEEN/20.0);
         return time;
     }
 
 
     // Combat methods
     // getEnemyCenter
     // getFriendlyCenter
 
}
