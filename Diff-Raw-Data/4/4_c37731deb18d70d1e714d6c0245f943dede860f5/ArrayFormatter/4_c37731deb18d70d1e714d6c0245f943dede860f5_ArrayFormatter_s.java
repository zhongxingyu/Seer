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
		double[] array = { 3.0001, 3.2211, 5000.43425, 452738764.000325235252 };
		printArray(array, 4);
 	}
 }
