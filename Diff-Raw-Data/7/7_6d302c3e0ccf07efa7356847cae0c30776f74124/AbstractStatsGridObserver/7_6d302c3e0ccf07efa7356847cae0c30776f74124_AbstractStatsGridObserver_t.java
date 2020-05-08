 /**
  * AbstractStatsGridObserver.java
  * 
  * An observer that receives a grid
  */
 
 package edu.wheaton.simulator.datastructure;
 
 import java.util.Set;
 
 public abstract class AbstractStatsGridObserver implements GridObserver {
 
 	/**
 	 * Will need to be implemented by the Stats team. This method is called each
 	 * time the Grid loops
 	 * 
 	 * @param grid
 	 */
	@Override
 	public abstract void update(Grid grid);
 
 	@Override
 	public void update(Set<AgentAppearance> agents) {
 	}
 
 }
