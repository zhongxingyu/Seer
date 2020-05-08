 import java.util.ArrayList;
 
 
 public class PrimeNumbers {
 	public static ArrayList<Integer> generatePrimes(int n){
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		
		while(n % 2 == 0){
 			ret.add(2);
 			n /= 2;
 		}
 		
		if(n > 1){
 			ret.add(n);
 		}
 		
 		return ret;
 	}
 }
