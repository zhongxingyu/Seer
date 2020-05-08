 import java.util.Scanner;
 import java.util.Arrays;
 
 /**
  * CalculateExactly contains functions for arbitrary precision
  * arithmetic on real numbers. It also contains a sample
  * application to try out the class.
  *
  * There is no encapsulated container format. This unfortunately
  * requires checks in every operation.
  *
  * Numbers are contained in char[] arrays where digits are
  * stored as integer values 0 - 9 and sign (-) and period (.)
  * characters as their respective ASCII values. The format
  * is similar to the human-readable string representation, but
  * there are a few differences. For example, integers like 5
  * are always stored as 5.0 to reduce code complexity. Numbers
  * as operands are also padded to the same size in operations,
  * but the result is always reduced to the smallest possible
  * representation, i.e. 0.05 + 0.05 will return 0.1 and not 0.10.
  *
 * TODO: Add limit to division for cases like 1.0/3.0 and 3.14/2.72
  *
  * @author Alexander Overvoorde
  */
 public class CalculateExactly {
 	public static final char[] ZERO = new char[] {0, '.', 0};
 	public static final char[] ONE = new char[] {1, '.', 0};
 
 	/**
 	 * This class is not intented to be instantiated as it
 	 * only contains static methods.
 	 */
 	private CalculateExactly() {}
 
 	/**
 	 * Returns the result of adding two real numbers.
 	 * @param a Left-hand value of addition operation
 	 * @param b Right-hand value of addition operation
 	 * @return Sum of the two values
 	 */
 	public static char[] add(char[] a, char[] b) {
 		if (!check(a) || !check(b)) throw new NumberFormatException();
 
 		// Additions with negative operands can sometimes be
 		// reinterpreted as different operations, e.g. 3 + -5 == 3 - 5
 		// -3 + -5 is evaluated as -(3 + 5) with nFlag
 		if (sign(a) >= 0 && sign(b) < 0) return subtract(a, negate(b));
 		else if (sign(a) < 0 && sign(b) >= 0) return subtract(b, negate(a));
 		else if (sign(a) < 0 && sign(b) < 0) return negate(add(negate(a), negate(b)));
 
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
 
 		return reduce(res);
 	}
 
 	/**
 	 * Returns the result of subtracting two real numbers.
 	 * @param a Left-hand value of subtraction operation
 	 * @param b Right-hand value of subtraction operation
 	 * @return Result of subtracing b from a
 	 */
 	public static char[] subtract(char[] a, char[] b) {
 		if (!check(a) || !check(b)) throw new NumberFormatException();
 
 		// Subtractions with negative operands can sometimes be
 		// reinterpreted as different operations, e.g. 3 - -5 == 3 + 5
 		if (sign(b) < 0) return add(a, negate(b));
 		else if (sign(a) < 0 && sign(b) >= 0) return negate(add(negate(a), b));
 
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
 
 			// Digits are subtracted from each other
 			byte t = (byte) (a[i] - b[i] - remainder);
 			if (t >= 0) {
 				res[i] = (char) t;
 				remainder = 0;
 			} else {
 				res[i] = (char) (10 + t);
 				remainder = 1;
 			}
 		}
 
 		// If there is a remainder, that means b > a and the result should be negative
 		// This can be easily handled with the identity a - b = -(b - a)
 		if (remainder != 0) {
 			return negate(subtract(b, a));
 		}
 
 		return reduce(res);
 	}
 
 	/**
 	 * Returns the result of multiplying two real numbers.
 	 * @param a Left-hand value of multiplication operation
 	 * @param b Right-hand value of multiplication operation
 	 * @return Product of the two values
 	 */
 	public static char[] multiply(char[] a, char[] b) {
 		if (!check(a) || !check(b)) throw new NumberFormatException();
 
 		int sa = sign(a); if (sa < 0) a = negate(a);
 		int sb = sign(b); if (sb < 0) b = negate(b);
 
 		int[] size = getNumberSize(a);
 		char[] res = ZERO;
 
 		// Multiplication is done in two steps to reduce the
 		// complexity of the shift operations.
 
 		char[] base = Arrays.copyOf(b, b.length);
 		char[] power = Arrays.copyOf(base, base.length);
 
 		// First multiply b by digits before decimal period
 		for (int i = size[0] - 1; i >= 0; i--) {
 			if (a[i] != 0)
 				res = add(res, multiply(power, a[i]));
 
 			power = shiftRight(power);
 		}
 
 		power = shiftLeft(Arrays.copyOf(base, base.length));
 
 		// Then multiply b by digits after decimal period
 		for (int i = size[0] + 1; i < a.length; i++) {
 			if (a[i] != 0)
 				res = add(res, multiply(power, a[i]));
 
 			power = shiftLeft(power);
 		}
 
 		if (sa == sb)
 			return res;
 		else
 			return negate(res);
 	}
 
 	/**
 	 * Returns the result of dividing two real numbers.
 	 * 
 	 * @param a Left-hand value of division operation
 	 * @param b Right-hand value of division operation
 	 * @return Result of dividing a by b
 	 */
 	public static char[] divide(char[] a, char[] b) {
 		if (!check(a) || !check(b)) throw new NumberFormatException();
 
 		int sa = sign(a); if (sa < 0) a = negate(a);
 		int sb = sign(b); if (sb < 0) b = negate(b);
 
 		// Don't modify input values
 		a = Arrays.copyOf(a, a.length);
 		b = Arrays.copyOf(b, b.length);
 
 		// Find largest power p where b * 10^p <= a
 		char[] power = ONE;
 		if (compare(a, b) == 1) {
 			while (compare(a, b) == 1) {
 				b = shiftRight(b);
 				power = shiftRight(power);
 			}
 			// Overshoot compensation should not apply in the special case a * 10^p == b (e.g. 50/5)
 			if (compare(a, b) != 0) {
 				b = shiftLeft(b);
 				power = shiftLeft(power);
 			}
 		} else if (compare(a, b) == -1) {
 			while (compare(a, b) == -1) {
 				b = shiftLeft(b);
 				power = shiftLeft(power);
 			}
 		} else {
 			// shortcut in case |a| == |b|
 			if (sa == sb)
 				return ONE;
 			else
 				return negate(ONE);
 		}
 
 		char[] res = ZERO;
 
 		while (!isZero(a)) {
 			int f = fit(a, b);
 			res = add(res, multiply(power, f));
 			a = subtract(a, multiply(b, f));
 
 			power = shiftLeft(power);
 			b = shiftLeft(b);
 		}
 
 		if (sa == sb)
 			return res;
 		else
 			return negate(res);
 	}
 
 	/**
 	 * Multiplies the specified number by a positive integer.
 	 * @param n Number to multiply
 	 * @param c Integer to multiply by (non-negative)
 	 * @return n * c
 	 */
 	private static char[] multiply(char[] n, int c) {
 		if (c == 0) return ZERO;
 
 		char[] total = n;
 
 		for (int i = 1; i < c; i++) {
 			total = add(total, n);
 		}
 
 		return total;
 	}
 
 	/**
 	 * Returns how many times the specified divisor can fit
 	 * in the given number. This function is not suited for
 	 * values for d significantly (over 10x) larger than n.
 	 * @param n Number to divide
 	 * @param d Devisor
 	 * @return floor(n / d)
 	 */
 	private static int fit(char[] n, char[] d) {
 		int c = 0;
 
 		while(compare(n, d) >= 0) {
 			n = subtract(n, d);
 			c++;
 		}
 
 		return c;
 	}
 
 	/**
 	 * Shifts the decimal period in a number to the left.
 	 * @param n Number to shift
 	 * @return n / 10
 	 */
 	private static char[] shiftLeft(char[] n) {
		n = Arrays.copyOf(n, n.length);

 		for (int i = 0; i < n.length; i++) {
 			if (n[i+1] == '.') {
 				n[i+1] = n[i];
 				n[i] = '.';
 				break;
 			}
 		}
 
 		if (n[0] == '.')
 			n = zeroPadArray(n, 1, 0);
 
 		return reduce(n);
 	}
 
 	/**
 	 * Shifts the decimal period in a number to the right.
 	 * @param n Number to shift
 	 * @return n * 10
 	 */
 	private static char[] shiftRight(char[] n) {
		n = Arrays.copyOf(n, n.length);

 		for (int i = 0; i < n.length; i++) {
 			if (n[i] == '.') {
 				n[i] = n[i+1];
 				n[i+1] = '.';
 				break;
 			}
 		}
 
 		if (n[n.length-1] == '.')
 			n = zeroPadArray(n, 0, 1);
 
 		return reduce(n);
 	}
 
 	/**
 	 * Verifies that the specified array contains a valid
 	 * internal representation of a number.
 	 * @param n Array to check
 	 * @return true if the array is a valid number, or false if not
 	 */
 	private static boolean check(char[] n) {
 		// Smallest valid number is 1.0
 		if (n.length < 3) return false;
 		
 		int periods = 0;
 
 		for (int i = 0; i < n.length; i++) {
 			char c = n[i];
 
 			// Only 0-9, . and - are valid values
 			if (c > 9 && c != '.' && c != '-') return false;
 
 			// Decimal periods can not occur at the start or end
 			if (c == '.' && (i == 0 || i == n.length - 1)) return false;
 
 			// Negative sign character can only occur at the start
 			if (c == '-' && i > 0) return false;
 
 			if (c == '.')
 				periods++;
 		}
 
 		// There must be exactly one decimal period
 		if (periods != 1)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Returns the sign of the specified number.
 	 * @param n Number
 	 * @return 0 if n == 0, 1 if n is positive and -1 if n is negative
 	 */
 	private static int sign(char[] n) {
 		if (isZero(n)) {
 			return 0;
 		} else if (n[0] == '-') {
 			return -1;
 		} else {
 			return 1;
 		}
 	}
 
 	/**
 	 * Returns whether the specified number is equal to 0.
 	 * @param n Number
 	 * @return true if n is equal to 0, false otherwise
 	 */
 	private static boolean isZero(char[] n) {
 		for (char c : n)
 			if (c > 0 && c <= 9) return false;
 		return true;
 	}
 
 	/**
 	 * Returns the magnitude of a compared to b.
 	 * @param a Number to compare
 	 * @param b Number to compare to
 	 * @return 1 if a is larger than b, -1 if a is smaller than b and 0 if they're equal
 	 */
 	private static int compare(char[] a, char[] b) {
 		return sign(subtract(a, b));
 	}
 
 	/**
 	 * Flips the sign of the specified number.
 	 * @param n Number
 	 * @return -n
 	 */
 	private static char[] negate(char[] n) {
 		if (n[0] == '-') {
 			return Arrays.copyOfRange(n, 1, n.length);
 		} else {
 			char[] temp = new char[n.length+1];
 
 			for (int i = 0; i < temp.length; i++) {
 				if (i == 0) {
 					temp[i] = '-';
 				} else {
 					temp[i] = n[i-1];
 				}
 			}
 
 			return temp;
 		}
 	}
 
 	/**
 	 * Pads a number with zeroes so that it has the same
 	 * size as another number.
 	 * @param n Number to pad
 	 * @param ref Reference number with target pad size
 	 * @return Number padded to have the same size as ref
 	 */
 	private static char[] pad(char[] n, char[] ref) {
 		int[] curSize = getNumberSize(n);
 		int[] refSize = getNumberSize(ref);
 
 		// If n is more or equally precise than ref, nothing
 		// needs to be done, ref should be padded to a instead.
 		if (curSize[0] >= refSize[0] && curSize[1] >= refSize[1]) {
 			return n;
 		} else {
 			return zeroPadArray(n, Math.max(0, refSize[0] - curSize[0]), Math.max(0, refSize[1] - curSize[1]));
 		}
 	}
 
 	/**
 	 * Removes zeroes before and after to reduce a number
 	 * to the smallest possible representation while
 	 * maintaining the precision.
 	 */
 	private static char[] reduce(char[] n) {
 		// The zero in 0.5 should not be removed
 		int before = 0;
 		for (int i = 0; i < n.length; i++)
 			if (n[i] == 0 && n[i+1] != '.')
 				before++;
 			else
 				break;
 
 		// The zero in 1.0 should not be removed either
 		int after = 0;
 		for (int i = n.length - 1; i >= 0; i--)
 			if (n[i] == 0 && n[i-1] != '.')
 				after++;
 			else
 				break;
 
 		// Return new array without them
 		char[] temp = new char[n.length - before - after];
 		for (int i = 0; i < temp.length; i++) {
 			temp[i] = n[i + before];
 		}
 
 		if (isZero(temp)) {
 			// Prevents -0 from occuring (sign makes no sense)
 			return ZERO;
 		} else {
 			return temp;
 		}
 	}
 
 	/**
 	 * Returns the amount of digits before and after the
 	 * decimal period in the specified number.
 	 * @param n A number
 	 * @return An array with two values, the before and after digit count
 	 */
 	private static int[] getNumberSize(char[] n) {
 		int before = 0;
 		int after = 0;
 		for (int i = 0; i < n.length; i++) {
 			if (n[i] == '.') {
 				before = i;
 				after = n.length - i - 1;
 				break;
 			}
 		}
 		return new int[] {before, after};
 	}
 
 	/**
 	 * Adds the specified amount of zeroes before and after
 	 * the values in an existing array.
 	 * @param arr Array to be padded
 	 * @param before Amount of zeroes to prepend
 	 * @param after Amount of zeroes to append
 	 */
 	private static char[] zeroPadArray(char[] arr, int before, int after) {
 		char[] temp = new char[arr.length + before + after];
 
 		for (int i = 0; i < temp.length; i++) {
 			if (i < before) {
 				temp[i] = 0;
 			} else if (i > before + arr.length - 1) {
 				temp[i] = 0;
 			} else {
 				temp[i] = arr[i - before];
 			}
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Verifies that a string contains a valid real number
 	 * and parses it into a correct char array.
 	 * @param str String representation of a number
 	 * @return Number in internal representation
 	 */
 	public static char[] parseString(String str) {
 		str = str.trim();
 
 		// There is no number to parse
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
 
 		// If there is a decimal period at the beginning or end, pad with a zero
 		if (number[0] == '.')
 			number = zeroPadArray(number, 1, 0);
 		if (number[number.length-1] == '.')
 			number = zeroPadArray(number, 0, 1);
 
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
 		n = reduce(n);
 
 		char[] t = new char[n.length];
 
 		for (int i = 0; i < n.length; i++) {
 			if (n[i] > 9) {
 				t[i] = n[i];
 			} else {
 				t[i] = (char) (n[i] + '0');
 			}
 		}
 
 		// Numbers like 1.0 are displayed as 1
 		if (t[t.length-2] == '.' && t[t.length-1] == '0')
 			return new String(t, 0, t.length - 2);
 		else
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
