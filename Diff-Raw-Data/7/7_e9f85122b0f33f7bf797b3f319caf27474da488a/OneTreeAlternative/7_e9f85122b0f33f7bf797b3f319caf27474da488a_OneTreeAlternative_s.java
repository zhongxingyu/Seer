 package edu.aa12;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Author: Philip Pickering <pgpick@gmx.at>
  * Date: 5/30/12
  * Time: 3:22 PM
  */
 public class OneTreeAlternative extends OneTreeStrategy {
 
     OneTreeAlternative(Graph g) {
         super(g);
     }
 
 
     public double lowerBound(Graph graph, BnBNode node) {
        int one = 0;

         BnBNode news = new BnBNode(null, null, false);
         List<Edge> edges = new LinkedList<Edge>(graph.incidentEdges[one]);
 
         for (Edge e : edges) {
             news = new BnBNode(news, e, false);
         }
 
 
         List<Edge> mst = kruskal.minimumSpanningTree(graph, news);
 
         for (int i = 0; i < 2; i++) {
             Edge e = popMin(edges);
             if (e.equals(node.edge)) e = popMin(edges);
 
             mst.add(e);
         }
        return 0;
     }
 }
