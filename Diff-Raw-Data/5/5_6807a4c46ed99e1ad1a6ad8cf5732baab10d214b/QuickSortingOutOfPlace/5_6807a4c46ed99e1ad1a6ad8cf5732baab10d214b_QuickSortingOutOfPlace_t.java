 package edu.msergey.jalg.sorting;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Simplest quick sort using out of place sorting. Used creating and concatenating lists.
  * Pivot is 1st element.
  */
 public class QuickSortingOutOfPlace<E extends Comparable<E>> extends BaseSorting<E> {
     public QuickSortingOutOfPlace(E[] data) {
         super(data);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public E[] sort() {
         List<E> list = new ArrayList<>(Arrays.asList(data));
         List<E> result = sort(list);
 
        result.toArray(data);
        return data;
     }
 
     private List<E> sort(List<E> a) {
         if (a.size() == 0 || a.size() == 1) return a;
 
         List<E> less = new ArrayList<>();
         List<E> greater = new ArrayList<>();
         E pivot = a.get(0);
         a.remove(0);
         while (a.size() != 0) {
             if (a.get(0).compareTo(pivot) <= 0) {
                 less.add(a.get(0));
             } else {
                 greater.add(a.get(0));
             }
             a.remove(0);
         }
 
         List<E> result = new ArrayList<>();
         result.addAll(sort(less));
         result.add(pivot);
         result.addAll(sort(greater));
         return result;
     }
 }
