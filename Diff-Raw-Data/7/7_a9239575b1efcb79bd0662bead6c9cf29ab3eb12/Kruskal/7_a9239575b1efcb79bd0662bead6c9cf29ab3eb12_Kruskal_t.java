 /* Kruskal.java */
 
 import graph.*;
 import set.*;
 import dict.*;
 import DList.*;
 import Constants.*;
 import sort.*;
 
 /**
  * The Kruskal class contains the method minSpanTree(), which implements
  * Kruskal's algorithm for computing a minimum spanning tree of a graph.
  */
 public class Kruskal {
 
   /**
    *  minSpanTree() returns a WUGraph that represents the minimum spanning tree
    *  of the WUGraph g.  The original WUGraph g is NOT changed.
    **/
   public static WUGraph minSpanTree(WUGraph g) {
     //Create a hashtable to map vertices to integers in the DisjointSets object
     Hashtable vertexToInt = new Hashtable(g.vertexCount());
 
     //Create a graph with the same vertices as g.
     WUGraph out = new WUGraph();
     Object[] vertices = g.getVertices();
     for (int i = 0; i < vertices.length; i++){
       out.addVertex(vertices[i]);
       vertexToInt.insert(vertices[i],i);
     }
 
     //Get and sort all the edges of g
     EdgeWrapper[] edges = getEdges(g);
     SortEdge.sort(edges);
 
     //Map the vertices of g to a DisjointSets object
     DisjointSets sets=new DisjointSets(vertices.length);
     for (EdgeWrapper edge:edges){
      int root1=sets.find((Integer)(vertexToInt.find(edge.v1)));
      int root2=sets.find((Integer)(vertexToInt.find(edge.v2)));
       if (root1!=root2){
         out.addEdge(edge.v1,edge.v2,edge.weight);
         sets.union(root1,root2);
       }
     }
 
     return out;
   }
  
   /**
    *  getEdges() gets an array of the edges from a given WUGraph.
    *
    *  @param g the WUGraph from which to extract the edges.
    *  @return an EdgeWrapper array containing all the edges from g.
    **/
   private static EdgeWrapper[] getEdges(WUGraph g){
     Object[] vertices = g.getVertices();  // list of vertices
     Hashtable visited = new Hashtable();  // list of visited edges
     EdgeWrapper[] edges = new EdgeWrapper[g.edgeCount()]; // list of edges to be returned
     int index = 0;
     for(Object v : vertices){
       Neighbors neighbors = g.getNeighbors(v);
       for(int i = 0; i < neighbors.neighborList.length; i++){
         EdgeWrapper curr = new EdgeWrapper(v,neighbors.neighborList[i], neighbors.weightList[i]); 
         // if the edge hasn't been visited, add it to the list
         if(visited.find(curr) == null){ 
           visited.insert(curr,neighbors.weightList[i]); 
           edges[index] = curr;
           index += 1;
         }
       }
     }
     return edges;
   }
 
   public static void main(String[] args){
     WUGraph w = new WUGraph();
     w.addVertex(1);
     w.addVertex(2);
     w.addVertex(3);
     w.addVertex(4);
     w.addEdge(4,3,4);
     w.addEdge(1,4,3);
     w.addEdge(1,3,2);
     w.addEdge(1,2,1);
 
     minSpanTree(w);
   }
 }
 
 /**
  *  The EdgeWrapper class does TODO: fill this out
  **/
 class EdgeWrapper implements Comparable{
 
   /**
    *  Member Variables. TODO: fill this out
    *
    *  v1
    *  v2
    *  weight
    **/
   public Object v1;
   public Object v2;
   public int weight;
 
   /**
    *  EdgeWrapper constructor creates a new edge wrapper object.
    *  
    *  @param v1
    *  @param v2
    *  @param weight
    **/
   public EdgeWrapper(Object v1, Object v2, int weight){
     this.v1=v1;
     this.v2=v2;
     this.weight=weight;
   }
   /**
    *  hashCode() gives a unique integer value representing this edge wrapper.
    *  This gives equal hash codes for two edge wrapper objects with equal
    *  fields.
    *
    *  @return a hash code value for this edge wrapper.
    **/
   @Override
   public int hashCode() {
     if (v1.equals(v2)) {
       return v1.hashCode() + 1;
     } else {
       return v1.hashCode() + v2.hashCode();
     }   
   }
 
   /** 
    * equals() returns true if this VertexPair represents the same unordered
    * pair of objects as the parameter "o".  The order of the pair does not
    * affect the equality test, so (u, v) is found to be equal to (v, u).
    */
   @Override
   public boolean equals(Object o) {
     if (o instanceof EdgeWrapper) {
       return ((v1.equals(((EdgeWrapper) o).v1)) &&
               (v2.equals(((EdgeWrapper) o).v2))) ||
              ((v1.equals(((EdgeWrapper) o).v2)) &&
               (v2.equals(((EdgeWrapper) o).v1)));
     } else {
       return false;
     }   
   }
   
   /**
    *  compareTo() compares this edge wrapper to another object to
    *  determine ordering. If this object is less than the given
    *  object, a negative valued is returned, equals-zero, greater 
    *  than-positive. If the given object is not also an instance
    *  of the EdgeWrapper class, a ClassCastException is thrown.
    *
    *  @param c the object to be compared to.
    *  @return an integer representation of the given objects' ordering.
    **/
  @Override
   public int compareTo(Object c){
     if (((EdgeWrapper) c).weight==this.weight){
       return 0;
     }
     return (((EdgeWrapper) c).weight>this.weight)?-1:1;
   }
 }
