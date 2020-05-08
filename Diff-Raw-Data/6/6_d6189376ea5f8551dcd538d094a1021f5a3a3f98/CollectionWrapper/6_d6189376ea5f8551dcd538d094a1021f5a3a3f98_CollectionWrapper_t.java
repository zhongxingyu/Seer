 /*
  * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
  * associated organization.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use, copy, modify,
  * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  */
 
 package com.agileapes.couteau.basics.collections;
 
 import com.agileapes.couteau.basics.api.Filter;
 import com.agileapes.couteau.basics.api.Processor;
 import com.agileapes.couteau.basics.api.Transformer;
 
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * The collection wrapper class is designed to wrap a collection and perform fluently designated operations on it.
  * It will make chained operations on the collection so much easier.
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (6/27/13, 2:57 PM)
  */
 public class CollectionWrapper<I> {
 
     /**
      * This method -- specially when statically imported -- will increase readability.
      * @param items    the array of items to be wrapped
      * @param <I>      the type of items in the collection
      * @return the wrapped to the collection
      */
     public static <I> CollectionWrapper<I> with(I... items) {
         return with(Arrays.asList(items));
     }
 
     /**
      * This method -- specially when statically imported -- will increase readability.
      * @param items    the collection to be wrapped
      * @param <I>      the type of items in the collection
      * @return the wrapped to the collection
      */
     public static <I> CollectionWrapper<I> with(Collection<I> items) {
         return new CollectionWrapper<I>(items);
     }
     
     private final List<I> items;
 
     private CollectionWrapper(Collection<I> items) {
         this.items = new ArrayList<I>(items);
     }
 
     /**
      * Will sort all "comparable" items in the wrapped collection
      * @return the wrapper for sorted items
      */
     public CollectionWrapper<I> sort() {
         return sort(new Comparator<I>() {
             @Override
             public int compare(I o1, I o2) {
                 if (o1 instanceof Comparable && o2 instanceof Comparable) {
                     //noinspection unchecked
                     return ((Comparable) o1).compareTo(o2);
                 }
                 return 0;
             }
         });
     }
 
     /**
      * Sorts all the items in the wrapped collection based on the given comparator
      * @param comparator    the comparator for wrapped items
      * @return the wrapper for sorted items
      */
     public CollectionWrapper<I> sort(Comparator<? super I> comparator) {
         final ArrayList<I> sorted = new ArrayList<I>(items);
         Collections.sort(sorted, comparator);
         return new CollectionWrapper<I>(sorted);
     }
 
     /**
      * Will work the processor over each of the items in the collection
      * @param processor    the processor
      * @return the wrapper again
      */
     public CollectionWrapper<I> each(Processor<? super I> processor) {
         for (I item : items) {
             processor.process(item);
         }
         return this;
     }
 
     /**
      * Will return a <em>new</em> wrapper for all items acceptable by the keep
      * @param filters    The filters that decide whether or not this item will be kept.
      *                   If any one filter accepts the item, the item will be kept.
      * @return the wrapper for all accepted items
      */
     public CollectionWrapper<I> keep(Filter<? super I>... filters) {
         final ArrayList<I> filtered = new ArrayList<I>();
         for (I item : items) {
             boolean kept = false;
             for (Filter<? super I> filter : filters) {
                 if (filter.accepts(item)) {
                     kept = true;
                     break;
                 }
             }
             if (kept) {
                 filtered.add(item);
             }
         }
         return new CollectionWrapper<I>(filtered);
     }
 
     /**
      * Will return a <em>new</em> wrapper for all items not acceptable by the keep
      * @param filters    the filters that decide whether or not the item will be dropped.
      *                   If any single filter accepts the item, it will drop.
      * @return the wrapper for all accepted items
      */
     public CollectionWrapper<I> drop(Filter<? super I>... filters) {
         final ArrayList<I> filtered = new ArrayList<I>();
         for (I item : items) {
             boolean dropped = false;
             for (Filter<? super I> filter : filters) {
                 if (filter.accepts(item)) {
                     dropped = true;
                     break;
                 }
             }
             if (!dropped) {
                 filtered.add(item);
             }
         }
         return new CollectionWrapper<I>(filtered);
     }
 
     /**
      * Will return a wrapper for a collection of items that are produced through the mapping of the currently wrapped items
      * @param transformer    the transformer
      * @param <O>       the type of output objects
      * @return the <em>newly instantiated</em> wrapper for the transformer's output
      */
     public <O> CollectionWrapper<O> transform(Transformer<? super I, O> transformer) {
        final List<O> result = new ArrayList<O>();
        for (I item : items) {
            result.add(transformer.map(item));
        }
        return new CollectionWrapper<O>(result);
     }
 
     public <O> Map<I, O> map(Transformer<? super I, O> transformer) {
         final HashMap<I, O> map = new HashMap<I, O>();
         for (I item : items) {
             map.put(item, transformer.map(item));
         }
         return map;
     }
 
     /**
      * Will produce a collection of collections for items in this wrapper. This is helpful when partitioning
      * items in the wrapper
      * @param expander    the expander
      * @return the wrapper
      */
     public CollectionWrapper<Collection<I>> expand(Transformer<Collection<I>, Collection<Collection<I>>> expander) {
         return new CollectionWrapper<Collection<I>>(expander.map(new ArrayList<I>(items)));
     }
 
     /**
      * Will transform all the items in the collection into instances of the wrapped items. This is useful when reducing
      * certain items in the collection
      * @param transformer    the transformer
      * @return the wrapper
      */
     public CollectionWrapper<I> all(Transformer<Collection<I>, Collection<I>> transformer) {
         return new CollectionWrapper<I>(transformer.map(new ArrayList<I>(items)));
     }
 
     /**
      * @return a thread-safe version of the items in the wrapper. Modifying this collection does not affect the wrapper.
      */
     public List<I> concurrentList() {
         return new CopyOnWriteArrayList<I>(items);
     }
 
     /**
      * @return a list of items in the wrapper. Modifying this collection does not affect the wrapper.
      */
     public List<I> list() {
         return new ArrayList<I>(items);
     }
 
     /**
      * @return the number of items currently wrapped
      */
     public int count() {
         return items.size();
     }
 
     /**
      * @return {@code true} if this wrapper is currently empty
      */
     public boolean isEmpty() {
         return items.isEmpty();
     }
 
     /**
      * @return the first item in the wrapper or {@code null} if it is empty
      */
     public I first() {
         if (isEmpty()) {
             return null;
         }
         return items.get(0);
     }
 
     /**
      * @return a wrapped version of the collection, save the very first item
      */
     public CollectionWrapper<I> rest() {
         return new CollectionWrapper<I>(items.subList(1, items.size()));
     }
 
     /**
      * @return the last item in the wrapper or {@code null} if it is empty
      */
     public I last() {
         if (isEmpty()) {
             return null;
         }
         return items.get(items.size() - 1);
     }
 
     /**
      * @return the set representing all items in the wrapper, minus duplicates
      */
     public Set<I> set() {
         return new HashSet<I>(items);
     }
 
     /**
      * Returns a wrapper for the collection items in this wrapper plus the
      * newly added items
      * @param items    the items to be added
      * @return the new wrapper
      */
     public CollectionWrapper<I> add(I... items) {
         return add(Arrays.asList(items));
     }
 
     /**
      * Returns a wrapper for the collection items in this wrapper plus the
      * newly added items
      * @param items    the items to be added
      * @return the new wrapper
      */
     public CollectionWrapper<I> add(Collection<I> items) {
         final ArrayList<I> list = new ArrayList<I>(this.items);
         list.addAll(items);
         return new CollectionWrapper<I>(list);
     }
 
     /**
      * Checks whether the given item exists in the wrapped
      * collection
      * @param filter    the picking filter
      * @return {@code true} if anb item is chosen by the filter
      */
     public boolean exists(Filter<? super I> filter) {
         for (I item : items) {
             if (filter.accepts(item)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if a condition would hold over the entire collection
      * @param filter    the filter encapsulating the predicate
      * @return {@code true} or {@code false} based on the examination performed
      */
     public boolean forAll(Filter<? super I> filter) {
         for (I item : items) {
             if (!filter.accepts(item)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Partitions the collection into two sub-collections, the first being those accepted
      * by the filter and the second being those that were rejected.
      * @param filter    the filter performing the partitioning
      * @return the partitioned collection
      */
     public CollectionWrapper<Collection<I>> partition(Filter<? super I> filter) {
         final List<I> accepted = new ArrayList<I>();
         final List<I> rejected = new ArrayList<I>();
         for (I item : items) {
             if (filter.accepts(item)) {
                 accepted.add(item);
             } else {
                 rejected.add(item);
             }
         }
         final ArrayList<Collection<I>> list = new ArrayList<Collection<I>>();
         list.add(accepted);
         list.add(rejected);
         return new CollectionWrapper<Collection<I>>(list);
     }
 
     /**
      * Keeps all items prior to the first item failing the criteria
      * @param filter    the filter
      * @return the matching items
      */
     public CollectionWrapper<I> keepWhile(Filter<? super I> filter) {
         final ArrayList<I> list = new ArrayList<I>();
         for (I item : items) {
             if (!filter.accepts(item)) {
                 break;
             }
             list.add(item);
         }
         return new CollectionWrapper<I>(list);
     }
 
     /**
      * Drops all the items in the beginning of the list that match the criteria
      * without any gaps
      * @param filter    the criteria
      * @return the matching items
      */
     public CollectionWrapper<I> dropWhile(Filter<? super I> filter) {
         final ArrayList<I> list = new ArrayList<I>();
         boolean found = false;
         for (I item : items) {
             if (!found && filter.accepts(item)) {
                 continue;
             }
             found = true;
             list.add(item);
         }
         return new CollectionWrapper<I>(list);
     }
 
     /**
      * Takes the indicates sublist of the underlying collection. Negative indexing means the number of items from
      * the end of the list
      * @param from    the index from which the items should be included.
      * @param to      the index to which the items should be included (inclusive).
      * @return the matching sublist
      */
     public CollectionWrapper<I> take(int from, int to) {
         if (from < 0) {
             from = count() + from;
         }
         if (to < 0) {
             to = count() + to;
         }
         final ArrayList<I> list = new ArrayList<I>();
         if (from <= to && from < count() && to < count()) {
             for (int i = from; i <= to; i ++) {
                 list.add(items.get(i));
             }
         }
         return new CollectionWrapper<I>(list);
     }
 
     /**
      * Takes the indicated items from the beginning or the end of the list, depending on whether or not
      * the offset is non-negative.
      * @param offset    the offset.
      * @return the selected sublist
      */
     public CollectionWrapper<I> take(int offset) {
         final ArrayList<I> list = new ArrayList<I>();
         if (offset > 0) {
             if (offset < count()) {
                 for (int i = 0; i <= offset; i ++) {
                     list.add(items.get(i));
                 }
             }
         } else {
             offset = count() + offset;
             for (int i = offset; i < count(); i ++) {
                 list.add(items.get(i));
             }
         }
         return new CollectionWrapper<I>(list);
     }
 
     /**
      * Counts the number of items matching the given criteria
      * @param filter    the filter picking matches
      * @return number of matching items
      */
     public int count(Filter<? super I> filter) {
         int count = 0;
         for (I item : items) {
             count += filter.accepts(item) ? 1 : 0;
         }
         return count;
     }
 
     /**
      * Finds the item matched by the filter
      * @param filter    the filter
      * @return the matched item or {@code null} if none are found
      */
     public I find(Filter<? super I> filter) {
         for (I item : items) {
             if (filter.accepts(item)) {
                 return item;
             }
         }
         return null;
     }
 
     /**
      * Returns the index for the given item
      * @param item    the item to look for
      * @return the index
      */
     public int indexOf(I item) {
         for (int i = 0; i < items.size(); i++) {
             if (item.equals(items.get(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the index of the last item in the least matching the given item
      * @param item    the item to look for
      * @return the index
      */
     public int lastIndexOf(I item) {
         for (int i = items.size() - 1; i >= 0; i--) {
             if (item.equals(items.get(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns index wherein the item matching the criteria can be found
      * @param filter    the criteria
      * @return the index
      */
     public int indexWhere(Filter<? super I> filter) {
         for (int i = 0; i < items.size(); i++) {
             if (filter.accepts(items.get(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the last index wherein the item matching the criteria can be found
      * @param filter    the criteria
      * @return the index
      */
     public int lastIndexWhere(Filter<? super I> filter) {
         for (int i = items.size() - 1; i >= 0; i--) {
             if (filter.accepts(items.get(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the sum of all items in the list if they are all numeric
      * @return the sum total
      */
     public double sum() {
         double sum = 0;
         for (I item : items) {
             if (item instanceof Number) {
                 Number number = (Number) item;
                 sum += Double.valueOf(number.toString());
             } else {
                 throw new UnsupportedOperationException();
             }
         }
         return sum;
     }
 
     /**
      * Returns the item residing at the specified position. Negative index counts from the end of the list.
      * @param index    the index leading to the item
      * @return the actual item
      */
     public I get(int index) {
         if (index < 0) {
             index = count() + index;
         }
         return items.get(index);
     }
 
     /**
      * Runs the processor over the items matching the filter
      * @param filter       the selector
      * @param processor    the processor
      * @return the items in the wrapper
      */
     public CollectionWrapper<I> forThose(final Filter<? super I> filter, final Processor<? super I> processor) {
         return each(new Processor<I>() {
             @Override
             public void process(I input) {
                 if (!filter.accepts(input)) {
                     return;
                 }
                 processor.process(input);
             }
         });
     }
 
     /**
      * Joins the elements in the underlying collection using the given delimiter
      * @return the joined array of items as a string
      */
     public String join(String delimiter) {
         final StringBuilder builder = new StringBuilder();
         for (int i = 0; i < items.size(); i++) {
             I item = items.get(i);
             if (i > 0) {
                 builder.append(delimiter);
             }
             builder.append(item);
         }
         return builder.toString();
     }
 
 }
