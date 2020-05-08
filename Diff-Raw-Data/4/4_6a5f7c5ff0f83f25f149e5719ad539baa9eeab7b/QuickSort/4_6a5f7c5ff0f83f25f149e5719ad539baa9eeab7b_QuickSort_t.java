 package rio.sorter;
 
 public class QuickSort implements Sort {
 
     private final long[] array;
     private final int threshold;
 
     public QuickSort(long[] array, int threshold) {
         
         this.array = array;
         this.threshold = threshold;
     }
 
     public QuickSort(long[] array) {
        this(array, 50);  // 50 seems to be the best threshold according to tests
     }
 
     @Override
     public void sort() {
         quickSort(0, array.length - 1);
     }
 
     private void quickSort(int leftmostIndex, int rightmostIndex) {
         
         if (rightmostIndex - leftmostIndex < threshold) {
             insertionSort(leftmostIndex, rightmostIndex);
             return;
         }
         
         if (leftmostIndex < rightmostIndex) {
             int pivotIndex = leftmostIndex + (rightmostIndex - leftmostIndex) / 2;
             pivotIndex = partition(leftmostIndex, rightmostIndex, pivotIndex);
             quickSort(leftmostIndex, pivotIndex);
             quickSort(pivotIndex + 1, rightmostIndex);
         }
     }
 
     private int partition(int leftmostIndex, int rightmostIndex, int pivotIndex) {
         
         long pivotValue = array[pivotIndex];
         swapElements(pivotIndex, rightmostIndex);
         int newPivotIndex = leftmostIndex;
         for (int i = leftmostIndex; i < rightmostIndex; i++) {
             if (array[i] < pivotValue) {
                 swapElements(i, newPivotIndex);
                 newPivotIndex++;
             }
         }
        
         swapElements(newPivotIndex, rightmostIndex);
 
         return newPivotIndex;
     }
 
     private void swapElements(int index1, int index2) {
         
         long temp = array[index1];
         array[index1] = array[index2];
         array[index2] = temp;
     }
     
     private void insertionSort(int from, int to) {
         
         for (int i = from; i <= to; i++) {
             long value = array[i];
             int j = i - 1;
             while (j >= 0 && array[j] > value) {
                 array[j + 1] = array[j];
                 j--;
             }
             array[j + 1] = value;
         }
     }
 }
