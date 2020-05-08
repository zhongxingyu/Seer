 package team216;
 
 import battlecode.common.*;
 
 public class Soldier
 {
     private static final int GL_RADIUS = 70 * 70;
     private static final int LC_RADIUS =
         RobotType.ARTILLERY.attackRadiusMaxSquared;
 
     private static final int MAX_MINES  = 10;
     private static final int MAX_ROBOTS = 15;
     private static final int MAX_BASES = 5;
     private static final double MAX_SHIELD = 40;
 
     private static void strengthen(
             double[] strength, Direction dir, double force)
     {
         if (dir == Direction.OMNI) return;
 
         int ord = dir.ordinal();
         final int mask = 8 - 1;
 
         strength[ord] += force;
 
         double dropoff = 1.0;
 
         for (int i = 1; i < 4 && dropoff > 0.0; ++i) {
             dropoff -= Weights.DROPOFF;
             strength[(ord - i) & mask] += force * dropoff;
             strength[(ord + i) & mask] += force * dropoff;
         }
     }
 
     private static void strengthen(
             double[] strength, Direction dir, double weight, double dist)
     {
         strengthen(strength, dir, weight * (1 / dist));
     }
 
     private static void mines(
             RobotController rc, MapLocation coord, double strength[],
             double w, int radius)
         throws GameActionException
     {
         MapLocation mines[] = rc.senseNonAlliedMineLocations(coord, radius);
         int steps = Math.max(1, Utils.ceilDiv(mines.length, MAX_MINES));
         int count = 0;
 
         for (int i = 0; i < mines.length; i += steps) {
             Direction dir = coord.directionTo(mines[i]);
             strengthen(strength, dir, w, coord.distanceSquaredTo(mines[i]));
             count++;
         }
 
         rc.setIndicatorString(1, "mines=" + count + "/" + mines.length);
     }
 
     /**
      */
     private static void battleFormation(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         // TODO: finish this method
 
         // when enemies are nearby, group into a tight formation
 
        Robot robots[] = rc.senseNearbyGameObjects(Robot.class, 3, team);
         Robot enemyRobots[] = rc.senseNearbyGameObjects(
                 Robot.class, LC_RADIUS, team.opponent());
 
         MapLocation closestEnemy = findClosest(rc, enemyRobots);
         Direction toward = coord.directionTo(closestEnemy);
 
         // check if we're already in formation
         boolean grouped = false;
         GameObject oneLeft = rc.senseObjectAtLocation(coord.add(leftOf(toward)));
         if (oneLeft != null) {
             if (oneLeft.getTeam().equals(team))
                 grouped = true;
         }
         GameObject oneRight = rc.senseObjectAtLocation(coord.add(rightOf(toward)));
         if (oneRight != null) {
             if (oneRight.getTeam().equals(team))
                 grouped = true;
         }
 
         // if we're already grouped, attack!
         if (grouped)
             strengthen(strength, toward, Weights.GROUP_ATTACK);
         // otherwise, group up
         else {
             MapLocation closestAlly = findClosest(rc, robots);
            strengthen(strength, toward, Weights.GROUP_UP);
         }
     }
 
     public static Direction rightOf(Direction from){
         return from.rotateRight().rotateRight();
     }
     public static Direction leftOf(Direction from){
         return from.rotateLeft().rotateLeft();
     }
 
     private static MapLocation findClosest(
             RobotController rc, Robot[] otherRobots)
         throws GameActionException {
             // Be careful of bytecode problems, as this method is unbounded
         int closestDist = Integer.MAX_VALUE;
         MapLocation closestEnemy = null;
 
         int steps = Utils.ceilDiv(otherRobots.length, MAX_ROBOTS);
 
         for (int i = 0; i < otherRobots.length; i += steps) {
             Robot robot = otherRobots[i];
             RobotInfo robotInfo = rc.senseRobotInfo(robot);
             int dist = robotInfo.location.distanceSquaredTo(rc.getLocation());
 
             if (dist < closestDist) {
                 closestDist = dist;
                 closestEnemy = robotInfo.location;
             }
         }
 
         return closestEnemy;
     }
 
     /**
      */
     private static void globalRobots(
             RobotController rc, MapLocation coord,
             double strength[], double w, Team team)
         throws GameActionException
     {
         Robot robots[] = rc.senseNearbyGameObjects(
                 Robot.class, GL_RADIUS, team);
 
         int steps = Utils.ceilDiv(robots.length, MAX_ROBOTS);
 
         for (int i = 0; i < robots.length; i += steps) {
             MapLocation robotCoord = rc.senseRobotInfo(robots[i]).location;
             Direction dir = coord.directionTo(robotCoord);
 
             strengthen(strength, dir, w, coord.distanceSquaredTo(robotCoord));
         }
 
         rc.setIndicatorString(0, "global=" + robots.length);
     }
 
     /**
      */
     private static boolean localRobots(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         // if there is an adjacent enemy, don't run away
         if (rc.senseNearbyGameObjects(Robot.class, 1, team.opponent()).length > 0)
             return true;
 
         Robot enemies[] = rc.senseNearbyGameObjects(
                 Robot.class, LC_RADIUS, team.opponent());
 
         if (enemies.length == 0) return false;
 
         int x = 0, y = 0;
         int numEnemies = 0;
         double enemyForce = 0.0;
         int steps = Utils.ceilDiv(enemies.length, MAX_ROBOTS);
 
         for (int i = 0; i < enemies.length; i += steps) {
             RobotInfo info = rc.senseRobotInfo(enemies[i]);
             if (info.type != RobotType.SOLDIER &&
                     info.type != RobotType.ARTILLERY &&
                     info.type != RobotType.MEDBAY)
             {
                 continue;
             }
 
             x += info.location.x;
             y += info.location.y;
             numEnemies++;
             enemyForce += info.energon/40;
         }
 
         if (numEnemies == 0) return false;
 
         MapLocation enemyCenter = new MapLocation(x/numEnemies, y/numEnemies);
 
         Robot allies[] = rc.senseNearbyGameObjects(Robot.class, LC_RADIUS, team);
         steps = Utils.ceilDiv(enemies.length, MAX_ROBOTS);
         int numAllies = 0;
         double allyForce = 0.0;
 
         for (int i = 0; i < allies.length; i += steps) {
             RobotInfo info = rc.senseRobotInfo(allies[i]);
             if (info.type != RobotType.SOLDIER &&
                     info.type != RobotType.ARTILLERY &&
                     info.type != RobotType.MEDBAY)
             {
                 continue;
             }
             numAllies++;
             allyForce += info.energon/40;
         }
 
         double force = (
                 allyForce * Weights.LC_ALLY_SD -
                 enemyForce * Weights.LC_ENEMY_SD) *
             Weights.LC_MUL;
 
         rc.setIndicatorString(0,
                 "enemies=" + numEnemies + "/" + enemies.length +
                 ", allies=" + numAllies + "/" + allies.length +
                 ", enemyCenter=" + enemyCenter.toString());
 
         Direction chargeDir = coord.directionTo(enemyCenter);
         strengthen(strength, chargeDir, force);
 
         return true;
     }
 
 
     /**
      */
     private static void neutralBases(
             RobotController rc, MapLocation coord, double strength[])
         throws GameActionException
     {
         // TODO : if there are no free encampments on the map, return
         // TODO : this is all bytecode-heavy and can be greatly alleviated
             // by Storage.
 
         double cost = rc.senseCaptureCost();
         if (cost >= rc.getTeamPower()) return;
 
         MapLocation bases[] =
             rc.senseEncampmentSquares(coord, LC_RADIUS, Team.NEUTRAL);
 
         int radius = LC_RADIUS;
         while (bases.length < 1 && radius < GL_RADIUS) {
             radius *= 3;
             bases = rc.senseEncampmentSquares(coord, radius, Team.NEUTRAL);
         }
 
         int steps = Utils.ceilDiv(bases.length, MAX_BASES);
         int count = 0, taken = 0;
 
         for (int i = 0; i < bases.length; i += steps) {
             if (rc.canSenseSquare(bases[i]) &&
                     rc.senseObjectAtLocation(bases[i]) != null)
             {
                 taken++;
                 continue;
             }
             // TODO
             // if (rc.canSenseSquare(bases[i].add(Direction.NORTH)) != null)
             //     ???
             // if (rc.canSenseSquare(bases[i].add(Direction.SOUTH)) != null)
             //     ???
             // if (rc.canSenseSquare(bases[i].add(Direction.EAST)) != null)
             //     ???
             // if (rc.canSenseSquare(bases[i].add(Direction.WEST)) != null)
             //     ???
 
             Direction dir = coord.directionTo(bases[i]);
             strengthen(
                     strength, dir, Weights.CAPTURE,
                     coord.distanceSquaredTo(bases[i]));
 
             count++;
         }
 
         rc.setIndicatorString(2, "neutral=" + taken + "/" + count + ", cost=" + cost);
     }
 
 
     private static void allyBases(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         double energon = rc.getEnergon();
         double shield = rc.getShields();
 
         MapLocation bases[] = rc.senseEncampmentSquares(coord, LC_RADIUS, team);
         int steps = Math.max(1, Utils.ceilDiv(bases.length, MAX_BASES));
 
         int count = 0;
         double shields = 0;
         double med = 0;
 
         for (int i = 0; i < bases.length; i += steps) {
             Robot base = (Robot) rc.senseObjectAtLocation(bases[i]);
             RobotInfo info = rc.senseRobotInfo(base);
 
             double force;
             count++;
 
             if (info.type == RobotType.MEDBAY) {
                 force = ((RobotType.SOLDIER.maxEnergon - energon) /
                         RobotType.SOLDIER.maxEnergon) * Weights.HEAL;
                 med += force;
             }
 
             else if (info.type == RobotType.SHIELDS) {
                 force = (MAX_SHIELD - shield) * Weights.SHIELD;
                 shields += force;
             }
 
             else continue;
 
             strengthen(strength, coord.directionTo(info.location), force);
         }
 
         rc.setIndicatorString(2,
                 "ally=" + count + ", meds=" + med + ", shields=" + shields);
     }
 
     // \todo this is never used...
     private static int numAlliedBases(RobotController rc, RobotType type)
         throws GameActionException
     {
         MapLocation bases[] = rc.senseAlliedEncampmentSquares();
         int steps = Utils.ceilDiv(bases.length, MAX_BASES);
         int n = 0;
 
         for (int i = 0; i < bases.length; i += steps) {
             Robot base = (Robot) rc.senseObjectAtLocation(bases[i]);
             RobotInfo baseType = rc.senseRobotInfo(base);
             if (baseType.equals(type)) n++;
         }
         return n;
     }
 
     // TODO: vastly improve the capture logic
     public static boolean capture(
             RobotController rc, MapLocation coord, double stratLoc, double defLoc)
         throws GameActionException
     {
         if (!rc.senseEncampmentSquare(coord)) return false;
 
         MapLocation ourBases[] = rc.senseAlliedEncampmentSquares();
         MapLocation neutBases[] = rc.senseEncampmentSquares(coord, GL_RADIUS, Team.NEUTRAL);
         double maxPower = Weights.MAX_POWER + ourBases.length * Weights.OURBASE_MULT;
         maxPower += (neutBases.length - ourBases.length) * Weights.NEUTBASE_MULT;
 
         double cost = rc.senseCaptureCost();
         if (cost <= 0.0) return false;
         if (cost >= rc.getTeamPower()) return false;
 
         double rnd = Math.random();
 
         // prioritize artillery on the path between the HQs, and closer to enemy HQ
         double militaryWeight = Weights.MILITARY *
         (Weights.STRAT_CAMP * stratLoc + Weights.DEF_CAMP * defLoc);
 
         rc.setIndicatorString(1, "def=" + defLoc + ", strat=" + stratLoc +
             ", w=" + militaryWeight + ", rnd=" + rnd/* +
             ", suppliers=" + numAlliedBases(rc, RobotType.SUPPLIER)*/);
 
 
         if (rnd < militaryWeight) {
             rnd = Math.random();
             if (rnd < Weights.MEDBAY_SUM)
                 rc.captureEncampment(RobotType.MEDBAY);
             else if (rnd < Weights.SHIELDS_SUM)
                 rc.captureEncampment(RobotType.SHIELDS);
             else
                 rc.captureEncampment(RobotType.ARTILLERY);
         } else {
             // TODO: improve supplier/generator decision
             if (Clock.getRoundNum() < Weights.MIN_ROUND)
                 rc.captureEncampment(RobotType.SUPPLIER);
             else if (rc.getTeamPower() < Weights.MIN_POWER)
                 rc.captureEncampment(RobotType.GENERATOR);
             else if (rc.getTeamPower() > maxPower)
                 rc.captureEncampment(RobotType.SUPPLIER);
             else {
                 rnd = Math.random();
                 double ratio =
                     (rc.getTeamPower() - Weights.MIN_POWER) /
                     (maxPower - Weights.MIN_POWER);
 
                 rc.setIndicatorString(1,
                         "def=" + defLoc + ", strat=" + stratLoc + 
                         ", w=" + militaryWeight + ", rnd=" + rnd
                         + ", pratio=" + ratio
                         /* + ", suppliers=" + numAlliedBases(rc, RobotType.SUPPLIER)*/);
 
                 if (rnd < ratio)
                     rc.captureEncampment(RobotType.GENERATOR);
                 else
                     rc.captureEncampment(RobotType.SUPPLIER);
             }
         }
 
         return true;
     }
 
     public static double getMineStr(
         RobotController rc, double defense, MapLocation coord, int minesNearby) {
 
         double mineStr = defense * Weights.LAY_MINE;
         if (rc.hasUpgrade(Upgrade.PICKAXE)) {
             int orthogonalMines = 0;
             if (rc.senseMine(coord.add(Direction.NORTH)) != null)
                 orthogonalMines++;
             if (rc.senseMine(coord.add(Direction.SOUTH)) != null)
                 orthogonalMines++;
             if (rc.senseMine(coord.add(Direction.EAST)) != null)
                 orthogonalMines++;
             if (rc.senseMine(coord.add(Direction.WEST)) != null)
                 orthogonalMines++;
             mineStr *= 5-orthogonalMines;
         }
         // TODO : make areas with encampments more enticing
         // if (rc.senseEncampmentSquare(coord)){
         //     mineStr += Weights.LAY_MINE;
         // }
         double minesNearbyFactor = Weights.NEARBY_MINE * ((LC_RADIUS/2)-(minesNearby));
         return mineStr + minesNearbyFactor;
     }
 
 
     /**
      */
     public static void run(RobotController rc) throws GameActionException
     {
 
 
         // first things first.
         rc.wearHat();
 
         Team team = rc.getTeam();
 
         while (true) {
 
             // This is an extremely ugly hack to get around the fact that every
             // agents start with the same seed.
             Math.random();
 
             if (!rc.isActive()) {
                 rc.yield();
                 continue;
             }
 
             debug_resetBc();
 
             MapLocation coord = rc.getLocation();
             // These represent the pull strengths in each direction by the affecting fields
             double strength[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 
 
             // Enemy HQ
             strengthen(
                     strength, coord.directionTo(Storage.ENEMY_HQ), Weights.ENEMY_HQ,
                     Storage.ENEMY_HQ.distanceSquaredTo(coord));
 
             debug_checkBc(rc, "HQ");
 
             // Check if there are enemies nearby
             boolean enemiesNearby = localRobots(rc, coord, strength, team);
             debug_checkBc(rc, "local-robot");
 
             // There are two different modes of action
             if (!enemiesNearby) {
 
                 mines(rc, coord, strength, Weights.EXPLORE_MINE, GL_RADIUS);
                 debug_checkBc(rc, "explore-mine");
 
                 neutralBases(rc, coord, strength);
                 debug_checkBc(rc, "neutral-base");
 
                 globalRobots(rc, coord, strength, Weights.GL_ENEMY_SD, team.opponent());
                 debug_checkBc(rc, "global-robot");
 
                 //TODO: defensiveMines(rc, coord, strength);
             }
             else {
                 mines(rc, coord, strength, Weights.BATTLE_MINE, LC_RADIUS);
                 debug_checkBc(rc, "battle-mine");
 
                 allyBases(rc, coord, strength, team);
                 debug_checkBc(rc, "ally-base");
 
                 battleFormation(rc, coord, strength, team);
                 debug_checkBc(rc, "battle-formation");
             }
 
             // Compute the final direction.
 
             double maxStrength = Double.NEGATIVE_INFINITY;
             Direction finalDir = null;
 
             for (int i = 0; i < 8; ++i) {
                 if (maxStrength > strength[i]) continue;
                 if (!rc.canMove(Utils.dirByOrd[i])) continue;
 
                 maxStrength = strength[i];
                 finalDir = Utils.dirByOrd[i];
             }
 
             // debug_dumpStrength(rc, strength);
             debug_checkBc(rc, "select-strength");
 
             if (finalDir == null) { rc.yield(); continue; }
             rc.setIndicatorString(0, "max_str=" + maxStrength + ", dir=" + finalDir);
 
             // TODO: all of these are things are Storage
             double dist = Utils.distTwoPoints(Storage.MY_HQ, Storage.ENEMY_HQ);
             double defense = Utils.defensiveRelevance(
                     coord, Storage.MY_HQ, Storage.ENEMY_HQ, dist, Weights.DEF_RATIO);
             double strat = Utils.strategicRelevance(
                     coord, Storage.MY_HQ, Storage.ENEMY_HQ, dist, Weights.STRAT_RATIO);
             double mineStr = 0;
 
             // TODO: incorporate threat level instead of boolean enemiesNearby
             // PS remi I hate you for your confusing tricks
             if (enemiesNearby || !capture(rc, coord, strat, defense)) {
                 debug_checkBc(rc, "capture");
                 if (!enemiesNearby && rc.senseMine(coord) == null) {
                     // see if we should lay a mine here
                     int minesNearby = rc.senseMineLocations(coord, LC_RADIUS, team).length;
                     mineStr = getMineStr(rc, defense, coord, minesNearby);
                     rc.setIndicatorString(1, "defense=" + defense + ", mine_str=" + mineStr);
                     debug_checkBc(rc, "getMineStr");
                 }
                 if (mineStr > maxStrength){
                         rc.layMine();
                 } else {
                     MapLocation target = coord.add(finalDir);
                     Team mine = rc.senseMine(target);
                     // Execute the move safely.
                     if (mine == null || mine == team) {
                         if (rc.canMove(finalDir))
                             rc.move(finalDir);
                     }
                     else rc.defuseMine(target);
                 }
             }
 
             debug_checkBc(rc, "end");
 
             rc.yield();
         }
     }
 
     private static int lastBcCounter;
 
     private static void debug_resetBc()
     {
         lastBcCounter = Clock.getBytecodeNum();
     }
 
     private static void debug_checkBc(RobotController rc, String where)
     {
         int bcCounter = Clock.getBytecodeNum();
         if (bcCounter < lastBcCounter) {
             System.err.println(
                     "BC EXCEEDED: " + where + ", " +
                     lastBcCounter +  " -> " + bcCounter);
             rc.breakpoint();
         }
         lastBcCounter = bcCounter;
     }
 
     private static int debug_countBc(boolean reset)
     {
         int bcCounter = Clock.getBytecodeNum();
         int delta = bcCounter - lastBcCounter;
         if (reset) lastBcCounter = bcCounter;
         return delta;
     }
 
     private static void debug_dumpStrength(
             RobotController rc, double[] strength)
     {
         String str = "{ ";
         for (int i = 0; i < 8; ++i) {
             str += "(" + Utils.dirByOrd[i].name() +
                 "=" + ((int)(strength[i] * 10000)) + ") ";
         }
         str += "}";
 
         rc.setIndicatorString(0, str);
     }
 
 }
