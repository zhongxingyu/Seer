 package team063;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 
 public class SoldierUnit extends BaseUnit {
 	private SoldierState state;
 	private MapLocation targetLoc = myBaseLoc;
 	private int squadId;
 	private MapLocation curLoc;
<<<<<<< HEAD
	
=======
	private int starting;
	private int finish;
 
>>>>>>> b947414cc15f78b9baa55fd1bb413ebd026f4427
 	public SoldierUnit(RobotController rc) {
 		super(rc);
 		state = SoldierState.SECURE_ENCAMPMENT;
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		/**
 		 * 1. read broadcast 2. switch state or squad if necessary 3. act upon
 		 * state
 		 */
 		rc.setIndicatorString(0, "id: " + rc.getRobot().getID());
 		rc.broadcast(0, id);
 		// readbroadcast(channelNum)
 		if (rc.isActive()) {
 			int unitMsg = rc.readBroadcast(getUnitChannelNum(id));
 			int squadMsg = rc.readBroadcast(getSquadChannelNum(squadId));
 			int allUnitMsg = rc.readBroadcast(getAllUnitChannelNum());
 		}
 		this.curLoc = rc.getLocation();
 
 		switch (state) {
 
 		case BRUTE_MOVE:
 			this.goToLocationBrute(this.enemyBaseLoc);
 			break;
 		case SMART_MOVE:
 			break;
 		case ATTACK_MOVE:
 			break;
 		case PATROL:
 			break;
 		case SCOUT:
 			/*robot will move towards a location
 			 * it will avoid enemies it encounters and send messages based on what it sees
 			 * 
 			 * basic implementation for sprint: seeing high numbers of enemy units near their HQ with mines assumes they are nuke rushing
 			 * seeing low numbers of enemies near their HQ -AND- high resistance at encampments assumes they are spread out
 			 * seeing low numbers of enemies near their HQ -AND- few encounters with enemies elsewhere assumes rush
 			 */
 			
 			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 16, otherTeam);
 			if (nearbyEnemies.length>=2){
 				if ((rc.senseNearbyGameObjects(Robot.class,49,otherTeam)).length>=8){
 					if (curLoc.distanceSquaredTo(this.enemyBaseLoc)<=81){
 							//broadcast high enemy presence near their HQ
 						}
 					} else {
 						//broadcast high enemy presence near this robot's current location
 					}
 				this.goToLocationBrute(curLoc.subtract(curLoc.directionTo(rc.senseRobotInfo(nearbyEnemies[0]).location)));
 				rc.yield();
 				} else {
 				this.goToLocationBrute(targetLoc);
 				rc.yield();
 			}
 			break;
 		case CAPTURE_MOVE:
 			break;
 		case DEFEND_POSITION:
 			if (rc.isActive()) {
 				defendPosition(targetLoc);
 			}
 			break;
 		case BATTLE:
 			break;
 		case CHASE_AND_DESTROY:
 			break;
 		case SEEK_AND_DESTROY_GUERILLA:
 			break;
 		case SECURE_ENCAMPMENT: // 21 - 900 bytecode
 			// assumes robot is close to encampment(targetLoc), go towards
 			// encampment
 			// capture encampment
 			// defend it
 
 			/**
 			 * if targetLoc has been captured: defend by laying mines around
 			 * encampment and attacking enemies that come by else: if standing
 			 * on encampment: capture it else: if mine in the way: defuse it
 			 * else: go towards targetLoc
 			 */
 
 			// MapLocation[] encampments = rc.senseAllEncampmentSquares();
 			MapLocation[] encampments = rc.senseEncampmentSquares(
 					new MapLocation(mapWidth / 2, mapHeight / 2), 10000,
 					Team.NEUTRAL);
 			MapLocation nearestEncamp = encampments[0];
 
 			int bestDistSquared = rc.getLocation().distanceSquaredTo(
 					encampments[0]);
 			for (int i = 1; i < encampments.length; i++) {
 				int curDist = rc.getLocation()
 						.distanceSquaredTo(encampments[i]);
 				if (curDist < bestDistSquared) {
 					nearestEncamp = encampments[i];
 					bestDistSquared = curDist;
 				}
 			}
 			targetLoc = nearestEncamp;
 
 			if (rc.isActive()) {
 				if (rc.getLocation().equals(targetLoc)) {
 					if (rc.senseCaptureCost() < rc.getTeamPower()) {
 						rc.setIndicatorString(1, "capturing encampment");
 
 						rc.captureEncampment(RobotType.SUPPLIER);
 					} else {
 						rc.setIndicatorString(1,
 								"not enough power, waiting till next turn to capture");
 						rc.yield();
 					}
 				} else if (rc.canSenseSquare(targetLoc)) {
 					GameObject ec = rc.senseObjectAtLocation(targetLoc);
 
 					if (ec == null) {
 						rc.setIndicatorString(1,
 								"near neutral encampment, moving towards it");
 						this.goToLocationBrute(targetLoc);
 					} else if (ec.getTeam().equals(myTeam)) {
 						rc.setIndicatorString(1,
 								"encampment captured, defend it");
 						this.defendPosition(targetLoc);
 					} else {
 						// uh oh
 						rc.setIndicatorString(1, "near enemy encampment");
 					}
 				} else {
 					rc.setIndicatorString(1,
 							"target out of range, move towards it");
 					this.goToLocationBrute(targetLoc);
 				}
 			} else {
 				rc.setIndicatorString(0, "currently not active");
 				// do some computation and broadcast
 			}
 
 			break;
 		default:
 			// do nothing if no instructions from hq
 			break;
 		}
 	}
 
 	/**
 	 * modifies squadId modifies state modifies targetLoc
 	 */
 	@Override
 	public void decodeMsg(int encodedMsg) {
 		// TODO Auto-generated method stub
 
 	}
 
 	protected void defendPosition(MapLocation defendPoint)
 			throws GameActionException { // 50 - 800 bytecode
 		Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 25,
 				otherTeam);
 		if (nearbyEnemies.length >= 1) {
 			if (rc.senseNearbyGameObjects(Robot.class, 4, myTeam).length < 2) {
 				rc.setIndicatorString(0, "not enough neraby allies to fight!");
 				this.goToLocationBrute(defendPoint);
 			} else if (curLoc.distanceSquaredTo(defendPoint) <= 49) {
 
 				rc.setIndicatorString(0, "attacking nearby enemy!");
 				this.goToLocationBrute(rc.senseRobotInfo(nearbyEnemies[0]).location);
 			} else {
 				rc.setIndicatorString(0, "enemy is too far away to chase!");
 				this.goToLocationBrute(defendPoint);
 			}
 		} else {
 			MapLocation nearbyMine = this.senseAdjacentMine();
 			if (nearbyMine != null) {
 				// if nearby neutral or enemy mine is found
 				rc.setIndicatorString(0, "mine detected at " + nearbyMine.x
 						+ " " + nearbyMine.y);
 				rc.defuseMine(nearbyMine);
 				rc.yield();
 			} else if (rc.senseMine(curLoc) == null
 					&& (curLoc.x * 2 + curLoc.y) % 5 == 1) {
 				// standing on patterned empty sq
 				rc.setIndicatorString(0, "laying mine");
 				rc.layMine();
 				rc.yield();
 			} else if (curLoc.distanceSquaredTo(defendPoint) <= 25) {
 				// standing on own mine and within defense radius
 				rc.setIndicatorString(0, "moving randomly");
 				Direction randomDir = Direction.values()[(int) (Math.random() * 8)];
 				if (rc.canMove(randomDir)) {
 					rc.move(randomDir);
 				}
 				rc.yield();
 			} else {
 				// outside defense radius, so move towards defend point
 				rc.setIndicatorString(0, "returning to defend point");
 				this.goToLocationBrute(defendPoint);
 			}
 		}
 	}
 
 }
