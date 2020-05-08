 /* FirstProgram.java - A Sample Java Program
  * Bryan Stockus - Introduction to Java
  */
 
 package chapter1;
 
 import java.util.Scanner;
 
 public class FirstProgram {
 	public static void main(String[] args) {
 		System.out.println("Hello out there.");
 		System.out.println("I will add two numbers for you.");
 		System.out.print("Enter two whole numbers on a line: ");
 		
 		int n1, n2;
 		
 		Scanner keyboard = new Scanner(System.in);
 		n1 = keyboard.nextInt();
 		n2 = keyboard.nextInt();
 		
 		int sum = n1 + n2;
 		
 		System.out.print("The sum of those two numbers is:");
 		System.out.println(n1 + " + " + n2 + " = " + sum);
 	}
 }
