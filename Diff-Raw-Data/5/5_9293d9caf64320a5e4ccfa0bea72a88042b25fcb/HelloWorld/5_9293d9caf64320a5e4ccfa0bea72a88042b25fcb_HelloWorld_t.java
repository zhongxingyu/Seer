 /*************************************************************************
  *  Compilation:  javac HelloWorld.java
  *  Execution:    java HelloWorld
  *
  *  Prints "Hello, World". By tradition, this is everyone's first program.
  *
  *  % java HelloWorld
  *  Hello, World
  *
  *  These 17 lines of text are comments. They are not part of the program;
  *  they serve to remind us about its properties. The first two lines tell
  *  us what to type to compile and test the program. The next line describes
  *  the purpose of the program. The next few lines give a sample execution
  *  of the program and the resulting output. We will always include such 
  *  lines in our programs and encourage you to do the same.
  *
  *************************************************************************/
 
 public class HelloWorld {
 
     private int testSum(int n) {
 	int i;
 	int sum = 0;
	for (i = 0; i < n; i++)
 		sum += i;
	return sum;
     }
 
     private int testArraySum(int array[]) {
 	int sum = 0;
 	for (int i = 0; i < array.length; i++)
 	    sum += array[i];
 	return sum;
     }
 
     public static void main(String[] args) {
         System.out.println("Hello, World");
 	HelloWorld inst = new HelloWorld();
 	int res = inst.testSum(100);
 	System.out.println("Sum = " + res);
 
 	int a[] = new int[20];
 	for (int i = 0; i < 20; i++)
 	    a[i] = i * 10;
 	res = inst.testArraySum(a);
 	System.out.println("Sum = " + res);
     }
 }
 
