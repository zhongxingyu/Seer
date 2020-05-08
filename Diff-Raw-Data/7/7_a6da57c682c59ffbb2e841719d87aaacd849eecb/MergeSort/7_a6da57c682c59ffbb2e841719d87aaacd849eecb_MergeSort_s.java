 package guibin.zhang.onlinecourse;
 
 import java.util.Arrays;
 
 /**
  *
  * @author Guibin Zhang <guibin.beijing@gmail.com>
  */
 public class MergeSort {
     
     
     /**
      * Given two sorted subarrays a[lo] to a[mid] and a[mid+1] to a[hi], 
      * replace with sorted subarray a[lo] to a[hi].
      * 
      * @param a 
      * @param aux 
      * @param lo inclusive
      * @param mid mid
      * @param hi inclusive
      */
     private void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi) {
         
         // Copy to auxiliary array
         for (int k = lo; k <= hi; k++) {
             aux[k] = a[k];
         }
         
         int i = lo, j = mid + 1;
         for (int k = lo; k <= hi; k++) {
             if (i > mid)
                 a[k] = aux[j++]; //The left part is consumed
             else if (j > hi)
                 a[k] = aux[i++]; //The right part is consumed
            else if (aux[j].compareTo(a[i]) < 0) 
                a[k] = aux[j++]; //The right side is bigger
             else
                a[k] = aux[i++]; //The left side is bigger or equal
         }
     }
     
     /**
      * The sort is just divide the array recursively from the middle point,
      * then merge the two parts.
      * 
      * @param a The array to be sorted.
      * @param aux The auxiliary array
      * @param lo low index inclusive
      * @param hi high index inclusive
      */
     private void sort(Comparable[] a, Comparable[] aux, int lo, int hi) {
         
         if (lo >= hi) return;
         
         int mid = lo + (hi - lo) / 2; //Divide the array from mid
         sort(a, aux, lo, mid);
         sort(a, aux, mid + 1, hi);
         merge(a, aux, lo, mid, hi);
     }
     
     public void sort(Comparable[] a) {
         Comparable[] aux = new Comparable[a.length];
         sort(a, aux, 0, a.length - 1);
     }
     
     public static void main(String[] args) {
         Comparable[] a = {50, 61, 44, 32, 99, 87, 51, 50, 50, 12};
         MergeSort s = new MergeSort();
         s.sort(a);
         System.out.println("After sorting");
         Arrays.stream(a).forEach((c) -> System.out.print(c + ","));
         System.out.println();
     }
 }
