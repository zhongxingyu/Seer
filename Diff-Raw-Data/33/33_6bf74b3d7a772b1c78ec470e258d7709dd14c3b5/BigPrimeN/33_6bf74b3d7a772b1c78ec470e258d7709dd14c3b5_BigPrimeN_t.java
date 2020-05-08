 package Aufgabe2Diffie;
 
 import java.math.BigInteger;
 import java.util.Random;
 
 public class BigPrimeN {
 
 	private BigPrimeN(){}
 	
 	public static BigPrimeN create(){
 		return new BigPrimeN();
 	}
 	
 	// 1024Bit 
 	BigInteger next(){
		return BigInteger.probablePrime(100, new Random());
 	}
 }
