 import edu.uci.ics.jung.graph.*;
 import edu.uci.ics.jung.graph.util.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.LinkedList;
 
 public class EdmondsKarp {
 
 	DirectedSparseGraph<EdmondsVertex,EdmondsEdge> f;
         EdmondsVertex source, sink;
 
 	EdmondsKarp(DirectedSparseGraph<EdmondsVertex, EdmondsEdge> f) {
 		this.f = f;
 
                 // this.source = source;
                 // this.sink = sink;
 	}
 
         public int maxFlow(EdmondsVertex source, EdmondsVertex sink) {
             for (EdmondsEdge i: f.getEdges()) {
                 i.setNewFlow(0);
             }
 
             // While there is a path with available capacity...
 	    System.out.println("Before");
             long capacity = findPath(source,sink);
 	    System.out.println("After");
             while (capacity != 0) {
                 // Travel backwards from the sink, adjusting the node capacities
                 //  as we go back.
                 EdmondsVertex currentV = sink;
                 while (currentV != source) {
                     f.findEdge(currentV.parentNode, currentV).addFlow((int)capacity);
                     currentV = currentV.parentNode;
                 }
                 capacity = findPath(source,sink);
 		//System.out.println(capacity);
             }
             Collection<EdmondsVertex> vertices;
             return 1;
         }
         /**
          * Finds a path with remaining capacity between s and t.
          * Returns the capacity of the path it has found. Also, this method
          * stores the path in the EdmondsVertex instances so it is possible
          * to work backwards using EdmondsVertex.parentNode from the sink.
          * @param s Source node
          * @param t Destination node
          */
         private long findPath(EdmondsVertex s, EdmondsVertex t) {
             // First, reset the Verticies to undiscovered
             for(EdmondsVertex i: f.getVertices()) {
 		    System.out.println("WAT");
                 i.discoveredState = -1;
                 i.parentNode = null;
             }
             s.discoveredState = -2;
             // Use a LinkedList as a Queue for finding verticies
             LinkedList<EdmondsVertex> discoveredV = new LinkedList<EdmondsVertex>();
             // Keep an array of distances from the source
	    //
             discoveredV.add(s);
 
             while (discoveredV.size() > 0) {
                 EdmondsVertex currentV = discoveredV.removeFirst();
                 for (EdmondsVertex adjV: f.getNeighbors(currentV)) {
                     EdmondsEdge connection = f.findEdge(currentV, adjV);
                     if (adjV.discoveredState == -1 && (connection.getRemainingCapacity() > 0)) {
                         adjV.parentNode = currentV;
                         adjV.pathCapacityToNode = Math.min(currentV.pathCapacityToNode, connection.getRemainingCapacity());
                         if (adjV != t) {
                             discoveredV.add(adjV);
                         } else {
                             return adjV.pathCapacityToNode;
                         }
                     }
                 }
             }
             return 0;
         }
         //private int capacity(EdmondsVertex a, EdmondsVertex b) {
 
         //}
 }
