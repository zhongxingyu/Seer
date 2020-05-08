 package team063;
 
 import java.util.Arrays;
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
 	public static final int ZONE_ENCAMPMENT_LIMIT = 10;
 	public MapLocation[] encampmentLocs = rc.senseAllEncampmentSquares();
 	public int numEncampments = encampmentLocs.length;
 
 	public MapLocation[] zone1Locs = new MapLocation[Math.min(numEncampments,
 			ZONE_ENCAMPMENT_LIMIT)];
 	public MapLocation[] zone2Locs = new MapLocation[Math.min(numEncampments,
 			ZONE_ENCAMPMENT_LIMIT)];
 	public MapLocation[] zone3Locs = new MapLocation[Math.min(numEncampments,
 			ZONE_ENCAMPMENT_LIMIT)];
 	public MapLocation[] zone4Locs = new MapLocation[Math.min(numEncampments,
 			ZONE_ENCAMPMENT_LIMIT)];
 	private int distBetweenBases = myBaseLoc.distanceSquaredTo(enemyBaseLoc); // the
 																				// distance
 																				// between
 																				// bases
 	private int closeToBase = (int) Math.min(300, distBetweenBases * .35); // the
 																			// distance
 																			// which
 																			// classifies
 																			// encampments
 																			// into
 																			// Zone1
 	private int awayFromEnemyForgiveness = (int) Math
 			.round(distBetweenBases * .2); // the distance added to the distance
 											// between bases for Zone1
 	private int awayFromEquidistantForgiveness = (int) Math.max(
			distBetweenBases * .02, 35); // the forgiveness from an equidistant
 											// location between both bases
 											// allowed for Zone2
 	private int closeToEnemy = 300; // the distance which classifies encampments
 									// into Zone3
 	private int farEnoughFromEnemy = 400; // the distance which classifies
 											// encampments into Zone4
 	private MapLocation chosenEncampment = null;
 
 	// squad consts
 	public static final int SQUAD_ASSIGNMENT_CHANNEL = 6011;
 	public static final int UNIT_ASSIGNMENT_CHANNEL = 11489;
 
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
 	protected int endZone1Index = 0; // 1 per squad
 	protected int endZone2Index = 0; // 2 per squad
 	protected int endZone3Index = 0; // many per squad or many squads
 	protected int endZone4Index = 0; // extras
 	private int curZone1Counter = 0;
 	private int curZone2Counter = 0;
 	private int curZone3Counter = 0;
 	private int curZone4Counter = 0;
 
 	public int unitsCount = 0;
 	protected int[] unitsMap;
 	protected int[] squads;
 	private int encampCounter;
 	protected int initialStrategy = REGULAR_MAP_STRAT;
 
 	protected RobotType[] supGenSelection = { RobotType.SUPPLIER,
 			RobotType.SUPPLIER, RobotType.GENERATOR, RobotType.SUPPLIER,
 			RobotType.SUPPLIER, RobotType.GENERATOR, RobotType.SUPPLIER,
 			RobotType.SUPPLIER, RobotType.GENERATOR, RobotType.SUPPLIER };
 	protected RobotType[] encampSelection = { RobotType.ARTILLERY,
 			RobotType.ARTILLERY, RobotType.ARTILLERY, RobotType.SHIELDS,
 			RobotType.ARTILLERY, RobotType.MEDBAY, RobotType.ARTILLERY,
 			RobotType.SHIELDS, RobotType.ARTILLERY, RobotType.MEDBAY,
 			RobotType.ARTILLERY, RobotType.SHIELDS };
 
 	public HQUnit(RobotController rc) {
 		super(rc);
 		this.unitsMap = new int[2000];
 		this.encampCounter = 0;
 		int start = Clock.getBytecodeNum();
 		if (rc.isActive()) {
 			try {
 				this.spawnInAvailable();
 				rc.broadcast(Util.getUnitChannelNum(0),
 						Util.encodeUnitSquadAssignmentChangeMsg(SCOUT_SQUAD));
 				rc.broadcast(Util.getSquadChannelNum(SCOUT_SQUAD), Util
 						.encodeMsg(enemyBaseLoc, SoldierState.SCOUT,
 								RobotType.HQ, 0));
 			} catch (GameActionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		this.zoneEncampments(encampmentLocs);
 		Arrays.sort(zone1Locs, new EncampmentComparatorZone1());
 		Arrays.sort(zone2Locs, new EncampmentComparatorZone2());
 		Arrays.sort(zone3Locs, new EncampmentComparatorZone3());
 		Arrays.sort(zone4Locs, new EncampmentComparatorZone4());
 
 		System.out.println("zone 1 sorted encampments:");
 		for (int i = 0; i < zone1Locs.length; i++) {
 			System.out.println(zone1Locs[i]);
 		}
 		System.out.println("zone 2 sorted encampments:");
 		for (int i = 0; i < zone2Locs.length; i++) {
 			System.out.println(zone2Locs[i]);
 		}
 		System.out.println("zone 3 sorted encampments:");
 		for (int i = 0; i < zone3Locs.length; i++) {
 			System.out.println(zone3Locs[i]);
 		}
 		System.out.println("zone 4 sorted encampments:");
 		for (int i = 0; i < zone4Locs.length; i++) {
 			System.out.println(zone4Locs[i]);
 		}
 
 	}
 
 	public void runTest() throws GameActionException {
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		if (mapHeight > 65 && mapWidth > 65) {
 			// big map, nuke strategy
 
 			if (Clock.getRoundNum() < 100) {
 				if (zone1Locs[0] != null) {
 					rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 							zone1Locs[0], SoldierState.SECURE_ENCAMPMENT,
 							RobotType.ARTILLERY, 0));
 				} else {
 					rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 							myBaseLoc, SoldierState.DEFEND_POSITION,
 							RobotType.HQ, 0));
 				}
 				if (rc.isActive()) {
 					this.spawnInAvailable();
 				}
 			} else if (Clock.getRoundNum() >= 100 && Clock.getRoundNum() < 200) {
 				// spawn robots
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						myBaseLoc, SoldierState.DEFEND_POSITION, RobotType.HQ,
 						0));
 				if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
 					if (rc.isActive()) {
 						rc.researchUpgrade(Upgrade.PICKAXE);
 					}
 				} else {
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 				}
 
 			} else {
 				System.out.println("researching nuke " + Clock.getRoundNum());
 				if (rc.isActive()) {
 					rc.researchUpgrade(Upgrade.NUKE);
 				}
 
 				rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 						myBaseLoc, SoldierState.DEFEND_POSITION, RobotType.HQ,
 						0));
 			}
 
 		}
 
 		if (this.distToEnemyBaseSquared <= 800) {
 			// System.out.println("small map: rush strategy");
 			// small map, rush strategy
 			if (rc.isActive()) {
 				this.spawnInAvailable();
 			}
 			if (Clock.getRoundNum() <= 100) {
 
 				for (int i = 0; i <= 3; i++) {
 					if (zone1Locs[i] != null) {
 						if (myBaseLoc.directionTo(zone1Locs[i]).equals(
 								myBaseLoc.directionTo(enemyBaseLoc))
 								|| myBaseLoc.directionTo(zone1Locs[i]).equals(
 										myBaseLoc.directionTo(enemyBaseLoc)
 												.rotateLeft())
 								|| myBaseLoc.directionTo(zone1Locs[i]).equals(
 										myBaseLoc.directionTo(enemyBaseLoc)
 												.rotateRight())) {
 
 							rc.broadcast(Util.getAllUnitChannelNum(), Util
 									.encodeMsg(zone1Locs[i],
 											SoldierState.SECURE_ENCAMPMENT,
 											RobotType.ARTILLERY, 0));
 							if (chosenEncampment == null) {
 								chosenEncampment = zone1Locs[i];
 							}
 						}
 					}
 				}
 
 			} else if (Clock.getRoundNum() <= 150 && Clock.getRoundNum() > 100) {
 				System.out.println(chosenEncampment);
 				if (chosenEncampment != null) {
 					rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 							chosenEncampment.add(chosenEncampment
 									.directionTo(enemyBaseLoc)),
 							SoldierState.DEFEND_POSITION, RobotType.HQ, 0));
 				} else {
 					rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 							myBaseLoc, SoldierState.DEFEND_POSITION,
 							RobotType.HQ, 0));
 				}
 
 			} /*
 			 * else if (zone3Locs[0]!=null){
 			 * rc.broadcast(Util.getAllUnitChannelNum
 			 * (),Util.encodeMsg(zone3Locs[0], SoldierState.SECURE_ENCAMPMENT,
 			 * RobotType.ARTILLERY, 0));
 			 * 
 			 * }
 			 */else {
 				rc.broadcast(Util.getAllUnitChannelNum(), Util
 						.encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE,
 								RobotType.HQ, 0));
 			}
 
 		} else {
 			// check enemy nuke progress
 			boolean nukeDetected = false;
 			if (Clock.getRoundNum() >= 200) {
 				if (rc.getTeamPower() >= GameConstants.BROADCAST_SEND_COST
 						&& rc.checkResearchProgress(Upgrade.NUKE) < 200) {
 					if (rc.senseEnemyNukeHalfDone()) {
 						// enemy half done with nuke, broadcast attack enemy
 						// base to all units
 						rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(enemyBaseLoc,
 										SoldierState.RUSH_ENEMY_HQ,
 										RobotType.HQ, 0));
 						if (rc.isActive()) {
 							if (!rc.hasUpgrade(Upgrade.DEFUSION)) {
 								rc.researchUpgrade(Upgrade.DEFUSION);
 							} else {
 								this.spawnInAvailable();
 							}
 						}
 						nukeDetected = true;
 					}
 				}
 			} 
 			
 			if (!nukeDetected) {
 
 				rc.broadcast(Util.getInitialUnitNumChannelNum(),
 						getCurrentUnitAssignment());
 
 				if (Clock.getRoundNum() < 300) {
 					// broadcast
 					if (rc.getTeamPower() >= 5 * GameConstants.BROADCAST_SEND_COST) {
 
 						if (unitsCount >= 1 && unitsCount < 6) {
 							rc.broadcast(Util.getUnitChannelNum(unitsCount), Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_1));
 						}
 
 						if (unitsCount >= 6 && unitsCount < 11) {
 							int zone1Index = unitsCount - 6;
 							if (zone1Index > endZone1Index
 									|| zone1Locs[zone1Index] == null) {
 								rc.broadcast(
 										Util.getUnitChannelNum(unitsCount),
 										Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_1));
 							} else {
 								rc.broadcast(
 										Util.getUnitChannelNum(unitsCount),
 										Util.encodeMsg(zone1Locs[zone1Index],
 												SoldierState.SECURE_ENCAMPMENT,
 												supGenSelection[zone1Index], 0));
 							}
 						}
 
 						if (unitsCount >= 11 && unitsCount < 16) {
 							rc.broadcast(
 									Util.getUnitChannelNum(unitsCount),
 									Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_2));
 						}
 
 						if (unitsCount >= 16 && unitsCount < 21) {
 							int index = unitsCount - 11;
 							if (index > endZone1Index
 									|| zone1Locs[curZone1Counter] == null) {
 								rc.broadcast(
 										Util.getUnitChannelNum(unitsCount),
 										Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_1));
 							} else {
 								rc.broadcast(
 										Util.getUnitChannelNum(unitsCount),
 										Util.encodeMsg(zone1Locs[index],
 												SoldierState.SECURE_ENCAMPMENT,
 												supGenSelection[index], 0));
 							}
 						}
 
 						if (unitsCount >= 21 && unitsCount < 26) {
 							rc.broadcast(
 									Util.getUnitChannelNum(unitsCount),
 									Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_3));
 
 						}
 
 						if (unitsCount >= 26 && unitsCount < 30) {
 							rc.broadcast(
 									Util.getUnitChannelNum(unitsCount),
 									Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_4));
 
 						}
 
 						if (unitsCount > 30) {
 							rc.broadcast(
 									Util.getUnitChannelNum(unitsCount),
 									Util.encodeUnitSquadAssignmentChangeMsg(ENCAMPMENT_SQUAD_1));
 						}
 
 						// if encampment captured, capture the next one
 						if (rc.canSenseSquare(zone2Locs[curZone2Counter])) {
 							GameObject obj = rc
 									.senseObjectAtLocation(zone2Locs[curZone2Counter]);
 							// should use broadcast, oh well
 							Robot[] objs = rc.senseNearbyGameObjects(
 									Robot.class, zone2Locs[curZone2Counter], 1,
 									myTeam);
 
 							if (obj != null
 									&& obj.getTeam().equals(myTeam)
 									&& rc.senseRobotInfo(objs[0]).type.isEncampment) {
 								// prev encampment captured
 								curZone2Counter = Math.min(curZone2Counter + 1,
 										endZone2Index);
 							}
 						}
 
 						rc.broadcast(
 								Util.getUnitChannelNum(0),
 								Util.encodeUnitSquadAssignmentChangeMsg(SCOUT_SQUAD));
 						Robot[] enemiesByBase = rc.senseNearbyGameObjects(
 								Robot.class, distBetweenBases / 9, otherTeam);
 						if (enemiesByBase.length > 0) {
 							System.out.println("base under attack");
 							rc.broadcast(
 									Util.getAllUnitChannelNum(),
 									Util.encodeMsg(
 											rc.senseRobotInfo(enemiesByBase[0]).location,
 											SoldierState.ATTACK_MOVE,
 											RobotType.HQ, 0));
 						} else {
 							rc.broadcast(
 									Util.getSquadChannelNum(ENCAMPMENT_SQUAD_1),
 									Util.encodeMsg(zone2Locs[curZone2Counter],
 											SoldierState.SECURE_ENCAMPMENT,
 											encampSelection[curZone2Counter], 0));
 							rc.broadcast(Util
 									.getSquadChannelNum(ENCAMPMENT_SQUAD_2),
 									Util.encodeMsg(zone2Locs[Math.max(0,
 											curZone2Counter - 1)],
 											SoldierState.SECURE_ENCAMPMENT,
 											encampSelection[Math.max(0,
 													curZone2Counter - 1)], 0));
 							rc.broadcast(Util
 									.getSquadChannelNum(ENCAMPMENT_SQUAD_3),
 									Util.encodeMsg(zone2Locs[Math.max(0,
 											curZone2Counter - 2)],
 											SoldierState.SECURE_ENCAMPMENT,
 											encampSelection[Math.max(0,
 													curZone2Counter - 2)], 0));
 							rc.broadcast(Util
 									.getSquadChannelNum(ENCAMPMENT_SQUAD_4),
 									Util.encodeMsg(zone2Locs[Math.max(0,
 											curZone2Counter - 3)],
 											SoldierState.SECURE_ENCAMPMENT,
 											encampSelection[Math.max(0,
 													curZone2Counter - 3)], 0));
 						}
 						// suicide scout
 						rc.broadcast(Util.getSquadChannelNum(SCOUT_SQUAD), Util
 								.encodeMsg(enemyBaseLoc, SoldierState.SCOUT,
 										RobotType.HQ, 0));
 
 					}
 
 					// upgrades
 					if (unitsCount > 30) {
 						if (!rc.hasUpgrade(Upgrade.VISION)) {
 							if (rc.isActive()) {
 								rc.researchUpgrade(Upgrade.VISION);
 							}
 						} else if (!rc.hasUpgrade(Upgrade.FUSION)) {
 							if (rc.isActive()) {
 								rc.researchUpgrade(Upgrade.FUSION);
 							}
 						}
 					}
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 
 				} else if (Clock.getRoundNum() > 300
 						&& Clock.getRoundNum() <= 425) {
 					System.out.println("between 300 and 425");
 					MapLocation target = zone2Locs[0];
 					if (target == null) {
 						target = new MapLocation(mapWidth / 2, mapHeight / 2);
 						rc.broadcast(Util.getAllUnitExceptScoutChannelNum(),
 								Util.encodeMsg(target, SoldierState.BRUTE_MOVE,
 										RobotType.ARTILLERY, 0));
 					} else {
 						rc.broadcast(Util.getAllUnitExceptScoutChannelNum(),
 								Util.encodeMsg(target,
 										SoldierState.SECURE_ENCAMPMENT,
 										RobotType.ARTILLERY, 0));
 					}
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 
 				} else if (Clock.getRoundNum() > 425
 						&& Clock.getRoundNum() <= 1200) {
 					System.out.println("broadcasting attack_move at enemyBaseLoc to all units");
 					rc.broadcast(Util.getAllUnitChannelNum(), Util.encodeMsg(
 							enemyBaseLoc, SoldierState.ATTACK_MOVE,
 							RobotType.HQ, 0));
 					// // if (rc.senseCaptureCost() > rc.getTeamPower()) {
 					// // rc.broadcast(Util.getAllUnitChannelNum(), Util
 					// // .encodeMsg(enemyBaseLoc, SoldierState.ATTACK_MOVE,
 					// // RobotType.HQ, 0));
 					// // }
 					// // else {
 					// // // if encampment captured, capture the next one
 					// // if (rc.canSenseSquare(zone3Locs[curZone3Counter])) {
 					// // GameObject obj = rc
 					// // .senseObjectAtLocation(zone3Locs[curZone3Counter]);
 					// // // should use broadcast, oh well
 					// // Robot[] objs = rc.senseNearbyGameObjects(
 					// // Robot.class, zone3Locs[curZone3Counter], 1,
 					// // myTeam);
 					// //
 					// // if (obj != null
 					// // && obj.getTeam().equals(myTeam)
 					// // && rc.senseRobotInfo(objs[0]).type.isEncampment) {
 					// // // prev encampment captured
 					// // System.out.println("prev encampment captured: " +
 					// curZone3Counter);
 					// // curZone3Counter = Math.min(curZone3Counter + 1,
 					// // endZone3Index);
 					// // }
 					// // }
 					// //
 					// // rc.broadcast(Util.getAllUnitChannelNum(), Util
 					// // .encodeMsg(zone3Locs[curZone3Counter],
 					// // SoldierState.SECURE_ENCAMPMENT,
 					// // RobotType.ARTILLERY, 0));
 					// // }
 					if (rc.isActive()) {
 						this.spawnInAvailable();
 					}
 				}
 
 				else if (Clock.getRoundNum() > 1200) {
 
 					rc.setIndicatorString(0,
 							"researching nuke, sending defend base msg");
 					if (rc.isActive()) {
 						if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
 							rc.researchUpgrade(Upgrade.PICKAXE);
 						} else {
 							rc.researchUpgrade(Upgrade.NUKE);
 						}
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
 
 			// can't move in dir, or if there is a non-allied mine in the way,
 			// then try dir.rotateLeft()
 			while (!rc.canMove(dir)
 					|| (rc.senseMine(myBaseLoc.add(dir)) != null && !rc
 							.senseMine(myBaseLoc.add(dir)).equals(myTeam))) {
 				dir = dir.rotateLeft();
 			}
 
 			if (dir.equals(dirOrig)) {
 				// looped all the way around
 				rc.setIndicatorString(0, "all dirs occupied");
 				return false;
 			} else {
 				rc.setIndicatorString(0,
 						"spawning robot at: " + myBaseLoc.add(dir));
 				// TODO: fix this
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
 
 	public void initialAnalysisAndInitialization() {
 		int numEncampmentsBetweenHQs = 0;
 		int numNeutralMinesBetweenHQs = 0;
 		int numTotalMines = 0;
 		int numTotalEncampments = 0;
 		int[][] mineMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT];
 		int[][] encampmentMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT];
 
 		MapLocation[] allEncampmentLocs = rc.senseAllEncampmentSquares();
 
 		// should cover all mines
 		MapLocation[] allNeutralMineLocs = rc.senseMineLocations(
 				new MapLocation(mapWidth / 2, mapHeight / 2), 2000,
 				Team.NEUTRAL);
 
 		for (int i = 0; i < allEncampmentLocs.length; i++) {
 			encampmentMap[allEncampmentLocs[i].x][allEncampmentLocs[i].y] = 1;
 		}
 
 		for (int i = 0; i < allNeutralMineLocs.length; i++) {
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
 
 		System.out.println("num encampments between hqs: "
 				+ numEncampmentsBetweenHQs);
 		System.out.println("num neutral mines between hqs: "
 				+ numNeutralMinesBetweenHQs);
 
 		// start out building suppliers and generators in encampments far-ish
 		// from base
 		// defense strategy: build shields(25%), medbays(25%), artillery(50%),
 		// capture neutral and enemy medbays near base, build nuke
 		// offense strategy: build artillery near their hq,
 		// build shields if they have artillery near base,
 		// attacking units should recharge at nearby shields and medbay
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
 
 	public void zoneEncampments(MapLocation[] encampmentLocs) {
 		int zone1Counter = 0;
 		int zone2Counter = 0;
 		int zone3Counter = 0;
 		int zone4Counter = 0;
 		for (MapLocation encampment : encampmentLocs) {
 			int distToMyBase = encampment.distanceSquaredTo(myBaseLoc);
 
 			// skip encampments within 2 of base so we don't trap ourselves in
 			if (distToMyBase <= 4) {
 				continue;
 			}
 			int distToEnemyBase = encampment.distanceSquaredTo(enemyBaseLoc);
 			// zone1
 			if (distToMyBase <= closeToBase
 					&& distToEnemyBase <= (distBetweenBases + awayFromEnemyForgiveness)) {
 				if (zone1Counter < ZONE_ENCAMPMENT_LIMIT) {
 					zone1Locs[zone1Counter] = encampment;
 					zone1Counter += 1;
 				}
 			}
 			// zone2
 
			else if (distToMyBase <= (distBetweenBases / 2 + awayFromEquidistantForgiveness)
					&& distToEnemyBase <= (distBetweenBases / 2 + awayFromEquidistantForgiveness)) {
 				if (zone2Counter < ZONE_ENCAMPMENT_LIMIT) {
 					zone2Locs[zone2Counter] = encampment;
 					zone2Counter += 1;
 				}
 			}
 			// zone3
 			else if (distToEnemyBase <= closeToEnemy
 					&& distToMyBase <= distBetweenBases) {
 				if (zone3Counter < ZONE_ENCAMPMENT_LIMIT) {
 					zone3Locs[zone3Counter] = encampment;
 					zone3Counter += 1;
 				}
 			}
 			// zone 4
 			else if (distToEnemyBase >= farEnoughFromEnemy) {
 				if (zone4Counter < ZONE_ENCAMPMENT_LIMIT) {
 					zone4Locs[zone4Counter] = encampment;
 					zone4Counter += 1;
 				}
 			}
 		}
 		endZone1Index = zone1Counter - 1;
 		endZone2Index = zone2Counter - 1;
 		endZone3Index = zone3Counter - 1;
 		endZone4Index = zone4Counter - 1;
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
 		// zone1 is the zone near our base, probably only one robot per squad
 		// needed
 		@Override
 		public int compare(Object Enc0, Object Enc1) {
 			if (Enc0 == null) {
 				return 1;
 			}
 			if (Enc1 == null) {
 				return -1;
 			}
 			return (((MapLocation) Enc0).distanceSquaredTo(myBaseLoc) - ((MapLocation) Enc1)
 					.distanceSquaredTo(myBaseLoc));
 		}
 	}
 
 	public class EncampmentComparatorZone2 implements Comparator {
 		// zone2 is the zone near the center of the map, may need medium sized
 		// squads and quick action to take these
 		@Override
 		public int compare(Object Enc0, Object Enc1) {
 			if (Enc0 == null) {
 				return 1;
 			}
 			if (Enc1 == null) {
 				return -1;
 			}
 			return ((((MapLocation) Enc0).distanceSquaredTo(myBaseLoc) + ((MapLocation) Enc0)
 					.distanceSquaredTo(enemyBaseLoc)) - (((MapLocation) Enc1)
 					.distanceSquaredTo(myBaseLoc) + ((MapLocation) Enc1)
 					.distanceSquaredTo(enemyBaseLoc)));
 		}
 	}
 
 	public class EncampmentComparatorZone3 implements Comparator {
 		// zone3 is the zone near the enemy base we would want to put artillery,
 		// this should be taken when we are already pushing into the enemy
 		// lines, but probably not done immediately
 		@Override
 		public int compare(Object Enc0, Object Enc1) {
 			if (Enc0 == null) {
 				return 1;
 			}
 			if (Enc1 == null) {
 				return -1;
 			}
 			return ((((MapLocation) Enc0).distanceSquaredTo(enemyBaseLoc) * 2 + ((MapLocation) Enc0)
 					.distanceSquaredTo(myBaseLoc)) - (((MapLocation) Enc1)
 					.distanceSquaredTo(enemyBaseLoc) * 2 + ((MapLocation) Enc1)
 					.distanceSquaredTo(myBaseLoc)));
 		}
 	}
 
 	public class EncampmentComparatorZone4 implements Comparator {
 		// zone4 is the "zone" of far away encampments that aren't near the
 		// enemy base
 		@Override
 		public int compare(Object Enc0, Object Enc1) {
 			if (Enc0 == null) {
 				return 1;
 			}
 			if (Enc1 == null) {
 				return -1;
 			}
 			return (((MapLocation) Enc0).distanceSquaredTo(myBaseLoc) - ((MapLocation) Enc1)
 					.distanceSquaredTo(myBaseLoc));
 		}
 	}
 }
