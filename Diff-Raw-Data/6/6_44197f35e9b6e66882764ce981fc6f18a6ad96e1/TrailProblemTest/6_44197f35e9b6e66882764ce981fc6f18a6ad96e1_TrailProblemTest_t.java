 package A4_test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import GKA_A1.IAIGraph;
 import GKA_A4.Hierholzer;
 import static GKA_A4.Hierholzer.*;
 import GKA_A4.NearestInsertion;
 import GraphUtils.JavaGraphParser;
 import GraphUtils.WhichPath;
 
 public class TrailProblemTest {
 
 	private String cmp = "km";
 	private IAIGraph yolo;
 
 	public TrailProblemTest() {
 	}
 
 	// NearestInsertion
 	@Test
 	public void NearestInsertionTest1() {
 		loadGraph("graph30.graph");
 		// this graph is the graph the algorithm is presented in the book with.
 		// write here call of nearest insertion algorithm and its test case
 
 		NearestInsertion algo = new NearestInsertion(yolo, cmp);
 		List<Long> bestCycle = algo.nearestInsertion();
 
 		System.out.println("BestCycle: " + bestCycle);
 		System.out.println("Minimum: " + algo.getMinimum());
 
 		assertTrue(algo.getMinimum() <= 120);
 	}
 
 	@Test
 	public void NearestInsertionTest2() {
 		loadGraph("graph31.graph");
 		// this graph is the graph used in the exercises for the nearest
 		// insertion algorithm
 		// write here call of nearest inserteion algorithm and its test case
 
 		NearestInsertion algo = new NearestInsertion(yolo, cmp);
 		List<Long> bestCycle = algo.nearestInsertion();
 
 		System.out.println("BestCycle: " + bestCycle);
 		System.out.println("Minimum: " + algo.getMinimum());
 
 		assertTrue(algo.getMinimum() <= 33);
 	}
 
 	// Hierholzer
 	// isEulerianPath
 	@Test
 	public void TestIsEulerianPath() {
 		loadGraph("graph35.graph");
 
 		List<Long> eulerpath = new ArrayList<>(Arrays.asList(0L, 1L, 2L, 3L));
 		assertTrue(isEulerianPath(yolo, eulerpath));
 
 		eulerpath = new ArrayList<>(Arrays.asList(0L, 1L, 2L, 1L, 3L));
 		assertTrue(!isEulerianPath(yolo, eulerpath));
 
 		eulerpath = new ArrayList<>(Arrays.asList(0L));
 		assertTrue(!isEulerianPath(yolo, eulerpath));
 
 		eulerpath = new ArrayList<>(Arrays.asList(2L, 0L, 1L, 3L));
 		assertTrue(!isEulerianPath(yolo, eulerpath));
 	}
 
 	// Hierholzer Algorithm
 	@Test
 	public void TestHierholzerPositive1() {
 		loadGraph("graph35.graph");
 		Hierholzer algo = new Hierholzer(yolo);
 		List<Long> result = algo.hierholzeEs();
 		System.out.println("Eulertour(edges): " + result);
 		assertTrue(isEulerianPath(yolo, result));
 	}
 
 	@Test
 	public void TestHierholzerPositive2() {
 		loadGraph("graph36.graph");
 		Hierholzer algo = new Hierholzer(yolo);
 		List<Long> result = algo.hierholzeEs();
 		System.out.println("Eulertour(edges): " + result);
 		assertTrue(isEulerianPath(yolo, result));
 	}
 
 	@Test
 	public void TestHierholzerPositive3() {
 		loadGraph("graph39.graph");
 		// more difficult graph which names the vertices in such a way
 		// so that a merging of two cycles is nessesary
 		Hierholzer algo = new Hierholzer(yolo);
 		List<Long> result = algo.hierholzeEs();
 		System.out.println("Eulertour(edges): " + result);
 		assertTrue(isEulerianPath(yolo, result));
 	}
 
 	@Test
 	public void TestHierholzerPositive4() {
 		loadGraph("graph42.graph"); // this is the one from the book page 121.
 									// its the one with 8 vertices
 		Hierholzer algo = new Hierholzer(yolo);
 		List<Long> result = algo.hierholzeEs();
 		System.out.println("Eulertour(edges): " + result);
 		assertTrue(isEulerianPath(yolo, result));
 	}
 
	// @Test
 	public void TestHierholzerPositive5() {
 		loadGraph("graph43.graph");
		// form the german wikipedia article:
		// https://de.wikipedia.org/wiki/Algorithmus_von_Hierholzer
 		Hierholzer algo = new Hierholzer(yolo);
 		List<Long> result = algo.hierholzeEs();
 		System.out.println("Eulertour(edges): " + result);
 		assertTrue(isEulerianPath(yolo, result));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void TestHierholzerNegative1() {
 		loadGraph("graph37.graph"); // invalid graph. has vertices with ood
 									// degrees
 		Hierholzer algo = new Hierholzer(yolo);
 		algo.hierholzeEs();
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void TestHierholzerNegative2() {
 		loadGraph("graph38.graph"); // invalid graph. has vertices with ood
 									// degrees
 		Hierholzer algo = new Hierholzer(yolo);
 		algo.hierholzeEs();
 
 	}
 
 	// TODO: activate all Tests
 
 	private void loadGraph(String graphname) {
 		String path = WhichPath.getPath();
 		JavaGraphParser jp = new JavaGraphParser(path + graphname, cmp);
 		this.yolo = jp.createGraph();
 		System.out.println("\n<======= New Test =======>\n Input Graph: "
 				+ yolo);
 	}
 }
