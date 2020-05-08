 package baseNuke;
 
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 
 public class ArtilleryRobot extends BaseRobot {
 	
 	public ArtilleryRobot(RobotController rc) {
 		super(rc);
 	}
 
 	@Override
 	public void run() {
 		try {
 			if (rc.isActive()) {
 				Robot[] potentialTargets = rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 63, rc.getTeam().opponent());
 				MapLocation target = getBestTarget(potentialTargets);
 				if (target != null && rc.canAttackSquare(target)) {
 					rc.attackSquare(target);
 				}
 			}
 		} catch (Exception e) {
 			//
 		}
 	}
 
 	public MapLocation getBestTarget(Robot[] potentialTargets) throws GameActionException {
 		int highestScore = 0;
 		MapLocation bestLocation = null;
 		for (Robot potentialTarget : potentialTargets){
 			int currentScore = 0;
 			MapLocation location = rc.senseLocationOf(potentialTarget);
 			Robot[] splashRobots = rc.senseNearbyGameObjects(Robot.class, location, GameConstants.ARTILLERY_SPLASH_RADIUS_SQUARED, null);
 			for (Robot splashRobot : splashRobots) {
				if (splashRobot.getTeam() == rc.getTeam()) {
 					currentScore -= 20;
 				} else {
 					currentScore += 20;
 				}
 			}
 			if (currentScore > highestScore) {
 				bestLocation = location;
 				highestScore = currentScore;
 			}
 		}
 		
 		int totalScore = highestScore + 40;
 		if (totalScore > 0) {
 			return bestLocation;
 		} else {
 			return null;
 		}
 	}
 }
