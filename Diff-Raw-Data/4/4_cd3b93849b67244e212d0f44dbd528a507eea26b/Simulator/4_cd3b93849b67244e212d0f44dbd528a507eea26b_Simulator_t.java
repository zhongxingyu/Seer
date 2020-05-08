 package balle.simulator;
 
 import balle.world.DataReader;
 
 /** A tool for testing the robot strategy without the need for a robot.
  * 
  * @author James Vaughan
  */
 public class Simulator implements DataReader {
 	
 	// -------------------\\
 	// Utility and Tools \\
 	// -------------------\\
 
 	private SoftBot yellow, blue;
 	
 	public SoftBot getYellow() {
 		return yellow;
 	}
 	
 	public SoftBot getBlue() {
 		return blue;
 	}
 	
	/** 
	 * Updates world object.
 	 */
 	@Override
 	public String nextLine() {
 		return "#";
 	}
 
 	/** A Main Method:
 	 *  	For testing the simulator.
 	 * 
 	 * @param args Input parameters
 	 */
 	public static void main(String[] args) {
 		System.out.println("Starting.");
 
 		Simulator sim = new Simulator();
 		sim.start();
 
 		System.out.println("Finished.");
 	}
 	
 	// Statics
 	private static int TIME_INCREMENTS = 100;
 
 	
 	/**
 	 * Starts the simulation running on a new thread.
 	 */
 	public final void start() {
 		if (thread == null) {
 			thread = new SimThread();
 			thread.start();
 		}
 		resume();
 	}
 
 	private int simTime = 0;
 	private boolean paused = false;
 
 	/**
 	 * Pauses the simulation so time no longer passes.
 	 */
 	public final void pause() {
 		paused = true;
 	}
 
 	/**
 	 * Resumes the simulation so time starts again.
 	 */
 	public final void resume() {
 		paused = false;
 	}
 
 	/**
 	 * Gets the current time of the match, since the start.
 	 * 
 	 * @return Duration in milliseconds.
 	 */
 	public final int getTime() {
 		return simTime;
 	}
 
 	private Thread thread = null;
 
 	class SimThread extends Thread {
 
 		/**
 		 * Moves the simulation forward one tick.
 		 * 
 		 */
 		@Override
 		public void run() {
 			while (true) {
 				if (!paused) {
 					simTime += TIME_INCREMENTS;
 				}
 
 				try {
 					Thread.sleep(TIME_INCREMENTS);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 	
 }
