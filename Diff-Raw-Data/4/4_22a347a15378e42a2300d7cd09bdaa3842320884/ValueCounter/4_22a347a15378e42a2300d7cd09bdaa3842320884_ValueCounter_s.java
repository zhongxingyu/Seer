 /*
  * Copyright (C) 2013 shawware.com.au
  *
  * License: GNU General Public License V3 (or later)
  * http://www.gnu.org/copyleft/gpl.html
  */
 
 package au.com.shawware.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
 *
  * Counts the number of occurrences of the set of values of type T.
  * 
  * @param <T> the type of the values to count
  *
  * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
  */
 public class ValueCounter<T>
 {
    /** The values and their counts. */
     private final Map<T, MutableInteger> mValues;
 
     /**
      * Constructs a new, empty counter.
      */
     public ValueCounter()
     {
         mValues = new HashMap<T, MutableInteger>();
     }
 
     /**
      * Initialise a counter for the given value.
      * 
      * @param value the value to initialise a counter for
      */
     public void initialiseCount(final T value)
     {
         assert !mValues.containsKey(value);
         initialise(value);
     }
 
     /**
      * Adds a counter for the given value.
      * 
      * @param value the value to count
      * 
      * @return the value's counter
      */
     private MutableInteger initialise(final T value)
     {
         final MutableInteger mi = new MutableInteger();
         mValues.put(value, mi);
         return mi;
     }
 
     /**
      * Counts an instance of the given value.
      * Adds a counter for the value if one is not present.
      * 
      * @param value the value to count
      */
     public void countValue(final T value)
     {
         final MutableInteger mi;
         if (mValues.containsKey(value))
         {
             mi = mValues.get(value);
         }
         else
         {
             mi = initialise(value);
         }
         mi.increment();
     }
 
     /**
      * The set of values currently in this counter.
      * 
      * @return The set of values.
      */
     public Set<T> values()
     {
         return mValues.keySet();
     }
 
     /**
      * The current count for the given value.
      * 
      * @param value the value to return the count for
      * 
      * @return The given value's current count
      */
     public int count(final T value)
     {
         return mValues.containsKey(value) ? mValues.get(value).getValue() : 0;
     }
 }
