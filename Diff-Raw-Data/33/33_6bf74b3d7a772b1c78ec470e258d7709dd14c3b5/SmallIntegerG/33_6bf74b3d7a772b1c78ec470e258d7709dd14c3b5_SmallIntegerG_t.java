 package Aufgabe2Diffie;
 
 import java.math.BigInteger;
 import java.util.Random;
 
 public class SmallIntegerG {
 	
 	private final BigInteger n;
 	
 	private SmallIntegerG(BigInteger n){
 		this.n = n;
 	}
 	
 	public static SmallIntegerG create(BigInteger n){
 		return new SmallIntegerG(n);
 	}
 
 	// {2,..,n-2}
 	BigInteger next(){
 		return rand(BigInteger.valueOf(2),n.subtract(BigInteger.valueOf(2)));
 	}
 	
 	public BigInteger rand(BigInteger up,BigInteger to) {
 		to = to.subtract(BigInteger.valueOf(2));
 	    Random rand = new Random();
	    BigInteger result = new BigInteger(n.intValue()/100, rand);
 	   
 	    result = result.mod(to);
 	    result = result.add(BigInteger.valueOf(2));
 	    return result;
 	}
 	
 	public static double myRandom(double high) {  
 	    return Math.random() * high;  
 	}  
 	
 }
