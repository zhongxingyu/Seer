 package com.github.haw.ai.gkap.graph;
 
 import java.util.Set;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public class GraphImpl<E, V> implements Graph<E, V> {
	public Graph<E,V> valueOf(Set<Edge<E,V>> edges, Set<Vertex<V>> vertexes) {
		return new GraphImpl<E, V>(edges, vertexes);
 	}
	private GraphImpl(Set<Edge<E,V>> edges, Set<Vertex<V>> vertexes) {
 		
 	}
 }
