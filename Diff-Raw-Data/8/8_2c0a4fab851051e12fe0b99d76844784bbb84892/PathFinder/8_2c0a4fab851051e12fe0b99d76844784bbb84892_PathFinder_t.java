 package team039.common;
 
 import team039.handler.ComponentsHandler;
 import battlecode.common.*;
 import team039.common.util.*;
 
 public class PathFinder {
 
     private final RobotController myRC;
     private final ComponentsHandler myCH;
     private final Knowledge knowledge;
     
     private        NavigationAlgorithm  navAlg = NavigationAlgorithm.NONE;
     private        boolean              navigating = false;
     private        boolean              goingToAdjacent = false;
     private        MapLocation          goal;
     
     /*** Exploring info ***/
     private        int                  exploreRound;
     private        int                  savedExploreRound;
     private        MapLocation          savedExploreGoal;
     private        Direction            exploreDirection;
     private        boolean              exploringPaused = false;
     
      /*** Bug Navigation info ***/
     private        boolean              tracking;
     private        boolean              trackingCW;
     private        boolean              startTracking;
     private        boolean              stopTracking;
     private        Direction            trackingDirection;
     private        Direction            prevTrackingDirection;
     private        Direction            prevDirectionToGoal;
     private        int                  turningNumber;
     private        MapLocation          bugStart;
     private        MapLocation []       bugPrevLocations;  
     private        Direction []         bugPrevDirections; 
     private        int                  bugStep;
     private        enum                 BugState { ROTATE, MOVE_FORWARD, MOVE_BACKWARD, NONE };
 
     
     public PathFinder(RobotController rc, ComponentsHandler comp, Knowledge know) {
         myRC = rc;
         myCH = comp;
         knowledge = know;
         
         //set a pseudo-random direction to start exploring
         exploreDirection = Direction.values()[knowledge.myRobotID % 8];
         exploreRound = knowledge.roundNum - 1;
         goal = knowledge.myLocation.add(exploreDirection, QuantumConstants.EXLPORE_GOAL_DISTANCE);
     }
     
     public void explore () {
         if(exploringPaused) {
             exploreRound = knowledge.roundNum - savedExploreRound;
             exploringPaused = false;
             goal = savedExploreGoal;
             initiateBugNavigation();
         }
         
         if(reachedGoal()) {
             //keep exploring in the same direction
             goal = knowledge.myLocation.add(exploreDirection, QuantumConstants.EXLPORE_GOAL_DISTANCE);
             initiateBugNavigation();
             exploreRound = knowledge.roundNum;
         } else if((knowledge.roundNum - exploreRound) % QuantumConstants.EXPLORE_TIME == 0) {
             // find a new exploration goal
             exploreDirection = exploreDirection.rotateRight().rotateRight().rotateRight();
             goal = knowledge.myLocation.add(exploreDirection, QuantumConstants.EXLPORE_GOAL_DISTANCE);
             
             initiateBugNavigation();
         } 
         
         try {
             step();
         } catch(Exception e) {
             Logger.debug_printExceptionMessage(e);
         }
         myRC.setIndicatorString(2, "exploreRound: " + exploreRound + " round Number: " + knowledge.roundNum);
         
     }
     
     public void pauseExploration() {
         exploringPaused = true;
         savedExploreRound = (knowledge.roundNum - exploreRound) % QuantumConstants.EXPLORE_TIME;
         savedExploreGoal = goal;
     }
     
     public void zigZag() { 
         if(myCH.motorActive())
             return;
         
         Direction testRDir1 = knowledge.myDirection;
         Direction testRDir2 = knowledge.myDirection.rotateRight();
         Direction testLDir1 = knowledge.myDirection;
         Direction testLDir2 = knowledge.myDirection.rotateLeft();
         
         try {
             if(myCH.canMove(knowledge.myDirection)) {
                 myCH.moveForward();
             } else {
                 //if path is blocked, find a direction to bounce in
                 Direction moveDir = Direction.NONE;
                 while(moveDir == Direction.NONE) {
                     testRDir1 = testRDir2;
                     testRDir2 = testRDir2.rotateRight();
                     moveDir = moveZigZagDir(testRDir1, testRDir2);
                     if(moveDir == Direction.NONE) {
                         testLDir1 = testLDir2;
                         testLDir2 = testLDir2.rotateLeft();
                         moveDir = moveZigZagDir(testLDir1, testLDir2);
                     }
                     
                     //if surrounded, do nothing.
                     if(testRDir1 == testLDir1)
                         break;
                 }
                 myCH.setDirection(moveDir);
             }
             
         } catch(Exception e) {
             Logger.debug_printExceptionMessage(e);
         }
     }
     
     private Direction moveZigZagDir(Direction dir1, Direction dir2) {
         if(myCH.canMove(dir1)) {
             if(dir2 == knowledge.myDirection.opposite())
                 return dir1;
             else if(myCH.canMove(dir2)) 
                 return dir2;
             else 
                 return dir1;
         } else {
             return Direction.NONE;
         }
     }
     
     public void setNavigationAlgorithm(NavigationAlgorithm alg) {
         navAlg = alg;
     }
     
     public void setGoal(MapLocation g) {
         goal = g;
     }
     
     public boolean isNavigating() {
         return navigating;
     }
     
     public void step() {
         //do nothing if the motor is active or you are not navigating
         if(!navigating)
             return;
         if(myCH.motorActive())
             return;
 
         try {
             switch(navAlg) {
                 case BUG:
                     navigateBug();
                     break;
                 case ZIG_ZAG:
                     zigZag();
                     break;
             }
         } catch(Exception e) {
             Logger.debug_printExceptionMessage(e);
         }
     }
     
     public boolean reachedGoal() {        
         return knowledge.myLocation.equals(goal);
     }
     
     public void initiateBugNavigation() {
         initiateBugNavigation(goal);
     }
     
     public void initiateBugNavigation(MapLocation newGoal) {
 //        bugNavigating = true;
         goal = newGoal;
         navAlg = NavigationAlgorithm.BUG;
         navigating = true;
         tracking = false;
         startTracking = false;
         stopTracking = false;
 //        bugGoal = goal;
         bugStart = knowledge.myLocation;
         prevDirectionToGoal = bugStart.directionTo(goal);
         bugPrevLocations = new MapLocation [QuantumConstants.BUG_MEMORY_LENGTH];
         bugPrevDirections = new Direction [QuantumConstants.BUG_MEMORY_LENGTH];
         bugStep = 0;
 //        bugState = BugState.ROTATE;
     }
     
     public void navigateBug()  {    

         MapLocation location = knowledge.myLocation;
         Direction directionToGoal = location.directionTo(goal);
         int bugPos = bugStep % QuantumConstants.BUG_MEMORY_LENGTH;
                 
         //stop navigating once you have reached your goal
         if(reachedGoal() && !goingToAdjacent) {
             navigating = false;
             return;
         }
 
         if(myCH.motorActive())
             return;
 
         BugState action = determineBugState();
         
         try {
             switch(action) {
                 case ROTATE:
                     if(tracking)
                         myCH.setDirection(trackingDirection);
                     else
                         myCH.setDirection(directionToGoal);
                     break;
                 
                 case MOVE_FORWARD:
                     myCH.moveForward();
                     prevDirectionToGoal = directionToGoal;
                     prevTrackingDirection = trackingDirection;
 //                    
 //                    if(stopTracking){
 //                        tracking = false;
 //                        stopTracking = false;
 //                    }
                     break;
                     
                 case MOVE_BACKWARD:
                     myCH.moveBackward();
                     prevDirectionToGoal = directionToGoal;
                     prevTrackingDirection = trackingDirection;
 //                    
 //                    if(stopTracking){
 //                        tracking = false;
 //                        stopTracking = false;
 //                    }
                     break; 
                     
                 case NONE:
                     break;
             }
         } catch(Exception e) {
             Logger.debug_printExceptionMessage(e);
         }
     }
     
     private BugState determineBugState() {
         
         MapLocation location = knowledge.myLocation;
         Direction directionToGoal = location.directionTo(goal);
 //        Direction myDirection = knowledge.myDirection;
         int bugPos = bugStep % QuantumConstants.BUG_MEMORY_LENGTH;
         BugState action;
         
         if(tracking) {
 
             if(directionToGoal != prevDirectionToGoal) {
                 //changing goal contributes negatively to the turning number.
                 turningNumber -= calculateTurningChange(prevDirectionToGoal, directionToGoal, trackingCW);
 //                prevDirectionToGoal = directionToGoal;
             }
             //set the initial direction to begin testing from.
             Direction startDirection = prevTrackingDirection.opposite();
             Direction testDirection = startDirection;
             
             //check directions beginning with the reference direction, in an order depending on 
             //if you are tracking clockwise or counterclockwise
             if(startTracking) {
                 startTracking = false;
             } else {
                 turningNumber -= 4;
             }
             boolean pathBlocked = true;
             while(pathBlocked) {
                 //increment direction
                 if(trackingCW) {
                     testDirection = testDirection.rotateLeft();
                 } else {
                     testDirection = testDirection.rotateRight();
                 }
                 
                 //update turning number
                 turningNumber++;
                 
                 if(myCH.canMove(testDirection)){
                     trackingDirection = testDirection;
                     pathBlocked = false;
                 } else if(testDirection == startDirection) {
                     turningNumber -= 4;
                     break;
                 }
             }
 
             //check if you are finished tracking
             if(turningNumber <= 0) {
                 tracking = false;
             }
             
             action = getBugAction(trackingDirection);
 //            myRC.setIndicatorString(2, "CW? " + trackingCW + "turning num: " + turningNumber);
             
         } else {
             //not tracking, so check if you can move towards the goal
             if(myCH.canMove(directionToGoal)) {
                 action = getBugAction(directionToGoal);
             } else {
                 //the path is blocked, so begin tracking.
                 tracking = true;
                 startTracking = true;
                 bugPos = (bugPos + 1) % QuantumConstants.BUG_MEMORY_LENGTH;
                 bugPrevLocations[bugPos] = location;
 //                trackingRefDirection = directionToGoal;
             
                 //determine if you should track around the obstacle clockwise or counterclockwise
                 Direction ccwDir = directionToGoal;
                 Direction cwDir = directionToGoal;
                 boolean searching = true;
                 while(searching) {
                     ccwDir = ccwDir.rotateRight();
                     cwDir = cwDir.rotateLeft();
                     if(myCH.canMove(ccwDir)) {
                         bugPrevDirections[bugPos] = ccwDir;
                         trackingCW = false;
                         searching = false;
                         if(myCH.canMove(cwDir) && location.add(cwDir).distanceSquaredTo(goal) 
                                 < location.add(ccwDir).distanceSquaredTo(goal)) {
                             bugPrevDirections[bugPos] = cwDir;
                             trackingCW = true;
                         }
                     } else if(myCH.canMove(cwDir)) {
                         bugPrevDirections[bugPos] = cwDir;
                         trackingCW = true;
                         searching = false;
                     }
                         
                     //stop searching if you are surrounded
                     if(ccwDir == cwDir) {
                         bugPrevDirections[bugPos] = ccwDir;
                         break;
                     }
                 }
                 
                 trackingDirection = bugPrevDirections[bugPos];
                 prevTrackingDirection = directionToGoal.opposite();
                 turningNumber = calculateTurningChange(directionToGoal, trackingDirection, trackingCW);
 //                prevDirectionToGoal = directionToGoal;
                 action = getBugAction(trackingDirection);
 //                myRC.setIndicatorString(2, "turning = " + turningNumber);
             }
         }
         
         return action;
 
     }
     
     private BugState getBugAction(Direction dir) {
         BugState action;
         if(knowledge.myDirection == dir)
             action = BugState.MOVE_FORWARD;
 //        else if(knowledge.myDirection == dir.opposite())
 //            action = BugState.MOVE_BACKWARD;
         else
             action = BugState.ROTATE;
         
         return action;
     }
     
     private int calculateTurningChange(Direction oldDir, Direction newDir, boolean CW) {
         int turn = -4;
         Direction testDir = oldDir.opposite();
         while(testDir != newDir) {
             turn++;
             if(CW) {
                 testDir = testDir.rotateLeft();
             } else {
                 testDir = testDir.rotateRight();
             }
         }
         return turn;
     }
 
     public void navigateToAdjacent() {
         //must initiate bug navigation before calling.
         if(!navigating)
             return;
         
         goingToAdjacent = true;
         
         MapLocation location = knowledge.myLocation;
         int distanceSquaredToGoal = location.distanceSquaredTo(goal);
 //        myRC.setIndicatorString(2,String.valueOf(distanceSquaredToGoal));
         
         if(myCH.motorActive()) {
             return;
         }
         
         if(distanceSquaredToGoal == 0) {
             Direction moveDir = knowledge.myDirection;
             boolean searching = true;
             myRC.setIndicatorString(2, "we are at goal");
 
             while(searching){
                 if(myCH.canMove(moveDir)) {
                     searching = false;
                 } else {
                     moveDir = moveDir.rotateRight();
                 }
                 
                 if(searching && moveDir == knowledge.myDirection) {
                     moveDir = Direction.NONE;
                     searching = false;
                 }
             }
             myRC.setIndicatorString(1, "finished loop");
             try {
                 if(moveDir == knowledge.myDirection)
                     myCH.moveForward();
                 else if(moveDir == knowledge.myDirection.opposite())
                     myCH.moveBackward();
                 else if(moveDir != Direction.NONE)
                     myCH.setDirection(moveDir);
             } catch(Exception e) {
                 Logger.debug_printExceptionMessage(e);
             }
             
         } else if(distanceSquaredToGoal <= 2) {
             navigating = false;
             myCH.setDirection(location.directionTo(goal));
             return;
         }
     
         try {
             navigateBug();
         } catch (Exception e) {
             Logger.debug_printExceptionMessage(e);
         }
         goingToAdjacent = false;
 
     }
 }
 
