 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Implementation of a list that can only be modified using the secret key
  * passed to the constructor.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public final class ProtectedList extends AbstractList implements Cloneable {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ProtectedList</code>.
     *
     * @param key
     *    the secret key that must be passed to
    *    {@link #add(Object,Object)} in order to be authorized to
    *    modify this list, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public ProtectedList(Object key)
    throws IllegalArgumentException {
       
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       _key  = key;
       _list = new ArrayList();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The secret key.
     */
    private final Object _key;
 
    /**
     * The list containing the objects.
     */
    private ArrayList _list;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public Object get(int index) {
       return _list.get(index);
    }
    
    public int size() {
       return _list.size();
    }
    
    /**
     * Adds the specified element to the list.
     *
     * <p>The key must be passed. If it is incorrect, then an
     * {@link IllegalArgumentException} is thrown. Note that an identity check
     * is done, <em>not</em> an equality check. So
     * {@link Object#equals(Object)} is not used, but the <code>==</code>
     * operator is.
     *
     * @param key
     *    the secret key, must be the same as the key specified with the
     *    constructor, cannot be <code>null</code>.
     *
     * @param element
     *    the element to add to the list, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if the key is incorrect.
     */
    public void add(Object key, Object element)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (key != _key) {
          throw new IllegalArgumentException("Invalid key.");
       }
 
       // Store the value
       _list.add(element);
    }
 
    /**
     * Removes the specified element.
     *
     * <p>The key must be passed. If it is incorrect, then an
     * {@link IllegalArgumentException} is thrown. Note that an identity check
     * is done, <em>not</em> an equality check. So
     * {@link Object#equals(Object)} is not used, but the <code>==</code>
     * operator is.
     *
     * @param key
     *    the secret key, must be the same as the key specified with the
     *    constructor, cannot be <code>null</code>.
     *
     * @param index
     *    the position of the element to remove.
     *
     * @throws IllegalArgumentException
     *    if the key is incorrect.
     */
    public void remove(Object key, int index) throws IllegalArgumentException {
 
       // Check preconditions
       if (key != _key) {
          throw new IllegalArgumentException("Invalid key.");
       }
 
       // Remove the element
       _list.remove(index);
    }
 
    /**
     * Clones this list. The cloned list will only be ediatable by using the 
     * same secret key.
     *
     * @return
     *    a new clone of this object, never <code>null</code>.
     */
    public Object clone() {
       ProtectedList clone = new ProtectedList(_key);
       clone._list = (ArrayList)_list.clone();
       return clone;
    }
 }
