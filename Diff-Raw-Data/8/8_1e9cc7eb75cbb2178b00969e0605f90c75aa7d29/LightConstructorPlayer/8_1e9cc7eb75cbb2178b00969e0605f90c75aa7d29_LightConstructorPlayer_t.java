 package team039.light;
 
 import team039.common.*;
 import team039.common.util.Logger;
 import team039.handler.ComponentsHandler;
 import battlecode.common.*;
 
 public class LightConstructorPlayer extends LightPlayer {
 
     private final RobotController myRC;
     private final Knowledge knowledge;
     private final ComponentsHandler compHandler;
     private MapLocation goal;
     private boolean atGoal = true;
     private int goalSqDist;
 
     public LightConstructorPlayer(RobotController rc,
             Knowledge know,
             ComponentsHandler compHand) {
 
         super(rc, know, compHand);
         myRC = rc;
         knowledge = know;
         compHandler = compHand;
     }
 
     @Override
     public void doSpecificActions() {
         super.doSpecificActions();
 
         switch (knowledge.myState) {
             case EXPLORING:
                 explore();
                 break;
             case BUILDING_RECYCLER:
                 buildRecycler();
                 break;
             case FLEEING:
                 flee();
                 break;
             case BUILDING:
                 compHandler.build().step();
                 break;
             case JUST_BUILT:
                 break;
             case IDLE:
             	knowledge.myState = RobotState.EXPLORING;
                 break;
         }
         
         myRC.setIndicatorString(2, knowledge.myState.toString());
     }
 
     @Override
     public void beginningStateSwitches() {
         if (knowledge.myState == RobotState.JUST_BUILT) {
             Logger.debug_print("I called JUST_BUILT at round " + knowledge.roundNum);
             knowledge.myState = RobotState.IDLE;
         }
 
         if (knowledge.myState == RobotState.IDLE) {
 //        	compHandler.pathFinder.setNavigationAlgorithm(NavigationAlgorithm.BUG);
 //            compHandler.pathFinder.setGoal(myRC.getLocation().add(Direction.SOUTH_EAST, 100));
 //            compHandler.pathFinder.initiateBugNavigation();
 //            compHandler.initiateBugNavigation(myRC.getLocation().add(Direction.SOUTH_EAST, 100));
             knowledge.myState = RobotState.EXPLORING;
         }
     }
 
     @Override
     public void doSpecificFirstRoundActions() {
         super.doSpecificFirstRoundActions();
 //        compHandler.pathFinder.setNavigationAlgorithm(NavigationAlgorithm.BUG);
 //        compHandler.pathFinder.setGoal(myRC.getLocation().add(Direction.SOUTH, 13));
 //        compHandler.pathFinder.initiateBugNavigation();
     }
 
     @Override
     public SpecificPlayer determineSpecificPlayer(ComponentType compType) {
         SpecificPlayer result = this;
         return result;
     }
 
     public void flee()
     {
         compHandler.pathFinder.setGoal( knowledge.startingTurnedOnRecyclerLocation );
     }
 
     public void explore() {
         if (compHandler.canIBuild()) {
             Mine[] sensedMines = compHandler.senseEmptyMines();
             //MapLocation nearestMine = compHandler.senseNearbyMines();
 
             if (compHandler.canSenseEnemies()) {
             }
 
             // TODO: else statement here?
 
             if (sensedMines != null) {
             //if(nearestMine != null) {
                 buildRecyclerLocation = sensedMines[0].getLocation();
 
 //                compHandler.pathFinder.pauseExploration();
                 compHandler.pathFinder.setGoal(buildRecyclerLocation);
                 compHandler.pathFinder.initiateBugNavigation();
                 knowledge.myState = RobotState.BUILDING_RECYCLER;
             } else {
 
 		        try {
 		            compHandler.pathFinder.zigZag();
 		        } catch (Exception e) {
 		            Logger.debug_printExceptionMessage(e);
 		        }
             }
 
         } 
 
     }
     
     MapLocation buildRecyclerLocation;
     public void buildRecycler() {
        if (compHandler.canBuildBuildingHere(buildRecyclerLocation) && 
        		myRC.getTeamResources() > Prefab.commRecycler.getTotalCost() + 1) {
        	
            if(knowledge.myLocation.distanceSquaredTo(buildRecyclerLocation) <= 2 && 
            		!compHandler.canMove(knowledge.myLocation.directionTo(buildRecyclerLocation))) {
            	knowledge.myState = RobotState.IDLE;
            }
             
             compHandler.build().buildChassisAndThenComponents(Prefab.commRecycler, buildRecyclerLocation);
         } else {
             try {
                 compHandler.pathFinder.navigateToAdjacent();
             } catch (Exception e) {
                 Logger.debug_printExceptionMessage(e);
             }
         }
     }
 }
