 package storage;
 
 import java.util.Set;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import query.BreadthFirstSearch;
 import query.DepthFirstSearch;
 import query.QueryPlan;
 
 /**
  * A node is a container that is stored in a {@link Graph}. It encapsulates the
  * data to be stored as well as storing the adjacency lists for graph links.
  * 
  * @author Varley
  * 
  * @param <T>
  *            The type of the data to be contained. Ideally the type should
  *            override the Java default implementation of
  *            {@link Object#equals(Object)} and {@link Object#hashCode()}.
  */
 public class Node<T> implements java.lang.Comparable<Node<?>> {
 	private T data;
 
 	/**
 	 * This contains all relationships that have this node as the origin. The
 	 * set implementation is designed for concurrent access.
 	 */
 	public Set<RelationshipHolder> exitRelations;
 
 	/**
 	 * This contains all relationships that have this node as the destination.
 	 * The set implementation is designed for concurrent access.
 	 */
 	public Set<RelationshipHolder> entranceRelations;
 
 	/**
 	 * Make a container that will store the data passed in.
 	 * 
 	 * @param data
 	 *            The data to be stored
 	 * 
 	 * @thows NullPointerException If the parameter is <code>null</code>.
 	 */
 	public Node(T data) {
 		if (data == null) {
 			throw new NullPointerException("The data to be stored was null");
 		}
 		this.data = data;
 
 		exitRelations = new ConcurrentSkipListSet<RelationshipHolder>();
 		entranceRelations = new ConcurrentSkipListSet<RelationshipHolder>();
 
 	}
 
 	/**
 	 * 
 	 * @return The data stored in this container.
 	 */
 	public T getData() {
 		return this.data;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((data == null) ? 0 : data.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Node<?> other = (Node<?>) obj;
 		if (data == null) {
 			if (other.data != null) {
 				return false;
 			}
 		} else if (!data.equals(other.data)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int compareTo(Node<?> o) {
		if (this.getData().equals(o.getData())) {
			return 0;
		} else {
			return 1;
		}
 	}
 
 	/**
 	 * Removes the relationship
 	 * 
 	 * @param holder The relationship to remove
 	 */
 	void removeRelationship(RelationshipHolder holder) {
 		this.entranceRelations.remove(holder);
 		this.exitRelations.remove(holder);
 	}
 
 	public QueryPlan breadthFirst() {
 		return new QueryPlan(new BreadthFirstSearch(this, Integer.MAX_VALUE));
 	}
 
 	public QueryPlan breadthFirst(int maxDepth) {
 		return new QueryPlan(new BreadthFirstSearch(this, maxDepth));
 	}
 
 	public QueryPlan depthFirst() {
 		return new QueryPlan(new DepthFirstSearch(this, Integer.MAX_VALUE));
 	}
 
 	public QueryPlan depthFirst(int maxDepth) {
 		return new QueryPlan(new DepthFirstSearch(this, maxDepth));
 	}
 }
