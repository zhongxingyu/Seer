 package tk.rebornlegendmc.random;

import java.util.InputMismatchException;
import java.util.Scanner;
 
 public class Number_Adder {
 	public static void main(String[] args) {
		try {
			System.out.println("Adder Test! :D");
			System.out.println("Type the first integer: ");
			Scanner sc = new Scanner(System.in);
			int i = sc.nextInt();
			Integer firstNumber = i;
			System.out.println("Type the second integer: ");
			int t = sc.nextInt();
			Integer secondNumber = t;
			System.out.println(firstNumber + secondNumber);
		} catch (InputMismatchException e) {
			System.out.println("Aarrrghh! You entered a letter instead of a number! " + "(" + e + ")");
			System.out.println("Please don't do that again, It's painful ;(");
		}
	}
	}
