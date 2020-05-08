 package bki;
 
 import org.junit.Test;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Provides the unit tests for the {@link RobotHelper} methods.
  * 
  * @author Bret K. Ikehara
  * 
  */
 public class TestRobotHelper {
 
   /**
    * Checks double value with precision to the 0.05.
    * 
    * @param expected double Expected value.
    * @param value double Value that should be check against the expected value.
    * @return boolean
    */
   private static boolean checkDouble(double expected, double value) {
     double epsilon = 0.05;
     return Math.abs(expected - value) < epsilon;
   }
 
   /**
    * Tests the {@link RobotHelper#calculateDistance} method.
    */
   @Test
   public void testCalculateDistance() {
 
     // test basic
     double dist = RobotHelper.calculateDistance(8, 12, 10, 10);
     boolean b = checkDouble(2.8, dist);
     assertTrue("Distance should be 2.8", b);
 
     // test negative
     dist = RobotHelper.calculateDistance(-1, -3, 10, 10);
     b = checkDouble(17, dist);
     assertTrue("Distance should be 17", b);
   }
 
  /**
   * Helper class for tests.
   * 
   * @param expectedAngle double
   * @param angle double
   */
   public void testCalculateAngleToPointHelper(double expectedAngle, double angle) {
     boolean b = checkDouble(expectedAngle, angle);
     assertTrue("Angle should be " + expectedAngle + "  degrees: " + angle, b);
   }
 
   /**
    * Test the {@link RobotHelper#calculateAngleToPoint} method.
    */
   @Test
   public void testCalculateAngleToPoint() {
     double centerX = 10, centerY = 10, angle, x, y;
 
     // robot position in regards to center: below x and below y.
     x = 8;
     y = 9;
     // face north.
     angle = RobotHelper.calculateAngleToPoint(0, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(63.43, angle);
 
     // face east
     angle = RobotHelper.calculateAngleToPoint(90, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-26.56, angle);
 
     // face south
     angle = RobotHelper.calculateAngleToPoint(180, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-116.56, angle);
 
     // face south
     angle = RobotHelper.calculateAngleToPoint(270, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-206.56, angle);
 
     // robot position in regards to center: above x and below y.
     x = 13;
     y = 8;
     // face north.
     angle = RobotHelper.calculateAngleToPoint(0, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-56.31, angle);
 
     // face east
     angle = RobotHelper.calculateAngleToPoint(90, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-146.31, angle);
 
     // face south
     angle = RobotHelper.calculateAngleToPoint(180, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-236.31, angle);
 
     // face west
     angle = RobotHelper.calculateAngleToPoint(270, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-326.31, angle);
 
     // robot position in regards to center: below x and above y.
     x = 8;
     y = 13;
     // face north.
     angle = RobotHelper.calculateAngleToPoint(0, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(146.31, angle);
 
     // face east
     angle = RobotHelper.calculateAngleToPoint(90, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(56.31, angle);
 
     // face south
     angle = RobotHelper.calculateAngleToPoint(180, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-33.69, angle);
 
     // face west
     angle = RobotHelper.calculateAngleToPoint(270, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-123.69, angle);
 
     // robot position in regards to center: above x and above y.
     x = 12;
     y = 13;
     // face north.
     angle = RobotHelper.calculateAngleToPoint(0, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(213.69, angle);
 
     // face east
     angle = RobotHelper.calculateAngleToPoint(90, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(123.69, angle);
 
     // face south
     angle = RobotHelper.calculateAngleToPoint(180, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(33.69, angle);
 
     // face west
     angle = RobotHelper.calculateAngleToPoint(270, x, y, centerX, centerY);
     testCalculateAngleToPointHelper(-56.31, angle);
   }
 
   /**
    * Helps the {@link TestRobotHelper#testCalculateOptimalTurnAngle} tests.
    * 
    * @param expectedAngle double
    * @param angle double
    */
   public void testCalculateOptimalTurnAngleHelper(double expectedAngle, double angle) {
     boolean b = checkDouble(expectedAngle, angle);
     assertTrue("Angle should be " + expectedAngle + ": " + angle, b);
   }
 
   /**
    * Tests the {@link RobotHelper#calculateOptimalAngle} method.
    */
   @Test
   public void testCalculateOptimalTurnAngle() {
     double angle;
     
     angle = RobotHelper.calculateOptimalAngle(360);
     testCalculateOptimalTurnAngleHelper(0, angle);
 
     angle = RobotHelper.calculateOptimalAngle(720);
     testCalculateOptimalTurnAngleHelper(0, angle);
     
 
     angle = RobotHelper.calculateOptimalAngle(-360);
     testCalculateOptimalTurnAngleHelper(0, angle);
     
 
     angle = RobotHelper.calculateOptimalAngle(180);
     testCalculateOptimalTurnAngleHelper(180, angle);
     
 
     angle = RobotHelper.calculateOptimalAngle(-180);
     testCalculateOptimalTurnAngleHelper(-180, angle);
     
 
     angle = RobotHelper.calculateOptimalAngle(185);
     testCalculateOptimalTurnAngleHelper(-175, angle);
     
 
     angle = RobotHelper.calculateOptimalAngle(-185);
     testCalculateOptimalTurnAngleHelper(175, angle);
   }
   
   /**
    * Tests the {@link RobotHelper#calculateAngleToHeading} method.
    */
   @Test  
   public void testCalculateAngleToHeading() {
     double angle, robotHeading, enemyBearing, enemyHeading, expected;
     
     // basic test.
     robotHeading = 0;
     enemyBearing = 45;
     enemyHeading = 90;
     expected = -135;
     angle = RobotHelper.calculateAngleToHeading(robotHeading, enemyBearing, enemyHeading);
     testCalculateAngleToHeadingHelper(expected, angle);
 
     // ensure angle is corretly normalized.
     robotHeading = 20;
     enemyBearing = -65;
     enemyHeading = 160;
     expected = 25;
     angle = RobotHelper.calculateAngleToHeading(robotHeading, enemyBearing, enemyHeading);
     testCalculateAngleToHeadingHelper(expected, angle);
     
     // TODO finish tests.
   }
   
   /**
    * Helps the {@link TestRobotHelper#testCalculateAngleToHeading} tests.
    * 
    * @param expectedAngle double
    * @param angle double
    */
   public void testCalculateAngleToHeadingHelper(double expectedAngle, double angle) {
     boolean b = checkDouble(expectedAngle, angle);
     assertTrue("Angle should be " + expectedAngle + ": " + angle, b);
   }
 }
