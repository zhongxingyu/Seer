 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gka.pathfinders;
 
 import java.util.*;
 import org.jgrapht.Graph;
 
 /**
  *
  * @author maiwald
  */
 public class FloydWarshall<V>
 {
     private final Graph g;
     private final List<V> vertices;
 
     private Map<V, Map<V, Double>> d;
     private Map<V, Map<V, V>> t;
 
     public int counter = 0;
 
     static class NegativeCircleFoundException extends RuntimeException
     {};
 
     public FloydWarshall(Graph g)
     {
         this.g = g;
         this.vertices = new ArrayList<V>(g.vertexSet());
 
         initializeDMatrix();
         initializeTMatrix();
         calculateShortestPaths();
     }
 
     public List<V> getShortestPath(V source, V target)
     {
         if (this.d.get(source).get(target) == Double.POSITIVE_INFINITY)
             return null;
 
         V intermediate = this.t.get(source).get(target);
         List<V> result = new LinkedList();
 
         if (intermediate == null)
         {
             result.add(0, target);
             result.add(0, source);
         }
         else
         {   
             result.addAll(getShortestPath(source, intermediate));
             
             List<V> temp = getShortestPath(intermediate, target);
             temp.remove(0);
             result.addAll(temp);
         }
 
         return result;
     }
 
     private void initializeDMatrix()
     {
         this.d = new HashMap<V, Map<V, Double>>();
         for (V row : this.vertices)
         {
             this.d.put(row, new HashMap<V, Double>());
 
             for (V col : this.vertices)
             {
                 Double value = null;
 
                 if (col.equals(row))
                     value = 0d;
 
                 else if (this.g.getEdge(row, col) != null)
                     value = this.g.getEdgeWeight(this.g.getEdge(row, col));
 
                 else
                     value = Double.POSITIVE_INFINITY;
 
                 this.d.get(row).put(col, value);
             }
         }
     }
 
     private void initializeTMatrix()
     {
         this.t = new HashMap<V, Map<V, V>>();
         for (V row : this.vertices)
         {
             this.t.put(row, new HashMap<V, V>());
 
             for (V col : this.vertices)
                 this.t.get(row).put(col, null);
         }
     }
 
     private void calculateShortestPaths()
     {
         for (V j : this.vertices)
         {
             for (V i : this.vertices)
             {
                 if (!i.equals(j))
                 {
                     for (V k : this.vertices)
                     {
                         if (!k.equals(j))
                         {
                             Double new_value = this.d.get(i).get(j) + this.d.get(j).get(k);
                             if (new_value < this.d.get(i).get(k))
                             {
                                 this.d.get(i).put(k, new_value);
                                 this.t.get(i).put(k, j);
                             }
                         }
                     }
                     
                    if (this.d.get(i).get(j) < 0)
                         throw new NegativeCircleFoundException();
                 }
             }
         }
     }
 }
