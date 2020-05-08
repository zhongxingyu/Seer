 /*
  * Copyright (C) 2008 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.collection;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.concurrent.BlockingQueue;
 
 import org.trancecode.api.ReturnsNullable;
 import org.trancecode.function.TcFunctions;
 
 /**
  * Utility methods related to {@link Iterable}.
  * 
  * @author Herve Quiroz
  */
 public final class TcIterables
 {
     private TcIterables()
     {
         // No instantiation
     }
 
     /**
      * Get the last argument from a sequence, or <code>null</code> if there is
      * no such last element.
      */
     @ReturnsNullable
     public static <T> T getLast(final Iterable<T> elements)
     {
         return getLast(elements, null);
     }
 
     /**
      * Get the last argument from a sequence, or <code>defaultElement</code> if
      * there is no such last element.
      */
     @ReturnsNullable
     public static <T> T getLast(final Iterable<T> elements, final T defaultElement)
     {
         try
         {
             return Iterables.getLast(elements);
         }
         catch (final NoSuchElementException e)
         {
             return defaultElement;
         }
     }
 
     public static <T> T getFirst(final Iterable<T> elements)
     {
         return TcIterators.getFirst(elements.iterator());
     }
 
     public static <T> T getFirst(final Iterable<T> elements, final T defaultElement)
     {
         return TcIterators.getFirst(elements.iterator(), defaultElement);
     }
 
     public static <T> Iterable<T> append(final Iterable<T> iterable, final T element)
     {
         return Iterables.concat(iterable, ImmutableList.of(element));
     }
 
     public static <T> Iterable<T> append(final Iterable<T> iterable, final T... elements)
     {
         return Iterables.concat(iterable, ImmutableList.copyOf(elements));
     }
 
     public static <T> Iterable<T> prepend(final Iterable<T> iterable, final T... elements)
     {
         return Iterables.concat(ImmutableList.copyOf(elements), iterable);
     }
 
     /**
      * Compute a sequence of results by applying each function form the list to
      * the same argument.
      */
     public static <F, T> Iterable<T> applyFunctions(final Iterable<Function<F, T>> functions, final F argument)
     {
         final Function<Function<F, T>, T> applyFunction = TcFunctions.applyTo(argument);
         return Iterables.transform(functions, applyFunction);
     }
 
     public static <T> Iterable<T> getDescendants(final Iterable<T> parentElements,
             final Function<T, Iterable<T>> getChildFunction)
     {
         if (Iterables.isEmpty(parentElements))
         {
             return parentElements;
         }
 
         final Iterable<T> children = Iterables.concat(Iterables.transform(parentElements, getChildFunction));
 
         return Iterables.concat(parentElements, getDescendants(children, getChildFunction));
     }
 
     public static <T> Iterable<T> getDescendants(final T parentElement, final Function<T, Iterable<T>> getChildFunction)
     {
         return getDescendants(ImmutableList.of(parentElement), getChildFunction);
     }
 
     public static boolean removeAll(final Iterable<?> iterable)
     {
         boolean removed = false;
         for (final Iterator<?> iterator = iterable.iterator(); iterator.hasNext();)
         {
             iterator.next();
             iterator.remove();
             removed = true;
         }
 
         return removed;
     }
 
     /**
      * @see TcIterators#concurrentModifiable(List)
      */
     public static <T> Iterable<T> concurrentModifiable(final List<T> sequence)
     {
         return new Iterable<T>()
         {
             @Override
             public Iterator<T> iterator()
             {
                 return TcIterators.concurrentModifiable(sequence);
             }
         };
     }
 
     /**
      * Returns the elements from the passed sequence up to the first element
      * that matches the passed predicate (this element is excluded from the
      * result sequence).
      * <p>
      * Returned {@link Iterable} will throw {@link NoSuchElementException} if
      * the predicate does not match for any element from the sequence.
      */
     public static <T> Iterable<T> until(final Iterable<T> elements, final Predicate<T> predicate)
     {
         Preconditions.checkNotNull(elements);
         Preconditions.checkNotNull(predicate);
 
         return new Iterable<T>()
         {
             @Override
             public Iterator<T> iterator()
             {
                 return TcIterators.until(elements.iterator(), predicate);
             }
         };
     }
 
     /**
      * Returns an {@link Iterable} that contains the elements from the queue.
      * 
      * @see TcIterators#removeAll(BlockingQueue)
      */
     public static <T> Iterable<T> removeAll(final BlockingQueue<T> fromQueue)
     {
         return new Iterable<T>()
         {
             @Override
             public Iterator<T> iterator()
             {
                 return TcIterators.removeAll(fromQueue);
             }
         };
     }
 
     /**
      * Returns the first non-null element from the specified sequence, or the
      * specified default value if all elements from the sequence were
      * {@code null}.
      */
     public static <T> T getFirstNonNull(final Iterable<T> elements, final T defaultValue)
     {
         return Iterables.getFirst(Iterables.filter(elements, Predicates.notNull()), defaultValue);
     }
 
     /**
     * Returns the first non-null element from the specified sequence, or the
     * specified default value if all elements from the sequence were
     * {@code null}.
      */
     public static <T> T getFirstNonNull(final T... elements)
     {
         return getFirstNonNull(ImmutableList.copyOf(elements), null);
     }
 }
