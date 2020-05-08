 package team063;
 
 import java.util.Comparator;
 
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
 
 //singleton
 public class HQUnit extends BaseUnit {
 	
 	// initial strategies
 	public static final int BIG_MAP_STRAT = 2;
 	public static final int SMALL_MAP_STRAT = 1;
 	public static final int REGULAR_MAP_STRAT = 0;
 	
 	// encampment arrays and relevant vars
 	public MapLocation[] encampmentLocs= rc.senseAllEncampmentSquares();
 	public int numEncampments=encampmentLocs.length;
 	public MapLocation[] zone1Locs= new MapLocation[numEncampments];
 	public MapLocation[] zone2Locs= new MapLocation[numEncampments];
 	public MapLocation[] zone3Locs= new MapLocation[numEncampments];
 	public MapLocation[] zone4Locs= new MapLocation[numEncampments];
 	private int distBetweenBases=myBaseLoc.distanceSquaredTo(enemyBaseLoc);		// the distance between bases
 	private int closeToBase=400;												// the distance which classifies encampments into Zone1
 	private int awayFromEnemyForgiveness=200; 									// the distance added to the distance between bases for Zone1
 	private int awayFromEquidistantForgiveness=200;								// the forgiveness from an equidistant location between both bases allowed for Zone2
 	private int closeToEnemy=400;												// the distance which classifies encampments into Zone3
 	private int farEnoughFromEnemy=600;											// the distance which classifies encampments into Zone4
 	
 	// squad consts
 	public static final int SQUAD_ASSIGNMENT_CHANNEL = 7907;
 	public static final int UNIT_ASSIGNMENT_CHANNEL = 13577;
 	
 	public static final int NO_UNIT_ID = -1;
 	public static final int NO_SQUAD = -1;
 	public static final int SCOUT_SQUAD = 1;
 	public static final int ENCAMPMENT_SQUAD_1 = 2;
 	public static final int ENCAMPMENT_SQUAD_2 = 3;
 	public static final int ENCAMPMENT_SQUAD_3 = 4;
 	public static final int ENCAMPMENT_SQUAD_4 = 5;
 	public static final int ENCAMPMENT_SQUAD_5 = 6;
 	public static final int DEFEND_BASE_SQUAD = 7;
 	public static final int ATTACK_SQUAD = 8;
 
 	// encampment zones
 	protected int[] encampSquadCounters = new int[4];
 	protected int totalEncampSquads = 3;
 	protected int numMaxEncampsZone1 = 0; // 1 per squad
 	protected int numMaxEncampsZone2 = 0; // 2 per squad
 	protected int numMaxEncampsZone3 = 0; // many per squad or many squads
 	protected int numMaxEncampsZone4 = 0; // extras
 	
 	
 	public int unitsCount = 0;
 	protected int[] unitsMap;
 	protected int[] squads;
 	protected MapLocation[] initialTargetEncampments;
 	private int encampCounter;
 	protected int initialStrategy = REGULAR_MAP_STRAT;
 	
 
 	
 	public HQUnit(RobotController rc) {
 		super(rc);
 		this.unitsMap = new int[2000];
 		this.encampCounter = 0;
 		int start = Clock.getBytecodeNum();
 //		System.out.println("getTargetEncampments bytecode usage: "
 //				+ (Clock.getBytecodeNum() - start));
 //		this.initialAnalysis();
 //		System.out.println("initial analysis: " + (Clock.getBytecodeNum() - start));
 		System.out.println("dist squared to enemy: " + this.distToEnemyBaseSquared + ", my hq loc: " + this.myBaseLoc + ", enemy hq loc: " + this.enemyBaseLoc);
 		System.out.println("dist squared: " + rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
 	}
 
 	public void runTest() {
 		
 	}
 	
 	@Override
 	public void run() throws GameActionException {
 		
 		if (mapHeight > 65 && mapWidth > 65) {
 			System.out.println("big map: nuke strategy");
 			// big map, nuke strategy
 
 			if (Clock.getRoundNum() < 100) {
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						initialTargetEncampments[0],
 						SoldierState.SECURE_ENCAMPMENT, RobotType.ARTILLERY, 0));
 				if (rc.isActive()) {
 					this.spawnInAvailable();
 				}
 			} else if (Clock.getRoundNum() < 200) {
 				// spawn robots
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						myBaseLoc, SoldierState.DEFEND_POSITION, RobotType.HQ,
 						0));
 
 				if (rc.isActive()) {
 					this.spawnInAvailable();
 				}
 			} else {
 				if (rc.isActive()) {
 					rc.researchUpgrade(Upgrade.NUKE);
 				}
 
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						myBaseLoc, SoldierState.DEFEND_POSITION, RobotType.HQ,
 						0));
 			}
 
 		} else if (this.distToEnemyBaseSquared <= 800) {
 			System.out.println("small map: rush strategy");
 			// small map, rush strategy
 			if (Clock.getRoundNum() <= 100
 					&& myBaseLoc.distanceSquaredTo(initialTargetEncampments[0]) <= 150
 					&& (myBaseLoc.directionTo(initialTargetEncampments[0]).equals(myBaseLoc.directionTo(enemyBaseLoc)) || 
 							myBaseLoc.directionTo(initialTargetEncampments[0]).equals(myBaseLoc.directionTo(enemyBaseLoc).rotateLeft()) || 
 							myBaseLoc.directionTo(initialTargetEncampments[0]).equals(myBaseLoc.directionTo(enemyBaseLoc).rotateRight()))) {
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						initialTargetEncampments[0],
 						SoldierState.SECURE_ENCAMPMENT, RobotType.ARTILLERY, 0));
 
 			} else if (Clock.getRoundNum() <= 100) {
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						myBaseLoc, SoldierState.DEFEND_POSITION, RobotType.HQ,
 						0));
 			} else {
 				rc.broadcast(Util.getAllUnitChannelNum(), Util
 						.encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE,
 								RobotType.HQ, 0));
 			}
 
 			if (rc.isActive()) {
 				this.spawnInAvailable();
 			}
 		} else {
 			System.out.println("not small or big map");
 			// check enemy nuke progress
 			if (Clock.getRoundNum() >= 200) {
 				if (rc.getTeamPower() >= 50) {
 					if (rc.senseEnemyNukeHalfDone()) {
 						// enemy half done with nuke, broadcast attack enemy
 						// base to all units
 						rc.broadcast(Util.getAllUnitChannelNum(), Util
 								.encodeMsg(enemyBaseLoc,
 										SoldierState.ATTACK_MOVE, RobotType.HQ,
 										0));
 					}
 				}
 			}
 
 			if (rc.getTeamPower() >= 2*GameConstants.BROADCAST_SEND_COST) {
 //				rc.broadcast(Util.getInitialSquadNumChannelNum(),
 //						getCurrentSquadAssignment());
 				rc.broadcast(Util.getInitialUnitNumChannelNum(), getCurrentUnitAssignment());
 				
 				if (Clock.getRoundNum() < 100) {
 					// broadcast
 					GameObject[] myUnits = rc.senseNearbyGameObjects(
 							Robot.class, 1000, myTeam);
 					RobotType encampSup = RobotType.SUPPLIER;
 					RobotType encampGen = RobotType.GENERATOR;
 
 					if (rc.getTeamPower() >= unitsCount*GameConstants.BROADCAST_SEND_COST) {
 						int firstSquadLimit = Math.min(6, unitsCount);
 						for (int id=0; id < firstSquadLimit; id++) {
 							rc.broadcast(Util.getUnitChannelNum(id), Util.encodeUnitSquadAssignmentChangeMsg(this.getSquadAssignment(id, Clock.getRoundNum())));
 						}
 						
 						for (int i = firstSquadLimit; i < unitsCount; i++) {
 							System.out.println("broadcasting to unit: " + i);
 							rc.broadcast(Util.getUnitChannelNum(i), Util.encodeMsg(initialTargetEncampments[i - firstSquadLimit + 1], SoldierState.SECURE_ENCAMPMENT, RobotType.SUPPLIER, 0));
 						}
 						
 						rc.broadcast(Util.getSquadChannelNum(ENCAMPMENT_SQUAD_1), Util.encodeMsg(initialTargetEncampments[0], SoldierState.SECURE_ENCAMPMENT, RobotType.ARTILLERY, 0));
 						rc.broadcast(Util.getSquadChannelNum(SCOUT_SQUAD), Util.encodeMsg(enemyBaseLoc, SoldierState.SCOUT,
 										RobotType.HQ, 0));
 						if (rc.isActive()) {
 							this.spawnInAvailable();
 						}
 					}
 				}
 				else if (Clock.getRoundNum() >= 100
 						&& Clock.getRoundNum() <= 300) {
 					rc.setIndicatorString(
 							1,
 							"round: "
 									+ Clock.getRoundNum()
 									+ " rally at shields, writing to channel: "
 									+ Util.getAllUnitExceptScoutChannelNum()
 									+ " msg: "
 									+ Util.encodeMsg(
 											initialTargetEncampments[2],
 											SoldierState.BRUTE_MOVE,
 											RobotType.SUPPLIER, 0));
 					// if encampments done
 					// TODO, fix this to use broadcasting later
 					MapLocation[] encamp0 = rc.senseEncampmentSquares(
 							initialTargetEncampments[0], 0, myTeam);
 					MapLocation[] encamp1 = rc.senseEncampmentSquares(
 							initialTargetEncampments[1], 0, myTeam);
 					MapLocation[] encamp2 = rc.senseEncampmentSquares(
 							initialTargetEncampments[2], 0, myTeam);
 					MapLocation[] encamp3 = rc.senseEncampmentSquares(
 							initialTargetEncampments[3], 0, myTeam);
 					MapLocation[] encamp4 = rc.senseEncampmentSquares(
 							initialTargetEncampments[4], 0, myTeam);
 
 					if (encamp0.length != 0 && encamp1.length != 0
 							&& encamp2.length != 0 && encamp3.length != 0
 							&& encamp4.length != 0) {
 						if (initialTargetEncampments[2]
 								.distanceSquaredTo(enemyBaseLoc) < myBaseLoc
 								.distanceSquaredTo(enemyBaseLoc)) {
 							rc.broadcast(
 									Util.getAllUnitExceptScoutChannelNum(),
 									Util.encodeMsg(initialTargetEncampments[2],
 											SoldierState.BRUTE_MOVE,
 											RobotType.SUPPLIER, 0));
 						} else {
 							rc.broadcast(
 									Util.getAllUnitExceptScoutChannelNum(),
 									Util.encodeMsg(myBaseLoc,
 											SoldierState.BRUTE_MOVE,
 											RobotType.SUPPLIER, 0));
 						}
 					} else {
 						// keep on capturing
 						rc.broadcast(Util
 								.getSquadChannelNum(ENCAMPMENT_SQUAD_1), Util
 								.encodeMsg(initialTargetEncampments[0],
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.SUPPLIER, 0));
 						rc.broadcast(Util
 								.getSquadChannelNum(ENCAMPMENT_SQUAD_2), Util
 								.encodeMsg(initialTargetEncampments[1],
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.SUPPLIER, 0));
 						rc.broadcast(Util
 								.getSquadChannelNum(ENCAMPMENT_SQUAD_3), Util
 								.encodeMsg(initialTargetEncampments[2],
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.SUPPLIER, 0));
 						rc.broadcast(Util
 								.getSquadChannelNum(ENCAMPMENT_SQUAD_3), Util
 								.encodeMsg(initialTargetEncampments[3],
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.SUPPLIER, 0));
 						rc.broadcast(Util
 								.getSquadChannelNum(ENCAMPMENT_SQUAD_3), Util
 								.encodeMsg(initialTargetEncampments[4],
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.GENERATOR, 0));
 						rc.broadcast(
 								Util.getSquadChannelNum(DEFEND_BASE_SQUAD),
 								Util.encodeMsg(myBaseLoc,
 										SoldierState.DEFEND_POSITION,
 										RobotType.HQ, 0));
 					}
 
 					if (!rc.hasUpgrade(Upgrade.FUSION)) {
 
 						rc.setIndicatorString(0,
 								"researching FUSION, telling units to rally at shields");
 						if (rc.isActive()) {
 							rc.researchUpgrade(Upgrade.FUSION);
 						}
 
 					} else {
 						if (rc.isActive()) {
 							this.spawnInAvailable();
 						}
 					}
 
 				}
 				else if (Clock.getRoundNum() > 300
 						&& Clock.getRoundNum() <= 1000) {
 
 					rc.setIndicatorString(0,
 							"sending attack move msg and spawning");
 					rc.broadcast(Util.getAllUnitExceptScoutChannelNum(), Util
 							.encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE,
 									RobotType.HQ, 0));
 
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 
 				} else if (Clock.getRoundNum() > 1000) {
 
 					rc.setIndicatorString(0,
 							"researching nuke, sending defend base msg");
 					if (rc.isActive()) {
 						rc.researchUpgrade(Upgrade.NUKE);
 					}
 					rc.broadcast(Util.getAllUnitExceptScoutChannelNum(), Util
 							.encodeMsg(myBaseLoc, SoldierState.DEFEND_POSITION,
 									RobotType.HQ, 0));
 
 				} else {
 					rc.setIndicatorString(0, "spawning in available space");
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 				}
 			}
 
 		}
 
 	}
 	
 	public int getCurrentUnitAssignment() {
 		return unitsCount;
 	}
 
 	public int getSquadAssignment(int unit, int round) {
 		if (unit == 0 ) {
 			return SCOUT_SQUAD;
 		}
 		else if (unit <= 5) {
 			return ENCAMPMENT_SQUAD_1;
 		}
 		else if (unit >= 11 && unit <= 15) {
 			return ENCAMPMENT_SQUAD_2;
 		}
 		return DEFEND_BASE_SQUAD;
 	}
 	// checks all available spaces around hq for spawning
 	public boolean spawnInAvailable() throws GameActionException {
 		Direction dir = myBaseLoc.directionTo(enemyBaseLoc);
 		Direction dirOrig = Direction.values()[dir.ordinal()];
 		if (rc.canMove(dir) && rc.senseMine(myBaseLoc.add(dir)) == null) {
 			rc.setIndicatorString(0, "spawning robot at: " + myBaseLoc.add(dir));
 			rc.spawn(dir);
 			unitsCount++;
 			return true;
 		} else {
 			dir = dir.rotateLeft();
 			while (!rc.canMove(dir) || rc.senseMine(myBaseLoc.add(dir)) != null
 					|| dir.equals(dirOrig)) {
 				dir = dir.rotateLeft();
 			}
 
 			if (dir.equals(dirOrig)) {
 				// looped all the way around
 				rc.setIndicatorString(0, "all dirs occupied");
 				return false;
 			} else {
 				rc.setIndicatorString(0,
 						"spawning robot at: " + myBaseLoc.add(dir));
 				rc.spawn(dir);
 				unitsCount++;
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
 		MapLocation[] targetEncampments = new MapLocation[10];
 
 		int[] targetDists = { 1000, 1000, 1000, 1000, 1000 };
 		for (int i = 0; i < encampments.length; i++) {
 			int dist = myBaseLoc.distanceSquaredTo(encampments[i]);
 			int largestIndex = 0;
 			for (int k = 1; k < 5; k++) {
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
 
 	public void initialAnalysisAndInitialization() {
 		int numEncampmentsBetweenHQs = 0;
 		int numNeutralMinesBetweenHQs = 0;
 		int numTotalMines = 0;
 		int numTotalEncampments = 0;
 		int[][] mineMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT];
 		int[][] encampmentMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT];
 		
 		MapLocation[] allEncampmentLocs = rc.senseAllEncampmentSquares();
 		
 		// should cover all mines
 		MapLocation[] allNeutralMineLocs = rc.senseMineLocations(new MapLocation(mapWidth/2, mapHeight/2), 2000, Team.NEUTRAL);
 		
 		for (int i=0; i < allEncampmentLocs.length; i++) {
 			encampmentMap[allEncampmentLocs[i].x][allEncampmentLocs[i].y] = 1;	
 		}
 		
 		for (int i=0; i < allNeutralMineLocs.length; i++) {
 			mineMap[allNeutralMineLocs[i].x][allNeutralMineLocs[i].y] = 1;
 		}
 		
 		int startX = Math.min(myBaseLoc.x, enemyBaseLoc.x);
 		int endX = Math.max(myBaseLoc.x, enemyBaseLoc.x);
 		int startY = Math.min(myBaseLoc.y, enemyBaseLoc.y);
 		int endY = Math.max(myBaseLoc.y, enemyBaseLoc.y);
 		
 		for (int i = startX; i <= endX; i++) {
 			for (int j = startY; j <= endY; j++) {
 				numEncampmentsBetweenHQs += encampmentMap[i][j];
 				numNeutralMinesBetweenHQs += mineMap[i][j];
 			}
 		}
 		
 		System.out.println("num encampments between hqs: " + numEncampmentsBetweenHQs);
 		System.out.println("num neutral mines between hqs: " + numNeutralMinesBetweenHQs);
 
 		// start out building suppliers and generators in encampments far-ish from base
 		// defense strategy: build shields(25%), medbays(25%), artillery(50%), capture neutral and enemy medbays near base, build nuke
 		// offense strategy: build artillery near their hq, 
 		//					 build shields if they have artillery near base, 
 		//					 attacking units should recharge at nearby shields and medbay
 		// build artillery at most within 7 units of bases
 		
 		// heuristics? build more artillery when there are less mines around
 	}
 
 	// not used
 	public void findPath(MapLocation start, MapLocation goal) {
 		MapLocation[] path = new MapLocation[200];
 		int[][] costs = new int[mapHeight][mapWidth];
 		MapLocation[] mines = rc.senseNonAlliedMineLocations(new MapLocation(
 				mapHeight / 2, mapWidth / 2), 2500);
 		int total = mapHeight * mapWidth;
 		int[] mineMap = new int[total];
 		for (int i = 0; i < mines.length; i++) {
 			int index = mines[i].x + mines[i].y * mapWidth;
 			mineMap[index] = 1;
 		}
 
 		for (int i = 0; i < mapHeight; i++) {
 			for (int j = 0; j < mapWidth; j++) {
 				int index = i + j * mapWidth;
 				if (mineMap[index] == 0) {
 					costs[i][j] = 1;
 				} else {
 					costs[i][j] = 12;
 				}
 			}
 		}
 
 		int[] alreadyEvaluated = new int[total];
 
 	}
 
 	public void zoneEncampments(MapLocation[] encampmentLocs){
 		int zone1Counter=0; int zone2Counter=0; int zone3Counter=0; int zone4Counter=0;
 		for (MapLocation encampment:encampmentLocs){
 			int distToMyBase=encampment.distanceSquaredTo(myBaseLoc);
 			int distToEnemyBase=encampment.distanceSquaredTo(enemyBaseLoc);
 			//zone1
 			if (distToMyBase<=closeToBase && distToEnemyBase<=(distBetweenBases+awayFromEnemyForgiveness)){
 				zone1Locs[zone1Counter]=encampment;
 				zone1Counter+=1;
 			}
 			//zone2
 			else if (distToMyBase<=(distBetweenBases+awayFromEquidistantForgiveness) && distToEnemyBase<=(distBetweenBases+awayFromEquidistantForgiveness)){
 				zone2Locs[zone2Counter]=encampment;
 				zone2Counter+=1;
 			}
 			//zone3
 			else if (distToEnemyBase<=closeToEnemy && distToMyBase<=distBetweenBases){
 				zone3Locs[zone3Counter]=encampment;
 				zone3Counter+=1;
 			}
 			//zone 4
 			else if (distToEnemyBase>=farEnoughFromEnemy){
 				zone4Locs[zone4Counter]=encampment;
 				zone4Counter+=1;
 			}
 		}
 	}
 	
 	public class MapLocationComparator implements Comparator {
 
 		@Override
 		public int compare(Object loc0, Object loc1) {
 			// TODO Auto-generated method stub
 			return ((MapLocation) loc0).distanceSquaredTo(myBaseLoc)
 					- ((MapLocation) loc1).distanceSquaredTo(myBaseLoc);
 		}
 
 	}
 	
 	public class EncampmentComparatorZone1 implements Comparator {
 		//zone1 is the zone near our base, probably only one robot per squad needed
 		@Override
 		public int compare(Object Enc0, Object Enc1){
			if (Enc0==null){ return -1;} if (Enc1==null){ return 1;}
 			return (((MapLocation) Enc0).distanceSquaredTo(myBaseLoc)-((MapLocation) Enc1).distanceSquaredTo(myBaseLoc));
 		}
 	}
 	
 	public class EncampmentComparatorZone2 implements Comparator {
 		//zone2 is the zone near the center of the map, may need medium sized squads and quick action to take these
 		@Override
 		public int compare(Object Enc0, Object Enc1){
			if (Enc0==null){ return -1;} if (Enc1==null){ return 1;}
 			return ((((MapLocation) Enc0).distanceSquaredTo(myBaseLoc)+((MapLocation) Enc0).distanceSquaredTo(enemyBaseLoc))-(((MapLocation) Enc1).distanceSquaredTo(myBaseLoc)+((MapLocation) Enc1).distanceSquaredTo(enemyBaseLoc)));
 		}
 	}
 	
 	public class EncampmentComparatorZone3 implements Comparator {
 		//zone3 is the zone near the enemy base we would want to put artillery, this should be taken when we are already pushing into the enemy lines, but probably not done immediately
 		@Override
 		public int compare(Object Enc0,Object Enc1){
			if (Enc0==null){ return -1;} if (Enc1==null){ return 1;}
 			return ((((MapLocation) Enc0).distanceSquaredTo(enemyBaseLoc)*2+((MapLocation) Enc0).distanceSquaredTo(myBaseLoc))-(((MapLocation) Enc1).distanceSquaredTo(enemyBaseLoc)*2+((MapLocation) Enc1).distanceSquaredTo(myBaseLoc)));
 		}
 	}
 	
 	public class EncampmentComparatorZone4 implements Comparator {
 		//zone4 is the "zone" of far away encampments that aren't near the enemy base
 		@Override
 		public int compare(Object Enc0,Object Enc1){
			if (Enc0==null){ return -1;} if (Enc1==null){ return 1;}
 			return (((MapLocation) Enc0).distanceSquaredTo(myBaseLoc)-((MapLocation) Enc1).distanceSquaredTo(myBaseLoc));
 		}
 	}
 }
