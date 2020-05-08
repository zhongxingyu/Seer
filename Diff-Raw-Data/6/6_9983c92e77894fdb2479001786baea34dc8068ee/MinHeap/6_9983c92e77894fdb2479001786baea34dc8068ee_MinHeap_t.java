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
 		HeapEntry<E> el = getAt(0);
		getAt(0).obj = getAt(size()).obj;
		delete(getAt(size()));
 		percolateDown(getAt(0).pos);
 		return el.obj;
 	}
 	public Iterator<E> iterator() {
 		return null;
 	}
 	public HeapEntry<E> insert(E x) {
 		return null;
 	}
 	public void decreaseKey(HeapEntry<E> e, E newValue) {
 		
 	}
 	public void increaseKey(HeapEntry<E> e, E newValue) {
 		
 	}
 	public void delete(HeapEntry<E> e) {
		heap[e.pos] = null;
 	}
 	/* Internal auxiliary method to percolate item up the heap.
 	@param index the index at which the percolate starts
 	*/
 	private void percolateUp(int index){
 		
 	}
 	/* Internal auxiliary method to percolate item down the heap.
 	@param index the index at which the percolate starts.
 	*/
 	private void percolateDown(int index){
 		
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
 }
