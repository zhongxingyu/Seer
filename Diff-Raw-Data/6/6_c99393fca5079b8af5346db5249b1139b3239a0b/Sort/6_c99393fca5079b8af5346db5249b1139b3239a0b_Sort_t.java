 package com.tuhailong.algorithm;
 
 import java.util.Random;
 
 public class Sort {
 	
 	private static final boolean DEBUG = false;
 
 	private static final int BASE = 100;
 	private static final int SIZE = 25;
 
 	private static final String ORIGIN = "Random Array";
 	private static final String BUBBLE = "Bubble Sort";
 	private static final String INSERT = "Insert Sort";
 	private static final String QUICK = "Quick Sort";
 	
 	// generate a random array whose length is 'size'
 	public static int[] createRandomArray(int size) {
 		int array[] = new int[size];
 		for (int i = 0; i < size; ++i) {
 			array[i] = new Random().nextInt(BASE);
 		}
 		return array;
 	}
 
 	public static int[] copyArray(int list[]) {
 		int len = list.length;
 		int copy[] = new int[len];
 		System.arraycopy(list, 0, copy, 0, len);
 		return copy;
 	}
 
 	public static void printArray(int list[], String tag) {
 		if (null != tag) {
 			System.out.print(tag + ": ");
 		}
 		System.out.print("[");
 		int len = list.length;
 		for (int i = 0; i < len; ++i) {
 			System.out.print(list[i]);
 			if (i != len - 1) {
 				System.out.print(", ");
 			}
 		}
 		System.out.println("]");
 	}
 
 	// exchange two elements in the array
 	public static void swap(int list[], int a, int b) {
 		list[a] ^= list[b];
 		list[b] ^= list[a];
 		list[a] ^= list[b];
 	}
 
 	/* Bubble-Sort works by repeatedly stepping through the list to be sorted,
 	 * comparing each pair of adjacent items and swapping them if they are in
 	 * the desired order. The pass through the list is repeated until no swaps
 	 * are needed, which indicates that the list is sorted.
 	 */
 	public static void bubbleSort(int list[]) {
 		// 'sorted' is used for optimizing for sorted array
 		boolean sorted = true;
 		for (int i = list.length - 1; i > 0; --i) {
 			for (int j = 0; j < i; ++j) {
 				if (list[j] > list[j + 1]) {
 					swap(list, j, j + 1);
 					sorted = false;
 				}
 			}
 			if (sorted) {
 				return;
 			}
 		}
 	}
 	
 	// Insert-Sort builds the final sorted array one item at a time.
 	public static void insertSort(int list[]) {
 		if (DEBUG) {
 			System.out.println();
 			System.out.println("START: debug insert sort:");
 		    printArray(list, null);
 		}
 		int len = list.length;
 		for (int i = 1; i < len; ++i) {
 			int tmp = list[i];
 			int j = i - 1;
 			while (j >= 0 && tmp < list[j]) {
 				list[j + 1] = list[j];
 				if (DEBUG) {
 				    printArray(list, null);
 				}
 				--j;
 			}
 			list[j + 1] = tmp;
 			if (DEBUG) {
 			    printArray(list, null);
 			}
 		}
 		if (DEBUG) {
 		    System.out.println("END: debug insert sort");
 		    System.out.println();
 		}
 	}
 
 	/* Quick-Sort is a divide and conquer algorithm. Quick sort first divides a
 	 * large list into two smaller sub-lists: the low elements and the high
 	 * elements. Quick sort can then recursively sort the sub-lists. 
 	 * The steps are: 
 	 * 1. Pick an element, called a pivot, from the list. 
 	 * 2. Reorder the list so that all elements with values less than 
 	 * the pivot come before the pivot, while all elements with values
 	 * greater than the pivot come after it(equal values can go either way). 
 	 * After this partitioning, the pivot is in its final position. 
 	 * This is called the partition operation. 
 	 * 3. Recursively apply the above steps to the sub-list of elements with
 	 * smaller values and separately the sub-list of elements with greater
 	 * values.
 	 */
 	public static void quickSort(int list[]) {
 		quickSortInner(list, 0, list.length - 1);
 	}
 
 	private static void quickSortInner(int list[], int low, int high) {
 		if (low < high) {
 			int pivot = partition(list, low, high);
 			quickSortInner(list, low, pivot - 1);
 			quickSortInner(list, pivot + 1, high);
 		}
 	}
 
 	private static int partition(int list[], int low, int high) {
 		int pivot = list[high];
 		int index = low - 1;
 
 		for (int k = low; k < high; ++k) {
 			if (list[k] <= pivot) {
 				++index;
 				// optimized for the case that 'index' equals 'k'
 				if (index != k) {
 					swap(list, index, k);
 				}
 			}
 		}
 
 		if (index + 1 != high) {
 			swap(list, index + 1, high);
 		}
 
 		return index + 1;
 	}
 
 	public static void main(String[] args) {
 		// generate a random integer array
 		int list[] = createRandomArray(SIZE);
 
 		// provide some special arrays to test algorithm robustness
 		//int list[] = {9, 8, 7, 6, 5, 4, 3, 2, 1};
 		//int list[] = {1, 2, 3, 4, 5, 6, 7, 8, 9};
 
 		// print the random array
 		printArray(list, ORIGIN);
 
 		int blist[] = copyArray(list);
 		int ilist[] = copyArray(list);
		int qlist[] = copyArray(list);
 
 		// do bubble sort
 		bubbleSort(blist);
 		// do insertion sort
 		insertSort(ilist);
 		// do quick sort
 		quickSort(qlist);
 
 		// print sorted array
 		printArray(blist, BUBBLE);
		printArray(ilist, INSERT);
 		printArray(qlist, QUICK);
 	}
 }
