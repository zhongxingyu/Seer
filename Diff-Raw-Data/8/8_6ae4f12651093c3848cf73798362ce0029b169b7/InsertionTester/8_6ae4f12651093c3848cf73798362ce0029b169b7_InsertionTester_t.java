 import java.io.*;
 import java.lang.*;
 
 public class InsertionTester {
 	public static void main(String[] args)
 	{
 		System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
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
 	}
 }
