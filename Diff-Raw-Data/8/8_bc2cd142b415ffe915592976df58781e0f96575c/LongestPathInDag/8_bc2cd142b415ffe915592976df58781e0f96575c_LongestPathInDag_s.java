 package other;
 
 import common.graph.DfsOrder;
 import common.graph.Edge;
 import common.graph.Graph;
 
 import java.util.Arrays;
 import java.util.Stack;
 
 /**
  * Created by sobercheg on 12/8/13.
  */
public class LongestPathInDAG {
 
     private final Graph graph;
     private final int source;
     private double[] distTo;
     private int[] pathTo;
 
    public LongestPathInDAG(Graph graph, int source) {
         this.graph = graph;
         this.source = source;
         this.distTo = new double[graph.getSize()];
         this.pathTo = new int[graph.getSize()];
         for (int i = 0; i < graph.getSize(); i++) {
             distTo[i] = Double.NEGATIVE_INFINITY;
         }
         distTo[source] = 0;
 
         DfsOrder dfsOrder = new DfsOrder(graph);
         if (dfsOrder.hasCycle()) throw new IllegalArgumentException("Graph has cycles!");
 
         for (int v : dfsOrder.getReversedPostorder()) {
             if (distTo[v] > Double.NEGATIVE_INFINITY) {
                 for (Edge edge : graph.adjacent(v)) {
                     if (distTo[edge.getFrom()] + edge.getWeight() > distTo[edge.getTo()]) {
                         distTo[edge.getTo()] = distTo[edge.getFrom()] + edge.getWeight();
                         pathTo[edge.getTo()] = v;
                     }
                 }
             }
         }
     }
 
     public double[] getDistTo() {
         return distTo;
     }
 
     public Iterable<Integer> getPathTo(int w) {
         Stack<Integer> path = new Stack<Integer>();
         path.push(w);
         while (w != pathTo[w]) {
             w = pathTo[w];
             path.push(w);
         }
         return path;
     }
 
     public static void main(String[] args) {
         Graph g = new Graph(6);
         g.addEdge(0, 1, 5);
         g.addEdge(0, 2, 3);
         g.addEdge(1, 3, 6);
         g.addEdge(1, 2, 2);
         g.addEdge(2, 4, 4);
         g.addEdge(2, 5, 2);
         g.addEdge(2, 3, 7);
         g.addEdge(3, 5, 1);
         g.addEdge(3, 4, -1);
         g.addEdge(4, 5, -2);
        LongestPathInDAG longestPathInDAG = new LongestPathInDAG(g, 1);
         System.out.println(Arrays.toString(longestPathInDAG.getDistTo()));
     }
 }
