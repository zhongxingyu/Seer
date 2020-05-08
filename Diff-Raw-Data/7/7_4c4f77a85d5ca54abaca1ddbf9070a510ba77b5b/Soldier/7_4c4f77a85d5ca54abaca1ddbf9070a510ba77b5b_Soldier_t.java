 package fieldbot;
 
 import battlecode.common.*;
 
 public class Soldier
 {
 
     private static final int GL_RADIUS = 100 * 100;
     private static final int LC_RADIUS = 20;
 
     private static final int MAX_MINES  = 10;
     private static final int MAX_ROBOTS = 20;
     private static final int MAX_BASES = 10;
 
 
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
 
     private static void strengthen(
             double[] strength, Direction dir, double force)
     {
         if (dir == Direction.OMNI) return;
 
         int ord = dir.ordinal();
         final int mask = 8 - 1;
 
         strength[ord] += force;
         strength[(ord - 1) & mask] += force / 2;
         strength[(ord + 1) & mask] += force / 2;
     }
 
     private static void strengthen(
             double[] strength, Direction dir, double weight, double dist)
     {
         strengthen(strength, dir, weight * (1 / dist));
     }
 
 
     /**
      */
     private static void mines(
             RobotController rc, MapLocation coord,
             double strength[], double w, Team team)
         throws GameActionException
     {
         MapLocation mines[] = rc.senseMineLocations(coord, GL_RADIUS, team);
 
         int steps = Math.max(mines.length / MAX_MINES, 1);
 
         for (int i = 0; i < mines.length; i += steps) {
             Direction dir = coord.directionTo(mines[i]);
             strengthen(strength, dir, w, coord.distanceSquaredTo(mines[i]));
         }
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
 
         int steps = Math.max(1, robots.length / MAX_ROBOTS);
 
         for (int i = 0; i < robots.length; i += steps) {
             MapLocation robotCoord = rc.senseRobotInfo(robots[i]).location;
             Direction dir = coord.directionTo(robotCoord);
 
             strengthen(strength, dir, w, coord.distanceSquaredTo(robotCoord));
         }
     }
 
 
     /**
      */
     private static void localRobots(
             RobotController rc, MapLocation coord, double strength[], Team team)
         throws GameActionException
     {
         Robot enemies[] = rc.senseNearbyGameObjects(
                 Robot.class, LC_RADIUS, team.opponent());
 
         if (enemies.length == 0) return;
 
         int x = 0, y = 0;
         int numEnemies = 0;
         int steps = Math.max(1, enemies.length / MAX_ROBOTS);
 
         for (int i = 0; i < enemies.length; i += steps) {
             RobotInfo info = rc.senseRobotInfo(enemies[i]);
             if (info.type != RobotType.SOLDIER) continue;
 
             x += info.location.x;
             y += info.location.y;
             numEnemies++;
         }
        if (numEnemies == 0) return;

         MapLocation enemyCenter = new MapLocation(x/numEnemies, y/numEnemies);
 
         Robot allies[] = rc.senseNearbyGameObjects(
                 Robot.class, LC_RADIUS, team);
 
         double force = (
                 allies.length * Weights.LC_ALLY_SD -
                 numEnemies * Weights.LC_ENEMY_SD) *
             Weights.LC_MUL;
 
         String str = "enemies=" + numEnemies + "/" + enemies.length +
             ", allies=" + allies.length +
             ", enemyCenter=" + enemyCenter.toString();
         rc.setIndicatorString(1, str);
 
         Direction chargeDir = coord.directionTo(enemyCenter);
         strengthen(strength, chargeDir, force);
 
         // Direction retreatDir = enemyCenter.directionTo(coord);
         // strengthen(strength, retreatDir, -force);
     }
 
 
     /**
      */
     public static void run(RobotController rc) throws GameActionException
     {
         final Team team = rc.getTeam();
         final Robot robot = rc.getRobot();
 
         rc.wearHat();
 
         while (true) {
 
             if (!rc.isActive()) {
                 rc.yield();
                 continue;
             }
 
             MapLocation coord = rc.getLocation();
             double strength[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 
 
             // Enemy HQ
             {
                 MapLocation hq = rc.senseEnemyHQLocation();
                 Direction dir = coord.directionTo(hq);
                 strengthen(
                         strength, dir, Weights.ENEMY_HQ,
                         hq.distanceSquaredTo(coord));
             }
 
 
             mines(rc, coord, strength, Weights.NEUTRAL_MINE, Team.NEUTRAL);
             mines(rc, coord, strength, Weights.ENEMY_MINE, team.opponent());
 
            globalRobots(rc, coord, strength, Weights.GL_ENEMY_SD, team.opponent());
            globalRobots(rc, coord, strength, Weights.GL_ALLY_SD, team);
 
             localRobots(rc, coord, strength, team);
 
 
             // Compute the final direction.
 
             double maxStrength = strength[0];
             Direction finalDir = Utils.dirByOrd[0];
 
             for (int i = 1; i < 8; ++i) {
                 if (maxStrength > strength[i]) continue;
                 if (!rc.canMove(Utils.dirByOrd[i])) continue;
 
                 maxStrength = strength[i];
                 finalDir = Utils.dirByOrd[i];
             }
 
             debug_dumpStrength(rc, strength);
 
 
             // Execute the move safely.
             {
                 MapLocation target = coord.add(finalDir);
                 Team mine = rc.senseMine(target);
 
                 if (mine == null || mine == team) {
                     if (rc.canMove(finalDir))
                         rc.move(finalDir);
                 }
                 else rc.defuseMine(target);
             }
 
             rc.yield();
         }
     }
 }
