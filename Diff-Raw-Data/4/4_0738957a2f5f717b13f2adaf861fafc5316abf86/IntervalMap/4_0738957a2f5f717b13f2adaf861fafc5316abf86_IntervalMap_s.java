 package org.sugis.intervalmap;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.NotThreadSafe;
 
 /**
  * An IntervalMap represents a map of Interval objects to values,
  * with the additional ability to query with a single parameter
  * and find the values of all intervals that contain it.
  * @author Steven Schlansker
  * @param <K> the type of the intervals' bounds
  * @param <V> the type of the values stored in the Map
  */
 @NotThreadSafe
 public class IntervalMap<K extends Comparable<K>, V> implements Map<Interval<K>, V> {
 
 	private class IntervalNode {
 		Interval<K> interval;
 		K maxChildValue;
 		int maxChildDepth, leftCount, rightCount;
 		V value;
 		IntervalNode left, right;
 	}
 
 	private abstract class Traversal<R, A> {
 		/** return null to continue traversal */
 		abstract R visit(IntervalNode node);
 	}
 
 	private IntervalNode root;
 
 	public IntervalMap<K, V> getContaining(@Nonnull K point) {
 		throw new AssertionError();
 	}
 	
 	public void clear() {
 		root = null;
 	}
 
 	public boolean containsKey(Object key) {
 		if (! (key instanceof Interval<?>) || root == null) return false;
 		return get(key) != null;
 	}
 
 	public boolean containsValue(final Object value) {
 		Boolean result = traverse(new Traversal<Boolean, Void>() {
 			@Override
 			Boolean visit(IntervalNode node) {
 				if (node.value.equals(value)) return true;
 				return null;
 			}
 		});
 		if (result == Boolean.TRUE) return true; /* necessary because result could be null */
 		return false;
 	}
 
 	public Set<Entry<Interval<K>, V>> entrySet() {
 		final Set<Entry<Interval<K>, V>> result = new HashSet<Entry<Interval<K>,V>>();
 		traverse(new Traversal<Void, Void>() {
 			@Override
 			Void visit(final IntervalNode node) {
 				result.add(new Entry<Interval<K>, V>() {
 					public Interval<K> getKey() {
 						return node.interval;
 					}
 					public V getValue() {
 						return node.value;
 					}
 					public V setValue(V value) {
 						return node.value = value;
 					}
 				});
 				return null;
 			}
 		});
 		return result;
 	}
 
 	public V get(Object key) {
 		IntervalNode node = findUnchecked(key, root, new LinkedList<IntervalNode>());
 		if (node == null) return null;
 		return node.value;
 	}
 
 	public boolean isEmpty() {
 		return root == null;
 	}
 
 	public Set<Interval<K>> keySet() {
 		final Set<Interval<K>> result = new HashSet<Interval<K>>();
 		traverse(new Traversal<Void, Void>() {
 			@Override
 			Void visit(final IntervalNode node) {
 				result.add(node.interval);
 				return null;
 			}
 		});
 		return result;
 	}
 
 	public V put(@Nonnull Interval<K> key, V value) {
 		IntervalNode newborn = new IntervalNode();
 		newborn.interval = key;
 		newborn.value = value;
 		if (root == null)
 			root = newborn;
 		else
 			return put(root, newborn);
 		return null;
 	}
 
 	private V put(@Nonnull IntervalNode top, @Nonnull IntervalNode newborn) {
 		if (top.interval.equals(newborn.interval)) {
 			V oldvalue = top.value;
 			top.value = newborn.value;
 			return oldvalue;
 		}
 		if (top.interval.getLowerBound().compareTo(newborn.interval.getLowerBound()) < 0) {
 			if (top.right == null) {
 				top.right = newborn;
 				top.rightCount++;
 				top.maxChildDepth = Math.max(top.maxChildDepth, 1);
 				return null;
 			} else {
 				V result = put(top.right, newborn);
 				if (result == null) // a new node was added
 					top.rightCount++;
 				return result;
 			}
 		} else {
 			if (top.left == null) {
 				top.left = newborn;
 				top.leftCount++;
 				top.maxChildDepth = Math.max(top.maxChildDepth, 1);
 				return null;
 			} else {
 				V result = put(top.left, newborn);
 				if (result == null) // a new node was added
 					top.leftCount++;
 				return result;
 			}
 		}
 	}
 
 	public void putAll(@Nonnull Map<? extends Interval<K>, ? extends V> m) {
 		for (Entry<? extends Interval<K>, ? extends V> e : m.entrySet()) {
 			put(e.getKey(), e.getValue());
 		}
 	}
 
 	public V remove(Object key) {
 		List<IntervalNode> trace = new LinkedList<IntervalNode>();
 		IntervalNode node = findUnchecked(key, root, trace);
 		throw new AssertionError();
 	}
 
 	@SuppressWarnings("unchecked")
 	private IntervalNode findUnchecked(Object key, IntervalNode current, List<IntervalNode> trace) {
 		if (! (key instanceof Interval<?>))
 			return null;
 		Interval<?> ikey = (Interval<?>) key;
 		Class<?> paramClass = ikey.getLowerBound().getClass();
 		Class<?> intervalClass = root.interval.getLowerBound().getClass();
 		if (! (intervalClass.isAssignableFrom(paramClass)))
 			return null;
 		try {
 			return find((Interval<K>) key, current, trace);
 		} catch (ClassCastException e) {
 			return null;
 		}
 	}
 
 	private IntervalNode find(Interval<K> key, IntervalNode current, List<IntervalNode> trace) {
 		if (current == null) return null;
 		if (current.interval.equals(key))
 			return current;
 		trace.add(current);
 		if (current.interval.getLowerBound().compareTo(key.getLowerBound()) < 0) 
 			return find(key, current.right, trace);
 		else
 			return find(key, current.left, trace);
 	}
 
 	public int size() {
 		if (root == null) return 0;
 		return root.leftCount + root.rightCount + 1;
 	}
 
 	public Collection<V> values() {
 		final Collection<V> result = new ArrayList<V>();
 		traverse(new Traversal<Void, Void>() {
 			@Override
 			Void visit(final IntervalNode node) {
 				result.add(node.value);
 				return null;
 			}
 		});
 		return result;
 	}
	
 	private <R> R traverse(@Nonnull Traversal<R, ?> t) {
 		Deque<IntervalNode> queue = new LinkedList<IntervalNode>();
 		queue.offerFirst(root);
 		while (!queue.isEmpty()) {
 			IntervalNode node = queue.pop();
 			if (node == null) continue;
 			R result = t.visit(node);
 			if (result != null) return result;
 			queue.offerFirst(node.left);
 			queue.offerFirst(node.right);
 		}
 		return null;
 	}

 }
