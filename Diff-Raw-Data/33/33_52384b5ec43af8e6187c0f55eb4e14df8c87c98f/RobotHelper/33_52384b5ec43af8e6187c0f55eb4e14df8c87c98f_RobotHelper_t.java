 package bki;
 
 import robocode.Robot;
 
 /**
  * Defines a Robocode Robot helper methods.
  * 
  * @author Bret K. Ikehara
  */
 public class RobotHelper {
 
   // helps calculate radian to degree.
   private static final double RADIAN_TO_DEGREE = 180 / Math.PI;
 
   /**
    * Converts a radian value to degree value.
    * 
   * @param radian double
    * @return double
    */
   public static final double convertRadiantoDegrees(double radian) {
     return radian * RADIAN_TO_DEGREE;
   }
 
   /**
    * Calculates the distance between two points on a Cartesian plane.
    * 
    * @param robot {@link Robot}
    * @param x2 double To-coordinate.
    * @param y2 double To-coordinate.
    * @return double
    */
   public static double calculateDistance(Robot robot, double x2, double y2) {
     return calculateDistance(robot.getX(), robot.getY(), x2, y2);
 
   }
 
   /**
    * Calculates the distance between two points on a Cartesian plane.
    * 
    * @param x1 double From-coordinate.
    * @param y1 double From-coordinate.
    * @param x2 double To-coordinate.
    * @param y2 double To-coordinate.
    * @return double
    */
   public static double calculateDistance(double x1, double y1, double x2, double y2) {
     double value = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
     return Math.sqrt(value);
   }
 
   /**
    * Calculate the angle needed to face towards a xy-point.
    * 
    * @param robot {@link Robot}
    * @param pointX double Coordinate to point the robot.
    * @param pointY double Coordinate to point the robot.
    * @return double
    */
   public static double calculateAngleToPoint(Robot robot, double pointX, double pointY) {
     return calculateAngleToPoint(robot.getHeading(), robot.getX(), robot.getY(), pointX, pointY);
   }
 
   /**
    * Calculate the angle needed to face towards a xy-point. Return value is given as an angle that a
    * robot could use to turn right.
    * 
    * @param heading double Robot's heading.
    * @param robotX double Robot's x coordinate.
    * @param robotY double Robot's y coordinate.
    * @param pointX double Coordinate to point the robot.
    * @param pointY double Coordinate to point the robot.
    * @return double
    */
   public static double calculateAngleToPoint(double heading, double robotX, double robotY,
       double pointX, double pointY) {
     double tanget = (pointX - robotX) / (pointY - robotY);
     double angle = Math.atan(tanget);
     angle = convertRadiantoDegrees(angle);
 
     // face in the y-direction towards the point.
     if (robotY > pointY) {
       heading = 180 - heading;
     }
     else {
       heading = 0 - heading;
     }
 
     return heading + angle;
   }
 
   /**
    * Calculates the oblique triangle in regards to an enemy bearing and heading. Helps calculate the
    * angle needing to turn the gun to hit our target.
    * 
    * @param robotHeading double
    * @param enemyBearing double
    * @param enemyHeading double
    * @return double
    */
   public static double calculateAngleToHeading(double robotHeading, double enemyBearing,
       double enemyHeading) {
     double angleToRobot = (robotHeading + enemyBearing) % 360;
     // right triangle angle.
     double triangleAngle = angleToRobot % 90;
 
     double offset;
     if (angleToRobot < 90) {
       offset = 180;
     }
     else if (angleToRobot < 180) {
       offset = 270;
     }
     else if (angleToRobot < 270) {
       offset = 0;
     }
     else {
       offset = 90;
     }
 
     return enemyHeading - (triangleAngle + offset % 360);
   }
 
   /**
    * Calculates the optimal right-turn angle.
    * 
    * @param angle double
    * @return double
    */
   public static double calculateOptimalAngle(double angle) {
     double a = angle % 360;
     if (a > 180) {
       return a - 360;
     }
     else if (a < -180) {
       return a + 360;
     }
     return a;
   }
 }
