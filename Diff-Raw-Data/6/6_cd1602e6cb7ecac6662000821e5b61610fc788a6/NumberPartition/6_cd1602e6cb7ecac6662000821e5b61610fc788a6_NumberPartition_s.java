 public class NumberPartition {
 	public NumberPartition() {
 
 	}
 
   String[] test_args = {"hellozz"};
 	public static void main(String[] args) {
 		System.out.println("Hello World");
 	}
 
   public static long KarmarkarKarp(Long[] arr) {
     MaxHeap<Long> heap = new MaxHeap<Long>(arr, arr.length, arr.length);
     if (arr.length < 1) {
       System.out.println("Incorrect input size");
       return -1;
     }
     // while max heap has more than 1 element
     // remove top two elements from max heap and take the difference
     while (heap.heapsize() > 1) {
       long a = heap.removemax().longValue();
       long b = heap.removemax().longValue();
       Long difference = new Long (a - b);
       // reinsert the difference into the heap
       heap.insert(difference);
     }
     // when the heap contains 1 element, return
    return heap.removemax();
   }
 }
