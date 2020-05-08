 /*
  * $Id$
  */
 package org.xins.common.collections;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Utility functions for collections.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public final class CollectionUtils extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns an array list containing the elements returned by the specified
     * iterator in the order they are returned by the iterator.
     *
     * @param iterator
     *    the {@link Iterator} providing elements for the returned array list,
     *    cannot be <code>null</code>.
     *
     * @return
     *   an {@link ArrayList} containing the elements returned by the specified
     *   iterator, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *   if <code>iterator == null</code>.
     */
    public static ArrayList list(Iterator iterator)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("iterator", iterator);
 
       // Create the ArrayList to store the elements in
       ArrayList list = new ArrayList();
 
       // Copy all elements from the Iterator to the ArrayList
       while (iterator.hasNext()) {
          list.add(iterator.next());
       }
 
       return list;
    }
 
    /**
     * Returns an array list containing the elements in the specified array, in
     * the same order.
     *
     * <p>No <code>null</code> values or duplicates are allowed.
     *
     * <p><em>This method is reserved for internal use by the XINS framework.
    * It may be removed in a future minor XINS release.</p>
     *
     * @param argumentName
     *    the name to assume for the array when constructing a message for an
     *    <code>IllegalArgumentException</code>, should not be
     *    <code>null</code>.
     *
     * @param array
     *    the array containing the elements to convert to an array list,
     *    cannot be <code>null</code>.
     *
     * @param min
     *    the minimum number of elements expected in the array.
     *
     * @return
     *   an {@link ArrayList} containing the elements in the array, in the same
     *   order, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>array                     ==   null
     *          || array.length              &lt; min
    *          || array[<em>i</em>          ==   null</code>
     *    or if <code>array</code> constains any duplicates.
     *
     * @since XINS 1.2.0
     */
    public static ArrayList list(String argumentName,
                                 Object[] array,
                                 int min)
    throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check(argumentName, array);
 
       // Check element count
       int arrayLength = array.length;
       if (arrayLength < min) {
          final String DETAIL = argumentName
                              + ".length ("
                              + arrayLength
                              + ") < "
                              + min;
          throw new IllegalArgumentException(DETAIL);
       }
 
       // Copy all elements to an array list, while validating
       ArrayList list = new ArrayList(arrayLength);
       for (int i = 0; i < arrayLength; i++) {
          Object elem = array[i];
          if (elem == null) {
             final String DETAIL = argumentName
                                 + '['
                                 + i
                                 + "] == null";
             throw new IllegalArgumentException(DETAIL);
          }
 
          int existing = list.indexOf(elem);
          if (existing >= 0) {
             final String DETAIL = argumentName
                                 + '['
                                 + existing
                                 + "] == "
                                 + argumentName
                                 + '['
                                 + i
                                 + "]";
             throw new IllegalArgumentException(DETAIL);
          }
       }
 
       return list;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Private constructor, no instances of this class should ever be
     * constructed.
     */
    private CollectionUtils() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
