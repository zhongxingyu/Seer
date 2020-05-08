 import java.util.*;
 
 public class Fraction {
 	/**
 	 * prompt for a numerator and denominator and print out resulting fraction
	 * as a float
	 */
 	public static void main(String[] args) {
 		Scanner console = new Scanner(System.in);
 		System.out.print("Enter a numerator: ");
 		float numerator = console.nextFloat();
 		System.out.print("Enter a denominator: ");
 		float denominator = console.nextFloat();
 		System.out.println("Result: " + numerator / denominator);
 	}
 }
