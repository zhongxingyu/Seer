 import java.io.*;
 
 // Sieve of Eratosthenes
 
 // Psuedo Code 
 // Start with an array is_prime[2..N], all initialized to true.
 // For each i such that 2 <= i  and i^2 <= N , do
 //   If isprime[i] is true,
 //     For each multiple m of i such that , 
 //       "cross out m" — set is_prime[m] to false
 // For more information : http://nlvm.usu.edu/en/nav/frames_asid_158_g_3_t_1.html?open=instructions
 
 public class SieveofEratosthenes{
 
 	private boolean[] isPrimes;
 	private int n;
 
 	public SieveofEratosthenes(int inp){
		System.out.println("\n--Sieve of Eratosthenes--\n");
		if(inp <= 0){
			System.out.println("Invalid Input");
			n = -1;
		}else if(inp == 1){
			System.out.println("It is prime");
			n = -1;
		}else{
			this.n = inp ;
			isPrimes = new boolean[n + 1];
			for(int i = 0; i < n+1; i++){
				isPrimes[i] = true;
			}
 		}
 	}
 
 	public void markPrimes(){
		if(n == -1)
			return;
 		for(int divisor = 2; divisor * divisor <= n; divisor++){
 			if(isPrimes[divisor]){
 				for(int i = 2 * divisor; i <= n; i = i + divisor){
 					isPrimes[i] = false;
 				}
 			}
 		}
 
 	}
 
 	public void printPrimes(){
		if(n == -1)
			return;
 
 		for(int i = 2; i <= n; i++){
 			if(isPrimes[i]){
 				System.out.print("" + i + "  ");
 			}
 		}
 	}
 
 
 
 } 
