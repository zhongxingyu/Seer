 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright Ⓒ 2012  Mattias Andrée, Peyman Eshtiagh,
  *                   Calle Lejdbrandt, Magnus Lundberg
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.util;
 
 import java.util.*;
 
 
 /**
  * Simple iterator and enumerator for {@link LinkedList}
  * 
  * @author  Peyman Eshtiagh
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class ListEnumerator<T> implements Enumeration<T>, Iterator<T>
 {
     /**
      * Constructor
      * 
      * @param  list  The enumerated list
      */
     public ListEnumerator(final CDLinkedList<T> list)
     {
 	this.list = list;
 	this.cursor = this.start = list.head;
     }
     
     
     
     /**
      * The enumerated list
      */
     private final CDLinkedList<T> list;
     
     /**
      * The current node
      */
     private ListNode<T> cursor;
     
     /**
      * The first node
      */
     private ListNode<T> start;
     
     /**
      * Whether the first invokation of {@link #nextElement()} has been made
      */
     private boolean first = false;
     
     
     
     /**
      * {@inheritDoc}
      */
     public boolean hasMoreElements()
     {
 	return (cursor != null) && (first || (cursor != start));
     }
     
     
     /**
      * {@inheritDoc}
      */
     public boolean hasNext()
     {
 	return hasMoreElements();
     }
     
     
     /**
      * {@inheritDoc}
      */
     public T nextElement()
     {
 	if (this.cursor == null)
 	    throw new NoSuchElementException();
 	
 	this.first = false;
 	
 	final T item = cursor.item;
 	cursor = cursor.next;
 	return item;
     }
     
     
     /**
      * {@inheritDoc}
      */
     public T next()
     {
 	return nextElement();
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void remove()
     {
	final ListNode<T> next = cursor.next;
 	this.list.remove(cursor);
 	
 	cursor = (next == cursor) ? null : next;
     }
     
 }
 
