 import java.util.Arrays;
 
 
 public class MyStack {
     
     private int size = 0;
     private static final int INITIAL_CAPACITY = 10;
     private Object elements[];
     
     /**
      * Nullary constructor
      */
     public MyStack() {
         this.elements = new Object[INITIAL_CAPACITY];
     }
     
     /**
      *
      * Constructor that overrides the initial capacity.  If less than 1, defaults to initial capacity.
      * @param size
      */
     public MyStack(int size) {
         if(size >= 1) {
             this.elements = new Object[size];
         } else {
             this.elements = new Object[INITIAL_CAPACITY];
         }
     }
     
     /**
      * Pushes an object onto the stack.  Ignores null.
      * @param o
      */
     public void push(Object o) {
         
         if(size == elements.length && o != null){
             this.resize();
         }
         this.elements[size++] = o;
         
     }
     
     /**
      * Resizes if the stack is too small.
      */
     protected void resize() {
         
         int newSize = this.elements.length * 3;
         
         elements = Arrays.copyOf(elements, newSize);
     }
 
     /**
      * Removes an element from the stack. Ignores if size is 0.
      *
      * @return
      */
     public Object pop() {
         
         if(size == 0) return null;
         
         Object o = this.elements[--size];
         this.elements[size] = null;
         return o;
     }
     
     /**
      * Returns the size of the stack
      * @return
      */
     public int size() {
         
         return this.size;
     }
     
     /**
      * Get value at stack
      * @param i
      * @return
      */
     public Object atIndex(int i) {
         if(i < size && i >= 0) {
             return this.elements[i];
         } else {
             return null;
         }
     }
 
     /**
      * Returns a string representation of objects on stack.
      * @return
      */
     public String toString() {
        String format = "[ ";
         for(int i = 0; i < this.size; i++) {
             format+="\"";
             format+= (this.elements[i] + "\"");
             if(i < (this.size-1)) {
                 format+=", ";
             }
         }
 
        return format+" ]";
     }
 }
 
 
