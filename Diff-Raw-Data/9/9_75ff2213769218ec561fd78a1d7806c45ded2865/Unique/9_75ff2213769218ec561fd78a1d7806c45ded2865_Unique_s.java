 /*
   This file is licensed through the GNU public License.  Please read it.
   Basically you can modify this code as you wish, but you need to distribute
   the source code for all applications that use this code.
 
   I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
 */
 package org.tcfreenet.schewe.utils;
 
 import java.util.Comparator;
 
 /**
    This interface is used to represent the uniqueness of an object.  Each
    object within a JVM that implements the Unique interface must return a
    different uid unless those objects are to be considered equal.  An easy way
    to implement this is to create a new {@link DefaultUnique} object in the
    constructor and delegate the getUID() method to that object.
 **/
 public interface Unique {
   public long getUID();
 
   /**
      Comparator for comparing unique objects.
   **/
  final static Comparator UNIQUE_COMPARATOR = new Comparator() {
     public boolean equals(final Object o) {
       return o == this;
     }
     
     /**
        @throws ClassCastException if o1 and o2 are not instances of Unique
     **/
     public int compare(final Object o1, final Object o2) throws ClassCastException {
       if(o1.equals(o2)) {
         return 0;
       } else {
         final Unique u1 = (Unique)o1;
         final Unique u2 = (Unique)o2;
         if(u1.getUID() < u2.getUID()) {
           return -1;
         } else {
           return 1;
         }
       }
     }
   };
   
 }
