 package edu.msergey.jalg.exercises.ch4.ex19;
 
 import edu.msergey.jalg.exercises.ch4.ex18.IPushdownStack;
 
 public class StrictArrayBasedStack<E> implements IPushdownStack<E> {
     protected final Object[] elements;
     protected final int maxCount;
     protected int count;
 
     public StrictArrayBasedStack(int maxCount) {
         if (maxCount <= 0) throw new IllegalArgumentException("maxCount should be positive");
 
         try {
             this.elements = new Object[maxCount];
         }
         catch (OutOfMemoryError e) {
            // it's an exercise requirement to implement handling of OutOfMemory errors
             throw new IllegalArgumentException("too big maxCount", e);
         }
 
         this.count = 0;
         this.maxCount = maxCount;
     }
 
     public boolean isEmpty() {
         return (this.count == 0);
     }
 
     public int count() {
         return this.count;
     }
 
     public void push(E item) {
         if (item == null) throw new IllegalArgumentException("item can't be null");
         if (this.count == this.maxCount) throw new IllegalStateException("stack is full, no more space");
 
         this.elements[this.count] = item;
         this.count++;
     }
 
     @SuppressWarnings("unchecked")
     public E pop() {
         if (this.count == 0) throw new IllegalStateException("stack is empty");
 
         this.count--;
         return (E) this.elements[this.count];
     }
 }
