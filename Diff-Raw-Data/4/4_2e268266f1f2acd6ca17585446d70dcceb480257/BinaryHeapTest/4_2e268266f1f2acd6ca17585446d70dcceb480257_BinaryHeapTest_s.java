 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 /**
  * @author Jared Moore
  * @version Feb 27, 2013
  */
 public class BinaryHeapTest {
 
 	@Test
 	public void testSimpleAdd() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(
 				"[1, 2, 3, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData())); // it is easy enough to write
 													// a getter for data
 	}
 
 	@Test
 	public void testSimpleBubbleUp() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		heap.add(2);
 		heap.add(3);
 		heap.add(1);
 		assertEquals(
 				"[1, 3, 2, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData()));
 	}
 
 	@Test
 	public void testSimpleRemove() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(1, (int) heap.remove());
 		assertEquals(
 				"[2, 3, null, null, null, null, null, null, null, null, null]",
 				Arrays.toString(heap.getData()));
 	}
 
 	@Test
 	public void testSimplePeek() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		assertEquals(1, (int) heap.peek());
 	}
 
 	@Test
 	public void funWithNulls() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		heap.add(null);
 		assertEquals(4, heap.size());
 		heap.remove();
 		heap.remove();
 		heap.remove();
 		heap.remove();
 		assertTrue(heap.isEmpty());
 	}
 
 	@Test
 	public void testLargerAdd() {
 		BinaryHeap<Integer> heap = makeBiggerHeap();
 		Integer array[] = { 1, 2, 6, 5, 3, 10, 7, 11, 8, 9, 4 };
 		assertArrayEquals(array, heap.getData());
 		assertFalse(heap.isEmpty());
 		assertEquals(11, heap.size());
 	}
 
 	@Test
 	public void testStepByStepEmpty() {
 		BinaryHeap<Integer> heap = makeBiggerHeap();
 		Integer array[] = { 1, 2, 6, 5, 3, 10, 7, 11, 8, 9, 4 };
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 2;
 		array[1] = 3;
 		array[4] = 4;
 		array[10] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 3;
 		array[1] = 4;
 		array[4] = 9;
 		array[9] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 4;
 		array[1] = 5;
 		array[3] = 8;
 		array[8] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 5;
 		array[1] = 8;
 		array[3] = 11;
 		array[7] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 6;
 		array[2] = 7;
 		array[6] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 7;
 		array[2] = 10;
 		array[5] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 8;
 		array[1] = 9;
 		array[4] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 9;
 		array[1] = 11;
 		array[3] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 10;
 		array[2] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = 11;
 		array[1] = null;
 		assertArrayEquals(array, heap.getData());
 
 		heap.remove();
 		array[0] = null;
 		assertArrayEquals(array, heap.getData());
 		assertTrue(heap.isEmpty());
 	}
 
 	@Test
 	public void testNullsEdge1() {
 		BinaryHeap<Integer> heap = makeSimpleHeap();
 		heap.add(null);
 		heap.add(null);
 		heap.add(5);
 		heap.add(6);
 		Integer array[] = new Integer[11];
 		array[0] = 1;
 		array[1] = 2;
 		array[2] = 3;
 		array[5] = 5;
 		array[6] = 6;
 		assertArrayEquals(array, heap.getData());
 		heap.remove();
 		array[0] = 2;
		array[1] = 3;
		array[2] = 5;
		array[5] = 6;
 		array[6] = null;
 		assertArrayEquals(array, heap.getData());
 	}
 
 	@Test
 	public void testRemoveEmpty() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		try {
 			heap.remove();
 		} catch (Exception e) {
 			// don't do anything, but also don't stop due to an error that may
 			// or may not be thrown
 		} finally {
 			assertEquals(0, heap.size());
 		}
 	}
 
 	/**
 	 * Makes a bigger heap (added in large to small order)
 	 * 
 	 * @return the heap
 	 */
 	private BinaryHeap<Integer> makeBiggerHeap() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		for (int i = 11; i > 0; i--)
 			heap.add(i);
 		return heap;
 	}
 
 	/**
 	 * Makes a simple heap
 	 * 
 	 * @return the heap
 	 */
 	private BinaryHeap<Integer> makeSimpleHeap() {
 		BinaryHeap<Integer> heap = new BinaryHeap<Integer>();
 		heap.add(1);
 		heap.add(2);
 		heap.add(3);
 		return heap;
 	}
 
 }
