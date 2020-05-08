 package mergesort;
 
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveTask;
 
 public class SerialQuickSort extends QuickSort {
 
     private static final int PARALLELISM = 32;
 
     private ForkJoinPool pool = new ForkJoinPool(PARALLELISM);
 
     public int[] quicksort(int[] list) {
         return pool.invoke(new SortingRecursiveTask(list));
     }
 
     private class SortingRecursiveTask extends RecursiveTask<int[]> {
 
         private int[] list;
 
         private SortingRecursiveTask(int[] list) {
             this.list = list;
         }
 
         @Override
         protected int[] compute() {
             if (list == null || list.length == 0 ) {
                 return new int[0];
             }
             if (list.length == 1) {
                 return list;
             }
 
             AlgorithmResult algorithmResult = algorithm(list);
            list = algorithmResult.getList();
             int i = algorithmResult.getI();
             int j = algorithmResult.getJ();
 
             int[] sortedA = quickSortPartial(list, 0, j);
             int[] sortedB = quickSortPartial(list, i, list.length - 1);
             return combinePartialLists(list, sortedA, j, i, sortedB);
         }
     }
 }
