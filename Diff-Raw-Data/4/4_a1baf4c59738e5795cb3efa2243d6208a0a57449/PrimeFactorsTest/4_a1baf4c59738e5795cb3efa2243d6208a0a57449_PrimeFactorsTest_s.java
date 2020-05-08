 package org.anumonk.katas.primefactors;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 public class PrimeFactorsTest {
 
 	private PrimeFactors primeFactorsService = new PrimeFactors();
 
 	@Test
 	public void shouldReturnEmptyListForOne() {
 		// when generating the prime-factors of 1
 		List<Integer> resultingFactors = primeFactorsService.generate(1);
 
 		// we expect an empty list to be returned
 		assertThat(resultingFactors, is(Collections.EMPTY_LIST));
 	}
 
 	@Test
 	public void shouldReturnPrimeForTwo() {
 		// when generating the prime factors of two
 		List<Integer> resultingFactors = primeFactorsService.generate(2);
 
 		// we expect the prime number 2 to be returned
 		assertThat("resulting prime factor for 2 should be the prime number 2",
				resultingFactors, is(listWithValues(1)));
 
 	}
 	
 	private List<Integer> listWithValues(Integer... vals) {
 		ArrayList<Integer> valueHolder = new ArrayList<Integer>();
 		for (Integer val : vals) {
 			valueHolder.add(val);
 		}
 		return valueHolder;
 	}
 
 }
