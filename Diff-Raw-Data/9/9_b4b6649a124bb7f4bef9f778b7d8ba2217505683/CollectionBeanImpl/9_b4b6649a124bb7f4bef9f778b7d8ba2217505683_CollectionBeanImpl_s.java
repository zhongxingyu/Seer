 /*
  * Copyright (C) 2014 Sebastien Diot.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.blockwithme.meta.beans.impl;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.Objects;
 
 import com.blockwithme.meta.Type;
 import com.blockwithme.meta.beans.CollectionBeanConfig;
 import com.blockwithme.meta.beans.ObjectCollectionInterceptor;
 import com.blockwithme.meta.beans._Bean;
 import com.blockwithme.meta.beans._CollectionBean;
 
 /**
  * Base class for a Bean that contains a Collection of other Objects.
  *
  * @author monster
  */
 public class CollectionBeanImpl<E> extends _BeanImpl implements
         _CollectionBean<E> {
 
     /** Non-comparable Comparator. */
     private static final Comparator<Object> CMP = new Comparator<Object>() {
         @Override
         public int compare(final Object o1, final Object o2) {
             if (o1 == null) {
                 if (o2 == null) {
                     return 0;
                 }
                 return -1;
             }
             if (o2 == null) {
                 return 1;
             }
             final int hash1 = o1.hashCode();
             final int hash2 = o2.hashCode();
             if (hash1 == hash2) {
                 if (o1.equals(o2)) {
                     return 0;
                 }
                 return o1.toString().compareTo(o2.toString());
             }
             return hash1 - hash2;
         }
     };
 
     /** An Iterable<_Bean>, over the Collection values */
     private class BeanCollectionIterator implements Iterable<_Bean>,
             Iterator<_Bean> {
 
         /** The next bean */
         private _Bean next;
 
         /** The index of "next". */
         private int nextIndex;
 
         private void findNext() {
             final ObjectCollectionInterceptor<E> oci = interceptor();
             final E[] array = data;
             while (nextIndex < size) {
                 final Object value = oci.getObjectAtIndex(
                         CollectionBeanImpl.this, nextIndex, array[nextIndex]);
                 if (value instanceof _Bean) {
                     next = (_Bean) value;
                     break;
                 }
                 nextIndex++;
             }
             next = null;
         }
 
         /** Constructor */
         public BeanCollectionIterator() {
             findNext();
         }
 
         /* (non-Javadoc)
          * @see java.lang.Iterable#iterator()
          */
         @Override
         public final Iterator<_Bean> iterator() {
             return this;
         }
 
         /* (non-Javadoc)
          * @see java.util.Iterator#remove()
          */
         @Override
         public final void remove() {
             throw new UnsupportedOperationException();
         }
 
         /* (non-Javadoc)
          * @see java.util.Iterator#hasNext()
          */
         @Override
         public final boolean hasNext() {
             return next != null;
         }
 
         /* (non-Javadoc)
          * @see java.util.Iterator#next()
          */
         @Override
         public final _Bean next() {
             if (hasNext()) {
                 final _Bean result = next;
                 nextIndex++;
                 findNext();
                 return result;
             }
             throw new NoSuchElementException();
         }
 
     }
 
     /**
      * Basic Collection Iterator
      */
     private class Itr implements Iterator<E> {
         int cursor;       // index of next element to return
         int lastRet = -1; // index of last element returned; -1 if no such
 
         @Override
         public boolean hasNext() {
             return cursor != size;
         }
 
         @Override
         public E next() {
             final int i = cursor;
             if (i >= size)
                 throw new NoSuchElementException();
             final E[] array = data;
             final ObjectCollectionInterceptor<E> oci = interceptor();
             if (i >= array.length)
                 throw new ConcurrentModificationException();
             cursor = i + 1;
             lastRet = i;
             return oci.getObjectAtIndex(CollectionBeanImpl.this, i, array[i]);
         }
 
         @Override
         public void remove() {
             if (lastRet < 0)
                 throw new IllegalStateException();
 
             try {
                 CollectionBeanImpl.this.remove(lastRet);
                 if (config.getFixedSize() == -1) {
                     cursor = lastRet;
                 }
                 lastRet = -1;
             } catch (final IndexOutOfBoundsException ex) {
                 throw new ConcurrentModificationException();
             }
         }
     }
 
     /**
      * A ListIterator
      */
     private class ListItr extends Itr implements ListIterator<E> {
         ListItr(final int index) {
             super();
             cursor = index;
         }
 
         @Override
         public boolean hasPrevious() {
             return cursor != 0;
         }
 
         @Override
         public int nextIndex() {
             return cursor;
         }
 
         @Override
         public int previousIndex() {
             return cursor - 1;
         }
 
         @Override
         public E previous() {
             final int i = cursor - 1;
             if (i < 0)
                 throw new NoSuchElementException();
             final E[] array = data;
             final ObjectCollectionInterceptor<E> oci = interceptor();
             if (i >= array.length)
                 throw new ConcurrentModificationException();
             cursor = i;
             lastRet = i;
             return oci.getObjectAtIndex(CollectionBeanImpl.this, i, array[i]);
         }
 
         @Override
         public void set(final E e) {
             if (lastRet < 0)
                 throw new IllegalStateException();
 
             try {
                 CollectionBeanImpl.this.set(lastRet, e);
             } catch (final IndexOutOfBoundsException ex) {
                 throw new ConcurrentModificationException();
             }
         }
 
         @Override
         public void add(final E e) {
             try {
                 final int i = cursor;
                 CollectionBeanImpl.this.add(i, e);
                 cursor = i + 1;
                 lastRet = -1;
             } catch (final IndexOutOfBoundsException ex) {
                 throw new ConcurrentModificationException();
             }
         }
     }
 
     /** Minimum size */
     private static final int MIN_SIZE = 8;
 
     /** The CollectionBeanConfig */
     private final CollectionBeanConfig config;
 
     /** The Type of the values. */
     private final Type<E> valueType;
 
     /** The collection size */
     private int size;
 
     /** The Collection */
     private E[] data;
 
     /** Returns a new E array of given size. */
     private E[] newArray(final int length) {
         return valueType.newArray(length);
     }
 
     private E getInternal(final int index) {
         return interceptor().getObjectAtIndex(this, index, data[index]);
     }
 
     /**
      * Checks if the given index is in range.
      */
     private void rangeCheck(final int index) {
         if (index >= size)
             throw new IndexOutOfBoundsException("Index: " + index + " Size: "
                     + size);
     }
 
     /** Increases the array size, if needed. */
     private void ensureCapacityInternal(final int minCapacity) {
         // ensureSelectionCapacity() makes sure that the value of minCapacity is "reasonable"
         ensureSelectionCapacity(minCapacity);
         final E[] array = data;
         final int oldCapacity = array.length;
         if (minCapacity > oldCapacity) {
             int newCapacity;
             if (minCapacity < MIN_SIZE) {
                 newCapacity = MIN_SIZE;
             } else {
                 newCapacity = oldCapacity;
                 while (newCapacity < minCapacity) {
                     newCapacity *= 2;
                 }
             }
             final E[] newArray = newArray(newCapacity);
             System.arraycopy(array, 0, newArray, 0, oldCapacity);
             data = newArray;
         }
     }
 
     /**
      * Creates a new CollectionBeanImpl with the given Type and configuration.
      *
      * @param metaType The type of the Bean (not of the component); required
      * @param valueType The type of the component; required
      * @param config The collection bean configuration; required.
      */
     @SuppressWarnings("unchecked")
     public CollectionBeanImpl(final Type<?> metaType, final Type<E> valueType,
             final CollectionBeanConfig config) {
         super(metaType);
         interceptor = DefaultCollectionInterceptor.INSTANCE;
         this.valueType = Objects.requireNonNull(valueType, "valueType");
         Objects.requireNonNull(config, "config").validate(valueType);
         this.config = config;
         final int fixedSize = config.getFixedSize();
         if (fixedSize != -1) {
             size = fixedSize;
             data = newArray(fixedSize);
         } else {
             data = (E[]) metaType.empty;
         }
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.CollectionBean#getValueType()
      */
     @Override
     public final Type<E> getValueType() {
         return valueType;
     }
 
     /** Returns an Iterable<_Bean>, over the property values */
     @Override
     protected final Iterable<_Bean> getBeanIterator() {
         return new BeanCollectionIterator();
     }
 
     /** Returns the number of possible selections. */
     @Override
     protected final int getSelectionCount() {
         return size;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#size()
      */
     @Override
     public final int size() {
         return size;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#isEmpty()
      */
     @Override
     public final boolean isEmpty() {
         return size == 0;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#contains(java.lang.Object)
      */
     @Override
     public final boolean contains(final Object o) {
         return indexOf(o) != -1;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#containsAll(java.util.Collection)
      */
     @Override
     public final boolean containsAll(final Collection<?> c) {
         for (final Object e : c)
             if (!contains(e))
                 return false;
         return true;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#toArray()
      */
     @Override
     public final Object[] toArray() {
         return toArray(newArray(size));
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#addAll(java.util.Collection)
      */
     @Override
     public final boolean addAll(final Collection<? extends E> c) {
         boolean modified = false;
         for (final E e : c)
             if (add(e))
                 modified = true;
         return modified;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#removeAll(java.util.Collection)
      */
     @Override
     public final boolean removeAll(final Collection<?> c) {
         boolean modified = false;
         for (final Object e : c)
             if (remove(e))
                 modified = true;
         return modified;
     }
 
     /* (non-Javadoc)
      * @see java.util.List#indexOf(java.lang.Object)
      */
     @Override
     public final int indexOf(final Object o) {
         final int length = size;
         final E[] array = data;
         final ObjectCollectionInterceptor<E> oci = interceptor();
         if (o == null) {
             for (int i = 0; i < length; i++) {
                 if (oci.getObjectAtIndex(this, i, array[i]) == null) {
                     return i;
                 }
             }
         } else {
             for (int i = 0; i < length; i++) {
                 if (o.equals(oci.getObjectAtIndex(this, i, array[i]))) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /* (non-Javadoc)
      * @see java.util.List#lastIndexOf(java.lang.Object)
      */
     @Override
     public final int lastIndexOf(final Object o) {
         final int length = size;
         final E[] array = data;
         final ObjectCollectionInterceptor<E> oci = interceptor();
         if (o == null) {
             for (int i = length - 1; i >= 0; i--) {
                 if (oci.getObjectAtIndex(this, i, array[i]) == null) {
                     return i;
                 }
             }
         } else {
             for (int i = length - 1; i >= 0; i--) {
                 if (o.equals(oci.getObjectAtIndex(this, i, array[i]))) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#retainAll(java.util.Collection)
      */
     @Override
     public final boolean retainAll(final Collection<?> c) {
         boolean modified = false;
         final E[] array = data;
         final ObjectCollectionInterceptor<E> oci = interceptor();
         for (int i = 0; i < size; i++) {
             final E value = oci.getObjectAtIndex(this, i, array[i]);
             if (!c.contains(value)) {
                 remove(i--);
                 modified = true;
             }
         }
         return modified;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#remove(java.lang.Object)
      */
     @Override
     public final boolean remove(final Object o) {
         final int index = indexOf(o);
         if (index == -1) {
             return false;
         }
         remove(index);
         return true;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#toArray(java.lang.Object[])
      */
     @SuppressWarnings("unchecked")
     @Override
     public final <T> T[] toArray(final T[] a) {
         final T[] result = a.length >= size ? a : (T[]) java.lang.reflect.Array
                 .newInstance(a.getClass().getComponentType(), size);
         final ObjectCollectionInterceptor<E> oci = interceptor();
         final E[] array = data;
         for (int i = 0; i < size; i++) {
             result[i] = (T) oci.getObjectAtIndex(this, i, array[i]);
         }
         return result;
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#iterator()
      */
     @Override
     public final Iterator<E> iterator() {
         return new Itr();
     }
 
     /* (non-Javadoc)
      * @see java.util.List#listIterator(int)
      */
     @Override
     public final ListIterator<E> listIterator(final int index) {
         if (index < 0 || index > size)
             throw new IndexOutOfBoundsException("Index: " + index);
         return new ListItr(index);
     }
 
     /* (non-Javadoc)
      * @see java.util.List#listIterator()
      */
     @Override
     public final ListIterator<E> listIterator() {
         return new ListItr(0);
     }
 
     /* (non-Javadoc)
      * @see java.util.List#get(int)
      */
     @Override
     public final E get(final int index) {
         rangeCheck(index);
         return getInternal(index);
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#add(java.lang.Object)
      */
     @Override
     public final boolean add(final E element) {
         return add2(size, element);
     }
 
     /* (non-Javadoc)
      * @see java.util.List#add(int,java.lang.Object)
      */
     @Override
     public final void add(final int index, final E element) {
         add2(index, element);
     }
 
     /* (non-Javadoc)
      * @see java.util.List#addAll(int, java.util.Collection)
      */
     @Override
     public final boolean addAll(final int index, final Collection<? extends E> c) {
         int i = index;
         for (final E e : c)
             if (add2(i, e)) {
                 i++;
             }
         return i != index;
     }
 
     /* (non-Javadoc)
      * @see java.util.List#subList(int, int)
      */
     @Override
     public final List<E> subList(final int fromIndex, final int toIndex) {
         throw new UnsupportedOperationException("TODO!");
     }
 
     /** Returns the collection interceptor. */
     @SuppressWarnings("unchecked")
     private ObjectCollectionInterceptor<E> interceptor() {
         return (ObjectCollectionInterceptor<E>) interceptor;
     }
 
     /** Validate the new value, based on the expected type, and acceptance of null values. */
     private void validateNewValue(final E element) {
         if (element == null) {
             if (!config.isNullAllowed()) {
                 throw new IllegalArgumentException("Null not allowed!");
             }
         } else if (config.isOnlyExactType()) {
             final Class<?> elementType = element.getClass();
             final Class<?> expectedType = getValueType().type;
             if (elementType != expectedType) {
                 throw new IllegalArgumentException("Expected type: "
                         + expectedType + " Actual type: " + elementType);
             }
         } else {
             // Just to be safe ...
             final Class<?> elementType = element.getClass();
             final Class<?> expectedType = getValueType().type;
             if (!expectedType.isAssignableFrom(elementType)) {
                 throw new IllegalArgumentException("Expected type: "
                         + expectedType + " Actual type: " + elementType);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see java.util.List#set(int,java.lang.Object)
      */
     @Override
     public final E set(final int index, final E newValue) {
         if (isImmutable()) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         rangeCheck(index);
         validateNewValue(newValue);
         if (config.isSet()) {
             throw new IllegalArgumentException(
                     "set(int,E) not allowed for Set!");
         }
         final E[] array = data;
         final ObjectCollectionInterceptor<E> oci = interceptor();
         final E oldValue = oci.getObjectAtIndex(this, index, array[index]);
         array[index] = oci.setObjectAtIndex(this, index, oldValue, newValue);
         return oldValue;
     }
 
     /* (non-Javadoc)
      * @see java.util.List#remove(int)
      */
     @Override
     public final E remove(final int index) {
         if (isImmutable()) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         rangeCheck(index);
         if (config.getFixedSize() == -1) {
             final E[] array = data;
             final ObjectCollectionInterceptor<E> oci = interceptor();
             final E result = oci.getObjectAtIndex(this, index, array[index]);
             if (config.isUnorderedSet()) {
                 oci.removeObjectAtIndex(this, index, result, false);
                 array[index] = oci.getObjectAtIndex(this, index,
                         array[size - 1]);
             } else {
                 for (int i = size - 1; i > index; i--) {
                     array[i - 1] = oci.getObjectAtIndex(this, i, array[i]);
                 }
                 oci.removeObjectAtIndex(this, index, result, true);
             }
             size--;
             array[size] = null;
             return result;
         }
         // In fixed-size lists, "removing" means setting to null.
         return set(index, null);
     }
 
     /* (non-Javadoc)
      * @see java.util.Collection#clear()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final void clear() {
         if (isImmutable()) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         if (config.getFixedSize() == -1) {
             interceptor().clear(this);
             size = 0;
             data = (E[]) getMetaType().empty;
             clearSelectionArray();
             clearSelection(false, false);
             incrementChangeCounter();
         } else {
             final E[] array = data;
             final ObjectCollectionInterceptor<E> oci = interceptor();
             for (int i = 0; i < size; i++) {
                 array[i] = oci.setObjectAtIndex(this, i, array[i], null);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see java.util.List#add(int,java.lang.Object)
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private boolean add2(final int index, final E element) {
         if (index > size || index < 0)
             throw new IndexOutOfBoundsException("Index: " + index);
         if (config.getFixedSize() != -1) {
             throw new UnsupportedOperationException("Fixed-size!");
         }
         if (isImmutable()) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         validateNewValue(element);
         ensureCapacityInternal(size + 1);
         final E[] array = data;
         final ObjectCollectionInterceptor<E> oci = interceptor();
         if (index == size) {
             if (config.isSet()) {
                 if (!contains(element)) {
                     if (!config.isSortedSet()) {
                         size++;
                         array[index] = oci.addObjectAtIndex(this, index,
                                 element, false);
                     } else {
                         final Comparable cmp = (Comparable) element;
                         boolean added = false;
                         for (int i = 0; !added && (i < size); i++) {
                            if (cmp.compareTo(array[i]) < 0) {
                                System.arraycopy(array, i, array, i + 1, size
                                        - i);
                                 size++;
                                 array[i] = oci.addObjectAtIndex(this, i,
                                         element, true);
                                 added = true;
                             }
                         }
                         if (!added) {
                             size++;
                             array[index] = oci.addObjectAtIndex(this, index,
                                     element, false);
                         }
                     }
                 }
             } else {
                 size++;
                 array[index] = oci
                         .addObjectAtIndex(this, index, element, false);
             }
         } else {
             if (config.isSet()) {
                 throw new IllegalArgumentException(
                         "add(int,E) not allowed for Set!");
             }
             for (int i = size; i > index; i--) {
                 array[i] = oci.getObjectAtIndex(this, i - 1, array[i - 1]);
             }
             size++;
             array[index] = oci.addObjectAtIndex(this, index, element, true);
         }
         return true;
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.ContentOwner#getContent()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final E[] getContent() {
         final E[] result = (E[]) toArray();
         if (config.isUnorderedSet()) {
             if (Comparable.class.isAssignableFrom(getMetaType().type)) {
                 Arrays.sort(result);
             } else {
                 Arrays.sort(result, CMP);
             }
         }
         return result;
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.CollectionBean#getConfig()
      */
     @Override
     public final CollectionBeanConfig getConfig() {
         return config;
     }
 
     /** Make a new instance of the same type as self. */
     @Override
     protected _BeanImpl newInstance() {
         return new CollectionBeanImpl<E>(metaType, valueType, config);
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.Bean#copy()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final CollectionBeanImpl<E> copy() {
         return (CollectionBeanImpl<E>) doCopy();
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.Bean#snapshot()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final CollectionBeanImpl<E> snapshot() {
         return (CollectionBeanImpl<E>) doSnapshot();
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.Bean#wrapper()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final CollectionBeanImpl<E> wrapper() {
         final CollectionBeanImpl<E> result = (CollectionBeanImpl<E>) doWrapper();
         result.interceptor = CollectionWrapperInterceptor.INSTANCE;
         result.ensureCapacityInternal(size);
         result.size = size;
         return result;
     }
 
     /* (non-Javadoc)
      * @see com.blockwithme.meta.beans.Bean#getDelegate()
      */
     @SuppressWarnings("unchecked")
     @Override
     public final CollectionBeanImpl<E> getDelegate() {
         return (CollectionBeanImpl<E>) super.getDelegate();
     }
 }
