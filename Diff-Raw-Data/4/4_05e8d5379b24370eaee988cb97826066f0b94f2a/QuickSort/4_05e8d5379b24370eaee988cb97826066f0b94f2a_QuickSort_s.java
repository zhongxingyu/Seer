 /******************************************************************************
  * Copyright (C) 2013 David Rusk
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * IN THE SOFTWARE.
  *****************************************************************************/
 package rusk.david.algorithms.sorting;
 
 import rusk.david.algorithms.utils.ArrayUtils;
 
 public abstract class QuickSort implements SortingAlgorithm {
 
 	@Override
 	public void sort(int[] array) {
 		sort(array, 0, array.length - 1);
 	}
 
 	private void sort(int[] array, int startIndex, int endIndex) {
		// If n = 1 return
 		if (endIndex - startIndex < 1) {
 			return;
 		}
 
 		int pivotIndex = choosePivot(ArrayUtils.copySlice(array, startIndex,
 				endIndex + 1)) + startIndex;
 
 		assert pivotIndex >= startIndex && pivotIndex <= endIndex;
 
 		pivotIndex = partition(array, pivotIndex, startIndex, endIndex);
 
 		sort(array, startIndex, pivotIndex - 1);
		sort(array, pivotIndex + 1, array.length - 1);
 	}
 
 	/**
 	 * Partitions the array such that values to the left of the pivot are less
 	 * than the pivot and values to the right are greater than the pivot.
 	 * Essentially it puts the pivot in its correct position for the final
 	 * sorted order.
 	 * 
 	 * @param array
 	 *            the array to partition.
 	 * @param pivotIndex
 	 *            the index of the value to partition around.
 	 * @return the index that the pivot element ended up at.
 	 */
 	private int partition(int[] array, int pivotIndex, int startIndex,
 			int endIndex) {
 		int pivotValue = array[pivotIndex];
 
 		// Put the pivot at the start
 		swap(array, pivotIndex, startIndex);
 
 		// This keeps track of the index which at the end of the method should
 		// hold the pivot.
 		int rightPartitionStartIndex = startIndex + 1;
 
 		for (int currentIndex = startIndex + 1; currentIndex <= endIndex; currentIndex++) {
 			if (array[currentIndex] < pivotValue) {
 				swap(array, currentIndex, rightPartitionStartIndex);
 				rightPartitionStartIndex++;
 			}
 		}
 
 		// Put the pivot at its proper location.
 		swap(array, startIndex, rightPartitionStartIndex - 1);
 
 		return rightPartitionStartIndex - 1;
 	}
 
 	private void swap(int[] array, int index1, int index2) {
 		int temp = array[index1];
 		array[index1] = array[index2];
 		array[index2] = temp;
 	}
 
 	/**
 	 * Chooses the pivot around which to partition the array at each recursive
 	 * call.
 	 * 
 	 * @param array
 	 *            the current array, which are also the possible pivot values.
 	 * @return the index of the value to partition around.
 	 */
 	protected abstract int choosePivot(int[] array);
 
 }
