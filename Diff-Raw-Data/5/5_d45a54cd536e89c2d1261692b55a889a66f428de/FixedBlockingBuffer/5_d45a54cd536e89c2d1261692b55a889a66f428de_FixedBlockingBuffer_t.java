 package org.transtruct.cmthunes.util;
 
 import java.util.*;
 
 /**
  * A simple fixed capacity FIFO buffer. Add calls will block until space is
  * available in the buffer, and get calls will block until an element is
  * available. This is useful in provider-consumer implementations where the
  * provider may be slow so more items can be buffered.
  * 
  * @author Christopher Thunes <cthunes@transtruct.org>
  * @param <T>
  *            Element type to store
  */
 public class FixedBlockingBuffer<T> {
     /** Buffer elements */
     private LinkedList<T> elements;
 
     /** Capacity of buffer */
     private int capacity;
 
     /** Buffer is full */
     private Flag full;
 
     /** Buffer is empty */
     private Flag empty;
 
     /**
      * Initializes a new FixedBlockingBuffer of the given capacity
      * 
      * @param capacity
      *            The capacity of the buffer
      */
     public FixedBlockingBuffer(int capacity) {
         this.elements = new LinkedList<T>();
        this.capacity = capacity;
 
         this.full = new Flag(false);
         this.empty = new Flag(true);
     }
 
     /**
      * Add an element to the buffer. If the buffer is full, this call will block
      * until room is available.
      * 
      * @param e
      *            The element to add to the buffer
      */
     public void add(T e) {
         synchronized(this.full) {
             while(this.full.isSet()) {
                 this.full.waitUninterruptiblyFor(false);
             }
 
             this.elements.add(e);
             if(this.empty.isSet()) {
                 this.empty.clear();
             }
 
            if(this.elements.size() >= this.capacity) {
                 this.full.set();
             }
         }
     }
 
     /**
      * Get an element from the buffer. If no element is available, block until
      * an element is available.
      * 
      * @return The next available element
      */
     public T get() {
         T element;
         synchronized(this.empty) {
             while(this.empty.isSet()) {
                 this.empty.waitUninterruptiblyFor(false);
             }
 
             element = this.elements.remove();
             if(this.full.isSet()) {
                 this.full.clear();
             }
 
             if(this.elements.size() == 0) {
                 this.empty.set();
             }
         }
 
         return element;
     }
 
     /**
      * Return the capacity of the buffer
      * 
      * @return the capacity of the buffer
      */
     public int getCapacity() {
         return this.capacity;
     }
 
     /**
      * Return the number of items in the buffer
      * 
      * @return the number of items in the buffer
      */
     public int getSize() {
         return this.elements.size();
     }
 }
