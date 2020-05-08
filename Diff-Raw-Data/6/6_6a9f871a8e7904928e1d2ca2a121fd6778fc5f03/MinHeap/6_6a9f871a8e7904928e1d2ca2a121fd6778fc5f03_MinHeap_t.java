 package com.codemelon.util;
 
 import java.util.AbstractQueue;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * MinHeap that can be used as priority queue for Dijkstra's algorithm.
  * <p>
  * Java's PriorityQueue doesn't support the necessary decrease-key()
  * operation, so we need a custom implementation. This one closely
  * follows CLRS, pp. 151ff.
  * <p>
 * Internally, the heap allows for fast access to its elements by means of
 * a hash mapping an item to the set of indices where it is located in
 * the array that backs the heap. This allows maximal performance (short
 * of a Fibonacci heap) when items other than the head need to be modified.
  * 
  * @author Marshall Farrier
  * @since Sep 23, 2013
  */
 public class MinHeap<T> extends AbstractQueue<T> {
 	private ArrayList<T> heap;
 	// for fast retrieval of item index
 	private Map<T, Set<Integer>> map;
 	private Comparator<? super T> c;
 	// following Java implementation of PriorityQueue<T>
 	private static final int DEFAULT_INITIAL_CAPACITY = 11;
 	
 	/**
 	 * Creates an empty <code>MinHeap</code> with default initial capacity (11)
 	 */
 	public MinHeap() {
 		this(DEFAULT_INITIAL_CAPACITY);
 	}
 	
 	/**
 	 * Construct an empty <code>MinHeap</code> using the natural ordering for type T
 	 * and specified initial capacity
 	 */
 	public MinHeap(int initialCapacity) {
 		this(initialCapacity, new Comparator<T>() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public int compare(T a, T b) {
 				Comparable<? super T> key = (Comparable<? super T>) a;
 				return key.compareTo(b);
 			}
 		});
 	}
 	
 	/**
 	 * Construct an empty <code>MinHeap</code> specifying the method for comparing
 	 * objects of type T
 	 */
 	public MinHeap(Comparator<? super T> comparator) {
 		this.heap = new ArrayList<T>(DEFAULT_INITIAL_CAPACITY);
 		c = comparator;
 		initMap(DEFAULT_INITIAL_CAPACITY);
 	}
 	
 	/**
 	 * Construct an empty <code>MinHeap</code> specifying the method for comparing
 	 * objects of type T and the heap's initial capacity
 	 */
 	public MinHeap(int initialCapacity, Comparator<? super T> comparator) {
 		this.heap = new ArrayList<T>(initialCapacity);
 		c = comparator;
 		initMap(initialCapacity);
 	}
 	
 	/**
 	 * Creates a <code>MinHeap</code> containing the elements in the specified collection, 
 	 * compared using their natural ordering.
 	 * 
 	 * @param items
 	 * @param comparator
 	 * @throws IllegalArgumentException if the collection contains duplicates.
 	 */
 	public MinHeap(Collection<? extends T> items) {
 		this(items, new Comparator<T>() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public int compare(T a, T b) {
 				Comparable<? super T> key = (Comparable<? super T>) a;
 				return key.compareTo(b);
 			}
 		});
 	}
 	
 	/**
 	 * Creates a <code>MinHeap</code> containing the elements in the specified collection, 
 	 * compared using the given comparator.
 	 * 
 	 * @param items
 	 * @param comparator
 	 * @throws IllegalArgumentException if the collection contains duplicates.
 	 */
 	public MinHeap(Collection<? extends T> items, Comparator<? super T> comparator) {
 		this.heap = new ArrayList<T>(items);
 		c = comparator;
 		initMap();
 		buildMinHeap();
 	}
 	
 	/**
 	 * Inserts the specified item into this heap.
 	 * <p>
 	 * Note that if the item is already present, it is not inserted a second time.
 	 * In this case, the method returns false and does not modify the heap.
 	 * 
 	 * @return true if the item is not already in the heap
 	 */
 	@Override
 	public boolean add(T item) {
 		return offer(item);
 	}
 	
 	/**
 	 * Returns the comparator used to order the elements in the heap.
 	 * <p>
 	 * If no comparator was passed when the heap was constructed, the comparator
 	 * returned will enforce the natural ordering for objects of type T.
 	 * 
 	 * @return the comparator used to order the elements in the heap.
 	 */
 	public Comparator<? super T> comparator() {
 		return c;
 	}
 	
 	/**
 	 * Removes all of the elements from this heap. The heap will be empty after this call returns.
 	 * <p>
 	 * The comparator for ordering elements of the heap remains unchanged.
 	 */
 	@Override
 	public void clear() {
 		heap = new ArrayList<T>(DEFAULT_INITIAL_CAPACITY);
 		initMap(DEFAULT_INITIAL_CAPACITY);
 	}
 	
 	/**
 	 * Returns true if this heap contains the specified element.
 	 * 
 	 * @return true if this heap contains the specified element
 	 */
 	@Override
 	public boolean contains(Object o) {
 		return map.containsKey(o);
 	}
 
 	/**
 	 * Inserts the specified item into this heap.
 	 * 
 	 * @return true (as specified by <code>Queue.offer(T))</code>
 	 */
 	@Override
 	public boolean offer(T item) {
 		heap.add(item);
 		addMapping(item, heap.size() - 1);
 		decreaseKeyByIndex(heap.size() - 1);
 		return true;
 	}
 
 	/**
 	 * Retrieves, but does not remove, the head of this heap, or returns <code>null</code>
 	 * if this queue is empty
 	 * 
 	 * @return the head of this heap
 	 */
 	@Override
 	public T peek() {
 		if (heap.size() == 0) { return null; }
 		return heap.get(0);
 	}
 
 	/**
 	 * Retrieves and removes the head of this heap, or returns <code>null</code> if this
 	 * heap is empty.
 	 * 
 	 * @return the head of this heap, or <code>null</code> if this heap is empty
 	 */
 	@Override
 	public T poll() {
 		if (heap.size() < 1) {
 			// following poll() method of Java PriorityQueue
 			// rather than exception used in CLRS, p. 163
 			return null;
 		}
 		int lastIndex = heap.size() - 1;
 		T min = heap.get(0);
 		removeMapping(min, 0);
 		heap.set(0, heap.get(lastIndex));
 		addMapping(heap.get(0), 0);
 		removeMapping(heap.get(lastIndex), lastIndex);
 		heap.remove(lastIndex);
 		minHeapify(0);
 		return min;
 	}
 
 	/**
 	 * Returns an iterator over the elements in this heap. The iterator does not return the
 	 * elements in any particular order.
 	 * 
 	 * @returns an iterator over the elements in this heap
 	 */
 	@Override
 	public Iterator<T> iterator() {
 		return heap.listIterator();
 	}
 
 	/**
 	 * Returns the number of elements in this collection.
 	 * 
 	 * @returns the number of elements in this collection
 	 */
 	@Override
 	public int size() {
 		return heap.size();
 	}
 	
 	/**
 	 * Return an index in the heap for the given item or -1 if the item is not found.
 	 * <p>
 	 * If the item is found in the heap multiple times, the returned index
 	 * may point to any such occurrence.
 	 * <p>
 	 * This operation requires only O(1) time.
 	 * <p>
 	 * The indices used in the get() and find() methods are relevant
 	 * only for the decreaseKey() operation.
 	 * 
 	 * @param item item whose index is to be found
 	 * @return an index of the item or -1 if the item is not present in the heap
 	 */
 	public int find(T item) {
 		if (!map.containsKey(item)) {
 			return -1;
 		}
 		return map.get(item).iterator().next();
 	}
 	
 	/**
 	 * Return the item found at the given index in the heap.
 	 * <p>
 	 * Calling this function with the value returned from find()
 	 * is guaranteed to return the same item that was passed to find.
 	 * That is: heap.get(heap.find(item)).equals(item) is always true.
 	 * <p>
 	 * The indices used in the get() and find() methods are relevant
 	 * only for the decreaseKey() operation.
 	 * 
 	 * @param index index for the item to be retrieved
 	 * @return the item found at the specified index
 	 */
 	public T get(int index) {
 		return heap.get(index);
 	}
 	
 	/**
 	 * Resets a heap that may have been corrupted by external
 	 * modifications of its items.
 	 */
 	public void reset() {
 		initMap();
 		buildMinHeap();
 	}
 	
 	/**
 	 * Check the integrity of the heap. 
 	 * <p>
 	 * If the min-heap conditions are satisfied, and the mapping used for
 	 * fast retrieval of items is valid, return true. Returns false
 	 * if any of these conditions is violated.
 	 * 
 	 * @return true iff the heap meets all conditions of a min-heap
 	 * and its mappings are all valid.
 	 */
 	public boolean check() {
 		int len = heap.size();
 		// check that heap property holds
 		for (int i = (len / 2) - 1; i >= 0; i--) {
 			if (c.compare(heap.get(left(i)), heap.get(i)) < 0)
 				return false;
 			if (right(i) < len && c.compare(heap.get(right(i)), heap.get(i)) < 0)
 				return false;
 		}
 		// check that mapping is correct
 		// everything in the heap is included in the map
 		for (int i = 0; i < len; i++) {
 			if (!map.containsKey(heap.get(i)) || !map.get(heap.get(i)).contains(i)) 
 				return false;
 		}
 		// everything in the map is included in the heap
 		for (T item : map.keySet()) {
 			for (int i : map.get(item)) {
 				if (!item.equals(heap.get(i)))
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Replace the item at index i in the heap with a "smaller" item
 	 * according to the comparison in effect for this heap.
 	 * <p>
 	 * The item passed may be a modification of the item 
 	 * already present at the given index. 
 	 * 
 	 * @param i index at which some prior item is to be replaced 
 	 * by the specified item
 	 * @param item item to be inserted in the given location
 	 * @throws IllegalArgumentException if the item to be inserted
 	 * is larger than the item currently present at the given
 	 * index.
 	 */
 	public void decreaseKey(int i, T item) {
 		if (c.compare(item, heap.get(i)) > 0) {
 			throw new IllegalArgumentException("new key is larger than current key");
 		}
 		removeMapping(heap.get(i), i);
 		heap.set(i, item);
 		addMapping(item, i);
 		decreaseKeyByIndex(i);
 	}
 	
 	/**
 	 * Reorganize the heap so as to accommodate a decrease in key
 	 * of one instance of the specified item.
 	 * <p>
 	 * Finds an instance of the given item, decreases its key according
 	 * to the decreaseKey() method of the given ItemChanger and reorganizes
 	 * the heap accordingly.
 	 * <p>
 	 * Note that it the overriden decreaseKey in itemChanger <em>increases</em>
 	 * the key in violation of its contract, the heap may become corrupt.
 	 * In this case an IllegalArgumentException is thrown
 	 * 
 	 * @param item
 	 * @param itemChanger
 	 */
 	public void decreaseKey(T item, ItemChanger<T> itemChanger) {
 		int index = find(item),
 			len = heap.size();
 		// This has to happen before the item is modified, so
 		// we can't check whether the itemChanger is really decreasing the key
 		// before removing the item from the map
 		removeMapping(item, index);
 		itemChanger.decreaseKey(item);
 		addMapping(item, index);
 		// item must be <= its children
 		if (left(index) < len) {
 			if (c.compare(heap.get(left(index)), item) < 0 || (right(index) < len 
 					&& c.compare(heap.get(right(index)), item) < 0)) {
 				throw new IllegalArgumentException("heap was corrupted due to illegal key change");
 			}
 		}
 		decreaseKeyByIndex(index);
 	}
 	
 	private void buildMinHeap() {
 		for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
 			minHeapify(i);
 		}
 	}
 	
 	/**
 	 * Following CLRS, p. 154, assumes that the subtrees
 	 * to the left and right are already heapified and allows
 	 * the element at i to float down as necessary
 	 * 
 	 * @param i index at which to heapify
 	 */
 	private void minHeapify(int i) {
 		int l = left(i),
 			r = right(i),
 			smallest = i;
 		
 		if (l < heap.size() && c.compare(heap.get(l), heap.get(i)) < 0) {
 			smallest = l;
 		}
 		if (r < heap.size() && c.compare(heap.get(r), heap.get(smallest)) < 0) {
 			smallest = r;
 		}
 		if (smallest != i) {
 			swap(i, smallest);
 			minHeapify(smallest);
 		}
 	}
 	
 	/**
 	 * The item at index i has been changed so that its key is less than or equal
 	 * to the previous key of the item in this position.
 	 * @param i
 	 */
 	private void decreaseKeyByIndex(int i) {
 		while (i > 0 && c.compare(heap.get(i), heap.get(parent(i))) < 0) {
 			swap(i, parent(i));
 			i = parent(i);
 		}
 	}
 	
 	private void removeMapping(T item, Integer i) {
 		map.get(item).remove(i);
 		if (map.get(item).isEmpty()) {
 			map.remove(item);
 		}
 	}
 	
 	private void addMapping(T item, Integer i) {
 		if (!map.containsKey(item)) {
 			map.put(item, new HashSet<Integer>());
 		}
 		map.get(item).add(i);
 	}
 	
 	private void swap(int i, int j) {
 		map.get(heap.get(i)).remove(new Integer(i));
 		map.get(heap.get(j)).remove(new Integer(j));
 		T tmp = heap.get(i);
 		heap.set(i, heap.get(j));
 		heap.set(j, tmp);
 		map.get(heap.get(i)).add(i);
 		map.get(heap.get(j)).add(j);
 		
 	}
 	
 	private static int parent(int i) {
 		return (i - 1) / 2;
 	}
 	
 	private static int left(int i) {
 		return 2 * i + 1;
 	}
 	
 	private static int right(int i) {
 		return 2 * i + 2;
 	}
 	
 	private void initMap(int initialCapacity) {
 		map = new HashMap<T, Set<Integer>>(initialCapacity);
 	}
 	
 	private void initMap() {
 		map = new HashMap<T, Set<Integer>>(heap.size());
 		for (int i = 0; i < heap.size(); i++) {
 			if (map.containsKey(heap.get(i))) {
 				// map maintains as many copies as there are instances of the given item
 				map.get(heap.get(i)).add(i);
 			}
 			else {
 				map.put(heap.get(i), new HashSet<Integer>());
 				map.get(heap.get(i)).add(i);
 			}
 		}
 	}
 }
