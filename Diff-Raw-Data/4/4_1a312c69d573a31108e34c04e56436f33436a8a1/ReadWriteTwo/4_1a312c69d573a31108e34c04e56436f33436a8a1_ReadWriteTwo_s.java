 /* 
  * Uses:
  * Method Call
  * Returns
  */
 
 package package_APLessons_TrialOne;
 
 import java.util.Scanner;
 
 public class ReadWriteTwo {
 
 	public static void main(String[] args) {
 		
 		//initialization
     	
     	System.out.println("This program was written by James Daniel");
     	System.out.println();
     	String varOne;
     	System.out.print("Enter your name: ");
     	Scanner readStuff = new Scanner(System.in);
         varOne = readStuff.nextLine();
         System.out.println();
     	
  
         
     	for (int foo = 0; foo < 4; ++foo) //ints
     	{
     		//section 1 of loop
     		
     		int firstNum, secondNum, sum, differ, mult, qot, remain;
             
             System.out.println();
             System.out.println("Trial " + (foo+1) + " integers: ");
             System.out.print("Please enter your first number: ");
             firstNum = readStuff.nextInt();
             System.out.print("Please enter your second number: ");
             secondNum = readStuff.nextInt();
             sum = summation(firstNum, secondNum);
             differ = difference(firstNum, secondNum);
             mult = multiplex(firstNum, secondNum);
             
             
             System.out.println("The sum of " + firstNum + " and " + secondNum + " is " + sum);
             System.out.println("The difference of " + firstNum + " and " + secondNum + " is " + differ);
             System.out.println("The product of " + firstNum + " and " + secondNum + " is " + mult);
             
             @SuppressWarnings("unused")
 			boolean dba = divisible(secondNum);
             if (dba = false)
             	System.out.println("The quotient of " + firstNum + " and " + secondNum + " cannot be resolved. | (ERR: DIV/0)");
             else
             {
             	qot = division(firstNum, secondNum);
                 remain = modulus(firstNum, secondNum);
             	System.out.println("The quotient of " + firstNum + " and " + secondNum + " is " + qot + "r" + remain);
             }
             	
             
             System.out.println();
             
             
             //section 2 of loop
     		
     		double firstNumd, secondNumd, sumd, differd, multd, qotd; //doubles
             
             System.out.println();
             System.out.println("Trial " + (foo+1) + " doubles: ");
             System.out.print("Please enter your first number: ");
             firstNumd = readStuff.nextDouble();
             System.out.print("Please enter your second number: ");
             secondNumd = readStuff.nextDouble();
             sumd = summationDb(firstNumd, secondNumd);
             differd = differenceDb(firstNumd, secondNumd);
             multd = multiplexDb(firstNumd, secondNumd);
             
             System.out.println("The sum of " + firstNumd + " and " + secondNumd + " is " + sumd);
             System.out.println("The difference of " + firstNumd + " and " + secondNumd + " is " + differd);
             System.out.println("The product of " + firstNumd + " and " + secondNumd + " is " + multd);
             
             
            if (secondNum == 0)
             	System.out.println("The quotient of " + firstNumd + " and " + secondNumd + " cannot be resolved. | (ERR: DIV/0)");
             else
             {
             	qotd = divisionDb(firstNumd, secondNumd);
             	System.out.println("The quotient of " + firstNumd + " and " + secondNumd + " is " + qotd);
             }
             
             System.out.println();
             
     	}
     	
     	
     	System.out.println("# # # # # # # # # # # # # # # # # # # #");
     	System.out.println("This program was run by " + varOne);
     	System.out.println("# # # # # # # # # # # # # # # # # # # #");
         readStuff.close();
 
 	}
 	
 	///////////////////////////////////////////// subroutines start here
 	
 	public static int summation (int a, int b) {
 		int foo = a + b;
 		return foo;
 	}
 	
 	public static int difference (int a, int b) {
 		int foo = a - b;
 		return foo;
 	}
 	
 	public static int multiplex (int a, int b) {
 		int foo = a * b;
 		return foo;
 	}
 	
 	public static boolean divisible (int a) {
 		if (a == 0)
 			return false;
 			else return true;
 	}
 	
 	public static int division (int a, int b) {
 		int foo = a / b;
 		return foo;
 	}
 	
 	public static int modulus (int a, int b) {
 		int foo = a % b;
 		return foo;
 	}
 	
 	
 	//////////////////////////////////////////////////
 	
 	
 	public static double summationDb (double a, double b) {
 		double foo = a + b;
 		return foo;
 	}
 	
 	public static double differenceDb (double a, double b) {
 		double foo = a - b;
 		return foo;
 	}
 	
 	public static double multiplexDb (double a, double b) {
 		double foo = a * b;
 		return foo;
 	}
 	
 	public static boolean divisibleDb (double a) {
 		if (a == 0.0)
 			return false;
 			else return true;
 	}
 	
 	public static double divisionDb (double a, double b) {
 		double foo = a / b;
 		return foo;
 	}
 	
 		
 }
