 package ca.hilikus.jrobocom;
 
 import java.awt.Point;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import ca.hilikus.jrobocom.player.ScanResult;
 import ca.hilikus.jrobocom.player.ScanResult.Found;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 /**
  * The game board
  * 
  * @author hilikus
  * 
  */
public class World {
 
     private BiMap<Robot, Point> robotsPosition = HashBiMap.create();
 
     private Set<Integer> teamIds = new HashSet<>();
 
     private MasterClock clock = new MasterClock();
 
     /**
      * Adds a new robot to the world in the reference field of the parent
      * 
      * @param parent
      * @param child
      */
     public void add(Robot parent, Robot child) {
 	Point newPosition = getReferenceField(parent, 1);
 	if (isOccupied(newPosition)) {
 	    child.die("Occupied position");
 	}
 	if (robotsPosition.containsKey(child)) {
 	    // child already exists
 	    throw new IllegalArgumentException("Trying to add an existing robot");
 	}
 	robotsPosition.put(child, newPosition);
 	clock.addListener(child);
     }
 
    /**
     * Adds the first robot to the world
     * 
     * @param eve the first robot of the player
     */
     public void addFirst(Robot eve) {
 	Random generator = new Random();
 
 	Point newPosition;
 	do {
 	    int x = generator.nextInt(GameSettings.BOARD_SIZE);
 	    int y = generator.nextInt(GameSettings.BOARD_SIZE);
 	    newPosition = new Point(x, y);
 	} while (isOccupied(newPosition));
 
 	if (robotsPosition.containsKey(eve)) {
 	    // child already exists
 	    throw new IllegalArgumentException("Trying to add an existing robot");
 	}
 	robotsPosition.put(eve, newPosition);
 	clock.addListener(eve);
     }
 
     /**
      * @param robot the robot to remove from the board
      */
     public void remove(Robot robot) {
 	if (!robotsPosition.containsKey(robot)) {
 	    throw new IllegalArgumentException("Robot doesn't exist");
 	}
 
 	robotsPosition.remove(robot);
 	clock.removeListener(robot);
     }
 
     /**
      * Changes the position of a robot to its current reference field
      * 
      * @param robot the robot
      */
     public void move(Robot robot) {
 	if (!robotsPosition.containsKey(robot)) {
 	    throw new IllegalArgumentException("Robot doesn't exist");
 	}
 	if (!robot.isMobile()) {
 	    throw new IllegalArgumentException("Robot can't move");
 	}
 
 	Point newPosition = getReferenceField(robot, 1);
 	if (!isOccupied(newPosition)) {
 	    robotsPosition.forcePut(robot, newPosition);
 	}
 
     }
 
     private boolean isOccupied(Point newPosition) {
 	return robotsPosition.containsValue(newPosition);
     }
 
     private Point getReferenceField(Robot robot, int dist) {
 	assert dist > 0;
 	assert robotsPosition.containsKey(robot);
 
 	int x = robotsPosition.get(robot).x;
 	int y = robotsPosition.get(robot).y;
 	int size = GameSettings.BOARD_SIZE;
 
 	switch (robot.getFacing()) {
 	    case NORTH:
 		y = (y - dist) % size;
 		break;
 	    case EAST:
 		x = (x + dist) % size;
 		break;
 	    case SOUTH:
 		y = (y + dist) % size;
 		break;
 	    case WEST:
 		x = (x - dist) % size;
 		break;
 
 	}
 
 	return new Point(x, y);
     }
 
     /**
      * Gets the robot in the reference field
      * 
      * @param robot the robot to use as centre
      * @return the robot in front, or null if field is empty
      */
     public Robot getNeighbour(Robot robot) {
 	Point neighbourPos = getReferenceField(robot, 1);
 	return robotsPosition.inverse().get(neighbourPos);
 
     }
 
     /**
      * Scans up to a number of fields in front of the robot, stopping on the first field that
      * contains a robot
      * 
      * @param robot scanning robot
      * @param dist maximum distance
      * @return a container of information about the scan
      * @see ScanResult
      */
     public ScanResult scan(Robot robot, int dist) {
 	Point space = getReferenceField(robot, dist);
 	Robot inPosition = robotsPosition.inverse().get(space);
	ScanResult ret = null;
 	if (inPosition == null) {
	    ret = new ScanResult(Found.EMPTY, dist);
 	} else {
 	    if (robot.getTeamId() == inPosition.getTeamId()) {
		ret = new ScanResult(Found.FRIEND, dist);
 	    } else {
		ret = new ScanResult(Found.ENEMY, dist);
 	    }
 	}
 
	return ret;
     }
 
     /**
      * Checks if the teamId specified is available. if it is, it reserves it
      * 
      * @param teamId team id to query
      * @return true if the value is unique
      */
     public boolean validateTeamId(int teamId) {
 	if (teamIds.contains(teamId)) {
 	    return false;
 	} else {
 	    teamIds.add(teamId);
 	    return true;
 	}
     }
 
     public int getBotsCount(int teamId, boolean invert) {
 	int total = 0;
 	for (Robot bot : robotsPosition.keySet()) {
 	    if (bot.getTeamId() == teamId) {
 		if (!invert) {
 		    total++;
 		}
 	    } else {
 		if (invert) {
 		    total++;
 		}
 	    }
 	}
 	return total;
 
     }
 
     /**
      * @return the age of the world in cycles / 1000
      */
     public int getAge() {
 	// TODO Auto-generated method stub
 	return 0;
     }
 
 }
