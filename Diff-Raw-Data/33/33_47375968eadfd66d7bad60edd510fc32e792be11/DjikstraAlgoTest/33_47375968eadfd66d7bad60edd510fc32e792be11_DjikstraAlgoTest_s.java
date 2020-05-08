 package ch.zhaw.jroute.model.algorithm;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import ch.zhaw.jroute.model.Way;
 import ch.zhaw.jroute.model.Waypoint;
 
 public class DjikstraAlgoTest {
 	List<Way> allWays;
 	Waypoint A;
 	Waypoint B;
 	Waypoint C;
 	Waypoint D;
 	Waypoint E;
 	Waypoint F;
 	Waypoint G;
 	Waypoint H;
 	Waypoint I;
 	Way way1;
 	Way way2;
 	Way way3;
 	Way way4;
 	Way way5;
 	Way way6;
 	Way way7;
 	Way way8;
 	Way way9;
 
 	public void createTestSzenario() {
 
 		allWays = new ArrayList<Way>();
 
 		A = new Waypoint("A");
 		B = new Waypoint("B");
 		C = new Waypoint("C");
 		D = new Waypoint("D");
 		E = new Waypoint("E");
 		F = new Waypoint("F");
 		G = new Waypoint("G");
 		H = new Waypoint("H");
 		I = new Waypoint("I");
 
 		way1 = new Way(1, A, B, 2);
 		way2 = new Way(2, A, C, 5);
 		way3 = new Way(3, A, D, 7);
 		way4 = new Way(4, B, E, 8);
 		way5 = new Way(5, D, H, 6);
 		way6 = new Way(6, C, F, 15);
 		way7 = new Way(7, E, A, 9);
 		way8 = new Way(8, B, G, 1);
 		way9 = new Way(9, B, I, 6);
 
 		allWays.add(way1);
 		allWays.add(way2);
 		allWays.add(way3);
 		allWays.add(way4);
 		allWays.add(way5);
 		allWays.add(way6);
 		allWays.add(way7);
 		allWays.add(way8);
 		allWays.add(way9);
 
 	}
 
 	/**
 	 * check a route from point A to point G
 	 * 
 	 * @author Pascal S.
 	 */
 	@Test
 	public void shortestPathPart1() {
 
 		createTestSzenario();
 
 		Waypoint startWaypoint = A;
 		Waypoint endWaypoint = G;
 
 		DjikstraAlgo algo = new DjikstraAlgo();
 
 		List<Way> shortestWay = algo.getShortestPath(startWaypoint,
 				endWaypoint, allWays);
 
 		int WayID0 = shortestWay.get(0).getWayID();
 		int WayID1 = shortestWay.get(1).getWayID();
 
 		int ExpectedWayID0 = 1;
 		int ExpectedWayID1 = 8;
 
 		// variant 1
 		assertEquals(ExpectedWayID0, WayID0);
 		assertEquals(ExpectedWayID1, WayID1);
 
 		// variant 2
 		assertTrue(WayID0 == ExpectedWayID0);
 		assertTrue(WayID1 == ExpectedWayID1);
 
 	}
 
 	/**
 	 * check a route from point A to point I
 	 * 
 	 * @author Pascal S.
 	 */
 	@Test
 	public void shortestPathPart2() {
 
 		createTestSzenario();
 
 		Waypoint startWaypoint = A;
 		Waypoint endWaypoint = I;
 
 		DjikstraAlgo algo = new DjikstraAlgo();
 
 		List<Way> shortestWay = algo.getShortestPath(startWaypoint,
 				endWaypoint, allWays);
 
 		int WayID0 = shortestWay.get(0).getWayID();
 		int WayID1 = shortestWay.get(1).getWayID();
 
 		int ExpectedWayID0 = 1;
 		int ExpectedWayID1 = 9;
 
 		assertEquals(ExpectedWayID0, WayID0);
 		assertEquals(ExpectedWayID1, WayID1);
 
 		assertTrue(WayID0 == ExpectedWayID0);
 		assertTrue(WayID1 == ExpectedWayID1);
 
 	}
 
 }
