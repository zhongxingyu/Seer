 public class Tricky {
 	public static void main (String[] args) {
 		int x = 1;
 		if( 	(4 > x) ^ 		// true, x = 1
			((++x + 2) > 3)) 	// true, x = 3  
 			x++; 			// does not execute, fails XOR
 		
		if(	(4 > ++x) ^		// false, x = 4
			!(++x == 5))		// false, x = 5 but !(expression) reserves the previous value of x
 			x++;  			// does not execute, fails XOR
 		
 		System.out.println(x); 		// prints '4'
 	}
 }
