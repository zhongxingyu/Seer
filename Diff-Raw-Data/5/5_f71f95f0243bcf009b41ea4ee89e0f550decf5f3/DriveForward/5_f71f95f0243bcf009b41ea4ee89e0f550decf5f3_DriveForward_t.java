 package edu.kit.curiosity.behaviors;
 
 import edu.kit.curiosity.Settings;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.*;
 
 /**
  * This class describes the Behavior to simple drive forward.
  * @author Team Curiosity
  *
  */
 public class DriveForward implements Behavior {
 	private DifferentialPilot pilot;
 	private boolean suppressed = false;
 	
 	/**
 	 * Constructs a new DriveForward Behavior
 	 */
 	public DriveForward() {
 		this.pilot = Settings.PILOT;
 	}
 
 	/**
 	 * Takes always Control.
 	 * returns true
 	 */
 	public boolean takeControl() {
 		return true;
 	}
 
 	/**
 	 * Initiates the cleanup when this Behavior is suppressed
 	 */
 	public void suppress() {
 		suppressed = true;
 	}
 
 	/**
 	 * Moves forward as long as this Behavior is active
 	 */
 	public void action() {
 		suppressed = false;
 		//pilot.forward();
 		while (!suppressed) {
			//if (!pilot.isMoving()) {
 				pilot.travel(10, true);
			//}
 		}
 		pilot.stop();
 	}
 }
