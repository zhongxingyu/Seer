 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.logical;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.collect.Iterators.concat;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.AbstractTreeIterator;
 
 /**
  * This structure will be used to maintain a undirected graph of elements.
  * <p>
  * This boils down to maintaining a list of children and parents of each node, updating them in sync with each
  * other.
  * </p>
  * <p>
  * Take note that the elements of this graph are not necessarily all connected together. This can be used to
  * represent a set of trees, a set of undirected graphs, a set of roots with no children...
  * </p>
  * 
  * @param <E>
  *            Kind of elements used as this graph's nodes.
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class Graph<E> {
 	/** Keeps track of this graph's individual nodes. */
 	private final Map<E, Node<E>> nodes;
 
 	/** Constructs an empty graph. */
 	public Graph() {
 		this.nodes = new LinkedHashMap<E, Node<E>>();
 	}
 
 	/**
 	 * Checks whether this graph already contains the given element.
 	 * 
 	 * @param element
 	 *            Element we need to check.
 	 * @return <code>true</code> if this graph already contains the given elment, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean contains(E element) {
 		synchronized(nodes) {
 			return nodes.containsKey(element);
 		}
 	}
 
 	/**
 	 * Adds a new element to this graph, if it does not exists yet. Elements will initially have no connection
 	 * to other elements, and can thus be considered "roots" of the graph.
 	 * 
 	 * @param element
 	 *            The element to add as a new root to this graph.
 	 * @return <code>true</code> if this element did not previously exist in the graph.
 	 */
 	public boolean add(E element) {
 		synchronized(nodes) {
 			Node<E> node = nodes.get(element);
 			if (node == null) {
 				node = new Node<E>(element);
 				nodes.put(element, node);
 				return true;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Removes the given element's node from this graph. This will effectively break all connections to that
 	 * node.
 	 * 
 	 * @param element
 	 *            The element which is to be removed from this graph.
 	 */
 	public void remove(E element) {
 		synchronized(nodes) {
 			final Node<E> node = nodes.remove(element);
 			if (node != null) {
 				node.breakConnections();
 			}
 		}
 	}
 
 	/**
 	 * Connects the given set of elements to a given parent. Note that nodes will be created for all new
 	 * elements if they do not exist yet.
 	 * 
 	 * @param element
 	 *            The element that is to be connected with new children.
 	 * @param newChildren
 	 *            The set of elements to connect to the given parent.
 	 */
 	public void addChildren(E element, Set<E> newChildren) {
 		synchronized(nodes) {
 			Node<E> node = nodes.get(element);
 			if (node == null) {
 				node = new Node<E>(element);
 				nodes.put(element, node);
 			}
 
 			for (E newChild : newChildren) {
 				Node<E> childNode = nodes.get(newChild);
 				if (childNode == null) {
 					childNode = new Node<E>(newChild);
 					nodes.put(newChild, childNode);
 				}
 				node.connectChild(childNode);
 			}
 		}
 	}
 
 	/**
 	 * Checks if the given element is a parent of the given potential child, directly or not.
 	 * 
 	 * @param parent
 	 *            Element that could be a parent of <code>potentialChild</code>.
 	 * @param potentialChild
 	 *            The potential child of <code>parent</code>.
 	 */
 	public boolean hasChild(E parent, E potentialChild) {
 		synchronized(nodes) {
 			final Node<E> node = nodes.get(potentialChild);
 			if (node != null) {
 				return Iterables.any(node.getAllParents(), is(parent));
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * This predicate will be used to check if a Node's element is the given one.
 	 * 
 	 * @return The constructed predicate.
 	 */
 	private Predicate<? super Node<E>> is(final E element) {
 		return new Predicate<Node<E>>() {
 			public boolean apply(Node<E> input) {
 				return input != null && input.getElement() == element;
 			}
 		};
 	}
 
 	/**
 	 * Returns an iterable over all elements of the subgraph containing the given element.
 	 * 
 	 * @param element
 	 *            Element we need the subgraph of.
 	 * @return An iterable over all elements of the subgraph containing the given element, an empty list if
 	 *         that element is not present in this graph.
 	 */
 	public Iterable<E> getSubgraphOf(E element) {
 		return getBoundedSubgraphOf(element, Collections.<E> emptySet());
 	}
 
 	/**
 	 * Returns an iterable over all elements of the subgraph containing the given element and ending at the
 	 * given boundaries.
 	 * 
 	 * @param element
 	 *            Element we need the subgraph of.
 	 * @param endPoints
 	 *            Boundaries of the need subgraph.
 	 * @return An iterable over all elements of the subgraph containing the given element, an empty list if
 	 *         that element is not present in this graph.
 	 */
 	public Iterable<E> getBoundedSubgraphOf(E element, Set<E> endPoints) {
 		synchronized(nodes) {
 			final Node<E> node = nodes.get(element);
 			if (node != null) {
 				Set<E> boundaries = endPoints;
 				if (boundaries == null) {
 					boundaries = Collections.emptySet();
 				}
 				return new SubgraphBuilder<E>(node, boundaries).build();
 			}
 			return Collections.emptyList();
 		}
 	}
 
 	/**
 	 * This will be used to represent individual elements in our graph.
 	 * 
 	 * @param <K>
 	 *            Type of the Node's underlying data.
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static class Node<K> {
 		/** Underlying data of this Node. */
 		private final K element;
 
 		/** Nodes that are connected with this one as a parent. */
 		private final Set<Node<K>> children;
 
 		/** Nodes that are connected with this one as a child. */
 		private final Set<Node<K>> parents;
 
 		/**
 		 * Construct a new Node for the given element.
 		 * 
 		 * @param element
 		 *            The element for which we need a graph Node.
 		 */
 		public Node(K element) {
 			this.element = element;
 			this.parents = new LinkedHashSet<Node<K>>();
 			this.children = new LinkedHashSet<Node<K>>();
 		}
 
 		/**
 		 * Returns all children nodes of this one.
 		 * 
 		 * @return All children nodes of this one.
 		 */
 		public Set<Node<K>> getChildren() {
 			return Collections.unmodifiableSet(children);
 		}
 
 		/**
 		 * Returns all parent nodes of this one.
 		 * 
 		 * @return All parent nodes of this one.
 		 */
 		public Set<Node<K>> getParents() {
 			return Collections.unmodifiableSet(parents);
 		}
 
 		/**
 		 * Returns all direct and indirect parents of this node.
 		 * 
 		 * @return All direct and indirect parents of this node.
 		 */
 		public Iterable<Node<K>> getAllParents() {
 			return new ParentsIterable<K>(this);
 		}
 
 		/**
 		 * Registers the given element as a child of this one.
 		 * 
 		 * @param other
 		 *            The Node that is to be connected to this one.
 		 */
 		public void connectChild(Node<K> child) {
 			this.children.add(child);
 			child.parents.add(this);
 		}
 
 		/**
 		 * Breaks all connections of this node.
 		 */
 		public void breakConnections() {
 			for (Node<K> parent : this.parents) {
 				parent.children.remove(this);
 			}
 			for (Node<K> child : this.children) {
 				child.parents.remove(this);
 			}
 			this.parents.clear();
 			this.children.clear();
 		}
 
 		/**
 		 * Returns the underlying data of this Node.
 		 * 
 		 * @return The underlying data of this Node.
 		 */
 		public K getElement() {
 			return element;
 		}
 	}
 
 	/**
 	 * This will be used to create a set over an entire subgraph given a starting point in said subgraph.
 	 * 
 	 * @param <L>
 	 *            Kind of elements the created set is meant to contain.
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static class SubgraphBuilder<L> {
 		/** The Node that will be used as a starting point of the iteration. */
 		private final Node<L> start;
 
 		/** Keeps track of the elements we've already iterated over. */
 		protected final Set<L> set;
 
 		/** Nodes that will be considered as boundaries for this subgraph. */
 		protected final Set<L> endPoints;
 
 		/**
 		 * Constructs a new iterable given the starting point the target subgraph and the elements that will
 		 * be considered as (excluded) boundaries of that subgraph.
 		 * 
 		 * @param start
 		 *            Starting point of the iteration.
 		 * @param endPoints
 		 *            Excluded boundaries of the target subgraph. Iteration will be pruned on these, along
 		 *            with their own subgraphs.
 		 */
 		public SubgraphBuilder(Node<L> start, Set<L> endPoints) {
 			this.start = start;
 			this.set = new LinkedHashSet<L>();
 			this.set.add(start.getElement());
 			this.endPoints = checkNotNull(endPoints);
 		}
 
 		/**
 		 * Constructs the set corresponding to the required subgraph.
 		 * 
 		 * @return The set of all nodes composing the required subgraph.
 		 */
 		public Set<L> build() {
 			return ImmutableSet.copyOf(new NodeIterator(start));
 		}
 
 		/**
 		 * A node iterator will allow us to iterate over the data of a Node and the data of all directly
 		 * connected Nodes; all the while avoiding data we've already iterated over.
 		 * <p>
 		 * This kind of iterator does not support {@link #remove() removal}.
 		 * </p>
 		 * <p>
 		 * Note : this is not a static class as we're building the external SubgraphBuilder's set through this
 		 * (and checking for already iterated over elements).
 		 * </p>
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 		 */
 		private class NodeIterator implements Iterator<L> {
 			/** Iterator over the all nodes directly connected with the underlying Node. */
 			private final Iterator<Node<L>> nodesIterator;
 
 			/**
 			 * Keeps track of the next Node's iterator. This will allow recursion over the connected Nodes'
 			 * own connections.
 			 */
 			private Iterator<L> nextNodeIterator;
 
 			/** Element that should be returned by the next call to {@link #next()}. */
 			private L next;
 
 			/**
 			 * Construct an iterator over the given Node's connected data.
 			 * 
 			 * @param node
 			 *            The node for which we need an iterator.
 			 */
 			public NodeIterator(Node<L> node) {
 				this.next = node.getElement();
 				this.nodesIterator = concat(node.getParents().iterator(), node.getChildren().iterator());
 				prepareNextIterator();
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.util.Iterator#hasNext()
 			 */
 			public boolean hasNext() {
 				return next != null || nextNodeIterator.hasNext() || nodesIterator.hasNext();
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.util.Iterator#next()
 			 */
 			public L next() {
 				if (next == null) {
 					throw new NoSuchElementException();
 				}
 				L result = next;
 				prepareNext();
 				return result;
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.util.Iterator#remove()
 			 */
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 
 			/**
 			 * This will be called to prepare for a subsequent call to {@link #next()}.
 			 */
 			private void prepareNext() {
 				if (!nextNodeIterator.hasNext()) {
 					prepareNextIterator();
 				}
 				if (nextNodeIterator.hasNext()) {
 					next = nextNodeIterator.next();
 				} else {
 					next = null;
 				}
 			}
 
 			/**
 			 * This will be called whenever we've consumed all nodes connected to the current to "jump over"
 			 * to the next one.
 			 */
 			private void prepareNextIterator() {
 				if (nodesIterator.hasNext()) {
 					Node<L> nextNode = nodesIterator.next();
 					while (SubgraphBuilder.this.set.contains(nextNode.getElement())
 							&& !SubgraphBuilder.this.endPoints.contains(nextNode.getElement())
 							&& nodesIterator.hasNext()) {
 						nextNode = nodesIterator.next();
 					}
 					if (!SubgraphBuilder.this.endPoints.contains(nextNode.getElement())
 							&& SubgraphBuilder.this.set.add(nextNode.getElement())) {
 						nextNodeIterator = new NodeIterator(nextNode);
 					} else {
 						nextNodeIterator = Iterators.emptyIterator();
 					}
 				} else {
 					nextNodeIterator = Iterators.emptyIterator();
 				}
 			}
 		}
 	}
 
 	/**
 	 * A custom Iterable that will iterate over the Node->parent Node tree of a given Node.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static class ParentsIterable<M> implements Iterable<Node<M>> {
 		/** The leaf of the Node->parent tree for which this iterable has been constructed. */
 		private final Node<M> start;
 
 		/**
 		 * Constructs an iterable given the root of its tree
 		 * 
 		 * @param start
 		 */
 		public ParentsIterable(Node<M> start) {
 			this.start = start;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.lang.Iterable#iterator()
 		 */
 		public Iterator<Node<M>> iterator() {
 			return new ParentsIterator<M>(start);
 		}
 	}
 
 	/**
 	 * A custom TreeIterator that will iterate over the Node->parent Node tree.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static class ParentsIterator<N> extends AbstractTreeIterator<Node<N>> {
 		/** Generated SUID. */
 		private static final long serialVersionUID = -4476850344598138970L;
 
 		/**
 		 * Construct an iterator given the root (well, "leaf") of its tree.
 		 * 
 		 * @param start
 		 *            Start node of the tree we'll iterate over.
 		 */
 		public ParentsIterator(Node<N> start) {
			super(start, false);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractTreeIterator#getChildren(java.lang.Object)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		protected Iterator<? extends Node<N>> getChildren(Object obj) {
 			return ((Node<N>)obj).getParents().iterator();
 		}
 	}
 }
