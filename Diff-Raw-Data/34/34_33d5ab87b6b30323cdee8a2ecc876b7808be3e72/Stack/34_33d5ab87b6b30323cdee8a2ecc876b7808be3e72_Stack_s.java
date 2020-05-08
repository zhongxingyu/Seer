 
 public class Stack {
	private Object[] mainStack;
 	public Stack(){
		mainStack = new Object[1000];
 	}
 	
 	public int pop(){
 	/*
 	 * returns the integer at the top of the stack
 	 * which is the last integer to be added
 	 * also removed that integer from the stack
 	 */
 		int top = (Integer)mainStack[0]; //will throw an error if the stack is empty...
 		int curIndex = 1;
 		while (mainStack[curIndex] != null){
 			mainStack[curIndex-1] = mainStack[curIndex];
 			curIndex++;
 		}
 		mainStack[curIndex-1] = null; //removes the redundant element at the bottom of the array
 		return top;
 	}
 	
 	public int push(int number){
 	/*
 	 * movesnumber int number to the top of the stack
 	 * returns the arg number
 	 */
 		int curIndex = 0;
 		Object temp = mainStack[curIndex];
 		while (mainStack[curIndex+1] != null){
 			temp = mainStack[curIndex+1];
 			mainStack[curIndex+1] = mainStack[curIndex];
 			curIndex++;
 		}
 		mainStack[curIndex+1] = temp;
 		mainStack[0] = (Object)number;
 		return number;
 	}
 	
 	public int top(){
 	/*
 	 * returns the next integer at the top of the stack
 	 * without taking it out of the stack
 	 */
		return (Integer)mainStack[0];	//should there be error handling for empty Stack?
 	}
 	
 	public boolean isEmpty(){
		if (mainStack.length == 0){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 }
