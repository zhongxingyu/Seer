 package Algorithms;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import Support.Person;
 
 public class SearchingandSorting {
 	private static final Random r = new Random();
 
 	/**
 	 * Merges two sorted arrays into a. Assumes that there are a.length elements in total
 	 * 
 	 * @param a - Sorted array with buffer for b
 	 * @param b - Sorted array to merge into a
 	 */
 	public static void merge(int[] a, int[] b) {
 		int aIndex = a.length - b.length - 1;
 		int bIndex = b.length - 1;
 		for (int i = a.length - 1; i >= 0; i--) {
 			if (aIndex < 0 || (bIndex >= 0 && b[bIndex] >= a[aIndex])) {
 				a[i] = b[bIndex--];
 			} else {
 				a[i] = a[aIndex--];
 			}
 		}
 	}
 	
 	/**
 	 * Returns the index of find in a by doing a rotated binary search.
 	 * 
 	 * @param a - The sorted rotated array
 	 * @param find - The element to find
 	 * @return The index of find in a (-1 if find is not in a)
 	 */
 	public static int rotatedBinarySearch(int[] a, int find) {
 		return rotatedBinarySearch(a, find, 0, a.length);
 	}
 	
 	/**
 	 * Returns the index of find in a by doing a rotated binary search.
 	 * 
 	 * @param a - The sorted rotated array
 	 * @param find - The element to find
 	 * @param min - The minimum index to search (inclusive)
 	 * @param max - The maximum index to search (exclusive)
 	 * @return The index of find in a (-1 if find is not in a)
 	 */
 	private static int rotatedBinarySearch(int[] a, int find, int min, int max) {
 		if (max > min) {
			int mid = (max + min) / 2;
 			if (a[mid] == find) {
 				return mid;
 			} else if (a[mid] > find && a[min] <= find || (a[mid] < find && a[max - 1] < find)) {
 				return rotatedBinarySearch(a, find, min, mid);
 			} else {
 				return rotatedBinarySearch(a, find, mid + 1, max);
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Returns the index of find in array a that is separated by empty strings.
 	 * 
 	 * @param a - The sorted array
 	 * @param find - The element to find
 	 * @return The index of find in a (-1 if not found)
 	 */
 	public static int stringSearch(String[] a, String find) {
 		return stringSearch(a, find, 0, a.length);
 	}
 
 	/**
 	 * Returns the index of find in array a that is separated by empty strings.
 	 * 
 	 * @param a - The sorted array 
 	 * @param find - The element to find
 	 * @param min - The minimum index (inclusive)
 	 * @param max - The maximum index (exclusive)
 	 * @return The index of find in a (-1 if not found)
 	 */
 	private static int stringSearch(String[] a, String find, int min, int max) {
 		if (max > min) {
 			int mid = (max + min) / 2;
 			int oldIndex = mid;
 			while (mid < max && a[mid].length() == 0) {
 				mid++;
 			}
 			if (mid < max) {
 				if (a[mid].equals(find)) {
 					return mid;
 				} else if (a[mid].compareTo(find) < 0) {
 					return stringSearch(a, find, mid + 1, max);
 				}
 			}
 			return stringSearch(a, find, min, oldIndex);
 		}
 		return -1;
 	}
 	
 	/**
 	 * Performs Heap Sort on the integer array.
 	 * 
 	 * @param a - The array to sort
 	 */
 	public static void heapSort(int[] a) {
 		heapify(a);
 		int last = a.length - 1;
 		for (int i = 0; i < a.length; i++) {
			a[last] = extractMax(a, last);
			last--;
 		}
 	}
 	
 	/**
 	 * Returns the maximum from the heapified array a.
 	 * 
 	 * @param a - The heap to get the max from
 	 * @param last - The last index of the heap in the array
 	 * @return The maximum value in the heap
 	 */
 	private static int extractMax(int[] a, int last) {
 		int max = a[0];
 		a[0] = a[last--];
 		int index = 0;
 		int child = getBiggerChildIndex(a, index);
 		while (child <= last && a[child] > a[index]) {
 			swap(a, index, child);
 			index = child;
 			child = getBiggerChildIndex(a, index);
 		}
 		return max;
 	}
 	
 	/**
 	 * Converts a into a heap by bubbling up from the leafs.
 	 * 
 	 * @param a - The array to convert into a heap
 	 */
 	private static void heapify(int[] a) {
 		for (int i = a.length - 1; i >= 0; i--) {
 			bubbleUp(a, i);
 		}
 	}
 	
 	/**
 	 * Bubbles up the maximum elements from the index in the heap a
 	 * 
 	 * @param a - The heap
 	 * @param index - The current index
 	 */
 	private static void bubbleUp(int[] a, int index) {
 		int parent = getParentIndex(index);
 		while (parent != -1 && a[parent] < a[index]) {
 			swap(a, index, parent);
 			index = parent;
 			parent = getParentIndex(index);
 		}
 	}
 	
 	/**
 	 * Swaps the two values at the indices in the array
 	 * 
 	 * @param a - The array
 	 * @param first - The first element to swap
 	 * @param second - The second element to swap
 	 */
 	private static void swap(int[] a, int first, int second) {
 		int temp = a[first];
 		a[first] = a[second];
 		a[second] = temp;
 	}
 	
 	/**
 	 * Returns the parent index of the index in the heap.
 	 * 
 	 * @param index - The child
 	 * @return The index of the parent (-1 if the index is 0 (the root))
 	 */
 	private static int getParentIndex(int index) {
 		return index == 0 ? -1 : (index - 1) / 2;
 	}
 	
 	/**
 	 * Returns the index of the bigger child.
 	 * 
 	 * @param a - The heap
 	 * @param index - The parent index
 	 * @return The index of the larger child (a.length if none exists)
 	 */
 	private static int getBiggerChildIndex(int[] a, int index) {
 		int first = index * 2 + 1;
 		int second = index * 2 + 2;
 		if (first < a.length || second < a.length) {
 			if (first >= a.length || (second < a.length && a[second] > a[first])) {
 				return second;
 			} else {
 				return first;
 			}
 		}
 		return a.length;
 	}
 	
 	/**
 	 * Sorts a n * n matrix into sorted order.
 	 * 
 	 * @param matrix - Matrix to sort
 	 * @param n - Length of the matrix
 	 */
 	public static void sort(int[][] matrix, int n) {
 		sort(matrix, n, 0, n * n);
 	}
 	
 	/**
 	 * Sorts a n * n matrix into sorted order from left to right, top to down.
 	 * 
 	 * @param matrix - Matrix to sort
 	 * @param n - Length of the matrix
 	 * @param start - The starting index
 	 * @param end - The ending index
 	 */
 	public static void sort(int[][] matrix, int n, int start, int end) {
 		if (start < end) {
 			int index = r.nextInt(end - start) + start;
 			int pivot = matrix[getRow(index, n)][getCol(index, n)];
 			swap(matrix, getRow(index, n), getCol(index, n), getRow(start, n), getCol(start, n));
 			int large = start + 1;
 			for (int i = large; i < end; i++) {
 				int row = getRow(i, n);
 				int col = getCol(i, n);
 				if (matrix[row][col] < pivot) {
 					int row2 = getRow(large, n);
 					int col2 = getCol(large, n);
 					swap(matrix, row, col, row2, col2);
 					large++;
 				}
 			}
 			large--;
 			swap(matrix, getRow(start, n), getCol(start, n), getRow(large, n), getCol(large, n));
 			sort(matrix, n, start, large);
 			sort(matrix, n, large + 1, end);
 		}
 	}
 	
 	/**
 	 * Swaps two elements in a matrix.
 	 * 
 	 * @param matrix - The matrix to swap the elements in
 	 * @param row - Row of the first element
 	 * @param col - Col of the first element
 	 * @param row2 - Row of the second element
 	 * @param col2 - Col of the second element
 	 */
 	private static void swap(int[][] matrix, int row, int col, int row2, int col2) {
 		int temp = matrix[row][col];
 		matrix[row][col] = matrix[row2][col2];
 		matrix[row2][col2] = temp;
 	}
 	
 	/**
 	 * Returns the row of the index in a n * n matrix.
 	 * 
 	 * @param index - The index of the element
 	 * @param n - The length of the matrix
 	 * @return The row of the element
 	 */
 	private static int getRow(int index, int n) {
 		return index / n;
 	}
 	
 	/**
 	 * Returns the col of the index in a n * n matrix.
 	 * 
 	 * @param index - The index of the element
 	 * @param n - The length of the matrix
 	 * @return The col of the element
 	 */
 	private static int getCol(int index, int n) {
 		return index % n;
 	}
 	
 	/**
 	 * Returns whether or not the sorted matrix contains the number.
 	 * 
 	 * @param matrix - The matrix to find the element in
 	 * @param number - The number to search for
 	 * @return True if the matrix contains the number and false otherwise
 	 */
 	public static boolean matrixBS(int[][] matrix, int number) {
 		int row = 0;
 		int col = matrix.length - 1;
 		while (row < matrix.length && col >=0) {
 			if (matrix[row][col] == number) {
 				return true;
 			} else if (matrix[row][col] < number) {
 				row++;
 			} else {
 				col--;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns a solution to the toppling gymnast problem. (See book)
 	 * 
 	 * @param gymnasts - A list of the gymnasts
 	 * @return The solution to the toppling gymnast problem
 	 */
 	public static List<Person> topplingGymnast(List<Person> gymnasts) {
 		List<Person> maxOrder = new ArrayList<Person>();
 		List<Person> current = new ArrayList<Person>();
 		Collections.sort(gymnasts);
 		if (gymnasts.size() > 0) {
 			current.add(gymnasts.get(0));
 		}
 		for (int i = 1; i < gymnasts.size(); i++) {
 			Person p = gymnasts.get(i);
 			Person previous = gymnasts.get(i - 1);
 			if (p.weight < previous.weight || p.height < previous.height) {
 				current.clear();
 			}
 			current.add(p);
 			if (current.size() > maxOrder.size()) {
 				maxOrder = current;
 			}
 		}
 		return maxOrder;
 	}
 	
 	/**
 	 * Returns the ith order statistic in the array.
 	 * 
 	 * @param a - The array
 	 * @param i - The order of the statistic pursued
 	 * @return The ith order statistic
 	 */
 	public static int ithSelect(int[] a, int i) {
 		if (i <= 0 || i > a.length) {
 			throw new IllegalArgumentException();
 		}
 		return ithSelect(a, i - 1, 0, a.length);
 	}
 	
 	/**
 	 * Returns the ith order statistic in the array.
 	 * 
 	 * @param a - The array
 	 * @param i - The order of the statistic pursued
 	 * @param left - The left bound (inclusive)
 	 * @param right - The right bound (exclusive)
 	 * @return The ith order statistic
 	 */
 	private static int ithSelect(int[] a, int i, int left, int right) {
 		if (right - left == 1) {
 			return a[left];
 		}
 		int index = r.nextInt(right - left) + left; // Random pivot
 		int pivot = a[index];
 		swap(a, index, left);
 		int large = left + 1;
 		for (int k = large; k < right; k++) { // Pivot around the index
 			if (a[k] < pivot) {
 				swap(a, k, large++);
 			}
 		}
 		index = large - 1;
 		swap(a, left, index);
 		if (index == i) {
 			return a[index];
 		} else if (index > i) { // Search left half
 			return ithSelect(a, i, left, index);
 		} else { // Right half
 			return ithSelect(a, i, index + 1, right);
 		}
 	}
 	
 	/**
 	 * Sorts the array by using the Counting Sort algorithm.
 	 * 
 	 * @param a - The array to sort
 	 */
 	public static void sort(int[] a) {
 		if (a.length > 1) {
 			int min = a[0];
 			int max = a[0];
 			for (int i : a) {
 				if (i > max) {
 					max = i;
 				} else if (i < min) {
 					min = i;
 				}
 			}
 			sort(a, min, max);
 		}
 	}
 	
 	/**
 	 * Sorts the array a that has a range of k (max - min + 1) in O(n + k) time and space.
 	 * 
 	 * @param a - The array to sort
 	 * @param min - The minimum value in the array
 	 * @param max - The maximum value in the array
 	 */
 	private static void sort(int[] a, int min, int max) {
 		int[] count = new int[max - min + 1]; // Counts
 		int[] result = new int[a.length];
 		
 		// Count all of the entries in a
 		for (int i : a) {
 			count[i - min]++;
 		}
 		
 		// Change all the entries in count to be the index of i in the new array
 		int sum = 0;
 		for (int i = 0; i < count.length; i++) {
 			int c = count[i];
 			count[i] = sum;
 			sum += c;
 		}
 		
 		for (int i = 0; i < a.length; i++) {
 			result[count[a[i] - min]] = a[i]; // Get the new position of a[i]
 			count[a[i] - min]++; // The next element at a[i] - min will be one after this one
 		}
 		
 		// Copy over the new results
 		for (int i = 0; i < result.length; i++) {
 			a[i] = result[i];
 		}
 	}
 	
 	/**
 	 * Sorts the input array using radix sort with a modified counting sort subroutine.
 	 * 
 	 * @param a - Array to sort
 	 */
 	public static void radixSort(int[] a) {
 		if (a != null && a.length > 1) {
 			int max = a[0];
 			for (int i : a) {
 				if (i > max) {
 					max = i;
 				}
 			}
 			for (int place = 1; max / place > 0; place *= 10) {
 				subSort(a, place);
 			}
 		}
 	}
 	
 	/**
 	 * Sorts a by the digit in certain place.
 	 * 
 	 * @param a - The array to sort
 	 * @param place - Digit to sort on
 	 */
 	private static void subSort(int[] a, int place) {
 		int[] count = new int[10];
 		for (int i : a) {
 			count[(i / place) % 10]++;
 		}
 		int sum = 0;
 		for (int i = 0; i < 10; i++) {
 			int temp = count[i];
 			count[i] = sum;
 			sum += temp;
 		}
 		int[] result = new int[a.length];
 		for (int i = 0; i < a.length; i++) {
 			int digit = (a[i] / place) % 10;
 			result[count[digit]] = a[i];
 			count[digit]++;
 		}
 		for (int i = 0; i < a.length; i++) {
 			a[i] = result[i];
 		}
 	}
 	
 	/**
 	 * Returns the distance between the two closest integers in a and b.
 	 * 
 	 * @param a - First sorted array
 	 * @param b - Second sorted array
 	 * @return The smallest distance between two integers in a and b
 	 */
 	public static int closestPair(int[] a, int[] b) {
 		int bestDistance = Integer.MAX_VALUE;
 		int aIndex = 0;
 		int bIndex = 0;
 		while (aIndex < a.length || bIndex < b.length) {
 			int aCompare = aIndex < a.length ? a[aIndex] : a[a.length - 1];
 			int bCompare = bIndex < b.length ? b[bIndex] : b[b.length - 1];
 			int distance = (int) Math.abs(aCompare - bCompare);
 			if (distance < bestDistance) {
 				bestDistance = distance;
 			}
 			if (aIndex < a.length && (aCompare < bCompare || bIndex >= a.length)) {
 				aIndex++;
 			} else {
 				bIndex++;
 			}
 		}
 		return bestDistance;
 	}
 }
