 package ch22.ex01;
 
 public class ArrayFormatter {
 	public static void printArray(double[] array, int queueCount) {
 		int count = 0;
 		if (array.length > queueCount) {
 			count = queueCount;
 		} else {
 			count = array.length;
 		}
 		for (int i = 0; i < count; i++) {
 			System.out.println(array[i]);
 		}
 	}
 	
 	
 	public static void main(String[] args) {
		double[] array = { 266.7878, 7.72878, 289.9022, 90.15671, 1781111.989 };
		printArray(array, 5);
 	}
 }
