 package de.unihalle.sim.util;
 
 public class MovementUtil {
 
	public static double metersPerSecond(double mps) {
 		return mps;
 	}
 
	public static double kilometersPerHour(double kmps) {
 		return kmps / 3.6d;
 	}
 
 	/**
 	 * Returns the time that is needed to travel the specified distance with the specified movement speed
 	 * 
 	 * @param distance
 	 * @param movementSpeed
 	 * @return time to travel the distance with the specified movement speed
 	 */
 	public static double calculateMovementTime(double distance, double movementSpeed) {
 		return distance / movementSpeed;
 	}
 
 }
