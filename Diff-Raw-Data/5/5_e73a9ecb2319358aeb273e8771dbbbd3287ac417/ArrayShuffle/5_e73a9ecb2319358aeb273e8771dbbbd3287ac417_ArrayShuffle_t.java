 package com.contentanalyst.util;
 
 import java.util.Random;
 
 /**
  * Utility class to shuffle array of type T.  Uses Knuth Shuffle Algorithm
  * 
  * @author David Flynt
  *
  * @param <T> any class
  */
 public final class ArrayShuffle <T> {
 	private final Random rand;
 	
 	/**
 	 * Construct new instance of ArrayShuffle
 	 */
 	public ArrayShuffle() {
 		rand = new Random();
 	}
 	
 	/**
 	 * Random permutation of an array of objects.  All permutations occur
 	 * with approximately equal likelihood.
 	 * 
 	 * @param arr the shuffled array
 	 */
 	// Knuth Shuffle Algorithm
 	public void shuffle(T[] arr) {
		for (int i = arr.length; i > 1; i--) {
			swap(arr, i - 1, rand.nextInt(i));
		}
 	}
 	
 	private void swap(T[] arr, int i, int j) {
 		T temp = arr[i];
 		arr[i] = arr[j];
 		arr[j] = temp;
 	}
 }
