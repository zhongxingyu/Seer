 package tschumacher.playground.sorters;
 
 import tschumacher.playground.ArraySorter;
 
 /**
  *  <p>A simple wrapper class that logs timing information for an array sorter implementation.</p>
  */
 
 public class TimedSorter implements ArraySorter {
     
     private final ArraySorter sorter;
   
     /**
      *  <p>Create a new timed sorter.</p>
      *  
      *  @arg sorter The underlying sorting implementation.  Cannot be null.
      */  
     public TimedSorter(ArraySorter sorter) {
         if(sorter == null) {
             throw new NullPointerException("The sorter cannot be null");
         }
         this.sorter = sorter;
     }
 
     @Override 
     public void sort(int[] arr) {
         long a, b;
        a = System.currentTimeMillis();
         this.sorter.sort(arr);
        b = System.currentTimeMillis();
         System.out.println("Sorted in " + (b-a) + " millis.");
     }
 }
