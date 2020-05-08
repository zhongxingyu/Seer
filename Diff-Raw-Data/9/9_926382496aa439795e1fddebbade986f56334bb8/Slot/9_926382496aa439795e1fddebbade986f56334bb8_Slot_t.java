 package edu.wheaton.simulator.entity;
 
import java.awt.Color;

 import edu.wheaton.simulator.simulation.Grid;
 
 /**
  * Slot.java
  * 
  * Class that defines properties for a specific point in the overall grid.
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  *         Wheaton College, CSCI 335, Spring 2013
  */
 
 public class Slot extends GridEntity {
 
 	/**
 	 * The Agent currently in this slot
 	 */
 	private Agent agent;
 
 	/**
	 * Constructor
 	 * 
 	 * @param g
 	 *            The Grid object
 	 */
 	public Slot(Grid g) {
		super(g);
 	}
 
 	/**
 	 * Adds a different Agent to this slot
 	 * 
 	 * @param a
 	 *            The new agent
 	 */
 	public void setAgent(Agent a) {
 		agent = a;
 	}
 
 	/**
 	 * Returns the agent currently in this slot
 	 * 
 	 * @return The Agent reference
 	 */
 	public Agent getAgent() {
 		return agent;
 	}
 
 }
