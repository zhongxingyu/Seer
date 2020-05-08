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
 
 import repast.simphony.context.Context;
 import repast.simphony.context.DefaultContext;
 import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.DefaultContinuousSpace;
 import repast.simphony.space.continuous.NdPoint;
 import repast.simphony.space.continuous.SimpleCartesianAdder;
 import siver.context.LaneContext;
 import siver.river.River;
 import siver.river.RiverFactory;
 import siver.river.lane.Lane.CompletedLaneException;
 import siver.river.lane.Lane.NoNextNode;
 import siver.river.lane.Lane.UnstartedLaneException;
 
 /**
  * @author henryaddison
  *
  */
 public class LaneTest {
 	private Lane startedL;
 	
 	private static ArrayList<Point2D.Double> exp_top, exp_bottom;
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		
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
 		startedL = new Lane(new LaneContext("Test Context"), "Test Lane");
 		startedL.start(new Point2D.Double(0,10));
 		
 		exp_top = new ArrayList<Point2D.Double>();
 		
 		exp_bottom = new ArrayList<Point2D.Double>();
 		
 		exp_top.add(new Point2D.Double(0,15));
 		
 		exp_bottom.add(new Point2D.Double(0,5));
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
 		Lane unstartedlane = new Lane(new LaneContext("Test Context"), "Test Lane");
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
 		exp_top.add(new Point2D.Double(20, 15));
 		exp_bottom.add(new Point2D.Double(20, 5));
 		
 		assertEquals(exp_top, startedL.getTop());
 		assertEquals(exp_bottom, startedL.getBottom());
 		
 		startedL.extend(Math.PI/2.0);
 		exp_top.add(new Point2D.Double(15, 30));
 		exp_bottom.add(new Point2D.Double(25, 30));
 		
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
 		Lane unstartedLane = new Lane(new LaneContext("Test Context"), "Test Lane");
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
 		assertFalse(startedL.isComplete());
 		startedL.complete();
 		assertNotNull(startedL.getOutline());
 		assertTrue(startedL.isComplete());
 	}
 	
 	@Test
 	public void testGetStartNode() {
 		LaneNode sn = startedL.getStartNode();
 		assertEquals(new Point2D.Double(0,10), sn.getLocation());
 		assertEquals(startedL, sn.getLane());
 	}
 	
 	@Test
 	public void testGetStartNodeNotStarted() {
 		Lane unstartedLane = new Lane(new LaneContext("Test Context"), "Test Lane");
 		assertNull(unstartedLane.getStartNode());
 	}
 	
 	@Test
 	public void testGetNextEdge() throws UnstartedLaneException, CompletedLaneException {
 		startedL.extend(0);
 		startedL.extend(0);
 		
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
 	public void testGetNextEdgeIgnoresLaneChangingEdges() throws UnstartedLaneException, CompletedLaneException {
 		startedL.extend(0);
 		LaneNode first = startedL.getStartNode();
 		LaneNode second = startedL.getNextEdge(first, false).getTarget();
 		LaneNode tempNode = new LaneNode(50,50, startedL);
		LaneChangeEdge<LaneNode> temp_edge = new LaneChangeEdge<LaneNode>(second, tempNode, null);
 		startedL.getContext().add(tempNode);
 		
 		startedL.getNet().addEdge(temp_edge);
 		startedL.extend(0);
 		
 		LaneEdge<LaneNode> edge = startedL.getNextEdge(second, false);
 		assertSame(second, edge.getSource());
 		assertEquals(new Point2D.Double(40,10), edge.getTarget().getLocation());
 		
 	}
 	
 	@Test	
 	public void testGetNextEdgeNoEdge() throws UnstartedLaneException, CompletedLaneException {
 		startedL.extend(0);
 		startedL.extend(0);
 		LaneNode sln = startedL.getStartNode();
 		assertNull(startedL.getNextEdge(sln, true));
 		
 		LaneNode second = startedL.getNextEdge(sln, false).getTarget();
 		LaneNode third = startedL.getNextEdge(second, false).getTarget();
 		assertNull(startedL.getNextEdge(third, false));
 	}
 	
 	@Test
 	public void testNearestNodeTo() throws UnstartedLaneException, CompletedLaneException {
 		startedL.extend(0);
 		startedL.extend(0);
 		
 		LaneNode first = startedL.getStartNode();
 		LaneNode second = startedL.getNextEdge(first, false).getTarget();
 		LaneNode third = startedL.getNextEdge(second, false).getTarget();
 		
 		LaneNode tempNode = new TemporaryLaneNode(50,50, startedL);
		LaneChangeEdge<LaneNode> temp_edge = new LaneChangeEdge<LaneNode>(second, tempNode, null);
 		startedL.getContext().add(tempNode);		
 		startedL.getNet().addEdge(temp_edge);
 
 		assertEquals(first, startedL.nodeNearest(new NdPoint(0,10)));
 		assertEquals(first, startedL.nodeNearest(new NdPoint(9,10)));
 		assertEquals(second, startedL.nodeNearest(new NdPoint(15,10)));
 		assertEquals(second, startedL.nodeNearest(new NdPoint(15,15)));
 		
 		assertEquals(third, startedL.nodeNearest(new NdPoint(50,50)));
 	}
 	
 	@Test
 	public void testGetNextNode() throws UnstartedLaneException, CompletedLaneException, NoNextNode {
 		startedL.extend(0);
 		LaneNode first = startedL.getStartNode();
 		LaneNode second = startedL.getNextEdge(first, false).getTarget();
 		LaneNode tempNode = new LaneNode(50,50, startedL);
		LaneChangeEdge<LaneNode> temp_edge = new LaneChangeEdge<LaneNode>(second, tempNode, startedL);
 		startedL.getContext().add(tempNode);
 		
 		startedL.getNet().addEdge(temp_edge);
 		startedL.extend(0);
 		LaneNode third = startedL.getNextEdge(second, false).getTarget();
 		assertSame(third, startedL.getNextNode(second, false));
 		assertSame(first, startedL.getNextNode(second, true));
 	}
 	
 	@Test(expected = NoNextNode.class)
 	public void testGetNextNodeNoNode() throws UnstartedLaneException, CompletedLaneException, NoNextNode {
 		startedL.getNextNode(startedL.getStartNode(), false);
 	}
 	
 	public static River setupRiver() {
 		Context<Object> context = new DefaultContext();
 		
 		ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
 				.createContinuousSpace("Continuous Space", context, new SimpleCartesianAdder<Object>(),
 						new repast.simphony.space.continuous.StrictBorders(), 200, 200);
 		
 		River r = RiverFactory.Test(context, space);
 		return r;
 	}
 	
 	@Test
 	public void testgetNthNodeAhead() throws NoNextNode {
 		River r = setupRiver();
 		Lane l = r.getUpstream();
 		LaneNode startNode = l.getStartNode();
 		
 		LaneNode foundNode = l.getNthNodeAhead(startNode, false, 0);
 		assertSame(startNode, foundNode);
 		
 		foundNode = l.getNthNodeAhead(startNode, false, 1);
 		assertSame(l.getNextNode(startNode, false), foundNode);
 		
 		foundNode = l.getNthNodeAhead(startNode, false, 5);
 		assertEquals(new Point2D.Double(110, 30), foundNode.getLocation());
 	}
 	
 	@Test(expected = NoNextNode.class)
 	public void testGetNthNodeAheadTooFar() throws NoNextNode {
 		River r = setupRiver();
 		Lane l = r.getUpstream();
 		LaneNode startNode = l.getStartNode();
 		l.getNthNodeAhead(startNode, false, 100);
 	}
 	
 	@Test
 	public void testGetNearestEdge() throws NoNextNode {
 		River r = setupRiver();
 		Lane l = r.getMiddle();
 		LaneEdge<LaneNode> expEdge;
 		
 		expEdge = l.getNextEdge(l.getStartNode(), false);
 		LaneEdge<LaneNode> actualEdge = l.edgeNearest(new NdPoint(10,10));
 		assertSame(expEdge, actualEdge);
 		
 		expEdge = l.getNextEdge(l.getStartNode(), false);
 		actualEdge = l.edgeNearest(new NdPoint(-10,10));
 		assertSame(expEdge, actualEdge);
 		
 		expEdge = l.getNextEdge(l.getNthNodeAhead(l.getStartNode(), false, 3), false);
 		actualEdge = l.edgeNearest(new NdPoint(75,10));
 		assertSame(expEdge, actualEdge);
 		
 	}
 	
 }
