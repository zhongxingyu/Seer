 import java.util.AbstractQueue;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Queue;
 
 public class MinHeap<E> extends AbstractQueue<E> implements Queue<E> {
 	public static final int INITIAL_CAPACITY = 20;
 	private Object[] heap;
 	private int size;
 	private Comparator<E> cmp;
 	
 	public MinHeap() {
 		heap = new Object[INITIAL_CAPACITY];
 		size = 0;
 		cmp = null;
 	}
 	public MinHeap(Comparator<E> cmp) {
 		this();
 		this.cmp = cmp;
 	}
 	public int size() {
 		return size;
 	}
 	public boolean isEmpty() {
 		return size == 0;
 	}
 	public boolean offer(E x) {
 		HeapEntry<E> he = new HeapEntry<E>(x,20);
 		return true;
 	}
 	public E peek() {
 		HeapEntry<E> x = (HeapEntry<E>)heap[0];
 		return x.obj;
 	}
 	public E poll() {
 		if(isEmpty()){
 			return null;
 		}
 		HeapEntry<E> top = getAt(0),
 					last = getAt(size() - 1);
 
 		top.obj = last.obj;
 		delete(last);
 		percolateDown(0);
 		return top.obj;
 	}
 	public Iterator<E> iterator() {
 		
 		return null;
 	}
 	/** Adds the specified item to this heap.
 		@param x The item to be added to this heap
 		@return The HeapEntry object of the inserted item
 	*/
 	public HeapEntry<E> insert(E x) {
 		if (heap.length == size()) {
 			increaseCapacity();
 		}
		heap[size()] = new HeapEntry<E>(x, size());
		return null;
 	}
 	
 	/** Changes the value of the specified HeapEntry object to
 		newValue if the new value is less than the old value.
 		@param e The HeapEntry whose value is to be changed
 		@param newValue The new value of the specified HeapEntry object
 		@throws IllegalArgumentException if the new value
 				is greater than the old value
 	*/
 	public void decreaseKey(HeapEntry<E> e, E newValue) {
 		if (compareTo(newValue, e.obj) < 0) {
 			e.obj = newValue;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	/** Changes the value of the specified HeapEntry object to
 		newValue if the new value is greater than the old value.
 		@param e The HeapEntry whose value is to be changed
 		@param newValue The new value of the specified HeapEntry object
 		@throws IllegalArgumentException if the new value
 				is less than the old value
 	 */
 	public void increaseKey(HeapEntry<E> e, E newValue) {
 		if (compareTo(newValue, e.obj) > 0) {
 			e.obj = newValue;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	/** Deletes the specified HeapEntry object from this heap. */
 	public void delete(HeapEntry<E> e) {
 		heap[e.pos] = null;
 	}
 	/** Internal auxiliary method to percolate item up the heap.
 		@param index the index at which the percolate starts
 	*/
 	private void percolateUp(int index){
 		if(index > (size()-1)){
 			return; //index out of bounds, silent
 		}
 	}
 	/** Internal auxiliary method to percolate item down the heap.
 		@param index the index at which the percolate starts.
 	*/
 	private void percolateDown(int index){
 		if(index > (size()-1)){
 			return; //index out of bounds, silent
 		}
 	}
 	
 	private void increaseCapacity() {
 		Object[] temp = new Object[size()*2];
 		for (int i = 0; i < heap.length; i++) {
 			temp[i] = heap[i];
 		}
 		heap = temp;
 	}
 	
 	private HeapEntry<E> getAt(int i) {
 		return (HeapEntry<E>)heap[i];
 	}
 	public static class HeapEntry<E>{
 		int pos;
 		E obj;
 		private HeapEntry(E obj, int pos){
 			this.obj = obj;
 			this.pos = pos;
 		}
 	}
 	private int compareTo(E e, E other){
 		if(cmp == null){
 			Comparable ce = (Comparable)e;
 			return ce.compareTo(other);
 		} else {
 			return cmp.compare(e, other);
 		}
 	}
 }
