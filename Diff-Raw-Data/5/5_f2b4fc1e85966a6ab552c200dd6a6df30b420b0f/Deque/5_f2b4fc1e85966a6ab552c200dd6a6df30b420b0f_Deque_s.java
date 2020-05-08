 import java.util.Iterator;
 public class Deque<Item> implements Iterable<Item> { 
     
     private class DequeNode
     {
         public Item item;
         public DequeNode prev;
         public DequeNode next;
     }
     
     private DequeNode first;
     private DequeNode last;
     private int size;
     
     // construct an empty deque
     public Deque() {
         size = 0;
         first = null;
         last = null;
         
     }
     
     // is the deque empty?
     public boolean isEmpty() {
         return size == 0;
         
     }
     
     // return the number of items on the deque
     public int size() {
         return size;
     }
     
     // insert the item at the front
     public void addFirst(Item item) {
         if (item == null)
             throw new java.lang.NullPointerException();        
         if (first == null) {
             first = new DequeNode();
             first.item = item;
             first.next = null;
             first.prev = null;
             last = first;
         }
         else {
             DequeNode node = new DequeNode();
             node.item = item;
             node.prev = null;
             node.next = first;
             first.prev = node;
             first = node;
             
         }
         
         size++;
     }
     
     // insert the item at the end
     public void addLast(Item item) {
         if (item == null)
             throw new java.lang.NullPointerException();
         if (last == null) {
             last = new DequeNode();
             last.item = item;
             last.prev = null;
             last.next = null;
             first = last;
             
         } 
         else {
             DequeNode node = new DequeNode();
             node.item = item;
             node.prev = last;
             node.next = null;
             last.next = node;
             last = node;
         }
         
         size++;
         
     }
     
     // delete and return the item at the front 
     public Item removeFirst()  {
         if (isEmpty())
             throw new java.util.NoSuchElementException();
         Item item = first.item;
         first.item = null;
         if (size == 1) {
             first = null;
             last = null;
         }
         else {
             first = first.next;
             first.prev = null;
         }
         size--;
         
         return item;
         
     }
     
     // delete and return the item at the end
     public Item removeLast() {
         if (isEmpty())
            return null;
         Item item = last.item;
         last.item = null;
         if (size == 1) {
             first = null;
             last = null;
         }
         else {
             last = last.prev;
             last.next = null;
         }
         size--;
         
         return item;
     }
     
     // return an iterator over items in order from front to end
     public Iterator<Item> iterator() {
         return new DequeIterator();
     }
     
     private class DequeIterator implements Iterator<Item> {
         private DequeNode current = first;
         public boolean hasNext() {
            return (current.next != null);
         }
         public void remove() {
             throw new java.lang.UnsupportedOperationException();
         }
         public Item next()
         {
             if (current == null)
                 throw new java.util.NoSuchElementException();
             Item item = current.item;
             current = current.next;
             return item;
         }
         
     }
 }
