 package com.github.haw.ai.gkap.graph;
 
 import java.util.Set;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public class GraphImpl<E,V> implements Graph<E, V> {
	public static Graph<E,V> valueOf(Set<Edge<E,V>> edges, Set<Vertex<V>> vertices) {
 		return new GraphImpl<E,V>(edges, vertices);
 	}
 	private GraphImpl(Set<Edge<E,V>> edges, Set<Vertex<V>> vertices) {
 		
 	}
 	
 	@Override
 	public int degree(Vertex<V> vertex) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public Set<Vertex<V>> adjacent(Vertex<V> vertex, Vertex<V> otherVertex) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public Set<Edge<E, V>> incident(Vertex<V> vertex, Edge<E, V> edge) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
