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
 import battlecode.common.Upgrade;
 
 public class SoldierUnit extends BaseUnit {
 	private SoldierState state;
 	
 	//hardcoded test targetLoc
 	private MapLocation targetLoc = myBaseLoc;
 	private int squadId;
 	private MapLocation curLoc;
 	private RobotType encampmentSecureType;
 
 
 	public SoldierUnit(RobotController rc) {
 		super(rc);
 		
 		//hardcoded test state
 		state = SoldierState.DEFAULT;
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		/**
 		 * 1. read broadcast 2. switch state or squad if necessary 3. act upon
 		 * state
 		 */
 		if (rc.getTeamPower() > .1) {
 		// readbroadcast(channelNum)
 //			int unitMsg = rc.readBroadcast(getUnitChannelNum(id));
 //			int squadMsg = rc.readBroadcast(getSquadChannelNum(squadId));
 //			int allUnitMsg = rc.readBroadcast(getAllUnitChannelNum());
 			int msg = rc.readBroadcast(this.getAllUnitChannelNum());
 			
 			targetLoc = this.getMapLocationFromMsg(msg);
 			state = this.getSoldierStateFromMsg(msg);
 			encampmentSecureType = this.getEncampmentTypeFromMsg(msg);	
 		}
 		else {
 			state = SoldierState.DEFAULT;
 		}
 
 		this.curLoc = rc.getLocation();
		rc.setIndicatorString(2, "cur state: " + state + "cur target: " + curLoc);
 
 		//hardcoded test strategy
 //		if (Clock.getRoundNum() > 130){
 //			targetLoc = enemyBaseLoc;
 //			state=SoldierState.ATTACK_MOVE;
 //		}
 
 		switch (state) {
 
 		case BRUTE_MOVE:
			this.goToLocationBrute(this.enemyBaseLoc);
 			break;
 		case SMART_MOVE:
 			break;
 		case ATTACK_MOVE:
 			/*robot will move to a location in loose formation attacking what it runs into and avoiding mines.
 			 * it should defuse mines if there are no enemies around
 			 */
 			
 			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 16, otherTeam);
 			Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,9,myTeam);
 			Robot[] farAllies = rc.senseNearbyGameObjects(Robot.class,36,myTeam);
 			curLoc=rc.getLocation();
 			MapLocation[] farMines= {};
 			if (rc.hasUpgrade(Upgrade.DEFUSION)){
 				farMines= rc.senseNonAlliedMineLocations(curLoc, 14);
 			}
 			if (rc.isActive()) {
 				if (nearbyEnemies.length < 1 && farMines.length >0){
 					rc.setIndicatorString(0,"defusing mine");
 					rc.defuseMine(farMines[0]);
 				} else if (nearbyAllies.length >= 3){
 					rc.setIndicatorString(0, "attacking!");
 					this.goToLocationBrute(targetLoc);
 				} else if (farAllies.length >= 1){
 					rc.setIndicatorString(0, "regrouping");
 					this.goToLocationBrute(rc.senseRobotInfo(farAllies[0]).location);
 				} else {
 					rc.setIndicatorString(0,"no one nearby! retreating home!");
 					this.goToLocationBrute(myBaseLoc);
 				}
 			}
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
 			
 			Robot[] nearbyEnemies_scouting = rc.senseNearbyGameObjects(Robot.class, 16, otherTeam);
 			if (nearbyEnemies_scouting.length>=2){
 				if ((rc.senseNearbyGameObjects(Robot.class,49,otherTeam)).length>=8){
 					if (curLoc.distanceSquaredTo(this.enemyBaseLoc)<=81){
 							//broadcast high enemy presence near their HQ
 						}
 					} else {
 						//broadcast high enemy presence near this robot's current location
 					}
 				this.goToLocationBrute(curLoc.subtract(curLoc.directionTo(rc.senseRobotInfo(nearbyEnemies_scouting[0]).location)));
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
 			
 			if (rc.isActive()) {
 				if (rc.getLocation().equals(targetLoc)) {
 					if (rc.senseCaptureCost() < rc.getTeamPower()) {
 						rc.setIndicatorString(1, "capturing encampment");
 
 						rc.captureEncampment(encampmentSecureType);
 					} else {
 						rc.setIndicatorString(1,
 								"not enough power, waiting till next turn to capture");
 						rc.yield();
 					}
 				} else if (rc.canSenseSquare(targetLoc)) {
 					GameObject ec = rc.senseObjectAtLocation(targetLoc);
 					Robot[] objs = rc.senseNearbyGameObjects(Robot.class, targetLoc, 1, myTeam);
 					if (ec == null) {
 						rc.setIndicatorString(1,
 								"near neutral encampment, moving towards it");
 						this.goToLocationBrute(targetLoc);
 					}
 					else if (ec.getTeam().equals(myTeam) && (rc.senseRobotInfo(objs[0]).type.equals(RobotType.SOLDIER))) {
 						rc.setIndicatorString(1, "encampment currently being captured, move towards it");
 						this.goToLocationBrute(targetLoc);
 					}
 					else if (ec.getTeam().equals(myTeam)) {
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
 		case DEFAULT:
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
