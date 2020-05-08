 
 package graph;
 
 /**
  * Imaplements a basic Vertex object. 
  * 
  * @author <a ref ="zianfanti@gmail.com"> Zian Fanti<a/>
  */
 public class Vertex {
 
     /** The name of this vertex */
     private int name;
 
     /** Vertex parent. If it had */
     private Vertex parent;
 
     /** Flag for traversing algorithms */
     private boolean visited;
     
     /**
      * Create a vertex with an name
      * @param name Name of the vertex
      */
     public Vertex(int name) {
         this.name = name;
         this.visited = false;
     }
     
     /**
      * C
      * @param vertex 
      */
     public Vertex(Vertex vertex) {
        this.name = vertex.name;
        this.parent = vertex.parent;
     }
 
     /**
      * Return the name of this vertex
      * @return
      */
     public int getName() {
         return name;
     }
 
     /**
      * Return the parent vertex, if any.
      * @return the parent vertex, if any. Else return null;
      */
     public Vertex getParent() {
        return (parent != null) ? parent : null;
     }
 
     /**
      * Set parent of this <code>Vertex<c/code>
      * @param v
      */
     public void setParent(Vertex v) {
         this.parent = v;
     }
 
     /**
      * @return the visited
      */
     public boolean isVisited() {
         return visited;
     }
 
     /**
      * @param visited the visited to set
      */
     public void setVisited(boolean visited) {
         this.visited = visited;
     }
     
     @Override
     public int hashCode() {
         return name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Vertex) {
             return ((Vertex)obj).getName() == name;
         }
         else {
             return false;
         }
     }
 
     @Override
     public String toString() {
         return "" + name;
     }    
 }
