 package de.carullojabro.sortalgorithms.test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import de.carullojabro.sortalgorithms.algorithms.Bucketsort;
 import de.carullojabro.sortalgorithms.algorithms.BubbleSort;
 import de.carullojabro.sortalgorithms.algorithms.Heapsort;
 import de.carullojabro.sortalgorithms.algorithms.ISortAlgorithm;
 import de.carullojabro.sortalgorithms.algorithms.InsertionSort;
 import de.carullojabro.sortalgorithms.algorithms.Quicksort;
 import de.carullojabro.sortalgorithms.algorithms.Selectionsort;
 
 public class AlgorithmTest {
 	public static void main(String[] args) {
 
		List<Integer> list = createRandomListM10(1000);
 		System.out.println(list.toString());
 
 		Bucketsort bucket = new Bucketsort(list);
 		list = bucket.sort();
 
 		System.out.println(list.toString());
 	}
 
 	/**
 	 * Generiert eine zufllige Liste mit werten zwischen 0-9
 	 * 
 	 * @param n
 	 *            Lnge der Liste
 	 */
 	public static List<Integer> createRandomListM10(Integer n) {
 		List<Integer> list = new ArrayList<Integer>(n);
 		Random rdm = new Random();
 		for (int i = 0; i < n; i++) {
 			list.add(rdm.nextInt(10));
 		}
 		return list;
 	}
 
 	public static List<Integer> createSortedList(Integer n) {
 		List<Integer> list = new ArrayList<Integer>(n);
 		for (int i = 0; i < n; i++) {
 			list.add(i);
 		}
 		return list;
 	}
 
 	public static List<Integer> createInverseList(Integer n) {
 		List<Integer> list = new ArrayList<Integer>(n);
 		for (int i = n - 1; i >= 0; i--) {
 			list.add(i);
 		}
 		return list;
 	}
 
 	public static List<Integer> randomSortedList(Integer n) {
 		return null;
 	}
 }
