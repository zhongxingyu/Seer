 /**
  * Agent.java
  *
  * Agents model actors in the simulation's Grid.
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 
 public class Agent extends GridEntity {
 
 	/**
 	 * The list of all triggers/events associated with this agent.
 	 */
 	private List<Trigger> triggers;
 
 	/**
 	 * Prototype of the agent
 	 */
 	private Prototype prototype;
 	
 	private final AgentID id = new AgentID();
 
 	private int currentTriggerIndex = 0;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * Makes an agent with the default gridEntity color
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype) {
 		super(g);
 		init(prototype);
 	}
 
 	/**
 	 * Constructor. Makes an agent with a solid color
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this agent (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype, Color c) {
 		super(g, c);
 		init(prototype);
 	}
 
 	/**
 	 * Constructor. Makes an agent with custom color and color map
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this agent (passed to super constructor)
 	 * @param d
 	 *            The design for this agent (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype, Color c, byte[] d) {
 		super(g, c, d);
 		init(prototype);
 	}
 
 	private void init(Prototype p) {
 		triggers = new ArrayList<Trigger>();
 		prototype = p;
 	}
 
 	/**
 	 * Causes this Agent to perform 1 action. All triggers with valid
 	 * conditions will fire.
 	 * 
 	 * @throws Exception
 	 */
 	public void act() throws SimulationPauseException {
 		for (Trigger t : triggers)
 			try {
 				t.evaluate(this);
 			} catch (EvaluationException e) {
 				System.err.println(e.getMessage());
 				String errorMessage = "Error in Agent: " + this.getName()
 						+ "\n ID: " + this.getID() + "\n Trigger: "
 						+ t.getName() + "\n MSG: " + e.getMessage()
 						+ "\n condition: " + t.getConditions().toString();
 				throw new SimulationPauseException(errorMessage);
 			}
 	}
 
 	/**
 	 * Causes this Agent to perform only the triggers of the input priority
 	 * 
 	 * @param priority: the priority of the triggers evaluated
 	 * @throws SimulationPauseException
 	 */
 	public void priorityAct(int priority) throws SimulationPauseException {
		if (triggers.get(currentTriggerIndex).getPriority() > priority) {
 			currentTriggerIndex = 0;
 		}
 		for (int i = currentTriggerIndex; i < triggers.size(); i++) {
 			Trigger t = triggers.get(i);
 			if (t.getPriority() == priority) {
 				try {
 						t.evaluate(this);
 					} catch (EvaluationException e) {
 						System.err.println(e.getMessage());
 						String errorMessage = "Error in Agent: " + this.getName()
 								+ "\n ID: " + this.getID() + "\n Trigger: "
 								+ t.getName() + "\n MSG: " + e.getMessage()
 								+ "\n condition: " + t.getConditions().toString();
 						throw new SimulationPauseException(errorMessage);
 					}
 			}
 			else if (t.getPriority() > priority) {
 				currentTriggerIndex = i - 1;
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Removes this Agent from the environment's list.
 	 */
 	public void die() {
 		getGrid().removeAgent(getPosX(), getPosY());
 	}
 
 	/**
 	 * Adds to the Agent's list of triggers
 	 * 
 	 * @param trigger
 	 *            The trigger to add
 	 */
 	public void addTrigger(Trigger trigger) {
 		triggers.add(trigger);
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Removes a trigger with the given priority (index in array list)
 	 * 
 	 * @param priority
 	 *            The priority of the given trigger to remove.
 	 */
 	public void removeTrigger(int priority) {
 		triggers.remove(triggers.get(priority));
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Removes a trigger with the given name.
 	 * 
 	 * @param name
 	 */
 	public void removeTrigger(String name) {
 		for (int i = 0; i < triggers.size(); i++)
 			if (getTriggerName(i).equals(name))
 				triggers.remove(i);
 	}
 
 	/**
 	 * Updates the trigger(s) with the given name
 	 * 
 	 * @param name
 	 */
 	public void updateTrigger(String name, Trigger newT) {
 		for (int i = 0; i < triggers.size(); i++)
 			if (getTriggerName(i).equals(name))
 				triggers.set(i, newT);
 	}
 
 	private String getTriggerName(int index) {
 		return triggers.get(index).getName();
 	}
 
 	/**
 	 * Gets the current x position of this agent
 	 * 
 	 * @return x
 	 */
 	public int getPosX() {
 		return getField("x").getIntValue();
 	}
 
 	/**
 	 * Gets the current y position of this agent
 	 * 
 	 * @return y
 	 */
 	public int getPosY() {
 		return getField("y").getIntValue();
 	}
 
 	/**
 	 * Sets the Agent's new position
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void setPos(int x, int y) {
 		updateField("x", x + "");
 		updateField("y", y + "");
 	}
 
 	public Prototype getPrototype() {
 		return prototype;
 	}
 
 	public String getName() {
 		return getPrototype().getName();
 	}
 	
 	public AgentID getID() {
 		return id;
 	}
 }
