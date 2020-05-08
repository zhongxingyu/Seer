 /*
  * (Find the Smallest Value)
  * Write an application that finds the smallest of several integers.
  * Assume that the first value read specifies the number of values to input from the user.
  */
 import java.util.Scanner;
 
 public class SmallestValue {
 	public static void main(String[] args) {
 		int numOfVals = 0;
 		int value; // This need not be initialized because of the if condition inside the for
 		int small = 0;
 
 		Scanner input = new Scanner(System.in);
 		System.out.print("Enter the number of values: ");
 		numOfVals = input.nextInt();
 
 		for(int i = 0; i < numOfVals; i++) {
 			System.out.printf("Enter the value no. %d: ", i+1);
 			value = input.nextInt();
 
 			if(i == 0) {
 				small = value;
 			} else if (value < small) {
 				small = value;
 			}
 		}
 
		System.out.printf("The smallest number is: %d\n", small);
 	}
 }
 
