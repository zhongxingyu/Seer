 import java.util.Scanner;
 public class primes {
 	public static void main(String[] args) {
 		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter the nth prime to be calculated: ");
 		int nth = scanner.nextInt();
 		int counter = 1;
 		int[] primes = new int[nth];
 		primes[0] = 2;
 		for (int i = 3; counter < nth; i=i+2) {
 			for (int m = 0; m < counter; ++m) {
 				if (i % primes[m] == 0) {
 				break;
 				}
 			if (i/primes[m] <= primes[m]) {
 				primes[counter++] = i;
 				break;
 			}
 		}
 	}
		System.out.println("The prime number of " + nth + " is: " + primes[nth-1] + ".");
 	}
 }
