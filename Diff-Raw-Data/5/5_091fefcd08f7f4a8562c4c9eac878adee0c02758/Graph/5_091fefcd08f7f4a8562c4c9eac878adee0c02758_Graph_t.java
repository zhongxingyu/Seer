 package com.github.haw.ai.gkap.graph;
 
 import java.util.Set;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public interface Graph<E,V> {
 
   /**
    * @return the degree of a given Vertex
    */
   public int degree(Vertex<V> vertex);
 
   /**
    * @return the Set of Vertices that are adjacent to the given Vertex
    */
  public Set<Vertex<V>> adjacent(Vertex<V> vertex, Vertex<V> otherVertex);
 
   /**
    * @return the Set of Edges that are incident with the given Vertex
    */
  public Set<Edge<E,V>> incident(Vertex<V> vertex, Edge<E> edge);
 }
