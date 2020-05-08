 import java.io.*;
 import java.util.Scanner;
 
 public class Runtime {
 	
	/*public static void main(String[] args) {
 		System.out.println("Runtime library for Javalette language.");
	}*/
 	
	static Scanner s = new Scanner(System.in);
 
 	// Print functions
 	public static void printInt(int i) {
 		System.out.println(i);
 	}
 	
 	public static void printDouble(double d) {
 		System.out.println(d);
 	}
 	
 	public static void printString(String s) {
 		System.out.println(s);
 	}
 	
 	// Read functions
 	public static int readInt() {
 		//Scanner s = new Scanner(System.in);
 		return s.nextInt();
 	}
 	
 	public static double readDouble() {
 		//Scanner s = new Scanner(System.in);
 		return s.nextDouble();
 	}
 	
 	
 }
