 package com.igordaoconsulting.training.sorting;
 
class InsertionSort extends SortingAlgorithmUsingIntArray {
 
	InsertionSort(int[] input) {
 		super(input);
 	}
 
 	public void run() {
 		for (int i = 1; i < input.length; ++i) {
 			int key = input[i];
 			int j = i - 1;
			while (j >= 0 && input[j] > key) {
 				input[j + 1] = input[j];
 				--j;
 			}
 			input[j + 1] = key;
 		}
 	}
 
 }
