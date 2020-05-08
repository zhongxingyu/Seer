 import java.util.AbstractQueue;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
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
 		insert(x);
 		return true;
 	}
 	public E peek() {
 		HeapEntry<E> x = extract(heap[0]);
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
 	
 	/** Adds the specified item to this heap.
 		@param x The item to be added to this heap
 		@return The HeapEntry object of the inserted item
 	*/
 	public HeapEntry<E> insert(E x) {
 		if(x == null){
 			throw new NullPointerException();
 		}
 		if(cmp == null && !(x instanceof Comparable)){
 			throw new ClassCastException();
 		}
 		if (heap.length == size()) {
 			increaseCapacity();
 		}
 		HeapEntry<E> el = new HeapEntry<E>(x, size());
 		heap[size++] = el;
 		return el;
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
 			percolateUp(e.pos);
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
 			percolateDown(e.pos);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	/** Deletes the specified HeapEntry object from this heap. */
 	public void delete(HeapEntry<E> e) {
 		e.obj = getAt(--size).obj;
 		heap[size] = null;
 		HeapEntry<E> parent = getParent(e);
 		if (parent == null || compareTo(e.obj, parent.obj) > 0) {
 			percolateDown(e.pos);
 		} else {
 			percolateUp(e.pos);
 		}
 	}
 	
 	/** Internal auxiliary method to percolate item up the heap.
 		@param index the index at which the percolate starts
 	*/
 	private void percolateUp(int index){
 		if(index > (size()-1)){
 			return; //index out of bounds, silent
 		}
 		percolateUp(getAt(index));
 	}
 	
 	private void percolateUp(HeapEntry<E> node){
 		if(node == null){
 			return; //reached top
 		}
 		HeapEntry<E> parent = getParent(node);
		if(parent == null || compareTo(parent.obj,node.obj) >= 0){
 			return;
 		}
 		switchPos(parent,node);
 		percolateUp(parent);
 	}
 	
 	private HeapEntry<E> getParent(HeapEntry<E> node){
 		return getAt((node.pos - 1)/2);
 	}
 	
 	/** Internal auxiliary method to percolate item down the heap.
 		@param index the index at which the percolate starts.
 	*/
 	private void percolateDown(int index){
 		if(index > (size()-1)){
 			return; //index out of bounds, silent
 		}
 		HeapEntry<E> root = getAt(index);
 		percolateDown(root);
 	}
 	//recursive method
 	private void percolateDown(HeapEntry<E> root){
 		if(root == null){
 			return;
 		}
 		HeapEntry<E> minChild = findMinChild(root);
 		if(minChild == null){
 			return;
 		}
 		switchPos(root,minChild);
 		percolateDown(minChild);
 	}
 	private HeapEntry<E> findMinChild(HeapEntry<E> root){
 		HeapEntry<E> left = getAt(2*root.pos+1),
 				right = getAt(2*root.pos+2);
 		if(right == null){
 			return left;
 		} // if right exists then left also always exists.
 		
 		int cRight = compareTo(root.obj, right.obj),
 				cLeft = compareTo(root.obj, left.obj);
 		if(cRight <= cLeft){
 			return right;
 		}else{
 			return left;
 		}
 	}
 	private void increaseCapacity() {
 		Object[] temp = new Object[size()*2];
 		for (int i = 0; i < heap.length; i++) {
 			temp[i] = heap[i];
 		}
 		heap = temp;
 	}
 	private void switchPos(HeapEntry<E> e1, HeapEntry<E> e2){
 		E temp = e1.obj;
 		e1.obj = e2.obj;
 		e2.obj = temp;
 	}
 
 	public static class HeapEntry<E>{
 		private int pos;
 		private E obj;
 		private HeapEntry(E obj, int pos){
 			this.obj = obj;
 			this.pos = pos;
 		}
 		public E getObj(){
 			return obj;
 		}
 		public int getPos(){
 			return pos;
 		}
 	}
 	public Iterator<E> iterator() {
 		return new MinHeapIterator();
 	}
 	private class MinHeapIterator implements Iterator<E>{
 		private int pos;
 		private boolean removed = false;
 		public MinHeapIterator(){
 			pos = 0;
 		}
 		@Override
 		public boolean hasNext() {
 			return size() > pos;
 		}
 
 		@Override
 		public E next() {
 			if(!hasNext()){
 				throw new NoSuchElementException();
 			}
 			return getAt(pos++).obj;
 		}
 
 		@Override
 		public void remove() {
 			delete(getAt(pos));
 		}
 	}
 	
 	private HeapEntry<E> getAt(int index) {
 		if(index > (size() -1)){
 			return null;
 		}
 		return extract(heap[index]);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private int compareTo(E e, E other){
 		if(cmp == null){
 			Comparable<E> ce = (Comparable<E>)e;
 			return ce.compareTo(other);
 		} else {
 			return cmp.compare(e, other);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private HeapEntry<E> extract(Object o) {
 		if(o instanceof HeapEntry<?>){
 			return ((HeapEntry<E>)o);
 		}else{
 			throw new RuntimeException();
 		}
 	}
 }
