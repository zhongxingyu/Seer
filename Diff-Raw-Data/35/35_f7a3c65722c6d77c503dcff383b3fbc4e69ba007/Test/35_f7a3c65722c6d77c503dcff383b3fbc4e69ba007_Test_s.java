 import java.util.*;
 
 class Test{
 	public static void main(String args[]){
 		
 		System.out.println("\n\n\n----------------------------Start TEST World!------------------------------"
			+"------------\n\n\n");
 		// System.out.println(Arrays.toString(args));
 		SieveofEratosthenes obj = new SieveofEratosthenes(Integer.parseInt(args[0]));
 		obj.markPrimes();
 		obj.printPrimes();
		System.out.println("\n\n\n-----------------------------Ends TEST World!---------------------------"
 			+"---------------\n\n\n");
 	}
 }
