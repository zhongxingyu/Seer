 import java.io.*;
 import java.lang.*;
 
 public class InsertionTester {
 	public static void main(String[] args)
 	{
 		System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
<<<<<<< HEAD
 		System.loadLibrary("InsertionSorter");
 		int[] buffer = new int[]{5, 2, 9, 8, 3, 7};
 		double failRate = 0.5;
 		InsertionSorter sorter = new InsertionSorter(buffer, failRate);
 		sorter.run();
 		boolean result = sorter.getSuccess();
 		if(result)
 		{
 			System.out.println("C file suceeded.");
 		}		
 
 		for(int i = 0; i < buffer.length; i++)
 		{
 			System.out.println("result[" + i + "] = " + buffer[i]);
 		}
=======
		System.loadLibrary("InsertionSort");
		InsertionSort sorter = new InsertionSort();
		int[] buffer = new int[]{0,1};
		sorter.binarySort(buffer);
>>>>>>> c43d5ef7d079bce9effc7136ce463b2ae04194c2
 	}
 }
