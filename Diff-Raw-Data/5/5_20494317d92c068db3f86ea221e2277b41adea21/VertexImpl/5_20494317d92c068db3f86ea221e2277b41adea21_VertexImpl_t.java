 package com.github.haw.ai.gkap.graph;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public class VertexImpl<V> implements Vertex<V> {
 	private V content;
 
 	private VertexImpl(V content) {
 		if (content == null) {
 			throw new NullPointerException();
 		}
 		this.content = content;
 	}
 
 	public static <V> Vertex<V> valueOf(V content) {
 		return new VertexImpl<V>(content);
 	}
 
 	public V content() {
 		return content;
 	}
 
 	public int hashCode() {
		return content.hashCode();
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean equals(Object o) {
 		if (this == o) {
 			return true;
 		}
 		if (o == null) {
 			return false;
 		}
 		if (!(o instanceof Vertex)) {
 			return false;
 		}
 		if (content == null) {
 			if (((Vertex<V>) o).content() != null) {
 				return false;
 			}
 		}
 		if (!content.equals(((Vertex<V>) o).content())) {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 	    return content().toString();
 	}
 }
