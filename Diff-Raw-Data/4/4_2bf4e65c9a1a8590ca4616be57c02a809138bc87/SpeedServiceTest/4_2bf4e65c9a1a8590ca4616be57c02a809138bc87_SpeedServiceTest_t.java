 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.imperial.dws04.Presage2Experiments.Driver;
 import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
 import uk.ac.imperial.dws04.Presage2Experiments.SpeedService;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 
 /**
  * @author dws04
  *
  */
 public class SpeedServiceTest {
 	
 	Injector injector;
 	AbstractEnvironment env;
 	//RoadEnvironmentService roadEnvironmentService;
 	SpeedService globalSpeedService;
 	
 	private int lanes = 3;
 	private int length = 10;
 	private int maxSpeed = 10;
 	private int maxAccel = 1;
 	private int maxDecel = 1;
 	private final int junctionCount = 0;
 
 	// can't use this globally anymore since we want to be able to alter the params
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(
 				// rule module
 				new RuleModule(),
 				new AbstractEnvironmentModule()
 					.addActionHandler(LaneMoveHandler.class)
 					.addParticipantEnvironmentService(ParticipantLocationService.class)
 					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 					.addParticipantEnvironmentService(ParticipantSpeedService.class)
 					.addGlobalEnvironmentService(RoadEnvironmentService.class)
 					.setStorage(RuleStorage.class),
 				Area.Bind.area2D(lanes, length).addEdgeHandler(Edge.Y_MAX,
 						WrapEdgeHandler.class), new EventBusModule(),
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
 					}
 				});
 
 		env = injector.getInstance(AbstractEnvironment.class);
 		globalSpeedService = injector.getInstance(SpeedService.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	class TestAgent extends AbstractParticipant {
 
 		RoadLocation startLoc;
 		int startSpeed;
 		ParticipantRoadLocationService locationService;
 		ParticipantSpeedService speedService;
 		Driver driver;
 
 		public TestAgent(UUID id, String name, RoadLocation startLoc, int startSpeed) {
 			super(id, name);
 			this.startLoc = startLoc;
 			this.startSpeed = startSpeed;
 		}
 
 		@Override
 		protected Set<ParticipantSharedState> getSharedState() {
 			Set<ParticipantSharedState> ss = super.getSharedState();
 			ss.add(ParticipantRoadLocationService.createSharedState(getID(),startLoc));
 			ss.add(ParticipantSpeedService.createSharedState(getID(), startSpeed));
 			return ss;
 		}
 
 		@Override
 		public void initialise() {
 			super.initialise();
 			try {
 				this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
 			} catch (UnavailableServiceException e) {
 				logger.warn(e);
 			}
 			try {
 				this.speedService = getEnvironmentService(ParticipantSpeedService.class);
 			} catch (UnavailableServiceException e) {
 				logger.warn(e);
 			}
 			try {
 				this.driver = new Driver(getID(), this);
 			} catch (UnavailableServiceException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected void processInput(Input in) {
 		}
 
 		public void performAction(Action a) throws ActionHandlingException {
 			environment.act(a, getID(), authkey);
 		}
 
 		public void assertLocation(int lane, int offset) {
 			RoadLocation current = (RoadLocation) this.locationService
 					.getAgentLocation(getID());
 			assertEquals(lane, current.getLane());
 			assertEquals(offset, current.getOffset());
 		}
 		
 		public void assertSpeed(int speed) {
 			int current = this.speedService.getAgentSpeed(getID());
 			assertEquals(speed, current);
 		}
 		
 		
 	}
 
 	private TestAgent createTestAgent(String name, RoadLocation startLoc, int startSpeed) {
 		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed);
 		injector.injectMembers(a);
 		a.initialise();
 		return a;
 	}
 	
 	public void assertGlobalSpeed(int expected, UUID agentID) {
 		assertEquals(expected, globalSpeedService.getAgentSpeed(agentID));
 	}
 	
 	@Test
 	public void testSpeedCheck() throws Exception {
 		setUp();
 		int startSpeed = Random.randomInt(maxSpeed-1)+1;
 		int startLane = 2;
 		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);
 
 		env.incrementTime();
 
 		// make some valid moves
 		a.assertLocation(startLane, 0);
 		a.assertSpeed(startSpeed);
 		assertGlobalSpeed(startSpeed, a.getID());
 		
 		//System.out.println("Speed:" + startSpeed + " / move:" + a.driver.accelerate());
 		a.performAction(a.driver.accelerate());
 		env.incrementTime();
 		a.assertSpeed(startSpeed+1);
 		assertGlobalSpeed(startSpeed+1, a.getID());
 		
 		a.performAction(a.driver.decelerate());
 		env.incrementTime();
 		a.assertSpeed(startSpeed);
 		assertGlobalSpeed(startSpeed, a.getID());
 		
 		a.performAction(a.driver.changeLaneLeft());
 		env.incrementTime();
 		a.assertSpeed(startSpeed);
 		assertGlobalSpeed(startSpeed, a.getID());
 		
 		a.performAction(a.driver.constantSpeed());
 		env.incrementTime();
 		a.assertSpeed(startSpeed);
 		assertGlobalSpeed(startSpeed, a.getID());
 
 		globalSpeedService.setAgentSpeed(a.getID(), maxSpeed);
 		// check it doesn't update immediately
 		assertTrue(maxSpeed!=globalSpeedService.getAgentSpeed(a.getID()));
 		assertTrue(maxSpeed!=a.speedService.getAgentSpeed(a.getID()));
 		// check it does update in the next cycle
 		env.incrementTime();
 		assertGlobalSpeed(maxSpeed, a.getID());	
 		a.assertSpeed(maxSpeed);
 		
 		// Check the participant can't change it
 		try {
 			a.speedService.setAgentSpeed(a.getID(), maxSpeed-1);
 			fail();
 		} catch (SharedStateAccessException e) {
 			
 		}
 	}
 	
 	@Test
 	public void testStoppingDistance() throws Exception {
 		setUp();
 		int startSpeed = 5;
 		int startLane = Random.randomInt(lanes);
 		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);
 
 		env.incrementTime();
 		a.assertLocation(startLane, 0);
 		a.assertSpeed(5);
 		assertGlobalSpeed(startSpeed, a.getID());
 		assertTrue(this.maxDecel == 1);
 		
 		// check stopping distance is correct.
 		assertEquals(15, globalSpeedService.getStoppingDistance(a.getID()));
 		assertEquals(20, globalSpeedService.getConservativeStoppingDistance(5));
 		
 		// slow down so we can check it with another value
 		a.performAction(a.driver.decelerate());
 		env.incrementTime();
 		a.performAction(a.driver.decelerate());
 		env.incrementTime();
 		a.assertSpeed(3);
 		assertGlobalSpeed(3, a.getID());
 		assertEquals(6, globalSpeedService.getStoppingDistance(a.getID()));
 		assertEquals(9, globalSpeedService.getConservativeStoppingDistance(3));
 	}
 	
 	@Test
 	public void testStopDistAgain() throws Exception {
 		this.maxSpeed = 5;
 		this.maxAccel = 2;
 		this.maxDecel = 2;
 		this.testGetters();
 		
 		int startSpeed = 5;
 		int startLane = Random.randomInt(lanes);
 		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);
 
 		env.incrementTime();
 		a.assertLocation(startLane, 0);
 		a.assertSpeed(5);
 		assertGlobalSpeed(startSpeed, a.getID());
 		assertTrue(this.maxDecel == 2);
 		
 		// check stopping distance is correct.
 		assertEquals(9, globalSpeedService.getStoppingDistance(a.getID()));
 		assertEquals(14, globalSpeedService.getConservativeStoppingDistance(5));
 		
 		// slow down so we can check it with another value
 		a.performAction(a.driver.decelerate());
 		env.incrementTime();
 		a.performAction(a.driver.decelerate());
 		env.incrementTime();
 		a.assertSpeed(3);
 		assertGlobalSpeed(3, a.getID());
 		assertEquals(4, globalSpeedService.getStoppingDistance(a.getID()));
 		assertEquals(7, globalSpeedService.getConservativeStoppingDistance(3));
 	}
 	
 	@Test
 	public void testSpeedToStopIn() throws Exception {
 		setUp();
 		/*
 		 * Distance:	|0|9|8|7|6|5|4|3|2|1|0|
 		 * Markers:		  4       3     2   1 0
 		 * Speed:		|4|3|3|3|3|2|2|2|1|1|0|
 		 */
 		assertEquals(0,globalSpeedService.getSpeedToStopInDistance(0));
 		assertEquals(1,globalSpeedService.getSpeedToStopInDistance(1));
 		assertEquals(1,globalSpeedService.getSpeedToStopInDistance(2));
 		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(3));
 		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(4));
 		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(5));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(6));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(7));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(8));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(9));
 		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(10));
 		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(11));
 	}
 	
 	@Test
 	public void testSpeedToStopInAgain() throws Exception {
 		this.maxAccel = 2;
 		this.maxDecel = 2;
 		this.testGetters();
 		setUp();
 		/*
 		 * Distance:	|0|9|8|7|6|5|4|3|2|1|0|
 		 * Markers:		    5     4   3   2 1 0
 		 * Speed:		|4|3|3|3|3|2|2|2|2|1|0|
 		 */
 		assertEquals(0,globalSpeedService.getSpeedToStopInDistance(0));
 		assertEquals(1,globalSpeedService.getSpeedToStopInDistance(1));
 		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(2));
 		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(3));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(4));
 		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(5));
 		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(6));
 		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(7));
 		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(8));
 		assertEquals(5,globalSpeedService.getSpeedToStopInDistance(9));
 		assertEquals(5,globalSpeedService.getSpeedToStopInDistance(10));
 		assertEquals(5,globalSpeedService.getSpeedToStopInDistance(11));
 	}
 	
 	@Test
 	public void testGetters() throws Exception {
 		setUp();
 		int startSpeed = Random.randomInt(maxSpeed);
 		int startLane = Random.randomInt(lanes);
 		RoadLocation startLocation = new RoadLocation(startLane, 0);
 		TestAgent a = createTestAgent("a", startLocation, startSpeed);
 
 		env.incrementTime();
 
 		// Check that everything returns the same results
 		a.assertLocation(startLane, 0);
 		a.assertSpeed(startSpeed);
 		
 		assertEquals(maxAccel, a.driver.getMaxAccel());
 		assertEquals(maxDecel, a.driver.getMaxDecel());
 		assertEquals(maxSpeed, globalSpeedService.getMaxSpeed());
 		assertEquals(maxAccel, globalSpeedService.getMaxAccel());
 		assertEquals(maxDecel, globalSpeedService.getMaxDecel());
 		//System.out.println("Maxspeed:" + maxSpeed);
 		assertEquals(maxSpeed, a.speedService.getMaxSpeed());
 		assertEquals(maxAccel, a.speedService.getMaxAccel());
 		assertEquals(maxDecel, a.speedService.getMaxDecel());
 	}
 	
 	@Test
 	public void testLocationAwareParticipantFunctions() throws Exception {
 		length = 50;
 		maxSpeed = 5;
 		maxDecel = 1;
 		// perception range is 15
 		setUp();
 		
 		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 0), 2);
 		TestAgent c = createTestAgent("c", new RoadLocation(2, 14), 3);
 		TestAgent d = createTestAgent("d", new RoadLocation(0, 20), 4);
 		TestAgent e = createTestAgent("e", new RoadLocation(0, 49), 5);
 		
 		env.incrementTime();
 		// check it's all correct
 		a.assertLocation(0, 0);
 		a.assertSpeed(1);
 		b.assertLocation(1, 0);
 		b.assertSpeed(2);
 		c.assertLocation(2, 14);
 		c.assertSpeed(3);
 		d.assertLocation(0, 20);
 		d.assertSpeed(4);
 		e.assertLocation(0, 49);
 		e.assertSpeed(5);
 		
 		// check they can see people they're supposed to
 		assertEquals(2, a.speedService.getAgentSpeed(b.getID()));
 		assertEquals(3, a.speedService.getAgentSpeed(c.getID()));
 		// (it should wrap)
 		assertEquals(5, a.speedService.getAgentSpeed(e.getID()));
 		assertEquals(1, b.speedService.getAgentSpeed(a.getID()));
 		assertEquals(3, b.speedService.getAgentSpeed(c.getID()));
 		assertEquals(1, c.speedService.getAgentSpeed(a.getID()));
 		assertEquals(2, c.speedService.getAgentSpeed(b.getID()));
 		assertEquals(4, c.speedService.getAgentSpeed(d.getID()));
 		assertEquals(3, d.speedService.getAgentSpeed(c.getID()));
 		assertEquals(1, e.speedService.getAgentSpeed(a.getID()));
 		
 		try {
 			// try to get someone you can't see
 			a.speedService.getAgentSpeed(d.getID());
 			fail();
 		} catch (CannotSeeAgent ex) {
 		}
 		try {
 			// should be reflexive
 			d.speedService.getAgentSpeed(a.getID());
 			fail();
 		} catch (CannotSeeAgent ex) {
 		}
 		
		// greater than you expect due to looking at someone else behind you
		assertEquals(3, b.speedService.getAdjustedStoppingDistance(a.getID(), false));
 		try {
 			// can't see
 			a.speedService.getStoppingDistance(d.getID());
 			fail();
 		} catch (CannotSeeAgent ex) {
 		}
 		
 		Map<UUID, Integer> aMap = a.speedService.getNearbyAgentSpeeds();
 		assertTrue(aMap.get(b.getID()).equals(2));
 		assertTrue(aMap.get(c.getID()).equals(3));
 		// wraps
 		assertTrue(aMap.get(e.getID()).equals(5));
 		// can't see too far
 		assertTrue((!aMap.containsKey(d.getID())));
 		
 		
 	}
 	
 }
