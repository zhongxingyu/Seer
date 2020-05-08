 /**
  * 
  */
 package siver.river.lane;
 
 import static org.junit.Assert.*;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import siver.context.LaneContext;
 import siver.river.lane.Lane.CompletedLaneException;
 import siver.river.lane.Lane.UnstartedLaneException;
 
 /**
  * @author henryaddison
  *
  */
 public class LaneTest {
 	private static Lane startedL;
 	
 	private static ArrayList<Point2D.Double> exp_top, exp_bottom;
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		startedL = new Lane(new LaneContext(), "Test Lane");
 		startedL.start(new Point2D.Double(0,10));
 		
 		exp_top = new ArrayList<Point2D.Double>();
 		
 		exp_bottom = new ArrayList<Point2D.Double>();
 		
 		exp_top.add(new Point2D.Double(0,20));
 		
 		exp_bottom.add(new Point2D.Double(0,0));
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link siver.river.lane.Lane#Lane(siver.LaneContext}.
 	 */
 	@Test
 	public void testLane() {
 		Lane unstartedlane = new Lane(new LaneContext(), "Test Lane");
 		assertTrue(!unstartedlane.isStarted());
 		assertEquals(0, unstartedlane.getTop().size());
 		assertEquals(0, unstartedlane.getBottom().size());
 	}
 	
 	/**
 	 * Test method for {@link siver.river.lane.Lane#start(java.awt.geom.Point2D.Double)}.
 	 */
 	@Test
 	public void testLaneStart() {
 		assertTrue(startedL.isStarted());
 		assertEquals(exp_top, startedL.getTop());
 		assertEquals(exp_bottom, startedL.getBottom());
 	}
 	
 	
 	/**
 	 * Test method for {@link siver.river.lane.Lane#extend(double)}.
 	 * @throws CompletedLaneException 
 	 */
 	@Test
 	public void testExtend() throws UnstartedLaneException, CompletedLaneException {
 		startedL.extend(0);
 		exp_top.add(new Point2D.Double(20, 20));
 		exp_bottom.add(new Point2D.Double(20, 0));
 		
 		assertEquals(exp_top, startedL.getTop());
 		assertEquals(exp_bottom, startedL.getBottom());
 		
 		startedL.extend(Math.PI/2.0);
 		exp_top.add(new Point2D.Double(10, 30));
 		exp_bottom.add(new Point2D.Double(30, 30));
 		
 		assertEquals(exp_top, startedL.getTop());
 		assertEquals(exp_bottom, startedL.getBottom());
 	}
 	
 	/**
 	 * Test method for {@link siver.river.lane.Lane#extend(double)} on unstarted Lane.
 	 * @throws UnstartedLaneException 
 	 * @throws CompletedLaneException 
 	 */
 	@Test(expected=UnstartedLaneException.class)
 	public void testExtendToUnstarted() throws UnstartedLaneException, CompletedLaneException {
 		Lane unstartedLane = new Lane(new LaneContext(), "Test Lane");
 		unstartedLane.extend(0);
 		
 	}
 	
 	/**
 	 * Test method for {@link siver.river.lane.Lane#extend(double)} on a completed Lane.
 	 * @throws CompletedLaneException 
 	 */
 	@Test(expected=CompletedLaneException.class)
 	public void testExtendOnCompleted() throws CompletedLaneException, UnstartedLaneException {
 		startedL.complete();
 		startedL.extend(1);
 	}
 	
 	@Test
 	public void testComplete() {
 		assertTrue(startedL.isComplete());
 		startedL.complete();
 		assertNotNull(startedL.getOutline());
 		assertTrue(startedL.isComplete());
 	}
 	
 	@Test
 	public void testGetStartNode() throws UnstartedLaneException {
 		LaneNode sn = startedL.getStartNode();
 		assertEquals(new Point2D.Double(0,10), sn.getLocation());
 	}
 	
 	@Test(expected=UnstartedLaneException.class)
 	public void testGetStartNodeNotStarted() throws UnstartedLaneException {
 		Lane unstartedLane = new Lane(new LaneContext(), "Test Lane");
 		unstartedLane.getStartNode();
 	}
 	
 	@Test
 	public void getNextEdge() throws UnstartedLaneException {
 		LaneNode sln = startedL.getStartNode();
 		LaneEdge<LaneNode> edge = startedL.getNextEdge(sln, false);
 		assertSame(sln, edge.getSource());
 		assertEquals(new Point2D.Double(20,10), edge.getTarget().getLocation());
 		
 		LaneEdge<LaneNode> same_edge = startedL.getNextEdge(edge.getTarget(), true);
 		LaneEdge<LaneNode> another_edge = startedL.getNextEdge(edge.getTarget(), false);
 		assertSame(same_edge, edge);
 		assertNotSame(another_edge, edge);
 	}
 	
	@Test	
 	public void getNextEdgeNoEdge() throws UnstartedLaneException {
 		LaneNode sln = startedL.getStartNode();
		assertNull(startedL.getNextEdge(sln, true));
 		
 	}
 
 }
