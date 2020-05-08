 /**
  * Simulation.java
  * 
  * A single directional class for simulation. Once the simulation 
  * is completed, a new simulation must be made.
  *
  * @author Agent Team
  */
 
 package edu.wheaton.simulator.simulation;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.StatisticsManager;
 
 public class Simulation {
 
 	/**
 	 * Name of the simulation
 	 */
 	private String name;
 
 	/**
 	 * The Grid to hold all the Agents
 	 */
 	private Grid grid;
 
 	/**
 	 * Whether or not the simulation has begun
 	 */
 	private AtomicBoolean isStarted;
 
 	/**
 	 * Ending conditions for the loop
 	 */
 	private SimulationEnder ender;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 * @param gridX
 	 * @param gridY
 	 * @param se
 	 */
 	public Simulation(String name, int gridX, int gridY, SimulationEnder ender) {
 		this.name = name;
 		grid = new Grid(gridX, gridY);
 		isStarted = new AtomicBoolean(false);
 		this.ender = ender;
		StatisticsManager.getInstance().initialize(grid, ender);
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 * @param grid
 	 * @param se
 	 */
 	public Simulation(String name, Grid grid, SimulationEnder ender) {
 		this.name = name;
 		this.grid = grid;
 		isStarted = new AtomicBoolean(false);
 		this.ender = ender;
		StatisticsManager.getInstance().initialize(grid, ender);
 	}
 
 	/**
 	 * Causes all the triggers of all the entities in the simulator's grid to be
 	 * fired
 	 * 
 	 * @throws SimulationPauseException
 	 */
 	public void updateEntities() throws SimulationPauseException {
 		grid.updateEntities();
 	}
 
 	/**
 	 * Tells the grid to stop on the next iteration if the ender evaluates to
 	 * true
 	 * 
 	 * @return
 	 */
 	public boolean shouldEnd() {
 		return ender.evaluate(grid);
 	}
 
 	/**
 	 * Returns whether or not the simulation has begun
 	 */
 	public boolean getStarted() {
 		return isStarted.get();
 	}
 
 	/**
 	 * Let's the simulation know it has begun
 	 */
 	public void setStarted() {
 		isStarted.set(true);
 	}
 
 	/**
 	 * Notifies all the observers following this simulation's grid
 	 * 
 	 * @param layerRunning
 	 */
 	public void notifyObservers(boolean layerRunning) {
 		grid.notifyObservers(layerRunning);
 	}
 
 	/**
 	 * Provides this simulator's name
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Provides the Grid the Facade is using
 	 * 
 	 * @return Grid object
 	 */
 	public Grid getGrid() {
 		return grid;
 	}
 }
