 /* 3.1 Describe how you could use a single array to implement three stacks
  * */
 
 /* Pre-considerations:
  * 1. What is the type of the values in the stack.
  * 2. Can we wrap the value with an object?
  * 3. Is it a array stack or list stack?
  */
 
 /* Post-considerations:
  * 1. There is a problem when setting the entries in stackPointer as
  *    the original indices in the buffer, i.e., it will be difficult to check 
  *    if the stack is empty, since the pointer initially points to the first
  *    element of the stack, which is not initiated whenc constructing the stack.
  *    One possible solution is to set the first element as a sentinel.
 * 2. Another approach to address #1, is to set the entries in stackPointes as
 *    the relative indices in each stack, starting from -1 when initializing,
 *    instead of the absolute indices in the buffer.
  * */
 
 public class P0301 {
    // Implementation with array stack, assuming the value type is int.
    public class StacksInArray {
      private int stackSize;
      private int numOfStacks;
       private int[] buffer;
       private int[] stackPointer;
 
       // Defult constructer, stackSize = 300, numofStacks = 3
       public StacksInArray() {
          this(300, 3);
       }
 
       public StacksInArray(int stackSize, int numOfStacks) {
          this.stackSize = stackSize;
          this.numOfStacks = numOfStacks;
 
          buffer = new int[stackSize * numOfStacks];
          initPointers();
       }
 
       private void initPointers() {
          stackPointer = new int[numOfStacks];
          for (int i = 0; i < numOfStacks; i++) {
             stackPointer[i] = i * stackSize;
             // fix for post-consideration#1
             buffer[stackPointer[i]] = Integer.MIN_VALUE;
          }
       }
 
       public int getNumOfStacks() {
          return numOfStacks;
       }
 
       public void push(int stackNum, int value) {
          if (stackNum < 0 || stackNum >= numOfStacks) {
             throw new IndexOutOfBoundsException("The stack number should" +
                " between " + 0 + " and " + (numOfStacks - 1));
          }
 
          if (isFull(stackNum))
             throw new IndexOutOfBoundsException("Stack #" + stackNum + 
                " is full.");
 
          buffer[++stackPointer[stackNum]] = value;
       }
 
       public int pop(int stackNum) {
          if (stackNum < 0 || stackNum >= numOfStacks) {
             throw new IndexOutOfBoundsException("The stack number should" +
                " between " + 0 + " and " + (numOfStacks - 1));
          }
 
          // fix for post-consideration#1
          if (isEmpty(stackNum)) {
             throw new IndexOutOfBoundsException("Stack #" + stackNum +
                " is empty.");
          }
 
          int index = stackPointer[stackNum]--;
          int value = buffer[index];
          buffer[index] = 0;
          return value;
       }
 
       public boolean isEmpty(int stackNum) {
          return stackPointer[stackNum] == stackNum * stackSize;
       }
 
       public boolean isFull(int stackNum) {
          return stackPointer[stackNum] == (stackNum + 1) * stackSize - 1;
       }
    }
 
    public static void main(String[] args) {
       P0301 p0301 = new P0301();
       StacksInArray stacks = p0301.new StacksInArray();
       System.out.println(stacks.getNumOfStacks());
       System.out.println(stacks.isEmpty(1));
 
       stacks.push(1, 5);
       stacks.push(0, 4);
       System.out.println(stacks.isEmpty(0));
       System.out.println(stacks.pop(0));
       System.out.println(stacks.isEmpty(0));
    }
 }
