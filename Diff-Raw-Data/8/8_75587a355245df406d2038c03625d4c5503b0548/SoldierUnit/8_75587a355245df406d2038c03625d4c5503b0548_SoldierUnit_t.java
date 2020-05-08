 package team063;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 import battlecode.common.Upgrade;
 
 public class SoldierUnit extends BaseUnit {
 	private SoldierState state;
 	
 	private MapLocation targetLoc = myBaseLoc;
 	private int squadId;
 	private int unitId;
 	private MapLocation curLoc;
 	private RobotType encampmentSecureType;
 	private int lastSquadMsg;
 	private int lastUnitMsg;
 	private int lastAllMsg;
 	private int lastAllExceptScoutMsg;
 	
 	// bug crawl algo states
 	private boolean followingWall;
 	private Direction prevDir = null;
 	private MapLocation prevLoc = null;
 	private MapLocation initialWallLoc = null;
 	private Direction wallDir = null; // direction of wall relative to unit
 	
 	public SoldierUnit(RobotController rc) {
 		super(rc);
 		squadId = HQUnit.NO_SQUAD;
 		unitId = HQUnit.NO_UNIT_ID;
 		state = SoldierState.DEFAULT;
 		lastSquadMsg = 0;
 		lastUnitMsg = 0;
 		lastAllMsg = 0;
 		lastAllExceptScoutMsg = 0;
 	}
 	
 	@Override
 	public void run() throws GameActionException {
 		/**
 		 * 1. read broadcasts 2. switch state or squad if necessary 3. act upon
 		 * state
 		 */
 		if (rc.getTeamPower() >= 5 * GameConstants.BROADCAST_READ_COST) {
 			
 			if (unitId == HQUnit.NO_UNIT_ID) {
 				unitId = rc.readBroadcast(Util.getInitialUnitNumChannelNum());
 			}
 			
 
 			if (unitId != HQUnit.NO_UNIT_ID) {
 				if (Util.getUnitChannelNum(unitId) < 0 || Util.getUnitChannelNum(unitId) > 65535) {
 					System.out.println("unitId: " + unitId);
 					System.out.println("Util.getUnitChannelNum(unitId): " + Util.getUnitChannelNum(unitId));
 				}
 
 				int unitMsg = rc.readBroadcast(Util.getUnitChannelNum(unitId));
 
 				if (unitMsg != lastUnitMsg && unitMsg != 0) {
 					rc.setIndicatorString(0, "unitmsg in binary: " + Integer.toBinaryString(unitMsg)
 									+ " changeSquadBool: "
 									+ Util.getChangeSquadBool(unitMsg));
 					if (Util.getChangeSquadBool(unitMsg)) {
 						rc.setIndicatorString(1,"new squad: " + Util.getSquadAssignment(unitMsg));
 						squadId = Util.getSquadAssignment(unitMsg);
 					} else {
 						targetLoc = Util.getMapLocationFromMsg(unitMsg);
 						state = Util.getSoldierStateFromMsg(unitMsg);
 						encampmentSecureType = Util
 								.getEncampmentTypeFromMsg(unitMsg);
 					}
 				}
 			}
 			if (squadId != HQUnit.NO_SQUAD) {
 				int squadMsg = rc.readBroadcast(Util
 						.getSquadChannelNum(squadId));
 
 				if (squadMsg != lastSquadMsg && squadMsg != 0
 						&& Util.decode(squadMsg) != null) {
 					targetLoc = Util.getMapLocationFromMsg(squadMsg);
 					state = Util.getSoldierStateFromMsg(squadMsg);
 					encampmentSecureType = Util
 							.getEncampmentTypeFromMsg(squadMsg);
 					lastSquadMsg = squadMsg;
 				}
 			}	
 			// read message sent to all squads except scout
 			if (squadId != HQUnit.SCOUT_SQUAD) {
 				int curMsg = rc.readBroadcast(Util.getAllUnitExceptScoutChannelNum());
 				if (curMsg != lastAllExceptScoutMsg && curMsg != 0 && Util.decode(curMsg) != null) {
 					targetLoc = Util.getMapLocationFromMsg(curMsg);
 					state = Util.getSoldierStateFromMsg(curMsg);
 					encampmentSecureType = Util.getEncampmentTypeFromMsg(curMsg);
 					lastAllExceptScoutMsg = curMsg;
 				}
 			}
 			// read message sent to everyone
 			int msg = rc.readBroadcast(Util.getAllUnitChannelNum());
 			if (msg != 0 && Util.decode(msg) != null) {
 				rc.setIndicatorString(0, "round: " + Clock.getRoundNum() + "all unit channel: " + Util.getAllUnitChannelNum() + " msg: " + msg);
 				targetLoc = Util.getMapLocationFromMsg(msg);
 				state = Util.getSoldierStateFromMsg(msg);
 				encampmentSecureType = Util.getEncampmentTypeFromMsg(msg);
 //				lastAllMsg = msg;
 			}
 
 		}
 		else {
 			state = SoldierState.DEFAULT;
 		}
 
 		this.curLoc = rc.getLocation();
 		rc.setIndicatorString(2, "cur state: " + state + " cur target: " + targetLoc + " squadId: " + squadId + " unitId: " + unitId);
 
 		switch (state) {
 		case RUSH_ENEMY_HQ:
 			// if next enemyBase, don't move
 			// if next to enemy, don't move
 			int[] offsets = {0, 1, -1, 2, -2};
 			boolean nextToBase = false;
 			Direction dir = curLoc.directionTo(enemyBaseLoc);
 			for (int d: offsets) {
 				dir = Direction.values()[(dir.ordinal() + d + 8) % 8];
 				if (curLoc.add(dir).equals(enemyBaseLoc)) {
 					nextToBase = true;
 					break;
 				}
 			}
 			if (!nextToBase) {
 				this.goToLocationBrute(targetLoc);
 			}
 			break;
 		case BRUTE_MOVE:
 
 			this.goToLocationBrute(this.targetLoc);
 			break;
 		case SMART_MOVE:
 			break;
 		case ATTACK_MOVE:
 			/*robot will move to a location in loose formation attacking what it runs into and avoiding mines.
 			 * it should defuse mines if there are no enemies around
 			 */
 			
 			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 16, otherTeam);
 			Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,25,myTeam);
 			Robot[] farAllies;
 			double lowHealth=200;
 			int lowHealthIndex=0;
 			int squadSize;
 			
 			if (this.distToEnemyBaseSquared<=800){
 				squadSize=4;
 			} else {
 				squadSize=6;
 			}
 			
 			if (mapHeight <=30 && mapWidth<=30){
 				farAllies = rc.senseNearbyGameObjects(Robot.class,20,myTeam);
 			} else if (!(mapHeight >=60) && !(mapWidth>=60)){
 				farAllies = rc.senseNearbyGameObjects(Robot.class,36,myTeam);
 			} else {
 				farAllies = rc.senseNearbyGameObjects(Robot.class,49,myTeam);
 			}
 			curLoc=rc.getLocation();
 			MapLocation[] farMines= {};
 			if (rc.hasUpgrade(Upgrade.DEFUSION)){
 				farMines= rc.senseNonAlliedMineLocations(curLoc, 14);
 			}
 			if (rc.isActive()) {
 				if (nearbyEnemies.length < 1 && farMines.length >0){
 					rc.setIndicatorString(0,"defusing mine");
 					rc.defuseMine(farMines[0]);
 				} else if (nearbyAllies.length >= squadSize && nearbyEnemies.length <= 2){
 					rc.setIndicatorString(0, "attacking!");
 					this.goToLocationBrute(targetLoc);
 //					this.goToLocationSmart(targetLoc);
 				
 				} else if (nearbyAllies.length>=squadSize){
 					if (rc.getEnergon()<=20){
 						MapLocation stepAwayLoc=rc.senseRobotInfo(nearbyAllies[0]).location.subtract(curLoc.directionTo(targetLoc));
 						rc.setIndicatorString(0,"I am weak! stepping back to: ("+stepAwayLoc.x+","+stepAwayLoc.y+")");
 						this.goToLocationBrute(stepAwayLoc);
 					} else {
 						int[] dirOffsets={0, 1, -1, 2, -2, 3, -3, 4};
 						int minEnemies=10;
 						MapLocation lookingAtCurrently=curLoc;
 						MapLocation curTarget;
 						int adjacentEnemies;
 						Direction lookingDir;
 						for (int offset:dirOffsets){
 							lookingDir=Direction.values()[(8+offset)%8];
 							if (rc.canMove(lookingDir)) {
 								lookingAtCurrently=curLoc.add(lookingDir);
 								adjacentEnemies = rc.senseNearbyGameObjects(
 										Robot.class, lookingAtCurrently, 2,
 										otherTeam).length;
 								if (adjacentEnemies < minEnemies) {
 									minEnemies = adjacentEnemies;
 									curTarget = lookingAtCurrently;
 								}
 							}
 						}
 						rc.move(curLoc.directionTo(lookingAtCurrently));						
 						
 //						for (int index = 0; index < nearbyEnemies.length - 3; index += 3) {
 //							if (rc.senseRobotInfo(nearbyEnemies[index]).energon < lowHealth) {
 //								lowHealth = rc
 //										.senseRobotInfo(nearbyEnemies[index]).energon;
 //								lowHealthIndex = index;
 //							}
 //						}
 //						MapLocation weakEnemyLoc = rc
 //								.senseRobotInfo(nearbyEnemies[lowHealthIndex]).location;
 //						rc.setIndicatorString(0,
 //								"attacking weak enemy robot at: ("
 //										+ weakEnemyLoc.x + "," + weakEnemyLoc.y
 //										+ ")");
 //						if (rc.isActive()) {
 //							this.goToLocationCareful(weakEnemyLoc);
 						
 					}
 
 				} else if (farAllies.length >= 7){
 					rc.setIndicatorString(0, "regrouping to " + rc.senseRobotInfo(farAllies[0]).location);
 
 					
 					this.goToLocationBrute(rc.senseRobotInfo(farAllies[0]).location);
 //					this.goToLocationSmart(rc.senseRobotInfo(farAllies[0]).location);
 
 				} else {
 					rc.setIndicatorString(0,"no one nearby! retreating home!");
 					this.goToLocationBrute(myBaseLoc);
 //					this.goToLocationSmart(myBaseLoc);
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
 			
 			Robot[] nearbyEnemies_scouting = rc.senseNearbyGameObjects(
 					Robot.class, 16, otherTeam);
 			if (nearbyEnemies_scouting.length >= 2) {
 				if ((rc.senseNearbyGameObjects(Robot.class, 49, otherTeam)).length >= 8) {
 					if (curLoc.distanceSquaredTo(this.enemyBaseLoc) <= 81) {
 						// broadcast high enemy presence near their HQ
 					}
 				} else {
 					// broadcast high enemy presence near this robot's current
 					// location
 				}
 				if (rc.isActive()) {
 					this.goToLocationCareful(curLoc.subtract(curLoc.directionTo(rc
 						.senseRobotInfo(nearbyEnemies_scouting[0]).location)));
 				}
 			} else {
 				if (rc.isActive()) {
 //					this.goToLocationCareful(targetLoc);
 //					this.goToLocationBugCrawl(targetLoc);
 					this.goToLocationBrute(targetLoc);
 				}
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
 						if (curLoc.distanceSquaredTo(targetLoc) > 9) {
 							rc.setIndicatorString(1, "encampment currently being captured, move towards it");
 							this.goToLocationBrute(targetLoc);
 //							this.goToLocationBugCrawl(targetLoc);
 						}
 						else {
 							rc.setIndicatorString(1, "encampment currently being captured, defend it");
 							this.defendPosition(targetLoc);
 						}
 
 					}
 					else if (ec.getTeam().equals(myTeam)) {
 						if (curLoc.distanceSquaredTo(targetLoc) > 9) {
 							rc.setIndicatorString(1, "encampment captured, move towards it");
 							this.goToLocationBrute(targetLoc);
 //							this.goToLocationBugCrawl(targetLoc);
 
 						}
 						else {
 							rc.setIndicatorString(1, "encampment captured, defend it");
 							this.defendPosition(targetLoc);
 						}
 					} else {
 						// uh oh
 						rc.setIndicatorString(1, "near enemy encampment");
 						this.goToLocationBrute(targetLoc);
 
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
 //			this.defendPosition(myBaseLoc);
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
 		if (rc.isActive()) {
 			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 20,
 					otherTeam);
 			if (nearbyEnemies.length >= 1) {
 				if (rc.senseNearbyGameObjects(Robot.class, 4, myTeam).length < 2) {
 					rc.setIndicatorString(0, "not enough neraby allies to fight!");
 					this.goToLocationBrute(defendPoint);
 				} else if (curLoc.distanceSquaredTo(defendPoint) <= 49) {
					if (rc.getEnergon()>=10){
 						rc.setIndicatorString(0, "attacking nearby enemy!");
 						this.goToLocationBrute(rc.senseRobotInfo(nearbyEnemies[0]).location);
					} else if (rc.senseNearbyGameObjects(Robot.class, targetLoc, 5, otherTeam).length<1){
 						MapLocation stepAwayLoc=curLoc.subtract(curLoc.directionTo(rc.senseRobotInfo(nearbyEnemies[0]).location));
 						rc.setIndicatorString(0,"I am weak! stepping back to: ("+stepAwayLoc.x+","+stepAwayLoc.y+")");
 						this.goToLocationBrute(stepAwayLoc);
					} else {
						this.goToLocationBrute(targetLoc);
 					}
 					
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
 						&& (curLoc.x * 2 + curLoc.y) % 5 == 1 && rc.hasUpgrade(Upgrade.PICKAXE)) {
 					// standing on patterned empty sq
 					rc.setIndicatorString(0, "laying mine");
 					rc.layMine();
 					rc.yield();
 				} else if (rc.senseMine(curLoc) == null
 						&& (curLoc.x + curLoc.y) % 2 == 1){
 					rc.setIndicatorString(0,"laying mine");
 					rc.layMine();
 					rc.yield();
 				} else if (curLoc.distanceSquaredTo(defendPoint) <= 20) {
 					// standing on own mine and within defense radius
 					rc.setIndicatorString(0, "moving to defensive formation");
 					MapLocation topLeft=new MapLocation(defendPoint.x-2, defendPoint.y-2);
 					MapLocation topRight=new MapLocation(defendPoint.x+2,defendPoint.y-2);
 					MapLocation bottomLeft=new MapLocation(defendPoint.x-2,defendPoint.y+2);
 					MapLocation bottomRight = new MapLocation(defendPoint.x+2,defendPoint.y+2);
 					MapLocation[] locationArray={topLeft,topRight,bottomLeft,bottomRight, new MapLocation(defendPoint.x,defendPoint.y+1), new MapLocation(defendPoint.x,defendPoint.y-1), new MapLocation(defendPoint.x-1,defendPoint.y), new MapLocation(defendPoint.x+1,defendPoint.y)};
 					Direction randomDir = Direction.values()[(int) (Math.random() * 8)];
 					
 					for (int index=0;index<=3;index++){
 						if (rc.canSenseSquare(locationArray[index]) && rc.senseObjectAtLocation(locationArray[index])==null && rc.senseMine(locationArray[index])==null){
 							this.goToLocationBrute(locationArray[index]);
 						} else if (curLoc == locationArray[index]){
 							rc.yield();
 						} else {
 							if (rc.canMove(randomDir) && rc.isActive()){
 								rc.move(randomDir);
 							}
 						}
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
 
 	// pick direction closest to line between curloc and dest
 	public void goToLocationBugCrawl(MapLocation dest) throws GameActionException {
 		int dist = curLoc.distanceSquaredTo(dest);
 		Direction dir = curLoc.directionTo(dest);
 		
 		if (followingWall) {
 			// following wall state
 			Direction initialDir = initialWallLoc.directionTo(dest);
 			if (initialDir.equals(dir) && dist < initialWallLoc.distanceSquaredTo(dest)) {
 				followingWall = false;
 			}
 			else {
 				int[] turnDirOffsets = {-1, 1}; // offsets from wallDir, 
 												//only one or none should be movable in this case, always try to turn towards goal
 				boolean canTurn = false;
 				Direction curDir = wallDir;
 				int offset = 0;
 				for (int d: turnDirOffsets) {
 					curDir= Direction.values()[(wallDir.ordinal() + d + 8) % 8];
 					MapLocation candidateLoc = curLoc.add(curDir);
 					Team potentialMineLoc = rc.senseMine(candidateLoc);
 					if (!candidateLoc.equals(prevLoc) && rc.canMove(curDir) && (potentialMineLoc == null || potentialMineLoc.equals(myTeam))) {
 						canTurn = true;
 						offset = d;
 						break;
 					}
 				}
 				Direction moveDir = curDir;
 				if (canTurn) {
 					rc.setIndicatorString(0, "followingWall, wallDir: " + wallDir + " turning to: " + curDir);
 					rc.move(moveDir);
 					prevLoc = curLoc;
 					if (offset == -1) {
 						wallDir = wallDir.rotateRight();
 						wallDir = wallDir.rotateRight();
 						prevDir = prevDir.rotateRight();
 						prevDir = prevDir.rotateRight();
 					}
 					else {
 						wallDir = wallDir.rotateLeft();
 						wallDir = wallDir.rotateLeft();
 						prevDir = prevDir.rotateLeft();
 						prevDir = prevDir.rotateLeft();
 					}
 				}
 				else {
 					MapLocation candidateLoc = curLoc.add(prevDir);
 					Team potentialMineLoc = rc.senseMine(candidateLoc);
 					if (!candidateLoc.equals(prevLoc) && rc.canMove(curDir) && (potentialMineLoc == null || potentialMineLoc.equals(myTeam))) {
 						rc.setIndicatorString(0, "followingWall, wallDir: " + wallDir + " move dir: " + prevDir);
 						rc.move(prevDir);
 						prevLoc = curLoc;
 					}
 					else {
 						followingWall = false;
 						rc.setIndicatorString(0, "deadend, defusing mine: " + curLoc.add(dir));
 						rc.defuseMine(curLoc.add(dir));
 					}
 
 				}
 			}
 		}
 		
 		if (!followingWall) {
 			// moving towards dest state
 			int[] directionOffsets = { 0, 1, -1, 2, -2 };
 //			curLoc = rc.getLocation();
 
 			if (dist > 0 && rc.isActive()) {
 //				Direction dir = curLoc.directionTo(dest);
 				rc.setIndicatorString(1, "dir to dest: " + dir);
 				Direction lookingAtCurrently = dir;
 				Direction bestDir = dir;
 				int bestDist = 10000000; // should use max_int
 				MapLocation candidateLoc = curLoc.add(lookingAtCurrently);
 				boolean canMoveForward = false;
 				for (int d : directionOffsets) {
 					lookingAtCurrently = Direction.values()[(dir.ordinal() + d + 8) % 8];
 					candidateLoc = curLoc.add(lookingAtCurrently);
 					Team potentialMineLoc = rc.senseMine(candidateLoc);
 					if (rc.canMove(lookingAtCurrently)
 							&& (potentialMineLoc == null || potentialMineLoc
 									.equals(myTeam))) {
 						int curDist = candidateLoc.distanceSquaredTo(dest);
 						if (curDist < bestDist) {
 							canMoveForward = true;
 							bestDist = curDist;
 							bestDir = lookingAtCurrently;
 						}
 					}
 				}
 				if (canMoveForward) {
 					if (!curLoc.add(bestDir).equals(prevLoc)) {
 						rc.setIndicatorString(0, "can move forward: " + bestDir);
 						rc.move(bestDir);
 						prevLoc = curLoc;
 					}
 					else {
 						rc.setIndicatorString(0, "defusing mine b/c going to prevLoc: " + dir);
 						rc.defuseMine(curLoc.add(dir));
 					}
 				} else {
 					// decide to move sideways or defuse mine ahead
 					// rotate +2
 					Direction dir1 = Direction.values()[(dir.ordinal() + 3 + 8) % 8];
 					// rotate -2
 					Direction dir2 = Direction.values()[(dir.ordinal() - 3 + 8) % 8];
 
 					// count mines
 					MapLocation[] localMines = rc.senseNonAlliedMineLocations(
 							curLoc, 36);
 
 					int[][] mineMap = new int[70][70];
 					for (int i = 0; i < localMines.length; i++) {
 						mineMap[localMines[i].x][localMines[i].y] = 1;
 					}
 
 					int mapUpperLeftX = Math.max(curLoc.x - 6, 0);
 					int mapUpperLeftY = Math.max(curLoc.y - 6, 0);
 
 					int mapLowerRightX = Math.min(curLoc.x + 6, mapWidth);
 					int mapLowerRightY = Math.min(curLoc.y + 6, mapHeight);
 
 					MapLocation loc1 = curLoc.add(dir1);
 					MapLocation loc2 = curLoc.add(dir2);
 
 					// heuristics for deciding which way to go
 					// look at freeSpaces in a dir and mineNumAlongWall in a dir
 
 					int freeSpacesDir1 = 0;
 					int freeSpacesDir2 = 0;
 					for (int i = 0; i < 6; i++) {
 						if (loc1.x > 0 && loc1.x < mapWidth && loc1.y > 0
 								&& loc1.y < mapHeight) {
 							if (mineMap[loc1.x][loc1.y] == 1) {
 								break;
 							} else {
 								freeSpacesDir1++;
 							}
 						}
 						loc1 = loc1.add(dir1);
 					}
 
 					for (int i = 0; i < 6; i++) {
 						if (loc2.x > 0 && loc2.x < mapWidth && loc2.y > 0
 								&& loc2.y < mapHeight) {
 							if (mineMap[loc2.x][loc2.y] == 1) {
 								break;
 							} else {
 								freeSpacesDir2++;
 							}
 						}
 						loc2 = loc2.add(dir2);
 					}
 
 					int minesAlongWallDir1 = 0;
 					int minesAlongWallDir2 = 0;
 					Direction oppo = dir2.opposite();
 					loc1 = curLoc.add(oppo); // reused
 
 					for (int i = 0; i < 6; i++) {
 						if (loc1.x > 0 && loc1.x < mapWidth && loc1.y > 0
 								&& loc1.y < mapHeight) {
 							if (mineMap[loc1.x][loc1.y] == 0) {
 								break;
 							} else {
 								minesAlongWallDir1++;
 							}
 						}
 						loc1 = loc1.add(dir1);
 					}
 					oppo = dir1.opposite();
 					loc2 = curLoc.add(oppo);
 
 					for (int i = 0; i < 6; i++) {
 						if (loc2.x > 0 && loc2.x < mapWidth && loc2.y > 0
 								&& loc2.y < mapHeight) {
 							if (mineMap[loc2.x][loc2.y] == 0) {
 								break;
 							} else {
 								minesAlongWallDir2++;
 							}
 						}
 						loc2 = loc2.add(dir2);
 					}
 					if (minesAlongWallDir1 >= 6 && minesAlongWallDir2 >= 6) {
 						rc.setIndicatorString(0, "best option to defuse mine "
 								+ curLoc.add(dir) + " mineNum1: "
 								+ minesAlongWallDir1 + " mineNum2: "
 								+ minesAlongWallDir2);
 						rc.defuseMine(curLoc.add(dir));
 					} else {
 						if (minesAlongWallDir1 > minesAlongWallDir2) {
 							if (freeSpacesDir2 >= minesAlongWallDir2) {
 								rc.setIndicatorString(0, "wall moving dir2: "
 										+ dir2 + " mineNum1: "
 										+ minesAlongWallDir1 + " mineNum2: "
 										+ minesAlongWallDir2);
 								if (rc.canMove(dir2)) {
 									rc.move(dir2);
 									prevDir = dir2;
 									prevLoc = curLoc;
 									initialWallLoc = curLoc;
 									followingWall = true;
 									wallDir = dir1.opposite();
 								}
 							} else {
 								rc.setIndicatorString(0,
 										"DEFUSING MINE, mineNum2: "
 												+ minesAlongWallDir2
 												+ " freespacesDir2: "
 												+ freeSpacesDir2);
 								rc.defuseMine(curLoc.add(dir));
 							}
 						} else {
 							if (freeSpacesDir1 >= minesAlongWallDir1) {
 								rc.setIndicatorString(0, "moving dir1: " + dir1
 										+ " mineNum1: " + minesAlongWallDir1
 										+ " mineNum2: " + minesAlongWallDir2);
 								rc.move(dir1);
 								prevDir = dir1;
 								prevLoc = curLoc;
 								initialWallLoc = curLoc;
 								followingWall = true;
 								wallDir = dir2.opposite();
 							} else {
 								rc.setIndicatorString(0,
 										"DEFUSING MINE, mineNum1: "
 												+ minesAlongWallDir1
 												+ " freespacesDir1: "
 												+ freeSpacesDir1);
 								rc.defuseMine(curLoc.add(dir));
 
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void runTest() throws GameActionException {
 		// TODO Auto-generated method stub
 		
 	}
 		
 	
 }
