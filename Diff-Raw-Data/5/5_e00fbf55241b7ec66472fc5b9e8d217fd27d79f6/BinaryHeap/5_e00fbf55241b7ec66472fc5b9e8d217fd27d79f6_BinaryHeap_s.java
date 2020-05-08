 package com.max.algs.ds.heap;
 
 
 import java.util.AbstractQueue;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 
 import com.max.algs.util.ArrayUtils;
 
 /**
  *
  * Max/min binary heap.
  * Allow duplicate values.
  * Each time heap capacity exceeded, heap doubles.
  * Heap don't shrink.
  */
 public class BinaryHeap<T extends Comparable<T>> extends AbstractQueue<T> implements Queue<T>, java.io.Serializable {
	
	private static final long serialVersionUID = -3833676864928496756L;
 
 	private static final int DEFAULT_CAPACITY = 8;
 
     private final BinaryHeapType type;
 
     private T[] arr;
     private int size;
     
     private transient int modCount = 0;
 
 
     /**
      * Retrieves and removes the head of this queue,
      * or returns <tt>null</tt> if this queue is empty.
      */
 	@Override
 	public T poll() {
 		
 		if( isEmpty() ){
 			return null;
 		}
 		
 		if( size == 0 ){
             throw new IllegalStateException("Can't extract min value from empty Heap");
         }
 
         T retValue = arr[0]; 
 
         arr[0] = arr[size-1];
         arr[size-1] = null;
         --size;
         ++modCount;
 
        
         fixDown( 0 );
 
         return retValue;
 	}
 
 	 /**
      * Retrieves, but does not remove, the head of this queue,
      * or returns <tt>null</tt> if this queue is empty.
      */
 	@Override
 	public T peek() {		
 		if( isEmpty() ){
 			return null;
 		}
 		
 		return arr[0];
 	}	
 
 
 	@Override
 	public Iterator<T> iterator() {		
 		return new BinaryHeapIterator(modCount);
 	}
 
 	@Override
 	public boolean offer(T value) {
 		if( size == arr.length ){
             resize();
         }
 
         arr[size] = value;
         fixUp(size);
         ++size;
         ++modCount;
 
         return true;
 	}
     
 
 
     private BinaryHeap(BinaryHeapType type, int initialCapacity){
         this.type = type;
         this.arr = createArray(initialCapacity);
     }
     
     private BinaryHeap(BinaryHeapType type){
         this.type = type;
         this.arr = createArray( DEFAULT_CAPACITY );
     }
 
     public static <U extends Comparable<U>> BinaryHeap<U> maxHeap(){
         return new BinaryHeap<>(BinaryHeapType.MAX);
     }
 
     public static <U extends Comparable<U>> BinaryHeap<U> minHeap(){
         return new BinaryHeap<>(BinaryHeapType.MIN);
     }
     
     public static <U extends Comparable<U>> BinaryHeap<U> minHeap( int initialCapacity ){
     	return new BinaryHeap<>(BinaryHeapType.MIN, initialCapacity);
     }
 
     public BinaryHeap<T> inverseHeap(){
 
         BinaryHeap<T> newHeap = new BinaryHeap<>( type == BinaryHeapType.MAX ? BinaryHeapType.MIN : BinaryHeapType.MAX );
         newHeap.size = size;
         newHeap.arr = Arrays.copyOf( arr, arr.length );
 
         int firstChildIndex = newHeap.size/2;
 
         // min heap => max heap
         if( type == BinaryHeapType.MIN ){
             Arrays.sort(newHeap.arr, firstChildIndex, size);
 
         }
         // max heap => min heap
         else {
             Arrays.sort(newHeap.arr, firstChildIndex, size, new Comparator<T>() {
                 @Override
                 public int compare(T obj1, T obj2) {
                     return -obj1.compareTo(obj2);
                 }
             });
         }
 
         int parentIndex = 0;
         int childIndex = size-1;
 
         while( parentIndex < firstChildIndex ){
             ArrayUtils.swap(newHeap.arr, parentIndex, childIndex);
             parentIndex++;
             childIndex--;
         }
 
 
         return newHeap;
     }
 
 
     @Override
     public int size(){
         return size;
     }
     
     @Override
     public boolean isEmpty(){
     	return size == 0;
     }
     
     public Integer[] toArray(){
     	Integer[] arrCopy = new Integer[ size ];
     	
     	for( int i =0; i < size; i++ ){
     		arrCopy[i] = (Integer)arr[i];
     	}
     	
     	return arrCopy;
     }
     
     
     
     @Override
     public String toString(){
     	
     	if( isEmpty() ){
     		return "{}";
     	}
     	
     	StringBuilder buf = new StringBuilder( 2 * size );
     	
     	buf.append("{").append( arr[0] );
     	
     	for( int i = 1; i < size; i++ ){
     		buf.append(",").append( arr[i] );
     	}
     	
     	buf.append("}");
     	return buf.toString();
     }
     
     /**
     *
     * Double array size.
     *
     */
    @SuppressWarnings("unchecked")
 	private void resize() {
        T[] tempArrRef = arr;
        arr = (T[])new Comparable[2 * arr.length];
        System.arraycopy( tempArrRef, 0, arr, 0, tempArrRef.length );
    }
    
     
     private int leftChildIndex(int index){
     	return (index << 1) | 1;
     }
     
     private int rightChildIndex(int index){
     	return (index << 1) + 2;
     }
     
     
     private int parentIndex(int index){    
     	
 
     	// 'even' index
     	if( (index & 1) == 0 ){
     		return (index >>> 1) - 1;
     	}
     	
     	//'odd' index
     	return index >>> 1;
     }
 
 
     protected Comparable<T>[] getInternalArray(){
         return arr;
     }
 
 
     private void fixUp( int index ) {
 
         int curIndex = index;
         int parent;
         T temp = null;
 
         while( curIndex > 0 ) {
 
             parent = parentIndex(curIndex);
 
             if( type == BinaryHeapType.MAX && arr[parent].compareTo( arr[curIndex]) >= 0 ){
                 break;
             }
 
             if( type == BinaryHeapType.MIN && arr[parent].compareTo( arr[curIndex]) <= 0 ){
                 break;
             }
 
             temp = arr[parent];
             arr[parent] = arr[curIndex];
             arr[curIndex] = temp;
 
             curIndex = parent;
         }
     }
     
     private void fixDown( int index ) {    	
     	
     	int minIndex = index;   
     	
     	while( true ){
     		    		
     		int curIndex = minIndex; 
     		
     		int left = leftChildIndex(curIndex); 
     		if( left < size && ( (type == BinaryHeapType.MIN && arr[left].compareTo(arr[minIndex] ) < 0) || (type == BinaryHeapType.MAX && arr[left].compareTo(arr[minIndex] ) > 0) ) ){
     			minIndex = left;    			
     		}   
     		
     		int right = rightChildIndex(curIndex);
     		if( right < size && ( (type == BinaryHeapType.MIN && arr[right].compareTo(arr[minIndex] ) < 0) || (type == BinaryHeapType.MAX && arr[left].compareTo(arr[minIndex] ) > 0)) ){
     			minIndex = right;    			
     		}
     		
     		if( minIndex == curIndex ){
     			break;
     		}
     		
    			ArrayUtils.swap( arr, minIndex, curIndex);    		
     	}   	
     }
     
     
     @SuppressWarnings("unchecked")
     private T[] createArray( int length ){
     	return (T[])new Comparable[length];
     }
     
     
     private void removeAtIndex( int index ){
     	assert index >= 0 && index < size : "incorrect 'index' passed";
     	
     	arr[index] = arr[size-1];
     	arr[size-1] = null;
     	--size;
     	fixUp(index);
     	fixDown(index);    	
     }
     
     
 	private final class BinaryHeapIterator implements Iterator<T> {
 
 		int modCountSnapshot; 
 		int pos;
 		int lastPos;		
 		
 		public BinaryHeapIterator(int modCountSnapshot) {
 			super();
 			this.modCountSnapshot = modCountSnapshot;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return pos < BinaryHeap.this.size;
 		}
 
 		@Override
 		public T next() {
 			
 			if( BinaryHeap.this.modCount != modCountSnapshot ){
 				throw new ConcurrentModificationException("Binary heap was modified from another iterator or thread");
 			}
 			
 			if( !hasNext() ){
 				throw new NoSuchElementException();
 			}
 			
 			lastPos = pos;			
 			return BinaryHeap.this.arr[pos++];			
 		}
 
 		@Override
 		public void remove() {
 			
 			if( lastPos < 0 ){
 				throw new IllegalStateException("Call 'next' on iterator first");
 			}
 			
 			BinaryHeap.this.removeAtIndex( lastPos );
 			
 			lastPos = -1;
 			++BinaryHeap.this.modCount;
 			++modCountSnapshot;	
 		}
 		
 	}
 
 
 
 }
