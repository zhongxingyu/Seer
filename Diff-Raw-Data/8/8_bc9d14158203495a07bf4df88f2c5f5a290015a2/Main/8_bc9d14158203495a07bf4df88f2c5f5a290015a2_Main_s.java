 import java.util.Arrays;
 import java.util.Scanner;
 
 public class Main {
 
 	/**
 	 * 
 	 * 
 	 * I'll use the star pattern 
 	 * 
 	 *       0
	 * 		/ \
 	 * 11-10---1---7
	 * 	\ /     \ /
 	 *   9       2
 	 *  / \	    / \
 	 * 6---5   4---3
	 * 		\ /
 	 *       8
 	 * 
 	 * Successfully completes judge input correctly in 
 	 * an average of 12 seconds.
 	 */
 	public static void main(String[] args) {
 
 		Scanner in = new Scanner(System.in);
 
 		int[] input = new int[12];
 		boolean[] used = new boolean[12];
 		int[] pos = new int[12];
 
 		for (int i = 0; i < 12; i++) {
 			input[i] = in.nextInt();
 		}
 
 		
 		long startTime = System.nanoTime();
 
 		// MAIN
 		while (input[0] != 0) {
 
 			Arrays.fill(used, false);
 
 			int count = backtrack(input, pos, used, 0, 0);
 
 			System.out.println(count / 12);
 
 			// Input next set
 			for (int i = 0; i < 12; i++) {
 				input[i] = in.nextInt();
 			}
 
 		}
 		
 
 		long endTime = System.nanoTime();
 
 		long duration = endTime - startTime;
 		
 		//System.out.println(duration/1000000000.0);
 		
 		
 	}
 
 	public static int backtrack(int[] input, int[] p, boolean[] used,
 			int numUsed, int sum) {
 
 		int count = 0;
 
 		// Initialize sum
 		if (numUsed == 4) {
 			sum = p[0] + p[1] + p[2] + p[3];
 		}
 
 		// Check for Failure
 		if (!isProper(p, sum, numUsed)) {
 			return 0;
 		}
 
 		// Success!
 		if (numUsed == 12) {
 			return 1;
 		}
 
 		for (int i = 0; i < 12; i++) {
 
 			if (!used[i]) {
 
 				// Make Change
 				p[numUsed] = input[i];
 				used[i] = true;
 				
 				// Backtrack
 				count += backtrack(input, p, used, numUsed + 1, sum);
 				
 				// Revert state
 				used[i] = false;
 			}
 
 		}
 
 		return count;
 	}
 
 	/**
 	 * Takes in the number of used, the sum, and the state of the
 	 * board. 
 	 * 
 	 * If a row is freshly finished, checks to see if valid.
 	 * 
 	 */
 	public static boolean isProper(int[] p, int sum, int numUsed) {
 
 		if (numUsed == 7) {
 			if (p[3] + p[4] + p[5] + p[6] != sum) {
 				return false;
 			}
 		} else if (numUsed == 9) {
 			if (p[7] + p[2] + p[4] + p[8] != sum) {
 				return false;
 			}
 		} else if (numUsed == 11) {
 			if (p[0] + p[10] + p[9] + p[6] != sum) {
 				return false;
 			}
 		} else if (numUsed == 12) {
 			if (p[11] + p[10] + p[1] + p[7] != sum) {
 				return false;
 			}
 			if (p[11] + p[9] + p[5] + p[8] != sum) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 }
