 package simplebot.modules;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 /**
  * Navigtion encapsulates navigating to a destination on the map using simple
  * pathfinding.
  */
 public class Navigation {
     private RobotController robot;
     private MovementController motor;
 
     private int bnav_lastDist;
     private MapLocation bnav_lastDest;
 
     /**
      * Instantiates a Navigation object
      *
      * @param robot the robot this navigator controls, used to get current location
      * @parm motor the controlle for the motor of this robot
      */
     public Navigation(RobotController robot, MovementController motor) {
         this.robot = robot;
         this.motor = motor;
     }
 
     /**
      * Navigates the given motor towards the last given map location using a processor-friendly
      * bug navigation.  Start by moving towards the destination.  When a wall is sensed,
      * begin a wall following algorithm around the obstacle.  Wall following stops when two
      * things happen:
      *  - The robot is facing towards the goal
      *  - The robot is located closer to the goal than when it started
      *
      * If both conditions are met, the robot breaks out of the wall following code and simply
      * continues on towards the destination.  bugNavigate keeps track of 'bnav_lastDist', the distance
      * from the goal since the wall following started (0 if no wall following is occuring), and
      * 'bnav_lastDest', the last destination that navigate was called with.
      *
      * @param motor the motor object for the robot
      * @param sensor the sensor used to view the map
      */
     public void bugNavigate() {
         // If the motor is cooling down, don't bother navigating
         // Also return if no destination is set
         // Some precomputation might be useful eventually
         if(motor.isActive() || bnav_lastDest == null)
             return;
 
         MapLocation loc = robot.getLocation();
        // Likewise, if the robot is already at its destination,
        // signal finish
         if(loc.equals(bnav_lastDest)) {
             bnav_lastDest = null;
             bnav_lastDist = 0;
             return;
         }
 
         Direction d = loc.directionTo(bnav_lastDest);
         Direction cur = robot.getDirection();
 
             try {
         if(bnav_lastDist == 0) {
             // Try navigating towards the goal
             if(d == cur) {
                 if(robot.senseTerrainTile(loc.add(d)) == TerrainTile.LAND) {
                     if(motor.canMove(d))
                         motor.moveForward();
                 } else {
                     // Hit a wall, begin wall following
                     bnav_lastDist = loc.distanceSquaredTo(bnav_lastDest);
                     motor.setDirection(d.rotateRight().rotateRight());
                 }
             } else {
                 motor.setDirection(d);
             }
         } else {
             // Sample, do a right-hand follow, escaping when robot faces target
             // scan left to right for open directions:
             Direction scan = cur.rotateLeft();
             Direction test = scan.rotateLeft();
 
             while(scan != test) {
                 if(robot.senseTerrainTile(loc.add(scan)) == TerrainTile.LAND)
                     break;
                 scan = scan.rotateRight();
             }
 
             if(robot.senseTerrainTile(loc.add(d)) == TerrainTile.LAND && loc.distanceSquaredTo(bnav_lastDest) < bnav_lastDist) {
                 bnav_lastDist = 0;
                 scan = d;
             }
 
             // If the open square is forward, move forward, otherwise turn
             if(scan == cur) {
                 // Check square leading to destination, if that is free, take it and
                 // stop wall following
 
                 if(motor.canMove(cur)) {
                     motor.moveForward();
                 } else {
                     // TODO: Something is blocking our way
                 }
             } else {
                 motor.setDirection(scan);
             }
         }
             } catch(Exception e) {
                 // Do nothing at the moment
             }
     }
 
     /**
      * Navigates towards the given map location, restarting the bug navigation.
      *
      * @param motor the motor object for the robot
      * @param loc the destination location, in absolute coordinates
      *
      * @see simplebot.RobotPlayer#navigate(MovementController)
      */
     public void bugNavigate(MapLocation loc) {
         // restart if this is a new destination
         if(bnav_lastDest == null || !loc.equals(bnav_lastDest)) {
             bnav_lastDist = 0;
             bnav_lastDest = loc;
         }
 
         // start navigation
         bugNavigate();
     }
 }
