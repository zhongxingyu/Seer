 package org.opengraph.graph.edge.util;
 
 public class EdgeIndexComparatorImpl implements EdgeIndexComparator {
 
     private final boolean descending;
 
     public EdgeIndexComparatorImpl() {
         this(false);
     }
 
     public EdgeIndexComparatorImpl(boolean descending) {
         this.descending = descending;
     }
 
     @Override
     public int compare(Integer e1, Integer e2) {
         int c = e1.compareTo(e2);
         if (descending) {
            c *= -1;
         }
         return c;
     }
 
     @Override
     public boolean isSorted() {
         return true;
     }
 
 }
