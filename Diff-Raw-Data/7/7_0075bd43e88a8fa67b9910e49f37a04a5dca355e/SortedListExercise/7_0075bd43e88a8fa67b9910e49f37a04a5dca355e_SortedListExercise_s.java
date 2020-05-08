 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package exercise8;
 
 import BBK.PiJ01.common.Exercise;
 import BBK.PiJ01.common.IOGeneric;
 
 /**
  *
  * @author Sam Wright <swrigh11@dcs.bbk.ac.uk>
  */
 public class SortedListExercise implements Exercise {
     
     @Override
     public String getTitle() {
        return "Trees and LinkedLists as sets.";
     }
 
     @Override
     public String getDescription() {
        return "Demonstrates two implementations of a set, based on binary\n"
                + "trees and linked lists.";
     }
 
     @Override
     public void run() {
         TreeIntSortedList tree_sorted_list = new TreeIntSortedList();
         TreeIntSortedList tree_sorted_list_rebalanced = new TreeIntSortedList();
         ListIntSortedList lst_sorted = new ListIntSortedList();
         
         for (int i=0; i<10; i++) {
             tree_sorted_list.add(i);
             tree_sorted_list_rebalanced.add(i);
             lst_sorted.add(i);
         }
         
         for (int i=5; i<15; i++) {
             tree_sorted_list.add(i);
             tree_sorted_list_rebalanced.add(i);
             lst_sorted.add(i);
         }
         
         tree_sorted_list_rebalanced.rebalance();
         
         IOGeneric.printTitle("Sorted linked list:", "-");
         System.out.println(lst_sorted);
         
         IOGeneric.printTitle("Sorted tree list.", "-");
         System.out.println(tree_sorted_list);
         
         IOGeneric.printTitle("Sorted rebalanced tree list", "-");
         System.out.println(tree_sorted_list_rebalanced);
     }
 
 }
