 package chalmers.dax021308.ecosystem.model.population;
 
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.agent.AbstractAgent;
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
import chalmers.dax021308.ecosystem.model.util.AgentContainer;
 
 /**
  * Describes the general configuration of an arbitrary population.
  * 
  * @author Albin
  */
 public interface IPopulation extends Cloneable {
 	
 	/**
 	 * Handles things that should be updated regularly. 
 	 */
 	public void update();
 	
 	/**
 	 * @param agent - The IAgent for which the fitness will be calculated.
 	 * @return The fitness for the provided IAgent.
 	 */
 	public double calculateFitness(IAgent agent);
 	
 	/**
 	 * @return The name of the population.
 	 */
 	public String getName();
 	
 	/**
 	 * @return The whole population of IAgents.
 	 */
 	public List<IAgent> getAgents();
 	
 	/**
 	 * @return The predators that prey on a population. 
 	 */
 	public List<IPopulation> getPredators();
 	
 	/**
 	 * @param predator - Population of a predator.
 	 */
 	public void addPredator(IPopulation predator);
 	
 	/**
 	 * @return The preys that a predator population eats.
 	 */
 	public List<IPopulation> getPreys();
 	
 	/**
 	 * @param prey - Population of a prey.
 	 */
 	public void addPrey(IPopulation prey);
 	
 	/**
 	 * @param prey - Add a neutral population to this.
 	 */
 	public void addNeutralPopulation(IPopulation neutral);
 	
 	/**
 	 * @return A list of the neutral populations.
 	 */
 	public List<IPopulation> getNeutralPopulations();
 	
 	/**
 	 * @return clone a population. Uses {@link AbstractAgent#cloneAgent()}
 	 */
 	public IPopulation clonePopulation();
 	
 	/**
 	 * Removes the listed agents from the remove list.
 	 */
 	public void removeAgentsFromRemoveList();
 
 	/**
 	 * Add this agent to the remove list, a list of agents that are to be removed next iteration.
 	 * <p>
 	 * Warning! This is suppose to be a thread-safe method!
 	 */
 	public void addToRemoveList(IAgent a);
 	
 }
