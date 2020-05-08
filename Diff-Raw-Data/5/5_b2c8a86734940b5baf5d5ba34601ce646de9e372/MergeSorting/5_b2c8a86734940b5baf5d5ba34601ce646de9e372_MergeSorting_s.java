 package edu.msergey.jalg.sorting;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Merge sort is an O(n log n) comparison-based sorting algorithm.
  *
  * Conceptually, a merge sort works as follows:
  * 1.Divide the unsorted list into n sublists, each containing 1 element.
  * 2.Repeatedly Merge sublists to produce new sublists until there is only 1 sublist remaining.
  */
 public class MergeSorting<E extends Comparable<E>> extends BaseSorting<E> {
     @Override
     @SuppressWarnings("unchecked")
     public E[] sort() {
         List<E> list = Arrays.asList(data);
         List<E> result = sort(list);
 
        // TODO: mess, consider switching to collections
        data = (E[]) new Comparable[result.size()];
        return result.toArray(data);
     }
     
     private List<E> sort(List<E> list) {
         if (list.size() == 0 || list.size() == 1) return list;
         
         List<E> leftSortedList = sort(list.subList(0, list.size() / 2));
         List<E> rightSortedList = sort(list.subList(list.size() / 2, list.size()));
         return mergeSortedLists(leftSortedList, rightSortedList);
     }
     
     private List<E> mergeSortedLists(List<E> leftSortedList, List<E> rightSortedList) {
         int i = 0; // leftSortedList index
         int j = 0; // rightSortedList index
         List<E> mergedList = new ArrayList<>(leftSortedList.size() + rightSortedList.size());
         for (int k = 0; k < leftSortedList.size() + rightSortedList.size(); k++) {
             if (i == leftSortedList.size()) {
                 mergedList.addAll(rightSortedList.subList(j, rightSortedList.size()));
                 break;
             }
             if (j == rightSortedList.size()) {
                 mergedList.addAll(leftSortedList.subList(i, leftSortedList.size()));
                 break;
             }
             if (leftSortedList.get(i).compareTo(rightSortedList.get(j)) <= 0) {
                 mergedList.add(leftSortedList.get(i));
                 i++;
             } else {
                 mergedList.add(rightSortedList.get(j));
                 j++;
             }
         }
         return mergedList;
     }
 
     public MergeSorting(E[] data) {
         super(data);
     }
 }
