 /**
  * Created with IntelliJ IDEA.
  * User: ayoung
  * Date: 4/11/13
  * Time: 4:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Graph {
 
     private int numVertices;
    private double adjMat[][];
 
     public Graph(File f)
     {
 
     }
 
     /* Generate random graph */
     public Graph(int numVertices, double maxWeight)
     {
 
     }
 
     public Edge addEdge(Vertex v1, Vertex v2, double weight)
     {
         Edge e = new Edge(v1, v2, weight);
 
         adjMat[v1.getId()][v2.getId()] = weight;
 
         return e;
     }
 }
