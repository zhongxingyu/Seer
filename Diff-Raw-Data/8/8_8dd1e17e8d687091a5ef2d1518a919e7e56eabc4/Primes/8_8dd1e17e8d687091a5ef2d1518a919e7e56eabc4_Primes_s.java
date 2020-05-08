 /** 
  * Calculates and stores prime integers for quick and inexpensive lookups.
  *
  * Running times:
  *  - Looking up pre-calculated prime:            O(1)
  *  - Calculating the next prime in the sequence: O(N)
  *  - Calculating a prime out of sequence:        O(N^2)
  *
  * Memory usage:
  *  - 1/2 bit for each digit
  *  - Example: Memory to store all primes up to the number 64 costs 32 bits (1 int)
  * 
  * @author derv
  */
 public class Primes {
 	
 	/** 
 	 * Size of an integer in Java.
 	 * Standard on all JVMs, but put here for explicitness.
 	 */
 	private final int INT_SIZE = 32;
 	
 	/**
 	 * The most-recently-calculated prime (in sequence)
 	 * All numbers equal to or less than currentPrime are already in the bitmap.
 	 */
 	private int currentPrime = 1;
 	
 	/** 
 	 * Bitmap tracking which integers are prime.
 	 * We know even numbers will not be prime (excluding 2).
 	 *
 	 * Example:
	 * First elemnt's 7 (LSBs): ...  0    1    1    0   1   1   1
	 * These bits translate to: ... [15] [13] [11] [9] [7] [5] [3]
 	 * We can easily check if a number is a prime by ANDing with the appropriate bit.
 	 */
 	private int[] oddBitmap;
 	
 	/** Initializes bitmap. */
 	public Primes() { 
 		oddBitmap = new int[1];
 	}
 
 	/** 
 	 * Initializes bitmap to contain all primes up to capcity.
 	 * @param capacity The highest prime to pre-calculate.
 	 */
 	public Primes(int capacity) {
 		// Initialize array to hold all the incoming bits.
 		// Code will resize the bitmap on the fly, but this prevents that slow-down.
 		oddBitmap = new int[((capacity - 3) / 64) + 1];
 		// Generate bitmap for primes up (and possibly beyond) to the given number
 		isPrime(capacity);
 	}
 	
 	/** 
 	 * Calculates the next prime in sequence and adds to bitmap.
 	 * @return The next prime in the sequence.
 	 */
 	public int nextPrime() {
 		// First prime must be 2. This requires some ugly hacking.
 		if (currentPrime < 2) {
 			currentPrime += 2;
 			return 2;
 		} else if (currentPrime == 3) {
 			currentPrime -= 2; // isPrime will increase currentPrime by 2.
 		}
 		while (!isPrime(currentPrime));
 		return currentPrime - 2;
 	}
 	
 	/** 
 	 * Adds a number (prime or not) to the bitmap.
 	 * Doubles bitmap size as needed.
 	 * @param n The number to add.
 	 * @param prime True if n is prime, false otherwise.
 	 */
 	private void addNumber(int n, boolean prime) {
 		int oddIndex = (n - 3) / 2; // We want '3' to be the first bit
 		int row = oddIndex / 32;
 		int col = oddIndex % 32;
 		if (row >= oddBitmap.length) {
 			// We need to increase the size of the bitmap
 			// Increase by 2x current size
 			int[] newBitmap = new int[oddBitmap.length * 2];
 			System.arraycopy(oddBitmap, 0, newBitmap, 0, oddBitmap.length);
 			for (int i = oddBitmap.length; i < newBitmap.length; i++) {
 				newBitmap[i] = 0;
 			}
 			oddBitmap = newBitmap;
 		}
 		int i = (prime ? 1 : 0);
 		i = i << col;
 		oddBitmap[row] = (oddBitmap[row] | i);
 	}
 
 	/** 
 	 * Performs the O(n) computation to see if a number is prime.
 	 * @param n The number to check if prime or not.
 	 * @return True if n is prime, false otherwise.
 	 */
 	private boolean calculatePrime(int n) {
 		// Ignore multiples of 2 (except 2).
 		if (n < 2)      return false;
 		if (n == 2)     return true;
 		if (n % 2 == 0) return false;
 		int maxFactor = n / 2; // Highest possible factor of n.
 		// Only check odd factors.
 		for (int i = 3; i < maxFactor; i += 2) {
 			// If is a factor, then n is not prime.
 			if (n % i == 0) return false;
 		}
 		return true;
 	}
 	
 	/** 
 	 * Checks if n is a prime. 
 	 * Calculates all primes up to n if they haven't been calculated yet.
 	 * @param n The number to check for primality.
 	 * @return True if number is prime, false otherwise.
 	 */
 	public boolean isPrime(int n) {
 		// Fill bitmap with all primes up to n
 		while (currentPrime <= n) {
 			currentPrime += 2;
 			addNumber(currentPrime, calculatePrime(currentPrime));
 		}
 		// Check for even numbers
 		if (n < 2)      return false;
 		if (n == 2)     return true;
 		if (n % 2 == 0) return false;
 		// Find prime in bitmap
 		int oddIndex = (n - 3) / 2; // We want '3' to be the first bit
 		int row = oddIndex / 32;
 		int col = oddIndex % 32;
 		int i = 1 << col;
 		return (oddBitmap[row] & i) > 0;
 	}
 
 	/** Displays bitmap. */
 	public void printBitmap() {
		// Highest row that may contain 1 bits.
 		int maxRow = (((currentPrime - 1) / 2) / 32);
 		System.out.print("Bitmap of odd primes (starting at 3):\n\t");
 		// Iterate over every row in bitmap (except non-calculated rows)
 		for (int row = 0; row < oddBitmap.length && row <= maxRow; row++) {
 			int temp = 1; // Will be shifted to check for bits in bitmap
 			// Iterate over every bit in this row
 			for (int i = 0; i < INT_SIZE; i++) {
 				boolean prime = (temp & oddBitmap[row]) > 0;
 				System.out.print( prime ? "1" : "0" );
 				temp = temp << 1; // Shift to next bit
 			}
 			System.out.print("\n\t");
 		}
 		System.out.println();
 	}
 }
