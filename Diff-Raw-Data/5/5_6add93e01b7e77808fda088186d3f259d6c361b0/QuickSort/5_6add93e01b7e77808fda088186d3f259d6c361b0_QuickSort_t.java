 package quicksort;
 
 import java.util.Random;
 
 /**
  * Efficient implementation of an in-place quicksort. Probably a bit more
 * complex than necessary, but not as much as it seems. In-place quicksort with
  * proper handling of pivot element (not fixed, possibly multiple occurrences)
  * is not easy!
  * 
  * @author Michael Borgwardt
  */
 public class QuickSort {
     private static final Random baseRnd = new Random();
 
     /** Used for choosing pivot element */
     private final Random rnd;
 
     public QuickSort() {
         rnd = baseRnd;
     }
 
     public QuickSort(Random r) {
         rnd = r;
     }
 
     /**
     * Perform in-place quicksort (ascending order) of the array.
      */
     public void quicksort(int[] array) {
         if (array.length < 2) {
             return;
         }
         quicksort(array, 0, array.length - 1);
     }
 
     /**
      * Perform the quicksort. Pivot elements are collected at the start of the
      * slice and swapped to the center before recursing.
      */
     private void quicksort(int[] array, int start, int end) {
         assert start <= end;
         assert start >= 0;
         assert end < array.length;
 
         switch (end - start) {
         case 0: // termination conditions
             return;
         case 1: // save some recursions
             if (array[start] > array[end]) {
                 swap(array, start, end);
             }
             break;
         default: // here it gets difficult
 
             // Points at end of collected pivot elements
             int pivotIndex = rnd.nextInt(end - start) + start;
 
             // pivot element value
             int pivot = array[pivotIndex];
 
             swap(array, pivotIndex, start);
             pivotIndex = start;
 
             // points at end of section that contains elements smaller than
             // pivot
             int lowerIndex = start + 1;
 
             // points at start of section that contains elements bigger than
             // pivot
             int higherIndex = end;
 
             while (lowerIndex <= higherIndex) {
                 if (array[lowerIndex] <= pivot) {
                     if (array[lowerIndex] == pivot) {
                         swap(array, lowerIndex, pivotIndex + 1);
                         pivotIndex++;
                     }
                     lowerIndex++;
                     continue;
                 }
                 if (array[higherIndex] >= pivot) {
                     if (array[higherIndex] == pivot) {
                         swap(array, higherIndex, pivotIndex + 1);
                         pivotIndex++;
                     } else {
                         higherIndex--;
                     }
                     continue;
                 }
                 swap(array, lowerIndex, higherIndex);
                 switch (higherIndex - lowerIndex) {
                 case 1:
                     break;
                 case 2:
                     if (array[lowerIndex + 1] <= pivot) {
                         lowerIndex++;
                     } else {
                         higherIndex--;
                     }
                 default:
                     lowerIndex++;
                     higherIndex--;
                 }
             }
             lowerIndex--;
 
             // swap pivot elements back towards middle of slice
             int pivotBegin = start;
             while (lowerIndex > pivotIndex && pivotBegin <= pivotIndex) {
                 swap(array, pivotBegin++, lowerIndex--);
             }
 
             // Now do recursions
             if (pivotBegin > pivotIndex) {
                 quicksort(array, start, lowerIndex);
             } else {
                 quicksort(array, start, pivotBegin);
 
             }
             quicksort(array, higherIndex, end);
         }
     }
 
     private void swap(int[] array, int index1, int index2) {
         int tmp = array[index1];
         array[index1] = array[index2];
         array[index2] = tmp;
     }
 }
