 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.QueryResultsRow;
 import org.drools.runtime.rule.Variable;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
 import uk.ac.imperial.dws04.Presage2Experiments.SpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConActionMsg;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConMsgToRuleEngine;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.LeaveCluster;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ResignLeadership;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncAck;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.TimeStampedAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.presage2.core.IntegerTime;
 import uk.ac.imperial.presage2.core.Time;
 import uk.ac.imperial.presage2.core.TimeDriven;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.messaging.Performative;
 import uk.ac.imperial.presage2.core.network.ConstrainedNetworkController;
 import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
 import uk.ac.imperial.presage2.core.network.NetworkConstraint;
 import uk.ac.imperial.presage2.core.network.NetworkController;
 import uk.ac.imperial.presage2.core.simulator.Scenario;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Singleton;
 import com.google.inject.name.Names;
 
 /**
  * @author dws04
  *
  */
 public class IPConAgentTest {
 
 	final private Logger logger = Logger.getLogger(this.getClass());
 
 	Injector injector;
 	RuleStorage rules;
 	StatefulKnowledgeSession session;
 	
 	AbstractEnvironment env;
 	//RoadEnvironmentService roadEnvironmentService;
 	SpeedService globalSpeedService;
 	RoadEnvironmentService globalRoadEnvironmentService;
 	IPConService globalIPConService;
 	
 	private int lanes = 3;
 	private int length = 10;
 	private int maxSpeed = 10;
 	private int maxAccel = 1;
 	private int maxDecel = 1;
 	private int junctionCount = 0;
 	Time time = new IntegerTime(0);
 	SimTime sTime = new SimTime(time);
 	
 	// jmock stuff for scenario/time
 	Mockery context = new Mockery();
 	final Scenario scenario = context.mock(Scenario.class);
 	NetworkController networkController;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		Set<Class<? extends NetworkConstraint>> constraints = new HashSet<Class<? extends NetworkConstraint>>();
 		constraints.add(IPConMsgToRuleEngine.class);
 		injector = Guice.createInjector(
 				// rule module
 				new RuleModule()
 					.addClasspathDrlFile("IPConUtils.drl")
 					.addClasspathDrlFile("IPConPowPer.drl")
 					.addClasspathDrlFile("IPCon.drl")
 					.addClasspathDrlFile("IPConOblSan.drl"),
 				new AbstractEnvironmentModule()
 					.addActionHandler(LaneMoveHandler.class)
 					.addParticipantEnvironmentService(ParticipantLocationService.class)
 					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 					.addParticipantEnvironmentService(ParticipantSpeedService.class)
 					.addParticipantEnvironmentService(ParticipantIPConService.class)
 					.addGlobalEnvironmentService(RoadEnvironmentService.class)
 					.addGlobalEnvironmentService(IPConService.class)
 					.addParticipantGlobalEnvironmentService(IPConBallotService.class)
 					.setStorage(RuleStorage.class),
 				Area.Bind.area2D(lanes, length).addEdgeHandler(Edge.Y_MAX,
 						WrapEdgeHandler.class), new EventBusModule(),
 				NetworkModule.constrainedNetworkModule(constraints).withNodeDiscovery(),
 				new AbstractModule() {
 					// add in params that are required
 					@Override
 					protected void configure() {
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxSpeed")).toInstance(maxSpeed);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxAccel")).toInstance(maxAccel);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxDecel")).toInstance(maxDecel);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.junctionCount")).toInstance(junctionCount);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.lanes")).toInstance(lanes);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.length")).toInstance(length);
 						// need to bind a time and a scenario
 						bind(Time.class).to(IntegerTime.class);
 						bind(Scenario.class).toInstance(scenario);
 						
 						// temporary hack while Sam writes a real fix
 						bind(NetworkController.class).in(Singleton.class);
 						bind(ConstrainedNetworkController.class).in(Singleton.class);
 					}
 				});
 		
 		// allow the fake scenario to use any mock timedriven and environment classes
 		context.checking(new Expectations() {
 			{
 				allowing(scenario).addTimeDriven(with(any(NetworkController.class)));
 				allowing(scenario).addEnvironment(with(any(TimeDriven.class)));
 			}
 		});
 		
 		networkController = injector.getInstance(ConstrainedNetworkController.class);
 		env = injector.getInstance(AbstractEnvironment.class);
 		globalSpeedService = injector.getInstance(SpeedService.class);
 		globalRoadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
 		globalIPConService = injector.getInstance(IPConService.class);
 		
 		rules = injector.getInstance(RuleStorage.class);
 		session = injector.getInstance(StatefulKnowledgeSession.class);
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		session.dispose();
 	}
 	
 	public void outputObjects() {
 		Collection<Object> objects = session.getObjects();
 		logger.info("\nObjects: " + objects.toString() + " are :");
 		for (Object object : objects) {
 			logger.info(object);
 		}
 		logger.info("/objects");
 		analyseDroolsUsage();
 		logger.info("/usage");
 	}
 	
 	private void analyseDroolsUsage() {
 		Map<Class<?>, Integer> typeCards = new HashMap<Class<?>, Integer>();
 		for (Object o : session.getObjects()) {
 				if (!typeCards.containsKey(o.getClass())) {
 					typeCards.put(o.getClass(), 0);
 				}
 				typeCards.put(o.getClass(), typeCards.get(o.getClass()) + 1);
 		}
 		logger.info("Drools memory usage:");
 		for (Map.Entry<Class<?>, Integer> entry : typeCards.entrySet()) {
 			logger.info(entry.getKey().getSimpleName() + " - " + entry.getValue());
 		}
 	}
 	
 	class TestAgent extends RoadAgent {
 
 		public TestAgent(UUID id, String name, RoadLocation myLoc, int mySpeed,
 				RoadAgentGoals goals) {
 			super(id, name, myLoc, mySpeed, goals);
 		}
 		
 		public NetworkAdaptor getNetwork() {
 			return this.network;
 		}
 		
 		@Override
 		public HashMap<String, Pair<Integer, Integer>> getGoalMap() {
 			return super.getGoalMap();
 		}
 		
 	}
 	
 	private TestAgent createAgent(String name, RoadLocation startLoc, int startSpeed) {
 		 RoadAgentGoals goals = new RoadAgentGoals((Random.randomInt(maxSpeed)+1), Random.randomInt(length), 0);
 		 return createAgent(name, startLoc, startSpeed, goals);
 	}
 	
 	private TestAgent createAgent(String name, RoadLocation startLoc, int startSpeed, RoadAgentGoals goals) {
 		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed, goals);
 		// FIXME TODO Not sure if this is needed...?
 		injector.injectMembers(a);
 		a.initialise();
 		//Call this if needed
 		//initAgent(a.getIPConHandle(), roles, 0, issue, cluster);
 		return a;
 	}
 	
 	public void incrementTime(int t){
 		time.increment();
 		networkController.incrementTime();
 		
 		// Sam hack to make the network controller threads finish before trying to run the rulesengine
 		try {
 			Thread.sleep(t);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		// need to fire this manually since we don't have a simulation,
 		// otherwise msgs never delivered...
 		networkController.onParticipantsComplete(null);
 		env.incrementTime();
 	}
 	
 	public void incrementTime() {
 		incrementTime(500);
 	}
 	
 	private void insert(Object obj) {
 		if (obj instanceof TimeStampedAction) {
 			((TimeStampedAction)obj).setT(time.intValue());
 		}
 		session.insert(obj);
 	}
 	
 	/**
 	 * Artificially inserting facts without the service for sake of testing
 	 */
 	private void addRoles(IPConAgent agent, Role[] roles, Integer revision, String issue, UUID cluster) {
 		//Set roles
 		for (Role role : roles) {
 			insert(new IPConRIC(revision, issue, cluster));
 			insert(new HasRole(role, agent, revision, issue, cluster));
 		}
 		// Initially didn't vote
 		//insert(new Voted(agent, revision, 0, IPCNV.val(), issue, cluster));
 		// And the reportedvote for the initially
 		//insert(new ReportedVote(agent, revision, 0, IPCNV.val(), revision, 0, issue, cluster));
 	}
 	
 	/**
 	 * Unwraps Roles from HasRoles
 	 * @param agent
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return
 	 */
 	private Collection<Role> getRoles(IPConAgent agent, Integer revision, String issue, UUID cluster) {
 		Collection<HasRole> hasRoles = globalIPConService.getAgentRoles(agent, revision, issue, cluster);
 		Collection<Role> roles = new ArrayList<Role>();
 		for (HasRole hasRole : hasRoles) {
 			roles.add(hasRole.getRole());
 		}
 		return roles;
 	}
 	
 	/**
 	 * @throws Exception
 	 */
 	@Test
 	public void testObligationInstantiation() throws Exception {
 		logger.info("\nBeginning test of obligation instantiation...");
 		/**
 		 * This requires access to a fn which should be private, so calling public wrapper fn
 		 */
 		
 		/*
 		 * Create agents
 		 */
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		TestAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
 		TestAgent a3 = createAgent("a3", new RoadLocation(2,0), 1);
 		incrementTime();
 		
 		// get a valid RIC
 		IPConRIC a1RIC = globalIPConService.getCurrentRICs(a1.getIPConHandle()).iterator().next();
 		final Integer revision = a1RIC.getRevision();
 		final String issue = a1RIC.getIssue();
 		final UUID cluster = a1RIC.getCluster();
 		Object value = "VALUE";
 
 		// Add some roles
 		addRoles(a2.getIPConHandle(), new Role[]{Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
 		addRoles(a3.getIPConHandle(), new Role[]{Role.ACCEPTOR}, revision, issue, cluster);
 		
 		/*
 		 * Set up cluster to ensure at least one agent-relative obligation and one agent-neutral obligation
 		 * where the agents are all permitted. Preferably the permissions should include examples of multiple
 		 * options, no options, single options, and unconstrained(-ish) options. 
 		 */
 		/*
 		 * A2 requests.
 		 * A1 now should be obligated to prepare.
 		 */
 		insert(new Request0A(a2.getIPConHandle(), revision, value, issue, cluster));
 		incrementTime();
 		Collection<IPConAction> obl1 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 obligated to :" + obl1);
 		assertEquals(1,obl1.size());
 		Collection<IPConAction> obl1a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		assertEquals(1,obl1a.size());
 		Collection<IPConAction> per1 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 permitted to :" + per1);
 		assertEquals(1,per1.size());
 		
 		LinkedList<IPConAction> a1Obl1 = a1.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a1Obl1.size());
 		logger.info("A1 obligated to: " + obl1.toArray()[0]);
 		logger.info("A1 instantiated: " + a1Obl1.get(0));
 		assertTrue(a1Obl1.get(0).fulfils((IPConAction)obl1.toArray()[0]));
 		logger.info("** Unconstrained instantiation (Prepare1A) test passed **");
 		
 		/*
 		 * A1 sends instantiated prepare message.
 		 * A2 and A3 not obligated to respond, but should respond with IPCNV.
 		 */
 		session.insert(a1Obl1.get(0));
 		incrementTime();
 		Collection<IPConAction> obl2 = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		logger.info("Agents not obligated :" + obl2);
 		assertEquals(0,obl2.size());
 		Collection<IPConAction> per2 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Response1B", null, revision, issue, cluster);
 		logger.info("Agents permitted to :" + per2);
 		assertEquals(3,per2.size());
 		
 		/*
 		 * Check that agents with no obligations don't try instantiating anything
 		 */
 		for (TestAgent ag : new TestAgent[]{a1, a2, a3}) {
 			assertEquals(0, ag.TESTgetInstantiatedObligatedActionQueue().size());
 		}
 		logger.info("** No obligations to instantiate test passed **");
 		
 		/*
 		 * Cheat a bit and just insert the permitted response actions for A1-3.
 		 * A1 should be obligated to submit the proposed value.
 		 */
 		for (IPConAction act : per2) {
 			session.insert(act);
 		}
 		incrementTime();
 		Collection<IPConAction> obl3 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Submit2A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 obligated to :" + obl3);
 		assertEquals(1,obl3.size());
 		Collection<IPConAction> obl3a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		assertEquals(1,obl3a.size());
 		
 		LinkedList<IPConAction> a1Obl3 = a1.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a1Obl3.size());
 		logger.info("A1 obligated to: " + obl3.toArray()[0]);
 		logger.info("A1 instantiated: " + a1Obl3.get(0));
 		assertTrue(a1Obl3.get(0).fulfils((IPConAction)obl3.toArray()[0]));
 		logger.info("** One-option-constrained instantiation (Submit2A) test passed **");
 		
 		/*
 		 * Insert A1's instantiated submit action.
 		 * A1-3 not obligated to vote, but permitted to.
 		 */
 		session.insert(a1Obl3.get(0));
 		incrementTime();
 		Collection<IPConAction> per3 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Vote2B", null, revision, issue, cluster);
 		logger.info("Agents permitted to :" + per3);
 		assertEquals(3,per3.size());
 		
 		/*
 		 * Cheat a bit again and send the permitted vote actions for A1-3.
 		 * Agents should not be obligated to do anything.
 		 * VALUE should be chosen.
 		 */
 		for (IPConAction act : per3) {
 			session.insert(act);
 		}
 		incrementTime();
 		Collection<IPConAction> obl4 = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		logger.info("Agents not obligated :" + obl4);
 		assertEquals(0,obl4.size());
 		Collection<IPConFact> fact4 = globalIPConService.getFactQueryResults("Chosen", revision, issue, cluster);
 		assertEquals(1,fact4.size());
 		Chosen chosen = globalIPConService.getChosen(revision, issue, cluster);
 		assertEquals(value, chosen.getValue());
 		
 		/*
 		 * Add a new agent to check syncreq obligation.
 		 * A1 should be obligated to SyncReq the new agent.
 		 */
 		TestAgent a4 = createAgent("a4", new RoadLocation(0,2), 1);
 		insert(new AddRole(a1.getIPConHandle(), a4.getIPConHandle(), Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		Collection<IPConAction> obl5 = globalIPConService.getActionQueryResultsForRIC("getObligations", "SyncReq", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 obligated to :" + obl5);
 		assertEquals(1,obl5.size());
 		Collection<IPConAction> obl5a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		assertEquals(1,obl5a.size());
 		
 		LinkedList<IPConAction> a1Obl5 = a1.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a1Obl5.size());
 		logger.info("A1 obligated to: " + obl5.toArray()[0]);
 		logger.info("A1 instantiated: " + a1Obl5.get(0));
 		assertTrue(a1Obl5.get(0).fulfils((IPConAction)obl5.toArray()[0]));
 		logger.info("** One-option-constrained (self-)instantiation (SyncReq) test passed **");
 		
 		/*
 		 * A1 sends instantiated SyncReq.
 		 * A4 should be obligated to reply (multiple choice).
 		 * A1-3 should not be obligated.
 		 */
 		session.insert(a1Obl5.get(0));
 		incrementTime();
 		Collection<IPConAction> obl6 = globalIPConService.getActionQueryResultsForRIC("getObligations", "SyncAck", a4.getIPConHandle(), revision, issue, cluster);
 		logger.info("A4 obligated to :" + obl6);
 		assertEquals(1,obl6.size());
 		Collection<IPConAction> obl6a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		assertEquals(1,obl6a.size());
 		
 		LinkedList<IPConAction> a4Obl6 = a4.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a4Obl6.size());
 		logger.info("A4 obligated to: " + obl6.toArray()[0]);
 		logger.info("A4 instantiated: " + a4Obl6.get(0));
 		assertTrue(a4Obl6.get(0).fulfils((IPConAction)obl6.toArray()[0]));
 		assertEquals( value, ((SyncAck)a4Obl6.get(0)).getValue() );
 		logger.info("** Multi-option-constrained instantiation (SyncAck to unknown issue) test passed **");
 		
 		logger.info("Finished test of obligation instantiation.\n");
 	}
 	
 	/**
 	 * Tests self-instantiation of agent-neutral (leader directed) obligations in presence of multiple leaders
 	 * Ignores duelling-leader solution due to not calling the agent's execute()
 	 * @throws Exception
 	 */
 	@Test
 	public void testMultiLeaderObligationSelfInstantiation() throws Exception {
 		logger.info("\nBeginning test of obligation self-instantiation in the face of multiple leaders...");
 		/**
 		 * This requires access to a fn which should be private, so calling public wrapper fn
 		 */
 		/*
 		 * Create agents
 		 */
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		TestAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
 		TestAgent a3 = createAgent("a3", new RoadLocation(2,0), 1);
 		incrementTime();
 		
 		// get a valid RIC
 		IPConRIC a1RIC = globalIPConService.getCurrentRICs(a1.getIPConHandle()).iterator().next();
 		final Integer revision = a1RIC.getRevision();
 		final String issue = a1RIC.getIssue();
 		final UUID cluster = a1RIC.getCluster();
 		Object value = "VALUE";
 
 		// Add some roles
 		addRoles(a2.getIPConHandle(), new Role[]{Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
 		addRoles(a3.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
 		
 		/*
 		 * A2 requests.
 		 * A1 and A3 now should be obligated to prepare.
 		 */
 		insert(new Request0A(a2.getIPConHandle(), revision, value, issue, cluster));
 		incrementTime();
 		Collection<IPConAction> obl1 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 obligated to :" + obl1);
 		assertEquals(1,obl1.size());
 		Collection<IPConAction> obl3 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a3.getIPConHandle(), revision, issue, cluster);
 		logger.info("A3 obligated to :" + obl1);
 		assertEquals(1,obl3.size());
 		Collection<IPConAction> obla = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
 		assertEquals(1,obla.size()); // NB. there is only one actual obligation, but 2 agents consider it "theirs"
 		Collection<IPConAction> per1 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 permitted to :" + per1);
 		assertEquals(1,per1.size());
 		Collection<IPConAction> per3 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a3.getIPConHandle(), revision, issue, cluster);
 		logger.info("A3 permitted to :" + per3);
 		assertEquals(1,per3.size());
 		
 		LinkedList<IPConAction> a1Obl1 = a1.TESTgetInstantiatedObligatedActionQueue();
 		logger.info("A1 obligated to: " + obl1);
 		logger.info("A1 instantiated: " + a1Obl1);
 		assertEquals(1, a1Obl1.size());
 		assertTrue(a1Obl1.get(0).fulfils((IPConAction)obl1.toArray()[0]));
 		
 		LinkedList<IPConAction> a3Obl1 = a3.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a3Obl1.size());
 		logger.info("A3 obligated to: " + obl3.toArray()[0]);
 		logger.info("A3 instantiated: " + a3Obl1.get(0));
 		assertTrue(a3Obl1.get(0).fulfils((IPConAction)obl3.toArray()[0]));
 		
 		Prepare1A a1Prep = ((Prepare1A)a1Obl1.get(0));
 		Prepare1A a3Prep = ((Prepare1A)a3Obl1.get(0));
 
 		assertEquals(a1Prep.getRevision(), a3Prep.getRevision());
 		assertEquals(a1Prep.getIssue(), a3Prep.getIssue());
 		assertEquals(a1Prep.getCluster(), a3Prep.getCluster());
 		assertThat(a1Prep.getBallot(), is ( not( a3Prep.getBallot() ) ) );
 		
 		logger.info("A1 has " + a1Prep.getBallot() + ", A3 has " + a3Prep.getBallot());
 		logger.info("** Multiple leader self-instantiation test passed **");
 		
 
 		logger.info("Finished test of obligation self-instantiation in the face of multiple leaders.\n");
 	}
 	
 	@Test
 	public void testClusterArrogate() throws Exception {
 		logger.info("\nBeginning test of registration...");		
 		// Make an agent, execute(), check there are 2 RICs (spacing and speed)
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		logger.info("A1 is : " + a1);
 		a1.execute();
 		incrementTime();
 		Collection<IPConRIC> rics = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in " + rics);
 		assertThat(rics.size(), is( 2 ) );
 		logger.info("** Auto-Arrogate new RICs for goals during registration test passed **");
 		assertThat(rics.toArray(new IPConRIC[2])[0].getCluster(), is(rics.toArray(new IPConRIC[2])[1].getCluster()) );
 		logger.info("** Auto-Arrogate new RICs in one cluster during registration test passed **");
 		
 		logger.info("Finished test of registration.\n");
 		
 		incrementTime();
 
 		logger.info("\nBeginning test of arrogating into same cluster...");	
 		// TODO FIXME this doesn't really test what it says it does...
 		for (IPConRIC ric : rics) {
 			LeaveCluster leave = new LeaveCluster(a1.getIPConHandle(), ric.getCluster());
 			IPConActionMsg leavingMsg = new IPConActionMsg(Performative.INFORM, time, a1.getNetwork().getAddress(), leave);
 			a1.getNetwork().sendMessage(leavingMsg);
 			logger.trace("A1 leaving " + ric);
 		}
 		
 		incrementTime();
 		Collection<IPConRIC> noRICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in " + noRICs);
 		assertThat(noRICs.size(), is(0));
 		logger.info("Agent left all RICs.");
 		
 		a1.execute();
 		incrementTime();
 		
 		Collection<IPConRIC> newRICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in " + newRICs);
 		assertThat(newRICs.size(), is( 2 ) );
 		logger.info("** Arrogate new clusters for goals test passed **");
 		
 		IPConRIC[] array = newRICs.toArray(new IPConRIC[2]);
 		assertEquals(array[0].getCluster(), array[1].getCluster());
 		logger.info("** Correctly arrogated 2 issues into the same cluster **");
 		
 		
 		logger.info("Finished test of arrogating into same cluster\n");
 	}
 	
 	/**
 	 * @throws Exception
 	 */
 	@Test
 	public void testClusterMerge() throws Exception {
 		logger.info("\nBeginning test of joining nearby clusters...");		
 		// Make an agent, execute(), check there are 2 RICs (spacing and speed)
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1, new RoadAgentGoals(1, 50, 1));
 
 		a1.execute();
 		incrementTime();
 
 		Collection<IPConRIC> rics = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		assertThat(rics.size(), is( 2 ) );
 		
 		// insert random chosen fact (or try to...) into both of A1's RICs
 		// don't need this if agents have it autoChosen on registration
 		for (IPConRIC ric : rics) {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			Integer ballot = 0;
 			Object value = 1;
 			session.insert(new Chosen(revision, ballot, value, issue, cluster));
 			logger.debug("Session inserting chosen(" + revision + "," + ballot + "," + value + "," + issue + "," + cluster + ")");
 		}
 		
 		incrementTime();
 		
 		for (IPConRIC ric : rics) {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			assertThat(globalIPConService.getChosen(revision, issue, cluster), is( notNullValue() ));
 		}
 		logger.info("Successful setup.");
 		
 		// create another agent
 		TestAgent a2 = createAgent("a2", new RoadLocation(2, 0), 1, new RoadAgentGoals(1, 50, 1));
 		
 		// execute some more and check that a1 and a2 are both in (the same) 2 clusters
 		for (int i = 1; i<=10; i++) {
 			//logger.trace("Execution number " + i);
 			a1.execute();
 			a2.execute();
 			incrementTime();
 		}
 		
 		// A1's rics still have a chosen value in each
 		for (IPConRIC ric : rics) {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			assertThat(globalIPConService.getChosen(revision, issue, cluster), is( notNullValue() ));
 		}
 		
 		Collection<IPConRIC> a1RICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		assertThat(a1RICs.size(), is( 2 ) );
 		assertThat(a1RICs.toArray(new IPConRIC[2])[0].getCluster(), is(a1RICs.toArray(new IPConRIC[2])[1].getCluster()) );
 		logger.info("** A1 is in 2 RICs in 1 cluster **");
 		Collection<IPConRIC> a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		assertThat(a2RICs.size(), is( 2 ) );
 		
 		logger.info("A1 (" + a1RICs + ") and A2 (" + a2RICs + ") both in 2 RICs.");
 
 		// A1 and A2 are both in the same two clusters - A2 left it's clusters and merged with A1
 		for (IPConRIC a1RIC : a1RICs) {
 			assertThat( a2RICs, hasItem(a1RIC) );
 		}
 		logger.info("** Join test passed: A1 and A2 both in same RICs **");
 
 		
 		logger.info("Finished test of joining nearby clusters\n");
 	}
 	
 	@Test
 	public void testArrogateLeaderlessRIC() throws Exception {
 		logger.info("\nBeginning test of arrogating in a leaderless RIC...");		
 		
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		// get a valid RIC
 		IPConRIC a1RIC = globalIPConService.getCurrentRICs(a1.getIPConHandle()).iterator().next();
 		final Integer revision = a1RIC.getRevision();
 		final String issue = a1RIC.getIssue();
 		final UUID cluster = a1RIC.getCluster();
 		insert(new ResignLeadership(a1.getIPConHandle(), revision, issue, cluster));
 		// increment time to insert the resignation
 		incrementTime();
 		assertThat(globalIPConService.getCurrentRICs(a1.getIPConHandle()).size(), is( 2 ) );
		logger.info("Succesful setup.");
 		
 		for (int i = 1; i<=10; i++) {
 			//logger.trace("Execution number " + i);
 			a1.execute();
 			incrementTime();
 		}
 		Collection<IPConRIC> a1RICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		assertThat(a1RICs.size(), is( 2 ) );
 		logger.info("A1 is still in 2 RICs.");
 		
 		for (IPConRIC ric : a1RICs) {
 			assertThat(globalIPConService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster()).size(), is(1));
 			assertThat(globalIPConService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster()).iterator().next(), is(a1.getIPConHandle()));
 		}
 		logger.info("** A1 successfully arrogated the leaderless cluster **");
 		
 		logger.info("Finished test of arrogating in a leaderless RIC\n");
 	}
 	
 	/*@Test
 	public void testJoinRICInCurrentCluster() throws Exception {
 		logger.info("\nBeginning test of joining RIC in current cluster...");		
 
 		final Integer revision1 = 1;
 		final String issue1 = "spacing";
 		final Integer revision2 = 2;
 		final String issue2 = "speed";
 		final UUID cluster = Random.randomUUID();
 
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		TestAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
 		
 		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER}, revision1, issue1, cluster);
 		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER}, revision2, issue2, cluster);
 		addRoles(a2.getIPConHandle(), new Role[]{Role.ACCEPTOR}, revision2, issue2, cluster);
 
 		assertThat(globalIPConService.getCurrentRICs(a1.getIPConHandle()).size(), is( 2 ) );
 		assertThat(globalIPConService.getRICLeader(revision1, issue1, cluster).size(), is(1));
 		assertThat(globalIPConService.getRICLeader(revision1, issue1, cluster).iterator().next(), is(a1.getIPConHandle()));
 		assertThat(globalIPConService.getRICLeader(revision2, issue2, cluster).size(), is(1));
 		assertThat(globalIPConService.getRICLeader(revision2, issue2, cluster).iterator().next(), is(a1.getIPConHandle()));
 		assertThat(globalIPConService.getCurrentRICs(a2.getIPConHandle()).size(), is( 1 ) );
 		logger.info("Succesful setup.");
 		
 		// execute and check that a1 and a2 are both in (the same) 2 RICs
 		for (int i = 1; i<=10; i++) {
 			//logger.trace("Execution number " + i);
 			a1.execute();
 			a2.execute();
 			incrementTime();
 			outputObjects();
 		}
 		
 		Collection<IPConRIC> a1RICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in RICS : " + a1RICs);
 		assertThat(a1RICs.size(), is( 2 ) );
 		Collection<IPConRIC> a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		logger.info("A2 is in RICS : " + a2RICs);
 		assertThat(a2RICs.size(), is( 2 ) );
 		assertThat(globalIPConService.getRICLeader(revision1, issue1, cluster).size(), is(1));
 		assertThat(globalIPConService.getRICLeader(revision1, issue1, cluster).iterator().next(), is(a1.getIPConHandle()));
 		assertThat(globalIPConService.getRICLeader(revision2, issue2, cluster).size(), is(1));
 		assertThat(globalIPConService.getRICLeader(revision2, issue2, cluster).iterator().next(), is(a1.getIPConHandle()));
 		
 		for (IPConRIC a1RIC : a1RICs) {
 			assertThat( a2RICs, hasItem(a1RIC) );
 		}
 		logger.info("** Join test passed: A1 and A2 both in same RICs **");
 		logger.info("Finished test of joining RIC in current cluster\n");
 	}*/
 	
 	@Test
 	public void testClusterResign() throws Exception {
 		logger.info("\nBeginning test of resigning when multiple leaders exist...");		
 		// Make agents, make them both leader, execute, check only one leader
 		
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		TestAgent a2 = createAgent("a2", new RoadLocation(2, 0), 1);
 
 		final Integer revision = 1;
 		final String issue = "ISSUE";
 		final UUID cluster = globalIPConService.getCurrentRICs(a1.getIPConHandle()).iterator().next().getCluster();
 		
 		// make the agents have the goals for the new issue
 		a1.getGoalMap().put(issue, new Pair<Integer,Integer>(1,1));
 		a2.getGoalMap().put(issue, new Pair<Integer,Integer>(1,1));
 		
 		// give them the roals
 		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
 		addRoles(a2.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
 
 		/* 
 		 * increment time without executing to make sure the facts are inserted
 		 * but that no one has left yet
 		 */
 		incrementTime();
 		
 		Collection<Role> a1Roles = getRoles(a1.getIPConHandle(), revision, issue, cluster);
 		assertThat(a1Roles.size(), is(2));
 		Collection<Role> a2Roles = getRoles(a2.getIPConHandle(), revision, issue, cluster);
 		assertThat(a2Roles.size(), is(2));
 		for (Role a1Role : a1Roles) {
 			assertThat( a2Roles, hasItems(a1Role) );
 		}
 		logger.info("Succesful setup.");
 		
 		a1.execute();
 		a2.execute();
 		incrementTime();
 		
 		
 		Collection<Role> a1NewRoles = getRoles(a1.getIPConHandle(), revision, issue, cluster);
 		Collection<Role> a2NewRoles = getRoles(a2.getIPConHandle(), revision, issue, cluster);
 		
 		if (a1NewRoles.size()==2) {
 			assertThat(a1NewRoles, hasItem(Role.LEADER));
 			assertThat(a1NewRoles, hasItem(Role.ACCEPTOR));
 			assertThat(a2NewRoles.size(), is(1));
 			assertThat(a2NewRoles, not( hasItem(Role.LEADER)));
 			assertThat(a2NewRoles, hasItem(Role.ACCEPTOR));
 		}
 		else {
 			assertThat(a1NewRoles.size(), is(1));
 			assertThat(a1NewRoles, not( hasItem(Role.LEADER)));
 			assertThat(a1NewRoles, hasItem(Role.ACCEPTOR));
 			assertThat(a2NewRoles.size(), is(2));
 			assertThat(a2NewRoles, hasItem(Role.LEADER));
 			assertThat(a2NewRoles, hasItem(Role.ACCEPTOR));
 		}
 		
 		logger.info("** Resign test passed: only one leader **");
 
 		
 		logger.info("Finished test of resigning when multiple leaders exist\n");
 	}
 	
 	@Test
 	public void testVotingYes() {
 		logger.info("\nBeginning test of voting yes...");
 		
 		// Make agents
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1, new RoadAgentGoals(2,1,50,5,2));
 		TestAgent a2 = createAgent("a2", new RoadLocation(2, 0), 1, new RoadAgentGoals(2,5,50,5,2));
 		
 		
 		Collection<IPConRIC> a1RICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in RICS : " + a1RICs);
 		assertThat(a1RICs.size(), is( 2 ) );
 		
 		insert(new LeaveCluster(a2.getIPConHandle(), globalIPConService.getCurrentRICs(a2.getIPConHandle()).iterator().next().getCluster()));
 		// increment but don't execute, so we can get rid of their clusters
 		incrementTime();
 		Collection<IPConRIC> a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		logger.info("A2 is in RICS : " + a2RICs);
 		assertThat(a2RICs.size(), is( 0 ) );
 		
 		for (IPConRIC a1RIC : a1RICs) {
 			insert(new HasRole(Role.ACCEPTOR, a2.getIPConHandle(), a1RIC.getRevision(), a1RIC.getIssue(), a1RIC.getCluster()));
 		}
 		a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		logger.info("A2 is in RICS : " + a2RICs);
 		assertThat(a2RICs.size(), is( 2 ) );
 		
 		
 		for (int i = 1; i<=10; i++) {
 			a1.execute();
 			a2.execute();
 			incrementTime();
 		}
 		
 		
 		for (IPConRIC a1RIC : a1RICs) {
 			assertThat( a2RICs, hasItem(a1RIC) );
 		}
 		logger.info("** Setup successful: A1 and A2 both in same RICs **");
 		
 		// get leader for speed
 		ArrayList<HasRole> hasRoles = new ArrayList<HasRole>();
 		ArrayList<Object> lookup = new ArrayList<Object>();
 		lookup.addAll(Arrays.asList(new Object[]{Variable.v, "speed", Variable.v}));
 		QueryResults facts = session.getQueryResults("getRICLeader", lookup.toArray());
 		for (QueryResultsRow row : facts) {
 			//leaders.add((IPConAgent)row.get("$leader"));
 			hasRoles.add((HasRole)row.get("$role"));
 		}
 		TestAgent leader = null; 
 		IPConAgent ipconLeader = hasRoles.get(0).getAgent();
 		if (ipconLeader.equals(a1.getIPConHandle())) {
 			leader = a1;
 		}
 		else {
 			leader = a2;
 		}
 		
 		// The agents agree
 		Integer value = 2;
 		
 		// Need to make the leader a proposer...
 		AddRole addProp = new AddRole(ipconLeader, ipconLeader, Role.PROPOSER, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		// and both agents acceptors...
 		AddRole addAcc1 = new AddRole(ipconLeader, a1.getIPConHandle(), Role.ACCEPTOR, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		AddRole addAcc2 = new AddRole(ipconLeader, a2.getIPConHandle(), Role.ACCEPTOR, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		Request0A req = new Request0A(ipconLeader, hasRoles.get(0).getRevision(), value, hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		ArrayList<IPConAction> acts = new ArrayList<IPConAction>();
 		acts.add(addProp); acts.add(addAcc1); acts.add(addAcc2); acts.add(req);
 		
 		for (IPConAction act : acts) {
 			leader.getNetwork().sendMessage(new IPConActionMsg(Performative.INFORM, time, leader.getNetwork().getAddress(), act));
 		}
 		logger.info(leader + " sent msgs " + acts);
 		
 		// wait some more
 		for (int i = 1; i<=10; i++) {
 			a1.execute();
 			a2.execute();
 			incrementTime();
 		}
 		
 		assertThat((Integer)globalIPConService.getChosen(hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster()).getValue(), is(2));
 		assertThat(globalIPConService.getFactQueryResults("Vote", hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster()).size(), is(2));
 		logger.info("** Successfully both voted for the right value **");
 		
 		logger.info("Finished test of voting yes\n");
 	}
 	
 	@Test
 	public void testVotingNo() {
 		logger.info("\nBeginning test of voting no...");
 		
 		// Make agents
 		TestAgent a1 = createAgent("a1", new RoadLocation(0,0), 1, new RoadAgentGoals(2,1,50,5,2));
 		TestAgent a2 = createAgent("a2", new RoadLocation(2, 0), 1, new RoadAgentGoals(2,5,50,5,2));
 		
 		Collection<IPConRIC> a1RICs = globalIPConService.getCurrentRICs(a1.getIPConHandle());
 		logger.info("A1 is in RICS : " + a1RICs);
 		assertThat(a1RICs.size(), is( 2 ) );
 		
 		insert(new LeaveCluster(a2.getIPConHandle(), globalIPConService.getCurrentRICs(a2.getIPConHandle()).iterator().next().getCluster()));
 		// increment but don't execute, so we can get rid of their clusters
 		incrementTime();
 		Collection<IPConRIC> a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		logger.info("A2 is in RICS : " + a2RICs);
 		assertThat(a2RICs.size(), is( 0 ) );
 		
 		for (IPConRIC a1RIC : a1RICs) {
 			insert(new HasRole(Role.ACCEPTOR, a2.getIPConHandle(), a1RIC.getRevision(), a1RIC.getIssue(), a1RIC.getCluster()));
 		}
 		a2RICs = globalIPConService.getCurrentRICs(a2.getIPConHandle());
 		logger.info("A2 is in RICS : " + a2RICs);
 		assertThat(a2RICs.size(), is( 2 ) );
 		
 		
 		for (int i = 1; i<=10; i++) {
 			a1.execute();
 			a2.execute();
 			incrementTime();
 		}
 		
 		
 		for (IPConRIC a1RIC : a1RICs) {
 			assertThat( a2RICs, hasItem(a1RIC) );
 		}
 		logger.info("** Setup successful: A1 and A2 both in same RICs **");
 		
 		// get leader for speed
 		ArrayList<HasRole> hasRoles = new ArrayList<HasRole>();
 		ArrayList<Object> lookup = new ArrayList<Object>();
 		lookup.addAll(Arrays.asList(new Object[]{Variable.v, "speed", Variable.v}));
 		QueryResults facts = session.getQueryResults("getRICLeader", lookup.toArray());
 		for (QueryResultsRow row : facts) {
 			//leaders.add((IPConAgent)row.get("$leader"));
 			hasRoles.add((HasRole)row.get("$role"));
 		}
 		TestAgent leader = null; 
 		IPConAgent ipconLeader = hasRoles.get(0).getAgent();
 		if (ipconLeader.equals(a1.getIPConHandle())) {
 			leader = a1;
 		}
 		else {
 			leader = a2;
 		}
 		
 		// The agents do not agree
 		Integer value = 4;
 		
 		// Need to make the leader a proposer...
 		AddRole addProp = new AddRole(ipconLeader, ipconLeader, Role.PROPOSER, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		// and both agents acceptors...
 		AddRole addAcc1 = new AddRole(ipconLeader, a1.getIPConHandle(), Role.ACCEPTOR, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		AddRole addAcc2 = new AddRole(ipconLeader, a2.getIPConHandle(), Role.ACCEPTOR, hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		Request0A req = new Request0A(ipconLeader, hasRoles.get(0).getRevision(), value, hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster());
 		ArrayList<IPConAction> acts = new ArrayList<IPConAction>();
 		acts.add(addProp); acts.add(addAcc1); acts.add(addAcc2); acts.add(req);
 		
 		for (IPConAction act : acts) {
 			leader.getNetwork().sendMessage(new IPConActionMsg(Performative.INFORM, time, leader.getNetwork().getAddress(), act));
 		}
 		logger.info(leader + " sent msgs " + acts);
 		
 		// wait some more
 		for (int i = 1; i<=10; i++) {
 			a1.execute();
 			a2.execute();
 			incrementTime();
 		}
 		
 		// check theyre both still in the same clusters..
 		for (IPConRIC a1RIC : a1RICs) {
 			assertThat( a2RICs, hasItem(a1RIC) );
 		}
 		// a2 is inserted to the cluster artificially after a value has been chosen, so doesn't have a say about it.
 		assertThat(globalIPConService.getChosen(hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster()), nullValue());
 		assertThat(globalIPConService.getFactQueryResults("Voted", hasRoles.get(0).getRevision(), hasRoles.get(0).getIssue(), hasRoles.get(0).getCluster()).size(), is(1));
 		logger.info("** Successfully did not achieve consensus **");
 		
 		logger.info("Finished test of voting no\n");
 	}
 	
 	@Test
 	public void test() throws Exception {
 		/*
 		 * Create agents
 		 */
 		
 		/*
 		 * Agents independently decide values
 		 */
 		
 		/*
 		 * Agents merge
 		 */
 		
 		/*
 		 * Proposal for issues
 		 */
 		
 		/*
 		 * Prepare
 		 */
 		
 		/*
 		 * Response
 		 */
 		
 		/*
 		 * Submit
 		 */
 		
 		/*
 		 * Vote
 		 */
 	}
 }
