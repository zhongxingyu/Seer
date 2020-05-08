 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.list;
 
 import static org.ref_send.promise.Fulfilled.detach;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import org.joe_e.Equatable;
 import org.ref_send.promise.Fulfilled;
 
 /**
  * A linked list.
  * @param <T> element type
  */
 public final class
 List<T> implements Iterable<T>, Serializable {
     static private final long serialVersionUID = 1L;
 
     static private final class
     Link<T> implements Equatable, Serializable {
         static private final long serialVersionUID = 1L;
 
         Fulfilled<Link<T>> next;
         T value;
     }
 
     /**
      * first element link
      */
     private Fulfilled<Link<T>> first;
 
     /**
      * first unused link
      */
     private Link<T> last;
 
     /**
      * link count
      */
     private int capacity;
 
     /**
      * element count
      */
     private int size;
 
     private
     List() {
         last = new Link<T>();
         last.next = detach(last);
         first = last.next;
         capacity = 1;
         size = 0;
     }
 
     /**
      * Constructs a list.
      * @param <T> element type
     * @param values    each initial value
      */
     static public <T> List<T>
    list(final T... values) {
        final List<T> r = new List<T>();
        for (final T value : values) { r.append(value); }
        return r;
    }
 
     // java.lang.Iterable interface
 
     /**
      * Iterates over the values in this list.
      * @return forward iterator over this list
      */
     public final Iterator<T>
     iterator() { return new IteratorX(first.cast()); }
 
     private final class
     IteratorX implements Iterator<T>, Serializable {
         static private final long serialVersionUID = 1L;
 
         private Link<T> current;
         
         IteratorX(final Link<T> current) {
             this.current = current;
         }
 
         public boolean
         hasNext() { return current != last; }
 
         public T
         next() {
             if (current == last) { throw new NoSuchElementException(); }
             final T r = current.value;
             current = current.next.cast();
             return r;
         }
 
         public void
         remove() { throw new UnsupportedOperationException(); }
     }
 
     // org.ref_send.list.List interface
 
     /**
      * Is the element count zero?
      */
     public boolean
     isEmpty() { return 0 == size; }
 
     /**
      * Gets the element count.
      */
     public int
     getSize() { return size; }
 
     /**
      * Gets the front value.
      * @return front value
      * @throws NullPointerException list is empty
      */
     public T
     getFront() throws NullPointerException {
         if (0 == size) { throw new NullPointerException(); }
         return first.cast().value;
     }
 
     /**
      * Removes the front element.
      * @return removed value
      * @throws NullPointerException list is empty
      */
     public T
     pop() throws NullPointerException {
         if (0 == size) { throw new NullPointerException(); }
         final Link<T> x = first.cast();
         final T r = x.value;
         x.value = null;
         first = x.next;
         size -= 1;
         return r;
     }
 
     /**
      * Appends a value.
      * @param value value to append
      */
     public void
     append(final T value) {
         last.value = value;
         size += 1;
         if (capacity == size) {
             final Link<T> spare = new Link<T>();
             spare.next = last.next;
             last.next = detach(spare);
             capacity += 1;
         }
         last = last.next.cast();
     }
 }
