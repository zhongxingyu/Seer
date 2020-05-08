 package au.id.tmm.datastructures.list;
 
 import au.id.tmm.datastructures.Iterator;
 
 /**
  * Simple implementation of a doubly-linked list.
  */
 public class LinkedList<E> implements List<E> {
 
     private int size = 0;
 
     private ElementNode<E> head = null;
     private ElementNode<E> tail = null;
 
     /**
      * Create an empty list.
      */
     public LinkedList() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean add(E toAdd) {
 
         if (this.isEmpty()) {
             this.head = new ElementNode<E>(toAdd);
             this.tail = this.head;
         } else {
             ElementNode<E> newTail = new ElementNode<E>(toAdd);
             this.getLastNode().setNextNode(newTail);
             this.tail = newTail;
         }
 
         this.size++;
 
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void add(int index, E toAdd) {
 
         if (index == 0) {
             // Set as new head
             ElementNode<E> newNode = new ElementNode<E>(toAdd);
             newNode.setNextNode(this.head);
             this.head = newNode;
         } else if (index == this.getSize() - 1) {
             // Set as new tail
             ElementNode<E> newNode = new ElementNode<E>(toAdd);
             tail.setNextNode(newNode);
             this.tail = newNode;
         } else {
             ElementNode<E> insertAfter = this.getNode(index - 1);
             ElementNode<E> insertedNode = new ElementNode<E>(toAdd);
             insertedNode.setNextNode(insertAfter.getNextNode());
             insertAfter.setNextNode(insertedNode);
         }
 
         this.size++;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean remove(E toRemove) {
         this.removeNode(this.getNodeWithElement(toRemove));
 
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void remove(int index) {
         this.removeNode(this.getNode(index));
     }
 
     /**
      * Private utility method for removing a node from the list
      */
     private void removeNode(ElementNode<E> nodeToRemove) {
        if (this.getSize() == 1) {
            this.head = null;
            this.tail = null;
        } else if (this.head == nodeToRemove) {
             this.head = this.head.getNextNode();
             this.head.setPrevNode(null);
         } else if (this.tail == nodeToRemove) {
             this.tail = this.tail.getPrevNode();
             this.tail.setNextNode(null);
         } else {
             nodeToRemove.getPrevNode().setNextNode(nodeToRemove.getNextNode());
         }
 
         this.size--;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public E get(int index) {
         return this.getNode(index).getElement();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void set(int index, E toSet) {
         this.getNode(index).setElement(toSet);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void clear() {
         this.head = null;
         this.tail = null;
         this.size = 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean contains(E e) {
         return this.getNodeWithElement(e) != null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isEmpty() {
         return this.getSize() == 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getSize() {
         return this.size;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public au.id.tmm.datastructures.Iterator<E> iterator() {
         return new ConcreteLinkedListIterator<E>(this.head);
     }
 
     /**
      * Concrete implementation of the {@link Iterator} class for the LinkedList.
      */
     private class ConcreteLinkedListIterator<E> implements Iterator<E> {
 
         private ElementNode<E> currentNode;
 
         protected ConcreteLinkedListIterator(ElementNode<E> firstNode) {
             // Start with a simple pointer, so that the first call to next()
             // returns the first element.
             this.currentNode = new ElementNode<E>(null);
             this.currentNode.setNextNode(firstNode);
         }
 
         @Override
         public boolean hasNext() {
             return this.currentNode.hasNextNode();
         }
 
         @Override
         public E next() {
             this.currentNode = this.currentNode.getNextNode();
             return this.currentNode.getElement();
         }
     }
 
     /**
      * Returns the last node in the list (the tail).
      */
     private ElementNode<E> getLastNode() {
         return this.tail;
     }
 
     /**
      * Private utility method, performs a linear search of the elements in the
      * LinkedList and returns the first node with the given element.
      * @return the first node with the given element, or null if none can be
      *         found.
      */
     private ElementNode<E> getNodeWithElement(E element) {
 
         if (this.isEmpty()) {
             return null;
         } else {
 
             ElementNode<E> currentNode = this.head;
 
             do {
                 if (currentNode.getElement().equals(element)) {
                     return currentNode;
                 } else {
                     currentNode = currentNode.getNextNode();
                 }
             } while (currentNode != null);
 
         }
         return null;
     }
 
     /**
      * Private utility method, returns the node at the given index.
      */
     private ElementNode<E> getNode(int index) {
 
         if (this.isEmpty()) {
             throw new ArrayIndexOutOfBoundsException();
         }
 
         if (index < 0 || index >= this.getSize()) {
             throw new ArrayIndexOutOfBoundsException();
         }
 
         ElementNode<E> currentNode;
 
         if (index <= (this.getSize() / 2)) {
 
             currentNode = this.head;
 
             for (int i = 0; i < index; i++) {
                 if (currentNode.hasNextNode()) {
                     currentNode = currentNode.getNextNode();
                 } else {
                     throw new ArrayIndexOutOfBoundsException();
                 }
             }
 
         } else {
             currentNode = this.tail;
 
             for (int i = this.getSize() - 1; i > index; i--) {
                 if (currentNode.hasPrevNode()) {
                     currentNode = currentNode.getPrevNode();
                 } else {
                     throw new ArrayIndexOutOfBoundsException();
                 }
             }
 
         }
 
         return currentNode;
     }
 
     /**
      * Private class representing the nodes on the LinkedList. Each node
      * maintains a reference to the previous and next nodes.
      */
     private class ElementNode<E> {
         private E element;
         private ElementNode<E> nextNode = null;
         private ElementNode<E> prevNode = null;
 
         public ElementNode(E element) {
             this.element = element;
         }
 
         private E getElement() {
             return element;
         }
 
         private void setElement(E element) {
             this.element = element;
         }
 
         private boolean hasNextNode() {
             return this.getNextNode() != null;
         }
 
         private ElementNode<E> getNextNode() {
             return nextNode;
         }
 
         private void setNextNode(ElementNode<E> nextNode) {
             this.nextNode = nextNode;
             if (nextNode != null) {
                 nextNode.prevNode = this;
             }
         }
 
         private boolean hasPrevNode() {
             return this.getPrevNode() != null;
         }
 
         private ElementNode<E> getPrevNode() {
             return prevNode;
         }
 
         private void setPrevNode(ElementNode<E> prevNode) {
             this.prevNode = prevNode;
         }
     }
 
 }
