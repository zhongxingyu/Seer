 package chalmers.dax021308.ecosystem.model.population;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 
 /**
  * 
  * @author Henrik
  * Abstract class for handling the dummy methods
  */
 public abstract class AbstractPopulation implements IPopulation {
 	protected List<IAgent> agents;
 	protected Dimension gridDimension;
 	protected int capacity = Integer.MAX_VALUE;
 	protected List<IPopulation> preys;
 	protected List<IPopulation> predators;
 	protected List<IPopulation> neutral;
 	protected List<IAgent> removeList;
 	private String name;
 
 	public AbstractPopulation() {
 		preys = new ArrayList<IPopulation>();
 		predators = new ArrayList<IPopulation>();
 		neutral = new ArrayList<IPopulation>();
 		removeList = new ArrayList<IAgent>();
 	}
 	
 	public AbstractPopulation(String name, Dimension gridDimension) {
 		this();
 		this.name = name;
 		this.gridDimension = gridDimension;
 	}
 
 	public AbstractPopulation(String name, Dimension gridDimension, int capacity) {
 		this(name, gridDimension);
 		this.capacity = capacity;
 	}
 	
 	/**
 	 * Clone constructor.
 	 * <p>
 	 * Use only for cloning.
 	 * 
 	 * @param original
 	 *            the AbstractPopulation to clone
 	 */
 	public AbstractPopulation(AbstractPopulation original) {
 		this.gridDimension = original.gridDimension;
 		this.name = original.name;
 		preys = new ArrayList<IPopulation>();
 		predators = new ArrayList<IPopulation>();
 		neutral = new ArrayList<IPopulation>();
 		agents = new ArrayList<IAgent>();
 		for (IAgent a : original.agents) {
 			try {
 				agents.add(a.cloneAgent());
 			} catch (CloneNotSupportedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	@Override
 	public void update() {
 		for (IAgent a : agents) {
 			a.calculateNextPosition(predators, preys, neutral, gridDimension);
 			//Log.v("Updating" + a.getName());
 		}
 		List<IAgent> kids = new ArrayList<IAgent>();
 		int populationSize = agents.size();
 		for (IAgent a : agents) {
 			a.updatePosition();
 			List<IAgent> spawn = a.reproduce(null,populationSize);
 			if (spawn != null) {
 				kids.addAll(spawn);
 			}
 		}
 		if (kids != null)
 			agents.addAll(kids);
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public List<IAgent> getAgents() {
 		return agents;
 	}
 
 	@Override
 	public List<IPopulation> getPredators() {
 		return predators;
 	}
 
 	@Override
 	public void addPredator(IPopulation predator) {
 		predators.add(predator);
 	}
 
 	@Override
 	public List<IPopulation> getPreys() {
 		return preys;
 	}
 
 	@Override
 	public void addPrey(IPopulation prey) {
 		preys.add(prey);
 	}
 
 	@Override
 	public List<IPopulation> getNeutralPopulations() {
 		return neutral;
 	}
 
 	@Override
 	public void addNeutralPopulation(IPopulation neutral) {
 		this.neutral.add(neutral);
 	}
 
 	@Override
 	public IPopulation clonePopulation() {
 		return new AbstractPopulation(this) {
 			@Override
 			public double calculateFitness(IAgent agent) {
 				return 0;
 			}
 		};
 	}
 	
 	/**
 	 * Cleares out the agents in the removeList.
 	 * <p>
 	 * Warning! Use only when no other thread is iterating of the agentlist.
 	 */
 	public void removeAgentsFromRemoveList() {
 		IAgent a;
 		for(int i = 0 ; i < removeList.size() ; i++)  {
 			a = removeList.get(i);
 			agents.remove(a);
 		}
 		removeList.clear();
 	}
 
 
 	@Override
 	public synchronized void addToRemoveList(IAgent a) {
 		removeList.add(a);
 	}
 }
