 package com.tinkerpop.blueprints.pgm.impls.hollow.util;
 
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowEdge;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * @author Elaine Angelino (http://www.eecs.harvard.edu/~elaine)
  */
 public class HollowEdgeSequence implements Iterable<Edge>, Iterator<Edge> {
 	
     private HollowGraph graph = null;
     private long vid = -1;
     private boolean getOut = true;
     
     private long i = 0;
     private long degree = 0;
     
     public HollowEdgeSequence(final HollowGraph graph) {
         this.graph = graph;
         
         this.degree = graph.edgeCount;
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut) {
         this.graph = graph;
         this.vid = vid;
         this.getOut = true;
         
         long avgDegree = (graph.edgeCount + graph.vertexCount - 1) / graph.vertexCount;
         this.degree = (long) (this.graph.rand.nextDouble() * (2 * avgDegree + 1));
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut, final String[] labels) {
         this.graph = graph;
         this.vid = vid;
         this.getOut = true;
         
         long avgDegree = (graph.edgeCount + graph.vertexCount - 1) / graph.vertexCount;
         this.degree = (long) (this.graph.rand.nextDouble() * (2 * avgDegree + 1));
     }
 	
 	public Edge next() {
 		if (this.i++ < this.degree ) {
 			if (this.vid == -1)
				return new HollowEdge(this.graph, this.i, (long) (this.graph.rand.nextDouble() * this.graph.vertexCount), (long) (this.graph.rand.nextDouble() * this.graph.vertexCount), "");
 			else if (getOut)
				return new HollowEdge(this.graph, (long) (this.graph.rand.nextDouble() * this.graph.edgeCount), this.vid, (long) (this.graph.rand.nextDouble() * this.graph.vertexCount), "");
 			else
				return new HollowEdge(this.graph, (long) (this.graph.rand.nextDouble() * this.graph.edgeCount), (long) (this.graph.rand.nextDouble() * this.graph.vertexCount), this.vid, "");
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 	public boolean hasNext() {
     	return i < this.degree;
     }
 	
 	public void close() {
 		i = this.degree;
 	}
 
     public void remove() { 
         throw new UnsupportedOperationException(); 
     } 
 
     public Iterator<Edge> iterator() {
         return this;
     }
 }
