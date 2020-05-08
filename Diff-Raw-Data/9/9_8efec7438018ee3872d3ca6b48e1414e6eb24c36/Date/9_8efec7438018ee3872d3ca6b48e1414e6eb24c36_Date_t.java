 package com.undeadscythes.gedcomstd.structure;
 
 /**
  * This class offers some handy tools for manipulating dates in a way that can
 * be translated in the {@link com.undeadscythes.gedcomstd.GEDCOM} form.
  *
  * @author UndeadScythes
  */
 public class Date {
     public final int year;
 
     /**
      * Create an exact {@link Date} with the given properties.
      */
     public Date(final int day, final int month, final int year) {
         //TODO: Implement me
         this.year = year;
     }
 
     @Override
     public String toString() {
         return String.valueOf(year);
     }
 
     /**
      * Compare this {@link Date} to another, defaults to
      * {@link com.undeadscythes.gedcomstd.comparator.SortByDate#INCREASING INCREASING}.
      */
     public int compareTo(final Date date) {
         return 0; //TODO: Implement me
     }
 }
