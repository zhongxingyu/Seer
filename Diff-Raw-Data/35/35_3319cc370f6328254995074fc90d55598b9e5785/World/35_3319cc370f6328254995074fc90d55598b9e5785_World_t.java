 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.model;
 
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.factory.PathFactory;
 import de.uniluebeck.imis.casi.simulation.factory.WorldFactory;
 
 /**
  * World Class that is kind of a root object for a Model tree
  * 
  * @author Marvin Frick, Tobias Mende
  * 
  */
 public class World {
 	/** The development logger */
 	private static final Logger log = Logger.getLogger(World.class.getName());
 	/** A set containing the rooms */
 	private Set<Room> rooms;
 	/** A set containing the agents */
 	private Set<Agent> agents;
 	/** A set containing the actuators */
 	private Set<AbstractInteractionComponent> interactionComponents;
 
 	/** Collection of components that are neither agents, actuators nor sensors */
 	private Set<AbstractComponent> components;
 	/** The start time in this world */
 	private SimulationTime startTime;
 
 	/** Background image for the simulation */
 	private Image backgroundImage;
 	/**
 	 * The door graph is a graph with doors as nodes. In this case its an
 	 * adjacency matrix that saves the distance between doors if they are in the
 	 * same room or <code>-1</code> otherwise.
 	 */
 	private double[][] doorGraph;
 	private Path[][] doorPaths;
 
 	/**
 	 * Flag for saving whether this world is sealed or not. If sealed, no
 	 * changes can be made. Every call to a setter would cause an exception. If
 	 * not sealed, the world can not be simulated.
 	 */
 	private boolean sealed;
 
 	/**
 	 * Seals the world
 	 * 
 	 * @throws IllegalAccessException
 	 */
 	public void seal() throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed yet");
 		}
 		sealed = true;
 	}
 
 	/**
 	 * Initializes the world
 	 */
 	public void init() {
 		if (doorGraph != null
 				|| SimulationEngine.getInstance().getWorld() == null) {
 			log.warning("Don't call init yet!");
 			return;
 		}
 		calculateDoorGraph();
 		printDoorGraph();
 		calculateDoorPaths();
 		
 		connectInteractionComponentsWithAgents();
 	}
 
 	/**
 	 * Adds all sensors and actuators as listeners to the agents.
 	 */
 	private void connectInteractionComponentsWithAgents() {
 		log.fine("Connecting agents with sensors and actuators");
 		for(Agent agent : agents) {
 			for(AbstractInteractionComponent comp : interactionComponents) {
				if(comp.checkInterest(agent)) {
					agent.addVetoableListener(comp);
				}
 			}
 		}
 		log.fine("All agents are connected");
 		
 	}
 
 	/**
 	 * Get all rooms, hold by this world
 	 * 
 	 * @return a collection of rooms
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Set<Room> getRooms() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return rooms;
 	}
 
 	/**
 	 * Get all agents that are part of this world.
 	 * 
 	 * @return a collection of agents
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Set<Agent> getAgents() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return agents;
 	}
 
 	/**
 	 * Getter for all actuators in this world
 	 * 
 	 * @return a collection of actuators
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Set<AbstractInteractionComponent> getInteractionComponents() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return interactionComponents;
 	}
 
 
 	/**
 	 * Getter for components that are neither actuators nor sensors.
 	 * 
 	 * @return a collection of unspecified components
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Set<AbstractComponent> getComponents() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return components;
 	}
 
 	/**
 	 * Getter for the startTime of this simulation
 	 * 
 	 * @return the start time
 	 */
 	public SimulationTime getStartTime() {
 
 		return startTime;
 	}
 
 	/**
 	 * Getter for the door graph
 	 * 
 	 * @return an adjacency matrix that holds the adjacencies of doors with the
 	 *         specific best case costs (the distance between two doors)
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 * 
 	 */
 	public double[][] getDoorGraph() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return doorGraph;
 	}
 
 	/**
 	 * Getter for a path between two adjacent doors
 	 * 
 	 * @param start
 	 *            the start door
 	 * @param end
 	 *            the end door
 	 * @return a path or <code>null</code> if no path was found.
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Path getDoorPath(Door start, Door end) throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		if (start.equals(end)) {
 			log.severe("Shouldn't call this method if doors are equal!");
 			return null;
 		}
 		Path path = doorPaths[start.getIntIdentifier()][end.getIntIdentifier()];
 		if (path == null) {
 			// Path didn't exist this way. try another way round
 			path = doorPaths[end.getIntIdentifier()][start.getIntIdentifier()];
 			// Reverse path if one is found
 			path = (path != null) ? doorPaths[end.getIntIdentifier()][start
 					.getIntIdentifier()].reversed() : null;
 		}
 		return path;
 	}
 
 	/**
 	 * Getter for the background image
 	 * 
 	 * @return the background image
 	 * @throws IllegalAccessException
 	 *             if the world isn't sealed
 	 */
 	public Image getBackgroundImage() throws IllegalAccessException {
 		if (!sealed) {
 			throw new IllegalAccessException("World isn't sealed!");
 		}
 		return backgroundImage;
 	}
 
 	/**
 	 * Method for checking whether this world is sealed or not.
 	 * 
 	 * @return <code>true</code> if the world is sealed, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isSealed() {
 		return sealed;
 	}
 
 	/**
 	 * Setter for the rooms
 	 * 
 	 * @param rooms
 	 *            a collection of rooms to set.
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setRooms(Set<Room> rooms) throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.rooms = rooms;
 	}
 
 	/**
 	 * Setter for agents
 	 * 
 	 * @param agents
 	 *            a collection of agents to set
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setAgents(Set<Agent> agents) throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.agents = agents;
 		for (Agent a : agents) {
 			SimulationClock.getInstance().addListener(a);
 		}
 	}
 
 	/**
 	 * Setter for actuators and sensors
 	 * 
 	 * @param interactionComponents
 	 *            a collection of actuators and sensors
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setInteractionComponents(Set<AbstractInteractionComponent> interactionComponents)
 			throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.interactionComponents = interactionComponents;
 	}
 
 
 	/**
 	 * Setter for unspecified components
 	 * 
 	 * @param components
 	 *            a collection of components that are neither actuators nor
 	 *            sensors
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setComponents(Set<AbstractComponent> components)
 			throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.components = components;
 	}
 
 	/**
 	 * Setter for the start time in this world
 	 * 
 	 * @param startTime
 	 *            the start time
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setStartTime(SimulationTime startTime)
 			throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.startTime = startTime;
 	}
 
 	/**
 	 * Setter for the background image behind the simulation
 	 * 
 	 * @param backgroundImage
 	 *            the background image to set
 	 * @throws IllegalAccessException
 	 *             if the world is sealed.
 	 */
 	public void setBackgroundImage(Image backgroundImage)
 			throws IllegalAccessException {
 		if (sealed) {
 			throw new IllegalAccessException("World is sealed!");
 		}
 		this.backgroundImage = backgroundImage;
 	}
 
 	/**
 	 * Calculates the adjacency matrix that represents the door graph.
 	 */
 	private void calculateDoorGraph() {
 		int size = Door.getNumberOfDoors();
 		doorGraph = new double[size][size];
 		initializeDoorGraph();
 		for (Room room : rooms) {
 			// Get a list of doors for each room
 			ArrayList<Door> doors = new ArrayList<Door>(room.getDoors());
 			// For each door in this room, calculate distances to all other
 			// doors of this room.
 			log.config("Doors in " + room + ": " + doors);
 			calculateAdjacencies(doors);
 		}
 	}
 
 	/**
 	 * Calculates the adjacencies and costs between doors in a given list and
 	 * stores them in the doorGraph
 	 * 
 	 * @param doors
 	 *            the list of doors
 	 */
 	private void calculateAdjacencies(ArrayList<Door> doors) {
 		for (Door first : doors) {
 			for (Door second : doors) {
 				if (first.equals(second)) {
 					doorGraph[first.getIntIdentifier()][second
 							.getIntIdentifier()] = 0.0;
 					continue;
 				}
 				doorGraph[first.getIntIdentifier()][second.getIntIdentifier()] = 1;
 			}
 		}
 
 	}
 
 	/**
 	 * Sets all distances to <code>Double.NEGATIVE_INFINITY</code>, meaning that
 	 * the doors arn't adjacent.
 	 */
 	private void initializeDoorGraph() {
 		log.info("Initializing Doorgraph");
 		int size = doorGraph.length;
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				doorGraph[i][j] = Double.NEGATIVE_INFINITY;
 			}
 		}
 	}
 
 	/**
 	 * Calculates the paths from each door to all adjacent other doors. And
 	 * saves them in the doorPaths matrix
 	 */
 	private void calculateDoorPaths() {
 		doorPaths = new Path[doorGraph.length][doorGraph.length];
 		for (int i = 0; i < doorPaths.length; i++) {
 			for (int j = 0; j < i; j++) {
 				if (i == j || doorGraph[i][j] <= 0) {
 					// Doors are equal or not adjacent
 					doorPaths[i][j] = null;
 					continue;
 				}
 				Door from = WorldFactory.findDoorForIdentifier(i);
 				Door to = WorldFactory.findDoorForIdentifier(j);
 				if (from != null && to != null) {
 					doorPaths[i][j] = PathFactory.findPath(from, to);
 				} else {
 					doorPaths[i][j] = null;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Prints the adjacency matrix of doors
 	 */
 	private void printDoorGraph() {
 		log.fine("The door graph:");
 		StringBuffer head = new StringBuffer();
 		head.append("\t ");
 		for (int j = 0; j < doorGraph.length; j++) {
 			head.append("d-" + j + "\t ");
 		}
 		log.fine(head.toString());
 		for (int i = 0; i < doorGraph.length; i++) {
 			StringBuffer b = new StringBuffer();
 			b.append("d-" + i + ":\t ");
 			for (int j = 0; j < doorGraph.length; j++) {
 				b.append(doorGraph[i][j] + "\t ");
 			}
 			log.fine(b.toString());
 		}
 	}
 }
