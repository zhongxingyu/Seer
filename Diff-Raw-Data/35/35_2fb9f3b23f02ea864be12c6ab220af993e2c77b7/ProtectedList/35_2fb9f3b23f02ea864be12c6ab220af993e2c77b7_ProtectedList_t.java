 /*
  * $Id: ProtectedList.java,v 1.20 2007/09/18 08:45:08 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Modifiable <code>List</code> implementaton that can be protected from
  * unauthorized changes.
  *
  * <p>A secret key must be passed when constructing a
  * <code>ProtectedPropertyReader</code> instance. All modification methods on
  * this object then require this same secret key to be passed, otherwise they
  * fail with an {@link IncorrectSecretKeyException}.
  *
  * <p>Note that the secret key equality is always checked before the other
  * preconditions. This means that if the secret key is incorrect, then the
  * other preconditions will not even be checked. For example, if <code>
  * {@link #remove(Object,int) remove}(null, -1)</code> is called, then an
  * {@link IncorrectSecretKeyException} is thrown for the mismatching secret
  * key, and not an {@link IndexOutOfBoundsException}, for the negative index.
  *
  * @version $Revision: 1.20 $ $Date: 2007/09/18 08:45:08 $
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.1.0
  */
public final class ProtectedList<T> extends AbstractList<T> implements Cloneable {
 
    /**
     * The secret key.
     */
    private final Object _secretKey;
 
    /**
     * The list containing the objects.
     */
   private ArrayList<T> _list;
 
    /**
     * Constructs an empty <code>ProtectedList</code> with the specified
     * initial capacity.
     *
     * @param secretKey
     *    the secret key that must be passed to the modification methods in
     *    order to be authorized to modify this collection.
     *
     * @param initialCapacity
     *    the initial capacity, cannot be a negative number.
     *
     * @throws IllegalArgumentException
     *    if <code>secretKey == null || initialCapacity &lt; 0</code>.
     *
     * @since XINS 1.2.0
     */
    public ProtectedList(Object secretKey, int initialCapacity)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("secretKey", secretKey);
 
       // Store secret key and construct internal list
       _secretKey = secretKey;
      _list      = new ArrayList<T>(initialCapacity);
    }
 
    /**
     * Constructs an empty <code>ProtectedList</code>.
     *
     * @param secretKey
     *    the secret key that must be passed to the modification methods in
     *    order to be authorized to modify this collection, cannot be
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>secretKey == null</code>.
     */
    public ProtectedList(Object secretKey)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("secretKey", secretKey);
 
       // Store secret key and construct internal list
       _secretKey = secretKey;
      _list      = new ArrayList<T>();
    }
 
    /**
     * Constructs a new <code>ProtectedList</code> containing the elements of
     * the specified collection, in the order they are returned by the
     * collection's iterator.
     *
     * @param secretKey
     *    the secret key that must be passed to the modification methods in
     *    order to be authorized to modify this collection, cannot be
     *    <code>null</code>.
     *
     * @param c
     *    the collection whose elements are to be placed into this list, cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>secretKey == null || c == null</code>.
     *
     * @since XINS 1.2.0
     */
   public ProtectedList(Object secretKey, Collection<T> c)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("secretKey", secretKey, "c", c);
 
       // Store secret key and construct internal list
       _secretKey = secretKey;
      _list      = new ArrayList<T>(c);
    }
 
    /**
     * Verifies that the specified object matches the secret key. If not, an
     * exception is thrown.
     *
     * @param secretKey
     *    the secret key, must be identity-equal to the secret key passed to
     *    the constructor, cannot be <code>null</code>.
     *
     * @throws IncorrectSecretKeyException
     *    if <code>secretKey</code> does not match the secret key passed to the
     *    constructor.
     */
    private void checkSecretKey(Object secretKey)
    throws IncorrectSecretKeyException {
       if (secretKey != _secretKey) {
          throw new IncorrectSecretKeyException();
       }
    }
 
    /**
     * Returns the element at the specified position in this list.
     *
     * @param index
     *    the index of the element to return, must be &gt;= 0 and &lt;
     *    {@link #size()}.
     *
     * @return
     *    the element at the specified position in this list, can be
     *    <code>null</code>.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>index &lt; 0
     *          || index &gt;= {@link #size()}</code>.
     */
   @Override
   public T get(int index)
    throws IndexOutOfBoundsException {
       return _list.get(index);
    }
 
    /**
     * Returns the number of elements in this list. If this list contains more
     * than {@link Integer#MAX_VALUE} elements, then {@link Integer#MAX_VALUE}
     * is returned.
     *
     * @return
     *    the size of this list, maximized to {@link Integer#MAX_VALUE}.
     */
    public int size() {
       return _list.size();
    }
 
    /**
     * Adds the specified element to the list.
     *
     * <p>The correct secret key must be passed. If it is incorrect, then an
     * {@link IncorrectSecretKeyException} is thrown. Note that an identity
     * check is done, <em>not</em> an equality check. So
     * the {@link Object#equals(Object)} method is not used, but the
     * <code>==</code> operator is.
     *
     * @param secretKey
     *    the secret key, must be identity-equal to the secret key passed to
     *    the constructor, cannot be <code>null</code>.
     *
     * @param element
     *    the element to add to the list, can be <code>null</code>.
     *
     * @throws IncorrectSecretKeyException
     *    if <code>secretKey</code> does not match the secret key passed to the
     *    constructor.
     */
   public void add(Object secretKey, T element)
    throws IncorrectSecretKeyException {
 
       // Check preconditions
       checkSecretKey(secretKey);
 
       // Store the value
       _list.add(element);
    }
 
    /**
     * Removes the specified element.
     *
     * <p>The correct secret key must be passed. If it is incorrect, then an
     * {@link IncorrectSecretKeyException} is thrown. Note that an identity
     * check is done, <em>not</em> an equality check. So
     * the {@link Object#equals(Object)} method is not used, but the
     * <code>==</code> operator is.
     *
     * @param secretKey
     *    the secret key, must be identity-equal to the secret key passed to
     *    the constructor, cannot be <code>null</code>.
     *
     * @param index
     *    the position of the element to remove, must be &gt;= 0 and &lt;
     *    {@link #size()}.
     *
     * @throws IncorrectSecretKeyException
     *    if <code>secretKey</code> does not match the secret key passed to the
     *    constructor.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>index &lt;  0
     *          || index &gt;= {@link #size()}</code>.
     */
    public void remove(Object secretKey, int index)
    throws IncorrectSecretKeyException, IndexOutOfBoundsException {
 
       // Check preconditions
       checkSecretKey(secretKey);
 
       // Remove the element
       _list.remove(index);
    }
 
 
    /**
     * Removes the specified element.
     *
     * <p>The correct secret key must be passed. If it is incorrect, then an
     * {@link IncorrectSecretKeyException} is thrown. Note that an identity
     * check is done, <em>not</em> an equality check. So
     * the {@link Object#equals(Object)} method is not used, but the
     * <code>==</code> operator is.
     *
     * @param secretKey
     *    the secret key, must be identity-equal to the secret key passed to
     *    the constructor, cannot be <code>null</code>.
     *
     * @param element
     *    the element to remove.
     *
     * @throws IncorrectSecretKeyException
     *    if <code>secretKey</code> does not match the secret key passed to the
     *    constructor.
     */
    public void remove(Object secretKey, Object element)
    throws IncorrectSecretKeyException {
 
       // Check preconditions
       checkSecretKey(secretKey);
 
       // Remove the element
       _list.remove(element);
    }
 
    /**
     * Clones this object. The returned object will be a new
     * <code>ProtectedList</code> instance with the same secret key and the
     * same elements.
     *
     * @return
     *    a new clone of this object, never <code>null</code>.
     */
   @Override
    public Object clone() {
 
       // Use Object.clone() to create a shallow clone
      ProtectedList<T> clone = null;
       try {
         clone = (ProtectedList<T>) super.clone();
       } catch (CloneNotSupportedException e) {
          // should never happen
       }
 
       // For a deep clone, duplicate the list as well
      clone._list = (ArrayList<T>) _list.clone();
 
       return clone;
    }
 }
