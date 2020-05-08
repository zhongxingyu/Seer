 package com.madcoda.primes;
 
 import java.util.concurrent.Callable;
 
 /**
  * Count the number of primes from start to end-1
  * @author Jason
  *
  */
 public class CountJob implements Callable<Integer> {
 	
 	int start;
 	int end;
 	
 	public CountJob(int start, int end) {
 		super();
 		this.start = start;
 		this.end = end;
 	}
 
 
 	@Override
 	public Integer call() throws Exception {
 		int result = 0;
 		for(int i=start;i<end;i++){
 			if(isPrime(i)){
 				result++;
				//System.out.println(i + " ");
 			}
 		}
 		return result;
 	}
 	
 	
 	/**
 	 * A test for prime, potential performance increase if this also concurrent?
 	 * @param number
 	 * @return
 	 */
 	private static boolean isPrime(int number){
 		if(number <= 1)
 			return false;
 		if(number == 2) return true;
 		
 		int limit = (int) Math.round(Math.ceil(Math.sqrt(number)));
 		for(int i=2;i<=limit;i++){
 			if(number%i == 0){
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
