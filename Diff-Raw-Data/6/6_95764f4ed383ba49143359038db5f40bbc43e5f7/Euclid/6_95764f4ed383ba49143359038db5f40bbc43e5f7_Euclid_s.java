 package main;
 
 public class Euclid {
 	
 	/**
 	 * @return
 	 * Returns the Greatest Common Divisor or -1 if the input is invalid
 	 * (invalid inputs are any value less than 0)
 	 */
 	public static int GCD(int m, int n) {
		if(m<=0||n<=0) {
			return -1;
		}
		//Ensure m is greater than n otherwise swap
		if(m<n){int t=m;m=n;n=t;}
 
 		int r;
 		while((r=m%n)!=0) {
 			m = n;
 			n = r;
 		} 
 
 		return n;
 	}
 }
