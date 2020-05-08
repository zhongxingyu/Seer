 import java.util.Scanner;
 
 /**
  * CalculateExactly contains functions for arbitrary precision
  * arithmetic on real numbers. It also contains a sample
  * application to try out the class.
  *
  * Numbers are contained in char[] arrays where digits are
  * stored as integer values 0 - 9 and sign (-) and period (.)
  * characters as their respective ASCII values. There is no
  * container format. This unfortunately requires checks in
  * every operation.
  *
  * TODO: Clean up pad()
  * TODO: Support negative numbers
  * TODO: Return numbers in shortest form (unpad?)
  *
  * @author Alexander Overvoorde
  */
 public class CalculateExactly {
 	/**
 	 * Returns the result of adding two real numbers.
 	 * @param a Left-hand value of addition operation
 	 * @param b Right-hand value of addition operation
 	 * @return Sum of the two values
 	 */
 	public static char[] add(char[] a, char[] b) {
 		a = pad(a, b);
 		b = pad(b, a);
 
 		char[] res = new char[a.length];
 		char remainder = 0;
 
 		for (int i = a.length - 1; i >= 0; i--) {
 			// Sign and period characters are left alone
 			if (a[i] > 9) {
 				res[i] = a[i];
 				continue;
 			}
 
 			// Digits are added onto each other
 			char t = (char) (a[i] + b[i] + remainder);
 			if (t < 9) {
 				res[i] = t;
				remainder = 0;
 			} else {
 				res[i] = (char) (t % 10);
 				remainder = (char) ((t - res[i]) / 10);
 			}
 		}
 
 		// Add an extra digit if there is still a remainder
 		if (remainder > 0) {
 			char[] temp = new char[res.length+1];
 			for (int i = 0; i < res.length; i++) {
 				temp[i+1] = res[i];
 			}
 			temp[0] = remainder;
 			res = temp;
 		}
 
 		return res;
 	}
 
 	/**
 	 * Returns the result of subtracting two real numbers.
 	 * @param a Left-hand value of subtraction operation
 	 * @param b Right-hand value of subtraction operation
 	 * @return Result of subtracing b from a
 	 */
 	public static char[] subtract(char[] a, char[] b) {
 		return "3.14".toCharArray();
 	}
 
 	/**
 	 * Returns the result of multiplying two real numbers.
 	 * @param a Left-hand value of multiplication operation
 	 * @param b Right-hand value of multiplication operation
 	 * @return Product of the two values
 	 */
 	public static char[] multiply(char[] a, char[] b) {
 		return "3.14".toCharArray();
 	}
 
 	/**
 	 * Returns the result of dividing two real numbers.
 	 * @param a Left-hand value of division operation
 	 * @param b Right-hand value of division operation
 	 * @return Result of dividing a by b
 	 */
 	public static char[] divide(char[] a, char[] b) {
 		return "3.14".toCharArray();
 	}
 
 	/**
 	 * Pads a number with zeroes so that it has the same
 	 * size as another number.
 	 * @param n Number to pad
 	 * @param ref Reference number with target pad size
 	 * @return Number padded to have the same size as ref
 	 */
 	public static char[] pad(char[] n, char[] ref) {
 		// Collect information about structure of both numbers
 		int beforeRef = 0;
 		int afterRef = 0;
 		for (int i = 0; i < ref.length; i++) {
 			if (ref[i] == '.') {
 				beforeRef = i;
 				afterRef = ref.length - i - 1;
 				break;
 			}
 		}
 
 		int before = 0;
 		int after = 0;
 		for (int i = 0; i < n.length; i++) {
 			if (n[i] == '.') {
 				before = i;
 				after = n.length - i - 1;
 				break;
 			}
 		}
 
 		// If n is more precise than ref, nothing needs to be done
 		if (before > beforeRef && after > afterRef) {
 			return n;
 		} else {
 			if (before < beforeRef) {
 				char[] temp = new char[n.length + beforeRef - before];
 
 				for (int i = 0; i < temp.length; i++) {
 					if (i < beforeRef - before) {
 						temp[i] = 0;
 					} else {
 						temp[i] = n[i - beforeRef + before];
 					}
 				}
 
 				n = temp;
 			}
 
 			if (after < afterRef) {
 				char[] temp = new char[n.length + afterRef - after];
 
 				for (int i = 0; i < temp.length; i++) {
 					if (i >= n.length) {
 						temp[i] = 0;
 					} else {
 						temp[i] = n[i];
 					}
 				}
 
 				n = temp;
 			}
 
 			return n;
 		}
 	}
 
 	/**
 	 * Verifies that a string contains a valid real number
 	 * and parses it into a correct char array.
 	 * @param str String representation of a number
 	 * @return Number in internal representation
 	 */
 	public static char[] parseString(String str) {
 		str = str.trim();
 
 		if (str.length() == 0) throw new NumberFormatException();
 
 		// There is a sign character in the middle of the number
 		if (str.lastIndexOf('-') > 0) throw new NumberFormatException();
 
 		// There are multiple decimal periods
 		if (str.indexOf('.') != str.lastIndexOf('.')) throw new NumberFormatException();
 
 		char[] number = str.trim().toCharArray();
 
 		// Verify that there are no illegal characters
 		// and convert number digits to actual int values
 		boolean decimal = false;
 		for (int i = 0; i < number.length; i++) {
 			char c = number[i];
 			if ((c < '0' || c > '9') && c != '.' && c != '-')
 				throw new NumberFormatException();
 
 			if (c >= '0' && c <= '9')
 				number[i] = (char) (c - '0');
 			else if (c == '.')
 				decimal = true;
 		}
 
 		// Integers are internally represented as real numbers
 		// to reduce edge cases and decrease code complexity.
 		// e.g. 3 as input results in 3.0 internally
 		if (!decimal) {
 			char[] temp = new char[number.length+2];
 
 			for (int i = 0; i < temp.length; i++) {
 				if (i < number.length) {
 					temp[i] = number[i];
 				} else if (i == temp.length - 2) {
 					temp[i] = '.';
 				} else {
 					temp[i] = 0;
 				}
 			}
 
 			number = temp;
 		}
 
 		return number;
 	}
 
 	/**
 	 * Converts a number in the internal char array representation
 	 * back to a human-readable string.
 	 * @param n Internal representation of a number
 	 * @return Number in string representation
 	 */
 	public static String toString(char[] n) {
 		char[] t = new char[n.length];
 
 		for (int i = 0; i < n.length; i++) {
 			if (n[i] > 9) {
 				t[i] = n[i];
 			} else {
 				t[i] = (char) (n[i] + '0');
 			}
 		}
 
 		return new String(t);
 	}
 
 	/**
 	 * Sample program allowing a user to interact with exact calculations
 	 * and test the CalculateExactly implementation.
 	 */
 	public static void main(String[] args) {
 		Scanner scan = new Scanner(System.in);
 
 		// Ask for operation
 		System.out.print("Operation (+, -, *, /): ");
 		String t = scan.nextLine();
 		if (!t.matches("[\\+\\-\\*\\/]")) {
 			System.out.println("Invalid operation specified.");
 			return;
 		}
 		char op = t.charAt(0);
 
 		// Ask for operands
 		char[] a = null;
 		char[] b = null;
 		try {
 			System.out.print("Left-hand operand: ");
 			a = parseString(scan.nextLine());
 			System.out.print("Right-hand operand: ");
 			b = parseString(scan.nextLine());
 		} catch (NumberFormatException e) {
 			System.out.println("Invalid number specified.");
 			return;
 		}
 
 		// Evaluate calculation
 		char[] r = null;
 		switch (op) {
 			case '+':
 				r = add(a, b);
 				break;
 			case '-':
 				r = subtract(a, b);
 				break;
 			case '*':
 				r = multiply(a, b);
 				break;
 			case '/':
 				r = divide(a, b);
 				break;
 		}
 		System.out.println(toString(a) + " " + op + " " + toString(b) + " = " + toString(r));
 	}
 }
