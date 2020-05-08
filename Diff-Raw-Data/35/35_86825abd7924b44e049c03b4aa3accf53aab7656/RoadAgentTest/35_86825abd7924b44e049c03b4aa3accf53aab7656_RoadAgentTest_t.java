 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04;
 
 import static org.junit.Assert.assertEquals;
 
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 

import uk.ac.imperial.presage2.core.IntegerTime;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 
 /**
  * @author dws04
  *
  */
 public class RoadAgentTest {
 
 	
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
	IntegerTime time = new IntegerTime(0);
	SimTime sTime = new SimTime(time);
 
 	@Before
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(
 				new AbstractEnvironmentModule()
 					.addActionHandler(LaneMoveHandler.class)
 					.addParticipantEnvironmentService(ParticipantLocationService.class)
 					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 					.addParticipantEnvironmentService(ParticipantSpeedService.class)
 					.addGlobalEnvironmentService(RoadEnvironmentService.class),
 				Area.Bind.area2D(lanes, length).addEdgeHander(Edge.Y_MAX,
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
 	
 	private RoadAgent createAgent(String name, RoadLocation startLoc, int startSpeed, RoadAgentGoals goals) {
 		RoadAgent a = new RoadAgent(Random.randomUUID(), name, startLoc, startSpeed, goals);
 		injector.injectMembers(a);
 		a.initialise();
 		return a;
 	}
 	
 	public static void assertLocation(RoadAgent a, RoadLocation expected) {
 		assertEquals(expected.getLane(), ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getLane());
 		assertEquals(expected.getOffset(), ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getOffset());
 		assertEquals(expected.getLane(), a.myLoc.getLane());
 		assertEquals(expected.getOffset(), a.myLoc.getOffset());
 		assertEquals(expected, ((RoadLocation) a.locationService.getAgentLocation(a.getID())) );
 		assertEquals(expected, a.myLoc);
 	}
 	
 	public static void assertLocation(RoadAgent a, int expectedLane, int expectedOffset) {
 		assertEquals(expectedLane, ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getLane());
 		assertEquals(expectedOffset, ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getOffset());
 		assertEquals(expectedLane, a.myLoc.getLane());
 		assertEquals(expectedOffset, a.myLoc.getOffset());
 	}
 	
 	public static void assertSpeed(RoadAgent a, int expected){
 		assertEquals(expected, a.speedService.getAgentSpeed(a.getID()));
 		assertEquals(expected, a.mySpeed);
 	}
 	
	public void incrementTime(){
		time.increment();
		env.incrementTime();
	}
	
 	@Test
 	public void test() throws ActionHandlingException {
		int startSpeed = Random.randomInt(maxSpeed)+1;
 		int startLane = Random.randomInt(lanes);
 		int spacing = Random.randomInt(maxDecel);
 		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
 		RoadAgent a = createAgent("a", new RoadLocation(startLane, 0), startSpeed, goals);
 
		incrementTime();
 
 		assertLocation(a, startLane, 0);
 		assertSpeed(a, startSpeed);
 		
 		a.execute();
 		
 		
 	}
 	
 }
