 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.equalizer;
 
 import java.io.Serializable;
 
 /**
  * Simple class that holds a min and max integer value thus describing a range. The values are immutable.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0
  */
 public final class Range implements Serializable {
 
     //~ Instance fields --------------------------------------------------------
 
     private final int min;
     private final int max;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new Range object. <code>min</code> must be less than <code>max</code> or otherwise an
      * {@link IllegalArgumentException} is thrown.
      *
      * @param   min  the minimal value of the <code>Range</code>
      * @param   max  the maximal value of the <code>Range</code>
      *
      * @throws  IllegalArgumentException  if <code>min</code> is not less than <code>max</code>
      */
     public Range(final int min, final int max) {
         if (min >= max) {
             throw new IllegalArgumentException("min is not less than max: [min=" + min + "|max=" + max + "]"); // NOI18N
         }
         this.min = min;
         this.max = max;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Getter for the minimal value of this <code>Range</code>.
      *
      * @return  the minimal value of this <code>Range</code>
      */
     public int getMin() {
         return min;
     }
 
     /**
      * Getter for the maximal value of this <code>Range</code>.
      *
      * @return  the maximal value of this <code>Range</code>
      */
     public int getMax() {
         return max;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = (11 * hash) + this.min;
         hash = (11 * hash) + this.max;
 
         return hash;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (getClass() != obj.getClass()) {
             return false;
         }
 
         final Range other = (Range)obj;
 
         return (this.min == other.min) && (this.max == other.max);
     }

    @Override
    public String toString() {
        return super.toString() + " [min=" + min + "|max=" + max + "]"; // NOI18N
    }
 }
