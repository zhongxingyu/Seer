 package Aufgabe2Diffie;
 
 import java.math.BigInteger;
 
 public class Test {
 	
 	public static void main(String args[]){
 		Alice alice = Alice.create(BigInteger.valueOf(2));
		Bob bob = Bob.create(BigInteger.valueOf(2));
 		alice.init(bob);
 		System.out.println(alice);
 		System.out.println(bob);
 	}
 
 }
