 package siver.agents;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import repast.simphony.context.Context;
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.NdPoint;
 import siver.LanedTest;
 import siver.agents.boat.BoatAgent;
 import siver.agents.boat.BoatCorner;
 import siver.agents.boat.CoxAgent;
 import siver.river.River;
 import siver.river.lane.Lane.UnstartedLaneException;
 
 public class BoatHouseTest extends LanedTest {
 
 	private static River r;
 	private Context<Object> mockContext;
 	private ContinuousSpace<Object> mockSpace;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		createLanes();
 		r = new River(up, mid, down);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		mockContext = EasyMock.createMock(Context.class);
 		mockSpace = EasyMock.createMock(ContinuousSpace.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 	}
 
 	@Test
 	public void testBoatHouse() {
 		replay(mockContext);
 		replay(mockSpace);
 		new BoatHouse(r, mockContext, mockSpace);
 		verify(mockContext);
 		verify(mockSpace);
 	}
 
 	@Test
 	public void testLaunchBoat() {
 		expect(mockContext.add(anyObject(BoatAgent.class))).andReturn(true).times(1);
 		expect(mockContext.add(anyObject(CoxAgent.class))).andReturn(true).times(1);
 		expect(mockContext.add(anyObject(BoatCorner.class))).andReturn(true).times(4);
 		expect(mockSpace.moveTo(anyObject(BoatAgent.class), eq(0.0), eq(10.0))).andReturn(true).times(1);
		expect(mockSpace.getLocation(anyObject(BoatAgent.class))).andReturn(new NdPoint(0,10)).times(1);
 		expect(mockSpace.getDisplacement(new NdPoint(0,10), new NdPoint(20,10))).andReturn(new double[]{20,0}).times(1);
 		replay(mockContext);
 		replay(mockSpace);
 		
 		BoatHouse bh = new BoatHouse(r, mockContext, mockSpace);
 		bh.launchBoat();
 		
 		verify(mockContext);
 		verify(mockSpace);
 	}
 	
 	@Test
 	public void testGetLaunchLane() throws UnstartedLaneException {
 		replay(mockContext);
 		replay(mockSpace);
 		BoatHouse bh = new BoatHouse(r, mockContext, mockSpace);
 		assertEquals(down, bh.getLaunchLane());
 		verify(mockContext);
 		verify(mockSpace);
 	}
 
 }
