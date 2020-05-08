 package edu.wheaton.simulator.statistics;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
import edu.wheaton.simulator.datastructure.AbstractStatsGridObserver;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.entity.TriggerObserver;
 
 /**
  * @author Daniel Gill, Nico Lasta
  * 
  */
public class Recorder extends AbstractStatsGridObserver implements TriggerObserver {
 
 	private StatisticsManager statManager;
 
 	private Prototype gridPrototype;
 	
 	private static HashMap<AgentID, ArrayList<TriggerSnapshot>> triggers;
 
 	/**
 	 * Constructor.
 	 */
 	public Recorder(StatisticsManager statManager) {
 		this.statManager = statManager;
 		gridPrototype = null;
 		Recorder.triggers = new HashMap<AgentID, ArrayList<TriggerSnapshot>>();
 	}
 
 	/**
 	 * Record a step of the simulation.
 	 * 
 	 * @param grid
 	 *            The grid at the point in time corresponding to step.
 	 * @param step
 	 *            The point in the simulation being recorded.
 	 * @param prototypes
 	 */
 	@Override
 	public void update(Grid grid) {
 		Collection<Prototype> prototypes = Prototype.getPrototypes();
 		
 		for (Prototype prototype : prototypes)
 			StatisticsManager.addPrototypeSnapshot(SnapshotFactory
 					.makePrototypeSnapshot(prototype));
 		
 		for (Agent agent : grid) {
 			if (agent != null)
 				statManager.addGridEntity(SnapshotFactory.makeAgentSnapshot(
 						agent, triggers.get(agent.getID()), grid.getStep()));
 		}
 		
 		if(gridPrototype == null) {
 			gridPrototype = new Prototype("GRID");
 			statManager.addGridEntity(SnapshotFactory.makeGlobalVarSnapshot(grid, gridPrototype, grid.getStep()));
 		}
 		else
 			statManager.addGridEntity(SnapshotFactory.makeGlobalVarSnapshot(grid, gridPrototype, grid.getStep()));
 		
 		triggers.clear();
 	}
 
 	/**
 	 * TODO Fix documentation if necessary
 	 * 
 	 * Called by behaviors whenever a trigger is activated.
 	 * 
 	 * @param actor
 	 *            The agent acting
 	 * @param behavior
 	 *            The behavior of the agent
 	 * @param recipient
 	 *            The agent that is being acted upon
 	 * @param step
 	 *            The step that this method is called in
 	 */
 	@Override
 	public void update(AgentID caller, Trigger trigger, int step) {
 		TriggerSnapshot triggerSnap = SnapshotFactory.makeTriggerSnapshot(
 				trigger.getName(), trigger.getPriority(), null, null);
 		
 		if (triggers.containsKey(caller)) {
 			triggers.get(caller).add(triggerSnap);
 		} else {
 			ArrayList<TriggerSnapshot> triggerList = new ArrayList<TriggerSnapshot>();
 			triggerList.add(triggerSnap);
 			triggers.put(caller, triggerList);
 		}
 	}
 }
