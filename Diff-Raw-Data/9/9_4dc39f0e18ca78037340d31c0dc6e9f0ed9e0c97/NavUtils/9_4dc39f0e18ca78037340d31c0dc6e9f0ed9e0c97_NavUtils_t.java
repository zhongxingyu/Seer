 package edu.rosehulman.me435;
 
 /**
  * This class contains static helper methods for doing common field navigation
  * tasks. These methods should be helpful when doing arc radius and seeking home
  * calculations.
  * 
  */
 public class NavUtils {
 
 	/** Largest radius value. Arcs with a larger radius are straight. */
 	public static final double MAX_RADIUS_FT = 1000.0;
 
 	/** Smallest radius value. If smaller STOP. You are at the target. */
 	public static final double MIN_RADIUS_FT = 1.0;
 
 	/** Largest recommended turn angle to use Arc Radius strategy. */
 	public static final double RECOMMEND_MAX_TURN_DEGREES = 90.0;
 
 	/** Largest recommended arc length to drive. */
 	public static final double RECOMMEND_MAX_ARC_LENGHT_FT = 150.0;
 
 	/**
 	 * Smallest recommended radius for the arc strategy. If smaller STOP. You
 	 * are at the target.
 	 */
 	public static final double RECOMMEND_MIN_RADIUS_FT = 2.0;
 
 	/**
 	 * Arbitrary large value that is returned if pointing dead away from target.
 	 */
 	public static final double MAX_ARC_LENGTH_FT = 1000.0;
 
 	/** Error tolerance that is acceptable when using bisection for arc radius. */
 	public static final double BISECTION_ERROR_TOLERANCE_FT = 0.01;
 
 	/** Index of the radius value in the result array. */
 	public static final int RESULT_INDEX_RADIUS = 0;
 
 	/** Index of the arc length value in the result array. */
 	public static final int RESULT_INDEX_ARC_LENGTH = 1;
 
 	/**
 	 * This function calculates the radius of a pretend circle that hits the
 	 * target. This circle uses the robot's current heading as a tangent line to
 	 * the circle and robot's current position and the target point as two
 	 * points on the circle. So your robot is already on the circle heading in
 	 * the correct direction.
 	 * 
 	 * To use this function you need to pass in an array of two doubles as the
 	 * last parameter. The resulting radius will be placed into index location 0
 	 * and the arc length to drive will be placed into index location 1. All
 	 * units in feet. All angles in degrees.
 	 * 
 	 * @param robotX
 	 *            Current X location of the robot on the field.
 	 * @param robotY
 	 *            Current Y location of the robot on the field.
 	 * @param robotHeading
 	 *            Current heading of the robot on the field (0 degrees points
 	 *            along the positive X-axis, CCW is positive)
 	 * @param targetX
 	 *            Target X location (often 0 to go home)
 	 * @param targetY
 	 *            Target Y location (often 0 to go home)
 	 * @param result
 	 *            A boolean to indicate if the Arc Radius strategy is
 	 *            recommended. An arc can always be found, but imagine you were
 	 *            1 foot away driving away, the arc radius strategy would have
 	 *            you drive a VERY large radius circle instead of just turning
 	 *            around. In general only angles already pointing towards (plus
 	 *            or minus 70-90 degrees) work well with the Arc Radius
 	 *            strategy.
 	 * @return True if the arc radius strategy is recommended, false otherwise.
 	 */
 	public static boolean calculateArc(double robotX, double robotY,
 			double robotHeading, double targetX, double targetY, double[] result) {
 
 		// Calculate the angle needed to get to the target
 		double targetHeading = getTargetHeading(robotX, robotY, targetX,
 				targetY);
 		// Determine if the Arc Radius strategy is recommended
 		double leftTurnAmount = getLeftTurnHeadingDelta(robotHeading,
 				targetHeading);
 		double rightTurnAmount = getRightTurnHeadingDelta(robotHeading,
 				targetHeading);
 		boolean arcRadiusIsRecommend = leftTurnAmount < RECOMMEND_MAX_TURN_DEGREES
 				|| rightTurnAmount < RECOMMEND_MAX_TURN_DEGREES;
 
 		// Determine if the target is on the left or the right
 		boolean targetOnLeft = targetIsOnLeft(robotX, robotY, robotHeading,
 				targetX, targetY);
 		// Prepare for bisection by checking the extremes first.
 		double lowerRadius = targetOnLeft ? -MIN_RADIUS_FT : MIN_RADIUS_FT;
 		double upperRadius = targetOnLeft ? -MAX_RADIUS_FT : MAX_RADIUS_FT;
 
 		// See if the target XY is within the very large circle
 		if (!circleContainsTarget(upperRadius, robotX, robotY, robotHeading,
 				targetX, targetY)) {
 			// The robot heading is pointing directly at the target (or directly
 			// away).
 			result[RESULT_INDEX_RADIUS] = targetOnLeft ? -MAX_RADIUS_FT
 					: MAX_RADIUS_FT;
 			if (arcRadiusIsRecommend) {
 				result[RESULT_INDEX_ARC_LENGTH] = getDistance(robotX, robotY,
 						targetX, targetY);
 			} else {
 				result[RESULT_INDEX_ARC_LENGTH] = MAX_ARC_LENGTH_FT;
 			}
 		} else if (circleContainsTarget(lowerRadius, robotX, robotY,
 				robotHeading, targetX, targetY)) {
 			// The robot is too close to the target and has a small turn radius.
 			result[RESULT_INDEX_RADIUS] = targetOnLeft ? -MIN_RADIUS_FT
 					: MIN_RADIUS_FT;
 			result[RESULT_INDEX_ARC_LENGTH] = 0; // Don't drive if within a
 													// foot.
 		} else {
 			// Normal value not an edge case. Use bisection to find radius.
 			result[RESULT_INDEX_RADIUS] = bisectionForRadius(lowerRadius,
 					upperRadius, BISECTION_ERROR_TOLERANCE_FT, robotX, robotY,
 					robotHeading, targetX, targetY);
 			result[RESULT_INDEX_ARC_LENGTH] = getArcLength(
 					result[RESULT_INDEX_RADIUS], robotX, robotY, robotHeading,
 					targetX, targetY);
 		}
 
 		// Check if the result is recommended for the arc radius strategy.
 		if (Math.abs(result[RESULT_INDEX_RADIUS]) < RECOMMEND_MIN_RADIUS_FT) {
 			// Robot is at the target. Stop!
 			arcRadiusIsRecommend = false;
 		}
 		if (result[RESULT_INDEX_ARC_LENGTH] > RECOMMEND_MAX_ARC_LENGHT_FT) {
 			// Arc length is huge, use a different strategy.
 			arcRadiusIsRecommend = false;
 		}
 		return arcRadiusIsRecommend;
 	}
 
 	/**
 	 * Method for converting a voice command angle and distance to an arc
 	 * radius.
 	 * 
 	 * @param angle
 	 *            Angle given in the voice command. When I ride on the robot I
 	 *            think of negative as left. So 0 is straight ahead, -30 is a
 	 *            left turn, 150 would be a supper hard right turn.
 	 * 
 	 * @param distance
 	 *            Distance in feet from the robot to the target.
 	 * @param result
 	 *            A boolean to indicate if the Arc Radius strategy is
 	 *            recommended. An arc can always be found, but imagine you were
 	 *            1 foot away driving away, the arc radius strategy would have
 	 *            you drive a VERY large radius circle instead of just turning
 	 *            around (or backing up). In general only angles already
 	 *            pointing towards (plus or minus 70-90 degrees) work well with
 	 *            the Arc Radius strategy.
 	 * @return True if the arc radius strategy is recommended, false otherwise.
 	 */
 	public static boolean calculateArcForVoiceCommand(int angle, int distance,
 			double[] result) {
 		double robotX = -distance * Math.cos(Math.toRadians(-angle));
 		double robotY = -distance * Math.sin(Math.toRadians(-angle));
 		double robotHeading = 0;
 		double targetX = 0;
 		double targetY = 0;
 		return calculateArc(robotX, robotY, robotHeading, targetX, targetY,
 				result);
 	}
 
 	/**
 	 * Uses the robot's X and Y and the target X and Y and determines the
 	 * heading a robot should be on to hit the target. Note, that the robot's
 	 * current heading is not required for this calculation.
 	 * 
 	 * @param robotX
 	 *            Current X location of the robot on the field.
 	 * @param robotY
 	 *            Current Y location of the robot on the field.
 	 * @param targetX
 	 *            Target X location (often 0 to go home)
 	 * @param targetY
 	 *            Target Y location (often 0 to go home)
 	 * @return The heading that points directly to the target.
 	 */
 	public static double getTargetHeading(double robotX, double robotY,
 			double targetX, double targetY) {
 		double deltaX = targetX - robotX;
 		double deltaY = targetY - robotY;
 		return Math.toDegrees(Math.atan2(deltaY, deltaX));
 	}
 
 	/**
 	 * Helper function that determines if the target is on the left or right
 	 * side of the robot's current heading. This function will ALWAYS decide if
 	 * something is on the left or right, if experimental theoretical values are
 	 * given such that the target is perfectly on a straight line from the robot
 	 * heading, this function will return false, because it's not on the left.
 	 * 
 	 * @param robotX
 	 *            Current X location of the robot on the field.
 	 * @param robotY
 	 *            Current Y location of the robot on the field.
 	 * @param robotHeading
 	 *            Current heading of the robot on the field (0 degrees points
 	 *            along the positive X-axis, CCW is positive)
 	 * @param targetX
 	 *            Target X location (often 0 to go home)
 	 * @param targetY
 	 *            Target Y location (often 0 to go home)
 	 * @return True if the target is on the left of the robot's current heading.
 	 */
 	public static boolean targetIsOnLeft(double robotX, double robotY,
 			double robotHeading, double targetX, double targetY) {
 		// Determine the target heading
 		// Calculate the angle needed to turn left.
 		// Calculate the angle needed to turn right.
 		// Determine which angle is smaller.
 		double targetHeading = getTargetHeading(robotX, robotY, targetX,
 				targetY);
 		double leftTurnAmount = getLeftTurnHeadingDelta(robotHeading,
 				targetHeading);
 		double rightTurnAmount = getRightTurnHeadingDelta(robotHeading,
 				targetHeading);
 		return leftTurnAmount < rightTurnAmount;
 	}
 
 	/**
 	 * Returns the number of degrees to turn left to reach the target heading.
 	 * 
 	 * @param robotHeading
 	 *            Current heading of the robot on the field
 	 * @param targetHeading
 	 *            The heading that points directly to the target. (0 degrees
 	 *            always points along the positive X-axis, CCW is positive)
 	 * @return Number of degrees to turn left to reach target heading.
 	 */
 	public static double getLeftTurnHeadingDelta(double robotHeading,
 			double targetHeading) {
 		// Add degrees to robotHeading until it matches targetHeading.
 		if (targetHeading < robotHeading) {
 			targetHeading += 360.0;
 		}
 		return targetHeading - robotHeading;
 	}
 
 	/**
 	 * Returns the number of degrees to turn right to reach the target heading.
 	 * 
 	 * @param robotHeading
 	 *            Current heading of the robot on the field
 	 * @param targetHeading
 	 *            The heading that points directly to the target. (0 degrees
 	 *            always points along the positive X-axis, CCW is positive)
 	 * @return Number of degrees to turn right to reach target heading.
 	 */
 	public static double getRightTurnHeadingDelta(double robotHeading,
 			double targetHeading) {
 		// Subtract degrees from robotHeading until it matches targetHeading.
 		if (targetHeading > robotHeading) {
 			targetHeading -= 360.0;
 		}
 		return robotHeading - targetHeading;
 	}
 
 	/**
 	 * Simple helper to determine the distance between two points.
 	 * 
 	 * @param x1
 	 *            Point 1's x value.
 	 * @param y1
 	 *            Point 1's y value.
 	 * @param x2
 	 *            Point 2's x value.
 	 * @param y2
 	 *            Point 2's y value.
 	 * @return Distance between point 1 and point 2.
 	 */
 	public static double getDistance(double x1, double y1, double x2, double y2) {
 		double deltaX = x1 - x2;
 		double deltaY = y1 - y2;
 		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
 	}
 
 	/**
 	 * Normalize any angle so that it's between -180 degrees to 180 degrees.
 	 * Useful if you have an angle like 390 degrees.
 	 * 
 	 * @param angle
 	 *            Original angle that is not normalized.
 	 * @return A normalized equivalent angle.
 	 */
 	public static double normalizeAngle(double angle) {
 		while (angle <= -180.0)
 			angle += 360.0;
 		while (angle > 180.0)
 			angle -= 360.0;
 		return angle;
 	}
 
 	// ------ Private helper methods used to calculate arc radius info -------
 	// These functions are used by the calculateArc function.
 	// They are not documented well because they are not public.
 	// Library users can't and won't use them.
 
 	/**
 	 * Numerical method used to find the radius of the pretend circle.
 	 */
 	static double bisectionForRadius(double lowerRadius, double upperRadius,
 			double errorTolerance, double robotX, double robotY,
 			double robotHeading, double targetX, double targetY) {
 		double middleRadius = (lowerRadius + upperRadius) / 2.0;
 		int iterationCounter = 0;
 		while (Math.abs(upperRadius - lowerRadius) > errorTolerance
 				&& iterationCounter < 100) {
 			middleRadius = (lowerRadius + upperRadius) / 2.0;
 			if (circleContainsTarget(middleRadius, robotX, robotY,
 					robotHeading, targetX, targetY)) {
 				// The middle circle DOES contain the point.
 				upperRadius = middleRadius;
 			} else {
 				// The middle circle DOES NOT contain the point.
 				lowerRadius = middleRadius;
 			}
 		}
 		if (iterationCounter >= 100) {
 			// CONSIDER: Might want to give a warning message. :)
 		}
 		return middleRadius;
 	}
 
 	/**
 	 * High level function to determine if the target is within the circle.
 	 */
 	static boolean circleContainsTarget(double radius, double robotX,
 			double robotY, double robotHeading, double targetX, double targetY) {
 		// Figure out h and k (the center of the circle) for this radius
 		double[] centerPoint = new double[2];
 		calculateCircleCenter(radius, robotX, robotY, robotHeading, centerPoint);
 		double targetDistanceToCenter = getDistance(centerPoint[0],
 				centerPoint[1], targetX, targetY);
 		return targetDistanceToCenter < Math.abs(radius);
 	}
 
 	/**
 	 * Find the center of the circle for a given radius.
 	 */
 	static void calculateCircleCenter(double radius, double robotX,
 			double robotY, double robotHeading, double[] centerPoint) {
 		// Find the angle from the robot to the circle center point.
 		double angleToCenter;
 		if (radius < 0) {
 			// Left turn so the center is left of the robot heading
 			angleToCenter = robotHeading + 90.0;
 		} else {
 			// Right turn so the center is right of the robot heading
 			angleToCenter = robotHeading - 90.0;
 		}
 		angleToCenter = normalizeAngle(angleToCenter);
 		centerPoint[0] = robotX + Math.abs(radius)
 				* Math.cos(Math.toRadians(angleToCenter));
 		centerPoint[1] = robotY + Math.abs(radius)
 				* Math.sin(Math.toRadians(angleToCenter));
 	}
 
 	/**
 	 * Calculates the arc length from the robot to the target using the known
 	 * radius. Use this function AFTER finding the correct radius.
 	 */
 	static double getArcLength(double radius, double robotX, double robotY,
 			double robotHeading, double targetX, double targetY) {
 		double[] centerPoint = new double[2];
 		calculateCircleCenter(radius, robotX, robotY, robotHeading, centerPoint);
 		double robotAngle = Math.atan2(robotY - centerPoint[1], robotX
 				- centerPoint[0]);
 		double targetAngle = Math.atan2(targetY - centerPoint[1], targetX
 				- centerPoint[0]);
		double arcAngle; // Note, arc angle Updated thanks to Dan Kahl.
		// Check if arc traveled crosses over negative X-axis
		if ((normalizeAngle(Math.round(robotHeading - Math.toDegrees(robotAngle))) == 90 && robotAngle > targetAngle)
				|| (normalizeAngle(Math.round(robotHeading - Math.toDegrees(robotAngle))) == -90 && robotAngle < targetAngle)) {
			arcAngle = (2 * Math.PI) - Math.abs(robotAngle - targetAngle);
		} else {
			arcAngle = Math.abs(robotAngle - targetAngle);
		}
 		return Math.abs(radius) * arcAngle;
 	}
 }
