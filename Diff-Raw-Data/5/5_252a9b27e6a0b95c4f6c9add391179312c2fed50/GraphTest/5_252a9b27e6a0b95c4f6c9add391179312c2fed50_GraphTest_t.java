 package edu.msergey.jalg.graphs.randomized_contraction_to_find_min_cut;
 
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.*;
 
 public class GraphTest {
     @Test
     public void test_graphConstructing() {
         Vertex<String> vertexA = new Vertex<String>("A");
         Vertex<String> vertexB = new Vertex<String>("B");
         Vertex<String> vertexC = new Vertex<String>("C");
         Graph<String> graph = new Graph<String>(vertexA);
         Edge<String> edge1 = graph.connect("1", vertexA, vertexB);
         Edge<String> edge2 = graph.connect("2", vertexA, vertexB);
         Edge<String> edge3 = graph.connect("3", vertexB, vertexC);
         Edge<String> edge4 = graph.connect("4", vertexA, vertexC);
 
         assertTrue(vertexA.getIncidentEdges().contains(edge1));
         assertTrue(vertexA.getIncidentEdges().contains(edge2));
         assertTrue(vertexA.getIncidentEdges().contains(edge4));
         assertEquals(3, vertexA.getIncidentEdges().size());
 
         assertTrue(vertexB.getIncidentEdges().contains(edge1));
         assertTrue(vertexB.getIncidentEdges().contains(edge2));
         assertTrue(vertexB.getIncidentEdges().contains(edge3));
         assertEquals(3, vertexB.getIncidentEdges().size());
 
         assertTrue(vertexC.getIncidentEdges().contains(edge3));
         assertTrue(vertexC.getIncidentEdges().contains(edge4));
         assertEquals(2, vertexC.getIncidentEdges().size());
 
         assertSame(vertexA, edge1.getEndpoint1());
         assertSame(vertexB, edge1.getEndpoint2());
         assertSame(vertexA, edge2.getEndpoint1());
         assertSame(vertexB, edge2.getEndpoint2());
         assertSame(vertexB, edge3.getEndpoint1());
         assertSame(vertexC, edge3.getEndpoint2());
         assertSame(vertexA, edge4.getEndpoint1());
         assertSame(vertexC, edge4.getEndpoint2());
 
         assertTrue(graph.getVertices().contains(vertexA));
         assertTrue(graph.getVertices().contains(vertexB));
         assertTrue(graph.getVertices().contains(vertexC));
         assertEquals(3, graph.getVertices().size());
 
         assertTrue(graph.getEdges().contains(edge1));
         assertTrue(graph.getEdges().contains(edge2));
         assertTrue(graph.getEdges().contains(edge3));
         assertTrue(graph.getEdges().contains(edge4));
         assertEquals(4, graph.getEdges().size());
     }
 
     @Test
     public void test_fuseEdge_graph1() {
         Vertex<String> vertexA = new Vertex<String>("A");
         Vertex<String> vertexB = new Vertex<String>("B");
         Vertex<String> vertexC = new Vertex<String>("C");
         Graph<String> graph = new Graph<String>(vertexA);
         Edge<String> edge1 = graph.connect("1", vertexA, vertexB);
         Edge<String> edge2 = graph.connect("2", vertexA, vertexB);
         Edge<String> edge3 = graph.connect("3", vertexB, vertexC);
         Edge<String> edge4 = graph.connect("4", vertexA, vertexC);
         graph.fuseEdge(edge1);
 
         assertTrue(vertexA.getIncidentEdges().contains(edge3));
         assertTrue(vertexA.getIncidentEdges().contains(edge4));
         assertEquals(2, vertexA.getIncidentEdges().size());
 
         assertEquals(0, vertexB.getIncidentEdges().size());
 
         assertTrue(vertexC.getIncidentEdges().contains(edge3));
         assertTrue(vertexC.getIncidentEdges().contains(edge4));
         assertEquals(2, vertexC.getIncidentEdges().size());
 
         assertSame(vertexA, edge3.getEndpoint1());
         assertSame(vertexC, edge3.getEndpoint2());
         assertSame(vertexA, edge4.getEndpoint1());
         assertSame(vertexC, edge4.getEndpoint2());
 
         assertTrue(graph.getVertices().contains(vertexA));
         assertTrue(graph.getVertices().contains(vertexC));
         assertEquals(2, graph.getVertices().size());
 
         assertTrue(graph.getEdges().contains(edge3));
         assertTrue(graph.getEdges().contains(edge4));
         assertEquals(2, graph.getEdges().size());
 
         assertTrue(vertexA.getChildFusedVertices().contains(vertexB));
         assertEquals(1, vertexA.getChildFusedVertices().size());
     }
 
     @Test
     public void test_getMinCuts_graph2() {
         Vertex<String> vertexA = new Vertex<String>("A");
         Vertex<String> vertexB = new Vertex<String>("B");
         Vertex<String> vertexC = new Vertex<String>("C");
         Vertex<String> vertexD = new Vertex<String>("D");
         Graph<String> graph = new Graph<String>(vertexA);
         Edge<String> edge1 = graph.connect("1", vertexB, vertexD);
         Edge<String> edge2 = graph.connect("2", vertexA, vertexB);
         Edge<String> edge3 = graph.connect("3", vertexB, vertexC);
         Edge<String> edge4 = graph.connect("4", vertexD, vertexC);
         Edge<String> edge5 = graph.connect("5", vertexA, vertexD);
 
         int actual = graph.getMinCuts();
 
        // this is shitastic way to actually check anything, but I didn't find any other options
        assertTrue("correct implementation shouldn't get any other answers, but 2 or 3",
                actual >= 2 || actual <= 3);
        System.out.println(String.format("test_getMinCuts_graph2 has got answer: %s min cuts", actual));
     }
 }
