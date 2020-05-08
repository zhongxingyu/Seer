 /**
  * Prototype.java
  *
  * A template for an Agent
  * 
  * @author Elliot Penson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import edu.wheaton.simulator.simulation.Grid;
 
 public class Prototype extends GridEntity {
 
 	/**
 	 * The list of all Agent children of this Prototype
 	 */
 	private List<Agent> children;
 
 	/**
 	 * HashMap of Prototypes with associated names
 	 */
 	private static HashMap<String, Prototype> prototypes = new HashMap<String, Prototype>();
 
 	/**
 	 * The list of all triggers/events associated with this prototype.
 	 */
 	private List<Trigger> triggers;
 	
 	private final PrototypeID id;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 */
 	public Prototype(Grid g, Color c) {
 		super(g, c);
 		id = new PrototypeID();
 		children = new ArrayList<Agent>();
 		triggers = new ArrayList<Trigger>();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 * @param d
 	 *            The bitmask of this prototype (passed to super constructor)
 	 */
 	public Prototype(Grid g, Color c, byte[] d) {
 		super(g, c, d);
		id = new PrototypeID();
 		children = new ArrayList<Agent>();
		triggers = new ArrayList<Trigger>();
 	}
 
 	/**
 	 * Adds a Prototype to the HashMap
 	 * 
 	 * @param n
 	 * @param g
 	 * @param c
 	 */
 	public static void addPrototype(String n, Prototype p) {
 		prototypes.put(n, p);
 	}
 
 	/**
 	 * Returns the Prototype that corresponds to the given string.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public static Prototype getPrototype(String n) {
 		return prototypes.get(n);
 	}
 
 	/**
 	 * Gets a Set of the prototype names
 	 * 
 	 * @return
 	 */
 	public static Set<String> prototypeNames() {
 		return prototypes.keySet();
 	}
 
 	/**
 	 * Does a deep clone of this prototype and returns it as an Agent.
 	 * 
 	 * @return The clone of this prototype
 	 */
 	public Agent clonePrototype() {
 		Agent clone = new Agent(getGrid(), getColor(), getDesign());
 
 		// copy all fields
 		clone.getFieldMap().putAll(this.getFieldMap());
 
 		// copy all triggers
 		for (Trigger t : triggers)
 			clone.addTrigger(new Trigger(t));
 
 		children.add(clone);
 		return clone;
 	}
 
 	public void addTrigger(Trigger trigger) {
 		triggers.add(trigger);
 		Collections.sort(triggers);
 		for (Agent a : children) {
 			a.addTrigger(trigger);
 		}
 	}
 
 	/**
 	 * Removes a trigger with the given priority all children.
 	 * 
 	 * @param priority
 	 *            The priority of the given trigger to remove.
 	 */
 	public void removeTrigger(int priority) {
 		triggers.remove(triggers.get(priority));
 		Collections.sort(triggers);
 		for (Agent a : children) {
 			a.removeTrigger(priority);
 		}
 	}
 	
 	/**
 	 * Provides a number of this children this prototype currently has.
 	 * @return The size of the children List
 	 */
 	public int childPopulation() {
 		return children.size();
 	}
 
 	public PrototypeID getPrototypeID(){
 		return id;
 	}
 }
