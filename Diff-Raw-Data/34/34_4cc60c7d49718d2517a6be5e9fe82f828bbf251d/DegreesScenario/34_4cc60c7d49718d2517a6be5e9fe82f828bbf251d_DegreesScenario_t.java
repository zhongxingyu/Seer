 package de.tu_darmstadt.kom.mobilitySimulator.scenarios;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import net.sf.ehcache.CacheManager;
 
 import de.tu_darmstadt.kom.linkedRTree.Rectangle;
 
 import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FireEngineRole;
 import de.tu_darmstadt.kom.mobilitySimulator.agent.role.RescueVehicleRole;
 import de.tu_darmstadt.kom.mobilitySimulator.agent.role.SocialNetworkerRole;
 import de.tu_darmstadt.kom.mobilitySimulator.core.AbstractScenario;
 import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
 import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
 import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
 import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
 import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
 import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
 import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;
 import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
 import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
 import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgentRepository;
 import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeCollapsedBuildingMapEvent;
 import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;
 import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEventRepository;
 import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;
 import de.tu_darmstadt.kom.mobilitySimulator.output.AgentDensityEvaluationOutput;
 import de.tu_darmstadt.kom.mobilitySimulator.output.GenericFileOutput;
 import de.tu_darmstadt.kom.mobilitySimulator.output.VictimOutput;
 
 public class DegreesScenario extends AbstractScenario {
 
 	private int initialSeed = 123456;
 	/** Do multiple simulations with different seeds after each other */
 	private boolean doMultipleRuns = false;
 	private int amountOfSimulationRuns = 20;
 
 	/** Total number of agents (nodes) on the map. */
 	private int numberOfAgents = 200;
 
 	// In our scenario, we only care about connected agents, so we set to 100%
 	private int percentageOfMobileComEnabledAgents = 100;
 
 	/** Total simulation length (in seconds). */
 	private int simulationLength = 60 * 60 * 3; // In seconds
 	private float simulationSpeed = Scheduler.MAX_SPEED;
 
 	/** Used map file. Also defines how large the map is. */
 	private String mapFile = "frankfurt3.png";
 
 	private final File MAPS = new File("maps");
 	private final File TRACES = new File("output/"+ ScenesScenario.class.getName());
 
 	//TODO: Figure out how to get this number for real
 	private final double PIXELS_TO_MILES = 1000;
 
	private final double PROPORTION_OF_FRIENDS_WITHIN_SIM_WINDOW = 0.1;

 	private Map<Rectangle, Integer> agentBuckets;
 
 	@Override
 	protected OutputInterface getOutput() {
 
 		OutputInterface outputHelper = null;
 
 		String prefix = numberOfAgents + "agents_" + simulationLength + "sec";
 		int index = 0;
 
 		File newTrace = new File(TRACES, prefix);
 
 		// Generic File Output
 		if (!TRACES.exists())
 			if (!TRACES.mkdir())
 				System.err.println("Cannot create folder "
 						+ TRACES.getAbsolutePath());
 
 		// Generic File Output
 		if (!newTrace.exists())
 			if (!newTrace.mkdir())
 				System.err.println("Cannot create folder "
 						+ newTrace.getAbsolutePath());
 
 		System.out.println("Output Folder: " + newTrace);
 
 		prefix = "";
 
 		try {
 			// show map size in filename might be good
 			File outputFile = new File(newTrace, prefix + "trace.txt");
 
 			while (!outputFile.createNewFile()) {
 				index++;
 				outputFile = new File(newTrace, prefix + "trace(" + index
 						+ ").txt");
 			}
 			new Thread((GenericFileOutput) outputHelper);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// I guess we are getting some output of the normal agents here?
 		Collection<String> specialAgents = new ArrayList<String>();
 		specialAgents.add("NormalRole");
 		
 		outputHelper = new AgentDensityEvaluationOutput(newTrace, index,
 				prefix, specialAgents);
 
 		return outputHelper;
 	}
 
 	@Override
 	protected String getMapFile() {
 		return new File(MAPS, mapFile).getAbsolutePath();
 	}
 
 	@Override
 	protected MapFactory getMapFactory() {
 		return new EHCacheMapFactory();
 	}
 
 	@Override
 	protected AbstractAgentRepository getAgentRepository() {
 		return new LinkedRTreeAgentRepository(DiscreteMap.sizeX,
 				DiscreteMap.sizeY, numberOfAgents, true);
 	}
 
 	@Override
 	protected AbstractMapEventRepository getMapEventRepository() {
 		return new LinkedRTreeMapEventRepository(DiscreteMap.sizeX,
 				DiscreteMap.sizeY);
 	}
 
 	@Override
 	protected int getSimulationLenght() {
 		return simulationLength;
 	}
 
 	@Override
 	protected int getSecondsPerCycle() {
 		return 1;
 	}
 
 	@Override
 	protected String getScenarioName() {
 		return "";
 	}
 
 	@Override
 	protected float getSimulationSpeed() {
 		return simulationSpeed;
 	}
 
 	public DegreesScenario() {
 
 		// Setup cache
 		CacheManager.getInstance().addCache("largeMemoryNoDisk");
 
 		if (doMultipleRuns) {
 			System.err
 					.println("Simulation started.");
 
 			String prefix = numberOfAgents + "agents_" + simulationLength
 					+ "sec";
 			int index = 0;
 
 			File newTrace = new File(TRACES, prefix);
 
 			// Generic File Output
 			if (!TRACES.exists())
 				if (!TRACES.mkdir())
 					System.err.println("Cannot create folder "
 							+ TRACES.getAbsolutePath());
 
 			// Generic File Output
 			if (!newTrace.exists())
 				if (!newTrace.mkdir())
 					System.err.println("Cannot create folder "
 							+ newTrace.getAbsolutePath());
 
 			try {
 				File resultFile = new File(newTrace, prefix + "_consoleLog.txt");
 
 				while (!resultFile.createNewFile()) {
 					index++;
 					resultFile = new File(newTrace, prefix + "_consoleLog("
 							+ index + ").txt");
 				}
 				System.setOut(new PrintStream(new FileOutputStream(resultFile,
 						true), false));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("####### agents," + numberOfAgents
 				+ "; run,1 - MobComEnabledPercentage,"
 				+ percentageOfMobileComEnabledAgents + "; seed," + initialSeed
 				+ " ##########");
 		System.err.println("####### agents," + numberOfAgents
 				+ "; run,1 - MobComEnabledPercentage,"
 				+ percentageOfMobileComEnabledAgents + "; seed," + initialSeed
 				+ " ##########");
 		run(initialSeed);
 
 		if (doMultipleRuns) {
 
 			for (int i = 2; i <= amountOfSimulationRuns; i++) {
 
 				Watchdog.clearInstance();
 				Scheduler.clearInstance();
 				// MapFactory.clearInstance();
 				Runtime.getRuntime().gc();
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				Watchdog.clearInstance();
 
 				int seed = Scheduler.rand.nextInt();
 
 				System.out.println("####### agents," + numberOfAgents
 						+ "; run," + i + " - MobComEnabledPercentage,"
 						+ percentageOfMobileComEnabledAgents + "; seed," + seed
 						+ " ##########");
 				System.err.println("####### agents," + numberOfAgents
 						+ "; run," + i + " - MobComEnabledPercentage,"
 						+ percentageOfMobileComEnabledAgents + "; seed," + seed
 						+ " ##########");
 
 				run(seed);
 			}
 		}
 
 		System.out.println("All simulations terminated");
 
 		System.exit(0);
 
 	}
 
 	private int generateNumberOfFriends() {
		return (int) Math.min(PROPORTION_OF_FRIENDS_WITHIN_SIM_WINDOW*(Math.exp(Math.log((Scheduler.rand.nextDouble()-5.40996*Math.pow(24.0,-0.502))/-5.40996)/-0.502) - 24), numberOfAgents-1);
 	}
 
 
 	private double calculateFriendshipByGender(boolean isAgentMale, boolean isFriendMale) {
 		
 		if(isAgentMale && isFriendMale) {
 			return 0.4869;
 		} else if(!isAgentMale && !isFriendMale) {
 			return 0.5178;
 		} else if(!isAgentMale && isFriendMale) {
 			return 0.4822;
 		} else {
 			return 0.5131;
 		}
 	}
 
 	private double calculateFriendshipByAge(int agentAge, int friendAge) {
 
 		//Row is the age from 20 to 60 of the agent, and the column is the age of the friend
 		double ageProbabilityMatrix[][] = {
 			{0.80, 0.1, 0.033, 0.033, 0.033},
 			{0.25, 0.50, 0.15,  0.05, 0.05},
 			{0.2, 0.2, 0.5, 0.05, 0.05},
 			{0.2, 0.2, 0.2, 0.3, 0.1},
 			{0.175, 0.175, 0.175, 0.175, 0.3}
 		};
 
 		return ageProbabilityMatrix[(agentAge/10)-2][(friendAge/10)-2];
 
 	}
 
	private double calculateFriendshipProbability(AbstractAgent originAgent, AbstractAgent potentialFriend) {
 
 		double distance = Math.sqrt(Math.pow(originAgent.getX()-potentialFriend.getX(),2)+Math.pow(originAgent.getY()-potentialFriend.getY(),2)) / PIXELS_TO_MILES;
 				
 		boolean isAgentMale = originAgent.isMale;
 		boolean isFriendMale = potentialFriend.isMale;
 
 		int agentAge = originAgent.age;
 		int friendAge = potentialFriend.age;
 
 		return Math.pow(0.195716+distance,-1.050)*calculateFriendshipByGender(isAgentMale, isFriendMale)*calculateFriendshipByAge(agentAge, friendAge);
 
 	}
 
 	@Override
 	public void preRun() {
 
 		AbstractAgent a = null;
 
 		// Normal Agents
 		for (int i = 0; i < numberOfAgents; i++) {
 			// TODO, Create agent on map also?
 			a = createAgentInGreen((Scheduler.rand.nextInt(100)) < percentageOfMobileComEnabledAgents);
 			a.setRole(new SocialNetworkerRole(a));
 
 			// Set the demographics/properties of the agent
 			//Half and half male or female
 			a.isMale = Scheduler.rand.nextBoolean();
 
 			// Ages 20-60, only use the decade number instead of a more specific age
 			a.age = (Scheduler.rand.nextInt(5)+2)*10;
 
 			Scheduler.agentRepository.put(a);
 		}
 
 		Scheduler.agentRepository.executePut();
 		System.out.println(numberOfAgents + " agents initialized.");
 
 		// Iterate over the values
 		for(AbstractAgent agent : (Collection<AbstractAgent>) Scheduler.agentRepository.values()) {
 			SocialNetworkerRole socialNetRole = (SocialNetworkerRole) agent.getRole();
 			// Pick a number of friends for the agent, and then see how many they have already been assigned
 			int additionalFriends = generateNumberOfFriends() - socialNetRole.getFriends().size();

 			for(int i = 0 ; i < additionalFriends; i++) {
 				
 				//Set to keep the probabilities of friendship
 				HashMap<AbstractAgent,Double> probabilities = new HashMap<AbstractAgent,Double>();
 
 				//Iterate through each user to get their probability of being a friend
				for(AbstractAgent candidateFriend : (Collection<AbstractAgent>) Scheduler.agentRepository.values()) {
					if(!socialNetRole.getFriends().contains(candidateFriend) && agent != candidateFriend) {
						probabilities.put(candidateFriend,calculateFriendshipProbability(agent,candidateFriend));
 					}
 				}
 
 				// Get the sum of the probabilities, so we can scale them all back
 				double totalProbability = 0.0;
 				for(Double probability : probabilities.values()) {
 					totalProbability += probability;
 				}
 
 				// Randomly guess a number from 0 to 1
 				double randomFriendChoice = Scheduler.rand.nextDouble() * totalProbability;
 
 				// Now go through the agents and choose based on probability
 				double cumulativeProbability = 0.0;
				for(AbstractAgent candidateFriend : probabilities.keySet()) {
					cumulativeProbability += probabilities.get(candidateFriend);
 					if(cumulativeProbability >= randomFriendChoice) {
						// Set both friends
						socialNetRole.getFriends().add(candidateFriend);
						((SocialNetworkerRole) candidateFriend.getRole()).getFriends().add(agent);
 						break;
 					}
 				}
 				
 			}
 		}
 
 		// TODO, Do we want some of these events?
 		/*
 		 * Events
 		 */
 		/*
 		AbstractMapEvent e;
 
 		// Fires
 		e = new LinkedRTreeFireMapEvent(160, 310, 30, 0.3f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		FireEngineRole.reportedFires.add(e);
 
 		e = new LinkedRTreeFireMapEvent(540, 340, 30, 0.3f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		FireEngineRole.reportedFires.add(e);
 
 		e = new LinkedRTreeFireMapEvent(900, 700, 30, 0.3f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		FireEngineRole.reportedFires.add(e);
 
 		e = new LinkedRTreeFireMapEvent(1030, 190, 30, 0.3f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		FireEngineRole.reportedFires.add(e);
 
 		// Collapsed Buildings
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(440, 55, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(370, 240, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(200, 560, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(445, 680, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(540, 560, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(790, 115, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(895, 355, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		e = new LinkedRTreeCollapsedBuildingMapEvent(790, 75, 15, 0f);
 		Scheduler.mapEventRepository.put(e.getId(), e);
 		RescueVehicleRole.reportedEvents.add(e);
 
 		*/
 
 		// TODO, do we need this?
 		Scheduler.mapEventRepository.executePut();
 
 	}
 
 	@Override
 	public void preCycle() {
 
 	}
 
 	private AbstractAgent createAgentOnMap() {
 		int x, y, obstacle;
 		do {
 			x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
 			y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
 			obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
 		} while (obstacle > 100);
 
 		return Scheduler.agentRepository.createAgent(x, y);
 	}
 
 	private AbstractAgent createAgentInGreen(boolean mobileComEnabled) {
 		int x, y, percent, obstacle;
 		do {
 			x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
 			y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
 			percent = Scheduler.rand.nextInt(100) + 1;
 			// obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
 		} while (percent > DiscreteMap.getInstance().getAdditionalCahnnel()[y][x]);
 		return Scheduler.agentRepository.createAgent(x, y, mobileComEnabled);
 	}
 
 	@Override
 	public void preSimulation() {
 	}
 
 	@Override
 	public void postSimulation() {
 	}
 
 	@Override
 	public void postCycle() {
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		new DegreesScenario();
 
 	}
 
 }
