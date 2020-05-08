 public class IntStack{
     int[] stack;
     int top;
 
     public IntStack(int size)   //param optional: size of stack}
     {
 	stack = new int[size];
 	top=0;
     }
     boolean isEmpty() {
 	return top==0;
     }
     void push(int num){
 	stack[top++]=num;
     }
     int pop(){
 	return stack[--top];
     }
     int peek()  //sometimes
     {
 	return stack[top-1];
     }
     
     /**
       Function to peek at a specific depth
       @author Henry Screen
       @param depth the distance from the top
     */
     int peekDepth(int depth)      
     {
	if (depth<=top) return stack[top-depth];
	else
	{
	    return -1;
	}
     }
  
     public static void main (String[] args){
 	IntStack is = new IntStack(10);
         is.push(4);
 	is.push(5);
 	is.push(6);
 	is.push(7);
 	is.push(8);
 	is.push(9);
 	is.push(10);
 	System.out.println(is.peek());
 	
 	//tests for peekDepth:
	System.out.println(is.peekDepth(100));
 	
 	int val = is.pop();
 	System.out.println(val);
     }
 }
