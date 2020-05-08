 package team298;
 
 import battlecode.common.*;
 import battlecode.common.TerrainTile.TerrainType;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class ArchonPlayer extends NovaPlayer {
 
     public int[] verticalDeltas = new int[] {-6, 0, -5, 3, -4, 4, -3, 5, -2, 5, -1, 5, 0, 6, 1, 5, 2, 5, 3, 5, 4, 4, 5, 3, 6, 0};
     public int[] horizontalDeltas = new int[] {0, -6, 3, -5, 4, -4, 5, -3, 5, -2, 5, -1, 6, 0, 5, 1, 5, 2, 5, 3, 4, 4, 3, 5, 0, 6};
     public int[] diagonalDeltas = new int[] {5, -3, 5, -2, 5, 0, 6, 0, 5, 1, 5, 2, 5, 3, 4, 3, 4, 4, 3, 4, 3, 5, 2, 5, 1, 5, 0, 5, 0, 6};
     public int archonNumber = 0;
     public int archonGroup = -1;
     public SporadicSpawning spawning;
     public int minMoveTurns = 0, moveTurns = 0;
     public MapLocation towerSpawnFromLocation, towerSpawnLocation;
     public MapLocation destinationLocation;
     public MapLocation[] idealTowerSpawnLocations;
     public ArrayList<MapLocation> enemyLocations;
     public int turnsWaitedForTowerSpawnLocationMessage = 0, turnsSinceLastSpawn = 0, turnsWaitedForMove = 0, turnsSinceMessageForEnemyRobotsSent = 30, turnsSinceTowerStuffDone = 30, turnsLookingForTower = 0;
     boolean attacking;
     boolean attackingInitialized;
     public MapLocation closestEnemy;
     public MapLocation currentEnemy;
     public int closestEnemySeen=Integer.MIN_VALUE, closestEnemyTolerance = 10;
 
     public ArchonPlayer(RobotController controller) {
         super(controller);
         spawning = new SporadicSpawning(this);
         minMoveTurns = RobotType.ARCHON.moveDelayDiagonal() + 5;
         enemyLocations = new ArrayList<MapLocation>();
     }
 
     public void step() {
         // reevaluate goal here?
         //sensing.senseAllTiles();
 
         switch(currentGoal) {
             case Goal.idle:
             case Goal.collectingFlux:
                 navigation.changeToMoveableDirectionGoal(true);
                 spawning.changeModeToAttacking();
                 energon.transferFluxBetweenArchons();
 
                 attacking = sensing.senseEnemyRobotInfoInSensorRange().size() > 1 || closestEnemySeen+closestEnemyTolerance > Clock.getRoundNum();
 
                 // Arbitrary archonNumber hardcoded for now.
                 if (attacking && this.archonNumber == 3) {
                     pa("robot id: " + robot.getID());
                     if (sensing.senseEnemyRobotInfoInSensorRange().size() > 0) {
                         pa("Archon #3 attacking, there are " + sensing.senseEnemyRobotInfoInSensorRange().size() + " enemies in his scope");
                         enemyLocations = sensing.senseEnemyRobotLocations();
                         if (!navigation.changeToFlankingEnemyGoal(enemyLocations, true)) {
                             navigation.flankingEnemyGoal.setAvgLocation(enemyLocations);
                             pa("Archon #3 has updated FlankingEnemyGoal");
                         } else {
                             pa("Archon #3 has successfully set a FlankingEnemyGoal");
                         }
                     }
                 }
 
                 //add a small delay to archon movement so the other dudes can keep up
                 if(attacking) {
                     navigation.moveOnce(false);
                 } else if((moveTurns >= minMoveTurns && controller.getRoundsUntilMovementIdle() == 0)) {
                     int status = navigation.moveOnce(true);
                     //p("Status: "+status);
                     if(status == Status.success) moveTurns = 0;
                 }
 
                 //try to spawn a new dude every turn
                 if(turnsSinceLastSpawn > 2) {
                     int status = spawning.spawnRobot();
                     if(status == Status.cannotSupportUnit) turnsSinceLastSpawn = -1;
                     else if(status == Status.success) {
                         turnsSinceLastSpawn = -1;
                         try {
                             messaging.sendFollowRequest(controller.getLocation(), controller.senseGroundRobotAtLocation(spawning.spawnLocation).getID());
                         } catch(Exception e) {
                             pa("----Exception Caught in sendFollowRequest()");
                         }
                     }
                 }
                 turnsSinceLastSpawn++;
 
                 if(turnsSinceTowerStuffDone < 0) {
                     //try to spawn a tower every turn
                     sensing.senseAlliedTeleporters();
                     if(spawning.canSupportTower(RobotType.TELEPORTER)) {
                         turnsSinceTowerStuffDone = 1;
                         //System.out.println("Can support it");
                         if (attacking) {
                             ArrayList<RobotInfo> robots =  sensing.senseGroundRobotInfo();
                             for (RobotInfo robot : robots){
                                 if (robot.type == RobotType.WOUT){
                                     if (robot.location.isAdjacentTo(controller.getLocation())){
                                         energon.fluxUpWout(robot.location);
                                         sensing.senseAlliedTeleporters();
                                         if (sensing.knownAlliedTowerLocations == null)
                                             sensing.senseAlliedTowers();
                                         if (!sensing.knownAlliedTowerLocations.isEmpty()){
                                             MapLocation loc = navigation.findClosest(new ArrayList<MapLocation>(sensing.knownAlliedTowerLocations.values()));
                                             messaging.sendTowerPing(sensing.knownAlliedTowerIDs.get(loc.getX() +","+loc.getY()), loc);
                                         }
                                     }
                                 }
                             }
                         } else {
                             placeTower();
                         }
                     } else {
                         turnsSinceTowerStuffDone = 5;
                     }
                 }
                 turnsSinceTowerStuffDone--;
 
                 if(turnsSinceMessageForEnemyRobotsSent < 0) {
                     messaging.sendMessageForEnemyRobots();
                     turnsSinceMessageForEnemyRobotsSent = 1;
                 }
                 turnsSinceMessageForEnemyRobotsSent--;
 
                 moveTurns++;
                 break;
             case Goal.askingForTowerLocation:
                 turnsWaitedForTowerSpawnLocationMessage++;
 
                 if(idealTowerSpawnLocations != null) {
                     //we got the message, lets do something                	
                 	if(idealTowerSpawnLocations.length > 0) {
                         towerSpawnLocation = spawning.getTowerSpawnLocation(idealTowerSpawnLocations);                        
                         towerSpawnFromLocation = towerSpawnLocation.subtract(controller.getLocation().directionTo(towerSpawnLocation));
                         navigation.changeToLocationGoal(towerSpawnFromLocation, true);
                         turnsWaitedForTowerSpawnLocationMessage = 0;
                         setGoal(Goal.movingToTowerSpawnLocation);
                     }
                 }
                 if(turnsWaitedForTowerSpawnLocationMessage > 5) {
                 	checkKnownTowerLocations();                    	
                 }
                 break;
             case Goal.movingToPreviousTowerLocation:
                 if(navigation.goal.done()) {
                     //we shouldn't ever get here, but who knows
                     placeTower();
                 } else {
                     if(sensing.senseAlliedTowers().size() > 0) {
                         placeTower();
                     } else {
                         navigation.moveOnce(false);
                     }
                 }
                 break;
             case Goal.placingTeleporter:
                 if(navigation.goal.done()) {
                     navigation.faceLocation(towerSpawnLocation);
                     if(spawning.spawnTower(RobotType.TELEPORTER) != Status.success) {
                         placeTower();
                     } else {
                         setGoal(Goal.collectingFlux);
                     }
                 } else {
                     navigation.moveOnce(false);
                 }
                 break;
             case Goal.attackingEnemyArchons:
                 setGoal(Goal.collectingFlux);
                 break;
             case Goal.movingToTowerSpawnLocation:
                 if(navigation.goal.done()) {
                     navigation.faceLocation(towerSpawnLocation);
                     if (controller.getLocation().directionTo(towerSpawnLocation).isDiagonal())
                     	navigation.moveOnce(false);
                     if(navigation.isLocationFree(towerSpawnLocation, false)) {
                         if(spawning.spawnTower(RobotType.AURA) != Status.success) {
                             placeTower();
                         } else {
                             setGoal(Goal.collectingFlux);
                         }
                     } else {
                         if(turnsWaitedForMove > 5) {
                             placeTower();
                         } else {
                             messaging.sendMove(towerSpawnLocation);
                             turnsWaitedForMove++;
                         }
                     }
 
                 } else {
                     navigation.moveOnce(false);
                 }
                 break;
         }
     }
 
     public void enemyInSight(MapLocation[] locations, int[] ints, String[] strings, int locationStart, int intStart, int stringStart, int count) {
         closestEnemySeen = Clock.getRoundNum();
         closestEnemy = navigation.findClosest(locations, locationStart);
     }
 
     public void towerBuildLocationResponseCallback(MapLocation[] locations) {
         idealTowerSpawnLocations = locations;
     }
 
     public void placeTower() {
         idealTowerSpawnLocations = null;
         turnsWaitedForTowerSpawnLocationMessage = 0;
         towerSpawnFromLocation = null;
         towerSpawnLocation = null;
         turnsWaitedForMove = 0;
 
         ArrayList<MapLocation> towers; ////sensing.senseAlliedTeleporters();
         int towerID = BroadcastMessage.everyone;
         MapLocation location;
         Robot robot;
         
 
         towers = sensing.senseAlliedTowerLocations();
         if(towers.size() > 0) {
             //no teles in range, but there are other towers.  they should be talking to the tele and should know the status of where to build
             try {
                 location = navigation.findClosest(towers);
                 if(controller.canSenseSquare(location) && location != null) {
                     robot = controller.senseGroundRobotAtLocation(location);
                     if(robot != null) 
                     	towerID = robot.getID(); 
                     else {
                     	sensing.knownAlliedTowerLocations.remove(sensing.knownAlliedTowerIDs.remove(location.getX() + "," + location.getY()));
                     }
                 }
                 messaging.sendTowerBuildLocationRequest(towerID);
                 setGoal(Goal.askingForTowerLocation);
                 return;
             } catch(Exception e) {
                 pa("----Caught exception in place tower. "+e.toString());
             }
         }
 
         //no towers in range, lets just ask everyone
         messaging.sendTowerBuildLocationRequest(BroadcastMessage.everyone);
         setGoal(Goal.askingForTowerLocation);
         return;
     }
 
     public void checkKnownTowerLocations() {
     	Robot robot = null;
         
     	ArrayList<MapLocation> towers = sensing.senseKnownAlliedTowerLocations();
         if(towers.size() > 0) {
             //we remember that there used to be a tower here, so lets try going there.  once we get there, we can ask again
             MapLocation closest = navigation.findClosest(towers);
             if(controller.canSenseSquare(closest) && closest != null) {
                 try {
             	robot = controller.senseGroundRobotAtLocation(closest);
                 } catch (Exception e) {;}
                 if (sensing.knownAlliedTowerIDs == null)
                 	sensing.senseAlliedTowers();
                 if(robot == null) {
                 	int id = sensing.knownAlliedTowerIDs.remove(closest.getX() + "," + closest.getY());
                 	sensing.knownAlliedTowerLocations.remove(id);
                 	
                 	checkKnownTowerLocations();
                 	return;
                 }
             }
             navigation.changeToLocationGoal(closest, true);
             setGoal(Goal.movingToPreviousTowerLocation);
             
             return;
         }
 
         spawnTeleporter();
     }
 
     public void spawnTeleporter() {
         //there were no towers in range ever, so lets just build a new one:
         towerSpawnLocation = spawning.getTowerSpawnLocation();
         if(towerSpawnLocation == null) {
             pa("WTF.  There is nowhere to spawn the tower.");
             return;
         }
         towerSpawnFromLocation = towerSpawnLocation.subtract(controller.getLocation().directionTo(towerSpawnLocation));
         navigation.changeToLocationGoal(towerSpawnFromLocation, true);
         controller.setIndicatorString(2, towerSpawnFromLocation.toString());
         setGoal(Goal.placingTeleporter);
     }
     public void towerPingLocationCallback(MapLocation location, int robotID) {
     	sensing.senseAlliedTowerLocations();
 		if (!sensing.knownAlliedTowerLocations.containsKey(robotID)){
 			sensing.knownAlliedTowerLocations.put(new Integer(robotID), location);
 			sensing.knownAlliedTowerIDs.put(location.getX() + "," + location.getY(), robotID);
 		}
     }
     public void newUnit(int senderID, MapLocation location, String robotType) {
     	if (RobotType.valueOf(robotType).isBuilding()){    	
     		if (sensing.knownAlliedTowerLocations == null)
     			sensing.senseAlliedTowerLocations();
     		
     		if (!sensing.knownAlliedTowerLocations.containsKey(senderID)){
     			sensing.knownAlliedTowerLocations.put(new Integer(senderID), location);
     			sensing.knownAlliedTowerIDs.put(location.getX() + "," + location.getY(), senderID);    			
     		}
     	}
     }
     public void boot() {
         sensing.senseAllTiles();
         team = controller.getTeam();
         senseArchonNumber();
 
         //spread out so we dont group up
         MapLocation center = navigation.findAverage(sensing.senseArchonLocations());
         Direction dir = center.directionTo(controller.getLocation());
         navigation.changeToDirectionGoal(dir, false);
         navigation.moveOnce(true);
         navigation.popGoal();
        
         setGoal(Goal.attackingEnemyArchons);
         if(archonNumber == 1) {
        } else {
         }
 
     }
 
     /**
      * Calculates the order in which the archons were spawned.
      */
     public void senseArchonNumber() {
         Message[] messages = controller.getAllMessages();
         int min = 1;
         for(Message m : messages) {
             if (m.ints[0] == 1) {
                 archonLeader = m.ints[1];
             }
             if(m.ints[0] >= min) {
                 min = m.ints[0] + 1;
             }
         }
 
         archonNumber = min;
 
         Message m = new Message();
         m.ints = new int[] {min, robot.getID()};
         try {
             controller.broadcast(m);
         } catch(Exception e) {
             System.out.println("----Caught Exception in senseArchonNumber.  Exception: " + e.toString());
         }
         hasReceivedUniqueMsg = true;
         System.out.println("Number: "+min+" "+archonLeader);
     }
 
     public void senseNewTiles() {
         sensing.senseDeltas(verticalDeltas, horizontalDeltas, diagonalDeltas);
     }
 
     public boolean pathStepTakenCallback() {
         senseNewTiles();
         messaging.sendFollowRequest(controller.getLocation(), BroadcastMessage.everyone);
         return true;
     }
 
     public void lowEnergonMessageCallback(MapLocation location1, int amount, int isAirUnit) {
     }
 }
