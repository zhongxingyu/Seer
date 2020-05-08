 import java.util.EmptyStackException;
 
 public class Stack {
 
     public int sp;  /* assume stack is full for now and that sp is not
                            contained on the stack */
     public int pc;  /* program counter */
     public int[] stack;
     public int height;
 
     public Stack(int height) {
         this.sp = height;
         this.stack = new int[height];  /* create the stack with specified height */
         this.height = height;
     }
 
     public void push(int val) {
         if (sp <= 2) {  /* full stack */
             System.err.println("Can't push another value--this would overwrite PC.  Exiting");
             System.exit(1);
         } else {
             this.stack[--sp] = val;
         }
     }
 
     public int pop() {
         if (sp >= height) {  /* empty stack */
            System.err.println("Can't pop--empty stack.  Exiting");
             throw new EmptyStackException();  /* need to throw an exception
                                                  because Java complains that
                                                  we're not returning anything
                                                 */
         } else {
             return this.stack[sp++];
         }
     }
 
     public String toString() {
         /* prints out the stack like one might imagine a stack might look like */
         String s = new String();
         String temp;
 
         for (int pointer = height-1; pointer >= sp; pointer--) { /* start from the
                                                               top of the stack
                                                               and work your way
                                                               down to sp */
             temp = "" + stack[pointer] + '\n';
             s += temp;
         }
 
         return s;
     }
 }
