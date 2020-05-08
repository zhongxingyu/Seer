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
 
 import java.io.IOException;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Objects;
 
 import com.blockwithme.meta.ObjectProperty;
 import com.blockwithme.meta.Property;
 import com.blockwithme.meta.Type;
 import com.blockwithme.meta.beans.Entity;
 import com.blockwithme.meta.beans.Interceptor;
 import com.blockwithme.meta.beans._Bean;
 import com.blockwithme.murmur.MurmurHash;
 
 /**
  * Bean impl for all data/bean objects.
  *
  * This class is written in Java, due to the inability of Xtend to
  * use bitwise operators!
  *
  * @author monster
  */
 public abstract class _BeanImpl implements _Bean {
 
     /** An Iterable<_Bean>, over the property values */
     private class SubBeanIterator implements Iterable<_Bean>, Iterator<_Bean> {
 
         /** The properties */
         @SuppressWarnings("rawtypes")
         private final ObjectProperty[] properties = metaType.inheritedObjectProperties;
 
         /** The next _Bean, if any. */
         private _Bean next;
 
         /** Next property index to check. */
         private int nextIndex;
 
         @SuppressWarnings({ "rawtypes", "unchecked" })
         private void findNext() {
             while (nextIndex < properties.length) {
                 final ObjectProperty p = properties[nextIndex];
                 if (p.bean) {
                     next = (_Bean) p.getObject(_BeanImpl.this);
                     if (next != null) {
                        break;
                     }
                 }
                 nextIndex++;
             }
             next = null;
         }
 
         /** Constructor */
         public SubBeanIterator() {
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
                 findNext();
                 return result;
             }
             throw new NoSuchElementException();
         }
 
     }
 
     /** Empty long[], used in selectedArray */
     private static final long[] NO_LONG = new long[0];
 
     /** Reasonable maximum size. */
     private static final int MAX_SIZE = 65536;
 
     /** Our meta type */
     protected final Type<?> metaType;
 
     /**
      * The required interceptor. It allows customizing the behavior of Beans
      * while only generating a single implementation per type.
      */
     protected Interceptor interceptor = DefaultInterceptor.INSTANCE;
 
     /**
      * Optional "delegate"; must have the same type as "this".
      * Allows re-using the same generated code for "wrappers" ...
      */
     protected _Bean delegate;
 
     /** Are we immutable? */
     private boolean immutable;
 
     /**
      * The "parent" Bean, if any.
      *
      * Bean do not support cycles in the structure, and so are limited to tree
      * structures. This means we can only ever have one parent maximum. This
      * parent field is *managed automatically*, and allows traversing the tree
      * in all directions.
      */
     private _Bean parent;
 
     /** 64 "selected" flags */
     private long selected;
 
     /** More "selected" flags, if 64 is not enough, or if the number varies */
     private long[] selectedArray;
 
     /** The change counter */
     private int changeCounter;
 
     /**
      * Lazily cached toString result (null == not computed yet)
      * Cleared automatically when the "state" of the Bean changes.
      */
     private String toString;
 
     /**
      * Lazily cached hashCode64 result (0 == not computed yet)
      * Cleared automatically when the "state" of the Bean changes.
      */
     private long toStringHashCode64;
 
     /** Resets the cached state (when something changes) */
     private void resetCachedState() {
         toStringHashCode64 = 0;
         toString = null;
     }
 
     /** Compares two boolean */
     protected final int compare(final boolean a, final boolean b) {
         return (a == b) ? 0 : (a ? 1 : -1);
     }
 
     /** Compares two byte */
     protected final int compare(final byte a, final byte b) {
         return a - b;
     }
 
     /** Compares two short */
     protected final int compare(final short a, final short b) {
         return a - b;
     }
 
     /** Compares two char */
     protected final int compare(final char a, final char b) {
         return a - b;
     }
 
     /** Compares two int */
     protected final int compare(final int a, final int b) {
         return a - b;
     }
 
     /** Compares two long */
     protected final int compare(final long a, final long b) {
         return (a < b) ? -1 : ((a == b) ? 0 : 1);
     }
 
     /** Compares two float */
     protected final int compare(final float a, final float b) {
         return Float.compare(a, b);
     }
 
     /** Compares two double */
     protected final int compare(final double a, final double b) {
         return Double.compare(a, b);
     }
 
     /** Compares two Object */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     protected final int compare(final Comparable a, final Comparable b) {
         return (a == null) ? ((b == null) ? 0 : -1) : ((b == null) ? 1 : a
                 .compareTo(b));
     }
 
     /** For special beans with variable size, we can grow the selection range. */
     protected final void ensureSelectionCapacity(final int bitsMinCapacity) {
         if (bitsMinCapacity < 0) {
             throw new IllegalArgumentException("bitsMinCapacity="
                     + bitsMinCapacity);
         }
         if (bitsMinCapacity > MAX_SIZE) {
             throw new IllegalArgumentException("bitsMinCapacity ("
                     + bitsMinCapacity + ") > MAX_SIZE=" + MAX_SIZE);
         }
         final long[] array = selectedArray;
         final int oldArrayCapacity = array.length;
         int minCapacity = bitsMinCapacity / 64;
         if (bitsMinCapacity % 64 != 0) {
             minCapacity++;
         }
         // +1 because of the "selected" long field
         if (minCapacity > oldArrayCapacity + 1) {
             selectedArray = new long[minCapacity - 1];
             System.arraycopy(array, 0, selectedArray, 0, oldArrayCapacity);
         }
     }
 
     /** For special beans with variable size, we can clear the selection array. */
     protected final void clearSelectionArray() {
         selectedArray = NO_LONG;
     }
 
     /** The constructor; metaType is required. */
     public _BeanImpl(final Type<?> metaType) {
         Objects.requireNonNull(metaType, "metaType");
         // Make sure we get the "right" metaType
         final String myType = getClass().getName();
         final int lastDot = myType.lastIndexOf('.');
         final String myPkg = myType.substring(0, lastDot);
         final int preLastDot = myPkg.lastIndexOf('.');
         final String parentPkg = myPkg.substring(0, preLastDot);
         final String myInterfaceName = parentPkg + "."
                 + myType.substring(lastDot + 1, myType.length() - 4);
         if (!myInterfaceName.equals(metaType.type.getName())) {
             throw new IllegalArgumentException("Type should be "
                     + myInterfaceName + " but was " + metaType.type.getName());
         }
         this.metaType = metaType;
         // Setup the selectedArray. The idea is that small objects do not
         // require an additional long[] instance, making small more lightweight.
         final int propertyCount = getSelectionCount();
         int arraySizePlusOne = propertyCount / 64;
         if (propertyCount % 64 != 0) {
             arraySizePlusOne++;
         }
         selectedArray = (arraySizePlusOne <= 1) ? NO_LONG
                 : new long[arraySizePlusOne - 1];
     }
 
     /** Returns our metaType. Cannot be null. */
     @Override
     public final Type<?> getMetaType() {
         return metaType;
     }
 
     /** Returns true if we are immutable */
     @Override
     public final boolean isImmutable() {
         return immutable;
     }
 
     /** Sets the immutable flag to true. */
     @Override
     public final void makeImmutable() {
         immutable = true;
     }
 
     /** Returns true if some property is "selected" */
     @Override
     public final boolean isSelected() {
         if (selected != 0) {
             return true;
         }
         final long[] array = selectedArray;
         for (final long l : array) {
             if (l != 0) {
                 return true;
             }
         }
         return false;
     }
 
     /** Returns true, if some property was "selected", either in self, or in children */
     @Override
     public final boolean isSelectedRecursive() {
         if (isSelected()) {
             return true;
         }
         for (final _Bean value : getBeanIterator()) {
             if (value.isSelectedRecursive()) {
                 return true;
             }
         }
         return false;
     }
 
     /** Returns true if the specified property was selected */
     @Override
     public final boolean isSelected(final Property<?, ?> prop) {
         return isSelected(indexOfProperty(prop));
     }
 
     /** Returns true if the specified property at the given index was selected */
     @Override
     public final boolean isSelected(final int index) {
         if (index < 64) {
             return (selected & (1L << index)) != 0;
         }
         final long sel = selectedArray[index / 64 - 1];
         return (sel & (1L << (index % 64))) != 0;
     }
 
     /** Returns the index to use for this property. */
     @Override
     public final int indexOfProperty(final Property<?, ?> prop) {
         final int result = prop.inheritedPropertyId(metaType);
         if (result < 0) {
             throw new IllegalArgumentException("Property " + prop.fullName
                     + " unknown in " + metaType.fullName);
         }
         return result;
     }
 
     /** Marks the specified property as selected */
     @Override
     public final void setSelected(final Property<?, ?> prop) {
         setSelected(indexOfProperty(prop));
     }
 
     /** Marks the specified property as selected */
     @Override
     public final void setSelected(final int index) {
         if (immutable) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         changeCounter++;
         if (index < 64) {
             selected |= (1L << index);
         } else {
             selectedArray[index / 64 - 1] |= (1L << (index % 64));
         }
         // Setting the selected flag also means the content will probably change
         // so we reset the cached state.
         resetCachedState();
     }
 
     /**
      * Marks the specified property at the given index as selected,
      * as well as all the properties that follow.
      */
     @Override
     public final void setSelectedFrom(final int index) {
         if (immutable) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         changeCounter++;
         final int end = getSelectionCount();
         for (int i = index; i < end; i++) {
             if (i < 64) {
                 selected |= (1L << i);
             } else {
                 selectedArray[i / 64 - 1] |= (1L << (i % 64));
             }
         }
         // Setting the selected flag also means the content will probably change
         // so we reset the cached state.
         resetCachedState();
     }
 
     /** Clears all the selected flags */
     @Override
     public final void clearSelection(final boolean alsoChangeCounter,
             final boolean recursively) {
         if (immutable) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         if (isSelected()) {
             selected = 0;
             // It's always safe to set all bits to 0, even the ones we don't use.
             final long[] array = selectedArray;
             final int length = array.length;
             for (int i = 0; i < length; i++) {
                 array[i] = 0;
             }
             // selected has a special meaning, when used with a delegate.
             // This could cause the apparent "content" of the bean to change.
             if (delegate != null) {
                 resetCachedState();
             }
         }
         if (alsoChangeCounter) {
             changeCounter = 0;
         }
         if (recursively) {
             for (final _Bean value : getBeanIterator()) {
                 value.clearSelection(alsoChangeCounter, true);
             }
         }
     }
 
     /** Adds all the selected properties to "selected" */
     @Override
     public final void getSelectedProperty(
             final Collection<Property<?, ?>> selected) {
         selected.clear();
         if (isSelected()) {
             final Property<?, ?>[] props = metaType.inheritedProperties;
             final int propertyCount = props.length;
             for (int i = 0; i < propertyCount; i++) {
                 if (isSelected(i)) {
                     selected.add(props[i]);
                 }
             }
         }
     }
 
     /** Sets all selected flags to true, including the children */
     @Override
     public final void setSelectionRecursive() {
         if (immutable) {
             throw new UnsupportedOperationException(this + " is immutable!");
         }
         selected = -1L;
         final long[] array = selectedArray;
         final int length = array.length;
         for (int i = 0; i < length; i++) {
             array[i] = -1L;
         }
         final int rest = getSelectionCount() % 64;
         if (rest != 0) {
             // If we were to set too many bits to 1, then isSelected() would return the wrong value
             if (length == 0) {
                 selected = (1L << rest) - 1L;
             } else {
                 array[length - 1] = (1L << rest) - 1L;
             }
         }
         for (final _Bean value : getBeanIterator()) {
             value.setSelectionRecursive();
         }
     }
 
     /** Returns the 64 bit hashcode of toString */
     @Override
     public final long getToStringHashCode64() {
         if (toStringHashCode64 == 0) {
             toStringHashCode64 = MurmurHash.hash64(toString());
             if (toStringHashCode64 == 0) {
                 toStringHashCode64 = 1;
             }
         }
         return toStringHashCode64;
     }
 
     /** Returns the 32 bit hashcode */
     @Override
     public final int hashCode() {
         final long value = getToStringHashCode64();
         return (int) (value ^ (value >>> 32));
     }
 
     /** Computes the JSON representation */
     @Override
     public final void toJSON(final Appendable appendable) {
         try {
             final JacksonSerializer j = JacksonSerializer
                     .newSerializer(appendable);
             j.visit(this);
             j.generator.flush();
             j.generator.close();
         } catch (final IOException e) {
             throw new UndeclaredThrowableException(e);
         }
     }
 
     /** Returns the String representation */
     @Override
     public final String toString() {
         if (toString == null) {
             // Use JSON format
             final StringBuilder buf = new StringBuilder(1024);
             toJSON(buf);
             toString = buf.toString();
         }
         return toString;
     }
 
     /** Compares for equality with another object */
     @Override
     public final boolean equals(final Object obj) {
         if ((obj == null) || (obj.getClass() != getClass())) {
             return false;
         }
         if (obj == this) {
             return true;
         }
         final _BeanImpl other = (_BeanImpl) obj;
         if (getToStringHashCode64() != other.getToStringHashCode64()) {
             return false;
         }
         // Inequality here is very unlikely.
         // Since we, currently, build the hashCode64 on the toString text
         // We know it was already computed, and so is a cheap way to compare.
         return toString().equals(other.toString());
     }
 
     /** Returns the delegate */
     @Override
     public final _Bean getDelegate() {
         return delegate;
     }
 
     /** Sets the delegate (can be null); does not clear selection */
     @Override
     public final void setDelegate(final _Bean delegate) {
         setDelegate(delegate, false, false, false);
     }
 
     /** Sets the delegate */
     @Override
     public final void setDelegate(final _Bean delegate,
             final boolean clearSelection, final boolean alsoClearChangeCounter,
             final boolean clearRecursively) {
         if (this.delegate != delegate) {
             if ((delegate != null) && (delegate.getClass() != getClass())) {
                 throw new IllegalArgumentException("Expected type: "
                         + getClass() + " Actual type: " + delegate.getClass());
             }
             _Bean d = delegate;
             while (d != null) {
                 if (d == this) {
                     throw new IllegalArgumentException(
                             "Self-reference not allowed!");
                 }
                 d = d.getDelegate();
             }
             // Does NOT affect "selected state"
             this.delegate = delegate;
             // This could cause the apparent "content" of the bean to change.
             resetCachedState();
             if (delegate == null) {
                 interceptor = DefaultInterceptor.INSTANCE;
             } else {
                 interceptor = WrapperInterceptor.INSTANCE;
             }
         }
         if (clearSelection) {
             clearSelection(alsoClearChangeCounter, clearRecursively);
         }
     }
 
     /** Returns the interceptor */
     @Override
     public final Interceptor getInterceptor() {
         return interceptor;
     }
 
     /** Sets the interceptor; cannot be null */
     @Override
     public final void setInterceptor(final Interceptor interceptor) {
         if (this.interceptor != interceptor) {
             if (interceptor == null) {
                 throw new IllegalArgumentException("interceptor cannot be null");
             }
             // Does NOT affect "selected state"
             this.interceptor = interceptor;
             // This could cause the apparent "content" of the bean to change.
             resetCachedState();
         }
     }
 
     /** Returns the "parent" Bean, if any. */
     @Override
     public final _Bean getParent() {
         return parent;
     }
 
     /** Sets the "parent" Bean, if any. */
     @Override
     public final void setParent(final _Bean parent) {
         if (this instanceof Entity) {
             if (parent != null) {
                 throw new UnsupportedOperationException(getClass().getName()
                         + ": Entities do not have parents");
             }
         }
         this.parent = parent;
     }
 
     /** Returns the "root" Bean, if any. */
     @Override
     public final _Bean getRoot() {
         _Bean result = null;
         // parent is always null for Entities
         if (parent != null) {
             result = parent;
             _Bean p = result.getParent();
             while (p != null) {
                 result = p;
                 p = result.getParent();
             }
         }
         return result;
     }
 
     /** Returns true, if this Bean has the same (non-null) root as the Bean passed as parameter */
     @Override
     public final boolean hasSameRoot(final _Bean other) {
         _Bean root;
         return (other != null)
                 && ((this == other) || ((root = getRoot()) != null)
                         && (root == other.getRoot()));
     }
 
     /** Returns the current value of the change counter */
     @Override
     public final int getChangeCounter() {
         return changeCounter;
     }
 
     /** Sets the current value of the change counter */
     @Override
     public final void setChangeCounter(final int newValue) {
         changeCounter = newValue;
     }
 
     /** Increments the change counter. */
     protected final void incrementChangeCounter() {
         changeCounter++;
     }
 
     /** Copies the content of another instance of the same type. */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private void copyFrom(final _BeanImpl other, final boolean immutably) {
         if (other == null) {
             throw new IllegalArgumentException("other cannot be null");
         }
         if (other.getClass() != getClass()) {
             throw new IllegalArgumentException("Expected type: " + getClass()
                     + " Actual type: " + other.getClass());
         }
         if (other == this) {
             throw new IllegalArgumentException("other cannot be self");
         }
         for (final Property p : metaType.inheritedProperties) {
             if ((p instanceof ObjectProperty) && ((ObjectProperty) p).bean) {
                 final _BeanImpl value = (_BeanImpl) p.getObject(other);
                 if (value != null) {
                     if (immutably) {
                         p.setObject(this, value.doSnapshot());
                     } else {
                         p.setObject(this, value.doCopy());
                     }
                 }
                 // else nothing to do
             } else {
                 p.copyValue(other, this);
             }
         }
     }
 
     /** Make a new instance of the same type as self. */
     private _BeanImpl newInstance() {
         final _BeanImpl result = (_BeanImpl) metaType.constructor.get();
         copyOtherData(result);
         return result;
     }
 
     /** Copy the "non-property" data. */
     protected void copyOtherData(final _BeanImpl result) {
         result.toStringHashCode64 = toStringHashCode64;
         result.toString = toString;
     }
 
     /** Returns a full mutable copy */
     protected final _BeanImpl doCopy() {
         final _BeanImpl result = newInstance();
         result.copyFrom(this, false);
         return result;
     }
 
     /** Returns an immutable copy */
     protected final _BeanImpl doSnapshot() {
         if (immutable) {
             return this;
         }
         final _BeanImpl result = newInstance();
         result.copyFrom(this, true);
         result.makeImmutable();
         return result;
     }
 
     /** Returns a lightweight mutable copy */
     protected final _BeanImpl doWrapper() {
         final _BeanImpl result = newInstance();
         result.setDelegate(this);
         return result;
     }
 
     /** Returns an Iterable<_Bean>, over the property values */
     protected Iterable<_Bean> getBeanIterator() {
         return new SubBeanIterator();
     }
 
     /** Returns the number of possible selections. */
     protected int getSelectionCount() {
         return metaType.inheritedPropertyCount;
     }
 }
