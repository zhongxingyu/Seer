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
