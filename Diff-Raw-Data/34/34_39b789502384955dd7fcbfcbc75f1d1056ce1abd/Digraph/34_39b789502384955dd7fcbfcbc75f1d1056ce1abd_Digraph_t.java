 package org.javaan.graph;
 
 import java.util.Set;
 
 /**
  * Represents a digraph without multi-edges.  
  * Provides methods for querying and traversing graph.
  * 
  * @param <N> type of graph nodes
  */
 public interface Digraph<N extends Comparable<? super N>> extends Graph<N> {
 	
 	int size();
 
 	Set<N> getChilds(N parent);
 	
 	Set<N> getParents(N child);
 	
 	Set<N> getSuccessors(N parent);
 	
 	Set<N> getPredecessors(N child);
 
 	boolean hasChilds(N parent);
 
	Set<N> getLeafNodes(N node);
 	
 	void traverseSuccessorsBreadthFirst(N node, int depth, Visitor<N> visitor);
 	
 	void traverseSuccessorsDepthFirst(N node, int depth, Visitor<N> visitor);
 
 	void traversePredecessorsBreadthFirst(N node, int depth, Visitor<N> visitor);
 	
 	void traversePredecessorsDepthFirst(N node, int depth, Visitor<N> visitor);
 }
