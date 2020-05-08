 /**
  * @file Tournament.java
  * @author nward@fas.harvard.edu
  * @date 2012.03.17
  */
 
 package edu.harvard.seas.cs266.naptime;
 
 import sim.engine.SimState;
 import sim.field.continuous.Continuous2D;
 import sim.util.Double2D;
 
 /**
  * Implements a simulation of the robot foraging field.
  * 
  * @author nward@fas.harvard.edu
  */
 @SuppressWarnings("serial")
 public class Tournament extends SimState {
 	/**
 	 * Representation of 2-D space where robots will forage for food.
 	 */
 	public Continuous2D field = new Continuous2D(1.0, 220, 150);
 	
 	/**
 	 * Initial count of food particles in the field (may get parameterized).
 	 */
 	public int nTreats = 20;
 	
 	/**
 	 * Creates the simulation.
 	 * 
 	 * @param seed Seed for the simulation's RNG.
 	 */
 	public Tournament(long seed) {
 		super(seed);
 	}
 	
 	/**
 	 * Implements simulation initialization
 	 */
 	public void start() {
 		// Start the simulation
 		super.start();
 		
 		// Clear the field of food and robots
 		field.clear();
 		
 		// Add some randomly distributed food to the field
 		for (int t = 0; t < nTreats; t++) {
 			Treat treat = new Treat();
 			field.setObjectLocation(treat,
									new Double2D(field.getWidth()*random.nextDouble(),
												 field.getHeight()*random.nextDouble()));
 		}
 	}
 
 	/**
 	 * Runs simulation by invoking SimState.doLoop.
 	 * 
 	 * @param args MASON command-line args which we don't use.
 	 */
 	public static void main(String[] args) {
 		// Run the simulation using parent class convenience method.
 		doLoop(Tournament.class, args);
 		System.exit(0);
 	}
 
 }
