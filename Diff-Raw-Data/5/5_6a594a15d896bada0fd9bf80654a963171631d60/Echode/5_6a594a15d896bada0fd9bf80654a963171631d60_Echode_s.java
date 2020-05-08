package com;
 
 import java.util.Scanner;
 
 public class Echode {
 	static Scanner scan;
 	private static String in;
 	
 	/**
 	* @param args
 	*/
 	public static void main(String[] args) {
 		scan = new Scanner(System.in);
 		intro();
 	}
 	
 	//i couldn't get the welcome message right, so thats why i made this
 	public static void intro() {
		System.out.println("Welcome to ECHODE version 0.2");
 		mainLoop();
 	}
 	
 	private static void mainLoop() {
 		while (true) {
 			System.out.print("->");
 			in = scan.nextLine();
 			parse(in);
 		}
 	}
 	
 	private static void parse(String in2) {
 		if (in2.equalsIgnoreCase("about")) {
 			System.out.println("Echode version 0.2.2\nMade by Erik Konijn and Marks Polakovs");
 		} else {
 			if (in2.equalsIgnoreCase("kill")){
 				System.out.println("Echode shut down succesfully.");
 				System.exit(0);
 			}else{
 				System.out.println("Not implemented yet.");
 			}
 		}
 		
 	}
 
 }
