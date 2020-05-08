 /**
  *  Copyright (c) 2011, The Staccato-Commons Team
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation; version 3 of the License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  */
 
 package net.sf.staccatocommons.collections;
 
 import java.util.AbstractList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.RandomAccess;
 
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.MinSize;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 import net.sf.staccatocommons.restrictions.check.NotEmpty;
 import net.sf.staccatocommons.restrictions.value.Unmodifiable;
 
 import org.apache.commons.lang.ObjectUtils;
 
 /**
  * Class methods for dealing with {@link List}
  * 
  * @author flbulgarelli
  */
 public class Lists {
 
   /**
    * Inserts the given element after the reference. Throws
    * {@link NoSuchElementException} if the list does not contain the reference
    * 
    * @param <A>
    *          the list type
    * @param list
    *          the list
    * @param element
    *          the element to be inserted just afeter the reference element
    * @param reference
    *          the reference. The list must contain it
    */
   public static <A> void addAfter(@NonNull List<A> list, A element, A reference) {
     for (ListIterator<A> iter = list.listIterator(); iter.hasNext();)
       if (ObjectUtils.equals(iter.next(), reference)) {
         iter.add(element);
         return;
       }
     throw new NoSuchElementException(reference.toString());
   }
 
   /**
    * Inserts the given element before the reference. Throws
    * {@link NoSuchElementException} if the list does not contain the reference
    * 
    * @param <A>
    *          the list type
    * @param list
    *          the list
    * @param element
    *          the element to be inserted just before the reference element
    * @param reference
    *          the reference. The list must contain it
    */
   public static <A> void addBefore(@NonNull List<A> list, A element, A reference) {
     for (ListIterator<A> iter = list.listIterator(); iter.hasNext();)
       if (iter.next().equals(reference)) {
         iter.previous();
         iter.add(element);
         return;
       }
     throw new NoSuchElementException(reference.toString());
   }
 
   /**
    * Removes the list last element
    * 
    * @param <A>
    * @param list
    * @return the element removed
    */
   public static <A> A removeLast(@NonNull List<A> list) {
     return list.remove(list.size() - 1);
   }
 
   /**
    * Retrieves the list first element (at position 0)
    * 
    * @param <A>
    *          the list elements type
    * @param list
    * @return the element at position 1 in the list
    * @throws IllegalArgumentException
    *           if list is empty if list is empty is out of range
    * @see List#get(int)
    */
   public static <A> A first(@NotEmpty List<A> list) {
     return list.get(0);
   }
 
   /**
    * Retrieves the list second element (at position 1)
    * 
    * @param <A>
    *          the list elements type
    * @param list
    * @return the element at position 1 in the list
    * @throws IndexOutOfBoundsException
    *           if index is out of range
    * @see List#get(int)
    */
   public static <A> A second(@MinSize(2) List<A> list) {
     return list.get(1);
   }
 
   /**
    * Retrieves the list third element (at position 2)
    * 
    * @param <A>
    *          the list elements type
    * @param list
    * @return the element at position 2 in the list
    * @throws IndexOutOfBoundsException
    *           if index is out of range
    * @see List#get(int)
    */
   public static <A> A third(@MinSize(3) List<A> list) {
     return list.get(2);
   }
 
   /**
    * Retrieves the last element (at position size - 1)
    * 
    * @param <A>
    *          the list elements type
    * @param list
    * @return the element at position size - 1 in the list
    * @throws IndexOutOfBoundsException
    *           if index is out of range
    * @see List#get(int)
    */
   public static <A> A last(@NotEmpty List<A> list) {
     return list.get(list.size() - 1);
   }
 
   /**
    * Answers the List class, but preserving its element generic type. This
    * method is mostly aimed to be used with Staccato-Commons-Lambda
    * 
    * @param <A>
    * @return (Class&lt;List&lt;A&gt;&gt;) List
    */
   @Constant
   public static <A> Class<List<A>> type() {
     return (Class) List.class;
   }
 
   /**
    * Answers a new {@link Unmodifiable} list with the given elements.
    * 
    * This method is not equivalent to {@link Arrays#asList(Object...)}, which
    * answers mutable lists
    * 
    * @param <A>
    * @param elements
    * @return an unmodifiable list
    */
   public static <A> List<A> from(A... elements) {
     return new UnmodifiableArrayList<A>(elements);
   }
 
   /** Based on Arrays#ArrayList */
   private static class UnmodifiableArrayList<A> extends AbstractList<A> implements RandomAccess, java.io.Serializable {
     private static final long serialVersionUID = -5513852306359377448L;
     private final A[] a;
 
     UnmodifiableArrayList(A[] array) {
       if (array == null)
         throw new NullPointerException();
       a = array;
     }
 
     public int size() {
       return a.length;
     }
 
    @SuppressWarnings("cast")
     public Object[] toArray() {
      return (Object[]) a.clone();
     }
 
     public <T> T[] toArray(T[] a) {
       int size = size();
       if (a.length < size)
         return Arrays.copyOf(this.a, size, (Class<? extends T[]>) a.getClass());
       System.arraycopy(this.a, 0, a, 0, size);
       if (a.length > size)
         a[size] = null;
       return a;
     }
 
     public A get(int index) {
       return a[index];
     }
 
     public int indexOf(Object o) {
       if (o == null) {
         for (int i = 0; i < a.length; i++)
           if (a[i] == null)
             return i;
       } else {
         for (int i = 0; i < a.length; i++)
           if (o.equals(a[i]))
             return i;
       }
       return -1;
     }
 
     public boolean contains(Object o) {
       return indexOf(o) != -1;
     }
   }
 
 }
