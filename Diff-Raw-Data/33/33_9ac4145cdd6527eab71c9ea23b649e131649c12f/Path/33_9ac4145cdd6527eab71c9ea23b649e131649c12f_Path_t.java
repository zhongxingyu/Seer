 /*
  * dfh.treepath -- a library for querying arbitrary trees
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.treepath;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A compiled tree path expression.
  * <p>
  * 
  * @author David F. Houghton - May 1, 2012
  * 
  * @param <N>
  */
 public class Path<N> implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private final Selector<N>[][] selectors;
 	private final Forester<N> f;
 
 	Path(Forester<N> f, Selector<N>[][] selectors) {
 		this.f = f;
 		this.selectors = selectors;
 	}
 
 	/**
 	 * Selects nodes in the tree that match the path. If it is not a relative
 	 * path and the nodes in the tree do not know their own parents, this match
 	 * will fail unless the context node given is the root node.
 	 * <p>
 	 * This method is like {@link #select(Object, Index)} -- in fact, it
 	 * delegates to this method after constructing an appropriate index. Use
 	 * this method if you are doing few matches on the tree. Each select in this
 	 * case will generate a fresh index.
 	 * 
 	 * @param root
 	 *            a node in the tree; if this is not the root node and the
 	 *            tree's nodes do not know their own parents -- see
 	 *            {@link ParentIndex} -- this will be the de-facto root node
 	 * @return the nodes matching the path in the order of their discovery
 	 */
 	public List<N> select(N root) {
 		return select(root, f.index(root));
 	}
 
 	/**
 	 * Selects nodes in the tree that match the path. If it is not a relative
 	 * path and the nodes in the tree do not know their own parents, this match
 	 * will fail unless the context node given is the root node.
 	 * <p>
 	 * This method is like {@link #select(Object)}, but uses a pre-constructed
 	 * index. Use this method if you are doing many matches on the same tree as
 	 * it prevents redundant object creation and tree walking.
 	 * 
	 * @param n
 	 *            a node in the tree; if this is not the root node and the
 	 *            tree's nodes do not know their own parents -- see
 	 *            {@link ParentIndex} -- this will be the de-facto root node
 	 * @param i
 	 *            an index of the tree
 	 * @return the nodes matching the path in the order of their discovery
 	 */
	public List<N> select(N n, Index<N> i) {
		if (n == null)
 			throw new PathException("select called on null node");
 		if (!i.indexed())
 			i.index();
		return new ArrayList<N>(sel(n, i));
 	}
 
 	Collection<N> sel(N n, Index<N> index) {
 		Set<N> selection = new LinkedHashSet<N>();
 		for (Selector<N>[] fork : selectors) {
 			selection.addAll(sel(n, index, fork, 0));
 		}
 		return selection;
 	}
 
 	Collection<N> sel(N n, Index<N> index, Selector<N>[] fork, int stepIndex) {
 		Collection<N> next = fork[stepIndex++].select(n, index);
 		if (stepIndex == fork.length)
 			return next;
 		Set<N> selection = new LinkedHashSet<N>();
 		for (N c : next) {
 			selection.addAll(sel(c, index, fork, stepIndex));
 		}
 		return selection;
 	}
 }
