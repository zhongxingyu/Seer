 package team063;
 
 import java.util.Comparator;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Upgrade;
 
 //singleton
 public class HQUnit extends BaseUnit {
 	public int curSoldiers = 0;
 	protected int[] unitsMap;
 	protected MapLocation[] initialTargetEncampments;
 	private int encampCounter;
 
 	public HQUnit(RobotController rc) {
 		super(rc);
 		this.unitsMap = new int[2000];
 		this.encampCounter = 0;
 		int start = Clock.getBytecodeNum();
 		this.initialTargetEncampments = getTargetEncampments();
 		System.out.println("getTargetEncampments bytecode usage: "
 				+ (Clock.getBytecodeNum() - start));
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		if (mapHeight > 65 && mapWidth > 65) {
 			// big map, nuke strategy
 			int msg = this.encodeMsg(
 					myBaseLoc,
 					SoldierState.DEFEND_POSITION, RobotType.HQ, 0);
 			rc.broadcast(this.getAllUnitChannelNum(), msg);
 			if (rc.isActive()) {
 				if (Clock.getRoundNum() < 200) {
 					// spawn robots
 					this.spawnInAvailable();
 				} else {
 					rc.researchUpgrade(Upgrade.NUKE);
 
 					rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(
 							myBaseLoc,
 							SoldierState.DEFEND_POSITION, RobotType.HQ, 0));
 				}
 			}
 		}
 		else if (mapHeight <= 30 && mapWidth <= 30) {
 			// small map, rush strategy
 			// build a shield encamp, rally there until 130, then rush
			if (Clock.getRoundNum() < 50) {
 				rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(initialTargetEncampments[0], SoldierState.SECURE_ENCAMPMENT, RobotType.SHIELDS, 0));
 			}
 			else {
 				rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE, RobotType.HQ, 0));
 			}
 			if (rc.isActive()) {
 				this.spawnInAvailable();
 			}
 		}
 		else {
 			if (this.rc.isActive()) {
 //				if (Clock.getRoundNum() > 70
 //						&& !rc.hasUpgrade(Upgrade.DEFUSION)) {
 //					rc.setIndicatorString(0, "researching DEFUSION");
 //					rc.researchUpgrade(Upgrade.DEFUSION);
 //				} else 
 					
 				if (Clock.getRoundNum() > 70 && !rc.hasUpgrade(Upgrade.FUSION)) {
 					
 					rc.setIndicatorString(0, "researching FUSION");
 					rc.researchUpgrade(Upgrade.FUSION);
 					
 				} else if (Clock.getRoundNum() <= 200) {
 					rc.setIndicatorString(1, "rally at shields");
 					rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(initialTargetEncampments[2], SoldierState.BRUTE_MOVE, RobotType.SHIELDS, 0));
 					this.spawnInAvailable();
 				}
 				
 				else if (Clock.getRoundNum() > 200 && Clock.getRoundNum() < 1000) {
 					
 					rc.setIndicatorString(0, "sending attack move msg and spawning");
 					rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE, RobotType.HQ, 0));
 					this.spawnInAvailable();
 					
 				} else if (Clock.getRoundNum() > 1000) {
 					
 					rc.setIndicatorString(0, "researching nuke, sending defend base msg");
 					rc.researchUpgrade(Upgrade.NUKE);
 					rc.broadcast(this.getAllUnitChannelNum(), this.encodeMsg(
 							myBaseLoc,
 							SoldierState.DEFEND_POSITION, RobotType.HQ, 0));
 					
 				} else {
 					rc.setIndicatorString(0, "spawning in available space");
 					this.spawnInAvailable();
 				}
 			}
 
 			if (Clock.getRoundNum() < 70) {
 				// broadcast
 				GameObject[] myUnits = rc.senseNearbyGameObjects(Robot.class,
 						1000, myTeam);
 				RobotType encamp = RobotType.SUPPLIER;
 
 				if (rc.getTeamPower() > 5) {
 					if (myUnits.length <= 2) {
 						encampCounter = 0;
 						encamp = RobotType.SUPPLIER;
 					} else if (myUnits.length <= 4) {
 						encampCounter = 1;
 						encamp = RobotType.SUPPLIER;
 					} else {
 						encampCounter = 2;
 						encamp = RobotType.SHIELDS;
 					}
 					int msg = this.encodeMsg(
 							initialTargetEncampments[encampCounter],
 							SoldierState.SECURE_ENCAMPMENT, encamp, 0);
 
 					rc.broadcast(this.getAllUnitChannelNum(), msg);
 //					rc.broadcast(2, msg);
 //					rc.broadcast(3, msg);
 				}
 			}
 		}
 
 	}
 
 	// checks all available spaces around hq for spawning
 	public boolean spawnInAvailable() throws GameActionException {
 		Direction dir = myBaseLoc.directionTo(enemyBaseLoc);
 		Direction dirOrig = Direction.values()[dir.ordinal()];
 		if (rc.canMove(dir) && rc.senseMine(myBaseLoc.add(dir)) == null) {
 			rc.setIndicatorString(0, "spawning robot at: " + myBaseLoc.add(dir));
 			rc.spawn(dir);
 			return true;
 		}
 		else {
 			dir = dir.rotateLeft();
 			while (!rc.canMove(dir) || rc.senseMine(myBaseLoc.add(dir)) != null || dir.equals(dirOrig)) {
 				dir = dir.rotateLeft();
 			}
 			
 			if (dir.equals(dirOrig)) {
 				// looped all the way around
 				rc.setIndicatorString(0, "all dirs occupied");
 				return false;
 			}
 			else {
 				rc.setIndicatorString(0, "spawning robot at: " + myBaseLoc.add(dir));
 				rc.spawn(dir);
 				return true;
 			}
 		}
 	}
 	@Override
 	public void decodeMsg(int encodedMsg) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public MapLocation[] getTargetEncampments() {
 		MapLocation[] encampments = this.rc.senseAllEncampmentSquares();
 
 		// compute distances of encampments to hq location
 		// sort by distance
 		// take top k
 		// TODO use median of medians?
 		// Arrays.sort(encampments, new MapLocationComparator()); // 6300
 		// bytecodes
 
 		// int targetRange = Math.max(rc.getMapHeight(), rc.getMapWidth())/2;
 		// int targetRangeSquared = targetRange * targetRange;
 		MapLocation[] targetEncampments = new MapLocation[3];
 
 		/*
 		 * int j = 0; if (myBaseLoc.x <= rc.getMapWidth()/2 || myBaseLoc.y <=
 		 * rc.getMapHeight()/2) { for (int i=0; i < encampments.length; i++) {
 		 * if (encampments[i].distanceSquaredTo(myBaseLoc) < targetRangeSquared)
 		 * { targetEncampments[j] = encampments[i]; j++; if (j ==
 		 * targetEncampments.length) { break; } } } } else { for (int
 		 * i=encampments.length-1; i>=0; i--) { if
 		 * (encampments[i].distanceSquaredTo(myBaseLoc) < targetRangeSquared) {
 		 * targetEncampments[j] = encampments[i]; j++; if (j ==
 		 * targetEncampments.length) { break; } } } }
 		 */
 		int[] targetDists = { 1000, 1000, 1000 };
 		for (int i = 0; i < encampments.length; i++) {
 			int dist = myBaseLoc.distanceSquaredTo(encampments[i]);
 			int largestIndex = 0;
 			for (int k = 1; k < 3; k++) {
 				if (targetDists[k] > targetDists[largestIndex]) {
 					largestIndex = k;
 				}
 			}
 			if (targetEncampments[largestIndex] == null
 					|| targetDists[largestIndex] == 0
 					|| dist < targetDists[largestIndex]) {
 				targetDists[largestIndex] = dist;
 				targetEncampments[largestIndex] = encampments[i];
 			}
 		}
 		/*
 		 * [java] [server] basicplayer (A) wins
 		 * System.out.println("start sorted encampments");
 		 * System.out.println("myBaseLoc x: " + myBaseLoc.x + " y: " +
 		 * myBaseLoc.y); for (int i=0; i < targetEncampments.length; i++) {
 		 * System.out.println("x: " + targetEncampments[i].x + " y: " +
 		 * targetEncampments[i].y); }
 		 */
 		return targetEncampments;
 	}
 
 	public class MapLocationComparator implements Comparator {
 
 		@Override
 		public int compare(Object loc0, Object loc1) {
 			// TODO Auto-generated method stub
 			return ((MapLocation) loc0).distanceSquaredTo(myBaseLoc)
 					- ((MapLocation) loc1).distanceSquaredTo(myBaseLoc);
 		}
 
 	}
 
 }
