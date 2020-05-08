 package container.ordered;
 
 import java.util.NoSuchElementException;
 
/**
 * Michael Dorst
 * CISP 430 Data Structures
 * M Dixon
 */
 public class ListIterator<E extends Comparable<E>> implements java.util.ListIterator<E> {
   
   private LinkedList<E>.Node<E> lastReturned;
   private LinkedList<E>.Node<E> next;
   private LinkedList<E>.Node<E> previous;
   
   @Override
   public boolean hasNext() {
     //TODO: write test for hasNext
     return next != null;
   }
   
   public boolean hasPrevious() {
     //TODO: write test for hasPrevious
     return previous != null;
   }
   
   @Override
   public E next() {
     //TODO: write test for next
     if (!hasNext()) {
       throw new NoSuchElementException();
     }
     previous = next;
     next = next.next;
     return (lastReturned = previous).value;
   }
   
   public E previous() {
     //TODO: write test for previous
     if (!hasPrevious()) {
       throw new NoSuchElementException();
     }
     next = previous;
     previous = previous.previous;
     return (lastReturned = next).value;
   }
   
   @Override
   public int nextIndex() {
     //TODO: add test for nextIndex
     //TODO: implement nextIndex
     return 0;
   }
   
   @Override
   public int previousIndex() {
     //TODO: add test for previousIndex
     //TODO: implement previousIndex
     return 0;
   }
   
   @Override
   public void remove() {
     //TODO: add test for remove
     if (lastReturned == null) {
       throw new IllegalStateException();
     } else {
       lastReturned.previous.next = lastReturned.next;
       lastReturned.next.previous = lastReturned.previous;
       lastReturned = null;
     }
   }
   
   @Override
   public void set(E e) {
     //TODO: add test for set
     //TODO: implement set
   }
   
   @Override
   public void add(E e) {
     //TODO: add test for add
     //TODO: implement add
   }
   
   ListIterator(LinkedList<E>.Node<E> n) {
     next = n;
   }
 }
