 /**
  *  Copyright (c) 2010-2012, The StaccatoCommons Team
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
 
 package net.sf.staccatocommons.collections.stream.internal.algorithms;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.staccatocommons.collections.iterable.Iterables;
 import net.sf.staccatocommons.collections.stream.AbstractStream;
 import net.sf.staccatocommons.collections.stream.Stream;
 import net.sf.staccatocommons.iterators.thriter.Thriterator;
 import net.sf.staccatocommons.iterators.thriter.Thriterators;
 
 /**
  * @author flbulgarelli
  * 
  */
 public final class SortedStream<A> extends AbstractStream<A> {
 
   private final Stream<A> source;
 
   private final Comparator<? super A> comparator;
 
   /**
    * Creates a new {@link SortedStream}
    */
   public SortedStream(Stream<A> source, Comparator<A> comparator) {
     this.source = source;
     this.comparator = comparator;
   }
 
   @Override
   public Thriterator<A> iterator() {
     return Thriterators.from(toList().iterator());
   }
 
   @Override
   public List<A> toList() {
     return Iterables.toSortedList(getSource(), comparator);
   }
 
   @Override
   public Set<A> toSet() {
     return Iterables.toSortedSet(getSource(), comparator);
   }
 
   @Override
  public A head() {
    return source.minimumBy(comparator);
   }
 
   @Override
   public A last() {
    return source.maximumBy(comparator);
   }
 
   public boolean isEmpty() {
     return source.isEmpty();
   }
 
   /**
    * @return the source
    */
   private Stream<A> getSource() {
     return source;
   }
 }
