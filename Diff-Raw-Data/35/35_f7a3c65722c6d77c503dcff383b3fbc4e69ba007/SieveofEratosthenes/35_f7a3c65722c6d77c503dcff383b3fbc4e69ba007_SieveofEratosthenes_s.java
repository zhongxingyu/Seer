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
		// System.out.println("Inside SieveofEratosthenes constructor with inp::" + inp);
		this.n = inp ;
		isPrimes = new boolean[n + 1];
		for(int i = 0; i < n+1; i++){
			isPrimes[i] = true;
 		}
 	}
 
 	public void markPrimes(){

 		for(int divisor = 2; divisor * divisor <= n; divisor++){
 			if(isPrimes[divisor]){
 				for(int i = 2 * divisor; i <= n; i = i + divisor){
 					isPrimes[i] = false;
 				}
 			}
 		}
 
 	}
 
 	public void printPrimes(){
 
 		for(int i = 2; i <= n; i++){
 			if(isPrimes[i]){
 				System.out.print("" + i + "  ");
 			}
 		}
 	}
 
 
 
 } 
