 package pudgewars.util;
 
 public class Time {
	public final static int TICKS_PER_SECOND = 60;
 
 	// # of seconds between ticks
 	private static double tickInterval = 1.0f / TICKS_PER_SECOND;
 
 	// # of ticks since the start of game
 	public static int totalTicks = 0;
 
 	// Time multiplier
 	public static double timeScale = 1;
 
 	// # of seconds since start of game.
 	// Note: Based off of totalTicks
 	public static double timeSinceStart() {
 		return totalTicks * tickInterval;
 	}
 
 	public static double getTickInterval() {
 		return tickInterval * timeScale;
 	}
 
 	public static double getBaseTickInterval() {
 		return tickInterval;
 	}
 }
