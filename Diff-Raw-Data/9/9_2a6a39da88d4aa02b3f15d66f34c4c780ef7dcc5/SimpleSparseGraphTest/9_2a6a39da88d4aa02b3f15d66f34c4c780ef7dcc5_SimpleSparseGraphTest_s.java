 package edu.uci.ics.jung.graph;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.collections15.Factory;
 
 import edu.uci.ics.graph.EdgeType;
 import edu.uci.ics.graph.Graph;
 import edu.uci.ics.graph.util.Pair;
 import edu.uci.ics.jung.graph.generators.random.TestGraphs;
 
 public class SimpleSparseGraphTest extends TestCase {
 
     Integer v0 = 0;
     Integer v1 = 1;
     Integer v2 = 2;
     Number e01 = .1;
     Number e10 = .2;
     Number e12 = .3;
     Number e21 = .4;
 
     Factory<Number> vertexFactory = new Factory<Number>() {
     	int v=0;
 		public Number create() {
 			return v++;
 		}
     };
     Factory<Number> edgeFactory = new Factory<Number>() {
     	int e=0;
 		public Number create() {
 			return e++;
 		}
     };
     
     Graph<Number,Number> graph;
     int vertexCount = 50;
     Graph<Integer,Number> smallGraph;
 
     protected void setUp() throws Exception {
         super.setUp();
         Set<Number> seeds = new HashSet<Number>();
         seeds.add(1);
         seeds.add(5);
         graph = TestGraphs.generateMixedRandomGraph(vertexFactory, edgeFactory, 
         		new HashMap<Number,Number>(), vertexCount, seeds);
         smallGraph = new SimpleSparseGraph<Integer,Number>();
         smallGraph.addVertex(v0);
         smallGraph.addVertex(v1);
         smallGraph.addVertex(v2);
         smallGraph.addEdge(e01, v0, v1);
         smallGraph.addEdge(e10, v1, v0);
         smallGraph.addEdge(e12, v1, v2);
         smallGraph.addEdge(e21, v2, v1, EdgeType.DIRECTED);
 
     }
 
     public void testGetEdges() {
         assertEquals(smallGraph.getEdges().size(), 4);
     }
 
     public void testGetVertices() {
         assertEquals(smallGraph.getVertices().size(), 3);
     }
 
     public void testAddVertex() {
         int count = graph.getVertices().size();
         graph.addVertex(count);
         assertEquals(graph.getVertices().size(), count+1);
     }
 
     public void testRemoveEndVertex() {
         int vertexCount = graph.getVertices().size();
         int edgeCount = graph.getEdges().size();
         Collection<Number> incident = graph.getIncidentEdges(vertexCount-1);
         graph.removeVertex(vertexCount-1);
         assertEquals(vertexCount-1, graph.getVertices().size());
         assertEquals(edgeCount - incident.size(), graph.getEdges().size());
     }
 
     public void testRemoveMiddleVertex() {
         int vertexCount = graph.getVertices().size();
         int edgeCount = graph.getEdges().size();
         Collection<Number> incident = graph.getIncidentEdges(vertexCount/2);
         graph.removeVertex(vertexCount/2);
         assertEquals(vertexCount-1, graph.getVertices().size());
         assertEquals(edgeCount - incident.size(), graph.getEdges().size());
     }
 
     public void testAddEdge() {
         int edgeCount = graph.getEdges().size();
         graph.addEdge(edgeFactory.create(), 0, 1);
         assertEquals(graph.getEdges().size(), edgeCount+1);
     }
 
     public void testRemoveEdge() {
     	List<Number> edgeList = new ArrayList<Number>(graph.getEdges());
         int edgeCount = graph.getEdges().size();
         graph.removeEdge(edgeList.get(edgeList.size()/2));
         assertEquals(graph.getEdges().size(), edgeCount-1);
     }
 
     public void testGetInOutEdges() {
     	for(Number v : graph.getVertices()) {
     		Collection<Number> incident = graph.getIncidentEdges(v);
     		Collection<Number> in = graph.getInEdges(v);
     		Collection<Number> out = graph.getOutEdges(v);
     		assertTrue(incident.containsAll(in));
     		assertTrue(incident.containsAll(out));
     		for(Number e : in) {
    			assertFalse(out.contains(e));
     		}
     		for(Number e : out) {
    			assertFalse(in.contains(e));
     		}
     	}
     	
         assertEquals(smallGraph.getInEdges(v1).size(), 4);
         assertEquals(smallGraph.getOutEdges(v1).size(), 3);
         assertEquals(smallGraph.getOutEdges(v0).size(), 2);
     }
 
     public void testGetPredecessors() {
         assertTrue(smallGraph.getPredecessors(v0).containsAll(Collections.singleton(v1)));
     }
 
     public void testGetSuccessors() {
         assertTrue(smallGraph.getPredecessors(v1).contains(v0));
         assertTrue(smallGraph.getPredecessors(v1).contains(v2));
     }
 
     public void testGetNeighbors() {
         Collection neighbors = smallGraph.getNeighbors(v1);
         assertTrue(neighbors.contains(v0));
         assertTrue(neighbors.contains(v2));
     }
 
     public void testGetIncidentEdges() {
         assertEquals(smallGraph.getIncidentEdges(v0).size(), 2);
     }
 
     public void testFindEdge() {
         Number edge = smallGraph.findEdge(v1, v2);
         assertTrue(edge == e12 || edge == e21);
     }
 
     public void testGetEndpoints() {
         Pair<Integer> endpoints = smallGraph.getEndpoints(e01);
         assertTrue((endpoints.getFirst() == v0 && endpoints.getSecond() == v1) ||
                 endpoints.getFirst() == v1 && endpoints.getSecond() == v0);
     }
 
     public void testIsDirected() {
         for(Number edge : smallGraph.getEdges()) {
         	if(edge == e21) {
         		assertEquals(smallGraph.getEdgeType(edge), EdgeType.DIRECTED);
         	} else {
         		assertEquals(smallGraph.getEdgeType(edge), EdgeType.UNDIRECTED);
         	}
         }
     }
 }
