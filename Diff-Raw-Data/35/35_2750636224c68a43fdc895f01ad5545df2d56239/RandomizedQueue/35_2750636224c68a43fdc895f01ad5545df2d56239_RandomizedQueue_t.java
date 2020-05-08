 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class RandomizedQueue<Item> implements Iterable<Item> {
 
     private int size = 0;
     private Node<Item> first;
     private Node<Item> last;
 
     private Object[] indexes;
 
     public RandomizedQueue() {
 
         indexes = new Object[2];
     }
 
     public boolean isEmpty() {
 
         return size == 0;
     }
 
     public int size() {
 
         return size;
     }
 
     public void enqueue(Item item) {
 
         if (item == null) {
             throw new NullPointerException("Can't add a null item.");
         }
         assert item != null;
 
         Node<Item> node = getNode(item);
         if (size == indexes.length) {
 
             resizeIndexes(indexes.length * 2);
         }
         indexes[size] = node;
         if (size != 0) {
 
             Node<Item> prevLast = last;
             last = node;
             prevLast.setNext(last);
             last.setPrev(prevLast);
         } else {
 
             first = node;
             last = node;
         }
 
         size++;
     }
    
     private void resizeIndexes(int newSize) {
 
         Object[] newArray = new Object[newSize];
 
         for (int i = 0; i < size; i++) {
 
             newArray[i] = indexes[i];
         }
 
         indexes = newArray;
     }
 
     public Item sample() {
 
         if (size == 0) {
             throw new NoSuchElementException("The Queue is empty");
         }
         
         int index = StdRandom.uniform(size);
 
         Node<Item> nodeToReturn = (Node<Item>) indexes[index];
         return nodeToReturn.getItem();
     }
 
     public Item dequeue() {
 
         if (size == 0) {
             throw new NoSuchElementException("The Queue is empty");
         }
 
         int index = StdRandom.uniform(size);
 
         Node<Item> nodeToReturn = null;
         nodeToReturn = (Node<Item>) indexes[index];
         if (index == 0) {
 
             if (first == last) {
                 first = null;
                 last = null;
             } else {
 
                 first = nodeToReturn.next;
                 nodeToReturn.setNext(null);
                 first.setPrev(null);
             }
         } else if (index == size - 1) {
 
             if (first == last) {
                 first = null;
                 last = null;
             } else {
 
                 last = nodeToReturn.prev;
                 nodeToReturn.setPrev(null);
                 last.setNext(null);
             }
         } else {
 
             nodeToReturn.getPrev().setNext(nodeToReturn.getNext());
             nodeToReturn.getNext().setPrev(nodeToReturn.getPrev());
             nodeToReturn.setNext(null);
             nodeToReturn.setPrev(null);
         }
 
         readjustIndexes(index);
 
         size--;
        if (size > 0 && size == indexes.length / 4) {
             resizeIndexes(indexes.length / 2);
         }
         return nodeToReturn.getItem();
     }
 
     private void readjustIndexes(int offset) {
 
         int current = offset;
         while (current < size - 1) {
 
             indexes[current] = indexes[current + 1];
             current++;
         }
         indexes[size - 1] = null;
     }
 
     public Iterator<Item> iterator() {
 
         return new RandomizedQueueIterator();
     }
 
     private final class RandomizedQueueIterator implements Iterator<Item> {
 
         private int current;
         private Object[] iterator;
         
         public RandomizedQueueIterator() {
             
             iterator = new Object[size];
             
             for (int i = 0; i < size; i++) {
                 
                 int index = StdRandom.uniform(size);
                 while (iterator[index] != null) {
                     index = StdRandom.uniform(size);
                 }
                 iterator[index] = indexes[i];
             }
                 
             current = 0;
         }
 
         @Override
         public boolean hasNext() {
             return current < iterator.length;
         }
 
         @Override
         public Item next() {
 
             if (!hasNext()) {
                 throw new NoSuchElementException(
                         "There is no more elements to retrieve");
             }
             Item item = ((Node<Item>) iterator[current]).getItem();
             current++;
             return item;
         }
 
         @Override
         public void remove() {
 
             throw new UnsupportedOperationException(
                     "You can't remove from an iterator.");
         }
 
     }
 
     private <I> Node<I> getNode(I item) {
 
         return new Node<I>(item);
     }
 
     private final class Node<T> {
 
         private final T item;
         private Node<T> next;
         private Node<T> prev;
 
         public Node(T item) {
             this.item = item;
         }
 
         public T getItem() {
             return item;
         }
 
         public void setNext(Node<T> newNext) {
 
             this.next = newNext;
         }
 
         public Node<T> getNext() {
 
             return this.next;
         }
 
         public void setPrev(Node<T> newPrev) {
 
             this.prev = newPrev;
         }
 
         public Node<T> getPrev() {
             return prev;
         }
     }
 }
