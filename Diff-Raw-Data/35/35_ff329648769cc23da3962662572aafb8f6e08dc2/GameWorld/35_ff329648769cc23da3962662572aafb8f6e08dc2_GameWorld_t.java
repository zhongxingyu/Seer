 package battlecode.world;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameActionExceptionType;
 import battlecode.common.GameConstants;
 import battlecode.common.MapLocation;
 import battlecode.common.Message;
 import battlecode.common.RobotLevel;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 import battlecode.common.TerrainTile;
 import battlecode.engine.ErrorReporter;
 import battlecode.engine.GenericWorld;
 import battlecode.engine.instrumenter.RobotDeathException;
 import battlecode.engine.instrumenter.RobotMonitor;
 import battlecode.engine.signal.*;
 import battlecode.serial.DominationFactor;
 import battlecode.serial.GameStats;
 import battlecode.serial.RoundStats;
 import battlecode.world.signal.*;
 
 /**
  * The primary implementation of the GameWorld interface for
  * containing and modifying the game map and the objects on it.
  */
 /*
 oODO:
 - comments
 - move methods from RCimpl to here, add signalhandler methods
  */
 public class GameWorld extends BaseWorld<InternalObject> implements GenericWorld {
 
     private final GameMap gameMap;
     private RoundStats roundStats = null;	// stats for each round; new object is created for each round
     private final GameStats gameStats = new GameStats();		// end-of-game stats
     private double[] teamRoundResources = new double[2];
     private double[] lastRoundResources = new double[2];
     private final Map<MapLocation3D, InternalObject> gameObjectsByLoc = new HashMap<MapLocation3D, InternalObject>();
     private double[] teamResources = new double[]{GameConstants.INITIAL_FLUX, GameConstants.INITIAL_FLUX};
 
     private Map<MapLocation, ArrayList<MapLocation>> powerNodeGraph = new HashMap<MapLocation, ArrayList<MapLocation>>();
     private List<InternalPowerNode> powerNodes = new ArrayList<InternalPowerNode>();
 	private Map<Team,InternalPowerNode> baseNodes = new EnumMap<Team,InternalPowerNode>(Team.class);
 	private Map<Team,List<InternalPowerNode>> connectedNodesByTeam = new EnumMap<Team,List<InternalPowerNode>>(Team.class);
 	private Map<Team,List<InternalPowerNode>> adjacentNodesByTeam = new EnumMap<Team,List<InternalPowerNode>>(Team.class);
 
 	// robots to remove from the game at end of turn
 	private List<InternalRobot> deadRobots = new ArrayList<InternalRobot>();
 
 	private Map<Team,List<InternalRobot>> archons;
 
     @SuppressWarnings("unchecked")
     public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldArchonMemory) {
         super(gm.getSeed(), teamA, teamB, oldArchonMemory);
         gameMap = gm;
 		archons = new EnumMap<Team,List<InternalRobot>>(Team.class);
 		archons.put(Team.A,new ArrayList<InternalRobot>());
 		archons.put(Team.B,new ArrayList<InternalRobot>());
 		connectedNodesByTeam.put(Team.A,new ArrayList<InternalPowerNode>());
 		connectedNodesByTeam.put(Team.B,new ArrayList<InternalPowerNode>());
 		adjacentNodesByTeam.put(Team.A,new ArrayList<InternalPowerNode>());
 		adjacentNodesByTeam.put(Team.B,new ArrayList<InternalPowerNode>());
     }
 
     public int getMapSeed() {
         return gameMap.getSeed();
     }
 
     public GameMap getGameMap() {
         return gameMap;
     }
 
     public void processBeginningOfRound() {
         currentRound++;
 
         wasBreakpointHit = false;
 
         // process all gameobjects
         InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
         gameObjects = gameObjectsByID.values().toArray(gameObjects);
         for (int i = 0; i < gameObjects.length; i++) {
             gameObjects[i].processBeginningOfRound();
         }
 
     }
 
     public void processEndOfRound() {
         // process all gameobjects
         InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
         gameObjects = gameObjectsByID.values().toArray(gameObjects);
         for (int i = 0; i < gameObjects.length; i++) {
             gameObjects[i].processEndOfRound();
         }
 		
 		if(timeLimitReached()) {
 			// copy the node lists, because the damage could kill a node and disconnect the graph
 			List<InternalPowerNode> teamANodes = new ArrayList<InternalPowerNode>(connectedNodesByTeam.get(Team.A));
 			List<InternalPowerNode> teamBNodes = new ArrayList<InternalPowerNode>(connectedNodesByTeam.get(Team.B));
 			for(InternalPowerNode n : teamANodes)
 				n.takeDamage(GameConstants.TIME_LIMIT_DAMAGE/teamANodes.size());
 			for(InternalPowerNode n : teamBNodes)
 				n.takeDamage(GameConstants.TIME_LIMIT_DAMAGE/teamBNodes.size());
 			// TODO: find a more fair way to break ties if both teams die to the time limit damage
 			// in the same round?
 		}
 
         long aPoints = Math.round(teamRoundResources[Team.A.ordinal()] * 100), bPoints = Math.round(teamRoundResources[Team.B.ordinal()] * 100);
 
         roundStats = new RoundStats(teamResources[0] * 100, teamResources[1] * 100, teamRoundResources[0] * 100, teamRoundResources[1] * 100);
         lastRoundResources = teamRoundResources;
         teamRoundResources = new double[2];
 
     }
 
 	public void setWinner(Team t) {
 		winner = t;
 		if(archons.get(winner).size()>=GameConstants.NUMBER_OF_ARCHONS)
 			gameStats.setDominationFactor(DominationFactor.DESTROYED);
 		else if(!timeLimitReached())
 			gameStats.setDominationFactor(DominationFactor.OWNED);
 		else
 			gameStats.setDominationFactor(DominationFactor.BEAT);
         running = false;
 
         for (InternalObject o : gameObjectsByID.values()) {
             if (o instanceof InternalRobot)
                 RobotMonitor.killRobot(o.getID());
 		}
 	}
 
 	public void teamChanged(InternalPowerNode p, Team oldTeam, Team newTeam) {
 		recomputeConnections();
 		addSignal(new NodeCaptureSignal(p,newTeam));
 		if(baseNodes.get(oldTeam)==p) {
 			if(winner==null) {
 				setWinner(oldTeam.opponent());
 			}
 		}
 	}
 
 	private class Connections {
 
 		private Set<InternalPowerNode> visited = new HashSet<InternalPowerNode>();
 		private Deque<InternalPowerNode> queue = new ArrayDeque<InternalPowerNode>();
 		private Team team;
 
 		public Connections(Team t) {
 			team = t;
 			add(baseNodes.get(t));
 		}
 
 		public void add(InternalPowerNode n) {
 			if(!visited.contains(n)) {
 				visited.add(n);
 				n.setConnected(team,true);
 				if(n.getTeam()==team) {
 					connectedNodesByTeam.get(team).add(n);
 					queue.push(n);
 				}
 				else
 					adjacentNodesByTeam.get(team).add(n);
 			}
 		}
 
 		public void findAll() {
 			while(!queue.isEmpty()) {
 				InternalPowerNode n = queue.pop();
 				for(MapLocation l : powerNodeGraph.get(n.getLocation())) {
 					add((InternalPowerNode)gameObjectsByLoc.get(new MapLocation3D(l, RobotType.POWER_NODE.level)));
 				}
 			}
 		}
 
 	}
 
 	public void recomputeConnections() {
 		for(InternalPowerNode n: powerNodes) {
 			n.setConnected(Team.A,false);
 			n.setConnected(Team.B,false);
 		}
 		connectedNodesByTeam.get(Team.A).clear();
 		connectedNodesByTeam.get(Team.B).clear();
 		adjacentNodesByTeam.get(Team.A).clear();
 		adjacentNodesByTeam.get(Team.B).clear();
 		new Connections(Team.A).findAll();
 		new Connections(Team.B).findAll();
 	}
 
 	public int getConnectedNodeCount(Team t) {
 		return connectedNodesByTeam.get(t).size();
 	}
 
 	public boolean timeLimitReached() {
 		return currentRound >= gameMap.getMaxRounds()-1;
 	}
 
     public double[] getLastRoundResources() {
         return lastRoundResources;
     }
 
     public InternalObject getObject(MapLocation loc, RobotLevel level) {
         return gameObjectsByLoc.get(new MapLocation3D(loc, level));
     }
 
     public <T extends InternalObject> T getObjectOfType(MapLocation loc, RobotLevel level, Class<T> cl) {
         InternalObject o = getObject(loc, level);
         if (cl.isInstance(o))
             return cl.cast(o);
         else
             return null;
     }
 
     public InternalRobot getRobot(MapLocation loc, RobotLevel level) {
         InternalObject obj = getObject(loc, level);
         if (obj instanceof InternalRobot)
             return (InternalRobot) obj;
         else
             return null;
     }
 
     // should only be called by the InternalObject constructor
     public void notifyAddingNewObject(InternalObject o) {
         if (gameObjectsByID.containsKey(o.getID()))
             return;
         gameObjectsByID.put(o.getID(), o);
         if (o.getLocation() != null) {
             gameObjectsByLoc.put(new MapLocation3D(o.getLocation(), o.getRobotLevel()), o);
         }
 		if (o instanceof InternalPowerNode) {
 			addPowerNode((InternalPowerNode)o);
 		}
     }
 
 	public void addPowerNode(InternalPowerNode p) {
 		powerNodes.add(p);
 		if(p.getTeam()!=Team.NEUTRAL)
 			baseNodes.put(p.getTeam(),p);
 	}
 
 	public void addArchon(InternalRobot r) {
 		archons.get(r.getTeam()).add(r);
 	}
 
     public Collection<InternalObject> allObjects() {
         return gameObjectsByID.values();
     }
 
     // TODO: move stuff to here
     // should only be called by InternalObject.setLocation
     public void notifyMovingObject(InternalObject o, MapLocation oldLoc, MapLocation newLoc) {
         if (oldLoc != null) {
             MapLocation3D oldLoc3D = new MapLocation3D(oldLoc, o.getRobotLevel());
             if (gameObjectsByLoc.get(oldLoc3D) != o) {
                 ErrorReporter.report("Internal Error: invalid oldLoc in notifyMovingObject");
                 return;
             }
             gameObjectsByLoc.remove(oldLoc3D);
         }
         if (newLoc != null) {
             gameObjectsByLoc.put(new MapLocation3D(newLoc, o.getRobotLevel()), o);
         }
     }
 
     public void removeObject(InternalObject o) {
         if (o.getLocation() != null) {
             MapLocation3D loc3D = new MapLocation3D(o.getLocation(), o.getRobotLevel());
             if (gameObjectsByLoc.get(loc3D) == o)
                 gameObjectsByLoc.remove(loc3D);
             else
                 System.out.println("Couldn't remove " + o + " from the game");
         } else
             System.out.println("Couldn't remove " + o + " from the game");
 
         if (gameObjectsByID.get(o.getID()) == o)
             gameObjectsByID.remove(o.getID());
 
         if (o instanceof InternalRobot) {
             InternalRobot r = (InternalRobot) o;
             r.freeMemory();
         }
     }
 
     public boolean exists(InternalObject o) {
         return gameObjectsByID.containsKey(o.getID());
     }
 
     /**
      *@return the TerrainType at a given MapLocation <tt>loc<tt>
      */
     public TerrainTile getMapTerrain(MapLocation loc) {
         return gameMap.getTerrainTile(loc);
     }
 
     // TODO: optimize this too
     public int getUnitCount(Team team) {
         int result = 0;
         for (InternalObject o : gameObjectsByID.values()) {
             if (!(o instanceof InternalRobot))
                 continue;
             if (((InternalRobot) o).getTeam() == team)
                 result++;
         }
 
         return result;
     }
 
	public MapLocation [] getArchons(InternalRobot robot) {
		List<InternalRobot> allies = archons.get(robot.getTeam());
		MapLocation [] locs = new MapLocation [allies.size()-1];
		int j=-1;
		for(InternalRobot r : allies) {
			if(r!=robot)
				locs[++j]=r.getLocation();
		}
		return locs;
 	}
 
     public double getPoints(Team team) {
         return teamRoundResources[team.ordinal()];
     }
 
     public boolean canMove(RobotLevel level, MapLocation loc) {
 
         return gameMap.getTerrainTile(loc).isTraversableAtHeight(level) && (gameObjectsByLoc.get(new MapLocation3D(loc, level)) == null);
     }
 
     public void splashDamageGround(MapLocation loc, double damage, double falloutFraction) {
         //TODO: optimize this
         InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(loc, 2, -1);
         for (InternalRobot r : robots) {
             if (r.getRobotLevel() == RobotLevel.ON_GROUND) {
                 if (r.getLocation().equals(loc))
                     r.changeEnergonLevelFromAttack(-damage);
                 else
                     r.changeEnergonLevelFromAttack(-damage * falloutFraction);
             }
         }
     }
 
     public InternalObject[] getAllGameObjects() {
         return gameObjectsByID.values().toArray(new InternalObject[gameObjectsByID.size()]);
     }
 
     public InternalRobot getRobotByID(int id) {
         return (InternalRobot) getObjectByID(id);
     }
 
     public Signal[] getAllSignals(boolean includeBytecodesUsedSignal) {
         ArrayList<InternalRobot> energonChangedRobots = new ArrayList<InternalRobot>();
 		ArrayList<InternalRobot> fluxChangedRobots = new ArrayList<InternalRobot>();
         ArrayList<InternalRobot> allRobots = null;
         if (includeBytecodesUsedSignal)
             allRobots = new ArrayList<InternalRobot>();
         for (InternalObject obj : gameObjectsByID.values()) {
             if (!(obj instanceof InternalRobot))
                 continue;
             InternalRobot r = (InternalRobot) obj;
             if (includeBytecodesUsedSignal)
                 allRobots.add(r);
             if (r.clearEnergonChanged()) {
                 energonChangedRobots.add(r);
             }
 			if (r.clearFluxChanged()) {
 				fluxChangedRobots.add(r);
 			}
         }
         signals.add(new EnergonChangeSignal(energonChangedRobots.toArray(new InternalRobot[]{})));
 		signals.add(new FluxChangeSignal(fluxChangedRobots.toArray(new InternalRobot[]{})));
 
         if (includeBytecodesUsedSignal)
             signals.add(new BytecodesUsedSignal(allRobots.toArray(new InternalRobot[]{})));
         return signals.toArray(new Signal[signals.size()]);
     }
 
     public RoundStats getRoundStats() {
         return roundStats;
     }
 
     public GameStats getGameStats() {
         return gameStats;
     }
 
     public void beginningOfExecution(int robotID) {
         InternalRobot r = (InternalRobot) getObjectByID(robotID);
         if (r != null)
             r.processBeginningOfTurn();
     }
 
     public void endOfExecution(int robotID) {
         InternalRobot r = (InternalRobot) getObjectByID(robotID);
         // if the robot is dead, it won't be in the map any more
         if (r != null) {
             r.setBytecodesUsed(RobotMonitor.getBytecodesUsed());
             r.processEndOfTurn();
         }
     }
 
     public void resetStatic() {
     }
 
     public void createNodeLink(MapLocation id1, MapLocation id2)
     {
     	if(!this.powerNodeGraph.containsKey(id1))
     		this.powerNodeGraph.put(id1, new ArrayList<MapLocation>());
 		this.powerNodeGraph.get(id1).add(id2);
 
     	if(!this.powerNodeGraph.containsKey(id2))
     		this.powerNodeGraph.put(id2, new ArrayList<MapLocation>());
 		this.powerNodeGraph.get(id2).add(id1);
     }
 
 	public ArrayList<MapLocation> getAdjacentNodes(MapLocation loc) {
 		return powerNodeGraph.get(loc);
 	}
 
 	public Iterable<InternalPowerNode> getPowerNodesByTeam(Team t) {
 		return Iterables.filter(powerNodes,Util.isAllied(t));
 	}
 
 	public List<InternalPowerNode> getCapturableNodes(Team t) {
 		return adjacentNodesByTeam.get(t);
 	}
         
 	public void notifyDied(InternalRobot r) {
 		if(r==RobotMonitor.getCurrentRobot())
 			throw new RobotDeathException();
 		else
 			deadRobots.add(r);
 	}
 
 	public void removeDead() {
 		for(InternalRobot r : deadRobots) {
 			visitSignal(new DeathSignal(r));
 		}
 		deadRobots.clear();
 	}
 
     // ******************************
     // SIGNAL HANDLER METHODS
     // ******************************
     SignalHandler signalHandler = new AutoSignalHandler(this);
 
     public void visitSignal(Signal s) {
         signalHandler.visitSignal(s);
     }
 
     public void visitAttackSignal(AttackSignal s) {
         InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());
 		if(attacker.type==RobotType.SCORCHER) {
 			for(InternalObject o : gameObjectsByID.values()) {
 				if(attacker.type.canAttack(o.getRobotLevel())&&canAttackSquare(attacker,o.getLocation())&&(o instanceof InternalRobot)) {
 					InternalRobot target = (InternalRobot)o;
 					target.takeDamage(attacker.type.attackPower);
 				}
 			}
 		}
 		else {
         	MapLocation targetLoc = s.getTargetLoc();
         	RobotLevel level = s.getTargetHeight();
         	InternalRobot target = getRobot(targetLoc, level);
 
         	if (target != null) {
 				switch(attacker.type) {
 				case SCOUT:
 					double drain = Math.min(attacker.type.attackPower,target.getFlux());
 					target.adjustFlux(-drain);
 					attacker.adjustFlux(drain);
 					break;
 				case DISRUPTER:
             		target.takeDamage(attacker.type.attackPower);
 					target.delayAttack(GameConstants.DISRUPTER_DELAY);
 					break;
 				default:
             		target.takeDamage(attacker.type.attackPower);
 				}
         	}
 		}
 
         addSignal(s);
     }
 
     public void visitBroadcastSignal(BroadcastSignal s) {
         InternalObject sender = gameObjectsByID.get(s.robotID);
         Collection<InternalObject> objs = gameObjectsByLoc.values();
         Predicate<InternalObject> pred = Util.robotWithinDistance(sender.getLocation(), s.range);
         for (InternalObject o : Iterables.filter(objs, pred)) {
             InternalRobot r = (InternalRobot) o;
             if (r != sender)
                 r.enqueueIncomingMessage((Message) s.message.clone());
         }
         s.message = null;
 
         addSignal(s);
     }
 
     public void visitDeathSignal(DeathSignal s) {
         if (!running) {
             // All robots emit death signals after the game
             // ends.  We still want the client to draw
             // the robots.
             return;
         }
         int ID = s.getObjectID();
         InternalObject obj = getObjectByID(ID);
 
         if (obj instanceof InternalRobot) {
             InternalRobot r = (InternalRobot) obj;
             RobotMonitor.killRobot(ID);
             if (r.hasBeenAttacked()) {
                 gameStats.setUnitKilled(r.getTeam(), currentRound);
             }
         }
         if (obj != null) {
             removeObject(obj);
             addSignal(s);
         }
     }
 
     public void visitEnergonChangeSignal(EnergonChangeSignal s) {
         int[] robotIDs = s.getRobotIDs();
         double[] energon = s.getEnergon();
         for (int i = 0; i < robotIDs.length; i++) {
             InternalRobot r = (InternalRobot) getObjectByID(robotIDs[i]);
             System.out.println("el " + energon[i] + " " + r.getEnergonLevel());
             r.changeEnergonLevel(energon[i] - r.getEnergonLevel());
         }
     }
 
     public void visitIndicatorStringSignal(IndicatorStringSignal s) {
     	addSignal(s);
     }
 
     public void visitMatchObservationSignal(MatchObservationSignal s) {
         addSignal(s);
     }
 
     public void visitControlBitsSignal(ControlBitsSignal s) {
         InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
         r.setControlBits(s.getControlBits());
 
         addSignal(s);
     }
 
     public void visitMovementOverrideSignal(MovementOverrideSignal s) {
         InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
         if (!canMove(r.getRobotLevel(), s.getNewLoc()))
             throw new RuntimeException("GameActionException in MovementOverrideSignal",new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move to location: " + s.getNewLoc()));
         r.setLocation(s.getNewLoc());
         addSignal(s);
     }
 
     public void visitMovementSignal(MovementSignal s) {
         InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
         MapLocation loc = s.getNewLoc();//(s.isMovingForward() ? r.getLocation().add(r.getDirection()) : r.getLocation().add(r.getDirection().opposite()));
 
         r.setLocation(loc);
 
         addSignal(s);
     }
 
     public void visitSetDirectionSignal(SetDirectionSignal s) {
         InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
         Direction dir = s.getDirection();
 
         r.setDirection(dir);
 
         addSignal(s);
     }
 
 	@SuppressWarnings("unchecked")
     public void visitSpawnSignal(SpawnSignal s) {
         InternalRobot parent;
         int parentID = s.getParentID();
         MapLocation loc;
         if (parentID == 0) {
             parent = null;
             loc = s.getLoc();
         } else {
             parent = (InternalRobot) getObjectByID(parentID);
             loc = s.getLoc();
         }
 
         //note: this also adds the signal
         InternalRobot robot = GameWorldFactory.createPlayer(this, s.getType(), loc, s.getTeam(), parent);
     }
 
     // *****************************
     //    UTILITY METHODS
     // *****************************
     private static MapLocation origin = new MapLocation(0, 0);
 
 	protected static boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
 		MapLocation myLoc = ir.getLocation();
 		int d = myLoc.distanceSquaredTo(loc);
 		return d<=ir.type.attackRadiusMaxSquared && d>= ir.type.attackRadiusMinSquared
 			&& inAngleRange(myLoc,ir.getDirection(),loc,ir.type.attackCosHalfTheta);
 	}
 
     protected static boolean inAngleRange(MapLocation sensor, Direction dir, MapLocation target, double cosHalfTheta) {
         MapLocation dirVec = origin.add(dir);
         double dx = target.getX() - sensor.getX();
         double dy = target.getY() - sensor.getY();
         int a = dirVec.getX();
         int b = dirVec.getY();
         double dotProduct = a * dx + b * dy;
 
         if (dotProduct < 0) {
             if (cosHalfTheta > 0)
                 return false;
         } else if (cosHalfTheta < 0)
             return true;
 
         double rhs = cosHalfTheta * cosHalfTheta * (dx * dx + dy * dy) * (a * a + b * b);
 
         if (dotProduct < 0)
             return (dotProduct * dotProduct <= rhs + 0.00001d);
         else
             return (dotProduct * dotProduct >= rhs - 0.00001d);
     }
 
     // TODO: make a faster implementation of this
     protected InternalRobot[] getAllRobotsWithinRadiusDonutSq(MapLocation center, int outerRadiusSquared, int innerRadiusSquared) {
         ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();
 
         for (InternalObject o : gameObjectsByID.values()) {
             if (!(o instanceof InternalRobot))
                 continue;
             if (o.getLocation() != null && o.getLocation().distanceSquaredTo(center) <= outerRadiusSquared
                     && o.getLocation().distanceSquaredTo(center) > innerRadiusSquared)
                 robots.add((InternalRobot) o);
         }
 
         return robots.toArray(new InternalRobot[robots.size()]);
     }
 
     // TODO: make a faster implementation of this
     public MapLocation[] getAllMapLocationsWithinRadiusSq(MapLocation center, int radiusSquared) {
         ArrayList<MapLocation> locations = new ArrayList<MapLocation>();
 
         int radius = (int) Math.sqrt(radiusSquared);
 
         int minXPos = center.getX() - radius;
         int maxXPos = center.getX() + radius;
         int minYPos = center.getY() - radius;
         int maxYPos = center.getY() + radius;
 
         for (int x = minXPos; x <= maxXPos; x++) {
             for (int y = minYPos; y <= maxYPos; y++) {
                 MapLocation loc = new MapLocation(x, y);
                 TerrainTile tile = gameMap.getTerrainTile(loc);
                 if (!tile.equals(TerrainTile.OFF_MAP) && loc.distanceSquaredTo(center) < radiusSquared)
                     locations.add(loc);
             }
         }
 
         return locations.toArray(new MapLocation[locations.size()]);
     }
 
     public double resources(Team t) {
         return teamResources[t.ordinal()];
     }
 
     protected boolean spendResources(Team t, double amount) {
         if (teamResources[t.ordinal()] >= amount) {
             teamResources[t.ordinal()] -= amount;
             return true;
         } else
             return false;
     }
 
     protected void adjustResources(Team t, double amount) {
         if (amount >= GameConstants.MINE_DEPLETED_RESOURCES)
             teamRoundResources[t.ordinal()] += amount;
         teamResources[t.ordinal()] += amount;
     }
 }
