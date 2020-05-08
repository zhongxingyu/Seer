 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package team039.common;
 
 import team039.handler.ComponentsHandler;
 import battlecode.common.*;
 
 /**
  *
  * @author sashko
  */
 public class BuildHandler {
 
     private boolean currentlyBuilding;
     private MapLocation buildLocation = null;
     private RobotLevel buildHeight = null;
     private BuildInstructions buildInstructions = null;
     private int buildStep = 0;
     private ComponentsHandler compHandler;
     private RobotController myRC;
     private Knowledge knowledge;
     private RobotState stateToReturnTo = null;
 
     public BuildHandler(RobotController rc, ComponentsHandler ch, Knowledge know) {
         myRC = rc;
         compHandler = ch;
         knowledge = know;
 
         currentlyBuilding = false;
     }
 
     public void step() {
 
         //don't do anything if it's currently waiting for the builder to recharge
         if (compHandler.builderActive()) {
             return;
         }
 
         //skip things I can't build
         while (buildStep != buildInstructions.getNumSteps()
                 && !compHandler.canIBuildThis(buildInstructions.getComponent(buildStep))) {
             buildStep++;
         }
 
 
         if (buildStep < buildInstructions.getNumSteps()) {
 
             if (compHandler.buildComponent(buildInstructions.getComponent(buildStep), buildLocation, buildHeight)) {
                 buildStep++;
             }
 
         } else {
             myRC.setIndicatorString(1, "done building");
             finishBuilding();
         }
     }
 
     /*
      * Finds an empty location to build in, then tries to build there.
      */
     public Boolean autoBuildRobot(BuildInstructions instructions) {
         if (myRC.getTeamResources() < instructions.getTotalCost()) {
             return false;
         }
         if ( !currentlyBuilding ) {
             Chassis chassis = instructions.getBaseChassis();
             MapLocation location = compHandler.getAdjacentEmptySpot(chassis.level);
             if (location == null) {
                 return false;
             } else {
                 buildChassisAndThenComponents(instructions, location);
                 return true;
             }
         } else {
             return false;
         }
     }
 
     /**
      * Builds a chassis in the designated location, then begins building components
      * on it with startBuildingComponents
      */
     public Boolean buildChassisAndThenComponents(BuildInstructions instructions, MapLocation location) {
         Chassis chassis = instructions.getBaseChassis();
         if (compHandler.buildChassis(chassis, location)) {
             startBuildingComponents(instructions, location, chassis.level);
             return true;
         } else {
             return false;
         }
     }
     
     public Boolean buildChassisAndThenComponents(BuildInstructions instructions, MapLocation location, RobotState givenState) {
         Chassis chassis = instructions.getBaseChassis();
         stateToReturnTo = givenState;
         if (compHandler.buildChassis(chassis, location)) {
             startBuildingComponents(instructions, location, chassis.level);
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Initiates component build process by setting all of the required variables
      */
     public void startBuildingComponents(BuildInstructions instructions, MapLocation location, RobotLevel height) {
         //first, find the robot
     	//TODO:  we need to make sure to know how to handle this if the senseARobot returns null.
         currentlyBuilding = true;
         buildStep = 0;
         buildInstructions = instructions;
         buildLocation = location;
         buildHeight = height;
 
         knowledge.myState = RobotState.BUILDING;
     }
 
     public void startBuildingComponents(BuildInstructions instructions, MapLocation location, RobotLevel height, RobotState givenState) {
         //first, find the robot
     	//TODO:  we need to make sure to know how to handle this if the senseARobot returns null.
         currentlyBuilding = true;
         buildStep = 0;
         buildInstructions = instructions;
         buildLocation = location;
         buildHeight = height;
 
         knowledge.myState = RobotState.BUILDING;
 
         stateToReturnTo = RobotState.BUILD_ANTENNA_ON_SELF;
     }
 
     /**
      * Called after a successful build process... anything to activate the just-built robot should go here
      */
     private void finishBuilding() {
 
         //check if you just built a recycler (and are not, yourself, a recycler)
         if( compHandler.canIBuildThis(ComponentType.RECYCLER) && buildInstructions.equals( Prefab.commRecycler ) )
         {
             knowledge.msg().addToQueue( knowledge.myRecyclerNode.generateDesignation() );
         }
 
         currentlyBuilding = false;
         buildStep = 0;
         buildInstructions = null;
         buildLocation = null;
         buildHeight = null;
 
         if(stateToReturnTo == null) {
             knowledge.myState = RobotState.IDLE;
         }
         else {
             knowledge.myState = stateToReturnTo;
             stateToReturnTo = null;
         }
     }
 
     /**
      * Only called if something went wrong - erase all evidence of target
      */
     public void abortBuilding() {
 
         currentlyBuilding = false;
         buildStep = 0;
         buildInstructions = null;
         buildLocation = null;
         buildHeight = null;
 
         if(stateToReturnTo == null) {
             knowledge.myState = RobotState.IDLE;
         }
         else {
             knowledge.myState = stateToReturnTo;
             stateToReturnTo = null;
         }
     }
 }
