 package navigation;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import robocode.AdvancedRobot;
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 import robocode.util.Utils;
 
 import com.jaxelson.EnemyBot;
 import com.jaxelson.BotUtility;
 
 /**
  * Wave Surfer based on http://robowiki.net/wiki/Wave_Surfing_Tutorial
  * @author Jason Axelson
  */
 
 
 public class WaveSurfing
         extends State {
 
 	/*
 	 * Wave surfing helper variables
 	 */
 
 	public static int BINS = 47;
 	public static double _surfStats[] = new double[BINS];
 	public Point2D.Double _myLocation;     // our bot's location
 	public Point2D.Double _enemyLocation;  // enemy bot's location
 
 	public ArrayList<EnemyWave> _enemyWaves;
 	public ArrayList<Integer> _surfDirections;
 	public ArrayList<Double> _surfAbsBearings;
 
 	public static double _oppEnergy = 100.0;
 	
 	/** This is a rectangle that represents an 800x600 battle field,
 	    * used for a simple, iterative WallSmoothing method (by PEZ).
 	    * If you're not familiar with WallSmoothing, the wall stick indicates
 	    * the amount of space we try to always have on either end of the tank
 	    * (extending straight out the front or back) before touching a wall.
 	    */
 	// TODO: fix magic numbers
 	    public static Rectangle2D.Double _fieldRect
 	        = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
 	    public static double WALL_STICK = 160;
 
 
     // CONSTRUCTORS
 
     /**
      * Creates a new TrackState for the specified robot.
      * @param robot The ExtendedRobot object used to provide data and execute
      *              commands
      */
     public WaveSurfing(ExtendedBot robot) {
         super(robot);
     }
 
     // PUBLIC METHODS
 
     /**
      * Returns the statistics of how this state has performed against the
      * current target.
      * @return The NavigationStateStatistics object for the current state
      *         and target
      */
     public Statistics getStatistics() {
         if (statistics == null) {
             statistics = new Statistics();
         }        
         return statistics;
     }
 
     /**
      * Returns the name of this State.
      * @return A String containing the name of this State object
      */
     public String getName() {
         return "MoveLeftRightState";
     }
 
     /**
      * Returns whether the this state is valid (may be used under the
      * current circumstances).
      * @return A boolean indicating whether this State should be used
      */
     public boolean isValid() {
         return true;
     }
 
     /**
      * This method will be called to indicate the CommandListener should free
      * all resources and cease execution.
      */
     public void disable() {
         robot.removeEventListener(ON_HIT_BY_BULLET, this);
         robot.removeEventListener(ON_HIT_BY_BULLET, this);
         energy = 0;
         updateStatistics();
     }
 
     /**
      * This method will be called to indicate the CommandListener is free to
      * begin execution.
      */
     public void enable() {
 //    	robot.setAdjustRadarForGunTurn(true);
         startTime = robot.getTime();
         energy = robot.getEnergy();
         damageTaken = 0;
         robot.addEventListener(ON_HIT_BY_BULLET, this);
         robot.addEventListener(ON_SCANNED_ROBOT, this);
         robot.addEventListener(ON_PAINT, this);
         
         /** A collection of waves, to surf and gather stats on */
         _enemyWaves = new ArrayList<EnemyWave>();
         /** 
          * A collection of our direction in relation to the enemy in past ticks, 1 for
          * clock-wise, -1 for counter-clockwise.
          */
         _surfDirections = new ArrayList<Integer>();
         /** A collection of past absolute bearings to enemy. */
         _surfAbsBearings = new ArrayList<Double>();
         
         robot.setAdjustGunForRobotTurn(true);
         robot.setAdjustRadarForGunTurn(true);
         
         robot.setTurnRadarRightRadians(Math.PI*2);
     }
 
     /**
      * This method will be called each turn to allow the DodgeState to
      * execute turn based instructions.
      */
     public void execute() {
 //    	robot.setTurnRadarRightRadians(Math.PI*2);
 //    	robot.setAhead(100);
     }
 
     /**
      * This method will be called when your robot is hit by a bullet.
      * @param event A HitByBulletEvent object containing the details of your
      *              robot being hit by a bullet
      */
     public void onHitByBullet(HitByBulletEvent event) {
 //    	System.out.println("I've been hit!");
         damageTaken += BotMath.calculateDamage(event.getPower());
         
      // If the _enemyWaves collection is empty, we must have missed the
         // detection of this wave somehow.
         if (!_enemyWaves.isEmpty()) {
             Point2D.Double hitBulletLocation = new Point2D.Double(
                 event.getBullet().getX(), event.getBullet().getY());
             EnemyWave hitWave = null;
 
             // look through the EnemyWaves, and find one that could've hit us.
             for (int x = 0; x < _enemyWaves.size(); x++) {
                 EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
 
                 if (Math.abs(ew.distanceTraveled -
                     _myLocation.distance(ew.fireLocation)) < 50
                     && Math.abs(bulletVelocity(event.getBullet().getPower()) 
                         - ew.bulletVelocity) < 0.001) {
                     hitWave = ew;
                     break;
                 }
             }
 
             if (hitWave != null) {
                 logHit(hitWave, hitBulletLocation);
 
                 // We can remove this wave now, of course.
                 _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
             }
         }
     }
 
     
     public void onPaint(Graphics2D g) {
     	System.out.println("Now Painting");
         // Set the paint color to red
         g.setColor(java.awt.Color.RED);
         
         // Draw waves
         for(EnemyWave wave: _enemyWaves) {
         	wave.drawWave(g);
         }
         
     }
     
     /**
      * This method will be called when your robot sees another robot.<br>
      * NOTE: This class provides a blank instantiation of this method.
      * @param event A ScannedRobotEvent object containing the details of your
      *              robot's sighting of another robot
      */
     public void onScannedRobot(ScannedRobotEvent event) {
         targetBearing = event.getBearingRadians();
         targetAcquired = true;
         EnemyBot target = new EnemyBot(event);
 //        target.printBot();
         
         robot.narrowRadarLock(event);
         
         robot.turnGunTo(target);
         robot.setFire(1.0);
         
         
         _myLocation = new Point2D.Double(robot.getX(), robot.getY());
 
         double lateralVelocity = robot.getVelocity()*Math.sin(event.getBearingRadians());
         double absBearing = event.getBearingRadians() + robot.getHeadingRadians();
 
 //        robot.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
 //            - robot.getRadarHeadingRadians()) * 2);
 
         _surfDirections.add(0,
             new Integer((lateralVelocity >= 0) ? 1 : -1));
         _surfAbsBearings.add(0, new Double(absBearing + Math.PI));
 
         
         double bulletPower = _oppEnergy - event.getEnergy();
         if (bulletPower < 3.01 && bulletPower > 0.09
             && _surfDirections.size() > 2) {
         	
             EnemyWave ew = new EnemyWave();
             ew.fireTime = robot.getTime() - 1;
             ew.bulletVelocity = bulletVelocity(bulletPower);
             ew.distanceTraveled = bulletVelocity(bulletPower);
             ew.direction = ((Integer)_surfDirections.get(2)).intValue();
             ew.directAngle = ((Double)_surfAbsBearings.get(2)).doubleValue();
             ew.fireLocation = (Point2D.Double)_enemyLocation.clone(); // last tick
 
             _enemyWaves.add(ew);
         }
         
         _oppEnergy = event.getEnergy();
 
         // update after EnemyWave detection, because that needs the previous
         // enemy location as the source of the wave
         _enemyLocation = project(_myLocation, absBearing, event.getDistance());
 
         updateWaves();
 //        System.out.println("Now surfing");
         doSurfing();
     	
     }
 
     // PRIVATE METHODS
 
     /**
      * Recalculates state statistics.
      */
     private void updateStatistics() {
         statistics.update(robot.getOthers(),
                           damageTaken,
                           (robot.getTime() - startTime));
     }
 
     // INSTANCE VARIABLES
 
     // Ordinarily I would use accessor methods exclusively to access instance
     // variables, but in the interest of speed I have allowed direct access.
 
     private int state = 0;
     private boolean targetAcquired = false;
     
     /**
      * Last known bearing to the target bot
      */
     private double targetBearing;
     /**
      * The energy of the bot when this state was chosen
      */
     private double energy;
     /**
      * The total energy lost from bullet hits while this state has been
      * in use
      */
     private double damageTaken;
     /**
      * The time when this state was enabled.
      */
     private long startTime;
     /**
      * Used to track statistics of this state in battle
      */
     private static Statistics statistics;
     
     
     class EnemyWave {
     	Point2D.Double fireLocation;
     	long fireTime;
     	double bulletVelocity, directAngle, distanceTraveled;
     	int direction;
 
     	public EnemyWave() {
     	}
     	
     	public void drawWave(Graphics2D g) {
    		BotUtility.drawCircle(g, fireLocation, distanceTraveled);
     	}
     }
 
     /*
      * Wave surfing Helper Functions
      */
 
 
 
     public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
         while (!_fieldRect.contains(project(botLocation, angle, WALL_STICK))) {
             angle += orientation*0.05;
         }
         return angle;
     }
 
     public static Point2D.Double project(Point2D.Double sourceLocation,
     		double angle, double length) {
     	return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
     			sourceLocation.y + Math.cos(angle) * length);
     }
 
     public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
         return Math.atan2(target.x - source.x, target.y - source.y);
     }
 
     public static double limit(double min, double value, double max) {
         return Math.max(min, Math.min(value, max));
     }
 
     public static double bulletVelocity(double power) {
         return (20.0 - (3.0*power));
     }
 
     public static double maxEscapeAngle(double velocity) {
         return Math.asin(8.0/velocity);
     }
 
     public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
         double angle =
             Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
         if (Math.abs(angle) > (Math.PI/2)) {
             if (angle < 0) {
                 robot.setTurnRightRadians(Math.PI + angle);
             } else {
                 robot.setTurnLeftRadians(Math.PI - angle);
             }
             robot.setBack(100);
         } else {
             if (angle < 0) {
                 robot.setTurnLeftRadians(-1*angle);
            } else {
                 robot.setTurnRightRadians(angle);
            }
             robot.setAhead(100);
         }
     }
 
     public void updateWaves() {
         for (int x = 0; x < _enemyWaves.size(); x++) {
             EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
 
             ew.distanceTraveled = (robot.getTime() - ew.fireTime) * ew.bulletVelocity;
             if (ew.distanceTraveled >
                 _myLocation.distance(ew.fireLocation) + 50) {
                 _enemyWaves.remove(x);
                 x--;
             }
         }
     }
     
     public EnemyWave getClosestSurfableWave() {
         double closestDistance = Double.POSITIVE_INFINITY;
         EnemyWave surfWave = null;
 
         for (int x = 0; x < _enemyWaves.size(); x++) {
             EnemyWave ew = _enemyWaves.get(x);
             double distance = _myLocation.distance(ew.fireLocation)
                 - ew.distanceTraveled;
 
             if (distance > ew.bulletVelocity && distance < closestDistance) {
                 surfWave = ew;
                 closestDistance = distance;
             }
         }
 
         return surfWave;
     }
 
 	/**
 	 * Given the EnemyWave that the bullet was on, and the point where we were
 	 * hit, calculate the index into our stat array for that factor.
 	 * 
 	 * @param ew EnemyWave bullet was on
 	 * @param targetLocation point we were hit
 	 * @return index of our stat array for that factor
 	 */
 	public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
 		double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
         double factor = Utils.normalRelativeAngle(offsetAngle)
             / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
 
         return (int)limit(0,
             (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
             BINS - 1);
     }
 
 	/**
 	 * Given the EnemyWave that the bullet was on, and the point where we were
 	 * hit, update our stat array to reflect the danger in that area.
 	 * 
 	 * @param ew
 	 *            EnemyWave bullet was on
 	 * @param targetLocation
 	 *            point where we were hit
 	 */
     public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
         int index = getFactorIndex(ew, targetLocation);
 
         for (int x = 0; x < BINS; x++) {
             // for the spot bin that we were hit on, add 1;
             // for the bins next to it, add 1 / 2;
             // the next one, add 1 / 5; and so on...
             _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
         }
     }
     
     public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
         Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
         double predictedVelocity = robot.getVelocity();
         double predictedHeading = robot.getHeadingRadians();
         double maxTurning, moveAngle, moveDir;
 
         int counter = 0; // number of ticks in the future
         boolean intercepted = false;
 
         do {    // the rest of these code comments are rozu's
             moveAngle =
                 wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation,
                 predictedPosition) + (direction * (Math.PI/2)), direction)
                 - predictedHeading;
             moveDir = 1;
 
             if(Math.cos(moveAngle) < 0) {
                 moveAngle += Math.PI;
                 moveDir = -1;
             }
 
             moveAngle = Utils.normalRelativeAngle(moveAngle);
 
             // maxTurning is built in like this, you can't turn more then this in one tick
             maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
             predictedHeading = Utils.normalRelativeAngle(predictedHeading
                 + limit(-maxTurning, moveAngle, maxTurning));
 
             // this one is nice ;). if predictedVelocity and moveDir have
             // different signs you want to breack down
             // otherwise you want to accelerate (look at the factor "2")
             predictedVelocity +=
                 (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
             predictedVelocity = limit(-8, predictedVelocity, 8);
 
             // calculate the new predicted position
             predictedPosition = project(predictedPosition, predictedHeading,
                 predictedVelocity);
 
             counter++;
 
             if (predictedPosition.distance(surfWave.fireLocation) <
                 surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
                 + surfWave.bulletVelocity) {
                 intercepted = true;
             }
         } while(!intercepted && counter < 500);
 
         return predictedPosition;
     }
     
     public double checkDanger(EnemyWave surfWave, int direction) {
         int index = getFactorIndex(surfWave,
             predictPosition(surfWave, direction));
 
         return _surfStats[index];
     }
 
     public void doSurfing() {
         EnemyWave surfWave = getClosestSurfableWave();
 
         if (surfWave == null) { return; }
 
         double dangerLeft = checkDanger(surfWave, -1);
         double dangerRight = checkDanger(surfWave, 1);
 
         double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
         if (dangerLeft < dangerRight) {
             goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
         } else {
             goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
         }
 
         setBackAsFront(robot, goAngle);
     }
 }
