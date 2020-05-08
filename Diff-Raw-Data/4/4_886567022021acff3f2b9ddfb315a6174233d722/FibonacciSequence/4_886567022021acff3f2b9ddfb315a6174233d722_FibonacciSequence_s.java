 package com.mishadoff.algo.numeric;
 
 import java.math.BigInteger;
 
 public class FibonacciSequence implements Sequence<BigInteger> {
 	private BigInteger prev1 = BigInteger.ONE;
 	private BigInteger prev2 = BigInteger.ONE;
 	
 	private int currentIdx = 0;
 	
 	@Override
 	public int currentIdx() {
 		return this.currentIdx;
 	}
 	
 	public BigInteger next() {
 		if (currentIdx < 2) {
 			return BigInteger.ONE;
 		}
 		BigInteger result = prev1.add(prev2);
 			prev1 = prev2;
 			prev2 = result;
 			currentIdx++;
 		return result;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static void main(String[] args) {
 		// usage example
 		Sequence seq = new FibonacciSequence();
		for (int i = 0; i < 1000; i++) {
 			System.out.println(seq.next());
 		}
 	}
 }
