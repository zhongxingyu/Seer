 package team039.building.recycler;
 
 import team039.common.*;
 import team039.common.util.*;
 import team039.handler.ComponentsHandler;
 import battlecode.common.*;
 
 public class StartingBuildingPlayer extends RecyclerPlayer {
 
     private final RobotController myRC;
     private final Knowledge knowledge;
     private final ComponentsHandler compHandler;
 
     public StartingBuildingPlayer(RobotController rc,
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
 
     }
 
     @Override
     public void doSpecificFirstRoundActions() {
         
 
         MapLocation startingLight = compHandler.getStartingLightConstructorLocation();
         if(startingLight != null)
         {
             compHandler.build().startBuildingComponents( Prefab.startingConstructor , startingLight, RobotLevel.ON_GROUND, RobotState.BUILD_ANTENNA_ON_SELF);
         }
 
         super.doSpecificFirstRoundActions();
     }
 
     @Override
     public void initialize()
     {
         
         knowledge.myRecyclerNode = new RecyclerNode();
         knowledge.myRecyclerNode.myLocation = myRC.getLocation();
         knowledge.myRecyclerNode.myRobotID = myRC.getRobot().getID();
         knowledge.myRecyclerNode.parentLocation = null;
         knowledge.myRecyclerNode.parentRobotID = 0;
     }
 
     @Override
     public SpecificPlayer determineSpecificPlayer(ComponentType compType) {
         SpecificPlayer result = this;
 
         switch(compType) {
         case ANTENNA:
         case DISH:
         case NETWORK:
             result = new RecyclerCommPlayer(myRC, knowledge, compHandler);
             break;
         }
         return result;
     }
 
     @Override
     public void beginningStateSwitches() {
         super.beginningStateSwitches();
 
 
     }
 }
