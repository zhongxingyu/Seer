 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class PermutationsTest {
 	
 	String intListToString(List<Integer> l) {
 		Integer[] array = new Integer[l.size()];
 		for (int i = 0; i < array.length; ++i) {
 			array[i] = l.get(i);
 		}
 		Arrays.sort(array);
 		String s = "[";
 		for (int i = 0; i < array.length; ++i) {
 			s += array[i].toString();
 			if (i < (array.length - 1)) {
 				s += ", ";
 			}
 		}
 		return s + "]";
 	}
 	
 	String listListToString(String[] array) {
 		String s = "{";
 		Arrays.sort(array);
 		for (int i = 0; i < array.length; ++i) {
 			s += array[i].toString();
 			if (i < (array.length - 1)) {
 				s += ", ";
 			}
 		}
 		return s + "}";
 	}
 	
 	String allToUnique(List<List<Integer>> results) {
 		String[] resultStrings = new String[results.size()];
 		for (int i = 0; i < results.size(); ++i) {
 			resultStrings[i] = intListToString(results.get(i));
 		}
 		return listListToString(resultStrings);
 	}
 	
 	void harness(List<Integer> lst, List<List<Integer>> expected) throws Exception {
 		List<List<Integer>> results = TradeInProgress.permutationsWithFirstElement(lst);
 		String r = allToUnique(results);
 		String e = allToUnique(expected);
 		assertTrue(r + " != " + e, r.compareTo(e) == 0);
 	}
 	
 	@Test
 	public void simple() throws Exception {
 		harness(Arrays.asList(1, 2, 3),
 				Arrays.asList(
 						Arrays.asList(1),
 						Arrays.asList(1, 2),
 						Arrays.asList(1, 3),
						Arrays.asList(1, 2, 3),
						Arrays.asList(2),
						Arrays.asList(2, 3),
						Arrays.asList(3)
 						));
 	}
 }
